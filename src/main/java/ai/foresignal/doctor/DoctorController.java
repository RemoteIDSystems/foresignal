package ai.foresignal.doctor;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.security.visibility.SourceVisibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The adapter doctor over REST (prompt 140, OpenAPI tag {@code Sources}).
 *
 * <ul>
 *   <li>{@code POST /v1/sources/{sourceId}/doctor} ({@code runSourceDoctor}, admin, always authenticated because it
 *       can spend LLM budget): synchronous up to {@value #SYNC_SECONDS} s, else {@code 202} with {@code Location}.
 *   <li>{@code GET /v1/sources/{sourceId}/doctor/reports} ({@code listSourceDoctorReports}, agent).
 *   <li>{@code GET /v1/doctor/reports/{reportId}} ({@code getDoctorReport}, agent); {@code 202} while the run is still
 *       in progress.
 * </ul>
 * Scopes are enforced by {@code AccessPolicy}; a source the caller may not see is reported as unknown.
 */
@RestController
@Tag(name = "Sources")
public class DoctorController {

    /** How long {@code runSourceDoctor} waits before answering 202. */
    public static final int SYNC_SECONDS = 90;

    private final DoctorService doctor;
    private final SourceVisibility visibility;
    private volatile long syncMillis = SYNC_SECONDS * 1000L;

    /** Creates the controller. */
    public DoctorController(DoctorService doctor, SourceVisibility visibility) {
        this.doctor = doctor;
        this.visibility = visibility;
    }

    /** Overrides the synchronous wait (tests). */
    void syncMillis(long value) {
        this.syncMillis = value;
    }

    /** {@code runSourceDoctor}: 200 with the report, or 202 with {@code Location} after 90 s. */
    @PostMapping("/v1/sources/{sourceId}/doctor")
    @Operation(operationId = "runSourceDoctor", tags = "Sources")
    public ResponseEntity<Object> runSourceDoctor(@PathVariable String sourceId,
            @RequestBody(required = false) DoctorOptions body) {
        DoctorService.Started started = doctor.start(sourceId, body == null ? DoctorOptions.DEFAULTS : body,
                DoctorTrigger.MANUAL);
        try {
            DoctorReportView report = started.result().get(syncMillis, TimeUnit.MILLISECONDS);
            return ResponseEntity.ok(report);
        } catch (TimeoutException e) {
            return accepted(started.reportId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return accepted(started.reportId());
        } catch (ExecutionException | CompletionException e) {
            if (e.getCause() instanceof RuntimeException r) {
                throw r;
            }
            throw new IllegalStateException("The doctor run failed", e.getCause());
        }
    }

    /** {@code listSourceDoctorReports}: newest first, keyset-paged. */
    @GetMapping("/v1/sources/{sourceId}/doctor/reports")
    @Operation(operationId = "listSourceDoctorReports", tags = "Sources")
    public DoctorReportPage listSourceDoctorReports(@PathVariable String sourceId,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false) Integer limit, HttpServletRequest request) {
        if (limit != null && (limit < 1 || limit > 500)) {
            throw ValidationException.forField("limit", "must be between 1 and 500");
        }
        requireVisible(sourceId, request);
        return doctor.page(sourceId, cursor, limit == null ? 100 : limit);
    }

    /** {@code getDoctorReport}: one report; 202 while its run is in progress; 404 {@code UNKNOWN_DOCTOR_REPORT}. */
    @GetMapping("/v1/doctor/reports/{reportId}")
    @Operation(operationId = "getDoctorReport", tags = "Sources")
    public ResponseEntity<Object> getDoctorReport(@PathVariable String reportId, HttpServletRequest request) {
        if (doctor.isRunning(reportId)) {
            return accepted(reportId);
        }
        DoctorReportView report = doctor.requireReport(reportId);
        if (!visible(report.sourceId(), request)) {
            throw new NotFoundException(ai.foresignal.api.error.ErrorCodes.UNKNOWN_DOCTOR_REPORT,
                    "There is no doctor report '" + reportId + "'.");
        }
        return ResponseEntity.ok(report);
    }

    private static ResponseEntity<Object> accepted(String reportId) {
        String location = "/v1/doctor/reports/" + reportId;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reportId", reportId);
        body.put("status", "running");
        body.put("location", location);
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(URI.create(location)).body(body);
    }

    private boolean visible(String sourceId, HttpServletRequest request) {
        return visibility.visible(sourceId, CurrentToken.of(request), RequestPrincipal.of(request).orElse(null));
    }

    private void requireVisible(String sourceId, HttpServletRequest request) {
        if (!visible(sourceId, request)) {
            throw NotFoundException.of("Source", sourceId);
        }
    }
}
