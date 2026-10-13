package ai.foresignal.source.adapters.env.openaq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
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

/**
 * {@link OpenAqLatestParser} on the synthetic v3 {@code latest} page (built from the v3 OpenAPI document; see the
 * fixture README): ids, UCUM units, the newest reading per location, {@code maxAge}, missing and null-island
 * coordinates.
 */
class OpenAqLatestParserTest {

    static final OpenAqParameterResolver.Parameter PM25 = new OpenAqParameterResolver.Parameter(2, "pm25", "µg/m³");

    static EnvSources.Parsed parsed() {
        OpenAqLatestParser.Page page = OpenAqLatestParser.parse(AdapterFixtures.bytes("openaq", "latest-2-page1.json"),
                PM25);
        assertThat(page.resultCount()).isEqualTo(7);
        EnvSources.Collector c = new EnvSources.Collector(Instant.parse("2026-10-13T21:30:00Z"), Duration.ofHours(6));
        page.entries().forEach(c::add);
        return c.result();
    }

    @Test
    @DisplayName("seven results: three records, newest per location, one too old, two without a usable location")
    void page() {
        EnvSources.Parsed p = parsed();
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("2178:pm25", "4761:pm25", "8118:pm25");
        assertThat(p.skipped()).containsEntry(MappedRecord.DUPLICATE_ID, 1).containsEntry(EnvSources.TOO_OLD, 1)
                .containsEntry(EnvSources.MISSING_LOCATION, 2);
        NormalizedRecord berlin = EnvTestSupport.byId(p.records(), "4761:pm25");
        assertThat(berlin.kind()).isEqualTo("air-quality");
        assertThat(berlin.observedAt()).isEqualTo(Instant.parse("2026-10-13T21:00:00Z"));
        assertThat(berlin.eventTime()).isEqualTo(berlin.observedAt());
        assertThat(berlin.geometry().getCoordinate().x).isEqualTo(13.4347);
        assertThat(berlin.geometry().getCoordinate().y).isEqualTo(52.4815);
        assertThat(berlin.payload()).containsEntry("parameter", "pm25").containsEntry("value", 11.2)
                .containsEntry("unit", "ug/m3").containsEntry("sensorId", 9021L).containsEntry("locationId", 4761L);
        FieldSpecContract.assertPayloadKeysDeclared(new OpenAqAdapter("", () -> null).descriptor(), p.records());
    }

    @Test
    @DisplayName("a ppm parameter is published as the UCUM [ppm]")
    void ppm() {
        OpenAqParameterResolver.Parameter o3 = new OpenAqParameterResolver.Parameter(10, "o3", "ppm");
        String body = "{\"results\": [{\"datetime\": {\"utc\": \"2026-10-13T21:00:00Z\", \"local\":"
                + " \"2026-10-13T15:00:00-06:00\"}, \"value\": 0.031, \"coordinates\": {\"latitude\": 35.1353,"
                + " \"longitude\": -106.584702}, \"sensorsId\": 3917, \"locationsId\": 2178}]}";
        OpenAqLatestParser.Page page = OpenAqLatestParser.parse(body.getBytes(StandardCharsets.UTF_8), o3);
        assertThat(page.entries()).singleElement().satisfies(m -> {
            assertThat(m.record().externalId()).isEqualTo("2178:o3");
            assertThat(m.record().payload()).containsEntry("unit", "[ppm]").containsEntry("value", 0.031);
        });
    }

    @Test
    @DisplayName("entries without an id, a time or a value are skipped with their reason")
    void skips() {
        String body = "{\"results\": ["
                + "{\"datetime\": {\"utc\": \"2026-10-13T21:00:00Z\"}, \"value\": 1, \"coordinates\": {\"latitude\": 1,"
                + " \"longitude\": 1}, \"sensorsId\": 1},"
                + "{\"datetime\": {}, \"value\": 1, \"coordinates\": {\"latitude\": 1, \"longitude\": 1}, \"locationsId\": 2},"
                + "{\"datetime\": {\"utc\": \"2026-10-13T21:00:00Z\"}, \"coordinates\": {\"latitude\": 1, \"longitude\": 1},"
                + " \"locationsId\": 3}]}";
        assertThat(OpenAqLatestParser.parse(body.getBytes(StandardCharsets.UTF_8), PM25).entries())
                .extracting(MappedRecord::reason)
                .containsExactly(MappedRecord.MISSING_ID, MappedRecord.MISSING_OBSERVED_AT, EnvSources.NO_VALUES);
    }

    @Test
    @DisplayName("an answer without a results array is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> OpenAqLatestParser.parse("{\"detail\": \"x\"}".getBytes(StandardCharsets.UTF_8), PM25))
                .isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
        assertThatThrownBy(() -> OpenAqLatestParser.parse("not json".getBytes(StandardCharsets.UTF_8), PM25))
                .isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptor: valid, key required, measurement time base, 3 h OBSERVED TTL, disabled-by-default shape")
    void descriptor() {
        OpenAqAdapter adapter = new OpenAqAdapter("", () -> null);
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.credentialFields()).extracting(CredentialField::name).containsExactly("apiKey");
        assertThat(d.credentialFields()).allMatch(CredentialField::required);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(3));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(30));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(20);
        assertThat(d.defaultBudget().perDay()).isEqualTo(2000);
        assertThat(d.licence()).isEqualTo("various (OpenAQ providers)");
    }

    @Test
    @DisplayName("snapshot contract: an unchanged reading an hour later keeps its static hash")
    void snapshot() {
        NormalizedRecord r = EnvTestSupport.byId(parsed().records(), "4761:pm25");
        SnapshotContract.assertStableStaticHash(new OpenAqAdapter("", () -> null).descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofHours(1))));
    }
}
