/**
 * The chronicle network (prompt 134, Intelligence-Design §12.6): discovery of related chronicles, on this instance
 * and across consenting peers, from privacy-preserving fingerprints of what they captured, never from their contents.
 *
 * <ul>
 *   <li><b>Keys:</b> {@link ai.foresignal.chronicle.network.EvidenceKeys} derives global evidence keys (upstream event
 *       ids per originator, canonical-event cells, entity keys per day, area-time cells and topics) from captured
 *       rows ({@link ai.foresignal.chronicle.network.ChronicleKeyExtractor}); {@link
 *       ai.foresignal.chronicle.network.KeyedHasher} hashes them with HMAC-SHA256 under a per-instance salt (local
 *       discovery) or a per-network salt shared by consenting peers ({@link
 *       ai.foresignal.chronicle.network.NetworkSalts}).</li>
 *   <li><b>Sketches:</b> {@link ai.foresignal.chronicle.network.MinHashSketch} (128 seeded permutations) per chronicle,
 *       key type and salt, kept up to date by {@link ai.foresignal.chronicle.network.ChronicleSketcher}; hashed key
 *       sets stay on this instance ({@code chronicle_key}).</li>
 *   <li><b>Discovery:</b> {@link ai.foresignal.chronicle.network.ChronicleDiscoveryJob} finds candidate pairs by LSH
 *       banding (16 bands of 8 rows), estimates Jaccard and containment, computes exact overlap for local pairs, and
 *       maintains {@code chronicle_link} and {@code chronicle_cluster}.</li>
 *   <li><b>Disclosure:</b> {@link ai.foresignal.chronicle.network.Disclosure} decides what a viewer may learn about a
 *       link (full, opaque or nothing) from visibility, the owner's discoverability and TLP ({@link
 *       ai.foresignal.chronicle.network.DiscoverabilityLevel}); anti-probing limits apply.</li>
 *   <li><b>Federation and access:</b> {@link ai.foresignal.chronicle.network.FederatedSketches} exchanges signed sketch
 *       packages with 113 peers; {@link ai.foresignal.chronicle.network.ChronicleAccessService} lets analysts ask for
 *       access without revealing their own work.</li>
 * </ul>
 */
package ai.foresignal.chronicle.network;
