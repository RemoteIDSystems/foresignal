package ai.foresignal.security.user;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The one password policy (prompt R2-002), applied to activation, reset and an admin setting a password: at least
 * {@value #MIN_LENGTH} characters, not blank, at most {@value #MAX_LENGTH}. Deliberately no composition rules.
 * Lengths count Unicode code points, so a passphrase in any script counts what the person typed.
 */
@Component
public class PasswordPolicy {

    /** Fewest code points a password may have. */
    public static final int MIN_LENGTH = 12;

    /** Most code points a password may have; bounds the hashing work one request can ask for. */
    public static final int MAX_LENGTH = 1024;

    /**
     * Checks a password.
     *
     * @param raw the password; never included in the error
     * @param field the request field to name in the error, for example {@code password}
     * @throws ValidationException {@code 400 PASSWORD_POLICY} with one field error when the password is refused
     */
    public void validate(String raw, String field) {
        String problem = problem(raw);
        if (problem != null) {
            throw new ValidationException(ErrorCodes.PASSWORD_POLICY, "The password does not meet the password policy.",
                    List.of(new FieldError(field, problem)));
        }
    }

    /**
     * Why a password is refused.
     *
     * @param raw the password
     * @return the reason, or null when acceptable
     */
    public String problem(String raw) {
        if (raw == null || raw.isBlank()) {
            return "must not be empty or only whitespace";
        }
        int length = raw.codePointCount(0, raw.length());
        if (length < MIN_LENGTH) {
            return "must be at least " + MIN_LENGTH + " characters";
        }
        if (length > MAX_LENGTH) {
            return "must be at most " + MAX_LENGTH + " characters";
        }
        return null;
    }
}
