"""Fills the local database with movies from TMDb. Run once from the backend/ folder:

    python -m scripts.seed --pages 10

Uses the same content filters the task requires (no adult, US certification up to PG-13).
"""

import argparse
import logging
import time

import httpx

from app import models  # noqa: F401
from app.core.config import get_settings
from app.db.base import Base
from app.db.session import SessionLocal, engine
from app.models.movie import Movie
from app.repositories.movie_repository import MovieRepository

logger = logging.getLogger("seed")

DISCOVER_FILTERS = {
    "include_adult": "false",
    "certification_country": "US",
    "certification.lte": "PG-13",
    "sort_by": "popularity.desc",
}
MAX_RETRIES = 3


def _get(client: httpx.Client, path: str, params: dict) -> dict:
    """GET with a small retry for rate limits (429) and transient network errors."""
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            response = client.get(path, params=params)
            if response.status_code == 429:
                time.sleep(float(response.headers.get("Retry-After", "2")))
                continue
            response.raise_for_status()
            return response.json()
        except httpx.TransportError:
            if attempt == MAX_RETRIES:
                raise
            time.sleep(attempt)
    raise RuntimeError(f"TMDb kept rate-limiting {path}")


def _to_movie(details: dict) -> Movie:
    # Detail responses have `genres`; list responses have `genre_ids`. Keep both so one payload serves both.
    details["genre_ids"] = [g["id"] for g in details.get("genres", [])]
    return Movie(
        id=details["id"],
        title=details.get("title") or "",
        release_date=details.get("release_date") or None,
        popularity=float(details.get("popularity") or 0.0),
        payload=details,
    )


def seed(pages: int) -> int:
    settings = get_settings()
    if not settings.tmdb_api_key:
        raise SystemExit("TMDB_API_KEY is empty. Set it in backend/.env first.")

    Base.metadata.create_all(bind=engine)
    saved = 0

    with httpx.Client(base_url=settings.tmdb_base_url, timeout=15) as client, SessionLocal() as db:
        repo = MovieRepository(db)
        for page in range(1, pages + 1):
            listing = _get(client, "/discover/movie", {
                "api_key": settings.tmdb_api_key, "page": page, **DISCOVER_FILTERS,
            })
            for item in listing.get("results", []):
                if item.get("adult"):
                    continue
                details = _get(client, f"/movie/{item['id']}", {"api_key": settings.tmdb_api_key})
                repo.upsert(_to_movie(details))
                saved += 1
            logger.info("Page %s/%s done (%s movies so far)", page, pages, saved)
    return saved


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(message)s")
    parser = argparse.ArgumentParser(description="Seed the Movix database from TMDb.")
    parser.add_argument("--pages", type=int, default=10, help="Discover pages to fetch (20 movies each).")
    args = parser.parse_args()
    logger.info("Seeded %s movies.", seed(args.pages))


if __name__ == "__main__":
    main()
