"""The exported EDP 1.0 JSON Schemas shipped with the package, and validation against them.

The files under ``foresignal_edp/schemas/`` are byte-for-byte copies of
``docs/protocol/edp-1.0/*.schema.json`` (generated from ``docs/OpenAPI.json``); refresh them with
``scripts/sync_schemas.py``. ``format`` keywords are annotations in 2020-12, as on the server;
time strings are checked by the item validator itself.
"""

from __future__ import annotations

import json
from functools import lru_cache
from importlib import resources
from typing import Any, Iterable

from jsonschema import Draft202012Validator
from jsonschema.exceptions import ValidationError

from .errors import SchemaError
from .protocol import SCHEMA_NAMES

_MAX_MESSAGE = 500


@lru_cache(maxsize=None)
def load_schema(name: str) -> dict[str, Any]:
    """Returns the exported schema ``name`` (``manifest``, ``batch``, ``item``, ``feature``, ``event``,
    ``observation``, ``document``, ``forecast``, ``delete`` or ``status``)."""
    if name not in SCHEMA_NAMES:
        raise ValueError(f"Unknown EDP schema '{name}'")
    text = resources.files("foresignal_edp").joinpath("schemas").joinpath(f"{name}.schema.json").read_text("utf-8")
    schema: dict[str, Any] = json.loads(text)
    return schema


@lru_cache(maxsize=None)
def schema_validator(name: str) -> Draft202012Validator:
    """Returns a cached 2020-12 validator for the exported schema ``name``."""
    return Draft202012Validator(load_schema(name))


def pointer(parts: Iterable[Any]) -> str:
    """Formats a path as a JSON Pointer (RFC 6901)."""
    out = ""
    for part in parts:
        out += "/" + str(part).replace("~", "~0").replace("/", "~1")
    return out


def to_schema_errors(errors: Iterable[ValidationError], prefix: str = "") -> list[SchemaError]:
    """Converts ``jsonschema`` errors into :class:`SchemaError` values, sorted by path."""
    out = []
    for error in errors:
        message = error.message
        if len(message) > _MAX_MESSAGE:
            message = message[: _MAX_MESSAGE - 1] + "…"
        out.append(SchemaError(prefix + pointer(error.absolute_path), message))
    out.sort(key=lambda e: (e.path, e.message))
    return out


def shape_errors(name: str, document: Any) -> list[SchemaError]:
    """Validates ``document`` against the exported schema ``name``; returns every problem (empty when valid)."""
    return to_schema_errors(schema_validator(name).iter_errors(document))
