# R2-005 — User management API

**Goal.** The admin-facing user CRUD and lifecycle: create (invite), read, update, delete, assign roles, suspend,
unsuspend, resend invite, and admin reset password — all gated by `users.manage`. Deleting a user defaults to
**reassigning** that user's owned configuration to an admin (owner decision, roadmap §Decisions).

**Dependency.** R2-001 (model), R2-002 (status machine, activation/reset tokens, password hashing), R2-003
(sessions + the session-revocation hook), R2-004 (the `users.manage` permission gate). R2-007 sends the emails;
until it lands, issue the tokens and make the plaintext link retrievable by an admin (R2-002), and the CR notes
email delivery as wired-but-pending-R2-007.

**Operations (authored in `docs/OpenAPI.json`, `x-foresignal-status: planned`) — implement to the authored schemas and leave the status `planned`; R2-027 flips the whole v2.0 surface to `implemented` at cutover:** `listUsers`, `createUser`, `getUser`, `updateUser`,
`deleteUser`, `setUserRoles`, `suspendUser`, `unsuspendUser`, `resendUserInvite`, `adminResetUserPassword`. All are
`scope: admin`; add `x-foresignal-permission: users.manage` (R2-004 defines the extension). Schemas already authored:
`User`, `UserCreate`, `UserUpdate`, `RoleAssignment` (the `setUserRoles` body), and the user list response shape —
confirm the exact schema each op references in `docs/OpenAPI.json` and match it.

**Read first.**
- `docs/OpenAPI.json` — the ten `/v1/users/**` operations, their request/response schemas, and (for the
  reassign-on-delete decision) whether `deleteUser` takes a query/body option; implement the **reassign default**
  and surface the chosen target admin in the response.
- R2-001…R2-004 completion reports and the `ai.foresignal.security.user` package (`UserEntity`, `UserTokenService`,
  `UserLifecycleService`, `SessionService`, `EffectivePermissions`).
- The owner-scoped config that a deleted user may own, so reassignment is complete: `owner_user_id` on trigger rules
  (R2-008), channels (R2-009), LLM providers (R2-011), saved views/preferences/workspaces (R2-012), `user_source_pref`
  (R2-010), and user-owned `api_token`s. **Because R2-008…R2-012 may land after this prompt**, implement
  reassignment behind a `UserOwnedConfigReassigner` SPI: this prompt provides the interface and reassigns everything
  that already carries `owner_user_id` today; each later prompt registers its table. Document the contract so the
  later prompts plug in without changing this one.
- `src/main/java/ai/foresignal/security/audit/AuditService.java` — audit every create/update/delete/role-change/
  suspend/reset.

**Build.**

- **`UserService` / `UserAdminController` (`ai.foresignal.api`).**
  - `createUser` — normalize + validate the email (reject duplicates with a 409), create the user `status=invited`
    with no password, assign the requested roles (validate they exist), `created_by` = the acting admin, issue an
    **activation** token (R2-002) and request the **welcome** email (R2-007: full name + role names + activation
    link). Returns `User`.
  - `listUsers` — paged list (match the authored page/response schema); include each user's roles and status.
  - `getUser` — one user with roles/status; 404 if absent.
  - `updateUser` — change `full_name` and (per the `UserUpdate` schema) roles/status where the schema allows; do not
    change email silently unless the schema supports it (if it does, re-normalize and re-check uniqueness). Never set
    a password here.
  - `setUserRoles` — replace the user's role set with the `RoleAssignment` body (validate each role exists);
    invalidate that user's cached effective permissions (R2-004) and, when roles change materially, optionally send
    the role-changed email (R2-007). Returns the updated `User`.
  - `suspendUser`/`unsuspendUser` — drive `active ↔ suspended` (R2-002); suspend revokes the user's sessions
    (R2-003 hook) and may send the suspended email (R2-007).
  - `resendUserInvite` — re-issue the activation token (invalidating the prior one) and re-send the welcome email;
    only valid while the user is `invited` (409 otherwise).
  - `adminResetUserPassword` — issue a reset token and send the reset email (or return/log the retrievable link when
    SMTP is down); revokes the user's sessions. Does **not** reveal or set a password directly.
  - `deleteUser` — **default: reassign.** Reassign all of the user's owned config to a target admin via the
    `UserOwnedConfigReassigner` (choose the target: an explicit admin id in the request if the schema allows,
    otherwise the acting admin, otherwise the oldest/bootstrap admin — document the precedence), then delete the
    user (and cascade `user_role`/sessions/tokens). Refuse to delete the **last** remaining admin (409
    `LAST_ADMIN`). Revoke the user's sessions and tokens. The response reports what was reassigned and to whom.
- **Guards:** an admin cannot suspend/disable/delete themselves into a state with **no** active admin left
  (`LAST_ADMIN`); role assignment cannot leave the instance with zero `users.manage` holders. Bake these in so the
  management session can never lock the instance out.
- **Self-management:** `/v1/users/{id}` where `{id}` is the caller is allowed for an admin, but the last-admin guard
  still applies. Changing one's own name/password flows through R2-012/R2-003, not here.

**Model / migration.** None required beyond R2-001 (the reassigner operates on existing/upcoming `owner_user_id`
columns). If a column is needed to track `created_by` display and it is missing, add **`Vn__user_admin.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive); otherwise no migration.

**Contract / docs.** Implement the ten ops to the authored schemas but **leave their `x-foresignal-status: planned`** (R2-027 flips the surface and regenerates the SDKs once at cutover; regenerate Postman only). Add `x-foresignal-permission: users.manage` to them (additive metadata, allowed mid-release like an added error code). Keep
`AccessPolicy`/spec-sync in agreement. Regenerate Postman (bodies for `createUser`, `updateUser`, `setUserRoles`,
and the reassign option on `deleteUser`). Update `docs/RBAC-Design.md` (user lifecycle, reassign-on-delete, the
last-admin guard) and `docs/Operator-Guide.md` (adding and managing users).

**Tests to write (do not run).** create → invited + activation issued + welcome requested; duplicate email → 409;
list/get; update name and roles; setUserRoles replaces the set and invalidates cached permissions; suspend revokes
sessions and blocks login; unsuspend restores; resendUserInvite only while invited; adminResetUserPassword issues a
reset and revokes sessions without exposing a password; delete reassigns owned config to the chosen admin then
removes the user; last-admin delete/suspend/role-strip → 409 `LAST_ADMIN`; every mutation audited. Target ≥ 90%
line coverage.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** The ten user operations work and are gated by `users.manage`; create invites with a welcome email +
activation; delete defaults to reassign and never removes the last admin; sessions are revoked on suspend/reset/
delete; mutations are audited; ops built to their authored schemas (status stays `planned` until R2-027); Postman regenerated; docs updated; `checkUsEnglish` green;
compilation passes.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-005: User management API` + `completion-reports/R2-005-CR-User-Management.md`. **Stage only this
prompt's files.** End your reply with `Implementation commit: <hash>`.
