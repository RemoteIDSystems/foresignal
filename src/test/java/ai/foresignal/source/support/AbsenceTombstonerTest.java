package ai.foresignal.source.support;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.support.AbsenceTombstoner.Outcome;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness.MapState;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The absence tombstoner (prompt 072): an id is tombstoned only after two consecutive complete fetches without it;
 * a 304, a truncated body or a parse error changes nothing; the state survives a round trip above 64 KB.
 */
class AbsenceTombstonerTest {

    @Test
    @DisplayName("a tombstone only after two consecutive complete misses")
    void twoConsecutiveMisses() {
        MapState state = new MapState();
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b", "c"))).isEmpty();
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b"))).as("first miss").isEmpty();
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b"))).containsExactly("c");
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b"))).as("once only").isEmpty();
    }

    @Test
    @DisplayName("an id that reappears after one miss is not tombstoned")
    void reappears() {
        MapState state = new MapState();
        AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b"));
        AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a"));
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b"))).isEmpty();
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b"))).isEmpty();
        assertThat(state.asMap()).doesNotContainKey(AbsenceTombstoner.MISSED_KEY);
    }

    @Test
    @DisplayName("a 304, a truncated body and a parse error neither tombstone nor move the state")
    void incompleteOutcomes() {
        MapState state = new MapState();
        AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a", "b"));
        AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a"));
        var before = state.asMap();
        for (Outcome outcome : List.of(Outcome.NOT_MODIFIED, Outcome.TRUNCATED, Outcome.PARSE_ERROR)) {
            assertThat(AbsenceTombstoner.update(state, outcome, List.of())).as(outcome.name()).isEmpty();
            assertThat(state.asMap()).as(outcome.name()).isEqualTo(before);
        }
        // The second complete miss still counts, because the incomplete runs did not reset anything.
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, List.of("a"))).containsExactly("b");
    }

    @Test
    @DisplayName("the first complete fetch tombstones nothing and blank ids are ignored")
    void firstFetch() {
        MapState state = new MapState();
        List<String> ids = new ArrayList<>(List.of("x", " "));
        ids.add(null);
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, ids)).isEmpty();
        assertThat(AbsenceTombstoner.read(state, AbsenceTombstoner.IDS_KEY)).containsExactly("x");
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, null)).isEmpty();
        assertThat(state.asMap()).doesNotContainKey(AbsenceTombstoner.IDS_KEY);
    }

    @Test
    @DisplayName("scopes keep independent catalogs apart")
    void scopes() {
        MapState state = new MapState();
        AbsenceTombstoner.update(state, "east", Outcome.COMPLETE, List.of("a"));
        AbsenceTombstoner.update(state, "west", Outcome.COMPLETE, List.of("b"));
        AbsenceTombstoner.update(state, "east", Outcome.COMPLETE, List.of());
        assertThat(AbsenceTombstoner.update(state, "east", Outcome.COMPLETE, List.of())).containsExactly("a");
        assertThat(state.asMap()).containsKey(AbsenceTombstoner.IDS_KEY + ":west");
    }

    @Test
    @DisplayName("state above 64 KB is stored gzipped and base64-encoded, sorted, and reads back")
    void largeStateRoundTrip() {
        MapState state = new MapState();
        Set<String> ids = new TreeSet<>();
        for (int i = 0; i < 20_000; i++) {
            ids.add("camera-" + i);
        }
        AbsenceTombstoner.update(state, Outcome.COMPLETE, ids);
        String stored = state.get(AbsenceTombstoner.IDS_KEY).orElseThrow();
        assertThat(stored).startsWith(AbsenceTombstoner.GZIP_PREFIX);
        assertThat(stored.length()).as("compressed").isLessThan(String.join("\n", ids).length());
        assertThat(AbsenceTombstoner.read(state, AbsenceTombstoner.IDS_KEY)).isEqualTo(ids);

        Set<String> fewer = new TreeSet<>(ids);
        fewer.remove("camera-7");
        AbsenceTombstoner.update(state, Outcome.COMPLETE, fewer);
        assertThat(AbsenceTombstoner.update(state, Outcome.COMPLETE, fewer)).containsExactly("camera-7");

        assertThat(AbsenceTombstoner.encode(Set.of("b", "a"))).isEqualTo("a\nb");
    }

    @Test
    @DisplayName("a corrupt compressed value reads as no state")
    void corrupt() {
        MapState state = new MapState();
        state.put(AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.GZIP_PREFIX + "not base64!");
        assertThat(AbsenceTombstoner.read(state, AbsenceTombstoner.IDS_KEY)).isEmpty();
    }

    @Test
    @DisplayName("tombstones carry the kind and time and no payload")
    void tombstones() {
        Instant at = Instant.parse("2026-10-13T18:00:00Z");
        List<NormalizedRecord> out = AbsenceTombstoner.tombstones(List.of("a"), "camera", at);
        assertThat(out).singleElement().satisfies(r -> {
            assertThat(r.tombstone()).isTrue();
            assertThat(r.kind()).isEqualTo("camera");
            assertThat(r.observedAt()).isEqualTo(at);
            assertThat(r.payload()).isEmpty();
        });
    }
}
