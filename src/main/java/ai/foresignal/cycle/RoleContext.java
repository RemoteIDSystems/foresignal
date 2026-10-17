package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.EgressPolicy;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * What one LLM role of a run works with (prompt 128): the provider and its governed client, the egress destination
 * (sources excluded from LLM context, whether private records may be sent), the read-only tool caller, the effort,
 * the 102 profile's limits, the run's shared token budget, the citation set, and whether anything private reached the
 * model.
 *
 * @param providerId the provider
 * @param client the governed client
 * @param destination the egress destination
 * @param excluded sources excluded from LLM context
 * @param privateAllowed whether private records may be sent
 * @param caller the read-only tool caller
 * @param effort the effort wire value
 * @param profile the 102 profile (a transient default without one)
 * @param budget the run's token budget
 * @param citations what may be cited
 * @param privateSeen set when anything private reached the model
 * @param runId the run
 */
public record RoleContext(String providerId, LlmClient client, EgressPolicy.LlmDestination destination,
        Set<String> excluded, boolean privateAllowed, McpCaller caller, String effort, AnalystProfileEntity profile,
        TokenBudget budget, CycleCitations citations, AtomicBoolean privateSeen, String runId) {

    /** A copy for another provider (the forecaster runs once per configured provider). */
    public RoleContext withProvider(String newProviderId, LlmClient newClient,
            EgressPolicy.LlmDestination newDestination, Set<String> newExcluded, boolean newPrivateAllowed,
            McpCaller newCaller) {
        return new RoleContext(newProviderId, newClient, newDestination, newExcluded, newPrivateAllowed, newCaller,
                effort, profile, budget, citations, privateSeen, runId);
    }
}
