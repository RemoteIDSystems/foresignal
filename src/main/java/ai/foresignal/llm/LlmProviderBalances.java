package ai.foresignal.llm;

import ai.foresignal.llm.client.LlmProviderException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The balance a provider itself reports for the account behind a provider's API key, for
 * {@code getLlmProviderBudget}: information only, displayed beside the USD caps and never what they
 * enforce (that is the self-metered spend, {@link LlmBudgetGovernor}).
 *
 * <p>Only providers whose API returns a balance to the same key that makes inference calls are asked;
 * every other provider has none ({@link #balance} is empty). Recognized by the base URL's host:
 *
 * <ul>
 *   <li>DeepSeek ({@code api.deepseek.com}): {@code GET /user/balance}, each currency's
 *       {@code total_balance} (api-docs.deepseek.com/api/get-user-balance).
 *   <li>OpenRouter ({@code openrouter.ai}): {@code GET /api/v1/credits}, {@code total_credits} less
 *       {@code total_usage}, which answers only a management key; otherwise {@code GET /api/v1/key},
 *       the remaining spending limit of the key, {@code limit_remaining}, when it has a limit
 *       (openrouter.ai/docs/api-reference/get-credits and …/api-keys/get-current-api-key).
 * </ul>
 *
 * <p>xAI reports a prepaid balance only to a management key (management-api.x.ai), which is not the
 * inference key, so it is not asked. The requests go through {@link LlmHttp}, under the same outbound
 * URL policy and pinning as the provider's calls. A figure is kept for {@link #CACHE_FOR}, so opening
 * the budget does not call the provider every time; a failure is reported in the balance's
 * {@code error}, never as an error of the budget request.
 */
@Component
public class LlmProviderBalances {

    /** How long a fetched balance is reused. */
    static final Duration CACHE_FOR = Duration.ofSeconds(60);

    /** The timeout of one balance request. */
    static final Duration TIMEOUT = Duration.ofSeconds(15);

    /**
     * A provider-reported balance.
     *
     * @param provider who reported it, for example {@code DeepSeek}
     * @param kind {@code account} (the account's balance or credits) or {@code key-limit} (what is left
     *     of the key's own spending limit)
     * @param amounts the amounts, one per currency; empty when the request failed
     * @param fetchedAt when it was fetched
     * @param error why it could not be fetched, or null
     */
    public record Balance(String provider, String kind, List<Amount> amounts, Instant fetchedAt, String error) {

        /** Copies the amounts. */
        public Balance {
            amounts = amounts == null ? List.of() : List.copyOf(amounts);
        }
    }

    /**
     * One amount of a balance.
     *
     * @param amount the amount
     * @param currency its ISO 4217 code, for example {@code USD} or {@code CNY}
     */
    public record Amount(BigDecimal amount, String currency) {}

    private record Cached(String key, Balance balance) {}

    private final LlmHttp http;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    /** Creates the service. */
    public LlmProviderBalances(LlmHttp http, JsonMapper json, Clock clock) {
        this.http = http;
        this.json = json;
        this.clock = clock;
    }

    /** The provider that reports a balance at {@code baseUrl}, or null when it reports none. */
    public static String reporter(String baseUrl) {
        String host = host(baseUrl);
        if (host == null) {
            return null;
        }
        if (host.equals("api.deepseek.com")) {
            return "DeepSeek";
        }
        if (host.equals("openrouter.ai")) {
            return "OpenRouter";
        }
        return null;
    }

    /** The balance {@code config}'s provider reports, or empty when it reports none or has no key. */
    public Optional<Balance> balance(LlmProviderConfig config) {
        String reporter = config.type() == LlmProviderType.OPENAI_COMPATIBLE ? reporter(config.baseUrl()) : null;
        if (reporter == null || config.apiKey() == null || config.apiKey().isBlank()) {
            return Optional.empty();
        }
        String cacheKey = config.baseUrl() + "\n" + Integer.toHexString(config.apiKey().hashCode());
        Cached cached = cache.get(config.id());
        Instant now = clock.instant();
        if (cached != null && cached.key().equals(cacheKey)
                && cached.balance().fetchedAt().plus(CACHE_FOR).isAfter(now)) {
            return Optional.of(cached.balance());
        }
        Balance balance = fetch(config, reporter, now);
        cache.put(config.id(), new Cached(cacheKey, balance));
        return Optional.of(balance);
    }

    /** Asks {@code reporter}'s balance endpoint at {@code config}'s base URL, without the cache. */
    Balance fetch(LlmProviderConfig config, String reporter, Instant now) {
        try {
            return reporter.equals("DeepSeek") ? deepSeek(config, now) : openRouter(config, now);
        } catch (LlmProviderException | IllegalStateException e) {
            return new Balance(reporter, "account", List.of(), now, e.getMessage());
        }
    }

    /** Forgets a provider's cached balance. */
    public void forget(String providerId) {
        cache.remove(providerId);
    }

    private Balance deepSeek(LlmProviderConfig config, Instant now) {
        JsonNode body = get(config, "/user/balance");
        List<Amount> amounts = new ArrayList<>();
        for (JsonNode info : body.path("balance_infos")) {
            BigDecimal amount = decimal(info.get("total_balance"));
            String currency = info.path("currency").asString("");
            if (amount != null && !currency.isBlank()) {
                amounts.add(new Amount(amount, currency.toUpperCase(Locale.ROOT)));
            }
        }
        if (amounts.isEmpty()) {
            throw new IllegalStateException("DeepSeek answered /user/balance without a total_balance");
        }
        return new Balance("DeepSeek", "account", amounts, now, null);
    }

    private Balance openRouter(LlmProviderConfig config, Instant now) {
        HttpResponse<String> credits = send(config, "/api/v1/credits");
        if (credits.statusCode() == 200) {
            JsonNode data = parse(config, credits.body()).path("data");
            BigDecimal total = decimal(data.get("total_credits"));
            BigDecimal used = decimal(data.get("total_usage"));
            if (total != null && used != null) {
                return new Balance("OpenRouter", "account", List.of(new Amount(total.subtract(used), "USD")), now,
                        null);
            }
        }
        // /credits answers only a management key; an inference key reads what is left of its own limit.
        JsonNode data = get(config, "/api/v1/key").path("data");
        BigDecimal remaining = decimal(data.get("limit_remaining"));
        if (remaining == null) {
            return new Balance("OpenRouter", "key-limit", List.of(), now,
                    "this key has no spending limit, and only a management key may read the account's credits");
        }
        return new Balance("OpenRouter", "key-limit", List.of(new Amount(remaining, "USD")), now, null);
    }

    private JsonNode get(LlmProviderConfig config, String path) {
        HttpResponse<String> response = send(config, path);
        if (response.statusCode() != 200) {
            throw new IllegalStateException(path + " answered HTTP " + response.statusCode());
        }
        return parse(config, response.body());
    }

    private HttpResponse<String> send(LlmProviderConfig config, String path) {
        URI base = URI.create(config.baseUrl());
        String origin = base.getScheme() + "://" + base.getRawAuthority();
        HttpRequest.Builder request = HttpRequest.newBuilder(http.uri(config.id(), origin, path)).timeout(TIMEOUT)
                .header("Authorization", "Bearer " + config.apiKey()).header("Accept", "application/json").GET();
        config.extraHeaders().forEach(request::header);
        return http.send(config.id(), request.build());
    }

    private JsonNode parse(LlmProviderConfig config, String body) {
        try {
            return json.readTree(body == null ? "" : body);
        } catch (RuntimeException e) {
            throw new IllegalStateException("the balance answer of " + config.id() + " is not JSON");
        }
    }

    private static BigDecimal decimal(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return node.isNumber() ? node.decimalValue() : new BigDecimal(node.asString().strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String host(String baseUrl) {
        try {
            String host = baseUrl == null ? null : URI.create(baseUrl).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
