package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

/**
 * Attachments (prompt 130, COL-3): PDF text extraction, image EXIF stripping, size and type limits, safe serving
 * headers, content-addressed deduplication, and visibility.
 */
@IntegrationTest
@Import(AskTestSources.class)
class AttachmentIT extends CollabItSupport {

    @Autowired AttachmentBlobStore blobs;
    @Autowired AttachmentRepository attachmentRows;

    static byte[] pdf(String text) throws Exception {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 700);
                content.showText(text);
                content.endText();
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    /** A small JPEG with an EXIF APP1 segment carrying a marker string right after SOI. */
    static byte[] jpegWithExif() throws Exception {
        BufferedImage image = new BufferedImage(16, 8, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", plain);
        byte[] jpeg = plain.toByteArray();
        byte[] exif = ("Exif\0\0MM\0*\0\0\0\b\0\0GPS-SECRET-MARKER").getBytes(StandardCharsets.ISO_8859_1);
        int length = exif.length + 2;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(jpeg, 0, 2);
        out.write(0xFF);
        out.write(0xE1);
        out.write(length >> 8);
        out.write(length & 0xff);
        out.writeBytes(exif);
        out.write(jpeg, 2, jpeg.length - 2);
        return out.toByteArray();
    }

    MockHttpServletResponse upload(Person p, String name, String type, byte[] bytes, String tlp) throws Exception {
        var request = multipart("/v1/attachments").file(new MockMultipartFile("file", name, type, bytes));
        if (tlp != null) {
            request.param("tlp", tlp);
        }
        return call(p.bearer(), request);
    }

    @Test
    @DisplayName("a PDF's text is extracted and it is served as an attachment with nosniff")
    void pdfText() throws Exception {
        Person jo = person("jo-a", Scope.READ, null);
        MockHttpServletResponse created = upload(jo, "notice.pdf", "application/octet-stream",
                pdf("Berth 4 closed until further notice"), null);
        assertThat(created.getStatus()).isEqualTo(201);
        JsonNode a = body(created);
        assertThat(a.get("contentType").asString()).isEqualTo("application/pdf");
        assertThat(a.get("text").asString()).contains("Berth 4 closed");
        assertThat(a.get("visibility").asString()).isEqualTo("private");
        assertThat(a.get("tlp").asString()).isEqualTo("AMBER");
        MockHttpServletResponse content = call(jo.bearer(), get("/v1/attachments/" + a.get("id").asString()
                + "/content"));
        assertThat(content.getStatus()).isEqualTo(200);
        assertThat(content.getHeader("Content-Disposition")).startsWith("attachment");
        assertThat(content.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(content.getHeader("Content-Security-Policy")).contains("sandbox");
        assertThat(content.getContentType()).startsWith("application/pdf");
        assertThat(content.getHeader("ETag")).contains(a.get("sha256").asString());
    }

    @Test
    @DisplayName("an image loses its EXIF, is checked by ImageGuard and is served inline")
    void imageExif() throws Exception {
        Person jo = person("jo-img", Scope.READ, null);
        byte[] original = jpegWithExif();
        assertThat(new String(original, StandardCharsets.ISO_8859_1)).contains("GPS-SECRET-MARKER");
        JsonNode a = body(upload(jo, "photo.jpg", "image/jpeg", original, null));
        assertThat(a.get("imageMeta").get("width").asInt()).isEqualTo(16);
        assertThat(a.get("imageMeta").get("exifStripped").asBoolean()).isTrue();
        MockHttpServletResponse content = call(jo.bearer(), get("/v1/attachments/" + a.get("id").asString()
                + "/content"));
        assertThat(content.getHeader("Content-Disposition")).startsWith("inline");
        assertThat(new String(content.getContentAsByteArray(), StandardCharsets.ISO_8859_1))
                .doesNotContain("GPS-SECRET-MARKER");
        assertThat(ImageIO.read(new java.io.ByteArrayInputStream(content.getContentAsByteArray())).getWidth())
                .isEqualTo(16);
    }

    @Test
    @DisplayName("disallowed types are 415, uploads over the limit 413, and an empty form 400")
    void limits() throws Exception {
        Person jo = person("jo-lim", Scope.READ, null);
        MockHttpServletResponse html = upload(jo, "page.html", "text/html",
                "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8), null);
        assertThat(html.getStatus()).isEqualTo(415);
        assertThat(body(html).get("code").asString()).isEqualTo("UPLOAD_TYPE_NOT_ALLOWED");
        MockHttpServletResponse exe = upload(jo, "tool.exe", "application/octet-stream",
                new byte[] {'M', 'Z', (byte) 0x90, 0, 3, 0}, null);
        assertThat(exe.getStatus()).isEqualTo(415);
        assertThatThrownBy(() -> attachments.checkSize(51L * 1024 * 1024)).isInstanceOf(ForesignalException.class)
                .hasFieldOrPropertyWithValue("code", "UPLOAD_TOO_LARGE");
        assertThat(call(jo.bearer(), multipart("/v1/attachments")).getStatus()).isEqualTo(400);
        assertThat(upload(jo, "a.txt", "text/plain", "x".getBytes(StandardCharsets.UTF_8), "PURPLE").getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("identical uploads share one stored file, removed with the last attachment; others see nothing")
    void dedupAndVisibility() throws Exception {
        Person jo = person("jo-dup", Scope.READ, null);
        Person sam = person("sam-dup", Scope.READ, null);
        byte[] text = "lat,lon\n12.8,45.0\n".getBytes(StandardCharsets.UTF_8);
        JsonNode first = body(upload(jo, "points.csv", "text/csv", text, null));
        JsonNode second = body(upload(jo, "copy.csv", "text/csv", text, null));
        assertThat(first.get("sha256").asString()).isEqualTo(second.get("sha256").asString());
        assertThat(first.get("id").asString()).isNotEqualTo(second.get("id").asString());
        String path = attachmentRows.findById(first.get("id").asString()).orElseThrow().getStoragePath();
        assertThat(path).isEqualTo(attachmentRows.findById(second.get("id").asString()).orElseThrow()
                .getStoragePath());

        MockHttpServletResponse hidden = call(sam.bearer(), get("/v1/attachments/" + first.get("id").asString()));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(body(hidden).get("code").asString()).isEqualTo("UNKNOWN_ATTACHMENT");
        assertThat(call(sam.bearer(), delete("/v1/attachments/" + first.get("id").asString())).getStatus())
                .isEqualTo(404);

        assertThat(call(jo.bearer(), delete("/v1/attachments/" + first.get("id").asString())).getStatus())
                .isEqualTo(204);
        assertThat(blobs.exists(path)).isTrue();
        assertThat(call(jo.bearer(), delete("/v1/attachments/" + second.get("id").asString())).getStatus())
                .isEqualTo(204);
        assertThat(blobs.exists(path)).isFalse();
    }

    @Test
    @DisplayName("an attachment cited by an evidence item cannot be deleted")
    void inUse() throws Exception {
        Person jo = person("jo-use", Scope.READ, null);
        String id = body(upload(jo, "note.md", "text/markdown", "# Notice".getBytes(StandardCharsets.UTF_8), null))
                .get("id").asString();
        assertThat(call(jo.bearer(), json(post("/v1/evidence-items"), "{'title':'Notice','attachmentIds':['" + id
                + "']}")).getStatus()).isEqualTo(201);
        MockHttpServletResponse conflict = call(jo.bearer(), delete("/v1/attachments/" + id));
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(body(conflict).get("code").asString()).isEqualTo("ATTACHMENT_IN_USE");
    }
}
