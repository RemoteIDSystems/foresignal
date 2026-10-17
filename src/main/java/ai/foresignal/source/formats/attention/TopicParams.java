package ai.foresignal.source.formats.attention;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code topics} parameter of the attention sources (prompt 117): how {@code AttentionTopicService} hands the
 * enabled attention topics to {@code gdelt-timelines} and {@code wikimedia-pageviews} without either adapter reading
 * the store (Architecture §3).
 *
 * <p>The parameter is a string list; each entry is one compact JSON object:
 * {@code {"id":"red-sea-shipping","query":"..."}} for GDELT, and
 * {@code {"id":"red-sea-shipping","articles":[{"project":"en.wikipedia","article":"Bab-el-Mandeb"}]}} for
 * Wikipedia. The adapters validate entries with {@link #gdeltProblem} and {@link #wikiProblem} in their
 * {@code checkParams}, so an invalid entry is refused at {@code PUT /v1/sources/{id}} like any parameter.
 */
public final class TopicParams {

    /** The parameter name both sources use. */
    public static final String PARAM = "topics";

    /** A topic id. */
    public static final Pattern TOPIC_ID = Pattern.compile("^[a-z0-9-]{3,48}$");

    /** A Wikipedia project ({@code en.wikipedia}). */
    public static final Pattern PROJECT = Pattern.compile("^[a-z-]+\\.wikipedia$");

    /** The longest GDELT query. */
    public static final int MAX_QUERY_LENGTH = 250;

    /** The most articles per topic. */
    public static final int MAX_ARTICLES = 20;

    /** The longest article title. */
    public static final int MAX_ARTICLE_LENGTH = 255;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private TopicParams() {}

    /**
     * One GDELT topic.
     *
     * @param id the topic id
     * @param query the GDELT DOC 2.0 query
     */
    public record GdeltTopic(String id, String query) {}

    /**
     * One Wikipedia article.
     *
     * @param project the project, for example {@code en.wikipedia}
     * @param article the article title as in its URL ({@code Strait_of_Hormuz})
     */
    public record WikiArticle(String project, String article) {}

    /**
     * One Wikipedia topic.
     *
     * @param id the topic id
     * @param articles its articles
     */
    public record WikiTopic(String id, List<WikiArticle> articles) {

        /** Copies the article list. */
        public WikiTopic {
            articles = articles == null ? List.of() : List.copyOf(articles);
        }
    }

    /** The parameter entry of a GDELT topic. */
    public static String encode(GdeltTopic topic) {
        ObjectNode node = JSON.createObjectNode();
        node.put("id", topic.id());
        node.put("query", topic.query());
        return JSON.writeValueAsString(node);
    }

    /** The parameter entry of a Wikipedia topic. */
    public static String encode(WikiTopic topic) {
        ObjectNode node = JSON.createObjectNode();
        node.put("id", topic.id());
        ArrayNode articles = node.putArray("articles");
        for (WikiArticle article : topic.articles()) {
            ObjectNode a = articles.addObject();
            a.put("project", article.project());
            a.put("article", article.article());
        }
        return JSON.writeValueAsString(node);
    }

    /** The GDELT topics among {@code entries}; invalid entries are left out. */
    public static List<GdeltTopic> gdelt(List<String> entries) {
        List<GdeltTopic> out = new ArrayList<>();
        for (String entry : entries == null ? List.<String>of() : entries) {
            if (gdeltProblem(entry) == null) {
                JsonNode node = parse(entry);
                out.add(new GdeltTopic(node.get("id").asString(), node.get("query").asString().strip()));
            }
        }
        return out;
    }

    /** The Wikipedia topics among {@code entries}; invalid entries are left out. */
    public static List<WikiTopic> wiki(List<String> entries) {
        List<WikiTopic> out = new ArrayList<>();
        for (String entry : entries == null ? List.<String>of() : entries) {
            if (wikiProblem(entry) == null) {
                JsonNode node = parse(entry);
                List<WikiArticle> articles = new ArrayList<>();
                for (JsonNode a : node.get("articles")) {
                    articles.add(new WikiArticle(a.get("project").asString(),
                            a.get("article").asString().strip().replace(' ', '_')));
                }
                out.add(new WikiTopic(node.get("id").asString(), articles));
            }
        }
        return out;
    }

    /** What is wrong with a GDELT entry, or null when it is valid. */
    public static String gdeltProblem(String entry) {
        JsonNode node = parse(entry);
        if (node == null || !node.isObject()) {
            return "each entry must be a JSON object {\"id\": ..., \"query\": ...}";
        }
        String idProblem = idProblem(node);
        if (idProblem != null) {
            return idProblem;
        }
        JsonNode query = node.get("query");
        if (query == null || !query.isString()) {
            return "topic " + node.get("id").asString() + " needs a query";
        }
        return queryProblem(query.asString());
    }

    /** What is wrong with a Wikipedia entry, or null when it is valid. */
    public static String wikiProblem(String entry) {
        JsonNode node = parse(entry);
        if (node == null || !node.isObject()) {
            return "each entry must be a JSON object {\"id\": ..., \"articles\": [...]}";
        }
        String idProblem = idProblem(node);
        if (idProblem != null) {
            return idProblem;
        }
        JsonNode articles = node.get("articles");
        if (articles == null || !articles.isArray() || articles.isEmpty()) {
            return "topic " + node.get("id").asString() + " needs at least one article";
        }
        if (articles.size() > MAX_ARTICLES) {
            return "topic " + node.get("id").asString() + " has more than " + MAX_ARTICLES + " articles";
        }
        for (JsonNode a : articles) {
            String problem = articleProblem(a == null || !a.isObject() || a.get("project") == null ? null
                    : a.get("project").asString(), a == null || !a.isObject() || a.get("article") == null ? null
                    : a.get("article").asString());
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    /**
     * The dry character check of a GDELT query: 1 to {@value #MAX_QUERY_LENGTH} characters, no control characters,
     * balanced double quotes and parentheses.
     *
     * @return the problem, or null when the query passes
     */
    public static String queryProblem(String query) {
        if (query == null || query.isBlank()) {
            return "the GDELT query must not be blank";
        }
        String q = query.strip();
        if (q.length() > MAX_QUERY_LENGTH) {
            return "the GDELT query is longer than " + MAX_QUERY_LENGTH + " characters";
        }
        int depth = 0;
        int quotes = 0;
        for (int i = 0; i < q.length(); i++) {
            char c = q.charAt(i);
            if (Character.isISOControl(c)) {
                return "the GDELT query contains a control character";
            }
            if (c == '"') {
                quotes++;
            } else if (quotes % 2 == 0 && c == '(') {
                depth++;
            } else if (quotes % 2 == 0 && c == ')') {
                depth--;
                if (depth < 0) {
                    return "the GDELT query has an unbalanced ')'";
                }
            }
        }
        if (quotes % 2 != 0) {
            return "the GDELT query has an unbalanced double quote";
        }
        if (depth != 0) {
            return "the GDELT query has an unbalanced '('";
        }
        return null;
    }

    /**
     * What is wrong with one article, or null when it is valid.
     *
     * @param project the project, like {@code en.wikipedia}
     * @param article the article title
     */
    public static String articleProblem(String project, String article) {
        if (project == null || !PROJECT.matcher(project).matches()) {
            return "an article's project must look like en.wikipedia";
        }
        if (article == null || article.isBlank() || article.length() > MAX_ARTICLE_LENGTH) {
            return "an article title must be 1 to " + MAX_ARTICLE_LENGTH + " characters";
        }
        for (int i = 0; i < article.length(); i++) {
            char c = article.charAt(i);
            if (Character.isISOControl(c) || c == '#' || c == '<' || c == '>' || c == '[' || c == ']' || c == '|'
                    || c == '{' || c == '}') {
                return "the article title '" + article + "' contains a character Wikipedia titles cannot hold";
            }
        }
        return null;
    }

    private static String idProblem(JsonNode node) {
        JsonNode id = node.get("id");
        if (id == null || !id.isString() || !TOPIC_ID.matcher(id.asString()).matches()) {
            return "each entry needs an id matching " + TOPIC_ID.pattern();
        }
        return null;
    }

    private static JsonNode parse(String entry) {
        if (entry == null || entry.isBlank()) {
            return null;
        }
        try {
            return JSON.readTree(entry);
        } catch (JacksonException e) {
            return null;
        }
    }
}
