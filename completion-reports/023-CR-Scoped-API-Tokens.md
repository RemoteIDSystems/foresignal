# 023-CR-Scoped-API-Tokens

| | |
|---|---|
| Prompt | 023-Scoped-API-Tokens.md |
| Status | Complete |
| Implementation commit | 3d3ca0d16602e0f2b4d3d46dd7d2bc79f52becb0 |
| Date | 2026-10-12 |

## Summary

The single admin token is replaced by scoped API tokens (`read` < `agent` < `admin`), each owned
by a principal. The prompt's sections all landed:

- **Tokens:** `api_token` rows, created, listed, revoked and authenticated by hash. The bootstrap
  admin token is now the `bootstrap` row, migrated from the old secret.
- **Enforcement:** scopes, `openReads`, `alwaysAuthenticated`, endpoint restrictions and a
  fail-closed SPI for data restrictions.
- **Tickets:** reconnect-aware stream tickets and reusable tile tickets.
- **Phones:** device metadata, and pairing codes with a QR code.
- **Settings:** server settings (`timezone`, `clientLinks`, two https warnings), trusted proxies and
  the security settings endpoints.
- **Audit:** an audit log with a query endpoint and retention.
- **Private sources:** the private-source gate.

The 15 operations owned by 023 are `implemented` and off `pending-operations.txt`. The spec
changelog has a 023 entry, Postman is regenerated, and `./gradlew lintOpenApi` passes with 0 errors
(the 70 warnings are unchanged). Tests were written, not run (feature prompt).

The acceptance criteria are met in code:
- `AccessPolicy` agrees with `x-foresignal-scope` and `x-foresignal-always-authenticated`. A Python
  simulation of the policy table against the spec found no mismatch; `AccessPolicySyncTest` asserts
  it.
- An `agent` token gets 403 on admin areas.
- Pairing never involves the admin token.
- Revocation drops tickets and closes streams after commit.
- The project compiles.

## Files created / modified

**Security (`ai.foresignal.security`)**
- New:
  - entities and repositories: `ApiTokenEntity`, `ApiTokenRepository`, `PrincipalEntity`,
    `PrincipalRepository`;
  - services: `PrincipalService`, `TokenService`, `RestrictionValidator`, `SpecOperationIndex`
    (operation ids and endpoint reachability from the classpath spec);
  - token types: `PrincipalKind`, `TokenRestrictions`, `DeviceInfo`, `TokenCreatedVia`,
    `AuthenticatedToken`, `CurrentToken` (request attribute), `ApiTokenRevokedEvent`;
  - the data-restriction SPI: `RestrictionEnforcer` and its default `FailClosedRestrictionEnforcer`;
  - `SourceVisibilityGate`, `SecureRequestMonitor`, `TicketStrippingRequest`, `Ulids`.
- Rewritten:
  - `TokenFilter`;
  - `AdminTokenService`, which now uses the `bootstrap` row and migrates
    `system_secret['admin_token_hash']`.
- Changed: `AccessRequirement` (`RequiresScope.alwaysAuthenticated`), `AccessPolicy` (023 rules,
  flags, `alwaysAuthenticated()`), `package-info`.
- `security.ticket`: `TicketPurpose`, `Ticket`, `TicketAcceptance`, `TicketStore`.
- `security.pairing`: `PairingCodeEntity`, `PairingCodeRepository`, `PairingCodes`,
  `PairingService`, `PairingRateLimiter` (Bucket4j), `QrSvgRenderer` (ZXing).
- `security.audit`: `AuditEventEntity`, `AuditRecord`, `AuditService` (bounded queue, batch insert),
  `AuditQuery`, `AuditEventView`, `AuditRetentionStep` (order 750).

**API**
- New controllers: `TokensController`, `PairingController`, `AuditController`,
  `ServerSecuritySettingsController`.
- New DTOs: `dto/ApiTokenDto`, `dto/PairingCodeDto`.
- Changed:
  - `SystemController`: the full document needs `read`;
  - `SourcesController`: gated listings, 404 for hidden sources;
  - `RecordsController` and `ChangesController`: 404 for a named hidden source;
  - `support/ReadCallerResolver` and `support/Actors` (`token:<name>`);
  - `error/ErrorCodes`: six codes.

**Settings, system, streams, notify**
- New: `settings/ClientLinks`, `settings/ServerSettingsService`, `settings/SecuritySettingsService`,
  `system/PublicBaseUrlWarnings`.
- Changed:
  - `ServerSettings` (+`clientLinks`), `SecuritySettings` (ignores unknown members),
    `RetentionSettings` (+`audit`, default 90d), `RetentionSettingsService`,
    `SettingsTransferService` (+`security`, `server`), `SystemWarnings` (+two warnings);
  - `stream/sse`: `HeaderStreamPrincipalResolver` (uses `CurrentToken`), `StreamPrincipal`
    (`forToken`), `SseHub` (closes a revoked token's streams), and the private exclusion in
    `ChangesTopic`, `AlertsTopic` and `AbstractNotificationTopic`;
  - `notify/NotificationRouter`: "Open in app" link.
- `application.yml`: `server.forward-headers-strategy: native` and
  `server.tomcat.remoteip.internal-proxies: ${FORESIGNAL_TRUSTED_PROXIES:<loopback>}`.

**Build, spec, docs**
- `gradle/libs.versions.toml`, `build.gradle.kts`: ZXing core.
- `THIRD_PARTY_NOTICES.md`: a ZXing entry.
- `docs/OpenAPI.json`: statuses, error codes, `Problem.requiredScope`, five description
  reconciliations and the changelog.
- `src/test/resources/openapi/pending-operations.txt`: −15 lines.
- `scripts/generate-postman.py` and both `postman/` files.
- `docs/Operator-Guide.md`: a new "Tokens, pairing and the audit log" section, plus `/v1/system`,
  retention, export, remote access and streaming edits.
- `docs/Invariants.md`: SEC-4 lists `TokenFilterIT` and `AccessPolicySyncTest`.
- `docker-compose.yml`: `FORESIGNAL_TRUSTED_PROXIES`.
- `docker/caddy/Caddyfile.example`.

**Tests:** see Tests written (not run).

## Deviations from the prompt

1. **Loopback regex.** The prompt's default `128\.0\.0\.1` is a typo. The default is
   `127\.0\.0\.1|0:0:0:0:0:0:0:1|::1`, in `application.yml` and in the compose default.
2. **No separate `AccessPolicySpecSyncTest` class.** The prompt's checks went into 020's existing
   `AccessPolicySyncTest`:
   - scope plus `alwaysAuthenticated` equality for every implemented route;
   - for planned routes where the policy already grants `read`, the flag must match the spec.

   Checking every planned route's scope would mean transcribing ~730 later operations now. That is
   out of scope, and it is why 020 limited the check to implemented routes.
3. **No new stream resolver.** There is no extra `@Primary StreamPrincipalResolver`. The existing
   `HeaderStreamPrincipalResolver` reads `CurrentToken`, which the filter sets for a bearer token
   and for a ticket alike. The principal id is `token:<token uuid>` (was `token:<hash prefix>`), so
   tickets count against the parent token's stream limits and ownership.
4. **Restriction members later prompts own are refused (400).**
   - Restriction members: `area`, `placeIds`, `excludePrivate`, `maxRequestsPerMinute`,
     `federation`, `includeExercise`.
   - Token members: `privateAccess: true`, a non-empty `licencePolicy`.
   - Security settings: `anonymous`, `metricsOpen`, `exposeMlat`.
   - Server settings: `push` (031).

   Accepting them unenforced would fail open. The spec descriptions say so (changelog).
5. **The pairing QR payload is held in memory.** The code is stored hashed, so the payload cannot
   be rebuilt. `qr.svg` is 404 after a restart, and the operator creates a new code (spec
   description updated).
6. **The private-source gate sits in `SourcesController`** (list, health, get, usage), not inside
   `SourceService`. `SourceService` has no caller context, and the admin and write paths that also
   use it must see every source.
7. **What the audit log adds.**
   - Columns: `principal_id`, per the v1.3 principal note and the spec's `AuditEvent.principalId`.
   - `target_type` and `target_id` are the first two path segments after `/v1`.
   - Pairing claims are audited by `PairingService` with actor `pairing`, successes and failures
     alike.
8. **`publicBaseUrl` is required in `PUT /v1/settings/server`** (null allowed), as the schema's
   `required` says. A settings import that leaves the member out reads it as null.
9. **Bootstrap lookup.** The bootstrap row is found by `created_via = bootstrap`, not by name, so an
   operator token named "bootstrap" cannot shadow it.
10. **`/v1/tokens/{tokenId}` with a non-UUID id is 400.** The spec lists only 404 for this
    operation; the 400 is the standard `VALIDATION_FAILED` problem.

## Issues found / open questions

1. **A newly visible behavior change.** `read` tokens no longer see private sources. Before, any
   token did. This is per the prompt, and only admin tokens existed until now.
2. **A ticket on a covered path is checked even under `openReads`.** An invalid `?ticket=` is a 401
   even on a path that would be open without it. The client mints again, as the spec says.
3. **The principal's `disabled` flag is not checked during authentication.** Nothing can disable a
   principal until 130, which adds that check.
4. **Async audit inserts.** In tests, a write's audit row can land after the next test's truncation.
   `AuditIT` and `PairingIT` flush and clear `audit_event` first; no other test reads the table.
5. **Data:** no instructions directed at an agent were found in data, fixtures or tool output.
6. **Owner decisions carried from 012–022** are still open.

## Tests written (not run)

New:

| Test | What it covers |
|---|---|
| `security/TokenServiceIT` | Create, authenticate, revoke, expire; bootstrap refuses revoke with 409 and authenticates as admin; `last_used_at` throttling; restriction and input validation; principals; device; `current()`; the revoke event after commit |
| `security/TokenFilterIT` | The full matrix of {none, read, agent, admin, revoked, expired, endpoint-restricted, data-restricted} × {read, agent, admin, always op} with `openReads` on and off; `requiredScope`; a ticket in the header; `/v1/system`; actors; agent least privilege |
| `security/AlwaysAuthenticatedIT` | SEC-5 |
| `security/StreamTicketIT` | Over a real socket, with `MutableClock` and timeouts: the first-connect window, reconnects within 5 min, 24 h, one concurrent stream, tile reuse and ttl, wrong purpose, narrowed paths, the revoke cascade (stream closed, tickets invalid), the ticket absent from logs and TileJSON, minting rules |
| `security/pairing/PairingIT` | Create and list; claim once (lower case, no dash); expiry; admin refused; per-IP and global limits with 429 and `Retry-After`; the QR SVG decoded with ZXing back to `qrPayload`; delete; claims audited without the code; a paired token is not admin |
| `security/audit/AuditIT` | Writes audited with token, scope, operation, status and target; GETs not audited; 401/403 audited; no secrets or token values; revocations; filters and keyset paging; the retention step; overflow |
| `security/TrustedProxyIT`, `UntrustedProxyIT` | `X-Forwarded-For` and `X-Forwarded-Proto` honoured only from a configured internal proxy |
| `api/TokensControllerIT`, `api/SecuritySettingsIT`, `api/ServerSettingsIT` | Server settings: the https warnings, the `clientLinks` round trip, export and import of `security` and `server` |
| `notify/ClientLinksNotificationIT` | The "Open in app" link |
| Unit tests | `SourceVisibilityGateTest`, `ticket/TicketStoreTest`, `pairing/PairingCodesTest`, `pairing/QrSvgRendererTest`, `pairing/PairingRateLimiterTest`, `UlidsTest`, `PrincipalServiceTest`, `TicketStrippingRequestTest`, `TokenRestrictionsTest`, `FailClosedRestrictionEnforcerTest`, `SpecOperationIndexTest`, `settings/SecuritySettingsServiceTest`, `settings/ServerSettingsServiceTest`, `system/PublicBaseUrlWarningsTest` |

Test support:
- `testsupport/security/TestTokens`
- `testsupport/security/QrDecoder` (ZXing reader)

Changed:
- `AccessPolicyTest`: `assertScope` compares the scope; the 023 rules and flags are added.
- `AccessPolicySyncTest`: the always-authenticated flag.
- `AdminTokenServiceIT`: the new constructor, the bootstrap row, the two migration cases, rotate
  recreating the row.
- `SchemaBootstrapIT`: four new tables.
- `PostmanCollectionContractTest`: `{{readToken}}` auth, validators for 7 operations, and
  `createToken`/`createPairingCode` schema-only.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL in 3s`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings, 0 errors.

**New dependency:**
- `com.google.zxing:core` 3.5.4, Apache-2.0, no runtime dependencies.
- Verified on Maven Central: `maven-metadata.xml` lists 3.5.4 as latest; the license is in the
  `zxing-parent` POM.
- `java.version` 1.8 bytecode, so it runs on Java 21.

## Notes for the next prompt

1. **024 (ingest keys):** `AccessRequirement.IngestKeyOrAdmin` is still admin-only in `TokenFilter`.
   Add the key path there, and audit with `AuditRecord.INGEST_KEY`.
2. **025 (MCP):**
   - `AccessPolicy.alwaysAuthenticated()` lists the flagged rules.
   - `AuditService.record(...)` is the audit entry point.
   - `CurrentToken.of(request)` gives the caller.
3. **031/032:** listen for `ApiTokenRevokedEvent` (after commit) to delete push subscriptions and
   devices by `token_id`. `PUT /v1/settings/server` refuses `push` until 031 adds it to
   `ServerSettings` and `ServerSettingsService`.
4. **035:** replace `FailClosedRestrictionEnforcer` (a `RestrictionEnforcer` bean). Then:
   - add `RESTRICTED` and `RESTRICTION_NOT_ALLOWED`;
   - accept the `ReadRestriction` members that `RestrictionValidator.LATER_MEMBERS` refuses;
   - add `metricsOpen` and `anonymous` to `SecuritySettingsService`.
5. **039:** fold `SourceVisibilityGate` (used by `ReadCallerResolver`, the SSE topics and
   `SourcesController`) into `EgressPolicy`, and allow `privateAccess`.
6. **130:** principals have no endpoints yet. Add the `disabled` check to `TokenService.authenticate`.
7. **Tests:** contexts that change `security.openReads` must restore it (the token filter caches
   it). Use `TestTokens` to create tokens, and call `AuditService.flush()` before reading
   `audit_event`.
