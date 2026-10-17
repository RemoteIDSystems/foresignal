package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;

/**
 * The attachment types an upload may have (prompt 130) and the safe content type each is served with. The type is
 * decided from the bytes, never trusted from the client: PNG and JPEG by their signatures (then 043's
 * {@code ImageGuard}), PDF by {@code %PDF-}, and the text types (plain text, CSV, JSON, GeoJSON, KML, Markdown) by
 * the file extension or declared type, on bytes that must be valid UTF-8 without NUL characters. Anything else is
 * {@code 415 UPLOAD_TYPE_NOT_ALLOWED}.
 */
public final class UploadTypes {

    /** A recognized upload type. */
    public enum Kind {
        /** PNG image. */
        PNG("image/png", "png", true),
        /** JPEG image. */
        JPEG("image/jpeg", "jpg", true),
        /** PDF document. */
        PDF("application/pdf", "pdf", false),
        /** Plain text. */
        TEXT("text/plain", "txt", false),
        /** Comma-separated values. */
        CSV("text/csv", "csv", false),
        /** JSON. */
        JSON("application/json", "json", false),
        /** GeoJSON. */
        GEOJSON("application/geo+json", "geojson", false),
        /** KML. */
        KML("application/vnd.google-earth.kml+xml", "kml", false),
        /** Markdown. */
        MARKDOWN("text/markdown", "md", false);

        private final String contentType;
        private final String extension;
        private final boolean image;

        Kind(String contentType, String extension, boolean image) {
            this.contentType = contentType;
            this.extension = extension;
            this.image = image;
        }

        /** The content type it is stored and served with. */
        public String contentType() {
            return contentType;
        }

        /** The stored file extension. */
        public String extension() {
            return extension;
        }

        /** True for an image (served inline). */
        public boolean image() {
            return image;
        }

        /** True for a UTF-8 text type. */
        public boolean text() {
            return !image && this != PDF;
        }
    }

    /** The content types of the allowed kinds, for messages. */
    public static final List<String> ALLOWED = List.of("image/png", "image/jpeg", "application/pdf", "text/plain",
            "text/csv", "application/json", "application/geo+json", "application/vnd.google-earth.kml+xml",
            "text/markdown");

    private UploadTypes() {}

    /**
     * The kind of an upload.
     *
     * @param bytes the content
     * @param filename the client's file name, or null
     * @param declared the client's declared content type, or null
     * @return the kind
     * @throws ForesignalException {@code 415 UPLOAD_TYPE_NOT_ALLOWED}
     */
    public static Kind detect(byte[] bytes, String filename, String declared) {
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return Kind.PNG;
        }
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xFF && (bytes[1] & 0xff) == 0xD8 && (bytes[2] & 0xff) == 0xFF) {
            return Kind.JPEG;
        }
        if (bytes.length >= 5 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F'
                && bytes[4] == '-') {
            return Kind.PDF;
        }
        Kind text = textKind(filename, declared);
        if (text == null) {
            throw notAllowed("The upload is not an allowed type: " + ALLOWED + ".");
        }
        if (!isUtf8Text(bytes)) {
            throw notAllowed("A " + text.contentType() + " upload must be UTF-8 text.");
        }
        return text;
    }

    /** The text kind named by an extension or a declared type, or null. */
    static Kind textKind(String filename, String declared) {
        String ext = "";
        if (filename != null) {
            int dot = filename.lastIndexOf('.');
            ext = dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        switch (ext) {
            case "txt", "text", "log" -> {
                return Kind.TEXT;
            }
            case "csv" -> {
                return Kind.CSV;
            }
            case "json" -> {
                return Kind.JSON;
            }
            case "geojson" -> {
                return Kind.GEOJSON;
            }
            case "kml" -> {
                return Kind.KML;
            }
            case "md", "markdown" -> {
                return Kind.MARKDOWN;
            }
            default -> {
                // fall back to the declared type
            }
        }
        String type = declared == null ? "" : declared.split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
        return switch (type) {
            case "text/plain" -> Kind.TEXT;
            case "text/csv" -> Kind.CSV;
            case "application/json" -> Kind.JSON;
            case "application/geo+json" -> Kind.GEOJSON;
            case "application/vnd.google-earth.kml+xml" -> Kind.KML;
            case "text/markdown" -> Kind.MARKDOWN;
            default -> null;
        };
    }

    /** True when {@code bytes} decode as UTF-8 and hold no NUL character. */
    static boolean isUtf8Text(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0) {
                return false;
            }
        }
        try {
            StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    /** The {@code 415 UPLOAD_TYPE_NOT_ALLOWED} problem. */
    static ForesignalException notAllowed(String detail) {
        return new ForesignalException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UPLOAD_TYPE_NOT_ALLOWED, detail)
                .withProperty("allowed", ALLOWED);
    }
}
