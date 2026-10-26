# R2-006-CR-Role-Management

| | |
|---|---|
| Prompt | R2-006-Role-Management.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `6744974f60f54463a6747fde2c58a3e2996c9174` |
| Date | 2026-10-26 |

## What was added

- **`api/RoleAdminController`** — `listRoles` (`RolePage`, by name, opaque cursor), `createRole` (`201 Role`),
  `getRole`, `updateRole`, `deleteRole` (`204`), `listPermissions` (`PermissionList`). Non-UUID id ⇒ `404
  UNKNOWN_ROLE`.
- **`security/user/RoleAdminService`** — custom roles validated against `Permissions` (unknown keys ⇒ `400
  VALIDATION_FAILED`, one field error per key, all named in the detail); names unique ignoring case, built-ins
  included (`409 ROLE_NAME_TAKEN`); built-ins refuse every update and delete (`409 BUILTIN_ROLE_PROTECTED`); a held
  role refuses delete (`409 ROLE_IN_USE`, `userCount` property); a changed permission set publishes
  `RolePermissionsChangedEvent` (R2-004 cache, after commit) and is refused with `409 LAST_ADMIN` when it would leave
  no active holder of `users.manage` or `roles.manage` (under R2-005's advisory lock). `userCount` for the list comes
  from one grouped query (`UserRoleRepository.countUsersByRole`).
- **`security/user/BuiltInRoleSeeder`** — on `ApplicationReadyEvent`, re-inserts missing permission rows, built-in role
  rows and built-in role permission links with `on conflict do nothing` (never deletes or alters), warns, and
  publishes `RolePermissionsChangedEvent(null)` after a repair.
- Repository additions: `RoleRepository.findByNameIgnoreCase`, `UserRepository.countActiveHolders(permission)`.
- `ErrorCodes`: `UNKNOWN_ROLE`, `ROLE_NAME_TAKEN`, `BUILTIN_ROLE_PROTECTED`, `ROLE_IN_USE`.
- No migration: `user_role` already has an index on `role_id` (V14, R2-001).

## Contract

- `updateRole` 409 description now "Conflict (BUILTIN_ROLE_PROTECTED, ROLE_NAME_TAKEN, LAST_ADMIN)"; `deleteRole`
  409 "Conflict (ROLE_IN_USE, BUILTIN_ROLE_PROTECTED)". `x-foresignal-permission: roles.manage` was already there
  (R2-004). Status stays `planned`.
- Postman regenerated ("936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True"): bodies for
  `createRole`/`updateRole`, `{{roleId}}` stored by `createRole`.
- Static policy emulation over all 900 operations: 0 mismatches.

## Decisions and disagreements for the owner

1. **Unknown permission key is `400`, not the prompt's `422`.** The authored contract lists `400 VALIDATION_FAILED`
   for `createRole`/`updateRole` and no 422; I followed the contract. The prompt and the contract disagree — which
   should change?
2. **The built-in code is `BUILTIN_ROLE_PROTECTED`** (the prompt) where the authored `updateRole` description said
   `BUILTIN_ROLE`; I changed the description to match the code.
3. **No unassign-on-delete option**: the prompt allowed an opt-in, the contract has no parameter, so `deleteRole` on a
   held role is always `409 ROLE_IN_USE`.
4. Built-ins are fully immutable, description included, as the prompt says.
5. The prompt's "Done when" says "ops are `implemented`", which contradicts its own Contract section and the run
   sequence ("leave `planned`"); I left them `planned`.

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Not run (written only): `RoleAdminIT` — list with `userCount` and paging; create from a subset and read; unknown key
  400 naming it; duplicate and built-in name clash 409; update invalidates holders' cached permissions; built-in
  update/delete 409 and its permissions untouched; delete custom; in-use 409 with `userCount`; `LAST_ADMIN` on a role
  edit removing the only `users.manage`; full catalog grouped in order; a deleted built-in re-seeded with its
  permissions (and a second seed inserts nothing); Operator session `PERMISSION_DENIED`, Admin session allowed.
