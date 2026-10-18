package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.chronicle.network.ChronicleAccessService;
import ai.foresignal.chronicle.network.ChronicleNetworkService;
import ai.foresignal.chronicle.network.FederatedSketches;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.PeerCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The chronicle network (prompt 134): related chronicles at the caller's disclosure level (full when both are visible,
 * opaque when the other is only discoverable), the owner's discoverability, dismissing links, clusters and their
 * deduplicated timeline, access requests and their decisions, network salts (admin) and the peer sketch inbox (peer
 * credential, signed body). Every operation needs a token, except the peer inbox, which needs a peer ingest key.
 */
@RestController
@Tag(name = "Chronicles")
public class ChronicleNetworkController {

    /** The largest sketch batch read, in bytes. */
    static final int MAX_BATCH_BYTES = 16 * 1024 * 1024;

    private final ChronicleNetworkService network;
    private final ChronicleAccessService access;
    private final FederatedSketches federated;
    private final ObjectCallers callers;

    /** Creates the controller. */
    public ChronicleNetworkController(ChronicleNetworkService network, ChronicleAccessService access,
            FederatedSketches federated, ObjectCallers callers) {
        this.network = network;
        this.access = access;
        this.federated = federated;
        this.callers = callers;
    }

    /** {@code listRelatedChronicles}: links of a chronicle at the caller's disclosure level. */
    @GetMapping("/v1/chronicles/{chronicleId}/related")
    @Operation(operationId = "listRelatedChronicles", tags = "Chronicles")
    public Map<String, Object> listRelatedChronicles(@PathVariable String chronicleId,
            @RequestParam(required = false) String minStrength,
            @RequestParam(required = false) String includeDismissed, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return network.related(chronicleId, caller, ChronicleNetworkService.minStrength(minStrength),
                QueryParams.bool("includeDismissed", includeDismissed, false), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code setChronicleDiscoverability}: the owner's setting, capped by TLP. */
    @PutMapping("/v1/chronicles/{chronicleId}/discoverability")
    @Operation(operationId = "setChronicleDiscoverability", tags = "Chronicles")
    public Map<String, Object> setChronicleDiscoverability(@PathVariable String chronicleId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        return network.setDiscoverability(chronicleId, body, callers.authenticated(request));
    }

    /** {@code dismissChronicleLink}: the owner of either side dismisses a link. */
    @PostMapping("/v1/chronicle-network/links/{linkId}/dismiss")
    @Operation(operationId = "dismissChronicleLink", tags = "Chronicles")
    @ApiResponse(responseCode = "204", description = "Dismissed")
    public ResponseEntity<Void> dismissChronicleLink(@PathVariable String linkId, HttpServletRequest request) {
        network.dismiss(linkId, callers.authenticated(request));
        return ResponseEntity.noContent().build();
    }

    /** {@code requestChronicleAccess}: asks the other chronicle's owner for access. */
    @PostMapping("/v1/chronicle-network/links/{linkId}/request-access")
    @Operation(operationId = "requestChronicleAccess", tags = "Chronicles")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> requestChronicleAccess(@PathVariable String linkId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(access.request(linkId, body,
                callers.authenticated(request)));
    }

    /** {@code listChronicleClusters}: clusters holding a chronicle the caller can see. */
    @GetMapping("/v1/chronicle-network/clusters")
    @Operation(operationId = "listChronicleClusters", tags = "Chronicles")
    public Map<String, Object> listChronicleClusters(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        return network.clusters(callers.authenticated(request), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code getChronicleCluster}: one cluster at the caller's disclosure level. */
    @GetMapping("/v1/chronicle-network/clusters/{clusterId}")
    @Operation(operationId = "getChronicleCluster", tags = "Chronicles")
    public Map<String, Object> getChronicleCluster(@PathVariable String clusterId, HttpServletRequest request) {
        return network.cluster(clusterId, callers.authenticated(request));
    }

    /** {@code getChronicleClusterTimeline}: the visible members' timeline, deduplicated by evidence key. */
    @GetMapping("/v1/chronicle-network/clusters/{clusterId}/timeline")
    @Operation(operationId = "getChronicleClusterTimeline", tags = "Chronicles")
    public Map<String, Object> getChronicleClusterTimeline(@PathVariable String clusterId,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        return network.timeline(clusterId, callers.authenticated(request), QueryParams.instant("from", from),
                QueryParams.instant("to", to), cursor, QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code listChronicleAccessRequests}: the caller's incoming and outgoing requests. */
    @GetMapping("/v1/chronicle-network/requests")
    @Operation(operationId = "listChronicleAccessRequests", tags = "Chronicles")
    public Map<String, Object> listChronicleAccessRequests(@RequestParam(required = false) String direction,
            @RequestParam(required = false) String status, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        return access.list(callers.authenticated(request),
                QueryParams.oneOf("direction", direction, List.of("incoming", "outgoing"), null),
                QueryParams.oneOf("status", status, List.of("pending", "granted", "declined"), null), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code grantChronicleAccessRequest}: the owner grants a request. */
    @PostMapping("/v1/chronicle-network/requests/{requestId}/grant")
    @Operation(operationId = "grantChronicleAccessRequest", tags = "Chronicles")
    public Map<String, Object> grantChronicleAccessRequest(@PathVariable String requestId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        return access.grant(requestId, body, callers.authenticated(request));
    }

    /** {@code declineChronicleAccessRequest}: the owner declines a request. */
    @PostMapping("/v1/chronicle-network/requests/{requestId}/decline")
    @Operation(operationId = "declineChronicleAccessRequest", tags = "Chronicles")
    public Map<String, Object> declineChronicleAccessRequest(@PathVariable String requestId,
            HttpServletRequest request) {
        return access.decline(requestId, callers.authenticated(request));
    }

    /** {@code setChronicleNetworkSalt}: installs a network discovery salt (admin). */
    @PutMapping("/v1/chronicle-network/salts")
    @Operation(operationId = "setChronicleNetworkSalt", tags = "Chronicles")
    public Map<String, Object> setChronicleNetworkSalt(@RequestBody JsonNode body, HttpServletRequest request) {
        callers.authenticated(request);
        return network.installSalt(body);
    }

    /** {@code receiveChronicleSketches}: a peer delivers a signed sketch batch (peer ingest key). */
    @PostMapping("/v1/federation/chronicle-sketches")
    @Operation(operationId = "receiveChronicleSketches", tags = "Chronicles")
    @ApiResponse(responseCode = "202", description = "Accepted")
    public ResponseEntity<Map<String, Object>> receiveChronicleSketches(
            @RequestHeader(value = "Foresignal-Signature", required = false) String signature,
            HttpServletRequest request) throws IOException {
        String peerId = PeerCaller.of(request).map(PeerCaller::peerId).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "A peer ingest key is required."));
        byte[] body;
        try (InputStream in = request.getInputStream()) {
            body = in.readNBytes(MAX_BATCH_BYTES + 1);
        }
        if (body.length > MAX_BATCH_BYTES) {
            throw new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                    "A sketch batch is at most " + MAX_BATCH_BYTES + " bytes.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(federated.receive(body, signature, peerId));
    }
}
