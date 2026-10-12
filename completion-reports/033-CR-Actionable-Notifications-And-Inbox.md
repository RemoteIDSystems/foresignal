# 033-CR-Actionable-Notifications-And-Inbox

| | |
|---|---|
| Prompt | 033-Actionable-Notifications-And-Inbox.md |
| Status | Complete |
| Implementation commit | 3b374d5e8d0a52eb39026e716bec494af5481db2 |
| Date | 2026-10-12 |

## Summary

The operator can now act on an alert from the notification itself, every client keeps an inbox
with per-device read state and badges, phone notifications can wait while a desktop is active, and
unacknowledged alerts escalate along a policy.

The seventeen operations the spec assigns to 033 are implemented and marked `implemented`, and
their lines are gone from `pending-operations.txt`:

- open: `getActionConfirm`, `executeAction`
- read, always authenticated: `listNotifications`, `getNotification`, `markNotificationsRead`,
  `getNotificationBadge`, `streamNotifications`, `updatePresence`
- admin: `listClients`
- agent: `listEscalationPolicies`, `createEscalationPolicy`, `getEscalationPolicy`,
  `updateEscalationPolicy`, `deleteEscalationPolicy`, `muteTrigger`, `unmuteTrigger`
- read: `getAlertEscalation`

`AccessPolicy` gains `GET`/`POST /v1/actions/*` open (before the default), `/v1/notifications(/**)`
and `POST /v1/clients/presence` read always-authenticated, `GET /v1/clients` admin and
`/v1/escalation-policies(/**)` agent. `GET /v1/alerts/*/escalation` and the mute paths were already
covered by the existing alert (read) and trigger (agent) rules. All match each operation's
`x-foresignal-scope` and always-authenticated flag.

**Task 1, prerequisite hooks.** From 017, `alert.state`, `acknowledged_by`, `resolved_at`,
`snoozed_until` and the `acknowledged`, `resolved` and `snoozed` AlertBus events already existed.
From 018, the `notification` row (persisted in `NotificationService.publish` for every message),
`provider_message_ref`, `renderUpdate`, per-channel quiet hours and the Pushover receipt cancel
already existed. Added:

- `trigger_rule.muted_until`, `TriggerRuleService.mute/unmute`, `Mutes` (1 h to 7 d) and the
  `TriggerRuleMutedEvent`. `NotificationRouter.fired` persists a muted rule's alert but routes it to
  no channel.
- `trigger_rule.escalation_policy_id`, validated on every save.

**Task 2, action capabilities (`notify.action`).**

- `ActionToken`: `eac_<22>.<11>`, 38 characters. The MAC is 8 bytes of
  HMAC-SHA256(k, 16 random bytes), compared with `MessageDigest.isEqual` before any query.
  Non-canonical base64 spellings are refused. Only the hex SHA-256 is stored.
- `ActionKeyService`: the 32-byte key in `system_secret['action.hmac']`, encrypted with
  `CredentialCipher` (`nonce ‖ ciphertext`, as the VAPID key).
- `action_capability` (`ActionCapabilityEntity`): the prompt's columns plus `created_at`.
- `ActionService`:
  - `mint`: one capability per action, 12 h by default and at most 24 h. Snooze defaults to 1 h
    (15 min to 24 h) and mute to 4 h (1 h to 7 d), in `ActionDurations`.
  - `peek`: the confirm view; it never acts.
  - `execute`: one transaction with `SELECT … FOR UPDATE`. It marks the capability used first, then
    acknowledges (`acknowledged_by = action:<type>:<name>`), snoozes or mutes. An alert already
    acknowledged or resolved returns `already-<state>`. Unknown, expired and used tokens get
    `410 ACTION_EXPIRED`. Every execution, refusals included, is audited with actor kind `action`
    and the path `/v1/actions/eac_…`.
- `ActionRevoker` (an AlertBus listener) marks the open capabilities `superseded` on any
  acknowledgement or resolution.

**Task 3, endpoints.**

- `ActionsController`:
  - `GET` returns the confirm page (`ActionPages`: one POST form, no JavaScript, no external
    assets, every alert text HTML-escaped) or a 410 page.
  - `POST` returns JSON for `Accept: application/json`, otherwise an HTML page.
  - Every response carries the four required headers. The Origin check is in Deviation 2.
  - `ActionRateLimiter` (Bucket4j) allows 30 requests per minute per client IP.
  - Refusals are built in the controller, so a problem's `instance` carries the redacted path.

**Task 4, rendering.**

- `ActionMinter` mints at render time in `DeliveryWorker`. Actions go only on the first send of a
  firing `alert.fired` or `alert.escalated` notification, and on the enrichment edit, so Telegram
  keeps its buttons. `ChannelSettings` gains `actions` (the old constructor is kept).
- ntfy: `http` POST actions for the first two capabilities with `clear=true` and
  `headers.Accept=application/json`, then `view, Open`. The syntax was checked against ntfy's docs.
- Web Push: `PushMessage` gains `actions` (at most two) and `confirmUrl`, rendered by
  `PushPayloads`. Declarative payloads navigate to the confirm page and carry no actions.
- Native: the category is `FORESIGNAL_ALERT_ACTIONABLE`. `PushBadgeProvider`, through
  `InboxBadgeProvider`, sets `PushMessage.badge` per native device at send time.
- Telegram: an inline keyboard whose `callback_data` is `ea:` + the token (41 bytes). Edits send the
  current keyboard, which is empty after an acknowledgement. `TelegramCallbackPoller`:
  - one virtual thread per channel, `getUpdates` with `allowed_updates`, and the offset in
    `app_setting['telegram.<id>.offset']`;
  - a `TelegramUpdateHandler` SPI for 061. `ActionCallbackHandler` checks the presser
    (`allowedUserIds`, or the channel's chat when the list is empty), executes the capability,
    answers the callback query, and edits the message through `renderUpdate` to "✅ Acknowledged by
    &lt;first_name&gt; — …".
- Matrix: confirm-page links. `MatrixReactionPoller` long-polls `/sync` filtered to the room and
  `m.reaction`, persisting `since`. The first sync only records where to start. A ✅ from someone in
  `allowedSenders` on our event (`provider_message_ref`) acknowledges.
- Pushover: the ack confirm page is the URL. `PushoverReceiptPoller` (every 60 s, within 3 h) turns
  `acknowledged=1` into `pushover:<acknowledged_by_device>`. The format was checked against
  Pushover's receipts documentation.
- Webhook: an `actions` array in the default body.
- Other types: links appended to the body when `actions.links` is on.

**Task 5, inbox (`notify.inbox`).**

- `InboxService`:
  - listing (keyset, filters, cursor) with the live `alertState`;
  - single items;
  - read state: the cursor plus sparse reads, at most 500, oldest dropped;
  - the badge: unread system events plus unread `alert.fired` whose alert is still firing;
  - retention.
- Notifications about sources the caller may not see are hidden (Deviation 7).
- `DeviceKeys`: the token id of a token with device metadata, else `<tokenId>:<uuid>` from
  `Foresignal-Device-Id`, else `400 DEVICE_ID_REQUIRED`.
- `InboxTopic` is the 015 SSE topic `inbox`. Its `notification` events carry id = notification id
  and are replayed with read state. `badge` control events go to the device's streams through the
  new `SseClient.offerControl` and `SseHub.publishControl`.
- `RetentionSettings.inbox` defaults to 30 d. `InboxRetentionStep` (order 710) also deletes cursors
  unseen for 90 d, expired presence and old capabilities.
- `GET /v1/notifications/{id}` mints capabilities for the calling device.

**Task 6, presence.**

- `PresenceService` covers upsert, listing and `desktopActive`. Expired presence never counts.
- `NotificationService` defers a non-critical `alert.fired` by `deferWhileActiveSeconds`
  (`deferred_reason = presence`) while a desktop is active.
- `DeliveryWorker` settles a presence-deferred send as the new status `SUPPRESSED`, with a note,
  when the alert was acknowledged, resolved or snoozed, or the notification was read on a desktop.
- An open inbox or alert stream records `background` presence only.
- The channel column is `defer_while_active_seconds`, and `ChannelInput` and `NotificationChannel`
  carry it.

**Task 7, escalation (`notify.escalation`).**

- `escalation_policy` and `alert_escalation` are entities in `store`. `EscalationPolicyInput`
  validates the input, and `EscalationPolicyService` does the CRUD (`409 POLICY_IN_USE`).
- `EscalationWorker`:
  - begins an escalation on FIRED;
  - every 15 s claims due rows with `FOR UPDATE SKIP LOCKED`, advancing them in the claim
    transaction and sending after the commit;
  - sends `alert.escalated` titled `[Escalation n]` through `NotificationService.publishEscalation`
    (quiet hours apply except to critical; `DeliveryWorker` also never quiet-holds a critical
    escalation);
  - handles the repeat and then `exhausted`;
  - stops at once on ack, resolve and snooze (per `stopOn`) and on a mute: pending escalation
    deliveries are suppressed and Pushover receipts canceled.
- `notification_delivery.escalation_step` is filled and shown in `/deliveries`.

**Task 8, MCP and Ask.** The MCP tools are `list_notifications` (read), `snooze_alert` (agent),
`mute_watch` (agent) and `list_escalation_policies` (read). Ask gains the `propose_snooze` tool with
the `snooze_alert` handler.

**Task 9, spec, Postman, docs.**

- `docs/OpenAPI.json`: 17 operations implemented. `Problem` lists `ACTION_EXPIRED` (410),
  `DEVICE_ID_REQUIRED` (400) and `POLICY_IN_USE` (409). `listChannelDeliveries` takes
  `status=suppressed`. There is a changelog entry, and `lintOpenApi` passes.
- Postman, regenerated:
  - bodies for `markNotificationsRead`, `updatePresence`, `createEscalationPolicy`,
    `updateEscalationPolicy`, `muteTrigger` and `executeAction`;
  - the path variables `actionToken`, `notificationId` and `escalationPolicyId`, with captures;
  - the inbox stream in the Streams folder.
- `docs/Architecture.md` §17: the mute bullet, and the new §17.1 Acting from notifications, §17.2
  Notification inbox and §17.3 Presence deferral and escalation.
- `docs/Product-Charter.md` has a new §8.16a with FR-82 to FR-84.
- `docs/Operator-Guide.md` has a new section "Acting from notifications": the per-channel behavior
  table, Telegram bot dedication, Pushover receipts, why GET only confirms, presence deferral and
  escalation examples. The retention table gains the Inbox row.
- `docs/Client-Guide.md`: the NSE snippet now reads `/v1/notifications/{id}` and caches actions; the
  categories paragraph is updated; new sections "The notification inbox", "Presence heartbeats"
  and "Acting on notifications" (the service-worker snippet and the native action handler).
- `docs/Agent-Guide.md`: four tool rows.

Tests were written but not run (feature prompt).

## Files created / modified

**New**
- `notify/action/`: `ActionToken`, `ActionKeyService`, `ActionCapabilityEntity`, `ActionService`,
  `ActionDurations`, `ActionsConfig`, `ActionExpiredException`, `ActionPaths`, `ActionPages`,
  `ActionRateLimiter`, `ActionRevoker`, `ActionMinter`, `ActionProperties`,
  `TelegramUpdateHandler`, `TelegramCallbackPoller`, `ActionCallbackHandler`,
  `MatrixReactionPoller`, `PushoverReceiptPoller`, `package-info`
- `notify/inbox/`: `DeviceKeys`, `InboxService`, `InboxFilter`, `InboxTopic`,
  `InboxBadgeChangedEvent`, `InboxBadgeProvider`, `InboxRetentionStep`, `PresenceService`,
  `NotificationReadCursorEntity`, `NotificationReadEntity`, `ClientPresenceEntity`, `package-info`
- `notify/escalation/`: `EscalationPolicyInput`, `EscalationPolicyService`, `EscalationWorker`,
  `EscalationProperties`, `package-info`
- `notify/NotificationAction`, `notify/DeliveryStatusConstraint`, `notify/push/PushBadgeProvider`
- `store/EscalationPolicyEntity`, `store/EscalationPolicyRepository`, `store/AlertEscalationEntity`
- `trigger/Mutes`, `trigger/TriggerRuleMutedEvent`
- `api/ActionsController`, `api/NotificationsController`, `api/ClientsController`,
  `api/EscalationController`
- `mcp/tools/ListNotificationsTool`, `SnoozeAlertTool`, `MuteWatchTool`,
  `ListEscalationPoliciesTool`

**Changed**
- `notify/`:
  - `ChannelSettings` (`actions`), `ChannelInput`, `ChannelInputParser`, `ChannelService`,
    `NotificationChannel`
  - `DeliveryStore` (`suppress`, `suppressPendingEscalations`), `DeliveryWorker`, `FanOutDelivery`
  - `NotificationRouter` (mute), `NotificationService` (presence, `publishEscalation`)
- `notify/types/`: `NtfyChannel`, `TelegramChannel`, `PushoverChannel`, `WebhookChannel`
- `notify/push/`: `PushMessage`, `PushPayloads`, `PushContentFactory`, `PushFanout`,
  `PushDispatchWorker`
- `store/`: `DeliveryStatus` (`SUPPRESSED`), `DeferredReason` (`presence`), `NotificationChannelEntity`,
  `NotificationDeliveryEntity`, `NotificationDeliveryRepository`, `TriggerRuleEntity`
- `stream/sse/SseClient` (`offerControl`), `stream/sse/SseHub` (`publishControl`)
- `trigger/TriggerRuleService`
- `api/`: `AlertsController`, `ChannelsController`, `TriggersController`, `error/ErrorCodes`,
  `mapper/ChannelMapper`, `mapper/TriggerMapper`
- `ask/AlertActionHandlers`, `ask/AskActionTools`
- `security/AccessPolicy`
- `settings/RetentionSettings`, `retention/RetentionSettingsService`
- `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `scripts/generate-postman.py`
- `docs/Architecture.md`, `docs/Product-Charter.md`, `docs/Operator-Guide.md`,
  `docs/Client-Guide.md`, `docs/Agent-Guide.md` (all edited, not replaced)
- `src/test/resources/application-test.yml` (`actions.scheduled` and `escalation.scheduled` false)
- `src/test/resources/openapi/pending-operations.txt` (17 lines removed)
- Tests: `openapi/PostmanCollectionContractTest` (five validator entries; `executeAction`
  schema-only), `mcp/McpToolSchemaTest` (the four tools), `notify/ChannelInputParserTest`

No new dependencies. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` change. Nothing was
copied from AGPL/GPL references. No credential is committed.

## Deviations from the prompt

1. **`config.actions.enabled` defaults per the spec.** The prompt says "default true for the types
   below", which could be read as every type in task 4. The spec's `ChannelActionsConfig` says
   "default true for ntfy, webpush, telegram, matrix, pushover", so other types (email, Slack,
   webhook, …) opt in. This keeps capability URLs out of chat and mail logs by default. The spec
   description now states the false default.
2. **The Origin check also accepts the request's own origin**, its scheme and `Host` as Tomcat
   resolves them behind trusted proxies. Without this, a confirm page opened under a LAN IP rather
   than the `publicBaseUrl` host would refuse its own form post. A cross-site page still cannot
   match, and `Origin: null` is refused. Recorded in the spec and the changelog.
3. **There is no access log to redact.** Foresignal configures no Tomcat access log. The token is
   redacted (`ActionPaths.redact`, `/v1/actions/eac_…`) in the audit log, in problem documents'
   `instance` (refusals are built in the controller) and in the confirm and result pages, and no log
   line prints it. An operator who enables `server.tomcat.accesslog` would log raw paths. That is
   noted here, not in the guide.
4. **`notification` retention is the shorter of `inbox` and `notifications`.** 019's `OperationalTablesStep`
   already deletes `notification` rows after `notifications` (90 d), which its Javadoc and the spec
   both call "notification rows". The new `InboxRetentionStep` deletes them after `inbox` (30 d)
   once no delivery refers to them, and the listing hides older ones. The spec's `inbox`
   description calls `notifications` "delivery rows", while `notifications` itself says
   "notification rows"; the two disagree, so the 019 step and both descriptions were left unchanged.
   **Owner:** decide which window owns `notification` rows.
5. **The `SUPPRESSED` status and an existing check constraint.** Hibernate created
   `notification_delivery.status` with a check constraint listing the old enum values, and
   `ddl-auto: update` never rewrites it. `DeliveryStatusConstraint` drops
   `notification_delivery_status_check` once at start (`@Order(0)`), so existing databases accept
   `SUPPRESSED`. On a fresh database this changes nothing that matters. 144's Flyway baseline should
   own it.
6. **Telegram's "Acknowledged by &lt;first_name&gt;" edit is direct.** The callback handler edits
   the pressed message at once through `TelegramChannel.renderUpdate`. The 018 lifecycle update that
   follows then edits it again into "Acknowledged HH:mm — … Acknowledged by
   action:telegram:&lt;channel&gt;", because `acknowledged_by` must stay
   `action:<type>:<name>`. The first name is in the audit entry (`used_via telegram:<name>`).
7. **Inbox egress.** Notifications whose `data.sourceId` is a source the caller may not see (023
   `SourceVisibilityGate`) are hidden from listings, single reads and the stream replay, keeping
   EGR-1. The prompt does not say this.
8. **Escalation numbering and step 0.** Policy steps are indexed from 0 in `alert_escalation.next_step`.
   The rule's own routing is the alert's first notification, so the first policy step is
   "[Escalation 1]". Repeats keep the last step's number.
9. **Escalation messages are notifications.** Each step is an `alert.escalated` notification, so it
   is in the inbox and carries fresh capabilities. It is sent to the step's channels regardless of
   their filters. The badge counts only `alert.fired`, so an escalation does not raise it twice.
10. **Mute minimum.** `TriggerMuteInput` takes `until` or `for` from 1 h to 7 d, matching the
    capability mute range. The spec said only "max 7 days"; its description now states the minimum.
11. **The MCP `list_notifications` has no read state.** An agent is not a device; it uses the
    unmatchable device key `mcp:none` and never marks anything read.
12. **An escalation step needs a channel or `includePush`.** `EscalationStep.channelIds` is optional
    in the spec, but a step with neither would send nothing, so it is a 400.
13. **Presence `background` from streams lasts 300 s from the connect** and is not refreshed while
    the stream stays open. It never affects deferral, which needs `active`.

## Issues found / open questions

1. **Unverified at run time.** No test has run. These are first exercised at checkpoint 041:
   - the native SQL (the `ON CONFLICT … WHERE` upsert, `n.data ->> 'sourceId'`, `FOR UPDATE SKIP
     LOCKED` with `LIMIT`);
   - the Hibernate-created tables;
   - `SseClient.offerControl`;
   - MockMvc content negotiation of the action endpoints, which declare no `produces`;
   - the check-constraint drop.
2. **Never sent to real services.** The Telegram, Matrix, Pushover and ntfy formats follow their
   official documentation (Bot API `getUpdates`, `CallbackQuery`, `answerCallbackQuery`,
   `editMessageText`, the 64-byte `callback_data`; Pushover receipts; ntfy's `Actions` header);
   stand-ins only.
3. **Rate and receipt state are in memory**, per process: the action IP buckets and the Pushover
   "finished receipts" set. A restart can repoll a finished receipt once.
4. **A capability spent on a transaction that rolls back.** `execute` joins the 017 services'
   transaction, so a failure rolls the capability back too. The revoker, however, reacts to the
   AlertBus event, which `AlertStore.write` publishes before our outer transaction commits. In the
   rare case of a later rollback, other capabilities of that alert could be superseded although the
   acknowledgement did not stick.
5. **Guards already red before this prompt** (031 Issue 2, 032 Issue 4) are untouched. The
   `PostmanCollectionContractTest` validator table now covers 033's bodies.
6. **Data.** No instructions directed at an agent were found in data, docs, web pages or tool output.
   Fetched for verification: core.telegram.org/bots/api, pushover.net/api/receipts and
   docs.ntfy.sh/publish.
7. Owner decisions carried from 012–032 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `notify/action/ActionTokenTest` | • format, length 38, ≤ 64 bytes with `ea:`, uniqueness<br>• verification and determinism<br>• every single-character tamper, another key, truncation, padding, prefix, garbage refused<br>• non-canonical base64 refused<br>• the hash is the hex SHA-256<br>• a source check for `MessageDigest.isEqual`<br>• path redaction |
| `notify/action/ActionServiceIT` | • mint: labels, 12 h, hashes only, none for a non-firing alert<br>• ack and `action:ntfy:Ops`, single use<br>• two concurrent executions: one wins<br>• expiry, forged and unknown tokens<br>• snooze 1 h and mute 4 h effects<br>• already-acknowledged and already-resolved<br>• supersede after an ack through the API<br>• audit rows (200 and 410, token never stored)<br>• peek changes nothing |
| `notify/action/ActionEndpointIT` | • GET never acts, with the four headers, three times<br>• hostile names and messages escaped<br>• POST JSON versus HTML<br>• 410 page and problem without the token<br>• foreign and `null` Origin 403, configured and same origin accepted<br>• 30/min per IP, another IP unaffected, refill after a minute<br>• open without a token |
| `notify/action/ActionRenderingTest` | • the exact ntfy `Actions` header (3 entries)<br>• the Telegram keyboard (`ea:` + token, ≤ 64 bytes) and the empty keyboard on edit<br>• Matrix and Slack link appending and the `links=false` opt-out<br>• the webhook `actions` array<br>• the Pushover ack URL<br>• Web Push actions (≤ 2), `confirmUrl`, declarative navigate, `toMap`/`fromMap` round trip<br>• `ActionsConfig` defaults and filtering |
| `notify/action/TelegramCallbackPollerIT` | WireMock Bot API:<br>• a real keyboard from a routed alert<br>• an allowed press: ack, `answerCallbackQuery`, edit with an empty keyboard, offset 501, next poll sends the offset, a second press refused<br>• a disallowed presser<br>• chat-member fallback and a foreign chat<br>• other updates skipped with the offset advanced |
| `notify/action/MatrixReactionIT` | WireMock homeserver:<br>• first sync records `since` only<br>• intruder, wrong key and wrong event ignored<br>• an allowed ✅ acknowledges as `action:matrix:m`<br>• `since` persisted, the filter and bearer sent<br>• channels without reactions not polled |
| `notify/action/PushoverReceiptPollerIT` | WireMock API:<br>• receipt ack becomes `pushover:iphone` and is not polled again<br>• pending receipts repolled, expired ones stop<br>• the 3 h window |
| `notify/inbox/NotificationInboxIT` | • listing, live alert state, filters, `after`, cursor paging, limit validation<br>• `DEVICE_ID_REQUIRED`, a bad header, 401 without a token<br>• cursor and sparse reads, read never acknowledges, per-device isolation, the cursor never moves back, request validation<br>• badge math with ack<br>• the 500 sparse cap drops the oldest<br>• a device-metadata token as its own device; `getNotification` actions minted for it; 404s<br>• SSE replay with read state, live events to two devices, `badge` to the same device only (timeouts on every wait)<br>• retention<br>• presence and mute endpoints, scopes, validation |
| `notify/inbox/PresenceDeferralIT` | `MutableClock`:<br>• deferred, then sent<br>• suppressed after an ack, a snooze or a desktop read (a phone read does not count)<br>• critical and system immediate<br>• expired presence, idle or background desktops and active phones do not defer<br>• presence parsing |
| `notify/escalation/EscalationWorkerIT` | • steps at 5 and 15 min, `[Escalation n]`, `alert.escalated`, repeat ×2, then exhausted<br>• stop on ack with pending escalation deliveries suppressed<br>• resolve and snooze per `stopOn`, `resolve:false` keeps going<br>• mute stops; muted rules neither begin nor route<br>• two concurrent workers send once<br>• quiet hours defer warning, not critical<br>• CRUD: validation, unknown channels, `ruleCount`, rule references, unknown policy 400, update, 409 `POLICY_IN_USE` |
| `notify/ChannelInputParserTest` (added) | • `deferWhileActiveSeconds` stored and bounded<br>• `config.actions`, `allowedUserIds` and `allowedSenders` validated |
| `mcp/McpToolSchemaTest` (extended) | the four tools well formed, with their scopes |
| `openapi/PostmanCollectionContractTest` (extended) | validators for the five new bodies |

Test support: `testsupport/notify/ActionFixture` (rules saved directly, firing alerts inserted with
SQL).

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors.

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 827 requests, all operations
covered.

No new dependencies.

## Notes for the next prompt

1. **035 (access perimeter):**
   - `/v1/actions/*` is open and rate limited in `ActionsController`; consider moving the limit into
     the perimeter's rate limiting.
   - `ActionPaths.redact` is the redaction helper for any access log 035 adds.
   - Channel destinations polled by the new pollers (Telegram `apiBaseUrl`, Matrix `homeserverUrl`,
     Pushover `apiBaseUrl`) should get the outbound URL policy with the channels.
2. **041 (checkpoint):** the new ITs use the shared `NotifyTestConfig` context. Pollers and the
   escalation worker are off in the test profile (`foresignal.actions.scheduled`,
   `foresignal.escalation.scheduled`). See Issue 1.
3. **061 (bot commands):** add a `TelegramUpdateHandler` bean with `updateTypes()` `message`. The
   poller unions the `allowed_updates` and polls any channel some handler `wants`.
4. **144 (Flyway):** baseline `notification_delivery.status` without the old check constraint
   (Deviation 5), and the new tables `action_capability`, `notification_read_cursor`,
   `notification_read`, `client_presence`, `escalation_policy` and `alert_escalation`.
