# 027 — Web Push

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/027-CR-Web-Push.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/026-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §24 (Web Push), §17 (channel SPI)
- `docs/Product-Charter.md` §8.16 (FR-53)
- RFC 8030 (Web Push), RFC 8291 (message encryption, `aes128gcm`), RFC 8188 (encrypted content encoding), RFC 8292 (VAPID)
- `docs/OpenAPI.json`: operations tagged `Push`, schemas `PushSubscription*`

## Objective

Built-in browser push notifications, so future frontends (a PWA, the operator console) can receive alerts and briefings natively, with no third-party push service account.

## Tasks

### 1. VAPID keys (`ai.elaro.notify.push`)

- On first start, generate a P-256 key pair. Store the public key (uncompressed point, base64url) and the private key (PKCS#8, encrypted with `CredentialCipher`) in `system_secret`.
- `GET /v1/push/vapid-public-key` (open) returns `{publicKey}`.
- The VAPID `sub` claim is `mailto:` plus an operator-configurable contact. Add `push.contact` to `ServerSettings` (default `mailto:admin@localhost`) and update the spec and Postman if needed; record that in the CR.

### 2. Encryption and signing, using JDK crypto only

Implement RFC 8291 and RFC 8188 with `java.security` and `javax.crypto`:

1. Generate an ephemeral ECDH P-256 key pair per message.
2. `ecdh_secret = ECDH(as_private, ua_public)`.
3. `PRK_key = HKDF-Extract(auth_secret, ecdh_secret)`; `IKM = HKDF-Expand(PRK_key, "WebPush: info" || 0x00 || ua_public || as_public, 32)`.
4. A random 16-byte salt; `PRK = HKDF-Extract(salt, IKM)`; `CEK = HKDF-Expand(PRK, "Content-Encoding: aes128gcm" || 0x00, 16)`; `NONCE = HKDF-Expand(PRK, "Content-Encoding: nonce" || 0x00, 12)`.
5. Plaintext plus the `0x02` padding delimiter, AES-128-GCM.
6. Body = header (salt ‖ record size 4096 ‖ keyid length 65 ‖ as_public) ‖ ciphertext.

- Implement HKDF-SHA256 yourself (a small class) or use a JDK API if one is available on Java 21. Record which.
- **VAPID JWT:** ES256 over `{"aud": origin(endpoint), "exp": now+12h, "sub": contact}`. Convert the DER signature to raw R‖S (64 bytes). Header: `Authorization: vapid t=<jwt>, k=<publicKey>`.
- **Request:** `POST endpoint` with `Content-Encoding: aes128gcm`, `TTL: 86400`, `Urgency` (critical `high`, warning `normal`, info `low`) and `Content-Type: application/octet-stream`.

### 3. Subscriptions and channel

- `PushSubscriptionEntity` per §7.1. `POST /v1/push/subscriptions` (read scope):
  - validate the endpoint as https, its host against a deny-list of private and link-local ranges (push services are public), and the key lengths (p256dh 65 bytes uncompressed, auth 16 bytes)
  - store with filters
  - `DELETE` is allowed for the creating token or an admin, else 404
  - `GET` lists for admin only (`endpointHost` only, never full endpoints)
- **The `webpush` ChannelType** (prompt 015 SPI): one built-in channel row named "Web Push" (auto-created, enabled, events alert, briefing and system) that fans out to every subscription whose filters match (events, ruleIds, minSeverity).
  - Payload JSON `{title, body (plain text ≤ 1000 chars), severity, url, event, id}`.
  - The delivery outcome per subscription is recorded on the subscription (`last_success_at`). 404 or 410 deletes the subscription. 429 honours `Retry-After`.
  - A delivery row counts as delivered when at least one subscription succeeds, or when there are no matching subscriptions.
- **Docs:** `docs/Operator-Guide.md` gets a "Web Push" section with a minimal service-worker and subscribe snippet for frontend developers (`pushManager.subscribe({userVisibleOnly: true, applicationServerKey})`, then POST to Elaro).

## Tests to write (do not run)

- `WebPushCryptoTest`: **RFC 8291 Appendix A test vector**. With the RFC's fixed keys, salt and plaintext, the output must equal the RFC's ciphertext exactly. Also an HKDF RFC 5869 test vector, and a round trip of the VAPID signature verified with the public key, plus DER↔raw conversion.
- `PushSubscriptionsIT`: create with validation (http endpoint, private host, bad key lengths → 400), delete ownership, admin list hides full endpoints.
- `WebPushDeliveryIT` (WireMock as the push service): headers (`Content-Encoding`, `TTL`, `Urgency`, VAPID `Authorization`), the body decrypts with the subscription's private key in the test, filters applied, 410 deletes the subscription, 429 retry.

## Acceptance criteria

- RFC test vectors pass (when tests run in prompt 061).
- A subscribed endpoint receives encrypted alerts.
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
2. **Commit** all changes with the message `027: Web Push`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/027-CR-Web-Push.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `027: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
