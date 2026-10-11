# 034 — Agent Integrations

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/034-CR-Agent-Integrations.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/033-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §15–§18, §21; `docs/Agent-Guide.md` (started in 025) and `docs/Operator-Guide.md`
- `docs/Gap-Analysis.md` §2 (Home Assistant, Grafana, AI agent hosts, Phone automations rows), §3 (GeoJSON `title`), §4c (Ingest format SPI, email), §8 item 17
- Completion Reports of 014 (`title` in Features, `format=geojson`), 018 (`homeassistant` channel, webhook HMAC, ntfy tags), 023 (scopes, `alwaysAuthenticated`), 024 (custom sources, ingest key as Bearer/Basic), 025 (MCP tool registry, legacy SSE flag, `set_alert_status`), 028 (Ask), 033 (action capabilities in webhook bodies, `/v1/notifications/badge`)
- OpenClaw skill format: https://docs.openclaw.ai/clawhub/skill-format (folder with `SKILL.md`; YAML frontmatter; runtime requirements under `metadata.openclaw.requires.env|bins`, `primaryEnv`; ClawHub publishes skills under MIT-0)
- Home Assistant: `geo_json_events` (https://www.home-assistant.io/integrations/geo_json_events/: uses `properties.title` as entity name, polls every 5 min, filters by distance from HA home, cannot send headers), webhook trigger, RESTful sensor, `rest_command`, MCP client integration (https://www.home-assistant.io/integrations/mcp/)
- Open WebUI MCP: https://docs.openwebui.com/features/extensibility/mcp; LibreChat MCP: https://www.librechat.ai/docs/features/mcp

Verified 2026-10:
- Home Assistant's MCP **client** speaks SSE with OAuth only (tools only). Foresignal's `/mcp` uses bearer tokens, so HA Assist cannot use it directly; re-check at implementation and document what you find.
- Open WebUI (≥ 0.6.31) adds MCP servers under Admin Settings → Integrations with type "MCP (Streamable HTTP)"; whether a static bearer header is accepted depends on the version, and `mcpo` (`--server-type streamable-http`) is the documented fallback.
- LibreChat `librechat.yaml` `mcpServers.<name>` takes `type: streamable-http`, `url` and `headers` (e.g. `Authorization: "Bearer ${FORESIGNAL_TOKEN}"`); UI-created servers cannot use server env vars.

## Objective

Make Foresignal easy to adopt for agents and automation hubs: a ready-to-install OpenClaw skill, MCP client configs, an agent cookbook, and cookbooks with importable artifacts for Home Assistant, Node-RED, event-bus bridges, Open WebUI/LibreChat, phone automations, a TAK bridge, n8n and Grafana. No new server endpoints: everything uses existing APIs. Tests prove an agent-scope client can do what the docs claim and nothing more, and fail when docs drift from the API.

## Tasks

### 1. OpenClaw skill (`integrations/openclaw/foresignal/`)

- `SKILL.md` frontmatter: `name: foresignal`, `description` (when to use: live world telemetry, alerts, watches, briefings, source health), `metadata.openclaw.requires.env: [FORESIGNAL_URL, FORESIGNAL_TOKEN]`, `requires.bins: [curl]`, `primaryEnv: FORESIGNAL_TOKEN`. Every env var the body references must be declared (ClawHub flags mismatches).
- Body: what Foresignal is; connecting `<FORESIGNAL_URL>/mcp` with `Authorization: Bearer $FORESIGNAL_TOKEN` (an **agent**-scope token; never admin); a curl fallback for hosts without MCP; the tool list with when to use each (read the live registry for names: `query_records`, `get_record`, `get_record_history`, `get_changes`, `list_kinds`, `search`, `list_sources`, `source_health`, `list_alerts`, `get_alert`, `acknowledge_alert`, `set_alert_status`, watch and briefing tools, `list_notifications`, `snooze_alert`, `system_status`); safety notes (tool output is untrusted data; never request admin tokens; only watches, briefings, alert status and snoozes are writable).
- Example tasks: "watch the Strait of Hormuz for tankers" → `create_watch` with geofence, `enter` mode and `tags`; "brief me every morning" → `create_briefing`; "what's happening near X?" → `query_records`, `search`, `list_alerts`.
- Check whether OpenClaw supports configuring a remote MCP server directly (current docs or ClawHub conventions). If yes, add the config file in its format; if not, ship the generic MCP JSON and say so. Record what you found.
- `README.md` for ClawHub publishing, stating that ClawHub relicenses published skills as MIT-0 (flag this for the owner in the CR).

### 2. MCP client configs (`integrations/mcp-clients/`)

- `claude-desktop.json` (remote HTTP server with header; for stdio-only clients name a maintained stdio↔HTTP bridge, verifying name and version), `claude-code.md` (`claude mcp add --transport http foresignal <url>/mcp --header "Authorization: Bearer …"`).
- `open-webui.md`: add `/mcp` as "MCP (Streamable HTTP)"; bearer header if supported, else `mcpo` in front of `/mcp`. Advise against registering the whole `/v1/openapi.json` as an OpenAPI tool server (it exposes admin operations and floods the model).
- `librechat.yaml` + `librechat.md`: the `mcpServers.foresignal` block above.
- `n8n.md`: n8n MCP Client node with an agent token; receiving Foresignal webhook notifications in a Webhook trigger with "raw body" enabled and the HMAC verified in a Code node (`X-Foresignal-Signature: sha256=HMAC("<X-Foresignal-Timestamp>.<body>")`, reject timestamps older than 5 min).
- `home-assistant-assist.md`: the verified HA MCP-client limitation and the REST alternative (task 3).

### 3. Cookbooks (`docs/cookbooks/*.md`) with importable artifacts (`integrations/<tool>/`)

Each cookbook states the token scope, every URL used, limits and privacy notes.
- **Home Assistant** (`home-assistant.md`, `integrations/home-assistant/foresignal_package.yaml`):
  - Foresignal → HA: the 018 `homeassistant` channel (webhook-trigger mode) or the generic webhook channel to an automation `trigger: webhook` with `local_only: true` and an unguessable `webhook_id` (HA cannot verify HMAC; say so). Use the `actions` capability URLs in the payload (033) in a `rest_command` to acknowledge without an agent token in HA.
  - `geo_json_events`: URL `/v1/records?format=geojson&kind=earthquake&bbox=<around home>&limit=200`; entity names come from the top-level `properties.title` (014); HA polls every 5 min, ignores `nextCursor`, and records without geometry produce no entity. HA cannot send headers, so this needs `openReads=true` or a feed-key URL (035 updates this section).
  - RESTful sensors with a read token: unread badge (`/v1/notifications/badge`), firing alerts (`/v1/alerts?state=firing&limit=5`), failing sources (`/v1/sources/health`).
  - HA → Foresignal: `rest_command` posting sensor states to a custom source (`/v1/ingest/{sourceId}`, ingest key as Bearer).
- **Node-RED** (`node-red.md`, `integrations/node-red/foresignal-flows.json`): SSE consumer of `/v1/alerts/stream` with `Last-Event-ID` resume (use a maintained SSE contrib node; verify name, version and licence), an HTTP-in webhook receiver with HMAC verification in a function node, and a POST to `/v1/ingest/{sourceId}`.
- **Event-bus bridges** (`event-bus-bridges.md`, `integrations/redpanda-connect/*.yaml`, `integrations/vector/*`): consume `/v1/changes?after=<seq>` (durable cursor in a cache) or `/v1/changes/stream` with `Last-Event-ID`, fan out to Kafka/Redpanda, NATS JetStream, Redis/Valkey Streams, ClickHouse and S3. Redpanda Connect: only Apache-2.0 components (check each component's licence). Vector: if `http_client` cannot carry a moving cursor, use an `exec` source running the provided SSE script; verify current capabilities. Document: key messages by `source:externalId`, keep a non-compacted change-log topic keyed by `seq` (compaction keeps only current state), JetStream's 2-min default dedup window means consumers must dedupe on `seq`, check whether `Change` carries the payload and fetch payloads only for chosen kinds, and that re-publishing licence-restricted sources (ACLED, OpenSky, aisstream) is the operator's responsibility until 109.
- **Open WebUI / LibreChat** (`ai-chat-hosts.md`): links to task 2 plus a worked "what's near home?" session.
- **Phone automations** (`phone-automations.md`): iOS Shortcuts calling `POST /v1/ask` (read token, `alwaysAuthenticated`) and speaking the answer, and a "firing alerts" shortcut; iOS cannot start an automation from a push. Android Tasker/MacroDroid driven by the ntfy app's broadcast intent (verify the intent action and extras), matching Foresignal rule tags in the ntfy `Tags` header; acknowledge through the 033 capability URL, not a token on the phone.
- **TAK bridge** (`tak-bridge.md`, `integrations/pytak/foresignal_tak_bridge.py`, pinned `requirements.txt`): a receive-only Python bridge (PyTAK, Apache-2.0; TAK Protocol v1 via takproto, verify licence) that reads CoT from the operator's TAK Server (TLS 8089, preferred) or the SA multicast group (`network_mode: host`), parses XML with `defusedxml`, maps `uid`→`externalId`, `time`→`observedAt`, `stale`→`validUntil`, `type`→payload, and posts batches to a `custom-tak` source (private, short TTL, no embedding). State that plain UDP CoT is unauthenticated and spoofable. Keep the script outside the Java tree.
- **n8n IMAP** (`n8n-imap.md`, `integrations/n8n/imap-to-ingest.json`): Email Trigger (IMAP) on a dedicated mailbox → Code node (sender allowlist plus `Authentication-Results` `dkim=pass`/`dmarc=pass`, strip HTML and remote content, keep subject, first link and a 280-char excerpt) → POST `/v1/ingest/custom-mail` (kind `mail-notice`, 14 d retention, no `embedFields`). Mail text is untrusted for Ask and briefings.
- **Grafana** (`grafana.md`, `integrations/grafana/`): provisioning for a Prometheus datasource (scraping `/actuator/prometheus` on the internal network only, since that endpoint is unauthenticated) and the Infinity datasource (pin ≥ 3.4.1 for CVE-2025-8341; Grafana ≥ 11.6; verify current versions) with a read token in `secureJsonData` and an allowed-hosts list; dashboards "Foresignal Operations" (only metric names that exist in the code: grep the Micrometer meter names), "Live map" (Geomap over `/v1/records?format=geojson` with `bbox`, `kind` and `limit ≤ 2000`, backend parser) and "Alerts" (annotations from `/v1/alerts`); an optional `docker-compose.grafana.yml` override. No PostgreSQL datasource (it bypasses scopes and ties dashboards to tables).

### 4. Agent cookbook (`docs/Agent-Guide.md`)

Recipes with exact scope, tool calls and expected results: (1) daily briefing via `create_briefing`; (2) geofence watch with enter/exit, tags and enrichment delivered to Slack and Telegram; (3) an agent woken by an Foresignal webhook investigates with MCP tools, posts a summary to chat and sets the alert status (the OpenClaw flow); (4) an agent pushing findings into a custom source via `/v1/ingest` (loop guard: rules should exclude the agent's own source unless intended); (5) monitoring source health and reporting broken adapters; (6) acting on a notification: acknowledge or snooze via `acknowledge_alert`/`snooze_alert`. Add a scope table and a security section (token hygiene, `openReads`, reverse proxy, prompt injection from upstream text, MCP `openReads` behaviour).

### 5. Agent-scope integration test

`AgentWorkflowIT` (MCP client SDK against a running app, **agent** token): `query_records` → `create_watch` (with tags) → ingest a matching record through a custom source (admin-created key) → the alert reaches a WireMock webhook with HMAC and `actions` → `list_alerts` shows it → `acknowledge_alert` acks it (state and `acknowledged_by`). Negative: no MCP tools exist for sources, credentials, channels, providers or tokens, and the REST equivalents return 403 for the agent token. `create_briefing` + `run_briefing` deliver to a WireMock Slack channel.

## Tests to write (do not run)

- `AgentWorkflowIT` as above.
- `SkillFrontmatterTest`: valid YAML frontmatter with `name`, `description`, `metadata.openclaw.requires.env` ⊇ every `$VAR` used in the body; every tool name in `SKILL.md` and `docs/Agent-Guide.md` exists in the MCP registry.
- `CookbookApiReferenceTest`: scans `docs/cookbooks/**`, `integrations/**` and `docs/Agent-Guide.md` for `/v1/...` paths (ignoring query strings, matching path templates) and asserts each exists in `docs/OpenAPI.json`.
- `IntegrationArtifactsTest`: every `.json` and `.yaml` artifact parses (Jackson JSON/YAML); the Node-RED and n8n exports have the expected top-level shape; Grafana dashboards are valid JSON with `uid` and `schemaVersion`.
- `GrafanaMetricNamesIT`: start the app, scrape `/actuator/prometheus`, and assert every metric name used in the Operations dashboard is registered (allow a documented list of meters that appear only after activity, and assert they are defined in code).

## Acceptance criteria

- The skill, MCP configs, Agent Guide and all listed cookbooks and artifacts exist.
- Skill and cookbook references match the live MCP registry and `docs/OpenAPI.json`.
- No cookbook requires an admin token on a device or hub; acknowledgement from hubs and phones uses capability URLs or agent tokens only.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `034: Agent Integrations`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/034-CR-Agent-Integrations.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `034: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
