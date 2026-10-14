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
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code kiwisdr} (prompt 082): the public KiwiSDR receivers, from the list {@code rx.linkfanel.net} generates from
 * {@code kiwisdr.com/public} for the dyatlov map ({@link KiwiSdrParser}).
 *
 * <ul>
 *   <li><strong>Plain HTTP:</strong> {@value #LIST_URL}. HTTPS to this host timed out when verified (2026-10), while
 *       plain HTTP answers with the whole list (about 930 KB). The data is public and not sensitive, and nothing is
 *       sent but the request line and Foresignal's User-Agent, so plain HTTP is used.
 *   <li><strong>Conditional GET:</strong> the host sends {@code ETag} and {@code Last-Modified} (verified 2026-10-14);
 *       a 304 is {@link FetchResult#unchanged()}.
 *   <li><strong>Tombstones:</strong> a receiver absent from a complete list is tombstoned
 *       ({@link LiveIdSet#absentSince}), but only when that list holds at least half as many receivers as the
 *       previous one: a list that suddenly shrank is a generator fault, not hundreds of receivers going away.
 * </ul>
 */
@Component
public class KiwiSdrAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "kiwisdr";

    /** The list. */
    public static final String LIST_URL = "http://rx.linkfanel.net/kiwisdr_com.js";

    /** The conditional GET state key, also the {@link LiveIdSet} scope. */
    public static final String STATE_KEY = "kiwisdr";

    /** The counter of entries the parser skipped. */
    public static final String SKIPPED_METRIC = "foresignal.kiwisdr.skipped";

    /** Raw retention: a receiver directory is reference data (Conventions), 90 d of history. */
    static final Duration RETENTION = Duration.ofDays(90);

    /** The list's response cap, matching {@link LenientJsLiteralParser#MAX_BYTES}. */
    static final int MAX_RESPONSE_BYTES = LenientJsLiteralParser.MAX_BYTES;

    private final URI list;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param baseUrlOverride the value of {@value RadioBrowserAdapter#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public KiwiSdrAdapter(@Value("${" + RadioBrowserAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public KiwiSdrAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.list = RadioSources.uri(LIST_URL, baseUrlOverride);
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The list URL. */
    public URI listUri() {
        return list;
    }

    /**
     * Reads the list conditionally and maps it, with tombstones for receivers gone from a plausible list.
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the body is truncated or does not parse (its validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(list, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        KiwiSdrParser.Result parsed;
        try {
            parsed = KiwiSdrParser.parse(RadioSources.body(ctx, response, STATE_KEY, list));
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        SkipCounter.count(meters, SKIPPED_METRIC, "KiwiSDR list entries the parser skipped", ctx.sourceId(),
                parsed.skipped());
        List<NormalizedRecord> records = new ArrayList<>(parsed.records());
        int previous = LiveIdSet.read(ctx.state(), LiveIdSet.LIVE_PREFIX + STATE_KEY).size();
        if (records.isEmpty() || records.size() * 2 < previous) {
            ctx.log().warn("{}: the list holds {} receivers against {} before; tombstoning nothing", ID,
                    records.size(), previous);
            return FetchResult.of(records).withCoverage(new Coverage(records.size(), previous, records.size(), null));
        }
        List<String> gone = LiveIdSet.absentSince(ctx.state(), STATE_KEY,
                records.stream().map(NormalizedRecord::externalId).toList());
        for (String id : gone) {
            records.add(NormalizedRecord.tombstone(id, KiwiSdrParser.KIND, ctx.now()));
        }
        return FetchResult.complete(records).withCoverage(
                new Coverage(parsed.records().size(), null, parsed.records().size(), null));
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = KiwiSdrParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("KiwiSDR public receivers")
                .description("The public KiwiSDR web receivers (kiwisdr.com/public), from the list rx.linkfanel.net"
                        + " generates for the dyatlov map: name, link, antenna, frequency range, capacity and whether"
                        + " each is online, at the receiver's GPS position. Fetched over plain HTTP (the host's HTTPS"
                        + " timed out when verified; the list is public). Current users and SNR are volatile. A"
                        + " receiver missing from a list at least half the previous size is tombstoned.")
                .kind(kind)
                .homepage("http://kiwisdr.com/public/")
                .attribution("KiwiSDR receiver list (kiwisdr.com, via rx.linkfanel.net)")
                .licence("unspecified (public receiver list)")
                .termsUrl("http://rx.linkfanel.net/")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(6), Duration.ofHours(24))
                .defaultBudget(Budget.of(1, 4))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.RECEIVED)
                .zeroResultsNormal(false)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .volatileFields("users", "snr")
                .titleField("name")
                .searchFields("name", "antenna")
                .tileProperties("name", "online", "usersMax");
        RadioSources.field(b, "name", FieldType.STRING, null, "Name", "The receiver's name as its owner set it");
        RadioSources.field(b, "url", FieldType.STRING, null, "Link", "The receiver's web interface");
        RadioSources.field(b, "antenna", FieldType.STRING, null, "Antenna", "The antenna as its owner described it");
        RadioSources.field(b, "bandsHz", FieldType.ARRAY, "Hz", "Frequency range", "The tunable range [low, high]");
        RadioSources.field(b, "usersMax", FieldType.INTEGER, null, "Capacity", "The most simultaneous users");
        RadioSources.field(b, "sdrHw", FieldType.STRING, null, "Hardware", "The SDR hardware and firmware");
        RadioSources.field(b, "online", FieldType.BOOLEAN, null, "Online", "True when the list says it is online");
        RadioSources.field(b, "users", FieldType.INTEGER, null, "Users", "Current users (volatile)");
        RadioSources.field(b, "snr", FieldType.ARRAY, "dB", "SNR", "The receiver's measured SNR, all bands and HF"
                + " (volatile)");
        b.kindDisplay(kind, new KindDisplay("{name}", "antenna", "#1864AB", null));
        return b.build();
    }
}
