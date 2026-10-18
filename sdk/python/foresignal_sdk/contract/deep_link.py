"""Deep links (docs/Client-Contract.md section 6).

A context travels as ``{"v":1,"ws":…,"ctx":{…}}``: compact JSON with the context's null members left out,
base64url-encoded without padding, at most 2,048 bytes, carried in a ``#fs=`` URL fragment. Encoding gives the
same payload as the server's ``ContextLinks``; decoding checks the payload version and validates the context.
"""
from __future__ import annotations

import base64
import binascii
import json
import re
from dataclasses import dataclass
from typing import Any

from ..errors import DeepLinkError
from .context import context_problems, strip_null_members
from .envelope import DEEP_LINK_VERSION

#: Largest encoded payload, in bytes.
MAX_DEEP_LINK_BYTES = 2048
#: The URL fragment prefix.
DEEP_LINK_FRAGMENT = "#fs="

_BASE64URL = re.compile(r"^[A-Za-z0-9_-]*$")


@dataclass(frozen=True)
class EncodedDeepLink:
    """An encoded deep link (the shape of the server's ``ContextLink``)."""

    version: int
    workspace_id: str | None
    payload: str
    fragment: str
    bytes: int


@dataclass(frozen=True)
class DecodedDeepLink:
    """A decoded deep link."""

    version: int
    workspace_id: str | None
    context: dict[str, Any]


def _problems_text(problems: list[dict[str, str]]) -> str:
    return "; ".join(f"{e['field']} {e['message']}" for e in problems)


def encode_deep_link(workspace_id: str | None, context: dict[str, Any]) -> EncodedDeepLink:
    """Encodes a context as a deep link.

    :raises DeepLinkError: ``context`` when the context is invalid, ``too-large`` beyond 2,048 bytes
    """
    problems = context_problems(context, "context")
    if problems:
        raise DeepLinkError("context", _problems_text(problems))
    doc = {"v": DEEP_LINK_VERSION, "ws": workspace_id, "ctx": strip_null_members(context or {})}
    raw = json.dumps(doc, separators=(",", ":"), ensure_ascii=False).encode("utf-8")
    payload = base64.urlsafe_b64encode(raw).decode("ascii").rstrip("=")
    if len(payload) > MAX_DEEP_LINK_BYTES:
        raise DeepLinkError("too-large", f"The encoded context is {len(payload)} bytes; "
                                         f"a deep link holds at most {MAX_DEEP_LINK_BYTES}.")
    return EncodedDeepLink(DEEP_LINK_VERSION, workspace_id, payload, DEEP_LINK_FRAGMENT + payload, len(payload))


def decode_deep_link(payload_or_fragment: str) -> DecodedDeepLink:
    """Decodes a payload, with or without the ``#fs=`` prefix (a full URL with that fragment works too).

    :raises DeepLinkError: ``malformed``, ``version`` (unsupported payload version), ``context`` or ``too-large``
    """
    text = payload_or_fragment or ""
    at = text.find(DEEP_LINK_FRAGMENT)
    if at >= 0:
        text = text[at + len(DEEP_LINK_FRAGMENT):]
    if not text.strip():
        raise DeepLinkError("malformed", "The deep link payload is empty.")
    if len(text) > MAX_DEEP_LINK_BYTES:
        raise DeepLinkError("too-large", f"The payload is longer than {MAX_DEEP_LINK_BYTES} bytes.")
    try:
        if not _BASE64URL.match(text) or len(text) % 4 == 1:
            raise ValueError("not base64url")
        raw = base64.urlsafe_b64decode(text + "=" * (-len(text) % 4))
        doc = json.loads(raw.decode("utf-8"))
    except (ValueError, binascii.Error, UnicodeDecodeError) as error:
        raise DeepLinkError("malformed", "The deep link payload is not base64url JSON.") from error
    if not isinstance(doc, dict) or not isinstance(doc.get("v"), int) or isinstance(doc.get("v"), bool):
        raise DeepLinkError("malformed", 'The deep link payload must be {"v":1,"ws":…,"ctx":{…}}.')
    version = doc["v"]
    if version != DEEP_LINK_VERSION:
        raise DeepLinkError("version", f"Unsupported deep link version {version} "
                                       f"(this SDK reads {DEEP_LINK_VERSION}).")
    ctx = doc.get("ctx")
    if ctx is None:
        ctx = {}
    problems = context_problems(ctx, "ctx")
    if problems:
        raise DeepLinkError("context", _problems_text(problems))
    ws = doc.get("ws")
    return DecodedDeepLink(version, ws if isinstance(ws, str) else None, ctx)
