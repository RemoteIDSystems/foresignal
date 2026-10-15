package ai.foresignal.analyst;

import java.util.Set;

/** The analyst's allowed action types (prompt 102); 128 adds more. */
public final class AnalystActions {

    public static final String PROPOSE_WATCH = "propose_watch";
    public static final String PROPOSE_INTEL_EVENT = "propose_intel_event";
    public static final String PROPOSE_INVESTIGATION_ITEM = "propose_investigation_item";
    public static final String ANNOTATE_EVENT = "annotate_event";

    /** Every action an operator may allow today. */
    public static final Set<String> ALL = Set.of(PROPOSE_WATCH, PROPOSE_INTEL_EVENT, PROPOSE_INVESTIGATION_ITEM,
            ANNOTATE_EVENT);

    private AnalystActions() {}
}
