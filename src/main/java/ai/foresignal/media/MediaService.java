package ai.foresignal.media;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.Ulids;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The read side of the media store behind {@code /v1/media/**} (prompt 043, Weather-Imagery-Design
 * §6.6): visibility, the {@code 404}/{@code 410} decision and the {@code Media} document.
 *
 * <ul>
 *   <li>An unknown id, a malformed one, or a row whose producing or owning source the caller may not
 *       read (a private source, or a token restriction) answers {@code 404 UNKNOWN_MEDIA}.
 *   <li>A row past its {@code expires_at}, a deleted id (tombstone) or a row whose file is gone answers
 *       {@code 410 MEDIA_EXPIRED}.
 * </ul>
 */
@Component
public class MediaService {

    private final MediaQueries queries;
    private final MediaStore store;
    private final Clock clock;

    /** Creates the service. */
    public MediaService(MediaQueries queries, MediaStore store, Clock clock) {
        this.queries = queries;
        this.store = store;
        this.clock = clock;
    }

    /**
     * The row {@code id}, visible to a caller that may not read {@code excludedSources}.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_MEDIA} or {@code 410 MEDIA_EXPIRED}
     */
    public MediaRow visible(String id, Set<String> excludedSources) {
        if (!Ulids.isUlid(id)) {
            throw MediaErrors.unknown(id);
        }
        Optional<MediaRow> found = queries.find(id);
        if (found.isEmpty()) {
            throw queries.tombstoned(id) ? MediaErrors.expired(id) : MediaErrors.unknown(id);
        }
        MediaRow row = found.get();
        if (hidden(row, excludedSources)) {
            throw MediaErrors.unknown(id);
        }
        if (row.expired(clock.instant())) {
            throw MediaErrors.expired(id);
        }
        return row;
    }

    /** True when the row's producing or owning source is in {@code excludedSources}. */
    public static boolean hidden(MediaRow row, Set<String> excludedSources) {
        return row.sourceId() != null && excludedSources.contains(row.sourceId())
                || row.recordSourceId() != null && excludedSources.contains(row.recordSourceId());
    }

    /**
     * The file of a visible row.
     *
     * @throws ForesignalException {@code 410 MEDIA_EXPIRED} when the file is gone (the orphan sweep
     *     deletes the row)
     */
    public Path file(MediaRow row) {
        Path file = store.file(row);
        if (!Files.isRegularFile(file)) {
            throw MediaErrors.expired(row.id());
        }
        return file;
    }

    /** True for a {@code role=audio} row (prompt 141). */
    public static boolean isAudio(MediaRow row) {
        return ai.foresignal.source.spi.MediaRole.AUDIO.wireValue().equals(row.role());
    }

    /** The {@code Media} document of a row; links are absolute under {@code base}. */
    public static Map<String, Object> document(MediaRow row, String base) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.id());
        out.put("sha256", row.sha256());
        out.put("contentType", row.contentType());
        out.put("width", row.width());
        out.put("height", row.height());
        out.put("bytes", row.bytes());
        out.put("role", row.role());
        out.put("sourceId", row.sourceId());
        out.put("recordSourceId", row.recordSourceId());
        out.put("recordExternalId", row.recordExternalId());
        out.put("recordRef", recordRef(row.recordSourceId(), row.recordExternalId()));
        out.put("licence", row.licence());
        out.put("createdAt", row.createdAt().toString());
        out.put("expiresAt", row.expiresAt() == null ? null : row.expiresAt().toString());
        out.put("variantOf", row.variantOf());
        out.put("variantWidth", row.variantWidth());
        String self = base + "/v1/media/" + row.id();
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", self);
        links.put("meta", self + "/meta");
        if (!isAudio(row)) {
            // Prompt 141: audio has no thumbnails.
            links.put("thumbnail256", self + "/thumbnail?w=" + ThumbnailService.SMALL);
            links.put("thumbnail1024", self + "/thumbnail?w=" + ThumbnailService.LARGE);
        }
        out.put("links", links);
        return out;
    }

    /**
     * The Client Contract object reference of a record, {@code record:{sourceId}/{externalId}} with the
     * external id percent-encoded; null without a record.
     */
    public static String recordRef(String sourceId, String externalId) {
        if (sourceId == null || externalId == null) {
            return null;
        }
        return "record:" + sourceId + "/" + URLEncoder.encode(externalId, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
