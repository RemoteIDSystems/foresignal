package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * The {@code outage-weather} annotator (prompt 105 §5.2, post-review): only NWS warnings, radar ≥ 50 dBZ and
 * gusts ≥ 25 m/s (forecast or observed) within ±3 h and 25 km qualify; stored versions inside the window count
 * even when the current head is newer; the members are context with weight 0 and the alternative is
 * "weather-related damage".
 */
class WeatherEventAnnotatorTest {

    static final Instant EVENT = Instant.parse("2026-10-15T18:00:00Z");
    static final Instant FROM = EVENT.minus(Duration.ofHours(3));
    static final Instant TO = EVENT.plus(Duration.ofHours(3));
    static final Geometry AREA = Geo.point(-97.0, 32.0).buffer(0.05);

    private static HeadSnapshot head(String source, String kind, String id, Instant at, Geometry g,
            Map<String, Object> payload) {
        return new HeadSnapshot(source, id, kind, 10, 1, at, false, payload, g);
    }

    private static Geometry near() {
        return Geo.point(-97.05, 32.02).buffer(0.05);
    }

    private static Geometry far() {
        return Geo.point(-95.0, 32.0).buffer(0.05);
    }

    /** A reader over a fixed list of versions, filtered by source, kind, time and bbox. */
    private static RecordWindowReader reader(List<HeadSnapshot> versions) {
        return (sourceId, kind, from, to, bbox, limit) -> {
            List<HeadSnapshot> out = new ArrayList<>();
            for (HeadSnapshot v : versions) {
                Envelope e = v.geometry().getEnvelopeInternal();
                if (v.sourceId().equals(sourceId) && v.kind().equals(kind) && !v.observedAt().isBefore(from)
                        && !v.observedAt().isAfter(to) && e.intersects(bbox)) {
                    out.add(v);
                }
            }
            return out;
        };
    }

    private static EventAnnotator.AnnotatorContext context() {
        return new EventAnnotator.AnnotatorContext(AREA, FROM, TO, new FakeDerived(EVENT));
    }

    private static IntelligenceEventEntity event() {
        return new IntelligenceEventEntity("e1", EventTypes.INFRASTRUCTURE_DISRUPTION, "Outage", EVENT);
    }

    @Test
    @DisplayName("only severe weather qualifies: warnings, >= 50 dBZ, gusts >= 25 m/s; advisories and weak echoes do not")
    void filters() {
        List<HeadSnapshot> versions = List.of(
                head("nws-alerts", "nws-alert", "w1", EVENT.minus(Duration.ofHours(5)), near(), Map.of(
                        "event", "Severe Thunderstorm Warning", "onset", EVENT.minus(Duration.ofHours(1)).toString(),
                        "expires", EVENT.plus(Duration.ofHours(1)).toString())),
                head("nws-alerts", "nws-alert", "a1", EVENT, near(), Map.of("event", "Wind Advisory")),
                head("wx-areas", "wx-area", "refl:50:aoi", EVENT, near(), Map.of("field", "refl", "threshold", 50L,
                        "areaKm2", 120.0)),
                head("wx-areas", "wx-area", "refl:40:aoi", EVENT, near(), Map.of("field", "refl", "threshold", 40L,
                        "areaKm2", 300.0)),
                head("wx-storm-tracks", "storm-track", "t-strong", EVENT, near(), Map.of("cell", Map.of("maxDbz", 57.5))),
                head("wx-storm-tracks", "storm-track", "t-weak", EVENT, near(), Map.of("cell", Map.of("maxDbz", 45.0))),
                head("wx-forecast-areas", "forecast-area", "ifs:gust:>=25:aoi", EVENT.minus(Duration.ofHours(10)),
                        near(), Map.of("field", "gust", "threshold", Map.of("op", ">=", "value", "25"),
                                "firstExceedAt", EVENT.toString(), "validUntil", EVENT.plus(Duration.ofHours(3))
                                        .toString())),
                head("wx-forecast-areas", "forecast-area", "ifs:gust:>=20:aoi", EVENT, near(), Map.of("field", "gust",
                        "threshold", Map.of("op", ">=", "value", "20"), "firstExceedAt", EVENT.toString())),
                head("awc-metars", "metar", "KDFW", EVENT, Geo.point(-97.02, 32.0), Map.of("gustKt", 52)),
                head("awc-metars", "metar", "KAFW", EVENT, Geo.point(-97.03, 32.0), Map.of("gustKt", 30)),
                head("wx-storm-tracks", "storm-track", "t-far", EVENT, far(), Map.of("cell", Map.of("maxDbz", 60.0))));
        EventAnnotator.Result result = new WeatherEventAnnotator(reader(versions)).annotate(event(), context());

        assertThat(result.contextMembers()).extracting(m -> m.get("externalId"))
                .containsExactlyInAnyOrder("w1", "refl:50:aoi", "t-strong", "ifs:gust:>=25:aoi", "KDFW");
        assertThat(result.contextMembers()).allSatisfy(m -> {
            assertThat(m).containsEntry("role", "context").containsEntry("weight", 0.0);
        });
        @SuppressWarnings("unchecked")
        Map<String, Object> weather = (Map<String, Object>) result.context().get("weather");
        assertThat((List<?>) weather.get("warnings")).hasSize(1);
        assertThat((List<?>) weather.get("radar")).hasSize(1);
        assertThat((List<?>) weather.get("tracks")).hasSize(1);
        assertThat((List<?>) weather.get("gusts")).hasSize(2);
        assertThat(result.alternatives()).singleElement()
                .satisfies(a -> assertThat(a).containsEntry("hypothesis", "weather-related damage"));
    }

    @Test
    @DisplayName("a version inside the window counts although the current head is newer; one outside does not")
    void versionsInTheWindow() {
        List<HeadSnapshot> versions = List.of(
                head("wx-storm-tracks", "storm-track", "t-1", EVENT.plus(Duration.ofHours(6)), near(),
                        Map.of("cell", Map.of("maxDbz", 40.0))),
                head("wx-storm-tracks", "storm-track", "t-1", EVENT.plus(Duration.ofHours(1)), near(),
                        Map.of("cell", Map.of("maxDbz", 55.0))),
                head("wx-storm-tracks", "storm-track", "t-2", EVENT.minus(Duration.ofHours(4)), near(),
                        Map.of("cell", Map.of("maxDbz", 60.0))));
        EventAnnotator.Result result = new WeatherEventAnnotator(reader(versions)).annotate(event(), context());
        assertThat(result.contextMembers()).extracting(m -> m.get("externalId")).containsExactly("t-1");
    }

    @Test
    @DisplayName("without qualifying weather nothing is added; without a reader the current heads are used")
    void nothingAndHeadsFallback() {
        assertThat(new WeatherEventAnnotator(reader(List.of())).annotate(event(), context()).isEmpty()).isTrue();
        FakeDerived world = new FakeDerived(EVENT);
        world.heads.put("wx-storm-tracks", List.of(head("wx-storm-tracks", "storm-track", "t-now", EVENT, near(),
                Map.of("cell", Map.of("maxDbz", 52.5)))));
        EventAnnotator.Result result = new WeatherEventAnnotator().annotate(event(),
                new EventAnnotator.AnnotatorContext(AREA, FROM, TO, world));
        assertThat(result.contextMembers()).extracting(m -> m.get("externalId")).containsExactly("t-now");
    }

    @Test
    @DisplayName("qualifies() and inWindow() rules")
    void rules() {
        assertThat(WeatherEventAnnotator.qualifies("metar", Map.of("gustKt", 49))).isTrue();
        assertThat(WeatherEventAnnotator.qualifies("metar", Map.of("gustKt", 48))).isFalse();
        assertThat(WeatherEventAnnotator.qualifies("forecast-area", Map.of("field", "gust",
                "threshold", Map.of("op", "<=", "value", 30)))).isFalse();
        assertThat(WeatherEventAnnotator.qualifies("nws-alert", Map.of("event", "Tornado Warning"))).isTrue();
        WeatherEventAnnotator.Input alert = new WeatherEventAnnotator.Input("nws-alerts", "nws-alert", true);
        HeadSnapshot ended = head("nws-alerts", "nws-alert", "x", EVENT.minus(Duration.ofHours(8)), near(),
                Map.of("event", "Tornado Warning", "expires", EVENT.minus(Duration.ofHours(4)).toString()));
        assertThat(WeatherEventAnnotator.inWindow(alert, ended, FROM, TO)).isFalse();
    }
}
