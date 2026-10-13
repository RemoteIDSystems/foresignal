package ai.foresignal.feed;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes a {@link Feed} as JSON Feed 1.1 (https://jsonfeed.org/version/1.1) with Jackson; media type
 * {@value #MEDIA_TYPE}. Member names follow the spec (snake_case); alert items carry the
 * {@code _foresignal} extension {@code {severity, state, sourceId, externalId, geometry}}.
 */
@Component
public class JsonFeedWriter {

    /** The JSON Feed media type. */
    public static final String MEDIA_TYPE = "application/feed+json";

    /** The {@code version} member. */
    public static final String VERSION = "https://jsonfeed.org/version/1.1";

    private final JsonMapper json;

    /** Creates the writer. */
    public JsonFeedWriter(JsonMapper json) {
        this.json = json;
    }

    /** {@code feed} as a JSON Feed document. */
    public String write(Feed feed) {
        return json.writeValueAsString(document(feed));
    }

    /** {@code feed} as the ordered map that is serialized. */
    public Map<String, Object> document(Feed feed) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("version", VERSION);
        out.put("title", XmlText.clean(feed.title()));
        if (feed.homeUrl() != null) {
            out.put("home_page_url", feed.homeUrl());
        }
        out.put("feed_url", feed.feedUrl());
        if (feed.description() != null) {
            out.put("description", XmlText.clean(feed.description()));
        }
        out.put("language", "en");
        out.put("authors", List.of(Map.of("name", Feed.AUTHOR)));
        List<Map<String, Object>> items = new ArrayList<>();
        for (FeedEntry entry : feed.entries()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", entry.id());
            if (entry.link() != null) {
                item.put("url", entry.link());
            }
            item.put("title", XmlText.clean(entry.title()));
            item.put("content_html", entry.html() == null ? "" : entry.html());
            item.put("content_text", entry.text() == null ? "" : entry.text());
            if (entry.published() != null) {
                item.put("date_published", AtomFeedWriter.time(entry.published()));
            }
            item.put("date_modified", AtomFeedWriter.time(entry.updated()));
            List<String> tags = new ArrayList<>();
            for (FeedEntry.Category category : entry.categories()) {
                tags.add(XmlText.clean(category.term()));
            }
            item.put("tags", tags);
            item.put("authors", List.of(Map.of("name", Feed.AUTHOR)));
            if (entry.extension() != null) {
                item.put("_foresignal", entry.extension());
            }
            items.add(item);
        }
        out.put("items", items);
        return out;
    }
}
