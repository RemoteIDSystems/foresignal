# 160-CR-Collector-Governor-Status

| | |
|---|---|
| Prompt | 160-Collector-Governor-Status.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `e790589207401944da7199f1becda0e6deb53b94` |
| Date | 2026-10-22 |

## What was added

- **`GET /v1/settings/collector/status`** (`operationId` `getCollectorStatus`, tag Settings) on
  `CollectorSettingsController`, returning the pull governor's live `{ cap, inUse, waiting }`. The
  controller now takes the `PullGovernor` bean alongside `CollectorSettingsService`.
- **`CollectorStatus`** record (`ai.foresignal.collect`), the response body.
- **`PullGovernor.status()`**: a `synchronized` snapshot, so `cap` and `inUse` are read under the same
  lock that `resize` holds (calling `cap()` and `inUse()` separately could straddle a resize).
  `waiting` is the fair semaphore's queue length, as `waiting()` already reports it.
- **Access:** same as `getCollectorSettings`. Both are admin through the existing
  `rule(null, "/v1/settings/**", AccessRequirement.ADMIN)` in `AccessPolicy`, so no rule was added.
  `AccessPolicyTest`'s admin list now names `GET`/`PUT /v1/settings/collector` and
  `GET /v1/settings/collector/status`.

## Spec and Postman

- `docs/OpenAPI.json`: new path `/v1/settings/collector/status` (200 `CollectorStatus`, 401, 403, 429;
  `x-foresignal-scope: admin`, `x-foresignal-prompt: 160`, `implemented`) and new schema
  `CollectorStatus` (`cap` 1..16, `inUse` ≥ 0, `waiting` ≥ 0, all required, no additional
  properties). A 1.3.0 changelog entry was added. `info.version` stays **1.3.0**. The change is purely
  additive, but `checkApiCompat` was not run.
- The new 429 description says "honor Retry-After" (US English). The other 405 responses in the spec
  still say "honour". The spec is outside `checkUsEnglish`'s scanned trees, so those were left alone.
- `postman/` was regenerated with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`:
  "858 requests across 80 tag folders; quickstart 12 steps; covers all ops: True".

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- No tests were run (owner directive). These tests are written but **not** executed:
  - `CollectorSettingsControllerTest.statusReflectsTheGovernor`: with a governor of cap 3, the
    status is `(3, 0, 0)`, becomes `(3, 1, 0)` while a permit is held, and goes back to `(3, 0, 0)` once
    the permit is released.
  - `CollectorSettingsControllerTest.statusFollowsAResize`: after `resize(5)` the reported cap is 5.
  - `AccessPolicyTest`: the three admin rows above.

## Notes for the next prompt

- None. No blocker.
