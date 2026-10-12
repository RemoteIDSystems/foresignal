package ai.foresignal.security.pairing;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.DeviceInfo;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.RestrictionValidator;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.ServerSettingsService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Device pairing (prompt 023): an admin creates a short-lived, single-use code (and its QR); the
 * phone claims it with its device metadata for a {@code read} or {@code agent} token, never seeing
 * an admin token. {@code admin} is never pairable.
 *
 * <ul>
 *   <li>The code is 8 Crockford base32 characters ({@code XXXX-XXXX}); only its SHA-256 is stored.
 *       Its QR payload is kept in memory until the code expires or is claimed, so
 *       {@code qr.svg} is unavailable after a restart (create a new code).
 *   <li>Claims are rate limited ({@link PairingRateLimiter}), compared in constant time, single use
 *       (a conditional update), and every claim, successful or not, is audited.
 * </ul>
 */
@Service
public class PairingService {

    /** A code's lifetime when the request names none. */
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    /** The longest lifetime. */
    public static final Duration MAX_TTL = Duration.ofHours(1);

    static final Set<String> INPUT_MEMBERS = Set.of("name", "scope", "restrictions", "ttl", "principalId",
            "privateAccess");

    static final String CLAIM_PATH = "/v1/pairing/claim";

    /**
     * A created code, returned once.
     *
     * @param entity the stored code
     * @param code the code, {@code XXXX-XXXX}
     * @param qrPayload the {@code clientLinks.pair} template filled in
     */
    public record CreatedCode(PairingCodeEntity entity, String code, String qrPayload) {}

    private record Payload(String qrPayload, Instant expiresAt) {}

    private final PairingCodeRepository codes;
    private final TokenService tokens;
    private final PrincipalService principals;
    private final RestrictionValidator restrictions;
    private final ServerSettingsService server;
    private final PairingRateLimiter limiter;
    private final AuditService audit;
    private final QrSvgRenderer qr;
    private final TokenHasher hasher;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final Map<UUID, Payload> payloads = new ConcurrentHashMap<>();

    /** Creates the service. */
    public PairingService(PairingCodeRepository codes, TokenService tokens, PrincipalService principals,
            RestrictionValidator restrictions, ServerSettingsService server, PairingRateLimiter limiter,
            AuditService audit, QrSvgRenderer qr, TokenHasher hasher, PlatformTransactionManager transactions,
            Clock clock) {
        this.codes = codes;
        this.tokens = tokens;
        this.principals = principals;
        this.restrictions = restrictions;
        this.server = server;
        this.limiter = limiter;
        this.audit = audit;
        this.qr = qr;
        this.hasher = hasher;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- admin side

    /**
     * Creates a code from a {@code PairingCodeInput} body.
     *
     * @param body the request body
     * @param creator the admin token creating it, or null
     * @param requestBaseUrl the request's own scheme, host and port, used when {@code publicBaseUrl}
     *     is not set
     * @throws ValidationException listing every problem
     */
    public CreatedCode create(JsonNode body, AuthenticatedToken creator, String requestBaseUrl) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PairingCodeInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!INPUT_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a PairingCodeInput member"));
            }
        }
        JsonNode nameNode = body.get("name");
        String name = null;
        if (nameNode == null || !nameNode.isString() || nameNode.asString().isBlank()) {
            errors.add(new FieldError("name", "is required"));
        } else if (nameNode.asString().strip().length() > TokenService.MAX_NAME) {
            errors.add(new FieldError("name", "must be at most " + TokenService.MAX_NAME + " characters"));
        } else {
            name = nameNode.asString().strip();
        }
        Scope scope = pairableScope(body.get("scope"), errors);
        TokenRestrictions parsed = restrictions.parse(body.get("restrictions"), scope == null ? Scope.READ : scope,
                "restrictions", errors);
        Duration ttl = ttl(body.get("ttl"), errors);
        String principalId = null;
        JsonNode principal = body.get("principalId");
        if (principal != null && !principal.isNull()) {
            if (!principal.isString() || principal.asString().isBlank()) {
                errors.add(new FieldError("principalId", "must be a principal id or handle"));
            } else {
                try {
                    principalId = principals.resolve(principal.asString()).getId();
                } catch (ValidationException e) {
                    errors.addAll(e.getErrors());
                }
            }
        }
        JsonNode privateAccess = body.get("privateAccess");
        if (privateAccess != null && !privateAccess.isNull() && !privateAccess.isBoolean()) {
            errors.add(new FieldError("privateAccess", "must be a boolean"));
        }
        boolean grant = privateAccess != null && privateAccess.isBoolean() && privateAccess.asBoolean();
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The pairing code input is not valid.", errors);
        }
        Instant now = clock.instant();
        String code = PairingCodes.generate();
        PairingCodeEntity entity = new PairingCodeEntity(UUID.randomUUID(), hasher.hash(PairingCodes.normalize(code)),
                name, scope, parsed, principalId, now, now.plus(ttl), creator == null ? null : creator.id());
        entity.setPrivateAccess(grant && scope != Scope.ADMIN);
        codes.saveAndFlush(entity);
        String payload = qrPayload(code, requestBaseUrl);
        prunePayloads(now);
        payloads.put(entity.getId(), new Payload(payload, entity.getExpiresAt()));
        return new CreatedCode(entity, code, payload);
    }

    /** Unclaimed, unexpired codes, newest first. */
    public List<PairingCodeEntity> listOpen() {
        return codes.findOpen(clock.instant());
    }

    /**
     * Deletes code {@code id}.
     *
     * @throws NotFoundException when there is none
     */
    public void delete(UUID id) {
        PairingCodeEntity entity = codes.findById(id).orElseThrow(() -> NotFoundException.of("Pairing code", id));
        codes.delete(entity);
        payloads.remove(id);
    }

    /**
     * The SVG QR code of code {@code id}'s payload.
     *
     * @throws NotFoundException when the code is unknown, claimed, expired, or its payload is no
     *     longer held (after a restart)
     */
    public String qrSvg(UUID id) {
        Instant now = clock.instant();
        Optional<PairingCodeEntity> entity = codes.findById(id);
        Payload payload = payloads.get(id);
        if (entity.isEmpty() || !entity.get().isOpen(now) || payload == null) {
            throw new NotFoundException("Pairing code '" + id + "' is unknown, claimed or expired.");
        }
        return qr.render(payload.qrPayload());
    }

    /** The {@code clientLinks.pair} template filled with the base URL and {@code code}. */
    String qrPayload(String code, String requestBaseUrl) {
        var settings = server.get();
        String base = settings.normalizedPublicBaseUrl();
        if (base == null) {
            base = requestBaseUrl == null ? "" : requestBaseUrl;
        }
        return ClientLinks.fill(settings.effectiveClientLinks().pair(), Map.of(
                "publicBaseUrl", URLEncoder.encode(base, StandardCharsets.UTF_8),
                "code", code));
    }

    // ------------------------------------------------------------------------- device side

    /**
     * Claims a code from a {@code PairingClaim} body for a new token ({@code created_via=pairing}).
     *
     * @param body the request body
     * @param remoteIp the client address
     * @return the created token, whose plaintext is shown once
     * @throws ForesignalException {@code 429 RATE_LIMITED} past the limits
     * @throws ValidationException {@code 400 PAIRING_CODE_INVALID} or {@code VALIDATION_FAILED}
     */
    public TokenService.CreatedToken claim(JsonNode body, String remoteIp) {
        PairingRateLimiter.Decision decision = limiter.attempt(remoteIp);
        if (!decision.allowed()) {
            audit(remoteIp, 429, null, null, Map.of("outcome", "rate_limited"));
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "Too many pairing attempts; wait and try again.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(decision.retryAfterSeconds()));
        }
        if (body == null || !body.isObject()) {
            audit(remoteIp, 400, null, null, Map.of("outcome", "invalid_body"));
            throw new ValidationException("The request body must be a PairingClaim object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!Set.of("code", "device").contains(member)) {
                errors.add(new FieldError(member, "is not a PairingClaim member"));
            }
        }
        DeviceInfo device = TokenService.parseDevice(body.get("device"), "device.", errors);
        JsonNode codeNode = body.get("code");
        if (codeNode == null || !codeNode.isString()) {
            errors.add(new FieldError("code", "is required"));
        }
        if (!errors.isEmpty()) {
            audit(remoteIp, 400, null, null, Map.of("outcome", "invalid_body"));
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The pairing claim is not valid.", errors);
        }
        String normalized = PairingCodes.normalize(codeNode.asString());
        if (normalized == null) {
            throw invalidCode(remoteIp, null);
        }
        String presentedHash = hasher.hash(normalized);
        Optional<PairingCodeEntity> found = codes.findByCodeHash(presentedHash);
        if (found.isEmpty() || !MessageDigest.isEqual(presentedHash.getBytes(StandardCharsets.US_ASCII),
                found.get().getCodeHash().getBytes(StandardCharsets.US_ASCII))) {
            throw invalidCode(remoteIp, null);
        }
        PairingCodeEntity code = found.get();
        TokenService.CreatedToken created = tx.execute(status -> {
            Instant now = clock.instant();
            if (codes.claim(code.getId(), now) != 1) {
                return null;
            }
            TokenService.CreatedToken token = tokens.create(new TokenService.TokenSpec(code.getName(),
                    code.getScope(), null, code.getRestrictions(), device, TokenCreatedVia.PAIRING,
                    code.getPrincipalId(), code.isPrivateAccess()));
            PairingCodeEntity claimed = codes.findById(code.getId()).orElseThrow();
            claimed.setClaimedTokenId(token.entity().getId());
            codes.saveAndFlush(claimed);
            return token;
        });
        if (created == null) {
            throw invalidCode(remoteIp, code.getId());
        }
        payloads.remove(code.getId());
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("outcome", "claimed");
        detail.put("platform", device.platform());
        audit(remoteIp, 201, code.getId(), created.entity(), detail);
        return created;
    }

    private ValidationException invalidCode(String remoteIp, UUID codeId) {
        audit(remoteIp, 400, codeId, null, Map.of("outcome", "invalid_code"));
        return new ValidationException(ErrorCodes.PAIRING_CODE_INVALID,
                "The pairing code is unknown, expired or already claimed.", List.of());
    }

    private void audit(String remoteIp, int status, UUID codeId, ai.foresignal.security.ApiTokenEntity token,
            Map<String, Object> detail) {
        audit.record(new AuditRecord(
                clock.instant(),
                AuditRecord.PAIRING,
                token == null ? null : token.getId(),
                token == null ? null : token.getName(),
                token == null ? null : token.getPrincipalId(),
                token == null ? null : token.getScope().wireValue(),
                "POST",
                CLAIM_PATH,
                "claimPairingCode",
                status,
                "pairing-codes",
                codeId == null ? null : codeId.toString(),
                remoteIp,
                detail));
    }

    // ------------------------------------------------------------------------- helpers

    private static Scope pairableScope(JsonNode node, List<FieldError> errors) {
        if (node == null || !node.isString()) {
            errors.add(new FieldError("scope", "must be read or agent"));
            return null;
        }
        return switch (node.asString()) {
            case "read" -> Scope.READ;
            case "agent" -> Scope.AGENT;
            case "admin" -> {
                errors.add(new FieldError("scope", "admin is never pairable; use read or agent"));
                yield null;
            }
            default -> {
                errors.add(new FieldError("scope", "must be read or agent"));
                yield null;
            }
        };
    }

    private static Duration ttl(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return DEFAULT_TTL;
        }
        if (!node.isString()) {
            errors.add(new FieldError("ttl", "must be a duration such as 10m or PT10M"));
            return DEFAULT_TTL;
        }
        try {
            Duration ttl = Durations.parse(node.asString());
            if (ttl.isZero() || ttl.isNegative() || ttl.compareTo(MAX_TTL) > 0) {
                errors.add(new FieldError("ttl", "must be positive and at most 1h"));
                return DEFAULT_TTL;
            }
            return ttl;
        } catch (InvalidInputException e) {
            errors.add(new FieldError("ttl", e.getMessage()));
            return DEFAULT_TTL;
        }
    }

    private void prunePayloads(Instant now) {
        payloads.values().removeIf(p -> !now.isBefore(p.expiresAt()));
    }
}
