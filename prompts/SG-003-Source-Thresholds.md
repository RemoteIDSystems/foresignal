# SG-003 — Per-source severity thresholds

**Context.** API **2.2.0**, authored contract-first in `docs/OpenAPI.json` (two `/v1/me/source-thresholds`
operations present as `x-foresignal-status: planned`, tagged `x-foresignal-prompt: SG-003`,
`x-foresignal-permission: self.alerts`; schemas `SourceThresholds` = `entries[]`, `SourceThresholdEntry` =
`sourceId`, `minSeverity` (info/warning/critical, default warning), `mute`, `cooldown`). This prompt
**implements** that authored delta. Operations stay `planned`; `SG-004` activates them. Runs after `SG-002`
(a per-source entry overrides a source group's `notificationThreshold`).

**Goal.** A user holding `self.alerts` sets, per source, the **minimum alert severity that notifies**, plus
**mute** and a notification **cooldown** — through `/v1/me/source-thresholds` (get the whole set; replace the
whole set, mirroring `updateMySources`). The threshold is a **notification-suppression** decision: an alert
below the threshold (or muted, or within cooldown) is still fired, persisted and shown in the alerts inbox —
it just does not notify, exactly like the existing rule-mute path.

## Operations (authored `planned` — implement; `SG-004` activates)

| operationId | method + path | gating |
|---|---|---|
| `getMySourceThresholds` | `GET /v1/me/source-thresholds` | `self.alerts` |
| `updateMySourceThresholds` | `PUT /v1/me/source-thresholds` | `self.alerts` |

## Read first

- The authored 2.2.0 delta in `docs/OpenAPI.json`: the two ops, `SourceThresholds`, `SourceThresholdEntry`,
  and (for precedence) `GroupThreshold` from SG-002.
- **Self-service template:** `api/SourceViewsController.java` — `getMySources`/`updateMySources` at
  `/v1/me/sources` (whole-set get/replace, `self.sources`); copy its session/401 shape, swapping the
  permission to `self.alerts`. `security/user/AuthenticatedSession.java` (`userId()`/`principalId()`).
- **Severity model (use exactly this):** `trigger/Severity.java` — enum `INFO < WARNING < CRITICAL`, `wire()`,
  `fromWire(String)` (throws on a bad value), `atOrAbove()`. `minSeverity` must parse via `Severity.fromWire`
  and compare with `atOrAbove()`; default `warning`.
- **Duration parsing:** reuse the ISO-8601/shorthand parser the trigger `cooldown` uses (the
  `DurationParser`/`Duration` helper `RuleValidator` relies on) so `cooldown` accepts `P7D`/`15m`/`12h`.
- **Ownership neighbors (why principal-id here):** the alerts/notify world keys on `owner_principal_id`
  (string) — `store/TriggerRuleEntity.owner_principal_id`, `store/NotificationChannelEntity.owner_principal_id`,
  `store/AlertEntity` (`ownerPrincipalId`). The enforcement hot path reads `alert.ownerPrincipalId()`, so key
  `source_threshold` on `owner_principal_id` (not the uuid SG-002 uses) to avoid a per-alert id mapping.
  Confirm the choice in the CR.
- **THE enforcement hook:** `notify/NotificationRouter.java` — `onAlert(AlertEvent)` → `fired(alert)`
  (≈160-198), which builds a `Predicate<NotificationChannel>` (the existing muted-rule check) and routes via
  `service.publish(...)` / `service.publishOwned(message, alert.ownerPrincipalId(), filter)`. Apply the
  per-source decision **before** publish: short-circuit to no-route when below threshold, muted, or within
  cooldown. Alert persistence and the inbox are untouched.
- `api/NotificationSettingsController.java` / `notify/NotificationSettingsService.java` — the existing
  instance-level notify settings. SG-003 is **not** an extension of that blob; it is a new owner-scoped
  per-source table. Align only on the `Severity` enum and the router hook.
- `security/AccessPolicy.java` `rules()` (`/v1/me/*` block); `security/AccessPermissions.java` (generated;
  `self.alerts` already in the spec on these ops — precedents: `POST /v1/triggers`, `/v1/alerts/*/snooze`);
  `security/user/Permissions.java` (`SELF_ALERTS` exists); `self.alerts` already granted Editor+ — no
  role-seed change.
- `src/main/resources/db/migration/` — after SG-001 (`V34`) and SG-002 (`V35`), this prompt's migration is the
  next free (read the dir and confirm; expected `V36`).

## Build

1. **`source_threshold` store.** Additive Flyway migration (next free `V{n}`) creating `source_threshold`:
   `owner_principal_id` + `source_id` (composite PK), `min_severity`, `mute boolean`, `cooldown_seconds`
   (nullable), plus a `last_notified_at` per `(owner_principal_id, source_id)` for cooldown enforcement (same
   table or a sibling — your call; record it). Matching `@Entity`, repository
   (`findByOwnerPrincipalId...`, upsert-by-owner for whole-set replace), and a `UserOwnedConfigReassigner`
   (reassign by `owner_principal_id`; copy the channel reassigner).
2. **`SourceThresholdService`.** `get(principal)` returns the user's `SourceThresholds` (entries for every
   source they have set; a source with no entry is implicitly default `warning`, un-muted, no cooldown).
   `replace(principal, SourceThresholds)` validates each `sourceId` is known (catalog `SourceRegistry` or an
   owned custom source), each `minSeverity` via `Severity.fromWire`, each `cooldown` via the shared parser,
   then replaces the whole set for that owner (mirror `updateMySources`). Unknown source / bad severity →
   400.
3. **`MySourceThresholdsController`** (`@Tag("Alerts")` to match the contract tag) — get/replace, session/401
   like `SourceViewsController`, keyed on `session.principalId()`. Audit the replace through the
   session/`AuditService` path.
4. **Enforcement in the router.** In `NotificationRouter.fired(...)`, before routing, resolve the effective
   threshold for `(alert.ownerPrincipalId(), alert.sourceId())` and suppress the notification when the alert's
   severity is below `minSeverity`, or the source is muted, or it is within cooldown of the last notification
   for that `(owner, source)`; on a notification that passes, stamp `last_notified_at`. The alert is still
   persisted and in the inbox — only the notify is suppressed (reuse the existing suppressed/muted path's
   shape). **Precedence (document it):** a per-source `SourceThresholdEntry` **overrides** any SG-002 source
   group `GroupThreshold` covering that source, which overrides the default `warning` — i.e. per-source >
   group > default.
5. **Access rules (two places).** Add `/v1/me/source-thresholds` GET/PUT to `AccessPolicy.rules()`
   (`READ_ALWAYS_AUTHENTICATED`, `/v1/me/*` block), and regenerate `AccessPermissions.java` (`self.alerts`
   already in the spec) so `AccessPolicyTest`/`AccessPolicySyncTest` agree.

## Tests to write (do not run)

- `SourceThresholdsCrudIT` — a `self.alerts` user gets (empty → defaults) and replaces their thresholds;
  unknown source / bad severity → 400; an API token is 401; a Viewer without `self.alerts` is 403.
- `SourceThresholdSuppressionTest`/`IT` — an alert below `minSeverity`, or from a muted source, or within
  cooldown, is persisted and in the inbox but **not** notified; one at/above severity and outside cooldown
  notifies and stamps `last_notified_at`.
- `ThresholdPrecedenceTest` — a per-source entry overrides a covering group threshold, which overrides the
  default.

## What runs

Compile only — `./gradlew compileJava compileTestJava` — and `./gradlew checkUsEnglish`. **Write tests, do not
run them.** No SDK/Postman generation here (`SG-004`).

## Done when

A `self.alerts` user gets/replaces per-source thresholds through `/v1/me/source-thresholds`; an alert below
threshold / muted / within cooldown is suppressed from notification (but kept in the inbox); per-source
overrides group overrides default; access rules updated in both places; compile and `checkUsEnglish` green; the
two ops remain `planned`.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; `.gitlab-ci.yml`; other prompts' files;
alert persistence and the inbox (thresholds gate notification only). The 2.2.0 contract is **authored** —
implement to it; add a missing error/field only if needed, and record it. **Stage only this prompt's files.**

## Commit

`SG-003: Per-source severity thresholds`, then
`completion-reports/SG-003-CR-Source-Thresholds.md` with the 40-character commit hash and any owner questions
(notably the `owner_principal_id` keying choice, the `last_notified_at` cooldown store, and the per-source >
group > default precedence).
