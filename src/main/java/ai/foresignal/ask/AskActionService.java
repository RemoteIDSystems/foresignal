package ai.foresignal.ask;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Proposed Ask actions after they are stored (prompt 028; invariant EGR-3): confirmation, rejection
 * and execution, each audited.
 *
 * <p>An action is visible only to the token that asked, or to an {@code admin} token; any other caller
 * gets {@code 404}. Only a {@code pending} action can be confirmed or rejected ({@code 409
 * ACTION_NOT_PENDING} otherwise). Execution claims the row first (a conditional update), so two
 * concurrent confirmations execute it once. It runs the action's {@link AskActionHandler}, which goes
 * through the same services and validation as REST; the outcome is stored as {@code executed} with
 * the operation's result, or {@code failed} with the error, and written to {@code audit_event}.
 */
@Service
public class AskActionService {

    private static final Logger log = LoggerFactory.getLogger(AskActionService.class);

    /** The audit target type of an action. */
    public static final String AUDIT_TARGET = "ask_action";

    private final AskActionRepository actions;
    private final AskActionTools tools;
    private final AuditService audit;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public AskActionService(AskActionRepository actions, AskActionTools tools, AuditService audit, JdbcTemplate jdbc,
            JsonMapper json, Clock clock) {
        this.actions = actions;
        this.tools = tools;
        this.audit = audit;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * The action {@code id} when {@code caller} may see it.
     *
     * @throws NotFoundException when it does not exist or belongs to another non-admin token
     */
    public AskActionEntity visible(UUID id, AskCaller caller) {
        AskActionEntity action = actions.findById(id).orElseThrow(() -> NotFoundException.of("Ask action", id));
        if (!caller.owns(action.getTokenId())) {
            throw NotFoundException.of("Ask action", id);
        }
        return action;
    }

    /**
     * {@code confirmAskAction}: executes a pending action.
     *
     * @return the action after execution ({@code executed} or {@code failed})
     * @throws NotFoundException when the caller may not see it
     * @throws ConflictException {@code ACTION_NOT_PENDING} when it is not pending
     */
    public AskActionEntity confirm(UUID id, AskCaller caller) {
        AskActionEntity action = visible(id, caller);
        return execute(action, caller, "confirmAskAction", "/v1/ask/actions/" + id + "/confirm");
    }

    /**
     * {@code rejectAskAction}: discards a pending action.
     *
     * @throws NotFoundException when the caller may not see it
     * @throws ConflictException {@code ACTION_NOT_PENDING} when it is not pending
     */
    public AskActionEntity reject(UUID id, AskCaller caller) {
        AskActionEntity action = visible(id, caller);
        Instant now = clock.instant();
        claim(action, caller, now);
        action.setStatus(AskActionEntity.REJECTED);
        action.setExecutedAt(now);
        action.setDecidedBy(caller.actor());
        AskActionEntity saved = actions.save(action);
        audit(saved, caller, "rejectAskAction", "/v1/ask/actions/" + id + "/reject", null);
        return saved;
    }

    /**
     * Executes a pending action for {@code caller} (an {@code agent} or {@code admin} token): on
     * confirmation, or right after an Ask with {@code allowActions}.
     *
     * @param action the stored action
     * @param caller who executes it
     * @param operationId the operation, for the audit entry
     * @param path the request path, for the audit entry
     * @return the action after execution
     * @throws ConflictException {@code ACTION_NOT_PENDING} when it is not pending
     */
    public AskActionEntity execute(AskActionEntity action, AskCaller caller, String operationId, String path) {
        Instant now = clock.instant();
        claim(action, caller, now);
        AskActionHandler handler = tools.handler(action.getType());
        Map<String, Object> result;
        String status;
        if (handler == null) {
            status = AskActionEntity.FAILED;
            result = error("The action type " + action.getType() + " is not available on this server.", null);
        } else {
            try {
                JsonNode outcome = handler.execute(json.valueToTree(action.getPayload()), caller);
                status = AskActionEntity.EXECUTED;
                result = outcome == null || outcome.isNull() ? Map.of() : asMap(outcome);
            } catch (ForesignalException e) {
                status = AskActionEntity.FAILED;
                result = error(McpToolRegistry.describe(e), e.getCode());
            } catch (RuntimeException e) {
                log.warn("Ask action {} ({}) failed", action.getId(), action.getType(), e);
                status = AskActionEntity.FAILED;
                result = error("The action failed unexpectedly (" + e.getClass().getSimpleName() + ").", null);
            }
        }
        action.setStatus(status);
        action.setResult(result);
        action.setExecutedAt(now);
        action.setDecidedBy(caller.actor());
        AskActionEntity saved = actions.save(action);
        audit(saved, caller, operationId, path, status);
        return saved;
    }

    /** The actions of a thread, oldest first. */
    public List<AskActionEntity> ofThread(UUID threadId) {
        return actions.findByThreadIdOrderByCreatedAtAscIdAsc(threadId);
    }

    /**
     * Marks a pending action as being decided, or {@code 409} when another decision got there first.
     * {@code executed_at} is set while the status stays pending until the outcome is written.
     */
    private void claim(AskActionEntity action, AskCaller caller, Instant now) {
        if (!AskActionEntity.PENDING.equals(action.getStatus()) || action.getExecutedAt() != null) {
            throw notPending(action);
        }
        int claimed = jdbc.update("UPDATE ask_action SET executed_at = ?, decided_by = ? WHERE id = ? AND status = ?"
                        + " AND executed_at IS NULL",
                Timestamp.from(now), caller.actor(), action.getId(), AskActionEntity.PENDING);
        if (claimed != 1) {
            throw notPending(action);
        }
    }

    private static ConflictException notPending(AskActionEntity action) {
        return new ConflictException(ErrorCodes.ACTION_NOT_PENDING, "The action " + action.getId() + " is "
                + (AskActionEntity.PENDING.equals(action.getStatus()) ? "already being decided" : action.getStatus())
                + "; only pending actions can be confirmed or rejected.");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(JsonNode node) {
        if (node.isObject()) {
            return json.convertValue(node, Map.class);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("value", json.convertValue(node, Object.class));
        return out;
    }

    private static Map<String, Object> error(String message, String code) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("error", message);
        if (code != null) {
            out.put("code", code);
        }
        return out;
    }

    private void audit(AskActionEntity action, AskCaller caller, String operationId, String path, String outcome) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("type", action.getType());
        detail.put("status", action.getStatus());
        detail.put("threadId", action.getThreadId().toString());
        if (outcome != null) {
            detail.put("outcome", outcome);
        }
        AuthenticatedToken token = caller.token();
        try {
            audit.record(new AuditRecord(
                    clock.instant(),
                    AuditRecord.TOKEN,
                    token == null ? null : token.id(),
                    token == null ? null : token.name(),
                    token == null ? null : token.principalId(),
                    token == null ? null : token.scope().wireValue(),
                    "POST",
                    path,
                    operationId,
                    null,
                    AUDIT_TARGET,
                    action.getId().toString(),
                    caller.mcp().remoteIp(),
                    detail));
        } catch (RuntimeException e) {
            log.warn("Could not audit Ask action {}", action.getId(), e);
        }
    }
}
