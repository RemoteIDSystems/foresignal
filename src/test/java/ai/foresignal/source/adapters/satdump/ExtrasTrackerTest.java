package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.InboxAccess;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ExtrasTracker} (prompt 107): per-file settle, processed-once, the watermark and newest-wins ordering. */
class ExtrasTrackerTest {

    static final Instant T = Instant.parse("2026-10-16T12:00:00Z");

    private static ExtrasTracker.Candidate file(String key, long size, Instant modified) {
        return new ExtrasTracker.Candidate(key, new InboxAccess.FileStat(Path.of("/inbox", key), size, modified));
    }

    @Test
    @DisplayName("a file is ready once unchanged since the previous scan, and processed once")
    void settle() {
        ExtrasTracker tracker = new ExtrasTracker();
        ExtrasTracker.Candidate a = file("EMWIN/a.txt", 100, T);
        assertThat(tracker.ready(List.of(a))).isEmpty();
        List<ExtrasTracker.Candidate> ready = tracker.ready(List.of(a));
        assertThat(ready).containsExactly(a);
        tracker.markDone(a);
        assertThat(tracker.ready(List.of(a))).isEmpty();
    }

    @Test
    @DisplayName("a growing file is not ready until it stops changing")
    void growing() {
        ExtrasTracker tracker = new ExtrasTracker();
        tracker.ready(List.of(file("EMWIN/a.txt", 10, T)));
        assertThat(tracker.ready(List.of(file("EMWIN/a.txt", 20, T.plusSeconds(30))))).isEmpty();
        assertThat(tracker.ready(List.of(file("EMWIN/a.txt", 20, T.plusSeconds(30))))).hasSize(1);
    }

    @Test
    @DisplayName("a rewritten file is processed again")
    void rewritten() {
        ExtrasTracker tracker = new ExtrasTracker();
        ExtrasTracker.Candidate a = file("EMWIN/a.txt", 10, T);
        tracker.ready(List.of(a));
        tracker.markDone(tracker.ready(List.of(a)).get(0));
        ExtrasTracker.Candidate b = file("EMWIN/a.txt", 12, T.plusSeconds(60));
        tracker.ready(List.of(b));
        assertThat(tracker.ready(List.of(b))).containsExactly(b);
    }

    @Test
    @DisplayName("the watermark advances behind the scan but never past a waiting file, and survives a restore")
    void watermark() {
        ExtrasTracker tracker = new ExtrasTracker();
        ExtrasTracker.Candidate old = file("EMWIN/old.txt", 10, T.minus(Duration.ofHours(1)));
        ExtrasTracker.Candidate waiting = file("EMWIN/w.txt", 10, T.minus(Duration.ofMinutes(30)));
        tracker.ready(List.of(old, waiting));
        List<ExtrasTracker.Candidate> ready = tracker.ready(List.of(old, waiting));
        assertThat(ready).hasSize(2);
        tracker.markDone(old);
        // waiting was held back (for example by a per-scan cap).
        tracker.advance(T);
        assertThat(tracker.watermark()).isEqualTo(waiting.file().modified().toEpochMilli() - 1);
        tracker.markDone(waiting);
        tracker.advance(T);
        assertThat(tracker.watermark()).isEqualTo(T.minus(Duration.ofMinutes(ExtrasTracker.TRACK_MINUTES)).toEpochMilli());

        ExtrasTracker restored = new ExtrasTracker();
        restored.restore(tracker.state());
        assertThat(restored.ready(List.of(old, waiting))).isEmpty();
        assertThat(restored.state().done()).isEmpty();
    }

    @Test
    @DisplayName("an older product never overrides a newer one of the same warning")
    void newestWarningWins() {
        ExtrasTracker tracker = new ExtrasTracker();
        assertThat(tracker.acceptWarning("KFWD.TO.W.0042", T.toEpochMilli())).isTrue();
        assertThat(tracker.acceptWarning("KFWD.TO.W.0042", T.plusSeconds(600).toEpochMilli())).isTrue();
        assertThat(tracker.acceptWarning("KFWD.TO.W.0042", T.toEpochMilli())).isFalse();
        assertThat(tracker.acceptWarning("KFWD.TO.W.0042", T.plusSeconds(600).toEpochMilli())).isTrue();
        assertThat(tracker.acceptWarning("KFWD.TO.W.0043", T.toEpochMilli())).isTrue();
    }

    @Test
    @DisplayName("a DCS message not newer than the last one emitted is refused; memories are pruned by age")
    void dcsAndPrune() {
        ExtrasTracker tracker = new ExtrasTracker();
        assertThat(tracker.acceptDcs("CE1234", T.toEpochMilli())).isTrue();
        assertThat(tracker.acceptDcs("CE1234", T.toEpochMilli())).isFalse();
        assertThat(tracker.acceptDcs("CE1234", T.minusSeconds(60).toEpochMilli())).isFalse();
        assertThat(tracker.acceptDcs("CE1234", T.plusSeconds(60).toEpochMilli())).isTrue();
        tracker.acceptWarning("KFWD.TO.W.0042", T.toEpochMilli());
        tracker.prune(T.plus(Duration.ofDays(8)));
        assertThat(tracker.state().warnings()).isEmpty();
        assertThat(tracker.state().dcs()).isEmpty();
    }
}
