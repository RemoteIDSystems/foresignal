package ai.foresignal.collab;

import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import java.time.Clock;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Writes the collaboration changes to the 023 audit log (prompt 130, COL-1): every share, access-list change,
 * ownership transfer, principal and group change, with what changed in {@code detail} (never secrets). The token
 * filter already records each authenticated write; these entries add the object and the grants.
 */
@Component
public class CollabAudit {

    /** {@code targetType} of an access-list change or share. */
    public static final String TARGET_ACL = "object_acl";

    /** {@code targetType} of a principal change. */
    public static final String TARGET_PRINCIPAL = "principal";

    /** {@code targetType} of a group change. */
    public static final String TARGET_GROUP = "group";

    private final AuditService audit;
    private final Clock clock;

    /** Creates the component. */
    public CollabAudit(AuditService audit, Clock clock) {
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Records one change.
     *
     * @param caller who made it
     * @param operation the operation, e.g. {@code shareObject} or {@code mcp:share_object}
     * @param targetType what was changed
     * @param targetId its id or reference
     * @param detail what changed
     */
    public void record(ObjectCaller caller, String operation, String targetType, String targetId,
            Map<String, Object> detail) {
        audit.record(new AuditRecord(clock.instant(), AuditRecord.TOKEN, null,
                caller == null ? null : caller.actor(), caller == null ? null : caller.principal(),
                caller == null || caller.scope() == null ? null : caller.scope().wireValue(), null, null, operation,
                null, targetType, targetId, null, detail));
    }
}
