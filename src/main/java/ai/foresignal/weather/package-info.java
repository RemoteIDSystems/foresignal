/**
 * Weather intelligence (prompt 056; Weather-Imagery-Design §1.1 goal 4, §7.5): the same deterministic,
 * status-carrying weather facts on every intelligence surface.
 *
 * <ul>
 *   <li>{@link ai.foresignal.weather.WeatherContextService} computes an alert's {@code context.weather} from the
 *       frame caches and record heads only (never an upstream fetch), within a 500 ms budget;
 *   <li>{@link ai.foresignal.weather.AlertWeatherContext} attaches it to geolocated alerts before dispatch, and
 *       {@link ai.foresignal.weather.WeatherTemplateVars} exposes it to message templates;
 *   <li>{@link ai.foresignal.weather.WeatherEnrichmentContributor} feeds it to enrichment, and
 *       {@link ai.foresignal.weather.WeatherAtPlacesSection} adds "Weather at places" to briefings and digests;
 *   <li>{@link ai.foresignal.weather.WeatherCitationExtractor} and {@link ai.foresignal.weather.WeatherAnswerGuard}
 *       make Ask cite weather values and never let a non-ok section pass as clear;
 *   <li>{@link ai.foresignal.weather.WeatherSettingsService} owns the {@code weather.*} settings.
 * </ul>
 *
 * <p>Every section carries {@code status} ({@code ok}, {@code stale}, {@code no-coverage}, {@code unavailable});
 * anything but {@code ok} is unknown, never clear weather, and is worded as such.
 */
package ai.foresignal.weather;
