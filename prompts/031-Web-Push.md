# 031 — Web Push

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/031-CR-Web-Push.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/030-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §24 (Web Push), §17 (channel SPI, message model), §7.1 (`push_subscription`)
- `docs/Product-Charter.md` §8.16 (FR-53)
- `docs/Gap-Analysis.md` §1 item 4, §7 (UnifiedPush row), §8 item 16
- RFC 8030 (Web Push, incl. §5.2 TTL, §5.3 Urgency, §5.4 Topic), RFC 8291 (message encryption), RFC 8188 (`aes128gcm`), RFC 8292 (VAPID), RFC 5869 (HKDF)
- UnifiedPush developer docs: https://unifiedpush.org/developers/intro (app servers "should deny by default endpoints to non-global IPs" and, when self-hostable, offer an allowlist) and the Android spec https://unifiedpush.org/developers/spec/android/ (VAPID key passed to `register`)
- WebKit Declarative Web Push (Safari 18.4 iOS/iPadOS, 18.5 macOS): https://webkit.org/blog/16574/
- `docs/OpenAPI.json`: operations tagged `Push`, schemas `PushSubscription*`, `ServerSettings`
- Completion Reports of 018 (persisted `notification` row, `links`/`shortTitle`, quiet hours, `renderUpdate`) and 023 (device metadata, `alwaysAuthenticated`, revoke cascade hook, `clientLinks`)

## Objective

Built-in push for browsers (PWA, operator console) and for native Android and Linux apps through UnifiedPush, which speaks the same RFC 8030/8291 wire protocol. Elaro holds its own VAPID key, so no third-party push account is needed. Delivery runs through a `PushTransport` SPI so prompt 032 adds APNs and FCM as siblings without refactoring this prompt.

## Tasks

### 1. VAPID keys (`ai.elaro.notify.push`)

- On first start generate a P-256 key pair. Store the public key (uncompressed point, base64url, 87 chars) and the private key (PKCS#8, encrypted with `CredentialCipher`) in `system_secret` (`push.vapid.public`, `push.vapid.private`).
- `GET /v1/push/vapid-public-key` (open) returns `{publicKey}`. The same key is what a Flutter app passes to UnifiedPush `registerApp(vapid: …)`.
- VAPID `sub` = `push.contact` (default `mailto:admin@localhost`). Add `push` to `ServerSettings` (owned by 023): `{contact, allowedPrivateHosts: [], defaultLang: "en"}`. `allowedPrivateHosts` entries are exact hostnames or CIDRs; validate each (no wildcards, CIDR prefix ≥ /8 for IPv4, ≥ /32 for IPv6).

### 2. Encryption and signing (JDK crypto only)

Implement RFC 8291 + RFC 8188 with `java.security`/`javax.crypto`:
1. Ephemeral ECDH P-256 key pair per message; `ecdh_secret = ECDH(as_private, ua_public)`.
2. `PRK_key = HKDF-Extract(auth_secret, ecdh_secret)`; `IKM = HKDF-Expand(PRK_key, "WebPush: info" ‖ 0x00 ‖ ua_public ‖ as_public, 32)`.
3. Random 16-byte salt; `PRK = HKDF-Extract(salt, IKM)`; `CEK = HKDF-Expand(PRK, "Content-Encoding: aes128gcm" ‖ 0x00, 16)`; `NONCE = HKDF-Expand(PRK, "Content-Encoding: nonce" ‖ 0x00, 12)`.
4. Plaintext ‖ `0x02` delimiter, AES-128-GCM, single record.
5. Body = salt ‖ rs (4096, u32) ‖ idlen (65) ‖ as_public ‖ ciphertext.
- HKDF-SHA256 as a small class (`Hkdf`) unless a JDK API exists on Java 21; record which.
- VAPID JWT: ES256 over `{"aud": origin(endpoint), "exp": now+12h, "sub": contact}`; convert DER → raw R‖S (64 bytes). Cache per audience origin for 11 h. Header `Authorization: vapid t=<jwt>, k=<publicKey>`.
- Request headers: `Content-Encoding: aes128gcm`, `Content-Type: application/octet-stream`, `TTL` (alert 86400, briefing 43200, system 3600, verification 600), `Urgency` (critical `high`, warning `normal`, info `low`, quiet-hours downgrade `very-low`), `Topic` (task 5).
- **Byte cap:** the encrypted record must be ≤ 4096 bytes, so the plaintext JSON is ≤ 3993 bytes (4096 − 86 header − 16 tag − 1 delimiter). `PushPayloadBudget` serialises, then shrinks in this order until it fits: truncate `body` (UTF-8-safe, on code-point boundaries, ellipsis), drop `links.api`, cap `tags` at 5, truncate `title` to 120 chars. Never exceed the cap; a payload that still does not fit falls back to `{v, event, id, severity, shortTitle}`.

### 3. PushTransport SPI (shape shared with 032)

- `PushTarget` (sealed): `WebPushTarget(subscriptionId, endpoint, p256dh, auth, payloadFormat)` now; 032 adds `NativeDeviceTarget`.
- `PushTransport`: `String id()`, `boolean supports(PushTarget)`, `PushResult send(PushTarget, PushMessage, PushOptions)`. `PushResult` = `SENT | GONE (delete target) | RETRY(after) | REJECTED(reason) | FAILED(reason)`.
- `PushTargetSource` beans enumerate active targets matching a `NotificationMessage` (`PushFilters`: `events`, `ruleIds`, `minSeverity`, `tags` any-of). `WebPushTargetSource` is the first; 032 adds `NativeDeviceTargetSource`.
- **Fan-out with per-target state:** the built-in channel row (type `webpush`, named "Push", events alert/briefing/system, auto-created, enabled) renders nothing itself. When its `notification_delivery` row is claimed, `PushFanout` inserts one `push_dispatch` row per matching target: `id`, `delivery_id`, `target_type` (`webpush`|…), `target_id`, `status` (`pending|sent|gone|failed`), `attempts`, `next_attempt_at`, `last_status`, `last_error` (≤ 500 chars, endpoint redacted). A `PushDispatchWorker` (2 s poll, `FOR UPDATE SKIP LOCKED`, virtual threads) sends each row with the 018 retry ladder per target, so a retry never re-sends to targets that already succeeded. The parent delivery is `delivered` when every dispatch is terminal and at least one is `sent`, or when there were no targets; `failed` when all failed.
- Outcomes: 201/202 → `sent` and `last_success_at`; 404/410 → `gone` and delete the subscription; 413 → `failed` and log (should never happen given the cap); 429/503 → honour `Retry-After`. Per-host Bucket4j limit (default 20/s per push-service host).

### 4. Subscriptions and validation

- Extend `PushSubscriptionEntity`: `status` (`pending|active`), `verify_code_hash`, `verify_expires_at`, `endpoint_host`, `transport_label` (`browser|unifiedpush|other`, display and filter only; the wire protocol is identical), `payload_format` (`elaro|declarative`), `lang`, `token_id`, `failure_count`, `last_failure_at`, `last_status`.
- `POST /v1/push/subscriptions` (read scope, and `alwaysAuthenticated` per 023, so a token is always required): validate
  - `https` scheme only; no userinfo; endpoint ≤ 1000 bytes
  - key lengths: p256dh 65 bytes uncompressed (base64url-decoded), auth 16 bytes
  - **address policy:** resolve the host; every resolved address must be globally routable (reject loopback, RFC 1918, CGNAT 100.64/10, link-local, ULA fc00::/7, IPv4-mapped/compatible IPv6, NAT64 64:ff9b::/96, 6to4, multicast, 0.0.0.0/8, metadata addresses) **unless** the host or every resolved address matches `push.allowedPrivateHosts`. Reuse `LocalAddresses` from 004 for classification. No global "allow private" flag.
  - caps: ≤ 5 `pending` per token and ≤ 200 subscriptions in total (409 `PUSH_LIMIT_REACHED`)
- **Registration verification:** a new row is `pending`. Elaro immediately sends a verification push `{v:1, event:"verify", subscriptionId, code}` (16 random bytes base64url; only the SHA-256 is stored; TTL 600, Urgency `high`). The client echoes it with `POST /v1/push/subscriptions/{id}/verify {code}` (read scope, creating token only). Match → `active`; mismatch → 400 `VERIFY_FAILED` (5 tries, then delete). A sweeper deletes `pending` rows older than 10 min. Only `active` rows receive notifications. This stops a read-scope caller from using Elaro as a push amplifier.
- **Send-time recheck:** before each send, re-resolve the endpoint host, apply the same address policy, connect to the **checked IP** with the original Host/SNI (reuse the pinned-IP connector from 005 used for `LAN_URL`), never follow redirects. A failed recheck → `failed` with `URL_NOT_ALLOWED`, and the subscription is disabled after 3 consecutive rechecks fail. (Prompt 035 later swaps this check for `OutboundUrlPolicy` profile `PUSH_ENDPOINT`.)
- `DELETE /v1/push/subscriptions/{id}`: creating token or admin, else 404. `GET /v1/push/subscriptions` (admin) lists `endpointHost` only, never full endpoints, with `status`, `transportLabel`, `lastSuccessAt`, `failureCount`.
- **Revoke cascade:** check that 023 deletes subscriptions on token revoke/expiry; if the hook is missing, add a `TokenRevokedEvent` listener that deletes rows with that `token_id`.

### 5. Payload, Topic and urgency

- Elaro payload (`payload_format=elaro`, JSON, UTF-8): `{v:1, event, id (persisted notification id from 018; alert id if 018's row is absent), alertId?, ruleId?, severity, title, shortTitle (≤ 40 chars from 018's shared Truncator), body (plain text ≤ 1000 chars), url (alias of links.api), links {api, app, web} (from 023 clientLinks; omit absent), tags (rule tags), lang (subscription lang → push.defaultLang), tag ("elaro-alert-<alertId>" or "elaro-<event>-<id>", so repeats for one alert replace each other in the OS), silent, ts}`.
- **Declarative format** (`payload_format=declarative`, for Safari 18.4+ without a service worker): `{"web_push": 8030, "notification": {"title", "body", "navigate": links.web ?? links.api, "lang", "tag", "silent", "data": <elaro payload minus title/body>}}`. Verify the member list against the current WebKit documentation and record differences; omit `app_badge` (reported crash in Safari 18.5).
- **Topic header:** `Topic = base64url(SHA-256(key))[0:32]` with key `alert:<ruleId>:<source>:<externalId>` for alerts, `briefing:<briefingId>` for briefings, `system:<code>` for system events. The push service then replaces an undelivered message for the same entity.
- **Quiet hours:** honour the 018 per-channel quiet hours on the Push channel. Mode `downgrade` sends `Urgency: very-low` and `silent: true`; `defer` and `drop` follow 018. Critical bypasses quiet hours.
- Briefings send a 200-char excerpt; system events send title and code only.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: extend `PushSubscriptionInput` (`transportLabel`, `payloadFormat`, `lang`, `filters.tags`), `PushSubscription` (`status`, `transportLabel`, `payloadFormat`, `failureCount`, `lastStatus`); add `POST /v1/push/subscriptions/{subscriptionId}/verify` (`verifyPushSubscription`, `x-elaro-scope: read`); `ServerSettings.push`; problem codes `PUSH_LIMIT_REACHED`, `VERIFY_FAILED`, `URL_NOT_ALLOWED`; a `PushPayload` schema documenting both formats. Update `AccessPolicy`, add a BODIES example for the verify op, regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Architecture.md` §24: rename to "Push" with subsections Web Push, UnifiedPush and "Native push (032)"; describe the SPI and `push_dispatch`. `docs/Product-Charter.md` FR-53: native apps subscribe through UnifiedPush.
- `docs/Operator-Guide.md` "Web Push and UnifiedPush": service-worker snippet (`pushManager.subscribe({userVisibleOnly: true, applicationServerKey})`, POST to Elaro, handle `event: "verify"` by POSTing the code, `showNotification(title, {body, tag, lang, silent, data})`, `notificationclick` opens `links.web`); a Flutter UnifiedPush snippet (pass the VAPID key to `registerApp`, decrypt via the library, echo the verification code); the LAN distributor case (`push.allowedPrivateHosts`, https still required); reach limits (no iOS, no macOS/Windows desktop: use 032 or ntfy).

## Tests to write (do not run)

- `WebPushCryptoTest`: RFC 8291 Appendix A vector reproduced byte-for-byte with its fixed keys and salt; HKDF RFC 5869 vectors; VAPID sign/verify round trip; DER↔raw conversion.
- `PushPayloadBudgetTest`: multibyte titles and bodies, oversize payloads shrink in the stated order, never split a surrogate pair, result always ≤ 3993 bytes; minimal fallback.
- `TopicDerivationTest`: ≤ 32 chars, base64url alphabet, stable per entity.
- `PushAddressPolicyTest`: every blocked range (incl. IPv4-mapped, NAT64, CGNAT, metadata), allowlist by host and by CIDR, DNS returning one public and one private address → rejected.
- `PushSubscriptionsIT`: validation errors (http, private host, bad key lengths, limit), pending → verify → active, wrong code and expiry sweep, delete ownership, admin list hides endpoints, revoke cascade.
- `WebPushDeliveryIT` (WireMock push service): headers (Content-Encoding, TTL, Urgency incl. `very-low` under quiet-hours downgrade, Topic, VAPID Authorization); body decrypts with the subscription's private key; filters (events, ruleIds, minSeverity, tags); only active rows; 410 deletes; 429 retry; a retry of one failed target does not re-send to the others; declarative format shape; DNS rebinding between subscribe and send → blocked.

## Acceptance criteria

- RFC 8291 and HKDF vectors pass (when tests run in 041).
- A verified subscription receives encrypted alerts within the 4096-byte record limit; unverified subscriptions receive nothing but the verification push.
- Private-network endpoints work only via `push.allowedPrivateHosts`, and are rechecked at send time.
- 032 can add a transport by implementing `PushTransport` and `PushTargetSource` only.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 118, 119, 120).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `031: Web Push`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/031-CR-Web-Push.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `031: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
