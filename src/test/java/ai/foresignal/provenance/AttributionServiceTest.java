package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AttributionService} (prompt 109): the {@code Foresignal-Attribution} value is printable ISO-8859-1 and
 * truncated with {@code "..."}, and a layer's aggregated attribution falls back to the license's attribution text
 * for a source without an attribution line (the TileJSON aggregate).
 */
class AttributionServiceTest {

    static AttributionService service(Map<String, SourceDescriptor> all) {
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
        when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));
        when(registry.descriptors()).thenReturn(List.copyOf(all.values()));
        return new AttributionService(registry, new LicenceCatalog());
    }

    @Test
    @DisplayName("the header is ISO-8859-1 safe: dashes, quotes and the ellipsis become ASCII, others '?'")
    void latin1() {
        assertThat(AttributionService.latin1("Datenlizenz Deutschland – Namensnennung “2.0”…"))
                .isEqualTo("Datenlizenz Deutschland - Namensnennung \"2.0\"...");
        assertThat(AttributionService.latin1("café 東京\tline")).isEqualTo("café ?? line");
    }

    @Test
    @DisplayName("over 1 KB the header is cut with \"...\" and every character encodes in ISO-8859-1")
    void truncation() {
        AttributionService service = service(Map.of());
        String longText = "x".repeat(700);
        List<AttributionService.Attribution> attributions = List.of(
                new AttributionService.Attribution("a", "CC-BY-4.0", longText + "—a", null, true, 1),
                new AttributionService.Attribution("b", "CC-BY-4.0", longText + "—b", null, true, 1));
        String header = service.header(attributions);
        assertThat(header).hasSize(AttributionService.MAX_TEXT).endsWith("...");
        assertThat(StandardCharsets.ISO_8859_1.newEncoder().canEncode(header)).isTrue();
    }

    @Test
    @DisplayName("a layer's attribution aggregates attribution lines, else each license's attribution text")
    void layerText() {
        SourceDescriptor credited = SourceDescriptor.builder("credited").name("c").kind("k").mode(Mode.PUSH)
                .attribution("Data: Example Agency").licence("US-PD").build();
        SourceDescriptor bare = SourceDescriptor.builder("bare").name("b").kind("k").mode(Mode.PUSH)
                .licence("ODbL 1.0").build();
        AttributionService service = service(Map.of("credited", credited, "bare", bare));
        String text = service.layerText(List.of(credited, bare));
        assertThat(text).contains("Data: Example Agency").contains("Open Database License");
        assertThat(service.layerText(List.of())).isNull();
    }

    @Test
    @DisplayName("every attribution map carries requiredDisplay from its license (OpenAPI Attribution requires it)")
    void requiredDisplay() {
        SourceDescriptor open = SourceDescriptor.builder("open").name("o").kind("k").mode(Mode.PUSH)
                .licence("ODbL 1.0").build();
        SourceDescriptor publicDomain = SourceDescriptor.builder("pd").name("p").kind("k").mode(Mode.PUSH)
                .licence("US-PD").build();
        AttributionService service = service(Map.of("open", open, "pd", publicDomain));
        Map<String, Boolean> shown = new java.util.HashMap<>();
        for (AttributionService.Attribution a : service.scope(null, null)) {
            assertThat(a.toMap()).containsKey("requiredDisplay");
            shown.put(a.sourceId(), (Boolean) a.toMap().get("requiredDisplay"));
        }
        assertThat(shown).containsEntry("open", true).containsEntry("pd", false);
    }
}
