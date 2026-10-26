package ai.foresignal.egress;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link RouteSelector} (prompt R2-018): each rotation policy, {@code rotate-on-rate-limit} moving a source off a route
 * that throttled it, pins and exclusion, {@code direct} selecting no route, and failing closed with no usable route.
 */
class RouteSelectorTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID C = UUID.fromString("00000000-0000-0000-0000-00000000000c");
    private static final List<UUID> POOL = List.of(A, B, C);
    private static final Instant LATER = Instant.parse("2026-10-26T12:00:00Z");
    private static final Function<UUID, Instant> NONE_THROTTLED = id -> null;

    private final RouteSelector selector = new RouteSelector(bound -> bound - 1);

    private static EgressSettings policy(String rotation) {
        return new EgressSettings(rotation, null);
    }

    private RouteSelector.Selection pick(String source, EgressSettings settings, RouteSelector.Assignment assignment,
            Function<UUID, Instant> throttled) {
        return selector.select(source, settings, POOL, assignment, null, throttled);
    }

    @Test
    @DisplayName("direct selects no route for every source, a pinned one included")
    void directSelectsNoRoute() {
        assertThat(pick("s", EgressSettings.defaults(), RouteSelector.Assignment.NONE, NONE_THROTTLED))
                .isInstanceOf(RouteSelector.Direct.class);
        assertThat(pick("s", EgressSettings.defaults(), new RouteSelector.Assignment(A, false), NONE_THROTTLED))
                .isInstanceOf(RouteSelector.Direct.class);
        assertThat(pick("s", new EgressSettings(EgressSettings.DIRECT, B), new RouteSelector.Assignment(null, true),
                NONE_THROTTLED)).isInstanceOf(RouteSelector.Direct.class);
    }

    @Test
    @DisplayName("round-robin walks the pool per source, one route per pull")
    void roundRobin() {
        EgressSettings settings = policy(EgressSettings.ROUND_ROBIN);
        assertThat(pick("s", settings, null, NONE_THROTTLED)).isEqualTo(new RouteSelector.Routed(A));
        assertThat(pick("s", settings, null, NONE_THROTTLED)).isEqualTo(new RouteSelector.Routed(B));
        assertThat(pick("s", settings, null, NONE_THROTTLED)).isEqualTo(new RouteSelector.Routed(C));
        assertThat(pick("s", settings, null, NONE_THROTTLED)).isEqualTo(new RouteSelector.Routed(A));
        assertThat(pick("other", settings, null, NONE_THROTTLED))
                .as("each source has its own turn").isEqualTo(new RouteSelector.Routed(A));
    }

    @Test
    @DisplayName("within one pull the route chosen first is kept while it stays open")
    void keepsTheCurrentRoute() {
        EgressSettings settings = policy(EgressSettings.ROUND_ROBIN);
        assertThat(selector.select("s", settings, POOL, null, B, NONE_THROTTLED))
                .isEqualTo(new RouteSelector.Routed(B));
        assertThat(selector.select("s", settings, POOL, null, B, id -> id.equals(B) ? LATER : null))
                .as("a throttled current route is left").isEqualTo(new RouteSelector.Routed(A));
    }

    @Test
    @DisplayName("random picks among the open routes with the random source")
    void random() {
        assertThat(pick("s", policy(EgressSettings.RANDOM), null, NONE_THROTTLED))
                .isEqualTo(new RouteSelector.Routed(C));
        assertThat(pick("s", policy(EgressSettings.RANDOM), null, id -> id.equals(C) ? LATER : null))
                .isEqualTo(new RouteSelector.Routed(B));
    }

    @Test
    @DisplayName("per-source-sticky always gives a source the same route, and waits rather than moving it")
    void sticky() {
        EgressSettings settings = policy(EgressSettings.PER_SOURCE_STICKY);
        UUID mine = POOL.get(Math.floorMod("usgs-earthquakes".hashCode(), POOL.size()));
        for (int i = 0; i < 3; i++) {
            assertThat(pick("usgs-earthquakes", settings, null, NONE_THROTTLED))
                    .isEqualTo(new RouteSelector.Routed(mine));
        }
        assertThat(pick("usgs-earthquakes", settings, null, id -> id.equals(mine) ? LATER : null))
                .isEqualTo(new RouteSelector.Wait(LATER));
    }

    @Test
    @DisplayName("rotate-on-rate-limit stays on a route until it throttles the source, then moves to the next open one")
    void rotateOnRateLimitMovesOffAThrottledRoute() {
        EgressSettings settings = policy(EgressSettings.ROTATE_ON_RATE_LIMIT);
        Map<UUID, Instant> throttled = new HashMap<>();
        assertThat(pick("s", settings, null, throttled::get)).isEqualTo(new RouteSelector.Routed(A));
        assertThat(pick("s", settings, null, throttled::get)).isEqualTo(new RouteSelector.Routed(A));

        throttled.put(A, LATER);
        assertThat(pick("s", settings, null, throttled::get)).isEqualTo(new RouteSelector.Routed(B));
        assertThat(pick("other", settings, null, id -> null))
                .as("another source is not moved by this source's throttle").isEqualTo(new RouteSelector.Routed(A));

        throttled.remove(A);
        assertThat(pick("s", settings, null, throttled::get))
                .as("the source stays on the route it moved to").isEqualTo(new RouteSelector.Routed(B));
        throttled.put(B, LATER);
        assertThat(pick("s", settings, null, throttled::get)).isEqualTo(new RouteSelector.Routed(C));
    }

    @Test
    @DisplayName("when every route has throttled the source it waits for the earliest, and is never sent direct")
    void waitsForTheEarliestThrottle() {
        Instant earliest = LATER.minusSeconds(60);
        Map<UUID, Instant> throttled = Map.of(A, LATER, B, earliest, C, LATER);
        for (String rotation : List.of(EgressSettings.ROUND_ROBIN, EgressSettings.RANDOM,
                EgressSettings.ROTATE_ON_RATE_LIMIT)) {
            assertThat(pick("s", policy(rotation), null, throttled::get))
                    .as(rotation).isEqualTo(new RouteSelector.Wait(earliest));
        }
    }

    @Test
    @DisplayName("a pin is honored, waits out its own throttle, and fails rather than moving when the route is unusable")
    void pin() {
        EgressSettings settings = policy(EgressSettings.ROUND_ROBIN);
        RouteSelector.Assignment pinned = new RouteSelector.Assignment(C, false);
        assertThat(pick("s", settings, pinned, NONE_THROTTLED)).isEqualTo(new RouteSelector.Routed(C));
        assertThat(pick("s", settings, pinned, id -> id.equals(C) ? LATER : null))
                .isEqualTo(new RouteSelector.Wait(LATER));
        assertThat(selector.select("s", settings, List.of(A, B), pinned, null, NONE_THROTTLED))
                .isInstanceOf(RouteSelector.Unavailable.class);
    }

    @Test
    @DisplayName("an excluded source uses the default route, or goes direct when there is none")
    void exclusion() {
        RouteSelector.Assignment excluded = new RouteSelector.Assignment(null, true);
        assertThat(pick("s", new EgressSettings(EgressSettings.ROUND_ROBIN, B), excluded, NONE_THROTTLED))
                .isEqualTo(new RouteSelector.Routed(B));
        assertThat(pick("s", new EgressSettings(EgressSettings.ROUND_ROBIN, B), excluded, NONE_THROTTLED))
                .as("never rotated").isEqualTo(new RouteSelector.Routed(B));
        assertThat(pick("s", policy(EgressSettings.ROUND_ROBIN), excluded, NONE_THROTTLED))
                .isInstanceOf(RouteSelector.Direct.class);
    }

    @Test
    @DisplayName("with rotation on and no usable route the pull fails closed")
    void failsClosed() {
        for (String rotation : List.of(EgressSettings.ROUND_ROBIN, EgressSettings.RANDOM,
                EgressSettings.PER_SOURCE_STICKY, EgressSettings.ROTATE_ON_RATE_LIMIT)) {
            assertThat(selector.select("s", policy(rotation), List.of(), null, null, NONE_THROTTLED))
                    .as(rotation).isInstanceOf(RouteSelector.Unavailable.class);
        }
    }

    @Test
    @DisplayName("reset forgets the round-robin turns and the rate-limit choices")
    void reset() {
        EgressSettings settings = policy(EgressSettings.ROUND_ROBIN);
        pick("s", settings, null, NONE_THROTTLED);
        selector.reset();
        assertThat(pick("s", settings, null, NONE_THROTTLED)).isEqualTo(new RouteSelector.Routed(A));
    }
}
