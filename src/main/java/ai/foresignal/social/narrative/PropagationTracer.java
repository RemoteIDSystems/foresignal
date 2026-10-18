package ai.foresignal.social.narrative;

import ai.foresignal.analytics.math.RobustStats;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The propagation graph of a narrative (prompt 136, {@code PropagationTracer} 1.0.0): how it spread across reposts,
 * platforms and news, never who started it.
 *
 * <p>Nodes are member posts ({@code post:{memberId}}), linked 133 news articles ({@code article:{articleId}}) and
 * agency records cited by link ({@code record:{ref}}). Edges, each with its time delta:
 * <ul>
 *   <li>{@code repost}, {@code quote}, {@code reply}: from the member referenced to the post;
 *   <li>{@code link}: from an article or record to a post linking it;
 *   <li>{@code text-match}: from the member a post's text matched when it joined;
 *   <li>{@code media-match}: from the earliest member using a fingerprint to a later one;
 *   <li>{@code cross-platform}: to each of a platform's first {@value #CROSS_PLATFORM_POSTS} posts from the earliest
 *       earlier post on another platform sharing a URL, a fingerprint or near-identical text (MinHash Jaccard ≥ 0.8).
 * </ul>
 * Outputs: the earliest observed post per platform, the first news pickup, the <strong>descent share</strong> (posts and
 * articles reachable from the members of the three largest evidence groups, over all posts and articles), the
 * cross-platform order and the median repost delay. A graph of more than {@value NarrativeVocabulary#MAX_GRAPH_NODES}
 * nodes is summarized by collapsing evidence groups into one node each (edges merged with counts), then keeping the
 * largest nodes. Pure.
 */
public final class PropagationTracer {

    /** Posts per platform that get cross-platform edges. */
    static final int CROSS_PLATFORM_POSTS = 20;

    /** Near-identical text across platforms. */
    static final double CROSS_PLATFORM_JACCARD = 0.8;

    private PropagationTracer() {}

    /**
     * A news article linked to the narrative.
     *
     * @param articleId the article id ({@code source/externalId})
     * @param articleRef its record ref
     * @param storyId its 133 story
     * @param url its canonical URL
     * @param observedAt when it was seen
     * @param title its title, or null
     */
    public record Article(String articleId, String articleRef, String storyId, String url, Instant observedAt,
            String title) {}

    /**
     * An agency record cited by link.
     *
     * @param ref the record ref
     * @param url the URL posts link
     * @param observedAt when it was observed
     * @param label a label, or null
     */
    public record Official(String ref, String url, Instant observedAt, String label) {}

    /**
     * The computed graph.
     *
     * @param narrativeId the narrative
     * @param summarized true when collapsed
     * @param totalNodes nodes before summarizing
     * @param totalEdges edges before summarizing
     * @param nodes the (summarized) nodes
     * @param edges the (summarized) edges
     * @param earliestByPlatform earliest observed post per platform
     * @param firstNewsPickup the first news article, or null
     * @param descentShare the descent share
     * @param crossPlatformOrder platforms in first-observed order
     * @param medianRepostDelaySeconds the median repost delay, or null
     * @param fullEdges every edge (kept for the narrative's lifetime)
     */
    public record Graph(String narrativeId, boolean summarized, int totalNodes, int totalEdges,
            List<Map<String, Object>> nodes, List<Map<String, Object>> edges,
            List<Map<String, Object>> earliestByPlatform, Map<String, Object> firstNewsPickup, double descentShare,
            List<Map<String, Object>> crossPlatformOrder, Double medianRepostDelaySeconds,
            List<NarrativeStore.Edge> fullEdges) {

        /** The {@code PropagationGraph} members computed here. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("narrativeId", narrativeId);
            out.put("summarised", summarized);
            out.put("totalNodes", totalNodes);
            out.put("totalEdges", totalEdges);
            out.put("nodes", nodes);
            out.put("edges", edges);
            out.put("earliestByPlatform", earliestByPlatform);
            out.put("firstNewsPickup", firstNewsPickup);
            out.put("descentShare", descentShare);
            out.put("crossPlatformOrder", crossPlatformOrder);
            out.put("medianRepostDelaySeconds", medianRepostDelaySeconds);
            return out;
        }
    }

    /** The node id of a member. */
    public static String postNode(String memberId) {
        return "post:" + memberId;
    }

    /**
     * Builds every edge of a narrative.
     *
     * @param members the members
     * @param articles the linked articles
     * @param officials the cited agency records
     */
    public static List<NarrativeStore.Edge> edges(List<NarrativeStore.Member> members, List<Article> articles,
            List<Official> officials) {
        List<NarrativeStore.Member> sorted = sorted(members);
        Map<String, NarrativeStore.Member> byPost = new HashMap<>();
        Map<String, NarrativeStore.Member> byId = new HashMap<>();
        for (NarrativeStore.Member m : sorted) {
            byPost.putIfAbsent(m.postKey(), m);
            byId.put(m.id(), m);
        }
        Map<String, Article> articleByUrl = new HashMap<>();
        for (Article a : articles) {
            if (a.url() != null) {
                articleByUrl.putIfAbsent(a.url(), a);
            }
        }
        Map<String, Official> officialByUrl = new HashMap<>();
        for (Official o : officials) {
            if (o.url() != null) {
                officialByUrl.putIfAbsent(o.url(), o);
            }
        }
        List<NarrativeStore.Edge> out = new ArrayList<>();
        Map<String, NarrativeStore.Member> firstOfFingerprint = new HashMap<>();
        for (NarrativeStore.Member m : sorted) {
            String to = postNode(m.id());
            reference(out, byPost.get(m.repostOf()), m, "repost");
            reference(out, byPost.get(m.quoteOf()), m, "quote");
            reference(out, byPost.get(m.replyTo()), m, "reply");
            for (String link : m.links()) {
                Article a = articleByUrl.get(link);
                if (a != null) {
                    out.add(new NarrativeStore.Edge("article:" + a.articleId(), to, "link", delta(a.observedAt(),
                            m.createdAt())));
                }
                Official o = officialByUrl.get(link);
                if (o != null) {
                    out.add(new NarrativeStore.Edge("record:" + o.ref(), to, "link", delta(o.observedAt(),
                            m.createdAt())));
                }
            }
            if (NarrativeVocabulary.TEXT.equals(m.linkReason()) && m.matchedKey() != null) {
                NarrativeStore.Member matched = byId.get(m.matchedKey());
                if (matched != null && !matched.id().equals(m.id())) {
                    out.add(new NarrativeStore.Edge(postNode(matched.id()), to, "text-match",
                            delta(matched.createdAt(), m.createdAt())));
                }
            }
            for (String fp : m.fingerprints()) {
                NarrativeStore.Member first = firstOfFingerprint.putIfAbsent(fp, m);
                if (first != null && !first.id().equals(m.id())) {
                    out.add(new NarrativeStore.Edge(postNode(first.id()), to, "media-match",
                            delta(first.createdAt(), m.createdAt())));
                }
            }
        }
        out.addAll(crossPlatform(sorted));
        return out;
    }

    private static void reference(List<NarrativeStore.Edge> out, NarrativeStore.Member from, NarrativeStore.Member to,
            String type) {
        if (from != null && !from.id().equals(to.id())) {
            out.add(new NarrativeStore.Edge(postNode(from.id()), postNode(to.id()), type,
                    delta(from.createdAt(), to.createdAt())));
        }
    }

    /** The cross-platform edges. */
    static List<NarrativeStore.Edge> crossPlatform(List<NarrativeStore.Member> sorted) {
        List<NarrativeStore.Edge> out = new ArrayList<>();
        Map<String, List<NarrativeStore.Member>> byPlatform = new LinkedHashMap<>();
        for (NarrativeStore.Member m : sorted) {
            byPlatform.computeIfAbsent(m.platform(), k -> new ArrayList<>()).add(m);
        }
        if (byPlatform.size() < 2) {
            return out;
        }
        for (Map.Entry<String, List<NarrativeStore.Member>> e : byPlatform.entrySet()) {
            List<NarrativeStore.Member> own = e.getValue();
            for (NarrativeStore.Member p : own.subList(0, Math.min(CROSS_PLATFORM_POSTS, own.size()))) {
                NarrativeStore.Member source = null;
                for (Map.Entry<String, List<NarrativeStore.Member>> other : byPlatform.entrySet()) {
                    if (other.getKey().equals(e.getKey())) {
                        continue;
                    }
                    for (NarrativeStore.Member q : other.getValue()) {
                        if (!q.createdAt().isBefore(p.createdAt())) {
                            break;
                        }
                        if (shares(q, p) && (source == null || q.createdAt().isBefore(source.createdAt()))) {
                            source = q;
                            break;
                        }
                    }
                }
                if (source != null) {
                    out.add(new NarrativeStore.Edge(postNode(source.id()), postNode(p.id()), "cross-platform",
                            delta(source.createdAt(), p.createdAt())));
                }
            }
        }
        return out;
    }

    private static boolean shares(NarrativeStore.Member a, NarrativeStore.Member b) {
        for (String link : a.links()) {
            if (b.links().contains(link)) {
                return true;
            }
        }
        for (String fp : a.fingerprints()) {
            if (b.fingerprints().contains(fp)) {
                return true;
            }
        }
        return TextSignatures.jaccard(a.signature(), b.signature()) >= CROSS_PLATFORM_JACCARD;
    }

    /**
     * Builds the graph view from members, edges and groups.
     *
     * @param narrativeId the narrative
     * @param members the members (with their evidence groups)
     * @param edges the edges ({@link #edges})
     * @param articles the linked articles
     * @param officials the cited records
     * @param maxNodes the largest summarized graph
     */
    public static Graph graph(String narrativeId, List<NarrativeStore.Member> members, List<NarrativeStore.Edge> edges,
            List<Article> articles, List<Official> officials, int maxNodes) {
        List<NarrativeStore.Member> sorted = sorted(members);
        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
        for (NarrativeStore.Member m : sorted) {
            nodes.put(postNode(m.id()), node(postNode(m.id()), "post", m.recordRef(), m.platform(), m.createdAt(),
                    m.evidenceGroupId(), 1, null));
        }
        for (Article a : articles) {
            nodes.putIfAbsent("article:" + a.articleId(), node("article:" + a.articleId(), "news-article",
                    a.articleRef(), null, a.observedAt(), null, 1, a.title()));
        }
        for (Official o : officials) {
            nodes.putIfAbsent("record:" + o.ref(), node("record:" + o.ref(), "official-record", o.ref(), null,
                    o.observedAt(), null, 1, o.label()));
        }
        List<Map<String, Object>> earliest = new ArrayList<>();
        List<Map<String, Object>> order = new ArrayList<>();
        Set<String> seenPlatforms = new HashSet<>();
        for (NarrativeStore.Member m : sorted) {
            if (seenPlatforms.add(m.platform())) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("platform", m.platform());
                e.put("postRef", m.recordRef());
                e.put("observedAt", m.createdAt().toString());
                earliest.add(e);
                Map<String, Object> o = new LinkedHashMap<>();
                o.put("platform", m.platform());
                o.put("firstObservedAt", m.createdAt().toString());
                order.add(o);
            }
        }
        Map<String, Object> pickup = null;
        Instant firstPost = sorted.isEmpty() ? null : sorted.get(0).createdAt();
        Article firstArticle = null;
        for (Article a : articles) {
            if (a.observedAt() != null && (firstArticle == null || a.observedAt().isBefore(firstArticle.observedAt()))) {
                firstArticle = a;
            }
        }
        if (firstArticle != null) {
            pickup = new LinkedHashMap<>();
            pickup.put("storyId", firstArticle.storyId());
            pickup.put("articleRef", firstArticle.articleRef());
            pickup.put("observedAt", firstArticle.observedAt().toString());
            pickup.put("afterFirstPostSeconds", firstPost == null ? null : delta(firstPost, firstArticle.observedAt()));
        }
        double descent = descentShare(sorted, edges, articles.size());
        List<Double> repostDelays = new ArrayList<>();
        for (NarrativeStore.Edge e : edges) {
            if ("repost".equals(e.type())) {
                repostDelays.add(e.deltaSeconds());
            }
        }
        Double median = repostDelays.isEmpty() ? null
                : RobustStats.median(repostDelays.stream().mapToDouble(Double::doubleValue).toArray());
        int totalNodes = nodes.size();
        int totalEdges = edges.size();
        boolean summarized = totalNodes > maxNodes;
        List<Map<String, Object>> nodeList;
        List<Map<String, Object>> edgeList;
        if (!summarized) {
            nodeList = new ArrayList<>(nodes.values());
            edgeList = new ArrayList<>();
            for (NarrativeStore.Edge e : edges) {
                edgeList.add(edge(e.from(), e.to(), e.type(), e.deltaSeconds(), 1));
            }
        } else {
            Map<String, String> collapse = new HashMap<>();
            Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
            for (NarrativeStore.Member m : sorted) {
                String groupKey = m.evidenceGroupId() != null ? m.evidenceGroupId()
                        : m.evidenceGroupKey() != null ? m.evidenceGroupKey() : m.id();
                String id = "group:" + groupKey;
                collapse.put(postNode(m.id()), id);
                Map<String, Object> g = grouped.get(id);
                if (g == null) {
                    grouped.put(id, node(id, "evidence-group", m.recordRef(), m.platform(), m.createdAt(),
                            m.evidenceGroupId(), 1, m.evidenceGroupKey()));
                } else {
                    g.put("memberCount", (Integer) g.get("memberCount") + 1);
                    if (g.get("platform") != null && !g.get("platform").equals(m.platform())) {
                        g.put("platform", null);
                    }
                }
            }
            for (Map.Entry<String, Map<String, Object>> e : nodes.entrySet()) {
                if (!collapse.containsKey(e.getKey())) {
                    grouped.put(e.getKey(), e.getValue());
                }
            }
            List<Map<String, Object>> ranked = new ArrayList<>(grouped.values());
            ranked.sort(Comparator.comparingInt((Map<String, Object> n) -> (Integer) n.get("memberCount")).reversed()
                    .thenComparing(n -> String.valueOf(n.get("observedAt"))).thenComparing(n -> String.valueOf(
                            n.get("id"))));
            nodeList = new ArrayList<>(ranked.subList(0, Math.min(maxNodes, ranked.size())));
            Set<String> kept = new HashSet<>();
            for (Map<String, Object> n : nodeList) {
                kept.add(String.valueOf(n.get("id")));
            }
            Map<String, double[]> merged = new LinkedHashMap<>();
            for (NarrativeStore.Edge e : edges) {
                String from = collapse.getOrDefault(e.from(), e.from());
                String to = collapse.getOrDefault(e.to(), e.to());
                if (from.equals(to) || !kept.contains(from) || !kept.contains(to)) {
                    continue;
                }
                String key = from + "\u0000" + to + "\u0000" + e.type();
                double[] agg = merged.get(key);
                if (agg == null) {
                    merged.put(key, new double[] {e.deltaSeconds(), 1});
                } else {
                    agg[0] = Math.min(agg[0], e.deltaSeconds());
                    agg[1]++;
                }
            }
            edgeList = new ArrayList<>();
            for (Map.Entry<String, double[]> e : merged.entrySet()) {
                String[] parts = e.getKey().split("\u0000");
                edgeList.add(edge(parts[0], parts[1], parts[2], e.getValue()[0], (int) e.getValue()[1]));
            }
        }
        return new Graph(narrativeId, summarized, totalNodes, totalEdges, nodeList, edgeList, earliest, pickup, descent,
                order, median, edges);
    }

    /**
     * The share of posts and articles reachable from the members of the three largest evidence groups.
     *
     * @param members the members (with their evidence groups)
     * @param edges the edges
     * @param articleCount the linked articles
     */
    public static double descentShare(List<NarrativeStore.Member> members, List<NarrativeStore.Edge> edges,
            int articleCount) {
        int total = members.size() + articleCount;
        if (total == 0) {
            return 0;
        }
        Map<String, List<NarrativeStore.Member>> groups = new LinkedHashMap<>();
        for (NarrativeStore.Member m : members) {
            String key = m.evidenceGroupId() != null ? m.evidenceGroupId()
                    : m.evidenceGroupKey() != null ? m.evidenceGroupKey() : m.id();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }
        List<List<NarrativeStore.Member>> ranked = new ArrayList<>(groups.values());
        ranked.sort(Comparator.comparingInt((List<NarrativeStore.Member> l) -> l.size()).reversed()
                .thenComparing(l -> l.get(0).createdAt()));
        Set<String> reached = new LinkedHashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (int i = 0; i < Math.min(3, ranked.size()); i++) {
            for (NarrativeStore.Member m : ranked.get(i)) {
                if (reached.add(postNode(m.id()))) {
                    queue.add(postNode(m.id()));
                }
            }
        }
        Map<String, List<String>> next = new HashMap<>();
        for (NarrativeStore.Edge e : edges) {
            next.computeIfAbsent(e.from(), k -> new ArrayList<>()).add(e.to());
        }
        while (!queue.isEmpty()) {
            String n = queue.poll();
            for (String to : next.getOrDefault(n, List.of())) {
                if (reached.add(to)) {
                    queue.add(to);
                }
            }
        }
        int count = 0;
        for (String n : reached) {
            if (n.startsWith("post:") || n.startsWith("article:")) {
                count++;
            }
        }
        return Math.round(Math.min(1.0, (double) count / total) * 10_000) / 10_000.0;
    }

    private static List<NarrativeStore.Member> sorted(List<NarrativeStore.Member> members) {
        List<NarrativeStore.Member> sorted = new ArrayList<>(members);
        sorted.sort(Comparator.comparing(NarrativeStore.Member::createdAt).thenComparing(NarrativeStore.Member::id));
        return sorted;
    }

    private static Map<String, Object> node(String id, String type, String ref, String platform, Instant at,
            String groupId, int count, String label) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("type", type);
        out.put("ref", ref);
        out.put("platform", platform);
        out.put("observedAt", at == null ? Instant.EPOCH.toString() : at.toString());
        out.put("evidenceGroupId", groupId);
        out.put("memberCount", count);
        out.put("label", label == null ? null : NarrativeStore.clip(label, 200));
        return out;
    }

    private static Map<String, Object> edge(String from, String to, String type, double delta, int count) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", from);
        out.put("to", to);
        out.put("type", type);
        out.put("deltaSeconds", delta);
        out.put("count", count);
        return out;
    }

    /** Seconds from {@code a} to {@code b} (0 when either is null). */
    static double delta(Instant a, Instant b) {
        return a == null || b == null ? 0 : Duration.between(a, b).toMillis() / 1000.0;
    }
}
