package ai.foresignal.plugin;

import java.nio.file.Path;

/**
 * A plugin directory found under {@code ${FORESIGNAL_DATA_DIR}/plugins/} (prompt 141). Discovery reads and hashes
 * files but never runs anything.
 *
 * @param name the directory name
 * @param directory the directory
 * @param manifest the bound {@code plugin.json}, or null when it is missing or does not bind
 * @param actualSha256 the SHA-256 of {@code plugin.wasm} as read now, or null when the file is missing or unreadable
 * @param error why the plugin cannot run (manifest problems, an unreadable module), or null
 */
public record DiscoveredPlugin(String name, Path directory, PluginManifest manifest, String actualSha256,
        String error) {

    /** True when the manifest's declared SHA-256 equals the file's. */
    public boolean hashMatches() {
        return manifest != null && actualSha256 != null && actualSha256.equals(manifest.sha256());
    }
}
