package ai.foresignal.mapping;

import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Set;

/**
 * What a mapping is compiled against (prompt 059).
 *
 * @param kinds the target source's kinds, or null when no source is known (then {@code kind} is required)
 * @param observedAtMode the source's time base, or null when unknown
 * @param strictTimeBase true to fail on a time-base mismatch (saving and ingest), false to only warn (preview)
 */
public record MappingContext(Set<String> kinds, ObservedAtMode observedAtMode, boolean strictTimeBase) {

    /** Copies the kinds. */
    public MappingContext {
        kinds = kinds == null ? null : Set.copyOf(kinds);
    }

    /** No source: kind required, time base unchecked. */
    public static MappingContext none() {
        return new MappingContext(null, null, false);
    }

    /** The context of {@code descriptor}. */
    public static MappingContext of(SourceDescriptor descriptor, boolean strict) {
        return new MappingContext(descriptor.kinds(), descriptor.observedAtMode(), strict);
    }

    /** The single kind of the source, or null. */
    public String singleKind() {
        return kinds != null && kinds.size() == 1 ? kinds.iterator().next() : null;
    }
}
