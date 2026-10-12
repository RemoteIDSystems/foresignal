package ai.foresignal.security.feed;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.RestrictionValidator;
import ai.foresignal.security.Scope;
import ai.foresignal.security.SpecOperationIndex;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.restrict.PathKeyFilter;
import ai.foresignal.security.restrict.PathKeyResolver;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Feed keys for header-less clients (prompt 035): {@code api_token} rows with
 * {@code type = feed_key}, prefix {@value #KEY_PREFIX}, scope {@code read}, a mandatory read
 * restriction and the canonical path prefixes the key may read. Hashed, listed and revoked like
 * tokens, with {@code last_used_at} throttled the same way. Clients call {@code /v1/k/{key}/<rest>};
 * {@link PathKeyFilter} resolves the key through this service, which is its {@link PathKeyResolver}
 * for {@value #KEY_PREFIX}.
 *
 * <p>Validation of {@code FeedKeyInput}: {@code name} 1–200 characters; {@code prefixes} at least one
 * {@code /v1/…} path without dot or empty segments that starts some {@code read} GET operation of
 * the spec; {@code restrictions} as a read token's, with at least one of {@code sources},
 * {@code kinds}, {@code area} or {@code bbox} and no {@code endpoints} (the prefixes are the
 * endpoint restriction); {@code expiresAt} in the future and at most 365 days ahead;
 * {@code privateAccess: true} is refused until prompt 039.
 */
@Service
public class FeedKeyService implements PathKeyResolver {

    /** Every feed key starts with this. */
    public static final String KEY_PREFIX = "fsf_";

    /** The longest a feed key may live. */
    public static final Duration MAX_LIFETIME = Duration.ofDays(365);

    static final Set<String> INPUT_MEMBERS = Set.of("name", "prefixes", "restrictions", "expiresAt", "privateAccess");

    static final int ENTROPY_BYTES = 32;

    /**
     * A created key with its value, shown once.
     *
     * @param entity the stored row
     * @param key the plaintext key
     */
    public record CreatedKey(ApiTokenEntity entity, String key) {}

    private final ApiTokenRepository tokens;
    private final TokenService tokenService;
    private final TokenHasher hasher;
    private final PrincipalService principals;
    private final RestrictionValidator restrictions;
    private final SpecOperationIndex spec;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    /** Creates the service. */
    public FeedKeyService(ApiTokenRepository tokens, TokenService tokenService, TokenHasher hasher,
            PrincipalService principals, RestrictionValidator restrictions, SpecOperationIndex spec, Clock clock) {
        this.tokens = tokens;
        this.tokenService = tokenService;
        this.hasher = hasher;
        this.principals = principals;
        this.restrictions = restrictions;
        this.spec = spec;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- admin API

    /**
     * Validates a {@code FeedKeyInput} and creates the key.
     *
     * @throws ValidationException listing every problem
     */
    @Transactional
    public CreatedKey create(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a FeedKeyInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!INPUT_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a FeedKeyInput member"));
            }
        }
        String name = name(body.get("name"), errors);
        List<String> prefixes = prefixes(body.get("prefixes"), errors);
        JsonNode restrictionNode = body.get("restrictions");
        TokenRestrictions parsed = TokenRestrictions.NONE;
        if (restrictionNode == null || restrictionNode.isNull()) {
            errors.add(new FieldError("restrictions", "is required"));
        } else {
            parsed = restrictions.parse(restrictionNode, Scope.READ, "restrictions", errors);
            if (parsed.sources() == null && parsed.kinds() == null && parsed.area() == null && parsed.bbox() == null) {
                errors.add(new FieldError("restrictions", "must set at least one of sources, kinds or area"));
            }
            if (parsed.endpoints() != null) {
                errors.add(new FieldError("restrictions.endpoints",
                        "is not used on feed keys; the prefixes are the endpoint restriction"));
            }
            if (Boolean.FALSE.equals(parsed.excludePrivate())) {
                errors.add(new FieldError("restrictions.excludePrivate",
                        "feed keys never read private sources until prompt 039 adds privateAccess"));
            }
        }
        Instant expiresAt = expiresAt(body.get("expiresAt"), errors);
        JsonNode privateAccess = body.get("privateAccess");
        if (privateAccess != null && !privateAccess.isNull()) {
            if (!privateAccess.isBoolean()) {
                errors.add(new FieldError("privateAccess", "must be a boolean"));
            } else if (privateAccess.asBoolean()) {
                errors.add(new FieldError("privateAccess", "is not supported until prompt 039"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The feed key input is not valid.", errors);
        }
        PrincipalEntity principal = principals.createFor(name, PrincipalKind.SERVICE);
        String key = generate();
        Instant now = clock.instant();
        ApiTokenEntity entity = new ApiTokenEntity(UUID.randomUUID(), name, Scope.READ, hasher.hash(key),
                key.substring(0, ApiTokenEntity.PREFIX_LENGTH), TokenCreatedVia.API, principal.getId(), now);
        entity.setType(ApiTokenEntity.TYPE_FEED_KEY);
        entity.setPrefixes(prefixes);
        entity.setExpiresAt(expiresAt);
        TokenRestrictions stored = new TokenRestrictions(parsed.sources(), parsed.kinds(), parsed.bbox(), null,
                parsed.area(), Boolean.TRUE, parsed.maxRequestsPerMinute());
        entity.setRestrictions(stored);
        tokens.saveAndFlush(entity);
        return new CreatedKey(entity, key);
    }

    /** Every feed key, newest first. */
    @Transactional(readOnly = true)
    public List<ApiTokenEntity> list() {
        return tokens.findAllByOrderByCreatedAtDesc().stream().filter(ApiTokenEntity::isFeedKey).toList();
    }

    /**
     * Revokes feed key {@code id}; it stops working immediately ({@code 410}).
     *
     * @throws NotFoundException when there is no such feed key
     */
    @Transactional
    public void revoke(UUID id) {
        ApiTokenEntity entity = tokens.findById(id).filter(ApiTokenEntity::isFeedKey)
                .orElseThrow(() -> NotFoundException.of("Feed key", id));
        if (entity.getRevokedAt() == null) {
            entity.setRevokedAt(clock.instant());
            tokens.saveAndFlush(entity);
        }
    }

    /**
     * The ready-made URL of each prefix: {@code <base>/v1/k/<key>/<prefix without /v1/>}.
     *
     * @param base the public base URL (no trailing slash)
     */
    public static List<String> urls(String base, String key, List<String> prefixes) {
        List<String> out = new ArrayList<>();
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        for (String prefix : prefixes) {
            out.add(root + PathKeyFilter.PREFIX + key + prefix.substring("/v1".length()));
        }
        return out;
    }

    // ------------------------------------------------------------------------- resolution

    @Override
    public String prefix() {
        return KEY_PREFIX;
    }

    @Override
    @Transactional(readOnly = true)
    public Resolution resolve(String key) {
        if (key == null || !key.startsWith(KEY_PREFIX)) {
            return new Invalid();
        }
        Optional<ApiTokenEntity> found = tokens.findByTokenHash(hasher.hash(key));
        if (found.isEmpty() || !found.get().isFeedKey() || !hasher.matches(key, found.get().getTokenHash())) {
            return new Invalid();
        }
        ApiTokenEntity entity = found.get();
        if (!entity.isActive(clock.instant())) {
            return new Gone();
        }
        tokenService.recordUse(entity.getId());
        ReadRestriction restriction = ReadRestriction.of(entity.getRestrictions(), true).withExcludePrivate(true)
                .withEndpoints(null);
        RequestPrincipal principal = new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY, entity.getId(),
                entity.getName(), Scope.READ, restriction);
        List<String> prefixes = entity.getPrefixes() == null ? List.of() : entity.getPrefixes();
        return new Valid(principal, prefixes);
    }

    // ------------------------------------------------------------------------- helpers

    private String generate() {
        byte[] entropy = new byte[ENTROPY_BYTES];
        random.nextBytes(entropy);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
    }

    private static String name(JsonNode node, List<FieldError> errors) {
        if (node == null || !node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError("name", "is required"));
            return null;
        }
        String name = node.asString().strip();
        if (name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }
        return name;
    }

    private List<String> prefixes(JsonNode node, List<FieldError> errors) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            errors.add(new FieldError("prefixes", "must be a non-empty array of /v1/ paths"));
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (!item.isString()) {
                errors.add(new FieldError("prefixes", "must contain only strings"));
                continue;
            }
            String prefix = item.asString().strip();
            if (!prefix.startsWith("/v1/") || prefix.startsWith(PathKeyFilter.PREFIX) || prefix.contains("?")
                    || !safePrefix(prefix)) {
                errors.add(new FieldError("prefixes", "'" + prefix + "' must be a plain /v1/ path"));
                continue;
            }
            if (!readable(prefix)) {
                errors.add(new FieldError("prefixes", "'" + prefix + "' starts no read operation"));
                continue;
            }
            out.add(prefix);
        }
        return new ArrayList<>(out);
    }

    /** True when {@code prefix} has no dot, empty or encoded segments (a trailing slash is allowed). */
    static boolean safePrefix(String prefix) {
        String body = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        if (body.contains("%") || body.contains("\\") || body.contains(";")) {
            return false;
        }
        for (String segment : body.substring(1).split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        return true;
    }

    /**
     * True when some {@code read} GET operation lies under {@code prefix}: its template or a sample
     * path equals the prefix or continues it on a segment boundary.
     */
    boolean readable(String prefix) {
        for (SpecOperationIndex.SpecOperation op : spec.operations()) {
            if (!HttpMethod.GET.equals(op.method()) || !"read".equals(op.scope())) {
                continue;
            }
            for (String path : List.of(op.path(), op.samplePath())) {
                if (prefix.endsWith("/") ? path.startsWith(prefix)
                        : path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix + ".")) {
                    return true;
                }
            }
            // A prefix reaching into a templated segment, such as /v1/tiles/quakes for /v1/tiles/{layer}.json.
            int variable = op.path().indexOf('{');
            if (variable > 0 && prefix.startsWith(op.path().substring(0, variable))) {
                return true;
            }
        }
        return false;
    }

    private Instant expiresAt(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull() || !node.isString()) {
            errors.add(new FieldError("expiresAt", "is required (date-time, at most 365 days ahead)"));
            return null;
        }
        Instant at;
        try {
            at = Instant.parse(node.asString());
        } catch (DateTimeParseException e) {
            try {
                at = java.time.OffsetDateTime.parse(node.asString()).toInstant();
            } catch (DateTimeParseException e2) {
                errors.add(new FieldError("expiresAt", "must be an RFC 3339 date-time"));
                return null;
            }
        }
        Instant now = clock.instant();
        if (!at.isAfter(now)) {
            errors.add(new FieldError("expiresAt", "must be in the future"));
        } else if (at.isAfter(now.plus(MAX_LIFETIME))) {
            errors.add(new FieldError("expiresAt", "must be at most 365 days ahead"));
        }
        return at;
    }
}
