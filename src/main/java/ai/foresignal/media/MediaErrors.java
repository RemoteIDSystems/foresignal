package ai.foresignal.media;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import org.springframework.http.HttpStatus;

/** The media problem responses (prompt 043). */
public final class MediaErrors {

    private MediaErrors() {}

    /** {@code 404 UNKNOWN_MEDIA}. */
    public static ForesignalException unknown(String id) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_MEDIA,
                "Media '" + id + "' does not exist.");
    }

    /** {@code 410 MEDIA_EXPIRED}. */
    public static ForesignalException expired(String id) {
        return new ForesignalException(HttpStatus.GONE, ErrorCodes.MEDIA_EXPIRED,
                "Media '" + id + "' has expired or was deleted.");
    }
}
