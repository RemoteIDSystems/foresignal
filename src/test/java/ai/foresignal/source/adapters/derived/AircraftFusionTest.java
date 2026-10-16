package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-aircraft} fusion (prompt 099). */
class AircraftFusionTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");

    static Observation aircraft(String source, String hex, Instant at, String posSource, PositionSource position) {
        return new Observation(source, hex, "aircraft", at, Geo.point(-95, 32),
                Map.of("hex", hex, "gs", 250, "posSource", posSource), position);
    }

    @Test
    @DisplayName("the freshest observation wins regardless of arrival order")
    void freshestWins() {
        Observation older = aircraft("adsblol-area-1", "abc", T0, "adsb", PositionSource.REPORTED);
        Observation newer = aircraft("adsbfi-region-1", "abc", T0.plusSeconds(20), "adsb", PositionSource.REPORTED);
        assertThat(AircraftFusionGenerator.winner(List.of(older, newer)).sourceId()).isEqualTo("adsbfi-region-1");
        assertThat(AircraftFusionGenerator.winner(List.of(newer, older)).sourceId()).isEqualTo("adsbfi-region-1");
    }

    @Test
    @DisplayName("on a tie ADS-B beats MLAT, and a local receiver beats the network")
    void tieBreaks() {
        Observation mlat = aircraft("adsblol-area-1", "abc", T0, "mlat", PositionSource.CALCULATED);
        Observation adsb = aircraft("adsbfi-region-1", "abc", T0, "adsb", PositionSource.REPORTED);
        assertThat(AircraftFusionGenerator.winner(List.of(mlat, adsb)).sourceId()).isEqualTo("adsbfi-region-1");
        Observation local = aircraft("local-adsb-1", "abc", T0, "adsb", PositionSource.REPORTED);
        assertThat(AircraftFusionGenerator.winner(List.of(adsb, local)).sourceId()).isEqualTo("local-adsb-1");
    }

    @Test
    @DisplayName("on a tie MLAT beats TIS-B, in either arrival order, though both are calculated positions")
    void mlatBeatsTisB() {
        // The network source would win the locality tie-break; only the MLAT-over-TIS-B rank decides here.
        Observation tisb = aircraft("local-adsb-1", "abc", T0, "tisb", PositionSource.CALCULATED);
        Observation mlat = aircraft("adsblol-area-1", "abc", T0, "mlat", PositionSource.CALCULATED);
        assertThat(AircraftFusionGenerator.winner(List.of(tisb, mlat)).sourceId()).isEqualTo("adsblol-area-1");
        assertThat(AircraftFusionGenerator.winner(List.of(mlat, tisb)).sourceId()).isEqualTo("adsblol-area-1");
        Observation other = aircraft("local-adsb-2", "abc", T0, "other", PositionSource.UNKNOWN);
        assertThat(AircraftFusionGenerator.winner(List.of(other, tisb)).sourceId()).isEqualTo("local-adsb-1");
    }

    @Test
    @DisplayName("the fused payload lists every source and the winning primary source")
    void provenance() {
        Observation a = aircraft("adsblol-area-1", "abc", T0, "adsb", PositionSource.REPORTED);
        Observation b = aircraft("local-adsb-1", "abc", T0.plusSeconds(5), "adsb", PositionSource.REPORTED);
        NormalizedRecord record = AircraftFusionGenerator.record(AircraftFusionGenerator.winner(List.of(a, b)), List.of(a, b));
        assertThat(record.externalId()).isEqualTo("abc");
        assertThat(record.payload().get("primarySource")).isEqualTo("local-adsb-1");
        assertThat(record.payload().get("sources")).isEqualTo(List.of("adsblol-area-1", "local-adsb-1"));
        assertThat(record.payload().get("positionSource")).isEqualTo("adsb");
    }
}
