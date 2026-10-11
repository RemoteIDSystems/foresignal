# 097 — Checkpoint Sources

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/097-CR-Checkpoint-Sources.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/096-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- The Completion Reports for 057–096, especially each report's "Tests written (not run)", "Deviations" and "Notes for the next prompt" sections
- `docs/Adapter-Guide.md` (fixture capture, parser contract test, WireMock adapter test, ingest IT, repair procedure)
- `docs/Core-Data-Model.md` §2 (time contract and adapter mapping), §3 (descriptor fields, adapter rules 7–13, limits), §5.1 (policy validation), §5.3 (family defaults), §7 (streaming sinks)
- `docs/Source-Catalog.md` (every row added or changed by 062–096)
- `docs/Gap-Analysis.md` §4a (socket transport, mesh, Remote ID, ACARS privacy notes)

## Objective

Run the whole suite built so far (001–096) and fix what fails before the intelligence block (098 onward) starts building on the adapters. The adapter block added roughly forty sources across polling, streaming, socket and PUSH transports, each with tests that were written but never run. This checkpoint makes them green and verifies that the shared adapter infrastructure (fixtures, family defaults, reconnect ladders, socket transport, the CAP parser, privacy flags) behaves as the design docs require.

## Test run

1. Start Docker if needed (Testcontainers). Run `./gradlew test`.
2. Triage failures by root cause and group them (for example "fixture path convention", "WireMock base-URL override", "descriptor validation", "stream reconnect timing"). Fix production code. Change a test only if it asserts the wrong thing, and document each such change with the reason.
3. Re-run until green. Record pass/fail counts per run and every fix in the Completion Report.
4. Scrutinise these focus areas. Where a required test is missing, write it, run it and fix what it finds:
   1. **Fixture contract tests, every adapter.** Build an `AdapterInventoryTest` that walks `SourceRegistry.all()` (including every family member) and asserts, for every non-DERIVED, non-PUSH source:
      - a fixture directory `src/test/resources/fixtures/<source-id or family-id>/` exists, with a `README.md` naming the capture URL and date;
      - a parser contract test class exists for the adapter package (look up by naming convention `*ParserTest` / `*CodecTest`, or an explicit `@AdapterContract("<id>")` marker; add the marker where conventions differ);
      - the 008 `fieldSpecs` contract holds: every payload key the parser emits for the main fixture is declared in `fieldSpecs` (the check from 011's Adapter Guide);
      - `SnapshotContract.assertStableStaticHash` passes when the fixture is parsed twice and when it is re-parsed with `observedAt` shifted.
      List every gap you close in the report.
   2. **WireMock adapter tests.** Each polling adapter has one covering: the correct URL and params, conditional GET (200 with ETag then 304 → `notModified`) where the upstream supports it, `Retry-After` honoured on 429/503, credential redaction in recorded URLs, and the identifiable User-Agent (never a browser UA). Fix adapters that send credentials in a way the recorder does not redact.
   3. **Ingest ITs.** Every adapter's ingest IT seeds only through the pipeline or `TestRecords` (Core-Data-Model §17: no raw SQL into `record_head`, `record_live` or `record_version`). An identical re-fetch produces 0 versions and touches only; a revised fixture produces the expected `persistReason`.
   4. **Policy defaults per family (§5.3).** A `FamilyDefaultsTest` asserts, per descriptor:
      - aircraft (`adsblol-*`, `airplaneslive-*`, `adsbfi-*`, `opensky-states`): TRACK, SAMPLED 60 s / 240 s, TTL 5 min, 1,000 m DR, `alt_baro` 1,000 and `track` 20°, edgeMin 30 s, aircraft significant fields;
      - `local-adsb-*`: SAMPLED 10 s / 120 s, 300 m, `alt_baro` 500, `track` 10°, edgeMin 5 s;
      - vessels (`aisstream`, `ais-kystverket`, `ais-digitraffic`, `local-ais`): TRACK, SAMPLED 30 s / 1,200 s, TTL 30 min, 250 m, cog 15°, sog 3 kn, edgeMin 15 s, vessel significant fields;
      - transit and rail (`transit-*`, `amtraker`, `digitraffic-rail`): TRACK, SAMPLED 15 s / 240 s, TTL 5 min, 150 m, bearing 30°, edgeMin 8 s;
      - everything else: STANDARD and ALL, and no TRACK descriptor sets `defaultRetention`.
      Run `IngestPolicyValidator` over every descriptor and fail on any error. Check that TRACK SAMPLED budgets fit Core-Data-Model §5.3 (about 10k versions/min) by summing each family's documented expected versions/min from the Completion Reports; record the total.
   5. **Time bases (§2).** Spot-check one adapter per mapping row: readsb `observedAt = now − seen` always (never switching to `seen_pos`), OpenSky `last_contact`, streaming upstream message time, GTFS-RT vehicle timestamp fixed per feed, revised events `observedAt = updated` with origin as `eventTime`, measurements with `ttlAnchor=OBSERVED`. No adapter mixes time fields per record.
   6. **Streaming adapters.** For `aisstream`, `ais-digitraffic`, `emsc` WebSocket, `aprs-is`, `meshtastic-mqtt`, `ais-kystverket` and the socket receivers: the reconnect ladder matches the catalogue (aisstream 5 s → 15 s → 60 s → 300 s → 900 s and an hourly probe after key rejection; APRS-IS 30 s → 15 min with jitter; Meshtastic 15–300 s), the silence watchdog fires, one budget unit is charged per connection attempt, and the sink follows §7 (one outstanding batch, flush at 2 s or 2,000 entries, ALL buffer cap 50,000 with a 30 s block then drop). Use a controllable clock or scheduler, never real sleeps.
   7. **Socket transport (069).** LAN validator rejects public addresses and DNS that resolves to one; UDP listener drops datagrams from senders outside the allowlist; TCP client reconnects; codec SPI framing handles partial and concatenated frames; no listener binds to a non-configured port.
   8. **CAP parser (067, 089).** One shared parser: XXE disabled; `Cancel` closes (tombstones) the referenced alert, `Update` supersedes, `AllClear` resolves; `onset`/`effective` map per §2; polygon and circle areas convert correctly (circle `lat,lon radiusKm`); multi-`info` blocks are handled per the 067 Completion Report.
   9. **Mesh opt-in defaults (083, 084).** `meshtastic-mqtt` is disabled by default, one root topic, `maxSubscriptions ≤ 3`, 100 msg/min cap, keepalive 120 s, client id `foresignal-<installId>`; outbound mesh channels never target the public LongFast channel; no GPL Meshtastic protobuf classes are on the classpath (JSON/MQTT routes only); a test fails if `com.geeksville.mesh` or generated Meshtastic protobuf packages appear.
   10. **Privacy flags.** Every local or personal source (`local-*` receivers with station position, `meshtastic-local`, `meshcore-local`, ACARS/VDL2/HFDL, Remote ID, personal-location sources from 039, `local-aprs` messages) declares the 008 `private` flag or the documented local-only egress default. Assert: private records never appear in `openReads` responses, tiles, embeddings or LLM egress; ACARS content stays metadata-only; Remote ID pilot and home positions are stripped from public paths; observer data needs `include=observer` with agent or admin scope.
   11. **Licence fields.** Every descriptor has `licence` and `termsUrl` (or a documented null), and non-commercial sources (OpenSky, ACLED, IOC, TeleGeography if already present) carry the restriction in `licence`.

**Invariants check.** For every `docs/Invariants.md` entry whose owner prompt is ≤ 096, confirm at least one test asserts it (the test named there, or add one) and list the result in the Completion Report.

## Acceptance criteria

- `./gradlew test` passes.
- `AdapterInventoryTest` and `FamilyDefaultsTest` exist and pass; the report lists every adapter with its fixture, contract test, WireMock test and ingest IT.
- The report records pass/fail counts per run, every fix grouped by root cause, and each test changed with its reason.
- No feature work beyond fixes; compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `097: Checkpoint Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/097-CR-Checkpoint-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `097: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
