package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.collab.AttachmentEntity;
import ai.foresignal.collab.AttachmentService;
import ai.foresignal.collab.EvidenceItemEntity;
import ai.foresignal.collab.EvidenceItemService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

/**
 * Attachments and evidence items (prompt 130, tag {@code Collaboration}): {@code uploadAttachment},
 * {@code getAttachment}, {@code deleteAttachment}, {@code getAttachmentContent}, {@code listEvidenceItems},
 * {@code createEvidenceItem}, {@code getEvidenceItem}, {@code updateEvidenceItem} and {@code deleteEvidenceItem}.
 *
 * <p>Uploaded files are data, never code (COL-3): content is served as an attachment (inline only for images) with
 * its stored safe content type, {@code X-Content-Type-Options: nosniff} and a sandboxing Content-Security-Policy.
 */
@RestController
@Tag(name = "Collaboration")
public class AttachmentsController {

    private final AttachmentService attachments;
    private final EvidenceItemService evidence;
    private final ObjectCallers callers;

    /** Creates the controller. */
    public AttachmentsController(AttachmentService attachments, EvidenceItemService evidence,
            ObjectCallers callers) {
        this.attachments = attachments;
        this.evidence = evidence;
        this.callers = callers;
    }

    // ------------------------------------------------------------------------- attachments

    /** {@code uploadAttachment}: multipart {@code file}, optional {@code tlp} and {@code visibility}. */
    @PostMapping(value = "/v1/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "uploadAttachment", tags = "Collaboration")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "tlp", required = false) String tlp,
            @RequestParam(value = "visibility", required = false) String visibility, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        if (file == null || file.isEmpty()) {
            throw ValidationException.forField("file", "is required");
        }
        attachments.checkSize(file.getSize());
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the upload", e);
        }
        AttachmentEntity a = attachments.upload(bytes, file.getOriginalFilename(), file.getContentType(), blank(tlp),
                blank(visibility), caller);
        return ResponseEntity.created(URI.create("/v1/attachments/" + a.getId())).body(attachments.view(a));
    }

    /** {@code getAttachment}. */
    @GetMapping("/v1/attachments/{attachmentId}")
    @Operation(operationId = "getAttachment", tags = "Collaboration")
    public Map<String, Object> getAttachment(@PathVariable String attachmentId, HttpServletRequest request) {
        return attachments.view(attachments.get(attachmentId, callers.authenticated(request)));
    }

    /** {@code deleteAttachment} (owner only). */
    @DeleteMapping("/v1/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteAttachment", tags = "Collaboration")
    public void deleteAttachment(@PathVariable String attachmentId, HttpServletRequest request) {
        attachments.delete(attachmentId, callers.authenticated(request));
    }

    /** {@code getAttachmentContent}: the bytes, never as active content. */
    @GetMapping("/v1/attachments/{attachmentId}/content")
    @Operation(operationId = "getAttachmentContent", tags = "Collaboration")
    public ResponseEntity<byte[]> getAttachmentContent(@PathVariable String attachmentId,
            HttpServletRequest request) {
        AttachmentEntity a = attachments.get(attachmentId, callers.authenticated(request));
        byte[] bytes = attachments.content(a);
        ContentDisposition disposition = (a.isImage() ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(a.getFilename(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=300")
                .eTag("\"" + a.getSha256() + "\"")
                .body(bytes);
    }

    // ------------------------------------------------------------------------- evidence items

    /** {@code listEvidenceItems}. */
    @GetMapping("/v1/evidence-items")
    @Operation(operationId = "listEvidenceItems", tags = "Collaboration")
    public Map<String, Object> listEvidenceItems(@RequestParam(required = false) String q,
            @RequestParam(required = false) String sourceReliability, @RequestParam(required = false) String tag,
            @RequestParam(required = false) String investigationId, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        return evidence.list(callers.authenticated(request), q, blank(sourceReliability), blank(tag),
                blank(investigationId), cursor, QueryParams.intInRange("limit", limit, 1, 200, 50));
    }

    /** {@code createEvidenceItem}. */
    @PostMapping("/v1/evidence-items")
    @Operation(operationId = "createEvidenceItem", tags = "Collaboration")
    public ResponseEntity<Map<String, Object>> createEvidenceItem(@RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        EvidenceItemEntity e = evidence.create(body, caller);
        return ResponseEntity.created(URI.create("/v1/evidence-items/" + e.getId())).body(evidence.view(e, caller));
    }

    /** {@code getEvidenceItem}. */
    @GetMapping("/v1/evidence-items/{evidenceItemId}")
    @Operation(operationId = "getEvidenceItem", tags = "Collaboration")
    public Map<String, Object> getEvidenceItem(@PathVariable String evidenceItemId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return evidence.view(evidence.get(evidenceItemId, caller), caller);
    }

    /** {@code updateEvidenceItem} (owner or editor; JSON merge patch). */
    @PatchMapping(value = "/v1/evidence-items/{evidenceItemId}", consumes = "application/merge-patch+json")
    @Operation(operationId = "updateEvidenceItem", tags = "Collaboration")
    public Map<String, Object> updateEvidenceItem(@PathVariable String evidenceItemId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return evidence.view(evidence.patch(evidenceItemId, body, caller), caller);
    }

    /** {@code deleteEvidenceItem} (owner only). */
    @DeleteMapping("/v1/evidence-items/{evidenceItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteEvidenceItem", tags = "Collaboration")
    public void deleteEvidenceItem(@PathVariable String evidenceItemId, HttpServletRequest request) {
        evidence.delete(evidenceItemId, callers.authenticated(request));
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
