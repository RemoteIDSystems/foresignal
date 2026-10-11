package ai.foresignal.api.dto;

/**
 * The {@code FieldThreshold} schema: a SAMPLED threshold on a numeric payload field.
 *
 * @param field the top-level payload key
 * @param delta the minimum change that persists a version
 * @param angular whether the comparison wraps at 360 degrees
 */
public record FieldThresholdDto(String field, double delta, boolean angular) {}
