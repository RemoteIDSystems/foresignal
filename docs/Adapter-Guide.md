# Foresignal — Adapter Guide

How to write, test and repair a source adapter. Every adapter prompt after 011 follows this guide, and the adapter-maintenance agent (prompt 140) uses its repair procedure. The worked example throughout is `usgs-earthquakes` (`ai.foresignal.source.adapters.usgs`), the reference adapter built in prompt 011.

Authorities: Architecture §4.2 (adapter rules 1–6), Core-Data-Model §2–§5 (rules 7–13, time contract, hashing, ingest policies), and `docs/Conventions.md`, which wins over this guide where they disagree.

## 1. Package layout

One package per upstream under `ai.foresignal.source.adapters`, with no imports between adapter packages. Shared format parsers (readsb JSON, CAP 1.2, AIVDM, GeoJSON, KISS/AX.25) live under `ai.foresignal.source.formats.*`. Shared format code lives there, **not** under `adapters`, so any adapter may import it without breaking the no-cross-adapter rule: the readsb mapper and the aircraft descriptor defaults (`formats.readsb`, prompt 062) are used by `adsblol`, `opensky` and the local receivers (069), exactly as `formats.cap` is used by every CAP source. Code that only one adapter uses stays in that adapter's package.

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
- **Streaming adapters** (the first is `aisstream`, prompt 063) emit through the `RecordSink` they are given; the framework's `PolicyAwareRecordSink` batches, keeps latest-wins for SAMPLED and LATEST, and writes one `fetch_log` row per flush. See §16 for the full pattern.
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
