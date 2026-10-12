package ai.foresignal.notify.push;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.security.ApiTokenRevokedEvent;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.settings.PushSettings;
import ai.foresignal.store.PushSubscriptionEntity;
import ai.foresignal.store.PushSubscriptionRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Web Push and UnifiedPush subscriptions (prompt 031): {@code createPushSubscription},
 * {@code verifyPushSubscription}, {@code deletePushSubscription} and {@code listPushSubscriptions}.
 *
 * <p><b>Validation</b> (all problems in one 400 {@code VALIDATION_FAILED}): an {@code https} endpoint
 * of at most 1000 bytes without user info; {@code p256dh} a 65-byte uncompressed P-256 point on the
 * curve and {@code auth} 16 bytes (base64url); filters, transport label, payload format and language
 * from their enums. Then the outbound URL policy ({@link OutboundUrlPolicy} profile
 * {@code PUSH_ENDPOINT}, prompt 035; 400 {@code URL_NOT_ALLOWED} on {@code endpoint}; a host that does
 * not resolve is refused here, because the verification push follows at once)
 * and the caps: at most 5 {@code pending} per token and 200 subscriptions in total (409
 * {@code PUSH_LIMIT_REACHED}). A new subscription to an endpoint already registered replaces it.
 *
 * <p><b>Verification:</b> a new row is {@code pending}. Foresignal immediately sends it a
 * verification push {@code {v:1, event:"verify", subscriptionId, code}} (16 random bytes,
 * base64url; only its SHA-256 is stored; TTL 600, Urgency high). The creating token echoes the code;
 * a match activates the row, a mismatch is 400 {@code VERIFY_FAILED} and the fifth mismatch deletes
 * it. Pending rows expire after 10 minutes ({@link #sweep}). Only {@code active} rows receive
 * notifications, so a read-scope caller cannot use Foresignal to push to endpoints it does not
 * control.
 *
 * <p><b>Revocation:</b> revoking a token deletes its subscriptions ({@link ApiTokenRevokedEvent});
 * the sweeper also deletes subscriptions of expired or vanished tokens. Both write in their own
 * transaction, because the revoke event fires after the revoking transaction committed.
 */
@Service
public class PushSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(PushSubscriptionService.class);

    /** Status of an unverified subscription. */
    public static final String PENDING = "pending";
    /** Status of a verified subscription. */
    public static final String ACTIVE = "active";
    /** Status after three consecutive failed send-time address rechecks. */
    public static final String DISABLED = "disabled";

    /** The longest endpoint, in UTF-8 bytes. */
    public static final int MAX_ENDPOINT_BYTES = 1000;
    /** Pending subscriptions one token may have. */
    public static final int MAX_PENDING_PER_TOKEN = 5;
    /** Subscriptions in total. */
    public static final int MAX_TOTAL = 200;
    /** Verification attempts before the row is deleted. */
    public static final int MAX_VERIFY_ATTEMPTS = 5;
    /** How long a pending subscription waits for its code. */
    public static final Duration VERIFY_WINDOW = Duration.ofMinutes(10);

    static final Set<String> MEMBERS = Set.of("name", "subscription", "filters", "transportLabel", "payloadFormat", "lang");
    static final Set<String> TRANSPORT_LABELS = Set.of("browser", "unifiedpush", "other");
    static final Set<String> FORMATS = Set.of(PushPayloads.FORESIGNAL, PushPayloads.DECLARATIVE);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PushSubscriptionRepository subscriptions;
    private final OutboundUrlPolicy policy;
    private final WebPushTransport transport;
    private final ChannelService channels;
    private final PushDispatchStore dispatches;
    private final PushProperties properties;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Clock clock;

    /** Creates the service. */
    public PushSubscriptionService(PushSubscriptionRepository subscriptions, OutboundUrlPolicy policy,
            WebPushTransport transport, ChannelService channels, PushDispatchStore dispatches,
            PushProperties properties, NamedParameterJdbcTemplate jdbc, PlatformTransactionManager transactions,
            Clock clock) {
        this.subscriptions = subscriptions;
        this.policy = policy;
        this.transport = transport;
        this.channels = channels;
        this.dispatches = dispatches;
        this.properties = properties;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
    }

    /**
     * A validated {@code PushSubscriptionInput}.
     *
     * @param name the display name, or null
     * @param endpoint the push resource URL
     * @param host its host, lower case
     * @param p256dh the public key, base64url
     * @param auth the auth secret, base64url
     * @param filters the filters
     * @param transportLabel the transport label
     * @param payloadFormat the payload format
     * @param lang the language, or null
     */
    public record Input(String name, String endpoint, String host, String p256dh, String auth, PushFilters filters,
            String transportLabel, String payloadFormat, String lang) {}

    /**
     * A created subscription and the outcome of its verification push.
     *
     * @param subscription the row
     * @param verification the result of sending the verification push
     */
    public record Created(PushSubscriptionEntity subscription, PushResult verification) {}

    /**
     * Validates {@code body}, stores a {@code pending} subscription for {@code token} and sends the
     * verification push.
     *
     * @throws ValidationException 400 {@code VALIDATION_FAILED}
     * @throws ValidationException 400 {@code URL_NOT_ALLOWED} on {@code endpoint}
     * @throws ConflictException 409 {@code PUSH_LIMIT_REACHED}
     */
    public Created create(JsonNode body, AuthenticatedToken token) {
        if (token == null) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Registering a push subscription needs a token.");
        }
        Input input = parse(body);
        if (policy.check(URI.create(input.endpoint()), OutboundUrlPolicy.Profile.PUSH_ENDPOINT)
                instanceof OutboundUrlPolicy.Denied denied) {
            throw OutboundUrlPolicy.notAllowed(List.of(new FieldError("endpoint", denied.reason())));
        }
        String code = EcKeys.b64(randomBytes(16));
        Instant now = clock.instant();
        PushSubscriptionEntity row = tx.execute(status -> {
            jdbc.update("delete from push_subscription where endpoint = :endpoint",
                    new MapSqlParameterSource("endpoint", input.endpoint()));
            if (subscriptions.countByTokenIdAndStatus(token.id(), PENDING) >= MAX_PENDING_PER_TOKEN) {
                throw new ConflictException(ErrorCodes.PUSH_LIMIT_REACHED, "This token already has "
                        + MAX_PENDING_PER_TOKEN + " unverified subscriptions; verify or delete one first.");
            }
            if (subscriptions.count() >= MAX_TOTAL) {
                throw new ConflictException(ErrorCodes.PUSH_LIMIT_REACHED,
                        "Foresignal already has " + MAX_TOTAL + " push subscriptions.");
            }
            PushSubscriptionEntity e = new PushSubscriptionEntity(UUID.randomUUID(), input.endpoint(), input.p256dh(),
                    input.auth(), now);
            e.setName(input.name());
            e.setFilters(new LinkedHashMap<>(input.filters().toMap()));
            e.setTokenId(token.id());
            e.setStatus(PENDING);
            e.setVerifyCodeHash(sha256(code));
            e.setVerifyExpiresAt(now.plus(VERIFY_WINDOW));
            e.setEndpointHost(input.host());
            e.setTransportLabel(input.transportLabel());
            e.setPayloadFormat(input.payloadFormat());
            e.setLang(input.lang());
            return subscriptions.saveAndFlush(e);
        });
        PushResult verification = sendVerification(row, code);
        return new Created(subscriptions.findById(row.getId()).orElse(row), verification);
    }

    /** Sends the verification push of a pending row (TTL 600 s, Urgency high) and records the outcome. */
    PushResult sendVerification(PushSubscriptionEntity row, String code) {
        String base = channels.serverSettings().normalizedPublicBaseUrl();
        PushMessage message = PushMessage.verify(row.getId(), code, clock.millis(),
                base == null ? NotificationRouter.DEFAULT_BASE_URL : base);
        WebPushTarget target = WebPushTargetSource.target(row);
        PushResult result = transport.send(target, message, PushOptions.of(message, false));
        MapSqlParameterSource p = new MapSqlParameterSource("id", row.getId()).addValue("status", result.status())
                .addValue("at", Timestamp.from(clock.instant()));
        switch (result) {
            case PushResult.Sent s -> jdbc.update("update push_subscription set last_status = :status where id = :id", p);
            case PushResult.Gone g -> jdbc.update("delete from push_subscription where id = :id", p);
            default -> {
                jdbc.update("""
                        update push_subscription set last_status = :status, failure_count = failure_count + 1,
                               last_failure_at = :at
                         where id = :id
                        """, p);
                log.info("The verification push of subscription {} was not accepted: {}", row.getId(), result);
            }
        }
        return result;
    }

    /**
     * Activates the subscription when {@code code} matches.
     *
     * @throws NotFoundException unknown, expired, or not created by {@code token}
     * @throws ValidationException 400 {@code VERIFY_FAILED} on a mismatch (the fifth deletes the row)
     */
    public PushSubscriptionEntity verify(UUID id, JsonNode body, AuthenticatedToken token) {
        JsonNode codeNode = body == null ? null : body.get("code");
        if (body == null || !body.isObject() || codeNode == null || !codeNode.isString() || codeNode.asString().isBlank()) {
            throw ValidationException.forField("code", "is required");
        }
        String code = codeNode.asString().strip();
        Instant now = clock.instant();
        String[] outcome = {null};
        PushSubscriptionEntity result = tx.execute(status -> {
            PushSubscriptionEntity row = subscriptions.findById(id).orElse(null);
            if (row == null || token == null || !token.id().equals(row.getTokenId())) {
                return null;
            }
            if (ACTIVE.equals(row.getStatus())) {
                return row;
            }
            if (!PENDING.equals(row.getStatus()) || row.getVerifyExpiresAt() == null
                    || !now.isBefore(row.getVerifyExpiresAt())) {
                return null;
            }
            if (row.getVerifyCodeHash() != null && MessageDigest.isEqual(
                    row.getVerifyCodeHash().getBytes(StandardCharsets.US_ASCII),
                    sha256(code).getBytes(StandardCharsets.US_ASCII))) {
                row.setStatus(ACTIVE);
                row.setVerifyCodeHash(null);
                row.setVerifyExpiresAt(null);
                return subscriptions.save(row);
            }
            int attempts = row.getVerifyAttempts() + 1;
            if (attempts >= MAX_VERIFY_ATTEMPTS) {
                subscriptions.delete(row);
                outcome[0] = "deleted";
            } else {
                row.setVerifyAttempts(attempts);
                subscriptions.save(row);
                outcome[0] = "mismatch";
            }
            return null;
        });
        if (outcome[0] != null) {
            String detail = "deleted".equals(outcome[0])
                    ? "The code does not match; after " + MAX_VERIFY_ATTEMPTS + " failures the subscription was deleted."
                    : "The code does not match.";
            throw new ValidationException(ErrorCodes.VERIFY_FAILED, detail, List.of(new FieldError("code", "does not match")));
        }
        if (result == null) {
            throw NotFoundException.of("Push subscription", id);
        }
        return result;
    }

    /**
     * Deletes a subscription; only its creating token or an admin token, otherwise 404.
     *
     * @throws NotFoundException when it does not exist or belongs to another token
     */
    public void delete(UUID id, AuthenticatedToken token) {
        PushSubscriptionEntity row = subscriptions.findById(id).orElse(null);
        boolean allowed = row != null && token != null
                && (token.scope() == Scope.ADMIN || token.id().equals(row.getTokenId()));
        if (!allowed) {
            throw NotFoundException.of("Push subscription", id);
        }
        subscriptions.deleteById(id);
    }

    /** Every subscription, oldest first (admin). */
    public List<PushSubscriptionEntity> list() {
        return subscriptions.findAllByOrderByCreatedAtAscIdAsc();
    }

    /** Deletes the subscriptions of a revoked token, in its own transaction. */
    @EventListener
    public void onTokenRevoked(ApiTokenRevokedEvent event) {
        try {
            int n = jdbc.update("delete from push_subscription where token_id = :token",
                    new MapSqlParameterSource("token", event.tokenId()));
            if (n > 0) {
                log.info("Deleted {} push subscription(s) of revoked token {}", n, event.tokenId());
            }
        } catch (RuntimeException e) {
            log.error("Could not delete the push subscriptions of revoked token {}", event.tokenId(), e);
        }
    }

    /** The scheduled sweep; does nothing when {@code foresignal.push.scheduled} is false. */
    @Scheduled(fixedDelayString = "${foresignal.push.sweep-interval:60s}",
            initialDelayString = "${foresignal.push.sweep-interval:60s}")
    public void scheduledSweep() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            sweep();
        } catch (RuntimeException e) {
            log.error("Push subscription sweep failed", e);
        }
    }

    /**
     * Deletes pending subscriptions older than 10 minutes, subscriptions of tokens that are revoked,
     * expired or gone, and dispatches whose delivery retention removed.
     *
     * @return how many subscriptions were deleted
     */
    public int sweep() {
        Instant now = clock.instant();
        MapSqlParameterSource p = new MapSqlParameterSource("now", Timestamp.from(now))
                .addValue("cutoff", Timestamp.from(now.minus(VERIFY_WINDOW)));
        int expired = jdbc.update("""
                delete from push_subscription
                 where status = 'pending' and (created_at < :cutoff or verify_expires_at <= :now)
                """, p);
        int orphaned = jdbc.update("""
                delete from push_subscription s
                 where s.token_id is not null
                   and not exists (select 1 from api_token t
                                    where t.id = s.token_id and t.revoked_at is null
                                      and (t.expires_at is null or t.expires_at > :now))
                """, p);
        dispatches.deleteOrphans();
        return expired + orphaned;
    }

    /** Parses and validates a {@code PushSubscriptionInput}. */
    Input parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PushSubscriptionInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a PushSubscriptionInput member"));
            }
        }
        String name = optionalText(body, "name", errors);
        if (name != null && name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }
        String endpoint = null;
        String host = null;
        String p256dh = null;
        String auth = null;
        JsonNode subscription = body.get("subscription");
        if (subscription == null || !subscription.isObject()) {
            errors.add(new FieldError("subscription", "is required: {endpoint, keys: {p256dh, auth}}"));
        } else {
            JsonNode endpointNode = subscription.get("endpoint");
            if (endpointNode == null || !endpointNode.isString() || endpointNode.asString().isBlank()) {
                errors.add(new FieldError("subscription.endpoint", "is required"));
            } else {
                endpoint = endpointNode.asString().strip();
                host = endpointHost(endpoint, errors);
            }
            JsonNode keys = subscription.get("keys");
            if (keys == null || !keys.isObject()) {
                errors.add(new FieldError("subscription.keys", "is required: {p256dh, auth}"));
            } else {
                p256dh = key(keys.get("p256dh"), "subscription.keys.p256dh", EcKeys.POINT_LENGTH, errors);
                if (p256dh != null) {
                    try {
                        EcKeys.decodePoint(EcKeys.unb64(p256dh));
                    } catch (IllegalArgumentException e) {
                        errors.add(new FieldError("subscription.keys.p256dh", "is not a P-256 public key: " + e.getMessage()));
                        p256dh = null;
                    }
                }
                auth = key(keys.get("auth"), "subscription.keys.auth", WebPushCrypto.AUTH_LENGTH, errors);
            }
        }
        PushFilters filters = PushFilters.parse(body.get("filters"), "filters", errors);
        String label = choice(body, "transportLabel", TRANSPORT_LABELS, "browser", errors);
        String format = choice(body, "payloadFormat", FORMATS, PushPayloads.FORESIGNAL, errors);
        String lang = optionalText(body, "lang", errors);
        if (lang != null && !PushSettings.LANG.matcher(lang).matches()) {
            errors.add(new FieldError("lang", "must be a language tag such as en or pt-BR"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The push subscription is not valid.", errors);
        }
        return new Input(name, endpoint, host, p256dh, auth, filters, label, format, lang);
    }

    private static String endpointHost(String endpoint, List<FieldError> errors) {
        if (endpoint.getBytes(StandardCharsets.UTF_8).length > MAX_ENDPOINT_BYTES) {
            errors.add(new FieldError("subscription.endpoint", "must be at most " + MAX_ENDPOINT_BYTES + " bytes"));
            return null;
        }
        try {
            URI uri = new URI(endpoint);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                errors.add(new FieldError("subscription.endpoint", "must use https"));
                return null;
            }
            if (uri.getRawUserInfo() != null) {
                errors.add(new FieldError("subscription.endpoint", "must not carry user info"));
                return null;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                errors.add(new FieldError("subscription.endpoint", "must name a host"));
                return null;
            }
            if (uri.getRawFragment() != null) {
                errors.add(new FieldError("subscription.endpoint", "must not have a fragment"));
                return null;
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            return host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
        } catch (URISyntaxException e) {
            errors.add(new FieldError("subscription.endpoint", "is not a valid URL"));
            return null;
        }
    }

    private static String key(JsonNode node, String field, int length, List<FieldError> errors) {
        if (node == null || !node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(field, "is required (base64url)"));
            return null;
        }
        String value = node.asString().strip();
        try {
            byte[] bytes = EcKeys.unb64(value);
            if (bytes.length != length) {
                errors.add(new FieldError(field, "must decode to " + length + " bytes, not " + bytes.length));
                return null;
            }
            return EcKeys.b64(bytes);
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError(field, "is not base64url"));
            return null;
        }
    }

    private static String optionalText(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() ? null : value;
    }

    private static String choice(JsonNode body, String field, Set<String> allowed, String def, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return def;
        }
        if (!node.isString() || !allowed.contains(node.asString())) {
            errors.add(new FieldError(field, "must be one of " + String.join(", ", allowed.stream().sorted().toList())));
            return def;
        }
        return node.asString();
    }

    /** The API view ({@code PushSubscription}): never the endpoint, only its host. */
    public static Map<String, Object> view(PushSubscriptionEntity e) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId().toString());
        out.put("name", e.getName());
        out.put("endpointHost", e.getEndpointHost());
        out.put("filters", PushFilters.fromMap(e.getFilters()).toMap());
        out.put("createdAt", e.getCreatedAt().toString());
        out.put("lastSuccessAt", e.getLastSuccessAt() == null ? null : e.getLastSuccessAt().toString());
        out.put("status", e.getStatus());
        out.put("transportLabel", e.getTransportLabel());
        out.put("payloadFormat", e.getPayloadFormat());
        out.put("lang", e.getLang());
        out.put("failureCount", e.getFailureCount());
        out.put("lastFailureAt", e.getLastFailureAt() == null ? null : e.getLastFailureAt().toString());
        out.put("lastStatus", e.getLastStatus());
        return out;
    }

    /** Lower-case hex SHA-256 of {@code text}. */
    static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static byte[] randomBytes(int n) {
        byte[] out = new byte[n];
        RANDOM.nextBytes(out);
        return out;
    }
}
