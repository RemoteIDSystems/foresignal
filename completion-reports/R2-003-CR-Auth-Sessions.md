# R2-003-CR-Auth-Sessions

| | |
|---|---|
| Prompt | R2-003-Auth-Sessions.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `9722be5f3bdebd7dca30dc18504312f20030fa00` |
| Date | 2026-10-26 |

## What was added

- **`V15__user_sessions.sql`** + `UserSessionEntity`/`UserSessionRepository`: `user_session` (hash + 8-char prefix,
  `created_at`, `last_seen_at`, `expires_at`, `idle_expires_at`, `revoked_at`, `user_agent`, `ip_address`), unique
  index on `token_hash`, index `(user_id, revoked_at)`, cascade on user delete.
- **`SessionService`**: `login` (uniform `401 INVALID_CREDENTIALS`, dummy-hash for unknown emails, reason only in the
  audit), `authenticate` (live session + active user + permissions read per request; idle slide written ≤ 1/min),
  `logout`, `list`, `revoke`, `revokeAll`, and the `UserSessionsRevokedEvent` listener (R2-002's hook) that revokes
  all of a user's sessions. Constants: `fss_` prefix, 12 h absolute, 2 h idle, 1 min touch interval.
- **`EffectivePermissions`** + **`PermissionResolver`** (union of the user's roles) and **`AuthenticatedSession`**
  (request attribute; `asToken()` gives the `CurrentToken` view).
- **`TokenFilter`**: an `fss_` bearer is resolved through `SessionService` before the API-token lookup, on open and
  gated paths; refused on `IngestKeyOrAdmin` paths; peer paths already take only `fsp_`. Audit rows of session writes
  carry `detail.via=session` and `detail.userId`.
- **`AuthController`**: the eight operations to the authored schemas. Credential operations limited to 10/min per
  address via `ClientRateLimiter` (key `auth:ip:…`) and failed credentials counted by `AuthFailureBackoff`
  (both only when rate limiting is enabled). Token failures map to `410 RESET_TOKEN_GONE` / `410
  ACTIVATION_TOKEN_GONE`; policy failures stay `400 PASSWORD_POLICY`; unknown session `404 UNKNOWN_SESSION`.
- **`getMe`**: for a session, `Me` gains `user` (new `UserViews`, the `User` shape) and `permissions`, and `token`
  describes the session. Unchanged for API tokens.
- **`AccountMailer`** + `LoggingAccountMailer` (logs the user id and token prefix only, never the token) until R2-007.
- **`AccessPolicy`**: the eight Auth rules (four `OPEN`, four `READ_ALWAYS_AUTHENTICATED`, `/v1/auth/sessions/*`
  before `/v1/auth/sessions`).
- **`ErrorCodes`**: `INVALID_CREDENTIALS`, `UNKNOWN_SESSION`, `RESET_TOKEN_GONE`, `ACTIVATION_TOKEN_GONE`.
- `UserLifecycleService.resetWithToken(token, password, field)` overload so a refusal names `newPassword`.

## Contract

- The eight Auth operations stay `x-foresignal-status: planned` (R2-027 flips them).
- `bearerToken` description now covers `fss_` sessions (lifetimes, never on ingest/peer).
- `Me` gains optional `user` (`$ref User`) and `permissions` (additive). No changelog entry, no version change.
- Postman regenerated (`./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers
  all ops: True"). `scripts/generate-postman.py` now sends `{{sessionToken}}` on the `/v1/auth` session operations,
  stores it from `login`, and declares it in the collection and the environment.

## Decisions and disagreements for the owner

1. **`fss_` is also the share-link key prefix** (prompt 112). I kept `fss_` as the prompt and the contract say. They
   cannot be confused at runtime (a share key is only a `/v1/k/{key}` path segment, a session only a bearer, each
   looked up by hash in its own table), but a leaked `fss_…` string no longer says which it is. Changing the session
   prefix is one constant (`SessionService.PREFIX`) plus the docs.
2. **`audit.read` does not make a session `admin`.** The prompt's mapping is "any Platform/Identity permission ⇒
   ADMIN"; `audit.read` is in Platform, which would have made every Auditor a full admin under the scope gate. I
   excluded it; Auditor sessions are `read`.
3. **Login: suspended/disabled answer `401 INVALID_CREDENTIALS`, not the contract's `403 ACCOUNT_DISABLED`**, as the
   prompt requires (no disclosure). `ACCOUNT_DISABLED` is therefore never sent.
4. **`revokeAllSessions` always keeps the calling session.** The prompt allowed a query flag to include it, but the
   contract has no such parameter and its summary says "other sessions"; `logout` ends the current one.
5. The four session operations need a **session**; an API token gets `401` (it has no user until R2-025).

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Not run (written only): `EffectivePermissionsTest`, `UserSessionEntityTest` (unit); `AuthSessionsIT` (login
  success and uniform audited failures; revoked/expired/idle-expired sessions refused; suspension ends sessions;
  list/revoke/revokeAll; session ops refuse API tokens; uniform 202 reset request; reset and activation consume once
  and end sessions; `getMe` with session vs token; derived scope refuses a Viewer an agent op; session refused on
  ingest; role change applies to the next request); `AuthRateLimitIT` (429 + `Retry-After` past 10/min on each
  credential op).

## Notes for the next prompt

- R2-004 should gate with `AuthenticatedSession.of(request).permissions()`; API tokens have no permissions.
- `OpenApiContractIT` ("authored minus pending equals generated") was already failing after the planner authored the
  planned R2 operations without code and with an empty `pending-operations.txt`; each R2 controller narrows the gap.
