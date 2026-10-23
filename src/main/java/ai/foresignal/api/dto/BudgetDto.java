package ai.foresignal.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * The {@code Budget} schema: request limits per minute and per UTC day, and the optional money cap per UTC
 * calendar month.
 *
 * @param perMinute requests allowed per minute
 * @param perDay requests allowed per UTC day
 * @param monthlyCapUsd US dollars a paid source may spend per UTC calendar month, or null for no cap (135)
 */
public record BudgetDto(@NotNull @Min(0) Integer perMinute, @NotNull @Min(0) Integer perDay,
        @DecimalMin("0") BigDecimal monthlyCapUsd) {

    /** A budget with no money cap. */
    public BudgetDto(Integer perMinute, Integer perDay) {
        this(perMinute, perDay, null);
    }
}
