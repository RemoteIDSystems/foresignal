/**
 * Space weather, small bodies and ground stations (prompt 077), one sub-package per source family:
 *
 * <ul>
 *   <li>{@code swpc}: five focused NOAA SWPC sources ({@code swpc-alerts}, {@code swpc-kp}, {@code swpc-rtsw},
 *       {@code swpc-xray}, {@code swpc-aurora}) that replace the single {@code noaa-swpc} placeholder;
 *   <li>{@code donki}: NASA DONKI space-weather events and notifications ({@code nasa-donki});
 *   <li>{@code jpl}: JPL fireballs and asteroid close approaches ({@code jpl-fireballs},
 *       {@code jpl-close-approaches});
 *   <li>{@code satnogs}: SatNOGS Network ground stations ({@code satnogs-stations});
 *   <li>{@code sondehub}: SondeHub radiosondes near a point ({@code sondehub});
 *   <li>{@code localsonde}: radiosonde_auto_rx {@code PAYLOAD_SUMMARY} datagrams on the LAN ({@code local-sonde}).
 * </ul>
 *
 * <p>Every source is STANDARD, ALL and disabled by default. {@link ai.foresignal.source.adapters.space.SpaceSources}
 * holds what the polling sources share; adapters never import each other.
 */
package ai.foresignal.source.adapters.space;
