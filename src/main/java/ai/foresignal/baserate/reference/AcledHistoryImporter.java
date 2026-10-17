package ai.foresignal.baserate.reference;

import ai.foresignal.api.error.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Imports ACLED history (prompt 121) through the 081 ACLED client with the operator's credentials, by country and year,
 * within the 081 budget. Rows are {@code egress=LOCAL_ONLY} and license {@code ACLED-EULA}: ACLED's terms forbid
 * redistribution, so the counts are computed on and only derived statistics leave the instance. Without both
 * credentials the import is recorded as {@code skipped} with the reason {@code credentials absent}.
 *
 * <p>Event types map to {@code conflict:battles}, {@code conflict:explosions-remote},
 * {@code conflict:violence-civilians}, {@code conflict:protests} and {@code conflict:riots}; strategic developments are
 * read but not counted. Pages of {@value #PAGE_LIMIT} rows are read until a shorter one (at most {@value #MAX_PAGES}
 * per country-year); events are de-duplicated by {@code event_id_cnty}.
 */
@Component
public class AcledHistoryImporter implements ReferenceImporter {

    /** The dataset. */
    public static final String DATASET = "acled";

    /** ACLED's page size. */
    public static final int PAGE_LIMIT = 5_000;

    /** The most pages per country and year. */
    public static final int MAX_PAGES = 40;

    /** The most countries per import (as the 081 adapter). */
    public static final int MAX_COUNTRIES = 30;

    /** The default first year (ACLED's global coverage is complete from 2018). */
    public static final int DEFAULT_FROM_YEAR = 2018;

    /** The skip reason without credentials. */
    public static final String CREDENTIALS_ABSENT = "credentials absent";

    private final AcledHistoryClient client;
    private final ReferenceRegionResolver regions;
    private final JsonMapper json;

    /** Creates the importer. */
    public AcledHistoryImporter(AcledHistoryClient client, ReferenceRegionResolver regions, JsonMapper json) {
        this.client = client;
        this.regions = regions;
        this.json = json;
    }

    @Override
    public String dataset() {
        return DATASET;
    }

    @Override
    public String source() {
        return ReferenceFamilies.ACLED;
    }

    @Override
    public Map<String, Object> normalize(Map<String, Object> params) {
        int[] years = ImportParams.years(params, DEFAULT_FROM_YEAR);
        List<String> countries = ImportParams.upperList(params, "countries");
        if (countries.isEmpty() || countries.size() > MAX_COUNTRIES) {
            throw ValidationException.forField("params.countries", "must hold 1 to " + MAX_COUNTRIES
                    + " ISO 3166 alpha-2 codes");
        }
        for (String c : countries) {
            if (!c.matches("^[A-Z]{2}$") || regions.numericOf(c) == null) {
                throw ValidationException.forField("params.countries", "'" + c + "' is not a known ISO 3166 alpha-2"
                        + " code");
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("countries", countries);
        out.put("yearFrom", years[0]);
        out.put("yearTo", years[1]);
        return out;
    }

    @Override
    public Availability availability(Map<String, Object> params) {
        return client.credentialsPresent() ? Availability.available() : Availability.skip(CREDENTIALS_ABSENT);
    }

    @Override
    public LocalDate[] period(Map<String, Object> params) {
        return new LocalDate[] {ReferenceFamilies.yearChunk(((Number) params.get("yearFrom")).intValue())[0],
                ReferenceFamilies.yearChunk(((Number) params.get("yearTo")).intValue())[1]};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(ImportRun run) throws Exception {
        List<String> countries = (List<String>) run.params().get("countries");
        int from = ((Number) run.params().get("yearFrom")).intValue();
        int to = ((Number) run.params().get("yearTo")).intValue();
        int total = countries.size() * (to - from + 1);
        int step = 0;
        for (String country : countries) {
            String numeric = regions.numericOf(country);
            for (int year = from; year <= to; year++, step++) {
                String chunk = country + ":" + year;
                run.checkpoint((double) step / total);
                if (run.done(chunk)) {
                    continue;
                }
                LocalDate[] range = ReferenceFamilies.yearChunk(year);
                WeeklyCounts counts = new WeeklyCounts();
                Set<String> seen = new HashSet<>();
                long read = 0;
                for (int page = 1; page <= MAX_PAGES; page++) {
                    JsonNode data = data(client.page(numeric, range[0], range[1].minusDays(1), page));
                    for (JsonNode row : data) {
                        read++;
                        accept(row, country, counts, seen);
                    }
                    if (data.size() < PAGE_LIMIT) {
                        break;
                    }
                }
                run.writeChunk(chunk, range[0], range[1], List.of(country), counts, read);
            }
        }
        run.checkpoint(1);
    }

    /** The {@code data} array of a page; an unsuccessful page fails the chunk. */
    JsonNode data(byte[] body) {
        JsonNode root = json.readTree(body);
        if (root == null || !root.isObject()) {
            throw new IllegalStateException("ACLED answered something other than a JSON object");
        }
        JsonNode success = root.get("success");
        if (success != null && success.isBoolean() && !success.asBoolean()) {
            throw new IllegalStateException("ACLED refused the query");
        }
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new IllegalStateException("ACLED answered without a data array");
        }
        return data;
    }

    /** Counts one event row of the requested country. */
    void accept(JsonNode row, String requested, WeeklyCounts counts, Set<String> seen) {
        String id = text(row.get("event_id_cnty"));
        if (id != null && !seen.add(id)) {
            return;
        }
        String family = ReferenceFamilies.acledFamily(text(row.get("event_type")));
        LocalDate day = day(text(row.get("event_date")));
        if (family == null || day == null) {
            return;
        }
        String country = regions.countryByNumeric(text(row.get("iso")));
        if (country == null || !country.equals(requested)) {
            country = requested;
        }
        Long fatalities = number(row.get("fatalities"));
        counts.add(family, country, day, fatalities == null ? 0L : fatalities);
        String admin1 = regions.admin1(country, text(row.get("admin1")));
        if (admin1 != null) {
            counts.add(family, admin1, day, fatalities == null ? 0L : fatalities);
        }
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String s = node.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static Long number(JsonNode node) {
        String s = text(node);
        if (s == null) {
            return null;
        }
        try {
            return (long) Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
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
