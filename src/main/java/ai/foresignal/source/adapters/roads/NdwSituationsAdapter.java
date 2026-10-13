package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One NDW DATEX II v3 situation source (prompt 074): {@code ndw-situations} (the current situations,
 * {@code actueel_beeld.xml.gz}) or {@code ndw-srti} (EU safety-related traffic information), as {@code road-event}
 * records ({@link Datex3SituationParser}).
 *
 * <p>Each run makes one conditional GET ({@value #STATE_KEY}) of the gzip file, which NDW rewrites every minute; a
 * 304 is {@link FetchResult#unchanged()}. The body arrives as gzip bytes (NDW serves it as
 * {@code application/xml} without a {@code Content-Encoding}) and is decompressed by the parser, capped at 64 MB.
 * Interval 5 min (at least 1 min), budget 4/min and 2,000/day, 8 MB compressed. License CC0 (NDW's copyright
 * notice); the text is Dutch.
 */
public class NdwSituationsAdapter implements PollingAdapter {

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "situations";

    /** The default budget. */
    public static final Budget BUDGET = Budget.of(4, 2000);

    /** The response cap (compressed). */
    public static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    /**
     * One NDW feed.
     *
     * @param id the source id
     * @param name the display name
     * @param description what the feed holds
     * @param url the gzip file URL
     */
    public record Feed(String id, String name, String description, URI url) {

        /** This feed with its URL moved under a test base URL. */
        public Feed rebased(String base) {
            return new Feed(id, name, description, RoadFetch.rebased(url, base));
        }
    }

    /** The two NDW feeds (the 17 MB planned-works feed is out of scope). */
    public static final List<Feed> FEEDS = List.of(
            new Feed("ndw-situations", "NDW current road situations (Netherlands)",
                    "NDW's current traffic situations for the Netherlands (closures, roadworks, speed and lane"
                            + " management, obstructions, accidents) from the DATEX II v3 file actueel_beeld,"
                            + " rewritten every minute, as road-event records.",
                    URI.create("https://opendata.ndw.nu/actueel_beeld.xml.gz")),
            new Feed("ndw-srti", "NDW safety-related traffic information (Netherlands)",
                    "NDW's EU safety-related traffic information (SRTI: broken-down vehicles, obstructions,"
                            + " accidents) for the Netherlands from the DATEX II v3 file"
                            + " veiligheidsgerelateerde_berichten_srti, as road-event records.",
                    URI.create("https://opendata.ndw.nu/veiligheidsgerelateerde_berichten_srti.xml.gz")));

    private final Feed feed;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor;

    /**
     * Creates the source.
     *
     * @param feed one of {@link #FEEDS}, possibly rebased
     * @param meters supplies the registry of the skip counter, or null
     */
    public NdwSituationsAdapter(Feed feed, Supplier<MeterRegistry> meters) {
        this.feed = Objects.requireNonNull(feed);
        this.meters = meters;
        this.descriptor = RoadEventDescriptors.builder(feed.id(), feed.name(), feed.description()
                                + " A situation missing from two consecutive complete fetches is tombstoned.",
                        Duration.ofMinutes(1), Duration.ofMinutes(5), BUDGET, MAX_RESPONSE_BYTES)
                .homepage("https://opendata.ndw.nu/")
                .attribution("Nationaal Dataportaal Wegverkeer (NDW)")
                .licence("CC0 1.0")
                .termsUrl("https://www.ndw.nu/copyright")
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The feed. */
    public Feed feed() {
        return feed;
    }

    /**
     * Fetches the file once (see the class note).
     *
     * @return {@link FetchResult#unchanged()} on a 304; otherwise the situations plus tombstones
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a non-200 or truncated body, a corrupt or oversized gzip stream, or XML
     *     that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        UpstreamResponse response = ctx.http().getConditional(feed.url(), STATE_KEY,
                Map.of("Accept", "application/xml, application/gzip;q=0.9"));
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        byte[] body = RoadFetch.body(ctx, response, feed.id(), STATE_KEY, null);
        ParsedEvents parsed;
        try {
            parsed = Datex3SituationParser.parse(body, now);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
            throw e;
        }
        RoadFetch.count(meters, ctx.sourceId(), parsed.skipped());
        ctx.log().debug("{}: {} situation(s), skipped {}", ctx.sourceId(), parsed.ids().size(), parsed.skipped());
        return FetchResult.complete(RoadFetch.records(ctx, parsed, null, true, now));
    }
}
