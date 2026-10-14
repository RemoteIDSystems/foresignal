package ai.foresignal.source.adapters.wri;

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
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code wri-power-plants}: the World Resources Institute Global Power Plant Database (prompt 094), one
 * {@code power-plant} record per plant with a point geometry and a weekly conditional GET.
 *
 * <p>The CSV (about 12 MB) is streamed by {@link GppdParser} in chunks. An unchanged file answers 304 and
 * skips the run. A row that disappears from a complete run is tombstoned by the executor's complete-set
 * reconciliation.
 */
@Component
public class WriPowerPlantsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "wri-power-plants";

    /** The production CSV. */
    public static final String CSV_URL =
            "https://raw.githubusercontent.com/wri/global-power-plant-database/master/output_database/"
                    + "global_power_plant_database.csv";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.wri-power-plants.base-url";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "gppd";

    private final URI csvUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public WriPowerPlantsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.csvUri = URI.create(KitUrls.rebased(CSV_URL, baseUrlOverride));
    }

    /** The CSV URL this adapter fetches. */
    public URI csvUri() {
        return csvUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(csvUri, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the CSV answered HTTP " + response.status() : ": the CSV exceeds the response cap"), "");
        }
        GppdParser.Result result;
        try {
            result = GppdParser.parse(response.body(), ctx::emitChunk);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        ctx.log().debug("{}: parsed {} power plant(s), skipped {}", ID, result.records(), result.skipped());
        return FetchResult.complete(java.util.List.of(), release(response));
    }

    /** The dataset's release identity: the ETag, the Last-Modified, else the frozen version. */
    static String release(UpstreamResponse response) {
        String etag = response.header("ETag");
        if (etag != null && !etag.isBlank()) {
            return etag;
        }
        String modified = response.header("Last-Modified");
        return modified != null && !modified.isBlank() ? modified : GppdParser.VERSION;
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = GppdParser.KIND;
        SourceDescriptor.Builder b = ReferenceSupport.defaults(SourceDescriptor.builder(ID)
                .name("WRI Global Power Plant Database")
                .description("Power plants from the World Resources Institute Global Power Plant Database, frozen"
                        + " at v1.3.0 (2021): name, country, capacity, fuels, commissioning year, owner and"
                        + " source, with a point geometry where the dataset has one. Fetched weekly with a"
                        + " conditional GET; a record absent from a complete run is tombstoned.")
                .kind(kind)
                .kind("dataset-release")
                .homepage("https://datasets.wri.org/datasets/global-power-plant-database")
                .attribution("World Resources Institute")
                .licence("CC BY 4.0")
                .termsUrl("https://datasets.wri.org/datasets/global-power-plant-database")
                .titleField("name")
                .searchFields("name", "country", "owner")
                .tileProperties("name", "capacityMw", "primaryFuel")
                .rankField("capacityMw")
                .kindDisplay(kind, new KindDisplay("{name}", "power", "#C92A2A", null)));
        b.fieldSpec("name", new FieldSpec(FieldType.STRING, null, "Name", "The plant name", null, Set.of()));
        b.fieldSpec("country", new FieldSpec(FieldType.STRING, null, "Country", "The country name", null, Set.of()));
        b.fieldSpec("capacityMw", new FieldSpec(FieldType.NUMBER, "MW", "Capacity", "Installed capacity, MW", null,
                Set.of()));
        b.fieldSpec("primaryFuel", new FieldSpec(FieldType.STRING, null, "Primary fuel", "The primary fuel", null,
                Set.of()));
        b.fieldSpec("otherFuels", new FieldSpec(FieldType.ARRAY, null, "Other fuels", "Secondary fuels", null,
                Set.of()));
        b.fieldSpec("commissioningYear", new FieldSpec(FieldType.INTEGER, null, "Commissioned", "Commissioning"
                + " year", null, Set.of()));
        b.fieldSpec("owner", new FieldSpec(FieldType.STRING, null, "Owner", "The owner", null, Set.of()));
        b.fieldSpec("source", new FieldSpec(FieldType.STRING, null, "Source", "The upstream data source", null,
                Set.of()));
        b.fieldSpec("url", new FieldSpec(FieldType.STRING, null, "URL", "A reference URL", null, Set.of()));
        b.fieldSpec("geolocationSource", new FieldSpec(FieldType.STRING, null, "Geolocation source", "How the"
                + " coordinates were obtained", null, Set.of()));
        b.fieldSpec("yearOfCapacityData", new FieldSpec(FieldType.INTEGER, null, "Capacity data year", "Year of the"
                + " capacity figure", null, Set.of()));
        return b.build();
    }
}
