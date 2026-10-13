package ai.foresignal.kml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.map.SpriteCatalog;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.AsOfQueryService;
import ai.foresignal.store.query.CountResult;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import ai.foresignal.trigger.TriggerRuleService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.w3c.dom.Document;
import tools.jackson.databind.json.JsonMapper;

/** {@link KmlService} with stand-ins for the queries (prompt 058): the cap, egress, fields, tracks and fences. */
class KmlServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-10-13T10:00:00Z");
    static final PublicUrls URLS = new PublicUrls("https://fs.example", PublicUrls.V1);

    static final SourceDescriptor QUAKES = SourceDescriptor.builder("kml-quakes")
            .name("Quakes").attribution("USGS").kind("quake").mode(Mode.PUSH).defaultBudget(Budget.of(60, 1000))
            .titleField("place")
            .tileProperties("mag", "place")
            .fieldSpec("mag", new FieldSpec(FieldType.NUMBER, "1", "Magnitude", null, null, Set.of()))
            .fieldSpec("depth", new FieldSpec(FieldType.NUMBER, "km", "Depth", null, "positive down", Set.of()))
            .kindDisplay("quake", new KindDisplay("M {mag}", "earthquake", "#D9480F", null))
            .build();

    static final SourceDescriptor PLANES = SourceDescriptor.builder("kml-planes")
            .name("Planes").attribution("ADS-B").kind("aircraft").mode(Mode.PUSH).defaultBudget(Budget.of(60, 1000))
            .tileProperties("track")
            .fieldSpec("alt_baro", new FieldSpec(FieldType.NUMBER, "[ft_i]", "Altitude", null, "PRESSURE", Set.of()))
            .fieldSpec("track", new FieldSpec(FieldType.NUMBER, "deg", "Track", null, null, Set.of()))
            .kindDisplay("aircraft", new KindDisplay(null, "plane", "#112233", null, "track", null, null))
            .build();

    final RecordQueryService queries = mock(RecordQueryService.class);
    final AsOfQueryService asOf = mock(AsOfQueryService.class);
    final TrackService tracks = mock(TrackService.class);
    final SourceRegistry registry = mock(SourceRegistry.class);
    final EgressPolicy egress = mock(EgressPolicy.class);
    final SpriteCatalog sprite = new SpriteCatalog(JSON);
    final KmlService service = new KmlService(queries, asOf, tracks, registry, egress, sprite, new KmlWriter(),
            mock(TriggerRuleService.class), JSON);

    KmlServiceTest() {
        for (SourceDescriptor d : List.of(QUAKES, PLANES)) {
            when(registry.contains(d.id())).thenReturn(true);
            when(registry.descriptor(d.id())).thenReturn(d);
        }
        when(egress.allows(eq(EgressPath.FEED), any(), any())).thenReturn(true);
    }

    static RecordRow row(String source, String id, String kind, Instant observed, String payload, String geometry) {
        return new RecordRow(source, id, kind, 1, 1, observed, observed, null, null, null, false, null, geometry,
                payload, null, null, true, 0, null, null, null, null, null, null, null, null, null, null);
    }

    static KmlService.Selection selection(List<String> sources) {
        return new KmlService.Selection("Doc", sources, List.of(), null, Set.of(), null, Viewer.anonymous());
    }

    static Document parse(byte[] xml) throws Exception {
        var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    @Test
    @DisplayName("at the cap the document is truncated with the counted total; Placemarks carry title, fields and time")
    void capAndTruncation() throws Exception {
        List<RecordRow> rows = new ArrayList<>();
        for (int i = 0; i < KmlService.MAX_PLACEMARKS; i++) {
            rows.add(row("kml-quakes", "q" + i, "quake", T.minusSeconds(i), "{\"mag\":4.5,\"place\":\"Here <x>\"}",
                    "{\"type\":\"Point\",\"coordinates\":[-97.5,30.25]}"));
        }
        when(queries.list(any(CurrentQuery.class))).thenReturn(new Slices.RecordSlice(rows, "next", 1, null));
        when(queries.countCurrent(any(CurrentQuery.class))).thenReturn(CountResult.of(12_345, 1));
        KmlService.Output out = service.records(selection(List.of("kml-quakes")), null, URLS, false);
        assertThat(out.mediaType()).isEqualTo(KmlWriter.KML_MEDIA_TYPE);
        Document doc = parse(out.body());
        assertThat(doc.getElementsByTagNameNS(KmlWriter.KML, "Placemark").getLength()).isEqualTo(5_000);
        assertThat(doc.getElementsByTagNameNS(KmlWriter.KML, "Snippet").item(0).getTextContent())
                .isEqualTo("truncated: showing 5000 of 12345");
        String xml = new String(out.body(), StandardCharsets.UTF_8);
        assertThat(xml).contains("<description>USGS</description>");
        assertThat(xml).contains("https://fs.example/v1/map/icons/earthquake.png");
        assertThat(xml).contains("Magnitude: 4.5\nplace: Here &amp;lt;x&amp;gt;");
    }

    @Test
    @DisplayName("a record that fails EgressPolicy.allows(FEED) is left out and not counted")
    void egressFilter() throws Exception {
        when(egress.allows(EgressPath.FEED, "kml-planes", Viewer.anonymous())).thenReturn(false);
        when(queries.list(any(CurrentQuery.class))).thenReturn(new Slices.RecordSlice(List.of(
                row("kml-quakes", "q1", "quake", T, "{\"mag\":5}", null),
                row("kml-planes", "p1", "aircraft", T, "{\"track\":90}", null)), null, 1, null));
        Document doc = parse(service.records(selection(List.of()), null, URLS, false).body());
        assertThat(doc.getElementsByTagNameNS(KmlWriter.KML, "Placemark").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagNameNS(KmlWriter.KML, "Snippet").getLength()).isZero();
    }

    @Test
    @DisplayName("KMZ bundles doc.kml with relative icon hrefs and the icons used")
    void kmz() throws Exception {
        when(queries.list(any(CurrentQuery.class))).thenReturn(new Slices.RecordSlice(List.of(
                row("kml-planes", "p1", "aircraft", T, "{\"track\":90}",
                        "{\"type\":\"Point\",\"coordinates\":[1,2]}")), null, 1, null));
        KmlService.Output out = service.records(selection(List.of("kml-planes")), null, URLS, true);
        assertThat(out.mediaType()).isEqualTo(KmlWriter.KMZ_MEDIA_TYPE);
        List<String> names = new ArrayList<>();
        String doc = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.body()))) {
            for (var e = zip.getNextEntry(); e != null; e = zip.getNextEntry()) {
                names.add(e.getName());
                byte[] bytes = zip.readAllBytes();
                if (e.getName().equals("doc.kml")) {
                    doc = new String(bytes, StandardCharsets.UTF_8);
                }
            }
        }
        assertThat(names).containsExactly("doc.kml", "icons/plane.png");
        assertThat(doc).contains("<href>icons/plane.png</href>").contains("<heading>90</heading>");
    }

    @Test
    @DisplayName("tracks keep the newest 5,000 points, convert feet to meters and use the PRESSURE datum as absolute")
    void trackTruncation() {
        int n = 5_010;
        List<double[]> coords = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        List<Double> alts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            coords.add(new double[] {i * 0.001, 0});
            times.add(T.toEpochMilli() + i * 1000L);
            alts.add(i == 0 ? null : 1000.0);
        }
        TrackResult result = new TrackResult("kml-planes", "p1", "aircraft", T, T.plusSeconds(n), List.of(coords),
                List.of(times), Map.of("alt_baro", List.of(alts)), false, n, n, 0, 1, false, false, false, 1, false, true);
        KmlModel.Track track = KmlService.track("P1", null, result, "alt_baro", PLANES);
        assertThat(track.truncated()).isTrue();
        assertThat(track.whens()).hasSize(KmlService.MAX_TRACK_POINTS);
        assertThat(track.whens().getFirst()).isEqualTo(Instant.ofEpochMilli(T.toEpochMilli() + 10_000));
        assertThat(track.whens().getLast()).isEqualTo(Instant.ofEpochMilli(T.toEpochMilli() + (n - 1) * 1000L));
        assertThat(track.altitudeMode()).isEqualTo("absolute");
        assertThat(track.coords().getFirst()[2]).isCloseTo(304.8, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("without an altitude field a track is clamped to the ground, and a repeated lone point appears once")
    void clampToGround() {
        TrackResult result = new TrackResult("kml-quakes", "q1", "quake", T, T, List.of(List.of(new double[] {1, 2},
                new double[] {1, 2})), List.of(List.of(T.toEpochMilli(), T.toEpochMilli())), Map.of(), false, 1, 1, 0,
                1, false, false, false, 1, false, true);
        KmlModel.Track track = KmlService.track("Q", null, result, null, QUAKES);
        assertThat(track.altitudeMode()).isEqualTo("clampToGround");
        assertThat(track.whens()).hasSize(1);
        assertThat(track.coords().getFirst()[2]).isZero();
    }

    @Test
    @DisplayName("the altitude field needs a vertical datum and a length unit; depth (free-text datum) is not one")
    void altitudeField() {
        assertThat(KmlService.altitudeField(PLANES, "aircraft")).isEqualTo("alt_baro");
        assertThat(KmlService.altitudeField(QUAKES, "quake")).isNull();
        assertThat(KmlService.altitudeField(null, "x")).isNull();
        assertThat(KmlService.metersPerUnit("km")).isEqualTo(1000);
        assertThat(KmlService.metersPerUnit("deg")).isZero();
        assertThat(KmlService.metersPerUnit(null)).isZero();
    }

    @Test
    @DisplayName("geofences are simplified until they have at most the vertex limit")
    void simplify() {
        GeometryFactory f = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate[] ring = new Coordinate[20_001];
        for (int i = 0; i < 20_000; i++) {
            double a = 2 * Math.PI * i / 20_000;
            ring[i] = new Coordinate(10 * Math.cos(a), 10 * Math.sin(a));
        }
        ring[20_000] = ring[0];
        Geometry circle = f.createPolygon(ring);
        List<Geometry> simplified = KmlService.simplify(List.of(circle), KmlService.MAX_FENCE_VERTICES);
        assertThat(simplified.getFirst().getNumPoints()).isLessThanOrEqualTo(KmlService.MAX_FENCE_VERTICES);
        assertThat(simplified.getFirst().isValid()).isTrue();
        assertThat(KmlService.simplify(List.of(f.createPoint(new Coordinate(0, 0))), 10).getFirst().getNumPoints())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("fields: tile properties first, else field specs; scalar values only")
    void fields() {
        assertThat(KmlService.fields(QUAKES, "quake")).containsExactly("mag", "place");
        SourceDescriptor noTiles = SourceDescriptor.builder("kml-x").kind("k").mode(Mode.PUSH)
                .defaultBudget(Budget.of(1, 1)).fieldSpec("a", FieldSpec.of(FieldType.NUMBER, null)).build();
        assertThat(KmlService.fields(noTiles, "k")).containsExactly("a");
        assertThat(KmlService.valueText(JSON.readTree("{\"o\":{}}").get("o"))).isNull();
        assertThat(KmlService.valueText(JSON.readTree("4.5"))).isEqualTo("4.5");
        assertThat(KmlService.valueText(null)).isNull();
    }

    @Test
    @DisplayName("the NetworkLink document points every layer at records.kml, keyed when the request was")
    void networkLinks() {
        String xml = new String(service.networkLinks(List.of("kml-quakes"), 60,
                new PublicUrls("https://fs.example", "/v1/k/fsf_abcdefghijklmnop")).body(), StandardCharsets.UTF_8);
        assertThat(xml).contains("<href>https://fs.example/v1/k/fsf_abcdefghijklmnop/kml/records.kml?layer=kml-quakes</href>");
    }
}
