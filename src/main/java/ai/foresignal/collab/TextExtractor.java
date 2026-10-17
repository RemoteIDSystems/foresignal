package ai.foresignal.collab;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

/**
 * The searchable text of an attachment (prompt 130): PDF text through Apache PDFBox, KML with its markup and HTML
 * descriptions stripped to text by jsoup, other text types as they are. Images have none. The text is cut to
 * {@value #MAX_BYTES} UTF-8 bytes. Nothing is rendered or executed: PDFBox only reads the text content streams.
 */
public final class TextExtractor {

    /** The most extracted text kept, in UTF-8 bytes (1 MB). */
    public static final int MAX_BYTES = 1_048_576;

    /**
     * Extracted text.
     *
     * @param text the text, or null when there is none
     * @param truncated true when it was cut at {@link #MAX_BYTES}
     */
    public record Extracted(String text, boolean truncated) {

        /** No text. */
        public static final Extracted NONE = new Extracted(null, false);
    }

    private TextExtractor() {}

    /** The text of {@code bytes} of {@code kind}; a PDF that cannot be read (encrypted, damaged) has none. */
    public static Extracted extract(byte[] bytes, UploadTypes.Kind kind) {
        String text = switch (kind) {
            case PNG, JPEG -> null;
            case PDF -> pdf(bytes);
            case KML -> Jsoup.parse(Jsoup.parse(new String(bytes, StandardCharsets.UTF_8), "", Parser.xmlParser())
                    .text()).text();
            default -> new String(bytes, StandardCharsets.UTF_8);
        };
        if (text == null || text.isBlank()) {
            return Extracted.NONE;
        }
        return cut(text.strip());
    }

    private static String pdf(byte[] bytes) {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(doc);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** {@code text} cut to at most {@link #MAX_BYTES} UTF-8 bytes on a code point boundary. */
    static Extracted cut(String text) {
        if (text.length() * 3L <= MAX_BYTES || text.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES) {
            return new Extracted(text, false);
        }
        int bytes = 0;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            int n = cp < 0x80 ? 1 : cp < 0x800 ? 2 : cp < 0x10000 ? 3 : 4;
            if (bytes + n > MAX_BYTES) {
                break;
            }
            bytes += n;
            i += Character.charCount(cp);
        }
        return new Extracted(text.substring(0, i), true);
    }
}
