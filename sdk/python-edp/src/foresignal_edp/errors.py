"""Error and result types shared by the validator, the push client and the pull helper."""

from __future__ import annotations

from dataclasses import dataclass
from typing import TYPE_CHECKING, Any, Mapping

if TYPE_CHECKING:  # pragma: no cover
    from .manifest import ManifestCheck


@dataclass(frozen=True)
class SchemaError:
    """One validation problem: a JSON Pointer into the checked document and a message."""

    path: str
    message: str

    def to_dict(self) -> dict[str, str]:
        """Returns the wire form ``{"path", "message"}``."""
        return {"path": self.path, "message": self.message}

    @staticmethod
    def from_dict(data: Mapping[str, Any]) -> "SchemaError":
        """Reads the wire form."""
        return SchemaError(str(data.get("path", "")), str(data.get("message", "")))


@dataclass(frozen=True)
class ItemRejection:
    """Why one item was refused (OpenAPI ``EdpItemRejection``).

    ``reason`` is one of the stable codes the server uses: ``SCHEMA_INVALID``, ``UNKNOWN_STREAM``,
    ``ITEM_TYPE_MISMATCH``, ``PAYLOAD_INVALID``, ``GEOMETRY_INVALID``, ``PERIOD_INVALID``,
    ``GRANULARITY_MIXED``, ``FORECAST_INVALID``, ``MISSING_OBSERVED_AT``, ``OBSERVED_AT_IN_FUTURE``,
    ``LIMIT_EXCEEDED`` (or a server-only code in a push response).
    """

    index: int
    stream: str | None
    id: str | None
    reason: str
    schema_errors: tuple[SchemaError, ...] = ()

    @staticmethod
    def of(index: int, stream: str | None, item_id: str | None, reason: str, message: str) -> "ItemRejection":
        """Creates a rejection with one message at the item root."""
        return ItemRejection(index, stream, item_id, reason, (SchemaError("", message),))

    def to_dict(self) -> dict[str, Any]:
        """Returns the wire form (``schemaErrors`` in camelCase)."""
        return {
            "index": self.index,
            "stream": self.stream,
            "id": self.id,
            "reason": self.reason,
            "schemaErrors": [e.to_dict() for e in self.schema_errors],
        }

    @staticmethod
    def from_dict(data: Mapping[str, Any], offset: int = 0) -> "ItemRejection":
        """Reads the wire form, adding ``offset`` to the index (to map a batch index to a push index)."""
        errors = tuple(SchemaError.from_dict(e) for e in data.get("schemaErrors") or [] if isinstance(e, Mapping))
        return ItemRejection(
            int(data.get("index", 0)) + offset,
            data.get("stream"),
            data.get("id"),
            str(data.get("reason", "")),
            errors,
        )


class EdpError(Exception):
    """Base class of this library's exceptions."""


class ManifestInvalidError(EdpError):
    """A manifest failed validation; ``check`` holds every problem."""

    def __init__(self, check: "ManifestCheck") -> None:
        self.check = check
        summary = "; ".join(f"{e.path or '/'}: {e.message}" for e in check.errors[:5])
        more = f" (+{len(check.errors) - 5} more)" if len(check.errors) > 5 else ""
        super().__init__(f"The EDP manifest is invalid: {summary}{more}")


class PayloadSchemaError(EdpError):
    """A stream's ``payloadSchema`` breaks the EDP §7 rules or cannot be compiled."""

    def __init__(self, errors: list[SchemaError]) -> None:
        self.errors = errors
        super().__init__("; ".join(f"{e.path or '/'}: {e.message}" for e in errors))


class ItemTooLargeError(EdpError):
    """One item alone does not fit in a batch of the configured byte limit."""

    def __init__(self, index: int, size: int, limit: int) -> None:
        self.index = index
        self.size = size
        self.limit = limit
        super().__init__(f"Item {index} needs {size} bytes in a batch; the limit is {limit}")


class CursorError(EdpError):
    """A pull cursor could not be decoded (unknown, expired or tampered with)."""


class PushError(EdpError):
    """A push failed.

    ``status`` is the HTTP status (``None`` for a network failure), ``code`` the ProblemDetail
    ``code`` when the server sent one, ``retryable`` whether retrying later may succeed (429, 503 or
    a network error after the retries were spent), ``result`` the merged result of the batches that
    were accepted before the failure, and ``remaining`` the prepared batches that were not sent
    (the failed one first); pass them to ``PushClient.send_all`` to re-send them unchanged.
    """

    def __init__(
        self,
        message: str,
        *,
        status: int | None = None,
        code: str | None = None,
        problem: Mapping[str, Any] | None = None,
        body: bytes = b"",
        retryable: bool = False,
    ) -> None:
        super().__init__(message)
        self.status = status
        self.code = code
        self.problem = dict(problem) if problem else None
        self.body = body
        self.retryable = retryable
        self.result: Any = None
        self.remaining: list[Any] = []
