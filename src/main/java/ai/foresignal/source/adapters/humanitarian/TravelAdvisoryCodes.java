package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The State Department travel-advisory {@code Category} code set (prompt 090/097).
 *
 * <p>It is mostly FIPS 10-4, but not consistently, and FIPS 10-4 and ISO 3166-1 alpha-2 reuse the
 * same two letters for different countries. Reading the code as FIPS resolves almost everything —
 * {@code AS} Australia (an ISO2 reading would wrongly give American Samoa), {@code CH} China (not
 * Switzerland), {@code YM} Yemen — but the live API also sends ISO2 {@code KH} for Cambodia, whose
 * FIPS code is {@code CB}, and {@code SR} for Switzerland, whose FIPS code is {@code SZ} (and whose
 * FIPS {@code SR} is Suriname). This table records the exceptions explicitly rather than guessing,
 * then falls back to {@link CountryCentroids#byFips(String)}.
 *
 * <p>The collision cases are tested in {@code CountryCentroidsFipsTest} and
 * {@code UsTravelAdvisoryParserTest}.
 */
public final class TravelAdvisoryCodes {

    /**
     * {@code Category} codes whose meaning is not the FIPS 10-4 reading; maps to ISO2. The first group
     * is the FIPS/ISO2 collision set the checkpoint names; the second is the set of anomalous codes in
     * a live capture (2026-10-14) that the bundled FIPS table cannot resolve on its own.
     */
    private static final Map<String, String> EXCEPTIONS = Map.ofEntries(
            // Collision cases (FIPS 10-4 reading, or the API's ISO2 exception), tested explicitly.
            Map.entry("AS", "AU"), // FIPS 10-4 Australia (ISO2 AS is American Samoa)
            Map.entry("YM", "YE"), // FIPS 10-4 Yemen (there is no ISO2 YM)
            Map.entry("CH", "CN"), // FIPS 10-4 China (ISO2 CH is Switzerland)
            Map.entry("KH", "KH"), // the API's ISO2 Cambodia (its FIPS code is CB)
            // Other live-capture anomalies (ISO2 sent where FIPS 10-4 is the rule).
            Map.entry("SR", "CH"), // Switzerland (FIPS SZ; FIPS SR is Suriname)
            Map.entry("BI", "BA"), // Bosnia and Herzegovina (FIPS BK)
            Map.entry("GW", "GW"), // Guinea-Bissau (FIPS PU)
            Map.entry("MF", "MF"), // Saint-Martin (FIPS RN)
            Map.entry("MM", "MM"), // Burma/Myanmar (FIPS BM)
            Map.entry("NA", "NA"), // Namibia (FIPS WA)
            Map.entry("NN", "SX"), // Sint Maarten (FIPS NN)
            Map.entry("SV", "SI"), // Slovenia (FIPS SI)
            Map.entry("UC", "CW")); // Curaçao (FIPS UC; the bundled table has NT)

    private TravelAdvisoryCodes() {}

    /** The country a travel-advisory {@code Category} code names, or empty when unknown. */
    public static Optional<CountryRef> resolve(CountryCentroids countries, String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.strip().toUpperCase(Locale.ROOT);
        String override = EXCEPTIONS.get(normalized);
        if (override != null) {
            return countries.byIso2(override);
        }
        return countries.byFips(normalized);
    }
}
