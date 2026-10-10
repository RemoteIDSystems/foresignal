package ai.elaro.source.spi;

/**
 * What a streaming adapter is given when its stream is started.
 *
 * <p>Same surface as {@link FetchContext}; the difference is the lifecycle, not the capabilities.
 */
public interface StreamContext extends FetchContext {}
