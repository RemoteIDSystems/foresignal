package ai.foresignal.inbound;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.springframework.http.HttpStatus;

/**
 * Reads an inbound push body with the 5 MB cap (prompt 024), decompressing
 * {@code Content-Encoding: gzip} first (AIS-catcher {@code GZIP on}); the cap applies to the
 * decompressed size. Over the cap is {@code 413 PAYLOAD_TOO_LARGE}.
 */
public final class IngestBodyReader {

    /** The largest body accepted, decompressed. */
    public static final int MAX_BYTES = 5 * 1024 * 1024;

    private IngestBodyReader() {}

    /** The request body, at most {@link #MAX_BYTES}. */
    public static byte[] read(HttpServletRequest request) {
        return read(request, MAX_BYTES);
    }

    /** The request body, at most {@code maxBytes}. */
    public static byte[] read(HttpServletRequest request, int maxBytes) {
        long declared = request.getContentLengthLong();
        String encoding = request.getHeader("Content-Encoding");
        boolean gzip = encoding != null && encoding.strip().equalsIgnoreCase("gzip");
        if (!gzip && declared > maxBytes) {
            throw tooLarge(maxBytes);
        }
        try (InputStream raw = request.getInputStream();
                InputStream in = gzip ? new GZIPInputStream(raw) : raw) {
            byte[] body = in.readNBytes(maxBytes + 1);
            if (body.length > maxBytes) {
                throw tooLarge(maxBytes);
            }
            return body;
        } catch (java.util.zip.ZipException e) {
            throw new InvalidInputException("The gzip body could not be decompressed.", e);
        } catch (IOException e) {
            throw new InvalidInputException("The request body could not be read.", e);
        }
    }

    private static ForesignalException tooLarge(int maxBytes) {
        return new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                "The body is larger than " + maxBytes + " bytes (decompressed).");
    }
}
