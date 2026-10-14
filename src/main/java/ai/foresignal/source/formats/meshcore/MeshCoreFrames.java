package ai.foresignal.source.formats.meshcore;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

/**
 * The MeshCore companion frames Foresignal builds and reads (prompt 083). Layouts follow the companion firmware
 * ({@code writeContactRespFrame}, the {@code RESP_CODE_*} writers) and {@code docs/companion_protocol.md}; all
 * integers are little-endian and strings UTF-8.
 *
 * <p>Commands are built as payloads ({@code appStart()}, …) and wrapped for TCP by {@link #wire}. Decoders return
 * null for a frame that is too short or of another code, never throw on radio input, and never read a channel's
 * secret.
 */
public final class MeshCoreFrames {

    /** The app name sent with {@code CMD_APP_START}. */
    public static final String APP_NAME = "Foresignal";

    /** The length of a contact frame, code included: code, key, type, flags, path length, path, name, 4 × uint32. */
    public static final int CONTACT_FRAME_LENGTH = 1 + MeshCoreProtocol.PUB_KEY_SIZE + 3 + MeshCoreProtocol.MAX_PATH_SIZE
            + MeshCoreProtocol.NAME_SIZE + 16;

    /** The hex characters of a node id's key prefix ({@code mc:} + 12 hex = 6 bytes). */
    public static final int KEY_PREFIX_HEX = 12;

    private static final HexFormat HEX = HexFormat.of();

    private MeshCoreFrames() {}

    // ------------------------------------------------------------------ commands

    /** Wraps a command payload for TCP: {@code '<'}, the uint16 length, the payload. */
    public static byte[] wire(byte[] payload) {
        if (payload == null || payload.length == 0 || payload.length > MeshCoreProtocol.MAX_FRAME_SIZE) {
            throw new IllegalArgumentException("a command payload must be 1 to " + MeshCoreProtocol.MAX_FRAME_SIZE
                    + " bytes");
        }
        byte[] out = new byte[payload.length + 3];
        out[0] = MeshCoreProtocol.APP_TO_RADIO;
        out[1] = (byte) (payload.length & 0xFF);
        out[2] = (byte) (payload.length >>> 8);
        System.arraycopy(payload, 0, out, 3, payload.length);
        return out;
    }

    /** {@code CMD_APP_START}: the code, 7 reserved bytes and {@value #APP_NAME}. */
    public static byte[] appStart() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MeshCoreProtocol.CMD_APP_START);
        out.writeBytes(new byte[7]);
        out.writeBytes(APP_NAME.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    /** {@code CMD_DEVICE_QUERY} for protocol version 3: {@code 16 03}. */
    public static byte[] deviceQuery() {
        return new byte[] {MeshCoreProtocol.CMD_DEVICE_QUERY, MeshCoreProtocol.DEVICE_QUERY_VERSION};
    }

    /** {@code CMD_GET_CONTACTS} without a {@code since} filter. */
    public static byte[] getContacts() {
        return new byte[] {MeshCoreProtocol.CMD_GET_CONTACTS};
    }

    /** {@code CMD_SYNC_NEXT_MESSAGE}. */
    public static byte[] syncNextMessage() {
        return new byte[] {MeshCoreProtocol.CMD_SYNC_NEXT_MESSAGE};
    }

    /** {@code CMD_GET_DEVICE_TIME}. */
    public static byte[] getDeviceTime() {
        return new byte[] {MeshCoreProtocol.CMD_GET_DEVICE_TIME};
    }

    /** {@code CMD_GET_BATT_AND_STORAGE}. */
    public static byte[] getBatteryAndStorage() {
        return new byte[] {MeshCoreProtocol.CMD_GET_BATT_AND_STORAGE};
    }

    /** {@code CMD_GET_CONTACT_BY_KEY} with a 32-byte public key. */
    public static byte[] getContactByKey(byte[] publicKey) {
        if (publicKey == null || publicKey.length != MeshCoreProtocol.PUB_KEY_SIZE) {
            throw new IllegalArgumentException("a public key is " + MeshCoreProtocol.PUB_KEY_SIZE + " bytes");
        }
        byte[] out = new byte[1 + MeshCoreProtocol.PUB_KEY_SIZE];
        out[0] = MeshCoreProtocol.CMD_GET_CONTACT_BY_KEY;
        System.arraycopy(publicKey, 0, out, 1, publicKey.length);
        return out;
    }

    /** {@code CMD_GET_CHANNEL} for slot {@code index} (0 to 7). */
    public static byte[] getChannel(int index) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException("a channel index is 0 to 7, but was " + index);
        }
        return new byte[] {MeshCoreProtocol.CMD_GET_CHANNEL, (byte) index};
    }

    // ------------------------------------------------------------------ frames

    /** The code of a frame (its first byte, unsigned), or -1 for an empty frame. */
    public static int code(byte[] frame) {
        return frame == null || frame.length == 0 ? -1 : frame[0] & 0xFF;
    }

    /**
     * A contact, from {@code RESP_CODE_CONTACT} or {@code PUSH_CODE_NEW_ADVERT}.
     *
     * @param publicKey the 32-byte key, lower-case hex
     * @param type the advert type ({@link MeshCoreProtocol#ADV_TYPE_CHAT} …)
     * @param flags the contact flags
     * @param outPathLength the stored path length
     * @param name the name, up to its NUL
     * @param lastAdvert the advert's own timestamp (the node's clock, which may drift), or null when 0
     * @param latitude degrees, or null when the node shared no position (0, 0)
     * @param longitude degrees, or null when the node shared no position (0, 0)
     * @param lastModified when the companion last changed the contact, or null when 0
     */
    public record Contact(String publicKey, int type, int flags, int outPathLength, String name, Instant lastAdvert,
            Double latitude, Double longitude, Instant lastModified) {

        /** The node id: {@code mc:} and the first 12 hex characters of the key. */
        public String nodeId() {
            return MeshCoreFrames.nodeId(publicKey);
        }
    }

    /** The node id of a hex public key or key prefix: {@code mc:} and its first 12 hex characters, lower case. */
    public static String nodeId(String publicKeyHex) {
        String hex = publicKeyHex.toLowerCase(Locale.ROOT);
        return "mc:" + hex.substring(0, Math.min(KEY_PREFIX_HEX, hex.length()));
    }

    /** A contact frame, or null when it is not one or is too short. */
    public static Contact contact(byte[] frame) {
        int code = code(frame);
        if ((code != MeshCoreProtocol.RESP_CONTACT && code != MeshCoreProtocol.PUSH_NEW_ADVERT)
                || frame.length < CONTACT_FRAME_LENGTH - 8) {
            return null;
        }
        int i = 1;
        String key = HEX.formatHex(frame, i, i + MeshCoreProtocol.PUB_KEY_SIZE);
        i += MeshCoreProtocol.PUB_KEY_SIZE;
        int type = frame[i++] & 0xFF;
        int flags = frame[i++] & 0xFF;
        int pathLength = frame[i++] & 0xFF;
        i += MeshCoreProtocol.MAX_PATH_SIZE;
        String name = cString(frame, i, MeshCoreProtocol.NAME_SIZE);
        i += MeshCoreProtocol.NAME_SIZE;
        long lastAdvert = u32(frame, i);
        i += 4;
        Double lat = null;
        Double lon = null;
        Instant lastMod = null;
        if (frame.length >= i + 8) {
            int latE6 = i32(frame, i);
            int lonE6 = i32(frame, i + 4);
            i += 8;
            if ((latE6 != 0 || lonE6 != 0) && Math.abs(latE6) <= 90_000_000 && Math.abs(lonE6) <= 180_000_000) {
                lat = latE6 / 1e6;
                lon = lonE6 / 1e6;
            }
        }
        if (frame.length >= i + 4) {
            long mod = u32(frame, i);
            lastMod = mod == 0 ? null : Instant.ofEpochSecond(mod);
        }
        return new Contact(key, type, flags, pathLength, name, lastAdvert == 0 ? null : Instant.ofEpochSecond(lastAdvert),
                lat, lon, lastMod);
    }

    /** The 32-byte key of a {@code PUSH_CODE_ADVERT}, or null when the frame is not one. */
    public static byte[] advertKey(byte[] frame) {
        if (code(frame) != MeshCoreProtocol.PUSH_ADVERT || frame.length < 1 + MeshCoreProtocol.PUB_KEY_SIZE) {
            return null;
        }
        byte[] key = new byte[MeshCoreProtocol.PUB_KEY_SIZE];
        System.arraycopy(frame, 1, key, 0, key.length);
        return key;
    }

    /**
     * The companion's own node, from {@code RESP_CODE_SELF_INFO}.
     *
     * @param publicKey lower-case hex
     * @param advType the advert type the companion identifies as
     * @param latitude degrees, or null when none is set
     * @param longitude degrees, or null when none is set
     * @param name the node name
     */
    public record SelfInfo(String publicKey, int advType, Double latitude, Double longitude, String name) {

        /** The node id. */
        public String nodeId() {
            return MeshCoreFrames.nodeId(publicKey);
        }
    }

    /** A self-info frame, or null. */
    public static SelfInfo selfInfo(byte[] frame) {
        if (code(frame) != MeshCoreProtocol.RESP_SELF_INFO || frame.length < 44) {
            return null;
        }
        String key = HEX.formatHex(frame, 4, 36);
        int latE6 = i32(frame, 36);
        int lonE6 = i32(frame, 40);
        Double lat = latE6 == 0 && lonE6 == 0 ? null : latE6 / 1e6;
        Double lon = latE6 == 0 && lonE6 == 0 ? null : lonE6 / 1e6;
        String name = frame.length > 58 ? cString(frame, 58, frame.length - 58) : null;
        return new SelfInfo(key, frame[1] & 0xFF, lat, lon, name == null || name.isBlank() ? null : name);
    }

    /** A channel slot's name from {@code RESP_CODE_CHANNEL_INFO}; the secret that follows is never read. */
    public record ChannelName(int index, String name) {}

    /** A channel-info frame's index and name (null name for an empty slot), or null. */
    public static ChannelName channelName(byte[] frame) {
        if (code(frame) != MeshCoreProtocol.RESP_CHANNEL_INFO || frame.length < 2 + MeshCoreProtocol.NAME_SIZE) {
            return null;
        }
        String name = cString(frame, 2, MeshCoreProtocol.NAME_SIZE);
        return new ChannelName(frame[1] & 0xFF, name.isEmpty() ? null : name);
    }

    /**
     * A received text message.
     *
     * @param channel true for a channel message, false for a direct (contact) message
     * @param channelIndex the channel slot, or null for a direct message
     * @param fromKeyPrefix the sender's 6-byte key prefix in hex, or null for a channel message (the firmware does
     *     not say who sent a channel message; the text conventionally starts with {@code name: })
     * @param pathLength the path length byte
     * @param textType the text type (2 is signed plain text)
     * @param senderTimestamp the sender's clock
     * @param snr the SNR in dB (V3 frames), or null
     * @param text the text
     */
    public record TextMessage(boolean channel, Integer channelIndex, String fromKeyPrefix, int pathLength,
            int textType, Instant senderTimestamp, Double snr, String text) {}

    /** A contact or channel message frame (standard or V3), or null. */
    public static TextMessage textMessage(byte[] frame) {
        int code = code(frame);
        try {
            switch (code) {
                case MeshCoreProtocol.RESP_CONTACT_MSG_RECV, MeshCoreProtocol.RESP_CONTACT_MSG_RECV_V3 -> {
                    boolean v3 = code == MeshCoreProtocol.RESP_CONTACT_MSG_RECV_V3;
                    int i = v3 ? 4 : 1;
                    if (frame.length < i + 12) {
                        return null;
                    }
                    Double snr = v3 ? frame[1] / 4.0 : null;
                    String prefix = HEX.formatHex(frame, i, i + 6);
                    int pathLength = frame[i + 6] & 0xFF;
                    int textType = frame[i + 7] & 0xFF;
                    long ts = u32(frame, i + 8);
                    int textAt = i + 12 + (textType == 2 ? 4 : 0);
                    if (textAt > frame.length) {
                        return null;
                    }
                    return new TextMessage(false, null, prefix, pathLength, textType, Instant.ofEpochSecond(ts), snr,
                            new String(frame, textAt, frame.length - textAt, StandardCharsets.UTF_8));
                }
                case MeshCoreProtocol.RESP_CHANNEL_MSG_RECV, MeshCoreProtocol.RESP_CHANNEL_MSG_RECV_V3 -> {
                    boolean v3 = code == MeshCoreProtocol.RESP_CHANNEL_MSG_RECV_V3;
                    int i = v3 ? 4 : 1;
                    if (frame.length < i + 7) {
                        return null;
                    }
                    Double snr = v3 ? frame[1] / 4.0 : null;
                    int index = frame[i] & 0xFF;
                    int pathLength = frame[i + 1] & 0xFF;
                    int textType = frame[i + 2] & 0xFF;
                    long ts = u32(frame, i + 3);
                    int textAt = i + 7;
                    return new TextMessage(true, index, null, pathLength, textType, Instant.ofEpochSecond(ts), snr,
                            new String(frame, textAt, frame.length - textAt, StandardCharsets.UTF_8));
                }
                default -> {
                    return null;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /** The companion's battery in millivolts from {@code RESP_CODE_BATT_AND_STORAGE}, or null. */
    public static Integer batteryMillivolts(byte[] frame) {
        if (code(frame) != MeshCoreProtocol.RESP_BATT_AND_STORAGE || frame.length < 3) {
            return null;
        }
        return (frame[1] & 0xFF) | (frame[2] & 0xFF) << 8;
    }

    /**
     * The companion's device info from {@code RESP_CODE_DEVICE_INFO}.
     *
     * @param firmwareVersion the protocol version byte
     * @param model the hardware model, or null (protocol below 3)
     * @param version the firmware version string, or null (protocol below 3)
     */
    public record DeviceInfo(int firmwareVersion, String model, String version) {}

    /** A device-info frame, or null. */
    public static DeviceInfo deviceInfo(byte[] frame) {
        if (code(frame) != MeshCoreProtocol.RESP_DEVICE_INFO || frame.length < 2) {
            return null;
        }
        int fw = frame[1] & 0xFF;
        if (fw >= 3 && frame.length >= 80) {
            String model = cString(frame, 20, 40);
            String version = cString(frame, 60, 20);
            return new DeviceInfo(fw, model.isBlank() ? null : model.strip(), version.isBlank() ? null : version.strip());
        }
        return new DeviceInfo(fw, null, null);
    }

    // ------------------------------------------------------------------ helpers

    /** A NUL-terminated UTF-8 string in a fixed field (the whole field when it has no NUL). */
    static String cString(byte[] frame, int offset, int size) {
        int end = Math.min(frame.length, offset + size);
        int stop = offset;
        while (stop < end && frame[stop] != 0) {
            stop++;
        }
        return new String(frame, offset, stop - offset, StandardCharsets.UTF_8).strip();
    }

    /** An unsigned 32-bit little-endian value. */
    static long u32(byte[] frame, int offset) {
        return (frame[offset] & 0xFFL) | (frame[offset + 1] & 0xFFL) << 8 | (frame[offset + 2] & 0xFFL) << 16
                | (frame[offset + 3] & 0xFFL) << 24;
    }

    /** A signed 32-bit little-endian value. */
    static int i32(byte[] frame, int offset) {
        return (int) u32(frame, offset);
    }
}
