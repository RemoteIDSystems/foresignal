package ai.foresignal.news;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Translates news as it is ingested (prompt NP-002). Once a minute, while {@link TranslationSettings#active()}, it takes
 * the articles seen and the stories active in the last {@value #WINDOW_HOURS} hours that have no current translation in
 * each target language and hands them to {@link TranslationService}, newest first and at most {@value #BATCH} of each per
 * language and tick. It runs beside the story resolver rather than inside it, so a slow or unavailable LLM never holds
 * up ingest; a new article is translated within about a minute.
 *
 * <p>A translated story is marked changed, so {@link NewsStoryRecordPublisher} re-publishes its record with the
 * translated titles (for search, the situation linker and editions). A pass stops for a language as soon as the
 * translation budget is spent. Failed and budget-skipped objects are retried after {@value #RETRY_MINUTES} minutes.
 * Translations of expired articles are deleted hourly.
 */
@Component
public class NewsTranslationJob {

    private static final Logger log = LoggerFactory.getLogger(NewsTranslationJob.class);

    /** How far back new news is translated, hours. */
    static final int WINDOW_HOURS = 48;

    /** The most articles, and stories, per language and tick. */
    static final int BATCH = 100;

    /** How long a failed or budget-skipped object waits before another try, minutes. */
    static final int RETRY_MINUTES = 60;

    private final TranslationSettingsService settings;
    private final TranslationService translator;
    private final NewsTranslationStore translations;
    private final NewsStoryStore stories;
    private final Clock clock;
    private Instant lastCleanup = Instant.EPOCH;

    /** Creates the job. */
    public NewsTranslationJob(TranslationSettingsService settings, TranslationService translator,
            NewsTranslationStore translations, NewsStoryStore stories, Clock clock) {
        this.settings = settings;
        this.translator = translator;
        this.translations = translations;
        this.stories = stories;
        this.clock = clock;
    }

    /** One scheduled pass. */
    @Scheduled(fixedDelayString = "1m", initialDelayString = "2m")
    public void tick() {
        try {
            run();
        } catch (RuntimeException e) {
            log.warn("News translation pass failed: {}", e.getMessage(), e);
        }
    }

    /**
     * One pass over every target language.
     *
     * @return how many objects were newly translated
     */
    public synchronized int run() {
        Instant now = clock.instant();
        if (Duration.between(lastCleanup, now).toMinutes() >= 60) {
            lastCleanup = now;
            translations.deleteOrphanedArticles();
        }
        TranslationSettings s = settings.get();
        if (!s.active()) {
            return 0;
        }
        Instant since = now.minus(Duration.ofHours(WINDOW_HOURS));
        Instant retryBefore = now.minus(Duration.ofMinutes(RETRY_MINUTES));
        int translated = 0;
        for (String target : s.targetLanguages()) {
            translated += articles(s, target, since, retryBefore);
            translated += stories(s, target, since, retryBefore, now);
        }
        return translated;
    }

    private int articles(TranslationSettings s, String target, Instant since, Instant retryBefore) {
        List<String> ids = translations.articlesToTranslate(target, since, retryBefore, BATCH);
        Map<String, NewsStoryStore.Article> byKey = stories.articlesByKey(ids);
        int translated = 0;
        for (String id : ids) {
            NewsStoryStore.Article a = byKey.get(id);
            if (a == null) {
                continue;
            }
            TranslationService.Outcome outcome = translator.translateArticle(a, s, target);
            if (outcome == TranslationService.Outcome.TRANSLATED) {
                translated++;
            } else if (outcome == TranslationService.Outcome.SKIPPED && settings.exhausted(s)) {
                break;
            }
        }
        return translated;
    }

    private int stories(TranslationSettings s, String target, Instant since, Instant retryBefore, Instant now) {
        if (!s.translates(TranslationSettings.TITLE)) {
            return 0;
        }
        List<String> ids = translations.storiesToTranslate(target, since, retryBefore, BATCH);
        Map<String, NewsStoryStore.Story> byId = stories.stories(ids);
        int translated = 0;
        for (String id : ids) {
            NewsStoryStore.Story story = byId.get(id);
            if (story == null) {
                continue;
            }
            String declared = null;
            if (story.centralArticle() != null) {
                NewsStoryStore.Article central = stories.articlesByKey(List.of(story.centralArticle()))
                        .get(story.centralArticle());
                declared = central == null ? null : central.language();
            }
            TranslationService.Outcome outcome = translator.translateStory(story, declared, s, target);
            if (outcome == TranslationService.Outcome.TRANSLATED) {
                translated++;
                translations.touchStory(story.id(), now);
            } else if (outcome == TranslationService.Outcome.SKIPPED && settings.exhausted(s)) {
                break;
            }
        }
        return translated;
    }
}
