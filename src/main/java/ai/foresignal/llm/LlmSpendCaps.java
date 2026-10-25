package ai.foresignal.llm;

import java.math.BigDecimal;

/**
 * A provider's USD spend caps (OpenAPI {@code LlmProviderInput.hourlyCapUsd}, {@code dailyCapUsd},
 * {@code monthlyCapUsd}): the most it may spend in a UTC clock hour, a UTC day and a UTC calendar
 * month. Null means no cap in that window. They apply on top of {@code requestsPerMinute} and
 * {@code tokensPerDay}.
 *
 * @param hourlyUsd the hourly cap, or null
 * @param dailyUsd the daily cap, or null
 * @param monthlyUsd the monthly cap, or null
 */
public record LlmSpendCaps(BigDecimal hourlyUsd, BigDecimal dailyUsd, BigDecimal monthlyUsd) {

    /** No caps. */
    public static final LlmSpendCaps NONE = new LlmSpendCaps(null, null, null);

    /** True when any cap is set. */
    public boolean isSet() {
        return hourlyUsd != null || dailyUsd != null || monthlyUsd != null;
    }

    /** The cap of {@code window}, or null. */
    public BigDecimal of(LlmSpendWindow window) {
        return switch (window) {
            case HOUR -> hourlyUsd;
            case DAY -> dailyUsd;
            case MONTH -> monthlyUsd;
        };
    }
}
