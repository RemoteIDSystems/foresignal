package ai.foresignal.api.dto;

import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.TokenRestrictions;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * OpenAPI {@code TileKey}, and {@code TileKeyCreated} when {@code key} is set (prompt 050). The key
 * value and its {@code urlBase} appear only in the response that creates it.
 *
 * @param id the key id
 * @param name its name
 * @param keyPrefix the first eight characters of the key
 * @param prefixes the canonical path prefixes it may read
 * @param restrictions its read restriction, or null when it has none beyond the private exclusion
 * @param createdAt when it was created
 * @param expiresAt when it expires
 * @param lastUsedAt when it was last used (at most once a minute), or null
 * @param revokedAt when it was revoked, or null
 * @param key the value, only on creation
 * @param urlBase {@code {publicBaseUrl}/v1/k/{key}}, only on creation
 */
public record TileKeyDto(
        UUID id,
        String name,
        String keyPrefix,
        List<String> prefixes,
        TokenRestrictions restrictions,
        Instant createdAt,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant revokedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) String key,
        @JsonInclude(JsonInclude.Include.NON_NULL) String urlBase) {

    /**
     * OpenAPI {@code TileKeyPage}.
     *
     * @param items the keys
     * @param nextCursor the next page's cursor, or null at the end
     */
    public record Page(List<TileKeyDto> items, String nextCursor) {}

    /** The listing form of {@code entity}. */
    public static TileKeyDto of(ApiTokenEntity entity) {
        return of(entity, null, null);
    }

    /** {@code entity} with its value and URL base (creation only). */
    public static TileKeyDto of(ApiTokenEntity entity, String key, String urlBase) {
        List<String> prefixes = entity.getPrefixes() == null ? List.of() : entity.getPrefixes();
        return new TileKeyDto(entity.getId(), entity.getName(), entity.getTokenPrefix(), prefixes,
                visible(entity.getRestrictions()), entity.getCreatedAt(), entity.getExpiresAt(),
                entity.getLastUsedAt(), entity.getRevokedAt(), key, urlBase);
    }

    /** The stored restriction without its always-on private exclusion; null when nothing else is left. */
    static TokenRestrictions visible(TokenRestrictions stored) {
        if (stored == null) {
            return null;
        }
        TokenRestrictions shown = new TokenRestrictions(stored.sources(), stored.kinds(), stored.bbox(), null,
                stored.area(), null, stored.maxRequestsPerMinute());
        return shown.orNull();
    }
}
