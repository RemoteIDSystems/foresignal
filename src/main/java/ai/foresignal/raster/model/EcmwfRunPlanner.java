package ai.foresignal.raster.model;

import ai.foresignal.raster.RasterFormatException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Plans the byte ranges that read a set of ECMWF messages from one step file (prompt 105), the
 * counterpart of {@link IdxRangePlanner} for the JSON {@code .index} format.
 *
 * <ul>
 *   <li>An ECMWF entry carries its own {@code _offset} and {@code _length}, so a message's span is
 *       {@code [_offset, _offset + _length − 1]} and the object size is never needed.
 *   <li>Entries consecutive in file order are coalesced into one ranged GET, up to
 *       {@value #MAX_RANGE_BYTES} bytes.
 *   <li>A message larger than {@value #MAX_MESSAGE_BYTES} bytes is refused.
 * </ul>
 *
 * <p>Each range is charged to the source's {@code mbPerDay} in advance by the budgeted
 * {@code getRange} (prompt 005).
 */
public final class EcmwfRunPlanner {

    /** The largest single message read, bytes (8 MB). */
    public static final long MAX_MESSAGE_BYTES = 8L * 1024 * 1024;

    /** The largest coalesced range, bytes (16 MB). */
    public static final long MAX_RANGE_BYTES = 16L * 1024 * 1024;

    private EcmwfRunPlanner() {}

    /**
     * One message inside a range.
     *
     * @param entry the inventory line
     * @param start the first byte, inclusive
     * @param end the last byte, inclusive
     */
    public record Span(EcmwfIndex.Entry entry, long start, long end) {

        /** The message length, bytes. */
        public long length() {
            return end - start + 1;
        }
    }

    /**
     * One ranged GET.
     *
     * @param start the first byte, inclusive
     * @param end the last byte, inclusive
     * @param spans the messages it holds, in file order
     */
    public record Range(long start, long end, List<Span> spans) {

        /** Copies the spans. */
        public Range {
            spans = List.copyOf(spans);
        }

        /** The range length, bytes. */
        public long length() {
            return end - start + 1;
        }
    }

    /**
     * A plan.
     *
     * @param ranges the ranged GETs, in file order
     */
    public record Plan(List<Range> ranges) {

        /** Copies the ranges. */
        public Plan {
            ranges = List.copyOf(ranges);
        }

        /** The bytes the plan charges against {@code mbPerDay}. */
        public long chargeBytes() {
            return ranges.stream().mapToLong(Range::length).sum();
        }

        /** The number of requests. */
        public int requests() {
            return ranges.size();
        }
    }

    /**
     * Plans the ranges of {@code wanted}.
     *
     * @param wanted the entries to read (any order; duplicates are read once)
     * @return the plan
     * @throws RasterFormatException when an entry's length exceeds {@value #MAX_MESSAGE_BYTES}
     */
    public static Plan plan(List<EcmwfIndex.Entry> wanted) {
        List<Span> spans = new ArrayList<>();
        wanted.stream().distinct().sorted(Comparator.comparingInt(EcmwfIndex.Entry::position)).forEach(e -> {
            Span span = new Span(e, e.offset(), e.end());
            if (span.length() > MAX_MESSAGE_BYTES) {
                throw new RasterFormatException("ECMWF message " + e.label() + " is " + span.length()
                        + " bytes, over the " + MAX_MESSAGE_BYTES + "-byte cap");
            }
            spans.add(span);
        });
        List<Range> ranges = new ArrayList<>();
        List<Span> current = new ArrayList<>();
        for (Span span : spans) {
            if (!current.isEmpty()) {
                Span last = current.get(current.size() - 1);
                boolean adjacent = span.entry().position() == last.entry().position() + 1;
                boolean fits = span.end() - current.get(0).start() + 1 <= MAX_RANGE_BYTES;
                if (!adjacent || !fits) {
                    ranges.add(new Range(current.get(0).start(), last.end(), current));
                    current = new ArrayList<>();
                }
            }
            current.add(span);
        }
        if (!current.isEmpty()) {
            ranges.add(new Range(current.get(0).start(), current.get(current.size() - 1).end(), current));
        }
        return new Plan(ranges);
    }
}
