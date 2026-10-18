package ai.foresignal.doctor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code javac}-free sanity checks of a patched file (prompt 140, task 5): balanced braces, parentheses and
 * brackets outside strings, characters and comments, and the top-level type name unchanged. Nothing is compiled.
 */
public final class PatchSanity {

    private static final Pattern TYPE = Pattern.compile(
            "(?m)^\\s*(?:public\\s+|final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)*(?:class|record|interface|enum)"
                    + "\\s+([A-Za-z_$][A-Za-z0-9_$]*)");

    private PatchSanity() {}

    /**
     * The checks' results.
     *
     * @param bracesBalanced true when braces, parentheses and brackets balance
     * @param classNameUnchanged true when the first top-level type keeps its name
     */
    public record Checks(boolean bracesBalanced, boolean classNameUnchanged) {}

    /** Checks {@code patched} against {@code original}. */
    public static Checks check(String original, String patched) {
        return new Checks(balanced(patched), typeName(original) != null
                && typeName(original).equals(typeName(patched)));
    }

    /** The name of the first declared type, or null. */
    static String typeName(String source) {
        if (source == null) {
            return null;
        }
        Matcher m = TYPE.matcher(stripLiterals(source));
        return m.find() ? m.group(1) : null;
    }

    /** True when {@code {}}, {@code ()} and {@code []} balance outside strings, characters and comments. */
    static boolean balanced(String source) {
        if (source == null) {
            return false;
        }
        String code = stripLiterals(source);
        StringBuilder stack = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            switch (c) {
                case '{', '(', '[' -> stack.append(c);
                case '}', ')', ']' -> {
                    if (stack.isEmpty()) {
                        return false;
                    }
                    char open = stack.charAt(stack.length() - 1);
                    if ((c == '}' && open != '{') || (c == ')' && open != '(') || (c == ']' && open != '[')) {
                        return false;
                    }
                    stack.setLength(stack.length() - 1);
                }
                default -> { }
            }
        }
        return stack.isEmpty();
    }

    /** {@code source} with comments, string, text-block and character literals blanked out. */
    static String stripLiterals(String source) {
        StringBuilder out = new StringBuilder(source.length());
        int i = 0;
        int n = source.length();
        while (i < n) {
            char c = source.charAt(i);
            if (c == '/' && i + 1 < n && source.charAt(i + 1) == '/') {
                while (i < n && source.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '/' && i + 1 < n && source.charAt(i + 1) == '*') {
                int end = source.indexOf("*/", i + 2);
                i = end < 0 ? n : end + 2;
                out.append(' ');
            } else if (c == '"' && source.startsWith("\"\"\"", i)) {
                int end = source.indexOf("\"\"\"", i + 3);
                i = end < 0 ? n : end + 3;
                out.append("\"\"");
            } else if (c == '"' || c == '\'') {
                char quote = c;
                i++;
                while (i < n && source.charAt(i) != quote && source.charAt(i) != '\n') {
                    if (source.charAt(i) == '\\') {
                        i++;
                    }
                    i++;
                }
                i++;
                out.append(quote).append(quote);
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}
