package ai.foresignal.situation;

import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.locationtech.jts.geom.Geometry;

/**
 * The JSON shapes of situations (prompt 118, {@code docs/OpenAPI.json}): {@code SituationSummary} for lists and
 * SSE, the full {@code Situation} with its evidence (members grouped by signal family, corroboration roll-up),
 * inference (status, severity, summary) and labeled speculation (the brief) sections, {@code SituationMember} and
 * {@code SituationTimelineEntry}.
 */
public final class SituationMapper {

    /** The members listed per family in the evidence section. */
    static final int MEMBERS_PER_FAMILY = 20;

    private static final GeoJsonWriter GEOJSON = new GeoJsonWriter();

    private SituationMapper() {}

    /** A GeoJSON geometry map, or null. */
    public static Map<String, Object> geometry(Geometry geometry) {
        return geometry == null || geometry.isEmpty() ? null : GEOJSON.write(geometry);
    }

    /** The {@code SituationSummary} of a situation, its summary rendered at {@code now}. */
    public static Map<String, Object> summary(SituationEntity situation, Instant now) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", situation.getId());
        out.put("title", situation.getTitle());
        out.put("theme", situation.getTheme());
        out.put("status", situation.getStatus());
        out.put("severity", situation.getSeverity());
        out.put("severityOverridden", situation.getSeverityOverride() != null);
        out.put("regionCodes", situation.getRegionCodes());
        out.put("bbox", SituationGeometry.bbox(situation.getGeom()));
        out.put("centroid", geometry(situation.getCentroid()));
        out.put("startedAt", situation.getStartedAt());
        out.put("lastActivityAt", situation.getLastActivityAt());
        out.put("closedAt", situation.getClosedAt());
        out.put("memberCount", situation.getMemberCount());
        out.put("signalFamilies", situation.getSignalFamilies());
        out.put("corroborationShare", situation.getCorroborationShare());
        out.put("summary", rerender(situation, now));
        out.put("origin", situation.getOrigin());
        out.put("mergedInto", situation.getMergedInto());
        out.put("private", situation.isPrivateSituation());
        out.put("version", situation.getVersion());
        out.put("updatedAt", situation.getUpdatedAt());
        return out;
    }

    /**
     * The full {@code Situation}: the summary fields, then evidence, inference and speculation.
     *
     * @param members every member of the situation
     * @param rollup the corroboration roll-up of the members
     * @param coverageCaveats extra caveats (for example {@code LOW_REPORTING_DENSITY}), may be empty
     */
    public static Map<String, Object> full(SituationEntity situation, List<SituationMemberEntity> members,
            CorroborationRollup.Result rollup, int coreFamilies, List<Map<String, Object>> coverageCaveats,
            Instant now) {
        Map<String, Object> out = summary(situation, now);
        out.put("severityOverride", situation.getSeverityOverride());
        out.put("geometry", geometry(situation.getGeom()));
        out.put("topicIds", situation.getTopicIds());
        out.put("entityIds", situation.getEntityIds());
        out.put("investigationIds", situation.getInvestigationIds());
        out.put("linkerVersion", situation.getLinkerVersion());
        out.put("createdAt", situation.getCreatedAt());
        out.put("evidence", evidence(members, rollup));
        Map<String, Object> inference = new LinkedHashMap<>();
        inference.put("summary", rerender(situation, now));
        inference.put("summaryTemplateVersion", SummaryTemplate.VERSION);
        inference.put("statusBasis", SituationLifecycle.basis(situation.getStatus(), coreFamilies,
                rollup.independentGroups(), false));
        inference.put("severityBasis", situation.getSeverityOverride() != null
                ? "operator override " + situation.getSeverityOverride() + " (see the situation log)"
                : "maximum severity of the core members");
        Map<String, Object> linker = new LinkedHashMap<>();
        linker.put("name", SituationScorer.NAME);
        linker.put("version", situation.getLinkerVersion() == null ? SituationScorer.VERSION
                : situation.getLinkerVersion());
        linker.put("params", LinkerWeights.DEFAULT.toMap());
        inference.put("linker", linker);
        inference.put("manifestId", situation.getManifestId());
        out.put("inference", inference);
        Map<String, Object> speculation = new LinkedHashMap<>();
        speculation.put("label", "speculation");
        speculation.put("textOrigin", "llm");
        speculation.put("brief", situation.getBrief());
        speculation.put("briefGeneratedAt", situation.getBriefGeneratedAt());
        speculation.put("briefModel", situation.getBriefModel());
        speculation.put("manifestId", situation.getBriefManifestId());
        out.put("speculation", speculation);
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (rollup.uncorroboratedOnly()) {
            caveats.add(Map.of("code", "UNCORROBORATED_ONLY", "text",
                    "This situation rests only on uncorroborated reports (" + rollup.uncorroborated() + ")."));
        }
        if (coverageCaveats != null) {
            caveats.addAll(coverageCaveats);
        }
        out.put("caveats", caveats);
        out.put("coverage", null);
        out.put("social", null);
        return out;
    }

    /** The evidence section: member counts, members grouped by family (most recent first), the roll-up. */
    static Map<String, Object> evidence(List<SituationMemberEntity> members, CorroborationRollup.Result rollup) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("memberCounts", SituationDeriver.memberCounts(members));
        Map<String, List<SituationMemberEntity>> byFamily = new TreeMap<>();
        for (SituationMemberEntity member : members) {
            byFamily.computeIfAbsent(member.getSignalFamily() == null ? "unknown" : member.getSignalFamily(),
                    k -> new ArrayList<>()).add(member);
        }
        List<Map<String, Object>> families = new ArrayList<>();
        for (Map.Entry<String, List<SituationMemberEntity>> entry : byFamily.entrySet()) {
            List<SituationMemberEntity> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(recentFirst());
            Map<String, Object> family = new LinkedHashMap<>();
            family.put("family", entry.getKey());
            family.put("count", sorted.size());
            family.put("coreCount", sorted.stream().filter(SituationMemberEntity::isCore).count());
            family.put("members", sorted.stream().limit(MEMBERS_PER_FAMILY).map(SituationMapper::member).toList());
            family.put("more", sorted.size() > MEMBERS_PER_FAMILY);
            families.add(family);
        }
        out.put("families", families);
        Map<String, Object> corroboration = new LinkedHashMap<>();
        corroboration.put("corroborated", rollup.corroborated());
        corroboration.put("uncorroborated", rollup.uncorroborated());
        corroboration.put("unknown", rollup.unknown());
        corroboration.put("notApplicable", rollup.notApplicable());
        corroboration.put("share", rollup.share());
        corroboration.put("uncorroboratedOnly", rollup.uncorroboratedOnly());
        out.put("corroboration", corroboration);
        return out;
    }

    /** Most recently observed first, then by id. */
    public static Comparator<SituationMemberEntity> recentFirst() {
        return Comparator.comparing((SituationMemberEntity m) -> m.getObservedAt() == null ? Instant.EPOCH
                : m.getObservedAt()).reversed().thenComparing(SituationMemberEntity::getId);
    }

    /** The {@code SituationMember} of a member. */
    public static Map<String, Object> member(SituationMemberEntity member) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", member.getId());
        out.put("situationId", member.getSituationId());
        out.put("memberType", member.getMemberType());
        out.put("ref", member.getRef());
        out.put("objectRef", SituationMemberTypes.objectRef(member.getMemberType(), member.getRef(),
                SituationMemberTypes.recordRef(member.getMemberType()) ? member.getSeq() : null));
        out.put("seq", member.getSeq());
        out.put("version", member.getVersion());
        out.put("kind", member.getKind());
        out.put("signalFamily", member.getSignalFamily());
        out.put("title", member.getTitle());
        out.put("severity", member.getSeverity());
        out.put("observedAt", member.getObservedAt());
        out.put("role", member.getRole());
        out.put("joinReason", member.getJoinReason());
        out.put("corroboration", member.getCorroboration());
        out.put("addedAt", member.getAddedAt());
        out.put("addedBy", member.getAddedBy());
        return out;
    }

    /** The {@code SituationTimelineEntry} of a member. */
    public static Map<String, Object> timeline(SituationMemberEntity member) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("at", member.getObservedAt() == null ? member.getAddedAt() : member.getObservedAt());
        out.put("entryType", "member");
        out.put("member", member(member));
        out.put("action", null);
        out.put("actor", null);
        out.put("before", null);
        out.put("after", null);
        return out;
    }

    /** The {@code SituationTimelineEntry} of a log row. */
    public static Map<String, Object> timeline(SituationLogEntity log) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("at", log.getAt());
        out.put("entryType", "log");
        out.put("member", null);
        out.put("action", log.getAction());
        out.put("actor", log.getActor());
        out.put("before", log.getBefore());
        out.put("after", log.getAfter());
        return out;
    }

    /** The summary rendered at {@code now} from the stored stem (the stored text when it has no stem form). */
    static String rerender(SituationEntity situation, Instant now) {
        String stored = situation.getSummary();
        int last = stored == null ? -1 : stored.lastIndexOf(" Last activity ");
        if (last < 0 || now == null) {
            return stored == null ? "" : stored;
        }
        return stored.substring(0, last) + " Last activity "
                + SummaryTemplate.ago(situation.getLastActivityAt(), now) + ".";
    }

    /** The summary without its last sentence (what the {@code foresignal-intel} record carries). */
    public static String stem(SituationEntity situation) {
        String stored = situation.getSummary();
        int last = stored == null ? -1 : stored.lastIndexOf(" Last activity ");
        return last < 0 ? (stored == null ? "" : stored) : stored.substring(0, last);
    }
}
