package ai.foresignal.federation;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.collect.InBackoffException;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.HttpStream;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.store.FetchTrigger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

/**
 * One PULL replication of a peer (prompt 113), run on the {@code peer-{id}} stream the supervisor (010) starts:
 *
 * <ol>
 *   <li><strong>{@code /info}:</strong> the JWS is verified against the peer's JWKS and the pinned key (pinned on
 *       the first verified contact, TOFU). A mismatch, an unverifiable signature or a peer that is this instance
 *       stops ingest: state {@code failing}, health reason {@code PEER_KEY_MISMATCH}.
 *   <li><strong>Snapshot</strong> on the first start, on a resync request or a remote {@code reset}
 *       ({@code resync_required}); after a resync, local heads absent from the snapshot are tombstoned through the
 *       writer's {@code TombstoneAbsent} command. With {@code selection.backfill} set, the first start pages that
 *       much history from {@code /changes} instead.
 *   <li><strong>Catch-up</strong> via {@code /changes} pages from the last applied seq after every gap.
 *   <li><strong>SSE</strong> from the last applied seq; reconnects back off 5 s → 5 min, and a {@code Retry-After}
 *       holds the source in backoff (005), which the session waits out.
 * </ol>
 *
 * <p>A pass ends when the stream ends or fails; the supervisor then reconnects on the descriptor's ladder
 * (5 s → 5 min) or after the backoff a {@code Retry-After} set.
 *
 * <p>Every request is a budgeted request of the peer's source (SSE connects count, snapshot and catch-up pages are
 * budgeted). The last applied seq is kept in the source's adapter state ({@value #CURSOR_KEY}) and mirrored on the
 * peer row.
 */
final class PeerSession implements StreamHandle, Runnable {

    /** The adapter-state key of the last applied remote seq. */
    static final String CURSOR_KEY = "peer:lastSeq";

    /** The first reconnect delay (the descriptor's ladder). */
    static final Duration FIRST_DELAY = Duration.ofSeconds(5);

    /** The longest reconnect delay. */
    static final Duration MAX_DELAY = Duration.ofMinutes(5);

    /** Persist the cursor after this many applied events, and at least every 30 s while events arrive. */
    static final int CURSOR_PERSIST_EVERY = 1_000;

    private final PeerRuntime runtime;
    private final String peerId;
    private final StreamContext ctx;
    private final RecordSink sink;
    private final String ownSource;

    private volatile boolean running = true;
    private volatile HttpStream current;
    private volatile Consumer<Throwable> onClose = e -> {};
    private volatile boolean interrupted;
    private Thread thread;

    private long cursor = -1;
    private long persistedCursor = -1;
    private int sincePersist;
    private Instant persistedAt = Instant.EPOCH;

    PeerSession(PeerRuntime runtime, String peerId, StreamContext ctx, RecordSink sink) {
        this.runtime = runtime;
        this.peerId = peerId;
        this.ctx = ctx;
        this.sink = sink;
        this.ownSource = PeerDescriptors.standardId(peerId);
    }

    void start() {
        thread = Thread.ofVirtual().name("foresignal-peer-" + peerId).start(this);
    }

    /** Thrown when the peer's key does not match the pinned one. */
    static final class KeyMismatch extends Exception {
        KeyMismatch(String message) {
            super(message);
        }
    }

    /** Thrown when the peer asks for a resync (410 or SSE {@code reset}). */
    static final class ResyncRequired extends Exception {
        ResyncRequired(String message) {
            super(message);
        }
    }

    /**
     * One pass. The supervisor reconnects when it ends (the descriptor's 5 s → 5 min ladder, or the backoff a
     * {@code Retry-After} set), so a pass reports its failure through {@link #onClose} and never loops itself.
     */
    @Override
    public void run() {
        Throwable failure = null;
        try {
            replicate();
        } catch (ResyncRequired e) {
            runtime.requestResync(peerId, false);
        } catch (KeyMismatch e) {
            failure = new AdapterException(PeerService.REASON_KEY_MISMATCH + ": " + e.getMessage());
            runtime.states().update(peerId, PeerService.STATE_FAILING, PeerService.REASON_KEY_MISMATCH, null, null,
                    e.getMessage());
        } catch (InBackoffException e) {
            failure = e;
        } catch (Exception e) {
            if (running && !interrupted) {
                failure = new AdapterException(PeerService.REASON_UNREACHABLE + ": " + describe(e), e);
                runtime.states().update(peerId, PeerService.STATE_FAILING, PeerService.REASON_UNREACHABLE,
                        cursor < 0 ? null : cursor, null, describe(e));
            }
        } finally {
            persistCursor(true);
            running = false;
            runtime.ended(peerId, this);
            try {
                onClose.accept(failure);
            } catch (RuntimeException ignored) {
                // the supervisor's callback must not break teardown
            }
        }
    }

    /** One pass: check the key, snapshot or catch up, then stream until the connection ends. */
    private void replicate() throws Exception {
        PeerEntity peer = runtime.states().find(peerId).orElse(null);
        if (peer == null || !peer.isEnabled()) {
            running = false;
            return;
        }
        OutboundUrlPolicy.Result allowed = runtime.policy().check(peer.getBaseUrl(),
                OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        if (!allowed.allowed()) {
            throw new AdapterException("the outbound URL policy refuses " + peer.getBaseUrl() + " now");
        }
        PeerRemote remote = new PeerRemote(ctx.http(), peer.getBaseUrl(), runtime.token(peer), runtime.json());
        checkKey(remote, peer);
        PeerSelection selection = PeerSelection.parse(peer.getSelection(), "selection", new ArrayList<>());
        FederationIngestor.Context context = runtime.context(peer, selection, false);
        if (cursor < 0) {
            cursor = ctx.state().get(CURSOR_KEY).map(Long::parseLong).orElse(-1L);
            persistedCursor = cursor;
        }
        Boolean resync = runtime.takeResync(peerId);
        if (cursor < 0 || resync != null) {
            if (cursor < 0 && resync == null && selection.backfillDuration() != null) {
                backfill(remote, selection, context);
            } else {
                snapshot(remote, selection, context, resync != null, Boolean.TRUE.equals(resync));
            }
            persistCursor(true);
        }
        catchUp(remote, selection, context);
        runtime.states().update(peerId, PeerService.STATE_STREAMING, null, cursor, null, null);
        ctx.connected();
        stream(remote, selection, context);
    }

    /** Verifies {@code /info} and the pinned key; pins on the first verified contact. */
    private void checkKey(PeerRemote remote, PeerEntity peer) throws Exception {
        runtime.states().update(peerId, PeerService.STATE_CONNECTING, peer.getHealthReason(), null, null,
                peer.getLastError());
        PeerRemote.Verified verified = remote.verifiedInfo();
        PeerKeys.Verification v = verified.verification();
        String remoteId = verified.info().instanceId();
        if (!v.signatureValid()) {
            throw new KeyMismatch("the /info signature does not verify: " + v.error());
        }
        if (remoteId != null && remoteId.equals(runtime.identity().instanceId())) {
            throw new KeyMismatch("the peer is this instance");
        }
        if (peer.getRemoteInstanceId() != null && remoteId != null && !peer.getRemoteInstanceId().equals(remoteId)) {
            throw new KeyMismatch("the peer's instance id changed from " + peer.getRemoteInstanceId() + " to "
                    + remoteId);
        }
        String pinned = runtime.states().pinIfUnset(peerId, v.kid(), v.key(), remoteId);
        if (pinned == null || !pinned.equals(v.kid())) {
            throw new KeyMismatch("the peer signs with key " + v.kid() + ", but " + pinned + " is pinned");
        }
    }

    /** Replays the peer's snapshot; after a resync, tombstones the local heads it did not list. */
    private void snapshot(PeerRemote remote, PeerSelection selection, FederationIngestor.Context context,
            boolean resync, boolean explicit) throws Exception {
        runtime.states().update(peerId, PeerService.STATE_RESYNCING, PeerService.REASON_RESYNC, null, null, null);
        Instant started = runtime.clock().instant();
        FederationIngestor.Context replay = context.withSnapshot(true);
        Long atSeq = null;
        String next = null;
        do {
            JsonNode page = remote.snapshot(atSeq, next, selection);
            atSeq = page.path("atSeq").asLong(0);
            next = page.path("nextCursor").isString() ? page.path("nextCursor").asString() : null;
            FederationIngestor.Result result = runtime.ingestor().process(PeerRemote.items(page), replay,
                    runtime.clock().instant());
            if (!result.records().isEmpty()) {
                try {
                    runtime.writer().submit(FederationWriter.group(result.records()), FetchTrigger.STREAM,
                            "peer:" + peerId, true);
                } catch (RuntimeException e) {
                    runtime.ingestor().release(result.records());
                    throw e;
                }
            }
        } while (next != null && running);
        if (!running) {
            return;
        }
        cursor = atSeq == null ? 0 : atSeq;
        runtime.lag(peerId, cursor, cursor);
        if (resync) {
            tombstoneAbsent(context, started, explicit);
        }
    }

    /** Pages {@code backfill} of history from {@code /changes} on the first start; a 410 falls back to a snapshot. */
    private void backfill(PeerRemote remote, PeerSelection selection, FederationIngestor.Context context)
            throws Exception {
        Instant from = runtime.clock().instant().minus(selection.backfillDuration());
        PeerSelection bounded = new PeerSelection(selection.sources(), selection.kinds(), selection.bbox(),
                selection.placeId(), selection.backfillDuration(), selection.backfill(), selection.live(),
                selection.maxHops(), selection.trackTtl());
        cursor = 0;
        try {
            catchUp(remote, bounded, runtime.context(runtime.states().find(peerId).orElseThrow(), bounded, false));
        } catch (ResyncRequired e) {
            snapshot(remote, selection, context, false, false);
        }
        runtime.states().update(peerId, PeerService.STATE_CONNECTING, null, cursor, null,
                "backfilled from " + from);
    }

    /** Pages {@code /changes} from the cursor until the peer has nothing more. */
    private void catchUp(PeerRemote remote, PeerSelection selection, FederationIngestor.Context context)
            throws Exception {
        boolean more = true;
        while (more && running) {
            JsonNode page;
            try {
                page = remote.changes(Math.max(0, cursor), selection);
            } catch (UpstreamHttpException e) {
                if (e.getStatus() == 410) {
                    throw new ResyncRequired("the peer purged changes after " + cursor);
                }
                throw e;
            }
            FederationIngestor.Result result = runtime.ingestor().process(PeerRemote.items(page), context,
                    runtime.clock().instant());
            runtime.deliver(peerId, ownSource, sink, result.records());
            long last = page.path("lastSeq").asLong(cursor);
            cursor = Math.max(cursor, last);
            more = page.path("hasMore").asBoolean(false);
            long watermark = page.path("watermarkSeq").asLong(cursor);
            runtime.lag(peerId, watermark, cursor);
            runtime.states().update(peerId, PeerService.STATE_CONNECTING, null, cursor, watermark, null);
            persistCursor(false);
        }
    }

    /** Reads the SSE stream until it ends, applying {@code change} events and honoring {@code reset}. */
    private void stream(PeerRemote remote, PeerSelection selection, FederationIngestor.Context context)
            throws Exception {
        HttpStream stream;
        try {
            stream = remote.stream(Math.max(0, cursor), selection);
        } catch (UpstreamHttpException e) {
            if (e.getStatus() == 410) {
                throw new ResyncRequired("the peer purged changes after " + cursor);
            }
            throw e;
        }
        current = stream;
        String event = null;
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream.body(), StandardCharsets.UTF_8))) {
            String line;
            while (running && (line = reader.readLine()) != null) {
                ctx.reportBytes(line.length() + 1L);
                if (line.isEmpty()) {
                    dispatch(event, data.toString(), context);
                    event = null;
                    data.setLength(0);
                } else if (line.startsWith(":")) {
                    // a comment (keep-alive)
                } else if (line.startsWith("event:")) {
                    event = line.substring(6).strip();
                } else if (line.startsWith("data:")) {
                    if (!data.isEmpty()) {
                        data.append('\n');
                    }
                    data.append(line.substring(5).strip());
                }
            }
        } catch (IOException e) {
            if (running && !interrupted) {
                throw e;
            }
        } finally {
            current = null;
            stream.close();
        }
    }

    /** Applies one SSE event. */
    private void dispatch(String event, String data, FederationIngestor.Context context) throws ResyncRequired {
        if (event == null || data.isEmpty()) {
            return;
        }
        switch (event) {
            case "change" -> {
                JsonNode item;
                try {
                    item = runtime.json().readTree(data);
                } catch (JacksonException e) {
                    return;
                }
                FederationIngestor.Result result = runtime.ingestor().process(List.of(item), context,
                        runtime.clock().instant());
                runtime.deliver(peerId, ownSource, sink, result.records());
                long seq = item.path("seq").asLong(-1);
                if (seq > cursor) {
                    cursor = seq;
                }
                sincePersist++;
                persistCursor(false);
            }
            case "reset" -> {
                String reason = null;
                long resume = -1;
                try {
                    JsonNode node = runtime.json().readTree(data);
                    reason = node.path("reason").asString(null);
                    resume = node.path("resumeAfter").asLong(-1);
                } catch (JacksonException ignored) {
                    // an unreadable reset is treated as a reconnect
                }
                if ("resync_required".equals(reason)) {
                    throw new ResyncRequired("the peer sent reset (resync_required)");
                }
                if (resume > cursor) {
                    cursor = resume;
                }
                interrupted = true;
                closeCurrent();
            }
            default -> {
                // heartbeat, live (not applied: a live update carries only kinematic fields), resync
            }
        }
    }

    /** Stores the cursor in the adapter state every {@value #CURSOR_PERSIST_EVERY} events or 30 s, or now. */
    private void persistCursor(boolean force) {
        if (cursor < 0 || cursor == persistedCursor) {
            return;
        }
        Instant now = runtime.clock().instant();
        if (!force && sincePersist < CURSOR_PERSIST_EVERY && now.isBefore(persistedAt.plusSeconds(30))) {
            return;
        }
        try {
            ctx.state().put(CURSOR_KEY, Long.toString(cursor));
            runtime.states().recordSeq(peerId, cursor);
            persistedCursor = cursor;
            sincePersist = 0;
            persistedAt = now;
        } catch (RuntimeException e) {
            ctx.log().warn("Could not store the cursor of peer {}: {}", peerId, e.getMessage());
        }
    }

    /** Submits the absent-head reconciliation of the peer's own record sources. */
    private void tombstoneAbsent(FederationIngestor.Context context, Instant started, boolean explicit) {
        List<String> sources = new ArrayList<>();
        sources.add(context.standardSourceId());
        if (context.trackSourceId() != null) {
            sources.add(context.trackSourceId());
        }
        for (String source : sources) {
            if (!runtime.registry().contains(source)) {
                continue;
            }
            try {
                runtime.queue().submitTombstoneAbsent(source,
                        java.util.Set.copyOf(runtime.registry().descriptor(source).kinds()), started, 0.9, explicit,
                        Duration.ofSeconds(30));
            } catch (RuntimeException e) {
                ctx.log().warn("Could not reconcile absent heads of {} after a resync: {}", source, e.getMessage());
            }
        }
    }

    private static String describe(Throwable e) {
        if (e instanceof UpstreamHttpException http) {
            return "the peer answered " + http.getStatus();
        }
        String message = e.getMessage();
        return message == null ? e.getClass().getSimpleName() : message;
    }

    /** Closes the current connection so the pass ends and the next one resyncs (a resync request). */
    void interrupt() {
        interrupted = true;
        closeCurrent();
    }

    private void closeCurrent() {
        HttpStream stream = current;
        if (stream != null) {
            stream.close();
        }
    }

    @Override
    public void close() {
        running = false;
        closeCurrent();
        Thread t = thread;
        if (t != null) {
            t.interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void onClose(Consumer<Throwable> callback) {
        onClose = callback == null ? e -> {} : callback;
    }

    /** The error code health shows for a key mismatch. */
    static String keyMismatchCode() {
        return ErrorCodes.PEER_KEY_MISMATCH;
    }
}
