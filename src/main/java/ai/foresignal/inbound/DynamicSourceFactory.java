package ai.foresignal.inbound;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;

/**
 * Builds the registration of a {@code custom_source} row that is not a plain custom source: the
 * EDP core implements it for external providers, whose descriptor comes from the stored manifest
 * (prompt 024). {@link CustomSourceRegistrar} asks every factory before falling back to
 * {@link CustomSourceDescriptors}.
 */
public interface DynamicSourceFactory {

    /** True when this factory builds {@code row}'s registration. */
    boolean handles(CustomSourceEntity row);

    /**
     * The registration of {@code row}.
     *
     * @return the descriptor and its implementation (null for a PUSH source)
     */
    Registration build(CustomSourceEntity row);

    /**
     * A dynamic source's registration.
     *
     * @param descriptor its descriptor
     * @param adapter its implementation, or null for a PUSH source
     */
    record Registration(SourceDescriptor descriptor, SourceAdapter adapter) {}
}
