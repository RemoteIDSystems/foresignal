/**
 * River, snow, drought and coastal water sources (prompt 079), one sub-package per source:
 *
 * <ul>
 *   <li>{@code usgswater}: USGS real-time gauges through the Water Data OGC API ({@code usgs-water});
 *   <li>{@code nwps}: NOAA NWPS river gauges with official flood categories and stages ({@code nwps-gauges});
 *   <li>{@code snotel}: NRCS SNOTEL snowpack through the AWDB REST API ({@code snotel});
 *   <li>{@code usdm}: the weekly U.S. Drought Monitor map ({@code usdm});
 *   <li>{@code coops}: NOAA CO-OPS tide gauges with flood thresholds ({@code noaa-coops-tides});
 *   <li>{@code ea}: Environment Agency flood warnings for England ({@code ea-flood-warnings}, optional);
 *   <li>{@code eccc}: ECCC real-time hydrometric stations as a feature-kit spec ({@code eccc-hydrometric},
 *       optional).
 * </ul>
 *
 * <p>Every gauge, snow and tide source is a point measurement on the Core-Data-Model §2 "Measurements" time base
 * ({@code observedAt = eventTime} = measurement time, TTL anchored on {@code observedAt}), STANDARD history, policy
 * ALL, the {@code state} retention class (365 d); drought areas and flood warnings are the {@code event} class
 * (1825 d). Sentinels ({@code -999}, {@code -9999}, {@code 0001-01-01T00:00:00Z}) become omitted fields. Every source
 * is disabled by default. {@link ai.foresignal.source.adapters.hydro.HydroSources} holds what they share; adapters
 * never import each other.
 */
package ai.foresignal.source.adapters.hydro;
