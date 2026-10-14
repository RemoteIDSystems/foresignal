/**
 * The official sanctions lists (prompt 091): US OFAC ({@code ofac-sdn}), the FCDO UK Sanctions List
 * ({@code uk-sanctions-list}) and the UN consolidated list ({@code un-sanctions}). A {@code sanction} record has
 * no geometry; vessel IMO/MMSI, aircraft tail numbers and UN references become entity keys for correlation
 * (prompt 098). A delisting is a tombstone, guarded against an implausible list shrink.
 *
 * <p>The SEC 8-K current-filings feed lives in {@code ai.foresignal.source.adapters.sec}.
 */
package ai.foresignal.source.adapters.sanctions;
