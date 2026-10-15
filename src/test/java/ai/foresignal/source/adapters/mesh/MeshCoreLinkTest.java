package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.socket.DefaultSocketTransport;
import ai.foresignal.collect.socket.SocketMetrics;
import ai.foresignal.source.formats.meshcore.MeshCoreFrameDecoder;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.CommandHandle;
import ai.foresignal.source.spi.CommandGuard;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The MeshCore companion link and {@code meshcore-local} against a scripted companion on a loopback socket (prompt
 * 083): the startup sequence, the advert re-query and the message sync write exactly the commands of
 * {@code commands.hex}, every one allowlisted; records and bus messages flow; a command off the allowlist is refused
 * without a byte written; the command-writer socket is the only handle with a write method.
 */
class MeshCoreLinkTest {

    private static final List<byte[]> FRAMES = new MeshCoreFrameDecoder().feed(
            MeshFixtures.hexStream("meshcore-local", "companion-tcp.hex"));

    private ServerSocket server;
    private final List<byte[]> received = new CopyOnWriteArrayList<>();
    private final AtomicBoolean pushed = new AtomicBoolean();
    private final List<StreamHandle> handles = new CopyOnWriteArrayList<>();

    @BeforeEach
    void companion() throws IOException {
        server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().start(this::serve);
    }

    @AfterEach
    void close() throws IOException {
        handles.forEach(StreamHandle::close);
        server.close();
    }

    /** A scripted companion: answers each command from the fixture, then pushes an advert, a new advert and mail. */
    private void serve() {
        try (Socket socket = server.accept()) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();
            int syncs = 0;
            while (true) {
                int marker = in.read();
                if (marker < 0) {
                    return;
                }
                int length = in.readUnsignedByte() | in.readUnsignedByte() << 8;
                byte[] payload = in.readNBytes(length);
                byte[] whole = new byte[length + 3];
                whole[0] = (byte) marker;
                whole[1] = (byte) length;
                whole[2] = (byte) (length >> 8);
                System.arraycopy(payload, 0, whole, 3, length);
                received.add(whole);
                switch (payload[0] & 0xFF) {
                    case 0x01 -> send(out, 0);
                    case 0x16 -> send(out, 1);
                    case 0x1F -> send(out, 2 + payload[1]);
                    case 0x04 -> send(out, 10, 11, 12, 13, 14);
                    case 0x14 -> send(out, 15);
                    case 0x1E -> send(out, 18);
                    case 0x0A -> {
                        syncs++;
                        if (syncs == 1) {
                            send(out, 16);
                            if (pushed.compareAndSet(false, true)) {
                                send(out, 17, 19, 20);
                            }
                        } else {
                            send(out, syncs == 2 ? 21 : syncs == 3 ? 22 : 23);
                        }
                    }
                    default -> send(out, -1);
                }
            }
        } catch (IOException e) {
            // closed by the test
        }
    }

    private static void send(OutputStream out, int... indexes) throws IOException {
        for (int index : indexes) {
            byte[] frame = index < 0 ? new byte[] {0x01, 0x01} : FRAMES.get(index);
            out.write(new byte[] {'>', (byte) frame.length, (byte) (frame.length >> 8)});
            out.write(frame);
        }
        out.flush();
    }

    private LanEndpoint endpoint() {
        return new LanEndpoint(LanEndpoint.Scheme.TCP, "127.0.0.1", server.getLocalPort(), InetAddress.getLoopbackAddress());
    }

    private SocketStreamContext context(MeshCoreLocalAdapter adapter) {
        ValidatedParams params = new ValidatedParams(Map.of(MeshCoreLocalAdapter.ENDPOINT,
                URI.create("tcp://127.0.0.1:" + server.getLocalPort())),
                Map.of(MeshCoreLocalAdapter.ENDPOINT, InetAddress.getLoopbackAddress()));
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), Clock.systemUTC(), params);
        return ctx.withSockets(new DefaultSocketTransport(ctx, new SocketMetrics(null, ctx.sourceId()), null));
    }

    @Test
    @DisplayName("meshcore-local: exactly the commands of commands.hex, all allowlisted; records and two bus messages")
    void startupAndSync() throws Exception {
        MeshMessageBus bus = new MeshMessageBus();
        List<MeshTextMessage> heard = new CopyOnWriteArrayList<>();
        bus.subscribe(heard::add);
        MeshCoreLocalAdapter adapter = new MeshCoreLocalAdapter(new MeshCoreLinks(Clock.systemUTC()), bus);
        SocketStreamContext ctx = context(adapter);
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        handles.add(adapter.start(ctx, records::add));

        List<byte[]> expected = MeshFixtures.hexBlocks("meshcore-local", "commands.hex");
        await().atMost(Duration.ofSeconds(15)).until(() -> received.size() >= expected.size() && heard.size() == 2);
        for (int i = 0; i < expected.size(); i++) {
            assertThat(HexFormat.of().formatHex(received.get(i))).as("command %d", i)
                    .isEqualTo(HexFormat.of().formatHex(expected.get(i)));
        }
        assertThat(received).allSatisfy(frame -> assertThat(new ai.foresignal.source.formats.meshcore.MeshCoreCommandGuard()
                .allows(frame[3] & 0xFF)).isTrue());
        assertThat(ctx.charged.get()).as("one connect").isEqualTo(1);
        assertThat(records).extracting(NormalizedRecord::externalId).contains("mc:f7ff142f1f28", "mc:7b065359ae9c",
                "mc:5f6a9ca3f1cc", "mc:d104384f64d9", "mc:74979e2bf73d");
        NormalizedRecord repeater = records.stream().filter(r -> r.externalId().equals("mc:5f6a9ca3f1cc"))
                .reduce((a, b) -> b).orElseThrow();
        assertThat(repeater.payload()).containsEntry("advertStatus", RepeaterHealth.OK).containsKey("lastAdvertAt");
        assertThat(heard).extracting(MeshTextMessage::text).containsExactly("Synthetic Hiker: #E synthetic report",
                "synthetic direct");
        assertThat(heard).allSatisfy(m -> assertThat(m.sourceId()).isEqualTo(MeshCoreLocalAdapter.ID));
    }

    @Test
    @DisplayName("the link refuses a command off the allowlist before anything is written; 084 may register a send")
    void refusesOffList() throws Exception {
        MeshCoreCompanionLink link = new MeshCoreCompanionLink(endpoint(), Clock.systemUTC());
        SocketStreamContext ctx = new SocketStreamContext(new MeshCoreLocalAdapter(null, null).descriptor(),
                Clock.systemUTC(), ValidatedParams.empty());
        ctx.withSockets(new DefaultSocketTransport(ctx, new SocketMetrics(null, ctx.sourceId()), null));
        List<byte[]> frames = new CopyOnWriteArrayList<>();
        link.attach("test", ctx.sockets(), (frame, at) -> frames.add(frame));
        try {
            await().atMost(Duration.ofSeconds(10)).until(() -> received.size() >= 17);
            Thread.sleep(300);
            int before = received.size();
            for (int code : new int[] {0x13, 0x33, 0x06, 0x20, 0x1A, 0x27, 0x34, 0x03}) {
                assertThatThrownBy(() -> link.command(new byte[] {(byte) code}).get(2, TimeUnit.SECONDS))
                        .isInstanceOf(ExecutionException.class);
            }
            Thread.sleep(300);
            assertThat(received).hasSize(before);
            List<byte[]> answer = link.command(new byte[] {(byte) MeshCoreProtocol.CMD_GET_BATT_AND_STORAGE})
                    .get(5, TimeUnit.SECONDS);
            assertThat(answer).singleElement().satisfies(f -> assertThat(f[0]).isEqualTo((byte) 0x0C));
            link.guard().allowSend(MeshCoreProtocol.CMD_SEND_CHANNEL_TXT_MSG);
            assertThat(link.guard().allows(0x03)).isTrue();
            assertThat(link.connected()).isTrue();
            assertThat(link.owners()).containsExactly("test");
            assertThat(frames).isNotEmpty();
        } finally {
            link.detach("test");
        }
        assertThat(link.owners()).isEmpty();
    }

    @Test
    @DisplayName("a command-writer socket returns a CommandHandle whose guard refuses before writing; plain TCP has none")
    void commandWriterSocket() throws Exception {
        SocketStreamContext ctx = new SocketStreamContext(new MeshCoreLocalAdapter(null, null).descriptor(),
                Clock.systemUTC(), ValidatedParams.empty());
        DefaultSocketTransport transport = new DefaultSocketTransport(ctx, new SocketMetrics(null, ctx.sourceId()), null);
        List<String> refusals = new ArrayList<>();
        CommandGuard onlyAppStart = frame -> {
            if (frame[3] != 0x01) {
                refusals.add(HexFormat.of().formatHex(frame));
                throw new CommandGuard.CommandRefusedException("no");
            }
        };
        StreamHandle handle = transport.tcp(endpoint(), SocketOptions.commandWriter(onlyAppStart), Framing.RAW,
                (frame, at) -> {});
        handles.add(handle);
        assertThat(handle).isInstanceOf(CommandHandle.class);
        CommandHandle command = (CommandHandle) handle;
        assertThatThrownBy(() -> command.send(new byte[] {'<', 1, 0, 0x13})).isInstanceOf(
                CommandGuard.CommandRefusedException.class);
        command.send(new byte[] {'<', 1, 0, 0x01});
        await().atMost(Duration.ofSeconds(5)).until(() -> received.size() == 1);
        assertThat(received.get(0)).containsExactly('<', 1, 0, 0x01);
        assertThat(refusals).containsExactly("3c010013");
        handle.close();
        assertThatThrownBy(() -> command.send(new byte[] {'<', 1, 0, 0x01})).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("the link needs a tcp:// endpoint; the terminal codes of each read command")
    void basics() {
        assertThatThrownBy(() -> new MeshCoreCompanionLink(new LanEndpoint(LanEndpoint.Scheme.UDP, "0.0.0.0", 1,
                InetAddress.getLoopbackAddress()), Clock.systemUTC())).isInstanceOf(IllegalArgumentException.class);
        assertThat(MeshCoreCompanionLink.terminalCodes(0x04)).containsExactly(MeshCoreProtocol.RESP_END_OF_CONTACTS);
        assertThat(MeshCoreCompanionLink.terminalCodes(0x0A)).contains(0x07, 0x08, 0x10, 0x11, 0x0A);
        // Prompt 084: a channel text is answered by RESP_CODE_OK or RESP_SENT; both end the command.
        assertThat(MeshCoreCompanionLink.terminalCodes(0x03))
                .containsExactlyInAnyOrder(MeshCoreProtocol.RESP_OK, MeshCoreProtocol.RESP_SENT);
        MeshCoreLinks links = new MeshCoreLinks(Clock.systemUTC());
        assertThat(links.link(endpoint())).isSameAs(links.link(endpoint()));
        assertThat(links.size()).isEqualTo(1);
        assertThat(new MeshCoreCompanionLink(endpoint(), Clock.systemUTC()).command(new byte[] {0x01}))
                .isCompletedExceptionally();
    }
}
