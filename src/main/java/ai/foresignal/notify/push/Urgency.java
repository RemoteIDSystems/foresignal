package ai.foresignal.notify.push;

import java.util.Locale;

/** The RFC 8030 §5.3 {@code Urgency} header values (prompt 031). */
public enum Urgency {
    /** Only on power and Wi-Fi; quiet-hours downgrade. */
    VERY_LOW,
    /** Info. */
    LOW,
    /** Warning. */
    NORMAL,
    /** Critical and verification pushes. */
    HIGH;

    /** The header value ({@code very-low}, {@code low}, {@code normal}, {@code high}). */
    public String header() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** The urgency of a severity: critical high, warning normal, info low. */
    public static Urgency of(String severity) {
        if ("critical".equals(severity)) {
            return HIGH;
        }
        return "warning".equals(severity) ? NORMAL : LOW;
    }
}
