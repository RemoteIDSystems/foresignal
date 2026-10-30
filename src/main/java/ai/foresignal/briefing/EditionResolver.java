package ai.foresignal.briefing;

import ai.foresignal.news.EditionNews;
import ai.foresignal.notify.NotificationTranslator;
import ai.foresignal.preference.UserPreferencesEntity;
import ai.foresignal.preference.UserPreferencesRepository;
import ai.foresignal.source.view.UserSourceViewService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Resolves a briefing's {@link EditionContext} (prompt NP-003) and builds the "Top stories" section of a user's
 * edition.
 *
 * <ul>
 *   <li><b>Language:</b> the briefing's {@code outputLanguage}, else, for an owned edition, the owner's
 *       {@code UserPreferences.locale}; none otherwise.
 *   <li><b>Data:</b> an owned edition follows the owner's data: the sources the owner turned off
 *       ({@link UserSourceViewService#hiddenFor}) are hidden from it like private sources, and its news is limited to
 *       the owner's news subscriptions.
 *   <li><b>News:</b> {@value #TOP_STORIES_KEY} lists the period's top stories ({@link EditionNews}), titled in the
 *       edition's language where NP-002 translated them. It reaches the digest, the LLM writer and the audio like any
 *       other section.
 *   <li><b>Delivery:</b> an owned edition that lists no channels goes to its owner's personal channels
 *       ({@link #ownerChannels}), never to every channel subscribed to {@code briefing}, which would hand one user's
 *       edition to everyone.
 * </ul>
 */
@Component
public class EditionResolver {

    /** The {@code DigestStats.sections} key of the edition's news. */
    public static final String TOP_STORIES_KEY = "topStories";

    /** The section heading. */
    public static final String TOP_STORIES_HEADING = "Top stories";

    private final ObjectProvider<UserPreferencesRepository> preferences;
    private final ObjectProvider<UserSourceViewService> sourceViews;
    private final ObjectProvider<EditionNews> news;
    private final JdbcTemplate jdbc;

    /** Creates the resolver. */
    public EditionResolver(ObjectProvider<UserPreferencesRepository> preferences,
            ObjectProvider<UserSourceViewService> sourceViews, ObjectProvider<EditionNews> news, JdbcTemplate jdbc) {
        this.preferences = preferences;
        this.sourceViews = sourceViews;
        this.news = news;
        this.jdbc = jdbc;
    }

    /** The personal notification channels (R2-009) of user {@code ownerUserId}: those its principal owns. */
    public Set<UUID> ownerChannels(UUID ownerUserId) {
        return Set.copyOf(jdbc.query("SELECT c.id FROM notification_channel c JOIN \"user\" u"
                + " ON u.principal_id = c.owner_principal_id WHERE u.id = ?",
                (rs, i) -> rs.getObject(1, UUID.class), ownerUserId));
    }

    /** The edition context of {@code briefing}, or null for a plain briefing (no owner, no output language). */
    public EditionContext resolve(BriefingEntity briefing) {
        UUID owner = briefing.getOwnerUserId();
        String language = NotificationTranslator.canonicalTag(briefing.getOutputLanguage());
        if (owner == null) {
            return language == null ? null : new EditionContext(null, language, Set.of());
        }
        if (language == null) {
            language = locale(owner);
        }
        Set<String> hidden = Set.of();
        UserSourceViewService views = sourceViews.getIfAvailable();
        if (views != null) {
            try {
                hidden = views.hiddenFor(owner);
            } catch (RuntimeException e) {
                hidden = Set.of();
            }
        }
        return new EditionContext(owner, language, hidden);
    }

    /** The owner's preferred locale as a canonical tag, or null. */
    String locale(UUID owner) {
        UserPreferencesRepository repo = preferences.getIfAvailable();
        if (repo == null) {
            return null;
        }
        return repo.findById(owner).map(UserPreferencesEntity::getLocale).map(NotificationTranslator::canonicalTag)
                .orElse(null);
    }

    /**
     * The "Top stories" section of an owned edition, or empty for a plain briefing or a period without news.
     *
     * @param edition the edition context, or null
     * @param from the period start
     * @param to the period end
     * @param hiddenSources every source the edition may not list
     */
    public Optional<BriefingSection> topStories(EditionContext edition, Instant from, Instant to,
            Set<String> hiddenSources) {
        EditionNews source = news.getIfAvailable();
        if (edition == null || !edition.owned() || source == null) {
            return Optional.empty();
        }
        List<EditionNews.Story> stories = source.topStories(from, to, edition.ownerUserId(), edition.language(),
                hiddenSources, EditionNews.MAX_STORIES);
        if (stories.isEmpty()) {
            return Optional.empty();
        }
        List<Map<String, Object>> items = new ArrayList<>();
        StringBuilder md = new StringBuilder("## ").append(TOP_STORIES_HEADING).append("\n\n");
        for (EditionNews.Story s : stories) {
            items.add(s.toMap());
            md.append("- ").append(s.title()).append(" (").append(s.outletCount())
                    .append(s.outletCount() == 1 ? " outlet" : " outlets").append(")\n");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        Map<String, Object> llm = new LinkedHashMap<>();
        llm.put("instruction", "The news stories most covered in the period, already limited to the reader's "
                + "subscriptions; titles are in the edition's language where a translation exists.");
        llm.put("stories", items);
        return Optional.of(new BriefingSection(TOP_STORIES_KEY, data, md.toString(), llm));
    }
}
