# 023 — Scoped API Tokens

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/023-CR-Scoped-API-Tokens.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/022-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §15 (scopes, `AccessPolicy`), §7.1 (`api_token`), §7.2 (bootstrap token)
- `docs/Gap-Analysis.md` §1 items 4–5, §2 (Flutter mobile, local web rows), §6 (stream tickets, restricted tokens, device pairing, remote access, public-exposure hardening, audit log, deep links), §8.A item 12 ("extend 019" = 023)
- `docs/Core-Data-Model.md` §14 (observer gating, `security.exposeObserver`)
- `docs/Product-Charter.md` §8.7–§8.8 (FR-30, FR-31, FR-39, FR-40)
- `docs/OpenAPI.json`: operations tagged `Tokens`, `getSecuritySettings`/`updateSecuritySettings`, `getServerSettings`/`updateServerSettings`; schemas `ApiToken*`, `SecuritySettings`, `ServerSettings`; every operation's `x-foresignal-scope`
- Existing code: `ai.foresignal.security` (`AccessPolicy`, `AccessRequirement`, `TokenFilter`, `TokenHasher`, `AdminTokenService`, `Scope`); Completion Reports of 015 (stream limits), 018 (`links`, `server` setting), 019 (`/v1/system` gating, retention steps), 020 (pending operations list)

## Objective

Replace the single admin token with scoped API tokens (`read` < `agent` < `admin`) so agents, devices and integrations get least privilege. Add what browser and phone clients need to authenticate safely: reconnect-aware stream tickets, reusable tile tickets, device metadata and a pairing code with QR, plus `alwaysAuthenticated` on cost-bearing endpoints, trusted-proxy handling, deep-link templates, an audit log and the private-source gate hook.

> **v1.3 note (principals):** introduce `principal` here (`id`, `handle`, `display_name`, `kind` person | agent | service, `disabled`, timestamps). Every token belongs to one principal; the bootstrap admin token belongs to principal `owner`; tokens created without a principal get one named after the token. Audit entries, ownership fields and later prompts (128 annotations and workspaces, 129 sharing) refer to the principal, not the token. Prompt 129 adds groups, profiles, notification preferences and access lists; keep the entity open for those columns. No passwords: principals authenticate only with their tokens and pairing codes.

## Tasks

### 1. Token model (`ai.foresignal.security`)

- `ApiTokenEntity` (`api_token`) per §7.1 plus: restriction columns `allowed_sources text[]`, `allowed_kinds text[]`, `allowed_bbox geometry(Polygon,4326)`, `allowed_endpoints text[]` (each `"METHOD /v1/path/pattern"`, Ant-style, method may be `*`); device columns `device_name`, `device_platform` (`ios|android|macos|windows|linux|web|other`), `app_version`, `device_reported_at`; `created_via` (`api|pairing|bootstrap`).
- **`TokenService`:** `create(name, scope, expiresAt, restrictions, device)` returns the plaintext once (`fsr_` + base64url(32 bytes)), stored as SHA-256 via `TokenHasher`, with `token_prefix` (first 8 chars). `list()`, `revoke(id)` (bootstrap → 409 `BOOTSTRAP_TOKEN`), `authenticate(presented)` → `Optional<AuthenticatedToken(id, name, scope, restrictions)>` via lookup by hash with constant-time comparison; revoked and expired tokens rejected. `last_used_at` updated at most once per minute per token, asynchronously.
- Validation: an `allowed_endpoints` pattern must match at least one spec operation that the token's scope can reach; bbox valid and within ±180/±90; source ids and kinds must exist.
- **Bootstrap migration:** at startup, if `system_secret['admin_token_hash']` exists and no `api_token` row named `bootstrap` does, create it (scope `admin`, `created_via=bootstrap`) with that hash, then delete the secret. `AdminTokenService` (first start, `FORESIGNAL_RESET_ADMIN_TOKEN`, rotation) now operates on that row; operator-visible behaviour is unchanged.
- **Revoke cascade:** revocation publishes `ApiTokenRevokedEvent(tokenId)` after commit. Listeners: the stream-ticket store (invalidate tickets), the SSE hubs (close that token's open streams), and a documented hook for 031, which deletes push subscriptions created by the token (031 stores `token_id`).

### 2. Enforcement

- `TokenFilter` resolves bearer tokens (and tickets, §3) through `TokenService` and compares the scope with `AccessPolicy.requirementFor(method, path)`.
- 401 when a required token is missing or invalid; 403 `INSUFFICIENT_SCOPE` (with `requiredScope`) when the scope is too low; 403 `ENDPOINT_NOT_ALLOWED` when `allowed_endpoints` is set and no pattern matches.
- **Data restrictions (sources, kinds, bbox) are enforced in 035.** Until then fail closed: define a `RestrictionEnforcer` SPI whose default bean answers 403 `RESTRICTION_NOT_ENFORCED` for a token with data restrictions on any data-read path; 035 replaces it. Endpoint restrictions are enforced now.
- **`openReads`** (`app_setting['security'].openReads`, default true): READ rules need no token when true, ≥ `read` when false. Cache the setting; refresh on `AppSettingChangedEvent`.
- **`alwaysAuthenticated`:** add the flag to `AccessRequirement`; such operations need ≥ `read` even when `openReads` is true: `POST /v1/ask`, `POST /v1/ask/stream`, `GET /v1/ask/threads/**`, `GET /v1/search`, `POST /v1/push/subscriptions`, `DELETE /v1/push/subscriptions/*`, `POST /v1/stream-tickets`. Expose `AccessPolicy.alwaysAuthenticated()` for 025, which marks LLM-backed MCP tools the same way.
- `/v1/system` full details now need ≥ `read` (019 left any valid token).
- `CurrentToken` request-scoped holder (id, name, scope, restrictions, device) for audit, Ask actions and MCP.
- **Private-source gate hook:** `SourceVisibilityGate.visible(sourceId, CurrentToken)`: a source whose descriptor has `private=true` (008) is visible only to `agent`/`admin` tokens without a source restriction excluding it. Apply it now in `SourceService` listings and where records/changes resolve their source filter (private sources are excluded, or 404 when named explicitly). Prompt 039 extends it to every path (tiles, SSE, search, embeddings, LLM egress, exports).
- **`AccessPolicySpecSyncTest`:** for every operation in `docs/OpenAPI.json`, `AccessPolicy` returns the same scope as `x-foresignal-scope` and the same `alwaysAuthenticated` as `x-foresignal-always-authenticated`; template paths matched against the policy's patterns; the top-level `webhooks` object is skipped.

### 3. Stream and tile tickets (`ai.foresignal.security.ticket`)

- `POST /v1/stream-tickets` `{purpose: "stream"|"tiles", paths?: [...]}` → `{ticket, purpose, expiresAt}`. The ticket inherits the caller's token id, scope and restrictions, never more. Tickets are `fst_` + 24 random bytes, held in memory by hash (lost on restart; clients mint again on 401).
- **Stream tickets** (`?ticket=` on `/v1/changes/stream`, `/v1/alerts/stream`, `/v1/events/stream`): first connect within 60 s of minting; after that the same ticket keeps working for reconnects (browser `EventSource` reuses the URL) while the token is valid, up to 5 min after the last disconnect and 24 h total. At most one concurrent stream per ticket; 015's per-token stream limits count it against the parent token.
- **Tile tickets** (`?ticket=` on `/v1/tiles/**` and TileJSON): reusable for any number of requests until expiry (default 10 min, max 1 h, `ttl` field).
- Plug stream tickets into 015's `StreamPrincipalResolver` (the principal is the parent token, so per-token limits and ownership still apply); plug tile tickets into the token filter.
- A ticket is accepted only for its purpose's paths and never in an `Authorization` header. Strip `ticket` from access logs and from any URL echoed in responses.

### 4. Devices and pairing

- `PUT /v1/tokens/current/device` (read scope, the calling token itself): `{deviceName, platform, appVersion}`; sets `device_reported_at`.
- `POST /v1/pairing-codes` (admin): `{name, scope (read|agent only; admin is never pairable), restrictions?, ttl (default 10m, max 1h)}` → `{id, code, expiresAt, qrPayload}`. `code` = 8 Crockford base32 chars shown as `XXXX-XXXX`, stored hashed in `pairing_code` (`id`, `code_hash`, `name`, `scope`, `restrictions jsonb`, `expires_at`, `claimed_at`, `claimed_token_id`, `created_by_token_id`). `qrPayload` = the `clientLinks.pair` template (default `foresignal://pair?u={publicBaseUrl}&c={code}`).
- `GET /v1/pairing-codes/{id}/qr.svg` (admin): SVG QR of `qrPayload` via ZXing `core` (Apache-2.0; verify the version on Maven Central; add a THIRD_PARTY_NOTICES entry).
- `GET /v1/pairing-codes` (admin, unclaimed and unexpired), `DELETE /v1/pairing-codes/{id}` (admin).
- `POST /v1/pairing/claim` (open): `{code, device:{deviceName, platform, appVersion}}` → `ApiTokenCreated` (`created_via=pairing`). Single use; constant-time comparison; Bucket4j limit of 5 attempts per minute per client IP plus 50 per hour globally, then 429; every failure audited.

### 5. Server settings, trusted proxies, clientLinks

- `GET/PUT /v1/settings/server` (`ServerSettings`): `publicBaseUrl` (already used by 018; move its storage here if 018 stored it elsewhere, and record that), `timezone` (default UTC), `clientLinks` (templates `alert`, `record`, `briefingRun`, `pair`; defaults use the `foresignal://` scheme, e.g. `foresignal://alerts/{id}`; operators may set verified `https://` links). 018's link builder adds an "Open in app" link from `clientLinks` when set.
- **https warning:** PUT returns `warnings` and `/v1/system` lists `PUBLIC_BASE_URL_INSECURE` when `publicBaseUrl` is `http://` and its host is not loopback, private, CGNAT or `.local` (reuse `LocalAddresses`), and `PUBLIC_BASE_URL_MISMATCH` when requests arrive with forwarded proto `https` while `publicBaseUrl` is `http`.
- **Trusted proxies:** `server.forward-headers-strategy=native` (Tomcat `RemoteIpValve`, not `framework`); bootstrap env `FORESIGNAL_TRUSTED_PROXIES` (a regex) maps to `server.tomcat.remoteip.internal-proxies`, default loopback only (`127\.0\.0\.1|0:0:0:0:0:0:0:1|::1`). Document Docker-network and Tailscale examples. The client IP is then used by audit, pairing limits and 035.
- `GET/PUT /v1/settings/security` (`SecuritySettings`: `openReads`, `exposeObserver`; add the latter if 014 did not).
- Export/import (019) includes `security` and `server`. Tokens, tickets and pairing codes are **never** exported.

### 6. Audit log

- `audit_event` (Hibernate): `id bigint identity`, `at`, `actor_kind` (`token|ingest_key|pairing|system`), `token_id`, `token_name`, `scope`, `method`, `path` (query stripped), `operation_id`, `status`, `target_type`, `target_id`, `remote_ip`, `detail jsonb` (never secrets, token values or request bodies). Index `(at DESC)`, `(token_id, at DESC)`.
- Record every authenticated write (non-GET/HEAD), every 401/403 on a write path, pairing claims and token revocations; 025 adds MCP tool calls through `AuditService.record(...)`. Insert asynchronously through a bounded queue (drop and count on overflow).
- `GET /v1/audit` (admin): `since`, `until`, `tokenId`, `operationId`, `status`, keyset cursor, `limit`.
- Add retention key `audit` (default 90d) and a `RetentionStep` (019).

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: `ApiToken`/`ApiTokenInput` (+`restrictions`, `device`, `createdVia`); new schemas `TokenRestrictions`, `DeviceInfo`, `StreamTicketRequest`/`StreamTicket`, `PairingCode`/`PairingCodeInput`/`PairingClaim`, `AuditEvent`/`AuditEventPage`; `ServerSettings` (+`timezone`, `clientLinks`, `warnings`), `SecuritySettings` (+`exposeObserver`), `RetentionSettings` (+`audit`). New operations, each with `x-foresignal-scope`: `createStreamTicket` (read, `x-foresignal-always-authenticated: true`), `updateCurrentTokenDevice` (read), `listPairingCodes`/`createPairingCode`/`deletePairingCode`/`getPairingCodeQr` (admin), `claimPairingCode` (open), `listAuditEvents` (admin). Add `x-foresignal-always-authenticated: true` to the operations in §2. Add `ticket` query parameters to the SSE and tile operations. Error codes `INSUFFICIENT_SCOPE`, `ENDPOINT_NOT_ALLOWED`, `RESTRICTION_NOT_ENFORCED`, `BOOTSTRAP_TOKEN`, `PAIRING_CODE_INVALID`.
- `AccessPolicy` rules for every new path, in contract order, specific before general (`/v1/tokens/current/device` before `/v1/tokens/**`; `/v1/pairing/claim` open).
- Remove implemented operations from `pending-operations.txt` (020). Regenerate Postman; BODIES for `createToken` (restricted), `createStreamTicket`, `createPairingCode`, `claimPairingCode`, `updateCurrentTokenDevice`; extend the generator's auth mapping if `alwaysAuthenticated` read operations need `{{readToken}}`, and add `readToken` to the environment.
- `docs/Operator-Guide.md`: tokens and scopes, restricted tokens (data restrictions active from 035), pairing a phone (QR), tickets for browser clients, trusted proxies, clientLinks, the audit log.

## Tests to write (do not run)

- `TokenServiceIT`: create, authenticate, revoke, expire; bootstrap migration from the legacy secret; bootstrap revoke → 409; `last_used_at` throttling; restriction validation; revoke publishes the event.
- `AccessPolicySpecSyncTest` as above.
- `TokenFilterIT`: {no token, read, agent, admin, revoked, expired, endpoint-restricted, data-restricted} × {read op, agent op, admin op, alwaysAuthenticated op} with `openReads` on and off → expected 200/401/403 and codes.
- `StreamTicketIT` (`MutableClock`): first-connect window, reconnect within 5 min, rejected after 24 h, one concurrent stream, tile ticket reuse until expiry, wrong purpose rejected, revoke invalidates tickets and closes streams, `ticket` absent from logs.
- `PairingIT`: create, claim once, second claim fails, expiry, admin scope refused, rate limit → 429, QR SVG decodes back to `qrPayload` (ZXing reader in test).
- `AuditIT`: writes audited with token name and status, GETs not audited, secrets absent from `detail`, filters and paging, retention step.
- `SourceVisibilityGateTest`: private source hidden from anonymous and read tokens, visible to agent and admin.
- `TrustedProxyIT`: `X-Forwarded-For` honoured only from a configured internal proxy.
- `TokensControllerIT`, `SecuritySettingsIT`, `ServerSettingsIT` (https warnings, clientLinks round trip).

## Acceptance criteria

- `x-foresignal-scope` and `x-foresignal-always-authenticated` agree with `AccessPolicy` for every operation.
- An `agent` token can manage triggers and briefings but gets 403 on sources, credentials, channels, providers, tokens and settings.
- A phone can pair with a code or QR without ever seeing the admin token.
- Revoking a token immediately invalidates its tickets and open streams.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `023: Scoped API Tokens`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/023-CR-Scoped-API-Tokens.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `023: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
