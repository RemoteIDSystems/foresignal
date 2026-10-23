package ai.foresignal.source;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.store.SourceCredentialEntity;
import ai.foresignal.store.SourceCredentialRepository;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Owns the subscription half of the system: which sources run, how often, at what budget, with
 * which parameters and credentials.
 *
 * <p>A source is subscribed when {@code enabled = true}, and that row is the only thing that causes
 * an upstream request. Nothing a client reads or streams reaches here.
 *
 * <p>Credential values never leave this package: {@link SourceView} carries only
 * {@code credentialsSet}, and decryption lives on {@link CredentialsAccess}.
 */
@Service
public class SourceService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SourceService.class);

    /** Returned when an operator tries to set retention on a TRACK source. */
    public static final String RETENTION_TIER_FIXED = ErrorCodes.RETENTION_TIER_FIXED;

    /** The field every downsample-interval problem is reported under (prompt 044). */
    static final String DOWNSAMPLE_FIELD = "downsampleInterval";

    /** The shortest per-source downsample interval (prompt 044). */
    public static final Duration MIN_DOWNSAMPLE_INTERVAL = Duration.ofSeconds(30);

    /** Returned when an ingest-policy override breaks a Core-Data-Model §5.1 rule. */
    public static final String INGEST_POLICY_INVALID = ErrorCodes.INGEST_POLICY_INVALID;

    /** The JSON path every ingest-policy problem is reported under. */
    static final String POLICY_FIELD = "ingestPolicy";

    private final SourceRegistry registry;
    private final SourceSettingsRepository settings;
    private final SourceCredentialRepository credentials;
    private final ParamValidator paramValidator;
    private final IngestPolicyCodec policyCodec;
    private final CredentialCipher cipher;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final ObjectProvider<SourceHealthLookup> healthLookup;
    private final RetentionResolver retention;
    private final boolean defaultEnable;

    /**
     * Creates the service.
     *
     * @param healthLookup resolved lazily on each view, so health can depend on beans that depend on
     *     this service without a construction cycle
     * @param defaultEnable whether {@link #seedDefaults} enables the core sources it seeds
     *     ({@code foresignal.sources.default-enable}, true; false in tests, where cached contexts share one
     *     database and a running scheduler must never reach a real upstream)
     */
    public SourceService(
            SourceRegistry registry,
            SourceSettingsRepository settings,
            SourceCredentialRepository credentials,
            ParamValidator paramValidator,
            IngestPolicyCodec policyCodec,
            CredentialCipher cipher,
            ApplicationEventPublisher events,
            Clock clock,
            ObjectProvider<SourceHealthLookup> healthLookup,
            RetentionResolver retention,
            @Value("${foresignal.sources.default-enable:true}") boolean defaultEnable) {
        this.registry = registry;
        this.settings = settings;
        this.credentials = credentials;
        this.paramValidator = paramValidator;
        this.policyCodec = policyCodec;
        this.cipher = cipher;
        this.events = events;
        this.clock = clock;
        this.healthLookup = healthLookup;
        this.retention = retention;
        this.defaultEnable = defaultEnable;
    }

    /** Seeds default settings rows at startup. */
    @Override
    public void run(ApplicationArguments args) {
        seedDefaults();
    }

    // ---------- startup ----------

    /**
     * Inserts a default settings row for every registered source that has none, with the
     * descriptor's interval and budget.
     *
     * <p>The core set starts enabled (prompt 150, revising 145): a built-in source is seeded enabled when it is in
     * {@link SourceRegistry#CORE_IDS} and {@linkplain #runsWithoutOperatorInput runs without operator input}
     * ({@link #seedsEnabled}), so a fresh install comes up with the small live-tracking set, pulled a couple at a
     * time under the global pull governor. Every other source is seeded disabled: free non-core sources, sources
     * gated by a credential or a required parameter (a core one too, such as {@code nasa-firms} without its
     * MAP_KEY), and opt-in ones such as {@code wiki-edit-surge}; a dynamic source (prompt 024) keeps its own rule.
     * With {@code foresignal.sources.default-enable} false (the test profile) every seeded row is disabled. The
     * operator can disable any source. Only missing rows are created, so this governs fresh installs and newly
     * registered sources; {@link #enableCore()} brings an existing install to the core default and
     * {@link #enableFree()} turns on every free source.
     *
     * <p>Rows for sources that are no longer registered are left alone — an adapter removed in an
     * upgrade should not silently destroy its configuration — but are logged once so the situation
     * is visible.
     */
    @Transactional
    public void seedDefaults() {
        Set<String> existing = new TreeSet<>();
        settings.findAll().forEach(row -> existing.add(row.getSourceId()));

        List<SourceSettingsEntity> created = new ArrayList<>();
        int enabled = 0;
        for (SourceRegistry.Registration registration : registry.all()) {
            SourceDescriptor descriptor = registration.descriptor();
            if (existing.contains(descriptor.id())) {
                continue;
            }
            SourceSettingsEntity row = defaultRow(descriptor);
            if (defaultEnable && !registration.dynamic() && seedsEnabled(descriptor)) {
                row.setEnabled(true);
                enabled++;
            }
            created.add(row);
        }
        if (!created.isEmpty()) {
            settings.saveAll(created);
            log.info(
                    "Created default settings for {} new source(s), {} of them enabled (core sources)",
                    created.size(),
                    enabled);
        }

        Set<String> orphaned = new TreeSet<>(existing);
        orphaned.removeAll(registry.ids());
        if (!orphaned.isEmpty()) {
            log.warn(
                    "{} settings row(s) have no registered adapter and are being ignored: {}."
                            + " Their configuration is kept in case the adapter returns.",
                    orphaned.size(),
                    orphaned);
        }
    }

    /**
     * True when {@code descriptor} starts enabled: it {@linkplain #runsWithoutOperatorInput runs without
     * operator input} and its descriptor does not hold it off ({@link SourceDescriptor#autoEnable()},
     * prompt 146). The rule of {@link #enableFree()}.
     */
    boolean startsEnabled(SourceDescriptor descriptor) {
        return descriptor.autoEnable() && runsWithoutOperatorInput(descriptor);
    }

    /**
     * True when {@code descriptor} is seeded enabled (prompt 150): it is in the {@linkplain SourceRegistry#CORE_IDS
     * core set} and {@linkplain #runsWithoutOperatorInput runs without operator input}. The rule of
     * {@link #seedDefaults} and {@link #enableCore()}.
     */
    boolean seedsEnabled(SourceDescriptor descriptor) {
        return registry.isCore(descriptor.id()) && runsWithoutOperatorInput(descriptor);
    }

    /**
     * True when {@code descriptor} runs with zero operator input (prompt 145): it requires no
     * credential, declares no required parameter, and its parameters are valid with nothing supplied.
     *
     * <p>The last test is the {@link #update} enable guard's own ({@link ParamValidator#validate} with
     * no parameters, which includes the adapter's {@code checkParams}). The first two alone are not
     * enough: the {@code local-acars-*} slots, {@code usgs-water} and {@code snotel} declare nothing required yet
     * refuse an empty configuration (at least one endpoint; a box or locations; states or stations).
     * With all three, the rule is exactly what the guard accepts with default settings, so a source
     * seeded or bulk-enabled by it is never enabled-but-broken. No {@code LAN_URL} parameter has a
     * default, so validating defaults never resolves an address.
     *
     * <p>An {@link ParamType#INBOX_PATH} parameter also counts as operator input even with a default:
     * the operator still has to put a file there ({@code osm-infrastructure} was seeded enabled and failed
     * every run on an empty inbox, 2026-10-22).
     */
    boolean runsWithoutOperatorInput(SourceDescriptor descriptor) {
        if (descriptor.requiresCredentials() || descriptor.params().stream()
                .anyMatch(p -> p.required() || p.type() == ParamType.INBOX_PATH)) {
            return false;
        }
        try {
            paramValidator.validate(descriptor, Map.of());
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /**
     * A settings row with the descriptor's defaults, <em>disabled</em>. {@link #seedDefaults} enables
     * the core ones; every other caller (an update or a dynamic source with no row yet) starts from a
     * disabled row, so changing one setting of an unseeded source never switches it on.
     */
    private SourceSettingsEntity defaultRow(SourceDescriptor descriptor) {
        var entity = new SourceSettingsEntity(descriptor.id());
        entity.setEnabled(false);
        entity.setIntervalSeconds(
                descriptor.defaultInterval() == null ? 0 : (int) descriptor.defaultInterval().toSeconds());
        entity.setParams(new LinkedHashMap<>());
        Budget budget = descriptor.defaultBudget();
        entity.setBudgetPerMinute(budget == null ? 0 : budget.perMinute());
        entity.setBudgetPerDay(budget == null ? 0 : budget.perDay());
        entity.setBudgetMonthlyCapUsd(budget == null ? null : budget.monthlyCapUsd());
        entity.setRetention(null);
        entity.setIngestPolicy(null);
        entity.setUpdatedAt(clock.instant());
        return entity;
    }

    // ---------- dynamic sources (prompt 024) ----------

    /**
     * Creates the settings row of a newly registered dynamic source (a custom push source or an
     * external provider), with the descriptor's interval and budget.
     *
     * <p>Unlike the seeded rows of built-in sources, a custom source starts enabled: the operator
     * created it in order to push into it. Publishes {@link SourceSettingsChangedEvent} after commit.
     *
     * @param descriptor the registered descriptor
     * @param enabled whether the source starts enabled
     */
    @Transactional
    public void createDynamicSettings(SourceDescriptor descriptor, boolean enabled) {
        SourceSettingsEntity row = settings.findById(descriptor.id()).orElseGet(() -> defaultRow(descriptor));
        row.setEnabled(enabled);
        row.setIntervalSeconds(
                descriptor.defaultInterval() == null ? 0 : (int) descriptor.defaultInterval().toSeconds());
        Budget budget = descriptor.defaultBudget();
        row.setBudgetPerMinute(budget == null ? 0 : budget.perMinute());
        row.setBudgetPerDay(budget == null ? 0 : budget.perDay());
        row.setUpdatedAt(clock.instant());
        settings.save(row);
        publishAfterCommit(new SourceSettingsChangedEvent(descriptor.id(), null, view(descriptor.id())));
    }

    /**
     * Applies a redefined dynamic source's descriptor defaults (budget and interval) to its settings
     * row, keeping {@code enabled}, parameters, retention and the policy override. Publishes
     * {@link SourceSettingsChangedEvent} after commit so the writer refreshes the source's profile.
     */
    @Transactional
    public void refreshDynamicSettings(SourceDescriptor descriptor) {
        SourceView before = view(descriptor.id());
        SourceSettingsEntity row = settings.findById(descriptor.id()).orElseGet(() -> defaultRow(descriptor));
        Budget budget = descriptor.defaultBudget();
        row.setBudgetPerMinute(budget == null ? 0 : budget.perMinute());
        row.setBudgetPerDay(budget == null ? 0 : budget.perDay());
        if (descriptor.defaultInterval() != null) {
            row.setIntervalSeconds((int) descriptor.defaultInterval().toSeconds());
        }
        row.setUpdatedAt(clock.instant());
        settings.save(row);
        publishAfterCommit(new SourceSettingsChangedEvent(descriptor.id(), before, view(descriptor.id())));
    }

    /**
     * Switches a dynamic source on or off without the credential check of {@link #update}: an
     * external provider is enabled by its approval and disabled while pending or suspended
     * (prompt 024). A pull provider without its credential then fails its fetches visibly.
     */
    @Transactional
    public void setDynamicEnabled(String id, boolean enabled) {
        SourceDescriptor descriptor = registry.descriptor(id);
        SourceView before = view(id);
        SourceSettingsEntity row = settings.findById(id).orElseGet(() -> defaultRow(descriptor));
        if (row.isEnabled() == enabled && settings.existsById(id)) {
            return;
        }
        row.setEnabled(enabled);
        row.setUpdatedAt(clock.instant());
        settings.save(row);
        publishAfterCommit(new SourceSettingsChangedEvent(id, before, view(id)));
    }

    /**
     * Deletes a removed dynamic source's settings and credentials. The source must already be
     * unregistered; its records age out through retention.
     */
    @Transactional
    public void deleteDynamicSettings(String id) {
        settings.findById(id).ifPresent(settings::delete);
        credentials.findById(id).ifPresent(credentials::delete);
    }

    // ---------- views ----------

    /** Every registered source with its effective settings, ordered by id. */
    @Transactional(readOnly = true)
    public List<SourceView> list() {
        Set<String> withCredentials = new TreeSet<>();
        credentials.findAll().forEach(row -> withCredentials.add(row.getSourceId()));
        Map<String, SourceSettingsEntity> rows = new LinkedHashMap<>();
        settings.findAll().forEach(row -> rows.put(row.getSourceId(), row));

        return registry.descriptors().stream()
                .map(
                        descriptor ->
                                toView(
                                        descriptor,
                                        rows.get(descriptor.id()),
                                        withCredentials.contains(descriptor.id())))
                .toList();
    }

    /**
     * One source's view.
     *
     * @throws ai.foresignal.api.error.NotFoundException when the source is not registered
     */
    @Transactional(readOnly = true)
    public SourceView view(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        return toView(descriptor, settings.findById(id).orElse(null), credentials.existsById(id));
    }

    private SourceView toView(
            SourceDescriptor descriptor, SourceSettingsEntity row, boolean credentialsSet) {
        if (row == null) {
            // Registered but not yet seeded: present the descriptor's own defaults rather than
            // nulls, so a view is always answerable.
            return new SourceView(
                    descriptor,
                    false,
                    descriptor.defaultInterval(),
                    defaultParams(descriptor),
                    descriptor.defaultBudget(),
                    descriptor.defaultRetention(),
                    IngestPolicyValidator.applyDefaults(descriptor.defaultIngestPolicy(), descriptor),
                    credentialsSet,
                    null,
                    healthOf(descriptor, false));
        }
        return new SourceView(
                descriptor,
                row.isEnabled(),
                Duration.ofSeconds(row.getIntervalSeconds()),
                effectiveParamsOrRaw(descriptor, row),
                budgetOf(descriptor, row),
                resolveRetention(descriptor, row),
                resolveIngestPolicy(descriptor, row),
                credentialsSet,
                row.getUpdatedAt(),
                healthOf(descriptor, row.isEnabled()),
                row.getEmbed() != null ? row.getEmbed() : SourceView.defaultEmbed(descriptor));
    }

    /**
     * The health slice of a view, from the {@link SourceHealthLookup} when one is present.
     *
     * <p>A failure to read health must not make the source list unanswerable, so it degrades to
     * {@link SourceHealthSummary#unavailable(boolean)} with a log line instead.
     */
    private SourceHealthSummary healthOf(SourceDescriptor descriptor, boolean enabled) {
        SourceHealthLookup lookup = healthLookup.getIfAvailable();
        if (lookup == null) {
            return SourceHealthSummary.unavailable(enabled);
        }
        try {
            return lookup.summary(descriptor, enabled);
        } catch (RuntimeException e) {
            log.warn("Could not read health for {}; reporting it as unavailable", descriptor.id(), e);
            return SourceHealthSummary.unavailable(enabled);
        }
    }

    // ---------- updates ----------

    /**
     * Applies a partial settings change.
     *
     * @throws ValidationException when any supplied value is not acceptable
     * @throws ConflictException {@code CREDENTIALS_REQUIRED} when enabling without the credentials
     *     the source needs
     */
    @Transactional
    public SourceView update(String id, SourceSettingsUpdate update) {
        SourceDescriptor descriptor = registry.descriptor(id);
        SourceSettingsEntity row = settings.findById(id).orElseGet(() -> defaultRow(descriptor));
        SourceView before = settings.findById(id).isPresent() ? view(id) : null;

        List<FieldError> errors = new ArrayList<>();
        List<FieldError> policyErrors = new ArrayList<>();

        if (update.interval() != null) {
            Duration interval = update.interval();
            if (interval.isZero() || interval.isNegative()) {
                errors.add(new FieldError("interval", "must be greater than zero"));
            } else if (descriptor.minInterval() != null
                    && interval.compareTo(descriptor.minInterval()) < 0) {
                errors.add(
                        new FieldError(
                                "interval",
                                "must be at least "
                                        + Durations.format(descriptor.minInterval())
                                        + " for this source, but was "
                                        + Durations.format(interval)));
            } else {
                row.setIntervalSeconds((int) interval.toSeconds());
            }
        }

        if (update.budget() != null) {
            Budget budget = update.budget();
            if (budget.perMinute() < 1) {
                errors.add(new FieldError("budget.perMinute", "must be at least 1"));
            }
            if (budget.perDay() < 1) {
                errors.add(new FieldError("budget.perDay", "must be at least 1"));
            }
            boolean capValid = budget.monthlyCapUsd() == null || budget.monthlyCapUsd().signum() >= 0;
            if (!capValid) {
                errors.add(new FieldError("budget.monthlyCapUsd", "must be at least 0"));
            }
            if (budget.perMinute() >= 1 && budget.perDay() >= 1 && capValid) {
                row.setBudgetPerMinute(budget.perMinute());
                row.setBudgetPerDay(budget.perDay());
                // The budget object is replaced whole: no monthlyCapUsd means no money cap.
                row.setBudgetMonthlyCapUsd(budget.monthlyCapUsd());
            }
        }

        if (update.params() != null) {
            try {
                paramValidator.validate(descriptor, update.params());
                row.setParams(new LinkedHashMap<>(update.params()));
            } catch (ValidationException e) {
                errors.addAll(e.getErrors());
                if (e.getErrors().isEmpty()) {
                    errors.add(new FieldError("params", e.getMessage()));
                }
            }
        }

        applyRetention(descriptor, update, row, errors);
        applyDownsampleInterval(descriptor, update, row, errors);
        applyIngestPolicy(descriptor, update, row, policyErrors);
        applyEmbed(descriptor, update, row, errors);
        applyEgress(descriptor, update, row);
        applyReliability(update, row);

        if (update.enabled() != null) {
            row.setEnabled(update.enabled());
        }

        if (!policyErrors.isEmpty()) {
            // A policy problem gets its own code so a client can tell "fix the policy" from "fix
            // a field"; every other error found in the same request travels with it.
            List<FieldError> all = new ArrayList<>(policyErrors);
            all.addAll(errors);
            throw new ValidationException(
                    INGEST_POLICY_INVALID, "The ingest policy for " + id + " is not valid.", all);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The settings for " + id + " are not valid.", errors);
        }

        // Checked after every other field has been applied, so enabling and supplying parameters in
        // one call behaves as the operator expects.
        if (row.isEnabled()) {
            requireCredentials(descriptor, id);
            if (Boolean.TRUE.equals(update.enabled()) && update.params() == null) {
                requireValidParams(descriptor, id, row.getParams());
            }
        }

        row.setUpdatedAt(clock.instant());
        settings.save(row);

        SourceView after = toView(descriptor, row, credentials.existsById(id));
        publishAfterCommit(new SourceSettingsChangedEvent(id, before, after));
        return after;
    }

    /**
     * The egress override (prompt 069): accepted only when the descriptor is {@code egressOverridable};
     * otherwise {@code 400 EGRESS_FIXED}, because a license restriction must not be lifted by
     * configuration. Clearing is always accepted.
     */
    /** Stores the reliability override (prompt 109); clearing reverts to the descriptor's grade. */
    private void applyReliability(SourceSettingsUpdate update, SourceSettingsEntity row) {
        if (update.clearReliability()) {
            row.setReliability(null);
            return;
        }
        if (update.reliability() != null) {
            row.setReliability(update.reliability());
        }
    }

    /** The operator's reliability override for {@code id} (prompt 109), or empty for the descriptor's grade. */
    @Transactional(readOnly = true)
    public Optional<String> reliabilityOverride(String id) {
        return settings.findById(id)
                .map(SourceSettingsEntity::getReliability)
                .filter(value -> !value.isBlank());
    }

    /**
     * The reliability grade in force for {@code id} (prompt 109): the operator's override, else the
     * descriptor's (default {@code F}). Never null.
     */
    @Transactional(readOnly = true)
    public String effectiveReliability(String id) {
        return settings.findById(id)
                .map(SourceSettingsEntity::getReliability)
                .filter(value -> value != null && !value.isBlank())
                .orElseGet(() -> registry.descriptor(id).reliability());
    }

    private void applyEgress(SourceDescriptor descriptor, SourceSettingsUpdate update, SourceSettingsEntity row) {
        if (update.clearEgress()) {
            row.setEgress(null);
            return;
        }
        if (update.egress() == null) {
            return;
        }
        if (!descriptor.egressOverridable()) {
            throw new ValidationException(ai.foresignal.api.error.ErrorCodes.EGRESS_FIXED,
                    "The egress of " + descriptor.id() + " is fixed by its descriptor (" + descriptor.egress().wireValue()
                            + ") and cannot be overridden.",
                    List.of(new FieldError("egress", "this source's egress is not overridable")));
        }
        row.setEgress(update.egress().wireValue());
    }

    /**
     * The operator's egress override (prompt 069), or empty when the descriptor's {@code egress} applies.
     * Always empty for a descriptor that is not {@code egressOverridable}, whatever is stored.
     */
    @Transactional(readOnly = true)
    public Optional<ai.foresignal.source.spi.Egress> egressOverride(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        if (!descriptor.egressOverridable()) {
            return Optional.empty();
        }
        return settings.findById(id)
                .map(SourceSettingsEntity::getEgress)
                .filter(value -> !value.isBlank())
                .flatMap(value -> {
                    try {
                        return Optional.of(ai.foresignal.source.spi.Egress.valueOf(value));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }

    /** The egress in force for {@code id} (prompt 069): the override if any, else the descriptor's. */
    @Transactional(readOnly = true)
    public ai.foresignal.source.spi.Egress effectiveEgress(String id) {
        return egressOverride(id).orElseGet(() -> registry.descriptor(id).egress());
    }

    private void applyRetention(
            SourceDescriptor descriptor,
            SourceSettingsUpdate update,
            SourceSettingsEntity row,
            List<FieldError> errors) {
        if (update.clearRetention()) {
            if (descriptor.isTrack()) {
                // Clearing is harmless, but a TRACK source never had one to clear; say so rather
                // than silently accepting a no-op.
                row.setRetention(null);
                return;
            }
            row.setRetention(null);
            return;
        }
        if (update.retention() == null) {
            return;
        }
        if (descriptor.isTrack()) {
            // TRACK retention is a property of the tier, not a setting. Allowing an override would
            // let an operator truncate a track into something that is no longer a track.
            throw new ForesignalException(
                    HttpStatus.BAD_REQUEST,
                    RETENTION_TIER_FIXED,
                    "Retention for "
                            + descriptor.id()
                            + " is fixed by its TRACK history tier and cannot be set per source."
                            + " Change the TRACK window with the retention.trackWindow setting"
                            + " instead.");
        }
        Duration retention = update.retention();
        if (retention.isZero() || retention.isNegative()) {
            errors.add(new FieldError("retention", "must be greater than zero"));
            return;
        }
        row.setRetention(Durations.format(retention));
    }

    /**
     * The history downsample interval override (prompt 044). Accepted only for a TRACK source (a
     * STANDARD source's history is never downsampled), at least 30 s and, while downsampling is
     * enabled, no longer than {@code retention.trackRawWindow}. Clearing is accepted for any source.
     */
    private void applyDownsampleInterval(
            SourceDescriptor descriptor, SourceSettingsUpdate update, SourceSettingsEntity row, List<FieldError> errors) {
        if (update.clearDownsampleInterval()) {
            row.setDownsampleInterval(null);
            return;
        }
        Duration value = update.downsampleInterval();
        if (value == null) {
            return;
        }
        if (!descriptor.isTrack()) {
            errors.add(new FieldError(DOWNSAMPLE_FIELD, descriptor.id()
                    + " is a STANDARD source; only TRACK history is downsampled"));
            return;
        }
        if (value.compareTo(MIN_DOWNSAMPLE_INTERVAL) < 0) {
            errors.add(new FieldError(DOWNSAMPLE_FIELD, "must be at least 30s"));
            return;
        }
        ai.foresignal.settings.RetentionSettings global = retention.settings();
        if (global.downsamplingEnabled() && value.compareTo(global.trackRawWindow()) > 0) {
            errors.add(new FieldError(DOWNSAMPLE_FIELD, "must not be longer than retention.trackRawWindow ("
                    + Durations.format(global.trackRawWindow()) + ")"));
            return;
        }
        row.setDownsampleInterval(value);
    }

    /**
     * The {@code embed} flag (prompt 027). Turning it on is refused for a source that declares no
     * {@code embedFields} or is private, because nothing of it would ever be embedded.
     */
    private static void applyEmbed(
            SourceDescriptor descriptor, SourceSettingsUpdate update, SourceSettingsEntity row, List<FieldError> errors) {
        if (update.embed() == null) {
            return;
        }
        if (update.embed() && descriptor.embedFields().isEmpty()) {
            errors.add(new FieldError("embed", "this source declares no embedFields, so there is nothing to embed"));
            return;
        }
        if (update.embed() && descriptor.privateSource()) {
            errors.add(new FieldError("embed", "records of a private source are never embedded"));
            return;
        }
        row.setEmbed(update.embed());
    }

    private void applyIngestPolicy(
            SourceDescriptor descriptor,
            SourceSettingsUpdate update,
            SourceSettingsEntity row,
            List<FieldError> errors) {
        if (update.clearIngestPolicy()) {
            row.setIngestPolicy(null);
            return;
        }
        if (update.ingestPolicy() == null) {
            return;
        }
        IngestPolicy policy = IngestPolicyValidator.applyDefaults(update.ingestPolicy(), descriptor);
        List<String> problems = IngestPolicyValidator.problems(policy, descriptor, POLICY_FIELD);
        if (!problems.isEmpty()) {
            problems.forEach(problem -> errors.add(new FieldError(policyFieldOf(problem), problem)));
            return;
        }
        row.setIngestPolicy(policyCodec.toJson(policy));
    }

    /**
     * The field path a validator message is about.
     *
     * <p>{@link IngestPolicyValidator} starts each message with the path it concerns, for example
     * {@code "ingestPolicy.keyframe (PT1H) must not exceed ..."}, so the leading token is the field.
     * A message about the descriptor itself (a TTL too short for any keyframe) names no field and is
     * reported under {@code ingestPolicy.keyframe}, the value an operator would have to change.
     */
    static String policyFieldOf(String problem) {
        if (problem == null || !problem.startsWith(POLICY_FIELD + ".")) {
            return POLICY_FIELD + ".keyframe";
        }
        int end = problem.length();
        for (int i = POLICY_FIELD.length() + 1; i < problem.length(); i++) {
            char c = problem.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '.') {
                end = i;
                break;
            }
        }
        String field = problem.substring(0, end);
        return field.endsWith(".") ? field.substring(0, field.length() - 1) : field;
    }

    /**
     * Enables every currently disabled built-in source that {@linkplain #startsEnabled starts
     * enabled} ({@code POST /v1/sources/enable-free}, prompt 145): the one-time
     * control that brings an install seeded before prompt 145 to the free-sources default without
     * re-seeding, which would discard its configuration.
     *
     * <p>Each source is enabled through {@link #update}, so the same checks run and the same
     * {@link SourceSettingsChangedEvent} is published after commit; the collector picks the sources
     * up within seconds. A source the enable guard still refuses (stored parameters that are not
     * valid) is left disabled and counted as skipped, as is a gated source or one its descriptor holds
     * off ({@link SourceDescriptor#autoEnable()} false, prompt 146). Enabled rows and dynamic sources
     * (prompt 024: a custom push source or an external provider, which is enabled by its approval) are
     * left untouched. Idempotent: a second call enables nothing.
     *
     * @return the ids enabled, sorted, and how many disabled built-in sources were left disabled
     */
    @Transactional
    public EnableFreeResult enableFree() {
        return enableWhere(this::startsEnabled, "free");
    }

    /**
     * Enables every currently disabled built-in source that is {@linkplain #seedsEnabled seeded enabled} — the core
     * set ({@code POST /v1/sources/enable-core}, prompt 150): the one call by which an install seeded before prompt
     * 150 adopts the core default without re-seeding. Like {@link #enableFree()} it goes through {@link #update},
     * leaves enabled rows and dynamic sources alone, disables nothing, and is idempotent; a core source that needs
     * operator input ({@code nasa-firms} without its MAP_KEY) is counted as skipped, as is every non-core source.
     *
     * @return the ids enabled, sorted, and how many disabled built-in sources were left disabled
     */
    @Transactional
    public EnableFreeResult enableCore() {
        return enableWhere(this::seedsEnabled, "core");
    }

    private EnableFreeResult enableWhere(java.util.function.Predicate<SourceDescriptor> rule, String label) {
        Set<String> enabledRows = new TreeSet<>();
        settings.findByEnabledTrue().forEach(row -> enabledRows.add(row.getSourceId()));
        List<String> enabled = new ArrayList<>();
        int skipped = 0;
        for (SourceRegistry.Registration registration : registry.all()) {
            String id = registration.id();
            if (registration.dynamic() || enabledRows.contains(id)) {
                continue;
            }
            if (!rule.test(registration.descriptor())) {
                skipped++;
                continue;
            }
            try {
                update(id, SourceSettingsUpdate.enabled(true));
                enabled.add(id);
            } catch (ValidationException | ConflictException e) {
                // Refused before anything was saved: its stored settings need the operator first.
                log.info("Not enabling {}: {}", id, e.getMessage());
                skipped++;
            }
        }
        enabled.sort(null);
        if (!enabled.isEmpty()) {
            log.info("Enabled {} {} source(s): {}", enabled.size(), label, enabled);
        }
        return new EnableFreeResult(enabled, skipped);
    }

    /**
     * What {@link #enableFree()} or {@link #enableCore()} did.
     *
     * @param enabled the ids it enabled, sorted
     * @param skipped the disabled built-in sources it left disabled: gated by a credential or a
     *     required parameter, held off by their descriptor, outside the core set (for {@link #enableCore()}), or
     *     refused by the enable guard
     */
    public record EnableFreeResult(List<String> enabled, int skipped) {

        public EnableFreeResult {
            enabled = List.copyOf(enabled);
        }

        /** How many sources were enabled. */
        public int count() {
            return enabled.size();
        }
    }

    /**
     * Refuses to enable a source whose stored parameters are not valid (prompt 082): a required parameter that was
     * never set, or a combination its adapter's {@code checkParams} refuses ({@code aprs-is} cannot run without a
     * narrow {@code filter}). Parameters supplied in the same request were already validated.
     *
     * @throws ValidationException listing the problems as {@code params.<name>} field errors
     */
    private void requireValidParams(SourceDescriptor descriptor, String id, Map<String, Object> stored) {
        try {
            paramValidator.validate(descriptor, stored == null ? Map.of() : stored);
        } catch (ValidationException e) {
            throw new ValidationException("Cannot enable " + id + " until its parameters are valid; set them with"
                    + " PUT /v1/sources/" + id + ".", e.getErrors());
        }
    }

    private void requireCredentials(SourceDescriptor descriptor, String id) {
        List<CredentialField> required = descriptor.requiredCredentialFields();
        if (required.isEmpty()) {
            return;
        }
        Map<String, String> stored = credentials(id);
        List<String> missing =
                required.stream()
                        .map(CredentialField::name)
                        .filter(name -> !stored.containsKey(name) || stored.get(name).isBlank())
                        .toList();
        if (!missing.isEmpty()) {
            throw new ConflictException(
                    ConflictException.CREDENTIALS_REQUIRED,
                    "Cannot enable "
                            + id
                            + " until these credential field(s) are set: "
                            + String.join(", ", missing)
                            + ". Set them with PUT /v1/sources/"
                            + id
                            + "/credentials.");
        }
    }

    // ---------- credentials ----------

    /**
     * Stores {@code values}, replacing anything already stored.
     *
     * @throws ValidationException when a key is not declared, a required field is missing, or a
     *     value is blank
     */
    @Transactional
    public void setCredentials(String id, Map<String, String> values) {
        SourceDescriptor descriptor = registry.descriptor(id);
        Map<String, String> supplied = values == null ? Map.of() : values;

        Set<String> declared = new TreeSet<>();
        descriptor.credentialFields().forEach(field -> declared.add(field.name()));

        List<FieldError> errors = new ArrayList<>();
        if (declared.isEmpty()) {
            throw new ValidationException(descriptor.id() + " does not take any credentials.");
        }
        for (Map.Entry<String, String> entry : supplied.entrySet()) {
            if (!declared.contains(entry.getKey())) {
                errors.add(
                        new FieldError(
                                "values." + entry.getKey(),
                                "is not a credential field of " + id + ". Known fields: " + declared));
                continue;
            }
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                errors.add(new FieldError("values." + entry.getKey(), "must not be blank"));
            }
        }
        for (CredentialField field : descriptor.requiredCredentialFields()) {
            if (!supplied.containsKey(field.name())) {
                errors.add(new FieldError("values." + field.name(), "is required"));
            }
        }
        if (errors.isEmpty()) {
            // Prompt 062: rules that span fields, such as a client id that needs its secret.
            registry.find(id)
                    .flatMap(SourceRegistry.Registration::adapterIfPresent)
                    .map(adapter -> adapter.checkCredentials(Map.copyOf(supplied)))
                    .orElseGet(Map::of)
                    .forEach((name, problem) -> errors.add(new FieldError("values." + name, problem)));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The credentials for " + id + " are not valid.", errors);
        }

        Encrypted encrypted = cipher.encrypt(supplied);
        credentials.save(
                new SourceCredentialEntity(
                        id, encrypted.ciphertext(), encrypted.nonce(), clock.instant()));
        publishAfterCommit(SourceCredentialsChangedEvent.stored(id));
    }

    /**
     * Removes the stored credentials.
     *
     * <p>A source that needs credentials and is left enabled would fail on every fetch and look
     * like a broken upstream, so it is disabled here instead.
     */
    @Transactional
    public void deleteCredentials(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        credentials.deleteById(id);
        publishAfterCommit(SourceCredentialsChangedEvent.removed(id));

        if (!descriptor.requiresCredentials()) {
            return;
        }
        settings
                .findById(id)
                .filter(SourceSettingsEntity::isEnabled)
                .ifPresent(
                        row -> {
                            SourceView before = toView(descriptor, row, true);
                            row.setEnabled(false);
                            row.setUpdatedAt(clock.instant());
                            settings.save(row);
                            log.info(
                                    "Disabled {} because its credentials were removed and it requires them",
                                    id);
                            publishAfterCommit(
                                    new SourceSettingsChangedEvent(
                                            id, before, toView(descriptor, row, false)));
                        });
    }

    /** True when credentials are stored. Values are never returned here. */
    @Transactional(readOnly = true)
    public boolean credentialsSet(String id) {
        registry.descriptor(id);
        return credentials.existsById(id);
    }

    /**
     * The decrypted credentials, for the {@code collect} package only.
     *
     * <p>Package-private on purpose: a controller cannot reach it. {@link CredentialsAccess} is the
     * bean the collector injects.
     */
    Map<String, String> credentials(String id) {
        return credentials
                .findById(id)
                .map(entity -> cipher.decryptToMap(new Encrypted(entity.getCiphertext(), entity.getNonce())))
                .orElseGet(Map::of);
    }

    // ---------- effective settings ----------

    /** The validated parameters a fetch should run with. */
    @Transactional(readOnly = true)
    public ValidatedParams effectiveParams(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        Map<String, Object> raw =
                settings.findById(id).map(SourceSettingsEntity::getParams).orElseGet(Map::of);
        return paramValidator.validate(descriptor, raw);
    }

    /**
     * The parameters as the operator stored them, before defaults and coercion (prompt 117: the attention topics are
     * merged into them as one key, so the rest of the stored set is kept when {@link #update} replaces it wholesale).
     *
     * @return a copy, empty when nothing is stored
     */
    @Transactional(readOnly = true)
    public Map<String, Object> storedParams(String id) {
        registry.descriptor(id);
        return settings.findById(id)
                .map(SourceSettingsEntity::getParams)
                .map(params -> (Map<String, Object>) new LinkedHashMap<>(params))
                .orElseGet(LinkedHashMap::new);
    }

    /** The budget a fetch should be charged against. */
    @Transactional(readOnly = true)
    public Budget effectiveBudget(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        return settings
                .findById(id)
                .map(row -> budgetOf(descriptor, row))
                .orElseGet(descriptor::defaultBudget);
    }

    /** The ingest policy in force: the operator's override, else the descriptor's default. */
    @Transactional(readOnly = true)
    public IngestPolicy effectiveIngestPolicy(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        return resolveIngestPolicy(descriptor, settings.findById(id).orElse(null));
    }

    /**
     * The operator's ingest-policy override as stored, or empty when the descriptor default applies.
     *
     * <p>Unlike {@link #effectiveIngestPolicy(String)} no defaults are filled in, so a client can
     * tell an override from the default.
     */
    @Transactional(readOnly = true)
    public Optional<IngestPolicy> ingestPolicyOverride(String id) {
        registry.descriptor(id);
        return settings.findById(id).map(row -> policyCodec.fromJson(row.getIngestPolicy()));
    }

    /**
     * The operator's per-source retention override, or empty when none is stored.
     *
     * <p>Always empty for a TRACK source, which cannot have one.
     */
    @Transactional(readOnly = true)
    public Optional<Duration> retentionOverride(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        if (descriptor.isTrack()) {
            return Optional.empty();
        }
        return settings.findById(id)
                .map(SourceSettingsEntity::getRetention)
                .filter(value -> !value.isBlank())
                .map(Durations::parse);
    }

    /**
     * The operator's downsample interval override (prompt 044), or empty when the global
     * {@code retention.trackDownsampleInterval} applies. Always empty for a STANDARD source.
     */
    @Transactional(readOnly = true)
    public Optional<Duration> downsampleIntervalOverride(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        if (!descriptor.isTrack()) {
            return Optional.empty();
        }
        return settings.findById(id).map(SourceSettingsEntity::getDownsampleInterval);
    }

    /**
     * The history downsample interval of {@code sourceId} (prompt 044): its override, else
     * {@code retention.trackDownsampleInterval}. A STANDARD source answers the global interval too,
     * though its history is never downsampled.
     *
     * @throws ai.foresignal.api.error.NotFoundException when the source is not registered
     */
    @Transactional(readOnly = true)
    public Duration effectiveDownsampleInterval(String sourceId) {
        return downsampleIntervalOverride(sourceId).orElseGet(() -> retention.settings().trackDownsampleInterval());
    }

    /**
     * The source-level retention window from the source's own settings (the override, else the
     * descriptor's {@code defaultRetention}), or empty when the global default applies. Delegates to
     * {@link RetentionResolver#sourceWindow(String)} (prompt 019); per-kind windows are
     * {@link RetentionResolver#window(String, String)}'s.
     *
     * <p>Always empty for a TRACK source: its retention is fixed by the tier.
     */
    @Transactional(readOnly = true)
    public Optional<Duration> effectiveRetention(String id) {
        return retention.sourceWindow(id);
    }

    /**
     * The request limits and the monthly money cap from the settings row, plus the descriptor's byte budget.
     *
     * <p>{@code mbPerDay} is not operator-overridable yet: there is no column for it, and it is a
     * property of how large the upstream's objects are rather than a preference.
     */
    private static Budget budgetOf(SourceDescriptor descriptor, SourceSettingsEntity row) {
        Integer mbPerDay =
                descriptor.defaultBudget() == null ? null : descriptor.defaultBudget().mbPerDay();
        return new Budget(row.getBudgetPerMinute(), row.getBudgetPerDay(), mbPerDay, row.getBudgetMonthlyCapUsd());
    }

    private IngestPolicy resolveIngestPolicy(SourceDescriptor descriptor, SourceSettingsEntity row) {
        IngestPolicy stored = row == null ? null : policyCodec.fromJson(row.getIngestPolicy());
        IngestPolicy policy = stored != null ? stored : descriptor.defaultIngestPolicy();
        return IngestPolicyValidator.applyDefaults(policy, descriptor);
    }

    private Duration resolveRetention(SourceDescriptor descriptor, SourceSettingsEntity row) {
        if (descriptor.isTrack()) {
            return null;
        }
        if (row != null && row.getRetention() != null && !row.getRetention().isBlank()) {
            return Durations.parse(row.getRetention());
        }
        return descriptor.defaultRetention();
    }

    private Map<String, Object> defaultParams(SourceDescriptor descriptor) {
        try {
            return paramValidator.validate(descriptor, Map.of()).values();
        } catch (ValidationException e) {
            // A required parameter with no value: show nothing rather than failing a read.
            return Map.of();
        }
    }

    private Map<String, Object> effectiveParamsOrRaw(
            SourceDescriptor descriptor, SourceSettingsEntity row) {
        try {
            return paramValidator.validate(descriptor, row.getParams()).values();
        } catch (ValidationException e) {
            // Stored params that no longer validate — an adapter's spec changed under them. Show
            // what is stored so an operator can see and fix it, rather than hiding the row.
            return row.getParams();
        }
    }

    private void publishAfterCommit(Object event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            events.publishEvent(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        events.publishEvent(event);
                    }
                });
    }
}
