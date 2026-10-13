package ai.foresignal.access;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.RestrictionValidator;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.security.restrict.PathKeyResolver;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.settings.Durations;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Tile keys (prompt 050; Weather-Imagery-Design §6.7): a thin facade over prompt 035's feed-key model
 * filtered to {@code type = tile_key}, and the {@link PathKeyResolver} for {@value
 * TileKeyPolicy#KEY_PREFIX}.
 *
 * <p>Storage, hashing, listing, revocation and the constant-time lookup are {@link FeedKeyService}'s;
 * this class adds only the tile-key rules:
 *
 * <ul>
 *   <li>{@code name} 1–200 characters;
 *   <li>{@code prefixes} a non-empty set drawn from {@link TileKeyPolicy#ALLOWED_PREFIXES};
 *   <li>{@code ttl} an ISO-8601 or shorthand duration, positive and at most 30 days;
 *   <li>optional {@code restrictions} (sources, kinds, bbox or area, {@code maxRequestsPerMinute}) as a
 *       read token's, without {@code endpoints} (the prefixes play that role), {@code placeIds} (a URL
 *       credential is never a place reader) or {@code excludePrivate: false};
 *   <li>scope {@code read}, never the private grant: a tile key is treated like the anonymous profile
 *       by the private-source gate (039) and never sees private sources.
 * </ul>
 *
 * <p>At {@code /v1/k/{key}/…}, an unknown, expired or revoked tile key is {@code 401
 * INVALID_TILE_KEY}, and a canonical path outside the key's prefixes is {@code 403
 * INSUFFICIENT_SCOPE}. Lookups are not cached, so a revocation takes effect with the next request
 * (well within the 5 s the spec allows), and the key's open streams are reset.
 */
@Service
public class TileKeyService implements PathKeyResolver {

    static final Set<String> INPUT_MEMBERS = Set.of("name", "prefixes", "ttl", "restrictions");

    private static final String CURSOR_TAG = "tk:";

    private final FeedKeyService feedKeys;
    private final RestrictionValidator restrictions;
    private final Clock clock;

    /** Creates the service. */
    public TileKeyService(FeedKeyService feedKeys, RestrictionValidator restrictions, Clock clock) {
        this.feedKeys = feedKeys;
        this.restrictions = restrictions;
        this.clock = clock;
    }

    /**
     * A page of tile keys.
     *
     * @param items the keys, newest first
     * @param nextCursor the cursor of the next page, or null at the end
     */
    public record Page(List<ApiTokenEntity> items, String nextCursor) {}

    // ------------------------------------------------------------------------- API

    /**
     * Validates a {@code TileKeyCreate} and mints the key.
     *
     * @return the stored row and its plaintext value, shown once
     * @throws ValidationException {@code VALIDATION_FAILED}, listing every problem
     */
    public FeedKeyService.CreatedKey create(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a TileKeyCreate object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!INPUT_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a TileKeyCreate member"));
            }
        }
        String name = name(body.get("name"), errors);
        List<String> prefixes = prefixes(body.get("prefixes"), errors);
        Duration ttl = ttl(body.get("ttl"), errors);
        TokenRestrictions parsed = restrictions(body.get("restrictions"), errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The tile key input is not valid.", errors);
        }
        TokenRestrictions stored = new TokenRestrictions(parsed.sources(), parsed.kinds(), parsed.bbox(), null,
                parsed.area(), Boolean.TRUE, parsed.maxRequestsPerMinute());
        Instant expiresAt = clock.instant().plus(ttl);
        return feedKeys.mint(ApiTokenEntity.TYPE_TILE_KEY, TileKeyPolicy.KEY_PREFIX, name, prefixes, stored,
                expiresAt, false);
    }

    /** Every tile key, newest first (revoked and expired ones included, with their dates). */
    public List<ApiTokenEntity> list() {
        return feedKeys.listOfType(ApiTokenEntity.TYPE_TILE_KEY);
    }

    /**
     * A page of {@link #list()}.
     *
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size
     * @throws ValidationException {@code INVALID_CURSOR} for a cursor this listing did not issue
     */
    public Page page(String cursor, int limit) {
        List<ApiTokenEntity> all = list();
        int start = 0;
        if (cursor != null && !cursor.isBlank()) {
            UUID after = decodeCursor(cursor);
            start = -1;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId().equals(after)) {
                    start = i + 1;
                    break;
                }
            }
            if (start < 0) {
                throw invalidCursor();
            }
        }
        int end = Math.min(all.size(), start + limit);
        List<ApiTokenEntity> items = all.subList(start, end);
        String next = end < all.size() && !items.isEmpty() ? encodeCursor(items.getLast().getId()) : null;
        return new Page(List.copyOf(items), next);
    }

    /**
     * Revokes tile key {@code id}; it answers {@code 401 INVALID_TILE_KEY} from the next request on.
     *
     * @throws ai.foresignal.api.error.NotFoundException when there is no such tile key
     */
    public void revoke(UUID id) {
        feedKeys.revokeOfType(ApiTokenEntity.TYPE_TILE_KEY, id, "Tile key");
    }

    /** {@code {base}/v1/k/{key}}: append the canonical path after {@code /v1/}. */
    public static String urlBase(String base, String key) {
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return root + ai.foresignal.security.restrict.PathKeyFilter.PREFIX + key;
    }

    // ------------------------------------------------------------------------- resolution

    @Override
    public String prefix() {
        return TileKeyPolicy.KEY_PREFIX;
    }

    @Override
    public Resolution resolve(String key) {
        return feedKeys.resolveOfType(key, TileKeyPolicy.KEY_PREFIX, ApiTokenEntity.TYPE_TILE_KEY,
                TileKeyService::principalOf);
    }

    /**
     * The principal of an active tile-key row: kind {@code TILE_KEY}, scope {@code read}, its stored
     * restriction, private sources always excluded, sensitive alerts hidden, no private grant.
     */
    static RequestPrincipal principalOf(ApiTokenEntity entity) {
        ReadRestriction restriction = ReadRestriction.of(entity.getRestrictions(), true)
                .withExcludePrivate(true)
                .withEndpoints(null)
                .withExcludeSensitive(true);
        return new RequestPrincipal(RequestPrincipal.Kind.TILE_KEY, entity.getId(), entity.getName(), Scope.READ,
                restriction, false);
    }

    @Override
    public String invalidCode() {
        return ErrorCodes.INVALID_TILE_KEY;
    }

    @Override
    public HttpStatus goneStatus() {
        // Unknown, expired and revoked tile keys look alike to a map client (prompt 050).
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String goneCode() {
        return ErrorCodes.INVALID_TILE_KEY;
    }

    @Override
    public String outsidePrefixCode() {
        return ErrorCodes.INSUFFICIENT_SCOPE;
    }

    // ------------------------------------------------------------------------- validation

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

    private static List<String> prefixes(JsonNode node, List<FieldError> errors) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            errors.add(new FieldError("prefixes", "must be a non-empty array of allowed prefixes"));
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (!item.isString()) {
                errors.add(new FieldError("prefixes", "must contain only strings"));
                continue;
            }
            String prefix = item.asString().strip();
            if (!TileKeyPolicy.allowed(prefix)) {
                errors.add(new FieldError("prefixes", "'" + prefix + "' is not one of "
                        + String.join(", ", TileKeyPolicy.ALLOWED_PREFIXES)));
                continue;
            }
            out.add(prefix);
        }
        return new ArrayList<>(out);
    }

    static Duration ttl(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull() || !node.isString()) {
            errors.add(new FieldError("ttl", "is required (a duration of at most 30 days, e.g. 7d or P7D)"));
            return null;
        }
        Duration ttl;
        try {
            ttl = Durations.parsePositive(node.asString());
        } catch (InvalidInputException e) {
            errors.add(new FieldError("ttl", "must be a positive duration such as 7d, 12h or P7D"));
            return null;
        }
        if (ttl.compareTo(TileKeyPolicy.MAX_TTL) > 0) {
            errors.add(new FieldError("ttl", "must be at most 30 days"));
        }
        return ttl;
    }

    private TokenRestrictions restrictions(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return TokenRestrictions.NONE;
        }
        TokenRestrictions parsed = restrictions.parse(node, Scope.READ, "restrictions", errors);
        if (parsed == null) {
            return TokenRestrictions.NONE;
        }
        if (parsed.endpoints() != null) {
            errors.add(new FieldError("restrictions.endpoints",
                    "is not used on tile keys; the prefixes are the endpoint restriction"));
        }
        if (parsed.placeIds() != null) {
            errors.add(new FieldError("restrictions.placeIds", "tile keys cannot be restricted by place"));
        }
        if (Boolean.FALSE.equals(parsed.excludePrivate())) {
            errors.add(new FieldError("restrictions.excludePrivate", "a tile key never reads private sources"));
        }
        return parsed;
    }

    // ------------------------------------------------------------------------- cursor

    static String encodeCursor(UUID id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((CURSOR_TAG + id).getBytes(StandardCharsets.UTF_8));
    }

    static UUID decodeCursor(String cursor) {
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith(CURSOR_TAG)) {
                throw invalidCursor();
            }
            return UUID.fromString(text.substring(CURSOR_TAG.length()));
        } catch (IllegalArgumentException e) {
            throw invalidCursor();
        }
    }

    private static ValidationException invalidCursor() {
        return new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                List.of(new FieldError("cursor", "is not a cursor of this listing")));
    }
}
