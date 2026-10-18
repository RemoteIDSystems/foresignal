package ai.foresignal.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * The plugin directories under one root, normally {@code ${FORESIGNAL_DATA_DIR}/plugins/} (prompt 141).
 *
 * <p>A plugin is a directory whose name matches {@link PluginManifest#NAME} and that holds {@value #MODULE} and
 * {@value #MANIFEST}. Links are refused (a linked directory or file is reported as an error, never followed), other
 * entries are ignored, and a missing root holds no plugins. Discovery reads and hashes the module but never parses or
 * runs it.
 */
public final class PluginCatalog {

    /** The module file. */
    public static final String MODULE = "plugin.wasm";

    /** The manifest file. */
    public static final String MANIFEST = "plugin.json";

    /** The plugin folder under the data directory. */
    public static final String FOLDER = "plugins";

    /** The largest manifest read. */
    public static final int MAX_MANIFEST_BYTES = 64 * 1024;

    /** The largest module read. */
    public static final int MAX_MODULE_BYTES = 32 * 1024 * 1024;

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private final Path root;

    /**
     * Creates the catalog.
     *
     * @param root the plugin root; it need not exist
     */
    public PluginCatalog(Path root) {
        this.root = root;
    }

    /** The plugin root. */
    public Path root() {
        return root;
    }

    /** Every plugin directory, ordered by name. */
    public List<DiscoveredPlugin> discover() {
        List<DiscoveredPlugin> out = new ArrayList<>();
        if (root == null || !Files.isDirectory(root)) {
            return out;
        }
        List<Path> dirs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (PluginManifest.NAME.matcher(name).matches()
                        && (Files.isDirectory(entry) || Files.isSymbolicLink(entry))) {
                    dirs.add(entry);
                }
            }
        } catch (IOException e) {
            return out;
        }
        dirs.sort(Comparator.comparing(p -> p.getFileName().toString()));
        for (Path dir : dirs) {
            out.add(inspect(dir));
        }
        return out;
    }

    /**
     * One plugin directory.
     *
     * @param name the plugin name
     * @return the plugin, or empty when the name is invalid or no such directory exists
     */
    public Optional<DiscoveredPlugin> find(String name) {
        if (name == null || !PluginManifest.NAME.matcher(name).matches() || root == null) {
            return Optional.empty();
        }
        Path dir = root.resolve(name);
        if (!Files.isDirectory(dir) && !Files.isSymbolicLink(dir)) {
            return Optional.empty();
        }
        return Optional.of(inspect(dir));
    }

    /**
     * Reads a plugin's module.
     *
     * @param plugin the plugin
     * @return the module bytes
     * @throws PluginException ({@link PluginException.Reason#INVALID_MODULE}) when the file is missing, a link, too
     *     large or unreadable
     */
    public byte[] readModule(DiscoveredPlugin plugin) {
        Path file = plugin.directory().resolve(MODULE);
        try {
            return readRegular(file, MAX_MODULE_BYTES);
        } catch (IOException e) {
            throw new PluginException(plugin.name(), PluginException.Reason.INVALID_MODULE, e.getMessage(), e);
        }
    }

    /** The lower-case hex SHA-256 of {@code bytes}. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private DiscoveredPlugin inspect(Path dir) {
        String name = dir.getFileName().toString();
        if (Files.isSymbolicLink(dir)) {
            return new DiscoveredPlugin(name, dir, null, null, "the plugin directory is a link; links are refused");
        }
        List<String> problems = new ArrayList<>();
        PluginManifest manifest = null;
        try {
            byte[] json = readRegular(dir.resolve(MANIFEST), MAX_MANIFEST_BYTES);
            PluginManifest bound = MAPPER.readValue(json, PluginManifest.class);
            if (bound == null) {
                problems.add(MANIFEST + " is empty");
            } else {
                List<String> manifestProblems = bound.problems(name);
                manifestProblems.forEach(p -> problems.add(MANIFEST + ": " + p));
                manifest = manifestProblems.isEmpty() ? bound : null;
            }
        } catch (IOException e) {
            problems.add(e.getMessage());
        } catch (JacksonException e) {
            problems.add(MANIFEST + " does not bind: " + e.getOriginalMessage());
        }
        String actual = null;
        try {
            actual = sha256(readRegular(dir.resolve(MODULE), MAX_MODULE_BYTES));
        } catch (IOException e) {
            problems.add(e.getMessage());
        }
        return new DiscoveredPlugin(name, dir, manifest, actual, problems.isEmpty() ? null : String.join("; ", problems));
    }

    private static byte[] readRegular(Path file, int maxBytes) throws IOException {
        String label = file.getFileName().toString();
        if (Files.isSymbolicLink(file)) {
            throw new IOException(label + " is a link; links are refused");
        }
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException(label + " is missing");
        }
        try (InputStream in = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            byte[] bytes = in.readNBytes(maxBytes + 1);
            if (bytes.length > maxBytes) {
                throw new IOException(label + " is larger than " + maxBytes + " bytes");
            }
            return bytes;
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith(label)) {
                throw e;
            }
            throw new IOException(label + " could not be read: " + e.getMessage(), e);
        }
    }
}
