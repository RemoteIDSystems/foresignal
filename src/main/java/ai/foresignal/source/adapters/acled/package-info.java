/**
 * {@code acled} (prompt 081): Armed Conflict Location &amp; Event Data events from the ACLED API
 * ({@code acleddata.com/api/acled/read}, OAuth password grant at {@code acleddata.com/oauth/token}) and their
 * deletions ({@code /api/deleted/read}).
 *
 * <p>Terms, verified 2026-10-13 in the ACLED End User License Agreement ({@code acleddata.com/eula}, last updated
 * 8 July 2025): a non-commercial license; material published or distributed externally must be transformative and
 * must not allow the dataset to be recreated, and making the data available through one's own dashboard is not
 * sufficient. So the source is {@code egress: LOCAL_ONLY}: served to this instance's own clients, never re-served by
 * sinks, federation, share links, feed keys or exports.
 */
package ai.foresignal.source.adapters.acled;
