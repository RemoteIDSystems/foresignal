package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlRedactorTest {

    private final UrlRedactor redactor =
            new UrlRedactor(List.of("sup3rs3cret", "abc", "shortkey1"));

    @Test
    @DisplayName("a credential in a query value is masked")
    void masksQueryValue() {
        assertThat(redactor.redact("https://api.example/data?key=sup3rs3cret&bbox=1,2,3,4"))
                .isEqualTo("https://api.example/data?key=***&bbox=1,2,3,4");
    }

    @Test
    @DisplayName("a credential in a path segment is masked")
    void masksPathSegment() {
        assertThat(redactor.redact("https://api.example/shortkey1/data.json"))
                .isEqualTo("https://api.example/***/data.json");
    }

    @Test
    @DisplayName("masking is by value, not by parameter name, since upstreams name keys anything")
    void masksByValueNotName() {
        assertThat(redactor.redact("https://api.example/?u=sup3rs3cret")).contains("***");
        assertThat(redactor.redact("https://api.example/?apiKey=harmless"))
                .isEqualTo("https://api.example/?apiKey=harmless");
    }

    @Test
    @DisplayName("a value too short to be a secret is left alone")
    void ignoresVeryShortValues() {
        // Masking "abc" would redact fragments of ordinary URLs.
        assertThat(redactor.redact("https://api.example/abc/path")).contains("abc");
        assertThat(redactor.secretCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("a secret containing another is masked whole")
    void masksLongestFirst() {
        var nested = new UrlRedactor(List.of("abcd", "abcdefgh"));

        assertThat(nested.redact("token=abcdefgh")).isEqualTo("token=***");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Authorization", "authorization", "AUTHORIZATION", "Proxy-Authorization", "Cookie", "Set-Cookie", "X-Foresignal-Ingest-Key"})
    @DisplayName("credential-bearing headers are masked whole, whatever they contain")
    void masksSensitiveHeadersEntirely(String header) {
        assertThat(redactor.redactHeader(header, "Bearer a-token-we-never-stored"))
                .isEqualTo(UrlRedactor.MASK);
        assertThat(UrlRedactor.isAlwaysRedacted(header)).isTrue();
    }

    @Test
    @DisplayName("an ordinary header is masked only where it contains a known secret")
    void masksOtherHeadersByValue() {
        assertThat(redactor.redactHeader("X-Api-Key", "sup3rs3cret")).isEqualTo(UrlRedactor.MASK);
        assertThat(redactor.redactHeader("Accept", "application/json")).isEqualTo("application/json");
    }

    @Test
    @DisplayName("a redactor with no secrets still masks Authorization")
    void noneStillMasksAuthorization() {
        assertThat(UrlRedactor.none().redactHeader("Authorization", "Bearer x"))
                .isEqualTo(UrlRedactor.MASK);
        assertThat(UrlRedactor.none().redact("https://x/?key=value"))
                .isEqualTo("https://x/?key=value");
    }

    @Test
    void handlesUris() {
        assertThat(redactor.redact(URI.create("https://api.example/?key=sup3rs3cret")))
                .isEqualTo("https://api.example/?key=***");
    }

    @Test
    void handlesNullsAndBlanks() {
        assertThat(redactor.redact((String) null)).isNull();
        assertThat(redactor.redact((URI) null)).isEmpty();
        assertThat(redactor.redact("")).isEmpty();
        assertThat(new UrlRedactor(null).secretCount()).isZero();
    }

    @Test
    @DisplayName("every occurrence is masked, not just the first")
    void masksEveryOccurrence() {
        assertThat(redactor.redact("a=sup3rs3cret&b=sup3rs3cret"))
                .isEqualTo("a=***&b=***");
    }
}
