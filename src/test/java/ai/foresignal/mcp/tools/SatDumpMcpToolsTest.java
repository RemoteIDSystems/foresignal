package ai.foresignal.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaService;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code list_satellite_passes} and {@code get_satellite_pass} (prompt 049, Weather-Imagery-Design §6.9):
 * read scope, the untrusted-content note, the station and status filters, the limit, compact output, pass
 * ids in both forms, and the 256 px thumbnail as image content with a caption. {@code McpServerIT} applies
 * the schema well-formedness check to every registered tool, these two included.
 */
class SatDumpMcpToolsTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant AOS = Instant.parse("2026-10-12T18:02:10Z");

    @TempDir Path temp;

    RecordQueryService queries;
    RecordItems items;
    SourceRegistry registry;
    MediaService media;
    SatellitePasses passes;
    McpOutputShaper shaper;
    ListSatellitePassesTool list;
    GetSatellitePassTool get;

    @BeforeEach
    void setUp() {
        queries = mock(RecordQueryService.class);
        items = mock(RecordItems.class);
        registry = mock(SourceRegistry.class);
        media = mock(MediaService.class);
        when(registry.ids()).thenReturn(Set.of("satdump-home", "satdump-pi", "usgs-earthquakes"));
        when(registry.contains("satdump-home")).thenReturn(true);
        shaper = new McpOutputShaper(JSON, registry);
        passes = new SatellitePasses(registry, JSON);
        list = new ListSatellitePassesTool(queries, items, shaper, passes);
        get = new GetSatellitePassTool(queries, items, shaper, passes, media);
        when(items.items(anyList(), any(), any())).thenAnswer(inv -> {
            List<RecordRow> rows = inv.getArgument(0);
            List<ObjectNode> out = new ArrayList<>();
            for (RecordRow row : rows) {
                out.add(item(row));
            }
            return out;
        });
        when(items.item(any(), any(), any())).thenAnswer(inv -> item(inv.getArgument(0)));
    }

    static ObjectNode item(RecordRow row) {
        ObjectNode node = JSON.createObjectNode();
        node.put("id", row.id());
        node.put("title", row.title());
        node.set("payload", JSON.readTree(row.payload()));
        return node;
    }

    static RecordRow row(String source, String externalId, String status, Instant aos, String thumb) {
        String products = thumb == null ? "[]"
                : "[{\"instrument\":\"msu_mr\",\"composite\":\"rgb.png\",\"thumbMediaId\":\"" + thumb + "\"}]";
        String payload = "{\"status\":\"" + status + "\",\"aos\":\"" + aos + "\",\"title\":\"Meteor-M2-3 pass\","
                + "\"products\":" + products + "}";
        return new RecordRow(source, externalId, "satellite-pass", 1, 1, aos, aos, aos, null, null, false,
                "Meteor-M2-3 pass, max el 54°", null, payload, null, null, null, 0, null, null, aos, aos, null, null,
                null, null, null, null);
    }

    static ToolContext anonymous() {
        return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1"), "s", "t");
    }

    @Test
    @DisplayName("both tools are read tools with well-formed schemas and the untrusted-content note")
    void shape() {
        for (ForesignalMcpTool tool : List.of(list, get)) {
            assertThat(tool.inputSchema()).containsEntry("type", "object").containsEntry("additionalProperties", false);
            assertThat(tool.outputSchema()).isNotNull();
            assertThat(tool.readOnlyHint()).isTrue();
            assertThat(tool.requiredScope()).isEqualTo(Scope.READ);
            assertThat(tool.description()).startsWith(ForesignalMcpTool.UNTRUSTED_NOTE);
        }
        assertThat(list.name()).isEqualTo("list_satellite_passes");
        assertThat(get.name()).isEqualTo("get_satellite_pass");
        assertThat(list.inputSchema().toString()).contains("station").contains("since").contains("status")
                .contains("limit").contains("detail");
    }

    @Test
    @DisplayName("every visible station by default, newest AOS first; status filters and limit caps")
    void listing() {
        RecordRow older = row("satdump-home", "home:meteor-m2-3:1", "decoded", AOS, null);
        RecordRow newer = row("satdump-pi", "pi:meteor-m2-4:2", "recorded", AOS.plusSeconds(3600), null);
        RecordRow failed = row("satdump-home", "home:metop-b:3", "failed", AOS.plusSeconds(7200), null);
        when(queries.list(any())).thenReturn(new Slices.RecordSlice(List.of(older, newer, failed), null, 9, null));

        ToolResult all = list.call(anonymous(), JSON.readTree("{}"));
        ArgumentCaptor<CurrentQuery> query = ArgumentCaptor.forClass(CurrentQuery.class);
        org.mockito.Mockito.verify(queries).list(query.capture());
        assertThat(query.getValue().sources()).containsExactly("satdump-home", "satdump-pi");
        assertThat(query.getValue().kinds()).containsExactly("satellite-pass");
        assertThat(all.structured().get("items")).extracting(n -> n.get("id").asString())
                .containsExactly("satdump-home:home:metop-b:3", "satdump-pi:pi:meteor-m2-4:2",
                        "satdump-home:home:meteor-m2-3:1");

        ToolResult decoded = list.call(anonymous(), JSON.readTree("{\"status\":\"decoded\"}"));
        assertThat(decoded.structured().get("items")).hasSize(1);
        ToolResult limited = list.call(anonymous(), JSON.readTree("{\"limit\":1}"));
        assertThat(limited.structured().get("items")).hasSize(1);
        assertThat(limited.structured().get("truncated").asBoolean()).isTrue();
        assertThat(McpOutputShaper.size(all.structured())).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
    }

    @Test
    @DisplayName("one station: only its source; an unknown station is not found")
    void station() {
        when(queries.list(any())).thenReturn(new Slices.RecordSlice(List.of(), null, 0, null));
        list.call(anonymous(), JSON.readTree("{\"station\":\"home\"}"));
        ArgumentCaptor<CurrentQuery> query = ArgumentCaptor.forClass(CurrentQuery.class);
        org.mockito.Mockito.verify(queries).list(query.capture());
        assertThat(query.getValue().sources()).containsExactly("satdump-home");
        assertThatThrownBy(() -> list.call(anonymous(), JSON.readTree("{\"station\":\"attic\"}")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("get_satellite_pass: the metadata plus the 256 px thumbnail as image content with a caption")
    void thumbnail() throws Exception {
        RecordRow pass = row("satdump-home", "home:meteor-m2-3:29863802", "decoded", AOS, "01JA0Z6Q9A1B2C3D4E5F6G7H8J");
        when(queries.get(eq("satdump-home"), eq("home:meteor-m2-3:29863802"), eq(false), any(), any(), any(), any()))
                .thenReturn(Optional.of(pass));
        Path file = Files.write(temp.resolve("thumb.jpg"), new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2});
        MediaRow thumb = new MediaRow("01JA0Z6Q9A1B2C3D4E5F6G7H8J", "sha", "image/jpeg", 256, 171, 5, "x", "thumbnail",
                "satdump-home", "satdump-home", "home:meteor-m2-3:29863802", "operator-owned", AOS, null, "01JA", 256);
        when(media.visible(eq("01JA0Z6Q9A1B2C3D4E5F6G7H8J"), any())).thenReturn(thumb);
        when(media.file(thumb)).thenReturn(file);

        ToolResult result = get.call(anonymous(), JSON.readTree("{\"id\":\"home:meteor-m2-3:29863802\"}"));
        assertThat(result.content()).hasSize(2);
        ToolResult.Image image = (ToolResult.Image) result.content().get(0);
        assertThat(image.mimeType()).isEqualTo("image/jpeg");
        assertThat(Base64.getDecoder().decode(image.data())).hasSize(5);
        ToolResult.Text caption = (ToolResult.Text) result.content().get(1);
        assertThat(caption.text()).contains("Meteor-M2-3 pass").contains("decoded").contains("rgb.png");
        assertThat(result.structured().get("id").asString()).isEqualTo("satdump-home:home:meteor-m2-3:29863802");

        // The item id form names the same pass.
        ToolResult byItemId = get.call(anonymous(),
                JSON.readTree("{\"id\":\"satdump-home:home:meteor-m2-3:29863802\"}"));
        assertThat(byItemId.content()).hasSize(2);
    }

    @Test
    @DisplayName("no thumbnail, or an expired one: metadata only; an unknown pass is a tool error")
    void noThumbnail() {
        RecordRow pass = row("satdump-home", "home:goes-19:1", "recorded", AOS, null);
        when(queries.get(eq("satdump-home"), eq("home:goes-19:1"), eq(false), any(), any(), any(), any()))
                .thenReturn(Optional.of(pass));
        ToolResult result = get.call(anonymous(), JSON.readTree("{\"id\":\"home:goes-19:1\"}"));
        assertThat(result.content()).singleElement().isInstanceOf(ToolResult.Text.class);

        RecordRow withThumb = row("satdump-home", "home:goes-19:2", "decoded", AOS, "01JA0Z6Q9A1B2C3D4E5F6G7H8K");
        when(queries.get(eq("satdump-home"), eq("home:goes-19:2"), eq(false), any(), any(), any(), any()))
                .thenReturn(Optional.of(withThumb));
        when(media.visible(anyString(), any())).thenThrow(new NotFoundException("expired"));
        assertThat(get.call(anonymous(), JSON.readTree("{\"id\":\"home:goes-19:2\"}")).content()).hasSize(1);

        when(queries.get(eq("satdump-home"), eq("home:nothing:3"), eq(false), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> get.call(anonymous(), JSON.readTree("{\"id\":\"home:nothing:3\"}")))
                .isInstanceOf(McpToolException.class);
        assertThatThrownBy(() -> SatellitePasses.split("nocolon")).isInstanceOf(NotFoundException.class);
        assertThat(SatellitePasses.split("satdump-pi:pi:metop-c:5")).containsExactly("satdump-pi", "pi:metop-c:5");
        assertThat(SatellitePasses.split("pi:metop-c:5")).containsExactly("satdump-pi", "pi:metop-c:5");
        assertThat(RecordMapper.Shape.CURRENT).isNotNull();
        assertThat(Map.of()).isEmpty();
    }
}
