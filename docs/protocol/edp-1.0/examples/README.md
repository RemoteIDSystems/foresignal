# EDP 1.0 provider kit: examples

Documentation for provider authors; Foresignal never loads these files. The protocol is described in
[External Data Protocol](../../../External-Data-Protocol.md) and the JSON Schemas are one directory up. The
provider-ideas catalog is in [Source Catalog](../../../Source-Catalog.md), "External provider ideas".

## One minimal provider per item type

Each `*.provider.json` holds a `manifest` (register it with `POST /v1/external/sources`), a `batch` (push it with
`POST /v1/external/sources/ext-{source.id}/items` and the ingest key), and the `bindings` an operator would create with
`POST /v1/external/sources/ext-{source.id}/bindings` (prompt 138). `$comment` members explain each choice; unknown members
are ignored by the protocol, but they are not part of a real manifest or batch.

| File | Item type | Stream | Binding it shows |
|---|---|---|---|
| `observation.provider.json` | `observation` | atomic | `series`, `place` with a filter |
| `event.provider.json` | `event` | layer, with `coverage` and an aggregating originator | `canonical-family`, `indicator` |
| `feature.provider.json` | `feature` | layer, snapshot per region | `place` |
| `document.provider.json` | `document` (`social-post`, `article`) | atomic | `social`, `news` |
| `forecast.provider.json` | `forecast` | atomic | `forecast`, plus a question link |
| `delete.provider.json` | `delete` | layer | none |
| `status.provider.json` | `status` | source and stream health | none |

Placeholders such as `PLACE-ID-OF-KAUB`, `INDICATOR-ID` and `LEDGER-QUESTION-ID` stand for ids on your instance.
Validate a manifest and sample items without storing anything with `POST /v1/external/validate`.

## Reference provider (Python, push and pull)

`csv_provider.py` (about 60 lines, standard library only) reads a CSV with the columns
`series_key,period,value[,known_at]` (`sample.csv`) and serves its rows as `observation` items:

- `python3 csv_provider.py push sample.csv https://foresignal.example eik_YOUR_INGEST_KEY` pushes batches of 500 with the ingest
  key, after the manifest in the script was registered and approved;
- `python3 csv_provider.py serve sample.csv 8099` serves `GET /.well-known/foresignal-edp.json` (the manifest) and
  `GET /edp/items?cursor=&limit=` (pages with `nextCursor`, null when caught up) for Foresignal to pull. Its manifest
  names `http://localhost:8099`, so register it with `allowPrivateNetwork: true`.

Bind the stream with target `series` to get `series:ext-csv-example/csv-value/{series_key}`.

Prompt 141 adds Python and TypeScript provider libraries (batching, NDJSON, cursors, retries, the pull endpoint) and
`foresignal edp validate`.
