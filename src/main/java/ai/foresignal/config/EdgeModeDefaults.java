package ai.foresignal.config;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * The retention defaults of edge mode (prompt 113): a 6 h track window ({@code retention.trackWindow}) and a 1 d
 * default window ({@code retention.default}), with track downsampling off (the raw window could not be shorter
 * than the track window otherwise). Applied once, only when the operator has stored no retention settings; an
 * edge's history is a buffer for its hub, not an archive.
 */
@Component
@ConditionalOnProperty(name = EdgeMode.PROPERTY, havingValue = EdgeMode.EDGE)
public class EdgeModeDefaults implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EdgeModeDefaults.class);

    /** The edge track window. */
    public static final Duration TRACK_WINDOW = Duration.ofHours(6);

    /** The edge default window. */
    public static final Duration DEFAULT_WINDOW = Duration.ofDays(1);

    private final AppSettingService settings;

    /** Creates the defaults. */
    public EdgeModeDefaults(AppSettingService settings) {
        this.settings = settings;
    }

    @Override
    public void run(ApplicationArguments args) {
        apply();
    }

    /** Stores the edge retention defaults when no retention settings exist; true when it stored them. */
    public boolean apply() {
        if (settings.get(SettingKeys.RETENTION, RetentionSettings.class).isPresent()) {
            return false;
        }
        settings.put(SettingKeys.RETENTION, defaults());
        log.info("Edge mode: retention defaults set (track window {}, default window {})", TRACK_WINDOW,
                DEFAULT_WINDOW);
        return true;
    }

    /** The edge retention settings. */
    public static RetentionSettings defaults() {
        return new RetentionSettings(DEFAULT_WINDOW, null, null, null, TRACK_WINDOW, null, null, null, null, null,
                null, null, null, RetentionSettings.DOWNSAMPLING_DISABLED, null, null);
    }
}
