package ai.foresignal.analyst;

import java.util.Set;

/**
 * The analyst's allowed action types (prompt 102). Prompt 128 adds the analysis-cycle proposer's types:
 * {@code propose_hypothesis_set}, {@code propose_indicator}, {@code propose_question} and
 * {@code propose_situation_merge}; prompt 131 adds {@code propose_chronicle}. Nothing here deletes or disables
 * anything.
 */
public final class AnalystActions {

    /** Create a watch (a rule). */
    public static final String PROPOSE_WATCH = "propose_watch";

    /** Create an analyst intelligence event. */
    public static final String PROPOSE_INTEL_EVENT = "propose_intel_event";

    /** Add an item to an open investigation. */
    public static final String PROPOSE_INVESTIGATION_ITEM = "propose_investigation_item";

    /** Append labeled speculation to an event's narrative. */
    public static final String ANNOTATE_EVENT = "annotate_event";

    /** Create a hypothesis set from a 125 draft (prompt 128). */
    public static final String PROPOSE_HYPOTHESIS_SET = "propose_hypothesis_set";

    /** Add an indicator to an existing hypothesis set (prompt 128). */
    public static final String PROPOSE_INDICATOR = "propose_indicator";

    /** Open a ledger question, validated by 122 (prompt 128). */
    public static final String PROPOSE_QUESTION = "propose_question";

    /** Merge one situation into another (prompt 128). */
    public static final String PROPOSE_SITUATION_MERGE = "propose_situation_merge";

    /** Propose a chronicle for an emerging situation or a new theme; executing it activates it (prompt 131). */
    public static final String PROPOSE_CHRONICLE = "propose_chronicle";

    /** Every action an operator may allow today. */
    public static final Set<String> ALL = Set.of(PROPOSE_WATCH, PROPOSE_INTEL_EVENT, PROPOSE_INVESTIGATION_ITEM,
            ANNOTATE_EVENT, PROPOSE_HYPOTHESIS_SET, PROPOSE_INDICATOR, PROPOSE_QUESTION, PROPOSE_SITUATION_MERGE,
            PROPOSE_CHRONICLE);

    private AnalystActions() {}
}
