package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Trigger engine defaults (prompt 017), stored under {@link SettingKeys#TRIGGERS}.
 *
 * <p>Prompt 019's settings endpoints expose it; until then it is read with its default.
 *
 * @param defaultMaxAlertsPerHour the storm cap of a rule whose {@code maxAlertsPerHour} is null; 0
 *     means unlimited
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TriggerSettings(Integer defaultMaxAlertsPerHour) {

    /** The shipped storm cap. */
    public static final int DEFAULT_MAX_ALERTS_PER_HOUR = 120;

    /** A missing or negative cap reads as the default. */
    public TriggerSettings {
        if (defaultMaxAlertsPerHour == null || defaultMaxAlertsPerHour < 0) {
            defaultMaxAlertsPerHour = DEFAULT_MAX_ALERTS_PER_HOUR;
        }
    }

    /** The shipped defaults. */
    public static TriggerSettings defaults() {
        return new TriggerSettings(DEFAULT_MAX_ALERTS_PER_HOUR);
    }
}
