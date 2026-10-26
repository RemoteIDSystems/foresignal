# R2-014 — Disk & resource monitoring + operational alerting

**Context.** v2.0 (R2) backend. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle, base package
`ai.foresignal`. Identity/RBAC and the per-user substrate are built by **R2-001…R2-013** (users, roles,
the permission catalog, sessions, per-user alerts/channels). This prompt is the first of Part B
(storage, lifecycle). It depends on the permission catalog (`storage.manage`, `audit.read`) seeded in
R2-001 and gated by R2-004, the per-user channels of R2-009, and the existing channel/notification
system (prompt 018).

**Goal.** Give the instance eyes on its own host. Today `SystemInfoService` reports the database size and
the raster/media byte quota raises `/v1/system` warnings (prompt 042), but **nothing watches host disk
free space or memory**, and nothing turns a resource breach into an alert. Add a host-resource monitor
(disk / memory / DB), an admin **threshold** control (warn + critical), and make a breach surface **both**
as a `/v1/system` warning **and** as an alert through the channel system. Then generalize the same
alerting path so source-failure storms and collector backlog can raise operational alerts the same way.

## Operations (already authored, status `planned`, `x-foresignal-prompt: R2-014`)

| operationId | method + path | scope |
|---|---|---|
| `getResourceStatus` | `GET /v1/system/resources` | admin · `storage.manage` |
| `getResourceThresholds` | `GET /v1/settings/thresholds` | admin · `storage.manage` |
| `updateResourceThresholds` | `PUT /v1/settings/thresholds` | admin · `storage.manage` |

Schemas already in `docs/OpenAPI.json`: `ResourceStatus`, `ResourceGauge`, `ResourceThresholds`,
`ResourceThreshold`. Implement to them exactly.

## Read first

- `system/SystemInfoService.java` (`pg_database_size`, the `/v1/system` document), `system/SystemWarnings.java`
  + `system/SystemWarningContributor.java` (the SPI — a `warning`/`critical` warning degrades status),
  `system/OperatorGauges.java` (the 30 s cached-gauge pattern), `system/CanaryService.java` (how it fires
  `notify/SystemEvent`).
- `notify/SystemEvent.java`, `notify/NotificationService.java`, `notify/NotificationRouter.java`,
  `notify/ChannelService.java` (how a `system` event fans out to channels), and the per-user channel wiring
  added in R2-009.
- `settings/AppSettingService.java`, `settings/SettingKeys.java`, `settings/WatchdogSettingsService.java`
  (model a typed settings service on this), `api/SettingsController.java` (the `/v1/settings/**` admin surface).
- `collect/HealthTracker.java` + `collect/SourceHealthChangedEvent.java` (source health transitions),
  `collect/CatchUpMonitor.java` / `collect/PullGovernor.java` (collector backlog signals), `raster/RasterProperties.java`
  / `media` store roots (the second on-disk location besides the DB data dir).

## Build

1. **Host-resource monitor** (`ai.foresignal.system`). A `ResourceMonitor` service that reads, on a ~30 s
   cache like `OperatorGauges`:
   - **Disk** — for each on-disk root the instance uses (the Postgres data directory *and* the raster/media
     store root from `RasterProperties`; de-duplicate by `FileStore`), `java.nio.file.FileStore.getUsableSpace`
     / `getTotalSpace` → free bytes, free percent.
   - **Memory** — `java.lang.management.MemoryMXBean` heap + `OperatingSystemMXBean` physical memory where
     available; fall back to `Runtime` for heap only and mark physical as unknown.
   - **Database** — reuse `SystemInfoService`'s `pg_database_size(current_database())`.
   Expose `ResourceStatus` (worst-of `ok`/`warn`/`critical`, plus each `ResourceGauge`).

2. **`getResourceStatus`** — a `ResourcesController` (tag `Resources`) returning the monitor's
   `ResourceStatus`. Admin; require `storage.manage`.

3. **Threshold settings** (`ai.foresignal.settings`). A `ResourceThresholds` record + `ResourceThresholdsService`
   over `AppSettingService` (settings key `thresholds`): per-gauge `warn` and `critical` expressed as a
   percent-free and/or an absolute GB floor (the admin slider's backend), plus `alertChannels` (channel ids
   to notify; empty = every user with a channel who holds `audit.read`/Admin). `getResourceThresholds` /
   `updateResourceThresholds` (PUT replaces). Validate: `warn` less severe than `critical`; percents in 0–100;
   GB ≥ 0; unknown channel ids → one `400` listing them. Default: a sensible warn/critical (e.g. 15 %/5 %
   free disk) shipped **enabled** (disk safety is the point), memory thresholds **off** by default.

4. **Breach → warning + alert.** A `SystemWarningContributor` bean emits `DISK_LOW`/`DISK_CRITICAL`,
   `MEMORY_LOW`/`MEMORY_CRITICAL` warnings from the monitor+thresholds so `/v1/system.warnings` shows them and
   status degrades. A `@Scheduled` evaluator (reading the same cached snapshot) **edge-triggers** an alert on
   entering `warn`/`critical` and a recovery notice on returning to `ok` — hysteresis so one breach fires once,
   exactly like the canary — by publishing `notify/SystemEvent` (`system.disk_critical`, severity `critical`,
   etc.) routed to `alertChannels`.

5. **Generalize to operational alerts.** Factor the edge-trigger-and-publish into a small
   `OperationalAlertService` (name to fit) that any subsystem calls to raise/clear a named operational alert
   through the one channel path. Add two more producers that use it:
   - **Source-failure storm** — subscribe to `SourceHealthChangedEvent`; when the count of sources in a failed
     state crosses an admin threshold (add it to `ResourceThresholds` or a sibling key), raise `system.source_storm`.
   - **Collector backlog** — from `CatchUpMonitor`/`PullGovernor`, when catch-up lag or the governor wait queue
     stays over a bound for a sustained window, raise `system.collector_backlog`.
   Keep each alert edge-triggered with a clear/recovery event.

**Migration.** Thresholds live in `app_setting` (no new table needed). Add the next free additive Flyway
migration `src/main/resources/db/migration/V{n}__<desc>.sql` **only** if you persist alert edge-state across
restarts — `n` = one past the highest `V*` file now present (R2-001…R2-013 claim V14 upward; read the
directory, never hard-code). Prefer in-memory edge-state and no migration.

## Contract

The three ops are authored with `x-foresignal-status: planned`. Implement them to the authored schemas. You
may add an example or an error code if your implementation needs it (then regenerate Postman with
`python3 scripts/generate-postman.py`), but **leave `x-foresignal-status: planned` and do not regenerate the
client SDKs** — R2-027 flips the whole v2.0 surface to `implemented` and regenerates the SDKs once against the
finished contract. `docs/OpenAPI.json` is the only contract.

## Tests to write (do not run)

- `ResourceMonitorTest` — gauges computed from a stub `FileStore`/memory bean; worst-of status.
- `ResourceThresholdsServiceTest` — validation (warn vs critical, 0–100 %, unknown channel id → 400); round trip.
- `ResourceAlertIT` — crossing into `critical` fires exactly one `SystemEvent` and one `/v1/system` warning;
  returning to `ok` fires one recovery; no re-fire while still breached.
- `OperationalAlertIT` — a source-failure storm and a sustained backlog each raise and clear their alert.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. Per `000-Run-Sequence.md`, run no test suite, and do
not run `lintOpenApi` or `checkApiCompat` (R2-027 runs the full compat pass). **Do** run
`./gradlew checkUsEnglish` and keep it green. Write the tests above but do not execute them. US English everywhere.

## Done when

`GET /v1/system/resources` returns live disk/memory/DB gauges with an overall status; the admin can set
warn/critical thresholds via `GET`/`PUT /v1/settings/thresholds`; a breach shows in `/v1/system.warnings`
**and** fires an alert through the channel system to the configured channels; source-failure storms and
collector backlog use the same operational-alert path; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog
history; `.gitlab-ci.yml`; other prompts' files. **Stage only the files this prompt changes.**

## Commit

`R2-014: Disk & resource monitoring and operational alerting`, then
`completion-reports/R2-014-CR-Disk-Resource-Monitoring.md` with the 40-character implementation hash.
