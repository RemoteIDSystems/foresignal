package ai.foresignal.source.adapters.mesh;

import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.source.formats.meshcore.MeshCoreCommandGuard;
import ai.foresignal.source.formats.meshcore.MeshCoreFrameDecoder;
import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.CommandHandle;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.StreamHandle;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one TCP connection to a MeshCore WiFi companion (prompt 083), shared by everything that talks to the same
 * {@code host:port}: the companion firmware accepts a single client and drops the previous one when another connects,
 * so the {@code meshcore-local} source and prompt 084's {@code meshcore} channel must share this link ({@link
 * MeshCoreLinks}).
 *
 * <ul>
 *   <li><strong>Transport:</strong> 069's socket transport ({@code tcp(…, Framing.RAW, …)}) with
 *       {@link SocketOptions#commandWriter} and this link's {@link MeshCoreCommandGuard}: every frame written passes the
 *       allowlist. The transport of the first attached owner pays for each connect.
 *   <li><strong>Reconnect:</strong> owned here, 5 s doubling to 5 min with jitter, reset after 10 minutes connected.
 *   <li><strong>Commands:</strong> a queue with one command in flight; a command completes on its response code (or
 *       {@code RESP_CODE_ERR}), or fails after 5 s, and the next is sent. {@code CMD_GET_CONTACTS} completes on
 *       {@code RESP_CODE_END_OF_CONTACTS}.
 *   <li><strong>Startup on every connect:</strong> {@code CMD_APP_START} ("Foresignal"), {@code CMD_DEVICE_QUERY},
 *       {@code CMD_GET_CHANNEL} 0–7 (names only), {@code CMD_GET_CONTACTS}, {@code CMD_GET_BATT_AND_STORAGE}, then
 *       {@code CMD_SYNC_NEXT_MESSAGE} until {@code RESP_CODE_NO_MORE_MESSAGES}.
 *   <li><strong>Pushes:</strong> {@code PUSH_CODE_MSG_WAITING} starts the sync loop; {@code PUSH_CODE_ADVERT} (key
 *       only) re-queries the contact with {@code CMD_GET_CONTACT_BY_KEY}.
 *   <li><strong>Keepalive:</strong> {@code CMD_GET_BATT_AND_STORAGE} every 5 minutes, under a 15-minute silence
 *       timeout.
 *   <li><strong>Dispatch:</strong> every frame goes to every attached {@link Listener}, on the connection's thread.
 * </ul>
 */
public final class MeshCoreCompanionLink {

    private static final Logger log = LoggerFactory.getLogger(MeshCoreCompanionLink.class);

    /** The reconnect ladder. */
    public static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(5), Duration.ofMinutes(5), 0.2);

    /** How long one command may wait for its response. */
    public static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(5);

    /** How often the battery is polled, which also keeps the connection from going silent. */
    public static final Duration KEEPALIVE = Duration.ofMinutes(5);

    /** The silence timeout of the connection. */
    public static final Duration SILENCE = Duration.ofMinutes(15);

    /** A connection held this long resets the reconnect ladder. */
    static final Duration LADDER_RESET = Duration.ofMinutes(10);

    private static final ScheduledExecutorService TIMERS = Executors.newSingleThreadScheduledExecutor(
            WorkThreads.named(WorkRole.COLLECT, "meshcore-link-timer"));

    /** Hears the frames and the connection changes of a link. */
    public interface Listener {

        /** One frame from the companion, without its envelope. */
        void onFrame(byte[] frame, Instant receivedAt);

        /** The link connected (before the startup commands are answered). */
        default void onConnected() {}

        /** The link lost its connection; it reconnects on its own. */
        default void onDisconnected(Throwable cause) {}
    }

    private record Owner(SocketTransport transport, Listener listener) {}

    private static final class Pending {
        final byte[] payload;
        final Set<Integer> terminal;
        final List<byte[]> frames = new ArrayList<>();
        final CompletableFuture<List<byte[]>> result = new CompletableFuture<>();
        ScheduledFuture<?> timeout;

        Pending(byte[] payload, Set<Integer> terminal) {
            this.payload = payload;
            this.terminal = terminal;
        }
    }

    private final LanEndpoint endpoint;
    private final Clock clock;
    private final ReconnectPolicy reconnect;
    private final Duration commandTimeout;
    private final MeshCoreCommandGuard guard = new MeshCoreCommandGuard();
    private final Map<String, Owner> owners = new LinkedHashMap<>();
    private final Object commands = new Object();
    private final Deque<Pending> queue = new ArrayDeque<>();
    private final AtomicBoolean syncing = new AtomicBoolean();
    private Pending inFlight;
    private volatile CommandHandle handle;
    private volatile Thread runner;
    private volatile boolean running;
    private volatile String lastError;

    /**
     * Creates an idle link; it connects when the first owner attaches.
     *
     * @param endpoint the companion's pinned {@code tcp://} endpoint
     * @param clock the receive-time clock
     */
    public MeshCoreCompanionLink(LanEndpoint endpoint, Clock clock) {
        this(endpoint, clock, RECONNECT, COMMAND_TIMEOUT);
    }

    /**
     * Creates an idle link with explicit timings (tests).
     *
     * @param reconnect the reconnect ladder
     * @param commandTimeout how long a command waits for its response
     */
    public MeshCoreCompanionLink(LanEndpoint endpoint, Clock clock, ReconnectPolicy reconnect, Duration commandTimeout) {
        if (endpoint.scheme() != LanEndpoint.Scheme.TCP) {
            throw new IllegalArgumentException("a MeshCore companion link needs a tcp:// endpoint");
        }
        this.endpoint = endpoint;
        this.clock = clock;
        this.reconnect = reconnect;
        this.commandTimeout = commandTimeout;
    }

    /** The companion's endpoint. */
    public LanEndpoint endpoint() {
        return endpoint;
    }

    /** The allowlist every frame of this link passes; prompt 084 registers its send commands here. */
    public MeshCoreCommandGuard guard() {
        return guard;
    }

    /** True while connected. */
    public boolean connected() {
        CommandHandle h = handle;
        return h != null && h.isRunning();
    }

    /** The last connection error, or null. */
    public String lastError() {
        return lastError;
    }

    /** The attached owners, in attach order. */
    public synchronized List<String> owners() {
        return List.copyOf(owners.keySet());
    }

    /**
     * Attaches an owner and starts the link if it is idle.
     *
     * @param owner a unique key, for example the source id
     * @param transport the socket transport that pays for connects while this owner is the first
     * @param listener receives every frame
     */
    public synchronized void attach(String owner, SocketTransport transport, Listener listener) {
        owners.put(owner, new Owner(transport, listener));
        if (!running) {
            running = true;
            // Assigned before it starts: the loop runs only while it is the current runner.
            Thread next = WorkThreads.unstarted(WorkRole.COLLECT, "meshcore-link-" + endpoint.host() + "-" + endpoint.port(), this::loop);
            runner = next;
            next.start();
        } else if (connected() && listener != null) {
            listener.onConnected();
        }
    }

    /** Detaches an owner; the link closes when the last one leaves. */
    public void detach(String owner) {
        Thread stop = null;
        synchronized (this) {
            owners.remove(owner);
            if (owners.isEmpty() && running) {
                running = false;
                stop = runner;
            }
        }
        if (stop != null) {
            CommandHandle h = handle;
            if (h != null) {
                h.close();
            }
            stop.interrupt();
            failAll(new IOException("the MeshCore link was closed"));
        }
    }

    /**
     * Queues one command; it is sent when every earlier command is answered.
     *
     * @param payload the command payload (no envelope); its code must be on the guard's allowlist when sent
     * @return the frames that answered it, the terminal one last; fails on a timeout, a refusal or a lost connection
     */
    public CompletableFuture<List<byte[]>> command(byte[] payload) {
        int code = payload == null || payload.length == 0 ? -1 : payload[0] & 0xFF;
        Pending pending = new Pending(payload, terminalCodes(code));
        if (!guard.allows(code)) {
            pending.result.completeExceptionally(new IllegalArgumentException(
                    "MeshCore command 0x" + Integer.toHexString(code) + " is not on the allowlist"));
            return pending.result;
        }
        synchronized (commands) {
            if (handle == null) {
                pending.result.completeExceptionally(new IOException("the MeshCore link is not connected"));
                return pending.result;
            }
            queue.addLast(pending);
        }
        pump();
        return pending.result;
    }

    /** The response codes that end a command of {@code code}; {@code RESP_CODE_ERR} always ends one too. */
    static Set<Integer> terminalCodes(int code) {
        return switch (code) {
            case MeshCoreProtocol.CMD_APP_START -> Set.of(MeshCoreProtocol.RESP_SELF_INFO);
            case MeshCoreProtocol.CMD_DEVICE_QUERY -> Set.of(MeshCoreProtocol.RESP_DEVICE_INFO);
            case MeshCoreProtocol.CMD_GET_CONTACTS -> Set.of(MeshCoreProtocol.RESP_END_OF_CONTACTS);
            case MeshCoreProtocol.CMD_SYNC_NEXT_MESSAGE -> Set.of(MeshCoreProtocol.RESP_CONTACT_MSG_RECV,
                    MeshCoreProtocol.RESP_CHANNEL_MSG_RECV, MeshCoreProtocol.RESP_CONTACT_MSG_RECV_V3,
                    MeshCoreProtocol.RESP_CHANNEL_MSG_RECV_V3, MeshCoreProtocol.RESP_CHANNEL_DATA_RECV,
                    MeshCoreProtocol.RESP_NO_MORE_MESSAGES);
            case MeshCoreProtocol.CMD_GET_DEVICE_TIME -> Set.of(MeshCoreProtocol.RESP_CURR_TIME);
            case MeshCoreProtocol.CMD_GET_BATT_AND_STORAGE -> Set.of(MeshCoreProtocol.RESP_BATT_AND_STORAGE);
            case MeshCoreProtocol.CMD_GET_CONTACT_BY_KEY -> Set.of(MeshCoreProtocol.RESP_CONTACT);
            case MeshCoreProtocol.CMD_GET_CHANNEL -> Set.of(MeshCoreProtocol.RESP_CHANNEL_INFO);
            case MeshCoreProtocol.CMD_SEND_TXT_MSG -> Set.of(MeshCoreProtocol.RESP_SENT);
            // Prompt 084: the firmware answers a channel text with RESP_CODE_OK (MyMesh.cpp writeOKFrame), although
            // docs/companion_protocol.md names PACKET_MSG_SENT; both end the command.
            case MeshCoreProtocol.CMD_SEND_CHANNEL_TXT_MSG -> Set.of(MeshCoreProtocol.RESP_OK, MeshCoreProtocol.RESP_SENT);
            default -> Set.of(MeshCoreProtocol.RESP_OK);
        };
    }

    // ------------------------------------------------------------------ connection loop

    private void loop() {
        int attempt = 0;
        // Prompt 084: a quick detach and re-attach starts a new runner; the old one must stop rather than run on beside
        // it, and must never close the new runner's connection.
        while (running && runner == Thread.currentThread()) {
            SocketTransport transport = currentTransport();
            Instant connectedAt = null;
            Throwable cause = null;
            CommandHandle mine = null;
            if (transport != null) {
                try {
                    CompletableFuture<Throwable> closed = new CompletableFuture<>();
                    MeshCoreFrameDecoder decoder = new MeshCoreFrameDecoder();
                    StreamHandle opened = transport.tcp(endpoint, SocketOptions.commandWriter(guard)
                            .withSilenceTimeout(SILENCE), Framing.RAW, (bytes, receivedAt) -> {
                                for (byte[] frame : decoder.feed(bytes)) {
                                    onFrame(frame, receivedAt);
                                }
                            });
                    if (!(opened instanceof CommandHandle command)) {
                        opened.close();
                        throw new IOException("the socket transport returned a receive-only handle");
                    }
                    opened.onClose(closed::complete);
                    mine = command;
                    synchronized (commands) {
                        if (runner != Thread.currentThread()) {
                            command.close();
                            break;
                        }
                        handle = command;
                    }
                    connectedAt = clock.instant();
                    lastError = null;
                    for (Listener listener : listeners()) {
                        listener.onConnected();
                    }
                    startup();
                    ScheduledFuture<?> keepalive = TIMERS.scheduleAtFixedRate(() -> command(
                            MeshCoreFrames.getBatteryAndStorage()), KEEPALIVE.toMillis(), KEEPALIVE.toMillis(),
                            TimeUnit.MILLISECONDS);
                    try {
                        cause = closed.get();
                    } finally {
                        keepalive.cancel(false);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    cause = e;
                } catch (Exception e) {
                    cause = e;
                }
            }
            synchronized (commands) {
                if (mine != null) {
                    mine.close();
                }
                if (handle == mine) {
                    handle = null;
                }
            }
            if (runner != Thread.currentThread()) {
                break;
            }
            failAll(new IOException("the MeshCore connection ended"
                    + (cause == null ? "" : ": " + cause.getMessage())));
            if (!running) {
                break;
            }
            lastError = cause == null ? "closed by the companion" : cause.getMessage();
            if (connectedAt != null) {
                for (Listener listener : listeners()) {
                    listener.onDisconnected(cause);
                }
                if (Duration.between(connectedAt, clock.instant()).compareTo(LADDER_RESET) >= 0) {
                    attempt = 0;
                }
            }
            Duration delay = reconnect.delay(attempt++, ThreadLocalRandom.current()::nextDouble);
            log.info("MeshCore link {} down ({}); reconnecting in {}", endpoint, lastError, delay);
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /** The startup sequence of every connection. */
    private void startup() {
        command(MeshCoreFrames.appStart());
        command(MeshCoreFrames.deviceQuery());
        for (int i = 0; i < 8; i++) {
            command(MeshCoreFrames.getChannel(i));
        }
        command(MeshCoreFrames.getContacts());
        command(MeshCoreFrames.getBatteryAndStorage());
        sync();
    }

    /** Fetches queued messages until the companion has none, one loop at a time. */
    void sync() {
        if (!syncing.compareAndSet(false, true)) {
            return;
        }
        syncNext();
    }

    private void syncNext() {
        command(MeshCoreFrames.syncNextMessage()).whenComplete((frames, error) -> {
            int last = error != null || frames.isEmpty() ? -1 : MeshCoreFrames.code(frames.get(frames.size() - 1));
            if (error == null && last != MeshCoreProtocol.RESP_NO_MORE_MESSAGES && last != MeshCoreProtocol.RESP_ERR) {
                syncNext();
            } else {
                syncing.set(false);
            }
        });
    }

    /** Dispatches one frame: listeners first, then the in-flight command, then the link's own reactions. */
    void onFrame(byte[] frame, Instant receivedAt) {
        for (Listener listener : listeners()) {
            try {
                listener.onFrame(frame, receivedAt);
            } catch (RuntimeException e) {
                log.warn("A MeshCore link listener failed", e);
            }
        }
        int code = MeshCoreFrames.code(frame);
        if (code >= 0x80) {
            if (code == MeshCoreProtocol.PUSH_MSG_WAITING) {
                sync();
            } else if (code == MeshCoreProtocol.PUSH_ADVERT) {
                byte[] key = MeshCoreFrames.advertKey(frame);
                if (key != null) {
                    command(MeshCoreFrames.getContactByKey(key));
                }
            }
            return;
        }
        Pending done = null;
        synchronized (commands) {
            if (inFlight != null) {
                inFlight.frames.add(frame);
                if (inFlight.terminal.contains(code) || code == MeshCoreProtocol.RESP_ERR) {
                    done = inFlight;
                    inFlight = null;
                }
            }
        }
        if (done != null) {
            if (done.timeout != null) {
                done.timeout.cancel(false);
            }
            done.result.complete(List.copyOf(done.frames));
            pump();
        }
    }

    /** Sends the next queued command when none is in flight. */
    private void pump() {
        Pending next;
        CommandHandle h;
        synchronized (commands) {
            if (inFlight != null || queue.isEmpty() || handle == null) {
                return;
            }
            next = queue.pollFirst();
            inFlight = next;
            h = handle;
            next.timeout = TIMERS.schedule(() -> timeout(next), commandTimeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        try {
            h.send(MeshCoreFrames.wire(next.payload));
        } catch (IOException | RuntimeException e) {
            finish(next, e);
        }
    }

    private void timeout(Pending pending) {
        finish(pending, new TimeoutException("no MeshCore response within " + commandTimeout.toMillis() + " ms"));
    }

    private void finish(Pending pending, Throwable error) {
        boolean cleared = false;
        synchronized (commands) {
            if (inFlight == pending) {
                inFlight = null;
                cleared = true;
            }
        }
        if (pending.timeout != null) {
            pending.timeout.cancel(false);
        }
        pending.result.completeExceptionally(error);
        if (cleared) {
            pump();
        }
    }

    private void failAll(Throwable error) {
        List<Pending> failed = new ArrayList<>();
        synchronized (commands) {
            if (inFlight != null) {
                failed.add(inFlight);
                inFlight = null;
            }
            failed.addAll(queue);
            queue.clear();
        }
        syncing.set(false);
        for (Pending pending : failed) {
            if (pending.timeout != null) {
                pending.timeout.cancel(false);
            }
            pending.result.completeExceptionally(error);
        }
    }

    private synchronized SocketTransport currentTransport() {
        return owners.values().stream().map(Owner::transport).filter(t -> t != null).findFirst().orElse(null);
    }

    private synchronized List<Listener> listeners() {
        return owners.values().stream().map(Owner::listener).filter(l -> l != null).toList();
    }
}
