package ai.foresignal.testsupport.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** MockMvc helpers of the briefing HTTP tests (prompt 029): a request, its status asserted, its body read. */
public final class BriefingHttp {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final MockMvc mvc;

    /** Wraps {@code mvc}. */
    public BriefingHttp(MockMvc mvc) {
        this.mvc = mvc;
    }

    /** A request with an optional bearer and JSON body. */
    public static MockHttpServletRequestBuilder request(String method, String path, String bearer, String body) {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, java.net.URI.create(path));
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return request;
    }

    /** Performs {@code request} and asserts {@code expectedStatus}. */
    public MvcResult perform(MockHttpServletRequestBuilder request, int expectedStatus) throws Exception {
        MvcResult result = mvc.perform(request).andReturn();
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(result.getResponse().getStatus()).as(content).isEqualTo(expectedStatus);
        return result;
    }

    /** Performs a JSON call and returns the parsed body (null when empty). */
    public JsonNode call(String method, String path, String bearer, String body, int expectedStatus) throws Exception {
        String content = perform(request(method, path, bearer, body), expectedStatus).getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return content.isEmpty() ? null : JSON.readTree(content);
    }
}
