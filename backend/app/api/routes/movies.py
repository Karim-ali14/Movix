from typing import Annotated, Any

from fastapi import APIRouter, BackgroundTasks, Depends, Query

from app.api.deps import get_movie_service, get_notifier, require_admin
from app.schemas.movie import MoviePage, MovieUpdate
from app.services.movie_service import MovieService
from app.services.notification_service import MovieNotifier

# Paths match the TMDb endpoints the Android app already calls (AppRouter), so the app
# only needs a new base URL. Content filters (include_adult, certification_*) are applied
# when the database is seeded, so the extra query parameters the app sends are ignored.
router = APIRouter(tags=["movies"])

ServiceDep = Annotated[MovieService, Depends(get_movie_service)]
PageQuery = Annotated[int, Query(ge=1, le=500, description="1-based page number")]


@router.get("/discover/movie", response_model=MoviePage)
def discover_movies(service: ServiceDep, page: PageQuery = 1) -> MoviePage:
    return service.list_movies(page)


@router.get("/search/movie", response_model=MoviePage)
def search_movies(
    service: ServiceDep,
    query: Annotated[str, Query(max_length=200)] = "",
    page: PageQuery = 1,
) -> MoviePage:
    # TMDb returns an empty page for a blank query instead of an error; keep that behavior.
    if not query.strip():
        return MoviePage(page=page, results=[], total_pages=0, total_results=0)
    return service.search_movies(query, page)


@router.get("/movie/{movie_id}")
def movie_details(movie_id: int, service: ServiceDep) -> dict[str, Any]:
    return service.get_movie(movie_id)


@router.patch("/movie/{movie_id}", dependencies=[Depends(require_admin)])
def update_movie(
    movie_id: int,
    body: MovieUpdate,
    service: ServiceDep,
    notifier: Annotated[MovieNotifier, Depends(get_notifier)],
    background_tasks: BackgroundTasks,
) -> dict[str, Any]:
    movie = service.update_movie(movie_id, body.changes())
    # Runs after the response is sent, so a slow or failing push never delays the edit.
    background_tasks.add_task(
        notifier.movie_updated, movie_id, str(movie.get("title", "")), movie.get("poster_path")
    )
    return movie