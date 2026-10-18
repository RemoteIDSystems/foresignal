package ai.foresignal.plugin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The {@code plugins} setting (prompt 141, OpenAPI schema {@code PluginSettings}): whether out-of-tree WASM plugins may
 * run at all. Off by default; while off no plugin runs, whatever its own state, and none can be enabled.
 *
 * @param enabled true to allow plugins; null in a request is refused
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PluginSettings(Boolean enabled) {

    /** The shipped default: off. */
    public static final PluginSettings DEFAULTS = new PluginSettings(false);

    /** True when plugins are allowed. */
    public boolean on() {
        return Boolean.TRUE.equals(enabled);
    }
}
