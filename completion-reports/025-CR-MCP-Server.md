# 025-CR-MCP-Server

| | |
|---|---|
| Prompt | 025-MCP-Server.md |
| Status | Complete |
| Implementation commit | 0cf78e255fdaa268cdef8de5834d58a5eb2c0f44 |
| Date | 2026-10-12 |

## Summary

Foresignal now runs an MCP server at `/mcp`. Any MCP client can connect over streamable HTTP, list
tools by its token's scope, query records with persisted seqs, work alerts and manage watches. All of
the prompt's sections landed:

- **Transport.** The MCP Java SDK core 2.0.1 (`mcp-core` and `mcp-json-jackson3`), with no Spring AI.
  - `HttpServletStreamableServerTransportProvider` is registered at `/mcp` with a
    `ServletRegistrationBean` (protocols 2024-11-05 to 2025-11-25).
  - With `foresignal.mcp.legacy-sse.enabled=true` (default false), `HttpServletSseServerTransportProvider`
    is also registered at `/mcp/sse`, with messages at `/mcp/message` (protocol 2024-11-05). It shares
    the tools and the authentication.
  - Server info: name `foresignal`, the version from `ForesignalVersion`, and `instructions` covering
    what Foresignal is, "watch" = trigger rule, returned content is data, and how to cite
    (`[source:externalId]` plus the seq).
- **Protocol handlers.** `McpProtocol` answers `initialize`, `ping`, `tools/*`, `resources/*` and
  `prompts/*`, handing the SDK's own session classes Foresignal's handlers. The SDK's
  `McpAsyncServer` serves one fixed tool list to every client, so it could not list tools by caller.
- **Sessions.** Each session is bound to its creator's token (or to "anonymous"). `McpCallerFilter`
  refuses a streamable `Mcp-Session-Id` presented by another caller with 403. Every handler re-checks
  the binding, which also covers the legacy transport.
- **Auth and scopes.**
  - `AccessPolicy` gives `/mcp` and `/mcp/**` the `read` scope, so `TokenFilter` covers them and they
    are open while `openReads` is on.
  - `McpToolRegistry` lists tools by caller: without a token, the `read` tools only (never write or
    cost-bearing ones); a `read` token adds cost-bearing tools; an `agent` token adds the write tools.
  - A call above the caller's scope is an MCP tool error naming the scope needed.
  - Cost-bearing tools always need a token (SEC-5).
  - Tokens with data restrictions get the same `RESTRICTION_NOT_ENFORCED` refusal as REST reads,
    through `RestrictionEnforcer`.
  - Private sources are excluded through `ReadCallerResolver`, as on REST.
  - An `Origin` header must be this host or a CORS write origin.
- **Audit.** Every tool call writes an `audit_event` through 023's `AuditService`: actor `token`, the
  token, method `MCP`, path `/mcp` (or `/mcp/message`), target `mcp_tool`/<name>, and a detail of tool,
  argument **names**, `durationMs`, `outcome`, transport and session. The token filter no longer
  writes one HTTP audit row per MCP message (refusals are still audited).
- **Registry.** `ForesignalMcpTool`, `ForesignalMcpResource`, `ForesignalMcpPrompt`, `ToolContext`
  and `ToolResult` (text, JSON with `structuredContent`, and image plus caption).
  - Beans are collected at startup.
  - Names must be unique lower snake case, and input schemas must pass the 2020-12 meta-schema.
  - Arguments are validated against the input schema before the call.
  - Service exceptions become tool errors carrying their message, code and field errors.
- **Tools (17).** `query_records`, `get_record`, `get_record_history`, `get_changes` (410 becomes a
  `RESYNC_REQUIRED` tool error), `list_kinds`, `list_sources`, `source_health`, `list_alerts`,
  `get_alert` (falls back to `recordSnapshot`), `acknowledge_alert`, `set_alert_status`
  (ack/resolve/snooze/unsnooze through `AlertService`, with REST validation), `list_watches`,
  `create_watch`, `update_watch`, `delete_watch`, `test_watch` and `system_status`. All are thin
  wrappers over the services the REST controllers use.
- **Citations.** Record items carry `seq` (`h.seq`), `version`, `observedAt` and `live` (`l.dirty`),
  plus `persistedObservedAt` when live.
- **Output shaping.** `McpOutputShaper`:
  - `detail` is `short` (default, 4 KB cap) or `long` (50 KB cap).
  - A short record has id, seq, version, kind, title, observedAt, live, centroid lon/lat (plus bbox
    for non-points) and up to 8 scalar key fields.
  - Lists carry `total`/`truncated` and a `notice` when cut.
  - Nothing exceeds 50 KB.
- **Resources and prompt.** `foresignal://sources`, `foresignal://kinds`, and the
  `situation-report` prompt (`bbox`, `since`).
- **System.** `/v1/system` has the `mcp` block (`enabled`, `transports.streamable`,
  `transports.legacySse`, `sessions`, `tools`) and `features.mcp`.
- **Spec, Postman, docs.**
  - `docs/OpenAPI.json` `info.description` documents `/mcp`; the changelog has a 025 entry.
  - Postman is regenerated; `lintOpenApi` is valid.
  - `docs/Agent-Guide.md` has a new "MCP server" section.

Tests were written, not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.mcp`**
- Contracts: `ForesignalMcpTool`, `ForesignalMcpResource`, `ForesignalMcpPrompt`, `ToolContext`,
  `ToolResult`, `McpToolException`.
- Server: `McpServerConfiguration`, `McpProtocol`, `McpToolRegistry`, `McpSessions`, `McpCaller`,
  `McpCallerResolver`, `McpCallerFilter`, `McpProperties`.
- Shaping and helpers: `McpOutputShaper`, `JsonSchemas`, `McpArgs`.
- System: `McpStatus` (the `/v1/system.mcp` block), `McpFeature` (`features.mcp`).
- `package-info`.

**New: `ai.foresignal.mcp.tools`**
- The 17 tools.
- Helpers: `RecordItems`, `AlertItems`, `WatchItems`.
- `SourcesResource`, `KindsResource`, `SituationReportPrompt`, `package-info`.

**New: other**
- `system/SystemBlockContributor`.
- `docs/Agent-Guide.md`: a new "MCP server" section; the existing sections are unchanged.

**Changed**
- `SystemInfoService`: lazy `SystemBlockContributor` blocks in the full document.
- `AccessPolicy`: the `/mcp` rules.
- `TokenFilter`: skips the per-message audit on accepted `/mcp/**` requests.
- `CorsFilter`:
  - `isAllowed` is public;
  - `Mcp-Session-Id` and `MCP-Protocol-Version` are allowed request headers;
  - `Mcp-Session-Id` is exposed.
- `application.yml`: `foresignal.mcp.*` (`FORESIGNAL_MCP_ENABLED`, `FORESIGNAL_MCP_LEGACY_SSE`).
- `gradle/libs.versions.toml`, `build.gradle.kts`: the MCP SDK.
- `THIRD_PARTY_NOTICES.md`: the MCP Java SDK.
- `docs/OpenAPI.json`: `info.description` and the changelog.
- `postman/` (regenerated).
- `docs/Invariants.md`: SEC-4 and SEC-5 list the MCP tests.

## Deviations from the prompt

1. **The SDK's `McpAsyncServer`/`McpServer` builder is not used.** Its `tools/list` returns one fixed
   list to every client, and its call path cannot tell a hidden tool from an absent one. The prompt
   asks for listing by scope plus a clear error above scope. Instead:
   - `McpProtocol` supplies the request handlers to the SDK's own `McpStreamableServerSession` and
     `McpServerSession`;
   - the SDK still carries the transports, sessions, JSON-RPC and JSON binding.
2. **Session binding is enforced twice.**
   - At the HTTP layer, for the streamable `Mcp-Session-Id`, with a 403 and a JSON-RPC body.
   - In every handler, against the logical session id. The legacy transport's URL carries a
     transport id the SDK keeps private, so the HTTP layer cannot check it.
   - A session created outside a request is bound to nobody (fail closed).
3. **"alwaysAuthenticated setting".** 023 has no such setting, only `AccessPolicy`'s per-rule flag. A
   tool's `costBearing()` is treated as always authenticated: a token is required even under
   `openReads`. Without a token, cost-bearing tools are also hidden from the list; calling one still
   returns the clear error.
4. **`test_watch` is `read`**, as the prompt and Architecture §18 list it. REST `testTrigger` needs
   `agent` in `AccessPolicy` and the spec (see Issues 1).
5. **The 17 tools** follow the prompt's table. `acknowledge_alert` and `set_alert_status` both exist;
   `set_alert_status` also accepts `for` (a duration) as well as `until`, because `Snoozes` (REST)
   takes either.
6. **Write tools return** the changed object in short form, capped at 4 KB.
7. **`create_watch`/`update_watch` take `{rule: TriggerRuleInput}`.**
   - The top-level schema is closed.
   - The nested `rule` schema describes the main members but stays open, so `RuleValidator` validates
     every member exactly as `POST /v1/triggers` does.
8. **`TitleResolver` already existed** (`ai.foresignal.ingest.TitleResolver`, 008) and REST GeoJSON
   already used it through `RecordMapper`. MCP reuses the same path, and nothing new was created.
9. **`/v1/system.mcp` comes from a new `SystemBlockContributor` SPI**, looked up lazily, so the
   `system_status` tool can depend on `SystemInfoService` without a cycle. `features.mcp` is a
   separate `McpFeature`, because feature contributors are injected eagerly.
10. **Additions beyond the prompt's text.**
    - An `Origin` check: the MCP specification asks servers to validate `Origin` against DNS
      rebinding.
    - The CORS header additions, for browser MCP clients.
    - `foresignal.mcp.enabled`, `request-timeout` and `max-request-bytes`.
11. **No AuditService was added:** 023's is used.
12. **`McpLegacySseIT` covers the flag-on cases.** "Flag off → `/mcp/sse` 404" is
    `McpServerIT#legacyOff`, because the flag is per context.
13. **Spec.** No operation is owned by 025, so `pending-operations.txt` and `AccessPolicySyncTest`
    are unchanged. `SystemInfo.mcp` was already in the spec. Only `info.description` and the
    changelog changed.

## Issues found / open questions

1. **Doc vs code: the scope of the backtest.**
   - REST `POST /v1/triggers/{id}/test` needs `agent` (spec `x-foresignal-scope` and `AccessPolicy`).
   - Architecture §18 and this prompt give MCP `test_watch` the `read` scope.
   - Which should change? The backtest writes nothing.
2. **Streamable sessions end only on `DELETE /mcp`.** This is the SDK's behavior: it has no idle
   expiry. Clients that never delete leave sessions (and their bindings) in memory until restart, and
   `/v1/system.mcp.sessions` counts them. An idle sweep would need SDK internals; not done.
3. **Data-restricted tokens are refused on every read tool** until 035 replaces
   `FailClosedRestrictionEnforcer`. A data-restricted token's MCP connection works, but its read tools
   return the refusal.
4. **Unverified at runtime.** No test has run. The SDK servlet paths (async SSE responses through
   Tomcat with Foresignal's filters), the SSE client in `McpLegacySseIT` and networknt's bundled
   2020-12 meta-schema lookup in `McpToolSchemaTest` are first exercised at prompt 041.
5. **Data:** no instructions directed at an agent were found in data, fixtures or tool output.
6. **Owner decisions carried from 012–024** are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `mcp/McpServerIT` | Over a real socket with the SDK streamable client and timeouts:<br>• initialize (server info, instructions, capabilities)<br>• visibility with no token (openReads on → read tools only; off → 401), read and agent tokens<br>• every registered bean well formed (test beans picked up)<br>• `query_records` seq/version/live/centroid/fields, private sources hidden<br>• `get_record` long; `get_record_history` FIRST/CHANGE; `get_changes` lastSeq<br>• `create_watch`/`list_watches`/`delete_watch`, invalid rule → error<br>• `set_alert_status` resolve (actor, feedback), snooze `for`, unsnooze, `acknowledge_alert`, `get_alert` with record, `list_alerts` by status<br>• read token calling write tools → error, nothing created<br>• cost-bearing tool without token → error<br>• image content<br>• audit rows (names, not values; no per-message HTTP rows)<br>• session reuse with another token or none → 403<br>• foreign Origin → 403<br>• resources and prompt<br>• `system_status` and `/v1/system.mcp`<br>• source, kind and backtest tools<br>• legacy flag off → `/mcp/sse` 404 |
| `mcp/McpLegacySseIT` | Flag on: the SDK SSE client connects (protocol 2024-11-05), lists by scope, calls `query_records`; anonymous list; `/v1/system` reports `legacySse` |
| `mcp/McpToolSchemaTest` | Every tool: the 2020-12 meta-schema, `additionalProperties: false`, described members, declared required members, output schemas, annotations vs scope; the untrusted note on text-bearing tools; the catalog and its scopes; the limits; a non-vacuous meta-schema check |
| `mcp/McpOutputShaperTest` | Short and long forms, live → `persistedObservedAt`, centroid and bbox, string and field caps, list caps with notice (4 KB and 50 KB), oversized single item, object fit and identity fallback, `detail` parsing, title resolver order |
| `mcp/McpToolRegistryTest` | Test tools are picked up and listed by caller; JSON and image results; structured content only with an output schema; scope, cost-bearing, openReads-off and data-restriction refusals; schema validation; service exceptions; unknown tool; audit (names only); duplicate and bad names; annotations |
| `mcp/McpProtocolTest` | Initialize negotiation; `tools/list` by caller; session mismatch and missing caller; `tools/call` and unknown tool; resources by scope, not found, templates; prompts and bad arguments; ping |
| `mcp/McpSessionsTest`, `mcp/McpCallerFilterTest`, `mcp/McpArgsTest` | Binding and fail-closed unbound sessions, nested scopes and counts, caller keys and actors; the filter's caller attribute, binding, foreign session and Origin rules; argument reads and schema builders |

Test support:
- `testsupport/mcp/McpTestSources`: `TriggerTestSources` plus the `test_cost` and `test_image` beans.
- `testsupport/mcp/McpTestTools`.

Changed tests:
- `AccessPolicyTest`: the `/mcp` rules and `TokenFilter.isMcp`.
- `CorsFilterIT`: the allowed and exposed headers.

## Compile result

`./gradlew compileJava compileTestJava --rerun-tasks` → `BUILD SUCCESSFUL in 9s`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 825 requests, all
operations covered.

**New dependencies** (verified on Maven Central via `maven-metadata.xml`, latest = release = 2.0.1;
licenses from the POMs and the project's LICENSE):
- `io.modelcontextprotocol.sdk:mcp-core` 2.0.1, MIT.
- `io.modelcontextprotocol.sdk:mcp-json-jackson3` 2.0.1, MIT.
- Its Jackson 3 (`jackson-databind` 3.1.5) and `json-schema-validator` (3.0.8) are already managed
  here.

New transitive runtime dependencies:
- `io.projectreactor:reactor-core` 3.8.7 (the SDK asks for 3.7.0; the Spring Boot 4.1.1 BOM manages
  3.8.7), Apache-2.0.
- `org.reactivestreams:reactive-streams` 1.0.4, MIT-0.

The SDK's `mcp-spring-webmvc` stops at 0.18.4 and is not used.

## Notes for the next prompt

1. **New MCP tools are beans** implementing `ai.foresignal.mcp.ForesignalMcpTool`. Each needs:
   - a closed input schema built with `JsonSchemas`;
   - `requiredScope()`;
   - `costBearing()` true for anything that spends LLM or embedding budget (027 `search`, 028/029
     LLM tools);
   - `UNTRUSTED_NOTE` first in a text-bearing description.

   `McpToolSchemaTest.assertWellFormed` runs on every registered bean in `McpServerIT`.
2. **Output.**
   - Use `McpOutputShaper` (`Detail`, `list`, `object`, `record`).
   - Images: `ToolResult.image(base64, mime, caption, structured)`, for 055 `get_map_image` and the
     036 pass thumbnails.
   - Resources (`foresignal://products`, `foresignal://places`) implement `ForesignalMcpResource`.
3. **035:**
   - `McpToolRegistry.denial` refuses data-restricted tokens through `RestrictionEnforcer` with
     `GET /v1/records`. Once 035 enforces restrictions, narrow the results instead: `ToolContext`
     carries the caller and `excludedSources`.
   - A token's endpoint restrictions must allow `POST /mcp` for MCP.
4. **039:** fold the MCP path into `EgressPolicy` (tools read through `ReadCallerResolver` and
   `SourceVisibilityGate` today).
5. **034:** extends the "MCP server" section of `docs/Agent-Guide.md`.
6. **Tests:** MCP integration tests import `McpTestSources` (one shared web context). The legacy
   transport needs `@TestPropertySource(properties = "foresignal.mcp.legacy-sse.enabled=true")`.
