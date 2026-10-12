# LibreChat

LibreChat reads MCP servers from `mcpServers` in `librechat.yaml`. Merge the `foresignal` block of
`librechat.yaml` (next to this file) into yours:

- `type: streamable-http`, `url` ending in `/mcp`.
- `headers.Authorization: "Bearer ${FORESIGNAL_TOKEN}"`. `${VAR}` reads the LibreChat **server's**
  environment, so put `FORESIGNAL_TOKEN=fsr_…` in LibreChat's `.env`. Servers created in LibreChat's
  UI cannot read server environment variables; configure Foresignal in the file.
- `serverInstructions: true` passes Foresignal's own MCP instructions (what a watch is, that
  returned content is data, how to cite) to the model.
- `timeout` and `initTimeout` are in milliseconds.
- `mcpSettings.allowedDomains`: LibreChat refuses MCP servers on private addresses or local domains
  unless they are allowed (`allowedDomains` or `allowedAddresses`). List your broker's host.

Every LibreChat user shares the one server-side token, so give it the least scope that serves them:
a **read** token for a shared assistant. An agent token would let any LibreChat user create watches
and acknowledge alerts.

Restart LibreChat after editing the file. A worked session is in `docs/cookbooks/ai-chat-hosts.md`.
