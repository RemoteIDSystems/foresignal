package ai.foresignal.evidence;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A canonical-event lifecycle event (prompt 116) for the SSE topic {@code evidence}: summary only.
 *
 * @param type {@code canonical.created}, {@code canonical.merged} or {@code canonical.flagged}
 * @param canonicalEventId the event
 * @param family the family
 * @param status the status
 * @param eventTime the event time
 * @param regionCodes the region codes
 * @param observationCount the observations
 * @param evidenceGroupCount the evidence groups
 * @param qualityFlags the flags
 * @param flagsAdded the flags added ({@code canonical.flagged}), else empty
 * @param mergedInto the survivor, or null
 * @param mergedFrom the merged events ({@code canonical.merged}), else empty
 * @param sourceIds the event's sources (for the private-source filter; never sent)
 */
public record EvidenceEvent(String type, String canonicalEventId, String family, String status, Instant eventTime,
        List<String> regionCodes, int observationCount, int evidenceGroupCount, List<String> qualityFlags,
        List<String> flagsAdded, String mergedInto, List<String> mergedFrom, List<String> sourceIds) {

    /** A new event. */
    public static final String CREATED = "canonical.created";

    /** Events were merged into this one. */
    public static final String MERGED = "canonical.merged";

    /** Quality flags were added. */
    public static final String FLAGGED = "canonical.flagged";

    /** Copies the lists. */
    public EvidenceEvent {
        regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
        qualityFlags = qualityFlags == null ? List.of() : List.copyOf(qualityFlags);
        flagsAdded = flagsAdded == null ? List.of() : List.copyOf(flagsAdded);
        mergedFrom = mergedFrom == null ? List.of() : List.copyOf(mergedFrom);
        sourceIds = sourceIds == null ? List.of() : List.copyOf(sourceIds);
    }

    /** The event of a stored canonical event. */
    public static EvidenceEvent of(String type, EvidenceStore.StoredEvent event, List<String> flagsAdded,
            List<String> mergedFrom) {
        return new EvidenceEvent(type, event.id(), event.family(), event.status(), event.eventTime(),
                event.regionCodes(), event.observationCount(), event.evidenceGroupCount(), event.qualityFlags(),
                flagsAdded, event.mergedInto(), mergedFrom, event.sourceIds());
    }

    /** The {@code EvidenceEvent} payload as the spec defines it. */
    public Map<String, Object> payload() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("canonicalEventId", canonicalEventId);
        out.put("ref", "canonical-event:" + canonicalEventId);
        out.put("family", family);
        out.put("status", status);
        out.put("eventTime", eventTime == null ? null : eventTime.toString());
        out.put("regionCodes", regionCodes);
        out.put("observationCount", observationCount);
        out.put("evidenceGroupCount", evidenceGroupCount);
        out.put("qualityFlags", qualityFlags);
        if (FLAGGED.equals(type)) {
            out.put("flagsAdded", flagsAdded);
        }
        out.put("mergedInto", mergedInto);
        if (MERGED.equals(type)) {
            out.put("mergedFrom", mergedFrom);
        }
        return out;
    }
}
