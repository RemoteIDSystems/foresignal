package ai.foresignal.source.formats.cap;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link CapThreadResolver} (prompt 067): chains through references, the custom key, persistence and eviction. */
class CapThreadResolverTest {

    private static final Instant NOW = Instant.parse("2026-10-13T12:00:00Z");

    private static CapAlert alert(String identifier, String msgType, CapAlert.Reference... references) {
        return new CapAlert("1.2", identifier, "s@example.org", OffsetDateTime.parse("2026-10-13T12:00:00Z"),
                "Actual", msgType, null, "Public", null, null, List.of(), null, List.of(references), null, List.of());
    }

    private static CapAlert.Reference ref(String identifier, String sent) {
        return new CapAlert.Reference("s@example.org", identifier, sent == null ? null : OffsetDateTime.parse(sent));
    }

    @Test
    @DisplayName("an Alert keys its own thread; an Update and a Cancel join it through references")
    void chain() {
        CapThreadResolver resolver = new CapThreadResolver();

        String first = resolver.resolve(alert("A1", "Alert"), null, NOW);
        String second = resolver.resolve(alert("A2", "Update", ref("A1", "2026-10-13T11:00:00Z")), null, NOW);
        String third = resolver.resolve(alert("A3", "Cancel", ref("A2", "2026-10-13T11:30:00Z")), null, NOW);

        assertThat(first).isEqualTo("s@example.org:A1");
        assertThat(second).isEqualTo(first);
        assertThat(third).as("A2 is remembered as part of A1's thread").isEqualTo(first);
        assertThat(resolver.lookup("A3")).contains(first);
    }

    @Test
    @DisplayName("without a remembered reference, the earliest referenced message keys the thread")
    void earliestReference() {
        CapThreadResolver resolver = new CapThreadResolver();

        String key = resolver.resolve(alert("B3", "Update", ref("B2", "2026-10-13T11:30:00Z"),
                ref("B1", "2026-10-13T11:00:00Z")), null, NOW);

        assertThat(key).isEqualTo("s@example.org:B1");
        assertThat(resolver.lookup("B2")).contains(key);
    }

    @Test
    @DisplayName("a custom key wins over references and is remembered for later messages")
    void customKeyWins() {
        CapThreadResolver resolver = new CapThreadResolver();

        String key = resolver.resolve(alert("C1", "Alert"), "PAAQ:abc", NOW);
        String follow = resolver.resolve(alert("C2", "Update", ref("C1", null)), null, NOW);

        assertThat(key).isEqualTo("PAAQ:abc");
        assertThat(follow).isEqualTo("PAAQ:abc");
    }

    @Test
    @DisplayName("the map survives a save and load through the adapter state")
    void persistence() {
        WeatherAdapterHarness.MapState state = new WeatherAdapterHarness.MapState();
        CapThreadResolver resolver = CapThreadResolver.load(state, NOW);
        resolver.resolve(alert("D1", "Alert"), "T:1", NOW);
        resolver.save(state);

        assertThat(state.get(CapThreadResolver.STATE_KEY)).isPresent();
        CapThreadResolver reloaded = CapThreadResolver.load(state, NOW.plusSeconds(60));
        assertThat(reloaded.lookup("D1")).contains("T:1");
        assertThat(reloaded.resolve(alert("D2", "Update", ref("D1", null)), null, NOW.plusSeconds(60))).isEqualTo("T:1");
    }

    @Test
    @DisplayName("entries expire after the TTL and the least recently used go beyond the cap")
    void eviction() {
        WeatherAdapterHarness.MapState state = new WeatherAdapterHarness.MapState();
        CapThreadResolver resolver = new CapThreadResolver(Duration.ofDays(14), 3);
        resolver.resolve(alert("E1", "Alert"), "K1", NOW);
        resolver.resolve(alert("E2", "Alert"), "K2", NOW.plusSeconds(1));
        resolver.resolve(alert("E3", "Alert"), "K3", NOW.plusSeconds(2));
        resolver.resolve(alert("E4", "Alert"), "K4", NOW.plusSeconds(3));

        assertThat(resolver.size()).isEqualTo(3);
        assertThat(resolver.lookup("E1")).isEmpty();
        assertThat(resolver.lookup("E4")).contains("K4");

        resolver.save(state);
        CapThreadResolver later = CapThreadResolver.load(state, NOW.plus(Duration.ofDays(15)), Duration.ofDays(14), 3);
        assertThat(later.size()).isZero();
        assertThat(CapThreadResolver.TTL).isEqualTo(Duration.ofDays(14));
        assertThat(CapThreadResolver.MAX_ENTRIES).isEqualTo(20_000);
    }

    @Test
    @DisplayName("unreadable stored lines are dropped")
    void corruptState() {
        WeatherAdapterHarness.MapState state = new WeatherAdapterHarness.MapState();
        state.put(CapThreadResolver.STATE_KEY, "garbage\nF1\tK\tnot-a-number\nF2\tK2\t" + NOW.getEpochSecond());

        CapThreadResolver resolver = CapThreadResolver.load(state, NOW);

        assertThat(resolver.size()).isEqualTo(1);
        assertThat(resolver.lookup("F2")).contains("K2");
    }
}
