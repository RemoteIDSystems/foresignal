package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One {@code wzdx-*} source (prompt 074): a WZDx 4.x feed of the vendored registry snapshot as {@code road-event}
 * records ({@link WzdxParser}).
 *
 * <p>Each run makes one conditional GET of the feed ({@value #STATE_KEY}); a 304 is
 * {@link FetchResult#unchanged()}. A key-required feed sends its {@code apiKey} credential where the registry
 * documents it (a query parameter, or a header), and the upstream client redacts the value from every trace; a
 * missing key fails before any request. A 3.x or CWZ document is an {@link UpstreamFormatException} naming the
 * version. Interval = the registry's update frequency clamped to [5 min, 30 min]; budget 2/min and 600/day.
 */
public class WzdxAdapter implements PollingAdapter {

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "feed";

    /** The default budget. */
    public static final Budget BUDGET = Budget.of(2, 600);

    /** The response cap. */
    public static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    /** The license text of every feed (the registry records no license per feed). */
    public static final String LICENSE = "Publisher terms (listed in the USDOT WZDx Feed Registry; most U.S."
            + " public-agency feeds are open data)";

    /** The registry page, the homepage of every feed. */
    public static final String REGISTRY_PAGE = "https://data.transportation.gov/d/69qe-yiui";

    private final WzdxFeed feed;
    private final SourceDescriptor descriptor;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates the source.
     *
     * @param feed the vendored feed
     * @param meters supplies the registry of the skip counter, or null
     */
    public WzdxAdapter(WzdxFeed feed, Supplier<MeterRegistry> meters) {
        this.feed = Objects.requireNonNull(feed);
        this.meters = meters;
        SourceDescriptor.Builder b = RoadEventDescriptors.builder(feed.id(),
                        "Work zones: " + feed.issuingOrganization() + " (WZDx)",
                        "Current work zones, detours and restrictions from the WZDx " + feed.version() + " feed of "
                                + feed.issuingOrganization() + ", as road-event records. An event missing from two"
                                + " consecutive complete fetches is tombstoned; a completed or canceled event is"
                                + " tombstoned at once.",
                        WzdxFeed.MIN_INTERVAL, feed.defaultInterval(), BUDGET, MAX_RESPONSE_BYTES)
                .homepage(REGISTRY_PAGE)
                .attribution(feed.issuingOrganization())
                .licence(LICENSE)
                .termsUrl(feed.apiKeyUrl() != null ? feed.apiKeyUrl() : REGISTRY_PAGE);
        if (feed.apiKey() != null) {
            b.credential(CredentialField.required(WzdxFeed.CREDENTIAL, "The " + feed.issuingOrganization()
                    + " WZDx API key, sent as the " + feed.apiKey().placement() + " " + feed.apiKey().name()
                    + (feed.apiKeyUrl() == null ? "" : "; register at " + feed.apiKeyUrl())));
        }
        this.descriptor = b.build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The feed. */
    public WzdxFeed feed() {
        return feed;
    }

    /**
     * Fetches the feed once (see the class note).
     *
     * @return {@link FetchResult#unchanged()} on a 304; otherwise the events plus tombstones
     * @throws AdapterException when the request fails or the key is missing
     * @throws UpstreamFormatException on a non-200 or truncated body, an unsupported version, or a document that
     *     does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/geo+json, application/json;q=0.9");
        URI url = withKey(ctx, headers);
        UpstreamResponse response = ctx.http().getConditional(url, STATE_KEY, headers);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        byte[] body = RoadFetch.body(ctx, response, feed.id(), STATE_KEY, null);
        ParsedEvents parsed;
        try {
            parsed = WzdxParser.parse(body, now);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
            throw e;
        }
        RoadFetch.count(meters, ctx.sourceId(), parsed.skipped());
        ctx.log().debug("{}: {} event(s), {} ended, skipped {}", ctx.sourceId(), parsed.ids().size(),
                parsed.endedIds().size(), parsed.skipped());
        return FetchResult.complete(RoadFetch.records(ctx, parsed, null, true, now));
    }

    /**
     * The feed URL with the key added as its query parameter, or {@code headers} with the key header.
     *
     * @throws AdapterException when the feed needs a key and none is stored
     */
    URI withKey(FetchContext ctx, Map<String, String> headers) throws AdapterException {
        if (feed.apiKey() == null) {
            return feed.url();
        }
        String value = ctx.credentials() == null ? null : ctx.credentials().get(WzdxFeed.CREDENTIAL);
        if (value == null || value.isBlank()) {
            throw new AdapterException(feed.id() + " needs its " + WzdxFeed.CREDENTIAL + " credential");
        }
        if (!feed.apiKey().query()) {
            headers.put(feed.apiKey().name(), value.strip());
            return feed.url();
        }
        String base = feed.url().toString();
        String separator = feed.url().getRawQuery() == null ? "?" : "&";
        return URI.create(base + separator + URLEncoder.encode(feed.apiKey().name(), StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(value.strip(), StandardCharsets.UTF_8));
    }
}
