# 018 — Notification Channels

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/018-CR-Notification-Channels.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/017-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §17 (types, message model, routing, delivery), §7.1 (`notification_channel`, `notification_delivery`)
- `docs/Gap-Analysis.md` §7 (notification destinations), §6 ("Notification inbox", "Localized notifications"), §8.A item 8 (this prompt's extensions; "extend 015" = 018)
- `docs/Weather-Imagery-Design.md` §8 (attachments model; the Teams fix) and §10.1 (row 015 = 018)
- `docs/Product-Charter.md` §8.6 (FR-35 to FR-38)
- `docs/OpenAPI.json`: operations tagged `Channels`; schemas `Channel`, `ChannelInput`, `ChannelType`, `ChannelTestResult`, `NotificationDelivery`, `NotificationDeliveryPage`
- Completion Report of 017: the `AlertBus` event shape and the alert lifecycle (fired, acknowledged, snoozed, resolved)
- Upstream docs: Discord webhooks (execute with `?wait=true`, edit message), Telegram Bot API (`sendMessage`, `editMessageText`), Matrix client-server spec (`m.replace` relations), Pushover API (emergency priority, receipts), ntfy publish headers, Home Assistant REST API (`/api/webhook/{id}`, `/api/services/notify/{service}`), apprise-api README, Zulip REST API (`POST /api/v1/messages`)

## Objective

Deliver alerts, briefings and system events to the destinations an operator actually uses, formatted natively for each. Every message is persisted as a `notification` row before fan-out, deliveries are durable and rate-limited per destination, and alert lifecycle changes (017) update the original message in place where the destination allows it. Quiet hours, a global pause and backlog folding keep a storm from flooding a phone.

## Tasks

### 1. Replace the webhook entities from prompt 002

Delete `WebhookEntity`, `WebhookDeliveryEntity`, their repositories and every reference. Add (Hibernate, `ai.foresignal.store`):
- `NotificationChannelEntity` (`notification_channel`) per Architecture §7.1, plus `timezone varchar(64)`, `quiet_hours jsonb`, `group_window_seconds int default 0`, `backlog_cap int default 20`, `edit_in_place bool default true`.
- `NotificationEntity` (`notification`): `id bigint identity`, `event` (`alert|briefing|system|test`), `subtype varchar(64)` (`alert.fired`, `alert.resolved`, `alert.acknowledged`, `system.source_failing`, `system.delivery_failed`, …), `severity`, `title`, `short_title`, `body_markdown`, `url`, `links jsonb`, `data jsonb`, `alert_id`, `briefing_run_id`, `rule_id`, `tags text[]`, `update_of bigint null` (the notification this one updates), `origin varchar(64)`, `created_at`. Index `(created_at)`, `(alert_id)`. Every `NotificationMessage` is persisted, **including system events with no matching channel** (prompt 033 builds the inbox on this table).
- `NotificationDeliveryEntity` (`notification_delivery`) per §7.1, plus `notification_id`, `kind` (`send|update|followup|digest`), `provider_message_ref varchar(512)`, `deferred_reason varchar(32)` (`quiet_hours|paused|enrichment|grouping`), `note varchar(200)`. Index `(status, next_attempt_at)`, `(channel_id, id DESC)`, `(notification_id)`.

Hibernate `update` leaves the old webhook tables in dev databases; note that in the CR. Reuse `DeliveryStatus`.

### 2. Message model and SPI (`ai.foresignal.notify`)

- **`NotificationMessage`** record: `id` (notification id), `event`, `subtype`, `title`, `shortTitle` (≤ 40 chars, for watches, mesh and push), `bodyMarkdown`, `severity`, `url`, `links` (`List<Link(label, url)>`, max 3), `data`, `tags`, `attachments` (`List<Attachment(filename, contentType, byte[] bytes, String mediaId)>`, empty in this prompt; prompt 055 fills it), `updateOf` (nullable), `origin` (install id).
- **`ChannelType`**: `id()`, `spec()` (`ChannelTypeSpec`: config and secret fields, served by `GET /v1/channels/types`), `capabilities()` (`editInPlace`, `attachments`, `fanOut`), `validate(config, secrets)`, `render(msg, settings) → PreparedRequest`, `Optional<PreparedRequest> renderUpdate(msg, providerMessageRef, settings)` (empty = send a follow-up message instead), `Optional<String> extractRef(response)`, `rateLimit()`.
- `PreparedRequest`: method, URL, headers, and a body that is bytes, form fields, **or multipart parts** (so 055 only adds parts), or an SMTP `MimeMessage` builder for email. Types that do not support attachments ignore them without error.
- `fanOut` exists so `webpush` (031) and `mqtt` (060) fit the SPI without refactoring.
- **`MarkdownRenderer`**: Foresignal's subset (headings, bold, italic, links, bullet lists, code) to Slack mrkdwn (escape `&`, `<`, `>`), Telegram HTML (allowed tags only, entities escaped), Matrix HTML, Discord markdown, Adaptive Card TextBlock markdown, Zulip markdown, plain text and email HTML.
- **`Truncator`** (shared by every type): truncates by characters or UTF-8 bytes on a grapheme boundary (`BreakIterator`), never inside an HTML tag or entity, and appends "…" plus a link to `url` when space allows.
- **`OriginMarker`**: every outbound request carries the instance id (`InstallIdService`) as `X-Foresignal-Origin` where headers are possible, and as `foresignalOrigin` in JSON bodies (webhook, Apprise, Home Assistant) or an `foresignal-origin` tag (ntfy, Gotify extras). Prompt 024's ntfy/Gotify-compatible publish and ingest formats drop inbound messages that carry this instance's marker (loop guard). Document the contract in the class Javadoc.

### 3. Implement every type

Each type is its own class with golden-fixture payload tests:

| Type | Notes |
|---|---|
| `webhook` | Default body `{"event","subtype","message": NotificationMessage,"data","foresignalOrigin"}`, or an operator `bodyTemplate` (JSON-escaped `{{…}}`). Headers `X-Foresignal-Event`, `X-Foresignal-Delivery`, `X-Foresignal-Timestamp`, `X-Foresignal-Origin`, `User-Agent`; with `hmacSecret`, `X-Foresignal-Signature: sha256=<hex HMAC-SHA256("<timestamp>.<body>")>`. POST or PUT; extra headers whose values may be secret. Updates are follow-ups with `subtype` set. |
| `slack` | Incoming webhook. Block Kit: header, mrkdwn section, context (severity, time), link buttons from `url` and `links`; `text` fallback. Incoming webhooks cannot edit, so updates are follow-ups. |
| `mattermost` | Incoming webhook. `text` plus `attachments` with colour by severity; optional `channel`, `username`, `icon_url`. Follow-ups. |
| `discord` | `POST {webhookUrl}?wait=true`; one embed (title, description ≤ 4096, colour, url, timestamp). Store the returned message `id`. **Update:** `PATCH {webhookUrl}/messages/{id}`. |
| `teams` | **Power Automate Workflows URLs only.** Office 365 connector webhooks were retired in Teams in May 2026: reject any `*.webhook.office.com` or `outlook.office.com/webhook` URL with a 400 that tells the operator to create a Workflows webhook. Body `{"type":"message","attachments":[{"contentType":"application/vnd.microsoft.card.adaptive","content":<AdaptiveCard 1.5>}]}`. Follow-ups. |
| `google-chat` | Card v2 with header and text paragraph. Follow-ups. |
| `telegram` | `sendMessage` with `chat_id`, `parse_mode=HTML`, `disable_web_page_preview`; ≤ 4096 chars via `Truncator`. Store `result.message_id`. **Update:** `editMessageText` (`editMessageCaption` when the original was a photo, for 055). |
| `matrix` | `PUT {homeserver}/_matrix/client/v3/rooms/{roomId}/send/m.room.message/{txnId}` (txnId = delivery id, so retries are idempotent); `org.matrix.custom.html`. Store `event_id`. **Update:** a new event with `body` "* …", `m.new_content` and `m.relates_to {rel_type:"m.replace", event_id}`. |
| `ntfy` | `POST {serverUrl}/{topic}`: `Title`, `Priority` (info 3, warning 4, critical 5), `Tags`, `Click`, `Actions` (view actions from `links`), `Markdown: yes`; optional bearer. Config `call` (phone number or `yes`): send `X-Call` on **critical** only (needs an ntfy account with a verified number). Follow-ups. |
| `gotify` | `POST {serverUrl}/message` with `X-Gotify-Key`: priority info 4, warning 6, critical 9; markdown `extras`. Follow-ups. |
| `pushover` | `messages.json`: `html=1`, priority info 0, warning 1, critical 2 (`retry=300`, `expire=3600`), `url`. Store the `receipt` for priority 2. **On resolve or acknowledge:** `POST https://api.pushover.net/1/receipts/{receipt}/cancel.json` (stops the emergency retries), then a follow-up. |
| `email` | Jakarta Mail (`spring-boot-starter-mail`); a `JavaMailSender` per channel (host, port, TLS mode, auth). Multipart alternative text + HTML; config `plainTextOnly` sends `text/plain` only (Winlink gateways). Follow-ups. |
| `homeassistant` | Config `baseUrl`, `mode` (`webhook` default, or `notify`). **webhook:** `POST {baseUrl}/api/webhook/{webhookId}` (secret `webhookId`) with the default webhook JSON body, for automations. **notify:** `POST {baseUrl}/api/services/notify/{service}` with bearer `accessToken`, `{title, message, data}`; on critical, `data` asks for iOS critical sound (`push.sound {name, critical:1, volume:1.0}`) and the Android alarm stream (`ttl:0, priority:high, channel:alarm_stream`), switchable by config. Follow-ups. |
| `apprise` | apprise-api: `POST {serverUrl}/notify/{configKey}` (or stateless `/notify/` with secret `urls`), `{title, body, type: info\|warning\|failure, format: markdown, tag}`. **HTTP 424 means at least one target failed while others succeeded: mark delivered with a warning in `last_error`, never retry** (a retry would duplicate the successful targets). Follow-ups. |
| `zulip` | `POST {site}/api/v1/messages`, form `type=stream`, `to`, `topic`, `content`; Basic auth with bot email + secret API key. Topic from `topicTemplate` (default `{{ruleName}}`), so each rule gets its own topic. Follow-ups land in the same topic. Rate limit 200/min. |

`webpush` arrives in 031 and `mqtt` in 060. Rate limits: Architecture §17 for the original types; Home Assistant 10/s, Apprise 5/s, Zulip 200/min.

### 4. Routing (`NotificationRouter`, `NotificationService`)

- Subscribe to `AlertBus` (017). Lifecycle events map to subtypes `alert.fired`, `alert.acknowledged`, `alert.snoozed`, `alert.resolved` (check 017's event names). Channel selection: enabled, `events` contains `alert`, `rule_ids` null or containing the rule, `severity ≥ min_severity`.
- **Fired:** title and body from channel templates or defaults (title `"[{{severity}}] {{name}}"`; body = alert message, record summary, `{{summary}}`, link). Templates use 017's `MessageTemplate` plus `{{severity}}`, `{{summary}}`, `{{url}}`, `{{ruleName}}`.
- **Lifecycle updates:** persist a notification with `update_of` = the fired notification. For each channel that received the original, enqueue `kind=update` when the type supports edit-in-place, the channel has `edit_in_place=true` and the original delivery stored a `provider_message_ref`; otherwise `kind=followup`. The edited text shows the new state ("Resolved 14:02 — …") with strike-through or a ✅ marker per format.
- **Template filters** (added to `MessageTemplate`): `{{observedAt|local}}` formats in the channel `timezone` (IANA; default the `server` setting's `timezone`, else UTC; 023 exposes it) with a fixed pattern `yyyy-MM-dd HH:mm zzz`; `{{observedAt|ago}}` renders "3 min ago". java.time only, no ICU4J.
- `url = <publicBaseUrl>/v1/alerts/{id}`. `server.publicBaseUrl` (default `http://localhost:8080`) is stored with `AppSettingService` under key `server`; prompt 023 adds its API. `links` defaults to the alert link; 023 adds `clientLinks` templates.
- Enrichment wait: with `wait_for_enrichment_seconds > 0` and rule `enrich=true`, set `next_attempt_at = now + wait`, `deferred_reason=enrichment` (030 brings it forward).
- `NotificationService.publish(NotificationMessage, Predicate<Channel> extraFilter)` for briefings (029) and system events.
- **System events:** a source entering `failing` (subscribe to `SourceHealthChangedEvent`; add it to `HealthTracker` if 006 did not), and permanent delivery failure. Never notify a channel about its own failure.

### 5. Quiet hours, pause, folding, grouping

- **Per-channel `quietHours`:** `{start:"22:00", end:"07:00", days:["MON",…], bypassSeverity:"critical"}` in the channel timezone. Deliveries inside the window get `next_attempt_at` = window end, `deferred_reason=quiet_hours`. At release, more than one deferred alert for a channel is sent as one `kind=digest` message listing them (newest first, capped by `backlog_cap`).
- **Global pause:** `app_setting['notifications'] = {pausedUntil, pauseBypassSeverity:"critical"}`. Paused deliveries are deferred the same way and released as digests.
- **Backlog cap and folding:** when a channel has more than `backlog_cap` pending `send` deliveries, the oldest beyond the cap are folded into one digest ("+N more alerts") and marked `delivered` with `note='folded into delivery <id>'`.
- **Opt-in grouping:** `group_window_seconds > 0` holds the first alert per (channel, rule) for the window and sends one grouped message for everything that fired in it.
- Lifecycle updates and system events are never folded into another rule's digest.

### 6. Delivery worker

- Poll every 2 s; claim due rows with `SELECT … FOR UPDATE SKIP LOCKED LIMIT 50` in a short lease transaction; send on virtual threads with a dedicated `HttpClient` (5 s connect, 15 s request, **no redirects**).
- Bucket4j bucket per channel sized by `rateLimit()`; no capacity → reschedule without counting an attempt.
- Outcome: 2xx → `delivered`, store `extractRef`; 429 → honour `Retry-After` (counts as an attempt at most once per 10 min); other failures → `attempts++`, `last_error` (≤ 500 chars, redacted), retries at 10 s, 1 m, 5 m, 30 m, 2 h, then `failed` plus a system event. Apprise 424 per §3.
- Redact secrets everywhere: Slack, Discord, Teams, Telegram and Home Assistant webhook URLs embed secrets in the path.
- Channels may target private-network hosts (operator-configured). Prompt 035 retrofits the outbound URL policy.

### 7. API (`/v1/channels`, admin)

- `GET /v1/channels/types`; `GET/POST /v1/channels` (201 + Location); `GET/PUT/DELETE /{id}` (PUT is a full replace; omitted `secrets` keeps existing, a provided object replaces them; DELETE fails pending deliveries with "channel deleted"). Responses never include secrets; they list `secretFields`, `lastDeliveryAt`, `failedLast24h`.
- `POST /{id}/test`: synchronous synthetic `test` message → `ChannelTestResult`; persists nothing.
- `GET /{id}/deliveries`: newest first, `status` filter, keyset cursor; items gain `notificationId`, `kind`, `deferredReason`, `edited` (bool).
- `GET/PUT /v1/settings/notifications` (`NotificationSettings`: `pausedUntil`, `pauseBypassSeverity`).

### 8. Spec, Postman, docs

- `docs/OpenAPI.json`: add `homeassistant`, `apprise`, `zulip` to the `Channel`/`ChannelInput`/`ChannelType` enums; add `timezone`, `quietHours`, `groupWindowSeconds`, `backlogCap`, `editInPlace` to `Channel` and `ChannelInput`; extend `NotificationDelivery`; add schemas `NotificationMessage`, `NotificationLink`, `QuietHours`, `NotificationSettings`; add `getNotificationSettings`/`updateNotificationSettings` with `"x-foresignal-scope": "admin"` (covered by the existing `/v1/settings/**` AccessPolicy rule; verify). Add `notifications` to `SettingsBundle`.
- Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`); add BODIES for `createChannel` variants and `updateNotificationSettings`.
- `docs/Operator-Guide.md`: a "Notification channels" section with setup steps per type (Teams Workflows only; HA webhook vs notify mode; ntfy `call` prerequisites; Apprise 424 semantics; Zulip bot), edit-in-place support matrix, quiet hours and pause, webhook signature verification, the origin marker.

## Tests to write (do not run)

- `<Type>ChannelTest` for all 15 types: golden fixtures in `src/test/resources/fixtures/notify/<type>.json`; escaping (`<`, `&`, `*`, `_`, emoji), a 5,000-char body truncated, severity → priority/colour, origin marker present.
- `EditInPlaceTest`: Discord PATCH, Telegram `editMessageText`, Matrix `m.replace` payloads; follow-up for a type without edit support; follow-up when no ref was stored.
- `TeamsChannelTest`: `webhook.office.com` and `outlook.office.com/webhook` URLs rejected with 400.
- `TruncatorTest` (graphemes, bytes vs chars, HTML entity boundaries), `MarkdownRendererTest`, `HmacSignerTest` (known-answer vectors), `TemplateFiltersTest` (`|local` across DST in `America/Chicago`, `|ago`).
- `NotificationRouterIT`: rule and severity filters, a notification row per message (and for a system event with no channels), lifecycle update enqueues `update` vs `followup`, enrichment wait, system events never sent to the failing channel.
- `QuietHoursIT` (`MutableClock`): deferral, critical bypass, digest at window end; global pause; backlog folding at the cap; grouping window.
- `DeliveryWorkerIT` (WireMock; GreenMail or equivalent, version verified on Maven Central): per-type success and ref capture, retry ladder ending in `failed`, 429 `Retry-After`, Apprise 424 not retried, Pushover receipt cancel on resolve, per-channel rate limit, no double send with two workers, redirects not followed, secrets redacted.
- `ChannelsControllerIT`: CRUD, secrets never returned, PUT secret semantics, 401 without a token, types catalogue, test endpoint, deliveries pagination, notification settings round trip.

## Acceptance criteria

- All `Channels` operations and the notification settings operations match `docs/OpenAPI.json`.
- No webhook-only code remains; every message has a `notification` row.
- A resolved alert edits the original Discord, Telegram and Matrix message; other types get a follow-up.
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
2. **Commit** all changes with the message `018: Notification Channels`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/018-CR-Notification-Channels.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `018: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
