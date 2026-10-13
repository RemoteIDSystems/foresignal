# 061-CR-Interactive-Chat-Commands

| | |
|---|---|
| Prompt | 061-Interactive-Chat-Commands.md |
| Status | Complete |
| Implementation commit | 5cdeb66280e2fe0a2a5cc50f1b416df18c0877cc |
| Date | 2026-10-13 |

## Summary

The operator can now talk back to Foresignal from the Telegram and Matrix channels used for alerts.
- **Commands:** list, inspect, acknowledge, resolve and snooze alerts; ask questions; read or run briefings;
  check status and sources.
- **Who may command:** only chat ids allowlisted on an interactive channel. Each chat is mapped to `read` or
  `agent` scope (never `admin`) and checked against the same `AccessPolicy` rules as an API token.
- `getChannelInteractiveStatus` is `implemented` and off `pending-operations.txt`.

**1. Configuration** (`chatops.InteractiveConfig`, `AllowedChat`).
- **Members:** `interactive`, `allowedChats[]`, `askRateLimitPerHour` (10), `commandRateLimit` (30 per
  10 min), `takeOverWebhook`, `discoveryMode`.
- **Validation:** `TelegramChannel.validate` and `MatrixChannel.validate` reject:
  - `admin` or any other scope that is not `read` or `agent`;
  - a Telegram group or channel (negative id) without `userIds`;
  - more than 50 chats, or a chat listed twice;
  - ids that are not Telegram numeric ids or Matrix `!room:server` / `@user:server`;
  - unknown `AllowedChat` members and malformed scalars.
- **Catalog:** the scalar members are added to the `telegram` and `matrix` field specs.
- **Kill switch:** `NotificationSettings.chatops.enabled` (default true) is now parsed, stored and returned.
  It is checked by the supervisor and again for every command.

**2. Connectors** (`ai.foresignal.chatops`).
- **SPI `ChatConnector`:** `start`, `poll`, `stop`, `reply`, `answerCallback`, `supportsButtons`, `state`,
  `describe` (an error with credentials removed).
- **`ChatopsManager` (the supervisor):**
  - one virtual thread per served channel (enabled, interactive, kill switch on);
  - backoff 1 s doubling to 5 min;
  - 401/403 stops the connector, raises `system.chatops_auth_failed` and sets health `failing` until the
    channel is saved again;
  - a blocked connector (foreign webhook) is `degraded` and retried at the longest backoff;
  - synced at startup (`StartupOrder.CHATOPS` = 38), every 30 s and on `ChannelChangedEvent`;
  - commands run on their own virtual threads.
- **Tables:** `chatops_cursor(channel_id PK, cursor, updated_at)` and
  `chatops_pending(token PK, channel_id, chat_id, user_id, action, ref, expires_at, used_at)`. Hibernate
  entities; written with SQL.
- **`TelegramConnector`:**
  - start: `getMe`, `getWebhookInfo` (degraded, or `deleteWebhook` with `takeOverWebhook`), then
    `setMyCommands` scoped to each chat with its scope's commands;
  - first start skips the backlog: `getUpdates?offset=-1`, then stores last + 1 (or 0);
  - poll: `getUpdates` with `timeout=50`, the stored offset and
    `allowed_updates=["message","callback_query"]`; the offset is stored after each update;
  - stale messages (> 2 min) are ignored; `/cmd@BotName` is normalized, and commands addressed to other bots
    are ignored;
  - replies: `sendMessage` in HTML through 018's formatter, ≤ 4,096 characters, `reply_parameters`, inline
    keyboards.
- **One Telegram poller per bot (merge with 033):**
  - 033's `TelegramCallbackPoller` now skips interactive channels.
  - The connector's dispatcher is keyed on the `callback_data` prefix: `cc_` goes to the router, and `ea:`
    (033's existing prefix, not `ca_`) goes to the same `TelegramUpdateHandler` beans the 033 poller calls.
- **`MatrixConnector`:**
  - start: `whoami`; for each room, `GET …/state/m.room.encryption` (200 means the room is ignored, health
    `degraded`, "encrypted rooms are not supported") and `joined_members` (more than 2 without `userIds`
    means the room is ignored);
  - the first `/sync` is discarded;
  - `/sync` uses the prompt's filter;
  - the bot's own events and `m.replace` edits are ignored;
  - replies are rich replies with `formatted_body` and a deterministic SHA-256 transaction id;
  - confirmations use 6-character codes (`/confirm`, `/reject`).
- **Outbound limits:** replies go through 018's `ChannelRateLimiter`, one bucket per destination chat:
  Telegram 20/min, Matrix 5/s.

**3. Principals and router.**
- **`ChatPrincipal`:** `chat:{channelId}:{chatId}[:{userId}]`, with the chat's scope and private grant. It
  becomes the same `AuthenticatedToken` / `McpCaller` / `AskCaller` a token produces (`ChatPrincipals`), with
  a stable name-derived id, so the following are decided exactly as for a token:
  - private sources;
  - Ask thread ownership;
  - Ask action ownership.

  It is authenticated, so `alwaysAuthenticated` services (Ask) accept it.
- **`CommandRouter`:**
  - unknown chats and users are ignored silently (`foresignal.chatops.rejected{channel}`, debug log);
  - `/whoami` works while `discoveryMode` is on;
  - strict parsing: ≤ 2,000 characters, non-command text ignored, an unknown command gets a short help;
  - per-chat command limit ("rate limited, retry in N s");
  - scope check through `AccessPolicy.requirementFor` on each command's mirrored REST operation;
  - every command audited (actor kind `chat`: principal, command, argument names, result, duration).
- **`ChatCommandService`** implements the table:

  | Command | Behavior |
  |---|---|
  | `/help` | The commands of the chat's scope |
  | `/status` | The 038 text glance in a code block |
  | `/alerts [n] [severity]` | Firing, unacknowledged alerts, ≤ 10; Ack/Resolve buttons for agent chats on Telegram |
  | `/alert <id>` | Rule, record title from the snapshot, kind, source, observed time, near, state, link |
  | `/ack`, `/resolve`, `/snooze` | The 017 lifecycle, with the principal name as the actor |
  | `/ask` | One thread per principal (`/ask new` starts another); per-chat Ask limit; answer cut at 3,000 characters with a thread link; citations as record and alert links; proposed actions as Confirm/Reject buttons (Telegram) or codes (Matrix) for agent chats, listed only for read chats |
  | `/brief [id\|name]` | The latest finished run (title, first 1,500 characters, HTML link); withheld when it cites a source hidden from the chat |
  | `/brief run` | 029 run-now; replies when it finishes, or "running" plus a link after 30 s, then a follow-up |
  | `/sources` | Degraded and failing visible sources with reasons |

- **`DiscoveryMode`:** the window is kept in `app_setting['chatops.<channelId>.discovery']`.
  - It opens when the channel is first seen with `discoveryMode` on, or saved again, and lasts 10 min.
  - Afterwards it turns itself off: the channel's stored `discoveryMode` is set to false, and the window is
    marked expired, so a restart does not reopen it.

**4. Spec, Postman, docs.**
- **OpenAPI:**
  - `getChannelInteractiveStatus` is `implemented`, with a fuller description;
  - `InteractiveChatConfig` and `AllowedChat` descriptions;
  - `askRateLimitPerHour` and `commandRateLimit` get maximum 100000, and `AllowedChat.name` gets
    maxLength 200;
  - `AuditEvent.actorKind` gains `chat`;
  - a changelog entry.

  `Channel.interactive`, `ChannelInteractiveStatus` and `NotificationSettings.chatops` were already in the
  spec and match.
- **Postman:** a `createChannel` variant "Telegram, interactive chat commands (061)", then regenerated.
- **Architecture:** §3 package layout (`chatops`), the §17 table rows, and the new **§17.5 Interactive chat
  commands** (SPI, supervisor, cursors, pending tokens, principals, router and command table, safety rules,
  privacy cost).
- **Operator Guide "Chat commands":** BotFather with a dedicated bot, Telegram privacy mode in groups,
  `discoveryMode` and `/whoami`, a Matrix bot account in an unencrypted room, the scope mapping, the command
  reference, limits, status and the kill switch.
- **Invariants:** SEC-4 gains `CommandRouterTest` and `AllowlistValidationTest`; EGR-1 gains
  `CommandRouterTest` and `ChatAskIT`.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/chatops/`:
  - SPI and value types: `package-info`, `ChatConnector`, `AbstractChatConnector`, `ChannelConfig`,
    `ChatRef`, `ChatReply`, `ChatButton`, `CommandSink`, `ChatSession`, `ChatOutcome`, `ChatCommand`,
    `ChatText`;
  - connectors and state: `TelegramConnector`, `MatrixConnector`, `ChatConnectorFactory`, `ConnectorState`,
    `ChatAuthException`, `ConnectorBlockedException`;
  - supervision and settings: `ChatopsManager`, `ChatopsProperties`;
  - configuration: `InteractiveConfig`, `AllowedChat`;
  - principals: `ChatPrincipal`, `ChatPrincipals`;
  - routing and commands: `CommandRouter`, `ChatCommandService`, `ChatRateLimiter`, `DiscoveryMode`;
  - persistence: `PendingTokens`, `ChatopsCursorStore`, `ChatopsCursorEntity`, `ChatopsPendingEntity`.
- Tests:
  - unit: `chatops/CommandRouterTest`, `chatops/AllowlistValidationTest`;
  - integration: `chatops/TelegramConnectorIT`, `chatops/MatrixConnectorIT`, `chatops/ChatAskIT`,
    `chatops/ChatAuditIT`, `chatops/DiscoveryModeIT`;
  - support: `testsupport/chatops/ChatopsItSupport`, `testsupport/chatops/RecordingConnector`.

**Modified**
- `notify/`:
  - `ChannelSender`: a `send(request, timeout)` overload for long polls;
  - `NotificationSettingsService`: parses and returns `chatops`;
  - `action/TelegramCallbackPoller`: skips interactive channels;
  - `types/TelegramChannel`, `types/MatrixChannel`: interactive field specs and validation.
- `settings/NotificationSettings` (the `ChatOps` member), `settings/SettingsTransferService` (`interactive` is
  output-only), `api/ChannelsController` (`getChannelInteractiveStatus`), `api/mapper/ChannelMapper`
  (`Channel.interactive` for telegram and matrix), `config/StartupOrder` (`CHATOPS`).
- `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`.
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
  `src/test/resources/openapi/pending-operations.txt`.
- `src/test/resources/application-test.yml`: `foresignal.chatops.scheduled: false`, `async-commands: false`.
- `api/ChannelsControllerIT`: the interactive status, embedding, 404, validation and the kill switch.

## Deviations from the prompt

1. **Product Charter not edited.** The §5 bullet and the new FR are the owner's to place (see Open
   questions). The Architecture section ships with the code.
2. **Merge with 033 uses its existing prefix.** 033's signed actions use `callback_data = "ea:" + token`, not
   `ca_`; the dispatcher keys on `cc_` (chat commands) and `ea:` (033). The merge is one poller per bot:
   `TelegramCallbackPoller` skips interactive channels, and `TelegramConnector` hands `ea:` presses to the same
   `TelegramUpdateHandler` beans. When `chatops.enabled` is off, the 033 poller takes the channel back.
   Matrix needs no merge: `/sync` consumers with their own `since` tokens do not steal from each other, so 033's
   `MatrixReactionPoller` keeps its own sync.
3. **There is no `CurrentPrincipal` class.** 023 built `CurrentToken` and `McpCaller`/`AskCaller`. The chat
   principal is turned into an `AuthenticatedToken` (stable UUID from the name, `principalId` null) and passed
   through those. The scope check is `AccessPolicy.requirementFor(method, path)` of the REST operation each
   command mirrors (the REST layer's own check).
4. **Alert short ids.** Alert ids are `long`, not ULIDs, so `/alerts` shows the full id instead of "the first
   8 chars".
5. **`/alerts` severity** is a minimum (`critical`, `warning` and above, `info` = all), because `AlertQuery`
   filters by `minSeverity`.
6. **Actor of Ask-executed actions.** Alert transitions from commands and buttons record the principal name
   (`chat:…`). Actions confirmed through Ask record `AskActionService`'s actor, which is `token:chat:…`.
7. **Matrix stale messages.** Messages older than 2 min are ignored on Matrix too, not only on Telegram, so a
   backlog after an outage never acknowledges anything.
8. **`ChatConnector` SPI additions:** `poll()` (the supervisor's loop step), `supportsButtons()`, `state()`
   and `describe()`.
9. **`/confirm` and `/reject`** appear as commands in the table and help, at agent scope (the prompt names
   them only for Matrix confirmations).
10. **Discovery window.** It reopens for 10 minutes whenever the channel is saved again. Auto-off rewrites the
    stored `discoveryMode` to false (directly, without a `ChannelChangedEvent`).
11. **Spec amendments** (changelog):
    - `AuditEvent.actorKind` gains `chat`;
    - maxima on the two rate limits;
    - `AllowedChat.name` maxLength 200.
12. **Long polls** use a new `ChannelSender.send(request, timeout)` (the long-poll wait + 15 s), since the
    channel request timeout is 15 s.

## Issues found / open questions

1. **Owner: Product Charter.** The prompt asks for these texts; they were not written into
   `docs/Product-Charter.md` because it is the owner's document.
   - §5 bullet, replacing the current one: "Sending email or chat messages other than notifications the
     operator configured, **and replies to commands from chat ids the operator explicitly allowlisted on an
     interactive channel**."
   - New FR, the next free number in §8.6: "**FR-x** Telegram and Matrix channels can be made interactive.
     Allowlisted chats may send commands (`/alerts`, `/alert`, `/ack`, `/resolve`, `/snooze`, `/ask`,
     `/brief`, `/status`, `/sources`, `/help`), each chat mapped to `read` or `agent` scope (never `admin`).
     Foresignal replies only to allowlisted chats, uses the Bot API or the Matrix client API (never user
     sessions), and sends summaries and links rather than raw payloads."
2. **Unverified against live platforms.** Bot API and Matrix behavior was taken from their documentation:
   `getUpdates` 409 with a webhook, `setMyCommands` chat scope, `reply_parameters`, rich replies, and 404
   `M_NOT_FOUND` for a missing `m.room.encryption` state. None of it has been tried against real Telegram or
   Synapse until 143.
3. **Per-chat reply buckets** are separate from the channel's delivery bucket. Alerts and command replies to
   the same Telegram chat can together exceed 20/min for a moment.
4. **Encrypted-room and membership checks** run at connector start, so a room that becomes encrypted later is
   only noticed after the next restart or channel save.
5. Data contained no instructions directed at the agent.

## Tests written (not run)

- `CommandRouterTest` (unit, real `AccessPolicy`):
  - the scope matrix: read chats cannot `/ack`, `/resolve`, `/snooze`, `/brief run` or `/confirm`; agent
    chats acknowledge as the principal;
  - help by scope; unknown command → help;
  - parsing, limits and usage lines; the 2,000-character cap; plain text ignored;
  - the `/alerts` query and buttons; no buttons for read chats;
  - private alerts hidden without `privateAccess`; unknown alert;
  - unknown chats and users silent and counted;
  - the command rate limit;
  - the audit record; principals never admin.
- `AllowlistValidationTest`:
  - a group without `userIds`; admin and other scopes;
  - the 50-entry cap;
  - ids, duplicates, unknown members, scalars;
  - Matrix ids; lenient reading.
- `TelegramConnectorIT` (WireMock Bot API):
  - backlog skipped on first start, with the `setMyCommands` chat scope;
  - the offset stored after each update and sent after a restart;
  - a webhook: degraded, or taken over; 409 blocks;
  - stale messages ignored;
  - `/cmd@Bot`;
  - the button flow: wrong user refused, single use, expiry after 1 h;
  - an unknown chat gets no reply and changes nothing;
  - 401 → `ChatAuthException`; the supervisor sets `failing` and stores a `system.chatops_auth_failed`
    notification.
- `MatrixConnectorIT` (WireMock homeserver):
  - `since` persisted and the first sync discarded, with the filter shape;
  - own events and edits ignored;
  - an encrypted room degraded and ignored; a crowded room without `userIds` ignored;
  - the rich-reply shape and transaction id;
  - `/confirm` codes bound to the user, case-insensitive, single-use.
- `ChatAskIT` (`FakeLlmClient`):
  - a cited answer with record links, the thread kept per chat, `/ask new`;
  - Confirm/Reject buttons only for agent chats, and a press executes the proposal;
  - Matrix codes;
  - the Ask rate limit;
  - a private record never sent to a non-local provider from a `privateAccess` chat.
- `ChatAuditIT`: every command audited with the principal name, command, argument names, result and duration;
  a non-allowlisted chat is not audited.
- `DiscoveryModeIT`: `/whoami` answers only while enabled, auto-off after 10 min (`MutableClock`), the stored
  flag turned off, and a stale config cannot reopen the window.
- `ChannelsControllerIT.interactiveStatus`:
  - the status (idle before start), `Channel.interactive`, 404 for other types, 401;
  - an invalid allowlist refused;
  - the `chatops.enabled` kill switch stored and reflected.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (only the existing deprecation note in
  `AgentWorkflowIT`).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "832 requests across 80 tag folders …
  covers all ops: True".

## Notes for the next prompt

1. **062+:** no interaction. A new chat command belongs in:
   - `ChatCommand`, with the REST operation it mirrors for the `AccessPolicy` check;
   - `CommandRouter.run`;
   - a `ChatCommandService` method;
   - the Architecture §17.5 table and the Operator Guide table.
2. **097 checkpoint:**
   - The chatops ITs share `ChatopsItSupport` (`@Import(NotifyTestConfig.class)`); `ChatAskIT` extends
     `AskItSupport` with `AskTestSources` and `@AutoConfigureMockMvc`.
   - Tests rely on `foresignal.chatops.scheduled: false` and `async-commands: false` (replies are recorded
     when `poll()` or `onMessage` returns).
   - `TelegramConnectorIT.unauthorizedStops` starts a real supervisor thread through `ChatopsManager.sync` and
     waits up to 10 s with Awaitility; it deletes the channel afterwards.
   - WireMock fails on unmatched requests: every Bot API method a start calls is stubbed by
     `stubTelegramStart`.
   - Possible fragility:
     - WireMock decoding of the `filter` query parameter in `MatrixConnectorIT.sincePersisted`;
     - `ChatAskIT.proposedActionButtons` depends on 028's `create_watch` handler running for a non-token
       caller;
     - `ChatAuditIT` assumes the alert id does not appear quoted in the audit detail.
3. **143:** use a dedicated bot and a test Matrix account to verify:
   - `getUpdates` and the webhook check; `setMyCommands` per chat; inline buttons and
     `answerCallbackQuery`;
   - privacy mode in a group;
   - the Matrix encryption and membership checks, and rich replies in Element.
4. **144 (Flyway):** new tables `chatops_cursor` (`channel_id uuid PK`, `cursor text`,
   `updated_at timestamptz`) and `chatops_pending` (`token varchar(64) PK`, `channel_id uuid`,
   `chat_id varchar(255)`, `user_id varchar(255)`, `action varchar(32)`, `ref varchar(128)`,
   `expires_at timestamptz`, `used_at timestamptz`, index on `expires_at`). The new `app_setting` keys are
   `chatops.thread.<principalId>` and `chatops.<channelId>.discovery`.
5. **Owner decisions pending:** Issue 1 (the Charter texts) above, plus those carried from 012–060.
