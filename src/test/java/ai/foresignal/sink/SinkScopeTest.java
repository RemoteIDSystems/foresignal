package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.watch.Identity;
import ai.foresignal.watch.IdentityResolver;
import ai.foresignal.watch.WatchlistSnapshot;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code placeId} and {@code watchlistId} members of a sink filter (prompt 110, 036), which
 * {@link SinkFilter#matches(SinkRecord, SinkScope)} now applies: a place passes records whose geometry intersects
 * it, a watchlist passes records whose declared identity is on it, and both fail closed.
 */
class SinkScopeTest {

    static final GeometryFactory GEOMETRY = new GeometryFactory();
    static final UUID LIST = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");

    @SuppressWarnings("unchecked")
    static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    static SinkRecord at(String source, String id, double lon, double lat, Map<String, Object> props) {
        return new SinkRecord(source, id, "aircraft", 1, 1, null, null, false,
                GEOMETRY.createPoint(new Coordinate(lon, lat)), props, null, null);
    }

    SinkScope scope() {
        PlaceService places = mock(PlaceService.class);
        when(places.geometry("home")).thenReturn(Optional.of(GEOMETRY.toGeometry(new Envelope(-80, -79, 43, 44))));
        when(places.geometry("gone")).thenReturn(Optional.empty());
        WatchlistSnapshot watchlists = mock(WatchlistSnapshot.class);
        when(watchlists.list(LIST)).thenReturn(Optional.of(new WatchlistSnapshot.ListSnapshot(Identity.ICAO24,
                Identity.Normalize.UPPER, Set.of("ABC123"))));
        when(watchlists.list(any(UUID.class))).thenAnswer(i -> LIST.equals(i.getArgument(0))
                ? Optional.of(new WatchlistSnapshot.ListSnapshot(Identity.ICAO24, Identity.Normalize.UPPER,
                        Set.of("ABC123")))
                : Optional.empty());
        IdentityResolver identities = mock(IdentityResolver.class);
        when(identities.path("adsblol", Identity.ICAO24)).thenReturn("hex");
        return new SinkScope(provider(places), provider(watchlists), provider(identities));
    }

    static SinkFilter filter(String placeId, String watchlistId) {
        return new SinkFilter(List.of(), List.of(), null, placeId, watchlistId, false);
    }

    @Test
    @DisplayName("placeId passes records inside the place; outside, no geometry or a deleted place fail")
    void place() {
        SinkScope scope = scope();
        assertThat(filter("home", null).matches(at("adsblol", "a", -79.4, 43.6, Map.of()), scope)).isTrue();
        assertThat(filter("home", null).matches(at("adsblol", "a", 2.35, 48.85, Map.of()), scope)).isFalse();
        assertThat(filter("gone", null).matches(at("adsblol", "a", -79.4, 43.6, Map.of()), scope)).isFalse();
        SinkRecord noGeometry = new SinkRecord("adsblol", "a", "aircraft", 1, 1, null, null, false, null, Map.of(),
                null, null);
        assertThat(filter("home", null).matches(noGeometry, scope)).isFalse();
    }

    @Test
    @DisplayName("watchlistId passes records whose identity (normalized) is on the list")
    void watchlist() {
        SinkScope scope = scope();
        assertThat(filter(null, LIST.toString()).matches(at("adsblol", "x", 0, 0, Map.of("hex", " abc123 ")), scope))
                .isTrue();
        assertThat(filter(null, LIST.toString()).matches(at("adsblol", "x", 0, 0, Map.of("hex", "def456")), scope))
                .isFalse();
        assertThat(filter(null, LIST.toString()).matches(at("undeclared", "x", 0, 0, Map.of("hex", "abc123")), scope))
                .as("a source that does not declare the identity never matches").isFalse();
        assertThat(filter(null, UUID.randomUUID().toString()).matches(at("adsblol", "x", 0, 0, Map.of()), scope))
                .isFalse();
    }

    @Test
    @DisplayName("without place or watchlist services a filter naming either matches nothing; others pass")
    void none() {
        SinkRecord record = at("adsblol", "a", -79.4, 43.6, Map.of());
        assertThat(SinkFilter.ALL.matches(record, SinkScope.NONE)).isTrue();
        assertThat(filter("home", null).matches(record, SinkScope.NONE)).isFalse();
    }

    @Test
    @DisplayName("a watchlistId that is not a UUID is refused at save")
    void validation() {
        JsonMapper json = JsonMapper.builder().build();
        assertThatThrownBy(() -> SinkFilter.parse(json.readTree("{\"watchlistId\":\"nope\"}")))
                .isInstanceOf(ValidationException.class);
        assertThat(SinkFilter.parse(json.readTree("{\"watchlistId\":\"" + LIST + "\"}")).watchlistId())
                .isEqualTo(LIST.toString());
    }
}
