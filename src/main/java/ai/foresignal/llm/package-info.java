/**
 * The optional, budgeted LLM subsystem (Architecture §19, FR-44 to FR-46, prompt 026).
 *
 * <p>Providers ({@code llm_provider}) are managed through {@code /v1/llm/providers}; roles and
 * options live in {@code app_setting['llm']} ({@link ai.foresignal.llm.LlmSettings}). Every call goes
 * through {@link ai.foresignal.llm.LlmService}, which resolves the provider, checks the budget
 * ({@link ai.foresignal.llm.LlmBudgetGovernor}), meters usage ({@link ai.foresignal.llm.LlmUsageMeter}),
 * tracks provider health ({@link ai.foresignal.llm.LlmHealthTracker}) and logs the call. Every
 * provider HTTP client is built by {@link ai.foresignal.llm.LlmHttp}, where prompt 035 enforces the
 * outbound URL policy. Record content placed in a prompt passes through
 * {@link ai.foresignal.security.EgressPolicy} first.
 *
 * <p>With no provider configured nothing here runs, and every non-LLM feature behaves as before.
 */
package ai.foresignal.llm;
