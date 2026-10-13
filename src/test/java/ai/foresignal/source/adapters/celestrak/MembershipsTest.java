package ai.foresignal.source.adapters.celestrak;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.AdapterStateStore;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link Memberships}: delta encoding, the due order and the index (prompt 064). */
class MembershipsTest {

    private static final Instant NOW = Instant.parse("2026-10-13T12:00:00Z");

    /** An in-memory state store. */
    static final class MapState implements AdapterStateStore {
        final Map<String, String> values = new HashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public void put(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }
    }

    @Test
    @DisplayName("members are stored sorted and delta-encoded and decode back")
    void encoding() {
        assertThat(Memberships.encode(List.of(100057L, 25544L, 25545L))).isEqualTo("25544,1,74512");
        assertThat(Memberships.decode("25544,1,74512")).containsExactly(25544L, 25545L, 100057L);
        assertThat(Memberships.decode("")).isEmpty();
    }

    @Test
    @DisplayName("never-fetched groups first, then the stalest, at most two; fresh groups are not due")
    void due() {
        MapState state = new MapState();
        Memberships.store(state, "stations", List.of(1L), NOW.minus(Duration.ofHours(30)));
        Memberships.store(state, "visual", List.of(2L), NOW.minus(Duration.ofHours(26)));
        Memberships.store(state, "weather", List.of(3L), NOW.minus(Duration.ofHours(2)));
        assertThat(Memberships.due(state, List.of("stations", "visual", "weather", "gnss"), NOW))
                .containsExactly("gnss", "stations");
        assertThat(Memberships.due(state, List.of("stations", "visual", "weather"), NOW))
                .containsExactly("stations", "visual");
        assertThat(Memberships.due(state, List.of("weather"), NOW)).isEmpty();
    }

    @Test
    @DisplayName("the index maps each catalog number to its sorted groups; touch refreshes the time only")
    void index() {
        MapState state = new MapState();
        Memberships.store(state, "visual", List.of(25544L, 20580L), NOW);
        Memberships.store(state, "stations", List.of(25544L), NOW);
        Map<Long, List<String>> index = Memberships.index(state, List.of("visual", "stations"));
        assertThat(index.get(25544L)).containsExactly("stations", "visual");
        assertThat(index.get(20580L)).containsExactly("visual");
        Memberships.touch(state, "visual", NOW.plusSeconds(60));
        assertThat(Memberships.fetchedAt(state, "visual")).contains(NOW.plusSeconds(60));
        assertThat(Memberships.members(state, "visual")).containsExactly(20580L, 25544L);
    }
}
