package ai.foresignal.cli.output;

import tools.jackson.databind.JsonNode;

/**
 * One table column: a header and a dotted path into each row ({@code status}, {@code budget.daily},
 * {@code payload.mag}).
 *
 * @param header the column header
 * @param path   the dotted field path
 */
public record Column(String header, String path) {

    /**
     * A column whose header is the upper-cased path.
     *
     * @param path the dotted field path
     * @return the column
     */
    public static Column of(String path) {
        return new Column(path.toUpperCase(java.util.Locale.ROOT), path);
    }

    /**
     * Columns from dotted paths.
     *
     * @param paths the dotted field paths
     * @return the columns
     */
    public static Column[] of(String... paths) {
        Column[] columns = new Column[paths.length];
        for (int i = 0; i < paths.length; i++) {
            columns[i] = of(paths[i]);
        }
        return columns;
    }

    /**
     * Reads this column's value from a row.
     *
     * @param row the row
     * @return the node at the path ({@code MissingNode} when absent)
     */
    public JsonNode read(JsonNode row) {
        JsonNode node = row;
        for (String part : path.split("\\.")) {
            node = node.path(part);
        }
        return node;
    }
}
