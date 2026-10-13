package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@code lightning-cluster} records (prompt 048, Weather-Imagery-Design §7.2): DBSCAN's eps and minPts,
 * identity carried within 15 km, a quantized payload that stays put under small changes, the tombstone
 * after 10 minutes of processed data without members, and the freeze while no file is processed.
 */
class LightningClusterTest {

    static final long T0 = Instant.parse("2026-10-12T21:00:00Z").toEpochMilli();
    static final long FILE_MS = 20_000;
    /** About 1 km of latitude. */
    static final double KM = 1 / 111.195;

    AtomicInteger counter;
    LightningClusterer clusterer;

    @BeforeEach
    void setUp() {
        counter = new AtomicInteger();
        clusterer = new LightningClusterer(at -> "lc-" + counter.incrementAndGet());
    }

    /** {@code n} flashes around ({@code lat}, {@code lon}), spaced {@code spacingKm} north-south. */
    static Flashes around(double lat, double lon, int n, double spacingKm, long t, float energy) {
        Flashes f = new Flashes();
        for (int i = 0; i < n; i++) {
            f.add(t + i * 100L, lat + (i - n / 2.0) * spacingKm * KM, lon, energy);
        }
        return f;
    }

    /** A growable flash list. */
    static final class Flashes {
        final List<long[]> t = new ArrayList<>();
        final List<double[]> pos = new ArrayList<>();

        Flashes add(long time, double lat, double lon, float energy) {
            t.add(new long[] {time});
            pos.add(new double[] {lat, lon, energy});
            return this;
        }

        Flashes plus(Flashes other) {
            t.addAll(other.t);
            pos.addAll(other.pos);
            return this;
        }

        FlashBatch batch() {
            int n = t.size();
            long[] times = new long[n];
            float[] lat = new float[n];
            float[] lon = new float[n];
            float[] e = new float[n];
            for (int i = 0; i < n; i++) {
                times[i] = t.get(i)[0];
                lat[i] = (float) pos.get(i)[0];
                lon[i] = (float) pos.get(i)[1];
                e[i] = (float) pos.get(i)[2];
            }
            return new FlashBatch(times, lat, lon, e);
        }
    }

    private static List<NormalizedRecord> records(List<LightningClusterer.Change> changes) {
        return changes.stream().map(LightningClusterer.Change::record).toList();
    }

    @Test
    @DisplayName("DBSCAN: three flashes within 8 km form a cluster, two do not, and an isolated flash is noise")
    void epsAndMinPts() {
        FlashBatch three = around(30.0, -97.0, 3, 3.0, T0, 10).batch();
        assertThat(LightningClusterer.dbscan(three)).hasSize(1);

        FlashBatch two = around(30.0, -97.0, 2, 3.0, T0, 10).batch();
        assertThat(LightningClusterer.dbscan(two)).isEmpty();

        // 9 km apart: no flash has two neighbors within 8 km.
        FlashBatch sparse = around(30.0, -97.0, 3, 9.0, T0, 10).batch();
        assertThat(LightningClusterer.dbscan(sparse)).isEmpty();

        // A chain of flashes 6 km apart is one cluster (density-reachable), and a far flash is noise.
        Flashes chain = around(30.0, -97.0, 6, 6.0, T0, 10).add(T0, 33.0, -97.0, 10);
        List<int[]> clusters = LightningClusterer.dbscan(chain.batch());
        assertThat(clusters).hasSize(1);
        assertThat(clusters.get(0)).hasSize(6).doesNotContain(6);

        // Two separate storms 100 km apart.
        FlashBatch twoStorms = around(30.0, -97.0, 5, 1.0, T0, 10).plus(around(30.9, -97.0, 4, 1.0, T0, 10)).batch();
        assertThat(LightningClusterer.dbscan(twoStorms)).hasSize(2);
        assertThat(LightningClusterer.dbscan(FlashBatch.EMPTY)).isEmpty();
    }

    @Test
    @DisplayName("a new cluster gets an id, its first flash as eventTime and its newest as observedAt")
    void newCluster() {
        FlashBatch flashes = around(30.0, -97.0, 11, 1.0, T0, 37f).batch();
        List<NormalizedRecord> out = records(clusterer.step(flashes, T0 + FILE_MS, FILE_MS));

        assertThat(out).hasSize(1);
        NormalizedRecord r = out.get(0);
        assertThat(r.externalId()).isEqualTo("lc-1");
        assertThat(r.kind()).isEqualTo("lightning-cluster");
        assertThat(r.eventTime()).isEqualTo(Instant.ofEpochMilli(T0));
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochMilli(T0 + 1000));
        assertThat(r.payload()).containsEntry("flashes5m", 10).containsEntry("maxEnergyFj", 10L)
                .containsEntry("label", "Lightning cluster, 10+ flashes/5 min").containsEntry("automated", true)
                .containsEntry("notAWarning", true).containsEntry("radiusKm", 5L);
        Point p = (Point) r.geometry();
        assertThat(p.getX()).isEqualTo(-97.0);
        assertThat(p.getY()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("identity carries over to the nearest cluster within 15 km, closest first; farther is new")
    void identityCarryOver() {
        clusterer.step(around(30.0, -97.0, 5, 1.0, T0, 10).batch(), T0 + FILE_MS, FILE_MS);
        assertThat(clusterer.tracked()).extracting(t -> t.id).containsExactly("lc-1");

        // The storm moved 10 km north: same id.
        List<NormalizedRecord> moved = records(clusterer.step(around(30.0 + 10 * KM, -97.0, 5, 1.0, T0 + 60_000, 10).batch(),
                T0 + 80_000, 60_000));
        assertThat(moved).extracting(NormalizedRecord::externalId).containsExactly("lc-1");

        // Two clusters 9 km apart now, 6 km and 7 km from lc-1 (at +9.5 km): the closer one keeps lc-1.
        Flashes split = around(30.0 + 4 * KM, -97.0, 5, 1.0, T0 + 120_000, 10)
                .plus(around(30.0 + 17 * KM, -97.0, 5, 1.0, T0 + 120_000, 10));
        List<NormalizedRecord> after = records(clusterer.step(split.batch(), T0 + 140_000, 60_000));
        assertThat(after).extracting(NormalizedRecord::externalId).containsExactlyInAnyOrder("lc-1", "lc-2");
        NormalizedRecord kept = after.stream().filter(r -> r.externalId().equals("lc-1")).findFirst().orElseThrow();
        assertThat(((Point) kept.geometry()).getY()).isEqualTo(LightningClusterer.round2(30.0 + 3.5 * KM));

        // A storm 40 km away is never the same cluster.
        LightningClusterer other = new LightningClusterer(at -> "x-" + counter.incrementAndGet());
        other.step(around(30.0, -97.0, 5, 1.0, T0, 10).batch(), T0 + FILE_MS, FILE_MS);
        String first = other.tracked().get(0).id;
        List<NormalizedRecord> far = records(other.step(around(30.0 + 40 * KM, -97.0, 5, 1.0, T0 + 60_000, 10).batch(),
                T0 + 80_000, 60_000));
        assertThat(far).hasSize(1);
        assertThat(far.get(0).externalId()).isNotEqualTo(first);
        assertThat(other.tracked()).hasSize(2);
    }

    @Test
    @DisplayName("the payload is quantized: small changes in count, radius, energy or position change nothing")
    void quantizedPayload() {
        NormalizedRecord a = records(clusterer.step(around(30.0, -97.0, 21, 0.4, T0, 120f).batch(), T0 + FILE_MS,
                FILE_MS)).get(0);
        NormalizedRecord b = records(clusterer.step(around(30.001, -97.001, 22, 0.4, T0 + 1000, 180f).batch(),
                T0 + 2 * FILE_MS, FILE_MS)).get(0);

        assertThat(b.externalId()).isEqualTo(a.externalId());
        assertThat(b.payload()).isEqualTo(a.payload());
        assertThat(b.geometry().equalsExact(a.geometry())).isTrue();
        assertThat(a.payload()).containsEntry("flashes5m", 20).containsEntry("maxEnergyFj", 100L);

        NormalizedRecord c = records(clusterer.step(around(30.0, -97.0, 55, 0.2, T0 + 2000, 1500f).batch(),
                T0 + 3 * FILE_MS, FILE_MS)).get(0);
        assertThat(c.payload()).containsEntry("flashes5m", 50).containsEntry("maxEnergyFj", 1000L)
                .containsEntry("label", "Lightning cluster, 50+ flashes/5 min");
    }

    @Test
    @DisplayName("buckets: flash counts and energy decades")
    void buckets() {
        assertThat(LightningClusterer.flashBucket(3)).isEqualTo(3);
        assertThat(LightningClusterer.flashBucket(4)).isEqualTo(3);
        assertThat(LightningClusterer.flashBucket(5)).isEqualTo(5);
        assertThat(LightningClusterer.flashBucket(19)).isEqualTo(10);
        assertThat(LightningClusterer.flashBucket(199)).isEqualTo(100);
        assertThat(LightningClusterer.flashBucket(5000)).isEqualTo(200);
        assertThat(LightningClusterer.energyBucket(1.3)).isEqualTo(1L);
        assertThat(LightningClusterer.energyBucket(5249)).isEqualTo(1000L);
        assertThat(LightningClusterer.energyBucket(0.5)).isEqualTo(0.1);
        assertThat(LightningClusterer.energyBucket(0)).isEqualTo(0L);
    }

    @Test
    @DisplayName("a cluster without members is tombstoned after 10 minutes of processed data, not before")
    void tombstoneAfterTenMinutes() {
        clusterer.step(around(30.0, -97.0, 5, 1.0, T0, 10).batch(), T0 + FILE_MS, FILE_MS);
        clusterer.markEmitted(List.of("lc-1"));
        long end = T0 + FILE_MS;
        List<LightningClusterer.Change> changes = List.of();
        // 29 more files of 20 s: 9 min 40 s without members.
        for (int i = 0; i < 29; i++) {
            end += FILE_MS;
            changes = clusterer.step(FlashBatch.EMPTY, end, FILE_MS);
            assertThat(changes).as("file %d", i).isEmpty();
        }
        end += FILE_MS;
        changes = clusterer.step(FlashBatch.EMPTY, end, FILE_MS);

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).record().tombstone()).isTrue();
        assertThat(changes.get(0).record().externalId()).isEqualTo("lc-1");
        assertThat(changes.get(0).record().observedAt()).isEqualTo(Instant.ofEpochMilli(end));
        assertThat(changes.get(0).wasEmitted()).isTrue();
        assertThat(clusterer.tracked()).isEmpty();
    }

    @Test
    @DisplayName("members reset the quiet time")
    void membersResetQuiet() {
        clusterer.step(around(30.0, -97.0, 5, 1.0, T0, 10).batch(), T0 + FILE_MS, FILE_MS);
        clusterer.step(FlashBatch.EMPTY, T0 + 9 * 60_000, 9 * 60_000);
        clusterer.step(around(30.0, -97.0, 5, 1.0, T0 + 9 * 60_000, 10).batch(), T0 + 9 * 60_000 + FILE_MS, FILE_MS);
        List<LightningClusterer.Change> later = clusterer.step(FlashBatch.EMPTY, T0 + 18 * 60_000, 9 * 60_000);
        assertThat(later).isEmpty();
        assertThat(clusterer.tracked()).hasSize(1);
    }

    @Test
    @DisplayName("freeze: when no file is processed for more than 2 minutes the gap adds no quiet time")
    void freezeWhenNoFile() {
        clusterer.step(around(30.0, -97.0, 5, 1.0, T0, 10).batch(), T0 + FILE_MS, FILE_MS);
        String frozen = clusterer.serialize();
        Instant lastEnd = Instant.ofEpochMilli(T0 + FILE_MS);

        // The feed is down for 30 minutes: no step runs, so nothing changes.
        assertThat(clusterer.serialize()).isEqualTo(frozen);

        // Data resumes: the first file after the gap adds only its own 20 s.
        GoesGlmAdapter.Granule resumed = new GoesGlmAdapter.Granule("k", lastEnd.plusSeconds(1800),
                lastEnd.plusSeconds(1820), 1);
        long delta = GoesGlmAdapter.deltaMs(lastEnd, resumed);
        assertThat(delta).isEqualTo(FILE_MS);
        assertThat(clusterer.step(FlashBatch.EMPTY, resumed.end().toEpochMilli(), delta)).isEmpty();
        assertThat(clusterer.tracked().get(0).quietMs).isEqualTo(FILE_MS);

        // A gap within 2 minutes counts in full.
        GoesGlmAdapter.Granule late = new GoesGlmAdapter.Granule("k2", lastEnd.plusSeconds(60), lastEnd.plusSeconds(80), 1);
        assertThat(GoesGlmAdapter.deltaMs(lastEnd, late)).isEqualTo(80_000);
        assertThat(GoesGlmAdapter.deltaMs(null, late)).isEqualTo(FILE_MS);
    }

    @Test
    @DisplayName("a cluster born and gone within one fetch is reported as not emitted, so no tombstone is sent")
    void bornAndGone() {
        clusterer.step(around(30.0, -97.0, 5, 1.0, T0, 10).batch(), T0 + FILE_MS, FILE_MS);
        List<LightningClusterer.Change> changes = clusterer.step(FlashBatch.EMPTY, T0 + 11 * 60_000, 11 * 60_000);
        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).wasEmitted()).isFalse();
    }

    @Test
    @DisplayName("tracked clusters survive a restart through the adapter state")
    void serializeRoundTrip() {
        clusterer.step(around(30.0, -97.0, 5, 1.0, T0, 10).batch(), T0 + FILE_MS, FILE_MS);
        clusterer.step(FlashBatch.EMPTY, T0 + 2 * FILE_MS, FILE_MS);
        clusterer.markEmitted(List.of("lc-1"));
        String text = clusterer.serialize();

        LightningClusterer restored = new LightningClusterer(at -> "never");
        restored.restore(text + "\nmalformed|line");
        assertThat(restored.tracked()).hasSize(1);
        LightningClusterer.Tracked t = restored.tracked().get(0);
        assertThat(t.id).isEqualTo("lc-1");
        assertThat(t.firstMs).isEqualTo(T0);
        assertThat(t.quietMs).isEqualTo(FILE_MS);
        assertThat(t.emitted).isTrue();
        assertThat(restored.serialize()).isEqualTo(text);

        // Re-matched within 15 km after the restart: the same id comes back.
        List<NormalizedRecord> again = records(restored.step(around(30.0 + 2 * KM, -97.0, 5, 1.0, T0 + 600_000, 10).batch(),
                T0 + 620_000, FILE_MS));
        assertThat(again).extracting(NormalizedRecord::externalId).containsExactly("lc-1");
    }

    @Test
    @DisplayName("ULIDs are 26 Crockford characters and sort by time")
    void ulids() {
        String a = ClusterIds.ulid(Instant.parse("2026-10-12T21:00:00Z"));
        String b = ClusterIds.ulid(Instant.parse("2026-10-12T21:00:01Z"));
        assertThat(a).hasSize(26).matches("[0-9A-HJKMNP-TV-Z]{26}");
        assertThat(a.substring(0, 10).compareTo(b.substring(0, 10))).isNegative();
        assertThat(new LightningClusterer().step(around(30, -97, 3, 1, T0, 1).batch(), T0 + FILE_MS, FILE_MS).get(0)
                .record().externalId()).startsWith("lc-").hasSize(29);
    }
}
