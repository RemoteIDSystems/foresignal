package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceHealthSummary;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.SupersededSources;
import ai.foresignal.system.OperationalAlertService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The first-party aggregator surface (prompt R2-019): {@code listAggregators}, {@code getAggregator},
 * {@code updateAggregator} and {@code testAggregator} for every domain, dispatched to its {@link DomainAggregator}.
 *
 * <p><strong>Supersede, never remove.</strong> When an update leaves an aggregator live (enabled with an endpoint)
 * and {@code disableSupersededSources} is true, every superseded source that is on is turned off through
 * {@link SourceService} and remembered in {@code disabledByAggregator}; while it stays live, seeding and
 * {@code enable-core}/{@code enable-free} keep them off ({@link SupersededSources}). When it stops being live, or
 * {@code disableSupersededSources} is set to false, exactly those sources are turned back on. The sources stay
 * registered throughout, and an operator may turn any of them on by hand.
 *
 * <p><strong>Status</strong> comes from the health of the first of the domain's sources that is enabled
 * ({@link #statusSource}): {@code ok} when healthy, {@code degraded} when degraded or not yet run, {@code down} when
 * failing; {@code unconfigured} when the aggregator is not live. A live aggregator any of whose sources becomes
 * {@code failing} raises {@code system.aggregator_down} once, and {@code system.aggregator_recovered} when that source
 * is healthy again and no other enabled one is failing (prompt R2-021).
 */
@Service
public class AggregatorService implements SupersededSources {

    /** {@code getAggregator} and friends for an unknown domain. */
    public static final String UNKNOWN_AGGREGATOR = "UNKNOWN_AGGREGATOR";

    /** The domains, in listing order, with their names. */
    public static final Map<String, String> DOMAINS = domains();

    /** The members of {@code AggregatorConfig}, plus the read-only ones a client may echo back. */
    static final Set<String> MEMBERS = Set.of("enabled", "mode", "endpoint", "config", "disableSupersededSources",
            "domain", "name", "status", "statusReason", "supersededSources", "sourceIds", "lastSeenAt",
            "implemented");

    private static final Logger log = LoggerFactory.getLogger(AggregatorService.class);

    private final Map<String, DomainAggregator> aggregators = new LinkedHashMap<>();
    private final AppSettingService settings;
    private final SourceService sources;
    private final ObjectProvider<OperationalAlertService> alerts;
    private final JsonMapper json = JsonMapper.builder().build();
    private final Clock clock;

    /** Creates the service; a domain without a {@link DomainAggregator} bean is served as pending. */
    public AggregatorService(List<DomainAggregator> beans, AppSettingService settings, SourceService sources,
            ObjectProvider<OperationalAlertService> alerts, Clock clock) {
        this.settings = settings;
        this.sources = sources;
        this.alerts = alerts;
        this.clock = clock;
        Map<String, DomainAggregator> byDomain = new LinkedHashMap<>();
        for (DomainAggregator bean : beans) {
            if (!DOMAINS.containsKey(bean.domain())) {
                throw new IllegalStateException("Unknown aggregator domain " + bean.domain());
            }
            if (byDomain.put(bean.domain(), bean) != null) {
                throw new IllegalStateException("Two aggregators for the domain " + bean.domain());
            }
        }
        DOMAINS.forEach((domain, name) -> aggregators.put(domain,
                byDomain.getOrDefault(domain, new PendingDomainAggregator(domain, name))));
    }

    private static Map<String, String> domains() {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("adsb", "Local ADS-B aggregator");
        out.put("ais", "Local AIS aggregator");
        out.put("datalink", "Local aircraft datalink aggregator");
        out.put("satellite", "Direct satellite reception");
        out.put("ground-sensors", "Local ground sensors");
        return java.util.Collections.unmodifiableMap(out);
    }

    /** The setting key of {@code domain}. */
    public static String key(String domain) {
        return "aggregator." + domain;
    }

    /** The stored configuration of {@code domain}, or the default (off). */
    public AggregatorSettings settings(String domain) {
        return settings.getOrDefault(key(domain), AggregatorSettings.class, AggregatorSettings::defaults);
    }

    /** {@code listAggregators}: every domain's {@code Aggregator}. */
    public List<Map<String, Object>> list() {
        return aggregators.keySet().stream().map(this::view).toList();
    }

    /**
     * {@code getAggregator}.
     *
     * @throws NotFoundException {@code UNKNOWN_AGGREGATOR}
     */
    public Map<String, Object> get(String domain) {
        aggregator(domain);
        return view(domain);
    }

    /**
     * {@code updateAggregator}: replaces the configuration with an {@code AggregatorConfig}, configures the domain's
     * sources, and supersedes or restores the public sources.
     *
     * @throws NotFoundException {@code UNKNOWN_AGGREGATOR}
     * @throws ValidationException listing every problem; nothing is changed
     */
    @Transactional
    public Map<String, Object> update(String domain, JsonNode body) {
        DomainAggregator aggregator = aggregator(domain);
        AggregatorSettings previous = settings(domain);
        AggregatorSettings proposed = parse(body);
        List<FieldError> errors = new ArrayList<>();
        aggregator.validate(proposed, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The " + aggregator.name() + " configuration is not valid.", errors);
        }
        aggregator.apply(proposed);

        List<String> disabled = new ArrayList<>();
        if (proposed.live() && proposed.disableSupersededSources()) {
            Set<String> off = new LinkedHashSet<>(previous.disabledByAggregator());
            for (String id : aggregator.supersededSourceIds(proposed)) {
                SourceView view = sources.view(id);
                if (view.enabled()) {
                    sources.update(id, SourceSettingsUpdate.enabled(false));
                    off.add(id);
                }
            }
            disabled.addAll(off);
            if (!disabled.isEmpty()) {
                log.info("{} is live: turned off the superseded sources {}", aggregator.name(), disabled);
            }
        } else {
            restore(previous.disabledByAggregator());
        }
        settings.put(key(domain), proposed.withDisabledByAggregator(disabled));
        if (!proposed.live()) {
            report(aggregator, "ok", null);
        }
        return view(domain);
    }

    /** Turns back on the sources an aggregator turned off, except those an operator has since turned on. */
    private void restore(List<String> ids) {
        for (String id : ids) {
            try {
                if (!sources.view(id).enabled()) {
                    sources.update(id, SourceSettingsUpdate.enabled(true));
                }
            } catch (NotFoundException | ValidationException | ConflictException e) {
                // Unregistered since, or its stored settings need the operator first: it stays off.
                log.info("Not turning {} back on: {}", id, e.getMessage());
            }
        }
        if (!ids.isEmpty()) {
            log.info("Turned the formerly superseded sources {} back on", ids);
        }
    }

    /**
     * {@code testAggregator}: probes the stored endpoint; nothing is persisted.
     *
     * @throws NotFoundException {@code UNKNOWN_AGGREGATOR}
     */
    public Map<String, Object> test(String domain) {
        DomainAggregator aggregator = aggregator(domain);
        Instant started = clock.instant();
        Map<String, Object> out = new LinkedHashMap<>(aggregator.test(settings(domain)));
        out.put("checkedAt", started);
        return out;
    }

    @Override
    public boolean isSuperseded(String sourceId) {
        for (DomainAggregator aggregator : aggregators.values()) {
            if (!aggregator.implemented()) {
                continue;
            }
            AggregatorSettings current = settings(aggregator.domain());
            if (current.live() && current.disableSupersededSources()
                    && aggregator.supersededSourceIds(current).contains(sourceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Raises or clears {@code system.aggregator_down} when one of a live aggregator's sources changes health. It is
     * raised when any of them starts failing, and cleared when one turns healthy and none of its other enabled sources
     * is failing (prompt R2-021: a domain such as {@code datalink} reads through more than one source).
     */
    @EventListener
    public void onSourceHealthChanged(SourceHealthChangedEvent event) {
        for (DomainAggregator aggregator : aggregators.values()) {
            if (!aggregator.sourceIds().contains(event.sourceId())) {
                continue;
            }
            if (!settings(aggregator.domain()).live()) {
                return;
            }
            if (event.current() == HealthStatus.FAILING) {
                report(aggregator, "warn", event.reason());
            } else if (event.current() == HealthStatus.HEALTHY && !otherFailing(aggregator, event.sourceId())) {
                report(aggregator, "ok", null);
            }
        }
    }

    /** Whether an enabled source of {@code aggregator} other than {@code sourceId} is failing. */
    private boolean otherFailing(DomainAggregator aggregator, String sourceId) {
        for (String id : aggregator.sourceIds()) {
            if (id.equals(sourceId)) {
                continue;
            }
            SourceView view = sources.view(id);
            if (view.enabled() && view.health().status() == HealthStatus.FAILING) {
                return true;
            }
        }
        return false;
    }

    /**
     * The source whose health is the aggregator's {@code status}: the first of its sources that is enabled, else the
     * first (prompt R2-021: a domain reading through several sources may use only some of them).
     */
    String statusSource(DomainAggregator aggregator) {
        for (String id : aggregator.sourceIds()) {
            if (sources.view(id).enabled()) {
                return id;
            }
        }
        return aggregator.sourceIds().getFirst();
    }

    private void report(DomainAggregator aggregator, String level, String reason) {
        OperationalAlertService path = alerts.getIfAvailable();
        if (path == null) {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("domain", aggregator.domain());
        data.put("sourceIds", aggregator.sourceIds());
        String link = "/v1/aggregators/" + aggregator.domain();
        path.report("aggregator:" + aggregator.domain(), level, (now, previous) -> "ok".equals(now)
                ? new OperationalAlertService.Alert("system.aggregator_recovered", "info",
                        aggregator.name() + " is working again",
                        aggregator.name() + " is reading its endpoint again, or was turned off.", link, data)
                : new OperationalAlertService.Alert("system.aggregator_down", "warning",
                        aggregator.name() + " is down",
                        aggregator.name() + " cannot read its endpoint: " + (reason == null ? "its source is failing"
                                : reason) + ". The superseded public sources stay off; turn the aggregator off to"
                                + " turn them back on.", link, data));
    }

    private DomainAggregator aggregator(String domain) {
        DomainAggregator aggregator = domain == null ? null : aggregators.get(domain);
        if (aggregator == null) {
            throw new NotFoundException(UNKNOWN_AGGREGATOR, "No aggregator has the domain " + domain + ".");
        }
        return aggregator;
    }

    /** The {@code Aggregator} view of {@code domain}. */
    Map<String, Object> view(String domain) {
        DomainAggregator aggregator = aggregators.get(domain);
        AggregatorSettings current = settings(domain);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("domain", domain);
        out.put("name", aggregator.name());
        out.put("enabled", current.enabled());
        out.put("mode", current.mode());
        out.put("endpoint", current.endpoint());
        out.put("config", current.config());
        out.put("disableSupersededSources", current.disableSupersededSources());
        out.put("implemented", aggregator.implemented());
        String status = "unconfigured";
        String reason = aggregator.implemented() ? "not enabled with an endpoint"
                : "this server has no " + aggregator.name() + " backend yet";
        Instant lastSeen = null;
        if (!aggregator.sourceIds().isEmpty()) {
            SourceHealthSummary health = sources.view(statusSource(aggregator)).health();
            lastSeen = health.lastDataAt() != null ? health.lastDataAt() : health.lastSuccessAt();
            if (current.live()) {
                status = switch (health.status()) {
                    case HEALTHY -> "ok";
                    case FAILING, DISABLED -> "down";
                    case DEGRADED, IDLE -> "degraded";
                };
                reason = health.statusReason();
            }
        }
        out.put("status", status);
        out.put("statusReason", reason);
        out.put("sourceIds", aggregator.sourceIds());
        out.put("supersededSources", aggregator.supersededSourceIds(current));
        out.put("lastSeenAt", lastSeen);
        return out;
    }

    private AggregatorSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an AggregatorConfig object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!MEMBERS.contains(name)) {
                errors.add(new FieldError(name, "is not a member of AggregatorConfig"));
            }
        }
        boolean enabled = false;
        JsonNode enabledNode = body.get("enabled");
        if (enabledNode == null || enabledNode.isNull()) {
            errors.add(new FieldError("enabled", "is required"));
        } else if (!enabledNode.isBoolean()) {
            errors.add(new FieldError("enabled", "must be a boolean"));
        } else {
            enabled = enabledNode.asBoolean();
        }
        String mode = null;
        JsonNode modeNode = body.get("mode");
        if (modeNode != null && !modeNode.isNull()) {
            if (!modeNode.isString() || !AggregatorSettings.MODES.contains(modeNode.asString())) {
                errors.add(new FieldError("mode", "must be one of " + String.join(", ", AggregatorSettings.MODES)));
            } else {
                mode = modeNode.asString();
            }
        }
        String endpoint = null;
        JsonNode endpointNode = body.get("endpoint");
        if (endpointNode != null && !endpointNode.isNull()) {
            if (!endpointNode.isString()) {
                errors.add(new FieldError("endpoint", "must be a string"));
            } else if (!endpointNode.asString().isBlank()) {
                endpoint = endpointNode.asString().strip();
            }
        }
        Map<String, Object> config = Map.of();
        JsonNode configNode = body.get("config");
        if (configNode != null && !configNode.isNull()) {
            if (!configNode.isObject()) {
                errors.add(new FieldError("config", "must be an object"));
            } else {
                Map<String, Object> read = new LinkedHashMap<>();
                for (String name : configNode.propertyNames()) {
                    JsonNode value = configNode.get(name);
                    if (!value.isNull()) {
                        read.put(name, json.treeToValue(value, Object.class));
                    }
                }
                config = read;
            }
        }
        boolean disable = true;
        JsonNode disableNode = body.get("disableSupersededSources");
        if (disableNode != null && !disableNode.isNull()) {
            if (!disableNode.isBoolean()) {
                errors.add(new FieldError("disableSupersededSources", "must be a boolean"));
            } else {
                disable = disableNode.asBoolean();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The aggregator configuration is not valid.", errors);
        }
        return new AggregatorSettings(enabled, mode, endpoint, config, disable, List.of());
    }
}
