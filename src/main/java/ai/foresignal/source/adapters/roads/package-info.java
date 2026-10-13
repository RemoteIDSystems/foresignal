/**
 * Road events and work zones (prompt 074): current incidents, closures, roadworks and restrictions from four open
 * formats, all normalized into one {@code road-event} kind with one shared payload ({@link
 * ai.foresignal.source.adapters.roads.RoadEventPayload}).
 *
 * <ul>
 *   <li>{@code open511-*}: Open511 servers of a reviewed list ({@link ai.foresignal.source.adapters.roads.Open511Family});
 *   <li>{@code wzdx-*}: WZDx 4.x feeds of a vendored, reviewed snapshot of the USDOT WZDx Feed Registry
 *       ({@link ai.foresignal.source.adapters.roads.WzdxFamily});
 *   <li>{@code digitraffic-road}: Fintraffic's traffic-message JSON API
 *       ({@link ai.foresignal.source.adapters.roads.DigitrafficRoadAdapter}); Digitraffic's DATEX II is deliberately
 *       not used;
 *   <li>{@code ndw-situations}, {@code ndw-srti}: NDW's DATEX II v3 situation publications
 *       ({@link ai.foresignal.source.adapters.roads.NdwFamily}).
 * </ul>
 *
 * <p>Every source fetches the full current set. An event missing from two consecutive complete fetches is
 * tombstoned ({@link ai.foresignal.source.support.AbsenceTombstoner}); an event the upstream marks as ended is
 * tombstoned at once. No upstream contact data (phone numbers, e-mail addresses) reaches a payload. Every source ships
 * disabled.
 */
package ai.foresignal.source.adapters.roads;
