package ai.foresignal.social;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * The settings of one social platform adapter as adapters read them (prompt 135, OpenAPI
 * {@code SocialPlatformSettings} without the 005 budget, which the source carries).
 *
 * @param platformId the adapter id
 * @param enabled the operator's intent to collect
 * @param mode the mode, or null
 * @param channels channels polled regardless of topics
 * @param deletionSync whether deletion sync is on
 * @param postRetention the retention of this platform's posts
 * @param monthlyCapUsd the money cap, or null
 * @param prices the operator-editable prices
 * @param pricesAsOf the prices' date, or null
 * @param options platform options
 */
public record SocialPlatformSettings(String platformId, boolean enabled, String mode, List<String> channels,
        boolean deletionSync, Duration postRetention, BigDecimal monthlyCapUsd,
        List<SocialPlatformCatalog.Price> prices, String pricesAsOf, Map<String, Object> options) {

    /** Copies the collections. */
    public SocialPlatformSettings {
        channels = channels == null ? List.of() : List.copyOf(channels);
        prices = prices == null ? List.of() : List.copyOf(prices);
        options = options == null ? Map.of() : java.util.Collections.unmodifiableMap(
                new java.util.LinkedHashMap<>(options));
    }

    /** The catalog defaults of an adapter. */
    public static SocialPlatformSettings defaults(SocialPlatformCatalog.Definition d) {
        return new SocialPlatformSettings(d.id(), d.defaultEnabled(), d.defaultMode(), List.of(),
                d.deletionSyncDefault(), SocialVocabulary.DEFAULT_POST_RETENTION, d.defaultMonthlyCapUsd(),
                d.defaultPrices(), d.pricesAsOf(), Map.of());
    }

    /** A boolean option, or {@code fallback}. */
    public boolean option(String name, boolean fallback) {
        Object v = options.get(name);
        return v instanceof Boolean b ? b : v == null ? fallback : Boolean.parseBoolean(v.toString());
    }

    /** A string-list option. */
    public List<String> listOption(String name) {
        Object v = options.get(name);
        if (!(v instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(java.util.Objects::nonNull).map(Object::toString).map(String::strip)
                .filter(s -> !s.isEmpty()).toList();
    }

    /** The price of an item, or null when none is set. */
    public SocialPlatformCatalog.Price price(String item) {
        for (SocialPlatformCatalog.Price p : prices) {
            if (p.item().equals(item)) {
                return p;
            }
        }
        return null;
    }
}
