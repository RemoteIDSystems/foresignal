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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code gpslogger} ingest format (prompt 039): GPSLogger for Android's custom URL logging
 * (https://gpslogger.app/), whose method, body and headers the user templates.
 *
 * <p>The documented template is
 * {@code lat=%LAT&lon=%LON&time=%TIME&acc=%ACC&alt=%ALT&spd=%SPD&dir=%DIR&batt=%BATT&dev=%SER}, sent
 * as a {@code GET} query, a {@code POST} form body or a {@code POST} JSON object. Names are matched
 * case-insensitively: {@code lat}, {@code lon}, {@code time} (ISO-8601 or epoch; {@code timestamp}
 * is accepted too), {@code acc}, {@code alt}, {@code spd} (m/s), {@code dir}, {@code batt} (%),
 * {@code charging}/{@code ischarging}, and {@code dev}, which names the device (an explicit claim).
 * GPSLogger sends Basic or Bearer credentials when configured, or the key travels in the path. The
 * reply is an empty {@code 200}.
 */
@Component
public class GpsLoggerFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "gpslogger";

    private final JsonMapper json;

    /** Creates the format. */
    public GpsLoggerFormat(JsonMapper json) {
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
        return "GPSLogger custom URL: lat, lon, time, acc, alt, spd (m/s), dir, batt, dev (query, form or JSON)";
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
        Map<String, List<String>> p = LocationRecords.isJson(request) ? jsonParams(request) : LocationRecords.params(request);
        String header = LocationRecords.firstNonBlank(request.header(LocationRecords.EXTERNAL_ID_HEADER));
        String claimed = LocationRecords.firstNonBlank(header, LocationRecords.param(p, "dev"));
        LocationFix fix = new LocationFix();
        fix.externalId = request.externalId(claimed, null);
        ParsedIngest.Builder out = ParsedIngest.builder();
        out.items(1);
        fix.lat = LocationRecords.number(LocationRecords.param(p, "lat"));
        fix.lon = LocationRecords.number(LocationRecords.param(p, "lon"));
        try {
            fix.observedAt = LocationRecords.time(LocationRecords.param(p, "time", "timestamp"));
        } catch (InvalidInputException e) {
            out.reject(0, fix.externalId, LocationRecords.INVALID_ITEM, e.getMessage());
            return out.build();
        }
        fix.accuracyMeters = LocationRecords.nonNegative(LocationRecords.number(LocationRecords.param(p, "acc")));
        fix.altitudeM = LocationRecords.number(LocationRecords.param(p, "alt"));
        fix.speedMs = LocationRecords.nonNegative(LocationRecords.number(LocationRecords.param(p, "spd")));
        fix.courseDeg = LocationRecords.nonNegative(LocationRecords.number(LocationRecords.param(p, "dir")));
        fix.battery = LocationRecords.percent(LocationRecords.number(LocationRecords.param(p, "batt")), false);
        fix.charging = LocationRecords.bool(LocationRecords.param(p, "charging", "ischarging"));
        LocationRecords.add(0, fix, descriptor, LocationRecords.maxAccuracy(options), out);
        return out.build();
    }

    /** A JSON object's value members as parameters (query parameters are kept too). */
    private Map<String, List<String>> jsonParams(IngestRequest request) {
        JsonNode body = LocationRecords.readJson(json, request);
        if (!body.isObject()) {
            throw new InvalidInputException("A GPSLogger JSON body must be an object.");
        }
        Map<String, List<String>> out = new LinkedHashMap<>();
        request.query().forEach((name, values) -> out.computeIfAbsent(name, k -> new ArrayList<>()).addAll(values));
        for (Map.Entry<String, JsonNode> entry : body.properties()) {
            String value = LocationRecords.text(entry.getValue());
            if (value != null) {
                out.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(value);
            }
        }
        return out;
    }
}
