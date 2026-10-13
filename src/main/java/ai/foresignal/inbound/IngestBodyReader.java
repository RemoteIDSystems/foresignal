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
 *
 * <p>An empty form body whose fields the container already parsed into request parameters is rebuilt from them
 * (prompt 059, {@link #formFromParameters}).
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
            if (body.length == 0 && !gzip && isForm(request.getContentType())) {
                return formFromParameters(request);
            }
            return body;
        } catch (java.util.zip.ZipException e) {
            throw new InvalidInputException("The gzip body could not be decompressed.", e);
        } catch (IOException e) {
            throw new InvalidInputException("The request body could not be read.", e);
        }
    }

    /** True for {@code application/x-www-form-urlencoded}. */
    static boolean isForm(String contentType) {
        return contentType != null
                && contentType.toLowerCase(java.util.Locale.ROOT).startsWith("application/x-www-form-urlencoded");
    }

    /**
     * A form body rebuilt from the request parameters that are not query parameters (prompt 059). The servlet
     * container hands a form body to whoever first asks for a parameter and leaves the input stream empty; this
     * restores it so a form push (Ecowitt) never loses its fields.
     */
    static byte[] formFromParameters(HttpServletRequest request) {
        java.util.Set<String> queryNames = new java.util.HashSet<>();
        String query = request.getQueryString();
        if (query != null) {
            for (String pair : query.split("&")) {
                int eq = pair.indexOf('=');
                queryNames.add(java.net.URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq),
                        java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        StringBuilder out = new StringBuilder();
        for (java.util.Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (queryNames.contains(entry.getKey())) {
                continue;
            }
            for (String value : entry.getValue()) {
                if (!out.isEmpty()) {
                    out.append('&');
                }
                out.append(java.net.URLEncoder.encode(entry.getKey(), java.nio.charset.StandardCharsets.UTF_8))
                        .append('=')
                        .append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        return out.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static ForesignalException tooLarge(int maxBytes) {
        return new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                "The body is larger than " + maxBytes + " bytes (decompressed).");
    }
}
