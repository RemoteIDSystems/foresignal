/**
 * {@code ua-air-raid-siren} (prompt 081): Ukrainian air-raid alerts from siren.pp.ua, a keyless public wrapper of
 * {@code api.ukrainealarm.com} run by an individual ({@code github.com/PaulAnnekov/uasiren}).
 *
 * <p>Terms, verified 2026-10-13: the README states a limit of 50 requests per minute per IP and no license or terms.
 * The source ships disabled; {@code alerts-in-ua} is the preferred, keyed source, and the official
 * {@code api.ukrainealarm.com} needs an application key.
 */
package ai.foresignal.source.adapters.uasiren;
