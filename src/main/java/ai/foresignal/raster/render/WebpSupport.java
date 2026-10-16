package ai.foresignal.raster.render;

import ai.foresignal.system.SystemWarningContributor;
import ai.foresignal.system.SystemWarnings;
import java.awt.image.BufferedImage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The WebP health check (prompt 108): encode one 256×256 tile at startup; on failure WebP is disabled
 * with a reason, and {@code .webp} answers {@code 406 FORMAT_UNAVAILABLE}. The webp-imageio dependency
 * bundles libwebp natives; a missing native library (an unsupported platform) is the failure this
 * catches.
 */
@Component
public class WebpSupport implements SystemWarningContributor {

    private static final Logger log = LoggerFactory.getLogger(WebpSupport.class);

    /** The reason shown in {@code /v1/system} when WebP is unavailable. */
    public static final String REASON = "WebP is unavailable: the webp-imageio native library could not encode a tile";

    /** The warning code reported in {@code /v1/system} when WebP is disabled. */
    public static final String WARNING = "WEBP_UNAVAILABLE";

    private final boolean available;
    private final String reason;

    /** Runs the check once, encoding a 256×256 indexed probe tile. */
    public WebpSupport() {
        this(() -> {
            BufferedImage probe = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED,
                    new java.awt.image.IndexColorModel(8, 2, new byte[] {0, (byte) 255}, new byte[] {0, (byte) 255},
                            new byte[] {0, (byte) 255}, new byte[] {0, (byte) 255}));
            ImageEncoding.webp(probe, 1.0f);
        });
    }

    /**
     * Runs {@code probe} once. A native library that is missing or built for another platform fails with
     * a {@link LinkageError} ({@link UnsatisfiedLinkError}, {@link NoClassDefFoundError},
     * {@link ExceptionInInitializerError}), not a {@link RuntimeException}; both disable WebP instead of
     * failing the application context.
     */
    WebpSupport(Runnable probe) {
        boolean ok;
        String why = null;
        try {
            probe.run();
            ok = true;
        } catch (RuntimeException | LinkageError e) {
            ok = false;
            why = REASON + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
            log.warn("{}", why);
        }
        this.available = ok;
        this.reason = ok ? null : why;
    }

    /** True when WebP can be encoded. */
    public boolean available() {
        return available;
    }

    /** The reason WebP is disabled, or null. */
    public String reason() {
        return reason;
    }

    /** Reports the disabled WebP support to {@code /v1/system} (degraded). */
    @Override
    public List<SystemWarnings.Warning> warnings() {
        return available ? List.of() : List.of(new SystemWarnings.Warning(WARNING, "warning",
                reason == null ? REASON : reason));
    }
}
