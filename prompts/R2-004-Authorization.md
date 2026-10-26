# R2-004 — Authorization: role→permission checks

**Goal.** Turn effective permissions into enforcement. Add per-operation **permission requirements** to the
contract and to `AccessPolicy`, so a session principal is gated by what its roles grant — while **scoped API
tokens keep working for machines exactly as today**. This is the gate the whole management surface (R2-005/006/007,
and the self-service prompts) relies on.

**Dependency.** R2-003 (a session resolves to a principal carrying effective permissions and an effective scope).
R2-001 (the permission catalog). It runs **before** R2-005/R2-006 so those controllers can rely on the gate.

**Read first.**
- `src/main/java/ai/foresignal/security/AccessPolicy.java` — the single (method,path)→requirement table,
  transcribed from each operation's `x-foresignal-scope`; order matters (first match wins, specific before
  general); there is a spec-sync test (`AccessPolicySpecSyncTest`) asserting the table agrees with the spec.
- `src/main/java/ai/foresignal/security/AccessRequirement.java` — the sealed requirement set
  (`Open`, `RequiresScope(scope, alwaysAuthenticated)`, `IngestKeyOrAdmin`, `PeerKey`).
- `src/main/java/ai/foresignal/security/TokenFilter.java` — where the requirement is enforced after the caller is
  resolved (api token or, from R2-003, session).
- R2-001's `Permissions` catalog constants and the built-in role matrix; R2-003's `EffectivePermissions`.

**Build.**

- **Per-operation permission requirement in the contract.** Add an **`x-foresignal-permission`** extension to each
  operation that a session principal must hold a permission to call, as an additive, documented extension (the same
  way `x-foresignal-scope` already describes scope). At minimum:
  - *Identity (R2-005/006):* all `/v1/users/**` ops ⇒ `users.manage`; all `/v1/roles/**` and `GET /v1/permissions`
    ⇒ `roles.manage`.
  - *Platform (R2-007 and later R2 items):* `getSmtpSettings`/`updateSmtpSettings`/`testSmtpSettings` ⇒
    `system.settings`; `getCollectionSources`/`updateCollectionSources` ⇒ `collection.manage`; `listAuditEvents` ⇒
    `audit.read` (already-implemented op — add the extension additively).
  - *Self-service:* `getMySources`/`updateMySources` ⇒ `self.sources`; `getMyPreferences`/`updateMyPreferences` ⇒
    `self.preferences`; `getMyLlmBudget` ⇒ `self.llm`. (R2-008/R2-009 add `self.alerts`/`self.channels` to the
    existing trigger/channel ops.)
  - *Data:* the record/intel/forecast read operations map to `data.read`/`intel.read`/`forecast.read`/`data.export`
    (R2-013 completes the data-access gating; wire the extension on the read/export ops here so one table drives it).
  Where an op has no `x-foresignal-permission`, a session satisfies it on effective scope alone (e.g. generic reads
  under `openReads`).
- **`AccessPolicy` permission table.** Extend the requirement model so a rule can carry an optional required
  permission alongside the scope. Keep `RequiresScope` for machines; add the permission as a parallel field (e.g.
  a new `RequiresPermission(Scope fallbackScope, String permission, boolean alwaysAuthenticated)` or an added field
  on `RequiresScope` — pick the cleaner, record it). Transcribe every `x-foresignal-permission` into the table in
  contract order.
- **Enforcement in `TokenFilter`.** After resolving the caller:
  - **Scoped API token (machine):** gate on scope **only**, as today. An `admin` token satisfies an
    `x-foresignal-permission` operation (machines are not permission-scoped in v2.0 — document this explicitly:
    a permission requirement maps to `admin` scope for tokens). An `agent`/`read` token that lacks the scope gets
    the existing 403 `INSUFFICIENT_SCOPE`.
  - **Session principal (user):** gate on **both** the effective scope and, when the op carries a permission, on
    the user holding that permission. Missing permission ⇒ **403 `PERMISSION_DENIED`** with the required permission
    key in the problem detail. A suspended/disabled/expired session was already rejected at resolution (R2-003).
- **Effective-permission evaluation.** `union of the user's roles' permissions`. Admin role ⇒ full catalog ⇒ passes
  everything. Cache per session (invalidate on role change / session revoke). No per-request DB fan-out on the hot
  path.
- **Spec-sync guard.** Extend `AccessPolicySpecSyncTest` so that for **every** operation in `docs/OpenAPI.json`,
  `AccessPolicy`'s rule has the same `x-foresignal-scope` **and** the same `x-foresignal-permission` (or none).
  This guard asserts the shape of the source and must stay green every pass — it is exactly the kind of guard that
  catches a drift between the contract and the policy table.

**Model / migration.** None — this prompt is policy + enforcement over R2-001's catalog.

**Contract / docs.** Add `x-foresignal-permission` to the operations above (and the already-implemented data/audit
read ops it governs) — additive, no version bump (already 2.0.0). Regenerate Postman only — do **not** regenerate the client SDKs (R2-027 does that once at cutover) and leave every op's authored `x-foresignal-status`. Update `docs/RBAC-Design.md`
(the permission table, the token-vs-session enforcement rule, the `PERMISSION_DENIED` error) and `docs/Invariants.md`
(a session is gated by permissions; an admin machine token bypasses permission gating by design).

**Tests to write (do not run).** `AccessPolicySpecSyncTest` covers scope **and** permission for every op; a session
with only the Viewer role gets 403 `PERMISSION_DENIED` on `createUser` and 200 on a `data.read` op; an Operator
session passes `collection.manage` ops and fails `users.manage`; an Editor session passes `self.*`; an `admin`
api_token passes a `users.manage` op (machine bypass) while an `agent` token gets `INSUFFICIENT_SCOPE`; the
permission set is the union across multiple roles; a role change invalidates the cached permissions. Target ≥ 90%
line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** Operations carry `x-foresignal-permission`; `AccessPolicy` enforces scope for machines and
scope+permission for sessions; missing permission ⇒ 403 `PERMISSION_DENIED`; scoped api_tokens are unchanged; the
spec-sync guard covers permissions; Postman regenerated; docs updated; `checkUsEnglish` green; compilation passes.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-004: Authorization` + `completion-reports/R2-004-CR-Authorization.md`. **Stage only this prompt's
files.** End your reply with `Implementation commit: <hash>`.
