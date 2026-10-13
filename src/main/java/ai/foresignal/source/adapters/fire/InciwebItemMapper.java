package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedItem;
import ai.foresignal.source.kit.feed.FeedItemMapper;
import ai.foresignal.source.kit.feed.FeedSourceSpec;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the structured text of an InciWeb RSS item (prompt 075): the reviewed {@link FeedItemMapper} named
 * {@value #NAME}. Pure; the description is untrusted upstream text and only ever read as data.
 *
 * <p>The feed kit has already stripped the HTML and collapsed whitespace, so the description reads as one line:
 * {@code Last updated: 2026-10-13 --- The type of incident is Wildfire and involves the following unit(s)
 * Okanogan-Wenatchee National Forest. --- State: Washington --- Coordinates: Latitude: 48° 20 48 Longitude:
 * 120° 49 50 --- NOTE: … --- Incident Overview: …}.
 *
 * <ul>
 *   <li><strong>Fields:</strong> {@code lastUpdated} (the date string), {@code incidentType}, {@code state} and
 *       {@code overview} (the {@code Incident Overview:} paragraph, plain text, at most {@value #MAX_OVERVIEW_BYTES}
 *       bytes). The involved units are read but not stored: the prompt's payload has no field for them.
 *   <li><strong>Title:</strong> a leading unit code ({@code NVTMFX Bull Fire}) is split into {@code unitCode} and
 *       {@code name}; a title without one is the name.
 *   <li><strong>Coordinates:</strong> degrees with a degree sign, then minutes and seconds without symbols, no
 *       hemisphere ({@code Latitude: 39° 32 21  Longitude: 119° 59 16}). Minutes and seconds must be below 60.
 *       InciWeb covers US units, so the latitude is north and the longitude west (negated); the only exception is
 *       the Guam and Northern Mariana Islands box and the far Aleutians, which lie east of 180°. A position
 *       outside {@link #inUsEnvelope the US envelope} (or with minutes or seconds of 60 or more) is
 *       {@linkplain Coordinates#rejected() rejected}: the record has no geometry and the adapter counts it.
 * </ul>
 */
public final class InciwebItemMapper implements FeedItemMapper {

    /** The name a feed spec uses. */
    public static final String NAME = "inciweb";

    /** The record kind. */
    public static final String KIND = "fire-incident";

    /** The largest overview kept, in UTF-8 bytes. */
    public static final int MAX_OVERVIEW_BYTES = 2048;

    /** The separator InciWeb puts between sections. */
    private static final String SECTION = "(?=\\s*---|$)";

    static final Pattern LAST_UPDATED = Pattern.compile("Last updated:\\s*(\\d{4}-\\d{2}-\\d{2})");
    static final Pattern TYPE = Pattern.compile("The type of incident is\\s+(.+?)\\s+and involves the following"
            + " unit\\(s\\)\\s*(.*?)\\.?" + SECTION);
    static final Pattern STATE = Pattern.compile("State:\\s*(.+?)" + SECTION);
    static final Pattern COORDINATES = Pattern.compile("Latitude:\\s*(\\d{1,3})\\s*°\\s*(\\d{1,3})\\s+(\\d+(?:\\.\\d+)?)"
            + "\\s+Longitude:\\s*(\\d{1,3})\\s*°\\s*(\\d{1,3})\\s+(\\d+(?:\\.\\d+)?)");
    static final Pattern OVERVIEW = Pattern.compile("Incident Overview:\\s*(.*)$", Pattern.DOTALL);
    static final Pattern UNIT_TITLE = Pattern.compile("^([A-Z]{2}[A-Z0-9]{3,6})\\s+(.+)$");

    /**
     * A parsed position.
     *
     * @param latitude degrees north, or null
     * @param longitude degrees east, or null
     * @param present true when the description holds a coordinate line
     * @param rejected true when it was present but invalid or outside the US envelope
     */
    public record Coordinates(Double latitude, Double longitude, boolean present, boolean rejected) {

        /** No coordinate line. */
        static final Coordinates NONE = new Coordinates(null, null, false, false);

        /** A coordinate line that was refused. */
        static final Coordinates REJECTED = new Coordinates(null, null, true, true);

        /** True when a position was accepted. */
        public boolean accepted() {
            return latitude != null;
        }
    }

    /**
     * The fields of one description.
     *
     * @param lastUpdated the {@code Last updated} date string, or null
     * @param incidentType the incident type, or null
     * @param units the involved units, or null
     * @param state the state, or null
     * @param overview the overview, at most {@value #MAX_OVERVIEW_BYTES} bytes, or null
     * @param coordinates the position
     */
    public record Parsed(String lastUpdated, String incidentType, String units, String state, String overview,
            Coordinates coordinates) {}

    @Override
    public String name() {
        return NAME;
    }

    /**
     * Builds the {@code fire-incident} record from the kit's standard record: same id, times and link; the payload
     * replaced by {@code name, unitCode, incidentType, state, lastUpdated, overview, link}; the geometry from the
     * description's coordinates.
     */
    @Override
    public NormalizedRecord map(FeedItem item, FeedDocument document, FeedSourceSpec spec, NormalizedRecord standard) {
        Parsed parsed = parse(item.summary());
        String[] title = splitTitle(item.title());
        NormalizedRecord.Builder b = NormalizedRecord.builder(standard.externalId(), KIND)
                .observedAt(standard.observedAt())
                .eventTime(standard.eventTime())
                .put("name", title[1])
                .put("unitCode", title[0])
                .put("incidentType", parsed.incidentType())
                .put("state", parsed.state())
                .put("lastUpdated", parsed.lastUpdated())
                .put("overview", parsed.overview())
                .put("link", item.link());
        if (parsed.coordinates().accepted()) {
            b.geometry(Geo.point(parsed.coordinates().longitude(), parsed.coordinates().latitude()));
        }
        return b.build();
    }

    /**
     * Parses a description (see the class note).
     *
     * @param summary the item's plain-text description, or null
     */
    public static Parsed parse(String summary) {
        if (summary == null || summary.isBlank()) {
            return new Parsed(null, null, null, null, null, Coordinates.NONE);
        }
        String text = summary.strip();
        String type = null;
        String units = null;
        Matcher t = TYPE.matcher(text);
        if (t.find()) {
            type = blankToNull(t.group(1));
            units = blankToNull(t.group(2));
        }
        return new Parsed(group(LAST_UPDATED, text), type, units, group(STATE, text), overview(text),
                coordinates(text));
    }

    /**
     * The position in {@code text}.
     *
     * @param text the description
     */
    public static Coordinates coordinates(String text) {
        if (text == null) {
            return Coordinates.NONE;
        }
        Matcher m = COORDINATES.matcher(text);
        if (!m.find()) {
            return Coordinates.NONE;
        }
        Double lat = dms(m.group(1), m.group(2), m.group(3));
        Double lon = dms(m.group(4), m.group(5), m.group(6));
        if (lat == null || lon == null || lat > 90 || lon > 180) {
            return Coordinates.REJECTED;
        }
        if (inUsEnvelope(lat, -lon)) {
            return new Coordinates(lat, -lon, true, false);
        }
        if (inEastEnvelope(lat, lon)) {
            return new Coordinates(lat, lon, true, false);
        }
        return Coordinates.REJECTED;
    }

    /** Degrees, minutes and seconds as decimal degrees, or null when minutes or seconds are 60 or more. */
    static Double dms(String degrees, String minutes, String seconds) {
        try {
            double d = Double.parseDouble(degrees);
            double m = Double.parseDouble(minutes);
            double s = Double.parseDouble(seconds);
            if (m >= 60 || s >= 60) {
                return null;
            }
            return d + m / 60.0 + s / 3600.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * True when the position (longitude east-positive) lies in the contiguous US, Alaska west of 180°, Hawaii, or
     * Puerto Rico and the US Virgin Islands.
     */
    public static boolean inUsEnvelope(double lat, double lon) {
        return box(lat, lon, 24.0, 49.6, -125.0, -66.5) // contiguous United States
                || box(lat, lon, 51.0, 71.6, -180.0, -129.5) // Alaska
                || box(lat, lon, 18.5, 22.5, -161.0, -154.5) // Hawaii
                || box(lat, lon, 17.5, 18.6, -68.0, -64.5); // Puerto Rico and the US Virgin Islands
    }

    /** True when the position (longitude east-positive) lies in Guam and the Northern Mariana Islands or the far
     * Aleutians, east of 180°. */
    static boolean inEastEnvelope(double lat, double lon) {
        return box(lat, lon, 13.0, 20.7, 144.5, 146.2) || box(lat, lon, 51.0, 53.5, 172.0, 180.0);
    }

    private static boolean box(double lat, double lon, double minLat, double maxLat, double minLon, double maxLon) {
        return lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon;
    }

    /**
     * {@code [unitCode, name]} of a title: the leading unit code (two letters, then three to six letters or digits)
     * split off, or {@code [null, title]}.
     *
     * @param title the item title, or null
     */
    public static String[] splitTitle(String title) {
        if (title == null || title.isBlank()) {
            return new String[] {null, null};
        }
        Matcher m = UNIT_TITLE.matcher(title.strip());
        return m.matches() ? new String[] {m.group(1), m.group(2).strip()} : new String[] {null, title.strip()};
    }

    private static String overview(String text) {
        Matcher m = OVERVIEW.matcher(text);
        if (!m.find()) {
            return null;
        }
        return truncate(blankToNull(m.group(1)), MAX_OVERVIEW_BYTES);
    }

    /** {@code text} cut to at most {@code maxBytes} UTF-8 bytes on a character boundary. */
    static String truncate(String text, int maxBytes) {
        if (text == null || text.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
            return text;
        }
        int bytes = 0;
        int end = 0;
        while (end < text.length()) {
            int cp = text.codePointAt(end);
            int size = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (bytes + size > maxBytes) {
                break;
            }
            bytes += size;
            end += Character.charCount(cp);
        }
        return text.substring(0, end).strip();
    }

    private static String group(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? blankToNull(m.group(1)) : null;
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }
}
