package ai.foresignal.situation;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.SettingKeys;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * The situation settings in force (prompt 118), each read from {@code app_setting} with its documented default:
 * {@code situation.linkThreshold} 0.45, {@code situation.dormancyWindow} 7 d, {@code situation.reopenWindow}
 * 30 d, {@code situation.briefs.enabled} false, {@code situation.briefs.maxPerDay} 50 and
 * {@code situation.briefMinInterval} 30 min. An unreadable value falls back to the default.
 */
@Component
public class SituationSettings {

    /** The default dormancy window. */
    public static final Duration DORMANCY_WINDOW = Duration.ofDays(7);

    /** The default reopen window. */
    public static final Duration REOPEN_WINDOW = Duration.ofDays(30);

    /** The default shortest interval between two briefs of one situation. */
    public static final Duration BRIEF_MIN_INTERVAL = Duration.ofMinutes(30);

    /** The default daily brief cap across all situations. */
    public static final int BRIEFS_MAX_PER_DAY = 50;

    private final AppSettingService settings;

    /** Creates the component. */
    public SituationSettings(AppSettingService settings) {
        this.settings = settings;
    }

    /** The join threshold in [0, 1]. */
    public double linkThreshold() {
        try {
            double value = settings.get(SettingKeys.SITUATION_LINK_THRESHOLD, Double.class)
                    .orElse(LinkerWeights.DEFAULT.linkThreshold());
            return value < 0 || value > 1 ? LinkerWeights.DEFAULT.linkThreshold() : value;
        } catch (RuntimeException e) {
            return LinkerWeights.DEFAULT.linkThreshold();
        }
    }

    /** The dormancy window. */
    public Duration dormancyWindow() {
        return duration(SettingKeys.SITUATION_DORMANCY_WINDOW, DORMANCY_WINDOW);
    }

    /** The reopen window. */
    public Duration reopenWindow() {
        return duration(SettingKeys.SITUATION_REOPEN_WINDOW, REOPEN_WINDOW);
    }

    /** True when self-refreshing briefs are on. */
    public boolean briefsEnabled() {
        try {
            return settings.get(SettingKeys.SITUATION_BRIEFS_ENABLED, Boolean.class).orElse(false);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** The daily brief cap. */
    public int briefsMaxPerDay() {
        try {
            int value = settings.get(SettingKeys.SITUATION_BRIEFS_MAX_PER_DAY, Integer.class).orElse(BRIEFS_MAX_PER_DAY);
            return Math.max(0, value);
        } catch (RuntimeException e) {
            return BRIEFS_MAX_PER_DAY;
        }
    }

    /** The shortest interval between two briefs of one situation. */
    public Duration briefMinInterval() {
        return duration(SettingKeys.SITUATION_BRIEF_MIN_INTERVAL, BRIEF_MIN_INTERVAL);
    }

    private Duration duration(String key, Duration fallback) {
        try {
            return settings.get(key, String.class).map(Durations::parsePositive).orElse(fallback);
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
