package ai.foresignal.plugin;

import ai.foresignal.source.spi.SourceDescriptor;

/**
 * Runs a named parser plugin on raw upstream bytes (prompt 141). {@link PluginRuntime} is the production
 * implementation; adapter tests supply a stand-in.
 */
@FunctionalInterface
public interface PluginParsers {

    /**
     * Parses once.
     *
     * @param plugin the plugin name
     * @param input the raw upstream bytes
     * @param descriptor the calling source's descriptor (its kinds resolve a record's {@code kind})
     * @return the mapped output
     * @throws PluginException when plugins are off, the plugin is not enabled or no longer matches its reviewed hash,
     *     or the call fails a limit or answers something that is not the ABI's document
     */
    ParserOutput parse(String plugin, byte[] input, SourceDescriptor descriptor);

    /**
     * Checks, before any upstream request is spent, that {@code plugin} could run now. The default checks nothing.
     *
     * @param plugin the plugin name
     * @throws PluginException when plugins are off, the plugin is unknown or not enabled, or its file no longer
     *     matches the reviewed hash
     */
    default void requireAvailable(String plugin) {}
}
