# Foresignal — Adapter Guide

How to write, test and repair a source adapter. Every adapter prompt after 011 follows this guide, and the adapter-maintenance agent (prompt 140) uses its repair procedure. The worked example throughout is `usgs-earthquakes` (`ai.foresignal.source.adapters.usgs`), the reference adapter built in prompt 011.

Authorities: Architecture §4.2 (adapter rules 1–6), Core-Data-Model §2–§5 (rules 7–13, time contract, hashing, ingest policies), and `docs/Conventions.md`, which wins over this guide where they disagree.

## 1. Package layout

One package per upstream under `ai.foresignal.source.adapters`, with no imports between adapter packages. Shared format parsers (readsb JSON, CAP 1.2, AIVDM, GeoJSON, KISS/AX.25) live under `ai.foresignal.source.formats.*`.

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
- An upstream field kept verbatim in its domain-standard unit declares that unit: readsb `alt_baro` is `[ft_i]` with datum `PRESSURE`; USGS `depth` is `km` with datum "network-dependent reference, positive down".
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
- When a 200 body from `getConditional` could not be used (truncated or unparseable), call `SourceHttp.forgetValidators(ctx.state(), stateKey)`: the validators were stored when the response arrived, and without this the next run would get a 304 and never see the content.
- A probe never moves adapter state: during a probe `ctx.state()` is an in-memory overlay and `getConditional` stores no validators, so a probe cannot consume a live set or a cursor.

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
- **Streaming adapters** (the first is `aisstream`, prompt 063) emit through the `RecordSink` they are given; the framework's `PolicyAwareRecordSink` batches, keeps latest-wins for SAMPLED and LATEST, and writes one `fetch_log` row per flush. Implement `StreamHandle.onClose`, report bytes with `ctx.reportBytes(n)`, and throw `CredentialsRejectedException` for a refused key.
- **PUSH sources** (custom sources and inbound webhooks, prompt 024) have no fetch; records arrive through the ingest API.
- **DERIVED sources** make no upstream requests; `ctx.http()` throws for them. See §14.

## 10. Repair procedure

For the maintenance agent (prompt 140) or a person, when an adapter starts failing or its diagnostics show skipped features:

1. **Probe.** Run the source's probe (`ProbeService`, `POST /v1/sources/{sourceId}/probe` once 013 lands) to capture the raw response without ingesting it.
2. **Diff against the fixture.** Compare the probe body with the recorded fixture: document shape, new, renamed or retyped keys, units, timestamps, headers (validators, rate limits). The `UpstreamFormatException` snippet usually names the change.
3. **Update the parser** for the new shape, keeping the time base fixed (rule 13) and the payload allowlisted (rule 8). Update `fieldSpecs` for any new key.
4. **Refresh the fixture and README.** Recapture with the identifiable User-Agent, trim it the same way, rebuild the derived fixtures, and record the new capture date and what changed.
5. **Run the contract test** (`FieldSpecContract`, `SnapshotContract`, the parser test) and the adapter test, then record the change in the Completion Report.

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
the payload, so clients and rules know it is not a measured position. No adapter of prompts 068 (IODA),
076 (abuse.ch) or 090 (UNHCR, advisories) exists yet; when they are built they use this table.

