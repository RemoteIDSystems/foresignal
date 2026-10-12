package ai.foresignal.media;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * The media layout under {@code ${FORESIGNAL_DATA_DIR}} (Weather-Imagery-Design §2.4), created at
 * startup:
 *
 * <pre>
 * media/tmp/                  staging (atomic rename)
 * media/{ab}/{sha256}.{ext}   content-addressed files; {ab} is the first two hex characters
 * </pre>
 *
 * <p>Stored paths ({@code media.path}) are relative to {@code media/}.
 */
@Component
public class MediaPaths {

    /** The staging directory's name under {@code media/}. */
    public static final String TMP = "tmp";

    private final Path root;

    /** Creates the layout under {@code foresignal.data-dir}. */
    @org.springframework.beans.factory.annotation.Autowired
    public MediaPaths(ForesignalProperties properties) {
        this(properties.dataDir());
    }

    /** Creates the layout under {@code dataDir} (tests). */
    public MediaPaths(Path dataDir) {
        this.root = dataDir.toAbsolutePath().normalize().resolve("media");
        try {
            Files.createDirectories(tmp());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the media directory " + tmp(), e);
        }
    }

    /** {@code media/}. */
    public Path root() {
        return root;
    }

    /** {@code media/tmp/}. */
    public Path tmp() {
        return root.resolve(TMP);
    }

    /** The stored relative path of a file: {@code {ab}/{sha256}.{ext}}. */
    public static String relative(String sha256, String extension) {
        String hex = sha256.toLowerCase(Locale.ROOT);
        return hex.substring(0, 2) + "/" + hex + "." + extension;
    }

    /**
     * The absolute path of a stored relative path.
     *
     * @throws IllegalArgumentException when {@code relative} escapes {@code media/}
     */
    public Path resolve(String relative) {
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root) || resolved.startsWith(tmp())) {
            throw new IllegalArgumentException("Media path escapes the media store: " + relative);
        }
        return resolved;
    }

    /** The relative form of an absolute path under {@code media/}, with forward slashes. */
    public String relativize(Path absolute) {
        return root.relativize(absolute.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }
}
