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
| `entityKeys` | Identifier type to payload field, for the entity registry (prompt 098) | none |
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

## 8. Fixtures and tests

**Fixtures.** Capture a real response once with an identifiable User-Agent (for USGS: `curl -s -A "Foresignal-fixture-capture (+https://foresignal.ai)" <url>`), trim it to a reviewable size, and write a sibling `README.md` with the capture date, URL, command, notable response headers and the trimming method. Add hand-made fixtures for edge cases and for the transitions the writer must handle (a revision, a volatile-only change), and one for a format change. Keyed sources without a key build fixtures from published examples and mark them `synthetic: true` with "recapture with a real key" (Conventions).

**Required tests.** No test calls a real upstream.

- **Parser contract** (plain JUnit): the real fixture parses to the expected count with spot-checked fields; the time base; 2D geometry with SRID 4326; edge cases skipped with counts; a format change throws `UpstreamFormatException`; `FieldSpecContract` on every fixture; `SnapshotContract.assertStableStaticHash` on two snapshots that differ only in volatile content.
- **Adapter test** (WireMock): the URL per parameter, the conditional GET round trip (200 with a validator, then a 304), an error status → `UpstreamHttpException`, the User-Agent, and `DescriptorValidator.problems(...)` empty.
- **Ingest IT** (`@IntegrationTest` + WireMock + `MutableClock`): real fetches through the scheduler, `FetchExecutor` and the real writer, asserting versions and persist reasons, `fetch_log` rows, health and `record_live`. Never seed `record_head`, `record_live` or `record_version` with raw SQL (Core-Data-Model §17). ITs that need the scheduler running add `@TestPropertySource(properties = "foresignal.collector.scheduled=true")`.

**Test base URL.** An adapter's upstream base URL is a constant, overridable only by a test property named `foresignal.test.adapters.<adapter>.base-url`, read with `@Value("${...:}")` in the adapter's single constructor (blank means the production URL). Unit tests pass the WireMock URL to the constructor; ITs register it with `@DynamicPropertySource`. It is never an operator setting: the feed list is code-reviewed, which is what keeps adapters from becoming an SSRF vector. USGS uses `foresignal.test.adapters.usgs.base-url`.

## 9. Porting, families and other modes

- **God's Eye View** (`~/Documents/GitLab/gods-eye-view`, MIT) may be ported to Java. Add a header comment naming the origin file and the MIT notice, and list it in `THIRD_PARTY_NOTICES.md`. The USGS adapter was written independently and ports nothing (GEV's earthquake layer filters to M2.5+ and rejects whole snapshots, which Foresignal does not want).
- **ShadowBroker, Velocity, World Monitor (AGPL) and SatDump (GPL)** are reference material only. Read them for endpoints and quirks; never copy code.
- **Adapter families:** one `SourceAdapterFamily` bean, one source per feed, each with its own settings, budget and health.
- **Streaming adapters** (the first is `aisstream`, prompt 063) emit through the `RecordSink` they are given; the framework's `PolicyAwareRecordSink` batches, keeps latest-wins for SAMPLED and LATEST, and writes one `fetch_log` row per flush. Implement `StreamHandle.onClose`, report bytes with `ctx.reportBytes(n)`, and throw `CredentialsRejectedException` for a refused key.
- **PUSH sources** (custom sources and inbound webhooks, prompt 024) have no fetch; records arrive through the ingest API.
- **DERIVED sources** (runtime in prompt 052) make no upstream requests; `ctx.http()` throws for them.

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
