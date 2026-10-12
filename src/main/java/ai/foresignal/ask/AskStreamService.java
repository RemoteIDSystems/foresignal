package ai.foresignal.ask;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.stream.sse.EmitterSseSender;
import ai.foresignal.stream.sse.SseFrame;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamLimits;
import ai.foresignal.stream.sse.StreamPrincipal;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code askStream} (prompt 028): one Ask answered over Server-Sent Events, with the channel patterns of
 * prompt 015.
 *
 * <p>Events: {@code progress} ({@code {tool, status, summary}}), {@code delta} (provider text as it
 * streams, else one final delta; unverified), {@code citation}, {@code warning}, {@code action},
 * {@code done} (the full verified {@code AskResponse}, identical to the stored message) and
 * {@code error} (a problem document). {@code heartbeat} ({@code {}}) every
 * {@code stream.limits.heartbeatSeconds}. The response is committed at once ({@code :open}), so the
 * client gets its headers before the model answers.
 *
 * <p><strong>Limits and cleanup.</strong> Open Ask streams count per principal (the token, or the
 * remote address) against {@code stream.limits.maxStreamsPerToken} /
 * {@code maxStreamsAnonymousPerAddress}; beyond it, {@code 429 STREAM_LIMIT} with
 * {@code Retry-After: 5}. The count, the heartbeat and the emitter are released when the answer ends,
 * the client goes away or the emitter fails. A client that disconnects does not cancel the answer: it
 * still completes and is stored, so the thread stays whole.
 */
@Service
public class AskStreamService {

    private static final Logger log = LoggerFactory.getLogger(AskStreamService.class);

    /** {@code Retry-After} of a stream-limit refusal, in seconds. */
    static final String RETRY_AFTER_SECONDS = "5";

    private final AskService ask;
    private final SseHub hub;
    private final JsonMapper json;
    private final Map<String, Integer> open = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeats = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ask-stream-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    /** Creates the service. */
    public AskStreamService(AskService ask, SseHub hub, JsonMapper json) {
        this.ask = ask;
        this.hub = hub;
        this.json = json;
    }

    /** Stops the heartbeat thread. */
    @PreDestroy
    public void stop() {
        heartbeats.shutdownNow();
    }

    /** Open Ask streams of {@code principalId}. */
    public int openStreams(String principalId) {
        return open.getOrDefault(principalId, 0);
    }

    /**
     * Opens the stream of a prepared question and answers it on a virtual thread.
     *
     * @param prepared the checked question ({@link AskService#prepare})
     * @param principal who opens it, for the per-principal limit
     * @return the emitter to return from the controller
     * @throws ForesignalException {@code 429 STREAM_LIMIT} beyond the per-principal limit
     */
    public SseEmitter open(AskService.Prepared prepared, StreamPrincipal principal) {
        StreamLimits limits = hub.limits();
        register(principal, limits);
        SseEmitter emitter = EmitterSseSender.newCommittingEmitter();
        Channel channel = new Channel(principal.id(), new EmitterSseSender(emitter));
        emitter.onCompletion(channel::close);
        emitter.onTimeout(channel::close);
        emitter.onError(e -> channel.close());
        long period = Math.max(1, limits.heartbeatSeconds());
        channel.heartbeat = heartbeats.scheduleAtFixedRate(() -> channel.send("heartbeat", Map.of()), period, period,
                TimeUnit.SECONDS);
        Thread.ofVirtual().name("ask-stream").start(() -> {
            try {
                ask.answer(prepared, channel);
            } catch (ForesignalException e) {
                channel.send("error", problem(e.getStatus(), e.getCode(), e.getMessage()));
            } catch (RuntimeException e) {
                log.warn("Ask stream failed", e);
                channel.send("error", problem(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
                        "The answer failed unexpectedly; the failure is logged on the server."));
            } finally {
                channel.complete();
            }
        });
        return emitter;
    }

    private void register(StreamPrincipal principal, StreamLimits limits) {
        int allowed = limits.perPrincipalLimit(principal);
        Integer[] seen = new Integer[1];
        open.compute(principal.id(), (k, v) -> {
            int current = v == null ? 0 : v;
            seen[0] = current;
            return current >= allowed ? (v == null ? null : v) : current + 1;
        });
        if (seen[0] >= allowed) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.STREAM_LIMIT,
                    "This caller already has " + seen[0] + " open Ask streams (limit " + allowed + ").")
                    .withHeader(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);
        }
    }

    private void release(String principalId) {
        open.computeIfPresent(principalId, (k, v) -> v <= 1 ? null : v - 1);
    }

    private static Map<String, Object> problem(HttpStatus status, String code, String detail) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", ErrorCodes.typeUri(code).toString());
        out.put("title", status.getReasonPhrase());
        out.put("status", status.value());
        out.put("detail", detail);
        out.put("code", code);
        return out;
    }

    /** One open stream: an {@link AskListener} that writes events, serialized, until closed. */
    private final class Channel implements AskListener {

        private final String principalId;
        private final EmitterSseSender sender;
        private final AtomicBoolean closed = new AtomicBoolean();
        private final AtomicBoolean released = new AtomicBoolean();
        volatile ScheduledFuture<?> heartbeat;

        Channel(String principalId, EmitterSseSender sender) {
            this.principalId = principalId;
            this.sender = sender;
        }

        @Override
        public boolean streaming() {
            return true;
        }

        @Override
        public void progress(String tool, String status, String summary) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tool", tool);
            data.put("status", status);
            data.put("summary", summary);
            send("progress", data);
        }

        @Override
        public void delta(String text) {
            send("delta", Map.of("text", text));
        }

        @Override
        public void citation(AskCitation citation) {
            send("citation", citation.toMap());
        }

        @Override
        public void warning(String message) {
            send("warning", Map.of("message", message));
        }

        @Override
        public void action(Map<String, Object> action) {
            send("action", action);
        }

        @Override
        public void done(Map<String, Object> response) {
            send("done", response);
        }

        synchronized void send(String event, Object data) {
            if (closed.get()) {
                return;
            }
            try {
                sender.send(SseFrame.control(event, json.writeValueAsString(data)));
            } catch (IOException | RuntimeException e) {
                close();
            }
        }

        void complete() {
            sender.complete();
            close();
        }

        void close() {
            closed.set(true);
            ScheduledFuture<?> beat = heartbeat;
            if (beat != null) {
                beat.cancel(false);
            }
            if (released.compareAndSet(false, true)) {
                release(principalId);
            }
        }
    }
}
