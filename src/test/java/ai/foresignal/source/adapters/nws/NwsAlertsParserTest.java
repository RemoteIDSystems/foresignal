package ai.foresignal.source.adapters.nws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Geometry;

/**
 * The NWS alerts parser against recorded fixtures (prompt 047): {@code national.json} and
 * {@code area-TX.json} were captured 2026-10-13; {@code edge-cases.json} is synthetic, built from the
 * captured Texas alerts (see the fixture README).
 */
class NwsAlertsParserTest {

    private static final String DIR = "nws";

    private static byte[] fixture(String name) {
        return AdapterFixtures.bytes(DIR, name);
    }

    private static NormalizedRecord byId(NwsAlertsParser.ParseResult result, String id) {
        return result.alerts().stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"national.json", "area-TX.json", "edge-cases.json"})
    @DisplayName("every payload key is declared with its type (011 contract helper)")
    void fieldSpecs(String name) {
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture(name), CountyGeometries.none(), null);
        assertThat(result.alerts()).isNotEmpty();
        FieldSpecContract.assertPayloadKeysDeclared(new NwsAlertsAdapter((CountyGeometries) null, "").descriptor(),
                result.alerts());
    }

    @Test
    @DisplayName("the national capture: 217 alerts, 16 with polygons, ids and the time base")
    void national() {
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture("national.json"), CountyGeometries.none(), null);

        assertThat(result.diagnostics().features()).isEqualTo(217);
        assertThat(result.alerts()).hasSize(217);
        assertThat(result.alerts().stream().filter(r -> r.geometry() != null)).hasSize(16);
        NormalizedRecord gale = byId(result, "urn:oid:2.49.0.1.840.0.f4b19a6eb696d7189644e3ded5ce97d207911650.011.1");
        assertThat(gale.kind()).isEqualTo("nws-alert");
        assertThat(gale.observedAt()).isEqualTo(Instant.parse("2026-10-12T23:49:00Z"));
        assertThat(gale.eventTime()).isEqualTo(Instant.parse("2026-10-13T01:00:00Z"));
        assertThat(gale.validFrom()).isEqualTo(Instant.parse("2026-10-13T01:00:00Z"));
        // expires 14:00Z, ends 2026-10-14T01:00Z: the later one.
        assertThat(gale.validUntil()).isEqualTo(Instant.parse("2026-10-14T01:00:00Z"));
        assertThat(gale.payload()).containsEntry("event", "Gale Warning")
                .containsEntry("geometrySource", "none")
                .containsEntry("ugc", List.of("PKZ816"))
                .containsEntry("same", List.of("058816"))
                .containsEntry("eventCode", Map.of("same", "NWS", "nws", "GLW"))
                .containsEntry("vtec", List.of("/O.NEW.PAFG.GL.W.9486.261013T0100Z-261014T0100Z/"));
        assertThat(gale.payload().values()).doesNotContainNull();
    }

    @Test
    @DisplayName("polygon, county union and none: the geometry source and the SAME-derived FIPS codes")
    void geometrySources() {
        Geometry square = Geo.factory().toGeometry(new org.locationtech.jts.geom.Envelope(-94, -93, 30, 31));
        List<Collection<String>> calls = new ArrayList<>();
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture("edge-cases.json"),
                WeatherAdapterHarness.counties(square, calls), null);

        assertThat(byId(result, "urn:oid:test.polygon.1").payload()).containsEntry("geometrySource", "polygon");
        assertThat(byId(result, "urn:oid:test.polygon.1").geometry().getGeometryType()).isEqualTo("Polygon");
        NormalizedRecord county = byId(result, "urn:oid:test.county.1");
        assertThat(county.payload()).containsEntry("geometrySource", "county-union");
        assertThat(county.geometry()).isEqualTo(square);
        assertThat(calls).containsExactly(List.of("48199", "48241"));
        NormalizedRecord zone = byId(result, "urn:oid:test.zone.1");
        assertThat(zone.geometry()).isNull();
        assertThat(zone.payload()).containsEntry("geometrySource", "none");
        assertThat(result.diagnostics().countyUnions()).isEqualTo(1);
    }

    @Test
    @DisplayName("without ref_admin2 a county-only alert has no geometry")
    void countiesNotLoaded() {
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture("edge-cases.json"), CountyGeometries.none(), null);
        NormalizedRecord county = byId(result, "urn:oid:test.county.1");
        assertThat(county.geometry()).isNull();
        assertThat(county.payload()).containsEntry("geometrySource", "none");
    }

    @Test
    @DisplayName("onset is eventTime and validFrom; effective is eventTime when there is no onset")
    void onset() {
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture("edge-cases.json"), CountyGeometries.none(), null);
        NormalizedRecord polygon = byId(result, "urn:oid:test.polygon.1");
        assertThat(polygon.eventTime()).isEqualTo(Instant.parse("2026-10-12T15:35:00Z"));
        assertThat(polygon.validFrom()).isEqualTo(Instant.parse("2026-10-12T15:35:00Z"));
        NormalizedRecord minor = byId(result, "urn:oid:test.minor.1");
        assertThat(minor.eventTime()).isEqualTo(Instant.parse("2026-10-12T17:00:00Z"));
        assertThat(minor.validFrom()).isNull();
    }

    @Test
    @DisplayName("validUntil is the later of expires and ends; an onset after it drops validFrom")
    void validity() {
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture("edge-cases.json"), CountyGeometries.none(), null);
        NormalizedRecord ends = byId(result, "urn:oid:test.ends.1");
        assertThat(ends.validUntil()).isEqualTo(Instant.parse("2026-10-13T22:00:00Z"));
        NormalizedRecord inverted = byId(result, "urn:oid:test.inverted.1");
        assertThat(inverted.validUntil()).isEqualTo(Instant.parse("2026-10-12T09:30:00Z"));
        assertThat(inverted.validFrom()).isNull();
        assertThat(inverted.eventTime()).isEqualTo(Instant.parse("2026-10-12T10:00:00Z"));
    }

    @Test
    @DisplayName("Update and Cancel references are superseded; a referenced alert still listed is dropped")
    void references() {
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture("edge-cases.json"), CountyGeometries.none(), null);
        assertThat(result.superseded()).containsOnlyKeys("urn:oid:test.old.1", "urn:oid:test.old.2");
        assertThat(result.superseded().get("urn:oid:test.old.1")).isEqualTo(Instant.parse("2026-10-12T15:35:00Z"));
        assertThat(result.ids()).doesNotContain("urn:oid:test.old.1").contains("urn:oid:test.update.1",
                "urn:oid:test.cancel.1");
        assertThat(result.diagnostics().droppedSuperseded()).isEqualTo(1);
    }

    @Test
    @DisplayName("long text is cut to 8 KB with an ellipsis; the storm parameters are numbers")
    void textAndParameters() {
        NwsAlertsParser.ParseResult result = NwsAlertsParser.parse(fixture("edge-cases.json"), CountyGeometries.none(), null);
        NormalizedRecord severe = byId(result, "urn:oid:test.severe.1");
        String description = (String) severe.payload().get("description");
        assertThat(description.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(8192);
        assertThat(description).endsWith("…");
        assertThat(severe.payload()).containsEntry("maxHailSizeIn", 1.75)
                .containsEntry("maxWindGustMph", 60.0)
                .containsEntry("tornadoDetection", "POSSIBLE")
                .containsEntry("thunderstormDamageThreat", "CONSIDERABLE")
                .containsEntry("eventCode", Map.of("same", "SVR", "nws", "SVW"));
        assertThat((String) severe.payload().get("motion")).contains("245DEG");
    }

    @Test
    @DisplayName("truncate keeps multi-byte characters whole")
    void truncateMultibyte() {
        String text = "é".repeat(5000);
        String cut = NwsAlertsParser.truncate(text);
        assertThat(cut.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(8192);
        assertThat(cut).endsWith("…").startsWith("éé");
        assertThat(NwsAlertsParser.truncate("short")).isEqualTo("short");
        assertThat(NwsAlertsParser.truncate("  ")).isNull();
    }

    @Test
    @DisplayName("minSeverity keeps alerts at or above it")
    void minSeverity() {
        NwsAlertsParser.ParseResult all = NwsAlertsParser.parse(fixture("edge-cases.json"), CountyGeometries.none(), null);
        NwsAlertsParser.ParseResult moderate = NwsAlertsParser.parse(fixture("edge-cases.json"), CountyGeometries.none(),
                "Moderate");
        assertThat(all.ids()).contains("urn:oid:test.minor.1");
        assertThat(moderate.ids()).doesNotContain("urn:oid:test.minor.1").contains("urn:oid:test.severe.1");
        assertThat(moderate.diagnostics().skippedSeverity()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("county UGC detection")
    void counties() {
        assertThat(NwsAlertsParser.allCounties(List.of("TXC199", "TXC241"))).isTrue();
        assertThat(NwsAlertsParser.allCounties(List.of("TXC199", "TXZ245"))).isFalse();
        assertThat(NwsAlertsParser.allCounties(List.of("PKZ816"))).isFalse();
    }

    @Test
    @DisplayName("a body that is not a FeatureCollection is a format error")
    void notAFeatureCollection() {
        assertThatThrownBy(() -> NwsAlertsParser.parse("{\"type\":\"Feature\"}".getBytes(StandardCharsets.UTF_8), null, null))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NwsAlertsParser.parse("{\"type\":\"FeatureColl".getBytes(StandardCharsets.UTF_8), null, null))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NwsAlertsParser.parse(new byte[0], null, null)).isInstanceOf(UpstreamFormatException.class);
    }
}
