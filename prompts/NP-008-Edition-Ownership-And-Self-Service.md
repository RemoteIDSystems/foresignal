# NP-008 — Edition ownership & self-service

**Context.** API **2.1.1**, authored contract-first in `docs/OpenAPI.json` (already at `info.version`
2.1.1 with the changelog entry). This prompt **implements** that authored delta — it does not design
it. Builds on the newspaper engine (`NP-001`…`NP-007`) and the v2.0 identity/permission model
(R2-001/R2-003/R2-004). The contract already carries, as `planned`, the four self-service edition
operations and the writable `BriefingInput.ownerUserId`; implement and activate them.

**Goal.** Two gaps the newspaper campaign left: (1) an edition's owner can be **assigned** — a
`users.manage` operator may set `ownerUserId` on create/update to stand an edition up for a user;
(2) a normal signed-in user can **self-serve** their own edition (create/update/delete/run) through
`/v1/me/editions`, holding the new `self.editions` permission, without agent scope.

## Operations (authored `planned` in the contract — implement and set `implemented`)

| operationId | method + path | gating |
|---|---|---|
| `createMyEdition` | `POST /v1/me/editions` | `self.editions` |
| `updateMyEdition` | `PUT /v1/me/editions/{briefingId}` | `self.editions` |
| `deleteMyEdition` | `DELETE /v1/me/editions/{briefingId}` | `self.editions` |
| `runMyEdition` | `POST /v1/me/editions/{briefingId}/run` | `self.editions` |

Plus: `BriefingInput.ownerUserId` becomes writable on `createBriefing`/`updateBriefing` (the contract
already shows the field).

## Read first

- The authored 2.1.1 delta in `docs/OpenAPI.json` (the four ops, `BriefingInput.ownerUserId`, the
  2.1.1 changelog note) — the only contract.
- `briefing/` — `BriefingService`, `BriefingRunner`, `BriefingInput`, `BriefingEntity`
  (`owner_user_id` from NP-003), `BriefingsController`, the existing `listMyEditions` controller.
- `security/user/Permissions.java`, `PermissionGroup`, the permission catalog + how built-in roles
  seed permissions; `security/AccessPolicy.java`; how `self.*` permissions gate a session.
- How NP-007 activated ops (`planned`→`implemented`) and regenerated SDKs/Postman; `ApiVersionTest`.

## Build

1. **Writable owner.** Thread `BriefingInput.ownerUserId` into create/update: default to the creating
   user; a caller holding `users.manage` may set it to any existing active user (validate; 400 on an
   unknown user); an API token leaves it null. `Briefing.ownerUserId` stays read-only in responses.
2. **Self-service editions.** A controller for the four `/v1/me/editions` operations: a session user
   holding `self.editions` creates, updates, deletes and runs editions they own, with `ownerUserId`
   forced to the caller and **no agent scope required**. Reuse `BriefingService`/`BriefingRunner`;
   an edition the caller does not own is 404. Writes are audited as the user.
3. **Permission.** Add `self.editions` to the permission catalog (its `self.*` group) and grant it to
   the built-in roles that should self-serve editions (at least the standard signed-in analyst role;
   confirm against the role seed). Add `AccessPolicy` entries for the four new paths (`self.editions`).
4. **Activate + version.** Set the four ops `x-foresignal-status: implemented`. Bump the code release
   to **2.1.1**: `ApiVersionTest.RELEASE` and the Gradle `version` (the spec is already 2.1.1).
5. **Guard fix (required).** `ApiVersionTest.newspaperOperationsAreActive` concatenates only the notes
   of changelog entries whose version equals `RELEASE`; at 2.1.1 the seven `NP-001`…`NP-007` ops
   (named only in the 2.1.0 note) would be flagged "unnamed". Change that check to accept an `NP-`
   operation named in **any** `2.1.x` newspaper changelog note (concatenate every entry whose version
   starts `2.1.`), so both the 2.1.0 and 2.1.1 operations validate.
6. **Regenerate** the SDKs (the `generateSdks` task NP-007 used) and Postman
   (`python3 scripts/generate-postman.py`), bumping the SDK versions to 2.1.1.

**Migration.** If permissions are DB-seeded, add the next free additive Flyway `V{n}__self_editions.sql`
granting `self.editions` to the appropriate roles (read the directory for the next `V`; NP left V33).
`owner_user_id` already exists (NP-003). Additive only.

## Tests to write (do not run)

- `EditionAssignmentIT` — a `users.manage` operator sets `ownerUserId` to another user; a non-operator
  cannot; an unknown target is 400.
- `SelfServiceEditionIT` — a `self.editions` user without agent scope creates/updates/runs/deletes
  their own edition; `ownerUserId` is forced to the caller; another user's edition is 404.
- Extend `ApiVersionTest` for the 2.1.x note-concatenation change; confirm the eleven NP ops all
  validate as implemented and named.

## What runs

Compile only — `./gradlew compileJava compileTestJava`; run `./gradlew checkUsEnglish`. Write tests,
do not run. US English. SDK/Postman regeneration is generation, not the suite.

## Done when

An operator can assign an edition to a user; a `self.editions` user self-serves their own edition
without agent scope; the four ops are `implemented`; release is 2.1.1 in all three spots with the
changelog and the adjusted newspaper guard consistent; SDKs/Postman regenerated; compile and
`checkUsEnglish` green.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; `.gitlab-ci.yml`; other
prompts' files. The 2.1.1 contract in `docs/OpenAPI.json` is **authored** — implement to it; only add
a missing error/field if the implementation needs one, and record it. **Stage only this prompt's files.**

## Commit

`NP-008: Edition ownership & self-service`, then
`completion-reports/NP-008-CR-Edition-Ownership-And-Self-Service.md` with the 40-character hash and a
note that the newspaper feature is at API 2.1.1.
