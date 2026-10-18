package ai.foresignal.chronicle;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.situation.SituationEvent;
import ai.foresignal.situation.SituationEventBus;
import ai.foresignal.situation.SituationListener;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertView;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes the thread items that do not come from captured versions (prompt 131): alerts of {@code alerts} layers (from
 * the 017 {@link AlertBus}), changes of linked situations (from the 118 {@link SituationEventBus}), band changes of
 * linked hypothesis sets ({@code hypothesis_log}) and resolutions of ledger questions about linked situations or sets
 * ({@code forecast_question}). Each item keeps a JSON snapshot, so the thread survives the retention of the object's
 * owner. Only active chronicles receive items. The poll positions are the app setting
 * {@code chronicle.threadCursors}; the first poll starts from the present.
 */
@Component
public class ChronicleSignals implements AlertListener, SituationListener, DisposableBean, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ChronicleSignals.class);

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ChronicleCaptureService capture;
    private final ChronicleThreadService thread;
    private final AlertBus alerts;
    private final SituationEventBus situations;
    private final AppSettingService settings;
    private final SourceVisibility visibility;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final ChronicleProperties properties;
    private final List<Subscription> subscriptions = new ArrayList<>();

    /** Creates the component. */
    public ChronicleSignals(ChronicleCaptureService capture, ChronicleThreadService thread, AlertBus alerts,
            SituationEventBus situations, AppSettingService settings, SourceVisibility visibility, JdbcTemplate jdbc,
            JsonMapper json, Clock clock, ChronicleProperties properties) {
        this.capture = capture;
        this.thread = thread;
        this.alerts = alerts;
        this.situations = situations;
        this.settings = settings;
        this.visibility = visibility;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
        this.properties = properties;
    }

    /** Subscribes to the alert and situation buses once the application is ready (unless disabled). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.CHRONICLES)
    public synchronized void start() {
        if (!properties.autoStart() || !subscriptions.isEmpty()) {
            return;
        }
        subscriptions.add(alerts.subscribe("chronicle-thread", this, AlertBus.DEFAULT_QUEUE));
        subscriptions.add(situations.subscribe("chronicle-thread", this, SituationEventBus.DEFAULT_QUEUE));
    }

    /** Closes the subscriptions. */
    @Override
    public synchronized void destroy() {
        subscriptions.forEach(Subscription::close);
        subscriptions.clear();
    }

    /** Nothing cached besides the poll positions, which live in the app settings. */
    @Override
    public void resetState() {
        // The cleaner truncates app_setting; the next poll starts from the present again.
    }

    /**
     * Either bus dropped events for this lagging listener. Thread items are notable changes, not a ledger: the dropped
     * ones are not replayed, and the next events are handled as usual.
     */
    @Override
    public void onGap() {
        log.warn("Chronicle thread listener lagged; some alert or situation items were skipped");
    }

    // ------------------------------------------------------------------------- alerts

    @Override
    public void onAlert(AlertEvent event) {
        if (event == null || !AlertEvent.FIRED.equals(event.type()) || event.alert() == null) {
            return;
        }
        try {
            alert(event.alert());
        } catch (RuntimeException e) {
            log.warn("Chronicle alert item for alert {} failed: {}", event.alert().id(), e.toString());
        }
    }

    /**
     * Writes an {@code alert} item to every active chronicle with an {@code alerts} layer that takes the alert (its rule
     * id or one of its tags; a layer naming neither takes every alert).
     *
     * @return the items written
     */
    public int alert(AlertView a) {
        int written = 0;
        for (ChronicleIndex.Entry e : capture.index().entries()) {
            for (CompiledLayer layer : e.layers()) {
                if (!(layer.selector() instanceof ChronicleSelector.Alerts sel) || !takes(sel, a)) {
                    continue;
                }
                ChronicleEventEntity item = thread.item(e.id(), ChronicleVocabulary.ITEM_ALERT, a.firedAt(),
                        "Alert: " + (a.message() == null || a.message().isBlank() ? a.ruleName() : a.message()));
                item.setSeq(a.seq());
                item.setLayerId(layer.layerId());
                item.setRef("alert:" + a.id());
                item.setSummary(a.ruleName() + " (" + a.severity() + ")");
                item.setSnapshot(json.convertValue(a, MAP));
                item.setPrivateItem(isPrivate(a.sourceId()));
                thread.append(item);
                written++;
                break;
            }
        }
        return written;
    }

    /** True when an {@code alerts} selector takes the alert. */
    static boolean takes(ChronicleSelector.Alerts sel, AlertView a) {
        if (sel.ruleIds().isEmpty() && sel.tags().isEmpty()) {
            return true;
        }
        if (a.ruleId() != null && sel.ruleIds().contains(a.ruleId().toString())) {
            return true;
        }
        if (a.tags() != null) {
            for (String tag : a.tags()) {
                if (sel.tags().contains(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------- situations

    @Override
    public void onEvent(SituationEvent event) {
        if (event == null || SituationEvent.UPDATED.equals(event.event())
                || SituationEvent.CREATED.equals(event.event())) {
            return;
        }
        try {
            situation(event);
        } catch (RuntimeException e) {
            log.warn("Chronicle situation item for {} failed: {}", event.situationId(), e.toString());
        }
    }

    /**
     * Writes a {@code situation-change} item to every active chronicle linked to the situation (its
     * {@code situationIds} or a {@code situations} layer).
     *
     * @return the items written
     */
    public int situation(SituationEvent event) {
        int written = 0;
        for (ChronicleIndex.Entry e : capture.index().entries()) {
            if (!linksSituation(e, event.situationId())) {
                continue;
            }
            String title = event.situation() == null ? event.situationId()
                    : String.valueOf(event.situation().getOrDefault("title", event.situationId()));
            String change = event.event().startsWith("situation.") ? event.event().substring("situation.".length())
                    : event.event();
            ChronicleEventEntity item = thread.item(e.id(), ChronicleVocabulary.ITEM_SITUATION_CHANGE,
                    event.at() == null ? clock.instant() : event.at(), "Situation " + change + ": " + title);
            item.setRef("situation:" + event.situationId());
            item.setSummary(event.previousStatus() == null ? null : "was " + event.previousStatus()
                    + (event.previousSeverity() == null ? "" : ", " + event.previousSeverity()));
            item.setSnapshot(event.payload());
            item.setPrivateItem(event.privateSituation());
            thread.append(item);
            written++;
        }
        return written;
    }

    private static boolean linksSituation(ChronicleIndex.Entry e, String situationId) {
        if (e.chronicle().getSituationIds().contains(situationId)) {
            return true;
        }
        for (CompiledLayer l : e.layers()) {
            if (l.selector() instanceof ChronicleSelector.Situations s && s.situationIds().contains(situationId)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------- polls

    /** Polls band changes and resolutions every 30 seconds (when capture starts on its own). */
    @Scheduled(fixedDelayString = "${foresignal.chronicles.signal-poll:30s}", initialDelayString = "60s")
    public void scheduledPoll() {
        if (properties.autoStart()) {
            try {
                poll();
            } catch (RuntimeException e) {
                log.warn("Chronicle signal poll failed: {}", e.toString());
            }
        }
    }

    /**
     * Writes {@code hypothesis-band-change} items for band changes of linked sets and {@code forecast-resolution}
     * items for resolved questions about linked situations or sets, since the last poll.
     *
     * @return the items written
     */
    public synchronized int poll() {
        Map<String, Object> cursors = new LinkedHashMap<>(settings.get(SettingKeys.CHRONICLE_THREAD_CURSORS, Map.class)
                .map(m -> {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    ((Map<?, ?>) m).forEach((k, v) -> copy.put(String.valueOf(k), v));
                    return copy;
                }).orElseGet(LinkedHashMap::new));
        Instant now = clock.instant();
        boolean first = cursors.isEmpty();
        long logId = cursors.get("hypothesisLogId") instanceof Number n ? n.longValue() : -1;
        Instant resolvedAt = cursors.get("forecastResolvedAt") instanceof String s ? Instant.parse(s) : null;
        if (first || logId < 0) {
            Long max = jdbc.queryForObject("SELECT coalesce(max(id), 0) FROM hypothesis_log", Long.class);
            logId = max == null ? 0 : max;
        }
        if (first || resolvedAt == null) {
            resolvedAt = now;
        }
        int written = 0;
        ChronicleIndex idx = capture.index();
        if (!first && !idx.isEmpty()) {
            written += bandChanges(idx, logId);
            written += resolutions(idx, resolvedAt);
        }
        Long newLog = jdbc.queryForObject("SELECT coalesce(max(id), 0) FROM hypothesis_log", Long.class);
        cursors.put("hypothesisLogId", newLog == null ? logId : Math.max(logId, newLog));
        cursors.put("forecastResolvedAt", now.toString());
        settings.put(SettingKeys.CHRONICLE_THREAD_CURSORS, cursors);
        return written;
    }

    private int bandChanges(ChronicleIndex idx, long afterId) {
        int written = 0;
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, set_id, payload::text AS payload, private, at"
                + " FROM hypothesis_log WHERE id > ? AND event = 'set.band_changed' ORDER BY id LIMIT 1000", afterId);
        for (Map<String, Object> row : rows) {
            String setId = String.valueOf(row.get("set_id"));
            Map<String, Object> payload = parse(row.get("payload"));
            Instant at = row.get("at") instanceof Timestamp t ? t.toInstant() : clock.instant();
            for (ChronicleIndex.Entry e : idx.entries()) {
                if (!e.chronicle().getHypothesisSetIds().contains(setId)) {
                    continue;
                }
                ChronicleEventEntity item = thread.item(e.id(), ChronicleVocabulary.ITEM_HYPOTHESIS_BAND_CHANGE, at,
                        "Hypothesis band change: " + payload.getOrDefault("question", setId));
                item.setRef("hypothesis-set:" + setId);
                item.setSnapshot(payload);
                item.setPrivateItem(Boolean.TRUE.equals(row.get("private")));
                thread.append(item);
                written++;
            }
        }
        return written;
    }

    private int resolutions(ChronicleIndex idx, Instant since) {
        int written = 0;
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, title, situation_id, hypothesis_set_id,"
                + " resolution::text AS resolution, private, updated_at FROM forecast_question WHERE status = 'resolved'"
                + " AND updated_at > ? ORDER BY updated_at, id LIMIT 1000", Timestamp.from(since));
        for (Map<String, Object> row : rows) {
            Set<String> links = new HashSet<>();
            if (row.get("situation_id") != null) {
                links.add("s:" + row.get("situation_id"));
            }
            if (row.get("hypothesis_set_id") != null) {
                links.add("h:" + row.get("hypothesis_set_id"));
            }
            if (links.isEmpty()) {
                continue;
            }
            Instant at = row.get("updated_at") instanceof Timestamp t ? t.toInstant() : clock.instant();
            for (ChronicleIndex.Entry e : idx.entries()) {
                boolean linked = false;
                for (String s : e.chronicle().getSituationIds()) {
                    linked |= links.contains("s:" + s);
                }
                for (String h : e.chronicle().getHypothesisSetIds()) {
                    linked |= links.contains("h:" + h);
                }
                if (!linked) {
                    continue;
                }
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("questionId", row.get("id"));
                snapshot.put("title", row.get("title"));
                snapshot.put("resolution", parse(row.get("resolution")));
                ChronicleEventEntity item = thread.item(e.id(), ChronicleVocabulary.ITEM_FORECAST_RESOLUTION, at,
                        "Question resolved: " + row.get("title"));
                item.setRef("question:" + row.get("id"));
                item.setSnapshot(snapshot);
                item.setPrivateItem(Boolean.TRUE.equals(row.get("private")));
                thread.append(item);
                written++;
            }
        }
        return written;
    }

    private Map<String, Object> parse(Object text) {
        if (text == null) {
            return new LinkedHashMap<>();
        }
        try {
            return json.readValue(text.toString(), MAP);
        } catch (RuntimeException e) {
            return new LinkedHashMap<>();
        }
    }

    private boolean isPrivate(String sourceId) {
        try {
            return sourceId != null && visibility.isPrivate(sourceId);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
