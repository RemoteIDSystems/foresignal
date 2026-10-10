# 003 — Security And Secrets

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/003-CR-Security-And-Secrets.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/002-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §7.2 (secrets), §10 (API conventions: errors, auth, CORS)
- `docs/Product-Charter.md` FR-30, FR-31, FR-33, NFR-7
- `docs/OpenAPI.json` schema `Problem` and the `adminToken` security scheme

## Objective

Lock down writes with the single admin token. Encrypt upstream credentials at rest. Establish the install id used in User-Agents, the RFC 9457 error model every controller will use, and CORS that is configurable at runtime. **Do not add Spring Security.** A servlet filter is sufficient and keeps things simple.

## Tasks

### 1. SecretKeyStore (`ai.elaro.security`)

- On startup, load `${elaro.data-dir}/secret.key` (32 random bytes). If it is absent, create the directory and the file, setting POSIX permissions to `rw-------` where the filesystem supports them (log a warning otherwise).
- Never log the key. Expose a `SecretKey` (AES) to `CredentialCipher` only.

### 2. CredentialCipher

- AES-256-GCM with a random 12-byte nonce per encryption and a 128-bit tag.
- `Encrypted encrypt(byte[] plaintext)` returns `(ciphertext, nonce)`; `byte[] decrypt(Encrypted)`. Also provide convenience overloads for `Map<String,String>`, serialized as JSON.
- Tampering raises a `CredentialDecryptionException`.

### 3. AdminTokenService

- At startup (before the web server accepts requests), if `system_secret['admin_token_hash']` is absent, **or** `elaro.reset-admin-token=true`:
  - generate a token: `"elr_" + base64url(32 random bytes, no padding)`
  - store the SHA-256 hex of the token
  - log it once inside a clearly delimited banner (e.g. a box with "ELARO ADMIN TOKEN — shown once, store it securely")
- `boolean verify(String presentedToken)`: hash, then `MessageDigest.isEqual` against the stored hash. Cache the stored hash in memory.
- `String rotate()`: generate and store a new token, invalidate the cache and return the token. The endpoint for this comes in prompt 016.

### 4. AdminTokenFilter

A `OncePerRequestFilter` ordered after CORS. It requires `Authorization: Bearer <token>` when any of these is true:

- the method is not GET, HEAD or OPTIONS, and the path starts with `/v1/`
- the method is GET and the path matches `/v1/settings/**`, `/v1/webhooks/**` or `/v1/system/admin-token/**`

Behaviour:

- Everything else is open, including `/v1/openapi.json`, `/docs/**` and `/actuator/health`. `/actuator/prometheus` is open too (document this; it is LAN-only).
- When the token is missing or invalid, respond `401` with `application/problem+json`, `code: "UNAUTHORIZED"` and a `WWW-Authenticate: Bearer realm="elaro"` header. Never echo the presented token.
- Keep the path rules in one place (a small `AdminPathPolicy` class) so they can be unit tested exhaustively.

### 5. InstallIdService

- `app_setting['install.id']`: 8 lowercase alphanumerics, generated on first start and stable afterwards.
- `userAgent()` returns `Elaro/<version> (+https://elaro.ai; install=<id>)`.
- **Version source** (from the 001 report): add a small `ElaroVersion` bean that prefers `BuildProperties.getVersion()` (via `ObjectProvider<BuildProperties>`), falls back to `elaro.version`, then to `"dev"`. Every later use of the version (User-Agent, `/v1/system`, settings export) reads `ElaroVersion`, never `ElaroProperties.version` directly.

### 6. Error model (`ai.elaro.api.error`)

- **Exception hierarchy:** `ElaroException(HttpStatus status, String code, String detail)` with subclasses:
  - `NotFoundException` (404, `NOT_FOUND`)
  - `ConflictException` (409, code supplied, e.g. `CREDENTIALS_REQUIRED`, `ALREADY_RUNNING`, `IN_BACKOFF`)
  - `ValidationException` (400, `VALIDATION_FAILED`, with a list of `{field, message}`)
  - `BudgetExceededProblem` (429, `BUDGET_EXCEEDED`)
- **`GlobalExceptionHandler`:** `@RestControllerAdvice` extending `ResponseEntityExceptionHandler`. It maps the hierarchy plus Bean Validation errors, malformed JSON, type mismatches and `IllegalArgumentException` from parsing to `ProblemDetail`. Each problem gets:
  - `type = https://elaro.ai/problems/<code-in-kebab-case>`
  - a `code` property
  - an `errors` property when there are field errors
- Unexpected exceptions return a 500 with a generic detail; log the stack trace with a correlation id that is also included in the problem body (`instance` or a property).
- Set `spring.mvc.problemdetails.enabled=true`.

### 7. Dynamic CORS (`ai.elaro.config.CorsFilter`)

- Read `CorsSettings` from `AppSettingService` (cached; refreshed on `AppSettingChangedEvent("cors")`).
  - GET, HEAD and OPTIONS requests for read endpoints allow `readOrigins`, where `*` means any origin.
  - Write methods, and their preflights (preflight `Access-Control-Request-Method` not GET/HEAD), allow only `writeOrigins`. An empty list means no cross-origin writes.
- Allowed headers: `Authorization`, `Content-Type`, `Accept`, `Last-Event-ID`. Exposed headers: `Location`. Max age 600.
- `CorsSettingsService`: `get()` / `update(CorsSettings)` with validation (each origin is `*` or a valid `scheme://host[:port]`). The endpoints come in prompt 016.

### 8. Actuator health details (from the 001 report)

`management.endpoint.health.show-details: when-authorized` depends on Spring Security, which Elaro deliberately does not use, so it silently behaves as `never`. Set it explicitly to `never`. `/actuator/health` returns only `UP`/`DOWN` for container health checks; detailed status (DB, PostGIS version, scheduler) belongs to `GET /v1/system` in prompt 016. Record this in the CR.

## Tests to write (do not run)

- `CredentialCipherTest`: round trip, a unique nonce per call, tampered ciphertext and nonce, map round trip.
- `SecretKeyStoreTest`: creates the key with correct size and permissions (temp directory), reloads the same key, rejects a corrupt key file.
- `AdminPathPolicyTest`: an exhaustive table of method/path combinations.
- `AdminTokenServiceIT`: first-start generation (capture the log output), verify true/false, the reset flag regenerates, rotate invalidates the old token.
- `AdminTokenFilterIT`: MockMvc against stub controllers registered in test config. Covers 401 problem+json, 200 with the token, and open GETs.
- `GlobalExceptionHandlerTest`: each exception maps to the expected status, code, type and body.
- `CorsFilterIT`: read preflight with `*`, a write preflight rejected by default, allowed after updating the settings.
- `InstallIdServiceIT`: stable across calls; User-Agent format.
- `ElaroVersionTest`: BuildProperties preferred, then the property, then `dev`.

## Acceptance criteria

- No `spring-boot-starter-security` dependency.
- The admin token is never stored or logged except in the single first-start banner.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 040.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `003: Security And Secrets`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/003-CR-Security-And-Secrets.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `003: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
