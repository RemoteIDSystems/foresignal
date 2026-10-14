package ai.foresignal.source.adapters.ais;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.mqtt.MosquittoSupport;
import ai.foresignal.testsupport.mqtt.MqttProbe;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ais-digitraffic} (prompt 085): MQTT over WebSocket against the 060 test broker's WebSocket listener (the
 * three subscriptions, each paid for, the client id format, records with the bootstrapped metadata), the
 * {@code Digitraffic-User} header on the WebSocket upgrade, the 24 h stale-fix guard, the status coverage signal, the
 * 10-minute capture replayed, and the metadata bootstrap from WireMock through the real {@link UpstreamClient} with
 * the gzip requirement (a request without {@code Accept-Encoding: gzip} gets 406, as the real API answers).
 */
@IntegrationTest
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class DigitrafficAdapterTest extends WireMockSupport {

    /** Inside the capture's window (2026-10-14 around 03:01–03:12 UTC). */
    private static final Instant NOW = Instant.parse("2026-10-14T03:12:10Z");
    private static final String PATH = "/api/ais/v1/vessels";
    private static final String USER = "Foresignal/1.2.3 (itest085)";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private final MqttConnections connections = new MqttConnections(MqttProperties.defaults(),
            OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName),
            new StaticListableBeanFactory().getBeanProvider(MeterRegistry.class), Clock.systemUTC());
    private final List<StreamHandle> handles = new CopyOnWriteArrayList<>();
    private MutableClock clock;
    private UpstreamClient client;

    @BeforeEach
    void setUp() throws IOException {
        clock = new MutableClock(NOW);
        SourceService sources = mock(SourceService.class);
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 1_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        BackoffRegistry backoff = new BackoffRegistry(jdbc, clock);
        backoff.onSuccess(DigitrafficAisAdapter.ID);
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        serveMetadata();
    }

    @AfterEach
    void tearDown() {
        handles.forEach(StreamHandle::close);
        connections.closeAll();
    }

    /** The trimmed vessel list, gzipped, only to a request that accepts gzip and names itself; 406 otherwise. */
    private static void serveMetadata() throws IOException {
        ByteArrayOutputStream gz = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(gz)) {
            out.write(AdapterFixtures.bytes("ais", "digitraffic-vessels.json"));
        }
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(PATH)).atPriority(10)
                .willReturn(aResponse().withStatus(406).withBody("Not Acceptable")));
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(PATH)).atPriority(1)
                .withHeader("Accept-Encoding", containing("gzip"))
                .withHeader(DigitrafficAisAdapter.USER_HEADER, equalTo(USER))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("Content-Encoding", "gzip").withBody(gz.toByteArray())));
    }

    private DigitrafficAisAdapter adapter(String brokerUrl) {
        return new DigitrafficAisAdapter(connections, () -> "itest085", () -> "1.2.3", brokerUrl, url(PATH));
    }

    private Context context(DigitrafficAisAdapter adapter) {
        SourceDescriptor d = adapter.descriptor();
        return new Context(d, client.forSource(d.id(), d), client.stateStore(d.id()), clock, new AtomicInteger());
    }

    @Test
    @DisplayName("over WebSocket: three subscriptions, each paid; a location carries the bootstrapped metadata")
    void subscriptionsAndRecords() throws Exception {
        MosquittoSupport broker = MosquittoSupport.get();
        DigitrafficAisAdapter adapter = adapter(broker.webSocketUrl());
        Context ctx = context(adapter);
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        handles.add(adapter.start(ctx, records::add));

        assertThat(ctx.charged.get()).as("one CONNECT and three SUBSCRIBEs").isEqualTo(4);
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(PATH)).withHeader("Accept-Encoding", containing("gzip"))
                .withHeader(DigitrafficAisAdapter.USER_HEADER, equalTo(USER)));
        assertThat(adapter.merger().size()).as("the list seeded every vessel").isEqualTo(100);

        long time = NOW.minusSeconds(30).getEpochSecond();
        try (MqttProbe publisher = MqttProbe.subscribe(broker, "unused/085")) {
            publisher.publish("vessels-v2/309075000/location", "{\"time\":" + time + ",\"sog\":10.2,\"cog\":75.0,"
                    + "\"navStat\":0,\"rot\":-128,\"posAcc\":true,\"raim\":false,\"heading\":511,\"lon\":21.5,"
                    + "\"lat\":60.1}");
            publisher.publish("vessels-v2/status", "{\"updated\":" + time + "}");
            publisher.publish("vessels-v3/309075000/location", "{\"time\":" + time + "}");
            await().atMost(Duration.ofSeconds(10)).until(() -> records.size() == 1
                    && adapter.statusUpdated() != null);
        }
        NormalizedRecord r = records.getFirst();
        assertThat(r.externalId()).isEqualTo("309075000");
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochSecond(time));
        assertThat(r.payload()).containsEntry("name", "BALTIC WINTER").containsEntry("callsign", "C6VR4")
                .containsEntry("lengthM", 157).containsEntry("beamM", 23).containsEntry("draught", 8.4)
                .containsEntry("sog", 10.2).doesNotContainKeys("heading", "class");
        assertThat(r.quality().extra()).doesNotContainKey("ais.rot").containsEntry("ais.pos_acc", true);
        assertThat(adapter.statusUpdated()).isEqualTo(Instant.ofEpochSecond(time));
    }

    @Test
    @DisplayName("the connection: MQTT 3.1.1 subscriber session, client id 'Foresignal; {installId}-{random6}', user header")
    void spec() {
        DigitrafficAisAdapter adapter = adapter("");
        MqttConnectionSpec spec = adapter.spec();

        assertThat(spec.clientId()).matches("^Foresignal; itest085-[a-z0-9]{6}$");
        assertThat(adapter.spec().clientId()).isNotEqualTo(spec.clientId());
        assertThat(spec.endpoint().host()).isEqualTo("meri.digitraffic.fi");
        assertThat(spec.endpoint().port()).isEqualTo(443);
        assertThat(spec.endpoint().tls()).isTrue();
        assertThat(spec.endpoint().websocket()).isTrue();
        assertThat(spec.endpoint().path()).isEqualTo("mqtt");
        assertThat(spec.v3()).isTrue();
        assertThat(spec.username()).isNull();
        assertThat(spec.hasStatusTopic()).isFalse();
        assertThat(spec.options().autoReconnect()).isFalse();
        assertThat(spec.options().cleanStart()).isTrue();
        assertThat(spec.webSocketHeaders()).containsExactly(Map.entry("Digitraffic-User", USER));
    }

    @Test
    @DisplayName("the WebSocket upgrade request carries Digitraffic-User")
    void upgradeHeader() {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            DigitrafficAisAdapter adapter = adapter(server.uri().toString().replaceAll("/$", "") + "/mqtt");
            Context ctx = context(adapter);
            // The loopback server is not an MQTT broker, so the start fails after the upgrade.
            assertThatThrownBy(() -> handles.add(adapter.start(ctx, r -> {}))).isInstanceOf(AdapterException.class);
            assertThat(server.handshakes()).isNotEmpty();
            assertThat(server.handshakes().getFirst()).containsEntry(DigitrafficAisAdapter.USER_HEADER, USER);
        }
    }

    @Test
    @DisplayName("a location older than 24 h at arrival is dropped and counted; malformed and unknown topics too")
    void staleFixes() {
        DigitrafficAisAdapter adapter = adapter("");
        String stale = "{\"time\":" + NOW.minus(Duration.ofHours(25)).getEpochSecond() + ",\"lat\":60.0,\"lon\":21.0}";
        String fresh = "{\"time\":" + NOW.minus(Duration.ofHours(23)).getEpochSecond() + ",\"lat\":60.0,\"lon\":21.0}";

        assertThat(adapter.onMessage(inbound("vessels-v2/230000001/location", stale), NOW)).isEmpty();
        assertThat(adapter.staleDropped()).isEqualTo(1);
        assertThat(adapter.onMessage(inbound("vessels-v2/230000001/location", fresh), NOW)).hasSize(1);
        assertThat(adapter.onMessage(inbound("vessels-v2/230000002/location", "not json"), NOW)).isEmpty();
        assertThat(adapter.onMessage(inbound("vessels-v2/0/location", fresh), NOW)).isEmpty();
        assertThat(adapter.onMessage(inbound("vessels-v2/230000003/other", fresh), NOW)).isEmpty();
        assertThat(adapter.healthDetails(NOW)).containsEntry("staleFixesDropped", 1L)
                .containsEntry("malformedDropped", 2L);
    }

    @Test
    @DisplayName("metadata maps refA+refB to lengthM, refC+refD to beamM, tenths to meters and the ETA bit field")
    void metadata() {
        DigitrafficAisAdapter adapter = adapter("");
        String location = "{\"time\":" + NOW.minusSeconds(5).getEpochSecond() + ",\"lat\":60.0,\"lon\":21.0,"
                + "\"navStat\":15,\"sog\":102.3,\"cog\":360.0}";
        assertThat(adapter.onMessage(inbound("vessels-v2/230686000/location", location), NOW))
                .singleElement().satisfies(r -> assertThat(r.payload()).doesNotContainKeys("navStatus", "sog", "cog"));
        String metadata = "{\"callSign\":\"OJSY\",\"destination\":\"FIHEL\",\"draught\":42,\"eta\":795456,"
                + "\"imo\":9304899,\"name\":\"CALYPSO\",\"posType\":1,\"refA\":13,\"refB\":13,\"refC\":5,\"refD\":4,"
                + "\"timestamp\":" + NOW.toEpochMilli() + ",\"type\":52}";

        NormalizedRecord r = adapter.onMessage(inbound("vessels-v2/230686000/metadata", metadata), NOW).getFirst();

        assertThat(r.payload()).containsEntry("name", "CALYPSO").containsEntry("callsign", "OJSY")
                .containsEntry("imo", 9_304_899).containsEntry("shipType", 52).containsEntry("lengthM", 26)
                .containsEntry("beamM", 9).containsEntry("draught", 4.2).containsEntry("destination", "FIHEL");
        // 795456: month 12, day 4, hour 13, minute 0.
        assertThat(r.payload()).containsEntry("eta", "2026-12-04T13:00:00Z");
        assertThat(r.observedAt()).isEqualTo(NOW);
        assertThat(DigitrafficAisParser.eta(795_456L)).isEqualTo(new ai.foresignal.codec.ais.AisMessage.Eta(12, 4, 13, 0));
        assertThat(DigitrafficAisParser.eta(0L)).isNull();
    }

    @Test
    @DisplayName("the 10-minute capture replays: every vessel record valid, status seen, nothing stale")
    void captureReplay() throws IOException {
        DigitrafficAisAdapter adapter = adapter("");
        JsonMapper json = JsonMapper.builder().build();
        List<NormalizedRecord> records = new ArrayList<>();
        int lines = 0;
        byte[] gz = AdapterFixtures.bytes("ais", "digitraffic-mqtt.jsonl.gz");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new ByteArrayInputStream(gz)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                JsonNode n = json.readTree(line);
                lines++;
                records.addAll(adapter.onMessage(new MqttInbound(n.get("topic").asString(),
                        json.writeValueAsBytes(n.get("payload")), false),
                        Instant.ofEpochMilli(n.get("receivedAt").asLong())));
            }
        }
        assertThat(lines).isEqualTo(21_551);
        assertThat(records).hasSize(21_262);
        assertThat(adapter.staleDropped()).isZero();
        assertThat(adapter.statusUpdated()).isNotNull();
        assertThat(records).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("vessel");
            assertThat(r.externalId()).matches("[1-9][0-9]{8}");
            assertThat(r.observedAt()).isNotNull();
        });
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records);
    }

    @Test
    @DisplayName("the metadata list is fetched at start and again only after 12 h")
    void bootstrapEvery12Hours() {
        DigitrafficAisAdapter adapter = adapter("");
        Context ctx = context(adapter);

        assertThat(adapter.bootstrapIfDue(ctx)).isEqualTo(100);
        clock.advance(Duration.ofHours(11));
        assertThat(adapter.bootstrapIfDue(ctx)).isEqualTo(-1);
        clock.advance(Duration.ofHours(1));
        assertThat(adapter.bootstrapIfDue(ctx)).isEqualTo(100);
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    @DisplayName("a failed bootstrap is logged and does not stop the stream from starting")
    void bootstrapFailure() {
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(406)));
        DigitrafficAisAdapter adapter = adapter("");
        assertThat(adapter.bootstrapIfDue(context(adapter))).isEqualTo(-1);
        assertThat(adapter.merger().size()).isZero();
    }

    @Test
    @DisplayName("a status updated more than 10 min ago degrades coverage")
    void statusStale() {
        DigitrafficAisAdapter adapter = adapter("");
        assertThat(adapter.statusProblem(NOW)).isEmpty();
        adapter.onMessage(inbound("vessels-v2/status", "{\"updated\":" + NOW.minusSeconds(600).getEpochSecond() + "}"),
                NOW);
        assertThat(adapter.statusProblem(NOW)).isEmpty();
        Optional<String> problem = adapter.statusProblem(NOW.plusSeconds(61));
        assertThat(problem).hasValueSatisfying(p -> assertThat(p).contains("11 min ago"));
    }

    @Test
    @DisplayName("the descriptor: the fixed broker only, vessel defaults, UPSTREAM, CC BY 4.0 and OPEN")
    void descriptor() {
        DigitrafficAisAdapter adapter = adapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("ais-digitraffic");
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        assertThat(d.allowedHosts()).containsExactly(HostPort.parse("meri.digitraffic.fi:443"));
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.defaultIngestPolicy()).isEqualTo(VesselDescriptors.defaultPolicy());
        assertThat(d.reconnect().initial()).isEqualTo(Duration.ofSeconds(15));
        assertThat(d.reconnect().max()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.attribution()).isEqualTo("Source: Fintraffic / digitraffic.fi, licence CC 4.0 BY");
        assertThat(d.egress()).isEqualTo(Egress.OPEN);
        assertThat(DigitrafficAisAdapter.TOPICS).containsExactly("vessels-v2/+/location", "vessels-v2/+/metadata",
                "vessels-v2/status");
    }

    private static MqttInbound inbound(String topic, String payload) {
        return new MqttInbound(topic, payload.getBytes(StandardCharsets.UTF_8), false);
    }

    /** The smallest {@link StreamContext} around a real {@link SourceHttp}, counting the units charged directly. */
    private record Context(SourceDescriptor descriptor, SourceHttp http, AdapterStateStore state, Clock clock,
            AtomicInteger charged) implements StreamContext {

        @Override
        public String sourceId() {
            return descriptor.id();
        }

        @Override
        public ValidatedParams params() {
            return ValidatedParams.empty();
        }

        @Override
        public Map<String, String> credentials() {
            return Map.of();
        }

        @Override
        public Optional<Instant> lastSuccessAt() {
            return Optional.empty();
        }

        @Override
        public void chargeRequest(String label) throws AdapterException {
            http.chargeRequest(label);
            charged.incrementAndGet();
        }

        @Override
        public void recordReceivedBytes(long bytes) {}

        @Override
        public void connected() {}

        @Override
        public void disconnected(Throwable error) {}
    }
}
