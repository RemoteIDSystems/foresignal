package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/** The fail-closed data-restriction default until prompt 035 (prompt 023). */
class FailClosedRestrictionEnforcerTest {

    private final FailClosedRestrictionEnforcer enforcer = new FailClosedRestrictionEnforcer();
    private final AccessPolicy policy = new AccessPolicy();

    private static AuthenticatedToken token(TokenRestrictions restrictions) {
        return new AuthenticatedToken(UUID.randomUUID(), "t", Scope.READ, restrictions, null,
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", false);
    }

    private boolean refused(HttpMethod method, String path, TokenRestrictions restrictions) {
        return enforcer.check(method, path, policy.requirementFor(method, path), token(restrictions)).isPresent();
    }

    @Test
    @DisplayName("data-restricted tokens are refused on data reads with RESTRICTION_NOT_ENFORCED")
    void refusesDataReads() {
        TokenRestrictions boxed = new TokenRestrictions(null, null, List.of(0.0, 0.0, 1.0, 1.0), null);
        for (String path : List.of("/v1/records", "/v1/changes/stream", "/v1/tiles/k/1/2/3.mvt", "/v1/sources",
                "/v1/alerts", "/v1/kinds")) {
            assertThat(refused(HttpMethod.GET, path, boxed)).as(path).isTrue();
        }
        assertThat(refused(HttpMethod.POST, "/v1/ask", boxed)).isTrue();
        assertThat(enforcer.check(HttpMethod.GET, "/v1/records", AccessRequirement.READ, token(boxed)).orElseThrow().code())
                .isEqualTo("RESTRICTION_NOT_ENFORCED");
    }

    @Test
    @DisplayName("writes, admin reads and unrestricted or endpoint-only tokens pass")
    void passesTheRest() {
        TokenRestrictions boxed = new TokenRestrictions(List.of("s"), null, null, null);
        assertThat(refused(HttpMethod.POST, "/v1/stream-tickets", boxed)).isFalse();
        assertThat(refused(HttpMethod.PUT, "/v1/tokens/current/device", boxed)).isFalse();
        assertThat(refused(HttpMethod.GET, "/v1/settings/retention", boxed)).isFalse();
        assertThat(refused(HttpMethod.GET, "/v1/records", TokenRestrictions.NONE)).isFalse();
        assertThat(refused(HttpMethod.GET, "/v1/records",
                new TokenRestrictions(null, null, null, List.of("GET /v1/records")))).isFalse();
    }
}
