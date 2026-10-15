package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;

/**
 * Entity registry settings (prompt 098), stored under {@link SettingKeys#ENTITY}.
 *
 * @param callsignGap how long an {@code air:} callsign link stays open after its record was last
 *     observed (default 6 h)
 * @param includePrivate when true, records from private sources may create entities and links
 *     (default false)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EntitySettings(Duration callsignGap, Boolean includePrivate) {

    /** The default gap after which an {@code air:} callsign link closes. */
    public static final Duration DEFAULT_CALLSIGN_GAP = Duration.ofHours(6);

    public EntitySettings {
        callsignGap = callsignGap == null || callsignGap.isNegative() || callsignGap.isZero()
                ? DEFAULT_CALLSIGN_GAP
                : callsignGap;
        includePrivate = includePrivate != null && includePrivate;
    }

    /** The shipped defaults. */
    public static EntitySettings defaults() {
        return new EntitySettings(null, null);
    }
}
