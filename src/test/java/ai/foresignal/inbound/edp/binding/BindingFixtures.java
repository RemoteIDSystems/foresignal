package ai.foresignal.inbound.edp.binding;

import ai.foresignal.inbound.edp.EdpManifestModel;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/** Shared fixtures of the prompt 138 binding unit tests: a manifest with one stream of every item type, and items. */
final class BindingFixtures {

    /** Shared mapper. */
    static final JsonMapper JSON = JsonMapper.builder().build();

    /** The provider id. */
    static final String SOURCE = "ext-test-lab";

    /** The receipt time of the fixture items. */
    static final Instant RECEIVED = Instant.parse("2026-10-18T06:00:00Z");

    private BindingFixtures() {}

    /** The manifest JSON; {@code complete} sets the posts stream's {@code complete} flag. */
    static String manifestJson(boolean complete) {
        return """
            {
              "protocol": "foresignal-edp/1.0",
              "source": {
                "id": "test-lab",
                "name": "Test lab",
                "publisher": {"name": "Test"},
                "originator": {"id": "test-lab", "class": "aggregator", "aggregates": ["usgs-anss"]}
              },
              "transport": {"push": true},
              "streams": [
                {"id": "levels", "mode": "atomic", "itemType": "observation", "kind": "river-level", "unit": "m"},
                {"id": "incidents", "mode": "atomic", "itemType": "event", "kind": "maritime-incident",
                 "coverage": {"area": {"type": "Polygon", "coordinates": [[[54.5, 25.0], [58.0, 25.0], [58.0, 27.5],
                   [54.5, 27.5], [54.5, 25.0]]]}, "pDetect": 0.6},
                 "suggestedBindings": [{"target": "canonical-family", "config": {"family": "maritime-incident"}}]},
                {"id": "posts", "mode": "atomic", "itemType": "document", "kind": "channel-post",
                 "documentType": "social-post", "complete": %s},
                {"id": "articles", "mode": "atomic", "itemType": "document", "kind": "full-article",
                 "documentType": "article"},
                {"id": "notes", "mode": "atomic", "itemType": "document", "kind": "lab-note", "documentType": "note"},
                {"id": "odds", "mode": "atomic", "itemType": "forecast", "kind": "lab-odds"}
              ]
            }
            """.formatted(complete);
    }

    /** The parsed manifest. */
    static EdpManifestModel manifest(boolean complete) {
        return EdpManifestModel.parse(JSON.readTree(manifestJson(complete)));
    }

    /** A stream of the fixture manifest. */
    static BoundStream stream(String streamId) {
        return stream(streamId, false);
    }

    /** A stream of the fixture manifest, with the posts stream complete or not. */
    static BoundStream stream(String streamId, boolean complete) {
        EdpManifestModel m = manifest(complete);
        return new BoundStream(SOURCE, "sha", m, m.stream(streamId));
    }

    /** An item of {@code streamId} with EDP provenance, receipt {@link #RECEIVED} and the given extra payload keys. */
    static BoundItem item(String streamId, String itemId, String itemType, long seq, Map<String, Object> extra) {
        return item(streamId, itemId, itemType, seq, extra, null, null);
    }

    /** As {@link #item(String, String, String, long, Map)} with a point. */
    static BoundItem item(String streamId, String itemId, String itemType, long seq, Map<String, Object> extra,
            Double lon, Double lat) {
        Map<String, Object> payload = new LinkedHashMap<>(extra);
        Map<String, Object> edp = new LinkedHashMap<>();
        edp.put("stream", streamId);
        edp.put("mode", "atomic");
        edp.put("itemType", itemType);
        edp.put("originator", "test-lab");
        edp.put("originatorClass", "aggregator");
        edp.put("aggregates", List.of("usgs-anss"));
        payload.putIfAbsent("edp", edp);
        Map<String, Object> times = new LinkedHashMap<>();
        times.put("receivedAt", RECEIVED.toString());
        payload.putIfAbsent("edpTimes", times);
        return new BoundItem(seq, SOURCE, streamId + ":" + itemId, kindOf(streamId), 1, RECEIVED.minusSeconds(60),
                RECEIVED.minusSeconds(120), RECEIVED, lon, lat, payload);
    }

    /** The kind of a fixture stream. */
    static String kindOf(String streamId) {
        return manifest(false).stream(streamId).kind();
    }

    /** A binding of the fixture provider. */
    static ExternalBindingEntity binding(String streamId, String target, Map<String, Object> config) {
        ExternalBindingEntity b = new ExternalBindingEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", SOURCE, streamId, target,
                "admin", RECEIVED);
        b.setConfig(config);
        return b;
    }
}
