# Cookbook: phone automations

iOS Shortcuts and Android automation apps (Tasker, MacroDroid) can ask Foresignal questions, show
firing alerts, and, on Android, react to Foresignal notifications as they arrive.

**Token scope:** a **read** token on the phone, and only for the iOS shortcuts below (Ask and the
alert list). The Android automations need no Foresignal token at all. Acknowledging always goes
through the one-time capability URLs Foresignal puts in its notifications (prompt 033), never through
an agent or admin token on the phone.

**URLs used:** `/v1/ask`, `/v1/alerts`, `/v1/actions/{actionToken}` (capability URLs, opened by the
ntfy app's buttons), and for setup `/v1/tokens`, `/v1/channels`, `/v1/settings/server`.

## iOS: "Ask Foresignal" (Shortcuts)

`POST /v1/ask` answers a natural-language question from Foresignal's data. It always needs a token,
even while `openReads` is on, because every question spends the operator's LLM budget; a read token
is enough. It answers `409 LLM_NOT_CONFIGURED` until a chat provider is set up.

Build the shortcut:

1. **Ask for Input** (Text): "What do you want to know?"
2. **Get Contents of URL**: `http://foresignal.local:8080/v1/ask` (use the HTTPS tailnet address
   when away from home), method **POST**, headers `Authorization: Bearer fsr_…` and
   `Content-Type: application/json`, request body **JSON** with the key `question` set to the
   provided input. Optionally add `scope` (a dictionary with `bbox` or `sources`).
3. **Get Dictionary Value** for the key `answer`.
4. **Speak Text** (or **Show Result**) with that value.

The answer is markdown with citations such as `[usgs-earthquakes:us7000abcd@18342]`; for speech,
add a **Replace Text** action with the regular expression `\[[^\]]+\]` and an empty replacement.
Answers can take tens of seconds; `warnings` lists anything the server could not verify.

## iOS: "Firing alerts"

1. **Get Contents of URL**: `http://foresignal.local:8080/v1/alerts?state=firing&limit=5`, method
   GET, header `Authorization: Bearer fsr_…`.
2. **Get Dictionary Value** `items`, then **Repeat with Each** item: **Get Dictionary Value**
   `message` (and `severity`), and add it to a list.
3. **Show Result** or **Speak Text**.

**iOS cannot start an automation from a push notification.** Personal automations have no
"notification received" trigger, so a Foresignal push can only be acted on by the person: through
the notification's own actions (Web Push or the native app, prompts 031–033), or by running a
shortcut by hand, from a widget or by voice.

## Android: Tasker or MacroDroid, driven by ntfy

The [ntfy Android app](https://docs.ntfy.sh/subscribe/phone/) broadcasts every message it receives
as an intent, which automation apps can listen to:

- **Action:** `io.heckel.ntfy.MESSAGE_RECEIVED` (MacroDroid also asks for the package name,
  `io.heckel.ntfy`).
- **Extras:** `id`, `base_url`, `topic`, `muted`, `time`, `title`, `message`, `tags`
  (`"tag1,tag2"`), `priority` (1–5), `click`, and the attachment fields.

Set up:

1. With the admin token, create an `ntfy` channel to your topic (see the Operator Guide,
   "Notification channels"). Foresignal sets ntfy's `Priority` from the severity (info 3, warning 4,
   critical 5) and its `Tags` header from the **watch's tags** plus an origin tag.
2. Give the watches you want to automate distinctive tags, for example `"tags": ["door", "home"]`
   on a watch over your own sensors, or `"tags": ["quake-near-home"]`.
3. **Tasker:** Profile → Event → System → **Intent Received**, Action
   `io.heckel.ntfy.MESSAGE_RECEIVED`. Tasker exposes the extras as local variables (`%topic`,
   `%title`, `%message`, `%tags`, `%priority`). Add the condition `%tags ~R (^|,)quake-near-home(,|$)`
   and the actions you want (flash the lights through Home Assistant, play an alarm, read
   `%title` aloud).
   **MacroDroid:** Trigger → Connectivity → **Intent Received**, action
   `io.heckel.ntfy.MESSAGE_RECEIVED`, package `io.heckel.ntfy`, and match the `tags` extra.
4. **Acknowledging:** the ntfy notification itself carries **Acknowledge** and **Snooze 1h**
   buttons (HTTP actions that POST a capability URL; prompt 033). Tasker cannot read those URLs
   from the broadcast, and it does not need to: the person taps the button. Nothing on the phone
   holds a token that could acknowledge anything.

Capability URLs point at the server's `publicBaseUrl` (`PUT /v1/settings/server`); set it to an
address the phone can reach (for example the tailnet HTTPS name), or the buttons point at
`http://localhost:8080`.

## Limits

- Ask: one question per call, up to about 120 s; 429 `LLM_BUDGET_EXCEEDED` when the budget is spent.
- Capability URLs: single use, 12 h by default (at most 24 h), withdrawn once the alert is
  acknowledged or resolved anywhere.
- ntfy intents arrive only while the ntfy app may run in the background; battery optimizations can
  delay them.

## Privacy

- A token stored in Shortcuts syncs through iCloud with the shortcut; create one token per phone
  (`POST /v1/tokens`, read scope, with an `expiresAt`) so it can be revoked alone.
- ntfy topics are readable by anyone who knows the name on a public ntfy server; use your own ntfy
  server or an access-controlled topic for anything personal.
- Notification text from external feeds is shown and spoken as is.
