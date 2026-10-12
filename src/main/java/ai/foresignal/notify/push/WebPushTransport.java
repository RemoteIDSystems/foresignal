package ai.foresignal.notify.push;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.settings.PushSettings;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The RFC 8030 transport (prompt 031): browsers and UnifiedPush distributors.
 *
 * <p>For each send it:
 * <ol>
 *   <li>re-resolves the endpoint host and applies {@link OutboundUrlPolicy} profile
 *       {@code PUSH_ENDPOINT} with the current {@code push.allowedPrivateHosts} and outbound
 *       overrides (prompt 035; a refusal is {@link PushResult.Rejected} with {@code URL_NOT_ALLOWED});
 *   <li>takes a token from the per-host rate limit (none: {@link PushResult.Retry}, not counted);
 *   <li>renders the payload in the subscription's format and language within the 3993-byte budget,
 *       encrypts it (RFC 8291, {@code aes128gcm}) and signs a VAPID token for the endpoint origin;
 *   <li>POSTs to the checked address with {@code Content-Encoding: aes128gcm},
 *       {@code Content-Type: application/octet-stream}, {@code TTL}, {@code Urgency}, {@code Topic}
 *       and {@code Authorization: vapid t=…, k=…}, never following redirects.
 * </ol>
 *
 * <p>Outcomes: 2xx is {@link PushResult.Sent}; 404 and 410 {@link PushResult.Gone}; 429 and 503
 * {@link PushResult.Retry} after {@code Retry-After}; 413 is logged and {@link PushResult.Rejected}
 * (it cannot happen within the budget); other 4xx are rejected; 5xx and I/O errors are
 * {@link PushResult.Failed}. Errors name the host only, never the endpoint.
 */
@Component
public class WebPushTransport implements PushTransport {

    private static final Logger log = LoggerFactory.getLogger(WebPushTransport.class);

    /** The {@code URL_NOT_ALLOWED} code of a refused endpoint. */
    public static final String URL_NOT_ALLOWED = "URL_NOT_ALLOWED";

    private final OutboundUrlPolicy policy;
    private final PushHostLimiter limiter;
    private final PushPayloadBudget budget;
    private final VapidTokens vapid;
    private final PushHttp http;
    private final ChannelService channels;
    private final Clock clock;

    /** Creates the transport. */
    public WebPushTransport(OutboundUrlPolicy policy, PushHostLimiter limiter, PushPayloadBudget budget,
            VapidTokens vapid, PushHttp http, ChannelService channels, Clock clock) {
        this.policy = policy;
        this.limiter = limiter;
        this.budget = budget;
        this.vapid = vapid;
        this.http = http;
        this.channels = channels;
        this.clock = clock;
    }

    @Override
    public String id() {
        return WebPushTarget.TYPE;
    }

    @Override
    public boolean supports(PushTarget target) {
        return target instanceof WebPushTarget;
    }

    @Override
    public PushResult send(PushTarget target, PushMessage message, PushOptions options) {
        PushResult result = sendOnce(target, message, options);
        if (!(target instanceof WebPushTarget web)) {
            return result;
        }
        return switch (result) {
            case PushResult.Rejected r -> new PushResult.Rejected(r.status(), r.code(), redact(r.reason(), web.endpoint()));
            case PushResult.Failed f -> new PushResult.Failed(f.status(), redact(f.reason(), web.endpoint()));
            default -> result;
        };
    }

    /**
     * {@code text} without the endpoint or its path (the path is the capability), cut to 500
     * characters.
     */
    static String redact(String text, String endpoint) {
        if (text == null) {
            return null;
        }
        String out = text;
        if (endpoint != null && !endpoint.isBlank()) {
            out = out.replace(endpoint, "***");
            try {
                String path = URI.create(endpoint).getRawPath();
                if (path != null && path.length() > 1) {
                    out = out.replace(path, "/***");
                }
            } catch (IllegalArgumentException ignored) {
                // Nothing more to strip.
            }
        }
        return out.length() > 500 ? out.substring(0, 500) : out;
    }

    private PushResult sendOnce(PushTarget target, PushMessage message, PushOptions options) {
        if (!(target instanceof WebPushTarget web)) {
            return new PushResult.Rejected(null, null, "not a Web Push target");
        }
        URI endpoint;
        try {
            endpoint = URI.create(web.endpoint());
        } catch (IllegalArgumentException e) {
            return new PushResult.Rejected(null, URL_NOT_ALLOWED, "the endpoint is not a valid URL");
        }
        String host = endpoint.getHost() == null ? "?" : endpoint.getHost();
        PushSettings settings = channels.serverSettings().effectivePush();
        if (!"https".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getRawUserInfo() != null) {
            return new PushResult.Rejected(null, URL_NOT_ALLOWED, "the endpoint must be https without user info");
        }
        OutboundUrlPolicy.Result decision = policy.check(endpoint, OutboundUrlPolicy.Profile.PUSH_ENDPOINT);
        if (decision instanceof OutboundUrlPolicy.Denied denied) {
            return new PushResult.Rejected(null, URL_NOT_ALLOWED, denied.reason());
        }
        InetAddress pinned = ((OutboundUrlPolicy.Allowed) decision).pinned();
        String limitHost = host.toLowerCase(java.util.Locale.ROOT);
        if (limitHost.startsWith("[") && limitHost.endsWith("]")) {
            limitHost = limitHost.substring(1, limitHost.length() - 1);
        }
        Duration wait = limiter.tryAcquire(limitHost);
        if (!wait.isZero()) {
            return new PushResult.Retry(wait, false, null, "rate limit of " + host);
        }
        byte[] body;
        Map<String, String> headers = new LinkedHashMap<>();
        try {
            String lang = web.lang() != null ? web.lang() : settings.defaultLang();
            // Quiet hours' downgrade (PushOptions.silent) reaches the payload too, not only the Urgency header: the
            // stored content is built before the downgrade is decided (checkpoint 041, WebPushDeliveryIT).
            PushMessage payload = options.silent() && !message.silent() ? message.withSilent(true) : message;
            byte[] plaintext = budget.fit(payload, web.payloadFormat(), lang);
            body = WebPushCrypto.encrypt(plaintext, EcKeys.unb64(web.p256dh()), EcKeys.unb64(web.auth()));
            headers.put("Content-Encoding", "aes128gcm");
            headers.put("Content-Type", "application/octet-stream");
            headers.put("TTL", Long.toString(options.ttl().toSeconds()));
            headers.put("Urgency", options.urgency().header());
            if (options.topic() != null) {
                headers.put("Topic", options.topic());
            }
            headers.put("Authorization", vapid.authorization(endpoint, settings.contact()));
        } catch (RuntimeException e) {
            return new PushResult.Rejected(null, null, "could not encrypt for " + host + ": " + e.getMessage());
        }
        PushHttp.Response response;
        try {
            response = http.post(endpoint, pinned, headers, body);
        } catch (IOException e) {
            return new PushResult.Failed(null, e.getClass().getSimpleName() + " from " + host + ": " + e.getMessage());
        }
        int status = response.status();
        if (status >= 200 && status < 300) {
            return new PushResult.Sent(status);
        }
        if (status == 404 || status == 410) {
            return new PushResult.Gone(status);
        }
        if (status == 429 || status == 503) {
            return new PushResult.Retry(retryAfter(response), true, status, "HTTP " + status + " from " + host);
        }
        if (status == 413) {
            log.error("Push service {} answered 413 for a {}-byte message; the payload budget should prevent this",
                    host, body.length);
            return new PushResult.Rejected(status, null, "HTTP 413 from " + host);
        }
        String detail = response.body() == null ? "" : response.body().strip();
        String reason = "HTTP " + status + " from " + host + (detail.isEmpty() ? "" : ": " + detail);
        if (status >= 300 && status < 400) {
            return new PushResult.Rejected(status, null, "HTTP " + status + " from " + host + " (redirect not followed)");
        }
        return status >= 500 ? new PushResult.Failed(status, reason) : new PushResult.Rejected(status, null, reason);
    }

    /** The {@code Retry-After} wait (seconds or an HTTP date), else 60 s, between 1 s and 2 h. */
    Duration retryAfter(PushHttp.Response response) {
        Duration out = Duration.ofSeconds(60);
        var header = response.header("Retry-After");
        if (header.isPresent()) {
            String value = header.get().strip();
            try {
                out = Duration.ofMillis((long) (Double.parseDouble(value) * 1000));
            } catch (NumberFormatException e) {
                try {
                    Instant at = DateTimeFormatter.RFC_1123_DATE_TIME.parse(value, Instant::from);
                    out = Duration.between(clock.instant(), at);
                } catch (DateTimeParseException ignored) {
                    out = Duration.ofSeconds(60);
                }
            }
        }
        if (out.compareTo(Duration.ofSeconds(1)) < 0) {
            out = Duration.ofSeconds(1);
        }
        Duration max = DeliveryWorker.LADDER.getLast();
        return out.compareTo(max) > 0 ? max : out;
    }
}
