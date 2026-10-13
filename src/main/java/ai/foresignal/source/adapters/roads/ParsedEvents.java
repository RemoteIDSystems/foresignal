package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.spi.NormalizedRecord;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What one road-event document parsed into (prompt 074): the current events, the ids the upstream marks as ended,
 * the entries skipped by reason, and the next page when the format pages.
 *
 * <p>Mutable while a parser fills it; the adapter reads it once afterwards. Each id is listed once: a record id
 * added again is skipped as {@value #DUPLICATE}, and an id marked ended is never also a record.
 */
public final class ParsedEvents {

    /** Skip reason: the entry has no usable id. */
    public static final String MISSING_ID = "missing-id";

    /** Skip reason: the entry has no upstream update time (the time base, never replaced by another field). */
    public static final String MISSING_OBSERVED_AT = "missing-observed-at";

    /** Skip reason: the id was already listed in this document. */
    public static final String DUPLICATE = "duplicate-id";

    /** Skip reason: the entry's geometry could not be read. */
    public static final String BAD_GEOMETRY = "bad-geometry";

    private final List<NormalizedRecord> records = new ArrayList<>();
    private final Set<String> ids = new LinkedHashSet<>();
    private final Set<String> ended = new LinkedHashSet<>();
    private final Map<String, Integer> skipped = new LinkedHashMap<>();
    private String nextUrl;

    /**
     * Adds a current event.
     *
     * @return false (and counts {@value #DUPLICATE}) when its id is already listed or marked ended
     */
    public boolean add(NormalizedRecord record) {
        if (ids.contains(record.externalId()) || ended.contains(record.externalId())) {
            skip(DUPLICATE);
            return false;
        }
        ids.add(record.externalId());
        records.add(record);
        return true;
    }

    /** Marks {@code id} as ended by the upstream; a record already added for it is removed. */
    public void ended(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        if (ids.remove(id)) {
            records.removeIf(r -> r.externalId().equals(id));
        }
        ended.add(id);
    }

    /** Counts one skipped entry. */
    public void skip(String reason) {
        skipped.merge(reason, 1, Integer::sum);
    }

    /** Sets the next page URL, as the upstream gave it (possibly relative). */
    public void nextUrl(String url) {
        this.nextUrl = url == null || url.isBlank() ? null : url.strip();
    }

    /** Appends another page's events and skips (its next link replaces this one's). */
    public void addAll(ParsedEvents page) {
        for (NormalizedRecord r : page.records) {
            add(r);
        }
        page.ended.forEach(this::ended);
        page.skipped.forEach((k, v) -> skipped.merge(k, v, Integer::sum));
        this.nextUrl = page.nextUrl;
    }

    /** The current events, in document order. */
    public List<NormalizedRecord> records() {
        return List.copyOf(records);
    }

    /** The ids of the current events. */
    public List<String> ids() {
        return List.copyOf(ids);
    }

    /** The ids the upstream marks as ended. */
    public List<String> endedIds() {
        return List.copyOf(ended);
    }

    /** Skipped entries by reason. */
    public Map<String, Integer> skipped() {
        return Map.copyOf(skipped);
    }

    /** The next page URL, or null. */
    public String nextUrl() {
        return nextUrl;
    }
}
