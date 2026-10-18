"""WorkspaceSync: remote patches in version order, own-origin events ignored, gap and reconnect re-reads,
presence, and the 412 retry."""
from __future__ import annotations

import json

import pytest

from conftest import wait_for
from foresignal_sdk.contract import WorkspaceSync
from foresignal_sdk.errors import ApiError

W = "01JAB0000000000000000000WS"


def ctx_event(version, patch, origin):
    return {"event": "workspace.context", "data": {"workspaceId": W, "version": version, "patch": patch,
                                                   "originClientId": origin, "principal": "p1",
                                                   "at": "2026-10-18T00:00:00Z"}}


@pytest.fixture
def syncs():
    created = []
    yield created
    for s in created:
        s.stop()


def test_remote_patches_in_order_own_origin_ignored_gap_reread_and_presence(server, syncs, recording_sleep):
    gets = {"n": 0}

    def handler(req, reply):
        if req.path == f"/v1/workspaces/{W}" and req.method == "GET":
            gets["n"] += 1
            if gets["n"] == 1:
                return reply.json(200, {"id": W, "context": {"layers": ["a"]}, "contextVersion": 3})
            return reply.json(200, {"id": W, "context": {"layers": ["refreshed"]}, "contextVersion": 9})
        if req.path == "/v1/events/stream":
            return reply.sse([
                {"event": "hello", "data": {"streamId": "A" * 22, "topics": ["workspace"], "watermarkSeq": 0,
                                            "filterVersion": 1}},
                {"event": "workspace.presence", "data": {
                    "workspaceId": W, "at": "2026-10-18T00:00:00Z",
                    "clients": [{"clientId": "me", "clientKind": "map", "connectedAt": "2026-10-18T00:00:00Z"},
                                {"clientId": "other", "clientKind": "globe", "principal": "p1",
                                 "connectedAt": "2026-10-18T00:00:00Z"}],
                    "change": {"type": "joined", "clientId": "other"}}},
                ctx_event(4, {"layers": ["a", "b"]}, "other"),
                ctx_event(5, {"focusRef": "situation:own"}, "me"),
                ctx_event(6, {"timeRange": {"start": "2026-10-01T00:00:00Z"}}, "mcp:session-1"),
                ctx_event(6, {"layers": ["duplicate"]}, "other"),
                ctx_event(9, {"layers": ["after-gap"]}, "other"),
            ], keep_open=True)
        reply.json(404, {})

    server.handler = handler
    changes, presence = [], []
    sync = WorkspaceSync(server.url, "fs_t", workspace_id=W, client_id="me", client_kind="map",
                         sleep=recording_sleep[1])
    syncs.append(sync)
    sync.on_context(changes.append)
    sync.on_presence(presence.append)
    sync.start()
    wait_for(lambda: any(c.version == 9 for c in changes))

    assert [f"{c.origin}:{c.version}" for c in changes] == ["refresh:3", "remote:4", "remote:6", "refresh:9"]
    after_six = changes[2].context
    assert after_six["layers"] == ["a", "b"]
    assert after_six["timeRange"] == {"start": "2026-10-01T00:00:00Z"}
    assert "focusRef" not in after_six
    assert sync.version == 9
    assert sync.context == {"layers": ["refreshed"]}
    assert len(presence) == 1
    assert [c["clientId"] for c in sync.presence] == ["me", "other"]

    stream = server.to("/v1/events/stream")[0]
    assert stream.q("topics") == "workspace"
    assert stream.q("workspace.workspaceId") == W
    assert stream.q("workspace.clientId") == "me"
    assert stream.q("workspace.clientKind") == "map"
    assert stream.header("Authorization") == "Bearer fs_t"


def test_reconnect_rereads_the_workspace(server, syncs, recording_sleep):
    counts = {"gets": 0, "streams": 0}

    def handler(req, reply):
        if req.path == f"/v1/workspaces/{W}":
            counts["gets"] += 1
            return reply.json(200, {"id": W, "context": {}, "contextVersion": counts["gets"]})
        counts["streams"] += 1
        reply.sse([{"event": "heartbeat", "data": {}}], keep_open=counts["streams"] > 1)

    server.handler = handler
    sync = WorkspaceSync(server.url, workspace_id=W, sleep=recording_sleep[1])
    syncs.append(sync)
    sync.start()
    wait_for(lambda: counts["gets"] >= 2)
    wait_for(lambda: sync.version == 2)
    assert sync.client_id.startswith("c-") and len(sync.client_id) == 18


def test_stale_patch_is_retried_with_if_match_after_rereading(server, syncs):
    state = {"gets": 0, "patches": 0}

    def handler(req, reply):
        if req.method == "GET":
            state["gets"] += 1
            if state["gets"] == 1:
                return reply.json(200, {"id": W, "context": {"layers": ["a"]}, "contextVersion": 3})
            return reply.json(200, {"id": W, "context": {"layers": ["a", "x"]}, "contextVersion": 4})
        state["patches"] += 1
        if state["patches"] == 1:
            return reply.json(412, {"status": 412, "code": "WORKSPACE_CONTEXT_STALE", "contextVersion": 4})
        patch = json.loads(req.body)
        return reply.json(200, {"workspaceId": W, "contextVersion": 5, "context": {"layers": patch["layers"]}})

    server.handler = handler
    sync = WorkspaceSync(server.url, "fs_t", workspace_id=W, client_id="me")
    syncs.append(sync)
    result = sync.update(lambda ctx: {"layers": [*ctx.get("layers", []), "b"]})

    assert result["contextVersion"] == 5
    assert sync.version == 5
    assert sync.context == {"layers": ["a", "x", "b"]}
    sent = server.to(f"/v1/workspaces/{W}/context", "PATCH")
    assert [r.header("If-Match") for r in sent] == ["3", "4"]
    assert [json.loads(r.body) for r in sent] == [{"layers": ["a", "b"]}, {"layers": ["a", "x", "b"]}]
    assert all(r.header("Content-Type") == "application/merge-patch+json" for r in sent)
    assert all(r.header("Foresignal-Client-Id") == "me" for r in sent)
    assert state["gets"] == 2


def test_gives_up_after_max_attempts_and_passes_other_errors(server, syncs):
    def handler(req, reply):
        if req.method == "GET":
            return reply.json(200, {"id": W, "context": {}, "contextVersion": 1})
        if "forbidden" in req.body:
            return reply.json(403, {"status": 403, "code": "WORKSPACE_FORBIDDEN"})
        return reply.json(412, {"status": 412, "code": "WORKSPACE_CONTEXT_STALE"})

    server.handler = handler
    sync = WorkspaceSync(server.url, workspace_id=W, max_attempts=2)
    syncs.append(sync)
    with pytest.raises(ApiError) as stale:
        sync.update({"layers": ["x"]})
    assert stale.value.status == 412
    assert len(server.to(f"/v1/workspaces/{W}/context", "PATCH")) == 2
    with pytest.raises(ApiError) as forbidden:
        sync.update({"layers": ["forbidden"]})
    assert forbidden.value.code == "WORKSPACE_FORBIDDEN"
    assert sync.update(lambda ctx: None)["contextVersion"] == 1


def test_rejects_unknown_client_kind():
    with pytest.raises(ValueError):
        WorkspaceSync("http://127.0.0.1:9", workspace_id=W, client_kind="tablet")
