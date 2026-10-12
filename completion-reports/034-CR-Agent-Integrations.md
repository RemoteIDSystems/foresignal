# 034-CR-Agent-Integrations

| | |
|---|---|
| Prompt | 034-Agent-Integrations.md |
| Status | Complete |
| Implementation commit | fc72093fc6f794f599c35508ea162c824714f182 |
| Date | 2026-10-12 |

## Summary

Agents and automation hubs can now adopt Foresignal from ready-made material. Every item uses
existing APIs; no server endpoint or production class was added or changed.

The spec assigns no operation to 034 (`grep -c '"x-foresignal-prompt": "034"' docs/OpenAPI.json`
→ 0; `pending-operations.txt` has no 034 line). `docs/OpenAPI.json`, `AccessPolicy` and Postman are
therefore unchanged, and so is the spec changelog.

**Task 1, OpenClaw skill** (`integrations/openclaw/foresignal/`):
- `SKILL.md`:
  - frontmatter in ClawHub's documented YAML-map form: `name: foresignal`, `description`,
    `version`, and `metadata.openclaw.requires.env` [`FORESIGNAL_URL`, `FORESIGNAL_TOKEN`],
    `requires.bins` [`curl`], `primaryEnv: FORESIGNAL_TOKEN`;
  - the body: what Foresignal is, connecting to `/mcp` with an agent token, a tool table of all 27
    registry tools with scope and when to use each, the three example tasks (in `mcp` call
    blocks), a curl fallback and the safety notes.
- `README.md`: install, publish, and the MIT-0 relicensing warning.
- OpenClaw supports remote MCP servers natively (`mcp.servers` in `~/.openclaw/openclaw.json`,
  `transport: "streamable-http"`, `headers`). `integrations/openclaw/openclaw-mcp.json` ships that
  block, plus an optional `hooks` mapping for recipe 3 (`POST /hooks/foresignal` → agent turn).

**Task 2, MCP client configs** (`integrations/mcp-clients/`):
- `README.md`, with the Claude Desktop explanation.
- `claude-desktop.json`: `mcp-remote@0.14.3` through `npx`, with `--transport http-only`,
  `--allow-http` and the space-free `--header` form.
- `claude-code.md`, `open-webui.md` (native MCP, the `mcpo` 0.0.20 fallback, and why not to
  register `/v1/openapi.json`), `librechat.yaml` + `librechat.md`.
- `n8n.md`: the MCP Client Tool; a Webhook trigger with Raw Body and the HMAC check in a Code node
  with a 5-minute window.
- `home-assistant-assist.md`.

**Task 3, cookbooks and artifacts.** `docs/cookbooks/`:
- `home-assistant.md` with `integrations/home-assistant/foresignal_package.yaml`: REST sensors, a
  webhook automation with `local_only`, `rest_command` for capability URLs and ingest, an input_text
  helper and an acknowledge script.
- `node-red.md` with `integrations/node-red/foresignal-flows.json`: SSE with resume, a signed
  webhook receiver, ingest.
- `event-bus-bridges.md` with five Redpanda Connect pipelines (`integrations/redpanda-connect/`) and
  `integrations/vector/vector.yaml` + `foresignal_sse.py`.
- `ai-chat-hosts.md`.
- `phone-automations.md`.
- `tak-bridge.md` with `integrations/pytak/foresignal_tak_bridge.py` and a pinned
  `requirements.txt`.
- `n8n-imap.md` with `integrations/n8n/imap-to-ingest.json`.
- `grafana.md` with `integrations/grafana/`:
  - provisioning for the Prometheus and Infinity data sources and the dashboards folder;
  - three dashboards;
  - `prometheus.yml`;
  - `docker-compose.grafana.yml`.

Each cookbook states its token scope, every URL it uses, limits and privacy.

**Task 4, Agent Guide** (`docs/Agent-Guide.md`, edited):
- The intro points to the cookbooks.
- The Home Assistant section has the verified `geo_json_events` facts (UI-only, 5 min, one page,
  `limit=200`).
- "Connecting clients" names each host's verified mechanism.
- The tool table gains the missing `search` row, and the agent row lists every write tool.
- The stale sentence saying `search` and the briefing tools were still to come is fixed.
- A new "Agent cookbook" section has a scope table, the six recipes with exact scopes, tool calls
  and expected results, and a security section.

**Task 5, `AgentWorkflowIT`:**
- An agent token over the MCP SDK's streamable client:
  - `query_records`;
  - `create_watch` with tags and a geofence;
  - a record ingested through an admin-created custom source with its `eik_` key as Bearer;
  - the alert at a WireMock webhook, with an independently computed HMAC and `actions` (ack URL
    under `publicBaseUrl/v1/actions/eac_`);
  - `list_alerts`;
  - `acknowledge_alert`: the state, and `acknowledged_by = token:<name>` in the response, the
    database and REST.
- `create_briefing` + `run_briefing` deliver to a WireMock Slack channel.
- Negative checks:
  - the only tools with "source" in the name are `list_sources` and `source_health`;
  - no tool name mentions credentials, channels, providers, tokens, settings, ingest, LLM or
    secrets;
  - nine admin REST operations answer 403 to the agent token;
  - every tool `SKILL.md` names is served to an agent.

## Files created / modified

**New**
- `integrations/openclaw/foresignal/SKILL.md`, `integrations/openclaw/foresignal/README.md`,
  `integrations/openclaw/openclaw-mcp.json`
- `integrations/mcp-clients/`: `README.md`, `claude-desktop.json`, `claude-code.md`,
  `open-webui.md`, `librechat.yaml`, `librechat.md`, `n8n.md`, `home-assistant-assist.md`
- `integrations/home-assistant/foresignal_package.yaml`
- `integrations/node-red/foresignal-flows.json`
- `integrations/redpanda-connect/`: `changes-to-kafka.yaml`, `changes-to-nats.yaml`,
  `changes-to-redis.yaml`, `changes-to-clickhouse.yaml`, `changes-to-s3.yaml`
- `integrations/vector/`: `vector.yaml`, `foresignal_sse.py`
- `integrations/pytak/`: `foresignal_tak_bridge.py`, `requirements.txt`
- `integrations/n8n/imap-to-ingest.json`
- `integrations/grafana/`:
  - `provisioning/datasources/foresignal.yaml`, `provisioning/dashboards/foresignal.yaml`;
  - `dashboards/foresignal-operations.json`, `dashboards/foresignal-live-map.json`,
    `dashboards/foresignal-alerts.json`;
  - `prometheus.yml`, `docker-compose.grafana.yml`.
- `docs/cookbooks/`: `home-assistant.md`, `node-red.md`, `event-bus-bridges.md`,
  `ai-chat-hosts.md`, `phone-automations.md`, `tak-bridge.md`, `n8n-imap.md`, `grafana.md`
- Tests:
  - `integrations/`: `AgentWorkflowIT`, `SkillFrontmatterTest`, `CookbookApiReferenceTest`,
    `IntegrationArtifactsTest`, `GrafanaMetricNamesIT`;
  - test support: `testsupport/integrations/IntegrationDocs`.

**Changed**
- `docs/Agent-Guide.md`: edited, not replaced.
- `docs/Invariants.md`: SEC-4 also lists `AgentWorkflowIT`.

No production code, no new dependencies, no spec or Postman change, no Flyway, Liquibase or SQL
files, and no `.gitlab-ci.yml` change. Nothing was copied from AGPL/GPL references. No credential is
committed: the examples use placeholders (`fsr_your_agent_token`, `eik_…`), and
`CookbookApiReferenceTest` refuses real-looking `fsr_`/`eik_`/`eac_`/`fsf_`/`fsx_` strings. The
Python script lives outside the Java tree. Nothing was installed system-wide and no container was
touched.

## Deviations from the prompt

1. **The `homeassistant` channel carries no `actions` array.** Its webhook mode posts
   `WebhookChannel.defaultBody(m, origin)` without capabilities, and `ActionMinter` treats it as a
   type without buttons, so capabilities would only appear as links in `bodyMarkdown`. The cookbook
   uses the generic `webhook` channel with `config.actions.enabled: true` (aimed at Home
   Assistant's `/api/webhook/<id>`) when Home Assistant should acknowledge. It documents the
   `homeassistant` type for alert-only use. No code change: a structured `actions` array on the
   Home Assistant type would be a small 018/033 extension if the owner wants it.
2. **`geo_json_events` naming: Home Assistant's docs and code disagree.** The docs say the entity
   name is `properties.title`; the code (`geo_location.py`) prefers `properties.name` and falls back
   to the title. Foresignal features have no top-level `name` (record fields sit under
   `properties.payload`), so the title is used. The integration is UI-configured only, so the
   cookbook gives the URL rather than YAML. The example `limit` is 200, as the prompt asks; the
   Agent Guide's old example said 500.
3. **Home Assistant's MCP client**, re-checked: config flow only, "SSE Server URL", OAuth only,
   tools only. Its development-branch code tries streamable HTTP first. With no static token field
   it cannot authenticate to Foresignal; documented in `home-assistant-assist.md`.
4. **Node-RED SSE node.** The prompt asks for a maintained node with headers and `Last-Event-ID`.
   I chose `@parasaurolophus/node-red-eventsource` 3.0.0 (MIT, 2026-03), node "EventSource Client",
   which wraps `eventsource-client` 1.2 (`headers`, `initialLastEventId`; `onMessage` receives
   named events with their id). The alternatives were rejected:
   - the package's own `EventSource` node uses `onmessage` and so drops Foresignal's named events;
   - `node-red-contrib-sse-client` 0.2.4 dates from 2022 and passes no event id;
   - `@tq-bit/node-red-contrib-server-sent-events` needs one fixed event name, and its example
     fails its own `JSON.parse` of headers.

   No node persists the id across restarts, so the flow keeps it in flow context.
5. **Redpanda Connect licenses.** None of the components is under the Redpanda Community License.
   The connectors are Apache-2.0. The core components (`generate`, `http`, `cache`, `mapping`,
   `unarchive`, `catch`, `log`, `archive`, `broker`, the `file` cache) come from the Benthos core,
   which is MIT, not Apache-2.0. `kafka_franz` is deprecated in favor of the `redpanda` output; it
   is kept and the deprecation is noted.
6. **Vector.** `http_client` allows VRL in query parameters but cannot carry a cursor, so the feed
   is read by `foresignal_sse.py` through an `exec` source. Vector's `redis` sink cannot write
   Streams (only list, sorted set and channel), so Redis/Valkey Streams use Redpanda Connect only.
   The Vector JetStream sink sets no message id; consumers dedupe on `seq` either way.
7. **Claude Desktop.** Its config file documents stdio servers only. Remote custom connectors (with
   request headers) connect from Anthropic's cloud and cannot reach a LAN broker, so
   `claude-desktop.json` uses the `mcp-remote` bridge, verified on npm: 0.14.3, MIT, `--header`,
   `--allow-http`, `--transport http-only`.
8. **`test_watch` needs an existing watch** (`watchId`), so "test before creating" is written as:
   create with `enabled: false`, `test_watch`, then `update_watch`.
9. **Grafana.**
   - The Live map and Alerts queries use Infinity's backend parser, whose root selector is JSONata
     (verified in the plugin's docs); the map flattens point features only.
   - The Operations dashboard uses only meter names found in the code.
   - Infinity is pinned at 4.1.1 (≥ 3.4.1 for CVE-2025-8341), Grafana 13.2.3 and Prometheus 3.15.0,
     each verified on its registry.
   - The plugin is installed with `GF_PLUGINS_PREINSTALL_SYNC`; `GF_INSTALL_PLUGINS` is deprecated.
10. **n8n IMAP.** The trigger's Resolved format exposes `headers` as one line per lowercase name
    (n8n's `utils.ts`), so a repeated `Authentication-Results` keeps only one line. The gate
    therefore requires your own `AUTHSERV_ID` at the start of the line plus `dkim=pass` and
    `dmarc=pass`, and fails closed when an extra header displaced it. The source uses
    `observedAtMode: RECEIVED`, with the mail date in `eventTime`.
11. **Phone automations.** ntfy's broadcast intent carries no action URLs, so Tasker cannot
    acknowledge. The person taps ntfy's own Acknowledge button (a capability POST), and no
    acknowledging token is on the phone.
12. **Recipe 4** writes findings with the source's ingest key, because no MCP tool writes records
    (Gap-Analysis's `submit_observation` is not built). The loop guard is advice: give watches
    explicit `sources` or `kinds`.
13. **Test design.**
    - `CookbookApiReferenceTest` also requires at least one *implemented* operation per path, not
      only that the path exists.
    - It treats a path ending in `/` as a prefix that code completes (`'/v1/ingest/' + id`).
    - The unit tests read the tool catalog from production classes (a classpath scan plus Mockito
      `CALLS_REAL_METHODS` for `name()` and `requiredScope()`); `AgentWorkflowIT` checks the live
      registry.
    - `SkillFrontmatterTest` also checks the scope column of the skill's and the guide's tool
      tables, and that the guide's table lists the whole registry.
14. US spelling ("license") in the new prose; wire names such as the `licence` field are unchanged.

## Issues found / open questions

1. **Owner: ClawHub relicenses published skills as MIT-0.** ClawHub's docs say "All skills
   published on ClawHub are licensed under MIT-0". Publishing `integrations/openclaw/foresignal/`
   releases it under MIT-0 (the repository is MIT). The README says so; only the owner should
   publish.
2. **Owner: `openReads` is on by default**, and `geo_json_events` relies on it. Until 035's feed
   keys exist, the Home Assistant map needs every read open to the network. The cookbooks say so and
   show how to turn it off.
3. **Grafana is AGPL-3.0.** It runs only as a separate container in the optional override, and
   nothing of it is linked or copied, so PRC-2 is unaffected. Flagged for awareness.
4. **Unverified at run time.** No test has run, and none of the third-party artifacts has been
   executed against the real tool (Node-RED, n8n, Redpanda Connect, Vector, Grafana, Home Assistant,
   OpenClaw, PyTAK). Formats come from official docs and, where docs were thin or stale, from the
   tools' published source. Points resting on reading rather than execution:
   - Redpanda Connect: the `cache` output's `target`, per-output `processors` inside a
     `fan_out_sequential` broker, and the `file` cache directory field;
   - n8n: the HTTP Request 4.2 parameter names;
   - Vector: the `${VAR:-default}` syntax and the `exec` `environment` map;
   - PyTAK: that `CLITool.setup()` adds the receive worker, and the protobuf field names of
     takproto (`cotEvent.uid`, `sendTime`, `staleTime`, `detail.contact.callsign`);
   - the Grafana Geomap and annotation field mappings.

   The JSON and YAML artifacts were parsed with Jackson 3.1.5 (the test's reader) and the Python
   files byte-compiled, outside the test suite. Home Assistant's `!secret` tags parse.
5. **Test assumptions**, first exercised at checkpoint 041:
   - `run_briefing`'s output carries `briefingId` and `status: succeeded`;
   - `/actuator/prometheus` is live in the test context, as `AdminTokenFilterIT` already assumes,
     and Boot 4.1's test auto-configuration has no metrics-disabling customizer;
   - the timer and gauge series named in `GrafanaMetricNamesIT` appear in a fresh scrape;
   - `AgentWorkflowIT`'s MCP calls produce a compiler note about a deprecated API (`McpServerIT`
     uses the same calls).
6. **Data.**
   - No instructions directed at an agent were found in fetched pages, packages or tool output.
   - Redpanda's and Grafana's docs pages point to an `llms.txt` index, which is harmless.
   - The `redpanda-data/connect` repository contains a `CLAUDE.md`, which was not opened.
   - Pages fetched for verification include docs.openclaw.ai (skill format, skills, MCP, hooks,
     configuration, ClawHub publishing), code.claude.com/docs/en/mcp,
     modelcontextprotocol.io, support.claude.com, docs.openwebui.com, librechat.ai, docs.n8n.io
     and n8n's source, home-assistant.io and its core source, the npm and PyPI registries,
     docs.ntfy.sh, docs.redpanda.com and the connect repository, vector.dev, grafana.com, Docker Hub
     and GitHub releases.
7. Owner decisions carried from 012–033 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `integrations/AgentWorkflowIT` | Over a real socket with the MCP SDK client and an **agent** token:<br>• `query_records` (empty, with watermark)<br>• `create_watch` with tags and a geofence<br>• ingest through an admin-created custom source, its `eik_` key as Bearer<br>• the alert at a WireMock webhook: HMAC computed independently, `alert.fired`, tags, `actions` with an ack URL under `publicBaseUrl`<br>• `list_alerts` firing by watch<br>• `acknowledge_alert`: state, `acknowledgedBy` = `token:<name>` (response, database, REST)<br>• `create_briefing` + `run_briefing` delivered to WireMock Slack<br>• no tools for sources (beyond the two read tools), credentials, channels, providers, tokens, settings, ingest or LLM; nine admin REST operations answer 403<br>• the skill's tools are all served |
| `integrations/SkillFrontmatterTest` | • frontmatter `name`, `description`, `requires.env` ⊇ both variables, `bins` `curl`, `primaryEnv`<br>• every `$VAR` in the body declared<br>• every tool in `SKILL.md` and the Agent Guide (tables and `mcp` blocks) exists in the production registry, with the documented scope<br>• the guide's table lists the whole registry<br>• safety wording; no credential<br>• helper unit checks |
| `integrations/CookbookApiReferenceTest` | • every `/v1` path in `docs/cookbooks`, `integrations` and the Agent Guide matches an implemented spec path (query strings dropped, placeholders matched only to variables, prefixes ending in `/`)<br>• the eight cookbooks exist and state token scope, limits and privacy<br>• no real-looking credential<br>• extraction and matching unit cases |
| `integrations/IntegrationArtifactsTest` | • every JSON/YAML artifact parses<br>• Node-RED: an array with unique ids, one tab, wires to existing nodes and the expected node types<br>• n8n: `name`, `nodes` (IMAP, Code, HTTP Request) with `typeVersion` and position, `connections` between existing nodes, `settings`<br>• Grafana: three dashboards with unique `uid`, `title`, integer `schemaVersion` and panels; data sources are exactly Prometheus and Infinity (no PostgreSQL); the dashboard provider<br>• Claude Desktop (pinned `mcp-remote`, `/mcp`, `--header`) and LibreChat (`streamable-http`, bearer `${FORESIGNAL_TOKEN}`)<br>• Home Assistant: a `local_only` webhook trigger, REST resources with headers<br>• TAK: `==` pins, `defusedxml` and never `xml.etree`, nothing under `src/main/java` |
| `integrations/GrafanaMetricNamesIT` | Starts the app and scrapes `/actuator/prometheus`:<br>• every Operations-dashboard metric is present<br>• activity-only meters (ingest counters, fetch timer, source status, data age) are each defined under that Micrometer name in the named production file<br>• the extraction unit case |

Test support: `testsupport/integrations/IntegrationDocs`. It covers file walking, path extraction
and matching, frontmatter, env references, tool tables and `mcp` blocks, the production tool
catalog and the credential pattern.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. One note: `AgentWorkflowIT` uses a
deprecated API.

`./gradlew lintOpenApi` was not needed: the spec did not change.

No new dependencies in the build.

The artifacts reference external tools (not dependencies). Versions and licenses, verified on
their registries on 2026-10-12:

| Tool | Version | License |
|---|---|---|
| `mcp-remote` | 0.14.3 | MIT |
| `mcpo` | 0.0.20 | MIT |
| `@parasaurolophus/node-red-eventsource` | 3.0.0 | MIT |
| PyTAK | 7.6.1 | Apache-2.0 |
| takproto | 3.0.1 | MIT |
| defusedxml | 0.7.1 | PSF |
| aiohttp | 3.14.5 | Apache-2.0 AND MIT |
| Infinity | 4.1.1 | Apache-2.0 |
| Grafana | 13.2.3 | AGPL-3.0, separate service |
| Prometheus | 3.15.0 | Apache-2.0 |
| Redpanda Connect | connectors, core | Apache-2.0, MIT |
| Vector | | MPL-2.0 |

## Notes for the next prompt

1. **035 (access perimeter):**
   - Update `docs/cookbooks/home-assistant.md` §2 and the Agent Guide's Home Assistant section to
     feed-key URLs.
   - `CookbookApiReferenceTest` then requires the `/v1/k/{key}/…` paths to be `implemented`.
   - Bridges and Grafana can use restricted read tokens once restrictions are enforced; the
     cookbooks already suggest them.
2. **Every new MCP tool must get a row in the Agent Guide's tool table** (`| Tool | Scope | … |`,
   scope cell starting with the tool's scope). `SkillFrontmatterTest.agentGuideTools` fails
   otherwise. Add it to `SKILL.md` if agents should know it.
3. **New cookbooks and artifacts** go under `docs/cookbooks/` and `integrations/`. Every `/v1` path
   in them must be an implemented spec path; JSON and YAML must parse.
4. **039 / 109:** the TAK, Home Assistant and mail sources are documented as `private`; the
   bridge cookbook says license-restricted re-publishing is the operator's responsibility until
   109's license egress reaches the change feed.
5. **Home Assistant `actions`:** see Deviation 1, if the owner wants capabilities on the
   `homeassistant` channel type itself.
