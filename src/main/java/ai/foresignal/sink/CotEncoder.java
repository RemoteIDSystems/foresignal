package ai.foresignal.sink;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Cursor-on-Target XML (prompt 110; MITRE CoT base event, protocol version 0). Affiliation is neutral
 * ({@code u}, default) or unknown ({@code n}) only; type map aircraft {@code a-{aff}-A}, vessel
 * {@code a-{aff}-S}, other {@code a-{aff}-G}. Implemented from the public schema, no TAK code.
 */
public final class CotEncoder {

    /** The unknown hae/ce/le sentinel. */
    public static final double UNKNOWN = 9_999_999.0;

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    private CotEncoder() {}

    /** The CoT type of a kind and affiliation. */
    public static String type(String kind, String affiliation) {
        String aff = "n".equals(affiliation) ? "n" : "u";
        String suffix = "vessel".equals(kind) ? "S" : "aircraft".equals(kind) ? "A" : "G";
        return "a-" + aff + "-" + suffix;
    }

    /** An {@code <event>} document. */
    public static String event(String uid, String type, double lat, double lon, Instant time, Instant stale,
            String callsign, Double courseDeg, Double speedMs, String remarks, String link) {
        StringBuilder xml = new StringBuilder(512);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<event version=\"2.0\" uid=\"").append(esc(uid)).append("\" type=\"").append(esc(type))
                .append("\" how=\"m-g\" time=\"").append(TIME.format(time)).append("\" start=\"")
                .append(TIME.format(time)).append("\" stale=\"").append(TIME.format(stale)).append("\">");
        xml.append("<point lat=\"").append(fmt(lat)).append("\" lon=\"").append(fmt(lon)).append("\" hae=\"")
                .append(fmt(UNKNOWN)).append("\" ce=\"").append(fmt(UNKNOWN)).append("\" le=\"")
                .append(fmt(UNKNOWN)).append("\"/>");
        xml.append("<detail>");
        if (callsign != null && !callsign.isBlank()) {
            xml.append("<contact callsign=\"").append(esc(callsign.strip())).append("\"/>");
        }
        if ((courseDeg != null && Double.isFinite(courseDeg)) || (speedMs != null && Double.isFinite(speedMs))) {
            xml.append("<track course=\"").append(courseDeg == null ? "0" : fmt(courseDeg)).append("\" speed=\"")
                    .append(speedMs == null ? "0" : fmt(speedMs)).append("\"/>");
        }
        if (remarks != null && !remarks.isBlank()) {
            xml.append("<remarks>").append(esc(remarks)).append("</remarks>");
        }
        if (link != null && !link.isBlank()) {
            xml.append("<link url=\"").append(esc(link)).append("\"/>");
        }
        xml.append("</detail></event>");
        return xml.toString();
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.6f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /** Knots to metres per second (CoT {@code track/@speed} is m/s; Foresignal aircraft and AIS speeds are knots). */
    public static final double KNOTS_TO_MS = 0.514444;

    /**
     * XML-escapes {@code value} after removing every character XML 1.0 does not allow (C0 controls other than
     * tab, LF and CR, unpaired surrogates, U+FFFE and U+FFFF), so an upstream callsign or title can never make the
     * event unparseable.
     */
    static String esc(String value) {
        return stripInvalidXml(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /** {@code value} without the characters XML 1.0 forbids. */
    static String stripInvalidXml(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 < value.length() && Character.isLowSurrogate(value.charAt(i + 1))) {
                    out.append(c).append(value.charAt(++i));
                }
                continue;
            }
            if (Character.isLowSurrogate(c)) {
                continue;
            }
            boolean allowed = c == 0x9 || c == 0xA || c == 0xD || (c >= 0x20 && c <= 0xD7FF)
                    || (c >= 0xE000 && c <= 0xFFFD);
            if (allowed) {
                out.append(c);
            }
        }
        return out.toString();
    }
}
