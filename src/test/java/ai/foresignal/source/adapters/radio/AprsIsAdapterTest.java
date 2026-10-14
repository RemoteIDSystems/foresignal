package ai.foresignal.source.adapters.radio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.formats.aprs.AprsPacketDedup;
import ai.foresignal.source.formats.aprs.AprsRecords;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
 * {@code aprs-is} against an embedded TCP server standing in for aprsc (prompt 082): the exact login line with
 * {@code pass -1}, nothing written after it, the real 200-line capture decoded with igates as observers, filter and
 * login validation, the reconnect jitter bounds, dedup against a packet a local TNC heard, and the descriptor.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class AprsIsAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-14T00:30:00Z");
    private static final String FILTER = "r/37.77/-122.42/50";

    private ServerSocket server;
    private final BlockingQueue<Socket> accepted = new LinkedBlockingQueue<>();
    private final MutableClock clock = new MutableClock(NOW);
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final AprsPacketDedup dedup = new AprsPacketDedup();
    private AprsIsAdapter adapter;

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
        adapter = RadioTestAdapters.aprsIs(dedup, () -> meters);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
        for (Socket s : accepted) {
            s.close();
        }
    }

    private SocketStreamContext context(Map<String, Object> params) {
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), clock, ValidatedParams.of(params));
        return ctx.withSockets(new LoopbackPublicTransport(ctx, server.getLocalPort()));
    }

    private Socket peer() throws InterruptedException {
        Socket socket = accepted.poll(5, TimeUnit.SECONDS);
        assertThat(socket).as("the adapter connected").isNotNull();
        return socket;
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        int b;
        while ((b = in.read()) >= 0) {
            line.append((char) b);
            if (b == '\n') {
                break;
            }
        }
        return line.toString();
    }

    @Test
    @DisplayName("the descriptor validates: STREAMING to rotate.aprs2.net:14580 only, disabled-by-default opt-in shape")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();

        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        assertThat(d.allowedHosts()).containsExactly(HostPort.parse("rotate.aprs2.net:14580"));
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(60));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(1, 100));
        assertThat(d.kinds()).containsExactlyInAnyOrder(AprsRecords.STATION_KIND, AprsRecords.MESSAGE_KIND);
        assertThat(d.volatileFields()).containsExactly("path", "raw");
        assertThat(d.embedFields()).isEmpty();
        assertThat(d.param(AprsIsAdapter.FILTER).required()).isTrue();
        assertThat(d.param(AprsIsAdapter.FILTER).defaultValue()).isNull();
    }

    @Test
    @DisplayName("the exact login line with pass -1 is written once, then nothing; lines become records with observers")
    void loginAndNothingAfter() throws Exception {
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        SocketStreamContext ctx = context(Map.of(AprsIsAdapter.FILTER, FILTER));
        StreamHandle handle = adapter.start(ctx, records::add);
        try (Socket peer = peer()) {
            peer.setSoTimeout(5000);
            InputStream in = peer.getInputStream();
            assertThat(readLine(in)).isEqualTo("user ELABCD12 pass -1 vers Foresignal 1.3.0 filter " + FILTER + "\r\n");

            OutputStream out = peer.getOutputStream();
            out.write(AdapterFixtures.bytes("aprs-is", "stream-r-sf-bay.txt"));
            out.flush();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (records.size() < 175 && System.nanoTime() < deadline) {
                Thread.sleep(20);
            }
            assertThat(records).hasSize(175);

            peer.setSoTimeout(500);
            assertThatThrownBy(in::read).as("nothing written after the login").isInstanceOf(SocketTimeoutException.class);
        } finally {
            handle.close();
        }
        assertThat(ctx.charged.get()).isEqualTo(1);
        NormalizedRecord object = records.stream().filter(r -> r.externalId().equals("K6SDR-10")).findFirst().orElseThrow();
        assertThat(object.observers()).containsExactly(Observer.of("WLNK-1"));
        assertThat(object.payload()).containsEntry("sender", "WINLINK").doesNotContainKey("igate");
        assertThat(records).allSatisfy(r -> assertThat(r.payload()).doesNotContainKeys("igate", "receiver"));
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records.stream()
                .filter(r -> !r.tombstone()).toList());
        assertThat(meters.find(AprsIsAdapter.UNSUPPORTED_METRIC).tag("source", "aprs-is").counter().count())
                .isEqualTo(6.0);
    }

    @Test
    @DisplayName("the connection is public TCP to the allowed host, receive-only after the login, LINES framing")
    void connectionShape() throws Exception {
        SocketStreamContext ctx = context(Map.of(AprsIsAdapter.FILTER, FILTER, AprsIsAdapter.LOGIN, "N0ABC-5"));
        LoopbackPublicTransport transport = (LoopbackPublicTransport) ctx.sockets();
        StreamHandle handle = adapter.start(ctx, r -> { });
        try (Socket peer = peer()) {
            peer.setSoTimeout(5000);
            assertThat(readLine(peer.getInputStream())).startsWith("user N0ABC-5 pass -1 ");
        } finally {
            handle.close();
        }
        assertThat(transport.targets).containsExactly(HostPort.parse(AprsIsAdapter.HOST));
        assertThat(transport.options.getFirst().isReceiveOnly()).isFalse();
        assertThat(transport.options.getFirst().silenceTimeout()).isEqualTo(Duration.ofSeconds(120));
        assertThat(transport.options.getFirst().writeMode())
                .isEqualTo(ai.foresignal.source.spi.SocketOptions.WriteMode.ON_CONNECT_ONLY);
    }

    @Test
    @DisplayName("filter validation: required, at most 5 terms, r/ up to 250 km, no bare wildcard, no global t/")
    void filterValidation() {
        assertThat(AprsIsFilter.problem(FILTER)).isNull();
        assertThat(AprsIsFilter.problem("r/37.77/-122.42/250 b/N0CALL* p/K6/W6 t/poim/N0CALL/25 f/N0CALL-9/10")).isNull();
        Map<String, String> bad = new HashMap<>();
        bad.put("", "required");
        bad.put("r/37.77/-122.42/251", "over 250 km");
        bad.put("r/1/1/1 r/1/1/1 r/1/1/1 r/1/1/1 r/1/1/1 r/1/1/1", "at most 5");
        bad.put("b/*", "bare wildcard");
        bad.put("p/*", "bare wildcard");
        bad.put("t/p", "worldwide");
        bad.put("a/50/-125/30/-115", "use r/");
        bad.put("-b/N0CALL", "use r/");
        bad.put("r/95/0/10", "valid latitude");
        bad.put("r/37/-122", "r/lat/lon/km");
        bad.put("f/N0CALL/900", "over 250 km");
        bad.put("r/37/-122/0", "positive");
        bad.forEach((filter, message) -> assertThat(AprsIsFilter.problem(filter)).as(filter).contains(message));
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of()))).containsKey(AprsIsAdapter.FILTER);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(AprsIsAdapter.FILTER, FILTER)))).isEmpty();
    }

    @Test
    @DisplayName("login validation: N0CALL and NOCALL refused, the pattern enforced; the default is EL + install id")
    void loginValidation() throws Exception {
        assertThat(AprsIsAdapter.loginProblem("N0CALL")).contains("refused");
        assertThat(AprsIsAdapter.loginProblem("NOCALL-1")).contains("refused");
        assertThat(AprsIsAdapter.loginProblem("n0abc")).isNotNull();
        assertThat(AprsIsAdapter.loginProblem("N0ABC-123")).isNotNull();
        assertThat(AprsIsAdapter.loginProblem("N0ABC-15")).isNull();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(AprsIsAdapter.FILTER, FILTER, AprsIsAdapter.LOGIN,
                "N0CALL")))).containsKey(AprsIsAdapter.LOGIN);
        assertThat(adapter.loginLine(ValidatedParams.of(Map.of(AprsIsAdapter.FILTER, "r/1/2/3  b/N0ABC"))))
                .isEqualTo("user ELABCD12 pass -1 vers Foresignal 1.3.0 filter r/1/2/3 b/N0ABC\r\n");
        assertThatThrownBy(() -> adapter.loginLine(ValidatedParams.of(Map.of(AprsIsAdapter.FILTER, "b/*"))))
                .isInstanceOf(AdapterException.class);
        assertThatThrownBy(() -> adapter.start(context(Map.of()), r -> { })).isInstanceOf(AdapterException.class);
        assertThat(accepted).as("no connection without a valid filter").isEmpty();
    }

    @Test
    @DisplayName("reconnects back off from 30 s to 15 min with jitter inside ±20%")
    void reconnectJitter() {
        var policy = adapter.descriptor().reconnect();

        assertThat(policy.initial()).isEqualTo(Duration.ofSeconds(30));
        assertThat(policy.max()).isEqualTo(Duration.ofMinutes(15));
        java.util.Random random = new java.util.Random(82);
        for (int attempt = 0; attempt < 12; attempt++) {
            long base = policy.base(attempt).toMillis();
            for (int i = 0; i < 200; i++) {
                long delay = policy.delay(attempt, random::nextDouble).toMillis();
                assertThat(delay).as("attempt %d", attempt).isBetween(Math.round(base * 0.8), Math.round(base * 1.2));
            }
        }
    }

    @Test
    @DisplayName("dedup: a packet a local TNC heard within 30 s is dropped and counted; with dedupLocal=false it is kept")
    void dedupAgainstLocal() {
        String line = "N0CALL-9>APRS,WIDE1-1,qAR,N0CALL-10:=3745.00N/12225.00W>090/036/A=001234 mobile";
        dedup.register("N0CALL-9", "=3745.00N/12225.00W>090/036/A=001234 mobile", NOW);

        AprsIsAdapter.Session on = adapter.newSession("aprs-is", false, true);
        AprsIsAdapter.Session off = adapter.newSession("aprs-is", false, false);
        byte[] frame = line.getBytes(StandardCharsets.UTF_8);

        assertThat(on.onLine(frame, NOW.plusSeconds(10))).isEmpty();
        assertThat(off.onLine(frame, NOW.plusSeconds(10))).hasSize(1);
        assertThat(on.onLine(frame, NOW.plusSeconds(45))).as("after the window").hasSize(1);
        assertThat(meters.find(AprsIsAdapter.DEDUP_METRIC).tag("source", "aprs-is").counter().count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("server comments produce nothing; the logresp is kept; messages only with includeMessages")
    void commentsAndMessages() {
        AprsIsAdapter.Session session = adapter.newSession("aprs-is", false, true);
        AprsIsAdapter.Session messages = adapter.newSession("aprs-is", true, true);
        byte[] message = "N0CALL-1>APRS,TCPIP*,qAC,T2TEST::N0CALL-2 :Hello{1".getBytes(StandardCharsets.UTF_8);

        assertThat(session.onLine("# logresp ELABCD12 unverified, server T2TEST".getBytes(), NOW)).isEmpty();
        assertThat(session.logresp()).contains("unverified");
        assertThat(session.onLine(message, NOW)).isEmpty();
        assertThat(messages.onLine(message, NOW)).singleElement()
                .satisfies(r -> assertThat(r.kind()).isEqualTo(AprsRecords.MESSAGE_KIND));
    }
}
