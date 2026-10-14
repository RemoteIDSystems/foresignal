package ai.foresignal.codec.ais;

import java.util.Set;

/**
 * One AIS NMEA 0183 sentence (prompt 085): {@code !AIVDM}, {@code !AIVDO} or {@code !BSVDM}, for example
 * {@code !AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0*4A}.
 *
 * @param talker the talker and formatter without the {@code !}: {@code AIVDM}, {@code AIVDO} or {@code BSVDM}
 * @param fragmentCount the sentences of the message, 1 to 9
 * @param fragmentNumber this sentence's number, 1 to {@code fragmentCount}
 * @param sequentialId the sequential message id 0 to 9 linking the fragments, or null (single sentences)
 * @param channel the radio channel ({@code A}, {@code B}, {@code 1}, {@code 2}), or empty
 * @param payload the armored payload of this fragment
 * @param fillBits the fill bits, 0 to 5
 */
public record NmeaSentence(String talker, int fragmentCount, int fragmentNumber, Integer sequentialId, String channel,
        String payload, int fillBits) {

    /** The talkers accepted. */
    public static final Set<String> TALKERS = Set.of("AIVDM", "AIVDO", "BSVDM");

    /** The most fragments of one message. */
    public static final int MAX_FRAGMENTS = 9;

    /** Checks the field ranges. */
    public NmeaSentence {
        if (!TALKERS.contains(talker)) {
            throw new IllegalArgumentException("unsupported talker " + talker);
        }
        if (fragmentCount < 1 || fragmentCount > MAX_FRAGMENTS || fragmentNumber < 1 || fragmentNumber > fragmentCount) {
            throw new IllegalArgumentException("fragment " + fragmentNumber + " of " + fragmentCount);
        }
        if (sequentialId != null && (sequentialId < 0 || sequentialId > 9)) {
            throw new IllegalArgumentException("sequential id " + sequentialId);
        }
        if (fillBits < 0 || fillBits > 5) {
            throw new IllegalArgumentException("fill bits " + fillBits);
        }
        channel = channel == null ? "" : channel;
        payload = payload == null ? "" : payload;
    }

    /** The outcome of {@link #parse}. */
    public sealed interface Result {}

    /** A valid sentence. */
    public record Parsed(NmeaSentence sentence) implements Result {}

    /** A sentence whose checksum does not match: dropped and counted. */
    public record BadChecksum(String line) implements Result {}

    /**
     * Anything else: dropped and counted.
     *
     * @param reason why
     */
    public record Malformed(String reason) implements Result {}

    /**
     * Parses one sentence (without a TAG block). Trailing whitespace is ignored.
     *
     * @return the sentence, {@link BadChecksum} or {@link Malformed}; never throws
     */
    public static Result parse(String line) {
        if (line == null) {
            return new Malformed("empty");
        }
        String s = line.strip();
        if (s.length() < 2 || s.charAt(0) != '!') {
            return new Malformed("not an encapsulated sentence");
        }
        int star = s.lastIndexOf('*');
        if (star < 0 || star + 3 != s.length()) {
            return new Malformed("no checksum");
        }
        int expected;
        try {
            expected = Integer.parseInt(s.substring(star + 1), 16);
        } catch (NumberFormatException e) {
            return new Malformed("checksum not hexadecimal");
        }
        String body = s.substring(1, star);
        if (checksum(body) != expected) {
            return new BadChecksum(s);
        }
        String[] f = body.split(",", -1);
        if (f.length != 7) {
            return new Malformed("expected 7 fields, found " + f.length);
        }
        if (!TALKERS.contains(f[0])) {
            return new Malformed("unsupported talker " + f[0]);
        }
        try {
            int count = Integer.parseInt(f[1]);
            int number = Integer.parseInt(f[2]);
            Integer seq = f[3].isEmpty() ? null : Integer.valueOf(f[3]);
            int fill = f[6].isEmpty() ? 0 : Integer.parseInt(f[6]);
            for (int i = 0; i < f[5].length(); i++) {
                SixBit.value(f[5].charAt(i));
            }
            return new Parsed(new NmeaSentence(f[0], count, number, seq, f[4], f[5], fill));
        } catch (IllegalArgumentException e) {
            return new Malformed(e.getMessage());
        }
    }

    /** The sentence as text, {@code !AIVDM,…*hh}, with a computed checksum. */
    public String format() {
        String body = talker + "," + fragmentCount + "," + fragmentNumber + ","
                + (sequentialId == null ? "" : sequentialId) + "," + channel + "," + payload + "," + fillBits;
        return "!" + body + "*" + String.format("%02X", checksum(body));
    }

    /** True when this sentence is the whole message. */
    public boolean single() {
        return fragmentCount == 1;
    }

    /** The NMEA checksum: the XOR of every character of {@code body}. */
    public static int checksum(String body) {
        int sum = 0;
        for (int i = 0; i < body.length(); i++) {
            sum ^= body.charAt(i);
        }
        return sum & 0xFF;
    }
}
