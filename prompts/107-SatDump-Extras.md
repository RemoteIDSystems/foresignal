# 107 — SatDump Extras

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/107-CR-SatDump-Extras.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/106-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md`:
  - §10.3 row "075" (this prompt; its 075 = 107), §5.3 "SatDump extras"
  - §3 licence boundaries (SatDump GPL-3.0: files and HTTP only, no SatDump code or structs; an Foresignal hook is MIT, a separate file, never linked)
  - §4.0–§4.7 (station requirements, inbox loop, NOFOLLOW/realpath, pass join, `/api` drift fixtures), §2.2 (`LAN_URL`, `INBOX_PATH`), §11 (DCS/EMWIN deferred here)
  - Translate design numbers with `docs/Roadmap.md` §5: its 036 = 049, 041 = 054, 044 `celestrak-gp` = 064, and 035 = 048
- `docs/Gap-Analysis.md` §2 row "SatDump / Gpredict / Look4Sat" (CelesTrak-compatible `gp.php`; unknown `FORMAT` defaults to CSV)
- Completion Reports of 049 (`satdump-{station}` source, inbox loop, `adapter_state`, health reasons), 054 (polar overlays, GeoTIFF reader, remote ingest), 048 (GLM decoder, ring, `lightning-cluster`), 047 (`nws-alerts`, VTEC handling), 064 (`celestrak-gp` cache and `/compat/celestrak/gp.php`), 035 (feed keys), 036 (places AOI), 067 (shared CAP parser)
- SatDump pipeline options: `https://docs.satdump.org/md_docs_2pages_2Pipelines.html` (verified 2026-10: the GOES-R HRIT pipeline has `write_emwin` (default on), `parse_dcs` (default off, saves DCS as JSON), `write_dcs`, `tracked_addresses` and `write_messages`)
- SatDump releases: `https://github.com/SatDump/SatDump/releases` (verified 2026-10: the latest stable release is **1.2.2**; 2.0 exists only as alpha and nightly builds)

## Objective

Finish the ground-station integration:
- filtered DCS (environmental data platforms) and EMWIN products from the owner's GOES HRIT reception;
- an EMWIN warnings fallback that keeps alerts flowing when the internet is down;
- `glm-local` lightning from a GRB station;
- a SatDump-friendly orbital-elements feed served from Foresignal's CelesTrak cache;
- the SatDump 2.0 hook, gated on 2.0 actually being in use.

All of it stays on the far side of the GPL boundary: Foresignal reads files and HTTP only.

## Tasks

### 1. DCS messages (extend `satdump-{station}`)

- Params: `dcsEnabled` (default false), `dcsBbox` (default the places-AOI envelope, **mandatory**: enabling DCS without an effective bbox is a 400), `dcsAddresses` (optional string-list allowlist), `dcsAgencies` (optional), `dcsMaxPerScan` (default 500).
- **Input:** SatDump `parse_dcs` JSON files in the station output tree.
  - Locate them under the pass/run folder by glob `dcsGlob` (default `**/DCS/**/*.json`). Verify the actual layout on the owner's version and record it; keep a fixture per pinned version.
  - Parse leniently: DCP address, message time, channel, signal strength, frequency offset, data (bytes/ASCII), failure code. Unknown keys are ignored.
- **Location:** messages carry a DCP address, not a position. Resolve through a `dcp_location` table (address PK, lat, lon, name, agency, source, updated_at), loaded from:
  1. an operator CSV at `${FORESIGNAL_INBOX_DIR}/dcs/dcp_locations.csv` (`address,lat,lon,name,agency`), re-read on change;
  2. optionally, the NOAA DCS Platform Description Table export if a public, redistributable file is confirmed. Verify the URL and terms; if they are unclear, do not fetch it, and document the CSV path only.
  - **Messages without a location are dropped** and counted (`foresignal.satdump.dcs.dropped{reason=no_location|outside_bbox|not_allowlisted}`).
- **Records:**
  - kind `dcp-observation`, externalId `{address}`;
  - `observedAt` = message time; `eventTime` = message time;
  - payload: address, name, agency, channel, signalDbm (1 dp), `dataAscii` (≤ 1 KB, printable only) or `dataHex` (≤ 2 KB), `decoded` (null; per-agency decoders are out of scope);
  - **emit on change** (STANDARD; `significantFields` address, data, failure code; `volatileFields` signal fields);
  - kind retention 3 d (`kindRetention`).
- Per-scan cap `dcsMaxPerScan`. Over the cap, keep the newest per address, then degrade health with "DCS volume over cap: n dropped; tighten dcsBbox or dcsAddresses".
- Operator docs:
  - enable `parse_dcs` and set `tracked_addresses` in SatDump to reduce volume at the source;
  - the hemisphere-wide volume warning from design §11.

### 2. EMWIN products and the warnings fallback

- Params: `emwinEnabled` (default false), `emwinGlob` (default `**/EMWIN/**/*`; verify the layout), `emwinProducts` (default `TOR,SVR,FFW,SVS,SMW,EWW,SQW,FFS,MWS,SPS`; AWIPS product categories by the first 3 letters of the AWIPS id), `emwinWarnFallback` (default true).
- **Parsing (`EmwinTextParser`):**
  - The WMO heading (`TTAAii CCCC DDHHMM`) and AWIPS id line.
  - Product text bodies (ZIP-compressed EMWIN files: unzip with entry-count ≤ 16 and size ≤ 2 MB guards; reject path traversal).
  - P-VTEC lines `/O.NEW.KFWD.TO.W.0042.261010T2130Z-261010T2215Z/`.
  - `LAT...LON` polygons (hundredths of a degree, west longitudes positive in the text → negate).
  - `TIME...MOT...LOC`.
  - Text is untrusted (no HTML, length-capped).
- **Records:**
  - kind `emwin-product` (all allowed categories): externalId `{wmoHeading}:{awipsId}:{issue}`, payload heading, awipsId, office, issuedAt, `text` (≤ 16 KB). Retention 3 d. Embedded (027) via `embedFields: [text]`.
  - kind `emwin-warning` when a P-VTEC line and polygon are present:
    - externalId `{office}.{phen}.{sig}.{etn}` (event tracking number, so updates to the same warning version the same record);
    - payload: action, phenomena, significance, office, etn, `validFrom`/`validUntil` (from VTEC, explicit), headline, `fallback: true`, `authoritativeSource: "nws-alerts"`;
    - `CAN`/`EXP` actions → tombstone;
    - filtered to warnings intersecting the places AOI.
- **Fallback semantics:**
  - **When `nws-alerts` is healthy**, `emwin-warning` records are still written, but the payload gets `duplicateOf: {source: "nws-alerts", externalId}` when the matching VTEC exists there. The docs recommend rules that filter `duplicateOf` absent.
  - **When `nws-alerts` is failing or stale** (health), records carry no `duplicateOf`, and the built-in rule recipe "Warnings via satellite (internet down)" fires.
  - Add that recipe to the Operator Guide: `kind=emwin-warning`, condition "`duplicateOf` does not exist" (the 017 `exists` operator, negated), geofence or `near` home, `enter`.
  - Never present EMWIN as more authoritative than NWS. Messages render "Received via GOES HRIT EMWIN".

### 3. `glm-local` (GRB station)

- New source family `glm-local-{station}` (POLLING inbox, `INBOX_PATH`).
  - It reads GLM L2 LCFA NetCDF files written by a SatDump GOES GRB pipeline (1686.6 MHz; needs a larger dish than HRIT, so document hardware). Verify the file naming and NetCDF variables against a fixture; they should match NODD `GLM-L2-LCFA`.
- Decode with the 048 GLM decoder. Feed flashes into the same AOI ring tagged `origin: local`, de-duplicated against internet `goes-glm` flashes by (flash time ± 1 s, position ± 0.05°).
- `lightning-cluster` generation is unchanged. The ring is the single input, so clusters keep working when internet GLM fails.
- Health:
  - degraded when no file arrives for 5 min while the station `ground-station` state is `locked` or `receiving`;
  - idle otherwise (`zeroResultsNormal=true`).

### 4. SatDump-compatible elements feed

- Check that 064 delivered `/compat/celestrak/gp.php` (CelesTrak query grammar: `GROUP`, `CATNR`, `NAME`, `FORMAT=tle|3le|2le|xml|kvn|json|csv`; unknown `FORMAT` → CSV). Add it if missing.
- **Add:**
  - path aliases SatDump and other tools use: `/compat/celestrak/NORAD/elements/gp.php` and `/compat/celestrak/NORAD/elements/{group}.txt` (TLE). Groups at least `weather`, `noaa`, `goes`, `resource`, `amateur`, `cubesat`, `active`, `stations`;
  - **offline behaviour:** always serve from the cache; add `X-Foresignal-Elements-Age: <seconds>` and a warning header when older than 3 d. Never fetch upstream on request.
  - **access:** SatDump cannot send auth headers, so document the feed-key form `/v1/k/{key}/compat/celestrak/...` (035) or `openReads`.
- Operator docs:
  - pointing SatDump's TLE update URLs at Foresignal, via the settings UI or config file, verifying the exact config key on the owner's version and recording it (Gap-Analysis notes recent SatDump builds fetch from orbicentral by default);
  - Gpredict and Look4Sat equivalents.
- Tests: format matrix, aliases, stale header, feed-key access.

### 5. SatDump 2.0 hook (gated)

- **Gate:** active only when `foresignal.satdump.version` (a new setting with values `1.2` | `2.0`, from the owner's §1.5 answer) is `2.0`, or when a 2.0 stable release exists. Verified 2026-10: it does not; 1.2.2 is the latest stable.
- **Always implement the Foresignal side, version-agnostic.** The inbox loop recognises an `foresignal-manifest.json` in a pass folder: `{schema: 1, satellite, norad?, timestamp, products[{name, instrument, file, projection: "equirec", bounds[w,s,e,n], composite}]}`. The listed GeoTIFFs and PNGs go through the 054 pipeline (ImageGuard, GeoTIFF reader). The manifest is validated by a JSON schema; unknown versions are rejected with a health reason.
- **Ship the hook as a separate MIT file**, `contrib/satdump/foresignal_hook.as` (AngelScript), with an MIT header and a README:
  - it runs on product finish, writes equirectangular composites and the manifest, and never includes SatDump headers or copies SatDump code;
  - it is marked **experimental** and needs `PLUGIN_SCRIPTING` (design §4.0).
  - Foresignal images do not contain it; the operator copies it.
  - If the AngelScript API cannot be verified against a 2.0 build, ship the README and the manifest specification only, and record that in the Completion Report.
- Fixtures: a synthetic manifest pass folder; a contract test for the manifest schema.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: the `/compat/celestrak/**` aliases (scope read, `x-foresignal-scope: read`, `text/plain`/`application/json`/`application/xml`); kind schemas `dcp-observation`, `emwin-product`, `emwin-warning`; source param docs. Regenerate Postman.
- AccessPolicy: `/compat/celestrak/**` GET → READ (works under openReads and feed keys).
- `docs/Source-Catalog.md`: rows for DCS/EMWIN (via `satdump-{station}`) and `glm-local-{station}`, with the verification notes above.
- `docs/Operator-Guide.md` "SatDump extras": SatDump options to enable, `dcp_locations.csv`, volume warnings, the EMWIN fallback recipe, GRB hardware, the TLE feed setup, and the 2.0 hook status.
- `THIRD_PARTY_NOTICES.md`: confirm the SatDump GPL boundary note (no SatDump code included).

## Tests to write (do not run)

- `DcsJsonParserContractTest` (fixture per pinned SatDump version), `DcpLocationLoaderTest` (CSV changes, invalid rows), `DcsFilterTest` (bbox, allowlist, missing location dropped with metrics, per-scan cap and health).
- `EmwinTextParserTest`: WMO/AWIPS headers, P-VTEC parse, `LAT...LON` sign handling, ZIP guards (entry count, size, traversal), untrusted text caps.
- `EmwinFallbackIT`: with `nws-alerts` healthy, the record carries `duplicateOf`; with it failing, no `duplicateOf` and the recipe rule fires; CAN → tombstone.
- `GlmLocalAdapterTest`: decode fixture, dedup against internet flashes, health when locked with no files.
- `CelestrakCompatIT`: aliases, format matrix, unknown FORMAT → CSV, stale header, feed-key access, no upstream call on request.
- `ForesignalManifestTest`: schema validation, products routed through ImageGuard, unknown schema rejected.
- Ingest ITs for `dcp-observation` (emit only on change) and `emwin-warning` (versioned by ETN).

## Acceptance criteria

- DCS ingestion cannot be enabled without a bbox. Messages without a location or outside the filters are dropped and counted, and volume above the cap degrades health.
- EMWIN warnings are versioned by VTEC ETN and are marked as duplicates of `nws-alerts` while it is healthy. The fallback rule fires only when it is not.
- `glm-local` feeds the shared GLM ring with dedup. The elements feed is served from cache only.
- The SatDump 2.0 hook is gated, MIT, separate, and not bundled. The manifest reader works independently of the SatDump version.
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
2. **Commit** all changes with the message `107: SatDump Extras`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/107-CR-SatDump-Extras.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `107: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
