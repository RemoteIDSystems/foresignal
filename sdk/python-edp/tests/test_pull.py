"""Pull-server helper: cursor paging, cursor stores, snapshot paging, search, auth, status, WSGI."""

from __future__ import annotations

import base64
import io
import json
from datetime import datetime, timezone
from typing import Any

import pytest

from foresignal_edp import (
    CursorError,
    MemoryCursorStore,
    OpaqueCursorStore,
    Page,
    PullHandler,
    SearchQuery,
    SequenceSource,
    SnapshotSource,
    StatusBoard,
    basic_auth,
    bearer_auth,
    feature,
    header_auth,
    observation,
    point,
    validate_batch,
    wsgi_app,
)

from .conftest import minimal_manifest

NOW = datetime(2026, 10, 11, 18, 0, tzinfo=timezone.utc)


def rows(n: int) -> list[dict[str, Any]]:
    return [observation("s", series_key="k", t=f"2026-01-{(i % 28) + 1:02d}", value=i, observed_at=NOW,
                        item_id=f"r-{i}") for i in range(n)]


def handler_over(data: list[dict[str, Any]], **kwargs: Any) -> PullHandler:
    return PullHandler(SequenceSource(lambda: data), source_id="test-source", clock=lambda: NOW, **kwargs)


def get(handler: PullHandler, **query: Any) -> dict[str, Any]:
    response = handler.handle_items({k: str(v) for k, v in query.items()})
    assert response.status == 200, response.body
    assert response.headers["Content-Type"] == "application/json"
    return response.json()


def test_pages_follow_next_cursor_until_null() -> None:
    data = rows(7)
    handler = handler_over(data)
    first = get(handler, limit=3)
    assert first["protocol"] == "foresignal-edp/1.0" and first["sourceId"] == "test-source"
    assert first["sentAt"] == "2026-10-11T18:00:00Z"
    assert [i["id"] for i in first["items"]] == ["r-0", "r-1", "r-2"]
    second = get(handler, limit=3, cursor=first["nextCursor"])
    assert [i["id"] for i in second["items"]] == ["r-3", "r-4", "r-5"]
    third = get(handler, limit=3, cursor=second["nextCursor"])
    assert [i["id"] for i in third["items"]] == ["r-6"] and third["nextCursor"] is None
    # Foresignal re-sends the last cursor next run: the page comes back with anything appended since.
    data.extend(rows(9)[7:])
    again = get(handler, limit=3, cursor=second["nextCursor"])
    assert [i["id"] for i in again["items"]] == ["r-6", "r-7", "r-8"]
    assert again["nextCursor"] is not None


def test_served_pages_are_valid_batches() -> None:
    body = get(handler_over(rows(3)))
    assert validate_batch(body, minimal_manifest(), received_at=NOW).ok


def test_limit_defaults_clamps_and_validates() -> None:
    handler = handler_over(rows(700), max_limit=600)
    assert len(get(handler)["items"]) == 500
    assert len(get(handler, limit=10_000)["items"]) == 600
    assert len(get(handler, limit=0)["items"]) == 1
    bad = handler.handle_items({"limit": "many"})
    assert bad.status == 400 and bad.json()["code"] == "LIMIT_INVALID"
    assert bad.headers["Content-Type"] == "application/problem+json"


def test_query_values_may_be_lists_and_empty_cursor_is_the_start() -> None:
    handler = handler_over(rows(2))
    response = handler.handle_items({"cursor": [""], "limit": ["1"]})
    assert response.json()["items"][0]["id"] == "r-0"


def test_invalid_cursors_answer_400() -> None:
    handler = handler_over(rows(2))
    assert handler.handle_items({"cursor": "%%%"}).json()["code"] == "CURSOR_INVALID"
    negative = OpaqueCursorStore().encode(-1)
    assert handler.handle_items({"cursor": negative}).json()["code"] == "CURSOR_INVALID"


def test_signed_cursors_reject_tampering() -> None:
    store = OpaqueCursorStore("secret")
    cursor = store.encode({"offset": 5})
    assert store.decode(cursor) == {"offset": 5}
    payload, _, signature = cursor.partition(".")
    forged = base64.urlsafe_b64encode(b'{"offset":0}').decode().rstrip("=") + "." + signature
    with pytest.raises(CursorError):
        store.decode(forged)
    with pytest.raises(CursorError):
        store.decode(payload)
    assert OpaqueCursorStore().decode(OpaqueCursorStore().encode([1, "a"])) == [1, "a"]


def test_memory_cursor_store_is_stable_and_bounded() -> None:
    store = MemoryCursorStore(max_entries=2)
    a = store.encode(10)
    assert store.encode(10) == a and store.decode(a) == 10
    b = store.encode(20)
    store.decode(a)  # a is now the most recently used
    store.encode(30)  # evicts b
    assert store.decode(a) == 10
    with pytest.raises(CursorError):
        store.decode(b)
    with pytest.raises(CursorError):
        store.decode("unknown")


def test_pluggable_cursor_store_is_used() -> None:
    class Recording:
        def __init__(self) -> None:
            self.encoded: list[Any] = []

        def encode(self, position: Any) -> str:
            self.encoded.append(position)
            return f"pos-{position}"

        def decode(self, cursor: str) -> Any:
            return int(cursor.removeprefix("pos-"))

    store = Recording()
    handler = handler_over(rows(5), cursor_store=store)
    first = get(handler, limit=2)
    assert first["nextCursor"] == "pos-2" and store.encoded == [2]
    assert [i["id"] for i in get(handler, limit=2, cursor="pos-2")["items"]] == ["r-2", "r-3"]


def test_oversized_pages_are_halved_until_they_fit() -> None:
    data = [dict(r, properties={"blob": "x" * 1000}) for r in rows(40)]
    handler = handler_over(data, max_bytes=8_000)
    first = handler.handle_items({"limit": "40"})
    assert first.status == 200 and len(first.body) <= 8_000
    page = first.json()
    assert 0 < len(page["items"]) < 40
    following = get(handler, limit=40, cursor=page["nextCursor"])
    assert following["items"][0]["id"] == f"r-{len(page['items'])}"


def test_a_single_item_too_large_is_a_500() -> None:
    handler = handler_over([dict(rows(1)[0], properties={"blob": "x" * 5000})], max_bytes=2_000)
    response = handler.handle_items({})
    assert response.status == 500 and response.json()["code"] == "PAGE_TOO_LARGE"


def test_source_failures_are_500_and_requeue_status() -> None:
    class Broken:
        def fetch(self, position: Any, limit: int) -> Page:
            raise RuntimeError("database down")

    board = StatusBoard()
    board.report("degraded", "slow upstream")
    handler = PullHandler(Broken(), status=board)
    response = handler.handle_items({})
    assert response.status == 500 and response.json()["code"] == "PROVIDER_ERROR"
    assert "database down" not in response.body.decode()
    assert board.drain()[0]["health"] == "degraded"


def test_status_items_ride_on_the_next_page_once() -> None:
    board = StatusBoard()
    handler = handler_over(rows(2), status=board)
    board.report("layout-changed", "columns moved", stream="s")
    first = get(handler)
    assert first["items"][0] == {"stream": "s", "type": "status", "health": "layout-changed",
                                 "message": "columns moved"}
    assert len(first["items"]) == 3
    assert all(i["type"] != "status" for i in get(handler)["items"])


def test_snapshot_source_pages_one_snapshot_and_restarts_after_the_final_page() -> None:
    clock = [1000.0]
    ids = iter(["snap-1", "snap-2", "snap-3"])
    entities = [feature("cameras", f"cam-{i}", geometry=point(0, 0), observed_at=NOW) for i in (3, 1, 2, 5, 4)]
    source = SnapshotSource("cameras", lambda: entities, scope="north", clock=lambda: clock[0],
                            id_factory=lambda: next(ids))
    handler = PullHandler(source)
    p1 = get(handler, limit=2)
    assert [i["id"] for i in p1["items"]] == ["cam-1", "cam-2"]
    assert p1["snapshot"] == {"id": "snap-1", "stream": "cameras", "scope": "north", "page": 1, "final": False}
    entities.append(feature("cameras", "cam-0", geometry=point(0, 0), observed_at=NOW))  # sorts before: no shift
    p2 = get(handler, limit=2, cursor=p1["nextCursor"])
    assert [i["id"] for i in p2["items"]] == ["cam-3", "cam-4"] and p2["snapshot"]["page"] == 2
    p3 = get(handler, limit=2, cursor=p2["nextCursor"])
    assert [i["id"] for i in p3["items"]] == ["cam-5"]
    assert p3["snapshot"] == {"id": "snap-1", "stream": "cameras", "scope": "north", "page": 3, "final": True}
    assert p3["nextCursor"] is None
    # The next run re-sends the last cursor (page 3's): a finished snapshot starts a new one.
    restart = get(handler, limit=10, cursor=p2["nextCursor"])
    assert restart["snapshot"]["id"] == "snap-2" and restart["snapshot"]["page"] == 1
    assert restart["snapshot"]["final"] is True and len(restart["items"]) == 6


def test_snapshot_source_restarts_stale_snapshots_and_checks_streams() -> None:
    clock = [0.0]
    ids = iter(["a", "b"])
    entities = [feature("cameras", f"c-{i}", observed_at=NOW) for i in range(4)]
    source = SnapshotSource("cameras", lambda: entities, clock=lambda: clock[0], id_factory=lambda: next(ids),
                            window_seconds=60)
    first = source.fetch(None, 2)
    clock[0] = 61
    stale = source.fetch(first.next_position, 2)
    assert stale.snapshot is not None and stale.snapshot["id"] == "b" and stale.snapshot["page"] == 1
    wrong = SnapshotSource("cameras", lambda: [feature("other", "x")])
    with pytest.raises(ValueError):
        wrong.fetch(None, 10)


def test_search() -> None:
    seen: list[SearchQuery] = []

    def search(query: SearchQuery) -> list[dict[str, Any]]:
        seen.append(query)
        return rows(5)

    handler = handler_over([], search=search)
    response = handler.handle_search({"q": " flood ", "since": "2026-10-01T00:00:00Z", "until": "2026-10-11T00:00:00Z",
                                      "bbox": "5,45,15,55", "lang": "de, en", "limit": "3"})
    assert response.status == 200
    body = response.json()
    assert len(body["items"]) == 3 and body["nextCursor"] is None
    query = seen[0]
    assert query.q == "flood" and query.bbox == (5.0, 45.0, 15.0, 55.0) and query.lang == ["de", "en"]
    assert query.since == datetime(2026, 10, 1, tzinfo=timezone.utc) and query.limit == 3
    assert handler.handle_search({"q": "x"}).status == 200 and seen[-1].limit == 100


@pytest.mark.parametrize("query, code", [
    ({}, "QUERY_REQUIRED"),
    ({"q": "x", "since": "yesterday"}, "TIME_INVALID"),
    ({"q": "x", "bbox": "1,2,3"}, "BBOX_INVALID"),
    ({"q": "x", "bbox": "10,0,5,1"}, "BBOX_INVALID"),
    ({"q": "x", "bbox": "a,b,c,d"}, "BBOX_INVALID"),
    ({"q": "x", "limit": "x"}, "LIMIT_INVALID"),
])
def test_search_validation(query: dict[str, str], code: str) -> None:
    handler = handler_over([], search=lambda q: [])
    response = handler.handle_search(query)
    assert response.status == 400 and response.json()["code"] == code


def test_search_absent_or_failing() -> None:
    assert handler_over([]).handle_search({"q": "x"}).status == 404

    def broken(query: SearchQuery) -> list[dict[str, Any]]:
        raise RuntimeError("boom")

    assert handler_over([], search=broken).handle_search({"q": "x"}).json()["code"] == "PROVIDER_ERROR"
    page_result = handler_over([], search=lambda q: Page(rows(2)))
    assert len(page_result.handle_search({"q": "x"}).json()["items"]) == 2


@pytest.mark.parametrize("authorize, good, bad", [
    (bearer_auth("t0k"), {"Authorization": "Bearer t0k"}, {"Authorization": "Bearer nope"}),
    (header_auth("X-Api-Key", "k1"), {"x-api-key": "k1"}, {"X-Api-Key": "k2"}),
    (basic_auth("u", "p"), {"Authorization": "Basic " + base64.b64encode(b"u:p").decode()},
     {"Authorization": "Basic !!!"}),
])
def test_auth(authorize: Any, good: dict[str, str], bad: dict[str, str]) -> None:
    handler = handler_over(rows(1), authorize=authorize, search=lambda q: [])
    assert handler.handle_items({}, good).status == 200
    assert handler.handle_items({}, bad).status == 401
    assert handler.handle_items({}, {}).status == 401
    assert handler.handle_search({"q": "x"}, bad).status == 401


def test_routing_manifest_head_and_methods() -> None:
    manifest = minimal_manifest()
    handler = handler_over(rows(3), manifest=manifest)
    assert handler.handle("GET", "/.well-known/foresignal-edp.json").json() == manifest
    page = handler.handle("GET", "/edp/items?limit=2")
    assert len(page.json()["items"]) == 2
    head = handler.handle("HEAD", "/edp/items")
    assert head.status == 200 and head.body == b""
    assert handler.handle("POST", "/edp/items").status == 405
    assert handler.handle("POST", "/edp/items").headers["Allow"] == "GET, HEAD"
    assert handler.handle("GET", "/elsewhere").status == 404
    assert handler_over([]).handle("GET", "/.well-known/foresignal-edp.json").status == 404


def test_wsgi_adapter() -> None:
    handler = handler_over(rows(3), authorize=bearer_auth("t"))
    app = wsgi_app(handler)
    captured: dict[str, Any] = {}

    def start_response(status: str, headers: list[tuple[str, str]]) -> None:
        captured["status"], captured["headers"] = status, dict(headers)

    environ = {"REQUEST_METHOD": "GET", "PATH_INFO": "/edp/items", "QUERY_STRING": "limit=2",
               "HTTP_AUTHORIZATION": "Bearer t", "wsgi.input": io.BytesIO()}
    body = b"".join(app(environ, start_response))
    assert captured["status"] == "200 OK"
    assert captured["headers"]["Content-Length"] == str(len(body))
    assert len(json.loads(body)["items"]) == 2
    denied = b"".join(app(dict(environ, HTTP_AUTHORIZATION="Bearer x"), start_response))
    assert captured["status"] == "401 Unauthorized" and b"UNAUTHORIZED" in denied


def test_constructor_limits() -> None:
    with pytest.raises(ValueError):
        PullHandler(SequenceSource(list), max_limit=10_001)
    with pytest.raises(ValueError):
        PullHandler(SequenceSource(list), max_bytes=0)
