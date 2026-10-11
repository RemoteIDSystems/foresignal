# 143 — Runtime Verification

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/143-CR-Runtime-Verification.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/142-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Product-Charter.md` §14 (success criteria 1–8), NFR-1 to NFR-4, NFR-12
- `docs/Core-Data-Model.md` §15 (storage metrics: `versionsPerMinute`, `persistRatio`, `triggerEdgeShare`, `projectedBytes`, `/v1/system.history`), §16 (compose settings; expected 10–15 GB/day history writes, 15–20 GB/day WAL, 3d track window ≈ 40M rows ≈ 20 GB; NTP), "Per-prompt changes" 062 (measure versions/min, persist ratio, TRIGGER_EDGE share, WAL)
- `docs/Weather-Imagery-Design.md` §2.5 (budgets), §2.6 (memory profiles), §9 (resource budget per product, Postgres growth), §10.3 ("080": re-measure §9 per profile, live MRMS/GOES/GFS/HRRR/SatDump, stormy-day soak, cache tuning from `/v1/system`), §10.4 (items to verify at implementation)
- `docs/Gap-Analysis.md` §8 item D.63 (restore drill, Tailscale profile check, storm scenario, readiness flip during forced catch-up, metric-name drift)
- `docs/Source-Catalog.md` notes (adsb.lol HTTP 451 to non-browser UAs: mark failing, never spoof; OpenSky credit headers; CelesTrak 403 "not updated")
- `docs/Operator-Guide.md` (profiles, backups, remote access), `scripts/smoke.sh`, `postman/`, the 142 Completion Report

## Objective

Run Foresignal for real on the bench server and prove the Charter's success criteria with live upstreams, real channels and a local model. Measure ingest, storage, WAL and weather resource use against the design projections, rehearse failure and recovery (restore, forced catch-up, storms), check the remote-access profile and metric names, and leave a 7-day soak running with a checklist. Fix defects found, with tests. Write the results to `docs/Runtime-Verification.md`.

## Tasks

### 1. Environment and profiles

- On the bench server (Linux amd64; also record CPU, RAM, disk type, Docker and compose versions), with NTP synced first (`timedatectl`; Core-Data-Model §16 clock guard).
- Bring up `docker compose up -d` (default), then add profiles one at a time and record startup time to ready (NFR-4 ≤ 20 s after Postgres is reachable) and steady RSS per container:
  - `local-llm` (Ollama; pull a small tool-capable model, e.g. `qwen3:8b` or the smallest current model that passes the Ask tool loop, and record the choice)
  - `satdump` (with the owner's station if present; else verify the container, the inbox mount and the read-only bind with a recorded sample dataset)
  - `web` (Caddy `tls internal`, HTTP/2/3)
  - `tailscale` (task 7)
- Use the small and large memory profiles (Weather §2.6) for one hour each and record the heap/RSS envelope against NFR-1.
- Credentials the run needs (Slack webhook, AISStream key, FIRMS key, Tailscale auth key, ntfy topic) come from environment variables the owner supplies (`FORESIGNAL_VERIFY_*`). Missing ones mark the dependent step **SKIPPED (no credential)**; never invent or reuse other credentials.

### 2. Enable core sources live and verify etiquette

- Enable through the API: `usgs-earthquakes`, `adsblol-mil` plus one adsb.lol regional or global source (062), `opensky-states` (anonymous or OAuth), `aisstream` (if keyed), `celestrak-gp`, `launch-library`, `nasa-firms` (if keyed), `gdelt-events`, `nws-alerts`, `nhc-cyclones`, `gdacs`, `ioda-outages`, the weather v1 set (`mrms`, `goes-east-abi`, `goes-glm`, `goes-fdc`, `gfs`, `hrrr`, `mrms-probsevere`, `spc-outlooks`, `awc-metars`) and the owner's local receivers if present.
- For 24 h, verify per source:
  - no budget overrun (`/v1/sources/{id}/usage` vs descriptor; Charter G2)
  - `Retry-After` honoured (look for 429/503 in `fetch_log`)
  - conditional GET producing 304s where supported
  - the identifiable User-Agent in a sampled request (a capture via a local logging proxy on one keyless source)
  - health statuses sensible
- Known-risk upstreams:
  - adsb.lol **451** → the source must show `failing` with a clear reason. Record it; do not work around it.
  - CelesTrak 403 "not updated" treated as not-modified.
  - OpenSky `x-rate-limit-remaining` reserve kept.

### 3. Measurements (record method, raw numbers and the projection for each)

- **Ingest:** observations/min and versions/min per source and total (from `source_usage` and health `storage`); `persistRatio`; `triggerEdgeShare` (warn > 5%); late and clamped per hour; ChangeBus lag; SSE latency commit → client (NFR-3 ≤ 2 s) using a timestamped test client.
- **Storage:** `/v1/system.history` per tier every 6 h (partitions, rows, bytes, headroom); `pg_total_relation_size` of `record_head`/`record_live` (and bloat via `pgstattuple` if available); `record_live` HOT-update ratio (`pg_stat_user_tables.n_tup_hot_upd / n_tup_upd`; expect high, Core-Data-Model §8); daily bytes → 3d/7d projections versus §16 (10–15 GB/day history, ≈ 20 GB for a 3d track window).
- **WAL:** `pg_stat_wal.wal_bytes` delta per hour (or `pg_current_wal_lsn()` diff); checkpoint frequency (`pg_stat_bgwriter`/`pg_stat_checkpointer`) versus the 15–20 GB/day projection and `max_wal_size=8GB`.
- **Weather (§9 per profile):** download GB/day per product, decode CPU (process CPU time deltas), rolling raster disk, render queue depth and `RENDER_BUSY` count while a client animates `/loop`; cache hit ratios from `/v1/system`. Tune cache sizes from the data and record the changes.
- **Retention:** one full RetentionJob cycle: TRACK leaf drop duration, STANDARD chunk delete throughput, head purges, `seq_clock` pruning, partition headroom after.
- Provide the queries as `scripts/verify/measure.sql` and a wrapper `scripts/verify/measure.sh` (psql through `docker compose exec db`) so the owner can re-run them.

### 4. Features end to end (Charter §14)

- **Triggers and channels:**
  - `mag >= 6` (`enter`) and a geofence rule; a low threshold (e.g. `mag >= 2.5`) to force live alerts within the window
  - delivery to SSE, a real ntfy topic (ntfy.sh or self-hosted), Slack (if the webhook is given), Mattermost (if given), Web Push via a UnifiedPush endpoint on ntfy (031), and email to a local Mailpit container
  - measure commit → channel latency (NFR-12 ≤ 10 s) and enrichment with the local model
- **Briefings:** a daily digest and an LLM briefing on the local model, delivered to ntfy; check the detached JWS verifies (109).
- **Ask:** five fixed questions over live data with the local model; every citation verified; record answer latency and tokens.
- **MCP:** connect with an `agent` token from an MCP client; query data and create a watch; confirm `update_source`-type and credential operations are refused (success criterion 7).
- **Push:** UnifiedPush delivery as above; APNs/FCM only if the owner supplies keys (else SKIPPED).
- **Custom source:** push records from a script through the inbound webhook and trigger an alert (criterion 8).
- **Health and doctor:** simulate a format change with the 140 test hook (point one keyless source at a local stub serving a renamed field) → `degraded` plus an automatic doctor report naming the field (criterion 5).
- Spot checks: OGC API Features in QGIS, CAP Atom, an iCalendar subscription, a GeoParquet export opened in DuckDB, `aircraft.json` read by tar1090 (whichever the owner can run), and a sink never emitting OpenSky/aisstream data.

### 5. Failure and recovery drills

- **Readiness flip during forced catch-up:**
  - Pause trigger evaluation using the mechanism 019 provides (read its Completion Report; if none, stop the engine via its setting), let ingest run 10 min, then resume.
  - Verify `/actuator/health/readiness` reports catch-up as not ready, then flips back to ready when the cursor is within threshold, while liveness stays UP and the compose healthcheck behaves as documented.
  - Record the timings.
- **Restore drill** (on a scratch compose project, `-p foresignal-restore`, never the live volumes):
  - take the documented backup (backup profile or `pg_dump` plus `secret.key`)
  - restore into fresh volumes and start
  - verify row counts per table, `record_seq` ≥ max(seq), partitions attached and `ensureAhead` coverage, credentials decrypt, the signing key and JWKS unchanged, triggers fire on new data, SSE resume from a pre-backup `after` gets `reset` or continues correctly
  - record the duration and the steps, and update the Operator Guide if any step was wrong
- **Storm scenario:**
  - Run when the SPC Day-1 outlook has Slight risk or higher over the operator's area. Soak 12 h covering the event: radar cells, ProbSevere, GLM clusters, NWS alerts, place-weather versions, render load, versions/min peaks, alert storm control (`max_alerts_per_hour`, folding).
  - If no such day occurs during this prompt, run the reduced variant (replay a retained stormy window with the 115 exercise replay against opted-in test channels) and list the live stormy-day soak as pending in the checklist with the exact procedure.
- **Restart under load:** `docker compose restart foresignal` during peak ingest; verify no duplicate versions, the clock guard behaviour, the streaming reconnect ladder, and no budget burst after restart (seeded buckets).

### 5a. Capacity, disk-full and the intelligence layer

- **Disk-full drill:** on a dedicated small volume (or a filled test volume), drive the database volume to 95% and then 100%: confirm the readiness probe and `/v1/system` warnings fire before the volume is full, the writer stops cleanly (no partial batches, no corrupted partitions), and recovery after freeing space needs no manual SQL. Document the procedure and the observed thresholds.
- **Practical upper bounds:** from the measured persist ratios and versions/min, state in `docs/Operator-Guide.md` the realistic limits for this hardware (sources, dense TRACK feeds, retention years) and the disk needed for 1, 3 and 5 years with the analytic retention defaults.
- **Intelligence jobs:** run one daily, weekly and monthly cycle (128), the 123 synthetic and historical benchmark suites, precursor mining (120) and base-rate refresh (121) on live data; record wall time, peak heap, database load and token spend for each; confirm none blocks ingest (writer latency unchanged within 10%).

### 6. API conformance runs

- `scripts/smoke.sh` against the live instance; record the output.
- Newman against a **scratch instance** (`-p foresignal-verify`, fresh DB) so destructive requests don't touch live data: `npx newman run postman/Foresignal.postman_collection.json -e postman/Foresignal-Local.postman_environment.json --env-var adminToken=$TOKEN --reporters cli,junit`. Fix spec, collection or code mismatches; commit the regenerated collection if changed.

### 7. Remote access and metric names

- **Tailscale profile:** with `FORESIGNAL_VERIFY_TS_AUTHKEY`, start the profile. Check HTTPS via `tailscale serve`, `publicBaseUrl` https and no warning, forwarded headers honoured only from the trusted proxy, generated links (push actions, share links) using the tailnet URL, 8080 and 5433 still bound to 127.0.0.1 (`ss -ltnp`), and a phone pairing flow over the tailnet (023).
- **Metric-name drift:** `scripts/verify/metric-drift.sh` scrapes `/actuator/prometheus` and diffs metric names against those referenced in `docs/` (Operator Guide tables, Architecture) and the Grafana dashboards from 034. Fix the docs or the code so the sets match (excluding JVM/Tomcat defaults); keep the script for CI use in 144.

### 8. Report and soak checklist

- Write `docs/Runtime-Verification.md`:
  - environment
  - a table per measurement with the projection, measured value and verdict
  - per-source etiquette results
  - feature checks (pass/fail/skipped with reason)
  - drills with timings
  - tuning applied
  - defects found and fixed (each with its regression test)
  - open risks
- **7-day soak checklist** (a section in the same file) for the owner, with a daily row: budgets within limits, no source stuck failing without reason, daily briefing delivered (digest, LLM, local model), storage growth versus projection, WAL and checkpoints, partition headroom, retention ran, backups succeeded, alert latency sample, memory. Start the soak at the end of this prompt and record the start time.
- Update `docs/Operator-Guide.md` with corrected sizing (disk, RAM, bandwidth per profile) from the measurements.

## Tests to write (do not run)

- Regression tests for every defect fixed in this prompt, in the matching test class.
- `MetricNamesDocumentedTest`: every `foresignal.*` meter registered at startup appears in the Operator Guide metrics table (parses the markdown), so 7's drift check also runs in the suite.
- **Sanctioned run:** this is a verification prompt. If any production code changed, run `./gradlew test jacocoTestReport jacocoTestCoverageVerification` once before the final commit, fix until green, and record the counts and coverage in the Completion Report.

## Acceptance criteria

- `docs/Runtime-Verification.md` exists, with measured versus projected values for ingest, storage, WAL and weather, and every Charter §14 criterion marked pass, fail (with fix) or skipped (with reason).
- No source exceeded its budget during the run; adsb.lol or other blocks are reported, not worked around.
- Restore drill and readiness-flip drill completed and documented; smoke and Newman pass against scratch and live instances.
- The 7-day soak is running with its checklist.
- Compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `143: Runtime Verification`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/143-CR-Runtime-Verification.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `143: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
