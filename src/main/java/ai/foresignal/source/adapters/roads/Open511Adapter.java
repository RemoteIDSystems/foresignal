package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
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
 * One {@code open511-*} source (prompt 074): an Open511 server's current events as {@code road-event} records
 * ({@link Open511Parser}).
 *
 * <p>Each run requests the server's {@code /events} URL and follows {@code pagination.next_url} (resolved against
 * the page, same scheme, host and port only) for at most {@value #MAX_PAGES} pages. A set still unfinished after the
 * last page is returned partial and tombstones nothing by absence. Interval 5 min (at least 2 min), budget 4/min and
 * 1,500/day, 16 MB per page.
 */
public class Open511Adapter implements PollingAdapter {

    /** The most pages one run follows. */
    public static final int MAX_PAGES = 10;

    /** The default interval. */
    public static final Duration INTERVAL = Duration.ofMinutes(5);

    /** The shortest interval. */
    public static final Duration MIN_INTERVAL = Duration.ofMinutes(2);

    /** The default budget. */
    public static final Budget BUDGET = Budget.of(4, 1500);

    /** The response cap per page. */
    public static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    private final Open511Server server;
    private final SourceDescriptor descriptor;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates the source.
     *
     * @param server the reviewed server
     * @param meters supplies the registry of the skip counter, or null
     */
    public Open511Adapter(Open511Server server, Supplier<MeterRegistry> meters) {
        this.server = Objects.requireNonNull(server);
        this.meters = meters;
        this.descriptor = RoadEventDescriptors.builder(server.id(), server.name(),
                        "Current road events (construction, incidents, special events, road and weather conditions)"
                                + " from the Open511 server of " + server.jurisdiction() + ", as road-event records. An"
                                + " event missing from two consecutive complete fetches is tombstoned; an ARCHIVED"
                                + " event is tombstoned at once.",
                        MIN_INTERVAL, INTERVAL, BUDGET, MAX_RESPONSE_BYTES)
                .homepage(server.homepage())
                .attribution(server.attribution())
                .licence(server.license())
                .termsUrl(server.termsUrl())
                // Off until enabled: api.open511.gov.bc.ca, the one reviewed server, answers 403 on every path to
                // non-browser clients (verified 2026-10-22).
                .autoEnable(false)
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The server. */
    public Open511Server server() {
        return server;
    }

    /**
     * Reads every page of current events (see the class note).
     *
     * @return the events and tombstones; partial when more than {@value #MAX_PAGES} pages remain
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a non-200 or truncated page, a page that does not parse, or a next link to
     *     another server
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        URI first = server.eventsUrl();
        URI page = first;
        ParsedEvents all = new ParsedEvents();
        boolean complete = false;
        for (int n = 1; n <= MAX_PAGES; n++) {
            byte[] body = RoadFetch.body(ctx, ctx.http().get(page, Map.of("Accept", "application/json")),
                    server.id(), null, null);
            ParsedEvents parsed;
            try {
                parsed = Open511Parser.parse(body, server.language(), now);
            } catch (UpstreamFormatException e) {
                AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
                throw e;
            }
            all.addAll(parsed);
            if (parsed.nextUrl() == null) {
                complete = true;
                break;
            }
            URI next = page.resolve(parsed.nextUrl());
            if (!KitUrls.sameOrigin(first, next)) {
                throw new UpstreamFormatException(server.id() + ": next_url leaves the server (" + next.getHost()
                        + ")", "");
            }
            page = next;
        }
        RoadFetch.count(meters, ctx.sourceId(), all.skipped());
        List<NormalizedRecord> records = RoadFetch.records(ctx, all, null, complete, now);
        ctx.log().debug("{}: {} event(s), {} ended, skipped {}", ctx.sourceId(), all.ids().size(),
                all.endedIds().size(), all.skipped());
        if (!complete) {
            ctx.log().warn("{}: more than {} pages of events; returning a partial set", ctx.sourceId(), MAX_PAGES);
            return FetchResult.of(records);
        }
        return FetchResult.complete(records);
    }
}
