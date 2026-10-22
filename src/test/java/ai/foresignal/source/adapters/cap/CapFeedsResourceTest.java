package ai.foresignal.source.adapters.cap;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The committed CAP feed list (prompt 089, {@link CapFeeds#RESOURCE}): every entry is HTTPS, carries a license,
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
    @DisplayName("every entry has HTTPS, a license, a terms URL and a review date")
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
    @DisplayName("ids are cap-<iso2>-<authority>, one source per authority (DWD once, as cap-de-dwd)")
    void idsAndAuthorities() {
        List<CapFeed> feeds = CapFeeds.load();
        for (CapFeed feed : feeds) {
            assertThat(feed.id()).as(feed.id()).startsWith("cap-" + feed.country().toLowerCase(java.util.Locale.ROOT)
                    + "-");
        }
        assertThat(feeds).filteredOn(f -> f.authority().equals("Deutscher Wetterdienst")).extracting(CapFeed::id)
                .containsExactly("cap-de-dwd");
        CapFeed de = feeds.getFirst();
        CapFeed twin = new CapFeed("cap-de-dwd-en", "DE", de.authority(), de.feedUrl(), de.format(), List.of("en"),
                de.licence(), de.termsUrl(), de.reviewedOn(), null);
        assertThat(CapFeeds.problems(List.of(de, twin))).anyMatch(p -> p.contains("one source per authority"));
        CapFeed wrongCountry = new CapFeed("cap-fr-dwd", "DE", "Other", de.feedUrl(), de.format(), List.of("de"),
                de.licence(), de.termsUrl(), de.reviewedOn(), null);
        assertThat(CapFeeds.problems(List.of(wrongCountry))).anyMatch(p -> p.contains("cap-<iso2>-<authority>"));
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
