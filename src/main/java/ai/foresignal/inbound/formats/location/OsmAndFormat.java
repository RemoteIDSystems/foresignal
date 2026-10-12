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
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code osmand} ingest format (prompt 039): the OsmAnd protocol as Traccar documents it
 * (https://www.traccar.org/osmand/), sent by OsmAnd's online tracking and by Traccar Client.
 *
 * <p><strong>Query or form</strong> ({@code GET}, or {@code POST} with query parameters or an
 * {@code application/x-www-form-urlencoded} body): {@code lat} and {@code lon}, or
 * {@code location=lat,lon}; {@code timestamp} as epoch seconds, epoch milliseconds, ISO-8601 or
 * {@code yyyy-MM-dd HH:mm:ss} (UTC); {@code speed} in knots; {@code bearing} or {@code heading};
 * {@code altitude}; {@code accuracy}; {@code batt}; {@code charge}; {@code valid=false} rejects the
 * fix ({@value LocationRecords#INVALID_FIX}).
 *
 * <p><strong>JSON</strong> (current Traccar Client):
 * {@code {device_id, location{timestamp, coords{latitude, longitude, speed, heading, accuracy,
 * altitude}, battery{level, is_charging}, is_moving, event}}}; speed is m/s, a battery level in
 * [0, 1] is a fraction. {@code location} may also be an array of fixes.
 *
 * <p>{@code id}, {@code deviceid} or {@code device_id} names the device: an explicit claim, so a
 * device key bound to another entity gets {@code 403 DEVICE_ENTITY_MISMATCH}. A fix that names no
 * device (and no device key or {@code X-Foresignal-External-Id}) is rejected
 * ({@value LocationRecords#EXTERNAL_ID_INVALID}), not refused, so the app does not retry it forever.
 * OsmAnd has no authentication of its own: the key travels in the path
 * ({@code /v1/ingest/{sourceId}/k/{key}/osmand}). The reply is an empty {@code 200}.
 */
@Component
public class OsmAndFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "osmand";

    private final JsonMapper json;

    /** Creates the format. */
    public OsmAndFormat(JsonMapper json) {
        this.json = json;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Override
    public Set<HttpMethod> methods() {
        return Set.of(HttpMethod.GET, HttpMethod.POST);
    }

    @Override
    public boolean pathKeyFriendly() {
        return true;
    }

    @Override
    public String description() {
        return "OsmAnd / Traccar Client (query, form or JSON); speed in knots in queries, m/s in JSON";
    }

    @Override
    public List<FormatOptionSpec> options() {
        return List.of(LocationRecords.maxAccuracyOption());
    }

    @Override
    public NativeReply nativeReply(ParsedIngest parsed) {
        return NativeReply.empty();
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        double maxAccuracy = LocationRecords.maxAccuracy(options);
        ParsedIngest.Builder out = ParsedIngest.builder();
        String header = LocationRecords.firstNonBlank(request.header(LocationRecords.EXTERNAL_ID_HEADER));
        if (LocationRecords.isJson(request)) {
            parseJson(request, header, descriptor, maxAccuracy, out);
        } else {
            parseParams(request, header, descriptor, maxAccuracy, out);
        }
        return out.build();
    }

    private static void parseParams(IngestRequest request, String header, SourceDescriptor descriptor,
            double maxAccuracy, ParsedIngest.Builder out) {
        Map<String, List<String>> p = LocationRecords.params(request);
        String claimed = LocationRecords.firstNonBlank(header, LocationRecords.param(p, "id", "deviceid", "device_id"));
        LocationFix fix = new LocationFix();
        fix.externalId = request.externalId(claimed, null);
        out.items(1);
        if (Boolean.FALSE.equals(LocationRecords.bool(LocationRecords.param(p, "valid")))) {
            out.reject(0, fix.externalId, LocationRecords.INVALID_FIX, "the app marked the fix invalid");
            return;
        }
        String location = LocationRecords.param(p, "location");
        if (location != null && LocationRecords.param(p, "lat") == null) {
            String[] parts = location.split(",");
            if (parts.length == 2) {
                fix.lat = LocationRecords.number(parts[0]);
                fix.lon = LocationRecords.number(parts[1]);
            }
        } else {
            fix.lat = LocationRecords.number(LocationRecords.param(p, "lat"));
            fix.lon = LocationRecords.number(LocationRecords.param(p, "lon"));
        }
        try {
            fix.observedAt = LocationRecords.time(LocationRecords.param(p, "timestamp"));
        } catch (InvalidInputException e) {
            out.reject(0, fix.externalId, LocationRecords.INVALID_ITEM, e.getMessage());
            return;
        }
        fix.speedMs = LocationRecords.scaled(
                LocationRecords.nonNegative(LocationRecords.number(LocationRecords.param(p, "speed"))),
                LocationRecords.KNOTS_TO_MS);
        fix.courseDeg = LocationRecords.nonNegative(LocationRecords.number(LocationRecords.param(p, "bearing", "heading")));
        fix.altitudeM = LocationRecords.number(LocationRecords.param(p, "altitude"));
        fix.accuracyMeters = LocationRecords.nonNegative(LocationRecords.number(LocationRecords.param(p, "accuracy")));
        fix.battery = LocationRecords.percent(LocationRecords.number(LocationRecords.param(p, "batt")), false);
        fix.charging = LocationRecords.bool(LocationRecords.param(p, "charge"));
        LocationRecords.add(0, fix, descriptor, maxAccuracy, out);
    }

    private void parseJson(IngestRequest request, String header, SourceDescriptor descriptor, double maxAccuracy,
            ParsedIngest.Builder out) {
        JsonNode body = LocationRecords.readJson(json, request);
        if (!body.isObject()) {
            throw new InvalidInputException("A Traccar Client JSON object {device_id, location} is required.");
        }
        String claimed = LocationRecords.firstNonBlank(header, LocationRecords.text(body.get("device_id")),
                LocationRecords.text(body.get("id")), LocationRecords.text(body.get("deviceid")));
        String id = request.externalId(claimed, null);
        JsonNode location = body.get("location");
        List<JsonNode> fixes = new ArrayList<>();
        if (location != null && location.isArray()) {
            for (int i = 0; i < location.size(); i++) {
                fixes.add(location.get(i));
            }
        } else if (location != null && location.isObject()) {
            fixes.add(location);
        } else {
            throw new InvalidInputException("The Traccar Client JSON body has no location object.");
        }
        int index = 0;
        for (JsonNode item : fixes) {
            mapJson(index++, item, id, descriptor, maxAccuracy, out);
        }
        out.items(index);
    }

    private static void mapJson(int index, JsonNode item, String id, SourceDescriptor descriptor, double maxAccuracy,
            ParsedIngest.Builder out) {
        if (item == null || !item.isObject()) {
            out.reject(index, id, LocationRecords.INVALID_ITEM, "a location must be an object");
            return;
        }
        JsonNode coords = item.path("coords");
        LocationFix fix = new LocationFix();
        fix.externalId = id;
        fix.lat = LocationRecords.number(coords.get("latitude"));
        fix.lon = LocationRecords.number(coords.get("longitude"));
        try {
            JsonNode ts = item.get("timestamp");
            fix.observedAt = ts != null && ts.isNumber()
                    ? LocationRecords.time(ts.asString())
                    : LocationRecords.time(LocationRecords.text(ts));
        } catch (InvalidInputException e) {
            out.reject(index, id, LocationRecords.INVALID_ITEM, e.getMessage());
            return;
        }
        fix.speedMs = LocationRecords.nonNegative(LocationRecords.number(coords.get("speed")));
        fix.courseDeg = LocationRecords.nonNegative(LocationRecords.number(coords.get("heading")));
        fix.accuracyMeters = LocationRecords.nonNegative(LocationRecords.number(coords.get("accuracy")));
        fix.altitudeM = LocationRecords.number(coords.get("altitude"));
        JsonNode battery = item.path("battery");
        fix.battery = LocationRecords.percent(LocationRecords.number(battery.get("level")), true);
        fix.charging = LocationRecords.bool(battery.get("is_charging"));
        Boolean moving = LocationRecords.bool(item.get("is_moving"));
        if (moving != null) {
            fix.motion = List.of(moving ? "moving" : "stationary");
        }
        String event = LocationRecords.text(item.get("event"));
        if (event != null) {
            fix.extra.put("event", event);
        }
        LocationRecords.add(index, fix, descriptor, maxAccuracy, out);
    }
}
