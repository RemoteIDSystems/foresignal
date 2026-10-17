package ai.foresignal.baserate.reference;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The reference-history families, sources, licenses and week arithmetic (prompt 121).
 *
 * <p>A family is a 119 signal family plus a subtype: {@code conflict:battles}, {@code conflict:explosions-remote},
 * {@code conflict:violence-civilians}, {@code conflict:protests}, {@code conflict:riots}; {@code seismic:m4.5},
 * {@code seismic:m5}, {@code seismic:m6} (cumulative: an M6.2 counts in all three); {@code disaster:gdacs-orange+},
 * {@code disaster:gdacs-red} and {@code disaster:gdacs-{hazard}-orange+} per GDACS hazard type. Foresignal's own
 * history adds the bare canonical-event families ({@code conflict}, {@code fire}, …) and the seismic bands.
 */
public final class ReferenceFamilies {

    /** The UCDP source name in {@code reference_history}. */
    public static final String UCDP = "ucdp";

    /** The ACLED source name. */
    public static final String ACLED = "acled";

    /** The USGS ComCat source name. */
    public static final String USGS = "usgs";

    /** The GDACS source name. */
    public static final String GDACS = "gdacs";

    /** Foresignal's own canonical-event history. */
    public static final String FORESIGNAL = "foresignal";

    /** The datasets of {@code ReferenceImportRequest}. */
    public static final List<String> DATASETS = List.of("ucdp-ged", "acled", "usgs-comcat", "gdacs");

    /** The license of each source. */
    public static final Map<String, String> LICENSES = Map.of(UCDP, "CC-BY-4.0", ACLED, "ACLED-EULA", USGS,
            "public-domain", GDACS, "GDACS-terms-attribution", FORESIGNAL, "foresignal-derived");

    /** The egress of each source: ACLED's terms forbid redistribution. */
    public static final Map<String, String> EGRESS = Map.of(UCDP, "OPEN", ACLED, "LOCAL_ONLY", USGS, "OPEN", GDACS,
            "OPEN", FORESIGNAL, "OPEN");

    /** The registered source id of each reference source, for {@code EgressPolicy} decisions. */
    public static final Map<String, String> SOURCE_IDS = Map.of(UCDP, "ucdp-ged", ACLED, "acled", USGS,
            "usgs-earthquakes", GDACS, "gdacs");

    /** The source of each dataset. */
    public static final Map<String, String> DATASET_SOURCE = Map.of("ucdp-ged", UCDP, "acled", ACLED, "usgs-comcat",
            USGS, "gdacs", GDACS);

    /** The conflict families. */
    public static final String BATTLES = "conflict:battles";

    /** Explosions and remote violence. */
    public static final String EXPLOSIONS = "conflict:explosions-remote";

    /** Violence against civilians (UCDP one-sided violence). */
    public static final String VIOLENCE_CIVILIANS = "conflict:violence-civilians";

    /** Protests. */
    public static final String PROTESTS = "conflict:protests";

    /** Riots. */
    public static final String RIOTS = "conflict:riots";

    /** The seismic magnitude thresholds and their families. */
    public static final double[] MAGNITUDES = {4.5, 5.0, 6.0};

    /** The seismic families, in the order of {@link #MAGNITUDES}. */
    public static final List<String> SEISMIC = List.of("seismic:m4.5", "seismic:m5", "seismic:m6");

    /** GDACS orange or red, any hazard. */
    public static final String GDACS_ORANGE_PLUS = "disaster:gdacs-orange+";

    /** GDACS red, any hazard. */
    public static final String GDACS_RED = "disaster:gdacs-red";

    /** The GDACS hazard types. */
    public static final List<String> GDACS_HAZARDS = List.of("EQ", "TC", "FL", "VO", "DR", "WF");

    /** A family name: lower case, digits, {@code :}, {@code +}, {@code .} and {@code -}. */
    public static final Pattern FAMILY = Pattern.compile("^[a-z][a-z0-9:+.-]{0,63}$");

    /** The source priority for conflict families: ACLED over UCDP, then Foresignal. */
    public static final List<String> CONFLICT_PRIORITY = List.of(ACLED, UCDP, FORESIGNAL);

    /** The source priority for seismic families: USGS, then Foresignal. */
    public static final List<String> SEISMIC_PRIORITY = List.of(USGS, FORESIGNAL);

    /** The source priority for disaster families: GDACS, then Foresignal. */
    public static final List<String> DISASTER_PRIORITY = List.of(GDACS, FORESIGNAL);

    private ReferenceFamilies() {}

    /** The 119 signal family of a reference family: the part before the first {@code :}. */
    public static String root(String family) {
        int colon = family == null ? -1 : family.indexOf(':');
        return colon < 0 ? family : family.substring(0, colon);
    }

    /** The UCDP family of a {@code type_of_violence}: 1 and 2 are battles, 3 one-sided violence; else null. */
    public static String ucdpFamily(Integer typeOfViolence) {
        if (typeOfViolence == null) {
            return null;
        }
        return switch (typeOfViolence) {
            case 1, 2 -> BATTLES;
            case 3 -> VIOLENCE_CIVILIANS;
            default -> null;
        };
    }

    /** The ACLED family of an {@code event_type}; strategic developments and unknown types are null. */
    public static String acledFamily(String eventType) {
        if (eventType == null) {
            return null;
        }
        return switch (eventType.strip().toLowerCase(Locale.ROOT)) {
            case "battles" -> BATTLES;
            case "explosions/remote violence" -> EXPLOSIONS;
            case "violence against civilians" -> VIOLENCE_CIVILIANS;
            case "protests" -> PROTESTS;
            case "riots" -> RIOTS;
            default -> null;
        };
    }

    /** The seismic families a magnitude counts in (cumulative), possibly empty. */
    public static List<String> seismicFamilies(double magnitude) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < MAGNITUDES.length; i++) {
            if (magnitude >= MAGNITUDES[i]) {
                out.add(SEISMIC.get(i));
            }
        }
        return out;
    }

    /** The GDACS families of an event: any-hazard orange+, red, and the hazard's orange+; empty below orange. */
    public static List<String> gdacsFamilies(String hazard, String alertLevel) {
        String level = alertLevel == null ? "" : alertLevel.strip().toLowerCase(Locale.ROOT);
        if (!level.equals("orange") && !level.equals("red")) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        out.add(GDACS_ORANGE_PLUS);
        if (level.equals("red")) {
            out.add(GDACS_RED);
        }
        if (hazard != null && !hazard.isBlank()) {
            out.add("disaster:gdacs-" + hazard.strip().toLowerCase(Locale.ROOT) + "-orange+");
        }
        return out;
    }

    /** The source priority used for a family (by its root), or null for "longest coverage first". */
    public static List<String> priority(String family) {
        return switch (root(family)) {
            case "conflict" -> CONFLICT_PRIORITY;
            case "seismic" -> SEISMIC_PRIORITY;
            case "disaster" -> DISASTER_PRIORITY;
            default -> null;
        };
    }

    /** The Monday of the ISO week containing {@code day}. */
    public static LocalDate weekStart(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** The Monday on or after {@code day}: the first full week starting at or after it. */
    public static LocalDate weekCeil(LocalDate day) {
        return day.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    /**
     * The week-aligned chunk of a calendar year: from the Monday of the week holding 1 January to the Monday of the
     * week holding the next 1 January, so every week falls in exactly one chunk.
     */
    public static LocalDate[] yearChunk(int year) {
        return new LocalDate[] {weekStart(LocalDate.of(year, 1, 1)), weekStart(LocalDate.of(year + 1, 1, 1))};
    }
}
