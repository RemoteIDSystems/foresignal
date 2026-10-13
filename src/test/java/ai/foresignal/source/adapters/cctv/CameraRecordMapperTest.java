package ai.foresignal.source.adapters.cctv;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The shared camera mapping (prompt 072): the payload allowlist with nulls omitted, a stable static hash across two
 * parses of the same fixture, the fieldSpecs contract on every fixture, frame-URL origin checks and canonical form,
 * and the compass table.
 */
class CameraRecordMapperTest {

    static final List<String[]> FIXTURES = List.of(
            new String[] {"cctv-tfl", "cctv-tfl", "jamcam.json"},
            new String[] {"cctv-tfl", "cctv-tfl", "edge-cases.json"},
            new String[] {"cctv-caltrans-d4", "cctv-caltrans-d4", "cctvStatusD04.json"},
            new String[] {"cctv-caltrans-d4", "cctv-caltrans-d4", "edge-cases.json"},
            new String[] {"cctv-ontario511", "cctv-ontario511", "cameras.json"},
            new String[] {"cctv-digitraffic", "cctv-digitraffic", "stations.json"},
            new String[] {"cctv-drivebc", "cctv-drivebc", "webcams.json"},
            new String[] {"cctv-nsw", "cctv-nsw", "traffic-cam.json"},
            new String[] {"cctv-nsw", "cctv-nsw", "edge-cases.geojson"},
            new String[] {"cctv-calgary", "cctv-calgary", "k7p9-kppz.json"},
            new String[] {"cctv-austin", "cctv-austin", "b4k4-adkb.json"},
            new String[] {"cctv-deldot", "cctv-deldot", "videocamera.json"},
            new String[] {"cctv-vegvesen", "cctv-vegvesen", "items.json"});

    static List<NormalizedRecord> records(String[] f) {
        return CctvPackParsersTest.parse(f[0], f[1], f[2]).records();
    }

    static CctvPack pack(String id) {
        return CctvPacks.reviewed(id);
    }

    @Test
    @DisplayName("every payload key is in the allowlist and declared in fieldSpecs, with its declared type")
    void allowlistAndFieldSpecs() {
        for (String[] f : FIXTURES) {
            List<NormalizedRecord> records = records(f);
            assertThat(records).as(f[2]).isNotEmpty();
            for (NormalizedRecord r : records) {
                assertThat(CameraRecordMapper.PAYLOAD_KEYS).as(f[2]).containsAll(r.payload().keySet());
                assertThat(r.payload()).as(f[2]).containsKeys("name", "provider", "status");
                assertThat(r.kind()).isEqualTo(CameraRecordMapper.KIND);
                assertThat(r.observedAt()).as("RECEIVED: no upstream time").isNull();
                assertThat(r.eventTime()).isNull();
                assertThat(r.validUntil()).isNull();
                assertThat(r.geometry().getSRID()).isEqualTo(4326);
            }
            FieldSpecContract.assertPayloadKeysDeclared(CctvDescriptors.descriptor(pack(f[0])), records);
        }
        List<NormalizedRecord> estonia = CctvPackParsersTest.parse("cctv-estonia", "cctv-estonia",
                "roadCameraLocations.xml", AdapterFixtures.bytes("cctv-estonia", "roadCameraImages.xml")).records();
        FieldSpecContract.assertPayloadKeysDeclared(CctvDescriptors.descriptor(pack("cctv-estonia")), estonia);
    }

    @Test
    @DisplayName("nulls are omitted: a minimal entry carries only name, provider and status")
    void nullOmission() {
        CctvPack pack = pack("cctv-austin");
        NormalizedRecord r = CameraRecordMapper.map(pack, CameraEntry.builder("1", 30.3, -97.7).name(" Main ")
                .region(" ").build(), PackContext.of(pack));
        assertThat(r.payload()).containsOnlyKeys("name", "provider", "status").containsEntry("name", "Main")
                .containsEntry("provider", "Austin Transportation & Public Works").containsEntry("status", "active");
    }

    @Test
    @DisplayName("two parses of the same fixture hash equally (adapter rule 7)")
    void stableStaticHash() {
        for (String[] f : FIXTURES) {
            List<NormalizedRecord> first = records(f);
            List<NormalizedRecord> second = records(f);
            NormalizedRecord a = first.get(0);
            NormalizedRecord b = second.stream().filter(r -> r.externalId().equals(a.externalId())).findFirst()
                    .orElseThrow();
            SnapshotContract.assertStableStaticHash(CctvDescriptors.descriptor(pack(f[0])), List.of(a, b));
        }
    }

    @Test
    @DisplayName("frame URLs: allowed prefixes only, absolute http(s), no user info; canonical scheme, host and port")
    void frameUrls() {
        List<String> allowed = List.of("https://cwwp2.dot.ca.gov/", "https://s3-eu-west-1.amazonaws.com/jamcams.tfl.gov.uk/",
                "http://webcam.warendorf.de/");
        assertThat(CameraRecordMapper.frameUrl("https://cwwp2.dot.ca.gov/a.jpg", allowed))
                .isEqualTo("https://cwwp2.dot.ca.gov/a.jpg");
        assertThat(CameraRecordMapper.frameUrl(" HTTPS://CWWP2.DOT.CA.GOV:443/a.jpg?t=1#x ", allowed))
                .isEqualTo("https://cwwp2.dot.ca.gov/a.jpg?t=1");
        assertThat(CameraRecordMapper.frameUrl("https://cwwp2.dot.ca.gov:8443/a.jpg", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl("https://cwwp2.dot.ca.gov.evil.example/a.jpg", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl("https://user@cwwp2.dot.ca.gov/a.jpg", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl("/relative.jpg", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl("ftp://cwwp2.dot.ca.gov/a.jpg", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl("javascript:alert(1)", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl("https://s3-eu-west-1.amazonaws.com/other/x.jpg", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl("http://cwwp2.dot.ca.gov/a.jpg", allowed)).as("scheme pinned").isNull();
        assertThat(CameraRecordMapper.frameUrl("http://webcam.warendorf.de:80/image/jpeg.cgi", allowed))
                .isEqualTo("http://webcam.warendorf.de/image/jpeg.cgi");
        assertThat(CameraRecordMapper.frameUrl("not a url", allowed)).isNull();
        assertThat(CameraRecordMapper.frameUrl(null, allowed)).isNull();
    }

    @Test
    @DisplayName("frameAccess comes from the image host, else the video host; absent without a kept URL")
    void frameAccess() {
        CctvPack caltrans = pack("cctv-caltrans-d4");
        NormalizedRecord videoOnly = CameraRecordMapper.map(caltrans, CameraEntry.builder("v", 37.8, -122.3)
                .videoUrl("https://wzmedia.dot.ca.gov/D4/x.stream/playlist.m3u8").build(), PackContext.of(caltrans));
        assertThat(videoOnly.payload()).containsEntry("frameAccess", "direct").doesNotContainKey("imageUrl");
        CctvPack nsw = pack("cctv-nsw");
        NormalizedRecord browser = CameraRecordMapper.map(nsw, CameraEntry.builder("b", -33.8, 151.2)
                .imageUrl("https://webcams.transport.nsw.gov.au/x.jpeg").build(), PackContext.of(nsw));
        assertThat(browser.payload()).containsEntry("frameAccess", "browser").doesNotContainKey("frameInsecure");
        NormalizedRecord none = CameraRecordMapper.map(nsw, CameraEntry.builder("n", -33.8, 151.2).build(),
                PackContext.of(nsw));
        assertThat(none.payload()).doesNotContainKeys("frameAccess", "frameInsecure");
    }

    @Test
    @DisplayName("headings are normalized to [0, 360); non-finite headings are dropped; null island is out of bounds")
    void headingsAndBounds() {
        CctvPack pack = pack("cctv-tallinn");
        PackContext pc = PackContext.of(pack);
        assertThat(CameraRecordMapper.map(pack, CameraEntry.builder("a", 59.4, 24.7).headingDeg(-90.0).build(), pc)
                .payload()).containsEntry("headingDeg", 270.0);
        assertThat(CameraRecordMapper.map(pack, CameraEntry.builder("b", 59.4, 24.7).headingDeg(Double.NaN).build(), pc)
                .payload()).doesNotContainKey("headingDeg");
        CctvPack.Bounds world = new CctvPack.Bounds(-90, -180, 90, 180);
        assertThat(world.contains(0, 0)).isFalse();
        assertThat(world.contains(91, 0)).isFalse();
        assertThat(world.contains(Double.NaN, 0)).isFalse();
        assertThat(world.contains(1, 1)).isTrue();
        assertThat(CameraRecordMapper.map(pack, CameraEntry.builder(" ", 59.4, 24.7).build(), pc)).isNull();
        assertThat(pc.skipped(PackContext.MALFORMED)).isEqualTo(1);
    }

    @Test
    @DisplayName("the compass table: whole-field directions only, never free text")
    void compass() {
        assertThat(Compass.heading("N")).isEqualTo(0.0);
        assertThat(Compass.heading("n-e")).isEqualTo(45.0);
        assertThat(Compass.heading(" West ")).isEqualTo(270.0);
        assertThat(Compass.heading("Southbound")).isEqualTo(180.0);
        assertThat(Compass.heading("WB")).isEqualTo(270.0);
        assertThat(Compass.heading("North-West")).isEqualTo(315.0);
        assertThat(Compass.heading("SSW")).isEqualTo(202.5);
        assertThat(Compass.heading("Zoom west - Deptford Bridge")).isNull();
        assertThat(Compass.heading("Both Directions")).isNull();
        assertThat(Compass.heading("Median")).isNull();
        assertThat(Compass.heading("")).isNull();
        assertThat(Compass.heading(null)).isNull();
    }

    @Test
    @DisplayName("duplicates keep the first entry and are counted")
    void duplicates() {
        CctvPack pack = pack("cctv-austin");
        PackContext pc = PackContext.of(pack);
        List<CameraEntry> entries = new ArrayList<>();
        entries.add(CameraEntry.builder("1", 30.3, -97.7).name("first").build());
        entries.add(CameraEntry.builder("1", 30.31, -97.71).name("second").build());
        CameraRecordMapper.Mapped mapped = CameraRecordMapper.mapAll(pack, entries, null, pc);
        assertThat(mapped.records()).singleElement().satisfies(r -> assertThat(r.payload()).containsEntry("name",
                "first"));
        assertThat(pc.skipped(PackContext.DUPLICATE)).isEqualTo(1);
    }
}
