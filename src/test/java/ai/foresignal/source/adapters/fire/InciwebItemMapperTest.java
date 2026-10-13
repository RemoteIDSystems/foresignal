package ai.foresignal.source.adapters.fire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedItem;
import ai.foresignal.source.kit.feed.FeedItemMappers;
import ai.foresignal.source.kit.feed.FeedParser;
import ai.foresignal.source.kit.feed.FeedRecords;
import ai.foresignal.source.kit.feed.FeedSpecValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Point;

/**
 * {@link InciwebItemMapper} on InciWeb's real RSS feed (captured 2026-10-13, 25 items) and hand-made edge cases:
 * degrees-minutes-seconds without symbols, the US envelope, the title split and the payload.
 */
class InciwebItemMapperTest {

    static FeedDocument document(String name) {
        return FeedParser.parse(AdapterFixtures.bytes("inciweb-incidents", name), "application/rss+xml");
    }

    static List<NormalizedRecord> records(String name) {
        return FeedRecords.map(InciwebIncidentsAdapter.SPEC, document(name)).records();
    }

    static NormalizedRecord record(String name, String id) {
        return records(name).stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    static SourceDescriptor descriptor() {
        return new InciwebIncidentsAdapter("", (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>)
                null).descriptor();
    }

    @Test
    @DisplayName("the mapper is reviewed, and the source's spec passes the feed-kit rules (its id is not feed-*)")
    void reviewed() {
        assertThat(FeedItemMappers.of(InciwebItemMapper.NAME)).isInstanceOf(InciwebItemMapper.class);
        assertThat(FeedSpecValidator.problems(InciwebIncidentsAdapter.SPEC, false))
                .allMatch(p -> p.startsWith("id:"));
    }

    @Test
    @DisplayName("the fixture: 25 fire-incident records, 20 points, two coordinate lines refused, no authors")
    void fixture() {
        FeedDocument doc = document("rss.xml");
        List<NormalizedRecord> records = records("rss.xml");
        assertThat(records).hasSize(25).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("fire-incident");
            assertThat(r.observedAt()).as("receipt time base").isNull();
            assertThat(r.eventTime()).isNotNull();
            assertThat(r.payload().keySet()).isSubsetOf("name", "unitCode", "incidentType", "state", "lastUpdated",
                    "overview", "link");
            assertThat(r.payload()).doesNotContainKeys("title", "summary", "authors", "feedTitle");
        });
        assertThat(records.stream().filter(r -> r.geometry() != null)).hasSize(20);
        assertThat(doc.items().stream().filter(i -> InciwebItemMapper.coordinates(i.summary()).rejected())).hasSize(2);
        assertThat(records).allSatisfy(r -> {
            Object overview = r.payload().get("overview");
            if (overview != null) {
                assertThat(overview.toString().getBytes(StandardCharsets.UTF_8).length)
                        .isLessThanOrEqualTo(InciwebItemMapper.MAX_OVERVIEW_BYTES);
            }
        });
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), records);
    }

    @Test
    @DisplayName("one item in full: the Bull Fire, its unit code, type, state, date, point and link")
    void bullFire() {
        NormalizedRecord r = record("rss.xml", "332431");
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-10-02T14:13:48Z"));
        assertThat(r.payload()).containsEntry("name", "Bull Fire").containsEntry("unitCode", "NVTMFX")
                .containsEntry("incidentType", "Wildfire").containsEntry("state", "Nevada")
                .containsEntry("lastUpdated", "2026-10-12")
                .containsEntry("link", "http://inciweb.wildfire.gov/incident-information/nvtmfx-bull-fire");
        assertThat(r.payload().get("overview").toString()).contains("Bull Ranch Creek");
        Point p = (Point) r.geometry();
        assertThat(p.getY()).isCloseTo(39.0 + 32 / 60.0 + 21 / 3600.0, within(1e-9));
        assertThat(p.getX()).isCloseTo(-(119.0 + 59 / 60.0 + 16 / 3600.0), within(1e-9));
        assertThat(p.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("odd but valid lines: decimal seconds, a three-digit seconds field, a space before the degree sign")
    void oddLines() {
        assertThat(InciwebItemMapper.coordinates("Latitude: 46° 36 43.578  Longitude: 111° 59 16.8216").latitude())
                .isCloseTo(46.612105, within(1e-6));
        assertThat(InciwebItemMapper.coordinates("Latitude: 38° 43 015  Longitude: 108° 3 12.84").latitude())
                .isCloseTo(38.0 + 43 / 60.0 + 15 / 3600.0, within(1e-9));
        InciwebItemMapper.Coordinates spaced = InciwebItemMapper.coordinates(
                "Latitude: 44 ° 15 7  Longitude: 85 ° 24 4          ");
        assertThat(spaced.accepted()).isTrue();
        assertThat(spaced.longitude()).isCloseTo(-(85.0 + 24 / 60.0 + 4 / 3600.0), within(1e-9));
        assertThat(InciwebItemMapper.coordinates("Latitude: 40° 40 28.56  Longitude: 105 ° 31  11.97").accepted())
                .isTrue();
    }

    @Test
    @DisplayName("refused lines: seconds or minutes of 60 or more, a position outside the US; none without a line")
    void refused() {
        assertThat(InciwebItemMapper.coordinates("Latitude: 46° 49 76  Longitude: 113° 55 73").rejected()).isTrue();
        assertThat(InciwebItemMapper.coordinates("Latitude: 46° 60 0  Longitude: 113° 55 3").rejected()).isTrue();
        assertThat(InciwebItemMapper.coordinates("Latitude: 51° 30 0  Longitude: 0° 7 30").rejected()).isTrue();
        assertThat(InciwebItemMapper.coordinates("State: Utah").present()).isFalse();
        assertThat(InciwebItemMapper.coordinates(null).present()).isFalse();
    }

    @ParameterizedTest(name = "{0},{1} in the US envelope: {2}")
    @CsvSource({"39.5, -120.0, true", "64.8, -147.7, true", "19.7, -155.5, true", "18.2, -66.5, true",
            "47.6, -122.3, true", "49.38, -95.15, true", "51.5, -0.1, false", "45.4, -75.7, true", "60.0, -100.0, false",
            "13.47, -144.78, false", "10.0, -80.0, false"})
    void envelope(double lat, double lon, boolean inside) {
        assertThat(InciwebItemMapper.inUsEnvelope(lat, lon)).isEqualTo(inside);
    }

    @Test
    @DisplayName("edge cases: no coordinates, Alaska, outside the US, Guam east of 180°, bad seconds")
    void edgeCases() {
        List<NormalizedRecord> records = records("edge-cases.xml");
        assertThat(records).hasSize(5);
        NormalizedRecord noCoordinates = record("edge-cases.xml", "900001");
        assertThat(noCoordinates.geometry()).isNull();
        assertThat(noCoordinates.payload()).containsEntry("unitCode", "AKFAS").containsEntry("name",
                "Alaska Test Fire").containsEntry("state", "Alaska").containsEntry("incidentType", "Wildfire")
                .containsEntry("lastUpdated", "2026-10-12")
                .containsEntry("overview", "No coordinates are published for this incident.");
        assertThat(noCoordinates.eventTime()).isEqualTo(Instant.parse("2026-10-12T16:00:00Z"));
        Point alaska = (Point) record("edge-cases.xml", "900002").geometry();
        assertThat(alaska.getY()).isCloseTo(64.84, within(1e-9));
        assertThat(alaska.getX()).isCloseTo(-147.72, within(1e-9));
        NormalizedRecord outside = record("edge-cases.xml", "900003");
        assertThat(outside.geometry()).isNull();
        assertThat(outside.payload()).containsEntry("name", "Smith Fire").doesNotContainKey("unitCode");
        Point guam = (Point) record("edge-cases.xml", "900004").geometry();
        assertThat(guam.getX()).isCloseTo(144.0 + 47 / 60.0, within(1e-9));
        assertThat(record("edge-cases.xml", "900005").geometry()).isNull();
        long refused = document("edge-cases.xml").items().stream().map(FeedItem::summary)
                .filter(s -> InciwebItemMapper.coordinates(s).rejected()).count();
        assertThat(refused).isEqualTo(2);
    }

    @ParameterizedTest(name = "title \"{0}\" → unit {1}, name {2}")
    @CsvSource(delimiter = '|', value = {"NVTMFX Bull Fire|NVTMFX|Bull Fire", "WAOWF Sisi Fire|WAOWF|Sisi Fire",
            "MNSUF 2026 Superior National Forest Planned Prescribed FireMNSUF|MNSUF|2026 Superior National Forest"
                    + " Planned Prescribed FireMNSUF",
            "Smith Fire||Smith Fire", "CAL Fire Update||CAL Fire Update", "AB Fire||AB Fire"})
    void titleSplit(String title, String unit, String name) {
        String[] split = InciwebItemMapper.splitTitle(title);
        assertThat(split[0]).isEqualTo(unit);
        assertThat(split[1]).isEqualTo(name);
    }

    @Test
    @DisplayName("the description sections: an empty date, units, a state; the overview is cut to 2 KB")
    void sections() {
        InciwebItemMapper.Parsed p = InciwebItemMapper.parse("Last updated: --- The type of incident is Prescribed"
                + " Fire and involves the following unit(s) Grand Mesa Uncompahgre and Gunnison National Forests. ---"
                + " State: Colorado --- Incident Overview: " + "é".repeat(3000));
        assertThat(p.lastUpdated()).isNull();
        assertThat(p.incidentType()).isEqualTo("Prescribed Fire");
        assertThat(p.units()).isEqualTo("Grand Mesa Uncompahgre and Gunnison National Forests");
        assertThat(p.state()).isEqualTo("Colorado");
        assertThat(p.overview()).hasSize(1024);
        assertThat(InciwebItemMapper.parse(null).coordinates().present()).isFalse();
    }

    @Test
    @DisplayName("snapshot contract: two parses of one item keep one static hash")
    void snapshot() {
        NormalizedRecord a = record("rss.xml", "332431");
        NormalizedRecord b = record("rss.xml", "332431");
        Instant at = Instant.parse("2026-10-13T19:30:00Z");
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(withObservedAt(a, at), withObservedAt(b,
                at.plusSeconds(1800))));
    }

    /** {@code r} received at {@code at} (the RECEIVED time base stamps receipt at ingest). */
    static NormalizedRecord withObservedAt(NormalizedRecord r, Instant at) {
        return new NormalizedRecord(r.externalId(), r.kind(), at, r.eventTime(), r.geometry(), r.validUntil(),
                r.validFrom(), r.payload(), r.tombstone(), r.quality(), r.observers());
    }
}
