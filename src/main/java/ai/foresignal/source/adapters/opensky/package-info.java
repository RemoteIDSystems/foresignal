/**
 * {@code opensky-states}: worldwide civil aircraft from the OpenSky Network's {@code /states/all}
 * (prompt 062), credit-governed.
 *
 * <p>OAuth2 client credentials when the operator stores {@code clientId} and {@code clientSecret}, anonymous
 * otherwise; basic authentication ended on 2026-03-18 and is never sent. Every response's
 * {@code X-Rate-Limit-Remaining} is kept in adapter state, and a fetch the remaining credits would not cover
 * (plus the {@code creditReserve}) is deferred before anything is sent. The data's terms forbid
 * redistribution, so the source is {@code egress: LOCAL_ONLY}.
 */
package ai.foresignal.source.adapters.opensky;
