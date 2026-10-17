package ai.foresignal.federation;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.store.FetchTrigger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The hub side of an edge relay (prompt 113): {@code POST /v1/federation/ingest/{peerId}}.
 *
 * <ol>
 *   <li>The peer ingest key (checked by the token filter) must belong to {@code peerId}, a {@code RECEIVE} peer
 *       that is enabled; otherwise {@code 403 PEER_KEY_MISMATCH} or 404.
 *   <li>The body (gzip accepted, at most {@value #MAX_BODY_BYTES} bytes decoded) must carry the edge's detached JWS
 *       in {@code Foresignal-Signature}, verified against the pinned key. The first batch pins the key it was
 *       signed with (TOFU), taken from the {@code Foresignal-Key} header (the raw Ed25519 point, base64url, whose
 *       RFC 7638 thumbprint must equal the JWS {@code kid}); later batches must be signed by the pinned key.
 *   <li>At most {@value #MAX_ITEMS} items ({@code 413}); each goes through {@link FederationIngestor} with the same
 *       loop, hop, dedup, license and private checks as PULL. {@code LOCAL_ONLY} records are accepted only from a
 *       {@code sameOperator} peer and keep their {@code LOCAL_ONLY} license, so they are never re-federated.
 *       Idempotent on {@code (origin_ref, origin_seq)}: a re-sent batch counts as duplicates.
 * </ol>
 */
@Component
public class FederationIngestService {

    /** The most items per batch. */
    public static final int MAX_ITEMS = 1_000;

    /** The largest decoded body. */
    public static final int MAX_BODY_BYTES = 32 * 1024 * 1024;

    /** The header carrying the edge's public key (TOFU pinning). */
    public static final String KEY_HEADER = "Foresignal-Key";

    private final PeerStates states;
    private final FederationIngestor ingestor;
    private final FederationWriter writer;
    private final PeerRuntime runtime;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public FederationIngestService(PeerStates states, FederationIngestor ingestor, FederationWriter writer,
            PeerRuntime runtime, JsonMapper json, Clock clock) {
        this.states = states;
        this.ingestor = ingestor;
        this.writer = writer;
        this.runtime = runtime;
        this.json = json;
        this.clock = clock;
    }

    /**
     * Accepts one batch.
     *
     * @param peerId the path's peer
     * @param callerPeerId the peer whose key was presented
     * @param body the raw body (possibly gzip)
     * @param gzip true when {@code Content-Encoding: gzip}
     * @param signature the {@code Foresignal-Signature} header
     * @param presentedKey the {@code Foresignal-Key} header, or null
     * @return the {@code FederationIngestResult} document
     */
    public Map<String, Object> ingest(String peerId, String callerPeerId, InputStream body, boolean gzip,
            String signature, String presentedKey) {
        PeerEntity peer = receivingPeer(peerId, callerPeerId);
        byte[] bytes = read(body, gzip);
        verify(peer, bytes, signature, presentedKey);
        JsonNode root;
        try {
            root = json.readTree(bytes);
        } catch (JacksonException e) {
            throw new ValidationException("The batch is not JSON.");
        }
        JsonNode items = root == null ? null : root.get("items");
        if (items == null || !items.isArray()) {
            throw new ValidationException("The batch needs an items array.");
        }
        if (items.size() > MAX_ITEMS) {
            throw tooLarge("A batch carries at most " + MAX_ITEMS + " items; this one carries " + items.size() + ".");
        }
        List<JsonNode> list = new ArrayList<>(items.size());
        items.forEach(list::add);
        PeerSelection selection = PeerSelection.parse(peer.getSelection(), "selection", new ArrayList<>());
        FederationIngestor.Context context = runtime.context(peer, selection, false);
        FederationIngestor.Result result = ingestor.process(list, context, clock.instant());
        if (!result.records().isEmpty()) {
            try {
                writer.submit(FederationWriter.group(result.records()), FetchTrigger.PUSH, "peer:" + peerId, false);
            } catch (RuntimeException e) {
                ingestor.release(result.records());
                states.update(peerId, PeerService.STATE_FAILING, null, null, null, e.getMessage());
                throw new ForesignalException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.INGEST_BUSY,
                        "The ingest queue is busy; retry shortly.").withHeader("Retry-After", "30");
            }
        }
        states.update(peerId, PeerService.STATE_RECEIVING, null, result.lastSeq() < 0 ? null : result.lastSeq(),
                null, null);
        return result.toJson();
    }

    /** The enabled RECEIVE peer {@code peerId}, presented with its own key. */
    PeerEntity receivingPeer(String peerId, String callerPeerId) {
        PeerEntity peer = states.find(peerId).orElseThrow(() -> NotFoundException.of("Peer", peerId));
        if (callerPeerId == null || !callerPeerId.equals(peerId)) {
            throw mismatch("The peer ingest key was not issued for peer '" + peerId + "'.");
        }
        if (!PeerService.MODE_RECEIVE.equals(peer.getMode())) {
            throw mismatch("Peer '" + peerId + "' is not a RECEIVE peer.");
        }
        if (!peer.isEnabled()) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.SOURCE_DISABLED,
                    "Peer '" + peerId + "' is disabled.");
        }
        return peer;
    }

    /** Checks the batch's JWS against the pinned key, pinning on the first batch. */
    void verify(PeerEntity peer, byte[] bytes, String signature, String presentedKey) {
        if (signature == null || signature.isBlank()) {
            throw mismatch("The batch carries no Foresignal-Signature.");
        }
        byte[] key = peer.getPinnedKey();
        if (key == null) {
            key = PeerKeys.decodeKey(presentedKey);
            if (key == null) {
                throw mismatch("No key is pinned for this peer yet; send its public key in " + KEY_HEADER + ".");
            }
        }
        PeerKeys.Verification verification = PeerKeys.verify(bytes, signature, key);
        if (!verification.signatureValid()) {
            states.update(peer.getId(), PeerService.STATE_FAILING, PeerService.REASON_KEY_MISMATCH, null, null,
                    verification.error());
            throw mismatch("The batch signature does not verify against the pinned key: " + verification.error());
        }
        if (peer.getPinnedKid() != null && !peer.getPinnedKid().equals(verification.kid())) {
            throw mismatch("The batch is signed by " + verification.kid() + ", but " + peer.getPinnedKid()
                    + " is pinned.");
        }
        if (peer.getPinnedKey() == null) {
            String pinned = states.pinIfUnset(peer.getId(), verification.kid(), verification.key(), null);
            if (pinned != null && !pinned.equals(verification.kid())) {
                throw mismatch("The batch is signed by " + verification.kid() + ", but " + pinned + " is pinned.");
            }
        }
    }

    /** The body, gunzipped when asked, within {@value #MAX_BODY_BYTES} bytes. */
    static byte[] read(InputStream body, boolean gzip) {
        try (InputStream in = gzip ? new GZIPInputStream(body) : body) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int total = 0;
            int n;
            while ((n = in.read(buffer)) > 0) {
                total += n;
                if (total > MAX_BODY_BYTES) {
                    throw tooLarge("The batch is larger than " + MAX_BODY_BYTES + " bytes.");
                }
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new ValidationException("The batch body could not be read: " + e.getMessage());
        }
    }

    private static ForesignalException mismatch(String detail) {
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.PEER_KEY_MISMATCH, detail);
    }

    private static ForesignalException tooLarge(String detail) {
        return new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE, detail);
    }
}
