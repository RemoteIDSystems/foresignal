# 109 — Licence And Provenance

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/109-CR-Licence-And-Provenance.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/108-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Product-Charter.md` FR-56 (licence and provenance per record, licence-filtered responses, aggregated attributions), §12 assumptions
- `docs/Core-Data-Model.md` §8 (`fetch_log.licence`, `adapter_version`, `principal`), §13 step 5 (`fetch_log` retention), §14 (provenance: `record_version.batch_id → fetch_log.id`, licence snapshotted on the fetch row, `licence`/`termsUrl` in `/v1/sources`)
- `docs/Gap-Analysis.md` §3 rows "Bulk exports", "Signed outputs", "CoT/TAK, NMEA AIVDM, SBS…"; §4a (readsb "local-only egress default", "Treat MLAT as non-redistributable"); §5 row "Reliability grading (Admiralty)"; §8 item C.58; §9 (re-serving OpenSky/ACLED/aisstream rejected; Sigstore rejected in favour of instance-key JWS)
- `docs/Weather-Imagery-Design.md` §2.3 (`raster_product.spec` "structured attribution"), §10.3 ("Licence & Provenance: aggregates the structured attribution")
- `docs/Source-Catalog.md` notes column (licence notes: ODbL, CC BY, CC BY-NC-SA, "Non-commercial terms", "Licence restricts redistribution")
- RFC 7515 Appendix F (detached JWS), RFC 7797 (`b64:false`), RFC 8037 (OKP JWK, Ed25519), RFC 9864 (fully specified `alg` "Ed25519"; "EdDSA" deprecated), RFC 7638 (JWK thumbprint), RFC 4946 / IANA link relation `license`

## Objective

Make licence, provenance and reliability first-class on every record and every egress path. Each version resolves to the licence in force when it was fetched; clients can ask for licence-filtered responses and always get aggregated attributions; sources carry an Admiralty reliability grade and records a credibility grade. The instance gets an Ed25519 signing key, published as a JWKS, used for detached JWS on exports and briefings. Prompts 110–113 (sinks, exports, share links, federation) call the egress guard defined here.

## Tasks

### 1. Structured licence terms (`ai.foresignal.provenance`)

- **`LicenceTerms`** record: `id` (SPDX id where one exists, e.g. `CC-BY-4.0`, `ODbL-1.0`, `CC0-1.0`, `CC-BY-NC-SA-4.0`; else a slug like `us-gov-public-domain`, `opensky-terms`, `acled-terms`, `aisstream-terms`, `unknown`), `name`, `commercialUse` (`ALLOWED | FORBIDDEN | UNKNOWN`), `shareAlike` (bool), `attributionRequired` (bool), `redistribution` (`ALLOWED | LOCAL_ONLY | FORBIDDEN | UNKNOWN`), `attributionText`, `termsUrl`.
- **`LicenceCatalog`**: built-in entries for the ids above plus `CC-BY-SA-4.0`, `CC-BY-NC-4.0`, `PDDL-1.0`, `OGL-UK-3.0`, `NLOD-2.0`, `DL-DE-BY-2.0`. Loaded from `src/main/resources/provenance/licences.json` so new entries need no code.
- **Descriptor:** add `licenceTerms` (`LicenceTerms`, nullable) next to the existing free-text `licence`/`termsUrl` on `SourceDescriptor` (004). If only `licence` is set and matches a catalogue id, derive the terms from the catalogue. Registry validation (004) warns when neither resolves. Custom PUSH sources (024) accept `licenceTerms` in their config; peer sources (113) carry per-record terms (task 2).
- **Egress-safe default:** `UNKNOWN` redistribution is treated as `LOCAL_ONLY` and `UNKNOWN` commercial use as `FORBIDDEN` by every guard below.
- **Audit every registered adapter descriptor** and set `licenceTerms`, using the Source Catalog notes. Binding cases:
  - `opensky-states`, `acled`, `aisstream`: `redistribution=FORBIDDEN` (Gap §9). OpenSky and ACLED also `commercialUse=FORBIDDEN`.
  - `adsblol-*`: `ODbL-1.0` (share-alike, attribution).
  - local receivers (069 readsb/978, 085 `local-ais`, 082 `local-aprs`, 083 `*-local`, 086 decoders): `LOCAL_ONLY` by default (Gap §4a). Records with `quality.positionSource = MLAT` are `LOCAL_ONLY` per record regardless of source.
  - `submarine-cables`: `CC-BY-NC-SA-4.0`; IOC sea level: non-commercial.
  - US federal sources (USGS, NWS, NOAA, NASA, FAA): `us-gov-public-domain` with the courtesy attribution the agency asks for.
  - Raster products (042): map `raster_product.spec` structured attribution into `LicenceTerms`.
- Add `RegisteredSourcesHaveLicenceTest`: every registered non-DERIVED source resolves to non-null terms. DERIVED sources (099 etc.) inherit the **most restrictive** terms of their inputs, declared via a new descriptor field `derivedFrom` (list of source ids).

### 2. Per-record licence resolution

- **Rule:** a version's licence is `fetch_log.licence` of its `batch_id` (Core-Data-Model §14). `FetchExecutor` (010) must snapshot the resolved terms id there; check it does and add it if missing. PUSH batches (024) snapshot the custom source's terms.
- **Overrides**, applied in order: per-record MLAT downgrade (task 1); a per-record `_foresignal.originLicence` for peer sources (113; declare `_foresignal` a volatile field there); otherwise the batch licence.
- **`LicenceResolver`**:
  - `resolve(List<RecordKey+seq>)` → `Map<seq, LicenceTerms>` with **one query per response**: `SELECT v.seq, f.licence FROM record_version v LEFT JOIN fetch_log f ON f.id = v.batch_id WHERE v.seq = ANY(:seqs)`. Use the head's `seq` for current reads (014 already returns `seq`).
  - Fall back to the source's current descriptor terms when `batch_id` is null or the fetch row is gone, and mark `licenceBasis: "descriptor"`.
  - Cache catalogue lookups; the query is the only per-request cost.
- **Keep the evidence:** amend the `fetch_log` retention step (019, Core-Data-Model §13 step 5) so rows with `versions_written > 0` are kept for `max(retention.provenance, the source's version window)`, so the batch licence outlives no version it describes.

### 3. Client licence policy and filtering

- **Policy flags:** `no-nc` (exclude `commercialUse != ALLOWED`), `no-sa` (exclude `shareAlike`), `redistributable` (exclude `redistribution != ALLOWED`). `LicencePolicy` is an immutable set of flags.
- **Inputs:** request header `Foresignal-Licence-Policy: no-nc, no-sa` or query `licencePolicy=no-nc,no-sa`; and a per-token default: add `licence_policy text[]` to `api_token` (023) and accept it on token create/update. The token policy is a **floor**: a request may add flags but cannot remove the token's flags.
- **Applied on** every record-returning read: `/v1/records`, `/v1/history`, `/v1/changes`, SSE (015: filter events; never send excluded records), tiles (016: drop features), `/v1/search`, MCP read tools (025), Ask and briefing context (028/029: excluded records never reach the model). Filtered responses add `licenceFiltered: <count>` (or `Foresignal-Licence-Filtered` header for streams and tiles). Where a policy can exclude a whole source cheaply, prune the source in SQL before reading.
- **Aggregated attribution** on every record-returning response:
  - JSON pages and GeoJSON FeatureCollections gain `attributions: [{sourceId, licence, text, termsUrl, count}]` (foreign member in GeoJSON).
  - Every such response also carries `Link: <termsUrl>; rel="license"` per distinct licence (cap 10) and `Foresignal-Attribution` with the joined `attributionText` (≤ 1 KB, truncated with "…").
  - TileJSON (016) `attribution` is the aggregate for the layer's sources.
  - `GET /v1/attributions?source=&kind=` (read) returns the aggregate for a scope (for SSE clients and map footers).
- `include=provenance` on records adds a `provenance` block: `licence` (terms), `licenceBasis`, `fetchId`, `adapterVersion`, `principal` (admin only), `reliability`, `credibility`, `grade`.

### 4. Egress guard

- **`EgressPath`** enum: `API_READ`, `TILE`, `SSE`, `MCP`, `LLM_LOCAL`, `LLM_REMOTE`, `NOTIFICATION`, `FEED` (058 Atom/KML, 112 CAP/iCal), `SINK_SERVE_LAN`, `SINK_PUSH` (110), `EXPORT` (111), `ARCHIVE` (111 cold archive; operator-owned storage), `PUBLIC_SHARE` (112), `FEDERATION` (113).
- **`LicenceEgressGuard.allows(EgressPath, LicenceTerms, RecordContext)`** with this table:

| Redistribution | API/TILE/SSE/MCP/LLM/NOTIFICATION/FEED/ARCHIVE | SINK_SERVE_LAN | SINK_PUSH, EXPORT | PUBLIC_SHARE, FEDERATION |
|---|---|---|---|---|
| ALLOWED | yes | yes | yes | yes (NC terms: yes; attribution always attached) |
| LOCAL_ONLY | yes | yes, only if the sink binds loopback/private | no (EXPORT: only with `personalUse=true`) | no |
| FORBIDDEN | yes (own use) | no | no (EXPORT: only with `personalUse=true`, sidecar marks "do not redistribute") | no |

- `private` sources (039) are evaluated first: excluded on every path except `API_READ`/`TILE`/`SSE`/`MCP` for permitted tokens and `ARCHIVE`, regardless of licence.
- Each denial counts `foresignal.licence.egress.denied{path,licence}`. List every egress path in `docs/Operator-Guide.md` "Licences and egress" with this table; sinks (110) are an egress path.

### 5. Reliability and credibility (Admiralty code)

- **Source reliability** `A`–`F` (A completely reliable … E unreliable, F cannot be judged). Descriptor field `reliability` (default `F`); operator override `source_settings.reliability` via `PUT /v1/sources/{id}` (validate the letter). Seed defaults: official agencies `B`, established aggregators and the operator's own receivers `C`, community or news-derived feeds (GDELT, Wikipedia surge) `D`, custom and peer sources `F` unless configured.
- **Information credibility** `1`–`6` (1 confirmed … 5 improbable, 6 cannot be judged). `CredibilityService.assess(record)`, in this order:
  - `5` when an open 099 `derived-deception` record (kind `deception`) names the entity;
  - `1` when the record is a member of a 099 `derived-corroboration` cluster with ≥ 3 independent sources, at least one of them graded `A` or `B`;
  - otherwise the cluster's own `credibility` value when the record is a member (099 assigns 2 for ≥ 2 independent sources, 3 for one);
  - `3` when the source is graded `A`/`B` and the record has no QA flags;
  - otherwise `6`.
  - Look up membership through `members[]` of current corroboration records with one indexed query per response, not per record. Check 099's Completion Report for the actual source ids and payload fields; if 099 is missing, everything stays `6`/`3`. Now that grades exist, make 099 read `reliability` from this service, as its body anticipates.
- `grade` = reliability + credibility (e.g. `B2`). Shown in `provenance`, MCP `long` output, Ask citations and briefing evidence lines. Never hashed or stored on versions: computed at read time.

### 6. Instance signing key and detached JWS

- **Key:** on first start generate an Ed25519 key pair with JDK `KeyPairGenerator.getInstance("Ed25519")`; store the private key encrypted in `system_secret` (the VAPID pattern from 031) with `created_at`, `status` (`active | retired`). `kid` = RFC 7638 thumbprint of the OKP JWK.
- **JWKS:** `GET /.well-known/jwks.json` and `GET /v1/system/jwks` (open, no token; `Cache-Control: public, max-age=3600`): active plus retired keys (`{"kty":"OKP","crv":"Ed25519","x":…,"kid":…,"use":"sig","alg":"Ed25519"}`). Retired keys are kept for `signing.retainRetired` (default 365 d).
- **Rotate:** `POST /v1/system/signing-key/rotate` (admin) retires the current key and activates a new one; audit event (023).
- **`JwsSigner.signDetached(byte[] payload, String typ)`:** protected header `{"alg":"Ed25519","kid":…,"typ":typ,"b64":false,"crit":["b64"],"iat":…,"iss":<instance id>}`; signing input = `ASCII(BASE64URL(header)) || '.' || payload`; output `header..signature`. Setting `signing.legacyAlg=true` emits `"alg":"EdDSA"` for older verifiers. `JwsVerifier` verifies against the JWKS (used by 113 peer pinning).
- **Apply to:**
  - Briefing runs (029): sign the stored markdown bytes; `BriefingRun.signature`, header `Foresignal-Signature` on `GET /v1/briefings/{id}/runs/{runId}` markdown and HTML renditions (sign each rendition separately).
  - Exports (111): sign `manifest.json`. Provide `ManifestSigner` now so 111 only calls it.
  - Document a verification recipe (Python `jwcrypto`, Node `jose`) in `docs/Operator-Guide.md`. C2PA stays deferred; no Sigstore (Gap §9).

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: schemas `LicenceTerms`, `Attribution`, `Provenance`, `SigningKeyInfo`; `attributions` and `licenceFiltered` on `RecordPage`, `FeatureCollection`, `HistoryPage`, `ChangePage`, `SearchResult`; `licencePolicy` query parameter and `Foresignal-Licence-Policy` header on every record-returning operation; `include=provenance`; `reliability` on `SourceDescriptor`/`SourceSettingsUpdate`; `licenceTerms` on `SourceDescriptor` and custom-source schemas; `licencePolicy` on token create/update; `signature` on `BriefingRun`; operations `getAttributions` (read), `getJwks` (open), `rotateSigningKey` (admin), each with `x-foresignal-scope`. Update `AccessPolicy` (`/.well-known/**` open, the rest as listed). Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`; add a BODIES entry for `rotateSigningKey` if it takes a body).
- MCP (025 registry): add `get_attributions`; include `grade` and licence in `get_record` long output.
- `docs/Source-Catalog.md`: add a `Licence` value per row matching the descriptor (full column restructure happens in 144). `docs/Adapter-Guide.md`: "Licence terms and reliability" section (required fields, MLAT rule, `derivedFrom`).

## Tests to write (do not run)

- `LicenceCatalogTest`: catalogue loads; every built-in id resolves; unknown id → `unknown` terms with restrictive defaults.
- `RegisteredSourcesHaveLicenceTest`: as task 1, plus the binding cases (OpenSky/ACLED/aisstream FORBIDDEN, adsb.lol ODbL).
- `LicenceResolverIT`: batch licence wins; descriptor fallback with `licenceBasis`; MLAT downgrade; peer `originLicence`; one SQL statement per resolve (count statements).
- `LicencePolicyFilterIT`: records, GeoJSON, SSE and tiles under `no-nc`, `no-sa`, `redistributable`; token floor cannot be loosened by a request; `licenceFiltered` counts; `attributions` aggregation with counts; `Link rel=license` headers.
- `LicenceEgressGuardTest`: the full table, private-first rule, `personalUse` exception, UNKNOWN defaults.
- `FetchLogRetentionIT` (extend 019's): rows with versions survive while their versions are retained.
- `CredibilityServiceTest`: each grade rule with fixture corroboration and deception records; `grade` composition; reliability override via settings.
- `SigningKeyServiceIT`: key generated once and encrypted at rest; JWKS shape and thumbprint `kid`; rotation keeps the retired key verifiable.
- `JwsSignerTest`: detached `b64:false` round trip; tampered payload fails; RFC 8037 test vector for the Ed25519 key encoding; `legacyAlg` header.
- `BriefingSignatureIT`: a run carries a signature that verifies against the JWKS.

## Acceptance criteria

- Every registered source resolves licence terms; OpenSky, ACLED and aisstream are `FORBIDDEN` for redistribution and are denied on `SINK_*`, `PUBLIC_SHARE` and `FEDERATION`.
- Record reads return `attributions`; `licencePolicy=no-nc` removes non-commercial records on every read path, SSE and tiles included.
- `GET /.well-known/jwks.json` serves the instance key; a briefing run's detached JWS verifies with a stock JOSE library.
- Operator Guide lists every egress path and the guard table.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `109: Licence And Provenance`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/109-CR-Licence-And-Provenance.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `109: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
