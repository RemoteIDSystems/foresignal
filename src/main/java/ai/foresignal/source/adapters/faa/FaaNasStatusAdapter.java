package ai.foresignal.source.adapters.faa;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Airport delay programs from the FAA NAS status page ({@code faa-nas-status}, prompt 087):
 * {@code GET https://nasstatus.faa.gov/api/airport-status-information}, an XML document (not the JSON the catalog
 * once said), one request per run, parsed by {@link NasStatusParser} on the bundled {@link UsAirports} table.
 *
 * <p>One {@code airport-delay} record per airport and delay kind; the document is the complete current set, so a
 * program missing from it (cleared) is tombstoned at once ({@link LiveIdSet}). No delays at all is normal.
 * 5-minute poll, budget 2/min and 600/day, retention 1825 days ({@code event}).
 */
@Component
public class FaaNasStatusAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "faa-nas-status";

    /** The record kind. */
    public static final String KIND = NasStatusParser.KIND;

    /** The document URL. */
    public static final String URL = "https://nasstatus.faa.gov/api/airport-status-information";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final Supplier<UsAirports> airports;

    /**
     * Creates the adapter for Spring, with the bundled airport table.
     *
     * @param baseUrlOverride the value of {@value FaaSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public FaaNasStatusAdapter(@Value("${" + FaaSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(baseUrlOverride, UsAirports::bundled);
    }

    /**
     * Creates the adapter with an airport table (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param airports supplies the airport table
     */
    public FaaNasStatusAdapter(String baseUrlOverride, Supplier<UsAirports> airports) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride;
        this.airports = airports;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The document URL, rebased in tests. */
    URI uri() {
        return FaaSources.uri(URL, baseUrl);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        byte[] body = FaaSources.body(ctx.http().get(uri()), "NAS status");
        NasStatusParser.Parsed parsed = NasStatusParser.parse(body, airports.get(), ctx.now());
        List<NormalizedRecord> out = new ArrayList<>(parsed.records());
        List<String> ids = parsed.records().stream().map(NormalizedRecord::externalId).toList();
        for (String gone : LiveIdSet.absentSince(ctx.state(), KIND, ids)) {
            out.add(NormalizedRecord.tombstone(gone, KIND, ctx.now()));
        }
        ctx.log().debug("{}: {} delay(s), {} unknown airport(s), {} skipped, {} cleared", ctx.sourceId(),
                parsed.records().size(), parsed.unknownAirports(), parsed.skipped(),
                out.size() - parsed.records().size());
        return FetchResult.complete(out);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("FAA airport delays (NAS status)")
                .description("Ground delay programs, ground stops, arrival and departure delays, airport closures"
                        + " and deicing from the FAA NAS status page, one record per airport and delay kind, placed at"
                        + " the airport. A cleared program is tombstoned.")
                .kind(KIND)
                .homepage("https://nasstatus.faa.gov")
                .attribution(FaaSources.ATTRIBUTION)
                .licence(FaaSources.LICENSE)
                .termsUrl("https://www.faa.gov/web_policies")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(2), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(2, 600))
                .maxResponseBytes(2 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(FaaSources.EVENT_RETENTION)
                .kindRetention(KIND, FaaSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .titleField("airport")
                .searchFields("airport", "delayKind", "reason")
                .tileProperties("airport", "delayKind", "avgMin", "maxMin")
                .kindDisplay(KIND, new KindDisplay("{airport} {delayKind}", "airport", "#E67700", null));
        FaaSources.text(b, "airport", "Airport", "The FAA location identifier, for example ORD");
        FaaSources.text(b, "delayKind", "Delay kind", "ground-delay, ground-stop, arrival-delay, departure-delay,"
                + " general-delay, closure, deicing, or the block name in kebab case");
        FaaSources.text(b, "reason", "Reason", "The upstream reason (for a closure, the NOTAM text)");
        FaaSources.integer(b, "avgMin", "min", "Average delay", "Average ground delay, minutes");
        FaaSources.integer(b, "maxMin", "min", "Maximum delay", "Maximum delay, minutes");
        FaaSources.integer(b, "minMin", "min", "Minimum delay", "Minimum arrival or departure delay, minutes");
        FaaSources.text(b, "trend", "Trend", "Increasing, Decreasing or Steady");
        FaaSources.text(b, "endTime", "Ends", "The ground stop's end time as published, for example 7:45 pm EDT");
        FaaSources.text(b, "reopen", "Reopens", "A closure's reopening, ISO-8601 UTC when it reads, else as published");
        return b.build();
    }
}
