package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.IngestKeys;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * EDP registration tokens (prompt 024): created by the operator, used by a provider to
 * self-register. {@code fsx_} + base64url of 32 random bytes, shown once, stored as its SHA-256;
 * {@code maxUses} defaults to 1, {@code expiresAt} is optional, {@code sourceIdPrefix} limits the
 * provider ids it may register.
 */
@Service
public class RegistrationTokenService {

    private static final Pattern PREFIX = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*-?$");

    private final RegistrationTokenRepository tokens;
    private final IngestKeys keys;
    private final Clock clock;

    /** Creates the service. */
    public RegistrationTokenService(RegistrationTokenRepository tokens, IngestKeys keys, Clock clock) {
        this.tokens = tokens;
        this.keys = keys;
        this.clock = clock;
    }

    /**
     * A created token.
     *
     * @param entity the stored row
     * @param token the plaintext, shown once
     */
    public record Created(RegistrationTokenEntity entity, String token) {}

    /**
     * Creates a token from a {@code RegistrationTokenInput} body.
     *
     * @throws ValidationException listing every problem
     */
    @Transactional
    public Created create(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new InvalidInputException("The body must be a JSON object.");
        }
        List<FieldError> errors = new ArrayList<>();
        String name = body.path("name").isString() ? body.get("name").asString().strip() : null;
        if (name == null || name.isEmpty() || name.length() > 200) {
            errors.add(new FieldError("name", "is required (at most 200 characters)"));
        }
        Instant expiresAt = null;
        JsonNode expires = body.get("expiresAt");
        if (expires != null && !expires.isNull()) {
            try {
                expiresAt = OffsetDateTime.parse(expires.asString()).toInstant();
                if (!expiresAt.isAfter(clock.instant())) {
                    errors.add(new FieldError("expiresAt", "must be in the future"));
                }
            } catch (DateTimeParseException e) {
                errors.add(new FieldError("expiresAt", "must be an RFC 3339 date-time"));
            }
        }
        int maxUses = 1;
        JsonNode uses = body.get("maxUses");
        if (uses != null && !uses.isNull()) {
            if (!uses.isIntegralNumber() || uses.asLong() < 1 || uses.asLong() > 10_000) {
                errors.add(new FieldError("maxUses", "must be an integer from 1 to 10000"));
            } else {
                maxUses = uses.asInt();
            }
        }
        String prefix = null;
        JsonNode p = body.get("sourceIdPrefix");
        if (p != null && !p.isNull()) {
            prefix = p.asString();
            if (prefix.isEmpty() || prefix.length() > 48 || !PREFIX.matcher(prefix).matches()) {
                errors.add(new FieldError("sourceIdPrefix", "must be the start of a provider id (lowercase, digits, hyphens)"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The registration token is not valid.", errors);
        }
        String token = keys.generate(EdpProtocol.REGISTRATION_TOKEN_PREFIX);
        RegistrationTokenEntity entity = new RegistrationTokenEntity(Ulids.next(clock.instant()), name,
                keys.hash(token), IngestKeys.prefixOf(token), expiresAt, maxUses, prefix, clock.instant());
        tokens.save(entity);
        return new Created(entity, token);
    }

    /** Unexpired tokens, newest first. */
    @Transactional(readOnly = true)
    public List<RegistrationTokenEntity> list() {
        Instant now = clock.instant();
        return tokens.findAllByOrderByCreatedAtDesc().stream()
                .filter(t -> t.getExpiresAt() == null || t.getExpiresAt().isAfter(now))
                .toList();
    }

    /**
     * Revokes a token.
     *
     * @throws NotFoundException when it does not exist
     */
    @Transactional
    public void delete(String id) {
        RegistrationTokenEntity entity = tokens.findById(id).orElseThrow(() -> NotFoundException.of("Registration token", id));
        tokens.delete(entity);
    }

    /**
     * Takes one use of the presented token, in the caller's transaction.
     *
     * @throws ForesignalException {@code 401 REGISTRATION_TOKEN_INVALID}
     */
    @Transactional
    public RegistrationTokenEntity use(String presented) {
        if (presented == null || !presented.startsWith(EdpProtocol.REGISTRATION_TOKEN_PREFIX)) {
            throw invalid();
        }
        RegistrationTokenEntity entity = tokens.findByTokenHash(keys.hash(presented)).orElseThrow(
                RegistrationTokenService::invalid);
        if (!keys.matches(presented, entity.getTokenHash()) || !entity.usable(clock.instant())) {
            throw invalid();
        }
        entity.use();
        tokens.save(entity);
        return entity;
    }

    private static ForesignalException invalid() {
        return new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.REGISTRATION_TOKEN_INVALID,
                "The registration token is unknown, expired or used up.");
    }
}
