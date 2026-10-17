package ai.foresignal.baserate.reference;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.InboxFiles;
import ai.foresignal.source.adapters.ucdp.UcdpGedAdapter;
import ai.foresignal.source.formats.csv.CsvReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Imports UCDP GED history (prompt 121) as weekly counts, license {@code CC-BY-4.0}.
 *
 * <ul>
 *   <li><strong>File:</strong> the GED CSV the operator downloaded from {@code https://ucdp.uu.se/downloads/} (for
 *       example {@code ged261-csv.zip}, verified 2026-10-17, or the CSV inside it), placed under
 *       {@code ${FORESIGNAL_DATA_DIR}/imports} and named by {@code params.file} relative to it. The path is re-checked
 *       like an {@code INBOX_PATH} (no escape, no symbolic link). The columns read are those of the GED 26.1 header
 *       ({@code type_of_violence}, {@code country}, {@code adm_1}, {@code latitude}, {@code longitude},
 *       {@code date_start}, {@code best}). The whole file is one chunk, keyed by its SHA-256.
 *   <li><strong>API:</strong> {@code params.useApi} with {@code params.version} (the GED release, e.g. {@code 26.1}),
 *       through the {@code ucdp-ged} source's own budget and token, one year chunk at a time.
 * </ul>
 *
 * <p>{@code type_of_violence} 1 (state-based) and 2 (non-state) map to {@code conflict:battles}, 3 (one-sided) to
 * {@code conflict:violence-civilians}. The region is the country named in {@code country} (GED's {@code country_id}
 * is a Gleditsch-Ward code, not ISO, so the name is used; former names in parentheses are tried too), else the point,
 * else {@code ZZ}; when {@code adm_1} matches a 040 admin-1 the event is also counted there. Fatalities are
 * {@code best}. Each event is bucketed into the ISO week of {@code date_start}.
 */
@Component
public class UcdpGedImporter implements ReferenceImporter {

    /** The dataset. */
    public static final String DATASET = "ucdp-ged";

    /** The first year of GED. */
    public static final int FIRST_YEAR = 1989;

    /** The imports directory under the data directory. */
    public static final String IMPORTS_DIR = "imports";

    private final ReferenceRegionResolver regions;
    private final ObjectProvider<ReferenceHttp> http;
    private final ObjectProvider<CredentialsAccess> credentials;
    private final ObjectProvider<UcdpGedAdapter> adapter;
    private final Path importsDir;
    private final JsonMapper json;

    /** Creates the importer. */
    public UcdpGedImporter(ReferenceRegionResolver regions, ObjectProvider<ReferenceHttp> http,
            ObjectProvider<CredentialsAccess> credentials, ObjectProvider<UcdpGedAdapter> adapter,
            ForesignalProperties properties, JsonMapper json) {
        this.regions = regions;
        this.http = http;
        this.credentials = credentials;
        this.adapter = adapter;
        this.importsDir = properties.dataDir().resolve(IMPORTS_DIR).toAbsolutePath().normalize();
        this.json = json;
    }

    @Override
    public String dataset() {
        return DATASET;
    }

    @Override
    public String source() {
        return ReferenceFamilies.UCDP;
    }

    /** The imports directory. */
    public Path importsDir() {
        return importsDir;
    }

    @Override
    public Map<String, Object> normalize(Map<String, Object> params) {
        Map<String, Object> out = new LinkedHashMap<>();
        boolean useApi = ImportParams.bool(params, "useApi", false);
        String file = ImportParams.string(params, "file");
        if (useApi) {
            String version = ImportParams.string(params, "version");
            if (version == null || !version.matches("^[0-9]{2}\\.[0-9]{1,2}(\\.[0-9]{1,2})?$")) {
                throw ValidationException.forField("params.version", "the GED release (for example 26.1) is required"
                        + " with useApi");
            }
            int[] years = ImportParams.years(params, FIRST_YEAR);
            out.put("useApi", true);
            out.put("version", version);
            out.put("yearFrom", years[0]);
            out.put("yearTo", years[1]);
            return out;
        }
        if (file == null) {
            throw ValidationException.forField("params.file", "is required (a CSV or ZIP under"
                    + " ${FORESIGNAL_DATA_DIR}/imports), unless useApi is true");
        }
        if (file.startsWith("/") || file.contains("..") || file.contains("\\") || file.length() > 300) {
            throw ValidationException.forField("params.file", "must be a relative path inside the imports directory");
        }
        out.put("file", file);
        if (params != null && (params.get("yearFrom") != null || params.get("yearTo") != null)) {
            int[] years = ImportParams.years(params, FIRST_YEAR);
            out.put("yearFrom", years[0]);
            out.put("yearTo", years[1]);
        }
        return out;
    }

    @Override
    public Availability availability(Map<String, Object> params) {
        if (Boolean.TRUE.equals(params.get("useApi"))) {
            CredentialsAccess access = credentials.getIfAvailable();
            String token = access == null ? null : access.forSource(UcdpGedAdapter.ID).get(UcdpGedAdapter.TOKEN);
            if (token == null || token.isBlank()) {
                return Availability.unavailable("the UCDP API needs the ucdp-ged source's token credential");
            }
            return Availability.available();
        }
        try {
            Path path = resolve(params);
            InboxFiles.rootedAt(importsDir).stat(path);
            return Availability.available();
        } catch (IOException | RuntimeException e) {
            return Availability.unavailable("no readable file '" + params.get("file") + "' under " + importsDir);
        }
    }

    @Override
    public LocalDate[] period(Map<String, Object> params) {
        if (params.get("yearFrom") == null) {
            return null;
        }
        int from = ((Number) params.get("yearFrom")).intValue();
        int to = ((Number) params.get("yearTo")).intValue();
        return new LocalDate[] {ReferenceFamilies.yearChunk(from)[0], ReferenceFamilies.yearChunk(to)[1]};
    }

    @Override
    public void run(ImportRun run) throws Exception {
        if (Boolean.TRUE.equals(run.params().get("useApi"))) {
            runApi(run);
        } else {
            runFile(run);
        }
    }

    private Path resolve(Map<String, Object> params) {
        return importsDir.resolve(String.valueOf(params.get("file"))).normalize();
    }

    /** Reads the file (CSV, or the first CSV inside a ZIP) as one chunk. */
    void runFile(ImportRun run) throws IOException {
        Path path = resolve(run.params());
        InboxFiles files = InboxFiles.rootedAt(importsDir);
        long size = Math.max(1, files.stat(path).size());
        int[] years = years(run.params());
        WeeklyCounts counts = new WeeklyCounts();
        LocalDate[] span = new LocalDate[2];
        long read = 0;
        MessageDigest digest = sha256();
        try (InputStream raw = files.open(path); CountingStream counting = new CountingStream(raw);
                DigestInputStream in = new DigestInputStream(counting, digest)) {
            InputStream csv = in;
            ZipInputStream zip = null;
            if (path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                zip = new ZipInputStream(in);
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
                        break;
                    }
                }
                if (entry == null) {
                    throw new IOException("the ZIP holds no CSV file");
                }
                csv = zip;
            }
            CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8)));
            List<String> header = reader.next();
            if (header == null) {
                throw new IOException("the file is empty");
            }
            Map<String, Integer> index = CsvReader.index(header);
            for (String column : List.of("TYPE_OF_VIOLENCE", "DATE_START", "COUNTRY")) {
                if (!index.containsKey(column)) {
                    throw new IOException("the file is not a GED CSV: no " + column.toLowerCase(Locale.ROOT)
                            + " column");
                }
            }
            List<String> row;
            while ((row = reader.next()) != null) {
                if (row.size() <= 1) {
                    continue;
                }
                read++;
                Map<String, String> fields = new LinkedHashMap<>();
                for (String column : List.of("TYPE_OF_VIOLENCE", "DATE_START", "COUNTRY", "ADM_1", "LATITUDE",
                        "LONGITUDE", "BEST")) {
                    fields.put(column.toLowerCase(Locale.ROOT), CsvReader.get(row, index, column));
                }
                accept(fields, counts, years, span);
                if (read % 10_000 == 0) {
                    run.checkpoint(0.95 * counting.count() / size);
                }
            }
            // Drain what is left so the digest covers the whole file.
            in.transferTo(java.io.OutputStream.nullOutputStream());
        }
        String sha = HexFormat.of().formatHex(digest.digest());
        run.fileSha256(sha);
        if (span[0] == null) {
            throw new IOException("the file holds no GED event in the requested years (" + read + " rows read)");
        }
        LocalDate from = ReferenceFamilies.weekStart(span[0]);
        LocalDate to = ReferenceFamilies.weekStart(span[1]).plusWeeks(1);
        if (years != null) {
            from = ReferenceFamilies.yearChunk(years[0])[0];
            to = ReferenceFamilies.yearChunk(years[1])[1];
        }
        run.writeChunk("file:" + sha, from, to, List.of(), counts, read);
        run.checkpoint(1);
    }

    /** Reads the API one year chunk at a time. */
    void runApi(ImportRun run) throws Exception {
        String version = String.valueOf(run.params().get("version"));
        int[] years = years(run.params());
        UcdpGedAdapter ged = adapter.getObject();
        CredentialsAccess access = credentials.getObject();
        String token = access.forSource(UcdpGedAdapter.ID).get(UcdpGedAdapter.TOKEN);
        Map<String, String> headers = Map.of(UcdpGedAdapter.TOKEN_HEADER, token == null ? "" : token);
        ReferenceHttp.Fetcher fetcher = http.getObject().source(UcdpGedAdapter.ID);
        int total = years[1] - years[0] + 1;
        for (int year = years[0]; year <= years[1]; year++) {
            String chunk = Integer.toString(year);
            run.checkpoint((double) (year - years[0]) / total);
            if (run.done(chunk)) {
                continue;
            }
            LocalDate[] range = ReferenceFamilies.yearChunk(year);
            String query = "pagesize=" + UcdpGedAdapter.PAGE_SIZE + "&StartDate=" + range[0] + "&EndDate="
                    + range[1].minusDays(1);
            WeeklyCounts counts = new WeeklyCounts();
            long read = 0;
            int page = 0;
            Integer total0 = null;
            Integer firstIndex = null;
            while (true) {
                String q = page == 0 ? query : query + "&page=" + (firstIndex + page);
                URI uri = ged.eventsUri(version, q);
                ReferenceHttp.Response response = fetcher.get(uri, headers);
                if (response.status() == 401 || response.status() == 403) {
                    throw new IllegalStateException("UCDP refused the token (HTTP " + response.status() + ")");
                }
                if (response.status() != 200) {
                    throw new IllegalStateException("UCDP answered HTTP " + response.status() + " for " + year);
                }
                JsonNode root = json.readTree(response.body());
                JsonNode result = root.get("Result");
                if (result == null || !result.isArray()) {
                    throw new IllegalStateException("UCDP gedevents: no Result array");
                }
                for (JsonNode event : result) {
                    read++;
                    Map<String, String> fields = new LinkedHashMap<>();
                    for (String column : List.of("type_of_violence", "date_start", "country", "adm_1", "latitude",
                            "longitude", "best")) {
                        JsonNode v = event.get(column);
                        fields.put(column, v == null || v.isNull() ? null : v.asString());
                    }
                    LocalDate[] span = new LocalDate[2];
                    accept(fields, counts, null, span, range[0]);
                }
                if (page == 0) {
                    total0 = root.hasNonNull("TotalPages") ? root.get("TotalPages").asInt() : 1;
                    firstIndex = nextPage(root.hasNonNull("NextPageUrl") ? root.get("NextPageUrl").asString() : null);
                    firstIndex = firstIndex == null ? 1 : firstIndex - 1;
                }
                page++;
                if (page >= total0) {
                    break;
                }
            }
            run.writeChunk(chunk, range[0], range[1], List.of(), counts, read);
        }
        run.checkpoint(1);
    }

    /** The page number in a {@code NextPageUrl}, or null. */
    static Integer nextPage(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            String query = URI.create(url).getRawQuery();
            if (query == null) {
                return null;
            }
            for (String part : query.split("&")) {
                int eq = part.indexOf('=');
                if (eq > 0 && part.substring(0, eq).equalsIgnoreCase("page")) {
                    return Integer.parseInt(URLDecoder.decode(part.substring(eq + 1), StandardCharsets.UTF_8));
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    private static int[] years(Map<String, Object> params) {
        if (params.get("yearFrom") == null) {
            return null;
        }
        return new int[] {((Number) params.get("yearFrom")).intValue(), ((Number) params.get("yearTo")).intValue()};
    }

    /** Counts one GED event (file rows and API events share the column names). */
    void accept(Map<String, String> fields, WeeklyCounts counts, int[] years, LocalDate[] span) {
        accept(fields, counts, years, span, null);
    }

    private void accept(Map<String, String> fields, WeeklyCounts counts, int[] years, LocalDate[] span,
            LocalDate clampFrom) {
        String family = ReferenceFamilies.ucdpFamily(integer(fields.get("type_of_violence")));
        LocalDate day = date(fields.get("date_start"));
        if (family == null || day == null) {
            return;
        }
        if (years != null && (day.getYear() < years[0] || day.getYear() > years[1])) {
            return;
        }
        if (clampFrom != null && day.isBefore(clampFrom)) {
            day = clampFrom;
        }
        String country = regions.countryByName(fields.get("country"));
        if (country == null) {
            Double lat = number(fields.get("latitude"));
            Double lon = number(fields.get("longitude"));
            country = lat == null || lon == null ? ReferenceRegionResolver.UNKNOWN : regions.countryAt(lon, lat);
        }
        Long fatalities = longValue(fields.get("best"));
        counts.add(family, country, day, fatalities);
        if (!ReferenceRegionResolver.UNKNOWN.equals(country)) {
            String admin1 = regions.admin1(country, fields.get("adm_1"));
            if (admin1 != null) {
                counts.add(family, admin1, day, fatalities);
            }
        }
        if (span[0] == null || day.isBefore(span[0])) {
            span[0] = day;
        }
        if (span[1] == null || day.isAfter(span[1])) {
            span[1] = day;
        }
    }

    private static Integer integer(String text) {
        Long v = longValue(text);
        return v == null ? null : v.intValue();
    }

    private static Long longValue(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return (long) Double.parseDouble(text.strip());
        } catch (NumberFormatException e) {
            return null;
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

    /** The day of a GED date ({@code 1992-03-17 00:00:00.000} or {@code 1992-03-17}), or null. */
    static LocalDate date(String text) {
        if (text == null || text.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(text.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** Counts the bytes read, for progress. */
    private static final class CountingStream extends java.io.FilterInputStream {

        private long count;

        CountingStream(InputStream in) {
            super(in);
        }

        long count() {
            return count;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                count++;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0) {
                count += n;
            }
            return n;
        }
    }
}
