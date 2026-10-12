package ai.foresignal.raster;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The on-disk layout under {@code ${FORESIGNAL_DATA_DIR}} (Weather-Imagery-Design §2.4), created at
 * startup:
 *
 * <pre>
 * rasters/tmp/                                          staging (atomic rename)
 * rasters/frames/{productId}/{yyyy}/{mm}/{dd}/{frameId}.fsg
 * rasters/fields/  rasters/glm/  rasters/satdump/       used from prompts 048, 049 and 051
 * media/  cache/netcdf/  tmp/                           used from prompts 043 and 046
 * </pre>
 *
 * <p>Frame paths are stored in {@code raster_frame.path} relative to {@code rasters/}, so moving the
 * data volume does not invalidate rows.
 */
@Component
public class RasterPaths {

    private static final Logger log = LoggerFactory.getLogger(RasterPaths.class);

    private final Path dataDir;
    private final Path root;

    /** Creates the layout under {@code foresignal.data-dir}. */
    @org.springframework.beans.factory.annotation.Autowired
    public RasterPaths(ForesignalProperties properties) {
        this(properties.dataDir());
    }

    /** Creates the layout under {@code dataDir} (tests). */
    public RasterPaths(Path dataDir) {
        this.dataDir = dataDir.toAbsolutePath().normalize();
        this.root = this.dataDir.resolve("rasters");
        createLayout();
    }

    /** Creates every directory of the layout; existing ones are left alone. */
    public final void createLayout() {
        List<Path> dirs = List.of(tmp(), frames(), root.resolve("fields"), root.resolve("glm"),
                root.resolve("satdump"), dataDir.resolve("media"), dataDir.resolve("cache").resolve("netcdf"),
                dataDir.resolve("tmp"));
        for (Path dir : dirs) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not create raster directory " + dir, e);
            }
        }
        log.debug("Raster directory layout present under {}", root);
    }

    /** {@code rasters/}. */
    public Path root() {
        return root;
    }

    /** {@code rasters/tmp/}. */
    public Path tmp() {
        return root.resolve("tmp");
    }

    /** {@code rasters/frames/}. */
    public Path frames() {
        return root.resolve("frames");
    }

    /** {@code rasters/frames/{productId}/}. */
    public Path productFrames(String productId) {
        return frames().resolve(productId);
    }

    /**
     * The path of a frame relative to {@code rasters/}:
     * {@code frames/{productId}/{yyyy}/{mm}/{dd}/{frameId}.fsg}, dated by the valid time (UTC).
     */
    public String relativeFramePath(String productId, Instant validTime, String frameId) {
        ZonedDateTime t = validTime.atZone(ZoneOffset.UTC);
        return String.format(Locale.ROOT, "frames/%s/%04d/%02d/%02d/%s.fsg", productId, t.getYear(), t.getMonthValue(),
                t.getDayOfMonth(), frameId);
    }

    /**
     * The absolute path of a stored relative path.
     *
     * @throws IllegalArgumentException when {@code relative} escapes {@code rasters/}
     */
    public Path resolve(String relative) {
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Frame path escapes the raster store: " + relative);
        }
        return resolved;
    }

    /** The relative form of an absolute path under {@code rasters/}, with forward slashes. */
    public String relativize(Path absolute) {
        return root.relativize(absolute.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }
}
