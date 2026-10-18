package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.exchange.ExchangeInbox;
import ai.foresignal.exchange.ExchangePackageEntity;
import ai.foresignal.exchange.ExchangeService;
import ai.foresignal.exchange.ExchangeVocabulary;
import ai.foresignal.exchange.ReceivedPackageEntity;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.PeerCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

/**
 * Intelligence exchange (prompt 132): signed packages built here and sent to 113 peers or downloaded, and packages
 * received from other instances, verified and imported read-only. Building, downloading and listing sent packages
 * need agent scope; the peer inbox and receipts need a peer ingest key ({@code fsp_}); an uploaded import needs admin;
 * received packages are listed and read per recipient (RED only by its named recipients).
 */
@RestController
@Tag(name = "Exchange")
public class ExchangeController {

    private final ExchangeService exchange;
    private final ExchangeInbox inbox;
    private final ObjectCallers callers;

    /** Creates the controller. */
    public ExchangeController(ExchangeService exchange, ExchangeInbox inbox, ObjectCallers callers) {
        this.exchange = exchange;
        this.inbox = inbox;
        this.callers = callers;
    }

    // ------------------------------------------------------------------------- sent

    /** {@code createExchangePackage}: builds, signs and delivers (or keeps for download) a package. */
    @PostMapping("/v1/exchange/packages")
    @Operation(operationId = "createExchangePackage", tags = "Exchange")
    @ApiResponse(responseCode = "202", description = "Started")
    public ResponseEntity<Map<String, Object>> createExchangePackage(@RequestBody JsonNode body,
            HttpServletRequest request) {
        ExchangePackageEntity p = exchange.create(body, callers.authenticated(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(URI.create("/v1/exchange/packages/" + p.getId()))
                .body(exchange.view(p));
    }

    /** {@code getExchangePackage}: build and delivery state (creator or admin). */
    @GetMapping("/v1/exchange/packages/{packageId}")
    @Operation(operationId = "getExchangePackage", tags = "Exchange")
    public Map<String, Object> getExchangePackage(@PathVariable String packageId, HttpServletRequest request) {
        return exchange.view(exchange.get(packageId, callers.authenticated(request)));
    }

    /** {@code downloadExchangePackage}: the package ZIP to hand over by other means. */
    @GetMapping("/v1/exchange/packages/{packageId}/download")
    @Operation(operationId = "downloadExchangePackage", tags = "Exchange")
    public ResponseEntity<byte[]> downloadExchangePackage(@PathVariable String packageId,
            HttpServletRequest request) {
        byte[] zip = exchange.download(packageId, callers.authenticated(request));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(ExchangeVocabulary.MEDIA_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("foresignal-package-" + packageId + ".zip").build().toString())
                .header("X-Content-Type-Options", "nosniff").cacheControl(CacheControl.noStore()).body(zip);
    }

    /** {@code listSentPackages}: own packages, or all for an admin. */
    @GetMapping("/v1/exchange/sent")
    @Operation(operationId = "listSentPackages", tags = "Exchange")
    public Map<String, Object> listSentPackages(@RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int max = QueryParams.intInRange("limit", limit, 1, 500, 100);
        return exchange.sent(callers.authenticated(request), status, cursor, max);
    }

    // ------------------------------------------------------------------------- peers

    /** {@code receiveExchangePackage}: a peer delivers a package (peer ingest key). */
    @PostMapping("/v1/exchange/inbox")
    @Operation(operationId = "receiveExchangePackage", tags = "Exchange")
    @ApiResponse(responseCode = "202", description = "Delivery receipt")
    public ResponseEntity<Map<String, Object>> receiveExchangePackage(HttpServletRequest request) throws IOException {
        String peerId = peer(request);
        byte[] zip = body(request, exchange.maxBytes());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(inbox.receive(zip, peerId));
    }

    /** {@code receiveExchangeReceipt}: a peer reports a delivery or an acknowledgement (peer ingest key). */
    @PostMapping("/v1/exchange/receipts")
    @Operation(operationId = "receiveExchangeReceipt", tags = "Exchange")
    @ApiResponse(responseCode = "204", description = "Stored")
    public ResponseEntity<Void> receiveExchangeReceipt(@RequestBody JsonNode body, HttpServletRequest request) {
        exchange.receipt(body, peer(request));
        return ResponseEntity.noContent().build();
    }

    private static String peer(HttpServletRequest request) {
        return PeerCaller.of(request).map(PeerCaller::peerId).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "A peer ingest key is required."));
    }

    private static byte[] body(HttpServletRequest request, long max) throws IOException {
        byte[] bytes;
        try (InputStream in = request.getInputStream()) {
            bytes = in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8L, max + 1));
        }
        if (bytes.length > max) {
            throw new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.PACKAGE_TOO_LARGE,
                    "The package exceeds " + max + " bytes (exchange.maxPackageMb).");
        }
        return bytes;
    }

    // ------------------------------------------------------------------------- import and received

    /** {@code importExchangePackage}: an uploaded package (admin); {@code jwks} pins the sender's keys. */
    @PostMapping(value = "/v1/exchange/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "importExchangePackage", tags = "Exchange")
    @ApiResponse(responseCode = "201", description = "Imported")
    public ResponseEntity<Map<String, Object>> importExchangePackage(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "jwks", required = false) String jwks, HttpServletRequest request)
            throws IOException {
        ObjectCaller caller = callers.authenticated(request);
        if (file == null || file.isEmpty()) {
            throw ValidationException.forField("file", "is required: the package ZIP");
        }
        if (file.getSize() > exchange.maxBytes()) {
            throw new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.PACKAGE_TOO_LARGE,
                    "The package exceeds " + exchange.maxBytes() + " bytes (exchange.maxPackageMb).");
        }
        ReceivedPackageEntity rp = inbox.importPackage(file.getBytes(), jwks, caller);
        return ResponseEntity.created(URI.create("/v1/exchange/received/" + rp.getId())).body(inbox.view(rp));
    }

    /** {@code listReceivedPackages}: packages addressed to the caller. */
    @GetMapping("/v1/exchange/received")
    @Operation(operationId = "listReceivedPackages", tags = "Exchange")
    public Map<String, Object> listReceivedPackages(@RequestParam(required = false) String type,
            @RequestParam(required = false) String senderInstanceId,
            @RequestParam(required = false) String acknowledged, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        int max = QueryParams.intInRange("limit", limit, 1, 500, 100);
        Boolean ack = acknowledged == null || acknowledged.isBlank() ? null
                : QueryParams.bool("acknowledged", acknowledged, false);
        return inbox.list(callers.authenticated(request), type, senderInstanceId, ack, cursor, max);
    }

    /** {@code getReceivedPackage}: sender, authors, signature status and TLP. */
    @GetMapping("/v1/exchange/received/{receivedId}")
    @Operation(operationId = "getReceivedPackage", tags = "Exchange")
    public Map<String, Object> getReceivedPackage(@PathVariable String receivedId, HttpServletRequest request) {
        return inbox.view(inbox.get(receivedId, callers.authenticated(request)));
    }

    /** {@code getReceivedPackageRendition}: rendered here from the package, always marked with its origin. */
    @GetMapping("/v1/exchange/received/{receivedId}/rendition")
    @Operation(operationId = "getReceivedPackageRendition", tags = "Exchange")
    public ResponseEntity<Object> getReceivedPackageRendition(@PathVariable String receivedId,
            @RequestParam(required = false) String format, HttpServletRequest request) {
        String f = QueryParams.oneOf("format", format, List.of("md", "html", "json"), null);
        if (f == null) {
            throw ValidationException.forField("format", "is required: md, html or json");
        }
        return ReportsController.rendition(f, inbox.rendition(receivedId, f, callers.authenticated(request)));
    }

    /** {@code acknowledgeReceivedPackage}: records the acknowledgement and sends a receipt to the sender. */
    @PostMapping("/v1/exchange/received/{receivedId}/acknowledge")
    @Operation(operationId = "acknowledgeReceivedPackage", tags = "Exchange")
    public Map<String, Object> acknowledgeReceivedPackage(@PathVariable String receivedId,
            HttpServletRequest request) {
        return inbox.acknowledge(receivedId, callers.authenticated(request));
    }
}
