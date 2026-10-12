package ai.foresignal.api.dto;

import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.TokenRestrictions;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * OpenAPI {@code FeedKey}, and {@code FeedKeyCreated} when {@code key} is set (prompt 035). The key
 * value and its URLs appear only in the response that creates it.
 *
 * @param id the key id
 * @param name its name
 * @param keyPrefix the first eight characters of the key
 * @param prefixes the canonical path prefixes it may read
 * @param restrictions its read restriction
 * @param privateAccess always false until prompt 039
 * @param createdAt when it was created
 * @param expiresAt when it expires
 * @param lastUsedAt when it was last used (at most once a minute), or null
 * @param revokedAt when it was revoked, or null
 * @param key the value, only on creation
 * @param urls one ready-made URL per prefix, only on creation
 */
public record FeedKeyDto(
        UUID id,
        String name,
        String keyPrefix,
        List<String> prefixes,
        TokenRestrictions restrictions,
        boolean privateAccess,
        Instant createdAt,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant revokedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) String key,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<Url> urls) {

    /**
     * A ready-made URL for one prefix.
     *
     * @param prefix the prefix
     * @param url the URL
     */
    public record Url(String prefix, String url) {}

    /** The listing form of {@code entity}. */
    public static FeedKeyDto of(ApiTokenEntity entity) {
        return of(entity, null, null);
    }

    /** {@code entity} with its value and URLs (creation only). */
    public static FeedKeyDto of(ApiTokenEntity entity, String key, List<String> urls) {
        List<String> prefixes = entity.getPrefixes() == null ? List.of() : entity.getPrefixes();
        List<Url> pairs = null;
        if (urls != null) {
            pairs = new ArrayList<>();
            for (int i = 0; i < prefixes.size() && i < urls.size(); i++) {
                pairs.add(new Url(prefixes.get(i), urls.get(i)));
            }
        }
        return new FeedKeyDto(entity.getId(), entity.getName(), entity.getTokenPrefix(), prefixes,
                entity.getRestrictions(), false, entity.getCreatedAt(), entity.getExpiresAt(), entity.getLastUsedAt(),
                entity.getRevokedAt(), key, pairs);
    }
}
