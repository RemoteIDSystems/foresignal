/**
 * Keyless vessel sources (prompt 085), all on the shared AIVDM codec ({@code ai.foresignal.codec.ais}) and prompt
 * 063's {@code vessel} payload contract:
 *
 * <ul>
 *   <li>{@code ais-kystverket}: the Norwegian Coastal Administration's open AIS stream, raw TCP with TAG blocks
 *       ({@link ai.foresignal.source.adapters.ais.KystverketAdapter});
 *   <li>{@code ais-digitraffic}: Fintraffic's Digitraffic marine MQTT over WSS, plus a 12-hourly metadata bootstrap
 *       ({@link ai.foresignal.source.adapters.ais.DigitrafficAisAdapter});
 *   <li>{@code local-ais-{n}}: the operator's own AIS-catcher over TCP
 *       ({@link ai.foresignal.source.adapters.ais.LocalAisAdapter}, {@link
 *       ai.foresignal.source.adapters.ais.LocalAisFamily}).
 * </ul>
 */
package ai.foresignal.source.adapters.ais;
