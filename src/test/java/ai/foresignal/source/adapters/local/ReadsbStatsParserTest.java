package ai.foresignal.source.adapters.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link ReadsbStatsParser} (prompt 069): {@code messagesPerSec}, the signal fields, {@code tracksNew},
 * {@code aircraftWithPos}, the {@code state} thresholds, missing optional fields, and format errors.
 */
class ReadsbStatsParserTest {

    private static NormalizedRecord parse(String fixture, double min) {
        return ReadsbStatsParser.parse(AdapterFixtures.bytes("local-receivers", fixture), "station-readsb-1", min);
    }

    @Test
    @DisplayName("last1min: 11262 messages over 60 s is 187.7/s, with the local signal fields and new tracks")
    void healthy() {
        NormalizedRecord r = parse("readsb-stats.json", 1.0);
        assertThat(r.externalId()).isEqualTo("station-readsb-1");
        assertThat(r.kind()).isEqualTo("receiver-status");
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochSecond(1791900060L));
        assertThat(r.payload()).containsEntry("messagesPerSec", 187.7)
                .containsEntry("signalDbfs", -11.8)
                .containsEntry("noiseDbfs", -32.5)
                .containsEntry("peakSignalDbfs", -1.1)
                .containsEntry("strongSignals", 37L)
                .containsEntry("tracksNew", 9L)
                .containsEntry("aircraftWithPos", 41L)
                .containsEntry("state", "ok");
        assertThat(r.geometry()).isNull();
    }

    @Test
    @DisplayName("a minute without messages is silent; optional fields that are absent are left out")
    void silent() {
        NormalizedRecord r = parse("readsb-stats-silent.json", 1.0);
        assertThat(r.payload()).containsEntry("messagesPerSec", 0.0).containsEntry("state", "silent")
                .containsEntry("strongSignals", 0L)
                .doesNotContainKeys("signalDbfs", "noiseDbfs", "peakSignalDbfs", "aircraftWithPos");
    }

    @Test
    @DisplayName("state thresholds: below minMessagesPerSec is low-rate, at it is ok, no messages is silent")
    void thresholds() {
        assertThat(parse("readsb-stats.json", 200.0).payload()).containsEntry("state", "low-rate");
        assertThat(ReadsbStatsParser.state(60, 1.0, 1.0)).isEqualTo("ok");
        assertThat(ReadsbStatsParser.state(59, 0.98, 1.0)).isEqualTo("low-rate");
        assertThat(ReadsbStatsParser.state(0, 0.0, 0.0)).isEqualTo("silent");
    }

    @Test
    @DisplayName("a --net-only receiver without local stats still reports a rate")
    void netOnly() {
        String body = "{\"last1min\":{\"start\":100,\"end\":160,\"messages\":600,\"remote\":{\"modes\":600}}}";
        NormalizedRecord r = ReadsbStatsParser.parse(body.getBytes(StandardCharsets.UTF_8), "s", 1.0);
        assertThat(r.payload()).containsEntry("messagesPerSec", 10.0).containsEntry("state", "ok")
                .doesNotContainKeys("signalDbfs", "tracksNew");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "<html>", "[]", "{}", "{\"last1min\":{\"start\":1,\"end\":2}}",
            "{\"last1min\":{\"end\":2,\"messages\":3}}"})
    @DisplayName("a body that is not readsb stats is a format error")
    void formatErrors(String body) {
        assertThatThrownBy(() -> ReadsbStatsParser.parse(body.getBytes(StandardCharsets.UTF_8), "s", 1.0))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
