# R2-001 — Identity model: users, roles, permission catalog, tokens

**Goal.** Lay the v2.0 (R2) identity foundation the whole RBAC release stands on: the `user`, `role`,
`permission` catalog, `user_role`, `role_permission` and `user_token` tables, the five built-in roles,
and the seeded permission catalog. Model + Flyway only — **no API operations change in this prompt.**
Later R2 prompts wire auth, authorization and management on top.

**Dependency.** None — this is the foundation. R2-002…R2-013 all depend on it.

**Read first.**
- `src/main/resources/db/migration/V13__llm_pricing_and_spend_caps.sql` — the newest migration, and the
  additive `alter table … add column` style. Next free version is **V14**.
- `src/main/resources/application.yml` (the `flyway` + `jpa.hibernate.ddl-auto: validate` block) — Flyway
  owns the schema, Hibernate only **validates** entities against it, so every entity column must match a
  migrated column exactly.
- `src/main/java/ai/foresignal/security/PrincipalEntity.java`, `PrincipalKind.java`, `PrincipalService.java`
  — the existing `principal` model (prompt 023/130). A `user` is a new, password-bearing identity; a
  session (R2-003) resolves to a principal the way a token does. Decide and record how a `user` relates to
  a `principal`: **create one `principal` of kind `person` per user** (reuse the existing kind) and store
  `principal_id` on `user`, so annotations, workspaces, sharing and audit keep referring to a principal.
- `src/main/java/ai/foresignal/security/Ulids.java` / how existing entities mint ids (UUID vs ULID) —
  match the prevailing id style for the new tables.
- `docs/OpenAPI.json` schemas `User`, `Role`, `Permission` (already authored, 2.0.0) — the model must be
  able to produce exactly these shapes: `User{id,email,fullName,status,roles,mfaEnabled,createdBy,createdAt,updatedAt,lastLoginAt}`,
  `Role{id,name,description,builtIn,permissions,userCount,createdAt,updatedAt}`, `Permission{key,group,description}`.

**Build.**

- **`user` table / `UserEntity`** (`ai.foresignal.security.user`, new package): `id` (uuid, pk),
  `email citext`-normalized-unique (store a normalized lower-cased `email` with a unique index; if `citext`
  is not already enabled, normalize in code and keep a unique index on the stored value — do **not** assume
  an extension exists, verify in `V1__baseline.sql`), `full_name`, `password_hash` (nullable until
  activation; the hashing itself is R2-002), `status` (`invited`/`active`/`suspended`/`disabled`, default
  `invited`), `principal_id` (fk to `principal`), `mfa_enabled boolean not null default false` (column only;
  MFA logic is a later R2 item), `created_by` (uuid, nullable — the admin who created the user, null for the
  bootstrap-seeded first admin), `created_at`, `updated_at`, `last_login_at` (nullable).
- **`role` table / `RoleEntity`**: `id` (uuid, pk), `name` unique, `description`, `built_in boolean not null`,
  `created_at`, `updated_at`.
- **`permission` catalog / `PermissionEntity`**: `key` (pk, text), `group` (text), `description` (text). This
  is a **fixed catalog**, seeded by the migration and treated as read-only data at runtime (R2-006 lists it;
  nothing creates permissions at runtime).
- **`user_role`** (`user_id`, `role_id`, pk both, cascade on user/role delete) and **`role_permission`**
  (`role_id`, `permission_key`, pk both, cascade on role delete). Model the many-to-many either as
  `@ManyToMany` or as explicit link entities — pick the one that matches how existing link tables are done in
  this codebase and record the choice.
- **`user_token`** (`UserTokenEntity`): single-use, hashed, expiring tokens for **account activation** and
  **password reset** (the issuing/consuming logic is R2-002; this prompt creates the table + entity).
  Columns: `id` (uuid, pk), `user_id` (fk), `kind` (`activation`/`reset`), `token_hash` (text, the stored
  SHA-256; never the plaintext), `token_prefix` (first 8 chars, for lookup/display), `created_at`,
  `expires_at`, `consumed_at` (nullable), `created_by` (nullable). Index `(token_hash)` and `(user_id, kind)`.

- **Permission catalog (seed exactly these keys and groups):**
  - *Identity*: `users.manage`, `roles.manage`
  - *Platform*: `system.settings`, `collection.manage`, `collector.settings`, `llm.shared.manage`,
    `channels.global`, `storage.manage`, `network.manage`, `audit.read`
  - *Self-service*: `self.sources`, `self.alerts`, `self.channels`, `self.llm`, `self.views`,
    `self.preferences`, `self.workspaces`
  - *Data*: `data.read`, `data.export`, `intel.read`, `forecast.read`
  Define them once in a `Permissions` constants/enum class in Java **and** seed the same rows in V14; add a
  guard test asserting the Java catalog and the seeded rows agree (same keys, same groups) so the two can
  never drift.

- **Built-in roles (seed with stable ids; `built_in=true`; `role_permission` sets exactly):**
  - **Admin** — every permission in the catalog.
  - **Operator** — all *Platform* + all *Data*, **not** `users.manage`/`roles.manage`.
  - **Editor** — all *Self-service* + all *Data*.
  - **Viewer** — `data.read`, `self.views`, `self.preferences` only (read-only; self-service alerts/channels
    start at Editor).
  - **Auditor** — `audit.read` + `data.read`.
  Built-in roles and the permission catalog are **protected** (R2-006 enforces no-delete / no-permission-edit
  on built-ins; this prompt just marks and seeds them).

- **First-admin seeding is R2-013/R2-027 territory, not here.** This prompt seeds the catalog and the five
  built-in roles only. Do **not** create any user row. If no user exists, the instance keeps working exactly
  as today on the bootstrap admin token (R2-003/R2-004 integrate sessions without removing token auth).

**Model / migration.** Add **`Vn__identity_model.sql`** (where `n` = one past the highest `V*` now present in `src/main/resources/db/migration/` — V13 is the newest today, so R2-001 is **V14**; read the directory, never hard-code, because Tier-1 prompts build in parallel; additive: new tables + catalog/role seed rows,
`insert … on conflict do nothing` so re-running against a partially-seeded DB is safe). Entities validate
against it under `ddl-auto: validate`; every column, type, nullability and index must match. Keep all new
entities in a new `ai.foresignal.security.user` package with Javadoc on every class and public method, written
in the same pass.

**Contract / docs.** No operation changes and **no API version bump** (already 2.0.0; `User`/`Role`/`Permission`
schemas are already authored). Update `docs/Architecture.md` (identity/RBAC section — the new tables and the
user↔principal relationship) and add the permission catalog + built-in role matrix to `docs/Invariants.md` (the
per-user isolation invariants start here). If a `docs/RBAC-Design.md` does not yet exist, create it and make it
the authoritative description of the identity model, the catalog, and the role matrix (replaced whole in later
prompts as the layers land).

**Tests to write (do not run).** The V14 migration applies cleanly on top of V13; the catalog Java constants
match the seeded rows; the five built-in roles seed with exactly the permission sets above (Admin = full
catalog, Operator excludes identity, Viewer is read-only, Auditor = audit+data.read); a `UserEntity` round-trips
with a `principal_id`; `user_token` stores only a hash and a prefix. Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish`
and keep it green. Per `000-Run-Sequence.md` do **not** run the test suite, `lintOpenApi` or `checkApiCompat`.
Write the tests, do not execute them. US English everywhere (identifiers, comments, seed descriptions).

**Done when.** V14 creates `user`, `role`, `permission`, `user_role`, `role_permission`, `user_token`; the
permission catalog and the five built-in roles are seeded and match the Java catalog; entities validate under
`ddl-auto: validate`; no operation or API version changed; docs updated; `checkUsEnglish` green; compilation
passes. The completion report lists the seeded permission keys and the per-role permission sets.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the
`x-foresignal` changelog history.

**Commit.** `R2-001: Identity model` + `completion-reports/R2-001-CR-Identity-Model.md` (same format as the
earlier reports, with the 40-character implementation commit hash). **Stage only this prompt's files.** End
your reply with `Implementation commit: <hash>`.
