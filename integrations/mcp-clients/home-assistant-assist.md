# Home Assistant Assist and Foresignal's MCP server

**Short answer: Home Assistant's MCP client cannot connect to Foresignal today. Use the REST
integration in `docs/cookbooks/home-assistant.md` instead.**

## What was checked (2026-10)

Home Assistant's [Model Context Protocol integration](https://www.home-assistant.io/integrations/mcp/)
is an MCP **client** that gives Assist (with an LLM conversation agent) the tools of a remote MCP
server.

- **Setup:** UI config flow only. Its form asks for an "SSE Server URL" and, optionally, an OAuth
  client id and secret. There is no field for a static header or token.
- **Transport:** the documentation says SSE. The integration's code on the development branch
  (`homeassistant/components/mcp/coordinator.py`) tries streamable HTTP first and falls back to SSE,
  so newer releases may reach a streamable endpoint such as Foresignal's `/mcp`.
- **Authentication:** the only way the integration sends `Authorization: Bearer …` is through its
  OAuth token manager. Foresignal does not run an OAuth authorization server; it takes static
  bearer tokens (`fsr_…`).
- **Features:** tools only (no prompts, resources, sampling or notifications).

So the integration cannot present a Foresignal token. It could only connect while Foresignal's
`security.openReads` is on, and then it would see the anonymous tool set: the `read` tools, without
the cost-bearing ones (`search`, `run_briefing`) and without any write tool. Turning `openReads` on
for this exposes every read on the network to anyone; not recommended.

Home Assistant also ships an MCP **server** integration, which lets other MCP clients control Home
Assistant. That is the opposite direction and unrelated to this.

## The REST alternative

Everything Assist would need is reachable with plain REST calls that do carry a header:

- RESTful sensors with a **read** token for the unread badge (`/v1/notifications/badge`), firing
  alerts (`/v1/alerts?state=firing&limit=5`) and failing sources (`/v1/sources/health`).
- Automations triggered by a Foresignal webhook channel, and `rest_command` actions that acknowledge
  an alert through the capability URL in the webhook body (no agent token in Home Assistant).

`integrations/home-assistant/foresignal_package.yaml` contains all of these; Assist can then answer
from the sensors' states and attributes ("Are there any Foresignal alerts?").

If a later Home Assistant release adds a static header or bearer option to its MCP client, connect
it to `<FORESIGNAL_URL>/mcp` with a **read** token.
