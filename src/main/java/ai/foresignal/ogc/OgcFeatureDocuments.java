package ai.foresignal.ogc;

import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.RawJson;
import ai.foresignal.ogc.cql2.Cql2Queryables;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON documents of OGC API &ndash; Features (17-069r4) and Part 3 (19-079r2) for Foresignal's feature
 * collections (prompt 112): collection metadata, items, one feature and the queryables schema. JSON only; no
 * HTML is offered (Charter: no UI).
 */
public final class OgcFeatureDocuments {

    /** WGS 84 longitude/latitude, the only CRS offered. */
    public static final String CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    /** The Gregorian temporal reference system. */
    public static final String GREGORIAN = "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian";

    /** GeoJSON. */
    public static final String GEOJSON = "application/geo+json";

    /** JSON Schema. */
    public static final String SCHEMA_JSON = "application/schema+json";

    /** The conformance classes 112 implements (Features Part 1 and 3, CQL2); {@code oas30} is not claimed. */
    public static final List<String> CONFORMANCE = List.of(
            "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core",
            "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson",
            "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/filter",
            "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/features-filter",
            "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/queryables",
            "http://www.opengis.net/spec/cql2/1.0/conf/basic-cql2",
            "http://www.opengis.net/spec/cql2/1.0/conf/cql2-text",
            "http://www.opengis.net/spec/cql2/1.0/conf/cql2-json",
            "http://www.opengis.net/spec/cql2/1.0/conf/basic-spatial-functions");

    private OgcFeatureDocuments() {}

    /** The collection document of a feature collection. */
    public static Map<String, Object> collection(PublicUrls urls, OgcFeatureService.FeatureCollection c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.id());
        out.put("title", c.history() ? c.kind() + " (history)" : c.kind());
        out.put("description", c.history()
                ? "Every stored version of " + c.kind() + " records; items are versions."
                : "Current state of " + c.kind() + " records.");
        out.put("itemType", "feature");
        out.put("crs", List.of(CRS84));
        out.put("attribution", c.attribution());
        Map<String, Object> extent = new LinkedHashMap<>();
        if (c.bbox() != null) {
            extent.put("spatial", Map.of("bbox", List.of(Arrays.stream(c.bbox()).boxed().toList()), "crs", CRS84));
        }
        if (c.from() != null || c.to() != null) {
            List<String> interval = new ArrayList<>();
            interval.add(c.from() == null ? null : c.from().toString());
            interval.add(c.to() == null ? null : c.to().toString());
            extent.put("temporal", Map.of("interval", List.of(interval), "trs", GREGORIAN));
        }
        if (!extent.isEmpty()) {
            out.put("extent", extent);
        }
        String base = "/v1/ogc/collections/" + c.id();
        out.put("links", List.of(
                link(urls.api(base), "self", "application/json", "This collection"),
                link(urls.api(base + "/items"), "items", GEOJSON, "Items"),
                link(urls.api(base + "/queryables"), "http://www.opengis.net/def/rel/ogc/1.0/queryables",
                        SCHEMA_JSON, "Queryables")));
        return out;
    }

    /**
     * The items response: a GeoJSON FeatureCollection with links, counts and attributions.
     *
     * @param selfUrl the request's own URL
     * @param nextUrl the next page's URL, or null on the last page
     */
    public static Map<String, Object> items(PublicUrls urls, OgcFeatureService.FeatureCollection c,
            OgcFeatureService.ItemsPage page, String selfUrl, String nextUrl, Instant now) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "FeatureCollection");
        List<Map<String, Object>> features = new ArrayList<>();
        for (OgcFeatureService.Feature f : page.features()) {
            features.add(feature(urls, c, f, false));
        }
        out.put("features", features);
        List<Map<String, Object>> links = new ArrayList<>();
        links.add(link(selfUrl, "self", GEOJSON, "This page"));
        if (nextUrl != null) {
            links.add(link(nextUrl, "next", GEOJSON, "Next page"));
        }
        links.add(link(urls.api("/v1/ogc/collections/" + c.id()), "collection", "application/json", "The collection"));
        out.put("links", links);
        out.put("numberReturned", features.size());
        if (page.numberMatched() != null) {
            out.put("numberMatched", page.numberMatched());
        }
        out.put("timeStamp", now.toString());
        out.put("attributions", AttributionService.asMaps(page.attributions()));
        if (page.licenceFiltered() != null) {
            out.put("licenceFiltered", page.licenceFiltered());
        }
        return out;
    }

    /** One feature; {@code standalone} adds its {@code self} and {@code collection} links. */
    public static Map<String, Object> feature(PublicUrls urls, OgcFeatureService.FeatureCollection c,
            OgcFeatureService.Feature f, boolean standalone) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "Feature");
        out.put("id", f.id());
        out.put("geometry", f.geometry() == null ? null : new RawJson(f.geometry()));
        out.put("properties", f.properties());
        if (standalone) {
            String base = "/v1/ogc/collections/" + c.id();
            out.put("links", List.of(
                    link(urls.api(base + "/items/" + encode(f.id())), "self", GEOJSON, "This feature"),
                    link(urls.api(base), "collection", "application/json", "The collection")));
        }
        return out;
    }

    /** The queryables of a collection as a JSON Schema (2020-12), per Part 3 §6.2. */
    public static Map<String, Object> queryables(PublicUrls urls, OgcFeatureService.FeatureCollection c,
            Map<String, FieldSpec> specs) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("title", typed("string", "Title"));
        props.put("sourceId", typed("string", "Source id"));
        props.put("observedAt", timestamp("Observed time"));
        props.put("eventTime", timestamp("Event time"));
        props.put("version", typed("integer", "Record version"));
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("title", "Geometry");
        geometry.put("format", "geometry-any");
        geometry.put("x-ogc-role", "primary-geometry");
        props.put(Cql2Queryables.GEOMETRY, geometry);
        for (Map.Entry<String, FieldSpec> e : specs.entrySet()) {
            FieldSpec spec = e.getValue();
            Map<String, Object> p = schemaOf(spec.type());
            if (spec.label() != null) {
                p.put("title", spec.label());
            }
            if (spec.description() != null) {
                p.put("description", spec.description());
            }
            if (spec.unit() != null) {
                p.put("x-unit", spec.unit());
            }
            props.put(e.getKey(), p);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        out.put("$id", urls.api("/v1/ogc/collections/" + c.id() + "/queryables"));
        out.put("type", "object");
        out.put("title", c.id());
        out.put("properties", props);
        out.put("additionalProperties", false);
        return out;
    }

    private static Map<String, Object> schemaOf(FieldType type) {
        return switch (type) {
            case STRING -> typed("string", null);
            case INTEGER -> typed("integer", null);
            case NUMBER -> typed("number", null);
            case BOOLEAN -> typed("boolean", null);
            case TIMESTAMP -> timestamp(null);
            case OBJECT -> typed("object", null);
            case ARRAY -> typed("array", null);
        };
    }

    private static Map<String, Object> typed(String type, String title) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        if (title != null) {
            m.put("title", title);
        }
        return m;
    }

    private static Map<String, Object> timestamp(String title) {
        Map<String, Object> m = typed("string", title);
        m.put("format", "date-time");
        return m;
    }

    /** An OGC link object. */
    public static Map<String, Object> link(String href, String rel, String type, String title) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("href", href);
        m.put("rel", rel);
        if (type != null) {
            m.put("type", type);
        }
        if (title != null) {
            m.put("title", title);
        }
        return m;
    }

    private static String encode(String id) {
        return java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
