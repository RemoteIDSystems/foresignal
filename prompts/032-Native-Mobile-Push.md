# 032 — Native Mobile Push

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/032-CR-Native-Mobile-Push.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/031-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 4, §2 (Flutter mobile row), §7 (APNs / FCM and deep-link rows), §8 item 18 ("027a")
- `docs/Architecture.md` §24 as rewritten by 031 (PushTransport SPI, `push_dispatch`), §17, §15
- Completion Reports of 031 (SPI, fan-out, payload budget), 023 (device metadata on `api_token`, pairing, `clientLinks`, `alwaysAuthenticated`, `TokenRevokedEvent`) and 018 (persisted `notification` row, quiet hours, Truncator)
- APNs: token-based connection https://developer.apple.com/documentation/usernotifications/establishing-a-token-based-connection-to-apns, sending requests https://developer.apple.com/documentation/usernotifications/sending-notification-requests-to-apns, handling responses, `UNNotificationServiceExtension`
- FCM HTTP v1: https://firebase.google.com/docs/cloud-messaging/send/v1-api, auth https://firebase.google.com/docs/cloud-messaging/auth-server, errors https://firebase.google.com/docs/cloud-messaging/error-codes

Verified 2026-10:
- APNs auth keys are now either **team-scoped and environment-specific** (Sandbox or Production, max 2 per environment; legacy keys valid for both still work) or **topic-specific** (one environment, up to 400 topics per key). Store the key's environment and pick the endpoint per device.
- Provider JWT: ES256, header `kid`, claims `iss` (Team ID) and `iat`; refresh no more than once per 20 min and no less than once per 60 min (`TooManyProviderTokenUpdates` / `ExpiredProviderToken`).
- APNs server certificates moved to the USERTrust RSA root in 2025; the JDK `cacerts` includes it. Do not pin.
- FCM legacy APIs are gone; only HTTP v1 with an OAuth 2.0 access token (scope `https://www.googleapis.com/auth/firebase.messaging`). `404 UNREGISTERED` means the token is dead.

## Objective

Deliver alerts to the native Flutter apps on iOS (APNs) and Android (FCM) as two new `PushTransport`s behind the 031 fan-out, with device registration at `/v1/push/devices`. Pushes are **visible alerts** whose payload carries only ids, severity and minimal text. A Notification Service Extension (iOS) or the app's message handler (Android) fetches the details from Foresignal over the user's own network path. Each operator supplies their own APNs key and Firebase service account; a hosted relay is out of scope.

## Tasks

### 1. Provider configuration (`ai.foresignal.notify.push.provider`, admin only)

- `PushProviderEntity` (`push_provider`): `id` slug PK, `type` (`apns|fcm`), `app_ids` jsonb (bundle ids / Android package names served by this key), `config` jsonb, `secret_ciphertext`/`secret_nonce` (`CredentialCipher`), `enabled`, `last_test_at`, `last_error`, `updated_at`.
  - APNs config: `teamId` (10 chars), `keyId` (10 chars), `keyEnvironment` (`sandbox|production|both`), `timeSensitive` (bool, default false: set only when the app has the Time Sensitive capability). Secret: the `.p8` PEM (validate it parses as an EC P-256 PKCS#8 key).
  - FCM config: `projectId`, `clientEmail`, `tokenUri` (default `https://oauth2.googleapis.com/token`). Secret: the service-account private key (accept the whole service-account JSON on input, extract the fields, store only `private_key` encrypted).
- API: `GET /v1/push/providers`, `PUT /v1/push/providers/{providerId}` (full replace; omitted `secrets` keeps them), `DELETE /v1/push/providers/{providerId}`, `POST /v1/push/providers/{providerId}/test` (`{deviceId?}`): mint a JWT/OAuth token, and when `deviceId` is given send a test alert to it. Responses never contain secrets; `secretFields` lists what is set. Include providers in settings export **without** secrets.
- Document the self-hosting constraint plainly: APNs keys belong to the Apple team that signs the app and FCM credentials to the Firebase project in the app. An operator must build and sign their own app (or use a publisher's relay, not provided). UnifiedPush (031) and ntfy remain the no-account paths.

### 2. Device registration (`/v1/push/devices`)

- `PushDeviceEntity` (`push_device`): `id` uuid, `transport` (`apns|fcm`), `platform` (`ios|ipados|macos|android`), `app_id`, `environment` (`sandbox|production`, APNs only), `token_ciphertext`/`token_nonce`, `token_hash` (SHA-256, unique with `transport`), `token_id` (the api_token that registered it), `name`, `filters` jsonb (031 `PushFilters`), `content_level` (`minimal` default | `short`), `lang`, `status` (`active|disabled`), `created_at`, `last_success_at`, `last_failure_at`, `failure_count`, `last_status`.
- `POST /v1/push/devices` (read scope, `alwaysAuthenticated`): validate APNs token as 64–200 hex chars, FCM token 1–4096 chars; require an enabled provider whose `app_ids` contains `appId` (409 `PUSH_PROVIDER_NOT_CONFIGURED`); for APNs require the provider's `keyEnvironment` to cover `environment`. Upsert by (`transport`, `token_hash`): re-registering moves the row to the calling token. Copy `platform`/`appVersion`/`deviceName` from the 023 device metadata on the calling token when present. Cap 20 devices per token (409 `PUSH_LIMIT_REACHED`).
- `GET /v1/push/devices`: admin sees all; read/agent tokens see only their own. Never return tokens; return `tokenSuffix` (last 6 chars). `PUT /v1/push/devices/{deviceId}` (filters, name, contentLevel, lang), `DELETE` (owner or admin, else 404), `POST /v1/push/devices/{deviceId}/test` (owner; 5 per hour).
- **Revoke cascade:** a `TokenRevokedEvent` listener (from 023) deletes the token's devices; token expiry is swept hourly.
- `ClientDeviceResolver` (shared with 033): the device identity for a request is the api_token id when the token carries 023 device metadata. Expose `currentDevice()` for later prompts.

### 3. APNs transport (`ApnsTransport`)

- HTTP/2 over TLS with `java.net.http.HttpClient` (`Version.HTTP_2`), one client per provider; hosts `api.push.apple.com` and `api.sandbox.push.apple.com` (port 443) chosen by the device's `environment`.
- `ApnsJwtCache`: ES256 JWT (`{"alg":"ES256","kid":keyId}` / `{"iss":teamId,"iat":now}`), DER → raw R‖S, regenerated after 40 min; on `403 ExpiredProviderToken` regenerate once and retry; never regenerate more often than every 20 min otherwise.
- Request `POST /3/device/{token}` with headers `authorization: bearer <jwt>`, `apns-topic: <appId>`, `apns-push-type: alert`, `apns-priority: 10` (5 under quiet-hours downgrade), `apns-expiration` (now + TTL: alert 24 h, briefing 12 h, system 1 h), `apns-collapse-id` (≤ 64 bytes: `a-` + first 40 base64url chars of the 031 Topic hash), `apns-id` (the `push_dispatch` id as UUID).
- Body (≤ 4096 bytes, enforced by the 031 budget helper):
  `{"aps":{"alert":{"title":T,"body":B},"sound":"default","mutable-content":1,"interruption-level":L,"thread-id":"foresignal-<ruleId|event>","category":"FORESIGNAL_<EVENT>"},"e":{"v":1,"id":<notificationId>,"a":<alertId>,"s":"<severity>","ev":"<event>","l":"<links.app>"}}`
  - `interruption-level`: info `passive`, warning `active`, critical `time-sensitive` when the provider sets `timeSensitive`, else `active`. Never `critical` (needs an Apple entitlement). Quiet-hours downgrade → `passive`, no `sound`.
  - Text by `content_level`: `minimal` → title "Foresignal", body "<Severity> alert" / "Briefing ready" / "System notice"; `short` (operator opt-in per device) → title "Foresignal · <severity>", body = `shortTitle` (≤ 40 chars). Full titles and bodies never leave the server through Apple.
  - **No data-only tickles:** never send `apns-push-type: background` / `content-available` for alerts; iOS throttles them and drops them for force-quit apps.
- Responses: 200 → sent; 410 `Unregistered` or 400 `BadDeviceToken`/`DeviceTokenNotForTopic` → gone (delete device); 413 → failed; 429 `TooManyRequests` and 5xx → retry with backoff; 403 `InvalidProviderToken`/`TooManyProviderTokenUpdates` → provider error: mark `push_provider.last_error`, raise a `system` notification (never routed back to this channel), retry later.

### 4. FCM transport (`FcmTransport`)

- `FcmAccessTokenCache`: build an RS256 JWT `{iss: clientEmail, scope: "https://www.googleapis.com/auth/firebase.messaging", aud: tokenUri, iat, exp: iat+3600}`, POST `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=<jwt>` to `tokenUri`, cache until `expires_in − 300 s`. JDK crypto only; do not add `firebase-admin` (record the decision).
- `POST https://fcm.googleapis.com/v1/projects/{projectId}/messages:send` with `{"message":{"token":…, "data":{"v":"1","id":…,"a":…,"s":…,"ev":…,"l":…,"t":<minimal or short text>}, "android":{"priority":"HIGH"|"NORMAL","ttl":"86400s","collapse_key":<collapse id>}}}`. Android uses **data messages**, HIGH for warning/critical and NORMAL for info or quiet-hours downgrade; the app must display a notification for every HIGH message (FCM deprioritises HIGH messages that show nothing). For devices with `platform` ios/macos registered via FCM, add an `apns` block with the headers and `aps` payload from task 3.
- Errors: 404 `UNREGISTERED`, 403 `SENDER_ID_MISMATCH`, 400 `INVALID_ARGUMENT` whose details name the token → gone; 429 `QUOTA_EXCEEDED` → honour `Retry-After`; 500 `INTERNAL`/503 `UNAVAILABLE` → retry with backoff; 401/`THIRD_PARTY_AUTH_ERROR` → provider error as in task 3.

### 5. Fan-out integration

- `NativeDeviceTargetSource` (031 SPI) returns active devices whose filters match; `NativeDeviceTarget(deviceId, transport, …)`; both transports register as `PushTransport` beans. Per-target state goes in 031's `push_dispatch`; outcomes update the device row.
- `PushMessage` gains nullable `badge` (filled by 033) and `category`. Deep link `l` comes from 023 `clientLinks.app` (default custom scheme `foresignal://alert/{id}`, URL-encoded substitution); verified Universal/App Links are optional and only work for operators who build their own app.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json` (tag `Push`): `listPushProviders`/`putPushProvider`/`deletePushProvider`/`testPushProvider` (`x-foresignal-scope: admin`); `registerPushDevice`, `listPushDevices`, `updatePushDevice`, `deletePushDevice`, `testPushDevice` (`read`); schemas `PushProvider`, `PushProviderInput`, `PushDevice`, `PushDeviceInput`, `NativePushPayload`; codes `PUSH_PROVIDER_NOT_CONFIGURED`, `PUSH_LIMIT_REACHED`. `AccessPolicy` rules (`/v1/push/providers/**` admin before `/v1/push/devices/**` read). BODIES examples for every write; regenerate Postman.
- `docs/Architecture.md` §24 "Native push", Charter FR-35/FR-53 updated.
- `docs/Operator-Guide.md` "Native push": creating an environment-specific APNs key, the Firebase service account, configuring providers, `contentLevel` privacy trade-off, NSE reachability (LAN, VPN or Tailscale) and the fallback text.
- `docs/Client-Guide.md` (create if absent) "Native push for the Flutter app": APNs registration and environment; NSE (Swift) that reads `e.id`, fetches `GET /v1/notifications/{id}` (033; `/v1/alerts/{id}` until then) with the device token from the Keychain under a 5 s budget and rewrites title/body, else keeps the fallback; `FORESIGNAL_*` categories (actions arrive in 033); Android channels per severity and the HIGH-means-visible rule; `foresignal://` deep links; Live Activities stay on the client roadmap.

## Tests to write (do not run)

- `ApnsJwtCacheTest`: claims and header, raw signature verifies with the public key, reuse inside 40 min, refresh after, single forced refresh on `ExpiredProviderToken`.
- `FcmAccessTokenCacheTest` (WireMock token endpoint): assertion claims, caching, refresh before expiry.
- `ApnsPayloadTest` / `FcmPayloadTest`: minimal vs short text, interruption levels and priority per severity and downgrade, collapse id ≤ 64 bytes, ≤ 4096 bytes, no title/body beyond the content level.
- `PushProvidersIT`: CRUD, secrets never returned, invalid `.p8` and service-account JSON rejected, export omits secrets.
- `PushDevicesIT`: validation, provider-required 409, environment mismatch, upsert moves ownership, per-token visibility, revoke cascade.
- `NativePushDeliveryIT` (WireMock over HTTP/2 for APNs, HTTP for FCM): headers and bodies, 410/UNREGISTERED delete the device, 429 retry, provider auth error raises one system event, filters honoured, a failed APNs target does not block a successful FCM target in the same delivery.

## Acceptance criteria

- With a configured provider, a registered iOS or Android device receives a visible alert containing only ids, severity and minimal (or opted-in short) text.
- Dead tokens are removed automatically; revoking a token removes its devices.
- No secret, device token or full alert text appears in responses, logs or `last_error`.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `032: Native Mobile Push`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/032-CR-Native-Mobile-Push.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `032: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
