package ai.foresignal.source.adapters.ct;

import java.net.IDN;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Validates and normalizes a watched domain (prompt 076). Pure.
 *
 * <p>A watched domain is a host name of at least two labels, IDNA-normalized to its ASCII (Punycode) form, lower
 * case, without a trailing dot, at most {@value #MAX_LENGTH} characters, each label 1–63 letters, digits or inner
 * hyphens, and a top-level label that is not all digits (so an IP address is refused). Wildcards are refused. Whether
 * the name is registrable under the public suffix list is left to Cert Spotter, which refuses a bare suffix such as
 * {@code com} with {@code invalid_value}.
 */
public final class DomainNames {

    /** The longest domain accepted. */
    public static final int MAX_LENGTH = 253;

    private static final Pattern LABEL = Pattern.compile("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$");
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");

    private DomainNames() {}

    /**
     * The normalized form of {@code raw}, or empty when it is not an acceptable watched domain.
     *
     * @param raw the operator's value
     */
    public static Optional<String> normalize(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String s = raw.strip();
        if (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.isEmpty() || s.contains("*") || s.contains("/") || s.contains(":") || s.contains("@")) {
            return Optional.empty();
        }
        String ascii;
        try {
            ascii = IDN.toASCII(s, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (ascii.isEmpty() || ascii.length() > MAX_LENGTH) {
            return Optional.empty();
        }
        String[] labels = ascii.split("\\.", -1);
        if (labels.length < 2) {
            return Optional.empty();
        }
        for (String label : labels) {
            if (!LABEL.matcher(label).matches()) {
                return Optional.empty();
            }
        }
        if (DIGITS.matcher(labels[labels.length - 1]).matches()) {
            return Optional.empty();
        }
        return Optional.of(ascii);
    }
}
