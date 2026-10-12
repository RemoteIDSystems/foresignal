# Claude Code

Claude Code connects to remote MCP servers over HTTP directly; no bridge is needed.

```sh
claude mcp add --transport http foresignal http://foresignal.local:8080/mcp \
  --header "Authorization: Bearer fsr_your_agent_token"
```

- Add `--scope user` (or `-s user`) to make the server available in every project; user-scoped
  servers are stored in `~/.claude.json`. The default scope is `local` (this project, only you).
  `--scope project` writes `.mcp.json` in the repository: do not commit a token there.
- `--transport` and `--header` also accept `-t` and `-H`.
- Check the connection with `claude mcp list`, or `/mcp` inside a session.

Use a **read** token when Claude Code should only look things up, and an **agent** token when it
may create watches and briefings or acknowledge and snooze alerts. The tools and what each scope may
call are listed in `docs/Agent-Guide.md` ("MCP server").
