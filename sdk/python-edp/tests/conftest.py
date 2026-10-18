"""Shared test fixtures.

The EDP 1.0 conformance fixtures of prompt 024 are **referenced by relative path**, not copied:
``../../src/test/resources/edp/conformance`` from this library's root (the repository's single
copy, also used by the server's ``EdpManifestServiceTest`` and ``EdpValidationTest``). Set
``FORESIGNAL_EDP_CONFORMANCE_DIR`` to run the suite outside the repository.
"""

from __future__ import annotations

import json
import os
from dataclasses import dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Mapping

import pytest

from foresignal_edp.push import TransportError, TransportResponse

LIBRARY_ROOT = Path(__file__).resolve().parents[1]
REPOSITORY_ROOT = LIBRARY_ROOT.parents[1]
CONFORMANCE_DIR = Path(os.environ.get("FORESIGNAL_EDP_CONFORMANCE_DIR",
                                      REPOSITORY_ROOT / "src" / "test" / "resources" / "edp" / "conformance"))
EXPORTED_SCHEMAS_DIR = REPOSITORY_ROOT / "docs" / "protocol" / "edp-1.0"

#: The receipt time the conformance batches are written against (their sentAt).
CONFORMANCE_RECEIVED_AT = datetime(2026, 10, 11, 18, 0, 0, tzinfo=timezone.utc)


def load_json(path: Path) -> Any:
    """Reads a JSON file."""
    with open(path, encoding="utf-8") as f:
        return json.load(f)


@pytest.fixture(scope="session")
def conformance_dir() -> Path:
    """The conformance fixture directory (skips the test when it is not available)."""
    if not CONFORMANCE_DIR.is_dir():
        pytest.skip(f"EDP conformance fixtures not found at {CONFORMANCE_DIR}")
    return CONFORMANCE_DIR


@pytest.fixture(scope="session")
def hormuz_manifest(conformance_dir: Path) -> dict[str, Any]:
    """The §2 example manifest."""
    return load_json(conformance_dir / "manifests" / "valid" / "hormuz-example.json")


@pytest.fixture(scope="session")
def push_layer_manifest(conformance_dir: Path) -> dict[str, Any]:
    """The push-only manifest with a snapshot layer stream, notes and forecasts."""
    return load_json(conformance_dir / "manifests" / "valid" / "push-layer.json")


def minimal_manifest(**stream_overrides: Any) -> dict[str, Any]:
    """A small valid push manifest with one atomic observation stream (``s``)."""
    stream = {"id": "s", "mode": "atomic", "itemType": "observation", "kind": "k", "geometry": "none"}
    stream.update(stream_overrides)
    return {
        "protocol": "foresignal-edp/1.0",
        "source": {"id": "test-source", "name": "Test", "publisher": {"name": "X"},
                   "originator": {"id": "x", "class": "operator"}},
        "transport": {"push": True},
        "streams": [stream],
    }


@dataclass
class RecordedRequest:
    """One request the fake transport received."""

    method: str
    url: str
    headers: dict[str, str]
    body: bytes
    timeout: float


@dataclass
class FakeTransport:
    """A scripted transport: each entry of ``script`` is a ``TransportResponse``, an exception to
    raise, or a callable producing one from the request. Once the script runs out, every request gets
    ``default``."""

    script: list[Any] = field(default_factory=list)
    default: TransportResponse = field(
        default_factory=lambda: TransportResponse(200, {"content-type": "application/json"},
                                                  b'{"accepted": 0, "rejected": []}'))
    requests: list[RecordedRequest] = field(default_factory=list)

    def request(self, method: str, url: str, headers: Mapping[str, str], body: bytes | None,
                timeout: float) -> TransportResponse:
        recorded = RecordedRequest(method, url, dict(headers), body or b"", timeout)
        self.requests.append(recorded)
        step = self.script.pop(0) if self.script else self.default
        if isinstance(step, BaseException):
            raise step
        if callable(step):
            return step(recorded)
        return step


def ok_response(accepted: int, rejected: list[dict[str, Any]] | None = None) -> TransportResponse:
    """A 200 ``EdpIngestResult``."""
    return TransportResponse(200, {"content-type": "application/json"},
                             json.dumps({"accepted": accepted, "rejected": rejected or []}).encode())


def problem_response(status: int, code: str, retry_after: str | None = None) -> TransportResponse:
    """A ProblemDetail response, optionally with ``Retry-After``."""
    headers = {"content-type": "application/problem+json"}
    if retry_after is not None:
        headers["retry-after"] = retry_after
    body = json.dumps({"status": status, "code": code, "title": "x", "detail": f"{code} detail"}).encode()
    return TransportResponse(status, headers, body)


def network_error(message: str = "connection reset") -> TransportError:
    """A transport failure."""
    return TransportError(message)
