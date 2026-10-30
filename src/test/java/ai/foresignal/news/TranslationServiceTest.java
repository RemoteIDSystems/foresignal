package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link TranslationService} (prompt NP-002) with a scripted {@link FakeLlmClient} and an in-memory
 * {@link NewsTranslationStore}: a foreign article is translated beside its untouched original, the same content is
 * never translated twice, changed content is, an exhausted budget skips and is recorded, and articles already in the
 * target language or of uncertain language make no call.
 */
class TranslationServiceTest {

    static final Instant T0 = Instant.parse("2026-10-30T12:00:00Z");

    final MutableClock clock = new MutableClock(T0);
    final LlmService llm = mock(LlmService.class);
    final TranslationSettingsService settings = mock(TranslationSettingsService.class);
    final FakeLlmClient chat = new FakeLlmClient("chat");
    final MemoryStore store = new MemoryStore();
    final TranslationSettings toEnglish = new TranslationSettings(true, List.of("en"), null, null, null, null);
    TranslationService service;

    /** The store without a database. */
    static final class MemoryStore extends NewsTranslationStore {

        final Map<String, Translation> rows = new LinkedHashMap<>();

        MemoryStore() {
            super(null);
        }

        static String key(String type, String id, String language) {
            return type + "|" + id + "|" + language;
        }

        @Override
        public Optional<Translation> find(String objectType, String objectId, String targetLanguage) {
            return Optional.ofNullable(rows.get(key(objectType, objectId, targetLanguage)));
        }

        @Override
        public void upsert(Translation t) {
            rows.put(key(t.objectType(), t.objectId(), t.targetLanguage()), t);
        }

        @Override
        public void markChecked(String objectType, String objectId, String targetLanguage, Instant now) {}

        @Override
        public BigDecimal spentSince(Instant since) {
            return BigDecimal.ZERO;
        }
    }

    @BeforeEach
    void setUp() {
        when(llm.chatClient()).thenReturn(Optional.of(chat));
        when(settings.exhausted(any())).thenReturn(false);
        service = new TranslationService(llm, store, settings, clock);
    }

    static NewsStoryStore.Article article(String id, String title, String summary, String language) {
        return new NewsStoryStore.Article("gdelt-articles/" + id, "gdelt-articles", id, 1, null, null, null, null, null,
                "example.de", title, List.of(), "https://example.de/" + id, language, "DE", T0, T0, List.of(),
                List.of(), summary, null, false, null, null, null, T0);
    }

    /** The content between the delimiters of the last request. */
    String lastContent() {
        String user = ((ChatMessage.User) chat.requests().getLast().messages().getFirst()).text();
        return user.substring(user.indexOf("<<<TEXT") + 8, user.indexOf("TEXT>>>") - 1);
    }

    @Test
    @DisplayName("a German article is translated into English beside the original; purpose translate")
    void translatesForeignArticle() {
        chat.otherwise(r -> FakeLlmClient.text(lastContentOf(r).startsWith("Erdbeben")
                ? "Earthquake in Bavaria" : "Strong shaking reported near Munich"));
        NewsStoryStore.Article a = article("a1", "Erdbeben in Bayern", "Starkes Beben bei München gemeldet", "de");

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.TRANSLATED);

        NewsTranslationStore.Translation t = store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow();
        assertThat(t.ok()).isTrue();
        assertThat(t.translatedTitle()).isEqualTo("Earthquake in Bavaria");
        assertThat(t.translatedExcerpt()).isEqualTo("Strong shaking reported near Munich");
        assertThat(t.sourceLanguage()).isEqualTo("de");
        assertThat(t.engine()).isEqualTo("llm");
        assertThat(t.translatedAt()).isEqualTo(T0);
        assertThat(a.title()).isEqualTo("Erdbeben in Bayern");
        assertThat(a.summary()).isEqualTo("Starkes Beben bei München gemeldet");
        assertThat(chat.requests()).hasSize(2).allSatisfy(r -> {
            assertThat(r.purpose()).isEqualTo(Purpose.TRANSLATE);
            assertThat(r.system()).isEqualTo(TranslationService.SYSTEM);
            assertThat(r.tools()).isEmpty();
        });
        assertThat(t.wire()).containsEntry("targetLanguage", "en").containsEntry("translatedTitle",
                "Earthquake in Bavaria").containsKey("translationProvenance");
    }

    static String lastContentOf(ai.foresignal.llm.client.ChatRequest r) {
        String user = ((ChatMessage.User) r.messages().getFirst()).text();
        return user.substring(user.indexOf("<<<TEXT") + 8, user.indexOf("TEXT>>>") - 1);
    }

    @Test
    @DisplayName("the same content twice is translated once; changed content is translated again")
    void idempotent() {
        chat.otherwise(r -> FakeLlmClient.text("Earthquake"));
        NewsStoryStore.Article a = article("a2", "Erdbeben", null, "de");

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.TRANSLATED);
        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.UNCHANGED);
        assertThat(chat.requests()).hasSize(1);

        NewsStoryStore.Article changed = article("a2", "Erdbeben der Stärke 5", null, "de");
        assertThat(service.translateArticle(changed, toEnglish, "en"))
                .isEqualTo(TranslationService.Outcome.TRANSLATED);
        assertThat(chat.requests()).hasSize(2);
        assertThat(lastContent()).isEqualTo("Erdbeben der Stärke 5");
    }

    @Test
    @DisplayName("an exhausted translation budget skips without a call and is recorded")
    void budgetExhausted() {
        when(settings.exhausted(any())).thenReturn(true);
        NewsStoryStore.Article a = article("a3", "Erdbeben", null, "de");

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.SKIPPED);

        assertThat(chat.requests()).isEmpty();
        NewsTranslationStore.Translation t = store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow();
        assertThat(t.status()).isEqualTo(NewsTranslationStore.SKIPPED);
        assertThat(t.reason()).isEqualTo(TranslationService.BUDGET);
    }

    @Test
    @DisplayName("the provider governor refusing on its own caps is a budget skip too")
    void governorRefuses() {
        chat.otherwise(r -> {
            throw new LlmBudgetExceededException("chat", "spend cap", Duration.ofHours(1));
        });
        NewsStoryStore.Article a = article("a4", "Erdbeben", null, "de");

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.SKIPPED);
        assertThat(store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow().reason())
                .isEqualTo(TranslationService.BUDGET);
    }

    @Test
    @DisplayName("an article already in the target language (en-US is en) makes no call")
    void sameLanguage() {
        NewsStoryStore.Article a = article("a5", "Earthquake in Texas", null, "en-US");

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.SKIPPED);
        assertThat(chat.requests()).isEmpty();
        assertThat(store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow().reason())
                .isEqualTo(TranslationService.SAME_LANGUAGE);
    }

    @Test
    @DisplayName("a Latin-script article without a declared language is below the confidence floor: no call")
    void uncertainLanguage() {
        NewsStoryStore.Article a = article("a6", "Terremoto en Chile", null, null);

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.SKIPPED);
        assertThat(chat.requests()).isEmpty();
        assertThat(store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow().reason())
                .isEqualTo(TranslationService.LOW_CONFIDENCE);
    }

    @Test
    @DisplayName("an undeclared Korean headline is detected by script and translated")
    void detectedByScript() {
        chat.otherwise(r -> FakeLlmClient.text("Earthquake in Seoul"));
        NewsStoryStore.Article a = article("a7", "서울에서 지진 발생", null, null);

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.TRANSLATED);
        assertThat(store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow().sourceLanguage())
                .isEqualTo("ko");
    }

    @Test
    @DisplayName("an over-long answer is rejected: a failed row, no translation served")
    void rejectedOutput() {
        chat.otherwise(r -> FakeLlmClient.text("x".repeat(500)));
        NewsStoryStore.Article a = article("a8", "Erdbeben", null, "de");

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.FAILED);
        NewsTranslationStore.Translation t = store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow();
        assertThat(t.ok()).isFalse();
        assertThat(t.translatedTitle()).isNull();
    }

    @Test
    @DisplayName("no chat provider: skipped and recorded, nothing called")
    void noProvider() {
        when(llm.chatClient()).thenReturn(Optional.empty());
        NewsStoryStore.Article a = article("a9", "Erdbeben", null, "de");

        assertThat(service.translateArticle(a, toEnglish, "en")).isEqualTo(TranslationService.Outcome.SKIPPED);
        assertThat(store.find(NewsTranslationStore.ARTICLE, a.id(), "en").orElseThrow().reason())
                .isEqualTo(TranslationService.NO_PROVIDER);
    }

    @Test
    @DisplayName("the language detector: declared names and codes, kana, Hangul, Cyrillic; Latin unknown")
    void detector() {
        assertThat(LanguageDetector.detect("German", "x").language()).isEqualTo("de");
        assertThat(LanguageDetector.detect("pt-br", "x").language()).isEqualTo("pt");
        assertThat(LanguageDetector.detect(null, "東京で地震が発生").language()).isEqualTo("ja");
        assertThat(LanguageDetector.detect(null, "서울 지진").confidence()).isEqualTo(LanguageDetector.UNAMBIGUOUS);
        assertThat(LanguageDetector.detect(null, "Землетрясение в Москве"))
                .isEqualTo(new LanguageDetector.Detection("ru", LanguageDetector.SHARED));
        assertThat(LanguageDetector.detect(null, "Earthquake")).isEqualTo(LanguageDetector.Detection.UNKNOWN);
    }
}
