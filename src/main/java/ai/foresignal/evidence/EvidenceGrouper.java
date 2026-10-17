package ai.foresignal.evidence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Collapses the observations of one canonical event into evidence groups (prompt 116), deterministic for a
 * given membership. Two members are in the same group when:
 *
 * <ul>
 *   <li>they share an originator, or their originators share an independence group (equal
 *       {@link Originator#root() roots});
 *   <li>one originator aggregates (republishes) the other;
 *   <li>one member cites the other member's originator.
 * </ul>
 *
 * <p>A text-report member (GDELT, a feed kit, ReliefWeb) that cites an original source is judged by what it
 * cites, not by the aggregator that carried it: ten GDELT reports citing one outlet are one group, two
 * reports citing different outlets are two. A citation that names a registered originator joins that
 * originator's group.
 */
public final class EvidenceGrouper {

    private EvidenceGrouper() {}

    /**
     * One member.
     *
     * @param key the member's key (the link id, or a record key)
     * @param sourceId its source
     * @param originator its originator
     * @param citation its normalized cited source, or null
     * @param seq its seq
     */
    public record Member(String key, String sourceId, Originator originator, String citation, long seq) {}

    /**
     * One evidence group.
     *
     * @param root the originator root, or {@code cite:{source}} for an unregistered cited source
     * @param members the member keys, in input order
     * @param firstSeq the lowest member seq
     */
    public record Group(String root, List<String> members, long firstSeq) {}

    /** Groups {@code members}; groups are ordered by their first seq, then root. */
    public static List<Group> group(List<Member> members, OriginatorRegistry registry) {
        int n = members.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        String[] keys = new String[n];
        Originator[] effective = new Originator[n];
        for (int i = 0; i < n; i++) {
            Member m = members.get(i);
            if (m.citation() != null) {
                Optional<Originator> cited = registry.byCitation(m.citation());
                effective[i] = cited.orElse(null);
                keys[i] = cited.map(Originator::root).orElse("cite:" + m.citation());
            } else {
                effective[i] = m.originator();
                keys[i] = m.originator().root();
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (sameGroup(keys[i], keys[j], effective[i], effective[j], registry)) {
                    union(parent, i, j);
                }
            }
        }
        Map<Integer, List<Integer>> components = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            components.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);
        }
        List<Group> out = new ArrayList<>();
        for (List<Integer> component : components.values()) {
            List<String> memberKeys = new ArrayList<>();
            long firstSeq = Long.MAX_VALUE;
            for (int i : component) {
                memberKeys.add(members.get(i).key());
                firstSeq = Math.min(firstSeq, members.get(i).seq());
            }
            out.add(new Group(rootOf(component, keys, effective, registry), memberKeys, firstSeq));
        }
        out.sort((a, b) -> a.firstSeq() != b.firstSeq() ? Long.compare(a.firstSeq(), b.firstSeq())
                : a.root().compareTo(b.root()));
        return out;
    }

    private static boolean sameGroup(String keyA, String keyB, Originator a, Originator b, OriginatorRegistry registry) {
        if (Objects.equals(keyA, keyB)) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return registry.aggregates(a, b) || registry.aggregates(b, a);
    }

    /**
     * The group's root: the key of a member whose originator is republished by another member (the original),
     * else of a member that is not an aggregator, else the smallest key.
     */
    private static String rootOf(List<Integer> component, String[] keys, Originator[] effective,
            OriginatorRegistry registry) {
        TreeMap<String, Integer> candidates = new TreeMap<>();
        for (int i : component) {
            candidates.put(keys[i], i);
        }
        for (Map.Entry<String, Integer> entry : candidates.entrySet()) {
            Originator o = effective[entry.getValue()];
            if (o == null) {
                continue;
            }
            for (int j : component) {
                if (effective[j] != null && effective[j] != o && registry.aggregates(effective[j], o)) {
                    return entry.getKey();
                }
            }
        }
        for (Map.Entry<String, Integer> entry : candidates.entrySet()) {
            Originator o = effective[entry.getValue()];
            if (o != null && !Originator.DEFAULT_CLASS.equals(o.originatorClass())) {
                return entry.getKey();
            }
        }
        return candidates.firstKey();
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra != rb) {
            parent[Math.max(ra, rb)] = Math.min(ra, rb);
        }
    }
}
