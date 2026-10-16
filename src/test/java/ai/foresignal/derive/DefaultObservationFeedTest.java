package ai.foresignal.derive;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.testsupport.weather.FakeDerived;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The observation feed's read (prompt 099): the time filter applies before the cap, the newest entities are
 * kept when the cap is reached, a capped read is counted, and MLAT and TIS-B both read as calculated.
 */
class DefaultObservationFeedTest {

    private static final Instant NOW = Instant.parse("2026-10-15T12:00:00Z");

    private static HeadSnapshot head(String id, Instant at) {
        return new HeadSnapshot("aisstream", id, "vessel", 1, 1, at, false, Map.of("mmsi", id), Geo.point(0, 0));
    }

    private static DefaultObservationFeed feed(FakeDerived world, SimpleMeterRegistry meters) {
        return new DefaultObservationFeed(world, null, null, null, world.clock, meters);
    }

    @Test
    @DisplayName("an entity that sorts last by id but is recent is still read; stale ones are filtered in the query")
    void timeFilterBeforeTheCap() {
        FakeDerived world = new FakeDerived(NOW);
        List<HeadSnapshot> heads = new ArrayList<>();
        heads.add(head("100000001", NOW.minusSeconds(7200)));
        heads.add(head("100000002", NOW.minusSeconds(7200)));
        heads.add(head("999999999", NOW.minusSeconds(60)));
        world.heads.put("aisstream", heads);
        SimpleMeterRegistry meters = new SimpleMeterRegistry();

        List<Observation> recent = feed(world, meters).recent(Set.of("aisstream"), Set.of(), NOW.minusSeconds(600), 1);

        assertThat(recent).extracting(Observation::externalId).containsExactly("999999999");
    }

    @Test
    @DisplayName("at the cap the newest entities are kept and the truncation is counted")
    void capKeepsTheNewestAndCounts() {
        FakeDerived world = new FakeDerived(NOW);
        world.heads.put("aisstream", new ArrayList<>(List.of(
                head("a", NOW.minusSeconds(300)), head("b", NOW.minusSeconds(100)), head("c", NOW.minusSeconds(200)))));
        SimpleMeterRegistry meters = new SimpleMeterRegistry();

        List<Observation> recent = feed(world, meters).recent(Set.of("aisstream"), Set.of(), NOW.minusSeconds(600), 2);

        assertThat(recent).extracting(Observation::externalId).containsExactly("b", "c");
        assertThat(meters.counter(DefaultObservationFeed.TRUNCATED_METRIC).count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("a read under the cap is not counted as truncated")
    void underTheCapNotCounted() {
        FakeDerived world = new FakeDerived(NOW);
        world.heads.put("aisstream", new ArrayList<>(List.of(head("a", NOW.minusSeconds(300)))));
        SimpleMeterRegistry meters = new SimpleMeterRegistry();

        feed(world, meters).recent(Set.of("aisstream"), Set.of(), NOW.minusSeconds(600), 10);

        assertThat(meters.counter(DefaultObservationFeed.TRUNCATED_METRIC).count()).isZero();
    }

    @Test
    @DisplayName("position sources: ADS-B reported, MLAT and TIS-B calculated, vessels reported")
    void positionSources() {
        assertThat(DefaultObservationFeed.positionSource("aircraft", Map.of("posSource", "adsb")))
                .isEqualTo(PositionSource.REPORTED);
        assertThat(DefaultObservationFeed.positionSource("aircraft", Map.of("posSource", "mlat")))
                .isEqualTo(PositionSource.CALCULATED);
        assertThat(DefaultObservationFeed.positionSource("aircraft", Map.of("posSource", "tisb")))
                .isEqualTo(PositionSource.CALCULATED);
        assertThat(DefaultObservationFeed.positionSource("vessel", Map.of())).isEqualTo(PositionSource.REPORTED);
    }
}
