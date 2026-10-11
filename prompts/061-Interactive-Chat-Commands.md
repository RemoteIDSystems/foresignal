# 061 — Interactive Chat Commands

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/061-CR-Interactive-Chat-Commands.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/060-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §2 "Chat apps as clients" row, §7 "Two-way chat" row, §8 item 29 ("028j" = this prompt)
- `docs/Product-Charter.md` §5 (non-goals: "Sending email or chat messages other than notifications the operator configured"), §8.6, §8.13
- `docs/Architecture.md` §15 (scopes), §17 (channels), §19 (LLM safety), §21 (Ask)
- Completion Reports of:
  - 017: alert lifecycle (ack, resolve, snooze, `acknowledged_by`)
  - 018: `telegram` and `matrix` channel types, formatters, rate limits, `provider_message_ref`, edit-in-place
  - 023: principals, audit log, `alwaysAuthenticated`
  - 028: AskService, threads, proposed actions, confirm and reject
  - 029: briefing run and preview
  - 033: signed actions and Telegram callback handling. Find out whether 033 polls Telegram for `callback_query`, because only **one** consumer may call `getUpdates` per bot.
  - 038: glance text
  - 039: private gate and LLM egress rules
- Telegram Bot API: `getUpdates` (long poll, `offset`, `timeout`, `allowed_updates`), `getWebhookInfo`, `setMyCommands`, `sendMessage` (HTML, `reply_parameters`), inline keyboards and `callback_query`, `answerCallbackQuery`. `getUpdates` returns 409 while a webhook is set.
- Matrix Client-Server API v3: `GET /sync` (`since`, `timeout`, `filter`), `PUT /rooms/{roomId}/send/m.room.message/{txnId}`, rich replies (`m.relates_to.m.in_reply_to`), `GET /rooms/{roomId}/state/m.room.encryption`

## Objective

Let the operator talk back to Foresignal from the chat apps already used for alerts: list and acknowledge alerts, ask questions, run briefings and check health from Telegram or Matrix. Commands are accepted only from chat ids the operator allowlisted, and each chat is mapped to `read` or `agent` scope. That scope is enforced by the same service-layer checks as API tokens, and admin is never possible. This changes a Charter non-goal, so the amendment ships with the code.

## Tasks

### 1. Charter and Architecture amendments (write these texts)

- Product Charter §5: replace the bullet with:
  > Sending email or chat messages other than notifications the operator configured, **and replies to commands from chat ids the operator explicitly allowlisted on an interactive channel**.
- Add an FR (the next free number in §8.6):
  > **FR-x** Telegram and Matrix channels can be made interactive. Allowlisted chats may send commands (`/alerts`, `/alert`, `/ack`, `/resolve`, `/snooze`, `/ask`, `/brief`, `/status`, `/sources`, `/help`), each chat mapped to `read` or `agent` scope (never `admin`). Foresignal replies only to allowlisted chats, uses the Bot API or the Matrix client API (never user sessions), and sends summaries and links rather than raw payloads.
- Architecture: a new section "Interactive chat commands" covering the connector SPI, principals, cursors, the router and the safety rules below.

### 2. Configuration (extends the 018 `telegram` and `matrix` types)

- Non-secret config:
  - `interactive` (default false)
  - `allowedChats[]`: `{chatId, name, scope: read|agent, userIds?: [...], privateAccess?: false}`. Telegram chat ids are numeric strings; Matrix uses `roomId` plus user MXIDs.
  - `askRateLimitPerHour` (10), `commandRateLimit` (30 per 10 min)
  - `takeOverWebhook` (false)
  - `discoveryMode` (false; auto-off after 10 min)
- **Validation:**
  - Group chats or rooms with more than two members must list `userIds`, otherwise any member could command (Telegram ids < 0; for Matrix, check membership count at start).
  - `scope` ∈ {read, agent}.
  - At most 50 entries.
- Telegram: interactive mode needs a bot used only by this Foresignal instance.
  - At start, call `getWebhookInfo`. If a webhook URL is set and `takeOverWebhook` is false → interactive health `degraded` ("bot has a webhook; set takeOverWebhook or use a dedicated bot"). Otherwise call `deleteWebhook`.
  - Call `setMyCommands`, scoped to each allowed chat.
- Matrix: rooms must be **unencrypted**. If an `m.room.encryption` state event exists → `degraded` ("encrypted rooms are not supported"); ignore that room.
- Global kill switch: app setting `chatops.enabled` (default true).

### 3. Connectors (`ai.foresignal.chatops`)

- `ChatConnector` SPI:
  - `start(ChannelConfig, CommandSink)`, `stop()`
  - `reply(ChatRef, ChatReply)`, where a reply holds text, an optional `buttons[{label, token}]` and `replyTo`
  - `answerCallback(callbackRef, text)`
- Run one supervised virtual thread per interactive channel, on the same supervision pattern as the 010 streaming supervisor: backoff 1 s → 5 min; on 401/403, stop, raise a system event and set health `failing`. Channel views gain `interactive{connected, lastPollAt, lastError, chats}`.
- Table `chatops_cursor(channel_id PK, cursor text, updated_at)` so a restart neither re-processes nor drops updates.
- **TelegramConnector:**
  - Polls `getUpdates?timeout=50&offset={cursor}&allowed_updates=["message","callback_query"]`.
  - First start: fetch with `offset=-1` and store last+1, so the backlog is skipped.
  - Ignore messages older than 2 min (`date`).
  - Parse `/cmd` and `/cmd@BotUsername`.
  - Reply with `sendMessage` (HTML via 018's Telegram formatter, `reply_parameters.message_id`, ≤ 4,096 chars).
  - Buttons are inline keyboards whose `callback_data` is an opaque token (≤ 64 bytes) stored in `chatops_pending(token PK, channel_id, chat_id, user_id, action, ref, expires_at, used_at)`. Tokens last 1 h, are single-use, and are bound to the chat and user.
  - `callback_query` → resolve the token, run the action, `answerCallbackQuery`.
  - If 033 already consumes `callback_query` for this bot, merge the two into this single poller with one dispatcher keyed on token prefix (`ca_` for 033's signed actions, `cc_` for chat commands). Record the merge in the CR.
- **MatrixConnector:**
  - Uses `/sync` with filter `{room: {rooms: [allowed], timeline: {types: ["m.room.message"], limit: 20}, state: {lazy_load_members: true}}, presence: {types: []}, account_data: {types: []}}`.
  - First sync stores `next_batch` and discards the timeline.
  - Ignore the bot's own events and edits (`m.replace`).
  - Replies are rich replies with `formatted_body` (018's Matrix formatter) and a deterministic `txnId`.
  - Confirmations use text commands (`/confirm <code>`, `/reject <code>`) with the same pending-token table, using 6-character codes.

### 4. Principals and router

- Each allowlisted chat (and user) maps to a `ChatPrincipal(name = "chat:{channelId}:{chatId}[:{userId}]", scope, privateAccess)`. It is passed through 023's `CurrentPrincipal`, so every service call is scope-checked exactly like a token. Chat principals count as authenticated for `alwaysAuthenticated` services such as Ask.
- **Unknown chats or users are ignored silently** (no reply). They are counted in `foresignal.chatops.rejected{channel}` and audited at debug level. With `discoveryMode` on, `/whoami` from any chat replies with only its chat id and user id, so the operator can copy them.
- `CommandRouter` (strict parsing; ≤ 2,000 chars; unknown command → short help):

  | Command | Scope | Behaviour |
  |---|---|---|
  | `/help` | read | Commands available at this chat's scope |
  | `/status` | read | 038 glance `format=text` (full counts, because the principal is authenticated) |
  | `/alerts [n] [critical\|warning\|info]` | read | Open alerts (unresolved and unacknowledged), ≤ 10, newest first: short id (first 8 chars), severity, `shortTitle`, `{{near.text}}` if available, age, link. Agent chats get Ack/Resolve buttons. |
  | `/alert <id>` | read | Detail: rule, record summary (title, kind, source, observed time), state, link. No raw payload. |
  | `/ack <id>`, `/resolve <id>`, `/snooze <id> <duration>` | agent | 017 lifecycle; `acknowledged_by` = principal name |
  | `/ask <question>`, `/ask new` | read (actions need agent) | 028 AskService with one thread per chat (`/ask new` starts a new one). Uses the per-chat Ask rate limit and the LLM budget. Reply: the answer (truncated with a link to the thread), citations as links. Proposed actions become Confirm/Reject buttons for agent chats, and are listed but not executable for read chats. Private data per 039: only with `privateAccess` and the LLM egress rules. |
  | `/brief [id\|name]`, `/brief run <id\|name>` | read / agent | The latest run's title, first 1,500 chars and link; `run` triggers 029's run-now and replies when it finishes (or "running" plus a link after 30 s) |
  | `/sources` | read | Degraded and failing sources with reasons |

- **Safety:**
  - Replies never include raw payloads or secrets.
  - Message text from chat is only a command argument or an Ask question; it is never executed.
  - Every command is audited in `audit_event` with the principal, command, argument names, result and duration.
  - Per-chat command rate limit → reply "rate limited, retry in N s".
  - Outbound sends go through 018's per-destination rate limiter (Telegram 20/min per chat).
- The docs state the privacy cost: Telegram bot chats are not end-to-end encrypted, and Matrix rooms used here are unencrypted.

### 5. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - channel config docs for `interactive`, `allowedChats`, the rate limits, `takeOverWebhook` and `discoveryMode`
  - `Channel.interactive` status
  - new operation `getChannelInteractiveStatus` (`GET /v1/channels/{channelId}/interactive`, admin, `x-foresignal-scope`)
  - the app setting `chatops.enabled` in the settings schema
- Add a `BODIES` example for an interactive Telegram channel, then regenerate Postman. `AccessPolicy`: `/v1/channels/**` is already admin.
- `docs/Operator-Guide.md` "Chat commands":
  - BotFather setup with a dedicated bot; Telegram privacy mode in groups, where the bot sees only `/commands`
  - finding chat ids with `discoveryMode`
  - a Matrix bot account in an unencrypted room
  - the scope mapping and a command reference
- Architecture section; Charter texts from §1.

## Tests to write (do not run)

- `CommandRouterTest`:
  - scope matrix: read chats cannot `/ack`, `/resolve`, `/snooze` or run briefings
  - unknown command → help
  - argument parsing and limits
  - private alerts hidden without `privateAccess`
- `AllowlistValidationTest`: a group without `userIds` rejected; admin scope rejected; the 50-entry cap.
- `TelegramConnectorIT` (WireMock Bot API):
  - backlog skipped on first start
  - offset persisted across restart
  - 409 with a webhook → degraded, or takeover
  - stale messages ignored
  - `/cmd@Bot` parsing
  - callback flow: single-use, expiry, wrong user rejected
  - unknown chat gets no reply
  - 401 stops the connector and raises a system event
- `MatrixConnectorIT` (WireMock `/sync`): `since` persistence, own events ignored, encrypted room degraded, rich-reply shape, `/confirm` codes.
- `ChatAskIT` (`FakeLlmClient`): answer with citations; proposed action buttons only for agent chats; Ask rate limit; private egress rule respected.
- `ChatAuditIT`: every command audited with the principal name.
- `DiscoveryModeIT`: `/whoami` answers only while enabled; auto-off after 10 min (`MutableClock`).

## Acceptance criteria

- An allowlisted Telegram chat with agent scope can list alerts, acknowledge one with a button, and get a cited `/ask` answer.
- The same commands from a non-allowlisted chat get no reply and change nothing.
- The Charter amendment and Architecture section are committed with the code.
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
2. **Commit** all changes with the message `061: Interactive Chat Commands`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/061-CR-Interactive-Chat-Commands.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `061: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
