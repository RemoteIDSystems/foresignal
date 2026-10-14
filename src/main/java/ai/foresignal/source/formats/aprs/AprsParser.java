package ai.foresignal.source.formats.aprs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses one APRS packet in TNC2 monitor format {@code SRC>DEST,PATH:info} (prompt 082). Pure and in-house, written
 * from the APRS Protocol Reference 1.0.1 (APRS101.PDF) and the 1.1/1.2 addenda; no GPL or unlicensed APRS library.
 *
 * <p><strong>Supported information fields</strong>:
 * <ul>
 *   <li><strong>Positions</strong> {@code !} and {@code =} (no timestamp), {@code /} and {@code @} (a 7-character
 *       timestamp, which is skipped: APRS timestamps are optional and unreliable, so the sources use the receive
 *       time). Both the uncompressed form {@code DDMM.hhN/DDDMM.hhW$} (position ambiguity spaces read as zero) and
 *       the compressed base-91 form (APRS101 ch. 9), with the course/speed or altitude of its {@code cs} bytes.
 *       Course and speed come from a {@code CSE/SPD} data extension; altitude from {@code /A=aaaaaa} in the comment
 *       (feet), which is removed from the comment.
 *   <li><strong>Position weather:</strong> a position whose symbol code is {@code _}; wind direction and speed from
 *       the data extension, then {@code g t r p P h b} values (APRS101 ch. 12), converted to SI.
 *   <li><strong>Mic-E</strong> ({@code `}, {@code '}): latitude, the message bits and the N/S, W/E and longitude
 *       offset flags from the destination field, longitude, speed, course and symbol from the information field
 *       (APRS101 ch. 10), and the optional base-91 altitude ({@code xxx}}).
 *   <li><strong>Objects</strong> ({@code ;}: a 9-character name, {@code *} live or {@code _} killed, a timestamp, a
 *       position) and <strong>items</strong> ({@code )}: a 3–9 character name ended by {@code !} live or {@code _}
 *       killed, a position).
 *   <li><strong>Status</strong> ({@code >}, with or without a {@code DDHHMMz} timestamp).
 *   <li><strong>Messages</strong> ({@code :ADDRESSEE:text{id}), including acknowledgements and rejections.
 * </ul>
 * Everything else (telemetry, third-party, raw NMEA, queries, ...) and anything that does not parse returns a packet
 * of type {@link AprsPacket.Type#UNSUPPORTED}. The q-construct and igate are read from the path.
 */
public final class AprsParser {

    /** The longest comment kept, in characters. */
    public static final int MAX_COMMENT = 256;

    /** Knots to meters per second. */
    static final double KNOTS_TO_MPS = 0.514444;

    /** Feet to meters. */
    static final double FEET_TO_M = 0.3048;

    /** Miles per hour to meters per second. */
    static final double MPH_TO_MPS = 0.44704;

    /**
     * A sender call: letters and digits and an optional SSID of up to three characters (APRS-IS carries non-RF
     * sources such as {@code DEMO-002}).
     */
    private static final Pattern CALL = Pattern.compile("^[A-Za-z0-9]{1,9}(-[A-Za-z0-9]{1,3})?$");

    /** A q-construct path element. */
    private static final Pattern Q_CONSTRUCT = Pattern.compile("^qA[A-Za-z]$");

    /** The altitude token of a comment. */
    private static final Pattern ALTITUDE = Pattern.compile("/A=(-?\\d{5,6})");

    /** A course/speed data extension. */
    private static final Pattern COURSE_SPEED = Pattern.compile("^(\\d{3})/(\\d{3})");

    /** A status timestamp. */
    private static final Pattern STATUS_TIME = Pattern.compile("^\\d{6}z");

    private static final String[] MIC_E_STANDARD = {
        "Emergency", "Priority", "Special", "Committed", "Returning", "In Service", "En Route", "Off Duty"
    };

    private AprsParser() {}

    /**
     * Parses one TNC2 line.
     *
     * @param tnc2Line the line, with or without its CR/LF terminator
     * @return the packet; never null ({@link AprsPacket.Type#UNSUPPORTED} when it does not parse)
     */
    public static AprsPacket parse(String tnc2Line) {
        Draft d = new Draft();
        String line = tnc2Line == null ? "" : stripTerminator(tnc2Line);
        d.raw = line;
        int gt = line.indexOf('>');
        int colon = line.indexOf(':');
        if (gt <= 0 || colon < gt) {
            return d.unsupported("no SRC>DEST,PATH:info header");
        }
        String source = line.substring(0, gt);
        if (!CALL.matcher(source).matches()) {
            return d.unsupported("the source '" + source + "' is not a call sign");
        }
        d.source = source.toUpperCase(java.util.Locale.ROOT);
        String[] header = line.substring(gt + 1, colon).split(",", -1);
        d.destination = header[0];
        d.path = new ArrayList<>(Arrays.asList(header).subList(1, header.length));
        for (int i = 0; i < d.path.size(); i++) {
            if (Q_CONSTRUCT.matcher(d.path.get(i)).matches()) {
                d.qConstruct = d.path.get(i);
                if (i + 1 < d.path.size()) {
                    d.igate = d.path.get(i + 1);
                }
                break;
            }
        }
        d.info = line.substring(colon + 1);
        if (d.info.isEmpty()) {
            return d.unsupported("empty information field");
        }
        try {
            return switch (d.info.charAt(0)) {
                case '!', '=' -> position(d, d.info.substring(1));
                case '/', '@' -> d.info.length() < 8 ? d.unsupported("short timestamped position")
                        : position(d, d.info.substring(8));
                case ';' -> object(d);
                case ')' -> item(d);
                case '>' -> status(d);
                case ':' -> message(d);
                case '`', '\'', (char) 0x1c, (char) 0x1d -> micE(d);
                default -> d.unsupported("unsupported data type '" + printable(d.info.charAt(0)) + "'");
            };
        } catch (RuntimeException e) {
            return d.unsupported("malformed: " + e.getMessage());
        }
    }

    // ---------- positions ----------

    private static AprsPacket position(Draft d, String body) {
        String rest = positionBody(d, body);
        if (rest == null) {
            return d.unsupported("malformed position");
        }
        d.type = Character.valueOf('_').equals(d.symbolCode) ? AprsPacket.Type.WEATHER : AprsPacket.Type.POSITION;
        afterPosition(d, rest);
        return d.build();
    }

    /**
     * Reads an uncompressed or compressed position at the start of {@code body} into {@code d}.
     *
     * @return the text after the position (and its compressed course/speed), or null when malformed
     */
    private static String positionBody(Draft d, String body) {
        if (body.isEmpty()) {
            return null;
        }
        char first = body.charAt(0);
        if (Character.isDigit(first) || first == ' ') {
            if (body.length() < 19) {
                return null;
            }
            Double lat = latitude(body.substring(0, 8));
            Double lon = longitude(body.substring(9, 18));
            if (lat == null || lon == null) {
                return null;
            }
            d.latitude = lat;
            d.longitude = lon;
            d.symbolTable = body.charAt(8);
            d.symbolCode = body.charAt(18);
            return body.substring(19);
        }
        return compressed(d, body);
    }

    /** The compressed position (APRS101 ch. 9) at the start of {@code body}, or null. */
    private static String compressed(Draft d, String body) {
        if (body.length() < 13) {
            return null;
        }
        char table = body.charAt(0);
        boolean overlay = (table >= 'A' && table <= 'Z') || (table >= 'a' && table <= 'j');
        if (table != '/' && table != '\\' && !overlay) {
            return null;
        }
        Long y = base91(body, 1, 4);
        Long x = base91(body, 5, 4);
        if (y == null || x == null) {
            return null;
        }
        double lat = 90.0 - y / 380926.0;
        double lon = -180.0 + x / 190463.0;
        if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
            return null;
        }
        d.latitude = round(lat, 6);
        d.longitude = round(lon, 6);
        d.symbolTable = table >= 'a' && table <= 'j' ? (char) ('0' + (table - 'a')) : table;
        d.symbolCode = body.charAt(9);
        char c = body.charAt(10);
        char s = body.charAt(11);
        char t = body.charAt(12);
        if (c != ' ' && c >= '!' && s >= '!') {
            int compressionType = t - 33;
            if (compressionType >= 0 && ((compressionType >> 3) & 0x03) == 2) {
                double feet = Math.pow(1.002, (c - 33) * 91 + (s - 33));
                d.altitudeM = round(feet * FEET_TO_M, 1);
            } else if (c - 33 >= 0 && c - 33 <= 89) {
                d.courseDeg = (double) ((c - 33) * 4);
                d.speedMps = round((Math.pow(1.08, s - 33) - 1.0) * KNOTS_TO_MPS, 2);
            }
        }
        return body.substring(13);
    }

    /** Course/speed, weather and the comment after a position. */
    private static void afterPosition(Draft d, String rest) {
        String text = rest;
        Matcher cs = COURSE_SPEED.matcher(text);
        if (d.type == AprsPacket.Type.WEATHER) {
            Map<String, Object> weather = new LinkedHashMap<>();
            if (cs.find()) {
                Integer dir = intOrNull(cs.group(1));
                Integer speed = intOrNull(cs.group(2));
                if (dir != null) {
                    weather.put("windDirDeg", dir);
                }
                if (speed != null) {
                    weather.put("windSpeedMs", round(speed * MPH_TO_MPS, 2));
                }
                text = text.substring(7);
            } else if (text.length() >= 7 && text.charAt(3) == '/' && blankValue(text.substring(0, 3))
                    && blankValue(text.substring(4, 7))) {
                text = text.substring(7);
            }
            text = weatherValues(text, weather);
            d.weather = weather;
        } else if (cs.find()) {
            int course = Integer.parseInt(cs.group(1));
            int speed = Integer.parseInt(cs.group(2));
            if (course >= 1 && course <= 360) {
                d.courseDeg = (double) (course % 360);
            }
            if (course != 0 || speed != 0) {
                d.speedMps = round(speed * KNOTS_TO_MPS, 2);
            }
            text = text.substring(7);
        } else if (text.length() >= 7 && (text.startsWith("PHG") || text.startsWith("RNG") || text.startsWith("DFS"))) {
            text = text.substring(7);
        }
        d.comment = comment(d, text);
    }

    /**
     * Reads the weather values ({@code g t r p P h b L l s #}) at the start of {@code text} into {@code weather}.
     *
     * @return the text after the values (usually a software id)
     */
    static String weatherValues(String text, Map<String, Object> weather) {
        int i = 0;
        while (i < text.length()) {
            char key = text.charAt(i);
            int width = switch (key) {
                case 'g', 't', 'r', 'p', 'P', 'L', 'l', 's', '#' -> 3;
                case 'b' -> 5;
                case 'h' -> 2;
                default -> -1;
            };
            if (width < 0 || i + 1 + width > text.length()) {
                break;
            }
            String value = text.substring(i + 1, i + 1 + width);
            if (key == 'h' && i + 3 < text.length() && Character.isDigit(text.charAt(i + 3))) {
                // Some stations send three humidity digits (h074); accept them when the value is a percentage.
                Integer three = intOrNull(text.substring(i + 1, i + 4));
                if (three != null && three <= 100) {
                    value = text.substring(i + 1, i + 4);
                    width = 3;
                }
            }
            if (!blankValue(value)) {
                Integer n = intOrNull(value.strip());
                if (n == null) {
                    break;
                }
                switch (key) {
                    case 'g' -> weather.put("windGustMs", round(n * MPH_TO_MPS, 2));
                    case 't' -> weather.put("tempC", round((n - 32) * 5.0 / 9.0, 1));
                    case 'r' -> weather.put("rainLastHourMm", round(n * 0.254, 2));
                    case 'p' -> weather.put("rain24hMm", round(n * 0.254, 2));
                    case 'P' -> weather.put("rainDailyMm", round(n * 0.254, 2));
                    case 'h' -> weather.put("rh", n == 0 ? 100 : n);
                    case 'b' -> weather.put("pressureHpa", round(n / 10.0, 1));
                    default -> {
                        // L, l (luminosity), s (snow) and # (raw rain counter) are read past but not kept.
                    }
                }
            }
            i += 1 + width;
        }
        return text.substring(i);
    }

    // ---------- objects, items, status, messages ----------

    private static AprsPacket object(Draft d) {
        String info = d.info;
        if (info.length() < 18) {
            return d.unsupported("short object");
        }
        char state = info.charAt(10);
        if (state != '*' && state != '_') {
            return d.unsupported("object without a live/killed flag");
        }
        String name = info.substring(1, 10).strip();
        if (name.isEmpty()) {
            return d.unsupported("object without a name");
        }
        d.type = AprsPacket.Type.OBJECT;
        d.objectName = name;
        d.killed = state == '_';
        String rest = positionBody(d, info.substring(18));
        if (rest == null) {
            if (d.killed) {
                return d.build();
            }
            return d.unsupported("object with a malformed position");
        }
        afterPosition(d, rest);
        return d.build();
    }

    private static AprsPacket item(Draft d) {
        String info = d.info;
        int end = -1;
        for (int i = 4; i <= Math.min(10, info.length() - 1); i++) {
            char c = info.charAt(i);
            if (c == '!' || c == '_') {
                end = i;
                break;
            }
        }
        if (end < 0) {
            return d.unsupported("item without a 3-9 character name and live/killed flag");
        }
        d.type = AprsPacket.Type.ITEM;
        d.objectName = info.substring(1, end).strip();
        d.killed = info.charAt(end) == '_';
        String rest = positionBody(d, info.substring(end + 1));
        if (rest == null) {
            if (d.killed) {
                return d.build();
            }
            return d.unsupported("item with a malformed position");
        }
        afterPosition(d, rest);
        return d.build();
    }

    private static AprsPacket status(Draft d) {
        String text = d.info.substring(1);
        if (STATUS_TIME.matcher(text).find()) {
            text = text.substring(7);
        }
        d.type = AprsPacket.Type.STATUS;
        d.comment = clean(text);
        return d.build();
    }

    private static AprsPacket message(Draft d) {
        String info = d.info;
        if (info.length() < 11 || info.charAt(10) != ':') {
            return d.unsupported("message without a 9-character addressee");
        }
        String addressee = info.substring(1, 10).strip();
        if (addressee.isEmpty()) {
            return d.unsupported("message without an addressee");
        }
        String text = info.substring(11);
        String id = null;
        int brace = text.lastIndexOf('{');
        if (brace >= 0 && text.length() - brace - 1 <= 8) {
            id = text.substring(brace + 1);
            int close = id.indexOf('}');
            if (close >= 0) {
                id = id.substring(0, close);
            }
            text = text.substring(0, brace);
            id = id.isBlank() ? null : id.strip();
        }
        boolean ack = text.matches("^(ack|rej)[A-Za-z0-9}]{1,8}$");
        if (ack) {
            id = text.substring(3);
        }
        d.type = AprsPacket.Type.MESSAGE;
        d.addressee = addressee;
        d.messageText = clean(text);
        d.messageId = id;
        d.ack = ack;
        return d.build();
    }

    // ---------- Mic-E ----------

    private static AprsPacket micE(Draft d) {
        String dest = d.destination == null ? "" : d.destination;
        int dash = dest.indexOf('-');
        if (dash >= 0) {
            dest = dest.substring(0, dash);
        }
        if (dest.length() != 6 || d.info.length() < 9) {
            return d.unsupported("Mic-E needs a 6-character destination and 8 information bytes");
        }
        int[] digits = new int[6];
        for (int i = 0; i < 6; i++) {
            int digit = micEDigit(dest.charAt(i));
            if (digit < 0) {
                return d.unsupported("Mic-E destination character '" + dest.charAt(i) + "' is not valid");
            }
            digits[i] = digit;
        }
        int latDeg = digits[0] * 10 + digits[1];
        double latMin = digits[2] * 10 + digits[3] + (digits[4] * 10 + digits[5]) / 100.0;
        if (latDeg > 90 || latMin >= 60) {
            return d.unsupported("Mic-E latitude out of range");
        }
        boolean north = upper(dest.charAt(3));
        boolean offset = upper(dest.charAt(4));
        boolean west = upper(dest.charAt(5));
        double lat = latDeg + latMin / 60.0;

        String info = d.info;
        int lonDeg = info.charAt(1) - 28;
        if (offset) {
            lonDeg += 100;
        }
        if (lonDeg >= 180 && lonDeg <= 189) {
            lonDeg -= 80;
        } else if (lonDeg >= 190 && lonDeg <= 199) {
            lonDeg -= 190;
        }
        int lonMin = info.charAt(2) - 28;
        if (lonMin >= 60) {
            lonMin -= 60;
        }
        int lonHundredths = info.charAt(3) - 28;
        if (lonDeg < 0 || lonDeg > 180 || lonMin < 0 || lonHundredths < 0 || lonHundredths > 99) {
            return d.unsupported("Mic-E longitude out of range");
        }
        double lon = lonDeg + (lonMin + lonHundredths / 100.0) / 60.0;
        if (lon > 180) {
            return d.unsupported("Mic-E longitude out of range");
        }
        int sp = info.charAt(4) - 28;
        int dc = info.charAt(5) - 28;
        int se = info.charAt(6) - 28;
        if (sp < 0 || dc < 0 || se < 0) {
            return d.unsupported("Mic-E speed/course bytes out of range");
        }
        int speed = sp * 10 + dc / 10;
        if (speed >= 800) {
            speed -= 800;
        }
        int course = (dc % 10) * 100 + se;
        if (course >= 400) {
            course -= 400;
        }

        d.type = AprsPacket.Type.MIC_E;
        d.latitude = round(north ? lat : -lat, 6);
        d.longitude = round(west ? -lon : lon, 6);
        d.symbolCode = info.charAt(7);
        d.symbolTable = info.charAt(8);
        if (course >= 1 && course <= 360) {
            d.courseDeg = (double) (course % 360);
        }
        if (course != 0 || speed != 0) {
            d.speedMps = round(speed * KNOTS_TO_MPS, 2);
        }
        d.micEMessage = micEMessage(dest);

        String rest = info.substring(9);
        int start = !rest.isEmpty() && " >]`'".indexOf(rest.charAt(0)) >= 0 ? 1 : 0;
        if (rest.length() >= start + 4 && rest.charAt(start + 3) == '}') {
            Long altitude = base91(rest, start, 3);
            if (altitude != null) {
                d.altitudeM = (double) (altitude - 10000);
                rest = rest.substring(start + 4);
                start = 0;
            }
        }
        d.comment = clean(rest.substring(Math.min(start, rest.length())));
        return d.build();
    }

    /** The digit a Mic-E destination character encodes (0 for an ambiguity space), or -1. */
    static int micEDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'J') {
            return c - 'A';
        }
        if (c >= 'P' && c <= 'Y') {
            return c - 'P';
        }
        if (c == 'K' || c == 'L' || c == 'Z') {
            return 0;
        }
        return -1;
    }

    /** True for the destination characters that set N, W or the +100 offset ({@code P}–{@code Z}). */
    private static boolean upper(char c) {
        return c >= 'P' && c <= 'Z';
    }

    /** The Mic-E message of the first three destination characters (APRS101 table "Mic-E Message Type"). */
    static String micEMessage(String dest) {
        int bits = 0;
        boolean standard = false;
        boolean custom = false;
        for (int i = 0; i < 3; i++) {
            char c = dest.charAt(i);
            bits <<= 1;
            if (c >= 'P' && c <= 'Z') {
                bits |= 1;
                standard = true;
            } else if (c >= 'A' && c <= 'K') {
                bits |= 1;
                custom = true;
            }
        }
        if (bits == 0) {
            return "Emergency";
        }
        if (standard && custom) {
            return "Unknown";
        }
        return custom ? "Custom-" + (7 - bits) : MIC_E_STANDARD[bits];
    }

    // ---------- helpers ----------

    /** {@code DDMM.hhN}, ambiguity spaces read as zero; null when malformed or out of range. */
    static Double latitude(String text) {
        String t = text.replace(' ', '0');
        char hemisphere = Character.toUpperCase(t.charAt(7));
        if (t.charAt(4) != '.' || (hemisphere != 'N' && hemisphere != 'S') || !digits(t, 0, 4) || !digits(t, 5, 7)) {
            return null;
        }
        int deg = Integer.parseInt(t.substring(0, 2));
        double min = Double.parseDouble(t.substring(2, 7));
        if (deg > 90 || min >= 60 || (deg == 90 && min > 0)) {
            return null;
        }
        double value = deg + min / 60.0;
        return round(hemisphere == 'S' ? -value : value, 6);
    }

    /** {@code DDDMM.hhW}, ambiguity spaces read as zero; null when malformed or out of range. */
    static Double longitude(String text) {
        String t = text.replace(' ', '0');
        char hemisphere = Character.toUpperCase(t.charAt(8));
        if (t.charAt(5) != '.' || (hemisphere != 'E' && hemisphere != 'W') || !digits(t, 0, 5) || !digits(t, 6, 8)) {
            return null;
        }
        int deg = Integer.parseInt(t.substring(0, 3));
        double min = Double.parseDouble(t.substring(3, 8));
        if (deg > 180 || min >= 60 || (deg == 180 && min > 0)) {
            return null;
        }
        double value = deg + min / 60.0;
        return round(hemisphere == 'W' ? -value : value, 6);
    }

    /** The comment after removing the {@code /A=} altitude (read into {@code d}), cleaned. */
    private static String comment(Draft d, String text) {
        Matcher altitude = ALTITUDE.matcher(text);
        String rest = text;
        if (altitude.find()) {
            if (d.altitudeM == null) {
                d.altitudeM = round(Long.parseLong(altitude.group(1)) * FEET_TO_M, 1);
            }
            rest = text.substring(0, altitude.start()) + text.substring(altitude.end());
        }
        return clean(rest);
    }

    /**
     * Free text with control characters removed, whitespace trimmed and at most {@value #MAX_COMMENT} characters, or
     * null when nothing is left.
     */
    static String clean(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(text.length());
        text.codePoints().filter(cp -> !Character.isISOControl(cp) && cp != 0xFFFD).forEach(out::appendCodePoint);
        String value = out.toString().strip();
        if (value.isEmpty()) {
            return null;
        }
        if (value.codePointCount(0, value.length()) > MAX_COMMENT) {
            value = value.substring(0, value.offsetByCodePoints(0, MAX_COMMENT)).strip();
        }
        return value;
    }

    /** The base-91 value of {@code length} characters at {@code offset}, or null when a character is out of range. */
    static Long base91(String text, int offset, int length) {
        if (offset + length > text.length()) {
            return null;
        }
        long value = 0;
        for (int i = offset; i < offset + length; i++) {
            int c = text.charAt(i) - 33;
            if (c < 0 || c > 90) {
                return null;
            }
            value = value * 91 + c;
        }
        return value;
    }

    private static boolean digits(String text, int from, int to) {
        for (int i = from; i < to; i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean blankValue(String value) {
        return value.chars().allMatch(c -> c == '.' || c == ' ');
    }

    private static Integer intOrNull(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static double round(double value, int places) {
        double rounded = BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }

    private static String stripTerminator(String line) {
        int end = line.length();
        while (end > 0 && (line.charAt(end - 1) == '\n' || line.charAt(end - 1) == '\r')) {
            end--;
        }
        return line.substring(0, end);
    }

    private static String printable(char c) {
        return c < 0x20 || c > 0x7e ? String.format("0x%02x", (int) c) : String.valueOf(c);
    }

    /** The packet being built. */
    private static final class Draft {
        String raw;
        String source;
        String destination;
        List<String> path = List.of();
        String qConstruct;
        String igate;
        AprsPacket.Type type;
        String info;
        Double latitude;
        Double longitude;
        Character symbolTable;
        Character symbolCode;
        Double courseDeg;
        Double speedMps;
        Double altitudeM;
        String comment;
        String objectName;
        boolean killed;
        Map<String, Object> weather;
        String addressee;
        String messageText;
        String messageId;
        boolean ack;
        String micEMessage;

        AprsPacket build() {
            return new AprsPacket(raw, source, destination, path, qConstruct, igate, type, info, latitude, longitude,
                    symbolTable, symbolCode, courseDeg, speedMps, altitudeM, comment, objectName, killed, weather,
                    addressee, messageText, messageId, ack, micEMessage, null);
        }

        AprsPacket unsupported(String why) {
            return new AprsPacket(raw, source, destination, path, qConstruct, igate, AprsPacket.Type.UNSUPPORTED, info,
                    null, null, null, null, null, null, null, null, null, false, null, null, null, null, false, null,
                    why);
        }
    }
}
