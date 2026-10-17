package ai.foresignal.situation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

/**
 * Re-derives a situation's inference from its members (prompt 118 §4): status ({@link SituationLifecycle}),
 * severity (the maximum core-member severity, unless overridden), geometry, regions, entities, the start and the
 * last activity, the private flag, the corroboration roll-up and the deterministic summary. Members are evidence
 * and are never changed here, except for their stored corroboration status.
 */
@Component
public class SituationDeriver {

    private final CorroborationRollup corroboration;

    /** Creates the deriver. */
    public SituationDeriver(CorroborationRollup corroboration) {
        this.corroboration = corroboration;
    }

    /**
     * What a derivation changed.
     *
     * @param previousStatus the status before
     * @param previousSeverity the severity before
     * @param coreFamilies distinct signal families of the core members
     * @param independentGroups independent evidence groups of the core members
     * @param coreCount the number of core members
     * @param rollup the corroboration roll-up
     */
    public record Derivation(String previousStatus, String previousSeverity, int coreFamilies, int independentGroups,
            int coreCount, CorroborationRollup.Result rollup) {

        /** True when the status changed. */
        public boolean statusChanged(SituationEntity situation) {
            return !situation.getStatus().equals(previousStatus);
        }

        /** True when the severity was raised. */
        public boolean escalated(SituationEntity situation) {
            return Severities.rank(situation.getSeverity()) > Severities.rank(previousSeverity);
        }

        /** True when the severity changed in either direction. */
        public boolean severityChanged(SituationEntity situation) {
            return !situation.getSeverity().equals(previousSeverity);
        }
    }

    /**
     * Re-derives {@code situation} from {@code members}.
     *
     * @param newCoreMember true when the change added a core member (reopens a dormant situation)
     * @param now the derivation time (the summary's "Last activity")
     */
    public Derivation derive(SituationEntity situation, List<SituationMemberEntity> members, boolean newCoreMember,
            Instant now) {
        String previousStatus = situation.getStatus();
        String previousSeverity = situation.getSeverity();
        Set<String> families = new TreeSet<>();
        Set<String> regions = new TreeSet<>();
        Set<String> entities = new TreeSet<>(situation.getEntityIds());
        List<Geometry> geometries = new ArrayList<>();
        String severity = null;
        Instant started = null;
        Instant last = null;
        boolean anyPrivate = false;
        int core = 0;
        for (SituationMemberEntity member : members) {
            anyPrivate = anyPrivate || member.isPrivateMember();
            regions.addAll(member.getRegionCodes());
            entities.addAll(member.getEntityIds());
            if (member.getGeom() != null) {
                geometries.add(member.getGeom());
            }
            Instant observed = member.getObservedAt() == null ? member.getAddedAt() : member.getObservedAt();
            started = started == null || (observed != null && observed.isBefore(started)) ? observed : started;
            if (!member.isCore()) {
                continue;
            }
            core++;
            if (member.getSignalFamily() != null) {
                families.add(member.getSignalFamily());
            }
            severity = Severities.max(severity, member.getSeverity());
            last = last == null || (observed != null && observed.isAfter(last)) ? observed : last;
        }
        CorroborationRollup.Result rollup = corroboration.rollUp(members);
        for (SituationMemberEntity member : members) {
            String status = rollup.statuses().get(member.getId());
            if (status != null) {
                member.setCorroboration(status);
            }
        }
        if (!regions.isEmpty()) {
            // Regions follow the members; a situation without located members keeps the regions it was given.
            situation.setRegionCodes(new ArrayList<>(regions));
        }
        situation.setEntityIds(new ArrayList<>(entities));
        Geometry geom = SituationGeometry.of(geometries);
        situation.setGeom(geom);
        situation.setCentroid(SituationGeometry.centroid(geom));
        situation.setAreaKm2(SituationGeometry.areaKm2(geom));
        if (started != null && (situation.getStartedAt() == null || started.isBefore(situation.getStartedAt()))) {
            situation.setStartedAt(started);
        }
        if (last != null && (situation.getLastActivityAt() == null || last.isAfter(situation.getLastActivityAt()))) {
            situation.setLastActivityAt(last);
        }
        situation.setPrivateSituation(anyPrivate);
        String derivedSeverity = severity == null ? "info" : severity;
        situation.setSeverity(situation.getSeverityOverride() != null ? situation.getSeverityOverride() : derivedSeverity);
        situation.setStatus(SituationLifecycle.afterMembers(situation.getStatus(), families.size(),
                rollup.independentGroups(), newCoreMember));
        situation.setCorroborationShare(rollup.share());
        situation.setMemberCount(members.size());
        situation.setSignalFamilies(families);
        situation.setSummary(SummaryTemplate.render(facts(situation, members, families.size(), rollup), now));
        return new Derivation(previousStatus, previousSeverity, families.size(), rollup.independentGroups(), core,
                rollup);
    }

    /** The summary inputs of a situation and its members. */
    public static SummaryTemplate.Facts facts(SituationEntity situation, List<SituationMemberEntity> members,
            int families, CorroborationRollup.Result rollup) {
        List<String> titles = members.stream().filter(SituationMemberEntity::isCore)
                .sorted(Comparator.comparing((SituationMemberEntity m) -> m.getObservedAt() == null ? Instant.EPOCH
                        : m.getObservedAt()).reversed().thenComparing(SituationMemberEntity::getId))
                .map(SituationMemberEntity::getTitle).filter(t -> t != null && !t.isBlank()).limit(3).toList();
        Set<String> allFamilies = new TreeSet<>();
        for (SituationMemberEntity member : members) {
            if (member.getSignalFamily() != null) {
                allFamilies.add(member.getSignalFamily());
            }
        }
        return new SummaryTemplate.Facts(members.size(), Math.max(families, allFamilies.size()),
                situation.getRegionCodes(), situation.getStartedAt(), titles, rollup.corroborated(),
                rollup.uncorroborated(), situation.getLastActivityAt());
    }

    /** Member counts by type and by signal family, as stored in snapshots and returned as evidence. */
    public static Map<String, Object> memberCounts(List<SituationMemberEntity> members) {
        Map<String, Integer> byType = new TreeMap<>();
        Map<String, Integer> byFamily = new TreeMap<>();
        for (SituationMemberEntity member : members) {
            byType.merge(member.getMemberType(), 1, Integer::sum);
            byFamily.merge(member.getSignalFamily() == null ? "unknown" : member.getSignalFamily(), 1, Integer::sum);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("byType", byType);
        out.put("byFamily", byFamily);
        return out;
    }
}
