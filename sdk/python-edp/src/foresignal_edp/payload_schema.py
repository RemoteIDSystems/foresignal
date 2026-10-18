"""Vetting and compiling a stream's ``payloadSchema`` (EDP §7).

A payload schema is at most 64 KB serialized, uses only local ``$ref`` s (starting with ``#``),
never ``$dynamicRef`` or ``$recursiveRef``, declares no ``$schema`` other than 2020-12, and every
``pattern`` and ``patternProperties`` key is at most 256 characters and acceptable to a linear-time
engine. The server compiles patterns with RE2; this library refuses the constructs RE2 rejects
(look-around, backreferences, atomic groups and possessive quantifiers) and then compiles with
Python's ``re``. The server remains the final judge.
"""

from __future__ import annotations

import json
import re
from typing import Any

from jsonschema import Draft202012Validator
from jsonschema.exceptions import SchemaError as JsonSchemaSchemaError

from .errors import PayloadSchemaError, SchemaError
from .protocol import MAX_PATTERN_LENGTH, MAX_SCHEMA_BYTES
from .schema_store import to_schema_errors

#: The only ``$schema`` a payload schema may declare.
DIALECT = "https://json-schema.org/draft/2020-12/schema"

_NOT_LINEAR = re.compile(r"\(\?<?[=!]|\(\?>|\(\?P=|\\[1-9]|\\k<|[*+?}]\+")


def _escape(key: str) -> str:
    return key.replace("~", "~0").replace("/", "~1")


def check_pattern(regex: str, path: str, problems: list[SchemaError]) -> None:
    """Adds a problem when ``regex`` is too long, uses a construct a linear-time engine refuses, or
    does not compile."""
    if len(regex) > MAX_PATTERN_LENGTH:
        problems.append(SchemaError(path, f"pattern is {len(regex)} characters; the maximum is {MAX_PATTERN_LENGTH}"))
        return
    if _NOT_LINEAR.search(regex):
        problems.append(SchemaError(path, "pattern is not supported by the linear-time engine "
                                          "(look-around, backreferences, atomic groups and possessive "
                                          "quantifiers are refused)"))
        return
    try:
        re.compile(regex)
    except re.error as e:
        problems.append(SchemaError(path, f"pattern is not supported by the linear-time engine: {e}"))


def _walk(node: Any, path: str, problems: list[SchemaError]) -> None:
    if isinstance(node, list):
        for i, child in enumerate(node):
            _walk(child, f"{path}/{i}", problems)
        return
    if not isinstance(node, dict):
        return
    for key, value in node.items():
        child = f"{path}/{_escape(key)}"
        if key == "$ref":
            if not isinstance(value, str) or not value.startswith("#"):
                problems.append(SchemaError(child, "only local references (starting with #) are allowed"))
        elif key in ("$dynamicRef", "$recursiveRef"):
            problems.append(SchemaError(child, f"{key} is not allowed"))
        elif key == "pattern":
            if isinstance(value, str):
                check_pattern(value, child, problems)
        elif key == "patternProperties" and isinstance(value, dict):
            for pattern in value:
                check_pattern(pattern, f"{child}/{_escape(pattern)}", problems)
        _walk(value, child, problems)


def vet_payload_schema(schema: Any) -> list[SchemaError]:
    """Returns the §7 problems of ``schema`` without compiling it (empty when acceptable)."""
    problems: list[SchemaError] = []
    if not isinstance(schema, dict):
        return [SchemaError("", "must be a JSON object")]
    size = len(json.dumps(schema, separators=(",", ":"), ensure_ascii=False).encode("utf-8"))
    if size > MAX_SCHEMA_BYTES:
        return [SchemaError("", f"is {size} bytes; the maximum is {MAX_SCHEMA_BYTES}")]
    dialect = schema.get("$schema")
    if dialect is not None and dialect != DIALECT:
        problems.append(SchemaError("/$schema", f"must be absent or {DIALECT}"))
    _walk(schema, "", problems)
    return problems


class PayloadValidator:
    """A compiled payload schema; validates an item's ``properties``."""

    def __init__(self, schema: dict[str, Any]) -> None:
        """Wraps an already vetted schema. Use :func:`compile_payload_schema` instead."""
        self.schema = schema
        self._validator = Draft202012Validator(schema)

    def validate(self, properties: Any) -> list[SchemaError]:
        """Returns the problems of ``properties`` (``None`` is checked as an empty object)."""
        return to_schema_errors(self._validator.iter_errors({} if properties is None else properties))


def compile_payload_schema(schema: Any) -> PayloadValidator:
    """Vets and compiles ``schema``.

    :raises PayloadSchemaError: with every problem found, paths relative to the schema root
    """
    problems = vet_payload_schema(schema)
    if problems:
        raise PayloadSchemaError(problems)
    try:
        Draft202012Validator.check_schema(schema)
    except JsonSchemaSchemaError as e:
        raise PayloadSchemaError(to_schema_errors([e])) from e  # type: ignore[list-item]
    return PayloadValidator(schema)
