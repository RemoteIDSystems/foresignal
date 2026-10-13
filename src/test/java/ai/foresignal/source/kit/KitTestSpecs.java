package ai.foresignal.source.kit;

import ai.foresignal.source.kit.feature.FeatureSourceSpec;
import ai.foresignal.source.kit.feed.FeedSourceSpec;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Kit specs for tests (prompt 073), bound from JSON exactly as the resource files are, so a test spec is checked by
 * the same binding (unknown properties refused).
 */
public final class KitTestSpecs {

    /** A valid ArcGIS spec at {@code url}, the WFIGS field set. */
    public static final String ARCGIS = """
            {
              "id": "feature-test-arcgis",
              "name": "Test ArcGIS layer",
              "kind": "fire-incident",
              "protocol": "arcgis",
              "url": "%s",
              "idField": "irwinId",
              "titleField": "name",
              "fields": {
                "IrwinID": {"target": "irwinId", "type": "string"},
                "IncidentName": {"target": "name", "type": "string"},
                "IncidentSize": {"target": "sizeAcres", "type": "number", "unit": "[acr_us]"},
                "ModifiedOnDateTime_dt": {"target": "modifiedAt", "type": "timestamp"},
                "FireDiscoveryDateTime": {"target": "discoveredAt", "type": "timestamp"}
              },
              "time": {"observedAt": "modifiedAt", "eventTime": "discoveredAt", "format": "epoch-ms"},
              "budget": {"perMinute": 60, "perDay": 1000},
              "paging": {"pageSize": 5, "maxPages": 10},
              "absenceTombstones": true,
              "volatileFields": ["modifiedAt"],
              "license": "Public domain",
              "attribution": "Test",
              "homepage": "https://example.org/"
            }
            """;

    private KitTestSpecs() {}

    /** A feature spec from JSON. */
    public static FeatureSourceSpec feature(String json) {
        return KitSpecLoader.MAPPER.readValue(json, FeatureSourceSpec.class);
    }

    /** A feed spec from JSON. */
    public static FeedSourceSpec feed(String json) {
        return KitSpecLoader.MAPPER.readValue(json, FeedSourceSpec.class);
    }

    /** The ArcGIS test spec at {@code url}. */
    public static FeatureSourceSpec arcgis(String url) {
        return feature(ARCGIS.formatted(url));
    }

    /** The ArcGIS test spec at {@code url} with top-level members replaced ({@code null} removes one). */
    public static FeatureSourceSpec arcgis(String url, Map<String, Object> overrides) {
        return feature(with(ARCGIS.formatted(url), overrides));
    }

    /** {@code json} with top-level members replaced; a null value removes the member. */
    public static String with(String json, Map<String, Object> overrides) {
        ObjectNode node = (ObjectNode) KitSpecLoader.MAPPER.readTree(json);
        overrides.forEach((k, v) -> {
            if (v == null) {
                node.remove(k);
            } else {
                JsonNode value = KitSpecLoader.MAPPER.valueToTree(v);
                node.set(k, value);
            }
        });
        return node.toString();
    }

    /** A valid feed spec at {@code url} with top-level members replaced. */
    public static FeedSourceSpec feed(String id, String url, Map<String, Object> overrides) {
        String json = """
                {
                  "id": "%s",
                  "name": "Test feed",
                  "url": "%s",
                  "budget": {"perMinute": 60, "perDay": 1000},
                  "license": "Test license",
                  "attribution": "Test",
                  "homepage": "https://example.org/"
                }
                """.formatted(id, url);
        return feed(with(json, overrides));
    }
}
