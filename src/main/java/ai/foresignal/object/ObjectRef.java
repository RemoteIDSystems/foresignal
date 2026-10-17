package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * A canonical object reference {@code {type}:{id}} (docs/Client-Contract.md §2).
 *
 * <p>Ids are opaque to clients, and references compare as strings. Two types have a structured id:
 * <ul>
 *   <li>{@code record:{sourceId}/{externalId}[@{seq}]}, where the external id is percent-encoded ({@code /} →
 *       {@code %2F}, {@code @} → {@code %40}, {@code %} → {@code %25}) so the first {@code /} and the only raw
 *       {@code @} are unambiguous; {@code @{seq}} pins a version;</li>
 *   <li>{@code sensor:{kind}:{id}} with kind {@code source}, {@code receiver}, {@code station}, {@code satellite},
 *       {@code scene} or {@code raster-product}.</li>
 * </ul>
 * Unknown types and malformed ids are rejected with {@code 400 OBJECT_REF_INVALID}.
 *
 * @param type the object type, one of {@link ObjectTypes#ALL}
 * @param id the type-specific id, exactly as it appears after the first colon
 */
public record ObjectRef(String type, String id) {

    /** Longest accepted reference, in characters. */
    public static final int MAX_LENGTH = 1024;

    /** Sensor kinds accepted in {@code sensor:{kind}:{id}}. */
    public static final List<String> SENSOR_KINDS = List.of("source", "receiver", "station", "satellite", "scene",
            "raster-product");

    /** Validates the parts. */
    public ObjectRef {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }

    /**
     * Parses a reference.
     *
     * @param text the reference
     * @return the parsed reference
     * @throws ValidationException {@code OBJECT_REF_INVALID} when it is malformed or names an unknown type
     */
    public static ObjectRef parse(String text) {
        return parse(text, "ref");
    }

    /**
     * Parses a reference, reporting problems against {@code field}.
     *
     * @param text the reference
     * @param field the request field the reference came from
     * @return the parsed reference
     */
    public static ObjectRef parse(String text, String field) {
        if (text == null || text.isBlank()) {
            throw invalid(field, "is required");
        }
        if (text.length() > MAX_LENGTH) {
            throw invalid(field, "is longer than " + MAX_LENGTH + " characters");
        }
        int colon = text.indexOf(':');
        if (colon <= 0 || colon == text.length() - 1) {
            throw invalid(field, "must be {type}:{id}");
        }
        String type = text.substring(0, colon);
        String id = text.substring(colon + 1);
        if (!ObjectTypes.isType(type)) {
            throw invalid(field, "has unknown object type '" + type + "'");
        }
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (Character.isWhitespace(c) || Character.isISOControl(c)) {
                throw invalid(field, "must not contain whitespace or control characters");
            }
        }
        ObjectRef ref = new ObjectRef(type, id);
        if (ObjectTypes.RECORD.equals(type)) {
            ref.checkRecord(field);
        } else if (ObjectTypes.SENSOR.equals(type)) {
            int c = id.indexOf(':');
            if (c <= 0 || c == id.length() - 1 || !SENSOR_KINDS.contains(id.substring(0, c))) {
                throw invalid(field, "sensor refs are sensor:{kind}:{id} with kind one of " + SENSOR_KINDS);
            }
        }
        return ref;
    }

    /** True when {@code text} parses. */
    public static boolean isValid(String text) {
        try {
            parse(text);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /** A reference of {@code type} with {@code id}, validated as {@link #parse} would. */
    public static ObjectRef of(String type, String id) {
        return parse(type + ":" + id);
    }

    /**
     * A record reference.
     *
     * @param sourceId the source id
     * @param externalId the raw external id (percent-encoded here)
     * @param seq the pinned version, or null for the current one
     */
    public static ObjectRef record(String sourceId, String externalId, Long seq) {
        String id = sourceId + "/" + encode(externalId) + (seq == null ? "" : "@" + seq);
        return parse(ObjectTypes.RECORD + ":" + id);
    }

    /** The reference as text: {@code {type}:{id}}. */
    public String format() {
        return type + ":" + id;
    }

    @Override
    public String toString() {
        return format();
    }

    /** True for a record reference. */
    public boolean isRecord() {
        return ObjectTypes.RECORD.equals(type);
    }

    /** The source id of a record reference. */
    public String recordSourceId() {
        requireRecord();
        return id.substring(0, id.indexOf('/'));
    }

    /** The decoded external id of a record reference. */
    public String recordExternalId() {
        requireRecord();
        String rest = id.substring(id.indexOf('/') + 1);
        int at = rest.indexOf('@');
        return decode(at < 0 ? rest : rest.substring(0, at));
    }

    /** The pinned seq of a record reference, or null. */
    public Long recordSeq() {
        requireRecord();
        int at = id.indexOf('@');
        return at < 0 ? null : Long.valueOf(id.substring(at + 1));
    }

    /** This reference without a pinned version (records); other references are returned unchanged. */
    public ObjectRef withoutSeq() {
        if (!isRecord() || id.indexOf('@') < 0) {
            return this;
        }
        return new ObjectRef(type, id.substring(0, id.indexOf('@')));
    }

    /** The kind of a sensor reference ({@code source}, {@code satellite}, …). */
    public String sensorKind() {
        return id.substring(0, id.indexOf(':'));
    }

    /** The kind-specific id of a sensor reference. */
    public String sensorId() {
        return id.substring(id.indexOf(':') + 1);
    }

    private void requireRecord() {
        if (!isRecord()) {
            throw new IllegalStateException("not a record reference: " + this);
        }
    }

    private void checkRecord(String field) {
        int slash = id.indexOf('/');
        if (slash <= 0 || slash == id.length() - 1) {
            throw invalid(field, "record refs are record:{sourceId}/{externalId}[@{seq}]");
        }
        String rest = id.substring(slash + 1);
        int at = rest.indexOf('@');
        if (at == 0 || (at > 0 && rest.indexOf('@', at + 1) >= 0)) {
            throw invalid(field, "a record ref has at most one '@' (encode '@' in the external id as %40)");
        }
        if (at > 0) {
            String seq = rest.substring(at + 1);
            if (seq.isEmpty() || seq.length() > 19 || !seq.chars().allMatch(Character::isDigit)) {
                throw invalid(field, "the version after '@' must be a non-negative integer seq");
            }
        }
        try {
            decode(at < 0 ? rest : rest.substring(0, at));
        } catch (IllegalArgumentException e) {
            throw invalid(field, "has a malformed percent-encoding in the external id");
        }
    }

    /** Percent-encodes {@code %}, {@code /} and {@code @} (and whitespace and control characters) in an external id. */
    public static String encode(String externalId) {
        StringBuilder out = new StringBuilder(externalId.length() + 8);
        externalId.codePoints().forEach(cp -> {
            if (cp == '%' || cp == '/' || cp == '@' || Character.isWhitespace(cp) || Character.isISOControl(cp)) {
                for (byte b : new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8)) {
                    out.append('%').append(String.format("%02X", b & 0xff));
                }
            } else {
                out.appendCodePoint(cp);
            }
        });
        return out.toString();
    }

    /** Decodes a percent-encoded external id. */
    public static String decode(String encoded) {
        if (encoded.indexOf('%') < 0) {
            return encoded;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            if (c == '%') {
                if (i + 2 >= encoded.length()) {
                    throw new IllegalArgumentException("truncated escape");
                }
                int hi = Character.digit(encoded.charAt(i + 1), 16);
                int lo = Character.digit(encoded.charAt(i + 2), 16);
                if (hi < 0 || lo < 0) {
                    throw new IllegalArgumentException("bad escape");
                }
                out.write((hi << 4) | lo);
                i += 2;
            } else {
                byte[] b = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
                if (Character.isHighSurrogate(c) && i + 1 < encoded.length()) {
                    b = encoded.substring(i, i + 2).getBytes(StandardCharsets.UTF_8);
                    i++;
                }
                out.write(b, 0, b.length);
            }
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    /** The {@code OBJECT_REF_INVALID} problem for {@code field}. */
    public static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.OBJECT_REF_INVALID, "The object reference is not valid.",
                List.of(new FieldError(field, message)));
    }
}
