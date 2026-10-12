package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.audit.AuditQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** {@code listAuditEvents} (admin, prompt 023): the audit log, newest first, keyset paged. */
@RestController
@Tag(name = "Audit")
public class AuditController {

    private final AuditQuery query;

    /** Creates the controller. */
    public AuditController(AuditQuery query) {
        this.query = query;
    }

    /** {@code listAuditEvents}. */
    @GetMapping("/v1/audit")
    @Operation(operationId = "listAuditEvents", tags = "Audit")
    public AuditQuery.Page listAuditEvents(
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String tokenId,
            @RequestParam(required = false) String operationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        Integer statusValue = status == null || status.isBlank() ? null : QueryParams.intInRange("status", status, 100, 599, 0);
        UUID token = tokenId == null || tokenId.isBlank() ? null : TokensController.uuid(tokenId.strip(), "tokenId");
        AuditQuery.Filter filter = new AuditQuery.Filter(
                QueryParams.instant("since", since),
                QueryParams.instant("until", until),
                token,
                operationId == null || operationId.isBlank() ? null : operationId.strip(),
                statusValue);
        return query.page(filter, cursor, AuditQuery.limit(limit));
    }
}
