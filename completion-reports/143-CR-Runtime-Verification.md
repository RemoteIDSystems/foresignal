# 143-CR-Runtime-Verification

| | |
|---|---|
| Prompt | 143-Runtime-Verification.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: **no runtime verification performed**) |
| Implementation commit | `bd8e878fbf5e3d61e685c300d37397adb2f263a4` |
| Date | 2026-10-18 |

## Summary

**No runtime verification was performed.** Under the owner directive of 2026-10-17 (the override banner at the top
of `prompts/143-*.md` and the "Runtime verification deferred" rule in `prompts/000-Run-Sequence.md`), nothing ran:

- the application was not started, and no compose profile was brought up;
- no source was enabled and nothing was measured;
- no drill, soak, storm run, capacity test, smoke run or Newman run took place;
- no test was run, including the new one and the US English guard;
- none of the scripts written here was executed. `sh -n` parsed the two shell scripts without running them.

`./gradlew compileJava compileTestJava` succeeds on the committed tree: exit code 0.

What the prompt specifies as code, scripts and documents was built so the owner can run the verification:

| Path | Change |
|---|---|
| `scripts/verify/measure.sql` | New: read-only queries for ingest (observations/min, versions/min, `persistRatio`, `triggerEdgeShare`, late and clamped), persist reasons, fetch etiquette (304, 429, 503, 451, 403 and the gap after each 429/503), storage, HOT update ratio, partitions, `record_seq` against max(seq), `seq_clock`, optional `pgstattuple` bloat, WAL (`pg_stat_wal`) and checkpoints (`pg_stat_checkpointer`, PostgreSQL 17) |
| `scripts/verify/measure.sh` | New: runs the SQL through `docker compose exec -T db psql`. `report [HOURS]` saves a timestamped file. `wal-sample` appends to a CSV and prints the WAL GB and checkpoints since the last sample, with a GB/day projection. Honors `COMPOSE_PROJECT` for the scratch restore project |
| `scripts/verify/metric-drift.sh` | New: compares the `foresignal_*` series on `/actuator/prometheus` (or `SCRAPE_FILE`) and the Grafana dashboard references against `docs/Metrics.md`. Exits 1 on drift. Lists lazily registered meters for information only. Ready for the CI job in 144 |
| `docs/Metrics.md` | New: the 122 `foresignal.*` meter names found in `src/main/java`, with the emitting class and what each measures |
| `src/test/java/ai/foresignal/system/MetricNamesDocumentedTest.java` | New, **not run**: each `foresignal.*` meter registered at startup has a row, and each row names a meter that still exists as a literal in the main sources |
| `docs/Runtime-Verification.md` | New: environment, profile, measurement and drill tables, each with its projection and an empty "Measured" cell; every Charter §14 criterion; drill procedures; the 7-day soak checklist |

All scripts are POSIX `sh` and need only Docker Compose, `curl`, `awk`, `grep`, `sed`, `sort`, `comm` and
`mktemp`. Nothing assumes macOS tools or paths.

## How the meter list was built

- `grep -rnoE "\"foresignal\.[A-Za-z0-9_.-]+\"" src/main/java` printed 307 literals.
- I removed property prefixes, `foresignal.test.*` adapter URLs, logger names, the advisory lock name, a file
  name and Parquet metadata keys, then checked each remaining line. 122 meter names were left.
- `grep` found no `MeterFilter`, `@Timed`, `@Counted` or `MeterBinder` in `src/main/java`. No meter is renamed
  or registered by annotation.
- The 15 `foresignal_*` names in the three Grafana dashboards all expand from documented rows.

## Deviations from the prompt

1. **Nothing was run** (owner directive). None of these was done:
   - the environment and profile measurements;
   - the 24-hour etiquette check;
   - the ingest, storage, WAL and weather measurements;
   - the feature checks, drills and capacity runs;
   - smoke and Newman;
   - the Tailscale check;
   - starting the soak.

   The sanctioned `./gradlew test jacocoTestReport jacocoTestCoverageVerification` run was not done either. No
   production code changed, so the prompt's condition for it was not met anyway.
2. **The metrics table is in a new document, `docs/Metrics.md`, not in the Operator Guide.** The Operator Guide
   has no metrics table. `grep -n "| \`foresignal\." docs/Operator-Guide.md` printed nothing. Documents are
   replaced whole, not patched, and the guide is 9,453 lines. `MetricNamesDocumentedTest` and
   `metric-drift.sh` both read `docs/Metrics.md`. Whether to move the table into the guide is for the owner.
3. **The Operator Guide was not updated.** Its sizing and practical limits come from measurements that do not
   exist yet.
4. **No defects fixed and no regression tests.** Those come from the run.
5. **`foresignal.licence.egress.denied` keeps its British spelling.** It is a Prometheus name that crosses the
   wire, so it is reported here and not changed. The US English guard may flag it in `docs/Metrics.md`. The
   guard was not run.

## Owner: runbook, in order

Write each result into `docs/Runtime-Verification.md`.

1. **Environment.** Section 1. Check `timedatectl` first, then record CPU, RAM, disk, Docker and Compose.
2. **Profiles.** `docker compose up -d`, then `local-llm`, `satdump`, `web` and `tailscale`, one at a time.
   - Record time to ready (NFR-4: ≤ 20 s after `pg_isready`) and `docker stats --no-stream`.
   - Run `FORESIGNAL_PROFILE=small` and then `large` for one hour each. Look for the heap
     (`jvm_memory_used_bytes{area="heap"}`) and RSS staying within §2.6, and NFR-1's 512 MB heap when idle.
3. **Sources for 24 h.** Enable the list in section 3.
   - Run `scripts/verify/measure.sh report 24`, and `measure.sh wal-sample` hourly from cron.
   - **Look for:** no `refused` caused by an overrun; a long gap after every 429/503 (section 2b); 304s on
     conditional sources; adsb.lol `failing` with a 451 reason (not worked around); CelesTrak 403 counted as
     not-modified; the OpenSky reserve kept.
4. **Measurements.** Compare against section 4.
   - versions/min against 20,000; `triggerEdgeShare` ≤ 5%;
   - history 10–15 GB/day; WAL 15–20 GB/day;
   - requested checkpoints near 0 (otherwise `max_wal_size=8GB` is too small);
   - a high `record_live` HOT ratio;
   - weather 5–8.5 GB/day download and 10–12 GB rolling disk.
5. **Metric names.** Run `FORESIGNAL_TOKEN=… scripts/verify/metric-drift.sh`. It must print "No drift."
6. **Features.** Section 6.
   - Triggers to SSE, ntfy, Web Push and Mailpit, plus Slack or Mattermost if keyed; NFR-12 ≤ 10 s.
   - Briefings, checking the detached JWS against `/v1/system/jwks`.
   - Five Ask questions with citations.
   - MCP with an `agent` token: the watch is created, and source and credential changes are refused.
   - Inbound webhook to an alert.
7. **Format-change drill.** Point `foresignal.test.adapters.usgs.base-url` at a stub that renames a field.
   Expect `degraded` and a doctor report naming the field.
8. **Restore drill** on `-p foresignal-restore`. Check per section 7: row counts, `record_seq` ≥ max(seq),
   partitions, credential decryption, an unchanged JWKS, triggers on new data, and the SSE resume.
9. **Restart under load, the storm (or the 115 replay variant), disk full, and the intelligence jobs.**
   Sections 7 and 8.
10. **Smoke and Newman.** `scripts/smoke.sh` on the live instance. Newman on a scratch `-p foresignal-verify`
    instance only.
11. **Start the 7-day soak.** Record the start time and fill in one row of section 11 each day.
12. **At code-complete,** also run `MetricNamesDocumentedTest` with the suite.

## Issues found / open questions

**Owner decisions:**

- **(a) Readiness during catch-up.** The prompt expects readiness to go not-ready during a forced catch-up. As
  built, catch-up is the separate `/actuator/health/catchup` group. The Operator Guide (line 5344) calls it
  "deliberately not part of readiness", and `HealthProbesIT` asserts that independence. The prompt and the code
  disagree: which one should change?
- **(b) No trigger-evaluation pause exists** for that drill. `grep -rln -i "pause" src/main/java/ai/foresignal/trigger`
  printed nothing, and I found no engine on/off setting. The runbook forces catch-up through ingest load instead.
  Should a pause be added?
- **(c) Where the metrics table lives:** `docs/Metrics.md`, or moved into the Operator Guide.

**Still open** from 139 to 142: the decisions listed in those reports.

No upstream was fetched and no dependency was added. No file or tool output contained instructions directed at the
agent.

## Compile result

`./gradlew compileJava compileTestJava` on the committed tree: exit code 0, `BUILD SUCCESSFUL`.

## Notes for the next prompt

- **144 (Flyway, CI):** prompt 144 adds the `.gitlab-ci.yml` job for `scripts/verify/metric-drift.sh`.
  - It needs either a started instance and an admin token, or a saved scrape through `SCRAPE_FILE`.
  - This prompt added no schema change.
  - 142's note about `backtest_job.worker_instance` still stands.
- **At code-complete:** run the runbook above after the test suite.

No runtime verification was performed (owner directive 2026-10-17). Tests were written but **not run**.

Implementation commit: `bd8e878fbf5e3d61e685c300d37397adb2f263a4`
