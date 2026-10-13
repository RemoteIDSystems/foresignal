package ai.foresignal.feed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** JSON Feed 1.1 with the {@code _foresignal} extension (prompt 058), against a golden document. */
class JsonFeedWriterTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** The golden document of {@code FeedFixtures.feed(FeedFixtures.entry(4211, point))}. */
    static final String GOLDEN = """
            {
              "version": "https://jsonfeed.org/version/1.1",
              "title": "Foresignal alerts",
              "home_page_url": "https://fs.example/v1/alerts",
              "feed_url": "https://fs.example/v1/alerts/feed.atom",
              "description": "Alerts, newest first",
              "language": "en",
              "authors": [{"name": "Foresignal"}],
              "items": [{
                "id": "tag:foresignal,2026:INSTALL/alert/4211",
                "url": "https://fs.example/v1/alerts/4211",
                "title": "Quakes <M5> & \\"more\\"",
                "content_html": "<p>Body &amp; <strong>bold</strong></p>",
                "content_text": "Body & bold",
                "date_published": "2026-10-13T10:00:00Z",
                "date_modified": "2026-10-13T10:05:30Z",
                "tags": ["critical", "ops"],
                "authors": [{"name": "Foresignal"}],
                "_foresignal": {
                  "severity": "critical",
                  "state": "acknowledged",
                  "sourceId": "usgs-earthquakes",
                  "externalId": "us7000abcd",
                  "geometry": {"type": "Point", "coordinates": [-97.5, 30.25]}
                }
              }]
            }
            """;

    private final JsonFeedWriter writer = new JsonFeedWriter(JSON);

    @Test
    @DisplayName("the document equals the golden JSON Feed 1.1")
    void golden() {
        JsonNode written = JSON.readTree(writer.write(FeedFixtures.feed(FeedFixtures.entry(4211,
                FeedFixtures.point(-97.5, 30.25)))));
        assertThat(written).isEqualTo(JSON.readTree(GOLDEN));
    }

    @Test
    @DisplayName("members follow the spec: version, title and items are present; an item without extension has none")
    void members() {
        FeedEntry plain = new FeedEntry("tag:x/run/1", "Run", null, null, FeedFixtures.FIRED, null, null,
                java.util.List.of(), null, null);
        JsonNode doc = JSON.valueToTree(writer.document(FeedFixtures.feed(plain)));
        assertThat(doc.get("version").asString()).isEqualTo(JsonFeedWriter.VERSION);
        JsonNode item = doc.get("items").get(0);
        assertThat(item.has("_foresignal")).isFalse();
        assertThat(item.has("url")).isFalse();
        assertThat(item.has("date_published")).isFalse();
        assertThat(item.get("content_html").asString()).isEmpty();
        assertThat(item.get("date_modified").asString()).isEqualTo("2026-10-13T10:00:00Z");
        assertThat(JsonFeedWriter.MEDIA_TYPE).isEqualTo("application/feed+json");
    }
}
