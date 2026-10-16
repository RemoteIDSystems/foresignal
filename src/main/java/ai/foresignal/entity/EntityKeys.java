package ai.foresignal.entity;

import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The deterministic keys of the entity registry, and their normalisation (prompt 098). Pure: no
 * I/O, no fuzzy matching.
 *
 * <p>Key types and their role and default confidence:
 *
 * <table>
 *   <caption>Keys</caption>
 *   <tr><th>Type</th><th>Role</th><th>Normalisation</th><th>Confidence</th></tr>
 *   <tr><td>{@code airframe}</td><td>ANCHOR</td><td>{@code MFR:SERIAL}, upper, trimmed</td><td>1.0</td></tr>
 *   <tr><td>{@code icao24}</td><td>WEAK</td><td>6 lowercase hex, rejects {@code 000000}/{@code ffffff}/TIS-B</td><td>0.9</td></tr>
 *   <tr><td>{@code registration}</td><td>WEAK</td><td>upper, spaces and hyphens removed</td><td>0.9</td></tr>
 *   <tr><td>{@code imo}</td><td>ANCHOR</td><td>7 digits with a check digit</td><td>1.0</td></tr>
 *   <tr><td>{@code mmsi}</td><td>WEAK</td><td>9 digits, ship class only</td><td>0.8</td></tr>
 *   <tr><td>{@code norad}</td><td>ANCHOR</td><td>positive catalogue number</td><td>1.0</td></tr>
 *   <tr><td>{@code cospar}</td><td>ANCHOR</td><td>{@code YYYY-NNNP{PP}}</td><td>1.0</td></tr>
 *   <tr><td>{@code callsign}</td><td>WEAK</td><td>upper, domain-prefixed {@code air:}/{@code sea:}/{@code ham:}</td><td>0.5/0.7/0.9</td></tr>
 * </table>
 */
public final class EntityKeys {

    /** The callsign domains. */
    public static final Set<String> CALLSIGN_DOMAINS = Set.of("air", "sea", "ham");

    private static final Set<String> MMSI_SENTINELS = Set.of("000000000", "111111111", "123456789");

    private EntityKeys() {}

    /** True when {@code keyType} is one of the eight registry key types. */
    public static boolean known(String keyType) {
        return switch (keyType) {
            case "airframe", "icao24", "registration", "imo", "mmsi", "norad", "cospar", "callsign" -> true;
            default -> false;
        };
    }

    /** The role of {@code keyType}; null when it is not a known key type. */
    public static EntityKeyRole roleOf(String keyType) {
        return switch (keyType) {
            case "airframe", "imo", "norad", "cospar" -> EntityKeyRole.ANCHOR;
            case "icao24", "registration", "mmsi", "callsign" -> EntityKeyRole.WEAK;
            default -> null;
        };
    }

    /** The entity type a key type identifies, or null when ambiguous. */
    public static EntityType typeOf(String keyType) {
        return switch (keyType) {
            case "airframe", "icao24", "registration" -> EntityType.AIRCRAFT;
            case "imo", "mmsi" -> EntityType.VESSEL;
            case "norad", "cospar" -> EntityType.SATELLITE;
            default -> null;
        };
    }

    /**
     * The normalized value of {@code raw} for {@code keyType}, or null when it is blank, malformed,
     * a sentinel or not resolvable to that key type. {@code prefix} is the callsign domain (one of
     * {@link #CALLSIGN_DOMAINS}).
     */
    public static String normalize(String keyType, String raw, String prefix) {
        if (raw == null) {
            return null;
        }
        String value = raw.strip();
        if (value.isEmpty()) {
            return null;
        }
        return switch (keyType) {
            case "airframe" -> airframe(value);
            case "icao24" -> icao24(value);
            case "registration" -> normalizeRegistration(value);
            case "imo" -> imo(value);
            case "mmsi" -> mmsi(value);
            case "norad" -> norad(value);
            case "cospar" -> cospar(value);
            case "callsign" -> callsign(value, prefix);
            default -> null;
        };
    }

    /** The default confidence of a normalized key. */
    public static float confidence(String keyType, String normalized) {
        return switch (keyType) {
            case "airframe", "imo", "norad", "cospar" -> 1.0f;
            case "icao24", "registration" -> 0.9f;
            case "mmsi" -> 0.8f;
            case "callsign" -> callsignConfidence(normalized);
            default -> 0.5f;
        };
    }

    /** {@code {mfrMdlCode}:{serial}}, both uppercased and trimmed. */
    static String airframe(String value) {
        return value.toUpperCase(Locale.ROOT);
    }

    /** Six lowercase hex digits; rejects {@code 000000}, {@code ffffff} and readsb TIS-B ({@code ~}). */
    static String icao24(String value) {
        if (value.startsWith("~")) {
            return null;
        }
        String hex = value.toLowerCase(Locale.ROOT);
        if (!hex.matches("[0-9a-f]{6}")) {
            return null;
        }
        if (hex.equals("000000") || hex.equals("ffffff")) {
            return null;
        }
        return hex;
    }

    /** Upper case with spaces and hyphens removed. */
    static String normalizeRegistration(String value) {
        String upper = value.toUpperCase(Locale.ROOT).replace(" ", "").replace("-", "");
        return upper.isEmpty() ? null : upper;
    }

    /** The hyphenated registration form kept as an alias, or null when it has none. */
    public static String registrationAlias(String raw) {
        if (raw == null) {
            return null;
        }
        String upper = raw.strip().toUpperCase(Locale.ROOT);
        return upper.contains("-") ? upper : null;
    }

    /** Seven digits with a valid IMO check digit. */
    static String imo(String value) {
        String digits = value.toUpperCase(Locale.ROOT);
        if (digits.startsWith("IMO")) {
            digits = digits.substring(3).strip();
        }
        if (!digits.matches("[0-9]{7}")) {
            return null;
        }
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            sum += (digits.charAt(i) - '0') * (7 - i);
        }
        return sum % 10 == digits.charAt(6) - '0' ? digits : null;
    }

    /** Nine digits of a ship station (first digit 2–7); rejects sentinels and non-ship classes. */
    static String mmsi(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (!digits.matches("[0-9]{9}")) {
            return null;
        }
        if (MMSI_SENTINELS.contains(digits) || !isShipMmsi(digits)) {
            return null;
        }
        return digits;
    }

    /**
     * True when {@code digits} is a ship station MMSI (first digit 2–7, prompt 098). SAR aircraft
     * ({@code 111…}), AtoN ({@code 99…}) and coast stations ({@code 00…}) are not.
     */
    public static boolean isShipMmsi(String digits) {
        if (digits == null || !digits.matches("[0-9]{9}")) {
            return false;
        }
        char first = digits.charAt(0);
        return first >= '2' && first <= '7';
    }

    /** A positive integer catalogue number. */
    static String norad(String value) {
        String digits = value.strip();
        if (!digits.matches("[0-9]{1,9}")) {
            return null;
        }
        return Long.parseLong(digits) > 0 ? Long.toString(Long.parseLong(digits)) : null;
    }

    /** {@code YYYY-NNNP{PP}}. */
    static String cospar(String value) {
        String upper = value.strip().toUpperCase(Locale.ROOT);
        return upper.matches("[0-9]{4}-[0-9]{3}[A-Z]{1,3}") ? upper : null;
    }

    /**
     * Upper case, with the domain prefix. A bare callsign takes the given prefix. A {@code ham:}
     * callsign is the base call: its SSID ({@code -9} in {@code K1ABC-9}) is stripped (prompt 098).
     */
    static String callsign(String value, String prefix) {
        String upper = value.strip().toUpperCase(Locale.ROOT);
        String domain = prefix == null || prefix.isBlank() ? "air" : prefix.toLowerCase(Locale.ROOT);
        String call = upper;
        if (upper.contains(":")) {
            String head = upper.substring(0, upper.indexOf(':')).toLowerCase(Locale.ROOT);
            if (CALLSIGN_DOMAINS.contains(head)) {
                domain = head;
                call = upper.substring(upper.indexOf(':') + 1);
            }
        }
        if ("ham".equals(domain)) {
            int ssid = call.indexOf('-');
            if (ssid >= 0) {
                call = call.substring(0, ssid);
            }
        }
        return call.isBlank() ? null : domain + ":" + call;
    }

    /** air 0.5, sea 0.7, ham 0.9 (prompt 098). */
    static float callsignConfidence(String normalized) {
        String domain = normalized == null ? "" : normalized.split(":", 2)[0].toLowerCase(Locale.ROOT);
        return switch (domain) {
            case "sea" -> 0.7f;
            case "ham" -> 0.9f;
            default -> 0.5f;
        };
    }

    /**
     * The keys a descriptor declares that {@code payload} carries (prompt 098). The descriptor's
     * {@code entityKeys} maps a key type to a top-level payload field; {@code airframe} combines
     * {@code mfrMdlCode} and {@code serial}, and a callsign's domain comes from
     * {@code entityKeyPrefixes}.
     */
    public static List<Key> extract(SourceDescriptor descriptor, Map<String, ?> payload) {
        return extract(descriptor, payload, type -> {});
    }

    /**
     * {@link #extract(SourceDescriptor, Map)}, reporting each key type whose value was present but
     * rejected by normalization (blank values and absent fields are not reported).
     *
     * @param descriptor the source descriptor declaring {@code entityKeys}
     * @param payload the record payload
     * @param onInvalid receives the key type of every rejected value
     * @return the valid keys, in declaration order
     */
    public static List<Key> extract(SourceDescriptor descriptor, Map<String, ?> payload, Consumer<String> onInvalid) {
        if (descriptor == null || descriptor.entityKeys().isEmpty() || payload == null || payload.isEmpty()) {
            return List.of();
        }
        List<Key> keys = new ArrayList<>();
        descriptor.entityKeys().forEach((type, field) -> {
            if (!known(type)) {
                return;
            }
            String prefix = descriptor.entityKeyPrefixes().get(type);
            for (String raw : rawValues(type, field, payload)) {
                String normalized = normalize(type, raw, prefix);
                if (normalized != null) {
                    keys.add(new Key(type, normalized, roleOf(type), confidence(type, normalized)));
                } else if (!raw.isBlank()) {
                    onInvalid.accept(type);
                }
            }
        });
        return List.copyOf(keys);
    }

    /** The attributes a descriptor copies from {@code payload} (prompt 098). */
    public static Map<String, Object> attributes(SourceDescriptor descriptor, Map<String, ?> payload) {
        if (descriptor == null || descriptor.entityAttributes().isEmpty() || payload == null) {
            return Map.of();
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        descriptor.entityAttributes().forEach((field, name) -> {
            Object value = payload.get(field);
            if (value != null) {
                attributes.put(name, value);
            }
        });
        return attributes;
    }

    private static List<String> rawValues(String type, String field, Map<String, ?> payload) {
        if ("airframe".equals(type)) {
            // Only with both parts: a bare serial collides across manufacturers, so no key is emitted.
            Object mfr = payload.get("mfrMdlCode");
            Object serial = payload.get("serial");
            if (mfr == null || serial == null || String.valueOf(mfr).isBlank() || String.valueOf(serial).isBlank()) {
                return List.of();
            }
            return List.of(String.valueOf(mfr).strip() + ":" + String.valueOf(serial).strip());
        }
        Object value = payload.get(field);
        return value == null ? List.of() : List.of(String.valueOf(value));
    }
}
