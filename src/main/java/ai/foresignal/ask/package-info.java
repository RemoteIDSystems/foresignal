/**
 * Ask Foresignal (Architecture §21, FR-49, prompt 028): natural-language questions answered from
 * Foresignal's own data by the chat provider.
 *
 * <p>{@link ai.foresignal.ask.AskService} runs the model with the read tools of the MCP server (through
 * {@link ai.foresignal.mcp.ToolCatalog}) plus the {@code propose_*} action tools, with the request's
 * scope injected into every tool call ({@link ai.foresignal.ask.ScopeInjector}) and every tool result
 * filtered for LLM egress and scope ({@link ai.foresignal.ask.ToolOutputFilter}) before the provider
 * sees it. A provider without tool support answers in retrieval-only mode. Every citation in the answer
 * is verified against what the tools returned, down to the persisted version seq
 * ({@link ai.foresignal.ask.CitationVerifier}, shared with briefings and enrichment); dropped
 * citations and uncited factual claims are returned as warnings, streamed and persisted, never hidden.
 *
 * <p>Models never change anything themselves (invariant EGR-3): a {@code propose_*} tool only records
 * a pending {@code ask_action}, executed on confirmation by an {@code agent} token or at once when an
 * {@code agent} caller sets {@code allowActions} ({@link ai.foresignal.ask.AskActionService}).
 *
 * <p>Threads ({@code ask_thread}, {@code ask_message}) keep the provider's raw turns so follow-up
 * questions continue the conversation append-only; {@link ai.foresignal.ask.AskRetentionStep} deletes
 * threads older than the {@code ask} retention.
 */
package ai.foresignal.ask;
