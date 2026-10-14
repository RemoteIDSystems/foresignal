package ai.foresignal.notify.types;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.mesh.LoRaAirtime;
import ai.foresignal.notify.mesh.MeshChannelType;
import ai.foresignal.notify.mesh.MeshChannels;
import ai.foresignal.notify.mesh.MeshLanUrls;
import ai.foresignal.notify.mesh.MeshNetworks;
import ai.foresignal.notify.mesh.MeshRenderings;
import ai.foresignal.notify.mesh.MeshShortTextFormatter;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code meshtastic}: alerts on the operator's own Meshtastic mesh through the node's MQTT JSON downlink (prompt 084).
 * No Meshtastic protobuf is used (it is GPL): the node's JSON integration carries text.
 *
 * <p>Each delivery is one PUBLISH at QoS 1 on the shared client of the broker ({@code foresignal-<installId>-mesh},
 * {@code MeshtasticDownlinks}) to {@code {root}/2/json/mqtt/} ({@code !{gatewayHex}} appended when
 * {@code topicSuffixNodeId}) with {@code {"from": gatewayNodeNum, "type": "sendtext", "payload": text, "channel":
 * channelIndex[, "to": toNodeNum]}}. The text is {@link MeshShortTextFormatter}'s line (at most 200 UTF-8 bytes), or
 * with {@code profile=cbor} {@code ~E1 } and the base64url CBOR message. The delivery is {@code sent} once the broker
 * PUBACKs: JSON gives no delivery acknowledgement. Every send passes the channel's airtime governor first.
 *
 * <p><strong>Node prerequisites:</strong> JSON enabled in the node's MQTT settings (not available on nRF52), a channel
 * named {@code mqtt} with downlink enabled, and MQTT encryption disabled toward the private broker.
 *
 * <p><strong>Never public</strong> (400 {@code MESH_PUBLIC_CHANNEL} at create and update): a broker under
 * {@code meshtastic.org} or not on the LAN, channel index 0, an empty or modem-preset channel name, licensed (ham)
 * mode, which disables encryption, or {@code privateChannelConfirmed} not true.
 */
@Component
public class MeshtasticChannel extends AbstractChannelType implements MeshChannelType {

    /** The type id. */
    public static final String ID = MeshNetworks.MESHTASTIC;

    /** The default MQTT root topic. */
    public static final String DEFAULT_ROOT = "msh/local";

    /** The channel names of the modem presets, whose default keys are public. */
    public static final Set<String> PRESET_NAMES = Set.of("longfast", "longslow", "longmoderate", "mediumfast",
            "mediumslow", "shortfast", "shortslow", "shortturbo", "vlongslow");

    /** The largest unsigned 32-bit node number. */
    static final long MAX_NODE_NUM = 0xFFFF_FFFFL;

    private static final Pattern ROOT = Pattern.compile("^[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+){0,7}$");
    private static final Pattern DECIMAL = Pattern.compile("^\\d{1,10}$");

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Meshtastic",
            "Short alert lines on the operator's own private Meshtastic channel through the node's MQTT JSON downlink;"
                    + " never on a public or default channel",
            fields(), List.of(ChannelFieldSpec.optional("username", ChannelFieldSpec.STRING, "Broker user name", null),
                    ChannelFieldSpec.optional("password", ChannelFieldSpec.STRING, "Broker password", null)),
            null);

    private final OutboundUrlPolicy policy;

    /**
     * Creates the type.
     *
     * @param policy classifies the broker host (it must be on the LAN)
     */
    @Autowired
    public MeshtasticChannel(OutboundUrlPolicy policy) {
        this.policy = policy;
    }

    private static List<ChannelFieldSpec> fields() {
        List<ChannelFieldSpec> out = new ArrayList<>(List.of(
                ChannelFieldSpec.required("brokerUrl", ChannelFieldSpec.STRING,
                        "The private broker on the LAN: mqtt://host:port or mqtts://host:port (never *.meshtastic.org)"),
                ChannelFieldSpec.optional("root", ChannelFieldSpec.STRING, "The node's MQTT root topic", DEFAULT_ROOT),
                ChannelFieldSpec.required("gatewayNodeNum", ChannelFieldSpec.STRING,
                        "The gateway node's number as unsigned decimal (the downlink's from)"),
                ChannelFieldSpec.required(MeshChannels.CHANNEL_INDEX, ChannelFieldSpec.INTEGER,
                        "The private channel's index, 1-7 (0 is the primary, usually public, channel)"),
                ChannelFieldSpec.required("channelName", ChannelFieldSpec.STRING,
                        "The private channel's name (not a modem-preset name such as LongFast)"),
                ChannelFieldSpec.optional("toNodeNum", ChannelFieldSpec.STRING,
                        "A node number (unsigned decimal) for a direct message instead of the channel", null),
                ChannelFieldSpec.optional("topicSuffixNodeId", ChannelFieldSpec.BOOLEAN,
                        "Append !<gatewayHex> to the downlink topic (some firmware needs it)", false),
                ChannelFieldSpec.optional("licensedMode", ChannelFieldSpec.BOOLEAN,
                        "Must be false: licensed (ham) mode disables encryption", false),
                ChannelFieldSpec.required("privateChannelConfirmed", ChannelFieldSpec.BOOLEAN,
                        "Must be true: confirms the channel has its own random key")));
        out.addAll(MeshChannels.fields(LoRaAirtime.LONGFAST));
        return out;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String network() {
        return MeshNetworks.MESHTASTIC;
    }

    @Override
    public ChannelTypeSpec spec() {
        return SPEC;
    }

    @Override
    public ChannelRateLimit rateLimit() {
        // The airtime governor is the real limit; this only stops a burst before it reaches it.
        return ChannelRateLimit.perMinute(30);
    }

    @Override
    public boolean acceptsUpdate(String subtype) {
        return "alert.resolved".equals(subtype);
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        List<FieldError> errors = new ArrayList<>(MeshChannels.validate(config, LoRaAirtime.LONGFAST));
        Object root = config.get("root");
        if (root instanceof String r && !r.isBlank() && !ROOT.matcher(r.strip()).matches()) {
            errors.add(new FieldError("config.root", "must be 1-8 topic levels of A-Z a-z 0-9 _ -, without wildcards"
                    + " or a leading or trailing /"));
        }
        if (nodeNum(config.get("gatewayNodeNum")) == null) {
            errors.add(new FieldError("config.gatewayNodeNum", "must be an unsigned 32-bit decimal node number"));
        }
        Object to = config.get("toNodeNum");
        if (to != null && !(to instanceof String s && s.isBlank()) && nodeNum(to) == null) {
            errors.add(new FieldError("config.toNodeNum", "must be an unsigned 32-bit decimal node number"));
        }
        if (config.get(MeshChannels.CHANNEL_INDEX) instanceof Number n && (n.intValue() < 0 || n.intValue() > 7)) {
            errors.add(new FieldError("config.channelIndex", "must be 1 to 7"));
        }
        errors.addAll(publicProblems(config));
        return errors;
    }

    @Override
    public String validationCode(Map<String, Object> config, Map<String, String> secrets) {
        return publicProblems(config).isEmpty() ? null : ErrorCodes.MESH_PUBLIC_CHANNEL;
    }

    /** The public-channel rules {@code config} breaks; empty when the channel is private. */
    public List<FieldError> publicProblems(Map<String, Object> config) {
        List<FieldError> out = new ArrayList<>();
        Object broker = config.get("brokerUrl");
        if (broker instanceof String url) {
            String host = host(url);
            if (host != null && (host.equals("meshtastic.org") || host.endsWith(".meshtastic.org"))) {
                out.add(new FieldError("config.brokerUrl", "is a public Meshtastic broker; use your own broker on"
                        + " the LAN"));
            } else {
                MeshLanUrls.Outcome outcome = MeshLanUrls.pin(url, Set.of("mqtt", "mqtts"), policy);
                if (outcome instanceof MeshLanUrls.Refused refused) {
                    out.add(new FieldError("config.brokerUrl", refused.reason()));
                }
            }
        }
        if (config.get(MeshChannels.CHANNEL_INDEX) instanceof Number n && n.intValue() == 0) {
            out.add(new FieldError("config.channelIndex", "0 is the primary channel, which is public by default;"
                    + " use a private channel 1-7"));
        }
        Object name = config.get("channelName");
        if (!(name instanceof String s) || s.isBlank()) {
            out.add(new FieldError("config.channelName", "is required: the private channel's name"));
        } else if (PRESET_NAMES.contains(s.strip().toLowerCase(Locale.ROOT))) {
            out.add(new FieldError("config.channelName", "'" + s + "' is a modem-preset channel, whose default key"
                    + " is public"));
        }
        if (Boolean.TRUE.equals(config.get("licensedMode"))) {
            out.add(new FieldError("config.licensedMode", "must be false: licensed (ham) mode disables encryption"));
        }
        if (!Boolean.TRUE.equals(config.get("privateChannelConfirmed"))) {
            out.add(new FieldError("config.privateChannelConfirmed", "must be true: confirm that the channel has its"
                    + " own random key"));
        }
        return out;
    }

    @Override
    public MeshChannels.Common common(Map<String, Object> config) {
        return MeshChannels.common(config, LoRaAirtime.LONGFAST);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        MeshChannels.Common c = common(s.config());
        String text;
        if (MeshChannels.PROFILE_CBOR.equals(c.profile())) {
            text = MeshRenderings.cborText(MeshCborProfile.encode(MeshRenderings.cbor(m),
                    MeshRenderings.MESHTASTIC_CBOR_MAX));
        } else {
            text = MeshShortTextFormatter.format(m, MeshShortTextFormatter.Limit.MESHTASTIC);
        }
        return transmission(text, s, m.severity(), MeshRenderings.shortCode(m), MeshRenderings.folded(m), true);
    }

    @Override
    public PreparedRequest renderReply(String text, ChannelSettings s) {
        return transmission(text, s, "info", null, 0, false);
    }

    private PreparedRequest transmission(String text, ChannelSettings s, String severity, String code, int folded,
            boolean delivery) {
        Map<String, Object> config = s.config();
        long gateway = nodeNum(config.get("gatewayNodeNum"));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", gateway);
        body.put("type", "sendtext");
        body.put("payload", text);
        body.put("channel", ((Number) config.get(MeshChannels.CHANNEL_INDEX)).intValue());
        Long to = delivery ? nodeNum(config.get("toNodeNum")) : null;
        if (to != null) {
            body.put("to", to);
        }
        byte[] json = JSON.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        return new PreparedRequest("TRANSMIT", String.valueOf(config.get("brokerUrl")), Map.of(),
                new PreparedRequest.Mesh(s.channelId(), MeshNetworks.MESHTASTIC, topic(config), json,
                        MeshRenderings.bytes(text), severity, code, delivery ? s.deliveryId() : 0, folded, false));
    }

    /** The downlink topic: {@code {root}/2/json/mqtt/}, plus {@code !{gatewayHex}} with {@code topicSuffixNodeId}. */
    public static String topic(Map<String, Object> config) {
        String root = config.get("root") instanceof String r && !r.isBlank() ? r.strip() : DEFAULT_ROOT;
        String topic = root + "/2/json/mqtt/";
        if (Boolean.TRUE.equals(config.get("topicSuffixNodeId"))) {
            Long gateway = nodeNum(config.get("gatewayNodeNum"));
            topic += String.format("!%08x", gateway == null ? 0 : gateway);
        }
        return topic;
    }

    /** An unsigned 32-bit node number written as decimal text (or a JSON integer), or null. */
    static Long nodeNum(Object value) {
        String text = value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())
                ? Long.toString(n.longValue()) : value instanceof String s ? s.strip() : null;
        if (text == null || !DECIMAL.matcher(text).matches()) {
            return null;
        }
        long number = Long.parseLong(text);
        return number <= MAX_NODE_NUM ? number : null;
    }

    private static String host(String url) {
        try {
            String host = java.net.URI.create(url.strip()).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
