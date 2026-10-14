package ai.foresignal.source.adapters.radio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.JsonNode;

/**
 * {@link LenientJsLiteralParser} and {@link KiwiSdrParser} (prompt 082): comments outside strings, trailing commas,
 * the {@code var} assignment, single quotes and unquoted names, the size cap, and the real {@code kiwisdr_com.js}
 * capture (trimmed to 30 receivers, header comments and the closing {@code },]} kept).
 */
class LenientJsLiteralParserTest {

    @Test
    @DisplayName("comments are removed outside strings only; trailing commas and the assignment are removed")
    void lenient() {
        JsonNode node = LenientJsLiteralParser.parse("// head\nvar x = /* note */ [ {'a': \"//kept\", b: \"/*kept*/\","
                + " c: [1, 2,],}, ];\n");

        assertThat(node.isArray()).isTrue();
        assertThat(node.size()).isEqualTo(1);
        assertThat(node.get(0).get("a").asString()).isEqualTo("//kept");
        assertThat(node.get(0).get("b").asString()).isEqualTo("/*kept*/");
        assertThat(node.get(0).get("c").size()).isEqualTo(2);
        assertThat(LenientJsLiteralParser.parse("{\"q\": \"a\\\"b, ]\"}").get("q").asString()).isEqualTo("a\"b, ]");
        assertThat(LenientJsLiteralParser.parse("const y = 5;").asInt()).isEqualTo(5);
    }

    @Test
    @DisplayName("the helpers: an escaped quote does not end a string; a comma before a bracket inside a string is kept")
    void helpers() {
        assertThat(LenientJsLiteralParser.stripComments("'it\\'s // here' // gone")).isEqualTo("'it\\'s // here' ");
        assertThat(LenientJsLiteralParser.removeTrailingCommas("[\"a,]\", 1 ,\n ]")).isEqualTo("[\"a,]\", 1 \n ]");
        assertThat(LenientJsLiteralParser.stripAssignment("let z = {} ;;")).isEqualTo("{}");
    }

    @Test
    @DisplayName("empty, oversized, assignment without '=' and broken literals throw UpstreamFormatException")
    void errors() {
        assertThatThrownBy(() -> LenientJsLiteralParser.parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LenientJsLiteralParser.parse(new byte[LenientJsLiteralParser.MAX_BYTES + 1]))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("cap");
        assertThatThrownBy(() -> LenientJsLiteralParser.parse("var x [1]")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LenientJsLiteralParser.parse("var x = [1, {"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LenientJsLiteralParser.parse("// only a comment"))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the real kiwisdr_com.js: 30 receivers with position, links, range, capacity and online flag")
    void realFixture() {
        KiwiSdrParser.Result result = KiwiSdrParser.parse(AdapterFixtures.bytes("kiwisdr", "kiwisdr_com.js"));

        assertThat(result.records()).hasSize(30);
        assertThat(result.entries()).isEqualTo(30);
        assertThat(result.skipped()).isEmpty();
        NormalizedRecord first = result.records().getFirst();
        assertThat(first.externalId()).isEqualTo("7804738341e2");
        assertThat(first.observedAt()).isNull();
        assertThat(first.payload()).containsEntry("name", "SA4BNA 0-32 MHZ SDR 1, Arvika")
                .containsEntry("url", "http://sa4bna.hopto.org:8073")
                .containsEntry("antenna", "100 mtr beverage, end terminated")
                .containsEntry("bandsHz", List.of(0L, 30_000_000L)).containsEntry("usersMax", 8)
                .containsEntry("online", true).containsEntry("users", 2).containsEntry("snr", List.of(48, 44));
        assertThat(((Point) first.geometry()).getY()).isEqualTo(59.546);
        assertThat(((Point) first.geometry()).getX()).isEqualTo(12.526);
        assertThat(result.records()).allSatisfy(r -> assertThat(r.geometry()).isNotNull());
        FieldSpecContract.assertPayloadKeysDeclared(RadioTestAdapters.kiwi("").descriptor(), result.records());
    }

    @Test
    @DisplayName("the hand-made list: comments and trailing commas, a receiver without position, missing and repeated ids")
    void edgeFixture() {
        KiwiSdrParser.Result result = KiwiSdrParser.parse(AdapterFixtures.bytes("kiwisdr", "edge.js"));

        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("aaaa00000001",
                "aaaa00000002");
        assertThat(result.skipped()).containsEntry(KiwiSdrParser.MISSING_ID, 1).containsEntry(KiwiSdrParser.DUPLICATE, 1);
        assertThat(result.records().get(0).payload()).containsEntry("name", "Test receiver // not a comment")
                .containsEntry("antenna", "Dipole /* not a comment either */")
                .containsEntry("url", "http://example.org:8073");
        assertThat(result.records().get(1).geometry()).isNull();
        assertThat(result.records().get(1).payload()).containsEntry("online", false)
                .doesNotContainKeys("bandsHz", "usersMax");
        FieldSpecContract.assertPayloadKeysDeclared(RadioTestAdapters.kiwi("").descriptor(), result.records());
    }

    @Test
    @DisplayName("a list that is not an array throws; users and snr changes do not change the static hash")
    void shapeAndHash() {
        assertThatThrownBy(() -> KiwiSdrParser.parse("var k = {};".getBytes())).isInstanceOf(UpstreamFormatException.class);
        NormalizedRecord a = KiwiSdrParser.parse(("[{\"id\":\"x1\",\"name\":\"R\",\"users\":\"1\",\"snr\":\"10,9\","
                + "\"gps\":\"(1, 2)\"}]").getBytes()).records().getFirst();
        NormalizedRecord b = KiwiSdrParser.parse(("[{\"id\":\"x1\",\"name\":\"R\",\"users\":\"3\",\"snr\":\"12,11\","
                + "\"gps\":\"(1, 2)\"}]").getBytes()).records().getFirst();

        SnapshotContract.assertStableStaticHash(RadioTestAdapters.kiwi("").descriptor(), List.of(a, b));
        assertThat(KiwiSdrParser.gps("(95, 0)")).isNull();
        assertThat(KiwiSdrParser.range("30-10")).isNull();
        assertThat(KiwiSdrParser.integers("1,x")).isNull();
    }
}
