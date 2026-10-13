package ai.foresignal.source.kit.websub;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.collect.ClockTimeMeter;
import ai.foresignal.inbound.PushIngestService;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedKitFamily;
import ai.foresignal.source.kit.feed.FeedParser;
import ai.foresignal.source.kit.feed.FeedRecords;
import ai.foresignal.source.kit.feed.FeedSourceSpec;
import ai.foresignal.source.kit.feed.WebSubCoordinator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * WebSub for feed-kit sources (prompt 073): subscription, intent verification, signed content delivery and lease
 * renewal. Everything is off unless {@value #ENABLED_PROPERTY} is true; with it false no subscription is ever
 * attempted and both callbacks answer 404.
 *
 * <p><strong>Subscribing</strong> ({@link #afterPoll}): only for a spec with {@code websub: true}, only when
 * {@code server.publicBaseUrl} is https, and only when the feed advertises a hub and a self link. The hub must pass
 * the {@link OutboundUrlPolicy} ({@code PUBLIC_ONLY}). The request is {@code POST hub} through the source's budgeted
 * client with {@code hub.mode=subscribe}, {@code hub.topic=<self>},
 * {@code hub.callback=${publicBaseUrl}/v1/websub/{sourceId}/{token}}, {@code hub.secret} (32 random bytes, stored
 * encrypted with {@link CredentialCipher}) and {@code hub.lease_seconds=86400}. A pending request is not repeated
 * within an hour.
 *
 * <p><strong>Verification</strong> ({@link #verify}): the challenge is echoed only when the mode, the topic and the
 * token match a pending or active subscription; the lease starts then.
 *
 * <p><strong>Delivery</strong> ({@link #receive}): at most {@value #PUSHES_PER_MINUTE} per source per minute; the
 * {@code X-Hub-Signature} ({@code sha256=} preferred, {@code sha1=} accepted) is checked over the raw body with a
 * constant-time compare; the body is parsed with {@link FeedParser}, mapped like a poll ({@link FeedRecords}) and
 * handed to the shared push pipeline, which stamps {@code receivedAt} on receipt.
 *
 * <p><strong>Renewal</strong> ({@link #maintain}): at 80 % of the lease. A failed renewal, a denial or an expired
 * lease returns the source to polling and records the health reason {@value WebSubCoordinator#LEASE_LOST}.
 */
@Service
public class WebSubService implements WebSubCoordinator {

    private static final Logger log = LoggerFactory.getLogger(WebSubService.class);

    /** The bootstrap property enabling WebSub. */
    public static final String ENABLED_PROPERTY = "foresignal.kits.websub.enabled";

    /** The lease asked for, seconds. */
    public static final long LEASE_SECONDS = 86_400;

    /** Content deliveries accepted per source per minute. */
    public static final int PUSHES_PER_MINUTE = 60;

    /** The largest delivery body, bytes. */
    public static final int MAX_BODY_BYTES = 2 * 1024 * 1024;

    /** The share of the lease after which it is renewed. */
    public static final double RENEW_AT = 0.8;

    /** How long a pending subscribe request is left alone before it is sent again. */
    public static final Duration RETRY_PENDING_AFTER = Duration.ofHours(1);

    /** The callback path prefix. */
    public static final String CALLBACK_PATH = "/v1/websub/";

    private static final int NONCE_LENGTH = 12;

    private final boolean enabled;
    private final WebSubSubscriptionRepository subscriptions;
    private final CredentialCipher cipher;
    private final OutboundUrlPolicy policy;
    private final AppSettingService settings;
    private final FeedKitFamily feeds;
    private final SourceRegistry registry;
    private final SourceService sources;
    private final PushIngestService push;
    private final Clock clock;
    private final ClockTimeMeter time;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Bucket> rate = new ConcurrentHashMap<>();

    /**
     * Creates the service.
     *
     * @param enabled the value of {@value #ENABLED_PROPERTY}
     * @param subscriptions the subscription rows
     * @param cipher encrypts the secrets
     * @param policy checks hub URLs
     * @param settings holds {@code server.publicBaseUrl}
     * @param feeds the feed-kit specs
     * @param registry the source descriptors
     * @param sources the source settings (enabled or not)
     * @param push the shared push pipeline
     * @param clock the clock
     */
    public WebSubService(@Value("${" + ENABLED_PROPERTY + ":false}") boolean enabled,
            WebSubSubscriptionRepository subscriptions, CredentialCipher cipher, OutboundUrlPolicy policy,
            AppSettingService settings, FeedKitFamily feeds, SourceRegistry registry, SourceService sources,
            PushIngestService push, Clock clock) {
        this.enabled = enabled;
        this.subscriptions = subscriptions;
        this.cipher = cipher;
        this.policy = policy;
        this.settings = settings;
        this.feeds = feeds;
        this.registry = registry;
        this.sources = sources;
        this.push = push;
        this.clock = clock;
        this.time = new ClockTimeMeter(clock);
    }

    /** True when WebSub is enabled. */
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean active(String sourceId, Instant now) {
        if (!enabled) {
            return false;
        }
        return subscriptions.findById(sourceId)
                .filter(s -> WebSubSubscriptionEntity.ACTIVE.equals(s.getState()))
                .filter(s -> s.getLeaseExpiresAt() != null && now.isBefore(s.getLeaseExpiresAt()))
                .isPresent();
    }

    @Override
    public void afterPoll(FetchContext ctx, FeedSourceSpec spec, FeedDocument document) {
        if (!enabled || !spec.wantsWebSub()) {
            return;
        }
        String base = publicBaseUrl();
        if (base == null || !base.startsWith("https://")) {
            ctx.log().debug("{}: WebSub needs an https publicBaseUrl; polling only", ctx.sourceId());
            return;
        }
        String hub = document.hubs().isEmpty() ? null : document.hubs().get(0);
        String topic = document.self();
        if (hub == null || topic == null) {
            return;
        }
        Instant now = ctx.now();
        WebSubSubscriptionEntity existing = subscriptions.findById(spec.id()).orElse(null);
        if (existing != null && hub.equals(existing.getHub()) && topic.equals(existing.getTopic())) {
            if (WebSubSubscriptionEntity.ACTIVE.equals(existing.getState())) {
                return;
            }
            if (WebSubSubscriptionEntity.PENDING.equals(existing.getState()) && existing.getRequestedAt() != null
                    && now.isBefore(existing.getRequestedAt().plus(RETRY_PENDING_AFTER))) {
                return;
            }
        }
        subscribe(ctx, spec, existing, hub, topic, base, false);
    }

    @Override
    public void maintain(FetchContext ctx, FeedSourceSpec spec) {
        if (!enabled || !spec.wantsWebSub()) {
            return;
        }
        WebSubSubscriptionEntity s = subscriptions.findById(spec.id()).orElse(null);
        if (s == null || !WebSubSubscriptionEntity.ACTIVE.equals(s.getState())) {
            return;
        }
        Instant now = ctx.now();
        if (s.getLeaseExpiresAt() == null || !now.isBefore(s.getLeaseExpiresAt())) {
            s.setState(WebSubSubscriptionEntity.LOST);
            subscriptions.save(s);
            ctx.state().degrade(LEASE_LOST);
            ctx.log().warn("{}: the WebSub lease expired without renewal; polling again", ctx.sourceId());
            return;
        }
        if (LEASE_LOST.equals(ctx.state().get(ai.foresignal.source.spi.AdapterStateStore.DEGRADED_REASON_KEY)
                .orElse(null))) {
            ctx.state().clearDegraded();
        }
        boolean requestedSinceVerified = s.getRequestedAt() != null && s.getLastVerifiedAt() != null
                && s.getRequestedAt().isAfter(s.getLastVerifiedAt())
                && now.isBefore(s.getRequestedAt().plus(RETRY_PENDING_AFTER));
        if (renewalDue(s, now) && !requestedSinceVerified) {
            String base = publicBaseUrl();
            if (base == null || !base.startsWith("https://")) {
                return;
            }
            subscribe(ctx, spec, s, s.getHub(), s.getTopic(), base, true);
        }
    }

    /**
     * True when {@code s} has passed {@value #RENEW_AT} of its lease.
     *
     * @param s the subscription
     * @param now the current time
     */
    public static boolean renewalDue(WebSubSubscriptionEntity s, Instant now) {
        if (s.getLastVerifiedAt() == null || s.getLeaseExpiresAt() == null) {
            return false;
        }
        long lease = Duration.between(s.getLastVerifiedAt(), s.getLeaseExpiresAt()).toMillis();
        Instant due = s.getLastVerifiedAt().plusMillis((long) (lease * RENEW_AT));
        return !now.isBefore(due);
    }

    /** Sends {@code hub.mode=subscribe}; a renewal keeps the token and the secret. */
    private void subscribe(FetchContext ctx, FeedSourceSpec spec, WebSubSubscriptionEntity existing, String hub,
            String topic, String base, boolean renewal) {
        URI hubUri;
        try {
            hubUri = URI.create(hub);
        } catch (IllegalArgumentException e) {
            return;
        }
        OutboundUrlPolicy.Result check = policy.check(hubUri, OutboundUrlPolicy.Profile.PUBLIC_ONLY);
        if (check instanceof OutboundUrlPolicy.Denied denied) {
            ctx.log().warn("{}: the WebSub hub {} is refused by the outbound URL policy: {}", ctx.sourceId(),
                    hubUri.getHost(), denied.reason());
            return;
        }
        Instant now = ctx.now();
        boolean sameSubscription = existing != null && hub.equals(existing.getHub())
                && topic.equals(existing.getTopic());
        String token = existing != null ? existing.getToken() : newToken();
        String secret;
        WebSubSubscriptionEntity s;
        if (existing != null && (renewal || sameSubscription)) {
            s = existing;
            secret = decryptSecret(s.getSecretEnc());
        } else {
            secret = newToken();
            if (existing == null) {
                s = new WebSubSubscriptionEntity(spec.id(), hub, topic, token, encryptSecret(secret));
            } else {
                s = existing;
                s.setHub(hub);
                s.setTopic(topic);
                s.setSecretEnc(encryptSecret(secret));
                s.setState(WebSubSubscriptionEntity.PENDING);
                s.setLeaseExpiresAt(null);
                s.setLastVerifiedAt(null);
            }
        }
        String callback = base + CALLBACK_PATH + spec.id() + "/" + token;
        String form = "hub.mode=subscribe&hub.topic=" + KitUrls.encode(topic) + "&hub.callback="
                + KitUrls.encode(callback) + "&hub.secret=" + KitUrls.encode(secret) + "&hub.lease_seconds="
                + LEASE_SECONDS;
        boolean accepted;
        try {
            UpstreamResponse response = ctx.http().post(hubUri, "application/x-www-form-urlencoded",
                    form.getBytes(StandardCharsets.UTF_8), Map.of());
            accepted = response.status() >= 200 && response.status() < 300;
            if (!accepted) {
                ctx.log().warn("{}: the WebSub hub answered HTTP {} to the subscribe request", ctx.sourceId(),
                        response.status());
            }
        } catch (AdapterException | RuntimeException e) {
            ctx.log().warn("{}: the WebSub subscribe request failed: {}", ctx.sourceId(), e.getMessage());
            accepted = false;
        }
        s.setRequestedAt(now);
        if (accepted) {
            if (!renewal) {
                s.setState(WebSubSubscriptionEntity.PENDING);
            }
        } else {
            boolean wasActive = WebSubSubscriptionEntity.ACTIVE.equals(s.getState());
            s.setState(WebSubSubscriptionEntity.LOST);
            if (wasActive || renewal) {
                ctx.state().degrade(LEASE_LOST);
            }
        }
        subscriptions.save(s);
    }

    /**
     * Handles the hub's intent verification.
     *
     * @param sourceId the source in the callback path
     * @param token the token in the callback path
     * @param mode {@code hub.mode}
     * @param topic {@code hub.topic}
     * @param challenge {@code hub.challenge}
     * @param leaseSeconds {@code hub.lease_seconds}, or null
     * @return the body to answer 200 with (the challenge, or empty for an acknowledged denial); empty when the
     *     callback must answer 404
     */
    public Optional<String> verify(String sourceId, String token, String mode, String topic, String challenge,
            Long leaseSeconds) {
        if (!enabled || sourceId == null || token == null || mode == null || topic == null) {
            return Optional.empty();
        }
        WebSubSubscriptionEntity s = subscriptions.findById(sourceId).orElse(null);
        if (s == null || !tokenMatches(s.getToken(), token) || !s.getTopic().equals(topic)) {
            return Optional.empty();
        }
        Instant now = clock.instant();
        switch (mode) {
            case "subscribe" -> {
                boolean known = WebSubSubscriptionEntity.PENDING.equals(s.getState())
                        || WebSubSubscriptionEntity.ACTIVE.equals(s.getState());
                if (!known || challenge == null || challenge.isEmpty()) {
                    return Optional.empty();
                }
                long lease = leaseSeconds == null || leaseSeconds <= 0 ? LEASE_SECONDS : leaseSeconds;
                s.setState(WebSubSubscriptionEntity.ACTIVE);
                s.setLastVerifiedAt(now);
                s.setLeaseExpiresAt(now.plusSeconds(lease));
                subscriptions.save(s);
                log.info("WebSub subscription of {} verified; lease {} s", sourceId, lease);
                return Optional.of(challenge);
            }
            case "denied" -> {
                s.setState(WebSubSubscriptionEntity.LOST);
                subscriptions.save(s);
                log.warn("The WebSub hub denied the subscription of {}", sourceId);
                return Optional.of("");
            }
            default -> {
                // Foresignal never asks to unsubscribe, so an unsubscribe verification is not ours.
                return Optional.empty();
            }
        }
    }

    /**
     * Handles a content delivery.
     *
     * @param sourceId the source in the callback path
     * @param token the token in the callback path
     * @param signature the {@code X-Hub-Signature} header, or null
     * @param body the raw body, at most {@value #MAX_BODY_BYTES} bytes
     * @param contentType the declared content type, or null
     * @return how many records were handed over
     * @throws NotFoundException when WebSub is off or no active subscription matches (404)
     * @throws ForesignalException {@code 429 RATE_LIMITED}, {@code 403 FORBIDDEN} for a bad signature,
     *     {@code 415 UNSUPPORTED_MEDIA_TYPE} for a body that is not a feed
     */
    public int receive(String sourceId, String token, String signature, byte[] body, String contentType) {
        if (!enabled || sourceId == null || token == null) {
            throw notFound();
        }
        WebSubSubscriptionEntity s = subscriptions.findById(sourceId).orElse(null);
        if (s == null || !tokenMatches(s.getToken(), token) || !WebSubSubscriptionEntity.ACTIVE.equals(s.getState())) {
            throw notFound();
        }
        FeedSourceSpec spec = feeds.spec(sourceId);
        SourceDescriptor descriptor = registry.find(sourceId).map(SourceRegistry.Registration::descriptor)
                .orElse(null);
        if (spec == null || descriptor == null || !sources.view(sourceId).enabled()) {
            throw notFound();
        }
        Bucket bucket = rate.computeIfAbsent(sourceId, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(PUSHES_PER_MINUTE)
                        .refillIntervally(PUSHES_PER_MINUTE, Duration.ofMinutes(1)).build())
                .withCustomTimePrecision(time)
                .build());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            long wait = Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "More than " + PUSHES_PER_MINUTE + " WebSub notifications per minute for this source; retry after "
                            + wait + " s.").withHeader(HttpHeaders.RETRY_AFTER, Long.toString(wait));
        }
        if (!signatureValid(signature, body, decryptSecret(s.getSecretEnc()))) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "The X-Hub-Signature is missing or does not match.");
        }
        FeedDocument document;
        try {
            document = FeedParser.parse(body, contentType);
        } catch (UpstreamFormatException e) {
            throw new ForesignalException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                    "The body is not a recognized feed: " + e.getMessage());
        }
        FeedRecords.Mapped mapped = FeedRecords.map(spec, document);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < mapped.records().size(); i++) {
            indexes.add(i);
        }
        ParsedIngest parsed = new ParsedIngest(mapped.records(), indexes, List.of(), document.items().size());
        Instant now = clock.instant();
        push.submit(descriptor, new PushIngestService.PushRequest(sourceId, "websub", parsed, now, body.length,
                sha256(body), CALLBACK_PATH + sourceId + "/***"));
        s.setLastPushAt(now);
        subscriptions.save(s);
        return mapped.records().size();
    }

    /**
     * True when {@code header} is a valid {@code sha256=} or {@code sha1=} HMAC of {@code body} under
     * {@code secret}, compared in constant time.
     *
     * @param header the {@code X-Hub-Signature} value, or null
     * @param body the raw body
     * @param secret the subscription secret
     */
    public static boolean signatureValid(String header, byte[] body, String secret) {
        if (header == null || secret == null) {
            return false;
        }
        String value = header.strip();
        int eq = value.indexOf('=');
        if (eq < 0) {
            return false;
        }
        String method = value.substring(0, eq).toLowerCase(java.util.Locale.ROOT);
        String algorithm = switch (method) {
            case "sha256" -> "HmacSHA256";
            case "sha1" -> "HmacSHA1";
            default -> null;
        };
        if (algorithm == null) {
            return false;
        }
        byte[] given;
        try {
            given = HexFormat.of().parseHex(value.substring(eq + 1).toLowerCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            return MessageDigest.isEqual(mac.doFinal(body), given);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    /** The current subscription of {@code sourceId}, if any (operator views and tests). */
    public Optional<WebSubSubscriptionEntity> subscription(String sourceId) {
        return subscriptions.findById(sourceId);
    }

    /** Forgets every rate bucket (tests). */
    void resetRates() {
        rate.clear();
    }

    private String publicBaseUrl() {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class,
                ServerSettings::defaults);
        return server.normalizedPublicBaseUrl();
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] encryptSecret(String secret) {
        Encrypted encrypted = cipher.encrypt(secret);
        byte[] value = new byte[NONCE_LENGTH + encrypted.ciphertext().length];
        System.arraycopy(encrypted.nonce(), 0, value, 0, NONCE_LENGTH);
        System.arraycopy(encrypted.ciphertext(), 0, value, NONCE_LENGTH, encrypted.ciphertext().length);
        return value;
    }

    private String decryptSecret(byte[] value) {
        byte[] nonce = Arrays.copyOfRange(value, 0, NONCE_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(value, NONCE_LENGTH, value.length);
        return new String(cipher.decrypt(new Encrypted(ciphertext, nonce)), StandardCharsets.UTF_8);
    }

    private static boolean tokenMatches(String expected, String given) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), given.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static NotFoundException notFound() {
        return new NotFoundException("No WebSub subscription matches this callback.");
    }
}
