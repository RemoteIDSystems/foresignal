package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link TokenService} against the database (prompt 023). */
@IntegrationTest
@Import({MutableClock.TestConfig.class, TokenServiceIT.Events.class})
class TokenServiceIT {

    private static final Instant T0 = Instant.parse("2026-10-12T10:00:00Z");

    @Autowired private TokenService tokens;
    @Autowired private ApiTokenRepository repository;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private TokenHasher hasher;
    @Autowired private MutableClock clock;
    @Autowired private JsonMapper json;
    @Autowired private Events events;

    /** Collects revocation events (the configuration class is itself the listener bean). */
    @TestConfiguration
    static class Events {

        final List<ApiTokenRevokedEvent> revoked = new java.util.concurrent.CopyOnWriteArrayList<>();

        @EventListener
        void on(ApiTokenRevokedEvent event) {
            revoked.add(event);
        }
    }

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        events.revoked.clear();
    }

    @AfterEach
    void tearDown() {
        clock.reset();
    }

    private JsonNode body(String text) {
        return json.readTree(text);
    }

    @Test
    @DisplayName("create returns fsr_ + 43 base64url characters once and stores only the hash and prefix")
    void createStoresOnlyTheHash() {
        TokenService.CreatedToken created = tokens.create(body("{\"name\":\"agent one\",\"scope\":\"agent\"}"));

        assertThat(created.plaintext()).matches("^fsr_[A-Za-z0-9_-]{43}$");
        ApiTokenEntity row = repository.findById(created.entity().getId()).orElseThrow();
        assertThat(row.getTokenHash()).isEqualTo(hasher.hash(created.plaintext()));
        assertThat(row.getTokenPrefix()).isEqualTo(created.plaintext().substring(0, 8));
        assertThat(row.getScope()).isEqualTo(Scope.AGENT);
        assertThat(row.getCreatedVia()).isEqualTo("api");
        assertThat(row.getCreatedAt()).isEqualTo(T0);
        assertThat(created.principalHandle()).isEqualTo("agent-one");
        assertThat(row.getPrincipalId()).hasSize(26);
    }

    @Test
    @DisplayName("authenticate accepts the token and returns its id, name, scope and restrictions")
    void authenticateAcceptsTheToken() {
        TokenService.CreatedToken created = TestTokens.create(tokens, "reader", Scope.READ,
                new TokenRestrictions(null, null, null, List.of("GET /v1/records/**")), null);

        AuthenticatedToken token = tokens.authenticate(created.plaintext()).orElseThrow();

        assertThat(token.id()).isEqualTo(created.entity().getId());
        assertThat(token.name()).isEqualTo("reader");
        assertThat(token.scope()).isEqualTo(Scope.READ);
        assertThat(token.restrictions().endpoints()).containsExactly("GET /v1/records/**");
        assertThat(token.bootstrap()).isFalse();
    }

    @Test
    @DisplayName("unknown, malformed, revoked and expired tokens are refused")
    void refusesInvalidTokens() {
        TokenService.CreatedToken revoked = TestTokens.create(tokens, "gone", Scope.READ);
        TokenService.CreatedToken expiring = TestTokens.create(tokens, "brief", Scope.READ, TokenRestrictions.NONE,
                T0.plus(Duration.ofHours(1)));
        tokens.revoke(revoked.entity().getId());

        assertThat(tokens.authenticate(revoked.plaintext())).isEmpty();
        assertThat(tokens.authenticate(expiring.plaintext())).isPresent();
        clock.advance(Duration.ofHours(1));
        assertThat(tokens.authenticate(expiring.plaintext())).isEmpty();
        assertThat(tokens.authenticate("fsr_unknown")).isEmpty();
        assertThat(tokens.authenticate("not-a-token")).isEmpty();
        assertThat(tokens.authenticate("")).isEmpty();
        assertThat(tokens.authenticate(null)).isEmpty();
    }

    @Test
    @DisplayName("revoking publishes ApiTokenRevokedEvent after commit; an unknown id is 404")
    void revokePublishesTheEvent() {
        TokenService.CreatedToken created = TestTokens.create(tokens, "phone", Scope.READ);

        tokens.revoke(created.entity().getId());

        assertThat(events.revoked).containsExactly(new ApiTokenRevokedEvent(created.entity().getId()));
        assertThat(repository.findById(created.entity().getId()).orElseThrow().getRevokedAt()).isEqualTo(T0);
        assertThatThrownBy(() -> tokens.revoke(UUID.randomUUID())).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("the bootstrap token cannot be revoked: 409 BOOTSTRAP_TOKEN")
    void bootstrapCannotBeRevoked() {
        adminTokens.rotate();
        UUID bootstrap = adminTokens.bootstrapTokenId().orElseThrow();

        assertThatThrownBy(() -> tokens.revoke(bootstrap))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((ConflictException) e).getCode()).isEqualTo(ErrorCodes.BOOTSTRAP_TOKEN));
        assertThat(events.revoked).isEmpty();
    }

    @Test
    @DisplayName("the bootstrap token authenticates as admin and is marked bootstrap")
    void bootstrapAuthenticates() {
        String token = adminTokens.rotate();

        AuthenticatedToken authenticated = tokens.authenticate(token).orElseThrow();

        assertThat(authenticated.scope()).isEqualTo(Scope.ADMIN);
        assertThat(authenticated.bootstrap()).isTrue();
        assertThat(authenticated.name()).isEqualTo(ApiTokenEntity.BOOTSTRAP_NAME);
    }

    @Test
    @DisplayName("last_used_at is written at most once a minute per token, asynchronously")
    void lastUsedIsThrottled() {
        TokenService.CreatedToken created = TestTokens.create(tokens, "busy", Scope.READ);
        UUID id = created.entity().getId();

        tokens.authenticate(created.plaintext());
        await().atMost(Duration.ofSeconds(5)).until(() -> T0.equals(repository.findById(id).orElseThrow().getLastUsedAt()));

        clock.advance(Duration.ofSeconds(30));
        assertThat(tokens.recordUse(id)).isFalse();
        clock.advance(Duration.ofSeconds(30));
        assertThat(tokens.recordUse(id)).isTrue();
        Instant later = T0.plusSeconds(60);
        await().atMost(Duration.ofSeconds(5)).until(() -> later.equals(repository.findById(id).orElseThrow().getLastUsedAt()));
    }

    @Test
    @DisplayName("restriction validation: unknown sources and kinds, bad bboxes and unreachable endpoints are listed")
    void restrictionValidation() {
        assertThatThrownBy(() -> tokens.create(body("""
                {"name":"r","scope":"read","restrictions":{
                  "sources":["no-such-source"],"kinds":["no-such-kind"],
                  "bbox":[10,20,5,30],"endpoints":["GET /v1/tokens","FETCH /v1/x"]}}
                """)))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .extracting(f -> f.field())
                        .contains("restrictions.sources", "restrictions.kinds", "restrictions.bbox",
                                "restrictions.endpoints"));
        assertThatThrownBy(() -> tokens.create(body(
                "{\"name\":\"r\",\"scope\":\"read\",\"restrictions\":{\"bbox\":[-200,0,10,10]}}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> tokens.create(body(
                "{\"name\":\"r\",\"scope\":\"read\",\"restrictions\":{\"area\":{}}}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("valid restrictions are stored and read back, the bbox as a polygon")
    void validRestrictionsRoundTrip() {
        TokenService.CreatedToken created = tokens.create(body("""
                {"name":"kiosk","scope":"read","restrictions":{
                  "bbox":[-10.5,40,5,52.25],"endpoints":["GET /v1/records/**","* /v1/tiles/**"]}}
                """));

        TokenRestrictions stored = repository.findById(created.entity().getId()).orElseThrow().getRestrictions();

        assertThat(stored.bbox()).containsExactly(-10.5, 40.0, 5.0, 52.25);
        assertThat(stored.endpoints()).containsExactly("GET /v1/records/**", "* /v1/tiles/**");
        assertThat(stored.sources()).isNull();
        assertThat(stored.hasDataRestrictions()).isTrue();
    }

    @Test
    @DisplayName("an endpoint pattern must be reachable by the token's scope")
    void endpointPatternsFollowScope() {
        assertThatThrownBy(() -> tokens.create(body(
                "{\"name\":\"r\",\"scope\":\"read\",\"restrictions\":{\"endpoints\":[\"POST /v1/triggers\"]}}")))
                .isInstanceOf(ValidationException.class);
        assertThat(tokens.create(body(
                "{\"name\":\"a\",\"scope\":\"agent\",\"restrictions\":{\"endpoints\":[\"POST /v1/triggers\"]}}")))
                .isNotNull();
    }

    @Test
    @DisplayName("input validation: name, scope, expiresAt in the future, unknown members, later members")
    void inputValidation() {
        for (String bad : List.of(
                "{\"scope\":\"read\"}",
                "{\"name\":\"x\",\"scope\":\"owner\"}",
                "{\"name\":\"x\",\"scope\":\"read\",\"expiresAt\":\"2020-01-01T00:00:00Z\"}",
                "{\"name\":\"x\",\"scope\":\"read\",\"expiresAt\":\"tomorrow\"}",
                "{\"name\":\"x\",\"scope\":\"read\",\"color\":\"red\"}",
                "{\"name\":\"x\",\"scope\":\"read\",\"privateAccess\":true}",
                "{\"name\":\"x\",\"scope\":\"read\",\"principalId\":\"nobody-here\"}")) {
            assertThatThrownBy(() -> tokens.create(body(bad))).as(bad).isInstanceOf(ValidationException.class);
        }
    }

    @Test
    @DisplayName("a token can name an existing principal by handle")
    void namesAPrincipal() {
        TokenService.CreatedToken first = TestTokens.create(tokens, "laptop", Scope.READ);

        TokenService.CreatedToken second = tokens.create(body("{\"name\":\"phone\",\"scope\":\"read\",\"principalId\":\""
                + first.principalHandle() + "\"}"));

        assertThat(second.entity().getPrincipalId()).isEqualTo(first.entity().getPrincipalId());
    }

    @Test
    @DisplayName("device metadata is validated, stored and stamped")
    void deviceMetadata() {
        TokenService.CreatedToken created = TestTokens.create(tokens, "phone", Scope.READ);

        ApiTokenEntity updated = tokens.updateDevice(created.entity().getId(),
                body("{\"deviceName\":\"Pixel\",\"platform\":\"android\",\"appVersion\":\"1.2\"}"));

        assertThat(updated.getDevice()).isEqualTo(new DeviceInfo("Pixel", "android", "1.2", T0));
        assertThatThrownBy(() -> tokens.updateDevice(created.entity().getId(),
                body("{\"deviceName\":\"Pixel\",\"platform\":\"symbian\"}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("current() re-checks a token: revoked and expired tokens are gone")
    void currentRechecks() {
        TokenService.CreatedToken created = TestTokens.create(tokens, "t", Scope.READ, TokenRestrictions.NONE,
                T0.plusSeconds(10));
        assertThat(tokens.current(created.entity().getId())).isPresent();
        clock.advanceSeconds(10);
        assertThat(tokens.current(created.entity().getId())).isEmpty();
    }
}
