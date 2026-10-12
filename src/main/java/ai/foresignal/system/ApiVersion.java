package ai.foresignal.system;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The API contract version: {@code info.version} of the authored OpenAPI document, which the build
 * copies onto the classpath as {@value #RESOURCE} (prompt 020; {@code OpenApiController} serves the
 * same resource at {@code /v1/openapi.json}). Read once at startup;
 * {@value #UNKNOWN} when the document is missing (a test classpath without resources).
 */
@Component
public class ApiVersion {

    private static final Logger log = LoggerFactory.getLogger(ApiVersion.class);

    /** Reported when the document cannot be read. */
    public static final String UNKNOWN = "unknown";

    /**
     * Where the build puts the authored document: outside {@code static/}, so the only route to it
     * is the documented {@code getOpenApiDocument} operation.
     */
    public static final String RESOURCE = "openapi/openapi.json";

    private final String version;

    /** Reads the version. */
    public ApiVersion(JsonMapper json) {
        this.version = read(json);
    }

    private static String read(JsonMapper json) {
        ClassPathResource resource = new ClassPathResource(RESOURCE);
        if (!resource.exists()) {
            return UNKNOWN;
        }
        try (InputStream in = resource.getInputStream()) {
            JsonNode info = json.readTree(in).get("info");
            JsonNode version = info == null ? null : info.get("version");
            return version == null || !version.isString() ? UNKNOWN : version.asString();
        } catch (IOException | RuntimeException e) {
            log.warn("Could not read the API version from {}", RESOURCE, e);
            return UNKNOWN;
        }
    }

    /** For example {@code 1.3.0}. */
    public String version() {
        return version;
    }
}
