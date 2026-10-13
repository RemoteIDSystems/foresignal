package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code ground-station} record of a SatDump station (prompt 049, Weather-Imagery-Design §4.2).
 *
 * <p>{@code externalId} is the station name. The state is derived from an {@link ApiSnapshot}:
 * {@code locked} when any module reports a locked deframer or Viterbi decoder, else {@code receiving}
 * when a pipeline is running, else {@code tracking} when the tracker has a target above the horizon,
 * else {@code idle}; {@code unknown} without an {@code apiUrl}. Azimuth and elevation are quantized to
 * 1° and SNR to 1 dB, so a new version is written only on a state change or a quantized change.
 */
public final class GroundStationRecords {

    /** The record kind. */
    public static final String KIND = "ground-station";

    /** No {@code apiUrl}: nothing is known about the station's RF side. */
    public static final String UNKNOWN = "unknown";

    /** Nothing is tracked or received. */
    public static final String IDLE = "idle";

    /** The tracker has a target above the horizon. */
    public static final String TRACKING = "tracking";

    /** A pipeline is running. */
    public static final String RECEIVING = "receiving";

    /** A module reports a locked deframer or Viterbi decoder. */
    public static final String LOCKED = "locked";

    private GroundStationRecords() {}

    /** The state of a snapshot. */
    public static String state(ApiSnapshot snapshot) {
        if (snapshot == null) {
            return UNKNOWN;
        }
        if (snapshot.locked()) {
            return LOCKED;
        }
        if (snapshot.pipelineRunning()) {
            return RECEIVING;
        }
        if (snapshot.trackingAboveHorizon()) {
            return TRACKING;
        }
        return IDLE;
    }

    /**
     * The quantized payload of a snapshot; null snapshot gives the {@code unknown} payload.
     *
     * @param station the station name, for the title
     */
    public static Map<String, Object> payload(String station, ApiSnapshot snapshot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        String state = state(snapshot);
        payload.put("state", state);
        if (snapshot != null) {
            putIfNotNull(payload, "currentTarget", snapshot.target());
            putIfNotNull(payload, "frequencyHz", snapshot.frequencyHz());
            if (snapshot.target() != null) {
                putIfNotNull(payload, "az", quantize(snapshot.az()));
                putIfNotNull(payload, "el", quantize(snapshot.el()));
            }
            putIfNotNull(payload, "snrDb", quantize(snapshot.snr()));
            putIfNotNull(payload, "nextAos", snapshot.nextAos() == null ? null : snapshot.nextAos().toString());
            putIfNotNull(payload, "nextLos", snapshot.nextLos() == null ? null : snapshot.nextLos().toString());
            putIfNotNull(payload, "rotatorEngaged", snapshot.rotatorEngaged());
            payload.put("satdumpMode", snapshot.mode());
        }
        payload.put("title", title(station, state, snapshot == null ? null : snapshot.target()));
        return payload;
    }

    /**
     * The record.
     *
     * @param station the station name ({@code externalId})
     * @param payload the payload from {@link #payload}
     * @param observedAt when the {@code /api} response was read (or the stream start for {@code unknown})
     * @param point the station {@code {lon, lat}}, or null
     */
    public static NormalizedRecord record(String station, Map<String, Object> payload, Instant observedAt,
            double[] point) {
        NormalizedRecord.Builder builder = NormalizedRecord.builder(station, KIND)
                .observedAt(observedAt)
                .putAll(payload);
        if (point != null) {
            builder.point(point[0], point[1]);
        }
        return builder.build();
    }

    /** Rounds to a whole number (1° or 1 dB), or null. */
    static Long quantize(Double value) {
        return value == null || !Double.isFinite(value) ? null : Math.round(value);
    }

    private static String title(String station, String state, String target) {
        String base = "SatDump station " + station + ": " + state;
        return target == null || UNKNOWN.equals(state) || IDLE.equals(state) ? base : base + " " + target;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
