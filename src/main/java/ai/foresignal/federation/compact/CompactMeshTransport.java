package ai.foresignal.federation.compact;

import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeRegistry;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.SendDeferredException;
import ai.foresignal.notify.mesh.MeshChannelType;
import ai.foresignal.notify.mesh.MeshChannels;
import ai.foresignal.notify.mesh.MeshShortTextFormatter;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sends queued compact messages (prompt 113) on the 084 mesh channels that opted in with {@code compactEvents:
 * true}, through the channel's {@code profile} exactly as 084 sends an alert ({@link MeshChannelType#renderCompact})
 * and through the same {@code ChannelSender} → {@code MeshTransmitter} path, so every transmission passes the
 * channel's airtime governor.
 *
 * <p>Each channel gets the outbox's priority order within a byte budget per hour taken from its 084 duty-cycle
 * settings ({@code maxMessagesPerHour × 160} bytes); a message deferred by the governor goes back to the outbox
 * and the channel waits for the next round.
 */
@Component
public class CompactMeshTransport {

    private static final Logger log = LoggerFactory.getLogger(CompactMeshTransport.class);

    /** The budget window. */
    static final Duration WINDOW = Duration.ofHours(1);

    private final CompactOutboxService outbox;
    private final NotificationChannelRepository channels;
    private final ChannelTypeRegistry types;
    private final ChannelService channelService;
    private final ChannelSender sender;
    private final Clock clock;

    /** Creates the transport. */
    public CompactMeshTransport(CompactOutboxService outbox, NotificationChannelRepository channels,
            ChannelTypeRegistry types, ChannelService channelService, ChannelSender sender, Clock clock) {
        this.outbox = outbox;
        this.channels = channels;
        this.types = types;
        this.channelService = channelService;
        this.sender = sender;
        this.clock = clock;
    }

    /** One round over the opted-in mesh channels. */
    @Scheduled(fixedDelayString = "${foresignal.federation.compact-interval:30s}",
            initialDelayString = "${foresignal.federation.compact-interval:30s}")
    public void round() {
        if (outbox.outbox().size() == 0) {
            return;
        }
        List<NotificationChannelEntity> enabled;
        try {
            enabled = channels.findByEnabledTrue();
        } catch (RuntimeException e) {
            return;
        }
        for (NotificationChannelEntity channel : enabled) {
            if (!MeshChannels.TYPES.contains(channel.getType())) {
                continue;
            }
            Map<String, Object> config = channel.getConfig() == null ? Map.of() : channel.getConfig();
            if (!Boolean.TRUE.equals(config.get(MeshChannels.COMPACT_EVENTS))
                    || !(types.find(channel.getType()).orElse(null) instanceof MeshChannelType type)) {
                continue;
            }
            try {
                send(channel, type, config);
            } catch (RuntimeException e) {
                log.warn("Compact messages on mesh channel {} failed: {}", channel.getId(), e.getMessage());
            }
        }
    }

    /** Sends what fits {@code channel}'s budget. */
    void send(NotificationChannelEntity channel, MeshChannelType type, Map<String, Object> config) {
        MeshChannels.Common common = type.common(config);
        int budget = budgetBytes(common.limits().maxMessagesPerHour());
        Instant now = clock.instant();
        String transport = "mesh:" + channel.getId();
        List<CompactOutbox.Entry> due = outbox.outbox().take(transport, budget, WINDOW, now);
        for (int i = 0; i < due.size(); i++) {
            CompactOutbox.Entry entry = due.get(i);
            ChannelSettings settings = channelService.settingsFor(channel, 0, now);
            String text = CompactMessages.text(entry.message(), MeshShortTextFormatter.Limit.MESHCORE_CHANNEL.maxChars());
            PreparedRequest request = type.renderCompact(entry.encoded(), text,
                    CompactMessages.severityWire(entry.message().severity()), settings);
            try {
                sender.send(request);
            } catch (SendDeferredException e) {
                for (int j = i; j < due.size(); j++) {
                    outbox.outbox().unsend(transport, due.get(j));
                }
                return;
            } catch (IOException e) {
                log.info("A compact message on mesh channel {} was not sent: {}", channel.getId(), e.getMessage());
            }
        }
    }

    /** The bytes per hour of a channel allowed {@code maxMessagesPerHour} messages of at most 160 bytes. */
    static int budgetBytes(int maxMessagesPerHour) {
        return Math.max(1, maxMessagesPerHour) * MeshCborProfile.MAX_BYTES;
    }
}
