import secrets
from functools import lru_cache
from typing import Annotated

from fastapi import Depends, HTTPException, Security, status
from fastapi.security import APIKeyHeader
from sqlalchemy.orm import Session

from app.core.config import Settings, get_settings
from app.db.session import get_db
from app.repositories.movie_repository import MovieRepository
from app.services.movie_service import MovieService
from app.services.notification_service import MovieNotifier, build_notifier

_api_key_header = APIKeyHeader(name="X-Admin-Key", auto_error=False)


@lru_cache
def get_notifier() -> MovieNotifier:
    return build_notifier(get_settings())


def get_movie_service(
    db: Annotated[Session, Depends(get_db)],
    settings: Annotated[Settings, Depends(get_settings)],
) -> MovieService:
    return MovieService(MovieRepository(db), settings.page_size)


def require_admin(
    api_key: Annotated[str | None, Security(_api_key_header)],
    settings: Annotated[Settings, Depends(get_settings)],
) -> None:
    """Protects write endpoints when ADMIN_API_KEY is set; open when it is empty (local dev)."""
    if not settings.admin_api_key:
        return
    if not secrets.compare_digest(api_key or "", settings.admin_api_key):
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Invalid or missing admin key")
