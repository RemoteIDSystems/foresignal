package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-corroboration} clustering and credibility (prompt 099). */
class CorroborationTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");

    private static Observation quake(String source, String id, double lon, double lat, Instant at, double mag) {
        return new Observation(source, id, "earthquake", at, Geo.point(lon, lat), Map.of("mag", mag),
                PositionSource.UNKNOWN);
    }

    @Test
    @DisplayName("USGS and EMSC within tolerances cluster together")
    void independentCluster() {
        Observation usgs = quake("usgs", "a", -95.0, 32.0, T0, 5.0);
        Observation emsc = quake("emsc", "b", -95.2, 32.1, T0.plusSeconds(30), 5.4);
        assertThat(CorroborationGenerator.compatible(CorroborationGenerator.Family.EARTHQUAKE, usgs, emsc)).isTrue();
        List<List<Observation>> clusters = CorroborationGenerator.cluster(CorroborationGenerator.Family.EARTHQUAKE,
                List.of(usgs, emsc));
        assertThat(clusters).hasSize(1);
        assertThat(CorroborationGenerator.ulid(clusters.get(0))).hasSize(16);
    }

    @Test
    @DisplayName("events too far apart in space, time or magnitude do not cluster")
    void tolerances() {
        Observation usgs = quake("usgs", "a", -95.0, 32.0, T0, 5.0);
        assertThat(CorroborationGenerator.compatible(CorroborationGenerator.Family.EARTHQUAKE, usgs,
                quake("emsc", "b", -90.0, 32.0, T0, 5.0))).isFalse();
        assertThat(CorroborationGenerator.compatible(CorroborationGenerator.Family.EARTHQUAKE, usgs,
                quake("emsc", "c", -95.0, 32.0, T0.plusSeconds(300), 5.0))).isFalse();
        assertThat(CorroborationGenerator.compatible(CorroborationGenerator.Family.EARTHQUAKE, usgs,
                quake("emsc", "d", -95.0, 32.0, T0, 6.5))).isFalse();
    }
}
