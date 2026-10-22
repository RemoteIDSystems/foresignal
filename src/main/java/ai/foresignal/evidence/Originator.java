package ai.foresignal.evidence;

import java.util.List;
import java.util.Set;

/**
 * Who originated an observation (prompt 116): an agency, sensor network, aggregator, outlet, crowd, the
 * operator or Foresignal itself. Sources in the same {@link #independenceGroup()} are never independent of
 * each other, and an originator that {@link #aggregates()} another republishes it.
 *
 * @param id the originator id, e.g. {@code usgs-anss}
 * @param originatorClass {@code sensor-network}, {@code imagery}, {@code agency}, {@code aggregator},
 *     {@code media}, {@code crowd}, {@code operator}, {@code derived} or {@code social}
 * @param independenceGroup the group sources are judged in, or null for the originator alone
 * @param aggregates originator ids this originator republishes
 * @param notes free text, or null
 * @param domains web domains that identify this originator when a report cites it
 */
public record Originator(String id, String originatorClass, String independenceGroup, List<String> aggregates,
        String notes, List<String> domains) {

    /** The classes an originator may have (the spec's {@code Originator.class} enum). */
    public static final Set<String> CLASSES = Set.of("sensor-network", "imagery", "agency", "aggregator", "media",
            "crowd", "operator", "derived", "social");

    /** The class of an originator nothing declares. */
    public static final String DEFAULT_CLASS = "aggregator";

    /** Normalizes the lists and the class. */
    public Originator {
        originatorClass = originatorClass == null || !CLASSES.contains(originatorClass) ? DEFAULT_CLASS : originatorClass;
        independenceGroup = independenceGroup == null || independenceGroup.isBlank() ? null : independenceGroup;
        aggregates = aggregates == null ? List.of() : List.copyOf(aggregates);
        domains = domains == null ? List.of() : List.copyOf(domains);
    }

    /** The default originator of a source nothing maps: its own id, class {@code aggregator}. */
    public static Originator defaultFor(String sourceId) {
        return new Originator(sourceId, DEFAULT_CLASS, null, List.of(), "default: the source is its own originator",
                List.of());
    }

    /**
     * The root sources are judged by: the independence group when one is declared, else the originator id.
     * Two observations whose roots are equal are never independent.
     */
    public String root() {
        return independenceGroup == null ? id : independenceGroup;
    }
}
