package ai.foresignal.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The test plugins under {@code src/test/resources/plugins/} (prompt 141), assembled from their {@code plugin.wat}
 * sources by {@code scripts/plugins/AssembleWat.java} (the example's WAT lives in
 * {@code examples/plugins/geojson-passthrough/wat/}).
 *
 * <ul>
 *   <li>{@value #PASSTHROUGH}: the example parser (GeoJSON FeatureCollection to records);
 *   <li>{@value #MEMORY_HOG}: grows memory to 2048 pages before answering;
 *   <li>{@value #INFINITE_LOOP}: never returns;
 *   <li>{@value #OVERSIZED_OUTPUT}: claims 16 MB + 1 byte of output;
 *   <li>{@value #WASI_IMPORT}: imports {@code wasi_snapshot_preview1.fd_write};
 *   <li>{@value #LOG_SPAMMER}: calls {@code env.foresignal_log} 1000 times.
 * </ul>
 */
public final class PluginTestFiles {

    /** The example plugin. */
    public static final String PASSTHROUGH = "geojson-passthrough";

    /** The memory hog. */
    public static final String MEMORY_HOG = "memory-hog";

    /** The infinite loop. */
    public static final String INFINITE_LOOP = "infinite-loop";

    /** The oversized output. */
    public static final String OVERSIZED_OUTPUT = "oversized-output";

    /** The WASI importer. */
    public static final String WASI_IMPORT = "wasi-import";

    /** The log spammer. */
    public static final String LOG_SPAMMER = "log-spammer";

    /** The SHA-256 of the example plugin's committed {@code plugin.wasm}, as its {@code plugin.json} declares. */
    public static final String PASSTHROUGH_SHA256 = "232eeeefab43a723a76b0a15f8d1e8b67f8bf9aae0cfa2a7ffa8ba7c70d8f6f8";

    private PluginTestFiles() {}

    /** The bytes of {@code plugins/{plugin}/{file}} on the test class path. */
    public static byte[] bytes(String plugin, String file) {
        String path = "/plugins/" + plugin + "/" + file;
        try (InputStream in = PluginTestFiles.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("missing test resource " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A plugin's module. */
    public static byte[] wasm(String plugin) {
        return bytes(plugin, PluginCatalog.MODULE);
    }

    /** The example plugin's fixture, a GeoJSON FeatureCollection with four features. */
    public static byte[] fixture() {
        return bytes(PASSTHROUGH, "fixture.geojson");
    }

    /**
     * Copies a test plugin's module and manifest into {@code root/{name}/}.
     *
     * @param root the plugin root
     * @param plugin the test plugin
     * @return the plugin directory
     */
    public static Path install(Path root, String plugin) {
        return install(root, plugin, plugin, null);
    }

    /**
     * Copies a test plugin's module into {@code root/{name}/} with a manifest naming {@code name}; the declared hash is
     * {@code declaredSha256}, or the committed manifest's when null.
     *
     * @param root the plugin root
     * @param plugin the test plugin whose module is copied
     * @param name the directory and manifest name
     * @param declaredSha256 the hash to declare, or null for the real one
     * @return the plugin directory
     */
    public static Path install(Path root, String plugin, String name, String declaredSha256) {
        try {
            Path dir = Files.createDirectories(root.resolve(name));
            byte[] wasm = wasm(plugin);
            Files.write(dir.resolve(PluginCatalog.MODULE), wasm);
            String sha = declaredSha256 == null ? PluginCatalog.sha256(wasm) : declaredSha256;
            Files.writeString(dir.resolve(PluginCatalog.MANIFEST), manifest(name, sha), StandardCharsets.UTF_8);
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A valid manifest. */
    public static String manifest(String name, String sha256) {
        return "{\"name\":\"" + name + "\",\"version\":\"1.0.0\",\"abi\":1,\"kind\":\"parser\",\"sha256\":\"" + sha256
                + "\"}";
    }

    /** Deletes {@code dir} and everything under it, when it exists. */
    public static void deleteTree(Path dir) {
        if (dir == null || !Files.exists(dir, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
