package ai.foresignal.store.query;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.TimeField;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import ai.foresignal.security.restrict.ReadRestriction;

/**
 * A playback range over {@code record_version} ({@code GET /v1/history}).
 *
 * @param sources source filter; empty for all
 * @param kinds kind filter; empty for all
 * @param externalIds external id filter; empty for all
 * @param bbox spatial filter on the version's geometry, or null
 * @param near radius filter, or null
 * @param since start of the range on {@code timeField} (inclusive); required
 * @param until end of the range (exclusive), or null for now
 * @param timeField the time axis; {@code ingested} becomes seq bounds through {@code seq_clock}
 * @param cursor the opaque cursor of the previous page, or null
 * @param limit page size
 * @param fields the projection
 * @param geometry geometry output options
 * @param encoding geometry text encoding
 * @param includeObserver include observers
 * @param excludedSources sources the caller may not see
 * @param restriction the caller's read restriction (prompt 035; never null)
 */
public record HistoryQuery(
        List<String> sources,
        List<String> kinds,
        List<String> externalIds,
        BboxFilter bbox,
        NearFilter near,
        Instant since,
        Instant until,
        TimeField timeField,
        String cursor,
        int limit,
        FieldSelection fields,
        GeometryOptions geometry,
        GeometryEncoding encoding,
        boolean includeObserver,
        Set<String> excludedSources,
        ReadRestriction restriction) {

    /** An unrestricted query (the shape before prompt 035). */
    public HistoryQuery(List<String> sources, List<String> kinds, List<String> externalIds, BboxFilter bbox,
            NearFilter near, Instant since, Instant until, TimeField timeField, String cursor, int limit,
            FieldSelection fields, GeometryOptions geometry, GeometryEncoding encoding, boolean includeObserver,
            Set<String> excludedSources) {
        this(sources, kinds, externalIds, bbox, near, since, until, timeField, cursor, limit, fields, geometry,
                encoding, includeObserver, excludedSources, ReadRestriction.NONE);
    }

    /** Normalizes nulls. */
    public HistoryQuery {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        externalIds = externalIds == null ? List.of() : List.copyOf(externalIds);
        timeField = timeField == null ? TimeField.OBSERVED : timeField;
        fields = fields == null ? FieldSelection.ALL : fields;
        geometry = geometry == null ? GeometryOptions.DEFAULT : geometry;
        encoding = encoding == null ? GeometryEncoding.GEOJSON : encoding;
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }

    /** This query under {@code r}. */
    public HistoryQuery withRestriction(ReadRestriction r) {
        return new HistoryQuery(sources, kinds, externalIds, bbox, near, since, until, timeField, cursor, limit, fields,
                geometry, encoding, includeObserver, excludedSources, r);
    }

    /** The filters and ordering that define the range, for the cursor fingerprint. */
    public String canonical() {
        return String.join("|",
                "history", String.join(",", sources), String.join(",", kinds), String.join(",", externalIds),
                bbox == null ? "" : bbox.canonical(), near == null ? "" : near.canonical(),
                String.valueOf(since), String.valueOf(until), timeField.name(), restriction.canonical());
    }
}
