/**
 * Outbound network policy (prompt 035): {@link ai.foresignal.net.OutboundUrlPolicy}, the one SSRF
 * guard every operator-supplied outbound URL passes on save and on send, the IANA special-purpose
 * address table it judges addresses with ({@link ai.foresignal.net.SpecialPurposeAddresses}), and
 * the connect-time pinning shared by the {@code java.net.http} senders
 * ({@link ai.foresignal.net.PinnedHttp}).
 */
package ai.foresignal.net;
