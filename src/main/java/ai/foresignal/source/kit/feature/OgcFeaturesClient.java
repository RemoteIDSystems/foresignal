package ai.foresignal.source.kit.feature;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;

/**
 * OGC API – Features Part 1 (OGC 17-069r4) items paging (prompt 073). Stateless.
 *
 * <p>The first page is {@code {base}/collections/{id}/items?f=json&limit=…[&bbox=…][&datetime=…][&<filters>]}; later
 * pages follow the response's {@code links[rel=next]} whatever its query shape (offset, or USGS's {@code cursor=}),
 * resolved against the current page and kept on the same scheme, host and port. Paging stops on a missing
 * {@code next} or an empty page; {@code numberMatched} is ignored. {@code maxPages} and {@code maxFeatures} cut a
 * fetch short, which makes it incomplete.
 *
 * <p>Responses must be {@code application/geo+json} or {@code application/json} (any {@code +json} type is accepted;
 * a missing type is tolerated).
 */
public final class OgcFeaturesClient {

    /** The {@code Accept} header sent unless the spec sets one. */
    public static final String ACCEPT = "application/geo+json, application/json;q=0.9";

    /**
     * One collection query.
     *
     * @param base the API landing URL
     * @param collection the collection id
     * @param limit the page size
     * @param maxPages the page cap
     * @param maxFeatures the feature cap
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or empty
     * @param datetime the {@code datetime} parameter, or null
     * @param filters property filters
     * @param headers extra request headers
     * @param extraQuery extra query parameters (a query credential), added to every page
     */
    public record Request(URI base, String collection, int limit, int maxPages, int maxFeatures, List<Double> bbox,
            String datetime, Map<String, String> filters, Map<String, String> headers, Map<String, String> extraQuery) {

        /** Null collections become empty ones. */
        public Request {
            bbox = bbox == null ? List.of() : List.copyOf(bbox);
            filters = filters == null ? Map.of() : new LinkedHashMap<>(filters);
            headers = headers == null ? Map.of() : Map.copyOf(headers);
            extraQuery = extraQuery == null ? Map.of() : new LinkedHashMap<>(extraQuery);
        }
    }

    /**
     * Fetches the collection.
     *
     * @param ctx the fetch context
     * @param request the query
     * @return the features
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a redirect, a non-200 or truncated body, an unexpected content type, a body
     *     that is not a FeatureCollection, or a next link to another server
     */
    public FeatureFetch fetch(FetchContext ctx, Request request) throws AdapterException {
        return fetch(ctx, request, response -> { });
    }

    /**
     * Fetches the collection, handing every page's response to {@code onPage} before it is parsed (prompt 079:
     * {@code usgs-water} reads {@code X-RateLimit-Remaining}).
     *
     * @param ctx the fetch context
     * @param request the query
     * @param onPage sees each page's response, headers included
     * @return the features
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException as {@link #fetch(FetchContext, Request)}
     */
    public FeatureFetch fetch(FetchContext ctx, Request request, Consumer<UpstreamResponse> onPage)
            throws AdapterException {
        URI first = firstPage(request);
        Map<String, String> headers = new LinkedHashMap<>(request.headers());
        headers.putIfAbsent("Accept", ACCEPT);
        List<JsonNode> features = new ArrayList<>();
        URI page = first;
        for (int n = 1; n <= request.maxPages(); n++) {
            UpstreamResponse raw = ctx.http().get(page, headers);
            onPage.accept(raw);
            FeatureHttp.Page response = new FeatureHttp.Page(FeatureHttp.parse(raw, "OGC items page " + n), raw);
            checkType(response.response().contentType());
            JsonNode root = response.root();
            JsonNode list = root.get("features");
            if (list == null || !list.isArray()) {
                throw new UpstreamFormatException("OGC items page " + n + " has no features array", "");
            }
            for (JsonNode feature : list) {
                if (features.size() >= request.maxFeatures()) {
                    return new FeatureFetch(features, false, false, n, "max-features");
                }
                features.add(feature);
            }
            URI next = next(root, page);
            if (list.isEmpty()) {
                return new FeatureFetch(features, true, false, n, "empty-page");
            }
            if (next == null) {
                return new FeatureFetch(features, true, false, n, "last-page");
            }
            if (!KitUrls.sameOrigin(first, next)) {
                throw new UpstreamFormatException("OGC next link leaves the server (" + next.getHost() + ")", "");
            }
            page = withExtraQuery(next, request.extraQuery());
        }
        return new FeatureFetch(features, false, false, request.maxPages(), "max-pages");
    }

    /** The first page URL. */
    static URI firstPage(Request request) {
        String base = request.base().toString();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        Map<String, Object> params = FeatureHttp.params();
        params.put("f", "json");
        params.put("limit", request.limit());
        if (!request.bbox().isEmpty()) {
            params.put("bbox", request.bbox().stream().map(OgcFeaturesClient::number).collect(Collectors.joining(",")));
        }
        params.put("datetime", request.datetime());
        params.putAll(request.filters());
        params.putAll(request.extraQuery());
        return FeatureHttp.uri(base + "/collections/" + KitUrls.encode(request.collection()) + "/items", params);
    }

    /**
     * The {@code rel=next} link of a page, resolved against it, or null.
     *
     * @param root the page document
     * @param current the page URL
     */
    static URI next(JsonNode root, URI current) {
        JsonNode links = root.get("links");
        if (links == null || !links.isArray()) {
            return null;
        }
        for (JsonNode link : links) {
            JsonNode rel = link.get("rel");
            JsonNode href = link.get("href");
            if (rel == null || href == null || !"next".equals(rel.asString()) || !href.isString()) {
                continue;
            }
            JsonNode type = link.get("type");
            if (type != null && type.isString() && !type.stringValue().toLowerCase(Locale.ROOT).contains("json")) {
                continue;
            }
            try {
                return current.resolve(href.stringValue().strip());
            } catch (IllegalArgumentException e) {
                throw new UpstreamFormatException("OGC next link is not a URL", href.stringValue());
            }
        }
        return null;
    }

    /** Refuses a response that is not JSON. */
    static void checkType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return;
        }
        String type = contentType.toLowerCase(Locale.ROOT).split(";")[0].strip();
        if (!type.equals("application/geo+json") && !type.equals("application/json") && !type.endsWith("+json")) {
            throw new UpstreamFormatException("OGC items answered " + type + ", not GeoJSON", "");
        }
    }

    /** {@code next} with any missing extra parameter (a query credential) added. */
    private static URI withExtraQuery(URI next, Map<String, String> extra) {
        if (extra.isEmpty()) {
            return next;
        }
        String url = next.toString();
        String query = next.getRawQuery() == null ? "" : "&" + next.getRawQuery() + "&";
        for (Map.Entry<String, String> e : extra.entrySet()) {
            if (!query.contains("&" + KitUrls.encode(e.getKey()) + "=")) {
                url = KitUrls.withParam(url, e.getKey(), e.getValue());
            }
        }
        return URI.create(url);
    }

    private static String number(Double value) {
        return value == Math.rint(value) ? Long.toString(value.longValue()) : value.toString();
    }
}
