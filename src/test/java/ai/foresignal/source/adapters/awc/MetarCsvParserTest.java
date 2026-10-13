package ai.foresignal.source.adapters.awc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The AWC METAR cache parser: the live file captured 2026-10-13 ({@code metars.cache.csv.gz}) and the
 * synthetic {@code preamble.csv} (an older-format preamble, VRB, {@code 10+} and two reports of one
 * station).
 */
class MetarCsvParserTest {

    static final double[] TEXAS = {-106.65, 25.84, -93.51, 36.50};

    static byte[] live() {
        return AdapterFixtures.bytes("awc", "metars.cache.csv.gz");
    }

    static byte[] synthetic() {
        return AdapterFixtures.bytes("awc", "preamble.csv");
    }

    static NormalizedRecord station(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the live file: gzip inflated, header first, one record per station, KAUS mapped")
    void liveFile() {
        MetarCsvParser.ParseResult result = MetarCsvParser.parse(live(), MetarCsvParser.Filter.global());
        assertThat(result.diagnostics().preambleLines()).isZero();
        assertThat(result.records()).hasSize(5022);
        NormalizedRecord kaus = station(result.records(), "KAUS");
        assertThat(kaus.kind()).isEqualTo("metar");
        assertThat(kaus.observedAt()).isEqualTo(kaus.eventTime());
        assertThat(kaus.geometry().getGeometryType()).isEqualTo("Point");
        assertThat(kaus.payload()).containsEntry("stationId", "KAUS").containsEntry("metarType", "METAR")
                .containsEntry("windDirDeg", 130L).containsEntry("windKt", 11L)
                .containsEntry("visibilitySm", 10.0).containsEntry("visibilityPlus", true)
                .containsEntry("tempC", 30.0).containsEntry("flightCategory", "VFR");
        assertThat(kaus.payload().get("sky")).isEqualTo(List.of(
                Map.of("cover", "SCT", "baseFtAgl", 4000L), Map.of("cover", "SCT", "baseFtAgl", 5000L),
                Map.of("cover", "BKN", "baseFtAgl", 10000L), Map.of("cover", "BKN", "baseFtAgl", 20000L)));
        assertThat(kaus.payload().values()).doesNotContainNull();
    }

    @Test
    @DisplayName("a variable wind is read from the raw report: no windDirDeg, windVariable true")
    void variableWind() {
        NormalizedRecord wsss = station(MetarCsvParser.parse(live(), MetarCsvParser.Filter.global()).records(), "WSSS");
        assertThat(wsss.payload()).containsEntry("windVariable", true).doesNotContainKey("windDirDeg")
                .containsEntry("visibilitySm", 1.24).containsEntry("wx", "HZ").containsEntry("flightCategory", "IFR");
        assertThat(MetarCsvParser.variableWind("METAR KSRE 122355Z AUTO VRB06G12KT 10SM CLR")).isTrue();
        assertThat(MetarCsvParser.variableWind("METAR KAUS 122353Z 13011KT 10SM")).isFalse();
        assertThat(MetarCsvParser.variableWind(null)).isFalse();
    }

    @Test
    @DisplayName("the preamble is skipped, sky pairs are read by position, the newest report per station wins")
    void syntheticFile() {
        MetarCsvParser.ParseResult result = MetarCsvParser.parse(synthetic(), MetarCsvParser.Filter.global());
        assertThat(result.diagnostics().preambleLines()).isEqualTo(5);
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("KTST", "KVRB");
        NormalizedRecord test = station(result.records(), "KTST");
        assertThat(test.observedAt()).isEqualTo(Instant.parse("2026-10-13T00:53:00Z"));
        assertThat(test.payload()).containsEntry("metarType", "SPECI").containsEntry("gustKt", 25L)
                .containsEntry("visibilitySm", 10.0).containsEntry("visibilityPlus", true)
                .containsEntry("flightCategory", "LIFR")
                .containsEntry("sky", List.of(Map.of("cover", "OVC", "baseFtAgl", 300L)));
        NormalizedRecord vrb = station(result.records(), "KVRB");
        assertThat(vrb.payload()).containsEntry("windVariable", true).doesNotContainKeys("windDirDeg", "flightCategory",
                "visibilityPlus");
    }

    @Test
    @DisplayName("scope filters: the AOI envelope, an allowlist that overrides it")
    void filters() {
        MetarCsvParser.ParseResult texas = MetarCsvParser.parse(live(), new MetarCsvParser.Filter(TEXAS, Set.of()));
        assertThat(texas.records()).hasSize(291).allSatisfy(r -> assertThat(AreaOfInterest.contains(TEXAS,
                r.geometry().getCoordinate().x, r.geometry().getCoordinate().y)).isTrue());
        MetarCsvParser.ParseResult allow = MetarCsvParser.parse(live(),
                new MetarCsvParser.Filter(TEXAS, Set.of("KAUS", "EGLL")));
        assertThat(allow.records()).extracting(NormalizedRecord::externalId).containsExactlyInAnyOrder("KAUS", "EGLL");
    }

    @Test
    @DisplayName("every payload key is declared (011 contract helper)")
    void fieldSpecs() {
        var descriptor = new AwcMetarsAdapter((AreaOfInterest) null, "").descriptor();
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, MetarCsvParser.parse(live(), MetarCsvParser.Filter.global()).records());
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, MetarCsvParser.parse(synthetic(), MetarCsvParser.Filter.global()).records());
    }

    @Test
    @DisplayName("quoted cells, plain input and a missing header")
    void csvAndErrors() throws IOException {
        assertThat(MetarCsvParser.split("\"a,b\",c,,\"say \"\"hi\"\"\"")).containsExactly("a,b", "c", "", "say \"hi\"");
        byte[] plain = "raw_text,station_id\n".getBytes(StandardCharsets.UTF_8);
        assertThat(MetarCsvParser.inflate(plain)).isSameAs(plain);
        ByteArrayOutputStream gz = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(gz)) {
            out.write("no header here\n".getBytes(StandardCharsets.UTF_8));
        }
        assertThatThrownBy(() -> MetarCsvParser.parse(gz.toByteArray(), MetarCsvParser.Filter.global()))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("header");
        assertThatThrownBy(() -> MetarCsvParser.parse(new byte[] {0x1f, (byte) 0x8b, 1, 2}, MetarCsvParser.Filter.global()))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
