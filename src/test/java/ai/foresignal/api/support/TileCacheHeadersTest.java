package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.TilesController;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerType;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * The caching rule of time-travel responses ({@link TimeTravelCache}) and the canonical
 * {@code asOf} ({@link AsOfParam}) of prompt 037: immutable versus short max-age per rule,
 * {@code public} versus {@code private}, and the canonical redirect.
 */
class TileCacheHeadersTest {

    static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");
    static final Instant HORIZON = NOW.minus(Duration.ofDays(2));

    @Test
    @DisplayName("immutable only for a closed minute, settled history and 1 h past the horizon")
    void immutableRule() {
        Instant past = NOW.minus(Duration.ofHours(3));
        assertThat(TimeTravelCache.immutable(past, NOW, HORIZON, true)).isTrue();
        assertThat(TimeTravelCache.immutable(NOW.minus(Duration.ofMinutes(2)), NOW, HORIZON, true))
                .as("exactly two minutes old").isTrue();
        assertThat(TimeTravelCache.immutable(NOW.minus(Duration.ofSeconds(119)), NOW, HORIZON, true))
                .as("minute bucket may still be open").isFalse();
        assertThat(TimeTravelCache.immutable(past, NOW, HORIZON, false)).as("pending downsampling").isFalse();
        assertThat(TimeTravelCache.immutable(HORIZON.plus(Duration.ofMinutes(59)), NOW, HORIZON, true))
                .as("too close to the horizon").isFalse();
        assertThat(TimeTravelCache.immutable(HORIZON.plus(Duration.ofHours(1)), NOW, HORIZON, true)).isTrue();
        assertThat(TimeTravelCache.immutable(past, NOW, null, true)).as("no involved source").isTrue();
    }

    @Test
    @DisplayName("public or private, immutable or max-age=300")
    void cacheControl() {
        assertThat(TimeTravelCache.cacheControl(true, true)).isEqualTo("public, max-age=31536000, immutable");
        assertThat(TimeTravelCache.cacheControl(true, false)).isEqualTo("private, max-age=31536000, immutable");
        assertThat(TimeTravelCache.cacheControl(false, true)).isEqualTo("public, max-age=300");
        assertThat(TimeTravelCache.cacheControl(false, false)).isEqualTo("private, max-age=300");
    }

    @Test
    @DisplayName("public only while openReads covers the caller and the layer has no private source")
    void publicOrPrivate() {
        SourceDescriptor open = descriptor("open-src", false);
        SourceDescriptor secret = descriptor("secret-src", true);
        TileLayer openLayer = new TileLayer("k", TileLayerType.KIND, "k", "d", List.of(open), List.of(), null, Map.of());
        TileLayer mixed = new TileLayer("k", TileLayerType.KIND, "k", "d", List.of(open, secret), List.of(), null,
                Map.of());
        ReadCaller anonymousOpen = new ReadCaller(null, false, true);
        ReadCaller closed = new ReadCaller(Scope.READ, false, false);
        assertThat(TilesController.publicAsOf(anonymousOpen, openLayer)).isTrue();
        assertThat(TilesController.publicAsOf(anonymousOpen, mixed)).isFalse();
        assertThat(TilesController.publicAsOf(closed, openLayer)).isFalse();
        ReadCaller restricted = new ReadCaller(Scope.READ, false, true,
                new ReadRestriction(List.of("open-src"), null, null, null, null, false, null));
        assertThat(TilesController.publicAsOf(restricted, openLayer)).as("restricted responses are private").isFalse();
    }

    private static SourceDescriptor descriptor(String id, boolean privateSource) {
        return SourceDescriptor.builder(id).name(id).kind("k").mode(Mode.PUSH).defaultBudget(Budget.of(60, 100))
                .privateSource(privateSource).build();
    }

    @Test
    @DisplayName("asOf is normalized to whole seconds; other spellings need a redirect")
    void canonicalForm() {
        assertThat(AsOfParam.parse(null)).isNull();
        assertThat(AsOfParam.parse(" ")).isNull();
        AsOfParam canonical = AsOfParam.parse("2026-03-01T00:00:30Z");
        assertThat(canonical.redirect()).isFalse();
        assertThat(canonical.instant()).isEqualTo(Instant.parse("2026-03-01T00:00:30Z"));
        AsOfParam fraction = AsOfParam.parse("2026-03-01T00:00:30.999Z");
        assertThat(fraction.redirect()).isTrue();
        assertThat(fraction.canonical()).isEqualTo("2026-03-01T00:00:30Z");
        AsOfParam offset = AsOfParam.parse("2026-03-01T01:00:30+01:00");
        assertThat(offset.redirect()).isTrue();
        assertThat(offset.canonical()).isEqualTo("2026-03-01T00:00:30Z");
        assertThatThrownBy(() -> AsOfParam.parse("yesterday")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the redirect keeps the original path and query (key-in-path and ticket) and replaces asOf once")
    void redirectLocation() {
        MockHttpServletRequest original = new MockHttpServletRequest("GET", "/v1/k/fsf_abc/tiles/quake/3/1/2.mvt");
        original.setQueryString("ticket=fst_x&asOf=2026-03-01T00:00:30.5Z&agg=hex&asOf=2026-01-01T00:00:00Z");
        HttpServletRequest wrapped = new HttpServletRequestWrapper(original) {
            @Override
            public String getRequestURI() {
                return "/v1/tiles/quake/3/1/2.mvt";
            }

            @Override
            public String getQueryString() {
                return "agg=hex";
            }
        };
        assertThat(AsOfParam.redirectLocation(wrapped, "2026-03-01T00:00:30Z"))
                .isEqualTo("/v1/k/fsf_abc/tiles/quake/3/1/2.mvt?ticket=fst_x&asOf=2026-03-01T00:00:30Z&agg=hex");

        MockHttpServletRequest form = new MockHttpServletRequest("GET", "/v1/tiles.json");
        form.addParameter("asOf", "2026-03-01T00:00:30.5Z");
        assertThat(AsOfParam.redirectLocation(form, "2026-03-01T00:00:30Z"))
                .isEqualTo("/v1/tiles.json?asOf=2026-03-01T00:00:30Z");
    }
}
