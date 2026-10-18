package ai.foresignal.plugin;

import com.fasterxml.jackson.annotation.JsonValue;

/** A plugin's state as {@code GET /v1/plugins} reports it (prompt 141, OpenAPI {@code Plugin.state}). */
public enum PluginState {

    /** Not enabled by the admin, or plugins are off. */
    DISABLED("disabled"),
    /** Enabled, plugins on, hashes match and the module loads: it runs when a source calls it. */
    ENABLED("enabled"),
    /** The file does not hash to the declared or the approved SHA-256; it will not load. */
    HASH_MISMATCH("hash-mismatch"),
    /** The manifest or module is invalid; it will not load. */
    ERROR("error");

    private final String wire;

    PluginState(String wire) {
        this.wire = wire;
    }

    /** The wire value. */
    @JsonValue
    public String wire() {
        return wire;
    }
}
