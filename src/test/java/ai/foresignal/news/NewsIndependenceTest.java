package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.news.NewsArticles;
import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedItem;
import ai.foresignal.source.kit.feed.NewsArticleItemMapper;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * NEWS-2 (prompt 133): reprints of one originator count as one evidence group, near-identical titles of unknown origin
 * collapse too, independent outlets stay separate, and no author name is ever stored.
 */
class NewsIndependenceTest {

    private static NewsGrouper.Member member(String key, String outlet, String wire, List<String> aggregates,
            String title, long seq) {
        return new NewsGrouper.Member(key, outlet, outlet + ".example", wire, aggregates, TitleTokens.tokens(title),
                seq);
    }

    @Test
    @DisplayName("a wire story and 30 reprints are one evidence group; the reprints are syndicated")
    void wireReprints() {
        String title = "Iran seizes oil tanker in the Strait of Hormuz, US Navy says";
        List<NewsGrouper.Member> members = new ArrayList<>();
        members.add(member("ap-1", "ap-outlet", "ap", List.of(), title, 1));
        for (int i = 0; i < 30; i++) {
            members.add(member("r" + i, "outlet-" + i, null, List.of(), title + (i % 2 == 0 ? "" : " -"), 2 + i));
        }
        members.add(member("own", "independent", null, List.of(), "Analysis: what the Hormuz seizure means for oil",
                40));
        List<NewsGrouper.Group> groups = NewsGrouper.group(members, 0.9);
        assertThat(groups).hasSize(2);
        NewsGrouper.Group wire = groups.get(0);
        assertThat(wire.root()).isEqualTo("wire:ap");
        assertThat(wire.members()).hasSize(31);
        assertThat(wire.syndicated()).hasSize(30).doesNotContain("ap-1");
        assertThat(wire.wireOriginator()).isEqualTo("ap");
        assertThat(groups.get(1).root()).isEqualTo("outlet:independent");
    }

    @Test
    @DisplayName("an outlet that names other wires in aggregates keeps its own group")
    void explicitAggregates() {
        String title = "Iran seizes oil tanker in the Strait of Hormuz, US Navy says";
        List<NewsGrouper.Group> groups = NewsGrouper.group(List.of(
                member("ap-1", "ap-outlet", "ap", List.of(), title, 1),
                member("x", "reuters-only", null, List.of("reuters"), title, 2)), 0.9);
        assertThat(groups).extracting(NewsGrouper.Group::root).containsExactly("wire:ap", "outlet:reuters-only");
    }

    @Test
    @DisplayName("near-identical reprints without the wire's own article are one group of unknown origin")
    void unknownOrigin() {
        String title = "Ceasefire talks resume in Doha after weekend strikes";
        List<NewsGrouper.Group> groups = NewsGrouper.group(List.of(member("a", "o1", null, List.of(), title, 1),
                member("b", "o2", null, List.of(), title, 2), member("c", "o3", null, List.of(), title, 3)), 0.9);
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).root()).isEqualTo("syndicated:a");
        assertThat(groups.get(0).syndicated()).containsExactlyInAnyOrder("b", "c");
    }

    @Test
    @DisplayName("several articles of one outlet are one group; different titles from different outlets are not merged")
    void sameOutlet() {
        List<NewsGrouper.Group> groups = NewsGrouper.group(List.of(
                member("a", "o1", null, List.of(), "Port closed after storm", 1),
                member("b", "o1", null, List.of(), "Port reopens partially", 2),
                member("c", "o2", null, List.of(), "Shipping delays mount at the port", 3)), 0.9);
        assertThat(groups).extracting(NewsGrouper.Group::root).containsExactly("outlet:o1", "outlet:o2");
        assertThat(groups.get(0).members()).containsExactly("a", "b");
        assertThat(groups.get(0).syndicated()).isEmpty();
    }

    @Test
    @DisplayName("an outlet feed's authors are dropped: news-article payloads never carry bylines")
    void noAuthors() {
        FeedItem item = new FeedItem("id-1", "Tanker seized near Hormuz", "Summary text", "https://example.com/a?utm_x=1",
                Instant.parse("2026-10-16T10:00:00Z"), null, List.of("Jane Reporter"), List.of(), List.of(), "en",
                null);
        FeedDocument doc = new FeedDocument("rss2", "Example", "https://example.com", null, List.of(), "en",
                List.of(item), List.of());
        NormalizedRecord standard = NormalizedRecord.builder("id-1", "bulletin").observedAt(item.published())
                .put("authors", item.authors()).build();
        NormalizedRecord mapped = new NewsArticleItemMapper().map(item, doc, null, standard);
        assertThat(mapped).isNotNull();
        assertThat(mapped.kind()).isEqualTo(NewsArticles.KIND);
        assertThat(mapped.payload()).doesNotContainKeys("authors", "author", "byline");
        assertThat(mapped.payload().keySet()).isSubsetOf(NewsArticles.KEYS);
        assertThat(mapped.payload().toString()).doesNotContain("Jane Reporter");
    }

    @Test
    @DisplayName("the news-feed-* family: ids accepted, the reviewed news-article mapper registered, sources recognized")
    void feedFamily() {
        assertThat(ai.foresignal.source.kit.feed.FeedSpecValidator.ID_PATTERN.matcher("news-feed-example-daily")
                .matches()).isTrue();
        assertThat(ai.foresignal.source.kit.feed.FeedItemMappers.of("news-article")).isNotNull();
        assertThat(NewsArticles.isNewsSource("news-feed-example-daily")).isTrue();
        assertThat(NewsArticles.isNewsSource("op-news-feed-local")).isTrue();
        assertThat(NewsArticles.isNewsSource("gdelt-articles")).isTrue();
        assertThat(NewsArticles.isNewsSource("feed-who-news")).isFalse();
        assertThat(NewsCoverageService.newsKey("record:gdelt-articles/abc123@77")).isEqualTo("gdelt-articles/abc123");
        assertThat(NewsCoverageService.newsKey("record:usgs-earthquakes/us7000@5")).isNull();
    }
}
