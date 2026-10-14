package ai.foresignal.notify.mesh;

import ai.foresignal.collect.socket.DefaultSocketTransport;
import ai.foresignal.collect.socket.SocketMetrics;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.MeshCoreChannel;
import ai.foresignal.source.adapters.mesh.MeshCoreCompanionLink;
import ai.foresignal.source.adapters.mesh.MeshCoreLinks;
import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.SocketTransport;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sends {@code meshcore} channel transmissions over 083's shared companion link (prompt 084) and reads channel slots
 * for the public-slot check ({@link MeshCoreSlotProbe}).
 *
 * <ul>
 *   <li><strong>Attachment:</strong> each enabled channel attaches to the link of its pinned {@code host:port} as
 *       {@code channel:<id>} (the companion accepts one TCP client, so {@code meshcore-local} and every channel share
 *       it) and registers its send commands with the link's {@code MeshCoreCommandGuard}: 0x02, 0x03 and 0x3E.
 *   <li><strong>Slot check on every (re)connect:</strong> {@code CMD_GET_CHANNEL} for the channel's slot; nothing is
 *       sent until it reports a private slot (named, not {@code Public}, not {@code #…}, a non-zero secret).
 *   <li><strong>Sends:</strong> the command waits for its terminal response ({@code RESP_CODE_OK} for a channel text
 *       or datagram, {@code RESP_CODE_SENT} for a direct message); {@code RESP_CODE_ERR} is a failure.
 *   <li><strong>Direct-message acknowledgement</strong> ({@code requireAck}): {@code RESP_CODE_SENT} carries the
 *       expected ACK code (u32) and the suggested timeout (ms); the sender waits that long (at least 2 s, at most 60 s)
 *       for {@code PUSH_CODE_SEND_CONFIRMED} (0x82) with the same code, retries once with {@code attempt=1} when the
 *       airtime governor admits the retry, and otherwise answers {@code UNACKED}.
 * </ul>
 */
@Component
public class MeshCoreSender implements MeshCoreSlotProbe {

    private static final Logger log = LoggerFactory.getLogger(MeshCoreSender.class);

    /** How long a send waits for a connection being made. */
    static final Duration CONNECT_WAIT = Duration.ofSeconds(5);

    /** How long a command waits for its terminal response here (the link's own timeout is 5 s). */
    static final Duration COMMAND_WAIT = Duration.ofSeconds(10);

    /** The shortest acknowledgement wait. */
    static final Duration MIN_ACK_WAIT = Duration.ofSeconds(2);

    /** How long a connect's own slot reads may take before the slot is asked for directly. */
    static final Duration SLOT_STARTUP_WAIT = Duration.ofSeconds(3);

    /** The longest acknowledgement wait. */
    static final Duration MAX_ACK_WAIT = Duration.ofSeconds(60);

    private final MeshCoreLinks links;
    private final ObjectProvider<OutboundUrlPolicy> policy;
    private final ObjectProvider<MeterRegistry> meters;
    private final Clock clock;
    private final Map<UUID, Attachment> attachments = new ConcurrentHashMap<>();

    /**
     * Creates the sender.
     *
     * @param links 083's companion links
     * @param policy re-checks the companion's address on connect, resolved lazily
     * @param meters the socket counters' registry, resolved lazily
     * @param clock the clock
     */
    @Autowired
    public MeshCoreSender(MeshCoreLinks links, ObjectProvider<OutboundUrlPolicy> policy,
            ObjectProvider<MeterRegistry> meters, Clock clock) {
        this.links = links;
        this.policy = policy;
        this.meters = meters;
        this.clock = clock;
    }

    /** One channel's attachment to its companion link. */
    final class Attachment implements MeshCoreCompanionLink.Listener {

        final UUID channelId;
        final LanEndpoint endpoint;
        final int index;
        final MeshCoreCompanionLink link;
        final Map<Long, CompletableFuture<Void>> acks = new ConcurrentHashMap<>();
        final Map<Long, Instant> confirmed = new ConcurrentHashMap<>();
        volatile CompletableFuture<SlotCheck> slot = new CompletableFuture<>();

        Attachment(UUID channelId, LanEndpoint endpoint, int index, MeshCoreCompanionLink link) {
            this.channelId = channelId;
            this.endpoint = endpoint;
            this.index = index;
            this.link = link;
        }

        @Override
        public void onFrame(byte[] frame, Instant receivedAt) {
            int code = MeshCoreFrames.code(frame);
            if (code == MeshCoreProtocol.RESP_CHANNEL_INFO) {
                // The link's startup reads slots 0-7 after every connect; take ours from it.
                MeshCoreFrames.ChannelSlot info = MeshCoreFrames.channelSlot(frame);
                if (info != null && info.index() == index) {
                    settle(slot, classify(index, frame));
                }
                return;
            }
            if (code != MeshCoreProtocol.PUSH_SEND_CONFIRMED || frame.length < 5) {
                return;
            }
            long ack = u32(frame, 1);
            CompletableFuture<Void> waiting = acks.remove(ack);
            if (waiting != null) {
                waiting.complete(null);
            } else {
                confirmed.put(ack, receivedAt);
                confirmed.values().removeIf(at -> at.isBefore(receivedAt.minus(MAX_ACK_WAIT)));
            }
        }

        @Override
        public void onConnected() {
            CompletableFuture<SlotCheck> next = new CompletableFuture<>();
            slot = next;
            // A fresh connection reads every slot in the link's startup (onFrame settles it); an attach to a link
            // that is already up does not, so ask for the slot when nothing arrived shortly.
            Thread.ofVirtual().name("meshcore-slot-" + channelId).start(() -> {
                try {
                    next.get(SLOT_STARTUP_WAIT.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException | TimeoutException e) {
                    link.command(MeshCoreFrames.getChannel(index)).whenComplete((frames, error) -> settle(next,
                            error != null ? new SlotCheck(Status.UNREACHABLE, "the companion did not answer"
                                    + " CMD_GET_CHANNEL: " + error.getMessage()) : classify(index, lastFrame(frames))));
                }
            });
        }

        private void settle(CompletableFuture<SlotCheck> future, SlotCheck check) {
            if (future.complete(check) && check.status() == Status.PUBLIC) {
                log.warn("meshcore channel {}: slot {} on {} is not private ({}); nothing will be sent",
                        channelId, index, endpoint, check.reason());
            }
        }

        @Override
        public void onDisconnected(Throwable cause) {
            slot = new CompletableFuture<>();
        }
    }

    /**
     * Sends one transmission of {@code channelId}.
     *
     * @param config the channel's config ({@code companionUrl}, {@code channelIndex})
     * @param retryAdmitted asks the airtime governor for a direct-message retry
     * @return 200 with {@code OK}, {@code SENT}, {@code ACKED} or {@code UNACKED}
     * @throws IOException when the companion is unreachable, the slot is not private, or the companion refuses
     */
    public ChannelResponse send(UUID channelId, Map<String, Object> config, PreparedRequest.Mesh m,
            BooleanSupplier retryAdmitted) throws IOException {
        Attachment a = attach(channelId, config);
        awaitConnected(a.link);
        SlotCheck slot = await(a.slot, Duration.ofSeconds(10), "the slot check");
        if (slot.status() != Status.PRIVATE) {
            throw new IOException("the MeshCore slot " + a.index + " is not verified private: " + slot.reason());
        }
        List<byte[]> frames = command(a.link, m.payload());
        byte[] last = lastFrame(frames);
        if (!"dm".equals(m.topic())) {
            return new ChannelResponse(200, Map.of(), "OK");
        }
        if (MeshCoreFrames.code(last) != MeshCoreProtocol.RESP_SENT || last.length < 10) {
            throw new IOException("the companion did not answer the direct message with RESP_CODE_SENT");
        }
        if (!m.requireAck()) {
            return new ChannelResponse(200, Map.of(), "SENT");
        }
        if (awaitAck(a, last)) {
            return new ChannelResponse(200, Map.of(), "ACKED");
        }
        if (!retryAdmitted.getAsBoolean()) {
            return new ChannelResponse(200, Map.of(), MeshCoreChannel.UNACKED);
        }
        byte[] retry = m.payload().clone();
        retry[2] = 1;
        byte[] again = lastFrame(command(a.link, retry));
        if (MeshCoreFrames.code(again) == MeshCoreProtocol.RESP_SENT && again.length >= 10 && awaitAck(a, again)) {
            return new ChannelResponse(200, Map.of(), "ACKED");
        }
        return new ChannelResponse(200, Map.of(), MeshCoreChannel.UNACKED);
    }

    @Override
    public SlotCheck check(LanEndpoint endpoint, int index) {
        if (index == 0) {
            return new SlotCheck(Status.PUBLIC, "slot 0 is the Public channel");
        }
        MeshCoreCompanionLink link = links.link(endpoint);
        String owner = "probe:" + UUID.randomUUID();
        link.attach(owner, transport(owner), null);
        try {
            awaitConnected(link);
            return classify(index, lastFrame(command(link, MeshCoreFrames.getChannel(index))));
        } catch (IOException e) {
            return new SlotCheck(Status.UNREACHABLE, e.getMessage());
        } finally {
            link.detach(owner);
        }
    }

    /** Detaches {@code channelId} from its link (the channel was disabled, deleted or moved). */
    public void detach(UUID channelId) {
        Attachment a = attachments.remove(channelId);
        if (a != null) {
            a.link.detach(owner(channelId));
            a.acks.values().forEach(f -> f.completeExceptionally(new IOException("detached")));
        }
    }

    /**
     * The verdict on a {@code RESP_CODE_CHANNEL_INFO} frame for slot {@code index}: public for slot 0, an empty slot,
     * {@code Public}, a {@code #hashtag} name or an all-zero secret.
     */
    public static SlotCheck classify(int index, byte[] frame) {
        if (index == 0) {
            return new SlotCheck(Status.PUBLIC, "slot 0 is the Public channel");
        }
        MeshCoreFrames.ChannelSlot slot = MeshCoreFrames.channelSlot(frame);
        if (slot == null) {
            return new SlotCheck(Status.UNREACHABLE, "the companion did not answer with channel information"
                    + " (a firmware without CMD_GET_CHANNEL, or slot " + index + " does not exist)");
        }
        if (slot.name() == null || slot.name().isBlank()) {
            return new SlotCheck(Status.PUBLIC, "slot " + index + " is empty");
        }
        if (slot.name().strip().equalsIgnoreCase("Public")) {
            return new SlotCheck(Status.PUBLIC, "slot " + index + " is the Public channel");
        }
        if (slot.name().strip().startsWith("#")) {
            return new SlotCheck(Status.PUBLIC, "slot " + index + " is the hashtag channel " + slot.name()
                    + ", whose key anyone can derive from its name");
        }
        if (slot.zeroSecret()) {
            return new SlotCheck(Status.PUBLIC, "slot " + index + " has an all-zero secret");
        }
        return new SlotCheck(Status.PRIVATE, "slot " + index + " (" + slot.name() + ") is private");
    }

    private Attachment attach(UUID channelId, Map<String, Object> config) throws IOException {
        Object url = config.get("companionUrl");
        MeshLanUrls.Outcome outcome = MeshLanUrls.pin(url == null ? null : String.valueOf(url), Set.of("tcp"),
                policy.getIfAvailable());
        if (!(outcome instanceof MeshLanUrls.Pinned pinned)) {
            throw new IOException("the companion is refused: " + ((MeshLanUrls.Refused) outcome).reason());
        }
        int index = ((Number) config.get(MeshChannels.CHANNEL_INDEX)).intValue();
        Attachment current = attachments.get(channelId);
        if (current != null && current.endpoint.equals(pinned.endpoint()) && current.index == index) {
            return current;
        }
        synchronized (this) {
            current = attachments.get(channelId);
            if (current != null && current.endpoint.equals(pinned.endpoint()) && current.index == index) {
                return current;
            }
            if (current != null) {
                detach(channelId);
            }
            MeshCoreCompanionLink link = links.link(pinned.endpoint());
            link.guard().allowSend(MeshCoreProtocol.CMD_SEND_TXT_MSG);
            link.guard().allowSend(MeshCoreProtocol.CMD_SEND_CHANNEL_TXT_MSG);
            link.guard().allowSend(MeshCoreProtocol.CMD_SEND_CHANNEL_DATA);
            Attachment a = new Attachment(channelId, pinned.endpoint(), index, link);
            attachments.put(channelId, a);
            link.attach(owner(channelId), transport(owner(channelId)), a);
            return a;
        }
    }

    private SocketTransport transport(String owner) {
        String id = "channel-meshcore-" + owner.replace(':', '-');
        return new DefaultSocketTransport(new MeshChannelStreamContext(id, clock),
                new SocketMetrics(meters == null ? null : meters.getIfAvailable(), id), policy.getIfAvailable());
    }

    private static String owner(UUID channelId) {
        return "channel:" + channelId;
    }

    private boolean awaitAck(Attachment a, byte[] sent) {
        long code = u32(sent, 2);
        long suggested = u32(sent, 6);
        Duration wait = Duration.ofMillis(Math.max(MIN_ACK_WAIT.toMillis(), Math.min(MAX_ACK_WAIT.toMillis(),
                suggested)));
        if (a.confirmed.remove(code) != null) {
            return true;
        }
        CompletableFuture<Void> waiting = new CompletableFuture<>();
        a.acks.put(code, waiting);
        if (a.confirmed.remove(code) != null) {
            a.acks.remove(code);
            return true;
        }
        try {
            waiting.get(wait.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException | TimeoutException e) {
            return false;
        } finally {
            a.acks.remove(code);
        }
    }

    private static List<byte[]> command(MeshCoreCompanionLink link, byte[] payload) throws IOException {
        List<byte[]> frames = await(link.command(payload), COMMAND_WAIT, "the companion's answer");
        byte[] last = lastFrame(frames);
        if (MeshCoreFrames.code(last) == MeshCoreProtocol.RESP_ERR) {
            int error = last.length > 1 ? last[1] & 0xFF : -1;
            throw new IOException("the companion refused command 0x" + Integer.toHexString(payload[0] & 0xFF)
                    + " (error " + error + ")");
        }
        return frames;
    }

    private static void awaitConnected(MeshCoreCompanionLink link) throws IOException {
        long deadline = System.nanoTime() + CONNECT_WAIT.toNanos();
        while (!link.connected()) {
            if (System.nanoTime() > deadline) {
                throw new IOException("the MeshCore companion " + link.endpoint() + " is not connected"
                        + (link.lastError() == null ? "" : " (" + link.lastError() + ")"));
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("interrupted", e);
            }
        }
    }

    private static <T> T await(CompletableFuture<T> future, Duration wait, String what) throws IOException {
        try {
            return future.get(wait.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IOException(what + " failed: " + cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new IOException("no answer for " + what + " within " + wait.toSeconds() + " s");
        }
    }

    private static byte[] lastFrame(List<byte[]> frames) {
        return frames == null || frames.isEmpty() ? new byte[0] : frames.get(frames.size() - 1);
    }

    private static long u32(byte[] frame, int offset) {
        return (frame[offset] & 0xFFL) | (frame[offset + 1] & 0xFFL) << 8 | (frame[offset + 2] & 0xFFL) << 16
                | (frame[offset + 3] & 0xFFL) << 24;
    }
}
