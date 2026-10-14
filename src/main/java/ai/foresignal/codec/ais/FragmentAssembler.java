package ai.foresignal.codec.ais;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reassembles multi-sentence AIS messages (prompt 085).
 *
 * <ul>
 *   <li><b>Key:</b> {@code (talker, group, channel, fragment count)}, where the group is the TAG block's {@code g} id
 *       when the sentence has one, else the sentence's sequential id.
 *   <li><b>Order:</b> fragments may arrive in any order; the message completes when every number from 1 to the count
 *       is present. A repeated fragment number replaces the earlier one.
 *   <li><b>Timeout:</b> a group not completed within {@link #TIMEOUT} of its first fragment is dropped and counted
 *       ({@code expired}); time is the caller's receive time, never a clock read here.
 *   <li><b>Bound:</b> at most {@link #MAX_OPEN} open groups; opening one more drops the oldest (counted expired).
 * </ul>
 *
 * <p>Single-sentence messages pass straight through. Counters {@link #completed()} and {@link #expired()} back the
 * metrics {@code foresignal.ais.fragments.completed} and {@code foresignal.ais.fragments.expired}. Not thread-safe:
 * one assembler per connection.
 */
public final class FragmentAssembler {

    /** How long a group may stay incomplete. */
    public static final Duration TIMEOUT = Duration.ofSeconds(2);

    /** The most open groups. */
    public static final int MAX_OPEN = 1_000;

    /** The completed and expired counts, as a callback for metrics. */
    public interface Listener {

        /** No listener. */
        Listener NONE = new Listener() {};

        /** A multi-sentence message completed. */
        default void completed() {}

        /** {@code count} incomplete groups were dropped. */
        default void expired(int count) {}
    }

    /**
     * A complete message.
     *
     * @param talker the talker of its sentences
     * @param channel the radio channel, or empty
     * @param armored the joined payload with the last sentence's fill bits
     * @param tag the first fragment's TAG block, else the first TAG block among the fragments, or null
     * @param firstReceivedAt when the first fragment arrived
     */
    public record Assembled(String talker, String channel, SixBit.Armored armored, TagBlock tag,
            Instant firstReceivedAt) {}

    private record Key(String talker, String group, String channel, int count) {}

    private static final class Group {
        final NmeaSentence[] parts;
        final TagBlock[] tags;
        final Instant first;
        int present;

        Group(int count, Instant first) {
            this.parts = new NmeaSentence[count];
            this.tags = new TagBlock[count];
            this.first = first;
        }
    }

    private final Map<Key, Group> open = new LinkedHashMap<>();
    private final Listener listener;
    private final int maxOpen;
    private final Duration timeout;
    private long completed;
    private long expired;

    /** An assembler with the defaults and no listener. */
    public FragmentAssembler() {
        this(Listener.NONE);
    }

    /** An assembler with the defaults reporting to {@code listener}. */
    public FragmentAssembler(Listener listener) {
        this(listener, MAX_OPEN, TIMEOUT);
    }

    /**
     * An assembler with explicit bounds (tests).
     *
     * @param listener the metrics callback, or null
     * @param maxOpen the most open groups
     * @param timeout how long a group may stay incomplete
     */
    public FragmentAssembler(Listener listener, int maxOpen, Duration timeout) {
        if (maxOpen < 1) {
            throw new IllegalArgumentException("maxOpen must be positive");
        }
        this.listener = listener == null ? Listener.NONE : listener;
        this.maxOpen = maxOpen;
        this.timeout = timeout;
    }

    /**
     * Adds one sentence.
     *
     * @param sentence the sentence
     * @param tag its TAG block, or null
     * @param receivedAt when it arrived
     * @return the complete message when this sentence completes one
     */
    public Optional<Assembled> add(NmeaSentence sentence, TagBlock tag, Instant receivedAt) {
        expire(receivedAt);
        if (sentence.single()) {
            return Optional.of(new Assembled(sentence.talker(), sentence.channel(),
                    new SixBit.Armored(sentence.payload(), sentence.fillBits()), tag, receivedAt));
        }
        String groupId = tag != null && tag.group() != null ? "g:" + tag.group().id()
                : "s:" + (sentence.sequentialId() == null ? "" : sentence.sequentialId());
        Key key = new Key(sentence.talker(), groupId, sentence.channel(), sentence.fragmentCount());
        Group group = open.get(key);
        if (group == null) {
            while (open.size() >= maxOpen) {
                Iterator<Key> eldest = open.keySet().iterator();
                eldest.next();
                eldest.remove();
                expired(1);
            }
            group = new Group(sentence.fragmentCount(), receivedAt);
            open.put(key, group);
        }
        int index = sentence.fragmentNumber() - 1;
        if (group.parts[index] == null) {
            group.present++;
        }
        group.parts[index] = sentence;
        group.tags[index] = tag;
        if (group.present < group.parts.length) {
            return Optional.empty();
        }
        open.remove(key);
        StringBuilder payload = new StringBuilder();
        for (NmeaSentence part : group.parts) {
            payload.append(part.payload());
        }
        TagBlock first = group.tags[0];
        for (int i = 0; first == null && i < group.tags.length; i++) {
            first = group.tags[i];
        }
        int fill = group.parts[group.parts.length - 1].fillBits();
        completed++;
        listener.completed();
        return Optional.of(new Assembled(sentence.talker(), sentence.channel(),
                new SixBit.Armored(payload.toString(), fill), first, group.first));
    }

    /** Drops every group older than the timeout at {@code now}. */
    public void expire(Instant now) {
        Instant cutoff = now.minus(timeout);
        int dropped = 0;
        Iterator<Group> it = open.values().iterator();
        while (it.hasNext()) {
            Group g = it.next();
            if (g.first.isBefore(cutoff)) {
                it.remove();
                dropped++;
            }
        }
        if (dropped > 0) {
            expired(dropped);
        }
    }

    /** Groups waiting for fragments. */
    public int openGroups() {
        return open.size();
    }

    /** Multi-sentence messages completed so far. */
    public long completed() {
        return completed;
    }

    /** Groups dropped incomplete so far. */
    public long expired() {
        return expired;
    }

    private void expired(int count) {
        expired += count;
        listener.expired(count);
    }
}
