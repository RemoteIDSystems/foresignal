package ai.foresignal.baserate.reference;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Maps reference-dataset locations to 040 region codes (prompt 121): country names (UCDP's names carry former names in
 * parentheses, e.g. {@code "Russia (Soviet Union)"}), ISO alpha-3 and numeric codes, first-level names matched against
 * the 040 {@code ref_admin1} table, and points through the 040 tagger, where a point outside every country (offshore)
 * takes the nearest country within {@value #OFFSHORE_KM} km and otherwise {@code ZZ}.
 */
@Component
public class ReferenceRegionResolver {

    /** The region of an event no country can be found for. */
    public static final String UNKNOWN = "ZZ";

    /** How far offshore a point may be and still be given the nearest country. */
    public static final double OFFSHORE_KM = 200;

    private static final Logger log = LoggerFactory.getLogger(ReferenceRegionResolver.class);
    private static final Pattern PARENTHESIS = Pattern.compile("^(.*?)\\s*\\((.*)\\)\\s*$");
    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");
    private static final List<String> ADMIN_SUFFIXES = List.of(" province", " governorate", " state", " region",
            " oblast", " district", " department", " prefecture", " county", " division", " municipality",
            " territory", " city");

    private final CountryCentroids countries;
    private final RegionTagger tagger;
    private final JdbcTemplate jdbc;
    private final Map<String, String> admin1Cache = new ConcurrentHashMap<>();
    private final Map<Long, String> offshoreCache = new ConcurrentHashMap<>();

    /** Creates the resolver. */
    public ReferenceRegionResolver(CountryCentroids countries, RegionTagger tagger, JdbcTemplate jdbc) {
        this.countries = countries;
        this.tagger = tagger;
        this.jdbc = jdbc;
    }

    /**
     * The ISO2 code of a country name, or null: the full name, then the name before a parenthesis, then the name in
     * it.
     */
    public String countryByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String found = countries.byName(name).map(CountryRef::iso2).orElse(null);
        if (found != null) {
            return found;
        }
        Matcher m = PARENTHESIS.matcher(name.strip());
        if (m.matches()) {
            found = countries.byName(m.group(1)).map(CountryRef::iso2).orElse(null);
            if (found == null) {
                found = countries.byName(m.group(2)).map(CountryRef::iso2).orElse(null);
            }
        }
        return found;
    }

    /** The ISO2 code of an ISO alpha-3 code, or null. */
    public String countryByIso3(String iso3) {
        return iso3 == null ? null : countries.byIso3(iso3).map(CountryRef::iso2).orElse(null);
    }

    /** The ISO2 code of an ISO 3166 numeric code, or null. */
    public String countryByNumeric(String numeric) {
        if (numeric == null || !numeric.strip().matches("^[0-9]{1,3}$")) {
            return null;
        }
        String padded = String.format(Locale.ROOT, "%03d", Integer.parseInt(numeric.strip()));
        for (CountryRef c : countries.all()) {
            if (padded.equals(c.isoNumeric())) {
                return c.iso2();
            }
        }
        return null;
    }

    /** The ISO 3166 numeric code of an ISO2 code (ACLED's {@code iso} filter), or null. */
    public String numericOf(String iso2) {
        return countries.byIso2(iso2).map(CountryRef::isoNumeric).map(n -> Integer.toString(Integer.parseInt(n)))
                .orElse(null);
    }

    /** True when {@code iso2} is a known country. */
    public boolean knownCountry(String iso2) {
        return iso2 != null && countries.isKnown(iso2);
    }

    /**
     * The country of a point: the 040 tag, else the nearest country within {@value #OFFSHORE_KM} km (offshore
     * events), else {@value #UNKNOWN}.
     */
    public String countryAt(double lon, double lat) {
        if (!Double.isFinite(lon) || !Double.isFinite(lat) || Math.abs(lon) > 180 || Math.abs(lat) > 90) {
            return UNKNOWN;
        }
        RegionTags tags = tagger == null ? null : tagger.tag(lon, lat);
        if (tags != null && tags.iso2() != null && REGION.matcher(tags.iso2()).matches()) {
            return tags.iso2();
        }
        long key = Math.round(lon * 10) * 10_000L + Math.round(lat * 10);
        return offshoreCache.computeIfAbsent(key, k -> nearestCountry(lon, lat));
    }

    /** The nearest country within {@value #OFFSHORE_KM} km, or {@value #UNKNOWN}. */
    String nearestCountry(double lon, double lat) {
        if (jdbc == null) {
            return UNKNOWN;
        }
        try {
            List<String> found = jdbc.queryForList("SELECT iso2 FROM ref_country WHERE iso2 IS NOT NULL AND"
                    + " ST_DWithin(coalesce(geom_simple, geom)::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)"
                    + "::geography, ?) ORDER BY ST_Distance(coalesce(geom_simple, geom)::geography,"
                    + " ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) LIMIT 1", String.class, lon, lat,
                    OFFSHORE_KM * 1000, lon, lat);
            return found.isEmpty() || found.get(0) == null ? UNKNOWN : found.get(0);
        } catch (DataAccessException e) {
            log.debug("Offshore lookup failed at {}, {}: {}", lon, lat, e.getMessage());
            return UNKNOWN;
        }
    }

    /**
     * The 040 admin-1 code of a first-level name in a country, or null: the name, with its administrative suffix
     * ("Medea province") removed, matched case-insensitively against {@code ref_admin1}.
     */
    public String admin1(String iso2, String name) {
        if (iso2 == null || name == null || name.isBlank()) {
            return null;
        }
        String cleaned = name.strip().toLowerCase(Locale.ROOT);
        for (String suffix : ADMIN_SUFFIXES) {
            if (cleaned.endsWith(suffix)) {
                cleaned = cleaned.substring(0, cleaned.length() - suffix.length()).strip();
                break;
            }
        }
        String key = iso2 + "|" + cleaned;
        String cached = admin1Cache.get(key);
        if (cached != null) {
            return cached.isEmpty() ? null : cached;
        }
        String code = null;
        if (jdbc == null) {
            return null;
        }
        try {
            List<String> found = jdbc.queryForList("SELECT code FROM ref_admin1 WHERE iso2 = ? AND (lower(name) = ? OR"
                    + " lower(name) = ?) ORDER BY code LIMIT 1", String.class, iso2, cleaned,
                    name.strip().toLowerCase(Locale.ROOT));
            if (!found.isEmpty() && found.get(0) != null && REGION.matcher(found.get(0)).matches()
                    && found.get(0).length() > 2 && found.get(0).startsWith(iso2 + "-")) {
                code = found.get(0);
            }
        } catch (DataAccessException e) {
            log.debug("Admin-1 lookup failed for {} {}: {}", iso2, name, e.getMessage());
        }
        admin1Cache.put(key, code == null ? "" : code);
        return code;
    }
}
