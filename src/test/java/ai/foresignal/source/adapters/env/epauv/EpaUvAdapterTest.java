package ai.foresignal.source.adapters.env.epauv;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.PlaceTimeZones;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * EPA UV record identity, timezone fallback and the missing-ZIP note (prompt 106 task 6, post-review fix).
 */
class EpaUvAdapterTest {

    static final PlacePoints.Summary AUSTIN = new PlacePoints.Summary("home", "Home", -97.74, 30.27, List.of(), 1);

    /** Places whose only answers are a timezone and a ZIP. */
    static PlacePoints places(String timezone, String zip) {
        return new PlacePoints() {
            @Override
            public Optional<double[]> point(String placeId) {
                return Optional.of(new double[] {AUSTIN.lon(), AUSTIN.lat()});
            }

            @Override
            public Map<String, Object> attributes(String placeId) {
                return zip == null ? Map.of() : Map.of("zip", zip);
            }

            @Override
            public Optional<String> timezone(String placeId) {
                return Optional.ofNullable(timezone);
            }
        };
    }

    @Test
    @DisplayName("the externalId is place:{id}")
    void externalId() {
        assertThat(EpaUvAdapter.externalId("home")).isEqualTo("place:home");
    }

    @Test
    @DisplayName("the place's own timezone wins")
    void ownZone() {
        PlaceTimeZones gazetteer = (lon, lat) -> Optional.of(ZoneId.of("America/Denver"));
        assertThat(EpaUvAdapter.zoneOf(places("America/Chicago", "78701"), gazetteer, AUSTIN))
                .contains(ZoneId.of("America/Chicago"));
    }

    @Test
    @DisplayName("without a place timezone, the gazetteer zone at the place's point is used")
    void gazetteerFallback() {
        PlaceTimeZones gazetteer = (lon, lat) -> lon < -90 ? Optional.of(ZoneId.of("America/Chicago")) : Optional.empty();
        assertThat(EpaUvAdapter.zoneOf(places(null, "78701"), gazetteer, AUSTIN)).contains(ZoneId.of("America/Chicago"));
        assertThat(EpaUvAdapter.zoneOf(places("Not/AZone", "78701"), gazetteer, AUSTIN))
                .contains(ZoneId.of("America/Chicago"));
    }

    @Test
    @DisplayName("with neither, no zone is known (the adapter then reads UTC and warns)")
    void noZone() {
        assertThat(EpaUvAdapter.zoneOf(places(null, "78701"), PlaceTimeZones.none(), AUSTIN)).isEmpty();
    }

    @Test
    @DisplayName("the missing-ZIP note names the skipped places, up to five")
    void missingZipNote() {
        assertThat(EpaUvAdapter.missingZipNote(List.of("cabin")))
                .isEqualTo("1 place(s) skipped without a zip attribute (set attributes.zip for UV): cabin");
        String many = EpaUvAdapter.missingZipNote(List.of("a", "b", "c", "d", "e", "f", "g"));
        assertThat(many).startsWith("7 place(s) skipped").contains("a, b, c, d, e").endsWith(" and 2 more");
    }

    @Test
    @DisplayName("a ZIP must be five digits")
    void zipShape() {
        assertThat(EpaUvAdapter.zip(places(null, "78701"), "home")).isEqualTo("78701");
        assertThat(EpaUvAdapter.zip(places(null, "7870"), "home")).isNull();
        assertThat(EpaUvAdapter.zip(places(null, null), "home")).isNull();
    }
}
