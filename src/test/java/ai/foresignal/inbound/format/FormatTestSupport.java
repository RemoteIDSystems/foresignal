package ai.foresignal.inbound.format;

import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Shared fixtures for the ingest format unit tests. */
final class FormatTestSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant NOW = Instant.parse("2026-10-12T08:00:00Z");

    private FormatTestSupport() {}

    /** A push source with {@code kinds}. */
    static SourceDescriptor source(String... kinds) {
        return SourceDescriptor.builder("custom-test").kinds(kinds).mode(Mode.PUSH).defaultBudget(60, 86_400).build();
    }

    /** A JSON request. */
    static IngestRequest json(String body) {
        return request(body, MediaType.APPLICATION_JSON, new HttpHeaders());
    }

    /** A request with a content type and headers. */
    static IngestRequest request(String body, MediaType type, HttpHeaders headers) {
        return new IngestRequest(body.getBytes(StandardCharsets.UTF_8), type, headers, Map.<String, List<String>>of(), NOW);
    }

    /** Options from a JSON object. */
    static FormatOptions options(String json) {
        JsonNode node = JSON.readTree(json);
        return new FormatOptions(node);
    }

    /** The shared GeoJSON mapper. */
    static GeoJsonIngestMapper mapper() {
        return new GeoJsonIngestMapper(JSON);
    }
}
