/*
 * Ported from God's Eye View, server/providers/wind/gfs.js (windMessageRanges),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; the last message ends at the object size instead of throwing; adjacent messages are
 * coalesced into one range; the 8 MB per-message cap from decode.js is checked here.
 */
package ai.foresignal.raster.model;

import ai.foresignal.raster.RasterFormatException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Plans the byte ranges that read a set of GRIB2 messages from one object (prompt 051).
 *
 * <ul>
 *   <li>A message spans its own offset to the next message's offset − 1.
 *   <li>The last message ends at the object size − 1, from the S3 listing's {@code Size}; God's Eye
 *       View throws there, Foresignal does not.
 *   <li>Messages that are adjacent in the file are coalesced into one ranged GET (for example
 *       {@code UGRD} and {@code VGRD} at 10 m, which NCEP lists back to back), up to
 *       {@value #MAX_RANGE_BYTES} bytes per range.
 *   <li>A message larger than {@value #MAX_MESSAGE_BYTES} bytes is refused (the God's Eye View decode
 *       budget).
 * </ul>
 *
 * <p>Each range is charged to the source's {@code mbPerDay} in advance from these offsets: the
 * budgeted {@code getRange} (prompt 005) charges the requested length before it sends anything.
 */
public final class IdxRangePlanner {

    /** The largest single GRIB2 message read, bytes (8 MB). */
    public static final long MAX_MESSAGE_BYTES = 8L * 1024 * 1024;

    /** The largest coalesced range, bytes (16 MB). */
    public static final long MAX_RANGE_BYTES = 16L * 1024 * 1024;

    private IdxRangePlanner() {}

    /**
     * One message inside a range.
     *
     * @param message the inventory line
     * @param start the first byte, inclusive, in the object
     * @param end the last byte, inclusive, in the object
     */
    public record Span(GribIdx.Message message, long start, long end) {

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

    /** True when one of {@code wanted} is the inventory's last message, whose end needs the object size. */
    public static boolean needsObjectSize(GribIdx idx, List<GribIdx.Message> wanted) {
        return wanted.stream().anyMatch(idx::isLast);
    }

    /**
     * Plans the ranges of {@code wanted}.
     *
     * @param idx the inventory
     * @param wanted the messages to read (any order; duplicates are read once)
     * @param objectSize the object's size from the listing, or null when unknown
     * @return the plan
     * @throws IllegalArgumentException when the last message is wanted and {@code objectSize} is null
     * @throws RasterFormatException when offsets do not increase, or a message exceeds
     *     {@value #MAX_MESSAGE_BYTES} bytes or runs past the object
     */
    public static Plan plan(GribIdx idx, List<GribIdx.Message> wanted, Long objectSize) {
        List<Span> spans = new ArrayList<>();
        wanted.stream().distinct().sorted(Comparator.comparingInt(GribIdx.Message::position)).forEach(m -> {
            long end;
            if (idx.isLast(m)) {
                if (objectSize == null) {
                    throw new IllegalArgumentException("Message " + m.number() + " (" + m.label()
                            + ") is the last in the inventory; its end needs the object size");
                }
                end = objectSize - 1;
            } else {
                end = idx.next(m).orElseThrow().offset() - 1;
            }
            if (end < m.offset()) {
                throw new RasterFormatException("GRIB2 index offsets do not increase at message " + m.number() + " ("
                        + m.label() + ")");
            }
            if (objectSize != null && end >= objectSize) {
                throw new RasterFormatException("GRIB2 message " + m.number() + " (" + m.label()
                        + ") runs past the object's " + objectSize + " bytes");
            }
            Span span = new Span(m, m.offset(), end);
            if (span.length() > MAX_MESSAGE_BYTES) {
                throw new RasterFormatException("GRIB2 message " + m.number() + " (" + m.label() + ") is "
                        + span.length() + " bytes, over the " + MAX_MESSAGE_BYTES + "-byte cap");
            }
            spans.add(span);
        });
        List<Range> ranges = new ArrayList<>();
        List<Span> current = new ArrayList<>();
        for (Span span : spans) {
            if (!current.isEmpty()) {
                Span last = current.get(current.size() - 1);
                boolean adjacent = span.message().position() == last.message().position() + 1;
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
