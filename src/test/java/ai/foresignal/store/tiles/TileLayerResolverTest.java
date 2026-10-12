package ai.foresignal.store.tiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.SqlParams;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** {@link TileLayerResolver}: source vs kind, property union and validation, aggBelowZoom, privacy. */
class TileLayerResolverTest {

    private static SourceDescriptor.Builder source(String id, String... kinds) {
        return SourceDescriptor.builder(id).name("Name " + id).kinds(kinds).mode(Mode.PUSH);
    }

    private static final SourceDescriptor QUAKES = source("quakes", "earthquake")
            .description("Quakes")
            .attribution("USGS")
            .tileProperties("mag", "place", "bad-name", "with.dot", "source")
            .rankField("mag")
            .build();

    private static final SourceDescriptor EMSC = source("emsc", "earthquake", "felt-report")
            .attribution("EMSC")
            .tileProperties("depth", "mag")
            .aggBelowZoom(7)
            .rankField("magnitude")
            .build();

    private static final SourceDescriptor FIRES = source("fires", "fire")
            .tileProperties("frp")
            .aggBelowZoom(9)
            .build();

    private static final SourceDescriptor FIRES_2 = source("fires-2", "fire")
            .aggBelowZoom(4)
            .attribution("USGS")
            .build();

    private static final SourceDescriptor SECRET = source("secret", "fire", "earthquake")
            .privateSource(true)
            .tileProperties("owner")
            .aggBelowZoom(2)
            .build();

    private static final SourceDescriptor CLASH = source("clash", "quakes").build();

    private static final List<SourceDescriptor> ALL = List.of(QUAKES, EMSC, FIRES, FIRES_2, SECRET, CLASH);

    private static TileLayer resolve(String layer, Set<String> excluded) {
        return TileLayerResolver.resolve(layer, ALL, excluded).orElseThrow();
    }

    @Test
    @DisplayName("a source id resolves to that source and filters by source_id")
    void sourceLayer() {
        TileLayer layer = resolve("quakes", Set.of());
        assertThat(layer.type()).isEqualTo(TileLayerType.SOURCE);
        assertThat(layer.sourceIds()).containsExactly("quakes");
        assertThat(layer.name()).isEqualTo("Name quakes");
        assertThat(layer.description()).isEqualTo("Quakes");
        SqlParams p = new SqlParams();
        assertThat(layer.filterSql(p)).isEqualTo("h.source_id = CAST(:p0 AS text)");
        assertThat(p.source().getValue("p0")).isEqualTo("quakes");
    }

    @Test
    @DisplayName("a source id wins over a kind of the same name")
    void sourceIdWins() {
        TileLayer layer = resolve("quakes", Set.of());
        assertThat(layer.type()).isEqualTo(TileLayerType.SOURCE);
        assertThat(layer.sourceIds()).doesNotContain("clash");
    }

    @Test
    @DisplayName("a kind resolves to every visible source declaring it; the filter binds kind and sources")
    void kindLayer() {
        TileLayer layer = resolve("earthquake", Set.of("secret"));
        assertThat(layer.type()).isEqualTo(TileLayerType.KIND);
        assertThat(layer.sourceIds()).containsExactly("emsc", "quakes");
        assertThat(layer.name()).isEqualTo("earthquake");
        SqlParams p = new SqlParams();
        assertThat(layer.filterSql(p))
                .isEqualTo("(h.kind = CAST(:p0 AS text) AND h.source_id = ANY(CAST(:p1 AS text[])))");
        assertThat(p.source().getValue("p0")).isEqualTo("earthquake");
        assertThat(p.source().getValue("p1")).isEqualTo("{\"emsc\",\"quakes\"}");
    }

    @Test
    @DisplayName("properties: the base list, then the union of tile properties in first-seen order")
    void propertyUnion() {
        TileLayer layer = resolve("earthquake", Set.of("secret"));
        assertThat(layer.tileProperties()).containsExactly("depth", "mag", "place");
        assertThat(layer.properties()).containsExactly(
                "source", "externalId", "kind", "title", "observedAt", "version", "live", "depth", "mag", "place");
    }

    @Test
    @DisplayName("invalid property names are rejected; base names are not duplicated")
    void invalidPropertiesRejected() {
        TileLayer layer = resolve("quakes", Set.of());
        assertThat(layer.tileProperties()).containsExactly("mag", "place");
        assertThat(TileLayerResolver.validProperty("ok_Name1")).isTrue();
        assertThat(TileLayerResolver.validProperty("bad-name")).isFalse();
        assertThat(TileLayerResolver.validProperty("a'b")).isFalse();
        assertThat(TileLayerResolver.validProperty("x".repeat(65))).isFalse();
        assertThat(TileLayerResolver.validProperty("")).isFalse();
        assertThat(TileLayerResolver.validProperty(null)).isFalse();
    }

    @Test
    @DisplayName("an unknown layer is empty, and resolve() through the bean throws 404 UNKNOWN_LAYER")
    void unknownLayer() {
        assertThat(TileLayerResolver.resolve("nope", ALL, Set.of())).isEmpty();
        assertThat(TileLayerResolver.resolve("", ALL, Set.of())).isEmpty();
        assertThat(TileLayerResolver.resolve(null, ALL, Set.of())).isEmpty();
        ForesignalException e = TileLayerResolver.unknownLayer("nope");
        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(e.getCode()).isEqualTo(ErrorCodes.UNKNOWN_LAYER);
    }

    @Test
    @DisplayName("aggBelowZoom is the minimum over the sources' non-null values, and null when none declares one")
    void aggBelowZoom() {
        assertThat(resolve("fire", Set.of("secret")).aggBelowZoom()).isEqualTo(4);
        assertThat(resolve("fire", Set.of()).aggBelowZoom()).isEqualTo(2);
        assertThat(resolve("earthquake", Set.of("secret")).aggBelowZoom()).isEqualTo(7);
        assertThat(resolve("quakes", Set.of()).aggBelowZoom()).isNull();
    }

    @Test
    @DisplayName("excluded sources are removed first: a private source layer and a kind with only private sources are unknown")
    void privateExcluded() {
        assertThat(TileLayerResolver.resolve("secret", ALL, Set.of("secret"))).isEmpty();
        assertThat(resolve("secret", Set.of()).sourceIds()).containsExactly("secret");
        TileLayer fire = resolve("fire", Set.of("secret"));
        assertThat(fire.sourceIds()).containsExactly("fires", "fires-2");
        assertThat(fire.tileProperties()).doesNotContain("owner");
        List<SourceDescriptor> onlyPrivate = List.of(SECRET);
        assertThat(TileLayerResolver.resolve("fire", onlyPrivate, Set.of("secret"))).isEmpty();
    }

    @Test
    @DisplayName("maxRank only when every source declares a numeric rank field")
    void rankFields() {
        assertThat(resolve("earthquake", Set.of("secret")).rankFields())
                .containsEntry("quakes", "mag")
                .containsEntry("emsc", "magnitude");
        assertThat(resolve("earthquake", Set.of("secret")).rankable()).isTrue();
        assertThat(resolve("fire", Set.of("secret")).rankable()).isFalse();
        SourceDescriptor textRank = source("text-rank", "x")
                .rankField("label")
                .fieldSpec("label", new FieldSpec(FieldType.STRING, null, null, null, null, Set.of()))
                .build();
        assertThat(TileLayerResolver.numericRankField(textRank)).isNull();
        SourceDescriptor intRank = source("int-rank", "x")
                .rankField("n")
                .fieldSpec("n", new FieldSpec(FieldType.INTEGER, null, null, null, null, Set.of()))
                .build();
        assertThat(TileLayerResolver.numericRankField(intRank)).isEqualTo("n");
        assertThat(TileLayerResolver.numericRankField(source("bad", "x").rankField("a-b").build())).isNull();
    }

    @Test
    @DisplayName("attribution joins the distinct descriptor attributions with a middle dot")
    void attribution() {
        assertThat(resolve("earthquake", Set.of("secret")).attribution()).isEqualTo("EMSC · USGS");
        assertThat(resolve("fire", Set.of("secret")).attribution()).isEqualTo("USGS");
        assertThat(resolve("clash", Set.of()).attribution()).isNull();
    }

    @Test
    @DisplayName("the index lists visible source ids, then visible kinds, skipping a kind spelled like a source id")
    void index() {
        List<String> anonymous = TileLayerResolver.layers(ALL, Set.of("secret")).stream()
                .map(l -> l.type().wireValue() + ":" + l.id()).toList();
        assertThat(anonymous).containsExactly(
                "source:clash", "source:emsc", "source:fires", "source:fires-2", "source:quakes",
                "kind:earthquake", "kind:felt-report", "kind:fire");
        List<String> withToken = TileLayerResolver.layers(ALL, Set.of()).stream().map(TileLayer::id).toList();
        assertThat(withToken).contains("secret");
    }

    @Test
    @DisplayName("the SQL builders refuse an unvalidated name")
    void sqlBuildersValidate() {
        TileLayer forged = new TileLayer("x", TileLayerType.SOURCE, "x", "x", List.of(QUAKES),
                List.of("ok", "bad'name"), null, java.util.Map.of());
        assertThatThrownBy(() -> TileQueryService.rawProperties(forged)).isInstanceOf(IllegalArgumentException.class);
        TileLayer fine = resolve("quakes", Set.of());
        assertThat(TileQueryService.rawProperties(fine))
                .contains("'mag', mp.m -> 'mag'")
                .contains("'live', l.dirty")
                .startsWith("jsonb_strip_nulls(");
        assertThat(TileQueryService.rankExpression(fine, new SqlParams())).contains("jsonb_typeof(mp.m -> 'mag')");
        assertThat(TileQueryService.rankExpression(resolve("fire", Set.of("secret")), new SqlParams()))
                .isEqualTo("CAST(NULL AS double precision)");
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("a kind every declaring source hides (EDP atomic, 024) is left out of the index but still resolves")
    void hiddenKindsStayOutOfTheIndex() {
        SourceDescriptor ext = source("ext-harbor", "harbor-camera", "harbor-note").hiddenKind("harbor-note").build();
        SourceDescriptor other = source("notes-too", "shared-kind").build();
        SourceDescriptor hider = source("hider", "shared-kind").hiddenKind("shared-kind").build();
        List<SourceDescriptor> all = List.of(ext, other, hider);

        List<String> ids = TileLayerResolver.layers(all, Set.of()).stream().map(TileLayer::id).toList();

        org.assertj.core.api.Assertions.assertThat(ids).contains("harbor-camera", "shared-kind").doesNotContain("harbor-note");
        org.assertj.core.api.Assertions.assertThat(TileLayerResolver.resolve("harbor-note", all, Set.of())).isPresent();
    }
}
