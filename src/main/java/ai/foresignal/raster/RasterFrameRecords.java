package ai.foresignal.raster;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the {@code raster-frame} records (Weather-Imagery-Design §6.8; OpenAPI
 * {@code RasterFrameEvent}). Source = the owning source; externalId = the product id; geometry = the
 * product bounds; the payload is small so change events always carry it inline.
 *
 * <ul>
 *   <li>Observation, one version per frame: {@code observedAt = eventTime = valid_time}; payload
 *       {@code {productId, frameKey, time, timeKey, tiles, bounds, stats, expiresAt}}.
 *   <li>Forecast, one version per completed run: {@code observedAt = eventTime = reference_time};
 *       payload {@code {productId, run, steps[], validTimes[], tilesTemplate}}.
 *   <li>The optional {@code {productId}:partial} record reports a run in progress (payload as for
 *       a run, plus {@code partial: true}) and is tombstoned when the run completes.
 * </ul>
 *
 * <p>An observation payload is kept under {@value #MAX_PAYLOAD_BYTES} bytes: when the statistics
 * would push it over, the per-code counts are left out of the record (they stay on the frame row and
 * in the frames API), then the statistics.
 */
public final class RasterFrameRecords {

    /** The inline payload budget of a {@code raster-frame} change event. */
    public static final int MAX_PAYLOAD_BYTES = 1024;

    /** The suffix of the run-in-progress record's externalId. */
    public static final String PARTIAL_SUFFIX = ":partial";

    private RasterFrameRecords() {}

    /** The observation record of {@code frame}. */
    public static NormalizedRecord observation(ProductSpec spec, RasterFrameRow frame, List<Double> bounds,
            String base, JsonMapper json) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("productId", spec.id());
        payload.put("frameKey", frame.frameKey());
        payload.put("time", frame.validTime().toString());
        payload.put("timeKey", frame.timeKey());
        payload.put("tiles", ProductUrls.tiles(base, spec, frame));
        if (bounds != null) {
            payload.put("bounds", bounds);
        }
        if (frame.stats() != null && !frame.stats().isEmpty()) {
            payload.put("stats", new LinkedHashMap<>(frame.stats()));
        }
        if (frame.expiresAt() != null) {
            payload.put("expiresAt", frame.expiresAt().toString());
        }
        fit(payload, json);
        return NormalizedRecord.builder(spec.id(), ProductSpec.RECORD_KIND)
                .observedAt(frame.validTime())
                .eventTime(frame.validTime())
                .geometry(bounds == null ? null : box(bounds))
                .putAll(payload)
                .build();
    }

    /** The record of a completed forecast run. */
    public static NormalizedRecord forecastRun(ProductSpec spec, Instant run, List<RasterFrameRow> frames,
            List<Double> bounds, String base) {
        return NormalizedRecord.builder(spec.id(), ProductSpec.RECORD_KIND)
                .observedAt(run)
                .eventTime(run)
                .geometry(bounds == null ? null : box(bounds))
                .putAll(runPayload(spec, run, frames, base, false))
                .build();
    }

    /** The run-in-progress record ({@code {productId}:partial}). */
    public static NormalizedRecord partialRun(ProductSpec spec, Instant run, List<RasterFrameRow> frames,
            List<Double> bounds, String base) {
        return NormalizedRecord.builder(spec.id() + PARTIAL_SUFFIX, ProductSpec.RECORD_KIND)
                .observedAt(run)
                .eventTime(run)
                .geometry(bounds == null ? null : box(bounds))
                .putAll(runPayload(spec, run, frames, base, true))
                .build();
    }

    /** The tombstone of the run-in-progress record, once the run completes. */
    public static NormalizedRecord partialTombstone(ProductSpec spec, Instant run) {
        return NormalizedRecord.tombstone(spec.id() + PARTIAL_SUFFIX, ProductSpec.RECORD_KIND, run);
    }

    private static Map<String, Object> runPayload(ProductSpec spec, Instant run, List<RasterFrameRow> frames,
            String base, boolean partial) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("productId", spec.id());
        payload.put("run", FrameKeys.runIso(run));
        payload.put("steps", frames.stream().map(RasterFrameRow::forecastStep).toList());
        payload.put("validTimes", frames.stream().map(f -> f.validTime().toString()).toList());
        payload.put("tilesTemplate", ProductUrls.runTilesTemplate(base, spec, run));
        if (partial) {
            payload.put("partial", true);
        }
        return payload;
    }

    /** The JSON size of {@code payload}, bytes. */
    public static int size(Map<String, Object> payload, JsonMapper json) {
        return json.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8).length;
    }

    @SuppressWarnings("unchecked")
    private static void fit(Map<String, Object> payload, JsonMapper json) {
        if (size(payload, json) <= MAX_PAYLOAD_BYTES) {
            return;
        }
        Object stats = payload.get("stats");
        if (stats instanceof Map<?, ?> m && m.containsKey("codeCounts")) {
            Map<String, Object> trimmed = new LinkedHashMap<>((Map<String, Object>) m);
            trimmed.remove("codeCounts");
            payload.put("stats", trimmed);
            if (size(payload, json) <= MAX_PAYLOAD_BYTES) {
                return;
            }
        }
        payload.remove("stats");
    }

    private static org.locationtech.jts.geom.Polygon box(List<Double> b) {
        double west = b.get(0);
        double east = b.get(2);
        if (west > east) {
            west = -180;
            east = 180;
        }
        return GridStore.box(west, b.get(1), east, b.get(3));
    }
}
