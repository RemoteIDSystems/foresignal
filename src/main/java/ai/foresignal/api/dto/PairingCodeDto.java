package ai.foresignal.api.dto;

import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.pairing.PairingCodeEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

/**
 * OpenAPI {@code PairingCode}, and {@code PairingCodeCreated} when {@code code} and
 * {@code qrPayload} are set (prompt 023). The code itself appears only in the creating response.
 *
 * @param id the id
 * @param name the name the claimed token gets
 * @param scope {@code read} or {@code agent}
 * @param restrictions the claimed token's restrictions, or null
 * @param principalId the principal that will own the token, or null
 * @param privateAccess the 039 private grant passed to the claimed token
 * @param createdAt when it was created
 * @param expiresAt when it stops being claimable
 * @param createdByTokenId the admin token that created it, or null
 * @param claimedAt when it was claimed, or null
 * @param claimedTokenId the token the claim created, or null
 * @param code the code, only on creation
 * @param qrPayload the QR payload, only on creation
 */
public record PairingCodeDto(
        UUID id,
        String name,
        String scope,
        TokenRestrictions restrictions,
        String principalId,
        boolean privateAccess,
        Instant createdAt,
        Instant expiresAt,
        UUID createdByTokenId,
        Instant claimedAt,
        UUID claimedTokenId,
        @JsonInclude(JsonInclude.Include.NON_NULL) String code,
        @JsonInclude(JsonInclude.Include.NON_NULL) String qrPayload) {

    /** The listing form of {@code entity}. */
    public static PairingCodeDto of(PairingCodeEntity entity) {
        return of(entity, null, null);
    }

    /** {@code entity} with its code and QR payload (creation only). */
    public static PairingCodeDto of(PairingCodeEntity entity, String code, String qrPayload) {
        return new PairingCodeDto(entity.getId(), entity.getName(), entity.getScope().wireValue(),
                entity.getRestrictions().orNull(), entity.getPrincipalId(), entity.isPrivateAccess(), entity.getCreatedAt(),
                entity.getExpiresAt(), entity.getCreatedByTokenId(), entity.getClaimedAt(), entity.getClaimedTokenId(),
                code, qrPayload);
    }
}
