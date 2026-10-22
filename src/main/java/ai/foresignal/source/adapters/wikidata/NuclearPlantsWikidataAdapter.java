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
import java.time.Duration;
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
 * Foresignal's own User-Agent (with contact) is sent; a 429 honors {@code Retry-After}. The query is fixed in
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
        // WDQS runs a query for up to 60 s; the client's default 20 s request timeout would cut it short.
        UpstreamResponse response = ctx.http().post(endpoint, "application/x-www-form-urlencoded", body,
                Map.of("Accept", "application/sparql-results+json"), QUERY_TIMEOUT);
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the SPARQL endpoint answered HTTP " + response.status()
                    : ": the SPARQL response exceeds the cap"), "");
        }
        // Streamed binding by binding into chunks: the result is never held as one list of records.
        ReferenceSupport.ChunkEmitter chunks = ReferenceSupport.chunks(ctx);
        ReferenceSupport.ParseCounts counts = WikidataNuclearParser.parse(response.body(), chunks);
        chunks.flush();
        if (counts.skipped() > 0) {
            ctx.log().info("{}: {} plant(s), {} binding(s) without an item or repeating one skipped", ID,
                    counts.records(), counts.skipped());
        }
        return FetchResult.complete(List.of(), null);
    }

    /** The SPARQL request timeout: the Wikidata Query Service's own 60 s query limit (prompt 094). */
    public static final Duration QUERY_TIMEOUT = Duration.ofSeconds(60);

    /** The fetch timeout: the 60 s query plus the time to stream the result into the writer. */
    public static final Duration FETCH_TIMEOUT = Duration.ofSeconds(120);

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
                        new KindDisplay("{name}", "radiation", "#F03E3E", null))
                .fetchTimeout(FETCH_TIMEOUT));
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
