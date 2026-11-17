from __future__ import annotations

from sqlalchemy import Select, func, select
from sqlalchemy.orm import Session

from app.models.movie import Movie


def _escape_like(value: str) -> str:
    """Make %, _ and \\ in user input match literally."""
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")


class MovieRepository:
    """All database access for movies. No business rules here."""

    def __init__(self, db: Session) -> None:
        self._db = db

    def list_page(self, page: int, page_size: int) -> tuple[list[Movie], int]:
        return self._paginate(select(Movie), page, page_size)

    def search_page(self, query: str, page: int, page_size: int) -> tuple[list[Movie], int]:
        pattern = f"%{_escape_like(query)}%"
        stmt = select(Movie).where(Movie.title.ilike(pattern, escape="\\"))
        return self._paginate(stmt, page, page_size)

    def get(self, movie_id: int) -> Movie | None:
        return self._db.get(Movie, movie_id)

    def save(self, movie: Movie) -> Movie:
        self._db.add(movie)
        self._db.commit()
        return movie

    def upsert(self, movie: Movie) -> Movie:
        merged = self._db.merge(movie)
        self._db.commit()
        return merged

    def _paginate(self, stmt: Select, page: int, page_size: int) -> tuple[list[Movie], int]:
        total = self._db.scalar(select(func.count()).select_from(stmt.subquery())) or 0
        rows = self._db.scalars(
            stmt.order_by(Movie.popularity.desc(), Movie.id)
            .limit(page_size)
            .offset((page - 1) * page_size)
        ).all()
        return list(rows), total