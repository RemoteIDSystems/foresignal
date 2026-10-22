package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code geonet-volcanoes} (prompt 090): GeoNet (New Zealand) volcano alert levels 0–5 as GeoJSON.
 *
 * <p>GeoNet publishes no update time, so the source's time base is RECEIVED.
 */
@Component
public final class GeoNetVolcanoAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "geonet-volcanoes";

    /** The record kind. */
    public static final String KIND = "volcano-alert";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.geonet.org.nz";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.geonet-volcanoes.base-url";

    static final Duration RETENTION = Duration.ofDays(365);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final SourceDescriptor descriptor;
    private final GeoJsonReader reader = new GeoJsonReader(100_000, 8);

    /** Creates the adapter. */
    @Autowired
    public GeoNetVolcanoAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().get(URI.create(baseUrl + "/volcano/val"),
                Map.of("Accept", "application/vnd.geo+json;version=2"));
        if (response.status() != 200) {
            throw new UpstreamFormatException("GeoNet answered HTTP " + response.status(), "");
        }
        return FetchResult.of(parse(response.body()));
    }

    /** The records of a GeoNet volcano/val GeoJSON body. */
    public List<NormalizedRecord> parse(byte[] body) {
        JsonNode features = JSON.readTree(body).get("features");
        List<NormalizedRecord> out = new ArrayList<>();
        if (features == null || !features.isArray()) {
            return out;
        }
        for (JsonNode feature : features) {
            NormalizedRecord record = record(feature);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode feature) {
        JsonNode properties = feature.get("properties");
        String volcanoId = text(properties, "volcanoID");
        if (volcanoId == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("geonet:" + volcanoId, KIND);
        if (feature.get("geometry") != null) {
            try {
                b.geometry(reader.read(feature.get("geometry")));
            } catch (RuntimeException e) {
                // leave the geometry off rather than lose the alert
            }
        }
        b.put("name", text(properties, "volcanoTitle"));
        b.put("nzLevel", integer(properties, "level"));
        b.put("aviationColor", text(properties, "acc"));
        b.put("activity", text(properties, "activity"));
        b.put("hazards", text(properties, "hazards"));
        return b.build();
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Integer integer(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.asInt();
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("GeoNet volcano alert levels")
                .description("New Zealand volcano alert levels (0–5) and aviation color codes from GeoNet. "
                        + "GeoNet publishes no update time, so the time base is RECEIVED.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.geonet.org.nz/volcano")
                .attribution("GeoNet (GNS Science, EQC, LINZ)")
                .licence("CC BY 4.0")
                .termsUrl("https://www.geonet.org.nz/help/license")
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(10))
                .defaultBudget(2, 300)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("name")
                .searchFields("name", "activity")
                .significantFields("nzLevel", "aviationColor")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{name} level {nzLevel}", "mountain", "#E8590C", null))
                .build();
    }
}
