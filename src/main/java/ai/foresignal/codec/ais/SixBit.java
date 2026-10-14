package ai.foresignal.codec.ais;

import java.util.Arrays;
import java.util.Locale;

/**
 * AIS payload armoring and bit fields (prompt 085, ITU-R M.1371-5 and the gpsd AIVDM notes).
 *
 * <p>An AIVDM payload carries six bits per character: ASCII 48–87 ({@code 0}–{@code W}) are the values 0–39 and
 * ASCII 96–119 ({@code `}–{@code w}) the values 40–63. The last character may be padded with "fill bits", which the
 * sentence states. Text fields use the six-bit alphabet {@code @A–Z[\]^_ !"#$%&'()*+,-./0–9:;<=>?}, where {@code @}
 * is padding.
 */
public final class SixBit {

    /** The six-bit text alphabet, indexed by value. */
    static final String TEXT = "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_ !\"#$%&'()*+,-./0123456789:;<=>?";

    private SixBit() {}

    /**
     * An armored payload.
     *
     * @param payload the six-bit characters
     * @param fillBits the padding bits in the last character, 0 to 5
     */
    public record Armored(String payload, int fillBits) {

        /** Checks the fill bits. */
        public Armored {
            if (payload == null) {
                throw new IllegalArgumentException("payload is required");
            }
            if (fillBits < 0 || fillBits > 5) {
                throw new IllegalArgumentException("fill bits must be 0 to 5, but were " + fillBits);
            }
        }

        /** The number of data bits. */
        public int bitLength() {
            return payload.length() * 6 - fillBits;
        }
    }

    /**
     * The six-bit value of an armored character.
     *
     * @throws IllegalArgumentException when {@code c} is outside the armoring alphabet
     */
    public static int value(char c) {
        if (c >= 48 && c <= 87) {
            return c - 48;
        }
        if (c >= 96 && c <= 119) {
            return c - 56;
        }
        throw new IllegalArgumentException("'" + c + "' is not an AIS payload character");
    }

    /** The armored character of a six-bit value. */
    public static char character(int value) {
        if (value < 0 || value > 63) {
            throw new IllegalArgumentException("six-bit value out of range: " + value);
        }
        return (char) (value < 40 ? value + 48 : value + 56);
    }

    /**
     * Unpacks an armored payload into its bits.
     *
     * @throws IllegalArgumentException on a character outside the alphabet or impossible fill bits
     */
    public static Reader decode(Armored armored) {
        String payload = armored.payload();
        int length = armored.bitLength();
        if (length < 0) {
            throw new IllegalArgumentException("more fill bits than payload bits");
        }
        byte[] bits = new byte[payload.length() * 6];
        for (int i = 0; i < payload.length(); i++) {
            int v = value(payload.charAt(i));
            for (int b = 0; b < 6; b++) {
                bits[i * 6 + b] = (byte) ((v >> (5 - b)) & 1);
            }
        }
        return new Reader(Arrays.copyOf(bits, length));
    }

    /** Unpacks {@code payload} with {@code fillBits}. */
    public static Reader decode(String payload, int fillBits) {
        return decode(new Armored(payload, fillBits));
    }

    /** Reads bit fields in order, most significant bit first. */
    public static final class Reader {

        private final byte[] bits;
        private int position;

        Reader(byte[] bits) {
            this.bits = bits;
        }

        /** The number of data bits. */
        public int length() {
            return bits.length;
        }

        /** The next bit to read. */
        public int position() {
            return position;
        }

        /** Moves to bit {@code bit}. */
        public Reader seek(int bit) {
            if (bit < 0 || bit > bits.length) {
                throw new IllegalArgumentException("bit " + bit + " is outside the " + bits.length + "-bit payload");
            }
            position = bit;
            return this;
        }

        /** Bits not read yet. */
        public int remaining() {
            return bits.length - position;
        }

        /**
         * The next {@code n} bits as an unsigned number.
         *
         * @throws IllegalArgumentException when fewer than {@code n} bits remain
         */
        public long unsigned(int n) {
            if (n < 0 || n > 63) {
                throw new IllegalArgumentException("cannot read " + n + " bits at once");
            }
            if (remaining() < n) {
                throw new IllegalArgumentException("payload ends at bit " + bits.length + ", reading " + n + " bits at "
                        + position);
            }
            long v = 0;
            for (int i = 0; i < n; i++) {
                v = (v << 1) | bits[position++];
            }
            return v;
        }

        /** The next {@code n} bits as an unsigned int. */
        public int uint(int n) {
            return (int) unsigned(n);
        }

        /** The next {@code n} bits as a two's-complement signed number. */
        public long signed(int n) {
            long v = unsigned(n);
            if (n > 0 && (v & (1L << (n - 1))) != 0) {
                v -= 1L << n;
            }
            return v;
        }

        /** The next {@code n} bits as a signed int. */
        public int sint(int n) {
            return (int) signed(n);
        }

        /** The next bit as a boolean. */
        public boolean bool() {
            return unsigned(1) == 1;
        }

        /**
         * The next {@code chars} six-bit text characters, trimmed: the text ends at the first {@code @}, and spaces
         * at both ends are removed.
         *
         * @return the text, or null when nothing is left (empty or all {@code @})
         */
        public String text(int chars) {
            StringBuilder sb = new StringBuilder(chars);
            boolean ended = false;
            for (int i = 0; i < chars; i++) {
                int v = (int) unsigned(6);
                if (v == 0) {
                    ended = true;
                }
                if (!ended) {
                    sb.append(TEXT.charAt(v));
                }
            }
            String text = sb.toString().strip();
            return text.isEmpty() ? null : text;
        }

        /** {@code n} bits from the current position without moving it, or 0 past the end (optional trailing fields). */
        public long peekOrZero(int n) {
            if (remaining() < n) {
                return 0;
            }
            int saved = position;
            long v = unsigned(n);
            position = saved;
            return v;
        }
    }

    /** Writes bit fields in order, most significant bit first, and armors them. */
    public static final class Writer {

        private byte[] bits = new byte[512];
        private int length;

        /** Creates an empty writer. */
        public Writer() {}

        /** The bits written so far. */
        public int length() {
            return length;
        }

        /** Appends the low {@code n} bits of {@code value}. */
        public Writer unsigned(int n, long value) {
            if (n < 0 || n > 63) {
                throw new IllegalArgumentException("cannot write " + n + " bits at once");
            }
            ensure(n);
            for (int i = n - 1; i >= 0; i--) {
                bits[length++] = (byte) ((value >> i) & 1);
            }
            return this;
        }

        /** Appends {@code value} as an {@code n}-bit two's-complement number (the low {@code n} bits). */
        public Writer signed(int n, long value) {
            return unsigned(n, value & ((1L << n) - 1));
        }

        /** Appends one bit. */
        public Writer bool(boolean value) {
            return unsigned(1, value ? 1 : 0);
        }

        /**
         * Appends {@code text} as {@code chars} six-bit characters: upper-cased, characters outside the alphabet
         * written as {@code ?}, cut at {@code chars}, padded with {@code @}; null writes only padding.
         */
        public Writer text(int chars, String text) {
            String t = text == null ? "" : text.toUpperCase(Locale.ROOT);
            for (int i = 0; i < chars; i++) {
                int v = 0;
                if (i < t.length()) {
                    int index = TEXT.indexOf(t.charAt(i));
                    v = index < 0 ? TEXT.indexOf('?') : index;
                }
                unsigned(6, v);
            }
            return this;
        }

        /** The bits armored, padded to whole characters with zero fill bits. */
        public Armored armor() {
            int chars = (length + 5) / 6;
            int fill = chars * 6 - length;
            StringBuilder sb = new StringBuilder(chars);
            for (int c = 0; c < chars; c++) {
                int v = 0;
                for (int b = 0; b < 6; b++) {
                    int index = c * 6 + b;
                    v = (v << 1) | (index < length ? bits[index] : 0);
                }
                sb.append(character(v));
            }
            return new Armored(sb.toString(), fill);
        }

        private void ensure(int more) {
            if (length + more > bits.length) {
                bits = Arrays.copyOf(bits, Math.max(bits.length * 2, length + more));
            }
        }
    }
}
