# Foresignal Operator Guide

How to run and tune a Foresignal instance through its API. Later prompts add sections for their
features. Every write below needs the admin token: `Authorization: Bearer fsr_…`.

The [README](../README.md) has the five-minute quickstart. This guide covers everything after it:
the token, sources, policies and retention, triggers and channels, inbound webhooks and external
data providers, streaming for frontends, and
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
fetch log and `/v1/system`'s full document need a token. The admin token is the `api_token` row
named `bootstrap`; give agents, dashboards and phones their own scoped tokens instead (see
[Tokens, pairing and the audit log](#tokens-pairing-and-the-audit-log)). An install upgraded from
before prompt 023 keeps its admin token: it moves into that row on the first start.

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

## Inbound webhooks

Some data cannot be polled: an SDR decoder, AIS-catcher, Home Assistant, a script, an n8n flow,
Alertmanager, a CAP publisher, or any homelab tool that already speaks ntfy or Gotify. These push
into a **custom source**. Pushed records go through the same writer as polled data, so versioning,
policies, touches, the change stream, triggers, tiles, search and retention all apply.

### Creating a custom source

```json
POST /v1/custom-sources
{"id": "custom-garage", "name": "Garage sensors", "kinds": ["sensor-reading"],
 "observedAtMode": "UPSTREAM", "recordTtl": "1h", "ttlAnchor": "RECEIVED",
 "volatileFields": ["rssi"], "significantFields": [], "titleField": "name",
 "tileProperties": ["value"], "rateLimitPerMinute": 60, "recordsPerMinute": 6000}
```

The response is `201` with `ingestKey` (`eik_…`), **shown once**; only its SHA-256 and first eight
characters (`ingestKeyPrefix`) are stored. Rotate it with
`POST /v1/custom-sources/{id}/ingest-key/rotate`; the old key stops working at once. The id must
start with `custom-` and be unique across all sources. The source is created enabled, appears in
`GET /v1/sources` with `custom: true`, and is always STANDARD tier.

- **Time base (`observedAtMode`).** `UPSTREAM` (the default) means every record carries its own
  `observedAt`; a record without one is rejected `MISSING_OBSERVED_AT`, never silently stamped.
  `RECEIVED` stamps the receipt time on every record (flagged `TIME_IMPUTED`). A source never
  mixes the two, and the mode can change only while the source has no records
  (`409 OBSERVED_AT_MODE_LOCKED`).
- **TTL.** `recordTtl` makes records expire from the current view; `ttlAnchor` counts it from receipt
  (`RECEIVED`, the default) or from `observedAt` (`OBSERVED`).
- **Volatile and significant fields.** Volatile fields (an RSSI, a battery level) are left out of the
  hash, so a change to them alone is a touch, not a version. Significant fields are the ones whose
  change is always worth a version; a SAMPLED or LATEST ingest policy needs them
  (`400 INGEST_POLICY_INVALID` if they are emptied). Editing either list changes the hash recipe: the
  writer rehashes the source's records before its next batch, so the edit never produces a burst of
  spurious versions.
- **Editing.** `PUT /v1/custom-sources/{id}` replaces the definition; the id cannot change, and
  removing a kind that still has current records is `409 KIND_IN_USE`.
- **Caps.** `rateLimitPerMinute` limits requests (the source's budget); `recordsPerMinute` (default
  6,000, at most 60,000) limits records. Over either is `429` with `Retry-After`, and a `REFUSED` row
  appears in `GET /v1/sources/{id}/fetches`. A body is at most 5 MB (decompressed; `gzip` is accepted)
  and 10,000 records.
- **Deleting** a custom source stops ingest; its records age out through retention.
- **Members later prompts own** (`profile`, `allowPathKey`, `ingestAllowCidrs`, `mapping`, `preset`,
  `captureSamples`, `licenceTerms`) are refused with `400` until those prompts.

Every push answers `200` with an `IngestOutcome` (`seen`, `persisted`, `touched`, `rejected` with
each item's index and reason, …) whenever the request itself was valid, even if some records were
rejected. `GET /v1/sources/{id}/fetches` shows each push with `trigger: PUSH`, the principal
(`ingest-key:<prefix>`, `compat-ntfy:<prefix>`, `compat-gotify:<prefix>` or `token:<name>`) and the
body's SHA-256.

### Credentials

Send the key as `X-Foresignal-Ingest-Key: eik_…`, as `Authorization: Bearer eik_…`, or as the
Basic-auth password (any user name), for devices that only do Basic. An admin token also works. A
key belongs to one source; pushing it to another is `401`.

### Formats

`POST /v1/ingest/{sourceId}` reads the `native` format; `POST /v1/ingest/{sourceId}/{format}` reads a
named one. `GET /v1/ingest/formats` lists them with their options. Options live in the source's
`formats` object, keyed by format id.

| Format | Body | Notes |
|---|---|---|
| `native` | GeoJSON FeatureCollection, or `{"records": [...]}` | Feature `id` is the external id; properties `kind` (optional with one kind), `observedAt`, `eventTime`, `validUntil`, `tombstone`, `quality`; the rest is the payload |
| `alertmanager` | Alertmanager webhook payload v4 | One record per alert (`fingerprint`); `kindLabel` picks the kind from a label; `resolved: tombstone` (default) or `record` |
| `cloudevents` | CloudEvents 1.0 structured, batch or binary mode | External id `subject`, else `source/id`; `typeKinds` maps CloudEvents types to kinds; answers the webhook validation handshake (`OPTIONS` with `WebHook-Request-Origin`) |
| `cap` | CAP 1.2 XML | `Alert` creates a record, `Update` also tombstones the referenced alerts, `Cancel` only tombstones; polygons and circles become geometry; non-`Actual` alerts are rejected unless `acceptNonActual` |
| `mapping` | Any JSON | Uses the source's `formats.mapping`: JSON Pointers for `recordsPath`, `externalId`, `kind`, `observedAt` (`observedAtFormat` `ISO`, `epoch-s`, `epoch-ms`), `lat`/`lon` or `geometry`, and `payload` (`[{from, to}]`) |

`media`, `rasters` and `mapping-preview` are reserved for later prompts (`404 FORMAT_UNKNOWN` until
then).

```sh
# native GeoJSON
curl -X POST http://foresignal.local:8080/v1/ingest/custom-garage \
  -H "X-Foresignal-Ingest-Key: $KEY" -H "Content-Type: application/geo+json" \
  -d '{"type":"FeatureCollection","features":[{"type":"Feature","id":"garage-temp",
       "geometry":{"type":"Point","coordinates":[-97.74,30.27]},
       "properties":{"observedAt":"2026-10-12T08:00:00Z","name":"Garage","value":31.5}}]}'

# a CloudEvent
curl -X POST http://foresignal.local:8080/v1/ingest/custom-garage/cloudevents \
  -H "Authorization: Bearer $KEY" -H "Content-Type: application/cloudevents+json" \
  -d '{"specversion":"1.0","id":"e1","source":"/ha/garage","type":"door","subject":"garage-door",
       "time":"2026-10-12T08:00:00Z","data":{"state":"open"}}'

# a CAP alert
curl -X POST http://foresignal.local:8080/v1/ingest/custom-alerts/cap \
  -H "X-Foresignal-Ingest-Key: $KEY" -H "Content-Type: application/cap+xml" --data-binary @alert.xml
```

**AIS-catcher-style GeoJSON.** Point an HTTP output at
`http://foresignal.local:8080/v1/ingest/custom-ais` with the header
`X-Foresignal-Ingest-Key: eik_…` and GeoJSON output; `gzip` is accepted.

**Home Assistant** (`configuration.yaml`):

```yaml
rest_command:
  foresignal_push:
    url: "http://foresignal.local:8080/v1/ingest/custom-home"
    method: POST
    headers:
      X-Foresignal-Ingest-Key: !secret foresignal_ingest_key
    content_type: "application/json"
    payload: >-
      {"records": [{"externalId": "{{ entity_id }}", "kind": "state",
        "observedAt": "{{ now().isoformat() }}", "payload": {"state": "{{ state }}"}}]}
```

**Python:**

```python
import requests
requests.post("http://foresignal.local:8080/v1/ingest/custom-garage",
              headers={"X-Foresignal-Ingest-Key": KEY},
              json={"records": [{"externalId": "probe-1", "kind": "sensor-reading",
                                 "observedAt": "2026-10-12T08:00:00Z", "payload": {"value": 21.4}}]},
              timeout=10).raise_for_status()
```

**Alertmanager** (`alertmanager.yml`), which cannot sign, authenticates with the key as a bearer
token:

```yaml
receivers:
  - name: foresignal
    webhook_configs:
      - url: "http://foresignal.local:8080/v1/ingest/custom-alerts/alertmanager"
        send_resolved: true
        http_config:
          authorization:
            type: Bearer
            credentials_file: /etc/alertmanager/foresignal-key
```

### ntfy and Gotify compatibility

Tools that publish to ntfy or Gotify can publish to Foresignal unchanged. Give such sources
`observedAtMode: RECEIVED` (the messages carry no observation time) and a TTL. The kind is the
source's single kind, or `notification` (which must then be one of its kinds); the payload is
`{title, message, priority, tags, click, topic}`. The external id comes from
`X-Foresignal-External-Id`, an ntfy tag `id=<x>`, or Gotify `extras["foresignal::record"].externalId`,
otherwise each message gets a new ULID.

- **ntfy:** the topic is the custom source id. `POST` or `PUT /compat/ntfy/{topic}` (body = message;
  `X-Title`, `X-Priority` (1–5 or `min`…`urgent`), `X-Tags`, `X-Click`),
  `GET` or `POST /compat/ntfy/{topic}/publish` (also `send`, `trigger`) with `?message=&title=…`, and
  the JSON form `POST /compat/ntfy` with `{"topic", "message", "title", "priority", "tags", "click"}`.
  Credentials: Bearer or Basic with the key, or ntfy's `?auth=` (base64 of `Bearer eik_…`).
- **Uptime Kuma:** add an ntfy notification with server URL `http://foresignal.local:8080/compat/ntfy`
  (Kuma posts the JSON form there), topic `custom-uptime`, and the ingest key as the access token.
- **Gotify:** `POST /compat/gotify/message` (JSON or form `title`, `message`, `priority`, `extras`);
  the key in `X-Gotify-Key` or `?token=` picks the source.
- **Proxmox:** add a Gotify notification target with server URL
  `http://foresignal.local:8080/compat/gotify` (Gotify clients post to `{server}/message`) and the
  ingest key as the token.

`?auth=` and `?token=` values are masked as `***` in logs and in the fetch log. A message carrying
this instance's own origin marker (the ntfy tag `foresignal-origin-<id>` or the Gotify extra
`foresignal::origin`) is acknowledged and dropped, so a notification channel pointed back at
Foresignal cannot feed itself.

### Signatures

A source can require a webhook signature per format under `verifiers.{format}`:

| Scheme | What is checked |
|---|---|
| `hmac-sha256` | `X-Signature-256: sha256=<hex>` (header configurable) over the raw body |
| `standard-webhooks` | `webhook-id`, `webhook-timestamp` and `webhook-signature: v1,<base64>` over `id.timestamp.body`, ±5 minutes, a replayed id refused for 10 minutes |
| `foresignal` | `X-Foresignal-Timestamp` and `X-Foresignal-Signature: sha256=HMAC("<ts>.<body>")`, the scheme of Foresignal's own webhook channel (instance to instance) |

```json
"verifiers": {"native": {"scheme": "hmac-sha256", "secret": "…", "keyOptional": false}}
```

With a verifier set, a request must carry a valid signature **and** the key
(`401 SIGNATURE_INVALID` otherwise); with `keyOptional: true` a valid signature alone authenticates.
Secrets are write-only: reads show `secretSet: true`, and omitting `secret` on an update keeps the
stored one. An admin token is not asked for a signature.

### A geofence rule on a custom kind

Custom kinds work in rules like any other:

```json
POST /v1/triggers
{"name": "Drone near the house", "sources": ["custom-adsb"], "kinds": ["drone"], "mode": "enter",
 "geofence": {"type": "Polygon", "coordinates": [[[-97.75,30.26],[-97.73,30.26],[-97.73,30.28],
              [-97.75,30.28],[-97.75,30.26]]]}, "severity": "warning"}
```

## External data providers

The External Data Protocol (EDP, [docs/External-Data-Protocol.md](External-Data-Protocol.md)) lets
anyone add a data source without changing Foresignal: a provider describes itself with a
**manifest** and pushes or serves **items**. `GET /v1/external/protocol` shows the protocol version
and item types; `GET /v1/external/protocol/schemas/{name}` serves the JSON Schemas.

### Registering a provider

- **By manifest or URL (admin):** `POST /v1/external/sources` with `{"manifest": {...}}` or
  `{"manifestUrl": "https://provider.example/.well-known/foresignal-edp.json"}` (fetched once), plus
  `"approve": true` to activate it at once. The response carries `ingestKey` once. The provider
  becomes the source `ext-{source.id}`.
- **By registration token:** create one with `POST /v1/external/registration-tokens`
  (`{"name", "expiresAt", "maxUses" (default 1), "sourceIdPrefix"}`); the `fsx_…` token is shown
  once. The provider calls `POST /v1/external/register` with `Authorization: Bearer fsx_…` and its
  manifest; the source is created **pending**. A provider id outside the token's prefix is
  `409 SOURCE_ID_NOT_ALLOWED`; a used-up or expired token is `401 REGISTRATION_TOKEN_INVALID`.
  `GET /v1/external/registration-tokens` lists unexpired tokens (never the secret);
  `DELETE /v1/external/registration-tokens/{id}` revokes one.

A manifest is validated first: `400 MANIFEST_INVALID` lists every error with its JSON Pointer, and
a protocol minor version above what this instance supports is `400 PROTOCOL_UNSUPPORTED`.

### Approval and the access block

Nothing from a **pending** or **suspended** provider is stored (pushes answer
`409 SOURCE_PENDING_APPROVAL` / `SOURCE_SUSPENDED`, and it is not pulled). Before approving, read
`GET /v1/external/sources/{id}`: the **access block** says how the provider gets its data
(`official`, `unofficial-api`, `scrape`, `browser`, `other`), how likely it is to break
(`fragility`) and a summary of the upstream's terms; the licence's `redistribution` sets the egress
(`allowed` → `OPEN`, otherwise `LOCAL_ONLY`); `private` marks personal data. Then
`POST /v1/external/sources/{id}/approve`. `POST …/suspend` stops it (records stay);
`DELETE /v1/external/sources/{id}` removes it (records age out through retention).

Provider URLs (manifest, pull, search) must be `https` and may not reach private, loopback,
link-local, carrier-grade NAT or cloud-metadata addresses, including by DNS rebinding or redirects
(only same-host redirects are followed). For a provider on your own network, register it with
`"allowPrivateNetwork": true`; metadata addresses stay refused even then.

A provider (with its ingest key) or you can send `PUT /v1/external/sources/{id}/manifest`. Additive
changes (descriptions, presentation, new optional fields) apply at once; a change to streams, kinds,
item types, modes, payload schemas or provider URLs returns the source to **pending** until you
approve it again, and the response lists those changes in `breakingChanges`.

### Push and pull

- **Push:** the provider sends batches to `POST /v1/external/sources/{id}/items` (JSON, or NDJSON
  with an optional header line) with its ingest key, the same credentials as custom sources.
- **Pull:** a manifest with `transport.pull` makes Foresignal poll `itemsUrl?cursor=&limit=500` every
  `intervalSeconds`, following `nextCursor` and storing the cursor. Enter the credential with
  `PUT /v1/sources/{id}/credentials`: `token` for `bearer`, `value` for `header`, `username` and
  `password` for `basic`. Requests count against the source's budget; provider failures show in its
  health and in `pull.lastError`.

Layer streams become ordinary map layers (their `presentation` hints appear on the provider and in
the kind's TileJSON); atomic streams are stored, searchable and usable in rules, but kept out of the
default tile layer list until prompt 138 binds them. `status` items change the provider's health
without creating records. Snapshot streams remove entities only after a complete, validated snapshot
of the same scope; a snapshot that would remove more than half of a scope is held and the provider
turns degraded.

### Reading validation errors

A push answers `200` with `accepted`, `rejected` (each item's index, stream, id, reason such as
`SCHEMA_INVALID`, `PAYLOAD_INVALID`, `GEOMETRY_INVALID`, `PERIOD_INVALID` or `FORECAST_INVALID`, and
`schemaErrors` with JSON Pointers) and the writer's `outcome`. Valid items in the same batch are kept.
`GET /v1/external/sources/{id}` shows per-stream counts and the last validation errors.
`POST /v1/external/validate` (agent) checks a manifest and sample items without storing anything,
for provider developers and CI.

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

**Browsers and map loaders without an `Authorization` header** use a ticket instead of putting a
token in the URL: `POST /v1/stream-tickets` (any token, `read` or higher) and append
`?ticket=fst_…` to the stream or tile URL. See [Tickets for browser clients](#tickets-for-browser-clients).

**Tiles.** `GET /v1/tiles/{kind}/{z}/{x}/{y}.mvt` serves Mapbox Vector Tiles of current state for one
kind (for example `/v1/tiles/earthquake/3/1/3.mvt`), with `agg=grid` or `agg=hex` for aggregates.
`GET /v1/tiles.json` and `/v1/tiles/{kind}.json` are TileJSON for MapLibre.

**asOf.** `GET /v1/records?…&asOf=2026-10-11T12:00:00Z` returns what Foresignal *knew* at that time
(transaction time): each entity at the version stored then, even if it was revised later. The
response echoes `asOf` and reports `horizon`, the earliest time the involved sources' retention still
supports; earlier times are `400 ASOF_BEYOND_RETENTION`. `asOfSeq` pins the same thing by seq, and
`GET /v1/records/{source}/{id}/versions` lists every stored version.

## Tokens, pairing and the audit log

### Tokens and scopes

Every caller authenticates with `Authorization: Bearer fsr_…`. Scopes, least to most privileged:

| Scope | Can |
|---|---|
| `read` | Every data read (needed only when `openReads` is off), stream and tile tickets, its own device report; later Ask, search and Web Push |
| `agent` | `read` plus managing triggers and briefings and the alert lifecycle; sees private sources |
| `admin` | Everything: sources, credentials, channels, LLM providers, tokens, pairing codes, settings, the audit log |

- **Create:** `POST /v1/tokens {"name": "openclaw", "scope": "agent", "expiresAt": null}` answers
  `201` with `token` once; only its SHA-256 and its first eight characters (`tokenPrefix`) are
  stored. Every token belongs to a principal: name an existing one with `principalId` (id or
  handle), or a new one named after the token is created.
- **List:** `GET /v1/tokens` shows names, scopes, prefixes, `lastUsedAt` (updated at most once a
  minute), expiry, revocation, device and restrictions, never values.
- **Revoke:** `DELETE /v1/tokens/{id}`. The token stops working at once, its tickets are dropped and
  its open streams close. The `bootstrap` token cannot be revoked (`409 BOOTSTRAP_TOKEN`); rotate it
  with `POST /v1/system/admin-token/rotate`.
- **Errors:** a missing or invalid token is `401`; a token whose scope is too low is
  `403 INSUFFICIENT_SCOPE` with `requiredScope`.
- **Always authenticated:** ticket minting, the device report and, from their prompts, Ask, search
  and Web Push need a token even while `openReads` is on, because they cost money or send messages.

### Restricted tokens

`restrictions` narrows a token further:

```json
POST /v1/tokens
{"name": "kitchen-dashboard", "scope": "read",
 "restrictions": {"endpoints": ["GET /v1/records/**", "GET /v1/tiles/**", "GET /v1/tiles.json"]}}
```

- `endpoints` (`METHOD /v1/path/pattern`, Ant style, method may be `*`) is enforced now: any other
  request is `403 ENDPOINT_NOT_ALLOWED`. Each pattern must match an operation the scope can reach.
- `sources`, `kinds` and `bbox` are enforced on every read path from prompt 035. Until then a token
  carrying them is refused on data reads with `403 RESTRICTION_NOT_ENFORCED` rather than served
  unfiltered. Other members (`area`, `placeIds`, `federation`, …) are refused until their prompts.

### Pairing a phone

A phone gets its own `read` or `agent` token without ever seeing the admin token:

1. `POST /v1/pairing-codes {"name": "Pixel", "scope": "read", "ttl": "10m"}` returns `code`
   (`XXXX-XXXX`, single use, at most 1 h) and `qrPayload`
   (`foresignal://pair?u=<publicBaseUrl>&c=<code>`).
2. Show `GET /v1/pairing-codes/{id}/qr.svg` and scan it with the app, or type the code.
3. The app calls `POST /v1/pairing/claim {"code": "…", "device": {"deviceName": "Pixel", "platform": "android"}}`
   and receives its token once.

`admin` is never pairable. Codes are stored hashed and claims are limited to 5 per minute per client
IP and 50 per hour overall (`429 RATE_LIMITED`); every claim is audited. `GET /v1/pairing-codes`
lists open codes (never their values) and `DELETE` withdraws one. The QR image is held in memory, so
after a restart create a new code. Apps report their device later with
`PUT /v1/tokens/current/device`.

### Tickets for browser clients

`EventSource` and map tile loaders cannot send headers, and tokens never belong in URLs. Mint a
ticket with the token instead:

- `POST /v1/stream-tickets {"purpose": "stream"}` → `{"ticket": "fst_…", "expiresAt": …}`. Use it as
  `?ticket=` on `/v1/changes/stream`, `/v1/alerts/stream` or `/v1/events/stream`. The first connect
  must come within 60 s; `EventSource` reconnects with the same URL keep working up to 5 minutes
  after a disconnect and 24 h in total, one stream at a time (`429 STREAM_LIMIT` for a second).
- `{"purpose": "tiles", "ttl": "10m"}` gives a ticket for `/v1/tiles/**` and TileJSON, reusable for
  any number of requests until it expires (at most 1 h).

A ticket carries its token's scope and restrictions, never more; it is refused in the
`Authorization` header and on other paths, is stripped before logging, and is held in memory: after
a restart or a `401`, mint a new one.

### Trusted proxies

Behind Tailscale Serve or Caddy, Foresignal sees the proxy's address. Tomcat honours
`X-Forwarded-For` and `X-Forwarded-Proto` only from addresses matching `FORESIGNAL_TRUSTED_PROXIES`
(a regular expression, or a comma-separated CIDR list). The default trusts loopback only
(`127\.0\.0\.1|0:0:0:0:0:0:0:1|::1`), which is right for a proxy on the host
(`tailscale serve --bg 8080`, Caddy on the host). A proxy in another container reaches Foresignal
from the Docker network, so trust that network, for example
`FORESIGNAL_TRUSTED_PROXIES='172\.(1[6-9]|2[0-9]|3[01])\.\d{1,3}\.\d{1,3}'` for the compose
`tailscale` profile. Never trust addresses a client can reach directly: a client could then forge its
IP. The resolved client IP is what the audit log and the pairing limits use.

### Deep links (`clientLinks`)

`PUT /v1/settings/server` also sets `timezone` and `clientLinks`, the templates apps and
notifications use: `alert` (`foresignal://alerts/{id}`), `record`, `briefingRun` and `pair`. Templates
must use `foresignal://` or an `https://` link you have verified for the app. Once you set
`clientLinks`, alert notifications carry an "Open in app" link next to "Open alert".

### The audit log

`GET /v1/audit` (admin) lists, newest first: every authenticated write with its token, scope,
operation, status, target and client IP; every `401`/`403` on a write path; pairing claims; token
revocations. Filter with `since`, `until`, `tokenId`, `operationId` and `status`, and page with
`cursor` and `limit` (up to 500). Entries never contain secrets, token values or request bodies.
They are kept for `retention.audit` (default 90 days). Writing the log never slows a request: under
extreme load entries are dropped and counted (`foresignal.audit.dropped`).

## LLM

An LLM is optional. Without one, every other feature works exactly as before: briefings use
digest mode, search matches text, enrichment is skipped and Ask answers `409 LLM_NOT_CONFIGURED`.
With one, Foresignal can answer questions about its data (Ask), write briefings, enrich alerts and
search by meaning. Every LLM endpoint needs the `admin` scope.

### Providers and roles

A provider is one model API. Three types exist:

| Type | What it talks to | Defaults |
|---|---|---|
| `anthropic` | Claude, through the official Anthropic Java SDK | `baseUrl` `https://api.anthropic.com`, `chatModel` `claude-opus-5-5`, effort `medium`, `maxTokens` 16000. An `apiKey` is required. No embeddings. |
| `openai-compatible` | OpenAI, OpenRouter, Groq, Mistral, vLLM, LM Studio, llama.cpp server, LocalAI | `baseUrl` is required (for example `https://api.openai.com/v1`) |
| `ollama` | A local Ollama server | `baseUrl` `http://ollama:11434` |

Two roles use providers: `chat` (Ask, briefings, enrichment) and `embedding` (search). Assign them
with `PUT /v1/llm/settings`. A provider needs a `chatModel` for the chat role, and an
`embeddingModel` plus `embeddingDimensions` for the embedding role. Anthropic has no embeddings
endpoint, so pair a Claude chat provider with an Ollama or OpenAI-compatible embedding provider.
A provider that holds a role cannot be deleted (`409 LLM_PROVIDER_IN_USE`) or changed so that it
can no longer serve it; reassign the role first.

`baseUrl` must be `http` or `https` with no user name, password or fragment. Addresses on your own
network are allowed, because a local model is the point.

### A local model (nothing leaves the box)

```sh
docker compose --profile local-llm up -d
docker compose exec ollama ollama pull qwen3:14b
docker compose exec ollama ollama pull nomic-embed-text
```

Then add the provider and assign both roles:

```sh
curl -X POST http://localhost:8080/v1/llm/providers -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"id":"ollama","type":"ollama","chatModel":"qwen3:14b",
  "embeddingModel":"nomic-embed-text","embeddingDimensions":768}'
curl -X PUT http://localhost:8080/v1/llm/settings -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"chatProvider":"ollama","embeddingProvider":"ollama"}'
curl -X POST http://localhost:8080/v1/llm/providers/ollama/test -H "Authorization: Bearer $TOKEN"
```

The Ollama port is bound to `FORESIGNAL_BIND` (localhost by default), like the other services.
If the chat model does not support tools, the provider shows `supportsTools: false` and Ask
answers from retrieved records instead of calling tools.

### Claude

Create an API key in the Anthropic Console (<https://console.anthropic.com>), then:

```sh
curl -X POST http://localhost:8080/v1/llm/providers -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"id":"claude","type":"anthropic","apiKey":"sk-ant-..."}'
```

The key is encrypted with the install key and never returned (`apiKeySet: true`). To change
anything else later, send the provider without `apiKey` and the stored key is kept.

Foresignal sets the effort explicitly for each use: Ask and briefings `medium`, enrichment `low`.
`options.effort` on the provider overrides that for every call (`low`, `medium`, `high`, `xhigh`,
`max`); higher effort spends more tokens. Calls enable Anthropic's server-side refusal fallback, and
a refusal is reported as such (the test endpoint shows `refusalCategory`). The stable system prompt
and tool definitions are cached by Anthropic, so repeated Ask and briefing calls are cheaper; the
`cachedTokens` figures show how much was served from the cache.

### Budgets and costs

Every provider has `requestsPerMinute` (default 30) and `tokensPerDay` (default 2,000,000, counted
as input plus output tokens and reset at 00:00 UTC). Before each call Foresignal estimates its
tokens (prompt characters / 4, plus the output cap) and refuses the call with
`429 LLM_BUDGET_EXCEEDED` when the estimate does not fit in what is left today. A refused call is
never sent. The first daily-budget refusal of a day, and a provider that fails three calls in a row, raise a
`system` notification. Restarting does not reset the day: it is seeded from the recorded usage.

`GET /v1/llm/usage?provider=&from=&to=&resolution=` shows requests, input, output and cached tokens,
errors and refusals per minute, hour or day; `GET /v1/llm/providers` shows each provider's
`tokensToday`, health (`status`, `lastError`, `consecutiveFailures`) and roles; `/v1/system` shows
the `llm` block. To estimate money, multiply the tokens by your provider's prices; the test
endpoint itself spends a few tokens.

Every call is logged with its purpose, provider, model, token counts (cached included), duration
and stop reason. Prompts and responses are logged only when `logContent` is `true` in
`PUT /v1/llm/settings`, at DEBUG level. API keys are never logged.

### What leaves the box

With a frontier provider (Claude, OpenAI, or any remote OpenAI-compatible API), the records the
model reads to answer are sent to that provider. That is your choice to make. Records of private
sources (personal data such as family locations) are withheld from remote providers and sent only
to a local one: an `ollama` provider, or one you mark with `options.local: true` because it runs on
your own network. The `local-llm` profile keeps everything on this machine.

## Search

`GET /v1/search?q=…` finds records (and, once those prompts land, briefing runs and enriched
alerts) by words, by meaning, or both. It needs a token even when open reads are on, because a
semantic query spends the embedding budget.

| `mode` | How it ranks | Needs |
|---|---|---|
| `text` | Full-text match on the embedded text (`websearch_to_tsquery`), plus a substring match on each record's search fields for records that have no document | Nothing; always works |
| `semantic` | Cosine distance between the embedded query and each document | pgvector, an embedding provider and a built index; otherwise `409 SEMANTIC_UNAVAILABLE` |
| `hybrid` (default) | Reciprocal rank fusion of both | Falls back to text with `degraded: true` and a `reason` when semantic search is off |

Filters: `source`, `kind`, `docType`, `bbox`, and `since`/`until` on the effective time (event time
when the record has one, else observation time; `timeField=observed` for the observation time).
Record hits carry the persisted `seq` to cite and the current record. Private sources are never
embedded, and their records only appear in text results for callers allowed to see them. MCP
clients get the same search as the cost-bearing `search` tool.

### The database image

Semantic search stores vectors with the pgvector extension in the same PostgreSQL. The compose
`db` service builds `docker/db` (image `foresignal-db:17-3.5-pgvector`): the pinned
`postgis/postgis:17-3.5` plus `postgresql-17-pgvector` from the PostgreSQL apt repository. Existing
data volumes keep working; the first start after the switch runs `CREATE EXTENSION vector`.

```sh
docker compose build db && docker compose up -d
```

Without pgvector (an external PostgreSQL, or the plain PostGIS image) Foresignal starts normally,
text search works, and `GET /v1/search/status` says why semantic search is off.

### Choosing an embedding model

Assign an embedding provider in `PUT /v1/llm/settings` (see "LLM"). A local Ollama with
`nomic-embed-text` (768 dimensions) keeps everything on this machine; an OpenAI-compatible
provider works the same way. The first assignment builds the index from the current records;
until it finishes, `hybrid` answers from text and `semantic` is `409`.

Only sources that declare `embedFields` (text-heavy feeds such as alerts, warnings and notices) are
embedded. Each record is embedded once per text change: unchanged text is skipped without calling
the model, and new versions are batched 64 texts per call within the provider's budget. When the
budget runs out the worker pauses until the next minute and resumes where it stopped. Retention
waits for the worker (`embedding.cursor`), so a long pause holds old versions back until the clamp
is released at twice the window.

### Changing the model

Changing the embedding model (or `POST /v1/search/reindex`, which also rebuilds with the same
model) never takes search offline. The new index is built in the second vector slot while searches
keep using the old one, with queries embedded by the old model. New records are written to both.
When every document has the new vector, the new index is built concurrently and swapped in; the
old slot is dropped. `GET /v1/search/status` shows the progress:

```json
"reindex": {"state": "backfilling", "fromModel": "ollama:nomic-embed-text:768",
            "toModel": "openai:text-embedding-3-small:1536", "total": 120000, "done": 45000,
            "percent": 37.5, "ratePerMinute": 1900.0, "etaSeconds": 2368, "startedAt": "…"}
```

The backfill embeds every embeddable record again, so it costs one embedding per record (about
`total × average text tokens`; texts are capped at 2,000 characters). A restart resumes from where
it stopped. `DELETE /v1/search/reindex` cancels and discards the new slot. If the old provider is
deleted or loses the old model during the rebuild, searches answer from text (`degraded: true`)
until the swap.

### The `embed` flag

Each source has an `embed` setting (`PUT /v1/sources/{id}` with `{"embed": false}`), on by default
for sources that declare `embedFields`. Turning it off stops embedding the source and deletes its
documents. Turning it back on embeds new versions; run `POST /v1/search/reindex` to embed the
records it already has.

## Briefings

A briefing is a scheduled summary ("morning brief", "weekly maritime summary") delivered to your
channels. It needs no LLM: the default `digest` mode is deterministic markdown. With a chat provider
assigned, `llm` mode writes a cited narrative from the same data instead.

```json
POST /v1/briefings
{"name": "Morning brief", "schedule": "0 7 * * *", "timezone": "America/Chicago",
 "scope": {"kinds": ["earthquake"], "bbox": "-125,24,-66,50", "ruleIds": [], "includeHealth": true},
 "lookback": "24h", "mode": "digest", "channelIds": ["…"], "maxItemsPerKind": 5}
```

- **Schedule.** A standard 5-field cron (`minute hour day-of-month month day-of-week`) in the
  briefing's IANA `timezone` (default `UTC`), so `0 7 * * *` stays at 07:00 local time across DST
  changes. Briefings may run at most every 15 minutes. The scheduler checks every 30 s; runs missed
  while Foresignal was down run once when it is back, never once per missed slot.
- **Period.** `lookback` (1h to 7d) back from the run; without one, since the previous run (capped at
  7 days), or 24 hours for the first run.
- **Scope.** `sources`, `kinds` and `bbox` limit the records; `ruleIds` limits the alert section;
  `includeHealth` adds the source health section. Empty lists mean everything.
- **Channels.** The run is published as a `briefing` notification to `channelIds`, or to every
  channel subscribed to `briefing` when the list is empty. A channel that does not exist, is disabled
  or is not subscribed to `briefing` is reported in `warnings` when you save the briefing; it is not
  an error. The message links to the HTML rendition ("Read more").

### What a digest contains

- **Alerts** in the period, critical first, with counts per severity and state.
- **Activity**, per kind: versions that are `new` (first seen or reappeared), `changed` and
  `removed`, entities `active` in the period (including ones only re-observed, never re-versioned),
  and the delta against the previous period of the same length. Routine versions (samples,
  keyframes, heartbeats) are listed separately and never counted as change.
- **Notable** records per kind, ranked by the source's `rankField` (for example magnitude), else by
  time, each linked to `publicBaseUrl/v1/records/{source}/{externalId}`. Set `publicBaseUrl` in
  `PUT /v1/settings/server` so links work outside the box.
- **Source health**: sources currently degraded or failing (Foresignal does not keep a history of
  health changes yet, so this is the state at run time).
- A **"Counts incomplete (retention)"** note when the period reaches back past what retention kept.

Private sources are counted but their records are not listed, unless the briefing was created or
last saved with the admin token.

### LLM mode

`mode: "llm"` with `instructions` (focus, length, tone; up to 4,000 characters). The model gets the
digest statistics, the notable records and, when semantic search is available, a search on your
instructions within the scope, and must cite `[source:externalId@seq]`. Citations are verified as in
Ask; problems go to the run's `warnings`. If the model fails, refuses, or the LLM budget is spent,
the run falls back to the digest with status `fallback` and the reason in `error`. Records of private
sources never go to a non-local provider. Usage is recorded per run.

### Runs

- `POST /v1/briefings/{id}/run` runs now (agent token) and returns the run, waiting up to 120 s; a
  second run while one is going is `409 BRIEFING_RUNNING`. `POST /v1/briefings/preview` renders a
  definition without storing, delivering or scheduling anything.
- `GET /v1/briefings/{id}/runs` lists runs, newest first; `GET /v1/briefings/{id}/runs/latest` and
  `/runs/{runId}` return one as JSON, or as a self-contained HTML page with `Accept: text/html` or
  `?format=html`. The page is sanitized (no scripts, styles attributes or frames survive, even from
  record titles), follows the browser's light or dark theme, and is served with a strict
  Content-Security-Policy.
- Runs are kept for the `briefings` retention (default 180 days), even after the briefing is
  deleted, and are searchable with `docType=briefing` when semantic search is on. MCP agents use
  `list_briefings`, `get_briefing_run`, `create_briefing` and `run_briefing`.

## Alert enrichment

With a chat provider assigned, a rule can ask for a short summary of what Foresignal knows around each
of its alerts. Set `enrich: true` on the rule, optionally with `enrichRadiusKm` (1 to 1,000, default
100) and `enrichWindow` (10m to 7d, default 24h):

```json
PUT /v1/triggers/{ruleId}
{"name": "Big quakes", "sources": ["usgs-earthquakes"], "conditions": [{"path": "mag", "op": ">=", "value": 5}],
 "enrich": true, "enrichRadiusKm": 150, "enrichWindow": "12h"}
```

`enrich: true` is accepted without a provider: it is stored and enrichment is skipped until a chat
provider is assigned. `PUT /v1/llm/settings` with `"enrichmentEnabled": false` switches enrichment off
everywhere. `testTrigger` reports `"enrich": true` without calling the model.

**What the model reads.** Around the alert's observation time `T`, within the radius and `T ±
enrichWindow`: the record and up to 10 of its earlier versions, records of any kind nearby (also ones
that only moved, without a new version), up to 10 nearby alerts and, when semantic search is on, the
5 best search matches for the alert message. A record without a location gets its history and the
search matches only. The context is capped at 24 KB; the least relevant items are left out first.

**What it writes.** At most about 120 words for the operator: what happened, what is nearby or
related, and why it might matter, citing records as `[source:externalId@seq]` and alerts as
`[alert:id]`. Citations are checked against what the model was given and the ones it invented are
removed. "No notable related activity" means it found nothing relevant. The summary appears as
`summary`, `enrichedAt` and `summaryCitations` on `GET /v1/alerts/{id}` and in lists, in MCP
`get_alert`, and as an `alert.enriched` event on `/v1/alerts/stream` and the `alerts` topic of
`/v1/events/stream` (the full alert; treat it as an update of the alert with the same id).

**Waiting or editing, per channel.** Enrichment never holds a message back longer than the channel
allows:

| Channel | What happens |
|---|---|
| Any type with `waitForEnrichmentSeconds` > 0 (up to 60) | The alert waits up to that long. When the summary arrives the message is sent at once, with it; if it does not arrive in time, the message goes out without it. |
| `discord`, `telegram`, `matrix` with `editInPlace` on (the default) | A message already sent without the summary is edited to add it, if it is less than an hour old. |
| Every other type, wait 0 | The message goes out at once without the summary, and nothing more is sent: no follow-up message. |

`{{summary}}` is in the default body; a custom `bodyTemplate` must include it to show the summary.
Slack and Mattermost incoming webhooks cannot edit messages, so give those channels a wait (10 to 20
seconds is usually enough) if you want the summary in them.

**Cost per alert.** One call per enriched alert, never retried: up to about 6,000 input tokens (the
24 KB context plus a fixed system prompt that providers can cache) and at most 600 output tokens, at
effort `low`. A rule that fires often multiplies that; the LLM budget refuses calls once the day's
tokens are spent, and those alerts go out without a summary. Alerts older than 10 minutes (catch-up
after downtime) are not enriched. Usage shows under purpose `enrichment` in `GET /v1/llm/usage`, and
`foresignal.enrichment.succeeded`, `.failed{reason}`, `.skipped{reason}`, `.dropped` and `.duration`
are exported as metrics.

**What leaves the box.** The context goes to the chat provider. Records of private sources are left
out for a remote provider and sent only to a local one, and an alert on a private source is not
enriched at all by a remote provider. Record text is treated as data: instructions hidden in a record
are not followed, and the model is offered no tools, so it cannot change anything.

## Web Push and UnifiedPush

Foresignal pushes alerts, briefings and system events to browsers (a PWA, the operator console) and
to native Android and Linux apps through [UnifiedPush](https://unifiedpush.org), which speaks the same
Web Push protocol (RFC 8030 with RFC 8291 encryption). Foresignal holds its own VAPID key, generated on
first start, so no push account is needed.

**The Push channel.** One built-in channel named "Push" (type `webpush`) is created at startup and
fans out to every verified subscription. Its events (alert, briefing, system), rule filter, minimum
severity, quiet hours and the global pause apply as for any channel (`PUT /v1/channels/{id}`); it
cannot be created or tested like the others. Its `config.quietHoursMode` decides what quiet hours do:
`defer` (default, as for every channel), `downgrade` (sent at once with `Urgency: very-low` and
`silent: true`) or `drop`. Messages at or above the quiet hours' `bypassSeverity` (critical by
default) always go out. Each subscription adds its own filters (`events`, `ruleIds`, `minSeverity`,
`tags`).

**Subscribing.** A client needs a token (read scope is enough; a token is required even with open
reads). It fetches `GET /v1/push/vapid-public-key`, subscribes, and POSTs the subscription to
`/v1/push/subscriptions`. The new subscription is `pending`: Foresignal immediately sends it a
verification push `{"v":1,"event":"verify","subscriptionId":"…","code":"…"}`, and the client echoes the
code to `POST /v1/push/subscriptions/{id}/verify` within 10 minutes (five wrong codes delete it). Only
verified subscriptions receive notifications. Limits: 5 pending per token, 200 in total. Revoking the
token deletes its subscriptions. `GET /v1/push/subscriptions` (admin) lists them with their host,
status and failure count, never the endpoint.

A service worker for a browser client:

```js
// page: register the worker and subscribe
const reg = await navigator.serviceWorker.register('/sw.js');
const { publicKey } = await (await fetch(`${FS}/v1/push/vapid-public-key`)).json();
const sub = await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: publicKey });
await fetch(`${FS}/v1/push/subscriptions`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${TOKEN}` },
  body: JSON.stringify({ name: 'Laptop', subscription: sub.toJSON(), filters: { minSeverity: 'warning' } }),
});

// sw.js
self.addEventListener('push', (event) => {
  const msg = event.data.json();
  if (msg.event === 'verify') {
    event.waitUntil(fetch(`${FS}/v1/push/subscriptions/${msg.subscriptionId}/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${TOKEN}` },
      body: JSON.stringify({ code: msg.code }),
    }));
    return;
  }
  event.waitUntil(self.registration.showNotification(msg.title, {
    body: msg.body, tag: msg.tag, lang: msg.lang, silent: msg.silent, data: msg,
  }));
});
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const links = event.notification.data.links || {};
  event.waitUntil(clients.openWindow(links.web || links.api));
});
```

The payload is `{v, event, id, alertId, ruleId, severity, title, shortTitle, body, url, links {api,
app, web}, tags, lang, tag, silent, ts}` (absent members omitted), at most 3,993 bytes; long bodies
are cut first. `tag` is `foresignal-alert-<alertId>`, so a resolve replaces the fired notification.
`links.app` and `links.web` come from the server setting `clientLinks`. Safari 18.4 and later can use
`payloadFormat: "declarative"` (WebKit Declarative Web Push, no service worker needed); the
notification then opens `links.web`, or `links.api` when there is none.

A Flutter app with the [`unifiedpush`](https://pub.dev/packages/unifiedpush) package passes the
VAPID key to `registerApp` and gets an already decrypted message:

```dart
await UnifiedPush.initialize(
  onNewEndpoint: (PushEndpoint endpoint, String instance) async {
    final keys = endpoint.pubKeySet!;
    await api.post('/v1/push/subscriptions', {
      'name': 'Pixel', 'transportLabel': 'unifiedpush',
      'subscription': {'endpoint': endpoint.url, 'keys': {'p256dh': keys.pubKey, 'auth': keys.auth}},
    });
  },
  onMessage: (PushMessage message, String instance) async {
    final msg = jsonDecode(utf8.decode(message.content));
    if (msg['event'] == 'verify') {
      await api.post('/v1/push/subscriptions/${msg['subscriptionId']}/verify', {'code': msg['code']});
    } else {
      showLocalNotification(msg['title'], msg['body'], tag: msg['tag']);
    }
  },
);
final vapid = (await api.get('/v1/push/vapid-public-key'))['publicKey'];
if (await UnifiedPush.tryUseCurrentOrDefaultDistributor()) {
  await UnifiedPush.registerApp('default', vapid: vapid);
}
```

**A distributor on the LAN.** Endpoints must be `https` and every address the host resolves to must
be globally routable; loopback, private, CGNAT, link-local, unique-local, NAT64, 6to4 and metadata
addresses are refused. A self-hosted UnifiedPush distributor (for example ntfy) on the LAN is allowed
by listing it in the server setting `push.allowedPrivateHosts`, as an exact host name or a CIDR (IPv4
/8 or longer, IPv6 /32 or longer; no wildcards). It still needs https. The address is checked again
before every send and the connection goes to the checked address; three failed rechecks in a row
disable the subscription. `push.contact` (default `mailto:admin@localhost`) is the VAPID contact the
push services see, and `push.defaultLang` the payload language when a subscription sets none.

**Reach.** Web Push reaches Chrome, Edge and Firefox on desktop and Android while the browser runs,
and Safari when a site is added to the Home Screen. It does not reach the native iOS app, and it does
not wake a closed desktop browser on macOS or Windows. Use native push (APNs and FCM, below) or an
ntfy channel for those.

**Delivery.** Each subscription gets its own send and retries (10 s, 1 min, 5 min, 30 min, 2 h), so a
retry never repeats a push that already arrived. A push service answering 404 or 410 removes the
subscription; 429 and 503 are retried after `Retry-After`. At most 20 pushes per second go to one push
service host. The Push channel's delivery is delivered when at least one subscription received it.

## Native push (APNs and FCM)

The Flutter apps receive alerts on iOS, iPadOS and macOS through Apple Push Notification service
(APNs) and on Android through Firebase Cloud Messaging (FCM). Both are further targets of the
built-in Push channel, so its events, quiet-hours mode and retries apply unchanged.

**The self-hosting constraint.** An APNs key belongs to the Apple developer team that signs the app,
and a Firebase service account to the Firebase project compiled into the app. Foresignal can only
push to an app built and signed with your own team and Firebase project (or through a publisher's
relay, which Foresignal does not provide). Without that, use UnifiedPush (above) or an ntfy channel:
neither needs an account.

**Privacy.** A native push is a visible alert that carries only ids, the severity and a minimal
text; full titles and bodies never leave the server through Apple or Google. Per device,
`contentLevel` chooses the text:

| `contentLevel` | Title | Body |
|---|---|---|
| `minimal` (default) | Foresignal | "Critical alert", "Briefing ready" or "System notice" |
| `short` | Foresignal · critical | the alert's short title (at most 40 characters) |

`short` lets Apple or Google see the rule name; choose it only where that is acceptable. The app's
Notification Service Extension (iOS) or message handler (Android) replaces the text with the real
title and body, which it fetches from Foresignal itself (see `docs/Client-Guide.md`).

**Reachability for the rich text.** The extension fetches from your Foresignal over the phone's own
network: on the LAN, over a VPN, or through Tailscale. It has about five seconds; when Foresignal is
out of reach the minimal text is shown, and opening the notification fetches the details later.

### Create an APNs key

1. In your Apple Developer account, under Certificates, Identifiers & Profiles → Keys, create a key
   with Apple Push Notifications service enabled. Apple's keys are environment-specific: choose
   Sandbox (development builds) or Production (TestFlight and App Store); a team may hold two per
   environment. Older keys that serve both environments still work. Topic-specific keys are tied to
   one environment and a list of bundle ids.
2. Download the `.p8` file (you can download it once), and note the Key ID and your Team ID.
3. Configure the provider (the `.p8` text is the PEM; it is validated as an EC P-256 key, encrypted
   and never shown again):

```http
PUT /v1/push/providers/ios-apns
Authorization: Bearer <admin token>

{"type": "apns", "appIds": ["com.example.foresignal"],
 "config": {"teamId": "ABCDE12345", "keyId": "KEY1234567", "keyEnvironment": "production",
            "timeSensitive": false},
 "secrets": {"p8": "-----BEGIN PRIVATE KEY-----\n…\n-----END PRIVATE KEY-----\n"}}
```

`keyEnvironment` is `sandbox`, `production` or `both` (legacy keys). A device registers with its
own environment, and registration is refused (409 `PUSH_PROVIDER_NOT_CONFIGURED`) when no key
covers it: debug builds use `sandbox`, TestFlight and App Store builds `production`. Set
`timeSensitive` only when the app has the Time Sensitive Notifications capability; critical alerts
are then delivered as time-sensitive. Foresignal never sends Apple's `critical` level, which needs a
special entitlement.

### Create a Firebase service account

1. In the Firebase console of the project the Android app uses, open Project settings → Service
   accounts and generate a new private key. It downloads a JSON file.
2. Configure the provider with the whole JSON (only its `private_key` is stored, encrypted;
   `project_id` and `client_email` fill the config):

```http
PUT /v1/push/providers/android-fcm
Authorization: Bearer <admin token>

{"type": "fcm", "appIds": ["com.example.foresignal"], "config": {},
 "secrets": {"serviceAccountJson": "<the downloaded JSON, as a string>"}}
```

Foresignal uses the FCM HTTP v1 API with an OAuth 2.0 token it mints from the service account
(the legacy FCM APIs no longer exist). An iOS build that registers through FCM instead of APNs is
also served: the message then carries an APNs block.

### Test and monitor

`POST /v1/push/providers/{providerId}/test` mints an APNs provider token or an FCM access token and
reports whether it worked; with `{"deviceId": "…"}` it also sends that device a test notice. A
device's owner can send itself a test with `POST /v1/push/devices/{deviceId}/test` (five per hour).

When Apple or Google rejects the key (an invalid or revoked key, a key of the wrong environment, a
Firebase permission problem), the provider's `lastError` shows the reason code, one
`system.push_provider_failed` notification goes to your other channels (never to Push), and the
pushes are retried after 15 minutes. `lastError` clears on the next success. Responses, logs and
`lastError` never contain a key, a device token or alert text.

### Devices

The app registers each installation with `POST /v1/push/devices` using its own read token (a
paired token from `foresignal://pair`). `GET /v1/push/devices` lists every device for an admin and
only its own for a read token; device tokens are never shown, only their last six characters.
A device whose token Apple or Google reports as gone (APNs 410, `BadDeviceToken`; FCM
`UNREGISTERED`) is removed automatically, and revoking or expiring the API token that registered a
device removes it too. Deleting or disabling a provider stops pushes to its apps' devices until a
provider serves them again; the devices stay registered.

## Acting from notifications

A firing alert can be acknowledged, snoozed (1 h) or its watch muted (4 h) straight from the
notification. Each button or link is a **single-use capability** (`/v1/actions/eac_…`), valid for 12
hours and only for that alert; no API token is ever put on the phone. Opening a link (`GET`) only
shows a confirm page with one button, so link previews, mail scanners and a stray tap never act; the
action happens on `POST`. Acknowledging or resolving the alert any other way revokes the remaining
buttons, and every execution is in the audit log (actor kind `action`, the token shown as `eac_…`).
Acting on an alert that is already acknowledged or resolved answers "already acknowledged".

Per channel, under `config.actions` (`{"enabled": true, "list": ["ack", "snooze", "mute"], "links": true}`):

| Channel | How it acts | Default |
|---|---|---|
| ntfy | Two one-tap buttons (`http` POST, `clear=true`) and **Open** | on |
| Push (Web Push) | Up to two notification buttons; browsers without buttons (Safari) open the confirm page | on |
| Push (native apps) | Category `FORESIGNAL_ALERT_ACTIONABLE`; the app fetches its own buttons from `GET /v1/notifications/{id}`, so no capability goes through Apple or Google | on |
| Telegram | Inline buttons; the message is then edited ("Acknowledged by Ana") and the buttons removed | on |
| Matrix | Confirm-page links; with `"reactions": true`, a ✅ reaction from someone in `allowedSenders` acknowledges | on (links) |
| Pushover | Critical alerts are emergency priority; acknowledging in the Pushover app acknowledges in Foresignal (`pushover:<device>`); the URL opens the acknowledge confirm page | on |
| Webhook | An `actions` array (`action`, `label`, `url`, `expiresAt`) in the default JSON body | opt-in |
| Email, Slack, Discord, Mattermost, Teams, Google Chat, Gotify, … | Confirm-page links at the end of the body (`"links": true`) | opt-in |

The links use the `server` setting's `publicBaseUrl`, which must be reachable from the phone. Set
`"enabled": false` to send a channel no buttons, or narrow `list` (for example `["ack"]`).

**Telegram needs a dedicated bot.** Foresignal long-polls `getUpdates` for button presses, which
Telegram refuses while a webhook is set for the bot, and two programs polling one bot steal each
other's updates. Create a bot for Foresignal alone with @BotFather. `allowedUserIds` (Telegram user
ids) limits who may press; left empty, any member of the channel's chat may.

**Pushover receipts.** Foresignal polls each emergency receipt every 60 seconds (Pushover asks for no
more than every 5 seconds) for up to three hours, until it is acknowledged or expires.

**Why links only confirm.** A capability URL is a credential. Preview bots, antivirus scanners and
browsers prefetch links; if `GET` acted, they would acknowledge your alerts. The confirm page is
plain HTML without scripts or external assets, is never cached and is not indexed; posts from
another site are refused, and the endpoint takes at most 30 requests per minute per client address.

### Presence deferral

When you are at your desk, your phone need not buzz for what you are already looking at. Give the
phone-type channels (Push, ntfy, Pushover, Telegram, Gotify) `"deferWhileActiveSeconds": 120`. Your
desktop client reports `POST /v1/clients/presence {"kind":"desktop","state":"active","ttlSeconds":120}`
every minute or so. While a desktop is active, a non-critical alert waits 120 seconds on those
channels; if you acknowledge, snooze or resolve it, or read it on the desktop, in that time, the phone
copy is marked `suppressed` (visible in `/v1/channels/{id}/deliveries`) instead of sent. Critical
alerts and system events are never held, and presence that stops arriving expires, so everything goes
to the phone again. `GET /v1/clients` (admin) lists the current presence of every device.

### Escalation

An escalation policy reaches further people when nobody acknowledges:

```json
POST /v1/escalation-policies
{"name": "Wake me up",
 "steps": [{"afterMinutes": 10, "channelIds": ["<pager channel id>"], "includePush": true},
           {"afterMinutes": 30, "channelIds": ["<second person's channel id>"]}],
 "repeat": {"everyMinutes": 30, "maxTimes": 3},
 "stopOn": {"resolve": true, "snooze": true}}
```

Put its id in a watch's `escalationPolicyId`. Ten minutes after an alert of that watch fires, unless
it was acknowledged (always), resolved or snoozed (per `stopOn`), the pager channel and Push get
"[Escalation 1] …" with fresh buttons; at 30 minutes the second channel gets "[Escalation 2] …", and
then every 30 minutes up to three more times. Quiet hours hold escalations back except for critical
alerts. Acknowledging stops it at once, cancels escalation messages still waiting and cancels Pushover
emergency retries; muting the watch (`POST /v1/triggers/{id}/mute {"for":"4h"}`) stops escalation of
its open alerts too. `GET /v1/alerts/{id}/escalation` shows where an alert stands. A policy used by a
watch cannot be deleted (`409 POLICY_IN_USE`). Muted watches still record alerts and keep them in the
inbox, but notify nobody until the mute ends (1 hour to 7 days).

## Retention

`GET /v1/settings/retention` shows every window; `PUT` replaces the whole object (every window at
least `1h`); `POST /v1/settings/retention/run` runs the job now and returns what it deleted. The job
also runs hourly, starting 5 minutes after boot.

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "provenance": "30d", "alerts": "90d", "usage": "30d",
 "notifications": "90d", "audit": "90d", "kinds": {"earthquake": "3650d"}, "sources": {"usgs-earthquakes": "1825d"}}
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
| Audit | Audit log entries after `audit` (default 90 days) |
| Inbox | Notifications after `inbox` (default 30 days) once no delivery refers to them, read cursors of devices unseen for 90 days, expired presence and action capabilities expired for a week |

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
(`ok`, `degraded` or `down`), `features` and `limits`. A token of at least the `read` scope adds the
database, records, sources, scheduler, writer, streams, notifications, history per tier, catch-up
and `warnings`:

| Warning | Meaning |
|---|---|
| `OPEN_READS_EXPOSED` | Data reads need no token and `publicBaseUrl` or the published bind is not local (loopback, RFC 1918, 100.64/10 tailnets, link-local, unique-local or `.local`) |
| `OPEN_READS_ALL_INTERFACES` (info) | Data reads need no token and the port is published on `0.0.0.0` |
| `HISTORY_RATE_HIGH` | More than 20,000 versions per minute |
| `PARTITION_HEADROOM_LOW` | A tier has less than one partition block attached ahead |
| `RETENTION_CONSUMER_LAG` | The last retention run stopped waiting for a lagging consumer |
| `PUBLIC_BASE_URL_INSECURE` | `publicBaseUrl` is `http://` on a host that is not loopback, private, 100.64/10 or `.local` |
| `PUBLIC_BASE_URL_MISMATCH` | Requests arrive over HTTPS but `publicBaseUrl` is still `http://` |

Compose passes `FORESIGNAL_BIND` into the container so the warning knows the published bind.

### Logs

`FORESIGNAL_LOG_FORMAT` selects the console format: `ecs` (the default in the container), `gelf`,
`logstash`, or `text` (the default elsewhere). Lines logged while a request is served carry its
`traceId` and `spanId` from Micrometer Tracing (traces are never exported); an unexpected error's
`correlationId`, which the caller sees in the `500` body, is logged with its stack trace.

### Settings export and import

`GET /v1/settings/export` returns every setting (sources without credentials, retention, CORS,
notifications, the watchdog, `security`, `server`, trigger rules with their ids, channels without
secrets, LLM providers without their API keys, the LLM roles, and native push providers without their
keys). Tokens, tickets, pairing codes and push devices are never exported. An imported LLM provider
keeps a key it already has here; set the key of a new one with `PUT /v1/llm/providers/{id}`. An
imported push provider likewise keeps its key; a new one is imported disabled until you set its key
with `PUT /v1/push/providers/{providerId}` and enable it.
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
sets `flush_interval -1`, which SSE needs. Foresignal trusts its forwarded headers only when
`FORESIGNAL_TRUSTED_PROXIES` matches the proxy's address (see [Trusted proxies](#trusted-proxies)).
Prompt 050 adds a Caddy compose profile.

Whichever you use:

- set the `server` setting's `publicBaseUrl` to the HTTPS name with `PUT /v1/settings/server`, so
  notification links and pairing QR codes work from a phone (an `http://` URL on a public host
  answers with `PUBLIC_BASE_URL_INSECURE`);
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
