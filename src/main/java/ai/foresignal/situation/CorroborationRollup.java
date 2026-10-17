package ai.foresignal.situation;

import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceService;
import ai.foresignal.evidence.RecordRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The 116 corroboration roll-up of a situation (prompt 118 §4). For each member, {@link EvidenceService} gives the
 * canonical events and evidence groups of the records it rests on ({@code evidence_refs}; for an intelligence
 * event, its pinned member records):
 *
 * <ul>
 *   <li>{@code corroborated} — one of its canonical events has ≥ 2 independent evidence groups;
 *   <li>{@code uncorroborated} — its canonical events have one;
 *   <li>{@code unknown} — an event-class record the resolver has not linked yet;
 *   <li>{@code n/a} — kinds without canonical events (findings, anomalies, investigations, behavior signals).
 * </ul>
 *
 * <p>{@code corroborationShare} = corroborated ÷ (corroborated + uncorroborated), null when that is 0/0. The
 * independent evidence groups of the situation are the distinct originator roots of every core member's records
 * (unlinked records count by their source's root), so an echoed report never counts twice (EVD-1).
 */
@Component
public class CorroborationRollup {

    /** Corroborated by ≥ 2 independent evidence groups. */
    public static final String CORROBORATED = "corroborated";

    /** One evidence group. */
    public static final String UNCORROBORATED = "uncorroborated";

    /** Not linked yet. */
    public static final String UNKNOWN = "unknown";

    /** No canonical event applies. */
    public static final String NOT_APPLICABLE = "n/a";

    private final ObjectProvider<EvidenceService> evidence;
    private final ObjectProvider<CanonicalRules> rules;
    private final JdbcTemplate jdbc;

    /**
     * The roll-up of a member set.
     *
     * @param statuses member id → corroboration status
     * @param corroborated corroborated members
     * @param uncorroborated uncorroborated members
     * @param unknown members not linked yet
     * @param notApplicable members without canonical events
     * @param independentGroups the independent evidence groups of the core members
     */
    public record Result(Map<String, String> statuses, int corroborated, int uncorroborated, int unknown,
            int notApplicable, int independentGroups) {

        /** corroborated ÷ (corroborated + uncorroborated), or null. */
        public Double share() {
            int total = corroborated + uncorroborated;
            return total == 0 ? null : Math.round(1000.0 * corroborated / total) / 1000.0;
        }

        /** True when the situation rests only on uncorroborated reports. */
        public boolean uncorroboratedOnly() {
            return corroborated == 0 && uncorroborated > 0;
        }
    }

    /** Creates the component. */
    public CorroborationRollup(ObjectProvider<EvidenceService> evidence, ObjectProvider<CanonicalRules> rules,
            JdbcTemplate jdbc) {
        this.evidence = evidence;
        this.rules = rules;
        this.jdbc = jdbc;
    }

    /** Rolls up {@code members}. */
    public Result rollUp(Collection<SituationMemberEntity> members) {
        Map<String, List<RecordRef>> refsByMember = new LinkedHashMap<>();
        List<RecordRef> all = new ArrayList<>();
        for (SituationMemberEntity member : members) {
            List<RecordRef> refs = refsOf(member);
            refsByMember.put(member.getId(), refs);
            all.addAll(refs);
        }
        EvidenceService service = evidence.getIfAvailable();
        Map<String, EvidenceService.GroupKey> keys = Map.of();
        Map<String, Integer> groupsByEvent = Map.of();
        if (service != null && !all.isEmpty()) {
            try {
                keys = service.groupKeys(all);
                Set<String> events = new LinkedHashSet<>();
                for (EvidenceService.GroupKey key : keys.values()) {
                    if (key.canonicalEventId() != null) {
                        events.add(key.canonicalEventId());
                    }
                }
                groupsByEvent = service.independentGroupsOfEvents(events);
            } catch (RuntimeException e) {
                keys = Map.of();
                groupsByEvent = Map.of();
            }
        }
        Set<String> eventKinds = eventKinds();
        Map<String, String> statuses = new LinkedHashMap<>();
        int corroborated = 0;
        int uncorroborated = 0;
        int unknown = 0;
        int na = 0;
        Set<String> roots = new LinkedHashSet<>();
        for (SituationMemberEntity member : members) {
            List<RecordRef> refs = refsByMember.get(member.getId());
            int best = 0;
            boolean linked = false;
            for (RecordRef ref : refs) {
                EvidenceService.GroupKey key = keys.get(EvidenceService.recordKey(ref.sourceId(), ref.externalId()));
                if (key == null) {
                    if (member.isCore() && ref.sourceId() != null) {
                        roots.add("source:" + ref.sourceId());
                    }
                    continue;
                }
                if (member.isCore()) {
                    roots.add(key.root());
                }
                if (key.canonicalEventId() != null) {
                    linked = true;
                    best = Math.max(best, groupsByEvent.getOrDefault(key.canonicalEventId(), 1));
                }
            }
            String status;
            if (linked) {
                status = best >= 2 ? CORROBORATED : UNCORROBORATED;
            } else if (!refs.isEmpty() && member.getKind() != null && eventKinds.contains(member.getKind())
                    && !SituationMemberTypes.INTEL_EVENT.equals(member.getMemberType())) {
                status = UNKNOWN;
            } else {
                status = NOT_APPLICABLE;
            }
            statuses.put(member.getId(), status);
            switch (status) {
                case CORROBORATED -> corroborated++;
                case UNCORROBORATED -> uncorroborated++;
                case UNKNOWN -> unknown++;
                default -> na++;
            }
        }
        return new Result(statuses, corroborated, uncorroborated, unknown, na, roots.size());
    }

    /** The records a member rests on: its stored refs, or an intelligence event's pinned member records. */
    List<RecordRef> refsOf(SituationMemberEntity member) {
        List<RecordRef> out = new ArrayList<>();
        if (SituationMemberTypes.INTEL_EVENT.equals(member.getMemberType())) {
            try {
                jdbc.query("SELECT source_id, external_id FROM intelligence_event_member WHERE event_id = ?"
                                + " AND role <> 'contradicting' ORDER BY id",
                        rs -> {
                            out.add(new RecordRef(rs.getString(1), rs.getString(2), null));
                        }, member.getRef());
            } catch (RuntimeException e) {
                // The intelligence tables are absent: fall back to the stored refs.
            }
        }
        if (out.isEmpty()) {
            for (String ref : member.getEvidenceRefs()) {
                int slash = ref.indexOf('/');
                if (slash > 0) {
                    out.add(new RecordRef(ref.substring(0, slash), ref.substring(slash + 1), null));
                }
            }
        }
        return out;
    }

    private Set<String> eventKinds() {
        CanonicalRules canonical = rules.getIfAvailable();
        try {
            return canonical == null ? Set.of() : canonical.kinds();
        } catch (RuntimeException e) {
            return Set.of();
        }
    }
}
