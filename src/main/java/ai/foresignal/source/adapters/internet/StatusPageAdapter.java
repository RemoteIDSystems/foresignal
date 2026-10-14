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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code status-*} (prompt 092): a Statuspage v2 service status page. Each source polls
 * {@code GET https://{host}/api/v2/summary.json} with a conditional GET and emits one {@code service-status}
 * record for the page and one {@code service-incident} record per open incident.
 *
 * <p>A Statuspage page is a rolling view; an incident that leaves {@code summary.json} is emitted once more from
 * {@code incidents.json} (its resolved version) and then tombstoned. The incident.io shim some pages use
 * ({@code status-openai}) has no {@code shortlink}/{@code started_at}, so those fields are null.
 */
@Component
public final class StatusPageAdapter implements PollingAdapter {

    /** The page's status kind. */
    public static final String KIND_STATUS = "service-status";

    /** The incident kind. */
    public static final String KIND_INCIDENT = "service-incident";

    static final Duration RETENTION = Duration.ofDays(1825);
    static final int MAX_UPDATE_BYTES = 2 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final StatusPageFamily.Page page;
    private final String base;
    private final SourceDescriptor descriptor;

    /** Creates the adapter for one reviewed page. */
    public StatusPageAdapter(StatusPageFamily.Page page, String baseUrlOverride) {
        this.page = page;
        this.base = baseUrlOverride == null || baseUrlOverride.isBlank() ? "https://" + page.host() : baseUrlOverride.strip();
        this.descriptor = buildDescriptor(page, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(
                URI.create(base + "/api/v2/summary.json"), "status:summary");
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> openIds = new LinkedHashSet<>();
        if (!response.notModified()) {
            if (response.status() != 200) {
                throw new UpstreamFormatException("Statuspage answered HTTP " + response.status(), "");
            }
            JsonNode root = JSON.readTree(response.body());
            records.add(statusRecord(root));
            JsonNode incidents = root.get("incidents");
            if (incidents != null && incidents.isArray()) {
                for (JsonNode incident : incidents) {
                    NormalizedRecord record = incidentRecord(incident, false);
                    if (record != null) {
                        records.add(record);
                        openIds.add(record.externalId());
                    }
                }
            }
        }
        // Incidents that were open before and are gone now: emit their resolved version (from incidents.json)
        // once, then a tombstone.
        Set<String> previous = new LinkedHashSet<>();
        ctx.state().get("status:open").orElse("").lines().filter(s -> !s.isBlank()).forEach(previous::add);
        for (String id : ai.foresignal.source.adapters.sanctions.DelistingGuard.gone(previous, openIds)) {
            records.addAll(resolved(ctx, id));
        }
        ctx.state().put("status:open", String.join("\n", openIds));
        return FetchResult.complete(records);
    }

    private List<NormalizedRecord> resolved(FetchContext ctx, String externalId) throws AdapterException {
        String upstreamId = externalId.startsWith("incident:") ? externalId.substring("incident:".length()) : externalId;
        UpstreamResponse response = ctx.http().get(URI.create(base + "/api/v2/incidents.json"));
        List<NormalizedRecord> out = new ArrayList<>();
        if (response.status() == 200) {
            JsonNode incidents = JSON.readTree(response.body()).get("incidents");
            if (incidents != null && incidents.isArray()) {
                for (JsonNode incident : incidents) {
                    if (upstreamId.equals(text(incident, "id"))) {
                        NormalizedRecord record = incidentRecord(incident, true);
                        if (record != null) {
                            out.add(record);
                        }
                    }
                }
            }
        }
        out.add(NormalizedRecord.tombstone(externalId, KIND_INCIDENT, ctx.now()));
        return out;
    }

    private static NormalizedRecord statusRecord(JsonNode root) {
        JsonNode pageNode = root.get("page");
        if (pageNode == null) {
            return null;
        }
        String id = text(pageNode, "id");
        if (id == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND_STATUS)
                .observedAt(time(pageNode, "updated_at"));
        JsonNode status = pageNode.get("status");
        b.put("indicator", status == null ? null : text(status, "indicator"));
        b.put("description", status == null ? null : text(status, "description"));
        List<Map<String, Object>> degraded = new ArrayList<>();
        JsonNode components = root.get("components");
        if (components != null && components.isArray()) {
            for (JsonNode component : components) {
                String componentStatus = text(component, "status");
                if (componentStatus != null && !"operational".equals(componentStatus)) {
                    degraded.add(Map.of("name", text(component, "name"), "status", componentStatus));
                }
            }
        }
        b.put("degradedComponents", degraded);
        return b.build();
    }

    private static NormalizedRecord incidentRecord(JsonNode incident, boolean force) {
        String id = text(incident, "id");
        if (id == null) {
            return null;
        }
        String status = text(incident, "status");
        if (!force && "resolved".equals(status)) {
            return null;
        }
        Instant started = time(incident, "started_at");
        NormalizedRecord.Builder b = NormalizedRecord.builder("incident:" + id, KIND_INCIDENT)
                .eventTime(started != null ? started : time(incident, "created_at"))
                .observedAt(time(incident, "updated_at"));
        b.put("name", text(incident, "name"));
        b.put("status", status);
        b.put("impact", text(incident, "impact"));
        b.put("shortlink", text(incident, "shortlink"));
        List<String> components = new ArrayList<>();
        JsonNode componentNodes = incident.get("components");
        if (componentNodes != null && componentNodes.isArray()) {
            for (JsonNode component : componentNodes) {
                String name = text(component, "name");
                if (name != null) {
                    components.add(name);
                }
            }
        }
        b.put("components", components);
        JsonNode updates = incident.get("incident_updates");
        String latestUpdate = null;
        if (updates != null && updates.isArray() && !updates.isEmpty()) {
            latestUpdate = ai.foresignal.source.adapters.humanitarian.HtmlText.strip(
                    text(updates.get(0), "body"), MAX_UPDATE_BYTES);
        }
        b.put("latestUpdate", latestUpdate);
        b.put("updateCount", updates != null && updates.isArray() ? updates.size() : 0);
        b.put("resolvedAt", "resolved".equals(status) ? time(incident, "updated_at") : null);
        return b.build();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Instant time(JsonNode node, String field) {
        return ai.foresignal.source.adapters.humanitarian.Instants.parse(text(node, field));
    }

    private static SourceDescriptor buildDescriptor(StatusPageFamily.Page page, String baseUrlOverride) {
        return SourceDescriptor.builder(page.id())
                .name(page.name() + " status")
                .description("Service status and incidents from " + page.host()
                        + " (Statuspage v2). The page's indicator, degraded components and open incidents; a "
                        + "resolved incident leaves summary.json and is tombstoned after one final version.")
                .kinds(KIND_STATUS, KIND_INCIDENT)
                .mode(Mode.POLLING)
                .homepage("https://" + page.host())
                .attribution(page.name())
                .licence("provider terms")
                .termsUrl("https://" + page.host() + "/")
                .interval(Duration.ofMinutes(2), Duration.ofMinutes(2))
                .defaultBudget(2, 1_500)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .searchFields("name", "description")
                .embedFields("name", "latestUpdate")
                .zeroResultsNormal(true)
                .kindDisplay(KIND_STATUS, new KindDisplay("{indicator}", "globe", "#1971C2", null))
                .kindDisplay(KIND_INCIDENT, new KindDisplay("{name}", "caution", "#E8590C", null))
                .build();
    }
}
