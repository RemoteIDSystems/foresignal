package ai.foresignal.source.adapters.local;

import ai.foresignal.source.formats.readsb.ReadsbDocument;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses readsb's {@code stats.json} into one {@code receiver-status} record (prompt 069; readsb
 * {@code README-json.md}, section "stats.json", read 2026-10-13).
 *
 * <p>The document has five periods ({@code latest, last1min, last5min, last15min, total}), each with
 * {@code start}, {@code end} (epoch seconds), {@code local} (absent in {@code --net-only} mode),
 * {@code remote}, {@code cpu}, {@code cpr}, {@code tracks} and {@code messages}. Only {@code last1min} is
 * read: it is a whole minute, unlike {@code latest}.
 *
 * <ul>
 *   <li>{@code messagesPerSec} = {@code messages / (end − start)}, to 0.1;
 *   <li>{@code signalDbfs}, {@code noiseDbfs}, {@code peakSignalDbfs} (to 0.1) and {@code strongSignals}
 *       from {@code local.signal, noise, peak_signal, strong_signals}, each when present ({@code noise} is
 *       dump1090-fa's; readsb's README does not list it);
 *   <li>{@code tracksNew} = {@code tracks.all} (tracks created in the minute);
 *   <li>{@code aircraftWithPos} from a top-level {@code aircraft_with_pos} (or one inside {@code last1min}),
 *       only when present: readsb's README does not document it;
 *   <li>{@code state}: {@code silent} when no message arrived, {@code low-rate} below
 *       {@code minMessagesPerSec}, else {@code ok}.
 * </ul>
 *
 * <p>{@code observedAt} is {@code last1min.end}. A body without {@code last1min}, its {@code start},
 * {@code end} or {@code messages} is an {@link UpstreamFormatException}.
 */
public final class ReadsbStatsParser {

    /** The record kind. */
    public static final String KIND = "receiver-status";

    /** {@code state} when messages arrive at or above the threshold. */
    public static final String STATE_OK = "ok";

    /** {@code state} when messages arrive below the threshold. */
    public static final String STATE_LOW_RATE = "low-rate";

    /** {@code state} when no message arrived in the minute. */
    public static final String STATE_SILENT = "silent";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ReadsbStatsParser() {}

    /**
     * Parses a body.
     *
     * @param sourceId the record's {@code externalId}
     * @param minMessagesPerSec the {@code low-rate} threshold
     * @throws UpstreamFormatException when the body is not a readsb {@code stats.json}
     */
    public static NormalizedRecord parse(byte[] body, String sourceId, double minMessagesPerSec) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("stats.json is not JSON: " + e.getOriginalMessage(), snippet(body), e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("stats.json is not a JSON object", snippet(body));
        }
        JsonNode minute = root.get("last1min");
        if (minute == null || !minute.isObject()) {
            throw new UpstreamFormatException("stats.json has no last1min period", snippet(body));
        }
        Double start = number(minute.get("start"));
        Double end = number(minute.get("end"));
        Double messages = number(minute.get("messages"));
        if (start == null || end == null || messages == null) {
            throw new UpstreamFormatException("stats.json last1min lacks start, end or messages", snippet(body));
        }
        double seconds = end - start;
        double perSec = seconds > 0 ? messages / seconds : 0.0;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messagesPerSec", LocalReceivers.round(perSec, 1));
        JsonNode local = minute.get("local");
        if (local != null && local.isObject()) {
            putRounded(payload, "signalDbfs", number(local.get("signal")));
            putRounded(payload, "noiseDbfs", number(local.get("noise")));
            putRounded(payload, "peakSignalDbfs", number(local.get("peak_signal")));
            putLong(payload, "strongSignals", number(local.get("strong_signals")));
        }
        JsonNode tracks = minute.get("tracks");
        if (tracks != null && tracks.isObject()) {
            putLong(payload, "tracksNew", number(tracks.get("all")));
        }
        Double withPos = number(root.get("aircraft_with_pos"));
        if (withPos == null) {
            withPos = number(minute.get("aircraft_with_pos"));
        }
        putLong(payload, "aircraftWithPos", withPos);
        payload.put("state", state(messages, perSec, minMessagesPerSec));

        Instant observedAt = ReadsbDocument.epoch(end);
        return NormalizedRecord.builder(sourceId, KIND).observedAt(observedAt).putAll(payload).build();
    }

    /** {@code silent} for no messages, {@code low-rate} below {@code min}, else {@code ok}. */
    public static String state(double messages, double perSec, double min) {
        if (messages <= 0) {
            return STATE_SILENT;
        }
        return perSec < min ? STATE_LOW_RATE : STATE_OK;
    }

    private static void putRounded(Map<String, Object> payload, String key, Double value) {
        if (value != null) {
            payload.put(key, LocalReceivers.round(value, 1));
        }
    }

    private static void putLong(Map<String, Object> payload, String key, Double value) {
        if (value != null) {
            payload.put(key, Math.round(value));
        }
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    private static String snippet(byte[] body) {
        return body == null ? "" : new String(body, 0, Math.min(body.length, 1000), StandardCharsets.UTF_8);
    }
}
