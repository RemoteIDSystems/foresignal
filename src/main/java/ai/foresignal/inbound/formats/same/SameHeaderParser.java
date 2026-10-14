package ai.foresignal.inbound.formats.same;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses one Specific Area Message Encoding header (prompt 086; NWS, https://www.weather.gov/nwr/sameinfo; 47 CFR
 * 11.31), pure.
 *
 * <p>{@code ZCZC-ORG-EEE-PSSCCC(-PSSCCC)*+TTTT-JJJHHMM-LLLLLLLL-}:
 * <ul>
 *   <li>{@code ORG} originator ({@code EAS}, {@code CIV}, {@code WXR}, {@code PEP}); {@code EEE} event code;
 *   <li>up to 31 locations {@code PSSCCC}: {@code P} the part of the county (0 whole), {@code SS} the state FIPS,
 *       {@code CCC} the county FIPS ({@code 000} the whole state);
 *   <li>{@code TTTT} the purge time as hours and minutes after issue;
 *   <li>{@code JJJHHMM} the issue time: day of the year and UTC hour and minute, without a year. The year comes from
 *       the receive clock: of the previous, current and next year, the one that puts the issue nearest the receive
 *       time (so a header issued on 31 December and received on 1 January keeps its year);
 *   <li>{@code LLLLLLLL} the sender ({@code KEAX/NWS}).
 * </ul>
 * A line may carry multimon-ng's {@code EAS: } prefix. {@code NNNN} (the end-of-message marker) and anything else is
 * not a header.
 */
public final class SameHeaderParser {

    /** The end-of-message marker. */
    public static final String END_OF_MESSAGE = "NNNN";

    /** The most locations a header carries. */
    public static final int MAX_LOCATIONS = 31;

    static final Pattern HEADER = Pattern.compile(
            "ZCZC-([A-Z]{3})-([A-Z0-9]{3})-(\\d{6}(?:-\\d{6}){0,30})\\+(\\d{2})(\\d{2})-(\\d{3})(\\d{2})(\\d{2})-([^-\\s]{1,8})-?");

    /**
     * One parsed header.
     *
     * @param org the originator
     * @param eventCode the event code
     * @param locations the {@code PSSCCC} codes as sent, in order, duplicates removed
     * @param purge the purge time
     * @param julianDay the day of the year, 1 to 366
     * @param hour the UTC hour
     * @param minute the UTC minute
     * @param sender the sender
     * @param rawHeader the header from {@code ZCZC} to the end of the sender, with its trailing dash
     */
    public record Header(String org, String eventCode, List<String> locations, Duration purge, int julianDay, int hour,
            int minute, String sender, String rawHeader) {

        /** Copies the locations. */
        public Header {
            locations = List.copyOf(locations);
        }

        /**
         * The issue time, its year chosen from the receive clock.
         *
         * @param receivedAt when the header was received
         * @return the issue instant; empty when the day does not exist in any candidate year (day 366 outside leap years)
         */
        public Optional<Instant> issue(Instant receivedAt) {
            int year = receivedAt.atZone(ZoneOffset.UTC).getYear();
            Instant best = null;
            for (int y = year - 1; y <= year + 1; y++) {
                if (julianDay > LocalDate.of(y, 1, 1).lengthOfYear()) {
                    continue;
                }
                Instant candidate = LocalDate.ofYearDay(y, julianDay).atTime(hour, minute).toInstant(ZoneOffset.UTC);
                if (best == null || Duration.between(candidate, receivedAt).abs()
                        .compareTo(Duration.between(best, receivedAt).abs()) < 0) {
                    best = candidate;
                }
            }
            return Optional.ofNullable(best);
        }

        /** The five-digit county FIPS codes ({@code SSCCC}) of the locations that name a county. */
        public List<String> countyFips() {
            List<String> out = new ArrayList<>();
            for (String l : locations) {
                if (!l.endsWith("000") && !out.contains(l.substring(1))) {
                    out.add(l.substring(1));
                }
            }
            return out;
        }

        /** The two-digit state FIPS codes of the locations that name a whole state ({@code CCC} 000). */
        public List<String> wholeStates() {
            List<String> out = new ArrayList<>();
            for (String l : locations) {
                if (l.endsWith("000") && !out.contains(l.substring(1, 3))) {
                    out.add(l.substring(1, 3));
                }
            }
            return out;
        }
    }

    private SameHeaderParser() {}

    /** The header in {@code line}, or empty for {@code NNNN}, blank lines and anything that is not a valid header. */
    public static Optional<Header> parse(String line) {
        if (line == null) {
            return Optional.empty();
        }
        String text = line.strip();
        if (text.startsWith("EAS:")) {
            text = text.substring(4).strip();
        }
        if (text.isEmpty() || text.startsWith(END_OF_MESSAGE)) {
            return Optional.empty();
        }
        Matcher m = HEADER.matcher(text);
        if (!m.find()) {
            return Optional.empty();
        }
        int purgeHours = Integer.parseInt(m.group(4));
        int purgeMinutes = Integer.parseInt(m.group(5));
        int day = Integer.parseInt(m.group(6));
        int hour = Integer.parseInt(m.group(7));
        int minute = Integer.parseInt(m.group(8));
        if (purgeMinutes > 59 || day < 1 || day > 366 || hour > 23 || minute > 59) {
            return Optional.empty();
        }
        List<String> locations = new ArrayList<>();
        for (String l : m.group(3).split("-")) {
            if (!locations.contains(l)) {
                locations.add(l);
            }
        }
        String raw = m.group(0).endsWith("-") ? m.group(0) : m.group(0) + "-";
        return Optional.of(new Header(m.group(1), m.group(2), locations,
                Duration.ofHours(purgeHours).plusMinutes(purgeMinutes), day, hour, minute, m.group(9), raw));
    }

    /** True when {@code line} is the end-of-message marker (with or without multimon-ng's prefix). */
    public static boolean isEndOfMessage(String line) {
        if (line == null) {
            return false;
        }
        String text = line.strip();
        if (text.startsWith("EAS:")) {
            text = text.substring(4).strip();
        }
        return text.startsWith(END_OF_MESSAGE);
    }
}
