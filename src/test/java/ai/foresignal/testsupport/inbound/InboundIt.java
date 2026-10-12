package ai.foresignal.testsupport.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared steps of the inbound integration tests (prompt 024): create custom sources through the
 * API and push into them. Every source id is unique within the run, because the request and
 * record buckets are kept per source id for the life of the cached context.
 */
public final class InboundIt {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    /** Shared mapper. */
    public static final JsonMapper JSON = JsonMapper.builder().build();

    private final MockMvc mvc;
    private final String admin;

    /** Steps against {@code mvc} with the admin bearer value {@code admin}. */
    public InboundIt(MockMvc mvc, String admin) {
        this.mvc = mvc;
        this.admin = admin;
    }

    /** A fresh {@code custom-…} id. */
    public static String newId(String stem) {
        return "custom-" + stem + "-" + SEQUENCE.incrementAndGet() + "-" + Long.toString(System.nanoTime() % 1_000_000, 36);
    }

    /**
     * A created source.
     *
     * @param id its id
     * @param key its ingest key
     * @param body the create response
     */
    public record Created(String id, String key, JsonNode body) {}

    /** Creates a custom source from a JSON object (single quotes allowed) whose id is set here. */
    public Created create(String id, String json) throws Exception {
        String body = json.replace('\'', '"').replaceFirst("\\{", "{\"id\":\"" + id + "\",");
        MockHttpServletResponse response = perform(post("/v1/custom-sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        JsonNode created = JSON.readTree(response.getContentAsString());
        return new Created(id, created.get("ingestKey").asString(), created);
    }

    /** Performs a request and returns its response. */
    public MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    /** The JSON body of {@code response}. */
    public static JsonNode body(MockHttpServletResponse response) throws Exception {
        return JSON.readTree(response.getContentAsString());
    }

    /** POSTs {@code json} to {@code path} with the ingest key header. */
    public MockHttpServletResponse push(String path, String key, MediaType type, String json) throws Exception {
        return perform(post(path).header("X-Foresignal-Ingest-Key", key).contentType(type).content(json));
    }

    /** A GeoJSON FeatureCollection of one point feature. */
    public static String feature(String id, String kind, String observedAt, double value) {
        return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"id\":\"" + id + "\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-97.74,30.27]},\"properties\":{"
                + (kind == null ? "" : "\"kind\":\"" + kind + "\",")
                + (observedAt == null ? "" : "\"observedAt\":\"" + observedAt + "\",")
                + "\"name\":\"" + id + "\",\"value\":" + value + "}}]}";
    }
}
