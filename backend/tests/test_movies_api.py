from app.core.config import Settings, get_settings
from app.main import app

# What the app's ContentFilterInterceptor adds to discover / search requests.
APP_FILTERS = {"include_adult": "false", "certification_country": "US", "certification.lte": "PG-13"}


def test_discover_is_paginated_in_tmdb_shape(client):
    body = client.get("/discover/movie", params={"page": 1, **APP_FILTERS}).json()
    assert set(body) == {"page", "results", "total_pages", "total_results"}
    assert body["page"] == 1
    assert len(body["results"]) == 20
    assert body["total_results"] == 25
    assert body["total_pages"] == 2
    assert body["results"][0]["id"] == 1  # highest popularity first


def test_second_page_has_the_rest(client):
    assert len(client.get("/discover/movie", params={"page": 2}).json()["results"]) == 5


def test_page_must_be_positive(client):
    assert client.get("/discover/movie", params={"page": 0}).status_code == 422


def test_search_is_case_insensitive(client):
    body = client.get("/search/movie", params={"query": "movie 2", "include_adult": "false"}).json()
    ids = {m["id"] for m in body["results"]}
    assert 2 in ids and 20 in ids and 25 in ids


def test_search_treats_wildcards_literally(client):
    assert client.get("/search/movie", params={"query": "%"}).json()["total_results"] == 1
    assert client.get("/search/movie", params={"query": "_"}).json()["total_results"] == 1
    assert client.get("/search/movie", params={"query": "zzz"}).json()["results"] == []


def test_search_with_blank_query_returns_empty_page(client):
    for params in ({}, {"query": ""}, {"query": "   "}):
        body = client.get("/search/movie", params=params).json()
        assert body["results"] == [] and body["total_results"] == 0


def test_movie_details(client):
    assert client.get("/movie/3").json()["title"] == "Movie 3"


def test_missing_movie_is_404(client):
    assert client.get("/movie/999").status_code == 404


def test_patch_updates_and_notifies(client, notifier):
    response = client.patch("/movie/3", json={"title": "Renamed", "vote_average": 9.1})
    assert response.status_code == 200
    assert response.json()["title"] == "Renamed"
    assert client.get("/movie/3").json()["vote_average"] == 9.1
    assert client.get("/search/movie", params={"query": "Renamed"}).json()["total_results"] == 1
    assert notifier.calls == [3]


def test_patch_missing_movie_is_404_and_does_not_notify(client, notifier):
    assert client.patch("/movie/999", json={"title": "x"}).status_code == 404
    assert notifier.calls == []


def test_patch_rejects_empty_body_and_unknown_fields(client, notifier):
    assert client.patch("/movie/3", json={}).status_code == 422
    assert client.patch("/movie/3", json={"id": 5}).status_code == 422
    assert client.patch("/movie/3", json={"vote_average": 11}).status_code == 422
    assert notifier.calls == []


def test_patch_requires_admin_key_when_configured(client, notifier):
    app.dependency_overrides[get_settings] = lambda: Settings(admin_api_key="secret")

    assert client.patch("/movie/3", json={"title": "x"}).status_code == 401
    wrong = client.patch("/movie/3", json={"title": "x"}, headers={"X-Admin-Key": "nope"})
    assert wrong.status_code == 401
    right = client.patch("/movie/3", json={"title": "x"}, headers={"X-Admin-Key": "secret"})
    assert right.status_code == 200
    assert notifier.calls == [3]


def test_admin_page_is_served(client):
    response = client.get("/admin")
    assert response.status_code == 200
    assert "Movix Admin" in response.text


def test_swagger_has_a_single_movies_group(client):
    tags = {tag for path in client.get("/openapi.json").json()["paths"].values()
            for op in path.values() for tag in op.get("tags", [])}
    assert tags == {"movies"}


def test_notification_carries_the_new_title(client, notifier):
    client.patch("/movie/3", json={"title": "Fresh Title"})
    assert notifier.titles == ["Fresh Title"]


def test_notification_carries_the_poster_path(client, notifier):
    client.patch("/movie/3", json={"overview": "New text"})
    assert notifier.posters == [None]  # the test movies have no poster


def test_fcm_data_includes_poster_only_when_known():
    from unittest.mock import patch

    from app.services.notification_service import FcmNotifier

    notifier = FcmNotifier.__new__(FcmNotifier)
    notifier._topic = "movies"
    with patch("firebase_admin.messaging.send") as send:
        notifier.movie_updated(1, "Title", "/abc.jpg")
        notifier.movie_updated(2, "No poster")
    sent = [call.args[0].data for call in send.call_args_list]
    assert sent[0] == {"type": "movie_updated", "movieId": "1", "movieTitle": "Title", "posterPath": "/abc.jpg"}
    assert "posterPath" not in sent[1]