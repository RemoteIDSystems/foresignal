package ai.foresignal.source.formats.news;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ArticleUrls}, {@link NewsNames} and {@link NewsArticles} (prompt 133): article identity and metadata. */
class ArticleUrlsTest {

    @Test
    @DisplayName("canonical URLs: lower-case scheme and host, no default port, fragment or tracking parameters")
    void canonical() {
        assertThat(ArticleUrls.canonical("HTTPS://WWW.Example.COM:443/Path/A?utm_source=x&id=7&fbclid=y#top"))
                .isEqualTo("https://www.example.com/Path/A?id=7");
        assertThat(ArticleUrls.canonical("http://example.com")).isEqualTo("http://example.com/");
        assertThat(ArticleUrls.canonical("http://example.com:8080/a")).isEqualTo("http://example.com:8080/a");
        assertThat(ArticleUrls.canonical("ftp://example.com/a")).isNull();
        assertThat(ArticleUrls.canonical("not a url")).isNull();
        assertThat(ArticleUrls.externalId("https://example.com/a?utm_medium=rss"))
                .isEqualTo(ArticleUrls.externalId("https://EXAMPLE.com/a#x")).hasSize(64);
        assertThat(ArticleUrls.domain("https://www.example.co.uk/x")).isEqualTo("example.co.uk");
        assertThat(ArticleUrls.normalizeDomain("WWW.Example.org.")).isEqualTo("example.org");
    }

    @Test
    @DisplayName("country and language names map to ISO codes; unknown names are null")
    void names() {
        assertThat(NewsNames.countryIso2("United States")).isEqualTo("US");
        assertThat(NewsNames.countryIso2("Russia")).isEqualTo("RU");
        assertThat(NewsNames.countryIso2("FRA")).isEqualTo("FR");
        assertThat(NewsNames.countryIso2("gb")).isEqualTo("GB");
        assertThat(NewsNames.countryIso2("Atlantis")).isNull();
        assertThat(NewsNames.languageCode("English")).isEqualTo("en");
        assertThat(NewsNames.languageCode("en-us")).isEqualTo("en");
        assertThat(NewsNames.languageCode("Chinese")).isEqualTo("zh");
        assertThat(NewsNames.languageCode("Klingon")).isNull();
    }

    @Test
    @DisplayName("records: metadata only, summary at most 500 characters, unusable articles refused")
    void record() {
        String longSummary = "x".repeat(800);
        var r = NewsArticles.record(new NewsArticles.Article("Title", "https://example.com/a?utm_x=1", "o1", "en",
                "US", Instant.parse("2026-10-16T00:00:00Z"), null, List.of("t1"), longSummary, "javascript:alert(1)"));
        assertThat(r.payload().get("summary").toString()).hasSize(500);
        assertThat(r.payload()).doesNotContainKey("imageUrl").containsEntry("url", "https://example.com/a");
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-10-16T00:00:00Z"));
        assertThat(NewsArticles.record(new NewsArticles.Article(" ", "https://example.com/a", null, null, null,
                Instant.EPOCH, null, List.of(), null, null))).isNull();
        assertThat(NewsArticles.record(new NewsArticles.Article("T", "mailto:x@example.com", null, null, null,
                Instant.EPOCH, null, List.of(), null, null))).isNull();
    }
}
