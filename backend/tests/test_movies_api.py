from app.core.config import Settings, get_settings
from app.main import app


def test_list_is_paginated_in_tmdb_shape(client):
    body = client.get("/api/movies", params={"page": 1}).json()
    assert body["page"] == 1
    assert len(body["results"]) == 20
    assert body["total_results"] == 25
    assert body["total_pages"] == 2
    assert body["results"][0]["id"] == 1  # highest popularity first


def test_second_page_has_the_rest(client):
    body = client.get("/api/movies", params={"page": 2}).json()
    assert len(body["results"]) == 5


def test_page_must_be_positive(client):
    assert client.get("/api/movies", params={"page": 0}).status_code == 422


def test_search_is_case_insensitive(client):
    body = client.get("/api/movies/search", params={"query": "movie 2"}).json()
    ids = {m["id"] for m in body["results"]}
    assert 2 in ids and 20 in ids and 25 in ids


def test_search_treats_wildcards_literally(client):
    assert client.get("/api/movies/search", params={"query": "%"}).json()["total_results"] == 1
    assert client.get("/api/movies/search", params={"query": "_"}).json()["total_results"] == 1
    assert client.get("/api/movies/search", params={"query": "zzz"}).json()["results"] == []


def test_search_requires_a_query(client):
    assert client.get("/api/movies/search").status_code == 422


def test_get_movie(client):
    assert client.get("/api/movies/3").json()["title"] == "Movie 3"


def test_get_missing_movie_is_404(client):
    assert client.get("/api/movies/999").status_code == 404


def test_patch_updates_and_notifies(client, notifier):
    response = client.patch("/api/movies/3", json={"title": "Renamed", "vote_average": 9.1})
    assert response.status_code == 200
    assert response.json()["title"] == "Renamed"
    assert client.get("/api/movies/3").json()["vote_average"] == 9.1
    assert client.get("/api/movies/search", params={"query": "Renamed"}).json()["total_results"] == 1
    assert notifier.calls == [3]


def test_patch_missing_movie_is_404_and_does_not_notify(client, notifier):
    assert client.patch("/api/movies/999", json={"title": "x"}).status_code == 404
    assert notifier.calls == []


def test_patch_rejects_empty_body_and_unknown_fields(client, notifier):
    assert client.patch("/api/movies/3", json={}).status_code == 422
    assert client.patch("/api/movies/3", json={"id": 5}).status_code == 422
    assert client.patch("/api/movies/3", json={"vote_average": 11}).status_code == 422
    assert notifier.calls == []


def test_patch_requires_admin_key_when_configured(client, notifier):
    app.dependency_overrides[get_settings] = lambda: Settings(admin_api_key="secret")

    assert client.patch("/api/movies/3", json={"title": "x"}).status_code == 401
    wrong = client.patch("/api/movies/3", json={"title": "x"}, headers={"X-Admin-Key": "nope"})
    assert wrong.status_code == 401
    right = client.patch("/api/movies/3", json={"title": "x"}, headers={"X-Admin-Key": "secret"})
    assert right.status_code == 200
    assert notifier.calls == [3]


def test_admin_page_is_served(client):
    response = client.get("/admin")
    assert response.status_code == 200
    assert "Movix Admin" in response.text

APP_FILTERS = {"include_adult": "false", "certification_country": "US", "certification.lte": "PG-13"}


def test_discover_matches_the_app_endpoint(client):
    body = client.get("/discover/movie", params={"page": 2, **APP_FILTERS}).json()
    assert body["page"] == 2 and len(body["results"]) == 5
    assert set(body) == {"page", "results", "total_pages", "total_results"}


def test_search_matches_the_app_endpoint(client):
    body = client.get("/search/movie", params={"query": "Movie 3", "page": 1, "include_adult": "false"}).json()
    assert [m["id"] for m in body["results"]] == [3]


def test_search_with_blank_query_returns_empty_page(client):
    body = client.get("/search/movie", params={"query": ""}).json()
    assert body["results"] == [] and body["total_results"] == 0


def test_movie_details_matches_the_app_endpoint(client):
    assert client.get("/movie/3").json()["id"] == 3
    assert client.get("/movie/999").status_code == 404


def test_edit_is_visible_through_the_app_endpoints(client):
    client.patch("/api/movies/3", json={"title": "Edited"})
    assert client.get("/movie/3").json()["title"] == "Edited"
    assert client.get("/search/movie", params={"query": "Edited"}).json()["total_results"] == 1