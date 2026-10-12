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
   `after=<watermarkSeq>`; see "Streaming"). This delivers every version committed after the pin,
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

## Streaming

Two Server-Sent Events endpoints push changes as they are committed (typically within a second):

- `GET /v1/changes/stream` carries the `changes` topic only. Filters: `source`, `kind`,
  `externalId`, `bbox` (a box with `minLon > maxLon` crosses the antimeridian), `near` +
  `radiusKm`, `payload` (default false), `live` (default true) and `touches` (default false).
- `GET /v1/events/stream` multiplexes topics on one connection: `topics=changes,alerts` today,
  `notifications` and `system` as later releases add them. Filters take the topic as a prefix
  (`changes.bbox=…`, `changes.live=false`, `alerts.minSeverity=warning`). The first event is `hello {streamId, topics,
  watermarkSeq, filterVersion}`. The filter can be swapped without reconnecting (below).

Send the token in the `Authorization` header; tokens are never accepted in the query string.
Responses are never compressed, and carry `X-Accel-Buffering: no` so a reverse proxy does not
buffer them.

### Two lanes

- **`change` events are lossless.** Each is one persisted version (the `Change` shape of
  `/v1/changes`, plus `prevPersistedBbox`, `prevLiveBbox` and `leftFilter`) and carries
  `id: <seq>`. Reconnect with `Last-Event-ID` (browsers do this automatically) or `after=<seq>`, and
  the stream replays exactly what you missed before going live: no gap, no duplicate. A replay is
  capped at 10,000 events; beyond that you get `reset {reason: "replay_cap", resumeAfter}`, and you
  reconnect from `resumeAfter`. Without a resume point the stream starts live from now.
- **`live` and `touch` events are conflated and not stored.** A moving aircraft produces many
  positions that are not persisted as versions; the stream keeps only the newest one per entity
  and sends it about once a second. A `touch` says an entity was seen again unchanged (only with
  `touches=true`). These events have **no `id:`**, on purpose: they cannot be replayed, so
  `Last-Event-ID` always names a `change` and a reconnect never skips a persisted version.
  Control events (`resync`, `reset`, `heartbeat`, `hello`, `filter-applied`) have no id either.

`heartbeat` arrives every 15 s as a named event with data `{}`; treat a missing heartbeat as a dead
connection.

### Client contract

1. **Areas.** With `bbox` or `near`, a change is delivered when its new box *or* one of its
   previous boxes intersects the area. When only a previous box does, the event carries
   `leftFilter: true`: the entity has left your view, so remove it. Tombstones are delivered the
   same way.
2. **Order.** Apply an event only if its `observedAt` is at least the one you stored.
3. **`resync`** (`reason`: `gap`, `live_cap`, `live_dropped`) means some live updates were lost.
   Keep the stream open and re-fetch the current state of your view with
   `GET /v1/records?order=id&bbox=…` (or reload tiles).
4. **`reset`** is followed by the server closing the stream. For `buffer_overflow` and
   `replay_cap`, reconnect from `resumeAfter` (or your last received id). For `resync_required`,
   the history you asked for is gone: take a new snapshot ("Keeping a mirror", step 1) and
   reconnect from its `watermarkSeq`.
5. `raster-frame` changes always carry their payload; other changes only with `payload=true`.

### Panning a map without reconnecting

On `/v1/events/stream`, change the area in place:

```
PATCH /v1/events/stream/{streamId}/filter
{"changes": {"bbox": "-97.5,32.5,-96.5,33.2", "live": true}}
```

- **Debounce** pans and zooms by 300–500 ms and send one PATCH, rather than one per frame.
- The body is validated whole before anything changes. Omitted topic blocks are unchanged; a
  present block replaces that topic's filter (omitted fields take their defaults). `topics` adds
  or removes topics; an added topic starts live from now.
- The stream emits `filter-applied {atSeq, filterVersion}`; every event after it uses the new
  filter. `atSeq` is the last change delivered under the old one.
- Nothing is replayed for the newly exposed area: load it with `GET /v1/records?bbox=…` (or tiles)
  and merge by `observedAt`.
- Only the caller who opened the stream may patch it; anyone else gets 404. Anonymous callers send
  the `streamKey` from `hello` as `Foresignal-Stream-Key`.

On `/v1/events/stream`, ids are composite (`c=1234;a=56;n=7`, one cursor per subscribed topic) and
`Last-Event-ID` resumes every topic at once. Browsers cannot set headers on the first `EventSource`
connect, so the same value is accepted as `?lastEventId=`; the header wins when both are present.

### Limits

At most 100 open streams per server (`503 STREAM_LIMIT`), 8 per token and 4 per anonymous address
(`429 STREAM_LIMIT` with `Retry-After: 5`). Each client may have 1,000 undelivered changes queued;
a client that cannot keep up gets `reset {reason: "buffer_overflow"}`. The operator can change these
in the `stream.limits` setting.

## Rules and alerts

A trigger rule watches persisted versions and records an alert when one matches, so a source no
client is displaying can still announce itself. Create rules with `POST /v1/triggers` and test them
first with `POST /v1/triggers/{id}/test` (a backtest that creates nothing; `simulate=true` applies
cooldown, active windows and the storm cap and reports `wouldFire` and `wouldSuppress`).

- **What a rule matches.** `sources` and `kinds` (arrays; empty means any), `conditions` on payload
  paths (`> >= < <= == != in notIn exists startsWith matches`, all ANDed), and a `geofence` (a
  polygon, or a line with `bufferMeters` for a route corridor) or `near {point, radiusKm}`. A value
  of the wrong JSON type or a missing path never matches: the number `5` does not equal the string
  `"5"`. Conditions on a source's volatile fields are refused (`400 TRIGGER_PATH_VOLATILE`), because
  those fields change on every fetch and are not persisted reliably.
- **Prefer `enter` or `exit`, or a cooldown, on sampled sources and derived records.** `match` fires
  on every persisted version that matches, keyframes included, so a vessel parked inside a fence
  fires again at every keyframe. `enter` fires when an entity starts matching (or reappears, with
  `reappearAsEnter`, the default for fenced rules) and `exit` when it stops. `hysteresisMeters` adds
  a band around a fence so GPS jitter along the edge fires nothing. A cooldown counts on the
  version's `observedAt`, per entity (`cooldownScope: record`) or per rule.
- **Resolution follows the data.** Foresignal persists every flip of a rule's predicate, even
  under SAMPLED policies, so edges are exact, but only to *flush or poll resolution*: an entity that
  crosses a fence and comes back between two polls or stream flushes is never seen outside.
- **Edges need the predecessor version.** An edge compares a version with the one before it. When
  retention has removed the predecessor, nothing fires except a first appearance or a reappearance.
  Expiry is not an exit.
- **A tombstone after an inside version is an exit.** Deleting an entity (or a place a derived
  record belongs to) can therefore fire `exit` rules.
- **`forecastHorizon` fires on new versions only.** It matches versions whose `validFrom` is within
  the horizon of the moment they are evaluated; time passing alone does not re-evaluate a rule.

Alerts have a lifecycle: `firing`, `acknowledged` (`POST /v1/alerts/{id}/ack`) and `resolved`
(`POST /v1/alerts/{id}/resolve`, or automatically on the inverse edge, a tombstone, expiry or
staleness with `autoResolve`, the default for `enter` rules). Resolved is final; the next enter edge
fires a new alert. `PUT /v1/alerts/{id}/feedback` records `useful`, `noise`, `late` or
`false_positive`. Snoozing an alert (`POST /v1/alerts/{id}/snooze`) or a rule
(`POST /v1/triggers/{id}/snooze`, at most 7 days) records new matches as suppressed instead of
routing them. A rule that fires more than `maxAlertsPerHour` (default 120) drops the excess and
counts it in `suppressedCount`.

`GET /v1/alerts/stream` (or `topics=alerts` on `/v1/events/stream`) pushes `alert`,
`alert.updated` and `alert.resolved` events with `id: <rev>`. A resume replays every alert whose
`rev` is newer than yours in its current state, so upsert by alert `id`. Filters: `ruleId`,
`minSeverity`, `source`, `kind` and `includeSuppressed`.

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
