package ai.foresignal.source.formats.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A streaming RFC 4180 CSV reader (prompt 064, shared by the CelesTrak adapters): one row at a time, so a
 * file larger than the heap budget (CelesTrak SOCRATES, about 23 MB) is never held as rows.
 *
 * <p>Fields may be quoted; a doubled quote inside quotes is a literal quote, and quoted fields may hold commas
 * and line breaks. Rows end at CRLF, LF or CR. A UTF-8 byte-order mark at the start is skipped. Not
 * thread-safe.
 */
public final class CsvReader implements AutoCloseable {

    /** The longest field accepted, so a malformed file cannot grow one field without bound. */
    public static final int MAX_FIELD_CHARS = 64 * 1024;

    private final Reader in;
    private int pushback = -2;
    private boolean started;
    private long rows;

    /**
     * Wraps {@code in}; the caller should pass a buffered reader.
     *
     * @param in the text
     */
    public CsvReader(Reader in) {
        this.in = in;
    }

    /**
     * The next row's fields, or null at the end of the input. A blank line is returned as one empty field.
     *
     * @throws IllegalArgumentException when a field exceeds {@value #MAX_FIELD_CHARS} characters or a quote is
     *     never closed
     * @throws UncheckedIOException when reading fails
     */
    public List<String> next() {
        try {
            int c = read();
            if (!started) {
                started = true;
                if (c == '﻿') {
                    c = read();
                }
            }
            if (c == -1) {
                return null;
            }
            List<String> fields = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            boolean wasQuoted = false;
            while (true) {
                if (quoted) {
                    if (c == -1) {
                        throw new IllegalArgumentException("unclosed quote in CSV row " + (rows + 1));
                    }
                    if (c == '"') {
                        int d = read();
                        if (d == '"') {
                            append(field, '"');
                        } else {
                            quoted = false;
                            c = d;
                            continue;
                        }
                    } else {
                        append(field, (char) c);
                    }
                } else if (c == '"' && field.isEmpty() && !wasQuoted) {
                    quoted = true;
                    wasQuoted = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                    wasQuoted = false;
                } else if (c == '\r' || c == '\n' || c == -1) {
                    if (c == '\r') {
                        int d = read();
                        if (d != '\n') {
                            unread(d);
                        }
                    }
                    fields.add(field.toString());
                    rows++;
                    return fields;
                } else {
                    append(field, (char) c);
                }
                c = read();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Rows returned so far, the header included. */
    public long rows() {
        return rows;
    }

    /**
     * The column index of each header name, upper-cased and trimmed.
     *
     * @param header the header row
     */
    public static Map<String, Integer> index(List<String> header) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            index.putIfAbsent(header.get(i).strip().toUpperCase(Locale.ROOT), i);
        }
        return index;
    }

    /**
     * The value of {@code column} in {@code row}, trimmed, or null when the column is absent or the value
     * empty.
     */
    public static String get(List<String> row, Map<String, Integer> index, String column) {
        Integer i = index.get(column);
        if (i == null || i >= row.size()) {
            return null;
        }
        String v = row.get(i).strip();
        return v.isEmpty() ? null : v;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    private int read() throws IOException {
        if (pushback != -2) {
            int c = pushback;
            pushback = -2;
            return c;
        }
        return in.read();
    }

    private void unread(int c) {
        pushback = c;
    }

    private static void append(StringBuilder field, char c) {
        if (field.length() >= MAX_FIELD_CHARS) {
            throw new IllegalArgumentException("CSV field longer than " + MAX_FIELD_CHARS + " characters");
        }
        field.append(c);
    }
}
