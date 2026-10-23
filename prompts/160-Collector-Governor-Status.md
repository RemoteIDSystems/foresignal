# 160 — Collector governor status endpoint

**Goal.** Expose the pull governor's live state so the admin app's collector screen can show the
governor *working* (cap vs in-use vs waiting), instead of only the setting. Small, read-only, additive.

**Read first.** `collect/PullGovernor.java` (prompt 150) — it already tracks `cap()`, `inUse()`,
`waiting()`; `api/CollectorSettingsController.java` (`GET`/`PUT /v1/settings/collector`) and the access
pattern of the collector-settings read.

**Build.**
- Add a read endpoint **`GET /v1/settings/collector/status`** (`operationId` `getCollectorStatus`,
  tag Settings) returning the governor's live `{ cap, inUse, waiting }` from `PullGovernor`. Follow the
  same access rule as `getCollectorSettings`.
- Add the path and a `CollectorStatus` schema to the **authored** `docs/OpenAPI.json` under **1.3.0**
  (additive — **no version bump**); regenerate Postman; keep `checkApiCompat` additive.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Per `000-Run-Sequence.md`, run no
tests or other checks. Write a test (the endpoint reflects the governor's cap and a held permit shows in
`inUse`) but do not run it. US English.

**Done when.** `GET /v1/settings/collector/status` returns the governor's cap / in-use / waiting;
spec + Postman updated under 1.3.0; compile passes.

**Commit.** `160: Collector governor status endpoint` + `completion-reports/160-CR-*.md`. **Stage only
this prompt's files.**
