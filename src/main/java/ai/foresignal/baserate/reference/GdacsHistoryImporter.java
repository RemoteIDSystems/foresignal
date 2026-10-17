package ai.foresignal.baserate.reference;

import ai.foresignal.api.error.ValidationException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Imports GDACS history (prompt 121), open with attribution, through the GDACS event search API
 * ({@code GET /gdacsapi/api/Events/geteventlist/search?eventlist={type}&alertlevel=orange;red&fromDate&toDate
 * &pageSize=100&pageNumber={n}}, verified 2026-10-17 against the published Swagger document and a live call: a GeoJSON
 * FeatureCollection whose features carry {@code eventtype}, {@code eventid}, {@code alertlevel}, {@code fromdate},
 * {@code iso3} and {@code affectedcountries}; at most 100 per page). One request series per hazard type and year; a
 * year chunk holds every requested hazard, so the any-hazard families are written whole. Events are de-duplicated by
 * type and id; the week is that of {@code fromdate}; the region is {@code iso3}, else the first affected country, else
 * the point, else {@code ZZ}.
 */
@Component
public class GdacsHistoryImporter implements ReferenceImporter {

    /** The dataset. */
    public static final String DATASET = "gdacs";

    /** The production base URL. */
    public static final String DEFAULT_BASE_URL = "https://www.gdacs.org";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String URL_PROPERTY = "foresignal.test.baserate.gdacs-url";

    /** The search path. */
    public static final String PATH = "/gdacsapi/api/Events/geteventlist/search";

    /** The page size (the API's maximum). */
    public static final int PAGE_SIZE = 100;

    /** The most pages read per hazard and year. */
    public static final int MAX_PAGES = 50;

    /** The default first year. */
    public static final int DEFAULT_FROM_YEAR = 2000;

    private final ReferenceRegionResolver regions;
    private final ObjectProvider<ReferenceHttp> http;
    private final JsonMapper json;
    private final String baseUrl;

    /** Creates the importer. */
    public GdacsHistoryImporter(ReferenceRegionResolver regions, ObjectProvider<ReferenceHttp> http, JsonMapper json,
            @Value("${" + URL_PROPERTY + ":}") String baseUrlOverride) {
        this.regions = regions;
        this.http = http;
        this.json = json;
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride;
        this.baseUrl = chosen.strip().replaceAll("/+$", "");
    }

    @Override
    public String dataset() {
        return DATASET;
    }

    @Override
    public String source() {
        return ReferenceFamilies.GDACS;
    }

    @Override
    public Map<String, Object> normalize(Map<String, Object> params) {
        int[] years = ImportParams.years(params, DEFAULT_FROM_YEAR);
        List<String> hazards = ImportParams.upperList(params, "hazardTypes");
        for (String h : hazards) {
            if (!ReferenceFamilies.GDACS_HAZARDS.contains(h)) {
                throw ValidationException.forField("params.hazardTypes", "'" + h + "' is not one of "
                        + ReferenceFamilies.GDACS_HAZARDS);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("yearFrom", years[0]);
        out.put("yearTo", years[1]);
        out.put("hazardTypes", hazards.isEmpty() ? ReferenceFamilies.GDACS_HAZARDS : hazards);
        return out;
    }

    @Override
    public Availability availability(Map<String, Object> params) {
        return Availability.available();
    }

    @Override
    public LocalDate[] period(Map<String, Object> params) {
        return new LocalDate[] {ReferenceFamilies.yearChunk(((Number) params.get("yearFrom")).intValue())[0],
                ReferenceFamilies.yearChunk(((Number) params.get("yearTo")).intValue())[1]};
    }

    @Override
    public void run(ImportRun run) throws Exception {
        run(run, http.getObject().system());
    }

    /**
     * Runs with the given fetcher.
     *
     * @param run the run
     * @param fetcher the HTTP path
     * @throws Exception when a chunk fails
     */
    public void run(ImportRun run, ReferenceHttp.Fetcher fetcher) throws Exception {
        int from = ((Number) run.params().get("yearFrom")).intValue();
        int to = ((Number) run.params().get("yearTo")).intValue();
        List<String> hazards = hazards(run.params());
        int total = to - from + 1;
        for (int year = from; year <= to; year++) {
            String chunk = Integer.toString(year);
            run.checkpoint((double) (year - from) / total);
            if (run.done(chunk)) {
                continue;
            }
            LocalDate[] range = ReferenceFamilies.yearChunk(year);
            WeeklyCounts counts = new WeeklyCounts();
            Set<String> seen = new HashSet<>();
            long read = 0;
            for (String hazard : hazards) {
                for (int page = 1; page <= MAX_PAGES; page++) {
                    ReferenceHttp.Response response = fetcher.get(uri(hazard, range[0], range[1].minusDays(1), page),
                            Map.of());
                    if (response.status() == 404 || response.status() == 204) {
                        break;
                    }
                    if (response.status() != 200) {
                        throw new IllegalStateException("GDACS answered HTTP " + response.status() + " for " + hazard
                                + " " + year);
                    }
                    JsonNode features = response.body().length == 0 ? null : json.readTree(response.body())
                            .get("features");
                    if (features == null || !features.isArray() || features.isEmpty()) {
                        break;
                    }
                    for (JsonNode feature : features) {
                        read++;
                        accept(feature, hazard, counts, seen, range);
                    }
                    if (features.size() < PAGE_SIZE) {
                        break;
                    }
                }
            }
            run.writeChunk(chunk, range[0], range[1], List.of(), counts, read);
        }
        run.checkpoint(1);
    }

    @SuppressWarnings("unchecked")
    private static List<String> hazards(Map<String, Object> params) {
        Object value = params.get("hazardTypes");
        return value instanceof List<?> list && !list.isEmpty() ? (List<String>) list : ReferenceFamilies.GDACS_HAZARDS;
    }

    /** The search URL of one page. */
    URI uri(String hazard, LocalDate from, LocalDate to, int page) {
        return URI.create(baseUrl + PATH + "?eventlist=" + hazard + "&alertlevel=orange;red&fromDate=" + from
                + "&toDate=" + to + "&pageSize=" + PAGE_SIZE + "&pageNumber=" + page);
    }

    /** Counts one feature. */
    void accept(JsonNode feature, String hazard, WeeklyCounts counts, Set<String> seen, LocalDate[] range) {
        JsonNode p = feature == null ? null : feature.get("properties");
        if (p == null || !p.isObject()) {
            return;
        }
        String type = text(p.get("eventtype"));
        String id = text(p.get("eventid"));
        if (!seen.add((type == null ? hazard : type) + ":" + id)) {
            return;
        }
        List<String> families = ReferenceFamilies.gdacsFamilies(type == null ? hazard : type, text(p.get("alertlevel")));
        LocalDate day = day(text(p.get("fromdate")));
        if (families.isEmpty() || day == null) {
            return;
        }
        if (day.isBefore(range[0])) {
            day = range[0];
        }
        if (!day.isBefore(range[1])) {
            return;
        }
        String country = regions.countryByIso3(text(p.get("iso3")));
        if (country == null && p.get("affectedcountries") instanceof JsonNode affected && affected.isArray()
                && !affected.isEmpty()) {
            String iso2 = text(affected.get(0).get("iso2"));
            country = iso2 != null && regions.knownCountry(iso2) ? iso2 : null;
        }
        if (country == null) {
            JsonNode coordinates = feature.path("geometry").path("coordinates");
            if (coordinates.isArray() && coordinates.size() >= 2 && coordinates.get(0).isNumber()) {
                country = regions.countryAt(coordinates.get(0).asDouble(), coordinates.get(1).asDouble());
            } else {
                country = ReferenceRegionResolver.UNKNOWN;
            }
        }
        for (String family : families) {
            counts.add(family, country, day, null);
        }
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String s = node.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static LocalDate day(String text) {
        if (text == null || text.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(text.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
