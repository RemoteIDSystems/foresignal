/**
 * The operator's own hardware on the LAN (prompt 069): ADS-B receivers, receiver health, GNSS and weather
 * stations, each a family of numbered slots registered by {@link
 * ai.foresignal.source.adapters.local.LocalReceiversFamily}.
 *
 * <ul>
 *   <li>{@code local-adsb-{n}}: readsb, dump1090 or skyaware978 {@code aircraft.json} (POLLING);
 *   <li>{@code local-sbs-{n}}: SBS-1 BaseStation lines over TCP (STREAMING, codec {@code sbs1});
 *   <li>{@code local-gpsd-{n}}: gpsd JSON over TCP (STREAMING, codec {@code gpsd}), private;
 *   <li>{@code station-readsb-{n}}: receiver health from readsb {@code stats.json} (POLLING);
 *   <li>{@code wxstation-davis-{n}}: Davis WeatherLink Live current conditions (POLLING);
 *   <li>{@code wxstation-ecowitt-{n}}: Ecowitt gateway live data (POLLING, experimental).
 * </ul>
 *
 * <p><strong>The LAN exception to the static-feed-list rule:</strong> the slot lists are code; the operator
 * supplies only {@code LAN_URL} parameters, which resolve once to a loopback, private or unique-local
 * address and are pinned, so no slot can reach the internet. Every descriptor is disabled by default and
 * {@code egress} {@code LOCAL_ONLY}, overridable by the operator ({@code egressOverridable}); MLAT positions
 * never leave regardless ({@code EgressPolicy}).
 *
 * <p>Ported in part from God's Eye View (MIT): {@code server/providers/local-receivers.js},
 * {@code src/sources/adsbRecords.js} and {@code src/data/tapAddress.js}; see {@code THIRD_PARTY_NOTICES.md}.
 */
package ai.foresignal.source.adapters.local;
