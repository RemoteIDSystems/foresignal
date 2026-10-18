/**
 * Intelligence exchange between Foresignal instances (prompt 132, Intelligence-Design §12.4): signed intelligence
 * packages.
 *
 * <ul>
 *   <li><b>Format</b> ({@value ai.foresignal.exchange.ExchangeVocabulary#MEDIA_TYPE}): a ZIP with
 *       {@code manifest.json} (package id, type, sender instance and key id, authors, TLP, recipients, content hashes,
 *       provenance chain), {@code content/} (the JSON bundle, Markdown and HTML renditions, attachments by hash, a
 *       chronicle extract) and {@code signature.jws}, a detached JWS over the exact manifest bytes with the instance
 *       key (109). {@link ai.foresignal.exchange.PackageCodec} writes and reads it.</li>
 *   <li><b>Sending:</b> {@link ai.foresignal.exchange.ExchangeService} builds packages (TLP-checked per recipient;
 *       private and {@code LOCAL_ONLY} material reduced to counts per {@code EgressPolicy}), delivers them to 113 peers
 *       or keeps them for download, and tracks receipts.</li>
 *   <li><b>Receiving:</b> {@link ai.foresignal.exchange.ExchangeInbox} verifies the signature against the sender's
 *       JWKS (pinned or fetched from the peer) and every content hash, checks the recipients, enforces TLP locally and
 *       imports read-only: {@code remote:{instanceId}/{type}:{id}} references with local snapshots that never enter
 *       local evidence or the ledger unless an analyst copies an item deliberately (RPT-2).</li>
 * </ul>
 */
package ai.foresignal.exchange;
