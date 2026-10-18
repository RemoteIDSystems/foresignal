// Origin: ported from Meta's ThreatExchange PDQ reference implementation,
// https://github.com/facebook/ThreatExchange/tree/main/pdq (pdq/java/src/main/java/pdqhashing/hasher/PDQHasher.java,
// pdqhashing/types/Hash256.java and pdqhashing/utils/MatrixUtil.java, read 2026-10-18 at main commit
// 53f2929e3700360f0aeb5d74a209426c087f6d6a; PDQHasher.java last changed in cfb4bb47b2eb88df07b79a105dcdd3cfc290aba8).
// Copyright (c) Meta Platforms, Inc. and affiliates. BSD License (see THIRD_PARTY_NOTICES.md).
// Restructured for Foresignal: one immutable hash type, no timing metadata, no file reading (images are decoded by
// GuardedImages after ImageGuard has inspected them). The arithmetic (float luma, Jarosz box filters, decimation,
// quality metric, 16x64 DCT, Torben median, bit order) is unchanged so hashes match the reference test vectors.

package ai.foresignal.social.narrative;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;

/**
 * The PDQ perceptual image hash (prompt 136): a 256-bit hash and a 0–100 quality score per image. Two images whose
 * hashes differ in at most 31 bits ({@link NarrativeVocabulary#PDQ_MATCH_DISTANCE}, PDQ's recommended threshold) are
 * considered the same picture; hashes of quality below 50 are unreliable and kept without being matched.
 *
 * <p>Steps, as in the reference: luma ({@code 0.299 R + 0.587 G + 0.114 B} as floats), two X/Y passes of 1-D box
 * filters with window {@code ceil(dimension / 128)} (Jarosz tent filter), decimation to 64×64 at block centers, the
 * gradient quality metric, the first 16×16 DCT coefficients (rows and columns 1–16), and one bit per coefficient
 * above the Torben median. The hex form prints the sixteen 16-bit words from the last to the first. Thread-safe.
 */
public final class Pdq {

    private static final float LUMA_R = (float) 0.299;
    private static final float LUMA_G = (float) 0.587;
    private static final float LUMA_B = (float) 0.114;
    private static final float DCT_SCALE = (float) Math.sqrt(2.0 / 64.0);
    private static final int JAROSZ_PASSES = 2;
    private static final int WINDOW_DIVISOR = 128;
    private static final float[][] DCT = dctMatrix();

    private Pdq() {}

    /**
     * A PDQ hash with its quality.
     *
     * @param words the sixteen 16-bit words, word 0 holding bits 0–15
     * @param quality the quality, 0–100
     */
    public record Hash(short[] words, int quality) {

        /** Copies the words. */
        public Hash {
            if (words == null || words.length != 16) {
                throw new IllegalArgumentException("a PDQ hash has 16 words");
            }
            words = Arrays.copyOf(words, 16);
        }

        /** The 64-character lower-case hex form (word 15 first, as the reference prints it). */
        public String hex() {
            StringBuilder sb = new StringBuilder(64);
            for (int i = 15; i >= 0; i--) {
                sb.append(String.format(Locale.ROOT, "%04x", words[i] & 0xffff));
            }
            return sb.toString();
        }

        /** Word {@code i} as an unsigned value. */
        public int word(int i) {
            return words[i] & 0xffff;
        }

        /** The Hamming distance to another hash. */
        public int distance(Hash other) {
            int n = 0;
            for (int i = 0; i < 16; i++) {
                n += Integer.bitCount((words[i] ^ other.words[i]) & 0xffff);
            }
            return n;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Hash h && Arrays.equals(words, h.words) && quality == h.quality;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(words) * 31 + quality;
        }

        @Override
        public String toString() {
            return hex() + "," + quality;
        }
    }

    /**
     * Parses a 64-character hex hash (quality unknown, given as 0).
     *
     * @throws IllegalArgumentException when it is not 64 hex characters
     */
    public static Hash parse(String hex) {
        if (hex == null || !hex.matches("^[0-9a-fA-F]{64}$")) {
            throw new IllegalArgumentException("a PDQ hash is 64 hex characters");
        }
        byte[] bytes = HexFormat.of().parseHex(hex.toLowerCase(Locale.ROOT));
        short[] words = new short[16];
        for (int i = 0; i < 16; i++) {
            int b = (15 - i) * 2;
            words[i] = (short) (((bytes[b] & 0xff) << 8) | (bytes[b + 1] & 0xff));
        }
        return new Hash(words, 0);
    }

    /** The Hamming distance of two hex hashes. */
    public static int distance(String a, String b) {
        return parse(a).distance(parse(b));
    }

    /** Hashes a decoded image. */
    public static Hash hash(BufferedImage image) {
        int rows = image.getHeight();
        int cols = image.getWidth();
        float[] luma = new float[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int rgb = image.getRGB(j, i);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                luma[i * cols + j] = LUMA_R * r + LUMA_G * g + LUMA_B * b;
            }
        }
        return fromLuma(luma, rows, cols);
    }

    /**
     * Hashes a luma buffer (row-major, {@code rows × cols}); the buffer is overwritten.
     *
     * @param luma the luma values
     * @param rows the image height
     * @param cols the image width
     */
    public static Hash fromLuma(float[] luma, int rows, int cols) {
        float[] scratch = new float[rows * cols];
        int windowAlongRows = window(cols);
        int windowAlongCols = window(rows);
        for (int pass = 0; pass < JAROSZ_PASSES; pass++) {
            for (int i = 0; i < rows; i++) {
                box1D(luma, i * cols, scratch, i * cols, cols, 1, windowAlongRows);
            }
            for (int j = 0; j < cols; j++) {
                box1D(scratch, j, luma, j, rows, cols, windowAlongCols);
            }
        }
        float[][] block = new float[64][64];
        for (int i = 0; i < 64; i++) {
            int ini = (int) (((i + 0.5) * rows) / 64);
            for (int j = 0; j < 64; j++) {
                int inj = (int) (((j + 0.5) * cols) / 64);
                block[i][j] = luma[ini * cols + inj];
            }
        }
        int quality = quality(block);
        float[][] t = new float[16][64];
        float[][] b = new float[16][16];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 64; j++) {
                float sum = (float) 0.0;
                for (int k = 0; k < 64; k++) {
                    sum += DCT[i][k] * block[k][j];
                }
                t[i][j] = sum;
            }
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                float sum = (float) 0.0;
                for (int k = 0; k < 64; k++) {
                    sum += t[i][k] * DCT[j][k];
                }
                b[i][j] = sum;
            }
        }
        float median = torben(b);
        short[] words = new short[16];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (b[i][j] > median) {
                    int k = i * 16 + j;
                    words[(k & 255) >> 4] |= (short) (1 << (k & 15));
                }
            }
        }
        return new Hash(words, quality);
    }

    /** The box-filter window for a dimension: {@code ceil(dimension / 128)}. */
    static int window(int dimension) {
        return (dimension + WINDOW_DIVISOR - 1) / WINDOW_DIVISOR;
    }

    /** The gradient quality metric of the 64×64 downsample (0–100). */
    static int quality(float[][] block) {
        int gradientSum = 0;
        for (int i = 0; i < 63; i++) {
            for (int j = 0; j < 64; j++) {
                int d = (int) (((block[i][j] - block[i + 1][j]) * 100) / 255);
                gradientSum += Math.abs(d);
            }
        }
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 63; j++) {
                int d = (int) (((block[i][j] - block[i][j + 1]) * 100) / 255);
                gradientSum += Math.abs(d);
            }
        }
        return Math.min(100, gradientSum / 90);
    }

    private static void box1D(float[] in, int inStart, float[] out, int outStart, int length, int stride,
            int fullWindow) {
        int half = (fullWindow + 2) / 2;
        int phase1 = half - 1;
        int phase2 = fullWindow - half + 1;
        int phase3 = length - fullWindow;
        int phase4 = half - 1;
        int li = 0;
        int ri = 0;
        int oi = 0;
        float sum = (float) 0.0;
        int current = 0;
        for (int i = 0; i < phase1; i++) {
            sum += in[inStart + ri];
            current++;
            ri += stride;
        }
        for (int i = 0; i < phase2; i++) {
            sum += in[inStart + ri];
            current++;
            out[outStart + oi] = sum / current;
            ri += stride;
            oi += stride;
        }
        for (int i = 0; i < phase3; i++) {
            sum += in[inStart + ri];
            sum -= in[inStart + li];
            out[outStart + oi] = sum / current;
            li += stride;
            ri += stride;
            oi += stride;
        }
        for (int i = 0; i < phase4; i++) {
            sum -= in[inStart + li];
            current--;
            out[outStart + oi] = sum / current;
            li += stride;
            oi += stride;
        }
    }

    /** Torben's median of a 16×16 matrix, as the reference computes it. */
    static float torben(float[][] m) {
        int n = 256;
        int midn = (n + 1) / 2;
        int less;
        int greater;
        int equal;
        float min = m[0][0];
        float max = m[0][0];
        float guess;
        float maxltguess;
        float mingtguess;
        for (float[] row : m) {
            for (float v : row) {
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            }
        }
        while (true) {
            guess = (min + max) / 2;
            less = 0;
            greater = 0;
            equal = 0;
            maxltguess = min;
            mingtguess = max;
            for (float[] row : m) {
                for (float v : row) {
                    if (v < guess) {
                        less++;
                        if (v > maxltguess) {
                            maxltguess = v;
                        }
                    } else if (v > guess) {
                        greater++;
                        if (v < mingtguess) {
                            mingtguess = v;
                        }
                    } else {
                        equal++;
                    }
                }
            }
            if (less <= midn && greater <= midn) {
                break;
            } else if (less > greater) {
                max = maxltguess;
            } else {
                min = mingtguess;
            }
        }
        if (less >= midn) {
            return maxltguess;
        } else if (less + equal >= midn) {
            return guess;
        }
        return mingtguess;
    }

    private static float[][] dctMatrix() {
        float[][] d = new float[16][64];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 64; j++) {
                d[i][j] = (float) (DCT_SCALE * Math.cos((Math.PI / 2 / 64.0) * (i + 1) * (2 * j + 1)));
            }
        }
        return d;
    }
}
