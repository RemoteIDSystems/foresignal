package ai.foresignal.reference;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Named areas for scoping a rule (prompt 161, OpenAPI {@code searchReferenceAreas} and {@code getReferenceArea}):
 * the countries, admin-1 regions (states, provinces) and populated places a typed name finds, and the outline of a
 * country or admin-1 region to draw on a map.
 *
 * <p>A country and an admin-1 region are what a rule's {@code geofenceRef} names ({@code country} ISO2 codes and
 * {@code admin1} codes); a populated place is a point, which a rule uses as {@code near.point} with a radius. Countries
 * come from the bundled table (aliases included), so they are found with no reference data loaded; admin-1 regions
 * need {@code ref_admin1} and places {@code gazetteer_place}. A name can carry a qualifier after a comma, a country or
 * an admin-1 code or name: {@code "Paris, TX"}, {@code "Paris, France"}.
 *
 * <p>Ranking: an exact name before a name that starts with the text before one that contains it; then countries,
 * then admin-1 regions, then places by population.
 */
@Component
public class ReferenceAreas {

    /** Most results of one search. */
    public static final int MAX_LIMIT = 50;

    /** Results of a search without a limit. */
    public static final int DEFAULT_LIMIT = 10;

    /** Longest search text. */
    public static final int MAX_QUERY = 100;

    /** The simplification tolerance of an outline, in degrees (about 1 km at the equator). */
    static final double OUTLINE_TOLERANCE = 0.01;

    private static final Pattern ISO2 = Pattern.compile("^[A-Z]{2}$");

    /** Longest admin-1 code ({@code ref_admin1.code}). */
    private static final int MAX_CODE = 32;

    private final JdbcTemplate jdbc;
    private final CountryCentroids countries;
    private final BundledCountryPolygons bundled;
    private final RegionTagger tagger;
    private final GeoJsonWriter geoJson = new GeoJsonWriter();

    /** Creates the service. */
    public ReferenceAreas(JdbcTemplate jdbc, CountryCentroids countries, BundledCountryPolygons bundled,
            RegionTagger tagger) {
        this.jdbc = jdbc;
        this.countries = countries;
        this.bundled = bundled;
        this.tagger = tagger;
    }

    /** One result before ranking: how well its name matched (0 exact, 1 prefix, 2 contains) and its kind order. */
    private record Hit(int match, int kindOrder, long population, Map<String, Object> area) {}

    /**
     * The areas whose names match {@code text}, best first ({@code ReferenceArea} objects).
     *
     * @param text the name, optionally followed by a comma and a country or admin-1 qualifier
     * @param limit the most results (1 to {@value #MAX_LIMIT})
     * @throws ValidationException when the text is shorter than 2 or longer than {@value #MAX_QUERY} characters
     */
    public List<Map<String, Object>> search(String text, int limit) {
        String raw = text == null ? "" : text.strip();
        if (raw.length() < 2 || raw.length() > MAX_QUERY) {
            throw ValidationException.forField("q", "must be 2 to " + MAX_QUERY + " characters");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw ValidationException.forField("limit", "must be 1 to " + MAX_LIMIT);
        }
        int comma = raw.indexOf(',');
        String name = (comma < 0 ? raw : raw.substring(0, comma)).strip().toLowerCase(Locale.ROOT);
        String qualifier = comma < 0 ? "" : raw.substring(comma + 1).strip();
        if (name.length() < 2) {
            throw ValidationException.forField("q", "the name before a comma must be at least 2 characters");
        }
        Set<String> qualifierCountries = qualifier.isEmpty() ? Set.of() : countriesNamed(qualifier);
        List<Hit> hits = new ArrayList<>();
        if (qualifier.isEmpty()) {
            hits.addAll(countryHits(name));
        }
        if (tagger.loaded("ref_admin1")) {
            hits.addAll(admin1Hits(name, qualifier, qualifierCountries, limit));
        }
        if (tagger.loaded("gazetteer_place")) {
            hits.addAll(placeHits(name, qualifier, qualifierCountries, limit));
        }
        hits.sort(Comparator.comparingInt(Hit::match).thenComparingInt(Hit::kindOrder)
                .thenComparing(Comparator.comparingLong(Hit::population).reversed()));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Hit hit : hits) {
            if (out.size() == limit) {
                break;
            }
            out.add(hit.area());
        }
        return out;
    }

    /**
     * The outline of a country (ISO2 code) or admin-1 region ({@code CC-X}) as a {@code ReferenceAreaOutline}: its
     * names, bounding box and a simplified MultiPolygon. A country without loaded boundaries is drawn from the bundled
     * polygons.
     *
     * @throws ValidationException when {@code code} is empty or longer than an admin-1 code
     * @throws NotFoundException when no such region exists
     * @throws ConflictException {@code REFERENCE_DATA_MISSING} for an admin-1 code without loaded admin-1 boundaries
     */
    public Map<String, Object> outline(String code) {
        String upper = code == null ? "" : code.strip().toUpperCase(Locale.ROOT);
        if (ISO2.matcher(upper).matches()) {
            CountryRef country = countries.byIso2(upper)
                    .orElseThrow(() -> new NotFoundException("No country has the code " + upper + "."));
            Geometry geometry = tagger.loaded("ref_country")
                    ? simplified("SELECT ST_AsBinary(ST_SimplifyPreserveTopology(COALESCE(geom_simple, geom), ?))"
                            + " FROM ref_country WHERE iso2 = ?", upper)
                    : bundled.polygon(upper).orElse(null);
            if (geometry == null) {
                throw new NotFoundException("No outline is known for the country " + upper + ".");
            }
            return outlineMap("country", upper, country.name(), upper, country.name(), geometry);
        }
        if (upper.length() > 2 && upper.length() <= MAX_CODE) {
            if (!tagger.loaded("ref_admin1")) {
                throw new ConflictException(ErrorCodes.REFERENCE_DATA_MISSING,
                        "Admin-1 boundaries are not loaded; load them with POST /v1/reference/load.");
            }
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT name, iso2 FROM ref_admin1 WHERE code = ?",
                    upper);
            if (rows.isEmpty()) {
                throw new NotFoundException("No admin-1 region has the code " + upper + ".");
            }
            String iso2 = (String) rows.get(0).get("iso2");
            Geometry geometry = simplified("SELECT ST_AsBinary(ST_SimplifyPreserveTopology(COALESCE(geom_simple, geom),"
                    + " ?)) FROM ref_admin1 WHERE code = ?", upper);
            return outlineMap("admin1", upper, (String) rows.get(0).get("name"), iso2, countryName(iso2), geometry);
        }
        throw ValidationException.forField("code", "must be an ISO 3166-1 alpha-2 country code or an admin-1 code"
                + " such as US-TX (up to " + MAX_CODE + " characters)");
    }

    // ------------------------------------------------------------------------- search parts

    private List<Hit> countryHits(String name) {
        List<Hit> out = new ArrayList<>();
        for (CountryRef country : countries.all()) {
            int match = 3;
            if (isCode(country, name)) {
                match = 0;
            }
            for (String candidate : names(country)) {
                match = Math.min(match, match(candidate, name));
            }
            if (match < 3) {
                Map<String, Object> area = area("country", country.iso2(), country.name(), country.iso2(),
                        country.name(), null, null, country.labelLon(), country.labelLat(), null, null);
                out.add(new Hit(match, 0, 0, area));
            }
        }
        return out;
    }

    private List<Hit> admin1Hits(String name, String qualifier, Set<String> qualifierCountries, int limit) {
        List<Object> args = new ArrayList<>();
        String like = "%" + escapeLike(name) + "%";
        StringBuilder where = new StringBuilder("(lower(a.name) LIKE ? ESCAPE '\\' OR upper(a.code) = ?)");
        args.add(like);
        args.add(name.toUpperCase(Locale.ROOT));
        if (!qualifier.isEmpty()) {
            where.append(" AND a.iso2 = ANY(CAST(? AS text[]))");
            args.add(ai.foresignal.store.query.PgArrays.text(new ArrayList<>(qualifierCountries)));
        }
        args.add(name);
        args.add(escapeLike(name) + "%");
        args.add(limit * 3);
        // The label point is computed for the few rows kept, not for every candidate.
        String sql = "SELECT k.code, k.name, k.iso2, ST_X(p.pt) AS lon, ST_Y(p.pt) AS lat FROM ("
                + "SELECT a.code, a.name, a.iso2, COALESCE(a.geom_simple, a.geom) AS g FROM ref_admin1 a WHERE " + where
                + " ORDER BY lower(a.name) = ? DESC, lower(a.name) LIKE ? ESCAPE '\\' DESC, length(a.name), a.code"
                + " LIMIT ?) k, LATERAL (SELECT ST_PointOnSurface(k.g) AS pt) p";
        List<Hit> out = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList(sql, args.toArray())) {
            String code = (String) row.get("code");
            String regionName = (String) row.get("name");
            String iso2 = (String) row.get("iso2");
            int match = code.equalsIgnoreCase(name) ? 0 : match(regionName, name);
            Map<String, Object> area = area("admin1", code, regionName, iso2, countryName(iso2), code, regionName,
                    number(row.get("lon")), number(row.get("lat")), null, null);
            out.add(new Hit(match, 1, 0, area));
        }
        return out;
    }

    private List<Hit> placeHits(String name, String qualifier, Set<String> qualifierCountries, int limit) {
        List<Object> args = new ArrayList<>();
        String like = "%" + escapeLike(name) + "%";
        StringBuilder where = new StringBuilder("(lower(g.name) LIKE ? ESCAPE '\\' OR lower(g.ascii_name) LIKE ?"
                + " ESCAPE '\\')");
        args.add(like);
        args.add(like);
        if (!qualifier.isEmpty()) {
            // A qualifier names the country (code or name) or the admin-1 region the place lies in (TX, US-TX, Texas).
            // The region is found spatially, as records are tagged: GeoNames admin-1 codes do not map onto every
            // country's ref_admin1 codes.
            String q = qualifier.toUpperCase(Locale.ROOT);
            where.append(" AND (g.iso2 = ANY(CAST(? AS text[])) OR EXISTS (SELECT 1 FROM ref_admin1 qa WHERE"
                    + " (upper(qa.code) = ? OR upper(qa.code) = g.iso2 || '-' || ? OR lower(qa.name) LIKE ? ESCAPE"
                    + " '\\') AND ST_Intersects(qa.geom, g.geog::geometry)))");
            args.add(ai.foresignal.store.query.PgArrays.text(new ArrayList<>(qualifierCountries)));
            args.add(q);
            args.add(q);
            args.add(escapeLike(qualifier.toLowerCase(Locale.ROOT)) + "%");
        }
        args.add(name);
        args.add(name);
        args.add(escapeLike(name) + "%");
        args.add(escapeLike(name) + "%");
        args.add(limit * 3);
        // The admin-1 region a place lies in is found spatially for the rows kept, as records are tagged.
        String sql = "SELECT k.geonameid, k.name, k.iso2, k.population, k.lon, k.lat, r.code AS admin1,"
                + " r.name AS admin1_name FROM (SELECT g.geonameid, g.name, g.iso2, g.population,"
                + " g.geog::geometry AS pt, ST_X(g.geog::geometry) AS lon, ST_Y(g.geog::geometry) AS lat"
                + " FROM gazetteer_place g"
                + " WHERE " + where + " ORDER BY (lower(g.name) = ? OR lower(g.ascii_name) = ?) DESC,"
                + " (lower(g.name) LIKE ? ESCAPE '\\' OR lower(g.ascii_name) LIKE ? ESCAPE '\\') DESC,"
                + " g.population DESC NULLS LAST, g.geonameid LIMIT ?) k LEFT JOIN LATERAL (SELECT a.code, a.name FROM"
                + " ref_admin1 a WHERE ST_Intersects(a.geom, k.pt) ORDER BY a.code LIMIT 1) r ON true";
        List<Hit> out = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList(sql, args.toArray())) {
            String placeName = (String) row.get("name");
            String iso2 = (String) row.get("iso2");
            long population = row.get("population") instanceof Number n ? n.longValue() : 0;
            Map<String, Object> area = area("place", null, placeName, iso2, countryName(iso2),
                    (String) row.get("admin1"), (String) row.get("admin1_name"), number(row.get("lon")),
                    number(row.get("lat")), ((Number) row.get("geonameid")).longValue(),
                    row.get("population") == null ? null : population);
            out.add(new Hit(match(placeName, name), 2, population, area));
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    /** The ISO2 codes of the countries {@code qualifier} names: by code (ISO2 or ISO3), name or alias. */
    private Set<String> countriesNamed(String qualifier) {
        String q = qualifier.toLowerCase(Locale.ROOT);
        Set<String> out = new LinkedHashSet<>();
        for (CountryRef country : countries.all()) {
            boolean code = isCode(country, q);
            boolean named = names(country).stream().anyMatch(n -> n.toLowerCase(Locale.ROOT).startsWith(q));
            if (code || named && q.length() >= 3) {
                out.add(country.iso2());
            }
        }
        return out;
    }

    /** True when {@code text} is the country's ISO2 or ISO3 code, in any case. */
    private static boolean isCode(CountryRef country, String text) {
        return country.iso2().equalsIgnoreCase(text) || country.iso3() != null && country.iso3().equalsIgnoreCase(text);
    }

    private static List<String> names(CountryRef country) {
        List<String> out = new ArrayList<>();
        out.add(country.name());
        if (country.nameLong() != null) {
            out.add(country.nameLong());
        }
        out.addAll(country.aliases());
        return out;
    }

    /** 0 when {@code candidate} is {@code name}, 1 when it starts with it, 2 when it contains it, else 3. */
    static int match(String candidate, String name) {
        if (candidate == null) {
            return 3;
        }
        String c = candidate.toLowerCase(Locale.ROOT);
        return c.equals(name) ? 0 : c.startsWith(name) ? 1 : c.contains(name) ? 2 : 3;
    }

    private String countryName(String iso2) {
        return iso2 == null ? null : countries.byIso2(iso2).map(CountryRef::name).orElse(null);
    }

    private static Map<String, Object> area(String kind, String code, String name, String iso2, String countryName,
            String admin1, String admin1Name, double lon, double lat, Long placeId, Long population) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", kind);
        out.put("code", code);
        out.put("name", name);
        List<String> label = new ArrayList<>();
        label.add(name);
        if (!"admin1".equals(kind) && admin1Name != null && !admin1Name.equals(name)) {
            label.add(admin1Name);
        }
        if (!"country".equals(kind) && countryName != null) {
            label.add(countryName);
        }
        out.put("label", String.join(", ", label));
        out.put("countryIso2", iso2);
        out.put("countryName", countryName);
        out.put("admin1", admin1);
        out.put("admin1Name", admin1Name);
        out.put("lon", round(lon));
        out.put("lat", round(lat));
        out.put("placeId", placeId);
        out.put("population", population);
        return out;
    }

    private Map<String, Object> outlineMap(String kind, String code, String name, String iso2, String countryName,
            Geometry geometry) {
        Envelope box = geometry.getEnvelopeInternal();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", kind);
        out.put("code", code);
        out.put("name", name);
        out.put("countryIso2", iso2);
        out.put("countryName", countryName);
        out.put("bbox", List.of(round(box.getMinX()), round(box.getMinY()), round(box.getMaxX()),
                round(box.getMaxY())));
        out.put("geometry", geoJson.write(geometry));
        return out;
    }

    private Geometry simplified(String sql, String code) {
        byte[] wkb = jdbc.queryForObject(sql, byte[].class, OUTLINE_TOLERANCE, code);
        if (wkb == null) {
            throw new NotFoundException("No outline is stored for " + code + ".");
        }
        try {
            return new WKBReader().read(wkb);
        } catch (ParseException e) {
            throw new IllegalStateException("PostGIS returned unreadable WKB: " + e.getMessage(), e);
        }
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static double number(Object value) {
        return value instanceof Number n ? n.doubleValue() : Double.NaN;
    }

    private static double round(double value) {
        return Math.round(value * 1e5) / 1e5;
    }
}
