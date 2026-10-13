package ai.foresignal.source.adapters.env.airnow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

/**
 * {@link AirNowParser} on the synthetic monitoring-site answer (see the fixture README), the hour each run reads
 * and the request URL.
 */
class AirNowParserTest {

    static EnvSources.Parsed parsed() {
        return AirNowParser.parse(AdapterFixtures.bytes("airnow-hourly", "data.json"), EnvTestSupport.NOW,
                AirNowParser.DEFAULT_MAX_AGE);
    }

    @Test
    @DisplayName("five rows: four records keyed FullAQSCode:Parameter, the all-missing row skipped")
    void rows() {
        EnvSources.Parsed p = parsed();
        assertThat(p.records()).extracting(NormalizedRecord::externalId).containsExactly("080013001:PM2.5",
                "080310002:OZONE", "080310002:PM2.5", "080690011:OZONE");
        assertThat(p.skipped()).containsEntry(EnvSources.NO_VALUES, 1);
        NormalizedRecord camp = EnvTestSupport.byId(p.records(), "080310002:PM2.5");
        assertThat(camp.kind()).isEqualTo("air-quality");
        assertThat(camp.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:00:00Z"));
        assertThat(camp.eventTime()).isEqualTo(camp.observedAt());
        assertThat(camp.geometry().getCoordinate().y).isEqualTo(39.7516);
        assertThat(camp.payload()).containsEntry("parameter", "PM2.5").containsEntry("value", 6.8)
                .containsEntry("unit", "ug/m3").containsEntry("aqi", 28L).containsEntry("aqiCategory", 1L)
                .containsEntry("siteName", "Denver - CAMP")
                .containsEntry("agency", "Colorado Department of Public Health and Environment");
        assertThat(EnvTestSupport.byId(p.records(), "080310002:OZONE").payload()).containsEntry("unit", "[ppb]");
        FieldSpecContract.assertPayloadKeysDeclared(new AirNowHourlyAdapter("", () -> null).descriptor(), p.records());
    }

    @Test
    @DisplayName("rows without a site or a time are skipped; an error object is a format error")
    void skipsAndErrors() {
        String body = "[{\"UTC\": \"2026-10-13T20:00\", \"Parameter\": \"PM2.5\", \"Latitude\": 1, \"Longitude\": 1,"
                + " \"Value\": 1},"
                + "{\"FullAQSCode\": \"1\", \"Parameter\": \"PM2.5\", \"Latitude\": 1, \"Longitude\": 1, \"Value\": 1},"
                + "{\"FullAQSCode\": \"2\", \"UTC\": \"2026-10-13T20:00\", \"Parameter\": \"PM2.5\", \"Latitude\": -999,"
                + " \"Longitude\": -999, \"Value\": 1}]";
        EnvSources.Parsed p = AirNowParser.parse(body.getBytes(StandardCharsets.UTF_8), EnvTestSupport.NOW,
                Duration.ofHours(6));
        assertThat(p.records()).isEmpty();
        assertThat(p.skipped()).containsEntry(MappedRecord.MISSING_ID, 1)
                .containsEntry(MappedRecord.MISSING_OBSERVED_AT, 1).containsEntry(EnvSources.MISSING_LOCATION, 1);
        assertThatThrownBy(() -> AirNowParser.parse("{\"WebServiceError\": [{\"Message\": \"Invalid API key\"}]}"
                .getBytes(StandardCharsets.UTF_8), EnvTestSupport.NOW, Duration.ofHours(6)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThat(AirNowParser.hour("2026-10-13T20:00Z")).isEqualTo(Instant.parse("2026-10-13T20:00:00Z"));
        assertThat(AirNowParser.hour("garbage")).isNull();
    }

    @Test
    @DisplayName("the hour read: the previous hour once 30 minutes have passed, the one before until then")
    void hourFor() {
        assertThat(AirNowHourlyAdapter.hourFor(Instant.parse("2026-10-13T21:30:00Z")))
                .isEqualTo(Instant.parse("2026-10-13T20:00:00Z"));
        assertThat(AirNowHourlyAdapter.hourFor(Instant.parse("2026-10-13T21:29:59Z")))
                .isEqualTo(Instant.parse("2026-10-13T19:00:00Z"));
        assertThat(AirNowHourlyAdapter.hourFor(Instant.parse("2026-10-14T00:10:00Z")))
                .isEqualTo(Instant.parse("2026-10-13T22:00:00Z"));
    }

    @Test
    @DisplayName("request URL: the hour twice, parameters, BBOX as minLon,minLat,maxLon,maxLat and the key")
    void requestUri() {
        AirNowHourlyAdapter adapter = new AirNowHourlyAdapter("", () -> null);
        String url = adapter.requestUri(Instant.parse("2026-10-13T20:00:00Z"), List.of("PM25", "OZONE"),
                new Envelope(-105.5, -104.5, 39.5, 40.5), "KEY-123").toString();
        assertThat(url).startsWith("https://www.airnowapi.org/aq/data/?startDate=2026-10-13T20&endDate=2026-10-13T20")
                .contains("&parameters=PM25,OZONE").contains("&BBOX=-105.5,39.5,-104.5,40.5")
                .contains("&dataType=B&format=application/json&verbose=1&monitorType=0&includerawconcentrations=0")
                .endsWith("&API_KEY=KEY-123");
    }

    @Test
    @DisplayName("descriptor valid, key and bbox required; snapshot contract across an hour")
    void descriptorAndSnapshot() {
        AirNowHourlyAdapter adapter = new AirNowHourlyAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().credentialFields()).extracting(CredentialField::name).containsExactly("apiKey");
        assertThat(adapter.descriptor().params()).filteredOn(s -> s.name().equals("bbox")).singleElement()
                .satisfies(s -> assertThat(s.required()).isTrue());
        assertThat(adapter.descriptor().attribution()).contains("preliminary");
        NormalizedRecord r = EnvTestSupport.byId(parsed().records(), "080690011:OZONE");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(), List.of(r, EnvTestSupport.later(r, Duration.ofHours(1))));
    }
}
