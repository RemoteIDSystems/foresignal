package ai.foresignal.source.adapters.cctv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

/**
 * Every pack parser against its real fixture (captured 2026-10-13) and the hand-made edge cases (prompt 072):
 * expected counts, spot-checked fields, skips counted by reason, off-host frame URLs dropped with the camera kept,
 * no synthetic heading, and {@code http:} frames flagged.
 */
class CctvPackParsersTest {

    /** The parsed and mapped result of one fixture. */
    record Parsed(List<NormalizedRecord> records, PackContext pc, int outsideBbox) {

        NormalizedRecord byId(String id) {
            return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
        }

        long count(Predicate<NormalizedRecord> p) {
            return records.stream().filter(p).count();
        }
    }

    static Parsed parse(String packId, String fixtureDir, String file, byte[]... extra) {
        CctvPack pack = CctvPacks.reviewed(packId);
        PackContext pc = new PackContext(pack, List.of(extra));
        List<CameraEntry> entries = pack.parser().parse(AdapterFixtures.bytes(fixtureDir, file), pc);
        CameraRecordMapper.Mapped mapped = CameraRecordMapper.mapAll(pack, entries, null, pc);
        return new Parsed(mapped.records(), pc, mapped.outsideBbox());
    }

    static boolean has(NormalizedRecord r, String key) {
        return r.payload().containsKey(key);
    }

    static boolean offline(NormalizedRecord r) {
        return CameraRecordMapper.OFFLINE.equals(r.payload().get("status"));
    }

    @Test
    @DisplayName("TfL: 24 cameras, 2 offline, a heading only for the one wholly compass view, no video")
    void tfl() {
        Parsed p = parse("cctv-tfl", "cctv-tfl", "jamcam.json");
        assertThat(p.records()).hasSize(24);
        assertThat(p.count(CctvPackParsersTest::offline)).isEqualTo(2);
        assertThat(p.count(r -> has(r, "imageUrl"))).isEqualTo(24);
        assertThat(p.count(r -> has(r, "videoUrl"))).as("MP4 clips are not streams").isZero();
        assertThat(p.count(r -> has(r, "headingDeg"))).isEqualTo(1);
        NormalizedRecord first = p.byId("JamCams_00002.00865");
        assertThat(first.payload()).containsEntry("name", "A406 Billet Upass E").containsEntry("direction", "West")
                .containsEntry("headingDeg", 270.0).containsEntry("provider", "Transport for London")
                .containsEntry("status", "active").containsEntry("frameAccess", "direct")
                .containsEntry("imageUrl", "https://s3-eu-west-1.amazonaws.com/jamcams.tfl.gov.uk/00002.00865.jpg");
        assertThat(((Point) first.geometry()).getY()).isEqualTo(51.60067);
        assertThat(p.byId("JamCams_00001.04546").payload()).containsEntry("status", "offline");
        assertThat(p.pc().skippedTotal()).isZero();
    }

    @Test
    @DisplayName("TfL edge cases: no id, no position and a duplicate skipped; another bucket's image dropped")
    void tflEdges() {
        Parsed p = parse("cctv-tfl", "cctv-tfl", "edge-cases.json");
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("JamCams_edge.ok", "JamCams_unavailable", "JamCams_other.bucket");
        assertThat(p.pc().skipped()).containsEntry(PackContext.MALFORMED, 1).containsEntry(PackContext.NO_COORDINATES, 1)
                .containsEntry(PackContext.DUPLICATE, 1);
        assertThat(p.byId("JamCams_edge.ok").payload()).containsEntry("headingDeg", 315.0)
                .containsEntry("name", "Edge Ok");
        assertThat(p.byId("JamCams_unavailable").payload()).containsEntry("status", "offline")
                .doesNotContainKeys("direction", "headingDeg");
        assertThat(p.byId("JamCams_other.bucket").payload()).containsEntry("status", "active")
                .doesNotContainKeys("imageUrl", "frameAccess");
    }

    @Test
    @DisplayName("Caltrans D4: 25 cameras, 3 offline, still and HLS on the official hosts, refresh in seconds, no heading")
    void caltrans() {
        Parsed p = parse("cctv-caltrans-d4", "cctv-caltrans-d4", "cctvStatusD04.json");
        assertThat(p.records()).hasSize(25);
        assertThat(p.count(CctvPackParsersTest::offline)).isEqualTo(3);
        assertThat(p.count(r -> has(r, "imageUrl"))).isEqualTo(25);
        assertThat(p.count(r -> has(r, "videoUrl"))).isEqualTo(22);
        assertThat(p.count(r -> has(r, "headingDeg"))).as("direction of travel is not a facing").isZero();
        assertThat(p.byId("TV102").payload()).containsEntry("name", "I-580 : West of SR-24")
                .containsEntry("region", "Oakland").containsEntry("road", "I-580").containsEntry("direction", "West")
                .containsEntry("refreshSeconds", 300).containsEntry("provider", "Caltrans")
                .containsEntry("videoUrl", "https://wzmedia.dot.ca.gov/D4/W580_JWO_24_IC.stream/playlist.m3u8");
        assertThat(p.byId("TV965").payload()).containsEntry("status", "offline");
    }

    @Test
    @DisplayName("Caltrans edge cases: skips counted, off-host URLs dropped, :443 canonicalized, id from the image")
    void caltransEdges() {
        Parsed p = parse("cctv-caltrans-d4", "cctv-caltrans-d4", "edge-cases.json");
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("TV900", "TV903", "TV904", "i880atbroadway");
        assertThat(p.pc().skipped()).containsEntry(PackContext.NO_COORDINATES, 1)
                .containsEntry(PackContext.OUT_OF_BOUNDS, 1).containsEntry(PackContext.DUPLICATE, 1)
                .containsEntry(PackContext.MALFORMED, 1);
        assertThat(p.byId("TV900").payload())
                .containsEntry("videoUrl", "https://wzmedia.dot.ca.gov/D4/E80_edge.stream/playlist.m3u8")
                .containsEntry("name", "I-80 : Edge Ok");
        assertThat(p.byId("TV903").payload()).containsEntry("status", "active")
                .doesNotContainKeys("imageUrl", "videoUrl", "frameAccess", "refreshSeconds");
        assertThat(p.byId("TV904").payload()).containsEntry("status", "offline").containsEntry("refreshSeconds", 3600);
        assertThat(p.byId("i880atbroadway").payload()).containsEntry("name", "No camera code here")
                .doesNotContainKey("direction");
    }

    @Test
    @DisplayName("Ontario 511 (synthetic): traveliq views rebuilt on 511on.ca, a 'down' view passed over, headings")
    void ontario() {
        Parsed p = parse("cctv-ontario511", "cctv-ontario511", "cameras.json");
        assertThat(p.records()).extracting(NormalizedRecord::externalId).containsExactly("455", "456", "501", "502",
                "503");
        assertThat(p.pc().skipped()).containsEntry(PackContext.MALFORMED, 1).containsEntry(PackContext.OUT_OF_BOUNDS, 1);
        assertThat(p.byId("455").payload()).containsEntry("imageUrl", "https://511on.ca/map/Cctv/815")
                .containsEntry("road", "Highway 407").doesNotContainKeys("direction", "headingDeg");
        assertThat(p.byId("501").payload()).containsEntry("imageUrl", "https://511on.ca/map/Cctv/901")
                .containsEntry("direction", "Eastbound").containsEntry("headingDeg", 90.0);
        assertThat(p.byId("502").payload()).containsEntry("status", "offline")
                .containsEntry("direction", "Both Directions").doesNotContainKeys("headingDeg", "imageUrl");
        assertThat(p.byId("503").payload()).containsEntry("status", "active").doesNotContainKey("imageUrl");
    }

    @Test
    @DisplayName("Digitraffic: one record per preset of a GATHERING station, stationId:presetId, image built from the id")
    void digitraffic() {
        Parsed p = parse("cctv-digitraffic", "cctv-digitraffic", "stations.json");
        assertThat(p.records()).hasSize(20);
        assertThat(p.count(CctvPackParsersTest::offline)).isEqualTo(2);
        assertThat(p.pc().skipped()).containsEntry(PackContext.NOT_LISTED, 1);
        assertThat(p.byId("C01503:C0150301").payload()).containsEntry("name", "kt51 Inkoo (view 01)")
                .containsEntry("imageUrl", "https://weathercam.digitraffic.fi/C0150301.jpg")
                .containsEntry("provider", "Fintraffic").doesNotContainKey("headingDeg");
        assertThat(p.byId("C01632:C0163201").payload()).containsEntry("status", "offline");
        assertThat(p.records()).noneMatch(r -> r.externalId().startsWith("C01539:"));
    }

    @Test
    @DisplayName("DriveBC: 25 cameras, orientation headings, TransLink credit as provider, solar note dropped")
    void driveBc() {
        Parsed p = parse("cctv-drivebc", "cctv-drivebc", "webcams.json");
        assertThat(p.records()).hasSize(25);
        assertThat(p.count(CctvPackParsersTest::offline)).isEqualTo(2);
        assertThat(p.count(r -> has(r, "headingDeg"))).isEqualTo(25);
        assertThat(p.byId("1006").payload()).containsEntry("name", "Lynn Valley Road - W")
                .containsEntry("direction", "W").containsEntry("headingDeg", 270.0)
                .containsEntry("region", "Lower Mainland").containsEntry("road", "1")
                .containsEntry("imageUrl", "https://www.drivebc.ca/images/1006.jpg").containsEntry("provider", "DriveBC");
        assertThat(p.byId("827").payload()).containsEntry("provider", "Images courtesy of TransLink");
        assertThat(p.count(r -> "DriveBC".equals(r.payload().get("provider")))).isEqualTo(24);
        assertThat(p.byId("986").payload()).containsEntry("status", "offline");
    }

    @Test
    @DisplayName("NSW: 25 cameras, the data.livetraffic.com frame dropped, compass headings from N-E style text")
    void nsw() {
        Parsed p = parse("cctv-nsw", "cctv-nsw", "traffic-cam.json");
        assertThat(p.records()).hasSize(25);
        assertThat(p.count(r -> has(r, "imageUrl"))).isEqualTo(24);
        assertThat(p.count(r -> has(r, "headingDeg"))).isEqualTo(25);
        assertThat(p.byId("023651ee-389c-4677-978e-d39b6c24c1e7").payload()).containsEntry("name", "5 Ways (Miranda)")
                .containsEntry("region", "SYD SOUTH").containsEntry("direction", "W").containsEntry("headingDeg", 270.0)
                .containsEntry("frameAccess", "browser");
        assertThat(p.byId("deed2090-7deb-429d-8cf7-0b247c17f653").payload()).containsEntry("status", "active")
                .doesNotContainKeys("imageUrl", "frameAccess");
    }

    @Test
    @DisplayName("NSW edge cases: no geometry and string coordinates skipped, out of bounds, off-host and http dropped")
    void nswEdges() {
        Parsed p = parse("cctv-nsw", "cctv-nsw", "edge-cases.geojson");
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("edge-ok", "off-host", "insecure");
        assertThat(p.pc().skipped()).containsEntry(PackContext.NO_COORDINATES, 2)
                .containsEntry(PackContext.OUT_OF_BOUNDS, 1).containsEntry(PackContext.DUPLICATE, 1)
                .containsEntry(PackContext.MALFORMED, 1);
        assertThat(p.byId("edge-ok").payload()).containsEntry("direction", "N-E").containsEntry("headingDeg", 45.0);
        assertThat(p.byId("insecure").payload()).as("http is not an allowed origin for NSW")
                .doesNotContainKeys("imageUrl", "frameInsecure", "headingDeg").containsEntry("direction", "Both");
    }

    @Test
    @DisplayName("Calgary: 25 cameras, http frames upgraded to https, id from the frame file, quadrant as region")
    void calgary() {
        Parsed p = parse("cctv-calgary", "cctv-calgary", "k7p9-kppz.json");
        assertThat(p.records()).hasSize(25);
        assertThat(p.byId("loc74").payload()).containsEntry("name", "16 Avenue / 10 Street NW")
                .containsEntry("region", "NW").containsEntry("imageUrl", "https://trafficcam.calgary.ca/loc74.jpg")
                .doesNotContainKeys("frameInsecure", "headingDeg", "direction");
        assertThat(p.records()).allSatisfy(r -> assertThat(r.payload()).doesNotContainKey("frameInsecure"));
    }

    @Test
    @DisplayName("Austin: only TURNED_ON listed, image built from the camera id")
    void austin() {
        Parsed p = parse("cctv-austin", "cctv-austin", "b4k4-adkb.json");
        assertThat(p.records()).hasSize(21);
        assertThat(p.pc().skipped()).containsEntry(PackContext.NOT_LISTED, 3);
        assertThat(p.byId("674").payload()).containsEntry("name", "CESAR CHAVEZ ST / 35 SVRD")
                .containsEntry("road", "CESAR CHAVEZ ST")
                .containsEntry("imageUrl", "https://cctv.austinmobility.io/image/674.jpg");
    }

    @Test
    @DisplayName("DelDOT: HLS only, canonical without :443, county region, 2 offline")
    void delDot() {
        Parsed p = parse("cctv-deldot", "cctv-deldot", "videocamera.json");
        assertThat(p.records()).hasSize(25);
        assertThat(p.count(CctvPackParsersTest::offline)).isEqualTo(2);
        assertThat(p.count(r -> has(r, "imageUrl"))).isZero();
        assertThat(p.byId("KCAM001").payload())
                .containsEntry("videoUrl", "https://video.deldot.gov/live/KCAM001.stream/playlist.m3u8")
                .containsEntry("region", "Kent County").containsEntry("frameAccess", "direct")
                .containsEntry("name", "DE 1 @ MILFORD NECK ROAD (NORTH OFF)");
        assertThat(p.byId("NCAM267").payload()).containsEntry("status", "offline");
    }

    @Test
    @DisplayName("Vegvesen: faults offline, HLS only on the agency's manifest path, place → towards as the name")
    void vegvesen() {
        Parsed p = parse("cctv-vegvesen", "cctv-vegvesen", "items.json");
        assertThat(p.records()).hasSize(25);
        assertThat(p.count(CctvPackParsersTest::offline)).isEqualTo(9);
        assertThat(p.count(r -> has(r, "videoUrl"))).isEqualTo(3);
        assertThat(p.pc().nextPage()).isNull();
        assertThat(p.byId("3000063_1").payload()).containsEntry("name", "Langesi → Svelgen")
                .containsEntry("road", "F614")
                .containsEntry("imageUrl", "https://kamera.atlas.vegvesen.no/api/images/3000063_1")
                .doesNotContainKeys("videoUrl", "direction", "headingDeg");
        assertThat(p.byId("3000061_1").payload())
                .containsEntry("videoUrl", "https://kamera.vegvesen.no/public/3000061_1/manifest.m3u8");
    }

    @Test
    @DisplayName("Estonia: locations joined with images, two without a current image are offline")
    void estonia() {
        Parsed p = parse("cctv-estonia", "cctv-estonia", "roadCameraLocations.xml",
                AdapterFixtures.bytes("cctv-estonia", "roadCameraImages.xml"));
        assertThat(p.records()).hasSize(25);
        assertThat(p.count(CctvPackParsersTest::offline)).isEqualTo(2);
        assertThat(p.byId("a84734fc-8f1d-47af-ba22-8323cd6eb367").payload()).containsEntry("name", "Kaimi")
                .containsEntry("imageUrl", "https://tarktee.transpordiamet.ee/images/987/987_202610132006.jpg")
                .containsEntry("provider", "Transpordiamet (Tarktee)");
    }

    @Test
    @DisplayName("Estonia edge cases: no coordinates, out of bounds, duplicate; off-host image dropped")
    void estoniaEdges() {
        Parsed p = parse("cctv-estonia", "cctv-estonia", "edge-locations.xml",
                AdapterFixtures.bytes("cctv-estonia", "edge-images.xml"));
        assertThat(p.records()).extracting(NormalizedRecord::externalId).containsExactly("ok", "off-host", "no-image");
        assertThat(p.pc().skipped()).containsEntry(PackContext.NO_COORDINATES, 1)
                .containsEntry(PackContext.OUT_OF_BOUNDS, 1).containsEntry(PackContext.DUPLICATE, 1);
        assertThat(p.byId("off-host").payload()).containsEntry("status", "active").doesNotContainKey("imageUrl");
        assertThat(p.byId("no-image").payload()).containsEntry("status", "offline");
    }

    @Test
    @DisplayName("Estonia: a DOCTYPE with an external entity is never resolved")
    void estoniaXxe() {
        CctvPack pack = CctvPacks.reviewed("cctv-estonia");
        byte[] images = AdapterFixtures.bytes("cctv-estonia", "edge-images.xml");
        PackContext pc = new PackContext(pack, List.of(images));
        byte[] xxe = AdapterFixtures.bytes("cctv-estonia", "xxe-locations.xml");
        try {
            List<CameraEntry> entries = pack.parser().parse(xxe, pc);
            assertThat(entries).allSatisfy(e -> assertThat(String.valueOf(e.name())).doesNotContain("root:"));
        } catch (UpstreamFormatException expected) {
            assertThat(expected).hasMessageContaining("cctv-estonia");
        }
        assertThatThrownBy(() -> pack.parser().parse("<html/>".getBytes(StandardCharsets.UTF_8), pc))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> pack.parser().parse(AdapterFixtures.bytes("cctv-estonia", "edge-locations.xml"),
                PackContext.of(pack))).isInstanceOf(UpstreamFormatException.class).hasMessageContaining("missing");
    }

    @Test
    @DisplayName("bundled packs: Tallinn and Warendorf read; curated headings only; Warendorf http frame flagged")
    void bundled() {
        for (String id : List.of("cctv-tallinn", "cctv-warendorf")) {
            CctvPack pack = CctvPacks.reviewed(id);
            PackContext pc = PackContext.of(pack);
            byte[] body;
            try (var in = CctvPackParsersTest.class.getResourceAsStream(pack.resource())) {
                body = in.readAllBytes();
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
            List<NormalizedRecord> records = CameraRecordMapper.mapAll(pack, pack.parser().parse(body, pc), null, pc)
                    .records();
            if (id.equals("cctv-tallinn")) {
                assertThat(records).hasSizeGreaterThan(200);
                NormalizedRecord low = records.stream().filter(r -> r.externalId().equals("tln-103")).findFirst()
                        .orElseThrow();
                assertThat(low.payload()).as("low-confidence heading is a prior, not published")
                        .doesNotContainKey("headingDeg").containsEntry("region", "Tallinn");
                NormalizedRecord high = records.stream().filter(r -> r.externalId().equals("tln-104")).findFirst()
                        .orElseThrow();
                assertThat(high.payload()).containsEntry("headingDeg", 227.2)
                        .containsEntry("imageUrl", "https://ristmikud.tallinn.ee/last/cam104.jpg");
            } else {
                assertThat(records).singleElement().satisfies(r -> assertThat(r.payload())
                        .containsEntry("imageUrl", "http://webcam.warendorf.de/image/jpeg.cgi")
                        .containsEntry("frameInsecure", true).containsEntry("headingDeg", 221.0)
                        .containsEntry("provider", "Stadt Warendorf"));
            }
        }
    }

    @Test
    @DisplayName("no parser produces a synthetic heading: headingDeg only where a published direction exists")
    void noSyntheticHeading() {
        Map<String, String[]> fixtures = Map.of(
                "cctv-caltrans-d4", new String[] {"cctv-caltrans-d4", "cctvStatusD04.json"},
                "cctv-digitraffic", new String[] {"cctv-digitraffic", "stations.json"},
                "cctv-calgary", new String[] {"cctv-calgary", "k7p9-kppz.json"},
                "cctv-austin", new String[] {"cctv-austin", "b4k4-adkb.json"},
                "cctv-deldot", new String[] {"cctv-deldot", "videocamera.json"},
                "cctv-vegvesen", new String[] {"cctv-vegvesen", "items.json"});
        fixtures.forEach((pack, f) -> assertThat(parse(pack, f[0], f[1]).records()).as(pack)
                .allSatisfy(r -> assertThat(r.payload()).doesNotContainKey("headingDeg")));
        for (String file : List.of("jamcam.json", "edge-cases.json")) {
            assertThat(parse("cctv-tfl", "cctv-tfl", file).records())
                    .allSatisfy(r -> assertThat(r.payload().containsKey("headingDeg"))
                            .isEqualTo(r.payload().containsKey("direction")));
        }
    }

    @Test
    @DisplayName("the bbox parameter keeps only the cameras inside it")
    void bboxFilter() {
        CctvPack pack = CctvPacks.reviewed("cctv-caltrans-d4");
        PackContext pc = PackContext.of(pack);
        List<CameraEntry> entries = pack.parser().parse(AdapterFixtures.bytes("cctv-caltrans-d4",
                "cctvStatusD04.json"), pc);
        // A box around Oakland: TV102 (37.825, -122.273) is inside.
        CameraRecordMapper.Mapped mapped = CameraRecordMapper.mapAll(pack, entries,
                new Envelope(-122.35, -122.2, 37.75, 37.9), pc);
        assertThat(mapped.records()).extracting(NormalizedRecord::externalId).contains("TV102");
        assertThat(mapped.records().size() + mapped.outsideBbox()).isEqualTo(25);
        assertThat(mapped.outsideBbox()).isPositive();
    }

    @Test
    @DisplayName("bodies of the wrong shape are format errors naming the pack")
    void formatErrors() {
        byte[] html = "<html>maintenance</html>".getBytes(StandardCharsets.UTF_8);
        byte[] object = "{\"x\": 1}".getBytes(StandardCharsets.UTF_8);
        byte[] array = "[]".getBytes(StandardCharsets.UTF_8);
        for (CctvPack pack : CctvPacks.REVIEWED) {
            if (pack.id().equals("cctv-estonia")) {
                continue;
            }
            assertThatThrownBy(() -> pack.parser().parse(html, PackContext.of(pack))).as(pack.id())
                    .isInstanceOf(UpstreamFormatException.class).hasMessageContaining(pack.id());
            boolean arrayPack = List.of("cctv-tfl", "cctv-ontario511", "cctv-drivebc", "cctv-calgary", "cctv-austin",
                    "cctv-tallinn", "cctv-warendorf").contains(pack.id());
            byte[] wrong = arrayPack ? object : array;
            assertThatThrownBy(() -> pack.parser().parse(wrong, PackContext.of(pack))).as(pack.id())
                    .isInstanceOf(UpstreamFormatException.class);
        }
    }
}
