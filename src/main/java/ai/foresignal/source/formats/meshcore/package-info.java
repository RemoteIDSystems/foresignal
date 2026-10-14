/**
 * The MeshCore companion protocol (prompt 083), from MeshCore's MIT protocol documentation
 * ({@code docs/companion_protocol.md}) and its companion firmware's frame layouts: the TCP framing of the WiFi
 * companion firmware ({@link ai.foresignal.source.formats.meshcore.MeshCoreFrameDecoder}), the command and response
 * codes ({@link ai.foresignal.source.formats.meshcore.MeshCoreProtocol}), the frames Foresignal reads and the commands
 * it may build ({@link ai.foresignal.source.formats.meshcore.MeshCoreFrames}) and the allowlist every write passes
 * ({@link ai.foresignal.source.formats.meshcore.MeshCoreCommandGuard}). Pure code: no I/O, no clock. Shared by the
 * {@code meshcore-local} source, the {@code meshcore-companion} codec and, from prompt 084, the {@code meshcore}
 * channel.
 */
package ai.foresignal.source.formats.meshcore;
