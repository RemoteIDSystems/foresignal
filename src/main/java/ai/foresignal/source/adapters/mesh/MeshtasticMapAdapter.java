package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code meshtastic-map} (prompt 083): the community Meshtastic map's node list, at most once a day.
 *
 * <ul>
 *   <li><strong>Request:</strong> one conditional GET of {@value #URL} per run (about 29 MB, verified 2026-10);
 *       interval and minimum interval 24 h; budget 1 a minute and 2 a day; responses up to 64 MB.
 *   <li><strong>Parsing:</strong> {@link MeshtasticMapParser}, streamed one node at a time.
 *   <li><strong>Records:</strong> {@code mesh-node} snapshots; {@code observedAt} is the map's {@code updated_at}. No
 *       tombstones: a node that stops reporting expires after the TTL.
 *   <li><strong>TTL:</strong> 30 h, not the 6 h of the streaming sources: a daily poll needs
 *       {@code keyframe ≤ ttl − 24 h − 30 s}, which 6 h cannot satisfy.
 *   <li><strong>Terms:</strong> a community hobby service ("Meshtastic Map by Liam Cottle") with no stated license.
 * </ul>
 */
@Component
public class MeshtasticMapAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "meshtastic-map";

    /** The node list. */
    public static final String URL = "https://meshtastic.liamcottle.net/api/v1/nodes";

    /** The test-only property moving the URL under one base as {@code {base}/{host}{path}}; blank in production. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.mesh.base-url";

    /** The response cap. */
    static final int MAX_RESPONSE_BYTES = 64 * 1024 * 1024;

    /** The conditional-GET state key. */
    static final String STATE_KEY = ID;

    /** The record TTL (see the class note). */
    static final Duration TTL = Duration.ofHours(30);

    /** The keyframe: under {@code ttl − 24 h − 30 s}. */
    static final Duration KEYFRAME = Duration.ofHours(5);

    private final URI uri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public MeshtasticMapAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.uri = URI.create(KitUrls.rebased(URL, baseUrlOverride));
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The node list URL. */
    public URI uri() {
        return uri;
    }

    /**
     * Fetches the list conditionally and maps it.
     *
     * @throws UpstreamFormatException when the body is truncated or does not parse (its validators are forgotten)
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(uri, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(ID + ": " + uri.getHost() + uri.getPath()
                    + (response.status() != 200 ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        MeshtasticMapParser.Counts counts;
        try {
            counts = MeshtasticMapParser.parse(new ByteArrayInputStream(response.body()), records::add);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        if (counts.skipped() > 0) {
            ctx.log().info("{}: skipped {} nodes without an id or a time", ID, counts.skipped());
        }
        return FetchResult.of(records).withCoverage(new Coverage(null, counts.records() + counts.skipped(),
                counts.records(), null));
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Meshtastic community map (daily)")
                .description("Meshtastic nodes from the community map at meshtastic.liamcottle.net (a hobby service"
                        + " fed by the public MQTT broker), read once a day: name, role, hardware, firmware, battery and"
                        + " the last shared position with its precision. About 29 MB per read, parsed as a stream.")
                .kinds(MeshRecords.NODE_KIND)
                .homepage("https://meshtastic.liamcottle.net/")
                .attribution("Meshtastic Map by Liam Cottle")
                .licence("unspecified (community service)")
                .termsUrl("https://github.com/liamcottle/meshtastic-map")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(24), Duration.ofHours(24))
                .defaultBudget(Budget.of(1, 2))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .zeroResultsNormal(false)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(Duration.ofSeconds(180));
        MeshRecords.defaults(b, TTL, KEYFRAME);
        return b.build();
    }
}
