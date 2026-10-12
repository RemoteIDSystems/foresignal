# Foresignal — Agent and client guide

How programs read Foresignal: agents, dashboards, mobile and desktop clients, home automation.
Prompts add sections as they build the features (015 streaming, 017 rules, 025 MCP, 028 Ask, 029
briefings). The API contract is `docs/OpenAPI.json`; this guide explains how to use it well.

## Reading records

- `GET /v1/records` returns current state: one item per entity, with the newest live position and
  the merged payload. Expired (`validUntil` in the past) and tombstoned records are left out unless
  you pass `includeExpired=true` (tombstones never appear).
- `GET /v1/records/{sourceId}/{externalId}` returns one record. `/versions` lists its persisted
  versions, oldest first.
- `GET /v1/history?since=…` returns every version in a time range, for playback.
- `GET /v1/changes?after=<seq>` returns every new version in commit order.
- `GET /v1/kinds` lists the kinds present now; `GET /v1/kinds/{kind}/schema` describes a kind's
  payload fields (JSON Schema 2020-12 with units and labels).

### Keeping responses small

- `fields=externalId,title,geometry,payload.mag` returns only those properties. Unknown top-level
  names are a 400.
- `precision=0…7` rounds coordinates (storage is unchanged).
- `simplify=<meters>` simplifies lines and polygons. Topology is kept per feature only, so
  neighboring polygons may gain small gaps or overlaps. Points are never changed.
- `format=geojson` (or `Accept: application/geo+json`) returns a FeatureCollection.
- `format=ndjson`, `geojsonseq` (RFC 8142) and `csv` stream the whole result without paging, up to
  `foresignal.api.stream.maxRows` (500,000) rows. A larger result is refused with
  `400 RESULT_TOO_LARGE`; it is never cut short silently. `csv` writes geometry as WKT and needs
  `fields=payload.<path>` entries for payload columns.
- Responses are gzip-compressed when you send `Accept-Encoding: gzip`.
- Lists, single records, kinds and kind schemas carry a strong `ETag`. Send it back as
  `If-None-Match` to get `304 Not Modified`. A list tag changes on every write and at least once a
  minute, because expiry depends on the clock.

## Keeping a mirror

An offline client (a phone, a laptop on a plane) can keep a copy of the data that is provably
complete. The contract has three parts: a snapshot with a watermark, a change feed that starts at
that watermark, and a rule for applying events.

1. **Snapshot.** Page `GET /v1/records?order=id&limit=…` from the first page to the last, following
   `nextCursor`. Every page returns `watermarkSeq`, read before the query. With `order=id` the
   first page pins the snapshot at that watermark (`asOfSeq` in the response), and every later page
   keeps the same pin, so the pages form one consistent snapshot even while data changes underneath
   it. Entities that change after the pin are left out of the remaining pages.
2. **Catch up.** Replay `GET /v1/changes?after=<watermarkSeq>` (or open the SSE stream with
   `after=<watermarkSeq>`, prompt 015). This delivers every version committed after the pin,
   including the entities the snapshot left out. Store `lastSeq` and pass it as `after` next time.
   `seq` has gaps but strictly increases in commit order, so `after=<lastSeq>` never misses a
   change. When a page is the last one (`hasMore: false`), `lastSeq` is at least the watermark, so a
   filtered feed does not rescan seqs that matched nothing.
3. **Apply.** Apply an event only if its `observedAt` is at least the stored one; otherwise you
   already have newer state. A `tombstone: true` change deletes the entity.
4. **Expiry.** Records and changes carry `validUntil`. Expiry produces no change event: drop an
   entity yourself once its `validUntil` has passed.
5. **Resync.** If retention purged entities without tombstones after your `after`, the feed answers
   `410 RESYNC_REQUIRED` with `purgeHorizon` and `watermarkSeq`. Throw the mirror away and start
   again at step 1. Routine retention (old versions deleted every hour) never causes this; only
   purges of whole entities do.

`raster-frame` changes always include their payload, even with `payload=false`, because the frame
description is small and a client needs it to fetch tiles.

## Replaying the past (asOf)

`GET /v1/records?asOf=<T>&source=…` returns the state Foresignal knew at transaction time `T`, to
the granularity of one ingest batch. It needs a `source` or `kind` filter
(`400 ASOF_FILTER_REQUIRED`), and `T` must lie after the retention horizon of every source
involved (`400 ASOF_BEYOND_RETENTION`, with `horizon`). It cannot be combined with `asOfSeq`,
`order=observed`, `sort=distance`, `includeExpired`, `since` or `until` (`400 ASOF_INVALID`). The
response carries `asOf`, `watermarkSeq` (the last seq known at `T`), `horizon`, and `approximate`,
which is true when a sampled source (SAMPLED or LATEST policy) is involved: those sources store
only some observations, so positions between stored versions are not exact. For valid time (what
was true at `T` according to the data itself), use `/v1/history`.

## Home Assistant

Home Assistant's `geo_json_events` integration can show Foresignal records on its map. Point it at a
GeoJSON URL with a bounding box and an explicit limit:

```
http://foresignal.local:8080/v1/records?format=geojson&kind=earthquake&bbox=-106.7,25.8,-93.5,36.5&limit=500
```

The integration cannot send a bearer token, so this works only while `security.openReads` is on
(the default). Feature properties carry `title`, which Home Assistant shows as the entity name.
