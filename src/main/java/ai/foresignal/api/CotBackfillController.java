package ai.foresignal.api;

import ai.foresignal.markets.CotBackfillService;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /v1/sources/cftc-cot/backfill} (prompt 117, admin): starts a resumable, budget-limited backfill of the
 * CFTC Commitments of Traders history down to {@code from} (default 2010-01-01) and answers 202 with the job.
 */
@RestController
@Tag(name = "Sources")
public class CotBackfillController {

    private final CotBackfillService backfills;

    /** Creates the controller. */
    public CotBackfillController(CotBackfillService backfills) {
        this.backfills = backfills;
    }

    /**
     * {@code backfillCftcCot}.
     *
     * @param body the optional {@code CotBackfillRequest} ({@code {"from": "YYYY-MM-DD"}})
     * @return the OpenAPI {@code BackfillJob}
     */
    @PostMapping("/v1/sources/cftc-cot/backfill")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "backfillCftcCot", tags = "Sources")
    public Map<String, Object> backfillCftcCot(@RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        Object from = body == null ? null : body.get("from");
        String tokenId = CurrentToken.of(request).map(c -> c.id().toString()).orElse(null);
        return backfills.start(from == null ? null : String.valueOf(from), tokenId, true).view();
    }
}
