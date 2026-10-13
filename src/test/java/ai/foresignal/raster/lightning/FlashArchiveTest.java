package ai.foresignal.raster.lightning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.place.PlacesAoiService;
import ai.foresignal.raster.RasterProfile;
import ai.foresignal.source.adapters.goes.GlmParser;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.test.MutableClock;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * The GLM archive and ring (prompt 048): the 16-byte little-endian layout, the torn-tail truncation,
 * day splitting, the ring rebuilt from the archive, AOI filtering, the quality-flag filter, the read API,
 * retention and the scope rules.
 */
class FlashArchiveTest {

    static final Instant NOW = Instant.parse("2026-10-13T00:20:00Z");
    static final double[] TEXAS = {-106.65, 25.84, -93.51, 36.50};

    @TempDir Path root;

    MutableClock clock;
    FlashArchive archive;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        archive = new FlashArchive(root.resolve("glm"));
    }

    private static FlashBatch batch(long[] t, float[] lat, float[] lon, float[] e) {
        return new FlashBatch(t, lat, lon, e);
    }

    private static long ms(String iso) {
        return Instant.parse(iso).toEpochMilli();
    }

    @Test
    @DisplayName("each flash is 16 bytes, little-endian: int32 msOfDayUtc, float32 lat, lon, energyFj")
    void layout() throws Exception {
        archive.append("G19", batch(new long[] {ms("2026-10-13T00:00:01.250Z")}, new float[] {30.25f},
                new float[] {-97.75f}, new float[] {123.5f}));

        Path file = archive.file("G19", LocalDate.of(2026, 10, 13));
        byte[] bytes = Files.readAllBytes(file);
        assertThat(bytes).hasSize(FlashArchive.RECORD_BYTES);
        ByteBuffer b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(b.getInt()).isEqualTo(1_250);
        assertThat(b.getFloat()).isEqualTo(30.25f);
        assertThat(b.getFloat()).isEqualTo(-97.75f);
        assertThat(b.getFloat()).isEqualTo(123.5f);
        assertThat(file.getParent().getFileName().toString()).isEqualTo("G19");
        assertThat(file.getFileName().toString()).isEqualTo("20261013.bin");
    }

    @Test
    @DisplayName("a granule straddling midnight goes to two daily files, read back in order")
    void splitsByDay() {
        long before = ms("2026-10-12T23:59:55Z");
        long after = ms("2026-10-13T00:00:05Z");
        archive.append("G19", batch(new long[] {before, after}, new float[] {1, 2}, new float[] {3, 4}, new float[] {5, 6}));

        FlashBatch day12 = archive.read("G19", LocalDate.of(2026, 10, 12), 0, Long.MAX_VALUE);
        FlashBatch day13 = archive.read("G19", LocalDate.of(2026, 10, 13), 0, Long.MAX_VALUE);
        assertThat(day12.timesMs()).containsExactly(before);
        assertThat(day13.timesMs()).containsExactly(after);
        assertThat(archive.totalBytes()).isEqualTo(2L * FlashArchive.RECORD_BYTES);
        assertThat(archive.read("G19", LocalDate.of(2026, 10, 13), after + 1, Long.MAX_VALUE).isEmpty()).isTrue();
        assertThat(archive.read("G19", LocalDate.of(2026, 10, 1), 0, Long.MAX_VALUE).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("a torn trailing record is ignored by readers and truncated on the next append")
    void tornTail() throws Exception {
        long t = ms("2026-10-13T00:10:00Z");
        archive.append("G19", batch(new long[] {t, t + 1}, new float[] {1, 2}, new float[] {3, 4}, new float[] {5, 6}));
        Path file = archive.file("G19", LocalDate.of(2026, 10, 13));
        Files.write(file, new byte[] {9, 9, 9, 9, 9, 9, 9}, StandardOpenOption.APPEND);
        assertThat(Files.size(file)).isEqualTo(39);

        assertThat(archive.read("G19", LocalDate.of(2026, 10, 13), 0, Long.MAX_VALUE).size()).isEqualTo(2);

        archive.append("G19", batch(new long[] {t + 2}, new float[] {7}, new float[] {8}, new float[] {9}));
        assertThat(Files.size(file)).isEqualTo(48);
        FlashBatch all = archive.read("G19", LocalDate.of(2026, 10, 13), 0, Long.MAX_VALUE);
        assertThat(all.timesMs()).containsExactly(t, t + 1, t + 2);
        assertThat(all.lat()).containsExactly(1f, 2f, 7f);
    }

    @Test
    @DisplayName("satellite names cannot leave the archive root")
    void satelliteNames() {
        assertThatThrownBy(() -> archive.file("../etc", LocalDate.of(2026, 10, 13)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> archive.file("", LocalDate.of(2026, 10, 13))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("files of days older than the cutoff's day are deleted; their bytes are reported")
    void retention() {
        for (int day = 1; day <= 13; day++) {
            long t = LocalDate.of(2026, 10, day).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() + 1000;
            archive.append("G19", batch(new long[] {t}, new float[] {1}, new float[] {2}, new float[] {3}));
        }
        long deleted = archive.deleteBefore(Instant.parse("2026-10-10T12:00:00Z"));

        assertThat(deleted).isEqualTo(9L * FlashArchive.RECORD_BYTES);
        assertThat(archive.files()).hasSize(4);
        assertThat(archive.files().get(0).getFileName().toString()).isEqualTo("20261010.bin");
    }

    @Test
    @DisplayName("the ring keeps only flashes inside the AOI geometry and answers the read API")
    void aoiFilterAndReads() {
        FlashStore store = new FlashStore(archive, null, FlashStore.Scope.AOI, clock);
        long t = NOW.minus(Duration.ofMinutes(2)).toEpochMilli();
        store.append("G19", batch(new long[] {t, t + 1000, t + 2000, t + 3000},
                new float[] {30.27f, 30.30f, 45.0f, 31.0f}, new float[] {-97.74f, -97.70f, -80.0f, -97.0f},
                new float[] {10, 20, 30, 40}), NOW.minusSeconds(60));

        assertThat(store.ringFlashes()).as("the Atlantic flash is outside the Texas box").isEqualTo(3);
        assertThat(archive.read("G19", LocalDate.of(2026, 10, 13), 0, Long.MAX_VALUE).size())
                .as("the archive keeps the full field of view").isEqualTo(4);
        assertThat(store.newestFlashFileTime()).contains(NOW.minusSeconds(60));

        Instant since = NOW.minus(Duration.ofMinutes(15));
        assertThat(store.flashesWithin(Geo.point(-97.74, 30.27), 6, since).size()).isEqualTo(2);
        assertThat(store.nearestKm(Geo.point(-97.74, 30.27), since).getAsDouble()).isLessThan(0.01);
        assertThat(store.nearestKm(Geo.point(-97.74, 30.27), NOW.plusSeconds(1))).isEmpty();
        assertThat(store.countWithin(Geo.point(-97.74, 30.27).buffer(0.1), since)).isEqualTo(2);
        assertThat(store.between(Instant.ofEpochMilli(t), Instant.ofEpochMilli(t + 1000)).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("with places, the ring follows the AOI geometry and is rebuilt when it changes")
    void placesGeometry() {
        PlacesAoiService places = mock(PlacesAoiService.class);
        when(places.version()).thenReturn(1L);
        when(places.aoi()).thenReturn(square(-98, 30, -97, 31));
        FlashStore store = new FlashStore(archive, places, FlashStore.Scope.AOI, clock);
        long t = NOW.minus(Duration.ofMinutes(1)).toEpochMilli();
        FlashBatch flashes = batch(new long[] {t, t + 1}, new float[] {30.5f, 33.5f}, new float[] {-97.5f, -100.5f},
                new float[] {1, 2});
        store.append("G19", flashes, NOW);
        assertThat(store.ringFlashes()).isEqualTo(1);

        // The AOI moves to cover the second flash: the next append rebuilds the ring from the archive.
        when(places.version()).thenReturn(2L);
        when(places.aoi()).thenReturn(square(-101, 33, -100, 34));
        store.append("G19", FlashBatch.EMPTY, NOW);
        assertThat(store.ringFlashes()).isEqualTo(1);
        assertThat(store.nearestKm(Geo.point(-100.5, 33.5), NOW.minus(Duration.ofHours(1))).getAsDouble()).isLessThan(0.01);
    }

    @Test
    @DisplayName("on startup the ring is rebuilt from the tail of today's and yesterday's archive")
    void rebuildFromArchive() {
        long old = NOW.minus(Duration.ofMinutes(90)).toEpochMilli();
        long beforeMidnight = Instant.parse("2026-10-12T23:59:30Z").toEpochMilli();
        long recent = NOW.minus(Duration.ofMinutes(5)).toEpochMilli();
        archive.append("G19", batch(new long[] {old, beforeMidnight, recent}, new float[] {30, 30, 30},
                new float[] {-97, -97, -97}, new float[] {1, 1, 1}));

        FlashStore store = new FlashStore(archive, null, FlashStore.Scope.AOI, clock);
        store.rebuild();

        assertThat(store.ringFlashes()).as("the 90-minute-old flash is outside the window").isEqualTo(2);
        assertThat(store.between(Instant.EPOCH, NOW).timesMs()).containsExactly(beforeMidnight, recent);
        assertThat(store.newestFlashFileTime()).as("not known until a file is processed").isEmpty();
    }

    @Test
    @DisplayName("only quality-0 flashes reach the store")
    void qualityFilter() {
        GlmParser.Result parsed = GlmParser.assemble(845_114_240.0, new float[] {30.3f, 30.4f, 30.5f},
                new float[] {-97.7f, -97.6f, -97.5f}, new float[] {1.0f, 2.0f, 3.0f}, new float[] {1e-14f, 2e-14f, 3e-14f},
                new float[] {0f, 3f, 0f});
        clock.setInstant(Instant.parse("2026-10-12T21:58:00Z"));
        FlashStore store = new FlashStore(archive, null, FlashStore.Scope.AOI, clock);
        store.append("G19", parsed.flashes(), Instant.parse("2026-10-12T21:57:40Z"));

        assertThat(parsed.dropped()).isEqualTo(1);
        assertThat(store.ringFlashes()).isEqualTo(2);
        assertThat(archive.read("G19", LocalDate.of(2026, 10, 12), 0, Long.MAX_VALUE).lat()).containsExactly(30.3f, 30.5f);
    }

    @Test
    @DisplayName("conus scope needs the large profile; anything else is refused")
    void scopes() {
        assertThat(FlashStore.resolveScope("aoi", RasterProfile.LARGE)).isEqualTo(FlashStore.Scope.AOI);
        assertThat(FlashStore.resolveScope(null, RasterProfile.SMALL)).isEqualTo(FlashStore.Scope.AOI);
        assertThat(FlashStore.resolveScope("CONUS", RasterProfile.LARGE)).isEqualTo(FlashStore.Scope.CONUS);
        assertThat(FlashStore.resolveScope("conus", RasterProfile.SMALL)).isEqualTo(FlashStore.Scope.AOI);
        assertThatThrownBy(() -> FlashStore.resolveScope("world", RasterProfile.LARGE))
                .isInstanceOf(IllegalArgumentException.class);

        FlashStore conus = new FlashStore(archive, null, FlashStore.Scope.CONUS, clock);
        long t = NOW.minusSeconds(30).toEpochMilli();
        conus.append("G19", batch(new long[] {t, t, t}, new float[] {45f, 30f, -10f}, new float[] {-80f, -97f, -60f},
                new float[] {1, 1, 1}), NOW);
        assertThat(conus.ringFlashes()).isEqualTo(2);
    }

    @Test
    @DisplayName("the ring window follows the newest flash and drops flashes older than 60 minutes")
    void ringWindow() {
        FlashRing ring = new FlashRing();
        long base = NOW.toEpochMilli();
        assertThat(ring.add(base, 1, 1, 1)).isTrue();
        assertThat(ring.add(base - 61 * 60_000L, 1, 1, 1)).isFalse();
        assertThat(ring.add(base - 59 * 60_000L, 1, 1, 1)).isTrue();
        assertThat(ring.size()).isEqualTo(2);
        assertThat(ring.add(base + 30 * 60_000L, 2, 2, 2)).isTrue();
        assertThat(ring.size()).as("the flash 89 minutes before the newest left the window").isEqualTo(2);
        ring.clear();
        assertThat(ring.size()).isZero();
    }

    @Test
    @DisplayName("a probe's view reads but never writes")
    void probingView() {
        FlashStore store = new FlashStore(archive, null, FlashStore.Scope.AOI, clock);
        FlashSink view = store.probingView();
        view.append("G19", batch(new long[] {NOW.toEpochMilli()}, new float[] {30}, new float[] {-97}, new float[] {1}),
                NOW);

        assertThat(view.probing()).isTrue();
        assertThat(store.ringFlashes()).isZero();
        assertThat(archive.totalBytes()).isZero();
        assertThat(view.newestFlashFileTime()).isEmpty();
    }

    private static MultiPolygon square(double minLon, double minLat, double maxLon, double maxLat) {
        Polygon p = Geo.factory().createPolygon(new Coordinate[] {new Coordinate(minLon, minLat),
                new Coordinate(maxLon, minLat), new Coordinate(maxLon, maxLat), new Coordinate(minLon, maxLat),
                new Coordinate(minLon, minLat)});
        return Geo.factory().createMultiPolygon(new Polygon[] {p});
    }
}
