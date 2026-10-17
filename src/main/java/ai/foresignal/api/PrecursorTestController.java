package ai.foresignal.api;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.backtest.PrecursorTestService;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** Precursor tests (prompt 103). */
@RestController
@Tag(name = "Analysis")
@RequestMapping("/v1/precursor-tests")
public class PrecursorTestController {

    private final PrecursorTestService tests;
    private final BacktestJobService jobs;

    /** Creates the controller. */
    public PrecursorTestController(PrecursorTestService tests, BacktestJobService jobs) {
        this.tests = tests;
        this.jobs = jobs;
    }

    /** {@code listPrecursorTests}. */
    @GetMapping
    @Operation(operationId = "listPrecursorTests", tags = "Analysis")
    public Map<String, Object> listPrecursorTests() {
        return BacktestViews.page(tests.list().stream().map(BacktestViews::precursorTest).toList());
    }

    /** {@code createPrecursorTest}: 201. */
    @PostMapping
    @Operation(operationId = "createPrecursorTest", tags = "Analysis")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createPrecursorTest(@RequestBody JsonNode body, HttpServletRequest request) {
        return BacktestViews.precursorTest(tests.create(body, tokenId(request)));
    }

    /** {@code getPrecursorTest}. */
    @GetMapping("/{id}")
    @Operation(operationId = "getPrecursorTest", tags = "Analysis")
    public Map<String, Object> getPrecursorTest(@PathVariable String id) {
        return BacktestViews.precursorTest(tests.get(id));
    }

    /** {@code updatePrecursorTest}. */
    @PutMapping("/{id}")
    @Operation(operationId = "updatePrecursorTest", tags = "Analysis")
    public Map<String, Object> updatePrecursorTest(@PathVariable String id, @RequestBody JsonNode body) {
        return BacktestViews.precursorTest(tests.update(id, body));
    }

    /** {@code deletePrecursorTest}. */
    @DeleteMapping("/{id}")
    @Operation(operationId = "deletePrecursorTest", tags = "Analysis")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePrecursorTest(@PathVariable String id) {
        tests.delete(id);
    }

    /** {@code testPrecursorTest}: 202 with the queued precursor job. */
    @PostMapping("/{id}/test")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "testPrecursorTest", tags = "Analysis")
    public Map<String, Object> testPrecursorTest(@PathVariable String id, @RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        BacktestJobEntity job = jobs.submit("precursor", id, null, body, null, tokenId(request));
        return BacktestViews.job(job);
    }

    /** The calling token's id (the one-job-per-token rule and the creator), or null when anonymous. */
    private static String tokenId(HttpServletRequest request) {
        return CurrentToken.of(request).map(c -> c.id().toString()).orElse(null);
    }
}
