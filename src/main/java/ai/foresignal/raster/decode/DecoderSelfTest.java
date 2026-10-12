package ai.foresignal.raster.decode;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductStatusTracker;
import ai.foresignal.raster.RasterPaths;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The startup decoder self-test (prompt 046, Weather-Imagery-Design §3).
 *
 * <p>On {@code ApplicationReadyEvent} it starts a thread that decodes one bundled fixture per
 * {@link DecoderCapability} (under {@code src/main/resources/raster/selftest/}, provenance in that
 * directory's {@code README.md}):
 *
 * <ul>
 *   <li>GRIB2 template 5.3, a GFS 1° 2 m temperature message (complex packing);
 *   <li>GRIB2 template 5.41, an MRMS Hawaii MESH field (PNG);
 *   <li>GRIB2 template 5.42, an ECMWF open-data land-sea mask message (CCSDS/AEC, libaec through JNA,
 *       which extracts its native library under {@code jna.tmpdir});
 *   <li>NetCDF4/HDF5, a GOES-19 ABI derived-motion-winds file.
 * </ul>
 *
 * <p>A fixture passes when it decodes to the expected number of samples, with at least one finite
 * value, through the expected data template. For every capability that fails, the products that
 * need it ({@link DecoderDependent}) are {@linkplain ProductStatusTracker#disable disabled} with the
 * reason: their frame endpoints answer {@code 503 PRODUCT_UNAVAILABLE}, their source's health shows
 * it, and their adapter skips them. Products needing only working paths are untouched.
 *
 * <p>The fixtures are copied out of the classpath into {@code rasters/selftest/} first, because
 * NetCDF-Java reads files and {@link SafeNetcdf} opens nothing outside the raster directories.
 * Disable with {@code foresignal.raster.decoder-self-test.enabled=false}.
 */
@Component
public class DecoderSelfTest {

    private static final Logger log = LoggerFactory.getLogger(DecoderSelfTest.class);

    /** The classpath directory of the bundled fixtures. */
    public static final String RESOURCE_DIR = "raster/selftest/";

    /**
     * One bundled fixture.
     *
     * @param capability the decoding path it proves
     * @param file the file name under {@value #RESOURCE_DIR}
     * @param request what to decode
     * @param expectedCells the sample count it must decode to
     */
    public record Fixture(DecoderCapability capability, String file, DecodeRequest request, int expectedCells) {}

    /** The four fixtures, one per capability. */
    public static final List<Fixture> FIXTURES = List.of(
            new Fixture(DecoderCapability.GRIB2_COMPLEX_PACKING, "gfs-1p00-tmp2m-f000.grib2",
                    DecodeRequest.grib2(null, "TMP", 0, 0, 0, 103, 2.0, "2 m"), 360 * 181),
            new Fixture(DecoderCapability.GRIB2_PNG, "mrms-hawaii-mesh-60m-20261012-000000.grib2",
                    DecodeRequest.grib2(null, "MESH_Max_60min", 209, 3, 30, 102, 500.0, "500 m"), 2600 * 2200),
            new Fixture(DecoderCapability.GRIB2_CCSDS, "ecmwf-ifs-0p25-lsm-0h.grib2",
                    DecodeRequest.grib2(null, "lsm", 2, 0, 0, null, null, null), 1440 * 721),
            new Fixture(DecoderCapability.NETCDF4, "goes19-abi-dmwvc-c08-20261012-0001.nc",
                    DecodeRequest.netcdf(null, "wind_speed").valuesOnly(), 712));

    /**
     * The outcome of one capability.
     *
     * @param capability the decoding path
     * @param passed true when its fixture decoded as expected
     * @param reason why it failed, or null
     * @param at when it ran
     */
    public record Outcome(DecoderCapability capability, boolean passed, String reason, Instant at) {}

    /** Where fixture bytes come from: the classpath in production, a test's own bytes otherwise. */
    @FunctionalInterface
    public interface FixtureSource {

        /** Opens the fixture named {@code file}. */
        InputStream open(String file) throws IOException;
    }

    private final Grib2Decoder grib;
    private final NetcdfDecoder netcdf;
    private final RasterPaths paths;
    private final ProductRegistry registry;
    private final ProductStatusTracker status;
    private final List<DecoderDependent> dependents;
    private final Clock clock;
    private final boolean enabled;
    private final FixtureSource source;
    private final Map<DecoderCapability, Outcome> outcomes = Collections.synchronizedMap(new EnumMap<>(DecoderCapability.class));

    /** Creates the self-test over the bundled fixtures. */
    @Autowired
    public DecoderSelfTest(Grib2Decoder grib, NetcdfDecoder netcdf, RasterPaths paths, ProductRegistry registry,
            ProductStatusTracker status, List<DecoderDependent> dependents, Clock clock,
            @Value("${foresignal.raster.decoder-self-test.enabled:true}") boolean enabled) {
        this(grib, netcdf, paths, registry, status, dependents, clock, enabled,
                file -> {
                    InputStream in = DecoderSelfTest.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + file);
                    if (in == null) {
                        throw new IOException("bundled fixture " + RESOURCE_DIR + file + " is missing");
                    }
                    return in;
                });
    }

    /** Creates the self-test over {@code source} (tests). */
    public DecoderSelfTest(Grib2Decoder grib, NetcdfDecoder netcdf, RasterPaths paths, ProductRegistry registry,
            ProductStatusTracker status, List<DecoderDependent> dependents, Clock clock, boolean enabled,
            FixtureSource source) {
        this.grib = grib;
        this.netcdf = netcdf;
        this.paths = paths;
        this.registry = registry;
        this.status = status;
        this.dependents = List.copyOf(dependents);
        this.clock = clock;
        this.enabled = enabled;
        this.source = source;
    }

    /** Starts the self-test on its own thread once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.DECODER_SELF_TEST)
    public void onReady() {
        if (!enabled) {
            log.info("Decoder self-test disabled (foresignal.raster.decoder-self-test.enabled=false)");
            return;
        }
        Thread.ofPlatform().daemon(true).name("decoder-self-test").start(() -> {
            try {
                run();
            } catch (RuntimeException e) {
                log.error("The decoder self-test itself failed", e);
            }
        });
    }

    /**
     * Runs every fixture now, records the outcomes and disables the affected products.
     *
     * @return the outcome per capability
     */
    public Map<DecoderCapability, Outcome> run() {
        Map<DecoderCapability, Outcome> results = new EnumMap<>(DecoderCapability.class);
        for (Fixture fixture : FIXTURES) {
            results.put(fixture.capability(), check(fixture));
        }
        outcomes.putAll(results);
        disableAffected(results);
        List<String> failed = results.values().stream().filter(o -> !o.passed())
                .map(o -> o.capability().label() + ": " + o.reason()).toList();
        if (failed.isEmpty()) {
            log.info("Decoder self-test passed: {}", results.keySet());
        } else {
            log.error("Decoder self-test failed for {}", failed);
        }
        return results;
    }

    /** The latest outcome per capability (empty before the first run). */
    public Map<DecoderCapability, Outcome> outcomes() {
        synchronized (outcomes) {
            return Map.copyOf(outcomes);
        }
    }

    /** The latest outcome of {@code capability}, if it has run. */
    public Optional<Outcome> outcome(DecoderCapability capability) {
        return Optional.ofNullable(outcomes.get(capability));
    }

    private Outcome check(Fixture fixture) {
        Instant at = clock.instant();
        Path file = null;
        try {
            file = unpack(fixture.file());
            RasterDecoder decoder = grib.supports(fixture.request()) ? grib : netcdf;
            DecodedGrid grid = decoder.decode(file, fixture.request());
            String problem = verify(fixture, grid);
            return problem == null ? new Outcome(fixture.capability(), true, null, at)
                    : new Outcome(fixture.capability(), false, problem, at);
        } catch (IOException | RuntimeException | LinkageError e) {
            // LinkageError: libaec's native library or JNA failing to load is exactly what this is for.
            return new Outcome(fixture.capability(), false, e.getClass().getSimpleName() + ": " + e.getMessage(), at);
        } finally {
            GzipInputs.deleteQuietly(file);
        }
    }

    /** Null when {@code grid} is what {@code fixture} must decode to, else the problem. */
    static String verify(Fixture fixture, DecodedGrid grid) {
        if (!grid.hasData()) {
            return "no samples decoded";
        }
        if (grid.cells() != fixture.expectedCells()) {
            return "decoded " + grid.cells() + " samples, expected " + fixture.expectedCells();
        }
        int template = fixture.capability().gribTemplate();
        if (template >= 0 && !Integer.valueOf(template).equals(grid.metadata().get("dataTemplate"))) {
            return "decoded through data template " + grid.metadata().get("dataTemplate") + ", expected 5." + template;
        }
        for (int i = 0; i < grid.cells(); i++) {
            if (Float.isFinite(grid.value(i))) {
                return null;
            }
        }
        return "every decoded sample is missing";
    }

    private Path unpack(String file) throws IOException {
        Path dir = paths.selfTest();
        Files.createDirectories(dir);
        Path target = Files.createTempFile(dir, "selftest-", "-" + file);
        try (InputStream in = source.open(file)) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            GzipInputs.deleteQuietly(target);
            throw e;
        }
        return target;
    }

    private void disableAffected(Map<DecoderCapability, Outcome> results) {
        Map<String, List<String>> reasons = new LinkedHashMap<>();
        for (DecoderDependent dependent : dependents) {
            for (Map.Entry<String, Set<DecoderCapability>> entry : dependent.decoderRequirements().entrySet()) {
                for (DecoderCapability needed : entry.getValue()) {
                    Outcome outcome = results.get(needed);
                    if (outcome != null && !outcome.passed()) {
                        reasons.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                                .add(needed.label() + ": " + outcome.reason());
                    }
                }
            }
        }
        Instant now = clock.instant();
        reasons.forEach((productId, why) -> registry.find(productId).ifPresent(product -> {
            String reason = "decoder self-test failed (" + String.join("; ", why) + ")";
            status.disable(productId, product.sourceId(), reason, now);
            log.warn("Product {} disabled: {}", productId, reason);
        }));
    }
}
