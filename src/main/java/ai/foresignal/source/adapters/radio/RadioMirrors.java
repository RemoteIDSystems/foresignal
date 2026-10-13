// Ported from God's Eye View, server/providers/radio/transport.js (radioMirrorOrigin),
// server/providers/radio/catalog.js (mirrors, fetchPath) and server/providers/radio/constants.js
// (RADIO_FALLBACK_MIRRORS, RADIO_MIRROR_CACHE_MS), MIT; see THIRD_PARTY_NOTICES.md. Reworked in Java: the cache
// lives in the adapter state, the order is shuffled, and only a fallback replaces a failed discovery.
package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Radio Browser mirror discovery (prompt 075).
 *
 * <p>{@code GET https://all.api.radio-browser.info/json/servers} answers {@code [{ip, name}]}. Only names matching
 * {@value #HOST_PATTERN_TEXT} are kept: this is the SSRF guard, so a discovery answer can never point Foresignal at
 * another host. The names are de-duplicated (each mirror is listed once per address family), shuffled to spread the
 * load, and cached for {@link #CACHE_TTL} in the adapter state ({@value #MIRRORS_KEY}, {@value #MIRRORS_AT_KEY}).
 *
 * <p>When discovery fails (a request error, a non-200 answer, a body that is not a JSON array, or no acceptable
 * name), the fallback list {@link #FALLBACK} (GEV's constants) is used for this run and nothing is cached, so the
 * next run tries discovery again. Verified 2026-10-13: discovery returned only {@code de1.api.radio-browser.info}
 * (one IPv4 and one IPv6 entry).
 */
public final class RadioMirrors {

    /** The discovery URL. */
    public static final String DISCOVERY_URL = "https://all.api.radio-browser.info/json/servers";

    /** The accepted mirror host names. */
    public static final String HOST_PATTERN_TEXT = "^[a-z0-9-]+\\.api\\.radio-browser\\.info$";

    /** {@link #HOST_PATTERN_TEXT} compiled. */
    public static final Pattern HOST_PATTERN = Pattern.compile(HOST_PATTERN_TEXT);

    /** The mirrors used when discovery fails (GEV {@code RADIO_FALLBACK_MIRRORS}). */
    public static final List<String> FALLBACK = List.of("de1.api.radio-browser.info", "de2.api.radio-browser.info",
            "nl1.api.radio-browser.info");

    /** How long a discovered list is reused (GEV {@code RADIO_MIRROR_CACHE_MS}). */
    public static final Duration CACHE_TTL = Duration.ofHours(6);

    /** The state key of the cached mirror names (comma-separated, in the shuffled order). */
    public static final String MIRRORS_KEY = "radio:mirrors";

    /** The state key of when they were discovered (ISO-8601). */
    public static final String MIRRORS_AT_KEY = "radio:mirrorsAt";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI discovery;
    private final Random random;

    /**
     * Creates the discovery.
     *
     * @param discovery the discovery URL (moved under a test base in tests)
     * @param random shuffles the list
     */
    public RadioMirrors(URI discovery, Random random) {
        this.discovery = discovery;
        this.random = random == null ? new Random() : random;
    }

    /**
     * The mirror host names for this run: the cached list while fresh, else a new discovery, else the fallback.
     *
     * @param ctx the fetch context (network, state, clock)
     * @throws AdapterException only when the budget or a backoff refuses the discovery request; every upstream
     *     failure falls back instead
     */
    public List<String> resolve(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        List<String> cached = cached(ctx.state(), now);
        if (cached != null) {
            return cached;
        }
        List<String> discovered;
        try {
            UpstreamResponse response = ctx.http().get(discovery);
            discovered = response.status() == 200 && !response.truncated() ? parse(response.body()) : List.of();
        } catch (AdapterException e) {
            if (!RadioBrowserAdapter.retryable(e)) {
                throw e;
            }
            discovered = List.of();
        }
        if (discovered.isEmpty()) {
            ctx.log().warn("{}: Radio Browser mirror discovery failed; using the fallback mirrors {}", ctx.sourceId(),
                    FALLBACK);
            return FALLBACK;
        }
        List<String> shuffled = new ArrayList<>(discovered);
        Collections.shuffle(shuffled, random);
        ctx.state().put(MIRRORS_KEY, String.join(",", shuffled));
        ctx.state().put(MIRRORS_AT_KEY, now.toString());
        return List.copyOf(shuffled);
    }

    /**
     * The acceptable mirror names of a discovery answer, de-duplicated, in answer order.
     *
     * @param body the {@code /json/servers} body
     * @return the names; empty when the body is not a JSON array or nothing matches
     */
    public static List<String> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            return List.of();
        }
        if (root == null || !root.isArray()) {
            return List.of();
        }
        Set<String> names = new LinkedHashSet<>();
        for (JsonNode row : root) {
            String name = accepted(row == null || !row.isObject() || !row.has("name") ? null : row.get("name")
                    .asString());
            if (name != null) {
                names.add(name);
            }
        }
        return List.copyOf(names);
    }

    /**
     * {@code name} lower-cased without a trailing dot when it is a Radio Browser mirror host, else null.
     *
     * @param name a host name from the discovery answer
     */
    public static String accepted(String name) {
        if (name == null) {
            return null;
        }
        String host = name.strip().toLowerCase(Locale.ROOT);
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        return HOST_PATTERN.matcher(host).matches() ? host : null;
    }

    /** The cached list while it is fresh and every entry is still acceptable, else null. */
    static List<String> cached(AdapterStateStore state, Instant now) {
        String names = state.get(MIRRORS_KEY).orElse(null);
        String at = state.get(MIRRORS_AT_KEY).orElse(null);
        if (names == null || at == null) {
            return null;
        }
        try {
            if (Instant.parse(at).plus(CACHE_TTL).isBefore(now)) {
                return null;
            }
        } catch (DateTimeParseException e) {
            return null;
        }
        List<String> list = Arrays.stream(names.split(",")).map(RadioMirrors::accepted).toList();
        return list.isEmpty() || list.stream().anyMatch(java.util.Objects::isNull) ? null : list;
    }
}
