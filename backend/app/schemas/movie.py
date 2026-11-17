from datetime import date
from typing import Any

from pydantic import BaseModel, ConfigDict, Field, model_validator


class MoviePage(BaseModel):
    """Same envelope as TMDb list endpoints."""

    page: int
    results: list[dict[str, Any]]
    total_pages: int
    total_results: int


class MovieUpdate(BaseModel):
    """Fields an admin may change. Unknown fields are rejected; at least one is required."""

    model_config = ConfigDict(extra="forbid")

    title: str | None = Field(default=None, min_length=1, max_length=500)
    overview: str | None = None
    tagline: str | None = None
    release_date: date | None = None
    vote_average: float | None = Field(default=None, ge=0, le=10)

    @model_validator(mode="after")
    def _require_at_least_one_field(self) -> "MovieUpdate":
        if not self.model_dump(exclude_none=True):
            raise ValueError("Provide at least one field to update.")
        return self

    def changes(self) -> dict[str, Any]:
        """Only the fields that were sent, as JSON-safe values."""
        return self.model_dump(mode="json", exclude_none=True)
