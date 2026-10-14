package ai.foresignal.source.adapters.wikistream;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link SurgeDetector} thresholds, banding, new-page rule, tombstone and eviction (prompt 095). */
class SurgeDetectorTest {

    private static final Instant T0 = Instant.parse("2026-09-01T12:00:00Z");

    private static String editor(int i) {
        return "editor" + i;
    }

    @Test
    @DisplayName("fourteen edits by fourteen editors is not a surge; the fifteenth is, as rising")
    void thresholdBoundary() {
        SurgeDetector detector = new SurgeDetector();
        for (int i = 0; i < 14; i++) {
            assertThat(detector.onEdit("enwiki", "P", "url", false, editor(i), T0.plusSeconds(i * 30L))).isEmpty();
        }
        Optional<SurgeDetector.Surge> surge = detector.onEdit("enwiki", "P", "url", false, editor(14),
                T0.plusSeconds(14 * 30L));
        assertThat(surge).isPresent();
        assertThat(surge.get().phase()).isEqualTo(SurgeDetector.RISING);
        assertThat(surge.get().edits()).isEqualTo(15);
        assertThat(surge.get().editors()).isEqualTo(15);
    }

    @Test
    @DisplayName("edits in bands of 5 emit nothing until the band changes")
    void banding() {
        SurgeDetector detector = new SurgeDetector();
        for (int i = 0; i < 15; i++) {
            detector.onEdit("enwiki", "P", "url", false, editor(i), T0.plusSeconds(i * 30L));
        }
        assertThat(detector.onEdit("enwiki", "P", "url", false, editor(15), T0.plusSeconds(15 * 30L))).isEmpty();
        assertThat(detector.onEdit("enwiki", "P", "url", false, editor(16), T0.plusSeconds(16 * 30L))).isEmpty();
        assertThat(detector.onEdit("enwiki", "P", "url", false, editor(17), T0.plusSeconds(17 * 30L))).isEmpty();
        assertThat(detector.onEdit("enwiki", "P", "url", false, editor(18), T0.plusSeconds(18 * 30L))).isEmpty();
        Optional<SurgeDetector.Surge> atTwenty =
                detector.onEdit("enwiki", "P", "url", false, editor(19), T0.plusSeconds(19 * 30L));
        assertThat(atTwenty).isPresent();
        assertThat(atTwenty.get().edits()).isEqualTo(20);
    }

    @Test
    @DisplayName("a new page surges at 10 edits by 5 editors within 30 minutes")
    void newPageRule() {
        SurgeDetector detector = new SurgeDetector();
        Optional<SurgeDetector.Surge> last = Optional.empty();
        for (int i = 0; i < 10; i++) {
            last = detector.onEdit("enwiki", "New", "url", true, editor(i % 5), T0.plusSeconds(i * 120L));
        }
        assertThat(last).isPresent();
        assertThat(last.get().isNewPage()).isTrue();
        assertThat(last.get().phase()).isEqualTo(SurgeDetector.RISING);
    }

    @Test
    @DisplayName("a surge is tombstoned 60 minutes after it stopped meeting the threshold")
    void tombstoneAfterLinger() {
        SurgeDetector detector = new SurgeDetector();
        Instant lastEdit = T0.plusSeconds(14 * 30L);
        for (int i = 0; i < 15; i++) {
            detector.onEdit("enwiki", "P", "url", false, editor(i), T0.plusSeconds(i * 30L));
        }
        assertThat(detector.sweep(lastEdit.plus(Duration.ofMinutes(30)))).isEmpty();
        var tombstones = detector.sweep(lastEdit.plus(Duration.ofMinutes(61)));
        assertThat(tombstones).hasSize(1);
        assertThat(tombstones.get(0).tombstone()).isTrue();
        assertThat(detector.size()).isZero();
    }

    @Test
    @DisplayName("the least recently updated page is evicted at the cap and the eviction counted")
    void eviction() {
        SurgeDetector detector = new SurgeDetector(new SurgeDetector.Config(15, 5, 10, 5, Duration.ofMinutes(15),
                Duration.ofMinutes(30), Duration.ofMinutes(60), 2));
        detector.onEdit("enwiki", "A", "url", false, editor(1), T0);
        detector.onEdit("enwiki", "B", "url", false, editor(2), T0.plusSeconds(1));
        detector.onEdit("enwiki", "C", "url", false, editor(3), T0.plusSeconds(2));
        assertThat(detector.size()).isEqualTo(2);
        assertThat(detector.evictions()).isEqualTo(1);
    }

    @Test
    @DisplayName("editor names are one-way hashed and deterministic")
    void editorHashing() {
        assertThat(SurgeDetector.hashEditor("alice")).isEqualTo(SurgeDetector.hashEditor("alice"));
        assertThat(SurgeDetector.hashEditor("alice")).isNotEqualTo(SurgeDetector.hashEditor("bob"));
        assertThat(SurgeDetector.hashEditor("alice")).hasSize(16).matches("[0-9a-f]{16}");
        assertThat(SurgeDetector.hashEditor(null)).isEqualTo(SurgeDetector.hashEditor(""));
    }
}
