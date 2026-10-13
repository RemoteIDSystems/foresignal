package ai.foresignal.source.adapters.nhc;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.nhc.NhcLayerResolver.Layer;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link NhcConeMapper} on the real NHC summary MapServer layers (captured 2026-10-13: Hurricane Simon and Tropical
 * Depression Twenty-One-E): the storm id equals prompt 067's {@code nhc-cyclones} id, the per-kind ids, the time
 * base, the 9999 placeholder and the dropped fields.
 */
class NhcConeMapperTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant FILED_SIMON = Instant.parse("2026-10-13T18:02:27Z");

    static List<JsonNode> features(Layer layer) {
        List<JsonNode> out = new ArrayList<>();
        JSON.readTree(AdapterFixtures.bytes("nhc-cyclone-cones", "query-" + layer.key() + ".json")).get("features")
                .forEach(out::add);
        return out;
    }

    static List<NormalizedRecord> records(Layer layer) {
        return NhcConeMapper.map(layer, features(layer)).stream().map(MappedRecord::record).toList();
    }

    static SourceDescriptor descriptor() {
        return new NhcCycloneConesAdapter("", (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>)
                null).descriptor();
    }

    @Test
    @DisplayName("every layer: every feature kept, storm ids in nhc-cyclones' format, no upstream bookkeeping fields")
    void allLayers() {
        List<NormalizedRecord> all = new ArrayList<>();
        for (Layer layer : Layer.values()) {
            List<MappedRecord> mapped = NhcConeMapper.map(layer, features(layer));
            assertThat(mapped).as(layer.name()).allMatch(MappedRecord::accepted);
            mapped.forEach(m -> all.add(m.record()));
        }
        assertThat(all).hasSize(18 + 2 + 2 + 3).allSatisfy(r -> {
            String stormId = (String) r.payload().get("stormId");
            assertThat(NhcCurrentStormsParser.ID.matcher(stormId).matches()).as(stormId).isTrue();
            assertThat(stormId).isIn("ep202026", "ep212026");
            assertThat(r.payload().keySet()).noneMatch(k -> k.startsWith("idp_") || k.startsWith("st_")
                    || k.equals("objectid") || k.equals("binnumber") || k.equals("shape") || k.equals("lat")
                    || k.equals("lon") || k.equals("datelbl") || k.equals("fldatelbl") || k.equals("timezone")
                    || k.equals("stormsrc"));
            assertThat(r.geometry()).isNotNull();
        });
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), all);
    }

    @Test
    @DisplayName("cones and tracks: the storm id (the track suffixed :track); observedAt the product time; no eventTime")
    void conesAndTracks() {
        List<NormalizedRecord> cones = records(Layer.CONE);
        assertThat(cones).extracting(NormalizedRecord::externalId).containsExactly("ep202026", "ep212026");
        NormalizedRecord simon = cones.getFirst();
        assertThat(simon.kind()).isEqualTo("cyclone-cone");
        assertThat(simon.observedAt()).isEqualTo(FILED_SIMON);
        assertThat(simon.eventTime()).isNull();
        assertThat(simon.geometry().getGeometryType()).isEqualTo("Polygon");
        assertThat(simon.payload()).containsEntry("stormName", "Simon").containsEntry("stormType", "HU")
                .containsEntry("basin", "EP").containsEntry("advisoryNumber", "25A")
                .containsEntry("advisoryDate", "1100 AM MST Tue Oct 13 2026").containsEntry("forecastPeriod", 120L)
                .doesNotContainKeys("tau", "warningType");
        assertThat(records(Layer.TRACK)).extracting(NormalizedRecord::externalId).containsExactly("ep202026:track",
                "ep212026:track");
        assertThat(records(Layer.TRACK).getFirst().payload()).containsEntry("stormId", "ep202026");
        assertThat(records(Layer.TRACK).getFirst().kind()).isEqualTo("cyclone-track");
    }

    @Test
    @DisplayName("warnings: stormId:<tcww>:<n>, n counting each type from 1 in layer order")
    void warnings() {
        List<NormalizedRecord> warnings = records(Layer.WARNING);
        assertThat(warnings).extracting(NormalizedRecord::externalId).containsExactly("ep202026:TWA:1",
                "ep202026:HWA:1", "ep202026:HWA:2");
        assertThat(warnings).allSatisfy(r -> assertThat(r.kind()).isEqualTo("cyclone-warning"));
        assertThat(warnings.getFirst().payload()).containsEntry("warningType", "TWA");
    }

    @Test
    @DisplayName("forecast points: stormId:tau<tau>, eventTime = validtime, 9999 placeholders left out")
    void points() {
        List<NormalizedRecord> points = records(Layer.POINTS);
        assertThat(points).hasSize(18);
        NormalizedRecord now = points.getFirst();
        assertThat(now.externalId()).isEqualTo("ep202026:tau0");
        assertThat(now.kind()).isEqualTo("cyclone-forecast-point");
        assertThat(now.eventTime()).isEqualTo(Instant.parse("2026-10-13T15:00:00Z"));
        assertThat(now.payload()).containsEntry("tau", 0L).containsEntry("validTime", "2026-10-13T15:00:00Z")
                .containsEntry("maxWindKt", 80L).containsEntry("gustKt", 100L).containsEntry("mslpHpa", 977L)
                .containsEntry("development", "Hurricane").containsEntry("label", "H")
                .containsEntry("motionDirDeg", 270L).containsEntry("motionSpeedKt", 2L)
                .containsEntry("saffirSimpson", 1L).containsEntry("stormName", "Hurricane Simon");
        NormalizedRecord later = points.get(1);
        assertThat(later.externalId()).isEqualTo("ep202026:tau12");
        assertThat(later.eventTime()).isEqualTo(Instant.parse("2026-10-14T00:00:00Z"));
        assertThat(later.payload()).doesNotContainKeys("mslpHpa", "motionDirDeg", "motionSpeedKt");
        assertThat(points.get(9).externalId()).isEqualTo("ep212026:tau0");
    }

    @Test
    @DisplayName("the storm id without idp_source: basin, two-digit number and the advisory year")
    void stormIdFallback() {
        ObjectNode f = (ObjectNode) features(Layer.CONE).getFirst().deepCopy();
        ObjectNode p = (ObjectNode) f.get("properties");
        p.remove("idp_source");
        p.put("basin", "AL");
        p.put("stormnum", 9);
        assertThat(NhcConeMapper.map(Layer.CONE, List.of(f)).getFirst().record().externalId()).isEqualTo("al092026");
        p.remove("advdate");
        assertThat(NhcConeMapper.map(Layer.CONE, List.of(f)).getFirst().record().externalId()).as("year of the"
                + " product time").isEqualTo("al092026");
        p.put("basin", "WP");
        assertThat(NhcConeMapper.map(Layer.CONE, List.of(f)).getFirst().reason()).as("not an NHC basin")
                .isEqualTo(MappedRecord.MISSING_ID);
        p.remove("idp_filedate");
        assertThat(NhcConeMapper.map(Layer.CONE, List.of(f)).getFirst().reason())
                .isEqualTo(MappedRecord.MISSING_OBSERVED_AT);
    }

    @ParameterizedTest(name = "validtime {0} filed {1} → {2}")
    @CsvSource({"13/1500, 2026-10-13T18:02:27Z, 2026-10-13T15:00:00Z", "02/0600, 2026-10-30T21:00:00Z,"
            + " 2026-11-02T06:00:00Z", "31/1800, 2026-11-01T03:00:00Z, 2026-10-31T18:00:00Z",
            "01/0000, 2026-12-31T21:00:00Z, 2027-01-01T00:00:00Z"})
    void validTime(String text, String filed, String expected) {
        assertThat(NhcConeMapper.validTime(text, Instant.parse(filed))).isEqualTo(Instant.parse(expected));
    }

    @Test
    @DisplayName("an unreadable validtime is no eventTime")
    void badValidTime() {
        assertThat(NhcConeMapper.validTime("13-1500", FILED_SIMON)).isNull();
        assertThat(NhcConeMapper.validTime("32/1500", FILED_SIMON)).isNull();
        assertThat(NhcConeMapper.validTime("13/2500", FILED_SIMON)).isNull();
        assertThat(NhcConeMapper.validTime(null, FILED_SIMON)).isNull();
    }

    @Test
    @DisplayName("snapshot contract: two parses of the cone and of a point keep one static hash")
    void snapshot() {
        JsonNode cone = features(Layer.CONE).getFirst();
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(
                NhcConeMapper.map(Layer.CONE, List.of(cone)).getFirst().record(),
                NhcConeMapper.map(Layer.CONE, List.of(cone)).getFirst().record()));
        JsonNode point = features(Layer.POINTS).getFirst();
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(
                NhcConeMapper.map(Layer.POINTS, List.of(point)).getFirst().record(),
                NhcConeMapper.map(Layer.POINTS, List.of(point)).getFirst().record()));
    }
}
