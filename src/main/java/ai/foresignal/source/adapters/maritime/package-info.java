/**
 * Maritime safety and trade intelligence (prompt 088): NGA navigational warnings with geometry recovered from their
 * free text ({@code nga-msi-warnings}, {@link ai.foresignal.source.adapters.maritime.MsiCoordinateExtractor}), IMF
 * PortWatch chokepoint and port activity on prompt 073's ArcGIS client ({@code imf-portwatch-chokepoints},
 * {@code imf-portwatch-ports}) and Global Fishing Watch vessel events ({@code gfw-events}, CC BY-NC 4.0). PortWatch
 * disruptions are a feature-kit spec ({@code kits/features/imf-portwatch.json}), not code here. They feed prompt
 * 099's chokepoint and port-call layers.
 */
package ai.foresignal.source.adapters.maritime;
