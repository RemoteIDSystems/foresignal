package ai.foresignal.plugin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The plugins the admin enabled after reviewing their SHA-256 (prompt 141), stored under
 * {@code plugins.approvals}. A plugin runs only while its {@code plugin.wasm} still hashes to the approved value; a
 * changed file needs a new review.
 *
 * @param approved plugin name to its approval
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PluginApprovals(Map<String, Approval> approved) {

    /** No plugin enabled. */
    public static final PluginApprovals NONE = new PluginApprovals(Map.of());

    /** Copies the map, ordered by name. */
    public PluginApprovals {
        approved = approved == null ? Map.of() : java.util.Collections.unmodifiableMap(new TreeMap<>(approved));
    }

    /** The approval of {@code name}, or empty. */
    public Optional<Approval> find(String name) {
        return Optional.ofNullable(approved.get(name));
    }

    /** These approvals with {@code name} approved at {@code sha256}. */
    public PluginApprovals with(String name, String sha256, Instant at) {
        Map<String, Approval> copy = new TreeMap<>(approved);
        copy.put(name, new Approval(sha256, at));
        return new PluginApprovals(copy);
    }

    /** These approvals without {@code name}. */
    public PluginApprovals without(String name) {
        Map<String, Approval> copy = new TreeMap<>(approved);
        copy.remove(name);
        return new PluginApprovals(copy);
    }

    /**
     * One approval.
     *
     * @param sha256 the reviewed SHA-256 of {@code plugin.wasm}
     * @param enabledAt when the admin enabled it
     */
    public record Approval(String sha256, Instant enabledAt) {}
}
