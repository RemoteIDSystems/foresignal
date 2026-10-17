package ai.foresignal.analytics.finding;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * What a detector found, before {@link FindingService#record} stores it (prompt 119).
 *
 * @param findingType the finding type
 * @param title a short title
 * @param severity {@code info}, {@code notable} or {@code significant} ({@link FindingSeverities})
 * @param geometry the geometry, or null (the first region's label point is used then)
 * @param regionCodes the 040 region codes
 * @param windowStart the window start
 * @param windowEnd the window end
 * @param detectedAt when the detector found it (its decision time)
 * @param seriesRefs the series it rests on
 * @param members pinned object references ({@code record:src/id@seq}, {@code finding:id}, …)
 * @param asOfSeq the seq the detector read up to
 * @param stats the method numbers
 * @param baseRate the base rate ({@code FindingBaseRate})
 * @param caveats the caveats ({@code {code, text, data?}})
 * @param detectorName the detector name
 * @param detectorVersion the detector version
 * @param detectorParams the detector parameters
 * @param explanation the deterministic explanation
 * @param privateFinding true when built from a private source
 * @param signalFamily the signal family, or null
 * @param localOnlySources the {@code LOCAL_ONLY} sources whose raw values {@code stats} holds
 * @param rawStatKeys the {@code stats} members holding those raw values
 * @param dedupScope what makes two findings of this type the same subject (a series ref, a region and family pair)
 */
public record FindingDraft(String findingType, String title, String severity, Geometry geometry,
        List<String> regionCodes, Instant windowStart, Instant windowEnd, Instant detectedAt, List<String> seriesRefs,
        List<String> members, long asOfSeq, Map<String, Object> stats, Map<String, Object> baseRate,
        List<Map<String, Object>> caveats, String detectorName, String detectorVersion,
        Map<String, Object> detectorParams, String explanation, boolean privateFinding, String signalFamily,
        List<String> localOnlySources, List<String> rawStatKeys, String dedupScope) {

    /** Copies the collections. */
    public FindingDraft {
        regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
        seriesRefs = seriesRefs == null ? List.of() : List.copyOf(seriesRefs);
        members = members == null ? List.of() : List.copyOf(members);
        stats = stats == null ? Map.of() : new java.util.LinkedHashMap<>(stats);
        baseRate = baseRate == null ? Map.of() : new java.util.LinkedHashMap<>(baseRate);
        caveats = caveats == null ? List.of() : List.copyOf(caveats);
        detectorParams = detectorParams == null ? Map.of() : new java.util.LinkedHashMap<>(detectorParams);
        localOnlySources = localOnlySources == null ? List.of() : List.copyOf(localOnlySources);
        rawStatKeys = rawStatKeys == null ? List.of() : List.copyOf(rawStatKeys);
    }
}
