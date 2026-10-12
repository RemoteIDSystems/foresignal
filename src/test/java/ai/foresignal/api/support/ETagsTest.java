package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

class ETagsTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:10Z");

    @Test
    void recordTagIsVersionAndObservedMillis() {
        assertThat(ETags.forRecord(3, Instant.ofEpochMilli(1234567))).isEqualTo("\"3.1234567\"");
    }

    @Test
    void listTagChangesWithEveryInput() {
        String base = ETags.forList("q", 1, 2, NOW);
        assertThat(base).startsWith("\"l-").endsWith("\"");
        assertThat(ETags.forList("q", 1, 2, NOW.plusSeconds(30))).isEqualTo(base);
        assertThat(ETags.forList("q", 1, 2, NOW.plusSeconds(60))).isNotEqualTo(base);
        assertThat(ETags.forList("r", 1, 2, NOW)).isNotEqualTo(base);
        assertThat(ETags.forList("q", 2, 2, NOW)).isNotEqualTo(base);
        assertThat(ETags.forList("q", 1, 3, NOW)).isNotEqualTo(base);
    }

    @Test
    void documentTag() {
        assertThat(ETags.forDocument("{}")).isEqualTo(ETags.forDocument("{}")).isNotEqualTo(ETags.forDocument("[]"));
    }

    @Test
    void ifNoneMatchUsesWeakComparison() {
        assertThat(ETags.matches("\"a\"", "\"a\"")).isTrue();
        assertThat(ETags.matches("W/\"a\"", "\"a\"")).isTrue();
        assertThat(ETags.matches("\"b\", \"a\"", "\"a\"")).isTrue();
        assertThat(ETags.matches("*", "\"a\"")).isTrue();
        assertThat(ETags.matches("\"b\"", "\"a\"")).isFalse();
        assertThat(ETags.matches(null, "\"a\"")).isFalse();
        assertThat(ETags.matches("\"a\"", null)).isFalse();
    }

    @Test
    void cacheHeaders() {
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), "\"x\"", true);
        assertThat(headers.getETag()).isEqualTo("\"x\"");
        assertThat(headers.getCacheControl()).isEqualTo("public, no-cache");
        assertThat(headers.getFirst(HttpHeaders.VARY)).isEqualTo("Accept, Accept-Encoding, Authorization");
        assertThat(ETags.cacheControl(false)).isEqualTo("private, no-cache");
        ResponseEntity<Object> notModified = ETags.notModified("\"x\"", false);
        assertThat(notModified.getStatusCode().value()).isEqualTo(304);
        assertThat(notModified.getHeaders().getETag()).isEqualTo("\"x\"");
    }
}
