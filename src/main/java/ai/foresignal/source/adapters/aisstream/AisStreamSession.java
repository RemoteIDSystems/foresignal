package ai.foresignal.source.adapters.aisstream;

// Origin: the liveness rule (only a recognized envelope proves the feed works), the auth-versus-transport
// classification and the report-fast/recycle-slow silence watchdog are ported from God's Eye View (MIT,
// Copyright (c) 2026 Bilawal Sidhu): src/data/aisStreamAdapter.js and src/data/aisWatchdog.js. See
// THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.MeteredWebSocket;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import java.io.ByteArrayOutputStream;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;

/**
 * One aisstream connection: the WebSocket listener and the {@link StreamHandle} the supervisor holds
 * (prompt 063).
 *
 * <ul>
 *   <li><strong>Subscription.</strong> Sent in {@code onOpen}, well within aisstream's 3 s. A changed
 *       subscription ({@link #update}) is resent on the same connection, at most once a second: faster
 *       updates make aisstream close the connection, so a second change within the second waits and the
 *       newest one wins.
 *   <li><strong>Frames.</strong> aisstream sends binary frames of UTF-8 JSON; text frames are handled too.
 *       Fragments accumulate until {@code last}; a message over {@link AisStreamAdapter#MAX_MESSAGE_BYTES}
 *       is dropped and counted ({@code reason=oversize}). {@code request(1)} is called only after the
 *       message was handed to the sink, so the sink's back-pressure reaches the socket.
 *   <li><strong>Liveness.</strong> Only a recognized AIS envelope counts
 *       ({@link AisStreamMessageParser.Result#liveness()}); the confirmation does not.
 *   <li><strong>Errors.</strong> An {@code {"error"}} envelope whose text names the key ends the stream with
 *       {@link CredentialsRejectedException} (the supervisor retries hourly); any other is a transport
 *       error (the ladder). aisstream answers a bad key by closing the socket, so the adapter turns a second
 *       consecutive close before any confirmation or envelope into {@link CredentialsRejectedException}.
 *   <li><strong>Watchdog.</strong> {@link #silentFor} feeds the 120 s degraded health rule; after 300 s of
 *       silence the socket is aborted (never a close handshake) and the stream ends with an error, which is
 *       one step of the supervisor's ladder.
 * </ul>
 */
final class AisStreamSession implements StreamHandle, WebSocket.Listener {

    /** How often the watchdog thread reports bytes, sends a waiting subscription and checks silence. */
    static final Duration TICK = Duration.ofMillis(250);

    private final AisStreamAdapter adapter;
    private final StreamContext ctx;
    private final RecordSink sink;
    private final Logger log;

    private final List<Consumer<Throwable>> closeCallbacks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final Object sendLock = new Object();

    private volatile Throwable endedWith;
    private volatile boolean closedByUs;
    private volatile WebSocket socket;
    private volatile AisSubscription subscription;
    private volatile AisSubscription pending;
    private volatile Instant openedAt;
    private volatile Instant lastLivenessAt;
    private volatile boolean heardFrom;
    private volatile Boolean compressionEnabled;
    private volatile Thread watchdog;

    private CompletableFuture<WebSocket> lastSend = CompletableFuture.completedFuture(null);
    private long lastSendNanos = Long.MIN_VALUE;

    // read-loop state, touched only by the listener's (sequential) callbacks
    private final ByteArrayOutputStream binary = new ByteArrayOutputStream();
    private final StringBuilder text = new StringBuilder();
    private boolean discarding;
    private volatile long payloadBytes;
    private long reportedBytes;

    AisStreamSession(AisStreamAdapter adapter, StreamContext ctx, RecordSink sink, AisSubscription subscription) {
        this.adapter = adapter;
        this.ctx = ctx;
        this.sink = sink;
        this.subscription = subscription;
        this.log = ctx.log();
    }

    // ---------- StreamHandle ----------

    /** Aborts the socket and stops the watchdog; the supervisor flushes the sink. Idempotent. */
    @Override
    public void close() {
        closedByUs = true;
        finished.set(true);
        abortSocket();
        stopWatchdog();
        reportBytes();
    }

    @Override
    public boolean isRunning() {
        return !finished.get();
    }

    @Override
    public void onClose(Consumer<Throwable> callback) {
        closeCallbacks.add(callback);
        if (finished.get() && !closedByUs) {
            // Ended before the supervisor registered: replay, so the drop is not missed.
            callback.accept(endedWith);
        }
    }

    /**
     * Resends the subscription for {@code params} on this connection (it replaces the previous one).
     *
     * @return true when applied or queued; false when the connection has ended, or the parameters are
     *     invalid, so the supervisor restarts the stream
     */
    @Override
    public boolean update(ValidatedParams params) {
        if (finished.get()) {
            return false;
        }
        AisSubscription next;
        try {
            next = AisSubscription.of(subscription.apiKey(), params);
        } catch (IllegalArgumentException e) {
            log.warn("{}: the changed parameters cannot be subscribed ({}); restarting", ctx.sourceId(), e.getMessage());
            return false;
        }
        if (next.sameAs(subscription) && pending == null) {
            return true;
        }
        pending = next;
        sendPendingIfDue();
        return true;
    }

    // ---------- WebSocket.Listener ----------

    @Override
    public void onOpen(WebSocket webSocket) {
        socket = webSocket;
        Instant now = ctx.clock().instant();
        openedAt = now;
        lastLivenessAt = now;
        send(subscription);
        log.info("{}: subscribed {}", ctx.sourceId(), subscription);
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        payloadBytes += data.length();
        if (!discarding) {
            text.append(data);
            if (text.length() > AisStreamAdapter.MAX_MESSAGE_BYTES) {
                discard();
            }
        }
        if (last) {
            String message = discarding ? null : text.toString();
            endMessage();
            if (message != null) {
                handle(message);
            }
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        int size = data.remaining();
        payloadBytes += size;
        if (!discarding) {
            if (binary.size() + (long) size > AisStreamAdapter.MAX_MESSAGE_BYTES) {
                discard();
            } else {
                byte[] chunk = new byte[size];
                data.get(chunk);
                binary.writeBytes(chunk);
            }
        }
        if (last) {
            String message = discarding ? null : binary.toString(StandardCharsets.UTF_8);
            endMessage();
            if (message != null) {
                handle(message);
            }
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        String detail = reason == null || reason.isBlank() ? "" : ": " + reason;
        end(new AdapterException("aisstream closed the connection (" + statusCode + detail + ")"), true);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        end(new AdapterException("aisstream connection failed: " + error.getMessage(), error), true);
    }

    // ---------- the watchdog ----------

    /** Starts the watchdog thread; called once the socket is open. */
    void startWatchdog() {
        watchdog = Thread.ofVirtual().name("foresignal-aisstream-watchdog").start(() -> {
            while (!finished.get()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(TICK.toMillis());
                } catch (InterruptedException e) {
                    return;
                }
                tick();
            }
        });
    }

    /** One watchdog pass: report bytes, send a waiting subscription, recycle a socket silent too long. */
    void tick() {
        if (finished.get()) {
            return;
        }
        reportBytes();
        sendPendingIfDue();
        Duration silent = silentFor(ctx.clock().instant());
        if (silent != null && silent.compareTo(AisStreamAdapter.SILENCE_RECYCLE) >= 0) {
            log.warn("{}: connected but silent for {} s; recycling the connection", ctx.sourceId(), silent.toSeconds());
            end(new AdapterException("aisstream connected but silent for " + silent.toSeconds()
                    + " s; the connection was recycled"), false);
            abortSocket();
        }
    }

    /** How long the open connection has gone without a recognized envelope, or null when not open. */
    Duration silentFor(Instant now) {
        Instant since = lastLivenessAt;
        if (finished.get() || since == null) {
            return null;
        }
        Duration silent = Duration.between(since, now);
        return silent.isNegative() ? Duration.ZERO : silent;
    }

    /** {@code SubscriptionConfirmation.Message.CompressionEnabled}, or null before the confirmation. */
    Boolean compressionEnabled() {
        return compressionEnabled;
    }

    /** True when the server accepted {@code permessage-deflate} in the handshake (false on the JDK client). */
    boolean deflateNegotiated() {
        return socket instanceof MeteredWebSocket metered && metered.perMessageDeflateNegotiated();
    }

    /** When the connection opened, or null. */
    Instant openedAt() {
        return openedAt;
    }

    // ---------- internals ----------

    private void handle(String json) {
        AisStreamMessageParser.Result result = AisStreamMessageParser.parse(json);
        if (result.liveness()) {
            lastLivenessAt = ctx.clock().instant();
            markHeardFrom();
        }
        AisStreamMetrics metrics = adapter.metrics();
        switch (result) {
            case AisMessage message -> {
                metrics.message(message.messageType());
                adapter.store().merge(message).ifPresent(sink::accept);
            }
            case AisStreamMessageParser.Ignored ignored -> metrics.message(ignored.messageType());
            case AisStreamMessageParser.Untimed untimed -> metrics.dropped(AisStreamMetrics.UNTIMED);
            case AisStreamMessageParser.Confirmation confirmation -> {
                compressionEnabled = confirmation.compressionEnabled();
                markHeardFrom();
                log.info("{}: subscription confirmed; CompressionEnabled={}, permessage-deflate negotiated={}",
                        ctx.sourceId(), confirmation.compressionEnabled(), deflateNegotiated());
                if (!Boolean.TRUE.equals(confirmation.compressionEnabled())) {
                    log.warn("{}: aisstream did not confirm compression; uncompressed connections are bandwidth"
                            + " limited, so narrow the bbox", ctx.sourceId());
                }
            }
            case AisStreamMessageParser.ErrorEnvelope error -> {
                AdapterException failure = error.auth()
                        ? new CredentialsRejectedException("aisstream rejected the API key: " + error.text())
                        : new AdapterException("aisstream error: " + error.text());
                log.warn("{}: {}", ctx.sourceId(), failure.getMessage());
                end(failure, false);
                abortSocket();
            }
            case AisStreamMessageParser.Unrecognized unrecognized -> metrics.dropped(unrecognized.reason());
            case AisStreamMessageParser.Malformed malformed -> metrics.dropped(AisStreamMetrics.MALFORMED);
        }
    }

    private void markHeardFrom() {
        if (!heardFrom) {
            heardFrom = true;
            adapter.confirmed();
        }
    }

    private void discard() {
        discarding = true;
        binary.reset();
        text.setLength(0);
    }

    private void endMessage() {
        if (discarding) {
            adapter.metrics().dropped(AisStreamMetrics.OVERSIZE);
        }
        discarding = false;
        binary.reset();
        text.setLength(0);
    }

    /**
     * Ends the stream once and tells the supervisor why.
     *
     * @param closed true when the upstream closed the connection (counted towards the bad-key rule)
     */
    private void end(Throwable failure, boolean closed) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        Throwable reason = failure;
        if (closed && !closedByUs && !heardFrom && adapter.unconfirmedClose() >= AisStreamAdapter.CLOSES_BEFORE_REJECTED) {
            reason = new CredentialsRejectedException("aisstream closed the connection "
                    + AisStreamAdapter.CLOSES_BEFORE_REJECTED + " times in a row before confirming the subscription;"
                    + " the API key is probably invalid", failure);
        }
        endedWith = reason;
        stopWatchdog();
        reportBytes();
        for (Consumer<Throwable> callback : closeCallbacks) {
            try {
                callback.accept(reason);
            } catch (RuntimeException e) {
                log.debug("{}: a close callback failed", ctx.sourceId(), e);
            }
        }
    }

    private void sendPendingIfDue() {
        AisSubscription next = pending;
        if (next == null || finished.get() || socket == null) {
            return;
        }
        synchronized (sendLock) {
            if (pending != next) {
                return;
            }
            long sinceLast = System.nanoTime() - lastSendNanos;
            if (lastSendNanos != Long.MIN_VALUE && sinceLast < AisStreamAdapter.UPDATE_MIN_GAP.toNanos()) {
                return; // the watchdog tick sends it once the second has passed
            }
            pending = null;
            subscription = next;
            send(next);
            log.info("{}: resubscribed {}", ctx.sourceId(), next);
        }
    }

    /** Sends one subscription frame, after the previous send completed. Never logs the frame. */
    private void send(AisSubscription value) {
        WebSocket ws = socket;
        if (ws == null) {
            return;
        }
        synchronized (sendLock) {
            try {
                lastSend.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.debug("{}: the previous subscription send did not complete", ctx.sourceId());
            }
            lastSendNanos = System.nanoTime();
            lastSend = ws.sendText(value.toJson(), true).exceptionally(error -> {
                log.warn("{}: could not send the subscription: {}", ctx.sourceId(), error.getMessage());
                return null;
            }).toCompletableFuture();
        }
    }

    private void reportBytes() {
        WebSocket ws = socket;
        long total = ws instanceof MeteredWebSocket metered ? metered.wireBytesReceived() : payloadBytes;
        long delta;
        synchronized (this) {
            delta = total - reportedBytes;
            reportedBytes = total;
        }
        if (delta > 0) {
            ctx.reportBytes(delta);
        }
    }

    private void abortSocket() {
        WebSocket ws = socket;
        if (ws != null) {
            try {
                ws.abort();
            } catch (RuntimeException e) {
                log.debug("{}: aborting the socket failed", ctx.sourceId(), e);
            }
        }
    }

    private void stopWatchdog() {
        Thread thread = watchdog;
        if (thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
        }
    }
}
