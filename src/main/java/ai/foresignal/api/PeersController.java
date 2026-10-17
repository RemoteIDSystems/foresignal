package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.federation.PeerEntity;
import ai.foresignal.federation.PeerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Federation peers (OpenAPI tag {@code Peers}, prompt 113): CRUD, the {@code /info} test, resync and peer ingest
 * key rotation. Admin only; tokens are write-only and never returned, a new ingest key is shown once.
 */
@RestController
@Tag(name = "Peers")
public class PeersController {

    private final PeerService peers;

    /** Creates the controller. */
    public PeersController(PeerService peers) {
        this.peers = peers;
    }

    /** {@code listPeers}: ordered by id, cursor-paged. */
    @GetMapping("/v1/peers")
    @Operation(operationId = "listPeers", tags = "Peers")
    public Map<String, Object> listPeers(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, PeerService.MAX_LIMIT, 100);
        String after = cursor == null || cursor.isBlank() ? null : decode(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        for (PeerEntity peer : peers.list()) {
            if (after != null && peer.getId().compareTo(after) <= 0) {
                continue;
            }
            if (items.size() == size) {
                next = encode(String.valueOf(items.getLast().get("id")));
                break;
            }
            items.add(peers.view(peer));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", next);
        return out;
    }

    /** {@code createPeer}. */
    @PostMapping("/v1/peers")
    @Operation(operationId = "createPeer", tags = "Peers")
    public ResponseEntity<Map<String, Object>> createPeer(@RequestBody JsonNode body) {
        PeerEntity peer = peers.create(body);
        return ResponseEntity.created(URI.create("/v1/peers/" + peer.getId())).body(peers.view(peer));
    }

    /** {@code getPeer}. */
    @GetMapping("/v1/peers/{peerId}")
    @Operation(operationId = "getPeer", tags = "Peers")
    public Map<String, Object> getPeer(@PathVariable String peerId) {
        return peers.view(peers.get(peerId));
    }

    /** {@code updatePeer}. */
    @PutMapping("/v1/peers/{peerId}")
    @Operation(operationId = "updatePeer", tags = "Peers")
    public Map<String, Object> updatePeer(@PathVariable String peerId, @RequestBody JsonNode body) {
        return peers.view(peers.update(peerId, body));
    }

    /** {@code deletePeer}. */
    @DeleteMapping("/v1/peers/{peerId}")
    @Operation(operationId = "deletePeer", tags = "Peers")
    public ResponseEntity<Void> deletePeer(@PathVariable String peerId) {
        peers.delete(peerId);
        return ResponseEntity.noContent().build();
    }

    /** {@code testPeer}. */
    @PostMapping("/v1/peers/{peerId}/test")
    @Operation(operationId = "testPeer", tags = "Peers")
    public Map<String, Object> testPeer(@PathVariable String peerId) {
        return peers.test(peerId);
    }

    /** {@code resyncPeer}. */
    @PostMapping("/v1/peers/{peerId}/resync")
    @Operation(operationId = "resyncPeer", tags = "Peers")
    public ResponseEntity<Map<String, Object>> resyncPeer(@PathVariable String peerId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(peers.view(peers.resync(peerId)));
    }

    /** {@code rotatePeerIngestKey}. */
    @PostMapping("/v1/peers/{peerId}/ingest-key")
    @Operation(operationId = "rotatePeerIngestKey", tags = "Peers")
    public Map<String, Object> rotatePeerIngestKey(@PathVariable String peerId) {
        return peers.rotateIngestKey(peerId);
    }

    private static String encode(String id) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(id.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        try {
            return new String(java.util.Base64.getUrlDecoder().decode(cursor.strip()),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new ai.foresignal.api.error.ValidationException(ai.foresignal.api.error.ErrorCodes.INVALID_CURSOR,
                    "The cursor is not recognized.", List.of());
        }
    }
}
