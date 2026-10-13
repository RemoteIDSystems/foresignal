package ai.foresignal.source.adapters.nhc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.adapters.nhc.NhcLayerResolver.Layer;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link NhcLayerResolver}: layer ids by name from the real service document, and a missing name. */
class NhcLayerResolverTest {

    static final Instant NOW = Instant.parse("2026-10-13T19:30:00Z");

    /** A map-backed state store. */
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
    @DisplayName("the real service document: Forecast Points 5, Track 6, Cone 7, Watch-Warning 8")
    void resolvesByName() {
        Map<Layer, Integer> ids = NhcLayerResolver.parse(AdapterFixtures.bytes("nhc-cyclone-cones", "service.json"));
        assertThat(ids).containsExactly(Map.entry(Layer.POINTS, 5), Map.entry(Layer.TRACK, 6),
                Map.entry(Layer.CONE, 7), Map.entry(Layer.WARNING, 8));
    }

    @Test
    @DisplayName("a renumbered service still resolves; a missing name throws")
    void renumberedAndMissing() {
        String renumbered = "{\"layers\": [{\"id\": 31, \"name\": \"Watch-Warning\"}, {\"id\": 30, \"name\":"
                + " \"Forecast Cone\"}, {\"id\": 29, \"name\": \"Forecast Track\"}, {\"id\": 28, \"name\":"
                + " \"Forecast Points\"}]}";
        assertThat(NhcLayerResolver.parse(renumbered.getBytes(StandardCharsets.UTF_8))).containsEntry(Layer.CONE, 30)
                .containsEntry(Layer.POINTS, 28);
        assertThatThrownBy(() -> NhcLayerResolver.parse(AdapterFixtures.bytes("nhc-cyclone-cones",
                "service-missing-layer.json"))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("Forecast Cone");
        assertThatThrownBy(() -> NhcLayerResolver.parse("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NhcLayerResolver.parse("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the cache: encoded, reused for 24 h, ignored when stale or garbled")
    void cache() {
        MapState state = new MapState();
        Map<Layer, Integer> ids = NhcLayerResolver.parse(AdapterFixtures.bytes("nhc-cyclone-cones", "service.json"));
        state.put(NhcLayerResolver.LAYERS_KEY, NhcLayerResolver.encode(ids));
        state.put(NhcLayerResolver.LAYERS_AT_KEY, NOW.toString());
        assertThat(state.values.get(NhcLayerResolver.LAYERS_KEY)).isEqualTo("points=5,track=6,cone=7,warning=8");
        assertThat(NhcLayerResolver.cached(state, NOW.plus(Duration.ofHours(23)))).isEqualTo(ids);
        assertThat(NhcLayerResolver.cached(state, NOW.plus(Duration.ofHours(25)))).isNull();
        state.put(NhcLayerResolver.LAYERS_KEY, "points=5,track=x");
        assertThat(NhcLayerResolver.cached(state, NOW)).isNull();
    }
}
