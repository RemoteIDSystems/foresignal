package ai.foresignal.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.OpenApiSpec.SpecOperation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The API-first annotations of {@code docs/OpenAPI.json} and their agreement with
 * {@code pending-operations.txt} (prompt 020; docs/Conventions.md "API-first" and "Pending
 * operations").
 *
 * <p>Every operation, route or webhook, carries {@code x-foresignal-scope},
 * {@code x-foresignal-prompt} and {@code x-foresignal-status}. Every {@code planned} route is on the
 * pending list with its prompt, and no {@code implemented} one is. A {@code planned} operation owned
 * by a prompt the build has already passed ({@link OpenApiSpec#currentPrompt()}) is a defect.
 */
class SpecAnnotationsTest {

    private static final Set<String> SCOPES = Set.of("open", "read", "agent", "admin", "peer");
    private static final Set<String> STATUSES = Set.of("planned", "implemented");

    @Test
    @DisplayName("every operation has x-foresignal-scope, x-foresignal-prompt and x-foresignal-status with valid values")
    void everyOperationIsAnnotated() {
        List<String> problems = new ArrayList<>();
        for (SpecOperation op : OpenApiSpec.allOperations()) {
            if (op.operationId() == null) {
                problems.add(op.section() + " " + op.method().toUpperCase() + " " + op.path() + ": no operationId");
            }
            if (!SCOPES.contains(op.scope())) {
                problems.add(op.label() + ": x-foresignal-scope is " + op.scope());
            }
            if (op.promptNumber() < 1) {
                problems.add(op.label() + ": x-foresignal-prompt is " + op.prompt() + " (want NNN)");
            }
            if (!STATUSES.contains(op.status())) {
                problems.add(op.label() + ": x-foresignal-status is " + op.status());
            }
        }
        assertThat(problems).as("annotation problems").isEmpty();
    }

    @Test
    @DisplayName("operationIds are unique across paths and webhooks")
    void operationIdsAreUnique() {
        Set<String> seen = new HashSet<>();
        List<String> duplicates = new ArrayList<>();
        for (SpecOperation op : OpenApiSpec.allOperations()) {
            if (op.operationId() != null && !seen.add(op.operationId())) {
                duplicates.add(op.label());
            }
        }
        assertThat(duplicates).as("duplicate operationIds").isEmpty();
    }

    @Test
    @DisplayName("implemented operations are not pending; planned routes are pending with their own prompt")
    void pendingListAgreesWithStatuses() {
        Map<String, Integer> pending = OpenApiSpec.pending();
        List<String> problems = new ArrayList<>();
        Set<String> routeIds = new HashSet<>();
        for (SpecOperation op : OpenApiSpec.routes()) {
            routeIds.add(op.operationId());
            Integer listed = pending.get(op.operationId());
            if (op.implemented() && listed != null) {
                problems.add(op.label() + " is implemented: remove it from pending-operations.txt");
            }
            if (!op.implemented() && listed == null) {
                problems.add(op.label() + " is planned for " + op.prompt() + " but not in pending-operations.txt");
            }
            if (!op.implemented() && listed != null && listed != op.promptNumber()) {
                problems.add(op.label() + " is pending for " + listed + " but x-foresignal-prompt is " + op.prompt());
            }
        }
        for (String id : pending.keySet()) {
            if (!routeIds.contains(id)) {
                problems.add("pending-operations.txt lists " + id + ", which is not a route of docs/OpenAPI.json");
            }
        }
        assertThat(problems).as("pending list problems").isEmpty();
    }

    @Test
    @DisplayName("no planned operation belongs to a prompt the build has already passed")
    void nothingOverdue() {
        int current = OpenApiSpec.currentPrompt();
        List<String> overdue = new ArrayList<>();
        for (SpecOperation op : OpenApiSpec.allOperations()) {
            if (!op.implemented() && op.promptNumber() <= current) {
                overdue.add(op.label() + " is planned for " + op.prompt() + " (current prompt " + current
                        + "): implement or re-assign");
            }
        }
        assertThat(overdue).as("overdue operations").isEmpty();
    }

    @Test
    @DisplayName("the pending list holds only prompts after the current one")
    void pendingPromptsAreInTheFuture() {
        int current = OpenApiSpec.currentPrompt();
        List<String> stale = OpenApiSpec.pending().entrySet().stream()
                .filter(e -> e.getValue() <= current)
                .map(e -> e.getKey() + " " + e.getValue() + " (current prompt " + current + "): implement or re-assign")
                .toList();
        assertThat(stale).isEmpty();
    }
}
