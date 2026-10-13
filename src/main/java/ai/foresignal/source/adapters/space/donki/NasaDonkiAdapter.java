package ai.foresignal.source.adapters.space.donki;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code nasa-donki} (prompt 077): NASA's Space Weather Database Of Notifications, Knowledge, Information (DONKI)
 * as {@code space-weather-event} and {@code space-weather-notice} records ({@link DonkiParser}).
 *
 * <p>Verified 2026-10: the old {@code kauai.ccmc.gsfc.nasa.gov/DONKI/WS/get/*} endpoints redirect (301) to a CCMC
 * announcement page and are never contacted; the documented API is {@value #API_BASE}{@code {type}?startDate=
 * yyyy-MM-dd&endDate=yyyy-MM-dd} (OpenAPI at {@code /DONKI-API/v3/api-docs}), keyless and without {@code ETag}. It
 * refuses a range longer than 60 days ({@code 400 API Error: Date range cannot exceed 60 days}), so
 * {@code lookbackDays} is 1 to 60.
 *
 * <p><strong>Each run:</strong> one request per type in {@code types} (default {@code FLR, CME, GST, IPS, SEP})
 * over the last {@code lookbackDays} (default 7) UTC days. A type that fails is logged and skipped while the others
 * are kept; the run fails only when every type failed.
 */
@Component
public class NasaDonkiAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nasa-donki";

    /** The API base; the type is appended. */
    public static final String API_BASE = "https://ccmc.gsfc.nasa.gov/DONKI-API/get/";

    /** The {@code types} parameter. */
    public static final String TYPES_PARAM = "types";

    /** The {@code lookbackDays} parameter. */
    public static final String LOOKBACK_PARAM = "lookbackDays";

    /** The default types. */
    public static final List<String> DEFAULT_TYPES = List.of("FLR", "CME", "GST", "IPS", "SEP");

    /** The default look-back. */
    public static final int DEFAULT_LOOKBACK_DAYS = 7;

    /** The longest look-back the API accepts. */
    public static final int MAX_LOOKBACK_DAYS = 60;

    /** Raw retention: space weather events are the {@code event} class (1825 d). */
    static final Duration RETENTION = Duration.ofDays(1825);

    private final String apiBase;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public NasaDonkiAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.apiBase = SpaceSources.uri(API_BASE, baseUrlOverride).toString();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code lookbackDays} must be 1 to 60. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params != null && params.has(LOOKBACK_PARAM) && params.integer(LOOKBACK_PARAM) != null) {
            int days = params.integer(LOOKBACK_PARAM);
            if (days < 1 || days > MAX_LOOKBACK_DAYS) {
                problems.put(LOOKBACK_PARAM, "must be between 1 and " + MAX_LOOKBACK_DAYS + " (the API refuses longer"
                        + " ranges)");
            }
        }
        return problems;
    }

    /** The request URL of one type over {@code [start, end]}. */
    public URI uri(String type, LocalDate start, LocalDate end) {
        return URI.create(apiBase + type + "?startDate=" + start + "&endDate=" + end);
    }

    /**
     * Requests every configured type once (see the class note).
     *
     * @throws AdapterException when every type failed; the last failure
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        LocalDate end = LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC);
        int lookback = Math.max(1, Math.min(MAX_LOOKBACK_DAYS, ctx.params().integer(LOOKBACK_PARAM,
                DEFAULT_LOOKBACK_DAYS)));
        LocalDate start = end.minusDays(lookback);
        List<String> types = types(ctx.params());
        List<NormalizedRecord> out = new ArrayList<>();
        Exception last = null;
        int failed = 0;
        for (String type : types) {
            URI uri = uri(type, start, end);
            try {
                byte[] body = SpaceSources.body(ctx, ctx.http().get(uri), null, uri);
                DonkiParser.Parsed parsed = DonkiParser.parse(type, body);
                out.addAll(parsed.records());
                ctx.log().debug("{}: {} {} record(s), skipped {}", ID, type, parsed.records().size(), parsed.skipped());
            } catch (AdapterException | UpstreamFormatException e) {
                failed++;
                last = e;
                ctx.log().warn("{}: {} failed: {}", ID, type, e.getMessage());
                if (e instanceof AdapterException a && a.httpStatus().isEmpty()) {
                    // Budget, backoff or network: the remaining types would fail the same way.
                    break;
                }
            }
        }
        if (failed > 0 && out.isEmpty()) {
            if (last instanceof AdapterException a) {
                throw a;
            }
            throw (UpstreamFormatException) last;
        }
        return FetchResult.of(out);
    }

    /** The configured types, known ones only, in order, without repeats. */
    static List<String> types(ValidatedParams params) {
        List<String> given = params == null || !params.has(TYPES_PARAM) ? null : params.stringList(TYPES_PARAM);
        Set<String> out = new LinkedHashSet<>();
        for (String t : given == null || given.isEmpty() ? DEFAULT_TYPES : given) {
            if (t != null && DonkiParser.TYPES.contains(t.strip())) {
                out.add(t.strip());
            }
        }
        return List.copyOf(out);
    }

    private static SourceDescriptor buildDescriptor() {
        String event = DonkiParser.EVENT_KIND;
        String notice = DonkiParser.NOTICE_KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NASA DONKI space weather events")
                .description("NASA CCMC DONKI space weather events (solar flares, CMEs and CME analyses, geomagnetic"
                        + " storms, interplanetary shocks, solar energetic particles, magnetopause crossings,"
                        + " radiation belt enhancements, high-speed streams, WSA-ENLIL simulations) and"
                        + " notifications, with their linked events. No geometry. Keyless; one request per type"
                        + " per run over the last lookbackDays (1 to 60).")
                .kinds(event, notice)
                .homepage("https://ccmc.gsfc.nasa.gov/tools/DONKI/")
                .attribution("NASA Community Coordinated Modeling Center (CCMC), DONKI")
                .licence("NASA public data")
                .termsUrl("https://ccmc.gsfc.nasa.gov/tools/DONKI/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(30), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(12, 400))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(event, RETENTION)
                .kindRetention(notice, RETENTION)
                .titleField("type")
                .searchFields("type", "classType", "sourceLocation", "messageType")
                .embedFields("messageBody")
                .param(new ParamSpec(TYPES_PARAM, ParamType.STRING_LIST, "DONKI types requested, one request each per"
                        + " run: FLR, CME, CMEAnalysis, GST, IPS, SEP, MPC, RBE, HSS, WSAEnlilSimulations,"
                        + " notifications", DEFAULT_TYPES, false, DonkiParser.TYPES))
                .param(ParamSpec.optional(LOOKBACK_PARAM, ParamType.INTEGER, "Days back each run reads, 1 to 60",
                        DEFAULT_LOOKBACK_DAYS));
        Set<String> both = Set.of(event, notice);
        b.fieldSpec("type", new FieldSpec(FieldType.STRING, null, "Type", "The DONKI type (FLR, CME, GST, …,"
                + " notifications)", null, both));
        b.fieldSpec("catalog", spec(FieldType.STRING, null, "Catalog", "The DONKI catalog (M2M_CATALOG)", event));
        b.fieldSpec("beginTime", spec(FieldType.TIMESTAMP, null, "Begin", "Begin, start or event time (UTC)", event));
        b.fieldSpec("peakTime", spec(FieldType.TIMESTAMP, null, "Peak", "Flare peak time (UTC)", event));
        b.fieldSpec("endTime", spec(FieldType.TIMESTAMP, null, "End", "Flare end time (UTC)", event));
        b.fieldSpec("classType", spec(FieldType.STRING, null, "Class", "Flare class (M1.4)", event));
        b.fieldSpec("sourceLocation", spec(FieldType.STRING, null, "Source location",
                "Heliographic source location (N10E12)", event));
        b.fieldSpec("activeRegionNum", spec(FieldType.INTEGER, null, "Active region", "NOAA active region number",
                event));
        b.fieldSpec("instruments", spec(FieldType.ARRAY, null, "Instruments", "Instrument display names", event));
        b.fieldSpec("kpMax", spec(FieldType.NUMBER, "1", "Kp maximum", "Largest Kp of a geomagnetic storm", event));
        b.fieldSpec("cmeSpeedKms", spec(FieldType.NUMBER, "km/s", "CME speed", "CME speed from the most accurate"
                + " analysis", event));
        b.fieldSpec("cmeHalfAngle", spec(FieldType.NUMBER, "deg", "CME half angle", "CME half angle from the most"
                + " accurate analysis", event));
        b.fieldSpec("linkedEvents", spec(FieldType.ARRAY, null, "Linked events", "Ids of linked DONKI events",
                event));
        b.fieldSpec("link", new FieldSpec(FieldType.STRING, null, "Link", "The DONKI page", null, both));
        b.fieldSpec("versionId", spec(FieldType.INTEGER, null, "Version", "The DONKI entry version", event));
        b.fieldSpec("messageType", spec(FieldType.STRING, null, "Message type", "The notification type (CME, FLR,"
                + " Report)", notice));
        b.fieldSpec("messageBody", spec(FieldType.STRING, null, "Message", "The notification text, at most 4 KB;"
                + " upstream text, untrusted", notice));
        return b.build();
    }

    private static FieldSpec spec(FieldType type, String unit, String label, String description, String kind) {
        return new FieldSpec(type, unit, label, description, null, Set.of(kind));
    }
}
