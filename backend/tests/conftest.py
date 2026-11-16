import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app import models  # noqa: F401
from app.api.deps import get_notifier
from app.core.config import Settings, get_settings
from app.db.base import Base
from app.db.session import get_db
from app.main import app
from app.models.movie import Movie


class FakeNotifier:
    def __init__(self) -> None:
        self.calls: list[int] = []

    def movie_updated(self, movie_id: int) -> None:
        self.calls.append(movie_id)


@pytest.fixture
def session_factory():
    engine = create_engine(
        "sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool
    )
    Base.metadata.create_all(engine)
    factory = sessionmaker(bind=engine, expire_on_commit=False)
    with factory() as db:
        for i in range(1, 26):
            title = f"Movie {i}"
            if i == 1:
                title = "100% Wolf_Pack"
            db.add(Movie(
                id=i, title=title, release_date="2020-01-01", popularity=float(100 - i),
                payload={"id": i, "title": title, "overview": "text", "release_date": "2020-01-01",
                         "vote_average": 7.0},
            ))
        db.commit()
    return factory


@pytest.fixture
def notifier() -> FakeNotifier:
    return FakeNotifier()


@pytest.fixture
def client(session_factory, notifier):
    def _db():
        with session_factory() as db:
            yield db

    app.dependency_overrides[get_db] = _db
    app.dependency_overrides[get_notifier] = lambda: notifier
    app.dependency_overrides[get_settings] = lambda: Settings(admin_api_key="")
    # No `with`: skips the lifespan, so the real database file is never created in tests.
    yield TestClient(app)
    app.dependency_overrides.clear()
