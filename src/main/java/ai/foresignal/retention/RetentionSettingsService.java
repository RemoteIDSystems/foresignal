package ai.foresignal.retention;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * {@code getRetention} / {@code updateRetention} (prompt 019), shared with the settings import.
 *
 * <p>A PUT replaces the whole object; every window must be at least 1 h. Unknown source ids and
 * unknown kinds are listed in one 400, and a {@code sources} entry for a TRACK source, or a
 * {@code kinds} entry for a kind only TRACK sources emit, makes it {@code 400 RETENTION_TIER_FIXED}
 * (still with every error).
 *
 * <p><strong>One store for per-source overrides:</strong> {@code sources} is a view over
 * {@code source_settings.retention}. A PUT writes those rows in the same transaction (clearing the
 * override of every STANDARD source it does not list) and keeps the {@code app_setting} copy empty;
 * a GET reads them back.
 *
 * <p>{@code audit} (prompt 023), {@code inbox} (prompt 033) and {@code stats.hourly}/{@code stats.daily}
 * (prompt 038) are stored with the rest. Members owned by later prompts ({@code stats.regionHourly} and
 * {@code stats.regionDaily} 119, {@code social} 135) are accepted and not stored until those prompts.
 *
 * <p><strong>Downsampling (prompt 044).</strong> {@code trackRawWindow} (default 24 h; {@code null}
 * disables downsampling) must be shorter than {@code trackWindow}, and {@code trackDownsampleInterval}
 * (default 5 min) must lie in {@code [30s, trackRawWindow]}. Both are checked against the values in
 * force after the PUT, so an omitted member is checked at its default.
 */
@Service
public class RetentionSettingsService {

    /** The shortest window accepted anywhere in the settings. */
    public static final Duration MIN_WINDOW = Duration.ofHours(1);

    static final Set<String> DURATION_FIELDS = Set.of(
            "default", "alerts", "usage", "briefings", "ask", "trackWindow", "provenance", "notifications", "audit", "inbox");

    static final Set<String> LATER_FIELDS = Set.of("social");

    /** The shortest {@code trackDownsampleInterval} accepted (prompt 044). */
    public static final Duration MIN_DOWNSAMPLE_INTERVAL = Duration.ofSeconds(30);

    /** {@code stats} members stored now (prompt 038). */
    static final Set<String> STATS_FIELDS = Set.of("hourly", "daily");

    /** {@code stats} members of prompt 119, accepted and not stored until then. */
    static final Set<String> LATER_STATS_FIELDS = Set.of("regionHourly", "regionDaily");

    private final AppSettingService settings;
    private final RetentionResolver resolver;
    private final SourceRegistry registry;
    private final SourceService sources;

    /** Creates the service. */
    public RetentionSettingsService(AppSettingService settings, RetentionResolver resolver, SourceRegistry registry,
            SourceService sources) {
        this.settings = settings;
        this.resolver = resolver;
        this.registry = registry;
        this.sources = sources;
    }

    /** The settings in force, with {@code sources} read from {@code source_settings}. */
    @Transactional(readOnly = true)
    public RetentionSettings get() {
        return resolver.settings().withSources(resolver.overrides());
    }

    /**
     * Validates and stores {@code body}.
     *
     * @throws ValidationException {@code VALIDATION_FAILED} or {@code RETENTION_TIER_FIXED}, with every
     *     error
     */
    @Transactional
    public RetentionSettings update(JsonNode body) {
        return apply(parse(body));
    }

    /**
     * Stores already-validated settings: per-source overrides into {@code source_settings} (clearing
     * the others), the rest into {@code app_setting}.
     */
    @Transactional
    public RetentionSettings apply(RetentionSettings value) {
        Map<String, Duration> desired = value.sources();
        Map<String, Duration> current = resolver.overrides();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.isTrack()) {
                continue;
            }
            String id = descriptor.id();
            Duration want = desired.get(id);
            Duration have = current.get(id);
            if (Objects.equals(want, have)) {
                continue;
            }
            sources.update(id, want == null
                    ? SourceSettingsUpdate.clearingRetention()
                    : SourceSettingsUpdate.empty().withRetention(want));
        }
        settings.put(SettingKeys.RETENTION, value.withSources(Map.of()));
        return get();
    }

    /**
     * Parses and validates a {@code RetentionSettings} body without storing anything.
     *
     * @throws ValidationException listing every problem; {@code RETENTION_TIER_FIXED} when any entry
     *     targets the TRACK tier
     */
    public RetentionSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a RetentionSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        boolean tierFixed = false;
        Map<String, Duration> values = new LinkedHashMap<>();
        for (String field : body.propertyNames()) {
            if (DURATION_FIELDS.contains(field)) {
                Duration value = window(body.get(field), field, errors);
                if (value != null) {
                    values.put(field, value);
                }
            } else if (!field.equals("sources") && !field.equals("kinds") && !field.equals("stats")
                    && !field.equals("trackRawWindow") && !field.equals("trackDownsampleInterval")
                    && !LATER_FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a RetentionSettings member"));
            }
        }
        if (!body.has("default") || body.get("default").isNull()) {
            errors.add(new FieldError("default", "is required"));
        }

        Map<String, Duration> perSource = new TreeMap<>();
        JsonNode sourcesNode = body.get("sources");
        List<String> unknownSources = new ArrayList<>();
        if (sourcesNode != null && !sourcesNode.isNull()) {
            if (!sourcesNode.isObject()) {
                errors.add(new FieldError("sources", "must be an object of source id to duration"));
            } else {
                for (String id : sourcesNode.propertyNames()) {
                    Optional<SourceDescriptor> descriptor = registry.find(id).map(r -> r.descriptor());
                    if (descriptor.isEmpty()) {
                        unknownSources.add(id);
                        continue;
                    }
                    if (descriptor.get().isTrack()) {
                        tierFixed = true;
                        errors.add(new FieldError("sources." + id, id + " is TRACK; its retention is fixed by the tier."
                                + " Change retention.trackWindow instead."));
                        continue;
                    }
                    Duration value = window(sourcesNode.get(id), "sources." + id, errors);
                    if (value != null) {
                        perSource.put(id, value);
                    }
                }
            }
        }
        if (!unknownSources.isEmpty()) {
            errors.add(new FieldError("sources", "unknown source id(s): " + String.join(", ", unknownSources)));
        }

        Map<String, Duration> perKind = new TreeMap<>();
        JsonNode kindsNode = body.get("kinds");
        List<String> unknownKinds = new ArrayList<>();
        if (kindsNode != null && !kindsNode.isNull()) {
            if (!kindsNode.isObject()) {
                errors.add(new FieldError("kinds", "must be an object of kind to duration"));
            } else {
                Set<String> standardKinds = new HashSet<>();
                Set<String> trackKinds = new HashSet<>();
                for (SourceDescriptor descriptor : registry.descriptors()) {
                    (descriptor.isTrack() ? trackKinds : standardKinds).addAll(descriptor.kinds());
                }
                for (String kind : kindsNode.propertyNames()) {
                    if (!standardKinds.contains(kind) && !trackKinds.contains(kind)) {
                        unknownKinds.add(kind);
                        continue;
                    }
                    if (!standardKinds.contains(kind)) {
                        tierFixed = true;
                        errors.add(new FieldError("kinds." + kind, "only TRACK sources emit " + kind
                                + "; their retention is fixed by the tier. Change retention.trackWindow instead."));
                        continue;
                    }
                    Duration value = window(kindsNode.get(kind), "kinds." + kind, errors);
                    if (value != null) {
                        perKind.put(kind, value);
                    }
                }
            }
        }
        if (!unknownKinds.isEmpty()) {
            errors.add(new FieldError("kinds", "unknown kind(s): " + String.join(", ", unknownKinds)));
        }

        RetentionSettings.Stats stats = stats(body.get("stats"), errors);
        Duration trackWindow = values.getOrDefault("trackWindow", RetentionSettings.DEFAULT_TRACK_WINDOW);
        Duration[] downsampling = downsampling(body, trackWindow, errors);

        if (!errors.isEmpty()) {
            String code = tierFixed ? ErrorCodes.RETENTION_TIER_FIXED : ErrorCodes.VALIDATION_FAILED;
            throw new ValidationException(code, "The retention settings are not valid.", errors);
        }
        return new RetentionSettings(
                values.get("default"),
                values.get("alerts"),
                values.get("usage"),
                perSource,
                values.get("trackWindow"),
                values.get("provenance"),
                values.get("notifications"),
                values.get("briefings"),
                values.get("ask"),
                perKind,
                values.get("audit"),
                values.get("inbox"),
                stats,
                downsampling[0],
                downsampling[1]);
    }

    /**
     * {@code trackRawWindow} and {@code trackDownsampleInterval} (prompt 044), validated against each
     * other and against {@code trackWindow}.
     *
     * @return {raw window or null for the default or {@link RetentionSettings#DOWNSAMPLING_DISABLED},
     *     interval or null for the default}
     */
    static Duration[] downsampling(JsonNode body, Duration trackWindow, List<FieldError> errors) {
        Duration raw = null;
        boolean rawValid = true;
        JsonNode rawNode = body.get("trackRawWindow");
        if (rawNode != null) {
            if (rawNode.isNull()) {
                raw = RetentionSettings.DOWNSAMPLING_DISABLED;
            } else {
                raw = positive(rawNode, "trackRawWindow", errors);
                rawValid = raw != null;
            }
        }
        Duration interval = null;
        boolean intervalValid = true;
        JsonNode intervalNode = body.get("trackDownsampleInterval");
        if (intervalNode != null && !intervalNode.isNull()) {
            interval = positive(intervalNode, "trackDownsampleInterval", errors);
            intervalValid = interval != null;
        }
        Duration effectiveRaw = raw == null ? RetentionSettings.DEFAULT_TRACK_RAW_WINDOW : raw;
        Duration effectiveInterval = interval == null ? RetentionSettings.DEFAULT_TRACK_DOWNSAMPLE_INTERVAL : interval;
        boolean enabled = !effectiveRaw.isZero();
        if (rawValid && enabled && effectiveRaw.compareTo(trackWindow) >= 0) {
            errors.add(new FieldError("trackRawWindow", "must be shorter than trackWindow ("
                    + Durations.format(trackWindow) + "), but was " + Durations.format(effectiveRaw)
                    + "; set it to null to disable downsampling"));
        }
        if (intervalValid) {
            if (effectiveInterval.compareTo(MIN_DOWNSAMPLE_INTERVAL) < 0) {
                errors.add(new FieldError("trackDownsampleInterval", "must be at least 30s"));
            } else if (rawValid && enabled && effectiveInterval.compareTo(effectiveRaw) > 0) {
                errors.add(new FieldError("trackDownsampleInterval", "must not be longer than trackRawWindow ("
                        + Durations.format(effectiveRaw) + ")"));
            }
        }
        return new Duration[] {raw, interval};
    }

    private static Duration positive(JsonNode node, String field, List<FieldError> errors) {
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a duration such as 24h or PT24H"));
            return null;
        }
        try {
            return Durations.parsePositive(node.asString());
        } catch (InvalidInputException e) {
            errors.add(new FieldError(field, e.getMessage()));
            return null;
        }
    }

    /** The {@code stats} object (prompt 038): {@code hourly} and {@code daily} windows, each at least 1 h. */
    private static RetentionSettings.Stats stats(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("stats", "must be an object with hourly and daily durations"));
            return null;
        }
        Map<String, Duration> values = new LinkedHashMap<>();
        for (String field : node.propertyNames()) {
            if (STATS_FIELDS.contains(field)) {
                Duration value = window(node.get(field), "stats." + field, errors);
                if (value != null) {
                    values.put(field, value);
                }
            } else if (LATER_STATS_FIELDS.contains(field)) {
                window(node.get(field), "stats." + field, errors);
            } else {
                errors.add(new FieldError("stats." + field, "is not a RetentionSettings.stats member"));
            }
        }
        return new RetentionSettings.Stats(values.get("hourly"), values.get("daily"));
    }

    private static Duration window(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a duration such as 7d or P7D"));
            return null;
        }
        try {
            Duration value = Durations.parse(node.asString());
            if (value.compareTo(MIN_WINDOW) < 0) {
                errors.add(new FieldError(field, "must be at least 1h"));
                return null;
            }
            return value;
        } catch (InvalidInputException e) {
            errors.add(new FieldError(field, e.getMessage()));
            return null;
        }
    }
}
