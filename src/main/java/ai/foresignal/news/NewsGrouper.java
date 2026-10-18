package ai.foresignal.news;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The evidence groups of one news story (prompt 133), deterministic for a given membership: syndication never inflates
 * support (NEWS-2).
 *
 * <ol>
 *   <li>Articles from different outlets whose normalized titles have a Jaccard of at least {@code wireJaccard} (0.9)
 *       are near-identical, and near-identical articles form a reprint cluster (transitively).
 *   <li>In a cluster with a wire's own article, that wire's article and every reprint from an outlet that syndicates
 *       it (its {@code aggregates} list the wire, or list no wire at all) form one group {@code wire:{originator}};
 *       the reprints are flagged {@code syndicated}. With several wires in a cluster the earliest wire is the origin,
 *       and the other wires keep their own groups.
 *   <li>In a cluster without a wire article, reprints from outlets that all name a common wire in {@code aggregates}
 *       join {@code wire:{that wire}}; otherwise they form one group {@code syndicated:{first member}} of unknown
 *       origin, every member after the first flagged {@code syndicated}.
 *   <li>Every other article is in its outlet's group {@code outlet:{outletId}} (several articles of one outlet are one
 *       group), or {@code domain:{domain}} without an outlet.
 * </ol>
 * Groups are ordered by their first seq, then root. Pure.
 */
public final class NewsGrouper {

    /** The most members the reprint clustering compares pairwise; later members keep their outlet groups. */
    static final int MAX_PAIRWISE = 2_000;

    private NewsGrouper() {}

    /**
     * One article of the story.
     *
     * @param key the member key (the observation link id)
     * @param outletId the outlet, or null
     * @param domain the domain
     * @param wireOriginator the wire's originator when the outlet is a wire, else null
     * @param aggregates the wire originators the outlet syndicates (empty: any wire)
     * @param tokens the normalized title tokens
     * @param seq the version seq
     */
    public record Member(String key, String outletId, String domain, String wireOriginator, List<String> aggregates,
            List<String> tokens, long seq) {

        /** Copies the lists. */
        public Member {
            aggregates = aggregates == null ? List.of() : List.copyOf(aggregates);
            tokens = tokens == null ? List.of() : List.copyOf(tokens);
        }

        String outletKey() {
            return outletId != null ? "outlet:" + outletId : "domain:" + domain;
        }

        boolean syndicates(String wire) {
            return wireOriginator == null && (aggregates.isEmpty() || aggregates.contains(wire));
        }
    }

    /**
     * One evidence group.
     *
     * @param root the group key ({@code wire:…}, {@code outlet:…}, {@code domain:…}, {@code syndicated:…})
     * @param members the member keys, in input order
     * @param firstSeq the lowest member seq
     * @param wireOriginator the wire the group originates from, or null
     * @param syndicated the members that are reprints
     */
    public record Group(String root, List<String> members, long firstSeq, String wireOriginator,
            Set<String> syndicated) {

        /** Copies the collections. */
        public Group {
            members = List.copyOf(members);
            syndicated = Set.copyOf(syndicated);
        }
    }

    /** Groups the members of a story. */
    public static List<Group> group(List<Member> members, double wireJaccard) {
        int n = members.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        int limit = Math.min(n, MAX_PAIRWISE);
        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                Member a = members.get(i);
                Member b = members.get(j);
                if (!a.outletKey().equals(b.outletKey()) && !a.tokens().isEmpty()
                        && TitleTokens.jaccard(a.tokens(), b.tokens()) >= wireJaccard) {
                    union(parent, i, j);
                }
            }
        }
        Map<Integer, List<Integer>> clusters = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            clusters.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);
        }
        String[] rootOf = new String[n];
        String[] wireOf = new String[n];
        boolean[] syndicated = new boolean[n];
        for (List<Integer> cluster : clusters.values()) {
            Set<String> outlets = new HashSet<>();
            for (int i : cluster) {
                outlets.add(members.get(i).outletKey());
            }
            if (cluster.size() < 2 || outlets.size() < 2) {
                continue;
            }
            List<Integer> bySeq = new ArrayList<>(cluster);
            bySeq.sort(Comparator.comparingLong((Integer i) -> members.get(i).seq()).thenComparing(
                    i -> members.get(i).key()));
            String origin = null;
            for (int i : bySeq) {
                if (members.get(i).wireOriginator() != null) {
                    origin = members.get(i).wireOriginator();
                    break;
                }
            }
            if (origin != null) {
                for (int i : cluster) {
                    Member m = members.get(i);
                    if (m.wireOriginator() != null) {
                        rootOf[i] = "wire:" + m.wireOriginator();
                        wireOf[i] = m.wireOriginator();
                    } else if (m.syndicates(origin)) {
                        rootOf[i] = "wire:" + origin;
                        wireOf[i] = origin;
                        syndicated[i] = true;
                    }
                }
                continue;
            }
            Set<String> common = null;
            for (int i : cluster) {
                Member m = members.get(i);
                if (m.aggregates().isEmpty()) {
                    common = Set.of();
                    break;
                }
                if (common == null) {
                    common = new LinkedHashSet<>(m.aggregates());
                } else {
                    common.retainAll(m.aggregates());
                }
            }
            if (common != null && !common.isEmpty()) {
                String wire = java.util.Collections.min(common);
                for (int i : cluster) {
                    rootOf[i] = "wire:" + wire;
                    wireOf[i] = wire;
                    syndicated[i] = true;
                }
                continue;
            }
            int first = bySeq.get(0);
            String root = "syndicated:" + members.get(first).key();
            for (int i : cluster) {
                rootOf[i] = root;
                syndicated[i] = i != first;
            }
        }
        Map<String, List<Integer>> groups = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            String root = rootOf[i] != null ? rootOf[i] : members.get(i).wireOriginator() != null
                    ? "wire:" + members.get(i).wireOriginator() : members.get(i).outletKey();
            if (rootOf[i] == null && members.get(i).wireOriginator() != null) {
                wireOf[i] = members.get(i).wireOriginator();
            }
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
        }
        List<Group> out = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> e : groups.entrySet()) {
            List<String> keys = new ArrayList<>();
            Set<String> synd = new LinkedHashSet<>();
            long firstSeq = Long.MAX_VALUE;
            String wire = null;
            for (int i : e.getValue()) {
                Member m = members.get(i);
                keys.add(m.key());
                firstSeq = Math.min(firstSeq, m.seq());
                if (syndicated[i]) {
                    synd.add(m.key());
                }
                if (wireOf[i] != null) {
                    wire = wireOf[i];
                }
            }
            out.add(new Group(e.getKey(), keys, firstSeq, wire, synd));
        }
        out.sort(Comparator.comparingLong(Group::firstSeq).thenComparing(Group::root));
        return out;
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
