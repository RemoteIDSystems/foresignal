package ai.foresignal.briefing;

import ai.foresignal.ask.AskCitation;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.NotificationDeliveryRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Executes briefing runs (prompt 029, Architecture §22): the period, the digest statistics, the
 * markdown (digest mode, or LLM mode with fallback to the digest), then for a stored run the
 * {@code briefing_run} row, the embedding and the {@code briefing} notification.
 *
 * <p>The run row is written {@code running} in its own transaction before any work and completed in
 * another, so a run's state is visible while it runs and survives the scheduler's thread.
 * Notifications are published only after the completed run is committed. A failed run is stored
 * {@code failed} and not delivered.
 *
 * <p>Prompt 141: a briefing with an {@code audio} option has its completed run synthesized
 * ({@link BriefingAudioService}) before delivery, so the notification carries {@code audio.mediaId} and channels
 * can attach it. The outcome is stored on the run ({@code audio}); a synthesis failure is recorded there and the
 * text is delivered as usual.
 *
 * <p>Prompt NP-003: a user-owned edition ({@link EditionContext}, from {@link EditionResolver}) also hides the sources
 * its owner turned off, carries a "Top stories" section of the owner's news, is written in its language (the LLM writer
 * is told to; a digest is translated after rendering, the English kept with a warning when that fails) and is spoken
 * by a voice of that language ({@link EditionVoice}).
 *
 * <p>Prompt NP-004: a briefing with {@code deliveryTargets} is delivered by {@link EditionDelivery}, each target in its
 * own shape, and the outcome of each is stored on the run ({@code deliveryResults}); without targets it delivers
 * exactly as before.
 *
 * <p>Prompt NP-006: {@link #run(BriefingEntity, BreakingTrigger)} runs a breaking-news edition off schedule. The run
 * stores its alert ({@code breaking}); its heading and title say "Breaking", its markdown leads with the alert, the LLM
 * is told to lead with it, and its notifications carry the alert's severity. Its period is since the previous run of
 * any kind; a scheduled run's period ignores breaking runs.
 */
@Component
public class BriefingRunner {

    private static final Logger log = LoggerFactory.getLogger(BriefingRunner.class);

    /** The default lookback without one and without a previous run. */
    public static final Duration DEFAULT_LOOKBACK = Duration.ofHours(24);

    /** {@code subtype} of briefing notifications. */
    public static final String SUBTYPE = "briefing.run";

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);

    private final DigestStatsService statsService;
    private final ScopeResolver scopes;
    private final DigestRenderer renderer;
    private final LlmBriefingWriter writer;
    private final BriefingRunRepository runs;
    private final NotificationService notifications;
    private final NotificationDeliveryRepository deliveries;
    private final ObjectProvider<EmbeddingWorker> embeddings;
    private final SearchStatusService searchStatus;
    private final BriefingLinks links;
    private final SourceRegistry registry;
    private volatile SourceVisibility visibility;
    private final JsonMapper json;
    private final TransactionTemplate transactions;
    private final Clock clock;

    /** Creates the runner. */
    public BriefingRunner(DigestStatsService statsService, ScopeResolver scopes, DigestRenderer renderer,
            LlmBriefingWriter writer, BriefingRunRepository runs, NotificationService notifications,
            NotificationDeliveryRepository deliveries, ObjectProvider<EmbeddingWorker> embeddings,
            SearchStatusService searchStatus, BriefingLinks links, SourceRegistry registry, JsonMapper json,
            PlatformTransactionManager transactionManager, Clock clock) {
        this.statsService = statsService;
        this.scopes = scopes;
        this.renderer = renderer;
        this.writer = writer;
        this.runs = runs;
        this.notifications = notifications;
        this.deliveries = deliveries;
        this.embeddings = embeddings;
        this.searchStatus = searchStatus;
        this.links = links;
        this.registry = registry;
        this.json = json;
        this.transactions = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    /** Synthesizes audio briefings (prompt 141); resolved lazily, absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ObjectProvider<BriefingAudioService> audioService;

    /**
     * The run's audio (prompt 141) stored on it, when the briefing has the option; a failure of any kind is
     * recorded as {@code audio.status=failed} and never stops the run or its delivery.
     */
    BriefingRunEntity audio(BriefingInput input, BriefingRunEntity run) {
        return audio(input, run, null);
    }

    /**
     * {@link #audio(BriefingInput, BriefingRunEntity)} spoken in {@code language} (prompt NP-003): a voice of that
     * language is picked ({@link EditionVoice#match}).
     */
    BriefingRunEntity audio(BriefingInput input, BriefingRunEntity run, String language) {
        if (input.audio() == null) {
            return run;
        }
        BriefingInput.Audio spoken = EditionVoice.match(input.audio(), language);
        BriefingAudioService service = audioService == null ? null : audioService.getIfAvailable();
        Map<String, Object> outcome;
        if (service == null) {
            outcome = BriefingAudioService.result(BriefingAudioService.SKIPPED, spoken);
            outcome.put("error", "audio briefings are not available");
        } else {
            try {
                outcome = service.synthesize(title(run), spokenText(input, run), spoken);
            } catch (RuntimeException e) {
                log.warn("Audio of briefing run {} failed", run.getId(), e);
                outcome = BriefingAudioService.result(BriefingAudioService.FAILED, spoken);
                outcome.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        Map<String, Object> stored = outcome;
        try {
            return transactions.execute(status -> {
                BriefingRunEntity row = runs.findById(run.getId()).orElse(run);
                row.setAudio(new java.util.LinkedHashMap<>(stored));
                return runs.saveAndFlush(row);
            });
        } catch (RuntimeException e) {
            log.warn("The audio outcome of briefing run {} could not be stored", run.getId(), e);
            run.setAudio(new java.util.LinkedHashMap<>(stored));
            return run;
        }
    }

    /**
     * The text of a run's audio: its markdown, followed, when the briefing has {@code includeCorroboration} (NP-007)
     * and some cited item rests on a single source, by a one-sentence note on sourcing
     * ({@link EditionProvenance#spokenNote}).
     */
    static String spokenText(BriefingInput input, BriefingRunEntity run) {
        String markdown = run.getContentMarkdown();
        if (!input.edition().includeCorroboration()) {
            return markdown;
        }
        String note = EditionProvenance.spokenNote(run.getCitations());
        return note == null ? markdown : (markdown == null ? "" : markdown.strip()) + "\n\n" + note;
    }

    /** The notification title of a run, which is also the spoken title of its audio. */
    static String title(BriefingRunEntity run) {
        ZoneId zone = BriefingValidator.zone(run.getTimezone());
        return (run.isBreaking() ? "Breaking: " : "") + run.getBriefingName() + " — "
                + DATE.format(run.getPeriodTo().atZone(zone));
    }

    /** Signs stored runs (prompt 109); resolved lazily, absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ObjectProvider<ai.foresignal.provenance.JwsSigner> signer;

    /**
     * The detached JWS over a run's stored markdown bytes (prompt 109, task 6: {@code BriefingRun.signature}),
     * {@code typ} {@code text/markdown}; null when there is no markdown or no signer, or signing fails (a
     * signing failure never fails a run).
     */
    String signature(String markdown) {
        ai.foresignal.provenance.JwsSigner jws = signer == null ? null : signer.getIfAvailable();
        if (markdown == null || jws == null) {
            return null;
        }
        try {
            return jws.signDetached(markdown.getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/markdown");
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * What one run renders.
     *
     * @param name the briefing name
     * @param input the definition
     * @param periodFrom the period start
     * @param periodTo the period end
     * @param hiddenSources sources counted but never listed (private sources unless the owner is admin)
     * @param restriction the read restriction of the token that created the briefing (prompt 035):
     *     statistics, notable records, search hits and therefore citations stay inside it
     * @param edition the edition context (prompt NP-003), or null for a plain briefing
     * @param breaking the alert of a breaking-news run (prompt NP-006), or null
     */
    public record Plan(String name, BriefingInput input, Instant periodFrom, Instant periodTo,
            Set<String> hiddenSources, ai.foresignal.security.restrict.ReadRestriction restriction,
            EditionContext edition, BreakingTrigger breaking) {

        /** A null restriction becomes none. */
        public Plan {
            restriction = restriction == null ? ai.foresignal.security.restrict.ReadRestriction.NONE : restriction;
        }

        /** A plan of a plain briefing (the shape before prompt NP-003). */
        public Plan(String name, BriefingInput input, Instant periodFrom, Instant periodTo, Set<String> hiddenSources,
                ai.foresignal.security.restrict.ReadRestriction restriction) {
            this(name, input, periodFrom, periodTo, hiddenSources, restriction, null, null);
        }

        /** A scheduled plan (the shape before prompt NP-006). */
        public Plan(String name, BriefingInput input, Instant periodFrom, Instant periodTo, Set<String> hiddenSources,
                ai.foresignal.security.restrict.ReadRestriction restriction, EditionContext edition) {
            this(name, input, periodFrom, periodTo, hiddenSources, restriction, edition, null);
        }

        /** The heading name: {@code Breaking: <name>} for a breaking run (NP-006). */
        public String heading() {
            return breaking == null ? name : "Breaking: " + name;
        }

        /** The LLM instructions, told to lead with the alert of a breaking run (NP-006). */
        public String instructions() {
            if (breaking == null) {
                return input.instructions();
            }
            String lead = "This is a breaking-news edition fired by an alert of rule '" + breaking.ruleName()
                    + "' (alert " + breaking.alertId() + ", severity " + breaking.severity()
                    + "). Lead with that alert and what it means, then the rest.";
            return input.instructions() == null || input.instructions().isBlank() ? lead
                    : lead + "\n\n" + input.instructions();
        }

        /** The edition's language, or null. */
        public String language() {
            return edition == null ? null : edition.language();
        }

        /** The hidden sources with the edition owner's own turned-off sources. */
        public Set<String> allHidden() {
            if (edition == null || edition.ownerHiddenSources().isEmpty()) {
                return hiddenSources;
            }
            Set<String> all = new java.util.TreeSet<>(hiddenSources);
            all.addAll(edition.ownerHiddenSources());
            return all;
        }

        /** An unrestricted plan (the shape before prompt 035). */
        public Plan(String name, BriefingInput input, Instant periodFrom, Instant periodTo, Set<String> hiddenSources) {
            this(name, input, periodFrom, periodTo, hiddenSources, null, null, null);
        }
    }

    /**
     * A rendered run.
     *
     * @param status {@code succeeded}, {@code fallback} or {@code failed}
     * @param mode {@code digest} or {@code llm}
     * @param startedAt when rendering started
     * @param finishedAt when it finished
     * @param periodFrom the period start
     * @param periodTo the period end
     * @param markdown the content
     * @param citations {@code AskCitation} objects
     * @param stats the {@code DigestStats} JSON, or null when it could not be computed
     * @param warnings verifier warnings and fallback notes
     * @param usage LLM usage, or null
     * @param error the error, or null
     */
    public record Outcome(String status, String mode, Instant startedAt, Instant finishedAt, Instant periodFrom,
            Instant periodTo, String markdown, List<Map<String, Object>> citations, Map<String, Object> stats,
            List<String> warnings, Map<String, Object> usage, String error) {}

    /** The private sources a briefing owned by {@code ownerScope} counts but does not list: none for admin. */
    public Set<String> hiddenSources(String ownerScope) {
        Scope scope = ownerScope == null ? null : parseScope(ownerScope);
        if (scope == Scope.ADMIN) {
            return Set.of();
        }
        return visibility().privateSourceIds();
    }

    /**
     * The period of a run starting at {@code now}: {@code lookback} back, else since the previous
     * completed run (capped at 7 d), else {@value #DEFAULT_LOOKBACK_TEXT}.
     *
     * @param input the definition
     * @param previousPeriodTo the previous completed run's period end, or null
     */
    public static Instant periodFrom(BriefingInput input, Instant previousPeriodTo, Instant now) {
        if (input.lookbackDuration() != null) {
            return now.minus(input.lookbackDuration());
        }
        if (previousPeriodTo != null && previousPeriodTo.isBefore(now)) {
            Instant cap = now.minus(BriefingValidator.MAX_LOOKBACK);
            return previousPeriodTo.isBefore(cap) ? cap : previousPeriodTo;
        }
        return now.minus(DEFAULT_LOOKBACK);
    }

    /** The default lookback, as text for documentation. */
    static final String DEFAULT_LOOKBACK_TEXT = "24 h";

    /** Renders {@code plan} without storing or delivering anything (previews, and the body of a run). */
    public Outcome render(Plan plan) {
        Instant started = clock.instant();
        BriefingInput input = plan.input();
        ZoneId zone = BriefingValidator.zone(input.timezone());
        DigestStats stats;
        ScopeResolver.Resolved scope;
        Set<String> hidden = plan.allHidden();
        try {
            scope = scopes.resolve(input.scope());
            stats = statsService.compute(new DigestStatsService.Request(null, plan.periodFrom(), plan.periodTo(), null,
                    scope, input.maxItemsPerKind(), hidden, zone, input.scope(),
                    scope.narrow(plan.restriction())));
            stats = withTopStories(stats, plan, hidden);
        } catch (RuntimeException e) {
            log.warn("Briefing '{}' could not compute its statistics", plan.name(), e);
            return new Outcome(BriefingRunEntity.FAILED, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), "", List.of(), null, List.of(), null,
                    "the digest statistics could not be computed: " + e.getMessage());
        }
        Map<String, Object> statsJson = plain(stats.toMap());
        // Prompt 036: the digest heading names the scope's places ({place}), never their coordinates.
        String heading = scope.placeNames().isEmpty() ? plan.heading()
                : plan.heading() + " · " + String.join(", ", scope.placeNames());
        DigestRenderer.Rendered digest = renderer.render(heading, stats, zone, plan.periodFrom(), plan.periodTo());
        if (!BriefingInput.LLM.equals(input.mode())) {
            List<String> warnings = new ArrayList<>();
            String markdown = inLanguage(withLead(digest.markdown(), plan.breaking()), plan.language(), warnings);
            return new Outcome(BriefingRunEntity.SUCCEEDED, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), markdown, citations(digest.citations(), input), statsJson, warnings, null, null);
        }
        try {
            LlmBriefingWriter.Result result = writer.write(new LlmBriefingWriter.Request(plan.heading(),
                    plan.instructions(), stats, scope, hidden, zone, plan.periodFrom(), plan.periodTo(),
                    scope.narrow(plan.restriction()), plan.language()));
            return new Outcome(BriefingRunEntity.SUCCEEDED, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), result.markdown(), citations(result.citations(), input), statsJson, result.warnings(),
                    result.usage(), null);
        } catch (RuntimeException e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            Map<String, Object> usage = e instanceof LlmBriefingWriter.Fallback f ? f.usage() : null;
            if (!(e instanceof LlmBriefingWriter.Fallback)) {
                log.warn("LLM briefing '{}' failed; falling back to the digest", plan.name(), e);
            }
            List<String> warnings = new ArrayList<>(List.of("LLM mode fell back to the digest: " + reason));
            String markdown = inLanguage(withLead(digest.markdown(), plan.breaking()), plan.language(), warnings);
            return new Outcome(BriefingRunEntity.FALLBACK, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), markdown, citations(digest.citations(), input), statsJson, warnings, usage, reason);
        }
    }

    /**
     * {@code markdown} with the lead line of a breaking run (NP-006) after its heading line, or unchanged for any other
     * run.
     */
    static String withLead(String markdown, BreakingTrigger breaking) {
        if (breaking == null) {
            return markdown;
        }
        String text = markdown == null ? "" : markdown;
        if (text.startsWith("#")) {
            int end = text.indexOf('\n');
            String first = end < 0 ? text : text.substring(0, end);
            String rest = end < 0 ? "" : text.substring(end + 1).stripLeading();
            return first + "\n\n" + breaking.lead() + "\n\n" + rest;
        }
        return breaking.lead() + "\n\n" + text;
    }

    /** The notification severity of a run: the alert's for a breaking run (NP-006), else {@code info}. */
    static String severity(BriefingRunEntity run) {
        Object s = run.getBreaking() == null ? null : run.getBreaking().get("severity");
        return s instanceof String v && ai.foresignal.trigger.Severity.WIRE_VALUES.contains(v) ? v : "info";
    }

    /** Delivers to an edition's targets (prompt NP-004); resolved lazily, absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ObjectProvider<EditionDelivery> editionDelivery;

    /** Translates editions (prompt NP-003); resolved lazily, absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ObjectProvider<ai.foresignal.notify.NotificationTranslator> translator;

    /** Resolves editions (prompt NP-003); resolved lazily, absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ObjectProvider<EditionResolver> editions;

    /** {@code stats} with the edition's "Top stories" section appended, when it is an edition with news. */
    DigestStats withTopStories(DigestStats stats, Plan plan, Set<String> hidden) {
        EditionResolver resolver = editions == null ? null : editions.getIfAvailable();
        if (resolver == null || plan.edition() == null) {
            return stats;
        }
        try {
            return resolver.topStories(plan.edition(), plan.periodFrom(), plan.periodTo(), hidden).map(section -> {
                List<BriefingSection> sections = new ArrayList<>(stats.sections());
                sections.add(section);
                return stats.withSections(sections);
            }).orElse(stats);
        } catch (RuntimeException e) {
            log.warn("The top stories of edition '{}' could not be read", plan.name(), e);
            return stats;
        }
    }

    /**
     * The digest {@code markdown} in the edition's {@code language} (prompt NP-003): unchanged without a language or
     * for English; the English kept, with a warning, when the translation is unavailable or fails.
     */
    String inLanguage(String markdown, String language, List<String> warnings) {
        if (language == null || ai.foresignal.notify.NotificationTranslator.sameLanguage("en", language)) {
            return markdown;
        }
        ai.foresignal.notify.NotificationTranslator t = translator == null ? null : translator.getIfAvailable();
        String out = t == null ? null : t.translateMarkdown(markdown, language);
        if (out == null) {
            warnings.add("the edition could not be translated into " + language + "; it is in English");
            return markdown;
        }
        return out;
    }

    /** The edition context of {@code briefing}, or null for a plain briefing or without a resolver. */
    public EditionContext edition(BriefingEntity briefing) {
        EditionResolver resolver = editions == null ? null : editions.getIfAvailable();
        if (resolver == null) {
            return null;
        }
        try {
            return resolver.resolve(briefing);
        } catch (RuntimeException e) {
            log.warn("The edition context of briefing '{}' could not be resolved", briefing.getName(), e);
            return null;
        }
    }

    /**
     * Runs {@code briefing} now: stores the run, embeds it and delivers it.
     *
     * @param briefing a snapshot of the definition
     * @return the completed run
     */
    public BriefingRunEntity run(BriefingEntity briefing) {
        return run(briefing, null);
    }

    /**
     * Runs {@code briefing} now as a breaking-news edition fired by {@code breaking} (NP-006), or as a scheduled or
     * manual run when it is null.
     *
     * @param briefing a snapshot of the definition
     * @param breaking the alert, or null
     * @return the completed run
     */
    public BriefingRunEntity run(BriefingEntity briefing, BreakingTrigger breaking) {
        Instant now = clock.instant();
        BriefingInput input = BriefingInput.of(briefing);
        List<String> completed = List.of(BriefingRunEntity.SUCCEEDED, BriefingRunEntity.FALLBACK);
        Instant previousTo = (breaking == null
                ? runs.findFirstByBriefingIdAndStatusInAndBreakingIsNullOrderByStartedAtDescIdDesc(briefing.getId(),
                        completed)
                : runs.findFirstByBriefingIdAndStatusInOrderByStartedAtDescIdDesc(briefing.getId(), completed))
                .map(BriefingRunEntity::getPeriodTo).orElse(null);
        Instant from = periodFrom(input, previousTo, now);
        UUID runId = UUID.randomUUID();
        BriefingRunEntity running = new BriefingRunEntity(runId, briefing.getId(), briefing.getName(), input.mode(),
                input.timezone(), now, from, now);
        if (breaking != null) {
            running.setBreaking(new java.util.LinkedHashMap<>(breaking.toMap()));
        }
        transactions.executeWithoutResult(status -> runs.saveAndFlush(running));

        EditionContext edition = edition(briefing);
        Outcome outcome = render(new Plan(briefing.getName(), input, from, now, hiddenSources(briefing.getOwnerScope()),
                ai.foresignal.security.restrict.ReadRestriction.of(briefing.getRestrictionSnapshot(), false), edition,
                breaking));
        BriefingRunEntity done = transactions.execute(status -> {
            BriefingRunEntity row = runs.findById(runId).orElse(running);
            row.setStatus(outcome.status());
            row.setFinishedAt(outcome.finishedAt());
            row.setContentMarkdown(outcome.markdown());
            row.setCitations(new ArrayList<>(outcome.citations()));
            row.setStats(outcome.stats());
            row.setWarnings(new ArrayList<>(outcome.warnings()));
            row.setUsage(outcome.usage());
            row.setError(outcome.error());
            row.setSignature(signature(outcome.markdown()));
            return runs.saveAndFlush(row);
        });
        if (BriefingRunEntity.FAILED.equals(done.getStatus())) {
            return done;
        }
        done = audio(input, done, edition == null ? null : edition.language());
        if (edition == null || !edition.owned()) {
            // NP-003: the search index is shared by every user, so an edition's runs stay out of it.
            embed(done);
        }
        EditionDelivery targets = editionDelivery == null ? null : editionDelivery.getIfAvailable();
        List<Map<String, Object>> results;
        int delivered;
        if (!input.edition().deliveryTargets().isEmpty() && targets != null) {
            // NP-004: an edition with delivery targets goes to each of them in its own shape, instead of channelIds.
            EditionDelivery.Outcome sent = targets.deliver(title(done), input, done, edition,
                    plain(BriefingViews.run(done)));
            results = sent.results();
            delivered = sent.deliveries();
        } else {
            results = null;
            delivered = deliver(briefing, input, done, edition);
        }
        if (delivered > 0 || results != null) {
            done = transactions.execute(status -> {
                BriefingRunEntity row = runs.findById(runId).orElseThrow();
                row.setDeliveries(delivered);
                row.setDeliveryResults(results == null ? null : new ArrayList<>(results));
                return runs.saveAndFlush(row);
            });
        }
        return done;
    }

    /** Embeds a completed run as a {@code briefing} document when an embedding index is available. */
    void embed(BriefingRunEntity run) {
        EmbeddingWorker worker = embeddings.getIfAvailable();
        if (worker == null) {
            return;
        }
        SearchStatusService.Availability availability = searchStatus.availability();
        if (!availability.available() && (availability.state() == null || availability.state().pending() == null)) {
            return;
        }
        try {
            worker.embedDocument(DocInput.BRIEFING, run.getId().toString(),
                    run.getBriefingName() + "\n\n" + run.getContentMarkdown(), null, run.getPeriodTo(), null, null);
        } catch (RuntimeException e) {
            log.warn("Briefing run {} could not be embedded: {}", run.getId(), e.getMessage());
        }
    }

    /**
     * Publishes the {@code briefing} notification of a completed run to the briefing's channels (every
     * channel subscribed to {@code briefing} when it lists none).
     *
     * @return the deliveries enqueued
     */
    int deliver(BriefingEntity briefing, BriefingInput input, BriefingRunEntity run) {
        return deliver(briefing, input, run, null);
    }

    /**
     * {@link #deliver(BriefingEntity, BriefingInput, BriefingRunEntity)} of an edition (prompt NP-003): an owned
     * edition that lists no channels goes only to its owner's personal channels.
     */
    int deliver(BriefingEntity briefing, BriefingInput input, BriefingRunEntity run, EditionContext edition) {
        String title = title(run);
        String html = links.runHtml(run.getBriefingId(), run.getId());
        String body = run.getContentMarkdown().strip() + "\n\n[Read more](" + DigestRenderer.url(html) + ")";
        Set<UUID> channelIds = Set.copyOf(input.channelIds());
        if (channelIds.isEmpty() && edition != null && edition.owned()) {
            EditionResolver resolver = editions == null ? null : editions.getIfAvailable();
            channelIds = resolver == null ? Set.of() : resolver.ownerChannels(edition.ownerUserId());
            if (channelIds.isEmpty()) {
                return 0;
            }
        }
        Set<UUID> targets = channelIds;
        Predicate<ai.foresignal.notify.NotificationChannel> filter =
                targets.isEmpty() ? c -> true : c -> targets.contains(c.id());
        try {
            NotificationMessage message = notifications.publish(NotificationMessage
                    .builder(NotificationMessage.BRIEFING, SUBTYPE, title, severity(run))
                    .shortTitle(run.getBriefingName())
                    .body(body)
                    .url(html)
                    .links(List.of(new NotificationMessage.Link("Read more", html)))
                    .data(plain(BriefingViews.run(run)))
                    .tags(List.of("briefing"))
                    .briefingRunId(run.getId())
                    .build(), filter);
            return message.id() == null ? 0 : deliveries.findByNotificationIdOrderByIdAsc(message.id()).size();
        } catch (RuntimeException e) {
            log.error("Briefing run {} of '{}' could not be published", run.getId(), briefing.getName(), e);
            return 0;
        }
    }

    private static List<Map<String, Object>> citations(List<AskCitation> citations) {
        return citations.stream().map(AskCitation::toMap).toList();
    }

    /** Annotates citations with provenance and corroboration (prompt NP-007); resolved lazily. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ObjectProvider<EditionProvenance> provenance;

    /**
     * {@code citations} as {@code AskCitation} maps, with {@code provenance} and {@code corroboration} when the
     * briefing's {@code includeProvenance} and {@code includeCorroboration} ask for them (NP-007).
     */
    List<Map<String, Object>> citations(List<AskCitation> citations, BriefingInput input) {
        List<Map<String, Object>> maps = citations(citations);
        BriefingInput.Edition edition = input.edition();
        EditionProvenance annotator = provenance == null ? null : provenance.getIfAvailable();
        if (annotator == null || (!edition.includeProvenance() && !edition.includeCorroboration())) {
            return maps;
        }
        return annotator.annotate(maps, edition.includeProvenance(), edition.includeCorroboration());
    }

    /** {@code value} as plain JSON maps, lists, strings, numbers and booleans. */
    @SuppressWarnings("unchecked")
    Map<String, Object> plain(Map<String, Object> value) {
        return json.readValue(json.writeValueAsString(value), Map.class);
    }

    private static Scope parseScope(String value) {
        try {
            return Scope.fromWireValue(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Injects the shared private-source visibility (prompt 039). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setVisibility(SourceVisibility visibility) {
        this.visibility = visibility;
    }

    private SourceVisibility visibility() {
        SourceVisibility v = visibility;
        if (v == null) {
            v = new SourceVisibility(registry, null, null);
            visibility = v;
        }
        return v;
    }
}
