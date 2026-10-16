package ai.foresignal.source.spi;

import java.util.List;
import java.util.Locale;

/**
 * The seeded Admiralty reliability grade of a descriptor that does not set {@code reliability} itself
 * (prompt 109, task 5: "official agencies {@code B}, established aggregators and the operator's own receivers
 * {@code C}, community or news-derived feeds (GDELT, Wikipedia surge) {@code D}, custom and peer sources
 * {@code F} unless configured"). The descriptor's own value always wins, and the operator's
 * {@code source_settings.reliability} override wins over both.
 *
 * <p>The derivation reads only what every descriptor already declares, so no adapter needs editing:
 *
 * <ol>
 *   <li>{@code F}: custom PUSH and EDP sources ({@code custom}), peer sources ({@code peer-*}), DERIVED sources
 *       (their records are graded through credibility instead), and a descriptor with no license at all
 *       (nothing to judge by).
 *   <li>{@code C}: the operator's own receivers (license "Operator's own data…" or "operator-owned…").
 *   <li>{@code D}: community or news-derived feeds: {@code gdelt*} and {@code wiki*} ids, and licenses naming a
 *       community service.
 *   <li>{@code B}: official agencies, recognized by a government license: US federal public domain
 *       ({@code US-PD}, "US Government work", NASA), the UK, Canadian and Norwegian open government licenses,
 *       the German data license, EUMETNET/MeteoAlarm and WHO terms.
 *   <li>{@code C}: every other built-in source (established aggregators and providers).
 * </ol>
 */
public final class ReliabilitySeed {

    /** License fragments that identify an official agency's publication. */
    static final List<String> OFFICIAL = List.of("us-pd", "us government", "u.s. government", "us public domain",
            "public domain", "nasa", "open government licence", "ogl", "nlod", "dl-de", "eumetnet", "(who)");

    /** License prefixes of the operator's own receiver data. */
    static final List<String> OPERATOR = List.of("operator's own data", "operator-owned");

    private ReliabilitySeed() {}

    /**
     * The seeded grade.
     *
     * @param id the source id
     * @param licence the free-text license, or null
     * @param mode the source mode
     * @param custom true for a custom PUSH or EDP source
     * @return a grade {@code A}–{@code F}
     */
    public static String seed(String id, String licence, Mode mode, boolean custom) {
        String sourceId = id == null ? "" : id.toLowerCase(Locale.ROOT);
        if (custom || sourceId.startsWith("peer-") || mode == Mode.DERIVED || licence == null || licence.isBlank()) {
            return "F";
        }
        String text = licence.strip().toLowerCase(Locale.ROOT);
        for (String prefix : OPERATOR) {
            if (text.startsWith(prefix)) {
                return "C";
            }
        }
        if (sourceId.startsWith("gdelt") || sourceId.startsWith("wiki") || text.contains("community")) {
            return "D";
        }
        for (String fragment : OFFICIAL) {
            if (text.contains(fragment)) {
                return "B";
            }
        }
        return "C";
    }
}
