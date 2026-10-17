package ai.foresignal.cycle;

import ai.foresignal.forecast.language.EstimativeLanguage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds generated judgment text to the wording rules (prompt 128, Intelligence-Design §6.4) with
 * {@link EstimativeLanguage#lint}: a likelihood term must agree with the judgment's number (or a percentage in its
 * sentence), the two ICD 203 rows are never mixed, and a likelihood term never shares a sentence with a confidence
 * level. Failing text is <strong>regenerated once</strong> (the caller's model call); text that still fails is
 * <strong>downgraded</strong> deterministically — each mismatched term replaced by the term of its number, second-row
 * terms by their first-row equivalents, confidence phrases removed (the confidence belongs in the separate
 * confidence statement) — and text that cannot be repaired is dropped. Every step leaves a warning.
 */
public final class JudgmentLinter {

    /** A confidence phrase, with an optional leading "with" or "at". */
    static final Pattern CONFIDENCE = Pattern.compile(
            "(?i)[,;]?\\s*\\b(?:(?:with|at|and|we\\s+have)\\s+)?(?:low|moderate|high)[ -]confidence\\b");

    private static final Pattern EXPECTED = Pattern.compile("which is '([a-z ]+)'");

    private JudgmentLinter() {}

    /**
     * The outcome.
     *
     * @param text the final text, or null when dropped
     * @param regenerated true when the regeneration was used and passed
     * @param downgraded true when the deterministic repair was applied
     * @param dropped true when nothing usable remained
     * @param warnings what happened
     */
    public record Result(String text, boolean regenerated, boolean downgraded, boolean dropped,
            List<String> warnings) {}

    /** The violations of {@code text} for a judgment of probability {@code p} (may be null). */
    public static List<EstimativeLanguage.Violation> violations(String text, Double p) {
        if (text == null) {
            return List.of();
        }
        List<EstimativeLanguage.Claim> claims = p == null || p.isNaN() ? List.of()
                : List.of(new EstimativeLanguage.Claim(p, 0, text.length()));
        return EstimativeLanguage.lint(text, claims);
    }

    /**
     * Lints, regenerates once and downgrades.
     *
     * @param text the generated text
     * @param p the judgment's probability, or null
     * @param regenerate asks the model for a corrected text given the violation messages; may be null or return null
     */
    public static Result check(String text, Double p, Function<List<String>, String> regenerate) {
        List<String> warnings = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return new Result(null, false, false, true, List.of("empty judgment dropped"));
        }
        List<EstimativeLanguage.Violation> first = violations(text, p);
        if (first.isEmpty()) {
            return new Result(text, false, false, false, warnings);
        }
        List<String> messages = first.stream().map(EstimativeLanguage.Violation::message).toList();
        warnings.add("estimative language: " + String.join("; ", messages));
        String candidate = text;
        if (regenerate != null) {
            String again = null;
            try {
                again = regenerate.apply(messages);
            } catch (RuntimeException e) {
                warnings.add("regeneration failed: " + e.getMessage());
            }
            if (again != null && !again.isBlank()) {
                if (violations(again, p).isEmpty()) {
                    warnings.add("regenerated once; the new text passes the lint");
                    return new Result(again.strip(), true, false, false, warnings);
                }
                warnings.add("the regenerated text still fails the lint");
                candidate = again.strip();
            }
        }
        String repaired = downgrade(candidate, p);
        if (repaired != null && !repaired.isBlank() && violations(repaired, p).isEmpty()) {
            warnings.add("downgraded: terms derived from the numbers and confidence moved to its own statement");
            return new Result(repaired, false, true, false, warnings);
        }
        repaired = downgrade(text, p);
        if (repaired != null && !repaired.isBlank() && violations(repaired, p).isEmpty()) {
            warnings.add("downgraded: terms derived from the numbers and confidence moved to its own statement");
            return new Result(repaired, false, true, false, warnings);
        }
        warnings.add("dropped: the text could not be brought within the wording rules");
        return new Result(null, false, false, true, warnings);
    }

    /**
     * The deterministic repair: mismatched terms replaced by the term of their number, second-row terms by the
     * first-row term of the same band, confidence phrases removed.
     */
    static String downgrade(String text, Double p) {
        String out = text;
        List<EstimativeLanguage.Violation> found = new ArrayList<>(violations(out, p));
        found.sort(Comparator.comparingInt(EstimativeLanguage.Violation::start).reversed());
        int lastStart = Integer.MAX_VALUE;
        for (EstimativeLanguage.Violation v : found) {
            if (v.end() > lastStart) {
                continue;
            }
            String replacement = null;
            if (EstimativeLanguage.TERM_MISMATCH.equals(v.code())) {
                Matcher m = EXPECTED.matcher(v.message());
                String expected = null;
                while (m.find()) {
                    expected = m.group(1);
                }
                replacement = p != null && !p.isNaN() ? EstimativeLanguage.term(clamp(p)) : expected;
            } else if (EstimativeLanguage.MIXED_ROWS.equals(v.code())) {
                int index = EstimativeLanguage.SECOND_ROW.indexOf(v.term().toLowerCase(Locale.ROOT));
                replacement = index < 0 ? null : EstimativeLanguage.TERMS.get(index);
            }
            if (replacement != null) {
                out = out.substring(0, v.start()) + replacement + out.substring(v.end());
                lastStart = v.start();
            }
        }
        out = CONFIDENCE.matcher(out).replaceAll("");
        out = out.replaceAll("\\s{2,}", " ").replaceAll("\\s+([.,;:])", "$1").strip();
        return out;
    }

    private static double clamp(double p) {
        return Math.max(0, Math.min(1, p));
    }
}
