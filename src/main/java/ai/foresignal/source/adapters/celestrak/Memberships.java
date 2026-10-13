package ai.foresignal.source.adapters.celestrak;

import ai.foresignal.source.spi.AdapterStateStore;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * CelesTrak group memberships in the adapter state (prompt 064): {@code members:{group}} holds the group's
 * catalog numbers as a sorted, delta-encoded list ({@code 25544,10542,…}: the first number, then each
 * difference) and {@code membersAt:{group}} the time it was fetched. Each member group is fetched about once a
 * day, at most {@value #MAX_PER_RUN} per run, oldest first.
 */
final class Memberships {

    /** The state key prefix of a member list. */
    static final String MEMBERS_PREFIX = "members:";

    /** The state key prefix of a member list's fetch time. */
    static final String AT_PREFIX = "membersAt:";

    /** How old a membership may get before it is fetched again. */
    static final Duration MAX_AGE = Duration.ofHours(24);

    /** The most member groups fetched in one run. */
    static final int MAX_PER_RUN = 2;

    private Memberships() {}

    /**
     * The member groups to fetch this run: those never fetched or fetched more than {@link #MAX_AGE} ago,
     * oldest first (never-fetched groups first, in configured order), at most {@value #MAX_PER_RUN}.
     */
    static List<String> due(AdapterStateStore state, List<String> groups, Instant now) {
        List<String> never = new ArrayList<>();
        TreeMap<Instant, List<String>> stale = new TreeMap<>();
        for (String group : groups) {
            Optional<Instant> at = fetchedAt(state, group);
            if (at.isEmpty()) {
                never.add(group);
            } else if (!at.get().plus(MAX_AGE).isAfter(now)) {
                stale.computeIfAbsent(at.get(), k -> new ArrayList<>()).add(group);
            }
        }
        List<String> out = new ArrayList<>(never);
        stale.values().forEach(out::addAll);
        return out.size() <= MAX_PER_RUN ? out : out.subList(0, MAX_PER_RUN);
    }

    /** When {@code group}'s membership was last fetched. */
    static Optional<Instant> fetchedAt(AdapterStateStore state, String group) {
        Optional<String> text = state.get(AT_PREFIX + group);
        if (text.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(text.get()));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /** Stores {@code group}'s members and the fetch time. */
    static void store(AdapterStateStore state, String group, Collection<Long> members, Instant at) {
        state.put(MEMBERS_PREFIX + group, encode(members));
        state.put(AT_PREFIX + group, at.toString());
    }

    /** Marks {@code group}'s stored membership as current without changing it (CelesTrak said "not updated"). */
    static void touch(AdapterStateStore state, String group, Instant at) {
        state.put(AT_PREFIX + group, at.toString());
    }

    /** {@code group}'s stored members. */
    static TreeSet<Long> members(AdapterStateStore state, String group) {
        return decode(state.get(MEMBERS_PREFIX + group).orElse(""));
    }

    /**
     * The sorted groups each catalog number belongs to, over {@code groups}.
     *
     * @return catalog number → sorted group names
     */
    static Map<Long, List<String>> index(AdapterStateStore state, List<String> groups) {
        Map<Long, TreeSet<String>> sets = new TreeMap<>();
        for (String group : groups) {
            for (long norad : members(state, group)) {
                sets.computeIfAbsent(norad, k -> new TreeSet<>(Comparator.naturalOrder())).add(group);
            }
        }
        Map<Long, List<String>> out = new TreeMap<>();
        sets.forEach((k, v) -> out.put(k, List.copyOf(v)));
        return out;
    }

    /** Sorted, delta-encoded: the first number, then each difference to the previous one. */
    static String encode(Collection<Long> members) {
        StringBuilder b = new StringBuilder();
        long previous = 0;
        for (long n : new TreeSet<>(members)) {
            if (!b.isEmpty()) {
                b.append(',');
            }
            b.append(n - previous);
            previous = n;
        }
        return b.toString();
    }

    /** The inverse of {@link #encode}; malformed entries are ignored. */
    static TreeSet<Long> decode(String text) {
        TreeSet<Long> out = new TreeSet<>();
        if (text == null || text.isBlank()) {
            return out;
        }
        long value = 0;
        for (String part : text.split(",")) {
            try {
                value += Long.parseLong(part.strip());
                out.add(value);
            } catch (NumberFormatException ignored) {
                // A damaged entry: skip it rather than lose the whole membership.
            }
        }
        return out;
    }
}
