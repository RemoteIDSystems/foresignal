package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.media.MediaRow;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The size control of map attachments (prompt 055, Weather-Imagery-Design §8): 800 px → 600 px → the
 * 256 px thumbnail → text only, by the channel's {@code maxAttachmentBytes}; the per-type default caps;
 * and the validation of the map keys in a channel's config.
 */
class AttachmentDowngradeTest {

    static MediaRow row(String id, long bytes) {
        return new MediaRow(id, "0".repeat(64), "image/png", 800, 600, bytes, "ab/x.png", "map", null, "src", "e1",
                null, Instant.EPOCH, null, null, null);
    }

    final MediaRow full = row("01FULL", 300_000);
    final MediaRow small = row("01SMALL", 180_000);
    final MediaRow thumb = row("01THUMB", 40_000);
    final AtomicInteger smallCalls = new AtomicInteger();
    final AtomicInteger thumbCalls = new AtomicInteger();
    final Supplier<MediaRow> smallSupplier = () -> {
        smallCalls.incrementAndGet();
        return small;
    };
    final Supplier<MediaRow> thumbSupplier = () -> {
        thumbCalls.incrementAndGet();
        return thumb;
    };

    @Test
    @DisplayName("800 px when it fits; nothing else is rendered")
    void fullFits() {
        AlertMapService.Choice c = AlertMapService.downgrade(8 * 1024 * 1024, false, full, smallSupplier, thumbSupplier);
        assertThat(c.level()).isEqualTo(AlertMapService.MAP_800);
        assertThat(c.row()).isSameAs(full);
        assertThat(smallCalls).hasValue(0);
        assertThat(thumbCalls).hasValue(0);
    }

    @Test
    @DisplayName("600 px when 800 is over the cap")
    void small() {
        AlertMapService.Choice c = AlertMapService.downgrade(200_000, false, full, smallSupplier, thumbSupplier);
        assertThat(c.level()).isEqualTo(AlertMapService.MAP_600);
        assertThat(c.row()).isSameAs(small);
        assertThat(thumbCalls).hasValue(0);
    }

    @Test
    @DisplayName("the 256 px thumbnail when 600 is over the cap too")
    void thumbnail() {
        AlertMapService.Choice c = AlertMapService.downgrade(50_000, false, full, smallSupplier, thumbSupplier);
        assertThat(c.level()).isEqualTo(AlertMapService.THUMBNAIL);
        assertThat(c.row()).isSameAs(thumb);
    }

    @Test
    @DisplayName("text only when nothing fits, or when a variant cannot be made")
    void textOnly() {
        AlertMapService.Choice c = AlertMapService.downgrade(10_000, false, full, smallSupplier, thumbSupplier);
        assertThat(c.level()).isEqualTo(AlertMapService.TEXT_ONLY);
        assertThat(c.row()).isNull();
        AlertMapService.Choice failing = AlertMapService.downgrade(10_000, false, full, () -> {
            throw new IllegalStateException("render failed");
        }, () -> {
            throw new IllegalStateException("thumbnail failed");
        });
        assertThat(failing.level()).isEqualTo(AlertMapService.TEXT_ONLY);
    }

    @Test
    @DisplayName("a satellite pass thumbnail is sent as is or not at all")
    void passThumbnail() {
        assertThat(AlertMapService.downgrade(50_000, true, thumb, smallSupplier, thumbSupplier).level())
                .isEqualTo(AlertMapService.THUMBNAIL);
        assertThat(AlertMapService.downgrade(10_000, true, thumb, smallSupplier, thumbSupplier).level())
                .isEqualTo(AlertMapService.TEXT_ONLY);
        assertThat(smallCalls).hasValue(0);
    }

    @Test
    @DisplayName("default caps per type: Discord 8 MB, Telegram 10 MB, ntfy 15 MB, Pushover 5,242,880, email 10 MB")
    void defaultCaps() {
        long mb = 1024L * 1024L;
        assertThat(ChannelMapConfig.defaultCap("discord")).isEqualTo(8 * mb);
        assertThat(ChannelMapConfig.defaultCap("telegram")).isEqualTo(10 * mb);
        assertThat(ChannelMapConfig.defaultCap("ntfy")).isEqualTo(15 * mb);
        assertThat(ChannelMapConfig.defaultCap("pushover")).isEqualTo(5_242_880L);
        assertThat(ChannelMapConfig.defaultCap("email")).isEqualTo(10 * mb);
        assertThat(ChannelMapConfig.of("pushover", Map.of()).maxBytes()).isEqualTo(5_242_880L);
        assertThat(ChannelMapConfig.of("pushover", Map.of("maxAttachmentBytes", 100_000)).maxBytes()).isEqualTo(100_000);
    }

    @Test
    @DisplayName("attachMap is off by default; products default to mrms-reflectivity or the matched product")
    void configDefaults() {
        ChannelMapConfig off = ChannelMapConfig.of("discord", Map.of());
        assertThat(off.attach()).isFalse();
        assertThat(off.radiusKm()).isEqualTo(50);
        assertThat(off.productsOr(null)).containsExactly("mrms-reflectivity");
        assertThat(off.productsOr("goes-east-c13")).containsExactly("goes-east-c13");
        ChannelMapConfig on = ChannelMapConfig.of("discord", Map.of("attachMap", "static", "mapProducts",
                List.of("mrms-reflectivity", "mrms-mesh-60m"), "mapRadiusKm", 80));
        assertThat(on.attach()).isTrue();
        assertThat(on.productsOr("x")).containsExactly("mrms-reflectivity", "mrms-mesh-60m");
        assertThat(on.radiusKm()).isEqualTo(80);
    }

    @Test
    @DisplayName("invalid map keys are reported per field")
    void validation() {
        List<FieldError> errors = new ArrayList<>();
        ChannelMapConfig.validate(Map.of("attachMap", "dynamic", "mapProducts", List.of("Bad Id"), "mapRadiusKm", 0,
                "maxAttachmentBytes", 12.5), errors);
        assertThat(errors).extracting(FieldError::field).containsExactlyInAnyOrder("config.attachMap",
                "config.mapProducts", "config.mapRadiusKm", "config.maxAttachmentBytes");
        List<FieldError> none = new ArrayList<>();
        ChannelMapConfig.validate(Map.of("attachMap", "static", "mapProducts", List.of("mrms-reflectivity"),
                "mapRadiusKm", 25, "maxAttachmentBytes", 1_000_000), none);
        assertThat(none).isEmpty();
    }

    @Test
    @DisplayName("a link-only message gets a [Map](url) link at the end of its body; the buttons are unchanged")
    void bodyLink() {
        NotificationMessage one = NotifyFixtures.message();
        NotificationMessage linked = AlertMapService.withLink(one, "https://fs.example/v1/media/01ABC");
        assertThat(linked.links()).isEqualTo(one.links());
        assertThat(linked.bodyMarkdown()).isEqualTo(one.bodyMarkdown() + "\n\n[Map](https://fs.example/v1/media/01ABC)");
        NotificationMessage empty = NotificationMessage.builder("alert", "alert.fired", "t", "info").build();
        assertThat(AlertMapService.withLink(empty, "https://x/m").bodyMarkdown()).isEqualTo("[Map](https://x/m)");
    }
}
