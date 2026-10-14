package ai.foresignal.source.adapters.acled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link AcledParser} on the synthetic fixtures built from the ACLED API guides (prompt 081): string-valued rows, the
 * {@code geo_precision} to accuracy mapping, {@code timestamp} and {@code event_date} times, the 2 KB notes cap, the
 * deleted list, the envelope checks, the fieldSpecs contract and a stable static hash.
 */
class AcledParserTest {

    private static final AcledAdapter ADAPTER = new AcledAdapter("", new CountryCentroids());
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static AcledParser.Page events() {
        return AcledParser.events(AdapterFixtures.bytes("acled", "events.json"));
    }

    @Test
    @DisplayName("four rows: three events, one without coordinates skipped; the page's largest timestamp")
    void page() {
        AcledParser.Page page = events();
        assertThat(page.rows()).isEqualTo(4);
        assertThat(page.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("UKR100234", "UKR100235", "UKR100236");
        assertThat(page.skipped()).containsExactly(Map.entry(AcledParser.BAD_POSITION, 1));
        assertThat(page.maxTimestamp()).isEqualTo(1_791_821_000L);
    }

    @Test
    @DisplayName("UKR100234: point, 2 km accuracy, timestamp and event_date, numbers from strings, the payload")
    void event() {
        NormalizedRecord r = events().records().getFirst();
        assertThat(r.kind()).isEqualTo("conflict-event");
        Point p = (Point) r.geometry();
        assertThat(p.getX()).isEqualTo(36.2311);
        assertThat(p.getY()).isEqualTo(49.9923);
        assertThat(r.quality().accuracyMeters()).isEqualTo(2_000.0);
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochSecond(1_791_800_000L));
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-10-10T00:00:00Z"));
        assertThat(r.payload()).containsEntry("eventType", "Explosions/Remote violence")
                .containsEntry("subEventType", "Shelling/artillery/missile attack")
                .containsEntry("disorderType", "Political violence")
                .containsEntry("actor1", "Military Forces of Russia (2000-)").containsEntry("actor2", "Civilians (Ukraine)")
                .containsEntry("interaction", "State forces-Civilians")
                .containsEntry("civilianTargeting", "Civilian targeting").containsEntry("country", "Ukraine")
                .containsEntry("admin1", "Kharkiv").containsEntry("admin2", "Kharkivskyi")
                .containsEntry("location", "Kharkiv").containsEntry("fatalities", 2L)
                .containsEntry("sources", "Ukrinform; Suspilne").containsEntry("geoPrecision", 1L)
                .containsEntry("timePrecision", 1L).doesNotContainKeys("iso", "latitude", "longitude", "timestamp");
        String notes = (String) r.payload().get("notes");
        assertThat(notes.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(AcledParser.NOTES_MAX_BYTES);
        assertThat(notes).startsWith("On 10 October 2026");
    }

    @Test
    @DisplayName("blank strings are omitted (rule 8): no actor2, civilianTargeting or admin2 on UKR100236")
    void blanks() {
        NormalizedRecord r = events().records().get(2);
        assertThat(r.payload()).doesNotContainKeys("actor2", "civilianTargeting", "admin2")
                .containsEntry("timePrecision", 2L);
        assertThat(r.quality().accuracyMeters()).isEqualTo(100_000.0);
    }

    @ParameterizedTest(name = "geo_precision {0} -> {1} m")
    @CsvSource({"1, 2000.0", "2, 25000.0", "3, 100000.0"})
    @DisplayName("geo_precision to accuracy")
    void accuracy(long code, double meters) {
        assertThat(AcledParser.accuracyMeters(code)).isEqualTo(meters);
    }

    @Test
    @DisplayName("an unknown or missing geo_precision has no accuracy")
    void noAccuracy() {
        assertThat(AcledParser.accuracyMeters(4L)).isNull();
        assertThat(AcledParser.accuracyMeters(null)).isNull();
    }

    @Test
    @DisplayName("the deleted list: every id and the largest deleted_timestamp")
    void deleted() {
        AcledParser.Deleted deleted = AcledParser.deleted(AdapterFixtures.bytes("acled", "deleted.json"));
        assertThat(deleted.ids()).containsExactly("UKR099001", "SYR55555", "UKR099002");
        assertThat(deleted.rows()).isEqualTo(3);
        assertThat(deleted.maxDeletedTimestamp()).isEqualTo(1_791_720_000L);
        assertThat(AcledAdapter.matches("UKR099001", Set.of("UKR"))).isTrue();
        assertThat(AcledAdapter.matches("SYR55555", Set.of("UKR"))).isFalse();
        assertThat(AcledAdapter.matches("SYR55555", Set.of())).isTrue();
        assertThat(ADAPTER.alpha3(List.of("804", "760"))).containsExactly("UKR", "SYR");
    }

    @Test
    @DisplayName("success=false, a missing data array, the 403 body and HTML are format errors")
    void envelope() {
        for (String body : new String[] {"{\"status\":403,\"success\":false,\"data\":[]}", "{\"status\":200}",
                "{\"message\":\"Access denied\"}", "<html>login</html>", "[]"}) {
            assertThatThrownBy(() -> AcledParser.events(body.getBytes(StandardCharsets.UTF_8))).as(body)
                    .isInstanceOf(UpstreamFormatException.class);
        }
        assertThat(AcledParser.events(AdapterFixtures.bytes("acled", "events-empty.json")).rows()).isZero();
    }

    @Test
    @DisplayName("every payload key is declared; the descriptor validates; LOCAL_ONLY; both credentials required")
    void descriptor() {
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), events().records());
        assertThat(DescriptorValidator.problems(ADAPTER.descriptor(), ADAPTER)).isEmpty();
        assertThat(ADAPTER.descriptor().egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(ADAPTER.descriptor().credentialFields()).extracting(f -> f.name() + ":" + f.required())
                .containsExactly("email:true", "password:true");
        assertThat(ADAPTER.descriptor().accessDeniedIsTerminal()).isTrue();
        assertThat(ADAPTER.descriptor().accessDeniedReasons()).containsEntry("Access denied",
                AcledAdapter.ACCESS_LEVEL_REASON);
        assertThat(ADAPTER.descriptor().licence()).isEqualTo("ACLED Terms of Use: no redistribution of raw data");
    }

    @Test
    @DisplayName("countries is required, numeric and at most 30; lookbackDays is 1 to 3650")
    void params() {
        assertThat(ADAPTER.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of())))
                .containsKey(AcledAdapter.COUNTRIES_PARAM);
        assertThat(ADAPTER.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("countries",
                List.of("804", "UKR"))))).containsKey(AcledAdapter.COUNTRIES_PARAM);
        assertThat(ADAPTER.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("countries",
                java.util.stream.IntStream.rangeClosed(1, 31).mapToObj(Integer::toString).toList()))))
                .containsKey(AcledAdapter.COUNTRIES_PARAM);
        assertThat(ADAPTER.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("countries",
                List.of("804", "643"), "lookbackDays", 0)))).containsOnlyKeys(AcledAdapter.LOOKBACK_PARAM);
        assertThat(ADAPTER.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("countries",
                List.of("804"))))).isEmpty();
    }

    @Test
    @DisplayName("a later timestamp with unchanged content is one static content")
    void stableStaticHash() {
        ObjectNode first = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("acled", "events.json")).get("data").get(1);
        ObjectNode second = first.deepCopy();
        second.put("timestamp", "1791815000");
        SnapshotContract.assertStableStaticHash(ADAPTER.descriptor(),
                List.of(AcledParser.event(first).record(), AcledParser.event(second).record()));
    }
}
