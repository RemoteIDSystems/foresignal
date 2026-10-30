# foresignal-client (Python)

The Python SDK for the Foresignal API, one distribution with two packages:

- **`foresignal_client`** (generated) — the openapi-python-client client for every operation in
  [`docs/OpenAPI.json`](../../docs/OpenAPI.json) (`generator-config.yaml`, `--meta none`). Regenerate with
  `./gradlew generateSdks`; never edit it. The generator skips a few operations it cannot model (plain-text
  request bodies, parameters with mixed integer and number enums, and two report operations whose block schema
  it cannot resolve); use `foresignal_sdk.http.HttpTransport` for those.
- **`foresignal_sdk`** (hand-written, on `httpx` only), with the same semantics as the Dart and TypeScript SDKs:
  - `foresignal_sdk.realtime` — `ChangeStream`, a generator over `/v1/changes/stream` or `/v1/events/stream`:
    `Last-Event-ID` resume (or stream tickets with `use_ticket=True`), snapshot paging on
    `reset {resync_required}` and `resync`, the `observedAt` guard for `live`/`touch`, a heartbeat watchdog
    (2.5 × the interval, enforced as the httpx read timeout) and jittered backoff (1 s to 60 s) honoring `retry:`
    and `Retry-After`;
  - `foresignal_sdk.paging` — `paginate`, `paginate_pages`, `paginate_with` (generators over `nextCursor`) and
    `iterate_changes`, which raises `ResyncRequiredError` on `410 RESYNC_REQUIRED`;
  - `foresignal_sdk.contract` — `parse_object_ref`, `record_ref`, `object_ref_path_segment`, the envelope
    `TypedDict`s, `resolve_objects` (batches of at most 500), `context_problems`, `apply_merge_patch`,
    `encode_deep_link`, `decode_deep_link` and `WorkspaceSync` (follows the workspace on a daemon thread).

Python 3.11 or later.

## Install

```sh
pip install ./foresignal/sdk/python
# or from git
pip install "git+https://gitlab.com/adam.allard/foresignal.git#subdirectory=sdk/python"
```

## Use

```python
from foresignal_client import AuthenticatedClient
from foresignal_client.api.records import list_records
from foresignal_sdk.http import HttpTransport
from foresignal_sdk.realtime import ChangeStream
from foresignal_sdk.paging import paginate
from foresignal_sdk.contract import WorkspaceSync

client = AuthenticatedClient(base_url="https://foresignal.example.org", token="fs_…")
page = list_records.sync(client=client, limit=100)

transport = HttpTransport("https://foresignal.example.org", "fs_…")
for alert in paginate(transport, "/v1/alerts", {"status": "firing"}):
    ...

for signal in ChangeStream(transport=transport, filters={"kind": "aircraft", "touches": True}):
    if signal.type in ("change", "live", "touch"):
        apply(signal.data)
    elif signal.type == "reset":
        clear_local_state()
    elif signal.type == "snapshot":
        for record in signal.items:
            apply_record(record)

sync = WorkspaceSync(transport=transport, workspace_id="01JAB…", client_kind="analyst")
sync.on_context(lambda change: render(change.context))
sync.start()
sync.update(lambda ctx: {"focusRef": "situation:01JA8…"})
```

## Tests

`pip install -e ".[test]"` then `pytest` (an in-process `ThreadingHTTPServer` plays the SSE server; the contract
tests read [`sdk/contract-fixtures`](../contract-fixtures)).

## Versioning and publishing

The version is the API's `info.version` (`2.1.1`); tool and dependency versions are pinned in
[`sdk/versions.properties`](../versions.properties). The distribution carries the `Private :: Do Not Upload`
classifier; publishing to PyPI is a separate release step.
