package ai.foresignal.cli.output;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Renders API documents in the selected {@link OutputFormat}:
 *
 * <ul>
 *   <li>{@code table}: a list becomes aligned columns (the given columns, else the scalar fields of the first row);
 *       a single object becomes a {@code FIELD  VALUE} listing; nested values are shown as compact JSON;
 *   <li>{@code json}: the document pretty-printed;
 *   <li>{@code ndjson}: one compact line per list item, or one line for an object.
 * </ul>
 */
public final class Printer {

    /** Longest cell shown in a table; longer values are cut with an ellipsis. */
    public static final int MAX_CELL = 60;

    private final PrintWriter out;
    private final OutputFormat format;
    private final JsonMapper json;

    /**
     * Creates a printer.
     *
     * @param out    standard output
     * @param format the output format
     * @param json   the JSON mapper
     */
    public Printer(PrintWriter out, OutputFormat format, JsonMapper json) {
        this.out = out;
        this.format = format;
        this.json = json;
    }

    /**
     * The selected format.
     *
     * @return the format
     */
    public OutputFormat format() {
        return format;
    }

    /**
     * Prints a document: a list as rows, an object as fields.
     *
     * @param node    the document
     * @param columns the table columns of a list (none = infer)
     */
    public void print(JsonNode node, Column... columns) {
        switch (format) {
            case json -> out.println(json.writerWithDefaultPrettyPrinter().writeValueAsString(node));
            case ndjson -> {
                if (node.isArray()) {
                    for (JsonNode item : node) {
                        out.println(json.writeValueAsString(item));
                    }
                } else if (!node.isMissingNode()) {
                    out.println(json.writeValueAsString(node));
                }
            }
            case table -> {
                if (node.isArray()) {
                    table(node, columns);
                } else if (node.isObject()) {
                    fields(node);
                } else if (!node.isMissingNode()) {
                    out.println(cell(node, Integer.MAX_VALUE));
                }
            }
            default -> throw new IllegalStateException("Unknown format " + format);
        }
        out.flush();
    }

    /**
     * Prints one item of a stream (an SSE event, a snapshot record): one compact JSON line in {@code json} and
     * {@code ndjson}, the given text line in {@code table}.
     *
     * @param node      the document for JSON formats
     * @param tableLine the line for the table format
     */
    public void streamItem(JsonNode node, String tableLine) {
        if (format == OutputFormat.table) {
            out.println(tableLine);
        } else {
            out.println(json.writeValueAsString(node));
        }
        out.flush();
    }

    /**
     * Prints plain text to standard output (an answer, a diff).
     *
     * @param text the text
     */
    public void text(String text) {
        out.println(text);
        out.flush();
    }

    /**
     * Prints rows as aligned columns.
     *
     * @param rows    the rows (a JSON array of objects)
     * @param columns the columns, or none to use the scalar fields of the first row
     */
    public void table(JsonNode rows, Column... columns) {
        Column[] cols = columns.length > 0 ? columns : infer(rows);
        if (cols.length == 0) {
            if (rows.isEmpty()) {
                out.println("(none)");
            } else {
                for (JsonNode row : rows) {
                    out.println(cell(row, Integer.MAX_VALUE));
                }
            }
            return;
        }
        List<String[]> lines = new ArrayList<>();
        String[] header = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            header[i] = cols[i].header();
        }
        lines.add(header);
        for (JsonNode row : rows) {
            String[] line = new String[cols.length];
            for (int i = 0; i < cols.length; i++) {
                line[i] = cell(cols[i].read(row), MAX_CELL);
            }
            lines.add(line);
        }
        int[] widths = new int[cols.length];
        for (String[] line : lines) {
            for (int i = 0; i < line.length; i++) {
                widths[i] = Math.max(widths[i], line[i].length());
            }
        }
        for (String[] line : lines) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < line.length; i++) {
                if (i > 0) {
                    sb.append("  ");
                }
                sb.append(line[i]);
                if (i < line.length - 1) {
                    sb.append(" ".repeat(widths[i] - line[i].length()));
                }
            }
            out.println(sb.toString().stripTrailing());
        }
        if (rows.isEmpty()) {
            out.println("(none)");
        }
    }

    private void fields(JsonNode object) {
        int width = 0;
        for (Map.Entry<String, JsonNode> e : object.properties()) {
            width = Math.max(width, e.getKey().length());
        }
        for (Map.Entry<String, JsonNode> e : object.properties()) {
            String key = e.getKey();
            out.println(key + " ".repeat(width - key.length()) + "  " + cell(e.getValue(), Integer.MAX_VALUE));
        }
    }

    private static Column[] infer(JsonNode rows) {
        if (!rows.isArray() || rows.isEmpty() || !rows.get(0).isObject()) {
            return new Column[0];
        }
        List<Column> cols = new ArrayList<>();
        for (Map.Entry<String, JsonNode> e : rows.get(0).properties()) {
            if (e.getValue().isValueNode() && cols.size() < 8) {
                cols.add(Column.of(e.getKey()));
            }
        }
        return cols.toArray(new Column[0]);
    }

    /**
     * Formats one value for a table cell.
     *
     * @param node the value
     * @param max  the longest text allowed
     * @return {@code -} for null or missing, the text of a scalar, compact JSON otherwise
     */
    public String cell(JsonNode node, int max) {
        String text;
        if (node == null || node.isMissingNode() || node.isNull()) {
            text = "-";
        } else if (node.isValueNode()) {
            text = node.asString();
        } else if (node.isArray() && allScalars(node)) {
            List<String> parts = new ArrayList<>();
            for (JsonNode item : node) {
                parts.add(item.asString());
            }
            text = String.join(",", parts);
        } else {
            text = json.writeValueAsString(node);
        }
        text = text.replace('\n', ' ').replace('\r', ' ');
        return text.length() > max ? text.substring(0, Math.max(0, max - 3)) + "..." : text;
    }

    private static boolean allScalars(JsonNode array) {
        for (JsonNode item : array) {
            if (!item.isValueNode()) {
                return false;
            }
        }
        return true;
    }
}
