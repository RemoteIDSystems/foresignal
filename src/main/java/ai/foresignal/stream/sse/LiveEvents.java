package ai.foresignal.stream.sse;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Touch;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the JSON objects of the {@code live} ({@code LiveEvent}) and {@code touch} ({@code TouchEvent}) events. */
public final class LiveEvents {

    private static final GeoJsonWriter GEOJSON = new GeoJsonWriter();

    private LiveEvents() {}

    /**
     * A {@code LiveEvent}. {@code properties} is the kinematic overlay over the persisted payload;
     * a null value means the key was removed.
     *
     * @param update the live update
     * @param leftFilter true when only {@code prevBbox} matched the client's spatial filter
     */
    public static Map<String, Object> live(LiveUpdate update, boolean leftFilter) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", update.sourceId());
        out.put("externalId", update.externalId());
        out.put("kind", update.kind());
        out.put("observedAt", update.observedAt());
        out.put("validUntil", update.validUntil());
        out.put("version", update.version());
        out.put("geometry", update.geometry() == null ? null : GEOJSON.write(update.geometry()));
        out.put("bbox", RecordMapper.box(update.bbox()));
        out.put("prevBbox", RecordMapper.box(update.prevBbox()));
        out.put("properties", update.properties() == null ? Map.of() : new LinkedHashMap<>(update.properties()));
        out.put("quality", update.quality());
        out.put("leftFilter", leftFilter);
        return out;
    }

    /** A {@code TouchEvent}. */
    public static Map<String, Object> touch(Touch touch) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", touch.sourceId());
        out.put("externalId", touch.externalId());
        out.put("observedAt", touch.observedAt());
        out.put("validUntil", touch.validUntil());
        out.put("bbox", RecordMapper.box(touch.bbox()));
        return out;
    }

    /**
     * {@code update} with a later touch merged in: only {@code observedAt}, {@code validUntil} and
     * {@code bbox} change.
     */
    public static LiveUpdate merge(LiveUpdate update, Touch touch) {
        return new LiveUpdate(
                update.sourceId(),
                update.externalId(),
                update.kind(),
                touch.observedAt(),
                touch.validUntil(),
                update.version(),
                update.geometry(),
                touch.bbox() != null ? touch.bbox() : update.bbox(),
                update.prevBbox(),
                update.properties(),
                update.quality());
    }
}
