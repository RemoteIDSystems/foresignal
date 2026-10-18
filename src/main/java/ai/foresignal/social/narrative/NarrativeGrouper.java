package ai.foresignal.social.narrative;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The evidence groups of one narrative (prompt 136, 116 evidence identity), deterministic for a given membership: 50,000
 * posts that descend from three reports count as three.
 *
 * <ul>
 *   <li>Members that repost or quote the same original post, link the same original post or the same external URL, or
 *       share a media fingerprint first seen in one post are one group. An original that is not a member still roots
 *       its descendants' group ({@code post:{externalId}}), as does an external URL ({@code link:{url}}) or a
 *       fingerprint first seen outside the narrative ({@code media:{fingerprintId}}).
 *   <li>Posts of one account are one group (the account is their originator, 135).
 *   <li>Every other member, an independent author posting their own text and media, is its own group, rooted at its
 *       originator ({@code social:{platform}:{authorKey}}).
 * </ul>
 * A group containing an outside root is named after it (an original post before a media fingerprint before a link,
 * then the smallest); otherwise after the originator of its earliest member. Groups are ordered by size, largest first,
 * then by their earliest member. Pure.
 */
public final class NarrativeGrouper {

    private NarrativeGrouper() {}

    /**
     * One member post.
     *
     * @param key the member key (the observation link id, else the post key)
     * @param postKey the post's {@code {platform}:{postId}} external id
     * @param url the post's public URL, or null
     * @param originatorId the account's originator ({@code social:{platform}:{authorKey}})
     * @param repostOf the reposted post's external id, or null
     * @param quoteOf the quoted post's external id, or null
     * @param links the normalized links
     * @param fingerprints the media fingerprint ids
     * @param createdAt the creation time
     * @param seq the version seq
     */
    public record Member(String key, String postKey, String url, String originatorId, String repostOf, String quoteOf,
            List<String> links, List<String> fingerprints, Instant createdAt, long seq) {

        /** Copies the lists. */
        public Member {
            links = links == null ? List.of() : List.copyOf(links);
            fingerprints = fingerprints == null ? List.of() : List.copyOf(fingerprints);
        }
    }

    /**
     * One evidence group.
     *
     * @param root the group root
     * @param members the member keys, earliest first
     * @param firstSeq the lowest member seq
     * @param firstAt the earliest member's creation time
     * @param firstPostKey the earliest member's post key
     */
    public record Group(String root, List<String> members, long firstSeq, Instant firstAt, String firstPostKey) {

        /** Copies the members. */
        public Group {
            members = List.copyOf(members);
        }

        /** The number of members. */
        public int size() {
            return members.size();
        }
    }

    /**
     * Groups the members.
     *
     * @param members the members
     * @param firstPostOfFingerprint fingerprint id to the post key it was first seen in (may name non-members)
     */
    public static List<Group> group(List<Member> members, Map<String, String> firstPostOfFingerprint) {
        List<Member> sorted = new ArrayList<>(members);
        sorted.sort(Comparator.comparing((Member m) -> m.createdAt() == null ? Instant.EPOCH : m.createdAt())
                .thenComparing(Member::key));
        Map<String, Member> byPost = new HashMap<>();
        Map<String, String> byUrl = new HashMap<>();
        for (Member m : sorted) {
            byPost.putIfAbsent(m.postKey(), m);
            if (m.url() != null) {
                byUrl.putIfAbsent(m.url(), m.postKey());
            }
        }
        UnionFind uf = new UnionFind();
        for (Member m : sorted) {
            String node = "m:" + m.postKey();
            uf.add(node);
            if (m.originatorId() != null) {
                uf.union(node, "o:" + m.originatorId());
            }
            for (String target : new String[] {m.repostOf(), m.quoteOf()}) {
                if (target == null) {
                    continue;
                }
                uf.union(node, byPost.containsKey(target) ? "m:" + target : "x0:post:" + target);
            }
            for (String link : m.links()) {
                String member = byUrl.get(link);
                if (member != null && !member.equals(m.postKey())) {
                    uf.union(node, "m:" + member);
                } else if (member == null) {
                    uf.union(node, "x2:link:" + link);
                }
            }
            for (String fp : m.fingerprints()) {
                String first = firstPostOfFingerprint == null ? null : firstPostOfFingerprint.get(fp);
                if (first != null && byPost.containsKey(first)) {
                    if (!first.equals(m.postKey())) {
                        uf.union(node, "m:" + first);
                    }
                } else {
                    uf.union(node, "x1:media:" + fp);
                }
            }
        }
        Map<String, List<Member>> sets = new LinkedHashMap<>();
        for (Member m : sorted) {
            sets.computeIfAbsent(uf.find("m:" + m.postKey()), k -> new ArrayList<>()).add(m);
        }
        Map<String, String> outside = new HashMap<>();
        for (String node : uf.nodes()) {
            if (node.startsWith("x")) {
                String root = uf.find(node);
                String current = outside.get(root);
                if (current == null || node.compareTo(current) < 0) {
                    outside.put(root, node);
                }
            }
        }
        List<Group> out = new ArrayList<>();
        Map<String, Integer> seenRoots = new HashMap<>();
        for (Map.Entry<String, List<Member>> e : sets.entrySet()) {
            List<Member> list = e.getValue();
            Member first = list.get(0);
            String external = outside.get(e.getKey());
            String root = external != null ? external.substring(3)
                    : first.originatorId() != null ? first.originatorId() : "post:" + first.postKey();
            int dup = seenRoots.merge(root, 1, Integer::sum);
            if (dup > 1) {
                root = root + "#" + dup;
            }
            List<String> keys = new ArrayList<>();
            long firstSeq = Long.MAX_VALUE;
            for (Member m : list) {
                keys.add(m.key());
                firstSeq = Math.min(firstSeq, m.seq());
            }
            out.add(new Group(root, keys, firstSeq, first.createdAt(), first.postKey()));
        }
        out.sort(Comparator.comparingInt(Group::size).reversed()
                .thenComparing(g -> g.firstAt() == null ? Instant.EPOCH : g.firstAt())
                .thenComparing(Group::root));
        return out;
    }

    /** The share of posts in the three largest groups (0 without posts). */
    public static double originConcentration(List<Group> groups) {
        int total = 0;
        for (Group g : groups) {
            total += g.size();
        }
        if (total == 0) {
            return 0;
        }
        List<Group> sorted = new ArrayList<>(groups);
        sorted.sort(Comparator.comparingInt(Group::size).reversed());
        int top = 0;
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            top += sorted.get(i).size();
        }
        return Math.round(top * 10_000.0 / total) / 10_000.0;
    }

    /** A union-find over string nodes; the smaller node becomes the representative. */
    static final class UnionFind {
        private final Map<String, String> parent = new LinkedHashMap<>();

        void add(String node) {
            parent.putIfAbsent(node, node);
        }

        String find(String node) {
            add(node);
            String root = node;
            while (!parent.get(root).equals(root)) {
                root = parent.get(root);
            }
            String cur = node;
            while (!cur.equals(root)) {
                String next = parent.get(cur);
                parent.put(cur, root);
                cur = next;
            }
            return root;
        }

        void union(String a, String b) {
            String ra = find(a);
            String rb = find(b);
            if (!ra.equals(rb)) {
                if (ra.compareTo(rb) < 0) {
                    parent.put(rb, ra);
                } else {
                    parent.put(ra, rb);
                }
            }
        }

        List<String> nodes() {
            return new ArrayList<>(parent.keySet());
        }
    }
}
