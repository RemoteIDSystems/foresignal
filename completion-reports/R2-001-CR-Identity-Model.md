# R2-001-CR-Identity-Model

| | |
|---|---|
| Prompt | R2-001-Identity-Model.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run, per `R2-000-Run-Sequence.md`) |
| Implementation commit | `4cfbbab1590db7c16eb074799bbf3d38e559c011` |
| Date | 2026-10-26 |

## What was added

- **`V14__identity_model.sql`** (V13 was the newest): `"user"`, `role`, `permission`, `user_role`,
  `role_permission`, `user_token`, plus the seeded catalog and the five built-in roles, every insert
  `on conflict do nothing`. No user row; no API operation or version change.
- **Package `ai.foresignal.security.user`:** `UserEntity`, `RoleEntity`, `PermissionEntity`,
  `UserRoleEntity`, `RolePermissionEntity`, `UserTokenEntity`; their repositories; `Permissions` (the
  catalog enum), `PermissionGroup`, `BuiltInRole`, `UserStatus`, `UserTokenKind`. Javadoc on every class and
  public method.
- **Decisions recorded** (`docs/RBAC-Design.md` §A):
  - User ↔ principal: one `principal` of kind `person` per user, `user.principal_id varchar(26)` → `principal`.
  - Ids: `uuid` for user/role/user_token (the `User`/`Role` contract and `api_token` use uuid); principal stays ULID.
  - Links: explicit `@IdClass` entities, matching every other composite-key table (no `@ManyToMany` exists).
  - Email: `V1__baseline.sql` creates only `postgis` and `vector`, no `citext` — `UserEntity.normalizeEmail`
    trims and lower-cases, unique constraint on the stored value.
  - `"user"` and `"group"` are PostgreSQL reserved words: quoted in SQL, backtick-quoted in the mappings. The
    `permission.key` attribute is `permissionKey` in Java because `key` is an HQL keyword.
  - `user_token.token_hash` is `varchar(64)` (SHA-256 hex, the `api_token.token_hash` form) rather than `text`.
- **Test infrastructure:** `DatabaseCleaner` no longer truncates `permission`, `role`, `role_permission`
  (seeded reference data); it deletes custom roles instead. Without this the seed would vanish after the first
  test of every run.

## Seeded permission keys (21)

- identity: `users.manage`, `roles.manage`
- platform: `system.settings`, `collection.manage`, `collector.settings`, `llm.shared.manage`, `channels.global`,
  `storage.manage`, `network.manage`, `audit.read`
- self: `self.sources`, `self.alerts`, `self.channels`, `self.llm`, `self.views`, `self.preferences`,
  `self.workspaces`
- data: `data.read`, `data.export`, `intel.read`, `forecast.read`

## Per-role permission sets (stable ids `00000000-0000-4000-8000-00000000000N`)

| N | Role | Permissions |
|---|---|---|
| 1 | Admin | all 21 |
| 2 | Operator | the 8 platform + the 4 data (12) |
| 3 | Editor | the 7 self + the 4 data (11) |
| 4 | Viewer | `data.read`, `self.views`, `self.preferences` |
| 5 | Auditor | `audit.read`, `data.read` |

## ⚠ Disagreement for the owner

The prompt's matrix (seeded above) and the matrix in `docs/RBAC-Design.md` as committed in `5b38a9f2` differ:
that design gave Operator all self-service permissions too, Viewer `intel.read` and `forecast.read` too, and
Auditor `self.preferences` too. I followed the prompt and wrote the difference into `RBAC-Design.md` §B as an open
question. Note that as seeded, a Viewer cannot hold `intel.read` or `forecast.read`. Changing it is one new
migration plus `BuiltInRole`.

## Docs

- `docs/RBAC-Design.md`: "What exists today", §A and §B rewritten to describe the code; the rest unchanged.
- `docs/Architecture.md`: §7.1 lists the identity tables; new §15.5.
- `docs/Invariants.md`: new "Identity and RBAC" section (RBAC-0, RBAC-5) with the catalog and the role matrix.
  `InvariantRegistryTest` only parses three-digit owners, so these `R2-001` rows are not checked by it.

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew checkUsEnglish`: green.
- No tests were run. Written, not executed:
  - `IdentityModelTest` (unit): catalog keys and groups, `byKey`, wire values, every built-in role's exact
    set, entity behavior (email normalization, status, `updatedAt`, single-use token), link keys.
  - `PermissionCatalogMigrationIT` (scratch DB, V13 → V14): tables created; seeded catalog == `Permissions`;
    seeded roles and grants == `BuiltInRole`; re-running the seed restores a partly deleted grant set without
    duplicates; unique email, unknown permission key refused, cascades on role and user delete.
  - `IdentityModelIT` (app context, so `ddl-auto: validate` covers the entities): user round-trip with a
    `person` principal; catalog via JPA; user/role links and counts; `user_token` holds only hash and prefix.
  - `DatabaseCleanerIT.identityCatalogSurvivesCleaning`.

## Notes for the next prompt

- Not a blocker, but seen while reading: `FlywaySchemaParityIT` compares the full Flyway schema with Hibernate's
  generated one. V13's `llm_usage.cost_usd` is not mapped on `LlmUsageEntity`, and V14's foreign keys are not
  expressible from plain id columns, so that test will report differences when the owner runs the suite. Its
  allowlist was left untouched.
