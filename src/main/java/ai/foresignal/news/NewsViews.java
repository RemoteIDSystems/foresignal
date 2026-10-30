package ai.foresignal.news;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.media.outlet.Consensus;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.media.outlet.OutletViews;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The read side of news coverage shared by the REST controllers and the MCP tools (prompt 133): story and article
 * pages, the story detail (articles grouped by outlet with each outlet's ratings and consensus, coverage statistics,
 * linked situations and canonical events) and the outlet views.
 *
 * <p>Articles carry outbound links only. No author name is ever shown: NewsArticle has no byline field, and bodies or
 * bylines can only come from EDP {@code article} documents bound later (138).
 */
@Component
public class NewsViews {

    /** The largest page. */
    public static final int MAX_LIMIT = 500;

    /** The default page. */
    public static final int DEFAULT_LIMIT = 100;

    private final NewsStoryStore store;
    private final OutletRegistry outlets;
    private final NewsCoverageService coverage;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<ObjectService> objects;
    private final ObjectProvider<EgressPolicy> egress;
    private final ObjectProvider<NewsTranslationStore> translations;
    private final ObjectProvider<TranslationSettingsService> translationSettings;

    /** Creates the views. */
    public NewsViews(NewsStoryStore store, OutletRegistry outlets, NewsCoverageService coverage, JdbcTemplate jdbc,
            ObjectProvider<ObjectService> objects, ObjectProvider<EgressPolicy> egress,
            ObjectProvider<NewsTranslationStore> translations,
            ObjectProvider<TranslationSettingsService> translationSettings) {
        this.store = store;
        this.outlets = outlets;
        this.coverage = coverage;
        this.jdbc = jdbc;
        this.objects = objects;
        this.egress = egress;
        this.translations = translations;
        this.translationSettings = translationSettings;
    }

    /**
     * Adds the translation fields of NP-002 ({@code targetLanguage}, {@code translatedTitle},
     * {@code translatedExcerpt}, {@code translationProvenance}) to each view whose object has a served translation, in
     * the first configured target language that has one. Views without one, and every view while translation is off, are
     * left as they are, so a client sees the original fields only.
     *
     * @param type {@link NewsTranslationStore#ARTICLE} or {@link NewsTranslationStore#STORY}
     * @param views object id to its view
     */
    void addTranslations(String type, Map<String, Map<String, Object>> views) {
        NewsTranslationStore store = translations.getIfAvailable();
        TranslationSettingsService settings = translationSettings.getIfAvailable();
        if (store == null || settings == null || views.isEmpty()) {
            return;
        }
        TranslationSettings current = settings.get();
        List<String> languages = current.targetLanguages();
        if (!current.active()) {
            return;
        }
        Map<String, NewsTranslationStore.Translation> served = store.served(type, views.keySet(), languages);
        for (Map.Entry<String, NewsTranslationStore.Translation> e : served.entrySet()) {
            Map<String, Object> view = views.get(e.getKey());
            if (view != null) {
                view.putAll(e.getValue().wire());
            }
        }
    }

    /**
     * A page of views.
     *
     * @param items the items
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<Map<String, Object>> items, String nextCursor) {

        /** The page as {@code {items, nextCursor}}. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("items", items);
            out.put("nextCursor", nextCursor);
            return out;
        }
    }

    /**
     * Which articles a caller may read (prompt 035 restrictions): articles are records of their sources, so a source
     * outside the caller's restriction or hidden from it is left out, and a spatially restricted caller sees no article
     * (articles carry no geometry: fail closed). Stories and coverage statistics are derived counts and stay visible.
     *
     * @param allowed the sources a restricted caller may read, or null for any
     * @param excluded the sources hidden from the caller
     * @param none true when no article is visible
     */
    public record ArticleFilter(Set<String> allowed, Set<String> excluded, boolean none) {

        /** No restriction. */
        public static final ArticleFilter ALL = new ArticleFilter(null, Set.of(), false);

        /** The filter of a caller's restriction and hidden sources. */
        public static ArticleFilter of(ai.foresignal.security.restrict.ReadRestriction r, Set<String> excluded) {
            Set<String> hidden = excluded == null ? Set.of() : Set.copyOf(excluded);
            if (r == null || r.isUnrestricted()) {
                return new ArticleFilter(null, hidden, false);
            }
            boolean spatial = r.bbox() != null || r.area() != null;
            boolean kind = r.allowsKind(ai.foresignal.source.formats.news.NewsArticles.KIND);
            return new ArticleFilter(r.sources() == null || r.sources().isEmpty() ? null : Set.copyOf(r.sources()),
                    hidden, spatial || !kind);
        }

        /** True when articles of {@code sourceId} are visible. */
        public boolean visible(String sourceId) {
            return !none && !excluded.contains(sourceId) && (allowed == null || allowed.contains(sourceId));
        }
    }

    /** True when {@code LOCAL_ONLY} ratings may be served on {@code path}. */
    public boolean localAllowed(EgressPath path) {
        EgressPolicy policy = egress.getIfAvailable();
        return policy != null && policy.allowsLocalOnlyData(path);
    }

    // ------------------------------------------------------------------------- stories

    /**
     * Lists stories, newest activity first.
     *
     * @param topic an attention topic, or null
     * @param region a region code, or null
     * @param situationId a situation, or null
     * @param since the least {@code lastSeenAt}, or null
     * @param blindspot {@code left}, {@code right}, {@code any}, or null
     */
    public Page stories(String topic, String region, String situationId, Instant since, String blindspot,
            String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id, last_seen_at FROM news_story WHERE article_count > 0");
        List<Object> args = new ArrayList<>();
        if (topic != null && !topic.isBlank()) {
            sql.append(" AND ? = ANY(topic_ids)");
            args.add(topic.strip());
        }
        if (region != null && !region.isBlank()) {
            sql.append(" AND ? = ANY(region_codes)");
            args.add(region.strip().toUpperCase(Locale.ROOT));
        }
        if (situationId != null && !situationId.isBlank()) {
            sql.append(" AND id = ANY(?)");
            args.add(coverage.storiesOfSituation(situationId.strip()).toArray(String[]::new));
        }
        if (since != null) {
            sql.append(" AND last_seen_at >= ?");
            args.add(Timestamp.from(since));
        }
        if ("any".equals(blindspot)) {
            sql.append(" AND blindspot IS NOT NULL");
        } else if (blindspot != null && !blindspot.isBlank()) {
            sql.append(" AND blindspot = ?");
            args.add(blindspot);
        }
        Cursor c = Cursor.parse(cursor);
        if (c != null) {
            sql.append(" AND (last_seen_at < ? OR (last_seen_at = ? AND id < ?))");
            args.add(Timestamp.from(c.at()));
            args.add(Timestamp.from(c.at()));
            args.add(c.id());
        }
        sql.append(" ORDER BY last_seen_at DESC, id DESC LIMIT ?");
        args.add(limit + 1);
        List<String[]> rows = jdbc.query(sql.toString(), (rs, i) -> new String[] {rs.getString(1),
                rs.getTimestamp(2).toInstant().toString()}, args.toArray());
        String next = null;
        if (rows.size() > limit) {
            rows = rows.subList(0, limit);
            String[] last = rows.get(rows.size() - 1);
            next = new Cursor(Instant.parse(last[1]), last[0]).format();
        }
        List<String> ids = new ArrayList<>();
        for (String[] r : rows) {
            ids.add(r[0]);
        }
        Map<String, NewsStoryStore.Story> stories = store.stories(ids);
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        for (String id : ids) {
            NewsStoryStore.Story s = stories.get(id);
            if (s != null) {
                Map<String, Object> view = story(s);
                items.add(view);
                byId.put(s.id(), view);
            }
        }
        addTranslations(NewsTranslationStore.STORY, byId);
        return new Page(items, next);
    }

    /** The summary view of a story ({@code NewsStory} without the detail members). */
    public static Map<String, Object> story(NewsStoryStore.Story s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.id());
        out.put("ref", "news-story:" + s.id());
        out.put("canonicalEventId", s.id());
        out.put("title", s.title());
        out.put("firstSeenAt", s.firstSeenAt() == null ? null : s.firstSeenAt().toString());
        out.put("lastSeenAt", s.lastSeenAt() == null ? null : s.lastSeenAt().toString());
        out.put("articleCount", s.articleCount());
        out.put("outletCount", s.outletCount());
        out.put("evidenceGroupCount", s.evidenceGroupCount());
        out.put("topicIds", s.topicIds());
        out.put("regionCodes", s.regionCodes());
        out.put("coverage", s.coverage());
        out.put("blindspot", s.blindspot());
        out.put("corroborated", s.corroborated());
        out.put("situationIds", s.situationIds());
        return out;
    }

    /**
     * The detail view of a story.
     *
     * @param context the caller (for the linked envelopes)
     * @param path the read path (for {@code LOCAL_ONLY} ratings)
     * @throws NotFoundException 404 {@code UNKNOWN_STORY}
     */
    public Map<String, Object> storyDetail(String id, ResolveContext context, EgressPath path, ArticleFilter filter) {
        NewsStoryStore.Story s = store.story(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_STORY,
                "No news story '" + id + "'."));
        Map<String, Object> out = story(s);
        addTranslations(NewsTranslationStore.STORY, Map.of(s.id(), out));
        boolean local = localAllowed(path);
        List<NewsStoryStore.Article> articles = new ArrayList<>();
        for (NewsStoryStore.Article a : store.articles(s.id())) {
            if (filter == null || filter.visible(a.sourceId())) {
                articles.add(a);
            }
        }
        Set<String> outletIds = new LinkedHashSet<>();
        for (NewsStoryStore.Article a : articles) {
            if (a.outletId() != null) {
                outletIds.add(a.outletId());
            }
        }
        Map<String, OutletRegistry.Outlet> byId = outlets.byIds(outletIds);
        Set<String> resolved = new LinkedHashSet<>();
        for (OutletRegistry.Outlet o : byId.values()) {
            resolved.add(o.id());
        }
        Map<String, Map<String, Consensus.Result>> consensus = outlets.consensus(resolved);
        Map<String, List<NewsStoryStore.Article>> byOutlet = new LinkedHashMap<>();
        for (NewsStoryStore.Article a : articles) {
            OutletRegistry.Outlet o = a.outletId() == null ? null : byId.get(a.outletId());
            byOutlet.computeIfAbsent(o == null ? "domain:" + a.domain() : o.id(), k -> new ArrayList<>()).add(a);
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        Map<String, Map<String, Object>> articleViews = new LinkedHashMap<>();
        for (Map.Entry<String, List<NewsStoryStore.Article>> e : byOutlet.entrySet()) {
            NewsStoryStore.Article first = e.getValue().get(0);
            OutletRegistry.Outlet o = first.outletId() == null ? null : byId.get(first.outletId());
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("outlet", o == null ? unknownOutlet(first) : OutletViews.outlet(o, outlets.ratings(o.id()),
                    consensus.getOrDefault(o.id(), Map.of()), local));
            List<Map<String, Object>> list = new ArrayList<>();
            for (NewsStoryStore.Article a : e.getValue()) {
                Map<String, Object> view = article(a, o);
                list.add(view);
                articleViews.put(a.id(), view);
            }
            g.put("articles", list);
            g.put("evidenceGroupId", first.evidenceGroupId());
            groups.add(g);
        }
        addTranslations(NewsTranslationStore.ARTICLE, articleViews);
        out.put("outlets", groups);
        List<ObjectEnvelope> situations = new ArrayList<>();
        List<ObjectEnvelope> events = new ArrayList<>();
        ObjectService service = objects.getIfAvailable();
        if (service != null && context != null) {
            for (String sid : s.situationIds()) {
                envelope(service, "situation:" + sid, context).ifPresent(situations::add);
            }
            envelope(service, "canonical-event:" + s.id(), context).ifPresent(events::add);
            for (String cid : s.corroboratedBy()) {
                envelope(service, "canonical-event:" + cid, context).ifPresent(events::add);
            }
        }
        out.put("situations", situations);
        out.put("canonicalEvents", events);
        return out;
    }

    private static java.util.Optional<ObjectEnvelope> envelope(ObjectService service, String ref,
            ResolveContext context) {
        try {
            return service.find(ObjectRef.parse(ref), context);
        } catch (RuntimeException e) {
            return java.util.Optional.empty();
        }
    }

    private static Map<String, Object> unknownOutlet(NewsStoryStore.Article a) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", null);
        out.put("ref", null);
        out.put("name", a.domain());
        out.put("domains", List.of(a.domain()));
        out.put("type", "other");
        out.put("rated", false);
        out.put("consensus", List.of());
        out.put("createdAt", null);
        return out;
    }

    // ------------------------------------------------------------------------- articles

    /**
     * Lists articles, newest first.
     *
     * @param storyId a story, or null
     * @param outletId an outlet, or null
     * @param since the least {@code seenAt}, or null
     */
    public Page articles(String storyId, String outletId, Instant since, String cursor, int limit,
            ArticleFilter filter) {
        ArticleFilter f = filter == null ? ArticleFilter.ALL : filter;
        if (f.none()) {
            return new Page(List.of(), null);
        }
        StringBuilder sql = new StringBuilder("SELECT id, seen_at FROM news_article WHERE true");
        List<Object> args = new ArrayList<>();
        if (f.allowed() != null) {
            sql.append(" AND source_id = ANY(?)");
            args.add(f.allowed().toArray(String[]::new));
        }
        if (!f.excluded().isEmpty()) {
            sql.append(" AND NOT (source_id = ANY(?))");
            args.add(f.excluded().toArray(String[]::new));
        }
        if (storyId != null && !storyId.isBlank()) {
            sql.append(" AND story_id = ?");
            args.add(storyId.strip());
        }
        if (outletId != null && !outletId.isBlank()) {
            String resolved = outlets.find(outletId.strip()).map(OutletRegistry.Outlet::id).orElse(outletId.strip());
            sql.append(" AND outlet_id = ?");
            args.add(resolved);
        }
        if (since != null) {
            sql.append(" AND seen_at >= ?");
            args.add(Timestamp.from(since));
        }
        Cursor c = Cursor.parse(cursor);
        if (c != null) {
            sql.append(" AND (seen_at < ? OR (seen_at = ? AND id < ?))");
            args.add(Timestamp.from(c.at()));
            args.add(Timestamp.from(c.at()));
            args.add(c.id());
        }
        sql.append(" ORDER BY seen_at DESC, id DESC LIMIT ?");
        args.add(limit + 1);
        List<String[]> rows = jdbc.query(sql.toString(), (rs, i) -> new String[] {rs.getString(1),
                rs.getTimestamp(2).toInstant().toString()}, args.toArray());
        String next = null;
        if (rows.size() > limit) {
            rows = rows.subList(0, limit);
            String[] last = rows.get(rows.size() - 1);
            next = new Cursor(Instant.parse(last[1]), last[0]).format();
        }
        List<String> ids = new ArrayList<>();
        for (String[] r : rows) {
            ids.add(r[0]);
        }
        Map<String, NewsStoryStore.Article> byKey = store.articlesByKey(ids);
        Set<String> outletIds = new LinkedHashSet<>();
        for (NewsStoryStore.Article a : byKey.values()) {
            if (a.outletId() != null) {
                outletIds.add(a.outletId());
            }
        }
        Map<String, OutletRegistry.Outlet> byId = outlets.byIds(outletIds);
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Map<String, Object>> views = new LinkedHashMap<>();
        for (String id : ids) {
            NewsStoryStore.Article a = byKey.get(id);
            if (a != null) {
                Map<String, Object> view = article(a, a.outletId() == null ? null : byId.get(a.outletId()));
                items.add(view);
                views.put(a.id(), view);
            }
        }
        addTranslations(NewsTranslationStore.ARTICLE, views);
        return new Page(items, next);
    }

    /** A {@code NewsArticle}. */
    public static Map<String, Object> article(NewsStoryStore.Article a, OutletRegistry.Outlet o) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", "record:" + a.sourceId() + "/" + a.externalId().replace("%", "%25").replace("/", "%2F")
                .replace("@", "%40") + "@" + a.seq());
        out.put("externalId", a.externalId());
        out.put("title", a.title());
        out.put("url", a.url());
        out.put("domain", a.domain());
        out.put("outletId", o != null ? o.id() : a.outletId());
        out.put("outletName", o == null ? null : o.name());
        out.put("language", a.language());
        out.put("sourceCountry", a.sourceCountry());
        out.put("seenAt", a.seenAt() == null ? null : a.seenAt().toString());
        out.put("publishedAt", a.publishedAt() == null ? null : a.publishedAt().toString());
        out.put("topicIds", a.topicIds());
        out.put("summary", a.summary());
        out.put("imageUrl", a.imageUrl());
        out.put("storyId", a.storyId());
        out.put("evidenceGroupId", a.evidenceGroupId());
        out.put("syndicated", a.syndicated());
        out.put("sourceId", a.sourceId());
        return out;
    }

    // ------------------------------------------------------------------------- outlets

    /** A page of outlets with their consensus (list view). */
    public Page outlets(String q, String country, String type, String bucket, String cursor, int limit,
            EgressPath path) {
        OutletRegistry.Page page = outlets.list(q, country, type, bucket, cursor, limit);
        List<String> ids = new ArrayList<>();
        for (OutletRegistry.Outlet o : page.items()) {
            ids.add(o.id());
        }
        Map<String, Map<String, Consensus.Result>> consensus = outlets.consensus(ids);
        boolean local = localAllowed(path);
        List<Map<String, Object>> items = new ArrayList<>();
        for (OutletRegistry.Outlet o : page.items()) {
            items.add(OutletViews.outlet(o, null, consensus.getOrDefault(o.id(), Map.of()), local));
        }
        return new Page(items, page.nextCursor());
    }

    /**
     * One outlet with its ratings per provider, consensus and owner.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_OUTLET}
     */
    public Map<String, Object> outlet(String id, EgressPath path) {
        OutletRegistry.Outlet o = outlets.get(id);
        return OutletViews.outlet(o, outlets.ratings(o.id()), outlets.consensus(o.id()), localAllowed(path));
    }

    /**
     * A keyset cursor {@code {epochMillis}~{id}}.
     *
     * @param at the time
     * @param id the id
     */
    record Cursor(Instant at, String id) {

        String format() {
            return at.toEpochMilli() + "~" + id;
        }

        static Cursor parse(String text) {
            if (text == null || text.isBlank()) {
                return null;
            }
            int tilde = text.indexOf('~');
            if (tilde <= 0) {
                throw ai.foresignal.api.error.ValidationException.forField("cursor", "is not a cursor of this list");
            }
            try {
                return new Cursor(Instant.ofEpochMilli(Long.parseLong(text.substring(0, tilde))),
                        text.substring(tilde + 1));
            } catch (NumberFormatException e) {
                throw ai.foresignal.api.error.ValidationException.forField("cursor", "is not a cursor of this list");
            }
        }
    }
}
