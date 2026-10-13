package ai.foresignal.source.adapters.gdelt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The twenty CAMEO root event codes and their labels (CAMEO Manual 1.1b3, chapter headings {@code 01:} to {@code 20:},
 * verified 2026-10 against {@code CAMEO.Manual.1.1b3.pdf}), bundled so a summary never depends on a lookup at run
 * time.
 */
public final class CameoRoots {

    /** Root code → label, in code order. */
    public static final Map<String, String> LABELS = labels();

    /** Every root code, {@code 01} to {@code 20}. */
    public static final List<String> CODES = List.copyOf(LABELS.keySet());

    private CameoRoots() {}

    /**
     * The label of a root code.
     *
     * @param rootCode a two-digit root code such as {@code 18}
     * @return the label, such as {@code Assault}, or null when the code is not a CAMEO root
     */
    public static String label(String rootCode) {
        return rootCode == null ? null : LABELS.get(rootCode);
    }

    private static Map<String, String> labels() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("01", "Make public statement");
        m.put("02", "Appeal");
        m.put("03", "Express intent to cooperate");
        m.put("04", "Consult");
        m.put("05", "Engage in diplomatic cooperation");
        m.put("06", "Engage in material cooperation");
        m.put("07", "Provide aid");
        m.put("08", "Yield");
        m.put("09", "Investigate");
        m.put("10", "Demand");
        m.put("11", "Disapprove");
        m.put("12", "Reject");
        m.put("13", "Threaten");
        m.put("14", "Protest");
        m.put("15", "Exhibit force posture");
        m.put("16", "Reduce relations");
        m.put("17", "Coerce");
        m.put("18", "Assault");
        m.put("19", "Fight");
        m.put("20", "Use unconventional mass violence");
        return java.util.Collections.unmodifiableMap(m);
    }
}
