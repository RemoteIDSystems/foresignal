package ai.foresignal.search;

import java.time.Instant;
import java.util.Objects;

/**
 * A document to embed: its key, filter columns and text (prompt 027).
 *
 * @param docType {@code record}, {@code briefing} or {@code alert}
 * @param docId {@code <source>:<externalId>} for a record
 * @param sourceId the source, or null
 * @param kind the record kind, or null
 * @param seq the persisted version seq the text came from, or null
 * @param geomHex the geometry as hex EWKB (SRID 4326), or null
 * @param effectiveTime {@code COALESCE(event_time, observed_at)} of that version, or null
 * @param observedAt the observation time, or null
 * @param text the text to embed
 */
public record DocInput(String docType, String docId, String sourceId, String kind, Long seq, String geomHex,
        Instant effectiveTime, Instant observedAt, String text) {

    /** Record documents. */
    public static final String RECORD = "record";

    /** Briefing runs (029). */
    public static final String BRIEFING = "briefing";

    /** An autonomous analyst note (prompt 102). */
    public static final String ANALYST_NOTE = "analyst_note";

    /** Enriched alerts (030). */
    public static final String ALERT = "alert";

    /**
     * User-contributed evidence items (prompt 130), embedded with their attachments' text. They are work products
     * under access lists, so general searches leave them out; the saved-search runner asks for them by type.
     */
    public static final String EVIDENCE_ITEM = "evidence_item";

    /** Validates the members and cuts the text to {@link EmbeddingText#MAX_CHARS}. */
    public DocInput {
        Objects.requireNonNull(docType, "docType");
        Objects.requireNonNull(docId, "docId");
        Objects.requireNonNull(text, "text");
        text = EmbeddingText.cut(text);
    }

    /** The {@code doc_id} of a record. */
    public static String recordId(String sourceId, String externalId) {
        return sourceId + ":" + externalId;
    }

    /** {@code text_hash}. */
    public long textHash() {
        return EmbeddingText.hash(text);
    }
}
