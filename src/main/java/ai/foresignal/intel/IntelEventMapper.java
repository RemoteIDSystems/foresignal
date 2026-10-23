package ai.foresignal.intel;

import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Shapes intelligence events for the API, SSE and MCP (prompt 101). */
@Component
public class IntelEventMapper {

    private static final GeoJsonWriter GEOJSON = new GeoJsonWriter();

    /** The compact event summary used by SSE and list responses. */
    public static Map<String, Object> summary(IntelligenceEventEntity event) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", event.getId());
        out.put("type", event.getType());
        out.put("title", event.getTitle());
        out.put("severity", event.getSeverity());
        out.put("status", event.getStatus());
        out.put("confidence", event.getConfidence());
        out.put("confidenceLabel", event.getConfidenceLabel());
        out.put("evidenceQuality", event.getEvidenceQuality());
        out.put("memberCount", event.getMemberCount() == null ? 0 : event.getMemberCount());
        out.put("startTime", event.getStartTime());
        out.put("endTime", event.getEndTime());
        out.put("lastEvidenceAt", event.getLastEvidenceAt());
        out.put("private", event.isPrivateEvent());
        out.put("version", event.getVersion());
        out.put("updatedAt", event.getUpdatedAt());
        out.put("mergedInto", event.getMergedInto());
        return out;
    }

    /** The full event with its evidence, inference and speculation sections. */
    public Map<String, Object> full(IntelligenceEventEntity event, List<IntelEventMemberEntity> members,
            List<IntelEventLogEntity> log) {
        Map<String, Object> out = new LinkedHashMap<>(summary(event));
        out.put("severityOverridden", event.isSeverityOverridden());
        out.put("centroid", centroid(event));
        out.put("regionTags", event.getRegionTags());
        out.put("createdAt", event.getCreatedAt());
        out.put("confidenceMethod", event.getConfidenceMethod());
        out.put("calibrated", event.isCalibrated());
        // The uncalibrated EventScorer score behind confidence (prompt 151); equal to confidence until calibrated.
        out.put("rawConfidence", event.getRawConfidence());
        out.put("origin", event.getOrigin());
        out.put("recipeId", event.getRecipeId());
        out.put("recipeVersion", event.getRecipeVersion());
        out.put("detector", event.getDetector());
        out.put("manifestId", event.getManifestId());
        out.put("exposure", event.getExposure());
        // The contract's context is an object: empty until an annotator adds to it.
        out.put("context", event.getContext() == null ? Map.of() : event.getContext());
        out.put("explanation", event.getExplanation());
        out.put("alternatives", event.getAlternatives());
        out.put("narrative", event.getNarrative());
        out.put("narrativeLabel", event.getNarrative() == null ? null : "speculation: LLM narrative");
        // Operator or agent notes: authored by a person or agent, never labeled as LLM speculation.
        out.put("notes", event.getNotes());

        // evidence / inference / speculation, separated (prompt 101 §principles).
        Map<String, Object> evidence = new LinkedHashMap<>();
        List<Map<String, Object>> memberMaps = new ArrayList<>();
        for (IntelEventMemberEntity member : members) {
            memberMaps.add(member(member));
        }
        evidence.put("members", memberMaps);
        evidence.put("memberCount", members.size());
        evidence.put("signalFamilies", members.stream().map(IntelEventMemberEntity::getSignalFamily)
                .filter(java.util.Objects::nonNull).distinct().sorted().toList());
        Map<String, Object> inference = new LinkedHashMap<>();
        inference.put("type", event.getType());
        inference.put("severity", event.getSeverity());
        inference.put("confidence", event.getConfidence());
        inference.put("confidenceLabel", event.getConfidenceLabel());
        // The members the contract gives the inference section; the same values stay at the top level as well.
        inference.put("rawConfidence", event.getRawConfidence());
        inference.put("confidenceMethod", event.getConfidenceMethod());
        inference.put("calibrated", event.isCalibrated());
        inference.put("evidenceQuality", event.getEvidenceQuality());
        inference.put("lastEvidenceAt", event.getLastEvidenceAt());
        inference.put("centroid", GEOJSON.write(event.getCentroid()));
        inference.put("regionTags", event.getRegionTags() == null ? List.of() : event.getRegionTags());
        inference.put("explanation", event.getExplanation());
        // The contract's inference.geometry is a GeoJSON geometry, never WKT text.
        inference.put("geometry", GEOJSON.write(event.getGeom()));
        inference.put("startTime", event.getStartTime());
        inference.put("endTime", event.getEndTime());
        Map<String, Object> speculation = new LinkedHashMap<>();
        speculation.put("label", "speculation");
        speculation.put("alternatives", event.getAlternatives() == null ? List.of() : event.getAlternatives());
        speculation.put("narrative", event.getNarrative());
        speculation.put("narrativeOrigin", event.getNarrative() == null ? null : "llm");
        out.put("evidence", evidence);
        out.put("inference", inference);
        out.put("speculation", speculation);

        List<Map<String, Object>> logMaps = new ArrayList<>();
        for (IntelEventLogEntity entry : log) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("at", entry.getAt());
            item.put("actor", entry.getActor());
            item.put("action", entry.getAction());
            item.put("before", entry.getBefore());
            item.put("after", entry.getAfter());
            logMaps.add(item);
        }
        out.put("log", logMaps);
        return out;
    }

    /** One member. */
    public Map<String, Object> member(IntelEventMemberEntity member) {
        Map<String, Object> out = new LinkedHashMap<>();
        // The contract's IntelEventMember.id is a string; the stored id is a bigint.
        out.put("id", member.getId() == null ? null : String.valueOf(member.getId()));
        out.put("role", member.getRole());
        out.put("memberType", member.getMemberType());
        out.put("sourceId", member.getSourceId());
        out.put("externalId", member.getExternalId());
        out.put("seq", member.getSeq());
        out.put("version", member.getVersion());
        out.put("kind", member.getKind());
        out.put("observedAt", member.getObservedAt());
        out.put("joinReason", member.getJoinReason());
        out.put("signalFamily", member.getSignalFamily());
        out.put("weight", member.getWeight());
        out.put("addedAt", member.getAddedAt());
        out.put("addedBy", member.getAddedBy());
        return out;
    }

    private static Object centroid(IntelligenceEventEntity event) {
        if (event.getCentroid() == null) {
            return null;
        }
        return List.of(event.getCentroid().getX(), event.getCentroid().getY());
    }
}
