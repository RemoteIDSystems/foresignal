package ai.foresignal.source.adapters.mesh;

import java.time.Instant;

/**
 * The merged state of one mesh node (prompt 083, adapter rule 7): nodeinfo, position and telemetry arrive in separate
 * messages, and each record must be a complete snapshot, so every message updates this state and the record is built
 * from all of it. A telemetry-only message therefore still carries the node's last known position.
 *
 * <p>Not thread-safe on its own; {@link MeshNodeStates} hands it out under its lock.
 */
public final class MeshNodeState {

    private final String externalId;
    private final String network;
    String longName;
    String shortName;
    String role;
    String hwModel;
    String firmware;
    String nodeType;
    Double latitude;
    Double longitude;
    Integer positionPrecisionBits;
    Integer batteryLevel;
    Double voltage;
    Double channelUtil;
    Double airUtilTx;
    Long uptimeS;
    Instant lastHeardAt;
    String advertStatus;
    Instant lastAdvertAt;
    Instant advertTimestamp;

    /**
     * Creates an empty state.
     *
     * @param externalId the node id ({@code !xxxxxxxx} or {@code mc:} + 12 hex)
     * @param network {@code meshtastic} or {@code meshcore}
     */
    public MeshNodeState(String externalId, String network) {
        this.externalId = externalId;
        this.network = network;
    }

    /** The node id. */
    public String externalId() {
        return externalId;
    }

    /** The network. */
    public String network() {
        return network;
    }

    /** True once a position was merged. */
    public boolean hasPosition() {
        return latitude != null && longitude != null;
    }

    /** Merges node identity; null arguments keep the current value. */
    public MeshNodeState identity(String longName, String shortName, String role, String hwModel, String firmware,
            String nodeType) {
        this.longName = keep(longName, this.longName);
        this.shortName = keep(shortName, this.shortName);
        this.role = keep(role, this.role);
        this.hwModel = keep(hwModel, this.hwModel);
        this.firmware = keep(firmware, this.firmware);
        this.nodeType = keep(nodeType, this.nodeType);
        return this;
    }

    /** Merges a position; a null latitude or longitude keeps the previous position. */
    public MeshNodeState position(Double latitude, Double longitude, Integer precisionBits) {
        if (latitude != null && longitude != null) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.positionPrecisionBits = precisionBits;
        }
        return this;
    }

    /** Merges device telemetry; null arguments keep the current value. */
    public MeshNodeState telemetry(Integer batteryLevel, Double voltage, Double channelUtil, Double airUtilTx,
            Long uptimeS) {
        this.batteryLevel = batteryLevel != null ? batteryLevel : this.batteryLevel;
        this.voltage = voltage != null ? voltage : this.voltage;
        this.channelUtil = channelUtil != null ? channelUtil : this.channelUtil;
        this.airUtilTx = airUtilTx != null ? airUtilTx : this.airUtilTx;
        this.uptimeS = uptimeS != null ? uptimeS : this.uptimeS;
        return this;
    }

    /** Sets when the node was last heard, keeping the later of the two. */
    public MeshNodeState heard(Instant at) {
        if (at != null && (lastHeardAt == null || at.isAfter(lastHeardAt))) {
            lastHeardAt = at;
        }
        return this;
    }

    /** Merges a MeshCore advert: its own timestamp and when it was received; null arguments keep the current value. */
    public MeshNodeState advert(Instant advertTimestamp, Instant receivedAt) {
        this.advertTimestamp = advertTimestamp != null ? advertTimestamp : this.advertTimestamp;
        if (receivedAt != null && (lastAdvertAt == null || receivedAt.isAfter(lastAdvertAt))) {
            lastAdvertAt = receivedAt;
        }
        return this;
    }

    /** Sets the repeater health ({@code ok}, {@code late}, {@code silent}, or null). */
    public MeshNodeState advertStatus(String status) {
        this.advertStatus = status;
        return this;
    }

    /** The repeater health, or null. */
    public String advertStatus() {
        return advertStatus;
    }

    /** When the last advert was received, or null. */
    public Instant lastAdvertAt() {
        return lastAdvertAt;
    }

    /** The node type (MeshCore), or null. */
    public String nodeType() {
        return nodeType;
    }

    private static <T> T keep(T given, T current) {
        if (given instanceof String text && text.isBlank()) {
            return current;
        }
        return given != null ? given : current;
    }
}
