# 018-CR-Notification-Channels

| | |
|---|---|
| Prompt | 018-Notification-Channels.md |
| Status | Complete |
| Implementation commit | e3e91be00a1bcf7aae1a26e264a6ee5cc9a4b419 |
| Date | 2026-10-11 |

## Summary

This prompt delivers alerts and system events to 15 destination types, each in its own native
format. Every message is persisted as a `notification` row before fan-out. Deliveries are durable,
rate-limited per channel and retried on a ladder. An alert's lifecycle changes edit the original
Discord, Telegram and Matrix message, and every other type gets a follow-up. Quiet hours, a global
pause, backlog folding and opt-in grouping keep a storm from flooding a phone.

The 10 operations assigned to 018 in `docs/OpenAPI.json` are implemented and marked
`x-foresignal-status: implemented`:

- `listChannelTypes`, `listChannels`, `createChannel`, `getChannel`, `updateChannel`,
  `deleteChannel`, `testChannel`, `listChannelDeliveries`
- `getNotificationSettings`, `updateNotificationSettings`

The `notifications` and `system` topics of `/v1/events/stream` are registered too. The spec
assigns them to 018, and 017's notes asked for them.

**Store (`ai.foresignal.store`).**

- `WebhookEntity`, `WebhookDeliveryEntity` and their repositories are deleted; nothing else
  referenced them except `EntityMappingIT`.
- `NotificationChannelEntity` (`notification_channel`) has the Architecture §7.1 columns plus
  `timezone`, `quiet_hours` jsonb, `group_window_seconds` (default 0), `backlog_cap` (default 20)
  and `edit_in_place` (default true).
- `NotificationEntity` (`notification`) has every column of task 1. `tags` is `text[]`, and there
  are indexes on `(created_at)` and `(alert_id)`.
- `NotificationDeliveryEntity` (`notification_delivery`) has the §7.1 columns plus
  `notification_id`, `kind`, `provider_message_ref`, `deferred_reason` and `note`, and the three
  indexes the prompt lists. Deviation 3 lists the further columns.
- `DeliveryStatus` is reused, now with `wire()` and `fromWire()`. `DeliveryKind` and
  `DeferredReason` hold the string values.

**Message model and SPI (`ai.foresignal.notify`).**

- `NotificationMessage` with `Link` and `Attachment`. Attachments stay empty until 055.
- The `ChannelType` SPI: `id`, `spec`, `capabilities`, `validate`, `render`, `renderUpdate`,
  `extractRef` and `rateLimit`, plus `beforeUpdate` (Pushover's receipt cancel) and
  `partialSuccess` (Apprise 424).
- `ChannelTypeRegistry` collects every `ChannelType` bean, so 031 and 060 add a type by declaring
  a bean. `ChannelCapabilities` includes `fanOut`.
- `PreparedRequest` bodies are bytes, form fields, multipart parts or `Mail`.
- `MarkdownRenderer` renders 9 formats: plain, Slack mrkdwn, Discord, Telegram HTML, Matrix HTML,
  Adaptive Card, Zulip, Google Chat and email HTML.
- `Truncator` cuts on grapheme boundaries by characters or UTF-8 bytes, never inside a tag or
  entity, closes open tags and adds "…" plus a link.
- `OriginMarker` defines the loop-guard contract in its Javadoc, with `isOwn` for 024.
- `HmacSigner` signs webhooks; `Redactor` keeps secrets out of anything stored or logged.

**Types (`ai.foresignal.notify.types`), one class each, all as task 3 describes:**

- `webhook`, `slack`, `mattermost`, `discord`, `teams`, `google-chat`, `telegram`, `matrix`
- `ntfy`, `gotify`, `pushover`, `email`, `homeassistant`, `apprise`, `zulip`

**Routing.**

- `NotificationRouter` subscribes to `AlertBus` at the new `StartupOrder.NOTIFY_ROUTER = 18`,
  before the engine, and skips events flagged `suppressed`:
  - `fired` becomes `alert.fired`;
  - `acknowledged`, `snoozed` and `resolved` become updates with `update_of`. The text shows the
    new state, for example "✅ Resolved 14:02 CDT — …" with the old body struck through.
  - `updated` notifies nobody.
- It also routes three system events:
  - `SourceHealthChangedEvent` into `failing` (`system.source_failing`);
  - `RuleStormEvent` (`system.rule_storm`);
  - permanent delivery failures (`system.delivery_failed`), never sent to the failing channel.
- `NotificationService.publish(message, Predicate<NotificationChannel>)` always persists, then
  enqueues for each accepting channel. A channel accepts when it is enabled, subscribes to the
  event, the severity is at or above its minimum, and for alerts the rule passes its filter.
  `publishUpdate` enqueues `update` or `followup` per task 4.
- `AlertTemplates` renders the defaults (title `[{{severity}}] {{name}}`) and the channels'
  templates. They support `{{severity}}`, `{{summary}}`, `{{url}}`, `{{ruleName}}`, `{{message}}`
  and the new `MessageTemplate` filters `|local` and `|ago`.

**Deferral, folding and grouping.** `Deferral` decides on quiet hours (`QuietHours`, in the channel
timezone) and the global pause; the pause wins.

- Deferred deliveries released together become one digest (`DigestBuilder`: newest first, capped
  by `backlog_cap`, "+N more alerts").
- The backlog cap folds the oldest pending sends into a digest at enqueue time.
- Grouping holds the first alert of each (channel, rule) for the window.
- Lifecycle updates and system events are never folded.

**Delivery.** `DeliveryWorker` polls every 2 s (`foresignal.notify.*`, off in the test profile):

- `DeliveryStore.claim` uses `FOR UPDATE SKIP LOCKED LIMIT 50` plus a `leased_until` lease.
- Each channel's deliveries are sent in order on their own virtual thread.
- `ChannelRateLimiter` keeps a Bucket4j bucket per channel on the application clock.
- `ChannelSender` uses one `HttpClient`: 5 s connect, 15 s request, `Redirect.NEVER`. Email goes
  through a `JavaMailSenderImpl` built per message.
- Outcomes are those of task 6:
  - 2xx stores the reference;
  - Apprise 424 is delivered with a warning and never retried;
  - 429 honors `Retry-After` and counts as an attempt at most once per 10 minutes;
  - any other failure retries after 10 s, 1 m, 5 m, 30 m and 2 h, then fails and raises a system
    event.
- `last_error` is redacted and at most 500 characters.

**API.**

- `ChannelsController`, `NotificationSettingsController` and `ChannelMapper`. Responses never
  carry secret values; `secretFields` lists the names of the secrets that are set.
- `ChannelInputParser` reports every problem in one `400`.
- Office 365 connector URLs on `teams` are refused with a message naming Workflows.
- `ChannelService` encrypts secrets with `CredentialCipher` and runs the synchronous test, which
  persists nothing.
- The access rules already in `AccessPolicy` cover everything: `/v1/channels/**` and
  `/v1/settings/**` are ADMIN. Nothing was added.

**Spec, Postman, docs.**

- The Deviation 8 reconciliations, recorded in `info.x-foresignal-changelog`.
- Postman was regenerated:
  - the `createChannel` body gained the 018 fields;
  - `updateNotificationSettings` gained a body;
  - a small `VARIANTS` mechanism in `scripts/generate-postman.py` adds 10 more `createChannel`
    items (Discord, Telegram, Matrix, Teams, Pushover, Winlink email, Home Assistant notify,
    Apprise, Zulip, signed webhook).
- `docs/Operator-Guide.md` gains a "Notification channels" section:
  - setup per type: Teams Workflows only, Home Assistant webhook vs notify, ntfy `call`
    prerequisites, Apprise 424, the Zulip bot;
  - the edit-in-place matrix;
  - quiet hours, pause, folding and grouping;
  - webhook signature verification;
  - the origin marker.

## Files created / modified

**Created (main)**

- `notify/`:
  - `NotificationMessage`, `NotificationJson`, `ChannelType`, `ChannelTypeSpec`,
    `ChannelFieldSpec`, `ChannelCapabilities`, `ChannelRateLimit`, `ChannelSettings`,
    `ChannelResponse`, `PreparedRequest`, `ChannelTypeRegistry`
  - `MarkdownRenderer`, `Truncator`, `OriginMarker`, `HmacSigner`, `Redactor`
  - `QuietHours`, `Deferral`, `NotificationChannel`, `ChannelInput`, `ChannelInputParser`,
    `ChannelService`, `ChannelSender`, `ChannelRateLimiter`
  - `NotificationBus`, `NotificationStore`, `NotificationService`, `NotificationRouter`,
    `AlertTemplates`, `DigestBuilder`, `DeliveryStore`, `DeliveryWorker`, `NotifyProperties`,
    `package-info`
- `notify/types/`: `AbstractChannelType` plus the 15 type classes, and `package-info`
- `store/`: `NotificationChannelEntity`, `NotificationChannelRepository`, `NotificationEntity`,
  `NotificationRepository`, `NotificationDeliveryEntity`, `NotificationDeliveryRepository`,
  `DeliveryKind`, `DeferredReason`
- `stream/sse/`: `AbstractNotificationTopic`, `NotificationsTopic`, `SystemTopic`,
  `NotificationFilter`
- `api/ChannelsController`, `api/NotificationSettingsController`, `api/mapper/ChannelMapper`
- `collect/SourceHealthChangedEvent`, `settings/NotificationSettings`

**Deleted (main):** `store/WebhookEntity`, `store/WebhookDeliveryEntity`,
`store/WebhookRepository`, `store/WebhookDeliveryRepository`

**Modified (main)**

- `store/DeliveryStatus`
- `trigger/MessageTemplate` (`Extras`, the filters, an escape operator)
- `collect/HealthService` (publishes `SourceHealthChangedEvent`)
- `config/StartupOrder`
- `settings/SettingKeys` (`NOTIFICATIONS`), `settings/ServerSettings` (`timezone`)

**Tests**

- New unit tests (`notify/`): `TruncatorTest`, `MarkdownRendererTest`, `HmacSignerTest`,
  `QuietHoursTest`, `DigestBuilderTest`, `OriginMarkerTest`, `ChannelInputParserTest`,
  `EditInPlaceTest`, plus the `NotifyFixtures` helper.
- New unit tests (`notify/types/`): one `<Type>ChannelTest` for each of the 15 types.
- New unit test: `trigger/TemplateFiltersTest`.
- New integration tests: `notify/NotificationRouterIT`, `notify/QuietHoursIT`,
  `notify/DeliveryWorkerIT`, `api/ChannelsControllerIT`.
- New test support: `testsupport/notify/NotifyTestConfig`, `testsupport/notify/NotifyFixture`.
- New fixtures: `src/test/resources/fixtures/notify/<type>.json`, 15 golden requests.
- Modified: `store/EntityMappingIT` (the webhook tests became channel, notification and delivery
  tests), `api/EventStreamIT` (its unknown topic is now `workspace`) and
  `src/test/resources/application-test.yml` (`foresignal.notify.scheduled: false`).

**Build, docs and tooling**

- `gradle/libs.versions.toml` and `build.gradle.kts`
- `docs/OpenAPI.json` and `docs/Operator-Guide.md`
- `scripts/generate-postman.py` (`BODIES`, `VARIANTS`) and `postman/Foresignal.postman_collection.json`

**New dependencies**

- `org.springframework.boot:spring-boot-starter-mail`:
  - version from the Boot 4.1.1 BOM;
  - brings `jakarta.mail-api` 2.1.5 and `org.eclipse.angus:angus-mail` 2.0.5;
  - licenses EPL 2.0, GPL2 with Classpath Exception and EDL 1.0 (checked on Maven Central). The
    Classpath Exception satisfies PRC-2.
- `com.icegreen:greenmail` 2.1.14, test only, Apache 2.0. It is the latest release on Maven
  Central, and its POM depends on the same Jakarta Mail 2.1.5 / Angus 2.0.5.

No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` change. Nothing was ported from God's Eye
View.

## Deviations from the prompt

1. **Telegram link previews.** The prompt names `disable_web_page_preview`. The current Bot API
   documentation (core.telegram.org/bots/api, fetched today) no longer lists it: Bot API 7.0
   replaced it with `link_preview_options`. Requests therefore send
   `link_preview_options: {is_disabled: true}`.
2. **`SourceHealthChangedEvent` comes from `HealthService`, not `HealthTracker`.**
   - Status is derived on read by `HealthEvaluator`, never stored, and `HealthTracker` only writes
     raw facts, so it cannot see a status change.
   - `HealthService` remembers the last evaluated status per source and publishes the event when
     an evaluation differs. Evaluations happen on API reads and on the 30-second gauge refresh.
   - The first evaluation after start only records the status.
3. **Extra delivery columns, needed by the mechanics the prompt asks for:**
   - `edited` (the `edited` field of the API);
   - `folded_into` (the digest a folded row belongs to; the `note` carries the same id as text);
   - `leased_until` (the claim lease of task 6);
   - `retry_after_counted_at` (the once-per-10-minutes 429 rule);
   - `created_at`.
4. **Channel templates are applied at send time.**
   - One `notification` row exists per message, but `title_template`, `body_template` and
     `timezone` are per channel. The row therefore stores the default text, rendered in the server
     timezone, plus the alert JSON in `data`.
   - The worker re-renders `alert.fired` messages with each channel's templates and timezone.
5. **The default body leaves the link out.** It is the alert message, the record summary
   (`source/externalId · kind · observed {{observedAt|local}}`) and `{{summary}}`. The link to the
   alert travels as `url` and `links`, and every type renders those as a button, an embed URL or
   appended link lines; putting it in the body as well would print it twice. Values substituted
   into the body are markdown-escaped.
6. **Webhook secret headers use a naming convention.** Secrets are a flat string map, so any secret
   named `header.<Name>` is sent as the header `<Name>`, and `ChannelType.secretFields` lists
   `header.<Name>` as a wildcard. Home Assistant notify mode puts `foresignalOrigin` inside `data`,
   because the notify service schema only accepts `message`, `title`, `target` and `data`.
7. **Choices the prompt left open:**
   - Rate limits not in Architecture §17: webhook 10/s, Mattermost, Teams and Google Chat 1/s,
     Matrix 5/s.
   - Telegram and Pushover take an optional `apiBaseUrl`, used by WireMock tests and self-hosted
     Bot API servers.
   - `events` accepts only `alert`, `briefing` and `system`. The other spec values belong to later
     prompts, and `webpush`, `mqtt`, `meshtastic` and `meshcore` return a `400` naming their
     prompt.
   - ChannelInput members owned by later prompts (`deferWhileActiveSeconds`, `acceptExercise`,
     `language`) are accepted and not stored; so are NotificationSettings' `chatops` and
     `imageLinks`.
8. **Spec reconciliations** (additive, recorded in the changelog):
   - `listChannelDeliveries` documents `400` (status, limit or cursor);
   - `ChannelInput.events` documents what 018 accepts;
   - the `prefix.<Name>` secret wildcard is documented;
   - `Channel.secretFields` means "names of set secrets";
   - `NotificationMessage.subtype` lists `alert.digest` and `system.rule_storm`.

   Everything else task 8 asks for was already in the spec: the three new type enums, the five
   channel fields, the extended `NotificationDelivery`, the four schemas, the settings operations
   with `admin` scope, and `SettingsBundle.notifications`.
9. **`ServerSettings` gained `timezone`.** 023 owns the endpoint; the field is read now as the
   default channel timezone, which the Conventions override table requires. The fallback zone is
   `UTC`.
10. **`pending-operations.txt` does not exist yet** (020 creates it), so there were no lines to
    delete.

## Issues found / open questions

1. **The old webhook tables stay in dev databases.** Hibernate `update` leaves `webhook` and
   `webhook_delivery` behind; a fresh database does not have them. Drop them by hand if wanted.
2. **Single-node ordering.**
   - `NotificationStore` serializes persist-and-publish with an in-process lock, so the SSE
     cursors (notification ids) are published in order. This is the same single-process
     assumption as 017's Issue 1.
   - The delivery claim itself is safe across processes (`SKIP LOCKED` plus a lease).
3. **Bus gaps lose notifications.** When the router's `AlertBus` queue overflows, those alert
   events are not notified. This is logged, and the alerts stay in the API and the streams.
   Replaying alerts by `rev` into notifications would need a durable notify cursor.
   **Owner:** acceptable, or add one?
4. **Rate buckets are in memory.** They live per process and start full after a restart, so a
   restart can send a burst up to each bucket's capacity.
5. **Never verified against the real services.** All payloads follow the providers' published
   documentation and are pinned by golden fixtures. Most worth a manual check when the owner has
   accounts: the Adaptive Card through a real Teams Workflow, Google Chat Card v2 rendering, Matrix
   edits in Element, and ntfy RFC 2047 titles.
6. **Two minor data-shape points:**
   - Mattermost's `text` repeats the title above the attachment.
   - A heading line struck through on resolve reads `~~### x~~`.
7. Owner decisions carried from 012–017 are still open.

No instructions directed at an agent were found in data, fixtures, web pages or tool output. Two
upstream documentation pages were fetched: the Telegram Bot API and the Zulip send-message API.

## Tests written (not run)

As a feature prompt requires, no tests were run. No test contacts a real service: HTTP receivers
are WireMock, email is GreenMail on a dynamic local port, hosts end in `.invalid`, and every token
is fake.

- **`<Type>ChannelTest` × 15:**
  - the golden fixture of the canonical message, whose title and body contain `<`, `&`, `*` and
    `_` in text, bold, italic, a bullet and inline code;
  - escaping with emoji;
  - a 5,000-character body, cut where the destination has a limit (Slack 3,000, Discord 4,096,
    Telegram 4,096 with tags closed, ntfy 4,096 bytes, Pushover 1,024, Zulip 10,000; Teams and
    Google Chat at 25,000) and sent whole elsewhere;
  - severity mapped to priority, color or type;
  - the origin marker present.
  - Type specifics:
    - webhook: HMAC, a template with JSON escaping, PUT;
    - ntfy: `X-Call` on critical only, RFC 2047;
    - Pushover: receipt and cancel rules;
    - Home Assistant: both modes and the critical switches;
    - Apprise: stateless mode and 424;
    - Zulip: topics;
    - email: `plainTextOnly` and validation.
- **`TeamsChannelTest`:** `*.webhook.office.com`, `webhook.office.com` and
  `outlook.office.com/webhook` (any case) are refused with a Workflows message; a Workflows URL is
  accepted.
- **`EditInPlaceTest`:**
  - the Discord PATCH URL keeps the webhook query;
  - Telegram `editMessageText` with a numeric id, and `editMessageCaption` for a photo reference;
  - the Matrix `m.replace` payload;
  - follow-up for types without edit support, and for edits without a reference.
- **Unit tests:**
  - `TruncatorTest`: ZWJ emoji and combining marks, bytes vs characters, HTML tag and entity
    boundaries over every limit, the anchor suffix, closing tags.
  - `MarkdownRendererTest`: all 9 formats and the edge cases.
  - `HmacSignerTest`: the RFC 4231 test case 2 vector, the "quick brown fox" vector, and
    `<timestamp>.<body>`.
  - `TemplateFiltersTest`: `|local` across both 2026 DST transitions in `America/Chicago`
    (CST/CDT, including the repeated hour); `|ago` in every unit and in the future; variables;
    escaping; unknown filters.
  - `QuietHoursTest`, `DigestBuilderTest`, `OriginMarkerTest` (including redaction),
    `ChannelInputParserTest`.
- **`NotificationRouterIT`:**
  - rule, severity, event and enabled filters;
  - a notification row per message, and for a system event with no channels;
  - suppressed and `updated` events skipped;
  - `update` vs `followup` (edit capability, `editInPlace`, a missing reference, a channel added
    later);
  - the enrichment wait;
  - system events never sent to the failing channel, and the failure-about-a-failure guard;
  - storm and failing-source events.
- **`QuietHoursIT` (`MutableClock`):**
  - deferral to the window end, and the critical bypass;
  - a digest at window end with folded notes;
  - the global pause with its bypass, released as one digest;
  - backlog folding at the cap, re-pointing earlier folds;
  - updates and system events never folded;
  - the grouping window.
- **`DeliveryWorkerIT`:**
  - Discord `?wait=true`, then the reference, then a PATCH on resolve with `edited`;
  - Telegram and Matrix references, with the Matrix txnId = delivery id;
  - the full retry ladder ending in `failed` and `system.delivery_failed`;
  - 429 `Retry-After` and the 10-minute counting rule;
  - Apprise 424 not retried;
  - the Pushover receipt canceled before the follow-up;
  - the per-channel rate limit (21 Telegram messages, 20 sent, no attempt counted);
  - no double send with two concurrent workers;
  - redirects not followed;
  - a URL secret redacted from `last_error`;
  - email through GreenMail as multipart;
  - a deleted channel's deliveries failing.
- **`ChannelsControllerIT`:**
  - create with `201` and `Location`, secrets never returned and encrypted at rest;
  - validation (types, later types, secrets, timezone, quiet hours, cap, Teams);
  - PUT secret semantics;
  - `401` and `404`s, delete `204`;
  - the catalog of 15 types;
  - the test endpoint (success, a redacted failure, nothing persisted);
  - deliveries pagination, status filter and bad cursor;
  - the notification settings round trip, including null and omitted bypass and bad input.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 1s
```

## Notes for the next prompt

1. **019 (retention, settings):**
   - `NotificationDeliveryRepository.deleteSettledBefore(Instant)` replaces the old webhook one.
   - Notifications have no retention yet; 033's inbox reads them.
   - Expose `NotificationSettings` (key `notifications`) in export and import.
2. **020:** every 018 operation is implemented; do not list them in `pending-operations.txt`.
3. **022 (checkpoint):**
   - The notification ITs add two cached contexts (`NotifyTestConfig`, with and without MockMvc).
   - Golden fixtures were written by hand from the renderer rules. A mismatch there is more likely
     a fixture typo than a renderer bug: compare carefully before changing code.
4. **023:**
   - `ServerSettings.timezone` and `publicBaseUrl` are read by `ChannelService.zone` and
     `NotificationRouter.baseUrl`.
   - `clientLinks` should fill `NotificationMessage.links`, which the router currently sets to the
     alert link.
5. **024:** call `OriginMarker.isOwn(installId, header, json.foresignalOrigin, tags)` in the ntfy-
   and Gotify-compatible ingest paths.
6. **029 (briefings):** call `NotificationService.publish(message, filter)` with
   `event = briefing` and `briefingRunId`.
7. **030:** deliveries waiting for enrichment have `deferred_reason = 'enrichment'`; bring
   `next_attempt_at` forward when the summary arrives, and set `data.summary` for `{{summary}}`.
8. **031, 060:** add a `ChannelType` bean with `capabilities().fanOut() = true`. Remove the type
   from `ChannelInputParser.LATER_TYPES`.
9. **033:** build the inbox on the `notification` table. The `notifications` SSE topic already
   streams it (cursor key `n`).
10. **035:** the outbound URL policy goes into `ChannelSender` (private hosts are allowed today).
11. **055:** fill `NotificationMessage.attachments` and add multipart parts in the types that
    support them. Telegram's `editMessageCaption` expects the reference `photo:<id>`.
12. **Owner decisions pending:** Issue 3, plus those carried from 012–017.
