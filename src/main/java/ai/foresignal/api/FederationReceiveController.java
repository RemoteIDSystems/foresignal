package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.FederationIngestService;
import ai.foresignal.federation.compact.CompactBundleReceiver;
import ai.foresignal.federation.compact.CompactBundles;
import ai.foresignal.federation.compact.CompactOutbox;
import ai.foresignal.federation.compact.CompactOutboxService;
import ai.foresignal.provenance.JwsSigner;
import ai.foresignal.security.PeerCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Federation identity, the edge-relay ingest and the compact-message bundles (OpenAPI tag {@code Federation},
 * prompt 113). Present in every mode: an edge serves its identity and bundles, a hub receives pushes.
 *
 * <ul>
 *   <li>{@code GET /v1/federation/info} (read): the instance identity, signed with a detached JWS in
 *       {@code Foresignal-Signature} (109's {@link JwsSigner}).
 *   <li>{@code POST /v1/federation/ingest/{peerId}} (peer key): an edge's batch ({@link FederationIngestService}).
 *   <li>{@code GET /v1/federation/bundle} (read + federation): a CBOR array of pending compact messages.
 *   <li>{@code POST /v1/federation/bundle} (peer key): a peer's bundle into {@code peer-{id}-events}.
 * </ul>
 */
@RestController
@Tag(name = "Federation")
public class FederationReceiveController {

    /** The CBOR media type. */
    public static final MediaType CBOR = MediaType.parseMediaType("application/cbor");

    private final FederationIdentity identity;
    private final JwsSigner signer;
    private final JsonMapper json;
    private final FederationIngestService ingest;
    private final CompactOutboxService outbox;
    private final CompactBundleReceiver bundles;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public FederationReceiveController(FederationIdentity identity, JwsSigner signer, JsonMapper json,
            FederationIngestService ingest, CompactOutboxService outbox, CompactBundleReceiver bundles,
            ReadCallerResolver callers) {
        this.identity = identity;
        this.signer = signer;
        this.json = json;
        this.ingest = ingest;
        this.outbox = outbox;
        this.bundles = bundles;
        this.callers = callers;
    }

    /** {@code getFederationInfo}: the body bytes are exactly what the detached JWS covers. */
    @GetMapping("/v1/federation/info")
    @Operation(operationId = "getFederationInfo", tags = "Federation")
    public ResponseEntity<byte[]> getFederationInfo(HttpServletRequest request) {
        byte[] body = json.writeValueAsBytes(identity.info(request));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Foresignal-Signature", signer.signDetached(body, "application/json"))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    /** {@code federationIngest}. */
    @PostMapping("/v1/federation/ingest/{peerId}")
    @Operation(operationId = "federationIngest", tags = "Federation")
    public Map<String, Object> federationIngest(@PathVariable String peerId,
            @RequestHeader(value = "Foresignal-Signature", required = false) String signature,
            @RequestHeader(value = FederationIngestService.KEY_HEADER, required = false) String key,
            @RequestHeader(value = HttpHeaders.CONTENT_ENCODING, required = false) String encoding,
            HttpServletRequest request) throws IOException {
        String caller = PeerCaller.of(request).map(PeerCaller::peerId).orElse(null);
        try (InputStream body = request.getInputStream()) {
            return ingest.ingest(peerId, caller, body, encoding != null && encoding.strip().equalsIgnoreCase("gzip"),
                    signature, key);
        }
    }

    /** {@code getFederationBundle}: pending compact messages as one CBOR array. */
    @GetMapping("/v1/federation/bundle")
    @Operation(operationId = "getFederationBundle", tags = "Federation")
    public ResponseEntity<byte[]> getFederationBundle(@RequestParam(required = false) String since,
            HttpServletRequest request) {
        FederationController.caller(request, callers);
        List<byte[]> encoded = new ArrayList<>();
        for (CompactOutbox.Entry entry : outbox.pending(QueryParams.instant("since", since))) {
            encoded.add(entry.encoded());
        }
        return ResponseEntity.ok().contentType(CBOR).header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(CompactBundles.encode(encoded));
    }

    /** {@code receiveFederationBundle}. */
    @PostMapping("/v1/federation/bundle")
    @Operation(operationId = "receiveFederationBundle", tags = "Federation")
    public Map<String, Object> receiveFederationBundle(HttpServletRequest request) throws IOException {
        String peerId = PeerCaller.of(request).map(PeerCaller::peerId).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "A peer ingest key is required."));
        byte[] body;
        try (InputStream in = request.getInputStream()) {
            body = in.readNBytes(CompactBundles.MAX_BYTES + 1);
        }
        return bundles.receive(peerId, body);
    }
}
