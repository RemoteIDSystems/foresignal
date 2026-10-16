/*
 * Ported from God's Eye View, server/providers/wind/gfs.js (parseGfsIdx),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; the date and time-range fields are kept; field lookup by variable, level and
 * optional time range.
 */
package ai.foresignal.raster.model;

import ai.foresignal.raster.RasterFormatException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An NCEP GRIB2 {@code .idx} inventory (prompt 051, Weather-Imagery-Design §1.4): one line per
 * message, {@code msg:offset:d=YYYYMMDDHH:VAR:LEVEL:TIME:…}, in file order.
 *
 * <p>Malformed lines are skipped; a body with no valid line is refused, so an HTML error page is
 * never read as an empty inventory. Fields are found by variable, level and, for accumulations, the
 * time-range field ({@code 0-6 hour acc fcst}); <strong>never by message number</strong>, which shifts
 * whenever NCEP adds a field.
 *
 * @param messages the valid lines, in file order
 */
public record GribIdx(List<Message> messages) {

    private static final Pattern DIGITS = Pattern.compile("^\\d+$");

    /** Copies the messages. */
    public GribIdx {
        messages = List.copyOf(messages);
    }

    /**
     * One inventory line.
     *
     * @param number the message number (1-based, as listed)
     * @param offset the message's byte offset in the GRIB2 object
     * @param date the {@code d=} reference date field without its prefix, for example
     *     {@code 2026101012}, or empty
     * @param variable the variable, for example {@code UGRD}
     * @param level the level, for example {@code 10 m above ground}
     * @param timeRange the time field, for example {@code 6 hour fcst}, {@code anl} or
     *     {@code 0-6 hour acc fcst}; empty when the line has none
     * @param position the line's index in {@link #messages()}
     */
    public record Message(int number, long offset, String date, String variable, String level, String timeRange,
            int position, String extra) {

        /** A line without the trailing fields (the v1 shape). */
        public Message(int number, long offset, String date, String variable, String level, String timeRange,
                int position) {
            this(number, offset, date, variable, level, timeRange, position, "");
        }

        /** {@code VAR:LEVEL}, as health reasons name a field. */
        public String label() {
            return variable + ":" + level;
        }

        /** True when the line is a probability forecast ({@code prob >X}). */
        public boolean probabilistic() {
            return extra != null && extra.contains("prob");
        }
    }

    /**
     * Parses an inventory.
     *
     * @param text the {@code .idx} body
     * @return the inventory
     * @throws RasterFormatException when no line is valid
     */
    public static GribIdx parse(String text) {
        List<Message> out = new ArrayList<>();
        if (text != null) {
            for (String line : text.split("\\r?\\n")) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(":", -1);
                if (parts.length < 6 || !DIGITS.matcher(parts[0]).matches() || !DIGITS.matcher(parts[1]).matches()
                        || parts[3].isEmpty() || parts[4].isEmpty()) {
                    continue;
                }
                long offset;
                int number;
                try {
                    number = Integer.parseInt(parts[0]);
                    offset = Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    continue;
                }
                String date = parts[2].startsWith("d=") ? parts[2].substring(2) : parts[2];
                String extra = parts.length > 6 ? String.join(":", java.util.Arrays.copyOfRange(parts, 6, parts.length))
                        : "";
                out.add(new Message(number, offset, date, parts[3], parts[4], parts[5], out.size(), extra));
            }
        }
        if (out.isEmpty()) {
            throw new RasterFormatException("malformed GRIB2 index: no valid inventory line");
        }
        return new GribIdx(out);
    }

    /**
     * The first message of {@code variable} at {@code level} whose time range satisfies
     * {@code selector}; the first match wins when a field is listed twice (GFS lists
     * {@code APCP:surface:0-6 hour acc fcst} twice at f006, verified 2026-10).
     *
     * @return the message, or empty when the inventory lacks the field
     */
    public Optional<Message> find(FieldSelector selector) {
        for (Message m : messages) {
            if (selector.matches(m)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    /** The message after {@code message} in file order, or empty for the last one. */
    public Optional<Message> next(Message message) {
        int i = message.position() + 1;
        return i < messages.size() ? Optional.of(messages.get(i)) : Optional.empty();
    }

    /** True when {@code message} is the last line. */
    public boolean isLast(Message message) {
        return message.position() == messages.size() - 1;
    }

    /** The messages, unmodifiable (for tests and probes). */
    public List<Message> all() {
        return Collections.unmodifiableList(messages);
    }
}
