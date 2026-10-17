/**
 * Federation and edge relay (prompt 113): global record references ({@link ai.foresignal.federation.RecordRef}),
 * the federation feed served to peers ({@link ai.foresignal.federation.FederationFeedService}), the
 * {@code foresignal-peer} source family that consumes another instance's feed with provenance chains, loop
 * prevention and cross-path dedup ({@link ai.foresignal.federation.PeerService},
 * {@link ai.foresignal.federation.PeerSession}, {@link ai.foresignal.federation.FederationIngestor},
 * {@link ai.foresignal.federation.OriginLedger}), the hub endpoint of an edge relay and its
 * {@code federation-push} sink.
 */
package ai.foresignal.federation;
