package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.kit.feature.WfsJsonClient;
import ai.foresignal.source.spi.AdapterException;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * One CWFIS feature type (prompt 075), read with the prompt 073 {@link WfsJsonClient}
 * ({@code outputFormat=application/json}, {@code count=1000}, {@code startIndex} paging, coordinates longitude first)
 * and mapped by {@link CwfisMappers}.
 *
 * <p>A fetch cut short by the page cap is partial. {@code cwfis-perimeters} tombstones a polygon missing from two
 * consecutive complete fetches; the station and hotspot layers expire by TTL instead. Every CWFIS source ships
 * disabled; the data is published under the Open Government Licence – Canada.
 */
public class CwfisAdapter implements PollingAdapter {

    /** The response cap of one page. */
    static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    private final CwfisLayer layer;
    private final URI endpoint;
    private final Supplier<MeterRegistry> meters;
    private final WfsJsonClient client = new WfsJsonClient();
    private final SourceDescriptor descriptor;

    /**
     * Creates the source.
     *
     * @param layer the reviewed layer
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public CwfisAdapter(CwfisLayer layer, String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.layer = Objects.requireNonNull(layer);
        this.endpoint = URI.create(FireSources.rebased(CwfisLayer.ENDPOINT, baseUrlOverride));
        this.meters = meters;
        this.descriptor = buildDescriptor(layer);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The layer. */
    public CwfisLayer layer() {
        return layer;
    }

    /**
     * Fetches the feature type once (see the class note).
     *
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a redirect, a bad status or body, or a page that is not a FeatureCollection
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        FeatureFetch fetch;
        try {
            fetch = client.fetch(ctx, new WfsJsonClient.Request(endpoint, layer.typeName(), CwfisLayer.PAGE_SIZE,
                    layer.maxPages(), layer.maxPages() * CwfisLayer.PAGE_SIZE, false, CwfisLayer.NORTH_AMERICA,
                    layer.filters(), Map.of(), Map.of(), layer.id()));
        } catch (UpstreamFormatException e) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
            throw e;
        }
        FireFetch.Batch batch = FireFetch.map(layer.reorder().apply(fetch.features()), layer.mapper());
        return FireFetch.result(ctx, fetch, batch, layer.kind(), layer.tombstones(), meters);
    }

    static SourceDescriptor buildDescriptor(CwfisLayer layer) {
        String kind = layer.kind();
        SourceDescriptor.Builder b = SourceDescriptor.builder(layer.id())
                .name(layer.name())
                .description(layer.description())
                .kind(kind)
                .homepage("https://cwfis.cfs.nrcan.gc.ca/")
                .attribution(CwfisLayer.ATTRIBUTION)
                .licence(CwfisLayer.LICENSE)
                .termsUrl("https://open.canada.ca/en/open-government-licence-canada")
                .mode(Mode.POLLING)
                .interval(layer.minInterval(), layer.interval())
                .defaultBudget(layer.budget())
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(Duration.ofSeconds(60))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(layer.recordTtl() == null ? TtlAnchor.RECEIVED : TtlAnchor.OBSERVED)
                .recordTtl(layer.recordTtl())
                .defaultRetention(layer.retention())
                .kindRetention(kind, layer.retention())
                .zeroResultsNormal(true);
        switch (kind) {
            case CwfisLayer.FIRE_WEATHER -> stationFields(b);
            case CwfisLayer.FIRE_DETECTION -> hotspotFields(b);
            default -> perimeterFields(b);
        }
        return b.build();
    }

    private static void perimeterFields(SourceDescriptor.Builder b) {
        b.rankField("areaHa").tileProperties("hotspotCount", "areaHa", "lastDetected")
                .kindDisplay(NifcPerimeterMapper.KIND, new KindDisplay("Burned area {areaHa} ha", "fire", "#C92A2A",
                        null));
        spec(b, "hotspotCount", FieldType.INTEGER, null, "Hotspots", "The hotspots the polygon was drawn around (hcount)");
        spec(b, "firstDetected", FieldType.TIMESTAMP, null, "First detected", "The first hotspot's time (firstdate)");
        spec(b, "lastDetected", FieldType.TIMESTAMP, null, "Last detected", "The newest hotspot's time (lastdate)");
        spec(b, "areaHa", FieldType.NUMBER, "har", "Area", "The polygon's area in hectares (area)");
        spec(b, "perimeterSource", FieldType.STRING, null, "Perimeter source", "Always m3: a hotspot-derived polygon,"
                + " not an agency-mapped perimeter");
        spec(b, FeatureGeometry.SIMPLIFIED_KEY, FieldType.BOOLEAN, null, "Geometry simplified",
                "Present and true when the geometry was simplified to fit the 10,000-vertex limit");
        spec(b, FeatureGeometry.TOLERANCE_KEY, FieldType.NUMBER, "deg", "Simplification tolerance",
                "The tolerance the geometry was simplified with");
    }

    private static void stationFields(SourceDescriptor.Builder b) {
        b.titleField("name").searchFields("name", "province", "agency").rankField("fwi")
                .tileProperties("name", "fwi", "tempC")
                .kindDisplay(CwfisLayer.FIRE_WEATHER, new KindDisplay("{name} FWI {fwi}", "forecast", "#F08C00",
                        null));
        spec(b, "name", FieldType.STRING, null, "Name", "The station name, trimmed");
        spec(b, "province", FieldType.STRING, null, "Province", "The province or state code (prov)");
        spec(b, "agency", FieldType.STRING, null, "Agency", "The reporting agency, trimmed");
        spec(b, "elevationM", FieldType.NUMBER, "m", "Elevation", "The station elevation (elev)");
        spec(b, "tempC", FieldType.NUMBER, "Cel", "Temperature", "Air temperature (temp)");
        spec(b, "rhPct", FieldType.NUMBER, "%", "Relative humidity", "Relative humidity (rh)");
        spec(b, "windKph", FieldType.NUMBER, "km/h", "Wind", "Wind speed (ws)");
        spec(b, "gustKph", FieldType.NUMBER, "km/h", "Gust", "Wind gust (wg)");
        spec(b, "windDirDeg", FieldType.NUMBER, "deg", "Wind direction", "Wind direction (wdir)");
        spec(b, "precipMm", FieldType.NUMBER, "mm", "Precipitation", "Precipitation (precip)");
        spec(b, "ffmc", FieldType.NUMBER, "1", "FFMC", "Fine Fuel Moisture Code");
        spec(b, "dmc", FieldType.NUMBER, "1", "DMC", "Duff Moisture Code");
        spec(b, "dc", FieldType.NUMBER, "1", "DC", "Drought Code");
        spec(b, "isi", FieldType.NUMBER, "1", "ISI", "Initial Spread Index");
        spec(b, "bui", FieldType.NUMBER, "1", "BUI", "Buildup Index");
        spec(b, "fwi", FieldType.NUMBER, "1", "FWI", "Fire Weather Index");
        spec(b, "dsr", FieldType.NUMBER, "1", "DSR", "Daily Severity Rating");
    }

    private static void hotspotFields(SourceDescriptor.Builder b) {
        b.rankField("hfi").tileProperties("hfi", "ros", "fuel", "frp")
                .kindDisplay(CwfisLayer.FIRE_DETECTION, new KindDisplay("Hotspot HFI {hfi} kW/m", "fire", "#F76707",
                        null));
        spec(b, "hfi", FieldType.NUMBER, "kW/m", "Head fire intensity", "Predicted head fire intensity");
        spec(b, "ros", FieldType.NUMBER, "m/min", "Rate of spread", "Predicted rate of spread");
        spec(b, "fuel", FieldType.STRING, null, "Fuel type", "The FBP fuel type (for example C2, non_fuel)");
        spec(b, "cfb", FieldType.NUMBER, "%", "Crown fraction burned", "Predicted crown fraction burned");
        spec(b, "sfc", FieldType.NUMBER, "kg/m2", "Surface fuel consumption", "Predicted surface fuel consumption");
        spec(b, "tfc", FieldType.NUMBER, "kg/m2", "Total fuel consumption", "Predicted total fuel consumption");
        spec(b, "frp", FieldType.NUMBER, "MW", "FRP", "Fire radiative power");
        spec(b, "sensor", FieldType.STRING, null, "Sensor", "The sensor (for example VIIRS-I, MODIS)");
        spec(b, "satellite", FieldType.STRING, null, "Satellite", "The satellite (for example NOAA-21)");
        spec(b, "agency", FieldType.STRING, null, "Agency", "The agency whose area the hotspot is in");
    }

    private static void spec(SourceDescriptor.Builder b, String key, FieldType type, String unit, String label,
            String description) {
        b.fieldSpec(key, new FieldSpec(type, unit, label, description, null, Set.of()));
    }
}
