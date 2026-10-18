package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.media.GuardedImages;
import ai.foresignal.media.ImageGuard;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The PDQ port (prompt 136) against Meta ThreatExchange's reference test vectors: the photo-hasher regression output
 * of {@code pdq/java/reg_test/expected/out} for {@code pdq/data/reg-test-input/dih/bridge-1-original.jpg} and
 * {@code bridge-5-flipx.jpg} (both copied to {@code src/test/resources/pdq/}), the quality metric, hex round trips, and
 * exact multi-index Hamming search over 100,000 hashes.
 */
class PdqTest {

    /** Reference hash of bridge-1-original.jpg (quality 100). */
    static final String ORIGINAL = "f8f8f0cee0f4a84f06370a22038f63f0b36e2ed596621e1d33e6b39c4e9c9b22";

    /** Reference hash of bridge-5-flipx.jpg (quality 100). */
    static final String FLIPX = "f8f80f31e0f417b20e37f5cd028f980fb36ed02a9662c1e233e64c634e9c64dd";

    static final GuardedImages IMAGES = new GuardedImages(new ImageGuard(25L * 1024 * 1024, null));

    private static Pdq.Hash hashOf(String resource) throws IOException {
        try (InputStream in = PdqTest.class.getResourceAsStream("/pdq/" + resource)) {
            assertThat(in).as(resource).isNotNull();
            return Pdq.hash(IMAGES.decode(in.readAllBytes(), "image/jpeg"));
        }
    }

    @Test
    @DisplayName("the Java port reproduces the reference hashes and quality of the regression vectors")
    void referenceVectors() throws IOException {
        Pdq.Hash original = hashOf("bridge-1-original.jpg");
        assertThat(original.hex()).isEqualTo(ORIGINAL);
        assertThat(original.quality()).isEqualTo(100);
        Pdq.Hash flipped = hashOf("bridge-5-flipx.jpg");
        assertThat(flipped.hex()).isEqualTo(FLIPX);
        assertThat(flipped.quality()).isEqualTo(100);
        assertThat(original.distance(flipped)).as("a flip is a different picture to PDQ").isGreaterThan(31);
    }

    @Test
    @DisplayName("a flat image has quality 0; a re-encoded copy stays within the match threshold")
    void quality() {
        BufferedImage flat = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                flat.setRGB(x, y, 0x808080);
            }
        }
        assertThat(Pdq.hash(flat).quality()).isZero();
        BufferedImage gradient = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        BufferedImage brighter = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                int v = ((x / 32 + y / 32) % 2 == 0) ? 40 : 210;
                gradient.setRGB(x, y, v << 16 | v << 8 | v);
                int w = Math.min(255, v + 10);
                brighter.setRGB(x, y, w << 16 | w << 8 | w);
            }
        }
        Pdq.Hash a = Pdq.hash(gradient);
        assertThat(a.quality()).isGreaterThanOrEqualTo(NarrativeVocabulary.PDQ_MIN_QUALITY);
        assertThat(a.distance(Pdq.hash(brighter))).isLessThanOrEqualTo(NarrativeVocabulary.PDQ_MATCH_DISTANCE);
    }

    @Test
    @DisplayName("hex round trip, distance, and malformed hashes")
    void hex() {
        Pdq.Hash h = Pdq.parse(ORIGINAL);
        assertThat(h.hex()).isEqualTo(ORIGINAL);
        assertThat(Pdq.distance(ORIGINAL, ORIGINAL)).isZero();
        assertThat(Pdq.distance(ORIGINAL, FLIPX)).isEqualTo(Pdq.parse(ORIGINAL).distance(Pdq.parse(FLIPX)));
        assertThatThrownBy(() -> Pdq.parse("abc")).isInstanceOf(IllegalArgumentException.class);
    }

    private static Pdq.Hash random(Random r) {
        short[] w = new short[16];
        for (int i = 0; i < 16; i++) {
            w[i] = (short) r.nextInt(1 << 16);
        }
        return new Pdq.Hash(w, 100);
    }

    private static Pdq.Hash flip(Pdq.Hash h, int bits, Random r) {
        short[] w = new short[16];
        for (int i = 0; i < 16; i++) {
            w[i] = (short) h.word(i);
        }
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            positions.add(i);
        }
        java.util.Collections.shuffle(positions, r);
        for (int k = 0; k < bits; k++) {
            int p = positions.get(k);
            w[p >> 4] ^= (short) (1 << (p & 15));
        }
        return new Pdq.Hash(w, 100);
    }

    @Test
    @DisplayName("multi-index search over 100,000 hashes finds every planted neighbor within 31 bits, exactly")
    void search() {
        Random r = new Random(136);
        PdqIndex index = new PdqIndex();
        List<Pdq.Hash> all = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            Pdq.Hash h = random(r);
            all.add(h);
            index.add("h" + i, h);
        }
        assertThat(index.size()).isEqualTo(100_000);
        for (int q = 0; q < 50; q++) {
            int target = r.nextInt(all.size());
            int bits = r.nextInt(32);
            Pdq.Hash query = flip(all.get(target), bits, r);
            List<PdqIndex.Match> matches = index.search(query, 31, 10);
            assertThat(matches).extracting(PdqIndex.Match::id).contains("h" + target);
            int brute = 0;
            for (Pdq.Hash h : all) {
                if (h.distance(query) <= 31) {
                    brute++;
                }
            }
            assertThat(index.search(query, 31, 1_000)).hasSize(brute);
        }
        Pdq.Hash far = flip(all.get(0), 40, r);
        assertThat(index.search(far, 31, 10)).extracting(PdqIndex.Match::id).doesNotContain("h0");
        assertThat(index.search(far, 63, 10)).extracting(PdqIndex.Match::id).contains("h0");
    }
}
