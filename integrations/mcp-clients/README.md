# MCP client configurations

Foresignal's MCP server is at `<FORESIGNAL_URL>/mcp` (streamable HTTP, protocol versions 2024-11-05
to 2025-11-25). Every client needs the URL and the header `Authorization: Bearer <token>`. Give
agents an **agent**-scope token, and read-only assistants a **read**-scope token. Never an admin
token: MCP has no admin tools.

| File | Client |
|---|---|
| `claude-desktop.json` | Claude Desktop, through the `mcp-remote` stdio-to-HTTP bridge |
| `claude-code.md` | Claude Code (`claude mcp add --transport http`) |
| `open-webui.md` | Open WebUI (native "MCP (Streamable HTTP)", or `mcpo`) |
| `librechat.yaml`, `librechat.md` | LibreChat (`mcpServers` in `librechat.yaml`) |
| `n8n.md` | n8n (MCP Client Tool, plus receiving Foresignal webhooks) |
| `home-assistant-assist.md` | Home Assistant Assist (why its MCP client cannot connect, and the REST alternative) |
| `../openclaw/openclaw-mcp.json` | OpenClaw (`mcp.servers`) |

## Claude Desktop

Claude Desktop's `claude_desktop_config.json` documents local (stdio) servers only: `command`,
`args`, `env`. Remote servers are added as custom connectors (Customize → Connectors → Add custom
connector, with request headers), but connectors connect from Anthropic's cloud, so the server must
be reachable from the public internet. A broker on your LAN or tailnet is not.

`claude-desktop.json` therefore runs [`mcp-remote`](https://www.npmjs.com/package/mcp-remote)
0.14.3 (MIT), a maintained stdio-to-HTTP bridge, with `npx`:

- `--transport http-only`: Foresignal speaks streamable HTTP; there is no SSE fallback unless the
  operator enables the legacy transport.
- `--allow-http`: needed for a plain `http://` URL on a trusted LAN. Drop it when the broker is
  behind HTTPS (for example Tailscale Serve), and use the `https://` URL.
- `--header "Authorization:${AUTH_HEADER}"` with the bearer value in `env`: the form without spaces
  in `args` that `mcp-remote` documents for Claude Desktop on Windows, which does not escape spaces.

Merge the `foresignal` entry into `mcpServers` of your `claude_desktop_config.json` and replace the
placeholder token. It needs Node.js for `npx`.
