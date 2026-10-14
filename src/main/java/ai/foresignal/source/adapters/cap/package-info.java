/**
 * The reviewed CAP feed family ({@code cap-*}, prompt 089): one source per entry of the committed,
 * human-reviewed {@value ai.foresignal.source.adapters.cap.CapFeeds#RESOURCE}, seeded offline from the WMO
 * Register of Alerting Authorities. Each source reads an Atom/RSS index and fetches only the CAP
 * documents whose link is new, then parses them with prompt 067's {@code CapParser} and applies the shared
 * update-chain rule so an {@code Alert}, its {@code Update}s and its {@code Cancel} touch one record.
 */
package ai.foresignal.source.adapters.cap;
