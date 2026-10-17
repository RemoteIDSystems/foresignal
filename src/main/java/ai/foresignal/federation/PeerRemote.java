package ai.foresignal.federation;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.HttpStream;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The client side of a peer's federation API (prompt 113), always through the budgeted {@link SourceHttp} of the
 * peer's source (SRC-1): {@code /v1/federation/info} with its JWS, the JWKS, change and snapshot pages, and the
 * SSE stream. The JWKS is always fetched from the peer's own base URL ({@code /.well-known/jwks.json}), never from
 * a host the {@code jwksUri} of the response names, so a peer cannot point this instance at a third host.
 */
public final class PeerRemote {

    /** The page size requested. */
    public static final int PAGE = 1_000;

    /** The signature header. */
    public static final String SIGNATURE_HEADER = "Foresignal-Signature";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final SourceHttp http;
    private final String baseUrl;
    private final String token;
    private final JsonMapper json;

    /**
     * Creates the client.
     *
     * @param http the peer source's budgeted HTTP client
     * @param baseUrl the peer's base URL (no trailing slash needed)
     * @param token the remote token (read + federation), or null
     */
    public PeerRemote(SourceHttp http, String baseUrl, String token, JsonMapper json) {
        this.http = http;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.token = token;
        this.json = json;
    }

    /**
     * {@code /v1/federation/info} as received.
     *
     * @param body the parsed document
     * @param raw the exact bytes the signature covers
     * @param signature the {@code Foresignal-Signature} header, or null
     */
    public record Info(Map<String, Object> body, byte[] raw, String signature) {

        /** The remote instance id, or null. */
        public String instanceId() {
            return body.get("instanceId") instanceof String id ? id : null;
        }
    }

    /** Fetches {@code /v1/federation/info}. */
    public Info info() throws AdapterException {
        UpstreamResponse response = http.get(URI.create(baseUrl + "/v1/federation/info"), headers(true));
        return new Info(parse(response.body()), response.body(), response.header(SIGNATURE_HEADER));
    }

    /** Fetches the peer's JWKS from its own base URL. */
    public Map<String, Object> jwks() throws AdapterException {
        return parse(http.get(URI.create(baseUrl + "/.well-known/jwks.json"), Map.of("Accept", "application/json"))
                .body());
    }

    /**
     * Fetches {@code /info} and verifies its JWS against the peer's JWKS.
     *
     * @return the document and the verification (not yet compared with the pinned key)
     */
    public Verified verifiedInfo() throws AdapterException {
        Info info = info();
        PeerKeys.Verification verification = PeerKeys.verifyWithJwks(info.raw(), info.signature(), jwks());
        return new Verified(info, verification);
    }

    /**
     * A fetched and checked {@code /info}.
     *
     * @param info the document
     * @param verification the JWS check against the peer's JWKS
     */
    public record Verified(Info info, PeerKeys.Verification verification) {}

    /** One page of {@code /v1/federation/changes} after {@code after}. */
    public JsonNode changes(long after, PeerSelection selection) throws AdapterException {
        return getJson(URI.create(baseUrl + "/v1/federation/changes?after=" + after + "&limit=" + PAGE
                + filters(selection)));
    }

    /** One page of {@code /v1/federation/snapshot}. */
    public JsonNode snapshot(Long atSeq, String cursor, PeerSelection selection) throws AdapterException {
        StringBuilder uri = new StringBuilder(baseUrl).append("/v1/federation/snapshot?limit=").append(PAGE);
        if (atSeq != null) {
            uri.append("&atSeq=").append(atSeq);
        }
        if (cursor != null) {
            uri.append("&cursor=").append(encode(cursor));
        }
        uri.append(filters(selection));
        return getJson(URI.create(uri.toString()));
    }

    /** Opens {@code /v1/federation/stream} from {@code after}. */
    public HttpStream stream(long after, PeerSelection selection) throws AdapterException {
        Map<String, String> headers = headers(true);
        headers.put("Accept", "text/event-stream");
        headers.put("Cache-Control", "no-cache");
        headers.put("Last-Event-ID", Long.toString(after));
        return http.openStream(URI.create(baseUrl + "/v1/federation/stream?after=" + after
                + (selection.live() ? "&live=true" : "") + filters(selection)), headers);
    }

    /** The query string of a selection's remote filters (sources, kinds, bbox), each prefixed with {@code &}. */
    static String filters(PeerSelection selection) {
        StringBuilder out = new StringBuilder();
        for (String source : selection.sources()) {
            out.append("&source=").append(encode(source));
        }
        for (String kind : selection.kinds()) {
            out.append("&kind=").append(encode(kind));
        }
        if (selection.bbox() != null) {
            StringJoiner box = new StringJoiner(",");
            for (Double value : selection.bbox()) {
                box.add(BigDecimalText.of(value));
            }
            out.append("&bbox=").append(encode(box.toString()));
        }
        return out.toString();
    }

    private JsonNode getJson(URI uri) throws AdapterException {
        UpstreamResponse response = http.get(uri, headers(true));
        try {
            return json.readTree(response.body());
        } catch (JacksonException e) {
            throw new AdapterException("the peer answered " + uri.getPath() + " with a body that is not JSON");
        }
    }

    private Map<String, String> headers(boolean authenticated) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        if (authenticated && token != null && !token.isBlank()) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }

    private Map<String, Object> parse(byte[] body) throws AdapterException {
        try {
            Map<String, Object> map = json.readValue(body, MAP);
            return map == null ? Map.of() : map;
        } catch (JacksonException e) {
            throw new AdapterException("the peer's answer is not a JSON object");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** Plain decimal text of a coordinate (no exponent). */
    static final class BigDecimalText {

        private BigDecimalText() {}

        static String of(Double value) {
            return value == null ? "0" : new java.math.BigDecimal(Double.toString(value)).stripTrailingZeros()
                    .toPlainString();
        }
    }

    /** The items array of a page, or an empty list. */
    public static List<JsonNode> items(JsonNode page) {
        JsonNode items = page == null ? null : page.get("items");
        List<JsonNode> out = new java.util.ArrayList<>();
        if (items != null && items.isArray()) {
            items.forEach(out::add);
        }
        return out;
    }
}
