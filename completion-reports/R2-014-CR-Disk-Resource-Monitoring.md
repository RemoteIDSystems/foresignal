# R2-014-CR-Disk-Resource-Monitoring

| | |
|---|---|
| Prompt | R2-014-Disk-Resource-Monitoring.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `52d4bd467641373cb8e7d7352ee94def77093d7a` |
| Date | 2026-10-26 |

## What was added

- **No migration.** Edge state is kept in memory, as the prompt prefers. Thresholds live in `app_setting` under the
  new key `thresholds` (`SettingKeys.THRESHOLDS`, added to `SettingKeys.ALL`).
- **Host-resource monitor** (`system/`):
  - `ResourceMonitor` reads three things:
    - **Disk:** the file store of the data directory (`ForesignalProperties.dataDir`) and of the raster store root
      (`RasterPaths.root()`), de-duplicated by `FileStore`.
    - **Memory:** physical memory from `com.sun.management.OperatingSystemMXBean`, falling back to the heap from
      `MemoryMXBean`.
    - **Database:** `pg_database_size(current_database())`.
  - Readings are cached for 30 s. The cache is dropped when `thresholds` changes.
  - The `disk` gauge is the most severe volume (on a tie, the one with the least free share). The overall status is
    the worst gauge.
  - `ResourceGauge` and `ResourceStatus` are records that match their schemas.
- **`GET /v1/system/resources`** is served by `api/ResourcesController` (tag `Resources`), which also serves the two
  thresholds operations.
- **Thresholds:**
  - `settings/ResourceThresholds` holds `disk` and `memory` (each a `Threshold` of warn/critical × percent/bytes),
    `alertChannels`, `sourceStorm` and `collectorBacklog`.
  - A level is reached when the free share drops below its percent **or** the free amount drops below its bytes.
  - Defaults:
    - disk: 15 % warn, 5 % critical;
    - memory: off;
    - storm: 5 failing sources;
    - backlog: trigger lag above 10,000 seq for 300 s; waiting pulls off.
  - `settings/ResourceThresholdsService` parses a PUT as a whole replacement and reports every problem in one 400:
    - critical must be below warn;
    - percents must be 0–100;
    - bytes and counts must be whole numbers;
    - `sustainedSeconds` must be 30–86,400;
    - unknown members are refused;
    - unknown channel ids are one 400 listing them, checked on put.
  - In a PUT body, a missing member takes its default and an explicit `null` turns it off.
- **Breach → warning:** `ResourceWarnings`, a `SystemWarningContributor`, emits `DISK_LOW` / `DISK_CRITICAL` and
  `MEMORY_LOW` / `MEMORY_CRITICAL` into `/v1/system.warnings`. Its severity (`warning` / `critical`) degrades the
  status.
- **Breach → alert:** `OperationalAlertService` is the single path every operational alert takes:
  - `report(key, level, wording)` publishes only when the level changes. Entering `warn` or `critical` publishes the
    alert; returning to `ok` publishes one recovery notice. (Moving from critical to warn also counts as a change and
    publishes.)
  - The alert is a `SystemEvent` carrying `Targets`:
    - when `alertChannels` names channels, exactly those, whatever their event filters, personal channels included;
    - when it is empty, every channel subscribed to `system` **plus** the enabled personal channels of every active
      user holding `audit.read`.
- **Producers:**
  - `ResourceAlertEvaluator` runs every 30 s and publishes:
    - `system.disk_low`, `system.disk_critical` and `system.disk_recovered`;
    - `system.memory_low`, `system.memory_critical` and `system.memory_recovered`.
  - `SourceStormMonitor` counts the sources currently `failing` from `SourceHealthChangedEvent` and publishes
    `system.source_storm` and `system.source_storm_cleared`.
  - `CollectorBacklogMonitor` runs every 30 s. It reads `CatchUpMonitor.last().triggerLagSeq()` and
    `PullGovernor.waiting()`, raises when either stays over its bound for `sustainedSeconds`, and publishes
    `system.collector_backlog` and `system.collector_backlog_cleared`.
  - Both ticks are gated by `foresignal.resources.scheduled`: true in `application.yml`, false in
    `application-test.yml`.
- **Notification plumbing:**
  - `SystemEvent` gains a `targets` component. The 6-argument constructor is kept, so every existing publisher still
    means "the `system` subscribers".
  - `NotificationRouter.onSystemEvent` routes by targets; when called inside a transaction it publishes on the existing
    system publisher thread.
  - New `NotificationService.publish(message, filter, alsoTo)`: the existing loop over shared subscriber channels, then
    one send to each enabled channel in `alsoTo` not already reached. Quiet hours and the pause apply as for any send.

## Contract

- The three operations are implemented to their schemas and stay `x-foresignal-status: planned`. Their descriptions
  now name the warning codes and the notification subtypes.
- **Additive schema changes (see decision 1):**
  - `ResourceThresholds` gains `sourceStorm {failedSources}` and `collectorBacklog {triggerLagSeq, waitingPulls,
    sustainedSeconds}`.
  - The descriptions of `ResourceThresholds` and `alertChannels` now state the default and null semantics.
  - `ResourceThreshold` gains `minimum`/`maximum` bounds and descriptions for its two critical members.
  - `ResourceStatus.disk` and `ResourceStatus.memory` are now `allOf` wrappers of `ResourceGauge`, so each can carry a
    description saying what it measures. The spec already has 97 `allOf` blocks and 27 `$ref`s with a sibling
    `description`.
- `AccessPermissions` was regenerated with no change: 900 rows, 550 with a permission. The static access check printed
  "0 problems". The three operations already carried `storage.manage`.
- **Postman** was regenerated. `scripts/generate-postman.py` gains an `updateResourceThresholds` example body; before
  this, the generated body was `{}`. The SDKs were not regenerated.
- **Tests written, not run:**
  - `ResourceMonitorTest`:
    - disk gauges from a stub `FileStore`;
    - percent and byte levels;
    - worst-volume and worst-gauge status;
    - the heap fallback from a stub `MemoryMXBean`;
    - the warning codes.
  - `ResourceThresholdsServiceTest`:
    - defaults and null;
    - a full parse;
    - every validation rule, including all problems in one 400;
    - unknown channels in one 400, with nothing stored;
    - a JSON round trip.
  - `ResourceAlertIT`:
    - live gauges;
    - the thresholds round trip;
    - a Viewer gets 403 naming `storage.manage`;
    - critical fires exactly one `system.disk_critical` and one `DISK_CRITICAL` warning, with the status degraded;
    - no re-fire while still breached;
    - exactly one recovery;
    - an alert reaches only the chosen channel (checked against `notification_delivery`);
    - with no alert channels set, the targets include an admin's personal channel.
  - `OperationalAlertIT`:
    - a storm raises at the threshold, does not re-fire, and clears once;
    - a null storm threshold never raises;
    - a sustained lag raises and clears once;
    - a brief spike restarts the window;
    - the waiting-pull bound.
  - The breach in the ITs is forced with `criticalFreePercent: 100`, because a real disk cannot be filled in a test.

## Decisions and disagreements for the owner

1. **The authored schema had no field for the storm or backlog thresholds.** The prompt says to add them "to
   `ResourceThresholds` or a sibling key" and also to "implement to them exactly". An admin must be able to set them,
   so I added two optional members to `ResourceThresholds`. This is additive, and the operations are still `planned`.
   Should they move to a sibling key instead?
2. **The Postgres data directory cannot be gauged from here.** The prompt asks for it. It is the `pgdata` volume,
   mounted only in the `db` container (`docker-compose.yml:50`). The application container sees `/data`
   (`docker-compose.yml:109`). The disk gauge covers `/data` and the raster root; the database is covered by
   `database.sizeBytes`.
   - Both are named Docker volumes. They normally share the host's Docker volume filesystem, but that is a property of
     the host, not something I verified.
   - The alternatives are mounting `pgdata` read-only into the application container, or a check run inside the
     database container. Either changes `docker-compose.yml`, which this prompt does not cover.
3. **`ResourceGauge` is a single gauge, so `disk` is the worst volume.** The per-volume readings are not returned. A
   `disk.volumes` array would need another schema member.
4. **Memory in a container.** `OperatingSystemMXBean` reports the container's limit and free memory as the JVM sees
   them. "Free" there excludes page cache, so a busy box can look low on memory when it is not. That is one reason
   memory thresholds are off by default, as the prompt asks.
5. **Disk thresholds are on by default.** On any host with less than 15 % free on `/data`, `/v1/system` now reports
   `degraded`, and an alert fires within 30 s of startup. That includes a test host, where any existing IT asserting
   `/v1/system` `status: ok` would fail. This workstation has 35 % free (`df -h .`: 319G of 937G), so it does not
   trip.
6. **An empty `alertChannels` means "system subscribers plus audit.read users' personal channels".** The schema says
   "all channels of users with `audit.read`". Keeping the shared `system` subscribers means the operator's existing
   `system` channels still receive these alerts, as they receive every other system notification. Say if the shared
   subscribers should be dropped.
7. **Alerts sent to named channels ignore those channels' event filters,** the same as escalations (prompt 033).
   Naming a channel in `alertChannels` is taken as the opt-in.
8. **Thresholds are not in the settings export or import.** The export bundle is an explicit list tied to its own
   schema (`SettingsTransferService`), which this prompt does not cover.
9. **Edge state is in memory.** After a restart, a condition that is still breached fires once more.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- V20 is still the highest migration; the next is V21.
- To raise or clear an operational alert, call `OperationalAlertService.report(key, level, wording)`. It handles edge
  triggering and routing.
- `SystemEvent.Targets` routes a system notification to named channels. The 6-argument `SystemEvent` keeps the old
  behavior.
- `ResourceMonitor.snapshot()` is the cached disk/memory/DB reading. R2-015 (retention) and R2-016 (archival) can use
  it to act on disk pressure.
