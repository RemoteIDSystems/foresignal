package ai.foresignal.source.adapters.fire;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link NifcPerimeterMapper} on the real WFIGS perimeters page (captured 2026-10-13, 24 generalized perimeters plus
 * the full-resolution Wrights Spring perimeter of 10,750 vertices).
 */
class NifcPerimeterMapperTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static List<JsonNode> features() {
        List<JsonNode> out = new ArrayList<>();
        JSON.readTree(AdapterFixtures.bytes("nifc-perimeters", "query-page-1.json")).get("features").forEach(out::add);
        return out;
    }

    static List<NormalizedRecord> records() {
        return features().stream().map(NifcPerimeterMapper::map).map(MappedRecord::record).toList();
    }

    static SourceDescriptor descriptor() {
        return new NifcPerimetersAdapter("", (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>)
                null).descriptor();
    }

    static JsonNode feature(String uniqueId) {
        return features().stream().filter(f -> uniqueId.equals(f.get("properties").get("attr_UniqueFireIdentifier")
                .asString())).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the fixture: 25 perimeters, every one kept, polygons in SRID 4326, the payload allowlist only")
    void fixture() {
        List<MappedRecord> mapped = features().stream().map(NifcPerimeterMapper::map).toList();
        assertThat(mapped).hasSize(25).allMatch(MappedRecord::accepted);
        List<NormalizedRecord> records = records();
        assertThat(records).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("fire-perimeter");
            assertThat(r.observedAt()).isNotNull();
            assertThat(r.geometry()).isNotNull();
            assertThat(r.geometry().getSRID()).isEqualTo(4326);
            assertThat(r.geometry().getGeometryType()).isIn("Polygon", "MultiPolygon");
            assertThat(r.geometry().getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
            assertThat(r.payload().keySet()).isSubsetOf("name", "gisAcres", "incidentSizeAcres", "percentContained",
                    "state", "county", "incidentType", "cause", "behavior", "personnel", "complexityLevel",
                    "complexName", "irwinId", FeatureGeometry.SIMPLIFIED_KEY, FeatureGeometry.TOLERANCE_KEY);
            assertThat(r.payload()).doesNotContainKeys("poly_IncidentName", "attr_UniqueFireIdentifier", "OBJECTID");
        });
        assertThat(records.stream().filter(r -> r.payload().containsKey("personnel"))).hasSize(23);
        assertThat(records.stream().filter(r -> r.payload().containsKey("percentContained"))).hasSize(24);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), records);
    }

    @Test
    @DisplayName("one perimeter in full: the unique fire id, poly_DateCurrent and discovery time, renamed payload")
    void onePerimeter() {
        NormalizedRecord r = records().getFirst();
        assertThat(r.externalId()).isEqualTo("2026-FLFNF-000419");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-08-12T16:02:43Z"));
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-02-10T11:30:00Z"));
        assertThat(r.payload()).containsEntry("name", "Nubys Corner Area").containsEntry("gisAcres", 2L)
                .containsEntry("incidentSizeAcres", 1.92).containsEntry("state", "US-FL")
                .containsEntry("county", "Marion").containsEntry("incidentType", "WF")
                .containsEntry("cause", "Undetermined").containsEntry("complexityLevel", "Type 5 Incident")
                .containsEntry("irwinId", "{F6CFE8BA-B59D-4733-9E58-15034B31F208}")
                .doesNotContainKeys("percentContained", "behavior", "personnel", "complexName",
                        FeatureGeometry.SIMPLIFIED_KEY);
    }

    @Test
    @DisplayName("the 10,750-vertex Wrights Spring perimeter is simplified to 9,500 or fewer and flagged")
    void simplification() {
        NormalizedRecord r = NifcPerimeterMapper.map(feature("2026-ORFWF-260286")).record();
        assertThat(r.geometry().getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
        assertThat(r.geometry().isValid()).isTrue();
        assertThat(r.payload()).containsEntry(FeatureGeometry.SIMPLIFIED_KEY, true)
                .containsEntry(FeatureGeometry.TOLERANCE_KEY, FeatureGeometry.START_TOLERANCE_DEG)
                .containsEntry("name", "Wrights Spring").containsEntry("gisAcres", 55134L)
                .containsEntry("percentContained", 100.0).containsEntry("personnel", 1L);
    }

    @Test
    @DisplayName("ids: the IRWIN id when the unique fire id is missing; neither skips the feature")
    void identity() {
        ObjectNode f = (ObjectNode) feature("2026-FLFNF-000419").deepCopy();
        ((ObjectNode) f.get("properties")).putNull("attr_UniqueFireIdentifier");
        assertThat(NifcPerimeterMapper.map(f).record().externalId()).isEqualTo("{F6CFE8BA-B59D-4733-9E58-15034B31F208}");
        ((ObjectNode) f.get("properties")).put("attr_IrwinID", "  ");
        assertThat(NifcPerimeterMapper.map(f).reason()).isEqualTo(MappedRecord.MISSING_ID);
    }

    @Test
    @DisplayName("time and geometry: no poly_DateCurrent skips; a null geometry keeps the record; a broken one skips")
    void timeAndGeometry() {
        ObjectNode noTime = (ObjectNode) feature("2026-FLFNF-000419").deepCopy();
        ((ObjectNode) noTime.get("properties")).remove("poly_DateCurrent");
        assertThat(NifcPerimeterMapper.map(noTime).reason()).isEqualTo(MappedRecord.MISSING_OBSERVED_AT);

        ObjectNode noGeometry = (ObjectNode) feature("2026-FLFNF-000419").deepCopy();
        noGeometry.putNull("geometry");
        NormalizedRecord r = NifcPerimeterMapper.map(noGeometry).record();
        assertThat(r.geometry()).isNull();
        assertThat(r.payload()).containsEntry("name", "Nubys Corner Area");

        ObjectNode broken = (ObjectNode) feature("2026-FLFNF-000419").deepCopy();
        broken.set("geometry", JSON.readTree("{\"type\": \"Polygon\", \"coordinates\": \"nope\"}"));
        assertThat(NifcPerimeterMapper.map(broken).reason()).isEqualTo(MappedRecord.BAD_GEOMETRY);
    }

    @Test
    @DisplayName("snapshot contract: two parses, and a personnel change alone, keep one static hash")
    void snapshot() {
        JsonNode f = feature("2026-ORFWF-260286");
        ObjectNode shift = (ObjectNode) f.deepCopy();
        ((ObjectNode) shift.get("properties")).put("attr_TotalIncidentPersonnel", 42);
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(NifcPerimeterMapper.map(f).record(),
                NifcPerimeterMapper.map(f).record(), NifcPerimeterMapper.map(shift).record()));
    }
}
