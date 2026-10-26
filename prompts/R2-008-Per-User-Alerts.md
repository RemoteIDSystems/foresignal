# R2-008 — Per-user alerts

**Goal.** Give every user their own trigger rules (alerts) over the **one shared record/event stream**: add
user ownership to trigger rules, evaluate each user's rules against the shared stream, and deliver each alert to
**that user's** channels. The existing trigger operations gain owner semantics; a Viewer cannot author alerts
(self-service alerts start at Editor, via `self.alerts`).

**Dependency.** R2-001 (users ↔ principals), R2-003/R2-004 (a session principal + the `self.alerts` gate),
R2-009 (per-user channels — alert delivery targets a user's channels; if R2-009 has not landed yet, deliver to the
owning user's channels once ownership exists and note the ordering in the CR). R2-005's reassigner registers the
`trigger_rule` table.

**No new operations.** The planned-ops list has none for this item; the **existing** trigger operations
(create/list/get/update/delete trigger rules, and the alert stream/feed) gain owner semantics. Add
`x-foresignal-permission: self.alerts` to the trigger **write** ops and keep reads at `self.alerts`/`data.read`
per the matrix (confirm each trigger op's current `x-foresignal-scope` in `docs/OpenAPI.json` and layer the
permission additively; do not change scopes).

**Read first.**
- `src/main/resources/db/migration/V1__baseline.sql` `create table trigger_rule` (~L349) — **it has no owner
  column today; all trigger rules are global.** This prompt adds ownership.
- `src/main/java/ai/foresignal/store/TriggerRuleEntity.java`, `src/main/java/ai/foresignal/trigger/`
  (`TriggerRuleService`, `AlertService`, `RuleValidator`, `RuleSqlBuilder`, the evaluation/anchor path) — how
  rules are created, validated, evaluated against the stream, and how alerts are produced and delivered.
- `src/main/resources/db/migration/V1__baseline.sql` `create table notification_channel` (~L481) — it already
  carries **`owner_principal_id`**. Alerts deliver through channels; owner-scoped delivery pairs rule owner with
  channel owner.
- R2-001's user↔principal decision. **Reconcile ownership with reality:** reuse the existing principal-ownership
  convention — store the owner as the **user's principal id** (the roadmap's `owner_user_id` maps to the user's
  principal, consistent with `notification_channel.owner_principal_id`). Pick one representation and use it
  consistently across R2-008…R2-012; record the choice in `docs/RBAC-Design.md`.

**Build.**

- **Ownership on `trigger_rule`.** Add an owner column (`owner_user_id uuid` **or** reuse the principal convention
  — decide per the reconciliation note and be consistent). Existing global rules migrate to **shared/unowned** or
  to the first admin (document which; recommended: keep pre-existing rules as the first admin's, so nothing stops
  firing, and the migration from global→admin happens in R2-027 first-run — here, a null owner means a legacy/shared
  rule the admin manages). New rules created by a session are owned by that user.
- **Scoping the trigger API.** `TriggerRuleService` filters to the caller's own rules for a session principal
  (`self.alerts`); an admin/operator can see/manage all (document which permission — platform alert administration
  vs. self-service). Create stamps the owner; update/delete enforce ownership (a user cannot touch another user's
  rule → 403/404). An `agent` api_token keeps managing rules as today (machine path), stamping the owner as the
  token's principal.
- **Per-user evaluation over the shared stream.** The collector still pulls each source **once** (shared substrate).
  The trigger engine evaluates **every** owner's enabled rules against that single stream; an alert is attributed
  to the owning user and delivered to **that user's** channels (R2-009). Keep evaluation efficient — do not
  re-pull or duplicate the stream per user; partition rule evaluation by owner over the same records. Anchor/cooldown
  state (`trigger_anchor_state`) stays per-rule, so it is naturally per-user once rules are owned.
- **Alert delivery + visibility.** An alert's audience is its rule's owner. The alert stream/feed operations filter
  to the caller's own alerts for a session; admin can see all. Reassignment (R2-005) moves a deleted user's rules to
  an admin so their alerts keep working.

**Model / migration.** Add **`Vn__trigger_rule_owner.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive): the owner column + an index on it; backfill
existing rows to null/shared (or first admin per the decision). Update `TriggerRuleEntity` to validate.

**Contract / docs.** No new ops; add `x-foresignal-permission: self.alerts` to the trigger write ops (additive) and
reflect owner semantics in the trigger op descriptions (a session sees/owns its own rules; `owner` is implicit).
If the trigger rule schema should expose an `owner`/`ownerUserId` field for admins, add it additively. Keep
`AccessPolicy`/spec-sync in agreement. Regenerate Postman only — do **not** regenerate the client SDKs (R2-027 does that once at cutover) and leave every op's authored `x-foresignal-status`. Update `docs/RBAC-Design.md` (per-user alerts over the
shared stream; the ownership representation) and `docs/Invariants.md` (one pull feeds all; alerts are owner-scoped;
no cross-user rule access).

**Tests to write (do not run).** a session's created rule is stamped with its owner; a user sees only their own
rules; a user cannot update/delete another user's rule (403/404); an admin sees all; evaluation over one shared
record set fires each owner's matching rule and attributes the alert to that owner; the alert is delivered to the
owner's channels; anchor/cooldown state is per-rule; reassigning a deleted user's rules keeps them firing; the
collector still pulls once regardless of how many users enabled a source. Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** Trigger rules carry an owner; a session manages only its own; the engine evaluates each user's rules
against the single shared stream and delivers to the owner's channels; `self.alerts` gates authoring; Viewers cannot
author; owner semantics are in the spec descriptions and Postman; docs updated; `checkUsEnglish` green; compilation
passes. The CR records the ownership representation chosen and how legacy rules were handled.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-008: Per-user alerts` + `completion-reports/R2-008-CR-Per-User-Alerts.md`. **Stage only this
prompt's files.** End your reply with `Implementation commit: <hash>`.
