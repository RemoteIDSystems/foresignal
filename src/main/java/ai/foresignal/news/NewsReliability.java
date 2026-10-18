package ai.foresignal.news;

import ai.foresignal.media.outlet.Consensus;
import ai.foresignal.media.outlet.MediaVocabulary;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.formats.news.NewsArticles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Outlet factuality as source reliability for news evidence (prompt 133, setting {@code media.reliabilityMap}): the
 * outlet's factuality consensus band maps to a 109 Admiralty reliability grade (default 5 → A, 4 → B, 3 → C, 2 → D,
 * 1 → E, unrated → F "cannot be judged"). 101's {@code EventScorer} takes the grade's number ({@link #numeric}) as a
 * news member's reliability; 125's confidence assessor takes the grade.
 */
@Component
public class NewsReliability {

    /** The default map. */
    public static final Map<String, String> DEFAULT_MAP = Map.of("5", "A", "4", "B", "3", "C", "2", "D", "1", "E",
            MediaVocabulary.UNRATED, "F");

    /** The reliability numbers of grades A–E; F (cannot be judged) has none, so the scorer's default applies. */
    public static final Map<String, Double> NUMERIC = Map.of("A", 0.9, "B", 0.75, "C", 0.6, "D", 0.45, "E", 0.3);

    private static final Set<String> GRADES = Set.of("A", "B", "C", "D", "E", "F");

    private final NewsStoryStore store;
    private final OutletRegistry outlets;
    private final ObjectProvider<AppSettingService> settings;

    /** Creates the component. */
    public NewsReliability(NewsStoryStore store, OutletRegistry outlets, ObjectProvider<AppSettingService> settings) {
        this.store = store;
        this.outlets = outlets;
        this.settings = settings;
    }

    /** The map in force: the setting merged over the default; invalid entries are ignored. */
    public Map<String, String> map() {
        Map<String, String> out = new LinkedHashMap<>(DEFAULT_MAP);
        AppSettingService service = settings.getIfAvailable();
        if (service == null) {
            return out;
        }
        try {
            service.get(SettingKeys.MEDIA_RELIABILITY_MAP, Map.class).ifPresent(m -> {
                for (Object k : m.keySet()) {
                    Object v = m.get(k);
                    if (v != null && GRADES.contains(String.valueOf(v)) && out.containsKey(String.valueOf(k))) {
                        out.put(String.valueOf(k), String.valueOf(v));
                    }
                }
            });
        } catch (RuntimeException e) {
            // keep the default
        }
        return out;
    }

    /** The grade of a factuality band. */
    public String gradeOfBand(String band) {
        return map().getOrDefault(band == null ? MediaVocabulary.UNRATED : band, "F");
    }

    /**
     * The reliability grade of a news-article record, or null when the record is not a news article or its outlet is
     * unknown.
     */
    public String grade(String sourceId, String externalId) {
        if (!NewsArticles.isNewsSource(sourceId) || externalId == null) {
            return null;
        }
        NewsStoryStore.Article a = store.article(sourceId, externalId).orElse(null);
        if (a == null || a.outletId() == null) {
            return null;
        }
        Map<String, Consensus.Result> consensus = outlets.consensus(a.outletId());
        Consensus.Result f = consensus.get(MediaVocabulary.FACTUALITY);
        return gradeOfBand(CoverageAnalyzer.factualityBand(f == null ? null : f.median()));
    }

    /** The reliability number of a news-article record for the event scorer, or null (the scorer's default). */
    public Double reliability(String sourceId, String externalId) {
        return numeric(grade(sourceId, externalId));
    }

    /** The number of a grade, or null for F or none. */
    public static Double numeric(String grade) {
        return grade == null ? null : NUMERIC.get(grade);
    }
}
