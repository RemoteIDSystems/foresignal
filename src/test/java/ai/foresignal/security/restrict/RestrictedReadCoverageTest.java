package ai.foresignal.security.restrict;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.mcp.ForesignalMcpResource;
import ai.foresignal.mcp.ForesignalMcpTool;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Every controller method and MCP tool that returns records or alerts carries {@link RestrictedRead}
 * (prompt 035), so a new read path cannot forget {@code RestrictionClamp}.
 *
 * <p>A controller {@code GET} under one of {@link #READ_PREFIXES} must be annotated unless it is in
 * {@link #EXEMPT} (it returns no records or alerts). The operations of {@link #REQUIRED} must be
 * annotated wherever they live. MCP tools and resources are annotated unless their name is in
 * {@link #EXEMPT_TOOLS}.
 */
class RestrictedReadCoverageTest {

    /** Path prefixes whose GET operations return records or alerts. */
    static final List<String> READ_PREFIXES = List.of("/v1/records", "/v1/history", "/v1/changes", "/v1/kinds",
            "/v1/tiles", "/v1/events", "/v1/alerts", "/v1/search", "/v1/notifications", "/v1/digest", "/v1/briefings",
            "/v1/views", "/v1/timeline", "/v1/stats", "/v1/glance",
            // 093: series points and the series catalog are records (series-point, series-latest).
            "/v1/series",
            // 141: the FDSN event query, the STIX export and TAXII objects return earthquake and cyber records.
            "/fdsnws/event/1/query", "/v1/export", "/taxii2");

    /** GET operations under those prefixes that return no records or alerts. */
    static final Set<String> EXEMPT = Set.of("getSearchStatus", "getAlertEscalation", "listViews", "getView",
            // 141: TAXII discovery, the API root and collection descriptions carry no records.
            "getTaxiiDiscovery", "getTaxiiApiRoot", "listTaxiiCollections", "getTaxiiCollection",
            // Exports return pre-built artifacts filtered at export creation time, not live source-restricted records.
            "listExports", "downloadExport", "getExportManifest", "getExport", "downloadExportFile");

    /** Non-GET operations that read records or alerts on the caller's behalf. */
    static final Set<String> REQUIRED = Set.of("ask", "askStream", "previewBriefing", "runBriefing",
            "updateEventStreamFilter");

    /** MCP tools and resources that return no records or alerts. */
    static final Set<String> EXEMPT_TOOLS = Set.of("create_watch", "update_watch", "delete_watch", "list_watches",
            "mute_watch", "system_status", "list_escalation_policies", "create_briefing",
            // 036: places, view definitions and watchlists are not records.
            "list_places", "get_place", "create_place", "update_place", "delete_place", "PlacesResource",
            "list_views", "create_view", "list_watchlists", "get_watchlist", "add_to_watchlist",
            "remove_from_watchlist",
            // 039: an admin-only erasure that returns a purge report, never records.
            "purge_entity",
            // 040: region tags of a coordinate, never records.
            "reverse_geocode",
            // 052: product lists, model forecasts and area statistics are raster values, never records.
            "list_products", "weather_forecast", "area_weather_stats", "ProductsResource",
            // 112: creating a share link returns the link and its key, never records.
            "create_share_link",
            // 113: federation peers and their replication state, never records.
            "list_peers", "peer_status",
            // 117: attention topics are configuration, never records.
            "list_attention_topics",
            // 130: comment/share/tag are writes, run_social_cue is an agent action, whoami is identity; not records.
            "comment", "share_object", "tag_objects", "run_social_cue", "whoami");

    @Test
    @DisplayName("every record- or alert-returning controller method carries @RestrictedRead")
    void controllers() throws Exception {
        List<String> missing = new ArrayList<>();
        Set<String> seen = new TreeSet<>();
        for (Class<?> type : scan(new AnnotationTypeFilter(RestController.class))) {
            String base = "";
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
            if (mapping != null && mapping.path().length > 0) {
                base = mapping.path()[0];
            }
            for (Method method : type.getDeclaredMethods()) {
                Operation operation = method.getAnnotation(Operation.class);
                if (operation == null) {
                    continue;
                }
                String id = operation.operationId();
                seen.add(id);
                boolean annotated = method.isAnnotationPresent(RestrictedRead.class);
                if (REQUIRED.contains(id) && !annotated) {
                    missing.add(type.getSimpleName() + "." + method.getName() + " (" + id + ")");
                    continue;
                }
                GetMapping get = method.getAnnotation(GetMapping.class);
                if (get == null || EXEMPT.contains(id)) {
                    continue;
                }
                String[] paths = get.value().length > 0 ? get.value() : get.path();
                String path = base + (paths.length > 0 ? paths[0] : "");
                if (READ_PREFIXES.stream().anyMatch(path::startsWith) && !annotated) {
                    missing.add(type.getSimpleName() + "." + method.getName() + " (" + id + " " + path + ")");
                }
            }
        }
        assertThat(seen).as("the scan found the read controllers").contains("listRecords", "getTile", "listAlerts");
        assertThat(missing).as("read paths without @RestrictedRead").isEmpty();
    }

    @Test
    @DisplayName("every MCP tool and resource that returns records or alerts carries @RestrictedRead")
    void mcpTools() throws Exception {
        List<String> missing = new ArrayList<>();
        List<Class<?>> types = new ArrayList<>(scan(new AssignableTypeFilter(ForesignalMcpTool.class)));
        types.addAll(scan(new AssignableTypeFilter(ForesignalMcpResource.class)));
        int checked = 0;
        for (Class<?> type : types) {
            if (type.isInterface() || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            String name = toolName(type);
            if (EXEMPT_TOOLS.contains(name)) {
                continue;
            }
            checked++;
            if (!type.isAnnotationPresent(RestrictedRead.class)) {
                missing.add(type.getSimpleName() + " (" + name + ")");
            }
        }
        assertThat(checked).as("tools were found").isGreaterThan(10);
        assertThat(missing).as("MCP tools without @RestrictedRead").isEmpty();
    }

    /** The tool's name, read from a Mockito spy-free instance when possible, else the class name. */
    private static String toolName(Class<?> type) {
        if (ForesignalMcpTool.class.isAssignableFrom(type)) {
            try {
                Object tool = org.mockito.Mockito.mock(type, org.mockito.Mockito.CALLS_REAL_METHODS);
                return ((ForesignalMcpTool) tool).name();
            } catch (RuntimeException e) {
                return type.getSimpleName();
            }
        }
        return type.getSimpleName();
    }

    private static List<Class<?>> scan(org.springframework.core.type.filter.TypeFilter filter) throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(filter);
        List<Class<?>> out = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("ai.foresignal")) {
            Class<?> type = Class.forName(definition.getBeanClassName());
            if (type.getName().contains("Test") || type.getPackageName().contains("testsupport")) {
                continue;
            }
            out.add(type);
        }
        return out;
    }
}
