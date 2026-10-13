package ai.foresignal.source.adapters.space.jpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link JplParsers} on real Fireball (1.2) and Close Approach (1.5) answers of 2026-10-13: hemisphere signs,
 * numbers sent as strings, lunar distances, empty windows and a hand-made row without {@code h}.
 */
class JplParsersTest {

    static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("fireballs: 18 rows, signed coordinates, energy x 1e10 J, string numbers, null velocity left out")
    void fireballs() {
        JplParsers.Parsed p = JplParsers.fireballs(AdapterFixtures.bytes("jpl-fireballs", "fireball.json"));
        assertThat(p.version()).isEqualTo("1.2");
        assertThat(p.records()).hasSize(18).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("fireball");
            assertThat(r.observedAt()).as("RECEIVED").isNull();
            assertThat(r.geometry()).isNotNull();
        });
        NormalizedRecord south = byId(p.records(), "2026-10-04 03:14:45");
        assertThat(south.eventTime()).isEqualTo(Instant.parse("2026-10-04T03:14:45Z"));
        assertThat(south.geometry().getCoordinate().x).isEqualTo(-173.1);
        assertThat(south.geometry().getCoordinate().y).isEqualTo(-41.8);
        assertThat(south.payload()).containsEntry("radiatedEnergyJ", 5.4e10).containsEntry("impactEnergyKt", 0.18)
                .containsEntry("altitudeKm", 26.9).containsEntry("velocityKms", 13.8);
        NormalizedRecord east = byId(p.records(), "2026-05-21 02:15:51");
        assertThat(east.geometry().getCoordinate().x).as("E positive").isEqualTo(133.1);
        assertThat(east.geometry().getCoordinate().y).as("N positive").isEqualTo(46.6);
        assertThat(byId(p.records(), "2026-09-15 11:26:13").payload()).doesNotContainKey("velocityKms");
        FieldSpecContract.assertPayloadKeysDeclared(new JplFireballsAdapter("").descriptor(), p.records());
        assertThat(JplParsers.fireballs(AdapterFixtures.bytes("jpl-fireballs", "fireball-empty.json")).records()).isEmpty();
    }

    @Test
    @DisplayName("close approaches: 42 rows, des@cd ids, lunar distance, TDB time read as UTC, trimmed full name")
    void closeApproaches() {
        JplParsers.Parsed p = JplParsers.closeApproaches(AdapterFixtures.bytes("jpl-close-approaches", "cad.json"));
        assertThat(p.version()).isEqualTo("1.5");
        assertThat(p.records()).hasSize(42).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("close-approach");
            assertThat(r.geometry()).isNull();
        });
        NormalizedRecord tc1 = byId(p.records(), "2026 TC1@2026-Oct-13 04:29");
        assertThat(tc1.eventTime()).isEqualTo(Instant.parse("2026-10-13T04:29:00Z"));
        assertThat(tc1.payload()).containsEntry("designation", "2026 TC1").containsEntry("fullName", "(2026 TC1)")
                .containsEntry("distAu", 0.0413163692039975).containsEntry("distLunar", 16.08)
                .containsEntry("distMinAu", 0.040545511410268).containsEntry("distMaxAu", 0.0420872059953534)
                .containsEntry("vRelKms", 13.1985452971442).containsEntry("absMagnitude", 25.277)
                .containsEntry("orbitId", "4").containsEntry("timeSigma", "< 00:01");
        FieldSpecContract.assertPayloadKeysDeclared(new JplCloseApproachesAdapter("").descriptor(), p.records());
        assertThat(JplParsers.closeApproaches(AdapterFixtures.bytes("jpl-close-approaches", "cad-empty.json")).records())
                .isEmpty();
    }

    @Test
    @DisplayName("edge rows: h missing leaves absMagnitude out; a repeated id and a missing des are skipped")
    void closeApproachEdges() {
        JplParsers.Parsed p = JplParsers.closeApproaches(AdapterFixtures.bytes("jpl-close-approaches", "cad-edge.json"));
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("2026 TC1@2026-Oct-13 04:29", "2026 TK35@2026-Oct-13 08:49");
        assertThat(p.records().get(1).payload()).doesNotContainKey("absMagnitude").containsEntry("distLunar", 19.12);
        assertThat(p.skipped()).containsEntry(MappedRecord.DUPLICATE_ID, 1).containsEntry(MappedRecord.MISSING_ID, 1);
    }

    @Test
    @DisplayName("signs, times and format errors")
    void helpers() {
        assertThat(JplParsers.signed(41.8, "S", "S", "N")).isEqualTo(-41.8);
        assertThat(JplParsers.signed(41.8, "n", "S", "N")).isEqualTo(41.8);
        assertThat(JplParsers.signed(41.8, "X", "S", "N")).isNull();
        assertThat(JplParsers.signed(null, "S", "S", "N")).isNull();
        assertThat(JplParsers.cadTime("2026-Oct-13 04:29")).isEqualTo(Instant.parse("2026-10-13T04:29:00Z"));
        assertThat(JplParsers.cadTime("2026-13-13")).isNull();
        assertThatThrownBy(() -> JplParsers.fireballs("[]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> JplParsers.fireballs("{\"count\": \"2\", \"data\": []}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> JplParsers.closeApproaches(("{\"count\": 1, \"fields\": [\"des\"], \"data\": []}")
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(UpstreamFormatException.class).hasMessageContaining("cd");
        assertThatThrownBy(() -> JplParsers.closeApproaches("not json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptors: RECEIVED time, 6 h and 12 h, 1/min and 10/day; distMaxAu in (0, 0.5]; the request URLs")
    void descriptors() {
        JplFireballsAdapter fireballs = new JplFireballsAdapter("");
        assertThat(DescriptorValidator.problems(fireballs.descriptor(), fireballs)).isEmpty();
        assertThat(fireballs.descriptor().observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(fireballs.descriptor().defaultInterval()).isEqualTo(Duration.ofHours(6));
        assertThat(fireballs.uri(Instant.parse("2026-10-13T20:00:00Z")).toString())
                .isEqualTo("https://ssd-api.jpl.nasa.gov/fireball.api?date-min=2026-09-13&req-loc=true");
        JplCloseApproachesAdapter cad = new JplCloseApproachesAdapter("");
        assertThat(DescriptorValidator.problems(cad.descriptor(), cad)).isEmpty();
        assertThat(cad.descriptor().defaultInterval()).isEqualTo(Duration.ofHours(12));
        assertThat(cad.descriptor().recordTtl()).isEqualTo(Duration.ofDays(7));
        assertThat(cad.uri(0.05).toString()).isEqualTo(
                "https://ssd-api.jpl.nasa.gov/cad.api?date-min=now&date-max=%2B60&dist-max=0.05&fullname=true");
        assertThat(cad.checkParams(ValidatedParams.of(Map.of("distMaxAu", 0.6)))).containsKey("distMaxAu");
        assertThat(cad.checkParams(ValidatedParams.of(Map.of("distMaxAu", 0.0)))).containsKey("distMaxAu");
        assertThat(cad.checkParams(ValidatedParams.of(Map.of("distMaxAu", 0.1)))).isEmpty();
    }

    @Test
    @DisplayName("an unchanged row received again keeps the static hash")
    void snapshot() {
        byte[] body = AdapterFixtures.bytes("jpl-close-approaches", "cad.json");
        NormalizedRecord a = JplParsers.closeApproaches(body).records().getFirst();
        NormalizedRecord b = JplParsers.closeApproaches(body).records().getFirst();
        SnapshotContract.assertStableStaticHash(new JplCloseApproachesAdapter("").descriptor(), List.of(a, b));
        NormalizedRecord f1 = JplParsers.fireballs(AdapterFixtures.bytes("jpl-fireballs", "fireball.json")).records().getFirst();
        NormalizedRecord f2 = JplParsers.fireballs(AdapterFixtures.bytes("jpl-fireballs", "fireball.json")).records().getFirst();
        SnapshotContract.assertStableStaticHash(new JplFireballsAdapter("").descriptor(), List.of(f1, f2));
    }
}
