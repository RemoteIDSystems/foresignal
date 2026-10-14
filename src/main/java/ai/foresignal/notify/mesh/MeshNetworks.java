package ai.foresignal.notify.mesh;

/** The two mesh networks and channel type ids (prompt 084). */
public final class MeshNetworks {

    /** Meshtastic: the {@code meshtastic} channel type, an MQTT JSON downlink. */
    public static final String MESHTASTIC = "meshtastic";

    /** MeshCore: the {@code meshcore} channel type, the companion protocol. */
    public static final String MESHCORE = "meshcore";

    private MeshNetworks() {}
}
