package ai.foresignal.source.adapters.env.epauv;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.FixtureHttp;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The production {@link EpaUvAdapter} against the captured Envirofacts response for ZIP 78701
 * (checkpoint 124; {@code src/test/resources/fixtures/epa-uv/README.md}, captured 2026-10-17): the
 * adapter asks for the documented URL and turns the twelve hourly rows into one {@code uv-forecast}
 * record with the hours in UTC, read in the place's own zone.
 */
class EpaUvAdapterContractTest {

    static final String FIXTURE = "fixtures/epa-uv/getEnvirofactsUVHOURLY-ZIP-78701.json";
    static final String URL = "https://data.epa.gov/efservice/getEnvirofactsUVHOURLY/ZIP/78701/JSON";
    static final Instant NOW = Instant.parse("2026-10-17T11:45:00Z");

    @TempDir Path dir;

    /** One place, Austin, with the ZIP the capture was made for and its timezone. */
    static PlacePoints austin() {
        PlacePoints.Summary home = EpaUvAdapterTest.AUSTIN;
        return new PlacePoints() {
            @Override
            public Optional<double[]> point(String placeId) {
                return Optional.of(new double[] {home.lon(), home.lat()});
            }

            @Override
            public List<Summary> summaries() {
                return List.of(home);
            }

            @Override
            public Map<String, Object> attributes(String placeId) {
                return Map.of("zip", "78701");
            }

            @Override
            public Optional<String> timezone(String placeId) {
                return Optional.of("America/Chicago");
            }
        };
    }

    @Test
    @DisplayName("the captured ZIP 78701 response becomes one record: 12 hours, max UV 7 at 1 PM CDT")
    @SuppressWarnings("unchecked")
    void capturedResponse() throws Exception {
        FixtureHttp http = new FixtureHttp(dir).serve(URL, DecoderFixtures.bytes(FIXTURE));
        EpaUvAdapter adapter = new EpaUvAdapter(EpaUvAdapterContractTest::austin, "");
        ProductAdapterHarness.MapState state = new ProductAdapterHarness.MapState();
        ProductAdapterHarness.RecordingSink sink = new ProductAdapterHarness.RecordingSink();

        FetchResult result = adapter.fetch(new ProductAdapterHarness.Context(adapter.descriptor(), http,
                Clock.fixed(NOW, ZoneOffset.UTC), state, sink));

        assertThat(http.requests()).containsExactly("GET " + URL);
        assertThat(sink.formatChanges()).isEmpty();
        List<NormalizedRecord> records = result.records().stream().filter(r -> !r.tombstone()).toList();
        assertThat(records).hasSize(1);
        NormalizedRecord record = records.get(0);
        assertThat(record.externalId()).isEqualTo("place:home");
        assertThat(record.kind()).isEqualTo(EpaUvAdapter.KIND);
        assertThat(record.payload()).containsEntry("zip", "78701").containsEntry("maxUv", 7)
                .containsEntry("maxAt", "2026-10-17T18:00:00Z");
        List<Map<String, Object>> hours = (List<Map<String, Object>>) record.payload().get("hours");
        assertThat(hours).hasSize(12);
        // "Oct/17/2026 07 AM" in Austin (CDT, UTC-5) is 12:00Z; "06 PM" is 23:00Z.
        assertThat(hours.get(0)).containsEntry("time", "2026-10-17T12:00:00Z").containsEntry("uv", 0);
        assertThat(hours.get(11)).containsEntry("time", "2026-10-17T23:00:00Z").containsEntry("uv", 1);
        assertThat(hours.stream().map(h -> (Integer) h.get("uv")).toList())
                .containsExactly(0, 0, 0, 2, 3, 5, 7, 7, 6, 4, 2, 1);
        assertThat(record.validFrom()).isEqualTo(Instant.parse("2026-10-17T12:00:00Z"));
        assertThat(record.validUntil()).isEqualTo(Instant.parse("2026-10-17T23:00:00Z"));
    }
}
