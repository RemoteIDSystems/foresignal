# Identity & RBAC design

How Foresignal becomes multi-user: identity, authentication, roles and permissions, and per-user
configuration over a shared collection. This is the design the Release 2 prompts (backend 164–176)
build to; `docs/OpenAPI.json` (API **2.0.0**) is the contract, and the code is what is true — where
this page and the code disagree, the code wins and this page is wrong.

The dividing line: **collected data and the collector stay global; who may see and configure what
becomes per-user.** One instance, one database, one paced collector — many accounts, each with its
own view, alerts, channels, LLM budget, saved views and preferences layered on top.

## What exists today (verified 2026-10-26)

Identity today is **capability-scoped tokens only**, not accounts.

| Thing | Where | Shape |
|---|---|---|
| Scopes | `security/Scope.java` | `READ` < `AGENT` < `ADMIN`, ordered, fail-closed |
| API tokens | `security/ApiTokenEntity.java` | hashed `api_token` rows (`fsr_…`), area/path/tile restrictions |
| Authorization | `security/AccessPolicy.java`, `security/TokenFilter.java` | per-operation scope gate; unmatched `/v1/**` needs admin |
| Bootstrap admin | `security/AdminTokenService.java` | one rotatable break-glass admin token, hash-stored, printed once |
| Global config | `store/SourceSettingsEntity.java`, `store/TriggerRuleEntity.java`, `llm/LlmProviderEntity.java` | **no owner column** — settings are instance-wide |
| Owner-scoped data | `workspace`, `shareable_object`, `relation`, `chronicle_*`, `principal` | already carries an owner (the 129/130 collaboration layer) |

So two owner concepts already live in the schema: scoped **tokens** (machine identity) and
collaboration **principals** (`Principal` schema: id, handle, displayName, kind `person`/`agent`/`service`).
RBAC adds a third, the **human account** (`user`), and ties the three together: a user authenticates,
holds roles, owns a principal for collaboration, and may mint scoped tokens for its machines.

Email/SMTP delivery (the channel subsystem) and the USD LLM budget (the 1.5.0 provider work) already
exist and are reused rather than rebuilt.

## A. Identity model

New tables, all additive (Flyway, numbered from the next free version — the latest applied migration
is `src/main/resources/db/migration/V9__source_monthly_cap.sql`):

| Table | Columns (essential) | Notes |
|---|---|---|
| `user` | `id` (uuid), `email` (unique, normalized), `full_name`, `password_hash`, `status`, `created_by`, `created_at`, `updated_at`, `last_login_at`, `mfa_enabled` | email is the primary identity; `password_hash` is argon2id |
| `role` | `id`, `name`, `description`, `built_in` | Admin/Operator/Editor/Viewer/Auditor are `built_in` and undeletable |
| `permission` | `key`, `group`, `description` | fixed catalog, seeded; not operator-editable |
| `user_role` | `user_id`, `role_id` | many-to-many; a user may hold several roles |
| `role_permission` | `role_id`, `permission_key` | many-to-many |
| `user_token` | `id`, `user_id`, `token_hash`, `purpose`, `expires_at`, `used_at` | single-use, expiring activation/reset tokens (hashed) |
| `user_session` | `id`, `user_id`, `token_hash`, `created_at`, `last_seen_at`, `expires_at`, `idle_expires_at`, `user_agent`, `ip_address`, `revoked_at` | the login session bearer (`fss_…`), hashed |

`user.status` is an extensible enum (`User.status` in the contract): **`invited`** (created, not yet
activated) → **`active`** → **`suspended`** (login blocked, sessions revoked, config retained) /
**`disabled`**. The schema is the `User`, `Role`, `Permission` and `Session` schemas in `OpenAPI.json`;
their exact field names (`fullName`, `builtIn`, `roles[].`, `lastLoginAt`, `mfaEnabled`) are the API
surface and must not drift.

## B. Permission catalog and built-in roles

**Effective permissions = the union of the user's roles.** The catalog (`GET /v1/permissions` →
`Permission[]`), grouped:

| Group | Keys |
|---|---|
| Identity | `users.manage`, `roles.manage` |
| Platform / global | `system.settings`, `collection.manage`, `collector.settings`, `llm.shared.manage`, `channels.global`, `storage.manage`, `network.manage`, `audit.read` |
| Self-service | `self.sources`, `self.alerts`, `self.channels`, `self.llm`, `self.views`, `self.preferences`, `self.workspaces` |
| Data | `data.read`, `data.export`, `intel.read`, `forecast.read` |

Built-in roles (seeded; `built_in = true`, undeletable, permission set fixed):

| Role | Holds |
|---|---|
| **Admin** | every permission |
| **Operator** | all Platform/global + all Data + all Self-service; **not** `users.manage`, `roles.manage` |
| **Editor** | all Self-service + all Data |
| **Viewer** | `data.read`, `intel.read`, `forecast.read`, `self.views`, `self.preferences` — truly read-only |
| **Auditor** | `audit.read`, `data.read`, `self.preferences` |

Design decisions locked (owner, 2026-10-25): **Viewer is truly read-only** — self-service alerts and
channels start at Editor. A **custom role** is a named subset of the same catalog (`POST /v1/roles`,
`RoleInput` = name + description + permission keys); built-ins cannot be edited or deleted, custom
roles can. `MFA` is optional for all and **required for Admin/Operator** (`MfaStatus.required` is true
when a held role requires it).

Every authenticated user manages their **own** profile and preferences regardless of role; the
Self-service permissions gate whether a user may own *configuration* (sources view, alerts, channels,
LLM providers, saved views, workspaces), not whether they can set their own name, password or timezone.

## C. Authentication and sessions

Passwords are hashed with **argon2id** through an audited library (Spring Security's
`Argon2PasswordEncoder`) — standard and unexotic, consistent with the crypto posture. No hand-rolled
primitives.

Auth endpoints (all `open` scope except logout, which is `read`):

| Operation | Method / path | Purpose |
|---|---|---|
| `login` | `POST /v1/auth/login` (`LoginRequest`) | email + password (+ MFA code) → a `SessionToken` |
| `logout` | `POST /v1/auth/logout` | revoke the current session |
| `requestPasswordReset` | `POST /v1/auth/password/reset-request` | email a reset link |
| `resetPassword` | `POST /v1/auth/password/reset` | consume a `user_token`, set a new password |
| `activateAccount` | `POST /v1/auth/activate` | consume an invite `user_token`, set the first password |

A successful login mints a **session bearer** `fss_…`, stored only as a hash in `user_session`, with
an **absolute** expiry and an **idle** expiry, revocable on logout, suspend, password change, or admin
action. `TokenFilter`/`AccessPolicy` gain a branch that resolves an `fss_` bearer to a **user
principal** carrying the effective-permission set, in addition to the existing `fsr_` scoped-token
branch. Session management (list/revoke own sessions) is `GET /v1/auth/sessions`,
`DELETE /v1/auth/sessions`, `DELETE /v1/auth/sessions/{id}` (the `Session.current` flag marks the
calling session).

**Scoped `api_token`s stay** for machines and agents; they gain an `owner_user_id` so every token is
attributable to a user, minted and revoked through `GET/POST /v1/me/tokens`, `DELETE /v1/me/tokens/{id}`
with the existing area/path/tile-key restrictions (`TokenRestrictions`). The **bootstrap admin token**
remains the break-glass machine credential and the first-run seed (see F).

`GET /v1/me` returns the calling principal, the token in use, the expanded scopes, groups, and
notification preferences (`Me` schema) — the single call a client makes to learn who it is and what it
may do.

## D. Per-user configuration over a shared substrate

The substrate — sources, the collector, collected records, shared LLM, global channels, server
settings — stays global. **Ownership is added only to the user-facing configuration layer:**

| Entity | Change | Permission |
|---|---|---|
| trigger rules (alerts) | `owner_user_id` (null = instance/shared) | `self.alerts` |
| channels | `owner_user_id`; operator may still own global channels | `self.channels` / `channels.global` |
| LLM providers | `owner_user_id` (null = shared) | `self.llm` / `llm.shared.manage` |
| saved views | `owner_user_id` | `self.views` |
| workspaces | align `workspace.owner` to user ids | `self.workspaces` |
| `user_source_pref` (new) | the user's enabled-source view + per-user display name/color | `self.sources` |
| preferences (new) | timezone, UI, notification prefs | `self.preferences` |

**The collection union is the load-safety heart of the design.** The paced collector pulls a source
**once** if it is in the instance set (`collection.manage`) **and** any user's view enables it
(`user_source_pref.enabled`). One pull feeds everyone; each user's views, alerts and map filter down to
their own enabled set. The shared collection is pulled once and paced — it is **never** duplicated
per user, so adding accounts never multiplies upstream load (`HostPacer`, `BudgetGovernor` are
unchanged).

Per-user endpoints under `/v1/me` (all `read` scope — a session may always read and write its own
config): `GET/PUT /v1/me/sources` (`UserSourceView`, a list of `UserSourcePref`),
`GET/PUT /v1/me/preferences` (`UserPreferences`), `GET/PUT /v1/me/notification-preferences`,
`GET /v1/me/quota` (`QuotaUsage`), `GET /v1/me/llm/budget`.

## E. LLM — shared and personal, per-user budgets

`LlmProviderEntity` gains `owner_user_id`: **null = shared** (admin-managed under `llm.shared.manage`),
**non-null = personal** (the user's own keys under `self.llm`). Usage metering and the USD caps/balance
that already exist (the 1.5.0 provider work) become **per-user-per-provider**: `GET /v1/me/llm/budget`
reports the caller's cap and spend. A user with no personal provider falls back to the shared providers
within whatever budget the admin set. Private-source egress rules (EGR-1, `EgressPolicy`) are unchanged
— a personal remote provider is still a remote provider and never receives private records.

## F. Transactional email

Reuses the channel/SMTP infrastructure. Global SMTP settings (host, port, TLS, credentials,
from-address) live under `system.settings`; the default from-address is `info@remoteidsystems.com`.
Templated, US-English emails:

- **account added** — full name, assigned role(s), and the activation link (`activateAccount`);
- **password reset** — the reset link (`requestPasswordReset`);
- **role changed / suspended** — notice of an account change.

With no SMTP configured it degrades gracefully: the admin is warned, and the activation/reset link is
retrievable by an admin from the user record (`UserCreate.sendInvite = false` returns the link instead
of mailing it). This also folds in the `server.contactEmail` loose end — the outbound User-Agent
contact several adapters ask for (`source/adapters/wikimedia/WikimediaPageviewsAdapter.java`,
`source/adapters/sec/Sec8kAdapter.java`) — set on the same system-settings screen.

## G. User and role management API

Admin-scoped (`admin`), permission `users.manage` / `roles.manage`:

| Operation | Method / path |
|---|---|
| `listUsers` / `createUser` | `GET` / `POST /v1/users` (`UserCreate`: email, fullName, roles, sendInvite) |
| `getUser` / `updateUser` / `deleteUser` | `GET` / `PUT` / `DELETE /v1/users/{id}` |
| `setUserRoles` | `POST /v1/users/{id}/roles` |
| `suspendUser` / `unsuspendUser` | `POST /v1/users/{id}/suspend` · `/unsuspend` |
| `adminResetUserPassword` | `POST /v1/users/{id}/password-reset` |
| `resendUserInvite` | `POST /v1/users/{id}/resend-invite` |
| `getUserQuota` / `updateUserQuota` | `GET` / `PUT /v1/users/{id}/quota` (`Quota`) |
| `listRoles` / `createRole` | `GET` / `POST /v1/roles` |
| `getRole` / `updateRole` / `deleteRole` | `GET` / `PUT` / `DELETE /v1/roles/{id}` |
| `listPermissions` | `GET /v1/permissions` |

**User delete reassigns by default** (owner decision): a deleted user's owned configuration
(alerts, channels, providers, views, workspaces) is reassigned to an admin rather than destroyed, with
the choice surfaced per delete (`UserDeleteResult` reports what moved). Deleting a role that users still
hold is refused; built-in roles cannot be deleted at all.

## H. Audit

Admin/security actions and configuration changes write an append-only `AuditEvent` (actor kind, token,
principal, scope, method, path, operationId, status, target, remote IP, detail). It is exposed to
`audit.read` (the Auditor role) at `GET /v1/audit` (`listAuditEvents`) with filtering, and retained per
`RetentionSettings.audit` (default 90d). The existing 023 audit log is extended, not replaced.

## I. Migration, first-run, and versioning

- **First run, no users.** `GET /v1/system/first-run` (`getFirstRunStatus`) reports the empty state;
  the bootstrap admin token (`AdminTokenService`) creates the first admin account. Existing global
  config becomes the **shared/seed defaults** plus that first admin's personal config.
- **Scoped tokens keep working** throughout — existing `fsr_` tokens are unaffected; they simply gain
  an owner on first attribution.
- **API → 2.0.0.** The per-user semantics of the existing config endpoints are a deliberate, documented
  break (a config list that was instance-wide now returns the caller's own). The frozen v1.0.0 baseline
  at `docs/api/v1.0.0/OpenAPI.json` is kept, and `./gradlew checkApiCompat` records the v2 breaks
  honestly rather than suppressing them.

## Invariants this design must keep

These extend `docs/Invariants.md` (the implementing prompts register them there with their test names):

| Id | Invariant |
|---|---|
| RBAC-1 | Effective permissions are exactly the union of a user's roles; a missing permission fails closed, like an unmatched path today (SEC-1) |
| RBAC-2 | A session bearer never carries more authority than its user's roles grant at request time; suspend/role-change takes effect on the next request (sessions re-resolve, not cache, permissions) |
| RBAC-3 | A user reads and writes only its own per-user config; one user's alerts, channels, providers, views and sources are never visible or mutable by another non-admin user |
| RBAC-4 | The collector pulls each source at most once per cycle regardless of how many users enable it; per-user views never cause an extra upstream fetch (extends SRC-6) |
| RBAC-5 | Passwords are stored only as argon2id hashes; activation/reset/session tokens are stored only hashed and are single-use or expiring |
| RBAC-6 | Personal LLM providers obey the same private-source egress rules as shared providers (EGR-1); per-user budgets are enforced before a spend, like the global cap (SEC-5) |
