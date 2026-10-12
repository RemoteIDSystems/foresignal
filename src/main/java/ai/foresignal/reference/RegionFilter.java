package ai.foresignal.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The {@code country}, {@code admin1} and {@code county} filters of a read (prompt 040), validated by
 * {@link RegionCodes}. Members are ANDed; the codes within a member are ORed. Request-scoped: carried on
 * the caller's {@code ReadRestriction} ({@code withRegionFilter}) and never stored with a token.
 *
 * <p>Records are matched through {@code record_region}, so a filter sees only tagged records: records of
 * {@code lazy} sources that were never read, and movers that have not moved past the retag distance since
 * they crossed a border, may lag by up to one retag threshold. Alerts are matched through the region
 * snapshot taken when they fired ({@code alert.region}).
 *
 * @param countries ISO 3166-1 alpha-2 codes; empty for any
 * @param admin1 ISO 3166-2 codes (or Natural Earth {@code adm1_code}s); empty for any
 * @param counties US county FIPS5 codes; empty for any
 */
public record RegionFilter(List<String> countries, List<String> admin1, List<String> counties) {

    /** Copies the lists. */
    public RegionFilter {
        countries = countries == null ? List.of() : List.copyOf(countries);
        admin1 = admin1 == null ? List.of() : List.copyOf(admin1);
        counties = counties == null ? List.of() : List.copyOf(counties);
    }

    /** The filter of the three members, or null when all are empty (nothing to filter). */
    public static RegionFilter of(List<String> countries, List<String> admin1, List<String> counties) {
        RegionFilter filter = new RegionFilter(countries, admin1, counties);
        return filter.isEmpty() ? null : filter;
    }

    /** True when no member narrows anything. */
    public boolean isEmpty() {
        return countries.isEmpty() && admin1.isEmpty() && counties.isEmpty();
    }

    /** A stable text form, for cursor fingerprints, entity tags and cache keys. */
    public String canonical() {
        return "c=" + String.join(",", countries.stream().sorted().toList()) + ";a="
                + String.join(",", admin1.stream().sorted().toList()) + ";n="
                + String.join(",", counties.stream().sorted().toList());
    }

    /**
     * The predicate over a record's key: {@code EXISTS} a {@code record_region} row of the record whose
     * members match.
     *
     * @param sourceColumn the record's source id column
     * @param externalIdColumn the record's external id column
     * @param textArray binds a list of codes and returns its {@code text[]} placeholder
     */
    public String sql(String sourceColumn, String externalIdColumn, Function<List<String>, String> textArray) {
        List<String> parts = new ArrayList<>();
        parts.add("rr.source_id = " + sourceColumn);
        parts.add("rr.external_id = " + externalIdColumn);
        if (!countries.isEmpty()) {
            parts.add("rr.iso2 = ANY(" + textArray.apply(countries) + ")");
        }
        if (!admin1.isEmpty()) {
            parts.add("rr.admin1 = ANY(" + textArray.apply(admin1) + ")");
        }
        if (!counties.isEmpty()) {
            parts.add("rr.admin2 = ANY(" + textArray.apply(counties) + ")");
        }
        return "EXISTS (SELECT 1 FROM record_region rr WHERE " + String.join(" AND ", parts) + ")";
    }

    /**
     * The predicate over an alert's region snapshot ({@code alert.region}, a {@code RegionTags} object).
     *
     * @param regionColumn the jsonb column, for example {@code a.region}
     * @param textArray binds a list of codes and returns its {@code text[]} placeholder
     */
    public String jsonSql(String regionColumn, Function<List<String>, String> textArray) {
        List<String> parts = new ArrayList<>();
        if (!countries.isEmpty()) {
            parts.add("(" + regionColumn + " ->> 'iso2') = ANY(" + textArray.apply(countries) + ")");
        }
        if (!admin1.isEmpty()) {
            parts.add("(" + regionColumn + " ->> 'admin1') = ANY(" + textArray.apply(admin1) + ")");
        }
        if (!counties.isEmpty()) {
            parts.add("(" + regionColumn + " ->> 'admin2') = ANY(" + textArray.apply(counties) + ")");
        }
        return parts.isEmpty() ? "TRUE" : "(" + regionColumn + " IS NOT NULL AND " + String.join(" AND ", parts) + ")";
    }

    /** The in-memory test, the same as the SQL: null tags never match. */
    public boolean test(RegionTags tags) {
        if (tags == null) {
            return false;
        }
        return (countries.isEmpty() || countries.contains(tags.iso2()))
                && (admin1.isEmpty() || admin1.contains(tags.admin1()))
                && (counties.isEmpty() || counties.contains(tags.admin2()));
    }
}
