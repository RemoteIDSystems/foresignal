package ai.foresignal.precursor;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import org.springframework.stereotype.Component;

/**
 * The precursor mining settings in force (prompt 120): {@code analysis.precursors.maxTestsPerRun} (500, 1–5000),
 * {@code analysis.precursors.lookbackDays} (365, 90–3650) and {@code analysis.precursors.discoveryFraction} (2/3,
 * 0.5–0.9). Unset or unreadable values fall back to the defaults; out-of-range values are clamped.
 */
@Component
public class PrecursorSettings {

    /** The default lookback, days. */
    public static final int DEFAULT_LOOKBACK_DAYS = 365;

    /** The default discovery share of the range. */
    public static final double DEFAULT_DISCOVERY_FRACTION = 2.0 / 3.0;

    private final AppSettingService settings;

    /** Creates the component. */
    public PrecursorSettings(AppSettingService settings) {
        this.settings = settings;
    }

    /** The cap on tests per mining run. */
    public int maxTestsPerRun() {
        return (int) Math.max(1, Math.min(5000, number(SettingKeys.ANALYSIS_PRECURSORS_MAX_TESTS,
                CandidateGenerator.DEFAULT_MAX_TESTS)));
    }

    /** The days of history mining reads. */
    public int lookbackDays() {
        return (int) Math.max(90, Math.min(3650, number(SettingKeys.ANALYSIS_PRECURSORS_LOOKBACK_DAYS,
                DEFAULT_LOOKBACK_DAYS)));
    }

    /** The discovery share of each range (the rest, newest, is validation). */
    public double discoveryFraction() {
        return Math.max(0.5, Math.min(0.9, number(SettingKeys.ANALYSIS_PRECURSORS_DISCOVERY_FRACTION,
                DEFAULT_DISCOVERY_FRACTION)));
    }

    private double number(String key, double fallback) {
        try {
            return settings.get(key, Double.class).orElse(fallback);
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
