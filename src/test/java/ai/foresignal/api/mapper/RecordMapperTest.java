package ai.foresignal.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.api.support.RawJson;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class RecordMapperTest {

    private static final Instant T = Instant.parse("2026-03-01T12:00:00Z");
    private static final String POINT = "{\"type\":\"Point\",\"coordinates\":[1.5,2.5]}";

    private final SourceRegistry registry = mock(SourceRegistry.class);
    private final JsonMapper json = JsonMapper.builder().build();
    private RecordMapper mapper;

    @BeforeEach
    void setUp() {
        SourceDescriptor descriptor = SourceDescriptor.builder("src").name("Src").kind("quake").mode(Mode.PUSH)
                .attribution("USGS").titleField("label").build();
        when(registry.find("src")).thenReturn(Optional.of(new SourceRegistry.Registration(descriptor, null, false)));
        when(registry.find("gone")).thenReturn(Optional.empty());
        mapper = new RecordMapper(registry, json);
    }

    private static RecordRow row(String source, String title, String payload, String titlePayload, Boolean live) {
        return new RecordRow(source, "e1", "quake", 3, 42L, T, T.minusSeconds(5), T.minusSeconds(60), null, T.plusSeconds(600),
                false, title, POINT, payload, titlePayload, "{\"confidence\":0.5}", live, 1, null, null,
                T.plusSeconds(1), T, (short) 1, null, (short) 0, 7L, new double[] {1.5, 2.5, 1.5, 2.5}, null);
    }

    @Test
    void currentRecordCarriesTheSpecFields() {
        Map<String, Object> item = mapper.record(row("src", "Head title", "{\"mag\":4.5}", null, true),
                FieldSelection.ALL, RecordMapper.Shape.CURRENT);
        assertThat(item).containsEntry("id", "src:e1").containsEntry("source", "src").containsEntry("externalId", "e1")
                .containsEntry("title", "Head title").containsEntry("version", 3).containsEntry("seq", 42L)
                .containsEntry("live", true).containsEntry("qaFlags", 1).containsEntry("attribution", "USGS")
                .containsEntry("validFrom", null).doesNotContainKeys("ingestedAt", "persistReason", "distanceKm", "observer");
        assertThat(item.get("geometry")).isEqualTo(RawJson.of(POINT));
        assertThat(item.get("payload")).isEqualTo(RawJson.of("{\"mag\":4.5}"));
    }

    @Test
    @DisplayName("a version's title is resolved from its payload with the source's titleField")
    void versionTitleFromPayload() {
        Map<String, Object> item = mapper.record(row("src", null, "{\"label\":\" Big one \",\"name\":\"x\"}", null, null),
                FieldSelection.ALL, RecordMapper.Shape.VERSION);
        assertThat(item).containsEntry("title", "Big one").containsEntry("persistReason", "FIRST")
                .containsEntry("historyTier", "STANDARD").containsEntry("batchId", 7L).containsEntry("downsampled", false)
                .containsEntry("live", false).containsKey("ingestedAt").containsKey("receivedAt");
    }

    @Test
    void titleFallsBackWhenTheSourceIsGone() {
        Map<String, Object> item = mapper.record(row("gone", null, "{\"name\":\"N\"}", null, null),
                FieldSelection.ALL, RecordMapper.Shape.AS_OF);
        assertThat(item).containsEntry("title", "N").containsEntry("attribution", null).containsKey("ingestedAt")
                .doesNotContainKey("persistReason");
    }

    @Test
    @DisplayName("fields= keeps only the selected properties and nests payload paths")
    void projection() {
        FieldSelection fields = FieldSelector.parse("externalId,title,payload.mag,payload.a.b");
        Map<String, Object> item = mapper.record(
                row("src", null, "{\"mag\":4.5,\"a.b\":\"deep\"}", "{\"label\":\"L\"}", null), fields,
                RecordMapper.Shape.VERSION);
        assertThat(item).containsOnlyKeys("externalId", "title", "payload").containsEntry("title", "L");
        JsonNode payload = (JsonNode) item.get("payload");
        assertThat(payload.get("mag").doubleValue()).isEqualTo(4.5);
        assertThat(payload.get("a").get("b").asString()).isEqualTo("deep");
    }

    @Test
    void featureProperties() {
        Map<String, Object> feature = mapper.feature(row("src", "T", "{}", null, false), FieldSelection.ALL,
                RecordMapper.Shape.CURRENT);
        assertThat(feature).containsEntry("type", "Feature").containsEntry("id", "src:e1");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
        assertThat(properties).containsOnlyKeys(
                "source", "externalId", "kind", "title", "version", "seq", "observedAt", "validUntil", "live", "payload");
        Map<String, Object> noGeometry = mapper.feature(row("src", "T", "{}", null, false), FieldSelector.parse("title"),
                RecordMapper.Shape.CURRENT);
        assertThat(noGeometry.get("geometry")).isNull();
    }

    @Test
    void changeCarriesFeedFields() {
        Map<String, Object> change = mapper.change(row("src", null, null, null, null), FieldSelection.ALL);
        assertThat(change).containsEntry("seq", 42L).containsEntry("persistReason", "FIRST")
                .containsEntry("historyTier", "STANDARD").containsEntry("bbox", List.of(1.5, 2.5, 1.5, 2.5))
                .containsKey("validUntil").containsKey("prevSeq").doesNotContainKey("payload");
        Map<String, Object> feature = mapper.changeFeature(row("src", null, "{\"x\":1}", null, null), FieldSelection.ALL);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
        assertThat(properties).containsKey("payload").doesNotContainKey("geometry");
    }

    @Test
    void csvColumnsAndRows() {
        assertThat(RecordMapper.csvColumns(FieldSelection.ALL, RecordMapper.DEFAULT_CSV_COLUMNS))
                .isEqualTo(RecordMapper.DEFAULT_CSV_COLUMNS);
        FieldSelection fields = FieldSelector.parse("payload.mag,geometry,externalId,quality");
        List<String> columns = RecordMapper.csvColumns(fields, RecordMapper.DEFAULT_CSV_COLUMNS);
        assertThat(columns).containsExactly("externalId", "quality", "geometry", "payload.mag");
        RecordRow wkt = new RecordRow("src", "e1", "quake", 1, 1L, T, T, null, null, null, false, "t", "POINT(1 2)",
                "{\"mag\":4.5}", null, "{\"confidence\":0.5}", false, 0, null, null, null, null, null, null, null, null,
                null, null);
        Map<String, Object> csv = mapper.csvRow(mapper.record(wkt, fields, RecordMapper.Shape.CURRENT), columns);
        assertThat(csv).containsEntry("externalId", "e1").containsEntry("geometry", "POINT(1 2)")
                .containsEntry("payload.mag", "4.5").containsEntry("quality", "{\"confidence\":0.5}");
    }
}
