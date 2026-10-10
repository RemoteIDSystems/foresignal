# 022 — LLM Providers

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/022-CR-LLM-Providers.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/021-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §19 (LLM subsystem), §7.1 (`llm_provider`, `llm_usage`), §14 (compose `local-llm` profile)
- `docs/Product-Charter.md` §8.11 (FR-44 to FR-46), NFR-13 to NFR-15
- `docs/OpenAPI.json`: operations tagged `LLM`, schemas `LlmProvider*`, `LlmSettings`, `LlmTestResult`, `LlmModelInfo`, `LlmUsage*`
- Anthropic Java SDK: https://github.com/anthropics/anthropic-sdk-java (README: client setup, messages, streaming, tool use, error types, beta features)

## Objective

Make an LLM a first-class, optional, budgeted part of Elaro. The operator can configure Anthropic Claude through the official SDK, any OpenAI-compatible API, or a local Ollama model. Later prompts (023–026, 060) build on the `LlmClient` abstraction from this prompt.

## Tasks

### 1. Provider model (`ai.elaro.llm`)

- **Entities:** `LlmProviderEntity` and `LlmUsageEntity` per §7.1. The API key is encrypted with `CredentialCipher` and never returned (`apiKeySet` only).
- **`LlmSettings`** in `app_setting['llm']`: `chatProvider`, `embeddingProvider`, `askEnabled`, `enrichmentEnabled`, `logContent`, `askMaxTokens`. Validate that the assigned providers exist and are enabled, that the chat provider has `chatModel`, and that the embedding provider has `embeddingModel` and `embeddingDimensions`. **Anthropic cannot be the embedding provider** (no embeddings endpoint), so return 400 with an explanation.
- **Provider defaults:**
  - `anthropic`: `baseUrl` `https://api.anthropic.com`, `chatModel` `claude-opus-5-5`, options `effort: medium`, `maxTokens: 16000`
  - `ollama`: `baseUrl` `http://ollama:11434`
  - `openai-compatible`: `baseUrl` required

### 2. `LlmClient` abstraction

- **Interface:**
  - `ChatResponse chat(ChatRequest)`, where `ChatRequest` has `system` (stable text), `messages`, `tools` (name, description, JSON schema), `maxTokens`, `effort`, `purpose` (`ask`, `briefing`, `enrichment`, `test`, `doctor`), and `stream` with an optional delta listener.
  - `ChatResponse` holds `content` (text), `toolCalls` (id, name, input JSON), `stopReason` (normalized: `end`, `tool_use`, `max_tokens`, `refusal`, `error`), `usage` (input, output, cached tokens), `model`, and `rawAssistantTurn`: an opaque, provider-specific object that **must be appended unchanged** to the conversation for the next turn.
  - `EmbeddingResponse embed(List<String> texts)`.
  - `List<LlmModelInfo> models()`.
- **Tool loop helper (`ToolLoop`):** run chat → execute tool calls in parallel on virtual threads → append **all** tool results in one message → repeat, up to `maxIterations`, a deadline and a token cap. Failed tools return error results instead of being dropped. Prompt 024 uses it.

### 3. Implementations

**`AnthropicLlmClient`**, using the official SDK `com.anthropic:anthropic-java` (verify the current version on Maven Central; 2.34.0 was current when this prompt was written). Follow the SDK README for exact class and method names, and fix any wrong names by compiling.

- **Client:** `AnthropicOkHttpClient.builder().apiKey(decryptedKey).baseUrl(baseUrl)`, built once per provider configuration and rebuilt on change.
- **Thinking:** model default `claude-opus-5-5`. Do **not** send `thinking: disabled` or `budget_tokens`; on Opus 5.5 both return 400. Thinking is adaptive and always on, and depth is controlled with `output_config.effort`. Set effort explicitly from the request (Opus 5.5 defaults to `medium`).
- **Tool choice:** `auto` only. Forced `tool_choice` (`any`/`tool`) returns 400 on this model.
- **Conversation history is append-only.** Pass the assistant's content blocks back **unchanged**, including thinking blocks, and never edit earlier turns (preserved thinking). `rawAssistantTurn` holds the SDK's content blocks for this.
- **Refusals:** enable the server-side refusal fallback by adding the beta `server-side-fallback-2026-07-01` and the top-level body property `fallbacks: "default"` (through the SDK's beta messages path or additional-body-property support). Always check `stop_reason == refusal` before reading content, and map it to `stopReason=refusal` with the `stop_details` category.
- **Streaming:** use the SDK's streaming API when `stream=true` or `maxTokens > 16000`, and accumulate the final message with the SDK helper.
- **Prompt caching:** put `cache_control: ephemeral` on the last system block and on the tool definitions, so repeated Ask and briefing calls reuse the stable prefix. Keep the system prompt and tool order deterministic. Record `cache_read_input_tokens` as `cachedTokens`.
- **Errors:** map the SDK's typed exceptions (rate limit, overloaded, auth, invalid request) to Elaro errors. Rate-limit errors honour `retry-after`. Let the SDK's built-in retries handle transient failures (configure `maxRetries` 2).
- **Models:** `models()` uses the Models API (`client.models().list()`).
- **No embeddings:** `embed()` throws `UnsupportedOperationException` with a clear message.

**`OpenAiCompatibleLlmClient`** (`java.net.http`):

- `POST {baseUrl}/chat/completions` with `tools` (function calling), `max_tokens`, and `temperature` if set.
- `POST {baseUrl}/embeddings`; `GET {baseUrl}/models`.
- Bearer API key; extra headers from options (e.g. OpenRouter's `HTTP-Referer`).
- Normalize tool calls and finish reasons.

**`OllamaLlmClient`** (`java.net.http`):

- `POST {baseUrl}/api/chat` (`stream:false`, or NDJSON streaming), with `tools` for models that support them.
- `POST /api/embed` (batch input); `GET /api/tags` for models.
- When a model rejects tools, set `supportsTools=false` in the provider status so Ask uses retrieval-only mode (prompt 024).

### 4. Budgets, usage, health

- **`LlmBudgetGovernor`:** Bucket4j `requestsPerMinute` per provider, plus a daily token budget.
  - Before a call, estimate tokens (characters / 4 for the input, plus `maxTokens`). Refuse with `LlmBudgetExceededException` (→ 429 `LLM_BUDGET_EXCEEDED`) if the estimate would exceed the remaining budget.
  - After the call, record the actual usage.
  - Seed the daily figures from `llm_usage` at startup.
- **`LlmUsageMeter`:** per-minute counters flushed every 60 s (the same pattern as `UsageMeter`), and a `series()` query for `/v1/llm/usage`.
- **Provider health:** last success, last error, consecutive failures; status uses the same derivation style as source health.
- **System notifications:** when a daily budget is exhausted or a provider starts failing (`NotificationService.publish`, event `system`).
- **Content logging:** every call logs purpose, provider, model, token counts, duration and stop reason. Prompt and response bodies are logged only when `logContent=true`, at DEBUG level, never including API keys.

### 5. API

- `listLlmProviders`, `createLlmProvider` (201), `getLlmProvider`, `updateLlmProvider` (omit `apiKey` to keep it; changing the embedding model or dimensions of the active embedding provider publishes `EmbeddingModelChangedEvent`, which prompt 023 handles), `deleteLlmProvider` (409 if assigned to a role).
- `testLlmProvider`: chat "Reply with the single word: ok" (`maxTokens` 16, purpose `test`) plus an embedding of "test" if configured. Returns `LlmTestResult`; consumes budget.
- `listLlmProviderModels`; `getLlmSettings` / `updateLlmSettings`; `getLlmUsage`.
- **Export/import:** providers (without keys) and settings.
- **`/v1/system` `llm` block:** configured, providers per role, tokens today.

### 6. Docker Compose

- Add the `ollama` service under the profile `local-llm` (`ollama/ollama` image, volume `ollama`, port 11434), per Architecture §14.
- Document in `docs/Operator-Guide.md` (an "LLM" section):
  - `docker compose --profile local-llm up -d`, then `docker compose exec ollama ollama pull qwen3:14b` and `ollama pull nomic-embed-text`
  - configuring a Claude provider (API key from the Anthropic Console)
  - roles, budgets and costs
  - the data-egress statement: with a frontier provider, the records the model reads are sent to that provider; the local profile keeps everything on the box

## Tests to write (do not run)

**No test calls a real model.** Use WireMock stubs of each provider's HTTP API, and a `FakeLlmClient` for consumers.

- `AnthropicLlmClientIT`, with WireMock standing in for `api.anthropic.com` (point the SDK at it via `baseUrl`):
  - the request body contains model `claude-opus-5-5`, `output_config.effort`, `fallbacks: "default"` plus the beta header, `cache_control` on the system prompt and tools, and **no** `thinking.disabled` or `budget_tokens`
  - tool-use round trip with `rawAssistantTurn` appended unchanged
  - a refusal stop reason is mapped
  - 429 with retry-after
  - a streaming response is assembled
  - usage, including cached tokens, is recorded
- `OpenAiCompatibleLlmClientIT` and `OllamaLlmClientIT`: chat, tool calls, embeddings, models, error mapping, tools-unsupported detection.
- `ToolLoopTest` (with `FakeLlmClient`): parallel tool execution, all results in one message, iteration, deadline and token caps, error results.
- `LlmBudgetGovernorTest`: per-minute and daily limits, estimate refusal, seeding.
- `LlmControllersIT`: CRUD, key never returned, Anthropic as embedding provider → 400, delete while assigned → 409, test endpoint against a WireMock provider, usage series.

## Acceptance criteria

- With no provider configured, every non-LLM feature behaves exactly as before.
- With a provider configured, the test endpoint succeeds against a stub.
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
2. **Commit** all changes with the message `022: LLM Providers`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/022-CR-LLM-Providers.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `022: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
