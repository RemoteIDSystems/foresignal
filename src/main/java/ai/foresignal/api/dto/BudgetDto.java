package ai.foresignal.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * The {@code Budget} schema: request limits per minute and per UTC day.
 *
 * @param perMinute requests allowed per minute
 * @param perDay requests allowed per UTC day
 */
public record BudgetDto(@NotNull @Min(0) Integer perMinute, @NotNull @Min(0) Integer perDay) {}
