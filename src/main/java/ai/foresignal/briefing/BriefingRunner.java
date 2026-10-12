package ai.foresignal.briefing;

import ai.foresignal.ask.AskCitation;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.SourceVisibilityGate;
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
     */
    public record Plan(String name, BriefingInput input, Instant periodFrom, Instant periodTo,
            Set<String> hiddenSources, ai.foresignal.security.restrict.ReadRestriction restriction) {

        /** A null restriction becomes none. */
        public Plan {
            restriction = restriction == null ? ai.foresignal.security.restrict.ReadRestriction.NONE : restriction;
        }

        /** An unrestricted plan (the shape before prompt 035). */
        public Plan(String name, BriefingInput input, Instant periodFrom, Instant periodTo, Set<String> hiddenSources) {
            this(name, input, periodFrom, periodTo, hiddenSources, null);
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
        return SourceVisibilityGate.excludedSources(registry, Scope.READ, null);
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
        try {
            scope = scopes.resolve(input.scope());
            stats = statsService.compute(new DigestStatsService.Request(null, plan.periodFrom(), plan.periodTo(), null,
                    scope, input.maxItemsPerKind(), plan.hiddenSources(), zone, input.scope(),
                    scope.narrow(plan.restriction())));
        } catch (RuntimeException e) {
            log.warn("Briefing '{}' could not compute its statistics", plan.name(), e);
            return new Outcome(BriefingRunEntity.FAILED, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), "", List.of(), null, List.of(), null,
                    "the digest statistics could not be computed: " + e.getMessage());
        }
        Map<String, Object> statsJson = plain(stats.toMap());
        // Prompt 036: the digest heading names the scope's places ({place}), never their coordinates.
        String heading = scope.placeNames().isEmpty() ? plan.name()
                : plan.name() + " · " + String.join(", ", scope.placeNames());
        DigestRenderer.Rendered digest = renderer.render(heading, stats, zone, plan.periodFrom(), plan.periodTo());
        if (!BriefingInput.LLM.equals(input.mode())) {
            return new Outcome(BriefingRunEntity.SUCCEEDED, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), digest.markdown(), citations(digest.citations()), statsJson, List.of(), null, null);
        }
        try {
            LlmBriefingWriter.Result result = writer.write(new LlmBriefingWriter.Request(plan.name(),
                    input.instructions(), stats, scope, plan.hiddenSources(), zone, plan.periodFrom(), plan.periodTo(),
                    scope.narrow(plan.restriction())));
            return new Outcome(BriefingRunEntity.SUCCEEDED, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), result.markdown(), citations(result.citations()), statsJson, result.warnings(),
                    result.usage(), null);
        } catch (RuntimeException e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            Map<String, Object> usage = e instanceof LlmBriefingWriter.Fallback f ? f.usage() : null;
            if (!(e instanceof LlmBriefingWriter.Fallback)) {
                log.warn("LLM briefing '{}' failed; falling back to the digest", plan.name(), e);
            }
            return new Outcome(BriefingRunEntity.FALLBACK, input.mode(), started, clock.instant(), plan.periodFrom(),
                    plan.periodTo(), digest.markdown(), citations(digest.citations()), statsJson,
                    List.of("LLM mode fell back to the digest: " + reason), usage, reason);
        }
    }

    /**
     * Runs {@code briefing} now: stores the run, embeds it and delivers it.
     *
     * @param briefing a snapshot of the definition
     * @return the completed run
     */
    public BriefingRunEntity run(BriefingEntity briefing) {
        Instant now = clock.instant();
        BriefingInput input = BriefingInput.of(briefing);
        Instant previousTo = runs.findFirstByBriefingIdAndStatusInOrderByStartedAtDescIdDesc(briefing.getId(),
                List.of(BriefingRunEntity.SUCCEEDED, BriefingRunEntity.FALLBACK))
                .map(BriefingRunEntity::getPeriodTo).orElse(null);
        Instant from = periodFrom(input, previousTo, now);
        UUID runId = UUID.randomUUID();
        BriefingRunEntity running = new BriefingRunEntity(runId, briefing.getId(), briefing.getName(), input.mode(),
                input.timezone(), now, from, now);
        transactions.executeWithoutResult(status -> runs.saveAndFlush(running));

        Outcome outcome = render(new Plan(briefing.getName(), input, from, now, hiddenSources(briefing.getOwnerScope()),
                ai.foresignal.security.restrict.ReadRestriction.of(briefing.getRestrictionSnapshot(), false)));
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
            return runs.saveAndFlush(row);
        });
        if (BriefingRunEntity.FAILED.equals(done.getStatus())) {
            return done;
        }
        embed(done);
        int delivered = deliver(briefing, input, done);
        if (delivered > 0) {
            done = transactions.execute(status -> {
                BriefingRunEntity row = runs.findById(runId).orElseThrow();
                row.setDeliveries(delivered);
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
        ZoneId zone = BriefingValidator.zone(run.getTimezone());
        String title = run.getBriefingName() + " — " + DATE.format(run.getPeriodTo().atZone(zone));
        String html = links.runHtml(run.getBriefingId(), run.getId());
        String body = run.getContentMarkdown().strip() + "\n\n[Read more](" + DigestRenderer.url(html) + ")";
        Set<UUID> channelIds = Set.copyOf(input.channelIds());
        Predicate<ai.foresignal.notify.NotificationChannel> filter =
                channelIds.isEmpty() ? c -> true : c -> channelIds.contains(c.id());
        try {
            NotificationMessage message = notifications.publish(NotificationMessage
                    .builder(NotificationMessage.BRIEFING, SUBTYPE, title, "info")
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
}
