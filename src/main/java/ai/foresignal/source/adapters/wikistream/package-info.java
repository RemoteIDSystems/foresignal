/**
 * The Wikipedia edit-surge source {@code wiki-edit-surge} (prompt 095): a {@link
 * ai.foresignal.source.adapters.wikistream.SurgeDetector} over the Wikimedia EventStreams {@code recentchange}
 * SSE feed. Disabled by default; one connection with {@code Last-Event-ID} resume, a clean 15-minute server close
 * reconnecting at once, and an error ladder of 1 s → 5 s → 30 s → 120 s. Contributor usernames, comments and
 * parsed comments are never stored; only a hashed editor name is held in memory inside the detector. A rising
 * surge is geotagged once against the wiki's own API, restricted to a {@code *.wikipedia.org} host.
 */
package ai.foresignal.source.adapters.wikistream;
