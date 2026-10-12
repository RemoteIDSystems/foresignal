package ai.foresignal.security.pairing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.security.QrDecoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** SVG QR rendering with ZXing (prompt 023). */
class QrSvgRendererTest {

    private final QrSvgRenderer renderer = new QrSvgRenderer();

    @Test
    @DisplayName("the SVG decodes back to its payload with ZXing's reader")
    void roundTrip() throws Exception {
        String payload = "foresignal://pair?u=https%3A%2F%2Ffs.example&c=ABCD-EFGH";

        String svg = renderer.render(payload);

        assertThat(svg).startsWith("<?xml").contains("<svg xmlns=\"http://www.w3.org/2000/svg\"")
                .doesNotContain("<script").doesNotContain("href");
        assertThat(QrDecoder.decode(svg)).isEqualTo(payload);
    }

    @Test
    @DisplayName("non-ASCII payloads survive (UTF-8)")
    void unicode() throws Exception {
        assertThat(QrDecoder.decode(renderer.render("foresignal://pair?u=http%3A%2F%2Fcafé.local&c=1234-5678")))
                .isEqualTo("foresignal://pair?u=http%3A%2F%2Fcafé.local&c=1234-5678");
    }
}
