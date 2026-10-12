package ai.foresignal.security.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenRevokedEvent;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link TicketStore}'s windows and rules, on a {@link MutableClock} (prompt 023). */
class TicketStoreTest {

    private static final Instant T0 = Instant.parse("2026-10-12T00:00:00Z");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final MutableClock clock = new MutableClock(T0);
    private final TokenService tokens = mock(TokenService.class);
    private final AuthenticatedToken parent = new AuthenticatedToken(UUID.randomUUID(), "browser", Scope.READ,
            TokenRestrictions.NONE, null, "01JA8Q3V7S2K9W4T6Y1B5N0M3C", false);
    private TicketStore store;

    @BeforeEach
    void setUp() {
        when(tokens.current(any())).thenReturn(Optional.of(parent));
        store = new TicketStore(new TokenHasher(), tokens, clock);
    }

    private static JsonNode body(String text) {
        return JSON.readTree(text);
    }

    private TicketAcceptance stream(String ticket) {
        return store.accept(ticket, HttpMethod.GET, "/v1/changes/stream");
    }

    private Ticket accepted(TicketAcceptance acceptance) {
        assertThat(acceptance).isInstanceOf(TicketAcceptance.Accepted.class);
        return ((TicketAcceptance.Accepted) acceptance).ticket();
    }

    @Test
    @DisplayName("a stream ticket is fst_ + 24 bytes; its expiresAt is the first-connect deadline")
    void mintStream() {
        TicketStore.MintedTicket minted = store.mint(parent, body("{\"purpose\":\"stream\"}"));

        assertThat(minted.ticket()).matches("^fst_[A-Za-z0-9_-]{32}$");
        assertThat(minted.purpose()).isEqualTo(TicketPurpose.STREAM);
        assertThat(minted.expiresAt()).isEqualTo(T0.plus(TicketStore.FIRST_CONNECT_WINDOW));
        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("first connect within 60 s, then reconnects within 5 min of the last disconnect")
    void streamWindows() {
        String ticket = store.mint(parent, body("{\"purpose\":\"stream\"}")).ticket();
        clock.advance(Duration.ofSeconds(60));
        Ticket claimed = accepted(stream(ticket));
        assertThat(claimed.parent()).isEqualTo(parent);
        assertThat(store.inUse(ticket)).isTrue();

        assertThat(stream(ticket)).isInstanceOf(TicketAcceptance.Busy.class);

        clock.advance(Duration.ofHours(1));
        store.release(claimed);
        assertThat(store.inUse(ticket)).isFalse();
        clock.advance(Duration.ofMinutes(5));
        Ticket again = accepted(stream(ticket));
        store.release(again);
        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        assertThat(stream(ticket)).isInstanceOf(TicketAcceptance.Rejected.class);
    }

    @Test
    @DisplayName("a stream ticket never connected after 60 s is rejected")
    void firstConnectDeadline() {
        String ticket = store.mint(parent, body("{\"purpose\":\"stream\"}")).ticket();
        clock.advance(Duration.ofSeconds(61));

        assertThat(stream(ticket)).isInstanceOf(TicketAcceptance.Rejected.class);
        assertThat(store.size()).isZero();
    }

    @Test
    @DisplayName("24 h is the hard limit, even with reconnects every few minutes")
    void streamLifetime() {
        String ticket = store.mint(parent, body("{\"purpose\":\"stream\"}")).ticket();
        for (int i = 0; i < 24 * 15; i++) {
            Ticket claimed = accepted(stream(ticket));
            clock.advance(Duration.ofMinutes(2));
            store.release(claimed);
            clock.advance(Duration.ofMinutes(2));
        }
        assertThat(clock.instant()).isAfterOrEqualTo(T0.plus(TicketStore.STREAM_LIFETIME));
        assertThat(stream(ticket)).isInstanceOf(TicketAcceptance.Rejected.class);
    }

    @Test
    @DisplayName("tile tickets are reusable until their ttl and default to 10 min")
    void tileTickets() {
        TicketStore.MintedTicket minted = store.mint(parent, body("{\"purpose\":\"tiles\"}"));
        assertThat(minted.expiresAt()).isEqualTo(T0.plus(TicketStore.DEFAULT_TILE_TTL));
        for (int i = 0; i < 5; i++) {
            accepted(store.accept(minted.ticket(), HttpMethod.GET, "/v1/tiles/earthquake/1/2/3.mvt"));
        }
        accepted(store.accept(minted.ticket(), HttpMethod.GET, "/v1/tiles.json"));
        clock.advance(Duration.ofMinutes(10));
        assertThat(store.accept(minted.ticket(), HttpMethod.GET, "/v1/tiles.json"))
                .isInstanceOf(TicketAcceptance.Rejected.class);
    }

    @Test
    @DisplayName("purpose, method and narrowed paths are enforced")
    void pathsAndPurpose() {
        String stream = store.mint(parent, body("{\"purpose\":\"stream\",\"paths\":[\"/v1/alerts/stream\"]}")).ticket();
        String tiles = store.mint(parent, body("{\"purpose\":\"tiles\"}")).ticket();

        assertThat(store.accept(stream, HttpMethod.GET, "/v1/changes/stream")).isInstanceOf(TicketAcceptance.Rejected.class);
        assertThat(store.accept(stream, HttpMethod.GET, "/v1/tiles.json")).isInstanceOf(TicketAcceptance.Rejected.class);
        assertThat(store.accept(tiles, HttpMethod.GET, "/v1/changes/stream")).isInstanceOf(TicketAcceptance.Rejected.class);
        assertThat(store.accept(tiles, HttpMethod.POST, "/v1/tiles.json")).isInstanceOf(TicketAcceptance.Rejected.class);
        assertThat(store.accept(tiles, HttpMethod.GET, "/v1/records")).isInstanceOf(TicketAcceptance.Rejected.class);
        accepted(store.accept(stream, HttpMethod.GET, "/v1/alerts/stream"));
        assertThat(store.accept("fst_unknown", HttpMethod.GET, "/v1/tiles.json")).isInstanceOf(TicketAcceptance.Rejected.class);
        assertThat(store.accept("fsr_token", HttpMethod.GET, "/v1/tiles.json")).isInstanceOf(TicketAcceptance.Rejected.class);
        assertThat(store.coversPath(HttpMethod.GET, "/v1/events/stream")).isTrue();
        assertThat(store.coversPath(HttpMethod.GET, "/v1/records")).isFalse();
    }

    @Test
    @DisplayName("a revoked parent drops its tickets; an invalid parent rejects them")
    void revocation() {
        String ticket = store.mint(parent, body("{\"purpose\":\"tiles\"}")).ticket();
        store.onTokenRevoked(new ApiTokenRevokedEvent(parent.id()));
        assertThat(store.size()).isZero();
        assertThat(store.accept(ticket, HttpMethod.GET, "/v1/tiles.json")).isInstanceOf(TicketAcceptance.Rejected.class);

        String other = store.mint(parent, body("{\"purpose\":\"tiles\"}")).ticket();
        when(tokens.current(parent.id())).thenReturn(Optional.empty());
        assertThat(store.accept(other, HttpMethod.GET, "/v1/tiles.json")).isInstanceOf(TicketAcceptance.Rejected.class);
    }

    @Test
    @DisplayName("invalid requests are listed in one 400")
    void validation() {
        for (String bad : new String[] {"[]", "{}", "{\"purpose\":\"x\"}", "{\"purpose\":\"stream\",\"ttl\":\"1m\"}",
            "{\"purpose\":\"tiles\",\"ttl\":\"61m\"}", "{\"purpose\":\"tiles\",\"ttl\":5}", "{\"purpose\":\"tiles\",\"ttl\":\"0s\"}",
            "{\"purpose\":\"tiles\",\"paths\":\"/v1/tiles.json\"}", "{\"purpose\":\"tiles\",\"paths\":[\"tiles\"]}",
            "{\"purpose\":\"stream\",\"paths\":[\"/v1/records\"]}", "{\"purpose\":\"tiles\",\"extra\":1}"}) {
            assertThatThrownBy(() -> store.mint(parent, body(bad))).as(bad).isInstanceOf(ValidationException.class);
        }
        assertThat(store.mint(parent, body("{\"purpose\":\"tiles\",\"ttl\":\"1h\"}")).expiresAt())
                .isEqualTo(T0.plus(Duration.ofHours(1)));
    }

    @Test
    @DisplayName("expired tickets are pruned when minting; clear forgets everything")
    void pruneAndClear() {
        store.mint(parent, body("{\"purpose\":\"tiles\",\"ttl\":\"1m\"}"));
        clock.advance(Duration.ofMinutes(2));
        store.mint(parent, body("{\"purpose\":\"tiles\"}"));
        assertThat(store.size()).isEqualTo(1);
        store.clear();
        assertThat(store.size()).isZero();
    }
}
