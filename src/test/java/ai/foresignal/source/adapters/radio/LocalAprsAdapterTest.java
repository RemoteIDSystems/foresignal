package ai.foresignal.source.adapters.radio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.socket.DefaultSocketTransport;
import ai.foresignal.collect.socket.SocketMetrics;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.codec.KissAx25Codec;
import ai.foresignal.source.formats.aprs.AprsPacketDedup;
import ai.foresignal.source.formats.aprs.AprsRecords;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
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
 * {@code local-aprs-{n}} against an embedded KISS server standing in for Direwolf (prompt 082): zero bytes reach the
 * TNC, the connection is opened receive-only, the handle has no write method and a writer is rejected; the
 * fixture's frames become records with the station as observer, non-AX.25 frames are counted, and every packet is
 * registered with the duplicate filter.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class LocalAprsAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-14T00:30:00Z");

    private ServerSocket server;
    private final BlockingQueue<Socket> accepted = new LinkedBlockingQueue<>();
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final AprsPacketDedup dedup = new AprsPacketDedup();
    private LocalAprsAdapter adapter;

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
        adapter = RadioTestAdapters.localAprs(dedup, () -> meters);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
        for (Socket s : accepted) {
            s.close();
        }
    }

    /** A context whose endpoint is the loopback server, pinned as the validator would pin it. */
    private SocketStreamContext context(Map<String, Object> extra, List<SocketOptions> seen) {
        Map<String, Object> values = new java.util.HashMap<>(extra);
        values.put(LocalAprsAdapter.ENDPOINT, URI.create("tcp://127.0.0.1:" + server.getLocalPort()));
        ValidatedParams params = new ValidatedParams(values,
                Map.of(LocalAprsAdapter.ENDPOINT, InetAddress.getLoopbackAddress()));
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), new MutableClock(NOW), params);
        DefaultSocketTransport real = new DefaultSocketTransport(ctx, new SocketMetrics(null, ctx.sourceId()), null);
        return ctx.withSockets(new ai.foresignal.source.spi.SocketTransport() {
            @Override
            public StreamHandle tcp(ai.foresignal.source.spi.LanEndpoint endpoint, SocketOptions options,
                    ai.foresignal.source.spi.Framing framing, ai.foresignal.source.spi.FrameHandler handler)
                    throws ai.foresignal.source.spi.AdapterException {
                seen.add(options);
                return real.tcp(endpoint, options, framing, handler);
            }

            @Override
            public StreamHandle udp(ai.foresignal.source.spi.LanEndpoint bind, java.util.Set<ai.foresignal.net.Cidr> senders,
                    ai.foresignal.source.spi.DatagramHandler handler) throws ai.foresignal.source.spi.AdapterException {
                throw new AssertionError("no UDP");
            }
        });
    }

    private Socket peer() throws InterruptedException {
        Socket socket = accepted.poll(5, TimeUnit.SECONDS);
        assertThat(socket).as("the adapter connected").isNotNull();
        return socket;
    }

    @Test
    @DisplayName("the descriptor validates: STREAMING, LAN tcp:// endpoint, LOCAL_ONLY overridable, its own ladder")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();

        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("local-aprs-1");
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.egressOverridable()).isTrue();
        assertThat(d.param(LocalAprsAdapter.ENDPOINT).type()).isEqualTo(ParamType.LAN_URL);
        assertThat(d.param(LocalAprsAdapter.ENDPOINT).allowed()).containsExactly("tcp");
        assertThat(d.allowedHosts()).isEmpty();
        assertThat(d.reconnect().initial()).isEqualTo(Duration.ofSeconds(5));
        assertThat(d.reconnect().max()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(1);
        assertThat(new LocalAprsFamily(3, new KissAx25Codec(), dedup, null).adapters())
                .extracting(a -> a.descriptor().id()).containsExactly("local-aprs-1", "local-aprs-2", "local-aprs-3");
        assertThatThrownBy(() -> new LocalAprsFamily(17, new KissAx25Codec(), dedup, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Foresignal sends zero bytes; the fixture's frames become records; non-AX.25 frames are counted")
    void receiveOnly() throws Exception {
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        List<SocketOptions> seen = new CopyOnWriteArrayList<>();
        SocketStreamContext ctx = context(Map.of(KissAx25Codec.STATION_PARAM, "N0CALL-10"), seen);
        StreamHandle handle = adapter.start(ctx, records::add);
        try (Socket peer = peer()) {
            OutputStream out = peer.getOutputStream();
            out.write(kissFixture());
            out.flush();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (records.size() < 5 && System.nanoTime() < deadline) {
                Thread.sleep(20);
            }
            peer.setSoTimeout(500);
            InputStream in = peer.getInputStream();
            assertThatThrownBy(in::read).as("no byte from Foresignal").isInstanceOf(SocketTimeoutException.class);
            handle.close();
            peer.setSoTimeout(5000);
            assertThat(in.read()).as("EOF without a single byte").isEqualTo(-1);
        }
        assertThat(seen).singleElement().satisfies(o -> {
            assertThat(o.isReceiveOnly()).isTrue();
            assertThat(o.onConnect()).isNull();
        });
        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("N0CALL-9", "N8QH-9", "N0CALL-13", "N0CALL-5", "N0CALL-3");
        assertThat(records).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(AprsRecords.STATION_KIND);
            assertThat(r.observers()).containsExactly(Observer.of("N0CALL-10"));
        });
        assertThat(meters.find(LocalAprsAdapter.NON_AX25_METRIC).tag("source", "local-aprs-1").counter().count())
                .isEqualTo(2.0);
        assertThat(dedup.seenRecently("N0CALL-9", "=3745.00N/12225.00W>090/036/A=001234 mobile", NOW)).isTrue();
        assertThat(ctx.charged.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("the handle has no write method, and a receive-only option set rejects an onConnect writer")
    void noWritePath() throws Exception {
        StreamHandle handle = adapter.start(context(Map.of(), new CopyOnWriteArrayList<>()), r -> { });
        try {
            peer();
            for (Class<?> type = handle.getClass(); type != null; type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    assertThat(method.getName().toLowerCase()).doesNotContain("write").doesNotContain("send");
                }
            }
        } finally {
            handle.close();
        }
        assertThatThrownBy(() -> SocketOptions.receiveOnly().withOnConnect("KISS".getBytes()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("checkParams refuses a malformed stationCallsign; the observer defaults to the source id")
    void params() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(KissAx25Codec.STATION_PARAM, "not a call"))))
                .containsKey(KissAx25Codec.STATION_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(KissAx25Codec.STATION_PARAM, "N0CALL-10")))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.empty())).isEmpty();
    }

    /** The bytes of {@code fixtures/local-aprs/direwolf-kiss.hex}. */
    static byte[] kissFixture() {
        StringBuilder hex = new StringBuilder();
        for (String line : ai.foresignal.testsupport.adapters.AdapterFixtures.text("local-aprs", "direwolf-kiss.hex")
                .split("\n")) {
            if (!line.startsWith("#")) {
                hex.append(line.replace(" ", "").strip());
            }
        }
        return HexFormat.of().parseHex(hex);
    }
}
