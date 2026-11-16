import logging
from typing import Protocol

from app.core.config import Settings

logger = logging.getLogger(__name__)

EVENT_MOVIE_UPDATED = "movie_updated"


class MovieNotifier(Protocol):
    def movie_updated(self, movie_id: int, title: str, poster_path: str | None = None) -> None: ...


class LoggingNotifier:
    """Used when Firebase is not configured: the event is only logged."""

    def movie_updated(self, movie_id: int, title: str, poster_path: str | None = None) -> None:
        logger.info("FCM disabled. Would notify: movie %s (%s) updated", movie_id, title)


class FcmNotifier:
    """Sends a data message to a topic. The app refreshes its cache and shows the notification itself."""

    def __init__(self, credentials_path: str, topic: str) -> None:
        import firebase_admin
        from firebase_admin import credentials

        if not firebase_admin._apps:
            firebase_admin.initialize_app(credentials.Certificate(credentials_path))
        self._topic = topic

    def movie_updated(self, movie_id: int, title: str, poster_path: str | None = None) -> None:
        from firebase_admin import messaging

        data = {"type": EVENT_MOVIE_UPDATED, "movieId": str(movie_id), "movieTitle": title}
        if poster_path:  # FCM data values must be strings, so the key is simply left out when unknown.
            data["posterPath"] = poster_path

        message = messaging.Message(
            topic=self._topic,
            data=data,
            android=messaging.AndroidConfig(priority="high"),
        )
        try:
            message_id = messaging.send(message)
            logger.info("FCM sent for movie %s (%s)", movie_id, message_id)
        except Exception:  # A push failure must never break the edit that triggered it.
            logger.exception("FCM send failed for movie %s", movie_id)


def build_notifier(settings: Settings) -> MovieNotifier:
    if settings.firebase_credentials_path:
        return FcmNotifier(settings.firebase_credentials_path, settings.fcm_topic)
    logger.warning("FIREBASE_CREDENTIALS_PATH is empty: push notifications are disabled.")
    return LoggingNotifier()