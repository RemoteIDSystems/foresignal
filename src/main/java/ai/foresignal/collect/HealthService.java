package ai.foresignal.collect;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceHealthLookup;
import ai.foresignal.source.SourceHealthSummary;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.SourceHealthRepository;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Assembles each source's health view from its health row, usage, budget and stream state
 * (FR-10, OpenAPI {@code SourceHealth}).
 *
 * <p>Status is derived on read by {@link HealthEvaluator}, never stored, so it can never disagree
 * with the facts it is derived from.
 *
 * <p>Also implements {@link SourceHealthLookup}, which is how source summaries and details carry
 * {@code status}, {@code lastSuccessAt}, {@code lastDataAt} and {@code budgetUsedToday}, and exposes
 * the Micrometer gauges {@code foresignal.source.status{source,status}} (1 for the current status, 0
 * for the others) and {@code foresignal.source.consecutive_failures{source}}.
 *
 * <p>When an evaluation yields a status different from the previous one in this process, a
 * {@link SourceHealthChangedEvent} is published (prompt 018); the first evaluation of a source after
 * start only records its status.
 */
@Service
public class HealthService implements SourceHealthLookup {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);

    /** Gauge name for the per-status indicator. */
    public static final String STATUS_GAUGE = "foresignal.source.status";

    /** Gauge name for consecutive failures. */
    public static final String FAILURES_GAUGE = "foresignal.source.consecutive_failures";

    /** How often the gauges are re-evaluated between reads. */
    static final long GAUGE_REFRESH_MS = 30_000L;

    /**
     * Retention assumed for a TRACK source's storage projection.
     *
     * <p>TRACK retention is the tier's {@code trackWindow} (Conventions, analytic retention: 3 d);
     * prompt 019 makes it a setting, and this constant gives way to it then.
     */
    static final Duration TRACK_WINDOW = Duration.ofDays(3);

    private final SourceRegistry registry;
    private final SourceService sources;
    private final SourceSettingsRepository settings;
    private final SourceHealthRepository healthRows;
    private final HealthTracker tracker;
    private final BackoffRegistry backoff;
    private final UsageMeter usage;
    private final BudgetGovernor budgets;
    private final AppSettingService appSettings;
    private final HealthEvaluator evaluator;
    private final ObjectProvider<HealthDetailsContributor> detailsContributors;
    private final MeterRegistry meters;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** The last evaluated status and failure count per source, read by the gauges. */
    private final Map<String, GaugeState> gaugeStates = new ConcurrentHashMap<>();

    /** Sources whose gauges are registered. */
    private final Set<String> gauged = ConcurrentHashMap.newKeySet();

    private record GaugeState(HealthStatus status, int consecutiveFailures) {}

    /**
     * Creates the service; contributed {@link HealthRule} beans are applied in their bean order, and the
     * {@link HealthDetailsContributor} beans fill {@code details} (063).
     */
    public HealthService(
            SourceRegistry registry,
            SourceService sources,
            SourceSettingsRepository settings,
            SourceHealthRepository healthRows,
            HealthTracker tracker,
            BackoffRegistry backoff,
            UsageMeter usage,
            BudgetGovernor budgets,
            AppSettingService appSettings,
            ObjectProvider<HealthRule> rules,
            ObjectProvider<HealthDetailsContributor> detailsContributors,
            MeterRegistry meters,
            ApplicationEventPublisher events,
            Clock clock) {
        this.registry = registry;
        this.sources = sources;
        this.settings = settings;
        this.healthRows = healthRows;
        this.tracker = tracker;
        this.backoff = backoff;
        this.usage = usage;
        this.budgets = budgets;
        this.appSettings = appSettings;
        this.evaluator = new HealthEvaluator(rules.orderedStream().toList());
        this.detailsContributors = detailsContributors;
        this.meters = meters;
        this.events = events;
        this.clock = clock;
    }

    /** The money ledgers of paid sources (prompt 135); null in a hand-built service. */
    @org.springframework.beans.factory.annotation.Autowired
    private ObjectProvider<SourceSpendLookup> spendLookups;

    // ---------- views ----------

    /**
     * One source's full health.
     *
     * @throws ai.foresignal.api.error.NotFoundException when the source is not registered
     */
    public SourceHealthView health(String sourceId) {
        return view(registry.descriptor(sourceId));
    }

    /**
     * Every registered source's health, ordered as the registry lists them.
     *
     * @param filter when present, only sources whose status is in the set
     */
    public List<SourceHealthView> all(Optional<Set<HealthStatus>> filter) {
        Optional<Set<HealthStatus>> f = filter == null ? Optional.empty() : filter;
        return registry.descriptors().stream()
                .map(this::view)
                .filter(v -> f.isEmpty() || f.get().contains(v.status()))
                .toList();
    }

    @Override
    public SourceHealthSummary summary(SourceDescriptor descriptor, boolean enabled) {
        String id = descriptor.id();
        Instant now = clock.instant();
        HealthSnapshot snapshot = snapshot(id);
        UsageTotalsWindow lastHour = usage.lastHour(id);
        HealthAssessment assessment = evaluate(descriptor, enabled, snapshot, lastHour, null, now);
        return new SourceHealthSummary(
                assessment.status(),
                assessment.statusReason(),
                snapshot.lastSuccessAt(),
                snapshot.lastDataAt(),
                usage.requestsToday(id));
    }

    private SourceHealthView view(SourceDescriptor descriptor) {
        String id = descriptor.id();
        Instant now = clock.instant();
        boolean enabled = settings.findById(id).map(SourceSettingsEntity::isEnabled).orElse(false);
        HealthSnapshot snapshot = snapshot(id);
        UsageTotalsWindow lastHour = usage.lastHour(id);

        Budget budget = sources.effectiveBudget(id);
        BudgetStatus budgetStatus = hasRequestBudget(budget) ? budgetStatus(id) : null;

        HealthAssessment assessment = evaluate(descriptor, enabled, snapshot, lastHour, budgetStatus, now);

        Instant windowStart = now.minus(StorageMath.BYTES_WINDOW);
        Instant windowEnd = now.plusSeconds(1);
        UsageTotalsWindow week = usage.totals(id, windowStart, windowEnd);
        Duration span =
                usage.earliestMinute(id, windowStart, windowEnd)
                        .map(first -> Duration.between(first, now))
                        .orElse(Duration.ZERO);
        SourceStorage storage = StorageMath.compute(lastHour, week, span, retentionFor(descriptor));

        Instant newestFrameAt = assessment.newestFrameAt();
        Long frameAgeSeconds =
                newestFrameAt == null ? null : Math.max(0, Duration.between(newestFrameAt, now).toSeconds());

        return new SourceHealthView(
                id,
                assessment.status(),
                assessment.statusReason(),
                snapshot.lastAttemptAt(),
                snapshot.lastSuccessAt(),
                snapshot.lastErrorAt(),
                snapshot.lastError(),
                snapshot.lastHttpStatus(),
                snapshot.consecutiveFailures(),
                snapshot.lastRecordsSeen() == null ? 0 : snapshot.lastRecordsSeen(),
                snapshot.lastRecordsChanged() == null ? 0 : snapshot.lastRecordsChanged(),
                HealthEvaluator.errorRate(lastHour),
                backoff.backoffUntil(id),
                hasRequestBudget(budget) ? budget : null,
                budgetStatus == null ? usage.requestsToday(id) : budgetStatus.usedToday(),
                budgetStatus == null ? null : budgetStatus.remainingToday(),
                lastHour.refused(),
                lastHour.requests(),
                lastHour.bytes(),
                snapshot.lastCoverage(),
                storage,
                snapshot.lastDataAt(),
                HealthEvaluator.budgetHoursRemaining(budgetStatus, lastHour),
                newestFrameAt,
                frameAgeSeconds,
                details(descriptor),
                spent(id));
    }

    /** The money a paid source spent this month (135), from the {@link SourceSpendLookup} beans; null when none. */
    Double spent(String sourceId) {
        if (spendLookups == null) {
            return null;
        }
        Double out = null;
        for (SourceSpendLookup lookup : spendLookups.orderedStream().toList()) {
            try {
                Double value = lookup.spentUsdThisMonth(sourceId);
                if (value != null) {
                    out = value;
                }
            } catch (RuntimeException e) {
                log.debug("A spend lookup failed for {}", sourceId, e);
            }
        }
        return out;
    }

    /** The merged {@code details} of every contributor, or null when none has anything (063). */
    Map<String, Object> details(SourceDescriptor descriptor) {
        Map<String, Object> merged = new java.util.LinkedHashMap<>();
        detailsContributors.orderedStream().forEach(contributor -> {
            try {
                Map<String, Object> entries = contributor.details(descriptor);
                if (entries != null) {
                    entries.forEach((key, value) -> {
                        if (key != null && value != null) {
                            merged.put(key, value);
                        }
                    });
                }
            } catch (RuntimeException e) {
                log.warn("A health details contributor failed for {}", descriptor.id(), e);
            }
        });
        return merged.isEmpty() ? null : java.util.Collections.unmodifiableMap(merged);
    }

    private HealthAssessment evaluate(
            SourceDescriptor descriptor,
            boolean enabled,
            HealthSnapshot snapshot,
            UsageTotalsWindow lastHour,
            BudgetStatus budgetStatus,
            Instant now) {
        String id = descriptor.id();
        StreamState stream = descriptor.mode() == Mode.STREAMING ? tracker.streamState(id) : null;
        HealthAssessment assessment =
                evaluator.evaluate(
                        new HealthInputs(
                                descriptor,
                                enabled,
                                snapshot,
                                backoff.backoffUntil(id),
                                lastHour,
                                budgetStatus,
                                stream,
                                now));
        remember(id, assessment.status(), assessment.statusReason(), snapshot.consecutiveFailures(), now);
        return assessment;
    }

    private HealthSnapshot snapshot(String id) {
        return healthRows.findById(id).map(HealthSnapshot::of).orElseGet(() -> HealthSnapshot.empty(id));
    }

    private BudgetStatus budgetStatus(String id) {
        try {
            return budgets.status(id);
        } catch (RuntimeException e) {
            log.warn("Could not read the budget status of {}", id, e);
            return null;
        }
    }

    /**
     * True when the budget can back a Bucket4j bucket; PUSH and DERIVED sources may have none, and a
     * zero capacity is not a bucket.
     */
    private static boolean hasRequestBudget(Budget budget) {
        return budget != null && budget.perMinute() >= 1 && budget.perDay() >= 1;
    }

    /**
     * The retention window the storage projection multiplies by: the source's own window, the
     * TRACK window for a TRACK source, or the global default.
     */
    private Duration retentionFor(SourceDescriptor descriptor) {
        if (descriptor.isTrack()) {
            return TRACK_WINDOW;
        }
        Optional<Duration> own = sources.effectiveRetention(descriptor.id());
        if (own.isPresent()) {
            return own.get();
        }
        try {
            return appSettings
                    .getOrDefault(SettingKeys.RETENTION, RetentionSettings.class, RetentionSettings::defaults)
                    .forSource(descriptor.id());
        } catch (RuntimeException e) {
            log.debug("Could not read retention settings; using the shipped default", e);
            return RetentionSettings.DEFAULT_RETENTION;
        }
    }

    // ---------- metrics ----------

    /** Registers gauges for every registered source once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public void registerGauges() {
        refreshGauges();
    }

    /**
     * Re-evaluates every source so the gauges track status between API reads.
     *
     * <p>Gauges read a cached state rather than evaluating on scrape: a scrape would otherwise cost
     * several queries per source per status.
     */
    @Scheduled(fixedDelay = GAUGE_REFRESH_MS, initialDelay = GAUGE_REFRESH_MS)
    public void refreshGauges() {
        for (SourceDescriptor descriptor : registry.descriptors()) {
            try {
                boolean enabled =
                        settings.findById(descriptor.id()).map(SourceSettingsEntity::isEnabled).orElse(false);
                evaluate(
                        descriptor,
                        enabled,
                        snapshot(descriptor.id()),
                        usage.lastHour(descriptor.id()),
                        null,
                        clock.instant());
            } catch (RuntimeException e) {
                log.debug("Could not refresh the health gauges of {}", descriptor.id(), e);
            }
        }
    }

    private void remember(String sourceId, HealthStatus status, String reason, int consecutiveFailures, Instant now) {
        GaugeState previous = gaugeStates.put(sourceId, new GaugeState(status, consecutiveFailures));
        if (previous != null && previous.status() != status) {
            try {
                events.publishEvent(new SourceHealthChangedEvent(sourceId, previous.status(), status, reason, now));
            } catch (RuntimeException e) {
                log.warn("A health change listener failed for {}", sourceId, e);
            }
        }
        if (gauged.add(sourceId)) {
            for (HealthStatus candidate : HealthStatus.values()) {
                Gauge.builder(
                                STATUS_GAUGE,
                                gaugeStates,
                                states -> {
                                    GaugeState state = states.get(sourceId);
                                    return state != null && state.status() == candidate ? 1.0 : 0.0;
                                })
                        .description("1 when the source currently has this health status, else 0")
                        .tag("source", sourceId)
                        .tag("status", candidate.value())
                        .register(meters);
            }
            Gauge.builder(
                            FAILURES_GAUGE,
                            gaugeStates,
                            states -> {
                                GaugeState state = states.get(sourceId);
                                return state == null ? 0.0 : state.consecutiveFailures();
                            })
                    .description("Consecutive failed fetches of the source")
                    .tag("source", sourceId)
                    .register(meters);
        }
    }
}
