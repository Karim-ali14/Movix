# Movix

Android movie app built with Kotlin, Jetpack Compose and Material 3, plus an optional custom backend
(FastAPI) that serves the movies and sends an FCM push notification when a movie is edited.

- Browse a paginated movie list, search by title, and open a details screen.
- Everything is cached in Room: cached data shows first, then refreshes in the background.
- Bonus: a custom backend with an **Edit Movie** endpoint. Editing a movie sends a push, and the app
  updates its cache and the screen in real time.
- Extra bonus: an **admin portal** to edit movies.

## Contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Quick start (TMDb only)](#quick-start-tmdb-only)
- [Running with the local backend](#running-with-the-local-backend)
- [Push notifications (FCM)](#push-notifications-fcm)
- [Tests](#tests)
- [Architectural decisions](#architectural-decisions)
- [Troubleshooting](#troubleshooting)
- [Known limitations](#known-limitations)
- [AI usage](#ai-usage)
- [Git workflow](#git-workflow)

## Features

- Paginated movie list (Paging 3) with loading and error states at the bottom of the list
- Search by title, with results updating as you type
- Movie details: synopsis, rating, genres, runtime, budget, revenue, production companies, links
- Offline-first caching with Room: cached data shows first, then refreshes in the background
- Domain error mapping: network and HTTP exceptions become clear, user-facing errors
- Loading skeletons, an offline banner over cached content, light and dark themes
- Live updates: a movie edited on the backend is refreshed in the app and announced with a
  notification that shows its title and poster
- Custom backend (FastAPI + SQLite) with pagination, search, an edit endpoint and an admin portal

## Tech stack

| Area         | Choice                                                        |
|--------------|---------------------------------------------------------------|
| Language     | Kotlin, coroutines and Flow                                   |
| UI           | Jetpack Compose, Material 3, Coil                             |
| Architecture | Clean Architecture + MVI                                      |
| DI           | Hilt                                                          |
| Network      | Retrofit, OkHttp, Gson                                        |
| Cache        | Room, Paging 3 (`RemoteMediator`)                             |
| Navigation   | Navigation Compose (type-safe routes)                         |
| Push         | Firebase Cloud Messaging                                      |
| Unit tests   | JUnit, MockK, Turbine, coroutines-test, Robolectric           |
| UI tests     | Compose UI test, Espresso                                     |
| Backend      | Python, FastAPI, SQLAlchemy, SQLite, `firebase-admin`, pytest |

## Architecture

Clean Architecture with MVI, one package per feature:

```
app/src/main/java/com/karimali/movix/
├── core/            database, network (interceptors, error mapping), mvi base, shared UI, fcm
└── features/
    ├── movies/      data / domain / presentation
    └── details/     data / domain / presentation

backend/             FastAPI service (see "Running with the local backend")
```

Each feature follows the same layers:

- **presentation:** composables, `ViewModel`, and the MVI contract (`UiState`, `UiIntent`,
  `UiEffect`)
- **domain:** models, repository interfaces and use cases. No Android or framework code
- **data:** remote and local data sources, DTOs, entities, mappers, repository implementations

Dependencies point inward: presentation depends on domain, data depends on domain, and domain
depends
on nothing.

### MVI

Each screen has one `UiState`, a set of `UiIntent`s the user can trigger, and one-shot `UiEffect`s
(navigation, opening a link). A shared `MviViewModel` exposes the state, receives intents through
`onIntent`, and emits effects. Screens are split into a stateful `Route` (owns the `ViewModel`) and
a
stateless `Screen` (only draws the state).

Paging 3 owns the list load state, so it is intentionally **not** copied into `MoviesState`.

### Caching flow

**Movies list**

1. `RemoteMediator` requests an initial refresh on start, while Room already shows the cached pages.
2. Fetched pages are written to Room together with their remote keys, in one transaction.
3. The UI only observes Room (`PagingSource`), so it updates when the latest data arrives.
4. If the refresh fails and there is cached data, the list stays visible with an offline banner.
   If there is no cached data, a full-screen error with retry is shown.
5. Errors while loading more pages appear in the footer of the list, with a retry button.

**Movie details**

1. The screen observes the movie in Room, so cached details show immediately.
2. A background refresh fetches the latest details and writes them to Room, which re-emits.
3. A failed refresh keeps the cached content and shows an offline banner. With no cache, it shows a
   full-screen error.

### Search

- Typing is debounced (400 ms). Clearing the box goes back to the cached list immediately.
- `flatMapLatest` cancels the previous search when the query changes.
- Search is online through a `PagingSource`. If the network fails, it falls back to a title search
  in the cached movies.

### Error handling

Network and HTTP exceptions are mapped once, in the data layer, to a sealed `DomainError`
(no internet, timeout, unauthorized, not found, rate limited, server error with its code, unknown).
Repositories throw a `DomainException` and use cases return `Result`. The UI converts a
`DomainError`
to a localized message in one place, so screens contain no exception-handling logic.

### Content filtering

The required TMDb filters are added centrally by `ContentFilterInterceptor`:

| Endpoint         | Parameters added                                                             |
|------------------|------------------------------------------------------------------------------|
| `discover/movie` | `include_adult=false`, `certification_country=US`, `certification.lte=PG-13` |
| `search/movie`   | `include_adult=false` (TMDb's search endpoint has no certification filter)   |

The backend applies the same filters once, when its database is seeded from TMDb, so it only ever
contains matching movies.

### Live updates (FCM)

```
Swagger / Postman / Admin portal
        │  PATCH /movie/{id}
        ▼
FastAPI backend ── saves the edit ──► FCM data message to topic "movies"
                                      { type, movieId, movieTitle, posterPath }
        ▼
MovieMessagingService (app)
  1. fetches GET /movie/{id}
  2. writes it to Room: the details row and the movies list row
  3. Room emits, so the open screen and the list update by themselves
  4. shows a notification with the movie title and poster
```

## Requirements

- Android Studio (latest stable) and JDK 17
- An Android emulator or device
- A free TMDb account
- For the backend: Python 3.12+
- For push notifications: a free Firebase project, and an emulator image **with Google Play**

## Quick start (TMDb only)

1. Create a TMDb **API Read Access Token**: TMDb account, Settings, API, "API Read Access Token".
2. Create or edit `local.properties` in the project root (it is git-ignored):

   ```properties
   tmdb.access.token=YOUR_TMDB_READ_ACCESS_TOKEN
   ```
3. Run the `app` configuration.

> **`tmdb.access.token` is required.** Without it the project will not build.
> If `api.base.url` is not set, the app talks to the **remote TMDb API**.

`local.properties.example` in the project root lists every supported key.

## Running with the local backend

### 1. Get the TMDb credentials (two different ones)

| Used by        | Credential                | Where to put it                          |
|----------------|---------------------------|------------------------------------------|
| Android app    | **API Read Access Token** | `local.properties` → `tmdb.access.token` |
| Backend (seed) | **API Key** (v3)          | `backend/.env` → `TMDB_API_KEY`          |

Both are on the same TMDb page: Settings, API.

### 2. Seed and start the backend

```bash
cd backend
python3.12 -m venv .venv
source .venv/bin/activate            # Windows: .venv\Scripts\activate
python -m pip install --upgrade pip
python -m pip install -r requirements-dev.txt
cp .env.example .env                 # Windows: copy .env.example .env
# edit .env and set TMDB_API_KEY, then seed once:
python -m scripts.seed --pages 10    # about 200 movies, takes a few minutes
uvicorn app.main:app --reload --host 0.0.0.0
```

- API docs (Swagger): http://localhost:8000/docs
- Admin portal: http://localhost:8000/admin
- To start over, delete `backend/movix.db` and seed again.

### 3. Point the app at the backend

Add to `local.properties`:

```properties
tmdb.access.token=YOUR_TMDB_READ_ACCESS_TOKEN
api.base.url=http://10.0.2.2:8000/
```

- `10.0.2.2` is how the **Android emulator** reaches your computer's `localhost`.
- On a **physical device**, use your computer's LAN IP (for example `http://192.168.1.20:8000/`)
  and keep both on the same Wi-Fi.
- The trailing `/` is required.
- Debug builds allow cleartext HTTP so they can reach the local backend. Release builds do not.

Sync Gradle and rebuild after changing `local.properties`.

### 4. Endpoints

The paths match the TMDb endpoints the app already calls, so only the base URL changes.

| Method | Path                                | Description                                                     |
|--------|-------------------------------------|-----------------------------------------------------------------|
| GET    | `/discover/movie?page=1`            | Paginated movie list (20 per page, `page` from 1 to 500)        |
| GET    | `/search/movie?query=batman&page=1` | Search by title. A blank query returns an empty page, like TMDb |
| GET    | `/movie/{movie_id}`                 | Movie details                                                   |
| PATCH  | `/movie/{movie_id}`                 | Edit a movie, then send an FCM push in the background           |
| GET    | `/admin`                            | Admin portal (HTML) to edit movies                              |

Responses have the same JSON shape as TMDb (`page`, `results`, `total_pages`, `total_results`).

The task suggests `PATCH /api/movies/{id}` as an example. This project uses
`PATCH /movie/{movie_id}`
so that every endpoint matches the paths the app already uses.

**Edit a movie:** send at least one of `title`, `overview`, `tagline`, `release_date` (`YYYY-MM-DD`)
and `vote_average` (0 to 10). Other fields are rejected with `422`.

```bash
curl -X PATCH http://localhost:8000/movie/1263337 \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: YOUR_ADMIN_KEY" \
  -d '{"title": "Edited title"}'
```

The `X-Admin-Key` header is only needed when `ADMIN_API_KEY` is set.

### 5. Admin portal

Open http://localhost:8000/admin. Search or page through the movies, press **Edit**, change the
title, overview, tagline, release date or rating, and save. Saving calls the same `PATCH` endpoint,
so the push is sent too. If `ADMIN_API_KEY` is set, type it in the field at the top of the page.
The portal only edits movies: it cannot create or delete them.

### Backend configuration (`backend/.env`)

| Variable                    | Required              | Description                                                                                          |
|-----------------------------|-----------------------|------------------------------------------------------------------------------------------------------|
| `TMDB_API_KEY`              | Only for seeding      | TMDb **API Key** (v3), used by `scripts/seed.py`                                                     |
| `FIREBASE_CREDENTIALS_PATH` | For push              | Path to the Firebase service-account JSON. Empty: no push is sent, it is only logged                 |
| `FCM_TOPIC`                 | No (default `movies`) | Topic the push is sent to. **Must match the topic the app subscribes to** (`TOPIC` in `MovixApp.kt`) |
| `ADMIN_API_KEY`             | Recommended           | If set, `PATCH` requests must send it in the `X-Admin-Key` header. Leave empty for local development |
| `DATABASE_URL`              | No (default SQLite)   | Database connection string                                                                           |

More backend details are in [`backend/README.md`](backend/README.md).

## Push notifications (FCM)

Push needs **two Firebase files**. They are credentials, so they are **not in this repository**.
Without them the app and the backend still run, but no push is sent or received.

### A. `google-services.json` (Android app)

1. Go to https://console.firebase.google.com and create a project (or use an existing one).
2. Add an **Android app** with package name `com.karimali.movix`.
3. Download `google-services.json` and place it at `app/google-services.json`.

> - The file must be named exactly `google-services.json` and sit directly inside `app/`.
> - The Firebase Android app package name must be `com.karimali.movix`.
> - It is optional when you only want to run the app without push notifications.

### B. Service account key (backend)

1. In the same Firebase project: **Project settings, Service accounts, Generate new private key**.
2. Save the file as `backend/firebase-service-account.json`.
3. Set it in `backend/.env`:

   ```
   FIREBASE_CREDENTIALS_PATH=firebase-service-account.json
   FCM_TOPIC=movies
   ```

> **Both files must come from the same Firebase project.** Never commit either file. They are
> listed in `.gitignore`.

### Test it end to end

1. Start the backend, then run the app on an emulator **with Google Play** and open a movie.
   On Android 13+ allow the notification permission when asked.
2. Open http://localhost:8000/docs and call `PATCH /movie/{movie_id}` with
   `{"title": "Edited title"}` (or edit the movie in the admin portal).
3. The new title appears on the open screen and in the list without restarting, and a notification
   with the movie title and poster is shown. It also works with the app in the background.

The backend log shows `FCM sent for movie ...` when Firebase accepts the message.

## Tests

```bash
./gradlew testDebugUnitTest          # unit tests: domain, data, ViewModels
./gradlew connectedDebugAndroidTest  # UI tests (needs a running emulator or device)

cd backend
python -m pip install -r requirements-dev.txt
pytest                               # backend API tests
```

- **Domain and data layers:** use cases, repositories, mappers and the remote mediator, with MockK.
- **ViewModels:** state, intents and effects, with Turbine and coroutines-test.
- **UI tests:** Compose UI tests for the screens.
- **Backend:** API tests run against an in-memory database with a fake notifier, so they need no
  TMDb key and no Firebase.

## Architectural decisions

1. **Room is the single source of truth.** The UI never reads network results directly. This gives
   offline support and makes real-time updates simple: any write to Room updates the screen.
2. **Paging 3 with `RemoteMediator`** for the list, so pagination, cache and load states come from
   one standard mechanism instead of hand-written scroll listeners.
3. **MVI with a one-way data flow.** One state per screen, explicit intents, and effects for
   one-shot events. This keeps screens predictable and easy to test.
4. **Errors are mapped once.** A single `DomainError` type means screens never inspect exceptions.
5. **Content filters live in one interceptor**, so no endpoint can forget them. The search endpoint
   only gets `include_adult` because TMDb does not support certification filters there.
6. **The base URL comes from `local.properties` through `BuildConfig`.** Nobody's IP address is
   committed, and the app defaults to TMDb when nothing is configured.
7. **The backend owns its data.** It is seeded once from TMDb instead of proxying it, so edits are
   not overwritten by a cache refresh. It stores each movie as TMDb-shaped JSON, so responses match
   what the app already parses, and title, date and popularity are real columns for search and sort.
8. **FCM data message to a topic.** The app subscribes once, so the backend stores no device tokens
   and needs no registration endpoint. A data message always reaches the app's code, even in the
   background, so the app can update its cache. The app then shows the notification itself. A
   `notification` payload would be shown by the system without running any app code.
9. **The push is sent in a background task**, so a slow or failing push never delays or fails the
   edit.
10. **A fresh movie updates both Room tables.** The details refresh also updates the movie's row in
    the
    list with a partial update (only the displayed columns), so the movie keeps its page and
    position.
11. **Write protection is optional.** With `ADMIN_API_KEY` set, `PATCH` needs the `X-Admin-Key`
    header. This is meant for local and demo use, not for production authentication.

## Troubleshooting

| Problem                                    | Fix                                                                                                                                                                                                        |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Build fails with a missing token           | Add `tmdb.access.token` to `local.properties`                                                                                                                                                              |
| App shows TMDb data instead of local data  | `api.base.url` is missing or has no trailing `/`. Rebuild after changing it                                                                                                                                |
| `CLEARTEXT communication not permitted`    | Use a **debug** build. Release builds only allow HTTPS                                                                                                                                                     |
| App can't reach the backend on an emulator | Use `10.0.2.2`, not `localhost`                                                                                                                                                                            |
| App can't reach the backend on a device    | Use your computer's LAN IP, the same Wi-Fi, and start uvicorn with `--host 0.0.0.0`                                                                                                                        |
| The movie list is empty                    | Run `python -m scripts.seed --pages 10` in `backend/`                                                                                                                                                      |
| Seeding stops with "TMDB_API_KEY is empty" | Set the TMDb **API Key** (v3) in `backend/.env`                                                                                                                                                            |
| `PATCH` returns `422`                      | Check the JSON (no trailing comma), send only the editable fields, and at least one of them                                                                                                                |
| `PATCH` returns `401`                      | `ADMIN_API_KEY` is set: send it in the `X-Admin-Key` header                                                                                                                                                |
| `google-services.json` missing             | Push is disabled. Follow "Push notifications" A and place it at `app/google-services.json`                                                                                                                 |
| No push received                           | Both Firebase files must be from the same project, the emulator needs Google Play, the app must not be force-stopped, and the topic must match `FCM_TOPIC`. The first topic subscription can take a minute |
| Push received but no notification shown    | Allow the notification permission (Android 13+)                                                                                                                                                            |
| `Address already in use`                   | Another server uses port 8000: `lsof -ti :8000 \| xargs kill`                                                                                                                                              |
| Python version problems                    | Use Python 3.12 or newer                                                                                                                                                                                   |

## Known limitations

- Search results come from the network and are not observed from Room, so an edit shows in search
  results on the next search. The list and details update immediately.
- Android does not deliver FCM messages to an app the user force-stopped.
- The admin portal has no user accounts, only the optional shared key.
- The backend serves a seeded copy of TMDb data, not live TMDb data. Run the seed again to refresh
  it.

## AI usage

AI was used as an engineering collaborator, while every decision, change and result was
reviewed and verified by the author.

- **Architecture and MVI:** discussed the project structure (Clean Architecture, feature-first
  packages) and the MVI pattern, then compared options before choosing.
- **Code review:** reviewed the presentation layer against the project's own principles, listing
  only
  real problems (bugs, inconsistencies, broken conventions). Each finding was checked before
  applying.
- **Backend:** scaffolded the FastAPI service (layered structure, seed script, endpoints, admin
  portal, FCM sender) from the task requirements, then reviewed and adjusted it so its endpoints
  match the app's existing API contract.
- **Push and caching update:** helped design the FCM data message and the flow that updates Room.
- **Documentation:** drafted and corrected parts of this README.
- **Verification:** generated code was run and checked with unit tests, backend tests (`pytest`),
  and manual end-to-end runs on an emulator, not accepted on trust.

## Git workflow

Git Flow: `main` holds released versions, `develop` is the integration branch, and work happens in
`feature/*` branches merged with `--no-ff` through pull requests. Commits follow Conventional
Commits (`feat`, `fix`, `refactor`, `test`, `docs`, `chore`).