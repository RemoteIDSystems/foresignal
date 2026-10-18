"""Error types of the hand-written SDK layer (they depend on httpx only, not on the generated client)."""
from __future__ import annotations

import email.utils
import json
import time
from typing import Any

import httpx


class ApiError(Exception):
    """A non-2xx answer from the Foresignal API."""

    def __init__(self, status: int, problem: dict[str, Any] | None = None,
                 retry_after: float | None = None, message: str | None = None) -> None:
        """Creates the error from the HTTP status, the problem document and a ``Retry-After`` in seconds."""
        problem = problem or None
        text = message or (problem or {}).get("detail") or (problem or {}).get("title") or f"HTTP {status}"
        super().__init__(text)
        self.status = status
        self.problem = problem
        self.code: str | None = problem.get("code") if problem and isinstance(problem.get("code"), str) else None
        self.retry_after = retry_after


class ResyncRequiredError(ApiError):
    """``410 RESYNC_REQUIRED`` from ``GET /v1/changes``: the cursor is below the purge horizon.

    Re-snapshot with ``/v1/records?order=id`` and continue from its ``watermarkSeq``.
    """

    def __init__(self, problem: dict[str, Any] | None, retry_after: float | None = None) -> None:
        """Creates the error from the 410 problem document."""
        super().__init__(410, problem, retry_after,
                         (problem or {}).get("detail") or "Resync required: the cursor is below the purge horizon")
        horizon = (problem or {}).get("purgeHorizon")
        watermark = (problem or {}).get("watermarkSeq")
        self.purge_horizon: int | None = horizon if isinstance(horizon, int) else None
        self.watermark_seq: int | None = watermark if isinstance(watermark, int) else None


class ObjectRefError(ValueError):
    """A malformed object reference (docs/Client-Contract.md section 2)."""

    def __init__(self, ref: str, message: str) -> None:
        """Creates the error for ``ref``."""
        shown = ref if len(ref) <= 80 else ref[:80] + "…"
        super().__init__(f"Invalid object reference {shown!r}: {message}")
        self.ref = ref


class ContextValidationError(ValueError):
    """A workspace context that breaks the schema version 1 rules (docs/Client-Contract.md section 5)."""

    def __init__(self, errors: list[dict[str, str]]) -> None:
        """Creates the error from its field errors (``{"field", "message"}``)."""
        super().__init__("The workspace context is not valid: "
                         + "; ".join(f"{e['field']} {e['message']}" for e in errors))
        self.errors = errors


class DeepLinkError(ValueError):
    """A deep link that cannot be encoded or decoded; ``kind`` is version, malformed, context or too-large."""

    def __init__(self, kind: str, message: str) -> None:
        """Creates the error."""
        super().__init__(message)
        self.kind = kind


class HeartbeatTimeout(Exception):
    """No event or heartbeat arrived within the watchdog timeout."""

    def __init__(self, timeout: float) -> None:
        """Creates the error for a timeout in seconds."""
        super().__init__(f"No event or heartbeat within {timeout:g} s")
        self.timeout = timeout


def parse_retry_after(value: str | None, now: float | None = None) -> float | None:
    """Parses a ``Retry-After`` header (delta seconds or an HTTP date) into seconds."""
    if value is None or not value.strip():
        return None
    text = value.strip()
    if text.isdigit():
        return float(text)
    try:
        at = email.utils.parsedate_to_datetime(text)
    except (TypeError, ValueError):
        return None
    if at is None:
        return None
    return max(0.0, at.timestamp() - (time.time() if now is None else now))


def api_error_from(response: httpx.Response) -> ApiError:
    """Builds the error for a non-2xx response (``ResyncRequiredError`` for 410 RESYNC_REQUIRED).

    The response body must already be read (``response.read()`` for streamed responses).
    """
    problem: dict[str, Any] | None = None
    try:
        parsed = json.loads(response.content.decode("utf-8")) if response.content else None
        if isinstance(parsed, dict):
            problem = parsed
    except (ValueError, UnicodeDecodeError):
        problem = None
    retry_after = parse_retry_after(response.headers.get("Retry-After"))
    if response.status_code == 410 and problem and problem.get("code") == "RESYNC_REQUIRED":
        return ResyncRequiredError(problem, retry_after)
    return ApiError(response.status_code, problem, retry_after)
