package ai.foresignal.source.adapters.hydro.usgswater;

import static ai.foresignal.source.adapters.hydro.HydroTestSupport.features;
import static ai.foresignal.source.adapters.hydro.HydroTestSupport.node;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import tools.jackson.databind.JsonNode;

/**
 * {@link UsgsLatestContinuousParser} on the two 2026-10-13 {@code latest-continuous} pages around Denver and the
 * matching {@code monitoring-locations} names: values as text, the {@code SEASONAL} qualifier with a null value,
 * non-numeric values ({@code Ice}, {@code Eqp}), sentinels, units, the name table's state round trip, and the
 * descriptor, parameter checks and snapshot contract of {@link UsgsWaterAdapter}.
 */
class UsgsLatestContinuousParserTest {

    static List<JsonNode> all() {
        List<JsonNode> out = new ArrayList<>(features("usgs-water", "latest-page1.json"));
        out.addAll(features("usgs-water", "latest-page2.json"));
        return out;
    }

    static Map<String, UsgsLatestContinuousParser.Site> sites() {
        return UsgsLatestContinuousParser.sites(features("usgs-water", "monitoring-locations.json"));
    }

    static EnvSources.Parsed parsed() {
        return UsgsLatestContinuousParser.parse(all(), sites());
    }

    static JsonNode feature(String value, String qualifier) {
        return node("""
                {"type": "Feature", "geometry": {"type": "Point", "coordinates": [-105.0, 39.7]},
                 "properties": {"monitoring_location_id": "USGS-1", "parameter_code": "00060",
                   "time": "2026-10-13T22:00:00+00:00", "value": %s, "unit_of_measure": "ft^3/s",
                   "approval_status": "Provisional", "qualifier": %s, "time_series_id": "x",
                   "last_modified": "2026-10-13T22:09:24+00:00"}}
                """.formatted(value, qualifier));
    }

    @Test
    @DisplayName("eight readings with names; text values become numbers; ids are site:parameter")
    void readings() {
        EnvSources.Parsed p = parsed();
        assertThat(p.records()).hasSize(8).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("water-observation");
            assertThat(r.observedAt()).isEqualTo(r.eventTime());
            assertThat(r.payload()).doesNotContainKeys("time_series_id", "last_modified", "lastModified");
        });
        assertThat(p.skipped()).isEmpty();
        NormalizedRecord bear = EnvTestSupport.byId(p.records(), "USGS-06710385:00060");
        assertThat(bear.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:00:00Z"));
        assertThat(bear.geometry().getCoordinate().x).isEqualTo(-105.336944);
        assertThat(bear.payload()).containsEntry("siteName", "BEAR CREEK ABOVE EVERGREEN, CO")
                .containsEntry("siteType", "ST").containsEntry("parameterCode", "00060")
                .containsEntry("parameterName", "Discharge").containsEntry("value", 8.65)
                .containsEntry("unit", "ft3/s").containsEntry("approval", "Provisional")
                .doesNotContainKey("qualifier");
        assertThat(EnvTestSupport.byId(p.records(), "USGS-06710150:00065").payload())
                .containsEntry("parameterName", "Gage height").containsEntry("unit", "ft").containsEntry("value", 1.63);
        assertThat(EnvTestSupport.byId(p.records(), "USGS-395331105134400:00060").payload())
                .containsEntry("siteType", "LK").containsEntry("value", 0.0).containsEntry("approval", "Approved");
        FieldSpecContract.assertPayloadKeysDeclared(new UsgsWaterAdapter("", () -> null).descriptor(), p.records());
    }

    @Test
    @DisplayName("a null value with the SEASONAL qualifier keeps the record without a value")
    void seasonal() {
        NormalizedRecord lakewood = EnvTestSupport.byId(parsed().records(), "USGS-06711780:00065");
        assertThat(lakewood.payload()).doesNotContainKey("value").containsEntry("qualifier", "SEASONAL");
        assertThat(lakewood.observedAt()).isEqualTo(Instant.parse("2026-10-01T07:05:00Z"));
    }

    @Test
    @DisplayName("Ice and Eqp are left out of value and recorded as qualifiers; -999 is scrubbed")
    void nonNumeric() {
        NormalizedRecord ice = UsgsLatestContinuousParser.feature(feature("\"Ice\"", "null"), Map.of()).record();
        assertThat(ice.payload()).doesNotContainKey("value").containsEntry("qualifier", "Ice");
        NormalizedRecord eqp = UsgsLatestContinuousParser.feature(feature("\"Eqp\"", "[\"P\"]"), Map.of()).record();
        assertThat(eqp.payload()).doesNotContainKey("value").containsEntry("qualifier", "P,Eqp");
        NormalizedRecord sentinel = UsgsLatestContinuousParser.feature(feature("\"-999\"", "null"), Map.of()).record();
        assertThat(sentinel.payload()).doesNotContainKeys("value", "qualifier");
        NormalizedRecord number = UsgsLatestContinuousParser.feature(feature("12.5", "\"e\""), Map.of()).record();
        assertThat(number.payload()).containsEntry("value", 12.5).containsEntry("qualifier", "e")
                .doesNotContainKeys("siteName", "siteType");
    }

    @Test
    @DisplayName("features without an id, a time or a point are skipped; a repeated id keeps the newest time")
    void skipsAndDuplicates() {
        assertThat(UsgsLatestContinuousParser.feature(node("{\"properties\": {}}"), Map.of()).reason())
                .isEqualTo(MappedRecord.MISSING_ID);
        assertThat(UsgsLatestContinuousParser.feature(node("""
                {"geometry": {"type": "Point", "coordinates": [-105, 39]}, "properties": {"monitoring_location_id":
                 "USGS-1", "parameter_code": "00060", "time": "0001-01-01T00:00:00Z"}}"""), Map.of()).reason())
                .isEqualTo(MappedRecord.MISSING_OBSERVED_AT);
        assertThat(UsgsLatestContinuousParser.feature(node("""
                {"geometry": null, "properties": {"monitoring_location_id": "USGS-1", "parameter_code": "00060",
                 "time": "2026-10-13T22:00:00Z"}}"""), Map.of()).reason()).isEqualTo(EnvSources.MISSING_LOCATION);
        JsonNode older = node("""
                {"geometry": {"type": "Point", "coordinates": [-105, 39]}, "properties": {"monitoring_location_id":
                 "USGS-1", "parameter_code": "00060", "time": "2026-10-13T21:00:00Z", "value": "1"}}""");
        EnvSources.Parsed p = UsgsLatestContinuousParser.parse(List.of(older, feature("\"2\"", "null")), Map.of());
        assertThat(p.records()).singleElement().satisfies(r -> assertThat(r.payload()).containsEntry("value", 2.0));
        assertThat(p.skipped()).containsEntry(MappedRecord.DUPLICATE_ID, 1);
    }

    @Test
    @DisplayName("units: ft^3/s to ft3/s, deg C to degC, others kept")
    void units() {
        assertThat(UsgsLatestContinuousParser.unit("ft^3/s")).isEqualTo("ft3/s");
        assertThat(UsgsLatestContinuousParser.unit("deg C")).isEqualTo("degC");
        assertThat(UsgsLatestContinuousParser.unit(" ft ")).isEqualTo("ft");
        assertThat(UsgsLatestContinuousParser.unit("uS/cm")).isEqualTo("uS/cm");
        assertThat(UsgsLatestContinuousParser.unit(null)).isNull();
    }

    @Test
    @DisplayName("the name table survives the state round trip; damaged state is empty")
    void namesRoundTrip() {
        Map<String, UsgsLatestContinuousParser.Site> sites = sites();
        assertThat(sites).hasSize(8).containsEntry("USGS-06727410",
                new UsgsLatestContinuousParser.Site("FOURMILE CREEK AT LOGAN MILL ROAD NEAR CRISMAN, CO", "ST"));
        assertThat(UsgsLatestContinuousParser.sitesFromJson(UsgsLatestContinuousParser.sitesToJson(sites)))
                .isEqualTo(sites);
        assertThat(UsgsLatestContinuousParser.sitesFromJson("{not json")).isEmpty();
        assertThat(UsgsLatestContinuousParser.sitesFromJson(null)).isEmpty();
    }

    @Test
    @DisplayName("checkParams: a box of at most 5° or a site list; five-digit codes")
    void params() {
        UsgsWaterAdapter adapter = new UsgsWaterAdapter("", () -> null);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", new Envelope(-106, -101, 38, 43)))))
                .isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", new Envelope(-110, -100, 38, 43)))))
                .containsKey("bbox");
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey("bbox");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("monitoringLocations", List.of("USGS-06710385")))))
                .isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("monitoringLocations", List.of("06710385")))))
                .containsKey("monitoringLocations");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", new Envelope(-106, -105, 39, 40),
                "parameterCodes", List.of("00060", "60"))))).containsKey("parameterCodes");
    }

    @Test
    @DisplayName("descriptor valid and never naming waterservices; snapshot contract across fifteen minutes")
    void descriptorAndSnapshot() {
        UsgsWaterAdapter adapter = new UsgsWaterAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().recordTtl()).isEqualTo(Duration.ofHours(3));
        assertThat(UsgsWaterAdapter.API_URL).doesNotContain("waterservices");
        NormalizedRecord r = EnvTestSupport.byId(parsed().records(), "USGS-06710385:00060");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofMinutes(15))));
    }
}
