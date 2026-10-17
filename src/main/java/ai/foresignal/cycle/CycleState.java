package ai.foresignal.cycle;

import java.util.List;
import java.util.Map;

/**
 * The current state an assessment describes beside the packet's changes (prompt 128 §4): the situations in scope,
 * the active hypothesis sets with their hypotheses and latest drivers, the indicators expected in the next period, and
 * (weekly, monthly) the 90-day scorecard summary with the operator next to the machine.
 *
 * @param situations situations ({@code id, title, status, severity, lastActivityAt, theme, regionCodes, private})
 * @param sets sets ({@code setId, question, cadence, situationId, questionId, private, stakes, hypotheses, drivers})
 * @param pendingIndicators indicators ({@code indicatorId, setId, description, expectedBy})
 * @param scorecard the scorecard summary, or null (daily and on demand)
 */
public record CycleState(List<Map<String, Object>> situations, List<Map<String, Object>> sets,
        List<Map<String, Object>> pendingIndicators, Map<String, Object> scorecard) {

    /** An empty state. */
    public static final CycleState EMPTY = new CycleState(List.of(), List.of(), List.of(), null);

    /** Copies the lists. */
    public CycleState {
        situations = situations == null ? List.of() : List.copyOf(situations);
        sets = sets == null ? List.of() : List.copyOf(sets);
        pendingIndicators = pendingIndicators == null ? List.of() : List.copyOf(pendingIndicators);
    }

    /** True when any described object is private. */
    public boolean anyPrivate() {
        return situations.stream().anyMatch(s -> Boolean.TRUE.equals(s.get("private")))
                || sets.stream().anyMatch(s -> Boolean.TRUE.equals(s.get("private")));
    }
}
