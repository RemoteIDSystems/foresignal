package ai.foresignal.collect;

import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.Quality;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * One parsed record as a probe shows it (OpenAPI {@code NormalizedRecordPreview}).
 *
 * <p>Exactly what the adapter produced, before normalization: a probe is for seeing what the
 * adapter makes of the upstream, so nothing is clamped, imputed or rejected here.
 *
 * @param externalId the record's id within the source
 * @param kind its kind
 * @param observedAt when upstream observed it; null when the adapter left it to the framework
 * @param geometry GeoJSON geometry, or null
 * @param validUntil the upstream-asserted expiry, or null
 * @param tombstone true when upstream says the entity is gone
 * @param payload the snapshot
 * @param eventTime the discrete event time, or null
 * @param validFrom the start of the asserted validity, or null
 * @param quality position and value trust, or null when the adapter gave none
 * @param observers the contributing receivers
 */
public record NormalizedRecordPreview(
        String externalId,
        String kind,
        Instant observedAt,
        Map<String, Object> geometry,
        Instant validUntil,
        boolean tombstone,
        Map<String, Object> payload,
        Instant eventTime,
        Instant validFrom,
        Quality quality,
        List<Observer> observers) {

    private static final GeoJsonWriter GEOJSON = new GeoJsonWriter();

    /** The preview of {@code record}. */
    public static NormalizedRecordPreview of(NormalizedRecord record) {
        return new NormalizedRecordPreview(
                record.externalId(),
                record.kind(),
                record.observedAt(),
                GEOJSON.write(record.geometry()),
                record.validUntil(),
                record.tombstone(),
                record.payload(),
                record.eventTime(),
                record.validFrom(),
                record.quality() == null || record.quality().isEmpty() ? null : record.quality(),
                record.observers());
    }
}
