from pathlib import Path

from fastapi import APIRouter
from fastapi.responses import FileResponse

router = APIRouter(tags=["admin"], include_in_schema=False)

_ADMIN_PAGE = Path(__file__).resolve().parents[2] / "static" / "admin.html"


@router.get("/admin")
def admin_portal() -> FileResponse:
    return FileResponse(_ADMIN_PAGE)
