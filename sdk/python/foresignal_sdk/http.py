"""The minimal HTTP layer of the hand-written SDK code: httpx, a base URL, a bearer token and JSON.

It does not use the generated client, so the realtime, paging and contract helpers work with or without it.
"""
from __future__ import annotations

import json as jsonlib
from collections.abc import Mapping
from typing import Any

import httpx

from .errors import api_error_from

#: Query parameters: lists become repeated parameters, ``None`` values are left out.
Query = Mapping[str, Any]


def clean_query(query: Query | None) -> list[tuple[str, str]]:
    """Returns query parameters as pairs (repeated keys for lists; booleans as ``true``/``false``)."""
    pairs: list[tuple[str, str]] = []
    for key, value in (query or {}).items():
        if value is None:
            continue
        values = value if isinstance(value, (list, tuple)) else [value]
        for item in values:
            if isinstance(item, bool):
                pairs.append((key, "true" if item else "false"))
            else:
                pairs.append((key, str(item)))
    return pairs


class HttpTransport:
    """Sends requests to one Foresignal instance."""

    def __init__(self, base_url: str, token: str | None = None, *, headers: Mapping[str, str] | None = None,
                 client: httpx.Client | None = None, timeout: float = 30.0) -> None:
        """Creates a transport.

        :param base_url: the Foresignal base URL, e.g. ``https://foresignal.example.org`` (no ``/v1``)
        :param token: a Foresignal API token, sent as ``Authorization: Bearer …``
        :param headers: extra headers for every request
        :param client: an ``httpx.Client`` to reuse (the transport then does not close it)
        :param timeout: default request timeout in seconds
        """
        self.base_url = base_url.rstrip("/")
        self.token = token
        self.extra_headers = dict(headers or {})
        self._owns_client = client is None
        self.client = client or httpx.Client(timeout=timeout)

    def url(self, path: str) -> str:
        """The absolute URL of ``path`` (starting with ``/``)."""
        return self.base_url + path

    def headers(self, extra: Mapping[str, str] | None = None, auth: bool = True) -> dict[str, str]:
        """The headers of a request: the extra headers, the token (when ``auth``), then ``extra``."""
        out = dict(self.extra_headers)
        if auth and self.token:
            out["Authorization"] = f"Bearer {self.token}"
        out.update(extra or {})
        return out

    def send(self, method: str, path: str, *, query: Query | None = None, json: Any = None,
             content_type: str = "application/json", headers: Mapping[str, str] | None = None,
             auth: bool = True) -> httpx.Response:
        """Sends a request and returns the response, whatever its status."""
        h = self.headers(headers, auth)
        content: bytes | None = None
        if json is not None:
            content = jsonlib.dumps(json).encode("utf-8")
            h["Content-Type"] = content_type
        return self.client.request(method, self.url(path), params=clean_query(query), content=content, headers=h)

    def request(self, method: str, path: str, *, query: Query | None = None, json: Any = None,
                content_type: str = "application/json", headers: Mapping[str, str] | None = None,
                auth: bool = True) -> tuple[Any, httpx.Response]:
        """Sends a request and returns ``(parsed JSON or None, response)``.

        :raises ApiError: for a non-2xx status (``ResyncRequiredError`` for 410 RESYNC_REQUIRED)
        """
        response = self.send(method, path, query=query, json=json, content_type=content_type,
                             headers={"Accept": "application/json", **dict(headers or {})}, auth=auth)
        if response.status_code >= 400 or response.status_code < 200:
            raise api_error_from(response)
        if response.status_code == 204 or not response.content:
            return None, response
        return response.json(), response

    def get_json(self, path: str, query: Query | None = None) -> tuple[Any, httpx.Response]:
        """``GET`` returning JSON."""
        return self.request("GET", path, query=query)

    def post_json(self, path: str, body: Any, query: Query | None = None) -> tuple[Any, httpx.Response]:
        """``POST`` with a JSON body returning JSON."""
        return self.request("POST", path, query=query, json=body)

    def open_stream(self, path: str, *, query: Query | None = None, headers: Mapping[str, str] | None = None,
                    auth: bool = True, read_timeout: float | None = None) -> httpx.Response:
        """Opens a streamed ``GET`` (the caller closes the response).

        ``read_timeout`` bounds the wait for each chunk, which is how the heartbeat watchdog is enforced.
        """
        request = self.client.build_request("GET", self.url(path), params=clean_query(query),
                                            headers=self.headers(headers, auth),
                                            timeout=httpx.Timeout(30.0, read=read_timeout))
        return self.client.send(request, stream=True)

    def close(self) -> None:
        """Closes the httpx client when the transport created it."""
        if self._owns_client:
            self.client.close()
