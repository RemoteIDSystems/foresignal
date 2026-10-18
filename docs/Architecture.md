# Foresignal — Architecture

Version 2.0 · 2026-10-18 · Companion to the [Product Charter](Product-Charter.md) and the [OpenAPI spec](OpenAPI.json).

This document is the single architecture reference. Version 2.0 folds in the v1.2 core data model and the weather and imagery design (both now archived under [design-history/](design-history/): [Core-Data-Model.md](design-history/Core-Data-Model.md), [Weather-Imagery-Design.md](design-history/Weather-Imagery-Design.md), [Gap-Analysis.md](design-history/Gap-Analysis.md)) and adds every subsystem built through prompt 144. The [Intelligence Design](Intelligence-Design.md) remains the detailed reference for the intelligence layer; §40 summarizes it.

Prompt numbers are the v1.3 numbering (001–144). Older text that uses earlier numbers is translated with [Roadmap](Roadmap.md) §5.

---

## 1. Overview

Foresignal is a headless intelligence broker: a single Spring Boot process backed by one PostgreSQL database with PostGIS and pgvector. Inside the process, five core subsystems communicate through narrow interfaces (diagram below). Around them sit the operator features (scoped tokens, inbound webhooks, notification channels, the MCP server, an optional budgeted LLM subsystem), the weather, raster and imagery part (§27), the interop outputs (§28, §33–§37), and the intelligence layer (§29–§32, §38–§40).

```
                 ┌──────────────────────────── Foresignal process ─────────────────────────────┐
                 │                                                                        │
  Upstreams ◄────┤  ① Collection                ② Ingest              ③ Distribution      │
  (HTTP, WS,     │  ┌───────────────┐   results ┌─────────────┐ batch  ┌───────────────┐   │
   MQTT, files)  │  │ Scheduler     ├──────────►│ Ingest      ├───────►│ Change bus    │   │
                 │  │ Adapters      │  (queue)  │ writer      │ events │ Live bus      │   │
                 │  │ Upstream      │           │ (single     │        │  ├─ SSE hub   ├───┼──► Frontends
                 │  │ client +      │           │  thread)    │        │  ├─ Trigger   │   │    Agents
                 │  │ budgets       │           └──────┬──────┘        │  │   engine ──┼───┼──► Channels, sinks
                 │  └───────┬───────┘                  │               │  └─ Cursored  │   │
                 │          │ usage/health             │ versions      │     consumers │   │
                 │          ▼                          ▼               └───────────────┘   │
                 │  ┌──────────────────────── ④ Store (PostgreSQL + PostGIS + pgvector) ┐  │
                 │  │ record_version (partitioned) · record_head · record_live ·       │  │
                 │  │ fetch_log · seq_clock · settings · rules · alerts · channels …   │  │
                 │  └───────────────────────────────▲──────────────────────────────────┘  │
                 │                                  │                                     │
                 │  ⑤ API (REST /v1, GeoJSON, MVT, SSE, MCP, OGC, OpenAPI) + key/token filters │
                 └────────────────────────────────────────────────────────────────────────┘
```

**Key principle:** the *only* thing that causes an upstream request is an enabled source in `source_settings` (or an explicit operator fetch, probe or doctor run). Nothing a client reads or streams ever reaches the collector. The same holds for the LLM: nothing calls a model except an explicit Ask, a scheduled briefing or analysis cycle, an enrichment-enabled rule, the analyst, a translation or speech request, or the embedding worker for sources configured to embed. All of these are budgeted like sources.

**No client request ever causes an upstream fetch:** not tiles, samples, media, maps or SatDump polar plots. Upstream data is pulled only by enabled, budgeted sources on their own schedule. Foresignal hosts the products it derives from what it ingested, and never proxies basemaps or third-party tiles.

**Access rule:** any `/v1/**` path that `AccessPolicy` does not list requires `admin` (fail closed), so a new endpoint is never accidentally world-writable.

## 2. Technology stack

| Concern | Choice | Notes |
|---|---|---|
| Runtime | Java 21 LTS (Gradle toolchain 21; images `eclipse-temurin:21-jdk` to build, `eclipse-temurin:21-jre` to run) | Virtual threads (`spring.threads.virtual.enabled=true`) |
| Framework | Spring Boot 4.1.1 | Web MVC (not WebFlux); SSE via `SseEmitter` |
| JSON | Jackson 3 (`tools.jackson.*`, Boot default) | Payloads stored as `jsonb` |
| Persistence | Spring Data JPA, Hibernate ORM 7 with `hibernate-spatial` and JTS | `ddl-auto=validate`; **Flyway owns the schema** (§25) |
| Schema migrations | Flyway 12.4.0 (`spring-boot-starter-flyway` plus `flyway-database-postgresql`) | `src/main/resources/db/migration/V{n}__{description}.sql` |
| Bulk writes | `JdbcTemplate` batch with `reWriteBatchedInserts=true` | The ingest writer is native SQL only (invariant I9) |
| Database | PostgreSQL 17 + PostGIS 3.5 + pgvector | Image `docker/db/Dockerfile`: `postgis/postgis:17-3.5` plus `postgresql-17-pgvector` |
| HTTP and WebSocket client | `java.net.http` (`UpstreamClient`), plus Java-WebSocket for permessage-deflate streams | The only network path of adapters is `ctx.http()` |
| MQTT | HiveMQ MQTT Client (shaded artifact) | One supervised client per owner through `ai.foresignal.mqtt.MqttConnections` (§17.4); MQTT source adapters use it too |
| Vectors | pgvector through native SQL | Two vector slots per row with HNSW indexes created at the configured dimension (§20); no Hibernate vector mapping |
| MCP | MCP Java SDK (core plus Jackson 3 binding), servlet transports registered directly | `/mcp` streamable HTTP, optional legacy HTTP+SSE (§18) |
| LLM | Official Anthropic Java SDK for type `anthropic`; `java.net.http` clients for OpenAI-compatible APIs and Ollama | §19 |
| OpenAPI | Authored `docs/OpenAPI.json` served at `/v1/openapi.json`; springdoc for Swagger UI at `/docs` and drift testing | §10 |
| Metrics and tracing | Micrometer with the Prometheus registry; Micrometer Tracing over the OpenTelemetry bridge with no exporter | `/actuator/prometheus`; trace ids reach logs only |
| Build | Gradle 9.8.1 wrapper, Kotlin DSL, single module | `gradle/libs.versions.toml` pins every explicit version |
| Packaging | Multi-stage Dockerfile; Docker Compose | §14 |

### 2.1 Runtime dependencies

Every dependency on the broker's runtime classpath (`build.gradle.kts` with `gradle/libs.versions.toml`). "Boot BOM" means the version is managed by Spring Boot 4.1.1.

| Library | Version | License | Used for |
|---|---|---|---|
| Spring Boot starters `web`, `data-jpa`, `flyway`, `validation`, `actuator`, `mail`; `spring-boot-micrometer-tracing-opentelemetry` | 4.1.1 | Apache-2.0 | Framework |
| Spring dependency-management plugin | 1.1.7 | Apache-2.0 | BOM import (build) |
| Hibernate ORM and `hibernate-spatial` | Boot BOM (7.x) | Apache-2.0 | JPA, spatial types |
| JTS (`jts-core`, through hibernate-spatial) | Boot BOM (1.20.0) | EPL-2.0 or EDL-1.0 | Geometry |
| Flyway (`flyway-core` via the starter, `flyway-database-postgresql`) | 12.4.0 | Apache-2.0 | Schema migrations (§25) |
| PostgreSQL JDBC (`runtimeOnly`) | Boot BOM (42.7.x) | BSD-2-Clause | Database driver |
| Jackson 3 databind, `jackson-dataformat-csv`, `jackson-dataformat-cbor` | Boot BOM (3.1.x) | Apache-2.0 | JSON, CSV output (014), SatDump `product.cbor` (049) |
| Micrometer, Prometheus registry, Micrometer Tracing OTel bridge, OpenTelemetry API | Boot BOM | Apache-2.0 | Metrics; trace ids in logs (019) |
| Caffeine | Boot BOM (3.2.4) | Apache-2.0 | Raster, tile and index caches (042, 045) and others |
| Angus Mail (through `spring-boot-starter-mail`) | Boot BOM (2.0.5) | EPL-2.0, EDL-1.0 or GPL-2.0 with Classpath Exception | Email channel (018) |
| springdoc-openapi (`starter-webmvc-ui`) | 3.1.1 | Apache-2.0 | Swagger UI, generated spec for drift tests |
| Bucket4j (`bucket4j_jdk17-core`) | 8.21.0 | Apache-2.0 | Source budgets, client rate limits |
| ZXing core | 3.5.4 | Apache-2.0 | Pairing QR codes (023) |
| networknt `json-schema-validator` | 3.0.8 | Apache-2.0 | EDP manifests and items (024); contract tests |
| RE2/J | 1.8 | BSD-3-Clause | Linear-time EDP schema patterns |
| MCP Java SDK `mcp-core`, `mcp-json-jackson3` | 2.0.1 | MIT | MCP server (025) |
| Nimbus JOSE+JWT | 10.10 | Apache-2.0 | MCP remote authorization (141) |
| Google Tink (protobuf excluded) | 1.23.0 | Apache-2.0 | Ed25519 verification for Nimbus (141) |
| Anthropic Java SDK (`anthropic-java`, with OkHttp, Kotlin stdlib, Jackson 2) | 2.71.0 | MIT (dependencies Apache-2.0) | LLM type `anthropic` (026) |
| commonmark and `commonmark-ext-gfm-tables` | 0.30.1 | BSD-2-Clause | Markdown to HTML (029, 058) |
| OWASP Java HTML Sanitizer | 20261011.1 | Apache-2.0 or BSD-2-Clause | HTML rendition allowlist |
| Uber H3 | 4.5.0 | Apache-2.0 | Rollup cells and grids (038) |
| Apache Commons Compress | 1.27.1 | Apache-2.0 | Bzip2 of the RIPE Atlas dump (092) |
| Apache Commons Statistics (`distribution`, `inference`) | 1.3 | Apache-2.0 | Discovery statistics (119) |
| Apache Commons Numbers (`gamma`) | 1.3 | Apache-2.0 | Regularized gamma and beta |
| Apache Commons RNG (`simple`, `sampling`) | 1.7 | Apache-2.0 | Seeded sampling |
| Hipparchus `hipparchus-optim` | 4.0.3 | Apache-2.0 | ETS maximum likelihood (126) |
| NetCDF-Java `cdm-core`, `grib`, `cdm-radial` (BOM `netcdf-java-bom`) | 5.11.0 | BSD-3-Clause | GRIB2, NetCDF4 and NIDS decoding (046, 106) |
| `libaec-native` (`runtimeOnly`) | 1.1.7.0 | BSD-2-Clause | GRIB2 template 5.42 (CCSDS) |
| Orekit (with Hipparchus modules) | 13.1.9 | Apache-2.0 | SGP4 swaths and passes (054, 064) |
| Jayway JsonPath (json-smart excluded) | 3.0.0 | Apache-2.0 | RecordMapping (059) |
| HiveMQ MQTT Client, shaded | 1.4.0 | Apache-2.0 | MQTT channel and sources (060, 083, 085) |
| Java-WebSocket | 1.6.0 | MIT | permessage-deflate WebSockets (063) |
| MobilityData `gtfs-realtime-bindings` | 0.2.0 | Apache-2.0 | GTFS-RT (070) |
| protobuf-java (pinned above the BOM) | 4.36.1 | BSD-3-Clause | GTFS-RT and NetCDF-Java runtime |
| jsoup | 1.23.2 | MIT | Feed-kit HTML to text (073) |
| Apache PDFBox | 3.0.8 | Apache-2.0 | Attachment text extraction (130) |
| parquet-java `parquet-column`, `parquet-hadoop` (Hadoop server stack excluded) | 1.18.1 | Apache-2.0 | GeoParquet exports and archive (111) |
| zstd-jni | 1.5.7-15 | BSD-2-Clause | GeoParquet page compression |
| FlatGeobuf (`org.wololo:flatgeobuf`) | 3.29.0 | ISC | FlatGeobuf exports (111) |
| webp-imageio | 0.10.0 | Apache-2.0 (libwebp BSD-3-Clause) | WebP tiles (108) |
| sqlite-jdbc | 3.53.4.0 | Apache-2.0 | MBTiles and GeoPackage files (108, 111) |
| Lingua | 1.2.2 | Apache-2.0 | Social post language detection (135) |
| Chicory `runtime` (brings `wasm`) | 1.7.5 | Apache-2.0 | WASM parser plugins (141) |

`hadoop-common` 3.4.1 (Apache-2.0) is `compileOnly` and test-only, non-transitive: javac needs `org.apache.hadoop.conf.Configuration` for parquet overload resolution, and nothing of Hadoop ships (`ParquetClasspathTest`). The JDK supplies `java.net.http`, `javax.imageio` (PNG, TIFF), Java2D (static maps, legends) and the crypto behind Web Push, VAPID, APNs and FCM tokens.

**Test dependencies:** `spring-boot-starter-test` (JUnit Jupiter EPL-2.0, AssertJ Apache-2.0, Mockito MIT), `spring-boot-testcontainers`, `spring-boot-webmvc-test`, Testcontainers 2.0.5 (`postgresql`, `junit-jupiter`, `toxiproxy`; MIT), WireMock standalone 3.13.2 (Apache-2.0), Awaitility 4.3.0 (Apache-2.0), GreenMail 2.1.14 (Apache-2.0), JaCoCo 0.8.15 (EPL-2.0).

**Tools outside the classpath:** Redocly CLI 2.62.1 through `npx` (`lintOpenApi`), oasdiff through a local binary or the Docker image `tufin/oasdiff:v1.33.0` (Apache-2.0; `checkApiCompat`, §25), openapi-generator 7.25.0 (Apache-2.0), openapi-typescript 7.13.0 and openapi-fetch 0.17.0 (MIT), openapi-python-client 0.29.1 (MIT) for the SDKs (§42). The standalone CLI build (`cli/`) uses picocli 4.7.7 (Apache-2.0), Jackson 3.2.3, json-schema-validator 3.0.8 and slf4j 2.0.20 (MIT); none of it is on the broker's classpath (`THIRD_PARTY_NOTICES.md`).

**No GPL or AGPL library ships** (invariant PRC-2, `LicenseAllowlistTest`, which scans the runtime classpath and denies Smile, Weka, JSAT and jdistlib by name).

### 2.2 The one exception to the Maven-Central rule

Libraries come from Maven Central, except NetCDF-Java, which is published only to Unidata's own repository (prompt 046). `build.gradle.kts` declares `https://artifacts.unidata.ucar.edu/all/` (named `unidata`) inside Gradle `exclusiveContent`, filtered to the groups `edu.ucar` and `edu.ucar.unidata`, so those groups resolve from there and nowhere else, and nothing else resolves from there. Three checks keep it narrow:
- `gradle/verification-metadata.xml` pins sha256 checksums for the `edu.ucar*` artifacts only (every other group is trusted with `<trust group="^(?!edu\.ucar).*" regex="true"/>`);
- `checkUnidataScope` (`gradle/unidata-scope.gradle.kts`, wired into `check`) fails if any component resolved from `unidata` has another group;
- transitive dependencies of NetCDF-Java (Guava, JNA, jdom2, protobuf, Apache HttpClient 4 and others) still come from Maven Central.

The old `/repository/unidata-all/` path 301-redirects and is not used. If the Unidata host is down, the Operator Guide describes a vendored file repository under `gradle/vendor-repo/`. Licenses: NetCDF-Java BSD-3-Clause, libaec BSD-2-Clause, jj2000 its own permissive license, JNA under its Apache-2.0 option (`THIRD_PARTY_NOTICES.md`).

## 3. Package layout

Single Gradle module, base package `ai.foresignal` (`ForesignalApplication` at the root). One line per package (`ls src/main/java/ai/foresignal`):

```
access         Tile keys (fsk_): the map-client variant of feed keys (050)
analyst        Scheduled LLM analyst: profiles, runs, notes, proposals, feedback (102)
analytics      Discovery: series reader, analysis-series registry, findings, change points, event studies,
               co-occurrence, signal families, in-house statistics (math) (119)
api            Controllers, DTOs, mappers, ProblemDetail handling, pagination cursors
ask            Ask Foresignal: tool loop, threads, proposed actions (028)
attention      Attention topics driving the GDELT and Wikimedia attention sources (117)
backtest       Job framework, rule and recipe backtests, precursor tests, caveats, tuning suggestions (103)
baserate       Base-rate library, reference-history imports, historical analogs (121)
benchmark      Analytic benchmark harness: planted-truth scenarios, component benchmarks, gating (123)
briefing       Scheduled briefings and the catch-up digest (029)
cap            CAP 1.2 output: one message per alert state change, Atom index (112)
chatops        Interactive Telegram and Matrix chat commands (061)
chronicle      Chronicles: capture into per-chronicle partitions, thread, replay (131); network: fingerprints,
               discovery, access requests (134)
codec          Shared AIVDM codec for AIS NMEA sentences (085)
collab         Principals, groups, access lists, TLP, tags, comments, attachments, evidence items, saved
               searches, personal inbox (130)
collect        Scheduler, UpstreamClient, budgets, usage meter, health tracker, streaming supervisor, sockets
config         Bootstrap configuration: properties, Jackson, CORS, clock, edge mode, startup order, version
cycle          Analysis cycles, assessments, intel board, maintenance runs (128)
czml           CZML output for CesiumJS (141)
derive         The DERIVED-source runtime: generators, observation feed, coverage, reconciliation (052, 099)
doctor         Adapter doctor: probe, structural diff, diagnosis, optional LLM patch proposal (140)
entity         Entity registry: deterministic keys, resolver, linker (098)
evidence       Originators, canonical events, observation links, evidence groups (116)
exchange       Signed intelligence packages between instances (132)
exercise       Exercise replays through selected rules (115)
export         Bulk exports, snapshots, holds, cold archive, offline packs, PMTiles writer (111)
fdsn           FDSN event web service over earthquake records (141)
federation     Global record refs, federation feed, peer sources, edge relay, compact mesh messages (113)
feed           Atom and JSON Feed for alerts and briefing runs (058)
forecast       Forecast ledger: questions, forecasts, resolvers, scoring, ensemble, evaluation,
               estimative language (122)
geo            Shared H3 geometry helpers (092)
hypothesis     Hypothesis sets, indicators, indicator-bayes updater, ACH, key assumptions (125)
ical           iCalendar feeds (112)
impact         Impact projection from hazard forecasts (127)
inbound        Custom push sources, ingest keys and formats, devices, signatures, External Data
               Protocol (edp) and bindings (024, 039, 138)
infra          Infrastructure dependency graph (104)
ingest         Single-writer ingest: normalizer, hasher, policy engine, writer and its commands (008, 009)
intel          Intelligence events, correlation recipes, scoring, investigations, exposure (101)
kinematics     CPA and ETA, the approach condition (114)
kml            KML 2.2 and KMZ (058)
llm            Provider registry, LlmClient implementations, budgets, usage, prompt templates (026)
manifest       Analysis manifests and replay (116)
map            Content-addressed sprite and MapLibre overlay style (058)
mapping        Declarative RecordMapping compiler and engine, device presets (059)
markets        CFTC Commitments of Traders history backfill (117)
mcp            MCP server, tool catalog, remote OAuth authorization (025, 141)
media          Content-addressed media store, ImageGuard, thumbnails (043); outlet registry and
               ratings (133)
mqtt           Supervised MQTT client; the mqtt channel's state topics and Home Assistant discovery (060)
net            OutboundUrlPolicy and the IANA special-purpose address table (035)
news           News articles, stories, coverage analytics (133)
notify         Channels and formatters, routing, delivery worker, Web Push and native push, inbox,
               escalation, actionable notifications, mesh channels (018, 031–033, 084)
object         Client object contract: refs, envelopes, relation graph, annotations, workspaces, sensor
               discovery (129)
observability  Detection-probability estimates and the coverage layer (116)
ogc            OGC API Features over records, CQL2 (112)
orbit          Orekit data context and SGP4 swath estimation (054)
outlook        Series forecasts, regional outlooks, regional indices (126)
place          Places, the places AOI, forecast-series cache (036)
plugin         Out-of-tree WASM parser plugins and the wasm-feed family (141)
pol            Pattern of life: subjects, stays, places, deviations (114)
precursor      Precursor mining, validation, library, live watch (120)
provenance     License catalog and resolution, egress guard, reliability and credibility, signing keys
               and JWS (109)
quality        Alert-quality signals, daily trends, tuning suggestions and log (115)
raster         Product catalog, frame store and .fsg codec, grids, decoders, models, lightning, rendering,
               sampling, GeoTIFF, raster exports (042–056, 105–108)
reference      Reference geography, gazetteer and region tagging (040)
report         Reports and renditions (132)
retention      Window resolution, the retention job and its steps, retention state, history downsampling
               (019, 044)
search         Embeddings, vector slots, reindex, semantic and hybrid search (027)
security       Tokens, principals, AccessPolicy, filters, restrictions, private visibility, feed keys,
               tickets, pairing, audit, rate limits, credential encryption (023, 035, 039)
settings       Typed application settings, export and import
share          Path-bound share links (fss_) (112)
sink           Outbound sink SPI, cursors and the sink types (110)
situation      Situations and storylines, linker, snapshots, briefs (118)
social         Social posts, volume series and baselines (135); narratives, claims, media fingerprints
               (136); social–physical comparison, attention gaps, cues (137)
source         Source SPI, descriptors, registry, settings and credentials; adapters (one sub-package per
               upstream), shared formats, series model, kits, codecs
space          Space data helpers (064)
stats          Activity rollups, stats API, glance, metric projections (038)
stix           STIX 2.1 export and read-only TAXII 2.1 (141)
store          Core JPA entities and repositories, spatial, tile, history, track, timeline and purge queries
stream         Change bus, live bus, SSE hub and topics, stream filters
system         /v1/system, feature flags, configuration warnings, health indicators (019)
trigger        Trigger rules, engine, persist gate, alerts and lifecycle (017); windowed rules and
               anomalies (window) (100)
trip           Trip and commute corridor watch (114)
view           Saved views and predicate scopes (036)
watch          Watchlists and identity resolution (036)
weather        Weather intelligence: alert weather context, briefing and Ask integration (056)
```

**Dependency rule:** `api → (service packages) → store`. Adapters depend only on `source` SPI types and `collect.UpstreamClient`; they never touch the store, the scheduler or each other. Product adapters (046 on) may also use the stateless decoders in `raster.decode`, like the shared parsers under `source.formats`; frames still reach the store only through `ctx.rasters()`. Weather record adapters (047) reach the places AOI and the `ref_admin2` county outlines only through the SPI types `AreaOfInterest` and `CountyGeometries` (implemented by `place.PlacesAoiService` and `reference.CountyUnionService`). The lightning and fire adapters (048) write GLM flashes only through `FlashSink` (`ctx.rasters().flashes()`, implemented by `raster.lightning.FlashStore`). The SatDump station adapter (049) reads its inbox only through `InboxAccess` (`ctx.inbox()`, `source.InboxFiles`), decodes images only through `MediaSink.decode` (`media.GuardedImages`), registers dynamic products and provenance copies only through `RasterSink.registerProduct` and `RasterSink.storeProvenance`, resolves a place-id location through `PlacePoints` and holds the live polar plot through `LiveImageHolder` (`media.LiveImageStore`); its polar overlays (054) read GeoTIFFs with the stateless readers in `raster.geotiff`, estimate swaths with `orbit.SwathEstimator`, warp only through `RasterSink.warpToWgs84`, read remote pass bundles through `RasterSink.uploads()` and take pushed images through `RemoteMediaReceivers` (`inbound.RemoteMediaRegistry`). The forecast model adapters (051) are thin descriptors over `raster.model.ModelRunIngest`, reaching the network only through `ctx.http()`. DERIVED generators implement `derive.DerivedGenerator` and read frames, records and places only through their `DerivedContext`; the `DerivedRunner` hands each invocation's records to the writer as one batch. `ai.foresignal.derive` itself knows no weather type.

## 4. Source adapter SPI

### 4.1 Interfaces and ingest input

```java
public interface SourceAdapter { SourceDescriptor descriptor(); }

public interface PollingAdapter extends SourceAdapter {
    /** Fetch once. All network access MUST go through ctx.http(). */
    FetchResult fetch(FetchContext ctx) throws AdapterException;
}

public interface StreamingAdapter extends SourceAdapter {
    /** Open the stream and push records to the sink until the handle is closed. */
    StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException;
}
```

`Mode` is `POLLING`, `STREAMING`, `PUSH` (custom and device sources, §16; the scheduler never polls them) or `DERIVED` (generators fed by the change bus, frame events or ticks; §27.13, §30).

```java
public record NormalizedRecord(
    String externalId, String kind,
    Instant observedAt,              // required unless observedAtMode=RECEIVED
    Instant eventTime,               // nullable; discrete events and measurement periods only
    Geometry geometry,               // JTS, SRID 4326, nullable
    Instant validUntil,              // nullable; ONLY an upstream-asserted expiry
    Map<String, Object> payload,     // the COMPLETE snapshot
    boolean tombstone,
    Quality quality, List<Observer> observers) {}
public record Quality(Double accuracyMeters, Double confidence, PositionSource positionSource, Map<String,Object> extra) {}
public record Observer(String receiverId, Long frequencyHz, Double rssi, Double snr, Double decodeQuality) {}
public record Coverage(Integer receivers, Integer expected, Integer actual, Envelope region) {}
public record FetchResult(List<NormalizedRecord> records, boolean complete, boolean notModified, Coverage coverage) {}
```

`FetchResult.unchanged()` builds the 304 result (the accessor is `notModified()`). A complete-set reference dataset (094) may ask the writer to tombstone the records a successful run no longer saw (`TombstoneAbsent`, §6.8).

`FetchContext` exposes `http()` (the budgeted `UpstreamClient` bound to the source), `params()` (validated adapter parameters), `credentials()` (decrypted, keyed by credential field), `clock()`, `lastSuccessAt()`, the per-source key/value `state()` persisted in `source_health.adapter_state`, and the product, media and inbox sinks of the weather part (`rasters()`, `media()`, `inbox()`).

**`SourceDescriptor`** carries the id (stable, kebab-case), name, description, kinds, homepage, attribution, mode, credential fields, parameter specs, `minInterval`, `defaultInterval`, `defaultBudget` (`perMinute`, `perDay`, optional `mbPerDay`), `recordTtl`, `zeroResultsNormal`, `tileProperties`, `searchFields`, `embedFields`, and the fields below. Later prompts added `products` (042), `kindRetention`, `kindDisplay` (058), `entityKeys` (036, 098), `private` (039), `egress` (062), `licenceTerms`, `reliability` and `derivedFrom` (109), `originator`, `upstreamRefFields` and `observability` (116) and `hostPacing`.

| Field | Default | Rule |
|---|---|---|
| `licence`, `termsUrl` | null | Exposed by `/v1/sources`; snapshotted on every fetch row |
| `historyTier` | STANDARD | Fixed, not overridable. TRACK requires `recordTtl` and `ttlAnchor=OBSERVED` |
| `observedAtMode` | UPSTREAM | Not overridable (§6.4) |
| `ttlAnchor` | OBSERVED for TRACK, RECEIVED for STANDARD | Not overridable |
| `defaultIngestPolicy` | ALL | §6.5 |
| `volatileFields` | [] | Top-level keys left out of hashes and `kinematic` |
| `significantFields` | [] | Empty means every non-volatile key is significant. Required for SAMPLED and LATEST |
| `motion` | null | `{speedField, speedUnit, courseField}` (not significant) |
| `thresholds` | [] | `[{field, delta, angular}]` (not significant) |
| `defaultRetention` | null | STANDARD only; a TRACK source with it fails registry validation |
| `skewTolerance` | 2 min | Flag and metric only |
| `allowNullIsland` | false | Keep an exact (0,0) point |

**Framework types:** `IngestRecord(record, receivedAt, arrival)`; `IngestBatch(sourceId, FetchLogEntry log, records, future)`; the sealed `IngestCommand = IngestBatch | HeadPurge | EntityPurge | TombstoneAbsent` (§6.8).

### 4.2 Adapter rules

1. Self-contained: one package per adapter containing the adapter class, its parser and DTOs. No cross-adapter imports; shared parsers live under `ai.foresignal.source.formats.*` (readsb JSON, CAP 1.2, AIVDM, GeoJSON, KISS/AX.25 and others).
2. No direct `HttpClient` construction: always `ctx.http()`.
3. Parsers are pure functions (`bytes/string → List<NormalizedRecord>`), testable against fixtures without network access.
4. Each adapter ships a fixture under `src/test/resources/fixtures/<source-id>/`, a parser contract test and a WireMock-backed adapter test. Fixtures built without a key are marked `synthetic: true` in their README.
5. Adapters are Spring `@Component`s; the `SourceRegistry` discovers them by type and fails startup on a duplicate id. **Adapter families:** when one parser serves many feeds (GTFS-RT agencies, CCTV packs, GBFS systems, adsb.lol regions, local receiver slots, peers), a `SourceAdapterFamily` bean returns one adapter per feed from a static, code-reviewed list or operator configuration validated at startup. Each feed is a separate source with its own settings, budget and health. Feed URLs are never supplied by API callers (no SSRF).
6. Code ported from God's Eye View carries a header comment naming the origin file and the MIT notice; `THIRD_PARTY_NOTICES.md` lists it (§26).
7. The payload is a complete snapshot. Protocols with split messages merge state per entity; test with `SnapshotContract.assertStableStaticHash`.
8. Slim, allowlisted payloads with no nulls. Leave out query-relative fields. Raw counters are left out or declared volatile.
9. `validUntil` only for an upstream-asserted expiry. TRACK sources must not set it, and asOf ignores it there.
10. `eventTime` only for discrete events and measurement periods.
11. Scrub sentinels (AIS lat 91 / lon 181, SOG 102.3, heading 511).
12. Quality transitions that matter go into significant fields the adapter computes (for example `navDegraded`). Quality is never hashed.
13. The time base is fixed per source (§6.4). Never switch an entity between timestamp fields from record to record.

### 4.3 Limits and quality flags

- Payload ≤ 64 KB, depth ≤ 8, ≤ 512 keys, strings ≤ 8 KB. Geometry ≤ 10,000 vertices, checked before `GeometryFixer`. No NaN or Infinity. ≤ 8 observers.
- An exact (0,0) point becomes null geometry with flag `NULL_ISLAND`, unless the source sets `allowNullIsland`.
- A violation rejects that record only, with a reason.
- `qa_flags` (`ai.foresignal.ingest.QaFlags`): 1 `FUTURE_CLAMPED`, 2 `GEOMETRY_FIXED`, 4 `NULL_ISLAND`, 8 `TIME_IMPUTED`, 16 `POSITION_STALE`, 32 `PURGED`.
- **Geometry carry-forward (TRACK only):** a non-tombstone observation with null geometry whose running state has a geometry inherits it and gets `POSITION_STALE`, before hashing, so a loss of position never flips `static_hash`.
- **Tombstone normalization:** a tombstone is stored with `geom = NULL`, `payload = {}`, `kinematic = {}`, and its kind kept.

## 5. Collection

### 5.1 CollectorScheduler

- On startup, and every **2 seconds** (reconcile tick), it compares the enabled sources in `source_settings` with its running set: a newly enabled polling source is scheduled immediately and then every `interval`; a newly enabled streaming source goes to `StreamingSupervisor.start(source)`; a disabled or deleted source has its future runs canceled and its stream closed. In-flight fetches finish, but their results are discarded (a `DISCARDED` fetch row) if the source was disabled before commit.
- Setting changes made through the API call `scheduler.reconcileNow()`, so they apply at once.
- One platform thread drives timing; each fetch runs on a **virtual thread**. A per-source flag prevents overlapping fetches; a run that would overlap is skipped and counted.
- Before each run the scheduler checks `HealthTracker.backoffUntil(source)`.
- Each fetch has an overall timeout (default 60 s, per-descriptor override).
- `FetchExecutor` stamps `receivedAt`, builds the `FetchLogEntry` from the request trace and coverage, writes `FAILED`, `REFUSED`, `NOT_MODIFIED` and `DISCARDED` rows itself, and updates `last_data_at` on OK fetches with records.
- Startup order (`ai.foresignal.config.StartupOrder`): Flyway migration → history partitions → writer (lock, state, clock guard) → trigger engine → embedding and derived workers and other cursored consumers → scheduler.

### 5.2 UpstreamClient and BudgetGovernor

`UpstreamClient` is the **only** path to the network. For each request:

1. `BudgetGovernor.tryConsume(sourceId)` checks two Bucket4j bandwidths, `perMinute` (greedy refill) and `perDay` (interval refill at UTC midnight), plus the optional `mbPerDay`, charged in advance from the known size (S3 `Size`, `.idx` offsets) and corrected afterwards. A refusal throws `BudgetExceededException`, the meter records `refused`, and no request is sent.
2. Send with a per-request timeout (default 20 s) and gzip accepted. The `User-Agent` is `Foresignal/<version> (+https://foresignal.ai; install=<id>)`, where `<id>` is a random 8-character install id generated on first start (in `app_setting`). Upstreams can then rate-limit or contact one misbehaving install without blocking every Foresignal user. Browser User-Agents are never spoofed.
   - **Conditional GET:** `ctx.http().getConditional(url, stateKey)` stores `ETag`/`Last-Modified` in `adapter_state`; a `304` returns `NotModified`, counts as a success with zero changes, and never triggers zero-result degradation.
   - **Response cap:** bodies are read with a byte cap (default 32 MB, per-descriptor override).
   - **Ranges and files (042):** `getRange(url, start, end, maxBytes)` accepts only 206 (or 200 when start is 0) and rejects short bodies; `download(url, Path tmp, maxBytes)` streams to `rasters/tmp`; `listS3(bucket, prefix, startAfter)`.
   - **Host pacing:** descriptors that share a host declare `hostPacing`, and a shared `HostPacer` keeps their requests serial (080, 133).
3. `UsageMeter.record(sourceId, bytes, status)`.
4. HTTP 429/503 → `HealthTracker.backoff(sourceId, retryAfter)`; exponential backoff (30 s × 2ⁿ, at most 1 h) without `Retry-After`.
5. Non-2xx → `UpstreamHttpException` with the status and a truncated body.

Budgets are rebuilt when settings change. On startup the daily bucket is seeded from today's `source_usage`, so a restart cannot reset the daily allowance. **Streaming budgets:** a "request" is a connection attempt or subscription message, so the budget bounds reconnect storms; adapters declare `maxSubscriptions` where relevant (no wildcard fan-out). `LAN_URL` parameters resolve once, must be loopback, RFC 1918, link-local or ULA, and connect to the pinned address with the original Host header and redirects off. `INBOX_PATH` parameters must resolve under `FORESIGNAL_INBOX_DIR`, and every scanned file is opened with `NOFOLLOW_LINKS` and its real path re-checked.

### 5.3 UsageMeter

In-memory `LongAdder` counters per `(source, minute)`, flushed to `source_usage` every 60 s and on shutdown with an accumulating upsert. Besides `requests`, `bytes`, `errors` and `refused`, the writer adds `records_seen`, `versions`, `live_updates`, `touches`, `late`, `skipped`, `rejected` and `version_bytes` (`UsageMeter.recordIngest`). Unflushed counts are merged into API responses.

### 5.4 HealthTracker

Persists `source_health` (one row per source) on every run: last attempt, last success, last error (message, class, HTTP status), consecutive failures, records seen and changed in the last fetch, the recent record counts, `backoff_until`, `last_data_at` (the last fetch or flush with records, never a 304), `last_coverage` and `recent_coverage` (a ring of 20).

| Status | Rule |
|---|---|
| `disabled` | source not enabled |
| `idle` | enabled, no attempt yet |
| `failing` | ≥ 3 consecutive failures, or in backoff |
| `degraded` | last run failed (< 3 consecutive), or the last success had 0 records while ≥ 3 of the previous 5 had records (unless `zeroResultsNormal`), or > 20% budget refusals in the last hour, or coverage dropped (receivers or expected down ≥ 50% against the median of the last 20), or a frame-age, format-change or other health rule fired |
| `healthy` | otherwise |

The health view adds `coverage`, `lastDataAt`, the `storage` block (§7.4), frame age for product sources, and the latest doctor report (§41).

### 5.5 StreamingSupervisor and streaming sinks

The supervisor owns stream handles. On disconnect it reconnects with exponential backoff, and each attempt consumes budget. Records go to a policy-aware sink (`PolicyAwareRecordSink`):

- One outstanding batch per streaming source. Flush at 2 s or 2,000 entries, once the previous future has completed.
- **SAMPLED and LATEST:** a map keyed by `externalId`; the greatest `observedAt` wins, ties go to the later arrival.
- **ALL:** an ordered list capped at `allBufferMax` = 50,000. At the cap, `accept` blocks for up to 30 s, then drops and counts `foresignal.stream.records.dropped`.
- `receivedAt` is stamped on accept. Each flush produces one `fetch_log` row (`trigger=STREAM`) and updates `last_data_at`.
- **Documented limit:** under SAMPLED and LATEST, trigger edges and samples have *flush resolution* for streams (≥ 2 s, longer while a batch is in flight) and *poll resolution* for polling sources.

## 6. Ingest and the change sequence

### 6.0 Invariants

| Id | Invariant | Enforced by |
|---|---|---|
| I1 | Only the writer thread inserts `record_version` and writes `record_head`, `record_live` and `seq_clock`. Retention may delete versions and drop leaves; head purges run on the writer. | Code structure; with `foresignal.ingest.writer.exclusive=true` (production) `pg_try_advisory_lock` on a dedicated non-pooled connection |
| I2 | `seq` is allocated inside the writer transaction by the single allocator; every seq of a batch is greater than every seq allocated earlier, including rolled-back allocations | §6.2 step 7, `lastAllocatedSeq` |
| I3 | `ingested_at` never decreases with seq | `ingestedAt = max(clock.now, lastIngestedAt)`, state loaded from the database, clock guard |
| I4 | Per entity, accepted `observed_at` never decreases | Fixed time base per source, future clamp, LATE drop, in-batch sort |
| I5 | Current-state reads touch only `record_head ⋈ record_live` | §9 |
| I6 | A continuously observed entity has a persisted version no older than `effectiveKeyframe + pollInterval` on its anchor axis, `effectiveKeyframe = min(policy keyframe, window/4)` | Keyframe and retention-keyframe rules, validator |
| I7 | A head is purged only when every version of it is gone | `h.seq < floorSeq(source)` (§11) |
| I8 | For every enabled rule, after each observation, P(head state) = P(latest state), except a flip held back by `edgeMinInterval`, which is persisted on the first observation after the interval | `PersistGate` compares against persisted state (§8.3) |
| I9 | `record_version` is changed only by native SQL | `NoJpaWritesToRecordVersionTest` |
| I10 | Every head has exactly one live row, and the reverse | Writer upserts both; the load query repairs a missing live row and counts `foresignal.ingest.repair.live_missing` |

The machine-checked index of these and every later invariant, with their tests, is [Invariants](Invariants.md).

### 6.1 Single writer: lifecycle and state

All writes to `record_version`, `record_head`, `record_live` and `seq_clock` happen on **one dedicated platform thread**, `foresignal-ingest-writer`, consuming a bounded `IngestQueue` (`foresignal.ingest.queue-capacity`, 64 commands), started on `ApplicationReadyEvent`. Polling fetch threads block when the queue is full (natural back-pressure); streaming sinks coalesce while waiting.

- **Why one writer:** `seq` is **monotonic in commit order** (not gap-free: rolled-back allocations leave harmless gaps), so it is a reliable "greater than" cursor for SSE resumption, trigger catch-up, `/v1/changes` and every durable consumer. One transaction per batch, with JDBC batches.
- **Lock** (when `exclusive=true`): `pg_try_advisory_lock(hashtext('foresignal.ingest.writer'))` on a dedicated connection opened with `DriverManager` outside Hikari, kept alive with `SELECT 1` every 30 s. A lock held elsewhere fails startup with a clear error. On connection loss the writer pauses, reacquires with backoff and raises `foresignal.ingest.writer.lock_lost`.
- **State init** (`resetState()`, also called by `DatabaseCleaner` in tests): `lastSeq` and `lastIngestedAt` from the newest version, `lastIngestedAt` raised to `max(seq_clock.minute, fetch_log.ingested_at)`, `lastAllocatedSeq` from `SELECT last_value FROM record_seq`; the migrated-recipe map is cleared.
- **Clock guard:** at start, if `clock.now < lastIngestedAt − 5 min`, batches wait (queue fills, producers back off, health alarm `writer.clockBehind`, re-check every 30 s). At run time each batch compares the wall-clock delta with `System.nanoTime`; a divergence above 5 min counts `foresignal.ingest.clock.jump{direction}` and warns. Backward jumps are absorbed by the I3 clamp (`foresignal.ingest.clock.clamped`). Ingest is never stopped for a forward jump.
- **Profiles:** `SourceIngestProfile` is cached and refreshed on settings events; the rule snapshot is read once at batch start from `PersistGate`.
- Each batch transaction uses `SET LOCAL lock_timeout` (`foresignal.ingest.writer.lock-timeout`, 10 s); a timeout is retried.

### 6.2 Batch steps

1. **Pre-flight:** if `lastAllocatedSeq + n ≥ coveredUpTo(tier)`, call `PartitionManager.ensureFor(tier, lastAllocatedSeq + n)` inline and fail the batch only if coverage is still insufficient; under one block of headroom, request an async ensure and warn.
2. **Normalize** (§4.3, §6.4): time base, clamp, limits, flags, explicit and effective `validUntil`. Duplicates are kept.
3. **Stable-sort** by `(externalId, observedAt, arrival)`.
4. **Recipe check** (§6.7) when the source is not yet migrated.
5. **Begin** the transaction; `ingestedAt = max(clock.now, lastIngestedAt)`.
6. **Load state** with one query over `record_head h LEFT JOIN record_live l USING (source_id, external_id)` for the batch's external ids (persisted payload paths for motion, thresholds and the gate; persisted and live bounding boxes; live geometry for TRACK), repair a missing live row (I10), then fold each entity through the policy engine (§6.5), updating the running state after each outcome (carry-forward needs the running geometry).
7. **Allocate seq:** `SELECT nextval('record_seq') FROM generate_series(1,:n)`, sorted in Java and assigned in order; `version = running.version + 1`, `prev_seq = running.seq`.
8. `INSERT INTO fetch_log … RETURNING id` gives `batch_id`.
9. **Version insert:** `JdbcTemplate.batchUpdate`, geometry through `ST_GeomFromWKB(?,4326)`, jsonb through `?::jsonb`.
10. **Head upsert** (`ON CONFLICT (source_id, external_id) DO UPDATE`), one row per entity with at least one PERSIST (asserted): kind, seq, version, event_time, valid_until_explicit, tombstone, static_hash, hash_recipe, persisted observed/received times and geometry, payload, quality, search_text.
11. **Live upsert**, one row per entity with at least one PERSIST or LIVE: on PERSIST `kinematic` is the kinematic subset of the persisted payload and `dirty = false`; on LIVE it is the incoming kinematic keys plus JSON null for keys that disappeared, and `dirty = true`; `last_received_at` is the batch maximum; tombstones get null geometry and `{}`.
12. **Touch:** `UPDATE record_live SET observed_at, last_received_at, valid_until … WHERE … AND observed_at <= ?`, one row per key; the summed update counts must equal the row count, or the batch fails.
13. `INSERT INTO seq_clock(minute, first_seq) … ON CONFLICT DO NOTHING` on **every** batch.
14. **Commit**; advance `lastSeq` and `lastIngestedAt`.
15. **Publish** the `ChangeBatch`, then the `LiveBatch`, then complete the future, then `UsageMeter.recordIngest`.

**Retry:** on SQLState `08*`, `40*`, `55P03` or `57014`, roll back and retry the whole batch up to 3 times (0.5, 1, 2 s), re-folding from fresh state. Other errors, or exhausted retries, fail the future; the thread never dies.

**IngestOutcome:** `seen, persisted, live, touched, touchesThrottled, late, skipped, rejected, fromSeq, toSeq, byReason` (the nine persist reasons). `version_bytes` = UTF-8 length of payload, quality and observer JSON + 120.

**Metrics:** `foresignal.ingest.records.{seen,persisted,live,touched,touches_throttled,late,skipped,clamped,rejected}{source}`, `foresignal.ingest.persist{source,reason}`, `foresignal.ingest.batch.duration{phase}`, `foresignal.ingest.batch.retries`.

### 6.3 Sequence and seq_clock

`record_seq` is a PostgreSQL sequence (increment 1) created by the Flyway baseline. `RecordVersionEntity` is `@Immutable` and never inserted by Hibernate. The writer allocates a block per batch (step 7).

`seq_clock(minute PK, first_seq)` gets an insert on every batch and is pruned by retention to (largest window + 1 d). `cutoffSeq(t)` = `first_seq` of the latest minute ≤ t: every row below it was ingested before t. `S(T)` = the largest seq in minute `m` (the latest minute ≤ T) with `ingested_at ≤ T`, else `first_seq(m) − 1`. Both translate wall-clock times to seq bounds for asOf, history `timeField=ingested`, retention, backtests and exercises.

### 6.4 Time contract

| Column | Meaning | Set by | Hashed | Drives |
|---|---|---|---|---|
| `event_time` (nullable) | When a discrete real-world event happened, or a measurement period | Adapter | Yes | `effective_time`, `{{eventTime}}` |
| `observed_at` (required) | When upstream last observed or updated this state | Adapter, or the framework under `observedAtMode=RECEIVED` | No | LATE check, SAMPLED Δt, OBSERVED-anchor TTL and keyframes, client last-writer-wins, cooldown, playback |
| `received_at` | When Foresignal received the response, message or push (the sink stamps streams) | Framework | No | Clamp, RECEIVED-anchor TTL and keyframes, retention keyframe, touch throttle, `last_received_at` |
| `ingested_at` | Batch timestamp, monotonic | Writer | No | asOf, retention through `seq_clock` |

`effective_time = COALESCE(event_time, observed_at)`.

- **`observedAtMode`:** `UPSTREAM` (default; a record without `observedAt` is rejected `MISSING_OBSERVED_AT`) or `RECEIVED` (the framework sets `observed_at = received_at` on every record and flags `TIME_IMPUTED`). Never mixed within a source.
- **`ttlAnchor`:** `OBSERVED` or `RECEIVED` (TRACK must be OBSERVED; STANDARD defaults to RECEIVED). Effective `valid_until = explicit ?? anchor + ttl`.

| Adapter type | `event_time` | `observed_at` | Notes |
|---|---|---|---|
| readsb snapshots (adsb.lol etc.) | null | `now − seen`, always | Geometry `lat/lon`, else `lastPosition`; `quality.extra["adsb.seen_pos"]` |
| OpenSky states | null | `last_contact`, always | `quality.extra["opensky.time_position"]` |
| Streaming (AIS, MQTT) | null | Message time of the newest merged message | Merged snapshot (rule 7) |
| GTFS-RT | null | Vehicle `timestamp`, else header `timestamp` (fixed per feed) | |
| Revised events (USGS, EMSC, GDACS) | Origin time | Upstream `updated` | ttlAnchor RECEIVED if a TTL is set |
| FIRMS | Acquisition time | Acquisition time | |
| NWS | Onset if present | `sent` / `updated` | `validUntil = expires` |
| Catalogs and reference data | null | The descriptor picks UPSTREAM or RECEIVED, never a per-record fallback | |
| Measurements (OpenAQ, NDBC, EIA) | Measurement period | Measurement time | ttlAnchor OBSERVED |
| Push (024) | Per custom-source configuration | Per custom-source `observedAtMode` | |

**Normalizer rules:** (1) if `observed_at > received_at`, set `observed_at = received_at`; only an excess above `skewTolerance` sets `FUTURE_CLAMPED` and counts `foresignal.ingest.records.clamped{source}`; (2) reject `observed_at < 1970-01-02`; (3) in the writer, `observed_at < l.observed_at` is **LATE** and dropped with no side effects; equal timestamps are accepted in arrival order.

### 6.5 Ingest policies

```json
{"mode":"SAMPLED","minInterval":"60s","keyframe":"240s","minDeviationMeters":1000,"thresholds":true,"edgeMinInterval":"30s"}
{"mode":"LATEST","keyframe":"20m","edgeMinInterval":"15s"}
{"mode":"ALL","keyframe":"4m"}      // default: ttl − pollInterval − 30 s; "none" without a TTL
```

`IngestPolicyValidator` runs on descriptor defaults at registry start and on `source_settings.ingest_policy` at PUT (`400 INGEST_POLICY_INVALID`): `2 s ≤ minInterval ≤ keyframe`; `edgeMinInterval ≤ minInterval` (default `minInterval / 2`); with a TTL, `keyframe ≤ ttl − pollInterval − 30 s` (pollInterval 2 s for streams; a default ≤ 0 fails the registry); `keyframe: none` only without a TTL; SAMPLED and LATEST require `significantFields`; threshold and motion fields must not be significant; non-significant `searchFields` and `tileProperties` warn.

**Decision function** (pure `IngestPolicyEngine`). With `Δt = observed_at − h.persisted_observed_at`, `keyframeDue = (anchor(obs) − h.persisted_anchor ≥ keyframe) OR (received_at − h.persisted_received_at ≥ window/4)` and `expired = l.valid_until IS NOT NULL AND l.valid_until < anchor(obs)`:

| # | Condition | Outcome (`persist_reason`) |
|---|---|---|
| 1 | `observed_at < l.observed_at` | **LATE** |
| 2 | tombstone, and (no head, or head tombstoned) | **SKIP** |
| 3 | tombstone | PERSIST `TOMBSTONE` (4) |
| 4 | no head | PERSIST `FIRST` (1) |
| 5 | head tombstoned, or `expired` | PERSIST `REAPPEAR` (2) |
| 6 | `static_hash ≠ h.static_hash` | PERSIST `SIGNIFICANT` (3) |
| 7 | policy ALL and `full_hash ≠ l.full_hash` | PERSIST `CHANGE` (5) |
| 8 | `keyframeDue` | `HEARTBEAT` (8) if `full_hash = l.full_hash AND NOT l.dirty`, else `KEYFRAME` (7) |
| 9 | the gate reports P(candidate) ≠ P(persisted) for an applicable rule, and `Δt ≥ edgeMinInterval` | PERSIST `TRIGGER_EDGE` (9) |
| 10 | SAMPLED, `Δt ≥ minInterval`, and (deviation ≥ minDeviation or a threshold exceeded) | PERSIST `SAMPLE` (6) |
| 11 | `full_hash = l.full_hash` | **TOUCH** if `received_at − l.last_received_at ≥ touchMin` OR `observed_at − l.observed_at ≥ touchMin`, else **TOUCH_THROTTLED** |
| 12 | otherwise | **LIVE** |

`touchMin = clamp(ttl/10, 30 s, 1 h)`, or 1 h without a TTL. `LATEST` is SAMPLED without row 10. Deviation is dead reckoning from the persisted centroid with persisted speed and course, else haversine; 0 without geometry. Thresholds compare against `h.payload`, with angular wrap where set. Rows 1–12 ship as a parameterized unit-test matrix (`IngestPolicyEngineTest`).

**Defaults** (Conventions overrides the earlier 5-minute aircraft TTL):

| Family | Policy | minInt | keyframe | TTL | Deviation | Thresholds | edgeMin | Tier |
|---|---|---|---|---|---|---|---|---|
| Polled global aircraft (`adsblol-*`, `airplaneslive-*`, `adsbfi-*`) | SAMPLED | 60 s | 240 s | 6 min | 1,000 m DR | alt_baro 1,000; track 20° | 30 s | TRACK |
| `opensky-states` | SAMPLED | | | 15 min (300 s poll) | | | | TRACK |
| `local-adsb-*` | SAMPLED | 10 s | 120 s | 5 min | 300 m DR | alt_baro 500; track 10° | 5 s | TRACK |
| Vessels | SAMPLED | 30 s | 1,200 s | 30 min | 250 m DR | cog 15°; sog 3 kn | 15 s | TRACK |
| Transit and rail | SAMPLED | 15 s | 240 s | 5 min | 150 m | bearing 30° | 8 s | TRACK |
| Everything else | ALL | — | ttl − poll − 30 s, or none | per source | — | — | — | STANDARD |

Significant fields: aircraft `flight, r, t, squawk, category, emergency, onGround, navDegraded`; vessels `name, callsign, imo, shipType, destination, navStatus, draught`. The design budget is about 10,000 versions per minute (NFR-2: 40,000 observations per minute, ≤ 20,000 persisted versions per minute); a `TRIGGER_EDGE` share above 20% of a source's versions over 1 h warns.

### 6.6 Provenance, quality, observer, coverage

- `record_version.batch_id → fetch_log.id` (no FK). License id, adapter version and principal (for pushes) are snapshotted on the fetch row; per-record license resolution is §33.
- **Quality:** validated (`accuracyMeters ≥ 0`, `confidence ∈ [0,1]`, `extra` keys `^[a-z0-9]+\.[a-z0-9_]+$` with scalar values); stored on versions, head and live; never hashed.
- **Observer:** ≤ 8, on versions only; returned only with `include=observer`, to agent or admin tokens, or with `security.exposeObserver=true`.
- **Coverage:** `fetch_log.coverage` and `source_health.last_coverage` / `recent_coverage`; the silent-break rule reports "coverage dropped" when receivers or expected fall by ≥ 50% against the median of the last 20.

### 6.7 Hashing

Canonical JSON (`CanonicalJson`), hash = the first 8 bytes of SHA-256 as a signed bigint.

| Input | `full_hash` (on `record_live`) | `static_hash` (on `record_head`) |
|---|---|---|
| kind, event_time, explicit validUntil, tombstone | ✓ | ✓ |
| geometry (WKT at 1e-7, after carry-forward) | ✓ | `geom IS NULL` only |
| payload minus volatile keys | ✓ | significant keys only (all non-volatile when the list is empty) |
| observed/received/ingested times, derived valid_until, quality, observers | ✗ | ✗ |

The **recipe** = hash(hasher version, sorted volatile list, sorted significant list), stored per head (`record_head.hash_recipe`). **Lazy migration** on the writer thread: at the first batch for a source after start, or after its recipe changes (a custom-source edit), `SELECT 1 FROM record_head WHERE source_id=? AND hash_recipe<>? LIMIT 1`; if a row exists, heads are migrated in chunks of 2,000 in their own transactions before the batch (`RecipeMigrator`): `static_hash` recomputed from the head columns (exact, because the head holds the full persisted state), `kinematic` and `full_hash` recomputed from the merged payload and live geometry, `dirty` unchanged.

### 6.8 Writer commands

- **`HeadPurge(List<PurgeSpec(sourceId, floorSeq, staleBefore)>)`:** in chunks of 10,000, heads with `seq < floorSeq` whose live row was last received before `staleBefore` lose their live and head rows in one transaction; when more remain, the command re-enqueues itself at the queue tail so batches interleave. Only head purges advance `RetentionState.purgeSeq` (clients get `410 RESYNC_REQUIRED` only when `after < purgeSeq`).
- **`EntityPurge(sourceId, externalId)` (039):** submitted by `POST /v1/records/{sourceId}/{externalId}/purge` (admin), `purgeDevice` and the MCP `purge_entity` tool. In one transaction it locks the head (none → not found), allocates one seq, writes a `fetch_log` row and a tombstone version (`persist_reason` TOMBSTONE, qa flag `PURGED`, empty payload, `observed_at` never earlier than the entity's last), deletes every older version of the entity on the `(source_id, external_id, seq)` index of both tiers, and sets head and live to the tombstone state. The change is published with `purged: true`; a later `HeadPurge` removes the head under I7. A `PurgeFanout` then deletes embedding rows, redacts or deletes referencing alerts, redacts stored notification bodies, drops region rows and metric series, cascades to pattern-of-life data (`EntityPurgeListener`), and counts (does not edit) Ask messages and briefing runs that cite the entity. Messages already delivered to external channels cannot be recalled.
- **`TombstoneAbsent` (094):** after the last chunk of a successful complete-set run, the non-tombstoned heads of the listed kinds whose live row predates the run are tombstoned through the normal fold in chunks of 10,000; a shrink guard refuses when more than `maxAbsentRatio` would go at once, until the operator passes `allowShrink=true` on `POST /v1/sources/{id}/fetch`.

## 7. Data model

### 7.1 Tables

The schema is owned by Flyway (§25): `V1__baseline.sql` creates every table, sequence, extension and index; Hibernate maps 238 entities and only validates them. The core history tables:

**`record_version`** (partitioned, §7.3; columns in order): `seq bigint, ingested_at, observed_at, received_at timestamptz, event_time, valid_until, valid_until_explicit timestamptz null, prev_seq, batch_id bigint null, version int, history_tier, persist_reason, qa_flags smallint, tombstone bool, source_id varchar(64), external_id varchar(256), kind varchar(64), geom geometry(Geometry,4326) null, payload jsonb, quality, observer jsonb null`. PK `(seq, history_tier)`; partitioned indexes `(source_id, external_id, seq)`, `(source_id, observed_at)` and GiST `(geom)`; no content hash, no `ingested_at` index, no unique constraint. `RecordVersionEntity` is `@Immutable`, maps smallints as `Short`, and declares no `@UniqueConstraint` or `@Index`.

**`record_head`** (written only on PERSIST, migration and purge): PK `(source_id, external_id)`; `kind, seq, version, event_time, valid_until_explicit, tombstone, static_hash bigint, hash_recipe int, persisted_observed_at, persisted_received_at, persisted_geom` (no index), `payload jsonb, quality jsonb, search_text`. Index `(kind)` only.

**`record_live`** (hot): PK `(source_id, external_id)`; `observed_at, last_received_at, valid_until, geom, full_hash bigint, dirty bool, kinematic jsonb not null default '{}', quality jsonb, qa_flags smallint`. GiST `(geom)` only. **No index on `observed_at`, by design**, so touches stay HOT; `fillfactor=70, autovacuum_vacuum_scale_factor=0.02, autovacuum_vacuum_cost_limit=2000`. **Current payload** = `jsonb_strip_nulls(h.payload || l.kinematic)`.

**`fetch_log`:** identity `id`, `source_id`, `trigger` (SCHEDULED, MANUAL, STREAM, PUSH), `status` (OK, NOT_MODIFIED, FAILED, REFUSED, DISCARDED), `started_at, received_at, ingested_at`, `http_status, request_count, url_redacted, etag, bytes, body_sha256`, `adapter_version, licence, principal`, `records_seen, rejected, late, skipped, versions_written, live_updates, touches`, `from_seq, to_seq, coverage jsonb, error`. Index `(source_id, started_at DESC)`.

**`seq_clock`** (§6.3) and **`record_seq`**. **`record_version_held`**: copies of versions under a hold (§35).

**Source state:** `source_settings` (`enabled, interval_seconds, params jsonb, budget_per_minute, budget_per_day, retention, ingest_policy jsonb, downsample_interval, reliability, …`), `source_credential` (AES-GCM ciphertext and nonce), `source_health` (§5.4), `source_usage` (PK `(source_id, minute)` with the counters of §5.3).

**Alerting:** `trigger_rule` (`name, enabled, source_id, kind, conditions jsonb, geofence, mode, only_new, cooldown_seconds, cooldown_scope, near, reappear_as_enter, severity, enrich, enrich_radius_km, message_template, muted_until, escalation_policy_id, type, window_spec, …`), `alert` (`rule_id, seq, source_id, external_id, fired_at, observed_at, record_snapshot jsonb, context jsonb, region, severity, lifecycle state, summary, enriched_at, feedback, exercise, exercise_run_id`, the `alert_rev` sequence and `alert_open_idx`), `trigger_anchor_state`, `window_state`, `activity_sample`, `anomaly_observation`, `escalation_policy`, `alert_escalation`.

**Notifications:** `notification_channel`, `notification`, `notification_delivery`, `notification_read`, `notification_read_cursor`, `client_presence`, `action_capability`, `push_subscription`, `push_dispatch`, `push_provider`, `push_device`, `chatops_cursor`, `chatops_pending`, `cap_message`.

**Security and settings:** `api_token` (scope, hash, prefix, expiry, revocation, device metadata, restriction columns, `type` = token, feed key or tile key, `prefixes`, `private_access`, `licence_policy`, federation and exercise restrictions), `principal`, `pairing_code`, `audit_event`, `app_setting`, `system_secret`, `signing_key`, `share_link`.

**Other subsystems** (one line each; details in their sections): inbound `custom_source`, `ingest_device`, `external_source`, `edp_registration_token`, `edp_snapshot`, `edp_snapshot_stage`, `websub_subscription`, `dcp_location`; LLM and search `llm_provider`, `llm_usage`, `embedding_document` (generated `tsv` with GIN, GiST, `(source_id, effective_time)`; vector slots per §20), `ask_thread`, `ask_message`, `ask_action`, `briefing`, `briefing_run`; places, views and reference `place`, `forecast_series_cache`, `saved_view`, `watch_list`, `watch_list_item`, `ref_country`, `ref_admin1`, `ref_admin2`, `ref_sea`, `ref_dataset`, `record_region`, `gazetteer_country`, `gazetteer_admin1`, `gazetteer_place`, `activity_rollup`, `history_downsample`, `retention_state`; weather `raster_product`, `raster_grid`, `raster_frame`, `raster_export_job`, `media`, `media_tombstone`; the intelligence and interop tables listed in §29–§42.

### 7.2 Secrets

- **Admin token:** 32 random bytes, base64url, prefixed `fsr_`, generated on first start, printed once in a delimited log block; only the SHA-256 hash is stored, as the `api_token` row named `bootstrap` with scope `admin`. Rotation: `POST /v1/system/admin-token/rotate` returns a new token once. Recovery: `FORESIGNAL_RESET_ADMIN_TOKEN=true` regenerates and prints a new one.
- **Credential encryption:** AES-256-GCM with a key generated on first start and stored on the data volume (`${FORESIGNAL_DATA_DIR}/secret.key`, mode 0600), never in the database, so a dump alone reveals no upstream keys. Channel, provider, sink, archive and peer secrets use the same `CredentialCipher`; the social salt is stored with the secret key material.

### 7.3 Partitioning and schema ownership

```
record_version          PARTITION BY LIST (history_tier)
├── record_version_std  FOR VALUES IN (0) PARTITION BY RANGE (seq)   blocks of 250,000,000 (≈17 d at 10k/min)
└── record_version_trk  FOR VALUES IN (1) PARTITION BY RANGE (seq)   blocks of 20,000,000  (≈33 h at 10k/min)
```

`seq` is global across tiers. At a 3-day track window there are about 3–5 track leaves plus up to 2 ahead. Block sizes are `foresignal.history.track-block` and `standard-block`.

- **DDL owner:** the Flyway baseline creates `record_seq`, the parent, the two tier parents, the PK and the three partitioned indexes, **with no leaf partitions and no DEFAULT partition**. Hibernate validates the partitioned parents because `spring.jpa.properties.hibernate.hbm2ddl.extra_physical_table_types: PARTITIONED TABLE` is set in the main `application.yml` (`application-test.yml` layers on top and does not override `spring.jpa.properties`).
- **`PartitionManager`** (`store.history`) creates leaves at runtime. It depends only on `DataSource` and `MeterRegistry`, never on JPA. All operations go through one in-process `ReentrantLock` on a dedicated autocommit connection with `SET lock_timeout='3s'` and 5 retries; never `@Transactional`.
  - **Bootstrap:** a `HistoryPartitionBootstrap` bean runs `ensureAhead()` (coverage ≥ `last_value + 1.5 × block` per tier) after the Flyway initializer and before the EntityManagerFactory.
  - **Create:** check `pg_inherits`, `CREATE TABLE rv_t_<start>` / `rv_s_<start> (LIKE record_version INCLUDING DEFAULTS)`, then `ATTACH PARTITION … FOR VALUES FROM (a) TO (b)`; `42P07`, and `42P17` when the re-check finds a covering leaf, count as success.
  - **Drop:** `ensureAhead` first, then `ALTER TABLE … DETACH PARTITION … CONCURRENTLY` with `statement_timeout='10min'`, then `DROP TABLE`; a pending detach is finished with `FINALIZE` on the next run.
  - **Schedule:** at bootstrap, every 10 minutes (`foresignal.history.maintenance`), and inline from the writer's pre-flight. Gauge `foresignal.history.partition.headroom{tier}`, warning under 1 block. `ANALYZE record_version` daily and after each attach or drop, under the same lock.
- **`chronicle_version`** is a second partitioned parent (LIST on `chronicle_id`), created by the baseline; one leaf per chronicle is created and dropped at runtime by `ChronicleDdl` (§40).
- **Other dynamic DDL** that remains by design is listed in §25.

### 7.4 Storage visibility

- `source_usage` accumulates per-source counts and `version_bytes`.
- Health `storage`: `observationsPerMinute, versionsPerMinute, persistRatio, triggerEdgeShare, bytesPerDay` (7 d), `projectedBytes` (× window), `lateLastHour, clampedLastHour`.
- `/v1/system.history`, per tier from the `pg_partition_tree('record_version')` leaves: `partitions`, `rows` (Σ reltuples), `bytes` (Σ `pg_total_relation_size`), `headroomSeq`, `windowDays`, `downsampledLeaves`, `downsampledRows`; totals and a warning above 20,000 versions per minute.

## 8. Distribution

### 8.1 Buses

- **ChangeBus:** `ChangeBatch(sourceId, batchId, ingestedAt, fromSeq, toSeq, List<ChangeSummary>)`. `ChangeSummary` = seq, prevSeq, historyTier, sourceId, externalId, kind, version, persistReason, observedAt, eventTime, validUntil, tombstone, qaFlags, `bbox`, `prevPersistedBbox` (from `h.persisted_geom`), `prevLiveBbox` (from `l.geom`). Summaries, not payloads. Subscribers include the SSE hub, the trigger engine, the embedding worker and every cursored consumer (entity linker, correlation engine, evidence resolver, binding router, chronicles, sinks…), each of which also catches up from the store by seq.
- **LiveBus:** `LiveBatch(sourceId, List<LiveUpdate>, List<Touch>)`. Subscriber queues are bounded; on overflow the batch is dropped and counted and the subscriber's `onLiveDropped()` runs.
- Change is always published before live.
- **AlertBus** carries alert lifecycle events to the alert stream, channels, enrichment, CAP, MQTT state, the compact outbox and quality signals; **IntelEventBus** carries intelligence-event changes (§32).

### 8.2 SSE

**`/v1/changes/stream`:**

| Event | `id:` | Lane |
|---|---|---|
| `change` | seq | FIFO (`clientBuffer` 1,000); overflow sends `reset` |
| `live`, `touch` | none | Conflation map per client, drained every `liveIntervalMs` (1,000), capped at `liveMaxEntities` (50,000) |
| `resync` | none | `{reason: gap\|live_cap\|live_dropped, watermarkSeq}` |
| `heartbeat` (15 s), `reset` | none | |

- Parameters: `live` (true), `touches` (false), `source`, `kind`, `bbox`, `externalId`, `place`, `view`, `inWatchlist`.
- **Filter match:** `bbox` OR `prevPersistedBbox` OR `prevLiveBbox` intersects the filter; when only a previous box matches, `leftFilter: true`.
- **Resume:** the SSE id is seq; with `Last-Event-ID` (or `?after=`) the hub replays from the database (filtered, up to 10,000) and then switches to live. When `after < RetentionState.lowestRetainedSeq` of the filtered sources, `reset` is sent at once.
- Payload loads use `seq = ANY(?)` (runtime pruning, one leaf per tier); `kind=raster-frame` events always carry their small payload.
- **Client contract:** open SSE with `after = watermarkSeq`, page the snapshot with `order=id`, apply an event only if its `observedAt` ≥ the stored `observedAt`. The SDKs implement this (§42).

**`/v1/events/stream`** multiplexes topics on one connection, each with its own cursor key: `changes`, `alerts`, `notifications`, `system`, `analyst`, `inbox`, `intel`, `evidence`, `situations`, `analytics`, `forecasts`, `hypotheses`, `assessments`, `workspace`, `me`, `chronicle`, `social`, `external`. `/v1/alerts/stream` and `/v1/notifications/stream` follow the same pattern. Every stream applies the caller's restriction and private filter at the hub; revoking a token sends `reset` (`reason: revoked`) and closes its streams. Browser `EventSource` uses stream tickets or tile keys.

### 8.3 TriggerEngine

- Subscribes to the ChangeBus; each `ChangeBatch` evaluates the enabled `record`-type rules whose source and kind filter could match. Each rule compiles (`RuleSqlBuilder`) into one parameterized query over `record_version` for `seq ∈ (from, to]`, **excluding `HEARTBEAT`**. Conditions use the operator registry: numeric with a number-type guard, boolean, string equality, arrays, `inWatchlist`/`notInWatchlist`, `near`, geofences (`ST_Intersects`) and `geofenceRef` (country, admin1, county unions). Tombstones never match.
- **Edge join** on seq only: `LEFT JOIN record_version p ON p.seq = v.prev_seq` (one leaf per tier).
  - `enter = NOT v.tombstone AND P(v) AND (v.prev_seq IS NULL OR p.tombstone OR (r.reappear_as_enter AND v.persist_reason = 2) OR (p.seq IS NOT NULL AND NOT P(p)))`
  - `exit = p.seq IS NOT NULL AND NOT p.tombstone AND P(p) AND (v.tombstone OR NOT P(v))`
  - A missing `p` (retention) fires nothing except through REAPPEAR or FIRST; expiry itself is not an exit. Fail quiet, never noisy. No in-memory state, so it survives restarts.
- **PersistGate:** `TriggerPersistGate` (`@Primary`, refreshed on `TriggerRulesChangedEvent`) compares the incoming snapshot against the **persisted** state (`h.payload` paths and `h.persisted_geom`). Any applicable rule with P(before) ≠ P(candidate) gives `TRIGGER_EDGE`, subject to `edgeMinInterval`. A new or edited rule therefore converges on each entity's next observation with no reconcile pass. Evaluation is exact, with the same operators as `RuleSqlBuilder`; when a JSON type differs from the operator's expectation or a value is missing, it reports a flip iff before ≠ candidate. `RuleSqlBuilderIT` and `TriggerPersistGateTest` share `src/test/resources/trigger/operator-truth-table.json`. Non-SQL predicates (moving anchors §8.8, the approach condition §38) load their paths and geometry and are conservative on ambiguity.
- **Validation:** a condition whose path root is a volatile field is `400 TRIGGER_PATH_VOLATILE`.
- **Match mode** fires per non-HEARTBEAT version, keyframes included; edge modes or a cooldown are recommended on SAMPLED sources.
- **Cooldown** on `observed_at`: SQL seeds `NOT EXISTS (alert … a.observed_at > v.observed_at − :cooldown)`, then Java filters per (rule, entity), or per rule with `cooldownScope: rule`. `maxAlertsPerHour`, active periods, mute, snooze and auto-resolve on inverse edges are part of the alert lifecycle.
- Matches are inserted into `alert` with `observed_at` and `record_snapshot` (kind, version, observedAt, GeoJSON geometry, payload ≤ 16 KB) and published on the AlertBus. `getAlert` loads the version by seq and falls back to the snapshot.
- **Durable cursor** `trigger.cursor` in `app_setting`; on startup the engine catches up in chunks before subscribing live.
- **Templates:** `{{name}}`, `{{source}}`, `{{externalId}}`, `{{kind}}`, `{{observedAt}}`, `{{eventTime}}`, `{{time}}`, `{{payload.<path>}}`, the region placeholders (040: `{{near.name}}`, `{{near.km}}`, `{{near.mi}}`, `{{near.bearing}}`, `{{near.text}}`, `{{country}}`, `{{country.name}}`, `{{admin1}}`, `{{admin1.name}}`, `{{county}}`, `{{sea}}`, from the alert's `region` snapshot), `{{place.name}}`, `{{place.distanceKm}}`, `{{anchor.distanceKm}}` and `{{context.weather.*}}` (§27.13). Default `"{{name}}: {{source}}/{{externalId}}"`.
- `POST /v1/triggers/{id}/test` runs the same SQL over a recent window (default 24 h, `since` translated to seq through `seq_clock`) without inserting alerts; it warns `DOWNSAMPLED_RANGE` over downsampled history.
- Windowed rules (`count`, `absence`, `anomaly`) are §31; exercise replays are §39.

### 8.4 Notification delivery

See §17. Outbound webhooks are one channel type among many; outbound sinks (§34) are a separate, record-level path.

### 8.5 Places and the area of interest (036)

- **`place`** (`ai.foresignal.place`): a slug id, a name, a shape (`point` + `radius_km`, `polygon`, `route` + `buffer_m`), its geometry and a derived **`area`** (MultiPolygon, SRID 4326) computed on every save (geography buffer, split at the antimeridian, ≤ 10,000 vertices, GiST-indexed). Every save bumps `version` and publishes `PlaceChangedEvent` after commit.
- Rules that name a place are re-materialized in the save's own transaction; the persist gate compares persisted state under the current fence, so editing a place never fires edges for stationary entities.
- Deleting a referenced place is `409 PLACE_IN_USE` unless `force=true`, which disables the rules; views and briefings that still name it match nothing there (fail closed).
- **Places AOI** (`PlacesAoiService`): the union of each place buffered to at least `max(radiusKm, foresignal.weather.aoi-buffer-km)` (150 km), its envelope and a version (`PlacesAoiChangedEvent`). With no places it is `foresignal.weather.default-aoi` (a Texas box), and AOI-consuming sources report a health warning. Both values are also app settings (`PUT /v1/settings/weather`), which win once stored.
- **Filters:** `place=<id>[,<id>]` on records, history, changes, tiles, the streams and search resolves to the union area (`&&` prefilter, then `ST_Intersects`); places are never anonymous. A token's `placeIds` restriction resolves through the same service.
- **Privacy:** place reads are always authenticated; templates get names and distances, never coordinates; MCP returns coordinates only with `detail=long` to agent tokens.

### 8.6 Saved views (036)

A saved view (`saved_view`) is a named `RecordPredicate`: sources, kinds, places, bbox and area, up to 20 conditions in the 017 grammar, a time axis, the expiry choice and a projection of up to 50 fields; `version` bumps on every save. One predicate, three renderings: `/v1/views/{id}/records` (every records format, keyset pages), `/v1/views/{id}/tiles/{z}/{x}/{y}.mvt` (with `?v=<version>` in the template and ETag) and `/v1/views/{id}/stream`. Each runs as a request filter through `RestrictionClamp`; `view=` applies the same filter to records, changes, the streams and search. A version that stops matching is streamed with `leftFilter: true`.

### 8.7 Watchlists (036)

`watch_list` (identity `icao24 | registration | callsign | mmsi | imo | custom`, normalization `upper | lower | digits | none`) and `watch_list_item` (≤ 100,000 per list), with CSV and JSON import and export. `IdentityResolver` maps (source, identity) to the payload field the descriptor declares in `entityKeys`; `inWatchlist` without a path renders an `EXISTS` over the normalized value. The gate reads the in-memory `WatchlistSnapshot`; list edits converge on each entity's next observation. Alerts of rules that reference a watchlist are `sensitive` and hidden from anonymous callers and feed keys.

### 8.8 Moving anchors (036)

`near: {anchor: {source, externalId}, radiusKm (0–2000), maxAnchorAge (PT30M), minMoveMeters (200)}`. Target side: `ST_DWithin` against the anchor's **current** position (`AnchorPositions`, fed by the change and live lanes), identical for `v` and `p`. Anchor side: `AnchorEvaluator` runs after each engine batch and every 5 s; when an anchor moved at least `minMoveMeters` from `trigger_anchor_state.eval_geom`, current targets fire `enter`/`exit` through the same `AlertPass`, referencing the target's head seq. A stale, tombstoned or expired anchor suspends the rule (`ANCHOR_STALE`, one `system.anchor_stale` notification per transition). Anchor coordinates never appear in templates, notifications, embeddings or LLM prompts.

## 9. Query paths

| Endpoint | Strategy |
|---|---|
| `GET /v1/records` | `record_head h JOIN record_live l`, merged payload. Filters `l.geom &&`, `NOT h.tombstone`, `l.valid_until > now()` unless `includeExpired`, `timeField` window. `order=observed` (keyset `(l.observed_at, source_id, external_id)`; sorts the matched set per page without an index, by design) or `order=id`. `watermarkSeq = ChangeBus.lastPublishedSeq()` is read before the query |
| `GET /v1/records/{s}/{id}` | Head plus live; `include=observer` reads observers from the head's version (gated) |
| `…/versions` | `WHERE source_id=? AND external_id=? ORDER BY seq` |
| `GET /v1/history` | By `timeField` (default `observed`) and bbox; `ingested` is translated to seq bounds through `seq_clock` |
| `GET /v1/changes` | `seq > :after` |
| `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt` | `ST_AsMVT` over `record_live l JOIN record_head h` where `l.geom && ST_TileEnvelope(z,x,y)`, merged properties (`source, externalId, kind, observedAt` plus `tileProperties`), cap ordered by `l.observed_at DESC`; `agg=hex` below a zoom |
| `kinds`, kind schemas | Head ⋈ live, current |
| `GET /v1/views/{id}/…` | The view's predicate ANDed into the same SQL through `RestrictionClamp` |

- `timeField = effective | observed | event | ingested`; default `effective` for records, search and briefings, `observed` for history.
- **Record DTO:** `version`, `seq`, `observedAt`, `persistedObservedAt`, `eventTime`, `quality`, `live` (= `l.dirty`), `qaFlags`, `globalRef` (§37), license fields (§33). **Version items:** `persistReason, ingestedAt, receivedAt, batchId, quality, observer` (gated), `downsampled`.
- Free-text `q` matches `record_head.search_text`, computed in the writer from the descriptor's `searchFields`.
- Every read path that returns records or alerts applies `RestrictionClamp` and carries `@RestrictedRead` (`RestrictedReadCoverageTest`), plus the private filter (§15.4) and the license policy (§33).

**asOf (transaction time):**
1. A `source` or `kind` filter is required (`400 ASOF_FILTER_REQUIRED`). `horizon(source) = now − window + effectiveKeyframe + 1 h`; T before the horizon of any involved source is `400 ASOF_BEYOND_RETENTION`.
2. `S = S(T)` (§6.3).
3. Per tier, one query: TRACK `L = S(T − ttl − 2 min)`, STANDARD `L = 0`.
4. `SELECT DISTINCT ON (source_id, external_id) … FROM record_version v JOIN (VALUES …) s(source_id, ttl) USING (source_id) WHERE history_tier = :tier AND seq BETWEEN :L AND :S ORDER BY source_id, external_id, seq DESC`, keeping rows where `NOT tombstone AND (valid_until_explicit > T OR (valid_until_explicit IS NULL AND (s.ttl IS NULL OR ingested_at + s.ttl > T)))` (on TRACK the explicit value is ignored); bbox and kind apply to the chosen version; keyset on `(source_id, external_id)`.
5. The response adds `asOf`, `watermarkSeq=S`, `approximate` (any SAMPLED or LATEST source, or a downsampled range) and `horizon`. Below the purge horizon a read fails `410 RESYNC_REQUIRED`.

Granularity is one batch. Valid-time asOf is not offered; use `/v1/history`. Snapshots and holds extend asOf beyond retention (§35).

## 10. API conventions

- Base path `/v1`. JSON uses camelCase. Timestamps are ISO-8601 UTC. Durations accept ISO-8601 (`PT15M`, `P7D`) or shorthand (`15m`, `7d`, `12h`).
- Errors use RFC 9457 `application/problem+json` with a stable `code` extension (`ErrorCodes`; a code is never reused for a different condition).
- Pagination: opaque `cursor` plus `limit` (default 500, max 5,000); responses include `nextCursor`.
- GeoJSON via `Accept: application/geo+json` or `?format=geojson`; features carry `id = "<source>:<externalId>"`. Other record formats include CSV, NDJSON, GeoJSON sequences, KML, CZML and the standards outputs of §36.
- **Auth:** `TokenFilter` checks `Authorization: Bearer fsr_…` against the scope `AccessPolicy` requires for the method and path (§15). Key-in-path URLs use `/v1/k/{key}/…` (feed `fsf_`, tile `fsk_`, share `fss_`), resolved by one `PathKeyFilter`. Comparison is constant-time against stored hashes.
- **CORS:** origins from `app_setting.cors` (default `*` for GET/HEAD/OPTIONS; writes need an explicit list).
- **The spec is authored, not generated, and complete up front (API-first).** `docs/OpenAPI.json` is copied onto the classpath and served verbatim at `/v1/openapi.json`; Swagger UI at `/docs` points to it. springdoc generates `/v1/openapi-generated.json` only so `OpenApiContractIT` can assert the implementation matches the authored spec. `src/test/resources/openapi/pending-operations.txt` is empty: every operation is implemented.

### 10.1 API evolution

`/v1` is **additive only** (NFR-10). `OpenApiContractIT`, `SpecAnnotationsTest`, `PostmanCollectionContractTest`, `./gradlew lintOpenApi` and, from v1.0.0, `./gradlew checkApiCompat` (oasdiff against the frozen `docs/api/v1.0.0/OpenAPI.json`, §25) enforce what a machine can check.

- **Allowed in `/v1`:** new operations, new optional request fields, new response fields, new enum values, new error codes and new SSE event types.
- **Clients must be tolerant:** they ignore unknown fields, enum values and SSE event names; the generated SDKs are configured the same way.
- **Never in `/v1`:** removing or renaming a field, changing its type, tightening validation, or adding a required request field. Such a change goes to `/v2`.
- **Deprecation:** `deprecated: true` in the spec plus the `Deprecation` (RFC 9745) and `Sunset` (RFC 8594) headers, for at least one minor release before removal in `/v2`.
- **Every spec change** regenerates Postman (`./gradlew generatePostman`), passes `lintOpenApi` and `checkApiCompat`, is recorded in `info.x-foresignal-changelog`, and bumps `info.version` by a minor version when it adds to the API. The released contract is `info.version` `1.0.0`.
- **Every operation** carries `x-foresignal-scope` (`open`, `read`, `agent`, `admin` or `peer`), `x-foresignal-prompt`, optional `x-foresignal-extended-by` and `x-foresignal-status`.
- **Events:** SSE payloads are described in the spec and mapped per operation in `x-foresignal-sse-events`; outbound requests (notification webhook, watchdog ping, CloudEvents sink) are in the top-level `webhooks` object.

## 11. Retention

**Windows:**
- TRACK uses `retention.trackWindow` (default 3 d) and is retained only by dropping whole partitions; a per-source retention PUT on a TRACK source is `400 RETENTION_TIER_FIXED`.
- STANDARD resolves per (source, kind): `retention.kinds.{kind}` → `retention.sources.{id}` → the descriptor's `kindRetention[kind]` → its `defaultRetention` → `retention.default` (7 d).
- Adapters set defaults by **analytic class** (Conventions "Analytic retention"): `event` 1825 d, `bulk-event` 90 d, `series` 3650 d, `state` 365 d, `reference` 90 d, `intel` 1825 d, `social` 30 d; the 7-day global default applies only to sources without a class.
- Settings shape: `{"default":"7d","alerts":"90d","usage":"30d","trackWindow":"3d","provenance":"30d","briefings":"180d","ask":"90d","kinds":{…},"sources":{…},"trackRawWindow":"24h","trackDownsampleInterval":"5m",…}`. Defaults in code (`RetentionSettings`): alerts 90 d, usage 30 d, track window 3 d, provenance 30 d, notifications 90 d, briefings 180 d, Ask 90 d, audit 90 d, inbox 30 d, entity links 365 d.

**Consumer clamp:** `cutoffSeq` is clamped to `min(cursor) + 1` over every durable consumer (`trigger.cursor`, `embedding.cursor`, the sink, chronicle, evidence, situation, region-stats, precursor, impact, relation, social and external-binding cursors, through `RetentionConsumerProvider`). A cursor older than `now − 2 × window` releases the clamp and alarms `foresignal.retention.consumer_lag`.

**RetentionJob** (hourly, `foresignal.retention.*`; not `@Transactional`; `POST /v1/settings/retention/run`). A `RetentionPlanner` computes once per run the TRACK leaves to drop and the STANDARD scopes and clamped cutoffs; ordered `RetentionStep`s then act on that plan:
1. **Holds and archive** copy exactly the rows about to go (`HoldCopyStep`, `ArchiveStep`, §35); a failed copy skips the STANDARD source or blocks the TRACK leaf.
2. **TRACK:** `PartitionManager.drop` for every `rv_t_*` whose upper bound ≤ `cutoffSeq(now − trackWindow)`, stopping at the first blocked leaf; `floorSeq` = the lower bound of the oldest remaining leaf.
3. **History downsampling** (044) at order 150 (below).
4. **STANDARD:** chunked deletes per source (`DELETE … WHERE history_tier = 0 AND seq < :cutoff AND seq IN (SELECT … LIMIT 10000)`), one transaction per chunk; on completion `floorSeq(source) = cutoff`; on failure the source is skipped this run. Empty `rv_s_*` leaves below the oldest standard window are dropped.
5. **Heads:** one `HeadPurge` per run for the sources whose steps succeeded and which have candidates; `staleBefore = min(now − window, source_health.last_data_at − touchMin)`, so a 304 streak never ages out entities from the last full response.
6. `fetch_log`: rows with `versions_written = 0` after 7 d; others after `max(retention.provenance, the largest version window)` (`ProvenanceStep`). `seq_clock`: minutes older than the largest window + 1 d.
7. Operational tables (alerts, usage, notifications, briefing runs, Ask threads, audit, embeddings of vanished records, CAP messages of deleted alerts, doctor reports 90 d, exercise alerts 7 d, rule quality 400 d, pattern-of-life data 180 d, exports), then `RetentionState` (floors, `lowestRetainedSeq`, `purgeSeq`) is published.

**FR-17 as amended:** current state is kept while current; versions follow the window; observed entities are re-keyframed every window/4, so they always keep a recent version.

**Raster frames (042).** `RasterRetentionStep` deletes a row and then unlinks its file: ladders (`ProductSpec.retention`; MRMS every frame 6 h then 10-minute frames to 48 h; GOES C13 5-minute frames for 24 h then 30-minute to 72 h; models the current and previous complete run; SatDump geostationary 72 h), the quota `foresignal.raster.disk-quota-gb` (200; at 90% `/v1/system` degrades with `RASTER_DISK_QUOTA`), and an hourly orphan sweep that removes files without rows and rows without files and re-emits the `raster-frame` record of any complete frame newer than its head (a crash between commit and enqueue), as a `MANUAL` fetch row with principal `system:raster-sweeper`. A removed frame answers `410 FRAME_EXPIRED`.

**Media (043).** `MediaRetentionStep` (order 780): expired rows deleted (static-map attachments live 7 d); rows that follow their record are deleted with the record's window or 24 h after its head disappears (`foresignal.media.head-missing-grace`); thumbnails go with their original; a file is unlinked only when no row references its sha256; deleted ids keep a tombstone for 30 days (`410 MEDIA_EXPIRED`). Media bytes count against the raster quota, which evicts frames, never media.

**History downsampling (044).** `HistoryDownsampler` rewrites at most one TRACK leaf per run, oldest first, into a thinned copy and swaps it in:
- Settings `retention.trackRawWindow` (24 h; null disables) and `retention.trackDownsampleInterval` (5 m, 30 s ≤ interval ≤ raw window), overridable per TRACK source (`source_settings.downsample_interval`).
- **Eligible leaf:** an attached `rv_t_*` leaf with no DONE `history_downsample` row, upper bound ≤ `cutoffSeq(now − trackRawWindow)` and ≤ the consumer bound, and not about to be dropped.
- **Keep set:** persist reasons FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE and TRIGGER_EDGE; the newest seq per entity in the leaf; every seq a head or an alert references; and, among the rest, the lowest seq per (source, entity, interval bucket).
- **Rewrite and swap** on the `PartitionManager` connection under its lock: a RUNNING row; `CREATE TABLE rv_t_<start>_ds (LIKE record_version INCLUDING DEFAULTS)`; `INSERT … SELECT` the keep set; leaf indexes built before attaching; a CHECK matching the bounds; one short transaction with `SET LOCAL lock_timeout = '3s'` that detaches the original and attaches the copy (5 retries, then FAILED and the copy dropped); then drop, rename, analyze; DONE with `rows_before`/`rows_after`. Startup recovery rolls back or finishes an interrupted swap.
- `DownsampleRanges` caches the DONE ranges: version items carry `downsampled: true`, asOf over an overlapping TRACK range widens validity to `max(ttl, interval)` and answers `approximate: true`, and `testTrigger` warns. Live triggers are unaffected.

## 12. Testing strategy

**Policy.** Tests are written in the same prompt as their code. Under the owner directive of 2026-10-17, **no test was run from that date until code-complete**: prompts 124 onward (including the checkpoints 124 and 139, the full test run 142, runtime verification 143 and production hardening 144) wrote tests and compiled them (`./gradlew compileJava compileTestJava`) without executing them. The full suite, the coverage pass, the 123 benchmark baseline and every checkpoint verification are deferred to the owner. `docs/Runtime-Verification.md` holds the 143 procedure with empty "Measured" cells. Earlier checkpoints (012, 022, 041, 057, 097) and the sanctioned narrow runs of 007–009 ran under the earlier policy.

| Layer | Tooling | Scope |
|---|---|---|
| Unit | JUnit 5, AssertJ, Mockito | Hashing, policy matrix, budget math, health derivation, rule SQL, durations, cursors, templates, parsers, statistics with golden values |
| Adapter contract | Fixtures + parser tests | Each parser against a recorded (or documented synthetic) upstream response |
| Adapter integration | WireMock | Each adapter through `UpstreamClient` (budget, headers, 429 backoff) |
| Persistence integration | Testcontainers with the Foresignal database image (PostGIS + pgvector, built from `docker/db/Dockerfile` once per JVM by `PostgisContainers`) | Writer, partitions, spatial queries, MVT, retention, trigger SQL, vector search. **Flyway migrates the Testcontainers database** exactly as in production |
| LLM | WireMock stubs of the Anthropic, OpenAI-compatible and Ollama APIs; a deterministic fake client | Tool loops, budgets, fallbacks, prompt-injection containment. **No test calls a real model** |
| Notifications | WireMock receivers; GreenMail for SMTP; Toxiproxy for MQTT | Payload formats, retries, rate limits, Last Will |
| API integration | `@SpringBootTest` + MockMvc + Testcontainers | Every endpoint, auth rules, ProblemDetail errors, SSE replay and resume |
| End-to-end | Testcontainers + WireMock USGS | Enable source → ingest → revision → trigger → SSE alert → channel delivery |
| Performance | `./gradlew perfTest` (`@Tag("perf")`, not part of `test`) | The 022 smoke |

**No credentials during construction:** every test uses WireMock and fixtures; no test calls a real upstream. Raster fixtures too large to commit are fetched with `./gradlew downloadTestFixtures` (sha256-pinned); tests that need them skip with a logged reason when absent.

**Test profile** (`src/test/resources/application-test.yml`, layered on the main file): `foresignal.ingest.writer.exclusive=false`, scheduled partition maintenance, retention and every background schedule off. `DatabaseCleaner` truncates between tests, skips `pg_inherits` children and `flyway_schema_history`, drops orphan chronicle leaves and calls `IngestWriter.resetState()`. `TestRecords` seeds through the writer; ITs never seed `record_head`, `record_live` or `record_version` with raw SQL. The test JVM gets 4 GB and caps Spring's context cache at 12.

**Guards** assert the shape of the source rather than one unit's behavior, so the change that breaks one is usually in a file whose own suite had no reason to run. They run with the suite:
- `LicenseAllowlistTest` (PRC-2, runtime classpath), `ParquetClasspathTest` (no Hadoop at runtime), `checkUnidataScope` / `UnidataScopeBuildTest`;
- `RestrictedReadCoverageTest` (every read path is `@RestrictedRead`), `LicenceEgressGuardUsageTest`, `GuardedImagesOnlyDecodeEntryTest`, `NoJpaWritesToRecordVersionTest`;
- `OpenApiContractIT`, `SpecAnnotationsTest` (annotations, pending list), `PostmanCollectionContractTest`, `lintOpenApi`, `checkApiCompat`;
- `InvariantRegistryTest` (every test class named in [Invariants](Invariants.md) exists);
- 144: `NoBootstrapDdlTest` (no DDL outside the allowlist of §25), `FlywayBaselineIT`, `FlywaySchemaParityIT`, the Flyway variant of `SchemaParityIT`, and `SourceCatalogParityTest` (Source Catalog tier, policy, license and egress columns against the descriptors).

JaCoCo enforces ≥ 90% line coverage (`jacocoTestCoverageVerification`, wired into `check`), excluding `ForesignalApplication`.

## 13. Configuration

File-based configuration is limited to bootstrap concerns in `src/main/resources/application.yml`, each overridable by an environment variable. Everything else (sources, budgets, credentials, retention, triggers, channels, tokens, LLM providers, briefings, CORS, rate limits, outbound policy, licenses, MCP authorization, plugins) is stored in the database and managed through the API.

**Variables in `application.yml`** (default when unset):

| Variable | Default | Purpose |
|---|---|---|
| `FORESIGNAL_DB_URL` | `jdbc:postgresql://localhost:5433/foresignal` | Database URL |
| `FORESIGNAL_DB_USER` / `FORESIGNAL_DB_PASSWORD` | `foresignal` / `foresignal` | Database credentials |
| `FORESIGNAL_PORT` | `8080` | HTTP port |
| `FORESIGNAL_TRUSTED_PROXIES` | `127\.0\.0\.1\|0:0:0:0:0:0:0:1\|::1` | Tomcat RemoteIpValve internal proxies (regex or CIDR list); feeds audit, pairing limits and rate limits |
| `FORESIGNAL_ACCESS_LOG` | `false` | Tomcat access log (keys redacted) |
| `FORESIGNAL_COMPRESSION` | `true` | gzip for read responses (never SSE) |
| `FORESIGNAL_DATA_DIR` | `./data` | Data volume: secret key, rasters, media, exports, plugins, imports, temp |
| `FORESIGNAL_MODE` | `full` | `full` or `edge` (§14, §37) |
| `FORESIGNAL_FEDERATION_INSTANCE_NAME` | empty | Display name in `/v1/federation/info` (the setting wins) |
| `FORESIGNAL_INBOX_DIR` | `${FORESIGNAL_DATA_DIR}/inbox` | Root of `INBOX_PATH` parameters |
| `FORESIGNAL_RESET_ADMIN_TOKEN` | `false` | Regenerate the admin token on start |
| `FORESIGNAL_SATDUMP_STATIONS` | empty | One `satdump-{station}` source per comma-separated name |
| `FORESIGNAL_LOCAL_RECEIVERS_SLOTS` | `2` | Slots per local-receiver family (1–16) |
| `FORESIGNAL_TRANSIT_CATALOG_FEEDS` | empty | Mobility Database ids to register as transit sources (startup only) |
| `FORESIGNAL_PROFILE` | `small` | Memory profile `small` or `large` (§27.7) |
| `FORESIGNAL_RASTER_FRAME_CACHE_MB`, `FORESIGNAL_RASTER_TILE_CACHE_MB`, `FORESIGNAL_RASTER_INDEX_CACHE_MB`, `FORESIGNAL_RASTER_RENDER_POOL`, `FORESIGNAL_RASTER_DECODE_CONCURRENCY` | empty (profile value) | Override one profile value each |
| `FORESIGNAL_RASTER_DISK_QUOTA_GB` | `200` | Frame store quota (media and GLM archive count) |
| `FORESIGNAL_RASTER_GLM_RING_SCOPE` | `aoi` | GLM ring scope: `aoi` or `conus` (large profile only) |
| `FORESIGNAL_RASTER_GDAL_ENABLED` | `auto` | Optional `gdalwarp` path (`auto`, `true`, `false`); needs an image built with `WITH_GDAL=true` |
| `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE` | empty | Optional LAN XYZ tile server for static maps; public hosts refused |
| `FORESIGNAL_MEDIA_MAX_FILE_BYTES` | `25MB` | Media store file cap (`413 MEDIA_TOO_LARGE`) |
| `FORESIGNAL_BIND` | `127.0.0.1` | The published bind (`foresignal.advertised-bind`); `/v1/system` warns when open reads are exposed |
| `FORESIGNAL_MCP_ENABLED` | `true` | MCP server at `/mcp` |
| `FORESIGNAL_MCP_LEGACY_SSE` | `false` | Legacy HTTP+SSE MCP transport |

**Variables read in code:** `FORESIGNAL_LOG_FORMAT` (`ecs`, `gelf`, `logstash` or `text`; `ecs` under the `docker` profile, else `text`; `LogFormatEnvironmentPostProcessor`) and `FORESIGNAL_GIT_COMMIT` (the commit recorded in analysis manifests, `manifest.BuildInfo`).

**Variables in `docker-compose.yml`** (compose default):

| Variable | Default | Purpose |
|---|---|---|
| `FORESIGNAL_DB_BIND` | `127.0.0.1` | Host bind of the database port `5433` |
| `FORESIGNAL_BIND` | `127.0.0.1` | Host bind of `8080` and Ollama's `11434`; passed to the broker |
| `FORESIGNAL_LOG_FORMAT` | `ecs` | Log format |
| `FORESIGNAL_TRUSTED_PROXIES` | loopback | As above; set it to the compose network for the `tailscale` and `web` profiles |
| `FORESIGNAL_PROFILE` | `small` | Memory profile |
| `FORESIGNAL_RASTER_DISK_QUOTA_GB` | `200` | Raster quota |
| `FORESIGNAL_JAVA_TOOL_OPTIONS` | `-Xmx4g -XX:+UseG1GC` | JVM flags; `-Djava.io.tmpdir=/data/tmp -Djna.tmpdir=/data/tmp` is always appended (libaec's native library is extracted there, so the volume must allow executables) |
| `FORESIGNAL_MEM_LIMIT` | `6g` | Container memory limit |
| `FORESIGNAL_SATDUMP_STATIONS` | empty | SatDump stations |
| `WITH_GDAL` | `false` | Build arg: install `gdal-bin` |
| `SATDUMP_STATION` | `home` | Station name of the read-only SatDump output mount |
| `SATDUMP_IMAGE` | `satdump:local` | Operator-built SatDump image (`satdump` profile) |
| `TS_AUTHKEY` | empty | Tailscale auth key (`tailscale` profile) |
| `FORESIGNAL_HOSTNAME` | `foresignal.local` | Caddy site name (`web` profile) |

Compose also fixes `FORESIGNAL_DB_URL=jdbc:postgresql://db:5432/foresignal`, the database user and password, `FORESIGNAL_DATA_DIR=/data`, `FORESIGNAL_PORT=8080` and `SPRING_PROFILES_ACTIVE=docker`. The Dockerfile sets `FORESIGNAL_DATA_DIR=/data` and the temp-directory `JAVA_TOOL_OPTIONS`. The CLI (§42) reads `FORESIGNAL_URL` and `FORESIGNAL_TOKEN`; prompt 143 reads optional `FORESIGNAL_VERIFY_*` keys.

Decoder directories (046): downloads land in `${FORESIGNAL_DATA_DIR}/rasters/tmp/`; the self-test unpacks into `rasters/selftest/`; NetCDF-Java's disk caches are pinned to `${FORESIGNAL_DATA_DIR}/cache/netcdf/`. `SafeNetcdf` opens nothing outside `rasters/tmp` and `rasters/selftest`. `foresignal.raster.decoder-self-test.enabled` (true) controls the startup decoder self-test.

## 14. Deployment

```
docker-compose.yml
├── db          build docker/db (foresignal-db:17-3.5-pgvector)   volume pgdata    ${FORESIGNAL_DB_BIND:-127.0.0.1}:5433→5432
│               healthcheck pg_isready; tuned flags (below); restart unless-stopped
├── foresignal  build . (WITH_GDAL)   volume foresignal-data:/data, satdump-output:/data/inbox/satdump/<station>:ro
│               ${FORESIGNAL_BIND:-127.0.0.1}:8080; depends_on db healthy; mem_limit ${FORESIGNAL_MEM_LIMIT:-6g}
│               healthcheck /actuator/health/readiness; restart unless-stopped
├── tailscale   tailscale/tailscale:v1.102.5   profile "tailscale"   HTTPS on the tailnet through Tailscale Serve
├── caddy       caddy:2.11.7-alpine            profile "web"         HTTP/2 and HTTP/3, `tls internal` local CA, ports 80/443
│               (443/udp), proxies /v1, /mcp and probes; serves ./deploy/web same-origin
├── ollama      ollama/ollama:0.40.3           profile "local-llm"   ${FORESIGNAL_BIND:-127.0.0.1}:11434
└── satdump     ${SATDUMP_IMAGE:-satdump:local} profile "satdump"    `satdump legacy autotrack`, host network, /dev/bus/usb,
                writes satdump-output (operator-built from SatDump's own Dockerfile)
```

- `docker compose up -d` starts the database and the broker; `docker compose up -d db` the database only. Profiles add `tailscale`, `web`, `local-llm` and `satdump` (`docker compose --profile <name> up -d`).
- **Database flags** (sized for sustained history writes): `shared_buffers=512MB, max_wal_size=8GB, checkpoint_timeout=15min, wal_compression=lz4, max_locks_per_transaction=256, autovacuum_vacuum_cost_limit=2000`.
- **Binds:** everything is localhost by default; LAN access needs `FORESIGNAL_BIND=0.0.0.0`. For internet exposure, put it behind TLS (the `web` or `tailscale` profile, or another reverse proxy).
- **External database:** the JAR runs standalone (`java -jar foresignal.jar`) against a PostgreSQL that has PostGIS and pgvector. Flyway's baseline runs `CREATE EXTENSION IF NOT EXISTS postgis` and `vector`; on a database where Foresignal's role is not a superuser, a superuser must create both extensions first (§25). `DETACH PARTITION … CONCURRENTLY` (§7.3) needs PostgreSQL 14 or later; the tested image is PostgreSQL 17 with PostGIS 3.5.
- **Upgrading a pre-1.0 development database:** `baseline-on-migrate` adopts it at version 1 without running V1 and logs a warning; compare it with `scripts/db/dump-schema.sh` or recreate the volume (`docker compose down -v`).
- **Capacity:** expect about 10–15 GB/day of history writes plus 15–20 GB/day of WAL at the design volume; a 3-day track window holds about 40M rows, roughly 20 GB with indexes. NTP must be synchronized before Foresignal starts, or the clock guard holds ingest.
- **Edge mode** (`FORESIGNAL_MODE=edge`): a lightweight relay that collects locally and pushes to a hub through the `federation-push` sink; it leaves out embeddings, Ask, the analyst, briefings, raster products and rendering, OGC, exports and federation serving, and seeds a short retention (6 h track window, 1 d default) when none is stored (§37).
- **Version:** Gradle `version = "1.0.0"`; `ForesignalVersion` reads Spring Boot build info; the image carries `org.opencontainers.image.version=1.0.0`.

Memory profiles are §27.7.

## 15. Access tokens and scopes

- **Scopes**, least to most privileged: `read` < `agent` < `admin`; federation peers use `peer` keys (`fsp_`).

| Scope | Can |
|---|---|
| `read` | Every data GET (needed only when `security.openReads=false`), semantic search, MCP read tools, Ask without actions, push registration |
| `agent` | `read` plus triggers, briefings, places, tile keys, alert lifecycle, Ask with actions, MCP write tools, intelligence work products within their access lists |
| `admin` | Everything: sources, credentials, custom sources, channels, providers, tokens, settings, sinks, peers, plugins, system |

- **Tokens** live in `api_token`; the plaintext (`fsr_` + 32 random bytes) is shown once; only the SHA-256 hash is stored. Tokens belong to **principals** (people, agents, services; §40) and can expire and be revoked; `last_used_at` updates at most once a minute; disabling a principal stops all its tokens.
- **`AccessPolicy`** is the single table mapping (method, path pattern) to a requirement (`open`, `read`, `agent`, `admin`, `peer`, ingest key); unmatched `/v1/**` paths require `admin`. The MCP server uses the same scopes.
- **Settings:** `app_setting['security'] = {"openReads": true, "anonymous": {…}, "metricsOpen": true, …}`.
- **API:** `GET/POST /v1/tokens`, `PATCH /v1/tokens/{tokenId}`, `DELETE /v1/tokens/{id}` (admin). Stream tickets and device pairing (023) let browsers and apps authenticate without long-lived headers.

### 15.1 Read restrictions (035)

- **`ReadRestriction`** (`security.restrict`): `sources`, `kinds`, `bbox` and/or `area` (MultiPolygon, ≤ 2,000 vertices; their intersection when both are set), `endpoints`, `excludePrivate`, `maxRequestsPerMinute`, plus `placeIds`, `federation` and `includeExercise`; stored on `api_token`. Restrictions on admin tokens are refused (`400 RESTRICTION_NOT_ALLOWED`).
- **`RequestPrincipal`**: a token, a feed key, a tile key, a share key or an anonymous caller, with its effective restriction.
- **`RestrictionClamp`** is the one place that turns a restriction into predicates. **Intersect, never union:** the restriction is ANDed into every query, so an absent request filter becomes the restriction and a disjoint one yields nothing. A single record or alert outside it is `404`, never `403`. Streams, push fan-out and citations use its in-memory `test()`. Responses to a restricted caller are `Cache-Control: private`.
- **Rule for new read paths:** every endpoint, MCP tool or export that returns records or alerts applies `RestrictionClamp` and carries `@RestrictedRead` (`RestrictedReadCoverageTest`).
- **Anonymous profile:** `security.anonymous` applies to tokenless reads while `openReads` is on (default: private sources excluded, data GETs only, 120 requests per minute). `/v1/system` warns `ANONYMOUS_UNRESTRICTED` when the profile limits nothing and the bind is not local.
- **Feed keys:** `api_token` rows with `type = feed_key`, prefix `fsf_`, scope `read`, `prefixes`, a mandatory restriction and expiry ≤ 365 d (`/v1/feed-keys`, admin). `PathKeyFilter` resolves `/v1/k/{key}/<rest>` (GET/HEAD only, prefix check, `401 INVALID_FEED_KEY`, `410 FEED_KEY_GONE`) and forwards to the canonical path. Responses carry `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex` and `Cache-Control: private`; the key is redacted from the access log and Micrometer tags. Resolvers are pluggable by prefix (`PathKeyResolver`).
- **Tile keys (050):** `type = tile_key`, prefix `fsk_`, scope `read`, `prefixes` from `TileKeyPolicy.ALLOWED_PREFIXES` (`/v1/products/`, `/v1/styles`, `/v1/media/`, `/v1/tiles/`, `/v1/maps/static`, `/v1/sample`, `/v1/ground-stations/`, `/v1/changes/stream`, `/v1/events/stream`, `/v1/map/`), an optional restriction, expiry ≤ 30 d, never the private grant (`/v1/tile-keys`, agent; `access.TileKeyService`). Unknown, expired or revoked: `401 INVALID_TILE_KEY`; outside the prefixes: `403 INSUFFICIENT_SCOPE`. CORS on `/v1/k/**` exposes `Foresignal-Frame-Time`, `Foresignal-Image-Bounds`, `Foresignal-Frame-Expired`, `Retry-After`, `Link`, `ETag`, allows `If-None-Match` and `Foresignal-Prefetch`, and caches preflights for 86,400 s.

### 15.2 Rate limits and auth-failure backoff (035)

- **Per-client limits:** `ClientRateLimitFilter` keeps Bucket4j buckets per client key (`token:<id>`, `feed:<id>`, `tilekey:<id>`, `ingest:<principal>`, `ip:<address>`; the address as RemoteIpValve resolved it through `FORESIGNAL_TRUSTED_PROXIES`). Classes: `tiles`, `stream` (30 opens per minute, 8 concurrent), `expensive` (search, Ask, sampling, LLM-backed MCP tools), `write`, `read`, and `anonymous` per IP. Over a limit: `429 RATE_LIMITED` with `Retry-After`. Settings `app_setting['ratelimits']`.
- **Auth-failure backoff:** after 5 failed credential checks per IP within 60 s, each further failure is `429` with `Retry-After` doubling from 1 s to 300 s. A correct credential is always evaluated first and resets the address.
- **Metrics:** `foresignal.ratelimit.rejected{class}`, `foresignal.auth.failures`, `foresignal.auth.backoff.active`.

### 15.3 Outbound URL policy (035)

Every operator-supplied outbound URL passes `ai.foresignal.net.OutboundUrlPolicy` when saved (`400 URL_NOT_ALLOWED`) and again on every send. Shape first: `https`, plus `http` in the LAN profiles, no user info; the host must be a strict dotted-quad IPv4, a valid IPv6 literal (no zone id) or a name; other numeric IPv4 forms are refused. A name is resolved once and every address must pass; IPv4 embedded in mapped, compatible, NAT64, 6to4 and Teredo forms is judged. Addresses are classified against the IANA special-purpose registries, multicast and the cloud metadata addresses. Profiles: `PUBLIC_ONLY`; `LAN_ALLOWED` (plus loopback, RFC 1918, ULA, 100.64/10: channels, LLM base URLs, FCM token URI, watchdog, archive and MCP OAuth discovery); `LAN_ONLY` (`LAN_URL` parameters); `PUSH_ENDPOINT` (`PUBLIC_ONLY` plus `push.allowedPrivateHosts`). Link-local, metadata, unspecified, multicast, broadcast, documentation and reserved blocks are refused everywhere. Admin overrides (`app_setting['outbound']`): deny wins, and allow never unblocks the always-blocked set. Senders connect to the checked address with the original Host and TLS name and never follow redirects (`PinnedHttp`, `PushHttp`, `UpstreamClient`, `EdpHttpClient` with at most 3 same-host redirects). With a JVM proxy configured, `LAN_ONLY` is refused and a startup warning says address checks are bypassed for proxied requests.

### 15.4 Private sources and the egress decision (039)

`ai.foresignal.security.visibility.SourceVisibility` decides which sources are private: the descriptor's `private` flag **or** the operator's tightening (`app_setting['visibility']`). An `admin` token always sees them; a `read` or `agent` token, feed key or ticket only when created with `privateAccess`; anonymous callers never. `PrivateFilter` is the one per-caller exclusion every query builder applies; a direct read of a hidden record is `404`.

`ai.foresignal.security.EgressPolicy` is **the single egress decision** (Conventions): `decide(path, sourceId, recordTerms, payload, EgressContext)` returns a `Decision` (allowed, reason, terms) for every path — API reads, tiles, SSE, search and embeddings, LLM context, MCP, exports, sinks, federation, share links and feeds. It combines the private flag, the descriptor's `egress` (`OPEN | LOCAL_ONLY`, 062), license terms (109, §33) and TLP (130). Private records stay out of LLM prompts unless the provider is local and `llm.allowPrivateLocal` is on; private payloads are stripped from notifications unless the channel has `privatePayload: true`, and always from Web Push, APNs and FCM; nothing private is ever federated. Private-data reads by non-admin principals, exports and purges are audited.

## 16. Inbound webhooks and custom push sources

Some data can't be polled: a local SDR, an ais-catcher instance, Home Assistant, a script, an n8n flow or an agent that wants to report something. These push into Foresignal.

- **Custom sources** are created at runtime (`POST /v1/custom-sources`, admin), stored in `custom_source`, and registered as dynamic descriptors with mode `PUSH`. The id starts with `custom-`; the source declares kinds, attribution, TTL, `tileProperties`, `searchFields`, `embedFields`, `observedAtMode`, `ttlAnchor` (STANDARD), `volatileFields`, `significantFields`, privacy and license terms. Creation returns an **ingest key** (`eik_`) once; it can be rotated. Editing hashed fields triggers the lazy recipe migration (§6.7).
- **Ingest endpoint:** `POST /v1/ingest/{sourceId}[/{format}]` accepts a GeoJSON FeatureCollection, `{"records": [...]}` in the `NormalizedRecordPreview` shape, or a named format (device presets, Alertmanager, ntfy/Gotify-compatible publish and others). Auth: `X-Foresignal-Ingest-Key`, bearer or basic with the key, or an admin token; device keys (`eid_`) go in the path `/v1/ingest/{sourceId}/k/{key}/{format}` for firmware that cannot send headers. Limits: 5 MB per request (after gzip decompression) and 10,000 records. Records go through the same queue, so versioning, streams, triggers, embeddings and retention all apply. Response: an `IngestOutcome`.
- **External Data Protocol** (024, 138): third-party providers register with `fsx_` tokens, publish a manifest and push or serve items; an EDP source is a custom source whose descriptor comes from the manifest (`ai.foresignal.inbound.edp`); see [External Data Protocol](External-Data-Protocol.md) and §40.

### 16.1 RecordMapping and device presets (059)

Any JSON, form or query-string device can be onboarded without code (`ai.foresignal.mapping`).

- **RecordMapping** (`version: 1`) is data, not a script: every value is `{path}` (Jayway JSONPath), `{template}` (substitution only) or `{const}`. Members: `select`, `externalId`, `kind`, `observedAt`, `eventTime`, `validUntil`, `geometry`, `tombstone`, `quality`, `fields[≤ 64]` (`name, path, type, unit, scale, offset, default, sentinels`), `pairs`, `spread`, `drop`, `filter`.
- **`MappingCompiler`** collects every error with its path (`400 MAPPING_INVALID`); it refuses deep scan, function calls and regex filters, unknown members, bad field names and unit pairs outside the built-in table.
- **`MappingEngine.apply(doc, mapping, receivedAt)`** turns one document into records, rejections and warnings; records then go through the standard normalizer.
- **Ingest variants:** form bodies, GET for formats that declare it, gzip, the WU `PASSWORD` query key, Basic and key-in-path URLs from `ingestAllowCidrs` (`403 INGEST_SOURCE_DENIED`). Secrets in queries and paths are redacted everywhere.
- **Device presets** (`src/main/resources/mapping/presets/{id}.json`): `ecowitt`, `wu-pws`, `aiscatcher`, `chirpstack-v4`, `tts-v3`, `alertmanager`, `airrohr`, `gmc` and others, each with a format, mapping, defaults, parameters and setup instructions.
- **Preview, samples, test, suggest:** `POST /v1/mappings/preview` writes nothing; `captureSamples` keeps five redacted requests for an hour; `POST /v1/mappings/suggest` asks the LLM for a draft, validated and never stored or applied (a remote provider needs `confirmEgress: true`; a private source's sample never goes to one).

## 17. Notification channels

One delivery system serves alerts, briefings and system events across many destinations.

| Type | Non-secret config | Secret config | Delivery |
|---|---|---|---|
| `webhook` | `url`, `method`, `headers`, `bodyTemplate` | `hmacSecret`, secret headers | JSON; `X-Foresignal-Event`, `-Delivery`, `-Timestamp`, `X-Foresignal-Signature: sha256=HMAC("<timestamp>.<body>")` |
| `slack` | `username`, `iconEmoji` | `webhookUrl` | Block Kit, mrkdwn |
| `mattermost` | `channel`, `username`, `iconUrl` | `webhookUrl` | Slack-compatible payload |
| `discord` | `username` | `webhookUrl` | Embeds; color by severity |
| `teams` | — | `workflowUrl` (Workflows only; `webhook.office.com` refused) | Adaptive Card |
| `google-chat` | — | `webhookUrl` | Card v2 |
| `telegram` | `chatId`; interactive settings (§17.5) | `botToken` | Bot API, HTML |
| `matrix` | `homeserverUrl`, `roomId`; interactive settings | `accessToken` | `m.room.message` with `formatted_body` |
| `ntfy` | `serverUrl`, `topic` | `accessToken` | Title, priority, tags, click URL |
| `gotify` | `serverUrl` | `appToken` | Priority from severity |
| `pushover` | — | `appToken`, `userKey` | Priority from severity |
| `email` | `smtpHost`, `smtpPort`, `tls`, `from`, `to[]` | `username`, `password` | Multipart text + HTML |
| `webpush` | `quietHoursMode`; one built-in channel "Push" | — | Web Push, UnifiedPush, APNs and FCM fan-out (§24) |
| `mqtt` | broker, prefix, QoS, protocol version, expiry, template, Home Assistant options | `username`, `password`, `caCertPem` | §17.4 |

Further types: `apprise`, `homeassistant`, `zulip`, and the mesh channels `meshtastic` and `meshcore` (084, through the node's JSON integration or companion protocol; no Meshtastic protobuf). `GET /v1/channels/types` returns the catalog with config field specs.

- **Message model:** `NotificationMessage` (`event`, `title`, `body` markdown, `severity`, `url`, `data`, `attachments`), converted and escaped per destination.
- **Routing:** events, optional `rule_ids` and `min_severity`, templates with the §8.3 placeholders plus `{{severity}}`, `{{summary}}`, `{{url}}`; `config.units` metric or imperial; optional `language` translation (§42); `acceptExercise` (§39).
- **Delivery:** `notification_delivery` rows claimed with `FOR UPDATE SKIP LOCKED`; per-destination rate limits (Slack about 1/s, Discord 5 per 2 s, Telegram 20/min per chat, ntfy and Gotify 10/s, Pushover 2/s, email 30/min); `429` honors `Retry-After`; retry ladder 10 s, 1 m, 5 m, 30 m, 2 h, then `failed` (a system event never sent back to the failing channel).
- **Enrichment wait:** `wait_for_enrichment_seconds` (0–60).
- **Security:** channels are admin-only; secrets are encrypted and never returned; URLs pass the `LAN_ALLOWED` policy.
- **API:** CRUD under `/v1/channels`, `POST …/test`, `GET …/deliveries`, `GET /v1/channels/types`.
- **Mute (033):** `POST /v1/triggers/{id}/mute` (1 h–7 d) routes the rule's alerts to no channel while they still record.

### 17.1 Acting from notifications (033)

A firing alert's first send carries single-use action capabilities (`ack`, `snooze`, `mute`, and `feedback:useful` / `feedback:noise` from 115) minted per delivery in `action_capability`. Token `eac_<16 random bytes>.<HMAC prefix>` (38 characters, fits Telegram `callback_data`); `GET /v1/actions/{token}` renders a confirm page (no JavaScript, strict headers), `POST` executes once. Per type: ntfy HTTP actions, Web Push actions, native categories, Telegram inline keyboards (`TelegramCallbackPoller`), Matrix links and reactions, Pushover emergency receipts, webhook `actions` arrays, confirm links elsewhere.

### 17.2 Notification inbox (033)

Every persisted `notification` row is an inbox item for the `inbox` window (30 d). Read state is per device (`notification_read_cursor` plus sparse `notification_read`); `GET /v1/notifications`, `/{id}`, `/badge`, `POST /read`, and `/v1/notifications/stream` (the `inbox` topic). Principals also get a personal inbox (§40).

### 17.3 Presence deferral and escalation (033)

`client_presence` heartbeats let a channel hold non-critical alerts while a desktop is active (`deferWhileActiveSeconds`). `escalation_policy` (≤ 5 steps, optional repeat, `stopOn`) drives `alert_escalation` rows through the `EscalationWorker` (15 s, `FOR UPDATE SKIP LOCKED`); acknowledgement, resolution, snooze or a mute stop it.

### 17.4 MQTT channel and Home Assistant discovery (060)

`MqttConnections` keeps one HiveMQ client per enabled `mqtt` channel (MQTT 5 or 3.1.1; client id `foresignal-{installId}-{channelIdShort}`; reconnect 1 s doubling to 120 s with jitter; TLS with the system trust store plus optional `caCertPem`; Last Will `{prefix}/status` = `offline`, retained). Topics: `{prefix}/alerts/{severity}`, `{prefix}/alerts/updates`, `{prefix}/briefings/{id}` (retained), `{prefix}/system/events`, `{prefix}/rules/{ruleId}/state` (retained), `{prefix}/sources/{sourceId}/health` (retained; never private sources), `{prefix}/summary` (retained glance), `{prefix}/test`. Segments replace `+`, `#`, `/`, NUL and control characters with `_`; payloads are UTF-8 JSON ≤ 256 KB. Home Assistant discovery publishes one device-based config with severity sensors, source counts, per-rule and per-source problem sensors, republished on HA birth messages. `/v1/system` `mqtt` block; metric `foresignal.mqtt.connected{channel}`.

### 17.5 Interactive chat commands (061)

The operator can talk back from the Telegram and Matrix channels used for alerts; this is the one exception to the Charter non-goal on sending chat messages. Foresignal replies only to allowlisted chats (`allowedChats[]` ≤ 50, scope `read` or `agent`, never `admin`). Connectors use bot interfaces only; `ChatopsManager` supervises one virtual thread per served channel with cursors in `chatops_cursor` and confirmations in `chatops_pending`. Each chat is the principal `chat:{channelId}:{chatId}[:{userId}]`, decided exactly as a token. Commands mirror REST operations and pass `AccessPolicy` for the chat's scope: `/help`, `/status`, `/alerts`, `/alert`, `/ack`, `/resolve`, `/snooze`, `/ask`, `/brief`, `/sources`, `/confirm`, `/reject`, `/feedback`. Replies are summaries and links, never raw payloads; every command is audited. Telegram bot chats and the Matrix rooms used are not end-to-end encrypted.

## 18. MCP server

- **Transport:** the MCP Java SDK 2.0.1 servlet transports, registered directly: streamable HTTP at `/mcp` (`FORESIGNAL_MCP_ENABLED`) and, optionally, the legacy HTTP+SSE transport at `/mcp/sse` and `/mcp/message` (`FORESIGNAL_MCP_LEGACY_SSE`). Bearer Foresignal tokens, or identity-provider access tokens when remote authorization is configured (§42); with `openReads` on, read tools also work without a token.
- **Tools** are thin wrappers over the service layer (no HTTP loopback), each declaring its scope; every read tool joins the read catalog that Ask, the analyst and the cycles use. Groups cover records, search, sources, alerts, watches, briefings, system, places and weather, satellite passes, maps, intelligence events and investigations, entities, anomalies, backtests, exports, share links, peers, pattern of life and trips, exercises and tuning, every intelligence-layer object (§40), the doctor (§41) and tracks (`get_track`, optionally CZML). The full catalog is in the [Agent Guide](Agent-Guide.md).
- **Output shaping:** `detail: short|long`, list caps with `*_total` and `truncated`, optional geometry simplification; compact by default.
- **Resources and prompts:** `foresignal://sources`, `foresignal://kinds`, `foresignal://products`, `foresignal://places`, product legends, and a `situation-report` prompt.
- Every LLM-spending tool is always authenticated and charged to the `expensive` rate-limit class.

## 19. LLM subsystem

Optional. Without a provider, Foresignal is fully functional: briefings use digest mode, search uses text matching, enrichment is skipped, the analyst and cycles run deterministically, and Ask returns `409 LLM_NOT_CONFIGURED`.

- **Providers** live in `llm_provider` (`/v1/llm/providers`); roles in `app_setting['llm']` (`chatProvider`, `embeddingProvider`, `ttsProvider`, `askEnabled`, `enrichmentEnabled`, `logContent`, …).

| Type | Implementation | Notes |
|---|---|---|
| `anthropic` | Anthropic Java SDK 2.71.0 | Default model `claude-opus-5-5`. Adaptive thinking; depth is set with `effort` per use (Ask and briefings `medium`, enrichment, doctor and translation `low`). Forced `tool_choice` is not used. Server-side refusal fallback is enabled and `stop_reason == "refusal"` is checked. Long outputs stream. Stable system prompts and tools are cached. **No embeddings endpoint**: pair it with an embedding provider |
| `openai-compatible` | `java.net.http` client for `/chat/completions`, `/embeddings` and `/audio/speech` | OpenAI, OpenRouter, Groq, Mistral, vLLM, LM Studio, llama.cpp server, LocalAI |
| `ollama` | `java.net.http` client for `/api/chat` and `/api/embed` | Local (`http://ollama:11434` with the `local-llm` profile) |

- **`LlmClient`:** `chat(system, messages, tools, options)` and `embed(texts)`; tool calling normalized across providers; a model without tools runs Ask in retrieval-only mode.
- **Prompt templates** live in `src/main/resources/llm/templates/{id}@{version}.md` (`PromptTemplates`) and are recorded in analysis manifests.
- **Budgets:** `requests_per_minute` and `tokens_per_day` per provider, checked before and recorded after each call in `llm_usage`; exhaustion refuses with `LLM_BUDGET_EXCEEDED` and raises a system notification. Speech is budgeted in characters.
- **Safety:** record content is **untrusted** and passed as delimited data; models never change sources, credentials, channels, providers, tokens or settings; model-initiated writes are *proposed* actions executed only after confirmation (or automatically with `agent` scope and `allowActions`). Every call is logged with metadata; content only with `logContent=true`.
- **Knowledge cutoff:** each model may declare `knowledgeCutoff`; the forecast ledger excludes leakage-prone questions when evaluating an LLM forecaster (§40).
- **Data egress:** with a remote provider, the records the model reads go to that provider; private and `LOCAL_ONLY` data follow `EgressPolicy`. The local Ollama profile keeps everything on the box.

## 20. Embeddings and semantic search

- **Storage:** `embedding_document` holds one row per embedded document (doc types `record` for the latest version of a record, `briefing` for a briefing run, `alert` for an enriched alert), with `geom`, `observed_at`, `source_id` and `kind` for filtering, a generated `tsv` (`to_tsvector('simple', …)`, GIN), GiST on `geom` and `(source_id, effective_time)`.
- **Vector slots:** two slots (`embedding_a`/`model_a`, `embedding_b`/`model_b`) with HNSW indexes `embedding_document_hnsw_a`/`_b` (`vector_cosine_ops`). They are **not in the Flyway baseline**: no default dimension is configured, so `EmbeddingSchemaManager` creates a slot at the active embedding model's dimension, and `ReindexService` fills the other slot when the model or dimension changes and then swaps. This is one of the dynamic-DDL exceptions of §25.
- **What is embedded:** records of sources that declare `embedFields` (text-heavy feeds), plus briefing runs and enriched alerts. `EmbeddingWorker` follows the ChangeBus with the durable `embedding.cursor`, batches 64 texts per call, skips an unchanged `text_hash`, respects the provider budget, and never embeds private sources.
- **Search:** `GET /v1/search?q=&mode=semantic|text|hybrid&source=&kind=&bbox=&since=&until=&limit=`; `text` uses `tsv` (falling back to `record_head.search_text`), `semantic` cosine distance, `hybrid` reciprocal rank fusion. `GET /v1/search/status` explains why semantic search is off when it is.

## 21. Ask Foresignal

- `POST /v1/ask` (`{question, threadId?, scope?, allowActions?}` → answer, citations, proposed actions, usage) and `POST /v1/ask/stream` (SSE `progress`, `delta`, `citation`, `action`, `done`); threads persist in `ask_thread` and `ask_message`.
- **Loop:** the chat provider with the read tool catalog plus `propose_*` tools (watch, briefing, ack, feedback, tuning); at most 12 tool iterations, 120 s, and a token cap (`llm.askMaxTokens`).
- **Citations are verified:** ids that never appeared in tool output are dropped; uncited factual claims are flagged.
- **Proposed actions** are stored in `ask_action` as `pending`; `POST /v1/ask/actions/{id}/confirm` (agent) executes one. Ask requires `read` (or open reads); actions require `agent`. The tool loop runs with the caller's principal and restriction.

## 22. Scheduled briefings

- **Definitions** (`briefing`): 5-field cron plus IANA timezone, scope (sources, kinds, bbox, rule ids, places, weather products, `includeHealth`, `includeAlertQuality`), `lookback`, `mode` `digest` or `llm`, instructions, channel ids, optional `audio`.
- **`BriefingScheduler`** checks every 30 s; missed runs after downtime run once.
- **Digest mode** is deterministic markdown (alerts by severity, counts with deltas, top records by `rankField`, source health, weather at places, optional alert quality). **LLM mode** adds retrieved records and instructions and falls back to digest on failure.
- **Output:** `briefing_run` with markdown and a detached JWS signature (§33), HTML and JSON renditions, embedded, delivered as a `briefing` event; optional MP3 audio and a podcast feed (§42). `POST /v1/briefings/{id}/run`, `POST /v1/briefings/preview`, `GET /v1/digest`.

## 23. Alert enrichment

When a rule with `enrich=true` fires and `llm.enrichmentEnabled` is on, the `EnrichmentWorker` (an AlertBus listener) gathers the record and its history, records within `enrichRadiusKm` and `enrichWindow`, semantic neighbors, nearby alerts and contributors such as the weather context (§27.13). The chat provider (effort `low`) writes a summary of at most 120 words with citations, stored in `alert.summary` and `enriched_at`, emitted as `alert.enriched`, and embedded. Enrichment never blocks delivery beyond the channel's wait.

## 24. Push

Foresignal pushes notifications itself, without a third-party account for the web. All transports share one SPI and one fan-out (031); native push (032) adds transports and targets.

### 24.1 Web Push

A VAPID key pair (P-256) is generated on first start (`system_secret`; private key encrypted). Messages use RFC 8291 encryption in the RFC 8188 `aes128gcm` coding with JDK crypto (`Hkdf` on `HmacSHA256`) and an ES256 VAPID JWT (RFC 8292) cached per origin. Requests carry `TTL`, `Urgency` and a `Topic` derived from the entity, so the push service replaces an undelivered message about the same entity. Payloads (`foresignal` format, or the WebKit declarative format) stay within 3,993 bytes (`PushPayloadBudget`). Subscriptions (`POST /v1/push/subscriptions`, read, authenticated) start `pending` and become `active` only after the creating token echoes a verification code, so a read token cannot use Foresignal as a push amplifier. Endpoints pass `OutboundUrlPolicy` profile `PUSH_ENDPOINT` on save and before every send.

### 24.2 UnifiedPush

A UnifiedPush distributor exposes an RFC 8030 endpoint and decrypts RFC 8291 messages, so to Foresignal it is a Web Push subscription with `transportLabel: unifiedpush`. A self-hosted LAN distributor is reached through `push.allowedPrivateHosts`; https is still required.

### 24.3 Native push (032)

APNs (iOS, iPadOS, macOS) and FCM (Android) are further `PushTransport`s. Each operator configures their own APNs key and Firebase service account (`/v1/push/providers`, admin; keys encrypted), so native push works for an operator who builds and signs their own app; UnifiedPush and ntfy remain the account-free paths. Devices (`/v1/push/devices`) are upserted by transport and token hash, ≤ 20 per token, deleted with their token. Payloads carry ids, severity and a minimal text only (`contentLevel` `minimal` or `short`); full content is fetched by the app over the user's own path. APNs uses HTTP/2 with an ES256 provider JWT; FCM HTTP v1 with an OAuth token minted from the service account (no `firebase-admin`). Provider errors set `push_provider.last_error` and raise one `system.push_provider_failed`.

### 24.4 SPI and delivery

`PushTarget` (sealed), `PushTransport`, `PushResult` (`SENT | GONE | RETRY | REJECTED | FAILED`), `PushTargetSource`. The built-in channel "Push" inserts one `push_dispatch` row per matching target; `PushDispatchWorker` (2 s, `FOR UPDATE SKIP LOCKED`, leases) sends each on a virtual thread with the retry ladder per target and a per-host limit; quiet hours follow `quietHoursMode` (`defer`, `downgrade`, `drop`).

## 25. Production hardening: the Flyway regime (144)

From v1.0.0 **Flyway owns the database schema**. Hibernate maps and validates; it never creates or alters a table.

- **Dependencies:** `spring-boot-starter-flyway` (Boot 4.1.1) and `org.flywaydb:flyway-database-postgresql` 12.4.0, the version the Boot 4.1.1 BOM manages (Apache-2.0).
- **Baseline:** `src/main/resources/db/migration/V1__baseline.sql`, ordered and commented by subsystem. It contains:
  - `CREATE EXTENSION IF NOT EXISTS postgis` and `vector` — **pgvector is required at migration time**;
  - `record_seq`; the partitioned `record_version` (LIST on `history_tier` → `record_version_std` (0) and `record_version_trk` (1), each RANGE on `seq`; PK `(seq, history_tier)`; the three partitioned indexes; **no leaves and no DEFAULT partition**); the `chronicle_version` partitioned parent (LIST on `chronicle_id`, no leaves); `record_version_held`;
  - every entity table, and every index the former bootstrap initializers created: the `record_live` GiST index with `fillfactor=70` and its autovacuum settings, the `alert_rev` sequence and `alert_open_idx`, the place, trigger, `ref_*`, gazetteer, `raster_frame` (`footprint` GiST, `(product_id, valid_time DESC)`, `(product_id, reference_time, forecast_step)`), trip, situation, analytic-finding and infrastructure GiST and GIN indexes, the entity partial unique indexes, and `embedding_document`'s generated `tsv` with its GIN index, the GiST index and `(source_id, effective_time)`.
  - **Not in the baseline, deliberately:** the enum `CHECK` constraints Hibernate would generate (the application enforces enums; this replaced `DeliveryStatusConstraint`), and the embedding vector slots (§20).
- **Removed:** `SchemaBootstrap`, `HistorySchemaInitializer`, `PostgisExtensionInitializer`, `VectorExtensionInitializer` (replaced by a read-only pgvector presence check), `SpatialIndexInitializer`, `ChronicleSchemaInitializer`, `ExportSchemaInitializer`, `InfraSchemaInitializer`, `DeliveryStatusConstraint`, and the tests that only covered them.
- **Configuration:** `spring.jpa.hibernate.ddl-auto=validate`; `hibernate.hbm2ddl.extra_physical_table_types: PARTITIONED TABLE` kept so validation accepts the partitioned parents; `spring.flyway.locations=classpath:db/migration`, `baseline-on-migrate=true`, `baseline-version=1`. An existing pre-1.0 development database (Hibernate `update` plus bootstrap code, no history table) is **adopted at version 1 without running V1**, and a warning tells the operator to compare it with `scripts/db/dump-schema.sh` or recreate the volume with `docker compose down -v`. An empty database runs V1.
- **PartitionManager after Flyway:** the `HistoryPartitionBootstrap` bean runs `PartitionManager.ensureAhead()` after the Flyway initializer and before the EntityManagerFactory, so the writer always finds leaves ahead of it (§7.3).
- **Dynamic DDL that remains, by design** (the allowlist of `NoBootstrapDdlTest`):
  - `PartitionManager`: `record_version` leaf create, attach, detach, drop;
  - `HistoryDownsampler`: the leaf rewrite and swap (§11);
  - `ChronicleDdl`: per-chronicle `chronicle_version` leaf create and drop;
  - `EmbeddingSchemaManager` slot DDL and `ReindexService`: vector columns and HNSW indexes at the configured dimension;
  - `ReferenceDataLoader`: staging tables for bulk reference loads (working tables, not schema);
  - `GeoPackageWriter` and `MbtilesWriter`: SQLite DDL inside export files, not PostgreSQL.
- **Migration conventions:** `V{n}__{description}.sql`; additive by default; one migration per change; **never edit an applied migration**. The [Adapter Guide](Adapter-Guide.md) carries the same rules for adapter authors.
- **External PostgreSQL:** on a database where Foresignal's role is not a superuser, a superuser must run `CREATE EXTENSION postgis` and `CREATE EXTENSION vector` before the first start.
- **Baseline verification is owed.** The baseline was derived by hand from the 238 JPA entities and the former bootstrap SQL, because no database could be started under the 2026-10-17 directive. The owner verifies it by running `scripts/db/dump-schema.sh` against a pre-cutover `ddl-auto=update` database and against a Flyway-built one and diffing them; `FlywaySchemaParityIT` automates the comparison and `FlywayBaselineIT` checks the partition layout and that `ensureAhead` creates leaves afterwards (both written, not run).
- **API-compatibility gate:** `docs/OpenAPI.json` `info.version` is `1.0.0` (it was 1.3.0, a pre-release document version), and `docs/api/v1.0.0/OpenAPI.json` is the frozen contract. `./gradlew checkApiCompat` runs `oasdiff breaking docs/api/v1.0.0/OpenAPI.json docs/OpenAPI.json --fail-on ERR` with a local `oasdiff` binary, else the Docker image `tufin/oasdiff:v1.33.0`, else skips with a warning. It is not wired to `build`. Under NFR-10, `/v1` stays additive only; a breaking change goes to `/v2` (§10.1).
- **CI:** no CI configuration is added (standing owner rule). The proposed pipeline (stages build, test, api, lint) is in `docs/CI-Proposal.md` for the owner to adopt.
- **Release:** the tag `v1.0.0` is an owner action after the full suite passes.

## 26. Reuse policy and reference projects

| Project | License | Use |
|---|---|---|
| God's Eye View | MIT | **Port with attribution.** Adapter fetch and parse logic and the GFS/HRRR wind logic (`GribIdx`, `IdxRangePlanner`, `ModelCycleSelector`, `WindResampler`, 051) are ported to Java; each ported file carries an origin header and is listed in `THIRD_PARTY_NOTICES.md` (Copyright 2026 Bilawal Sidhu). Its `weather.js` proxy is reference only and not ported; bundled non-MIT datasets and 3D models are not used |
| SatDump | GPL-3.0 | **Reference only.** A separate, operator-supplied process; Foresignal talks to it only through files (its output directory under the inbox) and HTTP (`/api`, `/polarplot.jpeg`). No SatDump code, headers, structs or projection code in Foresignal, and no SatDump binaries or SatDump-produced images in the Foresignal image (the `satdump` compose profile uses an image the operator builds). Parsers are written from observed output and fixtures; the geostationary math comes from the public CGMS and GOES-R PUG specifications; polar overlays read SatDump's EPSG:4326/3857 GeoTIFFs and estimate swaths with Orekit; SatDump's projection and raytracer are never reimplemented. Any Foresignal-authored AngelScript hook is MIT, a separate file, never linked |
| ShadowBroker | AGPL-3.0 | Reference only: endpoints, parameters, quirks. No code copied |
| Velocity | AGPL-3.0 | Reference only |
| World Monitor | AGPL-3.0 | Reference only |
| Meshtastic firmware and protobufs | GPL-3.0 | **Excluded.** JSON field names of the firmware's MQTT output were checked against `MeshPacketSerializer.cpp`; **no Meshtastic code, protobuf class or `.proto` file is in the repository**. Every Meshtastic path reads JSON; protobuf topics (`/2/e/`, `/2/map/`) are never subscribed and, if delivered, are dropped unparsed (`MeshtasticJsonParser.isProtobufTopic`). nRF52 nodes that cannot emit JSON need an out-of-tree bridge |
| MeshCore | MIT | Protocol reference for mesh sources and channels; the companion protocol is implemented in `source.formats.meshcore`, written from MeshCore's documentation (consulted, not copied) |
| Meta ThreatExchange (PDQ) | BSD | The PDQ perceptual hash is ported to `ai.foresignal.social.narrative.Pdq` with an origin header and notice (136) |
| MeshDTN | The owner's project (firmware and app are MIT forks of MeshCore and meshcore-open) | Store-carry-forward mesh partner: its specifications define what Foresignal sends over mesh (084's CBOR profile, 113's compact messages) |

Reference designs named in the [Intelligence Design](Intelligence-Design.md) §14.2 are consulted under the same rule: reference only unless the license is MIT or BSD and the port is attributed.

**What each project contributed:** God's Eye View — camera catalogs, transit, bikeshare, fire perimeters, radio and weather-model sources. ShadowBroker — the second main input to the source inventory, and the cautionary tale: the Meshtastic project called out its traffic as one to two orders of magnitude above typical clients, which is why Foresignal meters every source and enforces budgets. Velocity — keyless and official feeds and enter/exit geofence alerting. World Monitor — cyber, conflict, disasters, sanctions, outages and energy series. All four together — the product shape, separated into a headless broker that any map or app uses as a client. SatDump — the station integration (049, 054, 107). MeshCore — mesh sources and channels (083, 084). MeshDTN — compact messages for delay-tolerant mesh delivery (084, 113).

**Where to get them:** prompts reference these at fixed paths under `~/Documents/GitLab/`, as siblings of `foresignal`; `gods-eye-view` is required because prompts may port from it, the rest are read-only references.

| Local path | Clone from |
|---|---|
| `~/Documents/GitLab/gods-eye-view` | `http://192.168.1.134/mirrors/gods-eye-view.git` (upstream https://github.com/bilawalsidhu/gods-eye-view) |
| `~/Documents/GitLab/Shadowbroker` | https://github.com/BigBodyCobain/Shadowbroker.git |
| `~/Documents/GitLab/velocity` | https://github.com/AndrewCTF/velocity.git |
| `~/Documents/GitLab/worldmonitor` | https://github.com/koala73/worldmonitor.git |
| `~/Documents/GitLab/SatDump` | `http://192.168.1.134/mirrors/SatDump.git` (upstream https://github.com/SatDump/SatDump) |
| `~/Documents/GitLab/MeshCore` | `http://192.168.1.134/mirrors/MeshCore.git` |
| `~/Documents/GitLab/meshdtn` | https://gitlab.com/adam.allard/meshdtn.git |
| `~/Documents/GitLab/meshdtn-firmware` | https://gitlab.com/adam.allard/meshdtn-firmware.git |
| `~/Documents/GitLab/meshdtn-app` | https://gitlab.com/adam.allard/meshdtn-app.git |

**Dependency repositories:** Maven Central, with the one Unidata exception of §2.2.

## 27. Weather, rasters and imagery

This part folds in the weather and imagery design (prompts 042–056 and 105–108). Its goals: weather becomes an intelligence input (radar, lightning, fire, storm objects and forecast thresholds become records the trigger engine can alert on, and **missing data never reads as an all-clear**); self-hosted overlays for every map client; the operator's ground station as a first-class source; agents that can read the weather with explicit validity, run, age and coverage; and a fit on a home server.

### 27.1 Settled choices

| Question | Decision |
|---|---|
| Decoder | NetCDF-Java 5.11.0 (`cdm-core`, `grib`, `cdm-radial`) with `libaec-native`, from the Unidata repository only (§2.2). One Java 21 stack decodes GFS (5.3), MRMS (5.41), GOES NetCDF4, IFS (5.42) and NIDS |
| Where frames live | `raster_frame` is the source of truth. Observation products write one `raster-frame` record version per frame; forecast products one per **completed run**, so SSE, resume, MCP and triggers work with no new code |
| URL namespace | `/v1/products` (FR-55) |
| Rendering | On-the-fly PNG tiles from compressed native-grid frames with an index-map cache; the newest frame is warmed for the places AOI up to the animation zoom |
| Immutable caching | Only content-addressed URLs (`{timeKey}~{sha8}`, `name@v`, pinned `run`, complete frame) are immutable; everything else gets a short max-age and an ETag |
| Map-client auth | Tile keys in the path (`/v1/k/{key}/…`, §15.1) |
| Overload | Bounded wait ≤ 1.5 s with priority and per-client fairness, then `503 RENDER_BUSY` |
| Raster alerts | Derived records only, plus `cooldownScope` and `near` in the trigger engine |
| Unknown vs nothing nearby | "None within 100 km" is the numeric sentinel `999`; "unknown" keeps the last values and sets a section `status` with `asOf`; `null` is never used for a metric |
| SatDump | One source per station, `satdump-{station}` |
| Hosting | Foresignal hosts products derived from data it ingests; it never proxies basemaps or third-party tiles; GIBS and nowCOAST are `kind: external` catalog entries clients fetch directly |

### 27.2 Components

```
 Upstreams (NODD S3, NCEP, AWC, NWS, SPC, NESDIS, ECMWF)        SatDump (separate GPL process)
        │ budgeted ctx.http(): listS3 / getRange / download        │ /api (optional)   │ output dir (ro mount)
        ▼                                                          ▼                   ▼
 ┌─ ① Source adapters (existing scheduler, budgets, health, probe) ─────────────────────────────────┐
 │  rasters: mrms · goes-east-abi · goes-west-abi · gfs · hrrr · ecmwf-ifs · aifs · rtma-ru · nbm · … │
 │  station: satdump-{station} (api loop + inbox loop)                                                │
 │  records: nws-alerts · mrms-probsevere · spc-* · awc-* · goes-glm · goes-fdc · hms-* · nws-gridpoints │
 └──────┬──────────────────────────────────────────────────────────┬────────────────────────────────┘
        │ temp file → ② Raster pipeline (bounded decode pool)        │ NormalizedRecords
        │   decode → quantize → stats → footprint → ③ FrameStore     │
        │   → raster_frame row → raster-frame record ─────────────────┤
        ▼                                                             ▼
 ④ Catalog (ProductRegistry; raster_product/grid/frame)        IngestQueue → single writer → ChangeBus
        ▼                                                             │   ├─ SSE (raster-frame payload inline)
 ⑤ Serving: TileRenderer · RenderQueue · FieldService · SampleService │   └─ TriggerEngine → alerts → channels
    · Loop/Image · Legend · MapRenderer (static PNG) · WMTS · OGC      │
        ▲                                                             │
 ⑥ DERIVED generators (no TTL; emit on change; tombstone; reconcile) ◄┘ frame events, GLM ring
    wx-places · wx-areas · wx-cells · wx-forecast-areas · wx-storm-tracks
```

Packages: `raster` (catalog, frame store, grids, pipeline, retention), `raster.decode`, `raster.lightning`, `raster.model`, `raster.render`, `raster.sample`, `raster.geotiff`, `raster.export`, `media`, `place`, `derive`, `orbit`, `access`, `weather` (§3).

### 27.3 Source-framework extensions

| Concern | Extension |
|---|---|
| Descriptor | Optional `List<ProductSpec> products`; `kindRetention`; `zeroResultsNormal=true` for product sources; a `products` parameter with small and large profile defaults (106, `ProductEnablement`) |
| Scheduler | Heavy decodes run on a bounded platform pool behind a semaphore (decode concurrency 1 small, 2 large), not on virtual threads |
| UpstreamClient | `getRange`, `download`, `listS3`, per-descriptor response caps (§5.2) |
| Budgets | Optional `mbPerDay`, charged in advance and corrected; `BudgetOutlookHealthRule` degrades when the projected day exceeds 120% of it |
| Health | Frame-age rule: degraded when the newest frame is older than `3 × cadence` (or `maxGap`; forecast products use the run cadence); `/health` adds `newestFrameAt` and `frameAgeSeconds`. A missing `.idx` field or variable is a silent format change: health degrades and no empty frame is written |
| Probe | Lists the newest object (or reads the `.idx`), range-reads one message, decodes the header only, writes nothing |
| DERIVED mode | Fed by the ChangeBus or frame events; enabled, parameterized, health-tracked and retained like any source. Derived records have `recordTtl = null` (except a TRACK fused layer, §30); liveness comes from explicit tombstones and startup reconciliation |
| ChangeBus / SSE | `externalId` filter on `/v1/changes` and the stream; `kind=raster-frame` events always carry their small payload (< 1 KB) |

### 27.4 Data model

**`raster_product`:** `id` (kebab) · `source_id` · `category` (radar, satellite, model, lightning, imagery) · `kind` (raster | field | external) · `spec` jsonb (units, dtype, scale, offset, nodata, codes, styles, default style, `temporal{type, cadence, maxGap}`, `spatial{coverage, nativeMaxZoom, minZoom}`, retention ladder, structured attribution) · `dynamic` · timestamps. A durable mirror of the in-memory `ProductRegistry`.

**`raster_grid`:** `id` = SHA-256 of the canonical definition · `type` (latlon | geos | lcc | affine) · `definition` jsonb · `nx` · `ny` · `bbox`. Frames on one grid share a row.

**`raster_frame`:** PK (`product_id`, `frame_id`), `frame_id` time-based and immutable (`20261010T143240Z`, `2026101012Z-f006`) · `valid_time`, `reference_time`, `forecast_step`, `obs_start`, `obs_end` · `grid_id`, `path`, `bytes`, `sha256` of the decoded content (its first 8 hex characters are the URL token `sha8`), `dtype`, `codec` · `stats` jsonb, `footprint` (MultiPolygon) · `upstream_key`, `status` (partial | complete), `thinned`, `run_complete`, `ingested_at`, `expires_at`. Indexes: `(product_id, valid_time DESC)`, `(product_id, reference_time, forecast_step)`, GiST `(footprint)`.

**`media`:** ULID `id` · `sha256` · `content_type` · `width` · `height` · `bytes` · `path` · `role` (image, thumbnail, projected, map, document, audio) · `source_id` · `record_source_id` · `record_external_id` · `licence` · `created_at` · `expires_at` (null follows the owning record) · `variant_of` · `variant_width`. Content-addressed files under `media/{ab}/{sha256}.{ext}`, staged and renamed only after `ImageGuard` passed; adapters store images only through `ctx.media()`. **`media_tombstone`** keeps deleted ids for 30 days.

**Changes to existing tables:** `trigger_rule.cooldown_scope` (record | rule) and `near` (materialized into the geofence and re-materialized when a place moves); `alert.context` jsonb; channel config `attachMap`, `mapProducts`, `mapRadiusKm`, `maxAttachmentBytes`. Tile keys reuse `api_token`.

**`.fsg` frame file** (little-endian, 64-byte header, golden-file test; `ElgCodec`):

| Offset | Size | Field |
|---|---|---|
| 0 | 4 | magic `FSRG` |
| 4 | 2 | version u16 = 1 |
| 6 | 1 | dtype u8: 1 u8, 2 u16, 3 i16, 4 f32, 5 rgba8 |
| 7 | 1 | codec u8: 0 none, 1 deflate |
| 8 | 4 | nx u32 |
| 12 | 4 | ny u32 |
| 16 | 2 | bands u16 |
| 18 | 2 | flags u16 (bit 0 = partial) |
| 20 | 4 | reserved, 0 |
| 24 | 8 | scale f64 |
| 32 | 8 | offset f64 |
| 40 | 8 | nodata f64 (raw code) |
| 48 | 8 | payloadLength u64 (compressed) |
| 56 | 4 | crc32c u32 of the payload |
| 60 | 4 | reserved, 0 |

Band-sequential payload, rows north to south; physical value = `code × scale + offset`. **MRMS reflectivity u8 codes:** 0 = no coverage (MRMS −999), 1 = no echo or below −32 dBZ (MRMS −99), 2..255 = `clamp(round(dBZ×2)+66, 2, 255)`. **Quantization:** reflectivity u8, precipitation rate u16, GOES IR u16 (0.01 K, offset 150), model scalars i16 with scale and offset, wind fields f32, SatDump composites rgba8. Frame compression is JDK `Deflater(1)`.

### 27.5 Storage layout and retention

```
${FORESIGNAL_DATA_DIR}/rasters/tmp/                                  staging (atomic rename)
${FORESIGNAL_DATA_DIR}/rasters/frames/{productId}/{yyyy}/{mm}/{dd}/{frameId}.fsg
${FORESIGNAL_DATA_DIR}/rasters/fields/{productId}/{run}/{step}.f32.gz   God's Eye View format, precompressed
${FORESIGNAL_DATA_DIR}/rasters/glm/{sat}/{yyyymmdd}.bin                  16 bytes per flash
${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/{passId}/               provenance copies
${FORESIGNAL_DATA_DIR}/media/{ab}/{sha256}.{ext}
${FORESIGNAL_DATA_DIR}/cache/netcdf/                                     NetCDF-Java disk cache (pinned)
${FORESIGNAL_DATA_DIR}/exports/                                          raster exports (48 h) and §35 exports
${FORESIGNAL_DATA_DIR}/tmp/                                              java.io.tmpdir, jna.tmpdir (exec allowed)
${FORESIGNAL_INBOX_DIR}/satdump/{station}/                               read-only mount
```

Per-kind record retention follows §11. Descriptor defaults: `raster-frame` 7 d (versions outlive frames), `satellite-pass` 365 d, `ground-station` 30 d, `place-weather` 14 d, `wx-area`, `fire-detection`, `fire-cluster`, `smoke-polygon`, `metar` 7 d, `radar-cell`, `lightning-cluster`, `probsevere-object` 3 d, `nws-alert`, `storm-report` 30 d, `spc-outlook` 14 d, unless the analytic class of Conventions sets a longer window. Raster ladders, the quota and the orphan sweep are in §11; SatDump polar frames 30 d; the GLM archive 14 d; media follows its record (map attachments 7 d).

### 27.6 Budgets (defaults in code; operator-overridable)

| Source | perMinute / perDay / mbPerDay |
|---|---|
| `mrms` | 30 / 12,000 / 4,500 |
| `goes-east-abi` | 10 / 2,000 / 2,500 |
| `goes-glm` | 10 / 10,000 / 3,000 |
| `goes-fdc` | 4 / 800 / 300 |
| `gfs` | 60 / 3,000 / 1,000 |
| `hrrr` | 60 / 4,000 / 2,000 |
| `ecmwf-ifs`, `aifs` | 20 / 3,000 / 1,200 |
| `mrms-probsevere` | 4 / 1,600 / 300 |
| `awc-metars` | 2 / 400 / 100 (AWC asks for ≤ 1 request per minute per thread) |
| `hms-smoke`, `hms-fire` | 4 / 200 |
| `satdump-{station}` | 60 / 50,000 (LAN only; each inbox scan counts as one request) |

### 27.7 Memory and concurrency profiles

Selected by `FORESIGNAL_PROFILE=small|large`, never derived from `MaxRAMPercentage`; every value can be overridden on its own (§13).

| | Small (32 GB host, default) | Large (64 GB host) |
|---|---|---|
| JVM (`FORESIGNAL_JAVA_TOOL_OPTIONS`) | `-Xmx4g -XX:+UseG1GC` | `-Xmx8g -XX:+UseZGC -XX:+ZGenerational` |
| Container limit (`FORESIGNAL_MEM_LIMIT`) | 6 GB | 11 GB |
| Frame / tile / index-map cache | 768 / 128 / 64 MB | 1536 / 256 / 128 MB |
| Render pool | min(cores − 2, 6) | cores − 2 |
| Decode concurrency | 1 | 2 |
| HRRR runs | every 6 h | every 3 h |
| GLM ring and `radar-cell` scope | places AOI | AOI (CONUS opt-in) |
| Product defaults (106) | GOES-East C13 + C08/C09, RTMA-RU, NBM, OVATION, TAF, HRRR smoke, HeatRisk, UV | also C02, GOES-18 C13, GFS-Wave, MRMS extras, SST, AIFS |

All caches are Caffeine with byte weighers; hit ratios and evictions appear in `/v1/system` `rasters`. Ollama (8–20 GB) plus PostgreSQL plus the small profile is the limit of a 32 GB host. Optional GDAL subprocesses (up to 1.5 GB RSS) stay off the hot path.

### 27.8 Toolchain and license boundaries

| Concern | Choice | License |
|---|---|---|
| GRIB2, NetCDF4/HDF5, NIDS | NetCDF-Java 5.11.0 + libaec 1.1.7.0; `Grib2RecordScanner` (no `.gbx9`/`.ncx4`); no `netcdf4` module; only Foresignal temp paths are passed to `NetcdfFiles.open` (it accepts URLs) | BSD-3; BSD-2; jj2000 permissive; JNA Apache-2.0 option |
| CBOR | `jackson-dataformat-cbor` | Apache-2.0 |
| GeoTIFF read | JDK `javax.imageio` TIFF plus a GeoKey parser (tags 33550, 33922, 34735); EPSG:4326 and 3857 only; BigTIFF rejected or routed to GDAL | JDK |
| Image guard | `ImageGuard` reads the header and rejects anything over 12,000 px on a side or 150 MP **before** decoding | — |
| Reprojection | In-house `latlon`, `geos` (GOES-R PUG / CGMS), `lcc`, `affine`; no GeoTools, no proj4j | — |
| PNG, WebP | JDK ImageIO PNG-8 with `IndexColorModel` and tRNS; webp-imageio (108) | JDK; Apache-2.0 |
| SGP4 | Orekit 13.1.9 with a pinned minimal `orekit-data` | Apache-2.0 |
| Static maps | Natural Earth 1:50m, preprocessed at build | Public domain |
| Barb MVT | PostGIS `ST_AsMVT` over unnested arrays | — |
| GDAL (optional) | Build arg `WITH_GDAL`; `ProcessBuilder` only; no AGPL drivers, no JNI | MIT/X |

Notices also list jj2000, libaec, JNA, Natural Earth, the viridis and cmocean palettes and ECMWF CC BY 4.0 (with "changes made" on every IFS-derived output: `EcmwfDefinition.CHANGES_MADE`). The SatDump, AGPL and God's Eye View boundaries are §26.

### 27.9 Grids, rendering, styles and caching (045)

- **Grids** (`GridDef`, sealed): `LatLonGrid`, `GeosGrid` (GOES-R fixed grid / CGMS normalized geostationary projection; off-disc points NaN), `LccGrid` (spherical Lambert conformal conic, HRRR, RTMA-RU), `AffineGrid` (GDAL geotransform in EPSG:4326 or 3857). Every parameter comes from `raster_grid.definition`.
- **Renderer** (`raster.render`): WebMercatorQuad 256 px tiles and `@2x`; `IndexMapCache` maps `(grid, z, x, y, scale)` to source cells (bilinear where the product says so); band-0 codes go through the compiled style to palette indices; PNG-8 (rgba8 passthrough writes 32-bit PNG). `TileCache` holds encoded tiles keyed `(product, frameKey, style@v, z/x/y@scale)`. Zooms outside `[minZoom, nativeMaxZoom]` are `400 TILE_OUT_OF_RANGE`; empty tiles are one shared transparent PNG with a fixed ETag (`?empty=204` opts into 204). `/image` (≤ 2048 px, bounds snapped and returned in `Foresignal-Image-Bounds`) and `/loop` (≤ 24 frames over one snapped box, one forecast run) use the same path.
- **Styles** (`StyleRegistry`): `src/main/resources/raster/styles/{name}@{version}.json`, compiled per product to ≤ 256 colors and a code table; only registered names are accepted (`400 UNKNOWN_STYLE`); TileJSON, frames, `/loop` and SSE always carry `name@v`. Legends as JSON (with unit conversion and `meaning` text) and a Java2D PNG.
- **Cache policy** (`RasterCachePolicy`, one rule for tiles, images, fields and media): content-addressed URLs are `{public|private}, max-age=31536000, immutable`; everything else `max-age=min(cadence/2, 60s)` with an ETag checked before rendering and `Foresignal-Frame-Time`. `public` only when `openReads` covers the path and the source is not private. A stale `sha8` is `404 UNKNOWN_FRAME` with a `Link` to the current key; an expired frame is `410 FRAME_EXPIRED`, or the transparent tile with `Foresignal-Frame-Expired: true` under `?onExpired=transparent`.
- **Render queue** (`RenderQueue`): priority warming > interactive > prefetch (`Foresignal-Prefetch: 1` or `?prefetch=1`); round-robin per client; single-flight per key; a caller waits at most 1.5 s, then `503 RENDER_BUSY` with `Retry-After: 1`.
- **Warming** (`TileWarmer`): each committed complete frame queues its tiles over the places-AOI envelope up to `maxZoomForAnimation`, at 1× and 2×, at most 512 per frame.
- **External catalog:** `src/main/resources/raster/external-catalog.json` seeds `kind=external` products of the DERIVED source `external-catalogue` (GIBS WMTS and nowCOAST WMS, with templates, tiling, zoom, bounds, time dimension and attribution); tile and image routes refuse them; nothing fetches them.

### 27.10 Decoders, models and sampling (046, 051–053, 105, 106)

- **Decoders** (`raster.decode`): `Grib2Decoder` selects fields by discipline, category, number and level, never by message index, and reads templates 3.0 and 3.30 (Lambert); `NetcdfDecoder` builds the `geos` grid from `goes_imager_projection`; `SafeNetcdf` is the only call site of `NetcdfFiles.open`; `DecoderSelfTest` decodes bundled 5.3, 5.41 and 5.42 messages and a NetCDF4 file at startup and disables products whose path failed (`503 PRODUCT_UNAVAILABLE`, reason in health).
- **Products:** `mrms` (reflectivity, precipitation rate with its flag band, MESH; second wave rotation, QPE 1 h/24 h, FFG ratio; extras lightning probability, VIL, echo top; S3 `start-after` listing every 2 minutes); `goes-east-abi` (GOES-19 C13, C08/C09, C02, mesoscale sectors) and `goes-west-abi`; `nexrad-l3` (NIDS); `rtma-ru`, `nbm`, `gfs-wave`, `sst`.
- **Forecast models and wind** (`raster.model`): `gfs` and `hrrr` read only the messages they need from each step's `.idx` (`GribIdx`, `FieldSelector`), fetch coalesced byte ranges charged to `mbPerDay` in advance (`IdxRangePlanner`), rotate HRRR grid-relative winds (`WindRotation`), write frames per step and one `raster-frame` version per completed run (`ModelRunIngest`), and write God's Eye View wind fields (`FieldStore`). `ecmwf-ifs` and `aifs` use `EcmwfRunIngest` over the JSON `.index`. Served as `/fields/{frameKey}.json|.f32|.i16|png-uv` (`FieldService`), barb vector tiles (`VectorTileService`), the `wind-speed@1` style, and `/v1/products/timeline` (`TimelineService`, one run per forecast product, `mode=seamless`).
- **Sampling** (`raster.sample`): `SampleService` resolves `latest|now|<iso>|-PT1H|+PT6H` to a frame and samples by the product's method, with the status chain `unavailable` → `no-coverage` → `no-data` → `stale` → `ok` (non-ok values carry `statusText` and no value; "outside product coverage; this is not evidence of clear weather"). `AreaStatsService` uses spherical cell areas; `PlaceGridIndex` precomputes each place's cells within 100 km; `ForecastSeriesService` caches point series per run. Served as `/v1/sample` (GET, POST ≤ 100 points), `/v1/products/{id}/series`, `/v1/products/{id}/stats`, `/v1/places/{id}/weather`. Never fetches.
- **Lightning and fire** (048): `goes-glm` writes flashes only to the 60-minute places-AOI `FlashRing` and the 14-day `FlashArchive`, plus `lightning-cluster` records; `goes-fdc` writes `fire-detection` and `fire-cluster`; `hms-smoke`, `hms-fire`.

### 27.11 SatDump integration (049, 054, 107)

- **Station requirements:** `/api` exists only in the headless legacy CLI (`satdump legacy autotrack|live … --http_server`); a GUI-run station works in products-only mode. `finish_processing: true` is required, with no server mode. **Never fetch `/fft.jpeg`** (requesting it turns the FFT on). Pin a SatDump version and keep a fixture per output type, including an `/api` fixture per pinned version.
- **One source per station** (`satdump-{station}`): parameters `apiUrl` (`LAN_URL`), `inboxPath` (`INBOX_PATH`; at least one of the two), `location`, `satNames`, and the DCS and EMWIN options (107). Two loops share `adapter_state`: the `/api` loop (5 s, 2–30 s) and the inbox scan (30 s; `WatchService` is only a hint). Kinds: `ground-station`, `satellite-pass`, `raster-frame`, `dcp-observation`, `emwin-product`, `emwin-warning`.
- **`/api` loop:** lenient parsing; a `ground-station` record versioned only on a state change or a quantized change; RF sessions sampled about every 2 s into an in-memory ring flushed every 30 s and at LOS, then aggregated (`snrMaxDb`, `snrMeanDb`, BER, synced fraction, lock times and elevations, an SNR-against-elevation series). The latest polar plot is held **in memory only** and served at `GET /v1/ground-stations/{sourceId}/polarplot.jpeg` (404 without an active pass; never fetched on client demand).
- **Inbox loop:** a polar pass folder is ready when `dataset.json` exists and every file size was stable for 2 scans; an xRIT timestamp folder produces a `partial` frame as soon as one channel validates and becomes `complete` after 120 s of quiet or when the next folder appears. PNGs go through ImageGuard. Health reasons name the likely cause ("no dataset.json after 20 min: is `finish_processing: true` set, and server mode off?").
- **Pass lifecycle:** satellite keys are normalized (`Meteor-M2-3`, NORAD 57166 → `meteor-m2-3`; overridable with `satNames`); `externalId = {station}:{satKey}:{aosEpochMinute}`, minted from the RF session or `dataset.json`; a product joins the RF session whose window [firstLock − 5 min, LOS + 5 min] contains it. Versions `receiving` → `recorded` → `decoded` or `failed`; `predicted` passes come from 064. Geometry: actual footprint, else approximate swath, else the station point.
- **Georeferencing:** geostationary xRIT maps `projection_cfg {type: geos, …}` to a `geos` grid rendered like GOES ABI (FY-4 and MSG HRV rejected with a reason); polar overlays read any EPSG:4326/3857 GeoTIFF matching `polarGlob` as an RGBA `affine` frame with an alpha-mask footprint split at the antimeridian; the fallback is an Orekit SGP4 approximate swath from the TLE; other CRSs go to the optional `gdalwarp` or are rejected.
- **Thumbnails and provenance:** 256 px and 1024 px thumbnails per composite in the media store; originals and provenance files under `rasters/satdump/{station}/{passId}`; `.cadu` and basebands are never copied.
- **Extras (107):** DCS messages filtered by a mandatory effective bbox, address allowlist and agency, with a per-scan cap; EMWIN products and warnings (the warnings are a fallback while `nws-alerts` is unavailable); `glm-local`; processed incrementally once files settle.
- **Deployment:** the `satdump` compose profile on the same host, or a remote station syncing into `${FORESIGNAL_INBOX_DIR}` (recommended) or pushing to `POST /v1/ingest/{sourceId}/media` and `/rasters` (ingest key, 25 MB per file, ImageGuard). SatDump's webhook plugin is not an ingest path (plain text only).

### 27.12 API conventions for products

- **Time in JSON:** every frame carries `time`, `timeKey` (`20261010T143240Z`), `frameKey` (`{timeKey}~{sha8}`), `expiresAt`, `status`, and `run` and `step` for forecasts; responses carry `serverTime` and `earliest`. **The server hands out fully expanded URLs and templates; clients never build time strings.** An ISO-extended time in a path is canonicalized with a 301.
- **Aliases:** `latest` (newest complete observed frame; for forecasts the newest complete run's step nearest now), `now`, `run=latest|previous|<runIso>`; partial frames only with `includePartial=true`; a concrete forecast time without `run` is `400 RUN_REQUIRED`.
- **Catalog, frames, timeline:** `GET /v1/products` (with conditional GET and `compact=true`), `/v1/products/{id}`, `/frames` (cursor pages with expanded templates and a prefetch hint), `/frames/latest`, `/v1/products/timeline`.
- **Tiles and images:** `/v1/products/{id}/tiles/{frameKey}/{z}/{x}/{y}[@2x].png`, the `{tileMatrixSetId}` form with `tileSize=512` and WebP (108), `/tilejson` (TileJSON 3.0.0 with a concrete content-addressed template), `/v1/products/loop` (and `loop.gif`), `/image/{frameKey}.{png|jpg}`, `/v1/styles`, `/legend` (JSON and PNG).
- **Fields and wind:** `/v1/products/{id}/fields/…` and `/vectors/{frameKey}/{z}/{x}/{y}.mvt`.
- **Sampling, places, media, maps:** §27.10; `/v1/media/{id}` and `/thumbnail` (immutable); `/v1/maps/static` (055); `/v1/places/**`.
- **SSE:** `/v1/changes/stream?kind=raster-frame&externalId=<product>`; observation events carry `{productId, frameKey, time, timeKey, tiles, bounds, stats, expiresAt}`, forecast events one per completed run with `{run, steps[], validTimes[], tilesTemplate}`. Frame removals are not streamed; clients drop frames past `expiresAt` and resume with `GET /v1/changes?after=<seq>&kind=raster-frame`.
- **Map-client access:** tile keys (§15.1), the CORS header set, and the `web` compose profile (Caddy with HTTP/2 and HTTP/3) for map clients; on plain HTTP/1.1, use `/loop` and one shared SSE connection per web client. Client recipes for MapLibre GL JS, maplibre_gl, flutter_map and Cesium are in the [Frontend Guide](Frontend-Guide.md) (`flutter_map_tile_caching` is avoided: GPL-3.0).
- **MCP:** `list_products`, `sample_weather`, `weather_forecast`, `area_weather_stats`, place tools, `list_satellite_passes`, `get_satellite_pass`, `get_map_image` (≤ 512 px, ≤ 200 KB, with a text caption); compact output ≤ 4 KB by default; every value returns `validTime`, `localTime`, `ageMinutes`, `run`, `coverage` and `status`.
- **AccessPolicy:** read for GETs on products, styles, sample, media, maps, places and ground stations and POST on sample and stats; agent for place writes and tile keys.
- **Problem codes:** `UNKNOWN_PRODUCT`, `UNKNOWN_FRAME`, `UNKNOWN_PLACE` (404), `FRAME_EXPIRED` (410), `TILE_OUT_OF_RANGE`, `UNKNOWN_STYLE`, `RUN_REQUIRED` (400), `PRODUCT_UNAVAILABLE` (503), `RENDER_BUSY` (503 + `Retry-After`), `MEDIA_TOO_LARGE` (413), `INVALID_TILE_KEY` (401), `FORMAT_UNAVAILABLE` (406).
- **`/v1/system` `rasters` block:** profile, products, frames, store bytes, quota percentage, cache hit and eviction rates, render queue, `renderBusyLastHour`, MB downloaded today, media bytes.

### 27.13 Alerting and intelligence integration (052, 056)

- **Derived-record principles:** emit only on change of the quantized payload (`observedAt` stays at the frame where the change was seen); no `validUntil`, so no heartbeat versions; generators tombstone explicitly and reconcile from `record_head` at startup; fields that change every frame are left out; each section (`radar`, `hail`, `precip`, `lightning`, `cell`, `smoke`, plus later `forecast`, `marine`) has `status: ok|stale|no-coverage|unavailable`, and when input is older than `maxGap`, comes from a failing source or lacks coverage the section **keeps its previous values**, sets the status and adds `asOf`; "none within 100 km" is `999`; every payload carries `automated: true, notAWarning: true` (NWS alerts stay authoritative). Rule recipes use `enter`/`exit` or a cooldown, never bare `match`.
- **Derived kinds:** `place-weather` (`place:{id}`, `wx-places`, on frame events and a 60-s staleness check, with the model-disagreement flag from 105), `wx-area` (`{field}:{threshold}:{aoi}`, `wx-areas`), `radar-cell` (`wx-cells`: 40 dBZ mask, opening, union-find split at 50 dBZ cores, centroid-v1 motion), `lightning-cluster` (DBSCAN over the GLM ring), `forecast-area` (`wx-forecast-areas`, 105), `storm-track` and `storm-swath` (`wx-storm-tracks`, the pure `titan-lite-v1` tracker with Hungarian assignment and a GLM lightning-jump flag, 105). GLM's footprint over Texas is about 8–10 km and its detection efficiency below 100%, so GLM-only lightning rules should use 15 km or more and all-clears are advisory.
- **Trigger additions:** `cooldownScope: record|rule` and `near: {placeId|point, radiusKm}`. A tombstone counts as an exit when P(previous) held, so deleting a place can fire exit rules; generators never tombstone because of staleness.
- **Rule recipes:** ≥ 50 dBZ within 20 km (`radar.nearest50Km10m <= 20`, `enter`); "echoes cleared" (`radar.nearest50Km10m <= 20 OR radar.status != ok`, `exit`: fires only when the previous version was near or unknown and the current one is ok and far); lightning within 15 km with an advisory all-clear; storm core within 30 min; ProbSevere tornado probability entering a county (`cooldownScope=rule`); fire near home; pass decoded. A weather feed going down is not a rule: the degraded source raises the system event.
- **Weather intelligence** (`ai.foresignal.weather`, 056): `WeatherContextService` computes an alert's `context.weather` (radar maximum within 10 km and the nearest `radar-cell`, GLM flashes within 20 km over 15 minutes, rain rate and 1-h QPE, intersecting NWS alerts, HRRR-else-GFS wind and gust, HRRR smoke, a deterministic `summary`) from stored frames, the GLM ring and current records, each section within a 500 ms budget with the status rules; templates expose `{{context.weather.*}}`; `WeatherEnrichmentContributor` feeds enrichment, `WeatherAtPlacesSection` the briefings and `/v1/digest?places=`, and Ask cites `wx:{productId}@{frameKey}` / `wx:{productId}@{run}/{step}` with a caveat when an answer ignores a non-ok status. `GET/PUT /v1/settings/weather`.
- **Weather-impact correlation (105):** recipes `dry-lightning-ignition`, `airport-weather` and `smoke-aq` (§32), and the `EventAnnotator` SPI that adds `context.weather` to existing events as context, never as supporting evidence.

### 27.14 Notification image attachments (055)

One static PNG per alert at dispatch (800 px, `MapRenderer` with Natural Earth, alert geometry, range rings, timestamps, legend, attribution and an "Automated — not an official warning" footer), waiting up to 5 s for a frame newer than the alert, stored as media for 7 d and reused by every channel; `satellite-pass` alerts attach the composite thumbnail. Over a channel's cap the image degrades to 600 px, then the thumbnail, then text. Discord, Telegram, ntfy, Pushover and email attach natively; other channels get text and a link. No public basemaps: an operator basemap URL is allowed only for a LAN tile server (`FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`).

### 27.15 Resource budget (design defaults)

| Product | Download per day | Decode CPU per day | Rolling disk |
|---|---|---|---|
| MRMS reflectivity, precipitation + flag, MESH, every frame | 1.7–3.4 GB | ~10 min | ~2 GB |
| MRMS second wave | ~0.5 GB | ~3 min | ~0.5 GB |
| GOES-19 CMIPC C13 | 1.0–1.1 GB | ~2 min | ~1.2 GB |
| GLM (AOI ring; files are CONUS-wide) | ~1.5 GB | ~2 min | 0.4 GB (14 d) |
| FDC / ProbSevere | 0.15 / 0.19 GB | < 1 min | records only |
| GFS | ~0.5 GB | ~1 min | ~0.5 GB |
| HRRR | 0.4–0.8 GB | 1–3 min | ~0.4 GB |
| Records (NWS, SPC, AWC, HMS, gridpoints) | ~0.1 GB | negligible | records only |
| SatDump (LAN) | 0 | ~2 min | 3–6 GB polar (30 d) + ~1 GB geo (3 d) |
| **Total (v1 products)** | **≈ 5–8.5 GB/day** | **≈ 0.05–0.1 core average** | **≈ 10–12 GB** (quota 200 GB) |

PostgreSQL grows by roughly 0–100 MB/day of weather records at 3–14 d retention by kind. The 106 extended products add to these figures within their own `mbPerDay` budgets. Prompt 143 re-measures them on the bench server (`docs/Runtime-Verification.md`).

### 27.16 Map interop and offline (108)

`TileMatrixSet` names `WebMercatorQuad` (EPSG:3857, default) and `WorldCrs84Quad` (EPSG:4326, 2×1 at z0); the tile path gains a `{tileMatrixSetId}` segment and a native 512 px pyramid. `WebpSupport` health-checks the WebP writer at startup (a failure disables WebP with a `/v1/system` warning and `406 FORMAT_UNAVAILABLE`). `DataTileEncoder` serves band-0 values as RGB (`custom`/`terrarium`) for MapLibre `raster-dem`; `I16FieldEncoder` and tiled `png-uv` fields; `GifEncoder` for loops. `WmtsController` (capabilities with `Time`, KVP and RESTful tiles), `OgcController` (OGC API Tiles and EDR CoverageJSON; Features in §36) and `StyleFragmentController` open the products to GIS, TAK and 3D clients. Raster exports run async on `raster_export_job`: `MbtilesWriter` (sqlite-jdbc), `PmtilesWriter` (in-house v3) and `CogWriter` (tiled deflate GeoTIFF with overviews), plus place offline packs (§35); files live under `exports/` for 48 h. Tiles never trigger an upstream fetch.

## 28. Map styles, feeds and KML (058)

Off-the-shelf clients use Foresignal with no custom code. Every output is overlay-only (no basemap, no glyphs), honors `openReads`, feed and tile keys, the private filter and `EgressPolicy` on the `FEED` path, and escapes all upstream text.

- **Sprite** (`map.SpriteCatalog`): `scripts/build-sprite.sh` builds an SDF sprite and white 32 px PNG icons from pinned Maki and Temaki releases (CC0) under `src/main/resources/map/`, content-addressed by `sha8` at `/v1/map/sprites/foresignal-{sha8}[@2x].{json,png}`. `DescriptorValidator` fails startup for a `kindDisplay.icon` outside the sprite or a style field that is not a `tileProperties` entry.
- **MapLibre style** (`MapStyleService`, `GET /v1/map/style.json`): one vector source per tile layer, an `agg=hex` source below `aggBelowZoom`, per kind a `symbol` or `circle`, a `line` and a `fill`; colors from the descriptor's `kindDisplay` (field `colour`) or a `match` on `colorField` / `severity`. Strong ETag, `max-age=300`.
- **Feeds** (`ai.foresignal.feed`): Atom (StAX, GeoRSS-Simple) and JSON Feed 1.1 for alerts and briefing runs; markdown through commonmark and an OWASP allowlist; conditional GET. No RSS 2.0 (except the briefing podcast feed of §42) and no WebSub output.
- **KML** (`ai.foresignal.kml`): a NetworkLink document, Placemarks (≤ 5,000), `gx:Track` per entity (≤ 100 entities, ≤ 5,000 points each), trigger geofences; `KmlWriter` escapes every string and never uses CDATA with upstream text; KMZ bundles the icons.
- **Keys and URLs:** generated URLs use `publicBaseUrl`; a request through `/v1/k/{key}/…` gets follow-up URLs on the same key, always `Cache-Control: private`.

## 29. Entity registry (098)

A canonical identity layer for physical objects (`ai.foresignal.entity`). Records link to one entity per real thing through **deterministic keys only**: no fuzzy matching and no automatic merge of two anchored entities. Entities cover vehicles and objects, never people or organizations.

- **Tables:** `entity`, `entity_key` (partial unique `(key_type, key_value) WHERE valid_to IS NULL`), `entity_alias` (prefix index on `lower(alias)`), `entity_link` (partial unique `(source_id, external_id) WHERE valid_to IS NULL`).
- **`EntityKeys`** (pure): normalization and validation for `airframe`, `icao24`, `registration`, `imo` (check digit), `mmsi` (ship class only), `norad`, `cospar`, `callsign` (domain-prefixed), with roles and per-type confidence; rejected values counted (`foresignal.entity.invalid_keys{type}`).
- **`EntityResolver`:** anchored creation, provisional merge and weak-key reassignment; a weak-key conflict between two entities links by precedence and changes nothing else (`foresignal.entity.conflicts`); a key asserted by a stronger origin (operator > registry > observation) is never superseded by a weaker one; callsign-only creates nothing. `EntityKeysChangedEvent` on every key change.
- **`EntityLinker`:** a ChangeBus consumer (FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE) with cold-start backfill and catch-up on its own virtual thread; copies declared attributes and display names; closes stale flight-session links every 10 minutes. Entity links are retained 365 d by default.
- The `faa-registry` adapter supplies registry keys; descriptors declare `entityKeys`. Windowed rules, pattern of life, chronicles and the object contract resolve identity through the entity link.

## 30. Derived layers (099)

Eighteen DERIVED sources on the 052 runtime (`source.adapters.derived`) turn stored and live records into ordinary records, so triggers, SSE, tiles, search, briefings and MCP use them unchanged: `derived-gps-jamming`, `derived-ais-gap`, `derived-loiter`, `derived-rendezvous`, `derived-deception`, `derived-holding`, `derived-emergency-squawk`, `derived-chokepoint`, `derived-mil-naval`, `derived-aircraft` (the fused layer: TRACK with a 5-minute TTL), `derived-corroboration`, `derived-port-calls`, `derived-cpa`, `derived-felt-locally`, `derived-cable-risk`, `derived-dark-vessel-match`, `derived-lightning-holdover`, `derived-hf-blackout`. All others are STANDARD, ALL, `recordTtl=null`, disabled by default, parameterized and health-tracked, and declare their input families.

- **Runtime extensions** (`ai.foresignal.derive`): `Observation`, `ObservationFeed` (the merged current view, subscribed to both buses for gap detection; truncated reads counted), `CoverageService`, `DensityIndex` (H3 res-5), `LayerStatus` (`ok|insufficient-data|no-coverage|degraded-input`), `StatefulGenerator`, per-generator metrics.
- **Coverage-aware:** absence of evidence is never reported as evidence of absence; a window touched by a change-bus gap or a live drop is `degraded-input` and suppressed from absence-based events. From 116, `derived-corroboration` is one record per canonical event and counts independent evidence groups (§40).
- `DescriptorValidator` allows a DERIVED source a `recordTtl` only when its tier is TRACK.

## 31. Windowed rules and anomalies (100)

`trigger_rule.type` (`record`, `count`, `absence`, `anomaly`) and `window_spec` (`ai.foresignal.trigger.window`). Only `record` rules compile into the per-version engine and persist gate.

- **Membership** (`WindowMembership`): distinct entities seen in `(from, to]` through a live row or a non-tombstone, non-heartbeat version with `seq ≤ watermark`, with the knowledge bound (`ingested_at`, `last_received_at` ≤ decision time) and the entity link valid at `to`. More than 200,000 candidates is `unknown`, never a partial count.
- **Evaluation** (`WindowEngine`, `WindowScheduler`): count and absence evaluate the current window on each step; anomaly fills completed buckets from a durable cursor (`anomaly.cursor.{ruleId}`) with catch-up and a 12-week backfill. Alerts are owed on clear → active and reference the state's `rule-windows` DERIVED record (kinds `window-state`, `anomaly`); cooldown and `maxAlertsPerHour` apply per (rule, group); `unknown` and `learning` never fire or resolve; an unevaluable absence rule raises one `system.window_rule_unknown` per transition.
- **Coverage gate** (`CoverageGate`): `ok` only when every involved source is enabled, healthy enough, in fresh contact, above `coverageMin`, and shows no receivers drop; past buckets are judged from `fetch_log` history. From 116, quiet absence windows and downward anomalies also need detectability (`LOW_DETECTABILITY`).
- **Baselines:** `activity_sample` (pruned after 26 weeks) and `anomaly_observation`; Poisson and negative-binomial tails over coverage-ok samples of the last 12 weeks, winsorized at p99; classifier `activity-up|activity-down|coverage-drop|insufficient-coverage|learning|normal`; every result records detector `{name, version, params}`, baseline summary, watermark, member references and the `fetch_log` id range, plus a manifest.
- **API and MCP:** window state, baseline, rebuild (202), anomalies list; `testTrigger` for windowed types (`approximate: true`); `WINDOW_RULE_INVALID`, `BASELINE_NOT_READY`; MCP `list_anomalies`, `get_window_state`, `watch_for_unusual_activity`.

## 32. Intelligence events, investigations and exposure (101–104)

### 32.1 Intelligence events and investigations (101)

Package `ai.foresignal.intel`: `intelligence_event` (optimistic `version`, `merged_into`, `private`), `intelligence_event_member` (pinned `(source, externalId, seq, version)`), `intelligence_event_log`, `correlation_recipe`, `investigation` (display ids `INV-{yyyy}-{nnnn}`), `investigation_item` (snapshots ≤ 64 KB taken when added, so evidence survives retention), `population_h3`.

- **Correlation engine:** a ChangeBus subscriber with the durable `intel.cursor` (10k-seq chunks, time look-back across chunks); bundled versioned recipes in `src/main/resources/intel/recipes/*.json` (`maritime-disruption`, `aviation-anomaly`, `infrastructure-disruption`, `dry-lightning-ignition`, `airport-weather`, `smoke-aq`), loaded disabled with a warning when a kind is missing.
- **Scoring:** `event-scorer` (1.1.0 from 116: members collapsed by evidence group; `strong` needs ≥ 3 families from ≥ 2 independent roots). Client-supplied confidence is rejected.
- **Manual and agent events** from verified members, patch with optimistic locking (`409 EVENT_VERSION_CONFLICT`), merge.
- **Exposure** (`ExposureService`): facility counts, places inside, live aircraft and vessels, optional population from an imported GHSL GeoTIFF pack under `${FORESIGNAL_DATA_DIR}/packs`.
- Every **public** event is a record of the DERIVED source `foresignal-intel` (kind `intelligence-event`), tombstoned on dismissal, merge or turning private; SSE topic `intel`; seven MCP tools.

### 32.2 Autonomous analyst (102)

`ai.foresignal.analyst`: profiles with schedules, scope, budgets and action permissions; `AnalystScheduler` (30 s poll, `FOR UPDATE SKIP LOCKED`, a global `Semaphore(2)`); `AnalystRunner` detects new in-scope inputs without an LLM, builds a ranked packet, runs the 028 `ToolLoop` over the read catalog plus output tools under the provider's egress rules, validates every claim and proposal (`AnalystOutputValidator`, including the 122 estimative-language lint), persists notes and proposals (auto-executing only when allowed), and logs the run with a manifest. SSE topic `analyst`.

### 32.3 Backtesting and precursor tests (103)

`ai.foresignal.backtest`: one job framework (`backtest_job`, `BacktestJobService`, `/v1/jobs/{jobId}`) for rule and recipe backtests, precursor tests, reference imports and manifest replays; ranges translated through `seq_clock`, clamped to retention and the watermark, ≤ 90 days; deterministic `CaveatGenerator` (always `NOT_GROUND_TRUTH`), `CooldownSimulator`, Wilson intervals and `TuningSuggester`. Record rules replay `RuleSqlBuilder` per 50k-seq chunk through the live engine's `AlertPass` in simulate mode.

### 32.4 Infrastructure dependency mapping (104)

`ai.foresignal.infra`: `infra_node`, `infra_edge` (relation, method, label, score, rule, evidence, most restrictive license of its nodes) and `infra_build`, built by `InfraGraphBuilder` from `infra/node-mapping.json` and the versioned `infra/dependency-rules.json` (operator overrides of `enabled` and `params` only; proximity capped at `possible`) in one transaction per build; readers see only the newest successful build; the same inputs give the same graph. The `osm-infrastructure` source supplies nodes.

## 33. License, provenance and signing (109)

`ai.foresignal.provenance` makes license, provenance and reliability first-class.

- **Structured terms:** `LicenceTerms` (`CommercialUse`, `Redistribution`, egress-safe `UNKNOWN` defaults) and `LicenceCatalog` (`src/main/resources/provenance/licences.json`: built-ins, provider classes and aliases for every free-text license an adapter declares). Descriptors carry `licenceTerms`, `reliability` and `derivedFrom`; `SourceLicences` gives a DERIVED source the most restrictive terms of its inputs (fail closed to `unknown`).
- **Per-record resolution:** `LicenceResolver` resolves a page in one query over `record_version ⋈ fetch_log`; the batch license wins, a gone fetch row falls back to the source's terms, a peer record's `_foresignal.originLicence` overrides, and an MLAT position is downgraded to `LOCAL_ONLY`.
- **Client policy:** `LicencePolicy` (`no-nc`, `no-sa`, `redistributable`) from the `Foresignal-Licence-Policy` header or the `licencePolicy` parameter, with the token's `api_token.licence_policy` as a floor. Records and single reads filter per record and carry `licenceFiltered` and `attributions`, a `Foresignal-Attribution` header and `Link rel="license"`; tiles, SSE and `/v1/changes` prune whole sources and send `Foresignal-Licence-Filtered`. `GET /v1/attributions` aggregates a scope.
- **Egress:** `EgressPolicy.decide` (§15.4) consults `LicenceEgressGuard`, its internal license table, which counts `foresignal.licence.egress.denied{path,licence}`. License egress flags are defaults the operator can override per source.
- **Reliability and credibility:** descriptors without a grade are seeded (official agencies `B`, own receivers and built-in providers `C`, community feeds `D`, custom, peer, DERIVED and license-less `F`); operator overrides in `source_settings.reliability`; `CredibilityService` derives credibility from corroboration and deception records; `grade = reliability + credibility`.
- **Signing:** `signing_key` (Ed25519, private key encrypted; rotation serialized by an advisory lock and audited), JWKS at `/.well-known/jwks.json` and `/v1/system/jwks`, rotation at `POST /v1/system/signing-key/rotate`; `JwsSigner` (detached, `b64:false`) and `JwsVerifier`; `ManifestSigner` for exports. Briefing runs, federation info, exchange packages and export manifests are signed.

## 34. Outbound sinks (110)

An outbound-sink SPI with durable cursors (`ai.foresignal.sink`): `sink` (secrets encrypted, write-only) and `sink_cursor` (last and watermark seq, state, delivered, dropped, denied by reason and source). `SinkSupervisor` runs one virtual thread per enabled sink; `SinkCursorConsumer` joins the retention consumer clamp; `/v1/system` has a `sinks` block.

- **Egress gate:** every record passes `EgressPolicy.decide` before a type sees it (`SinkRecordGate`: `SINK_SERVE_LAN` for SERVE, `SINK_PUSH` for PUSH, `FEDERATION` for the federation push sink); denials counted `foresignal.sink.denied{sink,reason}`.
- **Types:** `mqtt-state`, `cot` (CoT XML over UDP multicast or SERVE TCP), `readsb-json` (`aircraft.json`/`receiver.json`), `sbs` (BaseStation), `nmea-aivdm` (the 085 encoder), `aprs-is-serve`, `cloudevents` (CloudEvents 1.0 with HMAC), and `federation-push` (§37).
- **SERVE safety:** `ServeGuard` binds only loopback, RFC 1918, link-local or ULA (100.64/10 with `allowTailnet`, `0.0.0.0` only with `allowAllInterfaces`); `SinkTcpServer` checks peers and caps clients.
- **API:** `/v1/sinks/**` (admin): types, CRUD, test, status, resync.

## 35. Exports, snapshots, holds, archive and offline packs (111)

Durable, portable copies of Foresignal's data (`ai.foresignal.export`).

- **Export jobs:** `export_job` with `spec` and `plan`; one bounded worker pool (`exports.maxConcurrent`, 1) shared by exports, snapshots and packs, submitted after commit; files written to `exports/tmp`, fsynced and renamed; limits `exports.maxRows` (5,000,000), `exports.diskQuotaGb` (20, `409 EXPORT_QUOTA`), `exports.ttl` (7 d).
- **Query** (`ExportQueryService`): keyset pages of 10,000 in their own read transactions; current state from head ⋈ live; history per kind and source; asOf and `atSeq` with the §9 `DISTINCT ON` per tier, unioned with covering holds' copies; the caller's restriction in every mode.
- **Formats:** GeoParquet 1.1 (Hadoop-free, ZSTD), GeoPackage 1.4, FlatGeobuf (packed Hilbert R-tree), GPX, plus CSV and GeoJSON.
- **License rules:** every source goes through `EgressPolicy.decide(EXPORT, …)`; private sources need admin and `includePrivate`; each export carries `licence.json`, `ATTRIBUTION.txt` and a signed `manifest.json` (`manifest.json.jws`).
- **Snapshots and holds:** `SnapshotService` with `PointInTimePlanner` (asOf validity, held reads before a source's horizon, else `400 SNAPSHOT_BEYOND_RETENTION`); `hold` (≤ 20) copies the versions retention is about to remove into `record_version_held` (`HoldCopyStep`).
- **Cold archive:** `ArchiveStep` writes hive-partitioned GeoParquet per UTC date, verifies row counts before the delete, keeps `archive_watermark` so nothing is archived twice, maintains `_manifest.json`, and uploads through `S3Uploader` (SigV4 on `java.net.http`, multipart above 64 MiB, verified before a local delete; endpoint under `LAN_ALLOWED`).
- **Offline packs:** `PackService` builds PMTiles of MVT and raster tiles for a place (`413 PACK_TOO_LARGE` with `tileCount`).
- `/v1/system` blocks `exports`, `archive`, `holds`; MCP `create_export` and `get_export` return URLs only.

## 36. Standards and headerless outputs (112)

- **OGC API – Features** (`ai.foresignal.ogc`, `/v1/ogc`): one collection per kind for current state and a `{kind}-history` collection of versions; GeoJSON items with links, keyset cursors, `bbox`, `datetime`, `source`, `licencePolicy`; private sources, restrictions, feed keys and the license policy apply. **CQL2** text and JSON parse to one normalized tree (`Cql2TextParser`, `Cql2JsonParser`) and translate to parameterized SQL reusing the 017 operators (`400 FILTER_INVALID` with a position).
- **CAP 1.2** (`ai.foresignal.cap`): `CapService` stores one message per alert state change in `cap_message` (`Alert`, `Update`, `AllClear`; never `Cancel`; `references` list every prior message; status `Exercise` for exercise alerts); served as an Atom index and per alert.
- **iCalendar** (`ai.foresignal.ical`): a hand-written RFC 5545 writer and the `launches`, `tfr`, `nws`, `briefings` and `custom` feeds (tombstoned or expired records stay 7 days as `CANCELLED`).
- **Share links** (`ai.foresignal.share`): `share_link` stores the hash of `fss_` + 32 random bytes, the target, mask, precision, expiry (7 d default, 30 d max), `max_uses` and the creator's restriction snapshot; `ShareKeyResolver` plugs into `PathKeyFilter`; creation passes `EgressPolicy` on `PUBLIC_SHARE` (`409 SHARE_LICENCE_FORBIDDEN`); use is rate-limited and counted atomically (`410 SHARE_EXPIRED`); the key is never a bearer token.

## 37. Federation and edge relay (113)

Instances share data through the `foresignal-peer` source family (`ai.foresignal.federation`).

- **Refs:** `RecordRef` is the one class that formats and parses `urn:foresignal:{instanceId}:{sourceId}:{externalId}` (instance id = install id; external id percent-encoded); served as `globalRef` on records, versions and changes. A federated record's local external id is `{originSourceId}/{originExternalId}`, or `sha256:` + hex above 256 characters.
- **Serving:** `/v1/federation/changes`, `/stream` (≤ 16 streams, `429 STREAM_LIMIT`) and `/snapshot`, for tokens with `restrictions.federation=true`; `FederationFeedService` decides every row with `EgressPolicy.decide(FEDERATION, …)` and its resolved license before serialization (private and `LOCAL_ONLY` sources are also pruned in SQL; exercise records only with `includeExercise`); below the purge horizon `410 RESYNC_REQUIRED` or `reset`. `GET /v1/federation/info` is signed (`Foresignal-Signature`).
- **Consuming:** `peer` rows register `peer-{id}` (STANDARD), `peer-{id}-trk` (TRACK, SAMPLED) and `peer-{id}-events`; PULL peers are STREAMING (`PeerSession`: verify the JWS against the peer's JWKS with the key pinned on first use, snapshot, catch-up, SSE), RECEIVE peers are PUSH (`POST /v1/federation/ingest/{peerId}` with a peer key `fsp_`, ≤ 1,000 items, JWS against the pinned key).
- **Rules** (`FederationIngestor`, identical for PULL and RECEIVE): a chain naming this instance is a `loop`; a chain of `maxHops` (3) or more is dropped; private, selection and license checks (FORBIDDEN never; `LOCAL_ONLY` only from a `sameOperator` peer); `OriginLedger` upserts `federation_origin` only when the origin seq is greater (equal is a duplicate, lower is `dedup`); the first claimant owns an origin until its peer is disabled. Records keep `_foresignal {originRef, originSeq, originLicence, chain}` (volatile, so identical re-sends make no version).
- **Edge relay:** `foresignal.mode=edge` drops the heavy beans by `@ConditionalOnProperty` and pushes with the `federation-push` sink on the `FEDERATION` path with the `sameOperator` exception.
- **Compact messages** (`federation.compact`): `CompactMessages` builds 084 CBOR messages (≤ 160 bytes; keys 10–12 for origin, message id and expiry) from alerts and intelligence events only; `CompactOutbox` orders, expires, deduplicates and budgets them for mesh channels and store-and-forward bundles (`/v1/federation/bundle`).

## 38. Pattern of life, trips and kinematics (114)

- **Kinematics** (`ai.foresignal.kinematics`): `KinematicState` from head ⋈ live (speed and course from the payload, else derived from the last two positions ≥ 10 s apart; `stale` beyond 2 × TTL; `422 STATE_TOO_OLD` beyond 1 h); one `CpaCalculator` (also used by `derived-cpa`); `getCpa`, `getEta`, `cpaBatch`. The **approach condition** `{type: "approach", target: {placeId|point|record}, withinMeters, withinMinutes}` is never rendered in SQL: the engine selects candidate versions with `RuleSqlBuilder.approachCandidates` and judges both sides in Java (`ApproachEvaluator`) with the SQL edge rules.
- **Pattern of life** (`ai.foresignal.pol`): opt-in subjects (`pol_subject`: a record, entity or watchlist), incremental stay-point extraction into `pol_stay`, places in `pol_place` from an in-house deterministic DBSCAN recomputed nightly, deviations `NEW_PLACE`, `UNUSUAL_TIME` and `EXPECTED_ABSENT` (health-gated), published by the DERIVED sources `pattern-of-life` (`pol-place`, `pol-deviation`) and `pattern-of-life-private`. Retention `pol.retention` (180 d); private subjects are admin-only; purges cascade.
- **Trips** (`ai.foresignal.trip`): a client-supplied route (Foresignal never routes; ≤ 5,000 vertices) buffered into `trip.corridor` (GiST); `TripHazards` finds hazards on, near and timed along the route; `TripWatch` (1-minute tick) delivers a pre-trip briefing at `departAt − leadTime` and maintains an ephemeral corridor rule during the trip.

## 39. Exercise replay and alert quality (115)

- **Exercise flag:** `alert.exercise` and `exercise_run_id`, `notification.exercise`, `notification_channel.accept_exercise`. `ExerciseMarking` is the one marking helper (`[EXERCISE] ` title prefix, footer, webhook header `X-Foresignal-Exercise: true`, push urgency `normal`, CAP status `Exercise`). Exercise alerts route only to accepting or named channels and are excluded (`includeExercise=false`) from alert lists and streams, glance, chat, CAP feeds, share links, Ask, the analyst, statistics, cooldown and edge seeds, rollups, digests, escalation, MQTT state and the compact outbox.
- **Replay runs** (`exercise_run`, `ExerciseService`): rules by id or tag over a window ≤ 7 d translated through `seq_clock`, refused below any source's floor (`400 REPLAY_BEYOND_RETENTION`), one active run (`409 EXERCISE_RUNNING`); `ExerciseReplay` reuses the 103 backtest replay and `AlertPass` in simulate mode with fresh state per rule, paced in seq order; dry runs project counts only; purge removes everything a run created; exercise alerts expire after 7 d.
- **Alert quality** (`ai.foresignal.quality`): explicit feedback (`feedback:useful`, `feedback:noise` actions, chat `/feedback`, MCP `set_alert_feedback`); implicit signals in `alert_signal` (`ACK_FAST`, `SNOOZED`, `FLAP`, nightly `UNOPENED`) with weights; daily trends in `rule_quality_daily` (400 d); `TuningSuggester` 1.1.0 suggestions in `tuning_suggestion` applied only explicitly through the normal rule update, logged in `rule_tuning_log`, undoable while the rule is unchanged, and marked `STALE` when the rule changes. Nothing changes a rule automatically.

## 40. Intelligence layer (116–139)

The authoritative design is the [Intelligence Design](Intelligence-Design.md); the client object contract is the [Client Contract](Client-Contract.md); third-party providers follow the [External Data Protocol](External-Data-Protocol.md). This section summarizes how the layer sits on the platform.

### 40.1 Stack and principles

```
 L8  Analysis cycles ─ daily / weekly / monthly assessments (128)
 L7  Forecast ledger ─ questions · forecasts · resolution · scores · calibration (122)
 L6  Hypothesis sets ─ competing hypotheses · indicators → watch rules · ACH · revisions (125)
 L5  Memory ─ situations / storylines (118) · base rates · historical analogs (121)
 L4  Discovery ─ change points · event studies · co-occurrence (119) · precursors (120)
          · regional outlooks and indices (126) · impact projection (127)
 L3  Correlation ─ intelligence events · investigations · exposure · dependency graph (101, 104)
 L2  Detection ─ derived layers · count / absence / anomaly rules (099, 100)
 L1  Signals ─ records and versions · rollups (038; region rollups 119) · series (093, 117)
 L0.5 Evidence ─ canonical events · evidence groups · observability · analysis manifests (116)
 L0  Collection ─ budgeted sources · health · history with analytic retention (001–097)
 Across all levels: object contract · relation graph · annotations · workspaces (129) · benchmark harness (123)
```

- **Three memories stay separate:** observations (versioned records; never edited), interpretation (everything Foresignal concluded; versioned revisions with manifests; kept indefinitely in typed tables) and chronicles (user-directed copies, independent of global retention). Interpretation cites observations by pinned seq; nothing downstream mutates an upstream memory.
- **Outputs are platform objects:** typed tables plus records of the DERIVED sources `foresignal-intel` (`intelligence-event`, `situation`, `hypothesis-set`), `foresignal-analytics` (`analytic-finding` with `findingType` `change-point`, `pre-event-signal`, `unexplained-move`, `co-occurrence`, `precursor-active`, `forecast-exceedance`, `outlook-exceedance`, `projected-impact`, and the social and news finding types) and `foresignal-outlook` (`regional-outlook`, `regional-index`). Forecast questions and forecasts are append-only ledger rows with their own API and SSE topic.
- **Detectors find, models explain:** detectors, scorers, updaters and forecasters are pure, versioned components; outputs store `{name, version, params}`, `asOfSeq` and a manifest id. The LLM explains, challenges, proposes and forecasts (scored as a named forecaster); it never creates evidence or changes a deterministic score.
- **No look-ahead:** series points carry `knownAt`; live detection uses only known data; replays and evaluations read through asOf.
- **Coverage and detectability:** absence counts only with healthy coverage and `ObservabilityService.canSeeAbsence`; thin-reporting regions carry `LOW_REPORTING_DENSITY`.
- **Evidence independence:** confidence, corroboration, base rates and hypothesis updates count independent evidence groups, never raw records or sources.
- **Numbers are the truth:** probabilities are numbers; ICD 203 terms are derived and linted by `EstimativeLanguage`; likelihood terms and confidence levels never share a sentence.
- **No people, no accusations:** analysis covers places, events, vehicles and objects, aggregate markets and organizations' public actions.
- **Licensed data stays local:** `LOCAL_ONLY` series are computed on but served raw only to callers allowed local egress. TLP 2.0 is part of `EgressPolicy`; derived products inherit the strictest input label.
- **Durable cursors** (`evidence.cursor`, `situation.cursor`, `stats.region.cursor`, `precursor.cursor`, `impact.cursor`, `relation.cursor`, `social.cursor`, `chronicle.cursor.{id}`, `external.binding.cursor`) join the retention consumer clamp. Scheduled jobs follow the 029/102 scheduler pattern and the 103 job framework.
- **Benchmarks gate autonomy** (123): autonomous proposals and LLM ensemble weight follow `BenchmarkGate`.
- **Libraries:** Apache Commons Statistics, Numbers and RNG and Hipparchus; CUSUM, BOCPD, ETS, PAV, CRPS, RPS, Benjamini–Hochberg and others are written in-house under `analytics.math` with golden-value tests; no GPL statistics library and no Commons Math 3.

### 40.2 Subsystems

| Prompt | Package | Tables | What it adds |
|---|---|---|---|
| 116 Evidence identity and observability | `evidence`, `observability`, `manifest` | `canonical_event`, `observation_link`, `evidence_group`, `observability_estimate`, `analysis_manifest` | Originator registry (`intel/originators.json` plus `intel.originators`); `EvidenceResolver` links event-class observations to canonical events and groups them by originator; time-versioned detection probabilities (capture–recapture, presence proxy, receiver range, declared footprints) behind `pDetect`, `canSeeAbsence` and coverage tiles; analysis manifests (component, build and git commit, configuration and registry hashes, `asOfSeq`, inputs, model metadata) with deterministic replay; retrofits 099–103 |
| 117 Market and attention signals | `attention`, `markets`, `source.series` | `attention_topic`, `history_backfill_job` | Series metadata (`family`, `transform`, `releaseLagDays`, `egress`, region, topic) on `series-point`/`series-latest`; attention topics driving GDELT timelines and Wikimedia pageviews; the CFTC COT source and resumable backfill |
| 118 Situations and storylines | `situation` | `situation`, `situation_member`, `situation_snapshot`, `situation_log`, `situation_theme_cache` | Persistent storylines grouping events, findings, alerts, anomalies, records and investigations; deterministic linker; daily snapshots; optional self-refreshing LLM brief; topic `situations` |
| 119 Change points and event studies | `analytics`, `stats` | `analysis_series`, `analytic_finding`, `analytic_finding_log` | Region rollups (`activity_rollup.region_code`); one coverage-aware `SeriesReader`; analysis-series registry; signal-family registry (`intel/signal-families.json`); CUSUM and BOCPD change points; event studies (batch, live pre-event signals, unexplained moves); co-occurrence with FDR control; topic `analytics` |
| 120 Precursor mining | `precursor` | `precursor`, `precursor_candidate`, `precursor_batch` | Candidates per region and series pair, discovery with FDR, validation on a later disjoint window, a library with a lifecycle, monthly re-tests, live `precursor-active` findings ("association in stored data; not causation") |
| 121 Base rates and analogs | `baserate` | `base_rate`, `reference_history`, `reference_import`, `region_profile`, `situation_signature`, `situation_outcome` | Reference-history imports (UCDP GED, ACLED, ComCat and others) as region-week counts; base rates with reference class, period, sample size and Jeffreys intervals; historical analogs |
| 122 Forecast ledger | `forecast` | `forecast_question`, `forecast`, `forecaster`, `forecast_score`, `forecast_calibration`, `forecast_log` | Resolvable questions, immutable forecasts from many forecasters, automatic resolution, Brier/log/RPS/CRPS scoring, calibration and bias monitors, track-record ensemble with recalibration, leak-free retrospective evaluation (LLM questions after the model's cutoff only); `EstimativeLanguage`; topic `forecasts` |
| 123 Benchmark harness | `benchmark` | `benchmark_run`, `benchmark_case` | Seeded planted-truth scenarios and a historical corpus measuring false discoveries, recall, delay, coverage handling and calibration per component; `BenchmarkGate` |
| 125 Hypotheses, indicators and ACH | `hypothesis` | `hypothesis_set`, `hypothesis`, `hypothesis_revision` (append-only), `indicator`, `ach_evidence`, `key_assumption`, `collection_gap`, `hypothesis_log` | Hypothesis sets with indicators that watch through ordinary 017 rules; the pure `indicator-bayes` updater (LR classes, one evidence group counts once, one per family per day); ACH; key assumptions; collection gaps; topic `hypotheses` |
| 126 Series forecasts, outlooks and indices | `outlook` | `series_forecast`, `series_forecast_model`, `regional_outlook`, `regional_outlook_model`, `regional_index_def`, `regional_index_fit`, `regional_index_value` | Method-selected series forecasts (seasonal naive, ETS, NB GLM, damped trend) with intervals; CAST-style regional outlooks; decomposed regional indices with fitted, proposed weights; every forecast scored on the ledger |
| 127 Impact projection | `impact` | — (findings and ledger rows) | `P(impact) = P(hazard) × P(impact | hazard class)` from hazard forecasts already ingested (including the `nhc-wind-probs` source), as `projected-impact` findings and scored questions |
| 128 Analysis cycles and assessments | `cycle` | `analysis_cycle`, `cycle_run`, `assessment`, `assessment_log`, `maintenance_run` | Daily, weekly and monthly cycles (created disabled) building a deterministic packet, running up to four budgeted LLM roles (analyst, red team, forecaster, proposer), always publishing an ICD 203-structured assessment that builds on the previous one; monthly maintenance; the intel board; topic `assessments` |
| 129 Object graph, workspaces, annotations | `object` | `relation`, `annotation`, `workspace`, `benchmark_case_proposal` | Object refs `{type}:{id}` (23 types) and one envelope; a temporal relation graph with confidence and provenance; annotations and verdicts with precision metrics; workspaces with a versioned shared context over SSE (`workspace`); dossiers, context summaries, sensor discovery; contract version `1.0` (`GET /v1/contract`) |
| 130 Principals, groups and collaboration | `collab`, `security` | `principal_group`, `group_member`, `object_acl`, `shareable_object`, `tag`, `tag_assignment`, `attachment`, `evidence_item`, `saved_search`, `principal_inbox`, `principal_notification` | Named principals and groups (no passwords in the broker); owner, access list (viewer, commenter, editor, owner) and TLP 2.0 on every work product (`AccessResolver`: unshared objects are invisible, not forbidden); tags, comments with mentions, attachments (PDF and image text; EXIF stripped), Admiralty-graded evidence items, saved searches, personal inbox (topic `me`) |
| 131 Chronicles | `chronicle` | `chronicle`, `chronicle_layer`, `chronicle_event`, `chronicle_frame`, `chronicle_rollup`, `chronicle_series_point`, `chronicle_read_cursor`, … and the partitioned `chronicle_version` | Named monitoring threads with layers and capture modes (`full`, `downsampled`, `events-only`, `latest-per-interval`, `rollup-only`, `frames`) copying matching data into a per-chronicle leaf, independent of global retention; thread, replay and lifecycle (proposed → active ⇄ paused → stopped → archived); topic `chronicle` |
| 132 Reports and intelligence exchange | `report`, `exchange` | `report`, `report_revision`, `report_send`, `exchange_package`, `exchange_sender_key`, `received_package`, `received_object` | Reports (`RPT-yyyy-nnnn`, 11 block types, TLP inheritance, immutable published revisions in Markdown, HTML and JSON) and signed intelligence packages between instances, imported read-only with `remote:` references |
| 133 News coverage and media bias | `news`, `media.outlet` | `news_article`, `news_story`, `news_coverage_daily`, `media_outlet`, `media_provider`, `media_rating`, `outlet_consensus` | Article metadata (`gdelt-articles` and outlet feeds), stories as canonical events (wire reprints count once), outlet ratings only from declared providers, coverage balance, blindspots and divergence |
| 134 Chronicle network | `chronicle.network` | `chronicle_fingerprint`, `chronicle_key`, `chronicle_link`, `chronicle_link_disclosure`, `chronicle_cluster`, `chronicle_salt`, `chronicle_remote_sketch`, `chronicle_sketch_state`, `chronicle_network_churn`, `chronicle_access_request` | Keyed MinHash fingerprints of captured evidence, discoverability bounded by visibility and TLP, opaque links, access requests, shared evidence counted once, circular-reporting detection |
| 135 Social sources and volume | `social` | `social_platform`, `social_volume_hourly`, `social_denominator_hourly`, `social_coverage`, `social_stream_gap`, `social_quota_use`, `social_spend`, `social_salt`, `social_event_log` | Operator-enabled platform adapters (Bluesky Jetstream on by default; Mastodon, Reddit, YouTube, X, Threads, Instagram with the operator's credentials and money budgets); `social-post` records; hourly mention-volume series with denominators; a `social-attention` observability family; topic `social` |
| 136 Narratives, claims and media | `social.narrative` | `social_narrative`, `social_narrative_member`, `social_narrative_log`, `social_claim`, `social_propagation_edge`, `media_fingerprint`, `media_use`, `social_media_queue` | Narratives as canonical events, structured claims as graded evidence items, PDQ image fingerprints and reuse findings, propagation graphs, coordination indicators against null models (never account labels) |
| 137 Social–physical correlation | `social.correlation` | `social_comparison`, `social_comparison_revision`, `social_cue` | Claims compared with independent physical evidence (`corroborated`, `unobserved`, `contradicted`, `insufficient-coverage`, `pending`), attention gaps, budgeted cues that never corroborate their own trigger, out-of-sample lead times |
| 138 External data bindings | `inbound.edp`, `inbound.edp.binding` | `external_source`, `external_binding`, `external_binding_applied`, `external_question_link` | EDP streams bound to series, evidence, indicators, forecasts, places, situations and chronicles by the `BindingRouter` (durable `external.binding.cursor`, exactly once per record version); the Python and TypeScript provider kits (§42); topic `external` |

Notification event types added by the layer include `situation.activated`, `situation.escalated`, `hypothesis.band_changed`, `indicator.observed`, `assumption.broken`, `forecast.resolved`, `finding.significant` and `assessment.published`. Checkpoints 124 and 139 wrote their verification tests; their execution is deferred (§12).

## 41. Adapter doctor (140)

When a source breaks, the doctor (`ai.foresignal.doctor`) says what changed and how the parser could be fixed. Everything except the explanation works without an LLM. **Patches are never applied.**

- **Runtime fixtures and sources:** `gradle/doctor-resources.gradle.kts` (`packageDoctorResources`, run by `processResources`) packages the test fixtures (256 KB cap per file, JSON arrays halved to fit, CSV truncated) and the adapter sources under `META-INF/foresignal/` with indexes; `FixtureCatalog` and `AdapterSourceCatalog` read them and are absent-safe.
- **Structure:** `StructureExtractor` (JSON path trees, CSV/TSV columns, XML with DOCTYPE and external entities disabled, binary magic, HTML) and `StructureDiff` (added, removed and retyped paths, presence drops, emptied arrays, reordered columns, content-type changes, scored rename candidates).
- **Diagnosis:** `DiagnosisRules` produces the 15 `DoctorFinding` codes from health facts, the last 20 fetch rows, budget and backoff, the probe, the diff and a static scan of parser literals (findings relying on it are `heuristic: true`); `UA_BLOCKED` advises never to spoof the User-Agent.
- **Run:** `DoctorService` probes through the budgeted client (skipped when the budget is exhausted, in backoff, or for push and derived sources), keeps the body in memory only (≤ 1 MB), and stores `doctor_report` without raw bodies (90 d); at most one run per source per 10 minutes (`409 DOCTOR_BUSY`).
- **LLM:** `DoctorLlm` (purpose `DOCTOR`, effort low, no tools, template `doctor-system@1.0.0`) with upstream data in an untrusted block and withheld when `EgressPolicy` excludes the source; the proposed patch is checked in memory (`UnifiedDiffApplier`, `PatchSanity`) and labeled "proposed, not applied".
- **Automatic runs:** `DoctorAutoRunner` starts a run on a transition to degraded or failing with a format-change, coverage or parse reason (cool-off `doctor.autoCooldown`, 6 h; LLM only with `doctor.autoLlm`); a `BROKEN` report raises `system.doctor_broken`.
- **Surface:** `POST /v1/sources/{id}/doctor` (200, or 202 + `Location`), report lists and reads; MCP `diagnose_source` (admin) and `get_doctor_report` (agent); `SourceHealth.lastDoctorReport`.

## 42. Client SDKs, CLI, MCP remote authorization, plugins and exchange formats (141)

None of these changes the core data path; each is off or inert until configured.

- **SDKs** (`sdk/`): generated clients committed for Dart (`sdk/dart/foresignal_api`, openapi-generator dart-dio from an OAS 3.0.3 copy made by `scripts/sdk/preprocess-dart.py`), TypeScript (`sdk/typescript/src/generated`, openapi-typescript types for openapi-fetch) and Python (`sdk/python/foresignal_client`, openapi-python-client). Each has a hand-written layer with the same semantics: `ChangeStream` (resume with `after`/`Last-Event-ID` or a stream ticket; on `reset` page the snapshot at `watermarkSeq` and reopen; refetch on `resync`; apply only when `observedAt` ≥ the stored value; heartbeat watchdog and jittered backoff honoring `retry:` and `Retry-After`), cursor iterators, a typed `410 RESYNC_REQUIRED`, and the object contract (`ObjectRef`, envelopes, batched `resolve`, `WorkspaceSync`, deep links). Shared fixtures in `sdk/contract-fixtures/`. `./gradlew generateSdks` and `checkSdkGeneration` (`gradle/sdk.gradle.kts`, versions in `sdk/versions.properties`) are not wired to `build`.
- **EDP provider libraries:** `sdk/python-edp` (`foresignal_edp`) and `sdk/typescript-edp` (`@foresignal/edp`): manifest builder and validator, item builders, a push client (≤ 10,000 items or 5 MB per batch, retries, idempotent re-sends) and a pull handler.
- **CLI** (`cli/`, a standalone Gradle build): picocli, `java.net.http`, Jackson 3; optional GraalVM native image. `login` profiles (file mode 0600; `FORESIGNAL_URL` and `FORESIGNAL_TOKEN` override), command groups mirroring the API (`sources doctor`, `changes tail` with SDK resume semantics, `edp validate`), output `table|json|ndjson`, exit codes 0–3.
- **MCP remote authorization** (`ai.foresignal.mcp.oauth`): settings `mcp.auth` (`GET/PUT /v1/settings/mcp-auth`, admin; `admin` is refused in `scopeMap`); RFC 9728 metadata at `/.well-known/oauth-protected-resource` and `/.well-known/oauth-protected-resource/mcp`; Nimbus JOSE+JWT validates the identity provider's tokens (RS256, ES256, EdDSA/Ed25519 through Tink; `iss`, `aud` per RFC 8707, `exp`/`nbf` with 60 s skew, algorithm allowlist) against a JWKS from `jwksUri` or RFC 8414 / OIDC discovery under the outbound policy, refreshed on an unknown `kid` at most once a minute; `/mcp` answers 401 with `resource_metadata` or 403 `insufficient_scope` (`McpStepUpFilter`). REST keeps Foresignal tokens; there is no authorization server and tokens are never forwarded.
- **WASM parser plugins** (`ai.foresignal.plugin`, [Plugin Design](Plugin-Design.md)): Chicory interpreter, `plugins.enabled=false` by default; modules in `${FORESIGNAL_DATA_DIR}/plugins/{name}/` (`plugin.json`, `plugin.wasm`), each approved by an admin by SHA-256 and re-checked on every call; ABI v1 (`foresignal_parse`, `alloc`/`dealloc`, the single import `env.foresignal_log`; WASI refused); limits 64 MB memory, 5 s interrupting timeout, 16 MB output. The `wasm-feed` family fetches operator-specified feeds (`${FORESIGNAL_DATA_DIR}/wasm-feeds/*.json`) through the budgeted client and the URL policy; records pass the normal normalizer.
- **Exchange formats:** CZML on history and tracks (`ai.foresignal.czml`; MCP `get_track` stores a CZML document under media role `document`); the FDSN event web service 1.2 (`/fdsnws/event/1/…`, QuakeML BED and text); STIX 2.1 export and a read-only TAXII 2.1 server (`/taxii2/**`) over the cyber kinds, every record through `EgressPolicy` on `EXPORT`.
- **Audio and translation:** the `tts` role (OpenAI-compatible `/v1/audio/speech`, budgeted in characters) renders briefing audio (MP3, media role `audio`, signed) with channel attachments and an RSS 2.0 podcast feed (`getBriefingPodcast`); a speech failure never blocks the text. Channel `language` translation runs through the chat role (purpose `translate`, low effort) with masked code spans, URLs and placeholders, a 1.5× length limit, a 24 h cache, and the original sent with `[untranslated]` on timeout or failure.

---

## Appendix A. Open questions

Conflicts found while writing this version, left for the owner to decide:

1. **Is pgvector optional or required?** `docker/db/Dockerfile` says "pgvector is optional at run time: without it Foresignal starts, text search works…", and the former Architecture §14 said "pgvector is optional". The 144 Flyway baseline runs `CREATE EXTENSION IF NOT EXISTS vector`, so pgvector is now required at migration time (§25). Which should change: the Dockerfile comment and operator text, or the baseline?
2. **Baseline parity is unverified.** `V1__baseline.sql` was derived by hand from the entities and the former bootstrap SQL; until `scripts/db/dump-schema.sh` and `FlywaySchemaParityIT` have been run, this document's §7 and §25 describe the intended schema, not a verified one.
3. **Weather budgets: design vs code.** The archived design's budget table differs from the code for `mrms` (design 4,000 MB/day, code 4,500), `mrms-probsevere` (design 2/800, code 4/1,600) and `goes-fdc` (design 4/600, code 4/800). §27.6 follows the code; confirm these are intended.
4. **Should `checkApiCompat` gate anything?** NFR-10 and the former §10.1 promised oasdiff checks "in CI" after v1.0.0. The task exists but, by the standing rule against CI configuration, is not wired to `build` or `check`; `docs/CI-Proposal.md` proposes an `api` stage. Wire it to `check`, adopt the CI proposal, or leave it manual?
5. **Former stack claims corrected to the code.** The former §2 listed `hibernate-vector` (not a dependency: vectors are native SQL slots, §20) and "the Spring WebMVC streamable-HTTP transport" for MCP (the SDK 2.0.1 servlet transports are registered directly). This version follows the code; no decision needed unless the old wording reflected an intent.
6. **`docs/Conventions.md` still points at the archived documents.** Its sources-of-truth table names `Core-Data-Model.md` and `Weather-Imagery-Design.md` (now under `docs/design-history/`) as authoritative for ingest and weather, and its startup order reads "schema → partitions → writer…". With this version those concerns are owned by this document and the order is "Flyway → partitions → writer…". Should Conventions be updated to match?
