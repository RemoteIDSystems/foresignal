package ai.foresignal.source.adapters.radio;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The narrow-filter rule of {@code aprs-is} (prompt 082; aprsc server-side filters, aprs-is.net "javAPRSFilter").
 * Pure.
 *
 * <p>APRS-IS etiquette is to ask only for what is needed, and an unfiltered or worldwide feed is tens of packets a
 * second. A filter is accepted only when:
 * <ul>
 *   <li>it has 1 to {@value #MAX_TERMS} space-separated terms;
 *   <li>every term is a range {@code r/lat/lon/km}, a buddy list {@code b/call1/call2...}, a prefix list
 *       {@code p/aa/bb...}, a type filter with a range {@code t/types/call/km}, or a friend range
 *       {@code f/call/km} (exclusions, {@code -} terms and every other filter type are refused);
 *   <li>no range is larger than {@value #MAX_RANGE_KM} km;
 *   <li>no term is a bare wildcard ({@code b/*}, {@code p/*}): a {@code *} is accepted only as the suffix of at least
 *       one character ({@code b/N0CALL*});
 *   <li>a type filter carries its call and range: {@code t/p} alone is worldwide.
 * </ul>
 */
public final class AprsIsFilter {

    /** The most terms. */
    public static final int MAX_TERMS = 5;

    /** The largest range, in kilometers. */
    public static final double MAX_RANGE_KM = 250.0;

    private static final Pattern CALL = Pattern.compile("^[A-Za-z0-9-]{1,9}\\*?$");

    private static final Pattern PREFIX = Pattern.compile("^[A-Za-z0-9]{1,9}$");

    private static final Pattern TYPES = Pattern.compile("^[poimqstunw]{1,10}$");

    private AprsIsFilter() {}

    /**
     * The first problem with {@code filter}, or null when it is a valid narrow filter.
     *
     * @param filter the filter as the operator wrote it
     */
    public static String problem(String filter) {
        if (filter == null || filter.isBlank()) {
            return "is required: a narrow filter such as r/37.77/-122.42/50 (latitude, longitude, km)";
        }
        String[] terms = filter.strip().split("\\s+");
        if (terms.length > MAX_TERMS) {
            return "has " + terms.length + " terms; at most " + MAX_TERMS + " are allowed";
        }
        for (String term : terms) {
            String problem = termProblem(term);
            if (problem != null) {
                return "'" + term + "' " + problem;
            }
        }
        return null;
    }

    /** The problem with one term, or null. */
    static String termProblem(String term) {
        if (term.length() < 3 || term.charAt(1) != '/') {
            return "is not a filter term; use r/, b/, p/, t/ or f/";
        }
        List<String> parts = List.of(term.substring(2).split("/", -1));
        for (String part : parts) {
            if (part.isEmpty()) {
                return "has an empty part";
            }
            if (part.equals("*") || part.startsWith("*")) {
                return "is a bare wildcard, which is not narrow";
            }
        }
        return switch (term.charAt(0)) {
            case 'r' -> range(parts);
            case 'b' -> parts.stream().allMatch(p -> CALL.matcher(p).matches()) ? null
                    : "lists something that is not a call sign";
            case 'p' -> parts.stream().allMatch(p -> PREFIX.matcher(p).matches()) ? null
                    : "lists something that is not a call-sign prefix";
            case 't' -> types(parts);
            case 'f' -> friend(parts);
            default -> "is not an allowed filter type; use r/, b/, p/, t/ or f/";
        };
    }

    private static String range(List<String> parts) {
        if (parts.size() != 3) {
            return "must be r/lat/lon/km";
        }
        Double lat = number(parts.get(0));
        Double lon = number(parts.get(1));
        Double km = number(parts.get(2));
        if (lat == null || lon == null || km == null || Math.abs(lat) > 90 || Math.abs(lon) > 180) {
            return "must be r/lat/lon/km with a valid latitude and longitude";
        }
        return distance(km);
    }

    private static String types(List<String> parts) {
        if (!TYPES.matcher(parts.get(0)).matches()) {
            return "has unknown types; use letters of poimqstunw";
        }
        if (parts.size() != 3) {
            return "must name a call and a range (t/types/call/km); a type filter alone is worldwide";
        }
        if (!CALL.matcher(parts.get(1)).matches() || parts.get(1).endsWith("*")) {
            return "must name a call sign";
        }
        Double km = number(parts.get(2));
        return km == null ? "must end in a range in km" : distance(km);
    }

    private static String friend(List<String> parts) {
        if (parts.size() != 2 || !CALL.matcher(parts.get(0)).matches() || parts.get(0).endsWith("*")) {
            return "must be f/call/km";
        }
        Double km = number(parts.get(1));
        return km == null ? "must end in a range in km" : distance(km);
    }

    private static String distance(double km) {
        if (!(km > 0)) {
            return "must have a positive range";
        }
        return km > MAX_RANGE_KM ? "has a range over " + (int) MAX_RANGE_KM + " km" : null;
    }

    private static Double number(String text) {
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
