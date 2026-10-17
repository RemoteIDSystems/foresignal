package ai.foresignal.cap;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.feed.FeedSanitizer;
import ai.foresignal.place.PlaceEntity;
import ai.foresignal.place.PlaceRepository;
import ai.foresignal.provenance.CredibilityService;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertView;
import jakarta.annotation.PreDestroy;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Issues one CAP 1.2 message per alert state change (prompt 112; 017 lifecycle) and stores it in
 * {@code cap_message}. Subscribed to the {@link AlertBus}, so it runs after the alert's transaction committed,
 * on its own thread, one event at a time (the message numbers of an alert are therefore consecutive).
 *
 * <ul>
 *   <li>{@code fired} → {@code msgType Alert}; {@code acknowledged}, {@code snoozed}, {@code updated},
 *       {@code enriched} → {@code Update}; {@code resolved} → {@code Update} with {@code responseType AllClear}.
 *       {@code Cancel} is never issued (CAP reserves it for erroneous issuance). An alert whose first message is
 *       missing (it fired before CAP output existed) gets {@code Alert} for its first stored message.
 *   <li>{@code references} lists every earlier message of the alert ({@code sender,identifier,sent}).
 *   <li>Suppressed alerts are not delivered anywhere, so they get no CAP message.
 *   <li>{@code status} is {@code Exercise} when the alert's context carries {@code "exercise": true} (the flag
 *       prompt 115 sets), else {@code Actual}.
 *   <li>For an alert on an official CAP source (the {@code cap-*} and {@code meteoalarm-*} families,
 *       {@code tsunami-warnings}, {@code nws-alerts}), {@code <web>} is the upstream message's link and the
 *       description names the upstream sender, so the message never reads as if Foresignal issued the warning.
 * </ul>
 */
@Service
public class CapService implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(CapService.class);

    /** The headline limit of CAP 1.2. */
    public static final int HEADLINE_MAX = 160;

    /** Sources whose records are official CAP messages from an upstream authority (067, 089, 047). */
    static final Pattern OFFICIAL_CAP_SOURCE = Pattern.compile("^(cap-.+|meteoalarm-.+|tsunami-warnings|nws-alerts)$");

    private final AlertBus bus;
    private final CapMessageRepository messages;
    private final TriggerRuleRepository rules;
    private final PlaceRepository places;
    private final SourceRegistry registry;
    private final CredibilityService credibility;
    private final AppSettingService settings;
    private final InstallIdService installId;
    private final FeedSanitizer sanitizer;
    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final CapCategories categories = CapCategories.load();
    private final GeoJsonReader geoJson = new GeoJsonReader();
    private volatile Subscription subscription;

    /** Creates the service. */
    public CapService(AlertBus bus, CapMessageRepository messages, TriggerRuleRepository rules, PlaceRepository places,
            SourceRegistry registry, CredibilityService credibility, AppSettingService settings,
            InstallIdService installId, FeedSanitizer sanitizer, NamedParameterJdbcTemplate jdbc, JsonMapper json,
            Clock clock) {
        this.bus = bus;
        this.messages = messages;
        this.rules = rules;
        this.places = places;
        this.registry = registry;
        this.credibility = credibility;
        this.settings = settings;
        this.installId = installId;
        this.sanitizer = sanitizer;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** Subscribes to the alert bus with the other alert consumers. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.NOTIFY_ROUTER)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe("cap", this, AlertBus.DEFAULT_QUEUE);
        }
    }

    /** Unsubscribes. */
    @PreDestroy
    public synchronized void stop() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    @Override
    public void onAlert(AlertEvent event) {
        if (event.suppressed() || event.alert() == null || event.alert().suppressed()) {
            return;
        }
        try {
            issue(event.type(), event.alert());
        } catch (RuntimeException e) {
            log.warn("Could not issue the CAP message for alert {} ({}): {}", event.alert().id(), event.type(),
                    e.toString());
        }
    }

    @Override
    public void onGap() {
        log.warn("The CAP writer missed alert events; those state changes have no CAP message");
    }

    /** The sender of Foresignal's CAP messages: setting {@code cap.sender}, default {@code foresignal@{installId}}. */
    public String sender() {
        return settings.getOrDefault(SettingKeys.CAP_SENDER, String.class, () -> "foresignal@" + installId.installId());
    }

    /** The instance's display name: setting {@code instance.name}, default the install id. */
    public String instanceName() {
        return settings.getOrDefault(SettingKeys.INSTANCE_NAME, String.class, installId::installId);
    }

    /**
     * Builds and stores the message for one state change of {@code alert}.
     *
     * @param eventType the {@link AlertEvent} type
     * @return the stored message
     */
    public CapMessageEntity issue(String eventType, AlertView alert) {
        List<CapMessageEntity> prior = messages.findByAlertIdOrderByNAsc(alert.id());
        int n = prior.isEmpty() ? 1 : prior.getLast().getN() + 1;
        String sender = sender();
        String identifier = installId.installId() + "-" + alert.id() + "-" + n;
        Instant sent = clock.instant();
        if (!prior.isEmpty() && !sent.isAfter(prior.getLast().getSent())) {
            sent = prior.getLast().getSent().plusSeconds(1);
        }
        List<String> references = new ArrayList<>();
        for (CapMessageEntity m : prior) {
            references.add(CapMessageWriter.reference(sender, m.getIdentifier(), m.getSent()));
        }
        CapMessage.MsgType msgType = prior.isEmpty() ? CapMessage.MsgType.Alert : CapMessage.MsgType.Update;
        boolean allClear = AlertEvent.RESOLVED.equals(eventType) && msgType == CapMessage.MsgType.Update;
        CapMessage message = new CapMessage(identifier, sender, sent, status(alert), msgType, references,
                info(alert, allClear));
        String xml = CapMessageWriter.write(message);
        CapMessageEntity row = new CapMessageEntity(identifier, alert.id(), n, msgType.name(), message.status().name(),
                eventType, sent, message.info().headline(), xml, references.isEmpty() ? null : String.join(" ", references));
        return messages.save(row);
    }

    /** The latest message of an alert. */
    public Optional<CapMessageEntity> latest(long alertId) {
        return messages.findFirstByAlertIdOrderByNDesc(alertId);
    }

    /** A message by identifier. */
    public Optional<CapMessageEntity> message(String identifier) {
        return messages.findById(identifier);
    }

    /** The latest message of each alert. */
    public List<CapMessageEntity> latest(List<Long> alertIds) {
        return alertIds.isEmpty() ? List.of() : messages.latestFor(alertIds);
    }

    // ------------------------------------------------------------------------------------- content

    CapMessage.Status status(AlertView alert) {
        // Prompt 115: the exercise column, or the 112 context flag, through the one marking helper.
        return ai.foresignal.exercise.ExerciseMarking.CAP_EXERCISE.equals(
                ai.foresignal.exercise.ExerciseMarking.capStatus(alert, json))
                ? CapMessage.Status.Exercise : CapMessage.Status.Actual;
    }

    CapMessage.Info info(AlertView alert, boolean allClear) {
        Optional<TriggerRuleEntity> rule = alert.ruleId() == null ? Optional.empty() : rules.findById(alert.ruleId());
        SourceDescriptor descriptor = registry.contains(alert.sourceId()) ? registry.descriptor(alert.sourceId()) : null;
        JsonNode snapshot = tree(alert.recordSnapshot());
        JsonNode payload = snapshot == null ? null : snapshot.get("payload");
        String event = alert.ruleName() != null && !alert.ruleName().isBlank() ? alert.ruleName().strip()
                : "Alert " + alert.id();
        boolean edge = rule.map(r -> r.getMode() == TriggerMode.ENTER || r.getMode() == TriggerMode.EXIT).orElse(false);
        String urgency = edge && "critical".equals(alert.severity()) ? "Immediate" : "Unknown";
        String certainty = descriptor == null ? "Unknown" : descriptor.mode() == Mode.DERIVED ? "Likely" : "Observed";
        boolean official = OFFICIAL_CAP_SOURCE.matcher(alert.sourceId() == null ? "" : alert.sourceId()).matches();
        String upstreamSender = official ? text(payload, "senderName", text(payload, "sender", null)) : null;
        String upstreamWeb = official ? text(payload, "web", null) : null;
        String body = alert.message() == null ? "" : alert.message();
        if (alert.summary() != null && !alert.summary().isBlank()) {
            body = body + "\n\n" + alert.summary();
        }
        String description = sanitizer.text(body);
        if (official) {
            description = (description == null || description.isBlank() ? "" : description + "\n\n")
                    + "Upstream official message from " + (upstreamSender == null ? "the issuing authority" : upstreamSender)
                    + "; this Foresignal message only relays it.";
        }
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("foresignalRuleId", alert.ruleId() == null ? null : alert.ruleId().toString());
        parameters.put("foresignalSource", alert.sourceId());
        parameters.put("foresignalExternalId", alert.externalId());
        parameters.put("foresignalSeq", Long.toString(alert.seq()));
        parameters.put("foresignalGrade", grade(alert));
        Geometry geometry = geometry(snapshot);
        CapMessage.Area area = CapAreas.of(geometry, areaDesc(alert, payload),
                rule.map(TriggerRuleEntity::getBufferMeters).orElse(null));
        return new CapMessage.Info("en-US", categories.of(alert.kind()), event, allClear, urgency,
                severity(alert.severity()), certainty, alert.firedAt(), expires(alert), "Foresignal (" + instanceName() + ")",
                headline(alert, event), description, upstreamWeb != null ? upstreamWeb : web(alert), parameters, area);
    }

    /** CAP severity: critical → Severe, warning → Moderate, info → Minor. */
    static String severity(String severity) {
        if (severity == null) {
            return "Unknown";
        }
        return switch (severity) {
            case "critical" -> "Severe";
            case "warning" -> "Moderate";
            case "info" -> "Minor";
            default -> "Unknown";
        };
    }

    static String headline(AlertView alert, String event) {
        String first = alert.message() == null ? "" : alert.message().strip().lines().findFirst().orElse("").strip();
        String headline = first.isEmpty() ? event : first;
        return headline.length() > HEADLINE_MAX ? headline.substring(0, HEADLINE_MAX - 1) + "…" : headline;
    }

    private String web(AlertView alert) {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        return ClientLinks.fill(server.effectiveClientLinks().alert(), Map.of("id", Long.toString(alert.id())));
    }

    private Instant expires(AlertView alert) {
        if (alert.sourceId() == null || alert.externalId() == null) {
            return null;
        }
        List<Timestamp> rows = jdbc.queryForList("SELECT valid_until FROM record_live WHERE source_id = :s"
                + " AND external_id = :e", Map.of("s", alert.sourceId(), "e", alert.externalId()), Timestamp.class);
        return rows.isEmpty() || rows.getFirst() == null ? null : rows.getFirst().toInstant();
    }

    private String grade(AlertView alert) {
        try {
            String reliability = credibility.reliability(alert.sourceId());
            RecordRow row = new RecordRow(alert.sourceId(), alert.externalId(), alert.kind(), 0, alert.seq(),
                    alert.observedAt(), null, null, null, null, false, null, null, null, null, null, null, 0, null, null,
                    null, null, null, null, null, null, null, null);
            int cred = credibility.assess(row, new CredibilityService.Lookup(CredibilityService.emptyMembers(),
                    java.util.Set.of()));
            return credibility.grade(reliability, cred);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String areaDesc(AlertView alert, JsonNode payload) {
        JsonNode context = tree(alert.context());
        JsonNode ids = context == null ? null : context.path("near").path("placeIds");
        if (ids != null && ids.isArray() && !ids.isEmpty()) {
            List<String> idList = new ArrayList<>();
            ids.forEach(id -> idList.add(id.asString()));
            List<String> names = places.findAllById(idList).stream().map(PlaceEntity::getName)
                    .filter(name -> name != null && !name.isBlank()).toList();
            if (!names.isEmpty()) {
                return String.join(", ", names);
            }
        }
        return text(payload, "areaDesc", null);
    }

    private Geometry geometry(JsonNode snapshot) {
        JsonNode node = snapshot == null ? null : snapshot.get("geometry");
        if (node == null || !node.isObject()) {
            return null;
        }
        try {
            return geoJson.read(node);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private JsonNode tree(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return json.readTree(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field, String fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        return value != null && value.isString() && !value.asString().isBlank() ? value.asString() : fallback;
    }
}
