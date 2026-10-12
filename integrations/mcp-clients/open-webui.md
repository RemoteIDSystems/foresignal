# Open WebUI

Open WebUI 0.6.31 and later connect to MCP servers natively, over streamable HTTP only, which is
what Foresignal serves.

## Native MCP (preferred)

1. Open **Settings → Admin → Integrations**. Under **External Tool Servers**, click **+ Add
   Connection**.
2. Set **Type** to **MCP (Streamable HTTP)**.
3. **Server URL:** `http://foresignal.local:8080/mcp` (the URL must be reachable from the Open WebUI
   server or container, not from your browser).
4. **Auth:** **Bearer**, and put the Foresignal token in **Key**. Selecting Bearer with an empty key
   sends an empty `Authorization` header, which Foresignal refuses with 401 unless `openReads` is on.
5. Save, and restart Open WebUI if it asks.

The section is greyed out when `ENABLE_TOOL_SERVERS` is `False`. Open WebUI's docs ask you to set
`WEBUI_SECRET_KEY` for OAuth-backed MCP connections; a static bearer key does not depend on it.

Use a **read** token for a chat assistant that only looks things up; use an **agent** token only if
the chat may create watches or acknowledge alerts.

## Fallback: mcpo

On an older Open WebUI, or one where the bearer option does not work, run
[`mcpo`](https://github.com/open-webui/mcpo) (MIT, 0.0.20 on PyPI) in front of `/mcp`. It exposes
the MCP tools as an OpenAPI tool server that Open WebUI can add as an ordinary tool server:

```sh
uvx mcpo@0.0.20 --port 8000 --api-key "a-long-random-key" \
  --server-type "streamable-http" \
  --header '{"Authorization": "Bearer fsr_your_read_token"}' \
  -- http://foresignal.local:8080/mcp
```

mcpo's README shows `--header` with the SSE server type; its source passes the same headers to the
streamable HTTP client. Add `http://<mcpo-host>:8000` as a tool server in Open WebUI with the
`--api-key` value as its bearer key.

## Do not register `/v1/openapi.json` as a tool server

Open WebUI can turn any OpenAPI document into tools. Do not point it at Foresignal's
`/v1/openapi.json`:

- the document describes every operation, including admin ones (sources, credentials, channels,
  tokens), and a model offered those tools will try them;
- several hundred operations flood the model's context and make tool choice worse;
- the MCP tools are curated for models: bounded output (4 KB short, 50 KB long), descriptions
  written for a model, and the untrusted-data notice on text-bearing tools.

A token's scope still protects admin operations, but use `/mcp`.
