package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.news.EditionNews;
import ai.foresignal.notify.NotificationTranslator;
import ai.foresignal.preference.UserPreferencesRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.view.UserSourceViewService;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/**
 * Editions in the owner's language (prompt NP-003): the LLM writer is told the language, a digest is translated, the
 * audio voice speaks the language, and a story NP-002 translated is listed by its translated title.
 */
class EditionLanguageTest {

    static final Instant FROM = Instant.parse("2026-10-29T00:00:00Z");
    static final Instant TO = Instant.parse("2026-10-30T00:00:00Z");

    @Test
    @DisplayName("the LLM user turn asks for the edition's language; without one it is unchanged")
    void writerLanguage() {
        String plain = BriefingPrompts.userTurn("Daily", null, FROM, TO, ZoneId.of("UTC"), "{}");
        String spanish = BriefingPrompts.userTurn("Daily", null, FROM, TO, ZoneId.of("UTC"), "{}", "es");
        assertThat(plain).doesNotContain("Output language");
        assertThat(spanish).contains("Output language: write the whole briefing in Spanish (es)");
        assertThat(BriefingPrompts.userTurn("Daily", null, FROM, TO, ZoneId.of("UTC"), "{}", "pt-BR"))
                .contains("Portuguese (Brazil) (pt-BR)");
        assertThat(BriefingPrompts.SYSTEM).doesNotContain("Spanish");

        LlmBriefingWriter.Request legacy = new LlmBriefingWriter.Request("Daily", null, null, null, Set.of(),
                ZoneId.of("UTC"), FROM, TO, null);
        assertThat(legacy.language()).isNull();
    }

    @Test
    @DisplayName("a digest edition is translated through the translator; English needs no call")
    void digestTranslated() {
        LlmService llm = mock(LlmService.class);
        FakeLlmClient chat = new FakeLlmClient("chat");
        when(llm.chatClient()).thenReturn(Optional.of(chat));
        chat.otherwise(r -> FakeLlmClient.text("## Terremotos\n\n- M5.2 cerca de Tokio"));
        NotificationTranslator translator = new NotificationTranslator(llm, mock(AppSettingService.class),
                new MutableClock(TO));

        assertThat(translator.translateMarkdown("## Earthquakes\n\n- M5.2 near Tokyo", "es"))
                .isEqualTo("## Terremotos\n\n- M5.2 cerca de Tokio");
        assertThat(((ChatMessage.User) chat.requests().getFirst().messages().getFirst()).text())
                .contains("Target language (BCP 47): es (Spanish)");
        assertThat(translator.translateMarkdown("## Earthquakes", "en-US")).isEqualTo("## Earthquakes");
        assertThat(chat.requests()).hasSize(1);

        when(llm.chatClient()).thenReturn(Optional.empty());
        assertThat(translator.translateMarkdown("## Floods", "de")).isNull();
    }

    @Test
    @DisplayName("the audio voice speaks the edition's language, keeping the voice's gender; other voices are kept")
    void voice() {
        BriefingInput.Audio bella = new BriefingInput.Audio("af_bella", "mp3");
        assertThat(EditionVoice.match(bella, null)).isSameAs(bella);
        assertThat(EditionVoice.match(bella, "en")).isSameAs(bella);
        assertThat(EditionVoice.match(bella, "en-GB").voice()).isEqualTo("bf_emma");
        assertThat(EditionVoice.match(bella, "es").voice()).isEqualTo("ef_dora");
        assertThat(EditionVoice.match(bella, "ja").voice()).isEqualTo("jf_alpha");
        assertThat(EditionVoice.match(bella, "pt-BR").voice()).isEqualTo("pf_dora");
        assertThat(EditionVoice.match(bella, "pt-BR").format()).isEqualTo("mp3");
        assertThat(EditionVoice.match(new BriefingInput.Audio("am_adam", "opus"), "it").voice())
                .isEqualTo("im_nicola");
        assertThat(EditionVoice.match(new BriefingInput.Audio("em_alex", "mp3"), "en").voice())
                .isEqualTo("am_michael");
        assertThat(EditionVoice.match(bella, "de")).isSameAs(bella);
        BriefingInput.Audio alloy = new BriefingInput.Audio("alloy", "mp3");
        assertThat(EditionVoice.match(alloy, "ja")).isSameAs(alloy);
    }

    @Test
    @DisplayName("a translated story is listed in the edition's language with its original kept in the data")
    void translatedStory() {
        UUID owner = UUID.randomUUID();
        EditionNews news = new EditionNews(null, null, null) {
            @Override
            public List<Story> topStories(Instant from, Instant to, UUID ownerUserId, String language,
                    Set<String> hiddenSources, int limit) {
                assertThat(ownerUserId).isEqualTo(owner);
                assertThat(language).isEqualTo("es");
                assertThat(hiddenSources).contains("private-src");
                return List.of(new Story("s1", "Terremoto en Japón", "Earthquake in Japan", true, 4, 9, TO));
            }
        };
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("news", news);
        EditionResolver resolver = new EditionResolver(beans.getBeanProvider(UserPreferencesRepository.class),
                beans.getBeanProvider(UserSourceViewService.class), beans.getBeanProvider(EditionNews.class), null);

        EditionContext edition = new EditionContext(owner, "es", Set.of());
        BriefingSection section = resolver.topStories(edition, FROM, TO, Set.of("private-src")).orElseThrow();
        assertThat(section.key()).isEqualTo(EditionResolver.TOP_STORIES_KEY);
        assertThat(section.markdown()).contains("- Terremoto en Japón (4 outlets)").doesNotContain("Earthquake");
        @SuppressWarnings("unchecked")
        Map<String, Object> item = ((List<Map<String, Object>>) ((Map<String, Object>) section.data()).get("items"))
                .getFirst();
        assertThat(item).containsEntry("title", "Terremoto en Japón")
                .containsEntry("originalTitle", "Earthquake in Japan").containsEntry("ref", "news-story:s1");

        assertThat(resolver.topStories(new EditionContext(null, "es", Set.of()), FROM, TO, Set.of())).isEmpty();
    }

    @Test
    @DisplayName("the edition language is its outputLanguage, else the owner's locale; a plain briefing has none")
    void resolve() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        EditionResolver resolver = new EditionResolver(beans.getBeanProvider(UserPreferencesRepository.class),
                beans.getBeanProvider(UserSourceViewService.class), beans.getBeanProvider(EditionNews.class), null);
        BriefingEntity plain = new BriefingEntity(UUID.randomUUID());
        assertThat(resolver.resolve(plain)).isNull();
        plain.setOutputLanguage("fr");
        assertThat(resolver.resolve(plain).language()).isEqualTo("fr");
        assertThat(resolver.resolve(plain).owned()).isFalse();

        BriefingEntity owned = new BriefingEntity(UUID.randomUUID());
        owned.setOwnerUserId(UUID.randomUUID());
        owned.setOutputLanguage("ja");
        EditionContext context = resolver.resolve(owned);
        assertThat(context.owned()).isTrue();
        assertThat(context.language()).isEqualTo("ja");
    }
}
