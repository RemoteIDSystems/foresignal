# R2-013 — Per-user data access gating + audit log

**Goal.** Finish the RBAC data story: enforce per-user **`data.read`** (and `data.export`/`intel.read`/
`forecast.read`) plus optional **area restrictions** on what a user can read, and record an append-only **audit log**
(actor / action / target) for user and config/security actions that feeds the already-implemented `listAuditEvents`
for the **Auditor** role (`audit.read`).

**Dependency.** R2-001 (users/permissions), R2-003 (session principal), R2-004 (the data/audit permission extensions
and enforcement hooks). This is the last core-RBAC prompt (R2-001…R2-013) and ties the data surface to users.

**No new operations.** `listAuditEvents` already exists (prompt 023, `scope: admin`, `x-foresignal-permission:
audit.read`). This prompt extends the audit **actor model** and the **data-read gating**, and makes R2's actions land
in the log. Confirm `listAuditEvents` filters/paging cover the new actor/action/target fields.

**Read first.**
- `src/main/java/ai/foresignal/security/audit/` — `AuditService.record(AuditRecord)` (async bounded queue),
  `AuditEventEntity`, `AuditRecord`, `AuditQuery`, `AuditEventView`, `AuditRetentionStep`. The existing `audit_event`
  table (prompt 023) with `actor_kind` (`token|ingest_key|pairing|system`), `token_*`, `method`, `path`,
  `operation_id`, `status`, `target_*`, `remote_ip`, `detail jsonb`.
- `src/main/java/ai/foresignal/security/restrict/` — `RestrictionEnforcer`, `ClampedRestrictionEnforcer`,
  `RestrictionValidator`, `TokenRestrictions`, `RequestPrincipal`, and `security/visibility/` — how token data
  restrictions (allowed sources/kinds/bbox) are enforced on read paths today (prompt 035). Per-user area restrictions
  reuse this machinery.
- R2-004's data/audit `x-foresignal-permission` extensions and the session-vs-token enforcement rule.
- `docs/OpenAPI.json` — `listAuditEvents` + the `AuditEvent`/`AuditEventPage` schemas.

**Build.**

- **Per-user data gating.** A session principal must hold `data.read` to read records/changes/tiles/intel/forecast
  data; `data.export` for exports; `intel.read`/`forecast.read` for those surfaces. R2-004 attached the extensions;
  this prompt **enforces** them end to end (including on the read/stream/export paths), returning 403
  `PERMISSION_DENIED` when missing. Viewer/Auditor hold `data.read`; a role lacking it cannot read data. Machine
  tokens keep scope-based access (an `admin`/`agent`/`read` token is unaffected by `data.read`, per R2-004's
  machine-bypass rule).
- **Per-user area restrictions.** Optionally constrain a user's data reads to an **area** (bbox/geofence) and/or a
  source/kind subset — the same shape as `TokenRestrictions`, but stored per user. Add a `user_data_restriction`
  (or columns on a user-settings row) carrying `allowed_sources`, `allowed_kinds`, `allowed_bbox`. When set, a
  session's reads are clamped through the existing `RestrictionEnforcer`/`ClampedRestrictionEnforcer` (reuse it; do
  not re-implement geofencing) so a user sees only records inside their area/subset. Null/empty = unrestricted.
  Managed by an admin (`users.manage`) — decide and document the operation surface: reuse `updateUser`/a user-admin
  sub-resource (R2-005) rather than inventing an unplanned endpoint; if a field on the user schema is the cleanest,
  add it additively.
- **Audit actor model.** Extend `AuditService`/`audit_event` so an action can be attributed to a **user**: add
  `actor_kind = user` with `user_id` and `user_email` (and keep token/system/ingest/pairing actors working). Record,
  through the existing async path:
  - **auth/security:** login success + failure, logout, session revocation, password reset/activation, MFA changes
    (when R2-024 lands) — R2-003 already emits some; ensure they carry the user actor.
  - **identity/config changes:** user create/update/delete/role-change/suspend (R2-005), role create/update/delete
    (R2-006), SMTP/settings changes (R2-007 and other `system.settings` writes), collection-set changes (R2-010),
    LLM provider changes (R2-011). Each lands as `actor=user, action=<operationId or verb>, target=<type:id>`.
  - **data access denials:** 403 `PERMISSION_DENIED` on data-read/export paths (so an Auditor can see attempted
    access). Keep the detail free of secrets, tokens and request bodies (the existing rule).
- **`listAuditEvents` for the Auditor.** Ensure the Auditor role (`audit.read` + `data.read`) can read the log and
  that filtering/paging covers actor kind, user, operation, status, target and time. The op stays `admin`-scoped by
  `x-foresignal-scope` but is reachable by a session holding `audit.read` (R2-004's permission gate), so an Auditor
  with no admin scope can read it — verify the scope/permission interplay lets the Auditor in and keeps others out.
  Add the new fields to the `AuditEvent` schema additively.
- **Retention.** The audit retention step (prompt 023, `AuditRetentionStep`) continues to apply; keep the default
  window and ensure the new rows are covered.

**Model / migration.** Add **`Vn__audit_user_actor_and_data_restrictions.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive): the `user_id` /
`user_email` columns (+ `actor_kind='user'`) on `audit_event`, and the per-user data-restriction storage. Entities
validate under `ddl-auto: validate`.

**Contract / docs.** No new ops; extend the `AuditEvent` schema (user actor fields) additively and ensure
`listAuditEvents` filters include them; confirm the data-read ops' `x-foresignal-permission` (set in R2-004) is
enforced. Keep `AccessPolicy`/spec-sync in agreement. Regenerate Postman only — do **not** regenerate the client SDKs (R2-027 does that once at cutover) and leave every op's authored `x-foresignal-status`. Update `docs/RBAC-Design.md` (data gating,
per-user area restrictions, the Auditor role) and `docs/Invariants.md` (data reads require `data.read`; a user's
reads are clamped to their area/subset; the audit log is append-only and secret-free; config/security actions are
audited with a user actor).

**Tests to write (do not run).** a session without `data.read` gets 403 on a record read; with `data.read` it
succeeds; a user with an area restriction sees only in-area records (clamped via the existing enforcer); a user
without the restriction sees all they are permitted; machine tokens are unaffected by `data.read`; user create/role
change/suspend/login/session-revoke/settings change each write an `actor=user` audit row with the right action/target;
secrets never appear in `detail`; an Auditor (audit.read + data.read, no admin scope) can read `listAuditEvents` and a
plain Viewer cannot; filters by actor kind/user/operation/status/time work; audit retention covers the new rows.
Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** Data reads are gated by `data.read`/`data.export`/`intel.read`/`forecast.read`; per-user area/subset
restrictions clamp a user's reads via the existing enforcer; the audit log records user-attributed auth/config/
security actions and data-access denials, secret-free and append-only; the Auditor can read `listAuditEvents` and
others cannot; machine tokens unaffected; schema/Postman updated; `checkUsEnglish` green; compilation passes. The CR
records the data-restriction storage + the audit actor extension.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-013: Per-user data access and audit log` + `completion-reports/R2-013-CR-Data-Access-Audit.md`.
**Stage only this prompt's files.** End your reply with `Implementation commit: <hash>`.
