package ai.foresignal.manifest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.databind.json.JsonMapper;

/**
 * Canonical JSON for hashing (prompt 116): object members sorted by key at every depth, so equal content
 * always hashes equally regardless of insertion order.
 */
public final class CanonicalJson {

    private CanonicalJson() {}

    /** {@code value} with every map replaced by a key-sorted copy, recursively. */
    public static Object canonical(Object value) {
        if (value instanceof Map<?, ?> map) {
            TreeMap<String, Object> out = new TreeMap<>();
            map.forEach((k, v) -> out.put(String.valueOf(k), canonical(v)));
            return out;
        }
        if (value instanceof Collection<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            for (Object item : list) {
                out.add(canonical(item));
            }
            return out;
        }
        return value;
    }

    /** The canonical JSON text of {@code value}. */
    public static String write(Object value, JsonMapper json) {
        return json.writeValueAsString(canonical(json.convertValue(value, Object.class)));
    }

    /** The SHA-256 of {@code text} as lower-case hex. */
    public static String sha256(String text) {
        return sha256(text.getBytes(StandardCharsets.UTF_8));
    }

    /** The SHA-256 of {@code bytes} as lower-case hex. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** The SHA-256 of the canonical JSON of {@code value}. */
    public static String hash(Object value, JsonMapper json) {
        return sha256(write(value, json));
    }
}
