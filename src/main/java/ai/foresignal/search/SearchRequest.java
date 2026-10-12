package ai.foresignal.search;

import ai.foresignal.api.support.BboxFilter;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One validated search (prompt 027).
 *
 * @param q the query text
 * @param mode {@code text}, {@code semantic} or {@code hybrid}
 * @param docTypes the document types to search; empty for all
 * @param sources source filter; empty for all
 * @param kinds kind filter; empty for all
 * @param bbox area filter, or null
 * @param since lower time bound (inclusive), or null
 * @param until upper time bound (exclusive), or null
 * @param observedAxis true to filter on {@code observed_at}, false for the effective time (default)
 * @param limit the most results (1–100)
 */
public record SearchRequest(String q, Mode mode, Set<String> docTypes, List<String> sources, List<String> kinds,
        BboxFilter bbox, Instant since, Instant until, boolean observedAxis, int limit) {

    /** The search modes. */
    public enum Mode {
        /** Full-text only; always available. */
        TEXT,
        /** Cosine distance to the embedded query. */
        SEMANTIC,
        /** Reciprocal rank fusion of both (default). */
        HYBRID;

        /** The wire value. */
        public String wireValue() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        /** The mode of a wire value. */
        public static Mode of(String value) {
            return valueOf(value.toUpperCase(java.util.Locale.ROOT));
        }
    }

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 100;

    /** The default {@code limit}. */
    public static final int DEFAULT_LIMIT = 25;

    /** The document types. */
    public static final List<String> DOC_TYPES = List.of(DocInput.RECORD, DocInput.BRIEFING, DocInput.ALERT);

    /** Validates and copies the members. */
    public SearchRequest {
        Objects.requireNonNull(q, "q");
        Objects.requireNonNull(mode, "mode");
        docTypes = docTypes == null ? Set.of() : Set.copyOf(docTypes);
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be 1–" + MAX_LIMIT);
        }
    }

    /** True when record documents are searched. */
    public boolean includesRecords() {
        return docTypes.isEmpty() || docTypes.contains(DocInput.RECORD);
    }
}
