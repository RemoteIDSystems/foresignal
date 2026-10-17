package ai.foresignal.stream.sse;

import static ai.foresignal.stream.sse.SseFixtures.T;
import static ai.foresignal.stream.sse.SseFixtures.box;
import static ai.foresignal.stream.sse.SseFixtures.limits;
import static ai.foresignal.stream.sse.SseFixtures.touch;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.notify.NotificationJson;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.WebhookChannel;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertSnapshots;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.MatchRow;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.databind.JsonNode;

/**
 * Event payloads, as Foresignal's own serializers produce them, against the schemas the spec
 * declares for them (prompt 020).
 *
 * <p>SSE payloads come from the code paths that write them: {@link SseClient} for the control and
 * live-lane events, {@link SseHub#open} for {@code hello}, {@link RecordMapper#streamChange} for
 * {@code change}, {@link AlertMapper#event} for the alert lifecycle and {@link NotificationJson} for
 * {@code notification} and {@code system}. The webhook body is rendered by the {@code webhook}
 * channel type itself. Each instance is serialized with Jackson and validated with a JSON Schema
 * 2020-12 validator against the schema that {@code x-foresignal-sse-events} maps its event name to.
 *
 * <p>{@link #SSE_EVENTS} is the table of event names Foresignal emits. An event name the spec lists
 * for an implemented stream without an entry here fails, and so does an entry the spec does not
 * list: a new SSE event type cannot ship without a schema.
 */
class EventSchemaConformanceTest {

    private static final SpecSchemas SCHEMAS = new SpecSchemas();
    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final UUID RULE = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** The implemented SSE operations whose event maps this test checks. */
    private static final List<String> STREAMS = List.of("streamChanges", "streamAlerts", "streamEvents");

    /** Every SSE event name Foresignal emits today, with the payloads its serializers produce. */
    static final Map<String, Supplier<List<JsonNode>>> SSE_EVENTS = Map.ofEntries(
            entry("hello", EventSchemaConformanceTest::hello),
            entry(ChangesTopic.EVENT, EventSchemaConformanceTest::changes),
            entry("live", () -> controlFrames("live")),
            entry("touch", () -> controlFrames("touch")),
            entry("resync", () -> controlFrames("resync")),
            entry("filter-applied", () -> controlFrames("filter-applied")),
            entry("heartbeat", () -> controlFrames("heartbeat")),
            entry("reset", EventSchemaConformanceTest::resets),
            entry("alert", () -> alertEvents("alert")),
            entry("alert.updated", () -> alertEvents("alert.updated")),
            entry("alert.resolved", () -> alertEvents("alert.resolved")),
            entry("alert.enriched", EventSchemaConformanceTest::enrichedAlerts),
            entry("notification", () -> List.of(toTree(NotificationJson.toMap(NotifyFixtures.message())),
                    toTree(NotificationJson.toMap(NotifyFixtures.update("alert.resolved"))))),
            entry("system", () -> List.of(toTree(NotificationJson.toMap(systemMessage())))),
            // 033: InboxTopic sends the device's badge as an id-less control event (checkpoint 041).
            entry("badge", () -> List.of(toTree(new ai.foresignal.notify.inbox.InboxService.Badge(3, 1, 42L).toMap()),
                    toTree(new ai.foresignal.notify.inbox.InboxService.Badge(0, 0, null).toMap()))),
            // 116: EvidenceTopic sends EvidenceEvent.payload() on topic evidence.
            entry(ai.foresignal.evidence.EvidenceEvent.CREATED,
                    () -> evidenceEvents(ai.foresignal.evidence.EvidenceEvent.CREATED)),
            entry(ai.foresignal.evidence.EvidenceEvent.MERGED,
                    () -> evidenceEvents(ai.foresignal.evidence.EvidenceEvent.MERGED)),
            entry(ai.foresignal.evidence.EvidenceEvent.FLAGGED,
                    () -> evidenceEvents(ai.foresignal.evidence.EvidenceEvent.FLAGGED)));

    /** {@code canonical.*} payloads as {@code EvidenceEvent.payload()} writes them (prompt 116). */
    static List<JsonNode> evidenceEvents(String type) {
        ai.foresignal.evidence.EvidenceEvent event = new ai.foresignal.evidence.EvidenceEvent(type,
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "seismic", "open", java.time.Instant.parse("2026-03-01T12:00:00Z"),
                List.of("TR"), 3, 2, List.of("LATE_REVISION"), List.of("LATE_REVISION"), null,
                List.of("01JA8Q3V7S2K9W4T6Y1B5N0M3D"), List.of("usgs-earthquakes"));
        return List.of(toTree(event.payload()));
    }

    // ------------------------------------------------------------------------- the table

    @Test
    @DisplayName("every current event name of the implemented streams has a payload factory, and every factory is in the spec")
    void tableMatchesTheSpec() {
        int current = OpenApiSpec.currentPrompt();
        Set<String> specEvents = new LinkedHashSet<>();
        for (String operationId : STREAMS) {
            JsonNode events = sseEvents(operationId);
            for (String name : events.propertyNames()) {
                JsonNode prompt = events.get(name).get("prompt");
                if (prompt == null || Integer.parseInt(prompt.asString()) <= current) {
                    specEvents.add(name);
                }
            }
        }
        assertThat(SSE_EVENTS.keySet())
                .as("event names emitted by the code (this table) versus x-foresignal-sse-events")
                .containsExactlyInAnyOrderElementsOf(specEvents);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"streamChanges", "streamAlerts", "streamEvents"})
    @DisplayName("every payload of every current event conforms to the schema its stream maps it to")
    void payloadsConform(String operationId) {
        JsonNode events = sseEvents(operationId);
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (String name : events.propertyNames()) {
            Supplier<List<JsonNode>> factory = SSE_EVENTS.get(name);
            if (factory == null) {
                continue; // a later prompt's event; tableMatchesTheSpec covers the current ones
            }
            String ref = events.get(name).get("schema").get("$ref").asString();
            List<JsonNode> payloads = factory.get();
            assertThat(payloads).as("payloads for " + name).isNotEmpty();
            for (JsonNode payload : payloads) {
                checked++;
                for (String error : SCHEMAS.validateRef(ref, payload)) {
                    problems.add(operationId + " " + name + " " + ref + ": " + error + " in " + payload);
                }
            }
        }
        assertThat(checked).isPositive();
        assertThat(problems).isEmpty();
    }

    // ------------------------------------------------------------------------- non-SSE events

    @ParameterizedTest(name = "AlertEvent type {0}")
    @ValueSource(strings = {"fired", "updated", "acknowledged", "snoozed", "resolved"})
    @DisplayName("an AlertEvent of every lifecycle type conforms to AlertEvent")
    void alertEventsConform(String type) {
        for (AlertView view : List.of(alert(type, "r"), alert(type, null))) {
            JsonNode payload = toTree(new AlertMapper().event(new AlertEvent(type, view, view.suppressed())));

            assertThat(SCHEMAS.validateComponent("AlertEvent", payload)).as(payload.toString()).isEmpty();
            assertThat(payload.get("type").asString()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("notification messages (alert, lifecycle update, system) conform to NotificationMessage")
    void notificationMessagesConform() {
        for (NotificationMessage message : List.of(NotifyFixtures.message(), NotifyFixtures.update("alert.resolved"),
                systemMessage())) {
            JsonNode payload = toTree(NotificationJson.toMap(message));

            assertThat(SCHEMAS.validateComponent("NotificationMessage", payload)).as(payload.toString()).isEmpty();
        }
    }

    @Test
    @DisplayName("the webhook channel's default body conforms to WebhookEnvelope")
    void webhookBodyConforms() {
        WebhookChannel webhook = new WebhookChannel();
        for (NotificationMessage message : List.of(NotifyFixtures.message(), systemMessage())) {
            PreparedRequest request = webhook.render(message, NotifyFixtures.settings(
                    Map.of("url", "https://receiver.example/hook"), Map.of("hmacSecret", "s3cret")));
            JsonNode body = SseFixtures.JSON.readTree(request.bodyText());

            assertThat(SCHEMAS.validateComponent("WebhookEnvelope", body)).as(body.toString()).isEmpty();
            assertThat(request.headers()).containsKeys("X-Foresignal-Event", "X-Foresignal-Delivery",
                    "X-Foresignal-Timestamp", "X-Foresignal-Signature");
        }
    }

    @Test
    @DisplayName("the validator rejects a payload that breaks its schema, so a pass means something")
    void validatorRejectsNonConformingPayloads() {
        JsonNode bad = SseFixtures.JSON.readTree("{\"reason\":\"not-a-reason\"}");

        assertThat(SCHEMAS.validateComponent("ResyncEvent", bad)).isNotEmpty();
        assertThat(SCHEMAS.validateComponent("HeartbeatEvent", SseFixtures.JSON.readTree("{\"x\":1}"))).isNotEmpty();
    }

    // ------------------------------------------------------------------------- factories

    private static JsonNode sseEvents(String operationId) {
        return OpenApiSpec.routes().stream()
                .filter(op -> operationId.equals(op.operationId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no operation " + operationId))
                .node().get("x-foresignal-sse-events");
    }

    private static JsonNode toTree(Object value) {
        return SseFixtures.JSON.readTree(SseFixtures.JSON.writeValueAsString(value));
    }

    private static List<JsonNode> data(List<SseFrame> frames, String event) {
        return frames.stream().filter(f -> f.event().equals(event)).map(f -> SseFixtures.JSON.readTree(f.data())).toList();
    }

    /** A live update with a geometry and a quality block, and one without either. */
    private static List<LiveUpdate> liveUpdates() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("gs", 412.5);
        properties.put("track", 271);
        properties.put("squawk", null);
        Map<String, Object> quality = new LinkedHashMap<>();
        quality.put("accuracyMeters", 12.0);
        quality.put("positionSource", "adsb");
        return List.of(
                new LiveUpdate("src", "a", "aircraft", T, T.plusSeconds(360), 3,
                        GEOMETRY.createPoint(new Coordinate(-97.75, 30.25)), box(-97.75, 30.25), box(-97.8, 30.2),
                        properties, quality),
                SseFixtures.live("b", T, box(1, 1), null));
    }

    /** Runs one client through every control event, the way SseClientTest does. */
    private static List<JsonNode> controlFrames(String event) {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.limits = limits(1, 0, 0);
        b.filter = StreamFilter.changesOnly(new ChangeFilter(List.of(), List.of(), List.of(), null, null, false, true,
                true, Set.of()));
        SseClient client = b.build();
        client.pump();
        for (LiveUpdate update : liveUpdates()) {
            client.offerLive(update);
        }
        client.offerTouch(touch("c", T, box(1, 1)), List.of("aircraft"));
        b.clock.advance(Duration.ofSeconds(1));
        client.pump();
        client.resync("gap");
        client.applyFilter(client.filter().next(List.of(ChangesTopic.NAME),
                Map.of(ChangesTopic.NAME, client.filter().changes())), Map.of());
        b.clock.advance(Duration.ofSeconds(15));
        client.pump();
        List<JsonNode> out = data(b.sender.frames, event);
        assertThat(out).as(event + " frames in " + b.sender.events()).isNotEmpty();
        return out;
    }

    /** {@code reset} for each reason: buffer_overflow, replay_cap and resync_required. */
    private static List<JsonNode> resets() {
        List<SseFrame> frames = new ArrayList<>();

        SseFixtures.ClientBuilder overflow = new SseFixtures.ClientBuilder();
        overflow.limits = limits(1, 0, 0);
        SseClient a = overflow.build();
        a.pump();
        a.offerFifo(ChangesTopic.NAME, f -> SseFixtures.fifo(1));
        a.offerFifo(ChangesTopic.NAME, f -> SseFixtures.fifo(2));
        a.pump();
        frames.addAll(overflow.sender.frames);

        SseFixtures.ClientBuilder cap = new SseFixtures.ClientBuilder();
        cap.limits = limits(0, 1, 0);
        cap.replays.put(ChangesTopic.NAME, new ReplayRequest(0, true));
        cap.driver = (topic, request, filter, sink) -> {
            for (long seq = 1; seq <= 2; seq++) {
                if (!sink.emit(SseFixtures.fifo(seq))) {
                    return new ReplayOutcome.CapReached();
                }
            }
            return new ReplayOutcome.Done(2);
        };
        cap.build().pump();
        frames.addAll(cap.sender.frames);

        SseFixtures.ClientBuilder floor = new SseFixtures.ClientBuilder();
        floor.replays.put(ChangesTopic.NAME, new ReplayRequest(3, true));
        floor.driver = (topic, request, filter, sink) -> new ReplayOutcome.ResyncRequired(77);
        floor.build().pump();
        frames.addAll(floor.sender.frames);

        List<JsonNode> out = data(frames, "reset");
        assertThat(out).extracting(n -> n.get("reason").asString())
                .contains("buffer_overflow", "replay_cap", "resync_required");
        return out;
    }

    /** {@code change} events as {@link ChangesTopic} renders them: from a loaded row and from the bus summary. */
    private static List<JsonNode> changes() {
        RecordMapper mapper = new RecordMapper(mock(SourceRegistry.class), SseFixtures.JSON);
        RecordRow row = new RecordRow("usgs-earthquakes", "us7000abcd", "earthquake", 2, 41L, T, T, T.minusSeconds(30),
                T, T.plusSeconds(86_400), false, "M 5.1 - 10 km N of Somewhere",
                "{\"type\":\"Point\",\"coordinates\":[-97.75,30.25,10.0]}", "{\"mag\":5.1,\"place\":\"Somewhere\"}",
                null, null, Boolean.FALSE, 0, null, null, T, T, PersistReason.CHANGE.code(), 40L,
                HistoryTier.STANDARD.code(), 7L, new double[] {-97.75, 30.25, -97.75, 30.25}, null);
        ChangeSummary first = new ChangeSummary(42L, null, HistoryTier.TRACK, "src", "e42", "aircraft", 1,
                PersistReason.FIRST, T, null, null, false, 0, box(1, 1), null, null);
        ChangeSummary tombstone = SseFixtures.change(43L, "src", "aircraft", null, box(1, 1), box(1, 1), true);

        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.payloads = (topic, seqs) -> Map.of(41L, row.payload());
        SseClient client = b.build();
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> new FifoEvent(ChangesTopic.NAME, 41L, ChangesTopic.EVENT,
                mapper.streamChange(row, box(-97.8, 30.2), null, false), 41L));
        client.offerFifo(ChangesTopic.NAME, f -> new FifoEvent(ChangesTopic.NAME, 42L, ChangesTopic.EVENT,
                mapper.streamChange(first, false), null));
        client.offerFifo(ChangesTopic.NAME, f -> new FifoEvent(ChangesTopic.NAME, 43L, ChangesTopic.EVENT,
                mapper.streamChange(tombstone, true), null));
        client.pump();
        List<JsonNode> out = data(b.sender.frames, ChangesTopic.EVENT);
        assertThat(out).hasSize(3);
        assertThat(out.getFirst().has("payload")).as("deferred payload loaded").isTrue();
        return out;
    }

    /** {@code hello} as {@link SseHub#open} builds it for an anonymous caller (with a stream key). */
    @SuppressWarnings("unchecked")
    private static List<JsonNode> hello() {
        EventTopic<ChangeFilter> topic = mock(EventTopic.class);
        when(topic.name()).thenReturn(ChangesTopic.NAME);
        when(topic.cursorKey()).thenReturn(ChangesTopic.CURSOR_KEY);
        when(topic.head()).thenReturn(0L);
        when(topic.replay(any(), any(), any())).thenAnswer(inv -> new ReplayOutcome.Done(0));
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.STREAM_LIMITS), eq(StreamLimits.class), any()))
                .thenReturn(StreamLimits.defaults());
        ChangeBus bus = mock(ChangeBus.class);
        when(bus.lastPublishedSeq()).thenReturn(99L);
        SseHub hub = new SseHub(List.of(topic), settings, bus, SseFixtures.JSON);
        FakeSender sender = new FakeSender();
        SseClient client = hub.open(new SseHub.OpenRequest(StreamPrincipal.anonymous("127.0.0.1"),
                SseClient.CursorMode.COMPOSITE, StreamFilter.changesOnly(ChangeFilter.all(Set.of())), Map.of(), true),
                sender);
        try {
            await().atMost(Duration.ofSeconds(5)).until(() -> !sender.named("hello").isEmpty());
        } finally {
            client.close();
        }
        return data(sender.frames, "hello");
    }

    /** Alert lifecycle events under their SSE names, as {@link AlertsTopic} publishes them. */
    private static List<JsonNode> alertEvents(String sseName) {
        List<JsonNode> out = new ArrayList<>();
        for (String type : List.of(AlertEvent.FIRED, AlertEvent.UPDATED, AlertEvent.ACKNOWLEDGED, AlertEvent.SNOOZED,
                AlertEvent.RESOLVED)) {
            AlertEvent event = AlertEvent.of(type, alert(type, "Quakes"));
            if (event.sseEvent().equals(sseName)) {
                out.add(toTree(new AlertMapper().event(event)));
            }
        }
        return out;
    }

    /**
     * {@code alert.enriched} payloads (prompt 030): the full {@code Alert} as {@link AlertsTopic} sends it,
     * with a summary and its citations, and an alert whose rule was deleted.
     */
    private static List<JsonNode> enrichedAlerts() {
        List<JsonNode> out = new ArrayList<>();
        for (String ruleName : java.util.Arrays.asList("Quakes", null)) {
            AlertView a = alert(AlertEvent.FIRED, ruleName);
            AlertView enriched = new AlertView(a.id(), a.ruleId(), a.ruleName(), a.seq(), a.sourceId(), a.externalId(),
                    a.kind(), a.firedAt(), a.observedAt(), a.message(), a.severity(), a.state(), a.rev() + 1,
                    a.acknowledgedAt(), a.acknowledgedBy(), a.resolvedAt(), a.resolvedBy(), a.resolvedSeq(), a.feedback(),
                    a.feedbackAt(), a.note(), a.snoozedUntil(), a.suppressed(), a.suppressedReason(), a.tags(),
                    a.recordSnapshot(), "A M5.1 near Somewhere [usgs-earthquakes:us7000abcd@41] [alert:7].",
                    T.plusSeconds(5), "[{\"id\":\"usgs-earthquakes:us7000abcd\",\"seq\":41,\"title\":\"M5.1\","
                            + "\"type\":\"record\"},{\"id\":\"alert:7\",\"seq\":null,\"title\":\"Quakes\","
                            + "\"type\":\"alert\"}]");
            out.add(toTree(new AlertMapper().alert(enriched, null)));
        }
        return out;
    }

    /** An alert in the state that {@code type} leaves it in; {@code ruleName} null means the rule was deleted. */
    private static AlertView alert(String type, String ruleName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mag", 5.1);
        String snapshot = AlertSnapshots.build(new MatchRow(41L, 40L, "usgs-earthquakes", "us7000abcd", "earthquake", 2,
                PersistReason.CHANGE.code(), T, null, T, null, false,
                "{\"type\":\"Point\",\"coordinates\":[-97.75,30.25]}", "{\"mag\":5.1}", null), payload, SseFixtures.JSON);
        boolean acknowledged = AlertEvent.ACKNOWLEDGED.equals(type);
        boolean resolved = AlertEvent.RESOLVED.equals(type);
        boolean snoozed = AlertEvent.SNOOZED.equals(type);
        Instant later = T.plusSeconds(60);
        return new AlertView(7L, RULE, ruleName, 41L, "usgs-earthquakes", "us7000abcd", "earthquake", T, T,
                "M5.1 near Somewhere", "warning", resolved ? "resolved" : acknowledged ? "acknowledged" : "firing", 3L,
                acknowledged ? later : null, acknowledged ? "token:admin" : null, resolved ? later : null,
                resolved ? "inverse_edge" : null, resolved ? 44L : null,
                AlertEvent.UPDATED.equals(type) ? "useful" : null, AlertEvent.UPDATED.equals(type) ? later : null,
                AlertEvent.UPDATED.equals(type) ? "checked" : null, snoozed ? later.plusSeconds(3600) : null, false,
                null, List.of("quake"), snapshot);
    }

    /** A system notification, as {@code notify.SystemEvent} produces them (019). */
    private static NotificationMessage systemMessage() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("consumer", "trigger.cursor");
        return NotificationMessage.builder(NotificationMessage.SYSTEM, "system.retention_consumer_lag",
                        "Retention consumer trigger.cursor is lagging", "warning")
                .id(9L).body("The clamp was released.").data(data).origin(NotifyFixtures.ORIGIN).createdAt(T).build();
    }
}
