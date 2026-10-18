package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaService;
import ai.foresignal.media.ThumbnailService;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.visibility.SourceVisibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code Media} operations (prompt 043, Weather-Imagery-Design §6.6): {@code getMedia},
 * {@code getMediaThumbnail} and {@code getMediaMeta}, all {@code read}.
 *
 * <p>Media is content-addressed, so the file and thumbnail responses carry a strong
 * {@code ETag: "sha256"} and {@code Cache-Control: {public|private}, max-age=31536000, immutable},
 * and answer {@code If-None-Match} with {@code 304}. {@code public} only when {@code security.openReads}
 * covers {@code /v1/media/**}, the caller is not restricted and the image's sources are not private.
 * A thumbnail that does not exist yet is rendered synchronously, once however many callers ask
 * ({@link ThumbnailService}). Unknown ids are {@code 404 UNKNOWN_MEDIA}; expired or deleted ones
 * {@code 410 MEDIA_EXPIRED}.
 */
@RestController
@Tag(name = "Media")
public class MediaController {

    /** The immutable cache lifetime, one year. */
    static final long IMMUTABLE_MAX_AGE = 31_536_000L;

    private static final MediaType JSON = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private final MediaService media;
    private final ThumbnailService thumbnails;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final ProductUrls urls;
    private final JsonMapper json;

    /** Creates the controller. */
    public MediaController(MediaService media, ThumbnailService thumbnails, ReadCallerResolver callers,
            SourceVisibility visibility, ProductUrls urls, JsonMapper json) {
        this.media = media;
        this.thumbnails = thumbnails;
        this.callers = callers;
        this.visibility = visibility;
        this.urls = urls;
        this.json = json;
    }

    /** {@code getMedia}: the stored file with immutable caching. */
    @GetMapping("/v1/media/{mediaId}")
    @Operation(operationId = "getMedia", tags = "Media")
    public ResponseEntity<Resource> getMedia(
            @PathVariable String mediaId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        MediaRow row = media.visible(mediaId, callers.excludedSources(caller));
        return serve(row, publicFor(caller, row), ifNoneMatch);
    }

    /** {@code getMediaThumbnail}: the 256 px or 1024 px variant, rendered on first request. */
    @GetMapping("/v1/media/{mediaId}/thumbnail")
    @Operation(operationId = "getMediaThumbnail", tags = "Media")
    public ResponseEntity<Resource> getMediaThumbnail(
            @PathVariable String mediaId,
            @RequestParam(required = false) String w,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        int width = width(w);
        ReadCaller caller = callers.resolve(request);
        MediaRow original = media.visible(mediaId, callers.excludedSources(caller));
        if (ai.foresignal.source.spi.MediaRole.DOCUMENT.wireValue().equals(original.role())) {
            // A generated document (prompt 141, for example CZML) is not an image: there is nothing to render.
            throw ValidationException.forField("mediaId", "this media is a document and has no thumbnail");
        }
        MediaRow variant = thumbnails.thumbnail(original.id(), width);
        return serve(variant, publicFor(caller, original), ifNoneMatch);
    }

    /** {@code getMediaMeta}: the row as a {@code Media} document. */
    @GetMapping("/v1/media/{mediaId}/meta")
    @Operation(operationId = "getMediaMeta", tags = "Media")
    public ResponseEntity<String> getMediaMeta(@PathVariable String mediaId, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        MediaRow row = media.visible(mediaId, callers.excludedSources(caller));
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(ETags.cacheControl(publicFor(caller, row)));
        headers.set(HttpHeaders.VARY, ETags.VARY);
        return ResponseEntity.ok().headers(headers).contentType(JSON)
                .body(json.writeValueAsString(MediaService.document(row, urls.base(request))));
    }

    private ResponseEntity<Resource> serve(MediaRow row, boolean publicCache, String ifNoneMatch) {
        String etag = "\"" + row.sha256() + "\"";
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(cacheControl(publicCache));
        headers.set(HttpHeaders.VARY, ETags.VARY);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        Path file = media.file(row);
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(row.contentType()))
                .contentLength(row.bytes()).body(new FileSystemResource(file));
    }

    /** {@code {public|private}, max-age=31536000, immutable}. */
    static String cacheControl(boolean publicCache) {
        return (publicCache ? "public" : "private") + ", max-age=" + IMMUTABLE_MAX_AGE + ", immutable";
    }

    private boolean publicFor(ReadCaller caller, MediaRow row) {
        return caller.publicCache()
                && (row.sourceId() == null || !visibility.isPrivate(row.sourceId()))
                && (row.recordSourceId() == null || !visibility.isPrivate(row.recordSourceId()));
    }

    private static int width(String w) {
        if (w != null) {
            try {
                int value = Integer.parseInt(w.trim());
                if (ThumbnailService.WIDTHS.contains(value)) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Falls through to the validation error.
            }
        }
        throw ValidationException.forField("w", "must be one of " + ThumbnailService.WIDTHS);
    }
}
