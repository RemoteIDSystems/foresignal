package ai.foresignal.source.adapters.seismic;

import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * One reviewed FDSN event service (prompt 089): a polling GET of {@code format=text}, with {@code updatedafter}
 * where the service supports it and a {@code now-2h} window otherwise.
 *
 * <p>The text format carries no update time, so the source's time base is {@link ObservedAtMode#RECEIVED}. Each
 * run pages through the matches ({@code limit}/{@code offset}) until a page is short; HTTP 204 is no matches. The
 * cursor ({@code fdsn:lastUpdate}) moves only when a pass completes, to the pass's start minus 5 minutes, giving a
 * small overlap so an event published just before the previous pass is still caught.
 */
public final class FdsnEventAdapter implements PollingAdapter {

    /** The adapter-state key of the update cursor. */
    static final String CURSOR_KEY = "fdsn:lastUpdate";

    /** The adapter-state key of a pass that did not finish in one run: {@code <startedAt>|<next offset>}. */
    static final String PASS_KEY = "fdsn:pass";

    /** Rows per page. */
    static final int LIMIT = 500;

    /** Pages per run; the 2-a-minute budget usually stops a long pass first, which is handled the same way. */
    static final int MAX_PAGES = 10;

    /** The overlap behind the cursor. */
    static final Duration OVERLAP = Duration.ofMinutes(5);

    /** The window used by a service without {@code updatedafter}. */
    static final Duration WINDOW = Duration.ofHours(2);

    static final Duration RETENTION = Duration.ofDays(1825);

    private final FdsnService service;
    private final SourceDescriptor descriptor;

    /** Creates the adapter for one reviewed service. */
    public FdsnEventAdapter(FdsnService service) {
        this.service = service;
        this.descriptor = build(service);
    }

    /** The reviewed service this adapter serves. */
    public FdsnService service() {
        return service;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Reads every event updated since the cursor (or in the window), page by page.
     *
     * <p>Pages of {@value #LIMIT} rows are requested with {@code offset} (1-based; verified live on GEOFON and INGV
     * 2026-10-16; GEOFON has no update-time filter and pages by start time, 2026-10-22) until a page holds fewer rows. HTTP 204 is FDSN's default {@code nodata} answer for "no matching
     * events" and is an empty, successful page; {@code nodata} is not sent, so a 404 still means a broken endpoint.
     * A pass that cannot finish in one run (the page cap, or a budget refusal after the first page) stores its place
     * ({@value #PASS_KEY}) and the next run continues it with the same {@code updatedafter}; the cursor moves only
     * when a pass completes, to the time that pass started minus {@link #OVERLAP}. The text format carries no update
     * time, so the start of the pass is the newest point known to be covered.
     *
     * @throws AdapterException when the first page fails or a later page fails for any reason but the budget
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Pass pass = Pass.read(ctx.state().get(PASS_KEY).orElse(null));
        if (pass == null) {
            pass = new Pass(ctx.now().truncatedTo(ChronoUnit.SECONDS), 1);
        }
        String since = since(ctx);
        List<NormalizedRecord> records = new ArrayList<>();
        int offset = pass.offset();
        for (int page = 0; page < MAX_PAGES; page++) {
            UpstreamResponse response;
            try {
                response = ctx.http().get(query(ctx, since, offset));
            } catch (BudgetExceededException e) {
                if (page == 0) {
                    throw e;
                }
                keep(ctx, new Pass(pass.startedAt(), offset));
                ctx.log().info("{}: the budget stopped the read at offset {}; the next run continues",
                        ctx.sourceId(), offset);
                return FetchResult.of(records);
            }
            int rows;
            if (response.status() == 204) {
                rows = 0;
            } else if (response.status() == 200) {
                records.addAll(FdsnTextParser.parse(response.body()));
                rows = FdsnTextParser.rows(response.body());
            } else {
                // An unexpected status is a failed request, not an unparsable body (the @throws contract above).
                throw new AdapterException("The FDSN service " + service.id() + " answered HTTP "
                        + response.status());
            }
            if (rows < LIMIT) {
                if (service.supportsUpdatedAfter()) {
                    ctx.state().put(CURSOR_KEY, pass.startedAt().minus(OVERLAP).toString());
                }
                if (ctx.state().get(PASS_KEY).isPresent()) {
                    ctx.state().remove(PASS_KEY);
                }
                return FetchResult.of(records);
            }
            offset += rows;
        }
        keep(ctx, new Pass(pass.startedAt(), offset));
        ctx.log().info("{}: {} pages read; the next run continues at offset {}", ctx.sourceId(), MAX_PAGES, offset);
        return FetchResult.of(records);
    }

    /** Stores a pass in progress; a service without {@code updatedafter} re-reads its moving window instead. */
    private void keep(FetchContext ctx, Pass pass) {
        if (service.supportsUpdatedAfter()) {
            ctx.state().put(PASS_KEY, pass.write());
        }
    }

    /** The {@code updatedafter} value (the cursor, else the window), or for services without it the start time. */
    private String since(FetchContext ctx) {
        String window = ctx.now().minus(WINDOW).truncatedTo(ChronoUnit.SECONDS).toString();
        return service.supportsUpdatedAfter() ? ctx.state().get(CURSOR_KEY).orElse(window) : window;
    }

    private URI query(FetchContext ctx, String since, int offset) {
        String base = service.baseUrl().toString();
        StringBuilder sb = new StringBuilder(base);
        sb.append(base.contains("?") ? '&' : '?').append("format=text&limit=").append(LIMIT)
                .append("&offset=").append(offset).append("&orderby=time");
        // FDSN times carry no zone (UTC is implied); INGV answers 400 to the trailing Z that Instant adds.
        String zoneless = since.endsWith("Z") ? since.substring(0, since.length() - 1) : since;
        sb.append(service.supportsUpdatedAfter() ? "&updatedafter=" : "&starttime=").append(encode(zoneless));
        return URI.create(sb.toString());
    }

    /**
     * A pass in progress.
     *
     * @param startedAt when the pass's first page was requested
     * @param offset the next 1-based offset
     */
    record Pass(Instant startedAt, int offset) {

        /** The stored form {@code <startedAt>|<offset>}. */
        String write() {
            return startedAt + "|" + offset;
        }

        /** The pass stored by {@link #write}, or null when none is stored or it cannot be read. */
        static Pass read(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                int bar = value.indexOf('|');
                return new Pass(Instant.parse(value.substring(0, bar)),
                        Math.max(1, Integer.parseInt(value.substring(bar + 1))));
            } catch (RuntimeException e) {
                return null;
            }
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static SourceDescriptor build(FdsnService service) {
        return SourceDescriptor.builder(service.sourceId())
                .name(service.name() + " FDSN earthquakes")
                .description("Earthquakes from the FDSN event service " + service.baseUrl()
                        + " (format=text, updatedafter where supported). " + (service.notes() == null ? "" : service.notes()))
                .kind(SeismicRecords.KIND)
                .mode(Mode.POLLING)
                .homepage(service.homepage() == null ? null : service.homepage().toString())
                .attribution(service.attribution())
                .licence(service.licence())
                .termsUrl(service.termsUrl() == null ? null : service.termsUrl().toString())
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(2, 600)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .searchFields("place", "magType")
                .zeroResultsNormal(true)
                .kindDisplay(SeismicRecords.KIND, new KindDisplay("M{mag} {place}", "circle", "#E03131", null))
                .build();
    }
}
