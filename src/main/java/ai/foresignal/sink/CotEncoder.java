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

    private static String esc(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
