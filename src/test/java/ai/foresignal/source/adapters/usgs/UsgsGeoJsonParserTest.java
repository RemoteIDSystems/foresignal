package ai.foresignal.source.adapters.usgs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.ingest.ContentHasher;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.ingest.NormalizedBatch;
import ai.foresignal.ingest.Normalizer;
import ai.foresignal.ingest.SourceIngestProfile;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.json.JsonMapper;

/**
 * The parser contract for {@code usgs-earthquakes} (Architecture §4.2 rule 4): the recorded feed
 * parses with the Core-Data-Model §2 time base, the edge cases are skipped and counted, a format
 * change throws, and every fixture satisfies the fieldSpecs and snapshot contracts.
 */
class UsgsGeoJsonParserTest {

    private static final String SOURCE = UsgsEarthquakesAdapter.ID;
    private static final SourceDescriptor DESCRIPTOR = new UsgsEarthquakesAdapter("").descriptor();

    private static byte[] fixture(String name) {
        return AdapterFixtures.bytes(SOURCE, name);
    }

    private static List<NormalizedRecord> parse(String name) {
        return UsgsGeoJsonParser.parse(fixture(name));
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream()
                .filter(r -> r.externalId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no record " + id));
    }

    @Nested
    @DisplayName("the recorded all_day feed")
    class RecordedFeed {

        private final List<NormalizedRecord> records = parse("all_day.geojson");

        @Test
        @DisplayName("parses every one of its 25 features, in feed order")
        void count() {
            assertThat(records).hasSize(25);
            assertThat(records.getFirst().externalId()).isEqualTo("aka2026uekvdg");
            assertThat(records.getLast().externalId()).isEqualTo("us6000u1i6");
            assertThat(records).extracting(NormalizedRecord::kind).containsOnly("earthquake");
            assertThat(records).noneMatch(NormalizedRecord::tombstone);
        }

        @Test
        @DisplayName("reports no skips")
        void diagnostics() {
            UsgsGeoJsonParser.ParseDiagnostics diagnostics =
                    UsgsGeoJsonParser.parseWithDiagnostics(fixture("all_day.geojson")).diagnostics();
            assertThat(diagnostics.features()).isEqualTo(25);
            assertThat(diagnostics.records()).isEqualTo(25);
            assertThat(diagnostics.skipped()).isZero();
            assertThat(diagnostics.tombstones()).isZero();
        }

        @Test
        @DisplayName("eventTime is properties.time and observedAt is properties.updated")
        void timeBase() {
            NormalizedRecord quake = byId(records, "aka2026uekvdg");
            assertThat(quake.eventTime()).isEqualTo(Instant.ofEpochMilli(1791757094767L));
            assertThat(quake.observedAt()).isEqualTo(Instant.ofEpochMilli(1791757136341L));
            for (NormalizedRecord record : records) {
                assertThat(record.eventTime()).isEqualTo(Instant.ofEpochMilli((Long) record.payload().get("time")));
                assertThat(record.observedAt()).isEqualTo(Instant.ofEpochMilli((Long) record.payload().get("updated")));
            }
        }

        @Test
        @DisplayName("geometry is a 2D point in SRID 4326 and depth is in the payload")
        void geometryAndDepth() {
            NormalizedRecord quake = byId(records, "aka2026uekvdg");
            assertThat(quake.geometry()).isInstanceOf(Point.class);
            assertThat(quake.geometry().getSRID()).isEqualTo(4326);
            Point point = (Point) quake.geometry();
            assertThat(point.getX()).isEqualTo(-152.369);
            assertThat(point.getY()).isEqualTo(61.358);
            assertThat(point.getCoordinate().getZ()).isNaN();
            // coordinates[2] was the JSON integer 5; NUMBER fields are always doubles.
            assertThat(quake.payload()).containsEntry("depth", 5.0);

            NormalizedRecord negative = byId(records, "ci41346535");
            assertThat(negative.payload()).containsEntry("depth", -0.55);
            for (NormalizedRecord record : records) {
                assertThat(record.geometry().getCoordinate().getZ()).as(record.externalId()).isNaN();
                assertThat(record.payload()).containsKey("depth");
            }
        }

        @Test
        @DisplayName("spot-checked payload values have their declared types, and nulls are omitted")
        void payload() {
            NormalizedRecord quake = byId(records, "aka2026uekvdg");
            assertThat(quake.payload())
                    .containsEntry("mag", 0.7)
                    .containsEntry("magType", "ml")
                    .containsEntry("place", "73 km WNW of Beluga, Alaska")
                    .containsEntry("status", "automatic")
                    .containsEntry("tsunami", 0L)
                    .containsEntry("sig", 8L)
                    .containsEntry("nst", 9L)
                    .containsEntry("gap", 148.0)
                    .containsEntry("dmin", 0.1)
                    .containsEntry("rms", 0.9)
                    .containsEntry("net", "ak")
                    .containsEntry("code", "a2026uekvdg")
                    .containsEntry("ids", ",aka2026uekvdg,")
                    .containsEntry("type", "earthquake")
                    .containsEntry("title", "M 0.7 - 73 km WNW of Beluga, Alaska")
                    .containsEntry("url", "https://earthquake.usgs.gov/earthquakes/eventpage/aka2026uekvdg")
                    .doesNotContainKeys("felt", "cdi", "mmi", "alert", "tz");

            NormalizedRecord pager = byId(records, "us6000u1kv");
            assertThat(pager.payload()).containsEntry("alert", "green").containsEntry("mmi", 5.229).containsEntry("mag", 6.2);

            NormalizedRecord felt = byId(records, "tx2026uacobs");
            assertThat(felt.payload())
                    .containsEntry("felt", 4L)
                    .containsEntry("cdi", 3.3)
                    .containsEntry("mag", 4.0)
                    .containsEntry("status", "reviewed");

            for (NormalizedRecord record : records) {
                assertThat(record.payload()).doesNotContainValue(null);
                assertThat(UsgsGeoJsonParser.PAYLOAD_TYPES.keySet()).containsAll(record.payload().keySet());
                assertThat(record.validUntil()).isNull();
            }
        }

        @Test
        @DisplayName("an integer magnitude becomes a double, so 2 and 2.0 hash the same")
        void integerMagnitude() {
            assertThat(byId(records, "aka2026ueipfd").payload().get("mag")).isEqualTo(2.0);
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        private final UsgsGeoJsonParser.ParseResult result =
                UsgsGeoJsonParser.parseWithDiagnostics(fixture("edge-cases.geojson"));

        @Test
        @DisplayName("skips a feature without an id, geometry or updated, and counts each")
        void skips() {
            UsgsGeoJsonParser.ParseDiagnostics d = result.diagnostics();
            assertThat(d.features()).isEqualTo(6);
            assertThat(d.records()).isEqualTo(3);
            assertThat(d.skippedNoId()).isEqualTo(1);
            assertThat(d.skippedNoCoordinates()).isEqualTo(1);
            assertThat(d.skippedNoUpdated()).isEqualTo(1);
            assertThat(d.skippedMalformed()).isZero();
            assertThat(d.skippedBadCoordinates()).isZero();
            assertThat(d.skipped()).isEqualTo(3);
            assertThat(result.records())
                    .extracting(NormalizedRecord::externalId)
                    .containsExactly("edge-null-mag", "edge-no-depth", "edge-deleted");
        }

        @Test
        @DisplayName("null and missing optional fields are omitted from the payload")
        void omittedNulls() {
            NormalizedRecord quake = byId(result.records(), "edge-null-mag");
            assertThat(quake.payload())
                    .doesNotContainKeys("mag", "felt", "cdi", "mmi", "alert", "tz", "nst", "magType")
                    .containsEntry("depth", 7.5)
                    .containsEntry("tsunami", 0L)
                    .containsEntry("status", "automatic");
        }

        @Test
        @DisplayName("a feature without depth has no depth key and a 2D point")
        void missingDepth() {
            NormalizedRecord quake = byId(result.records(), "edge-no-depth");
            assertThat(quake.payload()).doesNotContainKey("depth").containsEntry("mag", 3.0).containsEntry("gap", 40.0);
            assertThat(quake.geometry().getCoordinate().getZ()).isNaN();
        }

        @Test
        @DisplayName("status deleted becomes a tombstone observed at updated")
        void deletedIsTombstone() {
            NormalizedRecord deleted = byId(result.records(), "edge-deleted");
            assertThat(deleted.tombstone()).isTrue();
            assertThat(deleted.kind()).isEqualTo("earthquake");
            assertThat(deleted.observedAt()).isEqualTo(Instant.ofEpochMilli(1791750180000L));
            assertThat(deleted.payload()).isEmpty();
            assertThat(deleted.geometry()).isNull();
            assertThat(result.diagnostics().tombstones()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("document shape")
    class Shape {

        @Test
        @DisplayName("a document that is not a FeatureCollection throws UpstreamFormatException with a snippet")
        void notAFeatureCollection() {
            assertThatThrownBy(() -> parse("not-a-feature-collection.json"))
                    .isInstanceOf(UpstreamFormatException.class)
                    .hasMessageContaining("FeatureCollection")
                    .satisfies(e -> assertThat(((UpstreamFormatException) e).getSnippet()).contains("\"error\""));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "{\"type\":\"FeatureCollection\"}",
            "{\"type\":\"FeatureCollection\",\"features\":{}}",
            "[1,2,3]",
            "{\"type\":\"Feature\",\"features\":[]}",
            "not json at all"
        })
        @DisplayName("a missing features array, a non-object or invalid JSON throws")
        void malformedDocuments(String body) {
            assertThatThrownBy(() -> UsgsGeoJsonParser.parse(body.getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(UpstreamFormatException.class);
        }

        @Test
        @DisplayName("an empty body throws")
        void emptyBody() {
            assertThatThrownBy(() -> UsgsGeoJsonParser.parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
            assertThatThrownBy(() -> UsgsGeoJsonParser.parse(null)).isInstanceOf(UpstreamFormatException.class);
        }

        @Test
        @DisplayName("the snippet is capped at 500 characters")
        void snippetCapped() {
            String body = "{\"error\":\"" + "x".repeat(5_000) + "\"}";
            assertThatThrownBy(() -> UsgsGeoJsonParser.parse(body.getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(UpstreamFormatException.class)
                    .satisfies(e -> assertThat(((UpstreamFormatException) e).getSnippet().length())
                            .isLessThanOrEqualTo(UpstreamFormatException.MAX_SNIPPET_LENGTH + 20));
        }

        @Test
        @DisplayName("an empty features array is an empty list, not an error")
        void emptyFeatures() {
            byte[] body = "{\"type\":\"FeatureCollection\",\"metadata\":{\"count\":0},\"features\":[]}"
                    .getBytes(StandardCharsets.UTF_8);
            assertThat(UsgsGeoJsonParser.parse(body)).isEmpty();
        }

        @Test
        @DisplayName("non-object features and features without properties count as malformed")
        void malformedFeatures() {
            byte[] body = ("{\"type\":\"FeatureCollection\",\"features\":[1,"
                            + "{\"type\":\"Feature\",\"id\":\"a\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2]}}]}")
                    .getBytes(StandardCharsets.UTF_8);
            UsgsGeoJsonParser.ParseResult result = UsgsGeoJsonParser.parseWithDiagnostics(body);
            assertThat(result.records()).isEmpty();
            assertThat(result.diagnostics().skippedMalformed()).isEqualTo(2);
        }

        @Test
        @DisplayName("out-of-range coordinates skip the feature instead of failing the fetch")
        void badCoordinates() {
            byte[] body = ("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"id\":\"b\","
                            + "\"properties\":{\"updated\":1791750060000},"
                            + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[200,95,1]}}]}")
                    .getBytes(StandardCharsets.UTF_8);
            UsgsGeoJsonParser.ParseResult result = UsgsGeoJsonParser.parseWithDiagnostics(body);
            assertThat(result.records()).isEmpty();
            assertThat(result.diagnostics().skippedBadCoordinates()).isEqualTo(1);
        }

        @Test
        @DisplayName("a numeric id is accepted as text")
        void numericId() {
            byte[] body = ("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"id\":12345,"
                            + "\"properties\":{\"updated\":1791750060000,\"time\":1791750000000},"
                            + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[10,20,3]}}]}")
                    .getBytes(StandardCharsets.UTF_8);
            assertThat(UsgsGeoJsonParser.parse(body)).extracting(NormalizedRecord::externalId).containsExactly("12345");
        }
    }

    @Nested
    @DisplayName("value coercion")
    class Coercion {

        private final JsonMapper json = JsonMapper.builder().build();

        @Test
        @DisplayName("values of the declared JSON type are converted, others are kept verbatim")
        void coerce() {
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("2"), FieldType.NUMBER)).isEqualTo(2.0);
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("12.0"), FieldType.INTEGER)).isEqualTo(12L);
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("12.5"), FieldType.INTEGER)).isEqualTo(12.5);
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("1791750060000"), FieldType.TIMESTAMP))
                    .isEqualTo(1791750060000L);
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("\"2.5\""), FieldType.NUMBER)).isEqualTo("2.5");
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("7"), FieldType.STRING)).isEqualTo(7L);
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("true"), FieldType.BOOLEAN)).isEqualTo(true);
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("true"), FieldType.STRING)).isEqualTo(true);
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("null"), FieldType.STRING)).isNull();
            assertThat(UsgsGeoJsonParser.coerce(json.readTree("{\"a\":1}"), FieldType.STRING)).isNull();
            assertThat(UsgsGeoJsonParser.coerce(null, FieldType.STRING)).isNull();
        }
    }

    @Nested
    @DisplayName("contracts")
    class Contracts {

        @ParameterizedTest
        @ValueSource(strings = {"all_day.geojson", "edge-cases.geojson", "revised.geojson", "updated-only.geojson"})
        @DisplayName("every fixture satisfies the fieldSpecs contract")
        void fieldSpecs(String name) {
            FieldSpecContract.assertPayloadKeysDeclared(DESCRIPTOR, parse(name));
        }

        @Test
        @DisplayName("every payload key the parser can emit is declared with the same type")
        void allowlistMatchesFieldSpecs() {
            assertThat(DESCRIPTOR.fieldSpecs().keySet())
                    .containsExactlyInAnyOrderElementsOf(UsgsGeoJsonParser.PAYLOAD_TYPES.keySet());
            UsgsGeoJsonParser.PAYLOAD_TYPES.forEach((key, type) ->
                    assertThat(DESCRIPTOR.fieldSpecs().get(key).type()).as(key).isEqualTo(type));
        }

        @Test
        @DisplayName("all_day and updated-only share one static hash: updated is volatile")
        void stableStaticHash() {
            NormalizedRecord before = byId(parse("all_day.geojson"), "us6000u1i6");
            NormalizedRecord after = byId(parse("updated-only.geojson"), "us6000u1i6");
            assertThat(after.observedAt()).isAfter(before.observedAt());
            SnapshotContract.assertStableStaticHash(DESCRIPTOR, List.of(before, after));
        }

        @Test
        @DisplayName("the revised event changes the static hash, so it persists as SIGNIFICANT")
        void revisionChangesStaticHash() {
            NormalizedRecord before = byId(parse("all_day.geojson"), "aka2026uekklg");
            NormalizedRecord revised = byId(parse("revised.geojson"), "aka2026uekklg");
            assertThat(revised.payload()).containsEntry("mag", 2.5).containsEntry("status", "reviewed");
            assertThat(revised.eventTime()).isEqualTo(before.eventTime());
            assertThat(staticHash(revised)).isNotEqualTo(staticHash(before));
        }

        private long staticHash(NormalizedRecord record) {
            SourceIngestProfile profile = SourceIngestProfile.forDescriptor(DESCRIPTOR);
            NormalizedBatch batch = new Normalizer().normalize(
                    profile, List.of(new IngestRecord(record, record.observedAt(), 0)));
            assertThat(batch.rejections()).isEmpty();
            return ContentHasher.staticHash(batch.observations().getFirst(), profile);
        }
    }

    @Test
    @DisplayName("the payload allowlist is the one in the prompt, in order")
    void allowlist() {
        assertThat(UsgsGeoJsonParser.PAYLOAD_TYPES.keySet()).containsExactly(
                "mag", "magType", "place", "time", "updated", "url", "detail", "felt", "cdi", "mmi", "alert",
                "status", "tsunami", "sig", "net", "code", "ids", "sources", "types", "nst", "dmin", "rms", "gap",
                "type", "title", "depth");
        assertThat(Map.copyOf(UsgsGeoJsonParser.PAYLOAD_TYPES)).doesNotContainKey("tz");
    }
}
