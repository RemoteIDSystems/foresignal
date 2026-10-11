package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.TokenFilter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Guards the ordering the two filters depend on. */
class FilterOrderTest {

    @Test
    @DisplayName("CORS runs before the token filter")
    void corsRunsFirst() {
        // If this inverts, a cross-origin request that fails authentication loses its CORS headers,
        // and the browser reports an opaque CORS error instead of the 401.
        assertThat(CorsFilter.ORDER).isLessThan(TokenFilter.ORDER);
    }

    @Test
    @DisplayName("a wildcard matches any origin, an empty list matches none")
    void isAllowedSemantics() {
        assertThat(CorsFilter.isAllowed("https://anything.example", List.of("*"))).isTrue();
        assertThat(CorsFilter.isAllowed("https://anything.example", List.of())).isFalse();
    }

    @Test
    @DisplayName("an exact origin match is case-insensitive, as scheme and host are")
    void isAllowedIsCaseInsensitive() {
        assertThat(CorsFilter.isAllowed("HTTP://LOCALHOST:5173", List.of("http://localhost:5173")))
                .isTrue();
    }

    @Test
    void isAllowedRejectsOtherOrigins() {
        assertThat(CorsFilter.isAllowed("https://evil.example", List.of("http://localhost:5173")))
                .isFalse();
    }
}
