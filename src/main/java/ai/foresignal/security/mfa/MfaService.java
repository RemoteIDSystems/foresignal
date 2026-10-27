package ai.foresignal.security.mfa;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.pairing.QrSvgRenderer;
import ai.foresignal.security.user.AccountMailer;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleEntity;
import ai.foresignal.security.user.UserRoleRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Multi-factor authentication (prompt R2-024): TOTP enrollment, activation, step-up, recovery codes and disabling.
 *
 * <ul>
 *   <li><strong>Enroll</strong> stores a new TOTP secret, sealed with the instance key ({@link CredentialCipher}), as
 *       pending; MFA is not active until {@link #verify} confirms a code from it.
 *   <li><strong>Verify</strong> activates a pending enrollment with a current code and returns a first set of
 *       {@value #RECOVERY_CODES} recovery codes, shown once. Once active it is the step-up check: a current TOTP or an
 *       unused recovery code. A TOTP step is never accepted twice; a recovery code is consumed on use.
 *   <li><strong>Recovery codes</strong> are stored only as {@link TokenHasher} hashes of their normalized form
 *       (lower case, without spaces or dashes); regenerating replaces the whole set.
 *   <li><strong>Disable</strong> re-authenticates with a code or the password; a user holding a role that requires
 *       MFA (the built-in Admin or Operator) cannot disable it. {@link #adminReset} is the administrator's path, for
 *       the user-management API (R2-005), which does not call it yet.
 * </ul>
 */
@Service
public class MfaService {

    /** The issuer shown by authenticator apps. */
    public static final String ISSUER = "ForeSignal";

    /** Recovery codes in a set. */
    public static final int RECOVERY_CODES = 10;

    /** Characters in a recovery code, without its dash: 50 random bits in Base32. */
    public static final int RECOVERY_CODE_LENGTH = 10;

    /** The built-in roles that require MFA. */
    public static final Set<UUID> REQUIRING_ROLES = Set.of(BuiltInRole.ADMIN.id(), BuiltInRole.OPERATOR.id());

    private final UserRepository users;
    private final UserRoleRepository userRoles;
    private final RecoveryCodeRepository recoveryCodes;
    private final CredentialCipher cipher;
    private final TokenHasher hasher;
    private final PasswordHasher passwords;
    private final TotpService totp;
    private final QrSvgRenderer qr;
    private final AccountMailer mailer;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private volatile boolean enforceEnrollment;

    /**
     * Creates the service.
     *
     * @param users the user store
     * @param userRoles the user/role links, for {@link #required}
     * @param recoveryCodes the recovery-code store
     * @param cipher seals the TOTP secret
     * @param hasher hashes recovery codes
     * @param passwords verifies the password on {@link #disable}
     * @param totp RFC 6238 codes
     * @param qr renders the provisioning QR code
     * @param mailer sends the MFA-changed notice
     * @param clock the clock
     * @param enforceEnrollment {@code foresignal.mfa.enforce-enrollment}: whether a user whose role requires MFA is
     *     confined to enrollment until enrolled (true unless the test profile turns it off)
     */
    public MfaService(UserRepository users, UserRoleRepository userRoles, RecoveryCodeRepository recoveryCodes,
            CredentialCipher cipher, TokenHasher hasher, PasswordHasher passwords, TotpService totp, QrSvgRenderer qr,
            AccountMailer mailer, Clock clock,
            @Value("${foresignal.mfa.enforce-enrollment:true}") boolean enforceEnrollment) {
        this.users = users;
        this.userRoles = userRoles;
        this.recoveryCodes = recoveryCodes;
        this.cipher = cipher;
        this.hasher = hasher;
        this.passwords = passwords;
        this.totp = totp;
        this.qr = qr;
        this.mailer = mailer;
        this.clock = clock;
        this.enforceEnrollment = enforceEnrollment;
    }

    /**
     * Whether a session of this user must enroll before writing: a role requires MFA, it is not enrolled, and
     * enforcement is on. Read on every request of a session.
     *
     * @param user the user
     * @return true when the session is confined to enrollment
     */
    public boolean enrollmentPending(UserEntity user) {
        return enforceEnrollment && !user.isMfaEnabled() && required(user.getId());
    }

    /** Turns enrollment enforcement on or off; for {@code MfaEnforcementIT}, whose profile turns it off. */
    void enforceEnrollment(boolean on) {
        this.enforceEnrollment = on;
    }

    /**
     * Whether a role the user holds requires MFA.
     *
     * @param userId the user
     * @return true for a holder of the built-in Admin or Operator role
     */
    @Transactional(readOnly = true)
    public boolean required(UUID userId) {
        return userRoles.findByUserId(userId).stream().map(UserRoleEntity::getRoleId)
                .anyMatch(REQUIRING_ROLES::contains);
    }

    /**
     * The {@code MfaStatus} view.
     *
     * @param userId the user
     * @return {@code enabled}, {@code enrolledAt} and {@code required}
     */
    @Transactional(readOnly = true)
    public Map<String, Object> status(UUID userId) {
        return status(user(userId));
    }

    private Map<String, Object> status(UserEntity user) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", user.isMfaEnabled());
        out.put("enrolledAt", user.isMfaEnabled() ? user.getMfaEnrolledAt() : null);
        out.put("required", required(user.getId()));
        return out;
    }

    /**
     * {@code enrollMfa}: a new pending secret, replacing any earlier pending one.
     *
     * @param userId the user
     * @return the {@code MfaEnrollment} view: {@code secret}, {@code otpauthUri} and {@code qrSvg}
     * @throws ConflictException {@code MFA_ALREADY_ENABLED} while MFA is active
     */
    @Transactional
    public Map<String, Object> enroll(UUID userId) {
        UserEntity user = user(userId);
        if (user.isMfaEnabled()) {
            throw new ConflictException(ErrorCodes.MFA_ALREADY_ENABLED, "MFA is already enabled; disable it before"
                    + " enrolling again.");
        }
        byte[] secret = totp.newSecret();
        user.setPendingMfaSecret(cipher.encrypt(secret), clock.instant());
        users.save(user);
        String base32 = TotpService.base32(secret);
        String uri = otpauthUri(user.getEmail(), base32);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("secret", base32);
        out.put("otpauthUri", uri);
        out.put("qrSvg", qr.render(uri));
        return out;
    }

    /**
     * The provisioning URI ({@code otpauth://totp/ForeSignal:<email>?secret=…&issuer=ForeSignal}).
     *
     * @param email the account email
     * @param base32 the secret in Base32
     * @return the URI
     */
    static String otpauthUri(String email, String base32) {
        return "otpauth://totp/" + ISSUER + ":" + encode(email) + "?secret=" + base32 + "&issuer=" + encode(ISSUER);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * The outcome of {@link #verify}.
     *
     * @param status the {@code MfaStatus} view
     * @param recoveryCodes the first recovery codes when this call activated MFA, else null
     * @param generatedAt when they were generated, else null
     * @param activated true when this call activated MFA
     */
    public record Verified(Map<String, Object> status, List<String> recoveryCodes, Instant generatedAt,
            boolean activated) {

        @Override
        public String toString() {
            return "Verified[activated=" + activated + "]";
        }
    }

    /**
     * {@code verifyMfa}: activates a pending enrollment with a current TOTP code, or, once active, checks a TOTP or
     * recovery code (step-up).
     *
     * @param userId the user
     * @param code the code presented
     * @return the status, with the first recovery codes when this call activated MFA
     * @throws ConflictException {@code MFA_NOT_ENABLED} with neither an active nor a pending secret
     * @throws ValidationException {@code MFA_CODE_INVALID} for a wrong, expired or reused code
     */
    @Transactional
    public Verified verify(UUID userId, String code) {
        UserEntity user = user(userId);
        if (user.getMfaSecret() == null) {
            throw notEnabled();
        }
        Instant now = clock.instant();
        if (user.isMfaEnabled()) {
            if (!check(user, code, now)) {
                throw invalidCode();
            }
            return new Verified(status(user), null, null, false);
        }
        OptionalLong step = totp.match(cipher.decrypt(user.getMfaSecret()), code, now, user.getMfaLastStep());
        if (step.isEmpty()) {
            throw invalidCode();
        }
        user.recordMfaStep(step.getAsLong());
        user.activateMfa(now);
        users.save(user);
        List<String> codes = replaceRecoveryCodes(user.getId(), now);
        mailer.sendMfaChanged(user, true, false);
        return new Verified(status(user), codes, now, true);
    }

    /**
     * Checks a sign-in step-up code for a user with MFA active: a current TOTP code (its step recorded) or an unused
     * recovery code (consumed). Used by {@code login} (R2-003).
     *
     * @param user the user, MFA active
     * @param code the code presented
     * @return true when it was accepted
     */
    @Transactional
    public boolean verifyLogin(UserEntity user, String code) {
        return user.isMfaEnabled() && user.getMfaSecret() != null && check(user, code, clock.instant());
    }

    /** A TOTP code (recording its step) or an unused recovery code (consuming it); saves the user. */
    private boolean check(UserEntity user, String code, Instant now) {
        if (code == null || code.isBlank()) {
            return false;
        }
        OptionalLong step = totp.match(cipher.decrypt(user.getMfaSecret()), code, now, user.getMfaLastStep());
        if (step.isPresent()) {
            user.recordMfaStep(step.getAsLong());
            users.save(user);
            return true;
        }
        String normalized = normalizeRecoveryCode(code);
        if (normalized.length() != RECOVERY_CODE_LENGTH) {
            return false;
        }
        return recoveryCodes.findByUserIdAndCodeHashAndUsedAtIsNull(user.getId(), hasher.hash(normalized))
                .map(r -> {
                    r.use(now);
                    recoveryCodes.save(r);
                    return true;
                })
                .orElse(false);
    }

    /**
     * A recovery code as stored: lower case, without spaces or dashes.
     *
     * @param code the code as typed
     * @return the normalized code
     */
    static String normalizeRecoveryCode(String code) {
        return code.replace("-", "").replace(" ", "").toLowerCase(Locale.ROOT);
    }

    /**
     * {@code getMfaRecoveryCodes}: a fresh set, invalidating the previous one.
     *
     * @param userId the user
     * @return the {@code MfaRecoveryCodes} view: {@code codes} (shown once) and {@code generatedAt}
     * @throws ConflictException {@code MFA_NOT_ENABLED} while MFA is not active
     */
    @Transactional
    public Map<String, Object> regenerateRecoveryCodes(UUID userId) {
        UserEntity user = user(userId);
        if (!user.isMfaEnabled()) {
            throw notEnabled();
        }
        Instant now = clock.instant();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("codes", replaceRecoveryCodes(user.getId(), now));
        out.put("generatedAt", now);
        return out;
    }

    private List<String> replaceRecoveryCodes(UUID userId, Instant now) {
        recoveryCodes.deleteByUserId(userId);
        Base32 base32 = new Base32();
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODES; i++) {
            byte[] bytes = new byte[7];
            random.nextBytes(bytes);
            String code = base32.encodeAsString(bytes).substring(0, RECOVERY_CODE_LENGTH).toLowerCase(Locale.ROOT);
            recoveryCodes.save(new RecoveryCodeEntity(userId, hasher.hash(code), now));
            codes.add(code.substring(0, RECOVERY_CODE_LENGTH / 2) + "-" + code.substring(RECOVERY_CODE_LENGTH / 2));
        }
        return codes;
    }

    /**
     * {@code disableMfa}: re-authenticates with a code (TOTP or recovery) or the password, then forgets the secret and
     * the recovery codes. A pending enrollment is discarded the same way.
     *
     * @param userId the user
     * @param code a current TOTP or recovery code, or null
     * @param password the account password, or null
     * @return the {@code MfaStatus} view
     * @throws ConflictException {@code MFA_REQUIRED} for a holder of a role that requires MFA; {@code MFA_NOT_ENABLED}
     *     with nothing to disable
     * @throws ValidationException neither a code nor a password, or the one given is wrong
     */
    @Transactional
    public Map<String, Object> disable(UUID userId, String code, String password) {
        UserEntity user = user(userId);
        boolean hasCode = code != null && !code.isBlank();
        boolean hasPassword = password != null && !password.isEmpty();
        if (!hasCode && !hasPassword) {
            throw new ValidationException("Re-authenticate with a code or the account password.", List.of(
                    new FieldError("code", "a code or the password is required"),
                    new FieldError("password", "a code or the password is required")));
        }
        if (required(userId)) {
            throw new ConflictException(ErrorCodes.MFA_REQUIRED, "A role you hold requires MFA, so it cannot be"
                    + " disabled. An administrator can reset it.");
        }
        if (user.getMfaSecret() == null) {
            throw notEnabled();
        }
        Instant now = clock.instant();
        boolean ok = hasCode
                ? user.isMfaEnabled() && check(user, code, now)
                : user.getPasswordHash() != null && passwords.matches(password, user.getPasswordHash());
        if (!ok) {
            if (hasCode) {
                throw invalidCode();
            }
            throw ValidationException.forField("password", "is not correct");
        }
        boolean wasEnabled = user.isMfaEnabled();
        user.clearMfa(now);
        users.save(user);
        recoveryCodes.deleteByUserId(user.getId());
        if (wasEnabled) {
            mailer.sendMfaChanged(user, false, false);
        }
        return status(user);
    }

    /**
     * Clears a user's MFA on an administrator's behalf (for the user-management API, R2-005), whatever the user's
     * roles; a user whose role requires MFA must enroll again before writing anything.
     *
     * @param userId the user
     * @return true when there was anything to clear
     */
    @Transactional
    public boolean adminReset(UUID userId) {
        UserEntity user = user(userId);
        boolean had = user.getMfaSecret() != null || user.isMfaEnabled();
        boolean wasEnabled = user.isMfaEnabled();
        user.clearMfa(clock.instant());
        users.save(user);
        recoveryCodes.deleteByUserId(userId);
        if (wasEnabled) {
            mailer.sendMfaChanged(user, false, true);
        }
        return had;
    }

    private UserEntity user(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_USER,
                "No such user."));
    }

    private static ConflictException notEnabled() {
        return new ConflictException(ErrorCodes.MFA_NOT_ENABLED, "MFA is not enabled for this account.");
    }

    private static ValidationException invalidCode() {
        return new ValidationException(ErrorCodes.MFA_CODE_INVALID, "The code is not valid. It may be wrong, expired"
                + " or already used.", List.of(new FieldError("code", "is not valid")));
    }
}
