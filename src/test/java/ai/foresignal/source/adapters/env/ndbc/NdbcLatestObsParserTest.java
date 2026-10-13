package ai.foresignal.source.adapters.env.ndbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
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
 * {@link NdbcLatestObsParser} and {@link NdbcStationsParser} on the first 40 lines of the 2026-10-13
 * {@code latest_obs.txt} and the matching {@code activestations.xml} entries: {@code MM}, station 15009 at latitude
 * 0, the case-sensitive month and minute columns, and the station table's state round trip.
 */
class NdbcLatestObsParserTest {

    static Map<String, NdbcStationsParser.Station> stations() {
        return NdbcStationsParser.parse(AdapterFixtures.bytes("ndbc-buoys", "activestations.xml"));
    }

    static EnvSources.Parsed parsed() {
        return NdbcLatestObsParser.parse(AdapterFixtures.bytes("ndbc-buoys", "latest_obs.txt"), stations(),
                EnvTestSupport.NOW, NdbcLatestObsParser.DEFAULT_MAX_AGE);
    }

    @Test
    @DisplayName("38 stations; MM values left out; 15009 at 0.000 -3.051 is kept")
    void stationsAndMissing() {
        EnvSources.Parsed p = parsed();
        assertThat(p.records()).hasSize(38).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("buoy-observation");
            assertThat(r.observedAt()).isEqualTo(r.eventTime());
        });
        assertThat(p.skipped()).isEmpty();
        NormalizedRecord atEquator = EnvTestSupport.byId(p.records(), "15009");
        assertThat(atEquator.geometry().getCoordinate().y).isEqualTo(0.0);
        assertThat(atEquator.geometry().getCoordinate().x).isEqualTo(-3.051);
        assertThat(atEquator.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:00:00Z"));
        assertThat(atEquator.payload()).containsEntry("windDirDeg", 197.0).containsEntry("windSpeedMs", 6.5)
                .containsEntry("pressureHpa", 1012.5).containsEntry("airTempC", 25.7).containsEntry("waterTempC", 26.6)
                .containsEntry("stationType", "other")
                .containsEntry("owner", "Prediction and Research Moored Array in the Atlantic")
                .doesNotContainKeys("gustMs", "waveHeightM", "dewPointC", "visibilityNmi", "tideFt", "name");
        FieldSpecContract.assertPayloadKeysDeclared(new NdbcBuoysAdapter("", () -> null).descriptor(), p.records());
    }

    @Test
    @DisplayName("41004: every mapped column, names from activestations.xml, the minute column read as minutes")
    void fullRow() {
        EnvSources.Parsed p = parsed();
        NormalizedRecord r = EnvTestSupport.byId(p.records(), "41004");
        assertThat(r.payload()).containsEntry("name", "EDISTO - 41 NM Southeast of Charleston, SC")
                .containsEntry("stationType", "buoy").containsEntry("owner", "NDBC")
                .containsEntry("windDirDeg", 350.0).containsEntry("gustMs", 1.0).containsEntry("pressureTendencyHpa", -1.1)
                .containsEntry("dewPointC", 19.0).containsEntry("waterTempC", 28.1);
        assertThat(EnvTestSupport.byId(p.records(), "41024").observedAt())
                .isEqualTo(Instant.parse("2026-10-13T20:08:00Z"));
        assertThat(EnvTestSupport.byId(p.records(), "41067").payload()).containsEntry("waveHeightM", 0.3)
                .containsEntry("dominantPeriodS", 7.0).containsEntry("averagePeriodS", 5.7)
                .containsEntry("meanWaveDirDeg", 135.0);
    }

    @Test
    @DisplayName("without a station table the records carry no names; old rows, short rows and empty rows are skipped")
    void skips() {
        String text = "#STN       LAT      LON  YYYY MM DD hh mm WDIR WSPD   GST WVHT  DPD APD MWD   PRES  PTDY  ATMP  WTMP"
                + "  DEWP  VIS   TIDE\n"
                + "#text      deg      deg   yr mo day hr mn degT  m/s   m/s   m   sec sec degT   hPa   hPa  degC  degC  degC"
                + "  nmi     ft\n"
                + "41004    32.502  -79.099 2026 10 13 21 00 350   1.0   1.0   MM  MM   MM  MM 1017.2  -1.1  26.4  28.1  19.0"
                + "   MM     MM\n"
                + "41008    31.400  -80.866 2026 10 13 10 00 060   2.0   3.0   MM  MM   MM  MM 1016.8  -1.2  26.2    MM  23.2"
                + "   MM     MM\n"
                + "41009    28.508  -80.185 2026 10 13 21 00 060   4.0\n"
                + "41010    28.860  -78.478 2026 10 13 21 00  MM    MM    MM   MM  MM   MM  MM     MM    MM    MM    MM    MM"
                + "   MM     MM\n";
        EnvSources.Parsed p = NdbcLatestObsParser.parse(text.getBytes(StandardCharsets.UTF_8), Map.of(),
                EnvTestSupport.NOW, Duration.ofHours(6));
        assertThat(p.records()).extracting(NormalizedRecord::externalId).containsExactly("41004");
        assertThat(p.records().getFirst().payload()).doesNotContainKeys("name", "stationType", "owner");
        assertThat(p.skipped()).containsEntry(EnvSources.TOO_OLD, 1).containsEntry(NdbcLatestObsParser.BAD_ROW, 1)
                .containsEntry(EnvSources.NO_VALUES, 1);
    }

    @Test
    @DisplayName("a file without the #STN header, or with a header missing a column, is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> NdbcLatestObsParser.parse("41004 1 2 3".getBytes(StandardCharsets.UTF_8), Map.of(),
                EnvTestSupport.NOW, Duration.ofHours(6))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NdbcLatestObsParser.parse("#STN LAT LON YYYY MM DD hh\n".getBytes(StandardCharsets.UTF_8),
                Map.of(), EnvTestSupport.NOW, Duration.ofHours(6))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NdbcStationsParser.parse("<stations/>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NdbcStationsParser.parse("not xml".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("station table: 38 entries; a blank name is null; JSON and packed-state round trips")
    void stationTable() {
        Map<String, NdbcStationsParser.Station> table = stations();
        assertThat(table).hasSize(38);
        assertThat(table.get("15009").name()).isNull();
        assertThat(table.get("32ST0")).isEqualTo(new NdbcStationsParser.Station("Stratus", "fixed",
                "Woods Hole Oceanographic Institution"));
        String json = NdbcStationsParser.toJson(table);
        assertThat(NdbcStationsParser.fromJson(json)).isEqualTo(table);
        assertThat(NdbcStationsParser.fromJson(EnvSources.unpack(EnvSources.pack(json)))).isEqualTo(table);
        assertThat(NdbcStationsParser.fromJson("garbage")).isEmpty();
        assertThat(NdbcStationsParser.fromJson(null)).isEmpty();
    }

    @Test
    @DisplayName("descriptor valid; snapshot contract across ten minutes")
    void descriptorAndSnapshot() {
        NdbcBuoysAdapter adapter = new NdbcBuoysAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().recordTtl()).isEqualTo(Duration.ofHours(3));
        assertThat(adapter.descriptor().allowNullIsland()).isFalse();
        NormalizedRecord r = EnvTestSupport.byId(parsed().records(), "41004");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofMinutes(10))));
    }
}
