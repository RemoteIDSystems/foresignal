package ai.foresignal.source;

/**
 * Which sources a live first-party aggregator supersedes (prompt R2-019). A superseded source stays registered and
 * may still be turned on by hand, but {@link SourceService} never seeds it enabled and {@code enable-core} and
 * {@code enable-free} skip it while the aggregator that supersedes it is live.
 */
public interface SupersededSources {

    /** True when a live aggregator supersedes {@code sourceId} and is set to keep it off. */
    boolean isSuperseded(String sourceId);
}
