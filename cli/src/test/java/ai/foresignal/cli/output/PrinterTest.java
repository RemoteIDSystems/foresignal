package ai.foresignal.cli.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Table, JSON and NDJSON rendering. */
class PrinterTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final StringWriter buffer = new StringWriter();

    private Printer printer(OutputFormat format) {
        return new Printer(new PrintWriter(buffer, true), format, json);
    }

    @Test
    void infersColumnsFromTheScalarFieldsOfTheFirstRow() {
        JsonNode rows = json.readTree("[{\"id\":\"a\",\"n\":1,\"nested\":{\"x\":1}},{\"id\":\"bb\",\"n\":22}]");
        printer(OutputFormat.table).print(rows);
        assertEquals("ID  N\na   1\nbb  22\n", buffer.toString().replace("\r\n", "\n"));
    }

    @Test
    void readsDottedPathsAndFormatsArraysAndLongValues() {
        JsonNode rows = json.readTree("[{\"budget\":{\"daily\":5},\"kinds\":[\"a\",\"b\"],\"text\":\""
                + "x".repeat(80) + "\"}]");
        printer(OutputFormat.table).print(rows, Column.of("budget.daily"), Column.of("kinds"), Column.of("text"));
        String[] lines = buffer.toString().split("\\R");
        assertEquals("BUDGET.DAILY  KINDS  TEXT", lines[0]);
        assertTrue(lines[1].startsWith("5             a,b    xxx"), lines[1]);
        assertTrue(lines[1].endsWith("..."), lines[1]);
        assertEquals(Printer.MAX_CELL, lines[1].substring(lines[1].indexOf("xxx")).length());
    }

    @Test
    void anEmptyListSaysNone() {
        printer(OutputFormat.table).print(json.readTree("[]"), Column.of("id"));
        assertTrue(buffer.toString().contains("(none)"));
    }

    @Test
    void ndjsonOfAnObjectIsOneLineAndOfAMissingNodeNothing() {
        Printer p = printer(OutputFormat.ndjson);
        p.print(json.readTree("{\"a\":[1,2]}"));
        p.print(tools.jackson.databind.node.MissingNode.getInstance());
        assertEquals("{\"a\":[1,2]}\n", buffer.toString().replace("\r\n", "\n"));
    }

    @Test
    void streamItemsAreCompactJsonOutsideTables() {
        printer(OutputFormat.json).streamItem(json.readTree("{\"event\":\"change\"}"), "ignored");
        printer(OutputFormat.table).streamItem(json.readTree("{}"), "change 1");
        assertEquals("{\"event\":\"change\"}\nchange 1\n", buffer.toString().replace("\r\n", "\n"));
    }

    @Test
    void cellsShowNullsAsDashAndFlattenNewlines() {
        Printer p = printer(OutputFormat.table);
        assertEquals("-", p.cell(json.readTree("null"), 10));
        assertEquals("a b", p.cell(json.readTree("\"a\\nb\""), 10));
        assertEquals("true", p.cell(json.readTree("true"), 10));
    }
}
