package ai.foresignal.reference;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Validates region codes (prompt 040): the {@code country}, {@code admin1} and {@code county} filters and
 * a rule's {@code geofenceRef}. Unknown codes are {@code 400 UNKNOWN_REGION}.
 *
 * <ul>
 *   <li>A country must be in the bundled {@link CountryCentroids} table (always available).
 *   <li>An admin-1 code must be in {@code ref_admin1} when it is loaded; otherwise it must look like ISO
 *       3166-2 ({@code US-TX}) with a known country, or like a Natural Earth {@code adm1_code}.
 *   <li>A county must be five digits, and in {@code ref_admin2} when it is loaded.
 * </ul>
 */
@Component
public class RegionCodes {

    /** ISO 3166-2. */
    static final Pattern ISO_3166_2 = Pattern.compile("^[A-Z]{2}-[A-Z0-9]{1,3}$");

    /** Natural Earth's {@code adm1_code}, the fallback where ISO has no code. */
    static final Pattern ADM1_CODE = Pattern.compile("^[A-Z]{3}-[0-9]{1,6}$");

    /** A US county FIPS code. */
    static final Pattern FIPS5 = Pattern.compile("^[0-9]{5}$");

    private final CountryCentroids countries;
    private final JdbcTemplate jdbc;
    private final RegionTagger tagger;

    /** Creates the validator. */
    public RegionCodes(CountryCentroids countries, JdbcTemplate jdbc, RegionTagger tagger) {
        this.countries = countries;
        this.jdbc = jdbc;
        this.tagger = tagger;
    }

    /**
     * The filter of the request parameters {@code country}, {@code admin1} and {@code county}
     * (comma-separated or repeated), or null when none is given.
     *
     * @throws ValidationException {@code UNKNOWN_REGION} for an unknown code
     */
    public RegionFilter filter(List<String> country, List<String> admin1, List<String> county) {
        return filter(country, admin1, county, "");
    }

    /**
     * {@link #filter(List, List, List)} with {@code prefix} before the field names in errors (for
     * example {@code scope.}).
     */
    public RegionFilter filter(List<String> country, List<String> admin1, List<String> county, String prefix) {
        List<String> c = upper(QueryParams.multi(prefix + "country", country));
        List<String> a = upper(QueryParams.multi(prefix + "admin1", admin1));
        List<String> n = QueryParams.multi(prefix + "county", county);
        validate(c, a, n, prefix);
        return RegionFilter.of(c, a, n);
    }

    /**
     * Checks every code.
     *
     * @throws ValidationException {@code UNKNOWN_REGION} listing every unknown code
     */
    public void validate(List<String> country, List<String> admin1, List<String> county, String prefix) {
        List<FieldError> errors = new ArrayList<>();
        for (String code : country) {
            if (!countries.isKnown(code)) {
                errors.add(new FieldError(prefix + "country", "unknown country code: " + code));
            }
        }
        Set<String> knownAdmin1 = known("ref_admin1", "code", admin1);
        for (String code : admin1) {
            boolean ok;
            if (knownAdmin1 != null) {
                ok = knownAdmin1.contains(code);
            } else {
                ok = (ISO_3166_2.matcher(code).matches() && countries.isKnown(code.substring(0, 2)))
                        || ADM1_CODE.matcher(code).matches();
            }
            if (!ok) {
                errors.add(new FieldError(prefix + "admin1", "unknown first-level region code: " + code));
            }
        }
        Set<String> knownCounties = known("ref_admin2", "fips5", county);
        for (String code : county) {
            boolean ok = FIPS5.matcher(code).matches() && (knownCounties == null || knownCounties.contains(code));
            if (!ok) {
                errors.add(new FieldError(prefix + "county", "unknown county FIPS code: " + code));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.UNKNOWN_REGION, "Unknown region code.", errors);
        }
    }

    /** The codes of {@code codes} present in {@code table}, or null when the table is not loaded. */
    private Set<String> known(String table, String column, List<String> codes) {
        if (codes.isEmpty() || !tagger.loaded(table)) {
            return null;
        }
        try {
            return Set.copyOf(jdbc.queryForList("SELECT " + column + " FROM " + table + " WHERE " + column
                    + " = ANY(CAST(? AS text[]))", String.class, ai.foresignal.store.query.PgArrays.text(codes)));
        } catch (DataAccessException e) {
            return null;
        }
    }

    private static List<String> upper(List<String> values) {
        return values.stream().map(v -> v.toUpperCase(Locale.ROOT)).toList();
    }
}
