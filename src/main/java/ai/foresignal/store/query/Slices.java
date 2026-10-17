package ai.foresignal.store.query;

import java.util.List;

/** Result shapes of {@link RecordQueryService}. */
public final class Slices {

    private Slices() {}

    /**
     * A page of current records.
     *
     * @param rows the rows
     * @param nextCursor the cursor of the next page, or null on the last page
     * @param watermarkSeq {@code ChangeBus.lastPublishedSeq()} read before the query
     * @param asOfSeq the pinned snapshot seq, or null when not pinned
     */
    public record RecordSlice(List<RecordRow> rows, String nextCursor, long watermarkSeq, Long asOfSeq) {}

    /**
     * Current records of one kind and source in an area (prompt 129).
     *
     * @param kind the kind
     * @param sourceId the source
     * @param count every current non-tombstone record
     * @param live the unexpired ones
     */
    public record AreaCount(String kind, String sourceId, long count, long live) {}

    /**
     * A page of versions (versions endpoint or history).
     *
     * @param rows the rows
     * @param nextCursor the cursor of the next page, or null on the last page
     */
    public record VersionSlice(List<RecordRow> rows, String nextCursor) {}

    /**
     * A page of changes.
     *
     * @param rows the rows, in seq order
     * @param lastSeq pass as {@code after} next time
     * @param hasMore true when more changes matched than {@code limit}
     * @param watermarkSeq {@code ChangeBus.lastPublishedSeq()} at query time
     * @param purgeHorizon the purge horizon over the filtered sources
     */
    public record ChangeSlice(List<RecordRow> rows, long lastSeq, boolean hasMore, long watermarkSeq, long purgeHorizon) {}

    /**
     * One kind currently present.
     *
     * @param kind the kind
     * @param count current records of it
     * @param sources the sources emitting it, sorted
     */
    public record KindRow(String kind, long count, List<String> sources) {}
}
