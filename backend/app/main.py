import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app import models  # noqa: F401  (registers tables on Base.metadata)
from app.api.routes import admin, movies
from app.core.config import get_settings
from app.db.base import Base
from app.db.session import engine
from app.services.movie_service import MovieNotFoundError


@asynccontextmanager
async def lifespan(_: FastAPI):
    Base.metadata.create_all(bind=engine)
    yield


def create_app() -> FastAPI:
    logging.basicConfig(level=logging.INFO, format="%(levelname)s:     %(name)s - %(message)s")

    app = FastAPI(title=get_settings().app_name, version="1.0.0", lifespan=lifespan)
    app.include_router(movies.router)
    app.include_router(admin.router)

    @app.exception_handler(MovieNotFoundError)
    async def _not_found(_: Request, exc: MovieNotFoundError) -> JSONResponse:
        return JSONResponse(status_code=404, content={"detail": str(exc)})

    return app


app = create_app()