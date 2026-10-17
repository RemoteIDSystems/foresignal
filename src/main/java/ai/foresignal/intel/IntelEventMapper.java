package ai.foresignal.intel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Shapes intelligence events for the API, SSE and MCP (prompt 101). */
@Component
public class IntelEventMapper {

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
        out.put("centroid", centroid(event));
        out.put("regionTags", event.getRegionTags());
        out.put("confidenceMethod", event.getConfidenceMethod());
        out.put("calibrated", event.isCalibrated());
        out.put("origin", event.getOrigin());
        out.put("recipeId", event.getRecipeId());
        out.put("recipeVersion", event.getRecipeVersion());
        out.put("detector", event.getDetector());
        out.put("manifestId", event.getManifestId());
        out.put("exposure", event.getExposure());
        out.put("context", event.getContext());
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
        Map<String, Object> inference = new LinkedHashMap<>();
        inference.put("type", event.getType());
        inference.put("severity", event.getSeverity());
        inference.put("confidence", event.getConfidence());
        inference.put("confidenceLabel", event.getConfidenceLabel());
        inference.put("geometry", event.getGeom() == null ? null : event.getGeom().toString());
        inference.put("startTime", event.getStartTime());
        inference.put("endTime", event.getEndTime());
        Map<String, Object> speculation = new LinkedHashMap<>();
        speculation.put("alternatives", event.getAlternatives());
        speculation.put("narrative", event.getNarrative());
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
        out.put("id", member.getId());
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
