package ai.foresignal.inbound.formats.location;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.FormatOptionSpec;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code owntracks} ingest format (prompt 039): OwnTracks in HTTP mode
 * (https://owntracks.org/booklet/tech/http/), one JSON message or an array of them per POST.
 *
 * <ul>
 *   <li>{@code _type=location}: a {@code tracker} fix. {@code lat}, {@code lon}; {@code tst} (epoch
 *       seconds) is the observed time; {@code acc} the accuracy; {@code alt} altitude; {@code vel}
 *       (km/h) becomes {@code speedMs}; {@code cog} course; {@code batt} battery; {@code bs} (2
 *       charging, 3 full) becomes {@code charging}; {@code conn} and {@code tid} are kept.
 *   <li>{@code _type=transition}: a {@code tracker-transition} record ({@code event}, {@code desc},
 *       {@code wtst}) whose externalId is the entity's plus {@value #TRANSITION_SUFFIX}.
 *   <li>{@code _type=encrypted}: dropped and counted ({@value #ENCRYPTED_UNSUPPORTED}); the source is
 *       degraded with "OwnTracks payload encryption is not supported; disable it".
 *   <li>every other type ({@code waypoint}, {@code lwt}, {@code card}, {@code status}, …) is
 *       acknowledged and ignored.
 * </ul>
 *
 * <p>The entity is {@code X-Foresignal-External-Id} when sent, else {@code X-Limit-U/X-Limit-D}, else
 * {@code tid}. These are derived values, not explicit claims: a device key's bound id simply replaces
 * them. The reply is always {@code 200 application/json []}: OwnTracks expects an array of messages
 * for the device, and Foresignal sends none.
 */
@Component
public class OwnTracksFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "owntracks";

    /** Rejection of an encrypted message. */
    public static final String ENCRYPTED_UNSUPPORTED = "ENCRYPTED_UNSUPPORTED";

    /** Appended to the entity id for region transitions, so they never replace the position entity. */
    public static final String TRANSITION_SUFFIX = ":transition";

    private final JsonMapper json;
    private final OwnTracksEncryptionHealthRule encryption;

    /** Creates the format. */
    public OwnTracksFormat(JsonMapper json, OwnTracksEncryptionHealthRule encryption) {
        this.json = json;
        this.encryption = encryption;
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
        return "OwnTracks HTTP mode (location, transition); replies []";
    }

    @Override
    public List<FormatOptionSpec> options() {
        return List.of(LocationRecords.maxAccuracyOption());
    }

    @Override
    public NativeReply nativeReply(ParsedIngest parsed) {
        return NativeReply.json("[]");
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        JsonNode body = LocationRecords.readJson(json, request);
        if (!body.isObject() && !body.isArray()) {
            throw new InvalidInputException("An OwnTracks message (a JSON object) or an array of them is required.");
        }
        double maxAccuracy = LocationRecords.maxAccuracy(options);
        String entity = entity(request);
        ParsedIngest.Builder out = ParsedIngest.builder();
        int index = 0;
        List<JsonNode> messages = new java.util.ArrayList<>();
        if (body.isArray()) {
            for (int i = 0; i < body.size(); i++) {
                messages.add(body.get(i));
            }
        } else {
            messages.add(body);
        }
        for (JsonNode message : messages) {
            map(index++, message, entity, request, descriptor, maxAccuracy, out);
        }
        out.items(index);
        return out.build();
    }

    /** The entity from the headers, or null when they name none (then {@code tid} is used). */
    private static String entity(IngestRequest request) {
        String header = LocationRecords.firstNonBlank(request.header(LocationRecords.EXTERNAL_ID_HEADER));
        if (header != null) {
            return header;
        }
        String user = LocationRecords.firstNonBlank(request.header("X-Limit-U"));
        String device = LocationRecords.firstNonBlank(request.header("X-Limit-D"));
        if (user != null && device != null) {
            return user + "/" + device;
        }
        return LocationRecords.firstNonBlank(user, device);
    }

    private void map(int index, JsonNode message, String entity, IngestRequest request, SourceDescriptor descriptor,
            double maxAccuracy, ParsedIngest.Builder out) {
        if (message == null || !message.isObject()) {
            out.reject(index, null, LocationRecords.INVALID_ITEM, "an OwnTracks message must be an object");
            return;
        }
        String type = LocationRecords.text(message.get("_type"));
        if (type == null) {
            out.reject(index, null, LocationRecords.INVALID_ITEM, "the message has no _type");
            return;
        }
        String tid = LocationRecords.text(message.get("tid"));
        // Derived ids: a device key's bound id replaces them (no mismatch check for OwnTracks).
        String id = request.boundExternalId() != null ? request.boundExternalId()
                : LocationRecords.firstNonBlank(entity, tid);
        switch (type) {
            case "location" -> location(index, message, id, tid, descriptor, maxAccuracy, out);
            case "transition" -> transition(index, message, id, descriptor, out);
            case "encrypted" -> {
                encryption.recordEncrypted(descriptor.id(), request.receivedAt());
                out.reject(index, id, ENCRYPTED_UNSUPPORTED, OwnTracksEncryptionHealthRule.WARNING);
            }
            default -> {
                // waypoint, waypoints, lwt, card, status, cmd, …: acknowledged, nothing stored.
            }
        }
    }

    private static void location(int index, JsonNode m, String id, String tid, SourceDescriptor descriptor,
            double maxAccuracy, ParsedIngest.Builder out) {
        LocationFix fix = new LocationFix();
        fix.externalId = id;
        fix.lat = LocationRecords.number(m.get("lat"));
        fix.lon = LocationRecords.number(m.get("lon"));
        fix.observedAt = epochSeconds(m.get("tst"));
        fix.accuracyMeters = LocationRecords.nonNegative(LocationRecords.number(m.get("acc")));
        fix.altitudeM = LocationRecords.number(m.get("alt"));
        fix.speedMs = LocationRecords.scaled(LocationRecords.nonNegative(LocationRecords.number(m.get("vel"))),
                LocationRecords.KMH_TO_MS);
        fix.courseDeg = LocationRecords.nonNegative(LocationRecords.number(m.get("cog")));
        fix.battery = LocationRecords.percent(LocationRecords.number(m.get("batt")), false);
        fix.charging = charging(LocationRecords.number(m.get("bs")));
        String conn = LocationRecords.text(m.get("conn"));
        if (conn != null) {
            fix.extra.put("conn", conn);
        }
        if (tid != null) {
            fix.extra.put("tid", tid);
        }
        LocationRecords.add(index, fix, descriptor, maxAccuracy, out);
    }

    private static void transition(int index, JsonNode m, String id, SourceDescriptor descriptor,
            ParsedIngest.Builder out) {
        if (id == null) {
            out.reject(index, null, LocationRecords.EXTERNAL_ID_INVALID, "the transition names no device");
            return;
        }
        String transitionId = id + TRANSITION_SUFFIX;
        if (!LocationRecords.declares(descriptor, LocationRecords.KIND_TRANSITION)) {
            out.reject(index, transitionId, LocationRecords.UNKNOWN_KIND,
                    "the source does not declare kind " + LocationRecords.KIND_TRANSITION);
            return;
        }
        Instant observedAt = epochSeconds(m.get("tst"));
        if (observedAt == null) {
            out.reject(index, transitionId, LocationRecords.MISSING_OBSERVED_AT, "the transition carries no tst");
            return;
        }
        Instant wtst = epochSeconds(m.get("wtst"));
        NormalizedRecord.Builder record = NormalizedRecord.builder(transitionId, LocationRecords.KIND_TRANSITION)
                .observedAt(observedAt)
                .eventTime(observedAt)
                .put("event", LocationRecords.text(m.get("event")))
                .put("desc", LocationRecords.text(m.get("desc")))
                .put("wtst", wtst == null ? null : wtst.toString())
                .put("entity", id);
        Double lat = LocationRecords.number(m.get("lat"));
        Double lon = LocationRecords.number(m.get("lon"));
        if (LocationRecords.validCoordinates(lat, lon) && !(lat == 0d && lon == 0d)) {
            record.point(lon, lat);
        }
        out.add(index, record.build());
    }

    /** OwnTracks {@code bs}: 0 unknown, 1 unplugged, 2 charging, 3 full. */
    static Boolean charging(Double bs) {
        if (bs == null) {
            return null;
        }
        int value = bs.intValue();
        return switch (value) {
            case 1 -> Boolean.FALSE;
            case 2, 3 -> Boolean.TRUE;
            default -> null;
        };
    }

    private static Instant epochSeconds(JsonNode node) {
        Double seconds = LocationRecords.number(node);
        return seconds == null || seconds <= 0 ? null : Instant.ofEpochMilli((long) (seconds * 1000d));
    }
}
