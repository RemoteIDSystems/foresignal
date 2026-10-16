package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The place-weather {@code uv} section reads {@code epa-uv} records keyed {@code place:{id}} (prompt 106 post-review). */
class PlaceUvKeyTest {

    static final Instant NOW = Instant.parse("2026-10-16T15:00:00Z");

    @Test
    @DisplayName("a uv-forecast record keyed place:{id} fills the uv section")
    void placeKey() {
        FakeDerived world = new FakeDerived(NOW);
        world.addPlace("home", "Home", -98.0, 30.5, 20, 1);
        world.heads.put(WxPlacesGenerator.UV, List.of(new HeadSnapshot(WxPlacesGenerator.UV, "place:home",
                WxPlacesGenerator.UV_KIND, 1, 1, NOW, false, Map.of("maxUv", 8.4), Geo.point(-98.0, 30.5))));
        WxPlacesGenerator g = new WxPlacesGenerator();
        DefaultDerivedContext ctx = world.context(g.descriptor(), ValidatedParams.empty());
        g.reconcile(List.of(), ctx);
        NormalizedRecord r = world.collect(ctx).stream().filter(x -> x.externalId().equals("place:home")).findFirst()
                .orElseThrow();
        @SuppressWarnings("unchecked")
        Map<String, Object> uv = (Map<String, Object>) r.payload().get("uv");
        assertThat(uv).containsEntry("status", "ok").containsEntry("maxToday", 8L);
    }
}
