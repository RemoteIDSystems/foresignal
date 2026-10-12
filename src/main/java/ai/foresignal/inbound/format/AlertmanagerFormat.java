package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code alertmanager} ingest format (prompt 024): a Prometheus Alertmanager webhook payload,
 * {@code version: "4"}, one record per alert.
 *
 * <p>externalId is the alert {@code fingerprint}; {@code observedAt} is {@code startsAt} while
 * firing and {@code endsAt} once resolved, so a repeated notification of an unchanged alert carries
 * an equal timestamp and becomes a touch. A resolved alert becomes a tombstone unless the option
 * {@code resolved} is {@code record}. Alertmanager cannot sign its requests; it authenticates with a
 * bearer or Basic credential configured in {@code http_config}.
 */
@Component
public class AlertmanagerFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "alertmanager";

    private final JsonMapper json;
    private final GeoJsonIngestMapper mapper;

    /** Creates the format. */
    public AlertmanagerFormat(JsonMapper json, GeoJsonIngestMapper mapper) {
        this.json = json;
        this.mapper = mapper;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(MediaType.APPLICATION_JSON);
    }

    @Override
    public String description() {
        return "Prometheus Alertmanager webhook payload (version 4), one record per alert";
    }

    @Override
    public List<FormatOptionSpec> options() {
        return List.of(
                new FormatOptionSpec("kindLabel", "string", false, null,
                        "Label whose value becomes the kind; else the source's single kind"),
                new FormatOptionSpec("resolved", "string", false, "tombstone",
                        "tombstone (a resolved alert ends the record) or record (kept as a resolved record)"));
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        JsonNode body = NativeFormat.readJson(json, request);
        if (!body.isObject() || !"4".equals(GeoJsonIngestMapper.text(body.get("version")))) {
            throw new InvalidInputException("An Alertmanager webhook payload with version \"4\" is required.");
        }
        JsonNode alerts = body.get("alerts");
        if (alerts == null || !alerts.isArray()) {
            throw new InvalidInputException("The Alertmanager payload has no alerts array.");
        }
        String kindLabel = options.text("kindLabel", null);
        boolean tombstoneResolved = !"record".equals(options.text("resolved", "tombstone"));
        String receiver = GeoJsonIngestMapper.text(body.get("receiver"));
        String groupKey = GeoJsonIngestMapper.text(body.get("groupKey"));

        ParsedIngest.Builder out = ParsedIngest.builder();
        int index = 0;
        for (JsonNode alert : alerts) {
            map(index++, alert, descriptor, kindLabel, tombstoneResolved, receiver, groupKey, out);
        }
        out.items(index);
        return out.build();
    }

    private void map(int index, JsonNode alert, SourceDescriptor descriptor, String kindLabel,
            boolean tombstoneResolved, String receiver, String groupKey, ParsedIngest.Builder out) {
        if (alert == null || !alert.isObject()) {
            out.reject(index, null, "INVALID_ITEM", "an alert must be an object");
            return;
        }
        String fingerprint = GeoJsonIngestMapper.idText(alert.get("fingerprint"));
        if (fingerprint == null) {
            out.reject(index, null, "EXTERNAL_ID_INVALID", "the alert has no fingerprint");
            return;
        }
        JsonNode labels = alert.path("labels");
        String requested = kindLabel == null ? null : GeoJsonIngestMapper.text(labels.get(kindLabel));
        String kind = kindLabel != null && requested == null
                ? GeoJsonIngestMapper.resolveKind(null, descriptor)
                : GeoJsonIngestMapper.resolveKind(requested, descriptor);
        if (kind == null) {
            out.reject(index, fingerprint, "UNKNOWN_KIND", "no declared kind for this alert");
            return;
        }
        String status = GeoJsonIngestMapper.text(alert.get("status"));
        boolean resolved = "resolved".equals(status);
        Instant startsAt;
        Instant endsAt;
        try {
            startsAt = IngestTimes.parse(alert.get("startsAt"));
            endsAt = IngestTimes.parse(alert.get("endsAt"));
        } catch (InvalidInputException e) {
            out.reject(index, fingerprint, "INVALID_ITEM", e.getMessage());
            return;
        }
        Instant observedAt = resolved ? endsAt : startsAt;
        if (resolved && tombstoneResolved) {
            out.add(index, NormalizedRecord.tombstone(fingerprint, kind, observedAt));
            return;
        }
        NormalizedRecord.Builder record = NormalizedRecord.builder(fingerprint, kind)
                .observedAt(observedAt)
                .eventTime(startsAt)
                .geometry(point(labels))
                .put("status", status)
                .put("labels", mapper.toMap(labels))
                .put("annotations", mapper.toMap(alert.get("annotations")))
                .put("generatorURL", GeoJsonIngestMapper.text(alert.get("generatorURL")))
                .put("receiver", receiver)
                .put("groupKey", groupKey);
        out.add(index, record.build());
    }

    /** A point from the {@code latitude}/{@code longitude} (or {@code lat}/{@code lon}) labels. */
    static Point point(JsonNode labels) {
        Double lat = coordinate(labels, "latitude", "lat");
        Double lon = coordinate(labels, "longitude", "lon");
        if (lat == null || lon == null || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            return null;
        }
        return Geo.factory().createPoint(new Coordinate(lon, lat));
    }

    private static Double coordinate(JsonNode labels, String name, String alias) {
        String text = GeoJsonIngestMapper.text(labels.get(name));
        if (text == null) {
            text = GeoJsonIngestMapper.text(labels.get(alias));
        }
        if (text == null) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.strip());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
