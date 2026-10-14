package ai.foresignal.source.adapters.wikidata;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.ReferenceSupport;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code nuclear-plants-wikidata}: nuclear power plants from a fixed Wikidata SPARQL query (prompt 094). CC0.
 *
 * <p>A POST to {@code query.wikidata.org/sparql} with {@code Accept: application/sparql-results+json}.
 * Foresignal's own User-Agent (with contact) is sent; a 429 honours {@code Retry-After}. The query is fixed in
 * {@link WikidataNuclearParser#QUERY}, so the operator cannot change it.
 */
@Component
public class NuclearPlantsWikidataAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nuclear-plants-wikidata";

    /** The production SPARQL endpoint. */
    public static final String ENDPOINT = "https://query.wikidata.org/sparql";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nuclear-plants-wikidata.base-url";

    private final URI endpoint;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public NuclearPlantsWikidataAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.endpoint = URI.create(KitUrls.rebased(ENDPOINT, baseUrlOverride));
    }

    /** The SPARQL endpoint this adapter posts to. */
    public URI endpoint() {
        return endpoint;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        byte[] body = ("query=" + KitUrls.encode(WikidataNuclearParser.QUERY)).getBytes(StandardCharsets.UTF_8);
        UpstreamResponse response = ctx.http().post(endpoint, "application/x-www-form-urlencoded", body,
                Map.of("Accept", "application/sparql-results+json"));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the SPARQL endpoint answered HTTP " + response.status()
                    : ": the SPARQL response exceeds the cap"), "");
        }
        List<ai.foresignal.source.spi.NormalizedRecord> records;
        try {
            records = WikidataNuclearParser.parse(response.body());
        } catch (UpstreamFormatException e) {
            throw e;
        }
        ReferenceSupport.emit(ctx, records);
        return FetchResult.complete(List.of(), null);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = ReferenceSupport.defaults(SourceDescriptor.builder(ID)
                .name("Wikidata nuclear plants")
                .description("Nuclear power plants from a fixed Wikidata SPARQL query: English label, country,"
                        + " nominal capacity, state of use and inception, with a point geometry. CC0. The"
                        + " property ids are pinned in code. A plant absent from a complete run is tombstoned.")
                .kind(WikidataNuclearParser.KIND)
                .kind("dataset-release")
                .homepage("https://www.wikidata.org/")
                .attribution("Wikidata contributors, https://www.wikidata.org")
                .licence("CC0 1.0")
                .termsUrl("https://www.wikidata.org/wiki/Wikidata:Licensing")
                .titleField("name")
                .searchFields("name", "country")
                .tileProperties("name", "stateOfUse")
                .kindDisplay(WikidataNuclearParser.KIND,
                        new KindDisplay("{name}", "radiation", "#F03E3E", null)));
        b.fieldSpec("name", new FieldSpec(FieldType.STRING, null, "Name", "The English label", null, Set.of()));
        b.fieldSpec("country", new FieldSpec(FieldType.STRING, null, "Country", "The country label", null, Set.of()));
        b.fieldSpec("capacityMw", new FieldSpec(FieldType.NUMBER, "MW", "Capacity", "Nominal power capacity, MW",
                null, Set.of()));
        b.fieldSpec("stateOfUse", new FieldSpec(FieldType.STRING, null, "State of use", "The state of use", null,
                Set.of()));
        b.fieldSpec("inception", new FieldSpec(FieldType.TIMESTAMP, null, "Inception", "When the plant began", null,
                Set.of()));
        return b.build();
    }
}
