package ai.foresignal.social.narrative;

import ai.foresignal.chronicle.network.MinHashSketch;
import ai.foresignal.social.TextFolding;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalized post tokens and their MinHash signatures (prompt 136): the text test of narrative clustering without
 * embeddings (Jaccard ≥ 0.6), cross-platform near-identical text (≥ 0.8) and the near-duplicate clusters of the
 * coordination indicators. A text loses its URLs and {@code @}mentions, is folded ({@link TextFolding#fold}: case,
 * diacritics, Arabic letter variants) and split on spaces; a token of an unspaced script (Chinese, Japanese, Thai…) is
 * split into character bigrams; tokens shorter than two characters are dropped. Each token is hashed to 64 bits
 * (FNV-1a over UTF-8, then the SplitMix64 finalizer) and the set is signed with the 128-slot {@link MinHashSketch} of
 * prompt 134. Pure.
 */
public final class TextSignatures {

    private static final Pattern URL = Pattern.compile("https?://\\S+");
    private static final Pattern MENTION = Pattern.compile("(?<![\\p{L}\\p{N}_])@[\\p{L}\\p{N}_.\\-]+");

    private TextSignatures() {}

    /** The normalized tokens of a text, in first-seen order; empty for null. */
    public static List<String> tokens(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String stripped = MENTION.matcher(URL.matcher(text).replaceAll(" ")).replaceAll(" ");
        String folded = TextFolding.fold(stripped);
        Set<String> out = new LinkedHashSet<>();
        for (String t : folded.split(" ")) {
            if (t.isEmpty()) {
                continue;
            }
            if (TextFolding.unspacedScript(t.codePointAt(0))) {
                int[] cps = t.codePoints().toArray();
                if (cps.length == 1) {
                    continue;
                }
                for (int i = 0; i + 1 < cps.length; i++) {
                    out.add(new String(cps, i, 2));
                }
            } else if (t.codePointCount(0, t.length()) >= 2) {
                out.add(t);
            }
        }
        return List.copyOf(out);
    }

    /** The 64-bit hash of a token. */
    public static long hash(String token) {
        long h = 0xcbf29ce484222325L;
        for (byte b : token.getBytes(StandardCharsets.UTF_8)) {
            h ^= b & 0xff;
            h *= 0x100000001b3L;
        }
        long z = h;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** The token hashes of a text. */
    public static List<Long> hashes(String text) {
        List<Long> out = new ArrayList<>();
        for (String t : tokens(text)) {
            out.add(hash(t));
        }
        return out;
    }

    /** The MinHash signature of a text (empty for a text without tokens). */
    public static MinHashSketch signature(String text) {
        return MinHashSketch.of(hashes(text));
    }

    /** The estimated Jaccard similarity of two signatures (0 when either is null or empty). */
    public static double jaccard(MinHashSketch a, MinHashSketch b) {
        return a == null || b == null ? 0 : a.jaccard(b);
    }

    /** The exact token Jaccard of two texts (tests and small comparisons). */
    public static double exactJaccard(String a, String b) {
        Set<String> sa = new LinkedHashSet<>(tokens(a));
        Set<String> sb = new LinkedHashSet<>(tokens(b));
        if (sa.isEmpty() || sb.isEmpty()) {
            return 0;
        }
        int inter = 0;
        for (String t : sa) {
            if (sb.contains(t)) {
                inter++;
            }
        }
        return (double) inter / (sa.size() + sb.size() - inter);
    }
}
