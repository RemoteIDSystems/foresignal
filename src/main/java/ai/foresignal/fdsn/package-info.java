/**
 * The FDSN event web service (fdsnws-event 1.2) over Foresignal's {@code earthquake} records (prompt 141):
 * {@code GET /fdsnws/event/1/query}, {@code /version} and {@code /application.wadl}.
 *
 * <ul>
 *   <li>{@link ai.foresignal.fdsn.FdsnEventQuery} parses and validates the standard parameters (long and short names);
 *       a bad or unsupported parameter is an {@link ai.foresignal.fdsn.FdsnRequestException}, answered with the FDSN
 *       plain-text error body.
 *   <li>{@link ai.foresignal.fdsn.FdsnEventService} reads current {@code earthquake} records through the normal query
 *       service, so private sources, the caller's restriction and retention apply exactly as on {@code /v1/records}.
 *   <li>{@link ai.foresignal.fdsn.QuakeMlWriter} writes QuakeML 1.2 BED (event, origin, magnitude) with publicIDs
 *       {@code smi:foresignal/{source}/{externalId}}; {@link ai.foresignal.fdsn.FdsnTextWriter} writes the
 *       pipe-separated text format.
 * </ul>
 */
package ai.foresignal.fdsn;
