package ai.foresignal.analyst;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One typed claim in an analyst note (prompt 102): {@code evidence} (directly stated by a cited item),
 * {@code inference} (reasoned from cited items) or {@code speculation} (a possibility to check).
 */
public record AnalystClaim(String text, String basis, List<String> citations, boolean downgraded, String warning) {

    public static final String EVIDENCE = "evidence";
    public static final String INFERENCE = "inference";
    public static final String SPECULATION = "speculation";

    /** Copies the citations. */
    public AnalystClaim {
        citations = citations == null ? List.of() : List.copyOf(citations);
    }

    /** From the stored jsonb form. */
    public static AnalystClaim fromMap(Map<String, Object> map) {
        return new AnalystClaim(String.valueOf(map.get("text")),
                map.get("basis") == null ? INFERENCE : String.valueOf(map.get("basis")),
                strings(map.get("citations")),
                Boolean.TRUE.equals(map.get("downgraded")),
                map.get("warning") == null ? null : String.valueOf(map.get("warning")));
    }

    /** The stored jsonb form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("text", text);
        out.put("basis", basis);
        out.put("citations", citations);
        if (downgraded) {
            out.put("downgraded", true);
        }
        if (warning != null) {
            out.put("warning", warning);
        }
        return out;
    }

    /** A claim with {@code basis} replaced. */
    public AnalystClaim withBasis(String newBasis, boolean wasDowngraded, String note) {
        return new AnalystClaim(text, newBasis, citations, wasDowngraded, note);
    }

    static List<String> strings(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    out.add(String.valueOf(item));
                }
            }
        }
        return out;
    }
}
