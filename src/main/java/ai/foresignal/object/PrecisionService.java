package ai.foresignal.object;

import ai.foresignal.analytics.math.Wilson;
import ai.foresignal.api.error.ValidationException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

/**
 * Precision from analyst verdicts ({@code getPrecision}, prompt 129): for findings (by detector), intel events (by
 * recipe), situations (the linker) and precursor-active findings (by precursor), the share confirmed among labelled
 * verdicts with a 95% Wilson interval. Only the latest live verdict per object counts; {@code needs-review} counts as
 * unlabelled; {@code duplicate-of} counts as labelled but not confirmed. Private verdicts count only for callers with
 * private access.
 */
@Service
public class PrecisionService {

    /** Producer classes. */
    public static final List<String> COMPONENTS = List.of("finding", "intel-event", "situation", "precursor-active");

    private final AnnotationService annotations;
    private final Clock clock;

    /** Creates the service. */
    public PrecisionService(AnnotationService annotations, Clock clock) {
        this.annotations = annotations;
        this.clock = clock;
    }

    /** Counts for one producer. */
    static final class Tally {
        long confirmed;
        long rejected;
        long duplicate;
        long needsReview;

        void add(String verdict) {
            switch (verdict) {
                case "confirmed" -> confirmed++;
                case "rejected" -> rejected++;
                case "duplicate-of" -> duplicate++;
                default -> needsReview++;
            }
        }

        long labelled() {
            return confirmed + rejected + duplicate;
        }
    }

    /**
     * The {@code Precision} view.
     *
     * @param component one of {@link #COMPONENTS}, or null for all
     * @param since the earliest verdict counted, or null for all
     * @param seesPrivate true when private verdicts count
     * @return the view
     */
    public Map<String, Object> precision(String component, Instant since, boolean seesPrivate) {
        if (component != null && !COMPONENTS.contains(component)) {
            throw ValidationException.forField("component", "must be one of " + COMPONENTS);
        }
        Map<String, AnnotationEntity> latest = new LinkedHashMap<>();
        for (AnnotationEntity a : annotations.verdictsSince(since)) {
            if (a.getVerdict() != null && (!a.isPrivateAnnotation() || seesPrivate)) {
                latest.put(a.getTargetRef(), a);
            }
        }
        Map<String, Map<String, Tally>> byComponent = new LinkedHashMap<>();
        for (String c : COMPONENTS) {
            byComponent.put(c, new TreeMap<>());
        }
        for (AnnotationEntity a : latest.values()) {
            String c = a.getTargetComponent();
            if (c != null && byComponent.containsKey(c) && a.getTargetKey() != null) {
                byComponent.get(c).computeIfAbsent(a.getTargetKey(), k -> new Tally()).add(a.getVerdict());
            }
            if ("finding".equals(c) && a.getPrecursorKey() != null) {
                byComponent.get("precursor-active").computeIfAbsent(a.getPrecursorKey(), k -> new Tally())
                        .add(a.getVerdict());
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, Map<String, Tally>> c : byComponent.entrySet()) {
            if (component != null && !component.equals(c.getKey())) {
                continue;
            }
            for (Map.Entry<String, Tally> k : c.getValue().entrySet()) {
                rows.add(row(c.getKey(), k.getKey(), k.getValue()));
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("since", since);
        out.put("generatedAt", clock.instant());
        out.put("components", rows);
        return out;
    }

    static Map<String, Object> row(String component, String key, Tally t) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("component", component);
        row.put("key", key);
        row.put("confirmed", t.confirmed);
        row.put("rejected", t.rejected);
        row.put("duplicate", t.duplicate);
        row.put("needsReview", t.needsReview);
        long labelled = t.labelled();
        row.put("labelled", labelled);
        if (labelled == 0) {
            row.put("precision", null);
            row.put("interval", null);
        } else {
            row.put("precision", (double) t.confirmed / labelled);
            double[] ci = Wilson.interval(t.confirmed, labelled);
            Map<String, Object> interval = new LinkedHashMap<>();
            interval.put("lo", ci[0]);
            interval.put("hi", ci[1]);
            row.put("interval", interval);
        }
        return row;
    }
}
