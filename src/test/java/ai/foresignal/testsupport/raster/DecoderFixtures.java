package ai.foresignal.testsupport.raster;

import ai.foresignal.raster.decode.SafeNetcdf;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The prompt 046 decoder fixtures: the committed MRMS files ({@code fixtures/mrms/}), the bundled
 * self-test files ({@code raster/selftest/}) and the downloaded GOES file
 * ({@code build/test-fixtures/goes/}, from {@code ./gradlew downloadTestFixtures}).
 */
public final class DecoderFixtures {

    private static final Logger log = LoggerFactory.getLogger(DecoderFixtures.class);

    /** Committed MRMS composite reflectivity, 2026-10-12 12:00:37Z. */
    public static final String MRMS_REFLECTIVITY = "MRMS_MergedReflectivityQCComposite_00.50_20261012-120037.grib2.gz";

    /** Committed MRMS precipitation rate, 2026-10-12 12:00:00Z. */
    public static final String MRMS_PRECIP_RATE = "MRMS_PrecipRate_00.00_20261012-120000.grib2.gz";

    /** Committed MRMS precipitation flag, 2026-10-12 12:00:00Z. */
    public static final String MRMS_PRECIP_FLAG = "MRMS_PrecipFlag_00.00_20261012-120000.grib2.gz";

    /** Committed MRMS 60-minute MESH, 2026-10-12 12:00:00Z. */
    public static final String MRMS_MESH = "MRMS_MESH_Max_60min_00.50_20261012-120000.grib2.gz";

    /** Committed MRMS Hawaii MESH, the no-coverage-heavy sample. */
    public static final String MRMS_HAWAII_MESH = "HAWAII_MRMS_MESH_Max_60min_00.50_20261012-000000.grib2.gz";

    /** The downloaded GOES-19 CMIPC band 13 file. */
    public static final String GOES_C13 = "OR_ABI-L2-CMIPC-M6C13_G19_s20262851201170_e20262851203554_c20262851204053.nc";

    /** Where {@code downloadTestFixtures} puts the GOES file. */
    public static final Path GOES_DIR = Path.of("build", "test-fixtures", "goes");

    /** MRMS CONUS cells. */
    public static final int MRMS_CELLS = 7000 * 3500;

    private DecoderFixtures() {}

    /** A guard whose only root is {@code dir} (caches under {@code dir/../nccache}). */
    public static SafeNetcdf safe(Path dir) {
        return new SafeNetcdf(List.of(dir), dir.resolveSibling(dir.getFileName() + "-nccache"));
    }

    /** Copies the MRMS fixture {@code name} into {@code dir}. */
    public static Path mrms(String name, Path dir) {
        return copy("fixtures/mrms/" + name, dir.resolve(name));
    }

    /** Copies the self-test fixture {@code name} into {@code dir}. */
    public static Path selfTest(String name, Path dir) {
        return copy("raster/selftest/" + name, dir.resolve(name));
    }

    /** The bytes of the MRMS fixture {@code name}. */
    public static byte[] mrmsBytes(String name) {
        return bytes("fixtures/mrms/" + name);
    }

    /** The bytes of a classpath resource. */
    public static byte[] bytes(String resource) {
        try (InputStream in = DecoderFixtures.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource " + resource);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The downloaded GOES file, if present. */
    public static Optional<Path> goes() {
        Path file = GOES_DIR.resolve(GOES_C13);
        return Files.isRegularFile(file) ? Optional.of(file) : Optional.empty();
    }

    /**
     * Copies the downloaded GOES file into {@code dir}, or skips the test with a logged reason when
     * {@code ./gradlew downloadTestFixtures} has not been run.
     */
    public static Path goesOrSkip(Path dir) {
        Optional<Path> source = goes();
        if (source.isEmpty()) {
            log.warn("Skipping: {} is absent; run ./gradlew downloadTestFixtures to fetch it", GOES_DIR.resolve(GOES_C13));
        }
        Assumptions.assumeTrue(source.isPresent(), "GOES fixture absent: run ./gradlew downloadTestFixtures");
        try {
            Path target = dir.resolve(GOES_C13);
            Files.createDirectories(dir);
            Files.copy(source.get(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The bytes of the downloaded GOES file, or skips like {@link #goesOrSkip}. */
    public static byte[] goesBytesOrSkip() {
        Optional<Path> source = goes();
        if (source.isEmpty()) {
            log.warn("Skipping: {} is absent; run ./gradlew downloadTestFixtures to fetch it", GOES_DIR.resolve(GOES_C13));
        }
        Assumptions.assumeTrue(source.isPresent(), "GOES fixture absent: run ./gradlew downloadTestFixtures");
        try {
            return Files.readAllBytes(source.get());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path copy(String resource, Path target) {
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes(resource));
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
