package ai.foresignal.source.adapters.stac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/** {@link StacItemParser} against trimmed Earth Search captures (prompt 096). */
class StacItemParserTest {

    private static final Instant FROM = Instant.parse("2026-09-01T00:00:00Z");
    private static final Instant UNTIL = Instant.parse("2026-11-01T00:00:00Z");
    private static final Supplier<ai.foresignal.source.spi.PlacePoints> NO_PLACES = () -> null;

    private static SourceDescriptor descriptor(String id) {
        return new EarthSearchAdapter(StacSources.reviewed(id), "", NO_PLACES).descriptor();
    }

    private static StacItemParser.Options options(String collection, boolean cloud,
            Double maxCloud, Geometry aoi) {
        return new StacItemParser.Options(collection, cloud, maxCloud, FROM, UNTIL, aoi);
    }

    private static Geometry box(double west, double south, double east, double north) {
        Polygon polygon = (Polygon) Geo.factory().toGeometry(
                new org.locationtech.jts.geom.Envelope(west, east, south, north));
        polygon.setSRID(Geo.SRID);
        return polygon;
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String suffix) {
        return records.stream().filter(r -> r.externalId().endsWith(suffix)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Sentinel-2 Collection 1: fields, https thumbnail kept, asset keys sorted")
    void sentinel2() {
        byte[] body = AdapterFixtures.bytes("stac-sentinel2", "search.json");
        StacItemParser.Result result = StacItemParser.parse(body,
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-97.7, 30.13, -97.5, 30.33)));
        assertThat(result.rows()).isEqualTo(2);
        assertThat(result.records()).hasSize(2);
        NormalizedRecord scene = byId(result.records(), "S2C_T14RPU_20261010T171726_L2A");
        assertThat(scene.externalId()).isEqualTo("sentinel-2-c1-l2a:S2C_T14RPU_20261010T171726_L2A");
        assertThat(scene.observedAt()).isEqualTo(Instant.parse("2026-10-10T21:52:36.764Z"));
        assertThat(scene.eventTime()).isEqualTo(Instant.parse("2026-10-10T17:25:25.181000Z"));
        assertThat(scene.payload().get("platform")).isEqualTo("sentinel-2c");
        assertThat(scene.payload().get("constellation")).isEqualTo("sentinel-2");
        assertThat(scene.payload().get("cloudCover")).isEqualTo(0.0);
        assertThat(scene.payload().get("gridCode")).isEqualTo("MGRS-14RPU");
        assertThat(scene.payload().get("mgrsTile")).isNull();
        assertThat(scene.payload().get("stacItemUrl"))
                .isEqualTo("https://earth-search.aws.element84.com/v1/collections/sentinel-2-c1-l2a/items/"
                        + "S2C_T14RPU_20261010T171726_L2A");
        assertThat((String) scene.payload().get("thumbnailUrl")).startsWith("https://");
        assertThat(scene.payload().get("assetKeys")).isEqualTo(List.of("blue", "red", "thumbnail", "visual"));
        assertThat(scene.payload().get("aoiCoverPct")).isEqualTo(100);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor("stac-sentinel2"), result.records());
    }

    @Test
    @DisplayName("Landsat: WRS path/row, s3:// thumbnail dropped")
    void landsat() {
        byte[] body = AdapterFixtures.bytes("stac-landsat", "search.json");
        StacItemParser.Result result = StacItemParser.parse(body,
                options(StacSources.LANDSAT_COLLECTION, true, 30.0, box(-99.1, 29.2, -96.6, 31.4)));
        assertThat(result.records()).hasSize(2);
        NormalizedRecord scene = byId(result.records(), "LC09_L2SP_027039_20260928_02_T1");
        assertThat(scene.payload().get("platform")).isEqualTo("landsat-9");
        assertThat(scene.payload().get("wrsPath")).isEqualTo("027");
        assertThat(scene.payload().get("wrsRow")).isEqualTo("039");
        assertThat(scene.payload().get("cloudCover")).isEqualTo(3.8);
        assertThat(scene.payload()).doesNotContainKey("thumbnailUrl");
        assertThat(scene.payload().get("assetKeys")).isEqualTo(List.of("blue", "red", "thumbnail"));
        FieldSpecContract.assertPayloadKeysDeclared(descriptor("stac-landsat"), result.records());
    }

    @Test
    @DisplayName("Sentinel-1: no cloud cover, SAR mode and polarizations, s3:// thumbnail dropped")
    void sentinel1() {
        byte[] body = AdapterFixtures.bytes("stac-sentinel1", "search.json");
        StacItemParser.Result result = StacItemParser.parse(body,
                options(StacSources.SENTINEL1_COLLECTION, false, null, box(-61.3, 3.6, -58.7, 5.6)));
        assertThat(result.records()).hasSize(1);
        NormalizedRecord scene = result.records().getFirst();
        assertThat(scene.payload()).doesNotContainKey("cloudCover");
        assertThat(scene.payload().get("sarMode")).isEqualTo("IW");
        assertThat(scene.payload().get("polarizations")).isEqualTo(List.of("VV", "VH"));
        assertThat(scene.payload()).doesNotContainKey("thumbnailUrl");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor("stac-sentinel1"), result.records());
    }

    @Test
    @DisplayName("aoiCoverPct: full overlap is 100, a straddling box is partial, a far box is 0")
    void aoiCoverage() {
        byte[] body = AdapterFixtures.bytes("stac-sentinel2", "search.json");
        Geometry footprint = Geo.factory().toGeometry(new org.locationtech.jts.geom.Envelope(
                -97.966, -97.145, 29.734, 30.729));
        assertThat(Footprints.aoiCoverPct(footprint, box(-97.70, 30.13, -97.50, 30.33))).isEqualTo(100);
        assertThat(Footprints.aoiCoverPct(footprint, box(-98.4, 30.1, -97.6, 30.4))).isBetween(1, 99);
        assertThat(Footprints.aoiCoverPct(footprint, box(10, 10, 11, 11))).isZero();
        // The parsed record carries the integer coverage.
        StacItemParser.Result result = StacItemParser.parse(body,
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-97.70, 30.13, -97.50, 30.33)));
        assertThat(byId(result.records(), "S2C_T14RPU_20261010T171726_L2A").payload().get("aoiCoverPct"))
                .isEqualTo(100);
    }

    @Test
    @DisplayName("a footprint that crosses the antimeridian becomes a two-part MultiPolygon")
    void antimeridian() {
        String json = item("AM_1", "2026-10-05T00:00:00Z", "2026-10-05T01:00:00Z", 5.0,
                "[[175.0,10.0],[-175.0,10.0],[-175.0,20.0],[175.0,20.0],[175.0,10.0]]");
        StacItemParser.Result result = StacItemParser.parse(json.getBytes(StandardCharsets.UTF_8),
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(160, 0, 180, 30)));
        Geometry geometry = result.records().getFirst().geometry();
        assertThat(geometry).isInstanceOf(MultiPolygon.class);
        assertThat(geometry.getNumGeometries()).isEqualTo(2);
        assertThat(geometry.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("a footprint over the vertex limit is simplified below 10,000 vertices")
    void simplification() {
        StringBuilder ring = new StringBuilder("[");
        int points = 12_000;
        for (int i = 0; i <= points; i++) {
            double angle = 2 * Math.PI * i / points;
            double lon = -97.6 + 0.5 * Math.cos(angle);
            double lat = 30.2 + 0.5 * Math.sin(angle);
            if (i > 0) {
                ring.append(',');
            }
            ring.append('[').append(lon).append(',').append(lat).append(']');
        }
        ring.append(']');
        String json = item("BIG_1", "2026-10-05T00:00:00Z", "2026-10-05T01:00:00Z", 5.0, ring.toString());
        StacItemParser.Result result = StacItemParser.parse(json.getBytes(StandardCharsets.UTF_8),
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-98, 30, -97, 31)));
        Geometry geometry = result.records().getFirst().geometry();
        assertThat(geometry.getNumPoints()).isLessThanOrEqualTo(Footprints.MAX_VERTICES);
    }

    @Test
    @DisplayName("a response that violates the requested cloud bound is a filter-ignored format error")
    void filterIgnored() {
        String json = item("CLOUDY", "2026-10-05T00:00:00Z", "2026-10-05T01:00:00Z", 90.0,
                "[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]");
        assertThatThrownBy(() -> StacItemParser.parse(json.getBytes(StandardCharsets.UTF_8),
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-98, 30, -97, 31))))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining(StacItemParser.FILTER_IGNORED);
    }

    @Test
    @DisplayName("an item older than the window ends the page; nothing older is emitted")
    void olderItemsEndPage() {
        String older = item("OLD", "2026-08-01T00:00:00Z", "2026-08-01T01:00:00Z", 5.0,
                "[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]");
        StacItemParser.Result result = StacItemParser.parse(older.getBytes(StandardCharsets.UTF_8),
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-98, 30, -97, 31)));
        assertThat(result.olderSeen()).isTrue();
        assertThat(result.records()).isEmpty();
    }

    @Test
    @DisplayName("a reprocessing that moves only properties.updated hashes equal (a touch)")
    void reprocessedHashesEqual() {
        String first = item("SAME", "2026-10-05T00:00:00Z", "2026-10-05T01:00:00Z", 5.0,
                "[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]");
        String second = item("SAME", "2026-10-05T00:00:00Z", "2026-10-09T09:00:00Z", 5.0,
                "[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]");
        StacItemParser.Options options =
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-98, 30, -97, 31));
        NormalizedRecord a = StacItemParser.parse(first.getBytes(StandardCharsets.UTF_8), options).records()
                .getFirst();
        NormalizedRecord b = StacItemParser.parse(second.getBytes(StandardCharsets.UTF_8), options).records()
                .getFirst();
        assertThat(a.payload()).isEqualTo(b.payload());
        SnapshotContract.assertStableStaticHash(descriptor("stac-sentinel2"), List.of(a, b));
    }

    @Test
    @DisplayName("a body that is not a STAC ItemCollection is a format change")
    void formatChange() {
        assertThatThrownBy(() -> StacItemParser.parse("{\"nope\":[]}".getBytes(StandardCharsets.UTF_8),
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-98, 30, -97, 31))))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a malformed footprint drops that item only, and is counted")
    void malformedFootprintSkipped() {
        String good = item("GOOD", "2026-10-10T00:00:00Z", "2026-10-10T01:00:00Z", 5.0,
                "[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]");
        String bad = item("BAD", "2026-10-09T00:00:00Z", "2026-10-09T01:00:00Z", 5.0, "\"not coordinates\"");
        String page = good.replace("],\"links\":[]}", ","
                + bad.substring(bad.indexOf("\"features\":[") + "\"features\":[".length(), bad.lastIndexOf("],"))
                + "],\"links\":[]}");

        StacItemParser.Result result = StacItemParser.parse(page.getBytes(StandardCharsets.UTF_8),
                options(StacSources.SENTINEL2_COLLECTION, true, 30.0, box(-98, 30, -97, 31)));

        assertThat(result.rows()).isEqualTo(2);
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("sentinel-2-c1-l2a:GOOD");
        assertThat(result.malformed()).isEqualTo(1);
    }

    @Test
    @DisplayName("observedAt is read from the collection's fixed property, never chosen per item")
    void observedAtFixedPerCollection() {
        String onlyCreated = item("C1", "2026-10-10T00:00:00Z", "2026-10-10T01:00:00Z", 5.0,
                "[[-97.8,30.1],[-97.6,30.1],[-97.6,30.3],[-97.8,30.3],[-97.8,30.1]]")
                .replace("\"updated\":\"2026-10-10T01:00:00Z\",", "");
        Geometry aoi = box(-98, 30, -97, 31);

        StacItemParser.Result byUpdated = StacItemParser.parse(onlyCreated.getBytes(StandardCharsets.UTF_8),
                new StacItemParser.Options(StacSources.SENTINEL2_COLLECTION, true, 30.0, FROM, UNTIL, aoi,
                        StacSource.UPDATED));
        assertThat(byUpdated.records()).isEmpty();
        assertThat(byUpdated.skipped()).isEqualTo(1);

        StacItemParser.Result byCreated = StacItemParser.parse(onlyCreated.getBytes(StandardCharsets.UTF_8),
                new StacItemParser.Options(StacSources.SENTINEL2_COLLECTION, true, 30.0, FROM, UNTIL, aoi,
                        StacSource.CREATED));
        assertThat(byCreated.records()).hasSize(1);
        assertThat(byCreated.records().get(0).observedAt()).isEqualTo(Instant.parse("2026-10-10T01:00:00Z"));
        assertThat(StacSources.REVIEWED).allSatisfy(s -> assertThat(s.observedAtField()).isEqualTo(StacSource.UPDATED));
    }

    /** A minimal one-feature ItemCollection with a self link and an https thumbnail. */
    private static String item(String id, String datetime, String updated, double cloud, String ring) {
        return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"id\":\"" + id + "\","
                + "\"collection\":\"sentinel-2-c1-l2a\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":["
                + ring + "]},\"properties\":{\"datetime\":\"" + datetime + "\",\"updated\":\"" + updated
                + "\",\"created\":\"" + updated + "\",\"eo:cloud_cover\":" + cloud
                + ",\"platform\":\"sentinel-2c\",\"constellation\":\"sentinel-2\",\"instruments\":[\"msi\"],"
                + "\"grid:code\":\"MGRS-14RPU\"},\"assets\":{\"thumbnail\":{\"href\":\"https://example.org/t.jpg\"},"
                + "\"red\":{\"href\":\"https://example.org/red.tif\"}},"
                + "\"links\":[{\"rel\":\"self\",\"href\":\"https://example.org/items/" + id + "\"}]}],"
                + "\"links\":[]}";
    }
}
