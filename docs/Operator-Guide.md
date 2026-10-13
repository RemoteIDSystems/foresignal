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
  `{{observedAt}}` and the other placeholders the spec lists, plus the region placeholders of
  [Reference data and region tags](#reference-data-and-region-tags) (`{{near.text}}` …).
- **Region presets:** `"geofenceRef": {"country": ["UA"]}` (or `admin1: ["US-TX"]`,
  `county: ["48453"]`) instead of a polygon. It needs the reference data loaded (otherwise
  `409 REFERENCE_DATA_MISSING`) and cannot be combined with `geofence`, `near` or `hysteresisMeters`.

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

### Places

A place is a named area used everywhere a "where" is needed: rules, saved views, briefings, Ask, the
`place=` filter of the read endpoints, and the weather area of interest.

```json
POST /v1/places
{"id": "home", "name": "Home", "shape": "point", "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]},
 "radiusKm": 25, "timezone": "America/Chicago"}
```

- **Shapes:** a `point` with `radiusKm` (0.1–500, default 20), a `polygon` (fixed automatically; a ring
  that crosses the antimeridian is split), or a `route` (a LineString) with `bufferM` (10–50,000). The
  place's `area` is derived on every save (at most 10,000 vertices) and its `version` goes up.
- **Rules:** `"near": {"placeIds": ["home"], "radiusKm": 10}` fires inside the places' areas, grown by
  `radiusKm`. Editing a place moves its rules at once; a record that does not move never fires because
  of the edit, and one that moves afterwards is judged against the new area on its next observation.
  Alerts record `context.near` (place ids and versions).
- **Deleting** a place that rules, views or briefings use answers `409 PLACE_IN_USE` with the list;
  `?force=true` deletes it and disables those rules (their `lastError` says why). Views and briefings
  that still name it match nothing there. Exit rules on place-weather records can still fire when
  those records are tombstoned.
- **Privacy:** a home location is sensitive. Reading places always needs a token, even with
  `openReads`; `place=` filters do too. Notifications and the LLM get place names and rounded
  distances (`{{place.name}}`, `{{place.distanceKm}}`), never coordinates, and MCP returns coordinates
  only to agent tokens that ask for `detail=long`.
- **Weather AOI:** `GET /v1/places/aoi` is the union of the places, each grown to at least 150 km
  (`foresignal.weather.aoi-buffer-km`). With no places, weather sources use
  `foresignal.weather.default-aoi` (a Texas box) and report a health warning until a place exists.

### Saved views

A saved view is a named filter that the records API, map tiles and the stream render identically:

```json
POST /v1/views
{"slug": "quakes-near-home", "name": "Quakes near home", "kinds": ["earthquake"], "placeIds": ["home"],
 "conditions": [{"path": "mag", "op": ">=", "value": 2.5}], "fields": ["title", "observedAt", "payload.mag"]}
```

`GET /v1/views/quakes-near-home/records`, `/tiles.json` (its tile URLs carry the view's version) and
`/stream`; or `view=quakes-near-home` on `/v1/records`, `/v1/changes`, the streams and `/v1/search`,
combined with any other filter. A view that uses a place or a watchlist needs a token to render.

### Watchlists

A watchlist is a list of identifiers that rules and views match with
`{"op": "inWatchlist", "value": "<watchlist id>"}`: every source that declares the identity takes part.

```text
POST /v1/watchlists                       {"name": "Tails", "identity": "icao24"}
POST /v1/watchlists/{id}/import?dryRun=true   (Content-Type: text/csv)
value,label,notes,tags
a1b2c3,N123AB,Survey aircraft,survey;local
GET  /v1/watchlists/{id}/export
```

- **Identities:** `icao24` (six hex digits), `registration`, `callsign`, `mmsi` (nine digits), `imo`
  (seven digits with a valid check digit) and `custom` (rules must then give a `path`). Values are
  normalized (upper case for aircraft identifiers, digits only for MMSI and IMO) and validated; an
  import reports invalid rows and changes nothing with `dryRun=true`. `mode=replace` also removes what
  the file does not list. At most 100,000 items per list and 5 MB per import.
- **Identifier quality:** prefer ICAO 24-bit addresses for aircraft and IMO numbers for ships.
  Registrations move between airframes; MMSIs are reused, mistyped and spoofed, so an MMSI match is a
  lead, not proof.
- **PIA and LADD:** aircraft in the FAA's Privacy ICAO Address program fly under temporary addresses,
  and LADD-listed aircraft are blocked from many feeds; a watchlist cannot and should not get around
  either. Respect the operators' choice.
- **Policy:** watchlists are for aircraft, vessels and assets of public or professional interest. Do
  not use Foresignal to track private individuals.
- **Effect:** list edits take effect on each entity's next observation; nothing fires for the past.
  Alerts of watchlist rules are sensitive: anonymous readers and feed keys never see them.

### "Near me" rules

A rule can follow a moving record, for example a phone that reports its position through a custom
push source (keep that source private):

```json
POST /v1/triggers
{"name": "Quake near me", "kinds": ["earthquake"], "mode": "enter",
 "conditions": [{"path": "mag", "op": ">=", "value": 5}],
 "near": {"anchor": {"source": "my-phone", "externalId": "pixel"}, "radiusKm": 150, "maxAnchorAge": "PT1H"}}
```

It fires when a quake appears near the phone's current position and when the phone moves near a quake
that is still current (re-checked when the phone has moved `minMoveMeters`, default 200 m).
`radiusKm: 0` means "the phone is inside the record's polygon", for warning areas. While the phone has
not reported for `maxAnchorAge` (default 30 minutes), or its record is gone, the rule is paused, its
`lastError` starts with `ANCHOR_STALE`, and one system notification says so; it resumes on the next
position. Templates get `{{anchor.distanceKm}}`; the phone's coordinates never leave the server.

### Settings export with places and watchlists

`GET /v1/settings/export?includePlaces=true&includeWatchlists=true` adds places, saved views and
watchlists with their items (both default false); the bundle then carries a `warnings` list, because
places reveal locations. `PUT /v1/settings/import` restores them before the rules that use them.

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
`editInPlace`. The region placeholders (`{{near.text}}`, `{{country.name}}` …, prompt 040) read the
alert's region snapshot; `config.units: "imperial"` prints `{{near.text}}` in miles. Links in messages use the `server` setting's `publicBaseUrl` (default
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

### Map attachments

A geolocated alert can carry a static map: the alert's geometry over radar (or other products) on a
Natural Earth basemap, with the frame's valid time, a legend, the attribution and the footer
"Automated — not an official warning" (prompt 055). It is **off by default on every channel**, because
the image leaves your network on a cloud chat service and shows where the alert is. Turn it on per channel
in `config`:

| Key | Default | Meaning |
|---|---|---|
| `attachMap` | `none` | `static` attaches a map to each geolocated alert |
| `mapProducts` | `["mrms-reflectivity"]` | 1–4 products drawn; an alert on a `raster-frame` record defaults to the product that matched |
| `mapRadiusKm` | `50` | radius around the alert geometry (the map always contains the whole geometry) |
| `maxAttachmentBytes` | per type, below | the largest image sent on this channel |

The map is rendered **once per alert** when it fires, at 800 px, after waiting up to 5 s for a frame newer
than the alert, so it shows the weather that caused it. It is stored as media for 7 days (`role: map`) and
reused by every channel. Rendering starts at once and runs alongside the enrichment wait: a send waits for
its map at most 8 s, and never longer than the channel's `waitForEnrichmentSeconds` when that is set; past
that the alert goes out as text. A `satellite-pass` alert attaches the pass composite's 256 px thumbnail
instead of a map.

| Type | How the image is sent | Default cap |
|---|---|---|
| `discord` | multipart `files[0]`, shown in the embed (`attachment://map.png`) | 8 MB |
| `telegram` | `sendPhoto` with the text as caption (≤ 1,024 characters; longer text follows as a message) | 10 MB |
| `ntfy` | `PUT` of the PNG with `Filename: map.png`, the text in the `Message` header | 15 MB |
| `pushover` | multipart `attachment` | 5,242,880 bytes |
| `email` | inline image (`cid:map`) in the HTML part; none with `plainTextOnly` | 10 MB |
| every other type | the text plus a `[Map](…)` link to `{publicBaseUrl}/v1/media/{id}`; `webhook` also lists `attachments[{mediaId, url, contentType, bytes}]` in its default body | — |

**Size control:** an image over the channel's `maxAttachmentBytes` is replaced by a 600 px map, then by
the 256 px thumbnail, then the alert is sent as text only. The level chosen is shown on the delivery
(`attachmentLevel`: `map-800`, `map-600`, `thumbnail` or `text-only`), with the media references in
`attachments`; a retry resends the same image from the media store.

**ntfy attachments** need attachments enabled on the server: set `attachment-cache-dir` (a directory or
an S3 URL) and `base-url` in `server.yml` (ntfy's own option names; `attachment-file-size-limit` caps a
file, 15M by default). ntfy.sh has them on. When the server answers `413` (too large, or the bandwidth
limit) or `400` with code `40014` (attachments not allowed), Foresignal sends the text message instead and
records `text-only`.

**Links on the other types** point at the in-app media URL, which needs a token unless reads are open,
and works only where `publicBaseUrl` is reachable (your LAN or a VPN). There are no signed URLs in v1.
Private-source alerts never carry a map to a channel without `privatePayload: true`, and products of
private sources are left off the map for such a channel.

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

**Time travel.** The same transaction-axis replay serves paused maps (`asOf` on tiles and TileJSON),
trails (`GET /v1/records/{source}/{id}/track`), playback (`GET /v1/history/frames`) and scrubber
histograms (`GET /v1/timeline`). Past asOf tiles and tracks are sent with
`Cache-Control: …, max-age=31536000, immutable` once their instant is at least 2 minutes old, at least
1 hour after the retention horizon and not pending downsampling, so a caching proxy (Caddy, a CDN)
may keep them; `public` is used only while `openReads` covers the path and no private source is
involved. Chrome ignores `immutable` but honors the year-long `max-age`. The client recipe is in the
Client Guide ("Time travel").

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

- `endpoints` (`METHOD /v1/path/pattern`, Ant style, method may be `*`): any other request is
  `403 ENDPOINT_NOT_ALLOWED`. Each pattern must match an operation the scope can reach.
- `sources`, `kinds`, `bbox` and `area` (a GeoJSON Polygon or MultiPolygon, at most 2,000 vertices)
  narrow every read: records, history, changes, tiles, streams, search, alerts, the inbox, the digest,
  briefings, MCP tools and Ask. A request filter is intersected with the restriction, and a record
  outside it answers `404`. `excludePrivate: true` hides private sources even from an agent token,
  and `maxRequestsPerMinute` sets the token's own read rate. `placeIds` (036) limits the token to
  the union of those places' areas (an unknown or deleted place lets nothing through); it is refused
  for the anonymous profile and feed keys, because places are never anonymous. The federation
  members (113) are refused until their prompt. Admin tokens cannot carry restrictions
  (`400 RESTRICTION_NOT_ALLOWED`); an admin can mint any token anyway.

See "Exposing Foresignal" below for kiosks, family phones, feed keys and limits.

### Exposing Foresignal

Before Foresignal is reachable beyond your LAN (a tailnet shared with others, a public name behind
Caddy), close the perimeter:

- **Restricted tokens for kiosks and family phones.** Give each device a `read` token restricted to
  what it shows, for example a wall map:
  `{"name":"hall-map","scope":"read","restrictions":{"kinds":["earthquake","weather-alert"],"bbox":[-80.5,43.0,-78.5,44.5]}}`.
  A lost phone then exposes only that slice, and revoking the token cuts it off.
- **The anonymous profile.** While `openReads` is on, tokenless reads run under `security.anonymous`
  (`PUT /v1/settings/security`), for example
  `{"openReads":true,"anonymous":{"kinds":["earthquake"],"maxRequestsPerMinute":60}}`. Private
  sources are never served anonymously, and the inbox, places, watchlists and every
  always-authenticated operation need a token. `GET /v1/system` warns
  (`security.warnings: ANONYMOUS_UNRESTRICTED`, also logged) when `openReads` is on, `publicBaseUrl` is
  a public name and the profile restricts nothing; turn `openReads` off or restrict the profile.
- **Feed keys for clients that cannot send headers** (KML NetworkLinks, iCal subscriptions, feed
  readers, Home Assistant `geo_json_events`, a browser `EventSource`):
  `POST /v1/feed-keys {"name":"ha-map","prefixes":["/v1/records"],"restrictions":{"kinds":["earthquake"]},"expiresAt":"2027-06-01T00:00:00Z"}`
  returns the `fsf_` key and a ready-made URL per prefix, once. The URL `/v1/k/{key}/records?…`
  serves `GET`/`HEAD` of paths under the prefixes only, inside the key's restriction, and never
  private sources. A restriction (sources, kinds or area) is mandatory and the key expires within
  365 days. `GET /v1/feed-keys` lists keys without values; `DELETE /v1/feed-keys/{id}` revokes one,
  after which its URLs answer `410`. Treat the URL as a password: responses carry
  `Referrer-Policy: no-referrer` and `X-Robots-Tag: noindex`, and are never cached publicly.
- **Tile keys for map clients** (MapLibre, maplibre_gl, Cesium, a web map's `EventSource`): an `agent`
  token mints `POST /v1/tile-keys {"name":"kitchen-tablet","prefixes":["/v1/products/","/v1/styles","/v1/changes/stream"],"ttl":"7d"}`
  and gets the `fsk_` key and its `urlBase` once. They are the same path carrier as feed keys, with map
  prefixes only (`/v1/products/`, `/v1/styles`, `/v1/media/`, `/v1/tiles/`, `/v1/maps/static`,
  `/v1/sample`, `/v1/ground-stations/`, `/v1/changes/stream`, `/v1/events/stream`), a lifetime of at
  most 30 days, an optional restriction, and never private sources. An unknown, expired or revoked key
  answers `401 INVALID_TILE_KEY`. Mint one per device and rotate weekly; `docs/Frontend-Guide.md` has
  the client recipes. Responses through a tile key follow the normal cache rule, so content-addressed
  tiles stay cacheable (`public` only while `openReads` is on and the key has no restriction).
- **Limits.** Every client is rate-limited per minute. Defaults: read 600, tiles 1,200, streams 30
  opens and 8 open at once, expensive (search, Ask, LLM tools, sampling) 20, write 120, and
  anonymous 120 per IP. Change them with `PUT /v1/settings/ratelimits`; they apply immediately. To
  give a kiosk or dashboard token a different read limit, set `maxRequestsPerMinute` in its
  restrictions. Admin tokens are not limited on reads and tiles. Set `metricsPerMinute` to limit
  Prometheus scrapes per IP, and `metricsOpen: false` (`PUT /v1/settings/security`) to require an
  admin token for `/actuator/prometheus`. Behind a reverse proxy, list the proxy in
  `FORESIGNAL_TRUSTED_PROXIES`; otherwise every client shares the proxy's address and its limits.
  Repeated wrong credentials from one address get `429` with a growing `Retry-After`, up to 5
  minutes, while a correct token from the same address always works. Clients should honor
  `Retry-After`.
- **Log redaction.** Feed and tile keys are credentials in the path. Foresignal's access log (off
  unless `FORESIGNAL_ACCESS_LOG=true`) logs the URI with the key cut to its first eight characters
  (`/v1/k/fsk_AbCd…/…`, the same prefix the key listings show) and without the query string. `docker/caddy/Caddyfile.example` redacts both the key and `?ticket=` values in Caddy's
  log; do the same in any other proxy that logs request lines.
- **Outbound URLs.** Channel URLs, LLM `baseUrl`s, push endpoints and other operator-supplied URLs
  are checked against the outbound URL policy on save and on every send: link-local and cloud
  metadata addresses are always refused, and LAN addresses only where the field allows them.
  `PUT /v1/settings/outbound` adds `allowHosts`/`allowCidrs` and `denyHosts`/`denyCidrs`
  overrides (deny wins).

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
`tailscale` and `web` profiles. Never trust addresses a client can reach directly: a client could then forge its
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

## Reference data and region tags

Foresignal gives every record a human place context without any online geocoder (prompt 040):
country, first-level region, US county, sea area, and the nearest town ("12 km NNE of Austin, TX").
Tags are stored beside the record (`record_region`), never in its payload, so they never create
versions or change hashes.

**Datasets.** On the first start, when `foresignal.reference.auto-load` is on (the default), the
default datasets load in the background about 30 s after startup. Loading never blocks startup or
readiness; the `reference` readiness detail and `GET /v1/reference` show progress and failures, and a
failed load raises a `system.reference_load_failed` notification.

| Dataset | Source | Licence | Download | Table |
|---|---|---|---|---|
| `ne-admin0` | Natural Earth 10m admin-0 countries v5.1.2 | Public domain | 13 MB | `ref_country` |
| `ne-admin1` | Natural Earth 10m admin-1 states and provinces | Public domain | 41 MB | `ref_admin1` |
| `ne-admin2-us` | Natural Earth 10m admin-2 (US counties) | Public domain | 8 MB | `ref_admin2` |
| `ne-marine` | Natural Earth 10m marine polygons (sea areas) | Public domain | 1.7 MB | `ref_sea` |
| `geonames-cities15000` | GeoNames, places over 15,000 people (34,160 rows on 2026-10-12) | CC BY 4.0 | 3.4 MB | `gazetteer_place` |
| `geonames-admin1` | GeoNames first-level region names | CC BY 4.0 | 0.2 MB | `gazetteer_admin1` |
| `geonames-countryinfo` | GeoNames country names | CC BY 4.0 | 32 KB | `gazetteer_country` |
| `geonames-cities1000`, `geonames-cities500` (optional) | GeoNames, places over 1,000 / 500 people (171,192 / 236,046 rows) | CC BY 4.0 | 11 / 14 MB | `gazetteer_place` (replaces) |

Reload or add datasets with `POST /v1/reference/load {"datasets": ["ne-admin1", "geonames-cities1000"]}`
(admin, 202). Only names from the bundled manifest are accepted, never URLs. Downloads go through the
same budgeted client as adapters (id `foresignal-reference`, 10 requests a minute, 100 a day,
Foresignal's User-Agent) into `${FORESIGNAL_DATA_DIR}/reference/`. Each file is checked against the
SHA-256 pinned in the manifest; a mismatch is `REFERENCE_CHECKSUM_MISMATCH` and the dataset is not
loaded. GeoNames regenerates its dumps every day, so its entries are verified only when you pin a
checksum (`foresignal.reference.checksums.geonames-cities15000: <sha256>`); otherwise the observed
SHA-256 is recorded in `GET /v1/reference`. Every load goes into a staging table and is swapped in
inside one transaction, so readers never see a half-loaded table.

**Disk use.** The default downloads take 65 MB in `${FORESIGNAL_DATA_DIR}/reference/` (measured
2026-10-12; you may delete them after a load). In PostgreSQL the boundaries are stored at full Natural
Earth resolution plus a simplified copy, with GiST indexes; check the real size with
`SELECT pg_size_pretty(sum(pg_total_relation_size(c.oid))) FROM pg_class c WHERE c.relname LIKE 'ref\_%'
OR c.relname LIKE 'gazetteer\_%'`.

**Offline installs.** Copy the files named in `src/main/resources/reference/manifest.json` into a
directory and set `foresignal.reference.offline-dir` (or `FORESIGNAL_REFERENCE_OFFLINEDIR`) to it;
the loader reads them instead of downloading and still verifies the checksums. Without any reference
data, tags still carry the country, from a simplified boundary set bundled with the application, and
`GET /v1/reference/countries` serves the bundled country table.

**Boundaries.** Natural Earth draws de facto boundaries (who controls the ground), which are not an
endorsement of any claim. `foresignal.reference.ne-admin0-variant: ind` (or `arg`, `chn`, `deu`,
`fra`, `gbr`, `rus`, `tur`, `twn`, `ukr`, `usa` … every Natural Earth point-of-view file is in the
manifest with its hash) loads a point-of-view file instead.

**Licences.** Natural Earth is public domain. GeoNames is CC BY 4.0: the credit is in
`THIRD_PARTY_NOTICES.md`, in `GET /v1/reference`, and in every response that carries a `near`
(`RegionTags.attribution`, GeoJSON `properties.nearAttribution`). Keep it when you republish those
fields.

**How records are tagged.** Per source, `regionTagging` (`PUT /v1/sources/{id}`) is `eager` (default
for STANDARD sources: tagged as versions persist), `lazy` (default for TRACK sources: tagged when a
template renders, an alert fires, or a record is read with `include=region`) or `off`. A moving entity
is tagged again once it is more than 2 km (10 km for TRACK sources) from where it was last tagged. A
point outside every country but within 5 km of one gets that country; at sea the country is empty and
`sea` is set. After a reference load every current record of an eager source is tagged again.

**Using the tags.**
- `include=region` on `GET /v1/records` and `GET /v1/records/{source}/{id}` adds `region`; GeoJSON
  features gain `properties.near`.
- Filters `country=UA,PL`, `admin1=US-TX` and `county=48453` on records (JSON and GeoJSON), `/v1/search`,
  `/v1/alerts` (the alert's region at fire time), the stats reads (answered from raw rows) and briefing
  scopes. Unknown codes are `400 UNKNOWN_REGION`. Record filters see tagged records only, so a mover may
  lag by up to one retag threshold, and lazy sources match only once their records were tagged.
- `GET /v1/reference/reverse?lon=-97.7&lat=30.37` and the MCP tool `reverse_geocode` tag any point.
- Template examples:

```text
{{name}}: M{{payload.mag}} {{near.text}}            → Quake: M4.6 12 km NNE of Austin, TX
{{country.name}} / {{admin1.name}} / {{county}}     → United States of America / Texas / Travis
{{near.km}} km ({{near.mi}} mi) {{near.bearing}} of {{near.name}}
{{near.text|imperial}}                              → 8 mi NNE of Austin, TX
```

## Stats, glance and metrics

### Activity rollups

Foresignal keeps an `activity_rollup` table so clients can draw activity charts and maps without
paging raw history, and so the counts outlive retention. A job runs every 5 minutes and rolls up each
closed hour (and, once its 24 hours are done, each UTC day) from `record_version`. It works from a
durable cursor (`app_setting['stats.rollup.cursor']`) written in the same transaction as the rows, so a
crash never double-counts or skips an hour.

- **Buckets are on the transaction axis:** hour `H` holds the versions *ingested* in `[H, H + 1 h)`.
  Stats on the observed, effective or event time are served from raw history, inside retention.
- **Measures:** `versions` (HEARTBEAT and KEYFRAME excluded), `entities` (distinct entities),
  `firsts` (FIRST and REAPPEAR), `tombstones`, `edges` (TRIGGER_EDGE). Rows hold no entity ids.
- **Cells:** each version's centroid is mapped to an H3 cell at `foresignal.stats.cell-res` (default 4,
  about 1,770 km² per cell). Versions without a geometry go to cell 0.
- **First start:** the job backfills from the oldest retained version, at most
  `foresignal.stats.backfill` (default `30d`) back, 6 hours per run.
- **Retention:** the rollup cursor joins the consumer clamp (`min(trigger, embedding, rollup)`), so raw
  versions are never deleted before they are rolled up. Rollup rows have their own windows:
  `RetentionSettings.stats = {"hourly": "35d", "daily": "1825d"}`.
- **Which sources:** private sources are skipped unless `PUT /v1/sources/{id}` sets
  `statsIncludePrivate: true`; set `statsEnabled: false` to leave a noisy source out.
- **Rebuild:** `POST /v1/stats/rollup/rebuild {"from": "…", "to": "…"}` (admin) recomputes the closed
  buckets in the range, per source, only where that source's raw versions are all still retained.

**Storage estimate.** Rows ≈ hours × sources × kinds × occupied cells, plus one totals row per
source and kind per hour. Point sources in a few hundred cells cost little. **Global aircraft at
resolution 4 occupy about 20,000 cells an hour, about 0.5 million rows a day** (about 17 million rows in
the 35-day hourly window). If that is too much, lower `foresignal.stats.cell-res` to 3 (about 7 times
fewer cells) or set `statsEnabled: false` on the aircraft sources.

### Stats endpoints

| Endpoint | What |
|---|---|
| `GET /v1/stats/timeseries` | `bucket=1h\|1d\|auto` (≤ 2,000 buckets), `measure`, `groupBy=none\|source\|kind`, `timeField`. `backend` says `rollup`, `raw` or `mixed` |
| `GET /v1/stats/grid` | H3 cells as GeoJSON (`res` ≤ `cellRes`), or `mode=raw&sizeKm=` hexagons in EPSG:3857 (≤ 7 days); ≤ 10,000 cells |
| `GET /v1/stats/top` | Top sources, kinds, cells or entities (`by=entity` reads raw history inside retention) |
| `count=true` | On `/v1/records`, `/v1/history` and `/v1/search`: `{count, exact, watermarkSeq}`, exact up to 1,000,000 |

Every stats query has a 10-second statement timeout (`503 QUERY_TIMEOUT`). With a bbox, a restricted
area or a place, rollup rows are chosen by cell centre, so edges are accurate to a cell, and
`entities` counts an entity once per cell it visited.

### Glance

`GET /v1/glance` is a status document of at most 2 KB for widgets, watches and e-ink displays:
`status`, open alerts per severity with the latest three, source tallies, the latest briefing,
versions in the last hour, `lastSeq` and deep links. `?format=text` (or `Accept: text/plain`) gives at
most 8 lines of 40 ASCII characters. `?place=<id>` narrows the alerts to a place (token required).
Answers are cached for 15 seconds and carry a strong ETag, so polling with `If-None-Match` is cheap.

Without a token (under `openReads`) only `status`, `serverTime`, the latest alerts and `lastSeq` are
shown. To show the counts too, set the app setting `glance.exposeCountsAnonymously` to `true`.

- **TRMNL:** the TRMNL cloud accepts at most 12 webhook updates an hour, so whatever forwards the
  glance (a small script or an automation fetching `/v1/glance` with a read token) should coalesce to
  one update every 10 minutes or more. Bring-your-own-server (BYOS) setups need a renderer such as
  Terminus that turns the JSON into the panel image.
- **MagicMirror:** any JSON or text module can poll `/v1/glance?format=text` every minute.
- **Home Assistant:** a REST sensor:

  ```yaml
  sensor:
    - platform: rest
      name: Foresignal
      resource: http://foresignal.local:8080/v1/glance
      headers:
        Authorization: !secret foresignal_read_token
      value_template: "{{ value_json.status }}"
      json_attributes: [alerts, sources, activity, lastSeq]
      scan_interval: 60
  ```

- **Garmin and Wear OS:** the watch platforms need native code (a Connect IQ widget, a Wear OS tile)
  that fetches the JSON glance through the phone; the 2 KB limit keeps it within their request
  budgets.

### Metric projections

`/v1/metrics` exposes payload fields of current records as Prometheus gauges, from a registry of its
own: it never appears on `/actuator/prometheus`, and it needs the `read` scope (Prometheus sends the
token as a bearer). Define projections with `PUT /v1/settings/metrics` (admin):

```json
{"projections": [{"name": "usgs_magnitude", "source": "usgs-earthquakes", "kind": "earthquake",
  "path": "mag", "unit": "magnitude", "help": "Magnitude of current USGS earthquakes", "maxSeries": 500}]}
```

Each projection becomes `foresignal_data_<name>{source, external_id}` (or the projection's own
`labels`, at most 4) plus `foresignal_data_<name>_observed_timestamp_seconds`. Values follow live state
(SAMPLED sources included); a series disappears when its record is tombstoned, expires or is purged.
Non-numeric values are skipped and counted in `foresignal.projection.coercion_failures{projection}`.
Each projection keeps at most `maxSeries` series and the whole endpoint at most 10,000; reaching that
raises a `system` notification once. A private source needs `allowPrivate: true`, and its series are
shown only to callers that pass the private gate (an agent or admin token).

A Prometheus scrape job:

```yaml
scrape_configs:
  - job_name: foresignal-data
    metrics_path: /v1/metrics
    scrape_interval: 60s
    authorization:
      type: Bearer
      credentials_file: /etc/prometheus/foresignal.token
    static_configs:
      - targets: ["foresignal.local:8080"]
```

**Grafana without Prometheus:** the Infinity data source can chart the stats API directly. Add a
query of type JSON, URL `http://foresignal.local:8080/v1/stats/timeseries?bucket=1h&measure=versions&groupBy=source&from=${__from:date:iso}&to=${__to:date:iso}`,
an `Authorization: Bearer …` header, root selector `series`, and columns `key` (string) and
`points` expanded to `t` (time) and `value` (number).

## Personal location and privacy

Prompt 039 makes it safe to feed family phones and personal trackers into Foresignal. A **private**
source is invisible on every read and egress path unless the caller is explicitly allowed; the four
common tracker apps push without glue; each phone gets a key that can write only its own entity; and
any single entity can be exported or erased.

### Create a personal-location source

```http
POST /v1/custom-sources
{"id": "custom-family", "name": "Family phones", "profile": "personal-location"}
```

The `personal-location` profile fills in what the request leaves out:

- `private: true`. It cannot be unset: `"private": false` with this profile is a 400.
- STANDARD history tier, kinds `tracker` (fixes) and `tracker-transition` (OwnTracks region events).
- `observedAtMode: UPSTREAM`, `ttlAnchor: OBSERVED`, `recordTtl: 24h`, default retention 7 days.
- Ingest policy SAMPLED: `minInterval` 30 s, keyframe 10 min, `minDeviation` 50 m,
  `significantFields: [motion, charging]`.
- No embedding (`embedFields` are refused) and no enrichment (the private gate below).
- No precision cap: the stored coordinates keep 7 decimals. It is your own data; set
  `privatePrecisionDecimals` yourself if you want the 008 cap.
- Field specs `speedMs` (m/s), `courseDeg` (deg), `altitudeM` (m), `battery` (%), `charging`, `motion`.

Explicit members win over the profile, except `private`.

### One key per phone

```http
POST /v1/custom-sources/custom-family/devices
{"externalId": "alex-phone", "name": "Alex's phone"}
```

The answer carries the device key (`eid_…`) **once**, plus ready-made settings for each app under
`setup`, all built from the `server` setting's `publicBaseUrl` (set it to the address the phones
reach). A device key:

- authenticates only for its source, and forces every record it pushes to its bound `externalId`. A
  payload that names another device (`id`, `deviceid`, `device_id`, `dev`) is refused with
  `403 DEVICE_ENTITY_MISMATCH` and nothing is written;
- is accepted as `X-Foresignal-Ingest-Key`, `Authorization: Bearer`, the Basic-auth password (any
  username), or in the path: `/v1/ingest/{sourceId}/k/{key}/{format}`;
- stops working the moment it is revoked (`DELETE …/devices/{deviceId}`) or rotated
  (`POST …/devices/{deviceId}/key/rotate`, which returns the new key and setup once). The device's
  records stay until retention removes them or you purge them.

`GET /v1/custom-sources/{id}/devices` lists the devices with their key prefix and `lastSeenAt`; key
values are never shown again.

### App setup

| App | Setting | Value |
|---|---|---|
| OwnTracks (HTTP mode) | Import `setup.owntracks` as a `.otrc` file, or set Mode HTTP, URL `<base>/v1/ingest/<source>/owntracks`, Authentication on, any username, password = the device key | Foresignal replies `200 []` |
| Overland (iOS) | Receiver endpoint `setup.overland.endpoint`, access token `setup.overland.token` (sent as `Authorization: Bearer`) | Foresignal replies `{"result":"ok"}` even when some points are rejected (rejections are counted, not retried) |
| Traccar Client, OsmAnd | Server URL `setup.traccar.serverUrl` (`<base>/v1/ingest/<source>/k/<key>/osmand`); query, form or JSON bodies are all read | `200` with an empty body |
| GPSLogger (Android) | Log to custom URL: `setup.gpslogger.urlTemplate`, method GET (POST with a form or JSON body works too; Basic or Bearer auth instead of the path key is accepted) | `200` |

The GPSLogger template is
`lat=%LAT&lon=%LON&time=%TIME&acc=%ACC&alt=%ALT&spd=%SPD&dir=%DIR&batt=%BATT&dev=%SER`; parameter names
are read case-insensitively. Check the placeholders against the app's own Parameters screen (in the
custom URL settings): its website names only `%LAT`, `%LON`, `%TIME`, `%SAT`, `%ACC` and `%ALL`, and
the full list lives in the app. Foresignal reads `spd` as m/s.

OwnTracks region events (`_type=transition`) become a separate `tracker-transition` record whose
externalId is the phone's plus `:transition`, so an event never replaces the position.

Every fix maps to kind `tracker` with `speedMs`, `courseDeg`, `altitudeM`, `battery`, `charging`,
`motion` and `quality.accuracyMeters`. Units are normalized: OwnTracks `vel` is km/h, OsmAnd query
`speed` is knots, everything else is m/s.

- **Accuracy filter:** a fix whose accuracy is worse than `maxAccuracyMeters` (a format option under
  `formats.<format>`, default 200 m) is rejected with reason `LOW_ACCURACY`. Fixes with `valid=false`
  (`INVALID_FIX`) and exact (0, 0) (`NULL_ISLAND`) are rejected too. Overland's `wifi` name is dropped
  unless `formats.overland.keepWifi` is true.
- **OwnTracks encryption** is not supported: encrypted payloads are acknowledged with `[]`, dropped
  and counted, and the source's health shows "OwnTracks payload encryption is not supported; disable
  it".
- **Path keys and GET.** A key in the path, a key in the query or a GET push is accepted only from
  the addresses in the source's `ingestAllowCidrs`, or, when that is unset, from private addresses
  (loopback, RFC 1918, link-local, ULA); anything else is `403 INGEST_SOURCE_DENIED`. A phone on
  mobile data reaching you through a VPN (WireGuard, Tailscale) arrives from a private address; for a
  public reverse proxy, list its address range. The source ingest key (`eik_`) is accepted in the
  path only with `allowPathKey: true` (otherwise `404`); device keys always are.

### Logs and reverse proxies

Foresignal never logs a query string or a path that carries a key: `/k/{key}/` is rewritten to
`/k/redacted/` before anything (the token filter, problem documents, the fetch log, the access log)
sees it, and `key=`, `auth=` and `token=` query values are redacted. Do the same in front of it:

- never log `$request_uri` or the query string for `/v1/ingest/*/k/*` or anything carrying `key=`,
  `auth=` or `token=`;
- log the path with the `/k/[^/]+/` segment rewritten (nginx: a `map $uri` to `/k/redacted/`), or
  turn access logging off for `/v1/ingest/`;
- never log the `Authorization` or `X-Foresignal-Ingest-Key` headers.

### What `private` hides, and from whom

A source is private when its descriptor says so (built-in TAK, Home Assistant and mail sources,
`CustomSourceInput.private`, an EDP manifest's `private`) or when you tighten it with
`PUT /v1/sources/{id} {"private": true}`. You can make any source private, never a descriptor-private
source public (`"private": false` is a 400).

Private sources are visible only to:

- `admin` tokens;
- `read` and `agent` tokens, feed keys and pairing codes created with `privateAccess: true` (tickets
  inherit their token's grant). An `agent` token **without** the grant no longer sees them;
- never to anonymous callers, whatever `openReads` says.

For everyone else a private source does not exist:

| Path | Behavior for a caller without access |
|---|---|
| `/v1/records` (list, get, versions, track, asOf, `count=true`), `/v1/history`, `/v1/history/frames`, `/v1/timeline`, `/v1/changes`, `/v1/kinds` counts | absent from lists and counts; a direct read is `404`, never `403` |
| `/v1/sources` | absent; `GET /v1/sources/{id}` is `404` |
| Tiles | kind layers leave private sources out; a private source layer is `404`; TileJSON and the layer index omit them. A tile or TileJSON that can contain private data is sent `Cache-Control: private` only |
| SSE (`/v1/changes/stream`, `/v1/events/stream`, alerts, inbox) | filtered per client at the hub; revoking the token or feed key sends `reset` (reason `revoked`) and ends its streams |
| Search | text search leaves them out; they are never embedded (`embed` is forced off and existing embedding rows are deleted when a source becomes private) |
| MCP | every tool filters per token with the same rule |
| Alerts | alerts on private records (snapshot included) are hidden; see notifications below |
| Stats, glance, metrics | rollups skip private sources unless `statsIncludePrivate`; glance hides their alerts; metric projections need `allowPrivate` and a caller with access |

Every private-data read by a non-admin principal (a token or feed key holding the grant) is written to
the audit log (`targetType: private_read`), as is every export and purge.

### Notifications

A notification about a private-source alert carries only the rule name, the severity and a link,
unless the channel has `config.privatePayload: true` (an admin opt-in per channel). Web Push, APNs and
FCM payloads never carry it, whatever the channel says, because they cross third-party push services;
the app fetches the details over its own authenticated connection.

### LLM egress

Private records are never placed in a prompt for a provider classified as remote. A provider counts
as local only when it is Ollama or flagged `options.local` **and** its base URL resolves only to
loopback or private addresses. Even then, private records are included only when
`PUT /v1/settings/llm {"allowPrivateLocal": true}` (default false). This covers Ask, briefings and
alert enrichment; enrichment of an alert on a private record is skipped unless both conditions hold.

### Export and erase one entity

```http
GET /v1/records/custom-family/alex-phone/export?format=gpx       # or geojson, ndjson (admin)
POST /v1/records/custom-family/alex-phone/purge {"purgeAlerts": false}   # admin
POST /v1/custom-sources/custom-family/devices/{deviceId}/purge             # purge + revoke the key
```

- The export streams the current state, every retained version (with observers), the alerts that
  reference the entity and a manifest with counts and a SHA-256 of everything before it. GPX writes a
  `trk` for kind `tracker`.
- A purge writes a tombstone (qa flag `PURGED`, its change carries `purged: true` so SSE clients,
  `/v1/changes` readers and offline caches delete the entity), deletes every older version on both
  tiers, then deletes the entity's embeddings, redacts referencing alerts (`{"redacted": true}`; or
  deletes them with `purgeAlerts: true`), redacts stored notification bodies and drops its metric
  series. Ask messages and briefing runs that cite the entity are counted in the report, not edited.
  The head disappears with a later retention pass.
- **Messages already delivered to external channels (email, chat, push) cannot be recalled.**

### A note on data-protection law

Tracking your own and your household's devices on hardware you control is the kind of purely personal
or household activity that the EU GDPR (Article 2(2)(c)) and similar laws place outside their scope.
That exemption may not cover tracking people outside your household, or sharing the data further. This
is not legal advice: check the rules that apply to you, and get consent from the people you track.

## Retention

`GET /v1/settings/retention` shows every window; `PUT` replaces the whole object (every window at
least `1h`); `POST /v1/settings/retention/run` runs the job now and returns what it deleted. The job
also runs hourly, starting 5 minutes after boot.

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "provenance": "30d", "alerts": "90d", "usage": "30d",
 "notifications": "90d", "audit": "90d", "kinds": {"earthquake": "3650d"}, "sources": {"usgs-earthquakes": "1825d"},
 "stats": {"hourly": "35d", "daily": "1825d"}}
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
| Stats | Activity rollup rows: hourly after `stats.hourly` (default 35 days), daily after `stats.daily` (default 1825 days) |

**Current state is kept while it is current** (FR-17 as amended). Versions follow the window, but a
record that is still being observed keeps a version newer than a quarter of its window, so it never
disappears. A record purged after going quiet that comes back starts again at version 1.

**Durable consumers hold retention back.** The trigger engine's, the embedding worker's and the
activity rollup's cursors (and later the intelligence cursors) clamp every cutoff, so nothing is deleted before it was evaluated. If a cursor
falls more than two windows behind, retention stops waiting for it, counts
`foresignal.retention.consumer_lag` and sends a `system` notification; `/v1/system` then shows
`RETENTION_CONSUMER_LAG`.

**Clients.** A change-feed cursor below a purge gets `410 RESYNC_REQUIRED` (re-snapshot, then resume);
an SSE stream resumed below the retained floor gets `reset`. Routine version deletes never force a
resync.

### History downsampling

TRACK history (aircraft, vessels, other moving things) is kept at full detail for
`trackRawWindow` (24 hours by default). After that, the retention job thins one track partition per
hourly run, oldest first, until the partition reaches the end of `trackWindow` and is dropped.

**What is kept** in a thinned partition:

- every version that matters on its own: the first sighting, a reappearance, a significant change
  (callsign, squawk and the like), a tombstone and every trigger edge;
- the newest version of each entity in the partition;
- every version a current record or an alert points at;
- of the remaining positions, one per entity per `trackDownsampleInterval` (5 minutes by default).

So alerts, trigger edges and current records always resolve. What goes is the dense run of ordinary
position samples in between.

**Storage effect.** A thinned partition keeps at most one ordinary sample per entity per interval.
For an entity sampled once a minute at the default 5-minute interval, that is at most one ordinary
sample in five, plus the versions above. How much TRACK storage that saves depends on how many of
your versions are edges and keyframes. `/v1/system` `history.tiers[]` shows `downsampledLeaves` and
`downsampledRows`, and the `foresignal.history.downsample.*` metrics count the rows before and after.

**Reading thinned history.**

- `/versions` items inside a thinned partition say `downsampled: true`. Their `prevSeq` may name a
  version that downsampling removed.
- An asOf read over thinned history treats each surviving position as valid for
  `max(ttl, interval)`, so a stationary aircraft stays on the map between survivors. The answer says
  `approximate: true`.
- A trigger backtest (`POST /v1/triggers/{id}/test`) over thinned history warns `DOWNSAMPLED_RANGE`.
  Match-mode counts there are lower than they were live; edge rules are unaffected. Live triggers
  never see thinned data.

**Settings.**

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "trackRawWindow": "24h", "trackDownsampleInterval": "5m"}
```

- `trackRawWindow` must be shorter than `trackWindow`.
- `trackDownsampleInterval` must be between `30s` and `trackRawWindow`.
- A TRACK source can use its own interval: `PUT /v1/sources/{id}` with
  `{"downsampleInterval": "1m"}`, and `{"downsampleInterval": null}` to go back to the global one.
  A STANDARD source is refused with `400 VALIDATION_FAILED`, because its history is never thinned.

**How to disable it.** Set `"trackRawWindow": null`. Partitions that are already thinned stay as
they are until retention drops them; nothing new is thinned.

## Radar and satellite products

Prompt 046 adds the first two product sources. Like every source they start disabled; enable them
with `PUT /v1/sources/{id}` and `{"enabled": true}`.

| Source | Products | Upstream | Poll | Default budget |
|---|---|---|---|---|
| `mrms` | `mrms-reflectivity`, `mrms-precip-rate` (band 2 is the PrecipFlag type code), `mrms-mesh-60m` | NOAA MRMS, bucket `noaa-mrms-pds`, anonymous | 2 min, every frame | 30/min, 12,000/day, 4,000 MB/day |
| `goes-east-abi` | `goes-east-ir` (ABI band 13, clean IR) | GOES-19 CMIPC, bucket `noaa-goes19`, anonymous | 5 min | 10/min, 2,000/day, 2,500 MB/day |

Expect about 1.7–3.4 GB a day of downloads for `mrms` and 1.0–1.1 GB for `goes-east-abi`, and a
rolling disk of about 2 GB and 1.2 GB under the default retention ladders (MRMS: every frame for 6 h,
then 10-minute frames to 48 h; GOES: 5-minute frames for 24 h, then 30-minute frames to 72 h). The
`mbPerDay` budget is charged with each object's listed size **before** it is downloaded, so a day
whose byte budget is spent stops fetching rather than overspending.

**Health.** A source degrades when its newest frame is older than the product's `maxGap` (6 min for
MRMS, 15 min for GOES), and when an upstream file arrives without the field or variable Foresignal
expects ("field MergedReflectivityQCComposite missing at level 500 m", "variable CMI missing"): no
frame is written for such a file, because an empty frame would read as clear weather.

**The decoder self-test.** At startup Foresignal decodes four small bundled files, one per decoding
path: GRIB2 complex packing (GFS), GRIB2 PNG (MRMS), GRIB2 CCSDS/AEC (ECMWF, through the native
libaec library) and NetCDF4 (GOES). If a path fails, only the products that need it are disabled:
their tile, image and frame endpoints answer `503 PRODUCT_UNAVAILABLE`, the adapter skips them, and
the source's health shows `disabled: decoder self-test failed (…)`. The log line
`Decoder self-test failed for …` names the cause. The usual one is the native library: JNA extracts
libaec into `jna.tmpdir`, which the container sets to `/data/tmp` on the data volume. That directory
must allow executables. A named Docker volume does; a bind mount from a `noexec` filesystem does
not. Restart after fixing it. `foresignal.raster.decoder-self-test.enabled=false` turns the test off
(products are then never disabled by it).

**Files.** Downloads are staged in `${FORESIGNAL_DATA_DIR}/rasters/tmp/` and deleted after decoding.
NetCDF-Java's caches live in `${FORESIGNAL_DATA_DIR}/cache/netcdf/` (normally empty). Foresignal
never opens a NetCDF or GRIB file outside `rasters/tmp` and `rasters/selftest`.

### Building when the Unidata repository is down

NetCDF-Java is the one dependency not on Maven Central: Gradle fetches `edu.ucar` and
`edu.ucar.unidata` only from `https://artifacts.unidata.ucar.edu/all/` (Architecture §2). A build
host that has built Foresignal before already has the artifacts in its Gradle cache. To build
without the Unidata host:

1. Create `gradle/vendor-repo/` as a Maven repository layout and copy into it, from a machine that
   has them, every `edu.ucar` and `edu.ucar.unidata` jar and pom listed in
   `gradle/verification-metadata.xml` (for example from
   `~/.gradle/caches/modules-2/files-2.1/edu.ucar/`, renamed to
   `edu/ucar/<name>/<version>/<name>-<version>.{jar,pom}`).
2. In `build.gradle.kts`, replace `maven("https://artifacts.unidata.ucar.edu/all/") { name = "unidata" }`
   with `maven(uri("gradle/vendor-repo")) { name = "unidata" }`. Keep the name and the
   `exclusiveContent` filter: `checkUnidataScope` and the checksums in
   `gradle/verification-metadata.xml` keep applying, so a tampered or wrong file fails the build.
3. Build. Revert the line once the host is back; the vendored files need not be committed.

After a NetCDF-Java upgrade, regenerate the checksums with
`./gradlew --write-verification-metadata sha256 help`, then trim the file back to the `edu.ucar`
components and the `<trusted-artifacts>` rule.

## Static maps

`GET /v1/maps/static` (read scope; prompt 055) draws a PNG from the bundled Natural Earth 1:50m basemap and
Foresignal's own stored frames, so it works offline and **never fetches third-party tiles**. Give a box
(`bbox`), a `center` with `radiusKm`, an `alert` id (the map centers on its record geometry, radius default
50 km) or a `place` (a token is required; adds a marker and range rings), plus `products` (at most 4) with
optional `style`s, `time` (`latest`, `now`, an ISO time or an offset), `width` (≤ 1280, default 800),
`height` (default width × 0.75), `theme=light|dark` and `units=metric|imperial`. Every map shows each
product's frame valid time (UTC, plus the place's local time), a legend, the attribution, a scale bar and
the footer "Automated — not an official warning". A product without a usable frame is drawn as a note
("MRMS reflectivity unavailable — not evidence of clear weather"), never left out silently. The MCP tool
`get_map_image` returns the same map (≤ 512 px, ≤ 200 KB) with a text caption, and map attachments of
alerts use it too.

The response is immutable only when every frame is pinned (an ISO `time`, a versioned `style` per product,
a pinned `run` for forecasts, no `place`); otherwise it gets a short max-age and an ETag. Renders run on
the render pool within 2 s; under load the answer is `503 RENDER_BUSY` with `Retry-After: 1`.

**Your own tile server (optional).** `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, an XYZ template such as
`http://tiles.lan:8080/{z}/{x}/{y}.png`, replaces the Natural Earth fills with tiles from a server **on your
own network**: a host that does not resolve to a loopback or private address is refused (logged once per
map) and Natural Earth is used. Tiles are fetched only while a map is drawn (at most 16 per map, 0.8 s
each) and are never cached or passed on. Do **not** point it at `tile.openstreetmap.org`: the OpenStreetMap
tile usage policy forbids this kind of use, and Foresignal refuses public hosts anyway.

## Forecast models (GFS and HRRR)

Prompt 051 adds two forecast model sources. Both start disabled; enable them with
`PUT /v1/sources/{id}` and `{"enabled": true}`.

| Source | Products | Runs and steps | Poll | Default budget |
|---|---|---|---|---|
| `gfs` | `gfs` (10 m wind, u/v), `gfs-gust`, `gfs-t2m`, `gfs-apcp` (precipitation since the run started) | 4 runs a day; f000–f048 every 3 h, f054–f120 every 6 h | 15 min | 60/min, 3,000/day, 1,000 MB/day |
| `hrrr` | `hrrr` (10 m wind, u/v), `hrrr-refc`, `hrrr-gust`, `hrrr-smoke` | every 3 h, or every 6 h with `FORESIGNAL_PROFILE=small`; f00–f18 | 10 min | 60/min, 4,000/day, 2,000 MB/day |

Neither source downloads whole files (a GFS step is about 0.5 GB, an HRRR step about 150 MB). Each
step's `.idx` inventory names every message and its byte offset; Foresignal reads only the messages
it needs, with ranged requests, each charged to `mbPerDay` before it is sent. Expect about 0.4–0.5 GB
a day for `gfs`, and about 0.6 GB (small profile) or 1.1 GB (large) for `hrrr`, whose 10 m winds are
stored uncompressed upstream. At most 10 steps are read per poll, so a run completes over a few polls.
Disk: the current and the previous complete run are kept per product (about 29 steps × 4 products
for GFS), plus the GFS wind fields at 1° and 0.25° (about 6 MB compressed per step).

**What clients see.** A forecast product gets one `raster-frame` record version per **completed** run
(every configured step has a frame), not one per step; `{productId}:partial` reports a run in progress.
Tiles of a forecast frame need a run: a concrete time without `run=` answers `400 RUN_REQUIRED`
(`run=latest`, `previous` or the run time). The `gfs` product also serves God's Eye View wind fields
(`/v1/products/gfs/fields/…`) for web particle layers, and both wind products serve barb vector tiles
(`/v1/products/{id}/vectors/…`) and the `wind-speed@1` raster style. See the Frontend Guide.

**HRRR cadence by profile.** The small profile ingests every other HRRR run (00, 06, 12, 18z), which
halves the downloads; set the source's `runEvery` param to `3h` to ingest every run anyway, or `6h` on
a large host to save bandwidth. `runEvery` must be whole hours dividing 24.

**GFSv17.** NCEP plans GFS v17 (PNS 26-29/26-30). If its file layout changes, set the `gfs` source's
`pathTemplate` param instead of waiting for a release, for example
`{"params": {"pathTemplate": "gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}"}}` (the
default; tokens `{yyyyMMdd}`, `{HH}`, `{FFF}` three-digit step, `{FF}` two-digit step). Fields are
found by variable and level in the `.idx`, never by position, so new or reordered fields do not
matter. The `steps` param (for example `["0-48/3", "54-120/6"]`) and `availabilityLag` can be changed
the same way.

**Health.** A model source degrades when:
- its newest frame was ingested more than two runs ago (12 h for GFS, 6 h or 12 h for HRRR);
- the first `.idx` of the newest run is still missing 2 h after `availabilityLag` (3.5 h for GFS,
  50 min for HRRR): the reason names the missing object;
- an `.idx` lacks a field: "possible format change: GFS idx missing GUST:surface". No frame is written
  for that product (a missing wind field stops the whole step), so a format change never produces
  empty frames.

## Severe weather records

Prompt 047 adds four record sources (installed disabled, like every source):

| Source | What it brings | Notes |
|---|---|---|
| `nws-alerts` | NWS watches, warnings and advisories | Every 60 s. Param `area` (state or marine codes, empty = national) and `minSeverity`. The NWS asks for a contact in the User-Agent: set `contactEmail` in the server settings (`PUT /v1/settings/server`), which Foresignal appends as `contact=…`. |
| `mrms-probsevere` | MRMS ProbSevere v3 storm objects | Every 2 min, newest file only, inside the places area of interest (no places: the Texas box, with a health warning). Automated guidance, never a warning. |
| `spc-outlooks` | SPC day 1–3 convective outlooks | Every 15 min, 17 conditional requests, mostly 304. A missing layer shows the source `degraded` with "SPC layer {file} missing". |
| `awc-metars` | Airport METARs | Every 5 min (AWC allows 1 request per minute per thread). Param `scope` (`aoi`, `conus`, `global`) or a `stations` allowlist. A report stays current for 3 hours. |

```json
PUT /v1/settings/server
{"publicBaseUrl": null, "contactEmail": "ops@example.org"}
```

None of these sources ever turns a failed fetch into an all-clear: a record is tombstoned only when a
complete, successful response no longer contains it. When a feed is down its records stay, expire on
their own, and the source shows `degraded`. Rule recipes are in the Agent Guide, "Weather alerts", and for the
derived place weather below, "Weather alerts".

## Lightning and fire

Prompt 048 adds four more sources (installed disabled):

| Source | What it brings | Notes |
|---|---|---|
| `goes-glm` | GOES-19 GLM lightning | Every 30 s, every 20-second granule (about 1.1–1.9 GB a day). Flashes go to the flash store, not to records: a daily archive under `${FORESIGNAL_DATA_DIR}/rasters/glm/G19/{yyyymmdd}.bin` (16 bytes per flash, about 28 MB a day, kept 14 days, counted toward `FORESIGNAL_RASTER_DISK_QUOTA_GB`) and a 60-minute in-memory ring of the flashes inside the places area. Records are `lightning-cluster`. |
| `goes-fdc` | GOES-19 fire detections | Every 5 min, newest scan only. Param `scope` (`aoi` default, or `conus`). Records `fire-detection` (one per pixel) and `fire-cluster`. |
| `hms-smoke` | NOAA HMS analyst smoke polygons | Every 30 min, conditional requests; today's file, and yesterday's until 06:00 UTC. A 404 before the first analysis of the day is normal. |
| `hms-fire` | NOAA HMS analyst fire points | Same schedule and files as `hms-smoke`; records share `fire-detection` with `goes-fdc`. |

**The GLM ring scope.** `foresignal.raster.glm-ring-scope` (`FORESIGNAL_RASTER_GLM_RING_SCOPE`) is `aoi`
by default: the ring keeps the flashes inside the union of your places' buffers (no places: the
`weather.defaultAoi` box). The large profile (`FORESIGNAL_PROFILE=large`) may set `conus`; the small
profile logs a warning and stays on `aoi`. The archive always keeps the full field of view, and the ring
is rebuilt from it at startup and whenever a place changes. `GET /v1/system` reports
`rasters.glm{ringFlashes, archiveBytes, newestFileAt}`.

**Stale is not clear.** When the GLM or FDC feed stops, clusters and detections freeze: nothing is
tombstoned and no `exit` rule fires. The source shows `degraded` with "newest input file is …s old" once
the newest processed file is older than 2 minutes (GLM) or 15 minutes (FDC). An unreadable file or a
missing NetCDF variable also shows `degraded` with the reason, and the file is skipped. Rule recipes are
in the Agent Guide, "Lightning and fire".

## Weather alerts

Prompt 052 adds three **derived** sources (installed disabled). They make no upstream request: they read
the frames and records the weather sources already stored, and write a record only when its rounded
content changes.

| Source | Kind | What a record is |
|---|---|---|
| `wx-places` | `place-weather` | one per place (`place:{id}`): radar, hail, rain rate, lightning, storm-cell ETA, smoke and GFS forecast sections, re-evaluated on every MRMS frame and every 60 s |
| `wx-areas` | `wx-area` | one per configured area (`refl:50:aoi`, `mesh:25:home`): the area above a threshold inside the places AOI or a place, every 6–10 min |
| `wx-cells` | `radar-cell` | one per tracked storm cell (`c-{ulid}`): maximum reflectivity, area, hail size, GLM flashes and centroid motion |

Enable the inputs you want first (`mrms`, `goes-glm`, `gfs`, `hrrr`), then the derived sources:
`PUT /v1/sources/wx-places {"enabled": true}` and the same for `wx-cells` (the `cell` section needs it)
and `wx-areas`. `wx-areas` takes `areas` (a list of `field:threshold:aoi`, field `refl`, `mesh`, `precip`,
`rotation` (s⁻¹), `qpe1h` (mm) or `ffg` (% of flash-flood guidance))
and `interval` (6–10 min); `wx-cells` takes `scope` (`aoi`, or `conus` on the large profile). Each derived
source shows in `GET /v1/sources/{id}/health` like any other: every invocation (a frame, a change batch, a
tick) writes one `fetch_log` row with `principal: derived`.

**Unknown is not clear.** Every section has a `status`: `ok`, `stale` (MRMS older than 6 min, GLM older
than 2 min, model runs older than 12 h), `no-coverage` or `unavailable` (input disabled, failing or never
seen). A section that is not `ok` keeps its last values and adds `asOf`, the time of the last good data;
before any good data it carries only its status. "None within 100 km" is `999`, never null. So a stopped
feed can never look like clear weather, and a derived record is never tombstoned because its input is
stale; it is tombstoned only when its object disappears (a storm cell no longer present in a processed
frame) or its place is deleted. **Deleting a place tombstones its `place-weather` record, which fires
`exit` rules whose condition held** (a tombstone after a matching version is an exit).

**GLM limits.** GLM's footprint over Texas is about 8–10 km and it detects less than every flash. GLM-only
rules should use 15 km or more, and a lightning all-clear is advisory.

**Write derived rules with `enter` and `exit`** (or an explicit cooldown), never a bare `match`: a
place-weather record versions whenever any section changes, and `match` would fire on each version.
Conditions are AND-only, so all-clear rules use the **verified-clear helpers**:
`radar.verifiedNearest50Km10m` and `lightning.verifiedNearestKm30m` equal the metric while the section is
`ok` and `0` otherwise. A condition `<= X` on them is therefore true when a storm is near **or** the data is
unknown, and its `exit` fires only when the data is known **and** far.

**≥ 50 dBZ within 20 km:**

```json
{"name": "Heavy echoes near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "radar.nearest50Km10m", "op": "<=", "value": 20}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "≥ 50 dBZ within {{payload.radar.nearest50Km10m}} km of {{payload.place}} (automated, not a warning)"}
```

**Echoes cleared (verified only).** P is "near or unknown"; `exit` fires when the previous version was near
or unknown and the current one is `ok` and far:

```json
{"name": "Echoes cleared at home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "radar.verifiedNearest50Km10m", "op": "<=", "value": 20}],
 "mode": "exit", "severity": "info",
 "messageTemplate": "No ≥ 50 dBZ within 20 km of {{payload.place}} for 10 min (verified radar)"}
```

**Lightning within 15 km, with an advisory all-clear:**

```json
{"name": "Lightning near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "lightning.nearestKm15m", "op": "<=", "value": 15}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "Lightning {{payload.lightning.nearestKm15m}} km from {{payload.place}} (GLM, automated)"}
```

```json
{"name": "Lightning all-clear at home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "lightning.verifiedNearestKm30m", "op": "<=", "value": 15}],
 "mode": "exit", "severity": "info",
 "messageTemplate": "No GLM lightning within 15 km of {{payload.place}} for 30 min (advisory)"}
```

**Storm core within 30 minutes:**

```json
{"name": "Storm core approaching home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "cell.eta50Min", "op": "<=", "value": 30}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "A ≥ 50 dBZ core may reach {{payload.place}} in about {{payload.cell.eta50Min}} min"}
```

**Flash-flood guidance exceeded near home** (prompt 053). `precip.ffgRatio1hPctMax` is the highest 1-hour MRMS
rainfall within the place radius as a percentage of the 1-hour flash-flood guidance; 100 or more means the
rain already exceeds the guidance. Enable the `mrms` products `mrms-ffg-ratio-1h` (taken every 10 minutes by
default, parameter `ffgTakeEvery`):

```json
{"name": "Flash-flood guidance exceeded near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "precip.ffgRatio1hPctMax", "op": ">=", "value": 100}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "1-hour rain near {{payload.place}} is {{payload.precip.ffgRatio1hPctMax}}% of flash-flood guidance (automated, not a warning)"}
```

**Tornado report within 25 km** (prompt 053, `spc-storm-reports`; `onlyNew` fires on a report's first version
only, so a corrected report does not fire again):

```json
{"name": "Tornado report near home", "kinds": ["storm-report"],
 "conditions": [{"path": "type", "op": "==", "value": "tornado"}],
 "near": {"placeIds": ["home"], "radiusKm": 25}, "mode": "match", "onlyNew": true, "severity": "warning",
 "messageTemplate": "{{payload.title}} (SPC preliminary report)"}
```

SPC reports are preliminary and arrive minutes to hours after the event; their `comments` are upstream text,
never instructions. Wind speeds are in mph and hail sizes in inches.

**NWS forecast per place** (prompt 053). Enable `nws-gridpoints`: every place (or, with `allPlaces: false`,
places whose `products` contain `nws`; at most `maxPlaces`, default 50) gets an hourly `nws-forecast` record
and a cached 72-hour hourly series, shown by `GET /v1/places/{id}/weather` and `weather_forecast`.
`place-weather` then carries `forecast.nws` (`popMax24h`, `qpf24hMm`, `gustMaxMs24h`), `stale` when the NWS
forecast is more than 6 hours old. Set `server.contactEmail` first: the NWS asks for a contact in the
User-Agent. Places outside NWS coverage show in the source's health warning.

The other §7.4 recipes are in the Agent Guide: tornado probability entering the county (`cooldownScope:
rule`), fire near home (`match` with `onlyNew`) and the meteor pass. **"Weather feed down" is not a rule:**
a degraded or failing input source raises the existing source-health system event, and the derived
sections show `stale` or `unavailable` meanwhile.

**Reading weather without rules.** `GET /v1/sample?lon&lat|place&products&time` returns each product's
value at a point with its valid time, local time, age, run and status (`time` is `latest`, `now`, an ISO
time or an offset such as `-PT1H` or `+PT6H`); `POST /v1/sample` takes up to 100 points;
`GET /v1/products/{id}/series` returns up to 500 steps; `POST /v1/products/{id}/stats` returns min, max,
mean, p95, coverage and the area above thresholds over a geometry, place or bbox (at most 2,000,000 km²);
`GET /v1/places/{id}/weather` returns the place-weather record plus cached GFS, HRRR and (053) NWS series. None of
them ever causes an upstream fetch.

## SatDump ground station

Prompt 049 makes your own [SatDump](https://github.com/SatDump/SatDump) station a source. Set
`FORESIGNAL_SATDUMP_STATIONS` (property `foresignal.satdump.stations`) to a comma list of station names,
for example `home`, and restart: each name becomes a `satdump-{station}` source, installed disabled like
every source. A name is kebab-case and at most 56 characters.

SatDump is GPL-3.0 and stays a separate program that you install and run. Foresignal talks to it only
through HTTP (`/api`, `/polarplot.jpeg`) and the files it writes; no SatDump code is in Foresignal, and the
Foresignal image contains no SatDump binaries or SatDump-produced images.

### Station requirements (§4.0)

- **`/api` needs the headless legacy CLI.** Only `satdump legacy live …` and `satdump legacy autotrack …`
  serve it. In `live` mode pass `--http_server 0.0.0.0:8081` on the command line; in `autotrack` mode the
  web server and the other options come from the config file:
  `satdump legacy autotrack autotrack.json` with `"http_server": "0.0.0.0:8081"` in it. A station run from
  the GUI works in **products-only mode** (inbox only, no RF statistics).
- **`finish_processing: true`, and no server mode.** SatDump runs the offline pipeline (and writes
  `dataset.json`, the products and the composites) only when `finish_processing` is true and no
  `server_address`/`server_port` is set. Without it a pass is recorded but never decoded, and the source
  shows "Pass ended {t} but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode
  off?".
- **Pin a SatDump version.** The `/api` shape and the `product.cbor` keys differ between versions (1.2.2
  and 2.0.0-alpha differ). Foresignal keeps one fixture set per pinned version; a response that no longer
  matches shows "SatDump /api shape changed (version drift?)".
- Foresignal never requests `/fft.jpeg` (requesting it turns SatDump's FFT on) or `/schedule.jpeg`.

### Parameters

| Param | Type | Default | Meaning |
|---|---|---|---|
| `apiUrl` | LAN URL | — | SatDump's web server, for example `http://192.168.1.60:8081`. Local addresses only; resolved once and pinned. |
| `inboxPath` | inbox path | — | SatDump's output directory under `FORESIGNAL_INBOX_DIR`, for example `satdump/home`. |
| `location` | string | required | `lat,lon[,altM]` or a place id. |
| `satNames` | JSON object | — | Name or NORAD number to `satKey`, overriding the built-in table (`src/main/resources/satdump/sat-names.json`). |
| `apiInterval` | duration | `5s` | 2 s to 30 s. |
| `scanInterval` | duration | `30s` | 5 s to 10 min. Each full scan costs one budget unit. |
| `polarGlob` | glob | `**/*.tif` | Which files of a ready polar pass folder are read as GeoTIFF overlays, relative to the pass folder (`**` crosses directories, `*` and `?` do not; case is ignored). |

Credential (optional, `PUT /v1/sources/satdump-home/credentials`): `ingestKey`, a key you choose
(`eik_` followed by at least 32 random characters) that a remote station presents on the push endpoints
below. Without it only an admin token may push.

At least one of `apiUrl` and `inboxPath` is required (`400 VALIDATION_FAILED` otherwise). The budget is
60 a minute and 50,000 a day; every request stays on your LAN.

```json
PUT /v1/sources/satdump-home
{"enabled": true, "params": {"apiUrl": "http://192.168.1.60:8081", "inboxPath": "satdump/home", "location": "home"}}
```

### What it produces

- **`ground-station`** (`externalId` = the station name, kept 30 days): `state`
  (`idle | tracking | receiving | locked | unknown`), `currentTarget`, `frequencyHz`, `az`, `el`, `snrDb`,
  `nextAos`, `nextLos`, `rotatorEngaged`, `satdumpMode`. Azimuth and elevation are rounded to 1° and SNR to
  1 dB, so a version is written only when something changes. Without `apiUrl` there is one record in state
  `unknown`.
- **`satellite-pass`** (kept 365 days): `receiving` at AOS, `recorded` at LOS with the RF statistics
  (`rf.snrMaxDb`, `snrMeanDb`, `berMin`, `berMean`, `syncedFraction`, lock times and elevations, and an
  SNR-against-elevation series), then `decoded` when the pass folder's products arrive, or `failed` when
  none arrived 20 minutes after LOS although the station had lock. Each composite is stored as media with
  256 and 1024 px thumbnails (`products[].thumbMediaId`). The id is `{station}:{satKey}:{aosEpochMinute}`.
- **Geostationary frames**: GOES-19/18 HRIT and Elektro-L xRIT folders become frames of dynamic products
  `satdump-{station}-{satKey}-{region}-{channel}` (region `fd` when SatDump writes none), kept 72 hours and
  rendered through the normal tile path. A frame is `partial` as soon as one channel arrives and becomes
  complete after 120 s without changes or when the next timestamp folder appears. GK-2A needs KMA keys
  obtained legitimately. FY-4 and MSG HRV are refused ("FY-4/MSG HRV has no usable projection.").
- **Live polar plot** during an active autotrack pass: `GET /v1/ground-stations/satdump-home/polarplot.jpeg`
  (read scope; `404 NO_ACTIVE_PASS` otherwise). Fetched every 15 s by the source, held in memory only,
  never fetched because a client asked.

- **Polar overlays** (prompt 054): every file matching `polarGlob` in a ready pass folder is read as a
  GeoTIFF map overlay; see the next section.

The original images and the provenance files (`dataset.json`, `product.cbor`, any `.tif`) are copied to
`${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/{passId}/` and deleted after 30 days. `.cadu` and baseband
files are never copied; SatDump's own retention keeps them.

**Health.** The source is `failing` when `apiUrl` is set and `/api` has been unreachable for 3 polls in a
row, and `degraded` for the reasons above and the overlay reasons below. With no `apiUrl` it stays healthy
on the inbox loop alone.

### Polar overlays and footprints (prompt 054)

Foresignal only **reads** GeoTIFFs that SatDump wrote into a pass folder; it never reimplements SatDump's
projection, and no SatDump code is involved (SatDump's source was read for file-format facts only).

**The one producer configuration.** No SatDump version has been confirmed for your station yet
(Weather-Imagery-Design §1.5, question 1), so the documented producer is the default the design names:
**SatDump 1.2.2**, with a `project` block on each composite you want as an overlay, in the pipeline's
composite settings:

```json
"project": {"config": {"type": "equirec", "auto": true}, "img_format": ".tif", "draw_map_overlay": false}
```

SatDump 1.2.2 then writes `rgb_{composite}_projected.tif` next to the composite: an EPSG:4326 GeoTIFF
(one tie point, a pixel scale of about 0.016°, `GeographicTypeGeoKey` 4326). `"auto": true` is needed:
without it, or an explicit `width` and `height`, 1.2.2 logs "No width or height defined for projection!"
and writes nothing. **SatDump 2.0-alpha ignores a preset's `project` keys**, so a 2.0 station gets
thumbnails and approximate swaths only, until the 2.0 hook of prompt 107.

**What happens to each GeoTIFF:**

1. The header is checked first: a BigTIFF, more than 150 megapixels or a side over 12,000 px is refused
   before any pixel is decoded.
2. Only EPSG:4326 and EPSG:3857 are read (PixelIsArea and PixelIsPoint both; a rotated
   ModelTransformation is refused); 8-bit RGB/RGBA and 16-bit grayscale. A file without alpha is
   transparent where every band is zero (or equals `GDAL_NODATA`).
3. It becomes one frame of the dynamic product `satdump-{station}-{composite}` (the file stem without
   `_projected`): `rgba8`, `temporal.type` `pass`, coverage `swath`, style `rgb@1`, kept 30 days, tiled
   like every other product. Processing the same pass again replaces its frame.
4. Its footprint is the outline of the non-transparent pixels, simplified to about 0.02° and split at
   ±180°. The `satellite-pass` record's geometry becomes that footprint (`geometrySource: footprint`),
   and its `products[]` entry gains `productId`, `frameKey` and `tilesTemplate`.

**Approximate swath.** A pass without a usable GeoTIFF (a 2.0 station, a refused file, a failed decode) gets
an approximate swath: the SGP4 ground track (Orekit) from the TLE in `product.cbor`, between the first and
last product timestamps, widened by half the swath on each side (`corr_swath` from the product, else
MSU-MR 2,800 km, AVHRR 2,900 km, MHS 2,200 km). It is a geometry only (`geometrySource: swath`), never
tiles, and never replaces a footprint. Without a TLE the pass keeps the station point.

**Health reasons** (the source shows `degraded` until a later pass is clean):

| Reason | Fix |
|---|---|
| `GeoTIFF CRS EPSG:{n} not supported (build with WITH_GDAL=true)` | Use `"type": "equirec"` (or WebMercator) in the composite's `project`, or build with GDAL |
| `GeoTIFF ModelTransformation is rotated or sheared (not supported; build with WITH_GDAL=true)` | As above |
| `BigTIFF not supported.` | Keep overlays under 4 GB, or build with GDAL |
| `GeoTIFF … px has a side over the 12000 px limit` / `… over the 150 megapixel limit` | Raise `scale_x`/`scale_y` in the projection |

**Optional GDAL.** Build the image with `docker compose build --build-arg WITH_GDAL=true` (or
`WITH_GDAL=true docker compose build`): it installs `gdal-bin` only (no JNI, no AGPL drivers). Foresignal
detects `gdalwarp --version` at startup (`FORESIGNAL_RASTER_GDAL_ENABLED`, default `auto`; `false` turns it
off) and warps refused GeoTIFFs (another CRS, a rotation, BigTIFF) with
`gdalwarp -t_srs EPSG:4326 -of GTiff -co COMPRESS=DEFLATE`, a 120-second timeout and `GDAL_CACHEMAX=256`,
on the raster decode pool, with its files only under `rasters/tmp/`.

### Same host or remote Pi (§4.7)

- **Same host:** `docker compose --profile satdump up -d` runs a `satdump` service with
  `network_mode: host`, `/dev/bus/usb` passed through and the output volume `satdump-output`, which the
  broker mounts read-only at `/data/inbox/satdump/${SATDUMP_STATION:-home}`. Build the image yourself from
  SatDump's own Dockerfile and pin the commit:
  `git clone https://github.com/SatDump/SatDump && git -C SatDump checkout <commit> && docker build -t satdump:local SatDump`
  (or set `SATDUMP_IMAGE`). Write `docker/satdump/autotrack.json` with `"http_server": "0.0.0.0:8081"`,
  `"finish_processing": true` and `"output_folder": "/srv/output"`.
- **Remote Pi at the antenna (recommended in v1):** run SatDump on the Pi and copy its output directory into
  `${FORESIGNAL_INBOX_DIR}/satdump/{station}` with rsync or Syncthing; the 30-second rescan picks it up
  (inotify is not trusted, so a sync tool that renames into place works). Point `apiUrl` at the Pi if it
  is on your LAN.
- **Remote Pi that pushes (prompt 054):** when a sync tool is not an option, the Pi can push with the
  source's `ingestKey` credential (`X-Foresignal-Ingest-Key`, `Authorization: Bearer` or Basic) or an
  admin token:
  - `POST /v1/ingest/satdump-home/rasters`: a whole pass folder as `multipart/form-data`, a `meta` part
    `{"passFolder": "…", "files": ["dataset.json", "product.cbor", "…png", "…tif"], "product": "MSU-MR"}`
    and one `files` part per file. Each file at most 25 MB, the bundle at most 200 MB (`413
    MEDIA_TOO_LARGE`); file and folder names are single path segments. `dataset.json` goes to the pass
    folder and the rest to the `product` directory (default: the single product `dataset.json` lists).
    The bundle is written atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/home/incoming/{uploadId}/`
    and processed by the next inbox scan exactly like a pass folder; it is deleted after 2 days. Send one
    bundle per product for a multi-instrument pass. Answers `202 {uploadId, passFolder}`.
  - `POST /v1/ingest/satdump-home/media`: one image (`file`, at most 25 MB) and
    `meta {"composite", "timestamp", "satellite", "passId"?}`; it is checked by the image guard, stored
    with thumbnails and attached to the matching pass (or a new one). Answers
    `202 {mediaId, sha256, passId}`; `404` while the source's stream is not running.

  Each push costs one unit of the source's budget (`429 RATE_LIMITED` with `Retry-After` when spent).
  Other sources answer both paths with `404`.

  ```sh
  curl -H "X-Foresignal-Ingest-Key: $KEY" -F 'meta={"passFolder":"2026-10-12_18-02_meteor","files":["dataset.json","product.cbor","rgb_MSU-MR_221_Composite_projected.tif"],"product":"MSU-MR"};type=application/json' \
       -F files=@dataset.json -F files=@MSU-MR/product.cbor -F files=@MSU-MR/rgb_MSU-MR_221_Composite_projected.tif \
       https://foresignal.example/v1/ingest/satdump-home/rasters
  ```

**Do not use SatDump's webhook plugin** as an ingest path: it posts plain text only.

### Texas targets

| Target | Frequency | Hardware |
|---|---|---|
| GOES-19 HRIT/EMWIN | 1694.1 MHz | about 1 m dish, LNA, LTE filter |
| Meteor-M2-3 / M2-4 LRPT | 137.9 / 137.1 MHz | QFH or V-dipole |
| Metop-B/C AHRPT (optional) | 1701.3 MHz | tracking dish |

Drop `noaa_apt`, `noaa_dsb` and `noaa_hrpt` from SatDump's defaults: NOAA-15, -18 and -19 were
decommissioned in 2025.

### Open owner questions (Weather-Imagery-Design §1.5) and the defaults used

| Question | Default until answered |
|---|---|
| SatDump version and mode at the station | 2.0.0-alpha headless `legacy autotrack` for `/api` and the inbox; fixtures are hand-written from that version's observed keys until a capture replaces them. The polar overlay producer is documented for 1.2.2 (054, the design's default when unanswered); a 2.0 station gets approximate swaths only |
| Bench server or remote Pi | Both work; the remote Pi with rsync/Syncthing is the documented default |
| L-band dish for GOES HRIT, 137 MHz antenna | Both decoders are built; whatever arrives is used |
| 32 GB or 64 GB host | `small` profile |
| Clients outside the LAN | Tile keys and the `web` profile as documented; nothing SatDump-specific |
| Home location and places | `location` names a place when one exists, otherwise coordinates |

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

**For map clients: the `web` profile.** Map clients fetch many tiles at once and hold SSE streams open.
Over plain HTTP/1.1 a browser allows about six connections per host, so tiles queue behind each other
and every open stream costs a connection. The `web` profile puts Caddy in front of the broker with
HTTP/2 and HTTP/3 over TLS:

```sh
FORESIGNAL_HOSTNAME=foresignal.local \
FORESIGNAL_TRUSTED_PROXIES='172\.(1[6-9]|2[0-9]|3[01])\.\d{1,3}\.\d{1,3}' \
  docker compose --profile web up -d
```

- The `caddy` service (pinned `caddy:2.11.7-alpine`) publishes `80`, `443/tcp` and `443/udp` (HTTP/3)
  on every interface, and loads `deploy/caddy/Caddyfile`. Its state lives in the `caddy-data` and
  `caddy-config` volumes.
- `FORESIGNAL_HOSTNAME` is the site name (default `foresignal.local`); it must resolve to the host
  through mDNS, your DNS or the clients' hosts files.
- **Certificates:** `tls internal` by default: Caddy runs its own local certificate authority and
  issues the site certificate from it. Clients must trust that authority's root (below). For a
  publicly trusted certificate without opening port 80, use DNS-01: build a Caddy binary with your DNS
  provider's module (`xcaddy build --with github.com/caddy-dns/cloudflare`, or a Dockerfile `FROM
  caddy:2.11.7-builder-alpine` running that command), use that image, and replace `tls internal`
  with the commented `tls { dns … }` block in the Caddyfile. The stock image has no DNS modules.
- **Routes:** `/v1/*`, `/mcp*` and `/actuator/health*` go to the broker (with `flush_interval -1` for
  SSE); everything else is served from `./deploy/web` with a single-page-app fallback. Copy a separately
  built web client there: it is then same-origin with the API, so it makes no CORS preflights.
- **Compression:** zstd or gzip for JSON, vector tiles and text; PNG tiles and SSE are never
  re-encoded.
- **Trusted proxies:** Caddy reaches the broker from the compose network, so set
  `FORESIGNAL_TRUSTED_PROXIES` to that network as above (check it with
  `docker network inspect`; the compose network is named after the project, for example
  `foresignal_default`). Otherwise every client appears as Caddy's address in
  the audit log, the rate limits and the auth-failure backoff.
- Set `publicBaseUrl` to `https://<FORESIGNAL_HOSTNAME>` so feed-key and tile-key URLs, notification
  links and pairing codes use the HTTPS name.
- Plain `http://<host>:8080` keeps working for agents, scripts and tools.
- Caddy's access log (container stdout) keeps only the first eight characters of a path key and
  removes `?ticket=` values.

**Trusting Caddy's local CA.** Export the root certificate once:

```sh
docker compose cp caddy:/data/caddy/pki/authorities/local/root.crt ./foresignal-root.crt
```

Then install `foresignal-root.crt` on each client:

- **macOS:** open it in Keychain Access, add it to the System keychain, open the certificate, and set
  "When using this certificate" to **Always Trust** (or
  `sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain foresignal-root.crt`).
- **Windows:** `certutil -addstore -f Root foresignal-root.crt` from an administrator prompt, or
  double-click it and import it into **Trusted Root Certification Authorities** for the local machine.
- **Linux:** Debian and Ubuntu: copy it to `/usr/local/share/ca-certificates/foresignal-root.crt` and
  run `sudo update-ca-certificates`; Fedora: copy it to `/etc/pki/ca-trust/source/anchors/` and run
  `sudo update-ca-trust`. Firefox keeps its own store: import it under Settings → Privacy & Security →
  Certificates, or enable `security.enterprise_roots.enabled`.
- **Android:** Settings → Security → Encryption & credentials → Install a certificate → CA certificate.
  Browsers then trust it, but **apps targeting API level 24 or later do not trust user-installed CAs**
  unless their `network_security_config` allows them. A Flutter client that talks to the `web` profile
  must ship that configuration, for example a `res/xml/network_security_config.xml` with
  `<trust-anchors><certificates src="system"/><certificates src="user"/></trust-anchors>`, referenced
  from `AndroidManifest.xml` with `android:networkSecurityConfig`.
- **iOS and iPadOS:** send the file to the device (AirDrop, Mail or a download), install the profile under
  Settings → General → VPN & Device Management, then enable full trust under Settings → General →
  About → Certificate Trust Settings. Apps then trust it too.

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
