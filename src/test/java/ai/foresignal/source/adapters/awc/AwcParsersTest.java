package ai.foresignal.source.adapters.awc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import tools.jackson.databind.JsonNode;

/**
 * The AWC product parsers on live captures of 2026-10-14 (and a CWA fixture built from AWC's OpenAPI, since none was
 * in force): every GeoJSON hazard product, PIREP identity stability, TAF validity from the API and the cache file
 * (prompt 087).
 */
class AwcParsersTest {

    private static List<JsonNode> features(String fixture) {
        return AwcSupport.features(AdapterFixtures.bytes("awc", fixture), fixture);
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("airsigmet: convective SIGMETs with validity, tops, movement and raw text")
    void airSigmets() {
        AwcHazardParser.Parsed parsed = AwcHazardParser.airSigmets(features("airsigmet.geojson"));

        assertThat(parsed.records()).hasSize(7);
        NormalizedRecord r = byId(parsed.records(), "KKCI:5E:2026-10-14T03:55:00Z");
        assertThat(r.kind()).isEqualTo(AwcHazardParser.KIND);
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-14T03:55:00Z"));
        assertThat(r.validFrom()).isEqualTo(Instant.parse("2026-10-14T03:55:00Z"));
        assertThat(r.validUntil()).isEqualTo(Instant.parse("2026-10-14T05:55:00Z"));
        assertThat(r.geometry()).isInstanceOf(Polygon.class);
        assertThat(r.payload()).containsEntry("product", "SIGMET").containsEntry("hazard", "CONVECTIVE")
                .containsEntry("severity", "5").containsEntry("topFt", 38000L).containsEntry("movementDir", 280)
                .containsEntry("movementKt", 40L).doesNotContainKey("baseFt");
        assertThat((String) r.payload().get("raw")).startsWith("WSUS31 KKCI 140355").doesNotEndWith("\n");
    }

    @Test
    @DisplayName("isigmet: FIR identity, compass movement, base and top, qualifier")
    void isigmets() {
        AwcHazardParser.Parsed parsed = AwcHazardParser.isigmets(features("isigmet.geojson"));

        assertThat(parsed.records()).hasSize(21);
        assertThat(parsed.noGeometry()).as("three rings AWC left open are closed").isZero();
        NormalizedRecord r = byId(parsed.records(), "MHTG:A4:2026-10-13T22:50:00Z");
        assertThat(r.payload()).containsEntry("product", "ISIGMET").containsEntry("hazard", "VA")
                .containsEntry("qualifier", "FUEGO").containsEntry("baseFt", 0L).containsEntry("topFt", 14000L)
                .containsEntry("movementDir", 270).containsEntry("movementKt", 10L);
        assertThat(r.validUntil()).isEqualTo(Instant.parse("2026-10-14T04:50:00Z"));
    }

    @Test
    @DisplayName("gairmet: snapshot identity with the forecast hour, levels in feet, freezing-level pieces merged")
    void gairmets() {
        AwcHazardParser.Parsed parsed = AwcHazardParser.gairmets(features("gairmet.geojson"));

        // 27 features: 7 freezing-level pieces of one tag at five levels become five records.
        assertThat(parsed.records()).hasSize(25);
        NormalizedRecord ifr = byId(parsed.records(), "SIERRA:9E:IFR:2026-10-14T06:00:00Z:3");
        assertThat(ifr.observedAt()).isEqualTo(Instant.parse("2026-10-14T02:45:00Z"));
        assertThat(ifr.validFrom()).as("the snapshot time; a forecast > 0 starts later than issue")
                .isEqualTo(Instant.parse("2026-10-14T06:00:00Z"));
        assertThat(ifr.validUntil()).as("no asserted end: the TTL ends it").isNull();
        assertThat(ifr.payload()).containsEntry("product", "GAIRMET").containsEntry("qualifier",
                "CIG BLW 010 VIS BLW 3SM PCPN BR");
        NormalizedRecord ice = byId(parsed.records(), "ZULU:3C:ICE:2026-10-14T06:00:00Z:3");
        assertThat(ice.payload()).containsEntry("topFt", 20000L).containsEntry("severity", "MOD")
                .doesNotContainKey("baseFt");
        NormalizedRecord fzl = byId(parsed.records(), "ZULU:1C:FZLVL:2026-10-14T06:00:00Z:3:160");
        assertThat(fzl.geometry()).isInstanceOf(MultiLineString.class);
        assertThat(fzl.geometry().getNumGeometries()).isEqualTo(3);
        assertThat(fzl.payload()).containsEntry("baseFt", 16000L);
        assertThat(byId(parsed.records(), "ZULU:1C:FZLVL:2026-10-14T06:00:00Z:3:SFC").payload())
                .containsEntry("baseFt", 0L);
    }

    @Test
    @DisplayName("cwa: epoch-second and text validity, CWSU identity, raw text")
    void cwas() {
        AwcHazardParser.Parsed parsed = AwcHazardParser.cwas(features("cwa.geojson"));

        assertThat(parsed.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("ZFW:101:2026-10-14T00:00:00Z", "ZHU:201:2026-10-14T00:30:00Z");
        NormalizedRecord zfw = parsed.records().get(0);
        assertThat(zfw.validUntil()).isEqualTo(Instant.parse("2026-10-14T02:00:00Z"));
        assertThat(zfw.payload()).containsEntry("product", "CWA").containsEntry("hazard", "TS")
                .containsEntry("qualifier", "ISOL").containsEntry("topFt", 42000L);
        assertThat(parsed.records().get(1).validUntil()).isEqualTo(Instant.parse("2026-10-14T02:30:00Z"));
    }

    @Test
    @DisplayName("an empty body (AWC's 204) has no features; a non-collection is a format error")
    void emptyAndBroken() {
        assertThat(AwcSupport.features(new byte[0], "x")).isEmpty();
        assertThat(AwcSupport.features("{\"type\":\"FeatureCollection\",\"features\":[]}"
                .getBytes(StandardCharsets.UTF_8), "x")).isEmpty();
        assertThatThrownBy(() -> AwcSupport.features("{\"status\":\"error\"}".getBytes(StandardCharsets.UTF_8), "x"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AwcSupport.features("<html>".getBytes(StandardCharsets.UTF_8), "x"))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("pirep: identity receiptTime:lat:lon:sha1 is stable across parses; fields and point")
    void pireps() {
        AwcHazardParser.Parsed first = PirepParser.parse(features("pirep.geojson"));
        AwcHazardParser.Parsed again = PirepParser.parse(features("pirep.geojson"));

        assertThat(first.records()).hasSize(88);
        assertThat(first.records()).extracting(NormalizedRecord::externalId)
                .containsExactlyElementsOf(again.records().stream().map(NormalizedRecord::externalId).toList());
        NormalizedRecord r = first.records().get(0);
        assertThat(r.externalId()).isEqualTo("2026-10-14T04:30:04.569Z:42.1954:-89.0972:"
                + PirepParser.sha1("RFD UA /OV RFD/TM 0422/FLDURC/TP B763/SK BASE043").substring(0, 8));
        assertThat(r.geometry()).isInstanceOf(Point.class);
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-10-14T04:22:00Z")).isEqualTo(r.observedAt());
        assertThat(r.payload()).containsEntry("acType", "B763").containsEntry("reportType", "PIREP");
        NormalizedRecord urgent = first.records().get(2);
        assertThat(urgent.payload()).containsEntry("reportType", "Urgent PIREP").containsEntry("fltLvl", 70L)
                .containsEntry("turbulence", "MOD");
        assertThat(first.records()).anySatisfy(p -> assertThat(p.payload()).containsEntry("icing", "MOD RIME"));
        assertThat(first.records()).anySatisfy(p -> assertThat(p.payload()).containsEntry("skyCover", "OVC"));
        assertThat(PirepParser.join("MOD", "CHOP")).isEqualTo("MOD CHOP");
        assertThat(PirepParser.join(null, null)).isNull();
    }

    @Test
    @DisplayName("taf (API JSON): one record per station, validity from the epoch seconds, issue time with a space")
    void tafJson() {
        TafParser.Parsed parsed = TafParser.json(AdapterFixtures.bytes("awc", "taf.json"),
                new TafParser.Filter(null, Set.of("KAUS", "KDFW", "KJFK", "EGLL")));

        assertThat(parsed.records()).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder("EGLL", "KAUS", "KDFW", "KJFK");
        NormalizedRecord egll = byId(parsed.records(), "EGLL");
        assertThat(egll.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:56:00Z"));
        assertThat(egll.validFrom()).isEqualTo(Instant.ofEpochSecond(1791936000));
        assertThat(egll.validUntil()).isEqualTo(Instant.ofEpochSecond(1792044000));
        assertThat(egll.payload()).containsEntry("stationId", "EGLL").containsEntry("name", "London/Heathrow Intl")
                .containsEntry("elevationM", 26.0);
        assertThat((String) egll.payload().get("raw")).startsWith("TAF EGLL 132256Z");
        assertThat(AwcSupport.instant("2023-11-03 23:20:00.000Z")).isEqualTo(Instant.parse("2023-11-03T23:20:00Z"));
        assertThat(TafParser.json(new byte[0], null).records()).isEmpty();
    }

    @Test
    @DisplayName("taf (cache file): gzip XML filtered by the Texas box or an allowlist, validity from the XML")
    void tafCache() {
        byte[] gz = AdapterFixtures.bytes("awc", "tafs.cache.xml.gz");
        TafParser.Parsed texas = TafParser.xml(gz, new TafParser.Filter(AwcSupport.DEFAULT_ENVELOPE.clone(), Set.of()));
        TafParser.Parsed global = TafParser.xml(gz, new TafParser.Filter(null, Set.of()));
        TafParser.Parsed two = TafParser.xml(gz, new TafParser.Filter(null, Set.of("KAUS", "EGLL")));

        assertThat(global.tafs()).isEqualTo(90);
        assertThat(global.records()).hasSize(90);
        assertThat(texas.records()).hasSize(86);
        assertThat(two.records()).extracting(NormalizedRecord::externalId).containsExactlyInAnyOrder("KAUS", "EGLL");
        NormalizedRecord sbgr = byId(global.records(), "SBGR");
        assertThat(sbgr.observedAt()).isEqualTo(Instant.parse("2026-10-14T04:30:00Z"));
        assertThat(sbgr.validFrom()).isEqualTo(Instant.parse("2026-10-14T06:00:00Z"));
        assertThat(sbgr.validUntil()).isEqualTo(Instant.parse("2026-10-15T12:00:00Z"));
        assertThat(sbgr.payload()).containsEntry("elevationM", 745.0).doesNotContainKey("name");
        assertThat((String) sbgr.payload().get("raw")).startsWith("TAF SBGR 140430Z");
    }

    @Test
    @DisplayName("the TAF cache reader refuses a DOCTYPE and malformed XML")
    void tafCacheRejected() throws Exception {
        assertThatThrownBy(() -> TafParser.xml(("<!DOCTYPE response [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<response><data><TAF><raw_text>&x;</raw_text></TAF></data></response>")
                .getBytes(StandardCharsets.UTF_8), null)).isInstanceOf(UpstreamFormatException.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write("<response><data><TAF>".getBytes(StandardCharsets.UTF_8));
        }
        assertThatThrownBy(() -> TafParser.xml(out.toByteArray(), null)).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("shared helpers: the lat-first bbox, compass points, G-AIRMET levels, chunks, scopes")
    void helpers() {
        assertThat(AwcSupport.bboxLatLon(new Envelope(-130, -60, 20, 55))).isEqualTo("20,-130,55,-60");
        assertThat(AwcHazardParser.hundreds("090")).isEqualTo(9000L);
        assertThat(AwcHazardParser.hundreds("SFC")).isZero();
        assertThat(AwcHazardParser.hundreds("FZL")).isNull();
        assertThat(AwcSupport.chunks(new java.util.LinkedHashSet<>(List.of("A", "B", "C")), 2))
                .containsExactly(List.of("A", "B"), List.of("C"));
        assertThat(AwcSupport.envelope("global", null)).isNull();
        assertThat(AwcSupport.envelope("conus", null)).containsExactly(-125.0, 24.0, -66.5, 50.0);
        assertThat(AwcSupport.stations(List.of(" kaus", "", "EGLL"))).containsExactly("KAUS", "EGLL");
    }

    @Test
    @DisplayName("every payload key is declared in its descriptor's field specs")
    void fieldSpecs() {
        FieldSpecContract.assertPayloadKeysDeclared(new AwcSigmetsAdapter("").descriptor(),
                AwcHazardParser.isigmets(features("isigmet.geojson")).records());
        FieldSpecContract.assertPayloadKeysDeclared(new AwcSigmetsAdapter("").descriptor(),
                AwcHazardParser.airSigmets(features("airsigmet.geojson")).records());
        FieldSpecContract.assertPayloadKeysDeclared(new AwcGairmetsAdapter("").descriptor(),
                AwcHazardParser.gairmets(features("gairmet.geojson")).records());
        FieldSpecContract.assertPayloadKeysDeclared(new AwcCwasAdapter("").descriptor(),
                AwcHazardParser.cwas(features("cwa.geojson")).records());
        FieldSpecContract.assertPayloadKeysDeclared(new AwcPirepsAdapter("").descriptor(),
                PirepParser.parse(features("pirep.geojson")).records());
        FieldSpecContract.assertPayloadKeysDeclared(new AwcTafsAdapter((ai.foresignal.source.spi.AreaOfInterest) null,
                "").descriptor(), TafParser.json(AdapterFixtures.bytes("awc", "taf.json"), null).records());
    }
}
