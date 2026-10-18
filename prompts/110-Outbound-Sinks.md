# 110 — Outbound Sinks

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/110-CR-Outbound-Sinks.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/109-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/design-history/Gap-Analysis.md` §1 item 8, §2 rows "ATAK / iTAK / WinTAK", "tar1090 / Virtual Radar Server", "OpenCPN / Signal K"; §3 rows "MQTT + HA discovery", "CoT/TAK, NMEA AIVDM, SBS, local APRS-IS serve", "CloudEvents envelope"; §4a (readsb local-only egress, MLAT non-redistributable, "Ports are config, not assumed defaults"); §8 item D.59 ("059a"); §9 (re-serving OpenSky/ACLED/aisstream rejected; FlightAware MLAT re-serving rejected)
- `docs/design-history/Core-Data-Model.md` §10.1 (ChangeBus `ChangeBatch`/`ChangeSummary`, LiveBus `LiveBatch`, change before live), §10.2 client contract (snapshot `order=id` + `watermarkSeq`, apply only if `observedAt` ≥ stored), §13 consumer clamp
- Completion Reports of 014 (`/v1/changes`, `410 RESYNC_REQUIRED`, purge horizon), 015 (live lane), 019 (consumer clamp, `RetentionState`), 035 (outbound URL policy), 039 (private sources), 060 (MQTT client choice), 069 (socket transport), 082 (raw APRS packet field), 085 (AIVDM codec), 099 (aircraft fusion by hex), 109 (`LicenceEgressGuard`)
- CloudEvents 1.0 spec and JSON format (`application/cloudevents+json`, `application/cloudevents-batch+json`); MITRE Cursor-on-Target base event schema; readsb `README-json.md` (aircraft.json fields); BaseStation SBS-1 `MSG` line format; ITU-R M.1371 / IEC 61162-1 (AIVDM armouring); APRS-IS server protocol (login line, `# logresp`, filters)

## Objective

Let existing ecosystems consume Foresignal with no new frontend code. Add a Sink SPI with durable cursors and seven sink types: MQTT retained per-record state, Cursor-on-Target for TAK, readsb `aircraft.json` and SBS-1 for tar1090/VRS, NMEA AIVDM for OpenCPN/Signal K, a local APRS-IS server, and CloudEvents over HTTP. SERVE sinks are off by default and LAN-bound; every record passes the licence egress guard, so data whose licence forbids redistribution is never re-served.

## Tasks

### 1. Sink SPI and persistence (`ai.foresignal.sink`)

- **`SinkType`** (Spring bean per type): `id()`, `SinkTypeSpec spec()` (config JSON schema, secret fields, `kind`), `void validate(config, secrets)`, `SinkInstance open(SinkConfig)`.
- **Kinds:**
  - `CHANGE_FEED`: ordered, at-least-once delivery of persisted versions from a durable cursor (CloudEvents, MQTT event topic). The cursor advances only after the target acknowledges.
  - `STATE_MIRROR`: keeps an external copy of current state: snapshot from `record_head ⋈ record_live`, then ChangeBus plus LiveBus updates (MQTT retained state, CoT, aircraft.json, SBS, AIVDM, APRS-IS). The cursor records the watermark for resume and resync.
- **Modes:** `PUSH` (Foresignal connects out) or `SERVE` (Foresignal listens).
- **Entities:**
  - `sink`: `id` (slug), `type`, `name`, `enabled` (default **false**), `config` jsonb, `secrets` (encrypted with `CredentialCipher`, write-only), `filter` jsonb (`sources[]`, `kinds[]`, `bbox`, `placeId` (036), `watchlistId` (036), `includePrivate` (default false)), `created_at`, `updated_at`.
  - `sink_cursor`: `sink_id` PK, `last_seq`, `watermark_seq`, `last_delivered_at`, `state` (`RUNNING | RESYNCING | FAILING | STOPPED`), `last_error`, `resync_count`, `delivered`, `dropped`.
- **`SinkSupervisor`:** one virtual thread per enabled sink. Start on enable, stop on disable or delete, restart with backoff (5 s → 5 min) on failure, raise a `system` notification (018) after 10 min failing.
- **CHANGE_FEED loop:** read `seq > last_seq` with the sink filter through the 014 changes service, in pages of 500, load payloads with `seq = ANY(?)`, deliver, then persist `last_seq` in the same step as the ack. Below the purge horizon (the 014 resync condition) → `RESYNCING`: snapshot current state as synthetic `SNAPSHOT` events at `watermarkSeq`, then continue from the watermark.
- **STATE_MIRROR loop:** snapshot on start, resync, LiveBus `onLiveDropped` and ChangeBus gaps. Apply updates only when `observedAt` ≥ the mirrored `observedAt` (Core-Data-Model §10.2). Expiry: schedule removal at `validUntil` with a `DelayQueue`; tombstones remove immediately.
- **Retention clamp:** add enabled CHANGE_FEED sink cursors to the 019 consumer clamp (`min(trigger, embedding, sinks) + 1`), with the same release after `2 × window` and the `foresignal.retention.consumer_lag` alarm. Check the clamp exists; add the sink term.
- **Rate control:** a per-sink `maxPerSecond` (default 50 for PUSH, unlimited for SERVE), conflating by `(source, externalId)` for STATE_MIRROR; `dropped` counts conflated updates.

### 2. Egress rules (apply in `SinkRecordGate`, before any type sees a record)

- `LicenceEgressGuard.allows(SINK_PUSH | SINK_SERVE_LAN, terms, ctx)` from 109, per record. If 109 has not landed, stop and implement its guard first. `opensky-states`, `acled` and `aisstream` never reach a sink; `LOCAL_ONLY` records (local receivers, MLAT positions) reach only SERVE sinks bound to loopback or private addresses.
- `private` sources (039) are excluded unless `filter.includePrivate=true` **and** the sink is SERVE on loopback or a PUSH target that resolves to a private address.
- Counter `foresignal.sink.denied{sink,reason}`; the sink status shows denied counts per source.

### 3. SERVE binding and safety

- SERVE sinks listen on `config.bind` (default `127.0.0.1`) and `config.port` (**required, no default**; Gap §4a). Validation accepts only loopback, RFC 1918, link-local, ULA and, when `allowTailnet=true`, 100.64.0.0/10. `0.0.0.0` is allowed only with `allowAllInterfaces=true`, and then every connection's peer address must pass the same check.
- No authentication (these clients can't send any); connections from disallowed peers are closed immediately and counted. Cap `maxClients` (default 8). Idle keepalive per protocol.
- In Docker the container must publish the port: document the compose `ports:` entry with `${FORESIGNAL_SINK_BIND:-127.0.0.1}` in `docs/Operator-Guide.md`.

### 4. Sink types

1. **`mqtt-state`** (STATE_MIRROR, PUSH): reuse the MQTT client and connection settings type from 060; check the 060 Completion Report for the client chosen. Topic `{prefix}/state/{sourceId}/{externalId}` (default prefix `foresignal`, segments sanitised to `[A-Za-z0-9_-]`), retained, QoS 1, payload `{kind, observedAt, validUntil, version, geometry, properties, attribution}`. Removal publishes an empty retained payload. Optional CHANGE_FEED companion topic `{prefix}/changes/{sourceId}` (not retained). LWT `{prefix}/status` = `offline`. Broker URL through the outbound URL policy (035).
2. **`cot`** (STATE_MIRROR, PUSH or SERVE): Cursor-on-Target XML (protocol version 0). **Implement from the public schema; never copy GPL TAK code**, and use no TAK protobuf.
   - Outputs: TAK server TCP (plain, or TLS with a client PKCS#12 secret), UDP multicast SA (default group `239.2.3.1:6969`, LAN only, TTL 1), or SERVE TCP for direct ATAK/iTAK/WinTAK input.
   - `<event version="2.0" uid="FORESIGNAL.{instance8}.{sourceId}.{externalId}" type=… how="m-g" time start stale>` with `<point lat lon hae ce le>` (unknown → `9999999.0`), `<detail><contact callsign/><track course speed/>` (m/s) `<remarks>` (title • attribution) `<link url>`.
   - **Affiliation is neutral or unknown only:** `config.affiliation` ∈ `u` (default) or `n`; never `f`, `h`, `s` or `j`, military sources included. Type map (configurable per kind): aircraft `a-{aff}-A`, vessel `a-{aff}-S`, other point kinds `a-{aff}-G`. Alerts optional (`includeAlerts`), as `a-{aff}-G` points with the rule name as callsign.
   - `stale` = observedAt + min(record TTL, `config.stale`, default 2 min) for tracks, `validUntil` for events. Removal sends the event with `stale = now`.
3. **`readsb-json`** (STATE_MIRROR, SERVE HTTP via the JDK `HttpServer`): `GET /data/aircraft.json` and `/data/receiver.json` (`{version:"foresignal-…", refresh:1000, history:0, lat?, lon?}`; station position only when `config.publishStation=true`).
   - Body `{now, messages, aircraft:[{hex, flight (8-char space-padded), alt_baro (ft or "ground"), alt_geom, gs (kt), track, baro_rate (ft/min), squawk, category, lat, lon, seen, seen_pos, type (adsb_icao | mlat | tisb_icao | other), sources:[…]}]}`.
   - Convert from Foresignal units (metres and m/s per the 011 Adapter Guide altitude convention).
   - **Merge by hex** from 099's fusion source `derived-aircraft` (kind `aircraft`, id = hex, freshest `observedAt` wins; payload `sources[]`, `positionSource`): when it is enabled, the sink reads only that source. When it isn't, apply the same rule (freshest `observedAt` per hex; ties ADS-B > MLAT > TIS-B, local > network) across the filtered aircraft sources. `type` comes from `positionSource`. `ETag` and `Cache-Control: no-cache`.
4. **`sbs`** (STATE_MIRROR, SERVE TCP): BaseStation lines `MSG,{1|3|4},1,1,{HEX},1,{date},{time},{date},{time},{callsign},{alt ft},{gs kt},{track},{lat},{lon},{vrate},{squawk},0,0,0,{onground}` with CRLF. MSG,1 on callsign change, MSG,3 on position, MSG,4 on velocity. New clients get the current snapshot first.
5. **`nmea-aivdm`** (STATE_MIRROR, SERVE TCP, optional UDP unicast to a LAN host): encode vessels with the 085 AIVDM codec. Add an **encoder** to it if it only decodes (6-bit armouring, fill bits, multi-fragment with sequential message id, checksum).
   - Class A → type 1; class B → type 18; static → type 5 (class A) or 24A/24B, re-sent every 6 min and on change.
   - MMSI from payload. Records without a valid 9-digit MMSI are skipped. Sentinels (lat 91, lon 181, SOG 102.3, heading 511) are encoded as "not available".
6. **`aprs-is-serve`** (STATE_MIRROR, SERVE TCP): a read-only APRS-IS-compatible server for local clients (Xastir, YAAC, APRSdroid).
   - Banner `# foresignal aprs-is-serve {version}`. Accept `user CALL pass -1 vers X Y [filter …]`, reply `# logresp CALL unverified, server FORESIGNAL`. Support filters `r/lat/lon/km`, `b/CALL*` and `t/` types (minimum); keepalive comment every 20 s.
   - Stream the raw TNC2 packets of `aprs-is` and `local-aprs` records (082 must keep the raw packet; check, and add a `raw` payload field there if missing).
   - **Client-sent packets are discarded and never gated anywhere**; never connect to APRS-IS as a server.
7. **`cloudevents`** (CHANGE_FEED, PUSH HTTP): structured mode `application/cloudevents+json` (or batched `application/cloudevents-batch+json`, `batchSize` ≤ 100).
   - Attributes: `specversion 1.0`, `id {instanceId}:{seq}`, `source urn:foresignal:{instanceId}:source:{sourceId}`, `type ai.foresignal.record.{created|updated|tombstoned|snapshot}`, `subject {sourceId}/{externalId}`, `time` = observedAt, `datacontenttype application/json`, extensions `foresignalseq`, `foresignalkind`, `foresignalreason`. `data` = version item plus `attributions`.
   - HMAC header as the 018 webhook (`X-Foresignal-Signature: sha256=…` over `timestamp.body`). Retries with backoff on 5xx and 429 (`Retry-After`); 4xx other than 408/429 → `FAILING`, no cursor advance. URL through 035.

### 5. API and observability

- `GET /v1/sinks/types` (admin): spec and config schema per type. `GET/POST /v1/sinks`, `GET/PUT/DELETE /v1/sinks/{id}` (admin; secrets write-only; a create defaults `enabled=false`).
- `POST /v1/sinks/{id}/test` (admin): validate config, connect or bind once, send one synthetic record, report the result.
- `GET /v1/sinks/{id}/status` (admin): cursor, lag (`lastPublishedSeq − last_seq`), state, connected clients (SERVE), delivered, dropped, denied by reason.
- `POST /v1/sinks/{id}/resync` (admin).
- Metrics `foresignal.sink.delivered{sink,type}`, `foresignal.sink.lag_seq{sink}`, `foresignal.sink.clients{sink}`, `foresignal.sink.denied`. `/v1/system` gains a `sinks` block (count enabled, failing). Audit events (023) for create, update and delete.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: schemas `SinkType`, `Sink`, `SinkCreate`, `SinkUpdate`, `SinkFilter`, `SinkStatus`, `SinkTestResult`; the operations above with `x-foresignal-scope: admin`; add the CloudEvents `data` shape under the OAS `webhooks` object (020 convention). Update `AccessPolicy`. Regenerate Postman with BODIES entries for `createSink`, `updateSink` (a disabled `mqtt-state` example) and `testSink`.
- MCP (025): `list_sinks`, `sink_status` (admin).
- `docs/Operator-Guide.md` "Outbound sinks": each type with client setup (tar1090/VRS URL, OpenCPN TCP connection, Signal K NMEA0183 TCP provider, ATAK network input, Xastir server entry), compose port publishing, LAN-binding rules, licence exclusions (OpenSky/ACLED/aisstream never re-served; local/MLAT local-only). `docs/Agent-Guide.md`: CloudEvents consumer example. 034 cookbooks: point the PyTAK recipe at the `cot` sink.

## Tests to write (do not run)

- `SinkSupervisorIT`: enable/disable lifecycle; restart backoff; system event after prolonged failure.
- `ChangeFeedCursorIT`: at-least-once ordering by seq; cursor advances only after ack; a crash mid-page resumes without loss; purge horizon → resync snapshot then continue; clamp includes sink cursors and releases after 2× window.
- `StateMirrorTest`: out-of-order live updates ignored by `observedAt`; expiry removal at `validUntil`; tombstone removal; `onLiveDropped` → resync.
- `SinkRecordGateTest`: OpenSky/ACLED/aisstream always denied; LOCAL_ONLY and MLAT only on loopback/private SERVE; private sources need `includePrivate` plus a local target.
- `ServeBindingTest`: public bind rejected; `0.0.0.0` needs the flag; disallowed peer closed; `maxClients`.
- `MqttStateSinkIT` (HiveMQ CE or Mosquitto Testcontainer): retained topics, empty retained payload on removal, LWT.
- `CotEncoderTest`: golden XML for aircraft, vessel and point; affiliation never outside `u`/`n` even for `adsblol-mil`; stale computation; XML escaping. `CotSinkIT`: TCP receiver gets well-formed events.
- `ReadsbJsonSinkIT`: fields, units (m → ft, m/s → kt), fused merge by hex, `ground`, ETag.
- `SbsEncoderTest`: golden lines for MSG 1/3/4. `AivdmEncoderTest`: round trip through the 085 decoder for types 1, 5, 18, 24; multi-fragment; checksum.
- `AprsIsServeIT`: login handshake, `r/` filter, uploaded packets discarded, keepalive.
- `CloudEventsSinkIT` (WireMock): structured and batch bodies, attributes, HMAC, retry on 503 with `Retry-After`, no advance on 400.
- `SinkApiIT`: CRUD, secrets never returned, admin-only, test endpoint, status lag.

## Acceptance criteria

- All seven types exist, are disabled on creation, and SERVE types refuse non-LAN binds.
- No sink ever emits a record from `opensky-states`, `acled` or `aisstream`.
- tar1090 or VRS can read `/data/aircraft.json`; OpenCPN parses the AIVDM stream (documented manual check).
- CHANGE_FEED sinks survive restarts with no loss and resync below the purge horizon.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `110: Outbound Sinks`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/110-CR-Outbound-Sinks.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `110: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
