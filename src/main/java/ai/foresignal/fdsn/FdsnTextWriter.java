package ai.foresignal.fdsn;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes the fdsnws-event pipe-separated text format (prompt 141): the header line
 * {@value #HEADER} and one line per event.
 *
 * <p>Columns as the major FDSN services fill them: {@code EventID} and {@code ContributorID} are the external id,
 * {@code Time} is UTC with milliseconds and no zone designator, {@code Depth/km} the depth in km, {@code Author} and
 * {@code MagAuthor} the reporting agency, {@code Catalog} the record's catalog, {@code Contributor} the Foresignal
 * source id. An unknown value is empty. A {@code |} or line break inside a value becomes a space, so a value can never
 * split a line or a column.
 */
public final class FdsnTextWriter {

    /** The header line. */
    public static final String HEADER = "#EventID|Time|Latitude|Longitude|Depth/km|Author|Catalog|Contributor"
            + "|ContributorID|MagType|Magnitude|MagAuthor|EventLocationName";

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    private FdsnTextWriter() {}

    /** The text document of {@code events}, each line ending in {@code \n}. */
    public static String write(List<FdsnEvent> events) {
        StringBuilder out = new StringBuilder(HEADER).append('\n');
        for (FdsnEvent e : events) {
            out.append(clean(e.externalId())).append('|')
                    .append(time(e.time())).append('|')
                    .append(number(e.latitude())).append('|')
                    .append(number(e.longitude())).append('|')
                    .append(e.depthKm() == null ? "" : number(e.depthKm())).append('|')
                    .append(clean(e.author())).append('|')
                    .append(clean(e.catalog())).append('|')
                    .append(clean(e.sourceId())).append('|')
                    .append(clean(e.externalId())).append('|')
                    .append(clean(e.magnitudeType())).append('|')
                    .append(e.magnitude() == null ? "" : number(e.magnitude())).append('|')
                    .append(clean(e.author())).append('|')
                    .append(clean(e.place())).append('\n');
        }
        return out.toString();
    }

    /** UTC {@code yyyy-MM-ddTHH:mm:ss.SSS}. */
    static String time(Instant instant) {
        return TIME.format(instant);
    }

    /** A plain decimal without an exponent or trailing zeros ({@code 10}, {@code -8.1449}). */
    static String number(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        return decimal.scale() < 0 ? decimal.setScale(0).toPlainString() : decimal.toPlainString();
    }

    private static String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('|', ' ').replace('\n', ' ').replace('\r', ' ');
    }
}
