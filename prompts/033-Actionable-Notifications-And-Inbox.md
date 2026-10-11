# 033 — Actionable Notifications And Inbox

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/033-CR-Actionable-Notifications-And-Inbox.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/032-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 6, §2 (Flutter desktop/tray and Local web/PWA rows), §6 (Notification inbox row), §7 (Actionable notifications, Quiet hours/snooze, Escalation policies rows), §8 item 19 ("027b")
- `docs/Architecture.md` §17 (channels, delivery worker), §24 (Push, as revised by 031/032)
- Completion Reports of 017 (alert lifecycle: `state`, `acknowledged_by`, snooze, `alert.resolved`, AlertBus events), 018 (persisted `notification` row, `provider_message_ref` + `renderUpdate`, quiet hours, Pushover receipt cancel, Truncator), 023 (device metadata, audit log), 031, 032 (`ClientDeviceResolver`, `PushMessage.badge`)
- W3C TAG "Good Practices for Capability URLs" (2014 draft, not a standard); ntfy action buttons https://docs.ntfy.sh/publish/#action-buttons; Telegram Bot API `InlineKeyboardButton.callback_data` (1–64 bytes), `answerCallbackQuery`, `editMessageReplyMarkup`; Matrix `m.reaction` (`m.annotation`) and `/sync`; Pushover receipts API https://pushover.net/api/receipts; MDN `Notification.maxActions`

## Objective

Let the operator act on an alert from the notification itself (acknowledge, snooze, mute) without putting a token on the device, keep an inbox that every client can sync with per-device read state and badge counts, hold phone notifications back while the operator is at the desktop, and escalate unacknowledged alerts to further channels until someone acknowledges or the alert resolves.

## Tasks

### 1. Prerequisite hooks (check, add if missing)

- From 017: `alert.state` (`firing|acknowledged|resolved`), `acknowledged_by`, `resolved_at`, alert snooze (`snoozed_until`), and AlertBus lifecycle events (`AlertAcknowledged`, `AlertResolved`, `AlertSnoozed`). Add rule mute if absent: `trigger_rule.muted_until` (required expiry, max 7 d); a muted rule still records alerts but they are not routed; `POST/DELETE /v1/triggers/{id}/mute` (agent). Record what you added.
- From 018: the persisted `notification` row (id, event, alert_id, briefing_run_id, severity, title, body, created_at; system events included), `notification_delivery.provider_message_ref`, `ChannelType.renderUpdate`, per-channel quiet hours. Add the `notification` row in `NotificationService.publish` if 018 did not.

### 2. Action capabilities (`ai.elaro.notify.action`)

- `action_capability`: `id` bigint, `token_hash` (SHA-256), `alert_id`, `action` (`ack|snooze|mute`), `params` jsonb (`{duration}` for snooze 1 h default / mute 4 h default; allowed 15m–24h snooze, 1h–7d mute), `channel_id`, `delivery_id`, `device_id` null, `expires_at` (default 12 h, max 24 h), `used_at`, `used_via`, `result`.
- Token format `eac_<22 chars base64url(16 random bytes)>.<11 chars base64url(HMAC-SHA256(k, random)[0..8])>` (≈ 38 chars, fits Telegram's 64-byte `callback_data`). `k` is a 32-byte key in `system_secret['action.hmac']`, encrypted. Verify the MAC before any DB lookup (constant time), then the row: unknown, expired or used → `410 ACTION_EXPIRED`.
- `ActionService.mint(alert, channel, delivery, actions)` creates one capability per action at render time. `execute(token, via)` runs in one transaction with `SELECT … FOR UPDATE`: marks it used (single use), then calls the 017 services. Ack sets `acknowledged_by = "action:<channelType>:<channelName>"`; snooze sets the alert snooze; mute sets the rule's `muted_until`. Acting on an already acknowledged or resolved alert returns 200 with `result: "already-<state>"`. Audit every execution (023 `audit_event`).
- Capabilities for an alert are revoked (marked used, `result: superseded`) when the alert is acknowledged or resolved by any path.

### 3. Endpoints and confirm page

- `GET /v1/actions/{token}`: **never acts.** Returns a minimal server-rendered HTML confirm page (alert title, severity, fired time, the action and duration, one `<form method="post">` button, no JavaScript, no external assets), or `410` page. Headers: `Cache-Control: no-store`, `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex`, `Content-Security-Policy: default-src 'none'; style-src 'unsafe-inline'; form-action 'self'`. Escape all alert text.
- `POST /v1/actions/{token}`: executes. `Accept: application/json` → `{action, alertId, result}`; otherwise an HTML result page. Requests with an `Origin` header that is not the configured `publicBaseUrl` origin are rejected (403) to block cross-site form posts; push service workers and ntfy send no Origin or the right one.
- `AccessPolicy`: `/v1/actions/*` is `OPEN` (the capability is the credential); add a Bucket4j limit of 30 requests/min per client IP on this path. Redact `/v1/actions/*` tokens in access logs (path suffix replaced with `eac_…`).
- `GET /v1/notifications/{id}` (task 5) returns fresh capability URLs minted for the calling device, so native apps never receive capabilities through Apple or Google.

### 4. Rendering per channel

Add a per-channel config block `actions: {enabled (default true for the types below), list: [ack, snooze, mute], links: true}`; actions are attached only to `alert` events in state `firing`.
- **ntfy:** `Actions` header with up to 3 entries: `http, Acknowledge, <base>/v1/actions/<t>, method=POST, clear=true; http, Snooze 1h, …, method=POST, clear=true; view, Open, <links.web|links.api>`.
- **Web Push (031):** payload `actions: [{action, title, url}]` (≤ 2) plus `confirmUrl`. Service-worker snippet: on `notificationclick` with `event.action`, `fetch(url, {method: "POST", headers: {Accept: "application/json"}})` and close; feature-detect `Notification.maxActions` (Safari has none; Firefox 152+ has actions) and fall back to opening the confirm page. Declarative payloads use `navigate` to the confirm page only.
- **Native (032):** set `aps.category` / data `c` to `ELARO_ALERT_ACTIONABLE`; the app's action handler POSTs to the capability URL it obtained from `GET /v1/notifications/{id}` (the NSE fetches and caches it).
- **Telegram:** inline keyboard with `callback_data = "ea:" + token` buttons. A `TelegramCallbackPoller` per telegram channel with actions enabled long-polls `getUpdates` (`allowed_updates=["callback_query"]`, persisted `offset` in `app_setting['telegram.<channelId>.offset']`); checks `from.id` against the channel's `allowedUserIds` (empty → any member of `chatId`); executes; calls `answerCallbackQuery` with the result; then `renderUpdate` (018) edits the message to show "Acknowledged by <first_name>" and removes the keyboard. Document that the bot token must be dedicated to Elaro (`getUpdates` conflicts with webhooks). Design the poller so 061 can add command handling to it.
- **Matrix:** append confirm-page links ("Acknowledge · Snooze 1h · Mute 4h") to `formatted_body`. Optional `actions.reactions: true`: a `/sync` loop filtered to the room and `m.reaction` events (persisted `since` token) treats a ✅ reaction from a user in `allowedSenders` on Elaro's message (`provider_message_ref` event id) as ack.
- **Pushover:** critical alerts already use priority 2 (018). Store the `receipt`; a `PushoverReceiptPoller` polls `GET /1/receipts/{receipt}.json` every 60 s (≥ 5 s rule) until acknowledged or expired, and turns `acknowledged=1` into an Elaro ack (`acknowledged_by = "pushover:<acknowledged_by_device>"`). Send `url` = confirm page for ack.
- **All other types** (email, slack, discord, mattermost, teams, google-chat, gotify, webhook): when `actions.links` is on, add confirm-page links to the body (webhook: an `actions` array in the default JSON body).

### 5. Notification inbox (`/v1/notifications`)

- `GET /v1/notifications?after=&before=&events=&minSeverity=&unread=&limit=` (read scope, `alwaysAuthenticated`): newest first, keyset on id; items `{id, event, severity, title, body, alertId, briefingRunId, ruleId, createdAt, read, alertState}` (join the alert for live state). `GET /v1/notifications/{id}` adds `links` and `actions` (capabilities minted for the calling device, 12 h).
- `notification_read_cursor` (`device_key` PK, `read_up_to_id`, `updated_at`) plus `notification_read` (`device_key`, `notification_id`) for sparse reads above the cursor (delete entries at or below the cursor whenever it moves; cap 500 per device by dropping the oldest, which only makes them show as unread again, never as read). `device_key` = `ClientDeviceResolver.currentDevice()` (032), else the `Elaro-Device-Id` header (UUID, ≤ 64 chars) prefixed with the token id, else 400 `DEVICE_ID_REQUIRED`.
- `POST /v1/notifications/read` `{upToId}` or `{ids: [...]}` (read scope): moves the cursor or adds sparse reads. **Reading never acknowledges an alert;** say so in the spec description.
- `GET /v1/notifications/badge` → `{unread, unreadCritical, latestId}` over `event in (alert, system)` and alerts still `firing`. Feed the same count into `PushMessage.badge` (032 `aps.badge`) per device.
- `GET /v1/notifications/stream` (read): SSE through the generic channel from 015 (`id:` = notification id, replay from the table on `Last-Event-ID`), events `notification` and `badge` (sent to the same device key when its cursor moves, so other windows clear their badge).
- Retention: key `notifications` (default `30d`) in `RetentionSettings`; delete cursors for devices unseen for 90 d.

### 6. Presence-aware deferral

- `client_presence` (`device_key` PK, `kind` `desktop|mobile|web`, `state` `active|idle|background`, `expires_at`, `updated_at`). `POST /v1/clients/presence {kind, state, ttlSeconds ≤ 300}` (read scope) upserts; `GET /v1/clients` (admin) lists. An authenticated open `/v1/notifications/stream` or `/v1/alerts/stream` from a desktop counts as `background` presence only, never `active`.
- Channel option `deferWhileActiveSeconds` (0–600, default 0; meant for phone-type channels: webpush/push, ntfy, pushover, telegram, gotify). When a non-critical alert is routed and any `desktop` presence is `active` and unexpired, set the delivery's `next_attempt_at = now + defer` and `deferred_reason = 'presence'`.
- At claim time, if the alert was acknowledged, snoozed, resolved, or the notification was read on an active desktop, mark the delivery `suppressed` (add the status; visible in `/deliveries`) instead of sending. Otherwise send.
- **Critical always sends immediately.** Stale or expired presence fails open (deliver everywhere). System events are never deferred.

### 7. Escalation policies

- `escalation_policy`: `id` uuid, `name`, `steps` jsonb `[{afterMinutes (1–1440, strictly increasing), channelIds [], includePush (bool)}]` (max 5 steps), `repeat` `{everyMinutes, maxTimes}` (optional, max 10), `stopOn` (`ack` always, plus `resolve` default true, `snooze` default true), `created_at`, `updated_at`. `trigger_rule.escalation_policy_id` (nullable; validate on rule save).
- `alert_escalation`: `alert_id` PK, `policy_id`, `next_step` int, `next_at`, `repeats_done`, `state` (`active|stopped|exhausted`), `stopped_reason`, `updated_at`. Created when an alert from a rule with a policy is inserted (step 0 is the rule's normal routing).
- `EscalationWorker` every 15 s: claim due rows with `FOR UPDATE SKIP LOCKED`; if the alert is acknowledged/resolved/snoozed per `stopOn`, stop; else insert `notification_delivery` rows for the step's channels (title prefixed `[Escalation <n>]`, fresh action capabilities), advance `next_step`/`next_at`, handle `repeat`, set `exhausted` after the last step. Quiet hours apply to escalation deliveries except critical.
- Stop immediately on AlertBus ack/resolve/snooze: mark `stopped`, cancel pending escalation deliveries (`suppressed`), and trigger 018's Pushover receipt cancel. Muting a rule stops escalation for its open alerts.
- API (agent scope): `GET/POST /v1/escalation-policies`, `GET/PUT/DELETE /v1/escalation-policies/{id}` (delete → 409 `POLICY_IN_USE` while rules reference it); `GET /v1/alerts/{id}/escalation` (read).

### 8. MCP and Ask

- MCP tools (025 registry): `list_notifications` (read), `snooze_alert` and `mute_watch` (agent), `list_escalation_policies` (read). Ask gains a `propose_snooze` proposed action.

### 9. Spec, Postman, docs

- `docs/OpenAPI.json`: `getActionConfirm` (GET `/v1/actions/{token}`, `text/html`, `x-elaro-scope: open`), `executeAction` (POST, `open`); tag `Notifications`: `listNotifications`, `getNotification`, `markNotificationsRead`, `getNotificationBadge`, `streamNotifications`, `updatePresence` (read), `listClients` (admin); tag `Escalation`: CRUD (`agent`), `getAlertEscalation` (`read`); `muteTrigger`/`unmuteTrigger` if added; `ChannelInput.config.actions`, `deferWhileActiveSeconds`, `TriggerRuleInput.escalationPolicyId`, delivery status `suppressed`; codes `ACTION_EXPIRED`, `DEVICE_ID_REQUIRED`, `POLICY_IN_USE`. Update `AccessPolicy` (`/v1/actions/*` open before the default), BODIES examples, regenerate Postman.
- `docs/Architecture.md` §17 (actions, deferral, escalation) and a new "Notification inbox" subsection; Charter FRs for actions, inbox and escalation.
- `docs/Operator-Guide.md` "Acting from notifications": per-channel behaviour table, Telegram bot dedication, Pushover receipts, why GET only confirms, presence deferral and escalation examples. `docs/Client-Guide.md`: inbox sync (cursor + SSE), device id header, presence heartbeats, service-worker action snippet.

## Tests to write (do not run)

- `ActionTokenTest`: format and length ≤ 64, MAC rejects tampering before DB access, constant-time compare.
- `ActionServiceIT`: single use under two concurrent POSTs (one wins), expiry, ack/snooze/mute effects and `acknowledged_by`, already-acknowledged result, supersede on ack via API, audit rows.
- `ActionEndpointIT`: GET never changes state (assert alert unchanged), confirm-page headers and escaping of hostile titles, POST JSON vs HTML, foreign Origin 403, rate limit, log redaction.
- `ActionRenderingTest` (golden fixtures): ntfy Actions header, Web Push actions + confirmUrl, Telegram keyboard ≤ 64-byte callback data, Matrix links, webhook `actions` array.
- `TelegramCallbackPollerIT` / `PushoverReceiptPollerIT` / `MatrixReactionIT` (WireMock): allowed vs disallowed users, offset/since persistence, message edited after ack, receipt ack becomes Elaro ack.
- `NotificationInboxIT`: listing and filters, cursor + sparse reads, badge math, read does not ack, per-device isolation, SSE replay and `badge` event, retention.
- `PresenceDeferralIT` (`MutableClock`): deferred when desktop active, suppressed when read/acked before release, sent when not, critical immediate, stale presence fails open.
- `EscalationWorkerIT`: steps fire at the right times, stop on ack/resolve/snooze/mute, repeat and exhaustion, two workers never double-send, quiet hours respected except critical.

## Acceptance criteria

- One tap in ntfy, Telegram or a supporting browser acknowledges an alert without any token on the device; every GET link only opens a confirm page.
- Two clients of the same operator show independent read state and correct badges; reading never acknowledges.
- A non-critical alert reaches the phone only if it is still unread and unacknowledged after the deferral; critical always goes immediately.
- An unacknowledged alert escalates per its policy and stops on ack or resolve.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `033: Actionable Notifications And Inbox`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/033-CR-Actionable-Notifications-And-Inbox.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `033: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
