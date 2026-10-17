package ai.foresignal.hypothesis;

import ai.foresignal.forecast.language.EstimativeLanguage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The deterministic change rationale of automatic revisions (prompt 125 §4.8, ICD 203 standard 7): what moved the
 * judgment relative to the previous revision. Each likelihood term shares its sentence only with its own percentage,
 * the drivers are listed in a sentence of their own, and confidence is stated in separate sentences, so the text
 * passes {@link EstimativeLanguage#lint}. When operator-written labels or descriptions would break the lint (for
 * example a label containing a likelihood term), the text is rebuilt with neutral references instead.
 */
public final class RevisionRationale {

    /** The smallest change reported as a move. */
    public static final double MIN_MOVE = 0.005;

    /** At most this many hypotheses are described. */
    public static final int MAX_MOVES = 3;

    private RevisionRationale() {}

    /**
     * A hypothesis before and after.
     *
     * @param label its label
     * @param before the probability before
     * @param after the probability after
     * @param confidenceBefore the confidence before, or null
     * @param confidenceAfter the confidence after, or null
     * @param capped true when it is held at its speculative-link cap
     */
    public record Moved(String label, double before, double after, String confidenceBefore, String confidenceAfter,
            boolean capped) {}

    /**
     * One driver for prose.
     *
     * @param description the indicator or evidence description
     * @param kind {@code observed}, {@code absent}, {@code evidence-added}, {@code evidence-removed},
     *     {@code base-rate}, {@code review} or {@code operator}
     * @param lr the strongest LR it carried, or null
     * @param suppression the suppression reason when nothing was applied, or null
     * @param damped true when daily damping cut it
     */
    public record DriverNote(String description, String kind, Double lr, String suppression, boolean damped) {}

    /** Builds the rationale, falling back to neutral references when the lint would fail. */
    public static String build(List<Moved> moves, List<DriverNote> drivers) {
        String text = compose(moves, drivers, false);
        if (EstimativeLanguage.clean(text, List.of())) {
            return text;
        }
        return compose(moves, drivers, true);
    }

    /** A percentage that reads in the same ICD band as {@code p} (one or two decimals when rounding would cross). */
    public static String percent(double p) {
        int band = EstimativeLanguage.band(p).index();
        long whole = Math.round(p * 100);
        if (EstimativeLanguage.band(Math.max(0, Math.min(1, whole / 100.0))).index() == band) {
            return whole + "%";
        }
        for (int decimals = 1; decimals <= 3; decimals++) {
            String text = String.format(Locale.ROOT, "%." + decimals + "f", p * 100);
            double back = Double.parseDouble(text) / 100.0;
            if (EstimativeLanguage.band(Math.max(0, Math.min(1, back))).index() == band) {
                return text + "%";
            }
        }
        return String.format(Locale.ROOT, "%.4f", p * 100) + "%";
    }

    private static String compose(List<Moved> moves, List<DriverNote> drivers, boolean neutral) {
        StringBuilder out = new StringBuilder();
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            if (Math.abs(moves.get(i).after() - moves.get(i).before()) >= MIN_MOVE) {
                order.add(i);
            }
        }
        order.sort(Comparator.comparingDouble((Integer i) -> -Math.abs(moves.get(i).after() - moves.get(i).before()))
                .thenComparing(i -> i));
        if (order.isEmpty()) {
            out.append("No hypothesis moved by half a percentage point or more.");
        }
        for (int k = 0; k < Math.min(MAX_MOVES, order.size()); k++) {
            int i = order.get(k);
            Moved m = moves.get(i);
            String name = neutral ? "hypothesis " + (i + 1) : "'" + clean(m.label()) + "'";
            sentence(out, capitalize(name) + " was " + EstimativeLanguage.term(m.before()) + " (" + percent(m.before())
                    + ").");
            sentence(out, "It is now " + EstimativeLanguage.term(m.after()) + " (" + percent(m.after()) + ").");
        }
        if (order.size() > MAX_MOVES) {
            sentence(out, (order.size() - MAX_MOVES) + " more hypotheses moved less.");
        }
        List<String> items = new ArrayList<>();
        boolean damped = false;
        for (int i = 0; i < drivers.size(); i++) {
            DriverNote d = drivers.get(i);
            String what = neutral || d.description() == null ? "driver " + (i + 1) : clean(d.description());
            String strength = d.lr() == null ? "" : " (" + LikelihoodRatios.diagnosticity(d.lr()) + " diagnosticity)";
            String item = switch (d.kind() == null ? "" : d.kind()) {
                case "observed" -> what + " observed" + strength;
                case "absent" -> what + " absent with coverage" + strength;
                case "evidence-added" -> what + " added as evidence" + strength;
                case "evidence-removed" -> what + " removed from the evidence";
                case "base-rate" -> "the base rate behind the prior was refreshed";
                case "review" -> "a review set the probabilities";
                case "operator" -> "an operator override set the probabilities";
                default -> what;
            };
            if (d.suppression() != null) {
                item = item + ", not counted again (" + d.suppression() + ")";
            }
            damped |= d.damped();
            items.add(item);
        }
        if (!items.isEmpty()) {
            sentence(out, "Drivers: " + String.join("; ", items) + ".");
        }
        if (damped) {
            sentence(out, "Daily damping limited the change.");
        }
        for (int i = 0; i < moves.size(); i++) {
            Moved m = moves.get(i);
            String name = neutral ? "hypothesis " + (i + 1) : "'" + clean(m.label()) + "'";
            if (m.capped()) {
                sentence(out, capitalize(name) + " is held at its speculative-link cap from the coincidence"
                        + " baseline.");
            }
            if (m.confidenceBefore() != null && m.confidenceAfter() != null
                    && !m.confidenceBefore().equals(m.confidenceAfter())) {
                sentence(out, "Analytic confidence for " + name + " changed from " + m.confidenceBefore() + " to "
                        + m.confidenceAfter() + ".");
            }
        }
        return out.toString();
    }

    private static void sentence(StringBuilder out, String s) {
        if (!out.isEmpty()) {
            out.append(' ');
        }
        out.append(s);
    }

    private static String capitalize(String s) {
        if (s.isEmpty() || s.charAt(0) == '\'') {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** One line, no sentence breaks inside a quoted label or description, at most 160 characters. */
    private static String clean(String s) {
        String one = s.replace('\n', ' ').replace('\r', ' ').replaceAll("[.!?]+(\\s|$)", "$1").trim();
        return one.length() > 160 ? one.substring(0, 159) + "\u2026" : one;
    }
}
