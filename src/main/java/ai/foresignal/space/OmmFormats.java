package ai.foresignal.space;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes element sets in the formats CelesTrak's {@code gp.php} serves (prompt 064, the CelesTrak-compatible
 * output): TLE lines generated from OMM numbers with the classic column layout and mod-10 checksums, CSV,
 * JSON, CCSDS OMM XML and CCSDS OMM KVN.
 *
 * <p>Pure functions. TLE output can carry only catalog numbers up to {@value OmmElements#MAX_TLE_NORAD}:
 * CelesTrak does not use alpha-5, so neither does Foresignal, and callers leave larger numbers out.
 */
public final class OmmFormats {

    /** CelesTrak's line ending in TLE and CSV output. */
    public static final String CRLF = "\r\n";

    /** The width CelesTrak pads a TLE name line to. */
    public static final int NAME_WIDTH = 24;

    private OmmFormats() {}

    // ------------------------------------------------------------------------- TLE

    /**
     * The two TLE lines of an element set.
     *
     * @throws IllegalArgumentException when the catalog number exceeds {@value OmmElements#MAX_TLE_NORAD}
     */
    public static String[] tleLines(OmmElements e) {
        if (!e.tleCompatible()) {
            throw new IllegalArgumentException("NORAD " + e.norad() + " cannot be written as a TLE (no alpha-5)");
        }
        StringBuilder l1 = new StringBuilder(69);
        l1.append("1 ").append(String.format(Locale.ROOT, "%05d", e.norad()))
                .append(e.classification().charAt(0)).append(' ')
                .append(pad(intlDesignator(e.objectId()), 8)).append(' ')
                .append(tleEpoch(e.epoch())).append(' ')
                .append(firstDerivative(e.meanMotionDot())).append(' ')
                .append(exponential(e.meanMotionDdot())).append(' ')
                .append(exponential(e.bstar())).append(' ')
                .append(Math.max(0, Math.min(9, e.ephemerisType()))).append(' ')
                .append(String.format(Locale.ROOT, "%4d", Math.floorMod(e.elementSetNo(), 10_000)));
        l1.append(checksum(l1));

        StringBuilder l2 = new StringBuilder(69);
        l2.append("2 ").append(String.format(Locale.ROOT, "%05d", e.norad())).append(' ')
                .append(angle(e.inclination())).append(' ')
                .append(angle(e.raan())).append(' ')
                .append(String.format(Locale.ROOT, "%07d", eccentricityDigits(e.eccentricity())))
                .append(' ')
                .append(angle(e.argOfPericenter())).append(' ')
                .append(angle(e.meanAnomaly())).append(' ')
                .append(String.format(Locale.ROOT, "%11.8f", e.meanMotion()))
                .append(String.format(Locale.ROOT, "%5d", Math.floorMod(e.revAtEpoch(), 100_000L)));
        l2.append(checksum(l2));
        return new String[] {l1.toString(), l2.toString()};
    }

    /**
     * The seven assumed-decimal eccentricity digits, rounded half-even from the decimal value ({@code .00067245}
     * → {@code 0006724}, as CelesTrak writes the ISS element set of 2026-10-13).
     */
    static long eccentricityDigits(double eccentricity) {
        long digits = BigDecimal.valueOf(Math.abs(eccentricity)).movePointRight(7)
                .setScale(0, java.math.RoundingMode.HALF_EVEN).longValue();
        return Math.min(9_999_999L, digits);
    }

    /** The name line of a three-line element set: the name padded to {@value #NAME_WIDTH} characters. */
    public static String nameLine(OmmElements e) {
        String name = e.objectName() == null ? "" : e.objectName();
        return name.length() >= NAME_WIDTH ? name : pad(name, NAME_WIDTH);
    }

    /**
     * TLE text: name line (when {@code withName}) and two element lines per object, CRLF-terminated, as
     * CelesTrak serves {@code FORMAT=TLE}/{@code 3LE} and {@code 2LE}. Objects above
     * {@value OmmElements#MAX_TLE_NORAD} are left out.
     */
    public static String tle(List<OmmElements> elements, boolean withName) {
        StringBuilder out = new StringBuilder();
        for (OmmElements e : elements) {
            if (!e.tleCompatible()) {
                continue;
            }
            String[] lines = tleLines(e);
            if (withName) {
                out.append(nameLine(e)).append(CRLF);
            }
            out.append(lines[0]).append(CRLF).append(lines[1]).append(CRLF);
        }
        return out.toString();
    }

    /** The mod-10 checksum of a TLE line's first 68 characters: digits count their value, {@code -} counts 1. */
    public static int checksum(CharSequence line) {
        int sum = 0;
        for (int i = 0; i < Math.min(68, line.length()); i++) {
            char c = line.charAt(i);
            if (c >= '0' && c <= '9') {
                sum += c - '0';
            } else if (c == '-') {
                sum += 1;
            }
        }
        return sum % 10;
    }

    /** {@code 1998-067A} → {@code 98067A}; anything unparseable → blank. */
    static String intlDesignator(String objectId) {
        if (objectId == null || objectId.length() < 8 || objectId.charAt(4) != '-') {
            return "";
        }
        String piece = objectId.substring(8);
        return (objectId.substring(2, 4) + objectId.substring(5, 8) + piece).substring(0,
                Math.min(8, 5 + piece.length()));
    }

    /** {@code YYDDD.DDDDDDDD}. */
    static String tleEpoch(Instant epoch) {
        ZonedDateTime t = epoch.atZone(ZoneOffset.UTC);
        double secondOfDay = t.toLocalTime().toNanoOfDay() / 1e9;
        double day = t.getDayOfYear() + secondOfDay / 86_400.0;
        return String.format(Locale.ROOT, "%02d%012.8f", t.getYear() % 100, day);
    }

    /** The first-derivative field: sign and {@code .dddddddd} ({@code " .00005858"}). */
    static String firstDerivative(double value) {
        String digits = String.format(Locale.ROOT, "%.8f", Math.abs(value));
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        String field = (value < 0 ? "-" : " ") + digits;
        return field.length() > 10 ? field.substring(0, 10) : field;
    }

    /**
     * The assumed-decimal exponential field of {@code MEAN_MOTION_DDOT} and {@code BSTAR}: sign, five mantissa
     * digits, exponent sign and digit ({@code " 11499-3"}); zero is {@code " 00000+0"}, as CelesTrak writes it.
     */
    static String exponential(double value) {
        if (value == 0 || !Double.isFinite(value)) {
            return " 00000+0";
        }
        double a = Math.abs(value);
        int exp = (int) Math.floor(Math.log10(a)) + 1;
        long mantissa = Math.round(a / Math.pow(10, exp) * 1e5);
        if (mantissa >= 100_000) {
            mantissa /= 10;
            exp++;
        }
        if (exp > 9) {
            exp = 9;
            mantissa = 99_999;
        }
        if (exp < -9) {
            return " 00000+0";
        }
        return (value < 0 ? "-" : " ") + String.format(Locale.ROOT, "%05d", mantissa) + (exp < 0 ? "-" : "+")
                + Math.abs(exp);
    }

    private static String angle(double degrees) {
        return String.format(Locale.ROOT, "%8.4f", degrees);
    }

    private static String pad(String text, int width) {
        StringBuilder b = new StringBuilder(text);
        while (b.length() < width) {
            b.append(' ');
        }
        return b.toString();
    }

    // ------------------------------------------------------------------------- CSV

    /** CelesTrak's CSV: the 17 OMM columns with a header row, RFC 4180 quoting, CRLF line endings. */
    public static String csv(List<OmmElements> elements) {
        StringBuilder out = new StringBuilder(String.join(",", OmmElements.KEYS)).append(CRLF);
        for (OmmElements e : elements) {
            out.append(csvField(e.objectName())).append(',')
                    .append(csvField(e.objectId() == null ? "" : e.objectId())).append(',')
                    .append(e.epochText()).append(',')
                    .append(number(e.meanMotion())).append(',')
                    .append(number(e.eccentricity())).append(',')
                    .append(number(e.inclination())).append(',')
                    .append(number(e.raan())).append(',')
                    .append(number(e.argOfPericenter())).append(',')
                    .append(number(e.meanAnomaly())).append(',')
                    .append(e.ephemerisType()).append(',')
                    .append(csvField(e.classification())).append(',')
                    .append(e.norad()).append(',')
                    .append(e.elementSetNo()).append(',')
                    .append(e.revAtEpoch()).append(',')
                    .append(number(e.bstar())).append(',')
                    .append(number(e.meanMotionDot())).append(',')
                    .append(number(e.meanMotionDdot())).append(CRLF);
        }
        return out.toString();
    }

    private static String csvField(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    /** A number in plain notation without trailing zeros ({@code 0.00011499036}, {@code 0}). */
    static String number(double value) {
        if (value == 0) {
            return "0";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    // ------------------------------------------------------------------------- JSON

    /** CelesTrak's JSON: an array of OMM objects, compact or pretty. */
    public static String json(List<OmmElements> elements, JsonMapper mapper, boolean pretty) {
        List<java.util.Map<String, Object>> rows = elements.stream().map(OmmElements::toPayload).toList();
        return pretty ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows) : mapper.writeValueAsString(rows);
    }

    // ------------------------------------------------------------------------- CCSDS OMM

    /**
     * CCSDS OMM 2.0 XML (NDM/XML): one {@code omm} per object inside an {@code ndm} root, SGP4 mean elements
     * in TEME, UTC.
     *
     * @param created the creation date written in each header
     */
    public static String xml(List<OmmElements> elements, Instant created) {
        String date = created.truncatedTo(ChronoUnit.SECONDS).toString();
        StringBuilder out = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.append("<ndm xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
                .append(" xsi:noNamespaceSchemaLocation=\"https://sanaregistry.org/r/ndmxml_unqualified/")
                .append("ndmxml-2.0.0-master-2.0.xsd\">\n");
        for (OmmElements e : elements) {
            out.append("<omm id=\"CCSDS_OMM_VERS\" version=\"2.0\">\n")
                    .append("<header><CREATION_DATE>").append(date).append("</CREATION_DATE>")
                    .append("<ORIGINATOR>Foresignal (from CelesTrak)</ORIGINATOR></header>\n")
                    .append("<body><segment><metadata>")
                    .append(tag("OBJECT_NAME", e.objectName()))
                    .append(tag("OBJECT_ID", e.objectId() == null ? "" : e.objectId()))
                    .append("<CENTER_NAME>EARTH</CENTER_NAME><REF_FRAME>TEME</REF_FRAME>")
                    .append("<TIME_SYSTEM>UTC</TIME_SYSTEM><MEAN_ELEMENT_THEORY>SGP4</MEAN_ELEMENT_THEORY>")
                    .append("</metadata><data><meanElements>")
                    .append(tag("EPOCH", e.epochText()))
                    .append(tag("MEAN_MOTION", number(e.meanMotion())))
                    .append(tag("ECCENTRICITY", number(e.eccentricity())))
                    .append(tag("INCLINATION", number(e.inclination())))
                    .append(tag("RA_OF_ASC_NODE", number(e.raan())))
                    .append(tag("ARG_OF_PERICENTER", number(e.argOfPericenter())))
                    .append(tag("MEAN_ANOMALY", number(e.meanAnomaly())))
                    .append("</meanElements><tleParameters>")
                    .append(tag("EPHEMERIS_TYPE", Integer.toString(e.ephemerisType())))
                    .append(tag("CLASSIFICATION_TYPE", e.classification()))
                    .append(tag("NORAD_CAT_ID", Long.toString(e.norad())))
                    .append(tag("ELEMENT_SET_NO", Integer.toString(e.elementSetNo())))
                    .append(tag("REV_AT_EPOCH", Long.toString(e.revAtEpoch())))
                    .append(tag("BSTAR", number(e.bstar())))
                    .append(tag("MEAN_MOTION_DOT", number(e.meanMotionDot())))
                    .append(tag("MEAN_MOTION_DDOT", number(e.meanMotionDdot())))
                    .append("</tleParameters></data></segment></body>\n</omm>\n");
        }
        return out.append("</ndm>\n").toString();
    }

    /**
     * CCSDS OMM 2.0 KVN: one {@code KEY = value} block per object, separated by a blank line.
     *
     * @param created the creation date written in each block
     */
    public static String kvn(List<OmmElements> elements, Instant created) {
        String date = created.truncatedTo(ChronoUnit.SECONDS).toString();
        StringBuilder out = new StringBuilder();
        for (OmmElements e : elements) {
            if (!out.isEmpty()) {
                out.append('\n');
            }
            kv(out, "CCSDS_OMM_VERS", "2.0");
            kv(out, "CREATION_DATE", date);
            kv(out, "ORIGINATOR", "Foresignal (from CelesTrak)");
            kv(out, "OBJECT_NAME", e.objectName());
            kv(out, "OBJECT_ID", e.objectId() == null ? "" : e.objectId());
            kv(out, "CENTER_NAME", "EARTH");
            kv(out, "REF_FRAME", "TEME");
            kv(out, "TIME_SYSTEM", "UTC");
            kv(out, "MEAN_ELEMENT_THEORY", "SGP4");
            kv(out, "EPOCH", e.epochText());
            kv(out, "MEAN_MOTION", number(e.meanMotion()));
            kv(out, "ECCENTRICITY", number(e.eccentricity()));
            kv(out, "INCLINATION", number(e.inclination()));
            kv(out, "RA_OF_ASC_NODE", number(e.raan()));
            kv(out, "ARG_OF_PERICENTER", number(e.argOfPericenter()));
            kv(out, "MEAN_ANOMALY", number(e.meanAnomaly()));
            kv(out, "EPHEMERIS_TYPE", Integer.toString(e.ephemerisType()));
            kv(out, "CLASSIFICATION_TYPE", e.classification());
            kv(out, "NORAD_CAT_ID", Long.toString(e.norad()));
            kv(out, "ELEMENT_SET_NO", Integer.toString(e.elementSetNo()));
            kv(out, "REV_AT_EPOCH", Long.toString(e.revAtEpoch()));
            kv(out, "BSTAR", number(e.bstar()));
            kv(out, "MEAN_MOTION_DOT", number(e.meanMotionDot()));
            kv(out, "MEAN_MOTION_DDOT", number(e.meanMotionDdot()));
        }
        return out.toString();
    }

    private static void kv(StringBuilder out, String key, String value) {
        out.append(String.format(Locale.ROOT, "%-20s", key)).append("= ").append(value).append('\n');
    }

    private static String tag(String name, String value) {
        return "<" + name + ">" + escape(value) + "</" + name + ">";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder b = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> b.append("&amp;");
                case '<' -> b.append("&lt;");
                case '>' -> b.append("&gt;");
                case '"' -> b.append("&quot;");
                case '\'' -> b.append("&apos;");
                default -> {
                    if (c >= 0x20 || c == '\t') {
                        b.append(c);
                    }
                }
            }
        }
        return b.toString();
    }
}
