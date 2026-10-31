# SG-003-CR-Source-Thresholds

| | |
|---|---|
| Prompt | SG-003-Source-Thresholds.md |
| Status | Complete (compile only; `compileJava`/`compileTestJava` green; `checkUsEnglish` green; no test run) |
| Implementation commit | `c227ac81948f09d03b55f3e3a9b54a9fea4fcb65` |
| Date | 2026-10-30 |

A `self.alerts` user sets, per source, the minimum alert severity that notifies, plus mute and a notification
cooldown, through `/v1/me/source-thresholds`. The decision is notification-suppression only: an alert below the
threshold, muted, or within cooldown is still fired, persisted and shown in the inbox — it just does not notify. The
two operations remain `planned` — `SG-004` activates them.

## What was added

- **`source_threshold` + `source_notify_state` stores** (new package `ai.foresignal.notify.threshold`): migration
  `V36__source_threshold.sql` creates `source_threshold` (PK `(owner_principal_id, source_id)`, `min_severity`, `mute`,
  `cooldown_seconds`) and the sibling `source_notify_state` (PK `(owner_principal_id, source_id)`, `last_notified_at`).
  Both key on **`owner_principal_id`** (a string principal), matching the alert enforcement hot path
  (`alert.ownerPrincipalId()`), so no per-alert id mapping is needed. `SourceThresholdEntity` and
  `SourceNotifyStateEntity` share a composite `@IdClass` `SourceThresholdId`; `SourceThresholdRepository` and
  `SourceNotifyStateRepository` have `findByOwnerPrincipalId...` and `deleteByOwnerPrincipalId`. A
  `SourceThresholdReassigner` (`UserOwnedConfigReassigner`, audit name `sourceThresholds`, principal-keyed like the
  channel reassigner) removes a deleted user's thresholds and cooldown state.
- **`SourceThresholdService`** — `get(principal)` returns the user's `SourceThresholds` (one entry per source set; a
  source with no entry is the implicit default). `replace(principal, body)` validates each `sourceId` (a catalog
  source in the registry, or the caller's own custom source), each `minSeverity` via `Severity.fromWire`, and each
  `cooldown` via the shared `Durations.parsePositive`, then replaces the whole set (mirrors `updateMySources`); an
  unknown source / bad severity / bad cooldown is `400`. `shouldNotify(principal, sourceId, severity, now)` and
  `recordNotified(...)` are the router's hooks; `effective(principal, sourceId)` resolves the precedence.
- **`MySourceThresholdsController`** (`@Tag("Alerts")`) — `getMySourceThresholds` / `updateMySourceThresholds`,
  session/401 like `SourceViewsController`, keyed on `session.principalId()`.
- **Enforcement in `NotificationRouter.fired(...)`** — before routing, the per-source decision is applied for a
  user-owned alert (`sourceThresholds.shouldNotify`). When it suppresses (below severity, muted, or within cooldown)
  the message is still published but routed to no channel (the existing muted-rule `c -> false` path), so the alert is
  persisted and in the inbox but not notified. When it passes, `recordNotified` stamps `last_notified_at`, so the
  cooldown measures from when a notification actually went out. The service is injected as an
  `@Autowired(required = false)` field, like `alertMaps`, so hand-built router unit tests still construct.
- **Precedence (per-source > group > default)** — `effective(...)` returns the user's per-source entry if present;
  else the `GroupThreshold` of a source group of the user whose members include the source (SG-002); else the default
  (`warning`, un-muted, no cooldown). The group fallback maps the alert's `owner_principal_id` to the user
  (`UserRepository.findByPrincipalId`) and scans that user's groups.
- **Access rules (two places)** — `GET`/`PUT /v1/me/source-thresholds` in `AccessPolicy.rules()`
  (`READ_ALWAYS_AUTHENTICATED`). `AccessPermissions.java` already carried the `self.alerts` rows for these paths
  (SG-001's regeneration), so re-running the generator produces no diff.

## Tests written (not run)

- `SourceThresholdsCrudIT` — a `self.alerts` session gets (empty) and replaces its thresholds (whole-set, cooldown
  echoed as `PT1H`), an empty set clears it; an unknown source and a bad severity are `400`; an API token is `401`;
  a Viewer without `self.alerts` is `403`.
- `SourceThresholdSuppressionIT` — the decision: below `minSeverity` and within cooldown are suppressed, at/above and
  outside cooldown notify (and `recordNotified` stamps); a muted source never notifies; a source with no entry uses
  the default `warning`.
- `ThresholdPrecedenceIT` — `effective(...)` is the default with nothing set, the covering group's threshold when a
  group sets one, and the per-source entry when present (per-source > group > default).

## Decisions / owner questions

- **`owner_principal_id` keying.** The threshold and cooldown-state tables key on the string principal, not the user
  uuid, because the enforcement hot path has `alert.ownerPrincipalId()` and avoiding a per-alert principal→uuid lookup
  matters there. The controller keys on `session.principalId()`, and the group-threshold fallback maps principal→user
  once (not per alert). Confirm this is the intended key.
- **`last_notified_at` as a sibling table.** Cooldown state lives in `source_notify_state`, separate from
  `source_threshold`, so `getMySourceThresholds` returns only the entries the user set — never a row that exists
  solely to track cooldown (a cooldown can come from a group threshold, where there is no per-source entry). Recorded
  here as the chosen layout.
- **Precedence resolution for cooldown.** A cooldown may come from a per-source entry or a covering group threshold;
  either way the last-notified stamp is per `(owner, source)` in `source_notify_state`, so switching a source between
  a group cooldown and a per-source cooldown keeps one timeline. If multiple groups cover one source, the first by
  creation order wins (documented; no contract rule for ties).
- **Scope of enforcement.** Only a user-owned alert (`ownerPrincipalId != null`) has a per-source threshold; a shared
  alert routed to instance channels is unaffected, consistent with R2-008's owner model.

## Gate

- `./gradlew compileJava compileTestJava` — green (only pre-existing deprecation/unchecked notes).
- `./gradlew checkUsEnglish` — green.
- No test suite, `lintOpenApi`, `checkApiCompat` or SDK/Postman generation (compile-only gate; `SG-004` runs those).
