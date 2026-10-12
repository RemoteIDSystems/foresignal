package ai.foresignal.raster.decode;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.raster.RasterPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.io.RandomAccessFile;

/**
 * The only place Foresignal opens a file with NetCDF-Java (prompt 046, Weather-Imagery-Design §3).
 *
 * <p>NetCDF-Java's {@code NetcdfFiles.open(String)} and {@code NetcdfDatasets.openDataset(String)}
 * accept URLs ({@code http:}, {@code https:}, {@code dods:}, {@code cdms3:}), so a location string
 * that reached them from anywhere else would let a file name make Foresignal fetch an arbitrary URL:
 * server-side request forgery, outside the budgeted {@code UpstreamClient}. This class accepts only a
 * {@link Path} (there is deliberately no {@code String} overload), and only one that is
 *
 * <ul>
 *   <li>not a symbolic link, and a regular file;
 *   <li>under {@code ${FORESIGNAL_DATA_DIR}/rasters/tmp} (downloads) or {@code rasters/selftest} (the
 *       self-test's unpacked fixtures), by real path.
 * </ul>
 *
 * <p>{@code SafeNetcdfTest} scans {@code src/main/java} and fails if any other file calls
 * {@code NetcdfFiles.open} or {@code NetcdfDatasets.openDataset}. Constructing one also pins
 * NetCDF-Java's disk caches ({@link NetcdfRuntime}).
 */
@Component
public class SafeNetcdf {

    private final List<Path> roots;

    /** Creates the guard for the raster layout. */
    @Autowired
    public SafeNetcdf(RasterPaths paths) {
        this(List.of(paths.tmp(), paths.selfTest()), paths.netcdfCache());
    }

    /**
     * Creates a guard with explicit roots (tests).
     *
     * @param roots the directories files may be opened in
     * @param cacheRoot where NetCDF-Java's disk caches are pinned
     */
    public SafeNetcdf(List<Path> roots, Path cacheRoot) {
        this.roots = roots.stream().map(SafeNetcdf::realRoot).toList();
        NetcdfRuntime.configure(cacheRoot);
    }

    /** The directories files may be opened in, as real paths. */
    public List<Path> roots() {
        return roots;
    }

    /**
     * Checks {@code file} and returns its real path.
     *
     * @throws RasterFormatException when it is a symbolic link, not a regular file, or outside the roots
     */
    public Path requireAllowed(Path file) {
        if (file == null) {
            throw new RasterFormatException("No file given");
        }
        if (Files.isSymbolicLink(file)) {
            throw new RasterFormatException("Refusing to decode a symbolic link: " + file.getFileName());
        }
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new RasterFormatException("Not a regular file: " + file.getFileName());
        }
        Path real;
        try {
            real = file.toRealPath();
        } catch (IOException e) {
            throw new RasterFormatException("Cannot resolve " + file.getFileName() + ": " + e.getMessage(), e);
        }
        for (Path root : roots) {
            if (real.startsWith(root) && !real.equals(root)) {
                return real;
            }
        }
        throw new RasterFormatException("Refusing to decode " + file.getFileName()
                + ": it is outside the raster temp and self-test directories");
    }

    /**
     * Opens {@code file} without enhancement.
     *
     * @throws RasterFormatException when the file is not allowed or not readable by NetCDF-Java
     */
    public NetcdfFile open(Path file) {
        Path real = requireAllowed(file);
        try {
            return NetcdfFiles.open(real.toString());
        } catch (IOException | RuntimeException e) {
            throw new RasterFormatException("NetCDF-Java cannot open " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Opens {@code file} in enhanced mode ({@code scale_factor}, {@code add_offset},
     * {@code _Unsigned}, {@code _FillValue} and coordinate systems applied).
     *
     * @throws RasterFormatException when the file is not allowed or not readable by NetCDF-Java
     */
    public NetcdfDataset openDataset(Path file) {
        Path real = requireAllowed(file);
        try {
            return NetcdfDatasets.openDataset(real.toString(), true, null);
        } catch (IOException | RuntimeException e) {
            throw new RasterFormatException("NetCDF-Java cannot open " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Opens {@code file} as a read-only random access file, for {@code Grib2RecordScanner}.
     *
     * @throws RasterFormatException when the file is not allowed or cannot be opened
     */
    public RandomAccessFile openRandomAccess(Path file) {
        Path real = requireAllowed(file);
        try {
            return new RandomAccessFile(real.toString(), "r");
        } catch (IOException e) {
            throw new RasterFormatException("Cannot open " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private static Path realRoot(Path root) {
        try {
            Files.createDirectories(root);
            return root.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot prepare decoder directory " + root, e);
        }
    }
}
