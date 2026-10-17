package ai.foresignal.baserate.reference;

import ai.foresignal.api.error.ValidationException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/** Parameter helpers shared by the reference importers (prompt 121). */
public final class ImportParams {

    /** The earliest year an import may ask for. */
    public static final int MIN_YEAR = 1900;

    private ImportParams() {}

    /** An integer parameter, or {@code fallback} when absent; 400 when not a whole number. */
    public static Integer integer(Map<String, Object> params, String key, Integer fallback) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString().strip());
        } catch (NumberFormatException e) {
            throw ValidationException.forField("params." + key, "must be a whole number");
        }
    }

    /** A number parameter, or {@code fallback}; 400 when not a number. */
    public static double number(Map<String, Object> params, String key, double fallback) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString().strip());
        } catch (NumberFormatException e) {
            throw ValidationException.forField("params." + key, "must be a number");
        }
    }

    /** A boolean parameter, or {@code fallback}. */
    public static boolean bool(Map<String, Object> params, String key, boolean fallback) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return fallback;
        }
        return value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString().strip());
    }

    /** A string parameter, stripped, or null. */
    public static String string(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return value.toString().strip();
    }

    /** A string-list parameter, upper-cased, de-duplicated and sorted; empty when absent. */
    public static List<String> upperList(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        TreeSet<String> out = new TreeSet<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (o != null && !o.toString().isBlank()) {
                    out.add(o.toString().strip().toUpperCase(Locale.ROOT));
                }
            }
        } else if (value != null) {
            for (String part : value.toString().split(",")) {
                if (!part.isBlank()) {
                    out.add(part.strip().toUpperCase(Locale.ROOT));
                }
            }
        }
        return new ArrayList<>(out);
    }

    /**
     * The {@code [yearFrom, yearTo]} range: defaults when absent, 400 when inverted, before {@value #MIN_YEAR} or after
     * the current year.
     */
    public static int[] years(Map<String, Object> params, int defaultFrom) {
        int thisYear = LocalDate.now(ZoneOffset.UTC).getYear();
        int from = integer(params, "yearFrom", defaultFrom);
        int to = integer(params, "yearTo", thisYear);
        if (from < MIN_YEAR || to < MIN_YEAR) {
            throw ValidationException.forField("params.yearFrom", "years start at " + MIN_YEAR);
        }
        if (to > thisYear) {
            throw ValidationException.forField("params.yearTo", "must not be after " + thisYear);
        }
        if (from > to) {
            throw ValidationException.forField("params.yearFrom", "must not be after yearTo");
        }
        return new int[] {from, to};
    }
}
