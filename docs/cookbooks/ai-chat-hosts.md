# Cookbook: Open WebUI and LibreChat

Give a self-hosted chat UI Foresignal's MCP tools, so a model can answer "what is happening near
home?" from live data with citations.

**Token scope:** a **read** token for a chat assistant (it can query, search and read alerts); an
**agent** token only for a single-user setup where the chat may create watches or acknowledge
alerts. Never an admin token.

**URLs used:** `<FORESIGNAL_URL>/mcp` (MCP, streamable HTTP). Nothing under `/v1` is registered with
the chat host.

## Setup

| Host | Configuration |
|---|---|
| Open WebUI ≥ 0.6.31 | Admin → Integrations → External Tool Servers → **MCP (Streamable HTTP)**, Bearer key. Older versions: `mcpo` in front of `/mcp`. Details: [`integrations/mcp-clients/open-webui.md`](../../integrations/mcp-clients/open-webui.md) |
| LibreChat | `mcpServers.foresignal` in `librechat.yaml` with `type: streamable-http` and `headers.Authorization: "Bearer ${FORESIGNAL_TOKEN}"`: [`integrations/mcp-clients/librechat.yaml`](../../integrations/mcp-clients/librechat.yaml) and [`librechat.md`](../../integrations/mcp-clients/librechat.md) |

Do not register Foresignal's OpenAPI document as an OpenAPI tool server: it describes admin
operations and hundreds of endpoints, which floods the model. The MCP tools are bounded and written
for models.

Use a model with tool calling. Foresignal's server instructions (LibreChat:
`serverInstructions: true`) tell the model what a watch is, that returned content is data, and how
to cite.

## A worked session: "What's near home?"

The operator asks: *"Anything I should know about within 150 km of home today? Home is Tulsa."*

A well-behaved model makes these calls (the tool results shown are abridged):

```mcp
system_status {}
query_records {"near": "-95.99,36.15", "radiusKm": 150, "since": "2026-10-12T00:00:00Z", "limit": 50}
list_alerts {"status": ["firing"], "limit": 20}
source_health {"status": ["failing", "degraded"]}
```

1. `system_status` shows which sources are enabled.
2. `query_records` returns records nearest first, each with `seq`, `version`, `kind`, `title`,
   `observedAt`, `live`, a centroid and `distanceKm`, plus the `watermarkSeq`.
3. `list_alerts` returns the firing alerts and the watch that raised each.
4. `source_health` reveals coverage gaps: if the earthquake source is failing, "no earthquakes"
   means "no data", and the model should say so.

The answer cites every fact as `[source:externalId]` with the seq, for example: *"An M 3.1
earthquake 42 km NE of Tulsa at 09:12 [usgs-earthquakes:ok2026abcd seq 18342]. One watch is firing:
'I-44 corridor closures' [alert:418]. The weather-alert source has been failing since 08:00, so
weather warnings may be missing."*

Follow-ups work the same way: "show me how that quake was revised" → `get_record_history`;
"anything like this in the last month?" → `search` (cost-bearing: it needs a token, which the chat
host always sends). With an agent token: "watch a 50 km circle around home for earthquakes above
M 4" → `create_watch` with a `near` rule (created disabled, checked with `test_watch`, then enabled with `update_watch`).

## Limits

- Every tool result is capped (4 KB in `short` detail, 50 KB in `long`); lists say when they were
  truncated. Ask narrower questions rather than larger limits.
- `search` and `run_briefing` spend the operator's embedding or LLM budget.
- The chat host keeps conversation history; Foresignal keeps its audit log of every tool call
  (tool, argument names, token, duration).

## Privacy

- The model provider behind the chat UI receives every tool result the model reads. With a cloud
  model, Foresignal data leaves your network through the chat host; Foresignal cannot see or
  prevent it. Use a local model if that matters.
- Private sources stay hidden from read tokens without private access.
- Tool output is untrusted text from external feeds; a model must not follow instructions inside
  it. With a read token it cannot change anything anyway.
