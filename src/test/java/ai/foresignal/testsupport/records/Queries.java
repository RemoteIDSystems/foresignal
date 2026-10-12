package ai.foresignal.testsupport.records;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.store.query.ChangesQuery;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.HistoryQuery;
import ai.foresignal.store.query.ListOrder;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Fluent builders for the read queries, with the API defaults, for integration tests. */
public final class Queries {

    private Queries() {}

    /** A current-state query builder. */
    public static Current current() {
        return new Current();
    }

    /** A history query builder starting at {@code since}. */
    public static History history(Instant since) {
        History h = new History();
        h.since = since;
        return h;
    }

    /** A change-feed query builder. */
    public static Changes changes(long after) {
        Changes c = new Changes();
        c.after = after;
        return c;
    }

    /** Builds {@link CurrentQuery}. */
    public static final class Current {
        public List<String> sources = List.of();
        public List<String> kinds = List.of();
        public List<String> externalIds = List.of();
        public BboxFilter bbox;
        public NearFilter near;
        public Instant since;
        public Instant until;
        public TimeField timeField = TimeField.EFFECTIVE;
        public String text;
        public boolean includeExpired;
        public ListOrder order = ListOrder.OBSERVED;
        public Long asOfSeq;
        public String cursor;
        public int limit = 500;
        public FieldSelection fields = FieldSelection.ALL;
        public GeometryOptions geometry = GeometryOptions.DEFAULT;
        public GeometryEncoding encoding = GeometryEncoding.GEOJSON;
        public boolean includeObserver;
        public Set<String> excluded = Set.of();

        public Current sources(String... ids) { sources = List.of(ids); return this; }
        public Current kinds(String... values) { kinds = List.of(values); return this; }
        public Current ids(String... values) { externalIds = List.of(values); return this; }
        public Current bbox(BboxFilter value) { bbox = value; return this; }
        public Current near(NearFilter value) { near = value; return this; }
        public Current since(Instant value) { since = value; return this; }
        public Current until(Instant value) { until = value; return this; }
        public Current timeField(TimeField value) { timeField = value; return this; }
        public Current text(String value) { text = value; return this; }
        public Current includeExpired(boolean value) { includeExpired = value; return this; }
        public Current order(ListOrder value) { order = value; return this; }
        public Current asOfSeq(Long value) { asOfSeq = value; return this; }
        public Current cursor(String value) { cursor = value; return this; }
        public Current limit(int value) { limit = value; return this; }
        public Current fields(FieldSelection value) { fields = value; return this; }
        public Current geometry(GeometryOptions value) { geometry = value; return this; }
        public Current encoding(GeometryEncoding value) { encoding = value; return this; }
        public Current includeObserver(boolean value) { includeObserver = value; return this; }
        public Current excluded(String... ids) { excluded = Set.of(ids); return this; }

        /** The query. */
        public CurrentQuery build() {
            return new CurrentQuery(sources, kinds, externalIds, bbox, near, since, until, timeField, text, includeExpired,
                    order, asOfSeq, cursor, limit, fields, geometry, encoding, includeObserver, excluded);
        }
    }

    /** Builds {@link HistoryQuery}. */
    public static final class History {
        public List<String> sources = List.of();
        public List<String> kinds = List.of();
        public List<String> externalIds = List.of();
        public BboxFilter bbox;
        public NearFilter near;
        public Instant since;
        public Instant until;
        public TimeField timeField = TimeField.OBSERVED;
        public String cursor;
        public int limit = 5000;
        public FieldSelection fields = FieldSelection.ALL;
        public GeometryEncoding encoding = GeometryEncoding.GEOJSON;
        public Set<String> excluded = Set.of();

        public History sources(String... ids) { sources = List.of(ids); return this; }
        public History kinds(String... values) { kinds = List.of(values); return this; }
        public History ids(String... values) { externalIds = List.of(values); return this; }
        public History bbox(BboxFilter value) { bbox = value; return this; }
        public History near(NearFilter value) { near = value; return this; }
        public History until(Instant value) { until = value; return this; }
        public History timeField(TimeField value) { timeField = value; return this; }
        public History cursor(String value) { cursor = value; return this; }
        public History limit(int value) { limit = value; return this; }
        public History fields(FieldSelection value) { fields = value; return this; }
        public History encoding(GeometryEncoding value) { encoding = value; return this; }

        /** The query. */
        public HistoryQuery build() {
            return new HistoryQuery(sources, kinds, externalIds, bbox, near, since, until, timeField, cursor, limit, fields,
                    GeometryOptions.DEFAULT, encoding, false, excluded);
        }
    }

    /** Builds {@link ChangesQuery}. */
    public static final class Changes {
        public long after;
        public List<String> sources = List.of();
        public List<String> kinds = List.of();
        public List<String> externalIds = List.of();
        public BboxFilter bbox;
        public boolean payload;
        public int limit = 1000;
        public FieldSelection fields = FieldSelection.ALL;
        public GeometryEncoding encoding = GeometryEncoding.GEOJSON;
        public Set<String> excluded = Set.of();

        public Changes sources(String... ids) { sources = List.of(ids); return this; }
        public Changes kinds(String... values) { kinds = List.of(values); return this; }
        public Changes ids(String... values) { externalIds = List.of(values); return this; }
        public Changes bbox(BboxFilter value) { bbox = value; return this; }
        public Changes payload(boolean value) { payload = value; return this; }
        public Changes limit(int value) { limit = value; return this; }
        public Changes fields(FieldSelection value) { fields = value; return this; }
        public Changes encoding(GeometryEncoding value) { encoding = value; return this; }
        public Changes excluded(String... ids) { excluded = Set.of(ids); return this; }

        /** The query. */
        public ChangesQuery build() {
            return new ChangesQuery(after, sources, kinds, externalIds, bbox, payload, limit, fields,
                    GeometryOptions.DEFAULT, encoding, excluded);
        }
    }
}
