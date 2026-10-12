package ai.foresignal.store.timeline;

/**
 * Counts a validated timeline (prompt 037). {@link RawTimelineBackend} counts
 * {@code record_version}; prompt 038 serves ranges older than retention from
 * {@code activity_rollup} through its own implementation (declared {@code @Primary}, delegating the
 * retained part to the raw backend).
 */
public interface TimelineBackend {

    /** The histogram of {@code query}. */
    Timeline timeline(TimelineQuery query);
}
