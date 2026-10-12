package ai.foresignal.stats.glance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Glance options, {@code app_setting['glance']} (prompt 038).
 *
 * @param exposeCountsAnonymously when true, callers without a token also get the alert, source and
 *     activity counts; off by default
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GlanceSettings(boolean exposeCountsAnonymously) {

    /** The shipped default: counts are never shown anonymously. */
    public static GlanceSettings defaults() {
        return new GlanceSettings(false);
    }
}
