package ai.foresignal.source.adapters.cdc;

import java.util.Locale;
import java.util.Map;

/**
 * US state and territory codes to two-digit FIPS, for placing a CDC NWSS record at its state (prompt 095).
 *
 * <p>CDC NWSS publishes a {@code state} as a two-letter USPS code (or, when present, a full name); 040's
 * {@code ref_admin2} is keyed by FIPS. This is the offline crosswalk, with no network call. Unknown values
 * resolve to null, which callers treat as "no geometry".
 */
final class UsStates {

    private static final Map<String, String> BY_USPS = Map.ofEntries(
            Map.entry("AL", "01"), Map.entry("AK", "02"), Map.entry("AZ", "04"), Map.entry("AR", "05"),
            Map.entry("CA", "06"), Map.entry("CO", "08"), Map.entry("CT", "09"), Map.entry("DE", "10"),
            Map.entry("DC", "11"), Map.entry("FL", "12"), Map.entry("GA", "13"), Map.entry("HI", "15"),
            Map.entry("ID", "16"), Map.entry("IL", "17"), Map.entry("IN", "18"), Map.entry("IA", "19"),
            Map.entry("KS", "20"), Map.entry("KY", "21"), Map.entry("LA", "22"), Map.entry("ME", "23"),
            Map.entry("MD", "24"), Map.entry("MA", "25"), Map.entry("MI", "26"), Map.entry("MN", "27"),
            Map.entry("MS", "28"), Map.entry("MO", "29"), Map.entry("MT", "30"), Map.entry("NE", "31"),
            Map.entry("NV", "32"), Map.entry("NH", "33"), Map.entry("NJ", "34"), Map.entry("NM", "35"),
            Map.entry("NY", "36"), Map.entry("NC", "37"), Map.entry("ND", "38"), Map.entry("OH", "39"),
            Map.entry("OK", "40"), Map.entry("OR", "41"), Map.entry("PA", "42"), Map.entry("RI", "44"),
            Map.entry("SC", "45"), Map.entry("SD", "46"), Map.entry("TN", "47"), Map.entry("TX", "48"),
            Map.entry("UT", "49"), Map.entry("VT", "50"), Map.entry("VA", "51"), Map.entry("WA", "53"),
            Map.entry("WV", "54"), Map.entry("WI", "55"), Map.entry("WY", "56"), Map.entry("PR", "72"),
            Map.entry("VI", "78"), Map.entry("GU", "66"), Map.entry("AS", "60"), Map.entry("MP", "69"));

    private static final Map<String, String> BY_NAME = Map.ofEntries(
            Map.entry("ALABAMA", "01"), Map.entry("ALASKA", "02"), Map.entry("ARIZONA", "04"),
            Map.entry("ARKANSAS", "05"), Map.entry("CALIFORNIA", "06"), Map.entry("COLORADO", "08"),
            Map.entry("CONNECTICUT", "09"), Map.entry("DELAWARE", "10"),
            Map.entry("DISTRICT OF COLUMBIA", "11"), Map.entry("FLORIDA", "12"), Map.entry("GEORGIA", "13"),
            Map.entry("HAWAII", "15"), Map.entry("IDAHO", "16"), Map.entry("ILLINOIS", "17"),
            Map.entry("INDIANA", "18"), Map.entry("IOWA", "19"), Map.entry("KANSAS", "20"),
            Map.entry("KENTUCKY", "21"), Map.entry("LOUISIANA", "22"), Map.entry("MAINE", "23"),
            Map.entry("MARYLAND", "24"), Map.entry("MASSACHUSETTS", "25"), Map.entry("MICHIGAN", "26"),
            Map.entry("MINNESOTA", "27"), Map.entry("MISSISSIPPI", "28"), Map.entry("MISSOURI", "29"),
            Map.entry("MONTANA", "30"), Map.entry("NEBRASKA", "31"), Map.entry("NEVADA", "32"),
            Map.entry("NEW HAMPSHIRE", "33"), Map.entry("NEW JERSEY", "34"), Map.entry("NEW MEXICO", "35"),
            Map.entry("NEW YORK", "36"), Map.entry("NORTH CAROLINA", "37"), Map.entry("NORTH DAKOTA", "38"),
            Map.entry("OHIO", "39"), Map.entry("OKLAHOMA", "40"), Map.entry("OREGON", "41"),
            Map.entry("PENNSYLVANIA", "42"), Map.entry("RHODE ISLAND", "44"), Map.entry("SOUTH CAROLINA", "45"),
            Map.entry("SOUTH DAKOTA", "46"), Map.entry("TENNESSEE", "47"), Map.entry("TEXAS", "48"),
            Map.entry("UTAH", "49"), Map.entry("VERMONT", "50"), Map.entry("VIRGINIA", "51"),
            Map.entry("WASHINGTON", "53"), Map.entry("WEST VIRGINIA", "54"), Map.entry("WISCONSIN", "55"),
            Map.entry("WYOMING", "56"), Map.entry("PUERTO RICO", "72"), Map.entry("VIRGIN ISLANDS", "78"));

    private UsStates() {}

    /** The two-digit FIPS code of a USPS code or a full state name, or null. */
    static String fips2(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String key = value.strip().toUpperCase(Locale.ROOT);
        String fips = BY_USPS.get(key);
        return fips != null ? fips : BY_NAME.get(key);
    }
}
