package ai.foresignal.source.adapters.cdc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;

/**
 * A small Socrata (SODA 2.0) reader shared by the CDC NWSS sources (prompt 095).
 *
 * <p>A run reads pages of {@code GET https://data.cdc.gov/resource/{id}.json?$where=date_updated >= '{cursor}'&
 * $order=date_updated ASC, :id ASC&$limit={limit}&$offset={skip + n × limit}}, passing the optional
 * {@code X-App-Token} header (anonymous requests share an IP pool and are throttled with 429s).
 *
 * <p><strong>Resumable cursor</strong> (2026-10 review). A run reads at most the pages its caller allows (the
 * adapter's request budget), so a backlog larger than that is read over several runs. NWSS publishes weekly, and
 * one update gives thousands of rows the same {@code date_updated}; a cursor of only the newest timestamp with
 * {@code >} would skip the rest of that timestamp's rows after a page boundary. The cursor is therefore the pair
 * ({@code date_updated}, rows already read at that timestamp): the query uses {@code >=}, a stable
 * {@code date_updated, :id} order, and an offset that skips the rows already read.
 *
 * <p>Not a Spring bean and not an adapter: it holds no state and is constructed with the base URL, so it is not
 * discovered as a source.
 */
public final class SodaClient {

    /** The page size. */
    public static final int LIMIT = 5000;

    /** The most pages one read sends, whatever its caller allows ({@value #LIMIT} × {@value #MAX_PAGES} rows). */
    public static final int MAX_PAGES = 20;

    /** The suffix of the state key holding the rows already read at the cursor's timestamp. */
    static final String SKIP_SUFFIX = ":skip";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final String baseUrl;

    /**
     * Where a dataset's reading resumes.
     *
     * @param updated the newest {@code date_updated} read, verbatim, or null before the first read
     * @param skip the rows with exactly that {@code date_updated} already read
     */
    public record Cursor(String updated, long skip) {

        /** No rows read yet. */
        public static final Cursor START = new Cursor(null, 0);

        /** Normalizes a negative skip to 0. */
        public Cursor {
            skip = Math.max(0, skip);
            updated = updated == null || updated.isBlank() ? null : updated;
            if (updated == null) {
                skip = 0;
            }
        }

        /** The cursor stored under {@code key} (and {@code key + ":skip"}). */
        public static Cursor load(AdapterStateStore state, String key) {
            String updated = state.get(key).orElse(null);
            long skip = 0;
            try {
                skip = Long.parseLong(state.get(key + SKIP_SUFFIX).orElse("0").strip());
            } catch (NumberFormatException e) {
                skip = 0;
            }
            return new Cursor(updated, skip);
        }

        /** Stores this cursor under {@code key} (and {@code key + ":skip"}). */
        public void store(AdapterStateStore state, String key) {
            if (updated == null) {
                return;
            }
            state.put(key, updated);
            state.put(key + SKIP_SUFFIX, Long.toString(skip));
        }

        /** The cursor after reading {@code row}, which the stable order puts after every row already read. */
        Cursor after(JsonNode row) {
            String raw = CdcText.text(row.get("date_updated"));
            if (raw == null) {
                return this;
            }
            if (updated == null || compare(raw, updated) > 0) {
                return new Cursor(raw, 1);
            }
            if (raw.equals(updated)) {
                return new Cursor(updated, skip + 1);
            }
            return this;
        }
    }

    /**
     * What one read produced.
     *
     * @param rows the rows read, in ascending {@code date_updated, :id} order
     * @param next the cursor after the last row read
     * @param exhausted true when the last page was short, so nothing newer was left
     * @param requests the requests sent
     */
    public record Batch(JsonNode rows, Cursor next, boolean exhausted, int requests) {}

    /** Creates the reader. */
    public SodaClient(String baseUrl) {
        String chosen = baseUrl == null || baseUrl.isBlank() ? "" : baseUrl.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    /** The URL of page {@code page} after {@code cursor}. */
    public URI uri(String datasetId, Cursor cursor, int page) {
        StringBuilder url = new StringBuilder(baseUrl).append("/resource/").append(datasetId).append(".json?");
        if (cursor.updated() != null) {
            url.append("$where=").append(enc("date_updated >= '" + cursor.updated() + "'")).append("&");
        }
        url.append("$order=").append(enc("date_updated ASC, :id ASC"))
                .append("&$limit=").append(LIMIT)
                .append("&$offset=").append(cursor.skip() + (long) page * LIMIT);
        return URI.create(url.toString());
    }

    /**
     * Reads up to {@code maxPages} pages after {@code cursor}.
     *
     * @param datasetId the Socrata dataset id
     * @param cursor where the previous read stopped
     * @param appToken the optional {@code X-App-Token}, or null
     * @param maxPages the most requests to send (capped at {@value #MAX_PAGES}, at least 1)
     * @return the rows, the cursor after them and whether the dataset is read to its end
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException when a body is truncated or not a JSON array
     */
    public Batch read(FetchContext ctx, String datasetId, Cursor cursor, String appToken, int maxPages)
            throws AdapterException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        if (appToken != null && !appToken.isBlank()) {
            headers.put("X-App-Token", appToken.strip());
        }
        int pages = Math.max(1, Math.min(MAX_PAGES, maxPages));
        ArrayNode rows = JSON.createArrayNode();
        Cursor next = cursor;
        boolean exhausted = false;
        int requests = 0;
        for (int page = 0; page < pages; page++) {
            UpstreamResponse response = ctx.http().get(uri(datasetId, cursor, page), headers);
            requests++;
            if (response.truncated()) {
                throw new UpstreamFormatException("SODA dataset " + datasetId + " page exceeded maxResponseBytes",
                        null);
            }
            JsonNode array = array(response.body(), datasetId);
            for (JsonNode row : array) {
                rows.add(row);
                if (row != null && row.isObject()) {
                    next = next.after(row);
                }
            }
            if (array.size() < LIMIT) {
                exhausted = true;
                break;
            }
        }
        return new Batch(rows, next, exhausted, requests);
    }

    static JsonNode array(byte[] body, String datasetId) {
        if (body == null || body.length == 0) {
            // An empty body is a valid empty page (SODA sends [] but an empty body means the same).
            return JSON.readTree("[]");
        }
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("SODA dataset " + datasetId + " is not valid JSON: "
                    + e.getOriginalMessage(), null, e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("SODA dataset " + datasetId + " did not return a JSON array", null);
        }
        return root;
    }

    /** Orders two Socrata floating timestamps by instant, falling back to their text. */
    static int compare(String a, String b) {
        Instant ia = CdcDates.instant(a);
        Instant ib = CdcDates.instant(b);
        if (ia != null && ib != null) {
            int byInstant = ia.compareTo(ib);
            return byInstant != 0 ? byInstant : a.compareTo(b);
        }
        return a.compareTo(b);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
