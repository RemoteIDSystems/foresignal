package ai.foresignal.inbound.edp;

import static ai.foresignal.inbound.edp.EdpFixtures.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** Manifest, batch and item validation (docs/External-Data-Protocol.md §2, §3, §3.1, §3.3). */
class EdpValidationTest {

    private final EdpManifestValidator manifests = EdpFixtures.manifestValidator();
    private final EdpItemValidator items = EdpFixtures.itemValidator();

    private static EdpItemValidator.ItemCheck check(String manifest, String... items) {
        EdpManifestValidator.ManifestCheck m = EdpFixtures.manifest(manifest);
        List<JsonNode> list = java.util.Arrays.stream(items).map(EdpFixtures::parse).toList();
        return EdpFixtures.itemValidator().validate(list, m.model(), m.payloadSchemas(), RECEIVED);
    }

    private static String onlyReason(EdpItemValidator.ItemCheck check) {
        assertThat(check.valid()).isEmpty();
        assertThat(check.rejected()).hasSize(1);
        return check.rejected().get(0).reason();
    }

    @Nested
    class Manifests {

        @Test
        @DisplayName("the specification's example manifest is valid and parses into typed fields")
        void example() {
            EdpManifestValidator.ManifestCheck check = manifests.validate(EdpFixtures.json("manifests/valid/hormuz-example.json"));

            assertThat(check.errors()).isEmpty();
            assertThat(check.valid()).isTrue();
            assertThat(check.model().sourceId()).isEqualTo("ext-hormuz-transits");
            assertThat(check.model().stream("incidents").mode()).isEqualTo("layer");
            assertThat(check.payloadSchemas()).containsOnlyKeys("transits", "incidents");
        }

        @Test
        @DisplayName("a protocol minor above maxMinor is PROTOCOL_UNSUPPORTED at registration")
        void protocolTooNew() {
            EdpManifestValidator.ManifestCheck check = manifests.validate(EdpFixtures.json("manifests/invalid/protocol-too-new.json"));

            assertThat(check.valid()).isFalse();
            assertThat(check.protocolUnsupported()).isTrue();
            assertThat(check.errors()).extracting(EdpSchemaError::path).contains("/protocol");
        }

        @Test
        @DisplayName("a manifest with neither push nor pull is refused")
        void emptyTransport() {
            EdpManifestValidator.ManifestCheck check = manifests.validate(EdpFixtures.json("manifests/invalid/empty-transport.json"));

            assertThat(check.valid()).isFalse();
            assertThat(check.errors()).anySatisfy(e -> assertThat(e.message()).contains("delivery mechanism"));
        }

        @Test
        @DisplayName("duplicate stream ids are refused")
        void duplicateStreams() {
            EdpManifestValidator.ManifestCheck check = manifests.validate(EdpFixtures.json("manifests/invalid/duplicate-stream-ids.json"));

            assertThat(check.errors()).anySatisfy(e -> assertThat(e.message()).contains("duplicate stream id"));
        }

        @Test
        @DisplayName("a layer stream carrying observation items is refused")
        void layerObservation() {
            EdpManifestValidator.ManifestCheck check = manifests.validate(EdpFixtures.json("manifests/invalid/layer-observation.json"));

            assertThat(check.valid()).isFalse();
            assertThat(check.errors()).extracting(EdpSchemaError::path).anySatisfy(p -> assertThat(p).startsWith("/streams/0"));
        }

        @Test
        @DisplayName("header auth without a header name, a short interval and a bad color are each reported")
        void transportAndPresentation() {
            ObjectNode manifest = (ObjectNode) EdpFixtures.json("manifests/valid/pull-atomic.json");
            ((ObjectNode) manifest.path("transport").path("pull")).remove("header");
            ((ObjectNode) manifest.path("transport").path("pull")).put("intervalSeconds", 30);
            ((ObjectNode) manifest.path("streams").get(0)).putObject("presentation").put("color", "red");

            EdpManifestValidator.ManifestCheck check = manifests.validate(manifest);

            assertThat(check.errors()).extracting(EdpSchemaError::path)
                    .contains("/transport/pull/header", "/transport/pull/intervalSeconds", "/streams/0/presentation/color");
        }
    }

    @Nested
    class Batches {

        private final EdpBatchReader reader = new EdpBatchReader();

        @Test
        @DisplayName("a batch naming another source is 400 SOURCE_MISMATCH; the provider id and ext- id both match")
        void sourceMismatch() {
            EdpManifestModel model = EdpFixtures.manifest("hormuz-example.json").model();

            assertThatThrownBy(() -> EdpBatchChecks.checkHeader(
                    EdpFixtures.parse("{\"protocol\":\"foresignal-edp/1.0\",\"sourceId\":\"other\"}"), model,
                    "ext-hormuz-transits"))
                    .isInstanceOfSatisfying(ForesignalException.class, e -> {
                        assertThat(e.getCode()).isEqualTo("SOURCE_MISMATCH");
                        assertThat(e.getStatus().value()).isEqualTo(400);
                    });
            EdpBatchChecks.checkHeader(EdpFixtures.parse("{\"protocol\":\"foresignal-edp/1.0\",\"sourceId\":\"hormuz-transits\"}"),
                    model, "ext-hormuz-transits");
            EdpBatchChecks.checkHeader(EdpFixtures.parse("{\"protocol\":\"foresignal-edp/1.0\",\"sourceId\":\"ext-hormuz-transits\"}"),
                    model, "ext-hormuz-transits");
        }

        @Test
        @DisplayName("a batch minor above the manifest's is PROTOCOL_UNSUPPORTED")
        void batchMinor() {
            EdpManifestModel model = EdpFixtures.manifest("hormuz-example.json").model();

            assertThatThrownBy(() -> EdpBatchChecks.checkHeader(
                    EdpFixtures.parse("{\"protocol\":\"foresignal-edp/1.1\"}"), model, "ext-hormuz-transits"))
                    .isInstanceOfSatisfying(ForesignalException.class,
                            e -> assertThat(e.getCode()).isEqualTo("PROTOCOL_UNSUPPORTED"));
        }

        @Test
        @DisplayName("nesting deeper than 32 levels is refused by the parser")
        void depth() {
            String deep = "{\"protocol\":\"foresignal-edp/1.0\",\"items\":[{\"properties\":"
                    + "{\"a\":".repeat(40) + "1" + "}".repeat(40) + "}]}";

            assertThatThrownBy(() -> reader.read(deep.getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON))
                    .isInstanceOfSatisfying(ForesignalException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
        }

        @Test
        @DisplayName("a body over 5 MB is 413 and more than 10,000 items is 400")
        void sizeLimits() {
            byte[] big = new byte[EdpProtocol.MAX_BATCH_BYTES + 1];
            java.util.Arrays.fill(big, (byte) ' ');
            assertThatThrownBy(() -> reader.read(big, MediaType.APPLICATION_JSON))
                    .isInstanceOfSatisfying(ForesignalException.class, e -> assertThat(e.getStatus().value()).isEqualTo(413));

            String many = "{\"protocol\":\"foresignal-edp/1.0\",\"items\":[" + "{},".repeat(10_000) + "{}]}";
            assertThatThrownBy(() -> reader.read(many.getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON))
                    .isInstanceOfSatisfying(ForesignalException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
        }

        @Test
        @DisplayName("NDJSON: the first line may be a header, every other line is an item")
        void ndjson() {
            EdpBatchReader.EdpBatch batch = reader.read(
                    EdpFixtures.text("batches/valid/event-and-delete.ndjson").getBytes(StandardCharsets.UTF_8),
                    EdpBatchReader.NDJSON);

            assertThat(batch.header().path("protocol").asString()).isEqualTo("foresignal-edp/1.0");
            assertThat(batch.items()).hasSize(2);
            assertThat(batch.items().get(1).path("type").asString()).isEqualTo("delete");
        }

        @Test
        @DisplayName("the snapshot member is read; a malformed one is 400")
        void snapshot() {
            EdpBatchChecks.EdpSnapshotRef ref = EdpBatchChecks.snapshot(EdpFixtures.parse(
                    "{\"snapshot\":{\"id\":\"s1\",\"stream\":\"cameras\",\"scope\":\"north\",\"page\":2,\"final\":true}}"));

            assertThat(ref).isEqualTo(new EdpBatchChecks.EdpSnapshotRef("s1", "cameras", "north", 2, true));
            assertThat(EdpBatchChecks.snapshot(EdpFixtures.parse("{\"snapshot\":null}"))).isNull();
            assertThatThrownBy(() -> EdpBatchChecks.snapshot(EdpFixtures.parse("{\"snapshot\":{\"id\":\"s\",\"page\":0}}")))
                    .isInstanceOf(ForesignalException.class);
        }
    }

    @Nested
    class Geometry {

        private static final String FEATURE = """
                {"stream":"notes","type":"document","id":"g","observedAt":"2026-10-11T17:00:00Z",
                 "documentType":"note","text":"x","geometry":%s}""";

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
            "longitude out of bounds | {\"type\":\"Point\",\"coordinates\":[181,0]}",
            "latitude out of bounds | {\"type\":\"Point\",\"coordinates\":[0,-91]}",
            "open ring | {\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1]]]}",
            "ring of three | {\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[0,0]]]}",
            "geometry collection | {\"type\":\"GeometryCollection\",\"geometries\":[]}",
            "four ordinates | {\"type\":\"Point\",\"coordinates\":[0,0,0,0]}"
        })
        @DisplayName("invalid geometry rejects the item")
        void invalid(String name, String geometry) {
            assertThat(onlyReason(check("push-layer.json", FEATURE.formatted(geometry))))
                    .isIn("GEOMETRY_INVALID", "SCHEMA_INVALID");
        }

        @Test
        @DisplayName("a geometry type the stream does not declare is GEOMETRY_INVALID")
        void wrongType() {
            String item = """
                    {"stream":"cameras","type":"feature","id":"c","observedAt":"2026-10-11T17:00:00Z",
                     "properties":{"name":"x"},"geometry":{"type":"LineString","coordinates":[[0,0],[1,1]]}}""";

            assertThat(onlyReason(check("push-layer.json", item))).isEqualTo("GEOMETRY_INVALID");
        }

        @Test
        @DisplayName("more than 10,000 vertices is GEOMETRY_INVALID")
        void tooManyVertices() {
            StringBuilder coords = new StringBuilder();
            for (int i = 0; i < 10_001; i++) {
                coords.append(i == 0 ? "" : ",").append("[").append(i % 100 / 10.0).append(",0]");
            }
            List<EdpSchemaError> errors = EdpGeometryValidator.validate(
                    EdpFixtures.parse("{\"type\":\"LineString\",\"coordinates\":[" + coords + "]}"), "any", "/geometry");

            assertThat(errors).anySatisfy(e -> assertThat(e.message()).contains("vertices"));
        }

        @Test
        @DisplayName("a valid polygon with altitude passes, and a geometry on a 'none' stream does not")
        void valid() {
            assertThat(EdpGeometryValidator.validate(EdpFixtures.parse(
                    "{\"type\":\"Polygon\",\"coordinates\":[[[0,0,10],[1,0,10],[1,1,10],[0,0,10]]]}"), "Polygon", "/g"))
                    .isEmpty();
            assertThat(EdpGeometryValidator.validate(EdpFixtures.parse("{\"type\":\"Point\",\"coordinates\":[0,0]}"),
                    "none", "/g")).isNotEmpty();
        }
    }

    @Nested
    class Periods {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
            "2026, year, 2026-01-01T00:00:00Z, 2027-01-01T00:00:00Z",
            "2026-Q3, quarter, 2026-07-01T00:00:00Z, 2026-10-01T00:00:00Z",
            "2026-02, month, 2026-02-01T00:00:00Z, 2026-03-01T00:00:00Z",
            "2026-W01, week, 2025-12-29T00:00:00Z, 2026-01-05T00:00:00Z",
            "2026-10-10, day, 2026-10-10T00:00:00Z, 2026-10-11T00:00:00Z",
            "2026-10-10T12:00:00+02:00, instant, 2026-10-10T10:00:00Z, 2026-10-10T10:00:00Z"
        })
        @DisplayName("each t format parses to a UTC interval")
        void formats(String t, String granularity, String start, String end) {
            EdpPeriods.Period period = EdpPeriods.parse(t);

            assertThat(period).isNotNull();
            assertThat(period.granularity()).isEqualTo(granularity);
            assertThat(period.start()).isEqualTo(java.time.Instant.parse(start));
            assertThat(period.end()).isEqualTo(java.time.Instant.parse(end));
        }

        @ParameterizedTest
        @CsvSource({"2026-13", "2026-Q5", "2026-W54", "2026-02-30", "2026-10-10T12:00:00", "26"})
        @DisplayName("an invalid t is null")
        void invalid(String t) {
            assertThat(EdpPeriods.parse(t)).isNull();
        }

        @Test
        @DisplayName("one series keeps one granularity: a day after a week in the same series is GRANULARITY_MIXED")
        void mixedGranularity() {
            String week = "{\"stream\":\"transits\",\"type\":\"observation\",\"id\":\"a\",\"observedAt\":\"2026-10-11T06:00:00Z\","
                    + "\"seriesKey\":\"tanker\",\"t\":\"2026-W41\",\"value\":1}";
            String day = "{\"stream\":\"transits\",\"type\":\"observation\",\"id\":\"b\",\"observedAt\":\"2026-10-11T06:00:00Z\","
                    + "\"seriesKey\":\"tanker\",\"t\":\"2026-10-10\",\"value\":2}";
            String other = "{\"stream\":\"transits\",\"type\":\"observation\",\"id\":\"c\",\"observedAt\":\"2026-10-11T06:00:00Z\","
                    + "\"seriesKey\":\"cargo\",\"t\":\"2026-10-10\",\"value\":3}";

            EdpItemValidator.ItemCheck check = check("hormuz-example.json", week, day, other);

            assertThat(check.valid()).extracting(EdpValidItem::index).containsExactly(0, 2);
            assertThat(check.rejected()).singleElement().satisfies(r -> {
                assertThat(r.reason()).isEqualTo("GRANULARITY_MIXED");
                assertThat(r.index()).isEqualTo(1);
            });
        }
    }

    @Nested
    class Forecasts {

        @Test
        @DisplayName("a forecast without a probability or quantiles is FORECAST_INVALID (or refused by the schema)")
        void noValue() {
            String item = "{\"stream\":\"odds\",\"type\":\"forecast\",\"id\":\"f\",\"madeAt\":\"2026-10-11T12:00:00Z\",\"questionRef\":\"q\"}";

            assertThat(onlyReason(check("push-layer.json", item))).isIn("FORECAST_INVALID", "SCHEMA_INVALID");
            assertThat(EdpItemValidator.forecastProblem(EdpFixtures.parse(item))).contains("probability or quantiles");
        }

        @Test
        @DisplayName("a forecast without a question or questionRef is refused")
        void noQuestion() {
            String item = "{\"stream\":\"odds\",\"type\":\"forecast\",\"id\":\"f\",\"madeAt\":\"2026-10-11T12:00:00Z\",\"probability\":0.4}";

            assertThat(onlyReason(check("push-layer.json", item))).isIn("FORECAST_INVALID", "SCHEMA_INVALID");
            assertThat(EdpItemValidator.forecastProblem(EdpFixtures.parse(item))).contains("question");
        }

        @Test
        @DisplayName("decreasing quantiles are FORECAST_INVALID; non-decreasing ones with the optional p10/p90 pass")
        void quantiles() {
            assertThat(EdpItemValidator.forecastProblem(EdpFixtures.parse(
                    "{\"questionRef\":\"q\",\"quantiles\":{\"p05\":1,\"p25\":2,\"p50\":3,\"p75\":2.5,\"p95\":5}}")))
                    .contains("non-decreasing");
            assertThat(EdpItemValidator.forecastProblem(EdpFixtures.parse(
                    "{\"questionRef\":\"q\",\"quantiles\":{\"p05\":1,\"p10\":1,\"p25\":2,\"p50\":3,\"p75\":4,\"p90\":4,\"p95\":5}}")))
                    .isNull();
            assertThat(EdpItemValidator.forecastProblem(EdpFixtures.parse(
                    "{\"questionRef\":\"q\",\"quantiles\":{\"p05\":1,\"p50\":3,\"p95\":5}}")))
                    .contains("p25");
        }
    }

    @Nested
    class Items {

        @Test
        @DisplayName("an unknown stream, a type mismatch and a missing observedAt are each rejected with their reason")
        void reasons() {
            assertThat(onlyReason(check("hormuz-example.json",
                    "{\"stream\":\"nope\",\"type\":\"delete\",\"id\":\"x\"}"))).isEqualTo("UNKNOWN_STREAM");
            assertThat(onlyReason(check("hormuz-example.json",
                    "{\"stream\":\"transits\",\"type\":\"event\",\"id\":\"x\",\"eventTime\":\"2026-10-11T00:00:00Z\","
                            + "\"observedAt\":\"2026-10-11T00:00:00Z\"}"))).isEqualTo("ITEM_TYPE_MISMATCH");
            assertThat(onlyReason(check("hormuz-example.json",
                    "{\"stream\":\"incidents\",\"type\":\"event\",\"id\":\"x\",\"eventTime\":\"2026-10-11T00:00:00Z\","
                            + "\"properties\":{\"category\":\"c\"}}"))).isEqualTo("MISSING_OBSERVED_AT");
        }

        @Test
        @DisplayName("strings over 2,048 characters and text over 20,000 are LIMIT_EXCEEDED; 21 media entries too")
        void limits() {
            String longTitle = "{\"stream\":\"notes\",\"type\":\"document\",\"id\":\"d\",\"observedAt\":\"2026-10-11T17:00:00Z\","
                    + "\"documentType\":\"note\",\"text\":\"x\",\"title\":\"" + "t".repeat(2049) + "\"}";
            String longText = "{\"stream\":\"notes\",\"type\":\"document\",\"id\":\"d\",\"observedAt\":\"2026-10-11T17:00:00Z\","
                    + "\"documentType\":\"note\",\"text\":\"" + "t".repeat(20_001) + "\"}";
            String media = "{\"url\":\"https://example.org/a.jpg\"},".repeat(20) + "{\"url\":\"https://example.org/b.jpg\"}";
            String manyMedia = "{\"stream\":\"notes\",\"type\":\"document\",\"id\":\"d\",\"observedAt\":\"2026-10-11T17:00:00Z\","
                    + "\"documentType\":\"note\",\"text\":\"x\",\"media\":[" + media + "]}";

            assertThat(onlyReason(check("push-layer.json", longTitle))).isEqualTo("LIMIT_EXCEEDED");
            assertThat(onlyReason(check("push-layer.json", longText))).isIn("LIMIT_EXCEEDED", "SCHEMA_INVALID");
            assertThat(onlyReason(check("push-layer.json", manyMedia))).isEqualTo("LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("valid items survive next to rejected ones in the same batch")
        void partial() {
            EdpItemValidator.ItemCheck check = check("hormuz-example.json",
                    "{\"stream\":\"incidents\",\"type\":\"delete\",\"id\":\"a\"}",
                    "{\"stream\":\"incidents\",\"type\":\"bogus\",\"id\":\"b\"}",
                    "{\"type\":\"status\",\"health\":\"ok\"}");

            assertThat(check.valid()).extracting(EdpValidItem::index).containsExactly(0, 2);
            assertThat(check.valid().get(1).stream()).isNull();
            assertThat(check.rejected()).singleElement().satisfies(r -> {
                assertThat(r.index()).isEqualTo(1);
                assertThat(r.stream()).isEqualTo("incidents");
                assertThat(r.id()).isEqualTo("b");
            });
        }

        @Test
        @DisplayName("an item failing the payload schema carries schema errors under /properties")
        void payloadErrors() {
            EdpItemRejection rejection = check("hormuz-example.json",
                    "{\"stream\":\"incidents\",\"type\":\"event\",\"id\":\"x\",\"eventTime\":\"2026-10-11T00:00:00Z\","
                            + "\"observedAt\":\"2026-10-11T00:00:00Z\",\"properties\":{\"vessel\":7}}").rejected().get(0);

            assertThat(rejection.reason()).isEqualTo("PAYLOAD_INVALID");
            assertThat(rejection.schemaErrors()).isNotEmpty()
                    .allSatisfy(e -> assertThat(e.path()).startsWith("/properties"));
        }

        @Test
        @DisplayName("without a compiled payload schema, properties are not checked")
        void noSchemas() {
            EdpManifestModel model = EdpFixtures.manifest("hormuz-example.json").model();
            EdpItemValidator.ItemCheck result = items.validate(List.of(EdpFixtures.parse(
                    "{\"stream\":\"incidents\",\"type\":\"event\",\"id\":\"x\",\"eventTime\":\"2026-10-11T00:00:00Z\","
                            + "\"observedAt\":\"2026-10-11T00:00:00Z\"}")), model, Map.of(), RECEIVED);

            assertThat(result.valid()).hasSize(1);
        }
    }
}
