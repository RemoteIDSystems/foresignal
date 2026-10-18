package ai.foresignal.plugin;

import java.util.Objects;

/**
 * A plugin could not be loaded or did not complete a call (prompt 141). The {@link Reason} says which rule refused it;
 * the message names the plugin and never carries plugin output beyond a short excerpt.
 */
public class PluginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Why a plugin was refused or failed. */
    public enum Reason {
        /** The {@code plugins} setting is off. */
        DISABLED,
        /** The plugin exists but the admin has not enabled it. */
        NOT_ENABLED,
        /** No plugin directory with that name. */
        UNKNOWN,
        /** {@code plugin.json} is missing, unreadable or invalid, or names an ABI or kind this host does not run. */
        INVALID_MANIFEST,
        /** The file does not hash to the declared or the approved SHA-256. */
        HASH_MISMATCH,
        /** The module does not parse, imports something not granted, or lacks a required export. */
        INVALID_MODULE,
        /** The input is larger than the input cap. */
        INPUT_TOO_LARGE,
        /** {@code alloc} returned 0 or a pointer outside memory. */
        OUT_OF_MEMORY,
        /** The call ran past the wall-clock timeout and was interrupted. */
        TIMEOUT,
        /** The module trapped (unreachable, out-of-bounds access, failed memory growth, stack overflow). */
        TRAP,
        /** {@code foresignal_parse} returned 0. */
        NO_OUTPUT,
        /** The output's length prefix exceeds the output cap. */
        OUTPUT_TOO_LARGE,
        /** The output pointer or length lies outside memory. */
        OUTPUT_OUT_OF_BOUNDS,
        /** The output is not the ABI's JSON document. */
        INVALID_OUTPUT,
        /** Every plugin slot stayed busy for longer than the wait allows. */
        BUSY
    }

    private final String plugin;
    private final Reason reason;

    /**
     * Creates the exception.
     *
     * @param plugin the plugin name
     * @param reason the rule that refused it
     * @param message what happened, for logs and health reasons
     */
    public PluginException(String plugin, Reason reason, String message) {
        this(plugin, reason, message, null);
    }

    /**
     * Creates the exception with a cause.
     *
     * @param plugin the plugin name
     * @param reason the rule that refused it
     * @param message what happened
     * @param cause the underlying failure, or null
     */
    public PluginException(String plugin, Reason reason, String message, Throwable cause) {
        super("plugin " + plugin + ": " + message, cause);
        this.plugin = plugin;
        this.reason = Objects.requireNonNull(reason);
    }

    /** The plugin name. */
    public String plugin() {
        return plugin;
    }

    /** Why it was refused. */
    public Reason reason() {
        return reason;
    }
}
