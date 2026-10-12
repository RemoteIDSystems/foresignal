package ai.foresignal.notify.push.provider;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.NativePushHttp;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The native push providers behind {@code /v1/push/providers} (prompt 032): the operator's own APNs
 * auth key and Firebase service account.
 *
 * <p><b>Self-hosting constraint.</b> An APNs key belongs to the Apple team that signs the app, and a
 * Firebase service account to the Firebase project compiled into the app. An operator can therefore
 * only push to an app they build and sign themselves (or through a publisher's relay, which
 * Foresignal does not provide). UnifiedPush and ntfy remain the paths that need no account.
 *
 * <p><b>Validation</b> (all problems in one 400): the slug id; {@code type} {@code apns|fcm};
 * {@code appIds} 1–50 bundle ids or package names; the config of the type (APNs {@code teamId} and
 * {@code keyId} of 10 characters, {@code keyEnvironment} {@code sandbox|production|both},
 * {@code timeSensitive}; FCM {@code projectId}, {@code clientEmail}, {@code tokenUri}). Secrets: the
 * APNs {@code p8} must parse as an EC P-256 PKCS#8 key; the FCM {@code serviceAccountJson} must hold
 * an RSA {@code private_key}, and its {@code project_id}, {@code client_email} and {@code token_uri}
 * fill config members left out. Only the PEM key is stored, encrypted with {@link CredentialCipher}.
 * A replace that omits {@code secrets} keeps the stored secret (unless the type changes). Responses
 * never contain a secret: {@code secretFields} lists what is set.
 *
 * <p><b>Health.</b> {@link #providerError} records a credential problem in {@code last_error} (a
 * short code, never a key, a device token or alert text) and raises one {@code system} notification
 * when the provider turns from healthy to failing, never routed to the Push channel;
 * {@link #providerOk} clears it.
 */
@Service
public class PushProviderService {

    private static final Logger log = LoggerFactory.getLogger(PushProviderService.class);

    /** The provider id pattern (the spec's slug). */
    public static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** Apple Team and Key ids: 10 upper-case letters or digits. */
    static final Pattern APPLE_ID = Pattern.compile("^[A-Z0-9]{10}$");

    /** A bundle id or Android package name. */
    static final Pattern APP_ID = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,254}$");

    /** A Firebase project id (permissive: legacy ids carry a domain prefix). */
    static final Pattern PROJECT_ID = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9.:_-]{0,99}$");

    /** The default OAuth 2.0 token endpoint of service accounts. */
    public static final String DEFAULT_TOKEN_URI = "https://oauth2.googleapis.com/token";

    /** The APNs secret field. */
    public static final String P8 = "p8";

    /** The FCM secret field. */
    public static final String SERVICE_ACCOUNT = "serviceAccountJson";

    /** The most apps one provider serves. */
    static final int MAX_APP_IDS = 50;

    static final Set<String> TYPES = Set.of(NativeDeviceTarget.APNS, NativeDeviceTarget.FCM);
    static final Set<String> INPUT_MEMBERS = Set.of("type", "appIds", "config", "secrets", "enabled", "id",
            "secretFields", "lastTestAt", "lastError", "updatedAt");
    static final Set<String> KEY_ENVIRONMENTS = Set.of("sandbox", "production", "both");

    private final PushProviderRepository repository;
    private final CredentialCipher cipher;
    private final NamedParameterJdbcTemplate jdbc;
    private final NotificationRouter router;
    private final NativePushHttp http;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public PushProviderService(PushProviderRepository repository, CredentialCipher cipher,
            NamedParameterJdbcTemplate jdbc, NotificationRouter router, NativePushHttp http, JsonMapper json,
            Clock clock) {
        this.repository = repository;
        this.cipher = cipher;
        this.jdbc = jdbc;
        this.router = router;
        this.http = http;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A validated {@code PushProviderInput}.
     *
     * @param type the type
     * @param appIds the apps served
     * @param config the non-secret configuration
     * @param secret the PEM key to store, or null to keep the stored one
     * @param enabled whether the provider is used
     */
    public record Input(String type, List<String> appIds, Map<String, Object> config, String secret, boolean enabled) {}

    /**
     * The result of a put.
     *
     * @param provider the stored row
     * @param created true when it did not exist before
     */
    public record Put(PushProviderEntity provider, boolean created) {}

    /** Every provider, by id. */
    @Transactional(readOnly = true)
    public List<PushProviderEntity> list() {
        return repository.findAllByOrderByIdAsc();
    }

    /**
     * One provider.
     *
     * @throws NotFoundException when it does not exist
     */
    @Transactional(readOnly = true)
    public PushProviderEntity get(String id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Push provider", id));
    }

    /**
     * Creates or fully replaces provider {@code id}; omitted secrets keep the stored ones.
     *
     * @throws ValidationException listing every problem
     */
    @Transactional
    public Put put(String id, JsonNode body) {
        if (id == null || !SLUG.matcher(id).matches() || id.length() > 64) {
            throw ValidationException.forField("providerId", "must be a slug of at most 64 characters");
        }
        PushProviderEntity existing = repository.findById(id).orElse(null);
        Input input = parse(body);
        boolean typeChanged = existing != null && !existing.getType().equals(input.type());
        if (input.secret() == null && (existing == null || !existing.hasSecret() || typeChanged)) {
            String field = NativeDeviceTarget.APNS.equals(input.type()) ? P8 : SERVICE_ACCOUNT;
            throw ValidationException.forField("secrets." + field, existing == null
                    ? "is required for a new provider" : "is required when the type changes");
        }
        PushProviderEntity entity = existing == null ? new PushProviderEntity(id, input.type()) : existing;
        apply(entity, input);
        PushProviderEntity saved = repository.save(entity);
        evict(id);
        return new Put(saved, existing == null);
    }

    private void apply(PushProviderEntity entity, Input input) {
        entity.setType(input.type());
        entity.setAppIds(input.appIds());
        entity.setConfig(input.config());
        entity.setEnabled(input.enabled());
        if (input.secret() != null) {
            Map<String, String> secret = Map.of(NativeDeviceTarget.APNS.equals(input.type()) ? P8 : "privateKey",
                    input.secret());
            Encrypted encrypted = cipher.encrypt(secret);
            entity.setSecret(encrypted.ciphertext(), encrypted.nonce());
            entity.setLastError(null);
        }
        entity.setUpdatedAt(clock.instant());
    }

    /**
     * Imports one provider of a settings bundle (a {@code PushProvider} without secrets): an existing
     * one is replaced and keeps its secret; a new one is stored disabled, because it has no key yet.
     *
     * @return true when the provider is new and was stored disabled
     * @throws ValidationException when it is not valid
     */
    @Transactional
    public boolean importProvider(JsonNode node) {
        if (node == null || !node.isObject() || node.get("id") == null || !node.get("id").isString()) {
            throw ValidationException.forField("id", "is required");
        }
        String id = node.get("id").asString();
        if (!SLUG.matcher(id).matches() || id.length() > 64) {
            throw ValidationException.forField("id", "must be a slug of at most 64 characters");
        }
        PushProviderEntity existing = repository.findById(id).orElse(null);
        Input input = parse(node);
        boolean keyless = existing == null || !existing.hasSecret() || !existing.getType().equals(input.type());
        if (keyless && input.secret() == null) {
            input = new Input(input.type(), input.appIds(), input.config(), null, false);
        }
        PushProviderEntity entity = existing == null ? new PushProviderEntity(id, input.type()) : existing;
        if (keyless && input.secret() == null) {
            entity.setSecret(null, null);
        }
        apply(entity, input);
        repository.save(entity);
        evict(id);
        return keyless && input.secret() == null;
    }

    /**
     * Deletes a provider; devices of its apps stop receiving pushes until another provider serves
     * them.
     *
     * @throws NotFoundException when it does not exist
     */
    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw NotFoundException.of("Push provider", id);
        }
        repository.deleteById(id);
        evict(id);
    }

    private void evict(String id) {
        http.evict(apnsClientKey(id, "sandbox"));
        http.evict(apnsClientKey(id, "production"));
    }

    /** The HTTP client key of an APNs provider in one environment: one connection per provider. */
    public static String apnsClientKey(String providerId, String environment) {
        return "apns:" + providerId + ":" + environment;
    }

    /**
     * The credentials that serve {@code appId} on {@code type} (and, for APNs, {@code environment}):
     * the first enabled provider by id with a usable key.
     */
    @Transactional(readOnly = true)
    public Optional<ProviderCredentials> credentialsFor(String type, String appId, String environment) {
        for (PushProviderEntity provider : repository.findAllByOrderByIdAsc()) {
            if (!provider.isEnabled() || !provider.getType().equals(type) || !provider.getAppIds().contains(appId)) {
                continue;
            }
            Optional<ProviderCredentials> credentials = credentials(provider);
            if (credentials.isEmpty()) {
                continue;
            }
            if (credentials.get() instanceof ProviderCredentials.Apns apns && !apns.covers(environment)) {
                continue;
            }
            return credentials;
        }
        return Optional.empty();
    }

    /**
     * Why no provider serves a registration, or empty when one does: no enabled provider lists the
     * app, or (APNs) none whose key environment covers {@code environment}.
     */
    @Transactional(readOnly = true)
    public Optional<String> unservedReason(String type, String appId, String environment) {
        List<PushProviderEntity> serving = repository.findAllByOrderByIdAsc().stream()
                .filter(p -> p.isEnabled() && p.getType().equals(type) && p.getAppIds().contains(appId)
                        && p.hasSecret())
                .toList();
        if (serving.isEmpty()) {
            return Optional.of("No enabled " + type.toUpperCase(Locale.ROOT) + " provider serves app '" + appId
                    + "'; an admin configures one at PUT /v1/push/providers/{providerId}.");
        }
        if (NativeDeviceTarget.APNS.equals(type) && serving.stream()
                .noneMatch(p -> covers(String.valueOf(p.getConfig().get("keyEnvironment")), environment))) {
            return Optional.of("No APNs key for app '" + appId + "' covers the " + environment
                    + " environment; APNs keys are environment-specific.");
        }
        return Optional.empty();
    }

    private static boolean covers(String keyEnvironment, String environment) {
        return "both".equals(keyEnvironment) || keyEnvironment.equals(environment);
    }

    /** The decrypted credentials of {@code provider}, or empty when it has no usable key. */
    public Optional<ProviderCredentials> credentials(PushProviderEntity provider) {
        if (!provider.hasSecret()) {
            return Optional.empty();
        }
        String fingerprint = provider.getId() + ":" + provider.getUpdatedAt().toEpochMilli();
        Map<String, Object> config = provider.getConfig();
        try {
            Map<String, String> secret = cipher.decryptToMap(
                    new Encrypted(provider.getSecretCiphertext(), provider.getSecretNonce()));
            if (NativeDeviceTarget.APNS.equals(provider.getType())) {
                ECPrivateKey key = ProviderKeys.apnsKey(secret.get(P8));
                return Optional.of(new ProviderCredentials.Apns(provider.getId(), text(config.get("teamId")),
                        text(config.get("keyId")), text(config.get("keyEnvironment")),
                        Boolean.TRUE.equals(config.get("timeSensitive")), key, fingerprint));
            }
            RSAPrivateKey key = ProviderKeys.rsaKey(secret.get("privateKey"));
            Object tokenUri = config.get("tokenUri");
            return Optional.of(new ProviderCredentials.Fcm(provider.getId(), text(config.get("projectId")),
                    text(config.get("clientEmail")), tokenUri == null ? DEFAULT_TOKEN_URI : text(tokenUri), key,
                    fingerprint));
        } catch (RuntimeException e) {
            log.error("The key of push provider {} cannot be used: {}", provider.getId(), e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    /**
     * Records a credential problem of provider {@code id} ({@code error} is a short code-level
     * description) and, when it was healthy, raises one {@code system.push_provider_failed}
     * notification that is never routed to the Push channel. Never throws.
     */
    public void providerError(String id, String error) {
        try {
            MapSqlParameterSource p = new MapSqlParameterSource("id", id).addValue("error", cut(error));
            int first = jdbc.update("update push_provider set last_error = :error where id = :id and last_error is null", p);
            if (first == 0) {
                jdbc.update("update push_provider set last_error = :error where id = :id", p);
                return;
            }
            String type = jdbc.queryForList("select type from push_provider where id = :id", p, String.class)
                    .stream().findFirst().orElse("push");
            router.pushProviderFailed(id, type, cut(error));
        } catch (RuntimeException e) {
            log.warn("Could not record the error of push provider {}: {}", id, e.getMessage());
        }
    }

    /** Clears the last error of provider {@code id} after a success. Never throws. */
    public void providerOk(String id) {
        try {
            jdbc.update("update push_provider set last_error = null where id = :id and last_error is not null",
                    new MapSqlParameterSource("id", id));
        } catch (RuntimeException e) {
            log.warn("Could not clear the error of push provider {}: {}", id, e.getMessage());
        }
    }

    /** Records a provider test: {@code last_test_at} and the error (null on success). */
    public void recordTest(String id, String error) {
        jdbc.update("update push_provider set last_test_at = :at, last_error = :error where id = :id",
                new MapSqlParameterSource("id", id).addValue("at", java.sql.Timestamp.from(clock.instant()))
                        .addValue("error", cut(error)));
    }

    /** Parses and validates a {@code PushProviderInput}. */
    Input parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PushProviderInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!INPUT_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a PushProviderInput member"));
            }
        }
        JsonNode typeNode = body.get("type");
        String type = typeNode != null && typeNode.isString() && TYPES.contains(typeNode.asString())
                ? typeNode.asString() : null;
        if (type == null) {
            errors.add(new FieldError("type", "must be apns or fcm"));
        }
        List<String> appIds = new ArrayList<>();
        JsonNode apps = body.get("appIds");
        if (apps == null || !apps.isArray() || apps.isEmpty() || apps.size() > MAX_APP_IDS) {
            errors.add(new FieldError("appIds", "must list 1 to " + MAX_APP_IDS + " bundle ids or package names"));
        } else {
            for (JsonNode app : apps) {
                if (!app.isString() || !APP_ID.matcher(app.asString()).matches()) {
                    errors.add(new FieldError("appIds", "'" + app.asString() + "' is not a bundle id or package name"));
                } else if (!appIds.contains(app.asString())) {
                    appIds.add(app.asString());
                }
            }
        }
        JsonNode enabledNode = body.get("enabled");
        boolean enabled = true;
        if (enabledNode != null && !enabledNode.isNull()) {
            if (!enabledNode.isBoolean()) {
                errors.add(new FieldError("enabled", "must be a boolean"));
            } else {
                enabled = enabledNode.asBoolean();
            }
        }
        JsonNode config = body.get("config");
        if (config == null || !config.isObject()) {
            errors.add(new FieldError("config", "is required"));
            config = json.createObjectNode();
        }
        JsonNode secrets = body.get("secrets");
        if (secrets != null && !secrets.isNull() && !secrets.isObject()) {
            errors.add(new FieldError("secrets", "must be an object"));
            secrets = null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        String secret = null;
        if (NativeDeviceTarget.APNS.equals(type)) {
            secret = apns(config, secrets, out, errors);
        } else if (NativeDeviceTarget.FCM.equals(type)) {
            secret = fcm(config, secrets, out, errors);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The push provider is not valid.", errors);
        }
        return new Input(type, appIds, out, secret, enabled);
    }

    private String apns(JsonNode config, JsonNode secrets, Map<String, Object> out, List<FieldError> errors) {
        members(config, Set.of("teamId", "keyId", "keyEnvironment", "timeSensitive"), "config", errors);
        String team = string(config, "teamId");
        if (team == null || !APPLE_ID.matcher(team).matches()) {
            errors.add(new FieldError("config.teamId", "must be the 10-character Apple Team ID"));
        }
        String key = string(config, "keyId");
        if (key == null || !APPLE_ID.matcher(key).matches()) {
            errors.add(new FieldError("config.keyId", "must be the 10-character Key ID of the .p8 key"));
        }
        String environment = string(config, "keyEnvironment");
        if (environment == null || !KEY_ENVIRONMENTS.contains(environment)) {
            errors.add(new FieldError("config.keyEnvironment", "must be sandbox, production or both"));
        }
        JsonNode sensitive = config.get("timeSensitive");
        boolean timeSensitive = false;
        if (sensitive != null && !sensitive.isNull()) {
            if (!sensitive.isBoolean()) {
                errors.add(new FieldError("config.timeSensitive", "must be a boolean"));
            } else {
                timeSensitive = sensitive.asBoolean();
            }
        }
        out.put("teamId", team);
        out.put("keyId", key);
        out.put("keyEnvironment", environment);
        out.put("timeSensitive", timeSensitive);
        if (secrets == null) {
            return null;
        }
        members(secrets, Set.of(P8), "secrets", errors);
        JsonNode p8 = secrets.get(P8);
        if (p8 == null || p8.isNull()) {
            return null;
        }
        if (!p8.isString()) {
            errors.add(new FieldError("secrets.p8", "must be the .p8 PEM text"));
            return null;
        }
        try {
            ProviderKeys.apnsKey(p8.asString());
            return ProviderKeys.normalize(p8.asString());
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("secrets.p8", e.getMessage()));
            return null;
        }
    }

    private String fcm(JsonNode config, JsonNode secrets, Map<String, Object> out, List<FieldError> errors) {
        members(config, Set.of("projectId", "clientEmail", "tokenUri"), "config", errors);
        String project = string(config, "projectId");
        String email = string(config, "clientEmail");
        String tokenUri = string(config, "tokenUri");
        String secret = null;
        if (secrets != null) {
            members(secrets, Set.of(SERVICE_ACCOUNT), "secrets", errors);
            JsonNode account = secrets.get(SERVICE_ACCOUNT);
            if (account != null && !account.isNull()) {
                JsonNode parsed = null;
                if (account.isString()) {
                    try {
                        parsed = json.readTree(account.asString());
                    } catch (RuntimeException e) {
                        errors.add(new FieldError("secrets.serviceAccountJson", "is not JSON"));
                    }
                } else if (account.isObject()) {
                    parsed = account;
                } else {
                    errors.add(new FieldError("secrets.serviceAccountJson", "must be the service-account JSON"));
                }
                if (parsed != null) {
                    if (!parsed.isObject()) {
                        errors.add(new FieldError("secrets.serviceAccountJson", "must be a JSON object"));
                    } else {
                        String accountType = string(parsed, "type");
                        if (accountType != null && !"service_account".equals(accountType)) {
                            errors.add(new FieldError("secrets.serviceAccountJson", "is not a service-account key"));
                        }
                        project = project == null ? string(parsed, "project_id") : project;
                        email = email == null ? string(parsed, "client_email") : email;
                        tokenUri = tokenUri == null ? string(parsed, "token_uri") : tokenUri;
                        String pem = string(parsed, "private_key");
                        if (pem == null) {
                            errors.add(new FieldError("secrets.serviceAccountJson", "has no private_key"));
                        } else {
                            try {
                                ProviderKeys.rsaKey(pem);
                                secret = ProviderKeys.normalize(pem);
                            } catch (IllegalArgumentException e) {
                                errors.add(new FieldError("secrets.serviceAccountJson",
                                        "private_key " + e.getMessage()));
                            }
                        }
                    }
                }
            }
        }
        if (project == null || !PROJECT_ID.matcher(project).matches()) {
            errors.add(new FieldError("config.projectId", "must be the Firebase project id"));
        }
        if (email == null || email.length() > 320 || !email.matches("^[^@\\s]+@[^@\\s]+$")) {
            errors.add(new FieldError("config.clientEmail", "must be the service account's email"));
        }
        tokenUri = tokenUri == null ? DEFAULT_TOKEN_URI : tokenUri;
        if (!httpUrl(tokenUri)) {
            errors.add(new FieldError("config.tokenUri", "must be an absolute https URL (http only for a local stand-in)"));
        }
        out.put("projectId", project);
        out.put("clientEmail", email);
        out.put("tokenUri", tokenUri);
        return secret;
    }

    private static boolean httpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            // http is accepted for a local stand-in; Google's endpoint is https.
            return ("https".equals(scheme) || "http".equals(scheme)) && uri.getHost() != null
                    && uri.getRawUserInfo() == null && value.length() <= 2048;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static void members(JsonNode node, Set<String> allowed, String prefix, List<FieldError> errors) {
        for (String member : node.propertyNames()) {
            if (!allowed.contains(member)) {
                errors.add(new FieldError(prefix + "." + member, "is not a member here"));
            }
        }
    }

    private static String string(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isString() || value.asString().isBlank()) {
            return null;
        }
        return value.asString().strip();
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String cut(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }

    /** The API view ({@code PushProvider}): never a secret, only {@code secretFields}. */
    public static Map<String, Object> view(PushProviderEntity e) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId());
        out.put("type", e.getType());
        out.put("appIds", new ArrayList<>(e.getAppIds()));
        out.put("config", new LinkedHashMap<>(e.getConfig()));
        out.put("secretFields", e.hasSecret()
                ? List.of(NativeDeviceTarget.APNS.equals(e.getType()) ? P8 : SERVICE_ACCOUNT) : List.of());
        out.put("enabled", e.isEnabled());
        out.put("lastTestAt", e.getLastTestAt() == null ? null : e.getLastTestAt().toString());
        out.put("lastError", e.getLastError());
        out.put("updatedAt", e.getUpdatedAt() == null ? null : e.getUpdatedAt().toString());
        return out;
    }
}
