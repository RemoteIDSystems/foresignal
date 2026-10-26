# R2-004-CR-Authorization

| | |
|---|---|
| Prompt | R2-004-Authorization.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `2ee5578d9fc8ded7d08e98e4acf18f69ad888211` |
| Date | 2026-10-26 |

## What was added

- **`x-foresignal-permission` on 478 of the 900 operations** in `docs/OpenAPI.json`: every admin-scoped operation
  (an `identity` or `platform` key), the data, intelligence and forecast reads (`data.read` 112, `intel.read` 112,
  `forecast.read` 8), the exports (`data.export` 11), and the self-service operations the prompt names
  (`self.sources`, `self.preferences`, `self.llm`). The prompt's named mappings were checked one by one: SMTP ⇒
  `system.settings`, collection sources ⇒ `collection.manage`, `listAuditEvents` ⇒ `audit.read`, `/v1/users/**` ⇒
  `users.manage`, `/v1/roles/**` and `listPermissions` ⇒ `roles.manage`. No admin-scoped operation is left without
  one (checked: the list of admin ops without a permission is empty).
- **`scripts/generate-access-permissions.py`** → **`security/AccessPermissions.java`**: one row per operation (900), a
  null permission where it carries none, ordered literal-before-template. Rows without a permission are listed so a
  broader template cannot answer for them (`GET /v1/precursors/batches` vs `GET /v1/precursors/{id}`).
- **`AccessPolicy.permissionFor(method, path)`**: the permission half of the table, a parallel lookup (HEAD takes
  GET's row; trailing slash and query ignored). **Chosen over a field on `RequiresScope`** so the scope requirement,
  and every machine token's access, is byte-for-byte what it was; 18 existing equality assertions on
  `requirementFor` stay valid.
- **`AccessPolicy` scope rules written ahead** so every operation, planned included, agrees with its contract scope:
  the `/v1/me` self-service, MFA, token and quota operations (`READ_ALWAYS_AUTHENTICATED`), `GET /v1/sources/collection`
  (`ADMIN`, before the sources `READ` rule) and `GET /v1/system/first-run` (`OPEN`).
- **`TokenFilter`**: for a **session** on an operation with a permission, the permission alone decides —
  `403 PERMISSION_DENIED` with `requiredPermission`; on an operation without one, the derived scope. **API tokens:
  scope only**, so an `admin` token satisfies every permission and a lower one gets `INSUFFICIENT_SCOPE`.
- **`ErrorCodes.PERMISSION_DENIED`**.
- **`PermissionResolver` cache**: Caffeine per user (10,000 max, 5-minute TTL), invalidated **after commit** by the new
  `UserRolesChangedEvent` (one user), `RolePermissionsChangedEvent` (all) and the existing `UserSessionsRevokedEvent`.

## Contract

- `x-foresignal-permission` added additively; `info.description` gains a "Permissions (R2-004)" paragraph. No status,
  version or changelog change; the frozen v1.0.0 document untouched.
- Postman regenerated (`./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers
  all ops: True"); only the embedded description changed. SDKs not regenerated.

## Decisions and disagreements for the owner

1. **For a session the permission replaces the scope check instead of adding to it.** The prompt says "gate on both".
   With both, an Auditor (derived scope `read`, §C of RBAC-Design) could never call `listAuditEvents`, which is
   admin-scoped and is what `audit.read` is for. Every admin-scoped op carries a permission and the derived scope comes
   from the same set, so replacing changes no other answer.
2. **RBAC-2 wording changed.** `docs/RBAC-Design.md`'s invariant said sessions "re-resolve, not cache" permissions;
   this prompt says "cache per session (invalidate on role change)". I followed the prompt (cache per user, event
   invalidation after commit, 5-minute TTL bound) and rewrote RBAC-2 in both documents to say so. Which one is right is
   your call; removing the cache is one class.
3. **Viewer sessions see less than anonymous callers.** With the seeded matrix (R2-001), a Viewer lacks `intel.read`
   and `forecast.read`, so a Viewer session gets `PERMISSION_DENIED` on the intelligence and forecast reads an
   anonymous caller is served while `openReads` is on. The planner's earlier matrix (open question in RBAC-Design §B)
   would not have this.
4. The data-tag mapping (which tag is `data.read` and which `intel.read`) is my reading of the tags; R2-013 completes
   data-access gating and can move any op by editing the spec and rerunning the generator.

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Static check (not a test run): a Python emulation of `AccessPolicy`'s rule table and `AccessPermissions` against all
  900 operations reported **0 scope and 0 permission mismatches** (before the added rules: 15 scope and 2 permission
  mismatches, all fixed).
- Not run (written only): `AccessPolicySyncTest` (now every route, scope **and** permission; table size = route count),
  `AccessPolicyPermissionTest`, `PermissionResolverTest` (union, cache hit, each event's invalidation),
  `AuthorizationIT` (Viewer `createUser` ⇒ `PERMISSION_DENIED`, `GET /v1/records` 200; Operator passes
  `collection.manage`, refused `users.manage`; Editor passes `self.sources`, Viewer refused; Auditor reads the audit
  log; admin token passes a `users.manage` op, agent token `INSUFFICIENT_SCOPE`; Admin session passes; union across
  roles; role change stays cached until `UserRolesChangedEvent`). `AuthSessionsIT.permissionsReResolve` now
  invalidates the cache explicitly.

## Notes for the next prompt

- R2-005 must publish `UserRolesChangedEvent(userId)` when it assigns or removes roles; R2-006 publishes
  `RolePermissionsChangedEvent(roleId)` on role edit/delete. Both are invalidated after commit.
- After any `x-foresignal-permission` change, rerun `python3 scripts/generate-access-permissions.py docs/OpenAPI.json
  src/main/java/ai/foresignal/security/AccessPermissions.java`; `AccessPolicySyncTest` holds the two together. A new
  operation also needs a row (the table-size assertion).
