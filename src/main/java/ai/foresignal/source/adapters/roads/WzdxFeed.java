package ai.foresignal.source.adapters.roads;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One feed of the vendored WZDx Feed Registry snapshot (prompt 074): a {@code wzdx-*} source.
 *
 * @param feedname the registry's {@code feedname}
 * @param issuingOrganization the registry's {@code issuingorganization}, used as the attribution
 * @param state the registry's {@code state}
 * @param version the registry's {@code version} (4.x)
 * @param updateFrequency the registry's {@code datafeed_frequency_update} ({@code 1m}, {@code 60s}, {@code 72h}, …)
 * @param url the feed URL without any credential
 * @param apiKey where the key goes, or null for a keyless feed
 * @param apiKeyUrl the registry's page for obtaining a key, or null
 */
public record WzdxFeed(String feedname, String issuingOrganization, String state, String version,
        String updateFrequency, URI url, ApiKey apiKey, String apiKeyUrl) {

    /** The shortest interval. */
    public static final Duration MIN_INTERVAL = Duration.ofMinutes(5);

    /** The longest default interval. */
    public static final Duration MAX_INTERVAL = Duration.ofMinutes(30);

    /** The credential name of a key-required feed. */
    public static final String CREDENTIAL = "apiKey";

    private static final Pattern FREQUENCY = Pattern.compile("^(\\d+)\\s*([smhd])$");

    /**
     * Where a feed's key goes, as the registry documents it.
     *
     * @param placement {@code query} or {@code header}
     * @param name the query parameter or header name
     */
    public record ApiKey(String placement, String name) {

        /** True when the key is a query parameter. */
        public boolean query() {
            return "query".equals(placement);
        }
    }

    /** The source id: {@code wzdx-} plus the feed name in lower-kebab case ({@code txdot_v4_2} → {@code wzdx-txdot-v4-2}). */
    public String id() {
        String kebab = feedname == null ? "" : feedname.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return "wzdx-" + kebab;
    }

    /**
     * The registry's update frequency, or null when it cannot be read.
     */
    public Duration frequency() {
        if (updateFrequency == null) {
            return null;
        }
        Matcher m = FREQUENCY.matcher(updateFrequency.strip().toLowerCase(Locale.ROOT));
        if (!m.matches()) {
            return null;
        }
        long n = Long.parseLong(m.group(1));
        return switch (m.group(2)) {
            case "s" -> Duration.ofSeconds(n);
            case "m" -> Duration.ofMinutes(n);
            case "h" -> Duration.ofHours(n);
            default -> Duration.ofDays(n);
        };
    }

    /** The default interval: the update frequency clamped to [5 min, 30 min] (30 min when it is unreadable). */
    public Duration defaultInterval() {
        Duration f = frequency();
        if (f == null || f.compareTo(MAX_INTERVAL) > 0) {
            return MAX_INTERVAL;
        }
        return f.compareTo(MIN_INTERVAL) < 0 ? MIN_INTERVAL : f;
    }

    /** This feed with its URL moved under a test base URL. */
    public WzdxFeed rebased(String base) {
        return new WzdxFeed(feedname, issuingOrganization, state, version, updateFrequency,
                RoadFetch.rebased(url, base), apiKey, apiKeyUrl);
    }
}
