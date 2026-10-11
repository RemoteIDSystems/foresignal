package ai.foresignal.api.dto;

import java.util.List;

/**
 * The {@code Coverage} schema: receiver coverage reported by a fetch.
 *
 * @param receivers receivers contributing
 * @param expected items expected
 * @param actual items received
 * @param region covered region {@code [minLon, minLat, maxLon, maxLat]}, or null
 */
public record CoverageDto(Integer receivers, Integer expected, Integer actual, List<Double> region) {}
