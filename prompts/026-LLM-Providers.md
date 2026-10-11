# 026 — LLM Providers

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/026-CR-LLM-Providers.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/025-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §19 (LLM subsystem), §7.1 (`llm_provider`, `llm_usage`), §14 (compose `local-llm` profile)
- `docs/Product-Charter.md` §8.11 (FR-44 to FR-46), NFR-13 to NFR-15
- `docs/Gap-Analysis.md` §1 item 7 (private sources stay out of LLM egress), §8.B item 20 (prompt 035 retrofits the outbound URL policy onto the LLM `baseUrl`)
- `docs/OpenAPI.json`: tag `LLM`, schemas `LlmProvider*`, `LlmSettings`, `LlmTestResult`, `LlmModelInfo`, `LlmUsage*`
- Anthropic Java SDK: https://github.com/anthropics/anthropic-sdk-java (README: client setup, messages, streaming, tool use, error types, beta features, additional body properties)

## Objective

Make an LLM a first-class, optional, budgeted part of Foresignal. The operator can configure Anthropic Claude through the official SDK, any OpenAI-compatible API, or a local Ollama model. Later prompts (027–030, 056, 102, 116–132, 134) build on the `LlmClient` abstraction from this prompt.

> **v1.3 note:** add an optional `knowledgeCutoff` (ISO date) to each provider's model configuration (and to `/v1/llm/providers` schemas). Prompt 122 uses it to keep retrospective evaluation of LLM forecasters free of training-data leakage; nothing in this prompt reads it.

## Tasks

### 1. Provider model (`ai.foresignal.llm`)

- **Entities:** `LlmProviderEntity` and `LlmUsageEntity` per §7.1. The API key is encrypted with `CredentialCipher` and never returned (`apiKeySet` only).
- **`LlmSettings`** in `app_setting['llm']`: `chatProvider`, `embeddingProvider`, `askEnabled`, `enrichmentEnabled`, `logContent`, `askMaxTokens`. Validate that assigned providers exist and are enabled, that the chat provider has `chatModel`, and that the embedding provider has `embeddingModel` and `embeddingDimensions`. **Anthropic cannot be the embedding provider** (no embeddings endpoint): 400 with an explanation.
- **Provider defaults:**
  - `anthropic`: `baseUrl` `https://api.anthropic.com`, `chatModel` `claude-opus-5-5`, options `effort: medium`, `maxTokens: 16000`
  - `ollama`: `baseUrl` `http://ollama:11434`
  - `openai-compatible`: `baseUrl` required
- **`baseUrl` validation now:** http/https only, no userinfo, no fragment. Prompt 035 retrofits the outbound URL policy (SSRF guard) onto this field; add a `// 035: OutboundUrlPolicy` seam in `LlmProviderValidator` and route every provider HTTP client through one `LlmHttp` factory so 035 can enforce it in one place. Private-network URLs stay allowed (local Ollama is the point).

### 2. `LlmClient` abstraction

- **Interface:**
  - `ChatResponse chat(ChatRequest)`. `ChatRequest`: `system` (stable text), `messages`, `tools` (name, description, JSON schema), `maxTokens`, `effort`, `purpose` (`ask`, `briefing`, `enrichment`, `search`, `test`, `doctor`, `analyst`), `stream` with an optional delta listener.
  - `ChatResponse`: `content` (text), `toolCalls` (id, name, input JSON), `stopReason` (normalized: `end`, `tool_use`, `max_tokens`, `refusal`, `error`), `refusalCategory`, `usage` (input, output, cached tokens), `model`, and `rawAssistantTurn`: an opaque, provider-specific object that **must be appended unchanged** to the conversation for the next turn.
  - `EmbeddingResponse embed(List<String> texts)` and `embed(List<String> texts, EmbeddingModelSpec spec)` (explicit model + dimensions; prompt 027 needs it to embed queries with the *outgoing* model while a reindex is in progress).
  - `List<LlmModelInfo> models()`.
- **`EmbeddingModelSpec(providerId, model, dimensions)`** with `modelId()` = `"<providerId>:<model>:<dimensions>"` (used by 027 as the `model_id` tag).
- **Tool loop helper (`ToolLoop`):** chat → execute tool calls in parallel on virtual threads → append **all** tool results in **one** message → repeat, up to `maxIterations`, a deadline and a token cap. Failed tools return error results (`is_error`) instead of being dropped. Check `max_tokens`/`refusal` stop reasons before running tools. Prompt 028 uses it.
- **`EgressPolicy (LLM-context path; see docs/Conventions.md)`** (hook): every caller that puts record content into a prompt passes it through `EgressPolicy.filter(records, provider)`. The default implementation drops records from sources whose descriptor has `private=true` (prompt 008 flag) unless the provider is `ollama` or flagged `local=true` in options. Prompt 039 hardens it; Ask, briefings, enrichment and search use it.

### 3. Implementations

**`AnthropicLlmClient`**, official SDK `com.anthropic:anthropic-java` (verified 2026-10: **2.71.0** is current on Maven Central; re-check). Follow the SDK README for exact class and method names, and fix wrong names by compiling.

- **Client:** `AnthropicOkHttpClient.builder().apiKey(decryptedKey).baseUrl(baseUrl)`, built once per provider configuration, rebuilt on change. `maxRetries(2)`.
- **Thinking:** default model `claude-opus-5-5`. Never send `thinking: disabled` or `budget_tokens` (both 400 on Opus 5.5 at every effort). Thinking is adaptive and always on: omit `thinking` (or send adaptive). Control depth with `.outputConfig(OutputConfig.builder().effort(...))`, and **always set effort explicitly** from the request (Opus 5.5 defaults to `medium`). Ask `medium`, briefings `medium`, enrichment `low`.
- **Tool choice:** `auto` only; forced `tool_choice` (`any`/`tool`) returns 400 on this model. Put `strict: true` on tool definitions whose schemas have `additionalProperties: false` and `required`, and steer tool use from the prompt.
- **Conversation history is append-only.** Pass the assistant's content blocks back **unchanged**, including thinking blocks, and never edit or reorder earlier turns (preserved thinking). `rawAssistantTurn` holds the SDK content blocks. No assistant prefill.
- **Refusals:** enable the server-side refusal fallback: beta `server-side-fallback-2026-07-01` plus top-level body property `fallbacks: "default"` (via the beta messages path or the SDK's additional-body-property support). Always check `stop_reason == refusal` before reading content; map to `stopReason=refusal` with `stop_details.category`.
- **Streaming:** use the SDK's streaming API when `stream=true` or `maxTokens > 16000`, and accumulate the final message with the SDK helper.
- **Prompt caching:** `cache_control: ephemeral` on the last system block (`systemOfTextBlockParams`) and on the last tool definition, so repeated Ask and briefing calls reuse the stable prefix. Keep the system prompt and tool order deterministic (no timestamps in system; dates go in the final user turn). Record `cache_read_input_tokens` as `cachedTokens`.
- **Errors:** map the SDK's typed exceptions most-specific-first (rate limit, overloaded/5xx, auth, invalid request, connection) to Foresignal errors; rate-limit errors honour `retry-after`.
- **Models:** `models()` uses `client.models().list()`. **No embeddings:** `embed()` throws `UnsupportedOperationException` with a clear message.

**`OpenAiCompatibleLlmClient`** (`java.net.http`, via `LlmHttp`):
- `POST {baseUrl}/chat/completions` with `tools` (function calling), `max_tokens`, `temperature` if set; `POST {baseUrl}/embeddings` (with `dimensions` when the spec sets it); `GET {baseUrl}/models`.
- Bearer API key; extra headers from options (e.g. OpenRouter `HTTP-Referer`). Normalize tool calls and finish reasons; `content_filter` → `refusal`.

**`OllamaLlmClient`** (`java.net.http`, via `LlmHttp`):
- `POST {baseUrl}/api/chat` (`stream:false`, or NDJSON streaming) with `tools` for models that support them; `POST /api/embed` (batch input); `GET /api/tags`.
- When a model rejects tools, set `supportsTools=false` in provider status so Ask uses retrieval-only mode (prompt 028).

### 4. Budgets, usage, health

- **`LlmBudgetGovernor`:** Bucket4j `requestsPerMinute` per provider plus a daily token budget (UTC-aligned). Before a call, estimate tokens (characters / 4 for input, plus `maxTokens`); refuse with `LlmBudgetExceededException` (→ 429 `LLM_BUDGET_EXCEEDED`) when the estimate exceeds the remainder. Record actual usage after. Seed daily figures from `llm_usage` at startup.
- **`LlmUsageMeter`:** per-minute counters flushed every 60 s (same pattern as `UsageMeter`), `series()` for `/v1/llm/usage`.
- **Provider health:** last success, last error, consecutive failures; status derived like source health.
- **System notifications** (`NotificationService.publish`, event `system`, persisted by prompt 018) when a daily budget is exhausted or a provider starts failing.
- **Content logging:** every call logs purpose, provider, model, token counts (incl. cached), duration and stop reason. Prompt and response bodies only when `logContent=true`, at DEBUG, never API keys.

### 5. API

- `listLlmProviders`, `createLlmProvider` (201), `getLlmProvider`, `updateLlmProvider` (omit `apiKey` to keep it; changing model or dimensions of the active embedding provider, or reassigning `embeddingProvider`, publishes `EmbeddingModelChangedEvent(oldSpec, newSpec)`, handled by 027), `deleteLlmProvider` (409 if assigned to a role).
- `testLlmProvider`: chat "Reply with the single word: ok" (`maxTokens` 16, purpose `test`) plus an embedding of "test" if configured; returns `LlmTestResult`; consumes budget.
- `listLlmProviderModels`; `getLlmSettings` / `updateLlmSettings`; `getLlmUsage`. Export/import: providers (without keys) and settings. `/v1/system` `llm` block: configured, providers per role, tokens today.
- `docs/OpenAPI.json`: add `refusalCategory` and `cachedTokens` to `LlmTestResult`, `supportsTools` and health to `LlmProvider`; keep `x-foresignal-scope: admin` on every LLM operation; `AccessPolicy` rules for `/v1/llm/**` (admin); regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`) with BODIES examples for create/update provider and settings.

### 6. Docker Compose and docs

- `ollama` service under profile `local-llm` (`ollama/ollama`, volume `ollama`, port bound to `${FORESIGNAL_BIND:-127.0.0.1}:11434`), per Architecture §14.
- `docs/Operator-Guide.md` "LLM" section: `docker compose --profile local-llm up -d`, then `docker compose exec ollama ollama pull qwen3:14b` and `ollama pull nomic-embed-text`; configuring a Claude provider (API key from the Anthropic Console); roles, effort per use, budgets and costs; the data-egress statement (with a frontier provider the records the model reads are sent to that provider; private sources are withheld; the local profile keeps everything on the box).

## Tests to write (do not run)

**No test calls a real model.** WireMock stubs per provider API; `FakeLlmClient` for consumers.

- `AnthropicLlmClientIT` (SDK pointed at WireMock via `baseUrl`): body contains model `claude-opus-5-5`, `output_config.effort`, `fallbacks: "default"` and the beta header, `cache_control` on system and tools, `strict` on tools, `tool_choice` absent or `auto`, and **no** `thinking.disabled` or `budget_tokens`; tool-use round trip with `rawAssistantTurn` appended byte-identical (thinking blocks included); refusal with category mapped; 429 with retry-after; streaming assembled; cached tokens recorded.
- `OpenAiCompatibleLlmClientIT`, `OllamaLlmClientIT`: chat, tool calls, embeddings (incl. explicit `EmbeddingModelSpec`), models, error mapping, tools-unsupported detection.
- `ToolLoopTest` (`FakeLlmClient`): parallel execution, all results in one message, iteration/deadline/token caps, error results, refusal stops the loop.
- `EgressPolicy (LLM-context path; see docs/Conventions.md)Test`: private-source records dropped for anthropic/openai-compatible, kept for ollama.
- `LlmBudgetGovernorTest`: per-minute and daily limits, estimate refusal, seeding, UTC rollover.
- `LlmControllersIT`: CRUD, key never returned, Anthropic as embedding provider → 400, delete while assigned → 409, `EmbeddingModelChangedEvent` with old and new spec, test endpoint against WireMock, usage series, invalid `baseUrl` → 400.

## Acceptance criteria

- With no provider configured, every non-LLM feature behaves exactly as before.
- With a provider configured, the test endpoint succeeds against a stub; the Anthropic request shape matches the rules above.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `026: LLM Providers`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/026-CR-LLM-Providers.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `026: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
