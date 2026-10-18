"""Client-contract round trips on the fixtures shared by the three SDKs (sdk/contract-fixtures), plus resolve
batching against an in-process server."""
from __future__ import annotations

import copy
import json

import pytest

from conftest import load_fixture
from foresignal_sdk.contract import (
    apply_merge_patch,
    batch_refs,
    context_problems,
    contract_problems,
    decode_deep_link,
    decode_external_id,
    encode_deep_link,
    encode_external_id,
    format_object_ref,
    is_valid_object_ref,
    object_ref_path_segment,
    parse_object_ref,
    record_ref,
    remote_ref,
    resolve_objects,
    strip_null_members,
    validate_context,
)
from foresignal_sdk.errors import ContextValidationError, DeepLinkError, ObjectRefError
from foresignal_sdk.http import HttpTransport

REFS = load_fixture("refs.json")
CONTEXTS = load_fixture("contexts.json")


@pytest.mark.parametrize("case", REFS["valid"], ids=lambda c: c["ref"][:60])
def test_valid_refs_round_trip(case):
    parsed = parse_object_ref(case["ref"])
    assert parsed.ref == case["ref"]
    assert parsed.type == case["type"]
    assert parsed.id == case["id"]
    assert format_object_ref(parsed.type, parsed.id) == case["ref"]
    assert object_ref_path_segment(case["ref"]) == case["pathSegment"]
    assert is_valid_object_ref(case["ref"])
    record = case.get("record")
    if record:
        assert parsed.record is not None
        assert (parsed.record.source_id, parsed.record.external_id, parsed.record.seq) == (
            record["sourceId"], record["externalId"], record["seq"])
        assert record_ref(record["sourceId"], record["externalId"], record["seq"]) == case["ref"]
        assert decode_external_id(encode_external_id(record["externalId"])) == record["externalId"]
    else:
        assert parsed.record is None
    sensor = case.get("sensor")
    assert (parsed.sensor_kind, parsed.sensor_id) == ((sensor["kind"], sensor["id"]) if sensor else (None, None))
    remote = case.get("remote")
    if remote:
        assert (parsed.remote_instance_id, parsed.remote_ref) == (remote["instanceId"], remote["ref"])
        assert remote_ref(remote["instanceId"], remote["ref"]) == case["ref"]
    else:
        assert parsed.remote_instance_id is None


@pytest.mark.parametrize("case", REFS["invalid"], ids=lambda c: c["reason"])
def test_invalid_refs_are_rejected(case):
    with pytest.raises(ObjectRefError):
        parse_object_ref(case["ref"])
    assert not is_valid_object_ref(case["ref"])


def test_known_types_restrict_parsing():
    with pytest.raises(ObjectRefError):
        parse_object_ref("future-type:x", ["situation", "record"])
    assert parse_object_ref("situation:x", ["situation"]).type == "situation"


@pytest.mark.parametrize("case", CONTEXTS["contexts"], ids=lambda c: c["name"])
def test_deep_link_encodes_fixture_exactly(case):
    link = encode_deep_link(case["workspaceId"], case["context"])
    assert link.payload == case["payload"]
    assert link.fragment == case["fragment"]
    assert link.bytes == case["bytes"]
    assert link.version == CONTEXTS["deepLinkVersion"]


@pytest.mark.parametrize("case", CONTEXTS["contexts"], ids=lambda c: c["name"])
def test_deep_link_decodes_payload_fragment_and_url(case):
    for text in (case["payload"], case["fragment"], f"https://globe.example.org/app{case['fragment']}"):
        decoded = decode_deep_link(text)
        assert decoded.version == 1
        assert decoded.workspace_id == case["workspaceId"]
        assert decoded.context == case["decodedContext"]
    assert strip_null_members(case["context"]) == case["decodedContext"]


@pytest.mark.parametrize("case", CONTEXTS["invalidPayloads"], ids=lambda c: c["name"])
def test_deep_link_refuses_invalid_payloads(case):
    with pytest.raises(DeepLinkError) as raised:
        decode_deep_link(case["payload"])
    assert raised.value.kind == case["error"]


def test_deep_link_refuses_contexts_over_2_kb():
    case = CONTEXTS["tooLarge"]
    with pytest.raises(DeepLinkError) as raised:
        encode_deep_link(case["workspaceId"], case["context"])
    assert raised.value.kind == case["error"]


@pytest.mark.parametrize("case", CONTEXTS["mergePatches"], ids=lambda c: c["name"])
def test_merge_patch(case):
    target = copy.deepcopy(case["target"])
    assert apply_merge_patch(target, case["patch"]) == case["result"]
    assert target == case["target"]


def test_context_validation_mirrors_the_server():
    assert context_problems(CONTEXTS["contexts"][0]["context"]) == []
    errors = context_problems({
        "camera": {}, "timeRange": {"end": "2026-10-01T00:00:00Z"}, "playback": {"state": "rewinding"},
        "viewport": {"bbox": [0, 0, 1, 1], "center": [0, 0]}, "layers": [""], "activeSituationId": "nope",
        "selectedRefs": ["situation:ok", "bad ref"]})
    assert sorted(e["field"] for e in errors) == sorted([
        "context.activeSituationId", "context.camera", "context.layers", "context.playback.state",
        "context.selectedRefs[1]", "context.timeRange.start", "context.viewport"])
    with pytest.raises(ContextValidationError):
        validate_context({"timeRange": {"start": "2026-10-02T00:00:00Z", "end": "2026-10-01T00:00:00Z"}})
    assert context_problems({"viewport": {"center": [10, 10], "radiusKm": 25000}})[0]["field"] == \
        "context.viewport.radiusKm"


def test_contract_versions():
    ok = {"contractVersion": "1.5", "objectTypes": [], "relationTypes": [], "contextSchemaVersion": 1,
          "deepLinkVersion": 1}
    assert contract_problems(ok) == []
    assert len(contract_problems({**ok, "contractVersion": "2.0", "deepLinkVersion": 2})) == 2


def test_resolve_batches_of_500_keep_order_and_drop_duplicates(server):
    def handler(req, reply):
        body = json.loads(req.body)
        refs = body["refs"]
        reply.json(200, {"items": [{"ref": r, "type": "situation", "title": r} for r in refs if not r.endswith("7")],
                         "missing": [r for r in refs if r.endswith("7")], "asOf": body.get("asOf")})

    server.handler = handler
    refs = [f"situation:S{i}" for i in range(1200)] + ["situation:S0"]
    result = resolve_objects(HttpTransport(server.url, "fs_t"), refs, as_of="2026-10-01T00:00:00Z")
    calls = server.to("/v1/objects/resolve", "POST")
    assert [len(json.loads(c.body)["refs"]) for c in calls] == [500, 500, 200]
    assert json.loads(calls[0].body)["asOf"] == "2026-10-01T00:00:00Z"
    assert calls[0].header("Authorization") == "Bearer fs_t"
    assert len(result["items"]) + len(result["missing"]) == 1200
    assert "situation:S7" in result["missing"]
    assert result["items"][0]["ref"] == "situation:S0"
    assert result["asOf"] == "2026-10-01T00:00:00Z"


def test_resolve_validates_refs_before_sending():
    with pytest.raises(ObjectRefError):
        resolve_objects(HttpTransport("http://127.0.0.1:9"), ["situation:ok", "nope"])
    assert batch_refs(["a:1", "b:2", "c:3"], 2) == [["a:1", "b:2"], ["c:3"]]
