# Identity & RBAC design

How Foresignal becomes multi-user: identity, authentication, roles and permissions, and per-user
configuration over a shared collection. This is the design the Release 2 prompts (`prompts/R2-*.md`)
build to; `docs/OpenAPI.json` (API **2.0.0**) is the contract, and the code is what is true — where
this page and the code disagree, the code wins and this page is wrong.

The dividing line: **collected data and the collector stay global; who may see and configure what
becomes per-user.** One instance, one database, one paced collector — many accounts, each with its
own view, alerts, channels, LLM budget, saved views and preferences layered on top.

## What exists today (verified 2026-10-26, after R2-012)

Before Release 2, identity was **capability-scoped tokens only**, not accounts.

| Thing | Where | Shape |
|---|---|---|
| Scopes | `security/Scope.java` | `READ` < `AGENT` < `ADMIN`, ordered, fail-closed |
| API tokens | `security/ApiTokenEntity.java` | hashed `api_token` rows (`fsr_…`), area/path/tile restrictions |
| Authorization | `security/AccessPolicy.java`, `security/AccessPermissions.java`, `security/TokenFilter.java` | per-operation scope gate for tokens, per-operation permission gate for sessions (R2-004); unmatched `/v1/**` needs admin |
| Bootstrap admin | `security/AdminTokenService.java` | one rotatable break-glass admin token, hash-stored, printed once |
| Global config | `store/SourceSettingsEntity.java` | **no owner column** — settings are instance-wide |
| Trigger rules (R2-008) | `store/TriggerRuleEntity.java`, `V16__trigger_rule_owner.sql` | `owner_principal_id` (null = shared); alerts copy it when they fire (§D) |
| LLM providers (R2-011) | `llm/LlmProviderEntity.java`, `V18__llm_ownership_and_user_budget.sql` | `owner_principal_id` (null = shared), per-user caps; `llm_user_usage` per-user metering (§E) |
| Preferences, saved views (R2-012) | `preference/*`, `view/SavedViewEntity.java`, `V19__user_preferences.sql` | `user_preferences (user_id)`: locale, theme, UI bag; `saved_view.owner_principal_id` (null = shared) (§D) |
| Source views (R2-010) | `source/view/UserSourcePrefEntity.java`, `V17__user_source_pref.sql` | `user_source_pref (user_id, source_id)`: a user's opt-out view over the shared collection (§D) |
| Notification channels (R2-009) | `store/NotificationChannelEntity.java` | `owner_principal_id` (prompt 130's column): set = personal, null = global (§D) |
| Owner-scoped data | `workspace`, `shareable_object`, `relation`, `chronicle_*`, `principal` | already carries an owner (the 129/130 collaboration layer) |
| **Identity model (R2-001)** | `security/user/*`, `V14__identity_model.sql` | `user`, `role`, `permission`, `user_role`, `role_permission`, `user_token`; catalog and built-in roles seeded; **no user row, no API change yet** |

So two owner concepts already lived in the schema: scoped **tokens** (machine identity) and
collaboration **principals** (`Principal` schema: id, handle, displayName, kind `person`/`agent`/`service`).
RBAC adds a third, the **human account** (`user`), and ties the three together: a user authenticates,
holds roles, owns a principal for collaboration, and may mint scoped tokens for its machines.

Email/SMTP delivery (the channel subsystem) and the USD LLM budget (the 1.5.0 provider work) already
exist and are reused rather than rebuilt.

## A. Identity model (built: R2-001)

Package `ai.foresignal.security.user`; migration `src/main/resources/db/migration/V14__identity_model.sql`.
All additive. `"user"` and `"group"` are PostgreSQL reserved words and are quoted in SQL (backticks in the JPA
mappings).

| Table | Columns | Notes |
|---|---|---|
| `"user"` (`UserEntity`) | `id uuid`, `email varchar(320)` unique, `full_name`, `password_hash` (null until activation), `status` (default `invited`), `principal_id varchar(26)` → `principal`, `mfa_enabled` (default false), `created_by uuid` (null for the bootstrap first admin), `created_at`, `updated_at`, `last_login_at` | The baseline enables no `citext`, so `UserEntity.normalizeEmail` trims and lower-cases and the unique constraint is on the stored value |
| `role` (`RoleEntity`) | `id uuid`, `name` unique, `description`, `built_in`, `created_at`, `updated_at` | The five built-ins have stable ids `00000000-0000-4000-8000-00000000000{1..5}` |
| `permission` (`PermissionEntity`) | `key` (pk), `"group"`, `description` | Fixed catalog, seeded, read-only at runtime (no setters; nothing inserts) |
| `user_role` (`UserRoleEntity`) | `user_id`, `role_id` (pk both) | cascades on user and role delete; index on `role_id` |
| `role_permission` (`RolePermissionEntity`) | `role_id`, `permission_key` (pk both) | cascades on role delete; `permission_key` → `permission` |
| `user_token` (`UserTokenEntity`) | `id`, `user_id` → `"user"` (cascade), `kind` (`activation`/`reset`), `token_hash varchar(64)`, `token_prefix varchar(8)`, `created_at`, `expires_at`, `consumed_at`, `created_by` | SHA-256 hex only (same form as `api_token.token_hash`), never the plaintext; indexes `(token_hash)`, `(user_id, kind)` |
| `user_session` | (R2-003) | the login session bearer (`fss_…`), hashed |

Decisions recorded:

- **User ↔ principal.** Every user owns exactly one `principal` of kind `person` (the existing kind), stored as
  `user.principal_id`. Annotations, workspaces, sharing and audit keep referring to principals; a session
  (R2-003) resolves to that principal the way a token does.
- **Ids.** `user`, `role` and `user_token` use `uuid`, as the `User`/`Role` contract schemas declare and as
  `api_token` already does; the principal keeps its ULID.
- **Links are explicit entities with an `@IdClass`**, the way every other composite-key table in this codebase is
  mapped (`NotificationReadEntity`, `RasterFrameEntity`, …); there is no `@ManyToMany` anywhere.
- **No user row is created.** An instance with no users keeps working on the bootstrap admin token; first-admin
  seeding is R2-013/R2-027.

`user.status` is an extensible enum (`UserStatus`): **`invited`** (created, not yet activated) → **`active`** →
**`suspended`** (login blocked, sessions revoked, config retained) / **`disabled`**. The contract shapes are `User`,
`Role`, `Permission` and `Session` in `OpenAPI.json`; their field names (`fullName`, `builtIn`, `roles[]`,
`lastLoginAt`, `mfaEnabled`) are the API surface and must not drift.

## B. Permission catalog and built-in roles (built: R2-001)

**Effective permissions = the union of the user's roles.** The catalog is `ai.foresignal.security.user.Permissions`
(Java) and the seeded `permission` rows (SQL); `PermissionCatalogMigrationIT` fails when they differ. Wire groups are
`identity`, `platform`, `self`, `data` (`Permission.group`).

| Group | Keys |
|---|---|
| `identity` | `users.manage`, `roles.manage` |
| `platform` | `system.settings`, `collection.manage`, `collector.settings`, `llm.shared.manage`, `channels.global`, `storage.manage`, `network.manage`, `audit.read` |
| `self` | `self.sources`, `self.alerts`, `self.channels`, `self.llm`, `self.views`, `self.preferences`, `self.workspaces` |
| `data` | `data.read`, `data.export`, `intel.read`, `forecast.read` |

Built-in roles as seeded (`BuiltInRole`; `built_in = true`, undeletable, permission set fixed — R2-006 enforces):

| Role | Holds | Count |
|---|---|---|
| **Admin** | every permission | 21 |
| **Operator** | all `platform` + all `data`; **not** `users.manage`, `roles.manage` | 12 |
| **Editor** | all `self` + all `data` | 11 |
| **Viewer** | `data.read`, `self.views`, `self.preferences` | 3 |
| **Auditor** | `audit.read`, `data.read` | 2 |

**Open question for the owner.** The seeded matrix follows `prompts/R2-001-Identity-Model.md`. The earlier version of
this page (2026-10-26 planner commit `5b38a9f2`) gave a different matrix: Operator also held all `self` permissions,
Viewer also held `intel.read` and `forecast.read`, and Auditor also held `self.preferences`. The two disagree; which
one is right is the owner's call. Changing the seed is one new migration plus `BuiltInRole`; the guard test keeps the
two in step.

Design decisions (owner, 2026-10-25): **Viewer is read-only** — self-service alerts and channels start at Editor. A
**custom role** is a named subset of the same catalog (`POST /v1/roles`, `RoleInput` = name + description +
permission keys); built-ins cannot be edited or deleted, custom roles can. `MFA` is optional for all and **required
for Admin/Operator** (`MfaStatus.required` is true when a held role requires it).

Every authenticated user manages their **own** profile and preferences regardless of role; the
Self-service permissions gate whether a user may own *configuration* (sources view, alerts, channels,
LLM providers, saved views, workspaces), not whether they can set their own name, password or timezone.

## A2. Passwords and the account lifecycle (built: R2-002)

The credential engine, in `ai.foresignal.security.user`; no operation is wired to it yet (R2-003 and R2-005 do that).

**Hashing (`PasswordHasher`).** argon2id through Spring Security Crypto's `Argon2PasswordEncoder` (Bouncy Castle's
argon2; `spring-security-crypto` 7.1.1 from the Spring Boot 4.1.1 BOM, `bcprov-jdk18on` 1.86). Parameters, in one
place: salt 16 bytes, hash 32 bytes, parallelism 1, memory 16,384 KiB (16 MiB), iterations 3. The stored form is the
PHC string `$argon2id$v=19$m=16384,t=3,p=1$…`, so raising the parameters later does not invalidate stored hashes. Raw
passwords are never logged, echoed or stored.

**Policy (`PasswordPolicy`).** At least 12 code points, not blank or whitespace-only, at most 1,024; no composition
rules. Refusal is `400 PASSWORD_POLICY` with one field error. Used by activation, reset and an admin setting a
password; the policy is checked before a token is consumed, so a refused password does not use up the link.

**Status machine (`UserLifecycleService`).**

| From | May become |
|---|---|
| `invited` | `active` (activation), `disabled` |
| `active` | `suspended`, `disabled` |
| `suspended` | `active` (unsuspend), `disabled` |
| `disabled` | — (terminal for sign-in; the row is kept for audit and ownership) |

Anything else is `409 USER_STATUS_CONFLICT`. **Only an `active` user authenticates**; `verify` runs the password
check even for a user who may not sign in, so the time taken does not reveal the status. Suspending, disabling and
setting a password publish `UserSessionsRevokedEvent` (synchronously, in the same transaction) — the hook R2-003's
session store listens to.

**Activation and reset tokens (`UserTokenService`).**

| Kind | Prefix | Default life |
|---|---|---|
| activation | `fsv_` | 72 hours |
| reset | `fsw_` | 1 hour |

Prefix + 32 random bytes base64url (the API token shape). Only the SHA-256 hex and the first 8 characters are
stored; the plaintext is returned once (for the email link, or for an admin to copy when SMTP is down).
**Single-use:** consuming sets `consumed_at`; a replay fails. **Single-active:** issuing deletes the user's earlier
unconsumed tokens of that kind, so an older link stops working when a new one is requested. Every failure — unknown,
other kind, expired, used, or a reset for a user who is no longer active — is the same `400 USER_TOKEN_INVALID`.
**No enumeration:** `requestReset(email)` issues only for an active user and returns empty for every other case; the
caller (R2-003) must answer identically either way.

## C. Authentication and sessions (built: R2-003)

Passwords are argon2id (§A2). The `Auth` operations (`api/AuthController`):

| Operation | Method / path | Scope | Behavior |
|---|---|---|---|
| `login` | `POST /v1/auth/login` | open | email + password → `SessionToken`; every failure `401 INVALID_CREDENTIALS` |
| `logout` | `POST /v1/auth/logout` | read, always authenticated | revokes the calling session |
| `requestPasswordReset` | `POST /v1/auth/password/reset-request` | open | always `202`; a link is issued only for an active account |
| `resetPassword` | `POST /v1/auth/password/reset` | open | consumes a reset token, sets the password, ends every session; bad link `410 RESET_TOKEN_GONE` |
| `activateAccount` | `POST /v1/auth/activate` | open | consumes an activation token, sets the first password, `invited → active`; bad link `410 ACTIVATION_TOKEN_GONE` |
| `listSessions` | `GET /v1/auth/sessions` | read, always authenticated | the caller's live sessions, `current: true` on the calling one |
| `revokeAllSessions` | `DELETE /v1/auth/sessions` | read, always authenticated | revokes every **other** session; the calling one is always kept (`logout` ends it) |
| `revokeSession` | `DELETE /v1/auth/sessions/{id}` | read, always authenticated | revokes one of the caller's sessions; another user's is `404 UNKNOWN_SESSION` |

The four session operations need a signed-in **session**; an API token has no user and gets `401`.

**Sign-in failures are uniform.** Unknown email, wrong password, and an account that is `invited`, `suspended` or
`disabled` all answer the same `401 INVALID_CREDENTIALS` with the same detail and the same argon2id work (an unknown
email is checked against a dummy hash). The reason is written only to the audit log
(`detail.reason`: `unknown_email`, `wrong_password`, `status_<status>`). The contract also lists
`403 ACCOUNT_DISABLED` for `login`; the code does not use it, because the prompt asked for one generic `401` — an
open question for the owner.

**Credential operations are limited** to 10 requests a minute per client address (`429 RATE_LIMITED` with
`Retry-After`), and a failed sign-in or a bad link counts toward the address's auth-failure backoff (§15.2 of the
Architecture), whenever rate limiting is enabled.

**Session bearer.** `fss_` + 32 random bytes base64url, returned once in `SessionToken.token`, stored only as its
SHA-256 hex and 8-character prefix in `user_session` (`V15__user_sessions.sql`). It travels in the existing
`Authorization: Bearer` header (`bearerToken` scheme). Absolute lifetime **12 hours**, idle timeout **2 hours**; each
accepted request moves the idle expiry forward (never past the absolute one), written at most once a minute. A
session ends early on `logout`, `revokeSession`, `revokeAllSessions`, suspension, disabling, or any password change
(`UserSessionsRevokedEvent`). `fss_` is also the prefix of share-link keys (prompt 112); the two never meet, because a
share key is only ever a path segment of `/v1/k/{key}` and a session only a bearer, and each is looked up by hash in
its own table.

**Resolution (`TokenFilter`).** A bearer starting `fss_` goes to `SessionService` before the API-token lookup. It is
accepted only while the session is live and its user is `active` (the session and user rows are read on every
request); the user's permissions come from `PermissionResolver`'s per-user cache (§C2, RBAC-2). The request then carries a `CurrentToken` whose token is the session's token view (id = session id, name
`session:<email>`, no restrictions, the user's principal) and an `AuthenticatedSession` (user id, effective
permissions) under its own request attribute. A session is never accepted on ingest paths (`401`) or peer paths
(which take only `fsp_` keys), and is never a feed or tile key.

**Effective permissions and the derived scope.** `PermissionResolver` takes the union of the user's roles'
permissions. On an operation without an `x-foresignal-permission` (§C2), a session passes the scope gate with a scope
derived from that set (`EffectivePermissions.scope()`):

| The set holds | Scope |
|---|---|
| any `identity` or `platform` permission other than `audit.read` | `admin` |
| otherwise `self.sources`, `self.alerts`, `self.channels`, `self.llm`, `self.workspaces` or `data.export` | `agent` |
| otherwise | `read` |

So Admin and Operator sessions are `admin`, Editor `agent`, Viewer and Auditor `read`. `audit.read` is excluded from
the admin rule on purpose: an Auditor reads the log and does not run the instance. An admin-scoped session sees
private sources, as an admin token does.

**`getMe`.** For a session the `Me` view adds `user` (the `User` shape) and `permissions`, and `token` describes the
session (id, `session:<email>`, derived scope, prefix, absolute expiry). For an API token the view is unchanged.

**Scoped `api_token`s stay** for machines and agents; they gain an `owner_user_id` in R2-025 (`/v1/me/tokens`). The
**bootstrap admin token** remains the break-glass machine credential and the first-run seed (see I).

Audit: sign-ins, failed sign-ins, logout and session revocations are written by `SessionService` (actor kind
`token`, token name `session:<email>`, target `users/<id>`); every other write made with a session is audited by the
token filter with `detail.via = session` and `detail.userId`.

## C2. Authorization (built: R2-004)

**Every operation in `docs/OpenAPI.json` that a signed-in user needs a permission for carries
`x-foresignal-permission`** (526 of the 900 operations). `scripts/generate-access-permissions.py` transcribes the
contract into `security/AccessPermissions.java` — one row per operation, a null permission where it carries none, ordered
literal-before-template — and `AccessPolicy.permissionFor(method, path)` reads it. The permission is a parallel lookup,
not a field on `AccessRequirement.RequiresScope`: the scope requirement, and with it every API token's access, is exactly
what it was before R2-004.

| Permission | Operations (count) | Which |
|---|---|---|
| `users.manage` | 28 | `/v1/users/**`, API tokens, feed and tile keys, pairing codes, principals and their tokens, `runSetup`, admin-token rotation |
| `roles.manage` | 6 | `/v1/roles/**`, `GET /v1/permissions` |
| `audit.read` | 1 | `listAuditEvents` |
| `system.settings` | 80 | the remaining admin-scoped settings and system operations (SMTP included) |
| `collection.manage` | 43 | sources, custom and external sources, ingest, `getCollectionSources`/`updateCollectionSources` |
| `collector.settings` | 3 | `/v1/settings/collector*` |
| `storage.manage` | 24 | archive, backups, resources, record and retention settings |
| `network.manage` | 22 | egress, aggregators, peers |
| `channels.global` | 5 (+11) | escalation policies; and, as the alternative, the 11 channel operations |
| `self.channels` | 11 | every channel operation, with `channels.global` as `x-foresignal-permission-alternatives` (R2-009) |
| `llm.shared.manage` | 12 | shared LLM providers and budgets; the 8 provider operations take `self.llm` as the alternative (R2-011) |
| `data.read` | 118 | record, map, tile, history, search, alert, trigger and reference reads |
| `intel.read` | 112 | intelligence, situations, analysis, assessments, chronicles, analytics, briefings reads |
| `forecast.read` | 8 | forecast reads |
| `data.export` | 11 | the `Exports` tag and `/v1/export/**` |
| `self.alerts` | 18 | every trigger write (create, update, delete, test, snooze, mute, baseline rebuild, tuning) and the alert lifecycle writes (acknowledge, resolve, snooze, feedback); R2-008 |
| `self.sources` / `self.preferences` / `self.llm` | 2 / 2 / 1 (+8) | `getMySources`/`updateMySources`, `getMyPreferences`/`updateMyPreferences`, `getMyLlmBudget`; and, as the alternative, the 8 LLM provider operations |
| `self.views` | 9 | every saved-view operation; the 6 reads (`listViews`, `getView`, records, tiles, TileJSON, stream) take `data.read` as the alternative (R2-012) |
| `self.workspaces` | 10 | every workspace operation; all but `createWorkspace` and `shareWorkspace` take `data.read` as the alternative (R2-012) |

Every admin-scoped operation carries a permission, so no admin-scoped operation is reachable by a session on its
derived scope alone (an Operator, derived scope `admin`, cannot mint an admin API token: that is `users.manage`).
`self.alerts` arrived on the trigger and alert operations with R2-008, `self.channels` on the channel operations with
R2-009 (§D), `self.llm` on the LLM provider operations with R2-011 (§E), and `self.views` and `self.workspaces` on the
saved-view and workspace operations with R2-012 (§D); R2-013 completes data-access gating.

**Alternatives (R2-009).** One operation can serve two permissions: the channel operations manage a user's own channels
(`self.channels`) and the global ones (`channels.global`), and an Operator holds only the second while an Editor holds
only the first. An operation may therefore carry `x-foresignal-permission-alternatives`, further keys any one of which
satisfies it as well; the generator writes them into `AccessPermissions` and `AccessPolicy.acceptedPermissions` returns
the permission followed by its alternatives. A refusal still names the operation's own `x-foresignal-permission` in
`requiredPermission`. Which rows the caller may then touch is the service's decision (§D).

**The enforcement rule (`TokenFilter.checkToken`).**

| Caller | Operation with `x-foresignal-permission` | Operation without |
|---|---|---|
| API token (machine) | scope only: an `admin` token satisfies every permission; a lower token gets `403 INSUFFICIENT_SCOPE` with `requiredScope` | scope only, as before |
| Session (user) | the permission (or one of its alternatives) **alone**: held ⇒ allowed, else `403 PERMISSION_DENIED` with `requiredPermission` | the derived scope (§C) |

Machines are not permission-scoped in v2.0, by design: a permission requirement maps to `admin` scope for tokens.
For a session the permission **replaces** the scope check rather than adding to it. The prompt said "both"; with the
derived scope also applied, an Auditor (derived scope `read`) could never call the admin-scoped `listAuditEvents` its
`audit.read` is for. Replacing it gives the same answer everywhere else, because every admin-scoped operation carries a
permission and the derived scope is computed from the same set. A suspended, disabled, expired or revoked session was
already refused at resolution (§C, `401`).

**`PERMISSION_DENIED`.** `403`, `code: PERMISSION_DENIED`, `requiredPermission: <key>`, detail "This operation needs
the `<key>` permission, which none of your roles grants." Only sessions get it.

**Effective permissions are cached per user** (`PermissionResolver`, Caffeine, at most 10,000 users, 5-minute
time-to-live), so the gate does no permission read on the hot path. An entry is dropped after the change commits:
`UserRolesChangedEvent(userId)` (role assignment, R2-005 publishes it), `RolePermissionsChangedEvent(roleId)` (a role
edit or deletion, R2-006; drops every entry) and `UserSessionsRevokedEvent` (suspension, disabling, a new password).
The time-to-live bounds a change made outside those events, such as a direct SQL edit.

**Known consequence of the seeded matrix (for the owner).** A Viewer holds `data.read` but not `intel.read` or
`forecast.read` (§B), so a Viewer **session** is refused the intelligence and forecast reads (`PERMISSION_DENIED`) that
an **anonymous** caller is served while `security.openReads` is on. The earlier matrix in §B's open question (Viewer
with `intel.read` and `forecast.read`) would not have this; which is right is the owner's call.

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

**Built (R2-008): per-user alerts.**

- **Ownership is the user's principal.** `trigger_rule.owner_principal_id` (V16) holds the owning user's
  `principal_id`, not a `user.id`: users and principals are one-to-one (§A) and `notification_channel` already owned
  channels by `owner_principal_id`, so rule owner and channel owner compare directly. The table above says
  `owner_user_id`; this is that column under the existing convention, and R2-009…R2-012 use the same one. Null means
  **shared**: a rule an admin manages. Every rule that existed before V16 is shared, so nothing stopped firing or
  changed where it delivers; R2-027's first run decides whether they move to the first admin.
- **Who sees and manages what** (`security/user/OwnerScope`). A session sees and manages its user's own rules, and
  what it creates is its own. A session holding `users.manage` (an admin, the last-admin guard's definition) sees and
  manages every rule, shared ones included, and still owns what it creates. API tokens (the machine path, the bootstrap
  admin token included) see and manage every rule as before, and a rule a token creates is **shared**. Anonymous
  callers (`security.openReads`) and feed and tile keys see shared rules only. Another user's rule is `404` on every
  per-rule operation, exactly like an unknown id (`api/support/TriggerOwnershipInterceptor`, which sits in front of
  every `/v1/triggers/{ruleId}…` controller, the KML export included). `self.alerts` gates every trigger write, so a
  Viewer cannot author; trigger reads carry `data.read`. An Operator holds neither `self.alerts` nor `users.manage`, so
  an Operator session does not manage alerts at all.
- **One stream, every owner.** Nothing in the collector or the trigger engine is per user. The engine still evaluates
  every enabled rule against each record once, and per-rule state (cooldowns, anchors, windows) is keyed by rule, so it
  is naturally per user. The alert insert copies the rule's owner into `alert.owner_principal_id` in the same
  statement, so the engine's cached rule cannot attribute an alert to a former owner.
- **Alert visibility.** The audience travels in the request's `ReadRestriction` (`AlertAudience`, never stored), so
  every alert read already restricted there applies it: `listAlerts`, `getAlert` and the lifecycle writes (another
  user's alert is `404`), `streamAlerts` and `/v1/stream`, the Atom/JSON feeds, CAP, the notification inbox and badge,
  the notification topics, and search. A user sees their own alerts and the shared ones; a session holding
  `users.manage` and every API token see all; anonymous callers and path keys see the shared ones. The audience is in
  the restriction's canonical form, so two users never share a cached response or a cursor.
- **Delivery.** `NotificationRouter.fired` sends a user's alert only to channels that user owns
  (`NotificationService.publishOwned`), personal channels (prompt 130) included, with each channel's severity and rule
  filters; a shared rule's alert goes to the instance channels as before. Instance channels never receive a user's
  alert: R2-009 decides which global channels a user's alerts may use.
- **User deletion** (`trigger/TriggerRuleReassigner`, audit name `triggerRules`). Reassigning moves the rules and their
  alerts to the target admin's principal (shared with no target); `reassign=false` deletes the rules and keeps their
  alerts. The engine reloads once the deletion commits.

**Built (R2-009): per-user and global channels.**

- **One column, no migration.** `notification_channel.owner_principal_id` (prompt 130's personal channels) now means
  the owning user's principal for every channel a session creates; null means **global**, the instance-wide channels an
  operator runs, which every channel before R2-009 was (prompt 130's personal notice channels aside). No `global`
  flag was added: `Channel.global` and `Channel.ownerPrincipalId` are derived from the column. The table above says
  `owner_user_id`; the existing column wins, as the prompt says.
- **Who manages what** (`notify/ChannelScope`). The channel operations take `self.channels` or `channels.global`. A
  session sees its own channels and the global ones (marked `global: true`); it manages its own, and the global ones
  only with `channels.global` (`403 PERMISSION_DENIED`, `requiredPermission: channels.global`, otherwise). Another
  user's channel is `404` like an unknown id (`api/support/ChannelOwnershipInterceptor`, in front of every
  `/v1/channels/{channelId}…` controller). A session holding `users.manage` and every API token see and manage all
  channels. On create, `ChannelInput.global` picks the kind; absent, a user holding `self.channels` gets a personal
  channel and anyone else (an Operator) a global one. An API token always creates global channels, as before.
  Escalation policies, which name channels, need `channels.global`, so a user cannot route escalations to another
  user's channel.
- **Delivery resolution.** A user's alert (R2-008) goes to the user's own channels plus the global channels whose
  `ruleIds` name the rule: an operator selects a global channel for a user's rule by listing it there. A global channel
  without `ruleIds` takes shared alerts only, never a user's, so adding users never floods the instance channels. A
  shared rule's alert goes to the global channels as before.
- **User deletion** (`notify/ChannelReassigner`, audit name `channels`). Reassigning moves the channels to the target
  admin. With no target they would become global, so they are also **disabled**; an operator re-enables them on
  purpose. `reassign=false` deletes them as `deleteChannel` does.

Not yet per user: the secondary alert summaries that never took a read restriction (statistics counts, the dossier and
context-summary activity lists, briefings, the operator gauges) still count or list every alert. R2-013 completes
data-access gating.

**Built (R2-010): shared collection and per-user source views.**

- **Two sets.** The instance collection set is `source_settings.enabled`, read and changed under `collection.manage`
  by `getCollectionSources`/`updateCollectionSources` (`updateCollectionSources` applies each listed `inCollection`
  through `SourceService.update`, so `CREDENTIALS_REQUIRED` and parameter checks apply, all or nothing; an unlisted
  source keeps its state). A user's view is `user_source_pref` (V17), keyed `(user_id, source_id)`, read and replaced
  under `self.sources` by `getMySources`/`updateMySources`. A view belongs to a user, so an API token gets `401` there.
- **Opt-out views.** A source with no row is in the view with no overrides, and `updateMySources` replaces the rows (an
  unlisted source returns to the default). The table above said "any user's view enables it"; with an opt-out view a
  user who never touched theirs wants every source, so existing instances keep collecting everything until every
  active user turns a source off. A preference for a source outside the instance set is kept and takes effect while
  the instance collects it.
- **The union pull gate** sits in `CollectorScheduler.DESIRED_SQL`: instance-enabled and wanted by an `active` user, or
  every instance-enabled source while no user is active. Architecture §5.6 has the detail.
- **View filtering** narrows a session's read restriction to its view (`SessionReadNarrowing`), except for holders of
  `users.manage` or `collection.manage`.
- **User deletion** removes the view (`UserSourcePrefReassigner`, audit name `sourceViews`), whether or not the rest is
  reassigned.

**Built (R2-012): per-user preferences, saved views and workspace ownership.**

- **Preferences have one home per member** (`preference/UserPreferencesService`, `getMyPreferences`/
  `updateMyPreferences` under `self.preferences`; a session only, an API token gets `401`). `locale`, `theme` and `ui`
  are the new `user_preferences` row (V19), keyed by `user_id`. `timezone` is **not** a column there: it is the user's
  principal's `timezone`, which `patchPrincipal` already set and personal notification channels already rendered in,
  so a user has one timezone and changing it here also moves their personal channels to it. Reads fill defaults: the
  server timezone, theme `system`, and the instance notification language for the locale. Validation: the timezone
  must be one of the JVM's zone ids, the locale a well-formed BCP 47 tag naming an ISO 639 language (stored in
  canonical form), the theme `system`/`light`/`dark`, `ui` an object of at most 64 KiB.
- **Notification preferences are not forked.** `UserPreferences.notifications` is a summary of, and switches over, the
  user's prompt 130 notification preferences (`principal_notification` and the channels they own,
  `listMyNotificationPreferences`/`replaceMyNotificationPreferences`), which were already keyed by the principal and so
  already by the user. `emailEnabled` covers the email channels and `pushEnabled` the push ones (ntfy, gotify,
  pushover, webpush); each reads false only when the user has channels of that kind and every one is off, and writing
  it turns them all on or off, in the preference and in the channel (`ChannelService.adjustPersonal`). `quietHours`
  (`HH:MM-HH:MM`) reads as the window every preference shares and writes that window, every day, onto each of them. A
  switch left out of a write changes nothing, and with no notification preferences a switch has nothing to change.
- **Saved views are owned** (`saved_view.owner_principal_id`, V19, the convention of §D). Null is **shared**: every view
  from before R2-012 and every view an API token makes, so briefings, chronicles, share links and workspaces that name
  a view keep resolving it. Rules (`view/SavedViewScope`): a session lists and renders its own views and the shared
  ones, and creates, replaces and deletes its own; another user's view is `404 UNKNOWN_VIEW`
  (`api/support/SavedViewOwnershipInterceptor`, by id or slug, in front of every `/v1/views/{viewId}…` controller); a
  shared view is read-only without `users.manage` (`403 PERMISSION_DENIED`). Admins (`users.manage`) and API tokens see
  and manage everything; anonymous readers see shared views. Slugs stay unique across all views. Internal consumers that
  resolve a view by id (briefings, chronicles, share links, the MCP tools, which run as tokens) are not narrowed: they
  were authorized when they were configured. The settings export carries shared views only.
- **Workspace ownership was already the user's.** `workspace.owner` holds the creating caller's principal
  (`ObjectCallers.principalOf`), and a session's token view carries the user's principal, so a signed-in user's
  workspaces have been theirs since R2-003; the column is reused and nothing is backfilled. Sharing (members, prompt
  130 access lists, share links) is unchanged; the operations gain `self.workspaces`, with `data.read` accepted on all
  but create and share so that a Viewer can still open a workspace shared with them.
- **Per-user timezone in rendering.** The Ask assistant writes "now" in the asking user's timezone (`UserZones`), and
  personal notification channels render quiet hours in it; every stored and compared time stays UTC.
- **User deletion.** `preferences` (`UserPreferencesReassigner`) removes the row either way, like the source view.
  `savedViews` (`SavedViewReassigner`) moves the views to the target admin (shared with no target) or deletes them.
  `workspaces` (`collab/WorkspaceReassigner`) makes the target admin the owner, in the workspace and its access record,
  keeping members and grants; `workspace.owner` cannot be empty, so with no admin account the workspaces stay with the
  deleted user's principal, where an admin token can still transfer or delete them. `reassign=false` deletes them.

## E. LLM — shared and personal, per-user budgets

`LlmProviderEntity` gains `owner_user_id`: **null = shared** (admin-managed under `llm.shared.manage`),
**non-null = personal** (the user's own keys under `self.llm`). Usage metering and the USD caps/balance
that already exist (the 1.5.0 provider work) become **per-user-per-provider**: `GET /v1/me/llm/budget`
reports the caller's cap and spend. A user with no personal provider falls back to the shared providers
within whatever budget the admin set. Private-source egress rules (EGR-1, `EgressPolicy`) are unchanged
— a personal remote provider is still a remote provider and never receives private records.

**Built (R2-011): shared and personal LLM providers, per-user metering and budgets.**

- **Ownership is the user's principal**, as for rules, alerts and channels: `llm_provider.owner_principal_id` (V18),
  null = shared. The table above says `owner_user_id`; this is that column under the existing convention. Every
  provider that existed before V18 is shared.
- **Who manages what** (`llm/LlmProviderScope`). The provider operations take `llm.shared.manage` or `self.llm`
  (`x-foresignal-permission-alternatives`); probing, the LLM settings and the usage report stay `llm.shared.manage`
  only. A session sees its own and the shared providers, manages its own, and manages shared ones only with
  `llm.shared.manage`. Another user's provider is `404` (`api/support/LlmProviderOwnershipInterceptor`). On create,
  `shared` picks the kind; absent, a user holding `self.llm` gets a personal provider and anyone else a shared one. An
  API token creates shared providers. A replace keeps the kind. Keys stay encrypted and are never returned.
- **Selection rule.** Ask and the enrichment of a user's alert use the user's first enabled personal chat provider, by
  id, else the shared chat role. Instance tasks and the embedding and speech roles use shared providers only, and only
  a shared provider can hold a role.
- **Metering.** A call made for a user is also counted in `llm_user_usage (provider_id, principal_id, minute)`, a
  separate table, so `llm_usage` and every reader of it are unchanged.
- **Budgets.** Per-user caps are columns on the provider: `per_user_hourly_cap_usd`, `per_user_daily_cap_usd` and
  `per_user_monthly_cap_usd` apply to each user's own spend on a shared provider. I chose columns over a
  `llm_user_budget (user_id, provider_id)` table because no operation sets a cap for one named user. A personal
  provider's existing caps are its owner's. Machine calls keep the instance-level behavior.
- **`getMyLlmBudget`** reports the caller's monthly spend and cap on the enabled shared providers and their own
  providers.
- **User deletion** (`llm/LlmProviderReassigner`, audit name `llmProviders`). Reassigning moves the providers to the
  target admin. With no target they become shared and **disabled**, so one person's key is never silently put behind
  everyone's calls. `reassign=false` deletes them.

## F. Transactional email

Reuses the channel/SMTP infrastructure. Global SMTP settings (host, port, TLS, credentials,
from-address) live under `system.settings`; the default from-address is `info@remoteidsystems.com`.
Templated, US-English emails:

- **account added** — full name, assigned role(s), and the activation link (`activateAccount`);
- **password reset** — the reset link (`requestPasswordReset`);
- **role changed / suspended** — notice of an account change.

**Built (R2-007).** `settings/SmtpSettingsService` stores `SmtpSettings` under the app-setting key `smtp` (no
migration): `enabled`, `host`, `port` (587), `security` (`starttls`/`tls`/`none`), `username`, the password sealed with
`CredentialCipher` (write-only; `secretsSet`), `fromAddress` (`info@remoteidsystems.com`), `fromName` (`Foresignal`).
`getSmtpSettings`/`updateSmtpSettings`/`testSmtpSettings` (`api/SmtpSettingsController`, `system.settings`).
`security/user/TransactionalEmailService` is the `AccountMailer`; it renders the four templates and sends them through
`notify/TransactionalMailTransport` (`SmtpTransactionalMailTransport`, built by `notify/MailSenders`, the same JavaMail
builder the email channel now uses). Where each fires:

| Email | Fired by |
|---|---|
| welcome (full name, role names, activation link + expiry) | `createUser` (unless `sendInvite: false`), `resendUserInvite` |
| password reset (link + expiry, ignore-if-not-you) | `requestPasswordReset` (active account only), `adminResetUserPassword` |
| role changed (new role names) | `setUserRoles`, when the set changed; sent after commit |
| suspended | `suspendUser`; sent after commit |

Links fill `ClientLinks.activate` / `ClientLinks.passwordReset` (new, R2-007) with `{token}` and `{publicBaseUrl}`.
With no usable SMTP (off, or no host) or a failing send, nothing fails: the email is logged as not sent (user id and
token prefix only), the mailer returns false so user management hands the one-time token to the admin (§G), and
`/v1/system` warns `SMTP_NOT_CONFIGURED` while user accounts exist. This also folds in the `server.contactEmail` loose end — the outbound User-Agent
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

**Built (R2-005): the user operations** (`api/UserAdminController`, `security/user/UserAdminService`).

- **Lifecycle.** `createUser` makes an `invited` user with no password, the requested roles (each must exist, else
  `400`), `created_by` = the acting signed-in admin (null for an API token), a person principal, and an activation
  token; it asks `AccountMailer.sendWelcome` (full name, role names, link) unless `sendInvite` is false. A duplicate
  email is `409 EMAIL_TAKEN`. `updateUser` changes only `fullName` and `email` (re-normalized, re-checked). `setUserRoles`
  replaces the set and publishes `UserRolesChangedEvent` (the cached permissions are dropped after commit) and asks for
  the role-changed email when the set changed. `suspendUser`/`unsuspendUser` drive `active ↔ suspended` (R2-002; a
  wrong status is `409 USER_STATUS_CONFLICT`), suspension ending every session and asking for the suspended email.
  `resendUserInvite` replaces an invited user's activation token (`409 ALREADY_ACTIVE` otherwise);
  `adminResetUserPassword` issues a reset token, ends every session (`UserSessionsRevokedEvent.PASSWORD_RESET_ISSUED`)
  and asks for the reset email. No operation shows or sets a password.
- **Links without email.** The mailer reports whether an email left. When it did not (SMTP off or failing, §F; or
  `sendInvite: false`), the one-time token is returned once as
  `UserLinkIssued.token` — on `createUser` under the user's `activation`, and as the `202` body of `resendUserInvite`
  and `adminResetUserPassword`; an emailed link is reported with `token: null`. `UserLinkIssued` is an additive
  contract schema added by R2-005, because the authored `User` and the `202`s had no place for the link.
- **User delete reassigns by default** (owner decision). Before the row is deleted, every `UserOwnedConfigReassigner`
  bean moves the user's rows to the target (or deletes them with `reassign=false`). Target precedence: the acting
  signed-in admin, when that is not the user being deleted; else the oldest other active admin (`created_at`); else
  none, and the rows become instance-owned (owner null). R2-008 registered the first reassigner (trigger rules, §D);
  R2-009 (channels), R2-010 (`user_source_pref`), R2-011 (personal LLM
  providers), R2-012 (views, preferences, workspaces) and R2-025 (user-owned API tokens) each register one; the
  interface's Javadoc is the contract. The role links, sessions and activation/reset tokens go with the user row (`on
  delete cascade`); the user's principal stays, so collaboration history keeps its author. `UserDeleteResult` reports
  the disposition and the target; the audit row (`detail.event = user_deleted`) adds the per-reassigner counts.
- **Last-admin guard.** An admin is an `active` user holding `users.manage` through any role. Deleting, suspending, or
  setting roles without `users.manage` on the last one is `409 LAST_ADMIN`. The check and the change run under one
  transaction-scoped advisory lock. The bootstrap admin token is not counted and always works.

**Built (R2-006): the role operations** (`api/RoleAdminController`, `security/user/RoleAdminService`).

- **The catalog is fixed data.** `listPermissions` returns `Permissions` (Java) grouped `identity`, `platform`,
  `self`, `data`; nothing creates a permission at runtime. `BuiltInRoleSeeder` runs at every start
  (`ApplicationReadyEvent`) and re-inserts any missing permission row, built-in role row or built-in role permission
  link with `on conflict do nothing`, as `V14` seeded them; it never deletes or changes an existing row, and publishes
  `RolePermissionsChangedEvent` after a repair.
- **Built-in roles are immutable.** `updateRole` and `deleteRole` on Admin, Operator, Editor, Viewer or Auditor are
  `409 BUILTIN_ROLE_PROTECTED` — name, description and permissions alike.
- **Custom roles** are a named subset of the catalog: `createRole`/`updateRole` take `RoleInput` (`name` ≤ 100,
  `description` ≤ 500, `permissions`). An unknown key is `400 VALIDATION_FAILED` naming each one; a name equal to
  another role's, ignoring case and including the built-ins, is `409 ROLE_NAME_TAKEN`. `updateRole` replaces the whole
  role; a changed permission set publishes `RolePermissionsChangedEvent` (every cached permission set dropped after
  commit) and is refused with `409 LAST_ADMIN` if it would leave no active holder of `users.manage` or `roles.manage`
  (checked after the change, inside the transaction, under the user-management advisory lock).
- **Deleting** a custom role nobody holds is `204`; one still held is `409 ROLE_IN_USE` with `userCount` — the contract
  has no option to unassign, so the admin moves the users first.
- **`listRoles`** pages by name; `userCount` comes from one grouped query over `user_role` for every role.

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
| RBAC-1 | Effective permissions are exactly the union of a user's roles; a session on an operation with an `x-foresignal-permission` it does not hold gets `403 PERMISSION_DENIED`; an API token is gated by scope only, and an `admin` token satisfies every permission by design (R2-004) |
| RBAC-2 | A session bearer never carries more authority than its user's roles grant: status is read on every request; suspension takes effect on the next request; a role change takes effect when its event invalidates the cached permissions after commit, or within the 5-minute cache lifetime (R2-004 caches, where this row first said re-resolve) |
| RBAC-3 | A user reads and writes only its own per-user config; one user's alerts, channels, providers, views and sources are never visible or mutable by another non-admin user |
| RBAC-4 | The collector pulls each source at most once per cycle regardless of how many users enable it; per-user views never cause an extra upstream fetch (extends SRC-6) |
| RBAC-5 | Passwords are stored only as argon2id hashes; activation/reset/session tokens are stored only hashed and are single-use or expiring |
| RBAC-6 | Personal LLM providers obey the same private-source egress rules as shared providers (EGR-1); per-user budgets are enforced before a spend, like the global cap (SEC-5) |
