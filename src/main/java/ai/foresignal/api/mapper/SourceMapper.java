package ai.foresignal.api.mapper;

import ai.foresignal.api.dto.BudgetDto;
import ai.foresignal.api.dto.CredentialFieldDto;
import ai.foresignal.api.dto.FieldThresholdDto;
import ai.foresignal.api.dto.IngestPolicyDto;
import ai.foresignal.api.dto.MotionSpecDto;
import ai.foresignal.api.dto.ParamSpecDto;
import ai.foresignal.api.dto.SourceDescriptorDto;
import ai.foresignal.api.dto.SourceSettingsDto;
import ai.foresignal.api.dto.SourceSummary;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.IngestPolicyCodec;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Maps sources between the domain ({@link SourceDescriptor}, {@link SourceView}, {@link IngestPolicy})
 * and the API records ({@link SourceSummary}, {@link SourceDescriptorDto}, {@link SourceSettingsDto},
 * {@link IngestPolicyDto}), and turns an API settings update into the service's
 * {@link SourceSettingsUpdate}.
 *
 * <p>Ingest policies go through {@link IngestPolicyCodec} in both directions, so the API body and
 * {@code source_settings.ingest_policy} share one JSON shape.
 */
@Component
public class SourceMapper {

    private static final Logger log = LoggerFactory.getLogger(SourceMapper.class);

    static final String POLICY_FIELD = "ingestPolicy";

    private final IngestPolicyCodec codec;
    private final JsonMapper jsonMapper;
    private final AppSettingService appSettings;

    /**
     * Creates the mapper.
     *
     * @param codec the one ingest-policy JSON shape
     * @param jsonMapper Boot's mapper (durations in shorthand)
     * @param appSettings read for {@code retention.trackWindow} and the global retention default
     */
    public SourceMapper(IngestPolicyCodec codec, JsonMapper jsonMapper, AppSettingService appSettings) {
        this.codec = codec;
        this.jsonMapper = jsonMapper;
        this.appSettings = appSettings;
    }

    // ---------- summary ----------

    /** One row of {@code GET /v1/sources}. */
    public SourceSummary summary(SourceView view) {
        SourceDescriptor d = view.descriptor();
        return new SourceSummary(
                d.id(),
                d.name(),
                sorted(d.kinds()),
                d.mode().wireValue(),
                d.custom(),
                view.enabled(),
                view.health().status(),
                d.requiresCredentials(),
                view.credentialsSet(),
                view.interval(),
                budget(view.budget()),
                view.health().budgetUsedToday(),
                view.health().lastSuccessAt(),
                d.historyTier().name(),
                d.licence(),
                d.termsUrl(),
                view.health().lastDataAt());
    }

    // ---------- descriptor ----------

    /** Every descriptor field the API exposes. */
    public SourceDescriptorDto descriptor(SourceDescriptor d) {
        return new SourceDescriptorDto(
                d.id(),
                d.name(),
                d.description(),
                sorted(d.kinds()),
                d.homepage(),
                d.attribution(),
                d.mode().wireValue(),
                d.custom(),
                d.credentialFields().stream().map(SourceMapper::credentialField).toList(),
                d.params().stream().map(SourceMapper::param).toList(),
                d.minInterval(),
                d.defaultInterval(),
                budget(d.defaultBudget()),
                d.recordTtl(),
                d.zeroResultsNormal(),
                d.tileProperties(),
                d.searchFields(),
                d.embedFields(),
                d.rankField(),
                d.licence(),
                d.termsUrl(),
                d.historyTier().name(),
                d.observedAtMode().name(),
                d.ttlAnchor().name(),
                policy(IngestPolicyValidator.applyDefaults(d.defaultIngestPolicy(), d)),
                d.volatileFields(),
                d.significantFields(),
                motion(d.motion()),
                d.thresholds().stream().map(SourceMapper::threshold).toList(),
                d.defaultRetention(),
                new TreeMap<>(d.kindRetention()),
                d.skewTolerance(),
                d.allowNullIsland(),
                d.products().stream().map(ProductSpec::id).toList(),
                d.titleField(),
                new TreeMap<>(d.entityKeys()),
                new TreeMap<>(d.fieldSpecs()),
                new TreeMap<>(d.kindDisplay()),
                d.aggBelowZoom(),
                d.startField(),
                d.endField(),
                d.privateSource(),
                d.privatePrecisionDecimals(),
                d.egress().wireValue(),
                d.originator(),
                d.access(),
                d.egressOverridable(),
                d.llmNotice(),
                sourceLicences != null ? sourceLicences.termsOf(d) : d.licenceTerms(),
                d.reliability(),
                d.derivedFrom(),
                upstreamRefs != null ? upstreamRefs.apply(d) : d.upstreamRefFields(),
                declarations != null ? declarations.apply(d) : d.observability());
    }

    private ai.foresignal.provenance.SourceLicences sourceLicences;

    private java.util.function.Function<SourceDescriptor, List<String>> upstreamRefs;

    private java.util.function.Function<SourceDescriptor, ai.foresignal.source.spi.DeclaredObservability> declarations;

    /**
     * Wires the evidence registries (prompt 116) so {@code upstreamRefFields} and {@code observability} show the
     * values in force (descriptor, else {@code intel/originators.json} and {@code intel/observability.json}).
     *
     * @param upstreamRefs the upstream reference fields in force for a descriptor
     * @param declarations the declared observability in force for a descriptor
     */
    public void setEvidenceRegistries(java.util.function.Function<SourceDescriptor, List<String>> upstreamRefs,
            java.util.function.Function<SourceDescriptor, ai.foresignal.source.spi.DeclaredObservability> declarations) {
        this.upstreamRefs = upstreamRefs;
        this.declarations = declarations;
    }

    /** Wires the license-terms resolver (prompt 109) for {@code licenceTerms}; optional so tests need none. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setSourceLicences(ai.foresignal.provenance.SourceLicences sourceLicences) {
        this.sourceLicences = sourceLicences;
    }

    static CredentialFieldDto credentialField(CredentialField field) {
        return new CredentialFieldDto(field.name(), field.description(), field.required());
    }

    static ParamSpecDto param(ParamSpec spec) {
        return new ParamSpecDto(
                spec.name(),
                spec.type().wireValue(),
                spec.description(),
                spec.defaultValue() instanceof Duration duration ? Durations.format(duration) : spec.defaultValue(),
                spec.required(),
                spec.allowed());
    }

    static MotionSpecDto motion(MotionSpec motion) {
        return motion == null
                ? null
                : new MotionSpecDto(motion.speedField(), motion.speedUnit().wireValue(), motion.courseField());
    }

    static FieldThresholdDto threshold(FieldThreshold threshold) {
        return new FieldThresholdDto(threshold.field(), threshold.delta(), threshold.angular());
    }

    /** The API budget: request limits and the monthly money cap ({@code mbPerDay} is not part of the schema). */
    public static BudgetDto budget(Budget budget) {
        return budget == null ? null : new BudgetDto(budget.perMinute(), budget.perDay(), budget.monthlyCapUsd());
    }

    // ---------- settings ----------

    /**
     * The settings of one source.
     *
     * @param view the effective view ({@link ai.foresignal.source.SourceService#view(String)})
     * @param policyOverride the stored ingest-policy override, if any
     * @param retentionOverride the stored retention override, if any
     * @param sourceRetention {@link ai.foresignal.source.SourceService#effectiveRetention(String)}
     */
    public SourceSettingsDto settings(
            SourceView view,
            Optional<IngestPolicy> policyOverride,
            Optional<Duration> retentionOverride,
            Optional<Duration> sourceRetention) {
        return settings(view, policyOverride, retentionOverride, sourceRetention, Optional.empty());
    }

    /**
     * The settings of one source, with its downsample interval override (prompt 044).
     *
     * @param view the effective view ({@link ai.foresignal.source.SourceService#view(String)})
     * @param policyOverride the stored ingest-policy override, if any
     * @param retentionOverride the stored retention override, if any
     * @param sourceRetention {@link ai.foresignal.source.SourceService#effectiveRetention(String)}
     * @param downsampleOverride {@link ai.foresignal.source.SourceService#downsampleIntervalOverride(String)}
     */
    public SourceSettingsDto settings(
            SourceView view,
            Optional<IngestPolicy> policyOverride,
            Optional<Duration> retentionOverride,
            Optional<Duration> sourceRetention,
            Optional<Duration> downsampleOverride) {
        return settings(view, policyOverride, retentionOverride, sourceRetention, downsampleOverride, Optional.empty());
    }

    /**
     * The settings of one source, with its downsample interval and egress overrides (prompts 044 and 069).
     *
     * @param egressOverride {@link ai.foresignal.source.SourceService#egressOverride(String)}
     */
    public SourceSettingsDto settings(
            SourceView view,
            Optional<IngestPolicy> policyOverride,
            Optional<Duration> retentionOverride,
            Optional<Duration> sourceRetention,
            Optional<Duration> downsampleOverride,
            Optional<ai.foresignal.source.spi.Egress> egressOverride) {
        return settings(view, policyOverride, retentionOverride, sourceRetention, downsampleOverride, egressOverride,
                Optional.empty());
    }

    /**
     * The settings of one source, with its downsample interval, egress and reliability overrides (prompts 044,
     * 069 and 109).
     *
     * @param reliabilityOverride {@link ai.foresignal.source.SourceService#reliabilityOverride(String)}
     */
    public SourceSettingsDto settings(
            SourceView view,
            Optional<IngestPolicy> policyOverride,
            Optional<Duration> retentionOverride,
            Optional<Duration> sourceRetention,
            Optional<Duration> downsampleOverride,
            Optional<ai.foresignal.source.spi.Egress> egressOverride,
            Optional<String> reliabilityOverride) {
        return new SourceSettingsDto(
                view.enabled(),
                view.interval(),
                view.params(),
                budget(view.budget()),
                retentionOverride.orElse(null),
                view.embed() && !effectivePrivate(view),
                view.credentialsSet(),
                view.updatedAt(),
                policyOverride.map(this::policy).orElse(null),
                policy(view.ingestPolicy()),
                effectiveRetention(view.descriptor(), sourceRetention),
                statsSettings().enabled(view.id()),
                statsSettings().includePrivate(view.id()),
                effectivePrivate(view),
                regionTagging(view.descriptor()),
                downsampleOverride.orElse(null),
                egressOverride.map(ai.foresignal.source.spi.Egress::wireValue).orElse(null),
                reliabilityOverride.orElse(null));
    }

    /** The source's region tagging mode (prompt 040): the operator's choice, else its tier's default. */
    private String regionTagging(SourceDescriptor descriptor) {
        try {
            ai.foresignal.reference.RegionTaggingSettings value = appSettings.getOrDefault(SettingKeys.REGION_TAGGING,
                    ai.foresignal.reference.RegionTaggingSettings.class,
                    ai.foresignal.reference.RegionTaggingSettings::defaults);
            ai.foresignal.reference.RegionTagMode mode = value == null ? null : value.override(descriptor.id());
            return (mode != null ? mode : ai.foresignal.reference.RegionTagMode.defaultFor(descriptor.historyTier()))
                    .wire();
        } catch (RuntimeException e) {
            log.warn("Could not read the region tagging settings; showing the default", e);
            return ai.foresignal.reference.RegionTagMode.defaultFor(descriptor.historyTier()).wire();
        }
    }

    /**
     * The effective privacy of {@code view} (prompt 039): the descriptor flag or the operator's tightening
     * ({@code app_setting['visibility']}).
     */
    public boolean effectivePrivate(SourceView view) {
        if (view.descriptor().privateSource()) {
            return true;
        }
        try {
            ai.foresignal.security.visibility.VisibilitySettings value = appSettings.getOrDefault(
                    SettingKeys.VISIBILITY, ai.foresignal.security.visibility.VisibilitySettings.class,
                    ai.foresignal.security.visibility.VisibilitySettings::defaults);
            return value != null && value.isPrivate(view.id());
        } catch (RuntimeException e) {
            log.warn("Could not read the visibility settings; reporting the source as private", e);
            return true;
        }
    }

    /**
     * The retention window in force.
     *
     * <p>TRACK: {@code retention.trackWindow} (3d by default; prompt 019 adds more settings).
     * STANDARD: the source's own window (override, else descriptor default), else the global
     * retention setting for the source.
     */
    public Duration effectiveRetention(SourceDescriptor descriptor, Optional<Duration> sourceRetention) {
        RetentionSettings retention = retentionSettings();
        if (descriptor.isTrack()) {
            return retention.trackWindow();
        }
        return sourceRetention.orElseGet(() -> retention.forSource(descriptor.id()));
    }

    private ai.foresignal.stats.StatsSettings statsSettings() {
        try {
            ai.foresignal.stats.StatsSettings value = appSettings.getOrDefault(SettingKeys.STATS,
                    ai.foresignal.stats.StatsSettings.class, ai.foresignal.stats.StatsSettings::defaults);
            return value == null ? ai.foresignal.stats.StatsSettings.defaults() : value;
        } catch (RuntimeException e) {
            log.warn("Could not read the stats settings; showing the defaults", e);
            return ai.foresignal.stats.StatsSettings.defaults();
        }
    }

    private RetentionSettings retentionSettings() {
        try {
            return appSettings.getOrDefault(
                    SettingKeys.RETENTION, RetentionSettings.class, RetentionSettings::defaults);
        } catch (RuntimeException e) {
            log.debug("Could not read retention settings; using the shipped defaults", e);
            return RetentionSettings.defaults();
        }
    }

    // ---------- ingest policy ----------

    /** The API form of {@code policy}; a missing keyframe becomes {@code "none"}. Null for null. */
    public IngestPolicyDto policy(IngestPolicy policy) {
        if (policy == null) {
            return null;
        }
        ObjectNode tree = codec.toTree(policy);
        return new IngestPolicyDto(
                text(tree, "mode"),
                text(tree, "minInterval"),
                text(tree, "keyframe"),
                tree.hasNonNull("minDeviationMeters") ? tree.get("minDeviationMeters").doubleValue() : null,
                tree.hasNonNull("thresholds") ? tree.get("thresholds").booleanValue() : null,
                text(tree, "edgeMinInterval"));
    }

    private static String text(ObjectNode tree, String field) {
        return tree.hasNonNull(field) ? tree.get(field).asString() : null;
    }

    /**
     * The domain policy from its API form, parsed by {@link IngestPolicyCodec}.
     *
     * <p>Shape problems (unknown mode, unparseable duration, {@code keyframe: none} on an ALL
     * policy of a source with a TTL) are reported with field paths such as
     * {@code ingestPolicy.keyframe}. The §5.1 rules themselves are checked by the service.
     *
     * @throws ValidationException {@code INGEST_POLICY_INVALID} with every problem in {@code errors}
     */
    public IngestPolicy policy(IngestPolicyDto dto, SourceDescriptor descriptor) {
        if (dto == null) {
            return null;
        }
        List<FieldError> errors = new ArrayList<>();
        String mode = dto.mode() == null ? null : dto.mode().trim().toUpperCase(Locale.ROOT);
        if (mode == null || !List.of("ALL", "SAMPLED", "LATEST").contains(mode)) {
            errors.add(new FieldError(POLICY_FIELD + ".mode", "must be one of ALL, SAMPLED or LATEST, but was " + dto.mode()));
        }
        checkDuration(errors, "minInterval", dto.minInterval(), false);
        checkDuration(errors, "keyframe", dto.keyframe(), true);
        checkDuration(errors, "edgeMinInterval", dto.edgeMinInterval(), false);
        if ("SAMPLED".equals(mode) && dto.minInterval() == null) {
            errors.add(new FieldError(POLICY_FIELD + ".minInterval", "is required for SAMPLED"));
        }
        if ("ALL".equals(mode)
                && isNone(dto.keyframe())
                && descriptor != null
                && descriptor.recordTtl() != null) {
            errors.add(
                    new FieldError(
                            POLICY_FIELD + ".keyframe",
                            "'none' is only allowed when the source has no TTL; "
                                    + descriptor.id()
                                    + " has a recordTtl of "
                                    + Durations.format(descriptor.recordTtl())));
        }
        if (!errors.isEmpty()) {
            throw invalidPolicy(errors);
        }

        ObjectNode tree = jsonMapper.createObjectNode();
        tree.put("mode", mode);
        putIfSet(tree, "minInterval", dto.minInterval());
        putIfSet(tree, "keyframe", dto.keyframe());
        putIfSet(tree, "edgeMinInterval", dto.edgeMinInterval());
        if (dto.minDeviationMeters() != null) {
            tree.put("minDeviationMeters", dto.minDeviationMeters());
        }
        if (dto.thresholds() != null) {
            tree.put("thresholds", dto.thresholds());
        }
        try {
            return codec.fromTree(tree);
        } catch (InvalidInputException | JacksonException e) {
            throw invalidPolicy(List.of(new FieldError(POLICY_FIELD, e.getMessage())));
        }
    }

    private static void putIfSet(ObjectNode tree, String field, String value) {
        if (value != null) {
            tree.put(field, value.trim());
        }
    }

    private static boolean isNone(String value) {
        return value != null && IngestPolicyCodec.KEYFRAME_NONE.equalsIgnoreCase(value.trim());
    }

    private static void checkDuration(List<FieldError> errors, String field, String value, boolean noneAllowed) {
        if (value == null || (noneAllowed && isNone(value))) {
            return;
        }
        try {
            Durations.parse(value);
        } catch (InvalidInputException e) {
            errors.add(new FieldError(POLICY_FIELD + "." + field, e.getMessage()));
        }
    }

    private static ValidationException invalidPolicy(List<FieldError> errors) {
        return new ValidationException(
                ErrorCodes.INGEST_POLICY_INVALID, "The ingest policy is not valid.", errors);
    }

    // ---------- updates ----------

    /**
     * The service update for an API body.
     *
     * <p>Every malformed value is collected, so the caller sees all of them in one 400. Ingest
     * policy shape problems are reported on their own as {@code INGEST_POLICY_INVALID}.
     *
     * @throws ValidationException {@code VALIDATION_FAILED}, or {@code INGEST_POLICY_INVALID} for a
     *     malformed policy
     */
    public SourceSettingsUpdate update(
            ai.foresignal.api.dto.SourceSettingsUpdate body, SourceDescriptor descriptor) {
        if (body == null) {
            return SourceSettingsUpdate.empty();
        }
        List<FieldError> errors = new ArrayList<>();

        Duration interval = parseDuration(errors, "interval", body.getInterval());

        Duration retention = null;
        boolean clearRetention = false;
        if (body.isRetentionPresent()) {
            if (body.getRetention() == null) {
                clearRetention = true;
            } else {
                retention = parseDuration(errors, "retention", body.getRetention());
            }
        }

        Duration downsampleInterval = null;
        boolean clearDownsampleInterval = false;
        if (body.isDownsampleIntervalPresent()) {
            if (body.getDownsampleInterval() == null) {
                clearDownsampleInterval = true;
            } else {
                downsampleInterval = parseDuration(errors, "downsampleInterval", body.getDownsampleInterval());
            }
        }

        ai.foresignal.source.spi.Egress egress = null;
        boolean clearEgress = false;
        if (body.isEgressPresent()) {
            if (body.getEgress() == null) {
                clearEgress = true;
            } else {
                try {
                    egress = ai.foresignal.source.spi.Egress.valueOf(body.getEgress().strip());
                } catch (IllegalArgumentException e) {
                    errors.add(new FieldError("egress", "must be OPEN, LOCAL_ONLY or null, but was '"
                            + body.getEgress() + "'"));
                }
            }
        }

        String reliability = null;
        boolean clearReliability = false;
        if (body.isReliabilityPresent()) {
            if (body.getReliability() == null) {
                clearReliability = true;
            } else {
                String grade = body.getReliability().strip().toUpperCase(java.util.Locale.ROOT);
                if (SourceDescriptor.RELIABILITY_GRADES.contains(grade)) {
                    reliability = grade;
                } else {
                    errors.add(new FieldError("reliability", "must be one of A, B, C, D, E, F or null, but was '"
                            + body.getReliability() + "'"));
                }
            }
        }

        Budget budget = null;
        if (body.getBudget() != null) {
            BudgetDto b = body.getBudget();
            if (b.perMinute() == null) {
                errors.add(new FieldError("budget.perMinute", "is required"));
            }
            if (b.perDay() == null) {
                errors.add(new FieldError("budget.perDay", "is required"));
            }
            if (b.perMinute() != null && b.perDay() != null) {
                Integer mbPerDay =
                        descriptor.defaultBudget() == null ? null : descriptor.defaultBudget().mbPerDay();
                if (b.monthlyCapUsd() != null && b.monthlyCapUsd().signum() < 0) {
                    errors.add(new FieldError("budget.monthlyCapUsd", "must be at least 0"));
                }
                budget = new Budget(b.perMinute(), b.perDay(), mbPerDay, b.monthlyCapUsd());
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("The settings for " + descriptor.id() + " are not valid.", errors);
        }

        IngestPolicy policy = null;
        boolean clearPolicy = false;
        if (body.isIngestPolicyPresent()) {
            if (body.getIngestPolicy() == null) {
                clearPolicy = true;
            } else {
                policy = policy(body.getIngestPolicy(), descriptor);
            }
        }

        Map<String, Object> params = null;
        if (body.getParams() != null) {
            // A JSON null value means "not set", which falls back to the parameter's default.
            params = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : body.getParams().entrySet()) {
                if (entry.getValue() != null) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return new SourceSettingsUpdate(
                body.getEnabled(), interval, params, budget, retention, clearRetention, policy, clearPolicy,
                body.getEmbed(), downsampleInterval, clearDownsampleInterval, egress, clearEgress, reliability,
                clearReliability);
    }

    /**
     * An adapter's coerced parameters in their API wire form, safe to embed in a settings bundle and
     * re-validate on import (prompt 097). A {@code BBOX} becomes {@code minLon,minLat,maxLon,maxLat}, a
     * {@code LAN_URL} or {@code INBOX_PATH} a string, and a {@code DURATION} its shorthand; every other
     * value is already JSON-native.
     */
    public static Map<String, Object> wireParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            out.put(entry.getKey(), wireValue(entry.getValue()));
        }
        return out;
    }

    private static Object wireValue(Object value) {
        if (value instanceof Envelope envelope) {
            return envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + ","
                    + envelope.getMaxY();
        }
        if (value instanceof URI uri) {
            return uri.toString();
        }
        if (value instanceof Path path) {
            return path.toString();
        }
        if (value instanceof Duration duration) {
            return Durations.format(duration);
        }
        return value;
    }

    private static Duration parseDuration(List<FieldError> errors, String field, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Durations.parse(value);
        } catch (InvalidInputException e) {
            errors.add(new FieldError(field, e.getMessage()));
            return null;
        }
    }

    private static List<String> sorted(java.util.Collection<String> values) {
        return values.stream().sorted().toList();
    }
}
