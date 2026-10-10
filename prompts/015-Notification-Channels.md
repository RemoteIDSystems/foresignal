# 015 — Notification Channels

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/015-CR-Notification-Channels.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/014-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §17 (notification channels: types, message model, routing, delivery), §7.1 (`notification_channel`, `notification_delivery`)
- `docs/Product-Charter.md` §8.6
- `docs/OpenAPI.json`: operations tagged `Channels`, schemas `Channel`, `ChannelInput`, `ChannelType`, `ChannelTestResult`, `NotificationDelivery`, `NotificationDeliveryPage`

## Objective

Push alerts (and, later, briefings and system events) to wherever the operator lives: generic webhooks, Slack, Mattermost, Discord, Microsoft Teams, Google Chat, Telegram, Matrix, ntfy, Gotify, Pushover and email. Payloads are correctly formatted for each destination, deliveries are durable and rate-limited per destination, and generic webhooks are signed.

## Tasks

### 1. Replace the webhook entities from prompt 002

Delete `WebhookEntity`, `WebhookDeliveryEntity`, their repositories and any references. Add `NotificationChannelEntity` and `NotificationDeliveryEntity` per Architecture §7.1. Hibernate `update` leaves the old tables in existing dev databases; that is harmless, so note it in the CR. `DeliveryStatus` can be reused.

### 2. Channel type SPI (`ai.elaro.notify`)

- `ChannelType` interface: `String id()`, `ChannelTypeSpec spec()`, `void validate(config, secrets)`, `PreparedRequest render(NotificationMessage msg, ChannelSettings settings)`, `RateLimit rateLimit()`.
  - `ChannelTypeSpec` lists config fields and secret fields: name, type, required, description, default. It is served by `GET /v1/channels/types`.
  - `render` produces method, URL, headers and body, or an SMTP message for email.
- **`NotificationMessage`:** `event` (`alert`/`briefing`/`system`/`test`), `title`, `bodyMarkdown`, `severity`, `url`, `data` (JSON of the alert, briefing run or system event), `id`.
- **`MarkdownRenderer` utilities:** convert Elaro's limited markdown subset (headings, bold, italic, links, bullet lists, code) to each target syntax, with correct escaping.
  - Slack mrkdwn: escape `&`, `<`, `>`.
  - Telegram HTML: only allowed tags, entities escaped.
  - Matrix `formatted_body`: HTML.
  - Discord markdown.
  - Teams Adaptive Card `TextBlock` markdown.
  - Plain text, and HTML for email.

### 3. Implement every type

Each type is its own class with fixture-based payload tests:

| Type | Notes |
|---|---|
| `webhook` | Default body `{"event", "message": NotificationMessage, "data"}`, or an operator `bodyTemplate` (JSON with `{{…}}` placeholders, JSON-escaped). Headers `X-Elaro-Event`, `X-Elaro-Delivery`, `X-Elaro-Timestamp`, `User-Agent`, and when an `hmacSecret` is set, `X-Elaro-Signature: sha256=<hex HMAC-SHA256 of "<timestamp>.<body>">`. Method POST or PUT. Extra headers, whose values may be secret. |
| `slack` | Incoming webhook. Block Kit: header block (title), section (mrkdwn body), context (severity and time), and a link button to `url`. Fall back to `text` for notifications. |
| `mattermost` | Incoming webhook. `text` plus `attachments` with `color` by severity; optional `channel`, `username`, `icon_url`. |
| `discord` | Webhook `?wait=true`. One embed: title, description (≤ 4096), colour by severity, url, timestamp. |
| `teams` | Workflows (Power Automate) webhook URL. Body `{"type":"message","attachments":[{"contentType":"application/vnd.microsoft.card.adaptive","content":<AdaptiveCard 1.5>}]}`. |
| `google-chat` | Webhook URL. Card v2 with header and text paragraph. |
| `telegram` | `POST https://api.telegram.org/bot<token>/sendMessage` with `chat_id`, `parse_mode=HTML`, `disable_web_page_preview`. Message ≤ 4096 characters (truncate with a "…" link to `url`). |
| `matrix` | `PUT {homeserver}/_matrix/client/v3/rooms/{roomId}/send/m.room.message/{txnId}`. `txnId` is the delivery id, so retries are idempotent. Bearer `accessToken`; `msgtype m.text` with `format org.matrix.custom.html`. |
| `ntfy` | `POST {serverUrl}/{topic}` with the body as text and headers `Title`, `Priority` (info 3, warning 4, critical 5), `Tags`, `Click`, `Markdown: yes`; optional bearer token. |
| `gotify` | `POST {serverUrl}/message?token=…` (or the `X-Gotify-Key` header): `{title, message, priority (info 4, warning 6, critical 9), extras: {"client::display": {"contentType": "text/markdown"}}}`. |
| `pushover` | `POST https://api.pushover.net/1/messages.json`: `token`, `user`, `title`, `message`, `html=1`, `priority` (info 0, warning 1, critical 2 with `retry=300` and `expire=3600`), `url`. |
| `email` | Jakarta Mail (add `spring-boot-starter-mail`). Build a `JavaMailSender` per channel from its config (host, port, TLS mode, auth). Multipart alternative text and HTML; subject is the title. |
| `webpush` | **Not here.** Prompt 027 adds it. Make sure the SPI accommodates a type that fans out to many subscriptions. |

### 4. Routing (`NotificationRouter`)

- Listens on `AlertBus` (prompt 014). For each alert, select enabled channels where `events` contains `alert`, `rule_ids` is null or contains the rule, and `severity ≥ min_severity`.
- **Message:** title and body from the channel's templates if set, else from defaults. Default title: `"[{{severity}}] {{name}}"`. Default body: the alert message, then the record summary (kind, source, observed time, coordinates), then `{{summary}}` when present, then a link.
  - Templates use `MessageTemplate` from prompt 014, plus `{{severity}}`, `{{summary}}` and `{{url}}`.
  - `url` is `<publicBaseUrl>/v1/alerts/{id}`. `publicBaseUrl` is a new app setting `server.publicBaseUrl` (default `http://localhost:8080`), settable through the export/import bundle and stored with `AppSettingService`.
- Insert a `notification_delivery` for each matched channel. If the channel's `wait_for_enrichment_seconds > 0` and the rule has `enrich=true`, set `next_attempt_at = now + wait`; prompt 026 can bring it forward when the enrichment completes.
- **API for later prompts:** `NotificationService.publish(NotificationMessage, event, Predicate<Channel> extraFilter)`, used by briefings (prompt 025) and system events.
- **System events:** emit `system` notifications when a source enters `failing` (subscribe to health transitions: add a `SourceHealthChangedEvent` in `HealthTracker` if one does not exist; record it in the CR) and when a delivery fails permanently. Never notify the failing channel about its own failure.

### 5. Delivery worker

- Poll every 2 s. Claim due rows with `SELECT … FOR UPDATE SKIP LOCKED LIMIT 50` inside a short lease transaction.
- Send on virtual threads, using a dedicated `HttpClient` (5 s connect, 15 s request, **no redirects**) and the mail sender for email.
- **Rate limits:** a Bucket4j bucket per channel, sized by its type's `rateLimit()` (Architecture §17). When there is no capacity, reschedule a short time ahead without counting an attempt.
- **Outcome:**
  - 2xx: `delivered`.
  - 429: honour `Retry-After`; this doesn't count as an attempt more than once per 10 minutes.
  - Other failures: `attempts++`, `last_error` (≤ 500 characters, secrets redacted), next retry at 10 s, 1 m, 5 m, 30 m, 2 h; then `failed` plus a system event.
- Secrets never appear in logs or `last_error`. Redact URLs containing tokens: Slack, Discord, Teams and Telegram embed secrets in the URL.

### 6. API (`/v1/channels`, admin scope for everything)

- `GET /v1/channels/types` returns the type catalogue with field specs.
- `GET /v1/channels`, `POST` (201 + Location), `GET /{id}`, `PUT /{id}` (full replace; omitted `secrets` keeps existing secrets, while a provided `secrets` object replaces them entirely), `DELETE /{id}` (pending deliveries are marked failed with reason "channel deleted").
  - Responses never include secrets; `secretFields` lists which secret names are set.
  - Responses include `lastDeliveryAt` and `failedLast24h`.
- `POST /{id}/test` sends a synthetic `test` message synchronously and returns `ChannelTestResult` (`success`, `status`, `durationMs`, `error`). Nothing is persisted.
- `GET /{id}/deliveries`: newest first, filter by `status`, keyset cursor.

## Tests to write (do not run)

- **`<Type>ChannelTest` for every type:** render a fixed `NotificationMessage` and assert against a golden JSON or text fixture (`src/test/resources/fixtures/notify/<type>.json`). Cover the escaping edge cases (`<`, `&`, `*`, `_`, emoji, a 5,000-character body truncated) and the severity → priority and colour mappings.
- `MarkdownRendererTest`: each target syntax.
- `HmacSignerTest`: known-answer vectors; timestamp included.
- `NotificationRouterIT`: rule filtering, min severity, events filter, template rendering, the enrichment wait offsets `next_attempt_at`, system events (source failing, permanent failure, never sent to the failing channel itself).
- `DeliveryWorkerIT` (WireMock receivers; GreenMail or an equivalent embedded SMTP server, verifying the version on Maven Central):
  - successful delivery per type
  - retry ladder with `MutableClock` ending in `failed`
  - 429 with `Retry-After`
  - per-channel rate limiting
  - no double send with two workers (`SKIP LOCKED`)
  - redirects not followed
  - secrets redacted in `last_error`
- `ChannelsControllerIT`: CRUD; secrets never returned; `secretFields` correct; secrets kept or replaced on PUT; 401 without the token; `types` catalogue; test endpoint; deliveries pagination.

## Acceptance criteria

- All `Channels` operations in `docs/OpenAPI.json` are implemented exactly.
- No webhook-only code remains.
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
2. **Commit** all changes with the message `015: Notification Channels`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/015-CR-Notification-Channels.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `015: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
