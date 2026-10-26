package ai.foresignal.api;

import ai.foresignal.source.aggregator.AggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * First-party aggregators (OpenAPI tag {@code Aggregators}, prompt R2-019; admin, {@code network.manage}):
 * {@code listAggregators}, {@code getAggregator}, {@code updateAggregator} and {@code testAggregator} for the domains
 * {@code adsb}, {@code ais}, {@code datalink}, {@code satellite} and {@code ground-sensors}.
 */
@RestController
@Tag(name = "Aggregators")
public class AggregatorsController {

    private final AggregatorService aggregators;

    /** Creates the controller. */
    public AggregatorsController(AggregatorService aggregators) {
        this.aggregators = aggregators;
    }

    /** {@code listAggregators}: every domain, with its status and the public sources it supersedes. */
    @GetMapping("/v1/aggregators")
    @Operation(operationId = "listAggregators", tags = "Aggregators")
    public Map<String, Object> listAggregators() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", aggregators.list());
        return out;
    }

    /** {@code getAggregator}. */
    @GetMapping("/v1/aggregators/{domain}")
    @Operation(operationId = "getAggregator", tags = "Aggregators")
    public Map<String, Object> getAggregator(@PathVariable String domain) {
        return aggregators.get(domain);
    }

    /** {@code updateAggregator}: replaces the configuration and supersedes or restores the public sources. */
    @PutMapping("/v1/aggregators/{domain}")
    @Operation(operationId = "updateAggregator", tags = "Aggregators")
    public Map<String, Object> updateAggregator(@PathVariable String domain,
            @RequestBody(required = false) JsonNode body) {
        return aggregators.update(domain, body);
    }

    /** {@code testAggregator}: one read of the stored endpoint; nothing is persisted. */
    @PostMapping("/v1/aggregators/{domain}/test")
    @Operation(operationId = "testAggregator", tags = "Aggregators")
    public Map<String, Object> testAggregator(@PathVariable String domain) {
        return aggregators.test(domain);
    }
}
