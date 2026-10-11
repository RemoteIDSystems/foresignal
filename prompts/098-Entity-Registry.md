# 098 — Entity Registry

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/098-CR-Entity-Registry.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/097-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 2 and §5 row "Entity registry" (deterministic keys only; FAA registry without owner PII), §8 item 53 (labelled 056a = this prompt)
- The 008 Completion Report: the descriptor `entityKeys` field and the entity-key extraction hook
- The 036 Completion Report (watchlists, `inWatchlist`), 025 (MCP tool registry), 039 (private enforcement), 019 (retention consumer clamp), 094 (chunked fetch and complete-set reconciliation)
- `docs/Core-Data-Model.md` §10.1 (ChangeBus), §12 (head ⋈ live reads), §13 (consumer clamp, HeadPurge)
- FAA Releasable Aircraft Database: https://www.faa.gov/licenses_certificates/aircraft_certification/aircraft_registry/releasable_aircraft_download (archive `https://registry.faa.gov/database/ReleasableAircraft.zip`, about 60 MB, refreshed daily at 23:30 Central; layout in `ardata.pdf` inside the archive)

## Objective

Give Foresignal a canonical identity layer for physical objects observed across sources: one aircraft seen by adsb.lol, airplanes.live, OpenSky and a local receiver, one vessel seen by three AIS feeds and named in a sanctions list, one satellite in CelesTrak and SatNOGS. Resolution uses **deterministic keys only** (ICAO hex, registration, airframe serial, MMSI, IMO, NORAD id, COSPAR id, callsign with a validity window), with confidence and time-bounded links. There is no fuzzy name matching and no automatic merge of two anchored entities. Entities cover vehicles and objects only, never people or organisations.

## Tasks

### 1. Schema (`ai.foresignal.entity`, Hibernate entities)

- **`entity`:** `id varchar(26)` (ULID) PK, `type` (`aircraft|vessel|satellite|station`), `display_name`, `attributes jsonb` (≤ 16 KB), `provisional bool`, `status` (`ACTIVE|MERGED`), `merged_into varchar(26) null`, `created_at`, `updated_at`.
- **`entity_key`:** `id` identity PK, `entity_id`, `key_type`, `key_value varchar(64)` (normalised), `role` (`ANCHOR|WEAK`), `confidence real`, `origin` (`REGISTRY|CO_ASSERTED|OBSERVED|OPERATOR`), `origin_ref` (`sourceId:externalId` or operator token id), `valid_from`, `valid_to null`, `retracted bool`. Partial unique index `(key_type, key_value) WHERE valid_to IS NULL` (create it in `SchemaBootstrap`), index `(entity_id)`.
- **`entity_alias`:** `id`, `entity_id`, `alias`, `alias_type` (`NAME|FORMER_NAME|OPERATOR_LABEL`), `valid_from`, `valid_to`, `origin`. Index `lower(alias) text_pattern_ops` for prefix search.
- **`entity_link`:** `id`, `entity_id`, `source_id`, `external_id`, `key_type`, `key_value`, `confidence`, `valid_from`, `valid_to null`, `first_seq`. Partial unique `(source_id, external_id) WHERE valid_to IS NULL`; index `(entity_id, valid_to)`.
- Cursor: reuse the durable consumer-cursor table from 038 if it exists (`consumer_cursor(name, last_seq)`), otherwise create it here. Register the `entity` cursor with the retention consumer clamp (019); if 019 hard-codes trigger and embedding cursors, generalise it to a `RetentionConsumer` list.

### 2. Keys and normalisation (`EntityKeys`, pure)

| Key type | Role | Normalisation and validation | Default confidence |
|---|---|---|---|
| `airframe` | ANCHOR | `{faaMfrMdlCode}:{serial}` uppercased, trimmed | 1.0 (registry) |
| `icao24` | WEAK | 6 lowercase hex; reject `000000`, `ffffff`, and readsb non-ICAO addresses (`~` prefix) | 0.9 |
| `registration` | WEAK | uppercase, spaces and hyphens removed for `key_value`; hyphenated form kept as an alias | 0.9 |
| `imo` | ANCHOR | 7 digits with a valid check digit (Σ dᵢ·(7…2) mod 10 = d₇) | 1.0 |
| `mmsi` | WEAK | 9 digits; only ship MMSIs (first digit 2–7) create vessel links; reject `000000000`, `111111111`, `123456789`; ignore SAR aircraft (`111…`), AtoN (`99…`) and coast stations (`00…`) | 0.8 |
| `norad` | ANCHOR | positive integer catalogue number | 1.0 |
| `cospar` | ANCHOR | `YYYY-NNNP{PP}` | 1.0 |
| `callsign` | WEAK | uppercase, domain-prefixed: `air:` (flight id), `sea:` (radio callsign), `ham:` (base call, SSID stripped) | air 0.5, sea 0.7, ham 0.9 |

- Callsign keys never create an entity on their own. `air:` links are flight-session links: they close when the linked record has not been observed for `entity.callsignGap` (default 6 h).
- Privacy ICAO Address (PIA) hexes are absent from the registry; never try to resolve them to a registration. Payloads never carry owner data (§4).

### 3. Resolver and linker

- **`EntityResolver.resolve(KeySet, observedAt, origin)`** is pure and deterministic. Input keys come from the descriptor `entityKeys` declaration (008: `{type, field|fields, role, prefix}`) via the extraction hook. Order:
  1. Normalise and validate; drop invalid keys (`foresignal.entity.invalid_keys{type}`).
  2. An anchor key with an open entity E → link to E. For each co-asserted weak key: open on E → no-op; open on a **provisional** F → merge F into E (move keys, aliases and links; `F.status=MERGED`, `merged_into=E`); open on another **anchored** F → close F's key at `observedAt`, open it on E (re-assignment, e.g. MMSI moved to a new hull), count `foresignal.entity.reassigned`.
  3. An anchor key with no entity → create an anchored entity with every co-asserted key.
  4. No anchor key: weak keys matching exactly one entity → link. Matching two entities → link by precedence (`icao24` > `registration` > `air:`; `mmsi` > `sea:`), never merge, count `foresignal.entity.conflicts`.
  5. Nothing matches and a non-callsign weak key exists → create a provisional entity (`provisional=true`).
- **`EntityLinker`** consumes ChangeBus with the durable `entity` cursor:
  - processes only `FIRST`, `REAPPEAR`, `SIGNIFICANT` and `TOMBSTONE` versions (keys are significant fields, so other reasons cannot change them); loads payloads with `seq = ANY(?)`;
  - upserts `entity_link` (closing the previous open link when the record now resolves elsewhere); a tombstone closes the record's open link;
  - copies declared attributes (descriptor `entityAttributes`: payload field → attribute name) into `entity.attributes`, registry origin winning over observations; sets `display_name` by precedence registration > vessel name > satellite name > key;
  - **backfill:** with no cursor, scans current heads of every source with `entityKeys` in chunks of 2,000, then starts from the watermark;
  - a 10-minute job closes `air:` links past `callsignGap` (join `record_live.observed_at`), with no per-observation writes.
- **Private sources (039):** records from `private` sources never create entities or links unless `foresignal.entity.includePrivate=true`; even then, links from private sources are visible only to admin tokens.
- **Descriptor updates:** add `entityKeys` and `entityAttributes` to existing adapters where the payload carries the fields: aircraft families and `local-adsb-*` (`hex`, `r`, `flight`), vessel sources (`mmsi`, `imo`, `callsign`, `name` as alias), `celestrak-gp` (`NORAD_CAT_ID`, `OBJECT_ID`, `OBJECT_NAME`), sanctions lists from 091 (vessel IMO and MMSI, aircraft registration and serial only; never person entries), and `aprs-is` (`ham:`) when it is not private.

### 4. `faa-registry` adapter (`ai.foresignal.source.adapters.faaregistry`)

- POLLING, kind `aircraft-registration`, STANDARD, ALL, no TTL, `observedAtMode=RECEIVED`, `licence="US-PD"`, `termsUrl` = the FAA download page. Interval default 7 d (min 1 d), budget 2/min and 10/day, `mbPerDay` 200, `maxResponseBytes` 150 MB.
- `ctx.http().download(...)` to a temp file (conditional on `Last-Modified`), open the zip safely (reject entries with `..` or absolute paths, cap uncompressed size at 500 MB), stream-parse `MASTER.txt` and `ACFTREF.txt` (comma-delimited, header row, trailing spaces, trailing empty column). Emit with the 094 chunked fetch and complete-set reconciliation, so deregistered N-numbers are tombstoned. Our identifiable UA only; if the host refuses it, mark the source failing (no spoofing).
- `externalId = "N" + N-NUMBER`. Payload allowlist: `nNumber, serial, mfrMdlCode, manufacturer, model, typeAircraft, typeEngine, engines, seats, weightClass, yearMfr, registrantType` (decoded enum), `certIssueDate, airworthinessDate, expirationDate, statusCode, modeSHex` (lowercase), `fractionalOwner, kitMfr, kitModel`.
- **Never stored:** `NAME, STREET, STREET2, CITY, STATE, ZIP CODE, REGION, COUNTY, COUNTRY, OTHER NAMES(1–5), UNIQUE ID`. Optional param `nonIndividualOwnerNames` (default false) stores `NAME` only when the registrant type is Corporation (3), Government (5), LLC (7) or Non-Citizen Corporation (8).
- `entityKeys`: `airframe` from `[mfrMdlCode, serial]` (anchor), `registration` from `nNumber`, `icao24` from `modeSHex`; origin `REGISTRY`. When a later snapshot changes a hex or N-number, the old key closes at the snapshot date.
- Fixture: capture a real archive once, cut `MASTER.txt` to 30 rows and `ACFTREF.txt` to the referenced codes, re-zip, and record the capture date in `README.md`. Add a hand-made `edge-cases` zip (blank hex, individual registrant, a path-traversal entry).

### 5. API, MCP, watchlists

- `GET /v1/entities` (read): `q` (prefix, ≥ 2 chars, over aliases and display name), `type`, `key=<type>:<value>`, `at` (validity instant), `limit` ≤ 200, keyset `after`. Returns `EntityPage`.
- `GET /v1/entities/{entityId}` (read): entity, keys (validity, confidence, origin), aliases, per-source open-link counts; a merged id returns the canonical entity with `requestedId`.
- `GET /v1/entities/resolve?key=&at=` (read): 404 `ENTITY_NOT_FOUND` when nothing matches.
- `GET /v1/entities/{entityId}/records` (read): current records across sources (head ⋈ live), each with its `link` (keyType, confidence, validFrom, validTo); `at` selects links valid at that instant; `includeClosed`; `format=json|geojson`.
- Admin: `POST /v1/entities/{entityId}/keys` (operator assertion with confidence and validity; 400 `ENTITY_KEY_INVALID`; 409 `ENTITY_KEY_CONFLICT` when the key is open elsewhere and `force` is false), `DELETE /v1/entities/{entityId}/keys/{keyId}` (sets `valid_to=now`, `retracted=true`; rows are never deleted), `POST /v1/entities/{entityId}/merge` `{otherId}` (operator-only merge of anchored entities).
- `/v1/records` and `getRecord`: `include=entity` adds `entityId` from the open link.
- MCP (025 registry, read scope, compact output): `get_entity` (`id` or `key`, `detail`, open records capped at 20 with `*_total`), `find_entity` (`q`, `type`, `key`, `limit` ≤ 25).
- **Watchlists (036):** items may be `entity:<id>`. At evaluation, expand each to the entity's open keys, mapped to fields through the source's `entityKeys`, and feed the existing `in` matching. Refresh on `EntityKeysChangedEvent`. Rule SQL and `TriggerPersistGate` use the same expansion snapshot.
- Retention: entity facts are not tied to record retention. Closed links older than `retention.entityLinks` (default 365 d) are deleted by a new RetentionJob step.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: the operations above with `x-foresignal-scope` (read; admin for writes), schemas `Entity`, `EntityKey`, `EntityAlias`, `EntityLink`, `EntityPage`, `EntityKeyInput`, `EntityMergeInput`, `Record.entityId`, error codes `ENTITY_NOT_FOUND`, `ENTITY_KEY_INVALID`, `ENTITY_KEY_CONFLICT`. AccessPolicy rules. Add BODIES examples for the two POSTs, then run `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md` row `faa-registry`; `docs/Agent-Guide.md` (entity tools, "deterministic keys only"); `docs/Adapter-Guide.md` (`entityKeys`, `entityAttributes`); `docs/Operator-Guide.md` (privacy: no owner PII, PIA, private sources).

## Tests to write (do not run)

- `EntityKeysTest`: every normalisation and rejection row, IMO check digit, MMSI class filtering, the `~` hex.
- `EntityResolverTest` (parameterised): anchor link, provisional merge, re-assignment closes the old key, two-entity conflict uses precedence and never merges, callsign-only creates nothing.
- `EntityLinkerIT` (`@IntegrationTest`, `TestRecords`): AIS static data with MMSI+IMO merges an earlier MMSI-only provisional; an MMSI moving to a new IMO closes and reopens; a tombstone closes the link; backfill from heads; callsign gap closure; private source excluded.
- `FaaRegistryParserTest`: fixture rows, PII columns absent from every payload, `nonIndividualOwnerNames` behaviour, zip-slip rejection. `FaaRegistryAdapterTest` (WireMock): download, `Last-Modified` 304. `FaaRegistryIngestIT`: snapshot 2 with a changed hex closes the old `icao24` key; a missing N-number is tombstoned.
- `EntityControllerIT`: search, resolve, records across three sources, `at`, admin key assertion and retraction, merge, scopes. `EntityMcpToolsTest`. `WatchlistEntityExpansionIT`: an `entity:` item fires on any of its sources and follows a key re-assignment.

## Acceptance criteria

- One aircraft observed by two aircraft sources plus the FAA registry resolves to one entity, and `/v1/entities/{id}/records` returns both live records.
- No payload, entity attribute or API response contains FAA owner name or address fields (unless the non-individual option is on).
- No code path merges two anchored entities without an operator request.
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
2. **Commit** all changes with the message `098: Entity Registry`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/098-CR-Entity-Registry.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `098: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
