package ai.foresignal.source.adapters.bluesky;

import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.StreamLedger;
import ai.foresignal.source.formats.social.SocialDescriptors;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.WebSocketOptions;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code bluesky-jetstream} (prompt 135): the whole Bluesky network's public posts and reposts from Jetstream v2
 * ({@code wss://jetstream.us-east.bsky.network} or {@code us-west}, path
 * {@code /xrpc/network.bsky.jetstream.subscribeEvents}, subprotocol {@code xrpc.v1.json}, {@code kinds=commit},
 * {@code collections=app.bsky.feed.post} and {@code app.bsky.feed.repost}; verified on bsky.network/docs/jetstream
 * 2026-10-17). Open, no account; <strong>on by default</strong>.
 *
 * <p>A complete stream: every post is counted into the platform-language denominator and matched against the tracked
 * topics locally; matching posts are stored. The cursor is the event {@code seq} (Jetstream v2; the v1 {@code time_us}
 * the prompt names is still accepted as a cursor ≥ 10^15) and resumes a reconnect within the instance's 36-hour
 * window, so the gap is replayed; beyond it the gap is recorded as not replayed. Delete events tombstone stored posts
 * while deletion sync is on. Network Replay (operator API token, option {@code replay}) is planned with
 * {@code planSnapshot}; its segment format could not be verified, so a replay plan is reported but not applied
 * ({@link JetstreamReplay}).
 */
@Component
public class BlueskyJetstreamAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.BLUESKY_JETSTREAM;

    /** The us-east instance. */
    public static final String US_EAST = "wss://jetstream.us-east.bsky.network";

    /** The us-west instance. */
    public static final String US_WEST = "wss://jetstream.us-west.bsky.network";

    /** The subscription path. */
    public static final String PATH = "/xrpc/network.bsky.jetstream.subscribeEvents";

    /** The WebSocket subprotocol. */
    public static final String SUBPROTOCOL = "xrpc.v1.json";

    /** Test-only override of the WebSocket base URL. */
    public static final String URL_PROPERTY = "foresignal.test.adapters.bluesky-jetstream.url";

    /** The parameter choosing the instance. */
    public static final String ENDPOINT_PARAM = "endpoint";

    /** How far back a stored cursor is resumed (the hosted 36-hour window less a margin). */
    public static final Duration LIVE_WINDOW = Duration.ofHours(35);

    /** The adapter-state key of the last seq. */
    static final String SEQ_KEY = "jetstream:seq";

    /** The adapter-state key of the last event time. */
    static final String TIME_KEY = "jetstream:time";

    private static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(5), Duration.ofMinutes(5),
            0.2);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String urlOverride;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;

    /** Creates the adapter. */
    @Autowired
    public BlueskyJetstreamAdapter(@Value("${" + URL_PROPERTY + ":}") String urlOverride,
            ObjectProvider<SocialRuntime> runtimes) {
        this.urlOverride = urlOverride == null || urlOverride.isBlank() ? null : urlOverride.strip();
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; the runtime may be null for descriptor checks). */
    public BlueskyJetstreamAdapter(String urlOverride, SocialRuntime runtime) {
        this.urlOverride = urlOverride == null || urlOverride.isBlank() ? null : urlOverride.strip();
        this.runtimes = null;
        this.fixed = runtime;
    }

    SocialRuntime runtime() {
        return fixed != null ? fixed : runtimes == null ? null : runtimes.getIfAvailable();
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * The subscription URI.
     *
     * @param base the instance base URL
     * @param cursor the seq to resume from, or null to start live
     */
    static URI uri(String base, Long cursor) {
        StringBuilder sb = new StringBuilder(base.replaceAll("/+$", "")).append(PATH).append("?kinds=commit")
                .append("&collections=").append(BlueskyRecords.POST)
                .append("&collections=").append(BlueskyRecords.REPOST);
        if (cursor != null) {
            sb.append("&cursor=").append(cursor);
        }
        return URI.create(sb.toString());
    }

    String base(StreamContext ctx) {
        if (urlOverride != null) {
            return urlOverride;
        }
        String endpoint = ctx.params() == null ? null : ctx.params().string(ENDPOINT_PARAM);
        return "us-west".equals(endpoint) ? US_WEST : US_EAST;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        SocialRuntime runtime = runtime();
        if (runtime == null) {
            throw new AdapterException(ID + ": the social runtime is not available");
        }
        Instant now = ctx.now();
        Long storedSeq = parseLong(ctx.state().get(SEQ_KEY).orElse(null));
        Instant storedTime = parseInstant(ctx.state().get(TIME_KEY).orElse(null));
        Long cursor = null;
        StreamLedger.Gap pending;
        if (storedSeq != null && storedTime != null && !storedTime.isBefore(now.minus(LIVE_WINDOW))) {
            cursor = storedSeq;
            pending = new StreamLedger.Gap(storedTime, now, true, "resumed from the stored cursor");
        } else if (storedTime != null) {
            String reason = "the stored cursor is older than Jetstream's 36-hour window";
            if (runtime.settings(ID).option("replay", false)) {
                reason = JetstreamReplay.describe(ctx, base(ctx), storedSeq) + "; " + reason;
            }
            pending = new StreamLedger.Gap(storedTime, now, false, reason);
        } else {
            Instant hour = now.truncatedTo(ChronoUnit.HOURS);
            pending = hour.isBefore(now) ? new StreamLedger.Gap(hour, now, false, "first start") : null;
        }
        JetstreamSession session = new JetstreamSession(ctx, sink, runtime, storedSeq, pending);
        try {
            ctx.http().openWebSocket(uri(base(ctx), cursor), session, Map.of("Sec-WebSocket-Protocol", SUBPROTOCOL),
                    WebSocketOptions.DEFAULT);
        } catch (AdapterException e) {
            if (cursor != null) {
                // A cursor the instance refuses (CursorTooOld is an HTTP 400 before the upgrade) must not block every
                // later attempt: the next start connects live and records the gap as not replayed.
                ctx.state().remove(SEQ_KEY);
            }
            throw e;
        }
        session.startWatchdog();
        return session;
    }

    static Long parseLong(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(s.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static Instant parseInstant(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(s.strip());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Bluesky Jetstream (live)")
                .description("Every public Bluesky post and repost from Jetstream v2, matched locally against the"
                        + " attention topics' social terms; matching posts are stored, every post is counted for the"
                        + " platform denominators. Open, no account needed; on by default. Network Replay backfill"
                        + " uses the operator's API token from bsky.network/account.")
                .homepage("https://bsky.network/docs/jetstream/")
                .attribution("Bluesky Social PBC (Jetstream)")
                .licence("Bluesky public data; Bluesky Protocol Services terms")
                .termsUrl("https://bsky.social/about/support/tos")
                .mode(Mode.STREAMING)
                .credential(CredentialField.optional("apiToken",
                        "Network Replay API token from bsky.network/account (optional; only for backfill)"))
                .param(ParamSpec.enumerated(ENDPOINT_PARAM, "Jetstream v2 instance", "us-east",
                        List.of("us-east", "us-west")))
                .defaultBudget(Budget.of(6, 500))
                .reconnect(RECONNECT)
                .maxResponseBytes(16 * 1024 * 1024);
        return SocialDescriptors.posts(b, SocialVocabulary.BLUESKY).build();
    }
}
