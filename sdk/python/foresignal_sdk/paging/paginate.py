"""Cursor iterators (generators) over Foresignal's paged operations.

Every listing that takes ``cursor`` answers ``{items, nextCursor}`` (two older ones say ``next``);
``GET /v1/changes`` pages by seq (``after``, ``lastSeq``, ``hasMore``) and refuses a cursor below the purge
horizon with ``410 RESYNC_REQUIRED``, raised as :class:`~foresignal_sdk.errors.ResyncRequiredError`.
"""
from __future__ import annotations

from collections.abc import Callable, Generator, Iterator, Mapping
from typing import Any

from ..errors import ResyncRequiredError
from ..http import HttpTransport, Query

__all__ = ["next_cursor_of", "paginate_pages_with", "paginate_with", "paginate_pages", "paginate",
           "iterate_changes", "ResyncRequiredError"]


def next_cursor_of(page: Mapping[str, Any]) -> str | None:
    """The next cursor of a page (``nextCursor``, else ``next``); None on the last page."""
    cursor = page.get("nextCursor")
    if cursor is None:
        cursor = page.get("next")
    return cursor or None


def paginate_pages_with(fetch_page: Callable[[str | None], Mapping[str, Any]],
                        max_pages: int | None = None) -> Iterator[Mapping[str, Any]]:
    """Iterates pages from any page fetcher (for example a generated-client call), following the cursor."""
    cursor: str | None = None
    pages = 0
    while True:
        page = fetch_page(cursor)
        pages += 1
        yield page
        cursor = next_cursor_of(page)
        if cursor is None or (max_pages is not None and pages >= max_pages):
            return


def paginate_with(fetch_page: Callable[[str | None], Mapping[str, Any]],
                  max_pages: int | None = None) -> Iterator[Any]:
    """Iterates the items of every page from any page fetcher."""
    for page in paginate_pages_with(fetch_page, max_pages):
        yield from page.get("items") or []


def paginate_pages(transport: HttpTransport, path: str, query: Query | None = None, *,
                   page_size: int | None = None, max_pages: int | None = None) -> Iterator[Mapping[str, Any]]:
    """Iterates the pages of a cursor-paged ``GET`` operation, e.g. ``paginate_pages(t, "/v1/alerts")``."""
    base: dict[str, Any] = dict(query or {})
    if page_size is not None:
        base["limit"] = page_size

    def fetch(cursor: str | None) -> Mapping[str, Any]:
        data, _ = transport.request("GET", path, query={**base, "cursor": cursor} if cursor else base)
        return data or {}

    return paginate_pages_with(fetch, max_pages)


def paginate(transport: HttpTransport, path: str, query: Query | None = None, *,
             page_size: int | None = None, max_pages: int | None = None) -> Iterator[Any]:
    """Iterates the items of a cursor-paged ``GET`` operation."""
    for page in paginate_pages(transport, path, query, page_size=page_size, max_pages=max_pages):
        yield from page.get("items") or []


def iterate_changes(transport: HttpTransport, *, after: int = 0, filters: Query | None = None,
                    limit: int | None = None,
                    on_page: Callable[[Mapping[str, Any]], None] | None = None) -> Generator[Any, None, int]:
    """Iterates ``GET /v1/changes`` from ``after`` until ``hasMore`` is false; returns the last seq.

    :raises ResyncRequiredError: when ``after`` is below the purge horizon (410 RESYNC_REQUIRED)
    """
    cursor = after
    while True:
        query: dict[str, Any] = {**dict(filters or {}), "after": cursor}
        if limit is not None:
            query["limit"] = limit
        page, _ = transport.request("GET", "/v1/changes", query=query)
        page = page or {}
        if on_page is not None:
            on_page(page)
        yield from page.get("items") or []
        cursor = page.get("lastSeq", cursor)
        if not page.get("hasMore"):
            return cursor
