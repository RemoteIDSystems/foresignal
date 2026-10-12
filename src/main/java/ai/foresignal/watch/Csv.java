package ai.foresignal.watch;

import java.util.ArrayList;
import java.util.List;

/**
 * A small RFC 4180 CSV reader and writer for watchlist imports and exports (prompt 036): fields
 * separated by commas, optionally double-quoted, with {@code ""} for a quote inside a quoted field
 * and CRLF or LF line ends. A leading byte-order mark is ignored.
 */
final class Csv {

    private Csv() {}

    /** The rows of {@code text}, each a list of fields. */
    static List<List<String>> parse(String text) {
        List<List<String>> rows = new ArrayList<>();
        String s = text.startsWith("﻿") ? text.substring(1) : text;
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean any = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(c);
                }
                continue;
            }
            switch (c) {
                case '"' -> {
                    quoted = true;
                    any = true;
                }
                case ',' -> {
                    row.add(field.toString());
                    field.setLength(0);
                    any = true;
                }
                case '\r' -> {
                    // ignored; LF ends the row
                }
                case '\n' -> {
                    row.add(field.toString());
                    rows.add(row);
                    row = new ArrayList<>();
                    field.setLength(0);
                    any = false;
                }
                default -> {
                    field.append(c);
                    any = true;
                }
            }
        }
        if (any || field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }

    /** {@code value} as one CSV field: quoted when it holds a comma, quote or line break; empty for null. */
    static String field(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
