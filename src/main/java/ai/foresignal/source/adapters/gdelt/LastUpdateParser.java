package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Parses GDELT's {@code gdeltv2/lastupdate.txt} (prompt 066; pure).
 *
 * <p>The file has three lines of {@code <size> <md5> <url>}, for the newest {@code export}, {@code mentions} and
 * {@code gkg} files (verified 2026-10 on both {@code data.gdeltproject.org} and the {@code storage.googleapis.com}
 * mirror). The URLs point at {@code http://data.gdeltproject.org/…} even when read from the mirror, so only the
 * <b>file name</b> is ever taken from a line: the host, scheme and path of the URL are ignored, and the adapter
 * rebuilds the request URL itself. A URL is never requested verbatim.
 */
public final class LastUpdateParser {

    /** The export file name: a 14-digit UTC timestamp and {@code .export.CSV.zip}. */
    public static final Pattern EXPORT_FILE = Pattern.compile("^\\d{14}\\.export\\.CSV\\.zip$");

    /** The largest declared export size accepted (50 MB). */
    public static final long MAX_DECLARED_BYTES = 50L * 1024 * 1024;

    private static final Pattern MD5 = Pattern.compile("^[0-9a-fA-F]{32}$");
    private static final Pattern SIZE = Pattern.compile("^\\d{1,12}$");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

    private LastUpdateParser() {}

    /**
     * One line of the file.
     *
     * @param size the declared size in bytes
     * @param md5 the declared MD5, lowercase hex
     * @param fileName the last path segment of the line's URL; the rest of the URL is discarded
     */
    public record Line(long size, String md5, String fileName) {}

    /**
     * The newest export file, validated.
     *
     * @param fileName the file name, matching {@link #EXPORT_FILE}
     * @param size the declared size in bytes, 1 to {@link #MAX_DECLARED_BYTES}
     * @param md5 the declared MD5, lowercase hex
     * @param timestamp the file's timestamp (UTC)
     */
    public record ExportFile(String fileName, long size, String md5, Instant timestamp) {

        /** The name of the single entry the ZIP must hold: the file name without {@code .zip}. */
        public String entryName() {
            return fileName.substring(0, fileName.length() - ".zip".length());
        }
    }

    /**
     * Every well-formed line of the file.
     *
     * @param text the body of {@code lastupdate.txt}
     * @return the lines, in order
     * @throws UpstreamFormatException when a non-blank line is not {@code <size> <md5> <url>}
     */
    public static List<Line> parse(String text) {
        List<Line> lines = new ArrayList<>(3);
        if (text == null) {
            throw new UpstreamFormatException("GDELT lastupdate.txt is empty", "");
        }
        for (String raw : text.split("\n")) {
            String line = raw.strip();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = WHITESPACE.split(line);
            if (parts.length != 3 || !SIZE.matcher(parts[0]).matches() || !MD5.matcher(parts[1]).matches()) {
                throw new UpstreamFormatException("GDELT lastupdate.txt has a line that is not <size> <md5> <url>",
                        text);
            }
            lines.add(new Line(Long.parseLong(parts[0]), parts[1].toLowerCase(Locale.ROOT), fileName(parts[2])));
        }
        if (lines.isEmpty()) {
            throw new UpstreamFormatException("GDELT lastupdate.txt is empty", text);
        }
        return List.copyOf(lines);
    }

    /**
     * The export line of the file, validated: the file name matches {@link #EXPORT_FILE} and holds a real UTC
     * timestamp, and the declared size is 1 byte to 50 MB.
     *
     * @param text the body of {@code lastupdate.txt}
     * @return the export file
     * @throws UpstreamFormatException when there is no valid export line, or its declared size is out of range
     */
    public static ExportFile export(String text) {
        for (Line line : parse(text)) {
            if (!line.fileName().endsWith(".export.CSV.zip")) {
                continue;
            }
            if (!EXPORT_FILE.matcher(line.fileName()).matches()) {
                throw new UpstreamFormatException("GDELT lastupdate.txt names an export file that does not match"
                        + " {14 digits}.export.CSV.zip", line.fileName());
            }
            if (line.size() <= 0 || line.size() > MAX_DECLARED_BYTES) {
                throw new UpstreamFormatException("GDELT lastupdate.txt declares an export of " + line.size()
                        + " bytes; at most " + MAX_DECLARED_BYTES + " are accepted", text);
            }
            return new ExportFile(line.fileName(), line.size(), line.md5(), timestamp(line.fileName()));
        }
        throw new UpstreamFormatException("GDELT lastupdate.txt has no export line", text);
    }

    /**
     * The timestamp of an export file name.
     *
     * @param fileName a name matching {@link #EXPORT_FILE}
     * @return its UTC timestamp
     * @throws UpstreamFormatException when the 14 digits are not a real date and time
     */
    public static Instant timestamp(String fileName) {
        if (fileName == null || fileName.length() < 14) {
            throw new UpstreamFormatException("Not a GDELT export file name", fileName);
        }
        try {
            return LocalDateTime.parse(fileName.substring(0, 14), TIMESTAMP).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new UpstreamFormatException("GDELT export file name has an invalid timestamp", fileName, e);
        }
    }

    /** The last path segment of {@code url}; any query or fragment stays in it, so the name check refuses it. */
    static String fileName(String url) {
        int slash = url.lastIndexOf('/');
        return slash < 0 ? url : url.substring(slash + 1);
    }
}
