package ai.foresignal.source.adapters.hydro.snotel;

import static ai.foresignal.source.adapters.hydro.HydroTestSupport.fixture;
import static ai.foresignal.source.adapters.hydro.HydroTestSupport.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AwdbParser} on the 2026-10-13 AWDB station metadata and daily data of three Colorado SNOTEL sites: inches to
 * mm and cm, °F to °C, feet to meters, the value date at midnight in the station's data time zone (−8 h) as UTC, the
 * station table's state round trip, and the descriptor, parameters and URLs of {@link SnotelAdapter}.
 */
class AwdbParserTest {

    static Map<String, AwdbParser.Station> stations() {
        return AwdbParser.stations(fixture("snotel", "stations.json"));
    }

    static EnvSources.Parsed parsed() {
        return AwdbParser.data(fixture("snotel", "data.json"), stations());
    }

    @Test
    @DisplayName("three stations; the newest date (2026-10-12) at 00:00 −08:00 is 08:00 UTC")
    void stationsAndTime() {
        EnvSources.Parsed p = parsed();
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("1030:CO:SNTL", "1344:CO:SNTL", "335:CO:SNTL");
        assertThat(p.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("snow-observation");
            assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-12T08:00:00Z")).isEqualTo(r.eventTime());
        });
        assertThat(p.skipped()).isEmpty();
        FieldSpecContract.assertPayloadKeysDeclared(new SnotelAdapter("", () -> null).descriptor(), p.records());
    }

    @Test
    @DisplayName("unit conversions: 0.1 in SWE is 2.5 mm, 1 in depth 2.5 cm, 38.8 °F is 3.8 °C, 11,300 ft is 3444.2 m")
    void conversions() {
        NormalizedRecord berthoud = EnvTestSupport.byId(parsed().records(), "335:CO:SNTL");
        assertThat(berthoud.payload()).containsEntry("name", "Berthoud Summit").containsEntry("elevationM", 3444.2)
                .containsEntry("sweMm", 2.5).containsEntry("snowDepthCm", 2.5).containsEntry("precipAccumMm", 0.0)
                .containsEntry("airTempC", 3.8);
        assertThat(EnvTestSupport.byId(parsed().records(), "1344:CO:SNTL").payload())
                .containsEntry("precipAccumMm", 22.9).containsEntry("airTempC", 0.9).containsEntry("elevationM", 3441.2);
        assertThat(AwdbParser.convert("TOBS", "degF", 32.0)).isEqualTo(0.0);
        assertThat(AwdbParser.convert("TOBS", "degC", -4.25)).isEqualTo(-4.3);
        assertThat(AwdbParser.convert("WTEQ", "mm", 12.0)).isEqualTo(12.0);
        assertThat(AwdbParser.convert("SNWD", "cm", 30.0)).isEqualTo(30.0);
        assertThat(AwdbParser.convert("SNWD", "furlong", 1.0)).isNull();
        assertThat(AwdbParser.convert("SMS", "pct", 1.0)).isNull();
    }

    @Test
    @DisplayName("a station without metadata or values is skipped; -9999 values are scrubbed; a missing element is absent")
    void skips() {
        EnvSources.Parsed p = AwdbParser.data(node("""
                [{"stationTriplet": "999:CO:SNTL", "data": []},
                 {"stationTriplet": "335:CO:SNTL", "data": [
                   {"stationElement": {"elementCode": "WTEQ", "storedUnitCode": "in"},
                    "values": [{"date": "2026-10-13", "value": -9999}]},
                   {"stationElement": {"elementCode": "SNWD", "storedUnitCode": "in"},
                    "values": [{"date": "2026-10-13", "value": 4}]}]},
                 {"stationTriplet": "1344:CO:SNTL", "data": [{"stationElement": {"elementCode": "PREC",
                    "storedUnitCode": "in"}, "values": []}]}]"""), stations());
        assertThat(p.skipped()).containsEntry(EnvSources.MISSING_LOCATION, 1).containsEntry(EnvSources.NO_VALUES, 1);
        assertThat(p.records()).singleElement().satisfies(r -> assertThat(r.payload())
                .containsEntry("snowDepthCm", 10.2).doesNotContainKeys("sweMm", "precipAccumMm", "airTempC"));
        assertThatThrownBy(() -> AwdbParser.data(node("{}"), stations())).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AwdbParser.stations(node("{}"))).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the station table survives the state round trip; damaged state is empty")
    void stationsRoundTrip() {
        Map<String, AwdbParser.Station> stations = stations();
        assertThat(stations).hasSize(3).containsEntry("1344:CO:SNTL",
                new AwdbParser.Station("Alta Lakes", 37.88929, -107.84484, 11290.0, -8.0));
        assertThat(AwdbParser.stationsFromJson(AwdbParser.stationsToJson(stations))).isEqualTo(stations);
        assertThat(AwdbParser.stationsFromJson("[{")).isEmpty();
        assertThat(AwdbParser.stationsFromJson(null)).isEmpty();
    }

    @Test
    @DisplayName("checkParams, the triplet query, the URLs, the descriptor, and the snapshot contract across a day")
    void adapter() {
        SnotelAdapter adapter = new SnotelAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey("states");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("states", List.of("CO", "Utah")))))
                .containsKey("states");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("stations", List.of("335:CO:SNOW")))))
                .containsKey("stations");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("states", List.of("co"),
                "stations", List.of("335:CO:SNTL"))))).isEmpty();
        assertThat(SnotelAdapter.query(ValidatedParams.of(Map.of("states", List.of("co", "UT"),
                "stations", List.of("335:co:SNTL"))))).isEqualTo("*:CO:SNTL,*:UT:SNTL,335:CO:SNTL");
        assertThat(adapter.stationsUri("*:CO:SNTL")).hasToString("https://wcc.sc.egov.usda.gov/awdbRestApi/services/v1"
                + "/stations?stationTriplets=%2A%3ACO%3ASNTL&returnForecastPointMetadata=false");
        assertThat(adapter.dataUri(List.of("335:CO:SNTL", "1344:CO:SNTL"), LocalDate.parse("2026-10-13")))
                .hasToString("https://wcc.sc.egov.usda.gov/awdbRestApi/services/v1/data?stationTriplets="
                        + "335%3ACO%3ASNTL%2C1344%3ACO%3ASNTL&elements=WTEQ%2CSNWD%2CPREC%2CTOBS&duration=DAILY"
                        + "&beginDate=2026-10-11&endDate=2026-10-13");
        NormalizedRecord r = EnvTestSupport.byId(parsed().records(), "335:CO:SNTL");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofDays(1))));
    }
}
