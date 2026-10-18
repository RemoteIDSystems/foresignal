"""Object references ``{type}:{id}`` (docs/Client-Contract.md section 2).

Parsed and formatted with the same rules as the server's ``ObjectRef``: record ids are
``{sourceId}/{externalId}[@{seq}]`` with the external id percent-encoded (``/`` -> ``%2F``, ``@`` -> ``%40``,
``%`` -> ``%25``, whitespace and control characters as their UTF-8 bytes), sensor ids are ``{kind}:{id}``,
remote ids are ``{instanceId}/{type}:{id}``. Ids are otherwise opaque and references compare as strings. Types
are not checked against a fixed list (the contract grows additively); pass ``known_types`` (from
``GET /v1/contract``) to reject types the server does not know.
"""
from __future__ import annotations

import re
from collections.abc import Sequence
from dataclasses import dataclass

from ..errors import ObjectRefError

#: Longest accepted reference, in characters.
MAX_OBJECT_REF_LENGTH = 1024

#: Sensor kinds accepted in ``sensor:{kind}:{id}``.
SENSOR_KINDS = ("source", "receiver", "station", "satellite", "scene", "raster-product")

_TYPE = re.compile(r"^[a-z][a-z-]*$")
_HEX2 = re.compile(r"^[0-9A-Fa-f]{2}$")


@dataclass(frozen=True)
class RecordRefParts:
    """The parts of a ``record:`` reference."""

    source_id: str
    external_id: str
    """The decoded external id."""
    seq: int | None
    """The pinned version, or None for the current one."""


@dataclass(frozen=True)
class ObjectRef:
    """A parsed reference."""

    ref: str
    """The canonical text ``{type}:{id}``."""
    type: str
    id: str
    """The id exactly as it appears after the first colon."""
    record: RecordRefParts | None = None
    sensor_kind: str | None = None
    sensor_id: str | None = None
    remote_instance_id: str | None = None
    remote_ref: str | None = None

    def __str__(self) -> str:
        """The canonical text."""
        return self.ref


def _is_java_whitespace(cp: int) -> bool:
    """Whitespace as Java's ``Character.isWhitespace`` defines it (the server's rule)."""
    return (0x09 <= cp <= 0x0D or 0x1C <= cp <= 0x20 or cp == 0x1680 or 0x2000 <= cp <= 0x2006
            or 0x2008 <= cp <= 0x200A or cp in (0x2028, 0x2029, 0x205F, 0x3000))


def _is_control(cp: int) -> bool:
    return cp <= 0x1F or 0x7F <= cp <= 0x9F


def encode_external_id(external_id: str) -> str:
    """Percent-encodes ``%``, ``/``, ``@``, whitespace and control characters of an external id."""
    out: list[str] = []
    for ch in external_id:
        cp = ord(ch)
        if ch in "%/@" or _is_java_whitespace(cp) or _is_control(cp):
            out.extend(f"%{b:02X}" for b in ch.encode("utf-8"))
        else:
            out.append(ch)
    return "".join(out)


def decode_external_id(encoded: str) -> str:
    """Decodes a percent-encoded external id (UTF-8).

    :raises ValueError: on a truncated or non-hex escape
    """
    if "%" not in encoded:
        return encoded
    data = bytearray()
    i = 0
    while i < len(encoded):
        c = encoded[i]
        if c == "%":
            hex2 = encoded[i + 1:i + 3]
            if len(hex2) < 2 or not _HEX2.match(hex2):
                raise ValueError("truncated escape" if len(hex2) < 2 else "bad escape")
            data.append(int(hex2, 16))
            i += 3
        else:
            data.extend(c.encode("utf-8"))
            i += 1
    return data.decode("utf-8")


def parse_object_ref(text: str, known_types: Sequence[str] | None = None) -> ObjectRef:
    """Parses a reference.

    :param text: the reference
    :param known_types: optional types in force (``GET /v1/contract``); other types are rejected
    :raises ObjectRefError: when it is malformed
    """
    if not isinstance(text, str) or not text.strip():
        raise ObjectRefError(str(text or ""), "is required")
    if len(text) > MAX_OBJECT_REF_LENGTH:
        raise ObjectRefError(text, f"is longer than {MAX_OBJECT_REF_LENGTH} characters")
    colon = text.find(":")
    if colon <= 0 or colon == len(text) - 1:
        raise ObjectRefError(text, "must be {type}:{id}")
    typ, ident = text[:colon], text[colon + 1:]
    if not _TYPE.match(typ):
        raise ObjectRefError(text, f"has an invalid object type '{typ}'")
    if known_types is not None and typ not in known_types:
        raise ObjectRefError(text, f"has unknown object type '{typ}'")
    if any(_is_java_whitespace(ord(ch)) or _is_control(ord(ch)) for ch in ident):
        raise ObjectRefError(text, "must not contain whitespace or control characters")
    if typ == "record":
        return ObjectRef(text, typ, ident, record=_parse_record_id(text, ident))
    if typ == "sensor":
        c = ident.find(":")
        if c <= 0 or c == len(ident) - 1 or ident[:c] not in SENSOR_KINDS:
            raise ObjectRefError(text, "sensor refs are sensor:{kind}:{id} with kind one of "
                                 + ", ".join(SENSOR_KINDS))
        return ObjectRef(text, typ, ident, sensor_kind=ident[:c], sensor_id=ident[c + 1:])
    if typ == "remote":
        slash = ident.find("/")
        inner = ident[slash + 1:] if slash > 0 else ""
        inner_colon = inner.find(":")
        if slash <= 0 or not inner or inner_colon <= 0 or inner_colon == len(inner) - 1:
            raise ObjectRefError(text, "remote refs are remote:{instanceId}/{type}:{id}")
        inner_type = inner[:inner_colon]
        if not _TYPE.match(inner_type) or inner_type == "remote":
            raise ObjectRefError(text, "a remote ref names an object type other than remote")
        return ObjectRef(text, typ, ident, remote_instance_id=ident[:slash], remote_ref=inner)
    return ObjectRef(text, typ, ident)


def _parse_record_id(text: str, ident: str) -> RecordRefParts:
    slash = ident.find("/")
    if slash <= 0 or slash == len(ident) - 1:
        raise ObjectRefError(text, "record refs are record:{sourceId}/{externalId}[@{seq}]")
    rest = ident[slash + 1:]
    at = rest.find("@")
    if at == 0 or (at > 0 and rest.find("@", at + 1) >= 0):
        raise ObjectRefError(text, "a record ref has at most one '@' (encode '@' in the external id as %40)")
    seq: int | None = None
    if at > 0:
        digits = rest[at + 1:]
        if not digits or len(digits) > 19 or not digits.isascii() or not digits.isdigit():
            raise ObjectRefError(text, "the version after '@' must be a non-negative integer seq")
        seq = int(digits)
    try:
        external_id = decode_external_id(rest if at < 0 else rest[:at])
    except (ValueError, UnicodeDecodeError) as error:
        raise ObjectRefError(text, "has a malformed percent-encoding in the external id") from error
    return RecordRefParts(ident[:slash], external_id, seq)


def is_valid_object_ref(text: str, known_types: Sequence[str] | None = None) -> bool:
    """True when ``text`` parses."""
    try:
        parse_object_ref(text, known_types)
        return True
    except ObjectRefError:
        return False


def format_object_ref(type_: str, ident: str) -> str:
    """Formats ``{type}:{id}`` (validated)."""
    return parse_object_ref(f"{type_}:{ident}").ref


def record_ref(source_id: str, external_id: str, seq: int | None = None) -> str:
    """Formats a record reference from its parts, encoding the external id."""
    suffix = "" if seq is None else f"@{seq}"
    return parse_object_ref(f"record:{source_id}/{encode_external_id(external_id)}{suffix}").ref


def remote_ref(instance_id: str, ref: str) -> str:
    """Formats a remote reference ``remote:{instanceId}/{ref}``."""
    return parse_object_ref(f"remote:{instance_id}/{ref}").ref


def object_ref_path_segment(ref: str) -> str:
    """Encodes a reference as one URL path segment: ``%`` -> ``%25``, ``/`` -> ``%2F``, ``#`` -> ``%23``,
    ``?`` -> ``%3F``; everything else stays as written."""
    return ref.replace("%", "%25").replace("/", "%2F").replace("#", "%23").replace("?", "%3F")
