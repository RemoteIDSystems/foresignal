package ai.foresignal.raster.decode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductStatusTracker;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The startup decoder self-test (prompt 046). */
class DecoderSelfTestTest {

    static final Instant NOW = Instant.parse("2026-10-12T18:00:00Z");

    @TempDir Path dataDir;

    RasterPaths paths;
    Grib2Decoder grib;
    NetcdfDecoder netcdf;
    ProductRegistry registry;
    ProductStatusTracker status;

    /** Two products: one decoded with CCSDS (an ECMWF-like model), one with PNG (an MRMS-like radar). */
    final DecoderDependent dependents = () -> Map.of(
            "test-ifs", Set.of(DecoderCapability.GRIB2_CCSDS),
            "test-radar", Set.of(DecoderCapability.GRIB2_PNG),
            "test-both", Set.of(DecoderCapability.GRIB2_PNG, DecoderCapability.GRIB2_CCSDS));

    @BeforeEach
    void setUp() {
        paths = new RasterPaths(dataDir);
        SafeNetcdf safe = new SafeNetcdf(paths);
        grib = new Grib2Decoder(safe);
        netcdf = new NetcdfDecoder(safe);
        registry = mock(ProductRegistry.class);
        when(registry.find(anyString())).thenAnswer(inv -> Optional.of(new ProductRegistry.Registered(
                new ProductSpec(inv.getArgument(0), "Test", ProductSpec.KIND_RASTER), "test-source", false)));
        status = new ProductStatusTracker(event -> { });
    }

    private DecoderSelfTest selfTest(DecoderSelfTest.FixtureSource source) {
        return new DecoderSelfTest(grib, netcdf, paths, registry, status, List.of(dependents),
                Clock.fixed(NOW, ZoneOffset.UTC), true, source);
    }

    private static DecoderSelfTest.FixtureSource bundled() {
        return file -> new ByteArrayInputStream(DecoderFixtures.bytes(DecoderSelfTest.RESOURCE_DIR + file));
    }

    @Test
    @DisplayName("all four bundled fixtures decode (5.3, 5.41, 5.42 through libaec, NetCDF4); nothing is disabled")
    void allPass() throws IOException {
        Map<DecoderCapability, DecoderSelfTest.Outcome> outcomes = selfTest(bundled()).run();

        assertThat(outcomes).containsOnlyKeys(DecoderCapability.values());
        outcomes.values().forEach(o -> assertThat(o.passed()).as("%s: %s", o.capability(), o.reason()).isTrue());
        assertThat(status.disabled("test-ifs")).isEmpty();
        assertThat(status.disabled("test-radar")).isEmpty();
        try (Stream<Path> left = Files.list(paths.selfTest())) {
            assertThat(left).as("unpacked fixtures are deleted").isEmpty();
        }
    }

    @Test
    @DisplayName("the shipped fixtures exist on the classpath, one per capability")
    void fixturesShipped() {
        assertThat(DecoderSelfTest.FIXTURES).extracting(DecoderSelfTest.Fixture::capability)
                .containsExactlyInAnyOrder(DecoderCapability.values());
        for (DecoderSelfTest.Fixture fixture : DecoderSelfTest.FIXTURES) {
            byte[] bytes = DecoderFixtures.bytes(DecoderSelfTest.RESOURCE_DIR + fixture.file());
            assertThat(bytes.length).as("%s is under 200 KB", fixture.file()).isBetween(1, 200 * 1024);
        }
    }

    @Test
    @DisplayName("a corrupted 5.42 fixture disables only the products that need CCSDS, with the reason")
    void corruptedCcsds() {
        DecoderSelfTest.FixtureSource corrupted = file -> {
            byte[] bytes = DecoderFixtures.bytes(DecoderSelfTest.RESOURCE_DIR + file);
            if (file.startsWith("ecmwf")) {
                bytes = Arrays.copyOf(bytes, 1_000);
            }
            return new ByteArrayInputStream(bytes);
        };
        DecoderSelfTest selfTest = selfTest(corrupted);
        Map<DecoderCapability, DecoderSelfTest.Outcome> outcomes = selfTest.run();

        assertThat(outcomes.get(DecoderCapability.GRIB2_CCSDS).passed()).isFalse();
        assertThat(outcomes.get(DecoderCapability.GRIB2_PNG).passed()).isTrue();
        assertThat(outcomes.get(DecoderCapability.GRIB2_COMPLEX_PACKING).passed()).isTrue();
        assertThat(outcomes.get(DecoderCapability.NETCDF4).passed()).isTrue();
        assertThat(status.disabled("test-ifs")).isPresent();
        assertThat(status.disabled("test-ifs").get().reason()).startsWith("decoder self-test failed (GRIB2 template 5.42");
        assertThat(status.disabled("test-both")).isPresent();
        assertThat(status.disabled("test-radar")).as("PNG still works").isEmpty();
        assertThat(selfTest.outcome(DecoderCapability.GRIB2_CCSDS)).get().extracting(DecoderSelfTest.Outcome::at)
                .isEqualTo(NOW);
        assertThat(selfTest.outcomes()).hasSize(4);
    }

    @Test
    @DisplayName("a missing fixture fails its capability rather than the whole self-test")
    void missingFixture() {
        DecoderSelfTest selfTest = selfTest(file -> {
            if (file.endsWith(".nc")) {
                throw new IOException("gone");
            }
            return bundled().open(file);
        });
        Map<DecoderCapability, DecoderSelfTest.Outcome> outcomes = selfTest.run();
        assertThat(outcomes.get(DecoderCapability.NETCDF4).passed()).isFalse();
        assertThat(outcomes.get(DecoderCapability.NETCDF4).reason()).contains("gone");
    }

    @Test
    @DisplayName("verify: sample count, data template and an all-missing field are each caught")
    void verify() {
        DecoderSelfTest.Fixture fixture = DecoderSelfTest.FIXTURES.get(0);
        GridDefinition grid = GridDefinition.latLon(2, 1, 0, 0, 2, 1);
        assertThat(DecoderSelfTest.verify(fixture, new DecodedGrid(grid, 2, 1, null, null, null, null, Map.of())))
                .isEqualTo("no samples decoded");
        assertThat(DecoderSelfTest.verify(fixture, new DecodedGrid(grid, 2, 1, new float[] {1, 2}, null, null, null,
                Map.of("dataTemplate", 3)))).startsWith("decoded 2 samples");
        float[] nan = new float[360 * 181];
        Arrays.fill(nan, Float.NaN);
        GridDefinition gfs = GridDefinition.latLon(360, 181, -180, -90, 180, 90);
        assertThat(DecoderSelfTest.verify(fixture, new DecodedGrid(gfs, 360, 181, nan, null, null, null,
                Map.of("dataTemplate", 3)))).isEqualTo("every decoded sample is missing");
        assertThat(DecoderSelfTest.verify(fixture, new DecodedGrid(gfs, 360, 181, nan, null, null, null,
                Map.of("dataTemplate", 40)))).contains("expected 5.3");
    }

    @Test
    @DisplayName("disabled by property: the ready event starts nothing")
    void disabled() {
        DecoderSelfTest off = new DecoderSelfTest(grib, netcdf, paths, registry, status, List.of(dependents),
                Clock.fixed(NOW, ZoneOffset.UTC), false, bundled());
        off.onReady();
        assertThat(off.outcomes()).isEmpty();
    }
}
