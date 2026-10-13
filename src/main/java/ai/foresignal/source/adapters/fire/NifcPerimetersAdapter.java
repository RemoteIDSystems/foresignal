package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.kit.feature.FeatureGeometry;
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
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.AbsenceTombstoner;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * US wildfire perimeters from the NIFC WFIGS Interagency Perimeters Current layer (prompt 075), read with the
 * prompt 073 {@link ArcGisQueryClient} and mapped by {@link NifcPerimeterMapper}.
 *
 * <p><strong>Each run:</strong> the layer document is read (verified 2026-10-13: {@code maxRecordCount} 2000 and
 * {@code editingInfo} with all three edit dates); when its {@code dataLastEditDate} equals the one of the last
 * complete fetch, nothing is queried and the run is {@link FetchResult#unchanged()}. Otherwise the query runs as GEV's
 * ({@code where=1=1}, GEV's sixteen fields, {@code maxAllowableOffset=0.001}, {@code outSR=4326},
 * {@code f=geojson}), at most {@value #MAX_PAGES} pages of 2000.
 *
 * <p><strong>Absence:</strong> a perimeter missing from two consecutive complete fetches is tombstoned; a fetch cut
 * short by the page cap, a format error or an unchanged layer tombstones nothing. The layer is empty in winter
 * ({@code zeroResultsNormal}).
 */
@Component
public class NifcPerimetersAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nifc-perimeters";

    /** The production layer. */
    public static final String LAYER_URL = "https://services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/"
            + "WFIGS_Interagency_Perimeters_Current/FeatureServer/0";

    /** The page cap. */
    public static final int MAX_PAGES = 5;

    /** The page size requested (the layer's {@code maxRecordCount}). */
    public static final int PAGE_SIZE = 2000;

    /** GEV's generalization: about 100 m of boundary fidelity for a much smaller payload. */
    public static final double MAX_ALLOWABLE_OFFSET = 0.001;

    /** Raw retention: perimeters are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The response cap of one page (2000 generalized perimeters stay well below it). */
    static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    private final URI layer;
    private final Supplier<MeterRegistry> meters;
    private final ArcGisQueryClient client = new ArcGisQueryClient();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value FireSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public NifcPerimetersAdapter(@Value("${" + FireSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public NifcPerimetersAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.layer = URI.create(FireSources.rebased(LAYER_URL, baseUrlOverride));
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The layer URL in use. */
    public URI layerUrl() {
        return layer;
    }

    /**
     * Fetches the layer once (see the class note).
     *
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a redirect, a bad status or body, or an ArcGIS error body
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        FeatureFetch fetch;
        try {
            fetch = client.fetch(ctx, new ArcGisQueryClient.Request(layer, "1=1", NifcPerimeterMapper.OUT_FIELDS,
                    PAGE_SIZE, MAX_PAGES, MAX_PAGES * PAGE_SIZE, MAX_ALLOWABLE_OFFSET, null, false, Map.of(),
                    Map.of()));
        } catch (UpstreamFormatException e) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
            throw e;
        }
        if (fetch.unchanged()) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.NOT_MODIFIED, List.of());
            return FetchResult.unchanged();
        }
        FireFetch.Batch batch = FireFetch.map(fetch, NifcPerimeterMapper::map);
        return FireFetch.result(ctx, fetch, batch, NifcPerimeterMapper.KIND, true, meters);
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = NifcPerimeterMapper.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NIFC wildfire perimeters (WFIGS)")
                .description("Current US wildland fire perimeters from the NIFC WFIGS Interagency Perimeters layer"
                        + " (ArcGIS FeatureServer): the polygon (generalized to about 100 m, simplified further above"
                        + " 9,500 vertices), acreage, containment, location and incident attributes. A perimeter"
                        + " missing from two consecutive complete fetches is tombstoned; the layer is often empty in"
                        + " winter.")
                .kind(kind)
                .homepage("https://data-nifc.opendata.arcgis.com/")
                .attribution("National Interagency Fire Center (NIFC), Wildland Fire Interagency Geospatial Services")
                .licence("US public domain (NIFC Open Data)")
                .termsUrl("https://data-nifc.opendata.arcgis.com/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(6, 600))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(Duration.ofSeconds(60))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .zeroResultsNormal(true)
                .titleField("name")
                .rankField("gisAcres")
                .searchFields("name", "complexName", "county")
                .tileProperties("name", "gisAcres", "percentContained")
                .volatileFields("personnel")
                .kindDisplay(kind, new KindDisplay("{name}", "fire", "#D9480F", null));
        text(b, "name", "Name", "The incident name (poly_IncidentName)");
        b.fieldSpec("gisAcres", new FieldSpec(FieldType.INTEGER, "[acr_us]", "GIS acres",
                "The perimeter's mapped area (poly_GISAcres), rounded to 1 acre", null, Set.of()));
        b.fieldSpec("incidentSizeAcres", new FieldSpec(FieldType.NUMBER, "[acr_us]", "Incident size",
                "The incident's reported size (attr_IncidentSize)", null, Set.of()));
        b.fieldSpec("percentContained", new FieldSpec(FieldType.NUMBER, "%", "Contained",
                "Percent contained (attr_PercentContained)", null, Set.of()));
        text(b, "state", "State", "The point of origin's state (attr_POOState, for example US-CA)");
        text(b, "county", "County", "The point of origin's county (attr_POOCounty)");
        text(b, "incidentType", "Incident type", "The incident type category (attr_IncidentTypeCategory: WF"
                + " wildfire, RX prescribed fire, CX complex)");
        text(b, "cause", "Cause", "The fire cause (attr_FireCause)");
        text(b, "behavior", "Behavior", "The general fire behavior (attr_FireBehaviorGeneral)");
        b.fieldSpec("personnel", new FieldSpec(FieldType.INTEGER, null, "Personnel",
                "Total incident personnel (attr_TotalIncidentPersonnel); volatile, it changes every shift", null,
                Set.of()));
        text(b, "complexityLevel", "Complexity", "The incident complexity level (attr_IncidentComplexityLevel)");
        text(b, "complexName", "Complex", "The complex the fire belongs to (attr_CpxName)");
        text(b, "irwinId", "IRWIN id", "The IRWIN incident id (attr_IrwinID)");
        b.fieldSpec(FeatureGeometry.SIMPLIFIED_KEY, new FieldSpec(FieldType.BOOLEAN, null, "Geometry simplified",
                "Present and true when the geometry was simplified to fit the 10,000-vertex limit", null, Set.of()));
        b.fieldSpec(FeatureGeometry.TOLERANCE_KEY, new FieldSpec(FieldType.NUMBER, "deg", "Simplification tolerance",
                "The tolerance the geometry was simplified with", null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
