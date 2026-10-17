package ai.foresignal.evidence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Evidence identity for the rest of the intelligence layer (prompt 116; used by 099, 101, 118, 125 and later):
 * which canonical event and evidence group a record belongs to, and how many independent evidence groups a
 * set of records represents. Independence is counted in evidence groups, never in records or sources.
 *
 * <p>A record the resolver has not linked (a kind that is not event-class, or not yet processed) counts by its
 * source's originator root: records that trace to one originator are never independent of each other.
 */
@Service
public class EvidenceService {

    private final JdbcTemplate jdbc;
    private final OriginatorRegistry originators;

    /** Creates the service. */
    public EvidenceService(JdbcTemplate jdbc, OriginatorRegistry originators) {
        this.jdbc = jdbc;
        this.originators = originators;
    }

    /**
     * The evidence identity of a record.
     *
     * @param sourceId the source
     * @param externalId the record
     * @param canonicalEventId the canonical event, or null when not linked
     * @param groupId the evidence group, or null when not linked
     * @param root the group's originator root, or the source's originator root when not linked
     */
    public record GroupKey(String sourceId, String externalId, String canonicalEventId, String groupId, String root) {

        /** True when the record is linked to a canonical event. */
        public boolean linked() {
            return groupId != null;
        }

        /** The key two records share exactly when they are in one evidence group. */
        public String key() {
            return groupId != null ? "group:" + groupId : "root:" + root;
        }
    }

    /** The evidence group of {@code ref} ({@code record:{source}/{externalId}[@seq]}). */
    public Optional<GroupKey> groupOf(String ref) {
        RecordRef parsed = RecordRef.parse(ref);
        GroupKey key = groupKeys(List.of(parsed)).get(recordKey(parsed.sourceId(), parsed.externalId()));
        return Optional.ofNullable(key).filter(GroupKey::linked);
    }

    /** The canonical event {@code ref} is linked to (the newest when several), if any. */
    public Optional<String> canonicalEventOf(String ref) {
        return groupOf(ref).map(GroupKey::canonicalEventId);
    }

    /**
     * How many independent evidence groups {@code refs} represent: the distinct originator roots of their
     * groups (unlinked records count by their source's originator root).
     */
    public int independentCount(Collection<String> refs) {
        List<RecordRef> parsed = new ArrayList<>();
        for (String ref : refs) {
            parsed.add(RecordRef.parse(ref));
        }
        Set<String> roots = new LinkedHashSet<>();
        for (GroupKey key : groupKeys(parsed).values()) {
            roots.add(key.root());
        }
        return roots.size();
    }

    /** True when two records are in one evidence group, or trace to one originator root. */
    public boolean sameGroup(String refA, String refB) {
        RecordRef a = RecordRef.parse(refA);
        RecordRef b = RecordRef.parse(refB);
        Map<String, GroupKey> keys = groupKeys(List.of(a, b));
        GroupKey ka = keys.get(recordKey(a.sourceId(), a.externalId()));
        GroupKey kb = keys.get(recordKey(b.sourceId(), b.externalId()));
        if (ka == null || kb == null) {
            return false;
        }
        if (ka.groupId() != null && ka.groupId().equals(kb.groupId())) {
            return true;
        }
        return Objects.equals(ka.root(), kb.root());
    }

    /**
     * The evidence identity of each record, keyed by {@link #recordKey(String, String)}; one query for the
     * whole set.
     */
    public Map<String, GroupKey> groupKeys(Collection<RecordRef> refs) {
        Map<String, GroupKey> out = new LinkedHashMap<>();
        if (refs.isEmpty()) {
            return out;
        }
        List<String> sources = new ArrayList<>();
        List<String> externals = new ArrayList<>();
        for (RecordRef ref : refs) {
            sources.add(ref.sourceId());
            externals.add(ref.externalId());
        }
        jdbc.query("SELECT l.source_id, l.external_id, l.canonical_event_id, l.evidence_group_id, g.originator_root"
                        + " FROM observation_link l JOIN unnest(?::text[], ?::text[]) AS r(source_id, external_id)"
                        + " ON r.source_id = l.source_id AND r.external_id = l.external_id"
                        + " JOIN canonical_event c ON c.id = l.canonical_event_id AND c.status <> 'merged'"
                        + " LEFT JOIN evidence_group g ON g.id = l.evidence_group_id ORDER BY l.linked_at",
                rs -> {
                    String source = rs.getString(1);
                    String external = rs.getString(2);
                    String root = rs.getString(5);
                    if (root == null) {
                        root = originators.resolve(source).root();
                    }
                    out.put(recordKey(source, external), new GroupKey(source, external, rs.getString(3),
                            rs.getString(4), root));
                }, sources.toArray(String[]::new), externals.toArray(String[]::new));
        for (RecordRef ref : refs) {
            out.computeIfAbsent(recordKey(ref.sourceId(), ref.externalId()), k -> new GroupKey(ref.sourceId(),
                    ref.externalId(), null, null, originators.resolve(ref.sourceId()).root()));
        }
        return out;
    }

    /** The map key of a record. */
    public static String recordKey(String sourceId, String externalId) {
        return sourceId + "/" + externalId;
    }
}
