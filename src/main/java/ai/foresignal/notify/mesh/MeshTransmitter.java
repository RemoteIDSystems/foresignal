package ai.foresignal.notify.mesh;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelTypeRegistry;
import ai.foresignal.notify.DeliveryStore;
import ai.foresignal.notify.MeshDeliveryGateway;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.SendDeferredException;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The one way out to the mesh (prompt 084): every {@code meshtastic} and {@code meshcore} transmission (deliveries,
 * channel tests and field-report replies) comes here and passes the channel's {@link AirtimeGovernor} before it
 * reaches the radio path, so no path bypasses the governor.
 *
 * <ol>
 *   <li>The channel must exist, be enabled and be a mesh type.
 *   <li>The airtime is estimated ({@link LoRaAirtime}, the channel's preset, the message bytes plus the network's
 *       overhead) and offered to the governor. Deferred: {@link SendDeferredException} with reason {@code airtime}.
 *       Dropped: an {@link IOException}.
 *   <li>Admitted: Meshtastic publishes the downlink ({@link MeshtasticDownlinks}); MeshCore sends the companion
 *       command ({@link MeshCoreSender}), whose direct-message retry asks the governor again.
 *   <li>After a successful send the alert's short code is stored on the delivery row, and a digest's folded alerts
 *       are counted.
 * </ol>
 */
@Component
public class MeshTransmitter implements MeshDeliveryGateway {

    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final ChannelTypeRegistry types;
    private final AirtimeGovernors governors;
    private final MeshtasticDownlinks downlinks;
    private final MeshCoreSender meshcore;
    private final DeliveryStore deliveries;
    private final Clock clock;

    /**
     * Creates the transmitter.
     *
     * @param channels the stored channels
     * @param channelService decrypts the channel's secrets
     * @param types the channel types
     * @param governors the per-channel airtime governors
     * @param downlinks the Meshtastic downlink publisher
     * @param meshcore the MeshCore companion sender
     * @param deliveries stores the short code on the delivery row
     * @param clock the governor's clock
     */
    @Autowired
    public MeshTransmitter(NotificationChannelRepository channels, ChannelService channelService,
            ChannelTypeRegistry types, AirtimeGovernors governors, MeshtasticDownlinks downlinks,
            MeshCoreSender meshcore, DeliveryStore deliveries, Clock clock) {
        this.channels = channels;
        this.channelService = channelService;
        this.types = types;
        this.governors = governors;
        this.downlinks = downlinks;
        this.meshcore = meshcore;
        this.deliveries = deliveries;
        this.clock = clock;
    }

    @Override
    public ChannelResponse transmit(PreparedRequest.Mesh m) throws IOException {
        NotificationChannelEntity channel = channels.findById(m.channelId())
                .orElseThrow(() -> new IOException("the mesh channel " + m.channelId() + " is gone"));
        if (!channel.isEnabled()) {
            throw new IOException("the mesh channel " + m.channelId() + " is disabled");
        }
        if (!(types.find(channel.getType()).orElse(null) instanceof MeshChannelType type)) {
            throw new IOException("channel " + m.channelId() + " is not a mesh channel");
        }
        AirtimeGovernors.Entry entry;
        try {
            entry = governors.forChannel(channel, type);
        } catch (IllegalArgumentException e) {
            throw new IOException("the mesh channel's settings are not valid: " + e.getMessage());
        }
        double seconds = entry.radio().messageSeconds(m.network(), m.airtimeBytes());
        Instant now = clock.instant();
        AirtimeGovernor.Decision decision = entry.governor().tryAcquire(seconds, m.critical(), now);
        switch (decision) {
            case AirtimeGovernor.Drop drop -> {
                governors.dropped(channel.getId());
                throw new IOException("dropped by the airtime governor: " + drop.why());
            }
            case AirtimeGovernor.Defer defer -> {
                governors.deferred(channel.getId());
                throw new SendDeferredException(defer.until(), DeferredReason.AIRTIME,
                        "deferred by the airtime governor (" + defer.why() + ") until " + defer.until());
            }
            case AirtimeGovernor.Admit admit -> governors.airtime(channel.getId(), admit.seconds());
        }
        Map<String, Object> config = channel.getConfig() == null ? Map.of() : channel.getConfig();
        ChannelResponse response;
        if (MeshNetworks.MESHTASTIC.equals(m.network())) {
            response = downlinks.publish(config, channelService.secrets(channel), m);
        } else {
            BooleanSupplier retry = () -> {
                AirtimeGovernor.Decision again = entry.governor().tryAcquire(seconds, m.critical(), clock.instant());
                if (again instanceof AirtimeGovernor.Admit admitted) {
                    governors.airtime(channel.getId(), admitted.seconds());
                    return true;
                }
                if (again instanceof AirtimeGovernor.Drop) {
                    governors.dropped(channel.getId());
                } else {
                    governors.deferred(channel.getId());
                }
                return false;
            };
            response = meshcore.send(channel.getId(), config, m, retry);
        }
        if (m.shortCode() != null && m.deliveryId() > 0) {
            deliveries.update(m.deliveryId(), d -> d.setShortCode(m.shortCode()));
        }
        if (m.folded() > 0) {
            entry.governor().noteFolded(m.folded(), clock.instant());
            governors.folded(channel.getId(), m.folded());
        }
        return response;
    }
}
