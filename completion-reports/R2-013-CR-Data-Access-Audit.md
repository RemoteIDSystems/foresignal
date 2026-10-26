# R2-013-CR-Data-Access-Audit

| | |
|---|---|
| Prompt | R2-013-Data-Access-Audit.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `72aed2c74a3faa405f71a8f185ba711025dee327` |
| Date | 2026-10-26 |

## What was added

- **`V20__audit_user_actor_and_data_restrictions.sql`** (V19 was the highest migration on disk):
  - `audit_event.user_id` (uuid) and `audit_event.user_email` (varchar 320), with indexes on `(user_id, id desc)` and
    `(actor_kind, id desc)`. There is deliberately no foreign key: the row still names the user after the user is
    deleted.
  - Trigger `audit_event_no_update`, backed by function `audit_event_append_only`, raises on every UPDATE. Retention
    still deletes old rows. Before adding it I searched `src/main` and `src/test` for an UPDATE of `audit_event`
    (`grep -rni "update audit_event"`) and found none.
  - A new table, `user_data_restriction (user_id)`, holding `allowed_sources`, `allowed_kinds`, `allowed_bbox`,
    `allowed_area` and `allowed_place_ids`. Its foreign key to `"user"` is `on delete cascade`.
- **The user audit actor:**
  - `AuditRecord` gains `userId`, `userEmail`, the constant `USER = "user"` and `asUser(id, email)`. A 14-argument
    constructor keeps every existing caller compiling.
  - `AuditService` writes the two new columns; `AuditEventEntity` and `AuditEventView` carry them.
  - `TokenFilter` records what a session does as actor `user`, with the user's id and email.
  - `TokenFilter` now also audits a **read** refused with `403 PERMISSION_DENIED`, putting `detail.requiredPermission`
    on the row, so an Auditor sees attempted access.
  - `SessionService.auditAuth` records login, logout and session revocation as actor `user`. A sign-in for an unknown
    email is actor `user` with no user id.
  - `AuthController` now audits the account flows: `requestPasswordReset` (`password_reset_requested`, with reason
    `no_active_account` when nothing matched), `resetPassword` (`password_reset`), `activateAccount`
    (`account_activated`), and their 410 failures (`password_reset_failed`, `activation_failed`).
  - When an admin deletes a user, `UserAdminService.delete` attributes the row to the acting admin.
- **Audit query:**
  - `AuditQuery.Filter` and `GET /v1/audit` gain `actorKind`, `userId`, `targetType` and `targetId`.
  - A malformed `userId` is a 400, the same as `tokenId`.
- **Per-user data restriction:**
  - `ReadRestriction.intersect(other)`:
    - sources and kinds are intersected, and null on either side means the other side's set;
    - two regions are intersected into an `area`; an empty intersection lets nothing through;
    - endpoints, rate, filters and audiences stay the session's own.
  - The store is `UserDataRestrictionEntity` and `UserDataRestrictionRepository`.
  - `UserDataRestrictionService`:
    - parses a `dataRestriction` member through the existing `RestrictionValidator`, with `Scope.READ`;
    - accepts only `sources`, `kinds`, `bbox`, `area` and `placeIds`; any other member is a 400;
    - caches by user, and the cache is cleared after commit;
    - implements `SessionReadNarrowing`, so `TokenFilter.accept` intersects every session's read restriction with the
      stored one. Every read path already goes through `RestrictionClamp`: lists, single reads, tiles and exports.
  - `PUT /v1/users/{id}` (`updateUser`) accepts `dataRestriction`:
    - an object sets the restriction;
    - `null` lifts it;
    - leaving the member out leaves the restriction unchanged.
  - `getUser`, `listUsers` and `updateUser` return the restriction under `dataRestriction`; it is null when the user is
    unrestricted.

## Contract

- **Permissions:**
  - **`data.read` added to 21 data-reading operations that had no permission:**
    - `queryFdsnEvents`, `runSavedSearch`, `getObject`, `getObjectDossier`, `listRelatedObjects`, `listSensors`,
      `getAttributions`, `getPlaceWeather` and `getPrecision`;
    - `ask`, `askStream`, `queryGraph`, `findCommonDependencies`, `ownershipChain`, `computeAffectedInfrastructure`,
      `cpaBatch`, `resolveObjects` and `summarizeContext`;
    - `postProductStats`, `postSample` and `sampleTrack`.
  - **`intel.read` added** to `queryAnalogs`, `queryBaseRate` and `computeExposure`.
  - Each of those 24 operations gained a "v2.0" paragraph and, where it was missing, a 403 response.
  - Totals now: `data.read` is on 139 operations and `intel.read` on 115.
  - `AccessPermissions` was regenerated: 900 rows, 550 with a permission (was 526).
  - The static access check printed "0 problems".
- **Schemas:**
  - `AuditEvent.actorKind` gains `user`, and `AuditEvent` gains `userId` and `userEmail`.
  - `listAuditEvents` gains four query parameters and a rewritten description.
  - There is a new `UserDataRestriction` schema. It is used by `User.dataRestriction` (read) and by
    `UserUpdate.dataRestriction` (write, nullable).
  - All of these changes are additive.
- **Status:** the R2 operations stay `x-foresignal-status: planned`. The changelog history and the frozen v1.0.0 spec
  were not touched.
- **Postman** was regenerated. The SDKs were not regenerated.
- **Docs:**
  - `docs/RBAC-Design.md`: the "What exists today" table, the C2 count (550) and its table rows, a new "Data access
    (built: R2-013)" block, and §H "Built (R2-013): the user actor".
  - `docs/Invariants.md`: RBAC-18 (data access) and RBAC-19 (user audit).
- **Tests written, not run:**
  - `DataAccessIT`:
    - a session with no roles is refused with `403 PERMISSION_DENIED` (`requiredPermission` `data.read`) on records,
      record track and object reads, and the refusal is audited as actor `user`;
    - a Viewer can read, and the read is not audited;
    - read and admin tokens are unaffected;
    - a bbox restriction hides out-of-area records from that user only, and an out-of-area single read is 404;
    - a source restriction intersects with the bbox, and `null` lifts it;
    - leaving the member out leaves the restriction unchanged;
    - invalid restrictions are 400 and change nothing.
  - `UserAuditIT`:
    - successful, failed and unknown-email sign-ins are recorded;
    - an admin session's `createUser`, `setUserRoles`, `suspendUser`, `updateSmtpSettings` and `revokeSession` are
      attributed to the admin, with target type and id;
    - neither the SMTP password nor a session token appears anywhere in the table;
    - an Auditor can query the log and a Viewer gets 403;
    - every filter is exercised, and a bad `userId` is 400;
    - an UPDATE is refused;
    - retention drops a 91-day-old user row and keeps an 89-day-old one.
  - Unit tests: `ReadRestrictionIntersectTest` and `UserDataRestrictionServiceTest`.

## Decisions and disagreements for the owner

1. **The prompt names classes that do not exist.** The prompt says to use `RestrictionEnforcer` and
   `ClampedRestrictionEnforcer`. In the code, the clamp is `RestrictionClamp` and its input is `ReadRestriction`, so
   the user restriction plugs in there through `SessionReadNarrowing`. Should the prompt or the code change?
2. **I added data gates beyond R2-004.** The 24 operations listed under Contract read records, objects or derived
   intelligence, but they carried no permission. A user without `data.read` could therefore still read data through
   them. I gated them, which is what "data.read gates data reads" requires. Please confirm the list. In particular:
   - `ask` and `askStream` now need `data.read` as well as their LLM permission;
   - `postProductStats` and `postSample` are POSTs that only read.
3. **The data restriction also applies to admins.** It narrows any session that has one stored, whatever the roles. API tokens are never narrowed by it; a token
   has its own restrictions.
4. **Where the restriction is stored.** It has its own table, holding the same members as a token's read restriction.
   It is not part of the user's source view (R2-010). The two compose: the source view decides which sources a user
   chose to see, and the restriction decides what the user is allowed to see.
5. **The failed-sign-in actor.** A failed sign-in for an unknown email is recorded as actor `user` with a null
   `user_id`. The email that was tried is not stored, nor is the email named in a password-reset request that matched
   no one. Storing them would put arbitrary attacker-supplied text in the log. Say if Auditors need it.
6. **Some events produce two rows, as before this prompt.** Logout and session revocation each write a row from
   `TokenFilter` (the HTTP write) and one from `auditAuth` (the account event). Both are now actor `user`. I kept both
   rather than dropping one that existing audit queries may rely on.
7. **The append-only trigger is new behavior.** Nothing in the code updates audit rows today. But anything done by
   hand (for example a manual redaction) now needs the trigger disabled first.
8. **R2-012's open items 5 and 7 are still open.** Item 5 is who manages shared views. Item 7 is that saved views can
   still be referenced by id in briefings and workspaces. This prompt does not close them.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)".
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- V20 is taken; the next migration is V21.
- To attribute an audit row to a user, call `AuditRecord.asUser(userId, email)`. Any new audit entry for something a
  session does should use it.
- `SessionReadNarrowing` implementations now include `UserDataRestrictionService`. Any new read path must go through
  `RestrictionClamp`, or the user restriction is bypassed.
- Audit rows can no longer be updated; write a new row instead.
