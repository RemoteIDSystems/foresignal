package ai.foresignal.baserate.reference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Accumulates events into weekly counts per {@code (family, region)} (prompt 121): the only shape reference history is
 * kept in. Each event is bucketed into the ISO week (Monday, UTC) of its day.
 */
public final class WeeklyCounts {

    /**
     * One weekly row.
     *
     * @param family the family
     * @param regionCode the 040 region
     * @param weekStart the Monday of the week
     * @param count the events
     * @param fatalities the fatalities, or null when the source reports none
     */
    public record Row(String family, String regionCode, LocalDate weekStart, long count, Long fatalities) {}

    private record Key(String family, String region, LocalDate week) {}

    private final Map<Key, long[]> counts = new LinkedHashMap<>();
    private final Map<Key, Boolean> hasFatalities = new LinkedHashMap<>();

    /**
     * Adds one event.
     *
     * @param family the family
     * @param regionCode the 040 region ({@code ZZ} when unknown)
     * @param day the event's UTC day
     * @param fatalities its fatalities, or null
     */
    public void add(String family, String regionCode, LocalDate day, Long fatalities) {
        if (family == null || regionCode == null || day == null) {
            return;
        }
        Key key = new Key(family, regionCode, ReferenceFamilies.weekStart(day));
        long[] c = counts.computeIfAbsent(key, k -> new long[2]);
        c[0]++;
        if (fatalities != null && fatalities > 0) {
            c[1] += fatalities;
        }
        if (fatalities != null) {
            hasFatalities.put(key, true);
        }
    }

    /** The number of weekly rows. */
    public int size() {
        return counts.size();
    }

    /** True when no event was added. */
    public boolean isEmpty() {
        return counts.isEmpty();
    }

    /** The weekly rows, ordered by family, region and week. */
    public List<Row> rows() {
        List<Row> out = new ArrayList<>();
        counts.forEach((k, c) -> out.add(new Row(k.family(), k.region(), k.week(), c[0],
                Boolean.TRUE.equals(hasFatalities.get(k)) ? c[1] : null)));
        out.sort(Comparator.comparing(Row::family).thenComparing(Row::regionCode).thenComparing(Row::weekStart));
        return out;
    }
}
