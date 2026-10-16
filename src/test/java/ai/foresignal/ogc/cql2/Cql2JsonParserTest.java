package ai.foresignal.ogc.cql2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.ogc.cql2.Cql2.Bbox;
import ai.foresignal.ogc.cql2.Cql2.CompareOp;
import ai.foresignal.ogc.cql2.Cql2.Comparison;
import ai.foresignal.ogc.cql2.Cql2.Expr;
import ai.foresignal.ogc.cql2.Cql2.Num;
import ai.foresignal.ogc.cql2.Cql2.Point;
import ai.foresignal.ogc.cql2.Cql2.Property;
import ai.foresignal.ogc.cql2.Cql2.SIntersects;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class Cql2JsonParserTest {

    static Stream<Arguments> equivalent() {
        return Stream.of(
                Arguments.of("mag > 5", "{\"op\":\">\",\"args\":[{\"property\":\"mag\"},5]}"),
                Arguments.of("mag <= 5.0", "{\"op\":\"<=\",\"args\":[{\"property\":\"mag\"},5]}"),
                Arguments.of("5 < mag", "{\"op\":\"<\",\"args\":[5,{\"property\":\"mag\"}]}"),
                Arguments.of("name <> 'x'", "{\"op\":\"<>\",\"args\":[{\"property\":\"name\"},\"x\"]}"),
                Arguments.of("ok = true", "{\"op\":\"=\",\"args\":[{\"property\":\"ok\"},true]}"),
                Arguments.of("observedAt >= TIMESTAMP('2026-10-16T12:00:00Z')",
                        "{\"op\":\">=\",\"args\":[{\"property\":\"observedAt\"},{\"timestamp\":\"2026-10-16T12:00:00Z\"}]}"),
                Arguments.of("d = DATE('2026-10-16')",
                        "{\"op\":\"=\",\"args\":[{\"property\":\"d\"},{\"date\":\"2026-10-16\"}]}"),
                Arguments.of("depth IS NULL", "{\"op\":\"isNull\",\"args\":[{\"property\":\"depth\"}]}"),
                Arguments.of("depth IS NOT NULL",
                        "{\"op\":\"not\",\"args\":[{\"op\":\"isNull\",\"args\":[{\"property\":\"depth\"}]}]}"),
                Arguments.of("a = 1 AND (b = 2 OR c = 3)",
                        "{\"op\":\"and\",\"args\":[{\"op\":\"=\",\"args\":[{\"property\":\"a\"},1]},"
                                + "{\"op\":\"or\",\"args\":[{\"op\":\"=\",\"args\":[{\"property\":\"b\"},2]},"
                                + "{\"op\":\"=\",\"args\":[{\"property\":\"c\"},3]}]}]}"),
                Arguments.of("a = 1 AND b = 2 AND c = 3",
                        "{\"op\":\"and\",\"args\":[{\"op\":\"and\",\"args\":[{\"op\":\"=\",\"args\":[{\"property\":\"a\"},1]},"
                                + "{\"op\":\"=\",\"args\":[{\"property\":\"b\"},2]}]},"
                                + "{\"op\":\"=\",\"args\":[{\"property\":\"c\"},3]}]}"),
                Arguments.of("S_INTERSECTS(geometry, POINT(-97.7 30.3))",
                        "{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},"
                                + "{\"type\":\"Point\",\"coordinates\":[-97.7,30.3]}]}"),
                Arguments.of("S_INTERSECTS(geometry, BBOX(-98,30,-97,31))",
                        "{\"op\":\"s_intersects\",\"args\":[{\"bbox\":[-98,30,-97,31]},{\"property\":\"geometry\"}]}"),
                Arguments.of("S_INTERSECTS(geometry, POLYGON((0 0, 1 0, 1 1, 0 0)))",
                        "{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},"
                                + "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,0]]]}]}"));
    }

    @ParameterizedTest
    @MethodSource("equivalent")
    @DisplayName("the text and JSON forms of a filter give equal trees")
    void sameTree(String text, String json) {
        assertThat(Cql2JsonParser.parse(json)).isEqualTo(Cql2TextParser.parse(text));
    }

    @Test
    @DisplayName("comparisons, spatial literals and property positions as JSON pointers")
    void parses() {
        Expr e = Cql2JsonParser.parse("{\"op\":\">\",\"args\":[{\"property\":\"mag\"},5]}");
        assertThat(e).isEqualTo(new Comparison(CompareOp.GT, new Property("mag"), new Num(BigDecimal.valueOf(5))));
        assertThat(((Comparison) e).property().position()).isEqualTo("/args/0/property");
        assertThat(Cql2JsonParser.parse("{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},"
                + "{\"type\":\"Point\",\"coordinates\":[1,2]}]}"))
                .isEqualTo(new SIntersects(new Property("geometry"), new Point(1, 2)));
        assertThat(Cql2JsonParser.parse("{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},"
                + "{\"bbox\":[1,2,3,4]}]}")).isEqualTo(new SIntersects(new Property("geometry"), new Bbox(1, 2, 3, 4)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not json", "[]", "{\"args\":[]}", "{\"op\":\"=\"}", "{\"op\":\"like\",\"args\":[1,2]}",
        "{\"op\":\"=\",\"args\":[{\"property\":\"a\"}]}", "{\"op\":\"=\",\"args\":[1,2]}",
        "{\"op\":\"=\",\"args\":[{\"property\":\"a\"},null]}", "{\"op\":\"and\",\"args\":[{\"op\":\"=\",\"args\":[{\"property\":\"a\"},1]}]}",
        "{\"op\":\"isNull\",\"args\":[5]}", "{\"op\":\"=\",\"args\":[{\"property\":\"a\"},{\"timestamp\":\"x\"}]}",
        "{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},{\"type\":\"LineString\",\"coordinates\":[]}]}",
        "{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},{\"bbox\":[1,2,3]}]}",
        "{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1]]]}]}",
        "{\"op\":\"=\",\"args\":[{\"property\":\"a\"},{\"op\":\"+\",\"args\":[1,2]}]}"})
    @DisplayName("unsupported or malformed CQL2-JSON is refused with a pointer")
    void errors(String json) {
        assertThatThrownBy(() -> Cql2JsonParser.parse(json)).isInstanceOf(Cql2Exception.class)
                .satisfies(e -> assertThat(((Cql2Exception) e).position()).isNotNull());
    }

    @Test
    @DisplayName("the pointer names the offending argument")
    void pointer() {
        assertThatThrownBy(() -> Cql2JsonParser.parse("{\"op\":\"and\",\"args\":[{\"op\":\"=\",\"args\":[{\"property\":\"a\"},1]},"
                + "{\"op\":\"~\",\"args\":[1,2]}]}"))
                .isInstanceOf(Cql2Exception.class)
                .satisfies(e -> assertThat(((Cql2Exception) e).position()).isEqualTo("/args/1/op"));
    }

    @Test
    @DisplayName("depth and node limits apply to JSON as to text")
    void limits() {
        String deep = "{\"op\":\"not\",\"args\":[".repeat(25) + "{\"op\":\"=\",\"args\":[{\"property\":\"a\"},1]}"
                + "]}".repeat(25);
        assertThatThrownBy(() -> Cql2JsonParser.parse(deep)).isInstanceOf(Cql2Exception.class).hasMessageContaining("20");
        StringBuilder many = new StringBuilder("{\"op\":\"or\",\"args\":[");
        for (int i = 0; i < 80; i++) {
            many.append(i == 0 ? "" : ",").append("{\"op\":\"=\",\"args\":[{\"property\":\"a\"},").append(i).append("]}");
        }
        many.append("]}");
        assertThatThrownBy(() -> Cql2JsonParser.parse(many.toString())).isInstanceOf(Cql2Exception.class)
                .hasMessageContaining("200");
    }
}
