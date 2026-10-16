package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The WebP health check: availability mirrors the writer and a disabled check reports a warning (prompt 108). */
class WebpHealthCheckTest {

    @Test
    @DisplayName("availability matches the registered WebP writer, and a warning is reported only when disabled")
    void health() {
        WebpSupport support = new WebpSupport();
        assertThat(support.available()).isEqualTo(ImageEncoding.webpAvailable());
        if (support.available()) {
            assertThat(support.reason()).isNull();
            assertThat(support.warnings()).isEmpty();
        } else {
            assertThat(support.reason()).isNotBlank();
            assertThat(support.warnings()).hasSize(1);
            assertThat(support.warnings().get(0).code()).isEqualTo(WebpSupport.WARNING);
        }
    }
}
