package ai.foresignal.store.track;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A simplified track: the {@code Track} schema's content before it is written as a GeoJSON
 * {@code Feature}.
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the entity's kind
 * @param from window start
 * @param to window end
 * @param coordinates per segment, {@code [lon, lat]} pairs in {@code [-180, 180]}; a lone point is
 *     repeated so every segment is a valid LineString
 * @param times epoch milliseconds parallel to {@code coordinates}
 * @param values per requested field, values parallel to {@code coordinates}
 * @param live true when the last point is the current {@code record_live} position
 * @param pointsScanned versions read (at most 200,000)
 * @param pointsReturned points in {@code coordinates}
 * @param toleranceMeters the tolerance finally used
 * @param segments the number of segments
 * @param approximate a sampled source (SAMPLED or LATEST) or a downsampled range
 * @param downsampled a downsampled range was read
 * @param truncated more than 200,000 versions existed in the window
 * @param watermarkSeq {@code S(to)}
 * @param immutable true when the answer can never change ({@code TimeTravelCache} keyed on
 *     {@code to}, and no live point appended)
 * @param publicAllowed false when the source is private ({@code Cache-Control: private})
 */
public record TrackResult(
        String sourceId,
        String externalId,
        String kind,
        Instant from,
        Instant to,
        List<List<double[]>> coordinates,
        List<List<Long>> times,
        Map<String, List<List<Double>>> values,
        boolean live,
        int pointsScanned,
        int pointsReturned,
        double toleranceMeters,
        int segments,
        boolean approximate,
        boolean downsampled,
        boolean truncated,
        long watermarkSeq,
        boolean immutable,
        boolean publicAllowed) {}
