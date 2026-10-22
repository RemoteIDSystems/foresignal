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
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code nasa-eonet} (prompt 090): NASA EONET v3 open natural events.
 *
 * <p>The response is labeled {@code application/rss+xml} but is JSON, so JSON is detected by the first
 * non-space byte, not the content type (verified 2026-10).
 */
@Component
public final class EonetAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nasa-eonet";

    /** The record kind. */
    public static final String KIND = "natural-event";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://eonet.gsfc.nasa.gov";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nasa-eonet.base-url";

    static final Duration RETENTION = Duration.ofDays(1825);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final SourceDescriptor descriptor;
    private final GeoJsonReader reader = new GeoJsonReader(100_000, 8);

    /** Creates the adapter. */
    @Autowired
    public EonetAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        int days = ctx.params().integer("days", 30);
        UpstreamResponse response = ctx.http().get(URI.create(
                baseUrl + "/api/v3/events?status=open&days=" + days));
        if (response.status() != 200) {
            throw new UpstreamFormatException("EONET answered HTTP " + response.status(), "");
        }
        return FetchResult.of(parse(response.body()));
    }

    /**
     * The records of an EONET v3 events body.
     *
     * @throws UpstreamFormatException when the body is not JSON
     */
    public List<NormalizedRecord> parse(byte[] body) {
        if (!looksJson(body)) {
            throw new UpstreamFormatException("EONET did not return JSON (first byte is not '{')", "");
        }
        JsonNode events = JSON.readTree(body).get("events");
        List<NormalizedRecord> out = new ArrayList<>();
        if (events == null || !events.isArray()) {
            return out;
        }
        for (JsonNode event : events) {
            NormalizedRecord record = record(event);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode event) {
        String id = text(event, "id");
        if (id == null) {
            return null;
        }
        JsonNode geometries = event.get("geometry");
        JsonNode latest = latest(geometries);
        Instant first = geometries != null && geometries.isArray() && !geometries.isEmpty()
                ? time(geometries.get(0).get("date")) : null;
        Instant observedAt = latest == null ? first : time(latest.get("date"));
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(observedAt)
                .eventTime(first)
                .validUntil(time(event.get("closed")));
        if (latest != null && latest.get("type") != null) {
            try {
                b.geometry(reader.read(latest));
            } catch (RuntimeException e) {
                // a malformed geometry is left off the record rather than losing the event
            }
        }
        b.put("title", text(event, "title"));
        b.put("description", HtmlText.strip(text(event, "description"), 4 * 1024));
        b.put("categories", ids(event.get("categories"), "id"));
        b.put("sources", sources(event.get("sources")));
        if (latest != null) {
            b.put("magnitude", number(latest.get("magnitudeValue")));
            b.put("magnitudeUnit", text(latest, "magnitudeUnit"));
        }
        b.put("trackPoints", geometries != null && geometries.isArray() ? geometries.size() : 0);
        return b.build();
    }

    static boolean looksJson(byte[] body) {
        if (body == null) {
            return false;
        }
        for (byte value : body) {
            char c = (char) value;
            if (!Character.isWhitespace(c)) {
                return c == '{';
            }
        }
        return false;
    }

    private static JsonNode latest(JsonNode geometries) {
        if (geometries == null || !geometries.isArray() || geometries.isEmpty()) {
            return null;
        }
        JsonNode latest = null;
        Instant best = null;
        for (JsonNode geometry : geometries) {
            Instant date = time(geometry.get("date"));
            if (date != null && (best == null || date.isAfter(best))) {
                best = date;
                latest = geometry;
            }
        }
        return latest != null ? latest : geometries.get(geometries.size() - 1);
    }

    private static List<String> ids(JsonNode array, String field) {
        List<String> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode node : array) {
                String value = text(node, field);
                if (value != null) {
                    out.add(value);
                }
            }
        }
        return out;
    }

    private static List<Map<String, Object>> sources(JsonNode array) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode node : array) {
                Map<String, Object> source = new LinkedHashMap<>();
                source.put("id", text(node, "id"));
                source.put("url", text(node, "url"));
                out.add(source);
            }
        }
        return out;
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isNumber() ? node.asDouble() : null;
    }

    static Instant time(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asString(null);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.strip()).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(value.strip());
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("NASA EONET natural events")
                .description("Open natural events (wildfires, storms, volcanoes, floods, sea/lake ice, dust and "
                        + "haze, manmade) from NASA's Earth Observatory Natural Event Tracker v3. The latest "
                        + "geometry of each event is the record geometry; the response is JSON despite its RSS "
                        + "content type.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://eonet.gsfc.nasa.gov")
                .attribution("NASA Earth Observatory Natural Event Tracker (EONET)")
                .licence("public domain (NASA)")
                .termsUrl("https://www.nasa.gov/nasa-brand-center/images-and-media/")
                .param(ParamSpec.optional("days", ParamType.INTEGER, "How many days back to list open events", 30))
                .interval(Duration.ofMinutes(30), Duration.ofMinutes(30))
                .defaultBudget(2, 200)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("title")
                .searchFields("title")
                .embedFields("title", "description")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{title}", "globe", "#E8590C", null))
                .build();
    }
}
