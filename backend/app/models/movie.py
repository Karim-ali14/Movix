from __future__ import annotations
from datetime import datetime, timezone
from typing import Any, Optional

from sqlalchemy import JSON, DateTime, Float, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


def _utcnow() -> datetime:
    return datetime.now(timezone.utc)


class Movie(Base):
    """A movie row.

    `title`, `release_date` and `popularity` are real columns because we search and sort on them.
    `payload` holds the complete TMDb-shaped JSON, so the API returns exactly what the app expects.
    """

    __tablename__ = "movies"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=False)
    title: Mapped[str] = mapped_column(String(500), index=True)
    release_date: Mapped[Optional[str]] = mapped_column(String(10), nullable=True)
    popularity: Mapped[float] = mapped_column(Float, default=0.0, index=True)
    payload: Mapped[dict[str, Any]] = mapped_column(JSON)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=_utcnow, onupdate=_utcnow
    )