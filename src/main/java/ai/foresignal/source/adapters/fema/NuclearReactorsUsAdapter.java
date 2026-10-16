package ai.foresignal.source.adapters.fema;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.ReferenceSupport;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code nuclear-reactors-us}: FEMA's Nuclear Plant Power Reactor Status (prompt 094), updated daily from the
 * NRC. Public domain (US Government).
 *
 * <p>Ninety-nine reactors, each with a point geometry. {@code powerPct} is a significant field, so a power
 * change is a version. The NRC {@code reactors-operating.xls} is not used (it answers 403 to scripted clients
 * and has no coordinates).
 */
@Component
public class NuclearReactorsUsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nuclear-reactors-us";

    /** The production query URL. */
    public static final String QUERY_URL =
            "https://gis.fema.gov/arcgis/rest/services/Partner/Nuclear_Plant_Power_Reactor_Status/FeatureServer/0/"
                    + "query?where=1=1&outFields=*&f=geojson&outSR=4326";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nuclear-reactors-us.base-url";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "reactors";

    private final URI queryUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public NuclearReactorsUsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.queryUri = URI.create(KitUrls.rebased(QUERY_URL, baseUrlOverride));
    }

    /** The query URL this adapter fetches. */
    public URI queryUri() {
        return queryUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(queryUri, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the reactor layer answered HTTP " + response.status()
                    : ": the reactor response exceeds the cap"), "");
        }
        // Streamed feature by feature into chunks: the layer is never held as one list of records.
        ReferenceSupport.ChunkEmitter chunks = ReferenceSupport.chunks(ctx);
        ReferenceSupport.ParseCounts counts;
        try {
            counts = FemaReactorParser.parse(response.body(), chunks);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        chunks.flush();
        if (counts.skipped() > 0 || counts.badGeometry() > 0) {
            ctx.log().info("{}: {} reactor(s), {} without a license number skipped, {} malformed geometry(ies)"
                    + " kept without geometry", ID, counts.records(), counts.skipped(), counts.badGeometry());
        }
        return FetchResult.complete(List.of(), release(response));
    }

    private static String release(UpstreamResponse response) {
        String etag = response.header("ETag");
        if (etag != null && !etag.isBlank()) {
            return etag;
        }
        return response.header("Last-Modified");
    }

    /** The default interval: FEMA republishes the NRC reactor status daily (prompt 094). */
    public static final Duration INTERVAL = Duration.ofHours(24);

    /** The shortest interval allowed. */
    public static final Duration INTERVAL_MIN = Duration.ofHours(6);

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = ReferenceSupport.defaults(SourceDescriptor.builder(ID)
                .name("US nuclear reactors")
                .description("US nuclear power reactors from FEMA's Nuclear Plant Power Reactor Status layer"
                        + " (updated daily from the NRC): power plant, state, reactor type and status, the"
                        + " current power percentage, reported power, owner/operator and NRC region, with a"
                        + " point geometry. Public domain. A reactor absent from a complete run is tombstoned.")
                .kind(FemaReactorParser.KIND)
                .kind("dataset-release")
                .homepage("https://www.nrc.gov/reactors/operating/list-power-reactor-units.html")
                .attribution("Federal Emergency Management Agency (FEMA); data from the US Nuclear Regulatory"
                        + " Commission (NRC)")
                .licence("Public domain (US Government)")
                .termsUrl("https://www.fema.gov/about/openfema/data-sets")
                .titleField("reactorName")
                .searchFields("reactorName", "powerPlant", "state", "ownerOperator")
                .tileProperties("reactorName", "status", "powerPct")
                .rankField("mwe")
                .kindDisplay(FemaReactorParser.KIND, new KindDisplay("{reactorName}", "radiation", "#E03131", null)))
                // After the shared defaults, which set the weekly reference interval: FEMA republishes the NRC
                // status daily, so this source polls every 24 h (min 6 h; prompt 094, Source-Catalog).
                .interval(INTERVAL_MIN, INTERVAL);
        text(b, "reactorName", "Reactor", "The reactor name");
        text(b, "powerPlant", "Plant", "The power plant name");
        text(b, "state", "State", "The state");
        text(b, "reactorType", "Type", "The reactor type");
        text(b, "status", "Status", "The reactor status");
        b.fieldSpec("powerPct", new FieldSpec(FieldType.NUMBER, "%", "Power", "Current power output, percent", null,
                Set.of()));
        text(b, "reportDate", "Reported", "The report date");
        b.fieldSpec("mwe", new FieldSpec(FieldType.NUMBER, "MW", "Capacity", "Net electrical capacity, MW", null,
                Set.of()));
        text(b, "ownerOperator", "Owner/operator", "The owner or operator");
        text(b, "nrcRegion", "NRC region", "The NRC region");
        text(b, "eiaPlantCode", "EIA plant code", "The EIA plant code");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
