package ai.foresignal.watch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@link Identity}, its normalizations, {@link Csv} and the static parsers of {@link WatchlistService} (prompt 036). */
class IdentityTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("defaults: aircraft identifiers upper, vessel numbers digits, custom none")
    void defaults() {
        assertThat(Identity.ICAO24.defaultNormalize()).isEqualTo(Identity.Normalize.UPPER);
        assertThat(Identity.REGISTRATION.defaultNormalize()).isEqualTo(Identity.Normalize.UPPER);
        assertThat(Identity.CALLSIGN.defaultNormalize()).isEqualTo(Identity.Normalize.UPPER);
        assertThat(Identity.MMSI.defaultNormalize()).isEqualTo(Identity.Normalize.DIGITS);
        assertThat(Identity.IMO.defaultNormalize()).isEqualTo(Identity.Normalize.DIGITS);
        assertThat(Identity.CUSTOM.defaultNormalize()).isEqualTo(Identity.Normalize.NONE);
        assertThat(Identity.fromWire("mmsi")).isEqualTo(Identity.MMSI);
        assertThatThrownBy(() -> Identity.fromWire("passport")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Identity.Normalize.fromWire("title")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("normalization in Java and SQL: spaces trimmed, case folded, non-digits removed")
    void normalize() {
        assertThat(Identity.Normalize.UPPER.apply(" a1b2c3 ")).isEqualTo("A1B2C3");
        assertThat(Identity.Normalize.LOWER.apply("N123AB")).isEqualTo("n123ab");
        assertThat(Identity.Normalize.DIGITS.apply("IMO 9074729")).isEqualTo("9074729");
        assertThat(Identity.Normalize.NONE.apply("  x y ")).isEqualTo("x y");
        assertThat(Identity.Normalize.NONE.apply(null)).isNull();
        assertThat(Identity.Normalize.UPPER.sql("t")).isEqualTo("upper(btrim(t))");
        assertThat(Identity.Normalize.DIGITS.sql("t")).isEqualTo("regexp_replace(btrim(t), '[^0-9]', '', 'g')");
        assertThat(Identity.Normalize.NONE.sql("t")).isEqualTo("btrim(t)");
        assertThat(Identity.Normalize.LOWER.sql("t")).isEqualTo("lower(btrim(t))");
    }

    @Test
    @DisplayName("validation per identity, IMO check digit included")
    void validation() {
        assertThat(Identity.ICAO24.invalid("A1B2C3")).isNull();
        assertThat(Identity.ICAO24.invalid("A1B2C")).contains("six hex");
        assertThat(Identity.MMSI.invalid("366123456")).isNull();
        assertThat(Identity.MMSI.invalid("36612345")).contains("nine digits");
        assertThat(Identity.IMO.invalid("9074729")).isNull();
        assertThat(Identity.IMO.invalid("9074728")).contains("check digit");
        assertThat(Identity.IMO.invalid("907472")).contains("seven digits");
        assertThat(Identity.REGISTRATION.invalid("N123AB")).isNull();
        assertThat(Identity.REGISTRATION.invalid("N 123")).isNotNull();
        assertThat(Identity.CALLSIGN.invalid("UAL1234")).isNull();
        assertThat(Identity.CALLSIGN.invalid("UAL-1")).isNotNull();
        assertThat(Identity.CUSTOM.invalid("anything at all")).isNull();
        assertThat(Identity.CUSTOM.invalid("")).contains("empty");
        assertThat(Identity.imoCheckDigit("9176187")).isTrue();
    }

    @Test
    @DisplayName("CSV: quotes, doubled quotes, commas, CRLF and a byte-order mark; fields are quoted when needed")
    void csv() {
        List<List<String>> rows = Csv.parse("﻿value,label\r\n\"a,b\",\"say \"\"hi\"\"\"\r\nc,\n");
        assertThat(rows).containsExactly(List.of("value", "label"), List.of("a,b", "say \"hi\""), List.of("c", ""));
        assertThat(Csv.parse("x")).containsExactly(List.of("x"));
        assertThat(Csv.field(null)).isEmpty();
        assertThat(Csv.field("plain")).isEqualTo("plain");
        assertThat(Csv.field("a,b")).isEqualTo("\"a,b\"");
        assertThat(Csv.field("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
        assertThat(Csv.field("two\nlines")).isEqualTo("\"two\nlines\"");
    }

    @Test
    @DisplayName("the static parsers: list input, JSON items and CSV rows with invalid ones reported")
    void parsers() {
        WatchlistService.ListInput input = WatchlistService.parse(JSON.readTree(
                "{\"name\":\"Ships\",\"identity\":\"mmsi\",\"description\":\"d\"}"), null);
        assertThat(input.normalize()).isEqualTo(Identity.Normalize.DIGITS);
        assertThatThrownBy(() -> WatchlistService.parse(JSON.readTree("{\"identity\":\"x\",\"extra\":1}"), null))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);

        WatchListEntity list = new WatchListEntity(UUID.randomUUID());
        list.setIdentity("icao24");
        list.setNormalize("upper");
        WatchlistService.Parsed json = WatchlistService.parseJsonItems(list, JSON.readTree(
                "[{\"value\":\"a1b2c3\",\"tags\":[\"x\"]},{\"value\":\"zz\"},{\"label\":\"no value\"},7,"
                        + "{\"value\":\"a1b2c3\",\"label\":\"last wins\"},{\"value\":\"b1b2c3\",\"bogus\":1}]"));
        assertThat(json.items()).containsOnlyKeys("A1B2C3");
        assertThat(json.items().get("A1B2C3").label()).isEqualTo("last wins");
        assertThat(json.invalid()).extracting(WatchlistService.Invalid::row).containsExactly(2, 3, 4, 6);

        WatchlistService.Parsed csv = WatchlistService.parseCsv(list, "value,tags\nadf7c8,a; b ;\n\n,\n");
        assertThat(csv.items().get("ADF7C8").tags()).containsExactly("a", "b");
        assertThat(csv.invalid()).hasSize(1);
        assertThatThrownBy(() -> WatchlistService.parseCsv(list, "label\nx"))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        assertThatThrownBy(() -> WatchlistService.parseCsv(list, "value,color\nx,y"))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
    }

    @Test
    @DisplayName("the operator's JSON text of numbers matches PostgreSQL's #>> rendering for identifiers")
    void operatorText() {
        assertThat(WatchlistOperator.text("A1")).isEqualTo("A1");
        assertThat(WatchlistOperator.text(366123456)).isEqualTo("366123456");
        assertThat(WatchlistOperator.text(366123456L)).isEqualTo("366123456");
        assertThat(WatchlistOperator.text(new java.math.BigDecimal("1.50"))).isEqualTo("1.50");
        assertThat(WatchlistOperator.text(true)).isNull();
        assertThat(WatchlistOperator.text(null)).isNull();
        assertThat(WatchlistOperator.text(List.of(1))).isNull();
    }
}
