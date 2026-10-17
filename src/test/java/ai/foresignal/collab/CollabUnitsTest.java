package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The pure helpers of prompt 130: upload type detection, EXIF stripping, text cutting, mention parsing, cursors and
 * file-name sanitizing.
 */
class CollabUnitsTest {

    static final byte[] PNG_SIG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    @Test
    @DisplayName("types come from the bytes, never from the client; text must be UTF-8 without NUL")
    void uploadTypes() {
        assertThat(UploadTypes.detect(PNG_SIG, "x.txt", "text/plain")).isEqualTo(UploadTypes.Kind.PNG);
        assertThat(UploadTypes.detect(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0}, null, null))
                .isEqualTo(UploadTypes.Kind.JPEG);
        assertThat(UploadTypes.detect("%PDF-1.7".getBytes(StandardCharsets.US_ASCII), "a.bin", null))
                .isEqualTo(UploadTypes.Kind.PDF);
        assertThat(UploadTypes.detect("a,b\n1,2".getBytes(StandardCharsets.UTF_8), "t.csv", null))
                .isEqualTo(UploadTypes.Kind.CSV);
        assertThat(UploadTypes.detect("{}".getBytes(StandardCharsets.UTF_8), "x", "application/geo+json"))
                .isEqualTo(UploadTypes.Kind.GEOJSON);
        assertThat(UploadTypes.detect("# Title".getBytes(StandardCharsets.UTF_8), "n.md", null))
                .isEqualTo(UploadTypes.Kind.MARKDOWN);
        assertThat(UploadTypes.detect("<kml/>".getBytes(StandardCharsets.UTF_8), "p.kml", null))
                .isEqualTo(UploadTypes.Kind.KML);
        assertThatThrownBy(() -> UploadTypes.detect("<html><script>".getBytes(StandardCharsets.UTF_8), "x.html",
                "text/html")).isInstanceOf(ForesignalException.class)
                .hasFieldOrPropertyWithValue("code", "UPLOAD_TYPE_NOT_ALLOWED");
        assertThatThrownBy(() -> UploadTypes.detect(new byte[] {'a', 0, 'b'}, "x.txt", null))
                .hasFieldOrPropertyWithValue("code", "UPLOAD_TYPE_NOT_ALLOWED");
        assertThatThrownBy(() -> UploadTypes.detect(new byte[] {(byte) 0xC3, 0x28}, "x.txt", null))
                .hasFieldOrPropertyWithValue("code", "UPLOAD_TYPE_NOT_ALLOWED");
        assertThatThrownBy(() -> UploadTypes.detect(new byte[] {'M', 'Z', 0x50, 0}, "setup.exe", null))
                .hasFieldOrPropertyWithValue("code", "UPLOAD_TYPE_NOT_ALLOWED");
    }

    @Test
    @DisplayName("JPEG loses APP1, APP13 and comments; PNG loses text, time and eXIf chunks; pixels are kept")
    void exifStripping() {
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        jpeg.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xD8});
        jpeg.writeBytes(segment(0xE0, "JFIF\0"));
        jpeg.writeBytes(segment(0xE1, "Exif\0\0GPS-SECRET"));
        jpeg.writeBytes(segment(0xFE, "comment"));
        jpeg.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xDA, 0, 2, 1, 2, 3, (byte) 0xFF, (byte) 0xD9});
        byte[] stripped = ExifStripper.strip(jpeg.toByteArray(), UploadTypes.Kind.JPEG);
        String text = new String(stripped, StandardCharsets.ISO_8859_1);
        assertThat(text).contains("JFIF").doesNotContain("GPS-SECRET").doesNotContain("comment");
        assertThat(Arrays.copyOfRange(stripped, stripped.length - 2, stripped.length))
                .containsExactly((byte) 0xFF, (byte) 0xD9);

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        png.writeBytes(PNG_SIG);
        png.writeBytes(chunk("IHDR", new byte[13]));
        png.writeBytes(chunk("tEXt", "Author\0secret".getBytes(StandardCharsets.ISO_8859_1)));
        png.writeBytes(chunk("eXIf", "MM".getBytes(StandardCharsets.ISO_8859_1)));
        png.writeBytes(chunk("IDAT", new byte[] {1, 2, 3}));
        png.writeBytes(chunk("IEND", new byte[0]));
        String out = new String(ExifStripper.strip(png.toByteArray(), UploadTypes.Kind.PNG),
                StandardCharsets.ISO_8859_1);
        assertThat(out).contains("IHDR", "IDAT", "IEND").doesNotContain("tEXt", "secret", "eXIf");
        assertThatThrownBy(() -> ExifStripper.strip(new byte[] {(byte) 0xFF, (byte) 0xD8, 0x00, 0x01},
                UploadTypes.Kind.JPEG)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("extracted text is cut at 1 MB on a code point boundary")
    void textCut() {
        String big = "é".repeat(TextExtractor.MAX_BYTES);
        TextExtractor.Extracted cut = TextExtractor.cut(big);
        assertThat(cut.truncated()).isTrue();
        assertThat(cut.text().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(TextExtractor.MAX_BYTES);
        assertThat(TextExtractor.cut("short").truncated()).isFalse();
        assertThat(TextExtractor.extract(PNG_SIG, UploadTypes.Kind.PNG).text()).isNull();
        byte[] kml = "<kml><Placemark><description><![CDATA[<b>Port</b> closed]]></description></Placemark></kml>"
                .getBytes(StandardCharsets.UTF_8);
        assertThat(TextExtractor.extract(kml, UploadTypes.Kind.KML).text()).isEqualTo("Port closed");
    }

    @Test
    @DisplayName("mentions are @handles, not email addresses, deduplicated and lower case")
    void mentions() {
        assertThat(CommentService.parse("Ping @JSmith and @ops.team. Mail jo@example.org, again @jsmith!"))
                .containsExactly("jsmith", "ops.team");
        assertThat(CommentService.parse(null)).isEmpty();
        assertThat(CommentService.parse("@a alone is too short")).isEmpty();
    }

    @Test
    @DisplayName("offset cursors round-trip and reject anything else; file names are made safe")
    void cursorsAndNames() {
        assertThat(CollabInputs.offset(CollabInputs.cursor(150))).isEqualTo(150);
        assertThat(CollabInputs.offset(null)).isZero();
        assertThatThrownBy(() -> CollabInputs.offset("bogus!")).isInstanceOf(ValidationException.class);
        assertThat(AttachmentService.sanitize("../../etc/\"passwd\";.pdf", UploadTypes.Kind.PDF))
                .isEqualTo("passwd.pdf");
        assertThat(AttachmentService.sanitize(null, UploadTypes.Kind.PNG)).isEqualTo("attachment.png");
        assertThat(AttachmentService.sanitize("C:\\Users\\me\\report", UploadTypes.Kind.TEXT)).isEqualTo("report.txt");
        assertThat(AttachmentService.sanitize(".hidden", UploadTypes.Kind.MARKDOWN)).isEqualTo("hidden.md");
    }

    static byte[] segment(int marker, String payload) {
        byte[] p = payload.getBytes(StandardCharsets.ISO_8859_1);
        int length = p.length + 2;
        byte[] out = new byte[4 + p.length];
        out[0] = (byte) 0xFF;
        out[1] = (byte) marker;
        out[2] = (byte) (length >> 8);
        out[3] = (byte) length;
        System.arraycopy(p, 0, out, 4, p.length);
        return out;
    }

    static byte[] chunk(String type, byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int n = data.length;
        out.writeBytes(new byte[] {(byte) (n >> 24), (byte) (n >> 16), (byte) (n >> 8), (byte) n});
        out.writeBytes(type.getBytes(StandardCharsets.ISO_8859_1));
        out.writeBytes(data);
        out.writeBytes(new byte[4]);
        return out.toByteArray();
    }
}
