package ai.foresignal.chatops;

import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.PreparedRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

/**
 * What the Telegram and Matrix connectors share (prompt 061): requests through {@link ChannelSender} (the
 * outbound URL policy, no redirects), the cursor store, the state, and outbound replies through the 018
 * {@link ChannelRateLimiter} with one bucket per destination chat.
 */
public abstract class AbstractChatConnector implements ChatConnector {

    private static final Logger log = LoggerFactory.getLogger(AbstractChatConnector.class);

    /** How long a reply waits for the rate limiter at most before it is dropped. */
    static final Duration MAX_RATE_WAIT = Duration.ofSeconds(60);

    /** The request sender. */
    protected final ChannelSender sender;

    /** The cursor store. */
    protected final ChatopsCursorStore cursors;

    /** The per-destination rate limiter. */
    protected final ChannelRateLimiter limiter;

    /** The JSON mapper. */
    protected final JsonMapper json;

    /** The application clock. */
    protected final Clock clock;

    /** The settings. */
    protected final ChatopsProperties properties;

    /** The live state. */
    protected final ConnectorState state = new ConnectorState();

    /** The channel, once started. */
    protected volatile ChannelConfig config;

    /** The sink, once started. */
    protected volatile CommandSink sink;

    /** True once {@link #stop} was called. */
    protected volatile boolean stopped;

    /** Creates the connector. */
    protected AbstractChatConnector(ChannelSender sender, ChatopsCursorStore cursors, ChannelRateLimiter limiter,
            JsonMapper json, Clock clock, ChatopsProperties properties) {
        this.sender = sender;
        this.cursors = cursors;
        this.limiter = limiter;
        this.json = json;
        this.clock = clock;
        this.properties = properties;
    }

    @Override
    public ConnectorState state() {
        return state;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    /** The outbound limit of one destination chat. */
    protected abstract ChannelRateLimit replyLimit();

    /**
     * Takes a token of the destination chat's bucket, waiting up to {@link #MAX_RATE_WAIT}.
     *
     * @return false when the reply must be dropped
     */
    protected boolean acquire(String chatId) {
        UUID key = UUID.nameUUIDFromBytes(("chatops:" + config.channelId() + ":" + chatId).getBytes(StandardCharsets.UTF_8));
        Duration wait = limiter.tryAcquire(key, replyLimit());
        if (wait.isZero()) {
            return true;
        }
        if (wait.compareTo(MAX_RATE_WAIT) > 0) {
            log.warn("A chat reply on channel {} was dropped by the rate limiter", config.channelId());
            return false;
        }
        try {
            Thread.sleep(wait.toMillis() + 1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        if (!limiter.tryAcquire(key, replyLimit()).isZero()) {
            log.warn("A chat reply on channel {} was dropped by the rate limiter", config.channelId());
            return false;
        }
        return true;
    }

    /** A JSON request with {@code headers}. */
    protected PreparedRequest jsonRequest(String method, String url, Map<String, String> headers, Object body) {
        Map<String, String> all = new LinkedHashMap<>(headers == null ? Map.of() : headers);
        all.put("Content-Type", "application/json");
        return new PreparedRequest(method, url, all,
                PreparedRequest.Bytes.text("application/json", body == null ? "" : json.writeValueAsString(body)));
    }

    /** Sends {@code request} with the channel request timeout. */
    protected ChannelResponse send(PreparedRequest request) throws IOException {
        return sender.send(request);
    }

    /** Sends a long poll whose server-side wait is {@code wait}. */
    protected ChannelResponse sendLongPoll(PreparedRequest request, Duration wait) throws IOException {
        return sender.send(request, wait.plusSeconds(15));
    }

    /** True for 401 and 403. */
    protected static boolean authFailure(int status) {
        return status == 401 || status == 403;
    }

    /** Logs a failed reply without its body (it may echo the request). */
    protected void logReplyFailure(String what, ChannelResponse response) {
        log.warn("{} on interactive channel {} answered HTTP {}", what, config.channelId(), response.status());
    }

    /** Logs a reply that could not be sent. */
    protected void logReplyFailure(String what, Exception e) {
        log.warn("{} on interactive channel {} failed: {}", what, config.channelId(), e.toString());
    }
}
