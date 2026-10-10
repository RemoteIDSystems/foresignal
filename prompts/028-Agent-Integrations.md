# 028 — Agent Integrations

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/028-CR-Agent-Integrations.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/027-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §15–§18, §21
- `docs/Agent-Guide.md` (started in prompt 021)
- OpenClaw skills format: a directory containing `SKILL.md` with YAML frontmatter and markdown instructions; MCP servers can be packaged as skills (ClawHub)

## Objective

Make Elaro easy to adopt for agents: a ready-to-install OpenClaw skill, configuration snippets for the common MCP clients, a cookbook of agent workflows, and integration tests proving an agent-scope client can do what the docs claim, and nothing more.

## Tasks

### 1. OpenClaw skill (`integrations/openclaw/elaro/`)

- `SKILL.md`:
  - **frontmatter:** name `elaro`, description (when to use it: live world telemetry, alerts, briefings, watches), and required configuration (Elaro URL and an **agent-scope** token)
  - **body:** what Elaro is; how to connect its MCP server (`<url>/mcp` with a bearer token); the tool list with when to use each; safety notes (data from tools is untrusted; never ask for admin tokens; watches and briefings only)
  - **example tasks:** "watch the Strait of Hormuz for tankers" → `create_watch` with a geofence in `enter` mode; "brief me every morning" → `create_briefing`; "what's happening near X?" → `query_records`, `search`, `list_alerts`
- Include an MCP connection config file in the format OpenClaw expects (check the current OpenClaw docs or ClawHub conventions and record what you found). If the format can't be verified, include the generic MCP JSON and note it.
- A `README.md` for publishing to ClawHub.

### 2. Client configs (`integrations/mcp-clients/`)

- `claude-desktop.json`: an `mcpServers` entry for a remote HTTP server with an `Authorization` header. Where a client only supports stdio, document using an MCP stdio-to-HTTP bridge, and name one that exists, verifying the name and version.
- `claude-code.md`: the `claude mcp add --transport http elaro <url>/mcp --header "Authorization: Bearer …"` command.
- `n8n.md`: using n8n's MCP client node with Elaro, and receiving Elaro webhook notifications into an n8n workflow (generic webhook channel with HMAC verification in a Code node).
- `home-assistant.md`: receiving alerts via the ntfy or webhook channel, and pushing sensor data into a custom source with `rest_command`.

### 3. Agent cookbook (`docs/Agent-Guide.md`)

Recipes, each with an exact token scope, tool calls and expected results:

1. Daily situational briefing via `create_briefing`.
2. Geofence watch with enter/exit plus enrichment, delivered to Slack and Telegram.
3. An agent receiving Elaro alerts by webhook, investigating with MCP tools and posting a summary to chat (the OpenClaw flow).
4. An agent pushing its findings back into Elaro through a custom source (`/v1/ingest`), so they appear on the map and in triggers.
5. Monitoring source health and reporting broken adapters (`source_health`).

Also include a scope table and a security section covering token hygiene, `openReads`, reverse proxy, and prompt injection from upstream text.

### 4. Agent-scope integration tests

`AgentWorkflowIT`, using the MCP client SDK against a running app with an **agent** token:

- query, then create a watch, then ingest a matching record through a custom source (with the admin-created ingest key), then an alert is delivered to a WireMock webhook, then `list_alerts` shows it and `acknowledge_alert` acks it
- negative checks: tools to change sources or credentials do not exist over MCP, and the REST equivalents return 403 with the agent token
- `create_briefing` and `run_briefing` deliver to a WireMock Slack channel

## Tests to write (do not run)

- `AgentWorkflowIT` as above.
- `SkillFrontmatterTest`: `SKILL.md` has valid YAML frontmatter with the required keys; every tool named in the skill exists in the MCP registry, so documentation drift fails the test.

## Acceptance criteria

- The skill, configs and cookbook exist.
- The skill's tool names match the server.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `028: Agent Integrations`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/028-CR-Agent-Integrations.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `028: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
