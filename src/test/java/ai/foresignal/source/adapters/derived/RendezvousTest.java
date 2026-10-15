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

/** {@code derived-rendezvous} neighbour grid (prompt 099). */
class RendezvousTest {

    private static Observation vessel(String id, double lon, double lat) {
        return new Observation("aisstream", id, "vessel", Instant.parse("2026-10-15T10:00:00Z"), Geo.point(lon, lat),
                Map.of("mmsi", id, "sog", 1), PositionSource.REPORTED);
    }

    @Test
    @DisplayName("the neighbour search returns the eight surrounding cells, not the world")
    void neighbours() {
        Observation a = vessel("a", -95.0, 32.0);
        Observation near = vessel("b", -95.001, 32.001);
        Observation far = vessel("c", -80.0, 20.0);
        Map<String, List<Observation>> grid = RendezvousGenerator.bucket(List.of(a, near, far));
        List<Observation> candidates = RendezvousGenerator.neighbours(grid, a);
        assertThat(candidates).extracting(Observation::externalId).contains("b").doesNotContain("c");
    }
}
