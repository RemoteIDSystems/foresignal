package ai.foresignal.intel;

import ai.foresignal.manifest.ManifestReplayCheck;
import ai.foresignal.manifest.ManifestReplayer;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.manifest.ReplayOutcome;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Replays an intelligence event's score from its manifest (prompt 116): the member views recorded in the
 * manifest (family, weight, health at the time, evidence group) and the contradictions are scored again with
 * the installed {@link EventScorer} and compared with the event that carries the manifest. A manifest of an
 * older scorer version is {@code not-replayable} ({@code component-removed}).
 */
@Component
public class EventScorerReplayer implements ManifestReplayer {

    private final JdbcTemplate jdbc;

    /** Creates the replayer. */
    public EventScorerReplayer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean supports(String component) {
        return EventScorer.NAME.equals(component);
    }

    @Override
    public ReplayOutcome replay(ManifestService.Manifest manifest) {
        if (!EventScorer.VERSION.equals(manifest.componentVersion())) {
            return ReplayOutcome.notReplayable(ReplayOutcome.COMPONENT_REMOVED, "The scorer version "
                    + manifest.componentVersion() + " is no longer installed (now " + EventScorer.VERSION + ").");
        }
        Object members = manifest.config().get("members");
        List<EventScorer.MemberView> views = EventScorerViews.fromMaps(members instanceof List<?> l ? l : List.of());
        Object contradictions = manifest.config().get("contradictions");
        EventScorer.Score score = EventScorer.score(views, contradictions instanceof Number n ? n.intValue() : 0,
                quality(manifest.config().get("quality")));
        List<Map<String, Object>> stored = jdbc.queryForList("SELECT confidence, confidence_label, evidence_quality"
                + " FROM intelligence_event WHERE manifest_id = ? LIMIT 1", manifest.id());
        if (stored.isEmpty()) {
            return ReplayOutcome.notReplayable(null,
                    "No intelligence event still carries this manifest; its score was superseded by a later update.");
        }
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("confidence", stored.get(0).get("confidence"));
        before.put("label", stored.get(0).get("confidence_label"));
        before.put("evidenceQuality", stored.get(0).get("evidence_quality"));
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("confidence", score.confidence());
        after.put("label", score.label());
        after.put("evidenceQuality", score.evidenceQuality());
        return ManifestReplayCheck.compare(before, after);
    }

    /**
     * The quality thresholds a manifest recorded (prompt 154), or the defaults when absent (every manifest
     * written before 154, which used the default thresholds, stays replayable).
     */
    private static EventScorer.Quality quality(Object recorded) {
        if (!(recorded instanceof Map<?, ?> m)) {
            return EventScorer.Quality.DEFAULT;
        }
        EventScorer.Quality d = EventScorer.Quality.DEFAULT;
        return new EventScorer.Quality(intValue(m.get("strongFamilies"), d.strongFamilies()),
                intValue(m.get("strongGroups"), d.strongGroups()),
                intValue(m.get("moderateFamilies"), d.moderateFamilies()),
                intValue(m.get("requireModalities"), d.requireModalities()));
    }

    private static int intValue(Object v, int fallback) {
        return v instanceof Number n ? n.intValue() : fallback;
    }
}
