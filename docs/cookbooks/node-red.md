# Cookbook: Node-RED

Three flows in one importable tab,
[`integrations/node-red/foresignal-flows.json`](../../integrations/node-red/foresignal-flows.json)
(Menu → Import):

1. a consumer of the alert stream that resumes where it stopped;
2. an HTTP endpoint that receives Foresignal webhook notifications and verifies their signature;
3. a POST of records to a custom source.

**Token scope:** a **read** token for the alert stream; an **HMAC secret** (no token) for the
webhook receiver; an **ingest key** bound to one custom source for pushing records. No agent or admin
token in Node-RED.

**URLs used:** `/v1/alerts/stream`, `/v1/ingest/{sourceId}`, and for setup `/v1/channels`,
`/v1/custom-sources`, `/v1/tokens`. Node-RED's own: `POST /foresignal/webhook`.

The flows read their settings from Node-RED's environment (`env.get` in function nodes), so no
secret is stored in the flow file:

| Variable | Used by | Example |
|---|---|---|
| `FORESIGNAL_URL` | 1, 3 | `http://foresignal.local:8080` |
| `FORESIGNAL_TOKEN` | 1 | a read token, `fsr_…` |
| `FORESIGNAL_WEBHOOK_SECRET` | 2 | the channel's `hmacSecret` |
| `FORESIGNAL_INGEST_SOURCE` | 3 | `custom-nodered` |
| `FORESIGNAL_INGEST_KEY` | 3 | `eik_…` |

## 1. Alert stream with `Last-Event-ID` resume

Install [`@parasaurolophus/node-red-eventsource`](https://www.npmjs.com/package/@parasaurolophus/node-red-eventsource)
**3.0.0** (MIT, published 2026-03; Node-RED ≥ 3.1, Node.js ≥ 18) from the palette manager. The flow
uses its **EventSource Client** node, which wraps the maintained
[`eventsource-client`](https://www.npmjs.com/package/eventsource-client) library and passes every
event through with its `event`, `id` and `data`.

Why this node: Foresignal's alert events are *named* (`alert`, `alert.updated`, `alert.resolved`).
The package's other node (`EventSource`), and `node-red-contrib-sse-client` (0.2.4, last published
2022), forward only unnamed `message` events or drop the event id, so they cannot resume.

How the flow works:

- `start` (an inject node, once at deploy) → `stream options` builds `{url, headers:
  {Authorization: "Bearer …"}, initialLastEventId}` and sends it to the EventSource Client node,
  which connects to `/v1/alerts/stream?minSeverity=warning`.
- The client reconnects by itself and sends `Last-Event-ID` with the last id it saw. Across a
  Node-RED restart, `alert event` keeps the last id in flow context (`foresignal_alert_rev`) and
  `stream options` passes it as `initialLastEventId`. Configure a persistent context store
  (`contextStorage` with `localfilesystem` in `settings.js`), or the id is lost on restart.
- Alert events carry `id: <rev>`. On resume the stream replays every alert whose rev is newer, in
  its **current** state, so upsert by `payload.alert.id` rather than appending.
- `heartbeat` arrives every 15 s and is dropped by the function node.

## 2. Webhook receiver with HMAC verification

Create a `webhook` channel pointing at Node-RED, with a secret, using the admin token:

```sh
curl -fsS -X POST http://foresignal.local:8080/v1/channels \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{
    "name": "Node-RED", "type": "webhook", "events": ["alert", "system"],
    "config": {"url": "http://nodered.local:1880/foresignal/webhook"},
    "secrets": {"hmacSecret": "a-long-random-secret"}}'
```

- The `http in` node has **"Do not parse request body"** on (`skipBodyParsing` in the export), so
  `msg.payload` is the raw body as a Buffer. The signature covers the exact bytes; a parsed and
  re-serialized body would not verify.
- `verify HMAC` loads Node's `crypto` through the function node's **Setup → Modules** (`libs` in the
  export: `crypto` as `crypto`); function nodes have no `crypto` global. This needs
  `functionExternalModules` (on by default) in `settings.js`.
- It recomputes `sha256=` + hex HMAC-SHA256(secret, `<X-Foresignal-Timestamp>.<raw body>`),
  compares in constant time with `X-Foresignal-Signature`, and rejects timestamps more than 5
  minutes from Node-RED's clock. Output 1 is the verified notification (`msg.topic` = `subtype`);
  output 2 is always the HTTP reply: 204, or 401 when verification fails.

## 3. Pushing records

Create a custom source (admin token, once); keep the `ingestKey` it returns:

```sh
curl -fsS -X POST http://foresignal.local:8080/v1/custom-sources \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{
    "id": "custom-nodered", "name": "Node-RED sensors", "kinds": ["sensor-reading"],
    "titleField": "unit", "observedAtMode": "UPSTREAM"}'
```

`build record` sets `msg.url` to `/v1/ingest/custom-nodered`, `msg.headers` to
`Authorization: Bearer eik_…`, and `msg.payload` to `{"records": [{externalId, kind, observedAt,
geometry, payload}]}`; the `http request` node (method "set by msg.method", URL empty) sends it. The
response is the ingest outcome (`seen`, `persisted`, `rejected`, and `rejections` per item).

## Limits

- One alert stream per flow; Foresignal allows 8 open streams per token.
- A resume replays at most 10,000 events; beyond that the stream sends `reset` and closes, and the
  client reconnects from the id it has.
- Ingest: 5 MB and 10,000 records per request; 429 `INGEST_RECORD_RATE` above the source's
  `recordsPerMinute` (default 6,000), with `Retry-After`.

## Privacy

- The read token sees every non-private source; restrict it if Node-RED is shared.
- Mark sources that carry personal or home data `"private": true`.
- Node-RED's debug sidebar shows alert text; anyone with editor access sees it.
