package ai.foresignal.raster.decode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.grib.GribIndexCache;
import ucar.nc2.util.DiskCache;
import ucar.nc2.util.DiskCache2;

/**
 * Pins NetCDF-Java's process-wide disk caches under {@code ${FORESIGNAL_DATA_DIR}/cache/netcdf}
 * (prompt 046, Weather-Imagery-Design §2.4), never the user's home directory.
 *
 * <ul>
 *   <li>{@link DiskCache} (where NetCDF-Java would uncompress a {@code .gz} it was asked to open) is
 *       rooted there and set to always use the cache, never the directory beside the file;
 *   <li>{@link DiskCache2#getDefault()} reads the {@code nj22.cache} system property on first use, so
 *       the property is set before any NetCDF-Java class can ask for it;
 *   <li>the GRIB index cache ({@code .gbx9}/{@code .ncx4}) gets a {@link DiskCache2} there too.
 *       Foresignal never builds those indexes ({@link Grib2Decoder} reads messages with
 *       {@code Grib2RecordScanner}), so the directory stays empty; pinning it means a future code path
 *       that did would still not write into the home directory.
 * </ul>
 *
 * <p>The settings are static in NetCDF-Java, so they are applied once per process, by the first
 * {@link SafeNetcdf} constructed.
 */
public final class NetcdfRuntime {

    private static final Logger log = LoggerFactory.getLogger(NetcdfRuntime.class);

    /** The system property {@code DiskCache2.getDefault()} reads its root from. */
    static final String DEFAULT_CACHE_PROPERTY = "nj22.cache";

    private static volatile Path configured;

    private NetcdfRuntime() {}

    /**
     * Pins the caches under {@code cacheRoot} (normally {@code cache/netcdf}). Later calls with the
     * same root are no-ops; a different root re-pins (tests use several data directories).
     *
     * @throws UncheckedIOException when the directory cannot be created
     */
    public static synchronized void configure(Path cacheRoot) {
        Path root = cacheRoot.toAbsolutePath().normalize();
        if (root.equals(configured)) {
            return;
        }
        Path grib = root.resolve("grib");
        try {
            Files.createDirectories(grib);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the NetCDF-Java cache " + root, e);
        }
        String dir = root.toString() + java.io.File.separator;
        System.setProperty(DEFAULT_CACHE_PROPERTY, dir);
        DiskCache.setRootDirectory(dir);
        DiskCache.setCachePolicy(true);
        DiskCache2 gribCache = new DiskCache2(grib.toString(), false, 0, 0);
        gribCache.setAlwaysUseCache(true);
        GribIndexCache.setDiskCache2(gribCache);
        configured = root;
        log.debug("NetCDF-Java disk caches pinned under {}", root);
    }

    /** The root the caches are pinned under, or null before {@link #configure}. */
    public static Path configuredRoot() {
        return configured;
    }
}
