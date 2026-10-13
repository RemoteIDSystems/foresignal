package ai.foresignal.source.adapters.space.satnogs;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.kit.KitUrls;
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
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code satnogs-stations} (prompt 077): the online ground stations of the SatNOGS Network as
 * {@code ground-station} records ({@link SatnogsParser}).
 *
 * <ul>
 *   <li><strong>Request:</strong> {@value #STATIONS_URL}. Verified 2026-10-13: the filter takes
 *       {@code status=online} ({@code status=2} and {@code Online} answer {@code 400 Select a valid choice}); the
 *       answer was a single unpaged array of 297 stations (264 KB) without a {@code Link} header.
 *   <li><strong>Paging:</strong> a Django REST Framework {@code Link: <…>; rel="next"} header (or a page's
 *       {@code next}) is followed on the same origin, at most {@value #MAX_PAGES} pages; a link to another origin
 *       is not followed and leaves the list incomplete.
 *   <li><strong>Slow upstream:</strong> the endpoint timed out from the verification host on 2026-10-10, so the
 *       fetch timeout is 120 s and the interval 6 h.
 *   <li><strong>Absence:</strong> a station missing from two consecutive complete lists (no longer online) is
 *       tombstoned; a list cut at the page cap moves nothing.
 * </ul>
 */
@Component
public class SatnogsStationsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "satnogs-stations";

    /** The online stations. */
    public static final String STATIONS_URL = "https://network.satnogs.org/api/stations/?status=online";

    /** The most pages one run follows. */
    public static final int MAX_PAGES = 20;

    /** Raw retention: station directories are {@code reference} data (90 d). */
    static final Duration RETENTION = Duration.ofDays(90);

    /** The response cap (every station, unfiltered, was 3.5 MB on 2026-10-13). */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    private final URI stationsUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public SatnogsStationsAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.stationsUri = SpaceSources.uri(STATIONS_URL, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The first page's URL. */
    public URI stationsUri() {
        return stationsUri;
    }

    /**
     * Reads every page (see the class note).
     *
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException when a page cannot be used
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<NormalizedRecord> out = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<URI> seen = new HashSet<>();
        URI next = stationsUri;
        int pages = 0;
        boolean cut = false;
        while (next != null && pages < MAX_PAGES && seen.add(next)) {
            UpstreamResponse response = ctx.http().get(next);
            SatnogsParser.Page page = SatnogsParser.parse(SpaceSources.body(ctx, response, null, next));
            pages++;
            for (NormalizedRecord r : page.records()) {
                if (ids.add(r.externalId())) {
                    out.add(r);
                }
            }
            URI link = SatnogsParser.nextLink(response.header("Link"));
            if (link == null && page.next() != null) {
                link = SatnogsParser.nextLink("<" + page.next() + ">; rel=\"next\"");
            }
            if (link != null && !KitUrls.sameOrigin(stationsUri, link)) {
                ctx.log().warn("{}: ignoring a next link to another origin: {}", ID, link.getHost());
                link = null;
                cut = true;
            }
            next = link;
        }
        boolean complete = next == null && !cut;
        ctx.log().debug("{}: {} station(s) on {} page(s), complete {}", ID, out.size(), pages, complete);
        if (!complete) {
            return FetchResult.of(out);
        }
        List<String> gone = AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.COMPLETE, ids);
        List<NormalizedRecord> all = new ArrayList<>(out);
        all.addAll(AbsenceTombstoner.tombstones(gone, SatnogsParser.KIND, ctx.now()));
        return FetchResult.complete(all);
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = SatnogsParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("SatNOGS ground stations")
                .description("The online ground stations of the SatNOGS Network (Libre Space Foundation): position,"
                        + " altitude, Maidenhead locator, antenna bands and types, client version and when each was"
                        + " last seen. A station missing from two consecutive complete lists is tombstoned.")
                .kind(kind)
                .homepage("https://network.satnogs.org/")
                .attribution("SatNOGS Network, Libre Space Foundation (CC BY-SA 4.0)")
                .licence("CC BY-SA 4.0 (network.satnogs.org/api: \"all data are distributed freely under the"
                        + " Creative Commons Attribution-Share Alike license\")")
                .termsUrl("https://network.satnogs.org/api/")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(20, 100))
                .fetchTimeout(Duration.ofSeconds(120))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .volatileFields("lastSeen")
                .titleField("name")
                .searchFields("name", "qthLocator");
        b.fieldSpec("name", new FieldSpec(FieldType.STRING, null, "Name", "The station's name", null, Set.of()));
        b.fieldSpec("status", new FieldSpec(FieldType.STRING, null, "Status", "Online, Testing or Offline", null,
                Set.of()));
        b.fieldSpec("altitudeM", new FieldSpec(FieldType.INTEGER, "m", "Altitude", "Station altitude in meters",
                "MSL", Set.of()));
        b.fieldSpec("qthLocator", new FieldSpec(FieldType.STRING, null, "Locator", "Maidenhead locator", null,
                Set.of()));
        b.fieldSpec("antennas", new FieldSpec(FieldType.ARRAY, null, "Antennas",
                "Distinct {band, type} pairs (VHF, UHF, L, S; cross-yagi, turnstile, ...)", null, Set.of()));
        b.fieldSpec("clientVersion", new FieldSpec(FieldType.STRING, null, "Client version",
                "The satnogs-client version", null, Set.of()));
        b.fieldSpec("lastSeen", new FieldSpec(FieldType.TIMESTAMP, null, "Last seen",
                "When the station last contacted the network (volatile)", null, Set.of()));
        return b.build();
    }
}
