# Movix Backend

FastAPI + SQLite service that serves movies in the **same JSON shape as TMDb**, so the Android app only changes its base URL. Editing a movie sends an **FCM data message** that triggers the app's cache refresh.

## Architecture

```
app/
├── main.py                  App factory, lifespan, error handlers
├── core/config.py           Settings from environment / .env
├── db/                      Engine, session, declarative Base
├── models/movie.py          Movie table (searchable columns + full TMDb payload as JSON)
├── schemas/movie.py         Request/response contracts
├── repositories/            All SQL (pagination, search)
├── services/
│   ├── movie_service.py     Business rules (page envelope, updates, not-found)
│   └── notification_service.py   FCM sender (falls back to logging)
├── api/
│   ├── deps.py              Dependency wiring + admin-key check
│   └── routes/              movies.py (REST), admin.py (portal page)
└── static/admin.html        Admin portal (single page, no framework)
scripts/seed.py              One-time import from TMDb
tests/                       API tests (in-memory SQLite, fake notifier)
```

Request flow: `route -> service -> repository -> database`. Routes know nothing about SQL; the repository knows nothing about HTTP.

### Decisions

- **Own database, seeded once from TMDb.** Edits must survive; a live proxy/cache would overwrite them.
- **TMDb-shaped payload stored as JSON.** `title`, `release_date`, `popularity` are real columns (search and sorting); everything else lives in `payload`, so no mapping layer is needed.
- **FCM data message to a topic** (`movies`). The app subscribes once; no token storage or registration endpoint. Data messages reach the app in the background so it can refresh Room silently.
- **Push runs in a background task.** A failing push never fails or slows the edit.
- **Admin key is optional.** If `ADMIN_API_KEY` is set, `PATCH` requires the `X-Admin-Key` header; if empty (local dev) it is open.

## Run locally

Requires Python 3.11+.

```bash
cd backend
python -m venv .venv
# Windows: .venv\Scripts\activate      Mac/Linux: source .venv/bin/activate
pip install -r requirements-dev.txt
cp .env.example .env        # Windows: copy .env.example .env
```

Fill `.env`:

| Variable | Purpose |
|---|---|
| `TMDB_API_KEY` | Needed only for seeding |
| `FIREBASE_CREDENTIALS_PATH` | Path to the Firebase service-account JSON. Empty = notifications are only logged |
| `FCM_TOPIC` | Topic the app subscribes to (default `movies`) |
| `ADMIN_API_KEY` | Optional write protection |

Seed and start:

```bash
python -m scripts.seed --pages 10          # 10 pages = ~200 movies
uvicorn app.main:app --reload --host 0.0.0.0
```

- Swagger UI: http://localhost:8000/docs
- Admin portal: http://localhost:8000/admin
- Android emulator reaches the backend at `http://10.0.2.2:8000/`. The debug build needs `usesCleartextTraffic`.

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/movies?page=1` | Paginated list (20 per page): `page`, `results`, `total_pages`, `total_results` |
| GET | `/api/movies/search?query=...&page=1` | Case-insensitive title search |
| GET | `/api/movies/{id}` | Full details |
| PATCH | `/api/movies/{id}` | Edit `title`, `overview`, `tagline`, `release_date`, `vote_average`; then sends FCM |

FCM payload: `{ "type": "movie_updated", "movieId": "123" }`.

## Firebase setup

1. Create a project at https://console.firebase.google.com and add the Android app (same package name).
2. Put its `google-services.json` in the Android `app/` folder.
3. Project settings -> Service accounts -> Generate new private key. Save it in `backend/` and point `FIREBASE_CREDENTIALS_PATH` at it. It is git-ignored; never commit it.

## Tests

```bash
pytest
```
