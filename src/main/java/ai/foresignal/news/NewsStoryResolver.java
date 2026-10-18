package ai.foresignal.news;

import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceGrouper;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.ObservationFacts;
import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.QualityFlags;
import ai.foresignal.evidence.StoryFamilyHandler;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.source.formats.news.ArticleUrls;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The 116 resolver of the {@code news-story} family (prompt 133): clusters {@code news-article} records into stories on
 * the evidence resolver's cursor ({@link StoryFamilyHandler}).
 *
 * <ol>
 *   <li><strong>Outlet:</strong> the article's domain resolves through the registry; an unknown domain is added as an
 *       {@code unrated} outlet.
 *   <li><strong>Candidates:</strong> stories whose latest article is within {@code windowHours} (48) of the article and
 *       that share one of its attention topics or region tags (the topics' regions); an article with neither may join
 *       any story in the window.
 *   <li><strong>Similarity:</strong> the cosine between the article's title embedding (027) and the story centroid,
 *       joining at {@code minCosine} (0.82) when both exist under one model; otherwise the best normalized-title token
 *       Jaccard against the story's title and members, joining at {@code minJaccard} (0.6). The most similar candidate
 *       wins (ties: the older story); with none a new story starts.
 *   <li><strong>Identity:</strong> the story is a canonical event ({@code canonical_event} family {@code news-story}),
 *       the article an observation link; evidence groups come from {@link NewsGrouper}, so wire reprints are one group
 *       and flagged {@code syndicated}.
 *   <li><strong>Story:</strong> topics and regions are the union of its articles'; the centroid the mean of its
 *       articles' vectors; the title, deterministically, the most central article's title (highest mean cosine, else
 *       mean Jaccard, to the others; ties to the earliest); coverage is recomputed ({@link CoverageAnalyzer}).
 * </ol>
 * A record already placed is processed again only for a newer seq (its metadata is refreshed; it keeps its story).
 * Observations are never modified (MEM-1).
 */
@Component
public class NewsStoryResolver implements StoryFamilyHandler {

    /** The family name. */
    public static final String FAMILY = "news-story";

    /** The most candidate stories compared per article. */
    static final int CANDIDATES = 300;

    /** The most member titles compared per candidate. */
    static final int MEMBER_TITLES = 50;

    /** The most members whose vectors the centrality reads. */
    static final int CENTRALITY_MEMBERS = 200;

    private final EvidenceStore evidence;
    private final OutletRegistry outlets;
    private final NewsStoryStore store;
    private final TitleEmbeddings embeddings;
    private final NewsCoverageService coverage;
    private final ObjectProvider<AttentionTopicRepository> topics;
    private final Clock clock;

    /** Creates the resolver. */
    public NewsStoryResolver(EvidenceStore evidence, OutletRegistry outlets, NewsStoryStore store,
            TitleEmbeddings embeddings, NewsCoverageService coverage, ObjectProvider<AttentionTopicRepository> topics,
            Clock clock) {
        this.evidence = evidence;
        this.outlets = outlets;
        this.store = store;
        this.embeddings = embeddings;
        this.coverage = coverage;
        this.topics = topics;
        this.clock = clock;
    }

    @Override
    public void process(CanonicalRules.FamilyRule rule, Observation obs) {
        CanonicalRules.Story params = rule.story() == null ? CanonicalRules.Story.DEFAULT : rule.story();
        Map<String, Object> p = obs.payload() == null ? Map.of() : obs.payload();
        String title = text(p.get("title"));
        String url = text(p.get("url"));
        String domain = ArticleUrls.normalizeDomain(text(p.get("domain")) != null ? text(p.get("domain"))
                : ArticleUrls.domain(url));
        if (title == null || url == null || domain == null) {
            return;
        }
        Instant now = clock.instant();
        NewsStoryStore.Article existing = store.article(obs.sourceId(), obs.externalId()).orElse(null);
        if (existing != null && existing.seq() >= obs.seq()) {
            return;
        }
        OutletRegistry.Outlet outlet = outlets.byDomain(domain).orElseGet(() -> outlets.register(domain,
                text(p.get("sourceCountry")), text(p.get("language"))));
        List<String> topicIds = strings(p.get("topicIds"));
        List<String> regions = regionsOf(topicIds);
        List<String> tokens = TitleTokens.tokens(title);
        Instant seen = instant(p.get("seenAt"), obs.observedAt() != null ? obs.observedAt() : now);
        Instant at = obs.eventTime() != null ? obs.eventTime() : seen;
        String key = NewsStoryStore.articleId(obs.sourceId(), obs.externalId());
        if (existing != null && existing.storyId() != null) {
            store.upsertArticle(row(obs, existing.storyId(), existing.linkId(), outlet, domain, title, tokens, url, p,
                    seen, topicIds, regions, existing.similarity(), existing.matchMethod(), now));
            refresh(existing.storyId(), params, now);
            return;
        }
        TitleEmbeddings.TitleVectors vec = embeddings.vectors(List.of(key));
        float[] vector = vec.vectors().get(key);
        Duration window = Duration.ofMillis((long) (params.windowHours() * 3_600_000));
        String storyId = null;
        double bestScore = -1;
        String method = "first";
        Instant bestCreated = null;
        for (NewsStoryStore.Story s : store.candidates(at.minus(window), at.plus(window), topicIds, regions,
                CANDIDATES)) {
            Double cos = vector != null && s.centroid() != null && vec.model() != null
                    && vec.model().equals(s.centroidModel()) ? Vectors.cosine(vector, s.centroid()) : null;
            double score;
            boolean ok;
            String m;
            if (cos != null) {
                score = cos;
                ok = cos >= params.minCosine();
                m = "embedding";
            } else {
                score = bestJaccard(tokens, s, params.minJaccard());
                ok = score >= params.minJaccard();
                m = "tokens";
            }
            if (!ok) {
                continue;
            }
            if (score > bestScore || (score == bestScore && bestCreated != null && s.createdAt().isBefore(bestCreated))) {
                bestScore = score;
                storyId = s.id();
                method = m;
                bestCreated = s.createdAt();
            }
        }
        ObservationFacts facts = new ObservationFacts(obs.sourceId(), obs.externalId(), obs.seq(), obs.version(), at,
                seen, null, null, null, null, null, null, null, null, List.of(), domain);
        Originator originator = originatorOf(outlet);
        Map<String, Object> reason = new LinkedHashMap<>();
        if (storyId == null) {
            storyId = evidence.insertEvent(FAMILY, facts, regions, now);
            store.insertStory(storyId, title, tokens, key, seen, topicIds, regions, now);
            reason.put("rule", "first-observation");
            reason.put("similarity", 1.0);
            evidence.insertLink(storyId, facts, originator, 1.0, reason, now);
            bestScore = 1.0;
        } else {
            reason.put("rule", method);
            reason.put("similarity", round(bestScore));
            reason.put("threshold", "embedding".equals(method) ? params.minCosine() : params.minJaccard());
            evidence.insertLink(storyId, facts, originator, Math.max(0, Math.min(1, round(bestScore))), reason, now);
        }
        String linkId = null;
        for (EvidenceStore.StoredLink link : evidence.linksOf(obs.sourceId(), obs.externalId())) {
            if (storyId.equals(link.canonicalEventId())) {
                linkId = link.id();
                break;
            }
        }
        store.upsertArticle(row(obs, storyId, linkId, outlet, domain, title, tokens, url, p, seen, topicIds, regions,
                round(bestScore), method, now));
        refresh(storyId, params, now);
    }

    /** Recomputes a story's evidence groups, summary, title, centroid and coverage. */
    void refresh(String storyId, CanonicalRules.Story params, Instant now) {
        List<NewsStoryStore.Article> articles = store.articles(storyId);
        if (articles.isEmpty()) {
            return;
        }
        Set<String> outletIds = new LinkedHashSet<>();
        for (NewsStoryStore.Article a : articles) {
            if (a.outletId() != null) {
                outletIds.add(a.outletId());
            }
        }
        Map<String, OutletRegistry.Outlet> byId = outlets.byIds(outletIds);
        Map<String, NewsStoryStore.Article> byLink = new LinkedHashMap<>();
        List<NewsGrouper.Member> members = new ArrayList<>();
        for (NewsStoryStore.Article a : articles) {
            String memberKey = a.linkId() != null ? a.linkId() : a.id();
            byLink.put(memberKey, a);
            OutletRegistry.Outlet o = a.outletId() == null ? null : byId.get(a.outletId());
            String wire = o != null && o.wire() ? (o.originatorId() != null ? o.originatorId() : "outlet:" + o.id())
                    : null;
            members.add(new NewsGrouper.Member(memberKey, o == null ? null : o.id(), a.domain(), wire,
                    o == null ? List.of() : o.aggregates(), a.titleTokens(), a.seq()));
        }
        List<NewsGrouper.Group> groups = NewsGrouper.group(members, params.wireJaccard());
        List<EvidenceGrouper.Group> stored = new ArrayList<>();
        for (NewsGrouper.Group g : groups) {
            List<String> links = new ArrayList<>();
            for (String m : g.members()) {
                if (byLink.get(m) != null && byLink.get(m).linkId() != null) {
                    links.add(m);
                }
            }
            stored.add(new EvidenceGrouper.Group(g.root(), links, g.firstSeq()));
        }
        Map<String, String> groupIds = evidence.writeGroups(storyId, stored, now);
        boolean echo = false;
        for (NewsGrouper.Group g : groups) {
            for (String m : g.members()) {
                NewsStoryStore.Article a = byLink.get(m);
                boolean syndicated = g.syndicated().contains(m);
                echo |= syndicated;
                store.updateGroup(a.id(), g.root(), groupIds.get(g.root()), syndicated, g.wireOriginator());
            }
        }
        List<String> flags = new ArrayList<>();
        if (echo) {
            flags.add(QualityFlags.ECHO);
        }
        if (groups.size() == 1 && articles.size() > 1) {
            flags.add(QualityFlags.SINGLE_ORIGINATOR);
        }
        Set<String> sources = new TreeSet<>();
        Set<String> topicIds = new LinkedHashSet<>();
        Set<String> regions = new LinkedHashSet<>();
        Set<String> outletKeys = new LinkedHashSet<>();
        Instant first = null;
        Instant last = null;
        for (NewsStoryStore.Article a : articles) {
            sources.add(a.sourceId());
            topicIds.addAll(a.topicIds());
            regions.addAll(a.regionCodes());
            outletKeys.add(a.outletId() != null ? a.outletId() : a.domain());
            first = first == null || a.seenAt().isBefore(first) ? a.seenAt() : first;
            last = last == null || a.seenAt().isAfter(last) ? a.seenAt() : last;
        }
        evidence.updateSummary(storyId, articles.size(), groups.size(), flags, new ArrayList<>(sources), now);
        List<NewsStoryStore.Article> sample = articles.size() > CENTRALITY_MEMBERS
                ? articles.subList(0, CENTRALITY_MEMBERS) : articles;
        List<String> keys = new ArrayList<>();
        for (NewsStoryStore.Article a : sample) {
            keys.add(a.id());
        }
        TitleEmbeddings.TitleVectors vec = embeddings.vectors(keys);
        NewsStoryStore.Article central = central(sample, vec);
        List<float[]> vectors = new ArrayList<>(vec.vectors().values());
        float[] centroid = Vectors.mean(vectors);
        store.updateStory(storyId, central.title(), central.titleTokens(), central.id(), centroid,
                centroid == null ? null : vec.model(), centroid == null ? 0 : vectors.size(), first, last,
                articles.size(), outletKeys.size(), groups.size(), new ArrayList<>(topicIds), new ArrayList<>(regions),
                now);
        coverage.refreshStory(storyId);
    }

    /** The most central article: the highest mean cosine to the others, else the highest mean Jaccard; ties earliest. */
    static NewsStoryStore.Article central(List<NewsStoryStore.Article> articles, TitleEmbeddings.TitleVectors vec) {
        if (articles.size() <= 2) {
            return articles.get(0);
        }
        boolean useVectors = vec.vectors().size() == articles.size();
        NewsStoryStore.Article best = articles.get(0);
        double bestScore = -2;
        for (NewsStoryStore.Article a : articles) {
            double sum = 0;
            int n = 0;
            for (NewsStoryStore.Article b : articles) {
                if (a == b) {
                    continue;
                }
                Double s = useVectors ? Vectors.cosine(vec.vectors().get(a.id()), vec.vectors().get(b.id()))
                        : TitleTokens.jaccard(a.titleTokens(), b.titleTokens());
                if (s != null) {
                    sum += s;
                    n++;
                }
            }
            double score = n == 0 ? -1 : sum / n;
            if (score > bestScore + 1e-12) {
                bestScore = score;
                best = a;
            }
        }
        return best;
    }

    /**
     * The best token Jaccard of a title against a story: its title, and its latest {@value #MEMBER_TITLES} member
     * titles when the story title alone falls short but shares at least half the threshold (members are read only
     * then, to keep the comparison cheap).
     */
    private double bestJaccard(List<String> tokens, NewsStoryStore.Story s, double threshold) {
        double best = TitleTokens.jaccard(tokens, s.titleTokens());
        if (best >= threshold || best < threshold / 2) {
            return best;
        }
        List<NewsStoryStore.Article> members = store.articles(s.id());
        int start = Math.max(0, members.size() - MEMBER_TITLES);
        for (NewsStoryStore.Article a : members.subList(start, members.size())) {
            best = Math.max(best, TitleTokens.jaccard(tokens, a.titleTokens()));
        }
        return best;
    }

    private NewsStoryStore.Article row(Observation obs, String storyId, String linkId, OutletRegistry.Outlet outlet,
            String domain, String title, List<String> tokens, String url, Map<String, Object> p, Instant seen,
            List<String> topicIds, List<String> regions, Double similarity, String method, Instant now) {
        return new NewsStoryStore.Article(NewsStoryStore.articleId(obs.sourceId(), obs.externalId()), obs.sourceId(),
                obs.externalId(), obs.seq(), storyId, linkId, null, null, outlet == null ? null : outlet.id(), domain,
                title, tokens, url, text(p.get("language")), text(p.get("sourceCountry")), seen,
                instant(p.get("publishedAt"), null), topicIds, regions, text(p.get("summary")), text(p.get("imageUrl")),
                false, null, similarity, method, now);
    }

    /** The 116 originator of an article: its wire, else its outlet. */
    public static Originator originatorOf(OutletRegistry.Outlet outlet) {
        if (outlet == null) {
            return new Originator("outlet:unknown", "media", null, List.of(), null, List.of());
        }
        String id = outlet.wire() && outlet.originatorId() != null ? outlet.originatorId() : "outlet:" + outlet.id();
        return new Originator(id, "media", null, List.of(), null, outlet.domains());
    }

    private List<String> regionsOf(Collection<String> topicIds) {
        Set<String> out = new LinkedHashSet<>();
        AttentionTopicRepository repository = topics.getIfAvailable();
        if (repository == null || topicIds.isEmpty()) {
            return new ArrayList<>(out);
        }
        for (AttentionTopicEntity t : repository.findAllById(topicIds)) {
            if (t.getRegionCodes() != null) {
                out.addAll(t.getRegionCodes());
            }
        }
        return new ArrayList<>(out);
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof Collection<?> c) {
            for (Object o : c) {
                if (o != null && !String.valueOf(o).isBlank()) {
                    out.add(String.valueOf(o));
                }
            }
        }
        return out;
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value).strip();
        return s.isEmpty() ? null : s;
    }

    private static Instant instant(Object value, Instant fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

    private static double round(double v) {
        return Math.round(v * 10_000) / 10_000.0;
    }
}
