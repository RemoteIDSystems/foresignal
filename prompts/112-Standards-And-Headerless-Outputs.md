# 112 — Standards And Headerless Outputs

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/112-CR-Standards-And-Headerless-Outputs.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/111-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 5 (clients that can't send headers), §2 row "QGIS, GDAL, GeoPandas", §3 rows "OGC API Features", "CAP 1.2 output", "iCalendar", "Share links"; §8 item D.61 ("059c"; static map snapshots are already covered by 055)
- `docs/Core-Data-Model.md` §12 (query paths, `timeField`, record DTO, version items)
- Completion Reports of 008 (`startField`/`endField`, `titleField`), 014 (formats, ETags, `title`), 017 (`RuleSqlBuilder` operators, alert lifecycle and states), 023 (restricted tokens, `clientLinks`, audit log), 029 (briefing schedule), 035 (feed keys bound to a path, anonymous profile, per-client limits), 036 (places, saved views), 037 (`/track`), 039 (private sources, precision cap), 047 (`nws-alerts`), 058 (Atom writer), 064 (`launch-library`), 087 (`faa-tfr`), 109 (licence policy, attributions, `PUBLIC_SHARE` egress), 115 (exercise flag, if built later: reserve the CAP `Exercise` status now)
- OGC API – Features Part 1 (17-069r4) and Part 3 Filtering (19-079r2); OGC CQL2 (21-065r2) Basic CQL2 and Basic Spatial Functions; OASIS CAP 1.2 (+ XSD); RFC 5545 (iCalendar), RFC 7986 (`REFRESH-INTERVAL`); RFC 4287 (Atom)

## Objective

Serve Foresignal through standards that existing tools already speak, and to clients that can only fetch a URL: OGC API Features with basic CQL2 for QGIS/GDAL/GeoPandas; CAP 1.2 messages with an Atom index for alerting systems; iCalendar feeds for calendar apps; and share links with path-bound keys and privacy masks. Every output honours token scope, feed keys, private sources and the licence guard.

## Tasks

### 1. OGC API – Features (`ai.foresignal.ogc`, base `/v1/ogc`)

- **Endpoints** (read scope; `openReads` and feed keys honoured; JSON only, no HTML):
  - `GET /v1/ogc` landing page, with links `self`, `conformance`, `data`, `service-desc` → `/v1/openapi.json`.
  - `GET /v1/ogc/conformance` lists Part 1 `core` and `geojson`; Part 3 `filter`, `features-filter`, `queryables`; CQL2 `basic-cql2`, `cql2-text`, `cql2-json`, `basic-spatial-functions`. Don't claim `oas30` (the spec is 3.1).
  - `GET /v1/ogc/collections`, `GET /v1/ogc/collections/{collectionId}`, `…/items`, `…/items/{featureId}`, `…/queryables` (JSON Schema).
- **Collections:**
  - one per kind for **current state**: id = kind; `extent.spatial` from `ST_Extent` of live geometry (cached 5 min); `extent.temporal` from observed times;
  - one per kind for **history**: id `{kind}-history`; items are versions; temporal extent from `seq_clock` and retention.
  - Collection metadata includes `attribution` (109 aggregate) and `itemType: feature`.
- **Items:** GeoJSON FeatureCollection with `type`, `features`, `links` (`self`, `next` with an opaque keyset cursor, `collection`), `numberReturned`, `timeStamp`. Return `numberMatched` only when `count=true` and ≤ 100k (else omit).
  - Feature `id`: current `{sourceId}:{externalId}`; history `{sourceId}:{externalId}:{version}`. Properties are the merged payload plus `title`, `sourceId`, `observedAt`, `eventTime`, `version`.
- **Parameters:**
  - `limit` (default 100, max 10,000)
  - `bbox` (4 or 6 numbers; `bbox-crs` only CRS84, else `400`)
  - `datetime` (instant, `a/b`, `../b`, `a/..`; against `effective` time for current and `observed` for history, per Core-Data-Model §12)
  - `filter`, `filter-lang` (`cql2-text` default, `cql2-json`), `filter-crs` (CRS84 only)
  - Foresignal extensions `source`, `licencePolicy`
  - Unknown parameters → `400` (OGC permits it; document the choice).
- **CQL2:** a hand-written recursive-descent parser for CQL2-Text and a Jackson parser for CQL2-JSON, both producing one AST.
  - Supported: comparison `= <> < <= > >=`, `IS [NOT] NULL`, `AND/OR/NOT`, parentheses, string/number/boolean/`TIMESTAMP('…')`/`DATE('…')` literals, property references, and `S_INTERSECTS(geometry, <WKT POINT|POLYGON|BBOX>)`.
  - Queryables = top-level payload keys declared in the kind's `fieldSpecs` (008) plus `title`, `sourceId`, `observedAt`, `eventTime`, `version`.
  - Translate the AST to parameterised SQL reusing the `RuleSqlBuilder` operator semantics (017: numeric with a type guard, string equality, boolean). Unknown property → `400 FILTER_INVALID` with the position.
  - Limits: depth ≤ 20, ≤ 200 nodes.
- **Guards:** private sources (039) excluded unless the token permits them; restricted-token limits (035) apply; licence policy and `Link rel=license` (109).

### 2. CAP 1.2 output and Atom index (`ai.foresignal.cap`)

- **One CAP message per alert state change** (017 lifecycle). Store `cap_message`: `identifier`, `alert_id`, `msg_type`, `sent`, `xml` (text), `references`.
  - `identifier` = `{instanceId}-{alertId}-{n}`; `sender` = setting `cap.sender` (default `foresignal@{instanceId}`); `sent` with an offset.
  - `status`: `Actual`; `Exercise` for exercise alerts (115 sets the flag; read it if present); `Test` for channel tests.
  - `msgType`: `Alert` on fire; `Update` on any change (ack, snooze, enrichment, severity); `Update` with `<responseType>AllClear</responseType>` on resolve. **Never `Cancel`**: CAP reserves Cancel for erroneous issuance (Gap §3).
  - `<references>` lists every prior message for the alert (`sender,identifier,sent`).
- **`scope` is always `Public`** (Private would require `<addresses>`). Every message carries `<note>Generated automatically by an Foresignal instance from third-party data. Not an official warning.</note>`.
- **`<info>`:**
  - `language` (channel or default `en-US`)
  - `category`, mapped from kind via a table in `src/main/resources/cap/categories.json` (earthquake/tsunami → Geo, weather → Met, fire → Fire, outage/infrastructure → Infra, aircraft/vessel/transport → Transport, cyber/conflict → Security, health → Health, else Other)
  - `event` = rule name
  - `urgency` = `Unknown`, or `Immediate` for edge-triggered critical rules
  - `severity`: critical → `Severe`, warning → `Moderate`, info → `Minor`
  - `certainty`: `Observed` for upstream observations, `Likely` for DERIVED sources, `Unknown` otherwise
  - `effective`, `expires` (record `validUntil` or alert TTL)
  - `senderName` "Foresignal ({instance name})", `headline` (≤ 160 chars), `description` (plain-text body), `web` (deep link from `clientLinks`)
  - `parameter`s `foresignalRuleId`, `foresignalSource`, `foresignalExternalId`, `foresignalSeq`, `foresignalGrade` (109)
- **`<area>`:** `areaDesc` (place name or `{lat},{lon}`); `<polygon>` as `lat,lon` pairs (closed ring, simplified to ≤ 100 points, CW/CCW irrelevant), or `<circle>` `lat,lon radiusKm` for point records (radius = rule geofence buffer or 1 km).
- **Endpoints** (read scope; feed keys allowed):
  - `GET /v1/cap/alerts.atom` (Atom 1.0; reuse 058's writer): one entry per current message, `<link rel="alternate" type="application/cap+xml">`; filters `ruleId`, `minSeverity`, `tags`, `placeId`, `since`
  - `GET /v1/cap/alerts/{alertId}.cap` (latest message)
  - `GET /v1/cap/messages/{identifier}.cap`
- Content type `application/cap+xml`; ETag. `cap_message` rows follow alert retention.
- Do not re-emit upstream official CAP as if Foresignal were the sender: for alerts on official CAP sources (067/089), add `<web>` to the upstream message and state the upstream sender in the description.

### 3. iCalendar feeds (`ai.foresignal.ical`, hand-written writer)

- `GET /v1/calendars/{feed}.ics` (read; feed keys allowed; filters `bbox`, `placeId`, `source`, `kind`):
  - `launches` (064 `launch-library`): DTSTART = NET, DTEND = window end (or NET + 1 h); `STATUS:CONFIRMED` when the launch status is Go, else `TENTATIVE`; LOCATION = pad name; GEO = pad.
  - `tfr` (087 `faa-tfr`): effective → DTSTART, expiry → DTEND (open-ended: DTSTART only plus `X-FORESIGNAL-OPEN-ENDED:TRUE`).
  - `nws` (047 `nws-alerts`): onset (`validFrom`) → DTSTART, `ends`/`expires` → DTEND; SUMMARY = event + area.
  - `briefings` (029): the next 30 days of occurrences of each enabled briefing, expanded from cron plus timezone as individual VEVENTs. Don't translate cron to RRULE.
  - `custom?source=&kind=`: any kind whose descriptor declares `startField`/`endField` (008); `400 ICAL_FIELDS_MISSING` otherwise.
- **Event properties:**
  - `UID` = `{sourceId}:{externalId}@{instanceId}`; `SEQUENCE` = record version; `DTSTAMP` = ingested time of that version; `LAST-MODIFIED`
  - `URL` deep link; `DESCRIPTION` with the attribution line (109)
  - Tombstoned or expired records stay for 7 days as `STATUS:CANCELLED`
- **Calendar properties:** `PRODID:-//Foresignal//{version}//EN`, `VERSION:2.0`, `CALSCALE:GREGORIAN`, `METHOD:PUBLISH`, `X-WR-CALNAME`, `REFRESH-INTERVAL;VALUE=DURATION:PT15M`, `X-PUBLISHED-TTL:PT15M`.
- **Formatting:** UTC times with `Z`; CRLF; fold lines at 75 octets without splitting UTF-8 sequences; escape `\ ; ,` and newlines. Cap 2,000 events per feed (`X-FORESIGNAL-TRUNCATED:TRUE`). ETag plus conditional GET.

### 4. Share links (`ai.foresignal.share`)

- **Entity `share_link`:** `id`, `key_hash` (SHA-256 of `fss_` + 32 random bytes base64url; plaintext shown once), `target_type` (`record | alert | view | track | briefing-run | export`), `target_ref`, `field_mask` jsonb, `precision_m`, `expires_at` (default 7 d, max 30 d), `max_uses`, `uses`, `created_by`, `revoked_at`, `last_used_at`.
- **Path-bound key:** the key authorises only `GET /v1/k/{key}` and its sub-paths `/v1/k/{key}/geojson`, `/v1/k/{key}/tiles/{z}/{x}/{y}.mvt` (views and tracks) and `/v1/k/{key}/download` (exports) (Conventions, key-in-path URLs), resolved server-side to the target. It is never accepted as a bearer token anywhere else. Reuse the 035 feed-key matcher.
- **Rendering:** JSON (or GeoJSON by `Accept` or `/geojson`). No HTML (Charter: no UI). When `clientLinks.share` (023) is configured, `?open=1` redirects (302) to the client template with the key.
- **Masks:**
  - Default for personal or private sources (039): precision capped to 1,000 m (round coordinates and coarsen geometry), observer, device and address-like fields removed, payload reduced to `title`, `kind`, `observedAt` unless the creator passes an explicit allowlist. Private targets need admin.
  - Other sources default to the full payload. Any `field_mask` is an allowlist applied after licence filtering.
- **Licence:** `PUBLIC_SHARE` egress (109). Records whose redistribution is not `ALLOWED` are removed. If the target itself is such a record, `409 SHARE_LICENCE_FORBIDDEN` at creation. Attribution is always included.
- **API:** `POST /v1/share-links` (agent; admin for private targets), `GET /v1/share-links` (agent: own; admin: all), `DELETE /v1/share-links/{id}` (revoke). Per-link rate limit 60/min (035 limiter); `410 SHARE_EXPIRED` when revoked, expired or exhausted. Audit events on create, use (sampled) and revoke.

### 5. Spec, Postman, docs

- `docs/OpenAPI.json`: OGC operations (`ogcLanding`, `ogcConformance`, `ogcCollections`, `ogcCollection`, `ogcItems`, `ogcItem`, `ogcQueryables`), CAP (`capAtom`, `getCapForAlert`, `getCapMessage`), iCalendar (`getCalendar`), share links (`createShareLink`, `listShareLinks`, `revokeShareLink`, `openShareLink` and its sub-paths). Schemas `OgcLandingPage`, `OgcConformance`, `OgcCollection(s)`, `ShareLink`, `ShareLinkCreate`; error codes `FILTER_INVALID`, `ICAL_FIELDS_MISSING`, `SHARE_LICENCE_FORBIDDEN`, `SHARE_EXPIRED`. Every operation gets `x-foresignal-scope` (`/s/**` is `open` with key validation). Update `AccessPolicy` and the 035 feed-key path table. Regenerate Postman with a BODIES entry for `createShareLink`.
- MCP (025): `create_share_link` (agent).
- `docs/Operator-Guide.md`: "Standards outputs" (QGIS OGC API connection, GDAL `OAPIF:` string, GeoPandas example, CAP consumers and the not-official note, subscribing a calendar with a feed key, share-link privacy defaults).

## Tests to write (do not run)

- `Cql2TextParserTest` and `Cql2JsonParserTest`: every supported construct, precedence, literals, errors with positions, depth and node limits; text and JSON forms give equal ASTs.
- `Cql2SqlTranslatorTest`: SQL and parameters per operator; type guards; `S_INTERSECTS` with point, polygon and bbox; consumes the 017 `operator-truth-table.json` fixture where operators overlap.
- `OgcFeaturesIT`: landing, conformance, collections (current and history), items paging via `next`, `bbox`, `datetime` forms, `filter` in both languages, queryables schema, private exclusion, licence policy; a feature request with a feed key.
- `CapMessageWriterTest`: validates against the CAP 1.2 XSD (test resource) for Alert, Update and AllClear; `scope` always Public; note present; polygon closure and simplification; circle for points; category map; Exercise status when the flag is set. `CapFeedIT`: Atom entries track state changes; references chain.
- `ICalWriterTest`: folding at 75 octets including multibyte, escaping, CRLF, UID/SEQUENCE, cancelled entries, truncation flag. `CalendarFeedIT`: each feed with fixture records; briefing expansion across a DST change; `ICAL_FIELDS_MISSING`.
- `ShareLinkIT`: key works only on its own paths and not as a bearer token; private mask (precision, removed fields); explicit allowlist; licence-forbidden target → 409; expiry, max uses, revoke → 410; rate limit; audit rows.

## Acceptance criteria

- QGIS connects to `/v1/ogc` as an OGC API Features server and filters with CQL2 (manual check documented); conformance lists only implemented classes.
- CAP messages validate against the 1.2 XSD, are Public with the not-official note, and use Update/AllClear, never Cancel.
- Calendar apps can subscribe with a feed key; UIDs are stable and SEQUENCE follows the record version.
- Share links are path-bound, mask personal data by default, and never expose non-redistributable records.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `112: Standards And Headerless Outputs`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/112-CR-Standards-And-Headerless-Outputs.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `112: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
