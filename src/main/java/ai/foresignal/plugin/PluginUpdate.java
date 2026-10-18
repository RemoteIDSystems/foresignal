package ai.foresignal.plugin;

/**
 * The body of {@code PUT /v1/plugins/{name}} (prompt 141, OpenAPI schema {@code PluginUpdate}).
 *
 * @param enabled true to enable, false to disable; required
 * @param sha256 the SHA-256 the admin reviewed; required to enable and must equal the file's actual hash
 */
public record PluginUpdate(Boolean enabled, String sha256) {}
