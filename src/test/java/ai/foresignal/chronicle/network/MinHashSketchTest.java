package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** MinHash signatures (prompt 134): deterministic, accurate on planted overlaps, and incremental. */
class MinHashSketchTest {

    private static final KeyedHasher HASHER = new KeyedHasher("test-salt-0123456789".getBytes(StandardCharsets.UTF_8));

    /** Keyed hashes of {@code key0 … key(n-1)} from {@code from}. */
    private static List<Long> keys(int from, int n) {
        List<Long> out = new ArrayList<>();
        for (int i = from; i < from + n; i++) {
            out.add(HASHER.hash("ev:seismic:usgs:" + i));
        }
        return out;
    }

    @Test
    @DisplayName("signatures are deterministic and survive bytes and base64")
    void deterministic() {
        MinHashSketch a = MinHashSketch.of(keys(0, 500));
        MinHashSketch b = MinHashSketch.of(keys(0, 500));
        assertThat(a).isEqualTo(b);
        assertThat(a.slots()).hasSize(MinHashSketch.PERMUTATIONS);
        assertThat(a.toBytes()).hasSize(MinHashSketch.PERMUTATIONS * Long.BYTES);
        assertThat(MinHashSketch.fromBytes(a.toBytes())).isEqualTo(a);
        assertThat(MinHashSketch.fromBase64(a.toBase64())).isEqualTo(a);
        assertThat(a.bandKeys()).hasSize(MinHashSketch.BANDS).containsExactly(b.bandKeys());
        assertThat(MinHashSketch.BANDS * MinHashSketch.ROWS).isEqualTo(MinHashSketch.PERMUTATIONS);
        assertThatThrownBy(() -> MinHashSketch.fromBytes(new byte[10])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Jaccard estimates are within tolerance on planted overlaps")
    void plantedOverlaps() {
        MinHashSketch a = MinHashSketch.of(keys(0, 2000));
        MinHashSketch half = MinHashSketch.of(keys(1000, 2000));
        MinHashSketch quarter = MinHashSketch.of(keys(1500, 2000));
        MinHashSketch disjoint = MinHashSketch.of(keys(10_000, 2000));
        // J = 1000 / 3000 and 500 / 3500; 128 permutations give a standard error near 0.04.
        assertThat(a.jaccard(half)).isCloseTo(1.0 / 3, within(0.13));
        assertThat(a.jaccard(quarter)).isCloseTo(500.0 / 3500, within(0.11));
        assertThat(a.jaccard(disjoint)).isLessThan(0.08);
        assertThat(a.jaccard(a)).isEqualTo(1.0);
        assertThat(a.jaccard(MinHashSketch.empty())).isZero();
        LinkScores.Estimate e = LinkScores.estimate(a, 2000, half, 2000);
        assertThat(e.sharedEstimate()).isCloseTo(1000, within(400.0));
        assertThat(e.containment()).isCloseTo(0.5, within(0.2));
    }

    @Test
    @DisplayName("an incremental update equals a recompute, and union is slot-wise")
    void incremental() {
        List<Long> all = keys(0, 900);
        MinHashSketch recomputed = MinHashSketch.of(all);
        MinHashSketch stepwise = MinHashSketch.of(all.subList(0, 300)).with(all.subList(300, 600))
                .with(all.subList(600, 900));
        assertThat(stepwise).isEqualTo(recomputed);
        assertThat(MinHashSketch.of(all.subList(0, 450)).union(MinHashSketch.of(all.subList(450, 900))))
                .isEqualTo(recomputed);
        assertThat(recomputed.with(all.subList(0, 10))).as("re-adding values changes nothing").isEqualTo(recomputed);
        assertThat(recomputed.changedShare(recomputed)).isZero();
        assertThat(MinHashSketch.empty().isEmpty()).isTrue();
        assertThat(recomputed.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("size bands and shared estimates")
    void sizes() {
        assertThat(MinHashSketch.sizeBand(0)).isZero();
        assertThat(MinHashSketch.sizeBand(1)).isZero();
        assertThat(MinHashSketch.sizeBand(2)).isEqualTo(1);
        assertThat(MinHashSketch.sizeBand(1023)).isEqualTo(9);
        assertThat(MinHashSketch.sizeBand(1024)).isEqualTo(10);
        assertThat(MinHashSketch.sizeOfBand(10)).isBetween(1024.0, 2048.0);
        assertThat(MinHashSketch.sharedEstimate(1.0 / 3, 2000, 2000)).isCloseTo(1000, within(1e-9));
        assertThat(MinHashSketch.sharedEstimate(0, 10, 10)).isZero();
        assertThat(MinHashSketch.containment(50, 100, 1000)).isEqualTo(0.5);
        assertThat(MinHashSketch.containment(50, 0, 1000)).isZero();
    }
}
