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

    @Test
    @DisplayName("a missing native library (UnsatisfiedLinkError, NoClassDefFoundError) disables WebP instead of failing")
    void linkageErrorDisables() {
        WebpSupport unsatisfied = new WebpSupport(() -> {
            throw new UnsatisfiedLinkError("no webp-imageio in java.library.path");
        });
        assertThat(unsatisfied.available()).isFalse();
        assertThat(unsatisfied.reason()).contains("UnsatisfiedLinkError");
        assertThat(unsatisfied.warnings()).hasSize(1);
        WebpSupport noClass = new WebpSupport(() -> {
            throw new NoClassDefFoundError("com/luciad/imageio/webp/WebPWriteParam");
        });
        assertThat(noClass.available()).isFalse();
        WebpSupport runtime = new WebpSupport(() -> {
            throw new IllegalStateException("no writer");
        });
        assertThat(runtime.available()).isFalse();
        assertThat(new WebpSupport(() -> { }).available()).isTrue();
    }
}
