package ai.foresignal.notify.push.device;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.PushFilters;
import ai.foresignal.notify.push.PushProperties;
import ai.foresignal.notify.push.provider.PushProviderService;
import ai.foresignal.security.ApiTokenRevokedEvent;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.ClientDeviceResolver;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.Scope;
import ai.foresignal.settings.PushSettings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
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
 * Native push devices (prompt 032): {@code registerPushDevice}, {@code listPushDevices},
 * {@code updatePushDevice} and {@code deletePushDevice}.
 *
 * <p><b>Registration</b> (read scope, always authenticated): {@code transport} {@code apns|fcm};
 * {@code platform} {@code ios|ipados|macos|android} (APNs serves only the Apple platforms); an
 * APNs token is 64–200 hex characters and needs {@code environment} {@code sandbox|production}, an
 * FCM token is 1–4096 characters; filters, name, content level and language. Then an enabled
 * provider must serve {@code appId} (and, for APNs, its key environment must cover the device's),
 * else 409 {@code PUSH_PROVIDER_NOT_CONFIGURED}. The row is upserted by ({@code transport},
 * SHA-256 of the token): re-registering moves it to the calling token. {@code deviceName} and
 * {@code appVersion} come from the calling token's 023 device metadata. A token may hold 20 devices
 * (409 {@code PUSH_LIMIT_REACHED}).
 *
 * <p><b>Visibility:</b> admin tokens see and manage every device; other tokens only their own (404
 * otherwise). Responses never contain the device token, only its last 6 characters.
 *
 * <p><b>Revocation:</b> revoking a token deletes its devices ({@link ApiTokenRevokedEvent}, in its own
 * statement because the event fires after the revoking transaction committed), and an hourly sweep
 * deletes devices of expired, revoked or deleted tokens.
 */
@Service
public class PushDeviceService {

    private static final Logger log = LoggerFactory.getLogger(PushDeviceService.class);

    /** Status of a device that receives pushes. */
    public static final String ACTIVE = "active";
    /** Status of a device that receives nothing. */
    public static final String DISABLED = "disabled";

    /** Devices one token may register. */
    public static final int MAX_PER_TOKEN = 20;

    static final Pattern APNS_TOKEN = Pattern.compile("^[0-9A-Fa-f]{64,200}$");
    static final int MAX_FCM_TOKEN = 4096;
    static final Set<String> PLATFORMS = Set.of("ios", "ipados", "macos", "android");
    static final Set<String> APPLE = Set.of("ios", "ipados", "macos");
    static final Set<String> ENVIRONMENTS = Set.of("sandbox", "production");
    static final Set<String> CONTENT_LEVELS = Set.of(NativeDeviceTarget.MINIMAL, NativeDeviceTarget.SHORT);
    static final Set<String> REGISTER_MEMBERS = Set.of("transport", "platform", "appId", "environment", "token", "name",
            "filters", "contentLevel", "lang");
    static final Set<String> UPDATE_MEMBERS = Set.of("name", "filters", "contentLevel", "lang");

    private final PushDeviceRepository devices;
    private final PushProviderService providers;
    private final CredentialCipher cipher;
    private final ClientDeviceResolver deviceResolver;
    private final PushProperties properties;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Clock clock;

    /** Creates the service. */
    public PushDeviceService(PushDeviceRepository devices, PushProviderService providers, CredentialCipher cipher,
            ClientDeviceResolver deviceResolver, PushProperties properties, NamedParameterJdbcTemplate jdbc,
            PlatformTransactionManager transactions, Clock clock) {
        this.devices = devices;
        this.providers = providers;
        this.cipher = cipher;
        this.deviceResolver = deviceResolver;
        this.properties = properties;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
    }

    /**
     * A validated {@code PushDeviceInput}.
     *
     * @param transport {@code apns} or {@code fcm}
     * @param platform the platform
     * @param appId the app
     * @param environment the APNs environment, or null
     * @param token the device token
     * @param name the display name, or null
     * @param filters the filters
     * @param contentLevel the content level
     * @param lang the language, or null
     */
    public record Input(String transport, String platform, String appId, String environment, String token,
            String name, PushFilters filters, String contentLevel, String lang) {}

    /**
     * A registration result.
     *
     * @param device the row
     * @param created true when the token was new (201), false when re-registered (200)
     */
    public record Registered(PushDeviceEntity device, boolean created) {}

    /**
     * Registers (or re-registers) a device for {@code caller}.
     *
     * @throws ValidationException 400
     * @throws ConflictException 409 {@code PUSH_PROVIDER_NOT_CONFIGURED} or {@code PUSH_LIMIT_REACHED}
     */
    public Registered register(JsonNode body, AuthenticatedToken caller) {
        requireCaller(caller);
        Input input = parse(body);
        providers.unservedReason(input.transport(), input.appId(), input.environment()).ifPresent(reason -> {
            throw new ConflictException(ErrorCodes.PUSH_PROVIDER_NOT_CONFIGURED, reason);
        });
        String hash = sha256(input.token());
        Instant now = clock.instant();
        var metadata = deviceResolver.deviceOf(caller).map(ClientDeviceResolver.ClientDevice::info).orElse(null);
        Registered result = tx.execute(status -> {
            PushDeviceEntity row = devices.findByTransportAndTokenHash(input.transport(), hash).orElse(null);
            boolean created = row == null;
            boolean moving = row != null && !caller.id().equals(row.getTokenId());
            if ((created || moving) && devices.countByTokenId(caller.id()) >= MAX_PER_TOKEN) {
                throw new ConflictException(ErrorCodes.PUSH_LIMIT_REACHED,
                        "This token already has " + MAX_PER_TOKEN + " push devices; delete one first.");
            }
            if (created) {
                row = new PushDeviceEntity(UUID.randomUUID());
                row.setCreatedAt(now);
                row.setTransport(input.transport());
                row.setTokenHash(hash);
            }
            Encrypted token = cipher.encrypt(input.token());
            row.setTokenCiphertext(token.ciphertext());
            row.setTokenNonce(token.nonce());
            row.setTokenSuffix(suffix(input.token()));
            row.setTokenId(caller.id());
            row.setPlatform(input.platform());
            row.setAppId(input.appId());
            row.setEnvironment(input.environment());
            row.setName(input.name());
            row.setFilters(new LinkedHashMap<>(input.filters().toMap()));
            row.setContentLevel(input.contentLevel());
            row.setLang(input.lang());
            row.setStatus(ACTIVE);
            row.setFailureCount(0);
            if (metadata != null) {
                row.setDeviceName(metadata.deviceName());
                row.setAppVersion(metadata.appVersion());
            }
            return new Registered(devices.saveAndFlush(row), created);
        });
        return result;
    }

    /** The devices {@code caller} may see: every device for admin, else its own. */
    public List<PushDeviceEntity> list(AuthenticatedToken caller) {
        requireCaller(caller);
        return caller.scope() == Scope.ADMIN ? devices.findAllByOrderByCreatedAtAscIdAsc()
                : devices.findByTokenIdOrderByCreatedAtAscIdAsc(caller.id());
    }

    /**
     * The device {@code id} if {@code caller} owns it or is admin.
     *
     * @throws NotFoundException otherwise
     */
    public PushDeviceEntity visible(UUID id, AuthenticatedToken caller) {
        PushDeviceEntity row = id == null ? null : devices.findById(id).orElse(null);
        if (row == null || caller == null || (caller.scope() != Scope.ADMIN && !caller.id().equals(row.getTokenId()))) {
            throw NotFoundException.of("Push device", id);
        }
        return row;
    }

    /**
     * The device {@code id} if {@code caller} registered it (test pushes are for the owner only).
     *
     * @throws NotFoundException otherwise
     */
    public PushDeviceEntity owned(UUID id, AuthenticatedToken caller) {
        PushDeviceEntity row = id == null ? null : devices.findById(id).orElse(null);
        if (row == null || caller == null || !caller.id().equals(row.getTokenId())) {
            throw NotFoundException.of("Push device", id);
        }
        return row;
    }

    /**
     * Changes name, filters, content level and language (owner or admin).
     *
     * @throws NotFoundException when not visible
     * @throws ValidationException 400
     */
    public PushDeviceEntity update(UUID id, JsonNode body, AuthenticatedToken caller) {
        requireCaller(caller);
        PushDeviceEntity row = visible(id, caller);
        Update update = parseUpdate(body);
        if (update.name().isPresent()) {
            row.setName(update.name().get().orElse(null));
        }
        update.filters().ifPresent(f -> row.setFilters(new LinkedHashMap<>(f.toMap())));
        update.contentLevel().ifPresent(row::setContentLevel);
        if (update.lang().isPresent()) {
            row.setLang(update.lang().get().orElse(null));
        }
        return devices.save(row);
    }

    /**
     * Deletes a device (owner or admin).
     *
     * @throws NotFoundException when not visible
     */
    public void delete(UUID id, AuthenticatedToken caller) {
        requireCaller(caller);
        devices.delete(visible(id, caller));
    }

    /**
     * A validated {@code PushDeviceUpdate}: each member is present only when the body carries it
     * (an inner empty optional clears a nullable member).
     *
     * @param name the new name
     * @param filters the new filters
     * @param contentLevel the new content level
     * @param lang the new language
     */
    public record Update(java.util.Optional<java.util.Optional<String>> name, java.util.Optional<PushFilters> filters,
            java.util.Optional<String> contentLevel, java.util.Optional<java.util.Optional<String>> lang) {}

    /**
     * Parses and validates a {@code PushDeviceUpdate}.
     *
     * @throws ValidationException listing every problem
     */
    public static Update parseUpdate(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PushDeviceUpdate object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!UPDATE_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a PushDeviceUpdate member"));
            }
        }
        var name = body.has("name") ? java.util.Optional.of(java.util.Optional.ofNullable(name(body, errors)))
                : java.util.Optional.<java.util.Optional<String>>empty();
        var filters = body.has("filters")
                ? java.util.Optional.of(PushFilters.parse(body.get("filters"), "filters", errors))
                : java.util.Optional.<PushFilters>empty();
        var level = body.has("contentLevel") ? java.util.Optional.of(contentLevel(body, errors))
                : java.util.Optional.<String>empty();
        var lang = body.has("lang") ? java.util.Optional.of(java.util.Optional.ofNullable(lang(body, errors)))
                : java.util.Optional.<java.util.Optional<String>>empty();
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The device update is not valid.", errors);
        }
        return new Update(name, filters, level, lang);
    }

    /** Deletes the devices of a revoked token, in its own statement. Never throws. */
    @EventListener
    public void onTokenRevoked(ApiTokenRevokedEvent event) {
        try {
            int n = jdbc.update("delete from push_device where token_id = :token",
                    new MapSqlParameterSource("token", event.tokenId()));
            if (n > 0) {
                log.info("Deleted {} push device(s) of revoked token {}", n, event.tokenId());
            }
        } catch (RuntimeException e) {
            log.error("Could not delete the push devices of revoked token {}", event.tokenId(), e);
        }
    }

    /** The hourly sweep; does nothing when {@code foresignal.push.scheduled} is false. */
    @Scheduled(fixedDelayString = "${foresignal.push.native.expiry-sweep-interval:1h}",
            initialDelayString = "${foresignal.push.native.expiry-sweep-interval:1h}")
    public void scheduledSweep() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            sweep();
        } catch (RuntimeException e) {
            log.error("Push device sweep failed", e);
        }
    }

    /**
     * Deletes devices whose token is revoked, expired or gone.
     *
     * @return how many were deleted
     */
    public int sweep() {
        return jdbc.update("""
                delete from push_device d
                 where not exists (select 1 from api_token t
                                    where t.id = d.token_id and t.revoked_at is null
                                      and (t.expires_at is null or t.expires_at > :now))
                """, new MapSqlParameterSource("now", Timestamp.from(clock.instant())));
    }

    /**
     * Parses and validates a {@code PushDeviceInput}.
     *
     * @throws ValidationException listing every problem
     */
    public static Input parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PushDeviceInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!REGISTER_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a PushDeviceInput member"));
            }
        }
        String transport = choice(body, "transport", Set.of(NativeDeviceTarget.APNS, NativeDeviceTarget.FCM), errors);
        String platform = choice(body, "platform", PLATFORMS, errors);
        String appId = text(body, "appId");
        if (appId == null || !appId.matches("^[A-Za-z0-9][A-Za-z0-9._-]{0,254}$")) {
            errors.add(new FieldError("appId", "must be a bundle id or package name"));
        }
        String environment = null;
        JsonNode envNode = body.get("environment");
        if (envNode != null && !envNode.isNull()) {
            if (!envNode.isString() || !ENVIRONMENTS.contains(envNode.asString())) {
                errors.add(new FieldError("environment", "must be sandbox or production"));
            } else {
                environment = envNode.asString();
            }
        }
        JsonNode tokenNode = body.get("token");
        String token = tokenNode != null && tokenNode.isString() ? tokenNode.asString().strip() : null;
        if (NativeDeviceTarget.APNS.equals(transport)) {
            if (token == null || !APNS_TOKEN.matcher(token).matches()) {
                errors.add(new FieldError("token", "an APNs device token is 64 to 200 hexadecimal characters"));
            } else {
                token = token.toLowerCase(java.util.Locale.ROOT);
            }
            if (environment == null) {
                errors.add(new FieldError("environment", "is required for APNs: sandbox or production"));
            }
            if (platform != null && !APPLE.contains(platform)) {
                errors.add(new FieldError("platform", "APNs serves ios, ipados and macos"));
            }
        } else if (NativeDeviceTarget.FCM.equals(transport)) {
            if (token == null || token.isEmpty() || token.length() > MAX_FCM_TOKEN) {
                errors.add(new FieldError("token", "an FCM registration token is 1 to " + MAX_FCM_TOKEN + " characters"));
            }
            // The APNs environment does not apply to FCM; Firebase picks it from the app's configuration.
            environment = null;
        }
        String name = name(body, errors);
        PushFilters filters = PushFilters.parse(body.get("filters"), "filters", errors);
        String level = body.has("contentLevel") ? contentLevel(body, errors) : NativeDeviceTarget.MINIMAL;
        String lang = lang(body, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The push device is not valid.", errors);
        }
        return new Input(transport, platform, appId, environment, token, name, filters, level, lang);
    }

    private static String name(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("name");
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || node.asString().length() > 200) {
            errors.add(new FieldError("name", "must be a string of at most 200 characters"));
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() ? null : value;
    }

    private static String contentLevel(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("contentLevel");
        if (node == null || node.isNull()) {
            return NativeDeviceTarget.MINIMAL;
        }
        if (!node.isString() || !CONTENT_LEVELS.contains(node.asString())) {
            errors.add(new FieldError("contentLevel", "must be minimal or short"));
            return NativeDeviceTarget.MINIMAL;
        }
        return node.asString();
    }

    private static String lang(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("lang");
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || !PushSettings.LANG.matcher(node.asString().strip()).matches()) {
            errors.add(new FieldError("lang", "must be a language tag such as en or pt-BR"));
            return null;
        }
        return node.asString().strip();
    }

    private static String choice(JsonNode body, String field, Set<String> allowed, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || !node.isString() || !allowed.contains(node.asString())) {
            errors.add(new FieldError(field, "must be one of " + String.join(", ", allowed.stream().sorted().toList())));
            return null;
        }
        return node.asString();
    }

    private static String text(JsonNode body, String field) {
        JsonNode node = body.get(field);
        return node == null || !node.isString() || node.asString().isBlank() ? null : node.asString().strip();
    }

    private static void requireCaller(AuthenticatedToken caller) {
        if (caller == null) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Native push devices need a token.");
        }
    }

    /** The last 6 characters of a device token. */
    static String suffix(String token) {
        return token.length() <= 6 ? token : token.substring(token.length() - 6);
    }

    /** Lower-case hex SHA-256 of {@code text}. */
    static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** The API view ({@code PushDevice}): never the token, only {@code tokenSuffix}. */
    public static Map<String, Object> view(PushDeviceEntity e) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId().toString());
        out.put("transport", e.getTransport());
        out.put("platform", e.getPlatform());
        out.put("appId", e.getAppId());
        out.put("environment", e.getEnvironment());
        out.put("tokenSuffix", e.getTokenSuffix());
        out.put("tokenId", e.getTokenId().toString());
        out.put("name", e.getName());
        out.put("deviceName", e.getDeviceName());
        out.put("appVersion", e.getAppVersion());
        out.put("filters", PushFilters.fromMap(e.getFilters()).toMap());
        out.put("contentLevel", e.getContentLevel());
        out.put("lang", e.getLang());
        out.put("status", e.getStatus());
        out.put("createdAt", e.getCreatedAt().toString());
        out.put("lastSuccessAt", e.getLastSuccessAt() == null ? null : e.getLastSuccessAt().toString());
        out.put("lastFailureAt", e.getLastFailureAt() == null ? null : e.getLastFailureAt().toString());
        out.put("failureCount", e.getFailureCount());
        out.put("lastStatus", e.getLastStatus());
        return out;
    }
}
