// Ported from God's Eye View, src/layers/perimeters/records.js (normalizeFirePerimeterSnapshot) and
// server/providers/firePerimeters.js (the WFIGS field list), MIT; see THIRD_PARTY_NOTICES.md. Reworked in Java:
// the attr_IrwinID fallback, the renamed and allowlisted payload, the time base and the simplification above
// 9,500 vertices are Foresignal's.
package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * One WFIGS Interagency Perimeters Current feature as a {@code fire-perimeter} record (prompt 075). Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code attr_UniqueFireIdentifier}, else {@code attr_IrwinID}; a feature with
 *       neither is skipped ({@value MappedRecord#MISSING_ID}).
 *   <li><strong>Time:</strong> {@code observedAt = poly_DateCurrent} (the perimeter's own update time; a feature
 *       without it is skipped, {@value MappedRecord#MISSING_OBSERVED_AT}); {@code eventTime =
 *       attr_FireDiscoveryDateTime}.
 *   <li><strong>Geometry:</strong> the polygon, simplified by {@link FeatureGeometry} above 9,500 vertices (the
 *       payload then carries {@code geometrySimplified} and {@code simplifyToleranceDeg}); a null geometry (ArcGIS
 *       emits one when {@code maxAllowableOffset} generalizes a tiny polygon away) keeps the record without one; a
 *       malformed geometry skips the feature ({@value MappedRecord#BAD_GEOMETRY}).
 *   <li><strong>Payload</strong> (renamed, nulls omitted): {@code name, gisAcres} (rounded to 1 acre),
 *       {@code incidentSizeAcres, percentContained, state, county, incidentType, cause, behavior, personnel,
 *       complexityLevel, complexName, irwinId}.
 * </ul>
 */
public final class NifcPerimeterMapper {

    /** The record kind. */
    public static final String KIND = "fire-perimeter";

    /** The attributes requested from the layer, as GEV requests them. */
    public static final List<String> OUT_FIELDS = List.of("poly_IncidentName", "poly_GISAcres", "poly_DateCurrent",
            "attr_UniqueFireIdentifier", "attr_IrwinID", "attr_IncidentSize", "attr_PercentContained", "attr_POOState",
            "attr_POOCounty", "attr_IncidentTypeCategory", "attr_FireDiscoveryDateTime", "attr_FireCause",
            "attr_FireBehaviorGeneral", "attr_TotalIncidentPersonnel", "attr_IncidentComplexityLevel", "attr_CpxName");

    private NifcPerimeterMapper() {}

    /**
     * Maps one GeoJSON feature.
     *
     * @param feature a GeoJSON Feature from the layer query
     * @return the record, or the skip reason
     */
    public static MappedRecord map(JsonNode feature) {
        JsonNode p = feature == null ? null : feature.get("properties");
        String irwinId = JsonProps.text(p, "attr_IrwinID");
        String id = JsonProps.text(p, "attr_UniqueFireIdentifier");
        if (id == null) {
            id = irwinId;
        }
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant observedAt = JsonProps.epochMillis(p, "poly_DateCurrent");
        if (observedAt == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            return MappedRecord.skipped(MappedRecord.BAD_GEOMETRY);
        }
        Double gisAcres = JsonProps.number(p, "poly_GISAcres");
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(observedAt)
                .eventTime(JsonProps.epochMillis(p, "attr_FireDiscoveryDateTime"))
                .geometry(geometry.geometry())
                .put("name", JsonProps.text(p, "poly_IncidentName"))
                .put("gisAcres", gisAcres == null ? null : Math.round(gisAcres))
                .put("incidentSizeAcres", JsonProps.number(p, "attr_IncidentSize"))
                .put("percentContained", JsonProps.number(p, "attr_PercentContained"))
                .put("state", JsonProps.text(p, "attr_POOState"))
                .put("county", JsonProps.text(p, "attr_POOCounty"))
                .put("incidentType", JsonProps.text(p, "attr_IncidentTypeCategory"))
                .put("cause", JsonProps.text(p, "attr_FireCause"))
                .put("behavior", JsonProps.text(p, "attr_FireBehaviorGeneral"))
                .put("personnel", JsonProps.integer(p, "attr_TotalIncidentPersonnel"))
                .put("complexityLevel", JsonProps.text(p, "attr_IncidentComplexityLevel"))
                .put("complexName", JsonProps.text(p, "attr_CpxName"))
                .put("irwinId", irwinId);
        if (geometry.simplified()) {
            b.put(FeatureGeometry.SIMPLIFIED_KEY, true).put(FeatureGeometry.TOLERANCE_KEY, geometry.toleranceDeg());
        }
        return MappedRecord.of(b.build());
    }
}
