# 019 — Scoped API Tokens

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/019-CR-Scoped-API-Tokens.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/018-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §15 (scopes, `AccessPolicy`), §7.1 (`api_token`), §7.2 (bootstrap token)
- `docs/Product-Charter.md` §8.7–§8.8 (FR-30, FR-31, FR-39, FR-40)
- `docs/OpenAPI.json`: operations tagged `Tokens`, `getSecuritySettings`/`updateSecuritySettings`, `getServerSettings`/`updateServerSettings`, schemas `ApiToken*`, `SecuritySettings`, `ServerSettings`, and every operation's `x-elaro-scope`

## Objective

Replace the single admin token with scoped API tokens. Agents and integrations get least-privilege access (`read` < `agent` < `admin`), and the operator can choose whether data reads are open.

## Tasks

### 1. Token model (`ai.elaro.security`)

- Add `ApiTokenEntity` (`api_token`) per §7.1, plus a repository.
- **`TokenService`:**
  - `create(name, scope, expiresAt)` returns the plaintext once: `elr_` + base64url(32 bytes), stored as SHA-256 via `TokenHasher` from prompt 003, along with `token_prefix` (first 8 characters).
  - `list()`, `revoke(id)` (sets `revoked_at`; the bootstrap token cannot be revoked → 409).
  - `authenticate(presented)` returns `Optional<AuthenticatedToken(id, name, scope)>`. It does a constant-time hash comparison through a lookup by hash; the stored hash is the index. It rejects revoked and expired tokens.
  - Update `last_used_at` at most once per minute per token, asynchronously.
- **Bootstrap migration:** at startup, if `system_secret['admin_token_hash']` exists and there is no `api_token` row named `bootstrap`, create that row (scope `admin`) with the existing hash, then delete the secret. `AdminTokenService` (first-start generation, `ELARO_RESET_ADMIN_TOKEN`, rotation) now operates on the bootstrap row. The behaviour visible to the operator is unchanged.

### 2. Enforcement

- The token filter resolves the bearer token through `TokenService` and compares its scope against `AccessPolicy.requiredScope(method, path)`, which already holds the final scopes from prompt 003.
- **Responses:**
  - 401 when the token is missing or invalid and the rule needs a token
  - 403 problem `INSUFFICIENT_SCOPE` (with `requiredScope`) when the token is valid but its scope is too low
- **`openReads`:** `app_setting['security'].openReads` (default true). When true, `READ` rules need no token. When false, they need a token of at least `read`. Cache the setting and refresh it on `AppSettingChangedEvent`.
- Expose the authenticated token through a request attribute or a `CurrentToken` holder, for audit logging and for later prompts (Ask actions, MCP).
- **Audit:** log every authenticated write as structured log fields (token name, scope, method, path, status). Token values are never logged.
- **Sync check:** verify that `AccessPolicy` matches every operation's `x-elaro-scope` in `docs/OpenAPI.json`. Add a unit test that loads the spec and asserts that `AccessPolicy` returns the same scope for each path and method. Path templates must be matched against the policy's patterns.

### 3. API

- `GET /v1/tokens`, `POST /v1/tokens` (201, `ApiTokenCreated` including `token` once), `DELETE /v1/tokens/{tokenId}` (204; 409 for the bootstrap token).
- `GET/PUT /v1/settings/security` (`SecuritySettings`).
- `GET/PUT /v1/settings/server` (`ServerSettings.publicBaseUrl`, already used by prompt 015; move its storage under this API if 015 stored it differently, and record that in the CR).
- **Export/import (prompt 016):** include `security` and `server`. Tokens are **never** exported.

## Tests to write (do not run)

- `TokenServiceIT`: create, authenticate, revoke, expire; bootstrap migration from the legacy secret; bootstrap revoke gives 409; `last_used_at` throttling.
- `AccessPolicySpecSyncTest`: every operation in `docs/OpenAPI.json` maps to the same scope.
- `TokenFilterIT`: a matrix of {no token, read, agent, admin, revoked, expired} × {a read op, an agent op, an admin op} with `openReads` on and off, giving the expected 200/401/403.
- `TokensControllerIT`, `SecuritySettingsIT`, `ServerSettingsIT`.

## Acceptance criteria

- `x-elaro-scope` in the spec and `AccessPolicy` agree for every operation.
- An `agent` token can manage triggers and briefings but gets 403 on sources, credentials, channels, providers, tokens and settings.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `019: Scoped API Tokens`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/019-CR-Scoped-API-Tokens.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `019: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
