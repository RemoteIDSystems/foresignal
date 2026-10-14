package ai.foresignal.source.kit.feature;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * ArcGIS REST "Query (Feature Service/Layer)" for FeatureServer and MapServer layers (prompt 073). Stateless: the
 * only memory is the source's {@link AdapterStateStore}.
 *
 * <p><strong>Layer metadata</strong> ({@code {layerUrl}?f=json}): {@code maxRecordCount},
 * {@code advancedQueryCapabilities.supportsPagination}, {@code objectIdField}, the field list and
 * {@code editingInfo.dataLastEditDate} (else {@code lastEditDate}) are cached under {@value #META_KEY} and read at
 * most once per 24 h. A layer that publishes {@code editingInfo} has its document re-read on every run instead,
 * because that is the change probe: a cached edit date would hide a day of edits.
 *
 * <p><strong>Change short-circuit:</strong> when the edit date equals the one of the last complete fetch
 * ({@value #LAST_EDIT_KEY}), nothing is queried and the fetch is {@linkplain FeatureFetch#unchangedLayer() unchanged}.
 * Many services (NOAA MapServers among them) publish no {@code editingInfo}; those are always queried.
 *
 * <p><strong>Query:</strong> {@code {layerUrl}/query?where=…&outFields=…&returnGeometry=true&outSR=4326&f=geojson
 * &resultOffset=N&resultRecordCount=min(pageSize, maxRecordCount)&orderByFields=<objectIdField>}, plus
 * {@code maxAllowableOffset} and {@code geometryPrecision} when the spec sets them. Pages follow while
 * {@code exceededTransferLimit} is true (at the top level or under {@code properties}) and the page is non-empty, up
 * to {@code maxPages} and {@code maxFeatures}; a cut-short fetch is incomplete. A layer without pagination support is
 * queried once. Date fields arrive as epoch milliseconds. {@code format: esrijson} requests {@code f=json} and
 * converts with {@link EsriJsonConverter}.
 */
public final class ArcGisQueryClient {

    /** The state key of the cached layer metadata (JSON). */
    public static final String META_KEY = "arcgis:meta";

    /** The state key of the edit date of the last complete fetch. */
    public static final String LAST_EDIT_KEY = "arcgis:lastEdit";

    /** How long the layer metadata is reused. */
    public static final Duration META_TTL = Duration.ofHours(24);

    /** The object id field when the metadata names none. */
    public static final String DEFAULT_OBJECT_ID_FIELD = "OBJECTID";

    /**
     * One query.
     *
     * @param layerUrl the layer URL ({@code …/FeatureServer/0})
     * @param where the {@code where} clause
     * @param outFields the attributes requested
     * @param pageSize the page size wanted
     * @param maxPages the page cap
     * @param maxFeatures the feature cap
     * @param maxAllowableOffset generalization, or null
     * @param geometryPrecision decimal places, or null
     * @param esriJson true to request {@code f=json} and convert
     * @param headers extra request headers
     * @param extraQuery extra query parameters (a query credential)
     */
    public record Request(URI layerUrl, String where, List<String> outFields, int pageSize, int maxPages,
            int maxFeatures, Double maxAllowableOffset, Integer geometryPrecision, boolean esriJson,
            Map<String, String> headers, Map<String, String> extraQuery) {

        /** Null collections become empty ones. */
        public Request {
            outFields = outFields == null ? List.of() : List.copyOf(outFields);
            headers = headers == null ? Map.of() : Map.copyOf(headers);
            extraQuery = extraQuery == null ? Map.of() : Map.copyOf(extraQuery);
        }
    }

    /**
     * The cached layer metadata.
     *
     * @param fetchedAt when it was read
     * @param maxRecordCount the server's page ceiling
     * @param supportsPagination true when {@code resultOffset} works
     * @param objectIdField the object id field
     * @param lastEditDate the data edit date, epoch ms, or null
     * @param fields the field names and Esri types
     */
    public record LayerMeta(Instant fetchedAt, int maxRecordCount, boolean supportsPagination, String objectIdField,
            Long lastEditDate, Map<String, String> fields) {}

    /**
     * Fetches the layer.
     *
     * @param ctx the fetch context (network, state, clock)
     * @param request the query
     * @return the features, or an unchanged result
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a redirect, a non-200 or truncated body, a document that is not JSON or an
     *     ArcGIS error body
     */
    public FeatureFetch fetch(FetchContext ctx, Request request) throws AdapterException {
        Instant now = ctx.now();
        AdapterStateStore state = ctx.state();
        LayerMeta meta = cached(state);
        if (meta == null || meta.lastEditDate() != null || meta.fetchedAt().plus(META_TTL).isBefore(now)) {
            meta = readMeta(ctx, request, now);
            state.put(META_KEY, encode(meta));
        }
        if (meta.lastEditDate() != null
                && String.valueOf(meta.lastEditDate()).equals(state.get(LAST_EDIT_KEY).orElse(null))) {
            return FeatureFetch.unchangedLayer();
        }
        FeatureFetch fetch = query(ctx, request, meta);
        if (fetch.complete() && meta.lastEditDate() != null) {
            state.put(LAST_EDIT_KEY, String.valueOf(meta.lastEditDate()));
        }
        return fetch;
    }

    /** Pages through the query. */
    private FeatureFetch query(FetchContext ctx, Request request, LayerMeta meta) throws AdapterException {
        int pageSize = Math.max(1, Math.min(request.pageSize(), meta.maxRecordCount()));
        List<JsonNode> features = new ArrayList<>();
        int offset = 0;
        for (int page = 1; page <= request.maxPages(); page++) {
            Map<String, Object> params = FeatureHttp.params();
            params.put("where", request.where() == null || request.where().isBlank() ? "1=1" : request.where());
            params.put("outFields", request.outFields().isEmpty() ? "*" : String.join(",", request.outFields()));
            params.put("returnGeometry", "true");
            params.put("outSR", "4326");
            params.put("f", request.esriJson() ? "json" : "geojson");
            if (meta.supportsPagination()) {
                params.put("resultOffset", offset);
                params.put("resultRecordCount", pageSize);
            }
            params.put("orderByFields", meta.objectIdField());
            params.put("maxAllowableOffset", request.maxAllowableOffset());
            params.put("geometryPrecision", request.geometryPrecision());
            params.putAll(request.extraQuery());
            URI uri = FeatureHttp.uri(stripSlash(request.layerUrl().toString()) + "/query", params);
            JsonNode root = FeatureHttp.get(ctx, uri, request.headers(), "ArcGIS query page " + page).root();
            List<JsonNode> pageFeatures = request.esriJson() ? EsriJsonConverter.features(root) : geoJson(root);
            boolean exceeded = exceeded(root);
            for (JsonNode feature : pageFeatures) {
                if (features.size() >= request.maxFeatures()) {
                    return new FeatureFetch(features, false, false, page, "max-features");
                }
                features.add(feature);
            }
            if (pageFeatures.isEmpty()) {
                return new FeatureFetch(features, !exceeded, false, page, "empty-page");
            }
            if (!exceeded) {
                return new FeatureFetch(features, true, false, page, "last-page");
            }
            if (!meta.supportsPagination()) {
                return new FeatureFetch(features, false, false, page, "no-pagination");
            }
            offset += pageFeatures.size();
        }
        return new FeatureFetch(features, false, false, request.maxPages(), "max-pages");
    }

    /** True when the page says more features exist. */
    static boolean exceeded(JsonNode root) {
        JsonNode top = root.get("exceededTransferLimit");
        if (top != null && top.isBoolean() && top.booleanValue()) {
            return true;
        }
        JsonNode properties = root.get("properties");
        if (properties != null && properties.isObject()) {
            JsonNode nested = properties.get("exceededTransferLimit");
            return nested != null && nested.isBoolean() && nested.booleanValue();
        }
        return false;
    }

    /** The features of a GeoJSON page. */
    static List<JsonNode> geoJson(JsonNode root) {
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("ArcGIS query page has no features array", "");
        }
        List<JsonNode> out = new ArrayList<>(features.size());
        features.forEach(out::add);
        return out;
    }

    private static LayerMeta readMeta(FetchContext ctx, Request request, Instant now) throws AdapterException {
        Map<String, Object> params = FeatureHttp.params();
        params.put("f", "json");
        params.putAll(request.extraQuery());
        URI uri = FeatureHttp.uri(stripSlash(request.layerUrl().toString()), params);
        JsonNode root = FeatureHttp.get(ctx, uri, request.headers(), "ArcGIS layer metadata").root();
        return meta(root, now);
    }

    /**
     * The metadata of a layer document.
     *
     * @param root the {@code ?f=json} document
     * @param now when it was read
     */
    static LayerMeta meta(JsonNode root, Instant now) {
        int maxRecordCount = root.has("maxRecordCount") && root.get("maxRecordCount").isNumber()
                ? Math.max(1, root.get("maxRecordCount").intValue()) : 1000;
        JsonNode advanced = root.get("advancedQueryCapabilities");
        boolean pagination = advanced != null && advanced.has("supportsPagination")
                && advanced.get("supportsPagination").asBoolean();
        String objectIdField = root.has("objectIdField") && root.get("objectIdField").isString()
                ? root.get("objectIdField").stringValue() : DEFAULT_OBJECT_ID_FIELD;
        Long lastEdit = null;
        JsonNode editing = root.get("editingInfo");
        if (editing != null && editing.isObject()) {
            JsonNode data = editing.get("dataLastEditDate");
            JsonNode any = editing.get("lastEditDate");
            if (data != null && data.isNumber()) {
                lastEdit = data.longValue();
            } else if (any != null && any.isNumber()) {
                lastEdit = any.longValue();
            }
        }
        Map<String, String> fields = new java.util.LinkedHashMap<>();
        JsonNode list = root.get("fields");
        if (list != null && list.isArray()) {
            for (JsonNode f : list) {
                if (f.has("name") && f.get("name").isString()) {
                    fields.put(f.get("name").stringValue(), f.has("type") ? f.get("type").asString() : "");
                }
            }
        }
        return new LayerMeta(now, maxRecordCount, pagination, objectIdField, lastEdit, fields);
    }

    /**
     * The layer metadata the last {@link #fetch} stored in {@code state} (prompt 088: a hand-written adapter reads the
     * layer's edit date as the publication time of a catalog).
     *
     * @param state the state the fetch used (scoped when the fetch context was)
     * @return the metadata, or null when none is stored or it cannot be read
     */
    public static LayerMeta cachedMeta(AdapterStateStore state) {
        return cached(state);
    }

    /** The cached metadata, or null when none is stored or it cannot be read. */
    static LayerMeta cached(AdapterStateStore state) {
        String text = state.get(META_KEY).orElse(null);
        if (text == null) {
            return null;
        }
        try {
            JsonNode node = FeatureHttp.JSON.readTree(text);
            Map<String, String> fields = new java.util.LinkedHashMap<>();
            JsonNode list = node.get("fields");
            if (list != null && list.isObject()) {
                list.properties().forEach(e -> fields.put(e.getKey(), e.getValue().asString()));
            }
            JsonNode lastEdit = node.get("lastEditDate");
            return new LayerMeta(Instant.parse(node.get("fetchedAt").asString()), node.get("maxRecordCount").intValue(),
                    node.get("supportsPagination").booleanValue(), node.get("objectIdField").asString(),
                    lastEdit == null || lastEdit.isNull() ? null : lastEdit.longValue(), fields);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** {@code meta} as the JSON stored under {@value #META_KEY}. */
    static String encode(LayerMeta meta) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("fetchedAt", meta.fetchedAt().toString());
        node.put("maxRecordCount", meta.maxRecordCount());
        node.put("supportsPagination", meta.supportsPagination());
        node.put("objectIdField", meta.objectIdField());
        if (meta.lastEditDate() == null) {
            node.putNull("lastEditDate");
        } else {
            node.put("lastEditDate", meta.lastEditDate());
        }
        ObjectNode fields = JsonNodeFactory.instance.objectNode();
        meta.fields().forEach(fields::put);
        node.set("fields", fields);
        return node.toString();
    }

    private static String stripSlash(String url) {
        String out = url;
        while (out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }
}
