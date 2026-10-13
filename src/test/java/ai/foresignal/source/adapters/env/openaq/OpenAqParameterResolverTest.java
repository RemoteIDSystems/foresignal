package ai.foresignal.source.adapters.env.openaq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link OpenAqParameterResolver}: names to every matching id, the 24 h state cache and its round trip. */
class OpenAqParameterResolverTest {

    static final Instant T0 = Instant.parse("2026-10-13T00:00:00Z");

    @Test
    @DisplayName("parse: eight parameters; o3 and no2 resolve to both of their ids; an unknown name to none")
    void byName() {
        List<OpenAqParameterResolver.Parameter> all = OpenAqParameterResolver.parse(
                AdapterFixtures.bytes("openaq", "parameters.json"));
        assertThat(all).hasSize(8);
        var resolved = OpenAqParameterResolver.byName(all, List.of("pm25", "O3", "xx"));
        assertThat(resolved.keySet()).containsExactly("pm25", "o3", "xx");
        assertThat(resolved.get("pm25")).extracting(OpenAqParameterResolver.Parameter::id).containsExactly(2L);
        assertThat(resolved.get("pm25").getFirst().units()).isEqualTo("µg/m³");
        assertThat(resolved.get("o3")).extracting(OpenAqParameterResolver.Parameter::id).containsExactly(3L, 10L);
        assertThat(resolved.get("xx")).isEmpty();
    }

    @Test
    @DisplayName("cache: empty state is due; a stored list is reused for 24 h and due again after")
    void cache() {
        WeatherAdapterHarness.MapState state = new WeatherAdapterHarness.MapState();
        assertThat(OpenAqParameterResolver.due(state, T0)).isTrue();
        assertThat(OpenAqParameterResolver.cached(state)).isEmpty();

        List<OpenAqParameterResolver.Parameter> all = OpenAqParameterResolver.parse(
                AdapterFixtures.bytes("openaq", "parameters.json"));
        OpenAqParameterResolver.store(state, all, T0);
        assertThat(OpenAqParameterResolver.cached(state)).isEqualTo(all);
        assertThat(OpenAqParameterResolver.due(state, T0.plusSeconds(23 * 3600))).isFalse();
        assertThat(OpenAqParameterResolver.due(state, T0.plusSeconds(24 * 3600))).isTrue();

        state.put(OpenAqParameterResolver.CACHED_AT_KEY, "garbage");
        assertThat(OpenAqParameterResolver.due(state, T0)).isTrue();
        state.put(OpenAqParameterResolver.CACHE_KEY, "not json");
        assertThat(OpenAqParameterResolver.cached(state)).isEmpty();
    }

    @Test
    @DisplayName("an answer without parameters is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> OpenAqParameterResolver.parse("{\"results\": []}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> OpenAqParameterResolver.parse("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> OpenAqParameterResolver.parse("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
