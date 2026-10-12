package ai.foresignal.api.support;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Encodes {@link Cursor}s as opaque, versioned strings: {@code c1.<base64url JSON>.<check>}.
 *
 * <p>The JSON carries a variant tag {@code t} ({@code o} observed, {@code i} id, {@code d}
 * distance, {@code q} seq, {@code h} history), the keyset values, the optional pinned seq
 * {@code w}, and {@code f}, a fingerprint of the query the cursor was issued for. The check is a
 * truncated SHA-256 of the JSON. Decoding fails with {@code 400 INVALID_CURSOR} when the prefix,
 * encoding, check or JSON is wrong (a tampered or truncated cursor), or when the variant or
 * fingerprint does not match the request (a cursor reused with a different query or order).
 *
 * <p>The check detects corruption and casual edits; it is not a signature. A forged cursor can only
 * move a keyset position, since every filter is re-applied on each page.
 */
public final class CursorCodec {

    /** The version prefix. */
    public static final String PREFIX = "c1.";

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final HexFormat HEX = HexFormat.of();

    private CursorCodec() {}

    /**
     * A short fingerprint of a canonical query description.
     *
     * @param canonicalQuery the filters and ordering that define the listing
     * @return twelve hex characters
     */
    public static String fingerprint(String canonicalQuery) {
        return HEX.formatHex(sha256(canonicalQuery)).substring(0, 12);
    }

    /**
     * Encodes {@code cursor} for the query with {@code fingerprint}.
     *
     * @return the opaque cursor
     */
    public static String encode(Cursor cursor, String fingerprint) {
        Map<String, Object> body = new LinkedHashMap<>();
        switch (cursor) {
            case Cursor.Observed c -> {
                body.put("t", "o");
                body.put("o", c.o().toString());
                body.put("s", c.s());
                body.put("e", c.e());
            }
            case Cursor.ById c -> {
                body.put("t", "i");
                body.put("s", c.s());
                body.put("e", c.e());
            }
            case Cursor.Distance c -> {
                body.put("t", "d");
                body.put("d", c.d());
                body.put("s", c.s());
                body.put("e", c.e());
            }
            case Cursor.Seq c -> {
                body.put("t", "q");
                body.put("q", c.q());
            }
            case Cursor.History c -> {
                body.put("t", "h");
                body.put("o", c.o() == null ? null : c.o().toString());
                body.put("q", c.q());
            }
        }
        if (cursor.w() != null) {
            body.put("w", cursor.w());
        }
        body.put("f", fingerprint == null ? "" : fingerprint);
        String json = JSON.writeValueAsString(body);
        return PREFIX + ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8)) + "." + check(json);
    }

    /**
     * Decodes {@code token}, which must be of type {@code expected} and issued for
     * {@code fingerprint}.
     *
     * @return the cursor, or null when {@code token} is null or blank
     * @throws ValidationException with code {@code INVALID_CURSOR} on any mismatch
     */
    public static <T extends Cursor> T decode(String token, Class<T> expected, String fingerprint) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Cursor cursor = decodeAny(token.strip(), fingerprint);
        if (!expected.isInstance(cursor)) {
            throw invalid("The cursor belongs to a different ordering.");
        }
        return expected.cast(cursor);
    }

    private static Cursor decodeAny(String token, String fingerprint) {
        if (!token.startsWith(PREFIX)) {
            throw invalid("The cursor is not recognized.");
        }
        String rest = token.substring(PREFIX.length());
        int dot = rest.lastIndexOf('.');
        if (dot <= 0) {
            throw invalid("The cursor is malformed.");
        }
        String json;
        try {
            json = new String(DECODER.decode(rest.substring(0, dot)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw invalid("The cursor is malformed.");
        }
        if (!MessageDigest.isEqual(
                check(json).getBytes(StandardCharsets.US_ASCII),
                rest.substring(dot + 1).getBytes(StandardCharsets.US_ASCII))) {
            throw invalid("The cursor has been modified.");
        }
        JsonNode node;
        try {
            node = JSON.readTree(json);
        } catch (JacksonException e) {
            throw invalid("The cursor is malformed.");
        }
        if (node == null || !node.isObject()) {
            throw invalid("The cursor is malformed.");
        }
        String f = text(node, "f");
        if (!(fingerprint == null ? "" : fingerprint).equals(f == null ? "" : f)) {
            throw invalid("The cursor was issued for a different query.");
        }
        Long w = node.hasNonNull("w") ? requireLong(node, "w") : null;
        String type = text(node, "t");
        try {
            return switch (type == null ? "" : type) {
                case "o" -> new Cursor.Observed(Instant.parse(requireText(node, "o")),
                        requireText(node, "s"), requireText(node, "e"), w);
                case "i" -> new Cursor.ById(requireText(node, "s"), requireText(node, "e"), w);
                case "d" -> new Cursor.Distance(requireDouble(node, "d"),
                        requireText(node, "s"), requireText(node, "e"), w);
                case "q" -> new Cursor.Seq(requireLong(node, "q"), w);
                case "h" -> new Cursor.History(
                        node.hasNonNull("o") ? Instant.parse(requireText(node, "o")) : null,
                        requireLong(node, "q"), w);
                default -> throw invalid("The cursor is not recognized.");
            };
        } catch (java.time.format.DateTimeParseException e) {
            throw invalid("The cursor is malformed.");
        }
    }

    private static String text(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() || !value.isString() ? null : value.asString();
    }

    private static String requireText(JsonNode node, String name) {
        String value = text(node, name);
        if (value == null) {
            throw invalid("The cursor is malformed.");
        }
        return value;
    }

    private static long requireLong(JsonNode node, String name) {
        JsonNode value = node.get(name);
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong()) {
            throw invalid("The cursor is malformed.");
        }
        return value.longValue();
    }

    private static double requireDouble(JsonNode node, String name) {
        JsonNode value = node.get(name);
        if (value == null || !value.isNumber()) {
            throw invalid("The cursor is malformed.");
        }
        return value.doubleValue();
    }

    private static String check(String json) {
        return HEX.formatHex(sha256("foresignal-cursor:" + json)).substring(0, 16);
    }

    private static byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static ValidationException invalid(String detail) {
        return new ValidationException(ErrorCodes.INVALID_CURSOR, detail, List.of());
    }
}
