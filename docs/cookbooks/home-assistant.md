# Cookbook: Home Assistant

Show Foresignal alerts and records in Home Assistant, act on them from automations, and send Home
Assistant states into Foresignal. Everything here uses existing Foresignal APIs; the importable
package is [`integrations/home-assistant/foresignal_package.yaml`](../../integrations/home-assistant/foresignal_package.yaml).

**Token scope:** a **read** token on the hub (sensors), an **ingest key** bound to one custom source
(Home Assistant → Foresignal), and the one-time **capability URLs** in Foresignal's webhook body for
acknowledging. No agent or admin token is stored in Home Assistant. The admin token is used once, on
your workstation, to create the channel, the custom source and the read token.

**URLs used:** `/v1/records`, `/v1/notifications/badge`, `/v1/alerts`, `/v1/sources/health`,
`/v1/ingest/{sourceId}`, `/v1/actions/{actionToken}` (capability URLs), and for setup
`/v1/channels`, `/v1/custom-sources`, `/v1/tokens`, `/v1/sources/{sourceId}`,
`/v1/settings/server`. Home Assistant's own: `/api/webhook/<webhook_id>`.

Replace `http://foresignal.local:8080` with your broker and `http://homeassistant.local:8123` with
your Home Assistant everywhere below.

## 1. Foresignal → Home Assistant: alerts as automations

Foresignal's notification channels can post every alert to a Home Assistant automation with a
`webhook` trigger. Two channel types work:

| Channel | Body | Capability URLs (`actions`) |
|---|---|---|
| `homeassistant`, mode `webhook` (default) | The Foresignal JSON body | No: this type does not add an `actions` array |
| `webhook` pointed at `/api/webhook/<id>` | The same body | Yes, with `config.actions.enabled: true` |

Use the generic `webhook` type when Home Assistant should acknowledge alerts. Create it with the
admin token:

```sh
curl -fsS -X POST http://foresignal.local:8080/v1/channels \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{
    "name": "Home Assistant", "type": "webhook", "events": ["alert"], "minSeverity": "warning",
    "config": {"url": "http://homeassistant.local:8123/api/webhook/a-long-random-unguessable-id",
               "actions": {"enabled": true, "list": ["ack", "snooze"]}}}'
```

or, without acknowledgements, `{"type": "homeassistant", "config": {"baseUrl":
"http://homeassistant.local:8123"}, "secrets": {"webhookId": "a-long-random-unguessable-id"}}`.

The package's automation `foresignal_alert_webhook` listens on that id:

```yaml
triggers:
  - trigger: webhook
    webhook_id: !secret foresignal_webhook_id
    allowed_methods: [POST]
    local_only: true
```

- **Home Assistant cannot verify Foresignal's HMAC signature.** Its webhook trigger has no hook for
  it. The only protection is the unguessable `webhook_id` (anyone who knows it can trigger the
  automation) and `local_only: true` (only devices on the local network). Do not set an
  `hmacSecret` expecting it to be checked, keep the id out of logs and screenshots, and never expose
  Home Assistant's webhook endpoint to the internet for this.
- The body arrives as `trigger.json`: `subtype` (`alert.fired`, then `alert.acknowledged`,
  `alert.resolved`, `alert.snoozed` as follow-ups), `message.title`, `message.bodyMarkdown`,
  `message.severity`, `data.alertId`, `data.sourceId`, `data.externalId`, `data.tags` and, with
  actions enabled, `actions: [{action, label, url, expiresAt}]`.
- The automation creates a persistent notification and stores the `ack` URL in
  `input_text.foresignal_ack_url`; the script `foresignal_acknowledge_last` POSTs it through
  `rest_command.foresignal_action`. A capability works once, expires (12 h by default, at most
  24 h), and is withdrawn as soon as the alert is acknowledged or resolved by any path.
- Capability URLs are built from the server setting `publicBaseUrl` (`PUT /v1/settings/server`).
  Set it to an address Home Assistant can reach, or the URLs point at `http://localhost:8080`.

## 2. Records on the Home Assistant map: `geo_json_events`

The [GeoJSON integration](https://www.home-assistant.io/integrations/geo_json_events/) shows the
features of a GeoJSON feed as geo-location entities near your home. Add it in **Settings → Devices &
services → Add integration → GeoJSON** (it is configured in the UI only) with:

```
http://foresignal.local:8080/v1/records?format=geojson&kind=earthquake&bbox=-100.5,32.0,-91.5,40.0&limit=200
```

and a radius around your home.

- **Entity names come from `properties.title`.** Home Assistant uses a top-level `name` property
  when a feature has one and otherwise the `title`; Foresignal features carry `title` (the source's
  title field) and keep the record's own fields under `properties.payload`, so the title is used.
- **Polling:** every 5 minutes. Home Assistant reads one page and ignores `nextCursor`, so the
  `limit` decides how many records it can see; keep the `bbox` tight around your area rather than
  raising the limit.
- **Records without geometry produce no entity.** The integration is meant for point features; use
  kinds whose records are points (earthquakes, aircraft, vessels).
- **No headers.** The integration cannot send a bearer token, so the URL works only while
  `security.openReads` is on (the default), which also opens every other read on the network to
  anyone. Prompt 035 adds feed-key URLs that carry a narrow, revocable read credential in the path;
  this section will move to them.

## 3. RESTful sensors (read token)

The package defines three `rest:` resources, each with `Authorization: !secret foresignal_read_auth`:

| Sensor | Resource | State | Attributes |
|---|---|---|---|
| `sensor.foresignal_unread` | `/v1/notifications/badge` | unread notifications | `unreadCritical`, `latestId` |
| `sensor.foresignal_firing_alerts` | `/v1/alerts?state=firing&limit=5` | how many (≤ 5) | `items` (the alerts) |
| `sensor.foresignal_failing_sources`, `…_ids` | `/v1/sources/health?status=failing` | count, and the ids | |

- `/v1/notifications/badge` is a per-device inbox count and always needs a token. Send
  `Foresignal-Device-Id: <a UUID>` (one fixed UUID per hub, in `secrets.yaml`), or the call answers
  `400 DEVICE_ID_REQUIRED`. Reading never marks anything read.
- The secrets entry must include the word `Bearer`: `foresignal_read_auth: "Bearer fsr_…"`.
- Use `value_json['items']`, not `value_json.items`: in a template, `.items` is the dictionary
  method.

## 4. Home Assistant → Foresignal: states into a custom source

Create a custom source once with the admin token. The response's `ingestKey` (`eik_…`) is shown
once and can only write to this source:

```sh
curl -fsS -X POST http://foresignal.local:8080/v1/custom-sources \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{
    "id": "custom-home", "name": "Home Assistant states", "kinds": ["ha-state"],
    "private": true, "titleField": "name", "observedAtMode": "UPSTREAM"}'
curl -fsS -X PUT http://foresignal.local:8080/v1/sources/custom-home \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{"retention": "30d"}'
```

`rest_command.foresignal_ingest` POSTs `{"records": [{externalId, kind, observedAt, payload}]}` to
`/v1/ingest/custom-home` with `Authorization: Bearer eik_…` (the ingest key, from `secrets.yaml`).
The example automation pushes `binary_sensor.front_door` on every change; copy it for other
entities. A watch on `sources: ["custom-home"]` can then alert on home states like any other data.

## Limits

- `geo_json_events`: one page, every 5 minutes, needs `openReads` until feed keys exist (035).
- REST sensors poll; keep `scan_interval` at 60 s or more. Foresignal's alert stream
  (`/v1/alerts/stream`) is not usable from Home Assistant's REST integrations.
- Webhook automations are unauthenticated beyond the id; capability URLs expire after 12 h.
- Ingest: 5 MB and 10,000 records per request, `recordsPerMinute` per source (default 6,000).
- Home Assistant's MCP client cannot connect to Foresignal; see
  [`integrations/mcp-clients/home-assistant-assist.md`](../../integrations/mcp-clients/home-assistant-assist.md).

## Privacy

- Mark home data private (`"private": true`): private sources are hidden from anonymous callers and
  from tokens without private access, and are never embedded for search.
- The webhook body contains the alert text and record ids; it stays on your LAN with
  `local_only: true`.
- Anyone with `openReads` on can read what `geo_json_events` reads; turn it off
  (`PUT /v1/settings/security {"openReads": false}`) when the broker is reachable beyond your LAN.
