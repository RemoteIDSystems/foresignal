# Foresignal Operator Guide

How to run and tune a Foresignal instance through its API. Later prompts add sections for their
features. Every write below needs the admin token: `Authorization: Bearer fsr_…`.

The [README](../README.md) has the five-minute quickstart. This guide covers everything after it:
the token, sources, policies and retention, triggers and channels, streaming for frontends, and
running the host (storage, clock, binds, remote access, mDNS, backups and troubleshooting).

## First start and the admin token

`docker compose up -d` starts PostGIS and the broker. On the first start the broker generates an
admin token and prints it **once**, in a delimited block:

```sh
docker compose logs foresignal | grep -A3 "FORESIGNAL ADMIN TOKEN"
```

The token starts with `fsr_`. Only its SHA-256 hash is stored, so it cannot be shown again: put it
in a password manager. Send it as `Authorization: Bearer fsr_…`.

- **Rotate** (you still have the token): `POST /v1/system/admin-token/rotate` returns a new token
  once; the old one stops working immediately.
- **Reset** (you lost it): restart once with `FORESIGNAL_RESET_ADMIN_TOKEN=true`
  (for example `FORESIGNAL_RESET_ADMIN_TOKEN=true docker compose up -d foresignal` after adding the
  variable to the service's `environment`), read the new token from the log, then remove the
  variable again, or every restart replaces the token.

Data reads (`GET` of records, changes, tiles and alerts, and the streams) need no token while the
`security` setting's `openReads` is on, which is the default on a trusted network. Every write, the
fetch log and `/v1/system`'s full document need the token. Scoped read and agent tokens arrive in
prompt 023.

## Sources

A source is one upstream feed, such as `usgs-earthquakes`. Every source is installed **disabled**.
Enabling it is the only thing that makes Foresignal contact that upstream; reading or streaming data
never triggers a fetch.

| Call | What it does |
|---|---|
| `GET /v1/sources` | Every source, with status, tier, license and today's budget use |
| `GET /v1/sources/{id}` | Descriptor, settings and health of one source |
| `PUT /v1/sources/{id}` | Partial settings change (admin) |
| `PUT` / `DELETE /v1/sources/{id}/credentials` | Store or remove upstream keys (admin; never returned) |
| `POST /v1/sources/{id}/fetch` | Fetch once now, ingesting normally (admin; uses budget) |
| `POST /v1/sources/{id}/probe` | Diagnostic fetch that never ingests (admin; uses budget) |
| `GET /v1/sources/health`, `GET /v1/sources/{id}/health` | Health, coverage and storage cost |
| `GET /v1/sources/{id}/usage` | Requests, bytes and ingest counters over time |
| `GET /v1/sources/{id}/fetches` | Per-fetch provenance log (admin) |

Enable a source, with a slower poll and a smaller budget:

```json
PUT /v1/sources/usgs-earthquakes
{"enabled": true, "interval": "5m", "budget": {"perMinute": 2, "perDay": 500}}
```

Omitted fields are left unchanged. A source that needs credentials returns `409 CREDENTIALS_REQUIRED`
until you store them with `PUT /v1/sources/{id}/credentials`.

### Credentials and budgets

- **Credentials:** `PUT /v1/sources/{id}/credentials` with the fields the descriptor lists under
  `credentialFields` (for example `{"apiKey": "…"}`). They are encrypted with AES-256-GCM under the key
  in `${FORESIGNAL_DATA_DIR}/secret.key` and are never returned; a source only shows
  `credentialsSet: true`. `DELETE` removes them.
- **Budgets:** every source has a request budget (`budget.perMinute`, `budget.perDay`) that the
  broker never exceeds. A refused request is logged as a `REFUSED` fetch row, a manual fetch over
  budget answers `429`, and the scheduler simply waits. Scheduled polls, manual fetches and probes
  all spend the same budget. `budgetUsedToday` is in `GET /v1/sources`; the day is the UTC day.
- **Interval:** `interval` (shorthand such as `60s`, `5m`, `1h`, or ISO-8601) cannot go below the
  descriptor's minimum. An enable or interval change applies within seconds; the first poll after
  an enable is immediate.

### Health, usage and the fetch log at a glance

| Question | Call |
|---|---|
| Is it working? | `GET /v1/sources/{id}/health`: `status` (`healthy`, `degraded`, `failing`, `idle`, `disabled`), `statusReason`, `lastSuccessAt`, `lastError`, `consecutiveFailures`, `coverage` and `storage` |
| Which sources need attention? | `GET /v1/sources/health?status=failing,degraded` |
| How much is it costing upstream? | `GET /v1/sources/{id}/usage?resolution=hour` (requests, bytes, errors, refused, records seen, versions, touches) |
| What did each fetch do? | `GET /v1/sources/{id}/fetches` (below) |
| What does the upstream send right now? | `POST /v1/sources/{id}/probe` (a diagnostic fetch that never ingests) |

### History tiers

Each source's descriptor fixes its `historyTier`:

- **STANDARD** (discrete records: earthquakes, alerts, filings). Retention is per source and per
  kind; see [Retention](#retention) for the full resolution order. Set or clear a source's own
  window with `"retention": "365d"` or `"retention": null`.
- **TRACK** (moving things: aircraft, vessels). Every TRACK source shares one window,
  `retention.trackWindow` (3 days by default). A per-source `retention` is refused with
  `400 RETENTION_TIER_FIXED`; change `retention.trackWindow` instead.

`settings.effectiveRetention` shows the window in force for either tier.

**Why TRACK refuses a per-source window.** TRACK history lives in partitions shared by every TRACK
source and is removed by dropping whole partitions, never row by row: that is what keeps millions of
positions a day cheap. A partition can only be dropped when every source in it agrees, so there is
exactly one window for all of them. STANDARD history is deleted row by row and can therefore follow
each source's and each kind's own window.

### Ingest policies

The ingest policy decides which observations become stored versions. Everything else updates live
state only. The descriptor has a default (`descriptor.defaultIngestPolicy`). Your override is
`settings.ingestPolicy`, and `settings.effectiveIngestPolicy` is the one in force.

| Mode | Stores |
|---|---|
| `ALL` | Every change, plus a keyframe |
| `SAMPLED` | Significant changes, trigger edges and keyframes. Also a sample every `minInterval` when the position deviates by `minDeviationMeters` or a threshold moves (`thresholds: true`) |
| `LATEST` | Significant changes, trigger edges and keyframes only |

Override a policy, for example to sample aircraft less often:

```json
PUT /v1/sources/adsb-area
{"ingestPolicy": {"mode": "SAMPLED", "minInterval": "60s", "keyframe": "240s",
                  "minDeviationMeters": 1000, "thresholds": true, "edgeMinInterval": "30s"}}
```

Send `"ingestPolicy": null` to go back to the descriptor default. The rules (Core-Data-Model §5.1):

- `2s ≤ minInterval ≤ keyframe`, and `edgeMinInterval ≤ minInterval` (default `minInterval / 2`).
- With a record TTL: `keyframe ≤ ttl − pollInterval − 30s`.
- `"keyframe": "none"` is allowed only when the source has no TTL.
- `SAMPLED` and `LATEST` need a source that declares `significantFields`.

A policy that breaks a rule returns `400 INGEST_POLICY_INVALID`. Every violation is listed in
`errors[]` with its field, for example `ingestPolicy.keyframe`.

**Keyframes.** A keyframe is a version stored even though nothing significant changed, so history
always has a recent full picture of every entity: `keyframe` sets how often (for a source with a
TTL the default is `ttl − pollInterval − 30s`; `none` without a TTL). Separately, the **retention
keyframe** stores a fresh version of any entity still being observed once a quarter of its retention
window has passed since its last stored version, so retention can never delete the only version of
something that is still current. It adds well under 1% on STANDARD sources.

**What is not stored still counts.** An observation that is not persisted still updates live state
(`record_live`) and the `live` and `touch` stream events. An identical re-observation is a *touch*:
it moves `observedAt` and `lastReceivedAt` without a version, at most once per `touchMin` (one
hour without a TTL, else a tenth of the TTL between 30 s and 1 h). The fetch row's `touches`,
`versionsWritten` and per-reason counts show exactly what each fetch did.

**Defaults** (Core-Data-Model §5.3): aircraft, vessels and transit are `SAMPLED` on the TRACK tier
with family-specific intervals, deviations and thresholds; everything else is `ALL` on STANDARD.

### The fetch log

`GET /v1/sources/{id}/fetches` lists one row per fetch, stream flush or push batch, newest first.
Each row has its trigger and status, HTTP status, redacted URL, ETag, body hash, license, per-reason
counts and the seq range it wrote. Request and response bodies are never kept.

- Page with `limit` (up to 1000) and pass each page's `nextAfter` as `after`.
- Filter with `status` (`OK`, `NOT_MODIFIED`, `FAILED`, `REFUSED`, `DISCARDED`) and `trigger`
  (`SCHEDULED`, `MANUAL`, `STREAM`, `PUSH`).
- A row's `id` is the `batchId` on the versions it wrote.

### Reading `storage` and `budgetHoursRemaining`

Source health includes `storage`, which shows what the source costs:

- `observationsPerMinute`, `versionsPerMinute`: what arrives and what is stored, over the last hour.
- `persistRatio`: versions ÷ observations. A low ratio on a busy feed means the policy is doing its
  job. If it is near 1 on a position feed, consider `SAMPLED`.
- `triggerEdgeShare`: share of versions forced by trigger edges. Above 0.2, a trigger rule is
  probably flapping at a threshold; raise `edgeMinInterval` or adjust the rule.
- `bytesPerDay` (7-day average) and `projectedBytes` (`bytesPerDay` × retention window): the disk
  this source will hold at steady state.
- `lateLastHour`, `clampedLastHour`: observations that arrived out of order, and ones with
  timestamps in the future.

`budgetHoursRemaining` is how many hours today's request budget lasts at the last hour's rate. It is
`null` when nothing was requested in the last hour. If it is low early in the UTC day, lengthen the
`interval` or raise `budget.perDay`.

## Triggers and alerts

A trigger rule watches the change stream and opens an alert when a stored version matches. Rules live
under `/v1/triggers`; `POST /v1/triggers/{id}/test` backtests a rule against stored history without
alerting.

**A magnitude threshold** that opens one alert when a quake reaches M 6 and closes it on its own when a
revision takes it below:

```json
POST /v1/triggers
{"name": "Big quake", "sources": ["usgs-earthquakes"], "mode": "enter", "autoResolve": true,
 "conditions": [{"path": "mag", "op": ">=", "value": 6}],
 "messageTemplate": "M{{payload.mag}} {{payload.place}}", "severity": "warning"}
```

**A geofence** (GeoJSON Polygon or MultiPolygon in WGS 84; a LineString with `bufferMeters` becomes
a corridor) that alerts when a vessel enters a harbor and again when it leaves:

```json
POST /v1/triggers
{"name": "Harbor entry", "sources": ["aisstream"], "mode": "enter", "autoResolve": true,
 "geofence": {"type": "Polygon", "coordinates": [[[-97.40, 27.80], [-97.30, 27.80],
              [-97.30, 27.86], [-97.40, 27.86], [-97.40, 27.80]]]},
 "hysteresisMeters": 200, "cooldown": "30m"}
```

- **Conditions** are ANDed; operators include `>`, `>=`, `<`, `<=`, `==`, `!=`, `in`, `notIn`,
  `exists`, `matches` (a safe regex subset) and length tests. Paths are payload fields; volatile
  fields (such as USGS `updated`) are refused, because they never cause a version.
- **Mode:** `match` alerts on every matching version; `enter` when a version matches and the
  previous one did not (a new entity counts as entering); `exit` the reverse. With
  `autoResolve: true` an `enter` alert resolves on the exit edge, on a tombstone, or when the
  entity's live row expires. `hysteresisMeters` keeps a position jittering on a fence edge from
  flapping.
- **Cooldown** (`cooldown`, per entity or per rule with `cooldownScope: "rule"`) is measured on the
  observation time, so a burst of late data cannot fire a rule twice. `maxAlertsPerHour` caps a
  storm (default 120); `activeWindow` limits a rule to certain hours.
- **Templates:** `{{payload.<field>}}`, `{{title}}`, `{{source}}`, `{{externalId}}`,
  `{{observedAt}}` and the other placeholders the spec lists.

**The alert lifecycle.** An alert is `firing`, then optionally `acknowledged`, then `resolved`:

| Call | Effect |
|---|---|
| `GET /v1/alerts?state=firing` | List, newest first (filters for rule, source, kind, severity, tag) |
| `POST /v1/alerts/{id}/ack` | Acknowledge (optional `note` and `feedback`) |
| `POST /v1/alerts/{id}/resolve` | Resolve by hand |
| `POST /v1/alerts/{id}/snooze` `{"for": "8h"}` | Suppress repeats of this alert for a while |
| `PUT /v1/alerts/{id}/feedback` | `useful`, `noise`, `late` or `false_positive`, for tuning |
| `POST /v1/triggers/{id}/snooze` | Snooze the whole rule |

`GET /v1/alerts/stream` streams `alert` (fired), `alert.updated` and `alert.resolved` events, each
with the alert's `rev` as its id, so a client resumes with `Last-Event-ID`. Every lifecycle change
also goes to the notification channels below.

## Notification channels

A channel delivers alerts, briefings and system events to one destination, formatted natively for
it. Manage channels under `/v1/channels` (admin only). `GET /v1/channels/types` lists every type
with its config and secret fields. Secret fields (`secrets`) are write-only: they are encrypted at
rest, never returned (a channel lists only the names of its set secrets in `secretFields`), and a
`PUT` that omits `secrets` keeps the stored ones. `POST /v1/channels/{id}/test` sends a test message
at once and returns the destination's answer without storing anything.

Every message is stored as a notification before it is delivered, even when no channel takes it.
Deliveries are durable: a failure retries after 10 s, 1 min, 5 min, 30 min and 2 h, then fails, and
the failure is announced as a `system` event to your *other* channels. `429` answers honor
`Retry-After`. Each channel has a rate limit sized to its destination. Look at
`GET /v1/channels/{id}/deliveries` to see what happened (`kind` is `send`, `update`, `followup` or
`digest`; `deferredReason` says why one is waiting).

Common fields: `events` (`alert`, `briefing`, `system`; default `["alert"]`), `ruleIds` (null for
every rule), `minSeverity`, `titleTemplate` and `bodyTemplate` (the trigger placeholders plus
`{{severity}}`, `{{summary}}`, `{{url}}`, `{{ruleName}}` and `{{message}}`; `{{observedAt|local}}`
prints `2026-05-01 07:00 CDT` in the channel timezone and `{{observedAt|ago}}` prints `3 min ago`),
`waitForEnrichmentSeconds`, `timezone`, `quietHours`, `groupWindowSeconds`, `backlogCap` and
`editInPlace`. Links in messages use the `server` setting's `publicBaseUrl` (default
`http://localhost:8080`); set it to the address your phone can reach.

### Setting up each type

| Type | Config | Secrets | Notes |
|---|---|---|---|
| `webhook` | `url`, `method` (POST/PUT), `headers`, `bodyTemplate` | `hmacSecret`, `header.<Name>` | See "Verifying webhook signatures" |
| `slack` | `username`, `iconEmoji` | `webhookUrl` | An incoming webhook from a Slack app |
| `mattermost` | `channel`, `username`, `iconUrl` | `webhookUrl` | An incoming webhook |
| `discord` | `username` | `webhookUrl` | Channel settings → Integrations → Webhooks |
| `teams` | — | `workflowUrl` | **Workflows URLs only** (below) |
| `google-chat` | — | `webhookUrl` | Space → Apps & integrations → Webhooks |
| `telegram` | `chatId` | `botToken` | Create a bot with @BotFather and add it to the chat |
| `matrix` | `homeserverUrl`, `roomId` | `accessToken` | A bot account invited to the room (unencrypted room) |
| `ntfy` | `serverUrl`, `topic`, `call` | `accessToken` | `call` is below |
| `gotify` | `serverUrl` | `appToken` | An application token |
| `pushover` | `device`, `sound` | `appToken`, `userKey` | Critical is an emergency alert, canceled on resolve |
| `email` | `smtpHost`, `smtpPort`, `tls`, `from`, `to`, `plainTextOnly` | `username`, `password` | `plainTextOnly` for Winlink gateways |
| `homeassistant` | `baseUrl`, `mode`, `service`, `iosCritical`, `androidAlarmStream` | `webhookId` or `accessToken` | Below |
| `apprise` | `serverUrl`, `configKey`, `tag` | `urls` | Below |
| `zulip` | `site`, `stream`, `botEmail`, `topicTemplate` | `apiKey` | Below |

- **Microsoft Teams:** Office 365 connector webhooks were retired in May 2026, so a
  `*.webhook.office.com` or `outlook.office.com/webhook` URL is refused. In Teams, open the channel's
  *Workflows*, choose "Post to a channel when a webhook request is received", and use the URL it
  gives you.
- **Home Assistant:** `mode: webhook` (default) posts the full Foresignal JSON to
  `/api/webhook/{webhookId}` so an automation can react to it. `mode: notify` calls
  `notify.{service}` (for example `mobile_app_pixel`) with a long-lived access token; critical
  alerts then use the iOS critical sound and the Android alarm stream, which break through
  do-not-disturb. Turn either off with `iosCritical: false` or `androidAlarmStream: false`.
- **ntfy `call`:** set `call` to a phone number (or `yes` for the first verified number) to get a
  voice call for **critical** alerts only. It needs an ntfy account with a verified phone number
  and works only with a server that offers calls (ntfy.sh with a paid plan).
- **Apprise:** point `serverUrl` at an apprise-api server and either name a saved configuration
  (`configKey`) or pass Apprise URLs as the secret `urls`. When some of the configuration's targets
  fail and others succeed, apprise-api answers `424`; Foresignal records the delivery as delivered
  with a warning and **does not retry**, because a retry would notify the successful targets again.
- **Zulip:** create a generic bot (Settings → Bots), subscribe it to the stream, and use its email
  as `botEmail` and its API key as the secret `apiKey`. Each rule posts to its own topic
  (`topicTemplate`, default `{{ruleName}}`), and later updates of an alert land in the same topic.

### Edit in place

When an alert is acknowledged, snoozed or resolved, the original message is edited where the
destination allows it, and a follow-up message is sent everywhere else:

| Edited in place | Follow-up message |
|---|---|
| Discord, Telegram, Matrix | Webhook, Slack, Mattermost, Teams, Google Chat, ntfy, Gotify, Pushover, email, Home Assistant, Apprise, Zulip |

The edited text shows the new state, for example "✅ Resolved 14:02 CDT — …" with the old text struck
through. Set `editInPlace: false` on a channel to always get a follow-up instead. Pushover emergency
alerts are canceled on acknowledge or resolve, so the phone stops repeating them.

### Quiet hours, pause, folding and grouping

- **Quiet hours** (per channel): `{"start": "22:00", "end": "07:00", "days": ["MON", …],
  "bypassSeverity": "critical"}` in the channel `timezone` (else the `server` setting's timezone,
  else UTC). `days` are the days a window starts on; leave it out for every day. Messages inside the
  window wait until it ends; if more than one alert waited, they arrive as one digest, newest first.
  Messages at or above `bypassSeverity` are sent anyway (`null`: nothing bypasses).
- **Global pause:** `PUT /v1/settings/notifications` with `{"pausedUntil": "<time>",
  "pauseBypassSeverity": "critical"}` defers everything else on every channel until then, and each
  channel gets one digest when the pause ends. Set `pausedUntil` to `null` to end it early.
- **Backlog folding:** when more than `backlogCap` (default 20) alert messages wait for one channel,
  the oldest are folded into one digest ("+N more alerts"), so a storm cannot flood a phone.
- **Grouping** (opt-in): with `groupWindowSeconds` above 0, the first alert of a rule waits for the
  window and everything the rule fires meanwhile is sent with it as one message.

Lifecycle updates and system events are never folded into a digest.

### Verifying webhook signatures

With `hmacSecret` set, every `webhook` request carries `X-Foresignal-Timestamp` (Unix seconds) and
`X-Foresignal-Signature: sha256=<hex>`, where the hex is HMAC-SHA256 with your secret over the
timestamp, a dot and the raw request body. To verify: recompute it over the bytes you received,
compare in constant time, and reject timestamps more than a few minutes from your clock.
`X-Foresignal-Delivery` is the delivery id (the same on every retry, so you can drop duplicates).

### The origin marker

Every outbound request carries this instance's id in `X-Foresignal-Origin`, and the JSON bodies
Foresignal defines (webhook, Home Assistant, Apprise) carry it as `foresignalOrigin`; ntfy messages
carry the tag `foresignal-origin-<id>` and Gotify messages the extra `foresignal::origin`. When you
loop a channel back into Foresignal (for example ntfy into an ingest endpoint), the ntfy- and Gotify-compatible ingest endpoints (prompt 024) drop
messages carrying its own marker, so a notification can never feed itself.

## Streaming for frontends, tiles and asOf

**Snapshot plus stream.** A frontend that mirrors current state:

1. `GET /v1/records?source=…&order=id` returns the first page and `watermarkSeq`. Page on with
   `cursor`; the cursor pins the snapshot at that seq, so later changes are left to the stream.
2. Open `GET /v1/changes/stream?source=…&after=<watermarkSeq>` (SSE). Every persisted `change` has
   its seq as the event id, so a reconnect with `Last-Event-ID` resumes exactly where it stopped.
3. Apply an event only if its `observedAt` is not older than what you hold.

Stream parameters: `source`, `kind`, `bbox`, `live` (default true: non-persisted position updates as
`live` events, conflated to one per entity per second), `touches` (default false: `touch` events for
re-observations) and `payload` (include the payload in `change`). `/v1/events/stream` multiplexes
changes, alerts and other topics on one connection, and its filter can be changed in place with
`PATCH /v1/events/stream/{streamId}/filter`.

**When to start over.**

- `reset` (`buffer_overflow`, `replay_cap`, or a resume below the retained floor) ends the stream:
  reconnect with the `resumeAfter` it carries, or re-snapshot when it says so.
- `resync` (`gap`, `live_cap`, `live_dropped`) keeps the stream open but means live positions were
  lost: re-read current state for the affected sources.
- `GET /v1/changes?after=<seq>` answers `410 RESYNC_REQUIRED` when retention purged records after
  that seq; take a new snapshot. Routine version deletes never cause it.
- A `heartbeat` arrives every 15 s; a proxy must not buffer the stream (see the Caddy example).

**Tiles.** `GET /v1/tiles/{kind}/{z}/{x}/{y}.mvt` serves Mapbox Vector Tiles of current state for one
kind (for example `/v1/tiles/earthquake/3/1/3.mvt`), with `agg=grid` or `agg=hex` for aggregates.
`GET /v1/tiles.json` and `/v1/tiles/{kind}.json` are TileJSON for MapLibre.

**asOf.** `GET /v1/records?…&asOf=2026-10-11T12:00:00Z` returns what Foresignal *knew* at that time
(transaction time): each entity at the version stored then, even if it was revised later. The
response echoes `asOf` and reports `horizon`, the earliest time the involved sources' retention still
supports; earlier times are `400 ASOF_BEYOND_RETENTION`. `asOfSeq` pins the same thing by seq, and
`GET /v1/records/{source}/{id}/versions` lists every stored version.

## Retention

`GET /v1/settings/retention` shows every window; `PUT` replaces the whole object (every window at
least `1h`); `POST /v1/settings/retention/run` runs the job now and returns what it deleted. The job
also runs hourly, starting 5 minutes after boot.

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "provenance": "30d", "alerts": "90d", "usage": "30d",
 "notifications": "90d", "kinds": {"earthquake": "3650d"}, "sources": {"usgs-earthquakes": "1825d"}}
```

**Which window applies.** A TRACK source always uses `trackWindow`. A STANDARD record's window is the
first of:

1. `kinds.{kind}` (yours);
2. `sources.{id}` (yours; the same value as `PUT /v1/sources/{id}` `retention`, stored once);
3. the descriptor's per-kind default (`kindRetention`);
4. the descriptor's `defaultRetention` (adapters set it by analytic class: events 1825 days, series
   3650 days, bulk events 90 days);
5. `default` (7 days).

A `sources` entry for a TRACK source, or a `kinds` entry for a kind only TRACK sources emit, is
`400 RETENTION_TIER_FIXED`; unknown sources and kinds are listed in one `400`.

**What the job removes, in order.**

| Step | What |
|---|---|
| TRACK | Whole track partitions (`rv_t_*`) once they are older than `trackWindow`; never row by row |
| STANDARD | Versions older than their window, in transactions of 10,000 rows |
| Partitions | Standard partitions that are older than the largest window and already empty |
| Heads | Records whose every version is gone and that nobody has observed for a window, purged by the writer |
| Provenance | `fetch_log` rows that wrote nothing after 7 days, the others after `provenance` |
| `seq_clock` | Minutes older than the largest window plus a day |
| Other | Resolved or suppressed alerts after `alerts` (open alerts stay), usage minutes after `usage`, delivered or failed deliveries after 7 days, notifications after `notifications` |

**Current state is kept while it is current** (FR-17 as amended). Versions follow the window, but a
record that is still being observed keeps a version newer than a quarter of its window, so it never
disappears. A record purged after going quiet that comes back starts again at version 1.

**Durable consumers hold retention back.** The trigger engine's cursor (and later the embedding and
intelligence cursors) clamp every cutoff, so nothing is deleted before it was evaluated. If a cursor
falls more than two windows behind, retention stops waiting for it, counts
`foresignal.retention.consumer_lag` and sends a `system` notification; `/v1/system` then shows
`RETENTION_CONSUMER_LAG`.

**Clients.** A change-feed cursor below a purge gets `410 RESYNC_REQUIRED` (re-snapshot, then resume);
an SSE stream resumed below the retained floor gets `reset`. Routine version deletes never force a
resync.

## Operating the instance

### Probes and the compose healthcheck

| Endpoint | Includes | Use |
|---|---|---|
| `/actuator/health/liveness` | the application state only, never the database | restart decisions |
| `/actuator/health/readiness` | readiness, database, ingest writer (lock held, state loaded, clock in step), partition headroom | traffic and the compose healthcheck |
| `/actuator/health/catchup` | the catch-up signal (trigger lag over 10,000 seq, or the ingest queue above 80% for a minute) | dashboards; deliberately not part of readiness |

Details are never shown; `GET /v1/system` has them. The compose file checks readiness with
`curl -fsS http://localhost:8080/actuator/health/readiness` every 15 s after a 120 s start period.

### Watchdog and canary

A watchdog proves Foresignal is alive from somewhere else. Create a push check in Healthchecks.io
(or a push monitor in Uptime Kuma), then:

```json
PUT /v1/settings/watchdog
{"heartbeatUrl": "https://hc-ping.com/<uuid>", "failUrl": "https://hc-ping.com/<uuid>/fail",
 "interval": "60s", "method": "GET", "canary": {"enabled": true, "interval": "1h"}}
```

Every `interval` (at least 30 s) Foresignal pings `heartbeatUrl` while readiness is UP, and
`failUrl` (when set) while it is not. If the host dies the pings stop and the monitor alerts. Pings
time out after 5 s, never follow redirects, and their URLs are redacted in logs.

The opt-in **canary** submits one synthetic record to the built-in `system-canary` source every
`canary.interval` and requires its change on the change stream within 30 s
(`foresignal.canary.latency`). A failure sends a `system` notification and switches the watchdog to
`failUrl` until the canary passes again. `system-canary` keeps one day of history and never appears
in tiles.

### `/v1/system`

Anonymous callers get `version`, `apiVersion`, `startedAt`, `uptimeSeconds`, `status`
(`ok`, `degraded` or `down`), `features` and `limits`. Any valid token adds the database, records,
sources, scheduler, writer, streams, notifications, history per tier, catch-up and `warnings`:

| Warning | Meaning |
|---|---|
| `OPEN_READS_EXPOSED` | Data reads need no token and `publicBaseUrl` or the published bind is not local (loopback, RFC 1918, 100.64/10 tailnets, link-local, unique-local or `.local`) |
| `OPEN_READS_ALL_INTERFACES` (info) | Data reads need no token and the port is published on `0.0.0.0` |
| `HISTORY_RATE_HIGH` | More than 20,000 versions per minute |
| `PARTITION_HEADROOM_LOW` | A tier has less than one partition block attached ahead |
| `RETENTION_CONSUMER_LAG` | The last retention run stopped waiting for a lagging consumer |

Compose passes `FORESIGNAL_BIND` into the container so the warning knows the published bind.

### Logs

`FORESIGNAL_LOG_FORMAT` selects the console format: `ecs` (the default in the container), `gelf`,
`logstash`, or `text` (the default elsewhere). Lines logged while a request is served carry its
`traceId` and `spanId` from Micrometer Tracing (traces are never exported); an unexpected error's
`correlationId`, which the caller sees in the `500` body, is logged with its stack trace.

### Settings export and import

`GET /v1/settings/export` returns every setting (sources without credentials, retention, CORS,
notifications, the watchdog, trigger rules with their ids, channels without secrets).
`PUT /v1/settings/import` applies such a bundle atomically: unknown sources are listed, a source
that needs credentials you have not stored is imported disabled with a warning, rules and channels
are upserted by id (channels keep their stored secrets), and any error rejects the whole bundle.
`POST /v1/system/admin-token/rotate` returns a new admin token once; the old one stops working at
once.

## Running the host

### Binds

Compose publishes the broker on `127.0.0.1:8080` and PostgreSQL on `127.0.0.1:5433`, so a fresh
install is reachable only from the host itself.

- **LAN access:** `FORESIGNAL_BIND=0.0.0.0 docker compose up -d`. Compose passes the bind into the
  container, and `GET /v1/system` then warns `OPEN_READS_ALL_INTERFACES` (and `OPEN_READS_EXPOSED`
  when the address is not private) while `openReads` is on. Read those warnings: open reads mean
  anyone who can reach the port can read every collected record.
- **The database** has its own `FORESIGNAL_DB_BIND`. Leave it on `127.0.0.1`; nothing outside the
  host needs PostgreSQL, and the compose password is a default.

### Remote access and TLS

**Recommended: Tailscale Serve.** It gives the broker a real HTTPS certificate on your tailnet name
(`https://foresignal.<tailnet>.ts.net`) without opening any port on your router.

- **Compose profile:** create an auth key in the Tailscale admin console, then
  `TS_AUTHKEY=tskey-auth-… docker compose --profile tailscale up -d`. The `tailscale` service (pinned
  `tailscale/tailscale:v1.102.5`) joins the tailnet as `foresignal`, keeps its identity in the
  `tailscale-state` volume, and loads `docker/tailscale/serve.json`, which proxies HTTPS 443 to
  `http://foresignal:8080` over the compose network. Funnel (public internet) is off. The broker's
  own port stays on `127.0.0.1`. Enable HTTPS certificates for your tailnet in the admin console
  first.
- **Or on the host:** with Tailscale installed on the host, `tailscale serve --bg 8080` does the same
  for the localhost port.

**Alternative: Caddy.** `docker/caddy/Caddyfile.example` reverse-proxies with automatic HTTPS (a
public name pointing at the host, ports 80 and 443 open) or `tls internal` for a LAN-only name. It
sets `flush_interval -1`, which SSE needs. Forwarded headers are trusted only once prompt 023's
trusted-proxy setting names the proxy. Prompt 050 adds a Caddy compose profile.

Whichever you use:

- set the `server` setting's `publicBaseUrl` to the HTTPS name, so notification links work from a
  phone (`PUT /v1/settings/server` arrives in prompt 023);
- **never expose port 5433**;
- plain HTTP port forwarding from the internet is **unsupported**: the token would cross the network
  in clear text.

### mDNS

`docker/avahi/foresignal.service` lets LAN clients discover the broker. On a host running Avahi:

```sh
sudo cp docker/avahi/foresignal.service /etc/avahi/services/
```

It advertises `_http._tcp` and `_foresignal._tcp` on port 8080 with exactly two TXT records,
`path=/v1` and `api=v1`. It deliberately carries **no install id or other stable identifier**:
anyone on the network can read mDNS, and a stable value would let the device be recognized and
tracked wherever it goes. Clients ask `GET /v1/system` once connected. mDNS only helps when the broker
is bound to the LAN (`FORESIGNAL_BIND=0.0.0.0`).

### Clock and NTP

Every version's `ingested_at` comes from the host clock, and asOf, retention and the change cursor
depend on it never going backwards. **Sync time before Foresignal starts:** use chrony or
systemd-timesyncd and check with `timedatectl` (`System clock synchronized: yes`).

- If the clock steps **backwards** past the tolerance, the writer **holds ingest** (readiness goes down and
  `GET /v1/system` `ingestWriter` says why) until the clock catches up with the last stored time; small steps are absorbed.
- A **forward** jump is only counted and logged; it never blocks, but versions written during it carry
  the wrong time.

### Storage projection

At full load (every planned source, Core-Data-Model §5.3 budgets), expect about **10–15 GB/day of
history writes plus 15–20 GB/day of WAL**. The TRACK window (3 days) holds about **40 million rows,
roughly 20 GB including indexes**. Size the disk for the STANDARD windows on top (1825 days for events).

- `GET /v1/system` (with the token) reports `history` per tier: partitions, rows, bytes, headroom,
  window, plus `versionsPerMinute` and `projectedBytes`; above 20,000 versions per minute it warns
  `HISTORY_RATE_HIGH`.
- Each source's health has `storage.projectedBytes` (its 7-day average bytes per day times its
  window) and `persistRatio`.

The compose database runs with the flags Core-Data-Model §16 sets for sustained writes:
`shared_buffers=512MB`, `max_wal_size=8GB`, `checkpoint_timeout=15min`, `wal_compression=lz4`,
`max_locks_per_transaction=256` (many partitions) and `autovacuum_vacuum_cost_limit=2000`. A
standalone PostgreSQL should use the same. The broker also sets `reWriteBatchedInserts=true` on its
JDBC connection.

### Backups

A complete backup is **two things**: a database dump and the secret key file.

```sh
docker compose exec -T db pg_dump -U foresignal -Fc foresignal > foresignal-$(date +%F).dump
docker compose cp foresignal:/data/secret.key ./secret.key   # FORESIGNAL_DATA_DIR in the container
```

- `pg_dump -Fc` dumps and restores the partitioned history like any other table.
- **Source credentials and channel secrets cannot be decrypted without `secret.key`.** A dump without
  it restores everything except those secrets, which you would have to enter again. Keep the key with
  the dump, but not next to it on the same disk, and treat it like a password.
- The admin token's hash is in the database, so a restored instance accepts the token of the
  instance it came from.

**Restore** (stop Foresignal first, so the writer is not running):

```sh
docker compose stop foresignal
docker compose exec -T db dropdb -U foresignal foresignal
docker compose exec -T db createdb -U foresignal foresignal
docker compose exec -T db pg_restore -U foresignal -d foresignal --no-owner < foresignal-2026-10-11.dump
docker compose cp ./secret.key foresignal:/data/secret.key
docker compose start foresignal
```

**Drill it.** Restore into a scratch database now and then and check it, without touching the live
one:

```sh
docker compose exec -T db createdb -U foresignal restore_drill
docker compose exec -T db pg_restore -U foresignal -d restore_drill --no-owner < foresignal-2026-10-11.dump
docker compose exec -T db psql -U foresignal -d restore_drill -c "select count(*) from record_head"
docker compose exec -T db dropdb -U foresignal restore_drill
```

### Troubleshooting

| Symptom | Look at |
|---|---|
| A source shows no data | `GET /v1/sources/{id}/health` (`status`, `statusReason`, `lastError`), then `GET /v1/sources/{id}/fetches?limit=5` (HTTP status, error, `versionsWritten`), then `POST /v1/sources/{id}/probe` to see the raw upstream answer |
| Many sources unhappy at once | `GET /v1/sources/health?status=failing,degraded` |
| Fetches are `REFUSED` | The budget is spent: raise `budget` or lengthen `interval` |
| The container restarts or is unhealthy | `/actuator/health/readiness` (database, ingest writer, partitions; details are in `GET /v1/system`); `docker compose logs foresignal` |
| `GET /v1/system` `ingestWriter` says the clock is behind | The host clock went backwards: fix NTP (above) |
| Alerts or streams lag | `/actuator/health/catchup` and `GET /v1/system` `catchUp` (trigger lag, ingest queue) |
| Disk filling | `GET /v1/system` `history` and `warnings`, each source's `storage.projectedBytes`, then the retention windows |
| A smoke test against the instance | `FORESIGNAL_TOKEN=fsr_… scripts/smoke.sh` (it enables `usgs-earthquakes`) |
