package ai.foresignal.source.adapters.humanitarian;

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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code copernicus-ems} (prompt 090): Copernicus Emergency Management Service rapid-mapping activations.
 *
 * <p>The dashboard list is paged until an item's {@code lastUpdate} is no older than the stored watermark;
 * the detail (reason, subCategory, reportLink) is fetched only when {@code lastUpdate} changed.
 */
@Component
public final class CopernicusEmsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "copernicus-ems";

    /** The record kind. */
    public static final String KIND = "emergency-activation";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://rapidmapping.emergency.copernicus.eu";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.copernicus-ems.base-url";

    /** The adapter-state key of the watermark (the newest processed {@code lastUpdate}). */
    static final String CURSOR_KEY = "copernicus:lastUpdate";

    static final Duration RETENTION = Duration.ofDays(1825);

    /** The page size requested from the activations endpoint. */
    static final int PAGE_SIZE = 50;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public CopernicusEmsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        Instant watermark = parseInstant(ctx.state().get(CURSOR_KEY).orElse(null));
        List<NormalizedRecord> records = new ArrayList<>();
        Instant newest = watermark;
        int offset = 0;
        while (true) {
            UpstreamResponse response = ctx.http().get(URI.create(baseUrl
                    + "/backend/dashboard-api/public-activations-info/?limit=" + PAGE_SIZE + "&offset=" + offset));
            if (response.status() != 200) {
                throw new UpstreamFormatException("Copernicus EMS answered HTTP " + response.status(), "");
            }
            JsonNode root = JSON.readTree(response.body());
            JsonNode results = root.get("results");
            if (results == null || !results.isArray() || results.isEmpty()) {
                break;
            }
            // A short page is the last one; the upstream's `next` link is followed only while pages
            // are full, so a static or missing `next` cannot loop forever.
            boolean lastPage = results.size() < PAGE_SIZE;
            // Newest-updated first, so processing stops at the watermark and the expensive per-code
            // detail is fetched only for activations whose lastUpdate changed.
            List<JsonNode> items = new ArrayList<>();
            results.forEach(items::add);
            items.sort(java.util.Comparator.comparing(
                    (JsonNode n) -> parseInstant(text(n, "lastUpdate")),
                    java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));
            boolean reached = false;
            for (JsonNode item : items) {
                Instant lastUpdate = parseInstant(text(item, "lastUpdate"));
                if (lastUpdate != null && (newest == null || lastUpdate.isAfter(newest))) {
                    newest = lastUpdate;
                }
                if (watermark != null && lastUpdate != null && !lastUpdate.isAfter(watermark)) {
                    reached = true;
                    break;
                }
                // The stored watermark stays fixed for the whole run; the new one (the newest lastUpdate seen)
                // is written only after the loop, so every item newer than the previous run is kept.
                records.add(record(ctx, item));
            }
            offset += results.size();
            if (reached || lastPage || root.get("next") == null || root.get("next").isNull()) {
                break;
            }
        }
        if (newest != null) {
            ctx.state().put(CURSOR_KEY, newest.toString());
        }
        return FetchResult.of(records);
    }

    /** The records of an activations-info body, without the per-activation detail. */
    public List<NormalizedRecord> parse(byte[] body) {
        JsonNode results = JSON.readTree(body).get("results");
        List<NormalizedRecord> out = new ArrayList<>();
        if (results != null && results.isArray()) {
            for (JsonNode item : results) {
                NormalizedRecord record = build(item, null, null, null);
                if (record != null) {
                    out.add(record);
                }
            }
        }
        return out;
    }

    private NormalizedRecord record(FetchContext ctx, JsonNode item) throws AdapterException {
        String code = text(item, "code");
        String reason = null;
        String subCategory = null;
        String reportLink = null;
        if (code != null) {
            try {
                UpstreamResponse detail = ctx.http().get(URI.create(baseUrl
                        + "/backend/dashboard-api/public-activations/?code=" + code));
                if (detail.status() == 200) {
                    JsonNode node = detail(JSON.readTree(detail.body()));
                    reason = HtmlText.strip(text(node, "reason"), 4 * 1024);
                    subCategory = text(node, "subCategory");
                    reportLink = text(node, "reportLink");
                }
            } catch (AdapterException e) {
                // a missing detail leaves the summary fields null; the record is still useful
            }
        }
        return build(item, reason, subCategory, reportLink);
    }

    private NormalizedRecord build(JsonNode item, String reason, String subCategory, String reportLink) {
        String code = text(item, "code");
        if (code == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(code, KIND)
                .observedAt(parseInstant(text(item, "lastUpdate")))
                .eventTime(parseInstant(text(item, "eventTime")));
        double[] centroid = wktPoint(text(item, "centroid"));
        if (centroid != null) {
            b.point(centroid[0], centroid[1]);
        }
        b.put("name", text(item, "name"));
        b.put("category", text(item, "category"));
        b.put("countries", strings(item.get("countries")));
        b.put("activationTime", text(item, "activationTime"));
        b.put("closed", bool(item, "closed"));
        b.put("gdacsId", text(item, "gdacsId"));
        b.put("nAois", intValue(item.get("n_aois")));
        b.put("nProducts", intValue(item.get("n_products")));
        b.put("subCategory", subCategory);
        b.put("reason", reason);
        b.put("reportLink", reportLink);
        return b.build();
    }

    private static JsonNode detail(JsonNode root) {
        if (root == null) {
            return null;
        }
        JsonNode result = root.get("result");
        if (result != null && result.isObject()) {
            return result;
        }
        JsonNode results = root.get("results");
        if (results != null && results.isArray() && !results.isEmpty()) {
            return results.get(0);
        }
        if (root.isArray() && !root.isEmpty()) {
            return root.get(0);
        }
        return root;
    }

    /** Parses a WKT {@code POINT (lon lat)}, or null. */
    static double[] wktPoint(String wkt) {
        if (wkt == null) {
            return null;
        }
        int open = wkt.indexOf('(');
        int close = wkt.lastIndexOf(')');
        if (open < 0 || close < open) {
            return null;
        }
        String[] parts = wkt.substring(open + 1, close).strip().split("\\s+");
        if (parts.length < 2) {
            return null;
        }
        try {
            return new double[] {Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<String> strings(JsonNode array) {
        List<String> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode node : array) {
                if (node.isString()) {
                    out.add(node.asString());
                } else if (node.isObject() && node.get("name") != null) {
                    out.add(node.get("name").asString());
                }
            }
        } else if (array != null && array.isString()) {
            for (String part : array.asString().split(",")) {
                if (!part.isBlank()) {
                    out.add(part.strip());
                }
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

    private static Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private static Integer intValue(JsonNode value) {
        return value == null || value.isNull() || !value.isNumber() ? null : value.asInt();
    }

    static Instant parseInstant(String value) {
        return Instants.parse(value);
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Copernicus EMS activations")
                .description("Copernicus Emergency Management Service rapid-mapping activations, with the "
                        + "activation centroid and, when changed, the reason and report link. entityKeys gdacsId "
                        + "links to the gdacs source (prompt 067).")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://rapidmapping.emergency.copernicus.eu")
                .attribution("© European Union, Copernicus Emergency Management Service")
                .licence("Copernicus EMS (attribution)")
                .termsUrl("https://emergency.copernicus.eu/legal-notice")
                .interval(Duration.ofHours(1), Duration.ofHours(1))
                .defaultBudget(10, 300)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .entityKey("gdacs-id", "gdacsId")
                .titleField("name")
                .searchFields("name", "category", "countries")
                .embedFields("name", "reason")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{name}", "globe", "#C92A2A", null))
                .build();
    }
}
