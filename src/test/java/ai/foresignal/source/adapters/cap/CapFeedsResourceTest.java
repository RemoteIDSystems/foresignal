package ai.foresignal.source.adapters.cap;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The committed CAP feed list (prompt 089, {@link CapFeeds#RESOURCE}): every entry is HTTPS, carries a licence,
 * a terms URL and a review date, and the ids are unique and valid.
 */
class CapFeedsResourceTest {

    @Test
    @DisplayName("the committed list loads, validates, and covers several WMO regions")
    void valid() {
        List<CapFeed> feeds = CapFeeds.load();
        assertThat(feeds).hasSizeGreaterThanOrEqualTo(10);
        assertThat(CapFeeds.problems(feeds)).isEmpty();
        assertThat(feeds).extracting(CapFeed::country).contains("DE", "FI", "NO", "IS", "NZ");
    }

    @Test
    @DisplayName("every entry has HTTPS, a licence, a terms URL and a review date")
    void required() {
        for (CapFeed feed : CapFeeds.load()) {
            assertThat(feed.feedUrl().getScheme()).as(feed.id()).isEqualTo("https");
            assertThat(feed.licence()).as(feed.id()).isNotBlank();
            assertThat(feed.termsUrl()).as(feed.id()).isNotNull();
            assertThat(feed.reviewedOn()).as(feed.id()).isNotBlank();
            assertThat(feed.languages()).as(feed.id()).isNotEmpty();
            assertThat(CapFeeds.FORMATS).as(feed.id()).contains(feed.format());
        }
    }

    @Test
    @DisplayName("the reviewed date is recorded, and a bad entry is refused")
    void problems() {
        assertThat(CapFeeds.reviewedOn()).isNotBlank();
        CapFeed bad = new CapFeed("Cap Bad", "de", "X", URI.create("http://x.example/f"), "nope", List.of(),
                null, null, null, null);
        assertThat(CapFeeds.problems(List.of(bad))).isNotEmpty();
    }
}
