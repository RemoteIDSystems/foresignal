package ai.foresignal.social.correlation;

import ai.foresignal.llm.LlmBudgetGovernor;
import ai.foresignal.llm.LlmHttp;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialVocabulary;
import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The optional {@code xai-x-search} cue backend (prompt 137, off by default: {@code social.cues.xaiXSearch.enabled}):
 * the 026 LLM provider {@code xai} (an {@code openai-compatible} provider whose base URL is {@code https://api.x.ai/v1})
 * asked through {@code POST /responses} with the {@code x_search} tool limited to the cue's dates ({@code from_date}
 * inclusive, {@code to_date} exclusive, {@code YYYY-MM-DD}) and {@code max_turns} 2 (request shape read on
 * docs.x.ai on 2026-10-18). The cited post URLs ({@code url_citation} annotations of the {@code output_text}) are a
 * ranked sample of posts: only their ids and URLs are kept, never the model's text. Tokens are charged to the
 * provider's 026 budget; posts fetched are billed per result ($5 per 1,000 as of 2026-09-21), estimated before the
 * call against the cue's cost cap.
 */
@Component
public class XaiXSearchBackend {

    /** The 026 provider id. */
    public static final String PROVIDER = "xai";

    /** The price per post fetched (USD), as published for 2026-09-21. */
    public static final BigDecimal PRICE_PER_POST = new BigDecimal("0.005");

    /** The output tokens allowed. */
    static final int MAX_OUTPUT_TOKENS = 2_000;

    private static final Pattern STATUS = Pattern.compile(
            "^https?://(?:www\\.|mobile\\.)?(?:x|twitter)\\.com/([A-Za-z0-9_]{1,15})/status(?:es)?/(\\d{1,25})");

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * What a search gave.
     *
     * @param posts the cited posts (ids and URLs only)
     * @param postsFetched the posts xAI fetched (billed)
     * @param costUsd the estimated result cost
     * @param error why it failed, or null
     */
    public record Found(List<SocialPost> posts, long postsFetched, BigDecimal costUsd, String error) {}

    private final ObjectProvider<LlmProviderService> providers;
    private final ObjectProvider<LlmHttp> http;
    private final ObjectProvider<LlmBudgetGovernor> budgets;

    /** Creates the backend. */
    public XaiXSearchBackend(ObjectProvider<LlmProviderService> providers, ObjectProvider<LlmHttp> http,
            ObjectProvider<LlmBudgetGovernor> budgets) {
        this.providers = providers;
        this.http = http;
        this.budgets = budgets;
    }

    /** The estimated cost of {@code maxResults} posts. */
    public static BigDecimal estimate(int maxResults) {
        return PRICE_PER_POST.multiply(BigDecimal.valueOf(Math.max(0, maxResults)));
    }

    /** The request body. Pure. */
    public static Map<String, Object> body(String model, String query, Instant from, Instant to, int maxResults) {
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", "x_search");
        tool.put("from_date", DAY.format(from));
        tool.put("to_date", DAY.format((to == null ? from : to).plus(Duration.ofDays(1))));
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", "Find up to " + maxResults + " public X posts matching this query: " + query
                + ". Cite each post you find by its URL. Do not summarize or judge them.");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("model", model);
        out.put("input", List.of(message));
        out.put("tools", List.of(tool));
        out.put("max_turns", 2);
        return out;
    }

    /** The cited post URLs of a {@code /responses} answer, as posts of collection {@code cue}. Pure. */
    public static List<SocialPost> posts(JsonNode root, int maxResults) {
        Set<String> urls = new LinkedHashSet<>();
        JsonNode output = root == null ? null : root.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content == null || !content.isArray()) {
                    continue;
                }
                for (JsonNode part : content) {
                    JsonNode annotations = part.get("annotations");
                    if (annotations == null || !annotations.isArray()) {
                        continue;
                    }
                    for (JsonNode a : annotations) {
                        if ("url_citation".equals(a.path("type").asString("")) && a.hasNonNull("url")) {
                            urls.add(a.get("url").asString());
                        }
                    }
                }
            }
        }
        JsonNode citations = root == null ? null : root.get("citations");
        if (citations != null && citations.isArray()) {
            for (JsonNode c : citations) {
                if (c.isString()) {
                    urls.add(c.asString());
                }
            }
        }
        List<SocialPost> out = new ArrayList<>();
        for (String url : urls) {
            Matcher m = STATUS.matcher(url);
            if (!m.find() || out.size() >= maxResults) {
                continue;
            }
            out.add(new SocialPost(SocialVocabulary.X, m.group(2), "https://x.com/" + m.group(1) + "/status/"
                    + m.group(2), new SocialPost.Author(m.group(1), null, m.group(1), null, null, null), null, null,
                    null, null, null, null, List.of(), List.of(), List.of(), List.of(), null, null, null,
                    SocialVocabulary.COLLECTION_CUE, null));
        }
        return out;
    }

    /**
     * Searches X through xAI.
     *
     * @param query the exact query
     * @param from the window start
     * @param to the window end, or null
     * @param maxResults the most posts
     */
    public Found search(String query, Instant from, Instant to, int maxResults) {
        LlmProviderService service = providers.getIfAvailable();
        LlmHttp client = http.getIfAvailable();
        if (service == null || client == null || service.find(PROVIDER).isEmpty()) {
            return new Found(List.of(), 0, BigDecimal.ZERO, "PLATFORM_DISABLED: no LLM provider '" + PROVIDER + "'");
        }
        LlmProviderConfig config = service.config(PROVIDER);
        if (!config.enabled() || config.apiKey() == null || config.apiKey().isBlank()) {
            return new Found(List.of(), 0, BigDecimal.ZERO, "PLATFORM_DISABLED: provider '" + PROVIDER
                    + "' is disabled or has no key");
        }
        String body = JSON.writeValueAsString(body(config.chatModel(), query, from, to, maxResults));
        LlmBudgetGovernor governor = budgets.getIfAvailable();
        LlmBudgetGovernor.Reservation reservation = null;
        try {
            if (governor != null) {
                reservation = governor.reserve(config, LlmBudgetGovernor.estimate(body.length(), MAX_OUTPUT_TOKENS));
            }
            HttpRequest request = HttpRequest.newBuilder(client.uri(PROVIDER, config.baseUrl(), "/responses"))
                    .timeout(Duration.ofMinutes(2)).header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = client.send(PROVIDER, request);
            if (response.statusCode() != 200) {
                settle(governor, reservation, 0);
                return new Found(List.of(), 0, BigDecimal.ZERO, "xai-x-search: HTTP " + response.statusCode());
            }
            JsonNode root = JSON.readTree(response.body());
            JsonNode usage = root.path("usage");
            settle(governor, reservation, usage.path("total_tokens").asLong(0));
            long fetched = usage.path("server_side_tool_usage_details").path("x_posts_fetched").asLong(0);
            return new Found(posts(root, maxResults), fetched, PRICE_PER_POST.multiply(BigDecimal.valueOf(fetched)),
                    null);
        } catch (RuntimeException e) {
            settle(governor, reservation, 0);
            return new Found(List.of(), 0, BigDecimal.ZERO, "xai-x-search: " + (e.getMessage() == null ? e.toString()
                    : e.getMessage()));
        }
    }

    private static void settle(LlmBudgetGovernor governor, LlmBudgetGovernor.Reservation reservation, long tokens) {
        if (governor != null && reservation != null) {
            governor.settle(reservation, tokens);
        }
    }
}
