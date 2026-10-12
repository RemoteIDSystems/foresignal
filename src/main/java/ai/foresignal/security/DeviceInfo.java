package ai.foresignal.security;

import java.time.Instant;
import java.util.List;

/**
 * Device metadata a client app reports for its own token (OpenAPI {@code DeviceInfo}, prompt 023).
 *
 * @param deviceName the device's name, 1–200 characters
 * @param platform one of {@link #PLATFORMS}
 * @param appVersion the app version, up to 64 characters, or null
 * @param reportedAt when the token last reported it (server-set)
 */
public record DeviceInfo(String deviceName, String platform, String appVersion, Instant reportedAt) {

    /** The accepted {@code platform} values. */
    public static final List<String> PLATFORMS = List.of("ios", "android", "macos", "windows", "linux", "web", "other");

    /** Longest {@code deviceName}. */
    public static final int MAX_NAME = 200;

    /** Longest {@code appVersion}. */
    public static final int MAX_APP_VERSION = 64;
}
