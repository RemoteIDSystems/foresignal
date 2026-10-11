package ai.foresignal.source;

import ai.foresignal.source.spi.SourceDescriptor;

/**
 * The seam through which {@link SourceService} adds health to its views (left open in prompt 004).
 *
 * <p>An interface in this package, implemented in {@code ai.foresignal.collect}, rather than a
 * direct dependency: health reads the budget governor, which reads source settings, so a direct
 * constructor dependency would be a cycle. {@link SourceService} resolves it lazily.
 *
 * <p>Implementations must not call back into {@link SourceService#view} or
 * {@link SourceService#list}, which call this.
 */
public interface SourceHealthLookup {

    /**
     * The health summary for one source.
     *
     * @param descriptor the source's descriptor
     * @param enabled whether its settings row has it enabled
     */
    SourceHealthSummary summary(SourceDescriptor descriptor, boolean enabled);
}
