from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Application settings, read from environment variables or a local .env file."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_name: str = "Movix API"
    database_url: str = "sqlite:///./movix.db"
    page_size: int = 20

    tmdb_api_key: str = ""
    tmdb_base_url: str = "https://api.themoviedb.org/3"

    firebase_credentials_path: str = ""
    fcm_topic: str = "movies"

    admin_api_key: str = ""


@lru_cache
def get_settings() -> Settings:
    return Settings()
