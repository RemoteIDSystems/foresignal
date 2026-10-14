/**
 * {@code alerts-in-ua} (prompt 081): Ukrainian air-raid and threat alerts from the keyed alerts.in.ua API
 * ({@code api.alerts.in.ua/v1/alerts/active.json}), with oblast and raion polygons from the bundled boundaries.
 *
 * <p>Terms, verified 2026-10-13 in the developer docs ({@code devs.alerts.in.ua}): a personal token from a request
 * form, sent as {@code Authorization: Bearer} and kept server-side (public services must proxy); a soft limit of
 * 8–10 and a hard limit of 12 requests per minute per IP ({@code 429}, systematic violation blocks the IP and
 * token); {@code If-Modified-Since} with {@code Last-Modified} for caching. No license is published; the docs carry a
 * no-liability disclaimer and say not to use the API for critical infrastructure.
 */
package ai.foresignal.source.adapters.alertsinua;
