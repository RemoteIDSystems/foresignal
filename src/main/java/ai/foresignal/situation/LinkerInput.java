package ai.foresignal.situation;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * One object the situation linker considers (prompt 118 §2), already normalized from its record or alert row by
 * {@link LinkerInputReader}: what it is, where and when, what it reports, and which evidence it rests on.
 *
 * @param seq the seq the input was read at (the record version's seq, or the alert's seq)
 * @param memberType the member type ({@link SituationMemberTypes})
 * @param ref the member ref (an id, or {@code sourceId/externalId})
 * @param sourceId the source of the record behind the input
 * @param externalId the external id of that record
 * @param version the record version, or null
 * @param kind the record kind
 * @param signalFamily the signal family
 * @param severity the normalized severity (info, warning or critical)
 * @param title the display title
 * @param typeTitle the title of the input's type, for a seeded situation's title
 * @param observedAt when it was observed (the decision time)
 * @param geom the geometry, or null
 * @param regionCodes the 040 region codes (country and first-level region)
 * @param regionName a display name of its region, or null
 * @param entityIds the 098 entities it is linked to
 * @param text the text that topic keywords are matched against
 * @param theme the theme from the {@link ThemeTable}, or null when unknown
 * @param themeSource how the theme was found: {@code table}, {@code keyword}, {@code llm} or null
 * @param seedEligible true when the input may seed a new situation if it joins none
 * @param role core or context
 * @param evidenceRefs the records whose evidence groups and canonical events the input rests on
 * @param embeddingDocId the {@code embedding_document} id of the input's record, for text similarity
 */
public record LinkerInput(long seq, String memberType, String ref, String sourceId, String externalId, Integer version,
        String kind, String signalFamily, String severity, String title, String typeTitle, Instant observedAt,
        Geometry geom, Set<String> regionCodes, String regionName, Set<String> entityIds, String text, String theme,
        String themeSource, boolean seedEligible, String role, List<ai.foresignal.evidence.RecordRef> evidenceRefs,
        String embeddingDocId) {

    /** The key the input is linked by across situations: type and ref. */
    public String linkKey() {
        return memberType + "|" + ref;
    }

    /** The input's {@code ObjectRef}. */
    public String objectRef() {
        return SituationMemberTypes.objectRef(memberType, ref,
                SituationMemberTypes.recordRef(memberType) ? seq : null);
    }
}
