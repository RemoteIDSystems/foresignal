package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.system.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getOpenApiDocument} (prompt 020): serves the authored contract {@code docs/OpenAPI.json}
 * byte for byte at {@code /v1/openapi.json}.
 *
 * <p>The build copies the document onto the classpath as {@link ApiVersion#RESOURCE}, outside
 * {@code static/}, so this controller is its only route and the operation shows up in the
 * springdoc-generated document like every other one. The body is streamed from the classpath, never
 * parsed or re-serialized, so the server URL and every byte stay as authored. The path is open in
 * {@code AccessPolicy}.
 */
@RestController
@Tag(name = "System")
public class OpenApiController {

    /** The authored document as a classpath resource. */
    private final Resource document = new ClassPathResource(ApiVersion.RESOURCE);

    /**
     * {@code getOpenApiDocument}: the authored OpenAPI 3.1 document, {@code application/json}.
     *
     * @return the document, streamed verbatim
     * @throws NotFoundException when the build did not put the document on the classpath
     */
    @GetMapping("/v1/openapi.json")
    @Operation(operationId = "getOpenApiDocument", tags = "System")
    public ResponseEntity<Resource> getOpenApiDocument() {
        if (!document.exists()) {
            throw new NotFoundException("The OpenAPI document is not on the classpath.");
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(document);
    }
}
