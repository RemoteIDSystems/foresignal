package ai.foresignal.chronicle;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * How a chronicle layer captures what its selector matches (prompt 131, OpenAPI {@code CaptureMode}).
 *
 * <ul>
 *   <li>{@code full}: every matching version;</li>
 *   <li>{@code downsampled}: one version per entity per {@code interval} (default 60 s), plus every event version;</li>
 *   <li>{@code events-only}: versions persisted as FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE or TRIGGER_EDGE;</li>
 *   <li>{@code latest-per-interval}: the latest version per entity per {@code interval} bucket;</li>
 *   <li>{@code rollup-only}: counts per region and hour, no records;</li>
 *   <li>{@code frames}: every {@code everyNthFrame}-th raster frame, by reference.</li>
 * </ul>
 *
 * @param mode the mode
 * @param interval the interval of {@code downsampled} and {@code latest-per-interval}, else null
 * @param everyNthFrame the frame step of {@code frames}, else null
 */
public record CaptureMode(String mode, Duration interval, Integer everyNthFrame) {

    /** Every matching version. */
    public static final String FULL = "full";

    /** One per entity per interval. */
    public static final String DOWNSAMPLED = "downsampled";

    /** Event persist reasons only. */
    public static final String EVENTS_ONLY = "events-only";

    /** The latest per entity per interval. */
    public static final String LATEST_PER_INTERVAL = "latest-per-interval";

    /** Counts per region and hour. */
    public static final String ROLLUP_ONLY = "rollup-only";

    /** Every Nth raster frame. */
    public static final String FRAMES = "frames";

    /** Every mode, in spec order. */
    public static final List<String> MODES = List.of(FULL, DOWNSAMPLED, EVENTS_ONLY, LATEST_PER_INTERVAL, ROLLUP_ONLY,
            FRAMES);

    /** The default interval of {@code downsampled} and {@code latest-per-interval}. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(60);

    /** The shortest interval accepted. */
    public static final Duration MIN_INTERVAL = Duration.ofSeconds(1);

    /** The longest interval accepted. */
    public static final Duration MAX_INTERVAL = Duration.ofDays(31);

    /** The default frame step for raster products in a suggestion. */
    public static final int DEFAULT_FRAME_STEP = 4;

    /** Full capture. */
    public static final CaptureMode FULL_MODE = new CaptureMode(FULL, null, null);

    /** Events-only capture. */
    public static final CaptureMode EVENTS_ONLY_MODE = new CaptureMode(EVENTS_ONLY, null, null);

    /** Normalizes the mode's parameters: intervals only where they apply, a frame step only for frames. */
    public CaptureMode {
        if (DOWNSAMPLED.equals(mode) || LATEST_PER_INTERVAL.equals(mode)) {
            interval = interval == null ? DEFAULT_INTERVAL : interval;
            everyNthFrame = null;
        } else if (FRAMES.equals(mode)) {
            interval = null;
            everyNthFrame = everyNthFrame == null || everyNthFrame < 1 ? 1 : everyNthFrame;
        } else {
            interval = null;
            everyNthFrame = null;
        }
    }

    /** Downsampled at {@code interval}. */
    public static CaptureMode downsampled(Duration interval) {
        return new CaptureMode(DOWNSAMPLED, interval, null);
    }

    /** Every {@code n}-th frame. */
    public static CaptureMode frames(int n) {
        return new CaptureMode(FRAMES, null, n);
    }

    /**
     * Parses a {@code CaptureMode} body.
     *
     * @param node the body
     * @param field the field name for errors
     * @return the mode
     * @throws ValidationException {@code 400 CHRONICLE_LAYER_INVALID} when the mode or its parameters are invalid
     */
    public static CaptureMode parse(JsonNode node, String field) {
        if (node == null || node.isNull() || !node.isObject()) {
            throw invalid(field, "must be an object with mode");
        }
        for (String member : node.propertyNames()) {
            if (!List.of("mode", "interval", "everyNthFrame").contains(member)) {
                throw invalid(field + "." + member, "is not a CaptureMode member");
            }
        }
        JsonNode m = node.get("mode");
        if (m == null || !m.isString() || !MODES.contains(m.asString())) {
            throw invalid(field + ".mode", "must be one of " + MODES);
        }
        String mode = m.asString();
        Duration interval = null;
        JsonNode i = node.get("interval");
        if (i != null && !i.isNull()) {
            if (!DOWNSAMPLED.equals(mode) && !LATEST_PER_INTERVAL.equals(mode)) {
                throw invalid(field + ".interval", "applies only to downsampled and latest-per-interval");
            }
            interval = duration(i, field + ".interval");
        }
        Integer every = null;
        JsonNode e = node.get("everyNthFrame");
        if (e != null && !e.isNull()) {
            if (!FRAMES.equals(mode)) {
                throw invalid(field + ".everyNthFrame", "applies only to frames");
            }
            if (!e.isIntegralNumber() || e.asInt() < 1 || e.asInt() > 10_000) {
                throw invalid(field + ".everyNthFrame", "must be an integer from 1 to 10000");
            }
            every = e.asInt();
        }
        return new CaptureMode(mode, interval, every);
    }

    /** An ISO-8601 duration within [{@link #MIN_INTERVAL}, {@link #MAX_INTERVAL}]. */
    static Duration duration(JsonNode node, String field) {
        if (!node.isString()) {
            throw invalid(field, "must be an ISO-8601 duration such as PT60S");
        }
        try {
            Duration d = Duration.parse(node.asString());
            if (d.compareTo(MIN_INTERVAL) < 0 || d.compareTo(MAX_INTERVAL) > 0) {
                throw invalid(field, "must be from PT1S to P31D");
            }
            return d;
        } catch (DateTimeParseException ex) {
            throw invalid(field, "must be an ISO-8601 duration such as PT60S");
        }
    }

    /** A mode from its stored map (the {@code capture} jsonb column); full when missing. */
    public static CaptureMode fromMap(Map<String, Object> map) {
        if (map == null || !(map.get("mode") instanceof String mode) || !MODES.contains(mode)) {
            return FULL_MODE;
        }
        Duration interval = null;
        if (map.get("interval") instanceof String s) {
            try {
                interval = Duration.parse(s);
            } catch (DateTimeParseException e) {
                interval = null;
            }
        }
        Integer every = map.get("everyNthFrame") instanceof Number n ? n.intValue() : null;
        return new CaptureMode(mode, interval, every);
    }

    /** The {@code CaptureMode} wire shape (also stored as jsonb). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode);
        if (interval != null) {
            out.put("interval", interval.toString());
        }
        if (everyNthFrame != null) {
            out.put("everyNthFrame", everyNthFrame);
        }
        return out;
    }

    /** The mode in force while the chronicle is over its cap: full and downsampled become events-only. */
    public CaptureMode effective(boolean capExceeded) {
        if (capExceeded && (FULL.equals(mode) || DOWNSAMPLED.equals(mode))) {
            return EVENTS_ONLY_MODE;
        }
        return this;
    }

    /** True when the mode writes record versions (all but {@code rollup-only}). */
    public boolean writesVersions() {
        return !ROLLUP_ONLY.equals(mode);
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.CHRONICLE_LAYER_INVALID, "The capture mode is not valid.",
                List.of(new FieldError(field, message)));
    }
}
