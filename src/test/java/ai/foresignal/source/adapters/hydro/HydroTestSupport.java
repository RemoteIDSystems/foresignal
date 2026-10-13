package ai.foresignal.source.adapters.hydro;

import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/** Shared helpers of the prompt 079 tests: JSON trees from text and fixtures, and fixture features. */
public final class HydroTestSupport {

    private HydroTestSupport() {}

    /** {@code json} as a tree. */
    public static JsonNode node(String json) {
        return HydroSources.JSON.readTree(json);
    }

    /** A fixture as a tree. */
    public static JsonNode fixture(String sourceId, String name) {
        return HydroSources.JSON.readTree(AdapterFixtures.bytes(sourceId, name));
    }

    /** The {@code features} of a fixture FeatureCollection. */
    public static List<JsonNode> features(String sourceId, String name) {
        List<JsonNode> out = new ArrayList<>();
        fixture(sourceId, name).get("features").forEach(out::add);
        return out;
    }
}
