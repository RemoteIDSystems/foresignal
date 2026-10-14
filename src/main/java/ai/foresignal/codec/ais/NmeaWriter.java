package ai.foresignal.codec.ais;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes armored AIS payloads as NMEA 0183 sentences (prompt 085), for prompt 110's NMEA sink.
 *
 * <ul>
 *   <li>Every sentence is at most {@link #MAX_SENTENCE} characters including the CR LF that ends it on the wire, so
 *       a payload longer than one sentence holds is split into fragments (at most 9).
 *   <li>A multi-sentence message carries a sequential id that cycles 0 to 9 per writer; a single sentence has none.
 *   <li>Fill bits go on the last fragment only; earlier fragments end on a whole character and say 0.
 * </ul>
 *
 * <p>Thread-safe: the sequential id is taken under a lock.
 */
public final class NmeaWriter {

    /** The longest sentence, CR LF included (NMEA 0183). */
    public static final int MAX_SENTENCE = 82;

    private final String talker;
    private int nextSequence;

    /** A writer of {@code !AIVDM} sentences. */
    public NmeaWriter() {
        this("AIVDM");
    }

    /**
     * A writer for {@code talker}.
     *
     * @param talker {@code AIVDM} (others' reports) or {@code AIVDO} (own ship)
     */
    public NmeaWriter(String talker) {
        if (!NmeaSentence.TALKERS.contains(talker)) {
            throw new IllegalArgumentException("unsupported talker " + talker);
        }
        this.talker = talker;
    }

    /** The message encoded and written on {@code channel}. */
    public List<String> write(AisMessage message, String channel) {
        return write(AisEncoder.encode(message), channel);
    }

    /**
     * Writes {@code armored} on {@code channel} ({@code A}, {@code B} or empty).
     *
     * @return the sentences without line terminators, in order
     * @throws IllegalArgumentException when the payload needs more than 9 sentences
     */
    public List<String> write(SixBit.Armored armored, String channel) {
        String ch = channel == null ? "" : channel;
        String payload = armored.payload();
        int singleRoom = room(1, null, ch);
        if (payload.length() <= singleRoom) {
            return List.of(new NmeaSentence(talker, 1, 1, null, ch, payload, armored.fillBits()).format());
        }
        int sequence = sequence();
        int room = room(NmeaSentence.MAX_FRAGMENTS, sequence, ch);
        int count = (payload.length() + room - 1) / room;
        if (count > NmeaSentence.MAX_FRAGMENTS) {
            throw new IllegalArgumentException("a payload of " + payload.length() + " characters needs " + count
                    + " sentences; at most " + NmeaSentence.MAX_FRAGMENTS + " are allowed");
        }
        List<String> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int start = i * room;
            String part = payload.substring(start, Math.min(payload.length(), start + room));
            int fill = i == count - 1 ? armored.fillBits() : 0;
            out.add(new NmeaSentence(talker, count, i + 1, sequence, ch, part, fill).format());
        }
        return out;
    }

    /** The payload characters that fit in one sentence of a {@code count}-sentence message. */
    private int room(int count, Integer sequence, String channel) {
        // "!" + talker + ",n,m,s,ch," + payload + ",f*hh" + CR LF
        String sample = "!" + talker + "," + count + "," + count + "," + (sequence == null ? "" : sequence) + ","
                + channel + "," + ",0*00";
        return MAX_SENTENCE - 2 - sample.length();
    }

    private synchronized int sequence() {
        int s = nextSequence;
        nextSequence = (nextSequence + 1) % 10;
        return s;
    }
}
