package ai.foresignal.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * One plugin as the API reports it (prompt 141, OpenAPI schema {@code Plugin}).
 *
 * <p>{@code abi} and {@code kind} are always {@code 1} and {@code parser}, the only ones this host runs; a manifest
 * declaring others is in state {@code error} with the declared value in {@code error}. {@code sha256} is the declared
 * hash, or the file's own when the manifest gives none (empty when neither is known).
 *
 * @param name the plugin name
 * @param version the declared version (empty when the manifest is invalid)
 * @param abi always 1
 * @param kind always {@code parser}
 * @param sha256 the declared SHA-256
 * @param actualSha256 the SHA-256 of {@code plugin.wasm} as read now; absent when the file is missing
 * @param hashMatches true when the declared and actual hashes are equal
 * @param enabled true when the admin enabled this plugin (its approval is stored)
 * @param state the state
 * @param error why the plugin will not load, or null
 * @param sourceIds the {@code wasm-feed} sources naming this plugin
 * @param loadedAt when its current module was last loaded to run, or null
 */
public record PluginView(
        String name,
        String version,
        int abi,
        String kind,
        String sha256,
        @JsonInclude(JsonInclude.Include.NON_NULL) String actualSha256,
        boolean hashMatches,
        boolean enabled,
        PluginState state,
        String error,
        List<String> sourceIds,
        Instant loadedAt) {

    /** Copies the source ids. */
    public PluginView {
        sourceIds = sourceIds == null ? List.of() : List.copyOf(sourceIds);
    }
}
