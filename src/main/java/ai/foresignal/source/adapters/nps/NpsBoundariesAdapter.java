package ai.foresignal.source.adapters.nps;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.support.ReferenceSupport;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code nps-boundaries}: NPS Land Resources Division park boundaries (prompt 094), one {@code park-boundary}
 * (Multi)Polygon per unit, using the 073 ArcGIS kit's {@link ArcGisQueryClient}.
 *
 * <p>The layer's {@code editingInfo} edit date is checked first; when it is unchanged, nothing is queried. A
 * geometry over the store's vertex limit is simplified with JTS {@code TopologyPreservingSimplifier}.
 * Informational, not a legal boundary.
 */
@Component
public class NpsBoundariesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nps-boundaries";

    /** The production FeatureServer layer. */
    public static final String LAYER_URL =
            "https://services1.arcgis.com/fBc8EJBxQRMcHlei/arcgis/rest/services/"
                    + "NPS_Land_Resources_Division_Boundary_and_Tract_Data_Service/FeatureServer/2";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nps-boundaries.base-url";

    private final URI layerUri;
    private final ArcGisQueryClient client = new ArcGisQueryClient();
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public NpsBoundariesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.layerUri = URI.create(KitUrls.rebased(LAYER_URL, baseUrlOverride));
    }

    /** The layer URL this adapter queries. */
    public URI layerUri() {
        return layerUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ArcGisQueryClient.Request request = new ArcGisQueryClient.Request(
                layerUri, "1=1", List.of("*"), 1000, 10, 50_000, 0.0005, 6, false, Map.of(), Map.of());
        FeatureFetch fetch = client.fetch(ctx, request);
        if (fetch.unchanged()) {
            return FetchResult.unchanged();
        }
        // Records go straight into chunks; no list of the dataset's records is built. (The 073 kit itself holds
        // the layer's feature trees, 442 of them, while it pages.)
        ReferenceSupport.ChunkEmitter chunks = ReferenceSupport.chunks(ctx);
        int[] badGeometry = {0};
        int skipped = 0;
        for (JsonNode feature : fetch) {
            NormalizedRecord record = NpsBoundaryParser.map(feature, () -> badGeometry[0]++);
            if (record != null) {
                chunks.accept(record);
            } else {
                skipped++;
            }
        }
        chunks.flush();
        if (skipped > 0 || badGeometry[0] > 0) {
            ctx.log().info("{}: {} unit(s), {} without UNIT_CODE skipped, {} malformed geometry(ies) kept without"
                    + " geometry", ID, chunks.emitted(), skipped, badGeometry[0]);
        }
        String release = null;
        ArcGisQueryClient.LayerMeta meta = ArcGisQueryClient.cachedMeta(ctx.state());
        if (meta != null && meta.lastEditDate() != null) {
            release = String.valueOf(meta.lastEditDate());
        }
        return fetch.complete()
                ? FetchResult.complete(List.of(), release)
                : FetchResult.of(List.of());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = ReferenceSupport.defaults(SourceDescriptor.builder(ID)
                .name("NPS park boundaries")
                .description("National Park Service unit boundaries from the Land Resources Division boundary"
                        + " layer: unit name and type, park, state and region, with a simplified (Multi)Polygon."
                        + " Informational, not a legal boundary. Public domain. A unit absent from a complete"
                        + " run is tombstoned.")
                .kind(NpsBoundaryParser.KIND)
                .kind("dataset-release")
                .homepage("https://www.nps.gov/")
                .attribution("National Park Service, Land Resources Division")
                .licence("Public domain (US Government)")
                .termsUrl("https://www.nps.gov/aboutus/disclaimer.htm")
                .titleField("unitName")
                .searchFields("unitName", "parkName", "state")
                .tileProperties("unitName", "unitType", "state")
                .kindDisplay(NpsBoundaryParser.KIND, new KindDisplay("{unitName}", "natural", "#2F9E44", null)));
        b.fieldSpec("unitName", new FieldSpec(FieldType.STRING, null, "Unit", "The unit name", null, Set.of()));
        b.fieldSpec("unitType", new FieldSpec(FieldType.STRING, null, "Unit type", "The unit type", null, Set.of()));
        b.fieldSpec("parkName", new FieldSpec(FieldType.STRING, null, "Park", "The park name", null, Set.of()));
        b.fieldSpec("state", new FieldSpec(FieldType.STRING, null, "State", "The state", null, Set.of()));
        b.fieldSpec("region", new FieldSpec(FieldType.STRING, null, "Region", "The NPS region", null, Set.of()));
        b.fieldSpec("dateEdit", new FieldSpec(FieldType.STRING, null, "Edited", "The last edit date", null, Set.of()));
        b.fieldSpec("status", new FieldSpec(FieldType.STRING, null, "Status", "The boundary status", null, Set.of()));
        return b.build();
    }
}
