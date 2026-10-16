package ai.foresignal.notify;

import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaStore;
import ai.foresignal.media.ThumbnailService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameCommittedEvent;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.MapRenderer;
import ai.foresignal.raster.render.MapSpec;
import ai.foresignal.raster.render.StaticMapService;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Static map attachments of geolocated alerts (prompt 055; Weather-Imagery-Design §8).
 *
 * <p><b>Rendering, once per alert.</b> When an alert fires ({@link #prepare}, called by the router after the
 * {@code alert.fired} notification is persisted) and any enabled channel that accepts it has
 * {@code attachMap: static}, a background job starts at once, so it runs concurrently with prompt 030's
 * enrichment wait rather than after it:
 *
 * <ol>
 *   <li>it waits up to {@link #FRAME_WAIT} for a frame of each map product newer than the alert's
 *       {@code observed_at} (woken by {@link RasterFrameCommittedEvent}), so the map shows the weather
 *       that caused the alert, not the frame before;
 *   <li>renders once at {@value #WIDTH} px through {@link StaticMapService} ({@link MapRenderer});
 *   <li>stores the PNG as media ({@code role=map}, {@code expires_at = now + 7 d}, linked to the alert's
 *       record), reused by every channel that asked for the same products and radius.
 * </ol>
 * A {@code satellite-pass} alert attaches the pass composite's 256 px thumbnail (prompt 049) instead.
 *
 * <p><b>Sending</b> ({@link #attach}, called by the {@link DeliveryWorker} for a fired alert's send):
 *
 * <ul>
 *   <li>While the map is still being made, the send is held: {@link Outcome#pending()} asks the worker to
 *       look again shortly, up to {@link #MAP_HOLD} after the notification was created, and never longer
 *       than the channel's enrichment wait when it has one (the map never extends 030's limit). Past that
 *       the alert goes out as text.
 *   <li><b>Size control</b> for the types that send images ({@link ChannelCapabilities#attachments()}):
 *       the 800 px map if it fits {@code maxAttachmentBytes}, else a 600 px re-render, else the 256 px
 *       thumbnail, else text only. The chosen level and the media references are stored on the delivery
 *       ({@code attachment_level}, {@code attachments}); a retry loads the same bytes from the media store.
 *   <li>Every other type gets a {@code [Map](…)} link in the body to {@code {publicBaseUrl}/v1/media/{id}} (useful on the LAN
 *       or a VPN; no signed URLs in v1) and the attachment as metadata.
 *   <li>Nothing is attached to an alert whose payload the channel may not carry (a private source without
 *       {@code privatePayload}), and private-source products are left off the map for such a channel.
 * </ul>
 */
@Component
public class AlertMapService {

    private static final Logger log = LoggerFactory.getLogger(AlertMapService.class);

    /** The attachment width, pixels. */
    public static final int WIDTH = 800;

    /** The first downgrade width, pixels. */
    public static final int SMALL_WIDTH = 600;

    /** How long a job waits for a frame newer than the alert. */
    public static final Duration FRAME_WAIT = Duration.ofSeconds(5);

    /** How long a send is held for its map at most (the frame wait plus the 2-s render budget, plus slack). */
    public static final Duration MAP_HOLD = Duration.ofSeconds(8);

    /** How soon the worker looks again at a held send. */
    public static final Duration RECHECK = Duration.ofMillis(500);

    /** How long map media is kept. */
    public static final Duration MAP_RETENTION = Duration.ofDays(7);

    /** The attachment file name. */
    public static final String FILENAME = "map.png";

    /** The label of the link on types without images. */
    public static final String LINK_LABEL = "Map";

    /** The size-control levels (OpenAPI {@code NotificationDelivery.attachmentLevel}). */
    public static final String MAP_800 = "map-800";

    /** 600 px. */
    public static final String MAP_600 = "map-600";

    /** The 256 px thumbnail. */
    public static final String THUMBNAIL = "thumbnail";

    /** No image fits. */
    public static final String TEXT_ONLY = "text-only";

    /** The kind whose alerts attach the pass thumbnail. */
    static final String SATELLITE_PASS = "satellite-pass";

    /** How long finished jobs are remembered. */
    static final Duration JOB_TTL = Duration.ofHours(2);

    /** One map made for an alert: the spec (for the 600 px re-render) and the stored 800 px image. */
    record MapSet(MapSpec spec, MediaRow map, boolean thumbnailOnly) {}

    private record Job(CompletableFuture<Optional<MapSet>> future, Instant startedAt,
            Map<Integer, MediaRow> variants) {}

    /**
     * What {@link #attach} decided.
     *
     * @param message the message to send (with attachments or a link, or unchanged)
     * @param retryAt when to look again while the map is still being made, else null
     */
    public record Outcome(NotificationMessage message, Instant retryAt) {

        /** True when the send should wait for the map. */
        public boolean pending() {
            return retryAt != null;
        }
    }

    private final StaticMapService maps;
    private final SampleService samples;
    private final MediaStore media;
    private final ThumbnailService thumbnails;
    private final ChannelService channels;
    private final NotificationChannelRepository channelRepository;
    private final DeliveryStore deliveries;
    private final AlertStore alerts;
    private final ai.foresignal.security.feed.FeedKeyService feedKeys;
    private final ai.foresignal.settings.AppSettingService settings;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();
    private final Map<String, String> mediaKeys = new ConcurrentHashMap<>();
    private final Object frames = new Object();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Autowired(required = false)
    private EgressPolicy egress;

    /** Creates the service. */
    public AlertMapService(StaticMapService maps, SampleService samples, MediaStore media, ThumbnailService thumbnails,
            ChannelService channels, NotificationChannelRepository channelRepository, DeliveryStore deliveries,
            AlertStore alerts, ai.foresignal.security.feed.FeedKeyService feedKeys,
            ai.foresignal.settings.AppSettingService settings, JsonMapper json, Clock clock) {
        this.maps = maps;
        this.samples = samples;
        this.media = media;
        this.thumbnails = thumbnails;
        this.channels = channels;
        this.channelRepository = channelRepository;
        this.deliveries = deliveries;
        this.alerts = alerts;
        this.feedKeys = feedKeys;
        this.settings = settings;
        this.json = json;
        this.clock = clock;
    }

    /** Sets the egress policy (unit tests). */
    void egress(EgressPolicy policy) {
        this.egress = policy;
    }

    // ------------------------------------------------------------------ rendering

    /**
     * Starts the map jobs of a fired alert: one per distinct (products, radius) among the enabled channels
     * that accept {@code fired} and have {@code attachMap: static}.
     *
     * @return how many jobs were started (0 when no channel wants a map or the alert is not geolocated)
     */
    public int prepare(AlertView alert, NotificationMessage fired) {
        if (alert == null || fired == null || isPrivate(fired)) {
            return 0;
        }
        int started = 0;
        try {
            for (NotificationChannelEntity entity : channelRepository.findByEnabledTrue()) {
                ChannelMapConfig config = ChannelMapConfig.of(entity.getType(), entity.getConfig());
                if (!config.attach() || !NotificationChannel.of(entity, List.of()).accepts(fired)) {
                    continue;
                }
                List<String> products = products(alert, config, PrivatePayloads.optedIn(entity.getConfig()));
                String key = key(alert.id(), products, config.radiusKm());
                if (!jobs.containsKey(key) && job(alert, products, config.radiusKm(), key) != null) {
                    started++;
                }
            }
        } catch (RuntimeException e) {
            log.warn("Could not start the map jobs of alert {}: {}", alert.id(), e.getMessage());
        }
        return started;
    }

    /** The job of {@code key}, started when missing; null when the alert is not geolocated. */
    private Job job(AlertView alert, List<String> products, double radiusKm, String key) {
        prune();
        boolean pass = SATELLITE_PASS.equals(alert.kind());
        if (!pass && StaticMapService.alertGeometry(alert, json).isEmpty()) {
            return null;
        }
        return jobs.computeIfAbsent(key, k -> {
            CompletableFuture<Optional<MapSet>> future = new CompletableFuture<>();
            Job job = new Job(future, clock.instant(), new ConcurrentHashMap<>());
            executor.submit(() -> {
                try {
                    future.complete(pass ? passThumbnail(alert) : render(alert, products, radiusKm));
                } catch (RuntimeException | Error e) {
                    log.warn("The map of alert {} failed: {}", alert.id(), e.getMessage());
                    future.complete(Optional.empty());
                }
            });
            return job;
        });
    }

    /** Waits for newer frames, renders at 800 px and stores the map. */
    Optional<MapSet> render(AlertView alert, List<String> products, double radiusKm) {
        waitForFrames(products, alert.observedAt());
        StaticMapService.Request request = new StaticMapService.Request(products, List.of(), null, null, radiusKm,
                null, null, WIDTH, MapSpec.defaultHeight(WIDTH), alert, null, false, false);
        StaticMapService.Prepared prepared = maps.prepare(request, Set.of(), clock.instant());
        MapRenderer.Rendered rendered = maps.draw(prepared.spec());
        MediaRow row = store(rendered.png(), alert);
        return Optional.of(new MapSet(prepared.spec(), row, false));
    }

    /** The 256 px thumbnail of the pass's composite, when the pass has one. */
    Optional<MapSet> passThumbnail(AlertView alert) {
        JsonNode payload = snapshotPayload(alert);
        if (payload == null) {
            return Optional.empty();
        }
        for (JsonNode product : payload.path("products")) {
            JsonNode id = product.get("thumbMediaId");
            if (id != null && id.isString() && !id.asString().isBlank()) {
                return media.find(id.asString()).map(row -> new MapSet(null, row, true));
            }
        }
        return Optional.empty();
    }

    /** Blocks until every product has a frame newer than {@code observedAt}, or {@link #FRAME_WAIT} passed. */
    void waitForFrames(List<String> products, Instant observedAt) {
        if (observedAt == null || products.isEmpty()) {
            return;
        }
        long deadline = System.nanoTime() + FRAME_WAIT.toNanos();
        synchronized (frames) {
            while (!allNewer(products, observedAt)) {
                long left = deadline - System.nanoTime();
                if (left <= 0) {
                    return;
                }
                try {
                    TimeUnit.NANOSECONDS.timedWait(frames, Math.min(left, TimeUnit.MILLISECONDS.toNanos(250)));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private boolean allNewer(List<String> products, Instant observedAt) {
        for (String id : products) {
            Optional<RasterFrameRow> newest = samples.newestFrame(id);
            if (newest.isEmpty() || !newest.get().validTime().isAfter(observedAt)) {
                return false;
            }
        }
        return true;
    }

    /** Wakes the jobs waiting for a newer frame. */
    @EventListener
    public void onFrame(RasterFrameCommittedEvent event) {
        synchronized (frames) {
            frames.notifyAll();
        }
    }

    private MediaRow store(byte[] png, AlertView alert) {
        MediaInput input = MediaInput.of(png, "image/png", MediaRole.MAP)
                .withExpiresAt(clock.instant().plus(MAP_RETENTION));
        if (alert.sourceId() != null && alert.externalId() != null) {
            input = input.withRecord(alert.sourceId(), alert.externalId());
        }
        String id = media.store(input).id();
        return media.find(id).orElseThrow(() -> new IllegalStateException("map media " + id + " vanished"));
    }

    /** The 600 px variant of a map, rendered and stored once. */
    private MediaRow small(Job job, MapSet set, AlertView alert) {
        return job.variants().computeIfAbsent(SMALL_WIDTH, w -> {
            MapRenderer.Rendered rendered = maps.draw(set.spec().resized(SMALL_WIDTH, MapSpec.defaultHeight(SMALL_WIDTH)));
            return store(rendered.png(), alert);
        });
    }

    // ------------------------------------------------------------------ sending

    /**
     * Adds the alert's map to a send.
     *
     * @param d the claimed delivery
     * @param stored the stored notification
     * @param message the message as the channel would send it
     * @param channel the channel
     * @param type its type
     * @param now the send time
     */
    public Outcome attach(NotificationDeliveryEntity d, NotificationMessage stored, NotificationMessage message,
            NotificationChannelEntity channel, ChannelType type, Instant now) {
        if (!DeliveryKind.SEND.equals(d.getKind()) || stored.alertId() == null
                || !NotificationMessage.ALERT.equals(stored.event()) || !"alert.fired".equals(stored.subtype())
                || isPrivate(message)) {
            return new Outcome(message, null);
        }
        ChannelMapConfig config = ChannelMapConfig.of(channel.getType(), channel.getConfig());
        if (!config.attach()) {
            return new Outcome(message, null);
        }
        boolean images = imageCapable(type, channel);
        if (d.getAttachmentLevel() != null) {
            return new Outcome(fromStored(d, message, images), null);
        }
        Optional<AlertView> alert = alerts.find(stored.alertId());
        if (alert.isEmpty()) {
            return new Outcome(message, null);
        }
        List<String> products = products(alert.get(), config, PrivatePayloads.optedIn(channel.getConfig()));
        String key = key(alert.get().id(), products, config.radiusKm());
        Job job = jobs.get(key);
        if (job == null) {
            job = job(alert.get(), products, config.radiusKm(), key);
            if (job == null) {
                return new Outcome(message, null);
            }
        }
        if (!job.future().isDone()) {
            Instant created = stored.createdAt() == null ? d.getCreatedAt() : stored.createdAt();
            Duration hold = MAP_HOLD;
            if (channel.getWaitForEnrichmentSeconds() > 0) {
                Duration enrichment = Duration.ofSeconds(channel.getWaitForEnrichmentSeconds());
                hold = enrichment.compareTo(hold) < 0 ? enrichment : hold;
            }
            Instant deadline = created.plus(hold);
            if (now.isBefore(deadline)) {
                Instant next = now.plus(RECHECK);
                return new Outcome(message, next.isAfter(deadline) ? deadline : next);
            }
            return new Outcome(message, null);
        }
        Optional<MapSet> set = job.future().getNow(Optional.empty());
        if (set.isEmpty()) {
            return new Outcome(message, null);
        }
        Choice choice = images ? choose(job, set.get(), alert.get(), config.maxBytes())
                : new Choice(set.get().thumbnailOnly() ? THUMBNAIL : MAP_800, set.get().map());
        record(d, choice);
        return new Outcome(apply(message, choice, images), null);
    }

    /** A size-control decision: the level and the image (null for text only). */
    record Choice(String level, MediaRow row) {}

    /** 800 → 600 → thumbnail → text, the first that fits {@code cap}. */
    Choice choose(Job job, MapSet set, AlertView alert, long cap) {
        return downgrade(cap, set.thumbnailOnly(), set.map(),
                () -> small(job, set, alert),
                () -> thumbnails.thumbnail(set.map().id(), ThumbnailService.SMALL));
    }

    /**
     * The size-control rule (pure): the full map, then the 600 px map, then the 256 px thumbnail, then text
     * only, whichever is first at or under {@code cap}. For a pass thumbnail only the thumbnail and text.
     */
    static Choice downgrade(long cap, boolean thumbnailOnly, MediaRow full,
            java.util.function.Supplier<MediaRow> small, java.util.function.Supplier<MediaRow> thumbnail) {
        if (thumbnailOnly) {
            return full.bytes() <= cap ? new Choice(THUMBNAIL, full) : new Choice(TEXT_ONLY, null);
        }
        if (full.bytes() <= cap) {
            return new Choice(MAP_800, full);
        }
        MediaRow six = tryGet(small);
        if (six != null && six.bytes() <= cap) {
            return new Choice(MAP_600, six);
        }
        MediaRow thumb = tryGet(thumbnail);
        if (thumb != null && thumb.bytes() <= cap) {
            return new Choice(THUMBNAIL, thumb);
        }
        return new Choice(TEXT_ONLY, null);
    }

    private static MediaRow tryGet(java.util.function.Supplier<MediaRow> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            log.warn("A map variant could not be made: {}", e.getMessage());
            return null;
        }
    }

    private void record(NotificationDeliveryEntity d, Choice choice) {
        List<Map<String, Object>> refs = choice.row() == null ? List.of() : List.of(ref(choice.row()));
        d.setAttachmentLevel(choice.level());
        d.setAttachments(refs);
        if (d.getId() == null) {
            return;
        }
        deliveries.update(d.getId(), row -> {
            row.setAttachmentLevel(choice.level());
            row.setAttachments(refs);
        });
    }

    /** Records that the destination refused the image and the alert went out as text (ntfy 413). */
    public void refused(NotificationDeliveryEntity d) {
        d.setAttachmentLevel(TEXT_ONLY);
        if (d.getId() != null) {
            deliveries.update(d.getId(), row -> row.setAttachmentLevel(TEXT_ONLY));
        }
    }

    private Map<String, Object> ref(MediaRow row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("filename", FILENAME);
        out.put("contentType", row.contentType());
        out.put("bytes", row.bytes());
        out.put("mediaId", row.id());
        out.put("url", mediaUrl(row.id()));
        return out;
    }

    /** The message for a delivery whose level was chosen on an earlier attempt. */
    private NotificationMessage fromStored(NotificationDeliveryEntity d, NotificationMessage message, boolean images) {
        if (TEXT_ONLY.equals(d.getAttachmentLevel()) || d.getAttachments() == null || d.getAttachments().isEmpty()) {
            return message;
        }
        Object id = d.getAttachments().get(0).get("mediaId");
        Optional<MediaRow> row = id instanceof String s ? media.find(s) : Optional.empty();
        if (row.isEmpty()) {
            return message;
        }
        return apply(message, new Choice(d.getAttachmentLevel(), row.get()), images);
    }

    /** {@code message} with the chosen image (image types) or a link and metadata (the others). */
    NotificationMessage apply(NotificationMessage message, Choice choice, boolean images) {
        if (choice.row() == null) {
            return message;
        }
        MediaRow row = choice.row();
        String url = mediaUrl(row.id());
        if (images) {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(media.file(row));
            } catch (IOException | RuntimeException e) {
                log.warn("Map media {} could not be read; sending text: {}", row.id(), e.getMessage());
                return message;
            }
            return message.withAttachments(List.of(new NotificationMessage.Attachment(FILENAME, row.contentType(), bytes,
                    row.id(), bytes.length, url)));
        }
        return withLink(message.withAttachments(List.of(new NotificationMessage.Attachment(FILENAME, row.contentType(),
                null, row.id(), row.bytes(), url))), url);
    }

    /**
     * Appends a {@code [Map](url)} markdown link to the body. The body rather than a link button, because
     * every type renders the body while several (Mattermost, Gotify, Apprise, Home Assistant notify) render
     * no buttons.
     */
    public static NotificationMessage withLink(NotificationMessage message, String url) {
        String link = "[" + LINK_LABEL + "](" + url + ")";
        String body = message.bodyMarkdown() == null || message.bodyMarkdown().isBlank() ? link
                : message.bodyMarkdown() + "\n\n" + link;
        return message.withText(message.title(), body);
    }

    /** True when the type sends the image itself (an email channel in plain-text mode does not). */
    static boolean imageCapable(ChannelType type, NotificationChannelEntity channel) {
        if (!type.capabilities().attachments()) {
            return false;
        }
        return !("email".equals(channel.getType()) && channel.getConfig() != null
                && Boolean.TRUE.equals(channel.getConfig().get("plainTextOnly")));
    }

    // ------------------------------------------------------------------ helpers

    /**
     * The products of a channel's map: the configured ones, else the product the alert matched (a
     * {@code raster-frame} record's product), else {@code mrms-reflectivity}; only installed products, and
     * private-source products only for a channel that opted in to private payloads.
     */
    List<String> products(AlertView alert, ChannelMapConfig config, boolean privateOptIn) {
        List<String> out = new ArrayList<>();
        for (String id : new LinkedHashSet<>(config.productsOr(matchedProduct(alert)))) {
            Optional<ProductRegistry.Registered> product;
            try {
                product = Optional.of(samples.visible(id, Set.of()));
            } catch (RuntimeException e) {
                product = Optional.empty();
            }
            if (product.isEmpty() || product.get().spec().isExternal()) {
                continue;
            }
            if (egress != null && !egress.allowsNotificationPayload(product.get().sourceId(), privateOptIn)) {
                continue;
            }
            out.add(id);
        }
        return out;
    }

    /** The product an alert on a {@code raster-frame} record matched, or null. */
    String matchedProduct(AlertView alert) {
        if ("raster-frame".equals(alert.kind())) {
            JsonNode payload = snapshotPayload(alert);
            if (payload != null && payload.path("productId").isString()) {
                return payload.path("productId").asString();
            }
            return alert.externalId();
        }
        return null;
    }

    private JsonNode snapshotPayload(AlertView alert) {
        if (alert.recordSnapshot() == null) {
            return null;
        }
        try {
            JsonNode node = json.readTree(alert.recordSnapshot());
            JsonNode payload = node == null ? null : node.get("payload");
            return payload == null || !payload.isObject() ? null : payload;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** True for a message reduced by the private-payload rule (prompt 039): no map leaves with it. */
    static boolean isPrivate(NotificationMessage message) {
        return message.data() != null && Boolean.TRUE.equals(message.data().get("private"));
    }

    private String mediaUrl(String id) {
        String base;
        try {
            base = channels.serverSettings().normalizedPublicBaseUrl();
        } catch (RuntimeException e) {
            base = null;
        }
        if (base == null) {
            return NotificationRouter.DEFAULT_BASE_URL + "/v1/media/" + id;
        }
        if (imageLinksOn() && base.toLowerCase(java.util.Locale.ROOT).startsWith("https://")) {
            String key = mediaKey(id);
            if (key != null) {
                return base + "/v1/k/" + key + "/media/" + id;
            }
        }
        return base + "/v1/media/" + id;
    }

    /** True when {@code notifications.imageLinks} is on (prompt 108). */
    private boolean imageLinksOn() {
        try {
            return settings.getOrDefault(ai.foresignal.settings.SettingKeys.NOTIFICATIONS,
                    ai.foresignal.settings.NotificationSettings.class,
                    ai.foresignal.settings.NotificationSettings::defaults).imageLinksOn();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** A feed key bound to exactly one media path, read-only, expiring with the media (7 d; prompt 108). */
    private String mediaKey(String mediaId) {
        String cached = mediaKeys.get(mediaId);
        if (cached != null) {
            return cached;
        }
        try {
            ai.foresignal.security.feed.FeedKeyService.CreatedKey created = feedKeys.mint(
                    ai.foresignal.security.ApiTokenEntity.TYPE_FEED_KEY,
                    ai.foresignal.security.feed.FeedKeyService.KEY_PREFIX, "map-" + mediaId,
                    List.of("/v1/media/" + mediaId), ai.foresignal.security.TokenRestrictions.NONE,
                    clock.instant().plus(java.time.Duration.ofDays(7)), false);
            mediaKeys.put(mediaId, created.key());
            return created.key();
        } catch (RuntimeException e) {
            log.warn("Could not mint a media feed key for {}: {}", mediaId, e.getMessage());
            return null;
        }
    }

    static String key(long alertId, List<String> products, double radiusKm) {
        return alertId + "|" + String.join(",", products) + "|" + radiusKm;
    }

    private void prune() {
        Instant cutoff = clock.instant().minus(JOB_TTL);
        jobs.entrySet().removeIf(e -> e.getValue().future().isDone() && e.getValue().startedAt().isBefore(cutoff));
    }

    /** The geometry of an alert, if it has one (for callers deciding whether a map is possible). */
    public Optional<Geometry> geometry(AlertView alert) {
        return StaticMapService.alertGeometry(alert, json);
    }

    /** Waits for an alert's jobs to finish (tests). */
    boolean awaitJobs(long alertId, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        for (Map.Entry<String, Job> e : jobs.entrySet()) {
            if (!e.getKey().startsWith(alertId + "|")) {
                continue;
            }
            try {
                e.getValue().future().get(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    /** Stops the job executor. */
    @PreDestroy
    public void stop() {
        executor.shutdownNow();
    }
}
