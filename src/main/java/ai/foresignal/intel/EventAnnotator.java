package ai.foresignal.intel;

import ai.foresignal.derive.DerivedRecords;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * An annotator that adds context to an existing intelligence event (prompt 105): it never changes the
 * event's members' roles as evidence, its confidence or its type. Its findings are stored as
 * {@code context} (for example {@code context.weather}) and its context members carry the role
 * {@code context}, so rules, scoring and confidence treat them as background, not corroboration.
 *
 * <p>This is the "annotate, do not create" half of the weather correlation work; the recipes that
 * create weather-impact events are 101 recipe JSONs (type {@code weather-impact}).
 */
public interface EventAnnotator {

    /** A stable id, stored as the context's {@code annotator}. */
    String id();

    /** The event types this annotator applies to. */
    Set<String> types();

    /** Applies the annotator to {@code event}. */
    Result annotate(IntelligenceEventEntity event, AnnotatorContext ctx);

    /**
     * What an annotator found.
     *
     * @param context the context sections to merge into the event's {@code context}, or null
     * @param alternatives the alternatives to append (each {@code {hypothesis, basis, supportingMemberIds}})
     * @param contextMembers the context members to attach (each a member-shaped map)
     */
    record Result(Map<String, Object> context, List<Map<String, Object>> alternatives,
            List<Map<String, Object>> contextMembers) {

        /** Nothing. */
        public static Result none() {
            return new Result(null, List.of(), List.of());
        }

        /** True when there is nothing to store. */
        public boolean isEmpty() {
            return (context == null || context.isEmpty()) && alternatives.isEmpty() && contextMembers.isEmpty();
        }
    }

    /**
     * What the annotator may read.
     *
     * @param area the event's geometry (never null)
     * @param from the window start (event start minus 3 h)
     * @param to the window end (event end, or now, plus 3 h)
     * @param records the current derived-record heads
     */
    record AnnotatorContext(Geometry area, Instant from, Instant to, DerivedRecords records) {}
}
