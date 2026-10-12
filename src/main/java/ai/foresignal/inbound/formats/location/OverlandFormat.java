package ai.foresignal.inbound.formats.location;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.FormatOptionSpec;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code overland} ingest format (prompt 039): batches from the Overland app
 * (https://github.com/aaronpk/Overland-iOS), {@code {"locations": [GeoJSON Feature, …]}}, sent with
 * {@code Authorization: Bearer <key>}.
 *
 * <p>Each Feature's Point geometry is the fix; {@code properties.timestamp} (ISO-8601) the observed
 * time; {@code horizontal_accuracy}, {@code altitude}, {@code speed} (m/s), {@code course},
 * {@code motion[]}, {@code battery_level} (a fraction) and {@code battery_state} ({@code charging} or
 * {@code full} mean charging) are mapped; {@code wifi} is dropped unless the option
 * {@value #OPTION_KEEP_WIFI} is true. {@code properties.device_id} names the device (an explicit
 * claim). {@code trip} and {@code current} are ignored. A batch of more than {@value #MAX_POINTS}
 * points is a {@code 400}.
 *
 * <p>The reply is always {@code {"result":"ok"}} when the request was readable, even when some points
 * were rejected: Overland retries any other reply, and rejected points would never succeed.
 */
@Component
public class OverlandFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "overland";

    /** The most points one request may carry. */
    public static final int MAX_POINTS = 1_000;

    /** The option that keeps the {@code wifi} SSID. */
    public static final String OPTION_KEEP_WIFI = "keepWifi";

    private final JsonMapper json;

    /** Creates the format. */
    public OverlandFormat(JsonMapper json) {
        this.json = json;
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
        return "Overland batches {\"locations\": [Feature…]}; replies {\"result\":\"ok\"}";
    }

    @Override
    public List<FormatOptionSpec> options() {
        return List.of(LocationRecords.maxAccuracyOption(),
                new FormatOptionSpec(OPTION_KEEP_WIFI, "boolean", false, false,
                        "Keep the Wi-Fi network name Overland reports (dropped by default)"));
    }

    @Override
    public NativeReply nativeReply(ParsedIngest parsed) {
        return NativeReply.json("{\"result\":\"ok\"}");
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        JsonNode body = LocationRecords.readJson(json, request);
        JsonNode locations = body.isObject() ? body.get("locations") : null;
        if (locations == null || !locations.isArray()) {
            throw new InvalidInputException("An Overland batch {\"locations\": [...]} is required.");
        }
        if (locations.size() > MAX_POINTS) {
            throw new InvalidInputException(
                    "An Overland batch may carry at most " + MAX_POINTS + " points; this one has " + locations.size() + ".");
        }
        double maxAccuracy = LocationRecords.maxAccuracy(options);
        boolean keepWifi = options.bool(OPTION_KEEP_WIFI, false);
        String header = LocationRecords.firstNonBlank(request.header(LocationRecords.EXTERNAL_ID_HEADER));
        ParsedIngest.Builder out = ParsedIngest.builder();
        for (int index = 0; index < locations.size(); index++) {
            map(index, locations.get(index), header, request, descriptor, maxAccuracy, keepWifi, out);
        }
        out.items(locations.size());
        return out.build();
    }

    private static void map(int index, JsonNode feature, String header, IngestRequest request,
            SourceDescriptor descriptor, double maxAccuracy, boolean keepWifi, ParsedIngest.Builder out) {
        if (feature == null || !feature.isObject()) {
            out.reject(index, null, LocationRecords.INVALID_ITEM, "a location must be a GeoJSON Feature");
            return;
        }
        JsonNode props = feature.path("properties");
        String claimed = LocationRecords.firstNonBlank(header, LocationRecords.text(props.get("device_id")));
        LocationFix fix = new LocationFix();
        fix.externalId = request.externalId(claimed, null);
        JsonNode geometry = feature.path("geometry");
        JsonNode coordinates = geometry.path("coordinates");
        if (!"Point".equals(LocationRecords.text(geometry.get("type"))) || !coordinates.isArray()
                || coordinates.size() < 2) {
            out.reject(index, fix.externalId, LocationRecords.INVALID_FIX, "the geometry is not a Point");
            return;
        }
        fix.lon = LocationRecords.number(coordinates.get(0));
        fix.lat = LocationRecords.number(coordinates.get(1));
        try {
            fix.observedAt = LocationRecords.time(LocationRecords.text(props.get("timestamp")));
        } catch (InvalidInputException e) {
            out.reject(index, fix.externalId, LocationRecords.INVALID_ITEM, e.getMessage());
            return;
        }
        fix.accuracyMeters = LocationRecords.nonNegative(LocationRecords.number(props.get("horizontal_accuracy")));
        fix.altitudeM = LocationRecords.number(props.get("altitude"));
        fix.speedMs = LocationRecords.nonNegative(LocationRecords.number(props.get("speed")));
        fix.courseDeg = LocationRecords.nonNegative(LocationRecords.number(props.get("course")));
        fix.battery = LocationRecords.percent(LocationRecords.number(props.get("battery_level")), true);
        String state = LocationRecords.text(props.get("battery_state"));
        if (state != null) {
            String lower = state.toLowerCase(Locale.ROOT);
            fix.charging = switch (lower) {
                case "charging", "full" -> Boolean.TRUE;
                case "unplugged" -> Boolean.FALSE;
                default -> null;
            };
            fix.extra.put("batteryState", lower);
        }
        JsonNode motion = props.get("motion");
        if (motion != null && motion.isArray()) {
            List<String> activities = new ArrayList<>();
            for (int i = 0; i < motion.size(); i++) {
                String value = LocationRecords.text(motion.get(i));
                if (value != null) {
                    activities.add(value);
                }
            }
            fix.motion = activities;
        }
        String wifi = LocationRecords.text(props.get("wifi"));
        if (keepWifi && wifi != null) {
            fix.extra.put("wifi", wifi);
        }
        LocationRecords.add(index, fix, descriptor, maxAccuracy, out);
    }
}
