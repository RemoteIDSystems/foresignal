package ai.foresignal.attention;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.gdelt.GdeltTimelineAdapter;
import ai.foresignal.source.adapters.wikimedia.WikimediaPageviewsAdapter;
import ai.foresignal.source.formats.attention.TopicParams;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Attention topics (prompt 117, {@code /v1/attention/topics}): CRUD, validation, the shipped examples, and the
 * <strong>sync</strong> into the {@code topics} parameter of {@code gdelt-timelines} and
 * {@code wikimedia-pageviews}.
 *
 * <p>Adapters never read the store (Architecture §3), so every change re-writes those two sources' {@code topics}
 * parameter through {@link SourceService#update} (validated by their parameter schemas, {@link TopicParams}) from the
 * enabled topics, keeping the sources' other stored parameters. Disabled topics are left out.
 *
 * <p>A GDELT query gets a dry character check ({@link TopicParams#queryProblem}) and, on create while
 * {@code gdelt-timelines} is enabled, one budgeted test call through the source's own adapter
 * ({@link GdeltTimelineAdapter#testQuery}, run as a probe: nothing is stored). A query GDELT does not answer with a
 * timeline is {@code 400 TOPIC_INVALID}; a test call the budget refuses is skipped, and the topic is accepted on the
 * dry check alone.
 */
@Service
public class AttentionTopicService {

    /** The topic id pattern. */
    public static final Pattern ID = TopicParams.TOPIC_ID;

    /** A 040 region code. */
    public static final Pattern REGION_CODE = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    /** The most keywords. */
    public static final int MAX_KEYWORDS = 30;

    /** The most themes and region codes. */
    public static final int MAX_TAGS = 30;

    /** The longest keyword, theme or description. */
    static final int MAX_TAG_LENGTH = 100;

    /** The longest description. */
    static final int MAX_DESCRIPTION = 2000;

    /** The default and largest page sizes. */
    public static final int DEFAULT_LIMIT = 100;

    /** The largest page size. */
    public static final int MAX_LIMIT = 500;

    private static final Logger log = LoggerFactory.getLogger(AttentionTopicService.class);

    private final AttentionTopicRepository topics;
    private final SourceService sources;
    private final ProbeService probes;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the service. */
    public AttentionTopicService(AttentionTopicRepository topics, SourceService sources, ProbeService probes,
            AppSettingService settings, Clock clock) {
        this.topics = topics;
        this.sources = sources;
        this.probes = probes;
        this.settings = settings;
        this.clock = clock;
    }

    /**
     * A page of topics.
     *
     * @param items the topics
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page(List<AttentionTopicEntity> items, String nextCursor) {}

    /**
     * Topics in id order.
     *
     * @param enabled only enabled (true) or disabled (false) topics, or null for all
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     */
    @Transactional(readOnly = true)
    public Page list(Boolean enabled, String cursor, int limit) {
        String after = decodeCursor(cursor);
        List<AttentionTopicEntity> all = enabled == null ? topics.findAllByOrderByIdAsc()
                : topics.findByEnabledOrderByIdAsc(enabled);
        List<AttentionTopicEntity> page = new ArrayList<>();
        boolean more = false;
        for (AttentionTopicEntity topic : all) {
            if (after != null && topic.getId().compareTo(after) <= 0) {
                continue;
            }
            if (page.size() == limit) {
                more = true;
                break;
            }
            page.add(topic);
        }
        String next = more ? encodeCursor(page.get(page.size() - 1).getId()) : null;
        return new Page(List.copyOf(page), next);
    }

    /**
     * One topic.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_ATTENTION_TOPIC}
     */
    @Transactional(readOnly = true)
    public AttentionTopicEntity get(String id) {
        return topics.findById(id == null ? "" : id).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_ATTENTION_TOPIC, "No attention topic has the id '" + id + "'."));
    }

    /**
     * Creates a topic and re-syncs the sources.
     *
     * @throws ValidationException 400 {@code TOPIC_INVALID}
     * @throws ConflictException 409 {@code ATTENTION_TOPIC_EXISTS}
     */
    @Transactional
    public AttentionTopicEntity create(AttentionTopicInput input, String createdBy) {
        validate(input, null);
        if (topics.existsById(input.id())) {
            throw new ConflictException(ErrorCodes.ATTENTION_TOPIC_EXISTS,
                    "An attention topic with the id '" + input.id() + "' exists.");
        }
        testGdeltQuery(input.gdeltQuery());
        Instant now = clock.instant();
        AttentionTopicEntity topic = new AttentionTopicEntity(input.id(), now);
        topic.setCreatedBy(createdBy);
        apply(topic, input);
        topics.save(topic);
        syncSources();
        return topic;
    }

    /**
     * Replaces a topic (the body id must match {@code id}) and re-syncs the sources.
     *
     * @throws ValidationException 400 {@code TOPIC_INVALID}
     * @throws NotFoundException 404 {@code UNKNOWN_ATTENTION_TOPIC}
     */
    @Transactional
    public AttentionTopicEntity update(String id, AttentionTopicInput input) {
        AttentionTopicEntity topic = get(id);
        validate(input, id);
        if (!Objects.equals(topic.getGdeltQuery(), blankToNull(input.gdeltQuery()))) {
            testGdeltQuery(input.gdeltQuery());
        }
        apply(topic, input);
        topic.setUpdatedAt(clock.instant());
        topics.save(topic);
        syncSources();
        return topic;
    }

    /**
     * Deletes a topic and re-syncs the sources; its stored series stay.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_ATTENTION_TOPIC}
     */
    @Transactional
    public void delete(String id) {
        topics.delete(get(id));
        topics.flush();
        syncSources();
    }

    /**
     * Writes the enabled topics into the {@code topics} parameter of {@code gdelt-timelines},
     * {@code gdelt-articles} (prompt 133) and {@code wikimedia-pageviews}, keeping their other stored parameters. A source whose parameter already holds
     * exactly these entries is not written.
     */
    @Transactional
    public void syncSources() {
        List<AttentionTopicEntity> enabled = topics.findByEnabledOrderByIdAsc(true);
        List<String> gdelt = new ArrayList<>();
        List<String> wiki = new ArrayList<>();
        for (AttentionTopicEntity topic : enabled) {
            if (topic.getGdeltQuery() != null) {
                gdelt.add(TopicParams.encode(new TopicParams.GdeltTopic(topic.getId(), topic.getGdeltQuery())));
            }
            List<TopicParams.WikiArticle> articles = new ArrayList<>();
            for (Map<String, String> article : topic.getWikiArticles()) {
                articles.add(new TopicParams.WikiArticle(article.get("project"), article.get("article")));
            }
            if (!articles.isEmpty()) {
                wiki.add(TopicParams.encode(new TopicParams.WikiTopic(topic.getId(), articles)));
            }
        }
        writeTopics(GdeltTimelineAdapter.ID, gdelt);
        writeTopics(ai.foresignal.source.adapters.gdelt.GdeltArticlesAdapter.ID, gdelt);
        writeTopics(WikimediaPageviewsAdapter.ID, wiki);
    }

    private void writeTopics(String sourceId, List<String> entries) {
        Map<String, Object> stored = sources.storedParams(sourceId);
        Object current = stored.get(TopicParams.PARAM);
        List<String> currentEntries = current instanceof List<?> list
                ? list.stream().map(String::valueOf).toList() : List.of();
        if (currentEntries.equals(entries)) {
            return;
        }
        Map<String, Object> params = new LinkedHashMap<>(stored);
        params.put(TopicParams.PARAM, List.copyOf(entries));
        sources.update(sourceId, SourceSettingsUpdate.params(params));
    }

    /** Creates the five example topics, disabled, once per installation, then syncs the sources. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            seedExamples();
            syncSources();
        } catch (RuntimeException e) {
            log.warn("Could not seed or sync the attention topics: {}", e.toString());
        }
    }

    /** Creates the example topics unless they were created before (setting {@code attention.seeded}). */
    @Transactional
    public void seedExamples() {
        if (Boolean.TRUE.equals(settings.get(SettingKeys.ATTENTION_SEEDED, Boolean.class).orElse(false))) {
            return;
        }
        Instant now = clock.instant();
        for (AttentionTopicInput example : AttentionTopicExamples.ALL) {
            if (!topics.existsById(example.id())) {
                AttentionTopicEntity topic = new AttentionTopicEntity(example.id(), now);
                apply(topic, example);
                topics.save(topic);
            }
        }
        settings.put(SettingKeys.ATTENTION_SEEDED, Boolean.TRUE);
    }

    /** The one budgeted GDELT test call of a new or changed query, while {@code gdelt-timelines} is enabled. */
    void testGdeltQuery(String query) {
        String q = blankToNull(query);
        if (q == null || !sources.view(GdeltTimelineAdapter.ID).enabled()) {
            return;
        }
        ProbeResult result;
        try {
            result = probes.probeCall(GdeltTimelineAdapter.ID,
                    (adapter, ctx) -> ((GdeltTimelineAdapter) adapter).testQuery(ctx, q.strip()));
        } catch (BudgetExceededProblem e) {
            log.info("gdelt-timelines budget spent; the topic query was checked by characters only");
            return;
        }
        if (result.parseError() != null) {
            throw invalid("gdeltQuery", "GDELT did not accept the query: " + result.parseError());
        }
    }

    /** Checks an input; {@code pathId} is the path id of an update, or null for a create. */
    void validate(AttentionTopicInput input, String pathId) {
        List<FieldError> errors = new ArrayList<>();
        if (input == null) {
            throw new ValidationException(ErrorCodes.TOPIC_INVALID, "The topic body is required.",
                    List.of(new FieldError("body", "is required")));
        }
        if (input.id() == null || !ID.matcher(input.id()).matches()) {
            errors.add(new FieldError("id", "must match " + ID.pattern()));
        } else if (pathId != null && !pathId.equals(input.id())) {
            errors.add(new FieldError("id", "must equal the path id '" + pathId + "'"));
        }
        if (input.name() == null || input.name().isBlank() || input.name().length() > 200) {
            errors.add(new FieldError("name", "must be 1 to 200 characters"));
        }
        if (input.description() != null && input.description().length() > MAX_DESCRIPTION) {
            errors.add(new FieldError("description", "must be at most " + MAX_DESCRIPTION + " characters"));
        }
        if (blankToNull(input.gdeltQuery()) != null) {
            String problem = TopicParams.queryProblem(input.gdeltQuery());
            if (problem != null) {
                errors.add(new FieldError("gdeltQuery", problem));
            }
        }
        List<AttentionTopicInput.WikiArticle> articles = input.wikiArticles() == null ? List.of()
                : input.wikiArticles();
        if (articles.size() > TopicParams.MAX_ARTICLES) {
            errors.add(new FieldError("wikiArticles", "must hold at most " + TopicParams.MAX_ARTICLES + " articles"));
        }
        for (int i = 0; i < articles.size(); i++) {
            AttentionTopicInput.WikiArticle article = articles.get(i);
            String problem = article == null ? "must not be null"
                    : TopicParams.articleProblem(article.project(), article.article());
            if (problem != null) {
                errors.add(new FieldError("wikiArticles[" + i + "]", problem));
            }
        }
        tags(errors, "keywords", input.keywords(), MAX_KEYWORDS, null);
        tags(errors, "themes", input.themes(), MAX_TAGS, null);
        tags(errors, "regionCodes", input.regionCodes(), MAX_TAGS, REGION_CODE);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.TOPIC_INVALID, "The attention topic is not valid.", errors);
        }
    }

    private static void tags(List<FieldError> errors, String field, List<String> values, int max, Pattern pattern) {
        if (values == null) {
            return;
        }
        if (values.size() > max) {
            errors.add(new FieldError(field, "must hold at most " + max + " entries"));
            return;
        }
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value == null || value.isBlank() || value.length() > MAX_TAG_LENGTH) {
                errors.add(new FieldError(field + "[" + i + "]", "must be 1 to " + MAX_TAG_LENGTH + " characters"));
            } else if (pattern != null && !pattern.matcher(value).matches()) {
                errors.add(new FieldError(field + "[" + i + "]", "must match " + pattern.pattern()));
            }
        }
    }

    private static void apply(AttentionTopicEntity topic, AttentionTopicInput input) {
        topic.setName(input.name().strip());
        topic.setDescription(blankToNull(input.description()));
        String query = blankToNull(input.gdeltQuery());
        topic.setGdeltQuery(query == null ? null : query.strip());
        List<Map<String, String>> articles = new ArrayList<>();
        if (input.wikiArticles() != null) {
            for (AttentionTopicInput.WikiArticle article : input.wikiArticles()) {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("project", article.project());
                entry.put("article", article.article().strip().replace(' ', '_'));
                articles.add(entry);
            }
        }
        topic.setWikiArticles(articles);
        topic.setKeywords(strip(input.keywords()));
        topic.setRegionCodes(strip(input.regionCodes()));
        topic.setThemes(strip(input.themes()));
        topic.setEnabled(Boolean.TRUE.equals(input.enabled()));
    }

    private static List<String> strip(List<String> values) {
        return values == null ? List.of() : values.stream().map(String::strip).toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.TOPIC_INVALID, "The attention topic is not valid.",
                List.of(new FieldError(field, message)));
    }

    /** The view of a topic (OpenAPI {@code AttentionTopic}). */
    public static Map<String, Object> view(AttentionTopicEntity topic) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", topic.getId());
        out.put("name", topic.getName());
        out.put("description", topic.getDescription());
        out.put("gdeltQuery", topic.getGdeltQuery());
        out.put("wikiArticles", topic.getWikiArticles());
        out.put("keywords", topic.getKeywords());
        out.put("regionCodes", topic.getRegionCodes());
        out.put("themes", topic.getThemes());
        out.put("enabled", topic.isEnabled());
        out.put("social", null);
        if (topic.getCreatedBy() != null) {
            out.put("createdBy", topic.getCreatedBy());
        }
        out.put("createdAt", topic.getCreatedAt().toString());
        out.put("updatedAt", topic.getUpdatedAt().toString());
        return out;
    }

    static String encodeCursor(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    static String decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor from this endpoint");
        }
    }
}
