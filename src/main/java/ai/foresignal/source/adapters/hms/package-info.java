/**
 * The NOAA Hazard Mapping System sources (prompt 048): {@code hms-smoke} (analyst smoke polygons, KML,
 * kind {@code smoke-polygon}) and {@code hms-fire} (analyst fire points, text, kind
 * {@code fire-detection}, shared with {@code goes-fdc}). Both read the daily files of the current UTC
 * day, and yesterday's during the first hours after midnight, with conditional GETs.
 */
package ai.foresignal.source.adapters.hms;
