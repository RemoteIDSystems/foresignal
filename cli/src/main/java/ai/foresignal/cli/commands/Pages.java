package ai.foresignal.cli.commands;

import ai.foresignal.cli.ForesignalCli;
import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.Query;
import ai.foresignal.cli.output.Column;
import ai.foresignal.cli.output.OutputFormat;
import ai.foresignal.cli.output.Printer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Shared helpers of the commands: cursor paging and path segments.
 */
final class Pages {

    private Pages() {
    }

    /**
     * Percent-encodes one path segment.
     *
     * @param value the raw segment (an id, an external id with slashes)
     * @return the encoded segment
     */
    static String seg(String value) {
        return Query.component(value);
    }

    /**
     * Prints a cursor-paged list ({@code {"items": […], "nextCursor": …}}).
     *
     * <p>With {@code all}, every page is fetched by following {@code nextCursor} and the items are printed as one
     * list. Without it, {@code json} prints the page as returned, the other formats print its items, and a notice
     * names the next cursor.
     *
     * @param root    the root command
     * @param path    the API path
     * @param query   the query (without {@code cursor} when starting from the first page)
     * @param all     follow {@code nextCursor} to the end
     * @param columns the table columns
     */
    static void list(ForesignalCli root, String path, Query query, boolean all, Column... columns) {
        ApiClient api = root.api();
        Printer printer = root.printer();
        JsonNode page = api.get(path, query);
        if (!all) {
            if (printer.format() == OutputFormat.json) {
                printer.print(page);
            } else {
                printer.print(page.path("items"), columns);
            }
            String next = nextCursor(page);
            if (next != null) {
                root.notice("more results: add --cursor " + next + " (or --all)");
            }
            return;
        }
        ArrayNode items = root.json().createArrayNode();
        while (true) {
            for (JsonNode item : page.path("items")) {
                items.add(item);
            }
            String next = nextCursor(page);
            if (next == null) {
                break;
            }
            page = api.get(path, query.copy().set("cursor", next));
        }
        printer.print(items, columns);
    }

    /**
     * The {@code nextCursor} of a page.
     *
     * @param page the page
     * @return the cursor, or null on the last page
     */
    static String nextCursor(JsonNode page) {
        JsonNode next = page.path("nextCursor");
        return next.isString() && !next.asString().isEmpty() ? next.asString() : null;
    }
}
