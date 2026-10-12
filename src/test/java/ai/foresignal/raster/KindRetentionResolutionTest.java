package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Per-kind retention resolution (prompt 019's hook, which 042 relies on for {@code raster-frame}):
 * {@code retention.kinds.{kind}} → {@code retention.sources.{id}} → descriptor
 * {@code kindRetention[kind]} → descriptor {@code defaultRetention} → {@code retention.default}.
 */
class KindRetentionResolutionTest {

    static final Duration DEFAULT = Duration.ofDays(7);

    static RetentionSettings settings(Map<String, Duration> kinds) {
        return new RetentionSettings(Duration.ofDays(30), null, null, null, Duration.ofDays(3), null, null, null, null,
                kinds);
    }

    @Test
    @DisplayName("each step of the order wins over the ones after it")
    void resolutionOrder() {
        SourceDescriptor product = SyntheticRasterAdapter.buildDescriptor();
        String kind = ProductSpec.RECORD_KIND;
        // 3. the descriptor's kindRetention (the 7d raster default) beats retention.default (30d).
        assertThat(RetentionResolver.resolve(product, kind, Optional.empty(), settings(Map.of()))).isEqualTo(DEFAULT);
        // 2. the per-source override beats the descriptor.
        assertThat(RetentionResolver.resolve(product, kind, Optional.of(Duration.ofDays(2)), settings(Map.of())))
                .isEqualTo(Duration.ofDays(2));
        // 1. retention.kinds beats everything.
        assertThat(RetentionResolver.resolve(product, kind, Optional.of(Duration.ofDays(2)),
                settings(Map.of(kind, Duration.ofDays(14))))).isEqualTo(Duration.ofDays(14));
        // 5. no kind entry, no override, no default: retention.default.
        assertThat(RetentionResolver.resolve(product, "other-kind", Optional.empty(), settings(Map.of())))
                .isEqualTo(Duration.ofDays(30));
    }

    @Test
    @DisplayName("the descriptor's defaultRetention sits between kindRetention and retention.default")
    void descriptorDefault() {
        SourceDescriptor withDefault = SourceDescriptor.builder("kinds-src").kinds("a", "b")
                .mode(ai.foresignal.source.spi.Mode.PUSH).defaultRetention(Duration.ofDays(90))
                .kindRetention("a", Duration.ofDays(400)).build();
        assertThat(RetentionResolver.resolve(withDefault, "a", Optional.empty(), settings(Map.of())))
                .isEqualTo(Duration.ofDays(400));
        assertThat(RetentionResolver.resolve(withDefault, "b", Optional.empty(), settings(Map.of())))
                .isEqualTo(Duration.ofDays(90));
        assertThat(RetentionResolver.resolve(withDefault, null, Optional.empty(), settings(Map.of("a", DEFAULT))))
                .as("the source-level window ignores kinds").isEqualTo(Duration.ofDays(90));
    }
}
