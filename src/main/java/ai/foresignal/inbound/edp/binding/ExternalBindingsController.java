package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.support.Actors;
import ai.foresignal.intel.IntelVisibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The external data binding endpoints of prompt 138 (tag {@code External}): bindings of a provider's streams
 * (admin), suggested bindings, previews, and question links of forecast streams (read to list, agent to link and
 * unlink).
 */
@RestController
@Tag(name = "External")
public class ExternalBindingsController {

    private final BindingService service;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public ExternalBindingsController(BindingService service, IntelVisibility visibility) {
        this.service = service;
        this.visibility = visibility;
    }

    /** {@code listExternalBindings}. */
    @GetMapping("/v1/external/sources/{sourceId}/bindings")
    @Operation(operationId = "listExternalBindings", tags = "External")
    public List<Map<String, Object>> listExternalBindings(@PathVariable String sourceId) {
        return service.list(sourceId).stream().map(service::view).toList();
    }

    /** {@code createExternalBinding}: 201. */
    @PostMapping("/v1/external/sources/{sourceId}/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createExternalBinding", tags = "External")
    public Map<String, Object> createExternalBinding(@PathVariable String sourceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return service.view(service.get(service.create(sourceId, body, Actors.of(request)).getId()));
    }

    /** {@code acceptSuggestedBindings}: 201 with the bindings created. */
    @PostMapping("/v1/external/sources/{sourceId}/bindings/accept-suggested")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "acceptSuggestedBindings", tags = "External")
    public List<Map<String, Object>> acceptSuggestedBindings(@PathVariable String sourceId,
            HttpServletRequest request) {
        return service.acceptSuggested(sourceId, Actors.of(request)).stream()
                .map(b -> service.view(service.get(b.getId()))).toList();
    }

    /** {@code getExternalBinding}. */
    @GetMapping("/v1/external/bindings/{bindingId}")
    @Operation(operationId = "getExternalBinding", tags = "External")
    public Map<String, Object> getExternalBinding(@PathVariable String bindingId) {
        return service.view(service.get(bindingId));
    }

    /** {@code updateExternalBinding}. */
    @PutMapping("/v1/external/bindings/{bindingId}")
    @Operation(operationId = "updateExternalBinding", tags = "External")
    public Map<String, Object> updateExternalBinding(@PathVariable String bindingId, @RequestBody JsonNode body) {
        return service.view(service.get(service.update(bindingId, body).getId()));
    }

    /** {@code deleteExternalBinding}: 204. */
    @DeleteMapping("/v1/external/bindings/{bindingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteExternalBinding", tags = "External")
    public void deleteExternalBinding(@PathVariable String bindingId) {
        service.delete(bindingId);
    }

    /** {@code previewExternalBinding}: no side effects. */
    @PostMapping("/v1/external/bindings/{bindingId}/preview")
    @Operation(operationId = "previewExternalBinding", tags = "External")
    public Map<String, Object> previewExternalBinding(@PathVariable String bindingId) {
        return service.preview(bindingId);
    }

    /** {@code listExternalQuestionLinks}. */
    @GetMapping("/v1/external/sources/{sourceId}/question-links")
    @Operation(operationId = "listExternalQuestionLinks", tags = "External")
    public List<Map<String, Object>> listExternalQuestionLinks(@PathVariable String sourceId) {
        return service.links(sourceId).stream().map(BindingService::view).toList();
    }

    /** {@code createExternalQuestionLink}: 201. */
    @PostMapping("/v1/external/sources/{sourceId}/question-links")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createExternalQuestionLink", tags = "External")
    public Map<String, Object> createExternalQuestionLink(@PathVariable String sourceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return BindingService.view(service.link(sourceId, body, Actors.of(request), visibility.seesPrivate(request)));
    }

    /** {@code deleteExternalQuestionLink}: 204. */
    @DeleteMapping("/v1/external/question-links/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteExternalQuestionLink", tags = "External")
    public void deleteExternalQuestionLink(@PathVariable String linkId) {
        service.unlink(linkId);
    }
}
