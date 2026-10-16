package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * The global notification pause (prompt 018), stored under {@link SettingKeys#NOTIFICATIONS}
 * (OpenAPI schema {@code NotificationSettings}).
 *
 * <p>While {@code pausedUntil} is in the future every delivery is deferred to it, except messages at
 * or above {@code pauseBypassSeverity}; deferred deliveries are released as one digest per channel.
 * {@code chatops} (prompt 061) holds the global kill switch of interactive chat channels.
 * {@code imageLinks} (prompt 108) makes the map attachment also a URL for channels that cannot take
 * attachments; enabling it requires an https {@code publicBaseUrl} (400
 * {@code IMAGE_LINKS_REQUIRE_HTTPS}).
 *
 * @param pausedUntil pause deliveries until this instant, or null for no pause
 * @param pauseBypassSeverity the severity wire value at or above which messages are sent anyway, or
 *     null when nothing bypasses the pause
 * @param chatops the interactive chat-command settings (prompt 061); null reads as the defaults
 * @param imageLinks expose the map as a URL for non-attachment channels (prompt 108); default false
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationSettings(Instant pausedUntil, String pauseBypassSeverity, ChatOps chatops,
        Boolean imageLinks) {

    /** The shipped bypass severity. */
    public static final String DEFAULT_BYPASS = "critical";

    /** Nulls read as their defaults. */
    public NotificationSettings {
        chatops = chatops == null ? ChatOps.defaults() : chatops;
        imageLinks = imageLinks == null ? Boolean.FALSE : imageLinks;
    }

    /** Settings with the default {@code chatops} and image links off (the shape before prompt 061). */
    public NotificationSettings(Instant pausedUntil, String pauseBypassSeverity) {
        this(pausedUntil, pauseBypassSeverity, null, false);
    }

    /** Settings with image links off (the shape before prompt 108). */
    public NotificationSettings(Instant pausedUntil, String pauseBypassSeverity, ChatOps chatops) {
        this(pausedUntil, pauseBypassSeverity, chatops, false);
    }

    /** No pause; critical bypasses; interactive chat channels enabled; image links off. */
    public static NotificationSettings defaults() {
        return new NotificationSettings(null, DEFAULT_BYPASS, ChatOps.defaults(), false);
    }

    /** True when image links are enabled. */
    public boolean imageLinksOn() {
        return Boolean.TRUE.equals(imageLinks);
    }

    /**
     * {@code NotificationSettings.chatops} (prompt 061).
     *
     * @param enabled the global kill switch of every interactive chat channel (default true)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatOps(Boolean enabled) {

        /** A null {@code enabled} reads as true. */
        public ChatOps {
            enabled = enabled == null ? Boolean.TRUE : enabled;
        }

        /** Interactive channels enabled. */
        public static ChatOps defaults() {
            return new ChatOps(true);
        }

        /** True when interactive channels may run. */
        public boolean on() {
            return Boolean.TRUE.equals(enabled);
        }
    }

    /** True when deliveries are paused at {@code now}. */
    public boolean pausedAt(Instant now) {
        return pausedUntil != null && pausedUntil.isAfter(now);
    }
}
