package ai.foresignal.collab;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.media.ImageGuard;
import ai.foresignal.media.ImageInfo;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Attachments (prompt 130): uploaded documents and images stored as data, never code (COL-3).
 *
 * <ul>
 *   <li>The type is decided from the bytes ({@link UploadTypes}); images also pass 043's {@link ImageGuard}, and lose
 *       their EXIF, XMP and IPTC metadata unless {@code foresignal.collab.keep-exif} ({@link ExifStripper}).</li>
 *   <li>Uploads above {@code foresignal.collab.max-upload-mb} (50) are {@code 413 UPLOAD_TOO_LARGE}.</li>
 *   <li>Bytes are stored content-addressed ({@link AttachmentBlobStore}); text is extracted for search
 *       ({@link TextExtractor}).</li>
 *   <li>Attachments are private to their uploader until shared, and carry a TLP label.</li>
 * </ul>
 */
@Service
public class AttachmentService {

    private final AttachmentRepository attachments;
    private final EvidenceItemRepository evidence;
    private final AttachmentBlobStore blobs;
    private final ImageGuard guard;
    private final AccessResolver access;
    private final ShareService shares;
    private final PrincipalViews views;
    private final CollabProperties properties;
    private final Clock clock;

    /** Creates the service. */
    public AttachmentService(AttachmentRepository attachments, EvidenceItemRepository evidence,
            AttachmentBlobStore blobs, ImageGuard guard, AccessResolver access, ShareService shares,
            PrincipalViews views, CollabProperties properties, Clock clock) {
        this.shares = shares;
        this.attachments = attachments;
        this.evidence = evidence;
        this.blobs = blobs;
        this.guard = guard;
        this.access = access;
        this.views = views;
        this.properties = properties;
        this.clock = clock;
    }

    /** The reference of an attachment. */
    public static String ref(String id) {
        return ObjectTypes.ATTACHMENT + ":" + id;
    }

    /**
     * Stores an upload ({@code uploadAttachment}).
     *
     * @param bytes the content
     * @param filename the client's file name, or null
     * @param declaredType the client's content type, or null
     * @param tlpText the requested TLP label, or null for the default
     * @param visibilityText the requested visibility, or null for {@code private}
     * @param caller the uploader
     * @return the new attachment
     * @throws ForesignalException {@code 413 UPLOAD_TOO_LARGE} or {@code 415 UPLOAD_TYPE_NOT_ALLOWED}
     */
    @Transactional
    public AttachmentEntity upload(byte[] bytes, String filename, String declaredType, String tlpText,
            String visibilityText, ObjectCaller caller) {
        checkSize(bytes.length);
        if (bytes.length == 0) {
            throw ValidationException.forField("file", "is empty");
        }
        Tlp tlp = Tlp.parse("tlp", tlpText, access.defaultTlp());
        Visibility visibility = Visibility.parse("visibility", visibilityText, Visibility.PRIVATE);
        UploadTypes.Kind kind = UploadTypes.detect(bytes, filename, declaredType);
        byte[] stored = bytes;
        Map<String, Object> imageMeta = null;
        if (kind.image()) {
            ImageInfo info = guard.inspect(bytes, kind.contentType(), false);
            boolean strip = !properties.keepExif();
            if (strip) {
                try {
                    stored = ExifStripper.strip(bytes, kind);
                } catch (IllegalArgumentException e) {
                    throw ValidationException.forField("file", "the image's structure could not be read: "
                            + e.getMessage());
                }
            }
            imageMeta = new LinkedHashMap<>();
            imageMeta.put("width", info.width());
            imageMeta.put("height", info.height());
            imageMeta.put("exifStripped", strip);
        }
        shares.checkTlp(tlp, caller.principal(), java.util.List.of(), visibility);
        String sha = AttachmentBlobStore.sha256(stored);
        String path = blobs.store(stored, sha, kind.extension());
        Instant now = clock.instant();
        AttachmentEntity a = new AttachmentEntity(Ulids.next(now), sha, path, sanitize(filename, kind),
                kind.contentType(), stored.length, caller.principal(), now);
        TextExtractor.Extracted text = TextExtractor.extract(stored, kind);
        a.setText(text.text(), text.truncated());
        a.setImageMeta(imageMeta);
        attachments.save(a);
        access.register(ref(a.getId()), caller.principal(), visibility, tlp);
        return a;
    }

    /** Rejects an upload over the limit. */
    public void checkSize(long size) {
        if (size > properties.maxUploadBytes()) {
            throw new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.UPLOAD_TOO_LARGE,
                    "The upload is " + size + " bytes; the limit is " + properties.maxUploadBytes()
                            + " bytes (foresignal.collab.max-upload-mb).");
        }
    }

    /**
     * The attachment {@code id} when the caller may see it.
     *
     * @throws NotFoundException {@code UNKNOWN_ATTACHMENT} otherwise
     */
    @Transactional
    public AttachmentEntity get(String id, ObjectCaller caller) {
        AttachmentEntity a = id == null ? null : attachments.findById(id).orElse(null);
        if (a == null || access.role(caller, ref(a.getId())) == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_ATTACHMENT, "No attachment '" + id
                    + "' exists, or you may not see it.");
        }
        return a;
    }

    /** The stored bytes of an attachment the caller may see. */
    @Transactional
    public byte[] content(AttachmentEntity a) {
        return blobs.read(a.getStoragePath());
    }

    /**
     * Deletes an attachment ({@code deleteAttachment}; owner only); the bytes go when no other row uses them.
     *
     * @throws ConflictException {@code ATTACHMENT_IN_USE} while an evidence item cites it
     */
    @Transactional
    public void delete(String id, ObjectCaller caller) {
        AttachmentEntity a = get(id, caller);
        access.require(caller, ref(a.getId()), AccessAction.DELETE);
        if (!evidence.citingAttachment("[\"" + a.getId() + "\"]").isEmpty()) {
            throw new ConflictException(ErrorCodes.ATTACHMENT_IN_USE,
                    "An evidence item still cites this attachment; remove it from the evidence item first.");
        }
        attachments.delete(a);
        attachments.flush();
        access.forget(ref(a.getId()));
        if (attachments.countBySha256(a.getSha256()) == 0) {
            blobs.delete(a.getStoragePath());
        }
    }

    /** The {@code CollabAttachment} view. */
    @Transactional
    public Map<String, Object> view(AttachmentEntity a) {
        ShareableObjectEntity rec = access.record(ref(a.getId())).orElse(null);
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", a.getId());
        out.put("ref", ref(a.getId()));
        out.put("mediaId", a.getMediaId());
        out.put("filename", a.getFilename());
        out.put("contentType", a.getContentType());
        out.put("size", a.getSize());
        out.put("sha256", a.getSha256());
        out.put("text", a.getText());
        out.put("textTruncated", a.isTextTruncated());
        out.put("imageMeta", a.getImageMeta());
        out.put("uploadedBy", views.summary(a.getUploadedBy(), cache));
        out.put("owner", views.summary(rec == null ? a.getUploadedBy() : rec.getOwner(), cache));
        out.put("visibility", rec == null ? Visibility.PRIVATE.wireValue() : rec.getVisibility().wireValue());
        out.put("tlp", rec == null ? access.defaultTlp().wireValue() : rec.getTlp());
        out.put("createdAt", a.getCreatedAt());
        out.put("links", Map.of("content", "/v1/attachments/" + a.getId() + "/content"));
        return out;
    }

    /**
     * A safe file name: no directories, quotes or control characters, at most 255 characters, with the stored type's
     * extension when it has none.
     */
    static String sanitize(String filename, UploadTypes.Kind kind) {
        String name = filename == null ? "" : filename;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        name = name.substring(slash + 1);
        StringBuilder out = new StringBuilder();
        name.codePoints().forEach(cp -> {
            if (!Character.isISOControl(cp) && cp != '"' && cp != '\\' && cp != ';') {
                out.appendCodePoint(cp);
            }
        });
        String clean = out.toString().strip();
        while (clean.startsWith(".")) {
            clean = clean.substring(1);
        }
        if (clean.isEmpty()) {
            clean = "attachment";
        }
        if (!clean.contains(".")) {
            clean = clean + "." + kind.extension();
        }
        return clean.length() > 255 ? clean.substring(clean.length() - 255) : clean;
    }
}
