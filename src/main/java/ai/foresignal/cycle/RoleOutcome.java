package ai.foresignal.cycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The outcome of one LLM role in a run (prompt 128; the {@code roles} entries of {@code CycleRun} and
 * {@code Assessment}): status, reason, provider, model, effort, tokens, forecasts submitted, proposals, prompt
 * template and warnings. Mutable while the role runs; safe for the parallel tool calls of one loop turn.
 */
public final class RoleOutcome {

    private final String role;
    private volatile String status = CycleVocabulary.OK;
    private volatile String reason;
    private volatile String provider;
    private volatile String model;
    private volatile String effort;
    private volatile String templateId;
    private long inputTokens;
    private long outputTokens;
    private long cachedTokens;
    private int forecastsSubmitted;
    private final List<String> proposalIds = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    /** An outcome for {@code role}. */
    public RoleOutcome(String role) {
        this.role = role;
    }

    /** A finished outcome with {@code status} and {@code reason}. */
    public static RoleOutcome of(String role, String status, String reason) {
        RoleOutcome out = new RoleOutcome(role);
        out.finish(status, reason);
        return out;
    }

    /** The role. */
    public String role() {
        return role;
    }

    /** The status: ok, skipped, failed or not-configured. */
    public String status() {
        return status;
    }

    /** The reason, or null. */
    public String reason() {
        return reason;
    }

    /** The model, or null. */
    public String model() {
        return model;
    }

    /** The effort, or null. */
    public String effort() {
        return effort;
    }

    /** The provider, or null. */
    public String provider() {
        return provider;
    }

    /** The prompt template id, or null. */
    public String templateId() {
        return templateId;
    }

    /** Sets the status and reason. */
    public void finish(String newStatus, String newReason) {
        this.status = newStatus;
        this.reason = newReason;
    }

    /** Records the provider, effort and template. */
    public void using(String newProvider, String newEffort, String newTemplateId) {
        this.provider = newProvider;
        this.effort = newEffort;
        this.templateId = newTemplateId;
    }

    /** Records the model answering. */
    public void model(String newModel) {
        if (newModel != null) {
            this.model = newModel;
        }
    }

    /** Adds token use. */
    public synchronized void tokens(long in, long out, long cached) {
        inputTokens += in;
        outputTokens += out;
        cachedTokens += cached;
    }

    /** The total tokens (input plus output). */
    public synchronized long total() {
        return inputTokens + outputTokens;
    }

    /** Counts a submitted ledger forecast. */
    public synchronized void forecastSubmitted() {
        forecastsSubmitted++;
    }

    /** The forecasts submitted. */
    public synchronized int forecastsSubmitted() {
        return forecastsSubmitted;
    }

    /** Adds a proposal id. */
    public synchronized void proposal(String id) {
        proposalIds.add(id);
    }

    /** Adds a warning. */
    public synchronized void warn(String warning) {
        if (warning != null && !warnings.contains(warning)) {
            warnings.add(warning);
        }
    }

    /** The warnings. */
    public synchronized List<String> warnings() {
        return List.copyOf(warnings);
    }

    /** The wire form ({@code CycleRun.roles[]}). */
    public synchronized Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("role", role);
        out.put("status", status);
        out.put("reason", reason);
        out.put("provider", provider);
        out.put("model", model);
        out.put("effort", effort);
        out.put("inputTokens", inputTokens);
        out.put("outputTokens", outputTokens);
        out.put("cachedTokens", cachedTokens);
        out.put("forecastsSubmitted", forecastsSubmitted);
        out.put("proposalIds", List.copyOf(proposalIds));
        out.put("promptTemplate", templateId == null ? null : Map.of("id", templateId, "version",
                ai.foresignal.llm.PromptTemplates.V1));
        out.put("warnings", List.copyOf(warnings));
        return out;
    }
}
