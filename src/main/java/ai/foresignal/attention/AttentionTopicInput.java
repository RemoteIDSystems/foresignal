package ai.foresignal.attention;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * The body of {@code createAttentionTopic} and {@code updateAttentionTopic} (OpenAPI {@code AttentionTopicInput},
 * prompt 117). Every list may be null (empty).
 *
 * @param id the slug id ({@code ^[a-z0-9-]{3,48}$})
 * @param name the display name, 1 to 200 characters
 * @param description the description, or null
 * @param gdeltQuery the GDELT DOC 2.0 query (at most 250 characters), or null
 * @param wikiArticles at most 20 {@code {project, article}}
 * @param keywords at most 30 keywords (used by 118's linker)
 * @param regionCodes 040 region codes
 * @param themes free themes
 * @param enabled whether the topic drives the attention sources (default false)
 * @param social the social block (prompt 135, {@code ai.foresignal.social.SocialTopicBlock}), or null for none
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttentionTopicInput(
        String id,
        String name,
        String description,
        String gdeltQuery,
        List<WikiArticle> wikiArticles,
        List<String> keywords,
        List<String> regionCodes,
        List<String> themes,
        Boolean enabled,
        Map<String, Object> social) {

    /**
     * One Wikipedia article.
     *
     * @param project the project, for example {@code en.wikipedia}
     * @param article the article title
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WikiArticle(String project, String article) {}
}
