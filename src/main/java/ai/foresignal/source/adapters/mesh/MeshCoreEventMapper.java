package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps MeshCore companion events to records and text messages (prompt 083): one mapper for both transports, the native
 * TCP link ({@link #onFrame}) and the bridge's JSON events ({@code MeshCoreJsonFormat} builds the same
 * {@link MeshCoreFrames} values and calls {@link #onContact} and friends). Pure apart from its own state: no I/O, the
 * time comes in as arguments.
 *
 * <ul>
 *   <li><strong>Contacts</strong> ({@code RESP_CODE_CONTACT}, {@code PUSH_CODE_NEW_ADVERT}) merge into the node's
 *       state: name, node type, position (×1e-6), and the advert's own timestamp as {@code advertTimestamp}. A contact
 *       answering a re-query after {@code PUSH_CODE_ADVERT}, or a new advert, is a live advert: its receive time is
 *       {@code lastAdvertAt}.
 *   <li><strong>Repeater health:</strong> for {@code nodeType=repeater}, {@code advertStatus} from {@link RepeaterHealth}
 *       against {@code lastAdvertAt} (else the advert timestamp); {@link #reevaluate} emits only repeaters whose status
 *       changed.
 *   <li><strong>The companion itself</strong> (self info, device info, battery) is a node too.
 *   <li><strong>Channel names</strong> are remembered from {@code RESP_CODE_CHANNEL_INFO}; the secret is never read.
 *   <li><strong>Text</strong> (contact and channel messages) becomes a {@link MeshTextMessage} for the bus, and a
 *       {@code mesh-message} record only with {@code includeText}.
 * </ul>
 *
 * <p>Every record keeps {@code observedAt} null: {@code observedAtMode} is RECEIVED, because node clocks drift.
 */
public final class MeshCoreEventMapper {

    private final String sourceId;
    private final boolean includeText;
    private final RepeaterHealth health;
    private final MeshNodeStates states = new MeshNodeStates();
    private final Map<Integer, String> channelNames = new ConcurrentHashMap<>();
    private final Map<String, Instant> pendingAdverts = new ConcurrentHashMap<>();
    private volatile String selfId;

    /**
     * What one event produced.
     *
     * @param records records for the sink
     * @param messages text messages for the bus
     */
    public record Mapped(List<NormalizedRecord> records, List<MeshTextMessage> messages) {

        /** Nothing. */
        public static final Mapped NONE = new Mapped(List.of(), List.of());

        static Mapped of(NormalizedRecord record) {
            return new Mapped(List.of(record), List.of());
        }
    }

    /**
     * Creates a mapper.
     *
     * @param sourceId the source (the observer id until the companion's own key is known)
     * @param includeText whether text becomes records
     * @param health the repeater thresholds
     */
    public MeshCoreEventMapper(String sourceId, boolean includeText, RepeaterHealth health) {
        this.sourceId = sourceId;
        this.includeText = includeText;
        this.health = health == null ? RepeaterHealth.defaults() : health;
    }

    /** Maps one companion frame received at {@code receivedAt}. */
    public Mapped onFrame(byte[] frame, Instant receivedAt) {
        int code = MeshCoreFrames.code(frame);
        switch (code) {
            case MeshCoreProtocol.RESP_CONTACT -> {
                MeshCoreFrames.Contact contact = MeshCoreFrames.contact(frame);
                if (contact == null) {
                    return Mapped.NONE;
                }
                Instant advertAt = pendingAdverts.remove(contact.publicKey());
                return Mapped.of(onContact(contact, advertAt, receivedAt));
            }
            case MeshCoreProtocol.PUSH_NEW_ADVERT -> {
                MeshCoreFrames.Contact contact = MeshCoreFrames.contact(frame);
                return contact == null ? Mapped.NONE : Mapped.of(onContact(contact, receivedAt, receivedAt));
            }
            case MeshCoreProtocol.PUSH_ADVERT -> {
                byte[] key = MeshCoreFrames.advertKey(frame);
                if (key != null) {
                    pendingAdverts.put(HexFormat.of().formatHex(key), receivedAt);
                    if (pendingAdverts.size() > 1_000) {
                        pendingAdverts.clear();
                    }
                }
                return Mapped.NONE;
            }
            case MeshCoreProtocol.RESP_SELF_INFO -> {
                MeshCoreFrames.SelfInfo self = MeshCoreFrames.selfInfo(frame);
                return self == null ? Mapped.NONE : Mapped.of(onSelfInfo(self, receivedAt));
            }
            case MeshCoreProtocol.RESP_DEVICE_INFO -> {
                MeshCoreFrames.DeviceInfo info = MeshCoreFrames.deviceInfo(frame);
                String self = selfId;
                if (info == null || self == null) {
                    return Mapped.NONE;
                }
                return Mapped.of(states.update(self, MeshRecords.MESHCORE, s -> MeshRecords.node(
                        s.identity(null, null, null, info.model(), info.version(), null).heard(receivedAt), null,
                        observer())));
            }
            case MeshCoreProtocol.RESP_BATT_AND_STORAGE -> {
                Integer millivolts = MeshCoreFrames.batteryMillivolts(frame);
                String self = selfId;
                if (millivolts == null || self == null) {
                    return Mapped.NONE;
                }
                return Mapped.of(states.update(self, MeshRecords.MESHCORE, s -> MeshRecords.node(
                        s.telemetry(null, millivolts / 1000.0, null, null, null).heard(receivedAt), null, observer())));
            }
            case MeshCoreProtocol.RESP_CHANNEL_INFO -> {
                MeshCoreFrames.ChannelName channel = MeshCoreFrames.channelName(frame);
                if (channel != null) {
                    onChannelName(channel.index(), channel.name());
                }
                return Mapped.NONE;
            }
            case MeshCoreProtocol.RESP_CONTACT_MSG_RECV, MeshCoreProtocol.RESP_CONTACT_MSG_RECV_V3,
                    MeshCoreProtocol.RESP_CHANNEL_MSG_RECV, MeshCoreProtocol.RESP_CHANNEL_MSG_RECV_V3 -> {
                MeshCoreFrames.TextMessage text = MeshCoreFrames.textMessage(frame);
                return text == null ? Mapped.NONE : onText(text, receivedAt);
            }
            default -> {
                return Mapped.NONE;
            }
        }
    }

    /**
     * Merges a contact.
     *
     * @param advertReceivedAt when its advert was heard live, or null for a contact-list entry
     * @param receivedAt when the event arrived
     */
    public NormalizedRecord onContact(MeshCoreFrames.Contact contact, Instant advertReceivedAt, Instant receivedAt) {
        String nodeType = MeshCoreProtocol.nodeType(contact.type());
        return states.update(contact.nodeId(), MeshRecords.MESHCORE, s -> {
            s.identity(contact.name(), null, null, null, null, nodeType)
                    .position(contact.latitude(), contact.longitude(), null)
                    .advert(contact.lastAdvert(), advertReceivedAt);
            if (advertReceivedAt != null) {
                s.heard(advertReceivedAt);
            }
            if ("repeater".equals(s.nodeType())) {
                s.advertStatus(health.status(reference(s), receivedAt));
            }
            return MeshRecords.node(s, null, observer());
        });
    }

    /** Merges the companion's own node. */
    public NormalizedRecord onSelfInfo(MeshCoreFrames.SelfInfo self, Instant receivedAt) {
        selfId = self.nodeId();
        return states.update(self.nodeId(), MeshRecords.MESHCORE, s -> MeshRecords.node(
                s.identity(self.name(), null, null, null, null, MeshCoreProtocol.nodeType(self.advType()))
                        .position(self.latitude(), self.longitude(), null).heard(receivedAt), null, observer()));
    }

    /** Remembers a channel slot's name (null forgets an empty slot). */
    public void onChannelName(int index, String name) {
        if (name == null || name.isBlank()) {
            channelNames.remove(index);
        } else {
            channelNames.put(index, name);
        }
    }

    /** The remembered name of a channel slot, or null. */
    public String channelName(int index) {
        return channelNames.get(index);
    }

    /** Maps a text message: always a bus message, a record only with {@code includeText}. */
    public Mapped onText(MeshCoreFrames.TextMessage text, Instant receivedAt) {
        String from = text.fromKeyPrefix() == null ? null : MeshCoreFrames.nodeId(text.fromKeyPrefix());
        MeshTextMessage message = new MeshTextMessage(sourceId, MeshRecords.MESHCORE,
                text.channel() ? text.channelIndex() : null,
                text.channel() && text.channelIndex() != null ? channelNames.get(text.channelIndex()) : null, from,
                text.channel() ? null : selfId, text.text(), receivedAt, null, text.snr());
        if (from != null) {
            states.update(from, MeshRecords.MESHCORE, s -> s.heard(receivedAt));
        }
        if (!includeText) {
            return new Mapped(List.of(), List.of(message));
        }
        String origin = from != null ? from : "mc:ch" + text.channelIndex();
        String id = origin + ":" + text.senderTimestamp().getEpochSecond() + ":"
                + MeshRecords.hash(text.text(), String.valueOf(text.channelIndex()));
        return new Mapped(List.of(MeshRecords.message(message, id, null)), List.of(message));
    }

    /** The records of repeaters whose {@code advertStatus} changed by {@code now}; called every 15 minutes. */
    public List<NormalizedRecord> reevaluate(Instant now) {
        List<NormalizedRecord> changed = new ArrayList<>();
        states.forEach(s -> {
            if (!"repeater".equals(s.nodeType())) {
                return;
            }
            String status = health.status(reference(s), now);
            if (!status.equals(s.advertStatus())) {
                s.advertStatus(status);
                changed.add(MeshRecords.node(s, null, observer()));
            }
        });
        return changed;
    }

    /** The node states (tests). */
    MeshNodeStates states() {
        return states;
    }

    /** The companion's own node id, once known. */
    public String selfId() {
        return selfId;
    }

    private static Instant reference(MeshNodeState s) {
        return s.lastAdvertAt() != null ? s.lastAdvertAt() : s.advertTimestamp;
    }

    private Observer observer() {
        String self = selfId;
        return Observer.of(self != null ? self : sourceId);
    }
}
