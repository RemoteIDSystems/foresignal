package ai.foresignal.source.adapters.cdc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A small Socrata (SODA 2.0) reader shared by the CDC NWSS sources (prompt 095).
 *
 * <p>A run reads pages of {@code GET https://data.cdc.gov/resource/{id}.json?$where=date_updated > '{cursor}'&
 * $order=date_updated ASC&$limit={limit}&$offset={offset}}, passing the optional {@code X-App-Token} header
 * (anonymous requests share an IP pool and are throttled with 429s). It stops at the first short page or the
 * page cap and returns the raw rows; the adapter computes the new cursor as the largest {@code date_updated}.
 *
 * <p>Not a Spring bean and not an adapter: it holds no state and is constructed with the base URL, so it is not
 * discovered as a source.
 */
public final class SodaClient {

    /** The page size. */
    public static final int LIMIT = 5000;

    /** The most pages one run reads ({@value #LIMIT} × {@value #MAX_PAGES} rows). */
    public static final int MAX_PAGES = 20;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final String baseUrl;

    /** Creates the reader. */
    public SodaClient(String baseUrl) {
        String chosen = baseUrl == null || baseUrl.isBlank() ? "" : baseUrl.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    /** The page URL. */
    public URI uri(String datasetId, String cursor, int offset) {
        StringBuilder url = new StringBuilder(baseUrl).append("/resource/").append(datasetId).append(".json?");
        if (cursor != null && !cursor.isBlank()) {
            url.append("$where=").append(enc("date_updated > '" + cursor + "'")).append("&");
        }
        url.append("$order=").append(enc("date_updated ASC"))
                .append("&$limit=").append(LIMIT)
                .append("&$offset=").append(offset);
        return URI.create(url.toString());
    }

    /**
     * Reads every page newer than {@code cursor} and returns one array node holding every row, in ascending
     * {@code date_updated} order.
     *
     * @param datasetId the Socrata dataset id
     * @param cursor the largest {@code date_updated} already seen, or null
     * @param appToken the optional {@code X-App-Token}, or null
     * @return the rows as a JSON array
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException when a body is truncated or not a JSON array
     */
    public JsonNode read(FetchContext ctx, String datasetId, String cursor, String appToken)
            throws AdapterException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        if (appToken != null && !appToken.isBlank()) {
            headers.put("X-App-Token", appToken.strip());
        }
        var rows = JSON.createArrayNode();
        for (int page = 0; page < MAX_PAGES; page++) {
            UpstreamResponse response = ctx.http().get(uri(datasetId, cursor, page * LIMIT), headers);
            if (response.truncated()) {
                throw new UpstreamFormatException("SODA dataset " + datasetId + " page exceeded maxResponseBytes",
                        null);
            }
            JsonNode array = array(response.body(), datasetId);
            array.forEach(rows::add);
            if (array.size() < LIMIT) {
                break;
            }
        }
        return rows;
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

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
