package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.ratelimit.ClientRateLimiter;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * The fairness key of the render queue (prompt 045, extended by 050; Weather-Imagery-Design §6.3):
 * fair queueing is per tile key when one is present, then per token, then per client IP, and the
 * per-client rate limits (prompt 035) use the same key.
 */
class RenderQueueFairnessTest {

    private static MockHttpServletRequest request(RequestPrincipal principal) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/products/x/tiles/latest/1/0/0.png");
        request.setRemoteAddr("192.0.2.10");
        if (principal != null) {
            principal.bind(request);
        }
        return request;
    }

    @Test
    @DisplayName("a tile key queues under its own id, not its address")
    void tileKeyFirst() {
        UUID id = UUID.randomUUID();
        MockHttpServletRequest request = request(new RequestPrincipal(RequestPrincipal.Kind.TILE_KEY, id, "tablet",
                Scope.READ, ReadRestriction.NONE, false));
        assertThat(RasterTilesController.clientKey(request)).isEqualTo("tilekey:" + id);
        assertThat(ClientRateLimiter.clientKey(request)).isEqualTo("tilekey:" + id);
    }

    @Test
    @DisplayName("two tile keys behind one address are two clients")
    void twoKeysOneAddress() {
        String a = RasterTilesController.clientKey(request(new RequestPrincipal(RequestPrincipal.Kind.TILE_KEY,
                UUID.randomUUID(), "a", Scope.READ, ReadRestriction.NONE, false)));
        String b = RasterTilesController.clientKey(request(new RequestPrincipal(RequestPrincipal.Kind.TILE_KEY,
                UUID.randomUUID(), "b", Scope.READ, ReadRestriction.NONE, false)));
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("then the token, then the IP")
    void tokenThenIp() {
        UUID id = UUID.randomUUID();
        assertThat(RasterTilesController.clientKey(request(new RequestPrincipal(RequestPrincipal.Kind.TOKEN, id,
                "reader", Scope.READ, ReadRestriction.NONE)))).isEqualTo("token:" + id);
        assertThat(RasterTilesController.clientKey(request(new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY, id,
                "kiosk", Scope.READ, ReadRestriction.NONE)))).isEqualTo("feed:" + id);
        assertThat(RasterTilesController.clientKey(request(RequestPrincipal.anonymous(ReadRestriction.NONE))))
                .isEqualTo("ip:192.0.2.10");
        assertThat(RasterTilesController.clientKey(request(null))).isEqualTo("ip:192.0.2.10");
    }
}
