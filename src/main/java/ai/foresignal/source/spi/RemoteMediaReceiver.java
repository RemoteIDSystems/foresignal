package ai.foresignal.source.spi;

import java.time.Instant;

/**
 * A running source that takes images pushed by a remote station (prompt 054,
 * {@code POST /v1/ingest/{sourceId}/media}, Weather-Imagery-Design §4.7 b).
 *
 * <p>A SatDump station registers one with {@link RemoteMediaReceivers} while its stream runs. The
 * endpoint hands it the image, already within the 25 MB cap; the receiver runs the image guard,
 * stores the image and its thumbnails, attaches it to a pass and emits the pass record, all under its
 * own lock, so a pushed image and the inbox loop never race on the same pass.
 */
@FunctionalInterface
public interface RemoteMediaReceiver {

    /**
     * One pushed image.
     *
     * @param bytes the image
     * @param contentType the declared type ({@code image/png}, {@code image/jpeg} or {@code image/tiff})
     * @param passId the {@code satellite-pass} externalId to attach to, or null for the join rule
     * @param composite the composite name, for example {@code msu_mr_rgb_221}
     * @param timestamp the image time
     * @param satellite the satellite name as the station reports it
     */
    record Upload(byte[] bytes, String contentType, String passId, String composite, Instant timestamp,
            String satellite) {}

    /**
     * What was stored.
     *
     * @param mediaId the stored image's id
     * @param sha256 its content hash
     * @param passId the pass it attached to, or the pass minted for it
     */
    record Accepted(String mediaId, String sha256, String passId) {}

    /**
     * Stores and attaches one image.
     *
     * @throws ImageRejectedException when the image guard refuses it
     * @throws AdapterException when it cannot be stored
     */
    Accepted receive(Upload upload) throws AdapterException;
}
