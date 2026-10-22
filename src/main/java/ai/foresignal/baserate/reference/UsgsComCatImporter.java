package ai.foresignal.baserate.reference;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.formats.csv.CsvReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Imports USGS ComCat history (prompt 121), public domain, through the FDSN event service
 * ({@code https://earthquake.usgs.gov/fdsnws/event/1/query}, verified 2026-10-17): per year chunk
 * {@code format=csv&minmagnitude={m}&starttime&endtime&orderby=time-asc&limit=20000}. The service refuses queries over
 * 20,000 events, so a page that comes back full is split in two by time and each half read again; events are
 * de-duplicated by {@code id}. Only {@code type=earthquake} rows count. Families {@code seismic:m4.5},
 * {@code seismic:m5} and {@code seismic:m6} (cumulative). The region is the 040 country of the epicenter; offshore
 * events take the nearest country within 200 km, else {@code ZZ}.
 */
@Component
public class UsgsComCatImporter implements ReferenceImporter {

    /** The dataset. */
    public static final String DATASET = "usgs-comcat";

    /** The production endpoint. */
    public static final String DEFAULT_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    /** Test-only override of {@link #DEFAULT_URL}. */
    public static final String URL_PROPERTY = "foresignal.test.baserate.usgs-url";

    /** The service's per-query limit. */
    public static final int LIMIT = 20_000;

    /** The default minimum magnitude. */
    public static final double DEFAULT_MIN_MAGNITUDE = 4.5;

    /** The default first year (ComCat's global M4.5+ catalog is fairly complete from the 1970s). */
    public static final int DEFAULT_FROM_YEAR = 1973;

    private final ReferenceRegionResolver regions;
    private final ObjectProvider<ReferenceHttp> http;
    private final String url;

    /** Creates the importer. */
    public UsgsComCatImporter(ReferenceRegionResolver regions, ObjectProvider<ReferenceHttp> http,
            @Value("${" + URL_PROPERTY + ":}") String urlOverride) {
        this.regions = regions;
        this.http = http;
        this.url = urlOverride == null || urlOverride.isBlank() ? DEFAULT_URL : urlOverride.strip();
    }

    @Override
    public String dataset() {
        return DATASET;
    }

    @Override
    public String source() {
        return ReferenceFamilies.USGS;
    }

    @Override
    public Map<String, Object> normalize(Map<String, Object> params) {
        int[] years = ImportParams.years(params, DEFAULT_FROM_YEAR);
        double min = ImportParams.number(params, "minMagnitude", DEFAULT_MIN_MAGNITUDE);
        if (min < 2.5 || min > 9) {
            throw ValidationException.forField("params.minMagnitude", "must be between 2.5 and 9");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("yearFrom", years[0]);
        out.put("yearTo", years[1]);
        out.put("minMagnitude", min);
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
     * Runs with the given fetcher (tests pass one pointing at WireMock).
     *
     * @param run the run
     * @param fetcher the HTTP path
     * @throws Exception when a chunk fails; completed chunks stay written
     */
    public void run(ImportRun run, ReferenceHttp.Fetcher fetcher) throws Exception {
        int from = ((Number) run.params().get("yearFrom")).intValue();
        int to = ((Number) run.params().get("yearTo")).intValue();
        double min = ((Number) run.params().get("minMagnitude")).doubleValue();
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
            long[] read = new long[1];
            read(fetcher, range[0].atStartOfDay(ZoneOffset.UTC).toInstant(),
                    range[1].atStartOfDay(ZoneOffset.UTC).toInstant(), min, counts, seen, read, 0);
            run.writeChunk(chunk, range[0], range[1], List.of(), counts, read[0]);
        }
        run.checkpoint(1);
    }

    /** The query URL of one time range. */
    URI uri(Instant start, Instant end, double min) {
        return URI.create(url + "?format=csv&minmagnitude=" + min + "&starttime=" + start + "&endtime=" + end
                + "&orderby=time-asc&limit=" + LIMIT);
    }

    private void read(ReferenceHttp.Fetcher fetcher, Instant start, Instant end, double min, WeeklyCounts counts,
            Set<String> seen, long[] read, int depth) throws Exception {
        ReferenceHttp.Response response = fetcher.get(uri(start, end, min), Map.of());
        if (response.status() == 204) {
            return;
        }
        if (response.status() == 400 && depth < 12) {
            // Too many events for one query: split the range.
            split(fetcher, start, end, min, counts, seen, read, depth);
            return;
        }
        if (response.status() != 200) {
            throw new IllegalStateException("USGS ComCat answered HTTP " + response.status() + " for " + start + " to "
                    + end);
        }
        int rows = 0;
        try (CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(response.body()), StandardCharsets.UTF_8)))) {
            List<String> header = reader.next();
            if (header == null || header.size() <= 1) {
                return;
            }
            Map<String, Integer> index = CsvReader.index(header);
            List<String> row;
            List<Object[]> events = new ArrayList<>();
            while ((row = reader.next()) != null) {
                if (row.size() <= 1) {
                    continue;
                }
                rows++;
                events.add(new Object[] {CsvReader.get(row, index, "ID"), CsvReader.get(row, index, "TIME"),
                        CsvReader.get(row, index, "MAG"), CsvReader.get(row, index, "LATITUDE"),
                        CsvReader.get(row, index, "LONGITUDE"), CsvReader.get(row, index, "TYPE")});
            }
            if (rows >= LIMIT && depth < 12 && Duration.between(start, end).toHours() > 24) {
                split(fetcher, start, end, min, counts, seen, read, depth);
                return;
            }
            for (Object[] e : events) {
                accept((String) e[0], (String) e[1], (String) e[2], (String) e[3], (String) e[4], (String) e[5],
                        counts, seen);
            }
        }
        read[0] += rows;
    }

    private void split(ReferenceHttp.Fetcher fetcher, Instant start, Instant end, double min, WeeklyCounts counts,
            Set<String> seen, long[] read, int depth) throws Exception {
        Instant middle = start.plusSeconds(Duration.between(start, end).getSeconds() / 2);
        read(fetcher, start, middle, min, counts, seen, read, depth + 1);
        read(fetcher, middle, end, min, counts, seen, read, depth + 1);
    }

    /** Counts one event row. */
    void accept(String id, String time, String mag, String lat, String lon, String type, WeeklyCounts counts,
            Set<String> seen) {
        if (type != null && !"earthquake".equals(type.toLowerCase(Locale.ROOT))) {
            return;
        }
        if (id != null && !seen.add(id)) {
            return;
        }
        Double magnitude = number(mag);
        Double latitude = number(lat);
        Double longitude = number(lon);
        LocalDate day;
        try {
            day = time == null ? null : LocalDate.ofInstant(Instant.parse(time), ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            day = null;
        }
        if (magnitude == null || day == null) {
            return;
        }
        String country = latitude == null || longitude == null ? ReferenceRegionResolver.UNKNOWN
                : regions.countryAt(longitude, latitude);
        for (String family : ReferenceFamilies.seismicFamilies(magnitude)) {
            counts.add(family, country, day, null);
        }
    }

    private static Double number(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(text.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
