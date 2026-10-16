package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.CredentialField;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code reliefweb-disasters} (prompt 090): disaster records (status alert or current) from ReliefWeb.
 *
 * <p>ReliefWeb now requires an approved {@code appname}; a 403 saying so is a terminal credential rejection
 * ({@code APPNAME_NOT_APPROVED}) and is not retried until the credential changes.
 */
@Component
public final class ReliefWebDisastersAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "reliefweb-disasters";

    /** The record kind. */
    public static final String KIND = "disaster";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.reliefweb.int";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.reliefweb-disasters.base-url";

    /** The credential name. */
    public static final String APPNAME = "appname";

    static final Duration RETENTION = Duration.ofDays(1825);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public ReliefWebDisastersAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        String appname = ctx.credentials().get(APPNAME);
        if (appname == null || appname.isBlank()) {
            throw new CredentialsRejectedException("reliefweb-disasters needs an approved appname credential");
        }
        UpstreamResponse response;
        try {
            // The client throws for any 4xx/5xx; a 403 naming the appname is a terminal credential
            // rejection, not a retryable error.
            response = ctx.http().post(
                    URI.create(baseUrl + "/v2/disasters?appname="
                            + java.net.URLEncoder.encode(appname.strip(), StandardCharsets.UTF_8)), "application/json",
                    query().getBytes(StandardCharsets.UTF_8), Map.of());
        } catch (UpstreamHttpException e) {
            if (e.getStatus() == 403 && e.getBodySnippet().toLowerCase(java.util.Locale.ROOT).contains("appname")) {
                throw new CredentialsRejectedException("APPNAME_NOT_APPROVED: ReliefWeb rejected the appname; request "
                        + "an approved one at https://apidoc.reliefweb.int/parameters#appname");
            }
            throw e;
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("ReliefWeb answered HTTP " + response.status(), "");
        }
        return FetchResult.of(parse(response.body()));
    }

    private static String query() {
        ObjectNode root = JSON.createObjectNode();
        ObjectNode filter = root.putObject("filter");
        filter.put("field", "status");
        ArrayNode values = filter.putArray("value");
        values.add("alert");
        values.add("current");
        root.putArray("sort").add("date.changed:desc");
        root.put("limit", 100);
        ObjectNode fields = root.putObject("fields");
        ArrayNode include = fields.putArray("include");
        for (String field : List.of("id", "name", "status", "glide", "type.name", "country.iso3",
                "primary_country.iso3", "primary_country.location", "date.event", "date.changed", "url", "description")) {
            include.add(field);
        }
        return root.toString();
    }

    /** The records of a ReliefWeb disasters body. */
    public List<NormalizedRecord> parse(byte[] body) {
        JsonNode data = JSON.readTree(body).get("data");
        List<NormalizedRecord> out = new ArrayList<>();
        if (data == null || !data.isArray()) {
            return out;
        }
        for (JsonNode entry : data) {
            NormalizedRecord record = record(entry);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode entry) {
        JsonNode fields = entry.get("fields");
        if (fields == null) {
            return null;
        }
        String id = text(entry, "id");
        if (id == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("rw:" + id, KIND)
                .observedAt(time(node(fields, "date", "changed")))
                .eventTime(time(node(fields, "date", "event")));
        JsonNode location = node(fields, "primary_country", "location");
        if (location != null && location.get("lon") != null && location.get("lat") != null) {
            b.point(location.get("lon").asDouble(), location.get("lat").asDouble());
            // primary_country.location is the country's point, not the event's: approximate (Adapter Guide, 040)
            b.put("geometryApproximate", true);
        }
        b.put("name", truncate(text(fields, "name"), 4 * 1024));
        b.put("status", text(fields, "status"));
        b.put("glide", text(fields, "glide"));
        b.put("types", names(fields.get("type"), "name"));
        b.put("countries", names(fields.get("country"), "iso3"));
        b.put("url", text(fields, "url"));
        b.put("description", HtmlText.strip(text(fields, "description"), 4 * 1024));
        return b.build();
    }

    private static JsonNode node(JsonNode parent, String field, String child) {
        JsonNode node = parent.get(field);
        return node == null ? null : node.get(child);
    }

    private static List<String> names(JsonNode array, String field) {
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

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    static Instant time(JsonNode node) {
        return node == null || node.isNull() ? null : Instants.parse(node.asString(null));
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("ReliefWeb disasters")
                .description("Humanitarian disasters (status alert or current) from ReliefWeb (UN OCHA). Needs an "
                        + "approved appname; ReliefWeb now answers 403 without one.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://reliefweb.int")
                .attribution("ReliefWeb / UN OCHA")
                .licence("ReliefWeb terms (content rights vary by source)")
                .termsUrl("https://reliefweb.int/terms-conditions")
                .credential(CredentialField.required(APPNAME, "Approved ReliefWeb appname "
                        + "(https://apidoc.reliefweb.int/parameters#appname)"))
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(2, 50)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("name")
                .searchFields("name", "description")
                .embedFields("name", "description")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{name}", "caution", "#E8590C", null))
                .build();
    }
}
