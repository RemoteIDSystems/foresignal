/**
 * KML 2.2 and KMZ for Google Earth and Cesium (prompt 058): self-refreshing NetworkLinks, current
 * records as Placemarks, {@code gx:Track} history from the unsimplified track service and trigger
 * geofences. {@link ai.foresignal.kml.KmlWriter} escapes every string for XML and HTML (Google Earth
 * renders balloons as HTML) and never wraps upstream text in CDATA; {@link ai.foresignal.kml.KmlService}
 * applies the caps, the private filter and {@code EgressPolicy.allows(FEED, …)}.
 */
package ai.foresignal.kml;
