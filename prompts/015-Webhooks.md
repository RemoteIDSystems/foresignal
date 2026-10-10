# 015 — Webhooks

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/015-CR-Webhooks.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/014-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §8.4 (WebhookDispatcher), §7.1 (`webhook`, `webhook_delivery`)
- `docs/Product-Charter.md` FR-28
- `docs/OpenAPI.json`: operations tagged `Webhooks`, schemas `Webhook`, `WebhookInput`, `WebhookTestResult`, `WebhookDelivery`, `WebhookDeliveryPage`

## Objective

Push alerts to the operator's own endpoints (ntfy, Home Assistant, Discord bridges, custom scripts) reliably, with signed payloads and durable retries.

## Tasks

### 1. Webhook management (`ai.elaro.trigger.webhook`)

- **CRUD:** `listWebhooks`, `createWebhook` (201 + Location), `getWebhook`, `updateWebhook` (full replace; omitting `secret` keeps the existing one), `deleteWebhook` (204; pending deliveries are cancelled, i.e. marked failed with reason "webhook deleted").
- **Validation:**
  - `url` is absolute `http` or `https`, ≤ 2000 chars
  - `name` 1–200 chars
  - `ruleIds`: null (all rules) or a list of existing rule ids
- **Secret:** encrypted with `CredentialCipher` (`secret_ciphertext` / `secret_nonce`) and never returned; `secretSet` reflects its presence.
- **Response fields:** `lastDeliveryAt` and `failedLast24h` computed from deliveries.
- All endpoints, including GETs, require the admin token (already covered by the filter's path policy; verify).

### 2. Delivery pipeline

- **`AlertBus` listener:** for each new alert, insert one `webhook_delivery` (status `pending`, `next_attempt_at = now`) per enabled webhook whose `ruleIds` is null or contains the alert's rule.
- **`WebhookDeliveryWorker`:** polls every 2 s.
  - Claim due deliveries with `SELECT … WHERE status='pending' AND next_attempt_at <= now() ORDER BY next_attempt_at LIMIT 50 FOR UPDATE SKIP LOCKED`, inside a short transaction that pushes `next_attempt_at` forward as a lease.
  - Send on virtual threads with a dedicated `HttpClient`: 5 s connect timeout, 10 s request timeout, **no redirects**. This traffic does not use `UpstreamClient` and is not budgeted.
- **Request:**
  - `POST` with `Content-Type: application/json`
  - body `{"alert": Alert, "rule": TriggerRule, "record": Record}`, using the same DTOs as the API
  - headers: `X-Elaro-Event: alert`, `X-Elaro-Delivery: <delivery id>`, `X-Elaro-Timestamp: <unix seconds>`, `User-Agent: <install UA>`, and when a secret is set, `X-Elaro-Signature: sha256=<hex HMAC-SHA256 of "<timestamp>.<body>">`
  - document the signature scheme in `docs/Operator-Guide.md` (create it if absent; prompt 018 extends it), including a verification snippet
- **Outcome:**
  - a 2xx response sets `delivered`
  - otherwise increment `attempts`, store `last_error` (status or exception, ≤ 500 chars) and schedule a retry after 10 s, 1 m, 5 m, 30 m, then 2 h
  - after the 5th failed retry, set `failed`
- **Test endpoint (`testWebhook`):** synchronously sends a synthetic alert (flagged `"test": true` in the body) and returns `WebhookTestResult`. Nothing is persisted.
- **Deliveries list (`listWebhookDeliveries`):** newest first, filter by `status`, keyset cursor.

## Tests to write (do not run)

- `WebhookValidatorTest`.
- `HmacSignerTest`: known-answer vectors.
- `WebhookDeliveryWorkerIT` (WireMock as the receiver):
  - delivery on alert, with the correct headers and a verifiable signature
  - rule filtering
  - retry schedule progression (`MutableClock`), ending in `failed`
  - `SKIP LOCKED` prevents double send when two workers run concurrently
  - redirect responses are not followed
- `WebhooksControllerIT`: CRUD; secret never returned and kept on update; 401 for GETs without the token; test endpoint; deliveries pagination.

## Acceptance criteria

- All `Webhooks` operations match `docs/OpenAPI.json`.
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
2. **Commit** all changes with the message `015: Webhooks`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/015-CR-Webhooks.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `015: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
