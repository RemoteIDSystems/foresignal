package ai.foresignal.source.adapters.maritime;

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
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NGA Maritime Safety Information navigational warnings ({@code nga-msi-warnings}, prompt 088): every warning in
 * force in NAVAREAs IV and XII and the HYDROLANT, HYDROPAC and HYDROARC areas, with geometry recovered from the
 * warning text ({@link NgaMsiParser}, {@link MsiCoordinateExtractor}).
 *
 * <p><strong>Request:</strong> a conditional GET of {@value #URL} (state key {@value #STATE_KEY}). Verified
 * 2026-10-14: Foresignal's own identifiable User-Agent receives HTTP 200 (about 206 KB, 386 warnings) without a
 * browser User-Agent or a Referer, and neither is ever sent; should a block appear later, the source goes failing.
 * The response carries {@code Cache-Control: max-age} and no validators, so the request is unconditional in practice.
 *
 * <p><strong>Removal:</strong> the response is the complete active list, so a warning missing from the next complete
 * response is tombstoned at once ({@link LiveIdSet}, scope {@value #LIVE_SCOPE}). An empty list never tombstones:
 * NGA always has hundreds of warnings in force, so an empty array is treated as an upstream fault.
 *
 * <p>15-minute poll, budget 2/min and 200/day, retention 1825 days ({@code event}: navigational warnings). License:
 * US Government work, public domain.
 */
@Component
public class NgaMsiAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nga-msi-warnings";

    /** The active warnings. */
    public static final String URL = "https://msi.nga.mil/api/publications/broadcast-warn?status=active&output=json";

    /** The conditional GET state key. */
    public static final String STATE_KEY = "nga-msi-active";

    /** The {@link LiveIdSet} scope. */
    public static final String LIVE_SCOPE = NgaMsiParser.KIND;

    /** The license: US Government work. */
    public static final String LICENSE = "US-PD";

    private final URI url;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value MaritimeSources#BASE_URL_PROPERTY}; blank in production
     */
    public NgaMsiAdapter(@Value("${" + MaritimeSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.url = MaritimeSources.uri(URL, baseUrlOverride == null ? "" : baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * The request URL, rebased in tests.
     *
     * @return the URL
     */
    public URI url() {
        return url;
    }

    /**
     * Reads the active list and maps it, with tombstones for the warnings that left it.
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the body does not parse (its validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(url, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        NgaMsiParser.Result parsed;
        try {
            parsed = NgaMsiParser.parse(MaritimeSources.json(MaritimeSources.body(response, "NGA broadcast warnings"),
                    "NGA broadcast warnings"));
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        if (parsed.ids().isEmpty()) {
            ctx.log().warn("{}: the active list is empty; nothing is tombstoned", ctx.sourceId());
            return FetchResult.of(List.of());
        }
        Instant now = ctx.now();
        List<NormalizedRecord> records = new ArrayList<>(parsed.records());
        List<String> gone = LiveIdSet.absentSince(ctx.state(), LIVE_SCOPE, parsed.ids());
        for (String id : gone) {
            records.add(NormalizedRecord.tombstone(id, NgaMsiParser.KIND, now));
        }
        ctx.log().debug("{}: {} warning(s), {} without geometry, {} skipped, {} tombstone(s)", ctx.sourceId(),
                parsed.records().size(), parsed.withoutGeometry(), parsed.skipped(), gone.size());
        return FetchResult.complete(records);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NGA navigational warnings")
                .description("Navigational warnings in force from NGA Maritime Safety Information (NAVAREA IV, NAVAREA"
                        + " XII, HYDROLANT, HYDROPAC, HYDROARC), with geometry recovered from the warning text: areas"
                        + " bounded by positions, circles around a position, tracklines, or the positions themselves."
                        + " About 4% of warnings carry no position and have no geometry. A warning that leaves the"
                        + " active list is tombstoned. The text is NGA's and is stored as untrusted text.")
                .kind(NgaMsiParser.KIND)
                .homepage("https://msi.nga.mil")
                .attribution("National Geospatial-Intelligence Agency, Maritime Safety Information")
                .licence(LICENSE)
                .termsUrl("https://msi.nga.mil/api")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(2, 200))
                .maxResponseBytes(4 * 1024 * 1024)
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(MaritimeSources.EVENT_RETENTION)
                .kindRetention(NgaMsiParser.KIND, MaritimeSources.EVENT_RETENTION)
                .zeroResultsNormal(false)
                .titleField("title")
                .searchFields("text", "authority")
                .embedFields("text")
                .tileProperties("navArea", "title")
                .kindDisplay(NgaMsiParser.KIND, new KindDisplay("{title}", "warning", "#E8590C", null));
        MaritimeSources.text(b, "navArea", "NAVAREA", "P HYDROPAC, A HYDROLANT, C HYDROARC, 4 NAVAREA IV, 12 NAVAREA XII");
        MaritimeSources.text(b, "subregion", "Subregion", "The NGA subregion code(s), for example 24,25");
        MaritimeSources.integer(b, "msgYear", null, "Year", "The message year");
        MaritimeSources.integer(b, "msgNumber", null, "Number", "The message number within the area and year");
        MaritimeSources.text(b, "authority", "Authority", "The originating authority and its reference");
        MaritimeSources.text(b, "title", "Title", "The first line of the text, at most 120 characters");
        MaritimeSources.text(b, "text", "Text", "The warning text (upstream text, untrusted), at most 8,000 characters");
        MaritimeSources.array(b, "cancels", "Cancels", "The warnings this one cancels, as navArea/msgYear/msgNumber");
        MaritimeSources.bool(b, "geometryFromText", "Geometry from text", "False when the text holds no usable"
                + " position, so the record has no geometry");
        return b.build();
    }
}
