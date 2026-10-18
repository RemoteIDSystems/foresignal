package ai.foresignal.news;

import ai.foresignal.media.outlet.Consensus;
import ai.foresignal.media.outlet.MediaVocabulary;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.situation.SituationRecordPublisher;
import ai.foresignal.source.formats.news.NewsArticles;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Coverage of stories, topics, situations and hypotheses (prompt 133), computed with {@link CoverageAnalyzer} from the
 * stories' articles, their evidence groups and the outlets' rating consensus.
 *
 * <ul>
 *   <li><strong>Evidence group outlet:</strong> a wire group is judged by the wire's own outlet (from the story, else the
 *       registry's wire with that originator); an outlet group by its outlet; a reprint cluster of unknown origin by its
 *       first article's outlet.
 *   <li><strong>Outlet judgments:</strong> lean bucket = the lean consensus bucket ({@code unrated} without one);
 *       factuality band = the factuality median rounded to 1…5; state-affiliated = type {@code state-affiliated} or a
 *       state-affiliation consensus of at least 0.5. Every enabled provider counts, {@code LOCAL_ONLY} ones included:
 *       these are derived counts, never the ratings themselves.
 *   <li><strong>Situations:</strong> the news-story members ({@code foresignal-intel/{storyId}} records) of the
 *       situation; {@code corroboratedShare} is the share of those stories corroborated by non-news evidence.
 *   <li><strong>Hypotheses:</strong> the live ACH evidence rows rated consistent ({@code C}, {@code CC}) with the
 *       hypothesis whose references are news articles; each row is one evidence group (116). The support is
 *       corroborated when a supporting row is non-news, or a supporting article's story is corroborated.
 * </ul>
 */
@Component
public class NewsCoverageService {

    /** The situation member kind of news stories. */
    public static final String STORY_KIND = NewsStoryRecordPublisher.KIND;

    /** The member ref prefix of news stories in situations. */
    public static final String STORY_REF_PREFIX = SituationRecordPublisher.SOURCE_ID + "/";

    /** Consistent ACH ratings. */
    static final Set<String> SUPPORTING = Set.of("C", "CC");

    private static final TypeReference<List<String>> STRINGS = new TypeReference<>() {};
    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NewsStoryStore store;
    private final OutletRegistry outlets;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public NewsCoverageService(NewsStoryStore store, OutletRegistry outlets, JdbcTemplate jdbc, JsonMapper json,
            Clock clock) {
        this.store = store;
        this.outlets = outlets;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * The judgments of one outlet the coverage needs.
     *
     * @param outlet the outlet, or null
     * @param bucket the lean bucket
     * @param factualityBand the factuality band
     * @param stateAffiliated the state-affiliation judgment
     * @param factualityMedian the factuality median, or null
     */
    public record OutletJudgment(OutletRegistry.Outlet outlet, String bucket, String factualityBand,
            boolean stateAffiliated, Double factualityMedian) {}

    /**
     * A hypothesis's news support.
     *
     * @param coverage the {@code CoverageStats} of its supporting news evidence, or null when it has none
     * @param skewed whether that support is skewed
     * @param corroborated whether non-news evidence corroborates the support
     * @param newsGroups the supporting news evidence groups
     */
    public record HypothesisSupport(Map<String, Object> coverage, boolean skewed, boolean corroborated,
            int newsGroups) {

        /** True when skewed support has no non-news corroboration (the confidence step-down). */
        public boolean skewedUncorroborated() {
            return skewed && !corroborated && newsGroups > 0;
        }
    }

    // ------------------------------------------------------------------------- stories

    /** Recomputes and stores a story's coverage; returns the analysis (null for an unknown story). */
    public CoverageAnalyzer.Result refreshStory(String storyId) {
        NewsStoryStore.Story story = store.story(storyId).orElse(null);
        if (story == null) {
            return null;
        }
        CoverageAnalyzer.Result result = analyzeArticles("story", store.articles(storyId), story.corroborated(), null);
        store.updateCoverage(storyId, result.stats(), result.blindspot(), result.skewed(), result.nRated(),
                result.stateAffiliatedShare(), clock.instant());
        return result;
    }

    /** The coverage of a set of articles, grouped by their evidence group. */
    public CoverageAnalyzer.Result analyzeArticles(String scope, List<NewsStoryStore.Article> articles,
            boolean corroborated, Double corroboratedShare) {
        Map<String, List<NewsStoryStore.Article>> groups = new LinkedHashMap<>();
        for (NewsStoryStore.Article a : articles) {
            String key = a.storyId() + "|" + (a.evidenceGroupKey() != null ? a.evidenceGroupKey() : "article:" + a.id());
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
        }
        Set<String> outletIds = new LinkedHashSet<>();
        for (NewsStoryStore.Article a : articles) {
            if (a.outletId() != null) {
                outletIds.add(a.outletId());
            }
        }
        Map<String, OutletJudgment> judgments = judgments(outletIds);
        Map<String, OutletJudgment> wires = wireJudgments();
        List<CoverageAnalyzer.GroupInfo> infos = new ArrayList<>();
        for (List<NewsStoryStore.Article> members : groups.values()) {
            infos.add(groupInfo(members, judgments, wires));
        }
        return CoverageAnalyzer.analyze(scope, infos, corroborated, corroboratedShare, clock.instant());
    }

    private CoverageAnalyzer.GroupInfo groupInfo(List<NewsStoryStore.Article> members,
            Map<String, OutletJudgment> judgments, Map<String, OutletJudgment> wires) {
        NewsStoryStore.Article first = members.get(0);
        String wire = first.wireOriginator();
        OutletJudgment j = null;
        if (wire != null) {
            for (NewsStoryStore.Article a : members) {
                OutletJudgment candidate = a.outletId() == null ? null : judgments.get(a.outletId());
                if (candidate != null && candidate.outlet() != null && candidate.outlet().wire()
                        && wire.equals(candidate.outlet().originatorId())) {
                    j = candidate;
                    break;
                }
            }
            if (j == null) {
                j = wires.get(wire);
            }
        }
        if (j == null && first.outletId() != null) {
            j = judgments.get(first.outletId());
        }
        if (j == null) {
            return new CoverageAnalyzer.GroupInfo(MediaVocabulary.UNRATED, first.sourceCountry(),
                    MediaVocabulary.OTHER, MediaVocabulary.UNRATED, false, members.size());
        }
        String country = j.outlet() != null && j.outlet().country() != null ? j.outlet().country()
                : first.sourceCountry();
        return new CoverageAnalyzer.GroupInfo(j.bucket(), country, j.outlet() == null ? MediaVocabulary.OTHER
                : j.outlet().type(), j.factualityBand(), j.stateAffiliated(), members.size());
    }

    /** The coverage judgments of outlets (consensus of every enabled provider). */
    public Map<String, OutletJudgment> judgments(Collection<String> outletIds) {
        Map<String, OutletJudgment> out = new LinkedHashMap<>();
        if (outletIds == null || outletIds.isEmpty()) {
            return out;
        }
        Map<String, OutletRegistry.Outlet> byId = outlets.byIds(outletIds);
        Set<String> resolved = new LinkedHashSet<>();
        for (OutletRegistry.Outlet o : byId.values()) {
            resolved.add(o.id());
        }
        Map<String, Map<String, Consensus.Result>> consensus = outlets.consensus(resolved);
        for (Map.Entry<String, OutletRegistry.Outlet> e : byId.entrySet()) {
            OutletRegistry.Outlet o = e.getValue();
            out.put(e.getKey(), judge(o, consensus.getOrDefault(o.id(), Map.of())));
        }
        return out;
    }

    /** The judgment of one outlet from its consensus. */
    public static OutletJudgment judge(OutletRegistry.Outlet o, Map<String, Consensus.Result> consensus) {
        Consensus.Result lean = consensus.get(MediaVocabulary.LEAN);
        Consensus.Result factuality = consensus.get(MediaVocabulary.FACTUALITY);
        Consensus.Result state = consensus.get(MediaVocabulary.STATE_AFFILIATION);
        String bucket = lean == null || lean.bucket() == null ? MediaVocabulary.UNRATED : lean.bucket();
        boolean stateAffiliated = (o != null && MediaVocabulary.STATE_AFFILIATED.equals(o.type()))
                || (state != null && state.median() != null && state.median() >= 0.5);
        Double fm = factuality == null ? null : factuality.median();
        return new OutletJudgment(o, bucket, CoverageAnalyzer.factualityBand(fm), stateAffiliated, fm);
    }

    private Map<String, OutletJudgment> wireJudgments() {
        Map<String, OutletJudgment> out = new LinkedHashMap<>();
        List<OutletRegistry.Outlet> wires = outlets.wires();
        List<String> ids = new ArrayList<>();
        for (OutletRegistry.Outlet w : wires) {
            ids.add(w.id());
        }
        Map<String, Map<String, Consensus.Result>> consensus = outlets.consensus(ids);
        for (OutletRegistry.Outlet w : wires) {
            if (w.originatorId() != null) {
                out.putIfAbsent(w.originatorId(), judge(w, consensus.getOrDefault(w.id(), Map.of())));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- situations and topics

    /** The news stories that are members of a situation. */
    public List<String> storiesOfSituation(String situationId) {
        List<String> refs = jdbc.queryForList("SELECT ref FROM situation_member WHERE situation_id = ? AND kind = ?"
                + " AND member_type = 'record' ORDER BY added_at, id", String.class, situationId, STORY_KIND);
        List<String> out = new ArrayList<>();
        for (String ref : refs) {
            if (ref.startsWith(STORY_REF_PREFIX)) {
                String id = ref.substring(STORY_REF_PREFIX.length());
                if (!out.contains(id)) {
                    out.add(id);
                }
            }
        }
        return out;
    }

    /** The situations a news story is a member of. */
    public List<String> situationsOfStory(String storyId) {
        return jdbc.queryForList("SELECT DISTINCT situation_id FROM situation_member WHERE kind = ? AND member_type ="
                + " 'record' AND ref = ? ORDER BY situation_id", String.class, STORY_KIND, STORY_REF_PREFIX + storyId);
    }

    /**
     * A situation's coverage section: balance, blindspot, state-affiliated share and the corroborated share of its news
     * members; null when the situation has no news-story member.
     */
    public Map<String, Object> situationCoverage(String situationId) {
        List<String> storyIds = storiesOfSituation(situationId);
        if (storyIds.isEmpty()) {
            return null;
        }
        Map<String, NewsStoryStore.Story> stories = store.stories(storyIds);
        if (stories.isEmpty()) {
            return null;
        }
        int corroborated = 0;
        for (NewsStoryStore.Story s : stories.values()) {
            if (s.corroborated()) {
                corroborated++;
            }
        }
        List<NewsStoryStore.Article> articles = new ArrayList<>();
        store.articles(stories.keySet()).values().forEach(articles::addAll);
        double share = Math.round(corroborated / (double) stories.size() * 10_000) / 10_000.0;
        return analyzeArticles("situation", articles, corroborated > 0, share).stats();
    }

    /** A topic's coverage over the stories last seen at or after {@code since}; null without a story. */
    public Map<String, Object> topicCoverage(String topicId, Instant since) {
        List<String> ids = jdbc.queryForList("SELECT id FROM news_story WHERE ? = ANY(topic_ids) AND last_seen_at >= ?"
                + " ORDER BY last_seen_at DESC LIMIT 500", String.class, topicId, java.sql.Timestamp.from(since));
        if (ids.isEmpty()) {
            return null;
        }
        List<NewsStoryStore.Article> articles = new ArrayList<>();
        store.articles(ids).values().forEach(articles::addAll);
        return analyzeArticles("topic", articles, false, null).stats();
    }

    // ------------------------------------------------------------------------- hypotheses

    /** The news support of one hypothesis of a set. */
    public HypothesisSupport hypothesisSupport(String setId, String hypothesisId) {
        List<Map<String, Object>> rows = jdbc.query("SELECT ref, refs::text AS refs, ratings::text AS ratings, source"
                + " FROM ach_evidence WHERE set_id = ? AND removed_at IS NULL ORDER BY added_at, id", (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("ref", rs.getString("ref"));
                    m.put("refs", rs.getString("refs") == null ? List.of() : json.readValue(rs.getString("refs"),
                            STRINGS));
                    m.put("ratings", rs.getString("ratings") == null ? Map.of() : json.readValue(rs.getString("ratings"),
                            MAP));
                    m.put("source", rs.getString("source"));
                    return m;
                }, setId);
        List<List<String>> newsRows = new ArrayList<>();
        boolean nonNews = false;
        for (Map<String, Object> row : rows) {
            Object rating = ((Map<?, ?>) row.get("ratings")).get(hypothesisId);
            if (rating == null || !SUPPORTING.contains(String.valueOf(rating))) {
                continue;
            }
            List<String> refs = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<String> all = new ArrayList<>((List<String>) row.get("refs"));
            if (row.get("ref") != null && !all.contains(String.valueOf(row.get("ref")))) {
                all.add(0, String.valueOf(row.get("ref")));
            }
            for (String ref : all) {
                String key = newsKey(ref);
                if (key != null && !refs.contains(key)) {
                    refs.add(key);
                }
            }
            if (refs.isEmpty()) {
                nonNews |= !all.isEmpty() && !"manual".equals(row.get("source"));
            } else {
                newsRows.add(refs);
            }
        }
        if (newsRows.isEmpty()) {
            return new HypothesisSupport(null, false, nonNews, 0);
        }
        Set<String> keys = new LinkedHashSet<>();
        newsRows.forEach(keys::addAll);
        Map<String, NewsStoryStore.Article> articles = store.articlesByKey(keys);
        Set<String> storyIds = new LinkedHashSet<>();
        Set<String> outletIds = new LinkedHashSet<>();
        for (NewsStoryStore.Article a : articles.values()) {
            if (a.storyId() != null) {
                storyIds.add(a.storyId());
            }
            if (a.outletId() != null) {
                outletIds.add(a.outletId());
            }
        }
        boolean storyCorroborated = false;
        for (NewsStoryStore.Story s : store.stories(storyIds).values()) {
            storyCorroborated |= s.corroborated();
        }
        Map<String, OutletJudgment> judgments = judgments(outletIds);
        List<CoverageAnalyzer.GroupInfo> infos = new ArrayList<>();
        for (List<String> refs : newsRows) {
            NewsStoryStore.Article a = null;
            for (String key : refs) {
                if (articles.containsKey(key)) {
                    a = articles.get(key);
                    break;
                }
            }
            OutletJudgment j = a == null || a.outletId() == null ? null : judgments.get(a.outletId());
            if (j == null) {
                infos.add(new CoverageAnalyzer.GroupInfo(MediaVocabulary.UNRATED, a == null ? null : a.sourceCountry(),
                        MediaVocabulary.OTHER, MediaVocabulary.UNRATED, false, refs.size()));
            } else {
                infos.add(new CoverageAnalyzer.GroupInfo(j.bucket(), j.outlet() != null && j.outlet().country() != null
                        ? j.outlet().country() : a.sourceCountry(), j.outlet() == null ? MediaVocabulary.OTHER
                        : j.outlet().type(), j.factualityBand(), j.stateAffiliated(), refs.size()));
            }
        }
        boolean corroborated = nonNews || storyCorroborated;
        CoverageAnalyzer.Result result = CoverageAnalyzer.analyze("hypothesis", infos, corroborated, null,
                clock.instant());
        return new HypothesisSupport(result.stats(), result.skewed(), corroborated, infos.size());
    }

    /** The {@code source/externalId} key of a news-article record ref, or null for anything else. */
    static String newsKey(String ref) {
        if (ref == null) {
            return null;
        }
        String r = ref.startsWith("record:") ? ref.substring("record:".length()) : ref;
        int slash = r.indexOf('/');
        if (slash <= 0) {
            return null;
        }
        String source = r.substring(0, slash);
        if (!NewsArticles.writesNewsArticles(source)) {
            return null;
        }
        String external = r.substring(slash + 1);
        int at = external.lastIndexOf('@');
        if (at > 0 && external.substring(at + 1).chars().allMatch(Character::isDigit)) {
            external = external.substring(0, at);
        }
        external = external.replace("%2F", "/").replace("%40", "@").replace("%25", "%");
        return source + "/" + external;
    }

    // ------------------------------------------------------------------------- assessments and reports

    /**
     * The deterministic Coverage section of an assessment: each listed situation's coverage, the blindspot stories
     * and the divergence findings of the period; null when nothing applies.
     */
    public Map<String, Object> assessmentCoverage(List<String> situationIds, Instant from, Instant to) {
        List<Map<String, Object>> situations = new ArrayList<>();
        for (String id : situationIds) {
            Map<String, Object> stats = situationCoverage(id);
            if (stats != null) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("situationId", id);
                s.put("coverage", stats);
                situations.add(s);
            }
        }
        List<String> blindspots = jdbc.queryForList("SELECT id FROM news_story WHERE blindspot IS NOT NULL AND"
                + " last_seen_at >= ? AND first_seen_at <= ? ORDER BY evidence_group_count DESC, id LIMIT 20",
                String.class, java.sql.Timestamp.from(from), java.sql.Timestamp.from(to));
        List<String> divergences = jdbc.queryForList("SELECT id FROM analytic_finding WHERE finding_type ="
                + " 'coverage-divergence' AND status = 'open' AND detected_at >= ? AND detected_at <= ? ORDER BY"
                + " detected_at DESC, id LIMIT 20", String.class, java.sql.Timestamp.from(from),
                java.sql.Timestamp.from(to));
        if (situations.isEmpty() && blindspots.isEmpty() && divergences.isEmpty()) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("situations", situations);
        out.put("blindspotStoryIds", blindspots);
        out.put("divergenceFindingIds", divergences);
        return out;
    }

    /** The coverage statistics of situations for a report's {@code coverage} block (situations without news left out). */
    public List<Map<String, Object>> reportCoverage(Collection<String> situationIds) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String id : situationIds == null ? List.<String>of() : situationIds) {
            Map<String, Object> stats = situationCoverage(id);
            if (stats != null) {
                out.add(stats);
            }
        }
        return out;
    }
}
