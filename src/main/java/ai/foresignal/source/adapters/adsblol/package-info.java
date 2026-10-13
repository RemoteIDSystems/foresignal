/**
 * The adsb.lol aircraft sources (prompt 062): {@code adsblol-mil} (military aircraft worldwide) and the
 * {@code adsblol-area-1..3} family of operator-centered circles of at most 250 nm.
 *
 * <p>There is no global adsb.lol source: the published API declares {@code radius ≤ 250} on
 * {@code /v2/point/{lat}/{lon}/{radius}} and has no {@code /v2/all}. Worldwide civil coverage comes from
 * {@code opensky-states}.
 *
 * <p>Etiquette: Foresignal's identifiable User-Agent, {@code Retry-After} honored, 30 s after a 429 without
 * it, and an HTTP 451 or 403 treated as a refusal of this client (one-hour backoff, health {@code failing},
 * Operator Guide "adsb.lol access"). Never a browser User-Agent, browser headers or another egress.
 * Records are mapped by the shared {@link ai.foresignal.source.formats.readsb.ReadsbAircraftMapper}.
 */
package ai.foresignal.source.adapters.adsblol;
