package ai.foresignal.collect;

import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;

/**
 * What a {@link HealthRule} is given.
 *
 * @param descriptor the source's descriptor
 * @param health the source's health row
 * @param now the evaluation instant, from the injected clock
 */
public record HealthRuleContext(SourceDescriptor descriptor, HealthSnapshot health, Instant now) {

    /** The source id. */
    public String sourceId() {
        return descriptor.id();
    }
}
