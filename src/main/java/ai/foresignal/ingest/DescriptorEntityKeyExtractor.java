package ai.foresignal.ingest;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The default {@link EntityKeyExtractor}: reads the descriptor's {@code entityKeys} mapping
 * (key type to top-level payload field).
 *
 * <p>Values are stringified and trimmed. Known key types are normalized so the same entity matches
 * across sources: {@code icao24} is lowercase hexadecimal, {@code mmsi} and {@code imo} are digits
 * (an {@code IMO} prefix is dropped), and {@code registration} and {@code callsign} are
 * uppercase. A value that does not fit its type's form after normalization is left out rather than
 * guessed at. Other key types are kept trimmed.
 */
public final class DescriptorEntityKeyExtractor implements EntityKeyExtractor {

    /** A shared instance; the class is stateless. */
    public static final DescriptorEntityKeyExtractor INSTANCE = new DescriptorEntityKeyExtractor();

    @Override
    public Map<String, String> extract(SourceIngestProfile profile, Map<String, ?> payload) {
        if (profile == null || profile.entityKeys().isEmpty() || payload == null || payload.isEmpty()) {
            return Map.of();
        }
        Map<String, String> keys = new TreeMap<>();
        profile.entityKeys()
                .forEach(
                        (type, field) -> {
                            String value = normalize(type, JsonValues.text(payload.get(field)));
                            if (value != null) {
                                keys.put(type, value);
                            }
                        });
        return keys.isEmpty() ? Map.of() : Collections.unmodifiableMap(keys);
    }

    /**
     * {@code raw} normalized for {@code type}, or null when it is blank or malformed.
     *
     * <p>Package-visible for tests.
     */
    static String normalize(String type, String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.strip();
        if (value.isEmpty()) {
            return null;
        }
        return switch (type) {
            case "icao24" -> {
                String hex = value.toLowerCase(Locale.ROOT);
                if (hex.startsWith("~")) {
                    // readsb marks non-ICAO (TIS-B) addresses with a tilde; they are not ICAO keys.
                    yield null;
                }
                yield hex.matches("[0-9a-f]{6}") ? hex : null;
            }
            case "mmsi" -> value.matches("[0-9]{9}") ? value : null;
            case "imo" -> {
                String digits = value.toUpperCase(Locale.ROOT);
                if (digits.startsWith("IMO")) {
                    digits = digits.substring(3).strip();
                }
                yield digits.matches("[0-9]{7}") ? digits : null;
            }
            case "registration", "callsign" -> value.toUpperCase(Locale.ROOT);
            default -> value;
        };
    }
}
