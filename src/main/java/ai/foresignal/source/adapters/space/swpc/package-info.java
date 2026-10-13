/**
 * The NOAA SWPC sources of prompt 077, which replace the single {@code noaa-swpc} placeholder: {@code swpc-alerts}
 * (messages and scales), {@code swpc-kp} (estimated and official Kp), {@code swpc-rtsw} (downsampled solar wind),
 * {@code swpc-xray} (flares and the current X-ray class) and {@code swpc-aurora} (OVATION probability at places,
 * private).
 *
 * <p>Every file is under {@code https://services.swpc.noaa.gov/} and was verified 2026-10 to send {@code ETag},
 * {@code Last-Modified} and {@code Cache-Control: max-age=60}, so every request is a conditional GET. Several files
 * are megabytes and change every minute; the large ones ({@code rtsw_wind_1m.json}, the OVATION grid) are read at
 * most hourly and every fifteen minutes respectively. Timestamps without a zone are UTC.
 */
package ai.foresignal.source.adapters.space.swpc;
