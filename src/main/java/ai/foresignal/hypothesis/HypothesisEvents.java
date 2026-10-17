package ai.foresignal.hypothesis;

import ai.foresignal.forecast.ForecastEvents;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Writes the {@code hypotheses} SSE events (prompt 125): each event is a {@code hypothesis_log} row written in the
 * caller's transaction and published to live subscribers once it commits.
 */
@Component
public class HypothesisEvents {

    /** A set was created. */
    public static final String SET_CREATED = "set.created";

    /** A set was revised. */
    public static final String SET_REVISED = "set.revised";

    /** A hypothesis crossed an ICD 203 band. */
    public static final String SET_BAND_CHANGED = "set.band_changed";

    /** A set was resolved. */
    public static final String SET_RESOLVED = "set.resolved";

    /** An indicator was observed. */
    public static final String INDICATOR_OBSERVED = "indicator.observed";

    /** An indicator was absent with coverage. */
    public static final String INDICATOR_ABSENT = "indicator.absent";

    /** A key assumption broke. */
    public static final String ASSUMPTION_BROKEN = "assumption.broken";

    /** Every event name. */
    public static final List<String> ALL = List.of(SET_CREATED, SET_REVISED, SET_BAND_CHANGED, SET_RESOLVED,
            INDICATOR_OBSERVED, INDICATOR_ABSENT, ASSUMPTION_BROKEN);

    private final HypothesisLogRepository logs;
    private final HypothesesTopic topic;
    private final Clock clock;

    /** Creates the component. */
    public HypothesisEvents(HypothesisLogRepository logs, HypothesesTopic topic, Clock clock) {
        this.logs = logs;
        this.topic = topic;
        this.clock = clock;
    }

    /** The {@code HypothesisSetEvent} payload. */
    public static Map<String, Object> setPayload(HypothesisSetEntity set, List<HypothesisEntity> hypotheses,
            String revisionId, String cause, List<Map<String, Object>> bandChanges, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("setId", set.getId());
        out.put("version", set.getVersion());
        out.put("question", set.getQuestion());
        out.put("status", set.getStatus());
        out.put("revisionId", revisionId);
        out.put("cause", cause);
        List<Map<String, Object>> states = new ArrayList<>();
        for (HypothesisEntity h : hypotheses) {
            states.add(HypothesisViews.state(h));
        }
        out.put("hypotheses", states);
        out.put("bandChanges", bandChanges == null ? List.of() : bandChanges);
        out.put("private", set.isPrivateSet());
        out.put("at", at.toString());
        return out;
    }

    /** The {@code IndicatorEvent} payload. */
    public static Map<String, Object> indicatorPayload(IndicatorEntity indicator, String previousState,
            String revisionId, boolean privateSet, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indicatorId", indicator.getId());
        out.put("setId", indicator.getSetId());
        out.put("description", indicator.getDescription());
        out.put("signalFamily", indicator.getSignalFamily());
        out.put("state", indicator.getState());
        if (previousState != null) {
            out.put("previousState", previousState);
        }
        out.put("evidence", indicator.getObservedEvidence());
        out.put("revisionId", revisionId);
        out.put("private", privateSet);
        out.put("at", at.toString());
        return out;
    }

    /** The {@code KeyAssumptionEvent} payload. */
    public static Map<String, Object> assumptionPayload(KeyAssumptionEntity assumption, String previousStatus,
            boolean privateSet, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("assumptionId", assumption.getId());
        out.put("setId", assumption.getSetId());
        out.put("text", assumption.getText());
        out.put("linchpin", assumption.isLinchpin());
        out.put("status", assumption.getStatus());
        if (previousStatus != null) {
            out.put("previousStatus", previousStatus);
        }
        out.put("private", privateSet);
        out.put("at", at.toString());
        return out;
    }

    /** Emits a set event. */
    public void set(String event, HypothesisSetEntity set, List<HypothesisEntity> hypotheses, String revisionId,
            String cause, List<Map<String, Object>> bandChanges) {
        Instant now = clock.instant();
        write(new HypothesisLogEntity(event, set.getId(), setPayload(set, hypotheses, revisionId, cause, bandChanges,
                now), set.isPrivateSet(), now));
    }

    /** Emits {@code indicator.observed} or {@code indicator.absent}. */
    public void indicator(String event, IndicatorEntity indicator, String previousState, String revisionId,
            boolean privateSet) {
        Instant now = clock.instant();
        write(new HypothesisLogEntity(event, indicator.getSetId(), indicatorPayload(indicator, previousState,
                revisionId, privateSet, now), privateSet, now));
    }

    /** Emits {@code assumption.broken}. */
    public void assumptionBroken(KeyAssumptionEntity assumption, String previousStatus, boolean privateSet) {
        Instant now = clock.instant();
        write(new HypothesisLogEntity(ASSUMPTION_BROKEN, assumption.getSetId(), assumptionPayload(assumption,
                previousStatus, privateSet, now), privateSet, now));
    }

    private void write(HypothesisLogEntity row) {
        HypothesisLogEntity saved = logs.save(row);
        ForecastEvents.afterCommit(() -> topic.publish(saved));
    }
}
