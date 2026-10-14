package ai.foresignal.source.adapters.ais;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.codec.ais.TagBlock;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.codec.AivdmSocketCodec;
import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.TtlAnchor;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * {@code ais-kystverket} against an embedded TCP server replaying the TAG-block fixture (prompt 085): receive-only
 * public TCP to the one allowed host with a 20 s connect timeout, records with the TAG time and the base station as
 * observer, untimed and bad-checksum lines dropped and counted, the hourly connect cap, and the descriptor.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class KystverketAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-13T13:30:00Z");

    /** Records the fixture yields: 240 type 1, 20 type 18, 4 type 24 and 8 type 5, each after a first fix. */
    static final int FIXTURE_RECORDS = 272;

    private ServerSocket server;
    private final BlockingQueue<Socket> accepted = new LinkedBlockingQueue<>();
    private final MutableClock clock = new MutableClock(NOW);
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private KystverketAdapter adapter;

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
        adapter = new KystverketAdapter(new AivdmSocketCodec(() -> meters));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
        for (Socket s : accepted) {
            s.close();
        }
    }

    private SocketStreamContext context() {
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), clock, ValidatedParams.empty());
        return ctx.withSockets(new AisLoopbackTransport(ctx, server.getLocalPort()));
    }

    private Socket peer() throws InterruptedException {
        Socket socket = accepted.poll(5, TimeUnit.SECONDS);
        assertThat(socket).as("the adapter connected").isNotNull();
        return socket;
    }

    @Test
    @DisplayName("the captured-style stream replays into vessel snapshots with TAG times and base-station observers")
    void replay() throws Exception {
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        SocketStreamContext ctx = context();
        AisLoopbackTransport transport = (AisLoopbackTransport) ctx.sockets();
        StreamHandle handle = adapter.start(ctx, records::add);
        try (Socket peer = peer()) {
            OutputStream out = peer.getOutputStream();
            out.write(AdapterFixtures.bytes("ais", "kystverket-stream.txt"));
            out.flush();
            await().atMost(Duration.ofSeconds(10)).until(() -> records.size() >= FIXTURE_RECORDS);

            assertThat(records).hasSize(FIXTURE_RECORDS);
            assertThat(records).allSatisfy(r -> {
                assertThat(r.kind()).isEqualTo(VesselDescriptors.KIND);
                assertThat(r.observedAt()).isBetween(Instant.ofEpochSecond(1_791_900_000L),
                        Instant.ofEpochSecond(1_791_900_590L));
                assertThat(r.observers()).singleElement().satisfies(o ->
                        assertThat(o.receiverId()).isIn("2573485", "2573111"));
            });
            assertThat(records.stream().map(NormalizedRecord::externalId).collect(Collectors.toSet()))
                    .containsExactlyInAnyOrder("257100010", "257100020", "258200030", "259300040", "257900050");
            assertThat(records.stream().filter(r -> "257900050".equals(r.externalId())))
                    .allSatisfy(r -> assertThat(r.observers().getFirst().receiverId()).isEqualTo("2573111"));
            FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records);
            assertThat(transport.targets).containsExactly(HostPort.parse(KystverketAdapter.HOST));

            // Receive-only: nothing ever arrives from Foresignal.
            peer.setSoTimeout(300);
            InputStream in = peer.getInputStream();
            assertThatThrownBy(in::read).isInstanceOf(SocketTimeoutException.class);
        } finally {
            handle.close();
        }
        assertThat(ctx.charged).hasValue(1);
        assertThat(adapter.healthDetails(NOW)).containsKeys("messagesPerMin", "distinctMmsi10m", "lastMessageAt",
                "vessels");
    }

    @Test
    @DisplayName("receive-only options with a 20 s connect timeout and a 120 s silence watchdog")
    void options() throws Exception {
        SocketStreamContext ctx = context();
        AisLoopbackTransport transport = (AisLoopbackTransport) ctx.sockets();
        StreamHandle handle = adapter.start(ctx, r -> {});
        try {
            SocketOptions opts = transport.options.getFirst();
            assertThat(opts.isReceiveOnly()).isTrue();
            assertThat(opts.onConnect()).isNull();
            assertThat(opts.connectTimeout()).isEqualTo(Duration.ofSeconds(20));
            assertThat(opts.silenceTimeout()).isEqualTo(Duration.ofSeconds(120));
        } finally {
            handle.close();
        }
    }

    @Test
    @DisplayName("a line without TAG time is dropped and counted (no per-record fallback); bad checksums are counted")
    void untimedAndBadChecksum() {
        AivdmSocketCodec.Session session = adapter.newSession("ais-kystverket");
        String sentence = "!AIVDM,1,1,,A,13m=TNPtAsPh4;0RAV`8h6g22000,0*6B";

        assertThat(session.onFrame(sentence.getBytes(StandardCharsets.US_ASCII), NOW)).isEmpty();
        assertThat(session.onFrame((TagBlock.format(Map.of("s", "2573485")) + sentence)
                .getBytes(StandardCharsets.US_ASCII), NOW)).isEmpty();
        assertThat(session.onFrame(("\\s:2573485,c:1791900000*7F\\" + sentence).getBytes(StandardCharsets.US_ASCII),
                NOW)).isEmpty();
        assertThat(session.onFrame((TagBlock.format(Map.of("c", "1791900000")) + sentence.replace("*6B", "*00"))
                .getBytes(StandardCharsets.US_ASCII), NOW)).isEmpty();
        List<NormalizedRecord> ok = session.onFrame((TagBlock.format(Map.of("c", "1791900000250")) + sentence)
                .getBytes(StandardCharsets.US_ASCII), NOW);

        assertThat(session.counts().untimed()).isEqualTo(2);
        assertThat(session.counts().checksum()).isEqualTo(2);
        assertThat(ok).singleElement().satisfies(r -> {
            assertThat(r.observedAt()).isEqualTo(Instant.ofEpochMilli(1_791_900_000_250L));
            assertThat(r.observers()).isEmpty();
        });
        assertThat(meters.find(AivdmSocketCodec.DROPPED).tag("reason", "untimed").counter().count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("at most 12 connects in any hour; the 13th start is refused before any unit is spent")
    void hourlyCap() throws Exception {
        for (int i = 0; i < 12; i++) {
            SocketStreamContext ctx = context();
            adapter.start(ctx, r -> {}).close();
            clock.advance(Duration.ofMinutes(1));
        }
        SocketStreamContext thirteenth = context();
        assertThatThrownBy(() -> adapter.start(thirteenth, r -> {})).isInstanceOf(AdapterException.class)
                .hasMessageContaining("12 connects");
        assertThat(thirteenth.charged).hasValue(0);
        clock.advance(Duration.ofMinutes(50));
        adapter.start(context(), r -> {}).close();
    }

    @Test
    @DisplayName("the descriptor: STREAMING to the open host only, vessel defaults, UPSTREAM, NLOD 2.0 and OPEN")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();

        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("ais-kystverket");
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        assertThat(d.allowedHosts()).containsExactly(HostPort.parse("153.44.253.27:5631"));
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(d.defaultIngestPolicy()).isEqualTo(VesselDescriptors.defaultPolicy());
        assertThat(d.significantFields()).containsExactlyElementsOf(VesselDescriptors.SIGNIFICANT_FIELDS);
        assertThat(d.searchFields()).containsExactly("name", "callsign", "mmsi", "imo");
        assertThat(d.tileProperties()).containsExactly("name", "shipType", "sog", "cog", "heading", "navStatus");
        assertThat(d.titleField()).isEqualTo("name");
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(1, 200));
        assertThat(d.reconnect().initial()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.reconnect().max()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.reconnect().jitter()).isEqualTo(0.2);
        assertThat(d.licence()).isEqualTo("NLOD-2.0");
        assertThat(d.attribution()).startsWith("Contains data under the Norwegian licence for Open Government data");
        assertThat(d.egress()).isEqualTo(Egress.OPEN);
        assertThat(d.kinds()).isEqualTo(Set.of("vessel"));
    }
}
