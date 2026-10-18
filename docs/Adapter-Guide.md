# Foresignal — Adapter Guide

How to write, test and repair a source adapter. Every adapter prompt after 011 follows this guide, and the adapter-maintenance agent (prompt 140) uses its repair procedure. The worked example throughout is `usgs-earthquakes` (`ai.foresignal.source.adapters.usgs`), the reference adapter built in prompt 011.

Authorities: Architecture §4.2 (adapter rules 1–6), Core-Data-Model §2–§5 (rules 7–13, time contract, hashing, ingest policies), and `docs/Conventions.md`, which wins over this guide where they disagree.

## 1. Package layout

One package per upstream under `ai.foresignal.source.adapters`, with no imports between adapter packages. Shared format parsers (readsb JSON, CAP 1.2, GeoJSON, KISS/AX.25) live under `ai.foresignal.source.formats.*`; the AIVDM codec, which prompt 110's sink uses as well, is the platform package `ai.foresignal.codec.ais` (§24). Shared format code lives there, **not** under `adapters`, so any adapter may import it without breaking the no-cross-adapter rule: the readsb mapper and the aircraft descriptor defaults (`formats.readsb`, prompt 062) are used by `adsblol`, `opensky` and the local receivers (069), exactly as `formats.cap` is used by every CAP source. Code that only one adapter uses stays in that adapter's package.

```
source/adapters/usgs/
  UsgsEarthquakesAdapter.java   @Component, implements PollingAdapter: descriptor + fetch
  UsgsGeoJsonParser.java        pure static parser: byte[] -> List<NormalizedRecord>
  package-info.java
src/test/java/.../adapters/usgs/
  UsgsGeoJsonParserTest.java    parser contract (fixtures, FieldSpecContract, SnapshotContract)
  UsgsEarthquakesAdapterTest.java  WireMock: URL, conditional GET, errors, User-Agent, descriptor
  UsgsIngestIT.java             scheduler -> executor -> real writer, against WireMock
src/test/resources/fixtures/usgs-earthquakes/
  all_day.geojson  revised.geojson  updated-only.geojson  edge-cases.geojson
  not-a-feature-collection.json  README.md
```

**The parser is pure.** It takes the response bytes and returns records: no clock, no network, no state, no Spring. That is what lets it be tested against fixtures with no context, and what lets the maintenance agent diff it against a fresh capture. The adapter does only what needs a context: read params, call `ctx.http()`, map a 304, log the parse diagnostics.

## 2. Adapter rules

From Architecture §4.2:

1. Self-contained: one package per adapter (adapter, parser, DTOs). No cross-adapter imports.
2. Network access only through `ctx.http()`; never build an `HttpClient`. `UpstreamClientIsTheOnlyHttpClientTest` enforces this.
3. Parsers are pure functions.
4. Each adapter ships a fixture under `src/test/resources/fixtures/<source-id>/`, a parser contract test and a WireMock adapter test.
5. Adapters are Spring `@Component`s discovered by type; a duplicate id fails startup. One parser serving many feeds is a `SourceAdapterFamily` returning one adapter per feed from a static, code-reviewed list, never a list supplied by an API caller.
6. Code ported from God's Eye View carries an origin header comment and an entry in `THIRD_PARTY_NOTICES.md` (section 8).

From Core-Data-Model §3:

7. **The payload is a complete snapshot.** Protocols with split messages (AIS, MQTT) merge state per entity before emitting. Tested with `SnapshotContract.assertStableStaticHash`.
8. **Slim, allowlisted payloads with no nulls.** Leave out query-relative fields. Raw counters are left out or declared volatile. `NormalizedRecord.Builder.put` drops nulls for you. USGS emits 26 allowlisted keys and drops `tz`, which is always null.
9. **`validUntil` only for an upstream-asserted expiry.** Never a precomputed TTL; TRACK sources never set it. USGS sets none.
10. **`eventTime` only for discrete events and measurement periods.**
11. **Scrub sentinels** (AIS lat 91 / lon 181, SOG 102.3, heading 511) before they reach the payload.
12. **Quality transitions that matter go in significant fields** the adapter computes (for example `navDegraded`). `Quality` itself is never hashed.
13. **The time base is fixed per source.** Never switch an entity between timestamp fields from record to record (section 4).

## 3. Descriptor checklist

Build the descriptor once, in a static initializer, with `SourceDescriptor.builder(id)`. `DescriptorValidator` runs at registry start; `UsgsEarthquakesAdapterTest` also asserts it returns no problems.

| Field | How to choose it | USGS |
|---|---|---|
| `minInterval`, `defaultInterval` | From the upstream's published update cadence; never faster than it regenerates | 60 s / 60 s (feeds regenerate about once a minute) |
| `defaultBudget` | From the published rate limit; when there is none, well inside one request per update | 4 / min, 2,000 / day |
| `historyTier` | TRACK only for moving entities (Core-Data-Model §5.3); everything else STANDARD | STANDARD |
| `observedAtMode` | UPSTREAM when the feed carries a per-record observation time; RECEIVED otherwise, never mixed | UPSTREAM |
| `ttlAnchor` | OBSERVED for TRACK (required); RECEIVED is the STANDARD default | RECEIVED |
| `recordTtl` | How long a record stays current without being re-observed; null when records do not expire | null |
| `defaultIngestPolicy` | §5.3: ALL for everything but the position families; keyframe `ttl − poll − 30 s`, or none without a TTL | ALL, keyframe none |
| `volatileFields` | Keys that change on every fetch without meaning anything (generation stamps, counters) | `[updated]` |
| `significantFields` | Empty means every non-volatile key; required for SAMPLED and LATEST | `[]` |
| `motion`, `thresholds` | Position families only; never also significant | none |
| `defaultRetention` / `kindRetention` | By analytic class (Conventions, "Analytic retention") | 1825 d (`event`) |
| `licence`, `termsUrl`, `attribution` | From the upstream's terms page | `US-PD`, USGS copyrights page |
| `titleField` | The payload key that is the record's title | `title` |
| `searchFields`, `tileProperties`, `rankField` | Keep them significant, or the validator warns that they can go stale | `place, title`; `mag, place, depth, tsunami, alert, sig`; `mag` |
| `fieldSpecs` | Every payload key, with type, UCUM unit and label (section 6) | 26 keys |
| `kindDisplay` | Client hints per kind: title template, icon, `#RRGGBB` | `M {mag} - {place}`, `earthquake`, `#D9480F` |
| `entityKeys` | Identifier type to top-level payload field, for watchlists (prompt 036) and the entity registry (prompt 098). **Required for identity-bearing adapters:** declare every one of `icao24`, `registration`, `callsign`, `mmsi` and `imo` the payload carries (see §13) | none |
| `embedFields` | Only for text-bearing records (bulletins, posts) | none; quakes are found spatially |
| `private` | True for personal data; enforced on every egress path (prompt 039) | false |
| `zeroResultsNormal` | True when an empty response is ordinary (quiet hours, out of season) | true |
| `params` | Declared `ParamSpec`s; enumerate allowed values where the upstream has a fixed set | `feed`, 20 values, default `all_day` |

The `licence` field and the `fetch_log.licence` column keep their published spelling; they cross the wire and persist.

## 4. Time-base mapping

Core-Data-Model §2 is binding. `event_time` is when a discrete event happened (hashed, drives `effective_time`); `observed_at` is when the upstream last observed or updated the state (not hashed; drives LATE, TTL and touches); `received_at` and `ingested_at` are stamped by the framework.

| Adapter type | `eventTime` | `observedAt` |
|---|---|---|
| readsb snapshots | null | `now − seen`, always |
| OpenSky states | null | `last_contact`, always |
| Streaming (AIS, MQTT) | null | Message time of the newest merged message |
| GTFS-RT | null | Vehicle `timestamp`, else header `timestamp` (fixed per feed) |
| **Revised events (USGS, EMSC, GDACS)** | **Origin time** | **Upstream `updated`** |
| FIRMS | Acquisition time | Acquisition time |
| NWS | Onset if present | `sent` / `updated` |
| Catalog and reference | null | Descriptor picks UPSTREAM (publication date) or RECEIVED |
| Measurements (OpenAQ, NDBC, EIA) | Measurement period | Measurement time |

**Never switch timestamp fields per record.** USGS maps `eventTime = properties.time` and `observedAt = properties.updated` for every record; a feature without `updated` is skipped and counted, never given `time` instead. Mixing would make one entity's `observed_at` jump between two clocks, and the writer would drop the older one as LATE.

How the USGS mapping plays out in the writer: a revision changes `mag`, `status` and `updated`, so the static hash changes and the record is persisted as version 2 with `SIGNIFICANT`. A re-fetch where only `updated` moved hashes equal (`updated` is volatile) and becomes a touch. An unchanged re-poll is a `TOUCH_THROTTLED` inside `touchMin` (1 h without a TTL) and a `TOUCH` after it.

## 5. Altitude convention

**Geometry is always 2D.** Never put a Z coordinate in a geometry. Vertical quantities live in payload fields declared in `fieldSpecs` with a UCUM unit and a `datum`: `MSL`, `WGS84`, `AGL`, `PRESSURE`, or free text when none fits.

- New altitude and elevation fields use meters (`m`).
- An upstream field kept verbatim in its domain-standard unit declares that unit: USGS `depth` is `km` with datum "network-dependent reference, positive down".
- The aircraft sources (prompt 062) convert to meters instead, so readsb and OpenSky share one payload: `alt_baro` is `m` (feet × 0.3048, rounded to 1 m) with datum "pressure altitude (1013.25 hPa)", and `alt_geom` is `m` with datum "WGS84 ellipsoid".
- `DescriptorValidator` accepts a `datum` only on NUMBER or INTEGER fields in a UCUM length unit.

USGS reads `coordinates[2]` into the payload key `depth` and builds the geometry from `coordinates[0..1]` only. A feature with two coordinates simply has no `depth`.

## 6. The fieldSpecs contract

Every payload key an adapter emits must be declared in `fieldSpecs`, and its value must have the declared JSON type. `FieldSpecContract.assertPayloadKeysDeclared(descriptor, records)` (in `src/test/java/ai/foresignal/testsupport/adapters`) enforces both, respecting `FieldSpec.kinds`, and lists every violation at once. Call it on the records of every fixture.

Types: STRING, INTEGER (integral), NUMBER (any finite number), BOOLEAN, TIMESTAMP (an ISO-8601 string, or an integral epoch count in the declared unit, `ms` or `s`), OBJECT, ARRAY. Coerce in the parser so equal values hash equally: USGS turns a NUMBER sent as `2` into `2.0`, and keeps a value of an unexpected JSON type verbatim so the contract test reports the format change rather than the parser hiding it.

## 7. Etiquette

- Use `ctx.http().getConditional(uri, stateKey)` wherever the upstream supports validators, and return `FetchResult.unchanged()` on a 304. USGS sends no `ETag`, only `Last-Modified`; `If-Modified-Since` returns 304 (verified 2026-10).
- No concurrent requests unless the upstream allows them.
- Honor `Retry-After`; `UpstreamClient` turns 429 and 503 into backoff.
- Foresignal's identifiable User-Agent is set by `UpstreamClient`; never override it to look like a browser. `userAgentOverride` exists only for an upstream that demands a specific format.
- Official APIs only. No scraping. Fringe sources belong in external EDP providers (Conventions, "Operator choice").
- Return `FetchResult.complete(...)` when the response is the full current set. That is informational: absent records are never auto-tombstoned. Emit explicit tombstones where the upstream says something was deleted (USGS `status: deleted`).

### Tombstone only from a complete, successful response (prompt 047)

An adapter whose upstream returns the whole current set may tombstone what disappeared from it, but **only from a response that arrived in full and parsed without error**. A feed outage, a 5xx, a body cut at `maxResponseBytes`, an unparseable document or a file older than the last one processed must never reach the tombstoning step, because stale data must never read as an all-clear (Weather-Imagery-Design §7.1, "unknown is not clear"). The pattern:

1. Fetch, check `response.truncated()` and parse. Anything wrong throws (`UpstreamFormatException`, `UpstreamHttpException`) **before** step 2, so the run fails, health shows it, and nothing is tombstoned.
2. Call `LiveIdSet.absentSince(ctx.state(), scope, currentIds)` (in `source.spi`). It returns the ids live in the previous complete snapshot of that scope and absent now, remembers the new set, and reports a just-gone id once more on the next run in case that run's ingest failed (a second tombstone of a tombstoned head is skipped by the decision table).
3. Emit `NormalizedRecord.tombstone(id, kind, observedAt)` for each.

The scope keeps independent snapshots apart. Examples:

| Source | Complete set | Scope | Notes |
|---|---|---|---|
| `nws-alerts` | the whole `/alerts/active` response | the `area` selection | an alert superseded by an `Update`/`Cancel` (`references[].identifier`) is dropped from the set, so it is tombstoned at the superseding message's `sent` |
| `mrms-probsevere` | the newest **successfully processed** file, within the places AOI | `aoi` | an object that leaves the AOI is tombstoned too; a file older than the last processed one is ignored |
| `spc-outlooks` | each layer file fetched with 200 | one per day and type | a 304 changes nothing; a 404 degrades health (`AdapterStateStore.degrade`) and tombstones nothing |
| `awc-metars` | (none) | | station reports retire through the 3-hour TTL instead; nothing is tombstoned |

Two related rules:
- When a 200 body from `getConditional` could not be used (truncated or unparseable) and the fetch still succeeds, call `SourceHttp.forgetValidators(ctx.state(), stateKey)`: the validators were recorded when the response arrived, and without this the next run would get a 304 and never see the content. A fetch that throws needs no call: its staged validators are discarded (below).
- A probe never moves adapter state: during a probe `ctx.state()` is an in-memory overlay and `getConditional` stores no validators, so a probe cannot consume a live set or a cursor.

### Adapter state commits with the fetch (2026-10 review)

During a polling fetch run by the collector (`FetchExecutor`), `ctx.state()` is a `StagedAdapterStateStore`: every `put` and `remove`, and the `ETag`/`Last-Modified` that `getConditional` records, are held in memory and become durable only when the fetch's records have been written by the ingest writer.

| Outcome | Staged state |
|---|---|
| Records ingested (every chunk and the final batch) | committed in one statement, then reconciliation runs |
| 304, or an empty result | committed |
| The adapter throws (any exception, a budget refusal or a timeout included) | discarded |
| Ingest queue full, the writer failed the batch, or the writer did not finish in time | discarded |
| Scheduled run of a source disabled in flight (DISCARDED) | discarded |

Reads within the fetch see its own staged writes. After the commit or discard the store is closed, so a late write from an adapter thread that outlived its timeout is dropped. Consequences for adapters:

- A cursor, watermark, live set or validator never moves past records that were not written. A failed fetch re-reads the same window next run, so the at-least-once path (dedup in the writer) applies, never a gap.
- Keys under `health.` (`degrade`, `fail`, `recordInput`) are written at once, so a reason recorded right before a failure still reaches health.
- `AdapterStateStore.putDurably(key, value)` writes at once for bookkeeping that describes what was spent rather than what was ingested: `opensky-states` uses it for the remaining credit balance, `digitraffic-rail` for its daily lookup count. Never use it for a cursor.
- Chunks emitted with `ctx.emitChunk` are written as they go; if the fetch then fails, the chunks stay written and the state is discarded, so the next run re-reads (and re-touches) them.
- Probes keep their in-memory overlay. **Streams are not staged**: a stream connection has no end-of-fetch to commit at, so a streaming adapter's `ctx.state()` writes through at once. Persist a stream cursor periodically (by event count or elapsed time, and on close), never per event (§16).
- A test that calls `adapter.fetch(ctx)` directly with `AdapterHttpHarness` gets the persistent store, which writes through; the staging is the executor's, and `FetchExecutorIT` covers it.

## 8. Fixtures and tests

**Fixtures.** Capture a real response once with an identifiable User-Agent (for USGS: `curl -s -A "Foresignal-fixture-capture (+https://foresignal.ai)" <url>`), trim it to a reviewable size, and write a sibling `README.md` with the capture date, URL, command, notable response headers and the trimming method. Add hand-made fixtures for edge cases and for the transitions the writer must handle (a revision, a volatile-only change), and one for a format change. Keyed sources without a key build fixtures from published examples and mark them `synthetic: true` with "recapture with a real key" (Conventions).

**Required tests.** No test calls a real upstream.

- **Parser contract** (plain JUnit): the real fixture parses to the expected count with spot-checked fields; the time base; 2D geometry with SRID 4326; edge cases skipped with counts; a format change throws `UpstreamFormatException`; `FieldSpecContract` on every fixture; `SnapshotContract.assertStableStaticHash` on two snapshots that differ only in volatile content.
- **Adapter test** (WireMock): the URL per parameter, the conditional GET round trip (200 with a validator, then a 304), an error status → `UpstreamHttpException`, the User-Agent, and `DescriptorValidator.problems(...)` empty.
- **Ingest IT** (`@IntegrationTest` + WireMock + `MutableClock`): real fetches through the scheduler, `FetchExecutor` and the real writer, asserting versions and persist reasons, `fetch_log` rows, health and `record_live`. Never seed `record_head`, `record_live` or `record_version` with raw SQL (Core-Data-Model §17). ITs that need the scheduler running add `@TestPropertySource(properties = "foresignal.collector.scheduled=true")`.

**Test base URL.** An adapter's upstream base URL is a constant, overridable only by a test property named `foresignal.test.adapters.<adapter>.base-url`, read with `@Value("${...:}")` in the adapter's single constructor (blank means the production URL). Unit tests pass the WireMock URL to the constructor; ITs register it with `@DynamicPropertySource`. It is never an operator setting: the feed list is code-reviewed, which is what keeps adapters from becoming an SSRF vector. USGS uses `foresignal.test.adapters.usgs.base-url`.

### Raster fixtures (prompt 046)

Product sources (`mrms`, `goes-east-abi`) decode binary files, which cannot be cropped without
re-encoding, so their fixtures are whole real files.

- **Capture** with `scripts/capture-raster-fixtures.sh [output-dir]`: it lists today's prefix of each
  product anonymously over HTTPS (`https://{bucket}.s3.amazonaws.com/?list-type=2&prefix=…`), takes the
  newest key (PrecipRate and PrecipFlag at the same timestamp) and downloads it unchanged with
  `curl`, printing sizes and SHA-256s. With the AWS CLI, `aws s3 cp --no-sign-request
  s3://<bucket>/<key> .` fetches the same objects.
- **Commit** files under 3 MB in `src/test/resources/fixtures/mrms/` or `/goes/`, with a README
  listing key, size, SHA-256 and what the file holds (for MRMS: the sentinels and their counts).
  Include a no-coverage-heavy sample (the Hawaii MESH file is 41 % no coverage).
- **Pin** larger files instead: add an entry (path, URL, SHA-256, size) to `pinnedFixtures` in
  `build.gradle.kts`. `./gradlew downloadTestFixtures` fetches them into `build/test-fixtures/` and
  verifies them; tests read them through `DecoderFixtures` and skip with a logged reason when the file
  is absent. The GOES band 13 file (3.8 MB) is pinned this way.
- **Self-test fixtures** (`src/main/resources/raster/selftest/`, each under 200 KB) are single GRIB2
  messages cut by byte range from real files using the `.idx` (NCEP) or `.index` (ECMWF) offsets, and
  one small real NetCDF4 file; their provenance is in that directory's README.

Product adapter tests use `ProductAdapterHarness` (a hand-built `FetchContext` over the real
`UpstreamClient` pointed at WireMock with `setS3Endpoint`, plus a recording `RasterSink`), and the
ingest ITs (`ProductIngestSupport`) run the adapter through the real raster pipeline in 042's shared
raster context and render a tile. The object base URLs follow the test base URL rule:
`foresignal.test.adapters.mrms.base-url` and `foresignal.test.adapters.goes.base-url` (path-style,
`{base}/{bucket}/{key}`); bucket listings follow `foresignal.test.s3-endpoint`.

## 9. Porting, families and other modes

- **God's Eye View** (`~/Documents/GitLab/gods-eye-view`, MIT) may be ported to Java. Add a header comment naming the origin file and the MIT notice, and list it in `THIRD_PARTY_NOTICES.md`. The USGS adapter was written independently and ports nothing (GEV's earthquake layer filters to M2.5+ and rejects whole snapshots, which Foresignal does not want).
- **ShadowBroker, Velocity, World Monitor (AGPL) and SatDump (GPL)** are reference material only. Read them for endpoints and quirks; never copy code.
- **Adapter families:** one `SourceAdapterFamily` bean, one source per feed, each with its own settings, budget and health.
  - **Reviewed lists** (`TransitFeeds`, 070; `GbfsSystems`, 071) are validated at startup, and an invalid list stops
    Foresignal. Derive any host allowlist from the list itself and check every URL's scheme, host and path there, as
    `GbfsSystems.problems` does. Never discover URLs at runtime (GBFS `gbfs.json`, for example): discovery lets
    upstream data choose what Foresignal requests.
  - **Test rebasing:** a family takes a `foresignal.test.adapters.{family}.base-url` property that moves every URL to
    a WireMock base. When feeds share a path on different hosts (every `*.publicbikesystem.net` GBFS system), keep the
    host as the first path segment (`GbfsSystem.rebased`).
  - **Redirects:** a family whose upstream must not redirect sets `followRedirects(false)`. The 3xx then reaches the
    adapter as a response, and the adapter fails the run with an `UpstreamFormatException` naming the host (GBFS:
    `redirect refused: {host}`). That is a failure, not a backoff.
  - **Catalog additions** come only from a vendored, reviewed resource plus a bootstrap property and a restart
    (070's Mobility Database resolver, `MobilityCatalog`). GBFS systems are added only in code today. A resolver like
    070's could follow over MobilityData's `systems.csv` (CC BY 3.0, so it needs attribution in
    `THIRD_PARTY_NOTICES.md` and in each source's descriptor). It would still have to apply the GBFS path rule and
    refuse redirects.
- **Streaming adapters** (the first is `aisstream`, prompt 063) emit through the `RecordSink` they are given; the framework's `PolicyAwareRecordSink` batches, keeps latest-wins for SAMPLED and LATEST, and writes one `fetch_log` row per flush. See §16 for the full pattern.
- **PUSH sources** (custom sources and inbound webhooks, prompt 024) have no fetch; records arrive through the ingest API.
- **DERIVED sources** make no upstream requests; `ctx.http()` throws for them. See §14.

## 10. Repair procedure

For the maintenance agent (prompt 140) or a person, when an adapter starts failing or its diagnostics show skipped features:

1. **Run the adapter doctor.** `POST /v1/sources/{sourceId}/doctor` (or the MCP tool `diagnose_source`) probes the upstream once, compares the response's structure with the recorded fixture and names what changed (see "Adapter doctor" below). Health often has a report already: `lastDoctorReport` on `GET /v1/sources/{sourceId}/health`, written by an automatic run. For the raw response itself, run the probe (`ProbeService`, `POST /v1/sources/{sourceId}/probe`), which captures it without ingesting it.
2. **Diff against the fixture.** The doctor's structure diff lists added, removed, renamed and retyped paths, emptied arrays, and content-type or delimiter changes. Compare the probe body with the recorded fixture for anything it cannot see: units, timestamps, headers (validators, rate limits). The `UpstreamFormatException` snippet usually names the change.
3. **Update the parser** for the new shape, keeping the time base fixed (rule 13) and the payload allowlisted (rule 8). Update `fieldSpecs` for any new key. A patch the doctor proposed is a starting point to review, never a finished fix.
4. **Refresh the fixture and README.** Recapture with the identifiable User-Agent, trim it the same way, rebuild the derived fixtures, and record the new capture date and what changed.
5. **Run the contract test** (`FieldSpecContract`, `SnapshotContract`, the parser test) and the adapter test, then record the change in the Completion Report.

### Adapter doctor (prompt 140)

The doctor (`ai.foresignal.doctor`) answers "what changed upstream, and how could the parser be fixed?". It probes the live upstream through the normal budgeted client, extracts the structure of the probe body and of the adapter's fixture, diffs the two, and runs deterministic rules. Then, only when enabled and a chat provider is assigned, it asks the LLM to explain the change and propose a parser patch as text. **Everything except the explanation works without an LLM. A patch is never applied.**

**How fixtures feed it.** The Gradle task `packageDoctorResources` (`gradle/doctor-resources.gradle.kts`, wired into `processResources`) copies `src/test/resources/fixtures/**` into the jar under `META-INF/foresignal/fixtures/{sourceId}/`:

- Each file is capped at 256 KB. A larger JSON file has its largest arrays halved until it fits, so it stays a valid document. A larger CSV or TSV file keeps its header and the first 500 rows (fewer if still too large), and a JSON Lines file keeps whole lines. Any other file over the cap is skipped with a log line and listed under `skipped` in `fixtures-index.json`. Large rasters (GRIB2, NetCDF, compressed archives) are therefore usually not packaged.
- `fixtures-index.json` lists each source's packaged files with their size, original size, SHA-256 and whether they were truncated. It also records the capture date, taken from the first line of the fixture `README.md` that mentions the date or the capture and contains a `yyyy-MM-dd` date, and whether the README says `synthetic: true`.
- **The fixture compared** is the source's primary data file. READMEs and helper files are left out, and so is any file whose name marks an edge case or a deliberate break (`edge`, `error`, `not-a`, `invalid`, `malformed`, `empty`, `broken`, `bad`, `e2e`, `revised`, `updated`, `changed`, `format-change`, `truncated`, `garbage`, `html`, or a status such as `404`). Among the rest it prefers a file whose extension fits the probe's format, then the largest. For USGS this is `all_day.geojson`.

So **keep the main capture current**, write the capture date into the README, and give hand-made edge cases names that say so. A stale primary fixture makes every later upstream change look like a fresh break. A fixture with an unmarked name can be picked as the reference.

**Adapter source.** The same task copies `src/main/java/ai/foresignal/source/adapters/**/*.java` into `META-INF/foresignal/adapter-src/`, with `adapter-src-index.json` (adapter package → files). A source id resolves to its files by the package-per-adapter convention: the package of the source's adapter class. Two kinds of file are left out and listed as `excluded`: files ported from third-party code (an origin header naming the project they came from, see §9) and anything resembling key material. Their adapters get diagnosed without parser source. Only Foresignal's own MIT code ships.

**"Paths the parser reads"** is a heuristic: a static scan of the adapter source for string literals (split on `.`, `/` and `@`, so `"/properties/mag"` counts for `mag`), matched against the last name of each changed path. Findings that rely on it carry `heuristic: true` and say so in their evidence. A parser that builds field names at run time defeats it, so declare field names as literals.

**Structure extraction.** JSON becomes a path tree with `[]` for arrays (`features[].properties.mag`), with types, presence ratios, up to 3 examples of at most 80 characters, and array lengths. CSV and TSV give the delimiter, the header columns (or `colN`), and each column's inferred type and null ratio. XML is parsed with DTDs and external entities refused and gives element and `@attribute` paths. Binary bodies give the magic bytes, a kind (zip, gzip, png, grib2, netcdf, …) and ZIP entry names. An HTML page where data was expected is recognized and its `<title>` kept. The diff pairs a removed path with an added path under the same parent as a rename candidate, when their types are compatible and either their names are similar (snake↔camel equal, or normalized Levenshtein ≥ 0.6) or their value distributions overlap. Each candidate gets a score.

**Reading findings.** Each finding has a code, a severity (`info`, `warning`, `critical`), evidence, an optional path and a suggested action. The report's status is `BROKEN` with any critical finding, `DEGRADED_INFO` with any warning, and `OK` otherwise.

| Code | Meaning | What to do |
|---|---|---|
| `AUTH_FAILED` | 401 or 403 without a data body | Check the credentials (`PUT /v1/sources/{id}/credentials`) and whether the key expired or lost a permission |
| `UA_BLOCKED` | 451, or 403 with a block page | Contact the upstream or join its key or feeder program. **Never spoof the User-Agent** (§7) |
| `ENDPOINT_MOVED` | 404 or 410, or a 301/308 to another host | Find the current URL in the upstream's documentation, update the adapter, refresh the fixture |
| `RATE_LIMITED` | 429, or a backoff in force | Raise the interval or lower the budget; the evidence compares the budget with observed usage |
| `UPSTREAM_ERROR` | The probe answered 5xx, or 3 or more consecutive fetches did | Wait and watch the upstream's status; the adapter needs no change yet |
| `HTML_INSTEAD_OF_DATA` | A login wall, maintenance page, captcha or other HTML page | Open it in a browser, wait out maintenance, or move to the official API. **Foresignal never solves captchas** |
| `EMPTY_RESULT` | Structure intact, zero items | Check params and the upstream; declare `zeroResultsNormal` if empty is legitimate (then the finding is info) |
| `FIELD_RENAMED` | A rename candidate scoring ≥ 0.7 on a path the parser reads | Read the new name in the parser, refresh the fixture, re-run the contract test |
| `FIELD_REMOVED` | A removed path the parser reads, not explained by a rename | Find the replacement in the upstream's changelog, or tolerate the absence |
| `TYPE_CHANGED` | A path's types no longer overlap (critical when the parser reads it) | Accept the new type in the parser |
| `FORMAT_CHANGED` | Content type, delimiter or overall format changed, or columns reordered | Check whether the old format is still offered; otherwise adapt the parser |
| `PARSER_REJECTS` | The parser threw `UpstreamFormatException` on the probe; the snippet is included | Follow the snippet, adjust the parser, refresh the fixture |
| `COVERAGE_DROPPED` | Coverage fell to half the median of the recent reports (Core-Data-Model §14) | Check the receivers or stations and the area params before suspecting the parser |
| `NO_CHANGE_DETECTED` | No other finding: the probe matched the fixture and parsed | Read the fetch history and the budget if health still complains |
| `PROBE_SKIPPED_BUDGET` | No probe: the budget was spent or a backoff was in force | Diagnosed from health and history only; run again later |

**Running it.**

- `POST /v1/sources/{sourceId}/doctor`: admin, always authenticated (it can spend LLM budget). The body is `DoctorOptions {probe (default true), llm: auto|on|off (default auto), detail: compact|full (default compact)}`. The run is synchronous up to 90 s and returns the report; a longer run answers `202` with `Location: /v1/doctor/reports/{reportId}`, and that URL answers `202` until the report exists.
- One run per source per 10 minutes, never two at once: `409 DOCTOR_BUSY`. A probe still spends the source's request budget, manual runs included.
- `GET /v1/sources/{sourceId}/doctor/reports` (agent, newest first, `cursor` and `limit`) and `GET /v1/doctor/reports/{reportId}` (agent; `404 UNKNOWN_DOCTOR_REPORT`).
- Source health carries `lastDoctorReport` (`id`, `status`, top finding, `createdAt`).
- **Automatic runs:** when a source turns `degraded` or `failing` with a format-change, coverage-dropped or parse-error reason, the doctor runs with `trigger=AUTO`, a probe, and `llm=off`. The LLM runs automatically only when the operator sets `doctor.autoLlm` to true (NFR-13). Automatic runs happen at most once per source per `doctor.autoCooldown` (default 6 h). A `BROKEN` automatic report publishes a `system` notification, subtype `system.doctor_broken`: "{source}: {top finding} — {suggested action}", linking the report.
- Reports are kept 90 days. **No raw upstream body is stored.** The body is held in memory (at most 1 MB) for the run only. A report keeps the findings (a `PARSER_REJECTS` snippet included), the structure diff (paths, types and capped examples of changed paths), the probe summary (status, content type, size, redacted URL, duration), the fixture compared, and the LLM output.

**The LLM step.** It uses the 026 `chat` role, purpose `doctor`, at low effort; it is budgeted and metered by the provider's governed client. It works with local Ollama and OpenAI-compatible models. Compact context stays within about 6k tokens: the descriptor summary, the findings, the diff, up to 2 KB of probe excerpt around the first divergent path, and the parser files, trimmed to their parse methods when over 400 lines. Full context allows about 30k tokens and 8 KB of excerpt. Upstream-derived content sits inside a fixed-delimiter untrusted-data block (the delimiters are stripped from the content first). No tools are offered, and the answer is parsed as data. When the provider may not receive the source's data under `EgressPolicy`, the excerpt and examples are withheld. A failure, a 60 s timeout, a budget refusal or no provider leaves the deterministic report intact, with `llm.status` (`off`, `no-provider`, `budget-refused`, `timeout`, `failed`) saying why.

**Applying a proposed patch by hand.** The patch in `llm.patch` is labeled **"proposed, not applied"** everywhere. `patchApplies` means the diff applied cleanly, in memory, to the packaged copy of that file (every context line matched). `sanityChecks.bracesBalanced` and `sanityChecks.classNameUnchanged` are text checks on the patched copy. Nothing was compiled, loaded or written, and a patch for a file outside the adapter source catalog is refused. To use one:

1. Read the diff first: the model may be wrong. Save `unifiedDiff` to a file. The report's `file` is relative to `src/main/java/ai/foresignal/source/adapters/`; if the diff has no `---`/`+++` headers, add `--- a/{file}` and `+++ b/{file}`. Then apply it in a checkout with `git apply --directory=src/main/java/ai/foresignal/source/adapters patch.diff` from the repository root, or `patch -p1 < patch.diff` inside that folder.
2. Recapture the fixture and update its README (step 4 above), so the next doctor run compares against the new shape.
3. Update the parser contract test and the `fieldSpecs`, compile with `./gradlew compileJava compileTestJava`, and run the adapter's tests.
4. Open a merge request for human review.

The CLI command `foresignal sources doctor <id>` arrives with prompt 141.

## 11. Map tiles: `aggBelowZoom` and tile properties

Vector tiles (prompt 016, `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt`) show current live state, one feature per record, with the base properties `source`, `externalId`, `kind`, `title`, `observedAt`, `version` and `live`, plus each source's `tileProperties` read from the merged payload (`record_head.payload || record_live.kinematic`), so a tile shows live values before a new version is persisted.

**Declare `aggBelowZoom` for dense point layers**: aircraft, vessels, fire pixels, lightning, PSKReporter spots, anything that puts thousands of points in a low-zoom tile. Below that zoom, `agg=auto` renders hexagonal aggregate cells (`count`, `latestObservedAt`, `kinds`, `agg`, and `maxRank` when every source of the layer declares a numeric `rankField`) instead of capping the tile at `foresignal.tiles.maxFeatures` newest features. Pick the zoom where a tile still holds a readable number of points; 6 to 8 suits global aircraft and fire layers. Leave it null for sparse layers (earthquakes, alerts, outages), which never aggregate. A kind layer uses the lowest value its sources declare.

**Choose tile properties for what a style needs**, nothing more, because every property is sent for every feature:

- `track` (degrees) for icon rotation of aircraft and vessels;
- `frp` (fire radiative power, MW) for fire symbol size, and declare it as `rankField` so aggregate cells carry `maxRank`;
- `mag` for earthquake size and color;
- a severity or status field for alert fill color.

Property names must match `^[A-Za-z0-9_]{1,64}$`; any other name is left out of tiles with a warning, because names are interpolated into SQL. Declare every tile property in `fieldSpecs` with a label and unit: the TileJSON `vector_layers[].fields` descriptions (`GET /v1/tiles/{layer}.json`) come from them.

## 12. Operator-supplied URLs: the outbound URL policy

Adapter base URLs are constants (section 8), so they never pass through here. **Any URL an operator or API caller supplies does**: a `LAN_URL` parameter, a generic feed or dataset-by-URL, a kit or template that names an endpoint, a webhook or sink target, an EDP provider URL. Every such URL goes through `ai.foresignal.net.OutboundUrlPolicy` (prompt 035), the one SSRF guard; never write a private address check of your own.

- **Pick a profile.** `PUBLIC_ONLY` for anything that should only reach the internet (https only); `LAN_ALLOWED` for destinations that may be on the operator's network (public plus loopback, RFC 1918, unique-local and shared 100.64/10; http allowed); `LAN_ONLY` for receivers that must be local (what `ParamType.LAN_URL` uses); `PUSH_ENDPOINT` is Web Push's. Link-local (169.254/16, fe80::/10), cloud metadata (169.254.169.254, fd00:ec2::254, 100.100.100.200), multicast, broadcast and the other not-forwardable special-purpose blocks are refused in every profile, and no override unblocks them.
- **Validate on save.** Call `saveProblem(url, profile, allowHttp)` (or `requireSavable(field, …)`) where the URL is accepted, and answer 400 `URL_NOT_ALLOWED` on its field (`OutboundUrlPolicy.notAllowed(errors)`). A host that does not resolve at save time is accepted; it is checked again on use.
- **Check on every send, and connect to the checked address.** `check(uri, profile)` resolves once and returns `Allowed(pinnedAddresses)` or `Denied(reason)`. Connect to `pinned()` with the original `Host` and TLS name and redirects off: `PinnedHttp` does this for `java.net.http`, and a `LAN_URL` is pinned by `UpstreamClient` already. A refusal at send time is a delivery or fetch failure (`OutboundUrlDeniedException`), never a silent fallback to the unchecked URL.
- **Redirects.** Do not follow them. A caller that must follow them re-checks every hop with `checkRedirect(current, location, profile, allowHttp, hop)`, at most `MAX_REDIRECTS` (3).
- **Plain http outside the LAN profiles** needs a per-field allowance (`check(uri, profile, true)`), documented on that field in the spec; the default is https only.
- **Never echo the path or query** of a refused URL: webhook paths and queries carry tokens. The policy's reasons name only the host and address.
- **Tests** use a `HostResolver` stand-in (`MutableResolver`, or `TestOutbound.offlineResolver()`), never real DNS, and point at WireMock on 127.0.0.1 through a LAN profile or an `outbound.allowHosts` / `allowCidrs` override.

Admins can adjust the policy with `GET/PUT /v1/settings/outbound` (`allowHosts`, `allowCidrs`, `denyHosts`, `denyCidrs`; deny wins).

## 13. Identity fields: `entityKeys` and watchlists (036)

Watchlists match entities by identity (`icao24`, `registration`, `callsign`, `mmsi`, `imo`). A rule or
view condition `{op: "inWatchlist", value: "<listId>"}` without a `path` reads, for each source, the
top-level payload field its descriptor declares under that identity in `entityKeys`
(`IdentityResolver`). A source that declares nothing for the list's identity **never matches**, and
rule validation lists it in a warning.

- Every adapter whose records carry one of these identifiers declares it, for example
  `.entityKey("icao24", "hex").entityKey("registration", "reg").entityKey("callsign", "flight")` for a
  readsb-family source, or `.entityKey("mmsi", "mmsi").entityKey("imo", "imo")` for AIS.
- The field must be top-level and hold a JSON string or number; values are compared after the list's
  normalization (upper case for aircraft identifiers, digits only for MMSI and IMO), so the adapter
  does not need to normalize them itself.
- Adapters existing at prompt 036 (USGS earthquakes and the system canary) carry no such identifier,
  so none needed a change.

**The entity registry (098) reuses `entityKeys` and adds two descriptor fields.** The registry resolves
records to canonical entities deterministically; it recognises eight key types: `airframe`, `icao24`,
`registration`, `imo`, `mmsi`, `norad`, `cospar` and `callsign`. Beyond the five identity fields above,
an aircraft-registration source declares `airframe` (the payload must also carry `mfrMdlCode` and
`serial`), a space catalogue declares `norad` and `cospar`, and a callsign-carrying source declares the
domain with `.entityKeyPrefix("callsign", "air" | "sea" | "ham")` (a bare callsign defaults to `air`
and is rejected on its own). Copy payload values onto the entity with
`.entityAttribute("payloadField", "attributeName")` (for example manufacturer and model); only
`LOCAL_ONLY`/private sources are excluded unless the operator opts in, and no source may declare an
owner name or address.

## 14. Derived sources (052)

A derived source computes records from data Foresignal already holds: committed changes, raster frames,
places. The runtime is `ai.foresignal.derive` (prompt 099's intelligence sources reuse it); the weather
generators (`wx-places`, `wx-areas`, `wx-cells`) live in `ai.foresignal.source.adapters.wx`.

**The SPI.** Implement `DerivedGenerator` (a `SourceAdapter`) as a Spring bean whose descriptor declares
`Mode.DERIVED`, **no `recordTtl`** (the registry refuses one) and `zeroResultsNormal(true)` (most invocations
change nothing):

- `DerivedInputs inputs(ValidatedParams)`: the change subscription (`sources` and/or `kinds`), the raster
  products whose frame events you want, an optional `tick`, and `places` when place edits should reconcile.
- `onChanges(ChangeBatch, ctx)`, `onFrame(RasterFrameEvent, ctx)`, `onTick(Instant, ctx)`: each optional.
- `reconcile(List<HeadSnapshot> ownHeads, ctx)`: required; called at startup, when the source is enabled,
  after a change-bus gap or an inbox overflow, and after a place edit when `places` is set.

The `DerivedContext` gives `emit(record)`, `tombstone(kind, externalId)`, `state()` (the adapter state
store), `rasters()` (read-only frames, sampling and the GLM ring), `records()` (`record_head ⋈ record_live`
only), `places()`, `sourceStatus(id)` and `clock()`. Generators never write the store and never fetch.

**The runner.** One virtual thread per enabled derived source, hooks called one at a time, so a generator may
keep plain mutable state. Change batches queue up to `foresignal.derive.inbox-capacity` (64); on overflow
they are dropped and a reconcile is scheduled. Frame events are coalesced per product (only the newest
pending one is delivered), ticks are never queued twice. Each invocation is treated like a fetch:
`recordAttempt`, one `fetch_log` row with `principal: derived`, one ingest batch of everything the
invocation emitted, and `recordSuccess` or `recordFailure`. A generator that throws fails the invocation like
a failed fetch.

**Rules every generator keeps** (Weather-Imagery-Design §7.1):

1. **Emit on change only.** Quantize the payload (distances, rates, areas) so a steady state is byte-identical;
   the writer's content hash drops an identical version, and the runtime already skips an emit equal to the
   entity's previous one. Never put frame ids, timestamps or ages in the payload.
2. **`observedAt` is the input's time**: the frame's valid time, the change batch's ingest time, or the tick.
   The runtime raises it to the entity's previous emit when it would go backwards, so a staleness change seen
   on a tick never turns the next frame LATE.
3. **Tombstone explicitly, only when the object disappeared or its subject was deleted** (a storm cell absent
   from a processed frame, a deleted place). **Never tombstone because input is stale**, and never write
   heartbeat versions. Remember that a tombstone after a matching version is an `exit` for rules.
4. **Unknown is not clear.** A value whose input is stale, failing or uncovered keeps its previous value with a
   non-ok `status` and an `asOf`; before any good data, carry the status only (omit the metric keys, never
   null). Use a numeric sentinel (`999`) for "none within range".
5. **Reconcile** rebuilds in-memory state from your own heads (and recent frames, for rolling windows), and
   tombstones heads whose subject no longer exists.

**No cycles.** At startup `DerivedGraph` builds the graph "A's records feed B's change subscription" from each
generator's default inputs; a cycle (including a generator subscribed to its own kinds) fails startup.

## Country-level records (prompt 040)

A record that only knows its country (an outage, a travel advisory, a refugee figure, a botnet
controller's country) uses the bundled `CountryCentroids` (`ai.foresignal.reference`) for its
geometry instead of shipping its own centroid table: `byIso2`, `byIso3`, `byFips` (travel advisories
use FIPS 10-4) and `byName` (case-, accent- and punctuation-insensitive, aliases such as "Türkiye",
"Czech Republic" and "Ivory Coast" included) return a `CountryRef` whose `labelLon`/`labelLat` is
Natural Earth's label point, which always falls inside the country. It works with no network and no
database rows. Put the label point in the record's geometry and set `geometryApproximate: true` in
the payload, so clients and rules know it is not a measured position. `ioda-outages` (068) uses it; 076
(abuse.ch) and 090 (UNHCR, advisories) will too. A record that knows its first-level region by name uses
`Admin1Centroids.byName(iso2, name)` (068): the Natural Earth admin-1 label point from 040's `ref_admin1`, matched
case-, accent- and punctuation-insensitively, cached per country and dropped on a reference reload. It reads the
database, never the network, and misses until the reference datasets are loaded; treat a miss as "no geometry".

## 15. Aircraft sources and the shared readsb mapper (prompt 062)

The global aircraft sources are the first TRACK sources on the v1.2 pipeline. Reuse their pieces rather than
re-deriving them (prompts 069 and 080):

- **`ReadsbDocument.parse(bytes)`** reads the top level of an adsb.lol (`ac`) or local readsb (`aircraft`) response;
  `now` is seconds, or milliseconds when above 1e11 (adsb.lol sends milliseconds).
- **`ReadsbAircraftMapper.map(entry, now, MapperOptions)`** turns one entry into one record: `externalId` = `hex`
  lower-cased with any leading `~`, `observedAt = now − seen` always, geometry from `lat`/`lon`, else `lastPosition`
  (flagged in `quality.extra`), else null (TRACK carry-forward). The payload is the allowlist of `AircraftFields` in
  meters, knots, degrees and m/s, with `posSource`, `navDegraded` and the `dbFlags` bits; counters such as `messages`,
  `rssi` and `seen` are left out.
- **`AircraftDescriptors.global(builder[, ttl])`** applies TRACK, `ttlAnchor` OBSERVED, `observedAtMode` UPSTREAM, the
  §5.3 SAMPLED policy, significant fields, thresholds (`alt_baro` 300 m, `track` 20°), motion, tiles, search, title,
  entity keys, `aggBelowZoom` 7 and the `fieldSpecs` of every key. The default TTL is 6 minutes (Conventions);
  `opensky-states` passes 15.
- An upstream that answers **451**, or **403** with `accessDeniedIsTerminal(true)` on the descriptor, is a refusal of the
  client: `UpstreamClient` sets a one-hour backoff and throws `UpstreamAccessDeniedException`, and health shows
  `failing`. `defaultRetryAfter(Duration)` sets the backoff after a 429 without `Retry-After`. Both are internal
  descriptor fields, not in the OpenAPI.
- An adapter that checks an upstream's own quota before a request throws **`UpstreamQuotaDeferredException(retryAt)`**
  (SPI): the fetch is a `REFUSED` row and a refusal for health, never a failure.
- A rule that spans credential fields goes in `SourceAdapter.checkCredentials` (for example "clientId and clientSecret
  together"); one that spans parameters goes in `checkParams`.

## 16. Streaming adapters (prompt 063)

`aisstream` (`ai.foresignal.source.adapters.aisstream`) is the reference; the later streaming adapters (082, 083, 085,
089) follow it. The pieces:

```
source/adapters/aisstream/
  AisStreamAdapter.java        @Component, implements StreamingAdapter: descriptor, checkParams, start()
  AisStreamSession.java        one connection: WebSocket.Listener + StreamHandle (frames, liveness, watchdog)
  AisStreamMessageParser.java  pure: String -> sealed Result (message, ignored, untimed, confirmation, error, ...)
  VesselStateStore.java        per-entity merge into complete snapshots, bounded
  AisStreamHealth.java         @Component HealthRule + HealthDetailsContributor
```

**Connecting.** `start(ctx, sink)` opens the connection with `ctx.http().openWebSocket(uri, listener, headers,
options)` and returns a handle; the supervisor treats a returned handle as connected. The open is one budget unit, after
the backoff check, with Foresignal's User-Agent, exactly like an HTTP request; the supervisor's ladder (5 s, 15 s, 60 s,
300 s, then 900 s repeating, reset after 10 minutes connected) and the budget bound reconnects. Never reconnect from
inside the adapter: end the stream and let the supervisor do it.

**`WebSocketOptions`.** `WebSocketOptions.DEFLATE` opens the connection with `permessage-deflate` (RFC 7692) through
`collect/DeflateWebSocket` (Java-WebSocket), because the JDK client cannot negotiate extensions; `DEFAULT` is the JDK
client. Both present a `java.net.http.WebSocket` and drive a `WebSocket.Listener`, so the adapter is written once. The
deflate socket is a `MeteredWebSocket`: report the deltas of `wireBytesReceived()` (compressed bytes) with
`ctx.reportBytes(n)`, per tick rather than per message; on the JDK client report message sizes.

**Subscription timing.** An upstream that wants a subscription frame gets it in `onOpen`, at once (aisstream closes a
connection that has not subscribed within 3 s). Never log the frame: it carries the key. Give the subscription a
`toString()` that redacts it.

**Back-pressure.** Accumulate fragments until `last`, cap a message (aisstream: 256 KB, dropped and counted), hand the
complete message to the sink, and only then call `webSocket.request(1)`. `sink.accept` blocks when the ingest queue is
full; because the next message is requested only afterwards, the block reaches the socket and TCP slows the upstream,
instead of messages piling up in memory.

**Liveness.** Only a recognized data message proves the stream works: for AIS, a `MessageType` from the upstream's
complete enum, a body under it and an MMSI. Handshakes, confirmations, error envelopes and malformed frames are not
liveness. A list of recognized types that is missing one makes a subscription filtered to it read as dead, so copy the
upstream's enum whole.

**Errors: auth versus transport.** An error that names the credential ends the stream with
`CredentialsRejectedException`, which the supervisor retries hourly; anything else ends it with a plain
`AdapterException`, a ladder step. An upstream that answers a bad key by closing the socket without a word (aisstream
does) needs a rule: two consecutive closes before any confirmation or data are a rejected key, the count resetting on a
confirmation, on data and on a different key. End the stream by calling the `onClose` callbacks once, and replay the end
to a callback registered after it (the supervisor registers after `start` returns).

**Watchdogs: report fast, recycle slowly.** A `HealthRule` reports `degraded` ("connected but silent for N s") after a
short silence (120 s); the session aborts the socket (never a close handshake, which a dead peer never answers) only
after 2.5 times that (300 s), and the abort is a ladder step. Reporting quickly tells the operator; acting slowly keeps
recovery from becoming a reconnect cycle against the upstream's connection limits. Use `ctx.clock()` for silence, so a
`MutableClock` can drive it in tests.

**Merged snapshots.** A protocol that splits an entity over several message types merges them per entity and emits the
complete state on every message (rule 7). `observedAt` is the time of the newest message merged; a message without an
upstream time is dropped and counted, never stamped with the receive time (the time base is fixed, Core-Data-Model §2).
Late messages must not roll the state back. Hold an entity until it is usable (aisstream: until its first position).
Bound the store and evict idle entities. Scrub sentinels before they reach the payload (rule 11), and emit a
sentinel position as null geometry so the TRACK carry-forward applies (`POSITION_STALE`).

**Parameter changes: `update()`.** When the operator changes a running stream's parameters, the supervisor calls
`StreamHandle.update(params)`. Return true when the change was applied on the open connection (aisstream resends its
subscription, at most once a second, newest wins); return false, the default, to be restarted, which costs a
connection attempt. Storing new credentials always restarts a running stream.

**Adapter state.** A stream's `ctx.state()` writes through at once; it is not staged like a polling fetch's (§7,
"Adapter state commits with the fetch"). Persist a resume cursor such as a last event id periodically (by event count
or elapsed time) and once more when the connection ends, never once per event: one database write per message is
millions of writes a day on a busy stream. On a restart the stream resumes from the last persisted cursor and replays
at most one interval of events, which the writer deduplicates.

**Health details.** A `HealthDetailsContributor` bean adds entries to `SourceHealth.details` (aisstream:
`compressionEnabled`, `perMessageDeflate`, `vessels`).

**Tests.** A pure parser test over the fixtures; a store test with `SnapshotContract.assertStableStaticHash` and a
`MutableClock`; an adapter test against `testsupport/websocket/LoopbackWebSocketServer` (subscription timing,
fragments, oversize, back-pressure, errors, the bad-key rule, the watchdog, `update()`, no key in the logs); and an
ingest IT through a real `StreamingSupervisor` built with short timings, as `AisStreamIngestIT` does.

## 17. Strict usage policies, orbital elements and predicted passes (prompt 064)

**Stop and alert a human.** An upstream whose policy says a machine client must stop on unexpected answers
(CelesTrak: any non-200 other than its "not updated" 403, any redirect, a body of the wrong format) throws
**`UpstreamHaltException(status, reason)`**. The reason must contain "automatic retries stopped". `FetchExecutor`
then holds the source for 24 h with `BackoffRegistry.halt` (not clamped to the one-hour backoff ceiling, persisted
as `source_health.backoff_halted`), records the failure, so health is `failing` with the reason, and publishes a
`system.source_halted` notification. Only `POST /v1/sources/{id}/fetch` releases the hold early; an ordinary
backoff still answers `409 IN_BACKOFF`. Set **`followRedirects(false)`** on the descriptor when a redirect must reach
the adapter as a 3xx response instead of being followed. A status that is really "nothing new" (CelesTrak's 403) is
`FetchResult.unchanged()`, never a failure.

**Orbital elements.** `celestrak-gp` stores CCSDS OMM element sets; `ai.foresignal.space.OmmElements` reads them back
from a payload. An adapter that needs elements takes the SPI type **`OrbitalElements`** (`byNorad`) through an
`ObjectProvider`, as a SatDump station does; a DERIVED source reads `ctx.records().get("celestrak-gp", norad)`. Never
build TLE text to propagate: TLE cannot carry catalog numbers of 100000 and above. Use
`ai.foresignal.space.PassPredictor` (Orekit SGP4 through the TLE field constructor) and
`ai.foresignal.space.GroundTracks`.

**Predicted records.** Round predicted times (10 s), angles and geometry before emitting, so element refreshes do not
churn versions; tombstone a prediction only when fresh elements no longer produce it, never because elements are
missing.

## 18. Alert sources: time mapping, threads, cancellation (prompt 067)

Every CAP source reads and maps through **`ai.foresignal.source.formats.cap`**, the only CAP parser in the codebase:
024's `cap` ingest format, `tsunami-warnings`, and 089's CAP family. `CapParser.parse(InputStream, CapParseOptions)`
reads CAP 1.2 (and 1.1, recording `capVersion`) with hardened StAX: no DTDs, no external entities, no entity
expansion, a 2 MB cap and an element cap. Anything else, an un-namespaced `alert` included, and a missing
`identifier`, `sender`, `sent`, `status`, `msgType` or `scope` is an `UpstreamFormatException`. `CapGeometry` swaps
`lat,lon` to lon/lat, closes open rings and builds a MultiPolygon; a circle is a 64-segment polygon (radius 0 is a
Point); a geocode-only area has no geometry and its codes stay in the payload. The normalizer's `GeometryFixer` repairs
what is invalid.

**Time mapping** (`CapRecordMapper`, and the rule for every alert source, so forecast-aware rules work across them):

| Field | Value |
|---|---|
| `observedAt` | `sent` (or the feed's `updated`) |
| `eventTime` | `onset` when present |
| `validFrom` | `onset ?? effective`; null when neither; left out when it would fall after `validUntil` |
| `validUntil` | `expires`, explicit and upstream-asserted (Core-Data-Model §3 rule 9); a cancellation uses `expires ?? sent` |

`nws-alerts` (047) is GeoJSON and maps its fields directly; it currently sets `validFrom = onset` and
`validUntil = max(expires, ends)` (the 047 report, Issue 3 and 4).

**Threads.** One record per alert thread: `externalId` is the thread key from `CapThreadResolver`, which takes the
source's own key when it has one (tsunami: `{center}:{incidents}`), else the thread of the earliest referenced message
it remembers, else that reference's `{sender}:{identifier}`, else the message's own. The `identifier → threadKey` map
lives in the adapter state (`cap.threads`, 14 days, at most 20,000 entries). A push source (024's format) has no
adapter state and keys records by `identifier`.

**Cancellation is a version, not a tombstone.** `msgType = Cancel`, or the profile's cancellation rule (tsunami: an
`event` containing "cancel" or a VTEC action `CAN`), sets `state: cancelled`; `Update` sets `updated`; otherwise
`active`. The cancelled record keeps its thread id and expires through its own `validUntil`, and a rule on `state`
sees the change as SIGNIFICANT.

**Never close on absence.** A CAP or alert feed that drops a message has not cancelled it: the record expires at its
`expires`. The one exception is a feed that is by definition the complete active set and was received in full and
parsed without error: `nhc-cyclones` tombstones a storm that leaves `CurrentStorms.json` (with `LiveIdSet`, section 7).
A source that relies on a TTL instead (`gdacs`, 48 h from receipt) lets unlisted events expire.

Configure a new CAP source with a `CapMappingProfile`: kind, preferred languages, the parameter allowlist, the text cap
(4 KB for `headline`, `description` and `instruction`, which are the `embedFields` and untrusted text under 028's
rules), the cancellation rule and an optional `RecordExtender` for source-specific payload keys or a fallback geometry.

### CAP update chains (prompt 089)

The `cap-*` and `meteoalarm-*` families (prompt 089) share a second, simpler cancellation rule with prompt 067's
map: the record's `externalId` is the **root identifier of the thread**. `CapThreadResolver` keeps an
`identifier → root` map in adapter state; when a message has `references`, the earliest referenced identifier this
resolver remembers (or the earliest reference's own `{sender}:{identifier}`) is the thread, and the message's own
identifier and every reference are remembered against it. So an `Alert` starts a root, its `Update` versions the
same record, and a `Cancel` becomes a **tombstone** of the root (`NormalizedRecord.tombstone`), not a version.
`CapAlertRecords.map` applies this: it returns null for a non-`Actual` status unless `includeTests`, a tombstone
for `msgType = Cancel`, and otherwise one record with `observedAt = sent`, `eventTime = onset`, `validFrom =
onset ?? effective`, `validUntil = expires`, the union of the `polygon`/`circle` geometry (null with
`geometryFromGeocode = false` for a geocode-only area, never a remote lookup), and a payload whose other-language
headlines go under `headlines`. A future CAP source should reuse `CapParser` + `CapThreadResolver` +
`CapAlertRecords` rather than write its own thread rule.

## 19. LAN endpoints (prompt 069)

A source that reads the operator's own hardware takes its address from a `ParamType.LAN_URL` parameter and nothing
else. The family's slot list is code (`foresignal.local-receivers.slots`, default 2); the operator supplies only the
LAN parameters, so no request can name a host (the family rule of `SourceAdapterFamily`).

- **Schemes per parameter.** For a `LAN_URL`, `ParamSpec.allowed` lists the accepted schemes
  (`ParamSpec.lanUrl(name, description, required, "tcp")`); an empty list keeps the historical `http, https, ws, wss`.
  `tcp` and `udp` are accepted only where listed.
- **`http://` and `https://`:** resolved once under `OutboundUrlPolicy` profile `LAN_ONLY` (loopback, RFC 1918,
  unique-local; never link-local or metadata), pinned, and fetched by `UpstreamClient` at the pinned address with the
  original `Host` and redirects off. Check the path in `checkParams` (`aircraft.json`, `stats.json`) and refuse
  credentials, a query or a fragment there.
- **`tcp://host:port`:** the host is resolved once under `LAN_ONLY` and pinned; the socket transport connects to the
  pinned address and never resolves the name again. The port is always explicit; a path, query, fragment or user info
  is refused.
- **`udp://bindHost:port`:** `bindHost` is `0.0.0.0`, `::` or an address of one of this machine's interfaces (never a
  name). The descriptor must declare a `string-list` parameter `allowSenders` (`ParamValidator.ALLOW_SENDERS`), which is
  then required: IPs or CIDRs, each wholly inside loopback, private, link-local or unique-local space
  (`LocalAddresses.isPrivateBlock`).
- **Read it back** with `LanEndpoint.fromParams(params, name)`: the scheme, the host as written, the explicit port and
  the pinned (TCP) or bind (UDP) address.
- **Conventional ports** (30003 for SBS, 2947 for gpsd) appear in descriptions and docs only; no code assumes them.
- **Egress.** Every local descriptor sets `egress(LOCAL_ONLY)` and `egressOverridable(true)`: the data is the
  operator's own, so `source_settings.egress` may open it; any other descriptor's egress is fixed (`400 EGRESS_FIXED`).

## 20. Socket transport and codecs (prompt 069)

**Transport.** A streaming adapter reaches a LAN socket only through `StreamContext.sockets()`, a
`SocketTransport` (implemented by `ai.foresignal.collect.socket.DefaultSocketTransport`). Adapters never construct a
socket (a guard in `UpstreamClientIsTheOnlyHttpClientTest` fails the build), and Foresignal never listens on TCP for a
source (serving TCP is 110's SERVE sinks).

- `tcp(LanEndpoint, SocketOptions, Framing, FrameHandler) → StreamHandle`: one budget unit per connect attempt
  (`ctx.chargeRequest`), connect timeout 5 s, TCP keepalive, the optional `SocketOptions.onConnect` bytes written once
  (gpsd's `?WATCH`), and a read watchdog: a connection silent for `SocketOptions.silenceTimeout` (default 120 s) is
  closed with `SilenceTimeoutException`, so the supervisor's ladder reconnects (and pays another unit).
- `Framing.LINES` cuts LF or CRLF lines of at most 64 KB; a longer line is dropped whole and counted. `Framing.RAW`
  delivers read-sized chunks (KISS in 082).
- A `LAN_URL` may also list `mqtt` and `mqtts` (prompt 083): an operator's MQTT broker, pinned and private exactly
  like `tcp://`, explicit port; `LanEndpoint.fromParams` reads it as `Scheme.MQTT` / `Scheme.MQTTS`. MQTT itself goes
  through 060's `MqttConnections`, not this transport (see "MQTT sources" below).
- `udp(LanEndpoint bind, Set<Cidr> allowSenders, DatagramHandler)`: a `DatagramChannel` read on a virtual thread, one
  unit per bind; a datagram from outside the allowlist is counted (`foresignal.socket.udp.rejected{source}`) and never
  parsed; one over 64 KB is dropped.
- Handlers run on the connection's virtual thread and may block in `RecordSink.accept` (back-pressure). Throwing from
  a handler closes the connection with that error.
- Received bytes are metered through `ctx.reportBytes` after every read.
- Metrics: `foresignal.socket.connects{source,result}`, `foresignal.socket.frames{source}`,
  `foresignal.socket.frames.dropped{source,reason}`.
- `tcpPublic(HostPort, SocketOptions, Framing, FrameHandler)` (prompt 082): raw TCP to a fixed public host, otherwise
  exactly like `tcp`. Only a `host:port` listed in the descriptor's `allowedHosts` (`SourceDescriptor.Builder
  .allowedHost("rotate.aprs2.net:14580")`, internal, code-reviewed, never operator input) is accepted, and the name
  must resolve to a public address (`OutboundUrlPolicy` `PUBLIC_ONLY`); everything else is refused before a budget unit
  is spent. `aprs-is` uses it; 085 reuses it.
- **Writing.** A `StreamHandle` never has a write method: the only bytes a source socket can send are
  `SocketOptions.onConnect`, once. `SocketOptions.receiveOnly()` (prompt 082) refuses even that (an `onConnect` writer
  throws when the options are built) and the transport never obtains the socket's output stream, so a receive-only
  source has no code path that transmits; `receiveOnlyAfterConnect()` allows the single write on connect (the APRS-IS
  login line). Never half-close a socket (`shutdownOutput`): Direwolf and aprsc read the client's EOF as a disconnect.
- **Guarded commands (prompt 083).** The one exception to "no write method" is
  `SocketOptions.commandWriter(CommandGuard)`: the transport returns a `CommandHandle` whose `send(byte[])` hands every
  frame to the guard first (a refused frame throws `CommandGuard.CommandRefusedException` and nothing is written) and
  writes nothing after close; no `onConnect` bytes are allowed with it. Only a protocol that must ask before it is
  told uses it (the MeshCore companion, `source.formats.meshcore.MeshCoreCommandGuard`); every other handle stays
  receive-only.
- **Reconnect ladder.** A streaming descriptor may set `reconnect(ReconnectPolicy)` (prompt 082, internal): the
  supervisor then backs off exponentially from `initial` to `max` with the policy's jitter instead of its global
  ladder (`aprs-is`: 30 s → 15 min, ±20%; `local-aprs-{n}`: 5 s → 5 min). Budget refusals, backoff and rejected
  credentials keep their own waits.

**Codec SPI** (`ai.foresignal.source.codec`). A wire format is a `SocketCodec` bean:

- `id()` (kebab-case), `framing()`, `newSession(CodecContext)` and an optional `onConnect()` write.
- A `CodecSession` holds the per-connection state and returns `List<NormalizedRecord> onFrame(byte[] frame,
  Instant receivedAt)`. It is created afresh for each connection, so nothing survives a reconnect.
- Codecs do no I/O and read no clock: they are fixture-testable on their own. A malformed frame is skipped and counted
  (`skipped()`); an `UpstreamFormatException` means the whole stream is unusable and closes the connection.
- `CodecRegistry` collects the beans by id; a duplicate or malformed id fails startup.
- The shared body of a socket source is `CodecStream.start(ctx, sink, codec, endpointParam, options)` in
  `source.adapters.local`; later families copy that pattern in their own package (adapters never import each other).
- A connection that needs more than the default 5 s to connect sets `SocketOptions.withConnectTimeout` (prompt 085:
  `ais-kystverket` waits 20 s).

| Codec | Framing | Source | Notes |
|---|---|---|---|
| `sbs1` | LINES | `local-sbs-{n}` | 22-field BaseStation lines merged per hex (rule 7) into the readsb payload keys; feet → meters; `posSource: other`; times never parsed (declare `observedAtMode` RECEIVED); hexes evicted after 5 min |
| `gpsd` | LINES (RAW with `ubxMonitor`) | `local-gpsd-{n}` | `?WATCH={"enable":true,"json":true};` on connect (086: `,"raw":2` with `ubxMonitor`), the only write, checked by `GpsdCommandGuard` (`?WATCH`, `?VERSION`, `?DEVICES`, `?POLL` only); VERSION `proto_major` 3; TPV and SKY merged per device into `gnss-fix`; `observedAt` = TPV `time`. 086: every device feeds a `GnssIntegrityEvaluator` (SKY `ss` of used satellites, PPS/TOFF clock offset, UBX-MON-HW/MON-RF from `GpsdRawSplitter` via `Session.onUbx`) and each closed 30 s window emits a `gnss-integrity` record |
| `kiss-ax25` | RAW | `local-aprs-{n}` | KISS frames reassembled across reads (`formats.kiss.KissFrameDecoder`: FEND/FESC unescaping, data frames only), AX.25 UI frames to TNC2 text (`Ax25UiDecoder`), then `formats.aprs.AprsParser` and `AprsRecords`; frames that are not AX.25 UI are counted and dropped; writes nothing. A `PacketListener` reports each packet (the duplicate filter) and each dropped frame (counters) |
| `meshcore-companion` | RAW | `meshcore-local` (through `MeshCoreCompanionLink`) | MeshCore WiFi companion frames `>` + uint16 LE length reassembled across reads (`formats.meshcore.MeshCoreFrameDecoder`), frames over 176 bytes skipped, then `MeshCoreEventMapper`. The shared link owns the command queue (one in flight, 5 s timeout), the startup sequence and reconnecting, and writes through a `commandWriter` socket guarded by `MeshCoreCommandGuard` |
| `aivdm` | LINES | `ais-kystverket` (through `tcpPublic`), `local-ais-{n}` | AIS NMEA lines (`!AIVDM`, `!AIVDO`, `!BSVDM`), optionally behind an IEC 61162-450 TAG block, decoded by the shared codec in `ai.foresignal.codec.ais` (see §24). Each session owns a `FragmentAssembler` and a `VesselStateMerger`; `Options` choose the time base (`TAG`: `observedAt` = TAG `c`, a line without it dropped; `RECEIVED`) and the observer (TAG `s`, or a station name). Drops are counted by reason (`foresignal.ais.dropped`); writes nothing |

| `acars-json` | RAW | `local-acars-{n}` | acars_router's TCP output (acarsdec, dumpvdl2, dumphfdl JSON) split into objects by `formats.acars.JsonObjectStream` (newline-ended or back to back), mapped by `AcarsMetadataMapper` from an allowlist that never includes message text; nothing of a message is ever logged; writes nothing |
| `sonde-udp` | RAW (one datagram) | `local-sonde` | radiosonde_auto_rx `PAYLOAD_SUMMARY` through `RadiosondeRecords` (077); 086: session option `stationCallsign` names the observer, the observer carries `freq` as `frequencyHz`, `time` is the fallback for `datetime` |

Prompt 085 added `aivdm`, 082 `kiss-ax25`, 083 `meshcore-companion`, 077 `sonde-udp` (extended in 086), and 086 `acars-json`.

**Parameters that need privacy (prompt 086).** An adapter whose parameter is lawful or safe only on a private source
overrides `SourceAdapter.paramsRequiringPrivacy(params)`; `PUT /v1/sources/{id}` then answers `400 PRIVATE_REQUIRED` when
such a value would be in force while the source is not effectively private (descriptor OR the operator's `private`
setting, prompt 039), including a request that makes the source public. `local-remoteid`'s `keepOperatorPosition` is the
first.

**MQTT sources (prompt 083).** An MQTT source opens its broker connection through 060's `MqttConnections` with an
`MqttConnectionSpec` whose `MqttSessionOptions` is `subscriber(keepAlive)`: no status topic, so no Will and nothing
ever published; a clean session; QoS 0 subscriptions; and no reconnect by the client itself. A lost connection is
reported through the handle's `onClose`, and the streaming supervisor reconnects on the descriptor's ladder, so every
CONNECT is budgeted (`ctx.chargeRequest` before the CONNECT and before each SUBSCRIBE; `chargeRequest` is the
`consumeBudget` equivalent). Messages arrive on concurrent virtual threads, so per-entity merge state must be
thread-safe. A stream whose records carry observers may set the descriptor flag `coverageFromObservers` (internal):
each flush then reports the distinct observer receiver ids as `Coverage.receivers`. A broker reached over WebSocket
(`ws://`, `wss://`) may need headers on the upgrade request: `MqttConnectionSpec.webSocketHeaders` (prompt 085:
Digitraffic's `Digitraffic-User`). A fixed public broker is the descriptor's `allowedHosts` entry, never operator
input.

**Receivers are observers (prompt 082).** In an RF or crowd-sourced feed, the station that *received* a report is
not part of what the record describes: it goes in `NormalizedRecord.observers` (`Observer(receiverId, frequencyHz,
rssi, snr, decodeQuality)`, at most 8), never in the payload. Observers are stored on versions only, are not hashed,
and are served only with `include=observer` to agent or admin tokens (Core-Data-Model §14), so a receiver's call sign
or location never leaks through tiles, search, embeddings or anonymous reads, and a report heard by a different
receiver is not a content change. PSK Reporter's `receiverCallsign` becomes the observer with the spot's frequency and
SNR (the receiver's locator stays in the payload as `receiverLocator`); APRS-IS's igate (the station after the
q-construct) and a local TNC's `stationCallsign` become the observer of APRS records. Tests assert that no payload
value equals the observer's id.

## 21. Catalog sources and client-fetched media (prompt 072)

A **catalog source** publishes a list of things (cameras, plants, ports) rather than observations. The `cctv-*`
family is the first; prompts 074 and 075 follow the same pattern.

- **Time base:** `observedAtMode` RECEIVED and `ttlAnchor` RECEIVED, no record TTL, policy ALL with no keyframe.
  Catalogs carry no per-entry update time; never invent one from a file date. Retention is the `reference` class
  (90 d of history).
- **One shared mapper:** each pack's parser is pure (`byte[]` and a `PackContext` → entries) and reads only what the
  upstream publishes; one `CameraRecordMapper` turns entries into records, checks the bounding box, drops off-origin
  URLs, removes duplicates and counts every skip by reason (`foresignal.cctv.skipped{source, reason}`). Never port a
  synthetic value: God's Eye View's `fallbackHeadingFromId` and pose priors stay out, and a heading is set only
  from a field the upstream documents as the camera's direction (Caltrans' `direction` is the road's direction of
  travel, so it gives none).
- **Media is fetched by clients, never by Foresignal.** A record carries the upstream URL (`imageUrl`, `videoUrl`)
  and an access hint; there is no proxy, relay or snapshot store. Each URL must parse as an absolute `http`/`https`
  URL without user info that starts with one of the pack's **allowed origins** (URL prefixes, compared after
  canonicalizing scheme, host and default port). A URL that fails is dropped and the record kept.
- **The frame-access probe:** when capturing fixtures, request one frame per pack with Foresignal's User-Agent and
  no `Referer`. `image/*` (or an HLS playlist) → `direct`; an HTML page or 403 → `browser` (works only from a real
  browser `<img>`); a key-gated host → `key`. Record the result in `src/main/resources/cctv/hosts.json` and the
  fixture README. Record a `Referer` or `Origin` requirement only where the upstream documents it, and **never** add
  a User-Agent override to get past a bot wall. `frameInsecure: true` marks an `http:` frame URL; when the host also
  serves https, upgrade in the parser instead (Calgary).
- **Absence:** catalogs carry no deletion marker. `ai.foresignal.source.support.AbsenceTombstoner` keeps the ids of
  the last complete fetch (`absence:ids`, sorted, gzip+base64 above 64 KB) and the ids missed once
  (`absence:missed`), and tombstones an id missing from **two consecutive** complete fetches. Call it with the fetch
  `Outcome`: a 304, a truncated body or a parse error changes nothing. The `cctv-*` adapter also skips it for an
  empty catalog and for a paginated catalog that did not finish within its page limit.
- **Credentials in the query string** (TfL `app_key`, Ontario 511 `key`) are fine: `UpstreamClient` redacts every
  credential value from traces, recorded exchanges and logs. A required credential makes enabling fail with 409
  `CREDENTIALS_REQUIRED`.
- **Bundled catalogs** (Tallinn, Warendorf) are classpath resources with a sibling `NOTICE`; their fetch makes no
  request and runs daily.

## 22. Kit sources (prompt 073)

A **kit source** is a reviewed spec file, not a hand-written adapter. Two kits exist:

- the **feature kit** (`ai.foresignal.source.kit.feature`): ArcGIS FeatureServer/MapServer query, OGC API – Features
  and WFS 2.0 GeoJSON, one `KitFeatureAdapter` per entry of `src/main/resources/kits/features/*.json`;
- the **feed kit** (`ai.foresignal.source.kit.feed`): RSS 2.0, RSS 1.0, Atom 1.0 and JSON Feed, one `KitFeedAdapter` per
  entry of `src/main/resources/kits/feeds/*.json`.

Each file is a JSON array of specs. Reach for a kit first; write an adapter only when the upstream needs logic a spec
cannot express (a feed item mapper covers structured text inside feed items, see below).

- **Writing a feature spec** (`FeatureSourceSpec`): `id` (`feature-…`), `name`, `kind`, `protocol`
  (`arcgis|ogc|wfs`), `url` plus `layer`, `collection` or `typeName`, and `query` (`where` and `outFields` for ArcGIS,
  property `filters`, a `bbox`, an OGC `datetime`). `fields` maps each upstream attribute to `{target, type, unit,
  valueMap}`: only listed attributes reach the payload (adapter rule 8). Types are `string`, `number`, `integer`,
  `boolean` and `timestamp`; units are UCUM. `constants` adds fixed values (`restrictionType`). `idField`, `titleField`
  and every list (`significantFields`, `volatileFields`, `searchFields`, `tileProperties`) name **targets**. The mapper
  trims text, coerces numbers from text, removes nulls, blanks and the sentinels `-999`, `-9999` and year-1 dates
  (`0001-01-01…`), and rejects a feature without an id. ArcGIS dates arrive as epoch milliseconds.
- **Writing a feed spec** (`FeedSourceSpec`): `id` (`feed-…`), `name`, `url`, `kind` (default `bulletin`),
  `categories {include, exclude}`, `maxItems` (200), `recordTtl` (30 d, anchored on receipt), `observedAt`
  (`updated|published|RECEIVED`), `interval` (15 min, at least 5 min), `budget`, `license` (the descriptor's `licence`), `termsUrl`, `attribution`,
  `homepage`, and optionally `itemMapper` (a reviewed `FeedItemMapper` registered in `FeedItemMappers`; prompt 075
  adds InciWeb's) and `websub`.
- **Kit pieces in hand-written adapters** (prompt 075): a source whose mapping a spec cannot express (a custom id,
  rounding, a volatile field, several layers, its own source id) calls the kit clients (`ArcGisQueryClient`,
  `WfsJsonClient`), `FeatureGeometry`, `FeedParser` and `FeedRecords` from an adapter of its own, as `nifc-perimeters`,
  `cwfis-*`, `nhc-cyclone-cones` and `inciweb-incidents` do. A client that keeps state (`ArcGisQueryClient`'s
  `arcgis:meta`) and runs several times per fetch gets a `ScopedFetchContext` per run so the states stay apart.
- **Validator rules** (`FeatureSpecValidator`, `FeedSpecValidator`, run by `KitSpecs` at startup): https URLs without
  user info or credential-looking query parameters (a key belongs in `credential {name, placement query|header, param}`,
  entered at runtime and redacted); id pattern, length and uniqueness across both kits (the registry adds the check
  against every other source); declared targets for every reference; known types and UCUM units; durations that
  parse; `minInterval ≤ interval`; a keyframe that fits the TTL; paging caps; only `Accept` and `Digitraffic-User`
  headers. A bad spec stops startup with **every** problem listed, each with its file, index, id and field path.
- **The time-base rule** (Core-Data-Model §2): each time entry names exactly **one** field (`time.observedAt`,
  `eventTime`, `validFrom`, `validUntil` for features; `observedAt` for feeds), or receipt. A feature or item without
  that field is skipped and counted, never given another field's time. Revised-event layers follow USGS: observedAt is
  the upstream's modification time and that field is volatile.
- **Paging and completeness:** clients cap pages (`maxPages`, default 10) and features (`maxFeatures`, at most
  50,000). ArcGIS pages while `exceededTransferLimit` is true, OGC follows `links[rel=next]` on the same server, WFS
  pages while `numberReturned == count`. Only a **complete** fetch (the last page said so and no cap was hit) can
  tombstone: with `absenceTombstones: true`, a feature missing from two consecutive complete, non-empty fetches is
  tombstoned (`AbsenceTombstoner`, §21). Feeds are windows and never tombstone.
- **ArcGIS change short-circuit:** the layer document's `editingInfo` edit date is compared with the last complete
  fetch's; when equal, nothing is queried. Layers without `editingInfo` cache their metadata for 24 h and are queried
  every run.
- **WFS axis order:** GeoServer writes longitude first even for `EPSG::4326`, so `geometry.axisOrder` (`lonlat` by
  default) decides; with a `bbox`, points that fit only when swapped are swapped and logged once.
- **Simplification:** a geometry over 9,500 vertices is simplified with JTS `TopologyPreservingSimplifier` at doubling
  tolerances from 0.0005° until it fits the 10,000-vertex store limit; the record carries `geometrySimplified: true`
  and `simplifyToleranceDeg`.
- **Feed text is untrusted:** titles and summaries are reduced to plain text with jsoup `Safelist.none()` (summaries
  at most 4 KB), links must be absolute http(s), and nothing is rendered as HTML. Under prompt 028's rules, item text
  reaching an LLM is data, never instructions.
- **Operator overlay:** off by default. With `foresignal.kits.operator-specs.enabled=true`,
  `${FORESIGNAL_DATA_DIR}/kits/features/*.json` and `${FORESIGNAL_DATA_DIR}/kits/feeds/*.json` are loaded at startup;
  overlay ids must start with `op-`, and every overlay URL must pass `OutboundUrlPolicy` (`PUBLIC_ONLY`). Specs are
  never accepted over the API.
- **WebSub** (`foresignal.kits.websub.enabled=false` by default): only for a feed spec with `websub: true`, and only
  when `server.publicBaseUrl` is https and the feed advertises a hub and a self link. The subscribe request goes
  through the source's budgeted client; the hub must pass `OutboundUrlPolicy`; the secret (32 random bytes) is stored
  encrypted (`websub_subscription.secret_enc`). The hub verifies at `GET /v1/websub/{sourceId}/{token}` and delivers to
  `POST /v1/websub/{sourceId}/{token}` (signature `sha256=` or `sha1=` over the raw body, at most 2 MB, 60 a minute);
  delivered items go through the shared push pipeline. While a subscription is active the poll stretches to 6 h;
  renewal happens at 80 % of the lease, and a lost lease returns the source to polling with the health reason
  "WebSub lease lost".
- **Tests:** the test property `foresignal.test.kits.base-url` moves every kit URL under one stub as
  `/{host}{path}`; capture fixtures with the URL, command and trimming in a README, as for any adapter (§8).

## 23. Normalized kinds across formats (prompt 074)

When several upstream formats describe the same kind of thing, map them onto **one kind with one payload**, so a
trigger, tile style, search or briefing written once works for every source. `road-event` is the example: Open511
(DriveBC), WZDx 4.x (US DOT feeds), Digitraffic JSON (Finland) and DATEX II v3 (NDW, the Netherlands) all become
`road-event` records built by one payload builder, `RoadEventPayload`.

- **One builder, closed vocabularies.** `category`, `severity`, `direction`, `status` and `format` accept only their
  listed values; the builder throws on anything else, so a parser bug cannot invent a category. Each parser holds its
  own mapping table (`Open511Parser.CATEGORY`, `WzdxParser.CATEGORY`, `DigitrafficRoadParser.category`,
  `Datex3SituationParser.category`) and the tests run every row as a parameterized case. An unknown upstream code
  maps to `other` or `unknown`, never to a guess; keep the upstream code itself in `subtype` (lower-kebab case).
- **Map only what the format says.** A format without a severity (WZDx) derives one from a documented field
  (`vehicle_impact`) or leaves `unknown`. `isClosure` means the road itself is closed (a closed road state, an
  all-lanes-closed impact, a closure management type, an announced road closure), not a lane.
- **One time base per source** (Core-Data-Model §2): `observedAt` is each format's update or version time
  (`updated`, `core_details.update_date`, `versionTime`, `situationVersionTime`), never replaced per record by
  another field; an entry without it is skipped and counted. Periods are not instants, so `eventTime` stays null;
  `validFrom` is the scheduled start and `validUntil` only the upstream-asserted end of the last interval, never
  computed. The payload repeats both as `startsAt`/`endsAt`, the descriptor's `startField`/`endField`.
- **Shared text rules.** Headlines are at most 200 characters, descriptions plain text of at most 4 KB, cut on a
  character boundary; when an upstream "headline" is only its event-type code, use the description's first sentence.
  Upstream text is untrusted data (`embedFields` included).
- **Removal is per source, the rule is shared.** Every road source fetches the full current set, so absence is
  meaningful: `AbsenceTombstoner` (§21) retires an id after two consecutive complete misses, with one scope per
  independently fetched document (Digitraffic tracks each situation type apart, so a 304 on one never retires
  another's events). An explicit end marker (`ARCHIVED`, `completed`, `ended`, `retracted`, `earlyClosing`) is
  tombstoned at once. Unlike catalogs, a well-formed empty document counts as complete: road feeds are legitimately
  empty at times, and two consecutive empty documents are still needed before anything is retired.
- **Personal and contact data stay out.** The payload is an allowlist; contact members (Digitraffic `contact`, WZDx
  `contact_name`/`contact_email`) are never read, and the parser tests assert that none reaches a payload.
- **Prefer the simpler official format.** Digitraffic publishes the same messages as DATEX II and as JSON; the JSON
  is used. NDW publishes only DATEX II, so it is read with a streaming StAX parser on the hardened factory (no DTD,
  no external entities) with a decompressed-size cap. Check the axis order of GML `posList` on a real fixture
  (DATEX II uses "lat lon") and assert it in a contract test.
- **Reviewed lists, never runtime discovery.** Servers and feeds come from a reviewed list in code
  (`Open511Server.REVIEWED`) or a vendored, reviewed snapshot (`roads/wzdx-feeds.json`); a registry is never read at
  runtime and callers cannot add URLs. A snapshot URL must not embed a credential: a key-required feed declares a
  required credential and the adapter adds it where the registry documents it.

## 24. Shared codecs (prompt 085)

Some wire formats are shared by adapters **and** by code that is not an adapter: prompt 110's NMEA sink writes AIS
with the same codec the AIS sources read with. Such a codec is a platform package outside `ai.foresignal.source`,
the way the AIVDM codec is `ai.foresignal.codec.ais`, and it is the template for the next one:

- **No adapter imports.** The codec depends only on the SPI types it produces (`NormalizedRecord`, `Observer`,
  `Quality`), never on an adapter or a sink, so adapters, socket codecs (`source.codec`) and sinks can all use it.
- **Pure and clock-free.** Time comes in with each call (a receive time, a TAG time); counters are exposed as numbers
  or through a listener, and Micrometer stays in the caller (`AivdmSocketCodec` registers the
  `foresignal.ais.*` meters).
- **Both directions in one place.** `AisDecoder` turns sentinels into absent values (adapter rule 11) and
  `AisEncoder` writes absent values back as the sentinels, so `decode(encode(x)) == x` holds and is tested; values are
  kept on the wire resolution (1/10,000 minute, 1/10 kn, 1/10°, 1/10 m).
- **One record contract.** `VesselStateMerger` produces prompt 063's `vessel` payload for every vessel source,
  AIVDM or not: `ais-digitraffic` feeds it source-neutral `VesselUpdate`s built from JSON. The descriptor defaults the
  sources share are in `source.formats.ais.VesselDescriptors`, which `aisstream` uses too.
- **In-house, free.** No third-party AIS library: `tbsalling/aismessages` is not under a free license. Golden
  sentences in the tests were cross-checked with an independent decoder used only as an oracle outside the
  repository; nothing was copied from it.

| Piece | Role |
|---|---|
| `TagBlock` | IEC 61162-450 TAG block: checksum verified, `c` (seconds or milliseconds), `s`, `g`, `n`; a missing block is not an error |
| `NmeaSentence` | `!AIVDM`/`!AIVDO`/`!BSVDM` fields and checksum; a bad checksum is a result, not an exception |
| `FragmentAssembler` | key `(talker, TAG group or sequential id, channel, count)`, at most 9 fragments, out of order accepted, 2 s timeout, at most 1,000 open groups |
| `SixBit` | armoring, bit reader and writer with signed fields, six-bit text with `@` padding trimmed |
| `AisMessage`, `AisDecoder` | types 1/2/3, 4 (counted), 5, 18, 19, 21 (counted), 24A/B, 27; others `Unsupported` |
| `AisEncoder`, `NmeaWriter` | types 1, 5, 18, 24A/B; sentences of at most 82 characters with CR LF, sequential ids 0–9, fill bits on the last fragment |
| `VesselStateMerger`, `VesselUpdate` | per-MMSI merge (6 h, 100,000 MMSIs), static data held until the first fix, quality and observers |

## 25. Reference datasets (prompt 094)

A **reference dataset** is a slow-changing catalogue Foresignal polls weekly and versioned by comparing whole
runs. The power plants, airports, cables, volcanoes, ports, park boundaries, launch sites and nuclear
facilities are the family; `world-port-index` and `ourairports` feed 099.

- **Descriptor.** `ai.foresignal.source.support.ReferenceSupport.defaults(builder)` applies STANDARD, an `ALL`
  policy, no TTL, `observedAtMode=RECEIVED`, `zeroResultsNormal=false`, a 7-day interval (min 1 day), a 2/min
  and 20/day budget and 730 days of retention. The long window keeps the retention keyframe
  (`window / 4` ≈ 6 months) from rewriting every record each week. Declare `dataset-release` as a kind and a
  `licence`, `termsUrl` and `attribution`.
- **Chunked fetch.** `ctx.emitChunk(records)` (at most 5,000 records) hands one chunk to the executor, which
  submits it as its own `IngestBatch` and waits for it before accepting the next: a dataset larger than the
  heap budget is never held as records, and the writer's back-pressure reaches the parser. Parse with a
  streaming reader and hand each record to `ReferenceSupport.chunks(ctx)` (a `ChunkEmitter`; call `flush()` at
  the end); `ReferenceStreams.forEachElement(body, what, consumer, path...)` walks a JSON document to an array
  (`features`, `ports`, `results.bindings`) and reads one element tree at a time, and the CSV and TSV parsers
  read line by line. Never build a `List<NormalizedRecord>` of the whole dataset. Count records skipped for a
  missing id and records kept without geometry because theirs was malformed, and log the counts. The final
  `FetchResult.complete(records, release)` reports the dataset's own version (ETag, Last-Modified or its
  version string).
- **Complete-set reconciliation.** When a run returns `complete=true`, the executor submits `TombstoneAbsent`
  after the last chunk: the non-tombstoned heads whose `record_live.last_received_at` is older than the run's
  start were not seen and are tombstoned through the normal fold (`persist_reason=TOMBSTONE`) in chunks of
  10,000. A 304, a failed run and a partial run never reconcile.
- **Shrink guard.** If more than `reconcile.maxAbsentRatio` of the dataset (descriptor field, default 0.2)
  would be tombstoned at once — a truncated download must never read as a mass deletion — nothing is written,
  health shows "dataset shrank by N% — reconciliation skipped", a `DISCARDED` `fetch_log` row with the run's
  `run_id` records the skipped reconciliation, and the operator releases it once with `allowShrink=true` on
  `POST /v1/sources/{id}/fetch`. That run's conditional GETs send no stored validators, so it re-reads every
  file rather than getting a 304. The degradation clears whenever a reconciliation runs (the override, or a
  later complete run back within the ratio). The tombstone batches' `fetch_log` rows carry the run's `run_id`.
- **Forced touches.** Every batch of a complete-set run sets `IngestBatch.forceTouch`, so an unchanged record
  advances `record_live.last_received_at` even inside `touchMin` (TOUCH, never TOUCH_THROTTLED); otherwise a
  re-run inside the throttle would make the reconciliation think the record disappeared.
- **`dataset-release`.** After reconciliation the executor emits one record, kind `dataset-release`,
  `externalId` = the release, payload `upstreamVersion`, `records`, `appeared` (FIRST+REAPPEAR), `changed`
  (SIGNIFICANT+CHANGE) and `disappeared` (TOMBSTONE). Rules and briefings notice a release from it.
- **Multiple files (OurAirports, submarine cables, spaceports).** Each file is its own kind. Return
  `FetchResult.completeFor(freshKinds, records, release)` with the kinds whose file arrived fresh: the executor
  reconciles those kinds only, so a 304 on one file neither tombstones that file's records nor blocks the other
  file's reconciliation. When every file answered 304, return `FetchResult.unchanged()`.
- **Validators commit with the run.** The `ETag`/`Last-Modified` of a file is durable only once the run's
  records are written (§7, "Adapter state commits with the fetch"); a download that fails, or a run whose chunks
  are not ingested, keeps the old validators, so the next run re-reads the file.
- **Fixture READMEs** note `synthetic: true` when a capture needs a key or is trimmed; recapture at 143.

## Licence terms and reliability (prompt 109)

A `SourceDescriptor` may set `licenceTerms` (`ai.foresignal.provenance.LicenceTerms`) next to the
free-text `licence`/`termsUrl`. When `licenceTerms` is null and `licence` matches a catalogue id or
alias (see `src/main/resources/provenance/licences.json`), the terms are derived from the catalogue; the
registry warns when neither resolves. The required fields are `id`, `name`, `commercialUse`
(`ALLOWED | FORBIDDEN | UNKNOWN`), `shareAlike`, `attributionRequired`, `redistribution`
(`ALLOWED | LOCAL_ONLY | FORBIDDEN | UNKNOWN`), `attributionText` and `termsUrl`.

Set `reliability` (an Admiralty grade `A`–`F`, default `F`) and, for a DERIVED source, `derivedFrom`
(the source ids it is built from; its terms are the most restrictive of theirs). A local receiver
(readsb/978, `local-ais`, `local-aprs`, `*-local`, the decoders) defaults to `LOCAL_ONLY`, and a record
whose `posSource` is `MLAT` is `LOCAL_ONLY` per record however its source is configured. The fallback
for an unresolved licence is the restrictive `unknown` terms, so an operator must set terms before a
new source can be re-served.
