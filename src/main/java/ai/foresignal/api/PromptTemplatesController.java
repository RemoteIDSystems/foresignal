package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.llm.PromptTemplates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /v1/prompt-templates/{templateId}} (prompt 116, read): the versioned system prompt of an LLM role,
 * exactly as sent, so clients can show what a model was asked. JSON by default; {@code text/markdown} when asked
 * for. Operator instructions are per analyst profile (shown to agent scope on the profile), not part of a
 * template, so {@code instructions} is null here.
 */
@RestController
@Tag(name = "LLM")
@RequestMapping("/v1/prompt-templates")
public class PromptTemplatesController {

    /** The Markdown media type. */
    static final MediaType MARKDOWN = MediaType.parseMediaType("text/markdown");

    /** {@code getPromptTemplate}. */
    @GetMapping("/{templateId}")
    @Operation(operationId = "getPromptTemplate", tags = "LLM")
    public ResponseEntity<?> getPromptTemplate(@PathVariable String templateId,
            @RequestParam(required = false) String version,
            @RequestHeader(value = "Accept", required = false) String accept) {
        String chosen = version != null ? version : PromptTemplates.latest(templateId).orElse(null);
        String text = chosen == null ? null : PromptTemplates.find(templateId, chosen).orElse(null);
        if (text == null) {
            throw new NotFoundException("No prompt template " + templateId + (version == null ? "" : "@" + version));
        }
        if (accept != null && accept.contains("text/markdown") && !accept.contains("application/json")) {
            return ResponseEntity.ok().contentType(MARKDOWN).body(text);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", templateId);
        out.put("version", chosen);
        out.put("role", PromptTemplates.ROLES.get(templateId));
        out.put("sha256", PromptTemplates.sha256(templateId, chosen));
        out.put("text", text);
        out.put("versions", PromptTemplates.versions(templateId));
        // Operator instructions belong to analyst profiles (agent scope, GET /v1/analyst/profiles/{id}), not to
        // a bundled template, so none is returned here.
        out.put("instructions", null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(out);
    }
}
