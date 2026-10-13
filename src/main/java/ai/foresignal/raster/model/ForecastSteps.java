package ai.foresignal.raster.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A forecast model's step list (prompt 051), written as items of {@code N} or {@code A-B/S}
 * ({@code A} to {@code B} every {@code S} hours, inclusive): the GFS default {@code 0-48/3, 54-120/6}
 * expands to f000–f048 3-hourly and f054–f120 6-hourly, 29 steps.
 */
public final class ForecastSteps {

    /** The largest step accepted, hours. */
    public static final int MAX_STEP = 384;

    /** The most steps a list may expand to. */
    public static final int MAX_STEPS = 200;

    private static final Pattern RANGE = Pattern.compile("^(\\d{1,3})-(\\d{1,3})/(\\d{1,3})$");
    private static final Pattern SINGLE = Pattern.compile("^\\d{1,3}$");

    private ForecastSteps() {}

    /**
     * Expands the items (each may itself be comma-separated).
     *
     * @return the steps, ascending and distinct
     * @throws IllegalArgumentException for an unreadable item, a step above {@value #MAX_STEP}, an
     *     empty list or more than {@value #MAX_STEPS} steps
     */
    public static List<Integer> parse(List<String> items) {
        TreeSet<Integer> out = new TreeSet<>();
        if (items != null) {
            for (String item : items) {
                if (item == null) {
                    continue;
                }
                for (String part : item.split(",")) {
                    String p = part.strip();
                    if (p.isEmpty()) {
                        continue;
                    }
                    Matcher m = RANGE.matcher(p);
                    if (m.matches()) {
                        int from = Integer.parseInt(m.group(1));
                        int to = Integer.parseInt(m.group(2));
                        int every = Integer.parseInt(m.group(3));
                        if (every < 1 || to < from) {
                            throw new IllegalArgumentException("Step range '" + p + "' needs A ≤ B and S ≥ 1");
                        }
                        for (int s = from; s <= to; s += every) {
                            add(out, s, p);
                        }
                    } else if (SINGLE.matcher(p).matches()) {
                        add(out, Integer.parseInt(p), p);
                    } else {
                        throw new IllegalArgumentException("Step item '" + p + "' must be N or A-B/S");
                    }
                }
            }
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("The step list is empty");
        }
        return List.copyOf(new ArrayList<>(out));
    }

    private static void add(TreeSet<Integer> out, int step, String item) {
        if (step > MAX_STEP) {
            throw new IllegalArgumentException("Step " + step + " in '" + item + "' is above " + MAX_STEP);
        }
        out.add(step);
        if (out.size() > MAX_STEPS) {
            throw new IllegalArgumentException("The step list expands to more than " + MAX_STEPS + " steps");
        }
    }
}
