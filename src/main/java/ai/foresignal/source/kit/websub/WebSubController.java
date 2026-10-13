package ai.foresignal.source.kit.websub;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The WebSub subscriber callbacks of feed-kit sources (prompt 073), {@code verifyWebSubSubscription} and
 * {@code receiveWebSubNotification}. Open (no token): the unguessable path token identifies the subscription, and
 * {@code AccessPolicy} permits anonymous {@code GET} and {@code POST} on {@code /v1/websub/*}{@code /*} only.
 *
 * <p>While {@code foresignal.kits.websub.enabled=false} (the default) both answer 404.
 */
@RestController
@Tag(name = "WebSub")
public class WebSubController {

    private final WebSubService websub;

    /** Creates the controller. */
    public WebSubController(WebSubService websub) {
        this.websub = websub;
    }

    /**
     * {@code verifyWebSubSubscription}: echoes {@code hub.challenge} as {@code text/plain} when the mode, topic and
     * token match a pending or active subscription; otherwise 404.
     */
    @GetMapping(path = "/v1/websub/{sourceId}/{token}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(operationId = "verifyWebSubSubscription", tags = "WebSub")
    public ResponseEntity<String> verifyWebSubSubscription(@PathVariable String sourceId, @PathVariable String token,
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.topic", required = false) String topic,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.lease_seconds", required = false) String leaseSeconds,
            @RequestParam(name = "hub.reason", required = false) String reason) {
        Long lease = null;
        if (leaseSeconds != null && !leaseSeconds.isBlank()) {
            try {
                lease = Long.parseLong(leaseSeconds.strip());
            } catch (NumberFormatException e) {
                throw notFound();
            }
        }
        String body = websub.verify(sourceId, token, mode, topic, challenge, lease).orElseThrow(WebSubController::notFound);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(body);
    }

    /**
     * {@code receiveWebSubNotification}: 202 when the signed delivery was accepted and handed to the source's sink;
     * 403 for a bad signature, 404 without an active subscription, 413 above 2 MB, 415 for a body that is not a feed,
     * 429 above 60 per minute.
     */
    @PostMapping("/v1/websub/{sourceId}/{token}")
    @Operation(operationId = "receiveWebSubNotification", tags = "WebSub")
    public ResponseEntity<Void> receiveWebSubNotification(@PathVariable String sourceId, @PathVariable String token,
            @RequestHeader(name = "X-Hub-Signature", required = false) String signature, HttpServletRequest request) {
        if (!websub.enabled()) {
            throw notFound();
        }
        byte[] body = read(request);
        websub.receive(sourceId, token, signature, body, request.getContentType());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /** The raw body, at most {@link WebSubService#MAX_BODY_BYTES}; never decompressed, because the HMAC covers it. */
    static byte[] read(HttpServletRequest request) {
        long declared = request.getContentLengthLong();
        if (declared > WebSubService.MAX_BODY_BYTES) {
            throw tooLarge();
        }
        try (InputStream in = request.getInputStream()) {
            byte[] body = in.readNBytes(WebSubService.MAX_BODY_BYTES + 1);
            if (body.length > WebSubService.MAX_BODY_BYTES) {
                throw tooLarge();
            }
            return body;
        } catch (IOException e) {
            throw new InvalidInputException("The request body could not be read.", e);
        }
    }

    private static ForesignalException tooLarge() {
        return new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                "A WebSub notification may be at most " + WebSubService.MAX_BODY_BYTES + " bytes.");
    }

    private static NotFoundException notFound() {
        return new NotFoundException("No WebSub subscription matches this callback.");
    }
}
