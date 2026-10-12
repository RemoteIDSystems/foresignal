package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.notify.escalation.EscalationPolicyService;
import ai.foresignal.notify.escalation.EscalationWorker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
 * The {@code Escalation} operations (prompt 033): policy CRUD at {@code /v1/escalation-policies}
 * ({@code agent}) and {@code getAlertEscalation} at {@code /v1/alerts/{alertId}/escalation}
 * ({@code read}).
 */
@RestController
@Tag(name = "Escalation")
public class EscalationController {

    private final EscalationPolicyService policies;
    private final EscalationWorker escalations;

    /** Creates the controller. */
    public EscalationController(EscalationPolicyService policies, EscalationWorker escalations) {
        this.policies = policies;
        this.escalations = escalations;
    }

    /** {@code listEscalationPolicies}. */
    @GetMapping("/v1/escalation-policies")
    @Operation(operationId = "listEscalationPolicies", tags = "Escalation")
    public List<Map<String, Object>> listEscalationPolicies() {
        return policies.list();
    }

    /** {@code createEscalationPolicy}: 201. */
    @PostMapping("/v1/escalation-policies")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createEscalationPolicy", tags = "Escalation")
    public Map<String, Object> createEscalationPolicy(@RequestBody JsonNode body) {
        return policies.create(body);
    }

    /** {@code getEscalationPolicy}. */
    @GetMapping("/v1/escalation-policies/{policyId}")
    @Operation(operationId = "getEscalationPolicy", tags = "Escalation")
    public Map<String, Object> getEscalationPolicy(@PathVariable String policyId) {
        return policies.get(id(policyId));
    }

    /** {@code updateEscalationPolicy}: a full replace. */
    @PutMapping("/v1/escalation-policies/{policyId}")
    @Operation(operationId = "updateEscalationPolicy", tags = "Escalation")
    public Map<String, Object> updateEscalationPolicy(@PathVariable String policyId, @RequestBody JsonNode body) {
        return policies.update(id(policyId), body);
    }

    /** {@code deleteEscalationPolicy}: 204, or 409 POLICY_IN_USE. */
    @DeleteMapping("/v1/escalation-policies/{policyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteEscalationPolicy", tags = "Escalation")
    public void deleteEscalationPolicy(@PathVariable String policyId) {
        policies.delete(id(policyId));
    }

    /** {@code getAlertEscalation}: 404 when the alert has no escalation. */
    @GetMapping("/v1/alerts/{alertId}/escalation")
    @Operation(operationId = "getAlertEscalation", tags = "Escalation")
    public Map<String, Object> getAlertEscalation(@PathVariable String alertId) {
        long id = AlertsController.id(alertId);
        return escalations.view(id).orElseThrow(() -> NotFoundException.of("Alert escalation", alertId));
    }

    private static UUID id(String text) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Escalation policy", text);
        }
    }
}
