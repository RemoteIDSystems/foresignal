package ai.foresignal.store.query;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.TimeField;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * A current-state listing over {@code record_head ⋈ record_live} (Core-Data-Model §12).
 *
 * @param sources source filter; empty for all
 * @param kinds kind filter; empty for all
 * @param externalIds external id filter; empty for all
 * @param bbox spatial filter, or null
 * @param near radius filter, or null
 * @param since lower bound (inclusive) on {@code timeField}, or null
 * @param until upper bound (exclusive) on {@code timeField}, or null
 * @param timeField the time axis of {@code since}/{@code until}
 * @param text case-insensitive match on {@code search_text}, or null
 * @param includeExpired include records past {@code validUntil}
 * @param order the ordering
 * @param asOfSeq the snapshot seq to pin, or null
 * @param cursor the opaque cursor of the previous page, or null
 * @param limit page size
 * @param fields the projection
 * @param geometry geometry output options
 * @param encoding geometry text encoding
 * @param includeObserver read observers from the head's version
 * @param excludedSources sources the caller may not see (private sources for anonymous callers)
 */
public record CurrentQuery(
        List<String> sources,
        List<String> kinds,
        List<String> externalIds,
        BboxFilter bbox,
        NearFilter near,
        Instant since,
        Instant until,
        TimeField timeField,
        String text,
        boolean includeExpired,
        ListOrder order,
        Long asOfSeq,
        String cursor,
        int limit,
        FieldSelection fields,
        GeometryOptions geometry,
        GeometryEncoding encoding,
        boolean includeObserver,
        Set<String> excludedSources) {

    /** Normalizes nulls. */
    public CurrentQuery {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        externalIds = externalIds == null ? List.of() : List.copyOf(externalIds);
        timeField = timeField == null ? TimeField.EFFECTIVE : timeField;
        order = order == null ? ListOrder.OBSERVED : order;
        fields = fields == null ? FieldSelection.ALL : fields;
        geometry = geometry == null ? GeometryOptions.DEFAULT : geometry;
        encoding = encoding == null ? GeometryEncoding.GEOJSON : encoding;
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }

    /** True when the listing is pinned to a snapshot seq ({@code asOfSeq} or {@code order=id}). */
    public boolean pinned() {
        return asOfSeq != null || order == ListOrder.ID;
    }

    /** The filters and ordering that define the listing, for the cursor fingerprint. */
    public String canonical() {
        return String.join("|",
                "records", String.join(",", sources), String.join(",", kinds), String.join(",", externalIds),
                bbox == null ? "" : bbox.canonical(), near == null ? "" : near.canonical(),
                String.valueOf(since), String.valueOf(until), timeField.name(), text == null ? "" : text,
                String.valueOf(includeExpired), order.name(), String.valueOf(asOfSeq));
    }
}
