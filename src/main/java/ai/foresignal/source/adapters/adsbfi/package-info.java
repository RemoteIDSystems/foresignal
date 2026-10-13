/**
 * The adsb.fi open-data aircraft sources (prompt 080): {@code adsbfi-mil} (aircraft flagged military, worldwide)
 * and the {@code adsbfi-region-1..4} family of operator-centered circles of at most 250 nm.
 *
 * <p>Every source calls the one host {@code opendata.adsb.fi}, whose public endpoints allow one request per
 * second and restrict an IP address that keeps making invalid requests (400, 401, 403, 404 and 429 count). The
 * descriptors therefore set {@code hostPacing} 1.1 s: {@code UpstreamClient}'s {@code HostPacer} keeps requests
 * to the host strictly serial across all five sources. A 429 backs off through the prompt 005 ladder (or its
 * {@code Retry-After}); a 403 or 451 is a refusal of this client ({@code accessDeniedIsTerminal}: one-hour
 * backoff, health {@code failing}). Nothing is ever rotated, spoofed or retried with other headers.
 *
 * <p>Terms (verified 2026-10-13, {@code github.com/adsbfi/opendata}): personal, non-commercial use only; cite
 * adsb.fi with a link to its home page. The sources are {@code LOCAL_ONLY} by default and disabled. Records are
 * mapped by the shared {@link ai.foresignal.source.formats.readsb.ReadsbAircraftMapper}; MLAT positions carry
 * {@code posSource = mlat} and {@code quality.positionSource = CALCULATED}.
 */
package ai.foresignal.source.adapters.adsbfi;
