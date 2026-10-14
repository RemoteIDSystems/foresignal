package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Pilot reports from the Aviation Weather Center ({@code awc-pireps}, prompt 087):
 * {@code /api/data/pirep?format=geojson&age=2&bbox=…}, one request per run.
 *
 * <p><strong>The box is mandatory upstream:</strong> without {@code bbox} (or a station and radius) AWC answers 400
 * "Must specify bounding box or stations ID and radial distance" (verified 2026-10-14). The {@value #BBOX_PARAM}
 * parameter is Foresignal's usual {@code west,south,east,north} and defaults to CONUS
 * ({@value #DEFAULT_BBOX}); it is sent in AWC's {@code lat0,lon0,lat1,lon1} order ({@link AwcSupport#bboxLatLon}).
 * AWC returns at most 400 reports per query, so a smaller box keeps busy periods complete.
 *
 * <p>One {@code pirep} record per report ({@link PirepParser}); reports age out through the 2-hour TTL anchored on the
 * observation time, matching the 2-hour {@code age} window; nothing is tombstoned. 5-minute poll, never under 2
 * minutes, budget 1/min and 500/day. Retention 90 days ({@code bulk-event}).
 */
@Component
public class AwcPirepsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "awc-pireps";

    /** The record kind. */
    public static final String KIND = PirepParser.KIND;

    /** The product path. */
    public static final String PATH = "/api/data/pirep";

    /** The box parameter. */
    public static final String BBOX_PARAM = "bbox";

    /** The default box: CONUS, west,south,east,north. */
    public static final String DEFAULT_BBOX = "-130,20,-60,55";

    /** The {@code age} window, hours. */
    static final int AGE_HOURS = 2;

    /** A report stays current 2 hours after its observation. */
    static final Duration TTL = Duration.ofHours(2);

    /** Retention: the {@code bulk-event} class. */
    static final Duration RETENTION = Duration.ofDays(90);

    private static final Envelope CONUS = new Envelope(-130, -60, 20, 55);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value AwcSupport#BASE_URL_PROPERTY}; blank means the real site
     */
    public AwcPirepsAdapter(@Value("${" + AwcSupport.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = AwcSupport.baseUrl(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request for {@code params}: always with a box, the parameter's or CONUS. */
    URI request(ValidatedParams params) {
        Envelope box = params != null && params.has(BBOX_PARAM) ? params.bbox(BBOX_PARAM) : CONUS;
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("format", "geojson");
        query.put("age", AGE_HOURS);
        query.put("bbox", AwcSupport.bboxLatLon(box));
        return AwcSupport.uri(baseUrl, PATH, query);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().get(request(ctx.params()));
        AwcHazardParser.Parsed parsed = PirepParser.parse(AwcSupport.features(AwcSupport.body(response,
                "AWC pirep"), "AWC pirep"));
        ctx.log().debug("{}: {} report(s), {} skipped", ctx.sourceId(), parsed.records().size(), parsed.skipped());
        return FetchResult.complete(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = AwcSupport.builder(ID, "AWC pilot reports",
                "Pilot reports (PIREPs and AIREPs) of the last two hours inside a box, from the NOAA Aviation"
                        + " Weather Center. AWC limits every client to 100 requests per minute and returns at most 400"
                        + " reports per query; the default 5-minute poll uses one request.", KIND)
                .mode(Mode.POLLING)
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.BBOX, "The box to read, west,south,east,north"
                        + " (default CONUS); AWC requires a box", DEFAULT_BBOX))
                .interval(AwcSupport.MIN_INTERVAL, Duration.ofMinutes(5))
                .defaultBudget(Budget.of(1, 500))
                .maxResponseBytes(4 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .zeroResultsNormal(true)
                .titleField("raw")
                .searchFields("raw", "acType", "turbulence", "icing")
                .tileProperties("fltLvl", "acType", "turbulence", "icing", "reportType")
                .kindDisplay(KIND, new KindDisplay("{acType} FL{fltLvl}", "plane", "#7048E8", null));
        AwcSupport.integer(b, "fltLvl", null, "Flight level", "Flight level in hundreds of feet");
        AwcSupport.text(b, "acType", "Aircraft", "ICAO aircraft type designator");
        AwcSupport.text(b, "turbulence", "Turbulence", "Turbulence intensity and type, for example MOD CHOP");
        AwcSupport.text(b, "icing", "Icing", "Icing intensity and type, for example LGT RIME");
        AwcSupport.text(b, "skyCover", "Sky cover", "Reported cloud covers in order, for example BKN OVC");
        AwcSupport.text(b, "wx", "Weather", "Reported weather");
        AwcSupport.text(b, "raw", "Report", "The raw report (upstream text, untrusted)");
        AwcSupport.text(b, "reportType", "Report type", "PIREP, Urgent PIREP or AIREP");
        return b.build();
    }
}
