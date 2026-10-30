# NP-008-CR-Edition-Ownership-And-Self-Service

| | |
|---|---|
| Prompt | NP-008-Edition-Ownership-And-Self-Service.md |
| Status | Complete (compile only; `compileJava`/`compileTestJava` green; `checkUsEnglish` run and green; `generateSdks` and Postman run; no test was run) |
| Implementation commit | `e6834c83217cedc5b03c6022c303b314c9e9e2a0` |
| Date | 2026-10-30 |

**The newspaper feature is at API 2.1.1.** The four self-service edition operations are `implemented` and the
writable `BriefingInput.ownerUserId` is honored. The contract was authored first; this prompt implemented it.

## What was added

- **Writable owner** (`BriefingService.create`/`update`, each gaining a `boolean mayAssign` overload; the old
  signatures delegate with `false`). `resolveOwner` takes the body's `ownerUserId` only when the caller may assign it
  and it names an **active** user (`UserStatus.ACTIVE`), else a fallback: the creating user on create
  (`viewer(caller).userId()`, null for an API token), the edition's current owner on update. An unknown or inactive
  target is `400 VALIDATION_FAILED`. `Briefing.ownerUserId` is already read-only in responses (NP-003), so no view
  change.
  - `BriefingsController.createBriefing`/`updateBriefing` pass `mayAssign`, computed as "a session holding
    `users.manage`" (`mayAssignOwner`). `updateBriefing` lets such a session reach any edition (it bypasses the
    ownership `404`) so it can reassign; every other caller still reaches only its own and leaves the owner.
- **Self-service editions** (`EditionsController`, now also holding `McpCallerResolver`):
  - `createMyEdition` `POST /v1/me/editions` (201), `updateMyEdition` `PUT /v1/me/editions/{id}` (200),
    `deleteMyEdition` `DELETE /v1/me/editions/{id}` (204), `runMyEdition` `POST /v1/me/editions/{id}/run` (202).
  - Each requires a signed-in session (`requireSession` → `401` for an API token) and reuses `BriefingService`
    with `mayAssign = false`, so `ownerUserId` is forced to the caller (create defaults to the session user; update
    keeps it; an admin using these still owns them). An edition the caller does not own is `404`
    (`visibleTo`/`requireVisible`). No agent scope is required.
  - Writes run as the session user through the existing request pipeline (the synthetic `session:<email>` token), the
    same audited path as the briefing operations — no new audit code.
- **Permission** `Permissions.SELF_EDITIONS` (`self.editions`, group `self`, "Own newspaper editions"). Added to the
  catalog; `BuiltInRole.ADMIN` (all permissions) and `BuiltInRole.EDITOR` (all `self` + `data`) gain it through the
  group, so no `BuiltInRole` change was needed.
- **Migration `V33__self_editions.sql` (additive):** seeds the `self.editions` permission row and grants it to Admin
  and Editor, with `on conflict do nothing`. It is the first catalog permission added since V14.
- **Access policy:** four `AccessPolicy` scope rules for the new paths (`READ_ALWAYS_AUTHENTICATED`), placed with the
  other `/v1/me/editions` rules; the permission half (`AccessPermissions`) regenerated from the contract by
  `scripts/generate-access-permissions.py` (911 rows, the four new ones → `self.editions`).
- **Activation & version:** the four ops `planned` → `implemented` in `docs/OpenAPI.json`; release 2.1.1 in
  `info.version` (already authored), `ApiVersionTest.RELEASE` and `build.gradle.kts`.
- **Newspaper activation guard (required fix):** `ApiVersionTest.newspaperOperationsAreActive` now concatenates every
  changelog note whose version starts `2.1.`, not only the release's, so the seven 2.1.0 ops (named in the 2.1.0 note)
  and the four 2.1.1 ops (named in the 2.1.1 note) all validate; the count floor is now eleven.
- **Regenerated:** the Dart, TypeScript and Python SDKs (`generateSdks`) — new `createMyEdition`/`updateMyEdition`/
  `deleteMyEdition`/`runMyEdition` clients and the writable `ownerUserId` — and the Postman collection
  (`scripts/generate-postman.py docs/OpenAPI.json postman`). All SDK manifests/READMEs bumped 2.1.0 → 2.1.1 (the Dart
  `pubspec.yaml` is `.openapi-generator-ignore`d, so hand-maintained like the rest).

## Tests written (not run)

- `briefing/EditionAssignmentIT` — operator (users.manage) assigns on create and reassigns on update; an omitted
  `ownerUserId` on update keeps the owner; a non-operator cannot assign (forced to self); unknown and suspended
  targets are 400.
- `briefing/SelfServiceEditionIT` — a read-scope user whose only role holds `self.editions` + `data.read` self-serves
  through `/v1/me/editions` (and is `403` on `POST /v1/briefings`); full create/update/run/delete lifecycle;
  `ownerUserId` forced to the caller; another user's edition 404; an API token 401.
- `ApiVersionTest` — the 2.1.x note-concatenation change and the count floor of eleven.
- **Catalog-size guards updated** (the catalog grew to 22 permissions): `IdentityModelTest.catalogKeys` (22 keys,
  `self.editions` in the `self` group), `DatabaseCleanerIT` (`permission` count 22), and
  `PermissionCatalogMigrationIT` (the catalog/role/rerun checks now migrate through V33 and the seed-statement reader
  covers V14 + V33). Guards derived from `Permissions.values().length`/`BuiltInRole` (RoleAdminIT,
  EffectivePermissionsTest, IdentityModelIT) reconcile automatically.

## Decisions / owner questions

- **"An API token leaves it null."** Implemented as: the body's `ownerUserId` is honored only for a **session**
  holding `users.manage` (permissions are session-only). An instance/bootstrap token therefore never assigns, and its
  creates leave the owner null — satisfying the clause. A user's **personal** API token continues to own the editions
  it creates, per NP-003's existing behavior (`viewer(caller).userId()`), which no test contradicts. If the intent is
  that a personal API token's editions must also be unowned, say so and it is a one-line change to the create default.
- **Operator reassignment on update** required letting a `users.manage` session reach another user's edition through
  `updateBriefing` (it otherwise 404s by ownership). The bypass is limited to `mayAssign` callers; an omitted
  `ownerUserId` leaves the owner unchanged, so an operator edit never silently transfers an edition.
- The catalog-size guards above were not named in the prompt's "tests to write", but the new permission necessarily
  changes what they assert; they are updated in the same pass rather than left red.

## Gate

- `./gradlew compileJava compileTestJava` — green.
- `./gradlew checkUsEnglish` — green.
- `./gradlew generateSdks` — built successfully (pre-existing `EdpGeometry`/`Aggregator` generator warnings only).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` — 947 requests, covers all ops.
- No test suite, `lintOpenApi`, or `checkApiCompat` was run (per the campaign's compile-only gate).
