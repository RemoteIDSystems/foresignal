package ai.foresignal.inbound.formats.same;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The SAME event-code names (prompt 086), bundled from the NWS NWR-SAME table and 47 CFR 11.31 (public domain) in
 * {@value #RESOURCE}.
 */
public final class SameEventCodes {

    /** The classpath resource. */
    public static final String RESOURCE = "same/event-codes.json";

    private static final class Holder {
        static final Map<String, String> CODES = load();
    }

    private SameEventCodes() {}

    /** The name of {@code code}, or empty when the table does not list it. */
    public static Optional<String> name(String code) {
        return Optional.ofNullable(code == null ? null : Holder.CODES.get(code));
    }

    /** Every code and name. */
    public static Map<String, String> all() {
        return Holder.CODES;
    }

    private static Map<String, String> load() {
        try (InputStream in = SameEventCodes.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The SAME event-code table " + RESOURCE + " is missing");
            }
            JsonNode codes = JsonMapper.builder().build().readTree(in).path("codes");
            Map<String, String> out = new TreeMap<>();
            for (Map.Entry<String, JsonNode> e : codes.properties()) {
                out.put(e.getKey(), e.getValue().asString());
            }
            return Map.copyOf(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
