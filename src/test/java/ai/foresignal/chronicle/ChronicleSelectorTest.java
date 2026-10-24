package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.place.PlaceService;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.view.SavedViewEntity;
import ai.foresignal.view.SavedViewService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Chronicle layer selectors (prompt 131): every selector type parses and round-trips, malformed selectors are
 * {@code 400 CHRONICLE_LAYER_INVALID} with the field, areas match by geometry, buffer and region, and compiled layers
 * apply sources, kinds, 017 conditions, area overrides and the edit's seq.
 */
class ChronicleSelectorTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);
    private static final Instant T = Instant.parse("2026-10-12T12:00:00Z");

    private static JsonNode node(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    private static ChronicleSelector parse(String text) {
        return ChronicleSelector.parse(node(text), "selector");
    }

    private static void invalid(String text, String field) {
        assertThatThrownBy(() -> parse(text)).isInstanceOfSatisfying(ValidationException.class, e -> {
            assertThat(e.getCode()).isEqualTo(ErrorCodes.CHRONICLE_LAYER_INVALID);
            assertThat(e.getErrors()).extracting(err -> err.field()).contains(field);
        });
    }

    @Test
    @DisplayName("every selector type parses and survives a round trip through its stored map")
    void everyTypeRoundTrips() {
        List<String> inputs = List.of(
                "{'type':'records','sources':['acled'],'kinds':['conflict-event'],"
                        + "'conditions':[{'path':'fatalities','op':'>=','value':5}]}",
                "{'type':'saved-view','viewId':'red-sea-shipping'}",
                "{'type':'watchlist','watchlistId':'0b6f7a8e-5a0e-4a65-a1a0-1f6d8e0e2b11'}",
                "{'type':'entities','entityIds':['01JA8Q3V7S2K9W4T6Y1B5N0M3C']}",
                "{'type':'situations','situationIds':['01JA8Q3V7S2K9W4T6Y1B5N0M3D']}",
                "{'type':'findings','findingTypes':['change-point'],'families':['conflict']}",
                "{'type':'series','refs':['series:eia-steo/BREPUUS','rollup:day/*/conflict-event/YE']}",
                "{'type':'raster-products','productIds':['goes-east-ir']}",
                "{'type':'alerts','ruleIds':['0b6f7a8e-5a0e-4a65-a1a0-1f6d8e0e2b12'],'tags':['red-sea']}");
        for (String input : inputs) {
            ChronicleSelector selector = parse(input);
            assertThat(ChronicleSelector.TYPES).contains(selector.type());
            ChronicleSelector again = ChronicleSelector.fromMap(selector.toMap(), JSON);
            assertThat(again).isEqualTo(selector);
        }
        ChronicleSelector.Records r = (ChronicleSelector.Records) parse(inputs.getFirst());
        assertThat(r.conditions()).singleElement().satisfies(c -> {
            assertThat(c.path()).isEqualTo("fatalities");
            assertThat(c.op()).isEqualTo(">=");
            assertThat(c.value()).isEqualTo(5L);
        });
        ChronicleSelector.Findings empty = (ChronicleSelector.Findings) parse("{'type':'findings'}");
        assertThat(empty.findingTypes()).isEmpty();
        assertThat(empty.families()).isEmpty();
    }

    @Test
    @DisplayName("malformed selectors name the field; an unknown type is refused on selector.type")
    void malformedSelectors() {
        invalid("{'sources':['acled']}", "selector.type");
        invalid("{'type':'nope'}", "selector.type");
        // social-narratives was a later prompt's type here; 137 implemented it, so it is checked like the others.
        invalid("{'type':'social-narratives','statuses':['nope']}", "selector.statuses");
        invalid("{'type':'records'}", "selector");
        invalid("{'type':'records','sources':['a'],'extra':1}", "selector.extra");
        invalid("{'type':'records','sources':'acled'}", "selector.sources");
        invalid("{'type':'records','sources':['a'],'conditions':[{'path':'x','value':1}]}",
                "selector.conditions[0].op");
        invalid("{'type':'saved-view'}", "selector.viewId");
        invalid("{'type':'watchlist','watchlistId':'not-a-uuid'}", "selector.watchlistId");
        invalid("{'type':'entities','entityIds':[]}", "selector.entityIds");
        invalid("{'type':'entities','entityIds':['nope']}", "selector.entityIds");
        invalid("{'type':'situations'}", "selector.situationIds");
        invalid("{'type':'series','refs':[]}", "selector.refs");
        invalid("{'type':'alerts','ruleIds':['nope']}", "selector.ruleIds");
        assertThatThrownBy(() -> ChronicleSelector.parse(null, "selector")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("an area matches by geometry, by the buffer around it, by place and by region (a country covers its admin-1 codes)")
    void areas() {
        ChronicleArea area = ChronicleArea.parse(node("{'geometry':{'type':'Polygon','coordinates':"
                + "[[[40,10],[45,10],[45,15],[40,15],[40,10]]]},'regionCodes':['ye','SA-01']}"), "area", id -> true);
        assertThat(area.regionCodes()).containsExactly("YE", "SA-01");
        ChronicleArea.Resolved resolved = area.resolve(id -> null);
        assertThat(resolved.contains(point(42, 12), null)).isTrue();
        assertThat(resolved.contains(point(50, 20), null)).isFalse();
        assertThat(resolved.contains(null, "YE")).isTrue();
        assertThat(resolved.contains(null, "YE-SA")).isTrue();
        assertThat(resolved.contains(null, "SA-01")).isTrue();
        assertThat(resolved.contains(null, "SA-02")).isFalse();
        assertThat(resolved.contains(null, null)).isFalse();

        ChronicleArea buffered = ChronicleArea.parse(node("{'geometry':{'type':'Point','coordinates':[0,0]},"
                + "'bufferKm':200}"), "area", id -> true);
        assertThat(buffered.resolve(id -> null).contains(point(1, 1), null)).isTrue();
        assertThat(buffered.resolve(id -> null).contains(point(3, 3), null)).isFalse();

        ChronicleArea places = ChronicleArea.parse(node("{'placeIds':['aden']}"), "area", id -> true);
        Geometry aden = WGS84.toGeometry(new org.locationtech.jts.geom.Envelope(44, 46, 12, 14));
        assertThat(places.resolve(id -> "aden".equals(id) ? aden : null).contains(point(45, 13), null)).isTrue();
        assertThat(places.resolve(id -> null).contains(point(45, 13), null)).isFalse();

        assertThat(ChronicleArea.Resolved.ANYWHERE.contains(null, null)).isTrue();
        assertThat(ChronicleArea.parse(null, "area", id -> true)).isNull();
        assertThat(ChronicleArea.fromMap(area.toMap(), JSON).regionCodes()).containsExactly("YE", "SA-01");
    }

    @Test
    @DisplayName("malformed areas are refused with their field")
    void malformedAreas() {
        assertThatThrownBy(() -> ChronicleArea.parse(node("{'regionCodes':['Yemen']}"), "area", id -> true))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(err -> err.field()).contains("area.regionCodes"));
        assertThatThrownBy(() -> ChronicleArea.parse(node("{'placeIds':['nowhere']}"), "area", id -> false))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ChronicleArea.parse(node("{'bufferKm':-1}"), "area", id -> true))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ChronicleArea.parse(node("{'geometry':{'type':'Nope'}}"), "area", id -> true))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ChronicleArea.parse(node("{'shape':1}"), "area", id -> true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a records layer applies its sources, kinds, conditions, the chronicle area, an override and the edit's seq")
    void compiledRecordsLayer() {
        ChronicleLayerCompiler compiler = compiler(mock(SavedViewService.class));
        ChronicleSelector selector = parse("{'type':'records','sources':['acled'],'kinds':['conflict-event'],"
                + "'conditions':[{'path':'fatalities','op':'>=','value':5}]}");
        ChronicleArea yemen = ChronicleArea.parse(node("{'regionCodes':['YE']}"), "area", id -> true);
        CompiledLayer layer = compiler.compile("C", "L", "ACLED", selector, CaptureMode.FULL_MODE, 10L, null, yemen);

        assertThat(layer.matches(row(11, "acled", "conflict-event", "{\"fatalities\":7}", null, "YE"))).isTrue();
        assertThat(layer.matches(row(9, "acled", "conflict-event", "{\"fatalities\":7}", null, "YE")))
                .as("before the edit's seq").isFalse();
        assertThat(layer.matches(row(12, "acled", "conflict-event", "{\"fatalities\":2}", null, "YE"))).isFalse();
        assertThat(layer.matches(row(13, "ucdp", "conflict-event", "{\"fatalities\":7}", null, "YE"))).isFalse();
        assertThat(layer.matches(row(14, "acled", "protest", "{\"fatalities\":7}", null, "YE"))).isFalse();
        assertThat(layer.matches(row(15, "acled", "conflict-event", "{\"fatalities\":7}", null, "SA"))).isFalse();

        ChronicleArea override = ChronicleArea.parse(node("{'geometry':{'type':'Polygon','coordinates':"
                + "[[[30,20],[35,20],[35,25],[30,25],[30,20]]]}}"), "areaOverride", id -> true);
        CompiledLayer red = compiler.compile("C", "L2", "Red Sea", selector, CaptureMode.FULL_MODE, null, override,
                yemen);
        assertThat(red.matches(row(16, "acled", "conflict-event", "{\"fatalities\":9}", point(32, 22), "EG")))
                .as("the override replaces the chronicle area").isTrue();
        assertThat(red.matches(row(17, "acled", "conflict-event", "{\"fatalities\":9}", point(44, 15), "YE")))
                .isFalse();
        assertThat(red.sources()).containsExactly("acled");
        assertThat(red.recordBacked()).isTrue();
    }

    @Test
    @DisplayName("membership, findings, series, raster and alert layers match what they name")
    void compiledOtherLayers() {
        SavedViewService views = mock(SavedViewService.class);
        SavedViewEntity view = mock(SavedViewEntity.class);
        when(views.find("vessels")).thenReturn(Optional.of(view));
        when(views.compile(view)).thenReturn(new SavedViewService.CompiledView(view,
                new RecordPredicate(Set.of("ais"), Set.of("vessel"), List.of(), null), false, List.of()));
        ChronicleLayerCompiler compiler = compiler(views);
        ChronicleArea anywhere = null;

        CompiledLayer findings = compiler.compile("C", "F", "Findings", parse("{'type':'findings',"
                + "'findingTypes':['change-point']}"), CaptureMode.FULL_MODE, null, null, anywhere);
        assertThat(findings.matches(row(1, ChronicleVocabulary.ANALYTICS_SOURCE, ChronicleVocabulary.FINDING_KIND,
                "{\"findingType\":\"change-point\"}", null, null))).isTrue();
        assertThat(findings.matches(row(2, ChronicleVocabulary.ANALYTICS_SOURCE, ChronicleVocabulary.FINDING_KIND,
                "{\"findingType\":\"co-occurrence\"}", null, null))).isFalse();

        CompiledLayer series = compiler.compile("C", "S", "Series", parse("{'type':'series',"
                + "'refs':['series:eia-steo/BREPUUS','rollup:day/*/conflict-event/YE']}"), CaptureMode.FULL_MODE,
                null, null, anywhere);
        assertThat(series.matches(row(3, "eia-steo", ChronicleVocabulary.SERIES_POINT_KIND,
                "{\"series\":\"BREPUUS\"}", null, null, "BREPUUS:2026-10"))).isTrue();
        assertThat(series.matches(row(4, "eia-steo", ChronicleVocabulary.SERIES_POINT_KIND,
                "{\"series\":\"OTHER\"}", null, null, "OTHER:2026-10"))).isFalse();

        CompiledLayer raster = compiler.compile("C", "R", "IR", parse("{'type':'raster-products',"
                + "'productIds':['goes-east-ir']}"), CaptureMode.frames(4), null, null, anywhere);
        assertThat(raster.matches(row(5, "goes", ChronicleVocabulary.RASTER_FRAME_KIND, "{}", null, null,
                "goes-east-ir"))).isTrue();
        assertThat(raster.matches(row(6, "goes", ChronicleVocabulary.RASTER_FRAME_KIND, "{}", null, null,
                "goes-east-ir:partial"))).isFalse();

        CompiledLayer saved = compiler.compile("C", "V", "View", parse("{'type':'saved-view','viewId':'vessels'}"),
                CaptureMode.FULL_MODE, null, null, anywhere);
        assertThat(saved.matches(row(7, "ais", "vessel", "{}", null, null))).isTrue();
        assertThat(saved.matches(row(8, "ais", "aircraft", "{}", null, null))).isFalse();

        CompiledLayer alerts = compiler.compile("C", "A", "Alerts", parse("{'type':'alerts','tags':['red-sea']}"),
                CaptureMode.FULL_MODE, null, null, anywhere);
        assertThat(alerts.recordBacked()).isFalse();
        assertThat(alerts.matches(row(9, "ais", "vessel", "{}", null, null))).isFalse();
    }

    @Test
    @DisplayName("frames are only for raster products, raster products take frames, full or events-only, alerts full")
    void captureModePerSelector() {
        assertThatThrownBy(() -> ChronicleService.checkMode(parse("{'type':'records','sources':['a']}"),
                CaptureMode.frames(2), "capture")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ChronicleService.checkMode(parse("{'type':'raster-products','productIds':['p']}"),
                CaptureMode.downsampled(java.time.Duration.ofMinutes(1)), "capture"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ChronicleService.checkMode(parse("{'type':'alerts'}"), CaptureMode.EVENTS_ONLY_MODE,
                "capture")).isInstanceOf(ValidationException.class);
        ChronicleService.checkMode(parse("{'type':'raster-products','productIds':['p']}"), CaptureMode.frames(4),
                "capture");
        ChronicleService.checkMode(parse("{'type':'alerts'}"), CaptureMode.FULL_MODE, "capture");
    }

    // ------------------------------------------------------------------------- helpers

    private static ChronicleLayerCompiler compiler(SavedViewService views) {
        PlaceService places = mock(PlaceService.class);
        when(places.area(anyString())).thenReturn(Optional.empty());
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        return new ChronicleLayerCompiler(OperatorRegistry.builtIn(), views, places, jdbc, JSON);
    }

    static Geometry point(double lon, double lat) {
        Geometry p = WGS84.createPoint(new Coordinate(lon, lat));
        p.setSRID(4326);
        return p;
    }

    static CapturedRow row(long seq, String source, String kind, String payload, Geometry geometry, String region) {
        return row(seq, source, kind, payload, geometry, region, "e" + seq);
    }

    static CapturedRow row(long seq, String source, String kind, String payload, Geometry geometry, String region,
            String externalId) {
        byte[] wkb = geometry == null ? null : new WKBWriter().write(geometry);
        return new CapturedRow(seq, T, T, T, null, null, null, null, null, 1, (short) 0,
                PersistReason.FIRST.code(), (short) 0, false, source, externalId, kind, wkb, payload, null, null,
                null, region);
    }
}
