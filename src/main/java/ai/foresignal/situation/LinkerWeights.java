package ai.foresignal.situation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every constant of the situation linker's score (prompt 118 §2), in one place so the Operator Guide's table and
 * the code cannot drift.
 *
 * @param sharedAdmin1 region: a shared first-level region (0.30)
 * @param sharedCountry region: a shared country, when no first-level region is shared (0.20)
 * @param distance distance: the weight of {@code max(0, 1 − d / maxDistanceKm)} (0.25)
 * @param maxDistanceKm the distance at which the distance term reaches zero (300 km)
 * @param maritimeMaxDistanceKm the same for maritime inputs or situations (500 km)
 * @param entity a shared 098 entity (0.25)
 * @param topic the input's text matches a linked 117 topic's keywords (0.15)
 * @param textSimilarity the weight of {@code max(0, (cos − cosFloor) / (1 − cosFloor))} (0.20)
 * @param cosFloor the cosine below which text similarity contributes nothing (0.70)
 * @param themePenalty the penalty for an incompatible theme (−0.20)
 * @param recencyHalfLifeHours the recency decay constant: multiplier {@code 0.5 + 0.5·exp(−h / 72)} (72 h)
 * @param linkThreshold the default join threshold (0.45)
 */
public record LinkerWeights(double sharedAdmin1, double sharedCountry, double distance, double maxDistanceKm,
        double maritimeMaxDistanceKm, double entity, double topic, double textSimilarity, double cosFloor,
        double themePenalty, double recencyHalfLifeHours, double linkThreshold) {

    /** The weights of linker {@code 1.0.0}. */
    public static final LinkerWeights DEFAULT = new LinkerWeights(0.30, 0.20, 0.25, 300, 500, 0.25, 0.15, 0.20, 0.70,
            -0.20, 72, 0.45);

    /** The constants as a map, stored in the linker's manifests. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sharedAdmin1", sharedAdmin1);
        out.put("sharedCountry", sharedCountry);
        out.put("distance", distance);
        out.put("maxDistanceKm", maxDistanceKm);
        out.put("maritimeMaxDistanceKm", maritimeMaxDistanceKm);
        out.put("entity", entity);
        out.put("topic", topic);
        out.put("textSimilarity", textSimilarity);
        out.put("cosFloor", cosFloor);
        out.put("themePenalty", themePenalty);
        out.put("recencyHalfLifeHours", recencyHalfLifeHours);
        out.put("linkThreshold", linkThreshold);
        return out;
    }
}
