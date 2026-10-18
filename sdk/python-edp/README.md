# foresignal-edp (Python)

Provider library for the Foresignal **External Data Protocol (EDP) 1.0**
([docs/External-Data-Protocol.md](../../docs/External-Data-Protocol.md)). It is independent of the
Foresignal API client, so a provider project stays small: the only dependency is `jsonschema`.

What it gives you:

| Module | Purpose |
|---|---|
| `ManifestBuilder`, `StreamBuilder` | Write a manifest with Python names, including `coverage`, `complete`, `suggestedBindings`, snapshot streams and field roles. The wire keys keep the protocol's spelling (`source.licence`). |
| `validate_manifest` | The registration checks: the exported manifest schema, protocol minor, delivery mechanism, unique stream ids, layer item types and geometry, slugs, pull and search URLs, field roles, presentation hints, TTLs, and every `payloadSchema` (64 KB, local `$ref`s only, patterns of at most 256 characters a linear-time engine accepts). |
| `feature`, `event`, `observation`, `document`, `forecast`, `delete`, `status` | Typed item builders, plus `point`/`polygon`/... geometry helpers, `origin`, `question`, `quantiles`, `author`, `media`, `snapshot` and `batch`. |
| `validate_items`, `validate_batch` | Per-item validation in the server's order, with its rejection codes (`SCHEMA_INVALID`, `UNKNOWN_STREAM`, `PAYLOAD_INVALID`, `GEOMETRY_INVALID`, `PERIOD_INVALID`, `FORECAST_INVALID`, ...), and the batch-level refusals. |
| `PushClient` | `POST /v1/external/sources/{sourceId}/items` with `X-Foresignal-Ingest-Key`: batches of at most 10,000 items and 5 MB, optional NDJSON, retries with backoff on 429 and 503 honoring `Retry-After`, idempotent re-sends of byte-identical batches, snapshot paging, status reports, manifest updates. |
| `PullHandler` | A framework-neutral handler for `GET items?cursor=&limit=` and optional `search`, with a pluggable `CursorStore`, `SequenceSource` (offset paging) and `SnapshotSource` (snapshot paging), auth checks, a 5 MB page cap, and `wsgi_app` for any WSGI server. |
| `StatusBoard` | Health for pull providers; changes ride on the next page served. |

## Install

From a checkout of this repository (local path):

```sh
python3 -m pip install ./sdk/python-edp            # or: pip install -e ./sdk/python-edp[test]
```

From git:

```sh
python3 -m pip install "foresignal-edp @ git+https://<your-git-host>/<group>/foresignal.git#subdirectory=sdk/python-edp"
```

Python 3.10 or newer. Publishing to a package index is a separate release step and is not done here.

## Versioning

The major and minor version follow the EDP protocol version: `1.0.x` speaks `foresignal-edp/1.0`.
The patch number counts library releases for that protocol version. A later `1.N` protocol (always
additive) gets a `1.N.0` library; the validator's `max_minor` defaults to the minor it knows.

## Quick start

```python
from foresignal_edp import ManifestBuilder, StreamBuilder, PushClient, observation, validate_items

manifest = (ManifestBuilder("river-gauges", "River gauges")
            .publisher("Example Hydro")
            .originator("example-hydro", "sensor-network")
            .access("unofficial-api", "medium")
            .license(redistribution="local-only")
            .push()
            .stream(StreamBuilder("levels", mode="atomic", item_type="observation", kind="river-level")
                    .geometry("none").unit("m").suggest_binding("series"))
            .build())                       # raises ManifestInvalidError with every problem

items = [observation("levels", series_key="kaub", t="2026-10-15", value=1.42,
                     observed_at="2026-10-15T06:00:00Z")]
assert validate_items(items, manifest).ok   # the same checks Foresignal runs

client = PushClient("https://foresignal.example", "ext-river-gauges", "eik_...", ndjson=False)
result = client.push(items)                 # PushResult(accepted, rejected, batches, responses)
client.report_status("ok", stream="levels")
```

A failed push raises `PushError` with `status`, `code` (the ProblemDetail code), `retryable`,
`result` (what was accepted) and `remaining` (the prepared batches not accepted, the failed one
first). `client.send_all(error.remaining)` re-sends them byte for byte later; Foresignal turns
repeated items into touches, so re-sending is always safe.

Snapshots (EDP §3.2): `client.push_snapshot("cameras", all_current_items, scope="north")` pages the
full current set with one snapshot id and `final: true` on the last page. Nothing is deleted unless
every page arrives.

### Serving a pull endpoint

```python
from foresignal_edp import PullHandler, SequenceSource, OpaqueCursorStore, header_auth, wsgi_app

handler = PullHandler(
    SequenceSource(lambda: load_items_in_append_order()),
    cursor_store=OpaqueCursorStore(secret="change-me"),   # stateless, signed, survives restarts
    authorize=header_auth("X-Api-Key", "the-key-the-operator-enters"),
    manifest=manifest,                                     # served at /.well-known/foresignal-edp.json
)
app = wsgi_app(handler)    # mount in Flask, Django, gunicorn...; or call handler.handle_items(query, headers)
```

Foresignal follows `nextCursor` until it is `null` and starts its next run from the last cursor it
sent, so cursors must stay valid and replayable (asking again returns the items from that position
on). `SequenceSource` does this by offset; `SnapshotSource(stream, list_items, scope=...)` serves full
snapshots with keyset paging and starts a fresh snapshot after a finished one; any object with
`fetch(position, limit) -> Page` works. `MemoryCursorStore` keeps short random tokens but forgets
them on restart; implement `encode`/`decode` against your database for a durable store.

## Example provider

[`examples/river_gauges_provider.py`](examples/river_gauges_provider.py) serves
[`examples/river_levels.csv`](examples/river_levels.csv) by push and pull, with an optional API key
and `blocked` status when the CSV is missing:

```sh
python3 examples/river_gauges_provider.py manifest https://hydro.example.org
python3 examples/river_gauges_provider.py push examples/river_levels.csv https://foresignal.example eik_YOUR_KEY
RIVER_GAUGES_API_KEY=secret python3 examples/river_gauges_provider.py serve examples/river_levels.csv 8099
```

## Schemas and conformance fixtures

- `src/foresignal_edp/schemas/*.schema.json` are byte-for-byte **copies** of
  `docs/protocol/edp-1.0/` (generated from `docs/OpenAPI.json`). Refresh them with
  `python3 scripts/sync_schemas.py`; `tests/test_schemas_sync.py` fails while they differ.
- The tests run the prompt-024 conformance fixtures **by relative path** from
  `../../src/test/resources/edp/conformance` (the server's own copy, so they cannot drift). Outside
  the repository, set `FORESIGNAL_EDP_CONFORMANCE_DIR`.
- `format` keywords are annotations, as on the server; times are checked by the item validator.
  Patterns are checked with Python's `re` after refusing the constructs RE2 rejects; the server
  (RE2) remains the final judge.

## Tests

```sh
python3 -m pip install -e '.[test]'
python3 -m pytest
```
