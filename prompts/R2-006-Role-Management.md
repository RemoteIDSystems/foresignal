# R2-006 — Role management API

**Goal.** Manage roles and expose the permission catalog: list/create/read/update/delete roles and list
permissions, gated by `roles.manage`. Built-in roles are **protected** — they cannot be deleted and their
identity/permission set cannot be edited into something that breaks the matrix. Custom roles are a named subset
of the catalog.

**Dependency.** R2-001 (`role`, `permission`, `role_permission`, the five built-ins seeded), R2-004 (the
`roles.manage` gate and the effective-permission cache to invalidate on change).

**Operations (authored in `docs/OpenAPI.json`, `x-foresignal-status: planned`) — implement to the authored schemas and leave the status `planned`; R2-027 flips the whole v2.0 surface to `implemented` at cutover:** `listRoles`, `createRole`, `getRole`, `updateRole`,
`deleteRole`, `listPermissions`. All `scope: admin`; add `x-foresignal-permission: roles.manage`. Schemas already
authored: `Role{id,name,description,builtIn,permissions,userCount,createdAt,updatedAt}`, `RoleInput`,
`Permission{key,group,description}`.

**Read first.**
- `docs/OpenAPI.json` — the six operations and the `Role`/`RoleInput`/`Permission` schemas.
- R2-001's `RoleEntity`, `PermissionEntity`, `role_permission`, the `Permissions` catalog constants and the
  built-in matrix; R2-004's `EffectivePermissions` cache + `AccessPolicySpecSyncTest`.

**Build.**

- **`RoleService` / `RoleAdminController`.**
  - `listRoles` — all roles with `builtIn`, their permission key set, and `userCount` (count of `user_role` rows;
    do it in one query, not per-role fan-out).
  - `getRole` — one role (404 if absent).
  - `createRole` — a custom role (`built_in=false`) from `RoleInput{name, description, permissions[]}`. Validate:
    unique name (409 on clash, including clashes with built-in names); **every requested permission key exists in
    the catalog** (422 listing the unknown keys). A custom role is exactly a named subset of the catalog.
  - `updateRole` — rename/re-describe/re-set permissions for a **custom** role. For a **built-in** role: refuse to
    change its name or `built_in` flag and refuse to change its permission set (**409 `BUILTIN_ROLE_PROTECTED`**).
    (If product later wants built-in descriptions editable, that is out of scope here — keep built-ins fully
    protected.) On any permission change, invalidate the cached effective permissions of every user holding the role.
  - `deleteRole` — delete a **custom** role only; built-in ⇒ 409 `BUILTIN_ROLE_PROTECTED`. Refuse to delete a role
    still assigned to users unless the request opts into unassigning (document the behavior; default: 409
    `ROLE_IN_USE` with the user count, so an admin reassigns first). Never allow an action that leaves zero
    `users.manage`/`roles.manage` holders across the instance.
  - `listPermissions` — the full catalog as `Permission[]`, grouped (Identity/Platform/Self-service/Data). This is
    read-only reference data straight from R2-001's catalog; nothing creates permissions at runtime.
- **Protection invariants (bake in):** built-in roles are immutable in name/flag/permissions and undeletable; the
  catalog is fixed; the five built-ins always exist (a startup check re-seeds any missing built-in, mirroring R2-001,
  so a role cannot be lost).

**Model / migration.** None (R2-001 holds the schema). No migration unless a missing index is needed for `userCount`;
if so, add **`Vn__role_user_count.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive), otherwise none.

**Contract / docs.** Implement the six ops to the authored schemas but **leave their `x-foresignal-status: planned`** (R2-027 flips the surface and regenerates the SDKs once at cutover; regenerate Postman only). Add `x-foresignal-permission: roles.manage` to them (additive metadata, allowed mid-release). Keep
`AccessPolicy`/spec-sync in agreement. Regenerate Postman (bodies for `createRole`, `updateRole`). Update
`docs/RBAC-Design.md` (role management, built-in protection, custom-role rules, the catalog as fixed data).

**Tests to write (do not run).** list with `userCount`; create a custom role from a valid subset; create with an
unknown permission key → 422; duplicate/built-in name clash → 409; update a custom role's permissions invalidates
affected users' caches; update a built-in's name or permissions → 409 `BUILTIN_ROLE_PROTECTED`; delete a custom role;
delete a built-in → 409; delete an in-use role → 409 `ROLE_IN_USE`; `listPermissions` returns the full seeded
catalog grouped; the five built-ins are re-seeded if missing at startup. Target ≥ 90% line coverage.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** Roles list/create/read/update/delete and `listPermissions` work and are gated by `roles.manage`;
built-in roles and the catalog are protected; permission changes invalidate affected users' caches; ops are
`implemented`; Postman regenerated; docs updated; `checkUsEnglish` green; compilation passes.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-006: Role management API` + `completion-reports/R2-006-CR-Role-Management.md`. **Stage only this
prompt's files.** End your reply with `Implementation commit: <hash>`.
