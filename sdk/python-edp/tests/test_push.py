"""Push client: batching limits, NDJSON, retries with backoff on 429/503 honoring Retry-After,
idempotent re-sends, snapshots, status and manifest updates."""

from __future__ import annotations

import json
from datetime import datetime, timedelta, timezone
from email.utils import format_datetime

import pytest

from foresignal_edp import (
    ItemTooLargeError,
    PushClient,
    PushError,
    decode_ndjson,
    delete,
    observation,
)
from foresignal_edp.push import TransportResponse, parse_retry_after

from .conftest import FakeTransport, network_error, ok_response, problem_response

NOW = datetime(2026, 10, 11, 18, 0, tzinfo=timezone.utc)


def make_client(transport: FakeTransport, sleeps: list[float] | None = None, **kwargs: object) -> PushClient:
    recorded = sleeps if sleeps is not None else []
    return PushClient("https://foresignal.example/", "ext-river-gauges", "eik_secret", transport=transport,
                      sleep=recorded.append, random=lambda: 0.5, clock=lambda: NOW, **kwargs)  # type: ignore[arg-type]


def items(n: int, blob: int = 0) -> list[dict]:
    return [observation("levels", series_key="kaub", t=f"2026-{(i % 12) + 1:02d}", value=i, observed_at=NOW,
                        item_id=f"i-{i}", properties={"blob": "x" * blob} if blob else None) for i in range(n)]


def test_request_shape_and_result() -> None:
    transport = FakeTransport([ok_response(2)])
    result = make_client(transport).push(items(2))
    assert result.accepted == 2 and result.ok and result.batches == 1
    request = transport.requests[0]
    assert request.method == "POST"
    assert request.url == "https://foresignal.example/v1/external/sources/ext-river-gauges/items"
    assert request.headers["X-Foresignal-Ingest-Key"] == "eik_secret"
    assert request.headers["Content-Type"] == "application/json"
    assert request.headers["User-Agent"].startswith("foresignal-edp-python/")
    body = json.loads(request.body)
    assert body["protocol"] == "foresignal-edp/1.0"
    assert body["sourceId"] == "ext-river-gauges"
    assert body["sentAt"] == "2026-10-11T18:00:00Z"
    assert [i["id"] for i in body["items"]] == ["i-0", "i-1"]


def test_secret_is_not_in_repr() -> None:
    assert "eik_secret" not in repr(make_client(FakeTransport()))


def test_batches_are_split_at_the_item_limit() -> None:
    transport = FakeTransport()
    client = make_client(transport, max_items=10_000)
    prepared = client.prepare(items(25_001))
    assert [p.item_count for p in prepared] == [10_000, 10_000, 5_001]
    assert [p.first_index for p in prepared] == [0, 10_000, 20_000]
    small = make_client(transport, max_items=3).prepare(items(7))
    assert [p.item_count for p in small] == [3, 3, 1]


def test_batches_are_split_at_the_byte_limit_and_never_exceed_it() -> None:
    client = make_client(FakeTransport())
    prepared = client.prepare(items(30, blob=400_000))  # ~12 MB in total
    assert len(prepared) >= 3
    assert all(p.size <= 5 * 1024 * 1024 for p in prepared)
    assert sum(p.item_count for p in prepared) == 30
    for p in prepared:
        assert len(json.loads(p.body)["items"]) == p.item_count
    tight = make_client(FakeTransport(), max_bytes=4096).prepare(items(40))
    assert all(p.size <= 4096 for p in tight) and sum(p.item_count for p in tight) == 40


def test_byte_limit_is_exact_at_the_boundary() -> None:
    client = make_client(FakeTransport())
    one = client.prepare(items(1))[0]
    two = client.prepare(items(2))[0]
    exact = make_client(FakeTransport(), max_bytes=two.size)
    assert len(exact.prepare(items(2))) == 1
    below = make_client(FakeTransport(), max_bytes=two.size - 1)
    assert [p.item_count for p in below.prepare(items(2))] == [1, 1]
    assert one.size < two.size


def test_item_too_large() -> None:
    client = make_client(FakeTransport(), max_bytes=1024)
    with pytest.raises(ItemTooLargeError) as raised:
        client.prepare(items(1) + items(1, blob=2000))
    assert raised.value.index == 1 and raised.value.limit == 1024


def test_ndjson_option() -> None:
    transport = FakeTransport([ok_response(3)])
    client = make_client(transport, ndjson=True, max_items=3)
    client.push(items(3))
    request = transport.requests[0]
    assert request.headers["Content-Type"] == "application/x-ndjson"
    lines = request.body.decode().splitlines()
    assert len(lines) == 4
    header = json.loads(lines[0])
    assert "type" not in header and "items" not in header and header["protocol"] == "foresignal-edp/1.0"
    decoded = decode_ndjson(request.body)
    assert [i["id"] for i in decoded["items"]] == ["i-0", "i-1", "i-2"]
    assert all(p.size <= 2048 for p in make_client(FakeTransport(), ndjson=True, max_bytes=2048).prepare(items(50)))


def test_empty_push_sends_nothing() -> None:
    transport = FakeTransport()
    result = make_client(transport).push([])
    assert result.batches == 0 and transport.requests == []


def test_retries_429_and_503_honoring_retry_after_with_identical_bodies() -> None:
    transport = FakeTransport([problem_response(429, "RATE_LIMITED", "7"),
                               problem_response(503, "INGEST_BUSY", "30"),
                               ok_response(2)])
    sleeps: list[float] = []
    result = make_client(transport, sleeps).push(items(2))
    assert result.accepted == 2
    assert sleeps == [7.0, 30.0]
    bodies = {r.body for r in transport.requests}
    assert len(transport.requests) == 3 and len(bodies) == 1  # idempotent re-send: the same bytes


def test_retry_after_as_http_date() -> None:
    later = format_datetime(datetime.now(timezone.utc) + timedelta(seconds=120), usegmt=True)
    transport = FakeTransport([problem_response(503, "INGEST_BUSY", later), ok_response(1)])
    sleeps: list[float] = []
    make_client(transport, sleeps).push(items(1))
    assert 100 <= sleeps[0] <= 121


def test_backoff_without_retry_after_is_exponential_and_capped() -> None:
    transport = FakeTransport([problem_response(503, "INGEST_BUSY")] * 5 + [ok_response(1)])
    sleeps: list[float] = []
    make_client(transport, sleeps, initial_backoff=1.0, max_backoff=8.0, max_retries=5).push(items(1))
    # random() == 0.5 gives three quarters of min(8, 2**attempt)
    assert sleeps == [0.75, 1.5, 3.0, 6.0, 6.0]


def test_network_failures_are_retried() -> None:
    transport = FakeTransport([network_error(), network_error(), ok_response(1)])
    sleeps: list[float] = []
    assert make_client(transport, sleeps).push(items(1)).accepted == 1
    assert len(sleeps) == 2 and len({r.body for r in transport.requests}) == 1


def test_retries_are_bounded() -> None:
    transport = FakeTransport([problem_response(429, "RATE_LIMITED", "1")] * 10)
    with pytest.raises(PushError) as raised:
        make_client(transport, max_retries=2).push(items(1))
    assert raised.value.status == 429 and raised.value.code == "RATE_LIMITED" and raised.value.retryable
    assert len(transport.requests) == 3
    network = FakeTransport([network_error()] * 5)
    with pytest.raises(PushError) as failed:
        make_client(network, max_retries=1).push(items(1))
    assert failed.value.status is None and failed.value.retryable


def test_excessive_retry_after_fails_as_retryable() -> None:
    transport = FakeTransport([problem_response(429, "RATE_LIMITED", "3600")])
    with pytest.raises(PushError) as raised:
        make_client(transport, max_retry_after=60).push(items(1))
    assert raised.value.retryable and len(transport.requests) == 1


@pytest.mark.parametrize("status, code", [(400, "SOURCE_MISMATCH"), (401, "UNAUTHORIZED"),
                                          (409, "SOURCE_PENDING_APPROVAL"), (413, "PAYLOAD_TOO_LARGE")])
def test_other_errors_are_not_retried(status: int, code: str) -> None:
    transport = FakeTransport([problem_response(status, code)])
    with pytest.raises(PushError) as raised:
        make_client(transport).push(items(1))
    assert raised.value.status == status and raised.value.code == code and not raised.value.retryable
    assert raised.value.problem is not None and code in str(raised.value)
    assert len(transport.requests) == 1


def test_failure_keeps_the_remaining_batches_for_an_identical_resend() -> None:
    transport = FakeTransport([ok_response(2), problem_response(409, "SOURCE_SUSPENDED")])
    client = make_client(transport, max_items=2)
    with pytest.raises(PushError) as raised:
        client.push(items(5))
    error = raised.value
    assert error.result.accepted == 2
    assert [b.item_count for b in error.remaining] == [2, 1]
    failed_body = transport.requests[1].body
    resend = FakeTransport([ok_response(2), ok_response(1)])
    client.transport = resend
    result = client.send_all(error.remaining)
    assert result.accepted == 3
    assert resend.requests[0].body == failed_body


def test_rejection_indexes_map_to_push_positions() -> None:
    rejected = {"index": 1, "stream": "levels", "id": "i-3", "reason": "PERIOD_INVALID",
                "schemaErrors": [{"path": "", "message": "bad"}]}
    transport = FakeTransport([ok_response(2), ok_response(1, [rejected])])
    result = make_client(transport, max_items=2).push(items(4))
    assert result.accepted == 3 and not result.ok
    assert result.rejected[0].index == 3 and result.rejected[0].reason == "PERIOD_INVALID"
    assert result.rejected[0].schema_errors[0].message == "bad"
    assert result.rejected[0].to_dict()["schemaErrors"] == [{"path": "", "message": "bad"}]


def test_push_snapshot_pages() -> None:
    transport = FakeTransport()
    client = make_client(transport, max_items=2)
    client.push_snapshot("cameras", [delete("cameras", str(i)) for i in range(5)], scope="north", snapshot_id="snap")
    snapshots = [json.loads(r.body)["snapshot"] for r in transport.requests]
    assert snapshots == [
        {"id": "snap", "stream": "cameras", "scope": "north", "page": 1, "final": False},
        {"id": "snap", "stream": "cameras", "scope": "north", "page": 2, "final": False},
        {"id": "snap", "stream": "cameras", "scope": "north", "page": 3, "final": True},
    ]


def test_empty_snapshot_is_one_final_page_and_ndjson_carries_it_in_the_header() -> None:
    transport = FakeTransport()
    make_client(transport, ndjson=True).push_snapshot("cameras", [])
    assert len(transport.requests) == 1
    header = json.loads(transport.requests[0].body.decode().splitlines()[0])
    assert header["snapshot"]["page"] == 1 and header["snapshot"]["final"] is True and header["snapshot"]["id"]


def test_report_status() -> None:
    transport = FakeTransport()
    make_client(transport).report_status("layout-changed", "the dashboard moved", stream="levels")
    assert json.loads(transport.requests[0].body)["items"] == [
        {"stream": "levels", "type": "status", "health": "layout-changed", "message": "the dashboard moved"}]


def test_update_manifest() -> None:
    transport = FakeTransport([TransportResponse(200, {}, b'{"state": "active"}')])
    response = make_client(transport).update_manifest({"protocol": "foresignal-edp/1.0"})
    assert response == {"state": "active"}
    assert transport.requests[0].method == "PUT"
    assert transport.requests[0].url.endswith("/v1/external/sources/ext-river-gauges/manifest")


def test_source_id_can_be_omitted_from_batches_and_empty_success_bodies_are_tolerated() -> None:
    transport = FakeTransport([TransportResponse(204, {}, b"")])
    result = make_client(transport, include_source_id=False).push(items(1))
    assert "sourceId" not in json.loads(transport.requests[0].body)
    assert result.batches == 1 and result.accepted == 0


def test_parse_retry_after() -> None:
    assert parse_retry_after("12") == 12.0
    assert parse_retry_after("-5") == 0.0
    assert parse_retry_after(None) is None and parse_retry_after(" ") is None and parse_retry_after("soon") is None
    when = datetime(2026, 10, 11, 18, 1, tzinfo=timezone.utc)
    assert parse_retry_after(format_datetime(when, usegmt=True), now=NOW) == 60.0


def test_constructor_limits() -> None:
    with pytest.raises(ValueError):
        PushClient("https://x", "ext-a", "k", max_items=10_001)
    with pytest.raises(ValueError):
        PushClient("https://x", "ext-a", "k", max_bytes=6 * 1024 * 1024)
    with pytest.raises(ValueError):
        PushClient("https://x", "ext-a", "k", max_retries=-1)
    with pytest.raises(ValueError):
        PushClient("https://x", "", "k")
