package ai.foresignal.source.adapters.internet;

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
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Google status pages (prompt 092): {@code status-gcp} and {@code status-google-workspace}. Each is a single
 * {@code incidents.json} array covering a rolling ~12-month window, so every incident is emitted and the run is
 * complete.
 */
@Component
public final class GoogleStatusAdapter implements PollingAdapter {

    /** The incident kind. */
    public static final String KIND = "service-incident";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.google-status.base-url";
    static final Duration RETENTION = Duration.ofDays(1825);
    static final int MAX_TEXT = 2 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final GoogleStatusFamily.Page page;
    private final SourceDescriptor descriptor;

    /** Creates the adapter for one page. */
    public GoogleStatusAdapter(GoogleStatusFamily.Page page, String baseUrlOverride) {
        this.page = page;
        this.descriptor = buildDescriptor(page, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(URI.create(page.url()), "google-status:index");
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The Google status page answered HTTP " + response.status(), "");
        }
        return FetchResult.complete(parse(response.body()));
    }

    /** The records of one incidents.json body. */
    public List<NormalizedRecord> parse(byte[] body) {
        JsonNode root = JSON.readTree(body);
        List<NormalizedRecord> out = new ArrayList<>();
        if (root == null || !root.isArray()) {
            return out;
        }
        for (JsonNode incident : root) {
            NormalizedRecord record = map(incident);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private static NormalizedRecord map(JsonNode incident) {
        String id = text(incident, "id");
        if (id == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .eventTime(time(incident, "begin"))
                .observedAt(time(incident, "modified"));
        b.put("number", text(incident, "number"));
        b.put("externalDesc", ai.foresignal.source.adapters.humanitarian.HtmlText.strip(
                text(incident, "external_desc"), MAX_TEXT));
        b.put("severity", text(incident, "severity"));
        b.put("statusImpact", text(incident, "status_impact"));
        b.put("serviceName", text(incident, "service_name"));
        b.put("affectedProducts", strings(incident.get("affected_products")));
        b.put("currentlyAffectedLocations", strings(incident.get("currently_affected_locations")));
        b.put("end", text(incident, "end"));
        b.put("ongoing", bool(incident, "ongoing"));
        JsonNode update = incident.get("most_recent_update");
        b.put("latestUpdate", update == null ? null : ai.foresignal.source.adapters.humanitarian.HtmlText.strip(
                text(update, "text"), MAX_TEXT));
        return b.build();
    }

    private static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.isString()) {
                    out.add(item.asString());
                } else if (item.isObject() && item.get("title") != null) {
                    out.add(item.get("title").asString());
                }
            }
        }
        return out;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private static Instant time(JsonNode node, String field) {
        return ai.foresignal.source.adapters.humanitarian.Instants.parse(text(node, field));
    }

    private static SourceDescriptor buildDescriptor(GoogleStatusFamily.Page page, String baseUrlOverride) {
        return SourceDescriptor.builder(page.id())
                .name(page.name() + " status")
                .description("Incidents from the " + page.name() + " status page (a rolling ~12-month window). "
                        + "Every incident in the file is emitted; the latest update text is untrusted.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage(page.url())
                .attribution(page.name())
                .licence("provider terms")
                .termsUrl("https://" + page.termsHost() + "/")
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(2, 300)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .searchFields("serviceName", "externalDesc")
                .embedFields("serviceName", "latestUpdate")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{serviceName}", "caution", "#E8590C", null))
                .build();
    }
}
