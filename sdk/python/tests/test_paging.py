"""Cursor generators across pages, the changes feed, and the typed 410 RESYNC_REQUIRED error."""
from __future__ import annotations

import pytest

from foresignal_sdk.errors import ApiError, ResyncRequiredError
from foresignal_sdk.http import HttpTransport
from foresignal_sdk.paging import iterate_changes, next_cursor_of, paginate, paginate_pages, paginate_with


def test_paginate_follows_next_cursor_across_three_pages(server):
    def handler(req, reply):
        cursor = req.q("cursor")
        if cursor is None:
            return reply.json(200, {"items": [{"id": 1}, {"id": 2}], "nextCursor": "c2"})
        if cursor == "c2":
            return reply.json(200, {"items": [{"id": 3}], "nextCursor": "c3"})
        return reply.json(200, {"items": [{"id": 4}], "nextCursor": None})

    server.handler = handler
    transport = HttpTransport(server.url, "fs_t")
    items = list(paginate(transport, "/v1/alerts", {"status": "firing"}, page_size=2))
    assert [i["id"] for i in items] == [1, 2, 3, 4]
    requests = server.to("/v1/alerts")
    assert [r.q("cursor") for r in requests] == [None, "c2", "c3"]
    assert all(r.q("status") == "firing" and r.q("limit") == "2" for r in requests)
    assert requests[0].header("Authorization") == "Bearer fs_t"


def test_paginate_pages_stops_at_max_pages(server):
    server.handler = lambda req, reply: reply.json(200, {"items": [req.q("cursor") or "first"],
                                                         "nextCursor": "again"})
    pages = list(paginate_pages(HttpTransport(server.url), "/v1/places", max_pages=3))
    assert [p["items"][0] for p in pages] == ["first", "again", "again"]


def test_paginate_with_any_fetcher_and_next_spelling():
    pages = {None: {"items": [1], "next": "b"}, "b": {"items": [2], "next": "c"}, "c": {"items": [3], "next": None}}
    seen = []

    def fetch(cursor):
        seen.append(cursor)
        return pages[cursor]

    assert list(paginate_with(fetch)) == [1, 2, 3]
    assert seen == [None, "b", "c"]
    assert next_cursor_of({"nextCursor": "", "next": None}) is None
    assert next_cursor_of({"next": "x"}) == "x"


def test_paginate_surfaces_api_errors(server):
    server.handler = lambda req, reply: reply.json(400, {"status": 400, "code": "INVALID_CURSOR",
                                                         "detail": "bad cursor"})
    with pytest.raises(ApiError) as raised:
        list(paginate(HttpTransport(server.url), "/v1/records"))
    assert raised.value.code == "INVALID_CURSOR"
    assert str(raised.value) == "bad cursor"


def test_iterate_changes_pages_by_seq_and_returns_last_seq(server):
    def handler(req, reply):
        after = int(req.q("after"))
        if after == 0:
            return reply.json(200, {"items": [{"seq": 1}, {"seq": 2}], "lastSeq": 2, "hasMore": True,
                                    "watermarkSeq": 5})
        if after == 2:
            return reply.json(200, {"items": [{"seq": 4}], "lastSeq": 4, "hasMore": True, "watermarkSeq": 5})
        return reply.json(200, {"items": [], "lastSeq": 5, "hasMore": False, "watermarkSeq": 5})

    server.handler = handler
    last_seqs = []
    generator = iterate_changes(HttpTransport(server.url), filters={"source": ["adsb", "ais"]}, limit=2,
                                on_page=lambda p: last_seqs.append(p["lastSeq"]))
    seqs = []
    with pytest.raises(StopIteration) as stop:
        while True:
            seqs.append(next(generator)["seq"])
    assert seqs == [1, 2, 4]
    assert stop.value.value == 5
    assert last_seqs == [2, 4, 5]
    requests = server.to("/v1/changes")
    assert [r.q("after") for r in requests] == ["0", "2", "4"]
    assert requests[0].query["source"] == ["adsb", "ais"]
    assert requests[0].q("limit") == "2"


def test_iterate_changes_raises_resync_required_on_410(server):
    server.handler = lambda req, reply: reply.json(410, {
        "type": "https://foresignal.ai/problems/resync-required", "title": "Resync required", "status": 410,
        "code": "RESYNC_REQUIRED", "purgeHorizon": 1200, "watermarkSeq": 5000})
    with pytest.raises(ResyncRequiredError) as raised:
        list(iterate_changes(HttpTransport(server.url), after=10))
    error = raised.value
    assert isinstance(error, ApiError)
    assert error.status == 410
    assert error.code == "RESYNC_REQUIRED"
    assert error.purge_horizon == 1200
    assert error.watermark_seq == 5000


def test_other_410_stays_a_plain_api_error(server):
    server.handler = lambda req, reply: reply.json(410, {"status": 410, "code": "GONE"})
    with pytest.raises(ApiError) as raised:
        list(iterate_changes(HttpTransport(server.url)))
    assert not isinstance(raised.value, ResyncRequiredError)
