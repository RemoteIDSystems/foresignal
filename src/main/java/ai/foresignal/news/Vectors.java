package ai.foresignal.news;

import java.util.ArrayList;
import java.util.List;

/**
 * Embedding vectors of the story clustering (prompt 133): parsing pgvector's text form, cosine similarity and the
 * running centroid. Pure.
 */
public final class Vectors {

    private Vectors() {}

    /** Parses {@code [0.1,0.2,…]} (pgvector text, or a JSON array); null for blank or malformed text. */
    public static float[] parse(String text) {
        if (text == null) {
            return null;
        }
        String t = text.strip();
        if (t.length() < 2 || t.charAt(0) != '[' || t.charAt(t.length() - 1) != ']') {
            return null;
        }
        String body = t.substring(1, t.length() - 1).strip();
        if (body.isEmpty()) {
            return null;
        }
        String[] parts = body.split(",");
        float[] out = new float[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                out[i] = Float.parseFloat(parts[i].strip());
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return out;
    }

    /** The text form {@code [a,b,…]}. */
    public static String format(float[] v) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) {
                out.append(',');
            }
            out.append(v[i]);
        }
        return out.append(']').toString();
    }

    /** The cosine similarity of two vectors of one length, or null when either is missing, mismatched or zero. */
    public static Double cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) {
            return null;
        }
        double dot = 0;
        double na = 0;
        double nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        if (na == 0 || nb == 0) {
            return null;
        }
        return Math.round(dot / (Math.sqrt(na) * Math.sqrt(nb)) * 1_000_000) / 1_000_000.0;
    }

    /** The centroid after adding {@code v} to a centroid of {@code n} vectors ({@code centroid} null when n = 0). */
    public static float[] add(float[] centroid, int n, float[] v) {
        if (centroid == null || n <= 0) {
            return v.clone();
        }
        if (centroid.length != v.length) {
            return centroid;
        }
        float[] out = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            out[i] = (centroid[i] * n + v[i]) / (n + 1);
        }
        return out;
    }

    /** The mean of vectors of one length, or null when none. */
    public static float[] mean(List<float[]> vectors) {
        List<float[]> usable = new ArrayList<>();
        for (float[] v : vectors) {
            if (v != null && (usable.isEmpty() || usable.get(0).length == v.length)) {
                usable.add(v);
            }
        }
        if (usable.isEmpty()) {
            return null;
        }
        float[] out = new float[usable.get(0).length];
        for (float[] v : usable) {
            for (int i = 0; i < v.length; i++) {
                out[i] += v[i] / usable.size();
            }
        }
        return out;
    }
}
