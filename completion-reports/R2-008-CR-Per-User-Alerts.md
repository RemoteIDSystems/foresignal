# R2-008-CR-Per-User-Alerts

| | |
|---|---|
| Prompt | R2-008-Per-User-Alerts.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `8da63de9400c89ccd17afc64092e142be2b5cdb4` |
| Date | 2026-10-26 |

## What was added

- **`V16__trigger_rule_owner.sql`** — `trigger_rule.owner_principal_id varchar(64)` and `alert.owner_principal_id`,
  each indexed. No backfill: every existing rule and alert is **shared** (null).
- **Ownership representation: the user's principal** (`user.principal_id`), the convention
  `notification_channel.owner_principal_id` already used, so rule owner and channel owner compare directly. Not a
  `user.id` column named `owner_user_id`. R2-009…R2-012 should use the same.
- **`security/user/OwnerScope`** — one rule for every user-owned table: a session sees and manages its own rows and
  owns what it creates; a session holding `users.manage` sees and manages all (and owns what it creates); an API token
  sees and manages all and creates **shared** rows; anonymous callers and feed/tile keys see shared rows only.
- **Trigger API** — `listTriggers` filtered by `OwnerScope`; `createTrigger` stamps the owner; updates keep it;
  `TriggerRule.ownerPrincipalId` (read-only) in responses. **`api/support/TriggerOwnershipInterceptor`** answers
  `404` (as for an unknown id) for every `/v1/triggers/{ruleId}…` operation, KML included, on a rule the caller may
  not see; it covers `TriggersController`, `WindowController`, `RuleAnalysisController` and `KmlController` in one
  place.
- **Alerts** — the insert copies the rule's owner into `alert.owner_principal_id` (a subquery in the same
  statement, so the engine's cached rule is never consulted). `AlertView.ownerPrincipalId` (new last member, the old
  shape kept as a constructor). **`security/restrict/AlertAudience`** rides in `ReadRestriction` (never stored, part
  of `canonical()`): bound by `TokenFilter` for sessions without `users.manage`, and `SHARED_ONLY` for anonymous
  callers, feed keys and tile keys. Applied by `AlertStore` (lists, feeds, replay, counts), `AlertQuery.matches`
  (streams, CAP), `AlertsController` (`getAlert` and the lifecycle writes: `404`), the inbox and badge, the
  notification topics (`data.ownerPrincipalId`, added to alert notifications) and search.
- **Delivery** — `NotificationRouter.fired` sends a user's alert through `NotificationService.publishOwned` to the
  owner's enabled channels only (`NotificationChannel.acceptsOwned`: a personal channel also takes its owner's
  alerts; severity and rule filters apply). Shared alerts go to instance channels exactly as before.
- **`trigger/TriggerRuleReassigner`** (`triggerRules`) — reassign moves rules and their alerts to the target admin
  (shared with no target); `reassign=false` deletes the rules and keeps their alerts; the engine reloads after commit.
- Tests (written, not run): `PerUserAlertsIT`, `OwnedAlertDeliveryIT`, `OwnerScopeTest`,
  `TriggerOwnershipInterceptorTest`.
- Docs: RBAC-Design §D "Built (R2-008)", the §C2 permission table (502 operations; `self.alerts` 18, `data.read`
  118), the "what exists" table and the §G reassigner paragraph; Invariants RBAC-13.

## Contract

- `x-foresignal-permission: self.alerts` on 18 writes: `createTrigger`, `updateTrigger`, `deleteTrigger`,
  `testTrigger`, `snoozeTrigger`, `unsnoozeTrigger`, `muteTrigger`, `unmuteTrigger`, `rebuildBaseline`,
  `applyTuningSuggestion`, `dismissTuningSuggestion`, `undoTuning`, `analyzeTriggerSources`, `acknowledgeAlert`,
  `resolveAlert`, `snoozeAlert`, `unsnoozeAlert`, `setAlertFeedback`. `data.read` on 6 reads: `listTriggers`,
  `getTrigger`, `getTriggerWindowState`, `getTriggerBaseline`, `listTuningLog`, `getAlertEscalation`. Scopes
  unchanged. `AccessPermissions` regenerated (900 rows, 502 with a permission); static policy emulation: 0 problems.
- The authored "v2.0 (per-user)" paragraph on the five trigger CRUD operations replaced with what was built (see
  disagreement 1); a per-user paragraph appended to `listAlerts`, `getAlert`, `streamAlerts` and the two alert feeds.
  `TriggerRule` gains read-only `ownerPrincipalId`. Statuses untouched.
- Postman regenerated ("936 requests … covers all ops: True").

## Decisions and disagreements for the owner

1. **The authored description said `owner_user_id` and "an operator may also manage shared/global entries".** Built:
   the owner is the user's principal (the prompt's reconciliation note), and "manage everything" is `users.manage`
   (Admin), not Operator. The seeded Operator role has neither `self.alerts` nor `users.manage`, so an Operator
   session cannot manage alerts at all. Should Operators manage shared rules (which would need a permission, or
   `self.alerts` on the Operator role)?
2. **Rules created by an API token are shared, not owned by the token's principal** as the prompt says. An owned rule
   delivers only to its owner's channels, and a token's principal owns no alert channels, so stamping the token's
   principal would have silently stopped delivery for every machine-made rule (agents, MCP, settings import). Which do
   you want?
3. **Legacy rules stay shared (null) here**; moving them to the first admin is left to R2-027's first run, as the
   prompt recommends.
4. **Shared alerts are visible to every signed-in user** (they go to every instance channel anyway); shared **rules**
   are visible only to admins and tokens in the trigger API.
5. **A user's alert never reaches instance channels.** R2-009 adds global channels a user's alerts may use.
6. **Anonymous callers and feed/tile keys now see shared rules and alerts only.** Today all rules and alerts are
   shared, so nothing changes until a user creates one. Their restriction's canonical form gained `o=*shared`, which
   changes anonymous ETags and invalidates outstanding anonymous cursors once.
7. **Not per user yet** (no read restriction there before either): statistics counts, dossier and context-summary
   activity, briefing digests, operator gauges. Left for R2-013's data-access gating.
8. Alert lifecycle writes got `self.alerts`, so a Viewer session cannot acknowledge alerts (it could not before
   either: Viewer's derived scope is `read`).

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Not run (written only): owner stamped (session) / shared (token); list scoped per user, all for admin session and
  token; another user's and a shared rule `404` on get/put/snooze/test/tuning-log/delete; update keeps owner; Viewer
  `PERMISSION_DENIED` with `requiredPermission: self.alerts`; one record fires each owner's rule, attributed, stored
  once; alert lists, get and ack scoped by owner; per-rule cooldowns independent; reassign on delete moves rules and
  alerts and the rule keeps firing; `reassign=false` deletes rules, keeps alerts; owned alert delivered to owner's
  channels (personal included) only; shared alert to instance channels only; owner channel severity filter;
  `OwnerScope`, `AlertQuery`, `NotificationFilter` and canonical-form unit tests; interceptor path matching.
- The "collector pulls once regardless of users" test is the stored-once assertion above; the collector itself is
  untouched (per-user source views are R2-010).

## Notes for the next prompt

- R2-009: reuse `OwnerScope` and `owner_principal_id`; extend `NotificationService.publishOwned` with the global
  channels a user's alerts may use; register a channel reassigner.
- Any new alert read must apply `ReadRestriction.alertAudience()` (`AlertStore.audiencePredicate` for SQL,
  `AlertAudience.allows` in memory).
