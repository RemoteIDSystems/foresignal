package ai.foresignal.inbound.formats.mesh;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.adapters.mesh.MeshCoreEventMapper;
import ai.foresignal.source.adapters.mesh.MeshCoreLocalAdapter;
import ai.foresignal.source.adapters.mesh.MeshMessageBus;
import ai.foresignal.source.adapters.mesh.MeshRecords;
import ai.foresignal.source.adapters.mesh.MeshTextMessage;
import ai.foresignal.source.adapters.mesh.RepeaterHealth;
import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code meshcore-json} ingest format (prompt 083): events from {@code contrib/meshcore-bridge/bridge.py}.
 *
 * <p>The body is one event or {@code {"events":[…]}}; each event is {@code {"type": …, "data": {…}}}, where
 * {@code data} carries the meshcore_py event payload's own keys:
 * <ul>
 *   <li>{@code contact} (a contact-list entry) and {@code advert} (a live advert, resolved by the bridge to the full
 *       contact): {@code public_key, type, adv_name, last_advert, adv_lat, adv_lon, lastmod};
 *   <li>{@code self_info}: {@code public_key, name, adv_type, adv_lat, adv_lon};
 *   <li>{@code channel_msg}: {@code channel_idx, text, sender_timestamp, SNR} and the bridge's {@code channel_name};
 *   <li>{@code contact_msg}: {@code pubkey_prefix, text, sender_timestamp, SNR}.
 * </ul>
 *
 * <p>Events are mapped by the same {@link MeshCoreEventMapper} as {@code meshcore-local}, with the source's
 * {@code includeText} and repeater thresholds. Text messages are published to the {@link MeshMessageBus}, the one
 * side effect of this format, so a bridge behaves like the native link. An event that cannot be mapped is rejected
 * on its own ({@code MESHCORE_EVENT_INVALID}, {@code MESHCORE_EVENT_UNKNOWN}); a body that is not JSON is a 400.
 */
@Component
public class MeshCoreJsonFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "meshcore-json";

    /** The rejection of an event that lacks what its type needs. */
    public static final String INVALID = "MESHCORE_EVENT_INVALID";

    /** The rejection of an event of an unknown type. */
    public static final String UNKNOWN = "MESHCORE_EVENT_UNKNOWN";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Pattern KEY = Pattern.compile("^[0-9a-fA-F]{64}$");
    private static final Pattern PREFIX = Pattern.compile("^[0-9a-fA-F]{12,64}$");

    private final ObjectProvider<SourceService> sources;
    private final ObjectProvider<MeshMessageBus> bus;

    /**
     * Creates the format.
     *
     * @param sources the source settings, for {@code includeText} and the repeater thresholds, resolved lazily
     * @param bus the mesh message bus, resolved lazily
     */
    @Autowired
    public MeshCoreJsonFormat(ObjectProvider<SourceService> sources, ObjectProvider<MeshMessageBus> bus) {
        this.sources = sources;
        this.bus = bus;
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
        return "MeshCore bridge events (contact, advert, self_info, channel_msg, contact_msg) from"
                + " contrib/meshcore-bridge";
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        JsonNode root;
        try {
            root = JSON.readTree(request.body());
        } catch (JacksonException e) {
            throw new InvalidInputException("The body is not JSON: " + e.getOriginalMessage(), e);
        }
        List<JsonNode> events = new ArrayList<>();
        if (root != null && root.isObject() && root.has("events") && root.get("events").isArray()) {
            root.get("events").forEach(events::add);
        } else if (root != null && root.isObject() && root.has("type")) {
            events.add(root);
        } else {
            throw new InvalidInputException("Expected one event {\"type\":…,\"data\":{…}} or {\"events\":[…]}");
        }
        ValidatedParams params = params(descriptor.id());
        RepeaterHealth health;
        try {
            health = new RepeaterHealth(params.duration(MeshCoreLocalAdapter.REPEATER_LATE_AFTER),
                    params.duration(MeshCoreLocalAdapter.REPEATER_SILENT_AFTER));
        } catch (IllegalArgumentException e) {
            health = RepeaterHealth.defaults();
        }
        MeshCoreEventMapper mapper = new MeshCoreEventMapper(descriptor.id(), params.bool(MeshRecords.INCLUDE_TEXT, false),
                health);
        Instant receivedAt = request.receivedAt() == null ? Instant.now() : request.receivedAt();
        ParsedIngest.Builder out = ParsedIngest.builder().items(events.size());
        MeshMessageBus messages = bus == null ? null : bus.getIfAvailable();
        for (int i = 0; i < events.size(); i++) {
            JsonNode event = events.get(i);
            String type = text(event.get("type"));
            JsonNode data = event.path("data");
            if (type == null) {
                out.reject(i, null, INVALID, "the event has no type");
                continue;
            }
            switch (type) {
                case "contact", "advert" -> {
                    MeshCoreFrames.Contact contact = contact(data);
                    if (contact == null) {
                        out.reject(i, null, INVALID, type + " needs public_key (64 hex) and type");
                    } else {
                        out.add(i, mapper.onContact(contact, "advert".equals(type) ? receivedAt : null, receivedAt));
                    }
                }
                case "self_info" -> {
                    String key = text(data.get("public_key"));
                    if (key == null || !KEY.matcher(key).matches()) {
                        out.reject(i, null, INVALID, "self_info needs public_key (64 hex)");
                    } else {
                        Double lat = decimal(data.get("adv_lat"));
                        Double lon = decimal(data.get("adv_lon"));
                        boolean none = lat == null || lon == null || (lat == 0.0 && lon == 0.0);
                        out.add(i, mapper.onSelfInfo(new MeshCoreFrames.SelfInfo(key.toLowerCase(Locale.ROOT),
                                integer(data.get("adv_type"), MeshCoreProtocol.ADV_TYPE_CHAT), none ? null : lat,
                                none ? null : lon, text(data.get("name"))), receivedAt));
                    }
                }
                case "channel_msg", "contact_msg" -> {
                    MeshCoreFrames.TextMessage text = message(type, data);
                    if (text == null) {
                        out.reject(i, null, INVALID, type + " needs text, sender_timestamp and "
                                + ("channel_msg".equals(type) ? "channel_idx" : "pubkey_prefix (hex)"));
                        continue;
                    }
                    if (text.channel()) {
                        String name = text(data.get("channel_name"));
                        if (name != null) {
                            mapper.onChannelName(text.channelIndex(), name);
                        }
                    }
                    MeshCoreEventMapper.Mapped mapped = mapper.onText(text, receivedAt);
                    for (NormalizedRecord record : mapped.records()) {
                        out.add(i, record);
                    }
                    if (messages != null) {
                        for (MeshTextMessage message : mapped.messages()) {
                            messages.publish(message);
                        }
                    }
                }
                default -> out.reject(i, null, UNKNOWN, "unknown event type '" + type + "'");
            }
        }
        return out.build();
    }

    private ValidatedParams params(String sourceId) {
        SourceService service = sources == null ? null : sources.getIfAvailable();
        if (service == null) {
            return ValidatedParams.empty();
        }
        try {
            return service.effectiveParams(sourceId);
        } catch (RuntimeException e) {
            return ValidatedParams.empty();
        }
    }

    /** A contact from a meshcore_py contact payload, or null. */
    static MeshCoreFrames.Contact contact(JsonNode data) {
        String key = text(data.get("public_key"));
        Integer type = integer(data.get("type"), null);
        if (key == null || !KEY.matcher(key).matches() || type == null) {
            return null;
        }
        Double lat = decimal(data.get("adv_lat"));
        Double lon = decimal(data.get("adv_lon"));
        boolean none = lat == null || lon == null || (lat == 0.0 && lon == 0.0) || Math.abs(lat) > 90
                || Math.abs(lon) > 180;
        Long lastAdvert = number(data.get("last_advert"));
        Long lastMod = number(data.get("lastmod"));
        return new MeshCoreFrames.Contact(key.toLowerCase(Locale.ROOT), type, integer(data.get("flags"), 0),
                integer(data.get("out_path_len"), 0), text(data.get("adv_name")),
                lastAdvert == null || lastAdvert <= 0 ? null : Instant.ofEpochSecond(lastAdvert), none ? null : lat,
                none ? null : lon, lastMod == null || lastMod <= 0 ? null : Instant.ofEpochSecond(lastMod));
    }

    /** A text message from a meshcore_py message payload, or null. */
    static MeshCoreFrames.TextMessage message(String type, JsonNode data) {
        String text = data.path("text").isString() ? data.get("text").asString() : null;
        Long ts = number(data.get("sender_timestamp"));
        if (text == null || ts == null) {
            return null;
        }
        Double snr = decimal(data.get("SNR"));
        int pathLength = integer(data.get("path_len"), 0);
        int textType = integer(data.get("txt_type"), 0);
        if ("channel_msg".equals(type)) {
            Integer index = integer(data.get("channel_idx"), null);
            return index == null || index < 0 || index > 255 ? null
                    : new MeshCoreFrames.TextMessage(true, index, null, pathLength, textType, Instant.ofEpochSecond(ts),
                            snr, text);
        }
        String prefix = text(data.get("pubkey_prefix"));
        if (prefix == null || !PREFIX.matcher(prefix).matches()) {
            return null;
        }
        return new MeshCoreFrames.TextMessage(false, null, prefix.toLowerCase(Locale.ROOT), pathLength, textType,
                Instant.ofEpochSecond(ts), snr, text);
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() ? null : value;
    }

    private static Long number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        return node.asLong();
    }

    private static Integer integer(JsonNode node, Integer fallback) {
        Long value = number(node);
        return value == null ? fallback : (int) (long) value;
    }

    private static Double decimal(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double v = node.asDouble();
        return Double.isFinite(v) ? v : null;
    }
}
