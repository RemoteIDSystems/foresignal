package ai.foresignal.api.dto;

import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.DeviceInfo;
import ai.foresignal.security.TokenRestrictions;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

/**
 * OpenAPI {@code ApiToken}, and {@code ApiTokenCreated} when {@code token} is set (prompt 023). The
 * token value appears only in the response that creates it.
 *
 * @param id the token id
 * @param name its name
 * @param scope {@code read}, {@code agent} or {@code admin}
 * @param tokenPrefix the first eight characters of the value
 * @param createdAt when it was created
 * @param lastUsedAt when it was last used (at most once a minute), or null
 * @param expiresAt when it expires, or null
 * @param revokedAt when it was revoked, or null
 * @param bootstrap true for the bootstrap admin token
 * @param restrictions its restrictions, or null when none
 * @param device its reported device, or null
 * @param createdVia {@code api}, {@code pairing} or {@code bootstrap}
 * @param principalId the owning principal
 * @param principalHandle the owning principal's handle
 * @param privateAccess the 039 private grant: the token may see private sources (admin tokens always may)
 * @param licencePolicy the token's default licence policy flags (prompt 109), or null when none
 * @param token the value, only on creation
 */
public record ApiTokenDto(
        UUID id,
        String name,
        String scope,
        String tokenPrefix,
        Instant createdAt,
        Instant lastUsedAt,
        Instant expiresAt,
        Instant revokedAt,
        boolean bootstrap,
        TokenRestrictions restrictions,
        DeviceInfo device,
        String createdVia,
        String principalId,
        String principalHandle,
        boolean privateAccess,
        @JsonInclude(JsonInclude.Include.NON_NULL) java.util.List<String> licencePolicy,
        @JsonInclude(JsonInclude.Include.NON_NULL) String token) {

    /** The listing form of {@code entity}. */
    public static ApiTokenDto of(ApiTokenEntity entity, String principalHandle) {
        return of(entity, principalHandle, null);
    }

    /** {@code entity} with its value {@code token} (creation only; null for every other response). */
    public static ApiTokenDto of(ApiTokenEntity entity, String principalHandle, String token) {
        return new ApiTokenDto(entity.getId(), entity.getName(), entity.getScope().wireValue(), entity.getTokenPrefix(),
                entity.getCreatedAt(), entity.getLastUsedAt(), entity.getExpiresAt(), entity.getRevokedAt(),
                entity.isBootstrap(), entity.getRestrictions().orNull(), entity.getDevice(), entity.getCreatedVia(),
                entity.getPrincipalId(), principalHandle, entity.isPrivateAccess(),
                entity.getLicencePolicy().isEmpty() ? null : entity.getLicencePolicy(), token);
    }
}
