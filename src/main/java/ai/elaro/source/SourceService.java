package ai.elaro.source;

import ai.elaro.api.error.ConflictException;
import ai.elaro.api.error.ElaroException;
import ai.elaro.api.error.FieldError;
import ai.elaro.api.error.ValidationException;
import ai.elaro.security.CredentialCipher;
import ai.elaro.security.Encrypted;
import ai.elaro.settings.Durations;
import ai.elaro.source.spi.Budget;
import ai.elaro.source.spi.CredentialField;
import ai.elaro.source.spi.IngestPolicy;
import ai.elaro.source.spi.IngestPolicyValidator;
import ai.elaro.source.spi.SourceDescriptor;
import ai.elaro.source.spi.ValidatedParams;
import ai.elaro.store.SourceCredentialEntity;
import ai.elaro.store.SourceCredentialRepository;
import ai.elaro.store.SourceSettingsEntity;
import ai.elaro.store.SourceSettingsRepository;
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
    public static final String RETENTION_TIER_FIXED = "RETENTION_TIER_FIXED";

    private final SourceRegistry registry;
    private final SourceSettingsRepository settings;
    private final SourceCredentialRepository credentials;
    private final ParamValidator paramValidator;
    private final IngestPolicyCodec policyCodec;
    private final CredentialCipher cipher;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public SourceService(
            SourceRegistry registry,
            SourceSettingsRepository settings,
            SourceCredentialRepository credentials,
            ParamValidator paramValidator,
            IngestPolicyCodec policyCodec,
            CredentialCipher cipher,
            ApplicationEventPublisher events,
            Clock clock) {
        this.registry = registry;
        this.settings = settings;
        this.credentials = credentials;
        this.paramValidator = paramValidator;
        this.policyCodec = policyCodec;
        this.cipher = cipher;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedDefaults();
    }

    // ---------- startup ----------

    /**
     * Inserts a default, disabled settings row for every registered source that has none.
     *
     * <p>Defaults are disabled on purpose: installing Elaro must not start fetching from a hundred
     * upstreams. The operator opts each source in.
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
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (existing.contains(descriptor.id())) {
                continue;
            }
            created.add(defaultRow(descriptor));
        }
        if (!created.isEmpty()) {
            settings.saveAll(created);
            log.info("Created default settings for {} new source(s)", created.size());
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

    private SourceSettingsEntity defaultRow(SourceDescriptor descriptor) {
        var entity = new SourceSettingsEntity(descriptor.id());
        entity.setEnabled(false);
        entity.setIntervalSeconds(
                descriptor.defaultInterval() == null ? 0 : (int) descriptor.defaultInterval().toSeconds());
        entity.setParams(new LinkedHashMap<>());
        Budget budget = descriptor.defaultBudget();
        entity.setBudgetPerMinute(budget == null ? 0 : budget.perMinute());
        entity.setBudgetPerDay(budget == null ? 0 : budget.perDay());
        entity.setRetention(null);
        entity.setIngestPolicy(null);
        entity.setUpdatedAt(clock.instant());
        return entity;
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
     * @throws ai.elaro.api.error.NotFoundException when the source is not registered
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
                    null);
        }
        return new SourceView(
                descriptor,
                row.isEnabled(),
                Duration.ofSeconds(row.getIntervalSeconds()),
                effectiveParamsOrRaw(descriptor, row),
                new Budget(row.getBudgetPerMinute(), row.getBudgetPerDay()),
                resolveRetention(descriptor, row),
                resolveIngestPolicy(descriptor, row),
                credentialsSet,
                row.getUpdatedAt());
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
            if (budget.perMinute() >= 1 && budget.perDay() >= 1) {
                row.setBudgetPerMinute(budget.perMinute());
                row.setBudgetPerDay(budget.perDay());
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
        applyIngestPolicy(descriptor, update, row, errors);

        if (update.enabled() != null) {
            row.setEnabled(update.enabled());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("The settings for " + id + " are not valid.", errors);
        }

        // Checked after every other field has been applied, so enabling and supplying parameters in
        // one call behaves as the operator expects.
        if (row.isEnabled()) {
            requireCredentials(descriptor, id);
        }

        row.setUpdatedAt(clock.instant());
        settings.save(row);

        SourceView after = toView(descriptor, row, credentials.existsById(id));
        publishAfterCommit(new SourceSettingsChangedEvent(id, before, after));
        return after;
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
            throw new ElaroException(
                    HttpStatus.BAD_REQUEST,
                    RETENTION_TIER_FIXED,
                    "Retention for "
                            + descriptor.id()
                            + " is fixed by its TRACK history tier and cannot be set per source.");
        }
        Duration retention = update.retention();
        if (retention.isZero() || retention.isNegative()) {
            errors.add(new FieldError("retention", "must be greater than zero"));
            return;
        }
        row.setRetention(Durations.format(retention));
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
        List<String> problems = IngestPolicyValidator.problems(policy, descriptor, "ingestPolicy");
        if (!problems.isEmpty()) {
            problems.forEach(problem -> errors.add(new FieldError("ingestPolicy", problem)));
            return;
        }
        row.setIngestPolicy(policyCodec.toJson(policy));
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

    /** The budget a fetch should be charged against. */
    @Transactional(readOnly = true)
    public Budget effectiveBudget(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        return settings
                .findById(id)
                .map(row -> new Budget(row.getBudgetPerMinute(), row.getBudgetPerDay()))
                .orElseGet(descriptor::defaultBudget);
    }

    /** The ingest policy in force: the operator's override, else the descriptor's default. */
    @Transactional(readOnly = true)
    public IngestPolicy effectiveIngestPolicy(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        return resolveIngestPolicy(descriptor, settings.findById(id).orElse(null));
    }

    /**
     * The retention window in force, or empty when the global default applies.
     *
     * <p>Always empty for a TRACK source: its retention is fixed by the tier.
     */
    @Transactional(readOnly = true)
    public Optional<Duration> effectiveRetention(String id) {
        SourceDescriptor descriptor = registry.descriptor(id);
        return Optional.ofNullable(resolveRetention(descriptor, settings.findById(id).orElse(null)));
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
