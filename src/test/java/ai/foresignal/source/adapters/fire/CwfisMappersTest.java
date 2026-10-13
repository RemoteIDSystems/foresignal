package ai.foresignal.source.adapters.fire;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link CwfisMappers} on the real CWFIS GeoServer pages (captured 2026-10-13): padded strings, the id-stability
 * fallback of the m3 polygons, the surface station winning a repeated {@code aes}, and the hotspot id.
 */
class CwfisMappersTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static List<JsonNode> features(String source, String name) {
        List<JsonNode> out = new ArrayList<>();
        JSON.readTree(AdapterFixtures.bytes(source, name)).get("features").forEach(out::add);
        return out;
    }

    static FireFetch.Batch batch(String source, Function<JsonNode, MappedRecord> mapper,
            Function<List<JsonNode>, List<JsonNode>> reorder) {
        return FireFetch.map(reorder.apply(features(source, "page.json")), mapper);
    }

    static SourceDescriptor descriptor(String id) {
        return new CwfisAdapter(CwfisLayer.reviewed(id), "", null).descriptor();
    }

    @Test
    @DisplayName("the three descriptors: kinds, TTLs, tombstones, retention by class, OGL-Canada and attribution")
    void descriptors() {
        SourceDescriptor perimeters = descriptor("cwfis-perimeters");
        assertThat(perimeters.kinds()).containsExactly("fire-perimeter");
        assertThat(perimeters.recordTtl()).isNull();
        assertThat(perimeters.description()).contains("NOT agency-mapped");
        assertThat(perimeters.defaultRetention()).isEqualTo(Duration.ofDays(1825));
        SourceDescriptor stations = descriptor("cwfis-fwi-stations");
        assertThat(stations.kinds()).containsExactly("fire-weather");
        assertThat(stations.recordTtl()).isEqualTo(Duration.ofHours(36));
        assertThat(stations.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(stations.defaultRetention()).as("state class").isEqualTo(Duration.ofDays(365));
        SourceDescriptor hotspots = descriptor("cwfis-hotspots");
        assertThat(hotspots.kinds()).containsExactly("fire-detection");
        assertThat(hotspots.recordTtl()).isEqualTo(Duration.ofHours(24));
        assertThat(hotspots.description()).contains("nasa-firms (prompt 065) is the primary hotspot source");
        assertThat(hotspots.kindRetention().get("fire-detection")).as("Weather-Imagery-Design §2.4")
                .isEqualTo(Duration.ofDays(7));
        for (CwfisLayer layer : CwfisLayer.LAYERS) {
            SourceDescriptor d = descriptor(layer.id());
            assertThat(d.licence()).isEqualTo("Open Government Licence – Canada");
            assertThat(d.attribution()).isEqualTo(
                    "Natural Resources Canada, Canadian Wildland Fire Information System");
        }
        assertThat(CwfisLayer.reviewed("cwfis-fwi-stations").filters()).containsEntry("sortBy", "aes");
        assertThat(CwfisLayer.LAYERS).extracting(CwfisLayer::typeName).doesNotContain("public:fdr_current_shp");
    }

    @Test
    @DisplayName("m3 polygons: 24 records, the repeated upstream row dropped, ids firstdate@lat,lon, observedAt lastdate")
    void perimeters() {
        FireFetch.Batch b = batch("cwfis-perimeters", CwfisMappers::perimeter, l -> l);
        assertThat(b.records()).hasSize(24);
        assertThat(b.skipped()).containsExactly(java.util.Map.entry(MappedRecord.DUPLICATE_ID, 1));
        NormalizedRecord first = b.records().getFirst();
        assertThat(first.externalId()).isEqualTo("2026-05-13T18:09:00Z@51.73,-98.58");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-05-26T19:51:00Z"));
        assertThat(first.eventTime()).isEqualTo(Instant.parse("2026-05-13T18:09:00Z"));
        assertThat(first.payload()).containsEntry("hotspotCount", 29L).containsEntry("areaHa", 91.03685)
                .containsEntry("firstDetected", "2026-05-13T18:09:00Z")
                .containsEntry("lastDetected", "2026-05-26T19:51:00Z").containsEntry("perimeterSource", "m3");
        assertThat(b.records()).allSatisfy(r -> {
            assertThat(r.externalId()).matches("^\\d{4}-\\d{2}-\\d{2}T[0-9:]+Z@-?\\d+\\.\\d{2},-?\\d+\\.\\d{2}$");
            assertThat(r.geometry().getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
            assertThat(r.payload()).doesNotContainKeys("hcount", "area", "firstdate");
        });
        NormalizedRecord big = b.records().stream().filter(r -> r.externalId().startsWith("2026-05-31T18:17:00Z@"))
                .findFirst().orElseThrow();
        assertThat(big.payload()).containsEntry(FeatureGeometry.SIMPLIFIED_KEY, true)
                .containsEntry("hotspotCount", 51352L);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor("cwfis-perimeters"), b.records());
    }

    @Test
    @DisplayName("the id does not depend on the GeoServer feature id, which is rewritten when a polygon grows")
    void idStability() {
        JsonNode f = features("cwfis-perimeters", "page.json").getFirst();
        ObjectNode renumbered = (ObjectNode) f.deepCopy();
        renumbered.put("id", "m3_polygons_current.999999");
        assertThat(CwfisMappers.perimeter(renumbered).record().externalId())
                .isEqualTo(CwfisMappers.perimeter(f).record().externalId());
        ObjectNode noFirst = (ObjectNode) f.deepCopy();
        ((ObjectNode) noFirst.get("properties")).remove("firstdate");
        assertThat(CwfisMappers.perimeter(noFirst).reason()).isEqualTo(MappedRecord.MISSING_ID);
        ObjectNode noLast = (ObjectNode) f.deepCopy();
        ((ObjectNode) noLast.get("properties")).remove("lastdate");
        assertThat(CwfisMappers.perimeter(noLast).reason()).isEqualTo(MappedRecord.MISSING_OBSERVED_AT);
    }

    @Test
    @DisplayName("stations: padded names and agencies trimmed; the surface Baker Lake entry wins the repeated aes")
    void stations() {
        FireFetch.Batch b = batch("cwfis-fwi-stations", CwfisMappers::station, CwfisMappers::stationOrder);
        assertThat(b.records()).hasSize(24);
        assertThat(b.skipped()).containsExactly(java.util.Map.entry(MappedRecord.DUPLICATE_ID, 1));
        NormalizedRecord baker = b.records().stream().filter(r -> r.externalId().equals("2300503")).findFirst()
                .orElseThrow();
        assertThat(baker.payload()).containsEntry("name", "BAKER+LAKE+A").containsEntry("elevationM", 18.0)
                .containsEntry("agency", "MSC").containsEntry("province", "NU").containsEntry("tempC", 1.9)
                .containsEntry("rhPct", 92.0).containsEntry("windKph", 19.1).containsEntry("windDirDeg", 331.0)
                .containsEntry("precipMm", 0.2).doesNotContainKeys("ua", "instr", "pres", "vis", "aes", "wmo");
        assertThat(baker.observedAt()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(baker.eventTime()).isEqualTo(baker.observedAt());
        assertThat(b.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("fire-weather");
            String name = (String) r.payload().get("name");
            assertThat(name).isEqualTo(name.strip());
            assertThat(r.geometry()).isInstanceOf(Point.class);
        });
        FieldSpecContract.assertPayloadKeysDeclared(descriptor("cwfis-fwi-stations"), b.records());
    }

    @Test
    @DisplayName("a station without aes falls back to its WMO number; without either it is skipped")
    void stationIds() {
        ObjectNode f = (ObjectNode) features("cwfis-fwi-stations", "page.json").get(2).deepCopy();
        ObjectNode p = (ObjectNode) f.get("properties");
        p.putNull("aes");
        p.put("wmo", 71807);
        assertThat(CwfisMappers.station(f).record().externalId()).isEqualTo("wmo:71807");
        p.putNull("wmo");
        assertThat(CwfisMappers.station(f).reason()).isEqualTo(MappedRecord.MISSING_ID);
    }

    @Test
    @DisplayName("hotspots: id rep_date:lat:lon:satellite at five decimals, the FBP fields, TTL from the report time")
    void hotspots() {
        FireFetch.Batch b = batch("cwfis-hotspots", CwfisMappers::hotspot, l -> l);
        assertThat(b.records()).hasSize(25);
        assertThat(b.skipped()).isEmpty();
        NormalizedRecord first = b.records().getFirst();
        assertThat(first.externalId()).isEqualTo("2026-10-12T21:24:00Z:36.72795:-119.78029:NOAA-21");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-12T21:24:00Z"));
        assertThat(first.payload()).containsEntry("satellite", "NOAA-21").containsEntry("sensor", "VIIRS-I")
                .containsEntry("fuel", "non_fuel").containsEntry("frp", 2.97).containsEntry("agency", "CA")
                .containsEntry("hfi", 0.0).doesNotContainKeys("ffmc", "temp", "pconif", "source", "lat", "lon");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor("cwfis-hotspots"), b.records());
    }

    @Test
    @DisplayName("snapshot contract for each layer: two parses keep one static hash")
    void snapshots() {
        JsonNode perimeter = features("cwfis-perimeters", "page.json").getFirst();
        SnapshotContract.assertStableStaticHash(descriptor("cwfis-perimeters"),
                List.of(CwfisMappers.perimeter(perimeter).record(), CwfisMappers.perimeter(perimeter).record()));
        JsonNode station = features("cwfis-fwi-stations", "page.json").get(3);
        SnapshotContract.assertStableStaticHash(descriptor("cwfis-fwi-stations"),
                List.of(CwfisMappers.station(station).record(), CwfisMappers.station(station).record()));
        JsonNode hotspot = features("cwfis-hotspots", "page.json").getFirst();
        SnapshotContract.assertStableStaticHash(descriptor("cwfis-hotspots"),
                List.of(CwfisMappers.hotspot(hotspot).record(), CwfisMappers.hotspot(hotspot).record()));
    }

    @Test
    @DisplayName("fixed-decimal formatting never prints a negative zero")
    void fixed() {
        assertThat(CwfisMappers.fixed(-0.001, 2)).isEqualTo("0.00");
        assertThat(CwfisMappers.fixed(-98.5843, 2)).isEqualTo("-98.58");
        assertThat(CwfisMappers.fixed(36.727951, 5)).isEqualTo("36.72795");
    }
}
