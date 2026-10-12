package ai.foresignal.raster;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Frame ids, time keys and frame keys (Weather-Imagery-Design §2.3, §6.1).
 *
 * <ul>
 *   <li>{@code timeKey}: the path form of a valid time, {@code 20261010T143240Z} (UTC, seconds).
 *   <li>{@code frameKey}: {@code {timeKey}~{sha8}}, where {@code sha8} is the first 8 hex characters
 *       of the decoded content's SHA-256: the content-addressed, immutable token.
 *   <li>frame id: immutable and time-based, the {@code timeKey} for an observation and
 *       {@code 2026101012Z-f006} (run, then step hours) for a forecast step.
 * </ul>
 *
 * <p>Clients never build these strings; the server hands out fully expanded URLs.
 */
public final class FrameKeys {

    /** The separator between a time key and its sha8. */
    public static final char FRAME_KEY_SEPARATOR = '~';

    private static final DateTimeFormatter TIME_KEY =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter RUN_HOUR =
            DateTimeFormatter.ofPattern("yyyyMMddHH'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter RUN_MINUTE =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);
    private static final Pattern TIME_KEY_PATTERN = Pattern.compile("^[0-9]{8}T[0-9]{6}Z$");
    private static final Pattern SHA8_PATTERN = Pattern.compile("^[0-9a-f]{8}$");

    private FrameKeys() {}

    /** The time key of {@code instant}, truncated to the second. */
    public static String timeKey(Instant instant) {
        return TIME_KEY.format(instant.truncatedTo(ChronoUnit.SECONDS));
    }

    /** True when {@code token} has the time-key shape. */
    public static boolean isTimeKey(String token) {
        return token != null && TIME_KEY_PATTERN.matcher(token).matches();
    }

    /**
     * The instant of a time key.
     *
     * @throws IllegalArgumentException when {@code token} is not a valid time key
     */
    public static Instant parseTimeKey(String token) {
        if (!isTimeKey(token)) {
            throw new IllegalArgumentException("Not a time key: " + token);
        }
        try {
            return LocalDateTime.parse(token.substring(0, 15), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"))
                    .toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Not a time key: " + token, e);
        }
    }

    /** The frame id of an observation valid at {@code validTime}. */
    public static String observationFrameId(Instant validTime) {
        return timeKey(validTime);
    }

    /** The frame id of step {@code stepHours} of the run at {@code referenceTime}. */
    public static String forecastFrameId(Instant referenceTime, int stepHours) {
        Instant run = referenceTime.truncatedTo(ChronoUnit.MINUTES);
        boolean onHour = run.equals(run.truncatedTo(ChronoUnit.HOURS));
        return (onHour ? RUN_HOUR : RUN_MINUTE).format(run) + "-f" + String.format(Locale.ROOT, "%03d", stepHours);
    }

    /** The first 8 hex characters of a SHA-256. */
    public static String sha8(String sha256) {
        return sha256.substring(0, 8);
    }

    /** {@code {timeKey}~{sha8}}. */
    public static String frameKey(String timeKey, String sha256) {
        return timeKey + FRAME_KEY_SEPARATOR + sha8(sha256);
    }

    /** The ISO-8601 form of a run's reference time, as used in {@code run=}. */
    public static String runIso(Instant referenceTime) {
        return referenceTime.truncatedTo(ChronoUnit.SECONDS).toString();
    }

    /**
     * A frame token split into its time key and optional sha8.
     *
     * @param timeKey the time key
     * @param sha8 the sha8, or null for a bare time key
     */
    public record ParsedKey(String timeKey, String sha8) {

        /** True for a content-addressed key. */
        public boolean contentAddressed() {
            return sha8 != null;
        }

        /** The valid time. */
        public Instant time() {
            return parseTimeKey(timeKey);
        }
    }

    /**
     * Parses a bare time key or a frame key.
     *
     * @return the parsed key, or null when {@code token} is neither
     */
    public static ParsedKey parseFrameKey(String token) {
        if (token == null) {
            return null;
        }
        int sep = token.indexOf(FRAME_KEY_SEPARATOR);
        String time = sep < 0 ? token : token.substring(0, sep);
        String sha = sep < 0 ? null : token.substring(sep + 1).toLowerCase(Locale.ROOT);
        if (!isTimeKey(time) || (sha != null && !SHA8_PATTERN.matcher(sha).matches())) {
            return null;
        }
        try {
            parseTimeKey(time);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new ParsedKey(time, sha);
    }

    /**
     * The canonical time key of an ISO-8601 extended time ({@code 2026-10-10T14:32:40Z}, with an
     * offset or fractional seconds), for the 301 that canonicalizes such a path segment.
     *
     * @return the time key, or null when {@code token} is not an ISO-8601 extended instant
     */
    public static String canonicalFromIso(String token) {
        if (token == null || token.indexOf('-') < 0 || token.indexOf(':') < 0) {
            return null;
        }
        try {
            return timeKey(OffsetDateTime.parse(token).toInstant());
        } catch (DateTimeParseException e) {
            try {
                return timeKey(Instant.parse(token));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    /**
     * Parses a run parameter's concrete value: ISO-8601 extended, or a time key.
     *
     * @return the instant, or null when {@code value} is neither
     */
    public static Instant parseRun(String value) {
        if (value == null) {
            return null;
        }
        String canonical = isTimeKey(value) ? value : canonicalFromIso(value);
        return canonical == null ? null : parseTimeKey(canonical);
    }
}
