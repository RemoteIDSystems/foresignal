# R2-003 — Authentication & sessions

**Goal.** Let users sign in with email + password and carry a session bearer token the rest of the API accepts
alongside the existing scoped API tokens. Wire the `/v1/auth/*` operations, add a hashed, revocable `user_session`,
and teach `TokenFilter`/`AccessPolicy` to resolve a session to a **principal carrying effective permissions**.
`getMe` (already implemented) must additionally reflect the authenticated user and their effective permissions.

**Dependency.** R2-001 (identity model), R2-002 (password hashing, status machine, activation/reset tokens).
The fine-grained **permission** gate for admin/self-service operations lands in R2-004; this prompt makes a
session a first-class principal and keeps scope-based access working.

**Operations (authored in `docs/OpenAPI.json`, `x-foresignal-status: planned`, `x-foresignal-prompt: R2-003`) — implement to the authored schemas and leave the status `planned`; R2-027 flips the whole v2.0 surface to `implemented` at cutover:**
`login`, `logout`, `requestPasswordReset`, `resetPassword`, `activateAccount`, `listSessions`, `revokeSession`,
`revokeAllSessions`. Paths/schemas are already authored (2.0.0): `LoginRequest`→`SessionToken`,
`PasswordResetRequest`, `PasswordReset`, `AccountActivation`, `Session`. `SessionToken` carries
`{token,tokenPrefix,userId,issuedAt,expiresAt,idleExpiresAt,mfaRequired,scopes,permissions}`.

**Read first.**
- `docs/OpenAPI.json` — the eight Auth operations above (note `login`/`requestPasswordReset`/`resetPassword`/
  `activateAccount` are `scope: open`, `security: [{}]`; `logout`/`listSessions`/`revokeSession`/`revokeAllSessions`
  are `scope: read`) and the `SessionToken`/`Session` schemas.
- `src/main/java/ai/foresignal/security/TokenFilter.java` — how a bearer is resolved: `tokens.authenticate(bearer)`
  → `CurrentToken`, then `AccessPolicy.requirementFor(method,path)` is enforced (lines ~179–260). A session token
  must be resolved here **before** the api-token path, by prefix.
- `src/main/java/ai/foresignal/security/TokenService.java`, `AuthenticatedToken.java`, `CurrentToken.java`,
  `security/restrict/RequestPrincipal.java` — the principal objects a request carries.
- `src/main/java/ai/foresignal/security/Scope.java` — `READ < AGENT < ADMIN`.
- `src/main/java/ai/foresignal/api/CollaborationController.java` (`getMe`, `/v1/me`) and the `Me` schema.
- `src/main/java/ai/foresignal/security/audit/AuditService.java` — record login, logout, failed login, session
  revocation (R2-013 extends the actor model; use it now for auth events).
- `components/securitySchemes/bearerToken` in `docs/OpenAPI.json`.

**Build.**

- **`user_session` / `UserSessionEntity`** (`ai.foresignal.security.user`): `id` (uuid, pk), `user_id` (fk),
  `token_hash` (SHA-256 of the session bearer; never the plaintext), `token_prefix` (first 8 chars),
  `created_at`, `last_seen_at`, `expires_at` (absolute), `idle_expires_at` (sliding), `revoked_at` (nullable),
  `user_agent`, `ip_address`. Index `(token_hash)` and `(user_id, revoked_at)`.
- **Session bearer.** `fss_` + base64url(32 random bytes), returned once in `SessionToken.token`, stored hashed
  via `TokenHasher`. Carried in the **existing** `Authorization: Bearer …` header under the `bearerToken` security
  scheme — update that scheme's description in `docs/OpenAPI.json` to note that a session token (`fss_…`) is
  accepted there too (additive doc change, no new scheme). Absolute lifetime **12h**, idle timeout **2h**
  (constants, documented); each accepted request slides `idle_expires_at` and updates `last_seen_at` (throttle the
  write to at most once/minute per session, mirroring `api_token.last_used_at`).
- **`SessionService`:** `login(email,password,userAgent,ip)` → verify the user is `active` and the password matches
  (R2-002), mint a session, set `last_login_at`, audit success. Invalid credentials, or a non-`active` user, return
  **401 with a single generic message** (no user-existence disclosure) and audit the failure. If MFA is later
  required (R2-024) the response sets `mfaRequired=true` and withholds a usable session until verify — for now
  always `mfaRequired=false`. `logout` revokes the current session. `listSessions` returns the user's live sessions
  (`Session` shape; mark the calling session `current:true`). `revokeSession(id)` revokes one of the caller's own
  sessions (404 if not theirs). `revokeAllSessions` revokes all the caller's sessions except, by default, the
  current one (document the exact behavior; keep the current session unless a query flag says otherwise).
- **Effective permissions.** A session's principal carries the **union of its user's roles' permissions**
  (`EffectivePermissions` resolved from `user_role`→`role_permission`). Expose it on the request principal so R2-004
  can gate operations and so `getMe`/`SessionToken` can report it. Also derive an **effective `Scope`** for the
  existing scope gate so a session passes scope-gated operations without R2-004: map the permission set to a scope —
  any *Platform*/*Identity* permission ⇒ `ADMIN`; else any write-capable *Self-service*/`data.export` permission ⇒
  `AGENT`; else `READ`. Record this mapping in `docs/RBAC-Design.md`.
- **`TokenFilter` integration.** Before the api-token lookup, if the bearer starts with `fss_`, resolve it through
  `SessionService` to a `CurrentToken`-equivalent principal (same request attribute contract as today) carrying the
  user id, principal id, effective scope and effective permissions. Reject revoked/expired/idle-expired sessions as
  401. A session is **never** valid for ingest/peer/feed-key paths. Scoped api_tokens are unchanged.
- **Password reset / activation endpoints.** `requestPasswordReset{email}` → always 202 regardless of whether the
  email exists (enumeration-safe); when it exists, issue a reset token (R2-002) and hand it to the email sender
  (R2-007 — until R2-007 lands, log that an email would be sent and make the plaintext retrievable by an admin per
  R2-002). `resetPassword{token,newPassword}` and `activateAccount{token,password,...}` consume the token (R2-002),
  set the password, revoke existing sessions, and (activation) move `invited → active`. Rate-limit `login`,
  `requestPasswordReset`, `resetPassword` and `activateAccount` per client IP (reuse `security/ratelimit`), e.g. a
  small burst/minute, then 429 with `Retry-After`; audit every failure.
- **Session revocation hooks.** Suspend/disable (R2-005) and password change (R2-002) revoke a user's sessions —
  implement the listener/hook here so those prompts just fire it.
- **`getMe`.** For a session principal, additionally populate the authenticated user and their effective permissions.
  The `Me` response shape stays backward-compatible (additive): include the user and permissions without removing
  the existing `principal`/`token`/`scopes` fields.

**Model / migration.** Add **`Vn__user_sessions.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive) for `user_session`. Entities validate under
`ddl-auto: validate`.

**Contract / docs.** Implement the eight Auth ops to the authored schemas but **leave their `x-foresignal-status: planned`** — R2-027 flips the whole v2.0 surface to `implemented` and regenerates the client SDKs once at cutover; regenerate Postman only. Update the `bearerToken` scheme description to cover
`fss_` sessions. Keep `AccessPolicy` in agreement with every operation's `x-foresignal-scope` (the Auth ops' rules:
the four `open` ones need no credential; the four `read` ones are always-authenticated — add them to `AccessPolicy`
in contract order, specific before general, `/v1/auth/sessions/{id}` before `/v1/auth/sessions`). Regenerate Postman
(`./gradlew generatePostman`; add a `sessionToken` environment variable the Auth-gated requests use). Update
`docs/RBAC-Design.md`, `docs/Operator-Guide.md` (signing in; session lifetimes; revoking sessions) and
`docs/Invariants.md` (sessions stored only as hashes; only `active` users log in; a session never satisfies
ingest/peer).

**Tests to write (do not run).** login succeeds for an active user and fails (401, generic, audited) for bad
password / suspended / disabled / unknown email; a session bearer authenticates a `read` request and is rejected
once revoked, expired, or idle-expired; `listSessions` marks the current one; `revokeSession` only touches the
caller's own sessions; `revokeAllSessions` keeps the current one by default; reset-request is 202 for unknown and
known emails alike; resetPassword/activateAccount consume once and revoke sessions; a session principal's effective
permissions equal the union of its roles; the derived scope matches the mapping; rate limits return 429; `getMe`
reports the user and permissions for a session and is unchanged for a token. `AccessPolicySpecSyncTest` still passes
for the new Auth rules. Target ≥ 90% line coverage.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** A user logs in and receives an `fss_` session; the session authenticates API calls via the existing
bearer scheme with absolute + idle expiry and revocation; `listSessions`/`revokeSession`/`revokeAllSessions`/`logout`
work; reset and activation flows set passwords safely and revoke sessions; `getMe` reflects the user and effective
permissions; scoped api_tokens still work; the eight Auth ops are built to their authored schemas (status stays `planned` until R2-027); Postman regenerated; docs updated;
`checkUsEnglish` green; compilation passes.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-003: Authentication and sessions` + `completion-reports/R2-003-CR-Auth-Sessions.md`. **Stage only
this prompt's files.** End your reply with `Implementation commit: <hash>`.
