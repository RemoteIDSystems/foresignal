package ai.foresignal.notify.mesh.report;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeRegistry;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.SendDeferredException;
import ai.foresignal.notify.Truncator;
import ai.foresignal.notify.mesh.AirtimeGovernors;
import ai.foresignal.notify.mesh.MeshChannelType;
import ai.foresignal.notify.mesh.MeshChannels;
import ai.foresignal.notify.mesh.MeshNetworks;
import ai.foresignal.notify.mesh.MeshShortCode;
import ai.foresignal.security.Scope;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.adapters.mesh.MeshMessageBus;
import ai.foresignal.source.adapters.mesh.MeshRecords;
import ai.foresignal.source.adapters.mesh.MeshReportsAdapter;
import ai.foresignal.source.adapters.mesh.MeshTextMessage;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.stats.glance.GlanceData;
import ai.foresignal.stats.glance.GlanceService;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Field reports and commands from the operator's own mesh (prompt 084), read from 083's {@link MeshMessageBus}.
 *
 * <p><strong>Who is heard.</strong> A message is considered only when all of these hold:
 * <ul>
 *   <li>it arrived on a channel (never a direct message) whose network and index match an enabled {@code meshtastic}
 *       or {@code meshcore} channel with {@code reportsEnabled=true} (the private channel, never 0);
 *   <li>its sender is in that channel's {@code allowedSenders}: a Meshtastic message by its node id, a MeshCore
 *       channel message by {@code name:} and the name before its {@code ": "} (MeshCore does not report a channel
 *       message's sender key);
 *   <li>its text starts with {@code #E } ({@link MeshReportGrammar});
 *   <li>the sender sent at most {@value #COMMANDS_PER_MINUTE} commands in the last minute; extras are ignored and
 *       counted ({@code foresignal.channel.mesh.dropped}).
 * </ul>
 * Anything else is ignored silently: no reply goes to an unknown sender.
 *
 * <p><strong>Commands.</strong> {@code R} records a {@code field-report} in the private {@code mesh-reports} source
 * (an {@code @place} resolves through 036's places) and answers {@code OK R{ref} {TYPE}}; {@code ACK} (role
 * {@code ack} only) acknowledges the alert behind the short code as {@code mesh:<sender>}, writes an audit event and
 * answers {@code OK ACK #{code}}; {@code STATUS} answers a summary of at most 133 characters from the glance (038),
 * else the open-alert counts; {@code HELP} answers the grammar card; a malformed command answers
 * {@code ERR {reason}}. Replies go out on the same channel through the airtime governor; a reply the governor defers
 * is dropped and counted.
 *
 * <p>Report text is untrusted data: it is stored as the payload's {@code text}, never embedded, and never acted on.
 * Commands are handled on virtual threads, so the bus is never blocked by a reply's transmission.
 */
@Component
public class MeshReportService {

    private static final Logger log = LoggerFactory.getLogger(MeshReportService.class);

    /** The most commands one sender may send per minute. */
    public static final int COMMANDS_PER_MINUTE = 10;

    /** The actor kind of a mesh acknowledgement in the audit log. */
    public static final String ACTOR_KIND = "mesh";

    /** The longest STATUS reply. */
    public static final int STATUS_MAX = 133;

    /** How long a report waits for the ingest writer. */
    static final Duration INGEST_WAIT = Duration.ofSeconds(10);

    /** A MeshCore channel text: {@code name: text}. */
    private static final Pattern MESHCORE_SENDER = Pattern.compile("^([^:]{1,32}): (.*)$", Pattern.DOTALL);

    private final MeshMessageBus bus;
    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final ChannelTypeRegistry types;
    private final ChannelSender sender;
    private final NotificationDeliveryRepository deliveries;
    private final AlertService alerts;
    private final AlertStore alertStore;
    private final AuditService audit;
    private final IngestQueue queue;
    private final ObjectProvider<PlacePoints> places;
    private final ObjectProvider<GlanceService> glance;
    private final AirtimeGovernors governors;
    private final Clock clock;
    private final Map<String, ArrayDequeWindow> rates = new ConcurrentHashMap<>();
    private final AtomicLong arrival = new AtomicLong();
    private final ExecutorService work = Executors.newVirtualThreadPerTaskExecutor();
    private volatile AutoCloseable subscription;

    /**
     * Creates the service.
     *
     * @param bus 083's mesh message bus
     * @param channels the stored channels
     * @param channelService the channel settings for replies
     * @param types the channel types
     * @param sender sends replies (through the mesh transmitter and its governor)
     * @param deliveries finds the delivery behind a short code
     * @param alerts acknowledges alerts
     * @param alertStore counts open alerts for STATUS
     * @param audit records mesh acknowledgements
     * @param queue the ingest writer's queue for report records
     * @param places resolves {@code @place}, resolved lazily
     * @param glance the STATUS summary, resolved lazily
     * @param governors counts over-rate commands and dropped replies
     * @param clock the clock
     */
    @Autowired
    public MeshReportService(MeshMessageBus bus, NotificationChannelRepository channels, ChannelService channelService,
            ChannelTypeRegistry types, ChannelSender sender, NotificationDeliveryRepository deliveries,
            AlertService alerts, AlertStore alertStore, AuditService audit, IngestQueue queue,
            ObjectProvider<PlacePoints> places, ObjectProvider<GlanceService> glance, AirtimeGovernors governors,
            Clock clock) {
        this.bus = bus;
        this.channels = channels;
        this.channelService = channelService;
        this.types = types;
        this.sender = sender;
        this.deliveries = deliveries;
        this.alerts = alerts;
        this.alertStore = alertStore;
        this.audit = audit;
        this.queue = queue;
        this.places = places;
        this.glance = glance;
        this.governors = governors;
        this.clock = clock;
    }

    /** Subscribes to the mesh message bus once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.MESH)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe(message -> work.execute(() -> {
                try {
                    handle(message);
                } catch (RuntimeException e) {
                    log.warn("A mesh field-report command failed", e);
                }
            }));
        }
    }

    /** Unsubscribes and stops the command threads. */
    @PreDestroy
    public synchronized void stop() {
        if (subscription != null) {
            try {
                subscription.close();
            } catch (Exception e) {
                log.debug("Closing the mesh bus subscription failed", e);
            }
            subscription = null;
        }
        work.shutdownNow();
    }

    /**
     * What one message led to (tests).
     *
     * @param outcome {@code ignored}, {@code rate-limited} or {@code handled}
     * @param channelId the channel that handled it, or null
     * @param reply the reply text, or null
     * @param replySent whether the reply was transmitted
     */
    public record Outcome(String outcome, UUID channelId, String reply, boolean replySent) {

        static Outcome ignored() {
            return new Outcome("ignored", null, null, false);
        }
    }

    /** Handles one bus message synchronously. */
    public Outcome handle(MeshTextMessage message) {
        if (message.direct() || message.channelIndex() == null || message.channelIndex() == 0) {
            return Outcome.ignored();
        }
        String senderKey;
        String text;
        if (MeshNetworks.MESHCORE.equals(message.network()) && message.fromId() == null) {
            Matcher m = MESHCORE_SENDER.matcher(message.text());
            if (!m.matches()) {
                return Outcome.ignored();
            }
            senderKey = MeshChannels.senderKey("name:" + m.group(1).strip());
            text = m.group(2);
        } else {
            senderKey = MeshChannels.senderKey(message.fromId());
            text = message.text();
        }
        if (senderKey == null) {
            return Outcome.ignored();
        }
        MeshReportGrammar.Command command = MeshReportGrammar.parse(text);
        if (command == null) {
            return Outcome.ignored();
        }
        Match match = match(message, senderKey);
        if (match == null) {
            return Outcome.ignored();
        }
        Instant now = clock.instant();
        ArrayDequeWindow window = rates.computeIfAbsent(message.network() + "|" + senderKey,
                k -> new ArrayDequeWindow(COMMANDS_PER_MINUTE, Duration.ofMinutes(1)));
        if (!window.tryAcquire(now)) {
            governors.dropped(match.channel().getId());
            return new Outcome("rate-limited", match.channel().getId(), null, false);
        }
        String reply = execute(command, match, senderKey, message, now);
        boolean sent = reply(match, reply);
        return new Outcome("handled", match.channel().getId(), reply, sent);
    }

    /** The channel and role that admit {@code senderKey} on the message's network and index. */
    private record Match(NotificationChannelEntity channel, MeshChannelType type, String role) {}

    private Match match(MeshTextMessage message, String senderKey) {
        for (NotificationChannelEntity channel : channels.findAll()) {
            if (!channel.isEnabled() || !message.network().equals(channel.getType())
                    || !(types.find(channel.getType()).orElse(null) instanceof MeshChannelType type)) {
                continue;
            }
            Map<String, Object> config = channel.getConfig() == null ? Map.of() : channel.getConfig();
            MeshChannels.Common common;
            try {
                common = type.common(config);
            } catch (RuntimeException e) {
                continue;
            }
            if (!common.reportsEnabled() || common.channelIndex() != message.channelIndex()) {
                continue;
            }
            String role = common.allowedSenders().get(senderKey);
            if (role != null) {
                return new Match(channel, type, role);
            }
        }
        return null;
    }

    private String execute(MeshReportGrammar.Command command, Match match, String senderKey, MeshTextMessage message,
            Instant now) {
        return switch (command) {
            case MeshReportGrammar.Report r -> report(r, match, senderKey, message, now);
            case MeshReportGrammar.Ack a -> ack(a, match, senderKey, now);
            case MeshReportGrammar.Status s -> status();
            case MeshReportGrammar.Help h -> MeshReportGrammar.HELP;
            case MeshReportGrammar.Invalid i -> "ERR " + i.reason();
        };
    }

    private String report(MeshReportGrammar.Report r, Match match, String senderKey, MeshTextMessage message,
            Instant now) {
        Double lat = r.latitude();
        Double lon = r.longitude();
        if (r.place() != null) {
            PlacePoints lookup = places.getIfAvailable();
            Optional<double[]> point = lookup == null ? Optional.empty() : lookup.point(r.place());
            if (point.isEmpty()) {
                return "ERR unknown place " + r.place();
            }
            lon = point.get()[0];
            lat = point.get()[1];
        }
        String hash = MeshRecords.hash(message.network(), senderKey, message.receivedAt().toString(), r.text());
        String ref = MeshShortCode.of(Long.parseLong(hash.substring(0, 5), 16));
        NormalizedRecord record = record("r-" + hash, r, lat, lon, senderKey, message);
        try {
            queue.submit(MeshReportsAdapter.ID, FetchLogEntry.ok(MeshReportsAdapter.ID, FetchTrigger.PUSH, now, now),
                    List.of(new IngestRecord(record, now, arrival.incrementAndGet())), INGEST_WAIT)
                    .get(INGEST_WAIT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ERR not recorded";
        } catch (Exception e) {
            log.warn("A mesh field report from {} was not recorded: {}", senderKey, e.toString());
            return "ERR not recorded";
        }
        return "OK R" + ref + " " + r.type();
    }

    /** The {@code field-report} record of a report. */
    static NormalizedRecord record(String externalId, MeshReportGrammar.Report r, Double lat, Double lon,
            String senderKey, MeshTextMessage message) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, MeshReportsAdapter.KIND)
                .eventTime(message.receivedAt())
                .put("type", r.type())
                .put("text", r.text())
                .put("reporter", senderKey)
                .put("network", message.network())
                .put("channel", message.channelIndex())
                .observers(List.of(new Observer(senderKey, null, message.rssi(), message.snr(), null)));
        if (lat != null && lon != null) {
            b.point(lon, lat);
        }
        return b.build();
    }

    private String ack(MeshReportGrammar.Ack a, Match match, String senderKey, Instant now) {
        if (!MeshChannels.ROLE_ACK.equals(match.role())) {
            return "ERR not allowed";
        }
        List<UUID> meshChannels = new ArrayList<>();
        for (NotificationChannelEntity channel : channels.findAll()) {
            if (MeshChannels.TYPES.contains(channel.getType())) {
                meshChannels.add(channel.getId());
            }
        }
        Optional<NotificationDeliveryEntity> delivery = meshChannels.isEmpty() ? Optional.empty()
                : deliveries.findFirstByChannelIdInAndShortCodeOrderByIdDesc(meshChannels, a.code());
        if (delivery.isEmpty() || delivery.get().getAlertId() == null) {
            return "ERR unknown code " + a.code();
        }
        long alertId = delivery.get().getAlertId();
        try {
            alerts.acknowledge(alertId, AlertService.Transition.NONE, "mesh:" + senderKey);
        } catch (RuntimeException e) {
            return "ERR unknown code " + a.code();
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("network", match.type().network());
        detail.put("channelId", match.channel().getId().toString());
        detail.put("code", a.code());
        audit.record(new AuditRecord(now, ACTOR_KIND, null, "mesh:" + senderKey, null, null, null, null,
                "meshAcknowledgeAlert", null, "alerts", Long.toString(alertId), null, detail));
        return "OK ACK #" + a.code();
    }

    /** The STATUS line: the glance's status and open counts, else the open-alert counts. */
    String status() {
        Map<String, Long> open = null;
        String status = null;
        GlanceService summary = glance.getIfAvailable();
        if (summary != null) {
            try {
                GlanceData data = summary.data(new GlanceService.Request(
                        new ReadCaller(Scope.ADMIN, false, true, ReadRestriction.NONE, true), Set.of(), null));
                status = data.status();
                open = data.open();
            } catch (RuntimeException e) {
                log.debug("The glance is unavailable for #E STATUS", e);
            }
        }
        if (open == null) {
            try {
                open = alertStore.countBySeverity(new AlertQuery(null, null, null, null, false, List.of("firing"),
                        null, null, null, false, Set.of()));
            } catch (RuntimeException e) {
                return "ERR status unavailable";
            }
        }
        long critical = open.getOrDefault("critical", 0L);
        long warning = open.getOrDefault("warning", 0L);
        long info = open.getOrDefault("info", 0L);
        String line = (status == null ? "" : status + ": ") + "open " + critical + " crit, " + warning + " warn, "
                + info + " info";
        return line.length() > STATUS_MAX ? line.substring(0, STATUS_MAX) : line;
    }

    /** Sends {@code text} on the match's channel through the governor; false when it was deferred or failed. */
    private boolean reply(Match match, String text) {
        String fitted = Truncator.truncate(text, 200, Truncator.Unit.UTF8_BYTES, Truncator.Mode.PLAIN, null);
        try {
            ChannelSettings settings = channelService.settingsFor(match.channel(), 0, clock.instant());
            PreparedRequest request = match.type().renderReply(fitted, settings);
            return sender.send(request).success();
        } catch (SendDeferredException e) {
            governors.dropped(match.channel().getId());
            governors.find(match.channel().getId()).ifPresent(g -> g.governor().noteDropped(clock.instant()));
            log.info("A mesh reply on channel {} was dropped: {}", match.channel().getId(), e.getMessage());
            return false;
        } catch (IOException | RuntimeException e) {
            log.warn("A mesh reply on channel {} failed: {}", match.channel().getId(), e.getMessage());
            return false;
        }
    }

    /** A per-sender sliding window of command times. */
    static final class ArrayDequeWindow {

        private final int limit;
        private final Duration window;
        private final java.util.ArrayDeque<Instant> times = new java.util.ArrayDeque<>();

        ArrayDequeWindow(int limit, Duration window) {
            this.limit = limit;
            this.window = window;
        }

        synchronized boolean tryAcquire(Instant now) {
            Instant from = now.minus(window);
            while (!times.isEmpty() && !times.peekFirst().isAfter(from)) {
                times.removeFirst();
            }
            if (times.size() >= limit) {
                return false;
            }
            times.addLast(now);
            return true;
        }
    }
}
