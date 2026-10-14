package ai.foresignal.notify.types;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.mesh.LoRaAirtime;
import ai.foresignal.notify.mesh.MeshChannelType;
import ai.foresignal.notify.mesh.MeshChannels;
import ai.foresignal.notify.mesh.MeshCoreSlotProbe;
import ai.foresignal.notify.mesh.MeshLanUrls;
import ai.foresignal.notify.mesh.MeshNetworks;
import ai.foresignal.notify.mesh.MeshRenderings;
import ai.foresignal.notify.mesh.MeshShortTextFormatter;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code meshcore}: alerts on the operator's own MeshCore mesh through the MIT companion protocol (prompt 084), over
 * the one TCP link prompt 083 shares per companion ({@code MeshCoreSender}).
 *
 * <ul>
 *   <li><strong>Channel text</strong> (default): {@code CMD_SEND_CHANNEL_TXT_MSG} {@code 03 00 {idx} {ts u32 LE}
 *       {utf8}}, at most 133 characters and 127 bytes. No acknowledgement exists for channel messages.
 *   <li><strong>CBOR profile</strong> ({@code profile=cbor}): {@code CMD_SEND_CHANNEL_DATA} {@code 3E {idx} FF
 *       {dataType u16 LE} {payload}} with at most 160 bytes; {@code dataType} defaults to {@code 0xFF45}, in the
 *       0xFF00–0xFFFE development range (a production deployment registers a value through MeshCore's
 *       {@code number_allocations.md}).
 *   <li><strong>Direct message</strong> ({@code dmContactPrefix}, a 12-hex public-key prefix of a contact):
 *       {@code CMD_SEND_TXT_MSG} {@code 02 00 {attempt} {ts u32 LE} {prefix 6 bytes} {utf8}}. With {@code requireAck}
 *       the sender waits for {@code PUSH_CODE_SEND_CONFIRMED}, retries once with {@code attempt=1}, and the delivery
 *       ends {@code unacked} when no acknowledgement comes.
 * </ul>
 *
 * <p>The send commands are registered with 083's {@code MeshCoreCommandGuard}; every other command stays refused.
 * Every send passes the channel's airtime governor first. MeshCore radio settings vary by region, so
 * {@code airtimePreset=custom} is the default here.
 *
 * <p><strong>Never public</strong> (400 {@code MESH_PUBLIC_CHANNEL}): channel index 0, and, read from the companion
 * with {@code CMD_GET_CHANNEL} at create and update and again on every link (re)connect, a slot named {@code Public}
 * or {@code #…} (hashtag channels derive their key from the name), an empty slot or an all-zero secret. When the
 * companion cannot be reached at save time the channel is saved, and nothing is sent until a connect verified the
 * slot.
 */
@Component
public class MeshCoreChannel extends AbstractChannelType implements MeshChannelType {

    /** The type id. */
    public static final String ID = MeshNetworks.MESHCORE;

    /** The default channel-datagram data type of the CBOR profile ({@code 0xFF45}). */
    public static final int DEFAULT_DATA_TYPE = 0xFF45;

    /** The response body of an acknowledged-required message that was never acknowledged. */
    public static final String UNACKED = "UNACKED";

    private static final HexFormat HEX = HexFormat.of();

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "MeshCore",
            "Short alert lines on the operator's own private MeshCore channel (or to one contact) through the"
                    + " companion protocol over TCP; never on a public or hashtag channel",
            fields(), List.of(), null);

    private final OutboundUrlPolicy policy;
    private final ObjectProvider<MeshCoreSlotProbe> probe;

    /**
     * Creates the type.
     *
     * @param policy classifies the companion host (it must be on the LAN)
     * @param probe reads the channel slot from the companion at create and update, resolved lazily
     */
    @Autowired
    public MeshCoreChannel(OutboundUrlPolicy policy, ObjectProvider<MeshCoreSlotProbe> probe) {
        this.policy = policy;
        this.probe = probe;
    }

    private static List<ChannelFieldSpec> fields() {
        List<ChannelFieldSpec> out = new ArrayList<>(List.of(
                ChannelFieldSpec.required("companionUrl", ChannelFieldSpec.STRING,
                        "The WiFi companion on the LAN: tcp://host:port (explicit port, private address)"),
                ChannelFieldSpec.required(MeshChannels.CHANNEL_INDEX, ChannelFieldSpec.INTEGER,
                        "The private channel slot, 1-7 (slot 0 is Public)"),
                ChannelFieldSpec.optional("dmContactPrefix", ChannelFieldSpec.STRING,
                        "A contact's 12-hex public-key prefix for direct messages instead of the channel", null),
                ChannelFieldSpec.optional("requireAck", ChannelFieldSpec.BOOLEAN,
                        "Direct messages only: wait for the contact's acknowledgement (unacknowledged deliveries end"
                                + " unacked)", false),
                ChannelFieldSpec.optional("dataType", ChannelFieldSpec.INTEGER,
                        "Channel-datagram data type of the CBOR profile, 0xFF00-0xFFFE (65280-65534)",
                        DEFAULT_DATA_TYPE)));
        out.addAll(MeshChannels.fields(LoRaAirtime.CUSTOM));
        return out;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String network() {
        return MeshNetworks.MESHCORE;
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
    public boolean unacknowledged(ChannelResponse response) {
        return UNACKED.equals(response.body());
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        List<FieldError> errors = new ArrayList<>(MeshChannels.validate(config, LoRaAirtime.CUSTOM));
        if (config.get(MeshChannels.CHANNEL_INDEX) instanceof Number n && (n.intValue() < 0 || n.intValue() > 7)) {
            errors.add(new FieldError("config.channelIndex", "must be 1 to 7"));
        }
        Object prefix = config.get("dmContactPrefix");
        boolean dm = prefix instanceof String p && !p.isBlank();
        if (dm && !((String) prefix).strip().matches("[0-9a-fA-F]{12}")) {
            errors.add(new FieldError("config.dmContactPrefix", "must be 12 hex digits (a 6-byte public-key prefix)"));
        }
        if (Boolean.TRUE.equals(config.get("requireAck")) && !dm) {
            errors.add(new FieldError("config.requireAck", "applies to direct messages only (set dmContactPrefix)"));
        }
        if (dm && MeshChannels.PROFILE_CBOR.equals(config.get(MeshChannels.PROFILE))) {
            errors.add(new FieldError("config.profile", "cbor goes out as a channel datagram; it cannot be used with"
                    + " dmContactPrefix"));
        }
        Object dataType = config.get("dataType");
        if (dataType instanceof Number n && (n.intValue() < 0xFF00 || n.intValue() > 0xFFFE)) {
            errors.add(new FieldError("config.dataType", "must be 65280-65534 (0xFF00-0xFFFE, the development range)"));
        }
        if (config.get("companionUrl") instanceof String url
                && MeshLanUrls.pin(url, Set.of("tcp"), policy) instanceof MeshLanUrls.Refused refused) {
            errors.add(new FieldError("config.companionUrl", refused.reason()));
        }
        errors.addAll(publicProblems(config));
        return errors;
    }

    @Override
    public String validationCode(Map<String, Object> config, Map<String, String> secrets) {
        return publicProblems(config).isEmpty() ? null : ErrorCodes.MESH_PUBLIC_CHANNEL;
    }

    /**
     * The public-channel rules {@code config} breaks: slot 0, or a slot the companion reports as public. An
     * unreachable companion is no problem here (the slot is checked again on every connect); empty when private.
     */
    public List<FieldError> publicProblems(Map<String, Object> config) {
        List<FieldError> out = new ArrayList<>();
        if (!(config.get(MeshChannels.CHANNEL_INDEX) instanceof Number n)) {
            return out;
        }
        int index = n.intValue();
        if (index == 0) {
            out.add(new FieldError("config.channelIndex", "slot 0 is the Public channel; use a private slot 1-7"));
            return out;
        }
        if (index < 1 || index > 7) {
            return out;
        }
        MeshCoreSlotProbe slots = probe == null ? null : probe.getIfAvailable();
        if (slots != null && config.get("companionUrl") instanceof String url
                && MeshLanUrls.pin(url, Set.of("tcp"), policy) instanceof MeshLanUrls.Pinned pinned) {
            MeshCoreSlotProbe.SlotCheck check = slots.check(pinned.endpoint(), index);
            if (check.status() == MeshCoreSlotProbe.Status.PUBLIC) {
                out.add(new FieldError("config.channelIndex", check.reason()));
            }
        }
        return out;
    }

    @Override
    public MeshChannels.Common common(Map<String, Object> config) {
        return MeshChannels.common(config, LoRaAirtime.CUSTOM);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        Map<String, Object> config = s.config();
        MeshChannels.Common c = common(config);
        String code = MeshRenderings.shortCode(m);
        int folded = MeshRenderings.folded(m);
        String prefix = config.get("dmContactPrefix") instanceof String p && !p.isBlank() ? p.strip() : null;
        if (prefix != null) {
            String text = MeshShortTextFormatter.format(m, MeshShortTextFormatter.Limit.MESHCORE_DIRECT);
            byte[] payload = directText(0, s.now().getEpochSecond(), prefix, text);
            return request(s, "dm", payload, MeshRenderings.bytes(text), m.severity(), code, folded,
                    Boolean.TRUE.equals(config.get("requireAck")));
        }
        if (MeshChannels.PROFILE_CBOR.equals(c.profile())) {
            byte[] cbor = MeshCborProfile.encode(MeshRenderings.cbor(m));
            int dataType = config.get("dataType") instanceof Number n ? n.intValue() : DEFAULT_DATA_TYPE;
            return request(s, "datagram", channelData(c.channelIndex(), dataType, cbor), cbor.length, m.severity(),
                    code, folded, false);
        }
        String text = MeshShortTextFormatter.format(m, MeshShortTextFormatter.Limit.MESHCORE_CHANNEL);
        return request(s, "channel", channelText(c.channelIndex(), s.now().getEpochSecond(), text),
                MeshRenderings.bytes(text), m.severity(), code, folded, false);
    }

    @Override
    public PreparedRequest renderReply(String text, ChannelSettings s) {
        int index = ((Number) s.config().get(MeshChannels.CHANNEL_INDEX)).intValue();
        String reply = cut(text.replaceAll("[\\p{Cntrl}]+", " ").strip());
        return new PreparedRequest("TRANSMIT", String.valueOf(s.config().get("companionUrl")), Map.of(),
                new PreparedRequest.Mesh(s.channelId(), MeshNetworks.MESHCORE, "channel",
                        channelText(index, s.now().getEpochSecond(), reply), MeshRenderings.bytes(reply), "info", null,
                        0, 0, false));
    }

    private static String cut(String text) {
        return ai.foresignal.notify.Truncator.truncate(ai.foresignal.notify.Truncator.truncate(text,
                MeshShortTextFormatter.Limit.MESHCORE_CHANNEL.maxChars(), ai.foresignal.notify.Truncator.Unit.CHARS,
                ai.foresignal.notify.Truncator.Mode.PLAIN, null), MeshShortTextFormatter.Limit.MESHCORE_CHANNEL.maxBytes(),
                ai.foresignal.notify.Truncator.Unit.UTF8_BYTES, ai.foresignal.notify.Truncator.Mode.PLAIN, null);
    }

    private static PreparedRequest request(ChannelSettings s, String topic, byte[] payload, int airtimeBytes,
            String severity, String code, int folded, boolean requireAck) {
        return new PreparedRequest("TRANSMIT", String.valueOf(s.config().get("companionUrl")), Map.of(),
                new PreparedRequest.Mesh(s.channelId(), MeshNetworks.MESHCORE, topic, payload, airtimeBytes, severity,
                        code, s.deliveryId(), folded, requireAck));
    }

    /** {@code CMD_SEND_CHANNEL_TXT_MSG}: {@code 03 00 {idx} {ts u32 LE} {utf8}}. */
    public static byte[] channelText(int index, long epochSeconds, String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MeshCoreProtocol.CMD_SEND_CHANNEL_TXT_MSG);
        out.write(0);
        out.write(index);
        writeU32(out, epochSeconds);
        out.writeBytes(text.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    /** {@code CMD_SEND_CHANNEL_DATA}: {@code 3E {idx} FF {dataType u16 LE} {payload}} (flood, no path). */
    public static byte[] channelData(int index, int dataType, byte[] payload) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MeshCoreProtocol.CMD_SEND_CHANNEL_DATA);
        out.write(index);
        out.write(0xFF);
        out.write(dataType & 0xFF);
        out.write((dataType >>> 8) & 0xFF);
        out.writeBytes(payload);
        return out.toByteArray();
    }

    /** {@code CMD_SEND_TXT_MSG}: {@code 02 00 {attempt} {ts u32 LE} {prefix 6 bytes} {utf8}} (plain text). */
    public static byte[] directText(int attempt, long epochSeconds, String prefixHex, String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MeshCoreProtocol.CMD_SEND_TXT_MSG);
        out.write(0);
        out.write(attempt);
        writeU32(out, epochSeconds);
        out.writeBytes(HEX.parseHex(prefixHex.toLowerCase(java.util.Locale.ROOT)));
        out.writeBytes(text.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private static void writeU32(ByteArrayOutputStream out, long value) {
        out.write((int) (value & 0xFF));
        out.write((int) ((value >>> 8) & 0xFF));
        out.write((int) ((value >>> 16) & 0xFF));
        out.write((int) ((value >>> 24) & 0xFF));
    }
}
