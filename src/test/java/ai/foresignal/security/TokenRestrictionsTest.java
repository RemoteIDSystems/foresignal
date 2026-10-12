package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@link TokenRestrictions} (prompt 023). */
class TokenRestrictionsTest {

    @Test
    @DisplayName("data and endpoint restrictions are told apart; empty restricts nothing")
    void kinds() {
        assertThat(TokenRestrictions.NONE.isEmpty()).isTrue();
        assertThat(TokenRestrictions.NONE.orNull()).isNull();
        assertThat(TokenRestrictions.orNone(null)).isSameAs(TokenRestrictions.NONE);
        TokenRestrictions endpoints = new TokenRestrictions(null, null, null, List.of("GET /v1/records"));
        assertThat(endpoints.hasEndpointRestrictions()).isTrue();
        assertThat(endpoints.hasDataRestrictions()).isFalse();
        assertThat(new TokenRestrictions(List.of("a"), null, null, null).hasDataRestrictions()).isTrue();
        assertThat(new TokenRestrictions(null, List.of("k"), null, null).hasDataRestrictions()).isTrue();
        assertThat(new TokenRestrictions(null, null, List.of(0.0, 0.0, 1.0, 1.0), null).hasDataRestrictions()).isTrue();
    }

    @Test
    @DisplayName("JSON carries only the four members")
    void json() {
        String text = JsonMapper.builder().build().writeValueAsString(
                new TokenRestrictions(List.of("a"), null, null, List.of("GET /v1/records")));
        assertThat(text).contains("\"sources\"").contains("\"endpoints\"").doesNotContain("empty")
                .doesNotContain("DataRestrictions");
    }
}
