package ai.foresignal.ingest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * One side of a gate comparison: the values at the gate's paths and the geometry.
 *
 * <p>A missing path maps to null (the map permits null values), which a gate treats
 * conservatively (§11).
 *
 * @param pathValues path to value, null when absent
 * @param geometry the geometry, or null
 */
public record GateState(Map<String, Object> pathValues, Geometry geometry) {

    public GateState {
        pathValues = pathValues == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(pathValues));
    }
}
