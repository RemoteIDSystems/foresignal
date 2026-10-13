package ai.foresignal.source.adapters.abusech;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Feodo Tracker botnet C2 servers (prompt 076): one {@code malware-c2} record per {@code ip:port}, placed at the
 * country's label point.
 *
 * <p><strong>Each run:</strong> one conditional GET of the list chosen by {@value #LIST_PARAM}: {@code standard} →
 * {@value #STANDARD_URL} (C2s active in the last 30 days, with metadata), {@code recommended} →
 * {@value #RECOMMENDED_URL}. Verified 2026-10-13: keyless, {@code ETag} and {@code Last-Modified}, regenerated every
 * five minutes; the site banner says the datasets are currently empty, while the standard list still served five
 * entries last online in February and March 2026, so an empty list is normal ({@code zeroResultsNormal}).
 *
 * <p><strong>Removals:</strong> an entry missing from two consecutive complete lists is tombstoned
 * ({@link AbsenceTombstoner}); an empty, well-formed list counts as complete. A 304, a non-200 answer, a truncated
 * body or a list that does not parse tombstones nothing.
 */
@Component
public class FeodoAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "abusech-feodo";

    /** The list parameter. */
    public static final String LIST_PARAM = "list";

    /** The default list. */
    public static final String STANDARD = "standard";

    /** The recommended (high-confidence) list. */
    public static final String RECOMMENDED = "recommended";

    /** The standard list. */
    public static final String STANDARD_URL = "https://feodotracker.abuse.ch/downloads/ipblocklist.json";

    /** The recommended list. */
    public static final String RECOMMENDED_URL =
            "https://feodotracker.abuse.ch/downloads/ipblocklist_recommended.json";

    /** The test-only property moving every cyber-source URL under one base as {@code {base}/{host}{path}}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cyber.base-url";

    /** Raw retention: C2 sightings are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private final String baseUrlOverride;
    private final CountryCentroids countries;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param countries the bundled country table (prompt 040)
     */
    @Autowired
    public FeodoAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride, CountryCentroids countries) {
        this.baseUrlOverride = baseUrlOverride;
        this.countries = countries;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * The URL of {@code list}.
     *
     * @param list {@value #STANDARD} or {@value #RECOMMENDED}
     */
    public URI listUri(String list) {
        return URI.create(KitUrls.rebased(RECOMMENDED.equals(list) ? RECOMMENDED_URL : STANDARD_URL,
                baseUrlOverride));
    }

    /**
     * The {@code getConditional} state key of {@code list}.
     *
     * @param list the list name
     */
    public static String stateKey(String list) {
        return "list-" + (RECOMMENDED.equals(list) ? RECOMMENDED : STANDARD);
    }

    /**
     * Fetches the chosen list once (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a bad status, a truncated body or a list that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        String list = ctx.params() != null && ctx.params().has(LIST_PARAM) ? ctx.params().string(LIST_PARAM)
                : STANDARD;
        String key = stateKey(list);
        UpstreamResponse response = ctx.http().getConditional(listUri(list), key);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), key);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the blocklist answered HTTP " + response.status()
                    : ": the blocklist exceeds the response cap"), "");
        }
        FeodoParser.Blocklist blocklist;
        try {
            blocklist = FeodoParser.parse(response.body(), countries == null ? c -> java.util.Optional.empty()
                    : countries::byIso2);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), key);
            throw e;
        }
        ctx.log().debug("{}: {} C2 entr(ies) on the {} list, skipped {}", ctx.sourceId(), blocklist.records().size(),
                list, blocklist.skipped());
        List<String> gone = AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.COMPLETE,
                blocklist.records().stream().map(NormalizedRecord::externalId).toList());
        List<NormalizedRecord> out = new ArrayList<>(blocklist.records());
        out.addAll(AbsenceTombstoner.tombstones(gone, FeodoParser.KIND, ctx.now()));
        return FetchResult.complete(out);
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = FeodoParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("abuse.ch Feodo Tracker botnet C2")
                .description("Botnet command-and-control servers (Emotet, QakBot, Dridex and others) from abuse.ch"
                        + " Feodo Tracker: IP, port, status, host name, AS, country, malware family, first seen and"
                        + " last online. Placed at the country's label point (geometryApproximate), never at the"
                        + " server's real location. An entry missing from two consecutive lists is tombstoned; an"
                        + " empty list is normal.")
                .kind(kind)
                .homepage("https://feodotracker.abuse.ch/blocklist/")
                .attribution("abuse.ch Feodo Tracker")
                .licence("CC0 (\"All datasets offered by Feodo Tracker can be used for both, commercial and"
                        + " non-commercial purpose without any limitations\", feodotracker.abuse.ch/blocklist)")
                .termsUrl("https://abuse.ch/terms-of-use/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(2, 200))
                .zeroResultsNormal(true)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("ip")
                .searchFields("ip", "hostname", "asName", "malware", "country")
                .tileProperties("malware", "status", "port")
                .param(ParamSpec.enumerated(LIST_PARAM, "Which Feodo Tracker list to read: standard (C2s active in"
                        + " the last 30 days, with metadata) or recommended", STANDARD, List.of(STANDARD, RECOMMENDED)))
                .kindDisplay(kind, new KindDisplay("{malware} C2 {ip}:{port}", "danger", "#C92A2A", null));
        text(b, "ip", "IP", "The C2 server's IP address");
        b.fieldSpec("port", new FieldSpec(FieldType.INTEGER, null, "Port", "The C2 port", null, Set.of()));
        text(b, "status", "Status", "online or offline, as Feodo Tracker last saw it");
        text(b, "hostname", "Host name", "The server's host name, when known");
        b.fieldSpec("asn", new FieldSpec(FieldType.INTEGER, null, "AS number", "The autonomous system number", null,
                Set.of()));
        text(b, "asName", "AS name", "The autonomous system's name");
        text(b, "country", "Country", "ISO 3166-1 alpha-2 country code");
        text(b, "malware", "Malware", "The malware family");
        b.fieldSpec("firstSeen", new FieldSpec(FieldType.TIMESTAMP, null, "First seen", "When Feodo Tracker first saw"
                + " the C2 (UTC)", null, Set.of()));
        text(b, "lastOnline", "Last online", "The last day the C2 was seen online (yyyy-MM-dd)");
        b.fieldSpec("geometryApproximate", new FieldSpec(FieldType.BOOLEAN, null, "Approximate position", "True:"
                + " the point is the country's label point, not the server's location", null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
