package ai.foresignal.stream.sse;

import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertState;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code alerts} topic (prompt 017): alert lifecycle events on the FIFO lane, with the alert's
 * {@code rev} as the cursor (key {@code a} in the composite id). It serves
 * {@code GET /v1/alerts/stream} (single-cursor mode) and the {@code alerts} topic of
 * {@code /v1/events/stream}.
 *
 * <p>Events: {@code alert} (fired), {@code alert.updated} and {@code alert.resolved}, each with an
 * {@code AlertEvent}, and {@code alert.enriched} (prompt 030) with the full {@code Alert}: an update
 * of the same alert carrying its summary, whose id is the alert's new {@code rev} like every other
 * event, so resuming is unchanged. A replay sends the alert once in its current state, summary
 * included. Live events come from one {@link AlertBus} subscription shared by every client.
 * A resume replays the alerts with {@code rev > cursor} in their <em>current</em> state, so a client
 * upserts by alert id; an alert changed several times while the client was away is replayed once.
 *
 * <p>Filters ({@link AlertQuery}): {@code minSeverity}, {@code ruleId}, {@code source},
 * {@code kind} and, on {@code /v1/alerts/stream}, {@code includeSuppressed} (default false).
 * Anonymous callers never see alerts of private sources.
 */
@Component
public class AlertsTopic implements EventTopic<AlertQuery>, AlertListener {

    /** The topic name. */
    public static final String NAME = "alerts";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "a";

    /** Alerts per replay page. */
    static final int REPLAY_PAGE = 500;

    /** The PATCH block fields ({@code EventStreamAlertsFilter}). */
    static final Set<String> JSON_FIELDS = Set.of("minSeverity", "ruleId", "source", "kind", "includeSuppressed");

    private final AlertStore store;
    private final AlertBus bus;
    private final AlertMapper mapper;
    private final SourceRegistry registry;
    private volatile SseHub hub;

    /** Creates the topic. */
    public AlertsTopic(AlertStore store, AlertBus bus, AlertMapper mapper, SourceRegistry registry) {
        this.store = store;
        this.bus = bus;
        this.mapper = mapper;
        this.registry = registry;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String cursorKey() {
        return CURSOR_KEY;
    }

    @Override
    public int cursorOrder() {
        return 10;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<AlertQuery> filterType() {
        return AlertQuery.class;
    }

    // ------------------------------------------------------------------------- filters

    @Override
    public AlertQuery parseQuery(TopicParams params, StreamPrincipal principal) {
        String severity = params.single("minSeverity");
        Severity min = severity == null ? null : severity(params.field("minSeverity"), severity);
        Set<UUID> rules = uuids(params.field("ruleId"), params.multi("ruleId"));
        List<String> sources = params.multi("source");
        checkSources(sources);
        boolean suppressed = QueryParams.bool(params.field("includeSuppressed"), params.single("includeSuppressed"), false);
        return new AlertQuery(rules, sources, params.multi("kind"), null, null, null, null, min, null, suppressed,
                excludedSources(principal), principal.restriction());
    }

    @Override
    public AlertQuery parseJson(JsonNode block, StreamPrincipal principal) {
        String prefix = NAME + ".";
        if (block == null || block.isNull()) {
            return new AlertQuery(null, null, null, null, null, null, null, null, null, false, excludedSources(principal),
                    principal.restriction());
        }
        if (!block.isObject()) {
            throw ValidationException.forField(NAME, "must be an object");
        }
        for (String field : block.propertyNames()) {
            if (!JSON_FIELDS.contains(field)) {
                throw ValidationException.forField(prefix + field, "is not an alerts filter field");
            }
        }
        Severity min = null;
        JsonNode severity = block.get("minSeverity");
        if (severity != null && !severity.isNull()) {
            min = severity(prefix + "minSeverity", severity.isString() ? severity.asString() : "");
        }
        Set<UUID> rules = uuids(prefix + "ruleId", strings(block, "ruleId", prefix));
        List<String> sources = strings(block, "source", prefix);
        checkSources(sources);
        JsonNode suppressed = block.get("includeSuppressed");
        if (suppressed != null && !suppressed.isNull() && !suppressed.isBoolean()) {
            throw ValidationException.forField(prefix + "includeSuppressed", "must be true or false");
        }
        return new AlertQuery(rules, sources, strings(block, "kind", prefix), null, null, null, null, min, null,
                suppressed != null && suppressed.asBoolean(false), excludedSources(principal), principal.restriction());
    }

    private void checkSources(List<String> sources) {
        for (String id : sources) {
            registry.descriptor(id);
        }
    }

    /** Private sources the caller may not see: all of them unless it holds an agent or admin token. */
    Set<String> excludedSources(StreamPrincipal principal) {
        // Private sources: agent and admin tokens only (SourceVisibilityGate, prompt 023).
        return ai.foresignal.api.support.ReadCallerResolver.withRestriction(registry,
                SourceVisibilityGate.excludedSources(registry, principal.scope(), TokenRestrictions.NONE),
                principal.restriction());
    }

    // ------------------------------------------------------------------------- resume

    @Override
    public long head() {
        return store.maxRev();
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, AlertQuery filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<AlertView> page = store.afterRev(filter, cursor, REPLAY_PAGE);
            for (AlertView alert : page) {
                if (!sink.emit(event(replayed(alert)))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = alert.rev();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    /** The event a replayed alert is sent as, from its current state. */
    static AlertEvent replayed(AlertView alert) {
        String type;
        if (AlertState.RESOLVED.wire().equals(alert.state())) {
            type = AlertEvent.RESOLVED;
        } else if (AlertState.ACKNOWLEDGED.wire().equals(alert.state())) {
            type = AlertEvent.ACKNOWLEDGED;
        } else if (alert.snoozedUntil() != null) {
            type = AlertEvent.SNOOZED;
        } else if (alert.feedback() != null || alert.note() != null) {
            type = AlertEvent.UPDATED;
        } else {
            type = AlertEvent.FIRED;
        }
        return AlertEvent.of(type, alert);
    }

    /** The frame of {@code event}: an {@code AlertEvent}, or the full {@code Alert} for {@code alert.enriched}. */
    FifoEvent event(AlertEvent event) {
        Map<String, Object> data = AlertEvent.ENRICHED.equals(event.type())
                ? mapper.alert(event.alert(), null) : mapper.event(event);
        return new FifoEvent(NAME, event.alert().rev(), event.sseEvent(), data, null);
    }

    // ------------------------------------------------------------------------- live

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        Subscription subscription = bus.subscribe("sse-alerts", this, AlertBus.DEFAULT_QUEUE);
        return new Subscription() {
            @Override
            public String name() {
                return "sse-alerts";
            }

            @Override
            public void close() {
                subscription.close();
                hub = null;
            }
        };
    }

    @Override
    public void onAlert(AlertEvent event) {
        SseHub target = hub;
        if (target == null) {
            return;
        }
        target.publish(NAME, filter -> ((AlertQuery) filter).matches(event.alert()) ? event(event) : null);
    }

    @Override
    public void onGap() {
        SseHub target = hub;
        if (target != null) {
            target.gap(NAME);
        }
    }

    // ------------------------------------------------------------------------- helpers

    private static Severity severity(String field, String value) {
        try {
            return Severity.fromWire(value);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField(field, e.getMessage());
        }
    }

    private static Set<UUID> uuids(String field, List<String> values) {
        Set<UUID> ids = new LinkedHashSet<>();
        for (String value : values) {
            try {
                ids.add(UUID.fromString(value));
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField(field, "'" + value + "' is not a rule id");
            }
        }
        return ids;
    }

    private static List<String> strings(JsonNode block, String field, String prefix) {
        JsonNode node = block.get(field);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw ValidationException.forField(prefix + field, "must be an array of strings");
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField(prefix + field, "must be an array of non-empty strings");
            }
            values.add(item.asString().strip());
        }
        return QueryParams.multi(prefix + field, values);
    }
}
