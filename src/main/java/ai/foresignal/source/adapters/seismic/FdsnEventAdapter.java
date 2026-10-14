package ai.foresignal.source.adapters.seismic;

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
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * One reviewed FDSN event service (prompt 089): a polling GET of {@code format=text}, with {@code updatedafter}
 * where the service supports it and a {@code now-2h} window otherwise.
 *
 * <p>The text format carries no update time, so the source's time base is {@link ObservedAtMode#RECEIVED}. The
 * cursor ({@code fdsn:lastUpdate}) advances to {@code now-5 min} after each successful run, giving a small overlap
 * so an event published just before the previous run's response is still caught.
 */
public final class FdsnEventAdapter implements PollingAdapter {

    /** The adapter-state key of the update cursor. */
    static final String CURSOR_KEY = "fdsn:lastUpdate";

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

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().get(query(ctx));
        if (response.status() != 200) {
            throw new UpstreamFormatException("The FDSN service " + service.id() + " answered HTTP "
                    + response.status(), "");
        }
        List<NormalizedRecord> records = FdsnTextParser.parse(response.body());
        ctx.state().put(CURSOR_KEY, ctx.now().minus(OVERLAP).truncatedTo(ChronoUnit.SECONDS).toString());
        return FetchResult.of(records);
    }

    private URI query(FetchContext ctx) {
        String base = service.baseUrl().toString();
        StringBuilder sb = new StringBuilder(base);
        sb.append(base.contains("?") ? '&' : '?').append("format=text&limit=500&orderby=time");
        if (service.supportsUpdatedAfter()) {
            String from = ctx.state().get(CURSOR_KEY)
                    .orElseGet(() -> ctx.now().minus(WINDOW).truncatedTo(ChronoUnit.SECONDS).toString());
            sb.append("&updatedafter=").append(encode(from));
        } else {
            sb.append("&starttime=").append(encode(ctx.now().minus(WINDOW).truncatedTo(ChronoUnit.SECONDS).toString()));
        }
        return URI.create(sb.toString());
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
