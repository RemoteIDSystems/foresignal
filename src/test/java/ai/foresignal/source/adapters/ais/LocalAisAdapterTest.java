package ai.foresignal.source.adapters.ais;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.codec.AivdmSocketCodec;
import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * {@code local-ais-{n}} against an embedded TCP server standing in for AIS-catcher's {@code -S} server (prompt 085):
 * nothing is written to the socket, the station position is absent by default (and cannot even be stored), the
 * published station record when asked for, records with the station observer and no {@code observedAt} (RECEIVED),
 * health statistics, the slot family and the descriptor.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class LocalAisAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-13T13:30:00Z");

    private ServerSocket server;
    private final BlockingQueue<Socket> accepted = new LinkedBlockingQueue<>();
    private final MutableClock clock = new MutableClock(NOW);
    private final AivdmSocketCodec codec = new AivdmSocketCodec(() -> null);
    private LocalAisAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().start(() -> {
            while (!server.isClosed()) {
                try {
                    accepted.add(server.accept());
                } catch (IOException e) {
                    return;
                }
            }
        });
        adapter = new LocalAisAdapter(1, codec);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
        for (Socket s : accepted) {
            s.close();
        }
    }

    /** A context whose endpoint is the loopback server, pinned as the validator would pin it. */
    private SocketStreamContext context(Map<String, Object> extra) {
        Map<String, Object> values = new HashMap<>(extra);
        values.put(LocalAisAdapter.ENDPOINT, URI.create("tcp://127.0.0.1:" + server.getLocalPort()));
        ValidatedParams params = new ValidatedParams(values,
                Map.of(LocalAisAdapter.ENDPOINT, InetAddress.getLoopbackAddress()));
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), clock, params);
        return ctx.withSockets(new AisLoopbackTransport(ctx, server.getLocalPort()));
    }

    private Socket peer() throws InterruptedException {
        Socket socket = accepted.poll(5, TimeUnit.SECONDS);
        assertThat(socket).as("the adapter connected").isNotNull();
        return socket;
    }

    @Test
    @DisplayName("by default: no station record, records observed by 'local', RECEIVED time, and nothing written")
    void defaults() throws Exception {
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        SocketStreamContext ctx = context(Map.of());
        AisLoopbackTransport transport = (AisLoopbackTransport) ctx.sockets();
        StreamHandle handle = adapter.start(ctx, records::add);
        try (Socket peer = peer()) {
            OutputStream out = peer.getOutputStream();
            out.write(AdapterFixtures.bytes("ais", "aiscatcher-stream.txt"));
            out.flush();
            await().atMost(Duration.ofSeconds(10)).until(() -> records.size() >= KystverketAdapterTest.FIXTURE_RECORDS);

            assertThat(records).hasSize(KystverketAdapterTest.FIXTURE_RECORDS);
            assertThat(records).allSatisfy(r -> {
                assertThat(r.kind()).as("never an ais-receiver record by default").isEqualTo(VesselDescriptors.KIND);
                assertThat(r.observedAt()).isNull();
                assertThat(r.observers()).singleElement().satisfies(o -> assertThat(o.receiverId()).isEqualTo("local"));
            });
            FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records);

            SocketOptions opts = transport.options.getFirst();
            assertThat(opts.isReceiveOnly()).isTrue();
            peer.setSoTimeout(300);
            InputStream in = peer.getInputStream();
            assertThatThrownBy(in::read).as("Foresignal writes nothing").isInstanceOf(SocketTimeoutException.class);
        } finally {
            handle.close();
        }
        assertThat(ctx.charged).hasValue(1);
        assertThat(adapter.healthDetails(NOW.plusSeconds(1))).containsKeys("messagesPerMin", "distinctMmsi10m",
                "lastMessageAt").containsEntry("vessels", 5);
    }

    @Test
    @DisplayName("publishStationPosition emits the station record first and again after 10 minutes, named by stationName")
    void publishedStation() {
        ValidatedParams params = ValidatedParams.of(Map.of(LocalAisAdapter.STATION_NAME, "rooftop",
                LocalAisAdapter.PUBLISH_POSITION, true, LocalAisAdapter.STATION_LAT, 59.91,
                LocalAisAdapter.STATION_LON, 10.75));
        assertThat(adapter.checkParams(params)).isEmpty();
        LocalAisAdapter.Connection connection = adapter.newConnection("local-ais-1", params);
        byte[] line = "!AIVDM,1,1,,A,13m=TNPtAsPh4;0RAV`8h6g22000,0*6B".getBytes(StandardCharsets.US_ASCII);

        List<NormalizedRecord> first = connection.onFrame(line, NOW);
        assertThat(first).hasSize(2);
        assertThat(first.getFirst().kind()).isEqualTo(LocalAisAdapter.RECEIVER_KIND);
        assertThat(first.getFirst().externalId()).isEqualTo("rooftop");
        assertThat(first.getFirst().geometry().getCoordinate().y).isEqualTo(59.91);
        assertThat(first.get(1).observers().getFirst().receiverId()).isEqualTo("rooftop");

        assertThat(connection.onFrame(line, NOW.plus(Duration.ofMinutes(9)))).hasSize(1);
        assertThat(connection.onFrame(line, NOW.plus(Duration.ofMinutes(10)))).hasSize(2);
    }

    @Test
    @DisplayName("the station position is refused unless published, required when published, and range-checked")
    void stationParams() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalAisAdapter.STATION_LAT, 59.9))))
                .containsKey(LocalAisAdapter.STATION_LAT);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalAisAdapter.STATION_LON, 10.7))))
                .containsKey(LocalAisAdapter.STATION_LON);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalAisAdapter.PUBLISH_POSITION, true))))
                .containsKeys(LocalAisAdapter.STATION_LAT, LocalAisAdapter.STATION_LON);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalAisAdapter.PUBLISH_POSITION, true,
                LocalAisAdapter.STATION_LAT, 91.0, LocalAisAdapter.STATION_LON, 10.0))))
                .containsOnlyKeys(LocalAisAdapter.STATION_LAT);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalAisAdapter.STATION_NAME, "bad/name"))))
                .containsKey(LocalAisAdapter.STATION_NAME);
        assertThat(adapter.checkParams(ValidatedParams.empty())).isEmpty();
        assertThat(LocalAisAdapter.station(ValidatedParams.empty())).isEqualTo("local");
    }

    @Test
    @DisplayName("start refuses parameters checkParams refuses, before connecting")
    void startChecksParams() {
        SocketStreamContext ctx = context(Map.of(LocalAisAdapter.STATION_LAT, 59.9));
        assertThatThrownBy(() -> adapter.start(ctx, r -> {}))
                .isInstanceOf(ai.foresignal.source.spi.AdapterException.class);
        assertThat(ctx.charged).hasValue(0);
    }

    @Test
    @DisplayName("the family registers 1 to 16 slots and refuses anything else")
    void family() {
        LocalAisFamily family = new LocalAisFamily(3, codec);
        assertThat(family.adapters()).extracting(a -> a.descriptor().id())
                .containsExactly("local-ais-1", "local-ais-2", "local-ais-3");
        assertThat(family.find("local-ais-2")).isPresent();
        assertThat(family.find("local-ais-9")).isEmpty();
        assertThatThrownBy(() -> new LocalAisFamily(0, codec)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new LocalAisFamily(17, codec)).isInstanceOf(IllegalStateException.class);
        for (SourceAdapter a : family.adapters()) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).isEmpty();
        }
    }

    @Test
    @DisplayName("the descriptor: LAN tcp:// endpoint without default, RECEIVED, TRACK, LOCAL_ONLY and overridable")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("local-ais-1");
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.egressOverridable()).isTrue();
        assertThat(d.allowedHosts()).isEmpty();
        assertThat(d.param(LocalAisAdapter.ENDPOINT).type()).isEqualTo(ParamType.LAN_URL);
        assertThat(d.param(LocalAisAdapter.ENDPOINT).required()).isTrue();
        assertThat(d.param(LocalAisAdapter.ENDPOINT).defaultValue()).isNull();
        assertThat(d.param(LocalAisAdapter.PUBLISH_POSITION).defaultValue()).isEqualTo(false);
        assertThat(d.param(LocalAisAdapter.STATION_NAME).defaultValue()).isEqualTo("local");
        assertThat(d.kinds()).containsExactlyInAnyOrder("vessel", "ais-receiver");
    }
}
