package ai.foresignal.trigger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Accepts only the regular expressions a {@code matches} condition may use (prompt 017): at most
 * 256 characters, compilable by {@link Pattern}, and free of the constructs that make matching slow
 * or that PostgreSQL's ARE engine reads differently from Java.
 *
 * <p>Rejected:
 *
 * <ul>
 *   <li>backreferences ({@code \1}, {@code \k<name>});
 *   <li>every {@code (?…)} construct except the non-capturing group {@code (?:…)}, which covers
 *       lookahead, lookbehind, named and atomic groups and inline flags;
 *   <li>nested quantifiers: a group containing {@code *}, {@code +} or {@code {…}} that is itself
 *       followed by {@code *}, {@code +} or {@code {…}}, such as {@code (a+)+};
 *   <li>possessive quantifiers ({@code a*+});
 *   <li>escapes other than {@code \d \D \w \W \s \S \n \t \r \f} and escaped punctuation, because
 *       letters such as {@code \b} mean different things in Java and PostgreSQL;
 *   <li>nested character classes and {@code &&} intersections inside a class (Java only).
 * </ul>
 */
public final class RegexGuard {

    /** The longest pattern accepted. */
    public static final int MAX_LENGTH = 256;

    private static final String ALLOWED_LETTER_ESCAPES = "dDwWsSntrf";

    private RegexGuard() {}

    /**
     * Checks and compiles {@code regex}.
     *
     * @return the compiled pattern
     * @throws IllegalArgumentException with the reason when it is not acceptable
     */
    public static Pattern compile(String regex) {
        if (regex == null || regex.isEmpty()) {
            throw new IllegalArgumentException("must be a non-empty regular expression");
        }
        if (regex.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("must be at most " + MAX_LENGTH + " characters");
        }
        scan(regex);
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("is not a valid regular expression: " + e.getDescription());
        }
    }

    private static void scan(String regex) {
        // One frame per open group: whether it contains a quantifier.
        Deque<boolean[]> groups = new ArrayDeque<>();
        boolean lastWasQuantifier = false;
        int i = 0;
        int n = regex.length();
        while (i < n) {
            char c = regex.charAt(i);
            switch (c) {
                case '\\' -> {
                    if (i + 1 >= n) {
                        throw new IllegalArgumentException("ends with a dangling backslash");
                    }
                    checkEscape(regex.charAt(i + 1));
                    i += 2;
                    lastWasQuantifier = false;
                    continue;
                }
                case '[' -> {
                    i = skipClass(regex, i);
                    lastWasQuantifier = false;
                    continue;
                }
                case '(' -> {
                    if (i + 1 < n && regex.charAt(i + 1) == '?') {
                        if (i + 2 < n && regex.charAt(i + 2) == ':') {
                            i += 3;
                        } else {
                            throw new IllegalArgumentException(
                                    "must not use lookaround, named or atomic groups or inline flags; only (?:...) is allowed");
                        }
                    } else {
                        i++;
                    }
                    groups.push(new boolean[] {false});
                    lastWasQuantifier = false;
                    continue;
                }
                case ')' -> {
                    if (groups.isEmpty()) {
                        throw new IllegalArgumentException("has an unbalanced ')'");
                    }
                    boolean innerQuantified = groups.pop()[0];
                    i++;
                    if (i < n && isRepeating(regex.charAt(i)) && innerQuantified) {
                        throw new IllegalArgumentException("must not nest quantifiers such as (a+)+");
                    }
                    if (innerQuantified && !groups.isEmpty()) {
                        groups.peek()[0] = true;
                    }
                    lastWasQuantifier = false;
                    continue;
                }
                case '*', '+' -> {
                    if (lastWasQuantifier && c == '+') {
                        throw new IllegalArgumentException("must not use possessive quantifiers");
                    }
                    markQuantified(groups);
                    lastWasQuantifier = true;
                    i++;
                    continue;
                }
                case '{' -> {
                    int close = regex.indexOf('}', i);
                    if (close < 0) {
                        throw new IllegalArgumentException("has an unclosed '{'");
                    }
                    markQuantified(groups);
                    i = close + 1;
                    if (i < n && regex.charAt(i) == '+') {
                        throw new IllegalArgumentException("must not use possessive quantifiers");
                    }
                    lastWasQuantifier = true;
                    continue;
                }
                case '?' -> {
                    if (lastWasQuantifier && i + 1 < n && regex.charAt(i + 1) == '+') {
                        throw new IllegalArgumentException("must not use possessive quantifiers");
                    }
                    if (i + 1 < n && regex.charAt(i + 1) == '+') {
                        throw new IllegalArgumentException("must not use possessive quantifiers");
                    }
                    lastWasQuantifier = true;
                    i++;
                    continue;
                }
                default -> {
                    lastWasQuantifier = false;
                    i++;
                }
            }
        }
    }

    private static boolean isRepeating(char c) {
        return c == '*' || c == '+' || c == '{';
    }

    private static void markQuantified(Deque<boolean[]> groups) {
        if (!groups.isEmpty()) {
            groups.peek()[0] = true;
        }
    }

    private static void checkEscape(char escaped) {
        if (escaped >= '1' && escaped <= '9') {
            throw new IllegalArgumentException("must not use backreferences");
        }
        if (escaped == 'k') {
            throw new IllegalArgumentException("must not use backreferences");
        }
        if (Character.isLetterOrDigit(escaped) && ALLOWED_LETTER_ESCAPES.indexOf(escaped) < 0) {
            throw new IllegalArgumentException(
                    "must only use the escapes \\d \\D \\w \\W \\s \\S \\n \\t \\r \\f and escaped punctuation");
        }
    }

    /** The index just past the class that starts at {@code start}. */
    private static int skipClass(String regex, int start) {
        int i = start + 1;
        int n = regex.length();
        if (i < n && regex.charAt(i) == '^') {
            i++;
        }
        if (i < n && regex.charAt(i) == ']') {
            i++;
        }
        while (i < n) {
            char c = regex.charAt(i);
            if (c == '\\') {
                if (i + 1 >= n) {
                    throw new IllegalArgumentException("ends with a dangling backslash");
                }
                checkEscape(regex.charAt(i + 1));
                i += 2;
                continue;
            }
            if (c == '[') {
                throw new IllegalArgumentException("must not nest character classes");
            }
            if (c == '&' && i + 1 < n && regex.charAt(i + 1) == '&') {
                throw new IllegalArgumentException("must not use class intersections (&&)");
            }
            if (c == ']') {
                return i + 1;
            }
            i++;
        }
        throw new IllegalArgumentException("has an unclosed '['");
    }
}
