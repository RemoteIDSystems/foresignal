package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaStore;
import ai.foresignal.media.ThumbnailService;
import ai.foresignal.notify.types.DiscordChannel;
import ai.foresignal.notify.types.PushoverChannel;
import ai.foresignal.notify.types.SlackChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.raster.render.StaticMapService;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * {@link AlertMapService} (prompt 055) over the real renderer, frames and media store of the prompt 042
 * raster context (no new context): the job waits for a frame newer than the alert (at most 5 s, woken by
 * the frame commit), renders once for three channels, stores the map as media expiring in 7 days, a
 * {@code satellite-pass} alert uses the pass thumbnail, a send is held for the map at most until the
 * channel's enrichment wait ends, and the size control picks a smaller image for a small cap. The channel
 * list, the alert lookup and the delivery store are stand-ins.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class AlertMapServiceIT extends RasterApiSupport {

    static final String SNAPSHOT = "{\"kind\":\"wx-area\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":"
            + "[[[-98,30],[-97,30],[-97,31],[-98,31],[-98,30]]]},\"payload\":{}}";

    @Autowired StaticMapService maps;
    @Autowired SampleService samples;
    @Autowired MediaStore media;
    @Autowired ThumbnailService thumbnails;
    @Autowired ChannelService channelService;

    final NotificationChannelRepository channelRepository = mock(NotificationChannelRepository.class);
    final DeliveryStore deliveries = mock(DeliveryStore.class);
    final AlertStore alerts = mock(AlertStore.class);
    final List<NotificationChannelEntity> channels = new ArrayList<>();
    AlertMapService service;

    @BeforeEach
    void setUp() {
        channels.clear();
        when(channelRepository.findByEnabledTrue()).thenReturn(channels);
        service = new AlertMapService(maps, samples, media, thumbnails, channelService, channelRepository, deliveries,
                alerts, json, clock);
    }

    @AfterEach
    void tearDown() {
        service.stop();
    }

    NotificationChannelEntity channel(String type, Map<String, Object> config) {
        NotificationChannelEntity c = new NotificationChannelEntity(UUID.randomUUID(), type, type);
        c.setEnabled(true);
        c.setEvents(new ArrayList<>(List.of("alert")));
        c.setConfig(new java.util.LinkedHashMap<>(config));
        channels.add(c);
        return c;
    }

    static Map<String, Object> mapConfig() {
        return Map.of("attachMap", "static", "mapProducts", List.of(OBS), "mapRadiusKm", 60);
    }

    AlertView alert(long id, String kind, String snapshot, Instant observedAt) {
        AlertView a = new AlertView(id, UUID.randomUUID(), "Storm", 100 + id, SOURCE, "e" + id, kind, observedAt,
                observedAt, "storm", "warning", "firing", 1, null, null, null, null, null, null, null, null, null, false,
                null, List.of(), snapshot);
        when(alerts.find(id)).thenReturn(Optional.of(a));
        return a;
    }

    NotificationMessage fired(AlertView a) {
        return NotificationMessage.builder("alert", "alert.fired", "Storm", "warning").id(500 + a.id())
                .alertId(a.id()).createdAt(clock.instant()).data(Map.of("alertId", a.id(), "sourceId", SOURCE)).build();
    }

    static NotificationDeliveryEntity send(NotificationChannelEntity c, NotificationMessage m) {
        return new NotificationDeliveryEntity(c.getId(), m.id(), "alert", m.createdAt());
    }

    @Test
    @Timeout(30)
    @DisplayName("waits for a frame newer than the alert (≤ 5 s, woken by the commit), then stores 7-day map media")
    void waitsForNewerFrame() throws Exception {
        commitObservation(T, 0);
        NotificationChannelEntity discord = channel("discord", mapConfig());
        AlertView a = alert(1, "wx-area", SNAPSHOT, T.plusSeconds(30));
        long started = System.nanoTime();
        assertThat(service.prepare(a, fired(a))).isEqualTo(1);
        Thread.sleep(500);
        assertThat(service.awaitJobs(1, Duration.ofMillis(10))).as("still waiting for a newer frame").isFalse();
        commitObservation(T.plusSeconds(120), 1);
        assertThat(service.awaitJobs(1, Duration.ofSeconds(10))).isTrue();
        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(5));
        NotificationMessage message = fired(a);
        AlertMapService.Outcome outcome = service.attach(send(discord, message), message, message, discord,
                new DiscordChannel(), clock.instant());
        assertThat(outcome.pending()).isFalse();
        NotificationMessage.Attachment attachment = outcome.message().image();
        assertThat(attachment).isNotNull();
        assertThat(attachment.filename()).isEqualTo("map.png");
        MediaRow row = media.find(attachment.mediaId()).orElseThrow();
        assertThat(row.role()).isEqualTo(MediaRole.MAP.wireValue());
        assertThat(row.width()).isEqualTo(AlertMapService.WIDTH);
        assertThat(row.expiresAt()).isEqualTo(clock.instant().plus(Duration.ofDays(7)));
        assertThat(row.recordSourceId()).isEqualTo(SOURCE);
        assertThat(row.recordExternalId()).isEqualTo("e1");
    }

    @Test
    @Timeout(30)
    @DisplayName("renders once for three channels: the same media for Discord, Telegram and a Slack link")
    void oncePerAlert() {
        commitObservation0();
        NotificationChannelEntity discord = channel("discord", mapConfig());
        NotificationChannelEntity telegram = channel("telegram", mapConfig());
        NotificationChannelEntity slack = channel("slack", mapConfig());
        AlertView a = alert(2, "wx-area", SNAPSHOT, T.minusSeconds(60));
        assertThat(service.prepare(a, fired(a))).as("one job for one (products, radius)").isEqualTo(1);
        assertThat(service.awaitJobs(2, Duration.ofSeconds(10))).isTrue();
        NotificationMessage m = fired(a);
        String d = service.attach(send(discord, m), m, m, discord, new DiscordChannel(), clock.instant()).message()
                .image().mediaId();
        String t = service.attach(send(telegram, m), m, m, telegram, new TelegramChannel(), clock.instant())
                .message().image().mediaId();
        NotificationMessage linked = service.attach(send(slack, m), m, m, slack, new SlackChannel(), clock.instant())
                .message();
        assertThat(t).isEqualTo(d);
        assertThat(linked.image()).as("Slack gets no bytes").isNull();
        assertThat(linked.attachments().get(0).mediaId()).isEqualTo(d);
        assertThat(linked.bodyMarkdown()).contains("[Map](").contains("/v1/media/" + d + ")");
    }

    @Test
    @Timeout(30)
    @DisplayName("a retry resends the bytes of the media the delivery stored, with no map job left to consult")
    void retryResendsStoredMedia() {
        // Checkpoint 057 (Weather-Imagery-Design §8): deliveries store media references so a retry resends the
        // same bytes. The second delivery belongs to an alert with no job at all, so only the stored
        // reference can supply the image.
        commitObservation0();
        NotificationChannelEntity telegram = channel("telegram", mapConfig());
        AlertView a = alert(4, "wx-area", SNAPSHOT, T.minusSeconds(60));
        assertThat(service.prepare(a, fired(a))).isEqualTo(1);
        assertThat(service.awaitJobs(4, Duration.ofSeconds(10))).isTrue();
        NotificationMessage m = fired(a);
        NotificationDeliveryEntity first = send(telegram, m);
        NotificationMessage.Attachment sent = service.attach(first, m, m, telegram, new TelegramChannel(),
                clock.instant()).message().image();
        assertThat(first.getAttachmentLevel()).isNotNull();
        assertThat(first.getAttachments()).isNotEmpty();

        AlertView other = alert(5, "wx-area", SNAPSHOT, T.minusSeconds(60));
        NotificationMessage retryMessage = fired(other);
        NotificationDeliveryEntity retry = send(telegram, retryMessage);
        retry.setAttachmentLevel(first.getAttachmentLevel());
        retry.setAttachments(first.getAttachments());
        NotificationMessage.Attachment resent = service.attach(retry, retryMessage, retryMessage, telegram,
                new TelegramChannel(), clock.instant()).message().image();
        assertThat(resent).isNotNull();
        assertThat(resent.mediaId()).isEqualTo(sent.mediaId());
        assertThat(resent.bytes()).isEqualTo(sent.bytes());
    }

    @Test
    @Timeout(30)
    @DisplayName("a satellite-pass alert attaches the pass composite's thumbnail instead of a map")
    void satellitePassThumbnail() {
        String thumb = media.store(MediaInput.of(NotifyFixtures.mapPng(), "image/png", MediaRole.THUMBNAIL)
                .withExpiresAt(clock.instant().plus(Duration.ofDays(1)))).id();
        NotificationChannelEntity telegram = channel("telegram", Map.of("attachMap", "static"));
        AlertView a = alert(3, "satellite-pass", "{\"kind\":\"satellite-pass\",\"geometry\":null,\"payload\":"
                + "{\"products\":[{\"composite\":\"MSU-MR 221\",\"thumbMediaId\":\"" + thumb + "\"}]}}", T);
        assertThat(service.prepare(a, fired(a))).isEqualTo(1);
        assertThat(service.awaitJobs(3, Duration.ofSeconds(10))).isTrue();
        NotificationMessage m = fired(a);
        NotificationDeliveryEntity d = send(telegram, m);
        NotificationMessage out = service.attach(d, m, m, telegram, new TelegramChannel(), clock.instant()).message();
        assertThat(out.image().mediaId()).isEqualTo(thumb);
        assertThat(d.getAttachmentLevel()).isEqualTo(AlertMapService.THUMBNAIL);
    }

    @Test
    @Timeout(30)
    @DisplayName("concurrent with the enrichment wait: held while the map is made, never past the channel's wait")
    void neverPastEnrichmentWait() {
        commitObservation0();
        NotificationChannelEntity discord = channel("discord", mapConfig());
        discord.setWaitForEnrichmentSeconds(3);
        // The alert is newer than every frame, so the job keeps waiting for one (up to 5 s of real time).
        AlertView a = alert(4, "wx-area", SNAPSHOT, T.plus(Duration.ofHours(1)));
        NotificationMessage m = fired(a);
        assertThat(service.prepare(a, m)).as("started at fire time, before any enrichment").isEqualTo(1);
        Instant created = m.createdAt();
        AlertMapService.Outcome held = service.attach(send(discord, m), m, m, discord, new DiscordChannel(),
                created.plusSeconds(1));
        assertThat(held.pending()).isTrue();
        assertThat(held.retryAt()).isBeforeOrEqualTo(created.plusSeconds(3));
        AlertMapService.Outcome late = service.attach(send(discord, m), m, m, discord, new DiscordChannel(),
                created.plusSeconds(3));
        assertThat(late.pending()).as("the 3-s enrichment wait is the limit").isFalse();
        assertThat(late.message().attachments()).isEmpty();
        NotificationChannelEntity plain = channel("pushover", mapConfig());
        AlertMapService.Outcome noWait = service.attach(send(plain, m), m, m, plain, new PushoverChannel(),
                created.plusSeconds(5));
        assertThat(noWait.pending()).as("without an enrichment wait the hold is 8 s").isTrue();
        assertThat(service.attach(send(plain, m), m, m, plain, new PushoverChannel(), created.plusSeconds(8))
                .pending()).isFalse();
    }

    @Test
    @Timeout(30)
    @DisplayName("size control with real media: a cap below the 800 px map picks a smaller image or text")
    void sizeControl() {
        commitObservation0();
        NotificationChannelEntity big = channel("discord", mapConfig());
        AlertView a = alert(5, "wx-area", SNAPSHOT, T.minusSeconds(60));
        service.prepare(a, fired(a));
        assertThat(service.awaitJobs(5, Duration.ofSeconds(10))).isTrue();
        NotificationMessage m = fired(a);
        long fullBytes = service.attach(send(big, m), m, m, big, new DiscordChannel(), clock.instant()).message()
                .image().size();
        Map<String, Object> capped = new java.util.LinkedHashMap<>(mapConfig());
        capped.put("maxAttachmentBytes", Math.max(1024, fullBytes - 1));
        NotificationChannelEntity small = channel("discord", capped);
        NotificationDeliveryEntity d = send(small, m);
        NotificationMessage out = service.attach(d, m, m, small, new DiscordChannel(), clock.instant()).message();
        assertThat(d.getAttachmentLevel()).isIn(AlertMapService.MAP_600, AlertMapService.THUMBNAIL,
                AlertMapService.TEXT_ONLY);
        if (out.image() != null) {
            assertThat(out.image().size()).isLessThan(fullBytes);
        }
        Map<String, Object> tiny = new java.util.LinkedHashMap<>(mapConfig());
        tiny.put("maxAttachmentBytes", 1024);
        NotificationChannelEntity none = channel("discord", tiny);
        NotificationDeliveryEntity t = send(none, m);
        assertThat(service.attach(t, m, m, none, new DiscordChannel(), clock.instant()).message().attachments())
                .isEmpty();
        assertThat(t.getAttachmentLevel()).isEqualTo(AlertMapService.TEXT_ONLY);
    }

    @Test
    @DisplayName("nothing is attached without attachMap, to a private-payload message, or to a lifecycle update")
    void notAttached() {
        NotificationChannelEntity off = channel("discord", Map.of());
        AlertView a = alert(6, "wx-area", SNAPSHOT, T);
        NotificationMessage m = fired(a);
        assertThat(service.attach(send(off, m), m, m, off, new DiscordChannel(), clock.instant()).message())
                .isSameAs(m);
        NotificationChannelEntity on = channel("discord", mapConfig());
        NotificationMessage redacted = PrivatePayloads.redact(m);
        assertThat(service.attach(send(on, m), m, redacted, on, new DiscordChannel(), clock.instant()).message())
                .isSameAs(redacted);
        NotificationDeliveryEntity update = send(on, m);
        update.setKind("update");
        assertThat(service.attach(update, m, m, on, new DiscordChannel(), clock.instant()).message()).isSameAs(m);
        when(alerts.find(anyLong())).thenReturn(Optional.empty());
        assertThat(service.prepare(alert(7, "earthquake", "{\"kind\":\"earthquake\",\"payload\":{}}", T),
                fired(a))).as("no geometry, no job").isZero();
    }

    private void commitObservation0() {
        try {
            commitObservation(T, 0);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
