package ai.foresignal.codec.ais;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An IEC 61162-450 TAG block (prompt 085): {@code \k:v,k:v*hh\} in front of an NMEA sentence, as Kystverket's
 * stream sends it ({@code \s:2573485,c:1700000000*hh\!BSVDM,…}).
 *
 * <p>Parameters used here: {@code c} the time (Unix seconds, or milliseconds when the value has more than 11 digits),
 * {@code s} the source station, {@code g} the sentence group {@code n-m-id} and {@code n} the line count. Every
 * parameter is kept in {@link #fields()}.
 *
 * @param fields every parameter in order, by its one-letter code
 * @param time the {@code c} time, or null
 * @param source the {@code s} station, or null
 * @param group the {@code g} group, or null
 * @param lineCount the {@code n} line count, or null
 */
public record TagBlock(Map<String, String> fields, Instant time, String source, Group group, Long lineCount) {

    /** Unix times above this many seconds are read as milliseconds (year 5138). */
    static final long MILLIS_THRESHOLD = 99_999_999_999L;

    /** Copies the fields. */
    public TagBlock {
        fields = fields == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }

    /**
     * A sentence group: this sentence is number {@code number} of {@code count}, all sharing {@code id}.
     *
     * @param number the sentence's number, from 1
     * @param count the sentences in the group
     * @param id the group id
     */
    public record Group(int number, int count, String id) {}

    /**
     * A line with its TAG block split off.
     *
     * @param tag the TAG block, or null when the line has none
     * @param sentence the rest of the line (the NMEA sentence)
     */
    public record Split(TagBlock tag, String sentence) {}

    /** Why a TAG block could not be used; the line is then dropped and counted. */
    public static final class TagBlockException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        /** True when the block parsed but its checksum did not match. */
        private final boolean badChecksum;

        TagBlockException(String message, boolean badChecksum) {
            super(message);
            this.badChecksum = badChecksum;
        }

        /** True when the checksum did not match (as opposed to a malformed block). */
        public boolean badChecksum() {
            return badChecksum;
        }
    }

    /**
     * Splits a TAG block off the front of {@code line}. A line without one is returned whole with a null tag; that is
     * not an error.
     *
     * @throws TagBlockException when the block is unterminated, has no checksum, a wrong checksum or a malformed
     *     parameter
     */
    public static Split strip(String line) {
        if (line == null || line.isEmpty() || line.charAt(0) != '\\') {
            return new Split(null, line == null ? "" : line);
        }
        int end = line.indexOf('\\', 1);
        if (end < 0) {
            throw new TagBlockException("unterminated TAG block", false);
        }
        TagBlock tag = parse(line.substring(1, end));
        return new Split(tag, line.substring(end + 1));
    }

    /**
     * Parses the inside of a TAG block, {@code k:v,k:v*hh}.
     *
     * @throws TagBlockException on a missing or wrong checksum or a malformed parameter
     */
    public static TagBlock parse(String body) {
        int star = body.lastIndexOf('*');
        if (star < 0 || star + 3 != body.length()) {
            throw new TagBlockException("TAG block without a checksum", false);
        }
        String content = body.substring(0, star);
        int expected;
        try {
            expected = Integer.parseInt(body.substring(star + 1), 16);
        } catch (NumberFormatException e) {
            throw new TagBlockException("TAG block checksum is not hexadecimal", false);
        }
        if (NmeaSentence.checksum(content) != expected) {
            throw new TagBlockException("TAG block checksum mismatch", true);
        }
        Map<String, String> fields = new LinkedHashMap<>();
        if (!content.isEmpty()) {
            for (String part : content.split(",")) {
                int colon = part.indexOf(':');
                if (colon <= 0) {
                    throw new TagBlockException("malformed TAG parameter '" + part + "'", false);
                }
                fields.put(part.substring(0, colon), part.substring(colon + 1));
            }
        }
        return new TagBlock(fields, time(fields.get("c")), blankToNull(fields.get("s")), group(fields.get("g")),
                number(fields.get("n")));
    }

    /** The TAG block as text, {@code \k:v,…*hh\}, with a computed checksum (tests, fixtures). */
    public static String format(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        fields.forEach((k, v) -> {
            if (!sb.isEmpty()) {
                sb.append(',');
            }
            sb.append(k).append(':').append(v);
        });
        String content = sb.toString();
        return "\\" + content + "*" + String.format("%02X", NmeaSentence.checksum(content)) + "\\";
    }

    private static Instant time(String value) {
        Long n = number(value);
        if (n == null || n < 0) {
            return null;
        }
        return n > MILLIS_THRESHOLD ? Instant.ofEpochMilli(n) : Instant.ofEpochSecond(n);
    }

    private static Group group(String value) {
        if (value == null) {
            return null;
        }
        String[] parts = value.split("-");
        if (parts.length != 3) {
            throw new TagBlockException("malformed TAG group '" + value + "'", false);
        }
        try {
            return new Group(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2]);
        } catch (NumberFormatException e) {
            throw new TagBlockException("malformed TAG group '" + value + "'", false);
        }
    }

    private static Long number(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.strip());
        } catch (NumberFormatException e) {
            throw new TagBlockException("malformed TAG number '" + value + "'", false);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
