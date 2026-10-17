package ai.foresignal.manifest;

/**
 * The outcome of replaying a manifest (prompt 116).
 *
 * @param outcome {@code identical}, {@code differs} or {@code not-replayable}
 * @param reason when not replayable: {@code inputs-purged}, {@code component-removed} or {@code llm-output};
 *     else null
 * @param diffSummary when it differs, a short summary of what changed; else null
 * @param message a human-readable note, or null
 */
public record ReplayOutcome(String outcome, String reason, String diffSummary, String message) {

    /** The replayed output equals the stored one. */
    public static final String IDENTICAL = "identical";

    /** The replayed output differs from the stored one. */
    public static final String DIFFERS = "differs";

    /** The manifest cannot be replayed. */
    public static final String NOT_REPLAYABLE = "not-replayable";

    /** Inputs were purged by retention. */
    public static final String INPUTS_PURGED = "inputs-purged";

    /** The component no longer exists. */
    public static final String COMPONENT_REMOVED = "component-removed";

    /** The output came from an LLM. */
    public static final String LLM_OUTPUT = "llm-output";

    /** The message of an LLM manifest. */
    public static final String LLM_MESSAGE = "inputs recorded; model output not reproducible";

    /** An identical replay. */
    public static ReplayOutcome identical() {
        return new ReplayOutcome(IDENTICAL, null, null, null);
    }

    /** A replay that differs. */
    public static ReplayOutcome differs(String summary) {
        return new ReplayOutcome(DIFFERS, null, summary, null);
    }

    /** A manifest that cannot be replayed. */
    public static ReplayOutcome notReplayable(String reason, String message) {
        return new ReplayOutcome(NOT_REPLAYABLE, reason, null, message);
    }
}
