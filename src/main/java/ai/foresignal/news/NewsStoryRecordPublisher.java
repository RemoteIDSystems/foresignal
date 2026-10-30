package ai.foresignal.news;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.NormalizedRecord;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes news stories as records of the DERIVED source {@code foresignal-intel} (prompt 133), kind {@value #KIND},
 * externalId = the story id, so the 118 situation linker reads them like its other inputs (topic, region, time) and
 * situations show their coverage; rules, SSE, tiles and search see them unchanged. The {@code ForesignalIntelGenerator}
 * drives it on its ticks.
 *
 * <p>Payload: {@code title}, {@code canonicalEventId}, {@code topicIds}, {@code regionCodes}, {@code articleCount},
 * {@code outletCount}, {@code evidenceGroupCount}, {@code coverageBalance}, {@code blindspot},
 * {@code stateAffiliatedShare}, {@code corroborated}, {@code firstSeenAt}, {@code lastSeenAt}, {@code private: false}
 * and {@code automated: true}; and, once NP-002 has translated the story's title, {@code translatedTitles} (target
 * language to title) beside the untouched {@code title}: derived counts and shares only, never a provider's rating. A record is emitted only when
 * its payload changed; a story idle for longer than the {@code intel} retention class is tombstoned.
 */
@Component
public class NewsStoryRecordPublisher {

    /** The record kind. */
    public static final String KIND = "news-story";

    /** How long an idle story stays published (the {@code intel} class), days. */
    public static final int RETENTION_DAYS = 1825;

    /** Stories read per page. */
    static final int PAGE = 500;

    /** The overlap of an incremental pass, minutes. */
    static final int OVERLAP_MINUTES = 5;

    private final NewsStoryStore store;
    private final JdbcTemplate jdbc;
    private final NewsTranslationStore translations;
    private final Map<String, Integer> published = new HashMap<>();
    private Instant watermark;

    /** Creates the publisher. */
    public NewsStoryRecordPublisher(NewsStoryStore store, JdbcTemplate jdbc, NewsTranslationStore translations) {
        this.store = store;
        this.jdbc = jdbc;
        this.translations = translations;
    }

    /** Rebuilds the published set from the source's own live heads; the next tick is a full pass. */
    public synchronized void reconcile(List<HeadSnapshot> ownHeads) {
        published.clear();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone() && KIND.equals(head.kind())) {
                published.put(head.externalId(), null);
            }
        }
        watermark = null;
    }

    /** Publishes the changed stories (all of them after a reconcile) and tombstones the idle ones. */
    public synchronized void onTick(Instant now, DerivedContext ctx) {
        Instant since = watermark == null ? Instant.EPOCH : watermark.minus(Duration.ofMinutes(OVERLAP_MINUTES));
        Instant cursor = since;
        String lastId = "";
        while (true) {
            List<String> ids = jdbc.queryForList("SELECT id FROM news_story WHERE (updated_at > ? OR (updated_at = ?"
                    + " AND id > ?)) ORDER BY updated_at, id LIMIT ?", String.class, Timestamp.from(cursor),
                    Timestamp.from(cursor), lastId, PAGE);
            if (ids.isEmpty()) {
                break;
            }
            Map<String, NewsStoryStore.Story> stories = store.stories(ids);
            for (String id : ids) {
                NewsStoryStore.Story s = stories.get(id);
                if (s != null) {
                    apply(s, now, ctx);
                    cursor = s.updatedAt();
                    lastId = s.id();
                }
            }
            if (ids.size() < PAGE) {
                break;
            }
        }
        watermark = now;
    }

    private void apply(NewsStoryStore.Story s, Instant now, DerivedContext ctx) {
        boolean live = s.articleCount() > 0 && s.lastSeenAt() != null
                && s.lastSeenAt().plus(Duration.ofDays(RETENTION_DAYS)).isAfter(now);
        if (live) {
            Map<String, String> titles = new LinkedHashMap<>();
            translations.servedAll(NewsTranslationStore.STORY, s.id()).forEach((language, t) -> {
                if (t.translatedTitle() != null) {
                    titles.put(language, t.translatedTitle());
                }
            });
            Map<String, Object> payload = payload(s, titles);
            int hash = payload.hashCode();
            Integer last = published.get(s.id());
            if (last == null || last != hash) {
                ctx.emit(NormalizedRecord.builder(s.id(), KIND).observedAt(s.updatedAt()).eventTime(s.firstSeenAt())
                        .putAll(payload).build());
                published.put(s.id(), hash);
            }
        } else if (published.containsKey(s.id())) {
            published.remove(s.id());
            ctx.tombstone(KIND, s.id());
        }
    }

    /**
     * The record payload of a story.
     *
     * @param translatedTitles target language to translated title (NP-002); empty for none
     */
    static Map<String, Object> payload(NewsStoryStore.Story s, Map<String, String> translatedTitles) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("title", s.title());
        p.put("canonicalEventId", s.id());
        p.put("topicIds", new ArrayList<>(s.topicIds()));
        p.put("regionCodes", new ArrayList<>(s.regionCodes()));
        p.put("articleCount", s.articleCount());
        p.put("outletCount", s.outletCount());
        p.put("evidenceGroupCount", s.evidenceGroupCount());
        p.put("coverageBalance", s.coverage().get("coverageBalance"));
        p.put("blindspot", s.blindspot());
        p.put("stateAffiliatedShare", s.stateAffiliatedShare());
        p.put("corroborated", s.corroborated());
        p.put("firstSeenAt", s.firstSeenAt() == null ? null : s.firstSeenAt().toString());
        p.put("lastSeenAt", s.lastSeenAt() == null ? null : s.lastSeenAt().toString());
        p.put("private", false);
        p.put("automated", true);
        if (translatedTitles != null && !translatedTitles.isEmpty()) {
            p.put("translatedTitles", new LinkedHashMap<>(translatedTitles));
        }
        return p;
    }

    /** True when the story currently has a published record (tests). */
    synchronized boolean isPublished(String id) {
        return published.containsKey(id);
    }
}
