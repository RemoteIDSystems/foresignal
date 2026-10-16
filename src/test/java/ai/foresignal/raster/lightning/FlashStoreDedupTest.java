package ai.foresignal.raster.lightning;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.test.MutableClock;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Cross-origin flash de-duplication in the shared append path (prompt 107): the internet {@code goes-glm}
 * copy and the local GRB copy of one flash are counted once, whichever arrives first; flashes of the same
 * origin never de-duplicate each other; a rebuilt ring keeps each origin.
 */
class FlashStoreDedupTest {

    static final Instant NOW = Instant.parse("2026-10-13T00:20:00Z");

    @TempDir Path root;

    FlashArchive archive;
    FlashStore store;
    long t;

    @BeforeEach
    void setUp() {
        archive = new FlashArchive(root.resolve("glm"));
        store = new FlashStore(archive, null, FlashStore.Scope.AOI, new MutableClock(NOW));
        t = NOW.minus(Duration.ofMinutes(2)).toEpochMilli();
    }

    private static FlashBatch one(long time, float lat, float lon) {
        return new FlashBatch(new long[] {time}, new float[] {lat}, new float[] {lon}, new float[] {10});
    }

    @Test
    @DisplayName("internet first: the local copy within 1 s and 0.05 degrees is dropped from the ring and the archive")
    void internetFirst() {
        store.append("G19", one(t, 30.30f, -97.70f), NOW);
        store.append(FlashStore.LOCAL_ORIGIN, FlashStore.LOCAL_ORIGIN, one(t + 400, 30.32f, -97.68f), NOW);
        assertThat(store.ringFlashes()).isEqualTo(1);
        assertThat(archive.read(FlashStore.LOCAL_ORIGIN, LocalDate.of(2026, 10, 13), 0, Long.MAX_VALUE).size()).isZero();
    }

    @Test
    @DisplayName("local first: the internet copy is dropped, so the internet feed does not double the count")
    void localFirst() {
        store.append(FlashStore.LOCAL_ORIGIN, FlashStore.LOCAL_ORIGIN, one(t, 30.30f, -97.70f), NOW);
        store.append("G19", one(t - 800, 30.28f, -97.72f), NOW);
        assertThat(store.ringFlashes()).isEqualTo(1);
        assertThat(archive.read("G19", LocalDate.of(2026, 10, 13), 0, Long.MAX_VALUE).size()).isZero();
    }

    @Test
    @DisplayName("outside the tolerances, or of the same origin, both flashes are kept")
    void kept() {
        store.append("G19", one(t, 30.30f, -97.70f), NOW);
        store.append(FlashStore.LOCAL_ORIGIN, FlashStore.LOCAL_ORIGIN, one(t + 1500, 30.30f, -97.70f), NOW);
        store.append(FlashStore.LOCAL_ORIGIN, FlashStore.LOCAL_ORIGIN, one(t, 30.40f, -97.70f), NOW);
        store.append("G19", one(t + 100, 30.30f, -97.71f), NOW);
        assertThat(store.ringFlashes()).isEqualTo(4);
    }

    @Test
    @DisplayName("after a rebuild, flashes from the local archive keep their origin and still de-duplicate the internet copy")
    void rebuildKeepsOrigin() {
        store.append(FlashStore.LOCAL_ORIGIN, FlashStore.LOCAL_ORIGIN, one(t, 30.30f, -97.70f), NOW);
        store.rebuild();
        assertThat(store.ringFlashes()).isEqualTo(1);
        store.append("G19", one(t + 200, 30.30f, -97.70f), NOW);
        assertThat(store.ringFlashes()).isEqualTo(1);
        assertThat(FlashStore.originOf("G19", null)).isEqualTo(FlashRing.INTERNET);
        assertThat(FlashStore.originOf("local", null)).isEqualTo(FlashRing.LOCAL);
        assertThat(FlashStore.originOf("G19", "local")).isEqualTo(FlashRing.LOCAL);
    }
}
