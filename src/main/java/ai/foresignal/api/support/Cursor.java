package ai.foresignal.api.support;

import java.time.Instant;

/**
 * A keyset position, carried to clients as an opaque string by {@link CursorCodec}.
 *
 * <p>Every variant may carry {@code w}, the snapshot seq pinned on page 1 ({@code asOfSeq} or the
 * watermark under {@code order=id}); later pages filter {@code seq <= w} with the same value.
 */
public sealed interface Cursor {

    /** The pinned snapshot seq, or null when the listing is not pinned. */
    Long w();

    /**
     * {@code order=observed}: the last row's {@code (observed_at, source_id, external_id)}, newest first.
     *
     * @param o the observed time
     * @param s the source id
     * @param e the external id
     * @param w the pinned seq, or null
     */
    record Observed(Instant o, String s, String e, Long w) implements Cursor {}

    /**
     * {@code order=id} and asOf: the last row's {@code (source_id, external_id)}, ascending.
     *
     * @param s the source id
     * @param e the external id
     * @param w the pinned seq, or null
     */
    record ById(String s, String e, Long w) implements Cursor {}

    /**
     * {@code sort=distance}: the last row's {@code (distance in meters, source_id, external_id)}.
     *
     * @param d the distance in meters
     * @param s the source id
     * @param e the external id
     * @param w the pinned seq, or null
     */
    record Distance(double d, String s, String e, Long w) implements Cursor {}

    /**
     * Versions: the last row's seq, ascending.
     *
     * @param q the seq
     * @param w always null; present for the interface
     */
    record Seq(long q, Long w) implements Cursor {}

    /**
     * History: the last row's {@code (time on the selected axis, seq)}; {@code o} is null on the
     * ingested axis, which orders by seq alone.
     *
     * @param o the time value, or null
     * @param q the seq
     * @param w always null; present for the interface
     */
    record History(Instant o, long q, Long w) implements Cursor {}
}
