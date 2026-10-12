package ai.foresignal.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.integrations.IntegrationDocs;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Every metric the Grafana "Foresignal Operations" dashboard (prompt 034) queries is real: scraped
 * from {@code /actuator/prometheus} of a running application, or, for the meters that are only
 * registered after activity (a source fetching or ingesting), defined under that Micrometer name in
 * the production code.
 */
@WebIntegrationTest
class GrafanaMetricNamesIT {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final Pattern METRIC = Pattern.compile("\\b((?:foresignal|jvm|process|http)_[a-z0-9_]+)\\b");

    /**
     * Series that exist only after activity: Prometheus name → (Micrometer name, the production file
     * that registers it). Everything else in the dashboard must be in a fresh scrape.
     */
    static final Map<String, List<String>> AFTER_ACTIVITY = Map.of(
            "foresignal_ingest_records_persisted_total",
            List.of("foresignal.ingest.records.persisted", "ingest/IngestWriter.java"),
            "foresignal_ingest_records_seen_total",
            List.of("foresignal.ingest.records.seen", "ingest/IngestWriter.java"),
            "foresignal_ingest_records_rejected_total",
            List.of("foresignal.ingest.records.rejected", "ingest/IngestWriter.java"),
            "foresignal_fetch_duration_seconds_sum", List.of("foresignal.fetch.duration", "collect/FetchExecutor.java"),
            "foresignal_fetch_duration_seconds_count", List.of("foresignal.fetch.duration", "collect/FetchExecutor.java"),
            "foresignal_source_status", List.of("foresignal.source.status", "collect/HealthService.java"),
            "foresignal_source_data_age_seconds",
            List.of("foresignal.source.data_age_seconds", "system/OperatorGauges.java"));

    @LocalServerPort int port;

    @Test
    @DisplayName("every metric of the Operations dashboard is scraped, or is an activity-only meter defined in code")
    void metricNamesExist() throws Exception {
        Set<String> used = dashboardMetrics();
        assertThat(used).as("metrics in the dashboard (non-vacuous)").hasSizeGreaterThanOrEqualTo(10)
                .containsAll(AFTER_ACTIVITY.keySet());
        String scrape = scrape();
        List<String> missing = new ArrayList<>();
        for (String name : used) {
            List<String> definition = AFTER_ACTIVITY.get(name);
            if (definition != null) {
                String source = IntegrationDocs.read(Path.of("src", "main", "java", "ai", "foresignal", definition.get(1)));
                if (!source.contains("\"" + definition.get(0) + "\"")) {
                    missing.add(name + " (expected \"" + definition.get(0) + "\" in " + definition.get(1) + ")");
                }
            } else if (!Pattern.compile("(?m)^" + Pattern.quote(name) + "[{ ]").matcher(scrape).find()) {
                missing.add(name + " (not in /actuator/prometheus)");
            }
        }
        assertThat(missing).isEmpty();
    }

    @Test
    @DisplayName("the metric extraction reads expressions and skips label names and functions")
    void extraction() {
        Set<String> names = metrics("sum by (phase) (rate(foresignal_ingest_batch_duration_seconds_sum[5m])) / "
                + "foresignal_sse_clients{stream=\"all\"} + jvm_memory_used_bytes{area=\"heap\"}");
        assertThat(names).containsExactlyInAnyOrder("foresignal_ingest_batch_duration_seconds_sum",
                "foresignal_sse_clients", "jvm_memory_used_bytes");
    }

    private String scrape() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/prometheus"))
                .timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return response.body();
    }

    /** The metric names of every {@code expr} in the Operations dashboard. */
    static Set<String> dashboardMetrics() {
        JsonNode dashboard = JSON.readTree(IntegrationDocs.read(IntegrationDocs.OPERATIONS_DASHBOARD));
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode panel : dashboard.path("panels")) {
            for (JsonNode target : panel.path("targets")) {
                out.addAll(metrics(target.has("expr") ? target.get("expr").asString() : ""));
            }
        }
        return out;
    }

    static Set<String> metrics(String expression) {
        Set<String> out = new LinkedHashSet<>();
        Matcher m = METRIC.matcher(expression);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }
}
