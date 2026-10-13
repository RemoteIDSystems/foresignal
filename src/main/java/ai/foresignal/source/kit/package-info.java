/**
 * Protocol-level source kits (prompt 073): a reviewed resource file instead of a hand-written adapter.
 *
 * <ul>
 *   <li>{@link ai.foresignal.source.kit.feature}: the <strong>feature kit</strong> (ArcGIS FeatureServer and MapServer
 *       query, OGC API Features, WFS GeoJSON), one source per spec in {@code kits/features/*.json};
 *   <li>{@link ai.foresignal.source.kit.feed}: the <strong>feed kit</strong> (RSS 2.0, RSS 1.0, Atom 1.0, JSON Feed,
 *       with GeoRSS and W3C Basic Geo), one source per spec in {@code kits/feeds/*.json};
 *   <li>{@link ai.foresignal.source.kit.websub}: opt-in WebSub push for feed sources.
 * </ul>
 *
 * <p>Spec lists are static and code-reviewed (Architecture §4.2), validated at startup, and never accepted over the
 * API. An operator overlay under {@code ${FORESIGNAL_DATA_DIR}/kits/} is loaded only when
 * {@code foresignal.kits.operator-specs.enabled=true}, and every overlay URL must pass the outbound URL policy.
 */
package ai.foresignal.source.kit;
