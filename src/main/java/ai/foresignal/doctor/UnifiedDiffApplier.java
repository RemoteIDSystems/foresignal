package ai.foresignal.doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A small in-house unified-diff applier (prompt 140, task 5), used only to check in memory whether a patch the LLM
 * proposed applies cleanly to the packaged adapter source. Nothing is ever written to disk, compiled or loaded.
 *
 * <p>Strict: every context and removed line of every hunk must match the original exactly (trailing whitespace
 * ignored), at the hunk's stated line or within {@value #FUZZ} lines of it. Hunks apply in order and may not overlap.
 * {@code ---}/{@code +++} headers are optional; a diff for several files is refused.
 */
public final class UnifiedDiffApplier {

    /** How far a hunk may sit from its stated line. */
    public static final int FUZZ = 50;

    private static final Pattern HUNK = Pattern.compile("^@@ -(\\d+)(?:,(\\d+))? \\+(\\d+)(?:,(\\d+))? @@.*$");

    private UnifiedDiffApplier() {}

    /**
     * The outcome of an application.
     *
     * @param applies true when every hunk applied cleanly
     * @param patched the patched text when it applied, else null
     * @param error why it did not apply, else null
     * @param hunks the hunks found
     */
    public record Result(boolean applies, String patched, String error, int hunks) {

        static Result failure(String error, int hunks) {
            return new Result(false, null, error, hunks);
        }
    }

    /** One parsed hunk. */
    record Hunk(int oldStart, List<String> lines) {}

    /**
     * Applies {@code diff} to {@code original}.
     *
     * @param original the file's text
     * @param diff the unified diff
     */
    public static Result apply(String original, String diff) {
        if (original == null || diff == null || diff.isBlank()) {
            return Result.failure("empty patch or file", 0);
        }
        List<Hunk> hunks;
        try {
            hunks = parse(diff);
        } catch (IllegalArgumentException e) {
            return Result.failure(e.getMessage(), 0);
        }
        if (hunks.isEmpty()) {
            return Result.failure("the patch has no hunk", 0);
        }
        boolean trailingNewline = original.endsWith("\n");
        List<String> source = new ArrayList<>(List.of(original.split("\n", -1)));
        if (trailingNewline) {
            source.remove(source.size() - 1);
        }
        List<String> out = new ArrayList<>();
        int cursor = 0;
        int number = 0;
        for (Hunk hunk : hunks) {
            number++;
            List<String> expected = new ArrayList<>();
            List<String> replacement = new ArrayList<>();
            for (String line : hunk.lines()) {
                char kind = line.isEmpty() ? ' ' : line.charAt(0);
                String text = line.isEmpty() ? "" : line.substring(1);
                switch (kind) {
                    case ' ' -> {
                        expected.add(text);
                        replacement.add(text);
                    }
                    case '-' -> expected.add(text);
                    case '+' -> replacement.add(text);
                    case '\\' -> { }
                    default -> {
                        return Result.failure("hunk " + number + " has a line that is not context, '-' or '+'",
                                hunks.size());
                    }
                }
            }
            int stated = Math.max(0, hunk.oldStart() - 1);
            int at = locate(source, expected, stated, cursor);
            if (at < 0) {
                return Result.failure("hunk " + number + " does not apply: its context does not match the file near"
                        + " line " + hunk.oldStart(), hunks.size());
            }
            out.addAll(source.subList(cursor, at));
            out.addAll(replacement);
            cursor = at + expected.size();
        }
        out.addAll(source.subList(cursor, source.size()));
        String patched = String.join("\n", out) + (trailingNewline ? "\n" : "");
        return new Result(true, patched, null, hunks.size());
    }

    /** The parsed hunks of a unified diff. */
    static List<Hunk> parse(String diff) {
        List<Hunk> hunks = new ArrayList<>();
        String[] lines = diff.replace("\r\n", "\n").split("\n", -1);
        int files = 0;
        Hunk current = null;
        int remainingOld = 0;
        int remainingNew = 0;
        for (String line : lines) {
            if (current != null && (remainingOld > 0 || remainingNew > 0)) {
                if (line.startsWith("\\")) {
                    continue;
                }
                char kind = line.isEmpty() ? ' ' : line.charAt(0);
                if (kind == ' ' || line.isEmpty()) {
                    remainingOld--;
                    remainingNew--;
                } else if (kind == '-') {
                    remainingOld--;
                } else if (kind == '+') {
                    remainingNew--;
                } else {
                    throw new IllegalArgumentException("hunk " + hunks.size() + " ends early: expected more lines");
                }
                current.lines().add(line);
                continue;
            }
            if (line.startsWith("--- ")) {
                files++;
                if (files > 1) {
                    throw new IllegalArgumentException("the patch touches more than one file");
                }
                continue;
            }
            if (line.startsWith("+++ ") || line.startsWith("diff ") || line.startsWith("index ")) {
                continue;
            }
            Matcher m = HUNK.matcher(line);
            if (m.matches()) {
                int oldStart = Integer.parseInt(m.group(1));
                remainingOld = m.group(2) == null ? 1 : Integer.parseInt(m.group(2));
                remainingNew = m.group(4) == null ? 1 : Integer.parseInt(m.group(4));
                current = new Hunk(oldStart, new ArrayList<>());
                hunks.add(current);
            }
        }
        if (current != null && (remainingOld > 0 || remainingNew > 0)) {
            throw new IllegalArgumentException("the last hunk is incomplete");
        }
        return hunks;
    }

    private static int locate(List<String> source, List<String> expected, int stated, int cursor) {
        if (matches(source, expected, stated, cursor)) {
            return stated;
        }
        for (int delta = 1; delta <= FUZZ; delta++) {
            if (matches(source, expected, stated - delta, cursor)) {
                return stated - delta;
            }
            if (matches(source, expected, stated + delta, cursor)) {
                return stated + delta;
            }
        }
        return -1;
    }

    private static boolean matches(List<String> source, List<String> expected, int at, int cursor) {
        if (at < cursor || at + expected.size() > source.size()) {
            return false;
        }
        for (int i = 0; i < expected.size(); i++) {
            if (!source.get(at + i).stripTrailing().equals(expected.get(i).stripTrailing())) {
                return false;
            }
        }
        return true;
    }
}
