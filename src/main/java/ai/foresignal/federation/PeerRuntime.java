package ai.foresignal.federation;

import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.store.FetchTrigger;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The PULL runtime of federation peers (prompt 113): starts one {@link PeerSession} per enabled {@code PULL} peer
 * when the streaming supervisor (010) starts {@code peer-{id}}, hands TRACK items to {@code peer-{id}-trk}'s sink
 * while that source runs, and carries resync requests to running sessions.
 */
@Component
public class PeerRuntime {

    /** The lag gauge. */
    public static final String LAG_METRIC = "foresignal.federation.lag_seq";

    private final PeerStates states;
    private final FederationIngestor ingestor;
    private final FederationWriter writer;
    private final FederationIdentity identity;
    private final CredentialCipher cipher;
    private final JsonMapper json;
    private final IngestQueue queue;
    private final SourceRegistry registry;
    private final OutboundUrlPolicy policy;
    private final ObjectProvider<MeterRegistry> meters;
    private final Clock clock;

    private final Map<String, PeerSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, RecordSink> trackSinks = new ConcurrentHashMap<>();
    private final Map<String, Boolean> resyncRequests = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> lag = new ConcurrentHashMap<>();

    /** Creates the runtime. */
    public PeerRuntime(PeerStates states, FederationIngestor ingestor, FederationWriter writer,
            FederationIdentity identity, CredentialCipher cipher, JsonMapper json, IngestQueue queue,
            SourceRegistry registry, OutboundUrlPolicy policy, ObjectProvider<MeterRegistry> meters, Clock clock) {
        this.states = states;
        this.ingestor = ingestor;
        this.writer = writer;
        this.identity = identity;
        this.cipher = cipher;
        this.json = json;
        this.queue = queue;
        this.registry = registry;
        this.policy = policy;
        this.meters = meters;
        this.clock = clock;
    }

    /** Starts the session of {@code peerId} on its {@code peer-{id}} stream. */
    public StreamHandle startSession(String peerId, StreamContext ctx, RecordSink sink) {
        PeerSession session = new PeerSession(this, peerId, ctx, sink);
        PeerSession previous = sessions.put(peerId, session);
        if (previous != null) {
            previous.close();
        }
        session.start();
        return session;
    }

    /** Registers {@code peer-{id}-trk}'s sink until the returned handle is closed. */
    public StreamHandle startTrack(String peerId, RecordSink sink, StreamContext ctx) {
        trackSinks.put(peerId, sink);
        ctx.connected();
        return new StreamHandle() {
            private volatile boolean open = true;
            private volatile Consumer<Throwable> callback = e -> {};

            @Override
            public void close() {
                open = false;
                trackSinks.remove(peerId, sink);
            }

            @Override
            public boolean isRunning() {
                return open;
            }

            @Override
            public void onClose(Consumer<Throwable> value) {
                callback = value == null ? e -> {} : value;
            }
        };
    }

    /** Called by a session when it ends. */
    void ended(String peerId, PeerSession session) {
        sessions.remove(peerId, session);
    }

    /**
     * Asks {@code peerId}'s session to re-snapshot and then tombstone local heads absent from the snapshot; a
     * running session closes its stream and resyncs at once, a stopped one at its next start.
     *
     * @param explicit an operator's request: the shrink guard of the absent-head reconciliation is released
     */
    public void requestResync(String peerId, boolean explicit) {
        resyncRequests.merge(peerId, explicit, Boolean::logicalOr);
        PeerSession session = sessions.get(peerId);
        if (session != null) {
            session.interrupt();
        }
    }

    /** Takes a pending resync request: null when none, else whether it was explicit. */
    Boolean takeResync(String peerId) {
        return resyncRequests.remove(peerId);
    }

    /** Stops the session of {@code peerId}, if one runs. */
    public void stop(String peerId) {
        PeerSession session = sessions.remove(peerId);
        if (session != null) {
            session.close();
        }
    }

    /** True when a session of {@code peerId} runs. */
    public boolean running(String peerId) {
        PeerSession session = sessions.get(peerId);
        return session != null && session.isRunning();
    }

    /** The decrypted remote token of {@code peer}, or null. */
    String token(PeerEntity peer) {
        if (peer.getTokenCiphertext() == null || peer.getTokenNonce() == null) {
            return null;
        }
        return cipher.decryptToString(new Encrypted(peer.getTokenCiphertext(), peer.getTokenNonce()));
    }

    /** The ingest context of {@code peer}. */
    FederationIngestor.Context context(PeerEntity peer, PeerSelection selection, boolean snapshot) {
        String trk = PeerDescriptors.trackId(peer.getId());
        return new FederationIngestor.Context(peer.getId(), PeerDescriptors.standardId(peer.getId()),
                registry.contains(trk) ? trk : null, selection, peer.isSameOperator(), states.aliveSources(),
                snapshot);
    }

    /**
     * Hands applied records over: those of {@code ownSource} to the stream's sink, TRACK records to
     * {@code peer-{id}-trk}'s sink while it runs, everything else (another owner, or a stopped track source)
     * straight to the writer.
     */
    void deliver(String peerId, String ownSource, RecordSink sink, List<FederationIngestor.Mapped> mapped) {
        String trk = PeerDescriptors.trackId(peerId);
        RecordSink trackSink = trackSinks.get(peerId);
        List<FederationIngestor.Mapped> direct = new ArrayList<>();
        for (FederationIngestor.Mapped m : mapped) {
            if (m.sourceId().equals(ownSource)) {
                sink.accept(m.record());
            } else if (m.sourceId().equals(trk) && trackSink != null) {
                trackSink.accept(m.record());
            } else {
                direct.add(m);
            }
        }
        if (!direct.isEmpty()) {
            writer.submit(FederationWriter.group(direct), FetchTrigger.STREAM, "peer:" + peerId, false);
        }
    }

    /** Records the lag ({@code remote watermark - last applied seq}) of {@code peerId}. */
    void lag(String peerId, long watermark, long lastSeq) {
        AtomicLong value = lag.computeIfAbsent(peerId, id -> {
            AtomicLong created = new AtomicLong();
            MeterRegistry registry = meters == null ? null : meters.getIfAvailable();
            if (registry != null) {
                Gauge.builder(LAG_METRIC, created, AtomicLong::get).tag("peer", id).register(registry);
            }
            return created;
        });
        value.set(Math.max(0, watermark - lastSeq));
    }

    /** The current lag of {@code peerId}, or null when unknown. */
    public Long lagOf(String peerId) {
        AtomicLong value = lag.get(peerId);
        return value == null ? null : value.get();
    }

    PeerStates states() {
        return states;
    }

    FederationIngestor ingestor() {
        return ingestor;
    }

    FederationWriter writer() {
        return writer;
    }

    FederationIdentity identity() {
        return identity;
    }

    JsonMapper json() {
        return json;
    }

    IngestQueue queue() {
        return queue;
    }

    SourceRegistry registry() {
        return registry;
    }

    OutboundUrlPolicy policy() {
        return policy;
    }

    Clock clock() {
        return clock;
    }

    /** The ids of the running sessions. */
    public Set<String> sessionIds() {
        return Set.copyOf(sessions.keySet());
    }
}
