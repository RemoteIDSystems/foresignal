package ai.foresignal.source.formats.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link CsvReader}: RFC 4180 quoting, line endings, the BOM and the field cap (prompt 064). */
class CsvReaderTest {

    @Test
    @DisplayName("quoted fields keep commas, doubled quotes and line breaks; CRLF, LF and CR end rows")
    void rfc4180() {
        CsvReader csv = new CsvReader(new StringReader("﻿a,b,c\r\n\"x,1\",\"say \"\"hi\"\"\",\"two\nlines\"\nlast,,\r"));
        assertThat(csv.next()).containsExactly("a", "b", "c");
        assertThat(csv.next()).containsExactly("x,1", "say \"hi\"", "two\nlines");
        assertThat(csv.next()).containsExactly("last", "", "");
        assertThat(csv.next()).isNull();
        assertThat(csv.rows()).isEqualTo(3);
    }

    @Test
    @DisplayName("the index is upper-cased and get trims and returns null for empty or missing columns")
    void index() {
        Map<String, Integer> index = CsvReader.index(List.of(" Name ", "VALUE"));
        assertThat(index).containsEntry("NAME", 0).containsEntry("VALUE", 1);
        assertThat(CsvReader.get(List.of(" a ", ""), index, "NAME")).isEqualTo("a");
        assertThat(CsvReader.get(List.of(" a ", ""), index, "VALUE")).isNull();
        assertThat(CsvReader.get(List.of(" a "), index, "MISSING")).isNull();
    }

    @Test
    @DisplayName("an unclosed quote and an over-long field are refused")
    void limits() {
        assertThatThrownBy(() -> new CsvReader(new StringReader("\"open")).next())
                .isInstanceOf(IllegalArgumentException.class);
        String huge = "x".repeat(CsvReader.MAX_FIELD_CHARS + 1);
        assertThatThrownBy(() -> new CsvReader(new StringReader(huge)).next())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
