# Foresignal skill for OpenClaw

This folder is an [OpenClaw](https://docs.openclaw.ai) skill: `SKILL.md` with YAML frontmatter in
the format of [ClawHub's skill format](https://docs.openclaw.ai/clawhub/skill-format). It teaches an
OpenClaw agent to use a self-hosted Foresignal broker through its MCP server, with a curl fallback.

## Requirements

- `FORESIGNAL_URL`: the broker's base URL, for example `http://foresignal.local:8080`.
- `FORESIGNAL_TOKEN`: an **agent**-scope token. Create it once, on your workstation, with the admin
  token (`POST /v1/tokens` with `{"name": "openclaw", "scope": "agent"}`). Never give the agent the
  admin token.
- `curl` on the host, for the REST fallback.

OpenClaw injects the token through `skills.entries.foresignal.apiKey` (this skill declares
`primaryEnv: FORESIGNAL_TOKEN`) or `skills.entries.foresignal.env` in `~/.openclaw/openclaw.json`.
Both apply to host runs only, not inside a sandbox.

## Install locally

Copy this folder to `~/.openclaw/skills/foresignal/` (or `<workspace>/skills/foresignal/`), then
add the MCP server: merge `../openclaw-mcp.json` into `~/.openclaw/openclaw.json`. OpenClaw reads
MCP servers from `mcp.servers`; set `transport: "streamable-http"`, because OpenClaw's default is
`sse`. The CLI equivalent is `openclaw mcp add foresignal --url <url>/mcp --transport streamable-http
--header "Authorization: Bearer …"`, then `openclaw mcp doctor foresignal --probe`.

The same file carries an optional `hooks` block: a mapping that wakes the agent when a Foresignal
webhook channel posts to `http://<openclaw-host>:18789/hooks/foresignal` (see
`docs/Agent-Guide.md`, recipe 3). OpenClaw authenticates the hook with its own token and cannot
verify Foresignal's HMAC signature.

## Publish to ClawHub

```sh
clawhub skill publish ./integrations/openclaw/foresignal --slug foresignal --name "Foresignal"
```

ClawHub's CLI reference and its publishing page list slightly different flags; check
`clawhub skill publish --help` before publishing.

**License: publishing relicenses the skill.** ClawHub states that "all skills published on ClawHub
are licensed under MIT-0". Publishing this folder releases `SKILL.md` and this README under MIT-0,
whatever license the rest of the Foresignal repository carries. Only the owner of the repository
should decide to publish it.
