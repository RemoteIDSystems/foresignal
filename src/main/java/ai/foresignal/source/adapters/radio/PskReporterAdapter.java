package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code pskreporter-ft8} (prompt 082): digital-mode reception reports from PSK Reporter's retrieval API, as
 * {@code radio-spot} records ({@link PskReporterParser}).
 *
 * <ul>
 *   <li><strong>Request:</strong> {@code https://retrieve.pskreporter.info/query?mode={mode}&flowStartSeconds=-{window}
 *       &rronly=1&noactive=1&rptlimit={rptlimit}&appcontact={appContact}}, plus {@code &callsign={call}} for each
 *       watched call sign: one request per call sign, one after another (none in flight together). Without call
 *       signs, one unfiltered request.
 *   <li><strong>Etiquette:</strong> PSK Reporter asks clients to query no more often than every five minutes and to
 *       name a contact; the minimum interval is 5 min and {@code appContact} (the operator's email) is required.
 *   <li><strong>Size:</strong> an unfiltered 5-minute FT8 query was about 1.8 MB (2026-10); records are capped at
 *       {@code rptlimit} per request whether or not the upstream honors it (2026-10-14: it returned 57 reports for
 *       {@code rptlimit=50}).
 *   <li><strong>Observers:</strong> the receiving station is the record's observer, never payload.
 *   <li><strong>Coverage:</strong> {@code receivers} = the {@code activeReceiver} count, actual = records. With
 *       {@code rronly=1} PSK Reporter sends no {@code activeReceiver} elements (verified 2026-10-14), so
 *       {@code receivers} is normally unset.
 * </ul>
 */
@Component
public class PskReporterAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "pskreporter-ft8";

    /** The query endpoint. */
    public static final String QUERY_URL = "https://retrieve.pskreporter.info/query";

    /** The mode parameter. */
    public static final String MODE = "mode";

    /** The watched call signs parameter. */
    public static final String CALLSIGNS = "callsigns";

    /** The window parameter, in seconds. */
    public static final String WINDOW = "windowSeconds";

    /** The report limit parameter. */
    public static final String RPTLIMIT = "rptlimit";

    /** The operator's contact parameter, sent as {@code appcontact}. */
    public static final String APP_CONTACT = "appContact";

    /** The modes accepted. */
    public static final List<String> MODES = List.of("FT8", "FT4", "WSPR", "JS8", "CW");

    /** The counter of reports the parser skipped. */
    public static final String SKIPPED_METRIC = "foresignal.pskreporter.skipped";

    /** Raw retention: PSKReporter spots are the {@code bulk-event} class (Conventions), 90 d. */
    static final Duration RETENTION = Duration.ofDays(90);

    /** The record TTL, from {@code observedAt}. */
    static final Duration TTL = Duration.ofMinutes(30);

    /** The keyframe: under {@code ttl - poll - 30 s} (30 min - 10 min - 30 s). */
    static final Duration KEYFRAME = Duration.ofMinutes(19);

    /** The most watched call signs. */
    static final int MAX_CALLSIGNS = 20;

    /** The response cap of one request (an unfiltered 5-minute query was about 1.8 MB). */
    static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String baseUrlOverride;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param baseUrlOverride the value of {@value RadioBrowserAdapter#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public PskReporterAdapter(@Value("${" + RadioBrowserAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public PskReporterAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.baseUrlOverride = baseUrlOverride;
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Checks the window and limit ranges, the contact and the call signs. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        int window = params.integer(WINDOW, 900);
        if (window < 300 || window > 3600) {
            problems.put(WINDOW, "must be 300 to 3600 seconds");
        }
        int limit = params.integer(RPTLIMIT, 2000);
        if (limit < 1 || limit > 5000) {
            problems.put(RPTLIMIT, "must be 1 to 5000");
        }
        String contact = params.string(APP_CONTACT);
        if (contact != null && !EMAIL.matcher(contact.strip()).matches()) {
            problems.put(APP_CONTACT, "must be the operator's email address (PSK Reporter asks every client for one)");
        }
        List<String> calls = params.stringList(CALLSIGNS);
        if (calls != null) {
            if (calls.size() > MAX_CALLSIGNS) {
                problems.put(CALLSIGNS, "at most " + MAX_CALLSIGNS + " call signs");
            }
            for (String call : calls) {
                if (call == null || !RadioSources.CALLSIGN.matcher(call.strip()).matches()) {
                    problems.put(CALLSIGNS, "'" + call + "' is not a call sign");
                    break;
                }
            }
        }
        return problems;
    }

    /**
     * The request URL for one call sign, or the unfiltered request when {@code callsign} is null.
     */
    public URI queryUri(ValidatedParams params, String callsign) {
        String mode = params.optionalString(MODE).orElse("FT8");
        int window = params.integer(WINDOW, 900);
        int limit = params.integer(RPTLIMIT, 2000);
        String contact = params.optionalString(APP_CONTACT).orElse("").strip();
        String url = QUERY_URL + "?mode=" + encode(mode) + "&flowStartSeconds=-" + window
                + "&rronly=1&noactive=1&rptlimit=" + limit + "&appcontact=" + encode(contact)
                + (callsign == null ? "" : "&callsign=" + encode(callsign.strip().toUpperCase(java.util.Locale.ROOT)));
        return RadioSources.uri(url, baseUrlOverride);
    }

    /**
     * Runs the requests one after another and merges their spots.
     *
     * @throws AdapterException when a request fails or the budget refuses one
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        int limit = params.integer(RPTLIMIT, 2000);
        List<String> calls = params.stringList(CALLSIGNS);
        List<String> targets = new ArrayList<>();
        if (calls == null || calls.isEmpty()) {
            targets.add(null);
        } else {
            calls.stream().map(String::strip).distinct().forEach(targets::add);
        }
        Map<String, NormalizedRecord> spots = new LinkedHashMap<>();
        Map<String, Integer> skipped = new TreeMap<>();
        int receivers = 0;
        for (String call : targets) {
            URI uri = queryUri(params, call);
            UpstreamResponse response = ctx.http().get(uri);
            PskReporterParser.Result parsed = PskReporterParser.parse(RadioSources.body(ctx, response, null, uri), limit);
            parsed.skipped().forEach((reason, n) -> skipped.merge(reason, n, Integer::sum));
            for (NormalizedRecord r : parsed.records()) {
                spots.putIfAbsent(r.externalId(), r);
            }
            receivers += parsed.activeReceivers();
            if (parsed.capped()) {
                ctx.log().debug("{}: {} reports capped at rptlimit {}", ID, parsed.reports(), limit);
            }
        }
        SkipCounter.count(meters, SKIPPED_METRIC, "PSK Reporter reception reports the parser skipped", ctx.sourceId(),
                skipped);
        List<NormalizedRecord> records = new ArrayList<>(spots.values());
        return FetchResult.of(records).withCoverage(
                new Coverage(receivers > 0 ? receivers : null, null, records.size(), null));
    }

    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = PskReporterParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("PSK Reporter spots")
                .description("Digital-mode reception reports (FT8 by default; FT4, WSPR, JS8 or CW) from PSK Reporter,"
                        + " for the last windowSeconds, optionally only for watched call signs (one request each)."
                        + " One record per sender, receiver, frequency (kHz) and time slot; the point is the center"
                        + " of the sender's locator cell. The receiving station is an observer (include=observer),"
                        + " never payload. PSK Reporter asks for no more than one query every five minutes and for a"
                        + " contact: appContact (the operator's email) is required.")
                .kind(kind)
                .homepage("https://pskreporter.info/")
                .attribution("PSK Reporter (pskreporter.info), Philip Gladstone N1DQ")
                .licence("PSK Reporter terms (non-commercial, attribution)")
                .termsUrl("https://pskreporter.info/pskdev.html")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(10))
                .defaultBudget(Budget.of(2, 300))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .titleField("sender")
                .tileProperties("sender", "mode", "band", "snr")
                .searchFields("sender")
                .entityKey("callsign", "sender")
                .entityKeyPrefix("callsign", "ham")
                .param(ParamSpec.enumerated(MODE, "The digital mode: FT8, FT4, WSPR, JS8 or CW", "FT8", MODES))
                .param(ParamSpec.optional(CALLSIGNS, ParamType.STRING_LIST, "Watched call signs; one request each."
                        + " Empty means one unfiltered query", List.of()))
                .param(ParamSpec.optional(WINDOW, ParamType.INTEGER, "How far back each query reaches, 300 to 3600"
                        + " seconds", 900))
                .param(ParamSpec.optional(RPTLIMIT, ParamType.INTEGER, "The most reports per request, at most 5000",
                        2000))
                .param(ParamSpec.required(APP_CONTACT, ParamType.STRING, "The operator's email address, sent as"
                        + " appcontact so PSK Reporter can reach whoever runs this client"));
        RadioSources.field(b, "sender", FieldType.STRING, null, "Sender", "The transmitting station's call sign");
        RadioSources.field(b, "senderLocator", FieldType.STRING, null, "Sender locator", "The sender's Maidenhead"
                + " locator as reported (up to 10 characters)");
        RadioSources.field(b, "mode", FieldType.STRING, null, "Mode", "The digital mode (FT8, FT4, WSPR, JS8, CW)");
        RadioSources.field(b, "frequencyHz", FieldType.INTEGER, "Hz", "Frequency", "The reception frequency");
        RadioSources.field(b, "band", FieldType.STRING, null, "Band", "The amateur band of the frequency (20m, 40m,"
                + " ...)");
        RadioSources.field(b, "snr", FieldType.INTEGER, "dB", "SNR", "The signal-to-noise ratio the receiver"
                + " reported");
        RadioSources.field(b, "dxcc", FieldType.STRING, null, "DXCC entity", "The sender's DXCC entity name");
        RadioSources.field(b, "receiverLocator", FieldType.STRING, null, "Receiver locator", "The receiving station's"
                + " Maidenhead locator (its call sign is an observer)");
        b.kindDisplay(kind, new KindDisplay("{sender} {band} {mode}", "antenna", "#5C940D", null));
        return b.build();
    }
}
