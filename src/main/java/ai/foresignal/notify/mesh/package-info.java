/**
 * Mesh radio alert channels (prompt 084): the {@code meshtastic} and {@code meshcore} channel types' shared compact
 * message profile ({@link ai.foresignal.notify.mesh.MeshShortTextFormatter}, the CBOR profile in
 * {@code notify.mesh.cbor}), LoRa time-on-air ({@link ai.foresignal.notify.mesh.LoRaAirtime}), the per-channel airtime
 * governor ({@link ai.foresignal.notify.mesh.AirtimeGovernor}) every transmission passes, the transmitter behind the
 * non-HTTP delivery path ({@link ai.foresignal.notify.mesh.MeshTransmitter}) and the field-report grammar in
 * {@code notify.mesh.report}. Neither type can be configured to transmit on a public or default mesh channel.
 */
package ai.foresignal.notify.mesh;
