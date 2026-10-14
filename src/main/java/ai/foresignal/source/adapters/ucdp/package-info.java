/**
 * {@code ucdp-ged} (prompt 081): UCDP Georeferenced Event Dataset candidate events from the UCDP API
 * ({@code ucdpapi.pcr.uu.se/api/gedevents/{version}}), with the monthly candidate release discovered daily.
 *
 * <p>Terms, verified 2026-10-13: every request needs the header {@code x-ucdp-access-token} (tokens are issued on
 * request by the API maintainer; without one the API answers 401); usage is limited to 5,000 requests per day, errors
 * included. The UCDP Download Center states that all UCDP datasets are licensed under CC BY 4.0.
 */
package ai.foresignal.source.adapters.ucdp;
