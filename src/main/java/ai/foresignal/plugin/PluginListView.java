package ai.foresignal.plugin;

import java.util.List;

/**
 * The plugin subsystem's state (prompt 141, OpenAPI schema {@code PluginList}).
 *
 * @param enabled the {@code plugins} setting
 * @param plugins every plugin directory found, ordered by name
 */
public record PluginListView(boolean enabled, List<PluginView> plugins) {

    /** Copies the list. */
    public PluginListView {
        plugins = plugins == null ? List.of() : List.copyOf(plugins);
    }
}
