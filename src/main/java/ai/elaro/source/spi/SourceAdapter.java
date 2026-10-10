package ai.elaro.source.spi;

/**
 * Anything that describes a source.
 *
 * <p>Adapters are Spring {@code @Component}s; {@link ai.elaro.source.SourceRegistry} discovers them
 * by type. A duplicate id fails startup rather than letting one adapter silently shadow another.
 */
public interface SourceAdapter {

    /** This source's immutable description. Must return the same value every call. */
    SourceDescriptor descriptor();

    /** Convenience for {@code descriptor().id()}. */
    default String id() {
        return descriptor().id();
    }
}
