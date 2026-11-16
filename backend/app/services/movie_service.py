import math
from typing import Any

from app.repositories.movie_repository import MovieRepository
from app.schemas.movie import MoviePage


class MovieNotFoundError(Exception):
    def __init__(self, movie_id: int) -> None:
        super().__init__(f"Movie {movie_id} not found")
        self.movie_id = movie_id


class MovieService:
    """Business rules for movies: pagination envelope, lookup and updates."""

    def __init__(self, repository: MovieRepository, page_size: int) -> None:
        self._repo = repository
        self._page_size = page_size

    def list_movies(self, page: int) -> MoviePage:
        movies, total = self._repo.list_page(page, self._page_size)
        return self._to_page(page, movies, total)

    def search_movies(self, query: str, page: int) -> MoviePage:
        movies, total = self._repo.search_page(query.strip(), page, self._page_size)
        return self._to_page(page, movies, total)

    def get_movie(self, movie_id: int) -> dict[str, Any]:
        movie = self._repo.get(movie_id)
        if movie is None:
            raise MovieNotFoundError(movie_id)
        return movie.payload

    def update_movie(self, movie_id: int, changes: dict[str, Any]) -> dict[str, Any]:
        movie = self._repo.get(movie_id)
        if movie is None:
            raise MovieNotFoundError(movie_id)

        # Assign a new dict so SQLAlchemy sees the JSON column as changed.
        movie.payload = {**movie.payload, **changes}
        if "title" in changes:
            movie.title = changes["title"]
        if "release_date" in changes:
            movie.release_date = changes["release_date"]

        self._repo.save(movie)
        return movie.payload

    def _to_page(self, page: int, movies: list, total: int) -> MoviePage:
        return MoviePage(
            page=page,
            results=[m.payload for m in movies],
            total_pages=math.ceil(total / self._page_size),
            total_results=total,
        )
