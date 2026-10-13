package ai.foresignal.source.adapters.aisstream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.spi.WebSocketOptions;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link AisStreamAdapter} against a WebSocket server on loopback (prompt 063): subscription, frames,
 * back-pressure, errors, the bad-key rule, the silence watchdog and {@code update()}.
 */
@Timeout(value = 60, unit = TimeUnit.SECONDS)
class AisStreamAdapterTest {

    private static final String KEY = "test-key-7f3a9c";
    private static final Instant T0 = Instant.parse("2026-10-13T12:00:00Z");
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Duration WAIT = Duration.ofSeconds(10);

    private LoopbackWebSocketServer server;
    private MutableClock clock;
    private AisStreamAdapter adapter;
    private AisTestContext ctx;
    private CollectingSink sink;
    private final List<StreamHandle> handles = new ArrayList<>();

    @BeforeEach
    void setUp() {
        server = LoopbackWebSocketServer.start(false);
        clock = new MutableClock(T0);
        adapter = new AisStreamAdapter(server.uri().toString(), new SimpleMeterRegistry(), clock);
        ctx = new AisTestContext(adapter.descriptor(), clock, KEY);
        sink = new CollectingSink();
    }

    @AfterEach
    void tearDown() {
        handles.forEach(StreamHandle::close);
        server.close();
    }

    // ---------- harness ----------

    private AisStreamSession start(List<Throwable> closes) throws Exception {
        StreamHandle handle = adapter.start(ctx, sink);
        handles.add(handle);
        handle.onClose(closes::add);
        return (AisStreamSession) handle;
    }

    private static ValidatedParams params(Map<String, Object> values) {
        return ValidatedParams.of(values);
    }

    private static JsonNode json(String text) {
        return JSON.readTree(text);
    }

    private static String position(String mmsi, String time, double lat, double lon) {
        return "{\"MessageType\":\"PositionReport\",\"MetaData\":{\"MMSI\":" + mmsi + ",\"latitude\":" + lat
                + ",\"longitude\":" + lon + ",\"time_utc\":\"" + time + "\"},\"Message\":{\"PositionReport\":"
                + "{\"UserID\":" + mmsi + ",\"Latitude\":" + lat + ",\"Longitude\":" + lon
                + ",\"Sog\":10.5,\"Cog\":90.0,\"TrueHeading\":91,\"NavigationalStatus\":0}}}";
    }

    private static final String CONFIRMATION =
            "{\"MessageType\":\"SubscriptionConfirmation\",\"Message\":{\"CompressionEnabled\":true}}";

    /** A sink that records, and can block its first accept until released. */
    private static final class CollectingSink implements RecordSink {
        final List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        volatile CountDownLatch gate;
        volatile CountDownLatch entered = new CountDownLatch(1);

        @Override
        public void accept(NormalizedRecord record) {
            entered.countDown();
            CountDownLatch g = gate;
            if (g != null) {
                try {
                    g.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            records.add(record);
        }
    }

    // ---------- subscription ----------

    @Nested
    @DisplayName("the subscription")
    class Subscription {

        @Test
        @DisplayName("is sent within 3 s of opening, lat-first boxes, all five types, no MMSI key when empty")
        void sentWithinThreeSeconds() throws Exception {
            ctx.params = params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("-1.8,50.6,-0.9,50.95", "4,51,5,52")));
            long startedAt = System.nanoTime();

            start(new ArrayList<>());
            LoopbackWebSocketServer.Received first = server.awaitReceived(1, Duration.ofSeconds(3)).get(0);

            assertThat(Duration.ofNanos(System.nanoTime() - startedAt)).isLessThan(AisStreamAdapter.SUBSCRIBE_WITHIN);
            JsonNode frame = json(first.text());
            assertThat(frame.get("APIKey").stringValue()).isEqualTo(KEY);
            JsonNode boxes = frame.get("BoundingBoxes");
            assertThat(boxes.size()).isEqualTo(2);
            assertThat(boxes.get(0).get(0).get(0).doubleValue()).as("min latitude first").isEqualTo(50.6);
            assertThat(boxes.get(0).get(0).get(1).doubleValue()).isEqualTo(-1.8);
            assertThat(boxes.get(0).get(1).get(0).doubleValue()).isEqualTo(50.95);
            assertThat(boxes.get(0).get(1).get(1).doubleValue()).isEqualTo(-0.9);
            assertThat(boxes.get(1).get(0).get(0).doubleValue()).isEqualTo(51.0);
            assertThat(frame.get("FilterMessageTypes").size()).isEqualTo(5);
            assertThat(frame.has("FiltersShipMMSI")).isFalse();
            assertThat(ctx.options()).containsExactly(WebSocketOptions.DEFLATE);
        }

        @Test
        @DisplayName("defaults to the world and carries FiltersShipMMSI and the chosen types when set")
        void defaultsAndFilters() throws Exception {
            ctx.params = params(Map.of(AisStreamAdapter.MMSI_PARAM, List.of("235009802"),
                    AisStreamAdapter.TYPES_PARAM, List.of("PositionReport")));

            start(new ArrayList<>());
            JsonNode frame = json(server.awaitReceived(1, WAIT).get(0).text());

            assertThat(frame.get("BoundingBoxes").get(0).get(0).get(0).doubleValue()).isEqualTo(-90.0);
            assertThat(frame.get("BoundingBoxes").get(0).get(1).get(1).doubleValue()).isEqualTo(180.0);
            assertThat(frame.get("FiltersShipMMSI").get(0).stringValue()).isEqualTo("235009802");
            assertThat(frame.get("FilterMessageTypes").size()).isEqualTo(1);
        }

        @Test
        @DisplayName("update() resends on the same connection, at most once a second, newest wins")
        void updateRateLimited() throws Exception {
            AisStreamSession session = start(new ArrayList<>());
            server.awaitReceived(1, WAIT);

            boolean first = session.update(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("0,0,1,1"))));
            boolean second = session.update(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("2,2,3,3"))));
            List<LoopbackWebSocketServer.Received> frames = server.awaitReceived(2, WAIT);
            Thread.sleep(1500);

            assertThat(first).isTrue();
            assertThat(second).isTrue();
            List<LoopbackWebSocketServer.Received> all = server.received();
            assertThat(all).as("the two updates within a second collapse into one resend").hasSize(2);
            assertThat(Duration.between(frames.get(0).at(), frames.get(1).at()))
                    .isGreaterThanOrEqualTo(AisStreamAdapter.UPDATE_MIN_GAP.minusMillis(50));
            assertThat(json(all.get(1).text()).get("BoundingBoxes").get(0).get(0).get(0).doubleValue()).isEqualTo(2.0);
            assertThat(server.connectionCount()).as("no reconnect").isEqualTo(1);
            assertThat(ctx.opens()).isEqualTo(1);
        }

        @Test
        @DisplayName("update() with unchanged parameters sends nothing; after the end it asks for a restart")
        void updateUnchanged() throws Exception {
            AisStreamSession session = start(new ArrayList<>());
            server.awaitReceived(1, WAIT);

            assertThat(session.update(ValidatedParams.empty())).isTrue();
            Thread.sleep(1300);
            assertThat(server.received()).hasSize(1);

            session.close();
            assertThat(session.update(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("0,0,1,1"))))).isFalse();
        }
    }

    // ---------- frames ----------

    @Nested
    @DisplayName("frames")
    class Frames {

        @Test
        @DisplayName("fragmented binary frames are reassembled into one message")
        void fragments() throws Exception {
            start(new ArrayList<>());
            server.awaitReceived(1, WAIT);

            server.sendFragmentedBinary(position("235009802", "2026-10-13 11:59:58.5 +0000 UTC", 50.9, -1.4), 4);

            await().atMost(WAIT).until(() -> sink.records.size() == 1);
            NormalizedRecord record = sink.records.get(0);
            assertThat(record.externalId()).isEqualTo("235009802");
            assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-13T11:59:58.500Z"));
            await().atMost(WAIT).until(() -> ctx.reportedBytes() > 0);
        }

        @Test
        @DisplayName("text frames are handled too")
        void text() throws Exception {
            start(new ArrayList<>());
            server.awaitReceived(1, WAIT);

            server.sendText(position("235009803", "2026-10-13 11:59:58 +0000 UTC", 50.9, -1.4));

            await().atMost(WAIT).until(() -> sink.records.size() == 1);
        }

        @Test
        @DisplayName("a message over 256 KB is dropped and counted; the next one still arrives")
        void oversize() throws Exception {
            start(new ArrayList<>());
            server.awaitReceived(1, WAIT);
            String huge = "{\"MessageType\":\"PositionReport\",\"pad\":\"" + "x".repeat(300 * 1024) + "\"}";

            server.sendFragmentedBinary(huge, 5);
            server.sendBinary(position("235009804", "2026-10-13 11:59:58 +0000 UTC", 50.9, -1.4));

            await().atMost(WAIT).until(() -> sink.records.size() == 1);
            assertThat(adapter.metrics().droppedCount(AisStreamMetrics.OVERSIZE)).isEqualTo(1.0);
            assertThat(sink.records.get(0).externalId()).isEqualTo("235009804");
        }

        @Test
        @DisplayName("request(1) follows the hand-off: a blocked sink holds back the next message")
        void backPressure() throws Exception {
            sink.gate = new CountDownLatch(1);
            start(new ArrayList<>());
            server.awaitReceived(1, WAIT);

            server.sendBinary(position("235000101", "2026-10-13 11:59:58 +0000 UTC", 50.9, -1.4));
            server.sendBinary(position("235000102", "2026-10-13 11:59:58 +0000 UTC", 50.8, -1.3));
            assertThat(sink.entered.await(10, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(500);

            assertThat(sink.records).isEmpty();
            sink.entered = new CountDownLatch(1);
            sink.gate.countDown();
            await().atMost(WAIT).until(() -> sink.records.size() == 2);
        }

        @Test
        @DisplayName("malformed, unknown-type, no-MMSI and untimed messages are counted, not emitted")
        void dropped() throws Exception {
            start(new ArrayList<>());
            server.awaitReceived(1, WAIT);

            server.sendBinary("{nope");
            server.sendBinary("{\"MessageType\":\"Teleport\",\"Message\":{\"Teleport\":{}}}");
            server.sendBinary("{\"MessageType\":\"PositionReport\",\"Message\":{\"PositionReport\":{}}}");
            server.sendBinary(AdapterFixtures.text(AisStreamAdapter.ID, "untimed.json").trim());
            server.sendBinary(position("235009805", "2026-10-13 11:59:58 +0000 UTC", 50.9, -1.4));

            await().atMost(WAIT).until(() -> sink.records.size() == 1);
            AisStreamMetrics m = adapter.metrics();
            assertThat(m.droppedCount(AisStreamMetrics.MALFORMED)).isEqualTo(1.0);
            assertThat(m.droppedCount(AisStreamMetrics.UNKNOWN_TYPE)).isEqualTo(1.0);
            assertThat(m.droppedCount(AisStreamMetrics.NO_MMSI)).isEqualTo(1.0);
            assertThat(m.droppedCount(AisStreamMetrics.UNTIMED)).isEqualTo(1.0);
            assertThat(m.messageCount("PositionReport")).isEqualTo(1.0);
        }
    }

    // ---------- errors ----------

    @Nested
    @DisplayName("errors and the bad-key rule")
    class Errors {

        @Test
        @DisplayName("an auth error envelope ends the stream with CredentialsRejectedException")
        void authEnvelope() throws Exception {
            List<Throwable> closes = new CopyOnWriteArrayList<>();
            start(closes);
            server.awaitReceived(1, WAIT);

            server.sendBinary(AdapterFixtures.text(AisStreamAdapter.ID, "error-auth.json").trim());

            await().atMost(WAIT).until(() -> closes.size() == 1);
            assertThat(closes.get(0)).isInstanceOf(CredentialsRejectedException.class)
                    .hasMessageContaining("Api Key Is Not Valid");
        }

        @Test
        @DisplayName("any other error envelope is a transport error on the ladder")
        void transportEnvelope() throws Exception {
            List<Throwable> closes = new CopyOnWriteArrayList<>();
            start(closes);
            server.awaitReceived(1, WAIT);

            server.sendBinary("{\"error\":\"Too many connections\"}");

            await().atMost(WAIT).until(() -> closes.size() == 1);
            assertThat(closes.get(0)).isNotInstanceOf(CredentialsRejectedException.class)
                    .hasMessageContaining("Too many connections");
        }

        @Test
        @DisplayName("two closes before any confirmation are CredentialsRejectedException; the first is not")
        void twoClosesBeforeConfirmation() throws Exception {
            server.onMessage((conn, text) -> conn.close(1000, ""));
            List<Throwable> first = new CopyOnWriteArrayList<>();
            List<Throwable> second = new CopyOnWriteArrayList<>();

            start(first);
            await().atMost(WAIT).until(() -> first.size() == 1);
            start(second);
            await().atMost(WAIT).until(() -> second.size() == 1);

            assertThat(first.get(0)).isNotInstanceOf(CredentialsRejectedException.class);
            assertThat(second.get(0)).isInstanceOf(CredentialsRejectedException.class)
                    .hasMessageContaining("API key is probably invalid");
        }

        @Test
        @DisplayName("a confirmation between the closes resets the count")
        void confirmationResets() throws Exception {
            server.onMessage((conn, text) -> conn.close(1000, ""));
            List<Throwable> first = new CopyOnWriteArrayList<>();
            start(first);
            await().atMost(WAIT).until(() -> first.size() == 1);

            server.onMessage((conn, text) -> {
                conn.send(CONFIRMATION.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                conn.close(1000, "");
            });
            List<Throwable> second = new CopyOnWriteArrayList<>();
            start(second);
            await().atMost(WAIT).until(() -> second.size() == 1);
            server.onMessage((conn, text) -> conn.close(1000, ""));
            List<Throwable> third = new CopyOnWriteArrayList<>();
            start(third);
            await().atMost(WAIT).until(() -> third.size() == 1);

            assertThat(second.get(0)).isNotInstanceOf(CredentialsRejectedException.class);
            assertThat(third.get(0)).as("one unconfirmed close since the confirmation")
                    .isNotInstanceOf(CredentialsRejectedException.class);
        }

        @Test
        @DisplayName("a different key resets the count")
        void newKeyResets() throws Exception {
            server.onMessage((conn, text) -> conn.close(1000, ""));
            List<Throwable> first = new CopyOnWriteArrayList<>();
            start(first);
            await().atMost(WAIT).until(() -> first.size() == 1);

            ctx.credentials = Map.of(AisStreamAdapter.API_KEY, "another-key");
            List<Throwable> second = new CopyOnWriteArrayList<>();
            start(second);
            await().atMost(WAIT).until(() -> second.size() == 1);

            assertThat(second.get(0)).isNotInstanceOf(CredentialsRejectedException.class);
        }

        @Test
        @DisplayName("no key is refused before any connection")
        void noKey() {
            ctx.credentials = Map.of();

            org.assertj.core.api.Assertions.assertThatThrownBy(() -> adapter.start(ctx, sink))
                    .isInstanceOf(CredentialsRejectedException.class);
            assertThat(server.connectionCount()).isZero();
        }

        @Test
        @DisplayName("a close after a recognized envelope is a plain drop, and onClose replays a drop it missed")
        void closeAfterTraffic() throws Exception {
            StreamHandle handle = adapter.start(ctx, sink);
            handles.add(handle);
            server.awaitReceived(1, WAIT);
            server.sendBinary(position("235009806", "2026-10-13 11:59:58 +0000 UTC", 50.9, -1.4));
            await().atMost(WAIT).until(() -> sink.records.size() == 1);

            server.closeAll(1001, "going away");
            await().atMost(WAIT).until(() -> !handle.isRunning());
            List<Throwable> late = new ArrayList<>();
            handle.onClose(late::add);

            assertThat(late).hasSize(1);
            assertThat(late.get(0)).isNotInstanceOf(CredentialsRejectedException.class).hasMessageContaining("1001");
        }
    }

    // ---------- watchdog and health ----------

    @Nested
    @DisplayName("the silence watchdog")
    class Watchdog {

        private HealthRuleContext health(Instant now) {
            return new HealthRuleContext(adapter.descriptor(), null, now);
        }

        @Test
        @DisplayName("120 s without an envelope reports degraded; 300 s recycles the connection")
        void reportThenRecycle() throws Exception {
            AisStreamHealth rule = new AisStreamHealth(adapter);
            List<Throwable> closes = new CopyOnWriteArrayList<>();
            AisStreamSession session = start(closes);
            server.awaitReceived(1, WAIT);
            server.sendBinary(CONFIRMATION);
            await().atMost(WAIT).until(() -> session.compressionEnabled() != null);

            clock.advance(Duration.ofSeconds(119));
            assertThat(rule.evaluate(health(clock.instant()))).as("the confirmation is not liveness, but 119 s is fine")
                    .isEmpty();

            clock.advance(Duration.ofSeconds(1));
            Optional<HealthVerdict> degraded = rule.evaluate(health(clock.instant()));
            assertThat(degraded).isPresent();
            assertThat(degraded.get().status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(degraded.get().reason()).isEqualTo("connected but silent for 120 s");
            session.tick();
            assertThat(closes).as("reported, not yet recycled").isEmpty();

            clock.advance(Duration.ofSeconds(180));
            session.tick();

            await().atMost(WAIT).until(() -> closes.size() == 1);
            assertThat(closes.get(0)).isNotInstanceOf(CredentialsRejectedException.class)
                    .hasMessageContaining("silent for 300 s");
            assertThat(session.isRunning()).isFalse();
            await().atMost(WAIT).until(() -> server.openConnections().isEmpty());
            assertThat(rule.evaluate(health(clock.instant()))).as("no open connection, nothing to report").isEmpty();
        }

        @Test
        @DisplayName("a recognized envelope resets the silence")
        void envelopeResets() throws Exception {
            AisStreamHealth rule = new AisStreamHealth(adapter);
            start(new ArrayList<>());
            server.awaitReceived(1, WAIT);
            clock.advance(Duration.ofSeconds(200));

            server.sendBinary(position("235009807", "2026-10-13 12:03:00 +0000 UTC", 50.9, -1.4));
            await().atMost(WAIT).until(() -> sink.records.size() == 1);

            assertThat(rule.evaluate(health(clock.instant()))).isEmpty();
            assertThat(adapter.silentFor(clock.instant())).contains(Duration.ZERO);
        }

        @Test
        @DisplayName("the rule ignores other sources; details carry compressionEnabled and the vessel count")
        void otherSourcesAndDetails() throws Exception {
            AisStreamHealth rule = new AisStreamHealth(adapter);
            SourceDescriptor other = SourceDescriptor.builder("other").name("o").description("o").kind("k")
                    .mode(Mode.STREAMING).build();
            AisStreamSession session = start(new ArrayList<>());
            server.awaitReceived(1, WAIT);
            server.sendBinary(CONFIRMATION);
            await().atMost(WAIT).until(() -> session.compressionEnabled() != null);
            clock.advance(Duration.ofHours(1));

            assertThat(rule.evaluate(new HealthRuleContext(other, null, clock.instant()))).isEmpty();
            assertThat(rule.details(other)).isEmpty();
            Map<String, Object> details = rule.details(adapter.descriptor());
            assertThat(details).containsEntry("compressionEnabled", true).containsEntry("perMessageDeflate", false)
                    .containsEntry("vessels", 0);
        }
    }

    // ---------- secrets ----------

    @Test
    @DisplayName("the API key never appears in the logs, at any level")
    void keyNeverLogged() throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Level before = root.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        root.addAppender(appender);
        root.setLevel(Level.TRACE);
        try {
            List<Throwable> closes = new CopyOnWriteArrayList<>();
            AisStreamSession session = start(closes);
            server.awaitReceived(1, WAIT);
            server.sendBinary(CONFIRMATION);
            session.update(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("0,0,1,1"))));
            server.awaitReceived(2, WAIT);
            server.sendBinary(AdapterFixtures.text(AisStreamAdapter.ID, "error-auth.json").trim());
            await().atMost(WAIT).until(() -> closes.size() == 1);
        } finally {
            root.detachAppender(appender);
            root.setLevel(before);
        }

        assertThat(appender.list).isNotEmpty();
        for (ILoggingEvent event : appender.list) {
            assertThat(event.getFormattedMessage()).doesNotContain(KEY);
            if (event.getThrowableProxy() != null) {
                assertThat(event.getThrowableProxy().getMessage() == null ? "" : event.getThrowableProxy().getMessage())
                        .doesNotContain(KEY);
            }
        }
        assertThat(AisSubscription.of(KEY, ValidatedParams.empty()).toString()).doesNotContain(KEY);
    }

    // ---------- descriptor and params ----------

    @Nested
    @DisplayName("descriptor and parameters")
    class DescriptorAndParams {

        @Test
        @DisplayName("STREAMING, TRACK + SAMPLED (§5.3 vessels), LOCAL_ONLY, apiKey required, valid")
        void descriptor() {
            SourceDescriptor d = adapter.descriptor();

            assertThat(d.id()).isEqualTo("aisstream");
            assertThat(d.mode()).isEqualTo(Mode.STREAMING);
            assertThat(d.kinds()).containsExactly("vessel");
            assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
            assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
            assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
            assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(30));
            assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
            assertThat(d.credentialFields()).containsExactly(CredentialField.required(AisStreamAdapter.API_KEY,
                    d.credentialFields().get(0).description()));
            assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
            assertThat(d.defaultBudget().perDay()).isEqualTo(144);
            IngestPolicy.Sampled policy = (IngestPolicy.Sampled) d.defaultIngestPolicy();
            assertThat(policy.minInterval()).isEqualTo(Duration.ofSeconds(30));
            assertThat(policy.keyframe()).isEqualTo(Duration.ofSeconds(1200));
            assertThat(policy.minDeviationMeters()).isEqualTo(250.0);
            assertThat(policy.edgeMinInterval()).isEqualTo(Duration.ofSeconds(15));
            assertThat(IngestPolicyValidator.keyframeCeiling(d)).as("1800 - 2 - 30").isEqualTo(Duration.ofSeconds(1768));
            assertThat(IngestPolicyValidator.problems(policy, d, "defaultIngestPolicy")).isEmpty();
            assertThat(d.significantFields()).containsExactly(
                    "name", "callsign", "imo", "shipType", "destination", "navStatus", "draught");
            assertThat(d.titleField()).isEqualTo("name");
            assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        }

        @Test
        @DisplayName("bbox entries are range-checked and counted; mmsi entries are nine digits, at most 50")
        void checkParams() {
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("-180,-90,180,90")))))
                    .isEmpty();
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("-181,0,1,1")))))
                    .containsKey(AisStreamAdapter.BBOX_PARAM);
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("0,-91,1,1")))))
                    .containsKey(AisStreamAdapter.BBOX_PARAM);
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("2,0,1,1")))))
                    .containsKey(AisStreamAdapter.BBOX_PARAM);
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of("1,2,3")))))
                    .containsKey(AisStreamAdapter.BBOX_PARAM);
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.BBOX_PARAM, List.of()))))
                    .containsKey(AisStreamAdapter.BBOX_PARAM);
            List<String> eleven = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                eleven.add("0,0,1,1");
            }
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.BBOX_PARAM, eleven))))
                    .containsKey(AisStreamAdapter.BBOX_PARAM);
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.MMSI_PARAM, List.of("23500980")))))
                    .containsKey(AisStreamAdapter.MMSI_PARAM);
            List<String> many = new ArrayList<>();
            for (int i = 0; i < 51; i++) {
                many.add(String.format("235%06d", i));
            }
            assertThat(adapter.checkParams(params(Map.of(AisStreamAdapter.MMSI_PARAM, many))))
                    .containsKey(AisStreamAdapter.MMSI_PARAM);
            Map<String, Object> empty = new HashMap<>();
            empty.put(AisStreamAdapter.TYPES_PARAM, List.of());
            assertThat(adapter.checkParams(params(empty))).containsKey(AisStreamAdapter.TYPES_PARAM);
        }
    }
}
