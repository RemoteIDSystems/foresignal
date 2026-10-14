package ai.foresignal.source.series;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link FrequencyExcursionDetector} (prompt 093): in-band samples, a short excursion, statutory and severe
 * excursions, an excursion spanning two polls, a restart and stale data.
 */
class FrequencyExcursionDetectorTest {

    private static final Instant T0 = Instant.parse("2026-10-13T00:00:00Z");
    private static final FrequencyExcursionDetector.Config GB =
            new FrequencyExcursionDetector.Config("gb", 0.2, 0.5);

    private static FrequencyExcursionDetector.Sample sample(int seconds, double hz) {
        return new FrequencyExcursionDetector.Sample(T0.plusSeconds(seconds), hz);
    }

    private static List<NormalizedRecord> excursions(FrequencyExcursionDetector.Result result) {
        return result.records().stream().filter(r -> r.kind().equals("frequency-excursion")).toList();
    }

    private static NormalizedRecord state(FrequencyExcursionDetector.Result result) {
        return result.records().stream().filter(r -> r.kind().equals("grid-frequency")).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("in-band samples emit only the state record, which reads normal")
    void inBand() {
        FrequencyExcursionDetector.Result result = FrequencyExcursionDetector.process(GB,
                FrequencyExcursionDetector.State.empty(), List.of(sample(0, 50.05), sample(15, 49.9)), T0.plusSeconds(15));
        assertThat(excursions(result)).isEmpty();
        assertThat(state(result).externalId()).isEqualTo("gb:state");
        assertThat(state(result).payload()).containsEntry("state", "normal");
    }

    @Test
    @DisplayName("a short excursion opens and closes with a duration and an extreme")
    void shortExcursion() {
        FrequencyExcursionDetector.Result result = FrequencyExcursionDetector.process(GB,
                FrequencyExcursionDetector.State.empty(),
                List.of(sample(0, 50.0), sample(15, 49.7), sample(30, 50.0)), T0.plusSeconds(30));
        List<NormalizedRecord> excursions = excursions(result);
        assertThat(excursions).hasSize(2);
        NormalizedRecord open = excursions.get(0);
        NormalizedRecord closed = excursions.get(1);
        assertThat(open.externalId()).isEqualTo("gb:" + T0.plusSeconds(15).getEpochSecond());
        assertThat(open.payload()).containsEntry("open", true).doesNotContainKey("end");
        assertThat(closed.externalId()).isEqualTo(open.externalId());
        assertThat(closed.payload()).containsEntry("open", false).containsEntry("extremeHz", 49.7)
                .containsEntry("direction", "low").containsEntry("severity", "operational")
                .containsEntry("durationS", 15L);
        assertThat(closed.eventTime()).isEqualTo(T0.plusSeconds(15));
    }

    @Test
    @DisplayName("an excursion past the statutory band is statutory, and below 49.2 Hz is severe")
    void severity() {
        FrequencyExcursionDetector.Result statutory = FrequencyExcursionDetector.process(GB,
                FrequencyExcursionDetector.State.empty(), List.of(sample(0, 49.4)), T0);
        assertThat(excursions(statutory).get(0).payload()).containsEntry("severity", "statutory");

        FrequencyExcursionDetector.Result severe = FrequencyExcursionDetector.process(GB,
                FrequencyExcursionDetector.State.empty(), List.of(sample(0, 49.15)), T0);
        assertThat(excursions(severe).get(0).payload()).containsEntry("severity", "severe");
    }

    @Test
    @DisplayName("an excursion spanning two polls opens in the first and closes in the second")
    void spanningPolls() {
        FrequencyExcursionDetector.Result first = FrequencyExcursionDetector.process(GB,
                FrequencyExcursionDetector.State.empty(), List.of(sample(0, 49.5)), T0);
        assertThat(excursions(first)).hasSize(1);
        assertThat(excursions(first).get(0).payload()).containsEntry("open", true);

        FrequencyExcursionDetector.Result second = FrequencyExcursionDetector.process(GB, first.state(),
                List.of(sample(15, 49.3), sample(30, 50.0)), T0.plusSeconds(30));
        List<NormalizedRecord> closed = excursions(second);
        assertThat(closed).hasSize(1);
        assertThat(closed.get(0).payload()).containsEntry("open", false).containsEntry("extremeHz", 49.3)
                .containsEntry("durationS", 30L);
    }

    @Test
    @DisplayName("a restart skips already-processed samples and does not double-count")
    void restart() {
        FrequencyExcursionDetector.Result first = FrequencyExcursionDetector.process(GB,
                FrequencyExcursionDetector.State.empty(), List.of(sample(0, 50.0), sample(15, 49.7)), T0);
        assertThat(excursions(first)).hasSize(1);

        FrequencyExcursionDetector.Result restarted = FrequencyExcursionDetector.process(GB, first.state(),
                List.of(sample(0, 50.0), sample(15, 49.7), sample(30, 50.0)), T0.plusSeconds(30));
        List<NormalizedRecord> excursions = excursions(restarted);
        assertThat(excursions).hasSize(1);
        assertThat(excursions.get(0).payload()).containsEntry("open", false);
    }

    @Test
    @DisplayName("stale data reads no-data, never normal")
    void stale() {
        FrequencyExcursionDetector.Result first = FrequencyExcursionDetector.process(GB,
                FrequencyExcursionDetector.State.empty(), List.of(sample(0, 50.05)), T0);
        FrequencyExcursionDetector.Result stale = FrequencyExcursionDetector.process(GB, first.state(), List.of(),
                T0.plusSeconds(700));
        assertThat(state(stale).payload()).containsEntry("state", "no-data");
    }

    @Test
    @DisplayName("state round-trips through the adapter state encoding")
    void roundTrip() {
        FrequencyExcursionDetector.State state = new FrequencyExcursionDetector.State(T0, 49.8, true, T0, 49.7,
                "low");
        assertThat(FrequencyExcursionDetector.decode(FrequencyExcursionDetector.encode(state))).isEqualTo(state);
        assertThat(FrequencyExcursionDetector.decode("garbage")).isEqualTo(FrequencyExcursionDetector.State.empty());
    }
}
