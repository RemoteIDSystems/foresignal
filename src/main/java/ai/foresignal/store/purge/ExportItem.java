package ai.foresignal.store.purge;

import java.time.Instant;
import java.util.Map;

/**
 * One record of an export: its API representation plus what the GPX writer needs.
 *
 * @param record the {@code Record} object (as {@code getRecord} or {@code listRecordVersions} return it,
 *     observers included)
 * @param geometryJson the GeoJSON geometry text, or null
 * @param observedAt the observed time
 * @param tombstone true for a deletion marker
 * @param altitudeM {@code payload.altitudeM} when numeric, else null
 */
public record ExportItem(Map<String, Object> record, String geometryJson, Instant observedAt, boolean tombstone,
        Double altitudeM) {}
