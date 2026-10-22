/**
 * Environmental measurement sources (prompt 078), one sub-package per source:
 *
 * <ul>
 *   <li>{@code openaq}: OpenAQ v3 latest measurements per parameter ({@code openaq}, key required);
 *   <li>{@code safecast}: Safecast realtime fixed radiation devices ({@code safecast-radiation});
 *   <li>{@code radnet}: EPA RadNet fixed stations, the last row of each station's yearly CSV ({@code epa-radnet});
 *   <li>{@code ndbc}: NOAA NDBC buoy and station latest observations ({@code ndbc-buoys});
 *   <li>{@code sensorcommunity}: Sensor.Community sensors in one area ({@code sensor-community});
 *   <li>{@code airnow}: U.S. EPA AirNow hourly monitor data in a box ({@code airnow-hourly}, key required);
 *   <li>{@code airq}: the operator's own PurpleAir, airrohr and AirGradient monitors polled on the LAN
 *       ({@code airq-local-{n}}, private, {@code LOCAL_ONLY}).
 * </ul>
 *
 * <p>Every source is a point measurement on the Core-Data-Model §2 "Measurements" time base ({@code observedAt} =
 * measurement time, {@code eventTime} = the end of the measurement period, TTL anchored on {@code observedAt}),
 * STANDARD history and the {@code state} retention class (365 d). Each starts enabled unless it needs a credential or a
 * parameter (the seeding rule of prompt 145), so the keyed, boxed and LAN sources wait for their configuration.
 * {@link ai.foresignal.source.adapters.env.EnvSources} holds what the sources share; adapters never import each other.
 */
package ai.foresignal.source.adapters.env;
