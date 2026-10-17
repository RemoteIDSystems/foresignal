package ai.foresignal.notify.mesh.cbor;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * The CBOR profile of mesh messages (prompt 084): a definite-length CBOR map with small unsigned integer keys, written
 * and read with Jackson's CBOR dataformat (Boot-managed Jackson 3).
 *
 * <table>
 *   <caption>Keys</caption>
 *   <tr><th>Key</th><th>Value</th></tr>
 *   <tr><td>0</td><td>version, {@value #VERSION}</td></tr>
 *   <tr><td>1</td><td>type: 1 alert, 2 resolved, 3 report acknowledgement, 4 status</td></tr>
 *   <tr><td>2</td><td>short code (text)</td></tr>
 *   <tr><td>3</td><td>severity 0–2</td></tr>
 *   <tr><td>4</td><td>kind, at most 16 characters</td></tr>
 *   <tr><td>5, 6</td><td>latitude and longitude × 1e5 (integers)</td></tr>
 *   <tr><td>7</td><td>epoch seconds</td></tr>
 *   <tr><td>8</td><td>title, at most 60 characters</td></tr>
 *   <tr><td>9</td><td>source id, at most 24 characters</td></tr>
 *   <tr><td>10</td><td>origin instance short id, 4 bytes (byte string; prompt 113)</td></tr>
 *   <tr><td>11</td><td>message id: the first 8 bytes of the SHA-256 of the origin ref (byte string; prompt 113)</td></tr>
 *   <tr><td>12</td><td>expiry in minutes after key 7 (prompt 113)</td></tr>
 * </table>
 *
 * <p>Absent members are left out. Keys are written in ascending order, so an encoding is deterministic (the golden
 * vectors depend on it). A message longer than the size cap ({@value #MAX_BYTES} bytes by default) loses key 8, then
 * key 9, until it fits; if it still does not fit it is refused. A message with any of the prompt 113 extension keys
 * (10–12, which dedup and loop prevention need) loses key 9 first, then key 8, and never 10–12. The decoder ignores
 * integer keys it does not know (forward compatibility) and refuses anything that is not a map with integer keys.
 *
 * <p>Type {@value #TYPE_INTEL_EVENT} (prompt 113) is an intelligence event.
 */
public final class MeshCborProfile {

    /** The profile version. */
    public static final int VERSION = 1;

    /** An alert. */
    public static final int TYPE_ALERT = 1;

    /** A resolved alert. */
    public static final int TYPE_RESOLVED = 2;

    /** A field-report acknowledgement. */
    public static final int TYPE_REPORT_ACK = 3;

    /** A status summary. */
    public static final int TYPE_STATUS = 4;

    /** An intelligence event (prompt 113). */
    public static final int TYPE_INTEL_EVENT = 5;

    /** The length of the origin short id (key 10). */
    public static final int ORIGIN_SHORT_ID_BYTES = 4;

    /** The length of the message id (key 11). */
    public static final int MESSAGE_ID_BYTES = 8;

    /** The default size cap: one MeshCore channel datagram ({@code MAX_CHANNEL_DATA_LENGTH} 163) with room. */
    public static final int MAX_BYTES = 160;

    /** The longest kind. */
    public static final int MAX_KIND = 16;

    /** The longest title. */
    public static final int MAX_TITLE = 60;

    /** The longest source id. */
    public static final int MAX_SOURCE_ID = 24;

    private static final CBORMapper CBOR = CBORMapper.builder().build();

    private MeshCborProfile() {}

    /** {@code message} encoded within {@value #MAX_BYTES} bytes. */
    public static byte[] encode(MeshCborMessage message) {
        return encode(message, MAX_BYTES);
    }

    /**
     * {@code message} encoded within {@code maxBytes} (at most {@value #MAX_BYTES}), dropping the title and then the
     * source id when needed.
     *
     * @throws IllegalArgumentException when even the reduced message is longer
     */
    public static byte[] encode(MeshCborMessage message, int maxBytes) {
        int cap = Math.min(maxBytes, MAX_BYTES);
        byte[] out = write(message);
        if (message.extended()) {
            // Prompt 113: keys 10-12 carry dedup and loop prevention, so the source id goes first, then the title.
            if (out.length > cap && message.sourceId() != null) {
                message = message.withoutSourceId();
                out = write(message);
            }
            if (out.length > cap && message.title() != null) {
                message = message.withoutTitle();
                out = write(message);
            }
        } else {
            if (out.length > cap && message.title() != null) {
                message = message.withoutTitle();
                out = write(message);
            }
            if (out.length > cap && message.sourceId() != null) {
                message = message.withoutSourceId();
                out = write(message);
            }
        }
        if (out.length > cap) {
            throw new IllegalArgumentException("the CBOR message is " + out.length + " bytes, above " + cap);
        }
        return out;
    }

    /** {@code message} without any size reduction (members are still cut to their maximum lengths). */
    static byte[] write(MeshCborMessage m) {
        List<Object[]> members = new ArrayList<>();
        members.add(new Object[] {0, (long) m.version()});
        members.add(new Object[] {1, (long) m.type()});
        if (m.shortCode() != null) {
            members.add(new Object[] {2, m.shortCode()});
        }
        if (m.severity() != null) {
            members.add(new Object[] {3, (long) m.severity()});
        }
        if (m.kind() != null) {
            members.add(new Object[] {4, cut(m.kind(), MAX_KIND)});
        }
        if (m.latitudeE5() != null) {
            members.add(new Object[] {5, (long) m.latitudeE5()});
        }
        if (m.longitudeE5() != null) {
            members.add(new Object[] {6, (long) m.longitudeE5()});
        }
        if (m.epochSeconds() != null) {
            members.add(new Object[] {7, m.epochSeconds()});
        }
        if (m.title() != null) {
            members.add(new Object[] {8, cut(m.title(), MAX_TITLE)});
        }
        if (m.sourceId() != null) {
            members.add(new Object[] {9, cut(m.sourceId(), MAX_SOURCE_ID)});
        }
        if (m.originShortId() != null) {
            members.add(new Object[] {10, m.originShortId()});
        }
        if (m.messageId() != null) {
            members.add(new Object[] {11, m.messageId()});
        }
        if (m.expiryMinutes() != null) {
            members.add(new Object[] {12, (long) m.expiryMinutes()});
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (JsonGenerator g = CBOR.createGenerator(bytes)) {
            g.writeStartObject(null, members.size());
            for (Object[] member : members) {
                g.writePropertyId((Integer) member[0]);
                if (member[1] instanceof Long number) {
                    g.writeNumber(number);
                } else if (member[1] instanceof byte[] binary) {
                    g.writeBinary(binary);
                } else {
                    g.writeString((String) member[1]);
                }
            }
            g.writeEndObject();
        }
        return bytes.toByteArray();
    }

    /**
     * Decodes {@code bytes}; unknown integer keys are skipped.
     *
     * @throws IllegalArgumentException when it is not a CBOR map with integer keys, or misses the version or type
     */
    public static MeshCborMessage decode(byte[] bytes) {
        Integer version = null;
        Integer type = null;
        String code = null;
        Integer severity = null;
        String kind = null;
        Integer lat = null;
        Integer lon = null;
        Long epoch = null;
        String title = null;
        String sourceId = null;
        byte[] origin = null;
        byte[] messageId = null;
        Integer expiry = null;
        try (JsonParser p = CBOR.createParser(bytes)) {
            if (p.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("a mesh CBOR message is a map");
            }
            while (p.nextToken() == JsonToken.PROPERTY_NAME) {
                int key;
                try {
                    key = Integer.parseInt(p.currentName());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("a mesh CBOR message has integer keys, not '"
                            + p.currentName() + "'");
                }
                JsonToken value = p.nextToken();
                switch (key) {
                    case 0 -> version = p.getIntValue();
                    case 1 -> type = p.getIntValue();
                    case 2 -> code = p.getString();
                    case 3 -> severity = p.getIntValue();
                    case 4 -> kind = p.getString();
                    case 5 -> lat = p.getIntValue();
                    case 6 -> lon = p.getIntValue();
                    case 7 -> epoch = p.getLongValue();
                    case 8 -> title = p.getString();
                    case 9 -> sourceId = p.getString();
                    case 10 -> origin = binary(p, value);
                    case 11 -> messageId = binary(p, value);
                    case 12 -> {
                        if (value == JsonToken.VALUE_NUMBER_INT) {
                            expiry = p.getIntValue();
                        } else {
                            skip(p, value);
                        }
                    }
                    default -> {
                        if (value == JsonToken.START_OBJECT || value == JsonToken.START_ARRAY) {
                            p.skipChildren();
                        }
                    }
                }
            }
        } catch (tools.jackson.core.JacksonException e) {
            throw new IllegalArgumentException("not a mesh CBOR message: " + e.getOriginalMessage(), e);
        }
        if (version == null || type == null) {
            throw new IllegalArgumentException("a mesh CBOR message needs keys 0 (version) and 1 (type)");
        }
        return new MeshCborMessage(version, type, code, severity, kind, lat, lon, epoch, title, sourceId, origin,
                messageId, expiry);
    }

    /** A byte-string value (keys 10 and 11), or null for any other value, which is skipped. */
    private static byte[] binary(JsonParser p, JsonToken value) {
        if (value == JsonToken.VALUE_EMBEDDED_OBJECT) {
            return p.getBinaryValue();
        }
        skip(p, value);
        return null;
    }

    /** Skips a structured value the decoder does not read. */
    private static void skip(JsonParser p, JsonToken value) {
        if (value == JsonToken.START_OBJECT || value == JsonToken.START_ARRAY) {
            p.skipChildren();
        }
    }

    private static String cut(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        int end = max;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }
}
