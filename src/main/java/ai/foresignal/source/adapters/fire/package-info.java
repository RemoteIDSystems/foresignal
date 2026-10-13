/**
 * Wildfire sources (prompt 075): US interagency perimeters ({@code nifc-perimeters}, WFIGS through the ArcGIS
 * feature kit), InciWeb incident summaries ({@code inciweb-incidents}, the feed kit with a reviewed item mapper) and
 * the Canadian Wildland Fire Information System ({@code cwfis-perimeters}, {@code cwfis-fwi-stations},
 * {@code cwfis-hotspots}, WFS through the feature kit).
 *
 * <p>HMS smoke and fire stay in {@code ai.foresignal.source.adapters.hms} (prompt 048); FIRMS (prompt 065) is the
 * primary hotspot source. No adapter here fetches an HTML page.
 */
package ai.foresignal.source.adapters.fire;
