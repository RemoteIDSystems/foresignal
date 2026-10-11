package ai.foresignal.api.dto;

/**
 * The {@code MotionSpec} schema: payload fields used for dead reckoning.
 *
 * @param speedField top-level payload key holding speed
 * @param speedUnit unit of the speed field ({@code knots}, {@code meters_per_second}, ...)
 * @param courseField top-level payload key holding course over ground, in degrees
 */
public record MotionSpecDto(String speedField, String speedUnit, String courseField) {}
