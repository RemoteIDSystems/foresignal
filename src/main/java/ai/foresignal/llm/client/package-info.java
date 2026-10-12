/**
 * The provider-neutral LLM client abstraction (Architecture §19, prompt 026).
 *
 * <p>{@link ai.foresignal.llm.client.LlmClient} is what every LLM feature calls: Ask (028), briefings
 * (029), enrichment (030), search (027) and the analyst layer. A request is a
 * {@link ai.foresignal.llm.client.ChatRequest}; a response is a
 * {@link ai.foresignal.llm.client.ChatResponse} whose stop reason, tool calls and usage are normalized
 * across the three implementations: {@link ai.foresignal.llm.client.AnthropicLlmClient} (official
 * Anthropic Java SDK), {@link ai.foresignal.llm.client.OpenAiCompatibleLlmClient} and
 * {@link ai.foresignal.llm.client.OllamaLlmClient} (both {@code java.net.http} through
 * {@link ai.foresignal.llm.LlmHttp}). {@link ai.foresignal.llm.client.ToolLoop} drives multi-step
 * tool use.
 *
 * <p>The clients here are raw: they neither budget, meter nor log. Callers obtain governed clients
 * from {@link ai.foresignal.llm.LlmService}, which wraps them with the budget, usage meter, health
 * tracker and call log.
 */
package ai.foresignal.llm.client;
