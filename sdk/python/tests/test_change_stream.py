"""ChangeStream against an in-process SSE server: resume with the last id, reset with a snapshot, heartbeat
watchdog, observedAt guard, stream tickets, retry hints and the multiplexed events endpoint."""
from __future__ import annotations

import time

import pytest

from conftest import collect
from foresignal_sdk.errors import ApiError
from foresignal_sdk.realtime import (
    Backoff,
    ChangeStream,
    ObservedAtGuard,
    SseParser,
    composite_cursor_value,
    with_composite_cursor_value,
)


def change(seq, external_id="ae1234", observed_at="2026-10-18T00:00:00Z"):
    return {"seq": seq, "source": "adsb", "externalId": external_id, "kind": "aircraft", "version": 1,
            "observedAt": observed_at}


def test_resumes_after_disconnect_with_last_change_id(server, recording_sleep):
    count = {"n": 0}

    def handler(req, reply):
        count["n"] += 1
        if count["n"] == 1:
            reply.sse([{"id": "5", "event": "change", "data": change(5)},
                       {"id": "6", "event": "change", "data": change(6)}])
        else:
            reply.sse([{"id": "7", "event": "change", "data": change(7)}], keep_open=True)

    server.handler = handler
    _, sleep = recording_sleep
    stream = ChangeStream(server.url, "fs_test", after=4, sleep=sleep)
    signals = collect(stream, lambda s, _: s.type == "change" and s.data["seq"] == 7)

    streams = server.to("/v1/changes/stream")
    assert len(streams) == 2
    assert streams[0].header("Last-Event-ID") == "4"
    assert streams[0].header("Authorization") == "Bearer fs_test"
    assert streams[1].header("Last-Event-ID") == "6"
    assert [s.id for s in signals if s.type == "change"] == ["5", "6", "7"]
    assert any(s.type == "reconnecting" for s in signals)
    assert stream.last_seq == 7
    assert stream.last_event_id == "7"


def test_reset_resync_required_pages_snapshot_and_reopens_at_watermark(server, recording_sleep):
    count = {"n": 0}

    def handler(req, reply):
        if req.path == "/v1/records":
            if req.q("cursor") is None:
                return reply.json(200, {"items": [{"source": "adsb", "externalId": "a",
                                                   "observedAt": "2026-10-18T00:00:10Z"}],
                                        "nextCursor": "p2", "watermarkSeq": 500})
            return reply.json(200, {"items": [{"source": "adsb", "externalId": "b",
                                               "observedAt": "2026-10-18T00:00:11Z"}],
                                    "nextCursor": None, "watermarkSeq": 500})
        count["n"] += 1
        if count["n"] == 1:
            reply.sse([{"id": "10", "event": "change", "data": change(10)},
                       {"event": "reset", "data": {"reason": "resync_required", "resumeAfter": None,
                                                   "watermarkSeq": None}}])
        else:
            reply.sse([{"id": "501", "event": "change", "data": change(501)}], keep_open=True)

    server.handler = handler
    stream = ChangeStream(server.url, after=9, sleep=recording_sleep[1],
                          filters={"source": ["adsb"], "kind": "aircraft", "live": True, "touches": True})
    signals = collect(stream, lambda s, _: s.type == "change" and s.data["seq"] == 501)

    assert [s.type for s in signals] == ["open", "change", "reset", "snapshot", "snapshot", "reconnecting", "open",
                                         "change"]
    snapshots = [s for s in signals if s.type == "snapshot"]
    assert [s.done for s in snapshots] == [False, True]
    assert all(s.cause == "reset" and s.watermark_seq == 500 for s in snapshots)
    assert [i["externalId"] for s in snapshots for i in s.items] == ["a", "b"]

    records = server.to("/v1/records")
    assert len(records) == 2
    assert records[0].q("order") == "id"
    assert records[0].query["source"] == ["adsb"]
    assert records[0].q("kind") == "aircraft"
    assert "live" not in records[0].query and "touches" not in records[0].query
    assert records[1].q("cursor") == "p2"
    assert server.to("/v1/changes/stream")[1].header("Last-Event-ID") == "500"
    assert stream.guard.get(ObservedAtGuard.key("adsb", "b")) is not None


def test_reset_buffer_overflow_reopens_at_resume_after_without_snapshot(server, recording_sleep):
    count = {"n": 0}

    def handler(req, reply):
        count["n"] += 1
        if count["n"] == 1:
            reply.sse([{"id": "40", "event": "change", "data": change(40)},
                       {"event": "reset", "data": {"reason": "buffer_overflow", "resumeAfter": 42,
                                                   "watermarkSeq": 90}}])
        else:
            reply.sse([{"id": "43", "event": "change", "data": change(43)}], keep_open=True)

    server.handler = handler
    collect(ChangeStream(server.url, sleep=recording_sleep[1]),
            lambda s, _: s.type == "change" and s.data["seq"] == 43)
    assert server.to("/v1/records") == []
    assert server.to("/v1/changes/stream")[1].header("Last-Event-ID") == "42"


def test_resync_repages_live_state_and_keeps_stream_open(server, recording_sleep):
    def handler(req, reply):
        if req.path == "/v1/records":
            return reply.json(200, {"items": [{"source": "adsb", "externalId": "a"}], "nextCursor": None,
                                    "watermarkSeq": 77})
        reply.sse([{"event": "resync", "data": {"reason": "live_dropped", "watermarkSeq": 77}},
                   {"id": "78", "event": "change", "data": change(78)}], keep_open=True)

    server.handler = handler
    signals = collect(ChangeStream(server.url, sleep=recording_sleep[1]), lambda s, _: s.type == "change")
    assert [s.type for s in signals] == ["open", "resync", "snapshot", "change"]
    assert len(server.to("/v1/changes/stream")) == 1
    assert signals[2].cause == "resync" and signals[2].watermark_seq == 77


def test_heartbeat_timeout_reconnects(server, recording_sleep):
    count = {"n": 0}

    def handler(req, reply):
        count["n"] += 1
        if count["n"] == 1:
            reply.sse([{"event": "heartbeat", "data": {}}], keep_open=True)
        else:
            reply.sse([{"id": "1", "event": "change", "data": change(1)}], keep_open=True)

    server.handler = handler
    delays, sleep = recording_sleep
    stream = ChangeStream(server.url, heartbeat_interval=0.04, sleep=sleep)
    assert stream.watchdog_timeout == pytest.approx(0.1)
    started = time.monotonic()
    signals = collect(stream, lambda s, _: s.type == "change")
    reconnect = next(s for s in signals if s.type == "reconnecting")
    assert "HeartbeatTimeout" in reconnect.reason
    assert len([s for s in signals if s.type == "heartbeat"]) == 1
    assert time.monotonic() - started >= 0.09
    assert delays[0] >= 1.0
    assert len(server.to("/v1/changes/stream")) == 2


def test_observed_at_guard_drops_older_live_and_touch_events(server, recording_sleep):
    def live(ext, sec):
        return {"event": "live", "data": {"sourceId": "adsb", "externalId": ext, "kind": "aircraft", "version": 1,
                                          "observedAt": f"2026-10-18T00:00:0{sec}Z"}}

    def touch(ext, sec):
        return {"event": "touch", "data": {"sourceId": "adsb", "externalId": ext,
                                           "observedAt": f"2026-10-18T00:00:0{sec}Z"}}

    server.handler = lambda req, reply: reply.sse([
        live("A", 2), live("A", 1), touch("A", 3), live("B", 1),
        {"id": "9", "event": "change", "data": change(9, "A", "2026-10-18T00:00:05Z")},
        live("A", 4), touch("A", 5), {"event": "heartbeat", "data": {}},
    ], keep_open=True)
    stream = ChangeStream(server.url, sleep=recording_sleep[1])
    signals = collect(stream, lambda s, _: s.type == "heartbeat")
    applied = [f"{s.type}:{s.data['externalId']}@{s.data['observedAt'][17:19]}"
               for s in signals if s.type in ("live", "touch")]
    assert applied == ["live:A@02", "touch:A@03", "live:B@01", "touch:A@05"]
    assert stream.stale_dropped == 2


def test_stream_ticket_replaces_header_and_is_reminted_on_401(server, recording_sleep):
    state = {"tickets": 0, "streams": 0}

    def handler(req, reply):
        if req.path == "/v1/stream-tickets":
            state["tickets"] += 1
            return reply.json(201, {"ticket": f"fst_t{state['tickets']}", "purpose": "stream",
                                    "expiresAt": "2026-10-18T00:01:00Z"})
        state["streams"] += 1
        if state["streams"] == 1:
            reply.sse([{"id": "8", "event": "change", "data": change(8)}])
        elif state["streams"] == 2:
            reply.json(401, {"status": 401, "code": "UNAUTHORIZED"})
        else:
            reply.sse([{"id": "9", "event": "change", "data": change(9)}], keep_open=True)

    server.handler = handler
    stream = ChangeStream(server.url, "fs_secret", use_ticket=True, after=7, sleep=recording_sleep[1])
    collect(stream, lambda s, _: s.type == "change" and s.data["seq"] == 9)

    mints = server.to("/v1/stream-tickets", "POST")
    assert len(mints) == 2
    assert mints[0].header("Authorization") == "Bearer fs_secret"
    assert mints[0].body == '{"purpose": "stream"}'
    streams = server.to("/v1/changes/stream")
    assert len(streams) == 3
    assert all(s.header("Authorization") is None and s.header("Last-Event-ID") is None for s in streams)
    assert [(s.q("ticket"), s.q("after")) for s in streams] == [("fst_t1", "7"), ("fst_t1", "8"), ("fst_t2", "8")]


def test_retry_after_and_retry_field_set_the_reconnect_floor(server, recording_sleep):
    count = {"n": 0}

    def handler(req, reply):
        count["n"] += 1
        if count["n"] == 1:
            reply.json(429, {"status": 429, "code": "STREAM_LIMIT"}, {"Retry-After": "3"})
        elif count["n"] == 2:
            reply.sse([{"retry": 5000, "comment": "hint"}, {"id": "1", "event": "change", "data": change(1)}])
        else:
            reply.sse([{"id": "2", "event": "change", "data": change(2)}], keep_open=True)

    server.handler = handler
    delays, sleep = recording_sleep
    collect(ChangeStream(server.url, sleep=sleep, backoff=Backoff(random=lambda: 0.5)),
            lambda s, _: s.type == "change" and s.data["seq"] == 2)
    assert delays[0] >= 3.0
    assert delays[1] >= 5.0


def test_non_retryable_status_raises(server, recording_sleep):
    server.handler = lambda req, reply: reply.json(403, {"status": 403, "code": "INSUFFICIENT_SCOPE"})
    with pytest.raises(ApiError) as raised:
        collect(ChangeStream(server.url, sleep=recording_sleep[1]), lambda s, _: False)
    assert raised.value.code == "INSUFFICIENT_SCOPE"


def test_iterated_once_and_closed(server, recording_sleep):
    server.handler = lambda req, reply: reply.sse([{"event": "heartbeat", "data": {}}], keep_open=True)
    stream = ChangeStream(server.url, sleep=recording_sleep[1])
    signals = collect(stream, lambda s, _: s.type == "heartbeat")
    assert signals[-1].type == "heartbeat"
    assert stream.closed
    with pytest.raises(RuntimeError):
        iter(stream)


def test_events_endpoint_tracks_composite_cursor_and_yields_other_topics(server, recording_sleep):
    count = {"n": 0}

    def handler(req, reply):
        count["n"] += 1
        if count["n"] == 1:
            reply.sse([
                {"event": "hello", "data": {"streamId": "A" * 22, "topics": ["changes", "alerts"],
                                            "watermarkSeq": 100, "filterVersion": 1}},
                {"id": "c=101;a=5", "event": "change", "data": change(101)},
                {"id": "c=101;a=6", "event": "alert", "data": {"id": 6, "state": "firing"}},
            ])
        else:
            reply.sse([{"id": "c=102;a=6", "event": "change", "data": change(102)}], keep_open=True)

    server.handler = handler
    stream = ChangeStream(server.url, endpoint="events", sleep=recording_sleep[1],
                          filters={"topics": "changes,alerts", "changes.source": "adsb"})
    signals = collect(stream, lambda s, _: s.type == "change" and s.data["seq"] == 102)
    assert stream.stream_id == "A" * 22
    alert = next(s for s in signals if s.type == "event")
    assert alert.event == "alert" and alert.id == "c=101;a=6"
    streams = server.to("/v1/events/stream")
    assert streams[0].q("topics") == "changes,alerts"
    assert streams[0].q("changes.source") == "adsb"
    assert streams[1].header("Last-Event-ID") == "c=101;a=6"
    assert stream.last_seq == 102
    assert stream.last_event_id == "c=102;a=6"


def test_events_endpoint_reopens_at_watermark_keeping_other_cursors(server, recording_sleep):
    count = {"n": 0}

    def handler(req, reply):
        if req.path == "/v1/records":
            assert req.q("source") == "adsb"
            return reply.json(200, {"items": [], "nextCursor": None, "watermarkSeq": 900})
        count["n"] += 1
        if count["n"] == 1:
            reply.sse([{"id": "c=10;a=3", "event": "change", "data": change(10)},
                       {"event": "reset", "data": {"reason": "resync_required"}}])
        else:
            reply.sse([{"id": "c=901;a=3", "event": "change", "data": change(901)}], keep_open=True)

    server.handler = handler
    collect(ChangeStream(server.url, endpoint="events", sleep=recording_sleep[1],
                         filters={"changes.source": "adsb", "changes.live": True}),
            lambda s, _: s.type == "change" and s.data["seq"] == 901)
    assert server.to("/v1/events/stream")[1].header("Last-Event-ID") == "c=900;a=3"


def test_ticket_sends_last_event_id_in_query(server, recording_sleep):
    def handler(req, reply):
        if req.path == "/v1/stream-tickets":
            return reply.json(201, {"ticket": "fst_x", "purpose": "stream", "expiresAt": "2026-10-18T00:01:00Z"})
        reply.sse([{"event": "heartbeat", "data": {}}], keep_open=True)

    server.handler = handler
    collect(ChangeStream(server.url, endpoint="events", last_event_id="c=5;a=1", use_ticket=True,
                         sleep=recording_sleep[1]), lambda s, _: s.type == "heartbeat")
    req = server.to("/v1/events/stream")[0]
    assert req.q("lastEventId") == "c=5;a=1"
    assert req.q("ticket") == "fst_x"
    assert req.header("Last-Event-ID") is None


def test_composite_cursor_helpers():
    assert composite_cursor_value("c=1234;a=56;n=7", "a") == 56
    assert composite_cursor_value("a=56", "c") is None
    assert composite_cursor_value(None, "c") is None
    assert with_composite_cursor_value("c=1;a=2", "c", 9) == "c=9;a=2"
    assert with_composite_cursor_value(None, "c", 9) == "c=9"
    assert with_composite_cursor_value("a=2", "c", 9) == "c=9;a=2"


def test_backoff_doubles_from_1_to_60_seconds_with_jitter_and_floor():
    backoff = Backoff(random=lambda: 0.5)
    assert [backoff.next() for _ in range(9)] == [1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 60.0, 60.0, 60.0]
    backoff.reset()
    assert backoff.next(90.0) == 90.0
    low = Backoff(random=lambda: 0.0)
    assert low.next() == 1.0
    assert low.next() == 1.6
    high = Backoff(random=lambda: 0.999)
    high.next()
    assert high.next() <= 2.4


def test_observed_at_guard():
    guard = ObservedAtGuard()
    key = ObservedAtGuard.key("s", "e")
    assert guard.accept(key, "2026-10-18T00:00:02Z")
    assert not guard.accept(key, "2026-10-18T00:00:01Z")
    assert guard.accept(key, "2026-10-18T00:00:02Z")
    assert guard.accept(key, None)
    assert len(guard) == 1


def test_sse_parser_handles_split_lines_crlf_comments_and_ids():
    parser = SseParser()
    out = parser.feed("﻿id: 1\r")
    out += parser.feed("\nevent: change\r\ndata: {\"a\":\ndata: 1}\r\n\r\n: comment\n\nretry: 250\n")
    out += parser.feed("data: x\n\nid\ndata: y\n\nevent: e\n\n")
    assert [(m.event, m.data, m.id) for m in out] == [("change", '{"a":\n1}', "1"), ("message", "x", None),
                                                       ("message", "y", None)]
    assert parser.retry_ms == 250
