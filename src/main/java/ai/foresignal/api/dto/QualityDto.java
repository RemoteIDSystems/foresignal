package ai.foresignal.api.dto;

import java.util.Map;

/**
 * The {@code Quality} schema: observation quality, never hashed.
 *
 * @param accuracyMeters horizontal accuracy in meters
 * @param confidence confidence from 0 to 1
 * @param positionSource how the position was obtained ({@code reported}, {@code calculated}, ...)
 * @param extra namespaced scalar extras
 */
public record QualityDto(
        Double accuracyMeters, Double confidence, String positionSource, Map<String, Object> extra) {}
