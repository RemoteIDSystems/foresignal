package ai.foresignal.cli.config;

import ai.foresignal.cli.CliUsageException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Reads and writes {@code config.json}, the CLI's named profiles:
 *
 * <pre>{@code
 * {"currentProfile": "default",
 *  "profiles": {"default": {"url": "https://foresignal.example", "token": "fsr_…"}}}
 * }</pre>
 *
 * <p>On POSIX file systems the directory is created {@code 0700} and the file is written {@code 0600} (a temporary
 * file created with those permissions, then moved over the old one), so the token is never readable by other
 * users, not even briefly. Elsewhere (Windows) a warning says the file cannot be protected.
 */
public final class ConfigStore {

    /** The profile used when neither {@code --profile} nor {@code currentProfile} names one. */
    public static final String DEFAULT_PROFILE = "default";

    /** The file name inside the configuration directory. */
    public static final String FILE_NAME = "config.json";

    private static final Set<PosixFilePermission> FILE_PERMISSIONS = PosixFilePermissions.fromString("rw-------");
    private static final Set<PosixFilePermission> DIR_PERMISSIONS = PosixFilePermissions.fromString("rwx------");

    private final Path dir;
    private final PrintWriter err;
    private final JsonMapper json = JsonMapper.builder().build();

    /**
     * Creates a store over a directory.
     *
     * @param dir the configuration directory
     * @param err where permission warnings go
     */
    public ConfigStore(Path dir, PrintWriter err) {
        this.dir = dir;
        this.err = err;
    }

    /**
     * The configuration file.
     *
     * @return {@code <dir>/config.json}
     */
    public Path file() {
        return dir.resolve(FILE_NAME);
    }

    /**
     * Whether the directory's file system supports POSIX permissions.
     *
     * @return true on Linux and macOS
     */
    public boolean posix() {
        Path probe = Files.exists(dir) ? dir : dir.toAbsolutePath().getRoot();
        if (probe == null) {
            probe = dir.toAbsolutePath();
        }
        return probe.getFileSystem().supportedFileAttributeViews().contains("posix");
    }

    /**
     * Reads the whole configuration; a missing file is an empty configuration.
     *
     * @return the configuration document
     * @throws CliUsageException when the file exists but cannot be read or is not a JSON object
     */
    public ObjectNode load() {
        Path file = file();
        if (!Files.exists(file)) {
            return json.createObjectNode();
        }
        warnIfTooOpen(file);
        try {
            JsonNode node = json.readTree(Files.readString(file, StandardCharsets.UTF_8));
            if (node == null || !node.isObject()) {
                throw new CliUsageException(file + " is not a JSON object; fix or delete it and log in again");
            }
            return (ObjectNode) node;
        } catch (IOException | JacksonException e) {
            throw new CliUsageException("Cannot read " + file + ": " + e.getMessage(), e);
        }
    }

    /**
     * One profile.
     *
     * @param name the profile name, or {@code null} for {@code currentProfile} (else {@value #DEFAULT_PROFILE})
     * @return the profile, empty when it does not exist
     */
    public Optional<Profile> profile(String name) {
        ObjectNode config = load();
        String selected = name != null ? name : text(config, "currentProfile");
        if (selected == null) {
            selected = DEFAULT_PROFILE;
        }
        JsonNode node = config.path("profiles").path(selected);
        if (!node.isObject()) {
            return Optional.empty();
        }
        return Optional.of(new Profile(selected, text(node, "url"), text(node, "token")));
    }

    /**
     * Creates or replaces a profile and makes it the current one.
     *
     * @param name  the profile name
     * @param url   the server's base URL
     * @param token the API token, or {@code null} to keep the profile's existing token
     * @return the path written
     * @throws CliUsageException when the file cannot be written
     */
    public Path saveProfile(String name, String url, String token) {
        ObjectNode config = load();
        ObjectNode profiles = config.has("profiles") && config.get("profiles").isObject()
                ? (ObjectNode) config.get("profiles")
                : config.putObject("profiles");
        ObjectNode profile = profiles.has(name) && profiles.get(name).isObject()
                ? (ObjectNode) profiles.get(name)
                : profiles.putObject(name);
        profile.put("url", Connection.normalizeUrl(url));
        if (token != null) {
            profile.put("token", token);
        }
        config.put("currentProfile", name);
        write(config);
        return file();
    }

    /**
     * Writes the configuration with owner-only permissions where the platform allows it.
     *
     * @param config the whole document
     * @throws CliUsageException when the file cannot be written
     */
    public void write(ObjectNode config) {
        byte[] bytes = json.writerWithDefaultPrettyPrinter().writeValueAsBytes(config);
        Path file = file();
        try {
            if (posix()) {
                writePosix(file, bytes);
            } else {
                Files.createDirectories(dir);
                Files.write(file, bytes);
                err.println("warning: " + file + " holds an API token, and file permissions cannot be restricted on "
                        + "this platform; make sure only your account can read it");
                err.flush();
            }
        } catch (IOException e) {
            throw new CliUsageException("Cannot write " + file + ": " + e.getMessage(), e);
        }
    }

    private void writePosix(Path file, byte[] bytes) throws IOException {
        if (!Files.exists(dir)) {
            FileAttribute<Set<PosixFilePermission>> dirAttr = PosixFilePermissions.asFileAttribute(DIR_PERMISSIONS);
            Files.createDirectories(dir, dirAttr);
        }
        Path tmp = dir.resolve(FILE_NAME + ".tmp");
        Files.deleteIfExists(tmp);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS);
        try {
            Files.createFile(tmp, attr);
        } catch (FileAlreadyExistsException e) {
            throw new IOException("Another process is writing " + tmp, e);
        }
        // The umask can only remove bits, but set them explicitly in case the file system ignored the attribute.
        Files.setPosixFilePermissions(tmp, FILE_PERMISSIONS);
        Files.write(tmp, bytes);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.setPosixFilePermissions(file, FILE_PERMISSIONS);
    }

    private void warnIfTooOpen(Path file) {
        if (!posix()) {
            return;
        }
        try {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(file);
            if (!FILE_PERMISSIONS.containsAll(perms)) {
                err.println("warning: " + file + " is readable by other users ("
                        + PosixFilePermissions.toString(perms) + "); run 'chmod 600 " + file + "'");
                err.flush();
            }
        } catch (IOException | UnsupportedOperationException e) {
            // Permissions unknown; nothing to warn about.
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isString() ? null : value.asString();
    }
}
