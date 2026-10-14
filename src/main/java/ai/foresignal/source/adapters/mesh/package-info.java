/**
 * The mesh sources of prompt 083, built without repeating ShadowBroker's excess broker traffic and without any
 * Meshtastic protobuf (GPL-3.0): every Meshtastic path reads the firmware's JSON output.
 *
 * <ul>
 *   <li>{@code meshtastic-mqtt}: the public broker under fixed rules ({@code MeshtasticMqttRules}), disabled by
 *       default.
 *   <li>{@code meshtastic-map}: the community map's node list, daily, parsed as a stream.
 *   <li>{@code meshtastic-local}: the operator's own node through the operator's broker ({@code mqtt://} or
 *       {@code mqtts://} LAN URL); private.
 *   <li>{@code meshcore-local}: the operator's own MeshCore companion over its native TCP protocol, through the shared
 *       {@code MeshCoreCompanionLink} and its command allowlist; private.
 *   <li>{@code meshcore-bridge}: the same companion pushed by {@code contrib/meshcore-bridge}; private.
 * </ul>
 *
 * Shared model: {@code MeshRecords} (kinds {@code mesh-node} and {@code mesh-message}), the per-node merge
 * ({@code MeshNodeStates}), and the in-process {@code MeshMessageBus} that prompt 084 reads. The MeshCore protocol itself
 * is shared format code in {@code source.formats.meshcore}.
 */
package ai.foresignal.source.adapters.mesh;
