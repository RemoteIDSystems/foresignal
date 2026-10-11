# 057 — Checkpoint Weather

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/057-CR-Checkpoint-Weather.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–134).
- **Previous Completion Report:** read `completion-reports/056-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- Completion Reports **042–056** (and the 041 checkpoint report for the baseline pass/fail counts and any deferred failures).
- `docs/Weather-Imagery-Design.md` §2.3 (`.fsg` layout, MRMS codes), §2.4 (storage, ladders, crash safety), §4.3–§4.5 (SatDump readiness, join, georeferencing), §6.1 (cache policy), §6.3 (tiles, render queue), §6.5 (sampling statuses), §7.1–§7.4 (unknown is not clear; required ITs), §8 (attachment caps).
- `docs/Core-Data-Model.md` §10 (SSE), §11 (trigger edges), §13 (retention) for the interaction points.

## Objective

Run every test written in 001–056, fix production code until the suite is green, and scrutinise the weather, imagery and SatDump subsystems where silent errors would mislead operators: wrong pixels, mutable "immutable" URLs, partial files, and stale data reading as an all-clear. No feature work.

## Test run

1. Start Docker if it is not running (Testcontainers needs it). Run `./gradlew test`.
2. Triage failures by root cause and group them (e.g. "GridDef geos inverse off by half a pixel" may fail renderer, sampling and SatDump tests at once). Fix production code. Change a test only when it asserts the wrong thing; document each such change with the reason and the design section that decides it.
3. Re-run until green. Record pass/fail/skip counts for every run and every fix (file, root cause, test names) in the Completion Report.
4. Focus areas. Beyond making tests pass, check each item below; where coverage is missing, add a focused test (allowed in a checkpoint) and fix what it finds:
   - **`.fsg` codec golden tests:** the 64-byte little-endian header matches design §2.3 field by field (magic `FSRG`, version 1, dtype/codec codes, reserved bytes zero, `payloadLength`, CRC32C of the payload); golden files decode byte-identically; a corrupted CRC, wrong magic, unknown version or truncated payload is rejected with a clear error; `sha256` is over decoded content (re-encoding with a different codec keeps the same `sha8`); MRMS u8 round-trip (0 no coverage, 1 no echo, `dBZ = (code − 66)/2`, clamp at 2 and 255) against sentinel fixtures.
   - **FrameStore atomicity:** writes go to `rasters/tmp` and are renamed atomically on the same filesystem; a crash between file write and row insert leaves an orphan file that the sweeper removes; a crash between frame commit and enqueue is repaired by the sweeper's re-emit (frame newer than its record head); retention deletes the row before unlinking and the API then returns 410 `FRAME_EXPIRED`; concurrent readers never see a partial file; `partial` → `complete` transitions keep the frame id and change `sha8` only when content changes.
   - **Tile renderer correctness per GridDef:** `latlon`, `geos` (GOES-R PUG / CGMS formulas, sweep axis `x` for GOES and `y` for Meteosat-style), `lcc` (HRRR, including the 051 wind rotation) and `affine` 4326/3857: known lon/lat → pixel → lon/lat round-trips within half a cell; tiles at the native max zoom edge and across the antimeridian; `@2x` is the same geography at double resolution; out-of-range zoom → 400 `TILE_OUT_OF_RANGE`; empty or out-of-bounds tiles return the shared transparent PNG with one fixed ETag (or 204 with `?empty=204`); nearest vs bilinear per product (categorical never interpolated).
   - **Cache policy and content-addressed URLs:** `immutable` only with `~sha8`, a versioned style `name@v`, a pinned `run` for forecasts and a `complete` frame; everything else short max-age (`min(cadence/2, 60 s)`) plus ETag and `checkNotModified`; `public` only when `openReads` covers the path (also through `/v1/k/{key}/…`); a stale `sha8` → 404 `UNKNOWN_FRAME` with a `Link` to the current key; `?onExpired=transparent` behaviour; TileJSON templates are concrete (aliases resolved); unversioned or unknown styles rejected (`UNKNOWN_STYLE`).
   - **Render queue / `RENDER_BUSY`:** priority (newest frame, interactive, prefetch), per-client fairness (tile key, then token, then IP), single-flight per tile, bounded wait ≤ 1.5 s, then 503 with `Retry-After: 1`; warming does not starve interactive requests; `renderBusyLastHour` and queue depth appear in the `/v1/system` `rasters` block.
   - **Stale vs no-coverage semantics never firing all-clears:** the three design §7.4 ITs pass (GLM stops → no exit alert, section `stale`, system event; unchanged place-weather for 30 min → no versions, no alerts; MRMS no-coverage frame does not clear `radar`); sections keep previous values with fixed `asOf`; generators never tombstone for staleness; verified-clear helper fields evaluate as designed; `999` used for "none within 100 km", `null` never used for a metric; `context.weather`, briefings, Ask and `get_map_image` captions label non-ok sections instead of omitting them.
   - **SatDump fixtures:** `/api` contract fixture for the pinned version, `dataset.json` and `product.cbor` (Jackson CBOR) fixtures, xRIT `partial` → `complete` (one channel validates, then 120 s quiet or next timestamp folder), polar readiness (2 stable scans), pass join window [firstLock − 5 min, LOS + 5 min], externalId `{station}:{satKey}:{aosEpochMinute}`, single writer for `satellite-pass`, name normalisation table, `/fft.jpeg` never requested, polarplot kept in memory only, GeoTIFF reader rejects BigTIFF and oversize images **before** decoding, footprint antimeridian split, Orekit data loads offline, `INBOX_PATH` symlink escapes ignored per file.
   - **Sampling statuses:** `ok | no-coverage | no-data | stale | unavailable` mapped correctly at grid edges, for MRMS code 0, nodata cells, expired frames, failing sources and disabled products; relative times (`-PT1H`, `+PT6H`) pick observation vs forecast frames; `RUN_REQUIRED` for concrete forecast times; `/series` 500-step and `POST /v1/sample` 100-point caps; `/stats` areas correct on `latlon` and `lcc` within 1%.
   - **Attachment caps:** per-channel caps (Discord 8 MB, Telegram 10 MB, ntfy fallback on 413, Pushover 5,242,880 bytes, email inline CID) and the downgrade order 800 px → 600 px → thumbnail → text only; one render per alert reused across channels; map media expires after 7 days; deliveries store media references so retries resend bytes; non-image channels get text plus a link; `get_map_image` ≤ 512 px and ≤ 200 KB.
   - **Also verify:** no client request path performs an upstream fetch (tiles, samples, media, maps, polarplot); `ProductRegistry` reserves `timeline` and `loop`; NetCDF-Java resolves only from the Unidata `exclusiveContent` repository and the startup self-test disables products on decoder failure; `NetcdfFiles.open` only ever receives Foresignal temp paths; per-kind retention resolution order (`retention.kinds` → `retention.sources` → descriptor `kindRetention` → default); one `raster-frame` version per observation frame and per completed forecast run; `/v1/ingest/{id}/media|rasters` never routed to an IngestFormat.

**Invariants check.** For every `docs/Invariants.md` entry whose owner prompt is ≤ 056, confirm at least one test asserts it (the test named there, or add one) and list the result in the Completion Report.

## Acceptance criteria

- `./gradlew test` passes.
- Every focus area above is either covered by a passing test or recorded in the Completion Report as an open issue with its reason.
- No feature work beyond fixes and the focused tests the focus areas require.
- Compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 139).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `057: Checkpoint Weather`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/057-CR-Checkpoint-Weather.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `057: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
