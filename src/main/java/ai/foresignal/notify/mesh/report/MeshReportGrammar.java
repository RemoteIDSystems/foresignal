package ai.foresignal.notify.mesh.report;

import ai.foresignal.notify.mesh.MeshShortCode;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The field-report grammar (prompt 084): one command per message, case-insensitive ASCII keywords, after the
 * {@code #E } prefix.
 *
 * <ul>
 *   <li>{@code #E R <TYPE> [<lat>,<lon> | @<place>] <text>}: a report; TYPE is one of {@link #TYPES}; the place is a
 *       place id (prompt 036); the text (at most {@value #MAX_TEXT} characters kept) is required.
 *   <li>{@code #E ACK <code>}: acknowledges the alert behind a delivery's short code.
 *   <li>{@code #E STATUS}: a short summary.
 *   <li>{@code #E HELP}: the grammar card.
 * </ul>
 *
 * <p>A text without the prefix is not a command ({@link #parse} returns null). A prefixed text that does not parse
 * is an {@link Invalid} command, answered {@code ERR {reason}}. Pure.
 */
public final class MeshReportGrammar {

    /** The command prefix. */
    public static final String PREFIX = "#E ";

    /** The report types. */
    public static final List<String> TYPES = List.of("FIRE", "SMOKE", "FLOOD", "POWER", "ROAD", "MED", "WX", "SEC",
            "OTHER");

    /** The most report text characters kept. */
    public static final int MAX_TEXT = 200;

    /** The grammar card, as {@code #E HELP} answers it (at most 133 characters). */
    public static final String HELP = "#E R TYPE [lat,lon|@place] text; #E ACK code; #E STATUS. TYPE: FIRE SMOKE FLOOD"
            + " POWER ROAD MED WX SEC OTHER";

    private static final Pattern COORDINATES = Pattern.compile("^(-?\\d{1,3}(?:\\.\\d+)?),(-?\\d{1,3}(?:\\.\\d+)?)$");
    private static final Pattern PLACE = Pattern.compile("^@([A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)$");

    private MeshReportGrammar() {}

    /** One parsed command. */
    public sealed interface Command permits Report, Ack, Status, Help, Invalid {}

    /**
     * {@code #E R}: a field report.
     *
     * @param type one of {@link #TYPES}
     * @param latitude the latitude, or null
     * @param longitude the longitude, or null
     * @param place the place id (lower case), or null
     * @param text the report text, at most {@value #MAX_TEXT} characters
     */
    public record Report(String type, Double latitude, Double longitude, String place, String text)
            implements Command {}

    /**
     * {@code #E ACK}: acknowledge an alert.
     *
     * @param code the normalized short code
     */
    public record Ack(String code) implements Command {}

    /** {@code #E STATUS}. */
    public record Status() implements Command {}

    /** {@code #E HELP}. */
    public record Help() implements Command {}

    /**
     * A prefixed text that does not parse.
     *
     * @param reason the {@code ERR} reason
     */
    public record Invalid(String reason) implements Command {}

    /**
     * The command in {@code text}, or null when it does not start with {@value #PREFIX} (case-insensitive).
     */
    public static Command parse(String text) {
        if (text == null) {
            return null;
        }
        String t = text.strip();
        if (t.length() < PREFIX.length() || !t.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            return null;
        }
        String rest = t.substring(PREFIX.length()).strip();
        String[] words = rest.split("\\s+", 2);
        String verb = words[0].toUpperCase(Locale.ROOT);
        String args = words.length > 1 ? words[1].strip() : "";
        return switch (verb) {
            case "R" -> report(args);
            case "ACK" -> {
                String code = MeshShortCode.normalize(args);
                yield code == null ? new Invalid("bad code") : new Ack(code);
            }
            case "STATUS" -> args.isEmpty() ? new Status() : new Invalid("STATUS takes no arguments");
            case "HELP" -> new Help();
            default -> new Invalid("unknown command; send #E HELP");
        };
    }

    private static Command report(String args) {
        if (args.isEmpty()) {
            return new Invalid("report type missing");
        }
        String[] words = args.split("\\s+", 2);
        String type = words[0].toUpperCase(Locale.ROOT);
        if (!TYPES.contains(type)) {
            return new Invalid("unknown type " + ascii(words[0]));
        }
        String rest = words.length > 1 ? words[1].strip() : "";
        Double lat = null;
        Double lon = null;
        String place = null;
        if (!rest.isEmpty()) {
            String[] parts = rest.split("\\s+", 2);
            String first = parts[0];
            Matcher coordinates = COORDINATES.matcher(first);
            Matcher named = PLACE.matcher(first);
            if (coordinates.matches()) {
                lat = Double.parseDouble(coordinates.group(1));
                lon = Double.parseDouble(coordinates.group(2));
                if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    return new Invalid("bad coordinates");
                }
                rest = parts.length > 1 ? parts[1].strip() : "";
            } else if (named.matches()) {
                place = named.group(1).toLowerCase(Locale.ROOT);
                rest = parts.length > 1 ? parts[1].strip() : "";
            } else if (looksLikeCoordinates(first)) {
                return new Invalid("bad coordinates");
            } else if (first.startsWith("@")) {
                return new Invalid("bad place");
            }
        }
        if (rest.isEmpty()) {
            return new Invalid("report text missing");
        }
        String text = rest.length() > MAX_TEXT ? rest.substring(0, Character.isHighSurrogate(rest.charAt(MAX_TEXT - 1))
                ? MAX_TEXT - 1 : MAX_TEXT) : rest;
        return new Report(type, lat, lon, place, text);
    }

    /** A token with a comma between characters that start like numbers. */
    static boolean looksLikeCoordinates(String token) {
        int comma = token.indexOf(',');
        if (comma <= 0 || comma == token.length() - 1) {
            return false;
        }
        char head = token.charAt(0);
        return Character.isDigit(head) || head == '-' || head == '+' || head == '.';
    }

    private static String ascii(String word) {
        String cut = word.length() > 12 ? word.substring(0, 12) : word;
        return cut.replaceAll("[^\\x21-\\x7E]", "?");
    }
}
