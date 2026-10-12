package ai.foresignal.inbound.edp;

import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.networknt.schema.regex.RegularExpression;
import com.networknt.schema.regex.RegularExpressionFactory;

/**
 * Evaluates JSON Schema {@code pattern} and {@code patternProperties} with RE2/J, whose matching
 * time is linear in the input, so a provider's schema cannot run a catastrophic backtracking
 * pattern (EDP §7, invariant EXT-6).
 */
public final class Re2RegularExpressionFactory implements RegularExpressionFactory {

    /** The shared instance; stateless. */
    public static final Re2RegularExpressionFactory INSTANCE = new Re2RegularExpressionFactory();

    /**
     * Compiles {@code regex}.
     *
     * @throws IllegalArgumentException when it is longer than 256 characters or RE2 cannot compile it
     */
    @Override
    public RegularExpression getRegularExpression(String regex) {
        Pattern pattern = compile(regex);
        // JSON Schema patterns are not anchored: find, not matches.
        return value -> pattern.matcher(value).find();
    }

    /**
     * Compiles {@code regex} with RE2/J after the length check.
     *
     * @throws IllegalArgumentException when it is too long or not RE2 syntax
     */
    public static Pattern compile(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("A pattern is required");
        }
        if (regex.length() > EdpProtocol.MAX_PATTERN_LENGTH) {
            throw new IllegalArgumentException("pattern is " + regex.length() + " characters; the maximum is "
                    + EdpProtocol.MAX_PATTERN_LENGTH);
        }
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("pattern is not supported by the linear-time engine: "
                    + e.getMessage(), e);
        }
    }
}
