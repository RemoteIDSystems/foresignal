# R2-005-CR-User-Management

| | |
|---|---|
| Prompt | R2-005-User-Management.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `6b12933c0ebf6ad6751ad9ae257c47c6cf0f9cd7` |
| Date | 2026-10-26 |

## What was added

- **`api/UserAdminController`** — the ten operations to the authored schemas: `listUsers` (`UserPage`, ordered by
  email, `status`/`q` filters, opaque cursor), `createUser` (`201 User`), `getUser`, `updateUser` (`fullName`,
  `email` only), `deleteUser` (`UserDeleteResult`, `?reassign`, default true), `setUserRoles` (`RoleAssignment`),
  `suspendUser`, `unsuspendUser`, `resendUserInvite` (`202`), `adminResetUserPassword` (`202`). A non-UUID path id is
  `404 UNKNOWN_USER`.
- **`security/user/UserAdminService`** — the logic: invite (invited, no password, roles validated, `created_by` = the
  signed-in admin or null for a token, person principal, activation token, welcome email); email validated with
  `jakarta.mail.internet.InternetAddress` strict parsing and normalized; `409 EMAIL_TAKEN`; role replacement publishes
  `UserRolesChangedEvent` (R2-004 cache) and requests the role-changed email only when the set changed; suspend
  requests the suspended email; admin reset issues a reset token, ends sessions
  (`UserSessionsRevokedEvent.Reason.PASSWORD_RESET_ISSUED`, new) and requests the reset email.
- **Last-admin guard** — "admin" = `active` user holding `users.manage` through any role. Delete, suspend, and a role
  set without `users.manage` on the last one ⇒ `409 LAST_ADMIN`. Serialized by `pg_advisory_xact_lock` (the pattern
  `ChronicleService` uses). Counting queries in `UserRepository` read the tables, not the cache.
- **`UserOwnedConfigReassigner` SPI** — `name()`, `reassign(owner, target)`, `delete(owner)`; contract in its Javadoc.
  Collected through `ObjectProvider` so zero beans is fine. **No implementation today**: `grep -rn owner_user_id
  src/main/resources/db/migration` returns nothing, so no table carries an owner yet. R2-008/009/010/011/012/025
  register theirs. Target precedence: acting signed-in admin (if not the deleted user) → oldest other active admin →
  null (instance-owned). Role links, sessions and user tokens cascade with the row (FKs `on delete cascade` in
  V14/V15, checked); the principal is kept. An audit row (`detail.event = user_deleted`) records disposition, target
  and per-reassigner counts; every other write is audited by the token filter.
- **`AccountMailer`** gains `sendWelcome`, `sendRoleChanged`, `sendSuspended`; `sendWelcome`/`sendPasswordReset` now
  return whether the email left. `LoggingAccountMailer` returns false (sends nothing until R2-007).
- `UserEntity.touch`, `ErrorCodes` `EMAIL_TAKEN`, `UNKNOWN_USER`, `LAST_ADMIN`, `ALREADY_ACTIVE`.

## Contract

- **New additive schema `UserLinkIssued`** `{kind, emailed, token|null, tokenPrefix, expiresAt}`; `User` gains optional
  `activation` (only on `createUser`); the `202`s of `resendUserInvite`/`adminResetUserPassword` gain it as content.
  Reason: the prompt and RBAC-Design require an unsent link be retrievable by the admin, and the authored schemas had
  no field for it.
- `409` responses added: `setUserRoles` (`LAST_ADMIN`), `unsuspendUser` (`USER_STATUS_CONFLICT`); `suspendUser`'s
  409 description now also names `USER_STATUS_CONFLICT`.
- `x-foresignal-permission: users.manage` was already present from R2-004. Status stays `planned`; no version change.
- Postman regenerated ("936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True"): bodies for
  `createUser`/`updateUser`/`setUserRoles`, `/v1/users/{id}` uses `{{userId}}` (stored by `createUser`), `deleteUser`
  carries the optional `reassign` query parameter.
- Static policy emulation over all 900 operations: 0 scope and 0 permission mismatches.

## Decisions and disagreements for the owner

1. **The link is returned in the response when not emailed**, including when `sendInvite: true` but no sender exists.
   This means an admin with `users.manage` can activate an invited account themselves; that is what "retrievable by an
   admin" implies, but it is your call.
2. **No admin account ⇒ reassigned config becomes instance-owned (`owner_user_id` null)**, which RBAC-Design §D
   defines as shared. A private alert of a deleted user would then become shared. The alternative is to refuse the
   reassign in that case.
3. `adminResetUserPassword` works for any status (the contract lists no 409); it always ends the user's sessions.
4. The last-admin guard does not count the bootstrap admin token (it is not a user).

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Not run (written only): `UserAdminIT` — invite (status, no password, activation token, welcome email), link returned
  when not emailed, `409 EMAIL_TAKEN` and `400`s, list paging/filters/get/404, update name+email and 409, setUserRoles
  replaces + cache invalidated + email, suspend revokes sessions and blocks login / unsuspend, resend only while
  invited and old token dead, admin reset revokes sessions without exposing a password, delete reassigns to the actor /
  to the oldest admin / deletes with `reassign=false`, `LAST_ADMIN` on delete/suspend/role-strip, unguarded non-admin
  delete, every mutation audited, Viewer session `PERMISSION_DENIED`.

## Notes for the next prompt

- R2-006 must publish `RolePermissionsChangedEvent` on role edits and should apply the same last-admin rule to a role
  edit that removes `users.manage` (use `UserRepository.countActiveUserManagersExcept`).
- R2-007 replaces `LoggingAccountMailer` (implement `AccountMailer`, return true when sent).
