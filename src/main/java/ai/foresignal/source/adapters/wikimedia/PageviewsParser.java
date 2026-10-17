package ai.foresignal.source.adapters.wikimedia;

import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the Wikimedia Pageviews REST API (prompt 117): per-article daily views and the daily top articles of a
 * project. Pure: no network, no state.
 */
public final class PageviewsParser {

    /** The kind of the daily top-articles record. */
    public static final String TOP_KIND = "attention-top";

    /** The most top articles kept per day. */
    public static final int TOP_LIMIT = 100;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private PageviewsParser() {}

    /**
     * An article title encoded as in the API path: spaces become underscores, then the title is percent-encoded
     * (UTF-8; a space would be {@code %20}, never {@code +}).
     */
    public static String encodeArticle(String article) {
        return URLEncoder.encode(article.strip().replace(' ', '_'), StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** {@code wiki.{project}.{article}}, the article encoded as in the API path. */
    public static String seriesId(String project, String article) {
        return "wiki." + project + "." + encodeArticle(article);
    }

    /** The series metadata of a pageview series: attention, log-level, a 1-day release lag, its topic. */
    public static SeriesMeta meta(String topicId) {
        return new SeriesMeta(SeriesMeta.ATTENTION, SeriesMeta.LOG_LEVEL, 1, null, null, topicId);
    }

    /**
     * The points of a per-article body.
     *
     * @param project the project
     * @param article the article title
     * @param body the response
     * @param observedAt the fetch time
     */
    public static List<NormalizedRecord> perArticle(String project, String article, byte[] body,
            Instant observedAt) {
        JsonNode items = tree(body).get("items");
        List<NormalizedRecord> out = new ArrayList<>();
        if (items == null || !items.isArray()) {
            return out;
        }
        String seriesId = seriesId(project, article);
        for (JsonNode item : items) {
            LocalDate day = day(JsonProps.text(item, "timestamp"));
            Double views = JsonProps.number(item, "views");
            if (day == null || views == null) {
                continue;
            }
            Instant start = day.atStartOfDay(ZoneOffset.UTC).toInstant();
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("project", project);
            extra.put("agent", JsonProps.text(item, "agent"));
            out.add(SeriesPoint.point(seriesId, project + ": " + article.replace('_', ' '), "pageviews/per-article",
                    "P1D", day.toString(), start, day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(), views,
                    "{views}", project, extra, "final", observedAt));
        }
        return out;
    }

    /**
     * The top-articles record of a {@code pageviews/top} body: {@code kind=attention-top},
     * {@code externalId = {project}:{date}}, the top {@value #TOP_LIMIT} {@code {article, views, rank}}.
     *
     * @return the record, or null when the body holds no articles
     */
    public static NormalizedRecord top(String project, LocalDate date, byte[] body, Instant observedAt) {
        JsonNode items = tree(body).get("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            return null;
        }
        JsonNode articles = items.get(0).get("articles");
        if (articles == null || !articles.isArray()) {
            return null;
        }
        List<Map<String, Object>> top = new ArrayList<>();
        for (JsonNode a : articles) {
            if (top.size() >= TOP_LIMIT) {
                break;
            }
            String article = JsonProps.text(a, "article");
            Long views = JsonProps.integer(a, "views");
            Long rank = JsonProps.integer(a, "rank");
            if (article == null || views == null) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("article", article);
            entry.put("views", views);
            entry.put("rank", rank == null ? top.size() + 1 : rank);
            top.add(entry);
        }
        return NormalizedRecord.builder(project + ":" + date, TOP_KIND)
                .observedAt(observedAt)
                .eventTime(date.atStartOfDay(ZoneOffset.UTC).toInstant())
                .put("project", project)
                .put("date", date.toString())
                .put("title", "Top " + project + " articles " + date)
                .put("articles", top)
                .build();
    }

    /** The day of a {@code 2026100500} timestamp, or null. */
    static LocalDate day(String timestamp) {
        if (timestamp == null || timestamp.length() < 8) {
            return null;
        }
        try {
            return LocalDate.of(Integer.parseInt(timestamp.substring(0, 4)),
                    Integer.parseInt(timestamp.substring(4, 6)), Integer.parseInt(timestamp.substring(6, 8)));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static JsonNode tree(byte[] body) {
        if (body == null || body.length == 0) {
            return JSON.createObjectNode();
        }
        try {
            JsonNode root = JSON.readTree(body);
            if (root == null || !root.isObject()) {
                throw new UpstreamFormatException("wikimedia-pageviews: the response is not a JSON object", null);
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException("wikimedia-pageviews: the response is not JSON: "
                    + e.getOriginalMessage(), null, e);
        }
    }
}
