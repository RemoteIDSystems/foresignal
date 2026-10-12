# 026-CR-LLM-Providers

| | |
|---|---|
| Prompt | 026-LLM-Providers.md |
| Status | Complete |
| Implementation commit | a4748fe208d26dea7720dc275f3a78a00178deb4 |
| Date | 2026-10-12 |

## Summary

An LLM is now an optional, budgeted part of Foresignal. The operator can configure three kinds of
provider: Anthropic Claude (through the official SDK), any OpenAI-compatible API, or a local Ollama.
With no provider configured, nothing in the new code runs, and the spec, `/v1/system` and the
settings bundle show it as unconfigured.

- **Provider model (`ai.foresignal.llm`).**
  - `LlmProviderEntity` (`llm_provider`) and `LlmUsageEntity` (`llm_usage`) follow §7.1. The API key
    is encrypted with `CredentialCipher` and never returned; the API shows `apiKeySet` only.
  - `LlmProviderValidator` applies the type defaults:
    - `anthropic`: `https://api.anthropic.com`, `claude-opus-5-5`, effort `medium`, `maxTokens` 16000;
    - `ollama`: `http://ollama:11434`;
    - `openai-compatible`: `baseUrl` is required.
  - It checks the shape of `baseUrl` (http/https, a host, no user info, no fragment) and leaves a
    `// 035: OutboundUrlPolicy` seam. Private-network URLs stay allowed.
  - It also validates every option and the optional `knowledgeCutoff` (ISO date, read by 122).
- **`LlmSettings`** live in `app_setting['llm']`: `chatProvider`, `embeddingProvider`, `askEnabled`,
  `enrichmentEnabled`, `logContent`, `askMaxTokens` and `allowPrivateLocal`.
  - `LlmSettingsService` checks that an assigned provider exists and is enabled, and that it has
    the model(s) its role needs.
  - Anthropic as the embedding provider is a 400 that explains why.
  - Reassigning the embedding role publishes `EmbeddingModelChangedEvent(oldSpec, newSpec)` after
    commit.
- **`LlmClient` abstraction (`ai.foresignal.llm.client`).**
  - `ChatRequest` carries `system`, `messages`, `tools`, `maxTokens`, `effort`, `purpose`, `stream`
    and a delta listener.
  - `ChatResponse` carries `content`, `toolCalls`, the normalized `stopReason`, `refusalCategory`,
    `usage` (input, output, cached), `model` and an opaque `rawAssistantTurn`.
    `assistantMessage()` appends that raw turn unchanged.
  - `embed(texts)` and `embed(texts, EmbeddingModelSpec)`; `EmbeddingModelSpec.modelId()` is
    `provider:model:dims`.
  - `models()`, and `LlmProviderException` with its kinds.
- **`ToolLoop`.** Each round runs chat, then every tool call in parallel on virtual threads, then
  sends all the results back in one message.
  - It stops on end, refusal, `max_tokens` or an error stop reason, checked before any tool runs.
  - It also stops at the iteration, deadline and token caps.
  - A failed or timed-out tool becomes an `is_error` result instead of being dropped.
- **`EgressPolicy`** is the shared `ai.foresignal.security.EgressPolicy` and the first version of
  it.
  - `filter(records, destination)` drops records whose source descriptor has `private=true`,
    unless the provider is `ollama` or has `options.local=true`.
  - It also drops records from unregistered sources (fail closed).
  - A generic overload takes any item type.
- **`AnthropicLlmClient`** uses `com.anthropic:anthropic-java` 2.71.0 on the beta messages path.
  - The client is built per configuration by `LlmHttp` (`maxRetries(2)`) and rebuilt when the
    configuration changes.
  - Effort is always set explicitly through `output_config.effort`. `thinking` and `tool_choice`
    are never sent.
  - Closed schemas get `strict: true`.
  - The refusal fallback is on: beta `server-side-fallback-2026-07-01` plus
    `fallbacksDefault()` (`fallbacks: "default"`).
  - `stop_reason == refusal` is checked before content is read; the category comes from
    `stop_details.category`.
  - `cache_control: ephemeral` is set on the system block and the last tool.
  - `rawAssistantTurn` is `BetaMessage.toParam()`, which keeps every block, thinking included.
  - Streaming (`createStreaming` plus `BetaMessageAccumulator`) is used when asked for or above
    16000 tokens.
  - Typed SDK exceptions are mapped most-specific-first; a rate limit keeps `retry-after`.
  - `models()` uses the Models API. `embed()` throws `UnsupportedOperationException`.
- **`OpenAiCompatibleLlmClient`** (via `LlmHttp`):
  - `/chat/completions` with tools, `max_tokens` and `temperature`, plus SSE streaming with tool-call
    fragments joined;
  - `/embeddings`, sending `dimensions` only for an explicit spec;
  - `/models`;
  - bearer key and `extraHeaders`; `content_filter` maps to `refusal`.
- **`OllamaLlmClient`** (via `LlmHttp`):
  - `/api/chat`, either `stream: false` or NDJSON, with tools and `num_predict`;
  - `/api/embed` with a batch input (and `dimensions` for an explicit spec);
  - `/api/tags`;
  - "does not support tools" maps to `TOOLS_UNSUPPORTED`, and the service then records
    `supportsTools=false`.
- **Budgets, usage, health.**
  - `LlmBudgetGovernor` combines a Bucket4j per-minute bucket with a daily token budget aligned to
    UTC.
    - The estimate is characters / 4 plus the output cap, reserved before the call and settled
      with the actual tokens afterwards.
    - A refusal is `429 LLM_BUDGET_EXCEEDED` with `Retry-After`.
    - The day is seeded from `llm_usage`.
  - `LlmUsageMeter` keeps per-minute counters flushed every 60 s (the same pattern as
    `UsageMeter`) and serves `series()`.
  - `LlmHealthTracker` derives the status like source health and raises a `system` notification
    when a provider becomes `failing`. The first daily-budget refusal of a day also notifies.
- **`LlmService`** is the front door. It hands out governed clients by provider or role
  (`chatClient`, `embeddingClient`, `requireChatClient`). Each governed call is budgeted, metered,
  health-tracked and logged with purpose, provider, model, tokens (cached included), duration and
  stop reason. Prompt and response bodies are logged only with `logContent`, at DEBUG.
- **API.**
  - All 10 `LLM` operations are implemented and set to `implemented` in `docs/OpenAPI.json`, and
    removed from `pending-operations.txt`.
  - `AccessPolicy` already had `/v1/llm/**` admin, with the test endpoint always authenticated.
  - `/v1/system` gains the `llm` block and `features.llm`.
  - The settings export/import gains `llmProviders` (without keys) and `llmSettings`.
  - Postman is regenerated; `lintOpenApi` reports the API description valid.
- **Compose and docs.**
  - A new `ollama` service (`ollama/ollama:0.40.3`, profile `local-llm`, volume `ollama`, port
    `${FORESIGNAL_BIND:-127.0.0.1}:11434`).
  - A new "LLM" section in `docs/Operator-Guide.md`, which covers the local profile and model
    pulls, Claude setup, roles, effort, budgets and costs, logging, and the data-egress statement.

Tests were written but not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.llm`**
- `LlmProviderType`, `LlmProviderEntity`, `LlmProviderRepository`, `LlmUsageEntity`, `LlmUsageId`
- `LlmSettings`, `LlmSettingsService`
- `LlmProviderConfig`, `LlmProviderInput`, `LlmProviderValidator`, `LlmProviderService`
- `LlmHttp`, `LlmClientFactory`, `LlmService`
- `LlmBudgetGovernor`, `LlmBudgetExceededException`, `LlmUsageMeter`, `LlmUsagePoint`,
  `LlmHealthTracker`
- `EmbeddingModelChangedEvent`, `LlmStatus` (the `/v1/system` `llm` block), `LlmFeature`,
  `AfterCommit`, `package-info`

**New: `ai.foresignal.llm.client`**
- `LlmClient`, `ChatRequest`, `ChatResponse`, `ChatMessage`, `ToolSpec`, `ToolCall`, `ToolResult`
- `TokenUsage`, `StopReason`, `Effort`, `Purpose`
- `EmbeddingModelSpec`, `EmbeddingResponse`, `LlmModelInfo`, `LlmProviderException`
- `ToolLoop`, `AnthropicLlmClient`, `OpenAiCompatibleLlmClient`, `OllamaLlmClient`, `HttpJson`,
  `package-info`

**New: other**
- `security/EgressPolicy`
- `api/LlmController`, `api/mapper/LlmMapper`

**Changed**
- `api/error/ErrorCodes`: `LLM_BUDGET_EXCEEDED`, `LLM_PROVIDER_EXISTS`, `LLM_PROVIDER_IN_USE`,
  `LLM_PROVIDER_ERROR`, `LLM_PROVIDER_RATE_LIMITED`.
- `settings/SettingKeys` (`llm`), `settings/SettingsTransferService` (`llmProviders`, `llmSettings`).
- `gradle/libs.versions.toml`, `build.gradle.kts`: the Anthropic SDK.
- `docker-compose.yml`: the `ollama` service.
- `docs/OpenAPI.json`: statuses, reconciliations, error codes, changelog.
- `postman/` (regenerated) and `scripts/generate-postman.py` (the `updateLlmProvider` and
  `updateLlmSettings` bodies).
- `docs/Operator-Guide.md` (new "LLM" section; settings export paragraph).
- `docs/Invariants.md`: EGR-1 also lists `EgressPolicyTest`.
- `THIRD_PARTY_NOTICES.md`: the Anthropic Java SDK.
- `src/test/resources/openapi/pending-operations.txt`: the 10 LLM lines removed.
- `UpstreamClientIsTheOnlyHttpClientTest`: `LlmHttp` added to the sanctioned outbound clients.

## Deviations from the prompt

1. **The beta messages path is used for every Anthropic call.**
   - The typed `fallbacksDefault()` and the beta flag exist only there, so no raw additional body
     property is needed.
   - `rawAssistantTurn` is therefore a `BetaMessageParam`.
2. **Provider health is stored on `llm_provider`.**
   - New columns: `last_success_at`, `last_error_at`, `last_error`, `consecutive_failures`,
     `supports_tools`.
   - Also `knowledge_cutoff` and `created_at`, beyond the §7.1 column list, so health survives a
     restart.
3. **When the daily budget is seeded.**
   - The prompt asks for seeding "at startup". The budget is seeded from `llm_usage` the first
     time a provider is used after start, or after its budget changes. This is lazy, as in
     005's `BudgetGovernor`, and needs no startup phase.
   - At UTC midnight the day restarts from zero.
4. **Request budget per call.** The per-minute bucket counts every call, embeddings included, so
   the test endpoint spends two requests when the provider has an embedding model.
5. **`allowPrivateLocal` (spec, extended by 039)** is accepted only as `false`. `true` is a 400
   that names 039. This follows the 023/024 pattern of refusing members that later prompts enforce.
6. **Anthropic needs an `apiKey`** on create and replace. Settings import may leave an Anthropic
   provider without one, and `LlmHttp` then refuses to build its SDK client, so the SDK never falls
   back to host credentials (environment, profile files).
7. **`options.temperature` is refused for `anthropic`.** Current Claude models removed sampling
   parameters.
   - `options.local` (a boolean) was added to `LlmProviderInput.options`: `LlmProvider.local`
     already referred to it.
   - The reserved headers (`Authorization`, `x-api-key`, `Content-Type`, `anthropic-*`, …) cannot
     be set through `extraHeaders`.
8. **`updateLlmProvider`.**
   - A body `id` must match the path.
   - A change that would leave a role the provider holds unserved (disabled, no chat model, no
     embedding model) is a 400.
   - The Postman `updateLlmProvider` and `updateLlmSettings` bodies now update the Ollama provider
     the collection creates, instead of an absent `claude` provider. The Claude example moved to
     the Operator Guide.
9. **`testLlmProvider`** reports provider failures and refusals in the body (200,
   `success: false`). Only a budget refusal is an error status (429).
10. **`getLlmUsage`'s default end** is the end of the current minute, so unflushed counters of the
    minute in progress are included.
11. **OpenAI-compatible and Ollama** send `dimensions` only for an explicit `EmbeddingModelSpec`.
    The configured default stays compatible with models that reject the parameter. Every
    embedding response is checked against the expected width.
12. **New error codes**, recorded in the spec's Problem list and changelog: `LLM_PROVIDER_EXISTS`,
    `LLM_PROVIDER_IN_USE`, `LLM_PROVIDER_ERROR` (502) and `LLM_PROVIDER_RATE_LIMITED` (429, with
    the provider's `Retry-After`).
13. **The test name** `EgressPolicy (LLM-context path…)Test` is `EgressPolicyTest`.

## Issues found / open questions

1. **Unverified at run time.** No test has run. These are first exercised at checkpoint 041:
   - the SDK against WireMock (beta path query string, SSE accumulation, `toParam()` keeping
     thinking signatures byte-for-byte);
   - the SDK's Jackson 2 (resolved to 2.21.5 by the Boot BOM) next to Spring's Jackson 3;
   - the native `llm_usage` upsert.
2. **The SDK brings Kotlin stdlib/reflect, OkHttp/Okio and Jackson 2 onto the runtime classpath.**
   All are Apache-2.0 or MIT (listed in `THIRD_PARTY_NOTICES.md`), so `LicenseAllowlistTest`
   should pass.
3. **Doc vs code: `Architecture.md` §19** lists the settings members without `askMaxTokens` and
   `allowPrivateLocal`, which the spec and the prompt include. Should the Architecture be updated?
4. **The SDK retries 429/5xx twice on its own** (as the prompt asked, `maxRetries(2)`). A
   rate-limited Anthropic call can therefore take up to about 2 × `retry-after` before Foresignal
   sees the error.
5. **Data:** no instructions directed at an agent were found in data, fixtures, docs or tool
   output.
6. **Owner decisions carried from 012–025** are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `llm/client/AnthropicLlmClientIT` | The SDK against WireMock via `baseUrl`:<br>• the request shape: `claude-opus-5-5`, explicit `output_config.effort`, `fallbacks: "default"` plus the beta header, `cache_control` on system and the last tool, `strict` on the closed schema, no `tool_choice`, no `thinking`/`budget_tokens`/`disabled`, no temperature, no dates in system<br>• effort resolution (request → provider → purpose)<br>• tool-use round trip with the assistant turn appended JSON-identical, thinking signature included, and both results in one user message<br>• refusal (category mapped, content not read); `max_tokens`<br>• 429 with `retry-after` (3 attempts, `RATE_LIMITED`, `Retry-After`)<br>• 401/400/529 mapping<br>• streaming (deltas, accumulation, cached tokens, forced above 16000)<br>• models; embed unsupported; no key means no client |
| `llm/client/OpenAiCompatibleLlmClientIT` | • chat (bearer, extra headers, tools, `max_tokens`, temperature, cached tokens)<br>• tool calls with raw replay and tool messages<br>• `content_filter`/`length` mapping<br>• SSE streaming with joined tool-call fragments<br>• embeddings (configured and explicit spec, `dimensions`, index order, width check)<br>• models<br>• 401/429/503/garbage mapping; unreachable is `CONNECTION` |
| `llm/client/OllamaLlmClientIT` | • chat (`stream: false`, tools, `num_predict`)<br>• tool calls (synthesized ids, raw replay, `tool_name`)<br>• tools-unsupported detection<br>• 404/500 mapping<br>• NDJSON streaming<br>• embeddings (batch, explicit spec `dimensions`)<br>• `/api/tags` |
| `llm/client/ToolLoopTest` | With `FakeLlmClient`:<br>• parallel execution on virtual threads; all results in one message; append-only history<br>• error results; tool timeout<br>• iteration, token and deadline caps<br>• refusal, `max_tokens` and error stops with no tool run<br>• limit validation |
| `security/EgressPolicyTest` | • private-source records dropped for anthropic and openai-compatible, kept for ollama and `local` providers<br>• unknown sources dropped<br>• the generic overload |
| `llm/LlmBudgetGovernorTest` | • the estimate<br>• the per-minute limit with `Retry-After` and refill<br>• the daily limit, a single notification and settle<br>• `Retry-After` to UTC midnight<br>• seeding from usage<br>• UTC rollover<br>• a changed budget<br>• invalidate/reset |
| `llm/LlmProviderValidatorTest` | • type defaults<br>• `baseUrl` accept/reject cases (private addresses allowed)<br>• every member and option error<br>• Anthropic limits<br>• path/body id<br>• `LlmSettings.parse` |
| `api/LlmControllersIT` | • CRUD (201 with Location, defaults, key never returned or stored in plain text, replace keeps the key, 409 duplicate, 204/404)<br>• invalid `baseUrl` 400; Anthropic without a key 400; id mismatch<br>• Anthropic as embedding provider 400; delete while assigned 409; role-breaking update 400; `allowPrivateLocal` 400<br>• `EmbeddingModelChangedEvent` (null → spec, old → new, none on an unrelated change)<br>• test endpoint against WireMock (prompt, 16 tokens, health, `tokensToday`, budget 429)<br>• usage series (minute and hour, refused count, validation)<br>• failing provider (result, degraded health, models 502)<br>• models<br>• admin-only; `/v1/system` `llm` and `features.llm`<br>• export/import |

Test support:
- `testsupport/llm/FakeLlmClient`: a scripted, deterministic `LlmClient` for consumers (028–030,
  027).

Changed tests:
- `UpstreamClientIsTheOnlyHttpClientTest`: `LlmHttp` is a sanctioned outbound client.

## Compile result

`./gradlew compileJava compileTestJava --rerun-tasks` → `BUILD SUCCESSFUL in 5s`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 825 requests, all
operations covered.

**New dependency** (verified 2026-10-12 on Maven Central `maven-metadata.xml`, where
latest = release = 2.71.0; license from the POMs):
- `com.anthropic:anthropic-java` 2.71.0, MIT. It brings `anthropic-java-core` and
  `anthropic-java-client-okhttp` 2.71.0, both MIT.

New transitive runtime dependencies:
- `com.squareup.okhttp3:okhttp` 4.12.0 and `com.squareup.okio:okio(-jvm)` 3.6.0, Apache-2.0.
- `org.jetbrains.kotlin:kotlin-stdlib`/`kotlin-reflect` 2.3.21 (Boot BOM), Apache-2.0.
- `com.fasterxml.jackson.module:jackson-module-kotlin` and `jackson-datatype-jdk8`/`jsr310`
  2.21.5 (Boot BOM), Apache-2.0.
- `com.github.victools:jsonschema-generator` and its modules 4.38.0, Apache-2.0.
- `io.swagger.core.v3:swagger-annotations` 2.2.50, Apache-2.0.
- `com.google.errorprone:error_prone_annotations` 2.50.0, Apache-2.0.
- `com.standardwebhooks:standardwebhooks` 1.2.0, MIT.

Compose image: `ollama/ollama:0.40.3`, the latest non-rc tag on Docker Hub on 2026-10-12.

## Notes for the next prompt

1. **Calling a model.** Use `LlmService`:
   - `requireChatClient()` (409 `LLM_NOT_CONFIGURED`), or `chatClient()` / `embeddingClient()` /
     `client(providerId)`.
   - The returned clients are already budgeted, metered, health-tracked and logged. Never build
     raw clients (`LlmClientFactory`) in features.
   - Effort defaults by `Purpose` (Ask/briefing medium, enrichment/search low); pass `effort` only
     to override.
2. **Prompt content.**
   - Pass record content through `EgressPolicy.filter(rows, llm.config(providerId).destination())`
     before building a prompt.
   - Keep the system prompt stable and the tool order deterministic, and put dates in the last
     user turn.
   - Delimit tool results as untrusted data.
3. **Tool use (028).**
   - `new ToolLoop(client, executor, new ToolLoop.Limits(12, Duration.ofSeconds(120), settings.askMaxTokens()), clock).run(request)`.
   - Check `Result.cause()` (`REFUSAL` → answer 200 with a warning).
   - When the chat call throws `LlmProviderException` with `kind() == TOOLS_UNSUPPORTED`, or the
     provider row has `supportsTools == false`, switch to retrieval-only.
4. **027.**
   - Listen for `EmbeddingModelChangedEvent` (after commit; either spec may be null).
   - Embed queries with `embeddingClient().embed(texts, outgoingSpec)` during a reindex.
   - `EmbeddingModelSpec.modelId()` is the `model_id` tag.
   - `FakeLlmClient` gives deterministic vectors for tests.
5. **Cost-bearing MCP tools (027/028/029)** set `costBearing() = true` and call through
   `LlmService`.
6. **035.**
   - Enforce the outbound URL policy at the `// 035: OutboundUrlPolicy` seams in
     `LlmProviderValidator.validateBaseUrl` and `LlmHttp.checkBaseUrl`.
   - Every provider client, the Anthropic SDK client included, is built in `LlmHttp`.
7. **039** hardens the LLM path of `EgressPolicy` (`allowPrivateLocal`, currently refused when
   `true`) and folds `SourceVisibilityGate` into it.
8. **Tests:**
   - LLM ITs import `NotifyTestConfig` (shared context).
   - The budget, meter and governor reset with the database (`IngestStateReset`).
